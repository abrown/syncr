package rsync;

import rsync.RsyncBlock;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.*;

public class RsyncInstructions {

    public String name;
    public String checksum;
    public int[] data;
    private JsonReader in;
    private JsonWriter out;

    public RsyncInstructions(String filename, RsyncSignature signature){
        
    }
    
    public InputStream getInputStream() throws Exception{
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream();
        JsonWriter json = new JsonWriter(new OutputStreamWriter(out));
        // pipe json output to InputStream
        in.connect(out);
        // return
        return in;
    }
    
    
    public RsyncInstructions(JsonReader in) throws Exception {
        this.in = in;
        this.in.beginObject();
        while (in.hasNext()) {
            // get name
            String name = this.in.nextName();
            if (!name.equals("name")) {
                throw new Exception("Rsync instructions expected in order: name, md5, data.");
            }
            this.name = this.in.nextString();
            // get checksum
            name = this.in.nextName();
            if (!name.equals("md5")) {
                throw new Exception("Rsync instructions expected in order: name, md5, data.");
            }
            this.checksum = this.in.nextString();
            // get adlers
            name = this.in.nextName();
            if (!name.equals("data")) {
                throw new Exception("Rsync instructions expected in order: name, md5, data.");
            }
            this.in.beginArray();
        }
    }

    public boolean hasNext() throws Exception {
        if (this.in == null) {
            throw new Exception("Attempting to access non-initialized JsonReader");
        }
        if( this.in.hasNext() ) return true;
        else{ this.in.endArray(); this.in.endObject(); return false; }
    }

    public RsyncBlock getNext() throws Exception {
        this.in.beginObject();
        int index = -1;
        boolean modified = false;
        byte[] data = null;
        while( this.in.hasNext() ){
            String name = this.in.nextName();
            if( name.equals("i") ) index = this.in.nextInt();
            else if( name.equals("m") ) modified = this.in.nextBoolean();
            else if( name.equals("d") ) data = this.in.nextString().getBytes();
        }
        this.in.endObject();
        // return
        return new RsyncBlock(index, modified, data);
    }
}
