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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Map;
import java.util.StringTokenizer;


import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.*;
import java.io.Serializable;



/**
 * This class implements a fitness function based on the total area of wetlands used in the sub-basins.
 * A smaller value is better.
 * @author Meghna Babbar-Sebens, IUPUI, Dec 2010.
 * @version 1.0
 */
public class SWAT_BMPs_NitratesFitnessFunction implements FitnessFunction,Serializable {
	// application specific variables (mbabbars)
	// this variable stores a value of 1 if this fitness function is used in the optimization, else it stores a value of zero.
    int ffchosen;
    String nitrateExecFile;
    String nitrateOutFile;
    int noOfSubbasins = 130; // These are the total number of sub-basins in the SWAT model.
    int [] omittedSubBasins = new int[] {128, 129, 130};
    
	/**
	  * Constructor.
	  * 
	  * @param none (Params should be added in future (mbabbars))
	  */
   public SWAT_BMPs_NitratesFitnessFunction(int chosenFlag, String Fname, String OFname) {
		this.ffchosen = chosenFlag; 
                nitrateExecFile = new String (Fname);
                nitrateOutFile = new String (OFname);

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
          double [] NitObjectiveValue = readObjectiveValue (base);
          
          for (int i = 0; i < (this.noOfSubbasins - this.omittedSubBasins.length); i++ ) {
           aIndividual.subbasinsFF[i][4] =  -NitObjectiveValue [i+1]; // update sub-basin fitness function #4, which is for "Nitrates Reduction".
          }   
          //System.out.println ("Nitrate Reductions: " + NitObjectiveValue [0])  ;        
    	  return - NitObjectiveValue [0];// negative because objective functions are minimizing functions in the genetic algorithm.
      } else{
    	  return 0.0;
      }
   }
   
   // ************************************************
   // To run nitrate objective function Executable model
	public void runExec(String base) throws IOException{
	     Process p;
	     ProcessBuilder pb = new ProcessBuilder(base + this.nitrateExecFile);
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
                FileInputStream objFIS = new FileInputStream(base + this.nitrateOutFile);
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
                for (int i = 0; i< this.noOfSubbasins-this.omittedSubBasins.length; i++){
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
   
}