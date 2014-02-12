/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.MixedInitiative;

import igami2.DataBase.IndividualDesignManager;
import igami2.DataBase.hibernateconfig.SdmData;
import igami2.DistributedSystem.VTimer;
import igami2.IGAMI2Main;
import igami2.MixedInitiative.matlab.JavaMatlabMain;
import igami2.MixedInitiative.neuralnetneuroph.NNManager;
import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import java.io.*;
import java.util.LinkedList;

/**
 *
 * @author VIDYA
 */
public class SDMManager {

    private IndividualDesignManager idm;
    int training = 70;
    LinkedList<NNManager> nnmList; //The list of all the NN based SDMs
    LinkedList<ANFIS> ANFISList; //The list of all the ANFIS based SDMs
    LinkedList<double[]> LSFList; //The list of all the Leaset Square Fit based SDMs
    private LinkedList<Individual> popTrain;
    private LinkedList<Individual> popTest;
    int SDMCount = 0;
    private final StatisticsManager statm;
    private int[] chosenFF;
    public NNManager bestNNM; //saves the best Neural Network
    public double[][] bestScaleParam;
    public NNManager currentNNM;
    public NNManager cumNNM; //Neural Net created by combining the data from all the previously generated model
    public NNManager currentNNMExtended; //By adding more training data using Linear Models
    public NNManager cumNNMExtended; //By adding more traning data using lienar Models for cumulative data
    private String NNName = "NeuralNet";
    JavaMatlabMain mat;
    public ANFIS currentANFIS;
    public double[] currentLSF;
    private double Area_MAX = 800;//603.96; //Not used
    private double Flow_MAX = 56.05;
    private double Cost_MAX = 41916048;
    private double Soil_MAX = 401109.7;
    private double Nitrate_MAX = 7281552;
    private double Pesticide_MAX = 100; //Not Used
    private double WtCost = 1.5;//give the cost a weight of 3
    private int trainingPercent = 80; //can be varied
    int UserId = 0;
    private int totalIndv;
    private int input;
    private int output = 3;//for three different ratings
    private int anfis_epoc = 10; //default epoc for ANFIS
    private int anfis_Mfs = 3; //default epoc for ANFIS
    private boolean localPref = IGAMI2Main.localPrefs;
    private int[] localSubbasinIds;
    public boolean usingBMPAssign = IGAMI2Main.localAssignBMP; //true means using the BMP
    public int NoOfBMPChosens;
    private int[] chosenBMP;
    private boolean useANFIS = false;
    private int currentLinearId;
    LinkedList NormalizedModeldata = null;
    LinkedList ScaleParamModeldata = null; 
    private int selectModelId=0;//tell which model was selected
    private int randomDataPer = 50; //50% more Random data being generated
    private final LinkedList<NNManager> nnmListCum;
    private final LinkedList<NNManager> nnmListExt;
    private final LinkedList<NNManager> nnmListCumExt;
    
    private double minErrorPerc = 0;
    private double minErrorNN = 0;
    private int NNwin = 999; //Stores which type of Neurale Network out of 4 network won the competition
    private boolean useCum = false;
    private String userDIR = "../SWAT/USER/user";

    public SDMManager(IndividualDesignManager idm, StatisticsManager statm, int[] chosenFF, int[] localSubbasinIds, boolean useLocalPreference, int[] chosenBMP, boolean useANFIS) {
        this.statm = statm;
        this.idm = idm;
        this.chosenFF = chosenFF;
        this.useANFIS = useANFIS;
        popTrain = new LinkedList<Individual>();
        popTest = new LinkedList<Individual>();
        UserId = idm.UserId;
        nnmList = new LinkedList<NNManager>();
        nnmListCum = new LinkedList<NNManager>();
        nnmListExt = new LinkedList<NNManager>();
        nnmListCumExt = new LinkedList<NNManager>();
        ANFISList = new LinkedList<ANFIS>();
        LSFList = new LinkedList<double[]>();
        this.localPref = useLocalPreference;
        this.localSubbasinIds = localSubbasinIds;
        this.chosenBMP = chosenBMP;
        NoOfBMPChosens = getNoOfBMPs();
        input = initNoOfInputs();
        NormalizedModeldata = new LinkedList();
        ScaleParamModeldata = new LinkedList();
        userDIR = userDIR + UserId+"/";
        File f = new File(userDIR);
        if(!f.exists())
        {
            f.mkdirs();
        }
        //currentSDM = nnm;//point to the first SDM
        mat = new JavaMatlabMain();
    }
    public void createNewSDM(String type) {
        if (type.compareTo("NeuralNetwork") == 0) {
            currentNNM = new NNManager(input, output, chosenFF);            
            currentNNMExtended = new NNManager(input,output,chosenFF);
            if(useCum)
            {
                cumNNMExtended = new NNManager(input,output,chosenFF);
                cumNNM = new NNManager(input,output,chosenFF);
            }
        } else {
            //System.out.println("Please Specify the type");
        }
        popTrain = new LinkedList<Individual>();
        popTest = new LinkedList<Individual>();
    }
   
