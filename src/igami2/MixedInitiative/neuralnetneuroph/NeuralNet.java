/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.MixedInitiative.neuralnetneuroph;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.SupervisedLearning;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.Perceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.TransferFunctionType;

/**
 *
 * @author VIDYA
 */
public class NeuralNet implements Serializable{

    NeuralNetwork NN;
    int InputNeurons;
    int OutputNeurons;
    int HiddenNeurons;
    boolean first = true;// use to check if first time
    TrainingSet<SupervisedTrainingElement> trainingSet;
    int maxItr= 10000*5;

    SupervisedLearning learnRule;
    public NeuralNet(int in, int out) {
        this.InputNeurons = in;
        this.OutputNeurons = out;
        NN = new Perceptron(in, out);
    }

    public NeuralNet(int in, int HiddenNeurons, int out) {
        this.InputNeurons = in;
        this.OutputNeurons = out;
        this.HiddenNeurons = HiddenNeurons;
        List<java.lang.Integer> neuronsInLayers = new LinkedList<java.lang.Integer>();
        neuronsInLayers.add(in);
        neuronsInLayers.add(HiddenNeurons);
        neuronsInLayers.add(out);
        NN = new MultiLayerPerceptron(neuronsInLayers, TransferFunctionType.SIGMOID);
        
        
        learnRule = new BackPropagation();      
        
        learnRule.setMaxIterations(maxItr);
        //learnRule.setBatchMode(false);
        //learnRule.setLearningRate(0.2);
        //learnRule.setMaxError(0.01);
    }

    public void createTraingSet() {
        trainingSet = new TrainingSet<SupervisedTrainingElement>(InputNeurons, OutputNeurons);
    }

    public void learnNN(LinkedList in,LinkedList out, boolean newTh) {
        // add training data to training set (logical OR function)

        //trainingSet = new TrainingSet<SupervisedTrainingElement>(InputNeurons, OutputNeurons);


        /*trainingSet.addElement(new SupervisedTrainingElement(new double[]{0, 0}, new double[]{0}));
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{0, 1}, new double[]{1}));
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{1, 0}, new double[]{1}));
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{1, 1}, new double[]{1}));
         * 
         */
/*
        if(newTh)
        {
            //NN.learn(trainingSet);
        if (first) {
            for (int i = 0; i < list.size(); i++) {
                double inputs[] = new double[InputNeurons];
                double outputs[] = new double[OutputNeurons];
                double[][] data = (double[][]) list.get(i);
                for (int j = 0; j < this.InputNeurons; j++) {
                    inputs[j] = data[0][j];
                }
                //rest for the output
                for (int j = 0; j < data[0].length - this.InputNeurons; j++) {
                    outputs[j] = data[0][j + this.InputNeurons];
                }
                trainingSet.addElement(new SupervisedTrainingElement(inputs, outputs));
            }
            //System.out.println("First");
            NN.learnInNewThread(trainingSet,learnRule);
            first = false;
        } else if (!NN.getLearningThread().isAlive()) {
            for (int i = 0; i < list.size(); i++) {
                double inputs[] = new double[InputNeurons];
                double outputs[] = new double[OutputNeurons];
                double[][] data = (double[][]) list.get(i);
                for (int j = 0; j < this.InputNeurons; j++) {
                    inputs[j] = data[0][j];
                }
                //rest for the output
                for (int j = 0; j < data[0].length - this.InputNeurons; j++) {
                    outputs[j] = data[0][j + this.InputNeurons];
                }
                trainingSet.addElement(new SupervisedTrainingElement(inputs, outputs));
            }
            //System.out.println("Not Alive");
            NN.learnInNewThread(trainingSet, learnRule);
        } else {
            System.out.println("Is Alive");
            try {
                //            NN.pauseLearning();
                NN.getLearningThread().join();
            } catch (InterruptedException ex) {
              
             Logger.getLogger(NeuralNet.class.getName()).log(Level.SEVERE, null, ex);
            }
            //NN.pauseLearning();
            //NN.stopLearning();
            //NN.reset();
            for (int i = 0; i < list.size(); i++) {
                double inputs[] = new double[InputNeurons];
                double outputs[] = new double[OutputNeurons];
                double[][] data = (double[][]) list.get(i);
                for (int j = 0; j < this.InputNeurons; j++) {
                    inputs[j] = data[0][j];
                }
                //rest for the output
                for (int j = 0; j < data[0].length - this.InputNeurons; j++) {
                                outputs[j] = data[0][j + this.InputNeurons];
                }
                trainingSet.addElement(new SupervisedTrainingElement(inputs, outputs));
            }
            //NN.stopLearning();

            
            //System.out.println("Max rate "+ learnRule.getLearningRate());
            //NN.l
            NN.learnInNewThread(trainingSet, learnRule);
        }
            
        }
        //Learn in the same thread
        else
        * 
        */
        {  
            for (int i = 0; i < in.size(); i++) {
                double inputs[] = new double[InputNeurons];
                double outputs[] = new double[OutputNeurons];
                inputs = (double[]) in.get(i);
                outputs = (double[]) out.get(i);                
                trainingSet.addElement(new SupervisedTrainingElement(inputs, outputs));
            }
            NN.learn(trainingSet, learnRule); 
            //NN.learn(trainingSet); 
           // printNeuronsError();
        }
        
        System.out.println("Learning Complete");
    }

    public void saveNN(String location) {
       
        NN.save(location);

    }

    public void loadNN(String location) {
        NN.load(location);
    }

    public double[] calculateOutput(double[] inputVector) {
        double[] res = null;
        
        NN.setInput(inputVector);
        NN.calculate();
        res = NN.getOutput();
        //printNeuronsError();
        return res;
    }
    
    public void printNeuronsError()
    {
        System.out.print("\nError in OutputNeuro");
        for(int i=0;i<OutputNeurons;i++)
        {
            System.out.print("\t"+NN.getOutputNeurons().get(i).getError());
        }
        System.out.println();
    }
}