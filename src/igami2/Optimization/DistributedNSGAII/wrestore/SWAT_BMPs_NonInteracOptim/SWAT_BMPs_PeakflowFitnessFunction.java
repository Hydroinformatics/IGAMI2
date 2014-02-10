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
 * This class implements a fitness function based on the total difference in reduction in flow when a subset of wetlands are installed, 
 * compared to the baseline condition when all wetlands are installed. A smaller value is better.
 * 
 * @author Meghna Babbar-Sebens, IUPUI, Dec 2010.
 * @version 1.0
 */
public class SWAT_BMPs_PeakflowFitnessFunction implements FitnessFunction,Serializable {

	int noOfSubbasins = 130; // These are the total number of sub-basins in the SWAT model.
	int noOfDaysSkipped = 366; // These are the number of days to skip from the beginning of time for flow calculations.
	int noofDaysUsed = 1461; // These are the number of days used to do the flow calculations. For example, 365 days in 2005 + 365 days in 2006 + 365 days in 2007 + 366 days in 2008 = 1461 days.
	int [] omittedSubBasins = new int[] {128, 129, 130}; // These sub-basins are not included in the calculations.
	
	String base = "../SWAT/swat_dirs/swat0/"; // This will be changed when "evaluate" method is called.
	String rchFileName = "output.rch";
	String baselineFileName = "baseline.rch";
	
	FileInputStream rchFIS;
	InputStreamReader rchISR;
	BufferedReader rchBR;
	DataInputStream rchDIS;
	String rchData;	
	File rchF;
	Writer rchW;
	
	FileInputStream baselineFIS;
	InputStreamReader baselineISR;
	BufferedReader baselineBR;
	DataInputStream baselineDIS;
	String baselineData;
	
	StringBuilder data;
	
	
	double[] subbasinMaxPeakFlowReduction = new double[noOfSubbasins - omittedSubBasins.length];
	
	double watershedMaxPeakFlowreduction;
	// choose either maximumOfSubbasinMaxPeakFlowReduction or averageOfSubbasinMaxPeakFlowReduction to be TRUE.
	// If maximumOfSubbasinMaxPeakFlowReduction is true, then the largest value out of all the sub-basin maximum peak flow reductions is used to represent the watershed peak flow reduction.
	// If averageOfSubbasinMaxPeakFlowReduction is true, then the average of all the sub-basin maximum peak flow reductions is used to represent the watershed peak flow reduction.
	boolean maximumOfSubbasinMaxPeakFlowReduction = true;
	boolean averageOfSubbasinMaxPeakFlowReduction = false;
	
	// this variable stores a value of 1 if this fitness function is used in the optimization, else it stores a value of zero.
	int ffchosen;
	
   /**
    * Constructor.
    * 
    * @param 
    */
   public SWAT_BMPs_PeakflowFitnessFunction(int chosenFlag) {	   
	   this.ffchosen = chosenFlag;    	   
   }
   
