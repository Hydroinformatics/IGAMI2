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


import java.io.Serializable;



/**
 * This class implements a fitness function based on the total area of wetlands used in the sub-basins.
 * A smaller value is better.
 * @author Meghna Babbar-Sebens, IUPUI, Dec 2010.
 * @version 1.0
 */
public class ModifySWATInputs_CallSWATexec implements Serializable {
	
	// *********** Application specific variables (mbabbars) *************
	   public int[] regionSubbasinId;// This consists of the SWAT IDs of sub-basins that are being optimized for wetland installation
	   public int[] chosenBMPs; // This stores flags for which BMPs are used to define the individuals.
	   public int [] chosenFF; // This stores the flags for which fitness functions are being used in the optimization.
	   public double[] assignments; // This array stores the actual decision variable values for all BMPs and sub-basins
           
	   public String base;//SWAT folder

	// **************** Variables related to BMPs ********************
           String [] BMPlookup = new String[]{"strip_cropping", "crop_rotation", "cover_crops", "filter_strips", "grassed_waterway", "conservation_tillage", "binary_wetlands", "variable_area_wetlands","variable_wetfr_wetlands","binary_ponds"}; 
           // Wetland Variables
	   String wetlandFile = "wetland.csv";
	   String[] wetlandLookup = new String[]{"WET_FR", "WET_MXSA", "WET_MXVOL", "WET_NSA", "WET_NVOL", "WET_VOL", "WET_K"};
	   boolean designDepth = true;
	   double designDepthValue = 0.5; // units = meters. This is used only if designDepth = true. Otherwise the natural depth is used obtained from wetland.csv file.
	   double wet_k = 50.000; // new wetland seepage saturated hydraulic conductivity in mm/hr
           double base_wet_k = 0.0; // baseline existing wetland seepage saturated hydraulic conductivity in mm/hr
	   
	   // Filter strip variables
           boolean filterInAgLand = true;
           String filterWVarName = new String("FILTERW");
           
           // Grassed waterway variables
           String grssWtrwyFile = new String("Streams_order_one.txt");
           String [] grssWtrwyVarNames = new String[] {"CH_N2", "CH_COV"};
           double CH_N2 = 0.1;
           double CH_COV = 0.001;
           
           // Cropping practices operations (strip cropping, crop rotation, cover crops, conservation tillage (zero-till))
           String corn_default_Fname = new String ("corn_default.txt"); // This is a default corn operations file with no strip cropping, no cover crops, generic fertilizer application, generic pesticide application, and conventional tillage
           String soy_default_Fname = new String ("soy_default.txt"); // This is a default soy operations file with no strip cropping, no cover crops, generic fertilizer application, generic pesticide application, and conventional tillage
           String corn_coverCrops_Fname = new String ("corn_covercrop_WW.txt"); // This is a corn operations file with no strip cropping, with cover crops, generic fertilizer application, generic pesticide application, and conventional tillage
           String soy_coverCrops_Fname = new String ("soy_covercrop_WW.txt"); // This is a soy operations file with no strip cropping, with cover crops, generic fertilizer application, generic pesticide application, and conventional tillage
           String corn_default_practice_str ;
           String soy_default_practice_str ;
           String corn_coverCrops_practice_str ;
           String soy_coverCrops_practice_str ;
                   
           int corn_cropID = 19; // This is the crop type CORN in crop.dat
           int soy_cropID = 56; // This is the crop type SOYB in crop.dat
           int corn_stripCropID = 98; // This is crop type CSCP in crop.dat, which consists of corn row crop grown in strips with a close growing crop. USLE_C,mn becomes 0.115 for this crop type.
	   int soy_stripCropID = 99; // This is crop type SSCP in crop.dat, which consists of soy row crop grown in strips with a close growing crop. USLE_C,mn becomes 0.115 for this crop type.
           double usle_p_stripCrop = 0.3; // has a value of 0.3 when strip cropping is chosen. 
           double usle_p_stripCrop_default = 0.6; //Default value when no strip cropping is done.
           public String [][] CN2baseline; // This array stores the baseline values of curve numbers for all HRUs
           String CN2filename = new String ("CN2.csv"); 
           int numRowsInCN2File = 2744; // Number of HRUs in CN2.csv
           int numColsInCN2File = 6; // Number of columns in CN2.csv
           
           int NROT = 5; // For number of rotations. Should be equal to number of years (e.g. 5 for years 2004 to 2008) when crop rotation is used.
           String nrotVarName = new String ("NROT");
           String mgt_op_endOfYear = new String ("                 0");
           
           String tillageOperation = new String (" 6"); // This is the mgt_op for tillage in operations schedule. Use two spaces to identify ID.
           String till_id = new String (" 4"); // The value is 4 for zero-till, value is 1 for conventional fall till and value is 2 for conventional spring till. Use two spaces to identify ID.
           private boolean VIDYA_DEBUG = false; //use to debug the Distreibuted System
        // ***************************************************************
	/**
	  * Constructor.
	  * 
	  * @param none (Params should be added in future (mbabbars))
	  */
	public ModifySWATInputs_CallSWATexec(SWAT_BMPs_Individual individual, String base) {
	      this.regionSubbasinId = individual.regionSubbasinId;
	      this.chosenBMPs = individual.chosenBMPs;
	      this.chosenFF = individual.chosenFF;
	      this.assignments = individual.assignments;
	      this.base = base;
              
              // Get all crop operations  
              corn_default_practice_str = new String (getCropOperations (this.base + corn_default_Fname));
              soy_default_practice_str = new String (getCropOperations (this.base + soy_default_Fname));
              corn_coverCrops_practice_str = new String (getCropOperations (this.base + corn_coverCrops_Fname));
              soy_coverCrops_practice_str = new String (getCropOperations (this.base + soy_coverCrops_Fname));
              
              // Get all baseline curve numbers for all HRUs
              try {
                CN2baseline = new String[numRowsInCN2File][numColsInCN2File];
                CN2baseline = readCN2Baseline (this.base, CN2filename);
              } catch (IOException ex) {
              ex.printStackTrace();
              }
        }
        