    private void saveSDM(int UserId, int SDMCount, NNManager currentNNM, NNManager currentNNMExtended, NNManager cumNNM, NNManager cumNNMExtended, ANFIS currentANFIS, int[] linearModelErrors) {

        String loc = "";
        File f = null;
        //save all the individual NNM
        loc = userDIR + NNName + this.SDMCount;
        f = new File(loc);
        if (f.exists()) {
            f.delete(); //delete the file from the previous search
        }
        currentNNM.saveNN(loc);
        loc = userDIR + NNName + "Ext" + SDMCount;
        f = new File(loc);
        if (f.exists()) {
            f.delete();
        }
        currentNNMExtended.saveNN(loc);

        if (useCum) {
            loc = userDIR + NNName + "Cum" + SDMCount;
            f = new File(loc);
            if (f.exists()) {
                f.delete();
            }
            cumNNM.saveNN(loc);
            loc = userDIR + NNName + "CumExt" + SDMCount;
            f = new File(loc);
            if (f.exists()) {
                f.delete();
            }
            cumNNMExtended.saveNN(loc);
        }
        
        
        idm.dbm.saveSDM(UserId, SDMCount, null, null, null, null, null, linearModelErrors); //need to remove all the useless data
        System.out.println("Saved the current SDM");
    }
   
    void loadPrevSDMs() {
        String loc = "";
        File f = null;
        int SDMCount=0;
        LinkedList<SdmData> sdmDataList = idm.dbm.getSDMData();
        SDMCount = sdmDataList.size();
        if(SDMCount>0)
        {
        SdmData sdmData = sdmDataList.get(SDMCount-1);
        boolean loadCum = false;
        //save all the individual NNM
        for (int i = 0; i < SDMCount; i++) {
            loc = userDIR + NNName + i;
            f = new File(loc);
            if (f.exists()) {
                createNewSDM("NeuralNetwork"); //all four types created automatically
                this.currentNNM.loadNN(loc);
            }
            

            loc = userDIR + NNName + "Ext" + i;
            f = new File(loc);
            if (f.exists()) {
               currentNNMExtended.loadNN(loc);
            }
            
            if (loadCum) {
                loc = userDIR + NNName + "Cum" + i;
                f = new File(loc);
                if (f.exists()) {
                    cumNNM.loadNN(loc);
                }
                
                loc = userDIR + NNName + "CumExt" + i;
                f = new File(loc);
                if (f.exists()) {
                    cumNNMExtended.loadNN(loc);
                }                
            }
            else {
               cumNNM = null;
               cumNNMExtended = null;
            }
            this.useCum = true;
            loadCum=true;
            //this.useCum = true;
            nnmList.add(currentNNM);        
            nnmListExt.add(currentNNMExtended);
            nnmListCum.add(cumNNM); //add the null model also
            nnmListCumExt.add(cumNNMExtended);
        }
        
        //load previous SDM Data for cumulative model
        LinkedList SDMModelData = idm.dbm.getSDMModellingData();
        this.NormalizedModeldata = SDMModelData;
        
        if (nnmList.size() > 0) {
            
            //load the bestNNM
            
            this.bestNNM = new NNManager(input, output, chosenFF);
            loc = userDIR + "bestNNM";
            bestNNM.loadNN(loc);
            
            //load Range data 
            String rangeStr = sdmData.getRange();
            String[] rangeAr = rangeStr.split(",");
            double[][] range = new double[this.input][2];
            for(int i=0;i<range.length;i++)
            {
                range[i][0] = Double.parseDouble(rangeAr[i*2]);
                range[i][1] = Double.parseDouble(rangeAr[i*2+1]);               
            } 
            
            this.selectModelId = 0;
            bestScaleParam = range;
        }
        this.SDMCount = SDMCount+1; //for next generation
        }        
    }
    /*
     * Multiple different models are being run now
     */
    
