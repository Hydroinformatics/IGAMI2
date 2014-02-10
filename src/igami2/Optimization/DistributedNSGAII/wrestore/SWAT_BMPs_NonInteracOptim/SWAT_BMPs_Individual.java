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

/**
 * This class implements an individual representing a possible set of decision variables for use in the
 * multi-objective genetic algorithm NSGA-II.
 * 
 * @author Meghna Babbar-Sebens, IUPUI, Dec 2010.
 * @version 1.0
 */
public class SWAT_BMPs_Individual extends Individual implements Serializable {

   // These variables are specific to the application (mbabbars)
   int noOfSubbasins = 130; // These are the total number of sub-basins in the SWAT model.
   int [] omittedSubBasins = new int[] {128, 129, 130};
   // NOTE: DO NOT CHANGE THE ORDER OF THESE BMPS. IF YOU ADD MORE BMPS, ADD THEM IN THE END AND UPDATE THEIR USE AND INDEX IN OTHER FILES.
   String [] BMPlookup = new String[]{"strip_cropping", "crop_rotation", "cover_crops", "filter_strips", "grassed_waterway", "conservation_tillage", "binary_wetlands", "variable_area_wetlands","variable_wetfr_wetlands","binary_ponds"}; 
   public double[] filterStrip_minmaxValues ; // first column stores minimum values and second column stores maximum values 
   public double[][] variableAreaWetlands_minmaxValues ; // first column stores minimum values and second column stores maximum values, for every sub-basin in rows
   public double[][] variableWetFrWetlands_minmaxValues ; // first column stores minimum values and second column stores maximum values, for every sub-basin in rows
   public String economicCostsExecFname,economicCostsOutputFname, erosionExecFname, erosionOutputFname, nitrateExecFname, nitrateOutputFname, pesticidesExecFname, pesticidesOutputFname; 
   
   public String base = "../SWAT/swat_dirs/swat"; // "C:/vidya/SWAT/swat";//default SWAT folder
   public String indivFname = "indiv.dat"; // This file stores the information on the decision variables and the gene for this individual.

