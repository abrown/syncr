package dropbag;

/**
 *
 * @author andrew
 */
public class Main {

    public static ServerThread server;
    public static ClientDirectory dir;
    public static ListenerThread listen;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // parse arguments
        String client_dir = "";
        String server_url = "";
        for(int i = 0; i<args.length; i++){
            if( args[i].equals("-c") ) client_dir = args[i+1];
            else if( args[i].equals("-s") ) server_url = args[i+1];
        }
        if( client_dir.length() < 1 || server_url.length() < 1 ){
            System.out.println("Format: dropbag -c [client directory] -s ]server directory");
            System.exit(0);
        }
        
        // start thread
        ClientThread client =new ClientThread(client_dir, server_url);
        client.start();
    }
}