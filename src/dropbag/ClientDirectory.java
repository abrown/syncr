package dropbag;
import java.io.File;

public class ClientDirectory {
    
    /**
     * Absolute path to directory
     */
    private File path;

    /**
     * To string
     * @return String
     */
    public String toString(){
        return "<"+this.path.toString()+">";
    }
    
    /**
     * Constructor
     * @param path String
     */
    public ClientDirectory(String path){
        try{
            this.path = new File(path);
            if( !this.path.exists() ) throw new Exception("Path does not exist");
        }
        catch(Exception e){
            System.err.println("Could not locate: "+path);
            System.exit(1);
        }
    }
    
    /**
     * Constructor
     * @param path 
     */
    public ClientDirectory(File path) {
        this.path = path;
    }

    /**
     * 
     * @return 
     */
    public File getPath() {
        return path;
    }

    /**
     * 
     * @param path 
     */
    public void setPath(File path) {
        this.path = path;
    }
}
