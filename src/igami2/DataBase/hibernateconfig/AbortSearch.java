package igami2.DataBase.hibernateconfig;
// Generated Jul 17, 2013 10:33:09 PM by Hibernate Tools 3.2.1.GA

import javax.persistence.Entity;
import javax.persistence.Id;




/**
 * AbortSearch generated by hbm2java
 */
@Entity
public class AbortSearch  implements java.io.Serializable {


    @Id
     private int userid;
     private byte abort;
     private byte discardData;

    public AbortSearch() {
    }

    public AbortSearch(int userid, byte abort, byte discardData) {
       this.userid = userid;
       this.abort = abort;
       this.discardData = discardData;
    }
   
    public int getUserid() {
        return this.userid;
    }
    
    public void setUserid(int userid) {
        this.userid = userid;
    }
    public byte getAbort() {
        return this.abort;
    }
    
    public void setAbort(byte abort) {
        this.abort = abort;
    }
    public byte getDiscardData() {
        return this.discardData;
    }
    
    public void setDiscardData(byte discardData) {
        this.discardData = discardData;
    }
}


