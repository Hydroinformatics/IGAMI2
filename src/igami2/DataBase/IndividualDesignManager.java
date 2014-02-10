/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DataBase;

import igami2.DistributedSystem.DistributedSystem;
import igami2.IGAMI2Main;
import igami2.MixedInitiative.CheckBiases;
import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.NSGA2;
import igami2.Optimization.DistributedNSGAII.wrestore.SWAT_BMPs_NonInteracOptim.SWAT_BMPs_NSGA2Handler;
import java.util.LinkedList;
import java.io.*;
import java.rmi.Naming;
import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author VIDYA
 */
public class IndividualDesignManager {

    //public LinkedList<IndividualDesign> IndvDesigns;
    public NSGA2 nsga2;
    public LinkedList<Individual> IndvPopulation; //save the non-dominated Indvs
    public LinkedList<Individual> HDMPopulation_child;
    public HashMap<Integer,Individual> bestIndvs; //best indvs in currect GA loop
    public LinkedList<Individual> TotalIndvs; //used for Training and testing
    public LinkedList<Individual> iFinalIndvs; //low confidence from total cbm
    public LinkedList<Individual> BiasMonitorIndvs; //last paretofront
    int IndvCounter = 0;
    //public String location = "../SWAT/data/";
    public int UserId = 0;//default for system
    public int uSystemId = -1; //0 is an id
    public String userDIR;
    private int StartIndvId = 1000;//based on the count of the Indv in the CBM, now changed to 219
    public DBManager dbm;
    public int SDMGeneration = 0;
    public int global_sessionId = 0;
    public int local_sessionId = 0;
    public int sessionId = 0;
    public int session_type_search = 0; //for HDM and SDM types
    public int feedback_type=0;// 0-Introspection and 1-feedback
    public int searchId =0;
    public int introspectionNo = 0; //0 is first introspection
    public int feedbackid=0;
    //public int[] regionSubbasinId;
    public SWAT_BMPs_NSGA2Handler nsga2Handle;
    public int[] chosenFF;
    public int[] chosenBMPs;
    String host;
    private long min = 1000 * 10;
    private long wait = 1000 * 5;
    private boolean checkDuplicate = true;
    private boolean fromDB = true;
    private boolean checkBiase = true;
    public int[] farm_own;
    public int[] farm_crop;
    public int[] farm_cash;
    public int nowait=0;
    public int leave_forday = 0;
    public int jump =0;
    public int stop =0;
    private String FName = "";
    private String LName = "";
    private String email ="";
    private String gender = "";
    private int maxInitIndvNeeded = 20; //if zero then, use all otherwise use the selcted no
    private int maxFinalIndvNeeded = 20; 
    private double avgConfidence = 50;
    private CheckBiases bias;
    private boolean democratictest = false;
    //private int[] userIds = {11,12,13};
    //String []emailIds = {"bhushanjss@gmail.com","bhushanjss@gmail.com","bhushanjss@gmail.com"};//,"bhushanjss@gmail.com","bhushanjss@gmail.com"};
    private int[] userIds = {62,64,65,68,69};//62,64,65,68,69
    String []emailIds = {"meghna@oregonstate.edu","awsamuel@iupui.edu","debora.piemonti@gmail.com","waltersk@onid.orst.edu","glivings@gmail.com"};
    private int feedback_type_id = 0; //stores the feedback_type_id info for HS or Introsp.



    //int[] regionSubbasinId;
    /*
     * Using the RMI Temp Interface
     */
    public IndividualDesignManager(int userId, int uSystemId, String UserDIR, DBManager dbm, String host, int[] chosenFF, int[] chosenBMPs, boolean fromDB, int searchId) {
        this.nsga2Handle = nsga2Handle;
        this.UserId = userId;
        this.uSystemId = uSystemId;
        this.host = host;
        this.chosenFF = chosenFF;
        this.chosenBMPs = chosenBMPs;
        //this.regionSubbasinId = regionSubbasinId;
        this.searchId = searchId;
        
        File f = new File(UserDIR);
        if (!f.exists()) {
            f.mkdirs();//create the parent dirs
        }
        this.userDIR = UserDIR;
        this.dbm = dbm;
        this.fromDB = fromDB;
        //setLocalPreferences();
        initStuffs();
    }