        // Method to actually write the SWAT input files and call the SWAT executable.
        public void writeInpExecSWAT (){
              
              String [][] subBasinOperations = new String [this.regionSubbasinId.length][2]; // For each sub-basin, 1st column for corn and second column for soy operations.
                          
              // ****************************************************
              // Write all the inputs for simulating the various BMPs
              // ****************************************************
              
              // Strip cropping variables CN and USLA_P variables
	      if ((chosenBMPs[0]==1) ){
                  try {
                    writeStripCropping_CN_USLEP_ToMgtFiles(CN2baseline, usle_p_stripCrop, usle_p_stripCrop_default, this.base);
                  }catch (IOException ex) {
                    ex.printStackTrace();
                  }
	      }
              
              // Beginning of operations schedules modifications %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%              
              // Write crop operations depending upon whether strip cropping, crop rotation, cover crops, and/or conservation tillage are/is used.
              // STEP 1) ***Check if cover crops are being used or not in this experiment.***
              if (chosenBMPs[2]==1){
                  // Initialize all sub-basin operations for corn and soybean to ones with cover crops
                  for (int i=0; i<this.regionSubbasinId.length; i++){
                      subBasinOperations[i][0] = new String (corn_coverCrops_practice_str);
                      subBasinOperations[i][1] = new String (soy_coverCrops_practice_str);
                  }
                  // Now check if the assignment array related to cover crops is 1 or 0 for a specific sub-basin. If it is 1, then cover crop implemented in that sub-basin and if it is 0  then cover crop not implemented.
                  int coverCropsStartIndex = 0;
                  for (int i=0; i < 2; i++){ // cover crops are the 3rd BMP, with i=2 in the chosenBMPs array.
			coverCropsStartIndex = coverCropsStartIndex + chosenBMPs[i];
                  }
		  coverCropsStartIndex = coverCropsStartIndex * this.regionSubbasinId.length;
                  for (int i=0; i<this.regionSubbasinId.length; i++){
                      if (assignments[i+coverCropsStartIndex] < 1.0){
                            // Go back to default non-cover crop-based operation for this sub-basin
                            subBasinOperations[i][0] = new String (corn_default_practice_str);
                            subBasinOperations[i][1] = new String (soy_default_practice_str);
                      }
                  }                 
              } 
              else { // for all other crop operations that are based on default crop (and does not include cover crops)
                  // Initialize all sub-basin operations for corn and soybean to default non-cover crop based plants
                  for (int i=0; i<this.regionSubbasinId.length; i++){
                      subBasinOperations[i][0] = new String (corn_default_practice_str);
                      subBasinOperations[i][1] = new String (soy_default_practice_str);
                  }
              }
              // STEP 2) ***Check if strip cropping is used.***
              if ((chosenBMPs[0]==1) ){
                  int stripCropStartIndex = 0;
		  for (int i=0; i<this.regionSubbasinId.length; i++){
                      if (assignments[i+stripCropStartIndex] > 0.0){
                            // Change main crop plant type to the one with strip crop
                            // i.e., corn (plant ID 19) to CSCP (plant ID 98)
                            StringBuilder data = new StringBuilder(subBasinOperations[i][0]);
                            int cropIndex = data.indexOf(" "+ new Integer (corn_cropID).toString()+ " ");
                            String stripCropId = new String (" "+ new Integer (corn_stripCropID).toString()+ " ");
                            for (int k = 0; k< stripCropId.length(); k++){
                                data.setCharAt(cropIndex, stripCropId.charAt(k));
                                cropIndex++;
                            }
                            subBasinOperations[i][0] = new String (data.toString());
                            //System.out.println( subBasinOperations[i][0]);
                            
                            // and, soy (plant ID 56) to SSCP (plant ID 99)   
                            data = new StringBuilder(subBasinOperations[i][1]);
                            cropIndex = data.indexOf(" "+ new Integer (soy_cropID).toString()+ " ");
                            stripCropId = new String (" "+ new Integer (soy_stripCropID).toString()+ " ");
                            for (int k = 0; k< stripCropId.length(); k++){
                                data.setCharAt(cropIndex, stripCropId.charAt(k));
                                cropIndex++;
                            }
                            subBasinOperations[i][1] = new String (data.toString());
                            //System.out.println( subBasinOperations[i][1]);
                      }
                  }                  
              }
              // STEP 3) ***Check if conservation tillage is used. (i.e. mgt_op = 6)***
   	      if (chosenBMPs[5]==1){
	    	  int consvTillageStartIndex = 0;
                  for (int i=0; i < 5; i++){ // cover crops are the 6th BMP, with i=5 in the chosenBMPs array.
			consvTillageStartIndex = consvTillageStartIndex + chosenBMPs[i];
                  }
		  consvTillageStartIndex = consvTillageStartIndex * this.regionSubbasinId.length; 
                  for (int i=0; i<this.regionSubbasinId.length; i++){
                      if (assignments[i+consvTillageStartIndex] > 0.0){
                          // CORN: Change tillage operation (with mgt_op = 6) to zero-till (i.e. till_id = 4) 
                          StringBuilder data = new StringBuilder(subBasinOperations[i][0]);
                          String mgtOp = new String ();
                          //System.out.println("data.length : "+ data.length());
                          for( int pos = 0; pos < data.length()-1; pos++){ 
                                char c = data.charAt(pos); 
                                if( c == '\r' || c== '\n' ) { 
                                      mgtOp = data.substring(pos+17, pos+19);
                                      //System.out.println("mgtOp : "+ mgtOp);
                                      if (mgtOp.matches(this.tillageOperation)){
                                         // System.out.println("A Match!!");
                                         data.replace(pos+22, pos+24, this.till_id); 
                                      }                                      
                                } 
                          } 
                          subBasinOperations[i][0] = new String(data.toString());
                          //System.out.println("subBasinOperations[i][0] after till:");
                          //System.out.println(subBasinOperations[i][0]);
                                               
                          // SOYBEAN: Change tillage operation (with mgt_op = 6) to zero-till (i.e. till_id = 4) 
                          data = new StringBuilder(subBasinOperations[i][1]);
                          mgtOp = new String ();
                          for( int pos = 0; pos < data.length(); pos++){ 
                                char c = data.charAt(pos); 
                                if( c == '\r' || c== '\n' ) { 
                                      mgtOp = data.substring(pos+17, pos+19);
                                      if (mgtOp.matches(this.tillageOperation)){
                                         // System.out.println("A Match!!");
                                         data.replace(pos+22, pos+24, this.till_id); 
                                      }                                         
                                } 
                          } 
                          subBasinOperations[i][1] = new String(data.toString());
                          //System.out.println("subBasinOperations[i][1] after till:");
                          //System.out.println(subBasinOperations[i][1]);  
                          //System.out.println("*******");              
                      }
                  }
                      
	      }            
              // STEP 4) ***Check if crop rotation is used.***
              if (chosenBMPs[1]==1){
	    	  int cropRotStartIndex = 0;
                  for (int i=0; i < 1; i++){ // crop rotations are the 2nd BMP, with i=1 in the chosenBMPs array.
			cropRotStartIndex = cropRotStartIndex + chosenBMPs[i];
                  }
		  cropRotStartIndex = cropRotStartIndex * this.regionSubbasinId.length; 
                  for (int i=0; i<this.regionSubbasinId.length; i++){
                      if (assignments[i+cropRotStartIndex] > 0.0){
                          // Commented use of changeNRotInMgtFiles by Meghna 6/14/2012
                          // All files will have NROT = 5, whether same crop is used in all 5 years, or alternate crops are used in all 5 years.
                          /**try {
                            changeNRotInMgtFiles (this.NROT, regionSubbasinId[i], this.base);
                          } catch (IOException ex) {
                            ex.printStackTrace();
                          }**/
                          
                          String cornOper = new String (subBasinOperations[i][0]);
                          String soyOper = new String (subBasinOperations[i][1]);
                          StringBuilder cornTempOp = new StringBuilder (subBasinOperations[i][0]);
                          StringBuilder soyTempOp = new StringBuilder (subBasinOperations[i][1]);
                          boolean appendother = true; 
                          for (int k=0; k<this.NROT-1; k++){
                            if (appendother == true){
                                cornTempOp.append("\n"+ mgt_op_endOfYear + "\n"+ soyOper);
                                soyTempOp.append("\n"+ mgt_op_endOfYear + "\n"+ cornOper);
                                appendother = false;
                            } else {
                                cornTempOp.append("\n"+ mgt_op_endOfYear + "\n"+ cornOper);
                                soyTempOp.append("\n"+ mgt_op_endOfYear + "\n"+ soyOper);
                                appendother = true;
                            }
                          }
                          cornTempOp.append("\n");
                          soyTempOp.append("\n");
                          subBasinOperations[i][0] = new String (cornTempOp.toString()); 
                          subBasinOperations[i][1] = new String (soyTempOp.toString()); 
                                        
                      } else { // if (assignments[i+cropRotStartIndex] == 0.0)
                          
                          // Commented use of changeNRotInMgtFiles by Meghna 6/14/2012
                          // All files will have NROT = 5, whether same crop is used in all 5 years, or alternate crops are used in all 5 years.
                          /**
                          try {
                            changeNRotInMgtFiles (1, regionSubbasinId[i], this.base);
                          } catch (IOException ex) {
                            ex.printStackTrace();
                          }**/
                          
                          String cornOper = new String (subBasinOperations[i][0]);
                          String soyOper = new String (subBasinOperations[i][1]);
                          StringBuilder cornTempOp = new StringBuilder (subBasinOperations[i][0]);
                          StringBuilder soyTempOp = new StringBuilder (subBasinOperations[i][1]);
                          for (int k=0; k<this.NROT-1; k++){
                                cornTempOp.append("\n"+ mgt_op_endOfYear + "\n"+ cornOper); // add the same operation for full NROT-1 additional years.
                                soyTempOp.append("\n"+ mgt_op_endOfYear + "\n"+ soyOper);// add the same operation for full NROT-1 additional years.
                          }
                          cornTempOp.append("\n");
                          soyTempOp.append("\n");
                          subBasinOperations[i][0] = new String (cornTempOp.toString()); 
                          subBasinOperations[i][1] = new String (soyTempOp.toString()); 
                          
                      }
                      //System.out.println(subBasinOperations[i][0]);  
                      //System.out.println(subBasinOperations[i][1]);  
                      //System.out.println("*******"); 
                  }
	      } else{ // If crop rotation is not chosen. The default crop is repeated for all years.
                  // Make all 5 years with repetitions of default crop type.
                  for (int i=0; i<this.regionSubbasinId.length; i++){
                      String cornOper = new String (subBasinOperations[i][0]);
                      String soyOper = new String (subBasinOperations[i][1]);
                      StringBuilder cornTempOp = new StringBuilder (subBasinOperations[i][0]);
                      StringBuilder soyTempOp = new StringBuilder (subBasinOperations[i][1]);
                      for (int k=0; k<this.NROT-1; k++){
                            cornTempOp.append("\n"+ mgt_op_endOfYear + "\n"+ cornOper); // add the same operation for full NROT-1 additional years.
                            soyTempOp.append("\n"+ mgt_op_endOfYear + "\n"+ soyOper);// add the same operation for full NROT-1 additional years.
                      }
                      cornTempOp.append("\n");
                      soyTempOp.append("\n");
                      subBasinOperations[i][0] = new String (cornTempOp.toString()); 
                      subBasinOperations[i][1] = new String (soyTempOp.toString()); 
                  }
              }            
              // STEP 5) In the end, write all the operations to the management files (.mgt) of all sub-basins.
              if ((chosenBMPs[0]==1) || (chosenBMPs[1]==1) || (chosenBMPs[2]==1) || (chosenBMPs[5]==1)){
                try {
                        writeOperationsToMgtFiles (subBasinOperations, this.base);
                } catch (IOException ex) {
                        ex.printStackTrace();
                }
              }       
              // End of operation schedule modifications %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                      
              // Filter strips
	      if (chosenBMPs[3]==1){
	    	  writeFilterStripsInputs(base);
	      }
              
              // grassed waterways
	      if (chosenBMPs[4]==1){
	    	  writeGrassedWaterwayInputs(base);
	      }

              // Binary wetlands
	      if (chosenBMPs[6]==1){
	    	  writeBinaryWetlandsInputs(base);
	      }
	      // When variable area wetlands are used with maximum Wet_fr values found for potential wetlands.
	      if ((chosenBMPs[7]==1) && (chosenBMPs[8] == 0)){
	    	  writeVariableAreaWetlandsInputs(base);
	      }
	      // When variable area wetlands are used with variable Wet_fr values.
	      if ((chosenBMPs[7]==1) && (chosenBMPs[8] == 1)){
	    	  writeVariableAreaVariableWetfrWetlandsInputs(base);
	      }
              
              // Binary ponds
	      if (chosenBMPs[9]==1){
	    	  writeBinaryPondsInputs(base);
	      }
	      //System.out.println("Inputs writing ends");
	      // After writing all the inputs, now run the SWAT program.
	      try {
                  if(!VIDYA_DEBUG) //if not in debug mode
                  {
                      runSWATexec(base);
                  }
	      } catch (IOException ex) {
              ex.printStackTrace();
              }
	}
	   
// String [] BMPlookup = new String[]{"strip cropping", "conservation crop rotation", "cover crops", "filter strips", "grassed waterway", "conservation tillage", "binary wetlands", "variable area wetlands","variable Wetfr wetlands","binary ponds"}; 
	// **************************************************************************
        // To obtain all cropping operations
        public String getCropOperations (String Fname){
            String operData;
            StringBuilder data = new StringBuilder();
            try {
                FileInputStream operFIS = new FileInputStream(Fname);
                InputStreamReader operISR = new InputStreamReader(operFIS);
                BufferedReader operBR = new BufferedReader(operISR);

                int counter = 0;
                while ((operData = operBR.readLine()) != null) 
                {//read entire contents of pond file in data variable

                    //System.out.println(operData);
                    if (counter==0) { data = data.append(operData); counter++;}
                    else data = data.append("\n" + operData);

                }//oper data
                operFIS.close();
                operISR.close();
                operBR.close();//close operation file 
            } catch (IOException ex) {
		ex.printStackTrace();
            }
            //System.out.println(data.toString());
            return data.toString();
        }
        