    public double trainAndTestNormalizedMultiplModels() {
        LinkedList<Individual> Indv = idm.TotalIndvs;

        System.out.println("Doing SDM Training for User "+UserId);
        double[][] scaleParam = findMinRange(Indv);
        double[][] NormalizedData = normalizeData(Indv, scaleParam); //save
        NormalizedModeldata.add(NormalizedData); //save the normalized data for future Models
        ScaleParamModeldata.add(scaleParam);
        if(NormalizedModeldata.size()>1) //morethan one model data is available, begin the combined model
        {
            useCum = true;
        }
        
        createNewSDM("NeuralNetwork");
        //printNormalizedData(NormalizedData);
        
        /*
        totalIndv = Indv.size();        
        //using NFoldCrossValidation
        mat.createNFoldTest(currentLSF, input);
        int traningIndv = trainingPercent * totalIndv / 100; //partition training and test set
        int testIndv = totalIndv - traningIndv;
        double[][] dataTrain = getDataTrain(NormalizedData, traningIndv);
        double[][] dataTest = getDataTest(NormalizedData, traningIndv);        
        * 
        */
        //assuming training and test Indv as same
        totalIndv = Indv.size();
        int traningIndv =totalIndv;
        int testIndv = totalIndv;
        double[][] data = getDataTrain(NormalizedData, totalIndv);
        Object[] ob = this.mat.createTrainTestData(data);
        double[][] dataTrain = (double[][]) ob[0];
        double[][] dataTest = (double[][]) ob[1];
        double[][] dataTrainNew = null;
        double[][] dataTrainCumNew = null;
        double[][] rdataTrainNew = null; //only random data with linear model Rank  save
        double[][] rdataTrainCumNew = null;//only random data with linear model Rank save

        
        Object[] obj = trainCurrentLinearModels(dataTrain,dataTest);
        currentLinearId = Integer.parseInt(""+obj[2]); //save the id of the current Linear Discriminant Function

        int[] linearModelErrors = (int[]) obj[0]; //save
        int linearModelMinError = Integer.parseInt(""+obj[1]); //save
        
        minErrorPerc = linearModelMinError*100/testIndv;
        //create Random Test data for CurrentNN
        Object[] dat = createLinearRandomData(dataTrain);
        dataTrainNew = (double[][]) dat[0];
        rdataTrainNew = (double[][]) dat[1];
        
        //do the trainings using Indv
        currentNNM.learnNormalize(dataTrain);
        
        currentNNMExtended.learnNormalize(dataTrainNew);
        
        if(useCum) //morethan one model data is available, begin the combined model
        {
            //save the model data of previous NN
            //add the training data of current NN
            //don't add all the data of current NN, as it will include the test data as well
            int len = ((double[][]) NormalizedModeldata.get(0)).length;
            len = len*(NormalizedModeldata.size()-1)+dataTrain.length;
            int wid = dataTrain[0].length;
            double[][] cumdataTrain = new double[len][wid];
            
            //copy data
            int base = 0;
            for(int i=0;i<(NormalizedModeldata.size()-1);i++)
            {
                double[][] tmp= (double[][]) NormalizedModeldata.get(i);
                for(int j=0;j<tmp.length;j++)
                    cumdataTrain[base+j] = tmp[j];
                base = base + tmp.length;
            }
            
            for(int j=0;j<dataTrain.length;j++)
            {
                cumdataTrain[base+j] = dataTrain[j];
            }
            cumNNM.learnNormalize(cumdataTrain);
            
            //create Extended Model
            Object[] datacumNN = createLinearRandomData(cumdataTrain);           
            dataTrainCumNew = (double[][]) datacumNN[0];
            rdataTrainCumNew = (double[][]) datacumNN[1];           
            cumNNMExtended.learnNormalize(dataTrainCumNew);
        }
        
        
        //create Random Test data for cumNN
        
        if(useANFIS)
            currentANFIS = new ANFIS(trainCurrentANFIS(dataTrain));
        
        

        idm.dbm.saveModellingData(NormalizedData,idm.global_sessionId,idm.local_sessionId, Indv,rdataTrainNew,rdataTrainCumNew);  

        System.out.println("Finished the SDM Training for user "+this.UserId);


        //save the current SDMs
        double result = 0;//send the testing error of the best SDM

        double meanHDM = 0;
        nnmList.add(currentNNM);        
        nnmListExt.add(currentNNMExtended);
        nnmListCum.add(cumNNM); //add the null model also
        nnmListCumExt.add(cumNNMExtended);
        
        if(useANFIS)
            ANFISList.add(currentANFIS);
        
        double[] ErrorList = new double[nnmList.size()];
        double[] ErrorListExt = new double[nnmList.size()];
        double[] ErrorListCum = new double[nnmList.size()];
        double[] ErrorListCumExt = new double[nnmList.size()];
        double SDMRanksNN[][] = new double[nnmList.size()][testIndv]; //save the SDM Ranks for all SDMs
        double SDMRanksNNExt[][] = new double[nnmList.size()][testIndv]; //save the SDM Ranks for all SDMs
        double SDMRanksNNCum[][] = new double[nnmList.size()][testIndv]; //save the SDM Ranks for all SDMs
        double SDMRanksNNCumExt[][] = new double[nnmList.size()][testIndv]; //save the SDM Ranks for all SDMs
        double SDMRanksANFIS[][] = new double[nnmList.size()][testIndv]; //save the SDM Ranks for all SDMs
        //double SDMRanksLSF[][] = new double[nnmList.size()][testIndv]; //save the SDM Ranks for all SDMs

        double[] HumanR = new double[testIndv];

        int c = 0;
        for (int i = 0; i < NormalizedData.length; i++) {

            HumanR[c++] = NormalizedData[i][input];
        }

        idm.SDMGeneration = SDMCount; //Add the new SDMGeneration


        //the most recent last

        //System.out.println("Testing Neural Network");
        for (int j = 0; j < nnmList.size(); j++) {

            //System.out.println("Running the Test for SDM_NN " + j);
            NNManager nnm = nnmList.get(j);
            NNManager nnmExt = nnmListExt.get(j);
            NNManager nnmCum = null;
            NNManager nnmCumExt = null;


            double[] sdmR = new double[testIndv];
            double[] sdmRExt = new double[testIndv];
            double[] sdmRCum = new double[testIndv];
            double[] sdmRCumExt = new double[testIndv];


            double error = 0;
            double errorExt = 0;
            double errorCum = 0;
            double errorCumExt = 0;

            if (useCum) {
                nnmCum = nnmListCum.get(j);
                nnmCumExt = nnmListCumExt.get(j);
            }
            c = 0;
            for (int i = 0; i < NormalizedData.length; i++) {
                double[] inp = new double[this.input];
                for (int k = 0; k < input; k++) {
                    inp[k] = NormalizedData[i][k];
                }

                double[] nnVal = nnm.testNNNormalize(inp);
                sdmR[c] = getNNRank(nnVal);

                double[] nnValExt = nnmExt.testNNNormalize(inp);
                sdmRExt[c] = getNNRank(nnValExt);


                if (!(HumanR[c] - sdmR[c] == 0)) //Not same rating
                {

                    error++;
                }

                if (!(HumanR[c] - sdmRExt[c] == 0)) //Not same rating
                {

                    errorExt++;
                }

                //use Cumulative Models
                if (useCum) {
                    if (nnmCum != null) {
                        double[] nnValCum = nnmCum.testNNNormalize(inp);
                        sdmRCum[c] = getNNRank(nnValCum);
                        double[] nnValCumExt = nnmCumExt.testNNNormalize(inp);
                        sdmRCumExt[c] = getNNRank(nnValCumExt);

                        //System.out.print("\t" + sdmRCum[c]);
                        //System.out.print("\t" + sdmRCumExt[c]);
                        if (!(HumanR[c] - sdmRCum[c] == 0)) //Not same rating
                        {

                            errorCum++;
                        }
                        if (!(HumanR[c] - sdmRCumExt[c] == 0)) //Not same rating
                        {

                            errorCumExt++;
                        }
                    } //if nn is null, error is max error
                    else {
                    errorCum = testIndv;
                    errorCumExt = testIndv;
                    }
                } else {
                    errorCum = testIndv;
                    errorCumExt = testIndv;
                }
                c++;
            }

            SDMRanksNN[j] = sdmR;
            SDMRanksNNExt[j] = sdmRExt;
            SDMRanksNNCum[j] = sdmRCum;
            SDMRanksNNCumExt[j] = sdmRCumExt;

            ErrorList[j] = (error * 100 / testIndv);
            ErrorListExt[j] = (errorExt * 100 / testIndv);
            ErrorListCum[j] = (errorCum * 100 / testIndv);
            ErrorListCumExt[j] = (errorCumExt * 100 / testIndv);
        }

        
        double[] minErrorNNVals = new double[4];
        double[] minErrorNNID = new double[4];
        
        //find the best having min Error
        int last = ErrorList.length - 1;

        //find the min error NN
        double min = ErrorList[last]; //assume the last as min
        int res = last; //pointer to min error SDM
        for (int i = last - 1; i >= 0; i--) {
            if (ErrorList[i] < min) {
                min = ErrorList[i];
                res = i;//new min error SDM
            }
        }
        selectModelId = res;
        minErrorNNVals[0] = min;
        minErrorNN = min;
        minErrorNNID[0] = res;
        bestNNM = nnmList.get(res);// take the res SDM as the most effective SDM
        NNwin = 0;
        
        
        min=ErrorListExt[last];
        res = ErrorListExt.length - 1;
        for (int i = last - 1; i >= 0; i--) {
            if (ErrorListExt[i] < min) {
                min = ErrorListExt[i];
                res = i;//new min error SDM
            }
        }
        minErrorNNVals[1] = min;
        minErrorNNID[1] = res;
        if(min<minErrorNN)
        {
            minErrorNN = min;
            bestNNM = nnmListExt.get(res);
            selectModelId = res;
            NNwin = 1;
        }
        
        if(useCum) //morethan one model data is available, begin the combined model
        {
            min = ErrorListCum[last];
            res = ErrorListCum.length - 1;
            for (int i = last - 1; i >= 0; i--) {
                if (ErrorListCum[i] < min) {
                    min = ErrorListCum[i];
                    res = i;//new min error SDM             
                }
            }
            minErrorNNVals[2] = min;
            minErrorNNID[2] = res;
            if (min < minErrorNN) {
                minErrorNN = min;
                bestNNM = nnmListCum.get(res);
                selectModelId = res;
                NNwin = 2;
            }

            min = ErrorListCumExt[last];
            res = ErrorListCumExt.length - 1;
            for (int i = last - 1; i >= 0; i--) {
                if (ErrorListCumExt[i] < min) {
                    min = ErrorListCumExt[i];
                    res = i;//new min error SDM
                }
            }
            minErrorNNVals[3] = min;
            minErrorNNID[3] = res;
            if (min < minErrorNN) {
                minErrorNN = min;
                bestNNM = nnmListCumExt.get(res);
                selectModelId = res;
                NNwin = 3;
            }

        }
        else
        {
            minErrorNNVals[2] = 100;
            minErrorNNVals[3] = 100;
        }
        
       if(minErrorPerc<minErrorNN)
       {
           System.out.println("Linear Model Better than Neural Network");           
       }
       
       
       //save the NNs with Best one
       saveSDM(UserId, SDMCount, currentNNM, currentNNMExtended, cumNNM, cumNNMExtended, currentANFIS, linearModelErrors);
        
        //save the Actual ratings of the best NNM
        double[][] NNValues = new double[testIndv][this.output];
        double[] NNRank = new double[testIndv];
        c = 0;
        for (int i = traningIndv; i < NormalizedData.length; i++) {
            double[] inp = new double[this.input];
            for (int k = 0; k < input; k++) {
                inp[k] = NormalizedData[i][k];
            }

            double[] nnVal = currentNNM.testNNNormalize(inp);
            NNRank[c] = getNNRank(nnVal);
            for (int j = 0; j < output; j++) {
                NNValues[c][j] = nnVal[j];
            }
            c++;
        }
        
        
        double[] ANFISValues = new double[testIndv];
        double[] ANFISR = new double[testIndv];
        double minANFIS = 0;
        int minANFISID = 0;
        
        
        
        if (useANFIS) {
            
            double[][] testingData = new double[dataTest.length][this.input]; //NxM
        
        for (int i = 0; i < dataTest.length; i++) {
            int j = 0;
            for (; j < input; j++) {

                testingData[i][j] = dataTest[i][j];
            }
            
        }
            double[] ANFISErrors = new double[ANFISList.size()];
            for (int j = 0; j < ANFISList.size(); j++) {
                //double[] lsfWeights = LSFList.get(j);

                Object fis = ANFISList.get(j);
                double[][] tmpANFIS = testANFIS(testingData, fis);
                ANFISR = tmpANFIS[0];
                ANFISValues = tmpANFIS[1];
                
                SDMRanksANFIS[j] = ANFISR;

                int errorAnfis = 0;
                for (int i = 0; i < HumanR.length; i++) {

                    if (!(HumanR[i] - ANFISR[i] == 0)) //Not same rating
                    {
                        errorAnfis++;
                    }
                }
                ANFISErrors[j] = errorAnfis*100/testIndv;
            }
            
            //find minError ANFIS
             int pos=ANFISErrors.length-1;
            min=ANFISErrors[ANFISErrors.length-1];           
            for(int i=0;i<ANFISErrors.length-1;i++)
            {
                if(ANFISErrors[i]<min)
                {
                    min = ANFISErrors[i];
                    pos = i;
                }
            }
            minANFIS=min; //save
            minANFISID=pos; //save
            this.currentANFIS = (ANFIS) this.ANFISList.get(pos); //location of the best ANFIS
        }
        
        if(selectModelId==SDMCount) // New Model better than previous model
            bestScaleParam = scaleParam;
        

        idm.dbm.saveSDMData(UserId,idm.global_sessionId, SDMCount, NNwin, minErrorNN, minErrorNNVals, minErrorNNID, minErrorPerc, currentLinearId, linearModelErrors,minANFIS,minANFISID, bestScaleParam);
       
        // for next SDM        
        SDMCount++;        
        result = minErrorNN;// error from the selected SDM
        popTrain = new LinkedList<Individual>();
        return result;
    }
    