    /*
     * Using the Data Base
     */
    public IndividualDesignManager(int userId, int uSystemId, String UserDIR, DBManager dbm, int[] chosenFF, int[] chosenBMPs, boolean fromDB, int searchId) {
        this.nsga2Handle = nsga2Handle;
        this.UserId = userId;
        this.uSystemId = uSystemId;
        this.chosenFF = chosenFF;
        this.chosenBMPs = chosenBMPs;
        //this.regionSubbasinId = regionSubbasinId;
        this.searchId = searchId;
        //IndvDesigns = new LinkedList<IndividualDesign>();        
        File f = new File(UserDIR);
        if (!f.exists()) {
            f.mkdirs();//create the parent dirs
        }
        this.userDIR = UserDIR;
        this.dbm = dbm;
        this.fromDB = fromDB;
        if(userId==2) //run democratic test using User 2
            democratictest=true;
        initStuffs();
    }
    
    void initStuffs()
    {
        IndvPopulation = new LinkedList<Individual>();
        bestIndvs = new HashMap<Integer,Individual>();
        TotalIndvs = new LinkedList<Individual>();
        iFinalIndvs = new LinkedList<Individual>();
        BiasMonitorIndvs = new LinkedList<Individual>();
        bias = new CheckBiases();        
    }

    public int getStartIndvId() {
        return StartIndvId;
    }

    public void setStartIndvId(int StartIndvId) {
        this.StartIndvId = StartIndvId;
    }

    public void setTotalIndvs(LinkedList<Individual> TotalIndvs) {
        this.TotalIndvs = TotalIndvs;
    }

    public void setSearchId(int searchId) {
        this.searchId = searchId;
    }
    
    

    public void setUserInfo(String FName, String LName, String email, String gender)
    {
        this.FName = FName;
        this.LName = LName;
        this.email = email;
        this.gender = gender;
    }
    
    protected String getFName()
    {
        return this.FName;
    }
    
    protected String getLName()
    {
        return this.LName;
    }
    
    protected String getEmail()
    {
        return this.email;
    }
    
    protected String getGender()
    {
        return this.gender;
    }
    

    public void generateID() {

        if (checkDuplicate) //check if there is any duplicate Individual in the database
        {
            for (int i = 0; i < IndvPopulation.size(); i++) {
                IndvPopulation.get(i).UserId = UserId;
                IndvPopulation.get(i).confidence = 50;
                //
                int res = dbm.getDuplicateId(IndvPopulation.get(i));
                if (res == 0) {
                    IndvPopulation.get(i).IndvId = StartIndvId++;
                } else {
                    IndvPopulation.get(i).IndvId = res;
                }
            }
        } else {
            for (int i = 0; i < IndvPopulation.size(); i++) {
                IndvPopulation.get(i).UserId = UserId;
                IndvPopulation.get(i).confidence = 50;
                IndvPopulation.get(i).IndvId = StartIndvId++;
            }
        }
    }

    public void takeFeedback() {
        HDMPopulation_child = new LinkedList<Individual>();
        generateID(); // for the new set of the individual

        
        System.out.println("Taking Feedback for user " + UserId);

        //Using RMI Temporary Interface

        if (!fromDB) {
            try {
                //System.out.println("On Host " + host);
                DistributedSystem ob = (DistributedSystem) Naming.lookup(host);
                IndvPopulation = ob.takeFeedback(IndvPopulation);
            } catch (Exception ex) {
                System.out.println("Problem in connecting host " + host);
                //ex.printStackTrace();
                while (true) //busy loop
                {
                    try {
                        Thread.sleep(5000);//wait for 5 sec try again
                        //System.out.println("On Host " + host);
                        DistributedSystem ob = (DistributedSystem) Naming.lookup(host);
                        IndvPopulation = ob.takeFeedback(IndvPopulation);
                        //sucess
                        break;
                    } catch (Exception exx) {
                    }
                }
            }
        }


        //Using the DBMS
        if (fromDB) {
            
            this.feedback_type=1;//feedback only               
            feedback_type_id = this.sessionId;                
            
            boolean error = true;
            try
            {
                LinkedList<Individual> addindv = bias.addBiasIndvFeedback(IndvPopulation);     
                IndvPopulation = addindv;
                error = false;
                LinkedList<Integer> IndvIds = bias.getIndvIds();
                IndvPopulation = takeFeedbackDBM(IndvPopulation,IndvIds,IGAMI2Main.em.SEARCH_FEEDBACK);
                IndvPopulation = bias.removeBiasIndvFeedback(IndvPopulation);       
                //save the bias and new info
                dbm.saveBiasIndvData(this.UserId,global_sessionId, local_sessionId, sessionId, session_type_search,bias.BiasMonitorIndvsInfo,bias.BiasMonitorIndvsInfoFeedback);  
            }catch(Exception ex)
            {
                ex.printStackTrace();
                //if error due to adding bias indv, use without bias indv
                //save the indvids
                if (error) {
                    LinkedList<Integer> IndvIds = new LinkedList();
                    for (int i = 0; i < IndvPopulation.size(); i++) {
                        IndvIds.add(IndvPopulation.get(i).IndvId);
                    }
                    IndvPopulation = takeFeedbackDBM(IndvPopulation,IndvIds,IGAMI2Main.em.SEARCH_FEEDBACK);
                    //saveFeedbackTime(IndvIds,UserId);
                }
            }
        }
     
        for (int i = 0; i < IndvPopulation.size(); i++) {
            IndvPopulation.get(i).nsga2 = nsga2;
            //double rating = IndvPopulation.get(i).rating;
            HDMPopulation_child.add(IndvPopulation.get(i));//save the HDMA
            TotalIndvs.add(IndvPopulation.get(i)); //for training and testing  
           
            //save the best Indv, rating 3
            for(Individual in:IndvPopulation)
            {
                if(in.rating>2)
                {
                    if(!bestIndvs.containsKey(in.IndvId))
                    this.bestIndvs.put(in.IndvId, in);
                }
            }         
        }
        if (fromDB) {
         addBiasIndvs(HDMPopulation_child); //for next session
        }
    }

