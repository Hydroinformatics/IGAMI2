/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.MixedInitiative.neuralnetneuroph;

import java.io.*;
import java.util.LinkedList;

/**
 *
 * @author VIDYA
 */
public class NeuralNetProject {

    private static FileReader fin;
    static LinkedList lst;
    private static BufferedReader br;
    private static int N, M;
    private static int numOfOut;
    private static String line;
    private static int i;
    private static String[] name;
    private static LinkedList lst1;
    private static LinkedList InputList;
    private static LinkedList OutputList;
    private static int NN_input = 4;
    private static int NN_output = 3;
    private static LinkedList<Integer> rankList;
   

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            lst = new LinkedList();

            fin = new FileReader("data.txt");
            br = new BufferedReader(fin);

            
            while ((line = br.readLine()) != null) {
                if (line.compareTo("out") != 0) {
                    String[] resu = line.split("\t");
                    int len = resu.length;
                    double data[][] = new double[1][len];
                    
                    for (int j = 0; j <len ; j++) {
                        data[0][j] = Double.parseDouble(resu[j]);
                    }
                    lst.add(data);
                }
            }
        } catch (Exception e) {
        }

        //NeuralNet nn = new NeuralNet(2,3);
        
        NeuralNet nn = new NeuralNet(NN_input,4,NN_output);

        nn.createTraingSet();
        
        InputList = new LinkedList();
        OutputList = new LinkedList();

        for (int i = 0; i < lst.size(); i++) {
            double data[][] = (double[][]) lst.get(i);
            double dataIn[] = new double[NN_input];
            double dataOut[] = new double[NN_output];
            System.out.println("\nNN Learning Values ");
            for (int j = 0; j < NN_input; j++) {
                //use the j+1, skip the area
                dataIn[j] = data[0][j];
                System.out.print("\t"+dataIn[j]);
            }
            for (int j = 0; j < NN_output; j++) {
                if (data[0][NN_input+j] == 1) {
                    dataOut[j] = 1;//true
                } else {
                    dataOut[j] = 0;//false
                }
                System.out.print("\t"+dataOut[j]);
            }
            
            //data[0][NN_input] = inn.rating;
            InputList.add(dataIn);
            OutputList.add(dataOut);
        }
        nn.learnNN(InputList,OutputList,false);
        
        
        
        
        //one by one
        /*
        for(int i=0;i<lst.size();i++)
        {
            lst1 = new LinkedList();
            lst1.add(lst.get(i));
            nn.learnNN(lst1);
        }
         * 
         */
         
        //test
        try {
            lst = new LinkedList();
            rankList = new LinkedList();

            fin = new FileReader("test.txt");
            br = new BufferedReader(fin);

            
            while ((line = br.readLine()) != null) {
                if (line.compareTo("out") != 0) {
                    String[] resu = line.split("\t");
                    int len = resu.length;
                    double data[][] = new double[1][len-1];
                    
                    for (int j = 0; j <NN_input ; j++) {
                        data[0][j] = Double.parseDouble(resu[j]);
                    }
                    rankList.add(Integer.parseInt(resu[resu.length-1]));
                    lst.add(data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        for (int j = 0; j < lst.size(); j++) {
            double[][] invect = (double[][]) lst.get(j);
            double[] res = nn.calculateOutput(invect[0]);
            double max = 0;
        System.out.print("\nInput Values ");
        int rank=0;
        for(int i=0;i<invect[0].length;i++)
        {
            System.out.print("\t"+invect[0][i]);
        }
        System.out.print("\tNN Values ");
        for (int i = 0; i < res.length; i++) //max is taken
        {
            System.out.print("\t"+res[i]);
            if (res[i] > max) 
            {
                max = res[i];
                rank = i + 1;
            }
        }
        System.out.print("\t ActualRank " + rankList.get(j));
        System.out.print("\t NNRank " + rank);
            /*
            System.out.print("\nRanks for F0: " + invect[0][0] + "\tF1: " + invect[0][1]);
            for (int i = 0; i < res.length; i++) {
                System.out.print("\t " + res[i]);
            }
             * 
             */
        }
    }
}
