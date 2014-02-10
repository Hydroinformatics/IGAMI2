/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.MasterComputer;

import igami2.DistributedSystem.ClusterNodes.CleanExit;
import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import igami2.DistributedSystem.DistributedSystem;
import java.io.*;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author VIDYA
 */
public class UserInterfaceAgent extends UnicastRemoteObject implements DistributedSystem {

    public static int port = 11555;
    public static String masterHost = "rmi://in-geol-esaig04.geology.iupui.edu:" + port + "/MasterComputer";
    private static int beginId = 3;
    private static int maxFunctions = 1; //runs these many functions from this host
    private static int endId = 10;
    private static int strId = 1; //ids to be used
    /*
     * private double Area_MAX = 800;//603.96; private double Flow_MAX = 56.05;
     * private double Cost_MAX = 41916048; private double Soil_MAX = 401109.7;
     * private double Nitrate_MAX = 7281552; private double Pesticide_MAX = 100;
     *
     */
    private double Area_MAX = 800;//603.96;
    private double Flow_MAX = 56.05;
    private double Cost_MAX = 41916048;
    private double Soil_MAX = 401109.7;
    private double Nitrate_MAX = 7281552;
    private double Pesticide_MAX = 100;
    private double Area_Min = 0;//603.96;
    private double Flow_min = 0;//3.3;
    private double Cost_min = 175134.2;
    private double Soil_min = 357243.7;
    private double Nitrate_min = 6622098;
    private double Pesticide_min = 100;
    private double WtCost = 1.5;//give the cost a weight of 3
    private int FUN0 = 0; //weighted linear func(a+b+c+d)/4
    private int FUN1 = 1; //(a^2+b^2+c^2+d^2)/4+200
    private int FUN2 = 2;//(4a+3b+c+d/2)/8
    private int FUN3 = 3;//(ad/bc)/2
    private int FUN4 = 4;//(ac/bd)/2
    private int FUN5 = 5;//(ab/cd)/2
    
    //private int fun; //which function is used
    private String name;
    private int AgentId;
    
    static int[] ids = null; //stores the userId of various simulated users
    private int fun0 = 6;
    private int fun1 = 7;
    private int fun2 = 8;
    private int fun3 = 9;
    private int fun4 = 10;//random rating
    private int fun5 = 11;//random rating
    //private int fun4 = 10;//random rating
    private Random randRate = null;

    
    public UserInterfaceAgent(String name, int fun) throws RemoteException {

        super();
        this.name = name;

    }

