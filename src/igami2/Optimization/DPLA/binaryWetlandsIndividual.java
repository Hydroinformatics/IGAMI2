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

package igami2.Optimization.DPLA;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements an individual representing a possible set of decision variables for use in the
 * multi-objective genetic algorithm NSGA-II.
 * 
 * @author Meghna Babbar-Sebens, IUPUI, Dec 2010.
 * @version 1.0
 */
public class binaryWetlandsIndividual implements Serializable {


	// This consists of the SWAT IDs of sub-basins that are being optimized for wetland installation
	// The variable is specific to the application (mbabbars). 
   
    
   
   public int[] regionSubbasinId;
   // This consists of the actual assignment of wetlands in the sub-basins. If assignment[i] = 1 then regionSubbasinId[i] will have a wetland installed.
   // The variable is specific to the application (mbabbars)
   public int[] assignments;
   
   public String base = "../SWAT/swat0/";//default
   //public FitnessFunction[] fitnessFunctions;
   public final double sumflow = 10981;
    private final FitnessFunction[] fitnessFunctions;
    private final double[] fitnessValues;
   /**
    * Constructor.
    * 
    * @param nsga2 NSGA-II instance
    * @param int[] regionSubbasinId (These are the sub-basins where the wetlands are being installed
    * @param int[] assignments (These are the assignments of wetlands in the sub-basins with IDs = regionSubbasinId)
    * params will change for each application (mbabbars)
    */
   public binaryWetlandsIndividual(int[] regionSubbasinId, int[] assignments) throws IOException{
      
      
      if (regionSubbasinId == null) {
         throw new IllegalArgumentException("'regionSubbasinId' must not be null.");
      }
      if (assignments == null) {
          throw new IllegalArgumentException("'assignments' must not be null.");
       }     
      this.regionSubbasinId = regionSubbasinId;
      this.assignments = assignments;
      binaryWetlandsAreaFitnessFunction fitnessFunction0 = new binaryWetlandsAreaFitnessFunction();
      //System.out.println ("area fitness functions created");
      binaryWetlandsNetflowFitnessFunction fitnessFunction1 = new binaryWetlandsNetflowFitnessFunction(sumflow);
      
      fitnessFunctions =  new FitnessFunction[2];
      fitnessFunctions[0] = (FitnessFunction) fitnessFunction0;
      fitnessFunctions[1] = (FitnessFunction) fitnessFunction1;
      // set fitness values
      fitnessValues = new double[2];
      /*
      for (int i = 0; i < fitnessValues.length; i++) {
    	 //System.out.println ("fitnessValues i = "+ i);
         fitnessValues[i] = nsga2.getNSGA2Configuration().getFitnessFunction(i).evaluate(this);
      }
       * 
       */
   }
   
   /**
    * Gets the regionSubbasinId array.
    * Its definition is specific to the application (mbabbars)
    * @return regionSubbasinId
    */
   public int[] getRegionSubbasinIds() {
      return regionSubbasinId.clone();
   }
   
   /**
    * Gets the event assignments (for all sub-basins) in the same order as in the returned array
    * of method {@link #getRegistrations()}.
    * Its definition is specific to the application (mbabbars)
    * @return event assignments
    */
   public int[] getAssignments() {
      return assignments.clone();
   }
   
   /**
    * Gets this individual's fitness value for the index-th objective.
    * 
    * @param index index
    * @return fitness value for the index-th objective
    * @throws IndexOutOfBoundsException if the index is out of bounds
    */
   public double getFitnessValue(int index) throws IndexOutOfBoundsException {
      return fitnessValues[index];
   }
   
   
   public void evaluateIndividual(int idx) {
        String base1 = base + idx+"/";
        
        for (int i = 0; i < fitnessValues.length; i++) {
            try {
                fitnessValues[i] = fitnessFunctions[i].evaluate(base1);
            } catch (IOException ex) {
                Logger.getLogger(binaryWetlandsIndividual.class.getName()).log(Level.SEVERE, null, ex);
            }
            
         }
    }
}