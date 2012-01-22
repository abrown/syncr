package rsync;

import rsync.Rsync;

public class RsyncSignature {

    public String name;
    public String checksum;
    public int[] adlers;

    public RsyncSignature(String name, String filename) throws Exception {
        this.name = name;
        this.checksum = Rsync.getMD5(filename);
        this.adlers = Rsync.getAdlers(filename);
    }
}