    public void doSDMRating() {
        System.out.println("Doing SDM Rating for User "+UserId);

        double[][] NormalizedData = normalizeData(idm.IndvPopulation, (double[][])bestScaleParam);
        
        for (int i = 0; i < NormalizedData.length; i++) {
            Individual in = idm.IndvPopulation.get(i);
           
            double[] dataIn = new double[this.input];
            for(int j=0;j<dataIn.length;j++)
            {
                dataIn[j] = NormalizedData[i][j];
            }
            //in.rating = (int) getNNRank(bestNNM.testNN(in));
            in.rating = (int) getNNRank(bestNNM.testNNScaled(dataIn));
            //in.printFitnessValues();
            //System.out.print("\t SDM Gave Rating " + in.rating);
            //System.out.println();
        }        
        //save the SDM Rating Indv
        //idm.dbm.saveSDMRating(UserId,idm.global_sessionId, idm.local_sessionId, idm.sessionId, idm.session_type_search,idm.IndvPopulation,NormalizedData);
    }

    private double[][] testANFIS(double[][] dataTest, Object fis) {
        double[][] rating = new double[2][dataTest.length];

        double[] tmp = mat.testANFIS(dataTest, fis);
        for (int i = 0; i < tmp.length; i++) {
            double val = tmp[i];
            rating[1][i] = val;
            if (val < 1.5) {
                rating[0][i] = 1;
            } else if (val < 2.5) {
                rating[0][i] = 2;
            } else {
                rating[0][i] = 3;
            }
        }
        return rating;
    }

