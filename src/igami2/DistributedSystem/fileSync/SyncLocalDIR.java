/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.fileSync;

import java.io.*;
import java.util.Vector;

/**
 *
 * @author VIDYA
 */
public class SyncLocalDIR {

    
    String local = "../SWAT/swat_dirs/";
    private String DIRname = "swat";
    int idx;
    boolean decompress = true;

    public void sync(int p) {
        
        Decompress d = new Decompress();
        File f = null;
        File f2 = new File(local);
        if(!f2.exists())
        {
            f2.mkdirs();
            return;
        }
        for(int i=0;i<p;i++)
        {
            d.decompressDIR(local, DIRname);
             f = new File(local+DIRname);
             //delete the old instance;
             File f1 = new File(local+DIRname+i);
             if(f1.exists())//old dir exists
             deleteOldDir(local+DIRname+i);
             f1.delete();
            f.renameTo(new File(local+DIRname+i));            
        }

    }

    private static void deleteOldDir(String name) {
        DirectoryListing d = new DirectoryListing();
        Vector lst = d.getDIRList(name);
        for (int i = 0; i < lst.size(); i++) {
            File f = new File((String) lst.get(i));
            f.delete();//delete all the files
        }

    }
}
