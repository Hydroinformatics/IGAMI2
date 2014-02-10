/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.ClusterComputerHeadNode;

import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import igami2.DistributedSystem.DistributedSystem;
import java.rmi.Naming;
import java.rmi.registry.Registry;
import java.util.LinkedList;

/**
 *
 * @author VIDYA
 */
public class SuperComputerRMIMonitorThread implements Runnable{
    
    private LinkedList<Individual> a;
    Thread t;
    String host;
    int port;
    private Registry registry;
    
    public SuperComputerRMIMonitorThread(LinkedList<Individual> a,String host,int port)
    {
        this.host = host;
        this.a = a;
        this.port = port;
        t = new Thread(this,"WorkTh");
        t.start();
    }

    @Override
    public void run() {
         LinkedList<Individual> res = null;
            
           try{
               
            DistributedSystem ob = (DistributedSystem) Naming.lookup(host);            

            ob.JobAsyncResult(a,0);
            
        } catch (Exception ex) {
            System.out.println("Problem in connecting host "+ host);
            ex.printStackTrace();
        }       
    }
}