   /**
    * Evaluates the fitness value (variance of the event sizes) of the specified individual.
    * 
    * @param individual individual
    * @return fitness value
    */
   public double evaluate(Individual individual, String base) throws IOException{
       this.base = base;
       watershedMaxPeakFlowreduction = 0;

	   
	   if (individual == null) {
         throw new IllegalArgumentException("'individual' must not be null.");
      }
      if (!(individual instanceof SWAT_BMPs_Individual)) {
         throw new IllegalArgumentException("'individual' must be of type 'binaryWetlandsIndividual'.");
      }
      
      SWAT_BMPs_Individual aIndividual = (SWAT_BMPs_Individual)individual;
      
      if (this.ffchosen == 1){
    	double[] assignments = aIndividual.getAssignments();
      	int[] regionSubbasinIds = aIndividual.getRegionSubbasinIds();
     
      	subbasinMaxPeakFlowReduction = processRchFileForPeakFlowReductions(base);
      	
      	//System.out.println("***********************************************************************");
      	//System.out.println("Maximum peak flow reduction in sub-basin 70: " + subbasinMaxPeakFlowReduction[69]);
      	//System.out.println("***********************************************************************");      	
      	for (int i = 0; i < (this.noOfSubbasins - this.omittedSubBasins.length); i++ ) {
          aIndividual.subbasinsFF[i][1] =  -subbasinMaxPeakFlowReduction [i]; // update sub-basin fitness function #1, which is for "Peak Flow Reduction".
        }
                  
      	if (maximumOfSubbasinMaxPeakFlowReduction == true){
      		watershedMaxPeakFlowreduction = maxMatrix(subbasinMaxPeakFlowReduction);
      	}
      	
      	if (averageOfSubbasinMaxPeakFlowReduction == true){
      		watershedMaxPeakFlowreduction = avgMatrix(subbasinMaxPeakFlowReduction);
      	}
      	        //System.out.println("Maximum peak flow reduction in watershed: " + watershedMaxPeakFlowreduction);
 	  	return -(watershedMaxPeakFlowreduction); // Since all fitness functions are minimizing functions, so return negative of maximum reduction.
      } // ffchosen condition ends
      else {
    	  return 0.0;
      }
   } // end evaluate method
	
	 double avgMatrix(double[] a)
	{
		double avg = 0;
		for(int i=0;i<a.length;i++)
			avg = avg + a[i];
		return(avg/a.length);
	}
	
	 double maxMatrix(double[] a)
	{
		double max = - Double.MIN_VALUE;
                //System.out.println("double min value: " + max);
		for(int i=0;i<a.length;i++)
		{
			if(a[i] > max) {max = a[i];}
		}
		return max;
	}
	
