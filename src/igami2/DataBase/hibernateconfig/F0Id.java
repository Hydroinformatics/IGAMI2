package igami2.DataBase.hibernateconfig;
// Generated Jul 17, 2013 10:33:09 PM by Hibernate Tools 3.2.1.GA

import javax.persistence.Embeddable;




/**
 * F0Id generated by hbm2java
 */
@Embeddable
public class F0Id  implements java.io.Serializable {


     private int userid;
     private int searchid;
     private long indvid;

    public F0Id() {
    }

    public F0Id(int userid, int searchid, long indvid) {
       this.userid = userid;
       this.searchid = searchid;
       this.indvid = indvid;
    }
   
    public int getUserid() {
        return this.userid;
    }
    
    public void setUserid(int userid) {
        this.userid = userid;
    }
    public int getSearchid() {
        return this.searchid;
    }
    
    public void setSearchid(int searchid) {
        this.searchid = searchid;
    }
    public long getIndvid() {
        return this.indvid;
    }
    
    public void setIndvid(long indvid) {
        this.indvid = indvid;
    }


   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof F0Id) ) return false;
		 F0Id castOther = ( F0Id ) other; 
         
		 return (this.getUserid()==castOther.getUserid())
 && (this.getSearchid()==castOther.getSearchid())
 && (this.getIndvid()==castOther.getIndvid());
   }
   
   public int hashCode() {
         int result = 17;
         
         result = 37 * result + this.getUserid();
         result = 37 * result + this.getSearchid();
         result = 37 * result + (int) this.getIndvid();
         return result;
   }   


}


