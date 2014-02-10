/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 */
package igami2.DataBase;

/**
 *
 * @author MBS-Admin
 */
public class DataDumpMain {
    
    DBManager dbm;
    
    void dumpData(int UserId, int searchid)
    {
           dbm = new DBManager(UserId,null);
           dbm.searchId = searchid;
           dbm.dumpData("Real");
    }
    public static void main(String args[])
    {
        int userId=2;
        int searchid =4;
        DataDumpMain ddump = new DataDumpMain();
        ddump.dumpData(userId, searchid);
    }
}
