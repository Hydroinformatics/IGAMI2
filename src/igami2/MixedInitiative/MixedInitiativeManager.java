/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.MixedInitiative;

import igami2.DataBase.DBManager;
import igami2.DataBase.IndividualDesign;
import igami2.DataBase.IndividualDesignManager;
import igami2.DataBase.hibernateconfig.HdmarchiveChildrenId;
import igami2.DataBase.hibernateconfig.NewuserParamters;
import igami2.DistributedSystem.MasterComputer.PopulationEvaluation;
import igami2.DistributedSystem.VTimer;
import igami2.IGAMI2Main;
import igami2.Introspection.IntrospectionManager;
import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import igami2.Optimization.DistributedNSGAII.wrestore.SWAT_BMPs_NonInteracOptim.SWAT_BMPs_NSGA2Handler;
import igami2.Optimization.OptimizationManager;
import igami2.UserData;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author VIDYA
 */
public class MixedInitiativeManager {

    private int UserId = 0;
    private OptimizationManager om; //used to control different optimization algorithms
    private IntrospectionManager im; //used to manage different types of introspections
    private SDMManager sdmm; //used to manage different Simulated Decision Makers
    private IndividualDesignManager idm; //used to handle different designs and pass to different parts of the system
    private PopulationEvaluation evaluator; //Used to Evalaute population
    private StatisticsManager statm; //use to calculate some statistical information
    private DBManager dbm; //used to manage the database
    private MannKendall kendall; //perform MannKenall test
    //NSGA2 Paramters
    private double MUTATION_PROBABILITY = 0.05;
    private double CROSSOVER_PROBABILITY = 0.9;
    private int HDMPopulation = 20;
    private int SDMPopulation = 100;
    private int NoOfglobalSessions = 1;
    private int maxHDM = 2;// maxHDM+1 number of HDM training, after then SDM is mandatory
    private int noHDMGenerations = 5; //odd to make the std stats odd, which will be +2 more introspections 
    private int noSDMGenerations = 100;//18;//3;//assuming 1 hour for each generation
    private double percentCBM = 20;   // what percent of CBM data to be fed
    private double ThresholdMean = 0;//10; //Threshold confidence value
    //init
    private SWAT_BMPs_NSGA2Handler nsga2Handle; //Handles the NSGA2 Search
    private int hdmGenerationId = 0;//generationId
    private int sdmGenerationId = 0;//generationId
    private int session_type = 0;//0- interactive, 1- simulated
    private boolean global_cond = false;//continue the search unless the global cond. satisfied
    private boolean agent1 = true; //Agent1 will run
    private boolean agent2 = false;//Agent2 will run when true
    private int global_sessionId = 0;
    private int local_sessionId = 0;
    //Design Paramters
    private int[] chosenFF;
    private int[] chosenBMPs;
    private int[] regionSubbasinId;
    private int[] tenure_regionSubbasinId;
    private int tenure_type = 1;
    private int[] localSubbasinLoc = null;
    //other
    private int bmpId = 0; //use to find the correct BMP
    private boolean useLocalPreference = IGAMI2Main.localPrefs;
    private boolean useANFIS = false;
    LinkedList<IndividualDesign> resData =null;
    //learning and optimization type_optimize settings
    private String type_optimize = "NSGA"; //Type of Optimzation used
    private String type_learn = "NeuralNetwork"; //Type of Learning used
    private Integer learningType;
    private Integer optimizationType;
    private HdmarchiveChildrenId ResumeSearchInfo = null;
    private boolean noIntrospection = false; //use to decide if second introspection needed after resuming the search
    private boolean iFirstIntro = true; //first introspection is needed or not
    private boolean dumpData = false; //save the data to files when the search finishes
   

