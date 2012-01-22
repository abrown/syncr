package rsync;

public class RsyncBlock {
    int index;
    boolean modified;
    byte[] data;

    public RsyncBlock(int index, boolean unchanged, byte[] data) throws Exception{
        if( index == -1 ) throw new Exception("RsyncBlock created with bad index data: -1");
        this.index = index;
        this.modified = unchanged;
        this.data = data;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean unchanged) throws Exception{
        throw new Exception("Cannot reset 'modified' field of RsyncBlock.");
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) throws Exception {
        throw new Exception("Cannot reset index of RsyncBlock.");
    }
    
}
