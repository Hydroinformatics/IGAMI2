/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.fileSync;

import java.io.*;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author VIDYA
 */
public class SyncLocal {
    
    static String local = "../SWAT/swat_dirs/";
    private static boolean decompress = true;
    private static String dir = "swat";
    private static byte[] buff;
    private static int MAX = 1024;
    
    public static void sync(int p)
    {
        
        SyncLocalDIR ob = new SyncLocalDIR();
        ob.sync(p);
    }
    
    public static void main(String args[])
    {
        int p = 4;
        SyncLocalDIR ob = new SyncLocalDIR();
        ob.sync(p);
    }
       
    
    
}
