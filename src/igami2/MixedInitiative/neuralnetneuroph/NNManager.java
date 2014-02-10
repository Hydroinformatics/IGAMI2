/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.MixedInitiative.neuralnetneuroph;

import igami2.DistributedSystem.VTimer;
import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import java.io.Serializable;
import java.util.LinkedList;

/**
 *
 * @author VIDYA
 */
public class NNManager implements Serializable{

    private NeuralNet NN;
    private int NN_input = 4; //6X13X1 multi layer perceptron with backpropogation
    private int NN_Hidden = 5; //twice of input plus 1 as extra
    private int NN_output = 3;
    private LinkedList lst;
    private double Area_MAX = 800;//603.96; //Not used
    private double Flow_MAX = 56.05;
    private double Cost_MAX = 41916048;
    private double Soil_MAX = 401109.7;
    private double Nitrate_MAX = 7281552;
    private double Pesticide_MAX = 100; //Not Used
    private LinkedList InputList;
    private LinkedList OutputList;
    private int[] chosenFF;
    private double WtCost = 1.5;//give the cost a weight of 3

    public NNManager(int NN_input, int NN_output, int[] chosenFF) {
        this.NN_input = NN_input;
        this.NN_output = NN_output;
        this.chosenFF = chosenFF;
        createNewNN();
        NN.createTraingSet();
    }

    public void createNewNN() {
        NN = new NeuralNet(NN_input, NN_Hidden, NN_output);
    }

    public void learn(LinkedList<Individual> Population) {
        InputList = new LinkedList();
        OutputList = new LinkedList();

        //System.out.println("\nNN Learning Values ");
        for (int i = 0; i < Population.size(); i++) {
            Individual inn = Population.get(i);
            double dataIn[] = new double[NN_input];
            double dataOut[] = new double[NN_output];
            int count = 0;
            for (int j = 0; j < chosenFF.length - 1; j++) {
                //use the j+1, skip the area
                if (chosenFF[j] == 1) {
                    dataIn[count] = scaleFitnessValue(j, inn.fitnessValues[j]);
                    //System.out.print("\t" + dataIn[count]);
                    count++;
                }
            }
            for (int j = 0; j < NN_output; j++) {
                if (inn.rating == j + 1) {
                    dataOut[j] = 1;//true
                } else {
                    dataOut[j] = 0;//false
                }
                //System.out.print("\t" + dataOut[j]);
            }
            //System.out.println();
            InputList.add(dataIn);
            OutputList.add(dataOut);
        }

        VTimer time1 = new VTimer();
        time1.startTimer();
        NN.learnNN(InputList, OutputList, false);
        time1.endTimer();

        System.out.println("\nNN Learning Time is " + time1.getTimeHHMMSS());

    }

    public double[] testNN(Individual in) {
        double [] res = null;
        double[] input = new double[NN_input];
        //System.out.println("\nNN Input Values ");
        int count = 0;
            for (int j = 0; j < chosenFF.length - 1; j++) {
                if (chosenFF[j] == 1) {
                    input[count] = scaleFitnessValue(j, in.fitnessValues[j]);
                    //System.out.print("\t" + input[count]);
                    count++;
                }
            }
        double[] ress = NN.calculateOutput(input);
        res = ress;

        /*
        double max = 0;
        System.out.println("\nNN Values ");
        for (int i = 0; i < ress.length; i++) //max is taken
        {
            System.out.print("\t" + ress[i]);
            if (ress[i] > max) {
                max = ress[i];
                res = i + 1;
            }
        }
        System.out.println("\nValue ");
        * 
        */
        return res;
    }
    
     public double[] testNNScaled(double[] input) {
        double [] res = null;
        //System.out.println("\nNN Input Values ");
        
        res = NN.calculateOutput(input);
        
        return res;
    }

    public void saveNN(String location) {
        NN.saveNN(location);
    }

    public void loadNN(String location) {
        NN.loadNN(location);
    }

    private double scaleFitnessValue(int j, double val) {
        double res = 0;

        if (j == 0) { //Area
            res = val / Area_MAX;
        } else if (j == 1) { //Flow
            res = ((-1) * val) / Flow_MAX;
        } else if (j == 2) { //Flow
            res = ((-1) * val) * WtCost / Cost_MAX;
        } else if (j == 3) { //Flow
            res = ((-1) * val) / Soil_MAX;
        } else if (j == 4) { //Flow
            res = ((-1) * val) / Nitrate_MAX;
        } else if (j == 5) { //Flow
            res = ((-1) * val) / Pesticide_MAX;
        }

        return res;
    }

    public void learnNormalize(double[][] NormalizedData) {
                InputList = new LinkedList();
        OutputList = new LinkedList();

        //System.out.println("\nNN Learning Values ");
        for (int i = 0; i < NormalizedData.length; i++) {
            double dataIn[] = new double[NN_input];
            double dataOut[] = new double[NN_output];
            for (int j = 0; j < NN_input; j++) {
                //use the j+1, skip the area
                    dataIn[j] = NormalizedData[i][j];
                    //System.out.print("  " + dataIn[j]);
            }
            double rating = NormalizedData[i][NormalizedData[0].length-1]; //last
            for (int j = 0; j < NN_output; j++) {
                if (rating == j + 1) {
                    dataOut[j] = 1;//true
                } else {
                    dataOut[j] = 0;//false
                }
                //System.out.print("\t" + dataOut[j]);
            }
            //System.out.println();
            InputList.add(dataIn);
            OutputList.add(dataOut);
        }

        VTimer time1 = new VTimer();
        time1.startTimer();
        NN.learnNN(InputList, OutputList, false);
        time1.endTimer();

        System.out.println("\nNN Learning Time is " + time1.getTimeHHMMSS());

    }
    
    public double[] testNNNormalize(double[] input) {
        
        double[] ress = NN.calculateOutput(input);
        //System.out.println("\nValue ");
        return ress;
    }
}
