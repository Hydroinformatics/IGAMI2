/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2;

import igami2.DataBase.DBManager;
import igami2.DataBase.IndividualDesignManager;
import igami2.MixedInitiative.MixedInitiativeManager;
import igami2.Optimization.DistributedNSGAII.wrestore.SWAT_BMPs_NonInteracOptim.SWAT_BMP_ResearchEvaluator;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author VIDYA
 */
public class User implements Runnable
{
    
    private String Name;
    private String ClientId;
    protected Thread t;
    private MixedInitiativeManager mim;
    private IndividualDesignManager idm;
    private DBManager dbm;
    public int UserId=0;
    public Date date = null;
    private int [] chosenFF = new int[] {0,1,1,1,1,0,1}; //This tells us which BMPs are we simulating in this optimization problem.
    private int [] chosenBMPs = new int[] {1,1,1,1,1,1,0,1,1,0};
    
    private int[] regionSubbasinId = new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,71,76,77,78,80,82,83,85,86,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,110,111,112,115,117,119,121,122,123,124,125,126,127};     
    private  int[] tenure_regionSubbasinId = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}; //new int[regionSubbasinId.length];     
    public String userHost = "";
    private int searchId = 0; //starting
    private String admin_Email = IGAMI2Main.admin_Email;
    private SimpleDateFormat dateFormat;
    private boolean resume=false;
    private String userDIR = "../SWAT/USER/user";
    
    /*
     * Using the Database
     */
    public User(int UserId,int [] chosenFF, int[] chosenBMP, boolean fromDB, int uSystemId)
    {
        dbm = new DBManager(UserId, chosenFF);
       
        this.chosenFF = chosenFF;
        //set the last function true;
        this.chosenFF[chosenFF.length-1] = 1;// make the rankFF as always true   
        this.chosenBMPs = chosenBMP;            
        searchId = dbm.searchId;
        if (UserId != 1) { //not the system           
            idm = new IndividualDesignManager(UserId, uSystemId, userDIR, dbm, chosenFF, chosenBMPs, fromDB, searchId);
            mim = new MixedInitiativeManager(idm, chosenFF, chosenBMPs, UserId, regionSubbasinId, tenure_regionSubbasinId);
        }
        this.UserId = UserId;
        t = new Thread(this,"USER"+UserId);
        t.start();
    }
    /*
     * Using RMI Temporary interface
     */
    public User(int UserId,int [] chosenFF, String host, boolean fromDB, int uSystemId)
    {
        dbm = new DBManager(UserId, chosenFF);
        this.userHost = host;
        this.chosenFF = chosenFF;
        //set the last function true;
        this.chosenFF[chosenFF.length-1] = 1;// make the rankFF as always true
        userDIR = userDIR + UserId+"/";
        searchId = dbm.searchId;
        idm = new IndividualDesignManager(UserId, uSystemId, userDIR, dbm,host,chosenFF,chosenBMPs,fromDB,searchId);
        mim = new MixedInitiativeManager(idm, chosenFF,chosenBMPs,UserId,regionSubbasinId,tenure_regionSubbasinId);
        this.UserId = UserId;
        t = new Thread(this,"USER"+UserId);
        t.start();
    }
    
    @Override
    public void run() {
        
        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        if (UserId != 1) {
            //stop(); //wont allow new users
            //check if the previous search was unfinished
            //resume = dbm.checkUnfinishedSearch();
            if (!resume) //start a new search
            {
                saveTime("BEGIN");
                IGAMI2Main.em.sendEmail(this.admin_Email,IGAMI2Main.em.ADMIN1, UserId, "Admin"); //inform the admin that the search is started for this user
            } else {
                System.out.println("Previous search is resuming for User " + this.UserId);
            }
            mim.start(resume);
        }
        else //System is running, if id is 1 then user system is used to do some noninteractive search
        {
            saveTime("BEGIN");
            IGAMI2Main.em.sendEmail(this.admin_Email,IGAMI2Main.em.ADMIN1, UserId, "Admin"); //inform the admin that the search is started for this user    
            SWAT_BMP_ResearchEvaluator research = new SWAT_BMP_ResearchEvaluator(UserId,dbm,chosenBMPs,chosenFF,regionSubbasinId,tenure_regionSubbasinId);
            research.evaluatePopulation();
        }
        
        saveTime("FINISHED");       
        IGAMI2Main.em.sendEmail(this.admin_Email,IGAMI2Main.em.ADMIN2, UserId, "Admin"); //inform the admin that the search is finished for this user
        stop(); //use to cleanup the user data, so that user can begin another search
    }
    
    protected void stop()
    {
        boolean finish = true; //search is finished
        //System.out.println("User Exiting the System after finishing the search");
       IGAMI2Main.removeUserFromSystem(UserId,finish);
    }
    
    protected void saveTime(String type)
    {
        date = new Date();
        dbm.saveUserData(UserId, type, dateFormat.format(date));                
    }
}
