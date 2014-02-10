/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.MasterComputer;

import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import igami2.DistributedSystem.DistributedSystem;
import java.rmi.Naming;
import java.rmi.registry.Registry;
import java.util.LinkedList;

/**
 *
 * @author VIDYA
 */
public class MasterComputerRMIMonitorThread implements Runnable{
    
    private LinkedList<Individual> a;
    Thread t;
    String host;
    int port;
    private Registry registry;
    
    public MasterComputerRMIMonitorThread(LinkedList<Individual> a,String host,int port)
    {
        this.host = host;
        this.a = a;
        this.port = port;
        t = new Thread(this,"WorkTh");
        t.start();
    }

    @Override
    public void run() {
        boolean res;

        try {

            DistributedSystem ob = (DistributedSystem) Naming.lookup(host);

            res = ob.distributejob(a);

            if (res) {
                System.out.println("Send the Job Successsfully");
            } else {
                System.out.println("failed");
            }

        } catch (Exception ex) {
            System.out.println("Problem in connecting host " + host);
            ex.printStackTrace();

            
            while (true) {
                try {
                    Thread.sleep(5000);
                    DistributedSystem ob = (DistributedSystem) Naming.lookup(host);

                    res = ob.distributejob(a);

                    if (res) {
                        System.out.println("Send the Job Successsfully");
                        break;
                    } else {
                        System.out.println("failed");
                    }

                } catch (Exception e) {                    
                    System.out.println("Exception Occured Again " + a.get(0).UserId);
                }
            }
        }
    }
}