package dropbag;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author andrew
 */
public class ApplicationTest {

    //static Application instance;

    public ApplicationTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        //ApplicationTest.instance = new Application();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of addFileListener method, of class Application.
     */
    @Test
    public void testAddFileListener() {
        FileListenerStub listener = new FileListenerStub();
        Application instance = new Application();
        instance.addFileListener(listener);
    }

    /**
     * Test of removeFileListener method, of class Application.
     */
    @Test
    public void testRemoveFileListener() {
        FileListenerStub listener = new FileListenerStub();
        Application instance = new Application();
        instance.addFileListener(listener);
        instance.removeFileListener(listener);
    }

    /**
     * Test of fireFileEvent method, of class Application.
     */
    @Test
    public void testFireFileEvent() {
        Path path = Paths.get(System.getProperty("user.dir") + "\\test_data");
        FileListenerStub listener = new FileListenerStub();
        Application instance = new Application();
        FileEvent event = new FileEvent( path );
        // add listener
        instance.addFileListener(listener);
        // fire event
        instance.fireFileEvent(Application.FILE_DELETED, event);
        // test
        assertEquals( path, (Path) listener.event.getSource() );
        assertEquals( "file deleted", listener.type );
    }
    
    /**
     * Test of registerAll method, of class Application.
     */
    @Test
    public void testRegisterAll() {
        Path path = Paths.get(System.getProperty("user.dir") + "\\test_data");
        Application instance = new Application();
        instance.registerAll( path );
    }

    /**
     * Test of register method, of class Application.
     */
    @Test
    public void testRegister() throws Exception {
        Path dir = Paths.get(System.getProperty("user.dir") + "\\test_data");
        Application instance = new Application();
        instance.register(dir);
        assertEquals(1, instance.watch_keys.size());
    }
    
    /**
     * Test of stop method, of class Application.
     */
//    @Test
//    public void testStop() throws Exception {
//        Path dir = Paths.get(System.getProperty("user.dir") + "\\test_data");
//        Application instance = new Application();
//        instance.watch(dir);
//        System.out.println(instance.watch_keys);
//        assertEquals(21, instance.watch_keys.size());
//        // stop
//        instance.stop();
//        assertEquals(0, instance.watch_keys.size());
//    }
    
    /**
     * Test of watch method, of class Application.
     */
    @Test
    public void testWatchCreate() throws Exception{
        Path path = Paths.get(System.getProperty("user.dir") + "\\test_data");
        Application instance = new Application();
        FileListenerStub listener = new FileListenerStub();
        // add listener
        instance.addFileListener(listener);
        // watch
        instance.watch(path);
        // create file
        Path path1 = path.resolve("created.txt");
        Files.write(path1, "...".getBytes(), StandardOpenOption.CREATE);
        // wait
        long before = System.nanoTime();
        while(listener.type == null){
            // wait for event to fire
        }
        long after = System.nanoTime();
        double wait = after - before;
        System.out.println("Waited "+wait/1000000+" milliseconds for event to fire.");
        // test
        assertEquals( path1, (Path) listener.event.getSource() );
        assertEquals( "file created", listener.type );
    }
    
    /**
     * Test of watch method, of class Application.
     */
    @Test
    public void testWatchModify() throws Exception{
        Path path = Paths.get(System.getProperty("user.dir") + "\\test_data");
        Application instance = new Application();
        FileListenerStub listener = new FileListenerStub();
        // add listener
        instance.addFileListener(listener);
        // watch
        instance.watch(path);
        // create file
        Path path1 = path.resolve("created.txt");
        Files.write(path1, "...".getBytes(), StandardOpenOption.APPEND);
        // wait
        long before = System.nanoTime();
        while(listener.type == null){
            // wait for event to fire
        }
        long after = System.nanoTime();
        double wait = after - before;
        System.out.println("Waited "+wait/1000000+" milliseconds for event to fire.");
        // test
        assertEquals( path1, (Path) listener.event.getSource() );
        assertEquals( "file modified", listener.type );
    }
    