        // *******************************************************************
        // Change the Nrotation in .mgt files.
        public void changeNRotInMgtFiles (int nrot, int subbasinID, String base) throws IOException {      
            //change contents of the input file
            int counter = 0, end = 0,i,j, index;
            String temp = "";
			 
            // For all .sub files that store the names of the HRU .mgt files
            FileInputStream subFIS;
            InputStreamReader subISR;
            BufferedReader subBR;
            DataInputStream subDIS;
            String subData;

            // For all the .mgt files for every HRU
            FileInputStream mgtFIS;
            InputStreamReader mgtISR;
            BufferedReader mgtBR;
            DataInputStream mgtDIS;
            String mgtData;

            File mgtF;
            Writer mgtW;


            counter = 0;

            // create the name of the subbasin .sub file which will be opened to read the names of various .mgt files.	 
            String relevantSubbasinsData = new Integer(subbasinID).toString();
            j = relevantSubbasinsData.length();
            j = 5 - j;
            for(i=0;i<j;i++)
            {
                temp = temp + "0";
            }
            relevantSubbasinsData = temp + relevantSubbasinsData;
            relevantSubbasinsData = relevantSubbasinsData + "0000.sub";//subbasin id + 0000.sub file

            // Now read all the lines in .sub file for this sub-basin, search for .mgt file names	 
            subFIS = new FileInputStream(base + relevantSubbasinsData);
            subISR = new InputStreamReader(subFIS);
            subBR = new BufferedReader(subISR);	
            int numMgtFiles = 0;
            StringBuilder mgtFNames = new StringBuilder ();
            while ((subData = subBR.readLine()) != null) 
            {
                if (subData.contains(".mgt")){
                    numMgtFiles ++;
                    mgtFNames.append(subData.substring(13, 26)); // .mgt file name is stored from character index 13 to character index 25
                    //System.out.println(subData.substring(13, 26));
                } 	
            }
            //System.out.println("Sub-basin .mgt file names: ");
            //System.out.println(mgtFNames.toString());		 	
            subFIS.close();
            subISR.close();
            subBR.close();//close .sub file 

            // Convert NROT value to a string.
            String nrotStr = new Integer(nrot).toString(); 

            // open individual .mgt file in read mode, read the entire file and replace nrot value with new value.                  
            for(j=0;j<numMgtFiles;j++)
            {
                mgtFIS = new FileInputStream(base + mgtFNames.substring(j*13, (j*13)+13));
                mgtISR = new InputStreamReader(mgtFIS);
                mgtBR = new BufferedReader(mgtISR);
                StringBuilder data = new StringBuilder();
                counter = 0;
                while ((mgtData = mgtBR.readLine()) != null) 
                {//read entire contents of pond file in data variable	    	
                    //System.out.println(pndData);
                    if (counter==0) { data = data.append(mgtData); counter++;}
                    else data = data.append("\n" + mgtData);

                }//mgt data

                mgtFIS.close();
                mgtISR.close();
                mgtBR.close();//close .mgt file .. later open in write mode
                
                // modify NROT, only if the original landuse is corn or soybean
                int luseIndex = 0;
                luseIndex = data.indexOf("Luse:");
                if ((data.substring(luseIndex+5, luseIndex+9).matches("CORN"))|| (data.substring(luseIndex+5, luseIndex+9).matches("SOYB"))){
                    end = data.indexOf(this.nrotVarName);
                    //System.out.println(data.charAt(end));
                    end = end - 7; //end points to the last digit of the value
                    //System.out.println(data.charAt(end));
                    for(i=nrotStr.length()-1;i>=0;i--)
                    {
                        data.setCharAt(end, nrotStr.charAt(i));
                        end = end - 1;
                    }
                    while(data.charAt(end)!= ' ')
                    {
                        data.setCharAt(end, ' ');
                        end = end - 1;
                    }
                }
                data.append("\n");
                data.append("\n"); 
                // Now open the .mgt file in write mode and write the updated data (updated only for corn or soyb land use)
                mgtF = new File(base + mgtFNames.substring(j*13, (j*13)+13));
                mgtW = new BufferedWriter(new FileWriter(mgtF));
                mgtW.write(data.toString());
                mgtW.close();
            }//for numMgtFiles
            
        }
        