    public MixedInitiativeManager(IndividualDesignManager idm, int[] chosenFF, int[] chosenBMPs, int UserId, int[] regionSubbasinId, int[] tenure_regionSubbasinId) {      
        this.idm = idm;
        this.dbm = idm.dbm;
        this.UserId = UserId;
        this.chosenFF = chosenFF;
        this.chosenBMPs = chosenBMPs;
        this.tenure_regionSubbasinId = tenure_regionSubbasinId;
        this.regionSubbasinId = regionSubbasinId;
        im = new IntrospectionManager(idm);
        om = new OptimizationManager(idm);
        setLocalPreferences();//set the local BMP ids
        sdmm = new SDMManager(idm, statm, chosenFF, localSubbasinLoc, useLocalPreference, chosenBMPs, useANFIS);
        this.statm = new StatisticsManager(dbm);
        //sdmm.createNewSDM(type_optimize);
        kendall = new MannKendall();
    }

    public void start(boolean resume) {
        //global_sessionId++;//first global condition

        //if the search is resuming from previous time
        if (resume) {
            resData = dbm.getResumeIndv();
            if(resData==null)
            {
                iFirstIntro=false;
                resume = false;
            }
            else
            {
                //load the necessary search resume data
                IndividualDesign in0 = resData.get(0);
                this.chosenBMPs = in0.chosenBMPs;
                this.chosenFF = in0.chosenFF;
                idm.chosenFF = this.chosenFF;
                idm.chosenBMPs = this.chosenBMPs;
                ResumeSearchInfo = in0.searcInfo;
                sdmm = new SDMManager(idm, statm, chosenFF, localSubbasinLoc, useLocalPreference, chosenBMPs, useANFIS);
                idm.setSearchId(ResumeSearchInfo.getSearchid());
                this.global_sessionId = ResumeSearchInfo.getGlobalSessionid();
                this.local_sessionId = ResumeSearchInfo.getLocalSessionid();
                this.session_type = ResumeSearchInfo.getSessionType();
                this.hdmGenerationId = ResumeSearchInfo.getSessionid();
                idm.introspectionNo = local_sessionId + 1;//Human Search Based Introspection is +1.
                if (session_type == 1) //SDM search
                {
                    sdmGenerationId = hdmGenerationId;
                    agent1 = false;//use HDM
                    agent2 = true;
                }
            }
     }
        
        dbm.doLocalCleanUp(UserId); //pre search cleanup for current user
        
        nsga2Handle = new SWAT_BMPs_NSGA2Handler(UserId,regionSubbasinId, MUTATION_PROBABILITY, CROSSOVER_PROBABILITY, HDMPopulation, chosenBMPs, chosenFF, tenure_regionSubbasinId);
        nsga2Handle.initNSGA2();
        idm.nsga2Handle = nsga2Handle;
        idm.nsga2 = nsga2Handle.getNSGA2Instace();

        bmpId = getBMPID(chosenBMPs);

        //the very first introspection
        if (!resume && iFirstIntro) {
            LinkedList<IndividualDesign> resData = dbm.getInitCBMIndv(bmpId, tenure_type, chosenFF);
            im.doIntrospection("iFistIntrospection", resData, global_sessionId, chosenFF);
            local_sessionId = 0;
        }

        VTimer time1 = new VTimer();
        time1.startTimer();

        while (global_sessionId < NoOfglobalSessions) {
            //Anget I
            if (agent1) {
                VTimer time2 = new VTimer();
                time2.startTimer();

                //Inject the start population from the CBM
                int newIndvNeeded = ((int) (HDMPopulation * (100 - percentCBM) / 400)) * 4; //100
                int CBMIndvNeeded = HDMPopulation - newIndvNeeded;
                nsga2Handle = new SWAT_BMPs_NSGA2Handler(UserId,regionSubbasinId, MUTATION_PROBABILITY, CROSSOVER_PROBABILITY, HDMPopulation, chosenBMPs, chosenFF, tenure_regionSubbasinId);
                nsga2Handle.initNSGA2();
                idm.nsga2Handle = nsga2Handle;
                idm.nsga2 = nsga2Handle.getNSGA2Instace();
                idm.global_sessionId = global_sessionId;
                idm.session_type_search = session_type;
                idm.local_sessionId = local_sessionId;

                try {
                    evaluator = new PopulationEvaluation(idm, true, sdmm);//true to take human feedback also
                    idm.nsga2.setPopulationEvaluator(evaluator); //set a new Population Evaluator Instance

                    if (!resume) { //start a new search
                        session_type = 0;//HDM interactive session

                        LinkedList<IndividualDesign> restartData = dbm.getCBMIndv(UserId, 0, global_sessionId, chosenFF);
                        
                        hdmGenerationId = 0;
                        nsga2Handle.createUsingCBM(chosenFF, UserId, restartData, CBMIndvNeeded, newIndvNeeded);
                        idm.IndvPopulation = nsga2Handle.startPopulation;

                        idm.sessionId = hdmGenerationId;
                        idm.IndvPopulation = idm.nsga2.EvaluateGeneration(idm.IndvPopulation);
                        idm.saveArchive();
                        statm.doEvaluationConfidence(idm.IndvPopulation);
                        this.statm.UserStatKendal(global_sessionId, local_sessionId,hdmGenerationId);
                        
                    }
                    else //resume the old search
                    {
                        //Generation between 0 and < noOfGenerations
                        if (session_type == 0 && this.hdmGenerationId >= 0 && this.hdmGenerationId <= noHDMGenerations) {
                            idm.TotalIndvs = nsga2Handle.generateIndividual(resData, chosenFF, chosenBMPs, this.UserId);
                            //select the Indv for the last generations
                            int strtId = idm.getStartIndvId() + this.local_sessionId * ((noHDMGenerations + 1) * this.HDMPopulation) + this.hdmGenerationId * this.HDMPopulation; //Indv startId of the last generation
                            LinkedList<Individual> indvlst = new LinkedList(); //Indv for current generations
                            for (int i = 0; i < idm.TotalIndvs.size(); i++) {
                                Individual in = idm.TotalIndvs.get(i);
                                if (in.IndvId >= strtId) {
                                    indvlst.add(in);
                                }
                            }
                            int newStrtId = indvlst.getLast().IndvId + 1;//for next set if Indvs
                            idm.setStartIndvId(newStrtId);
                            idm.sessionId = hdmGenerationId;
                            idm.introspectionNo = local_sessionId + 1;//Human Search Based Introspection is +1.
                            if (indvlst.size() != HDMPopulation) {
                                System.out.println("Error in Resuming Search for User " + this.UserId);
                            }
                            idm.IndvPopulation = indvlst;
                            idm.HDMPopulation_child = indvlst;
                            
                            //load Kendal Stats
                            int kendalVal = statm.loadKendalStats(this.kendall, this.global_sessionId, this.local_sessionId, this.hdmGenerationId);
                            if (kendalVal == (hdmGenerationId + 2)) //if sufficient no of introspection sessions already done.
                            {
                                noIntrospection = true;
                            }
                            //load previously saved SDMs 
                            try{                                                        
                                    sdmm.loadPrevSDMs();
                            }catch(Exception ex)
                            {
                                ex.printStackTrace();
                            }                           
                        }
                        //make resume false for next local session
                        resume = false;
                        System.out.println("Search Resuming for User "+this.UserId +" with Ids (G:L:S) "+global_sessionId +":" + local_sessionId + ":"+idm.sessionId+" Introspection No "+idm.introspectionNo);
                    }                
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                //Run the GA Loop
                while (hdmGenerationId < noHDMGenerations)
                {
                    hdmGenerationId++;//count no of generations, increment in beginning, because 0 is already used for First Parents
                    if (hdmGenerationId == noHDMGenerations) //last feedback session
                    {
                        idm.nowait = 1; //stop the interface from waiting, not implemented yet
                    }
                    //Optimization
                    idm.sessionId = hdmGenerationId;
                    om.optimize(type_optimize); //only one generation                 
                    statm.doEvaluationConfidence(idm.IndvPopulation);
                    this.statm.UserStatKendal(global_sessionId, local_sessionId,hdmGenerationId);
                    idm.saveArchive();
                    noIntrospection = false; //second introspection has to be done
                }
                //do the second introspection
                if (!noIntrospection) {
                    //save the last Pareto front of the nondominated population
                    idm.addLastPareto(idm.IndvPopulation);
                    idm.saveBestIndv(); //save the best indv from the last pareto               
                    im.doIntrospection("iSecondIntrospection", null, global_sessionId, chosenFF);
                    statm.doEvaluationConfidence(idm.iFinalIndvs);
                    this.statm.UserStatKendal(global_sessionId, local_sessionId, hdmGenerationId + 1);
                    decideTraining();//just one time, modify it to give more training in future
                    //decide the next initiative
                }
                
                nextInitiative(); //check the next initiative
                
                idm.reInitIndv();//reset the design handler for next session
                time2.endTimer();
                System.out.println("\n HDM Took time " + time2.getTimeHHMMSS());
                local_sessionId++;//increment for the next HDM session

            } 
            
            else if (agent2) //Run the Automated Search
            {
                System.out.println("\n SDM Took over the User " + UserId);

                if(resume)//use to relearn the data when resuming
                {
                    relernNN();
                }
                if(!resume)
                    idm.informAutomatedSearch(); //inform the user to leave as the automated search is started

                session_type = 1;//SDM Non-interactive session
                idm.global_sessionId = global_sessionId;
                idm.session_type_search = session_type;
                idm.local_sessionId = local_sessionId;

                if(!resume)
                   sdmGenerationId = 0;
                idm.sessionId = sdmGenerationId;
                VTimer time2 = new VTimer();
                time2.startTimer();
                int POPULATION_SIZE = SDMPopulation;
                int newIndvNeeded = ((int) (SDMPopulation * (100 - percentCBM) / 400)) * 4; //100
                int CBMIndvNeeded = SDMPopulation - newIndvNeeded;

                nsga2Handle = new SWAT_BMPs_NSGA2Handler(UserId,regionSubbasinId, MUTATION_PROBABILITY, CROSSOVER_PROBABILITY, POPULATION_SIZE, chosenBMPs, chosenFF, tenure_regionSubbasinId);
                nsga2Handle.initNSGA2();
                idm.nsga2Handle = nsga2Handle;
                idm.nsga2 = nsga2Handle.getNSGA2Instace();

                try {

                    if(!resume)
                    {
                        LinkedList<IndividualDesign> restartData = dbm.getCBMIndv(UserId, 0, global_sessionId, chosenFF); //use all the CBM
                        nsga2Handle.createUsingCBM(chosenFF, UserId, restartData, CBMIndvNeeded, newIndvNeeded);
                        idm.nsga2 = nsga2Handle.getNSGA2Instace();
                        evaluator = new PopulationEvaluation(idm, false, sdmm);//SDM gives feedback
                        idm.nsga2.setPopulationEvaluator(evaluator);
                        idm.IndvPopulation = nsga2Handle.startPopulation;
                        idm.IndvPopulation = idm.nsga2.EvaluateGeneration(idm.IndvPopulation);
                        idm.saveArchive();
                    }
                    else //resming the search
                    {
                        evaluator = new PopulationEvaluation(idm, false, sdmm);//SDM gives feedback
                        idm.nsga2.setPopulationEvaluator(evaluator);
                        
                        //Generation between 0 and < noOfGenerations
                        if (session_type == 1 && this.sdmGenerationId >= 0 && this.sdmGenerationId <= noSDMGenerations) {
                            idm.TotalIndvs = nsga2Handle.generateIndividual(resData, chosenFF, chosenBMPs, this.UserId);
                            //select the Indv for the last generations
                            //int strtId = idm.getStartIndvId() + this.local_sessionId * ((noHDMGenerations + 1) * this.HDMPopulation) + this.hdmGenerationId * this.HDMPopulation; //Indv startId of the last generation
                            int strtId = idm.TotalIndvs.getLast().IndvId - SDMPopulation;
                            
                            LinkedList<Individual> indvlst = new LinkedList(); //Indv for current generations
                            for (int i = 0; i < idm.TotalIndvs.size(); i++) {
                                Individual in = idm.TotalIndvs.get(i);
                                if (in.IndvId > strtId) {
                                    indvlst.add(in);
                                }
                            }
                            int newStrtId = indvlst.getLast().IndvId + 1;//for next set if Indvs
                            idm.setStartIndvId(newStrtId);
                            idm.sessionId = sdmGenerationId;
                            idm.introspectionNo = 4;//local_sessionId + 1;//always be 4, the last
                            if (indvlst.size() != SDMPopulation) {
                                System.out.println("Error in Resuming Search for User " + this.UserId);
                            }
                            idm.IndvPopulation = indvlst;
                            idm.HDMPopulation_child = indvlst;
                            //clear TotalIndvs
                            idm.TotalIndvs = new LinkedList(); //make it free
                            //load previously saved SDMs 
                            try
                            {
                                sdmm.loadPrevSDMs();//one less model for current model
                            }catch(Exception ex)
                            {
                                ex.printStackTrace();
                            }
                        }
                        //make resume false for next HS session
                        resume = false;
                        System.out.println("Search Resuming for User "+this.UserId +" with Ids (G:L:S) "+global_sessionId +":" + local_sessionId + ":"+idm.sessionId+" Introspection No "+idm.introspectionNo);
                    }
                    
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                //Loop the GA search
                for (int i = sdmGenerationId; i < noSDMGenerations; i++) {
                    //Optimization
                    sdmGenerationId++;
                    idm.sessionId = sdmGenerationId;
                    om.optimize(type_optimize);
                    idm.saveArchive();                  
                }                
                agent1 = true;//use HDM
                agent2 = false;
                global_sessionId++;//later global session
                //inform the visualization about last introspection
                idm.stop = 1;
                idm.session_type_search=0;
                im.doIntrospection("SDMfinalIntrospection", null, global_sessionId, chosenFF);
                local_sessionId = 0;//reset the local_sessionId for new global session of Agent1
            }
        }
        //idm.informSearchFinished();
        dbm.doLocalCleanUp(UserId); //for next search
        //System.out.println("Finished search for User "+UserId);
        if(dumpData)
        {
            idm.dumpData();
        }
    }

    private boolean decideTraining() {
        boolean res = true;
        //no more training
        double SDMErrorThreshold = 100;

        //If testing error is less than some threshold then continue else do more training

        //double sdmError = sdmm.test(); //did the test
        double sdmError = 0;
        //if(!useLocalPreference) //Entire watershed
        // sdmError = sdmm.trainAndTest(); //do the train and test
        //else    //at local subbasin as well
        sdmError = sdmm.trainAndTestNormalizedMultiplModels(); //do the train and test
        //send the data to userinterface
        if (sdmError <= SDMErrorThreshold) {
            res = false; //no more training
        } else {
            res = true; //give more training
        }
        return res;
    }

    private boolean nextInitiative() {
        boolean res = true;

        double mean = statm.mean; //mean of current feedback session
        double[] stddev = statm.getStddev();
        statm.resetStddev();//reset the stdv for next test

        //I Condition  
        if (mean >= ThresholdMean) {
            try {
                //check the II and III conditions using Kendall
                boolean ken = kendall.doMannKendallTest(stddev);

                int hdm = 1; //true, i.e., Human is selected for next initiative
                if (!ken) {
                    hdm = 0; //SDM is selected for next initiative
                }                //If Kendall test is false then use SDM

                //VIDYA doing this to get fixed number of Models, temporarily
                ken = true; //forefully not allowing the search to move

                if ((local_sessionId>=maxHDM) || !ken) {
                    //Start SDM
                    agent1 = false;
                    agent2 = true; //use SDM  
                    maxHDM = 2;//reset for next session
                }
                //save the  kendall stats of the current search
                dbm.saveKendallStats(UserId, global_sessionId, local_sessionId, kendall.S, kendall.Z, hdm);

            } catch (Exception ex) {
                ex.printStackTrace();
                Logger.getLogger(MixedInitiativeManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            //else use HDM   
        }
        hdmGenerationId = 0;//reset the generation number for new session
        return res;
    }

    private int getBMPID(int[] chosen) {

        int res = 0;
        //binary to decimal conversion
        if (chosen[7] == 1) {
            chosen[6] = 1;//consider the 7th
        }
        for (int i = 0; i < chosen.length - 3; i++) {
            if (chosen[6 - i] == 1) {
                res = (int) (res + 1 * Math.pow(2, (i)));
            }
        }
        chosen[6] = 0;//reset to 0
        return res;
    }

    private void setLocalPreferences() {

        UserData usr = dbm.getUserInfo(this.UserId);
        if (usr != null) {
            int[] localSubbasinIds = usr.localSubbasin;

            if (localSubbasinIds.length > 0) {
                localSubbasinLoc = new int[localSubbasinIds.length];
                //find the position of local subbasins
                for (int i = 0; i < localSubbasinIds.length; i++) {
                    for (int j = 0; j < regionSubbasinId.length; j++) {
                        if (localSubbasinIds[i] == regionSubbasinId[j]) {
                            localSubbasinLoc[i] = j;
                            break;
                        }
                    }
                }
            }
            idm.setUserInfo(usr.FName, usr.LName, usr.email, usr.gender);
        } else {
            idm.setUserInfo("Admin", "", IGAMI2Main.admin_Email, "");
            useANFIS = false;  //consider ANFIS as well
        }
        setSearchParameters();
    }

    private void setSearchParameters() {

        //load system default properties
        List obSys = dbm.getHQLManager().executeHQLQuery("from NewuserParamters where USERID=0"); //user 0 is the system user
        NewuserParamters paramsSys = null;
        if (obSys != null) {
            paramsSys = (NewuserParamters) obSys.get(0);
        } else {
            System.out.println("User System Properties missing");
        }
        //modify the settings of the search parameter based on user specification
        List ob = null;

        try {
            ob = dbm.getHQLManager().executeHQLQuery("from NewuserParamters where USERID=" + UserId);
            if (ob.size() > 0) {
                NewuserParamters params = (NewuserParamters) ob.get(0);
                Integer val = 0;
                if ((val = params.getGlobalSessionSize()) != null) {
                    NoOfglobalSessions = val;
                } else {
                    this.NoOfglobalSessions = paramsSys.getGlobalSessionSize();
                }

                if ((val = params.getHdmGeneration()) != null) {
                    this.noHDMGenerations = val;
                } else {
                    this.noHDMGenerations = paramsSys.getHdmGeneration();
                }

                if ((val = params.getHdmPopulationSize()) != null) {
                    this.HDMPopulation = val;
                } else {
                    this.HDMPopulation = paramsSys.getHdmPopulationSize();
                }

                if ((val = params.getLearningType()) != null) {
                    this.learningType = val;
                } else {
                    this.learningType = paramsSys.getLearningType();
                }

                if ((val = params.getOptimizationType()) != null) {
                    this.optimizationType = val;
                } else {
                    this.optimizationType = paramsSys.getOptimizationType();
                }

                if ((val = params.getSdmGeneration()) != null) {
                    this.noSDMGenerations = val;
                } else {
                    this.noSDMGenerations = paramsSys.getSdmGeneration();
                }

                if ((val = params.getSdmPopulationSize()) != null) {
                    this.SDMPopulation = val;
                } else {
                    this.SDMPopulation = paramsSys.getSdmPopulationSize();
                }
                Double valdou = 0.0;
                if ((valdou = params.getGaMutationProbability()) != null) {
                    this.MUTATION_PROBABILITY = valdou;
                } else {
                    this.MUTATION_PROBABILITY = paramsSys.getGaMutationProbability();
                }

                if ((valdou = params.getGaCrossoverProbability()) != null) {
                    this.CROSSOVER_PROBABILITY = valdou;
                } else {
                    this.CROSSOVER_PROBABILITY = paramsSys.getGaCrossoverProbability();
                }
            }
            else {
                //use system default parameters
                this.NoOfglobalSessions = paramsSys.getGlobalSessionSize();
                this.noHDMGenerations = paramsSys.getHdmGeneration();
                this.HDMPopulation = paramsSys.getHdmPopulationSize();
                this.learningType = paramsSys.getLearningType();
                this.optimizationType = paramsSys.getOptimizationType();
                this.noSDMGenerations = paramsSys.getSdmGeneration();
                this.SDMPopulation = paramsSys.getSdmPopulationSize();
                this.MUTATION_PROBABILITY = paramsSys.getGaMutationProbability();
                this.CROSSOVER_PROBABILITY = paramsSys.getGaCrossoverProbability();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void relernNN() {
        sdmm.relearnNN();
    }
}