    private int initNoOfInputs() {
        int res = 0;


        for (int i = 0; i < chosenFF.length; i++) {
            if (chosenFF[i] == 1) {
                input++;
            }
        }
        input--;//remove the last for RatingFF

        if (localPref) //if considering the local preferences then find the local subbasinIds
        {

            if(localSubbasinIds!=null)
            {
            if (this.usingBMPAssign) {
        
                input = input * (1 + localSubbasinIds.length)+ localSubbasinIds.length*NoOfBMPChosens;
            } else {
                input = input * (1 + localSubbasinIds.length);
            }
            }
            //else no local subbasin
        }
        res = input;
        return res;
    }

    private double[][] findMinRange(LinkedList<Individual> Indv) {
        double[][] res = new double[input][2]; //one for min, other for range

        //init 0

        for (int i = 0; i < res.length; i++) {
            res[i][0] = 0; //min
            res[i][1] = 0; //max
        }


        int ffStrt = 0; //begin of input FF
        int c = 0; //position of ff
        //Start min values first
        { //for scope of i
            int i = 0;//copy first value
            Individual in = Indv.get(i); // for 0
            ffStrt = 0;
            //for entire watershed            
            c = 0;//ff begin from 0
            for (int j = 0; j < chosenFF.length - 1; j++) {
                //use the j+1, skip the area
                if (chosenFF[j] == 1) {
                    double val = Math.abs(in.fitnessValues[j]);
                    res[ffStrt + c][0] = val;
                    res[ffStrt + c][1] = val;
                    c++;
                }
            }

            if (this.localPref) {
                //for all the subbasins
                if(localSubbasinIds!=null)
            {
                for (int k = 0; k < this.localSubbasinIds.length; k++) {
                    ffStrt = ffStrt + c;
                    c = 0;

                    for (int j = 0; j < chosenFF.length - 1; j++) {
                        //use the j+1, skip the area
                        if (chosenFF[j] == 1) {
                            double val = Math.abs(in.subbasinsFF[localSubbasinIds[k]][j]);
                            res[ffStrt + c][0] = val;
                            res[ffStrt + c][1] = val;
                            c++;
                        }
                    }

                }
                //for BMP Assignment
                if (this.usingBMPAssign) {
                    double[] assign = in.assignments;

                    //for all the subbasins
                    for (int k = 0; k < this.localSubbasinIds.length; k++) {
                        ffStrt = ffStrt + c;
                        c = 0;

                        for (int j = 0; j < this.NoOfBMPChosens; j++) {
                            //use the j+1, skip the area
                            //if (chosenBMP[j] == 1) {
                                double val = Math.abs(assign[j * 108 + localSubbasinIds[k]]);
                                res[ffStrt + c][0] = val;
                                res[ffStrt + c][1] = val;
                                c++;
                           // }
                        }

                    }
                }
            }
            }
        }


        ffStrt = 0; //begin of input FF
        c = 0; //position of ff
        for (int i = 1; i < Indv.size(); i++) {
            Individual in = Indv.get(i);
            ffStrt = 0;
            //for entire watershed            
            c = 0;//ff begin from 0
            for (int j = 0; j < chosenFF.length - 1; j++) {
                //use the j+1, skip the area
                if (chosenFF[j] == 1) {
                    double val = Math.abs(in.fitnessValues[j]);
                    if ((res[ffStrt + c][0]) > val) //min
                    {
                        res[ffStrt + c][0] = val;
                    }
                    if ((res[ffStrt + c][1]) < val) //max
                    {
                        res[ffStrt + c][1] = val;
                    }
                    c++;
                }
            }

            if (this.localPref) {
                if(localSubbasinIds!=null)
            {
                //for all the subbasins
                for (int k = 0; k < this.localSubbasinIds.length; k++) {
                    ffStrt = ffStrt + c;
                    c = 0;

                    for (int j = 0; j < chosenFF.length - 1; j++) {
                        //use the j+1, skip the area
                        if (chosenFF[j] == 1) {
                            double val = Math.abs(in.subbasinsFF[localSubbasinIds[k]][j]);
                            if ((res[ffStrt + c][0]) > val) //min
                            {
                                res[ffStrt + c][0] = val;
                            }
                            if ((res[ffStrt + c][1]) < val) //max
                            {
                                res[ffStrt + c][1] = val;
                            }
                            c++;
                        }
                    }

                }
                
                //for BMP Assignment
                if (this.usingBMPAssign) {
                    double[] assign = in.assignments;

                    //for all the subbasins
                    for (int k = 0; k < this.localSubbasinIds.length; k++) {
                        ffStrt = ffStrt + c;
                        c = 0;

                        for (int j = 0; j < this.NoOfBMPChosens; j++) {
                            //use the j+1, skip the area
                            //if (chosenBMP[j] == 1) {
                                double val = Math.abs(assign[j * 108 + localSubbasinIds[k]]);
                            if ((res[ffStrt + c][0]) > val) //min
                            {
                                res[ffStrt + c][0] = val;
                            }
                            if ((res[ffStrt + c][1]) < val) //max
                            {
                                res[ffStrt + c][1] = val;
                            }
                            c++;
                            //}
                        }

                    }
                }
            }
            }
        }

        for (int i = 0; i < res.length; i++) {
            res[i][1] = res[i][1] - res[i][0]; //save the range
            if (res[i][1] == 0) {
                res[i][1] = 1; //divide by 1 is same number otherwise give NaN
            }
        }

        return res;
    }

