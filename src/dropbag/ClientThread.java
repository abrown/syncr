package dropbag;
import java.util.concurrent.atomic.AtomicReference;
import java.util.ArrayList;
import java.util.Date;


public class ClientThread extends Thread{

    private ClientDirectory dir;
    private ServerThread server;
    private ListenerThread listener;
    private ArrayList<Change> changes;
    private Date updated;            
    
    /**
     * Constructor
     * @param client_directory
     * @param server_url 
     */
    public ClientThread(String client_directory, String server_url){
        this.dir = new ClientDirectory(client_directory);
        this.server = new ServerThread( this );
        this.listener = new ListenerThread( this.dir );
        this.updated = new Date(0);
        
        // Configuration
        Configuration.SERVER_URL = server_url;
        Configuration.SERVER_USERNAME = "";
        Configuration.SERVER_PASSWORD = "";
    }
    
    /**
     * 
     */
    public void run(){
        // start polling
        this.server.start();
        // sync files initially
        Utility.sync(this.dir.getPath().toPath(), this.server);
        // then start listening
        this.listener.start();
        // setup variables
        ArrayList<Change> server_changes = new ArrayList<Change>();
        ArrayList<Change> client_changes = new ArrayList<Change>();
        ArrayList<Change> final_changes = new ArrayList<Change>();
        // start
        for(;;){
            // sleep
            try{ Thread.currentThread().sleep( Configuration.CLIENT_POLL_INTERVAL ); }
            catch(InterruptedException e){ Utility.log(e); }
            // get server changes
            try{
                //server_changes = server.getChanges( this.updated );
                this.updated = new Date(); 
            }
            catch(Exception e){ Utility.log(e); }
            // get client changes
            try{
                client_changes = listener.getChanges();
            }
            catch(Exception e){ Utility.log(e); }         
            // reconcile client and server lists
            final_changes = this.reconcile(client_changes, server_changes);
            // make changes
            for(Change c : final_changes){
                try{
                    this.server.add(c);
                }
                catch(Exception e){ Utility.log(e); }
            }
        }
    }
    
    /**
     * Fix discrepancies between server list and client list
     * @param client_changes
     * @param server_changes
     * @return ArrayList<Change>
     */
    public ArrayList<Change> reconcile(ArrayList<Change> client_changes, ArrayList<Change> server_changes){
        // check
        for(Change b: client_changes){
            for(Change c: server_changes){
                // situation: during the poll interval, both client and server have changed
                if( b.equals(c) ){
                    Utility.log("The same file has been modified by different sources...");
                }
            }
        }
        // add to change list
        ArrayList<Change> out = new ArrayList<Change>();
        for(Change b: client_changes){
            b.setDirection(Change.TO_SERVER);
            out.add(b);
        }
        for(Change c: server_changes){
            c.setDirection(Change.FROM_SERVER);
            out.add(c);
        }
        return out;
    }
    
    /**
     * To String
     * @return 
     */
    public String toString(){
        return "ClientThread: " + this.hashCode();
    }
}