    /**
     * Test of watch method, of class Application.
     */
    @Test
    public void testWatchDelete() throws Exception{
        Path path = Paths.get(System.getProperty("user.dir") + "\\test_data");
        Application instance = new Application();
        FileListenerStub listener = new FileListenerStub();
        // add listener
        instance.addFileListener(listener);
        // watch
        instance.watch(path);
        // create file
        Path path1 = path.resolve("created.txt");
        Files.delete(path1);
        // wait
        long before = System.nanoTime();
        while(listener.type == null){
            // wait for event to fire
        }
        long after = System.nanoTime();
        double wait = after - before;
        System.out.println("Waited "+wait/1000000+" milliseconds for event to fire.");
        // test
        assertEquals( path1, (Path) listener.event.getSource() );
        assertEquals( "file deleted", listener.type );
    }
    
    /**
     * Test of watch method, of class Application.
     */
    @Test
    public void testWatchDeleteDirectory() throws Exception{
        Path path = Paths.get(System.getProperty("user.dir") + "\\test_data");
        Application instance = new Application();
        FileListenerStub listener = new FileListenerStub();
        // add listener
        instance.addFileListener(listener);
        // watch
        instance.watch(path);
        // create file
        Path path1 = path.resolve("new_folder");
        Files.createDirectory(path1, new FileAttribute<?>[0]);
        Files.delete(path1);
        // wait
        long before = System.nanoTime();
        while(listener.type == null){
            // wait for event to fire
        }
        long after = System.nanoTime();
        double wait = after - before;
        System.out.println("Waited "+wait/1000000+" milliseconds for event to fire.");
        // test
        assertEquals( path1, (Path) listener.event.getSource() );
        assertEquals( "directory created", listener.type );
    }
    
}

/**
 * Stub for Observer; prints updates to System.out
 * @author andrew
 */
class ObserverStub implements Observer {

    public void update(Observable o, Object arg) {
        System.out.println("[" + Object.class + "] " + arg);
    }
    
}

/**
 * Stub for FileListener; prints events to System.out
 * @author andrew
 */
class FileListenerStub implements FileListener {

    public FileEvent event;
    public String type;
    
    public void directoryCreated(FileEvent e) {
        System.out.println("[FileListenerStub] Directory created: " + e.getSource());
        this.event = e;
        this.type = "directory created";
    }

    public void directoryModified(FileEvent e) {
        System.out.println("[FileListenerStub] Directory modified: " + e.getSource());
        this.event = e;
        this.type = "directory modified";
    }

    public void directoryDeleted(FileEvent e) {
        System.out.println("[FileListenerStub] Directory deleted: " + e.getSource());
        this.event = e;
        this.type = "directory deleted";
    }

    public void directoryRenamed(FileEvent e) {
        System.out.println("[FileListenerStub] Directory renamed: " + e.getSource());
        this.event = e;
        this.type = "directory renamed";
    }

    public void directoryMoved(FileEvent e) {
        System.out.println("[FileListenerStub] Directory moved: " + e.getSource());
        this.event = e;
        this.type = "directory moved";
    }
    
    public void fileCreated(FileEvent e) {
        System.out.println("[FileListenerStub] File created: " + e.getSource());
        this.event = e;
        this.type = "file created";
    }

    public void fileModified(FileEvent e) {
        System.out.println("[FileListenerStub] File modified: " + e.getSource());
        this.event = e;
        this.type = "file modified";
    }

    public void fileDeleted(FileEvent e) {
        System.out.println("[FileListenerStub] File deleted: " + e.getSource());
        this.event = e;
        this.type = "file deleted";
    }

    public void fileRenamed(FileEvent e) {
        System.out.println("[FileListenerStub] File renamed: " + e.getSource());
        this.event = e;
        this.type = "file renamed";
    }

    public void fileMoved(FileEvent e) {
        System.out.println("[FileListenerStub] File moved: " + e.getSource());
        this.event = e;
        this.type = "file moved";
    }
}
