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
 * 
 * @author Meghna Babbar-Sebens, IUPUI, Dec 2010.
 * @version 1.0
 */
public class binaryWetlandsAreaFitnessFunction implements Serializable {
	// application specific variables (mbabbars)
	String debFile = "deb.csv";
	String base = "../SWAT/swat0/";
	int noOfSubbasins = 130;
	StringBuilder data;
	 String [] lookup = new String[]{"WET_FR", "WET_MXSA", "WET_MXVOL", "WET_NSA", "WET_NVOL", "WET_VOL", "WET_K"}; 
	 double[][] wetlandsData ;

	/**
	  * Constructor.
	  * 
	  * @param none (Params should be added in future (mbabbars))
	  */
	public binaryWetlandsAreaFitnessFunction() {
	      if (wetlandsData == null){
	    	  try {
				readWetlandsInputData(base);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      }

	}
	
	
   /**
    * Evaluates the fitness value (total area of wetlands used in the sub-basins) of the specified individual.
    * 
    * @param individual individual
    * @return fitness value
    */
   public double evaluate(binaryWetlandsIndividual individual, String base) throws IOException{
      if (individual == null) {
         throw new IllegalArgumentException("'individual' must not be null.");
      }
      if (!(individual instanceof binaryWetlandsIndividual)) {
         throw new IllegalArgumentException("'individual' must be of type 'AssignmentIndividual'.");
      }
      
      binaryWetlandsIndividual aIndividual = (binaryWetlandsIndividual)individual;
      
      int[] regionSubbasinId = aIndividual.getRegionSubbasinIds();
      int[] assignments = aIndividual.getAssignments();
      //System.out.println("check1");
      // If matrix of all sub-basin wetland inputs is not initiated, then read the wetland inputs from the input file.
      if (wetlandsData == null){
    	  readWetlandsInputData(base);
    	  //System.out.println("check2");
      }
      double totalWetlandsArea = 0;
      //System.out.println("check3");
      for (int i = 0; i < assignments.length; i++) {
         if (assignments[i] == 1){
        	 totalWetlandsArea = totalWetlandsArea + getWetlandArea(regionSubbasinId[i]);
         }
      }
      //System.out.println("check4");
      double maxWetlandsArea = 0;
      for (int i = 0; i < assignments.length; i++) {          
         maxWetlandsArea = maxWetlandsArea + getWetlandArea(regionSubbasinId[i]);    
      }
      //System.out.print(" / totalWetlandsArea :" + totalWetlandsArea);
      //System.out.println(" / totalWetlandsArea/maxWetlandsArea : " + totalWetlandsArea/maxWetlandsArea);
      //System.out.println("Area Fun evaluated with base "+base);
      return totalWetlandsArea/maxWetlandsArea;
   }
   
   public void readWetlandsInputData(String base) throws IOException
	{
		FileInputStream debFIS;
		InputStreamReader debISR;
		BufferedReader debBR;
		DataInputStream debDIS;
		String debData;
		String tokenString;
		StringTokenizer st;
		Integer id;
				
		String[] replaceWith = new String[lookup.length];		
		wetlandsData = new double[noOfSubbasins][lookup.length];
		int counter = 0;
		
		debFIS = new FileInputStream(base + debFile);
		debISR = new InputStreamReader(debFIS);
		debBR = new BufferedReader(debISR);
		
		for(int i=0;i<noOfSubbasins;i++)
			for(int j=0;j<lookup.length;j++)
		{
			wetlandsData[i][j] = 0;
		}
		
		while ((debData = debBR.readLine()) != null) 
		{
			if(counter == 0) {counter++;}
			else
			{
				st = new StringTokenizer(debData, ",");
				tokenString = st.nextToken();
				id = new Integer(tokenString);
				
				for(int i=0;i<lookup.length-4;i++)
				{
					tokenString = st.nextToken();
					wetlandsData[id.intValue()-1][i] = new Double(tokenString).doubleValue();
					//System.out.println(wetlandsData[id.intValue()][i]);
				}
				
			}//end else
		}//end while
	}//end readWetlandsInputData
	
     double getWetlandArea(int regionSubbasinIdVal) 
    {
   	 Double doubleNumber1 = new Double(0.0);
   	 
   	 doubleNumber1 = new Double(wetlandsData[regionSubbasinIdVal-1][1]);

   	 return doubleNumber1.doubleValue();
    }
}