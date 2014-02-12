/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.fileSync;

import java.io.*;
import java.util.ArrayList;

/**
 *
 * @author VIDYA
 */
public class CleanLocalDIR {

    
    String local = "../swat_dirs/";
    private String DIRname = "swat";
    

    public void clean(int p) {
        
        
        for(int i=0;i<p;i++)
        {            
             //delete the old instance;
             File f1 = new File(local+DIRname+i);
             if(f1.exists())//old dir exists
             deleteOldDir(local+DIRname+i);
             f1.delete();                  
             System.out.println("deleted "+f1.getName());
        }
    }

    private static void deleteOldDir(String name) {
        DirectoryListing d = new DirectoryListing();
        ArrayList lst = d.getDIRList(name);
        for (int i = 0; i < lst.size(); i++) {
            File f = new File((String) lst.get(i));
            f.delete();//delete all the files
            System.out.println("deleted "+f.getAbsolutePath());
        }
    }
    
    public static void main(String args[])
    {
        int p=4;
        if(args.length>0)
        {
            p = Integer.parseInt(args[0]);
        }
        CleanLocalDIR cleanOb = new CleanLocalDIR();
        cleanOb.clean(p);
        System.out.println("Sucessfully Deleted SWAT DIRS");
    }
}