        // *******************************************************************
        // Write the opertions schedule in.Mgt files
        public void writeOperationsToMgtFiles (String [][] subBasinOperations, String base) throws IOException {
            //change contents of the input file
            int counter = 0, end = 0,i,j, index;
            String temp = "";

            // Outermost loop for sub-basins
            for (int subs = 0; subs < this.regionSubbasinId.length; subs++){
                // For all .sub files that store the names of the HRU .mgt files
                FileInputStream subFIS;
                InputStreamReader subISR;
                BufferedReader subBR;
                DataInputStream subDIS;
                String subData;
                
                int subbasinID = regionSubbasinId[subs];
                temp = "";
                // create the name of the subbasin .sub file which will be opened to read the names of various .mgt files.	 
                String relevantSubbasinsData = new Integer(subbasinID).toString();
                j = relevantSubbasinsData.length();
                j = 5 - j;
                for(i=0;i<j;i++)
                {
                    temp = temp + "0";
                }
                relevantSubbasinsData = temp + relevantSubbasinsData;
                relevantSubbasinsData = relevantSubbasinsData + "0000.sub";//subbasin id + 0000.sub file

                // Now read all the lines in .sub file for this sub-basin, search for .mgt file names	 
                subFIS = new FileInputStream(base + relevantSubbasinsData);
                subISR = new InputStreamReader(subFIS);
                subBR = new BufferedReader(subISR);	
                int numMgtFiles = 0;
                StringBuilder mgtFNames = new StringBuilder ();
                while ((subData = subBR.readLine()) != null) 
                {
                    if (subData.contains(".mgt")){
                        numMgtFiles ++;
                        mgtFNames.append(subData.substring(13, 26)); // .mgt file name is stored from character index 13 to character index 25
                        //System.out.println(subData.substring(13, 26));
                    } 	
                }
                //System.out.println("Sub-basin .mgt file names: ");
                //System.out.println(mgtFNames.toString());		 	
                subFIS.close();
                subISR.close();
                subBR.close();//close .sub file 

                // open individual .mgt file in read mode, read the entire file and replace nrot value with new value.                  
                for(j=0;j<numMgtFiles;j++)
                {
                    // For all the .mgt files for every HRU
                    FileInputStream mgtFIS= new FileInputStream(base + mgtFNames.substring(j*13, (j*13)+13));
                    InputStreamReader mgtISR = new InputStreamReader(mgtFIS);
                    BufferedReader mgtBR = new BufferedReader(mgtISR);
                    DataInputStream mgtDIS;
                    String mgtData;

                    File mgtF;
                    Writer mgtW;

                    StringBuilder data = new StringBuilder();

                    counter = 0;
                    while ((mgtData = mgtBR.readLine()) != null) 
                    {//read entire contents of pond file in data variable	    	
                        //System.out.println(pndData);
                        if (counter==0) { data = data.append(mgtData); counter++;}
                        else data = data.append("\n" + mgtData);

                    }//mgt data

                    mgtFIS.close();
                    mgtISR.close();
                    mgtBR.close();//close .mgt file .. later open in write mode

                    // modify operations, only if the original landuse is corn or soybean
                    int luseIndex = 0;
                    luseIndex = data.indexOf("Luse:");
                    if (data.substring(luseIndex+5, luseIndex+9).matches("CORN")){ 
                        //System.out.println("data corn luse: " + data.substring(luseIndex+5, luseIndex+9));
                        end = data.indexOf("Operation Schedule:");
                        data.setCharAt(end+19, '\n');
                        data.delete(end+20, data.length());
                        data.append(subBasinOperations[subs][0]);
                        //System.out.println(data.toString());
                    }
                    if (data.substring(luseIndex+5, luseIndex+9).matches("SOYB")){
                        end = data.indexOf("Operation Schedule:") + 19;
                        data.setCharAt(end, '\n');
                        data.delete(end+1, data.length());
                        data.append(subBasinOperations[subs][1]);                       
                    }
                    data.append("\n");
                    data.append("\n");
                    //System.out.println(data.toString());
                    //System.out.println("********");
                    // Now open the .mgt file in write mode and write the updated data (updated only for corn or soyb land use)
                    mgtF = new File(base + mgtFNames.substring(j*13, (j*13)+13));
                    mgtW = new BufferedWriter(new FileWriter(mgtF));
                    mgtW.write(data.toString());
                    mgtW.close();
                    //System.out.println("********");
                }//for numMgtFiles  
            }// for outermost loop for sub-basins
        }
        
