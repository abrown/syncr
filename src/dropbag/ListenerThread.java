package dropbag;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import static java.nio.file.StandardWatchEventKinds.*;
import java.io.*;
import java.util.*;

/**
 * Captures file events and organizes them for ClientThread use
 * @author andrew
 */
public class ListenerThread extends Thread{

    private final Map<WatchKey,String> keys;
    private Path dir;
    private WatchService listener;
    private long count;
    private ArrayList<Change> changes;

    /**
     * Constructor
     * @param dir
     * @throws IOException
     */
    ListenerThread(ClientDirectory dir){
        // initialize
        this.keys = new HashMap<WatchKey,String>();
        this.dir = dir.getPath().toPath(); // uses path, not file
        this.changes = new ArrayList<Change>();
        try{ this.listener = FileSystems.getDefault().newWatchService(); }
        catch(Exception e){ System.err.println(e); }       
    }
    
    /**
     * Return current changes
     * @return ArrayList<Change>
     */
    public synchronized ArrayList<Change> getChanges(){
        this.deduplicate();
        ArrayList<Change> out = (ArrayList<Change>) this.changes.clone();
        this.changes.clear();
        return out;
    }
    
    /**
     * Add a change
     * @param Change
     */
    public synchronized void add(Change c){
        this.changes.add(c);
    }
    
    /**
     * Organizes changes into a useful list
     * Requirements:
     *  remove nulls
     *  remove items not conforming to regex
     *  only one <modified> per file_name
     *  same time <delete> <create> turn in to <rename>
     *  same file_name <create> <delete> and <delete> <create> discarded as temporary
     */
    private synchronized void deduplicate(){
        // don't process an empty list
        if( this.changes.isEmpty() ) return;
        // setup
        ArrayList<Change> processed = new ArrayList<Change>();
        // add first
        p("add first");
        processed.add( this.changes.get(0) );
        // look through changes
        for(int i = 1; i<this.changes.size(); i++){
            Change c = this.changes.get(i);

            // remove nulls
            if( c.getName() == null ){ p("name null"); continue; }
            if( c.getModifiedDate() == null ){ p("modified null"); continue; }
            
            // remove items not conforming to regex
            if( Utility.isHidden(c.getName()) ) continue;
            // check for duplicates
            if( c.isModified() ){
                int end = processed.size();
                if( end < 1 ){ processed.add(c); continue; }
                // cycle backwards through
                boolean found = false;
                for(int j = end - 1; j >= 0; j--){
                    Change b = processed.get(j); // we call it b because, since the list is in time order, it ocurrs before c
                    // DUPLICATES: same file_name
                    if( !b.isModified() ) continue;
                    if( c.getName().equals(b.getName()) ){
                        p("replace mod");
                        found = true;
                        processed.set(j, c);
                        break;
                    }
                }
                if( !found ){ processed.add(c); }
                continue;
            }
            
            // same time <delete> <create> turn in to <rename>
            if( c.isCreated() ){
                // find 
                int end = processed.size();
                boolean found = false;
                Change r = new Change();
                for(int j = end - 1; j >= 0; j--){
                    Change b = processed.get(j); // we call it b because, since the list is in time order, it ocurrs before c
                    if( b.getModifiedDate().equals(c.getModifiedDate()) && b.isDeleted() ){
                        r.name = b.name;
                        r.data = c.name;
                        r.modified = c.modified;
                        r.type = Change.RENAMED;
                        p("rename");
                        processed.set(j, r); 
                        found = true;
                        break;
                    }
                }
                // delete prior mods to a rename
                if( found ){
                    for(int j = end - 2; j >= 0; j--){
                        Change b = processed.get(j); // we call it b because, since the list is in time order, it ocurrs before c
                        if( (b.isRenamed() && r.getName().equals(b.getRenamed()))
                             || (b.isModified() && r.getName().equals(b.getName())) ){
                            p("rename remove");
                            processed.remove(b);
                        }
                    }
                    continue;
                }
            }
            
            // same file_name <create> <delete> and <delete> <create> discarded as temporary
            if( c.isCreated() || c.isDeleted() ){
                int end = processed.size();
                boolean found = false;
                for(int j = end - 1; j >= 0; j--){
                    Change b = processed.get(j); // we call it b because, since the list is in time order, it ocurrs before c
                    if( !c.getName().equals(b.getName()) ) continue;
                    if( b.isCreated() && c.isDeleted() ){ 
                        p("temp"); 
                        processed.remove(b); 
                        found = true;
                        break; 
                    }
                    else if( b.isDeleted() && c.isCreated() ){ 
                        p("temp2"); 
                        processed.remove(b);
                        found = true;
                        break; 
                    }
                }
                if( found ) continue;
            }
   
            // NORMAL CHANGES
            p("normal");
            processed.add(c);
        }
        // save
        this.changes = processed;
    }
    
    private void p(String p){
        System.err.println(p);
    }
    
    /**
     * 
     * @return String
     */
    public String toString(){
        return "Listener Thread: " + this.dir.toString();
    }
    
    /**
     * Register a directory to listen to
     * @param dir
     * @throws IOException
     */
    private void register(Path dir) throws IOException {
        if( Utility.isHidden(dir.toString()) ) return;
        this.count++;
        WatchKey key = dir.register(this.listener, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir.toString());
    }

    /**
     * Recursively add each subdirectory to the listener
     * @param dir
     */
    private void registerAll(Path dir){
         try{
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>(){
                public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes attrs) {
                    try {
                        register(d);
                    } catch (IOException x) { throw new IOError(x); }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch(IOException e){
            System.err.println(e);
            return;
        }
    }
    
    /**
     * Listens for changes 
     */
    public void run(){
        
        // register folders
        System.out.println("Registering " + this.dir);
        registerAll(this.dir);
        System.out.println("Watching " + this.count + " folders");
        
        for (;;) {           
            // wait for key to be signaled
            WatchKey key;
            try {
                key = this.listener.take();
            } catch (InterruptedException x) {
                System.err.println(x);
                return;
            }
            // get changed directory
            String d = keys.get(key); // changed directory
            if (d == null) {
                System.err.println("WatchKey not recognized.");
                continue;
            }
            // determine event type
            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
                if (kind == OVERFLOW) continue;
                WatchEvent<Path> ev = (WatchEvent<Path>) event;            
                // get changed file
                Path e = new File(d).toPath(); // changed directory path
                Path f = ev.context(); // file name
                Path g = e.resolve(f); // absolute path
                String h = dir.relativize(g).toString();
                // only allow files past
                File i = new File(g.toString());
                if( i.isDirectory() ) continue;
                // populate change
                Change c = new Change();
                c.setName( h );
                c.setPath( g );
                c.setModifiedDate( new Date() );
                if( kind == ENTRY_DELETE ) c.setDeleted();
                else if( kind == ENTRY_CREATE ) c.setCreated();
                else c.setModified();
                //if( kind != ENTRY_DELETE ){
                //    try{ c.setChecksum( Rsync.getMD5(g.toString()) ); }
                //    catch(Exception x){ System.err.println(x); }
                //}
                // save change
                this.add(c);
            }
            // reset key
            if( !key.reset() ){ break; } // if cannot reset key, path no longer accessible
        }
    }
}