   /**
    * Constructor.
    * 
    * @param nsga2 NSGA-II instance
    * @param int[] regionSubbasinId (These are the sub-basins where the wetlands are being installed
    * @param int[] assignments (These are the assignments of wetlands in the sub-basins with IDs = regionSubbasinId)
    * params will change for each application (mbabbars)
    */
   public SWAT_BMPs_Individual(NSGA2 nsga2, int[] regionSubbasinId, int[] tenure_regionSubbasinId, int [] chosenBMPs, int [] chosenFF, double[] filterStrip_minmaxValues, double[][] variableAreaWetlands_minmaxValues, double[][] variableWetFrWetlands_minmaxValues, String economicCostsExecFname, String economicCostsOutputFname, String erosionExecFname, String erosionOutputFname, String nitrateExecFname, String nitrateOutputFname, String pesticidesExecFname, String pesticidesOutputFname, double[] assignments) throws IOException{
      super(nsga2);
      
      if (regionSubbasinId == null) {
         throw new IllegalArgumentException("'regionSubbasinId' must not be null.");
      }
      if (tenure_regionSubbasinId == null) {
         throw new IllegalArgumentException("'tenure_regionSubbasinId' must not be null.");
      }
      if (assignments == null) {
          throw new IllegalArgumentException("'assignments' must not be null.");
       }  
      if (chosenBMPs == null) {
          throw new IllegalArgumentException("'chosenBMPs' must not be null.");
       } 
      if (chosenFF == null) {
          throw new IllegalArgumentException("'chosenFF' must not be null.");
       } 
      if (filterStrip_minmaxValues == null) {
          throw new IllegalArgumentException("'filterStrip_minmaxValues' must not be null.");
       } 
      if (variableAreaWetlands_minmaxValues == null) {
          throw new IllegalArgumentException("'variableAreaWetlands_minmaxValues' must not be null.");
       } 
      if (variableWetFrWetlands_minmaxValues == null) {
          throw new IllegalArgumentException("'variableWetFrWetlands_minmaxValues' must not be null.");
       } 
      this.regionSubbasinId = regionSubbasinId.clone();
      this.tenure_regionSubbasinId = tenure_regionSubbasinId.clone();
      this.assignments = assignments.clone();
      this.chosenBMPs = chosenBMPs.clone();
      this.chosenFF = chosenFF.clone();
      this.subbasinsFF = new double [this.noOfSubbasins-this.omittedSubBasins.length][this.chosenFF.length];
      this.filterStrip_minmaxValues = filterStrip_minmaxValues.clone();
      this.variableAreaWetlands_minmaxValues = variableAreaWetlands_minmaxValues.clone();
      this.variableWetFrWetlands_minmaxValues = variableWetFrWetlands_minmaxValues.clone();
      this.economicCostsExecFname = new String (economicCostsExecFname);
      this.economicCostsOutputFname = new String (economicCostsOutputFname); 
      this.erosionExecFname = new String (erosionExecFname);
      this.erosionOutputFname = new String (erosionOutputFname);
      this.nitrateExecFname = new String (nitrateExecFname);
      this.nitrateOutputFname = new String (nitrateOutputFname);
      this.pesticidesExecFname = new String (pesticidesExecFname);
      this.pesticidesOutputFname = new String (pesticidesOutputFname);
      
      SWAT_BMPs_AreaFitnessFunction fitnessFunction0 = new SWAT_BMPs_AreaFitnessFunction(chosenFF[0], regionSubbasinId, chosenBMPs);
      SWAT_BMPs_PeakflowFitnessFunction fitnessFunction1 = new SWAT_BMPs_PeakflowFitnessFunction(chosenFF[1]);
      SWAT_BMPs_EconomicCostsFitnessFunction fitnessFunction2= new SWAT_BMPs_EconomicCostsFitnessFunction(chosenFF[2], BMPlookup, economicCostsExecFname, economicCostsOutputFname, this.tenure_regionSubbasinId, this.regionSubbasinId) ;
      SWAT_BMPs_SoilErosionFitnessFunction fitnessFunction3= new SWAT_BMPs_SoilErosionFitnessFunction(chosenFF[3], erosionExecFname, erosionOutputFname);
      SWAT_BMPs_NitratesFitnessFunction fitnessFunction4= new SWAT_BMPs_NitratesFitnessFunction(chosenFF[4], nitrateExecFname, nitrateOutputFname);
      SWAT_BMPs_PesticidesFitnessFunction fitnessFunction5= new SWAT_BMPs_PesticidesFitnessFunction(chosenFF[5], pesticidesExecFname, pesticidesOutputFname);

      SWAT_BMP_IndividualsRatingFitnessFunction fitnessFunction6 = new SWAT_BMP_IndividualsRatingFitnessFunction();
      
      fitnessFunctions = new FitnessFunction[chosenFF.length];
      fitnessFunctions[0] = fitnessFunction0;
      fitnessFunctions[1] = fitnessFunction1;
      fitnessFunctions[2] = fitnessFunction2;
      fitnessFunctions[3] = fitnessFunction3;
      fitnessFunctions[4] = fitnessFunction4;
      fitnessFunctions[5] = fitnessFunction5;
      fitnessFunctions[6] = fitnessFunction6; 
      //set overall fitness values
      fitnessValues = new double[fitnessFunctions.length];
      
      //why init zero, when clone also have the same value
      // set subbasins Fitness values
      this.confidence = 50;
      
      for (int i = 0; i < this.noOfSubbasins-this.omittedSubBasins.length; i++) {
    	 for (int j = 0; j < this.chosenFF.length; j++) {
             subbasinsFF[i][j] = 0.0;
         }
      }
      
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
   public double[] getAssignments() {
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
   
   /**
    * Mutates this individual.
    * 
    * After mutation, the fitness values are updated.
    */
   protected void mutate() {
      boolean mutated = false;
      int currentNumBMPs = 0;
            
      for (int i = 0; i< chosenBMPs.length; i++){
    	  if (this.chosenBMPs[i] == 1){
    		//Note: String [] BMPlookup = new String[]{"strip cropping", "conservation crop rotation", "cover crops", "filter strips", "grassed waterway", "conservation tillage", "binary wetlands", "variable area wetlands","variable Wetfr wetlands","binary ponds"}; 
			  // If  "strip cropping"
			  if (i==0){
		    	  for (int j = 0; j < regionSubbasinId.length; j++) {
		    		  // search random assignment 
		    		  if (Math.random() <= nsga2.getNSGA2Configuration().getMutationProbability()) {
		    			  if (assignments[currentNumBMPs*regionSubbasinId.length+j] == 1) {
		    				  assignments[currentNumBMPs*regionSubbasinId.length+j] = 0;
		    			  } else{
		    				  assignments[currentNumBMPs*regionSubbasinId.length+j] = 1;
		    			  }
		    		  }
		    	  }  
			  } 
			  // If "conservation crop rotation"
			  else if (i==1){
		    	  for (int j = 0; j < regionSubbasinId.length; j++) {
		    		  // search random assignment 
		    		  if (Math.random() <= nsga2.getNSGA2Configuration().getMutationProbability()) {           
		    			  if (assignments[currentNumBMPs*regionSubbasinId.length+j] == 1) {
		    				  assignments[currentNumBMPs*regionSubbasinId.length+j] = 0;
		    			  } else{
		    				  assignments[currentNumBMPs*regionSubbasinId.length+j] = 1;
		    			  }
		    		  }    	
		    	  }   
			  }
			  // If "cover crops"
			  else if (i==2){
		    	  for (int j = 0; j < regionSubbasinId.length; j++) {
		    		  // search random assignment 
		    		  if (Math.random() <= nsga2.getNSGA2Configuration().getMutationProbability()) {           
		    			  if (assignments[currentNumBMPs*regionSubbasinId.length+j] == 1) {
		    				  assignments[currentNumBMPs*regionSubbasinId.length+j] = 0;
		    			  } else{
		    				  assignments[currentNumBMPs*regionSubbasinId.length+j] = 1;
		    			  }
		    		  }    	
		    	  }     				  
			  }
			  // If "filter strips"
			  else if (i==3){
		    	  for (int j = 0; j < regionSubbasinId.length; j++) {
		    		  // search random assignment 
		    		  if (Math.random() <= nsga2.getNSGA2Configuration().getMutationProbability()) {
		    			  assignments[currentNumBMPs*regionSubbasinId.length+j] = filterStrip_minmaxValues[0] + Math.random()*(filterStrip_minmaxValues[1] - filterStrip_minmaxValues[0]);
		    		  }   	
		    	  }  				  
			  }   	
			  // If "grassed waterway"
			  else if (i==4){
		    	  for (int j = 0; j < regionSubbasinId.length; j++) {
		    		  // search random assignment 
		    		  if (Math.random() <= nsga2.getNSGA2Configuration().getMutationProbability()) {           
		    			  if (assignments[currentNumBMPs*regionSubbasinId.length+j] == 1) {
		    				  assignments[currentNumBMPs*regionSubbasinId.length+j] = 0;
		    			  } else{
		    				  assignments[currentNumBMPs*regionSubbasinId.length+j] = 1;
		    			  }
		    		  }    	
		    	  }  				  
			  }     
			  // If "conservation tillage"
			  else if (i==5){
		    	  for (int j = 0; j < regionSubbasinId.length; j++) {
		    		  // search random assignment 
		    		  if (Math.random() <= nsga2.getNSGA2Configuration().getMutationProbability()) {           
		    			  if (assignments[currentNumBMPs*regionSubbasinId.length+j] == 1) {
		    				  assignments[currentNumBMPs*regionSubbasinId.length+j] = 0;
		    			  } else{
		    				  assignments[currentNumBMPs*regionSubbasinId.length+j] = 1;
		    			  }
		    		  }    	
		    	  }  				  
			  }   
			  // If "binary wetlands"
			  else if (i==6){
		    	  for (int j = 0; j < regionSubbasinId.length; j++) {
		    		  // search random assignment 
		    		  if (Math.random() <= nsga2.getNSGA2Configuration().getMutationProbability()) {           
		    			  if (assignments[currentNumBMPs*regionSubbasinId.length+j] == 1) {
		    				  assignments[currentNumBMPs*regionSubbasinId.length+j] = 0;
		    			  } else{
		    				  assignments[currentNumBMPs*regionSubbasinId.length+j] = 1;
		    			  }
		    		  }    	
		    	  }  			  
			  }
   			  // If "variable area wetlands"
			  else if (i==7){
		    	  for (int j = 0; j < regionSubbasinId.length; j++) {
		    		// search random assignment 
		    		  if (Math.random() <= nsga2.getNSGA2Configuration().getMutationProbability()) { 
		    			  assignments[currentNumBMPs*regionSubbasinId.length+j] = variableAreaWetlands_minmaxValues[j][0] + Math.random()*(variableAreaWetlands_minmaxValues[j][1] - variableAreaWetlands_minmaxValues[j][0]);
		    		  }
		    	  }  			  
			  }
   			  // If "variable Wetfr wetlands"
			  else if (i==8){
		    	  for (int j = 0; j < regionSubbasinId.length; j++) {
		    		// search random assignment
		    		  if (Math.random() <= nsga2.getNSGA2Configuration().getMutationProbability()) {
		    			  assignments[currentNumBMPs*regionSubbasinId.length+j] = variableWetFrWetlands_minmaxValues[j][0] + Math.random()*(variableWetFrWetlands_minmaxValues[j][1] - variableWetFrWetlands_minmaxValues[j][0]);
		    		  }
		    	  }  			  
			  }
   			  // If "binary ponds"
			  else if (i==9){
		    	  for (int j = 0; j < regionSubbasinId.length; j++) {
		    		  // search random assignment 
		    		  if (Math.random() <= nsga2.getNSGA2Configuration().getMutationProbability()) {           
		    			  if (assignments[currentNumBMPs*regionSubbasinId.length+j] == 1) {
		    				  assignments[currentNumBMPs*regionSubbasinId.length+j] = 0;
		    			  } else{
		    				  assignments[currentNumBMPs*regionSubbasinId.length+j] = 1;
		    			  }
		    		  }    	
		    	  }  		  
			  }
			  currentNumBMPs++;
    	  }// end of "if (this.chosenBMPs[i] == 1)"
      }// end of for (int i = 0; i< chosenBMPs.length; i++)
      
   }
   
   /**
    * Does a uniform crossover between the two individuals. Afterwards, both individuals are altered. If
    * the origial individuals are still needed, use the {@link #clone()} method to get clones and
    * use them instead. 
    * 
    * After crossover, the fitness values of both individuals should be updated.
    * 
    * @param otherIndividual other individual
 * @throws IOException 
    */
   protected void crossover(Individual otherIndividual) throws IOException {
      if (otherIndividual == null) {
         throw new IllegalArgumentException("'otherIndividual' must not be null.");
      }
      
      SWAT_BMPs_Individual otherAssignmentIndividual = (SWAT_BMPs_Individual)otherIndividual;
      
      if (nsga2 != otherAssignmentIndividual.nsga2) {
         throw new IllegalArgumentException("Both individuals must belong to the same NSGA-II instance.");
      }
      
      // execute uniform crossover (added by mbabbars 4/25/2012)
      for (int i = 0; i < assignments.length; i++){
    	  if (Math.random() < nsga2.getNSGA2Configuration().getCrossoverProbability()){
    		  double dummy = assignments[i];
              assignments[i] = otherAssignmentIndividual.assignments[i];
              otherAssignmentIndividual.assignments[i] = dummy;
    	  }
      }
      
      // execute single point crossover
      // if (Math.random() < nsga2.getNSGA2Configuration().getCrossoverProbability()) {
         // crossover in front of 'randomIndex'
        // int randomIndex = (int)(Math.random() * (assignments.length + 1));
         
        // for (int i = 0; i < randomIndex; i++) {
        //    double dummy = assignments[i];
        //    assignments[i] = otherAssignmentIndividual.assignments[i];
        //    otherAssignmentIndividual.assignments[i] = dummy;
       //  }
      //}
   }
   
   /**
    * Creates a clone of this individual, so that changes on the clone do not change the intern data
    * of the original. The rank and crowding distance are not copied by this method. The NSGA-II
    * instance is only copied.
    * 
    * @return cloned individual (without correct rank and crowding distance)
    */
   public Individual createClonedIndividual() throws IOException{
	   SWAT_BMPs_Individual clone = new SWAT_BMPs_Individual(nsga2, regionSubbasinId, tenure_regionSubbasinId, chosenBMPs, chosenFF, filterStrip_minmaxValues, variableAreaWetlands_minmaxValues, variableWetFrWetlands_minmaxValues, economicCostsExecFname,economicCostsOutputFname, erosionExecFname, erosionOutputFname, nitrateExecFname, nitrateOutputFname, pesticidesExecFname, pesticidesOutputFname, assignments.clone());
      clone.fitnessValues = this.fitnessValues.clone();
      clone.UserId = this.UserId;
      clone.IndvId = this.IndvId;
      clone.rating = 0;
      clone.confidence = 0;
      clone.subbasinsFF = this.subbasinsFF.clone();
      return clone;
   }
   
   /*
    * This method writes the individual into a .dat file
    */
   public void writeIndividual (String indivFilename){
       try {
            DataOutputStream dosFlow = new DataOutputStream(new FileOutputStream(indivFilename));
            int numBMPs = 0;
            
            // First row: BMPlookup array
            for (int k = 0; k < this.BMPlookup.length-1; k++){
     		dosFlow.writeBytes(this.BMPlookup[k] + " , ");
            }
            dosFlow.writeBytes(this.BMPlookup[this.BMPlookup.length-1] + "\n");
            
            // Second row: chosenBMP array
            for (int k = 0; k < this.chosenBMPs.length-1; k++){
     		dosFlow.writeBytes(new Integer(this.chosenBMPs[k]).toString() + " , ");
                numBMPs = numBMPs + chosenBMPs[k];
            }
            dosFlow.writeBytes(new Integer(this.chosenBMPs[this.chosenBMPs.length-1]).toString() + "\n");  
            numBMPs = numBMPs + chosenBMPs[this.chosenBMPs.length-1];
            
            // Third row: Regionsubbasin IDs
            for (int k = 0; k < this.regionSubbasinId.length-1; k++){
     		dosFlow.writeBytes(new Integer(this.regionSubbasinId[k]).toString() + " , ");
            }
            dosFlow.writeBytes(new Integer(this.regionSubbasinId[this.regionSubbasinId.length-1]).toString() + "\n");    
            
            // Fourth row: Tenure type
            for (int k = 0; k < this.tenure_regionSubbasinId.length-1; k++){
     		dosFlow.writeBytes(new Integer(this.tenure_regionSubbasinId[k]).toString() + " , ");
            }
            dosFlow.writeBytes(new Integer(this.tenure_regionSubbasinId[this.tenure_regionSubbasinId.length-1]).toString() + "\n");    
            
            // Fifth, sixth, and so on rows are decision variable values for chosenBMPs that are active.
            System.out.println("numBMPs : " + numBMPs);
            for (int k =0; k < numBMPs; k++){
                for (int j = 0; j < this.regionSubbasinId.length-1; j++){
                    dosFlow.writeBytes(new Double(this.assignments[k*regionSubbasinId.length + j]).toString() + " , ");
                }
                dosFlow.writeBytes(new Double(this.assignments[k*regionSubbasinId.length + this.regionSubbasinId.length-1]).toString() + "\n");
            }
            
       } catch (IOException ex) {
            ex.printStackTrace();
       }
 
   }
   /*
    * Evaluate individual method
    */
   public void evaluateIndividual(int idx) {
        String base1 = base + idx+"/";
        
        // Write individual into a .dat file
        writeIndividual (base1+ this.indivFname);
        
        // call ModifySWATInputs_CallSWATexec class to setup all the inputs and run the SWAT executable once.
        ModifySWATInputs_CallSWATexec writeInputsRunSWAT = new ModifySWATInputs_CallSWATexec(this, base1);
        writeInputsRunSWAT.writeInpExecSWAT();
        
        // Now, after the SWAT has been executed for the new inputs related to this individual, calculate the fitness functions.
        for (int i = 0; i < fitnessValues.length; i++) {      
            try {
                //fitnessValues[i] = nsga2.getNSGA2Configuration().getFitnessFunction(i).evaluate(this,base1);
                fitnessValues[i] = fitnessFunctions[i].evaluate(this,base1);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
         }
        System.out.println("evaluateIndividual completed");
    }

    @Override
    public void evaluateRating() {
        int last = this.fitnessValues.length; //take the last fitness function
        this.fitnessValues[last-1] = ((-1)*this.rating);// make -3 as best
    }
}