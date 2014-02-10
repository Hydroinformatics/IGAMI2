/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.MasterComputer;

import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import igami2.DistributedSystem.DistributedSystem;
import igami2.DistributedSystem.fileSync.FileHandler;
import igami2.IGAMI2Main;
import igami2.UserData;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author VIDYA
 */
public class HPCController extends UnicastRemoteObject implements DistributedSystem {

    private static int port = 11555;
    private static Registry registry;
    private static String MainFrameHost;
    private static String cluster0 = "rmi://in-geol-esaig01.geology.iupui.edu:" + port + "/MainFrameController"; //this will be default cluster
    private static String cluster1 = "rmi://hydroinf.engr.oregonstate.edu:" + port + "/MainFrameController";
    private static String cluster2 = "rmi://tempest.ads.iu.edu:" + port + "/MainFrameController";
    private static String cluster3 = "rmi://in-geol-esaig05.ads.iu.edu:" + port + "/MainFrameController"; //this is used to manage cloud computers
    private static String supercomputer = "bigred2.uits.iu.edu:";
    private static int userMax = 150; //Need optimiation to add infinite users, create linked list instead of array or another data structure
    public static LinkedList<LinkedList<Individual>> ArrayIndvList;
    private static LinkedList<Individual> distributedJobResult;
    private static int distributedJobClusterCount=0;
    private static LinkedList SingleNodeWaitingQueue; //use to schedule the job to different clusters
    private static boolean usePCount = false; //Use P Count to distribute jobs otherwise use No of Active Users
    public static boolean useForInitialCase = false; //check if the class is used for intial case evalation
    String name;
    private FileHandler handler;
    private static String swatEvalautionFile = "../SWAT/swat_dirs/swat.zip"; //use for normal user evaluation
    private static String swatResearchFile = "../SWAT/RESEARCH/swat_dirs/swat.zip"; //use for reserach type of evaluations
    private static String swatFile = "../SWAT/swat_dirs/swat.zip"; //determined based on file type being received
    private static int[] chosenBMPs = new int[]{1, 1, 1, 1, 1, 1, 0, 1, 1, 0};
    private static ClusterData[] clusters;
    private static int NodeMax = 20;// use to control SDM Max .
 
    
    public HPCController(String name) throws RemoteException {
        //super();
        SingleNodeWaitingQueue = new LinkedList();
        clusters = new ClusterData[4]; //We can add more clusters if needed
        clusters[0] = new ClusterData("ESAIG Cluster", 0, cluster0, port, 5, 4, false,7);
        clusters[1] = new ClusterData("OSU Hydroinf Cluster", 1, cluster1, port, 7, 32, true,7);
        clusters[2] = new ClusterData("IU Tempest Cluster", 2, cluster2, port, 32, 20, false,32);
        clusters[3] = new ClusterData("AWS Cloud Cluster", 3, cluster3, port, 7, 32, false,2);
        this.name = name;
        ArrayIndvList = new LinkedList();      
    }

