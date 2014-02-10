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

import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.*;
import java.util.*;
import java.io.*;


/**
 * This class implements a fitness function based on the total difference in reduction in flow when a subset of wetlands are installed, 
 * compared to the baseline condition when all wetlands are installed. A smaller value is better.
 * 
 * @author Meghna Babbar-Sebens, IUPUI, Dec 2010.
 * @version 1.0
 */
public class binaryWetlandsNetflowFitnessFunction implements Serializable {

	 int noOfSubbasins = 130;
	 String base = "../SWAT/swat0/";
	 String rchFileName = "output.rch";
	 String baselineFileName = "baseline.rch";
	 String debFile = "deb.csv";

	
	 FileInputStream relevantSubbasinsFIS;
	 InputStreamReader relevantSubbasinsISR;
	 BufferedReader relevantSubbasinsBR;
	 DataInputStream relevantSubbasinsDIS;
	 String relevantSubbasinsData;	
	
	 FileInputStream pndFIS;
	 InputStreamReader pndISR;
	 BufferedReader pndBR;
	 DataInputStream pndDIS;
	 String pndData;
	
	 File pndF;
	 Writer pndW;
	
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
	 String[] lookup = new String[]{"WET_FR", "WET_MXSA", "WET_MXVOL", "WET_NSA", "WET_NVOL", "WET_VOL", "WET_K"};
	 String[] replaceWith = new String[lookup.length];	
	
	 double[][] restoreData = new double[noOfSubbasins][lookup.length];
	
	 double[] maxFlowOutput = new double[noOfSubbasins];
	 double[] maxFlowBaseline = new double[noOfSubbasins];
	 double[] percentageReductionFlow = new double[noOfSubbasins];
	 double[] volumeReductionFlow = new double[noOfSubbasins];
	 ExecRunner er;
	
	 double sumFlow;
	
   /**
    * Constructor.
    * 
    * @param 
    */
   public binaryWetlandsNetflowFitnessFunction(double sumflow) {
	 //get the baseline values in the restoreData array
	   try {
		readBaselineData(base);
	   } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	   }
	   