        // ******************************************
        public String[][] readCN2Baseline (String base, String CN2filename) throws IOException{
            	FileInputStream cn2FIS;
		InputStreamReader cn2ISR;
		BufferedReader cn2BR;
		DataInputStream cn2DIS;
		String cn2Data;
		String tokenString;
		StringTokenizer st;
		Integer id;
		String [][] baselineCN2Data = new String [this.numRowsInCN2File][this.numColsInCN2File]; 		
				
		int counter = 0;
		int ctr = 0;		
		cn2FIS = new FileInputStream(base+CN2filename);
		cn2ISR = new InputStreamReader(cn2FIS);
		cn2BR = new BufferedReader(cn2ISR);
						
		while ((cn2Data = cn2BR.readLine()) != null) 
		{
			if(counter == 0) {counter++;} // For header line
			else
				{
					st = new StringTokenizer(cn2Data, ",");
                                        for (int k=0; k< this.numColsInCN2File; k++){
                                            tokenString = st.nextToken();
                                            baselineCN2Data[ctr][k]= new String (tokenString);
                                        }
                                        //System.out.println();
					ctr++;
						
				}//end else
		}//end while
		
		return baselineCN2Data;
        }
        // ******************************************
	// To write inputs for strip cropping BMP
	public void writeStripCropping_CN_USLEP_ToMgtFiles(String[][] baselineCN2Data, double strip_USLE_P,double strip_USLE_P_default, String base) throws IOException {
            //change contents of the input file
            int counter = 0, end = 0,i,j, index;
            int cn2end, usle_p_end;
            String temp = "";

           /* for (i=0; i< baselineCN2Data.length; i++){
                for (j = 0; j < baselineCN2Data[0].length; j++){
                    System.out.print(baselineCN2Data[i][j] + " ");
                }
                System.out.println();
            }*/
            // Outermost loop for sub-basins (if assignments[subs] == 1, then new values of CN2 and usle_p related to strip cropping will be inserted, otherwise default values of CN2 and usle_p will be inserted.
            for (int subs = 0; subs < this.regionSubbasinId.length; subs++){
                // For all .sub files that store the names of the HRU .mgt files
                FileInputStream subFIS;
                InputStreamReader subISR;
                BufferedReader subBR;
                DataInputStream subDIS;
                String subData;
                
                int subbasinID = regionSubbasinId[subs];
                temp = "";
                // create the name of the subbasin .sub file which will be opened to read the names of various .mgt files.	 
                String relevantSubbasinsData = new Integer(subbasinID).toString();
                j = relevantSubbasinsData.length();
                j = 5 - j;
                for(i=0;i<j;i++)
                {
                    temp = temp + "0";
                }
                relevantSubbasinsData = temp + relevantSubbasinsData;
                relevantSubbasinsData = relevantSubbasinsData + "0000.sub";//subbasin id + 0000.sub file

                // Now read all the lines in .sub file for this sub-basin, search for .mgt file names	 
                subFIS = new FileInputStream(base + relevantSubbasinsData);
                subISR = new InputStreamReader(subFIS);
                subBR = new BufferedReader(subISR);	
                int numMgtFiles = 0;
                StringBuilder mgtFNames = new StringBuilder ();
                while ((subData = subBR.readLine()) != null) 
                {
                    if (subData.contains(".mgt")){
                        numMgtFiles ++;
                        mgtFNames.append(subData.substring(13, 26)); // .mgt file name is stored from character index 13 to character index 25
                        //System.out.println(subData.substring(13, 26));
                    } 	
                }
                //System.out.println("Sub-basin .mgt file names: ");
                //System.out.println(mgtFNames.toString());		 	
                subFIS.close();
                subISR.close();
                subBR.close();//close .sub file 

                // open individual .mgt file in read mode, read the entire file and replace nrot value with new value.                  
                for(j=0;j<numMgtFiles;j++)
                {
                    // For all the .mgt files for every HRU
                    //System.out.println("mgtFNames.substring(j*13, (j*13)+13)" + mgtFNames.substring(j*13, (j*13)+13));
                    FileInputStream mgtFIS= new FileInputStream(base + mgtFNames.substring(j*13, (j*13)+13));
                    InputStreamReader mgtISR = new InputStreamReader(mgtFIS);
                    BufferedReader mgtBR = new BufferedReader(mgtISR);
                    DataInputStream mgtDIS;
                    String mgtData;

                    File mgtF;
                    Writer mgtW;

                    StringBuilder data = new StringBuilder();

                    counter = 0;
                    while ((mgtData = mgtBR.readLine()) != null) 
                    {//read entire contents of pond file in data variable	    	
                        //System.out.println(pndData);
                        if (counter==0) { data = data.append(mgtData); counter++;}
                        else data = data.append("\n" + mgtData);

                    }//mgt data
                    mgtFIS.close();
                    mgtISR.close();
                    mgtBR.close();//close .mgt file .. later open in write mode

                    // read CN2 
                    String HRUid = new String();
                    String SHLname = new String();
                    String newCN2 = new String ();
                    
                    int subIndex = 0, luseIndex = 0;
                    int hruIndex = 0;
                    subIndex = data.indexOf("Subbasin:");
                    luseIndex = data.indexOf("Luse:");
                    SHLname = data.substring(subIndex, luseIndex);
                    hruIndex = SHLname.indexOf("HRU:");
                    HRUid = data.substring(subIndex + hruIndex + 4, luseIndex);
                    
                    for (int k = 0; k< baselineCN2Data.length; k++){
                        if (Integer.parseInt(baselineCN2Data[k][0]) == regionSubbasinId[subs]){
                            Integer.parseInt(baselineCN2Data[k][1].trim());
                            Integer.parseInt(HRUid.trim());
                            if (Integer.parseInt(baselineCN2Data[k][1].trim()) == Integer.parseInt(HRUid.trim())){
                               newCN2 = baselineCN2Data[k][5].trim();
                            }
                        }
                    }
                    
                    // Only for corn and soybean HRUs 
                    if (data.substring(luseIndex+5, luseIndex+9).matches("CORN") || (data.substring(luseIndex+5, luseIndex+9).matches("SOYB"))){ 
                        double CN2double = Double.valueOf(newCN2.trim());
                        if (this.assignments[subs] == 1){
                            CN2double = CN2double - 3; // Lower CN if this sub-basin has strip cropping.
                        }
                        newCN2 = new String (Double.toString(CN2double));
                    }
                    cn2end = data.indexOf("CN2");
                    usle_p_end = data.indexOf("USLE_P"); 
                    cn2end = cn2end - 7; //end points to the last digit of the value
                    usle_p_end = usle_p_end - 7; //end points to the last digit of the value
                    
                    //Now write the updated CN2
                    for(i=newCN2.length()-1;i>=0;i--)
                    {
                        
                        data.setCharAt(cn2end, newCN2.charAt(i));
                        cn2end = cn2end - 1;
                    }
                    while(data.charAt(cn2end)!= ' ')
                    {
                        data.setCharAt(cn2end, ' ');
                        cn2end = cn2end - 1;
                    }
                    
                    //Now write the updated USLE_P
                    if (data.substring(luseIndex+5, luseIndex+9).matches("CORN") || (data.substring(luseIndex+5, luseIndex+9).matches("SOYB"))){                         
                        String newUSLE_P = Double.toString(strip_USLE_P);
                        if (this.assignments[subs] == 0){
                            // revert back to default value if this sub-basin does not have strip cropping 
                            newUSLE_P = Double.toString(strip_USLE_P_default);
                        }
                        for(i=newUSLE_P.length()-1;i>=0;i--)
                        {
                            data.setCharAt(usle_p_end, newUSLE_P.charAt(i));
                            usle_p_end = usle_p_end - 1;
                        }
                        while(data.charAt(usle_p_end)!= ' ')
                        {
                            data.setCharAt(usle_p_end, ' ');
                            usle_p_end = usle_p_end - 1;
                        }
                    }
                    data.append("\n");
                    data.append("\n"); 
                    //System.out.println(data.toString());
                    //System.out.println("********"); 
                    // Now open the .mgt file in write mode and write the updated data (updated only for corn or soyb land use)
                    mgtF = new File(base + mgtFNames.substring(j*13, (j*13)+13));
                    mgtW = new BufferedWriter(new FileWriter(mgtF));
                    mgtW.write(data.toString());
                    mgtW.close();

                }//for numMgtFiles  
            }// for outermost loop for sub-basins		 
	}
	
	// ******************************************
	// To write inputs for Filter Strips BMP
	public void writeFilterStripsInputs(String base){
            // Calculate from which starting index location in the assignments array are the decision variables for filter strips stored.
		int filterStripStartIndex = 0;
		for (int i=0; i < 3; i++){ // filter strips are the 4th BMP, with i=3 in the chosenBMPs array.
			filterStripStartIndex = filterStripStartIndex + chosenBMPs[i];
		}
		filterStripStartIndex = filterStripStartIndex * this.regionSubbasinId.length;
                          
            // Open .mgt files for every HRU in every sub-basin in regionSubbasinId and write filter strip width value specific to the sub-basin of that .mgt file.
	        for(int i=0; i<this.regionSubbasinId.length; i++){			        
                    try {
				 replaceSubBasinFilterwInMgtFiles(regionSubbasinId[i], assignments[i+filterStripStartIndex], base);  
                    } catch (IOException ex) {
				 ex.printStackTrace();
		    }
                }
        }
	
	// ******************************************
	// To write inputs for Grassed Waterway BMP
	public void writeGrassedWaterwayInputs(String base){
            // Calculate from which starting index location in the assignments array are the decision variables for grassed waterways stored.
		int grassedWaterwaysStartIndex = 0;
		for (int i=0; i < 4; i++){ // grassed waterways are the 5th BMP, with i=4 in the chosenBMPs array.
			grassedWaterwaysStartIndex = grassedWaterwaysStartIndex + chosenBMPs[i];
		}
		grassedWaterwaysStartIndex = grassedWaterwaysStartIndex * this.regionSubbasinId.length;
                
            // Open file with IDs of order 1 streams (and sub-basins) that will be allowed to have grassed waterways and identify which subbasins have grassed waterways
                boolean [] grssWtrwySubbasin = new boolean [regionSubbasinId.length];
                try {
                    FileInputStream grssWtrwyFIS = new FileInputStream(base + this.grssWtrwyFile);
                    InputStreamReader grssWtrwyISR = new InputStreamReader(grssWtrwyFIS);
                    BufferedReader grssWtrwyBR = new BufferedReader(grssWtrwyISR);
                    String grssWtrwyData = new String ();

                    while ((grssWtrwyData = grssWtrwyBR.readLine()) != null) 
                    {
			
			String subID = grssWtrwyData.toString(); // string for sub-basin ID
                        //System.out.println(new Integer (subID).intValue());
                        // now find this sub-basin in regionSubbasinId array
                        for (int i = 0; i<this.regionSubbasinId.length; i++){
                            if (new Integer (subID).intValue() == this.regionSubbasinId[i]){
                                grssWtrwySubbasin[i] = true;	
                            }
                        }
                    }//end while
                } catch (IOException ex) {
				 ex.printStackTrace();
		}                
                
            // Open .mgt files for every HRU in every sub-basin and write grass waterway parameters values specific to the sub-basin of that .rte file.
	        for(int i=0; i<this.regionSubbasinId.length; i++){
                    if ((grssWtrwySubbasin [i] == true) && (assignments[i+grassedWaterwaysStartIndex] > 0.0)){
                        try {
				 replaceGrassedWaterwayParamsInRteFiles(regionSubbasinId[i], CH_N2, CH_COV, base);  
                        } catch (IOException ex) {
				 ex.printStackTrace();
                        }
                    }
                }
	}
		
