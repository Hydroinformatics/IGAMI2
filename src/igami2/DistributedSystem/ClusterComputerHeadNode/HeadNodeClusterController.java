/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.ClusterComputerHeadNode;

/**
 *
 * @author VIDYA
 */
/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
import igami2.DistributedSystem.ClusterNodes.VirtualAgent;
import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import igami2.DistributedSystem.DistributedSystem;
import igami2.DistributedSystem.fileSync.FileHandler;
import java.io.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vidya
 */
public final class HeadNodeClusterController extends UnicastRemoteObject implements DistributedSystem {

    public static int NumberOfNodes = 8;
    public static int ActiveNodes;
    public static int port = 11555;
    static boolean[] status;
    static boolean[] free;
    static Individual[] a1;
    public static Individual[] a2;
    static String base = "";
    public static Registry registry, registry1;
    static String name;
    static String mainhost;
    static int UsersMax = 50;//No of simultaneous Users handled by the MainFrame at a time
    public static LinkedList<Individual>[] ArrayIndvList;
    public static LinkedList SingleNodeWaitingQueue; //have priority over single node queue
    public static LinkedList MultipleNodeWaitingQueue; //have less priority, but next time move to SingleNodeWaitingQueue to increase priority
    private static int pMax = 4;//Max 20 instances at a time by a node
    private static VirtualAgentData[] Nodes;
    private static String masterHost = "rmi://in-geol-esaig06.ads.iu.edu:" + port + "/MasterComputer";
    private static String swatEvalautionFile = "../SWAT/swat_dirs/swat.zip"; //use for normal user evaluation
    private static String swatResearchFile = "../SWAT/RESEARCH/swat_dirs/swat.zip"; //use for reserach type of evaluations
    private static String swatFile = "../SWAT/swat_dirs/swat.zip"; //determined based on file type being received
    private static boolean currenthost;
    public static int ClusterId; //use to uniquely identify the clsuter
    private boolean multipleAgents = true; //use multiple agents to run simultaneously
    //private static LinkedList users;
    private static Map users;
    private static int currentAvailNodes;
    private FileHandler handler;

    public HeadNodeClusterController(String name) throws RemoteException {
        super();
        this.name = name;

        ActiveNodes = 0;//self
        ArrayIndvList = new LinkedList[UsersMax];
        for (int i = 0; i < UsersMax; i++) {
            ArrayIndvList[i] = new LinkedList<Individual>();
        }
        //users = new LinkedList();
        users = new HashMap();
        SingleNodeWaitingQueue = new LinkedList();
        MultipleNodeWaitingQueue = new LinkedList();

    }
    //Storing the Identity of Each Nodes as their Host Address
    //public static String[] Nodes = {"sl253-rrpc01.cs.iupui.edu", "sl253-rrpc02.cs.iupui.edu", "sl253-rrpc03.cs.iupui.edu", "sl253-rrpc04.cs.iupui.edu", "sl253-rrpc05.cs.iupui.edu"};

    public static void initController(boolean sync, boolean copyFiles) throws RemoteException, UnknownHostException, MalformedURLException, AlreadyBoundException {
        //load from config
        String configFile = "config.txt";

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        try {
            FileReader f = new FileReader(configFile);
            BufferedReader br = new BufferedReader(f);
            String line = null;
            ArrayList strs = new ArrayList();
            String str;
            String nam = InetAddress.getLocalHost().getHostName();
            currenthost = false;
            while ((line = br.readLine()) != null) {
                if (line.compareToIgnoreCase("localhost") == 0) {
                    currenthost = true;
                    str = "rmi://localhost:" + port + "/AgentMain";
                } else {
                    str = "rmi://" + line + ":" + port + "/AgentMain";
                }
                strs.add(str);
            }
            //host = new String[strs.size()];

            Nodes = new VirtualAgentData[strs.size()];//Number of all the Available Nodes



            for (int i = 0; i < strs.size(); i++) {
                Nodes[i] = new VirtualAgentData();//initialize
                Nodes[i].HostName = (String) strs.get(i);
            }
            NumberOfNodes = Nodes.length;


            if (currenthost) {
                VirtualAgent.main(null); //use current host
            }

            //System.out.println("No of Active Nodes are "+NumberOfNodes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        initAgent(sync, copyFiles);


    }

    /*
     * Command Linea Arguments
     * 1st one to sync the existing files to swat DIRS
     * 2nd to copy New files to swat DIRS
     */
    public static void main(String args[]) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }

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

