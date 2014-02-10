/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.fileSync;

import igami2.DistributedSystem.DistributedSystem;
import java.io.File;
import java.rmi.RemoteException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author VIDYA
 */
public class CleanLocalThread implements Runnable{
    Thread t;
    private int p;
    DistributedSystem ob ;
    int AgentId;    
    public CleanLocalThread(int p,DistributedSystem ob, int AgentId)
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
            CleanLocalDIR cleanOb = new CleanLocalDIR();
            cleanOb.clean(p);
            System.out.println("Sucessfully Copied the SWAT Directories");
            ob.asyncMsg(AgentId,"CLEANDIR", true);
        } catch (RemoteException ex) {
            Logger.getLogger(CleanLocalThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }  
}
