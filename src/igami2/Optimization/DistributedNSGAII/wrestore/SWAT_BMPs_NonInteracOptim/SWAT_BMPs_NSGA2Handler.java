/* ===========================================================
 * This uses JNSGA2: a free java NSGA-II library for the Java(tm) platform
 * ===========================================================
 *
 * Author: Meghna Babbar-Sebens, Dec 2010.
 * Author email: mbabbars@iupui.edu
 */
package igami2.Optimization.DistributedNSGAII.wrestore.SWAT_BMPs_NonInteracOptim;

import igami2.DataBase.IndividualDesign;
import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.*;
import java.util.*;
import java.io.*;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements a test for the binaryWetlands problem using the NSGA-II
 * multi-objective genetic algorithm.
 *
 * @author Meghna Babbar-Sebens, IUPUI, Dec. 2010.
 * @version 1.0
 */
public class SWAT_BMPs_NSGA2Handler {

    private double MUTATION_PROBABILITY = 0.05;  // A much higher mutation rate seems to have a negative effect!
    private double CROSSOVER_PROBABILITY = 0.9;
    private int POPULATION_SIZE = 4;
    private int NUMBER_OF_GENERATIONS = 1;
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // READ INPUT DATA SPECIFIC TO YOUR APPLICATION (mbabbars)
    ////////// Objective Functions / Fitness Functions ////////////////
    // NOTE: DO NOT CHANGE THE ORDER OF THESE FITNESS FUNCTIONS. IF YOU ADD MORE FUNCTIONS, ADD THEM IN THE END AND UPDATE THEIR USE AND INDEX IN OTHER FILES.
    String[] FitnessFunctionlookup = new String[]{"BMP area", "Peak Flow Reduction", "Economic Costs", "Sediments Reduction", "Nitrates Reduction", "Pesticides"};
    int[] chosenFF = new int[]{0, 1, 1, 1, 1, 0}; //This tells us which BMPs are we simulating in this optimization problem.
    String nitrateExecFname = new String("Nitrates.exe");
    String nitrateOutputFname = new String("Nitrates_reduction.txt");
    String erosionExecFname = new String("Sediments.exe");
    String erosionOutputFname = new String("Sediments_reduction.txt");
    String pesticidesExecFname = new String("Pesticides.exe");
    String pesticidesOutputFname = new String("Pesticides_reduction.txt");
    String economicCostsExecFname = new String("Costs.exe");
    String economicCostsOutputFname = new String("Total price SB.txt");
    // IDs of all 108 sub-basins modified
    private int[] regionSubbasinId = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 71, 76, 77, 78, 80, 82, 83, 85, 86, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 110, 111, 112, 115, 117, 119, 121, 122, 123, 124, 125, 126, 127};
    // This stores the tenure ID of each sub-basin.
    // tenure_regionSubbasinId[i] = 1 is a landowner who farms his own land
    // tenure_regionSubbasinId[i] = 2 is a landowner who rents out his land to a cash renter
    // tenure_regionSubbasinId[i] = 3 is a landowner wwho rents out his land to a share renter
    private int[] tenure_regionSubbasinId = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}; //new int[regionSubbasinId.length]; 
    //private  int numTenureTypes = 3;
    private int tenureType = 0; //User defined. Variable used when all sub-basins have same tenure type.
    ////////// Decision Variables ////////////////////////
    // NOTE: DO NOT CHANGE THE ORDER OF THESE BMPS. IF YOU ADD MORE BMPS, ADD THEM IN THE END AND UPDATE THEIR USE AND INDEX IN OTHER FILES.
    String[] BMPlookup = new String[]{"strip_cropping", "crop_rotation", "cover_crops", "filter_strips", "grassed_waterway", "conservation_tillage", "binary_wetlands", "variable_area_wetlands", "variable_wetfr_wetlands", "binary_ponds"};
    //This "chosenBMPs" array below tells us which BMPs we are simulating in this optimization problem. 
    // Note the following for Wetlands:
    // * If you choose binary wetland problem, then make sure that flag for variable area wetlands and variable wetfr wetlands are 0 each.
    // * If you choose variable area wetlands (i.e. its flag value is 1), then
    //   ** Make sure binary wetlands have a flag value of 0
    //   ** If variable wet_fr wetlands have a flag value of 0, then the maximum wet_fr value for the sub-basin will be used.
    //   ** if variable wet_fr wetlands have a flag value of 1, then the wet_fr will also be a decision variable with a range of values from 0 to maximum specific to every sub-basin.
    // chosenBMPs based on tenure types 1, 2, and 3. These should be equal to number of tenure types modeled in the program.
    int[] chosenBMPs = new int[]{1, 1, 1, 1, 1, 1, 0, 1, 1, 0}; // All BMPs modeled. In case of spatially-heterogeneous tenure type, this will be the union of tenure types modeled. 
    // All BMPs are modeled as binary decision variables, except for filter strips, variable area wetlands, variable wetfr wetlands
    // For real number decision variables, the variables below store the minimum and maximum values
    private double[] filterStrip_minmaxValues = {0.0, 5.0};
    private double[][] variableAreaWetlands_minmaxValues = new double[regionSubbasinId.length][2]; // first column stores minimum values and second column stores maximum values
    private double[][] variableWetFrWetlands_minmaxValues = new double[regionSubbasinId.length][2]; // first column stores minimum values and second column stores maximum values
    private String wetFile = "../SWAT/swat_dirs/swat0/wetland.csv";
    // NSGA-2 output file
    private String dosOutFileName = new String("../SWAT/data/SWAT_BMPs_nonInterac/SWAT_BMPs_final.out");
    // Restart file
    String indivsRestartFile = "../SWAT/data/SWAT_BMPs_nonInterac/SWAT_BMPs_TTrand_GAcrashIndivs.csv";
    //double [][] restartData = new double[POPULATION_SIZE][returnTotNumBMPs(chosenBMPs) * regionSubbasinId.length];
    boolean restartFlag = false;
    // debugflag
    boolean debugFlag = false; // Set it true when you want to test the code for specific individuals.
    boolean state = false;
    String location = "objects/";
    public LinkedList<Individual> startPopulation;
    private int UserId;
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // READ INPUT DATA SPECIFIC TO YOUR APPLICATION (mbabbars)
    ////////// Objective Functions / Fitness Functions ////////////////
    // NOTE: DO NOT CHANGE THE ORDER OF THESE FITNESS FUNCTIONS. IF YOU ADD MORE FUNCTIONS, ADD THEM IN THE END AND UPDATE THEIR USE AND INDEX IN OTHER FILES.
    NSGA2 nsga2;
    /////////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Main method
    // 
    // @param args arguments (not used)
    //
    public SWAT_BMPs_NSGA2Handler(int UserId,int[] regionSubbasinId, double MUTATION_PROBABILITY, double CROSSOVER_PROBABILITY, int POPULATION_SIZE, int[] chosenBMPs, int[] chosenFF, int[] tenure_regionSubbasinId) {
        this.MUTATION_PROBABILITY = MUTATION_PROBABILITY;
        this.CROSSOVER_PROBABILITY = CROSSOVER_PROBABILITY;
        this.POPULATION_SIZE = POPULATION_SIZE;
        this.regionSubbasinId = regionSubbasinId;
        this.tenure_regionSubbasinId = tenure_regionSubbasinId;
        this.chosenBMPs = chosenBMPs;
        this.chosenFF = chosenFF;
        this.UserId = UserId;
    }

    public void initNSGA2() {
        // First, create fitness functions specific to the problem
        // comment (mbabbars): You will need to modify these classes for your application
        // String [] FitnessFunctionlookup = new String[]{"BMP area", "Peak Flow", "Economic Costs", "Soil Erosion", "Nitrates", "Pesticides"}; 
        SWAT_BMPs_AreaFitnessFunction fitnessFunction0 = new SWAT_BMPs_AreaFitnessFunction(chosenFF[0], regionSubbasinId, chosenBMPs);
        SWAT_BMPs_PeakflowFitnessFunction fitnessFunction1 = new SWAT_BMPs_PeakflowFitnessFunction(chosenFF[1]);
        SWAT_BMPs_EconomicCostsFitnessFunction fitnessFunction2 = new SWAT_BMPs_EconomicCostsFitnessFunction(chosenFF[2], BMPlookup, economicCostsExecFname, economicCostsOutputFname, tenure_regionSubbasinId, regionSubbasinId);
        SWAT_BMPs_SoilErosionFitnessFunction fitnessFunction3 = new SWAT_BMPs_SoilErosionFitnessFunction(chosenFF[3], erosionExecFname, erosionOutputFname);
        SWAT_BMPs_NitratesFitnessFunction fitnessFunction4 = new SWAT_BMPs_NitratesFitnessFunction(chosenFF[4], nitrateExecFname, nitrateOutputFname);
        SWAT_BMPs_PesticidesFitnessFunction fitnessFunction5 = new SWAT_BMPs_PesticidesFitnessFunction(chosenFF[5], pesticidesExecFname, pesticidesOutputFname);
        //Always keep the Rating fitness function as last for evlaution at last
        SWAT_BMP_IndividualsRatingFitnessFunction fitnessFunction6 = new SWAT_BMP_IndividualsRatingFitnessFunction();

        // Set the fitnessfunction array to be the size of your number of objectives.
        FitnessFunction[] fitnessFunctions = new FitnessFunction[chosenFF.length];
        fitnessFunctions[0] = fitnessFunction0;
        fitnessFunctions[1] = fitnessFunction1;
        fitnessFunctions[2] = fitnessFunction2;
        fitnessFunctions[3] = fitnessFunction3;
        fitnessFunctions[4] = fitnessFunction4;
        fitnessFunctions[5] = fitnessFunction5;
        fitnessFunctions[6] = fitnessFunction6;
        //System.out.println ("fitness functions created");

        // initialize the NSGA2 configuration parameters.
        NSGA2Configuration conf = new NSGA2Configuration(fitnessFunctions,
                MUTATION_PROBABILITY,
                CROSSOVER_PROBABILITY,
                POPULATION_SIZE,
                NUMBER_OF_GENERATIONS);

        // create a NSGA2 class with the desired configuration
        nsga2 = new NSGA2(conf);
        //System.out.println ("nsga2 object created");

        // Add a listener to your NSGA2 class.
        // comment (mbabbars): you will need to create a NSGA2 listener specific to your application/problem
        nsga2.addNSGA2Listener(new SWAT_BMPs_NonInterac_NSGA2Listener(chosenFF, UserId));
        //System.out.println ("NSGA listener added");

    }

    public void CreateNewSet() throws IOException {

        // create start population
        startPopulation = new LinkedList<Individual>();

        // read min-max values of wetlands from "wetland.csv" file
        readWetlandData(wetFile);

        // Random assignment
        for (int i=0; i < POPULATION_SIZE; i++) {
                    // comment (mbabbars): you will need to create a specific individual class for your application
                    //System.out.println (i);
                    SWAT_BMPs_Individual individual = new SWAT_BMPs_Individual(nsga2, regionSubbasinId, tenure_regionSubbasinId, chosenBMPs, chosenFF, filterStrip_minmaxValues, variableAreaWetlands_minmaxValues, variableWetFrWetlands_minmaxValues, economicCostsExecFname, economicCostsOutputFname, erosionExecFname, erosionOutputFname, nitrateExecFname, nitrateOutputFname, pesticidesExecFname, pesticidesOutputFname, createRandomAssignment(regionSubbasinId, tenure_regionSubbasinId, chosenBMPs, filterStrip_minmaxValues, variableAreaWetlands_minmaxValues, variableWetFrWetlands_minmaxValues, debugFlag));
                    //System.out.print(" Assignment:"+ individual.getAssignments());
                    individual.UserId = this.UserId;
                    startPopulation.add(individual);         
        }
        //startPopulation = nsga2.EvaluateGeneration(startPopulation);//evaluate first generation
    }

    public NSGA2 getNSGA2Instace() {
        return nsga2;
    }

    public void createUsingCBM(int[] chosenFF, int userId, LinkedList<IndividualDesign> restartData, int needed, int newIndvs) throws IOException {


        //read percent random assigns
        int i = 0;
        int m = needed; //count no of indv added
        int max = restartData.size();
        i = max - 1;
        //Random rn = new Random();
        //rn.setSeed(max);


        // create start population
        startPopulation = new LinkedList<Individual>();

        // read min-max values of wetlands from "wetland.csv" file
        readWetlandData(wetFile);

        //feed with CBM at random
        while (i > 0 && m > 0) {

            //m= rn.nextInt(max);//%max;
            if (Math.random() > 0.5) {
                m--;
                //System.out.println("\n No k " + i);
                // comment (mbabbars): you will need to create a specific individual class for your application
                //System.out.println (i);
                SWAT_BMPs_Individual individual = new SWAT_BMPs_Individual(nsga2, regionSubbasinId, tenure_regionSubbasinId, chosenBMPs, chosenFF, filterStrip_minmaxValues, variableAreaWetlands_minmaxValues, variableWetFrWetlands_minmaxValues, economicCostsExecFname, economicCostsOutputFname, erosionExecFname, erosionOutputFname, nitrateExecFname, nitrateOutputFname, pesticidesExecFname, pesticidesOutputFname, restartData.get(i).assignments);
                //System.out.print(" restartData[i]:");
                for (int k = 0; k < regionSubbasinId.length; k++) {
                    //System.out.print(restartData.get(i).assignments[k]);
                }
                //System.out.println();
                startPopulation.add(individual);
            }
            i--;

        }

        //check if sufficient numbers taken or not otherwise add random assigns
        if (startPopulation.size() < needed) {
            for (int j = 0; j < m; j++) {
                SWAT_BMPs_Individual individual = new SWAT_BMPs_Individual(nsga2, regionSubbasinId, tenure_regionSubbasinId, chosenBMPs, chosenFF, filterStrip_minmaxValues, variableAreaWetlands_minmaxValues, variableWetFrWetlands_minmaxValues, economicCostsExecFname, economicCostsOutputFname, erosionExecFname, erosionOutputFname, nitrateExecFname, nitrateOutputFname, pesticidesExecFname, pesticidesOutputFname, createRandomAssignment(regionSubbasinId, tenure_regionSubbasinId, chosenBMPs, filterStrip_minmaxValues, variableAreaWetlands_minmaxValues, variableWetFrWetlands_minmaxValues, debugFlag));
                //System.out.print(" Assignment:"+ individual.getAssignments());
                startPopulation.add(individual);
                //System.out.println("Added a new Random Indv "+j);
            }
        }

        //add New Random assigns

        for (int j = 0; j < newIndvs; j++) {
            SWAT_BMPs_Individual individual = new SWAT_BMPs_Individual(nsga2, regionSubbasinId, tenure_regionSubbasinId, chosenBMPs, chosenFF, filterStrip_minmaxValues, variableAreaWetlands_minmaxValues, variableWetFrWetlands_minmaxValues, economicCostsExecFname, economicCostsOutputFname, erosionExecFname, erosionOutputFname, nitrateExecFname, nitrateOutputFname, pesticidesExecFname, pesticidesOutputFname, createRandomAssignment(regionSubbasinId, tenure_regionSubbasinId, chosenBMPs, filterStrip_minmaxValues, variableAreaWetlands_minmaxValues, variableWetFrWetlands_minmaxValues, debugFlag));
            //System.out.print(" Assignment:"+ individual.getAssignments());
            startPopulation.add(individual);
            //System.out.println("Added a new Random Indv "+j);
        }

        //add the UserId
        for (int j = 0; j < startPopulation.size(); j++) {
            startPopulation.get(j).UserId = userId;
            //startPopulation.get(j).confidence = 50-1;
        }
        //startPopulation = nsga2.EvaluateGeneration(startPopulation);//evaluate first generation
    }

    public void createRestartIndividuals(String file) throws IOException {
        startPopulation = new LinkedList<Individual>();

        // read min-max values of wetlands from "wetland.csv" file
        readWetlandData(wetFile);

        LinkedList restartData = readRestartData(file);
        //feeed with CBM
        //System.out.print("Individual Assign Lenghts ");
        for (int i = 0; i < restartData.size(); i++) {
            //double[] assign = changeBMPs(chosenBMPs, (double[]) restartData.get(i));
            double[] assign = (double[]) restartData.get(i);
            //System.out.print("\t "+assign.length);
            SWAT_BMPs_Individual individual = new SWAT_BMPs_Individual(nsga2, regionSubbasinId, tenure_regionSubbasinId, chosenBMPs, chosenFF, filterStrip_minmaxValues, variableAreaWetlands_minmaxValues, variableWetFrWetlands_minmaxValues, economicCostsExecFname, economicCostsOutputFname, erosionExecFname, erosionOutputFname, nitrateExecFname, nitrateOutputFname, pesticidesExecFname, pesticidesOutputFname, assign);
            individual.UserId = this.UserId;
            individual.IndvId = i;
            startPopulation.add(individual);
        }
        //System.out.println();
    }

    //regenerate the Individual
    public LinkedList<Individual> generateIndividual(LinkedList<IndividualDesign> restartData, int[] chosenFF,int[] chosenBMPs, int userId) {
        LinkedList<Individual> res = new LinkedList<Individual>();


        for (int i = 0; i < restartData.size(); i++) {
            try {
                IndividualDesign in = restartData.get(i);
                SWAT_BMPs_Individual individual = new SWAT_BMPs_Individual(nsga2, regionSubbasinId, tenure_regionSubbasinId, chosenBMPs, chosenFF, filterStrip_minmaxValues, variableAreaWetlands_minmaxValues, variableWetFrWetlands_minmaxValues, economicCostsExecFname, economicCostsOutputFname, erosionExecFname, erosionOutputFname, nitrateExecFname, nitrateOutputFname, pesticidesExecFname, pesticidesOutputFname, in.assignments);

                individual.UserId = userId;
                individual.IndvId = in.IndvId;
                individual.rating = in.rating;
                individual.confidence = in.confidence;
                individual.fitnessValues = in.fitnessValues;
                individual.chosenBMPs = chosenBMPs;
                individual.chosenFF = chosenFF;
                individual.regionSubbasinId = regionSubbasinId;
                individual.subbasinsFF = in.subbasinsFF;
                individual.tenure_regionSubbasinId = tenure_regionSubbasinId;
                res.add(individual);

            } catch (IOException ex) {
                Logger.getLogger(SWAT_BMPs_NSGA2Handler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return res;
    }
    
        /**
     * Calculate and return total number of BMPs modeled in the optimization
     *
     * @param chosenBMPs array that specifies which BMPs are chosen
     * @return totalNumBMPs created by mbabbars 4/25/2012
     */
    private int returnTotNumBMPs(int[] chosenBMPs) {
        int totalBMPs = 0;
        for (int i = 0; i < chosenBMPs.length; i++) {
            totalBMPs = totalBMPs + chosenBMPs[i];
        }
        return totalBMPs;
    }

    /**
     * Creates a random event assignment for all the registrants
     *
     * @param size number of registrants
     * @param events offered events
     * @return random event assignment comment(mbabbars): Change this for your
     * problem
     */
    private double[] createRandomAssignment(int[] regionSubbasinId, int[] tenure_regionSubbasinId, int[] chosenBMPs, double[] filterStrip_minmaxValues, double[][] variableAreaWetlands_minmaxValues, double[][] variableWetFrWetlands_minmaxValues, boolean debugFlag) {
        int totalBMPs = 0;
        int currentNumBMPs = 0;

        if (regionSubbasinId == null) {
            throw new IllegalArgumentException("'regionSubbasinId' must not be null.");
        }

        totalBMPs = returnTotNumBMPs(chosenBMPs);


        double[] assignment = new double[totalBMPs * regionSubbasinId.length];

        if (debugFlag == false) {

            for (int i = 0; i < chosenBMPs.length; i++) {
                if (chosenBMPs[i] == 1) {
                    //Note: String [] BMPlookup = new String[]{"strip cropping", "conservation crop rotation", "cover crops", "filter strips", "grassed waterway", "conservation tillage", "binary wetlands", "variable area wetlands","variable Wetfr wetlands","binary ponds"}; 
                    // If  "strip cropping"
                    if (i == 0) {
                        for (int j = 0; j < regionSubbasinId.length; j++) {
                            // search random assignment 
                            if (Math.random() <= 0.5) {
                                assignment[currentNumBMPs * regionSubbasinId.length + j] = 0;
                            } else {
                                assignment[currentNumBMPs * regionSubbasinId.length + j] = 1;
                            }
                        }
                    } // If "conservation crop rotation"
                    else if (i == 1) {
                        for (int j = 0; j < regionSubbasinId.length; j++) {
                            // search random assignment 
                            if (Math.random() <= 0.5) {
                                assignment[currentNumBMPs * regionSubbasinId.length + j] = 0;
                            } else {
                                assignment[currentNumBMPs * regionSubbasinId.length + j] = 1;
                            }
                        }
                    } // If "cover crops"
                    else if (i == 2) {
                        for (int j = 0; j < regionSubbasinId.length; j++) {
                            // search random assignment 
                            if (Math.random() <= 0.5) {
                                assignment[currentNumBMPs * regionSubbasinId.length + j] = 0;
                            } else {
                                assignment[currentNumBMPs * regionSubbasinId.length + j] = 1;
                            }
                        }
                    } // If "filter strips"
                    else if (i == 3) {
                        for (int j = 0; j < regionSubbasinId.length; j++) {
                            // search random assignment 
                            assignment[currentNumBMPs * regionSubbasinId.length + j] = filterStrip_minmaxValues[0] + Math.random() * (filterStrip_minmaxValues[1] - filterStrip_minmaxValues[0]);

                        }
                    } // If "grassed waterway"
                    else if (i == 4) {
                        for (int j = 0; j < regionSubbasinId.length; j++) {
                            // search random assignment 
                            if (Math.random() <= 0.5) {
                                assignment[currentNumBMPs * regionSubbasinId.length + j] = 0;
                            } else {
                                assignment[currentNumBMPs * regionSubbasinId.length + j] = 1;
                            }
                        }
                    } // If "conservation tillage"
                    else if (i == 5) {
                        for (int j = 0; j < regionSubbasinId.length; j++) {
                            // search random assignment 
                            if (Math.random() <= 0.5) {
                                assignment[currentNumBMPs * regionSubbasinId.length + j] = 0;
                            } else {
                                assignment[currentNumBMPs * regionSubbasinId.length + j] = 1;
                            }
                        }
                    } // If "binary wetlands"
                    else if (i == 6) {
                        for (int j = 0; j < regionSubbasinId.length; j++) {
                            // search random assignment 
                            if (Math.random() <= 0.5) {
                                assignment[currentNumBMPs * regionSubbasinId.length + j] = 0;
                            } else {
                                assignment[currentNumBMPs * regionSubbasinId.length + j] = 1;
                            }
                        }
                    } // If "variable area wetlands"
                    else if (i == 7) {
                        for (int j = 0; j < regionSubbasinId.length; j++) {
                            // search random assignment 
                            assignment[currentNumBMPs * regionSubbasinId.length + j] = variableAreaWetlands_minmaxValues[j][0] + Math.random() * (variableAreaWetlands_minmaxValues[j][1] - variableAreaWetlands_minmaxValues[j][0]);
                        }
                    } // If "variable Wetfr wetlands". Note that when this BMP is chosen, "variable area wetlands" BMP is also chosen!! In other words, if chosenBMP[8]=1, then make sure that chosenBMP[7] is also 1.
                    else if (i == 8) {
                        for (int j = 0; j < regionSubbasinId.length; j++) {
                            // search random assignment 
                            assignment[currentNumBMPs * regionSubbasinId.length + j] = variableWetFrWetlands_minmaxValues[j][0] + Math.random() * (variableWetFrWetlands_minmaxValues[j][1] - variableWetFrWetlands_minmaxValues[j][0]);
                        }
                    } // If "binary ponds"
                    else if (i == 9) {
                        for (int j = 0; j < regionSubbasinId.length; j++) {
                            // search random assignment 
                            if (Math.random() <= 0.5) {
                                assignment[currentNumBMPs * regionSubbasinId.length + j] = 0;
                            } else {
                                assignment[currentNumBMPs * regionSubbasinId.length + j] = 1;
                            }
                        }
                    }
                    currentNumBMPs++;
                }// if (chosenBMPs[i] == 1)
            }
        } // If debug flag is true. Modify this according to the BMPs. 
        // Currently this works for only binary BMPs.
        else {
            for (int i = 0; i < assignment.length; i++) {
                assignment[i] = 1; //1;
            }
            if (chosenBMPs[3] == 1) {
                // Calculate from which starting index location in the assignments array are the decision variables for filter strips stored.
                int filterStripStartIndex = 0;
                for (int i = 0; i < 3; i++) { // filter strips are the 4th BMP, with i=3 in the chosenBMPs array.
                    filterStripStartIndex = filterStripStartIndex + chosenBMPs[i];
                }
                filterStripStartIndex = filterStripStartIndex * regionSubbasinId.length;
                for (int i = 0; i < regionSubbasinId.length; i++) {
                    assignment[filterStripStartIndex + i] = filterStrip_minmaxValues[1]; //1;
                }
            }
        }
        for (int i = 0; i < assignment.length; i++) {
            //System.out.print(assignment[i] + ", ");
        }
        return assignment;
    }

    // Read wetland min-max values for variables areas and variable wet_fractions
    private void readWetlandData(String debFile) throws IOException {
        FileInputStream debFIS;
        InputStreamReader debISR;
        BufferedReader debBR;
        DataInputStream debDIS;
        String debData;
        String tokenString;
        StringTokenizer st;
        Integer id;


        int counter = 0;

        debFIS = new FileInputStream(debFile);
        debISR = new InputStreamReader(debFIS);
        debBR = new BufferedReader(debISR);

        for (int i = 0; i < regionSubbasinId.length; i++) {
            variableAreaWetlands_minmaxValues[i][0] = 0.0;
            variableWetFrWetlands_minmaxValues[i][0] = 0.0;
            variableAreaWetlands_minmaxValues[i][1] = 0.0;
            variableWetFrWetlands_minmaxValues[i][1] = 0.0;
        }

        while ((debData = debBR.readLine()) != null) {
            if (counter == 0) {
                counter++;
            } else {
                st = new StringTokenizer(debData, ",");
                tokenString = st.nextToken();
                id = new Integer(tokenString);

                tokenString = st.nextToken(); //  read maximum value of potential additional wet_fr in that sub-basin
                variableWetFrWetlands_minmaxValues[counter - 1][1] = new Double(tokenString).doubleValue();

                tokenString = st.nextToken();// read maximum value of potential additional wetland area in that sub-basin
                variableAreaWetlands_minmaxValues[counter - 1][1] = new Double(tokenString).doubleValue();

                counter++;

            }//end else
        }//end while
    }//end readWetlandData

    // read individual data (i.e. genes) from input file (e.g. restart file)
    LinkedList readRestartData(String file) throws IOException {
        LinkedList restartData = new LinkedList();

        FileInputStream indivsFIS;
        InputStreamReader indivsISR;
        BufferedReader indivsBR;
        DataInputStream indivsDIS;
        String indivData;
        String tokenString;
        
        int id;


        int counter = 0;

        indivsFIS = new FileInputStream(file);
        indivsISR = new InputStreamReader(indivsFIS);
        indivsBR = new BufferedReader(indivsISR);
        /*
         * for(int i=0;i<POPULATION_SIZE;i++){ for(int
         * j=0;j<returnTotNumBMPs(chosenBMPs)*regionSubbasinId.length;j++) {
         * restartData[i][j] = 0.0; } }
         *
         */

        int assignLenght = returnTotNumBMPs(chosenBMPs) * regionSubbasinId.length;
        //int assignLenght = (chosenBMPs.length-2) * regionSubbasinId.length;

        while ((indivData = indivsBR.readLine()) != null) {
            String[] st = indivData.split(",");
            double[] IndvAssign = new double[assignLenght];
            System.out.println("Length "+st.length);

            for (int i = 0; i < IndvAssign.length; i++) {
                
                IndvAssign[i] = Double.parseDouble(st[i]);
                System.out.print(IndvAssign[i]);
            }
            restartData.add(IndvAssign);
            System.out.println();
        }//end while

        return restartData;
    }//end readRestartData

    /**
     * Prints the specified binaryWetlands individuals.
     *
     * @param bestAssignments binaryWetlands individuals
     */
    public void printBest_SWAT_BMPs_Individuals(LinkedList<Individual> bestAssignments) throws IOException {
        DataOutputStream dosFlow;

        if (bestAssignments == null) {
            throw new IllegalArgumentException("'bestAssignments' must not be null.");
        }

        // sort best assignments
        SWAT_BMPs_Individual[] array =
                bestAssignments.toArray(new SWAT_BMPs_Individual[bestAssignments.size()]);
        Arrays.sort(array, new SWAT_BMPs_IndividualComparator());

        // print best solutions to standard output
        System.out.println();
        System.out.println("Number of offered solutions: " + bestAssignments.size());

        for (int i = 0; i < array.length; i++) {
            for (int k = 0; k < array[i].getAssignments().length; k++) {
                System.out.print(array[i].getAssignments()[k]);
            }
            System.out.print(" / BMP area Fitness: " + new Double(array[i].getFitnessValue(0)).toString());
            System.out.print(" / Peak Flow Fitness: " + new Double(array[i].getFitnessValue(1)).toString());
            System.out.print(" / Economic Costs Fitness: " + new Double(array[i].getFitnessValue(2)).toString());
            System.out.print(" / Soil Erosion Fitness: " + new Double(array[i].getFitnessValue(3)).toString());
            System.out.println(" / Nitrates Fitness: " + new Double(array[i].getFitnessValue(4)).toString());
            System.out.println(" / Pesticides Fitness: " + new Double(array[i].getFitnessValue(5)).toString());
        }

        // print best solutions to file
        dosFlow = new DataOutputStream(new FileOutputStream(dosOutFileName));
        dosFlow.writeBytes("Number of offered solutions: " + bestAssignments.size());
        dosFlow.writeBytes("\n");
        for (int i = 0; i < array.length; i++) {
            for (int k = 0; k < array[i].getAssignments().length; k++) {
                dosFlow.writeBytes(new Double(array[i].getAssignments()[k]).toString());
            }
            dosFlow.writeBytes(" / BMP area Fitness: " + new Double(array[i].getFitnessValue(0)).toString());
            dosFlow.writeBytes(" / Peak Flow Fitness: " + new Double(array[i].getFitnessValue(1)).toString());
            dosFlow.writeBytes(" / Economic Costs Fitness: " + new Double(array[i].getFitnessValue(2)).toString());
            dosFlow.writeBytes(" / Soil Erosion Fitness: " + new Double(array[i].getFitnessValue(3)).toString());
            dosFlow.writeBytes(" / Nitrates Fitness: " + new Double(array[i].getFitnessValue(4)).toString());
            dosFlow.writeBytes(" / Pesticides Fitness: " + new Double(array[i].getFitnessValue(5)).toString());
            dosFlow.writeBytes("\n");
        }
    }

    private double[] changeBMPs(int[] chosenBMPs, double[] allbmps) {
        
        int assignLenght = returnTotNumBMPs(chosenBMPs) * regionSubbasinId.length;
        
        double[] res = new double[assignLenght];

        //skip the BMPs not selected'
        int posY = 0;
        int posX = 0;
        for(int i=0;i<chosenBMPs.length;i++)
        {
            
            if(chosenBMPs[i]==1)
            {
                for(int j=0;j<regionSubbasinId.length;j++)
                {
                    res[posX++]= allbmps[posY++];
                }
                int a=0;
                
            }
            else
            {
                if(i!=6) //don't skip for 7th BMP
                    posY = posY + regionSubbasinId.length;
                else
                {
                    int b=0;
                }
            }
        }
        return res;
    }

    /**
     * This inner class implements a comparator for two assignment individuals.
     */
    private class SWAT_BMPs_IndividualComparator implements Comparator<SWAT_BMPs_Individual> {

        /**
         * Compares the two specified assignment individuals. First criterion is
         * small number of "no wish events", second one is a small sum of event
         * priorities and the third one a small variance of the event sizes.
         *
         * @param individual1 first individual
         * @param individual2 second individual
         * @return -1, 0 or 1 as the first individual is less than, equal to, or
         * greater than the second one
         */
        public int compare(SWAT_BMPs_Individual individual1, SWAT_BMPs_Individual individual2) {
            if (individual1 == null) {
                throw new IllegalArgumentException("'individual1' must not be null.");
            }
            if (individual2 == null) {
                throw new IllegalArgumentException("'individual2' must not be null.");
            }

            // (1) Total BMP area (objective 0)
            if (chosenFF[0] == 1) {
                if (individual1.getFitnessValue(0) < individual2.getFitnessValue(0)) {
                    return -1;
                }

                if (individual1.getFitnessValue(0) > individual2.getFitnessValue(0)) {
                    return 1;
                }
            }

            // (2) Peak flows (objective 1) 
            if (chosenFF[1] == 1) {
                if (individual1.getFitnessValue(1) < individual2.getFitnessValue(1)) {
                    return -1;
                }

                if (individual1.getFitnessValue(1) > individual2.getFitnessValue(1)) {
                    return 1;
                }
            }

            // (3) Economic Costs (objective 2) 
            if (chosenFF[2] == 1) {
                if (individual1.getFitnessValue(2) < individual2.getFitnessValue(2)) {
                    return -1;
                }

                if (individual1.getFitnessValue(2) > individual2.getFitnessValue(2)) {
                    return 1;
                }
            }
            // (4) Soil Erosion (objective 3) 
            if (chosenFF[3] == 1) {
                if (individual1.getFitnessValue(3) < individual2.getFitnessValue(3)) {
                    return -1;
                }

                if (individual1.getFitnessValue(3) > individual2.getFitnessValue(3)) {
                    return 1;
                }
            }
            // (5) Nitrates (objective 4) 
            if (chosenFF[4] == 1) {
                if (individual1.getFitnessValue(4) < individual2.getFitnessValue(4)) {
                    return -1;
                }

                if (individual1.getFitnessValue(4) > individual2.getFitnessValue(4)) {
                    return 1;
                }
            }
            // (6) Pesticides (objective 5) 
            if (chosenFF[5] == 1) {
                if (individual1.getFitnessValue(5) < individual2.getFitnessValue(5)) {
                    return -1;
                }

                if (individual1.getFitnessValue(5) > individual2.getFitnessValue(5)) {
                    return 1;
                }
            }
            // both individuals are equal
            return 0;
        }
    }
}