    public LinkedList<Individual> doIntrospectionCBM(LinkedList<IndividualDesign> IndvPop, boolean all) {
        LinkedList<Individual> res = null;
        LinkedList<Individual> Indv = null;     
        Indv = nsga2Handle.generateIndividual(IndvPop, chosenFF, chosenBMPs, UserId);

        //on Temporary RMI Interface
        if (!fromDB) {
            try {
                //System.out.println("On Host " + host);
                DistributedSystem ob = (DistributedSystem) Naming.lookup(host);
                res = ob.takeFeedback(Indv);
            } catch (Exception ex) {
                System.out.println("Problem in connecting host " + host);
                ex.printStackTrace();
                ex.printStackTrace();
                while (true) //busy loop
                {
                    try {
                        Thread.sleep(5000);//wait for 5 sec try again

                        //System.out.println("On Host " + host);
                        DistributedSystem ob = (DistributedSystem) Naming.lookup(host);

                        IndvPopulation = ob.takeFeedback(IndvPopulation);
                        //sucess
                        break;

                    } catch (Exception exx) {
                    }
                }
            }
        }


        //Using DBMS

        if (fromDB) {
            feedback_type=0; //introspection

            //IGAMI2Main.em.sendEmail(this.getEmail(), IGAMI2Main.em.INTROSPECTION, UserId, this.getFName());
            if(local_sessionId<2&&session_type_search ==1)
            {
                jump =1;
                introspectionNo++; //in case of JUMP
            }
            feedback_type_id = this.introspectionNo;
            System.out.println("Introspection No " + introspectionNo);
            //dbm.putSessionInfo(this.UserId,feedback_type,this.local_sessionId,introspectionNo,this.session_type_search,nowait,leave_forday,jump, stop );
            
            if(this.maxInitIndvNeeded==0||all) //use all the individuals
            {
                //save the indvids
                LinkedList<Integer> IndvIds = new LinkedList();
                for(int i=0;i<Indv.size();i++)
                {
                    IndvIds.add(Indv.get(i).IndvId);
                }
                Indv = takeFeedbackDBM(Indv,IndvIds,IGAMI2Main.em.INTROSPECTION);
                //saveFeedbackTime(IndvIds,UserId);
                addBiasIndvs(Indv); //bias
            }
            else
            {
                Indv = takeFeedbackLowConfidenceIndvDBM(Indv);
            }       
            res = Indv;
        }
        if (res != null) {           
            updateCBM(res);
        }
        introspectionNo++;
        //dbm.saveIntrospectionIndvs(res, global_sessionId, local_sessionId, this.introspectionNo, session_type_search);
 
        return res;

    }

    public boolean showResult(LinkedList<Individual> indv, double[] SDMRank, double[] data) {
        boolean res = true;
//        double[] res = dbm.getSDMData(UserId,sdmId,sdmGenerationId);
        try {
            System.out.println("Showing Result to user " + UserId);
            System.out.println("On Host " + host);
            DistributedSystem ob = (DistributedSystem) Naming.lookup(host);

            res = ob.showResult(indv, SDMRank, data);

            System.out.println("Feedback Completed for user " + UserId);

        } catch (Exception ex) {
            System.out.println("Problem in connecting host " + host);
            ex.printStackTrace();
        }

        return res;
    }

    //save the total designs
    public void save() {
        //save the changed verion of the individual

        saveArchive();
        saveBestIndv();
        //save bestIndv    
        reInitIndv();

    }

    public void reInitIndv() {
        TotalIndvs = new LinkedList<Individual>();//reinit 
        bestIndvs = new HashMap<Integer,Individual>();//reinit
        this.iFinalIndvs = new LinkedList<Individual>();//reinit
    }

