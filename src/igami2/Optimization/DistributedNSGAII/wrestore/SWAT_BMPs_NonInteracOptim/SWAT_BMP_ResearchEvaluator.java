/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.Optimization.DistributedNSGAII.wrestore.SWAT_BMPs_NonInteracOptim;

import igami2.DataBase.DBManager;
import igami2.DataBase.IndividualDesign;
import igami2.DataBase.hibernateconfig.NewuserParamters;
import igami2.DistributedSystem.MasterComputer.HPCController;
import igami2.DistributedSystem.MasterComputer.PopulationEvaluation;
import igami2.DistributedSystem.VTimer;
import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.NSGA2;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MBS-Admin
 */
public class SWAT_BMP_ResearchEvaluator {
    
    private SWAT_BMPs_NSGA2Handler nsga2Handle;
    private double MUTATION_PROBABILITY = 0.05;
    private double CROSSOVER_PROBABILITY = 0.9;
    private int HDMPopulation = 20;
    private int noHDMGenerations = 4;
    private int[] chosenFF;// = new int[]{1, 1, 0, 0, 0, 0, 1}; //This tells us which BMPs are we simulating in this optimization problem.
    private int[] chosenBMPs;// = new int[] {1,1,1,1,1,1,0,1,1,0};   
    private int[] regionSubbasinId;
    private int[] tenure_regionSubbasinId;
    private int UserId;
    private DBManager dbm;
    private LinkedList<Individual> startPopulation;
    
    private PopulationEvaluation popEvaluator;
    private NSGA2 nsga2;
    private boolean restart = false;
    private int BMP = 211;
    private boolean nondomsorting = false;
    
    public SWAT_BMP_ResearchEvaluator(int UserId, DBManager dbm, int[]chosenBMPs, int[] chosenFF,int[] regionSubbasinId, int[] tenure_regionSubbasinId)
    {
        this.UserId = UserId;
        this.dbm = dbm;
        this.chosenBMPs = chosenBMPs;
        this.chosenFF = chosenFF;
        this.regionSubbasinId = regionSubbasinId;
        this.tenure_regionSubbasinId = tenure_regionSubbasinId;
        popEvaluator = new PopulationEvaluation(dbm,0);
    }
    
    public int evaluatePopulation()
    {
        setSearchParameters();
        nsga2Handle = new SWAT_BMPs_NSGA2Handler(UserId,regionSubbasinId, MUTATION_PROBABILITY, CROSSOVER_PROBABILITY, HDMPopulation, chosenBMPs, chosenFF, tenure_regionSubbasinId);
        nsga2Handle.initNSGA2();
        nsga2 = nsga2Handle.getNSGA2Instace();
        nsga2.setPopulationEvaluator(popEvaluator);
        nsga2.setResearchEvaluation(true); //use to evaluate research
        try {
            System.out.println("Running Generation 0");
            if(restart)
            {
                String file = "../SWAT/cbmtenure.csv";
                nsga2Handle.createRestartIndividuals(file);
                startPopulation = nsga2Handle.startPopulation; 
            }
            else if(nondomsorting)
             {
                    //dbm.searchId = 29;
                    LinkedList<IndividualDesign> ind = dbm.getCBMIndv(1, 0, 0, chosenFF);
                    startPopulation = nsga2Handle.generateIndividual(ind, chosenFF, chosenBMPs, UserId);
                    startPopulation = nsga2.getBestIndividuals(startPopulation);
                    dbm.saveCBM(UserId,startPopulation,0, 0, 0, 51, 0);
                    return 0;
            }
            else
            {
                nsga2Handle.CreateNewSet();
                startPopulation = nsga2Handle.startPopulation;
            }
            //Initial GA
            VTimer vt = new VTimer();
            vt.startTimer();
            startPopulation = popEvaluator.evaluateIndividualOnceSystem(startPopulation);
            /*
            for (int i = 0; i < startPopulation.size(); i++) {                
                startPopulation.get(i).nsga2 = nsga2;
            }
            * 
            */
            if(restart)
            {
                 dbm.saveCBM(UserId,startPopulation,0, 0, 0, 0, 0);
                //dbm.saveArchive(startPopulation,startPopulation, 0, 0, 0, 1);
                //dbm.saveCBMInitial(startPopulation, 1, BMP);   
                 return 0;
            }
            //comment when do restart and cbm initial
            vt.endTimer();
            vt.printTime("m");
            dbm.saveArchive(UserId,startPopulation,null,0, 0, 0, 0, 0);
            //loop for all generations
            int i=0;
            for(i=1;i<=noHDMGenerations;i++)
            {
                System.out.println("Running Generation "+i);
                vt.startTimer();
                startPopulation = nsga2.evolveOneGen(startPopulation);
                vt.endTimer();
                vt.printTime("m");
                //dbm.saveCBM(startPopulation, 0, 0, i, 0);
            }
            startPopulation = nsga2.getBestIndividuals(startPopulation);
            dbm.saveCBM(UserId,startPopulation,0, 0, 0, noHDMGenerations, 0);
            //nsga2Handle.printBest_SWAT_BMPs_Individuals(startPopulation);
            
        } catch (IOException ex) {
            Logger.getLogger(SWAT_BMP_ResearchEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return 0;
    }
    private void setSearchParameters() {

        //modify the settings of the search parameter based on user specification
        List ob = null;

        try {
            ob = dbm.getHQLManager().executeHQLQuery("from NewuserParamters where USERID=" + UserId);
            if (ob.size() > 0) {
                NewuserParamters params = (NewuserParamters) ob.get(0);
                Integer val = 0;
                

                if ((val = params.getHdmGeneration()) != null) {
                    this.noHDMGenerations = val;
                }

                if ((val = params.getHdmPopulationSize()) != null) {
                    this.HDMPopulation = val;
                }
                
                Double valdou = 0.0;
                if ((valdou = params.getGaMutationProbability()) != null) {
                    this.MUTATION_PROBABILITY = valdou;
                }

                if ((valdou = params.getGaCrossoverProbability()) != null) {
                    this.CROSSOVER_PROBABILITY = valdou;
                }
            }
            if(this.noHDMGenerations==0) //restart indv for parent only
            {
                restart=true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    
}
