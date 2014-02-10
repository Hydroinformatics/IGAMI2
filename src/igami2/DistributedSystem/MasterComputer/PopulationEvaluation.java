/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.MasterComputer;

import igami2.DataBase.DBManager;
import igami2.DataBase.IndividualDesignManager;
import igami2.IGAMI2Main;
import igami2.MixedInitiative.SDMManager;
import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.NSGA2;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author VIDYA
 */
public class PopulationEvaluation {
    
    int AvgEvalTime = 60000;
    int waitTime = 5000;
    IndividualDesignManager idm;
    boolean feedback; //false for Automated
    private SDMManager sdmm;
    private int StartIndvId=0;//assign IndvIds
    private int generationId=0;
    private DBManager dbm;
    
    public PopulationEvaluation(IndividualDesignManager idm, boolean feedback, SDMManager sdmm)
    {
        this.idm = idm;
        this.feedback = feedback;
        this.sdmm = sdmm;
    }
    
    public PopulationEvaluation()
    {
        
    }
    
    public PopulationEvaluation(DBManager dbm, int GenerationId)
    {
        this.dbm = dbm;     
        this.generationId = GenerationId;
    }
    
    //Use by IGAMI2 only
    public LinkedList<Individual> evaluateGen(LinkedList<Individual> indv)
    {
        LinkedList<Individual> res = null;
        int userId = indv.get(0).UserId;
        int systemId = Integer.parseInt(""+IGAMI2Main.UserSystemId.get(userId)); //get the systemId of this user
        //NSGA2 nsga2 = indv.get(0).nsga2;
        idm.dbm.setUserActive(userId);
        try {
            HPCController.ArrayIndvList.get(systemId).clear();// = new LinkedList();//for next use
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        HPCController.sendEvaluationUsingQueue(indv); //block untill released by the master

        try {         
            System.out.println("Sleeping");
            Thread.sleep(AvgEvalTime); //sleep the user till the evaluation is done
         while(HPCController.ArrayIndvList.get(systemId).size()==0)// do busy loop
        {
            Thread.sleep(5000); //sleep the user till the evaluation is done
        }
         idm.IndvPopulation = new LinkedList<Individual>();
         idm.IndvPopulation = (LinkedList) HPCController.ArrayIndvList.get(systemId).clone();
         
         HPCController.ArrayIndvList.get(systemId).clear();//for next use
         
         
        } catch (InterruptedException ex) {
            Logger.getLogger(PopulationEvaluation.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
        //set the search inactive
        idm.dbm.setUserInActive(userId);
        
        
        
        if(feedback)//If true then take feedback from Human
        {
            idm.takeFeedback();            
        }
        else //do the SDM Rating
        {
            idm.generateID();
            sdmm.doSDMRating();
            idm.HDMPopulation_child = (LinkedList<Individual>) idm.IndvPopulation;
        }
        
        res = idm.IndvPopulation;
        //restore the NSGA2
        //Evaluate the last fitness functions for all
        for(int i=0;i<res.size();i++)
         {
             Individual in = res.get(i);             
             in.nsga2 = idm.nsga2;
             in.evaluateRating();//Set the Value of Rating Fitness function as Negative of Rating
             res.set(i, in);
         }       
        
        return res;
    }
    
    /*
     * Used by Initial Case Evaluator not IGAMI2
     */
    public LinkedList<Individual> evaluateIndividualOnce(LinkedList<Individual> indv)
    {
        LinkedList<Individual> res = new LinkedList<Individual>();
        int userId = indv.get(0).UserId;
        //NSGA2 nsga2 = indv.get(0).nsga2;
        
        HPCController.sendEvaluationUsingQueue(indv);
        
        try {
            
            System.out.println("Sleeping");
            Thread.sleep(AvgEvalTime); //sleep the user till the evaluation is done
         while(HPCController.ArrayIndvList.get(userId).size()==0)// do busy loop
        {
            Thread.sleep(5000); //sleep the user till the evaluation is done
        }
         res = (LinkedList) HPCController.ArrayIndvList.get(userId).clone();
         
         HPCController.ArrayIndvList.get(userId).clear();//for next use
         
         
        } catch (InterruptedException ex) {
            Logger.getLogger(PopulationEvaluation.class.getName()).log(Level.SEVERE, null, ex);
        }
     return res;   
    }
    
    public LinkedList<Individual> evaluateIndividualOnceSystem(LinkedList<Individual> indv)
    {
        LinkedList<Individual> res = new LinkedList<Individual>();
        int userId = indv.get(0).UserId;
        int systemId = Integer.parseInt(""+IGAMI2Main.UserSystemId.get(userId)); //get the systemId of this user      
        NSGA2 nsga2 = indv.get(0).nsga2;
        indv = generateID(indv);
        HPCController.sendEvaluationUsingQueue(indv);
        
        try {
            
            System.out.println("Sleeping");
            Thread.sleep(AvgEvalTime); //sleep the user till the evaluation is done
         while(HPCController.ArrayIndvList.get(systemId).size()==0)// do busy loop
        {
            Thread.sleep(5000); //sleep the user till the evaluation is done
        }
         res = (LinkedList) HPCController.ArrayIndvList.get(systemId).clone();
         
         HPCController.ArrayIndvList.get(systemId).clear();//for next use
         
         for(Individual in:res)
         {
             in.nsga2 = nsga2;//restore NSGA2
         }        
         //generateIds       
         for(Individual in:res)
         {
             in.IndvId = StartIndvId++;
             //startPopulation.get(i).nsga2 = nsga2;
         }
         dbm.saveArchive(userId,res, null,0, 0, 0,generationId, 1);
         this.generationId++;
        } catch (InterruptedException ex) {
            Logger.getLogger(PopulationEvaluation.class.getName()).log(Level.SEVERE, null, ex);
        }
     return res;   
    }
    public LinkedList<Individual> generateID(LinkedList<Individual> startPopulation) { 
            for (int i = 0; i < startPopulation.size(); i++) {
                //startPopulation.get(i).UserId = UserId;                
                startPopulation.get(i).IndvId = i;
            }
            return startPopulation;
    }
}