    public void saveBestIndv() {
        //
        Collection<Individual> c = bestIndvs.values();
        LinkedList<Individual> res = new LinkedList(c);
        if(this.democratictest)
        {
            for(int i=0;i<this.userIds.length;i++)
            {
                dbm.saveCBM(userIds[i],res,this.feedbackid, global_sessionId, local_sessionId, sessionId, session_type_search);
            }
        }
        else
        {
            dbm.saveCBM(this.UserId,res,this.feedbackid, global_sessionId, local_sessionId, sessionId, session_type_search);
        }
        //reInitIndv();
    }

    public void saveArchive() {
        //dbm.saveArchive(TotalIndvs, global_sessionId, sessionId, session_type_search);HDMPopulation_child
        dbm.saveArchive(this.UserId,HDMPopulation_child,IndvPopulation,feedbackid, global_sessionId, local_sessionId, sessionId, session_type_search);
        feedbackid++;
    }

    public void saveFinalPopulation()
    {
        //save the final population after the automated search
        if(this.democratictest)
        {
            for(int i=0;i<this.userIds.length;i++)
            {
               dbm.saveCBM(userIds[i],IndvPopulation,feedbackid, global_sessionId, local_sessionId, sessionId, session_type_search);                 
            }            
        }
        else
        {            
            dbm.saveCBM(this.UserId,IndvPopulation,feedbackid, global_sessionId, local_sessionId, sessionId, session_type_search);  
        }
    }
    /*
     * Not used yet
     */
    public LinkedList<Individual> IndividualDesignToIndividual1(LinkedList<IndividualDesign> IndvPop) {
        LinkedList<Individual> res = null;

        for (int i = 0; i < IndvPop.size(); i++) {
            //Individual in = new SWAT_BMPs_Individual();
        }

        return res;
    }

    private void updateCBM(LinkedList<Individual> res) {
        dbm.updateCBM(res, global_sessionId, local_sessionId, sessionId, session_type_search);
    }

    //Do Introspection of TotalIndv, Second Introspection, mainly whoese confidence is low
    public void doIntrospectionTotalBest() {
        
        LinkedList<IndividualDesign> IndvPop = dbm.getCBMIndv( UserId,0,0, chosenFF);

        //load the CBM Indvs
        LinkedList<Individual> Indv = nsga2Handle.generateIndividual(IndvPop, chosenFF, chosenBMPs, UserId);
        LinkedList<Individual> totalForFeedback = new LinkedList();
        
        for(int i=0;i<Indv.size();i++)
        {
            totalForFeedback.add(Indv.get(i));
        }
        
        //On Trmporary Interface

        if (!fromDB) {
            try {
                DistributedSystem ob = (DistributedSystem) Naming.lookup(host);
                totalForFeedback = ob.takeFeedback(totalForFeedback);
            } catch (Exception ex) {
                System.out.println("Problem in connecting host " + host);
                ex.printStackTrace();
            }
        }
        //on DBMS

        //do introspection only on selcted set of individuals, having low confidence from behind
        if (fromDB) {
            feedback_type=0; //introspection
            feedback_type_id = this.introspectionNo;
            //IGAMI2Main.em.sendEmail(this.getEmail(), IGAMI2Main.em.INTROSPECTION, UserId, this.getFName());
            System.out.println("Introspection No " + introspectionNo);
            //dbm.putSessionInfo(this.UserId,feedback_type,this.local_sessionId,introspectionNo,this.session_type_search,nowait,leave_forday,jump, stop );
            
            if(this.maxInitIndvNeeded==0) //use all the individuals
            {               
                //save the indvids
                LinkedList<Integer> IndvIds = new LinkedList();
                for(int i=0;i<totalForFeedback.size();i++)
                {
                    IndvIds.add(totalForFeedback.get(i).IndvId);
                }
                totalForFeedback = takeFeedbackDBM(totalForFeedback,IndvIds,IGAMI2Main.em.INTROSPECTION);              
                addBiasIndvs(totalForFeedback); //bias
            }
            else
            {
                totalForFeedback = takeFeedbackLowConfidenceIndvDBM(totalForFeedback);
            }
        }

        //update the best Indvs to TotalIndvs
        iFinalIndvs = totalForFeedback;
        //update back to DB
        if(totalForFeedback.size()>0)  //some indv for update
            dbm.updateCBM(totalForFeedback, global_sessionId, local_sessionId, session_type_search, session_type_search);
        dbm.saveArchive(this.UserId,totalForFeedback,null,feedbackid, global_sessionId, local_sessionId, sessionId, session_type_search);
        feedbackid++;
        introspectionNo++;
        //dbm.saveIntrospectionIndvs(totalForFeedback, global_sessionId, local_sessionId, this.introspectionNo, session_type_search);
    }

