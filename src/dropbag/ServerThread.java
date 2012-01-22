package dropbag;

import rsync.RsyncSignature;
import rsync.RsyncInstructions;
import rsync.RsyncBlock;
import rsync.Rsync;
import com.google.gson.*;
import com.google.gson.stream.*;
import java.io.*;
import java.util.LinkedList;

public class ServerThread extends Thread {

    private ClientThread parent;
    private ServerProxy service;
    private LinkedList<Change> queue;

    public ServerThread(ClientThread parent) {
        this.parent = parent;
        this.service = new ServerProxy(this);
        this.queue = new LinkedList<Change>();
    }

    /**
     * Push/pull the list of changed files
     */
    public void run() {
        for (;;) {
            try {
                Thread.currentThread().sleep(Configuration.SERVER_PUT_INTERVAL);
            } catch (InterruptedException e) {
                Utility.log(e);
            }
            // push/pull any changes
            while (!this.queue.isEmpty()) {
                try {
                    Change c = this.remove(); // removes one off head of list
                    // process by type
                    if (c.isModified() || c.isCreated()) {
                        if (c.fromServer()) {
                            this.pullFile(c);
                        } else {
                            this.pushFile(c);
                        }
                    } else if (c.isDeleted()) {
                        this.removeFile(c);
                    } else if (c.isRenamed()) {
                        this.renameFile(c);
                    } else if (c.isSync()) {
                        this.syncFile(c);
                    } else {
                        throw new Exception("Change has no given type");
                    }
                } catch (Exception e) {
                    Utility.log(e);
                }
            }
        }
    }

    /**
     * Add to change queue
     * @param c 
     */
    public synchronized void add(Change c) {
        this.queue.add(c);
    }

    /**
     * Remove one off head of change queue
     * @return 
     */
    public synchronized Change remove() {
        return this.queue.remove();
    }

    /**
     * To String
     * @return 
     */
    public String toString() {
        return "ServerThread: " + Configuration.SERVER_URL;
    }

    /**
     * Utility method
     * @return 
     */
    public Thread getParent() {
        return parent;
    }

    /**
     * Utility method: ensures url does not contain double slashes
     * @return 
     */
    public String getUrl() {
        String c = Configuration.SERVER_URL;
        return (c.charAt(c.length() - 1) == '/') ? c.substring(0, c.length() - 2) : c;
    }

    /**
     * Returns list of files changed on the server since given date
     * @param since
     * @return
     * @throws Exception 
     */
    public java.util.ArrayList<Change> getChanges(java.util.Date since) throws Exception {
        String url = this.getUrl() + "/changes/*/rsync";
        // get change list
        java.util.ArrayList<Change> changes = new java.util.ArrayList<Change>();
        Change[] cs = this.service.get(url, Change[].class);
        for (int i = 0; i < cs.length; i++) {
            changes.add(cs[i]);
        }
        // return
        return changes;
    }

    /**
     * Pushes file data (using Rsync) from local file to the server file
     * @param c
     * @throws Exception 
     */
    public void pushFile(Change c) throws Exception {
        String filename = c.getPath().toString();
        String url = this.getUrl() + "/files/*/rsync";
        // get signature
        RsyncSignature signature = this.service.get(url, RsyncSignature.class);
        // get instructions
        RsyncInstructions instructions = new RsyncInstructions(filename, signature);
        // send instructions
        String url2 = this.getUrl() + "/files/*/put";
        Integer bytes_written = this.service.post(url, instructions.getInputStream(), Integer.class);
    }

    /**
     * Pulls file data (using Rsync) from server to local file
     * @param c
     * @throws Exception 
     */
    public void pullFile(Change c) throws Exception {
        String filename = c.getPath().toString();
        String url = this.getUrl() + "/files/*/get";
        // create json signature
        Gson gson = new Gson();
        RsyncSignature s = new RsyncSignature(c.getName(), filename);
        String signature = gson.toJson(s);
        // post data
        JsonReader json = this.service.postStreamed(url, signature);
        // open temp file
        File _temp = File.createTempFile(null, Configuration.TEMP_FILE_EXTENSION);
        FileOutputStream temp = new FileOutputStream(_temp);
        FileInputStream file = new FileInputStream(filename);
        int index = 0;
        byte[] buffer = new byte[Configuration.RSYNC_BLOCK_SIZE];
        String checksum = "";
        // read data into file
        RsyncInstructions instructions = new RsyncInstructions(json);
        while (instructions.hasNext()) {
            RsyncBlock b = instructions.getNext();
            // write from server rsync instructions
            if (b.isModified()) {
                temp.write(b.getData());
            } // write from local file
            else {
                int k = index * Configuration.RSYNC_BLOCK_SIZE;
                int l = k * Configuration.RSYNC_BLOCK_SIZE - 1;
                file.read(buffer, k, l);
                temp.write(buffer);
            }
        }
        json.close();
        file.close();
        temp.close();
        // MD5 check
        if (!Rsync.getMD5(_temp.getPath()).equals(checksum)) {
            throw new Exception("MD5 checksum failed");
        }
        // rename
        _temp.renameTo(new File(filename));
    }

    /**
     * Removes file from client or server, depending on change direction
     * @param c Change to be made
     * @throws Exception 
     */
    public void removeFile(Change c) throws Exception {
        // if change is from the server, remove file here
        if (c.fromServer()) {
            File f = new File(c.getPath().toString());
            if (f.delete()) {
                Utility.log("Deleted: " + f.toString());
            } else {
                Utility.log("Could not delete: " + f.toString());
            }
        } // if change is from client, remove file on server
        else {
            String filename = c.getPath().toString();
            String url = this.getUrl() + "/files/*/delete";
            // make change
            Boolean success = this.service.get(url, Boolean.class);
            if (success) {
                Utility.log("Deleted: " + filename);
            } else {
                Utility.log("Could not delete: " + filename);
            }
        }
    }

    /**
     * Renames a file on client or server
     * @param c Rename details
     * @throws Exception 
     */
    public void renameFile(Change c) throws Exception {
        // if change is from the server, remove file here
        if (c.fromServer()) {
            File f = new File(c.getPath().toString());
            if (f.renameTo(new File(c.getRenamed()))) {
                Utility.log("Renamed: " + f.toString());
            } else {
                Utility.log("Failed to rename: " + f.toString());
            }
        } // if change is from client, remove file on server
        else {
            String filename = c.getPath().toString();
            String url = this.getUrl() + "/files/*/rename";
            // make change
            Gson g = new Gson();
            String json = g.toJson(c);
            Boolean success = this.service.post(url, json, Boolean.class);
            if (success) {
                Utility.log("Renamed: " + filename);
            } else {
                Utility.log("Could not rename: " + filename);
            }
        }
    }

    /**
     * Requests a directive from the server for what to do with a file
     * @param c Change with checksum
     * @throws Exception 
     */
    public void syncFile(Change c) throws Exception {
        String filename = c.getPath().toString();
        String url = this.getUrl() + "/files/*/sync";
        // request change from server
        Gson g = new Gson();
        String json = g.toJson(c);
        Change d = this.service.post(url, json, Change.class);
        // process server directive
        d.setDirection(Change.FROM_SERVER);
        this.add(d);
    }
}
