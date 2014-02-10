/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.Optimization.DPLA;

/**
 *
 * @author VIDYA
 */
public class DPLAThread implements Runnable{
    
    Thread t;
    
    DPLAThread()
    {
        t = new Thread(this,"");
        
    }

    public void begin()
    {
        t.start();        
    }
    
    public void run() {
       
        
    }
    
    
}
