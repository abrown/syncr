/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dropbag;

/**
 *
 * @author andrew
 */
public class Configuration {
    
    public final static long CLIENT_POLL_INTERVAL = 20000; // in milliseconds
    
    public final static long SERVER_PUT_INTERVAL = 20000; // in milliseconds
    
    public static final int RSYNC_BLOCK_SIZE = 1024; // in bytes
    
    public static final String TEMP_FILE_EXTENSION = ".dropbag"; 
    
    public static String SERVER_URL = "http://www.casabrown.com/dropbag/"; // end with /
    
    public static String SERVER_USERNAME = ""; //
    
    public static String SERVER_PASSWORD = ""; //
    
    public static String ENCRYPTION_PASSWORD = ""; //
    
    public static final String[] HIDE_RULES = {
        "desktop.ini",
        ".git"
    };
    
}