    private void saveFeedbackTime(LinkedList<Integer> IndvIds, int userId)
    {
        LinkedList feedbackEvent = dbm.getFeedbackEventTimeData(IndvIds,userId);
        dbm.saveUserFeedbackTiming(userId,feedbackid, global_sessionId, local_sessionId, sessionId, feedback_type, feedbackEvent);
    }
    
    /*
     * Use to Take the feedback from the DataBase
     */
    private LinkedList<Individual> takeFeedbackDBM(LinkedList<Individual> Indv, LinkedList<Integer> IndvIds,int Feedback_type) {
        boolean[] abort = new boolean[2];//first to abort and second to discard

        //A democratic User Search
        if (this.democratictest) {
            //IGAMI2Main.em.sendEmail(this.getEmail(), IGAMI2Main.em.SEARCH_FEEDBACK, UserId, this.getFName());

            //crete a HashMap to track which user is done
            HashMap<Integer, Boolean> jobdone = new HashMap();
            for (int i = 0; i < userIds.length; i++) {
                if(feedbackid==0)
                {
                    IGAMI2Main.em.sendEmail(this.emailIds[i], IGAMI2Main.em.SEARCH_BEGIN, this.userIds[i], "User");
                }
                else
                {
                    IGAMI2Main.em.sendEmail(this.emailIds[i], Feedback_type, this.userIds[i], "User");
                }
                dbm.putSessionInfo(userIds[i], feedback_type, this.local_sessionId, feedback_type_id, this.session_type_search, nowait, leave_forday, jump, stop);                
                jobdone.put(i, false);
                dbm.setUserInActive(userIds[i]);//Allow the users to login
            }
            dbm.putFeedbackDataAllUsers(Indv, userIds); //put the feedback data in Data base and notify the webserver
            try {
                Thread.sleep(min); //min time for feedback
            } catch (InterruptedException ex) {
            }

            //Busy looping
            boolean done = false;
            while (!done) //go on ininite while loop and sleep unless the data comes 
            {
                done = true;
                for (int i = 0; i < jobdone.size(); i++) {
                    if (!jobdone.get(i)) 
                    {
                        if (dbm.getMasterEvent(userIds[i])) //If Notified
                        {
                            jobdone.put(i, true);
                            dbm.setUserActive(userIds[i]);//Stop the user's from login to website
                        }
                        else {
                            done = false; //not done yet
                            break;
                        }
                    }
                }
                    try {
                        Thread.sleep(wait); //min time for feedback
                        //done = false;
                    } catch (InterruptedException ex) {
                    }
            }

            //retrieve the results back for every user
            LinkedList<IndividualDesign>[] AllIndv = new LinkedList[userIds.length];
            for (int k = 0; k < userIds.length; k++) {
                int uId = userIds[k];
                LinkedList<IndividualDesign> fIndv = dbm.getFeedbackData(uId);
                AllIndv[k] = fIndv;

                //update the rating and confidence
                if (fIndv.size() >= Indv.size()) {
                    for (int i = 0; i < Indv.size(); i++) {
                        Indv.get(i).rating = fIndv.get(i).rating;
                        Indv.get(i).confidence = fIndv.get(i).confidence;
                    }
                } else {
                    System.out.println("Some Individuals are Lost");
                    for (int i = 0; i < fIndv.size(); i++) {
                        if (fIndv.get(i).rating > 0) //if not greater than 0, that means user hasn't given rating for that so reatain the previous rating
                        {
                            Indv.get(i).rating = fIndv.get(i).rating;
                        }
                        if (fIndv.get(i).confidence > 0) {
                            Indv.get(i).confidence = fIndv.get(i).confidence;
                        }
                    }
                }
                dbm.saveArchive(uId, Indv, null, feedbackid, global_sessionId, local_sessionId, sessionId, this.session_type_search);
                saveFeedbackTime(IndvIds,uId);
               
            }
            
            //find the democratic rating and confidence
            Indv = getDemocraticRating(Indv,AllIndv);
            

        } //A single User Search
        else {
            if (feedbackid == 0) { //no need of first feedback for single user experiment
                //IGAMI2Main.em.sendEmail(this.getEmail(), IGAMI2Main.em.SEARCH_BEGIN, UserId, this.getFName());
            } else { //any other feedbacks.
                IGAMI2Main.em.sendEmail(this.getEmail(), Feedback_type, UserId, this.getFName());
            }
            dbm.putSessionInfo(this.UserId, feedback_type, this.local_sessionId, feedback_type_id, this.session_type_search, nowait, leave_forday, jump, stop);
            dbm.putFeedbackData(Indv); //put the feedback data in Data base and notify the webserver
            try {
                Thread.sleep(min); //min time for feedback
            } catch (InterruptedException ex) {
            }

            //Busy looping
            while (true) //go on ininite while loop and sleep unless the data comes 
            {
                if (dbm.getMasterEvent(UserId)) //If Notified
                {
                    break;
                } else //Sleep for a while and recheck again
                {
                    try {
                        Thread.sleep(wait); //min time for feedback
                    } catch (InterruptedException ex) {
                    }
                }
            }

            //check if the user wants to continue or not
            abort = dbm.checkAbort();

            if (abort[0]) {
                /*
                 * //if true, discrad the data if (abort[1]) {
                 * dbm.cleanCurrentSearch(); System.out.println("Discarding the
                 * data"); } else
                 *
                 */
                System.out.println("Data is not discarded ");
                //do the cleanup
                dbm.doLocalCleanUp(UserId);
                dbm.doLocalCleanUpExit(UserId);
                //System.out.println("User has aborted the search");
                IGAMI2Main.removeUserFromSystem(UserId, false);
            }

            //retrieve the results back
            LinkedList<IndividualDesign> fIndv = dbm.getFeedbackData(this.UserId);

            //update the rating and confidence
            if (fIndv.size() >= Indv.size()) {
                for (int i = 0; i < Indv.size(); i++) {

                    Indv.get(i).rating = fIndv.get(i).rating;
                    Indv.get(i).confidence = fIndv.get(i).confidence;
                }
            } else {
                System.out.println("Some Individuals are Lost");
                for (int i = 0; i < fIndv.size(); i++) {
                    if (fIndv.get(i).rating > 0) //if not greater than 0, that means user hasn't given rating for that so reatain the previous rating
                    {
                        Indv.get(i).rating = fIndv.get(i).rating;
                    }
                    if (fIndv.get(i).confidence > 0) {
                        Indv.get(i).confidence = fIndv.get(i).confidence;
                    }
                }
            }
            
            saveFeedbackTime(IndvIds,UserId);
        }
        return Indv;
    }

