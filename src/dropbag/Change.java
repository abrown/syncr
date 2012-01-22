/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dropbag;
import java.util.Date;
import java.nio.file.Path;

/**
 *
 * @author andrew
 */
public class Change{
    
    /**
     * Server Change ID
     */
    public int id;
    
    /**
     * Server File ID
     */
    public int file_id;
    
    /**
     * Client relative path, e.g. test/test.txt
     */
    public String name;
    
    /**
     * Absolute path to file
     */
    public Path path;
    
    /**
     * MD5 checksum
     */
    public String checksum;
 
    /**
     * Date modified (includes deletion)
     */
    public Date modified;
    
    /**
     * Change type
     */
    public int type;
    public static final int MODIFIED = 0;
    public static final int CREATED = 1;
    public static final int DELETED = 2;
    public static final int RENAMED = 3;
    public static final int SYNC = 4;
    
    /**
     * Direction of update
     */
    public int direction;
    public static final int TO_SERVER = 0;
    public static final int FROM_SERVER = 1;
    
    /**
     * Storage; on RENAMED, new relative path
     */
    public String data;

    /**
     * Constructor
     * @param id
     * @param file_name
     * @param checksum
     * @param modified
     * @param deleted 
     */
    public Change(String name, String checksum, Date modified, int type, String data) {
        this.name = name;
        this.checksum = checksum;
        this.modified = modified;
        this.type = type;
        this.data = data;
    }
    
    /**
     * Constructor
     */
    public Change(){
        
    }

    /** ID **/
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    
    /** SERVER FILE ID **/
    public int getFileId() {
        return file_id;
    }
    public void setFileId(int file_id) {
        this.file_id = file_id;
    }
    
    /** NAME **/
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    /** NAME **/
    public Path getPath() {
        return path;
    }
    public void setPath(Path p) {
        this.path = p;
    }
    
    /** CHECKSUM **/
    public String getChecksum() {
        return checksum;
    }
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
       
    /** MODIFIED DATE **/
    public Date getModifiedDate() {
        if( modified == null ) return new Date();
        return modified;
    }
    public void setModifiedDate(Date modified) {
        this.modified = modified;
    }
    
    /** TYPES **/
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    
    public boolean isModified(){
        return (this.type == MODIFIED);
    }
    public void setModified() {
        this.type = MODIFIED;
    }
    
    /** CREATED **/
    public boolean isCreated(){
        return (this.type == CREATED);
    }
    public void setCreated() {
        this.type = CREATED;
    }
     
    /** DELETED **/
    public boolean isDeleted() {
        return (this.type == DELETED);
    }   
    public void setDeleted() {
        this.type = DELETED;
    }
    
    /** RENAMED **/
    public boolean isRenamed() {
        return (this.type == RENAMED);
    }
    /**
     * Gets the new name of the file
     * @see getName() will return old name
     * @return new name
     */
    public String getRenamed(){
        return this.data;
    }
    public void setRenamed(String renamed) {
        this.type = RENAMED;
        this.data = renamed;
    }
    
    /** SYNC **/
    public boolean isSync() {
        return (this.type == SYNC);
    }   
    public void setSync() {
        this.type = SYNC;
    }
    
    
    /** DIRECTION **/
    public boolean toServer(){
        return ( this.direction == TO_SERVER ) ? true : false;
    }
    public boolean fromServer(){
        return ( this.direction == FROM_SERVER ) ? true : false;
    }
    public void setDirection(int direction){
        this.direction = direction;
    }
      
    /** DATA **/
    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }

    /**
     * To string
     * @return String
     */
    public String toString(){
        String out = "";
        // kind
        if( this.isDeleted() ) out += "deleted";
        else if( this.isRenamed() ) out += "renamed";
        else if( this.isCreated() ) out += "created";
        else out += "modified";
        // file_name
        out += " <" + this.getName() + "> ";
        // date
        out += this.getModifiedDate().getTime();
        // if renamed
        if( this.isRenamed() ) out += " ["+this.getRenamed()+"]";
        // return
        return out;
    }
  
    /**
     * Answers the question: are changes referring to the same file?
     * @param c
     * @return 
     */
    public boolean equals(Change c){
        if( this.isRenamed() && this.getRenamed().equals(c.getName()) ) return true;
        else if( c.isRenamed() && c.getRenamed().equals(this.getName())) return true;
        else if( this.getName().equals(c.getName()) ) return true;
        else return false;
    }
    
    /**
     * Answers the question: was this change made after the argument change?
     * @param c
     * @return 
     */
    public boolean after(Change c){
        return this.getModifiedDate().after(c.getModifiedDate());
    }
    
    /**
     * Answers the question: was this change made before the argument change?
     * @param c
     * @return 
     */
    public boolean before(Change c){
        return this.getModifiedDate().before(c.getModifiedDate());
    }
}