	// ******************************************
	// To write inputs for Binary Wetlands BMP. Maximum wet_fr and wetland areas are used for binary wetlands
	public void writeBinaryWetlandsInputs(String base){
		double [][] baselineWetlandData = new double [this.regionSubbasinId.length][3];
		double [][] potentialWetlandData = new double [this.regionSubbasinId.length][3];
		double [][] newWetlandData = new double [this.regionSubbasinId.length][3];
                double [] wet_k_array = new double [this.regionSubbasinId.length];
		String[] replace = new String[this.wetlandLookup.length];
		
		// First, read baseline (existing values of "WET_FR", "WET_MXSA", "WET_MXVOL") wetland data from wetland.csv
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
		
		// Third, initialize new values for wetland variables ("WET_FR", "WET_MXSA", "WET_MXVOL") to 0.0
		for(int i=0;i<this.regionSubbasinId.length;i++){
			newWetlandData[i][0] = 0.0; // initialize new "WET_FR" to 0.0
			newWetlandData [i][1]=0.0; // initialize "WET_MXSA" to 0.0
			newWetlandData[i][2] = 0.0;	// 	initialize "WET_MXVOL" to 0.0
                        wet_k_array[i] = this.wet_k; // initialize hydraulic conductivity to new value.
		}
		
		// Fourth, calculate from which starting index location in the assignments array are the decision variables for binary wetlands stored.
		int binWetStartIndex = 0;
		for (int i=0; i < 6; i++){ // binary wetlands are the 7th BMP, with i=6 in the chosenBMPs array.
			binWetStartIndex = binWetStartIndex + chosenBMPs[i];
		}
		binWetStartIndex = binWetStartIndex * this.regionSubbasinId.length;
		
		// Fifth, estimate new values for wetland variables: "WET_FR", "WET_MXSA", "WET_MXVOL", "WET_K"
		int j=0;
		for(int i=binWetStartIndex; i<(binWetStartIndex + this.regionSubbasinId.length); i++){
			if (this.assignments[i] == 1){
				// estimate new "WET_FR"
				newWetlandData[j][0] = baselineWetlandData[j][0] + potentialWetlandData[j][0];
				// estimate new "WET_MXSA"
				newWetlandData[j][1] = baselineWetlandData[j][1] + potentialWetlandData[j][1];
				//estimate new "WET_MXVOL"
				// Check whether volume should be based on a design depth or the natural representative depth.
				if (this.designDepth == false){
					// new volume = existing volume + maximum potential volume based on natural depressions
					newWetlandData[j][2] = baselineWetlandData[j][2] + potentialWetlandData[j][2];
				} else{
					// new volume = existing volume + maximum potential area * design depth
					newWetlandData[j][2] = baselineWetlandData[j][2] + potentialWetlandData[j][1]*this.designDepthValue ;
				}
			} else {
				// new "WET_FR" = existing "WET_FR"
				newWetlandData[j][0] = baselineWetlandData[j][0] ;
				// new "WET_MXSA" = existing "WET_MXSA"
				newWetlandData[j][1] = baselineWetlandData[j][1] ;
				// new "WET_MXVOL"	= existing 	"WET_MXVOL"	
				newWetlandData[j][2] = baselineWetlandData[j][2] ;
                                // new wet_k = 0.0
                                wet_k_array[j] = this.base_wet_k;
			}
			j++;
		}
		
		// Sixth, write values of new wetland variables in simulation pond files (.pnd)
		for(int i=0;i<this.regionSubbasinId.length;i++){
			for(j=0;j<replace.length-4;j++)
			 {
				 replace[j] = new Double(newWetlandData[i][j]).toString();
				 
			 }//for end
			 replace[replace.length-4] = "0.000";// "WET_NSA" 
			 replace[replace.length-3] = "0.000";// "WET_NVOL"
			 replace[replace.length-2] = "0.000";// "WET_VOL"
			 replace[replace.length-1] = new Double(wet_k_array[i]).toString();//"WET_K" 
			 try {
				 replaceValuesInPndFile(regionSubbasinId[i], replace,base);
			 } catch (IOException ex) {
				 ex.printStackTrace();
		     }
		}
		
	}
	
	// ***********************************************
	// To write inputs for Variable Area Wetlands BMP
	public void writeVariableAreaWetlandsInputs(String base){
		double [][] baselineWetlandData = new double [this.regionSubbasinId.length][3];
		double [][] potentialWetlandData = new double [this.regionSubbasinId.length][3];
		double [][] newWetlandData = new double [this.regionSubbasinId.length][3];
                double [] wet_k_array = new double [this.regionSubbasinId.length];
		String[] replace = new String[this.wetlandLookup.length];
		
		// First, read baseline (existing values of "WET_FR", "WET_MXSA", "WET_MXVOL") wetland data from wetland.csv
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
		
		// Third, initialize new values for wetland variables ("WET_FR", "WET_MXSA", "WET_MXVOL") to 0.0
		for(int i=0;i<this.regionSubbasinId.length;i++){
			newWetlandData[i][0] = 0.0; // initialize new "WET_FR" to 0.0
			newWetlandData [i][1]=0.0; // initialize "WET_MXSA" to 0.0
			newWetlandData[i][2] = 0.0;	// 	initialize "WET_MXVOL" to 0.0
                        wet_k_array[i] = this.wet_k; // initialize hydraulic conductivity to new value.
		}
		
		// Fourth, calculate from which starting index location in the assignments array are the decision variables for variable area wetlands stored.
		int varAreaWetStartIndex = 0;
		for (int i=0; i < 7; i++){ // variable area wetlands are the 8th BMP, with i=7 in the chosenBMPs array.
			varAreaWetStartIndex = varAreaWetStartIndex + chosenBMPs[i];
		}
		varAreaWetStartIndex = varAreaWetStartIndex * this.regionSubbasinId.length;
		
		// Fifth, estimate new values for wetland variables: "WET_FR", "WET_MXSA", "WET_MXVOL", "WET_K"
		int j=0;
		for(int i=varAreaWetStartIndex; i<(varAreaWetStartIndex + this.regionSubbasinId.length); i++){
			if (this.assignments[i] > 0){
				// estimate new "WET_FR" based on maximum wet_fr of potential wetlands
				newWetlandData[j][0] = baselineWetlandData[j][0] + potentialWetlandData[j][0];
				// estimate new "WET_MXSA" based on decision variable value in assignments[]
				newWetlandData[j][1] = baselineWetlandData[j][1] + this.assignments[i];
				//estimate new "WET_MXVOL"
				// Check whether volume should be based on a design depth or the natural representative depth.
				if (this.designDepth == false){
					// new volume = existing volume + maximum potential volume based on natural depressions (where natural depression = WET_MXVOL/WET_MXSA)
					newWetlandData[j][2] = baselineWetlandData[j][2] + this.assignments[i] * (potentialWetlandData[j][2]/potentialWetlandData[j][1]);
				} else{
					// new volume = existing volume + maximum potential area * design depth
					newWetlandData[j][2] = baselineWetlandData[j][2] + this.assignments[i]*this.designDepthValue ;
				}
			} else {
				// new "WET_FR" = existing "WET_FR"
				newWetlandData[j][0] = baselineWetlandData[j][0] ;
				// new "WET_MXSA" = existing "WET_MXSA"
				newWetlandData[j][1] = baselineWetlandData[j][1] ;
				// new "WET_MXVOL"	= existing 	"WET_MXVOL"	
				newWetlandData[j][2] = baselineWetlandData[j][2] ;
                                 // new wet_k = 0.0
                                wet_k_array[j] = this.base_wet_k;
			}
			j++;
		}
		
		// Sixth, write values of new wetland variables in simulation pond files (.pnd)
		for(int i=0;i<this.regionSubbasinId.length;i++){
			for(j=0;j<replace.length-4;j++)
			 {
				 replace[j] = new Double(newWetlandData[i][j]).toString();
				 
			 }//for end
			 replace[replace.length-4] = "0.000";// "WET_NSA" 
			 replace[replace.length-3] = "0.000";// "WET_NVOL"
			 replace[replace.length-2] = "0.000";// "WET_VOL"
			 replace[replace.length-1] = new Double(wet_k_array[i]).toString();//"WET_K" 
			 try {
				 replaceValuesInPndFile(regionSubbasinId[i], replace,base);
			 } catch (IOException ex) {
				 ex.printStackTrace();
		     }
		}
				
	}
	