    public void doFirstIntrospectionCBM(LinkedList<IndividualDesign> IndvPop) {
        LinkedList<Individual> res = null;
        LinkedList<Individual> Indv = null;

        System.out.println("Doing Introspection for user " + UserId);

        //Restart to Individual
        Indv = nsga2Handle.generateIndividual(IndvPop, chosenFF, chosenBMPs, UserId);

        //do nondominated soring of thse individuals and send the best ones
        
       Indv = nsga2.doNonDominatedSortingFirstPop(Indv);
       
       
       
        //on Temporary RMI Interface
       //System.out.println("No of CBM individuals for user"+UserId +" is "+Indv.size());

        if (!fromDB) {
            try {
                //System.out.println("On Host " + host);
                DistributedSystem ob = (DistributedSystem) Naming.lookup(host);
                res = ob.takeFeedback(Indv);

            } catch (Exception ex) {
                System.out.println("Problem in connecting host " + host);
                ex.printStackTrace();
                
                ex.printStackTrace();
                while (true) //busy loop(polling)
                {
                    try {
                        Thread.sleep(5000);//wait for 5 sec try again

                        //System.out.println("On Host " + host);
                        DistributedSystem ob = (DistributedSystem) Naming.lookup(host);

                        IndvPopulation = ob.takeFeedback(IndvPopulation);
                        //sucess
                        break;

                    } catch (Exception exx) {
                    }
                }
            }
        }


        //Using DBMS

        if (fromDB) {
            feedback_type=0; //introspection
            feedback_type_id = this.introspectionNo;
            int nowait=0;
            System.out.println("Introspection No " + introspectionNo);
            //dbm.putSessionInfo(this.UserId,feedback_type,this.local_sessionId,introspectionNo,this.session_type_search,nowait,leave_forday,jump, stop);
            
            //save the indvids
            LinkedList<Integer> IndvIds = new LinkedList();
            for(int i=0;i<Indv.size();i++)
            {
                IndvIds.add(Indv.get(i).IndvId);
            }
            res = takeFeedbackDBM(Indv,IndvIds,IGAMI2Main.em.INTROSPECTION);
            //saveFeedbackTime(IndvIds,UserId);
            addBiasIndvs(res); //bias
            res = nsga2.doNonDominatedSortingFirstPop(res); //second Non-Dominated sorting based on Ratings given by user
        }
        //dbm.saveIntrospectionIndvs(res, global_sessionId, local_sessionId, this.introspectionNo, session_type_search);
        introspectionNo++;
        updateFirstCBM(res);       
    }
    
    
    public void doLastIntrospectionCBM(LinkedList<IndividualDesign> IndvPop) {
        LinkedList<Individual> res = null;
        LinkedList<Individual> Indv = null;

        System.out.println("Doing Introspection for user " + UserId);

        //Restart to Individual
        Indv = nsga2Handle.generateIndividual(IndvPop, chosenFF, chosenBMPs, UserId);

        //do nondominated soring of thse individuals and send the best ones
        
       //Indv = nsga2.doNonDominatedSortingFirstPop(Indv);
       
       Indv = selectBestDesigns(Indv);
       
        //on Temporary RMI Interface
       //System.out.println("No of CBM individuals for user"+UserId +" is "+Indv.size());

        if (!fromDB) {
            try {
                //System.out.println("On Host " + host);
                DistributedSystem ob = (DistributedSystem) Naming.lookup(host);
                res = ob.takeFeedback(Indv);

            } catch (Exception ex) {
                System.out.println("Problem in connecting host " + host);
                ex.printStackTrace();
                
                ex.printStackTrace();
                while (true) //busy loop(polling)
                {
                    try {
                        Thread.sleep(5000);//wait for 5 sec try again

                        //System.out.println("On Host " + host);
                        DistributedSystem ob = (DistributedSystem) Naming.lookup(host);

                        IndvPopulation = ob.takeFeedback(IndvPopulation);
                        //sucess
                        break;

                    } catch (Exception exx) {
                    }
                }
            }
        }


        //Using DBMS

        if (fromDB) {
            feedback_type=0;
            feedback_type_id = this.introspectionNo;
            int nowait=0;
            //IGAMI2Main.em.sendEmail(this.getEmail(), IGAMI2Main.em.INTROSPECTION, UserId, this.getFName());
            System.out.println("Introspection No " + introspectionNo);
            //dbm.putSessionInfo(this.UserId,feedback_type,this.local_sessionId,introspectionNo,this.session_type_search,nowait,leave_forday,jump, stop);
            
            
            //save the indvids
            LinkedList<Integer> IndvIds = new LinkedList();
            for(int i=0;i<Indv.size();i++)
            {
                IndvIds.add(Indv.get(i).IndvId);
            }
            res = takeFeedbackDBM(Indv,IndvIds,IGAMI2Main.em.INTROSPECTION);
            //saveFeedbackTime(IndvIds,UserId);
        }
        //dbm.saveIntrospectionIndvs(res, global_sessionId, local_sessionId, this.introspectionNo, session_type_search);

        if (res != null) {        
            //res = nsga2.doNonDominatedSortingFirstPop(res); //second Non-Dominated sorting based on Ratings given by user           
            dbm.updateCBM(res, global_sessionId, local_sessionId, session_type_search, session_type_search);
            //updateFirstCBM(res); no need to update as this is the last CBM
            //addBiasIndv(res);
        }
        dbm.saveArchive(this.UserId,res,null,feedbackid, global_sessionId, local_sessionId, sessionId, session_type_search);
        feedbackid++;
        introspectionNo++;
    }
    
