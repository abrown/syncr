package dropbag;

import java.util.ArrayList;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 *
 * @author andrew
 */
public class FileWatcher extends Thread{
    
    protected Application parent;
    
    public FileWatcher(Application app){
        this.parent = app;
    }
    
    public void scan(){
        // scan for changes
    }
    
    public void run(){
        // poll
        for (;;) {           
            // wait for key to be signaled
            WatchKey key;
            try {
                key = this.parent.watch_service.take();
            } catch (InterruptedException x) {
                this.parent.notifyObservers(x);
                return;
            }
            // get changed directory
            String d = this.parent.watch_keys.get(key); // changed directory
            if (d == null) {
                this.parent.notifyObservers("WatchKey not recognized.");
                continue;
            }
            // push to stack
            ArrayList<WatchEvent<?>> stack = (ArrayList) key.pollEvents();
            //System.out.println(stack);
            // determine event type
            for (WatchEvent<?> event: stack) {
                WatchEvent.Kind kind = event.kind();
                // manually rescan (from StandardWatchEventKinds: indicates that events may have been lost or discarded)
                if (kind == OVERFLOW){
                    this.scan();
                    continue;
                }     
                // get changed file
                WatchEvent<Path> ev = (WatchEvent<Path>) event; 
                Path path = Paths.get(d).resolve( ev.context() ); // absolute path
                File file = path.toFile();
                // fire event
                FileEvent e = new FileEvent(path);
                if( file.isDirectory() && kind.name().equals("ENTRY_DELETE") ){
                    this.parent.fireFileEvent(Application.DIR_DELETED, e);
                }
                else if( file.isDirectory() && kind.name().equals("ENTRY_CREATE") ){
                    this.parent.fireFileEvent(Application.DIR_CREATED, e);
                }                
                else if( file.isDirectory() && kind.name().equals("ENTRY_MODIFIED") ){
                    this.parent.fireFileEvent(Application.DIR_CREATED, e);
                }
                else if( kind.name().equals("ENTRY_DELETE") ){
                    this.parent.fireFileEvent(Application.FILE_DELETED, e);
                }
                else if( kind.name().equals("ENTRY_CREATE") ){
                    this.parent.fireFileEvent(Application.FILE_CREATED, e);
                }
                else{
                    this.parent.fireFileEvent(Application.FILE_MODIFIED, e);
                }
            }
            // reset key
            if( !key.reset() ){ break; } 
        }
    }
}