    private double[][] normalizeData(LinkedList<Individual> Indv, double[][] scalParam) {
        double[][] res = new double[Indv.size()][input + 1]; //1 for output

        int ffStrt = 0; //begin of input FF
        int c = 0; //position of ff
        for (int i = 0; i < Indv.size(); i++) {
            Individual in = Indv.get(i);
            ffStrt = 0;
            //for entire watershed            
            c = 0;//ff begin from 0
            for (int j = 0; j < chosenFF.length - 1; j++) {
                //use the j+1, skip the area
                if (chosenFF[j] == 1) {
                    double val = Math.abs(in.fitnessValues[j]);
                    int pos = ffStrt + c;
                    double scal = ((val - scalParam[pos][0]) / scalParam[pos][1]);
                    if (scal > 1) {
                        res[i][pos] = 1;
                    } else if (scal < 0) {
                        res[i][pos] = 0;
                    } else {
                        res[i][pos] = scal;
                    }
                    c++;
                }
            }

            if (this.localPref) {
                if (localSubbasinIds != null) {
                    //for all the subbasins
                    for (int k = 0; k < this.localSubbasinIds.length; k++) {
                        ffStrt = ffStrt + c;
                        c = 0;

                        for (int j = 0; j < chosenFF.length - 1; j++) {
                            //use the j+1, skip the area
                            if (chosenFF[j] == 1) {
                                double val = Math.abs(in.subbasinsFF[localSubbasinIds[k]][j]);
                                int pos = ffStrt + c;
                                double scal = ((val - scalParam[pos][0]) / scalParam[pos][1]);
                                if (scal > 1) {
                                    res[i][pos] = 1;
                                } else if (scal < 0) {
                                    res[i][pos] = 0;
                                } else {
                                    res[i][pos] = scal;
                                }
                                c++;
                            }
                        }
                    }

                    //for BMP Assignment
                    if (this.usingBMPAssign) {
                        double[] assign = in.assignments;

                        //for all the subbasins
                        for (int k = 0; k < this.localSubbasinIds.length; k++) {
                            ffStrt = ffStrt + c;
                            c = 0;

                            for (int j = 0; j < this.NoOfBMPChosens; j++) {
                                //use the j+1, skip the area
                                //if (chosenBMP[j] == 1) {
                                double val = Math.abs(assign[j * 108 + localSubbasinIds[k]]);
                                int pos = ffStrt + c;
                                double scal = ((val - scalParam[pos][0]) / scalParam[pos][1]);
                                if (scal > 1) {
                                    res[i][pos] = 1;
                                } else if (scal < 0) {
                                    res[i][pos] = 0;
                                } else {
                                    res[i][pos] = scal;
                                }
                                c++;
                                //}
                            }

                        }
                    }
                }
            }

            res[i][ffStrt + c] = Math.abs(in.rating);
        }

        return res;
    }

