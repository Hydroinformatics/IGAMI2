package igami2.Optimization.DPLA;

import java.io.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;


public class WRESTORE {

	static int noOfSubbasins = 130;
	//static int noOfSubbasinsInRegion = 10;
	static int[] regionSubbasinId; //given id - 1;
	
	
	static int noOfAutomata;
	static String base = "../SWAT/swat0/";
	//static String base = "C:\\SWAT\\";
	static String rchFileName = "output.rch";
	static String baselineFileName = "baseline.rch";
	static String debFile = "deb.csv";
	
	static FileInputStream relevantSubbasinsFIS;
	static InputStreamReader relevantSubbasinsISR;
	static BufferedReader relevantSubbasinsBR;
	static DataInputStream relevantSubbasinsDIS;
	static String relevantSubbasinsData;
	
	static FileInputStream pndFIS;
	static InputStreamReader pndISR;
	static BufferedReader pndBR;
	static DataInputStream pndDIS;
	static String pndData;
	
	static File pndF;
	static Writer pndW;
	
	static FileInputStream rchFIS;
	static InputStreamReader rchISR;
	static BufferedReader rchBR;
	static DataInputStream rchDIS;
	static String rchData;
	
	static File rchF;
	static Writer rchW;
	
	static FileInputStream baselineFIS;
	static InputStreamReader baselineISR;
	static BufferedReader baselineBR;
	static DataInputStream baselineDIS;
	static String baselineData;
	
	static StringBuilder data;
	static String[] lookup = new String[]{"WET_FR", "WET_MXSA", "WET_MXVOL", "WET_NSA", "WET_NVOL", "WET_VOL", "WET_K"};
	static String[] replaceWith = new String[lookup.length];
	
	static PursuitAutomata[] pursuitAutomata;
	
	//static double[] WET_FRData = new double[noOfSubbasins];
	//static double[] WET_MXSAData = new double[noOfSubbasins];
	//static double[] WET_MXVOLData = new double[noOfSubbasins];
	
	static double[][] restoreData = new double[noOfSubbasins][lookup.length];
	
	static double[] maxFlowOutput = new double[noOfSubbasins];
	static double[] maxFlowBaseline = new double[noOfSubbasins];
	static double[] percentageReductionFlow = new double[noOfSubbasins];
	static double[] volumeReductionFlow = new double[noOfSubbasins];
	
	static Random r = new Random(System.currentTimeMillis());
	
	static ExecRunner er;
	
	static void printMatrix(double[] a)
	{
		for(int i=0;i<a.length;i++)
			System.out.println((i+1) + "," + a[i]);
		
	}
	
	static void avgMatrix(double[] a)
	{
		double avg = 0;
		for(int i=0;i<127;i++)
			avg = avg + a[i];
		System.out.println(avg/a.length);
	}
	
	static int maxMatrix(double[] a)
	{
		int maxIndex = -1;
		double max = Double.MIN_VALUE;
		for(int i=0;i<127;i++)
		{
			if(a[i] > max) {max = a[i];maxIndex = i;}
		}
		return maxIndex;
	}
	
	
	
	static void percentageReduction()
	{
		int i;
		for(i=0;i<percentageReductionFlow.length;i++)
		{
			percentageReductionFlow[i] = ((maxFlowBaseline[i] - maxFlowOutput[i])/maxFlowBaseline[i])*100;
			
		}
	}
	
	static void volumeReduction()
	{
		int i;
		for(i=0;i<volumeReductionFlow.length;i++)
		{
			volumeReductionFlow[i] = maxFlowBaseline[i] - maxFlowOutput[i];
			
		}
	}
	
	static boolean allConvergedPursuit(double threshold) {
		boolean result = false;
		int count = 0;
		for (int i = 0; i < pursuitAutomata.length; i++) {
			if (pursuitAutomata[i].hasConverged(threshold))
				count++;
		}

		if (count == pursuitAutomata.length)
			result = true;
		return result;
	}
	
	static void readBaselineDataTest() throws IOException
	{
		for(int i=0;i<noOfSubbasins;i++)
			for(int j=0;j<lookup.length;j++)
		{
			restoreData[i][j] = 5.000;
		}
		
	}
	
