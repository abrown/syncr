package dropbag;

import java.util.Observable;
import java.util.ArrayList;
import java.util.HashMap;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import static java.nio.file.StandardWatchEventKinds.*;
import java.io.IOException;

/**
 * ...
 * @author andrew
 */
public class Application extends Observable {

    /**
     * Directory to watch
     */
    protected Path source_directory;
    /**
     * List of FileListeners
     */
    protected ArrayList<FileListener> listeners;
    /**
     * List of WatchKeys
     */
    protected HashMap<WatchKey, String> watch_keys;
    /**
     * WatchService
     */
    protected WatchService watch_service;
    /**
     * Event Types
     */
    protected static final int DIR_CREATED = 1;
    protected static final int DIR_MODIFIED = 2;
    protected static final int DIR_DELETED = 3;
    protected static final int DIR_MOVED = 4;
    protected static final int DIR_RENAMED = 5;
    protected static final int FILE_CREATED = 6;
    protected static final int FILE_MODIFIED = 7;
    protected static final int FILE_DELETED = 8;
    protected static final int FILE_MOVED = 9;
    protected static final int FILE_RENAMED = 10;

    public Application() {
        // create watch service
        try {
            this.watch_service = FileSystems.getDefault().newWatchService();
        } catch (Exception e) {
            this.notifyObservers(e);
            return;
        }
        // create watch keys
        this.watch_keys = new HashMap<WatchKey, String>();
    }

    /**
     * Adds a FileListener
     * @param listener The FileListener to add
     */
    public void addFileListener(FileListener listener) {
        if (this.listeners == null) {
            this.listeners = new ArrayList<FileListener>();
        }
        this.listeners.add(listener);
    }

    /**
     * Removes a FileListener
     * @param listener The FileListener to remove
     */
    public void removeFileListener(FileListener listener) {
        listeners.remove(listener);
    }

    /**
     * Sends the FileEvent to each registered FileListener
     * @param type
     * @param event 
     */
    protected void fireFileEvent(int type, FileEvent event) {
        try {
            for (FileListener listener : this.listeners) {
                switch (type) {
                    case Application.DIR_CREATED:
                        listener.directoryCreated(event);
                        break;
                    case Application.DIR_MODIFIED:
                        listener.directoryModified(event);
                        break;
                    case Application.DIR_DELETED:
                        listener.directoryDeleted(event);
                        break;
                    case Application.DIR_MOVED:
                        listener.directoryMoved(event);
                        break;
                    case Application.DIR_RENAMED:
                        listener.directoryRenamed(event);
                        break;
                    case Application.FILE_CREATED:
                        listener.fileCreated(event);
                        break;
                    case Application.FILE_MODIFIED:
                        listener.fileModified(event);
                        break;
                    case Application.FILE_DELETED:
                        listener.fileDeleted(event);
                        break;
                    case Application.FILE_MOVED:
                        listener.fileMoved(event);
                        break;
                    case Application.FILE_RENAMED:
                        listener.fileRenamed(event);
                        break;
                    default:
                        throw new Exception("Event fired must be one of: CREATED, MODIFIED, DELETED, MOVED, or RENAMED");
                }
            }
        } catch (Exception e) {
            this.notifyObservers(e);
        }
    }

    /**
     * Starts watching a directory
     * @param dir 
     */
    public void watch(Path dir) {
        if (this.source_directory != null) {
            Exception e = new Exception("Application is already watching: " + this.source_directory);
            this.notifyObservers(e);
            return;
        }
        // register all directories
        this.source_directory = dir;
        this.registerAll(dir);
        // start watcher
        FileWatcher watcher = new FileWatcher(this);
        watcher.start();
    }
    
    /**
     * Stops watching the directory
     */
    public void stop(){
        try{
            this.watch_keys.clear();
            this.watch_service.close();
        }
        catch(Exception e){
            this.notifyObservers(e);
        }
    }

    /**
     * Recursively adds each subdirectory to the listener
     * @param dir
     */
    protected void registerAll(Path dir) {
        // push directories recursively to stack
        this.notifyObservers("Calculating directory size.");
        final ArrayList<Path> stack = new ArrayList<Path>();
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

                public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes attrs) {
                    stack.add(d);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            this.notifyObservers(e);
        }
        // register directories
        this.notifyObservers(stack.size() + " directories to register.");
        int count = 0;
        for (Path d : stack) {
            // report 
            count++;
            if (count % 100 == 0) {
                this.notifyObservers(count / stack.size());
            }
            // register
            this.register(d);
        }
    }

    /**
     * Registers a directory to listen to
     * @param dir
     * @throws IOException
     */
    protected void register(Path dir) {
        // if( Utility.isHidden(dir.toString()) ) return;
        try {
            WatchKey key = dir.register(this.watch_service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            this.watch_keys.put(key, dir.toString());
        } catch (IOException e) {
            this.notifyObservers(e);
        }
    }
}
