package rsync;

import dropbag.Configuration;
import java.security.MessageDigest;
import java.io.*;
import java.util.ArrayList;

/**
 * Collection of static methods for Rsync-related tasks
 * @author andrew
 */
public class Rsync {

    /**
     * Gets Adler-32 list for a file
     * @param filename File to process
     * @return list of Adler-32 integers, one per block
     * @see Configuration for block size
     * @throws Exception 
     */
    public static int[] getAdlers(String filename) throws Exception{
        File f = new File(filename);
        if( !f.exists() ) throw new Exception("File "+filename+" does not exist; cannot calculate Adler-32 checksums.");
        // setup
        int size = (int) java.lang.Math.ceil( f.length() / Configuration.RSYNC_BLOCK_SIZE );
        int[] adlers = new int[size];
        // input file
        InputStream in = new FileInputStream(filename);
        byte[] buffer = new byte[Configuration.RSYNC_BLOCK_SIZE];
        int b; // bytes read
        int k = 0; // start marker
        int l = 0; // end marker
        int i = 0; // array marker
        while ((b = in.read(buffer)) != -1) {
            l += b;
            adlers[i] = Rsync.getAdler(buffer, k, l);
            k = l+1;
            i++;
        }
        in.close();
        // return
        return adlers;
    }
    
    /**
     * Gets Adler-32 checksum for a chunk of data
     * @param data Chunk to process
     * @param k Starting offset
     * @param l Ending offset
     * @return Adler-32
     */
    public static int getAdler(byte[] data, int k, int l){
        int a = 0;
        int b = 0;
        int i = 0;
        int size = data.length;
        // calculate a and b
        for (; i < (size-4); i+=4) {
            b += 4*(a + data[i]) + 3*data[i+1] + 2*data[i+2] + data[i+3];
            a += data[i] + data[i+1] + data[i+2] + data[i+3];
        }
        // add on extras
        for (; i < size; i++) {
            a += data[i];
            b += a;
        }
        // get modulo
        a = a & 0xFFFF;
        b = b & 0xFFFF;
        // return
        return a + (b << 16);
    }
    
    /**
     * Get MD5 in byte array
     * @param filename
     * @return
     * @throws Exception 
     */
    public static byte[] getMD5ByteArray(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);
        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        fis.close();
        return complete.digest();
    }

    /**
     * Get MD5 Digest of file
     * @param filename
     * @return String
     * @throws Exception 
     */
    public static String getMD5(String filename) throws Exception {
        byte[] b = getMD5ByteArray(filename);
        // convert to String
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
}
