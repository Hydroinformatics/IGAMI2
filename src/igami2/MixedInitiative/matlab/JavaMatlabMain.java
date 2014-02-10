/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.MixedInitiative.matlab;

import JavaMat.MatLabMain;
import com.mathworks.toolbox.javabuilder.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author VIDYA
 */
public class JavaMatlabMain {

    MWNumericArray res = null;
    Object fis = null; //ANFIS FIS
    Object neuralnet = null; //save the best neuralnet
    Object neiveBayes = null; //save the best Naive Bayes Classifier
    
    double [] Weights; //Least Square Fit
    private double[] ratings = null;
    MatLabMain matlab;

    public JavaMatlabMain() {
        try {
            matlab = new MatLabMain();
        } catch (MWException ex) {
            ex.printStackTrace();
        }
    }

    /*
    public Object[] createNeuralNetClassifier(double[][] dataTrain,double[][] dataTarget, int noHiddenLayers, int trainPer, int valPer, int testPer, int trailNos) {
        
        MWNumericArray numTrain = new MWNumericArray(dataTrain, MWClassID.DOUBLE);
        MWNumericArray numTarget = new MWNumericArray(dataTarget, MWClassID.DOUBLE);
        Object[] result = null;

        try {
            result = matlab.createNeuralNetClassifier(2,numTrain,numTarget,noHiddenLayers,trainPer,valPer,testPer,trailNos);
            neuralnet = result[0]; //Neural Net Object
            MWNumericArray arr = (MWNumericArray) result[1];
            result[1] = arr.getInt();
        } catch (MWException ex) {
            ex.printStackTrace();
        }
        return result;

    }
    
    public int[] runNeuralNetClassifier(Object net, double[][] dataTrain) {
        
        MWNumericArray numTrain = new MWNumericArray(dataTrain, MWClassID.DOUBLE);
        Object[] result = null;
        int[] ranks=null;

        try {
            result = matlab.runNeuralNetClassifier(1,net,numTrain);
            MWNumericArray arr = (MWNumericArray) result[0];
            ranks = (int[]) arr.toIntArray();
        } catch (MWException ex) {
            ex.printStackTrace();
        }
        return ranks;

    }
    
     public Object[] createNaiveBayesClassifier(double[][] dataTrain,double[][] dataTarget) {
        
        MWNumericArray numTrain = new MWNumericArray(dataTrain, MWClassID.DOUBLE);
        MWNumericArray numTarget = new MWNumericArray(dataTarget, MWClassID.DOUBLE);
        Object[] result = null;
        try {
            //result = matlab.createNBClassifier(2,numTrain,numTarget);
            matlab.createNBClassifier(2,numTrain,numTarget);
            neiveBayes = result[0];
            MWNumericArray arr = (MWNumericArray) result[1];
            result[1] = arr.getInt();//error
            MWNumericArray arr1 = (MWNumericArray) result[2];
            result[2] = arr1.getInt(); //id , save which type of Naive Bayes is the best for current data, 1-Normal, 2-Kernel, 3-Multivatiate multinomial distribution
        } catch (MWException ex) {
            ex.printStackTrace();
        }
        return result;
    }
     
     public int[] runNaiveBayesClassifier(Object naiveBayes, double[][] dataTrain) {
        
        MWNumericArray numTrain = new MWNumericArray(dataTrain, MWClassID.DOUBLE);
        //MWNumericArray numTarget = new MWNumericArray(dataTarget, MWClassID.INT32);
        Object[] result = null;
        int[] ranks = null;
        try {
            result = matlab.runNBClassifier(1,naiveBayes,numTrain);
            MWNumericArray arr = (MWNumericArray) result[0];
            ranks = arr.getIntData();
        } catch (MWException ex) {
            ex.printStackTrace();
        }
        return ranks;
    }
     * 
     */
    
     public Object[] createTrainTestData(double[][] dataTrain) {
        
        MWNumericArray numTrain = new MWNumericArray(dataTrain, MWClassID.DOUBLE);
        Object[] result = null;
        double per=0.15;
        try {
            result = matlab.partitionTrainTestData(2,numTrain,per);
            MWNumericArray arr = (MWNumericArray) result[0];
            double[][] traindata = (double[][]) arr.toDoubleArray();
            result[0] = traindata;
            MWNumericArray arr1 = (MWNumericArray) result[1];
            double[][] testdata = (double[][]) arr1.toDoubleArray();
            result[1] = testdata;
        } catch (MWException ex) {
            ex.printStackTrace();
        }
        return result;
    }
    
     
    public Object[] createDiscriminantClassifier(double[][] dataTrain,double[][] dataTarget) {
        
        MWNumericArray numTrain = new MWNumericArray(dataTrain, MWClassID.DOUBLE);
        MWNumericArray numTarget = new MWNumericArray(dataTarget, MWClassID.DOUBLE);
        Object[] result = null;
        try {
            result = matlab.createDiscriminantClassifier(3,numTrain,numTarget);
            MWNumericArray arr = (MWNumericArray) result[0];
            int[] errors = arr.getIntData();
            result[0] = errors; //Errors in running various discriminant functions
            MWNumericArray arr1 = (MWNumericArray) result[1];
            result[1] = arr1.getInt();//Min Error
            MWNumericArray arr2 = (MWNumericArray) result[2];
            result[2] = arr2.getInt();//Id of the best discriminat //convert to integer, save which type of Naive Bayes is the best for current data, 1-Normal, 2-Kernel, 3-Multivatiate multinomial distribution
        } catch (MWException ex) {
            ex.printStackTrace();
        }
        return result;
    }
    