        try {

            String name = InetAddress.getLocalHost().getHostName();

            boolean sync = false;
            boolean copyFiles = false;
            if (args.length > 0) {
                sync = Boolean.parseBoolean(args[0]);
            }
            if (args.length > 1) {
                copyFiles = Boolean.parseBoolean(args[1]);
            }

            //name = "rmi://" + name + ":" + port + "/MainFrameController";
            name = "rmi://hydroinf.engr.oregonstate.edu" + ":" + port + "/MainFrameController";
            System.out.println("MasterComputer Binding to the name " + name);
            //MO mO = new MO(Name,T,N);

            //String str = name+":"+port+"/Agent";
            String str = "//localhost:" + port + "/MainFrameController";
            //String str = "//hydroinf.engr.oregonstate.edu:" + port + "/MainFrameController";

            //String str = "//149.165.236.202:"+port+"/MainFrameController";
            HeadNodeClusterController ob = new HeadNodeClusterController(str);
            Naming.rebind(str, ob);
            System.out.println("MasterComputer Ready and Running");

            initController(sync, copyFiles);

            System.out.println("Waiting for job");


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception orrcured in MasterComputer: " + e.getMessage());
        }

    }

    private static void initAgent(boolean sync, boolean copyFiles) {
        //initialize the hosts //Remote Call

        for (int i = 0; i < NumberOfNodes; i++) {

            String name = Nodes[i].HostName;
            //String name = "//"+host[i][0]+":"+host[i][1]+"/Agent";                
            try {
                //registry1 = LocateRegistry.getRegistry();
                DistributedSystem ob = (DistributedSystem) Naming.lookup(name);
                //System.out.println(name +" Host prog Not Running");
                Nodes[i].status = ob.initializeAgent(i);
                if (Nodes[i].status && copyFiles) //send the swat.zip file to the Agent node
                {
                    if (!currenthost) {
                        copyNewFilestoAgents(ob, swatFile);
                    } else if (i != (NumberOfNodes - 1)) //the last one won't be copied
                    {
                        copyNewFilestoAgents(ob, swatFile);
                    }
                    System.out.println("DIR Successfully copied to Agent " + i);
                } else if (Nodes[i].status && sync) //simply sync the existing swat files
                {
                    ob.synDirs();
                }
                System.out.println("Agent Active " + name + " with AgentId " + i);
            } catch (Exception ex) {
                //System.out.println(name + " Error Connecting");
                //ex.printStackTrace();
                //ex.getMessage();
            }
            if (!Nodes[i].status) {
                System.out.println(name + "\t Agent prog Not Running");
            } else {
                ActiveNodes++;//count number of active hosts
            }
        }
        currentAvailNodes = ActiveNodes;
        System.out.println("Currently Active Agents are " + currentAvailNodes);
    }

    private static void copyNewFilestoAgents(DistributedSystem ob, String filename) {

        File f = new File(filename);
        int sizeofBuf = 1048576;
        FileInputStream in = null;
        try {

            in = new FileInputStream(f);
            int len = 0;
            if (ob.openFileWriter(filename)) {


                byte[] buff = new byte[sizeofBuf];//create new every time

                while ((len = in.read(buff)) > 0) //not reading the last byte
                {

                    //make RMI call to send the file
                    if (len == sizeofBuf) {
                        ob.putData(filename, buff);
                        //System.out.println("Successfully copied bytes " + len);
                    } else if (len > 0) {
                        byte[] buuf = new byte[len];
                        for (int i = 0; i < len; i++) {
                            buuf[i] = buff[i];
                        }
                        ob.putData(filename, buuf);
                        //System.out.println("Successfully copied bytes " + len);                    
                    }
                }

                ob.closeFile(filename);

                ob.synDirs();

                in.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public synchronized boolean distributejob(LinkedList<Individual> a) throws RemoteException {

        boolean res = true;
        int UserId = a.get(0).UserId;

        System.out.println("Took Job for user " + UserId);


        int len = a.size();
        int nodes_needed = len / pMax;
        //int extr = len%pMax;
        if (nodes_needed * pMax != len) {
            nodes_needed++; //one more node is needed for last set of indvs
        }
        //check if sufficient nodes are avail




        if (nodes_needed > 1) { //put into multile node queue
            MultipleNodeWaitingQueue.add(a);
            System.out.println("Put in Multipe Node Waiting Queue User " + UserId);
        } else //put into waiting queue //only one node is needed
        {
            SingleNodeWaitingQueue.addLast(a);
            System.out.println("Put in Waiting Queue User " + UserId);
        }


        scheduleAnyWaiting();


        return res;
    }

    @Override
    public boolean initializeAgent(int id) throws RemoteException {
        //now useful
        ClusterId = id;
        System.out.println("Initialized with Cluster Id " + id);
        return true;
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
    /*
     * Return the results
     */
    public boolean JobAsyncResult(LinkedList<Individual> a, int NodeId) throws RemoteException {

        boolean res = false;
        int UserId = a.get(0).UserId;
        System.out.println("Got the Result of Agent " + NodeId);
        if (multipleAgents) {
            Nodes[NodeId].ArrayIndvList[0] = a; //save the results

            if (checkResult(UserId)) { //the last Agent
                //got the results from all the agents, copy back the data and send it to master
                LinkedList<Individual> results = new LinkedList();
                for (int i = 0; i < ActiveNodes; i++) {
                    if (Nodes[i].UserId[0] == UserId) //if the node belongs to this user
                    {
                        for (int j = 0; j < Nodes[i].ArrayIndvList[0].size(); j++) {
                            results.add(Nodes[i].ArrayIndvList[0].get(j));
                        }
                        Nodes[i].Users[0] = true;//make it free
                        Nodes[i].ActiveUserCount--;
                        Nodes[i].UserId[0] = 999; //release the node to system
                        Nodes[i].ArrayIndvList[0] = null;
                        currentAvailNodes++; //free one node
                    }
                }
                //schedule any existing waiting Indvs
                scheduleAnyWaiting(); //don't schedule untill all the nodes are free otherwise data will be overwritten before getting fetching the result
                MasterComputerAsyncResultThread agentMonitorThread = new MasterComputerAsyncResultThread(results, masterHost, port);
            } else {
                System.out.println("Waiting for others to finish");
            }
            res = true;
        } else { //single agent serving
            for (int i = 0; i < ActiveNodes; i++) {
                if (Nodes[i].UserId[0] == UserId) {
                    Nodes[i].Users[0] = true;//make it free
                    Nodes[i].ActiveUserCount--;
                    Nodes[i].UserId[0] = 999;
                    Nodes[i].ArrayIndvList[0] = null;
                    System.out.println("Freed Node No " + i + " by User " + UserId);
                    MasterComputerAsyncResultThread agentMonitorThread = new MasterComputerAsyncResultThread(a, masterHost, port);
                    currentAvailNodes++;
                    res = true;
                    break;
                }
            }
            //schedule any existing waiting Indvs
            scheduleAnyWaiting();
        }
        //schedule any existing waiting Indvs
        //scheduleAnyWaiting(); //schedule any waiting job
        return res;
    }

    @Override
    public boolean synDirs() throws RemoteException {

        for (int i = 0; i < NumberOfNodes; i++) {

            String name = Nodes[i].HostName;

            try {

                DistributedSystem ob = (DistributedSystem) Naming.lookup(name);

                if (Nodes[i].status) //send the swat.zip file to the Agent node
                {
                    if (Nodes[i].status) //simply sync the existing swat files
                    {
                        ob.synDirs();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public LinkedList<Individual> JobSync(LinkedList<Individual> indv) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void exitSystem() throws RemoteException {

        for (int i = 0; i < ActiveNodes; i++) {
            String name = Nodes[i].HostName;
            //String name = "//"+host[i][0]+":"+host[i][1]+"/Agent";                
            try {
                //registry1 = LocateRegistry.getRegistry();
                DistributedSystem ob = (DistributedSystem) Naming.lookup(name);
                //System.out.println(name +" Host prog Not Running");
                ob.exitSystem();
                System.out.println("Cleaned Node " + name);
            } catch (Exception ex) {
                //System.out.println(name + " Error Connecting");
                //ex.printStackTrace();
                //ex.getMessage();
            }
        }
    }

    @Override
    public int addUser(String host, int[] chosenFF) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
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

    /*
     * Use to wait for the results of all the agents
     */
    private synchronized boolean checkResult(int userId) {
        boolean res = false;
        int count = Integer.parseInt("" + users.remove(userId));
        count--; //result from the last node
        if (count == 0) {
            res = true;
        } else {
            users.put(userId, count); //put the count back
        }
        return res;
    }

    private void allocateNodes(LinkedList<Individual> a, String type) {
        int UserId = a.get(0).UserId;

        System.out.println("Allocating Nodes for User " + UserId);
        int len = a.size();
        int nodes_needed = len / pMax;
        //int extr = len%pMax;
        if (nodes_needed * pMax != len) {
            nodes_needed++; //one more node is needed for last set of indvs
        }

        if (nodes_needed <= currentAvailNodes && type.compareToIgnoreCase("MULTIPLE") == 0) //Multiple nodes Needed
        {
            //pMax;


            //int numPerAgent = len / (ActiveNodes); //One the controller //still need to be optimized
            //int extr = len % (ActiveNodes); //Extra the controller


            users.put(UserId, nodes_needed); //count how many nodes reserved for one user and how many results to be expected
            LinkedList<Individual> job[];

            SuperComputerRMIMonitorThread[] mon = new SuperComputerRMIMonitorThread[nodes_needed];

            job = new LinkedList[nodes_needed];

            for (int i = 0; i < nodes_needed; i++) {
                job[i] = new LinkedList<Individual>();
                for (int j = 0; j < pMax; j++) {
                    int loc = i * pMax + j;
                    if (loc < len) {
                        job[i].add(a.get(loc));
                    }
                }
            }
            //create NumberOfNodes of threads to distribute job

            int c = 0;

            for (int i = 0; i < ActiveNodes; i++) {
                if (Nodes[i].Users[0]) {
                    Nodes[i].Users[0] = false;//make it busy
                    Nodes[i].ActiveUserCount++;
                    Nodes[i].UserId[0] = UserId;
                    Nodes[i].ArrayIndvList[0] = job[c];
                    System.out.println("Reserved Node No " + i + " by User " + UserId);
                    SuperComputerRMIMonitorThread agentMonitorThread = new SuperComputerRMIMonitorThread(job[c], Nodes[i].HostName, port);
                    c++;
                    if (c == nodes_needed)// sufficient nodes are allocated now break the loop. no more nodes needed
                    {
                        break;
                    }
                }

            }
            if (c != nodes_needed) {
                System.out.println("Not Enough Nodes allocated"); //need to modify for more unallocated nodes
            }
            currentAvailNodes = currentAvailNodes - c; //reserved c nodes

        } else { //type.compareToIgnoreCase("SINGLE")==0 DEFAULT

            //A Single Agent can service only one Indv    

            for (int i = 0; i < ActiveNodes; i++) {
                if (Nodes[i].Users[0]) {
                    Nodes[i].Users[0] = false;//make it busy
                    Nodes[i].ActiveUserCount++;
                    Nodes[i].UserId[0] = UserId;
                    Nodes[i].ArrayIndvList[0] = a;
                    System.out.println("Reserved Node No " + i + " by User " + UserId);
                    SuperComputerRMIMonitorThread agentMonitorThread = new SuperComputerRMIMonitorThread(a, Nodes[i].HostName, port);
                    users.put(UserId, 1); //count how many nodes reserved for one user and how many results to be expected
                    break;
                }
            }
            currentAvailNodes--; //reserved 1 node
        }
    }

    /*
     * Scheduler needed to be optimized based on need
     */
    private void scheduleAnyWaiting() {
        String type = "SINGLE"; //type.compareToIgnoreCase("SINGE")==0
        if (SingleNodeWaitingQueue.size() > 0) { //always single node is allocated
            LinkedList<Individual> a = (LinkedList<Individual>) SingleNodeWaitingQueue.removeFirst();
            // A single Agent
            if (1 <= currentAvailNodes) // 1 nodes available
            {


                allocateNodes(a, type);
            } else { //not even a single node available
                SingleNodeWaitingQueue.addFirst(a); //put back to the begining, high priority
                System.out.println("User Waiting in Queue " + a.get(0).UserId);
            }
        }

        if (MultipleNodeWaitingQueue.size() > 0) { //multple node jobs are allocated here, but if sufficient nodes are not available then 
            LinkedList<Individual> a = (LinkedList<Individual>) MultipleNodeWaitingQueue.removeFirst();

            int len = a.size();
            int nodes_needed = len / pMax;
            //int extr = len%pMax;
            if (nodes_needed * pMax != len) {
                nodes_needed++; //one more node is needed for last set of indvs
            }

            //in case of multiple Agents
            if (nodes_needed > 1 && multipleAgents) {
                if (nodes_needed <= currentAvailNodes) // sufficient nodes available
                {
                    type = "MULTIPLE";
                    allocateNodes(a, type);
                } else {
                    SingleNodeWaitingQueue.addFirst(a); //put back to the begining
                    System.out.println("User moved From Multiple Node Queue to Single Node Queue " + a.get(0).UserId);
                    scheduleAnyWaiting();//try scheduling on Single node
                }
            } else // A single Agent //when multipleAgents are not allowed so use only single agent
            {
                if (1 <= currentAvailNodes) // 1 nodes available
                {
                    allocateNodes(a, type);
                } else { //not even a single node available
                    SingleNodeWaitingQueue.addFirst(a); //put back to the begining, high priority
                    System.out.println("User Waiting in Queue " + a.get(0).UserId);
                }

            }
        }
    }

    @Override
    public synchronized boolean asyncMsg(int id, String type, boolean val) throws RemoteException {

        boolean res = true;
        try {


            DistributedSystem ob = (DistributedSystem) Naming.lookup(this.masterHost);

            if (type.compareToIgnoreCase("SYNCDIR") == 0) //got the DIR Syn Msg
            {
                if (val) {
                    System.out.println("SWAT Directory Synchronization done sucessfully for Agent " + id);
                } else {
                    System.out.println("SWAT Directory Synchronization Failed for Agent " + id);
                }
                ob.asyncMsg(id, type, val);

            } else if (type.compareToIgnoreCase("CLEANDIR") == 0) //got the DIR Clean Msg
            {
                if (val) {
                    System.out.println("Sucessfully Clean SWAT DIRs for Agent " + id);
                } else {
                    System.out.println("Failed to Clean SWAT DIRs for Agent " + id);
                }
                ob.asyncMsg(id, type, val);
            }

            if (type.compareToIgnoreCase("uploadnewfinal") == 0) {
                this.swatFile = this.swatEvalautionFile;
                copyDIRAgents(swatFile);
            } else if (type.compareToIgnoreCase("uploadnewresearch") == 0) {
                this.swatFile = this.swatResearchFile;
                copyDIRAgents(swatFile);

            } else if (type.compareToIgnoreCase("sync") == 0) {
                this.synDirs(); //syn DIRs of all the Agents          
            }

            //further msg passing to Master Computer Based on need


        } catch (NotBoundException ex) {
            Logger.getLogger(HeadNodeClusterController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(HeadNodeClusterController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    private void copyDIRAgents(String filename) {
        for (int i = 0; i < NumberOfNodes; i++) {

            String name = Nodes[i].HostName;

            try {

                DistributedSystem ob = (DistributedSystem) Naming.lookup(name);

                Nodes[i].status = ob.initializeAgent(i);
                if (Nodes[i].status) //send the swat.zip file to the Agent node
                {
                    if (!currenthost) {
                        copyNewFilestoAgents(ob, filename);
                    } else if (i != (NumberOfNodes - 1)) //the last one won't be copied
                    {
                        copyNewFilestoAgents(ob, filename);
                    }
                    System.out.println("DIR Successfully copied to Agent " + i);
                }


            } catch (Exception ex) {
                //System.out.println(name + " Error Connecting");
                //ex.printStackTrace();
                //ex.getMessage();
            }

        }
    }
}
