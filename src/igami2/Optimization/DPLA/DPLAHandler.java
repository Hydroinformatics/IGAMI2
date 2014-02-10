/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.Optimization.DPLA;

/**
 *
 * @author VIDYA
 */
import java.io.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DPLAHandler {

    int noOfSubbasins = 130;
    // int noOfSubbasinsInRegion = 10;
    int[][] regionSubbasinId; //given id - 1;
    int noOfAutomata;
    PursuitAutomata[] pursuitAutomata;
    
    double[] maxFlowOutput = new double[noOfSubbasins];
    double[] maxFlowBaseline = new double[noOfSubbasins];
    double[] percentageReductionFlow = new double[noOfSubbasins];
    double[] volumeReductionFlow = new double[noOfSubbasins];
    Random r = new Random(System.currentTimeMillis());
    ExecRunner er;

    int noOfRegions;
    
    public DPLAHandler() {
        
    }
    
    public DPLAHandler(int noOfSubbasins,int noOfRegions,int[][] regionSubbasinId)
    {
        this.noOfSubbasins =  noOfSubbasins;       
        this.regionSubbasinId = regionSubbasinId;
        this.noOfRegions = noOfRegions;
        
    }

    
    boolean allConvergedPursuit(double threshold) {
        boolean result = false;
        int count = 0;
        for (int i = 0; i < pursuitAutomata.length; i++) {
            if (pursuitAutomata[i].hasConverged(threshold)) {
                count++;
            }
        }

        if (count == pursuitAutomata.length) {
            result = true;
        }
        return result;
    }

    
    
    double random() {
        double result;
        result = r.nextDouble();
        return result;
    }

    int commonPayoff(double payoff) {
        int response = 0;
        double d;
        double rand = random();
        d = payoff;
        if (rand <= d) {
            response = 1;//reward

        } else {
            response = 0;//penalty

        }
        return response;
    }

    public <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {

            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {

                return -1 * (o1.getValue()).compareTo(o2.getValue()); // -1 for descending, +1 for ascending

            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    
    public void start() {
        int i, sampledPayoff;
        int[] selectedAction;
        
        
        DataOutputStream dosFlow;
        
        
        double payoff = 0, sumArea = 0, sumFlow, area, flow, areaPayoff = 0, flowPayoff = 0, areaWeight = 0.5, flowWeight = 0.5, step = 0.1, startWeight = 0.1;

        noOfAutomata = noOfSubbasins;

        //the optimization procedure when dealing with a region
        //install all the subbasins ... get resultant output.rch file and call that as the baseline.rch file
        //also install all subbasins to get maximum reduction in the flow

        regionSubbasinId[0] = new int[]{51, 54, 59, 52, 53, 58, 60, 56, 62, 64};


       // sumArea = addAreaRegion();
        //sumFlow = -133248.99430840297;
        //sumFlow = -200000;
        sumFlow = 1327;
        flowWeight = startWeight;
        
        
        while (flowWeight < 0.9) {
            try {
                areaWeight = 1 - flowWeight;
                
                //
                pursuitAutomata = new PursuitAutomata[regionSubbasinId.length];
                selectedAction = new int[regionSubbasinId.length];
                for (i = 0; i < regionSubbasinId.length; i++) {
                    pursuitAutomata[i] = new PursuitAutomata(i, 2, 0.05);
                }
                String st = "../SWAT/Region7/" + "Flow" + new Double(flowWeight).toString().substring(0, 3) + "_Area" + new Double(areaWeight).toString().substring(0, 3) + ".txt";

                //File nfile = new File(st);
                //boolean success = nfile.createNewFile();
                
                dosFlow = new DataOutputStream(new FileOutputStream(st));

                while (!allConvergedPursuit(0.95)) {
                    area = 0;
                    flow = 0;
                    for (i = 0; i < regionSubbasinId.length; i++) {
                        selectedAction[i] = pursuitAutomata[i].selectAction();//Assignments

                        //Individual's Evaluation Function is called....

                    
                    System.out.println(flow);

                    String temp1 = "";
                    for (i = 0; i < regionSubbasinId.length; i++) {
                        temp1 = temp1 + new Integer(selectedAction[i]).toString();
                    }
                    temp1 = temp1 + ",";
                    areaPayoff = area / sumArea;
                    areaPayoff = 1 - areaPayoff;
                    flowPayoff = flow / sumFlow;
                    flowPayoff = 1 - flowPayoff;


                    //read output produced by the SWAT code and perform reinforcement learning


                    payoff = areaWeight * areaPayoff + flowWeight * flowPayoff;
                    //payoff = 1 - payoff;
                    //System.out.println(payoff);
                    //do learning based on this payoff
                    sampledPayoff = commonPayoff(payoff);

                    System.out.println(temp1 + flow + " " + area + " " + flowPayoff + " " + areaPayoff);
                    dosFlow.writeBytes(temp1 + new Double(flow).toString() + "," + new Double(area).toString() + "," + new Double(flowPayoff).toString() + "," + new Double(areaPayoff).toString() + "," + new Double(payoff).toString());
                    dosFlow.writeBytes("\n");

                    for (i = 0; i < regionSubbasinId.length; i++) {
                        pursuitAutomata[i].doLearning(sampledPayoff, false);
                    }

                    //setAllZerosInPndFiles();
                }//while end

                for (i = 0; i < regionSubbasinId.length; i++) {
                    System.out.println("Subbasin " + (i + 1) + " should be " + pursuitAutomata[i].convergedTo());
                }

                dosFlow.close();
                flowWeight = flowWeight + step;
            }
            } catch (Exception ex) {
                Logger.getLogger(DPLAHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