	   this.sumFlow = sumflow;
   }
   
   /**
    * Evaluates the fitness value (variance of the event sizes) of the specified individual.
    * 
    * @param individual individual
    * @return fitness value
    */
   public double evaluate(binaryWetlandsIndividual individual, String base) throws IOException{
       this.base = base;
	   String[] replace = new String[lookup.length];
	   double flow=0;

	   
	   if (individual == null) {
         throw new IllegalArgumentException("'individual' must not be null.");
      }
      if (!(individual instanceof binaryWetlandsIndividual)) {
         throw new IllegalArgumentException("'individual' must be of type 'binaryWetlandsIndividual'.");
      }
      
      binaryWetlandsIndividual aIndividual = (binaryWetlandsIndividual)individual;
      int[] assignments = aIndividual.getAssignments();
      int[] regionSubbasinIds = aIndividual.getRegionSubbasinIds();
      
   
      // Initialize all pond files to zero before setting their values as per the assignment.
      for(int i=0;i<noOfSubbasins;i++) {
    	  for(int j=0;j<replace.length;j++){
				replace[j] = "0.000";
    	  }//for end
    	  replaceValuesInPndFile(i, replace,base);
 	  }
      //System.out.println("check1");
      
      // Initialize maxflow values for all reaches.
	  for(int i=0;i<noOfSubbasins;i++){
		  maxFlowOutput[i] = -Double.MIN_VALUE;
		  maxFlowBaseline[i] = -Double.MIN_VALUE;
      }	 
     
	  // Set pond files to values as per the assignment
 	  for(int i=0;i<assignments.length;i++)
	  {
	 	 if(assignments[i] == 0)
		 {
			 for(int j=0;j<replace.length;j++)
			 {
				 replace[j] = "0.000";
			 }//for end
			 replaceValuesInPndFile(regionSubbasinIds[i]-1, replace,base);
			 //System.out.println("check2");
		 }//if end
		 else if(assignments[i] == 1)
		 {
			 for(int j=0;j<replace.length-4;j++)
			 {
				 replace[j] = new Double(restoreData[regionSubbasinIds[i]-1][j]).toString();
				 
			 }//for end
			 replace[replace.length-4] = "0.000";//new Double(restoreData[i][1]).toString();
			 replace[replace.length-3] = "0.000";//new Double(restoreData[i][2]).toString();
			 replace[replace.length-2] = "0.000";
			 replace[replace.length-1] = "50.000";//set WET_K to 0.5, rest all read from the restoreData matrix
			 replaceValuesInPndFile(regionSubbasinIds[i]-1, replace,base);
			 //System.out.println("check3");
		 }//else end
	  }//for end
 	
          
          
        Process p;
        ProcessBuilder pb = new ProcessBuilder(base+"SWAT2005.exe");
        Map<String, String> env = pb.environment();
        env.put("VAR1", "myValue");
        env.remove("OTHERVAR");
        env.put("VAR2", env.get("VAR1") + "suffix");
        pb.directory(new File(base));
        p = pb.start();
       
        try{ 
        p.waitFor();
        //System.out.println("sucess " + p.waitFor());
        } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        //System.out.println("check8");
        e.printStackTrace();
        }
       
 	  flow = processRchFileWithLog(base);
 	  
 	  for (int k = 0; k < assignments.length; k++){
    	 System.out.print(assignments[k]);
      } 
    
       /*
 	  // Execute SWAT2005.exe for the updated pond files.
 	  //System.out.println("check4");
	  Process p = Runtime.getRuntime().exec("SWAT2005.exe");
	  //System.out.println("check5");
 	  try {
 		//System.out.println("check6");
 		p.waitFor();
		//System.out.println("check7");
	  } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		//System.out.println("check8");
		e.printStackTrace();
	  }
           */
          
	  //System.out.println("check9");
 	  // Obtain Netflows after log transformation.
          //Runtime.getRuntime().gc();//make more memory available

       
 	  //System.out.print(" / Flow: " + flow);
 	  //System.out.println(" / flow/this.sumFlow : " + flow/this.sumFlow);
	//System.out.println("Flow Fun evaluated with base "+base);  
      return flow/this.sumFlow;
   } // end evaluate method
   
	 void printMatrix(double[] a)
	{
		for(int i=0;i<a.length;i++)
			System.out.println((i+1) + "," + a[i]);
		
	}
	
	 void avgMatrix(double[] a)
	{
		double avg = 0;
		for(int i=0;i<127;i++)
			avg = avg + a[i];
		System.out.println(avg/a.length);
	}
	
	 int maxMatrix(double[] a)
	{
		int maxIndex = -1;
		double max = Double.MIN_VALUE;
		for(int i=0;i<127;i++)
		{
			if(a[i] > max) {max = a[i];maxIndex = i;}
		}
		return maxIndex;
	}
	
	
	
	 void percentageReduction()
	{
		int i;
		for(i=0;i<percentageReductionFlow.length;i++)
		{
			percentageReductionFlow[i] = ((maxFlowBaseline[i] - maxFlowOutput[i])/maxFlowBaseline[i])*100;
			
		}
	}
	
	 void volumeReduction()
	{
		int i;
		for(i=0;i<volumeReductionFlow.length;i++)
		{
			volumeReductionFlow[i] = maxFlowBaseline[i] - maxFlowOutput[i];
			
		}
	}
	

	
	 void readBaselineData(String base) throws IOException
	{
		FileInputStream debFIS;
		InputStreamReader debISR;
		BufferedReader debBR;
		DataInputStream debDIS;
		String debData;
		String tokenString;
		StringTokenizer st;
		Integer id;
		
		
		int counter = 0;
		
		debFIS = new FileInputStream(base + debFile);
		debISR = new InputStreamReader(debFIS);
		debBR = new BufferedReader(debISR);
		
		for(int i=0;i<noOfSubbasins;i++)
			for(int j=0;j<lookup.length;j++)
		{
			restoreData[i][j] = 0;
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
					restoreData[id.intValue()-1][i] = new Double(tokenString).doubleValue();
					//System.out.println(restoreData[id.intValue()][i]);
				}
				
			}//end else
		}//end while
	}//end readMatrixData
	
	 synchronized void setAllZerosInPndFiles(String base) throws IOException
	{
		String[] replaceWith = new String[lookup.length];
		int i;
		
		for(i=0;i<replaceWith.length;i++)
		{
			replaceWith[i] = "0.000";
		}
		
		for(i=0;i<noOfSubbasins;i++)
		{
			replaceValuesInPndFile(i, replaceWith,base);
		}
		
	}
	
	 synchronized void replaceValuesInPndFile(int pondId, String[] replaceWith, String base) throws IOException
	{
		//change contents of the input file
		int counter = 0, start = 0, end = 0,i,j, index;
		String temp = "";
			    	 
	    	 data = new StringBuilder();
	    	 counter = 0;
	    	 
	    	 
	    	 for(j=0;j<lookup.length;j++)
	    	 {
	    		 //replaceWith[j] = new Double(restoreData[pondId][j]).toString() + "00";
	    		 StringBuilder sbTemp = new StringBuilder(replaceWith[j]+"00"); 
	    		 index = sbTemp.lastIndexOf(".");
	    		 index = index + 4;
	    		 sbTemp.delete(index, sbTemp.length());
	    		 replaceWith[j] = sbTemp.toString();
	    		 
	    	 }
	    	 
	    	 relevantSubbasinsData = new Integer(pondId+1).toString();
	    	 j = relevantSubbasinsData.length();
	    	 j = 5 - j;
	    	 for(i=0;i<j;i++)
	    	 {
	    		 temp = temp + "0";
	    	 }
	    	 relevantSubbasinsData = temp + relevantSubbasinsData;
	    	 relevantSubbasinsData = relevantSubbasinsData + "0000.pnd";//subbasin id + 0000 is pond file
	    	 
	    	 pndFIS = new FileInputStream(base + relevantSubbasinsData);
	 		 pndISR = new InputStreamReader(pndFIS);
	 		 pndBR = new BufferedReader(pndISR);
	 		
	 	     while ((pndData = pndBR.readLine()) != null) 
	 	     {//read entire contents of pond file in data variable
	 	    	
	 	    	 //System.out.println(pndData);
	 	    	 if (counter==0) { data = data.append(pndData); counter++;}
	 	    	 else data = data.append("\n" + pndData);
	 	    	
	 	     }//pnd data
	 	     pndFIS.close();
	 	     pndISR.close();
	 	     pndBR.close();//close pond file .. later open in write mode
	 	    //System.out.println(data.toString());
	 	for(j=0;j<lookup.length;j++)//look for the words appearing in lookup table ... corresponding values will be changed
	 	{
	 		end = data.indexOf(lookup[j]);
		 	   //System.out.println(data.charAt(end));
	 		end = end - 7; //end points to the last digit of the value
		 	//System.out.println(data.charAt(end));
		 	for(i=replaceWith[j].length()-1;i>=0;i--)
			    {
			    		data.setCharAt(end, replaceWith[j].charAt(i));
			    		end = end - 1;
			    }
			 while(data.charAt(end)!= ' ')
			    	{
			    		data.setCharAt(end, ' ');
			    		end = end - 1;
			    	}
			
	 	}//for 
	 	    pndF = new File(base + relevantSubbasinsData);
	 	    pndW = new BufferedWriter(new FileWriter(pndF));
		    pndW.write(data.toString());
		    pndW.close();
		    
	    
	}//replaceValues ends
	
     synchronized double processRchFile(String base) throws IOException
    {
   	 int counter = 0, start = 0, end = 0,i,j;
   	 String stringNumber, id;
		 Double doubleNumber1 = new Double(0.0) , doubleNumber2 = new Double(0.0), doubleNumber3 = new Double(0.0);
	  
		rchFIS = new FileInputStream(base + rchFileName);
		rchISR = new InputStreamReader(rchFIS);
		rchBR = new BufferedReader(rchISR);
		
		baselineFIS = new FileInputStream(base + baselineFileName);
		baselineISR = new InputStreamReader(baselineFIS);
		baselineBR = new BufferedReader(baselineISR);
		
	    	 
	    counter = 0;
		while (((rchData = rchBR.readLine()) != null)&&(((baselineData = baselineBR.readLine()) != null))) 
		{//read entire contents of pond file in data variable
	    	 
			if (rchData.contains("REACH")) counter++;
	    	 //System.out.println(pndData);
			if(counter>1300)
			{
	    	 //System.out.println(pndData);
	    	 if (rchData.contains("REACH") && baselineData.contains("REACH")) { 
	    		start = 61; //max flow id begins at this location
	    		stringNumber = rchData.substring(start, start+1);
	    		 start = start - 1;
	    		 while(rchData.charAt(start) != ' ')
	    		 {
	    			 stringNumber = rchData.substring(start, start+1) + stringNumber;
	    			start = start - 1;
	    		 }
	    		//System.out.println(id);
	    		 doubleNumber1 = new Double(stringNumber);
	    		 
	    		 start = 61; //max flow id begins at this location
	 	    	 stringNumber = baselineData.substring(start, start+1);
		    	 start = start - 1;
		    	 while(baselineData.charAt(start) != ' ')
		    	 {
		    			stringNumber = baselineData.substring(start, start+1) + stringNumber;
		    			start = start - 1;
		    	 }
		    		//System.out.println(id);
		    	 doubleNumber2 = new Double(stringNumber);
		    	 
		    	 id = "";
	    		 start = 9; //subbasin id begins at this location
	    		 id = rchData.substring(start, start+1);
	    		 start = start - 1;
	    		 while(rchData.charAt(start) != ' ')
	    		 {
	    			id = rchData.substring(start, start+1) + id;
	    			start = start - 1;
	    		 }
		    	 
	    		//System.out.println(doubleNumber1.doubleValue() - doubleNumber2.doubleValue());
	    		 if(new Integer(id).intValue() < 128) doubleNumber3 = new Double(doubleNumber3.doubleValue() + doubleNumber1.doubleValue() - doubleNumber2.doubleValue());
	    		 //System.out.println(doubleNumber3);
	    		 
	    		 }
			}
	    }//while end
	    return doubleNumber3.doubleValue();
	   
	   
	    //System.out.println(data.toString());
   
	 
    }//processRchFile end 
    
     synchronized double processRchFileWithLog(String base) throws IOException
    {
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
		while (((rchData = rchBR.readLine()) != null)&&(((baselineData = baselineBR.readLine()) != null))) 
		{//read entire contents of pond file in data variable
	    	 
			if (rchData.contains("REACH")) counter++;
	    	 //System.out.println(pndData);
			if(counter>1300)
			{
	    	 //System.out.println(pndData);
	    	 if (rchData.contains("REACH") && baselineData.contains("REACH")) { 
	    		start = 61; //max flow id begins at this location
	    		stringNumber = rchData.substring(start, start+1);
	    		 start = start - 1;
	    		 while(rchData.charAt(start) != ' ')
	    		 {
	    			 stringNumber = rchData.substring(start, start+1) + stringNumber;
	    			start = start - 1;
	    		 }
	    		//System.out.println(id);
	    		 doubleNumber1 = new Double(stringNumber);
	    		 
	    		 start = 61; //max flow id begins at this location
	 	    	 stringNumber = baselineData.substring(start, start+1);
		    	 start = start - 1;
		    	 while(baselineData.charAt(start) != ' ')
		    	 {
		    			stringNumber = baselineData.substring(start, start+1) + stringNumber;
		    			start = start - 1;
		    	 }
		    		//System.out.println(id);
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
	    		 
	    		 //System.out.println("id1 " + id1 + " id2 " + id2);
	    		//System.out.println(doubleNumber1.doubleValue() - doubleNumber2.doubleValue());
	    		 if(new Integer(id1).intValue() < 128)
	    		 {
	    			 temp = (doubleNumber1.doubleValue() - doubleNumber2.doubleValue())*(doubleNumber1.doubleValue() - doubleNumber2.doubleValue());
	    			 temp = 1 + temp;
	    			 //System.out.println("temp " + temp);
	    			 //if(temp > 0)temp = Math.log(temp);
	    			 //else temp = 0;
	    			temp = Math.log(temp);
	    			//System.out.println(temp);
	    			 doubleNumber3 = new Double(doubleNumber3.doubleValue() + temp);
	    		 }
	    		 //System.out.println(doubleNumber3);
	    		 
	    		 }
			}
	    }//while end
	    return doubleNumber3.doubleValue();
	   
	   
	    //System.out.println(data.toString());
   
	 
    }//processRchFile end
    
    
	
}