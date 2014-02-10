/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2;

import igami2.DataBase.DBManager;
import igami2.DistributedSystem.MasterComputer.HPCController;
import igami2.DistributedSystem.MasterComputer.PopulationEvaluation;
import igami2.DistributedSystem.VTimer;
import igami2.Optimization.DistributedNSGAII.wrestore.SWAT_BMPs_NonInteracOptim.SWAT_BMPs_NSGA2Handler;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author VIDYA
 */
public class IntialCaseEvaluator implements Runnable {

    int[] chosenFF = new int[]{0, 1, 1, 1, 1, 0, 0}; //This tells us which BMPs are we simulating in this optimization problem.
    int[] chosenBMPs = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    private int adj = 3; //no of BMPs not used
    String userDIR = "../SWAT/USER/user";
    private DBManager dbm = null;
    int[] regionSubbasinId = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 71, 76, 77, 78, 80, 82, 83, 85, 86, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 110, 111, 112, 115, 117, 119, 121, 122, 123, 124, 125, 126, 127};
    private int[] tenure_regionSubbasinId = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}; //new int[regionSubbasinId.length]; 
    private double MUTATION_PROBABILITY = 0.05;  // A much higher mutation rate seems to have a negative effect!
    private double CROSSOVER_PROBABILITY = 0.9;
    private final int POPULATION_SIZE = 100;
    int tenureType = 0;
    PopulationEvaluation evaluator;
    String tenureFileName = "../SWAT/cbmtenure";
    private final int userId;
    private int bmpNos = 127;
    
    Thread t;
    

    public IntialCaseEvaluator(int userId, int tenureType) {
        this.userId = userId;
        this.tenureType = tenureType;
        evaluator = new PopulationEvaluation();
        dbm = new DBManager();
        t = new Thread(this,"Eval"+userId);
        t.start();
    }

    @Override
    public void run() {

        
        tenure_regionSubbasinId = new int[regionSubbasinId.length];
        for (int i = 0; i < regionSubbasinId.length; i++) {
            tenure_regionSubbasinId[i] = tenureType;
        }


        //for each BMP create BMP id
        //for first 127 BMPs
        //run 16 bmp by each user
        int strtBmp=24;
        int endBmp=strtBmp+1;
        //int strtBmp=userId*4+1;
        //int endBmp=strtBmp+4;//only 18 bmps per user
        //int begin = 18;
        //int strtBmp=begin+userId*10+1;
        //int endBmp=strtBmp+10;
        //if(userId==1)
            //endBmp--;
        if(userId==31)
            endBmp--; //one less for the last user
            //endBmp = endBmp+2; //extra last two BMPs for last node
        
        for (int i = strtBmp; i < endBmp; i++) {
            VTimer time2 = new VTimer();
            time2.startTimer();
            
            System.out.println("Thread"+userId + " Evaluating BMP No "+i);
            chosenBMPs = new int[]{0,0,0,0,0,0,0,0,0,0};
            int ar[] = decToBin(i);
            int zerosNeeded = chosenBMPs.length - ar.length - adj;
            int j=0;
            for(;j<zerosNeeded;j++)
                chosenBMPs[j] = 0;//intialize 0
            for(int k =0;k<ar.length;k++)
                chosenBMPs[j+k] = ar[k];
            System.out.print(i+" bmp Binary Value ");
            for(int k=0;k<chosenBMPs.length;k++)
            {
                //for the selection of wetlands
                if(k==6)
                {
                    if(chosenBMPs[k]==1) //wetlands selected
                    {
                        chosenBMPs[k]=0;//set the 7th BMP 0, since we are not using it
                        chosenBMPs[k+1]=1;//set the 8th
                        chosenBMPs[k+2]=1; //set the 9th
                    }
                }
                System.out.print(chosenBMPs[k]);
            }
            System.out.println();
            SWAT_BMPs_NSGA2Handler nsgaHandle = new SWAT_BMPs_NSGA2Handler(userId,regionSubbasinId, MUTATION_PROBABILITY, CROSSOVER_PROBABILITY, POPULATION_SIZE, chosenBMPs, chosenFF, tenure_regionSubbasinId);

            nsgaHandle.initNSGA2();
            try {
                String tnfile = tenureFileName+userId+".txt";
                nsgaHandle.createRestartIndividuals(tnfile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            LinkedList pop = nsgaHandle.startPopulation;

            pop = evaluator.evaluateIndividualOnce(pop);

            //save it to DB
            //int bmpId=i;
            int bmpid=200; //to evaluate restart indv.            
            dbm.saveCBMInitial(pop,tenureType,bmpid);
            time2.endTimer();
            System.out.println("CBM took time "+time2.getTimeHHMMSS());
            //break 1
            //break;
        }

    }

    public int[] getBinArray(int number) {
        int length = 0;
        while (number != 0) {
            number >>>= 1;
            length++;
        }
        return new int[length];
    }

    public int[] decToBin(int number) {
        int[] array = getBinArray(number);
        int k = array.length - 1;
        while (number != 0) {
            array[k--] = number & 1;
            number >>>= 1;
        }
        return array;
    }

    public static void main(String args[]) {
        
         
        try {
            HPCController con = new HPCController("");
            con.initRMIMasterComputer(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //just for tenure type 1
        int users = 1; //only one for now
        VTimer time1 = new VTimer();
        
        time1.startTimer();
        
        IntialCaseEvaluator[] ice = new IntialCaseEvaluator[users];
        for(int i=0;i<users;i++) //1 per user
        {
             ice[i] = new IntialCaseEvaluator(i,1);
            
        }
        
        for(int i=0;i<users;i++)
        {
        try {
                ice[i].t.join(); //currently running sequential, but it can be run in parallel
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
       

        
        //run SINGLE TEST
        /*
        IntialCaseEvaluator ice;
        for(int i=0;i<users;i++) //1 per user
        {
             ice = new IntialCaseEvaluator(i,1);
            try {
                ice.t.join(); //currently running sequential, but it can be run in parallel
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            
            //break 1
            break;
        }
        * 
        */
        
       
        
        time1.endTimer();

        System.out.println("Job Done Successfully");
        System.out.println("Total Time taken to finish the job is "+time1.getTimeDDHHMMSS());
        System.exit(0);

    }
}
