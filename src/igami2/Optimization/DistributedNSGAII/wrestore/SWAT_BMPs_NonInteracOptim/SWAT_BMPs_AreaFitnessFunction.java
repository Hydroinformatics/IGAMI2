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
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;


import java.io.Serializable;



/**
 * This class implements a fitness function based on the total area of wetlands used in the sub-basins.
 * A smaller value is better.
 * @author Meghna Babbar-Sebens, IUPUI, Dec 2010.
 * @version 1.0
 */
public class SWAT_BMPs_AreaFitnessFunction implements FitnessFunction,Serializable {
	// **************application specific variables (mbabbars)******************
	 
	// this variable stores a value of 1 if this fitness function is used in the optimization, else it stores a value of zero.
    int ffchosen;
    
	// **************** Variables related to BMPs ********************
	// Wetland Variables
	String wetlandFile = "wetland.csv";
	double [][] baselineWetlandData;	
	double [][] potentialWetlandData;
	int[] regionSubbasinId;
	int[] chosenBMPs;
	
	/**
	  * Constructor.
	  * 
	  * @param none (Params should be added in future (mbabbars))
	  */
	public SWAT_BMPs_AreaFitnessFunction(int chosenFlag, int[] regionSubbasinId, int[] chosenBMPs) {
	    this.ffchosen = chosenFlag;
	    this.regionSubbasinId = regionSubbasinId;
	    this.chosenBMPs = chosenBMPs;
	}
	
	
   /**
    * Evaluates the fitness value (total area of BMPs used in the sub-basins that replace area of row crops (e.g. corn and soybean)) of the specified individual.
    * 
    * @param individual individual
    * @return fitness value
    */
   public double evaluate(Individual individual, String base) throws IOException{
      if (individual == null) {
         throw new IllegalArgumentException("'individual' must not be null.");
      }
      if (!(individual instanceof SWAT_BMPs_Individual)) {
         throw new IllegalArgumentException("'individual' must be of type 'SWAT_BMPs_Individual'.");
      }
      
      SWAT_BMPs_Individual aIndividual = (SWAT_BMPs_Individual)individual; // Create a SWAT_BMPs_Individual to points to the individual passed as an argument.
      // Initiate totalBMPArea
      double totalBMPArea = 0;
      // Initiate totalWetlandsArea
      double totalWetlandsArea = 0;
       
      // calculate value of fitness function only if this function is used in optimization (i.e. ffchosen = 1)
      if (this.ffchosen == 1){
    	double[] assignments = aIndividual.getAssignments();
   	
        //************** Calculate Wetland Area ***************************************************************
  	// First, read baseline wetland data from wetland.csv
  	try {
            baselineWetlandData = readBaselineWetlandData(base+wetlandFile);
  	} catch (IOException ex) {
            ex.printStackTrace();
        } 		
  	// Second, read potential (maximum values of "WET_FR", "WET_MXSA", "WET_MXVOL") wetland data from wetland.csv
  	try {
            potentialWetlandData = readPotentialWetlandData(base+wetlandFile);
  	} catch (IOException ex) {
            ex.printStackTrace();
        }

        
    	// If binary wetlands are chosen as a BMP
    	if (this.chosenBMPs[6] == 1){
    		//Calculate from which starting index location in the assignments array are the decision variables for binary wetlands stored.
    		int binWetStartIndex = 0;
    		for (int i=0; i < 6; i++){ // binary wetlands are the 7th BMP, with i=6 in the chosenBMPs array.
    			binWetStartIndex = binWetStartIndex + chosenBMPs[i];
    		}
    		binWetStartIndex = binWetStartIndex * this.regionSubbasinId.length;
    		
    		// Now calculate total area for binary wetlands
    		int j =0;
    		for(int i=binWetStartIndex; i<(binWetStartIndex + this.regionSubbasinId.length); i++){   		
    			if (assignments[i] == 1){
    				totalWetlandsArea = totalWetlandsArea + baselineWetlandData[j][1] + potentialWetlandData[j][1];
                                //System.out.println(j + " , " + regionSubbasinId[j] + " , " + aIndividual.subbasinsFF.length + " , " + aIndividual.subbasinsFF[0].length);
                                aIndividual.subbasinsFF[regionSubbasinId[j]-1][0] = baselineWetlandData[j][1] + potentialWetlandData[j][1]; // update sub-basin fitness function #0, which is for "Area".
                                
    			} else {
    				totalWetlandsArea = totalWetlandsArea + baselineWetlandData[j][1];
                                aIndividual.subbasinsFF[regionSubbasinId[j]-1][0] = baselineWetlandData[j][1] ; // update sub-basin fitness function #0, which is for "Area".
                                
    			}
    			j++;
    		}
    	}
    	
    	// If variable area wetlands are chosen as a BMP
    	if (this.chosenBMPs[7] == 1){
       		//Calculate from which starting index location in the assignments array are the decision variables for variable area wetlands stored.
    		int varAreaWetStartIndex = 0;
    		for (int i=0; i < 7; i++){ // variable area wetlands are the 8th BMP, with i=7 in the chosenBMPs array.
    			varAreaWetStartIndex = varAreaWetStartIndex + chosenBMPs[i];
    		}
    		varAreaWetStartIndex = varAreaWetStartIndex * this.regionSubbasinId.length;
    		
    		// Now calculate total area for binary wetlands
    		int j=0;
    		for(int i=varAreaWetStartIndex; i<(varAreaWetStartIndex + this.regionSubbasinId.length); i++){  		
    			if (assignments[i] > 0){
    				totalWetlandsArea = totalWetlandsArea + baselineWetlandData[j][1] + assignments[i];
                                aIndividual.subbasinsFF[regionSubbasinId[j]-1][0] = baselineWetlandData[j][1] + assignments[i]; // update sub-basin fitness function #0, which is for "Area".
                                
    			} else {
    				totalWetlandsArea = totalWetlandsArea + baselineWetlandData[j][1];
                                aIndividual.subbasinsFF[regionSubbasinId[j]-1][0] = baselineWetlandData[j][1] ; // update sub-basin fitness function #0, which is for "Area".
                                
    			}
    			j++;
    		}
    	}

    	// *******************************************************************************************
    	// Calculate total area occupied by the BMPs replacing area of row crops.
    	totalBMPArea = totalWetlandsArea ;
        System.out.println("totalBMPArea : " + totalBMPArea);
    	return totalBMPArea;
      } else{
    	  return 0.0;
      }
   }
  
