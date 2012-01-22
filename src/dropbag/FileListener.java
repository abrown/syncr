package dropbag;

/**
 * An event listener for file-related events.
 * @author andrew
 */
public interface FileListener extends java.util.EventListener{
    public void directoryCreated( FileEvent e );
    public void directoryModified( FileEvent e );
    public void directoryDeleted( FileEvent e );
    public void directoryRenamed( FileEvent e );
    public void directoryMoved( FileEvent e );
    public void fileCreated( FileEvent e );
    public void fileModified( FileEvent e );
    public void fileDeleted( FileEvent e );
    public void fileRenamed( FileEvent e );
    public void fileMoved( FileEvent e );
}