    public static void changeClusterInfo(int clusterId, boolean status)
    {
        //toggle the cluster
        if (clusterId >= 0 && clusterId<clusters.length) {
            if (status == true) {
                if (!clusters[clusterId].getStatus()) {
                    boolean stat = false;
                    try {
                        DistributedSystem ob1;
                        ob1 = (DistributedSystem) Naming.lookup(clusters[clusterId].getHostName());

                        if (stat = ob1.initializeAgent(clusterId)) {
                            System.out.println("Cluster Host Running with Id " + clusterId + "\tName:" + clusters[clusterId].getName());
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(HPCController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (stat) {
                        clusters[clusterId].setStatus(true);
                        System.out.println("Cluster status Changed to True of " + clusters[clusterId].getName());
                        //try rescheduling any waiting users
                        if (SingleNodeWaitingQueue.size() > 0) { //always single node is allocated
                            int len = SingleNodeWaitingQueue.size();
                            for (int i = 0; i < len; i++) {
                                LinkedList<Individual> a = (LinkedList<Individual>) SingleNodeWaitingQueue.removeFirst();

                                sendEvaluation(a);
                                if (SingleNodeWaitingQueue.size() == len) //no change in waiting queue as not sufficient nodes
                                {
                                    break;
                                }
                            }
                        }
                    }

                }
            } else if (status == false) {
                if(clusters[clusterId].getStatus())
                {
                    clusters[clusterId].setStatus(false);
                    System.out.println("Cluster status Changed to False of "+clusters[clusterId].getName());
                }
            }
        }
    }

    public void initRMIMasterComputer(boolean useForInitialCase) {

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        this.useForInitialCase = useForInitialCase;
        if (!useForInitialCase) {
            userMax = IGAMI2Main.maxUsers;
        }

        try {
            registry = LocateRegistry.createRegistry(port);
            //
        } catch (RemoteException e) {
            try {

                registry = LocateRegistry.getRegistry(port);
            } catch (RemoteException ex) {
                System.out.println("Reg Not running ");
                ex.printStackTrace();
            }
        }

        try {

            String name = InetAddress.getLocalHost().getHostName();
            String str = "rmi://" + name + ":" + port + "/MasterComputer";
            System.out.println("MasterComputer Binding to the name " + str);


            str = "//localhost:" + port + "/MasterComputer";

            HPCController ob = new HPCController(name);

            Naming.rebind(str, ob);

            
                for (int i = 0; i < clusters.length; i++) {
                    ClusterData clust = clusters[i];
                    
                    try
                    {
                    if (clust.getStatus()) //config as active
                    {
                        
                        DistributedSystem ob1 = (DistributedSystem) Naming.lookup(clust.getHostName());
                        if (ob1.initializeAgent(i)) {
                            System.out.println("Cluster Host Running with Id "+ i+ "\tName:" + clust.getName());
                        }
                    }
                

            } catch (ConnectException ex) {
                //ex.printStackTrace();
               clust.setStatus(false); //not active 
               System.out.println("Host Not running "+ clust.getName()); 
            }
                }

            System.out.println("MasterComputer Ready and Running");

            System.out.println("Waiting for job");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static String getAvailHost(LinkedList<Individual> a) {
        String host = null;
        int size = a.size();
        //go in loop
        boolean sucess = false;
        //Scheduling, Round Robin based on Cluster Capacity
        if(size>NodeMax)
            size = NodeMax; //set Max Threshold for SDM
        int userId = a.get(0).UserId;
        
        //for two different tests
        /*
        if(userId==2)
        {
            //send it to OSU Cluster
           
           ClusterData currentCluster = clusters[1];
           if(currentCluster.getStatus())
           {
            host = currentCluster.getHostName();
            currentCluster.incrementUserCount();
            sucess = true; 
           }
           
        }
        else //else send to IU Cluster for Other experiment
        {
             //send it to OSU Cluster       
           ClusterData currentCluster = clusters[2];
           if(currentCluster.getStatus())
           {
               host = currentCluster.getHostName();
               currentCluster.incrementUserCount();
               sucess = true;   
           }
        }
        * 
        */
        
        for(int i=0;i<clusters.length;i++)
        {
            ClusterData currentCluster = clusters[i];
            if(currentCluster.getStatus())
            {
                if (currentCluster.checkActiveSlot()) {
                    host = currentCluster.getHostName();
                    currentCluster.incrementUserCount();
                    sucess = true;
                    break;
                }
            }
        }
        
        if(!sucess) //if Job is not able to get to any cluster, either send it to default cluster or put in waiting queue
        {
            SingleNodeWaitingQueue.addLast(a); //put into a waiting queue
            System.out.println("User Put into Queue is "+a.get(0).UserId);
        }
        else
        {
            System.out.println("Send the job of User "+a.get(0).UserId +" to Host "+host);
        }

        return host;
    }
    
    public static synchronized void sendEvaluationUsingQueue(LinkedList<Individual> a)
    {
        SingleNodeWaitingQueue.addLast(a); //put into a waiting queue
        scheduleAnyWaiting();
    }

    private static void sendEvaluation(LinkedList<Individual> a) {
        //reserve a node in a cluster
        int UserId = a.get(0).UserId;
        int strt = 0; //start point of list
        int left = a.size();

        if(usePCount) //use multiple clusters to solve large jobs
        {
            for (int i = 0; i < clusters.length; i++) {
                ClusterData currentCluster = clusters[i];

                if (currentCluster.getStatus()) {
                    
                    int avail = currentCluster.getP_Avail();
                    if (avail > 0) 
                    {
                        String host = currentCluster.getHostName();
                        LinkedList<Individual> job = new LinkedList();
                        int j = 0;
                        if (left >= avail) {
                            for (; j < avail; j++) {
                                job.add(a.get(strt + j));
                            }
                            currentCluster.decrementP_Avail(avail);
                            left = left - avail;//decrement the left
                        } else //left is small
                        {
                            for (; j < left; j++) {
                                job.add(a.get(strt + j));
                            }
                            currentCluster.decrementP_Avail(left);
                            left = left - left;//decrement the left
                            
                        }
                        if(UserId==1)
                            distributedJobClusterCount++; //one cluster is assigned to this user
                        MasterComputerRMIMonitorThread agentMonitorThread = new MasterComputerRMIMonitorThread(job, host, port);
                        strt = strt + j;//new strt point
                        if (left == 0) {
                            System.out.println("Sucessfully deployed the job to Cluster");
                            break;//break the loop
                        }
                    }
                    
                    //String host = currentCluster.getHostName();
                    //MasterComputerRMIMonitorThread agentMonitorThread = new MasterComputerRMIMonitorThread(a, host, port);
                      
                }
            }           
            if (left != 0) {
                if(UserId==1)
                    System.out.println("Error Occured in Sending the job to clusters");
                else
                {
                    //put back on queue
                    SingleNodeWaitingQueue.addFirst(a); //put into a waiting queue
                    System.out.println("User Put into Queue is "+a.get(0).UserId);
                }
            }
        
        }
        else
        {
            String host = getAvailHost(a);
            if(host!=null)
            {
                MasterComputerRMIMonitorThread agentMonitorThread = new MasterComputerRMIMonitorThread(a, host, port);
            }
        }   
    }

    @Override
    public synchronized boolean JobAsyncResult(LinkedList<Individual> indv, int ClusterId) throws RemoteException {
        boolean res = false;

        //make the Cluster Nodes Available
        int size = indv.size();
        
        int userId = indv.get(0).UserId;
        
        if (userId == 1 && usePCount) //System
        {
            distributedJobClusterCount--;
            if (distributedJobResult == null) //currently empty
            {
                distributedJobResult = new LinkedList();
                //save the result
                for (Individual in : indv) {
                    distributedJobResult.add(in);
                }
            } else if (distributedJobResult.size() > 0) {
                //check which one is reached earlier
                if (distributedJobResult.get(0).IndvId < indv.get(0).IndvId) {
                    for (Individual in : indv) {
                        distributedJobResult.add(in);
                    }
                } else {
                    for (Individual in : distributedJobResult) {
                        indv.add(in);
                    }
                    distributedJobResult = indv;
                }
            } else {
                System.out.println("Something went wrong");
            }
            
            if (distributedJobClusterCount == 0) {
                
                indv = distributedJobResult;
                int systemId = Integer.parseInt("" + IGAMI2Main.UserSystemId.get(userId)); //get the systemId of this user
                if (systemId > ArrayIndvList.size()) {
                    ArrayIndvList.add(indv);
                } else {
                    ArrayIndvList.set(systemId, indv);//replace the previous
                }
                distributedJobResult = null; //make it free for next session
            } else {
                System.out.println("Waiting for others to finish");
            }
            
        } else if (!useForInitialCase) {
            if (size > NodeMax) {
                size = NodeMax;
            }
            Integer inte = (Integer) IGAMI2Main.UserSystemId.get(userId);
            if (inte != null) {
                int systemId = Integer.parseInt("" + inte); //get the systemId of this user
                if (systemId > ArrayIndvList.size()) {
                    ArrayIndvList.add(indv);
                } else {
                    ArrayIndvList.set(systemId, indv);//replace the previous
                }
            } else {
                System.out.println("Something went wrong for UserId " + userId);
            }
            
        } else //use for intial case evaluator
        {
            if (userId > ArrayIndvList.size()) {
                ArrayIndvList.add(indv);
            } else {
                ArrayIndvList.set(userId, indv);//replace the previous
            }            //ArrayIndvList[userId] = indv;
        }
        
        
        //ClusterData currentCluster = clusters[ClusterId];
        if (usePCount) //use multiple clusters to solve large jobs
        {
            clusters[ClusterId].incrementP_Avail(size);
        } else {
            clusters[ClusterId].decrementUserCount();
        }
        //need optimziation as this call is blocking a remote thread;
        
        this.scheduleAnyWaiting();
        return res;
    }

    @Override
    public boolean initializeAgent(int number) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean openFileWriter(String name) throws RemoteException {
        boolean res = false;
        handler = new FileHandler();
        try {
            handler.openFileWriter(name);
            res = true; //success
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return res;
    }

    @Override
    public boolean closeFile(String name) throws RemoteException {
        boolean res = false;
        try {
            handler.closeFile();
            res = true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return res;
    }

    @Override
    public boolean putData(String name, byte[] buff) throws RemoteException {
        boolean res = false;
        try {
            handler.writeFile(buff);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return res;
    }

    @Override
    public boolean synDirs() throws RemoteException {
        try {
            DistributedSystem ob = (DistributedSystem) Naming.lookup(MainFrameHost);
            ob.synDirs();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    @Override
    public LinkedList<Individual> JobSync(LinkedList<Individual> indv) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean distributejob(LinkedList<Individual> a) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void exitSystem() throws RemoteException {
        try {
            DistributedSystem ob = (DistributedSystem) Naming.lookup(MainFrameHost);
            ob.exitSystem();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
     * Add a simulated user from some remote machine
     *
     * create multiple users based on different functions from the same Host
     */
    @Override
    public int addUser(String rmi, int[] chosenFF) throws RemoteException {
        int res = 0;

        String[] stAr = rmi.split(",");
        String host = stAr[0];
        res = Integer.parseInt(stAr[1]);
        //int[] chosenBMP = ;
        if (IGAMI2Main.countUsers < userMax) {
            //res = ++usrCount;
            UserData usr = new UserData(res, chosenFF, host);
            IGAMI2Main.setUsrParam(chosenFF, chosenBMPs);
            IGAMI2Main.users.add(usr);//added a new user

        }
        return res;
    }

    @Override
    public LinkedList<Individual> takeFeedback(LinkedList<Individual> indv) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean showResult(LinkedList<Individual> indv, double[] SDMRank, double[] data) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LinkedList<Individual> doIntrospection(LinkedList<Individual> IndvPop) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static void copyNewFilestoMainFrame(DistributedSystem ob, String file) {

        File f = new File(file);
        int sizeofBuf = 1048576;
        FileInputStream in = null;
        try {

            in = new FileInputStream(f);
            int len = 0;
            if (ob.openFileWriter(file)) {


                byte[] buff = new byte[sizeofBuf];//create new every time

                while ((len = in.read(buff)) > 0) //not reading the last byte
                {

                    //make RMI call to send the file
                    if (len == sizeofBuf) {
                        ob.putData(file, buff);
                        //System.out.println("Successfully copied bytes " + len);
                    } else if (len > 0) {
                        byte[] buuf = new byte[len];
                        for (int i = 0; i < len; i++) {
                            buuf[i] = buff[i];
                        }
                        ob.putData(file, buuf);
                        //System.out.println("Successfully copied bytes " + len);                    
                    }
                }

                ob.closeFile(file);

                in.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public synchronized boolean asyncMsg(int id, String type, boolean val) throws RemoteException {
        boolean res = true;

        if (type.compareToIgnoreCase("SYNCDIR") == 0) //got the DIR Syn Msg
        {
            if (val) {
                System.out.println("SWAT Directory Synchronization done sucessfully");
            } else {
                System.out.println("SWAT Directory Synchronization Failed");
            }
            //further msg processing
        } else if (type.compareToIgnoreCase("CLEANDIR") == 0) //got the DIR Clean Msg
        {
            if (val) {
                System.out.println("Sucessfully Clean SWAT DIRs for Agent " + id);
            } else {
                System.out.println("Failed to Clean SWAT DIRs for Agent " + id);
            }
        }

        try {
            DistributedSystem ob = (DistributedSystem) Naming.lookup(this.MainFrameHost);



            if (type.compareToIgnoreCase("uploadnewfinal") == 0) {
                this.swatFile = this.swatEvalautionFile;
                this.copyNewFilestoMainFrame(ob, swatFile);
                ob.asyncMsg(0, "uploadnewfinal", true);
            } else if (type.compareToIgnoreCase("uploadnewresearch") == 0) {
                this.swatFile = this.swatResearchFile;
                this.copyNewFilestoMainFrame(ob, swatFile);
                ob.asyncMsg(0, "uploadnewresearch", true);

            } else if (type.compareToIgnoreCase("sync") == 0) {
                ob.asyncMsg(0, "sync", true);
                //ob.synDirs(); //simply sync the dirs

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return res;
    }
    //one to one correspondance
    private static void scheduleAnyWaiting() {
        String type = "SINGLE"; //type.compareToIgnoreCase("SINGE")==0
        if (SingleNodeWaitingQueue.size() > 0) { //always single node is allocated
            LinkedList<Individual> a = (LinkedList<Individual>) SingleNodeWaitingQueue.removeFirst();
            sendEvaluation(a);
        }
    }
}
