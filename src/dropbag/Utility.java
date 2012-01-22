package dropbag;

import rsync.Rsync;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.IOException;
import java.io.File;
import java.util.Date;
import com.google.gson.*;

/**
 * Utility bin for some commonly used methods
 * @author andrew
 */
public class Utility{
    
    /**
     * Displays logged errors
     * @param e Exception
     */
    public static void log(Exception e) {
        e.printStackTrace();
    }

    /**
     * Displays logged strings as messages
     * @param s 
     */
    public static void log(String s) {
        System.out.println(s);
    }

    /**
     * Displays an object structure in JSON
     * @param o 
     */
    public static void log(Object o) {
        Gson g = new GsonBuilder().setPrettyPrinting().create();
        String json = g.toJson(o);
        System.out.println(json);
    }
    
    
    static private Path _dir;
    static private ServerThread _server;
    /**
     * Walks through directory, adding files to the server queue for a sync check
     * @param dir Directory to walk through
     * @param server Server instance doing the sync check
     */
    public static void sync(Path dir, ServerThread server){
        Utility._dir = dir;
        Utility._server = server;
        try{
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>(){
                public FileVisitResult visitFile(Path d, BasicFileAttributes attrs) {
                    try{
                        String name = Utility._dir.relativize(d).toString();
                        if( Utility.isHidden(name) ) return FileVisitResult.CONTINUE;
                        String checksum = Rsync.getMD5(d.toString());
                        Date date = new Date( new File(d.toString()).lastModified() );
                        Change c = new Change(name, checksum, date, Change.SYNC, "");
                        Utility._server.add(c);
                    }
                    catch(Exception e){
                        Utility.log(e);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch(IOException e){
            Utility.log(e);
        }
    }
    
    /**
     * Tests whether a file or directory should be hidden from the application
     * @param filename the filename to test
     * @return true if file should be hidden, false otherwise
     */
    public static boolean isHidden(String filename){
        for(String rule:Configuration.HIDE_RULES){
            // regex
            if( rule.startsWith("/") ){
                if( filename.matches(rule) ) return true;
            }
            // regular
            else{
                if( rule.equals(filename) ) return true;
            }
        }
        return false;
    }
}
