package igami2.DataBase.hibernateconfig;
// Generated Jul 17, 2013 10:33:09 PM by Hibernate Tools 3.2.1.GA

import javax.persistence.Entity;
import javax.persistence.Id;




/**
 * Igami2EventMaster generated by hbm2java
 */
@Entity
public class Igami2EventMaster  implements java.io.Serializable {


    @Id
     private int userid;
     private boolean event;

    public Igami2EventMaster() {
    }

    public Igami2EventMaster(int userid, boolean event) {
       this.userid = userid;
       this.event = event;
    }
   
    public int getUserid() {
        return this.userid;
    }
    
    public void setUserid(int userid) {
        this.userid = userid;
    }
    public boolean isEvent() {
        return this.event;
    }
    
    public void setEvent(boolean event) {
        this.event = event;
    }




}