	static void readBaselineData() throws IOException
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
		
		debFIS = new FileInputStream(base+debFile);
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
	
	static void setAllZerosInPndFiles() throws IOException
	{
		String[] replaceWith = new String[lookup.length];
		int i;
		
		for(i=0;i<replaceWith.length;i++)
		{
			replaceWith[i] = "0.000";
		}
		
		for(i=0;i<noOfSubbasins;i++)
		{
			replaceValuesInPndFile(i, replaceWith);
		}
		
	}
	

	     
	
	static void replaceValuesInPndFile(int pondId, String[] replaceWith) throws IOException
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
	
	
     static double processRchFile() throws IOException
     {
    	 int counter = 0, start = 0, end = 0,i,j;
    	 String stringNumber, id;
 		 Double doubleNumber1 = new Double(0.0) , doubleNumber2 = new Double(0.0), doubleNumber3 = new Double(0.0);
	  
		rchFIS = new FileInputStream(rchFileName);
 		rchISR = new InputStreamReader(rchFIS);
 		rchBR = new BufferedReader(rchISR);
 		
 		baselineFIS = new FileInputStream(baselineFileName);
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
     
     static double processRchFileWithLog() throws IOException
     {
    	 int counter = 0, start = 0, end = 0,i,j;
    	 String stringNumber, id1, id2;
    	 double temp = 0;
 		 Double doubleNumber1 = new Double(0.0) , doubleNumber2 = new Double(0.0), doubleNumber3 = new Double(0.0);
	  
		rchFIS = new FileInputStream(base+rchFileName);
 		rchISR = new InputStreamReader(rchFIS);
 		rchBR = new BufferedReader(rchISR);
 		
 		baselineFIS = new FileInputStream(base+baselineFileName);
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
     
  
     
     static double addArea() throws IOException
     {
    	 Double doubleNumber1 = new Double(0.0) , doubleNumber2 = new Double(0.0), doubleNumber3 = new Double(0.0);
    	 
    	 for(int i=0;i<noOfSubbasins;i++)
    	 {
    		 doubleNumber1 = new Double(doubleNumber1.doubleValue() + restoreData[i][1]);
    	 }
    	 
    	 return doubleNumber1.doubleValue();
     }
     
     static double addAreaRegion() throws IOException
     {
    	 Double doubleNumber1 = new Double(0.0) , doubleNumber2 = new Double(0.0), doubleNumber3 = new Double(0.0);
    	 
    	 for(int i=0;i<regionSubbasinId.length;i++)
    	 {
    		 doubleNumber1 = new Double(doubleNumber1.doubleValue() + restoreData[regionSubbasinId[i]-1][1]);
    	 }
    	 
    	 return doubleNumber1.doubleValue();
     }
     
     static double random()
 	{
 		double result;
 		result = r.nextDouble();
 		return result;
 	}
     static int commonPayoff(double payoff)
 	{
 		int response=0;
 		double d;
 		double rand = random();
 		d = payoff;
 		if(rand<=d)
 		{
 			response = 1;//reward
 			 
 		}
 		else
 		{
 		response = 0;//penalty
 		
 		}
 		return response;
 	}
     
     public static <K, V extends Comparable<? super V>> Map<K, V> 
     sortByValue( Map<K, V> map )
     {
	     List<Map.Entry<K, V>> list =
	         new LinkedList<Map.Entry<K, V>>( map.entrySet() );
	     Collections.sort( list, new Comparator<Map.Entry<K, V>>()
	     {
	         public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
	         {
	     
	         	return -1*(o1.getValue()).compareTo( o2.getValue() ); // -1 for descending, +1 for ascending
	         	
	         }
	     } );
	
	     Map<K, V> result = new LinkedHashMap<K, V>();
	     for (Map.Entry<K, V> entry : list)
	     {
	         result.put( entry.getKey(), entry.getValue() );
	     }
	     return result;
 	}
     
     static void maxReplaceOutput(int subbasinId, double value)
     {
    	 if(value > maxFlowOutput[subbasinId]) maxFlowOutput[subbasinId] = value; 
     }
     
     static void maxReplaceBaseline(int subbasinId, double value)
     {
    	 if(value > maxFlowBaseline[subbasinId]) maxFlowBaseline[subbasinId] = value; 
     }
     
     static void processOutputRchFile() throws IOException
     {
    	 int counter = 0, start = 0, end = 0,i,j;
    	 String id, value;
 		 Double doubleNumber1 = new Double(0.0) , doubleNumber2 = new Double(0.0), doubleNumber3 = new Double(0.0);
 		 Integer basinId;
	  
		rchFIS = new FileInputStream(base+rchFileName);
 		rchISR = new InputStreamReader(rchFIS);
 		rchBR = new BufferedReader(rchISR);
 		
 		
 	    	 
 	    counter = 0;
 		while (((rchData = rchBR.readLine()) != null)) 
 		{//read entire contents of pond file in data variable
 			if (rchData.contains("REACH")) counter++;
 	    	 //System.out.println(pndData);
 			if(counter>1300)
 			{
 	    	 if (rchData.contains("REACH")) {
 	    		 id = "";
 	    		 start = 9; //subbasin id begins at this location
 	    		 id = rchData.substring(start, start+1);
 	    		 start = start - 1;
 	    		 while(rchData.charAt(start) != ' ')
 	    		 {
 	    			id = rchData.substring(start, start+1) + id;
 	    			start = start - 1;
 	    		 }
 	    		 //System.out.println(id);
 	    		 basinId = new Integer(id);
 	    		
 	    		 
 	    		 start = 61; //max flow id begins at this location
	    		 value = rchData.substring(start, start+1);
	    		 start = start - 1;
	    		 while(rchData.charAt(start) != ' ')
	    		 {
	    			value = rchData.substring(start, start+1) + value;
	    			start = start - 1;
	    		 }
	    		//System.out.println(id);
	    		 doubleNumber1 = new Double(value);
	    		 maxReplaceOutput(basinId.intValue()-1, doubleNumber1.doubleValue());
	    		 
 	    	 }//if
 			}//if counter
 			
 		}//while
     }//processOutputRchFile
     
     static void processBaselineRchFile() throws IOException
     {
    	 int counter = 0, start = 0, end = 0,i,j;
    	 String id, value;
 		 Double doubleNumber1 = new Double(0.0) , doubleNumber2 = new Double(0.0), doubleNumber3 = new Double(0.0);
 		 Integer basinId;
	  
 		baselineFIS = new FileInputStream(base+baselineFileName);
 		baselineISR = new InputStreamReader(baselineFIS);
 		baselineBR = new BufferedReader(baselineISR);
 		
 		
 	    	 
 	    counter = 0;
 		while (((baselineData = baselineBR.readLine()) != null)) 
 		{//read entire contents of pond file in data variable
 			if (baselineData.contains("REACH")) counter++;
 	    	 //System.out.println(pndData);
 			if(counter>1300)
 			{
 	    	 if (baselineData.contains("REACH")) {
 	    		 id = "";
 	    		 start = 9; //subbasin id begins at this location
 	    		 id = baselineData.substring(start, start+1);
 	    		 start = start - 1;
 	    		 while(baselineData.charAt(start) != ' ')
 	    		 {
 	    			id = baselineData.substring(start, start+1) + id;
 	    			start = start - 1;
 	    		 }
 	    		 //System.out.println(id);
 	    		 basinId = new Integer(id);
 	    		
 	    		 
 	    		 start = 61; //max flow begins at this location
	    		 value = baselineData.substring(start, start+1);
	    		 start = start - 1;
	    		 while(baselineData.charAt(start) != ' ')
	    		 {
	    			value = baselineData.substring(start, start+1) + value;
	    			start = start - 1;
	    		 }
	    		//System.out.println(id);
	    		 doubleNumber1 = new Double(value);
	    		 maxReplaceBaseline(basinId.intValue()-1, doubleNumber1.doubleValue());
	    		 
 	    	 }//if
 			}//if counter
 			
 		}//while
     }//processBaselineRchFile
     
     static void processBaselineRchFileTab() throws IOException
     {
    	 int counter = 0, start = 0, end = 0,i,j;
    	 String id, value;
 		 Double doubleNumber1 = new Double(0.0) , doubleNumber2 = new Double(0.0), doubleNumber3 = new Double(0.0);
 		 Integer basinId;
 		 StringTokenizer st;
	  
 		baselineFIS = new FileInputStream(base+baselineFileName);
 		baselineISR = new InputStreamReader(baselineFIS);
 		baselineBR = new BufferedReader(baselineISR);
 		
 		
 	    	 
 	    counter = 0;
 		while (((baselineData = baselineBR.readLine()) != null)) 
 		{//read entire contents of pond file in data variable
 			if (baselineData.contains("REACH")) counter++;
 			if(counter>1300)
 			{
 	    	 //System.out.println(pndData);
 	    	 if (baselineData.contains("REACH")) {
 	    		st = new StringTokenizer(baselineData, "	");
 	    		st.nextToken();
 	    		id = st.nextToken();
 	    		basinId = new Integer(id);
 	    		st.nextToken();
 	    		st.nextToken();
 	    		st.nextToken();
 	    		st.nextToken();
 	    		value = st.nextToken();
 	    		//System.out.println(value);
 	    		doubleNumber1 = new Double(value);
 	    		maxReplaceBaseline(basinId.intValue()-1, doubleNumber1.doubleValue());
 	    	 }//if
 			}//if counter
 			
 		}//while
     }//processOutputRchFile
     
     
     public static void main(String argv[]) throws IOException, InterruptedException
     {
    	 int i, j, sampledPayoff,maxIndex;
    	 int[] selectedAction;
    	 String[] replace = new String[lookup.length];
    	 String temp;
    	 DataOutputStream dosFlow;
    	 DataOutputStream dosPeakFlows;
    	 double payoff = 0, sumArea, sumFlow, area, flow, areaPayoff = 0, flowPayoff = 0,areaWeight = 0.5, flowWeight = 0.5, step = 0.1, startWeight = 0.1;
    	 double[] flows = new double[noOfSubbasins];
    	 Map<String,Double> flowMap = new HashMap<String,Double>();    	 
    	 
    	 
    	 noOfAutomata = noOfSubbasins;
    	 
    	 
    	 
    	 
    	 
    	 readBaselineData();//get the baseline values in the restoreData array
    	 //readBaselineDataTest();
    	 
    	 
    	 
    	//the optimization procedure when dealing with a region
    	 //install all the subbasins ... get resultant output.rch file and call that as the baseline.rch file
    	 //also install all subbasins to get maximum reduction in the flow
    	 
    	 regionSubbasinId= new int[]{51, 54, 59, 52, 53, 58, 60, 56, 62, 64};
    	 
    	 
    	 sumArea = addAreaRegion();
    	 //sumFlow = -133248.99430840297;
    	 //sumFlow = -200000;
    	 sumFlow = 1327;
    	 flowWeight = startWeight;
    while(flowWeight<0.9)
    {
    	 areaWeight = 1 - flowWeight;
    	 for(i=0;i<noOfSubbasins;i++)
    	 {
    	 for(j=0;j<replace.length;j++)
		 {
				replace[j] = "0.000";
		 }//for end
    	 replaceValuesInPndFile(i, replace);
    	 }
    	 pursuitAutomata = new PursuitAutomata[regionSubbasinId.length];
    	 selectedAction = new int[regionSubbasinId.length];
    	 for(i=0;i<regionSubbasinId.length;i++)
    	 {
    		 pursuitAutomata[i] = new PursuitAutomata(i,2,0.05);
    	 }
    	 String st = "../SWAT/Region7/" + "Flow"+ new Double(flowWeight).toString().substring(0,3)+ "_Area" + new Double(areaWeight).toString().substring(0,3) +".txt";
    	 
         //File nfile = new File(st);
         //boolean success = nfile.createNewFile();
         dosFlow = new DataOutputStream(new FileOutputStream(st));
    	 
    	 while(!allConvergedPursuit(0.95))
    	 {
    		 area = 0;
    		 flow = 0;
    		 
    		 for(i=0;i<noOfSubbasins;i++)
        	 {
        		 maxFlowOutput[i] = -Double.MIN_VALUE;
        		 maxFlowBaseline[i] = -Double.MIN_VALUE;
        	 }
    		 
	    	 for(i=0;i<regionSubbasinId.length;i++)
	    	 {
	    		 selectedAction[i] = pursuitAutomata[i].selectAction();
	    		 
	    		 //selectedAction[i] = 0;
	    		 if(selectedAction[i] == 0)
	    		 {
	    			 for(j=0;j<replace.length;j++)
	    			 {
	    				 replace[j] = "0.000";
	    			 }//for end
	    			 replaceValuesInPndFile(regionSubbasinId[i]-1, replace);
	    			 
	    		 }//if end
	    		 else if(selectedAction[i] == 1)
	    		 {
	    			 for(j=0;j<replace.length-4;j++)
	    			 {
	    				 replace[j] = new Double(restoreData[regionSubbasinId[i]-1][j]).toString();
	    				 
	    			 }//for end
	    			 replace[replace.length-4] = "0.000";//new Double(restoreData[i][1]).toString();
	    			 replace[replace.length-3] = "0.000";//new Double(restoreData[i][2]).toString();
	    			 replace[replace.length-2] = "0.000";
	    			 replace[replace.length-1] = "50.000";//set WET_K to 0.5, rest all read from the restoreData matrix
	    			 replaceValuesInPndFile(regionSubbasinId[i]-1, replace);
	    			 area = area + restoreData[regionSubbasinId[i]-1][1];
	    		 }//else end
	    	 }//for end
	    	
	    	 
	    	//run SWAT.exe file for the new conditions
	 		//Process p = Runtime.getRuntime().exec("SWAT2005.exe");
	    	//p.waitFor();

                 Process p;
        ProcessBuilder pb = new ProcessBuilder(base+"SWAT2005.exe");
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
        
	    	flow = processRchFileWithLog();	
	    	System.out.println(flow);
	 	
	    	String temp1 = "";
	    	for(i=0;i<regionSubbasinId.length;i++)
	    	{
	    		temp1 = temp1 + new Integer(selectedAction[i]).toString();
	    	}
	    	temp1 = temp1 + ",";
	 		areaPayoff = area / sumArea;
	 		areaPayoff = 1 - areaPayoff;
	 		flowPayoff = flow /sumFlow;
	 		flowPayoff = 1 - flowPayoff;
	 		

	    	//read output produced by the SWAT code and perform reinforcement learning
	 		
	 		
	    	 payoff = areaWeight * areaPayoff + flowWeight * flowPayoff;
	    	 //payoff = 1 - payoff;
	    	 //System.out.println(payoff);
	    	 //do learning based on this payoff
	    	 sampledPayoff = commonPayoff(payoff);
	    	 
	    	 System.out.println(temp1 + flow + " " + area + " " + flowPayoff + " " + areaPayoff);
		 	 dosFlow.writeBytes(temp1 + new Double(flow).toString() + "," + new Double(area).toString() + "," + new Double(flowPayoff).toString() + "," + new Double(areaPayoff).toString()+ "," + new Double(payoff).toString());
			 dosFlow.writeBytes("\n");
	    	 	    	 
	    	 for(i=0;i<regionSubbasinId.length;i++)
	    	 {
	    		 pursuitAutomata[i].doLearning(sampledPayoff, false);
	    	 }
	    	 
	    	 //setAllZerosInPndFiles();
    	}//while end
    
    	 for(i=0;i<regionSubbasinId.length;i++)
    	 {
    		 System.out.println("Subbasin " + (i+1) + " should be "+ pursuitAutomata[i].convergedTo());
    	 }
    	 
    	 dosFlow.close();
    	 flowWeight = flowWeight + step;
    }//while flowWeight
    	 
    	 
    /*
    	 //the optimization procedure when all subbasins are involved
     	 sumFlow = addMaxFlowBaselineRchFile();
    	 sumArea = addArea();
    	 pursuitAutomata = new PursuitAutomata[noOfSubbasins];
    	 for(i=0;i<noOfSubbasins;i++)
    	 {
    		 pursuitAutomata[i] = new PursuitAutomata(i,2,0.005);
    		 maxFlowOutput[i] = -Double.MIN_VALUE;
    		 maxFlowBaseline[i] = -Double.MIN_VALUE;
    	 }
    	 
    	 
    	 //while(!allConvergedPursuit(0.99))
    	 {
    		 area = 0;
    		 flow = 0;
    		 
	    	 for(i=0;i<noOfSubbasins;i++)
	    	 {
	    		 //selectedAction = pursuitAutomata[i].selectAction();
	    		 
	    		 selectedAction = 1;
	    		 if(selectedAction == 0)
	    		 {
	    			 for(j=0;j<replace.length;j++)
	    			 {
	    				 replace[j] = "0.000";
	    			 }//for end
	    			 replaceValuesInPndFile(i, replace);
	    			 
	    		 }//if end
	    		 else if(selectedAction == 1)
	    		 {
	    			 for(j=0;j<replace.length-4;j++)
	    			 {
	    				 replace[j] = new Double(restoreData[i][j]).toString();
	    				 
	    			 }//for end
	    			 replace[replace.length-4] = "0.000";//new Double(restoreData[i][1]).toString();
	    			 replace[replace.length-3] = "0.000";//new Double(restoreData[i][2]).toString();
	    			 replace[replace.length-2] = "0.000";
	    			 replace[replace.length-1] = "50.000";//set WET_K to 0.5, rest all read from the restoreData matrix
	    			 replaceValuesInPndFile(i, replace);
	    			 area = area + restoreData[i][1];
	    		 }//else end
	    	 }//for end
	    	
	    	 
	    	//run SWAT.exe file for the new conditions
	 		//Process p = Runtime.getRuntime().exec(base + "SWAT2005.exe");
	 		Process p = Runtime.getRuntime().exec("SWAT2005.exe");
	    	 //Process p = Runtime.getRuntime().exec("\"C:/Documents and Settings/csci/Desktop/PhD/WRESTORE/SWAT/SWAT2005.exe\"");
	    	 p.waitFor();

	 		//er = new ExecRunner();
	 		//er.setMaxRunTimeSecs(0);
	 		//er.exec(base + "SWAT2005.exe");
	 		//er.exec("\"C:/Documents and Settings/csci/Desktop/PhD/WRESTORE/SWAT/SWAT2005.exe\"");
	 		//er.exec("SWAT2005.exe"); 
	 		
	 		flow = processRchFile();
	 		System.out.println("net flow is " + flow);
	 		flowPayoff = flow / sumFlow;
	 		areaPayoff = area / sumArea;
	    	//read output produced by the SWAT code and perform reinforcement learning
	 		
	 		
	    	 payoff = areaWeight * areaPayoff + flowWeight * flowPayoff;
	    	 //System.out.println(payoff);
	    	 //do learning based on this payoff
	    	 sampledPayoff = commonPayoff(payoff);
	    	 
	    	 for(i=0;i<noOfSubbasins;i++)
	    	 {
	    		 pursuitAutomata[i].doLearning(sampledPayoff, false);
	    	 }
	    	 
	    	 //setAllZerosInPndFiles();
    	}//while end
    
    	 for(i=0;i<noOfSubbasins;i++)
    	 {
    		 System.out.println("Subbasin " + (i+1) + " should be "+ pursuitAutomata[i].convergedTo());
    	 }
    	 */
    
    
    
    
    
    
    
    
    	 /*
    	 flowWeight = startWeight;
    	 
    	    while(flowWeight<0.9)
    	    {
    	    	 areaWeight = 1 - flowWeight;
    	    	 System.out.println(flowWeight + "," + areaWeight);
    	    	 System.out.println("C:\\Documents and Settings\\OT-Admin\\Desktop\\Runs\\run_Flow" + new Double(flowWeight).toString().substring(0,3)+ "_Area" + new Double(areaWeight).toString().substring(0,3) +".txt");
    	    	 
    	    	 flowWeight = flowWeight + step;
    	    }//while flowWeight
    	 */
    	 
    	 
    	 
    	 /*
    	 //calculate flow when each individual subbasin is installed
    	 for(i=0;i<noOfSubbasins;i++)
    	 {
    	 for(j=0;j<replace.length;j++)
		 {
				replace[j] = "0.000";
		 }//for end
    	 replaceValuesInPndFile(i, replace);
    	 }
    	 
    	 area = 0;
		 flow = 0;
		 dosPeakFlows = new DataOutputStream(new FileOutputStream(".\\peakFlows.txt"));
		 for(j=0;j<noOfSubbasins;j++)
    	 {
    		 maxFlowBaseline[j] = -Double.MIN_VALUE;
    	 }
		 processBaselineRchFile();
		 
    	 for(i=0;i<noOfSubbasins;i++)
    	 {
    		 for(j=0;j<noOfSubbasins;j++)
        	 {
        		 maxFlowOutput[j] = -Double.MIN_VALUE;
        	 }
    		 
    		 for(j=0;j<replace.length-4;j++)
			 {
				 replace[j] = new Double(restoreData[i][j]).toString();
				 
			 }//for end
			 replace[replace.length-4] = "0.000";//new Double(restoreData[i][1]).toString();
			 replace[replace.length-3] = "0.000";//new Double(restoreData[i][2]).toString();
			 replace[replace.length-2] = "0.000";
			 if(replace[0].equals("0.000")) replace[replace.length-1] = "0.000";//no wetland there
			 else replace[replace.length-1] = "50.000";//set WET_K to 50, rest all read from the restoreData matrix
			 replaceValuesInPndFile(i, replace);
			 
    		 Process p = Runtime.getRuntime().exec("SWAT2005.exe");
    		 p.waitFor();
    		 flow = processRchFile();
    		 processOutputRchFile();
    		 percentageReduction();
        	 volumeReduction();
    		 //flowMap.put(new Integer(i+1).toString(), new Double(flow));
    		 
        	 temp = "";
        	 temp = new Integer(i+1).toString() + ",";
        	 temp = temp + new Double(flow).toString() + ",";
        	 maxIndex = maxMatrix(percentageReductionFlow);
        	 temp = temp + new Double(percentageReductionFlow[maxIndex]).toString() + ",";
        	 temp = temp + new Double(volumeReductionFlow[maxIndex]).toString() + ",";
        	 maxIndex = maxMatrix(volumeReductionFlow);
        	 temp = temp + new Double(volumeReductionFlow[maxIndex]).toString() + ",";
        	 temp = temp + new Double(percentageReductionFlow[maxIndex]).toString();
        	 dosPeakFlows.writeBytes(temp);
        	 dosPeakFlows.writeBytes("\n");
        	 System.out.println(temp);
          	 System.out.println("\n");
        	 
    		 for(j=0;j<replace.length;j++)
    		 {
    				replace[j] = "0.000";
    		 }//for end
    		 replaceValuesInPndFile(i, replace);
    			 
    		 
    	 }//for end
    	 
    	 dosPeakFlows.close();
		 */
    	 
    	 
    	 
		/* 
    	 dosFlow = new DataOutputStream(new FileOutputStream(".\\flows.txt"));
    	 flowMap = sortByValue(flowMap);
         for (String key : flowMap.keySet()) {
         	temp = key + "	" + flowMap.get(key);
             //System.out.println("key/value: " + key + "/"+frequencyMap.get(key));
         	System.out.println(temp);
         	System.out.println("\n");
         	dosFlow.writeBytes(temp);
			dosFlow.writeBytes("\n");
         }
         dosFlow.close();
    	*/
    	 
    	 /*
    	 //processBaselineRchFileTab();
    	 processBaselineRchFile();
    	 processOutputRchFile();
    	 percentageReduction();
    	 volumeReduction();
    	 //printMatrix(maxFlowBaseline);
    	 //printMatrix(maxFlowOutput);
    	 avgMatrix(percentageReductionFlow);
    	 printMatrix(percentageReductionFlow);
    	 */
    	 
    	 //flow = processRchFile();
    	 //System.out.println("net flow is " + flow);
    	 
    	 
    	 //replaceValuesInPndFile(12, new String[] {"323.4558", "424.3545", "6.545"});
    	 
    	 
    	
     }//main end
		
		
		

		
		
		
		
	
		
		
	
}
