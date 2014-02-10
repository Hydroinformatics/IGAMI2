/* ===========================================================
 * JNSGA2: a free NSGA-II library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2006-2007, Joachim Melcher, Institut AIFB, Universitaet Karlsruhe (TH), Germany
 *
 * Project Info:  http://sourceforge.net/projects/jnsga2/
 *
 * This library is free software; you can redistribute it and/or modify it  under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package igami2.Optimization.DistributedNSGAII.wrestore.SWAT_BMPs_NonInteracOptim;

import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.*;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements an NSGA-II event listener.
 * 
 * @author Joachim Melcher, Institut AIFB, Universitaet Karlsruhe (TH), Germany
 * @version 1.0
 */
public class SWAT_BMPs_NonInterac_NSGA2Listener implements NSGA2Listener,Serializable {
private String dosOutFileLocation = new String ("../SWAT/data/SWAT_BMPs_nonInterac/");
		
    private String dosOutFileName = new String ("../SWAT/data/SWAT_BMPs_nonInterac/SWAT_BMPs_GA_");
	private int [] chosenFF;
	public DataOutputStream dosFlow;
        int UserId =0;
	/**
	  * Constructor.
	  * 
	  * @param chosenFFFlag array that has the flag s for which fitness functions are being used in the optimization.
	  */
	public SWAT_BMPs_NonInterac_NSGA2Listener(int [] chosenFFFlag, int UserId) {
            this.UserId = UserId;
        try {
            dosOutFileName = dosOutFileName+UserId;
            File f = new File(dosOutFileLocation);
            if(!f.exists())
            {
                f.mkdirs();
            }
            dosFlow = new DataOutputStream(new FileOutputStream(dosOutFileName+".out"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SWAT_BMPs_NonInterac_NSGA2Listener.class.getName()).log(Level.SEVERE, null, ex);
        }
		this.chosenFF = new int [chosenFFFlag.length];
		for (int i=0; i< chosenFFFlag.length; i++){
			chosenFF[i] = chosenFFFlag [i];
		}	
	}
	
   /**
    * Performs the specified NSGA-II event.
    * <p>
    * Every 1 generations, the best individuals found so far are printed.
    * 
    * @param nsga2event NSGA-II event
    */
   public void performNSGA2Event(NSGA2Event nsga2event) {
      if (nsga2event.getNumberGeneration() % 1 == 0) {
         System.out.println();
         System.out.println("Generation: " + nsga2event.getNumberGeneration());
         
         LinkedList<Individual> bestIndividuals = nsga2event.getBestIndividuals();
      
         LinkedList<SWAT_BMPs_Individual> bestAssignments = new LinkedList<SWAT_BMPs_Individual>();
         for (Individual individual : bestIndividuals) {
            bestAssignments.add((SWAT_BMPs_Individual)individual);
         }
      
         try {
			printBestAssignments(bestAssignments, nsga2event);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
   }
   
   /**
    * Prints the specified assignment individuals.
    * 
    * @param bestAssignments assignment individuals
    */
   private  void printBestAssignments(LinkedList<SWAT_BMPs_Individual> bestAssignments, NSGA2Event nsga2event) throws IOException{
      if (bestAssignments == null) {
         throw new IllegalArgumentException("'bestAssignments' must not be null.");
      }
      
      // sort best assignments
      SWAT_BMPs_Individual[] array =
         bestAssignments.toArray(new SWAT_BMPs_Individual[bestAssignments.size()]);
      Arrays.sort(array, new SWAT_BMPs_IndividualComparator());
          
      // print best solutions to file
      
 	  dosFlow.writeBytes("\nGeneration: " + nsga2event.getNumberGeneration());
 	  dosFlow.writeBytes("\n");
      dosFlow.writeBytes("Number of offered solutions: " + bestAssignments.size());
      dosFlow.writeBytes("\n");
      for (int i = 0; i < array.length; i++) {
     	 for (int k = 0; k < array[i].getAssignments().length; k++){
     		dosFlow.writeBytes(new Double(array[i].getAssignments()[k]).toString()+ " ");
     	 }
     	 dosFlow.writeBytes(" BMP area Fitness: " + new Double(array[i].getFitnessValue(0)).toString());
     	 dosFlow.writeBytes(" Peak Flow Fitness: " + new Double(array[i].getFitnessValue(1)).toString());
     	 dosFlow.writeBytes(" Economic Costs Fitness: " + new Double(array[i].getFitnessValue(2)).toString());
     	 dosFlow.writeBytes(" Soil Erosion Fitness: " + new Double(array[i].getFitnessValue(3)).toString());
     	 dosFlow.writeBytes(" Nitrates Fitness: " + new Double(array[i].getFitnessValue(4)).toString());
         dosFlow.writeBytes(" Pesticides Fitness: " + new Double(array[i].getFitnessValue(5)).toString());
     	 dosFlow.writeBytes("\n");
      }
      dosFlow.flush();
   }
   
   /**
    * This inner class implements a comparator for two binaryWetlands individuals.
    */
   private  class SWAT_BMPs_IndividualComparator implements Comparator<SWAT_BMPs_Individual> {
      
      /**
       * Compares the two specified individuals. First criterion is small number of
       * "total wetland area", second one is a small value of netflow when compared to the baseline flows. 
       * 
       * @param individual1 first individual
       * @param individual2 second individual
       * @return -1, 0 or 1 as the first individual is less than, equal to, or greater than the
       *         second one
       */
      public int compare(SWAT_BMPs_Individual individual1, SWAT_BMPs_Individual individual2) {
         if (individual1 == null) {
            throw new IllegalArgumentException("'individual1' must not be null.");
         }
         if (individual2 == null) {
            throw new IllegalArgumentException("'individual2' must not be null.");
         }
         
         // (1) Total BMP area (objective 0)
         if (chosenFF[0] == 1){
        	 if (individual1.getFitnessValue(0) < individual2.getFitnessValue(0)) {
        		 return -1;
        	 }
         
        	 if (individual1.getFitnessValue(0) > individual2.getFitnessValue(0)) {
        		 return 1;
        	 }
         }
         
         // (2) Peak flows (objective 1) 
         if (chosenFF[1] == 1){
             if (individual1.getFitnessValue(1) < individual2.getFitnessValue(1)) {
            	 return -1;
             }
    
             if (individual1.getFitnessValue(1) > individual2.getFitnessValue(1)) {
            	 return 1;
             }
         }
         
         // (3) Economic Costs (objective 2) 
         if (chosenFF[2] == 1){
             if (individual1.getFitnessValue(2) < individual2.getFitnessValue(2)) {
            	 return -1;
             }
    
             if (individual1.getFitnessValue(2) > individual2.getFitnessValue(2)) {
            	 return 1;
             }
         }  
         // (4) Soil Erosion (objective 3) 
         if (chosenFF[3] == 1){
             if (individual1.getFitnessValue(3) < individual2.getFitnessValue(3)) {
            	 return -1;
             }
    
             if (individual1.getFitnessValue(3) > individual2.getFitnessValue(3)) {
            	 return 1;
             }
         }
         // (5) Nitrates (objective 4) 
         if (chosenFF[4] == 1){
             if (individual1.getFitnessValue(4) < individual2.getFitnessValue(4)) {
            	 return -1;
             }
    
             if (individual1.getFitnessValue(4) > individual2.getFitnessValue(4)) {
            	 return 1;
             }
         }   
         // (6) Pesticides (objective 5) 
         if (chosenFF[5] == 1){
             if (individual1.getFitnessValue(5) < individual2.getFitnessValue(5)) {
            	 return -1;
             }
    
             if (individual1.getFitnessValue(5) > individual2.getFitnessValue(5)) {
            	 return 1;
             }
         } 
         // both individuals are equal
         return 0;
      }
   }
}