/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2;

import igami2.EMailMgmt.EmailManager;
import igami2.DataBase.DBManager;
import igami2.DataBase.hibernateconfig.ComputerclusterInfo;
import igami2.DataBase.hibernateconfig.NewuserParamters;
import igami2.DistributedSystem.MasterComputer.HPCController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author VIDYA
 */
public class IGAMI2Main {

    static User[] ur;
    public static ArrayList users; //used by RMI Temp interface for running simulated usrs
    public static int maxUsers = 150;//max number of allowed users at a time
    public static int countUsers = 0;//count the number of active users at a time
    static String location_newuser = "../SWAT/USER/commondata/";
    public static EmailManager em;
    public static boolean localPrefs = true;
    public static String admin_Email = "vbsingh@umail.iu.edu";
    public static boolean localAssignBMP = true; //use BMP assignments as well
    public static HashMap UsersList; //keep track of all the currently existing users in the system
    public static HashMap UserSystemId; //Assign Every user a local system Id, internal id of the user
    private static DBManager dbm;
    private static int sleeptime = 1000 * 5;//5 Sec
    private static int countDaemonTime = 0;
    private static int maxDaemonTime = sleeptime * 20;//*60; ..1 min
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        System.out.println("IGMAI2 Working");
        users = new ArrayList();
        UsersList = new HashMap();
        UserSystemId = new HashMap();
        dbm = new DBManager();
        em = new EmailManager();
        try {
            HPCController con = new HPCController("");
            con.initRMIMasterComputer(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        dbm.CleanUpSystemWide(); //use to cean any local data, but recommended to clean manually

        //runs infinite loop and wait for user's to login and begin their search
        while (true) {

            //from New RMI interface, the user runs as a simulated program from another computer instead of real user
            //if(!fromDB)
            try {
                boolean fromDB = false;
                UserData usr = (UserData) users.remove(0);//Add a user and run the test for that user                       
                User u1 = null;
                u1 = (User) UsersList.get(usr.userId); //check if the user is already in the system
                if (u1 == null) //check if the user alread exist in the system
                {
                    int uSystemId = getaNewSystemId(usr.userId); //local id assigned to this user
                    User u = new User(usr.userId, usr.chosenFF, usr.host, fromDB, uSystemId);//create a new user running thread and start its tests
                    UsersList.put(usr.userId, u); //save the link of the user Thread
                    countUsers++;
                    //System.out.println("A new User is Added into the system with id" + usr.userId);
                } else {
                    System.out.println("User is already running in the system " + usr.userId);
                }
                continue;//check for next
            } catch (Exception e)//when no user online            
            {
            }

            //create a user thread from Data Base
            LinkedList<UserData> user = new LinkedList();
            user = dbm.getNewUser();
            if (user.size() != 0) {
                try {
                    boolean fromDB = true;
                    while (user.size() > 0) {
                        UserData usr = (UserData) user.removeFirst();//Add a user and run the test for that user  
                        User u1 = null;
                        u1 = (User) UsersList.get(usr.userId);
                        if (u1 == null) //check if the user alread exist in the system
                        {
                            int uSystemId = getaNewSystemId(usr.userId);
                            User usrTh = new User(usr.userId, usr.chosenFF, usr.chosenBMP, fromDB, uSystemId);//create a new user and start its tests
                            UsersList.put(usr.userId, usrTh);
                            //users.add(usr.userId);
                            countUsers++;
                            //System.out.println("A new User is Added into the system with id" + usr.userId);
                        } else {
                            System.out.println("User is already running in the system " + usr.userId);
                        }
                        continue;//check for next
                    }
                } catch (Exception e)//when no user online            
                {
                }
            }

            //Use to remove a user at runtime if the user's thread crash or some problem happens, prevents restarting the system
            ArrayList UserIds = dbm.checkAbortThread(); //safe abort is still needed
            if (UserIds.size() > 0) {
                for (int i = 0; i < UserIds.size(); i++) {
                    int id = Integer.parseInt(UserIds.get(i) + "");
                    removeUserFromSystem(id, false);
                }
            }
            try {
                Thread.sleep(sleeptime);
            } catch (InterruptedException ex) {
            }

            if (countDaemonTime >= maxDaemonTime) {
                //run System change Daemon every 1 minute
                countDaemonTime = 0;//reinit
                checkSystemConfigChange();
            } else {
                countDaemonTime = countDaemonTime + sleeptime;
            }
        }
    }

    private static int getaNewSystemId(int userId) {
        int res = -1;
        int len = UserSystemId.size();
        int i = 0;
        //check if the user is already allocated an id or allocate previosly removed user's id
        for (; i < len; i++) {
            //Object ob = UserSystemId.get(i); //check if i is mapped to some user             
            if (!UserSystemId.containsValue(i)) // i th id is free
            {
                res = i; //
                UserSystemId.put(userId, res);
                len++;
                break;
            }
        }
        if (res == -1) //didn't get any id
        {
            res = i; //assign the next id
            UserSystemId.put(userId, res);
            LinkedList obIndv = new LinkedList();
            HPCController.ArrayIndvList.add(obIndv); //Allocate a new Slot for the 
        }
        if (len >= maxUsers) {
            System.out.println("System reached Maximum Users"); //send warning message to admin            
        }

        return res;
    }

    public synchronized static void removeUserFromSystem(int id, boolean finish) {
        User u = (User) UsersList.get(id);
        if (u != null) {
            UserSystemId.remove(id);//make the user free
            UsersList.remove(id);
            countUsers--;
            dbm.doLocalCleanUp(u.UserId);
            dbm.doLocalCleanUpExit(u.UserId);//clean the SDM table
            if (!finish) //search is not finished so save the abort time
            {
                u.saveTime("ABORT");
            }
            u.t.stop(); //call the stop method of user
            System.out.println("User Removed from System with ID " + u.UserId);
        }
    }

    public static void setUsrParam(int[] chosenFF, int[] chosenBMP) {
        NewuserParamters param = new NewuserParamters();
    }

    /*
     * Use to modify the system at runtime, like adding or removing a cluster
     * computer
     */
    private static void checkSystemConfigChange() {

        ComputerclusterInfo[] clust = dbm.checkClusterChange();
        //check cluster change
        if (clust != null && clust.length > 0) {
            for (int i = 0; i < clust.length; i++) {
                int clusterid = -1;
                boolean status = false;
                clusterid = clust[i].getClusterid();
                status = clust[i].isStatus();
                HPCController.changeClusterInfo(clusterid, status);
            }
        }
    }
}
