package dropbag;

import java.util.EventObject;
import java.nio.file.Path;
import java.util.Date;

/**
 * Records a file-related event
 * @author andrew
 */
public class FileEvent extends EventObject{
  
    /**
     * Time the file event occurred.
     */
    private long modified;
    
    /**
     * The prior acting file in the event sequence (e.g. the location moved from, the name changed from)
     */
    private Path prior;
    
    /**
     * Constructor
     * @param source
     * @param modified
     * @param prior 
     */
    public FileEvent(Path source, Path prior){
        super(source);
        this.modified = System.nanoTime();
        this.prior = prior;
    }
    
    /**
     * Constructor
     * @param source
     * @param modified 
     */
    public FileEvent(Path source){
        super(source);
        this.modified = System.nanoTime();
    }
    
    public Path getPath(){
        return (Path) this.getSource();
    }
    
    public long getTime(){
        return modified;
    }
}