    /*
     * Inform the user that the automoated search has started and he can leave for the day
     */
    
    public void informAutomatedSearch()
    {
        if(this.democratictest)
        {
            for(int i=0;i<this.userIds.length;i++)
                IGAMI2Main.em.sendEmail(this.emailIds[i], IGAMI2Main.em.AUTOMATED, this.userIds[i], "User");
        }
        else
        {
            IGAMI2Main.em.sendEmail(this.getEmail(), IGAMI2Main.em.AUTOMATED, UserId, this.getFName());
        }
    }
    
    public void informSearchFinished()
    {
        if(this.democratictest)
        {
            for(int i=0;i<this.userIds.length;i++)
                IGAMI2Main.em.sendEmail(this.emailIds[i], IGAMI2Main.em.AUTOMATED, this.userIds[i], "User");
        }
        else
        {
            IGAMI2Main.em.sendEmail(this.getEmail(), IGAMI2Main.em.SEARCH_FINISHED, UserId, this.getFName());
        }
    }

    private void updateFirstCBM(LinkedList<Individual> res) {
        if(this.democratictest)
        {
            for(int i=0;i<this.userIds.length;i++)
            {
                dbm.saveArchive(userIds[i],res, null, feedbackid, global_sessionId, local_sessionId, sessionId, sessionId);      
                dbm.addFirstCBM(userIds[i],res,this.feedbackid, global_sessionId, local_sessionId, sessionId, session_type_search);              
            }
        }
        else
        {
            dbm.saveArchive(this.UserId,res, null, feedbackid, global_sessionId, local_sessionId, sessionId, sessionId);      
            dbm.addFirstCBM(this.UserId,res,this.feedbackid, global_sessionId, local_sessionId, sessionId, session_type_search);
        }
        feedbackid++;
    }

