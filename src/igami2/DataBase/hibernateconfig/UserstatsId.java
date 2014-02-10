package igami2.DataBase.hibernateconfig;
// Generated Jul 17, 2013 10:33:09 PM by Hibernate Tools 3.2.1.GA



/**
 * UserstatsId generated by hbm2java
 */
public class UserstatsId  implements java.io.Serializable {


     private int userid;
     private int searchid;

    public UserstatsId() {
    }

    public UserstatsId(int userid, int searchid) {
       this.userid = userid;
       this.searchid = searchid;
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


   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof UserstatsId) ) return false;
		 UserstatsId castOther = ( UserstatsId ) other; 
         
		 return (this.getUserid()==castOther.getUserid())
 && (this.getSearchid()==castOther.getSearchid());
   }
   
   public int hashCode() {
         int result = 17;
         
         result = 37 * result + this.getUserid();
         result = 37 * result + this.getSearchid();
         return result;
   }   


}

