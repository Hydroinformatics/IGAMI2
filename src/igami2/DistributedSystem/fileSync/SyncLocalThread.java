/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.fileSync;

import igami2.DistributedSystem.DistributedSystem;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author VIDYA
 */
public class SyncLocalThread implements Runnable{
    Thread t;
    private int p;
    DistributedSystem ob ;
    int AgentId;
    
    public SyncLocalThread(int p,DistributedSystem ob, int AgentId)
    {
        this.ob=ob;
        this.p= p;
        this.AgentId = AgentId;
        t = new Thread(this,"SyncThread");
        t.start();
    }
    
    public void run()
    {
        try {
            SyncLocalDIR synOb = new SyncLocalDIR();
            synOb.sync(p);
            System.err.println("Sucessfully Copied the SWAT Directories");
            ob.asyncMsg(AgentId,"SYNCDIR", true);
        } catch (RemoteException ex) {
            Logger.getLogger(SyncLocalThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
}