    // This method reads the output.rch file and baseline.rch files and calculates the maximum daily peak flow reductions
	// observed in every sub-basin.
     double[] processRchFileForPeakFlowReductions(String base) throws IOException
    {
        double [][] rchFlows = new double [noOfSubbasins][noofDaysUsed];
	double [][] baselineFlows = new double [noOfSubbasins][noofDaysUsed];
	double [][] flowReduction = new double [noOfSubbasins][noofDaysUsed];
	boolean [][] isPeakFlow = new boolean [noOfSubbasins][noofDaysUsed];	
    	int counter = 0, start = 0, end = 0,i,j;
   	 	String stringNumber, id1, id2;
   	 	double temp = 0;
		Double doubleNumber1 = new Double(0.0) , doubleNumber2 = new Double(0.0), doubleNumber3 = new Double(0.0);
	  
		rchFIS = new FileInputStream(base + rchFileName);
		rchISR = new InputStreamReader(rchFIS);
		rchBR = new BufferedReader(rchISR);
		
		baselineFIS = new FileInputStream(base + baselineFileName);
		baselineISR = new InputStreamReader(baselineFIS);
		baselineBR = new BufferedReader(baselineISR);
		//System.out.println("rch and baseline base "+base);
	    	 
	    counter = 0;
	    int subbasinCounter = 0;
	    int currentDay = 0;
		while (((rchData = rchBR.readLine()) != null)&&(((baselineData = baselineBR.readLine()) != null))) 
		{//read entire contents of .rch files in double [][] rchFlows and double [][] baselineFlows
	    	 
			if (rchData.contains("REACH")) counter++;
			if(counter> (noOfSubbasins*noOfDaysSkipped)) // 
			{
	    	 if (rchData.contains("REACH") && baselineData.contains("REACH")) { 
	    		 
	    		 // Which "currentDay" out of "noofDaysUsed" are we currently reading data for?
	    		 subbasinCounter++;
	    		 if (subbasinCounter > noOfSubbasins){
	    			 currentDay++;
	    			 subbasinCounter = 1;
	    		 } 
	    		 
	    		 start = 61; //read flow backwards
	    		 stringNumber = rchData.substring(start, start+1);
	    		 start = start - 1;
	    		 while(rchData.charAt(start) != ' ')
	    		 {
	    			 stringNumber = rchData.substring(start, start+1) + stringNumber;
	    			start = start - 1;
	    		 }

	    		 doubleNumber1 = new Double(stringNumber);
	    		 
	    		 start = 61; //read flow backwards
	 	    	 stringNumber = baselineData.substring(start, start+1);
		    	 start = start - 1;
		    	 while(baselineData.charAt(start) != ' ')
		    	 {
		    			stringNumber = baselineData.substring(start, start+1) + stringNumber;
		    			start = start - 1;
		    	 }

		    	 doubleNumber2 = new Double(stringNumber);
		    	 
		    	 id1 = "";
	    		 start = 9; //subbasin id begins at this location
	    		 id1 = rchData.substring(start, start+1);
	    		 start = start - 1;
	    		 while(rchData.charAt(start) != ' ')
	    		 {
	    			id1 = rchData.substring(start, start+1) + id1;
	    			start = start - 1;
	    		 }
		    	 
	    		 id2 = "";
	    		 start = 9; //subbasin id begins at this location
	    		 id2 = baselineData.substring(start, start+1);
	    		 start = start - 1;
	    		 while(baselineData.charAt(start) != ' ')
	    		 {
	    			id2 = baselineData.substring(start, start+1) + id2;
	    			start = start - 1;
	    		 }
	   
	    		 // populate rchFlows and baselineFlows
	    		 rchFlows[(new Integer (id1)).intValue()-1][currentDay] = doubleNumber1.doubleValue();
	    		 baselineFlows[(new Integer (id2)).intValue()-1][currentDay] = doubleNumber2.doubleValue();
	    		 
	    	 } // if (rchData.contains("REACH") && baselineData.contains("REACH"))
			} // if(counter> (noOfSubbasins*noOfDaysSkipped))
	    }//while end
	      
                //close all files
                
		
		rchBR.close();
		rchISR.close();
                rchFIS.close();
                
		baselineBR.close();
                baselineISR.close();                
                baselineFIS.close();

		// Identify which days in every sub-basin had a flow peak.
		for (i =0; i< noOfSubbasins; i++){
			for (j=0; j<noofDaysUsed; j++){
				isPeakFlow[i][j] = false;
				flowReduction[i][j]= 0.0;
			}
		}
		for (i =0; i< noOfSubbasins; i++){
			for (j=1; j<noofDaysUsed-1; j++){
				if((baselineFlows[i][j]>=baselineFlows[i][j-1]) &&(baselineFlows[i][j]>=baselineFlows[i][j+1])){
					isPeakFlow[i][j] = true;
				}
			}
		}
		
		// Calculate flow reductions for all days with peaks, in every sub-basin
		for (i = 0; i < noOfSubbasins; i++){
			for (j = 0; j < noofDaysUsed; j++){
				if(isPeakFlow[i][j] == true){
					flowReduction[i][j] = baselineFlows[i][j] - rchFlows[i][j];
				}
			}
		}	
		
		// calculate maximum reduction in peak flows across all days in every sub-basin
		// omittedSubBasins are not included, so check if i-th sub-basin is an omitted sub-basin
		int sbCtr = 0;
		boolean omittedSubBasin;
		for (i = 0; i < noOfSubbasins; i++){
			omittedSubBasin = false;
			for (j=0; j< omittedSubBasins.length; j++){
				if (omittedSubBasins[j]-1 == i) {
					omittedSubBasin = true;
				}
			}
			if (omittedSubBasin == false){
				subbasinMaxPeakFlowReduction[sbCtr] = maxMatrix(flowReduction[i]);
				sbCtr++;
			}
		}
		//free
                rchFlows = new double [1][1];
                baselineFlows = new double [1][1];
                flowReduction = new double [1][1];
                isPeakFlow = new boolean [1][1];;
		
                return subbasinMaxPeakFlowReduction;
    }//processRchFileForPeakFlowReductions end
    
    
	
}