    private LinkedList<Individual> takeFeedbackLowConfidenceIndvDBM(LinkedList<Individual> TotalIndvs) {
        
        // find first set of low confidence individual
        LinkedList<Individual> badConfdIndv = new LinkedList();
        LinkedList<Individual> goodConfdIndv = new LinkedList();
        int count=0;
        //add CBM individuals in reverse order
        int totl = TotalIndvs.size();
        for(int i=totl-1;i>=0;i--)
        {
            
            Individual in = TotalIndvs.get(i);
            if(in.confidence<=avgConfidence)
            {
                if(count<this.maxInitIndvNeeded)
                {
                    badConfdIndv.add(in);
                    count++;
                }
                else
                    break;
            }
            else
            {
                goodConfdIndv.add(in);
            }
        }
        
        int k=0;
        while(count<this.maxInitIndvNeeded && goodConfdIndv.size()>0 && k<goodConfdIndv.size())
        {
            badConfdIndv.add(goodConfdIndv.get(k));
            count++;
            k++;
        }
        
        //save the indvids
        LinkedList<Integer> IndvIds = new LinkedList();
        for (int i = 0; i < badConfdIndv.size(); i++) {
            IndvIds.add(badConfdIndv.get(i).IndvId);
        }
        badConfdIndv = takeFeedbackDBM(badConfdIndv,IndvIds,IGAMI2Main.em.INTROSPECTION);
        //saveFeedbackTime(IndvIds,UserId);
        addBiasIndvs(badConfdIndv); //bias
        
        return badConfdIndv;
    }

    public void addLastPareto(LinkedList<Individual> HDMPopulation) {
        
        //add best from last pareto of nondominated
        
        for(int i=0;i<HDMPopulation.size();i++)
        {
            Individual in1 = HDMPopulation.get(i);
            //if(in1.rating>2) //add entire non domination front from the last generation
            {         
                if(!bestIndvs.containsKey(in1.IndvId))
                    bestIndvs.put(in1.IndvId,in1);
            }
        }
    }

    /*
     * used to select N best Individuals
     * 
     * //create set of rules
     */
    private synchronized LinkedList<Individual> selectBestDesigns(LinkedList<Individual> Indv) {
        
        LinkedList<Individual> res = new LinkedList();
        
        //select best rating 3, from behind
        BitSet mapSelected = new BitSet(Indv.size());
        int countSelected = 0;
        
        for(int i=Indv.size()-1;i>=0;i--)
        {
            Individual in = Indv.get(i);
            if(in.rating==3) //best Indv
            {
                res.add(in);
                mapSelected.set(i); //sets the bit for selected indv
                countSelected++;
                if(countSelected>=maxFinalIndvNeeded)
                {
                    break;
                }
            }
        }
        ////select high confidence
        
        if (countSelected < maxFinalIndvNeeded) //need more Indv
        {
            for (int i = Indv.size() - 1; i >= 0; i--) {
                Individual in = Indv.get(i);
                if (!mapSelected.get(i)) //not selected
                {
                    if (in.confidence > avgConfidence) //confidence for Indv is better
                    {
                        res.add(in);
                        mapSelected.set(i); //sets the bit for selected indv
                        countSelected++;
                        if (countSelected >= maxFinalIndvNeeded) {
                            break;
                        }
                    }
                }
            }
        }
        
        //select rest from behind
        
        if (countSelected < maxFinalIndvNeeded) //need more Indv
        {
            for (int i = Indv.size() - 1; i >= 0; i--) {
                Individual in = Indv.get(i);
                if (!mapSelected.get(i)) //not selected
                {
                    res.add(in);
                    mapSelected.set(i); //sets the bit for selected indv
                    countSelected++;
                    if (countSelected >= maxFinalIndvNeeded) {
                        break;
                    }

                }
            }
        }

        
        return res;
    }
    
    
    public void addBiasIndvs(LinkedList<Individual> res)
    {
        try
        {
            bias.addBiasIndv(res);
        }catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void dumpData() {
        dbm.dumpData(FName);
    }

    private LinkedList<Individual> getDemocraticRating(LinkedList<Individual> Indv, LinkedList<IndividualDesign>[] AllIndv) {
        VotingSystem vs = new VotingSystem();
        for(int i=0;i<Indv.size();i++)
        { 
           vs.performVoting(AllIndv,i);
           Indv.get(i).rating = vs.getFinalVote();
           Indv.get(i).confidence = vs.getFinalConfidence();
        }        
        return Indv;
    }
}
