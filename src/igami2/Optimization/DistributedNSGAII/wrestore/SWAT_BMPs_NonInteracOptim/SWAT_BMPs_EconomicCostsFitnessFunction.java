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



import java.io.*;
import java.util.Map;



/**
 * This class implements a fitness function based on the total area of wetlands used in the sub-basins.
 * A smaller value is better.
 * @author Meghna Babbar-Sebens, IUPUI, Dec 2010.
 * @version 1.0
 */
public class SWAT_BMPs_EconomicCostsFitnessFunction implements FitnessFunction,Serializable {
	// application specific variables (mbabbars)
	// this variable stores a value of 1 if this fitness function is used in the optimization, else it stores a value of zero.
    int ffchosen;
    String economicExecFile;
    String economicOutFile; 
    int noOfSubbasins = 130; // These are the total number of sub-basins in the SWAT model.
    int [] omittedSubBasins = new int[] {128, 129, 130};
    int [] regionSubbasinId;
    int [] tenure_regionSubbasinId;
    int numTenureTypes = 3; // These are the total number of tenure types that can be modeled in the sub-basin.
    String [] BMPlookup;
    
	/**
	  * Constructor.
	  * 
	  * @param none (Params should be added in future (mbabbars))
	  */
	public SWAT_BMPs_EconomicCostsFitnessFunction(int chosenFlag, String [] BMPlookup, String Fname, String OFname, int[] tenure_regionSubbasinId, int[] regionSubbasinId) {
		this.ffchosen = chosenFlag; 
                this.BMPlookup = BMPlookup.clone();
                economicExecFile = new String (Fname);
                economicOutFile = new String (OFname);
                this.regionSubbasinId = regionSubbasinId.clone();
                this.tenure_regionSubbasinId = tenure_regionSubbasinId.clone();

	}
	
	
   /**
    * Evaluates the fitness value (total area of wetlands used in the sub-basins) of the specified individual.
    * 
    * @param individual individual
    * @return fitness value
    */
   public double evaluate(Individual individual, String base) throws IOException{
      if (individual == null) {
         throw new IllegalArgumentException("'individual' must not be null.");
      }
      if (!(individual instanceof SWAT_BMPs_Individual)) {
         throw new IllegalArgumentException("'individual' must be of type 'AssignmentIndividual'.");
      }
      
      SWAT_BMPs_Individual aIndividual = (SWAT_BMPs_Individual)individual;
      
       if (this.ffchosen == 1){
    	  // Run executable for nitrate objective function
          try {
	    runExec(base);
	  } catch (IOException ex) {
            ex.printStackTrace();
          }
          
          // open the fitness output file and read fitness function
          double [] econObjectiveValue = readObjectiveValue (base);
          
          for (int i = 0; i < (this.noOfSubbasins - this.omittedSubBasins.length); i++ ) {
           aIndividual.subbasinsFF[i][2] =  econObjectiveValue [i+1]; // update sub-basin fitness function #2, which is for "Economic Costs".
          }                      
          //System.out.println ("Economic Costs: " + econObjectiveValue [0]);
    	  return econObjectiveValue [0]; // The first one contains the overall watershed value
      } else{
    	  return 0.0;
      }
   }
   
   // ************************************************
   // To run nitrate objective function Executable model
	public void runExec(String base) throws IOException{
	     Process p;
	     ProcessBuilder pb = new ProcessBuilder(base + this.economicExecFile);
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
	}
        
    // *************************************************
    // Read objective function value
        public double [] readObjectiveValue (String base){
            double [] obj = new double [this.noOfSubbasins-this.omittedSubBasins.length+1];
            try{             
                FileInputStream objFIS = new FileInputStream(base + this.economicOutFile);
                InputStreamReader objISR = new InputStreamReader(objFIS);
                BufferedReader objBR = new BufferedReader(objISR);
                
                String headerData = new String ();
                String objData = new String ();
                String tokenString;
		StringTokenizer st;
                
                // read header
                headerData = objBR.readLine();
                // read watershed objective function value
                objData = objBR.readLine();
                obj[0] = new Double (objData.trim()).doubleValue();
                // read sub-basin objective function value
                for (int i = 0; i< this.regionSubbasinId.length; i++){
                    objData = objBR.readLine();
                    st = new StringTokenizer(objData, ",");
                    tokenString = st.nextToken();
		    int id = new Integer (tokenString.trim()).intValue();
                    tokenString = st.nextToken();
                    obj[id] = new Double (tokenString.trim()).doubleValue();
                    
                }
                
                objFIS.close();
                objISR.close();
                objBR.close();
                
            } catch (IOException ex) {
		ex.printStackTrace();
            }
            
            return obj;
                    
        }
        