    private double[][] getDataTrain(double[][] NormalizedData, int traningIndv) {

        double[][] res = null;

        res = new double[traningIndv][input + 1];
        for (int i = 0; i < traningIndv; i++) {
            for (int j = 0; j < input; j++) {
                double val = NormalizedData[i][j];
                //trim the unnecessary values
                if(val>1)
                    val=1;
                else if(val<0)
                    val = 0;
                res[i][j] = val;
            }
            //save the Rank
            res[i][input] = NormalizedData[i][input];
        }

        return res;
    }

    private Object trainCurrentANFIS(double[][] trainData) {
        Object res = null;
        VTimer time1 = new VTimer();
        time1.startTimer();
        res = mat.createANFIS(trainData, anfis_Mfs, anfis_epoc);
        time1.endTimer();
        time1.printTime("h");
        return res;
    }

    private Object[] trainCurrentLinearModels(double[][] DataTrain,double[][] DataTest) {

        Object[] res = null;

        /*
        double[][] trainData = new double[Data.length][this.input]; //NxM
        double[][] trainRating = new double[Data.length][1]; //Nx1
        for (int i = 0; i < Data.length; i++) {
            int j = 0;
            for (; j < input; j++) {

                trainData[i][j] = Data[i][j];
            }
            trainRating[i][0] = Data[i][j];
        }
        * 
        */

        VTimer time1 = new VTimer();
        time1.startTimer();
        res = mat.createDiscriminantClassifier(DataTrain, DataTest);
        time1.endTimer();
        time1.printTime("h");
        
        int[] errors = (int[]) res[0];
        System.out.println("Linear Model Erros are ");
        for (int i = 0; i < errors.length; i++) {
            System.out.print(errors[i] + "\t");
        }
        
        System.out.println();
        return res;
    }