    public static void main(String args[]) {
        int fun = 0;

        Registry registry;
        try {
            registry = LocateRegistry.createRegistry(port);
            //
        } catch (RemoteException e) {
            try {
                //System.out.println("Reg Not running ");                       
                registry = LocateRegistry.getRegistry(port);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        //take input of fitness function selection
        br = new BufferedReader(new InputStreamReader(System.in));
        try {
            //System.out.println("Please Enter the function you want to execute");
            //fun = Integer.parseInt(br.readLine());

            String name = InetAddress.getLocalHost().getHostName() + ".geology.iupui.edu";
            String rmi = "rmi://" + name + ":" + port + "/UserInterfaceAgent";
            //MO mO = new MO(Name,T,N);
            UserInterfaceAgent agent = new UserInterfaceAgent(rmi, fun);
            //String str = name+":"+port+"/Agent";
            String str = "//localhost:" + port + "/UserInterfaceAgent";

            System.out.println("Agent Binding to the name " + str);
            Naming.rebind(str, agent);
            System.out.println("Interface Agent Ready and Running at " + str);

            //SyncHost mh = new SyncHost("SYNCSWAT");
            //mh.startHost();
            //System.out.println("Waiting for job");


            int[] chosenFF = new int[]{0, 1, 1, 1, 1, 0, 1};


            //System.out.println("Welcome " + UserName + " to IGAMI2");

            /*
             * System.out.println("Please Enter Your Name"); UserName =
             * br.readLine(); System.out.println("Welcome "+UserName+ " to
             * IGAMI2");
             *
             *
             * System.out.println("Please Enter 1 (yes) or 0 (no) for Fitness
             * Function Selection");
             *
             * System.out.println("Do you want SWAT_BMPs_AreaFitnessFunction ");
             * chosenFF[0] = Integer.parseInt(br.readLine());
             * System.out.println("Do you want SWAT_BMPs_PeakflowFitnessFunction
             * "); chosenFF[1] = Integer.parseInt(br.readLine());
             * System.out.println("Do you want
             * SWAT_BMPs_EconomicCostsFitnessFunction "); chosenFF[2] =
             * Integer.parseInt(br.readLine()); System.out.println("Do you want
             * SWAT_BMPs_SoilErosionFitnessFunction "); chosenFF[3] =
             * Integer.parseInt(br.readLine()); System.out.println("Do you want
             * SWAT_BMPs_NitratesFitnessFunction "); chosenFF[4] =
             * Integer.parseInt(br.readLine());
             *
             */

            DistributedSystem ob = (DistributedSystem) Naming.lookup(masterHost);
             ids = new int[maxFunctions]; //for various Different functions
            for(int i=0;i<maxFunctions;i++)
            {
                int iids = 999;
                int cId = strId+i;//Id being requested by this user
                String rmi1 = rmi+","+cId;
                if (( iids = ob.addUser(rmi1, chosenFF)) > 0) {
                    if(iids==cId)//sucess
                    {
                        ids[i] = iids;
                        System.out.println("Simulated User Function "+(i+beginId)+ " is running with USERID" + ids[i]);
                    }
                    else
                    {
                        System.out.println("User Id Busy");
                    }
                //System.out.println("Introspection waiting time is 12 mins,\n Please Wait!.............");
                }
            }

        } catch (Exception e) {
            System.out.println("Exception orrcured in Agent: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean initializeAgent(int id) throws RemoteException {
        System.out.println("Agent Initiated " + id);
        this.AgentId = id;
        return true;
    }

    @Override
    public boolean openFileWriter(String name) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean closeFile(String name) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean putData(String name, byte[] buff) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean synDirs() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean distributejob(LinkedList<Individual> a) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void exitSystem() throws RemoteException {
        //create a thread to cleanup and exit
        CleanExit cleanExit = new CleanExit();
    }

    @Override
    public int addUser(String host, int[] chosenFF) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public synchronized LinkedList<Individual> takeFeedback(LinkedList<Individual> IndvPopulation) throws RemoteException {

        int id = IndvPopulation.get(0).UserId;
        int fun = getFunction(id);
        System.out.println("\nFeedback is given by Simulated User "+id);

        randRate = new Random();
        Random rn = new Random();
        for (int i = 0; i < IndvPopulation.size(); i++) {
            //IndvPopulation.get(i).printAssignment();
            //IndvPopulation.get(i).printFitnessValues();

            //computer

            int rating = 0;
            //double v = (-1)*IndvPopulation.get(i).getFitnessValue(2);

            Individual in = IndvPopulation.get(i);
            double v = getWeightedAVG(in,fun);

            rating = findRate(v,fun);

            //System.out.println("Weighted Average is " + v + " Rating is " + rating);
            System.out.println(v + "\t" + rating);

            double conf = Math.abs(rn.nextGaussian() * 100);
            if (conf > 100) {
                conf = conf % 100;
            }
            //IndvPopulation.get(i).confidence = conf;

            //human
            //int rating = getRating();
            //int conf = getConfidence();


            IndvPopulation.get(i).confidence = conf;
            IndvPopulation.get(i).rating = rating;


            //System.out.println("\n Rating is " + rating + " Confidence is " + conf);

        }

        //System.out.println("\nIntrospection Completeed for User "+id);
        //System.out.println("Next Introspection waiting time is 12 mins,\n Please Wait!.............");
        return IndvPopulation;
    }

    @Override
    public boolean JobAsyncResult(LinkedList<Individual> indv, int agentid) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LinkedList<Individual> JobSync(LinkedList<Individual> indv) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean showResult(LinkedList<Individual> indv, double[] sdmR, double[] data) throws RemoteException {
        boolean res = false; //keep training

        System.out.println("Average Human Rank " + data[0]);
        System.out.println("Average SDM Rank " + data[1]);
        System.out.println("Average errorPercent " + data[2]);

        for (int i = 0; i < indv.size(); i++) {
            Individual in = indv.get(i);
            in.printFitnessValues();
            System.out.print("\t Human Rating " + in.rating);
            System.out.print("\t SDM Rating " + sdmR[i]);
            System.out.println();
        }

        System.out.println("Do you want to continue SDM \nEnter yes to start SDM or no to give more traning  ");
        /*
         * try { String resp = br.readLine();
         * if(resp.compareToIgnoreCase("YES")==0) res = true; //start SDM }
         * catch (IOException ex) {
         * Logger.getLogger(UserInterfaceAgent.class.getName()).log(Level.SEVERE,
         * null, ex); }
         *
         */

        return res;
    }

    @Override
    public synchronized LinkedList<Individual> doIntrospection(LinkedList<Individual> Indv) throws RemoteException {
                
        int id = Indv.get(0).UserId;
        int fun = getFunction(id);
        System.out.println("\nIntrospection is done by Simulated User "+id);

        Random rn = new Random();
        for (int i = 0; i < Indv.size(); i++) {
            //Indv.get(i).printAssignment();
            //Indv.get(i).printFitnessValues();

            //System.out.println("Previous Rating is " + Indv.get(i).rating);

            int rating = 0;
            //double v = Indv.get(i).fitnessValues[0];
            Individual in = Indv.get(i);
            double v = getWeightedAVG(in,fun);
            rating = findRate(v,fun);

            //int rating = getRating();

            Indv.get(i).rating = rating;

            //System.out.println("Previous Confidence is " + Indv.get(i).confidence);

            double conf = Math.abs(rn.nextGaussian() * 100);
            //IndvPopulation.get(i).confidence = getConfidence();
            if (conf > 100) {
                conf = conf % 100;
            }

            Indv.get(i).confidence = conf;

            //System.out.println("\n Rating is " + rating + " Confidence is " + conf);
        }

        return Indv;
    }
    
    /*
     * Use to Scale the FF vales using the Base Values stated above
     */

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
    
    /*
     * get weighted average based on different fitness function values
     */

    private double getWeightedAVG(Individual in, int fun) {
        double res = 0;
        double sum = 0;

        //simple linear functio
        if (fun == FUN0) {
            for (int i = 0; i < 4; i++) {
                sum = sum + scaleFitnessValue(i + 1, in.fitnessValues[i + 1]);
            }
            res = sum/4; //take the weighted average, multiply with 1000 to make it a Integer rather than fraction, ease of use
        } else if (fun == FUN1) {

            for (int i = 0; i < 4; i++) {
                double val = scaleFitnessValue(i + 1, in.fitnessValues[i + 1]);
                sum = sum + Math.pow(val, 2);
            }
            res = sum/4 + 0.200; //add 200 to scale to rating place
        } else if (fun == FUN2) {
            double[] val = new double[4];
            for (int i = 0; i < 4; i++) {
                val[i] = scaleFitnessValue(i + 1, in.fitnessValues[i + 1]);
            }
            sum = 4 * val[0] + 3 * val[1] + val[2] + val[3] / 2;
            res = sum/8;

        } else if (fun == FUN3) {
            double[] val = new double[4];
            for (int i = 0; i < 4; i++) {
                val[i] = scaleFitnessValue(i + 1, in.fitnessValues[i + 1]);
            }
            sum = ((val[0] * val[3]) / (val[1] * val[2]));
            res = sum/2;

        } else if (fun == FUN4) {
            double[] val = new double[4];
            for (int i = 0; i < 4; i++) {
                val[i] = scaleFitnessValue(i + 1, in.fitnessValues[i + 1]);
            }
            sum = ((val[0] * val[2]) / (val[1] * val[3]));
            res = sum/2;
        } else if (fun == FUN5) {
            double[] val = new double[4];
            for (int i = 0; i < 4; i++) {
                val[i] = scaleFitnessValue(i + 1, in.fitnessValues[i + 1]);
            }
            sum = ((val[0] * val[1]) / (val[2] * val[3]));
            res = sum/2;
        }

        
        //Individual FFs
        if (fun == fun0) {
            double val = scaleFitnessValue(0 + 1, in.fitnessValues[0 + 1]);
            res = val;
        } else if (fun == fun1) {

            double val = scaleFitnessValue(1 + 1, in.fitnessValues[1 + 1]);
            res = val;
        } else if (fun == fun2) {
            double val = scaleFitnessValue(2 + 1, in.fitnessValues[2 + 1]);
            res = val;

        } else if (fun == fun3) {
           double val = scaleFitnessValue(3 + 1, in.fitnessValues[3 + 1]);
            res = val;
        }
        res = res * 1000;
        return res;
    }

    private int findRate(double v, int fun) {
        int rating = 0;

        if (fun == FUN0) {
            double min1 = 860;
            double max1 = 905;
                        
            if (v <= min1) {
                rating = 1;
            } else if (v > min1 && v <= max1) {
                rating = 2;
            } else { //>725, //better
                rating = 3;
            }
        } else if (fun == FUN1) {            
            double min1 = 945;
            double max1 = 1020;
            
            if (v <= min1) {
                rating = 1; 
            } else if (v > min1 && v <= max1) {
                rating = 2;
            } else { //>730, 
                rating = 3; //better
            }
        } else if (fun == FUN2) {
            double min1 = 990;
            double max1 = 1050;
            
            
            if (v <= min1) {
                rating = 1;
            } else if (v > min1 && v <= max1) {
                rating = 2;
            } else { //>725, cost very high
                rating = 3;
            }
        } else if (fun == FUN3) {            
            double min1 = 365;
            double max1 = 465;
            
            if (v <= min1) {
                rating = 3;
            } else if (v > min1 && v <= max1) {
                rating = 2;
            } else { //670, cost very high
                rating = 1;
            }
        } else if (fun == FUN4) {
            double min1 = 378;
            double max1 = 465;
            
            
            if (v <= min1) {
                rating = 3;
            } else if (v > min1 && v <= max1) {
                rating = 2;
            } else { //>725, cost very high
                rating = 1;
            }
            
            
            
        } else if (fun == FUN5) {
            double min1 = 700;
            double max1 = 880;
            
            if (v <= min1) {
                rating = 1;
            } else if (v > min1 && v <= max1) {
                rating = 2;
            } else { //>725, cost very high
                rating = 3;
            }
        }
        
        if (fun == fun0) {
            //double min1 = 700;
            //double max1 = 725;
            double min1 = 850;
            double max1 = 920;
            
            if (v <= min1) {
                rating = 1;
            } else if (v > min1 && v <= max1) {
                rating = 2;
            } else { //>max1 higher the better, more reduction
                rating = 3;
            }
        } else if (fun == fun1) {
            //double min1 = 690;
            //double max1 = 725;
            double min1 = 900;
            double max1 = 1100;
            
            if (v <= min1) {
                rating = 3;
            } else if (v > min1 && v <= max1) {
                rating = 2;
            } else { //>725, lesser the better
                rating = 1;
            }
        } else if (fun == fun2) {
            //double min1 = 700;
            //double max1 = 740;
            double min1 = 750;
            double max1 = 800;
            
            if (v <= min1) {
                rating = 1;
            } else if (v > min1 && v <= max1) {
                rating = 2;
            } else { //
                rating = 3; //higher better
            }
        } else if (fun == fun3) {
            //double min1 = 450;
            //double max1 = 520;
            double min1 = 755;
            double max1 = 790;
            
            if (v <= min1) {
                rating = 1;
            } else if (v > min1 && v <= max1) {
                rating = 2;
            } else { //>725, higher better
                rating = 3;
            }
        }
        
        else if (fun == fun4) {
            //random rating
            rating = Math.abs(randRate.nextInt()%3)+1;
        }
        
        else if (fun == fun5) {
            //random rating
            rating = Math.abs(randRate.nextInt()%3)+1;
        }

        return rating;
    }

    /*
     * return the function accociated with this userId
     */
    private int getFunction(int id) {
        int res=-1;
        
        for(int i=0;i<ids.length;i++)
        {
            if(ids[i]==id)
                res=(i+beginId);
        }
        
        return res;
        
    }

    @Override
    public boolean asyncMsg(int id,String type, boolean val) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
