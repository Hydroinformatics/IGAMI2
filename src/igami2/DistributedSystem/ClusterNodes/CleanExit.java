/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.ClusterNodes;

/**
 *
 * @author VIDYA
 */
public class CleanExit implements Runnable{

    Thread t;
    public CleanExit()
    {
        t = new Thread(this,"CleanSystem");
        t.start();
    }
    @Override
    public void run() {
        //do cleanup
        
        //exit the program
        System.exit(0);
    }
    
}