    // *************************************************
    // Read objective function value. This is the old code - Not used anymore (delete after confirming the method above)
        public double [] readObjectiveValue_old (String base){
            double [] obj = new double [this.noOfSubbasins-this.omittedSubBasins.length+1];
            
            String [][][] costs = new String [this.noOfSubbasins-this.omittedSubBasins.length] [this.BMPlookup.length][this.numTenureTypes];
            for (int i = 0; i < this.noOfSubbasins-this.omittedSubBasins.length; i++){
                for (int j = 0; j < this.BMPlookup.length; j++){
                    for (int k = 0; k < this.numTenureTypes; k++){
                        costs[i][j][k] = "0.0";
                    }
                }
            }
            try{             
                FileInputStream objFIS = new FileInputStream(base + this.economicOutFile);
                InputStreamReader objISR = new InputStreamReader(objFIS);
                BufferedReader objBR = new BufferedReader(objISR);
                
                String headerData = new String ();
                String objData = new String ();
                String tokenString;
		StringTokenizer st;
                
                // read header
                headerData = objBR.readLine();
                while ((objData = objBR.readLine()) != null) 
		{
                   st = new StringTokenizer(objData, ",");
                   tokenString = st.nextToken();
		   int ID = new Integer (tokenString.trim()).intValue(); // ID of sub-basin
                   
                   tokenString = st.nextToken();
		   String BMP = new String (tokenString.trim()); // BMP.
                   
                   tokenString = st.nextToken();// read are which is not used
                   
                   String [] tenure_costs = new String [this.numTenureTypes];
                   for (int i=0; i< this.numTenureTypes; i++){
                        tokenString = st.nextToken();
                        tenure_costs[i] = new String (tokenString.trim()); // Tenure costs for this specific practice "BMP" in sub-basin "ID"
                   }
                   System.out.print(ID + " , " + BMP );                  
                   // Assign appropriate cost values to "costs[this.regionSubbasinId.length] [this.BMPlookup.length][this.numTenureTypes]" array
                   for (int i = 0; i < this.regionSubbasinId.length; i++){
                       if (ID == regionSubbasinId[i]){
                           for (int j=0; j < this.BMPlookup.length; j++){
                               if (BMP.matches(BMPlookup[j])){
                                   for (int k=0; k< this.numTenureTypes; k++){
                                       costs[ID-1][j][k]= new String(tenure_costs[k]);
                                       System.out.print(" , " + costs[ID-1][j][k] );
                                   }
                               }
                           }
                       }
                   }
                   System.out.println(" ");
                }// while loop
                
                objFIS.close();
                objISR.close();
                objBR.close();
                
            } catch (IOException ex) {
		ex.printStackTrace();
            }
            
            // Initialize obj[] array
            for (int i = 0; i < this.noOfSubbasins-this.omittedSubBasins.length+1; i++){
                obj[i] = 0.0;
            }
            
            // Calculate sub-basin costs based on tenure type by using
            // "costs[this.regionSubbasinId.length] [this.BMPlookup.length][this.numTenureTypes]" array
            for (int i = 1; i < this.noOfSubbasins-this.omittedSubBasins.length+1; i++){
                for (int j = 0; j < this.regionSubbasinId.length; j++){
                    if (i == regionSubbasinId[j]){ //i.e. if the ID of sub-basin matched the one in regionSubbasinId
                        for (int k=0; k < this.BMPlookup.length; k++){
                            obj[i]= obj[i] + new Double(costs[i-1][k][this.tenure_regionSubbasinId[j]-1]).doubleValue();
                        }
                    }
                }
            }
            
            // Now calculate obj[0], i.e. the overall watershed costs
            for (int i = 1; i < this.noOfSubbasins-this.omittedSubBasins.length+1; i++){
                obj[0] = obj[0] + obj[i];
                System.out.println("Costs sub-basin " + i + ": " + obj[i]);
            }
            System.out.println("Costs watershed: " + obj[0]);
            return obj;
                    
        }
   
   
}