	// ************************************************
	// To write inputs for Variable area and Variable Wet_fr Wetlands BMP
	public void writeVariableAreaVariableWetfrWetlandsInputs(String base){
		double [][] baselineWetlandData = new double [this.regionSubbasinId.length][3];
		double [][] potentialWetlandData = new double [this.regionSubbasinId.length][3];
		double [][] newWetlandData = new double [this.regionSubbasinId.length][3];
                double [] wet_k_array = new double [this.regionSubbasinId.length];
		String[] replace = new String[this.wetlandLookup.length];
		
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
		
		// Third, initialize new values for wetland variables ("WET_FR", "WET_MXSA", "WET_MXVOL") to 0.0
		for(int i=0;i<this.regionSubbasinId.length;i++){
			newWetlandData[i][0] = 0.0; // initialize new "WET_FR" to 0.0
			newWetlandData [i][1]=0.0; // initialize "WET_MXSA" to 0.0
			newWetlandData[i][2] = 0.0;	// 	initialize "WET_MXVOL" to 0.0
                        wet_k_array[i] = this.wet_k; // initialize hydraulic conductivity to new value.
		}
		
		// Fourth, calculate from which starting index location in the assignments array are the decision variables for variable area and variable wet_fr stored.
		int varAreaWetStartIndex = 0;
		int varWetfrWetStartIndex = 0;
		for (int i=0; i < 7; i++){ // variable area wetlands are the 8th BMP, with i=7 in the chosenBMPs array.
			varAreaWetStartIndex = varAreaWetStartIndex + chosenBMPs[i];
		}
		for (int i=0; i < 8; i++){ // variable wet_fr wetlands are the 9th BMP, with i=8 in the chosenBMPs array.
			varWetfrWetStartIndex = varWetfrWetStartIndex + chosenBMPs[i];
		}
		varAreaWetStartIndex = varAreaWetStartIndex * this.regionSubbasinId.length;
		varWetfrWetStartIndex = varWetfrWetStartIndex * this.regionSubbasinId.length;
		
		// Fifth, estimate new values for wetland variables: "WET_FR", "WET_MXSA", "WET_MXVOL"
		for(int i=0; i< this.regionSubbasinId.length; i++){			
			if ((this.assignments[i+ varAreaWetStartIndex] > 0) && (this.assignments[i+varWetfrWetStartIndex] > 0 )){
				// estimate new "WET_FR" based on assignments[i+varWetfrWetStartIndex] value of potential wetlands wet_fr
				newWetlandData[i][0] = baselineWetlandData[i][0] + this.assignments[i+varWetfrWetStartIndex];
				// estimate new "WET_MXSA" based on decision variable value in assignments[]
				newWetlandData[i][1] = baselineWetlandData[i][1] + this.assignments[i+ varAreaWetStartIndex];
				//estimate new "WET_MXVOL"
				// Check whether volume should be based on a design depth or the natural representative depth.
				if (this.designDepth == false){
					// new volume = existing volume + maximum potential volume based on natural depressions (where natural depression = WET_MXVOL/WET_MXSA)
					newWetlandData[i][2] = baselineWetlandData[i][2] + this.assignments[i+ varAreaWetStartIndex] * (potentialWetlandData[i][2]/potentialWetlandData[i][1]);
				} else{
					// new volume = existing volume + maximum potential area * design depth
					newWetlandData[i][2] = baselineWetlandData[i][2] + this.assignments[i+ varAreaWetStartIndex]*this.designDepthValue ;
				}
			} else {
				// new "WET_FR" = existing "WET_FR"
				newWetlandData[i][0] = baselineWetlandData[i][0] ;
				// new "WET_MXSA" = existing "WET_MXSA"
				newWetlandData[i][1] = baselineWetlandData[i][1] ;
				// new "WET_MXVOL"	= existing 	"WET_MXVOL"	
				newWetlandData[i][2] = baselineWetlandData[i][2] ;
                                 // new wet_k = 0.0
                                wet_k_array[i] = this.base_wet_k;
			}

		}
		
		int j;
		// Sixth, write values of new wetland variables in simulation pond files (.pnd)
		for(int i=0;i<this.regionSubbasinId.length;i++){
			for(j=0;j<replace.length-4;j++)
			 {
				 replace[j] = new Double(newWetlandData[i][j]).toString();
				 
			 }//for end
			 replace[replace.length-4] = "0.000";// "WET_NSA" 
			 replace[replace.length-3] = "0.000";// "WET_NVOL"
			 replace[replace.length-2] = "0.000";// "WET_VOL"
			 replace[replace.length-1] = new Double(wet_k_array[i]).toString();//"WET_K" 
			 try {
				 replaceValuesInPndFile(regionSubbasinId[i], replace,base);
			 } catch (IOException ex) {
				 ex.printStackTrace();
                         }
		}		
	}
	
	// ************************************************
	// To write inputs for Binary Ponds BMP
	public void writeBinaryPondsInputs(String base){
		
	}
	
	// ************************************************
	// To run SWAT model
	public void runSWATexec(String base) throws IOException{
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
	}
 