    private double getNNRank(double[] ress) {
        int rank = 0;
        double max = 0;
        //System.out.println("\nNN Values ");
        for (int i = 0; i < ress.length; i++) //max is taken
        {
            //System.out.print("\t" + ress[i]);
            if (ress[i] > max) {
                max = ress[i];
                rank = i + 1;
            }
        }

        return rank;
    }

    private int getNoOfBMPs() {
        int res = 0;
        for (int i = 0; i < this.chosenBMP.length; i++) {
            res = res + chosenBMP[i];
        }
        return res;
    }

    

    private Object[] createLinearRandomData(double[][] Data) {
        Object[] res = null;
        
        
        double[][] trainData = new double[Data.length][this.input]; //NxM
        double[][] trainRating = new double[Data.length][1]; //Nx1
        for (int i = 0; i < Data.length; i++) {
            int j = 0;
            for (; j < input; j++) {

                trainData[i][j] = Data[i][j];
            }
            trainRating[i][0] = Data[i][j];
        }

        res = mat.createLinearRandomData(trainData, trainRating, this.currentLinearId, randomDataPer);
        return res;
    }

    void relearnNN() {
        this.bestNNM = new NNManager(input, output, chosenFF);
        
        LinkedList<NNManager> listNN = new LinkedList();
        LinkedList<Double> minPerError = new LinkedList();
        for(int j=0;j<3;j++)
        {
            double[][] NormalizedData;
            if(j==2)
            {
                double[][] cumData = new double[((double[][])NormalizedModeldata.get(0)).length*j][input+1];
                
                int count=0;
                for(int k=0;k<NormalizedModeldata.size();k++)
                {
                    double[][] dat = (double[][]) NormalizedModeldata.get(k);
                    for(int l=0;l<dat.length;l++)
                    {
                        cumData[count++] = dat[l];
                    }
                }
                NormalizedData = cumData;
            }
            else
            {
                NormalizedData = (double[][]) NormalizedModeldata.get(j);
            }
        double[][] data = getDataTrain(NormalizedData, NormalizedData.length);
        Object[] ob = this.mat.createTrainTestData(data);
        double[][] dataTrain = (double[][]) ob[0];
        double[][] dataTest = (double[][]) ob[1];
        
        bestNNM.learnNormalize(dataTrain);
        listNN.add(bestNNM);
        int error=0;
        for(int i=0;i<dataTest.length;i++)
        {
            double[] inp = new double[this.input];
                for (int k = 0; k < input; k++) {
                    inp[k] = dataTest[i][k];
                }
                double[] nnVal = bestNNM.testNNNormalize(inp);
                double rank = getNNRank(nnVal);
                if(rank!=dataTest[i][input])
                    error++;
                System.out.println("HR "+dataTest[i][input]+" : NNR "+rank);
        }
         double errorPer = error*100/dataTest.length;
         minPerError.add(errorPer);
         if(NormalizedModeldata.size()==1)
             break;
        }
        
        //find the best
        if(NormalizedModeldata.size()>1)
        {
        double min= minPerError.get(0);
        int pos = 0;
        for(int i=1;i<minPerError.size();i++)
        {
            double minNew = minPerError.get(i);
            if(minNew<=min)
            {
                pos = i;
                min = minNew;
            }
        }
               
        this.bestNNM = listNN.get(pos); 
        System.out.println(this.UserId + " Model Selected is "+pos);
        }
        
        //additional for user specific data
        /*
        if(this.UserId==78 || UserId==80)
        {
            this.bestNNM = listNN.get(0);
            System.out.println(this.UserId + " Model Selected is "+0);
        }
        * 
        */
        
    }
}
