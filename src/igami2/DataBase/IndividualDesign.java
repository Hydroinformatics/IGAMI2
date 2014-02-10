/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DataBase;

import igami2.DataBase.hibernateconfig.HdmarchiveChildrenId;
import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import java.io.Serializable;

/**
 *
 * @author VIDYA
 */
public class IndividualDesign implements Serializable{
    
   public int rating=0;
   public double confidence=0;
   public double[] fitnessValues;
   public int[] regionSubbasinId;
   public int[] chosenBMPs; 
   public int [] chosenFF;
   public Individual Indv;
   public double[] assignments;
   public int IndvId;
   public double [][] subbasinsFF; // This stores the fitness function values at sub-basin scale for every sub-basin (except for omittedSubBasins).
   public int[] tenure_regionSubbasinId; // This consists of the tenure in every sub-basin that is being optimized
   public HdmarchiveChildrenId searcInfo = null;
    
    public IndividualDesign(Individual indv,int[] regionSubbasinId, int[] chosenBMPs, int [] chosenFF)
    {
        this.Indv = indv;
        this.fitnessValues = indv.fitnessValues;
        this.regionSubbasinId = regionSubbasinId;
        this.chosenBMPs = chosenBMPs;
        this.chosenFF = chosenFF;       
        this.subbasinsFF = indv.subbasinsFF;
        //this.tenure_regionSubbasinId = tenure_regionSubbasinId;
    }
    
    public IndividualDesign(Individual indv)
    {
        this.Indv = indv;
        this.IndvId = indv.IndvId;
        this.fitnessValues = indv.fitnessValues;
        this.rating = indv.rating;
        this.confidence = indv.confidence;
    }
    public IndividualDesign()
    {
        
    }
    
    public void clearIndv()//deletes the bulky individual
    {
        Indv = null;
    }
    
    public void printAssignment()
   {
       
       for(int i=0;i<assignments.length;i++)
       {
           System.out.print(assignments[i]+" ");           
       }
       System.out.println();
   }
   
   public void printFitnessValues()
   {
       //System.out.println(" Fitness Values ");
       for(int i=0;i<fitnessValues.length;i++)
       {
           System.out.print("\tF"+i+" "+fitnessValues[i]);
       }
       System.out.println();
   }
    
    
    
}