	//***********************************************************************************************************************
	//************ Below are private methods used to support the above public methods ***************************************
	//***********************************************************************************************************************
	
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Write filterw (filter strip width) in all the management files (.mgt) related to a sub-basin.
        private void replaceSubBasinFilterwInMgtFiles(int subbasinID, double filterw, String base) throws IOException
        {
                    //System.out.println("sub-basin ID for which filterwidth is being written: " + subbasinID);
                    //change contents of the input file
                    int counter = 0, end = 0,i,j, index;
                    String temp = "";
			 
                    // For all .sub files that store the names of the HRU .mgt files
                    FileInputStream subFIS;
                    InputStreamReader subISR;
                    BufferedReader subBR;
                    DataInputStream subDIS;
                    String subData;
                    
                    // For all the .mgt files for every HRU
                    FileInputStream mgtFIS;
                    InputStreamReader mgtISR;
                    BufferedReader mgtBR;
                    DataInputStream mgtDIS;
                    String mgtData;
			
                    File mgtF;
                    Writer mgtW;
			 
		    StringBuilder data = new StringBuilder();
		    counter = 0;
		    
		    // create the name of the subbasin .sub file which will be opened to read the names of various .mgt files.	 
		    String relevantSubbasinsData = new Integer(subbasinID).toString();
		    j = relevantSubbasinsData.length();
		    j = 5 - j;
		    for(i=0;i<j;i++)
		    {
		    	temp = temp + "0";
		    }
		    relevantSubbasinsData = temp + relevantSubbasinsData;
		    relevantSubbasinsData = relevantSubbasinsData + "0000.sub";//subbasin id + 0000.sub file
                    
		    // Now read all the lines in .sub file for this sub-basin, search for .mgt file names	 
		    subFIS = new FileInputStream(base + relevantSubbasinsData);
                    subISR = new InputStreamReader(subFIS);
                    subBR = new BufferedReader(subISR);	
                    int numMgtFiles = 0;
                    StringBuilder mgtFNames = new StringBuilder ();
                    while ((subData = subBR.readLine()) != null) 
                    {
                        if (subData.contains(".mgt")){
                            numMgtFiles ++;
                            mgtFNames.append(subData.substring(13, 26)); // .mgt file name is stored from character index 13 to character index 25
                            //System.out.println(subData.substring(13, 26));
                        } 	
                    }
                    //System.out.println("Sub-basin .mgt file names: ");
                    //System.out.println(mgtFNames.toString());		 	
                    subFIS.close();
                    subISR.close();
                    subBR.close();//close .sub file 

                    // Convert filter width value to a string with appropriate number of significant digits.
		    String filterWStr = new Double(filterw).toString(); 
                    StringBuilder sbTemp = new StringBuilder(filterWStr + "00"); 
		    index = sbTemp.lastIndexOf(".");
		    index = index + 4;
		    sbTemp.delete(index, sbTemp.length());
		    filterWStr = sbTemp.toString();
                    
                       
                    // open individual .mgt file in read mode, read the entire file and replace filterw value with new value.                  
                    for(j=0;j<numMgtFiles;j++)
                    {
                        //System.out.println(".mgt File for which filterwidth is being written: " + mgtFNames.substring(j*13, (j*13)+13));
                        data = new StringBuilder();
                        mgtFIS = new FileInputStream(base + mgtFNames.substring(j*13, (j*13)+13));
                        mgtISR = new InputStreamReader(mgtFIS);
                        mgtBR = new BufferedReader(mgtISR);
                        counter = 0;
                        while ((mgtData = mgtBR.readLine()) != null) 
		 	{//read entire contents of pond file in data variable	    	
		 	    //System.out.println(pndData);
		 	    if (counter==0) { data = data.append(mgtData); counter++;}
		 	    else data = data.append("\n" + mgtData);
		 	    	
		 	}//mgt data
                        data = data.append("\n");
                        data = data.append("\n");
		 	mgtFIS.close();
		 	mgtISR.close();
		 	mgtBR.close();//close .mgt file .. later open in write mode
		 	
                        // idenitfy what land use type this is.
                        if (filterInAgLand == true){ // Check if filterstrips are supposed to be put only on land use that is agricultural (corn and soybean)
                            int luseIndex = data.indexOf("Luse:");
                            if (data.substring(luseIndex+5, luseIndex+9).matches("CORN") || (data.substring(luseIndex+5, luseIndex+9).matches("SOYB"))){
                                end = data.indexOf(this.filterWVarName);
                                //System.out.println(data.charAt(end));
                                end = end - 7; //end points to the last digit of the value
                                //System.out.println(data.charAt(end));
                                for(i=filterWStr.length()-1;i>=0;i--)
                                {
                                    data.setCharAt(end, filterWStr.charAt(i));
                                    end = end - 1;
                                }
                                while(data.charAt(end)!= ' ')
                                {
                                    data.setCharAt(end, ' ');
                                    end = end - 1;
                                }
                            } 
                        } else { // Don't check for land use and put filter strips everywhere
                            end = data.indexOf(this.filterWVarName);
                            //System.out.println(data.charAt(end));
                            end = end - 7; //end points to the last digit of the value
                            //System.out.println(data.charAt(end));
                            for(i=filterWStr.length()-1;i>=0;i--)
			    {
		    		data.setCharAt(end, filterWStr.charAt(i));
		    		end = end - 1;
			    }
                            while(data.charAt(end)!= ' ')
                            {
                                data.setCharAt(end, ' ');
				end = end - 1;
                            }
                        }
                        // Now open the .mgt file in write mode and write the updated data
                        mgtF = new File(base + mgtFNames.substring(j*13, (j*13)+13));
                        mgtW = new BufferedWriter(new FileWriter(mgtF));
                        mgtW.write(data.toString());
                        mgtW.close();
                    }//for 
   		    
           
        }
         //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Write CH_N2 and CH_COV (for grassed waterways) in all the rte files (.rte) related to a sub-basin.
        private void replaceGrassedWaterwayParamsInRteFiles(int subbasinID, double CH_N2, double CH_COV, String base) throws IOException
        {
                    //change contents of the input file
                    int counter = 0, start = 0, end = 0,i,j, index;
                    String temp = "";

			 
                    // For .rte file 
                    FileInputStream rteFIS;
                    InputStreamReader rteISR;
                    BufferedReader rteBR;
                    DataInputStream rteDIS;
                    String rteData;
			
                    File rteF;
                    Writer rteW;
			 
		    StringBuilder data = new StringBuilder();
		    counter = 0;
		    
		    // create the name of the subbasin .rte file 	 
		    String relevantSubbasinsData = new Integer(subbasinID).toString();
		    j = relevantSubbasinsData.length();
		    j = 5 - j;
		    for(i=0;i<j;i++)
		    {
		    	temp = temp + "0";
		    }
		    relevantSubbasinsData = temp + relevantSubbasinsData;
		    relevantSubbasinsData = relevantSubbasinsData + "0000.rte";//subbasin id + 0000.rte file
                    
                    // Convert CH_N2 and CH_COV values to strings with appropriate number of significant digits.
		    String CH_N2Str = new Double(CH_N2).toString(); 
                    StringBuilder sbTemp = new StringBuilder(CH_N2Str + "00"); 
		    index = sbTemp.lastIndexOf(".");
		    index = index + 4;
		    sbTemp.delete(index, sbTemp.length());
		    CH_N2Str = sbTemp.toString();
                    
 		    String CH_COVStr = new Double(CH_COV).toString(); 
                    sbTemp = new StringBuilder(CH_COVStr + "00"); 
		    index = sbTemp.lastIndexOf(".");
		    index = index + 4;
		    sbTemp.delete(index, sbTemp.length());
		    CH_COVStr = sbTemp.toString();                   
                       
                    // open .RTE file in read mode, read the entire file and replace CH_N2 and CH_COV values with new values.                  
                    rteFIS = new FileInputStream(base + relevantSubbasinsData);
                    rteISR = new InputStreamReader(rteFIS);
                    rteBR = new BufferedReader(rteISR);
                    counter = 0;
                    while ((rteData = rteBR.readLine()) != null) 
                    {//read entire contents of rte file in data variable
		 	if (counter==0) { data = data.append(rteData); counter++;}
		 	else data = data.append("\n" + rteData);
		 	    	
                    }//rte data
                    rteFIS.close();
                    rteISR.close();
                    rteBR.close();//close .rte file .. later open in write mode
                    
                    for (int k = 0; k < this.grssWtrwyVarNames.length; k++){
                        end = data.indexOf(this.grssWtrwyVarNames[k]);
                        //System.out.println(data.charAt(end));
                        end = end - 7; //end points to the last digit of the value
                        //System.out.println(data.charAt(end));
                        String chStr = new String ();
                        if (this.grssWtrwyVarNames[k] == "CH_N2"){
                            chStr = new String (CH_N2Str);
                        } else if (this.grssWtrwyVarNames[k] == "CH_COV"){
                            chStr = new String (CH_COVStr);
                        }
                        for(i=chStr.length()-1;i>=0;i--)
			    {
		    		data.setCharAt(end, chStr.charAt(i));
		    		end = end - 1;
			    }
                        while(data.charAt(end)!= ' ')
                            {
                                data.setCharAt(end, ' ');
				end = end - 1;
                            }
                    }
                    data.append("\n");
                    data.append("\n"); 
                    // Now open the .mgt file in write mode and write the updated data
                    rteF = new File(base + relevantSubbasinsData);
                    rteW = new BufferedWriter(new FileWriter(rteF));
                    rteW.write(data.toString());
                    rteW.close();
                    //System.out.println("Done");
   		    
           
        }
               
        
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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
	}//end readPotentialWetlandData
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// This method writes wetland parameters into the .pnd files.
	synchronized void replaceValuesInPndFile(int pondId, String[] replaceWith, String base) throws IOException
		{
			//change contents of the input file
			int counter = 0, start = 0, end = 0,i,j, index;
			String temp = "";
			String relevantSubbasinsData;
			 
			FileInputStream pndFIS;
			InputStreamReader pndISR;
			BufferedReader pndBR;
			DataInputStream pndDIS;
			String pndData;
			
			File pndF;
			Writer pndW;
			 
		    StringBuilder data = new StringBuilder();
		    counter = 0;
		    	 
		    	 
		    for(j=0;j<this.wetlandLookup.length;j++)
		    {
		    	//replaceWith[j] = new Double(restoreData[pondId][j]).toString() + "00";
		    	StringBuilder sbTemp = new StringBuilder(replaceWith[j]+"00"); 
		    	index = sbTemp.lastIndexOf(".");
		    	index = index + 4;
		    	sbTemp.delete(index, sbTemp.length());
		    	replaceWith[j] = sbTemp.toString();
		    		 
		    }
		    	 
		    relevantSubbasinsData = new Integer(pondId).toString();
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
		 	
		 	for(j=0;j<this.wetlandLookup.length;j++)//look for the words appearing in lookup table ... corresponding values will be changed
		 	{
		 		end = data.indexOf(wetlandLookup[j]);
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
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	
	
}