/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.ClusterNodes;

import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import igami2.DistributedSystem.DistributedSystem;
import igami2.DistributedSystem.fileSync.FileHandler;
import igami2.DistributedSystem.fileSync.SyncLocal;
import igami2.DistributedSystem.fileSync.SyncLocalThread;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
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
public class VirtualAgent extends UnicastRemoteObject implements DistributedSystem{
    private static Registry registry;
    public static int port = 11555;
    //private static String MainFrameHost =  "rmi://tempest.ads.iu.edu:"+port+"/MainFrameController";
    //private static String MainFrameHost =  "rmi://localhost:"+port+"/MainFrameController";
    private static String MainFrameHost =  "rmi://in-geol-esaig01.geology.iupui.edu:"+port+"/MainFrameController";
    private static int AgentId;
    private static String base="";
    private static String name;
    private static int userMax = 5;//No of simultaneous Users handled at a node
    private static int p = 4; //No of instances the system is able to run on its own
    private static int pMax = 20; //max p
    private static int pCurrent=0;
    public static LinkedList<Individual>[] ArrayIndvList;
    public static boolean[] freeslot;
    private FileHandler handler;
    
    
    public VirtualAgent(String name) throws RemoteException
    {        
        super();
        this.name = name;
        ArrayIndvList = new LinkedList[userMax];
        freeslot = new boolean[userMax];
        for(int i=0;i<userMax;i++)
        {
            ArrayIndvList[i] = new LinkedList<Individual>();
            freeslot[i] = true;
        }
    }

    

    /*
     * agrs[0] is host name of the cluster
     * args[1] is number of No of Swat Models
     * args[2] is sync DIRs or not
     */
    
    public static void main(String args[])
    {
        
        if (System.getSecurityManager() == null) {
        System.setSecurityManager(new RMISecurityManager());
        }
        
       if(args!=null)
       {
        if(args.length>0)
        {          
            String hostname = args[0];
            MainFrameHost =  "rmi://"+hostname+":"+port+"/MainFrameController";
        }
        if(args.length>1)
        {
            int p1 = Integer.parseInt(args[1]);
            p = p1;
            
        }
        if(args.length>2)
        {
            Boolean stat = Boolean.parseBoolean(args[2]);
            if(stat)
                SyncLocal.sync(p);
        }
        
       }
        
        try{
            registry = LocateRegistry.createRegistry(port);
            //
        }catch(RemoteException e)
        {
            try {
                //System.out.println("Reg Not running ");                       
                registry = LocateRegistry.getRegistry(port);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
        
        try {
            
            String name = InetAddress.getLocalHost().getHostName();
            String str = "rmi://"+name+":"+port+"/AgentMain";
            //MO mO = new MO(Name,T,N);
            VirtualAgent agent = new VirtualAgent(str);
            //String str = name+":"+port+"/Agent";
            str = "//localhost:"+port+"/AgentMain";
            
            System.out.println("Agent Binding to the name " +name);
            Naming.rebind(str, agent);
            System.out.println("GA Agent Ready and Running at "+str);
            
            //SyncHost mh = new SyncHost("SYNCSWAT");
            //mh.startHost();
            System.out.println("Waiting for job");
            
            
        } catch (Exception e) {
            System.out.println("Exception orrcured in Agent: " + e.getMessage());
        }
    }
    
    @Override
    public boolean initializeAgent(int id) throws RemoteException {
        System.out.println("Agent Initiated "+ id);
        this.AgentId = id;
        return true;
    }

    @Override
    public boolean JobAsyncResult(LinkedList<Individual> indv, int agentId) throws RemoteException {

        boolean res = false; //job was not sucessful
        
        System.out.println("Got the job and Running");
        for(int i=0;i<userMax;i++)
        {
            if(freeslot[i]) //check the ith slot is free or not
            {
                ArrayIndvList[i] = indv;
                pCurrent = pCurrent + indv.size();
                freeslot[i] = false; //make it busy
                new AsyncEvaluationThread(i,p); //give the job to local thread and return
                res = true;
                break;
            }
        }        
        return res;
    }

    public static synchronized void AsyncReturn(int ArIdx)
    {
        try {
            DistributedSystem ob = (DistributedSystem) Naming.lookup(MainFrameHost);
            LinkedList<Individual> indv = new LinkedList<Individual>();
            for(int i=0;i<ArrayIndvList[ArIdx].size();i++)
            indv.add((Individual)ArrayIndvList[ArIdx].get(i).clone());
            
            //print fitness functions
            for(int i=0;i<indv.size();i++)
            {
                Individual in = indv.get(i);
                System.out.print("\nIndv "+i);
                for(int j =0;j<in.fitnessValues.length;j++)
                {
                    System.out.print("\tF"+j+"="+in.fitnessValues[j]);                    
                }               
                
            }
            /*
            //print subbasinFF
            System.out.println("\nSubbasinFFs");          
            for(int i=0;i<indv.size();i++)
            {
                System.out.println("Indv "+i);
                double[][] subffs = indv.get(i).subbasinsFF;
                for(int j=0;j<subffs.length;j++)
                {
                    System.out.print("SubId"+(j+1));
                    for(int k=0;k<subffs[0].length;k++)
                    {
                        System.out.print("\t"+subffs[j][k]);
                    }
                    System.out.println();
                }
            }
            * 
            */
             
            
            ob.JobAsyncResult(indv, AgentId);
            ArrayIndvList[ArIdx].clear();//empty
            pCurrent = pCurrent - ArrayIndvList[ArIdx].size();
            freeslot[ArIdx] = true;
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }    
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
        boolean res = true;
        try {
            
             DistributedSystem ob = (DistributedSystem) Naming.lookup(MainFrameHost);
            new SyncLocalThread(p,ob, AgentId);
            
            
        } catch (NotBoundException ex) {
            Logger.getLogger(VirtualAgent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(VirtualAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    @Override
    public LinkedList<Individual> JobSync(LinkedList<Individual> indv) throws RemoteException {
        System.out.println("Got the job from User"+indv.get(0).UserId);
        
        LinkedList<Individual> res = new LinkedList<Individual>();
        /*
        System.out.println("Fitness Value Before");
        
        for(int i=0;i<indv.length;i++)
        {
            System.out.println("Indv "+i+" F0= "+indv[i].fitnessValues[0]+" F1= "+indv[i].fitnessValues[1]);
            //rest[i].nsga2 = nsg;
        }
         * 
         */
        
        DistributeJob job = new DistributeJob();
        
        Individual[] rest = job.makeParallel(p,indv);
        for(int i=0;i<rest.length;i++)
        {
            res.add(rest[i]);            
            //rest[i].nsga2 = nsg;
        }
        
        System.out.println("Job Completed Sucessfully");
        
        return res;
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

    @Override
    public boolean asyncMsg(int id,String type, boolean val) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
}