    public int[] runDiscriminantClassifier(double[][] dataSample,double[][] dataTrain,double[][] dataTarget, int id) {
        
        MWNumericArray numSample = new MWNumericArray(dataSample, MWClassID.DOUBLE);
        MWNumericArray numTrain = new MWNumericArray(dataTrain, MWClassID.DOUBLE);
        MWNumericArray numTarget = new MWNumericArray(dataTarget, MWClassID.DOUBLE);
        Object[] result = null;
        int[] ranks = null;
        try {
            result = matlab.runDiscriminantClassifier(1,numSample,numTrain,numTarget,id);
            MWNumericArray arr = (MWNumericArray) result[0];
            ranks = arr.getIntData();
            //result[0] = Weights; //Errors in running various discriminant functions
            
        } catch (MWException ex) {
            ex.printStackTrace();
        }
        return ranks;
    }
    
    /*
     * Create Multivariate Random data based on the training data and target data
     */
    
    public Object[] createLinearRandomData(double[][] dataTrain,double[][] dataTarget, int id, int per) {
        
        MWNumericArray numTrain = new MWNumericArray(dataTrain, MWClassID.DOUBLE);
        MWNumericArray numTarget = new MWNumericArray(dataTarget, MWClassID.DOUBLE);
        Object[] result = null;
        int[] ranks = null;
        try {
            result = matlab.createLinearRandomData(2,numTrain,numTarget,id,per);
            
            MWNumericArray traindataNew = (MWNumericArray) result[0];
            result[0] =(double[][]) traindataNew.toDoubleArray();
            MWNumericArray targ = (MWNumericArray) result[1];
            result[1] = targ.toDoubleArray();
            
        } catch (MWException ex) {
            ex.printStackTrace();
        }
        return result;
    }
    
    public int compareErrorVectors(int[][] vect1, int[][] vect2)
    {
        int res=0;
        int sucess=0;
        for(int i=0;i<vect1.length;i++)
        {
            if(vect1[i][0]==vect2[i][0])
                sucess++; //got sucess comparision
        }
        res = vect1.length-sucess;//no of failures
        
        return res;
    }
    
    /*
     * Use to create a N fold test
     * which divides the total train data in N various train and test sets
     * use kfold test
     * @TrainGrp //training groups
     * 
     * return
     * a 2-D Array of various training sets,
     * each Column, contains 1 or 0 - indicating if value is selected for training or not, otherwise value is used for testing
     */
     
    
    public Object[] createNFoldTest(double[] TrainGrp, int folds) {
        
        MWNumericArray numTrain = new MWNumericArray(TrainGrp, MWClassID.DOUBLE);
        Object[] result = null;
        try {
            result = matlab.createNFoldTest(2,numTrain,folds);
            MWNumericArray arr = (MWNumericArray) result[0];
            double[][] TrainSets = (double[][]) arr.toDoubleArray();
            result[0] = TrainSets; //the new training set
            MWNumericArray arr1 = (MWNumericArray) result[1];
            result[1] = arr1.getInt();//test set size, generally 10%
            
        } catch (MWException ex) {
            ex.printStackTrace();
        }
        return result;
    }
     
    
    public Object createANFIS(double[][] dataTrain, double ff, int epoc) {
        
        MWNumericArray numTrain = new MWNumericArray(dataTrain, MWClassID.DOUBLE);
        Object[] result = null;

        try {
            result = matlab.createANFIS(1, numTrain, ff, epoc);
            fis = result[0];
        } catch (MWException ex) {
            ex.printStackTrace();
        }
        return fis;

    }

    public double[] testANFIS(double[][] dataTest, Object fis) {
        MWNumericArray numTest = new MWNumericArray(dataTest, MWClassID.DOUBLE);
        Object[] result = null;
        
        try {
            result = matlab.runANFIS(1, numTest, fis);
        } catch (MWException ex) {
            ex.printStackTrace();
        }
        res = (MWNumericArray) result[0];
        ratings = res.getDoubleData();
        return ratings;
    }

    public double[] createLeastSquareFit(double[][] dataTrain, double[][] rank) {
        MWNumericArray numTrain = new MWNumericArray(dataTrain, MWClassID.DOUBLE);
        Object[] result = null;

        try {
            result = matlab.createLeastsquarefit(1, numTrain,rank);
            MWNumericArray arr = (MWNumericArray) result[0];
            Weights = arr.getDoubleData();
        } catch (MWException ex) {
            ex.printStackTrace();
        }
        return Weights;
    }

    public double[] testLeastSquareFit(double[][] dataTest, double[] weights) {

        double[] rating = new double[dataTest.length];
        for(int i=0;i<dataTest.length;i++) //Matrix Multiplication
        {
            rating[i]=0;
            for(int j=0;j<dataTest[0].length;j++)
            {
                rating[i] = rating[i]+ dataTest[i][j]*weights[j];
            }
        }        
        return rating;
    }

}