   //***************************************************************************************************
   // Supporting methods
   //***************************************************************************************************
   
	// Read baseline wetland data for existing wetlands in all the sub-basins.
	private double[][] readBaselineWetlandData(String debFile) throws IOException
	{
		FileInputStream debFIS;
		InputStreamReader debISR;
		BufferedReader debBR;
		DataInputStream debDIS;
		String debData;
		String tokenString;
		StringTokenizer st;
		Integer id;
		double [][] baselineWetlandData = new double [this.regionSubbasinId.length][3]; 		
				
		int counter = 0;
				
		debFIS = new FileInputStream(debFile);
		debISR = new InputStreamReader(debFIS);
		debBR = new BufferedReader(debISR);
				
		for(int i=0;i<this.regionSubbasinId.length;i++){
			baselineWetlandData[i][0] = 0.0;
			baselineWetlandData [i][1]=0.0;
			baselineWetlandData[i][2] = 0.0;		
		}
				
		while ((debData = debBR.readLine()) != null) 
		{
			if(counter == 0) {counter++;}
			else
				{
					st = new StringTokenizer(debData, ",");
					tokenString = st.nextToken(); // string for sub-basin ID
					id = new Integer(tokenString);
					tokenString = st.nextToken(); // string for maximum Potential Wet_Fr
					tokenString = st.nextToken(); // string for maximum Potential cumulative wetland area
					tokenString = st.nextToken(); // string for maximum Potential cumulative wetland volume based on natural depressions depths (i.e. NOT design depth!)
						
					tokenString = st.nextToken(); //  read baseline wet_fr in that sub-basin
					baselineWetlandData[counter-1][0] = new Double(tokenString).doubleValue();
						
					tokenString = st.nextToken();// read baseline wetland area in that sub-basin
					baselineWetlandData[counter-1][1] = new Double(tokenString).doubleValue();
					
					tokenString = st.nextToken();// read baseline wetland cumulative volume in that sub-basin
					baselineWetlandData[counter-1][2] = new Double(tokenString).doubleValue();					
						
					counter++;
						
				}//end else
		}//end while
		
		return baselineWetlandData;
	}//end readBaselineWetlandData
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Read potential wetland data (maximum values of "WET_FR", "WET_MXSA", "WET_MXVOL") for all the sub-basins.
	private double[][] readPotentialWetlandData(String debFile) throws IOException
	{
		FileInputStream debFIS;
		InputStreamReader debISR;
		BufferedReader debBR;
		DataInputStream debDIS;
		String debData;
		String tokenString;
		StringTokenizer st;
		Integer id;
		double [][] potentialWetlandData = new double [this.regionSubbasinId.length][3]; 		
				
		int counter = 0;
				
		debFIS = new FileInputStream(debFile);
		debISR = new InputStreamReader(debFIS);
		debBR = new BufferedReader(debISR);
				
		for(int i=0;i<this.regionSubbasinId.length;i++){
			potentialWetlandData[i][0] = 0.0;
			potentialWetlandData[i][1]=0.0;
			potentialWetlandData[i][2] = 0.0;		
		}
				
		while ((debData = debBR.readLine()) != null) 
		{
			if(counter == 0) {counter++;}
			else
				{
					st = new StringTokenizer(debData, ",");
					tokenString = st.nextToken(); // string for sub-basin ID
					id = new Integer(tokenString);
					
					tokenString = st.nextToken(); // string for maximum Potential Wet_Fr
					potentialWetlandData[counter-1][0] = new Double(tokenString).doubleValue();
					
					tokenString = st.nextToken(); // string for maximum Potential cumulative wetland area
					potentialWetlandData[counter-1][1] = new Double(tokenString).doubleValue();
					
					tokenString = st.nextToken(); // string for maximum Potential cumulative wetland volume based on natural depressions depths (i.e. NOT design depth!)
					potentialWetlandData[counter-1][2] = new Double(tokenString).doubleValue();	
					
					tokenString = st.nextToken(); //  baseline wet_fr in that sub-basin						
					tokenString = st.nextToken();// baseline wetland area in that sub-basin					
					tokenString = st.nextToken();// baseline wetland cumulative volume in that sub-basin
				
						
					counter++;
						
				}//end else
		}//end while
		
		return potentialWetlandData;
	}//end readBaselineWetlandData
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~	

}