/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dropbag;

import java.nio.file.*;
import java.util.ArrayList;
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
public class ListenerThreadTest {
    
    ListenerThread instance;
    
    public ListenerThreadTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ClientDirectory dir = new ClientDirectory( System.getProperty("user.dir")+"\\test" );
        ListenerThread instance = new ListenerThread( dir );
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
     * Test of getChanges method, of class ListenerThread.
     */
    @Test
    public void testGetChanges() throws Exception{
        // test create
        String path1 = System.getProperty("user.dir")+"\\test\\created.txt";
        Path file1 = Paths.get( path1 );
        System.out.println( file1 );
        Files.write( file1, "...".getBytes(), StandardOpenOption.CREATE );
        ArrayList<Change> changes = instance.getChanges();
        assertEquals(path1, changes.get(0).getPath());
    }

    /**
     * Test of add method, of class ListenerThread.
     */
    @Test
    public void testAdd() {
        System.out.println("add");
        Change c = null;
        ListenerThread instance = null;
        instance.add(c);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class ListenerThread.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        ListenerThread instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of run method, of class ListenerThread.
     */
    @Test
    public void testRun() {
        System.out.println("run");
        ListenerThread instance = null;
        instance.run();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
