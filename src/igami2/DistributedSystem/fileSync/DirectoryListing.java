/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.fileSync;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author VIDYA
 */
public class DirectoryListing {
    
    ArrayList list;
    DirectoryListing()
    {
        
    }

    public static void main(String args[]) {
        
        String dirname = "SWAT";
        File f1 = new File(dirname);
        
        DirectoryListing d = new DirectoryListing();
        
        
        ArrayList v = d.getDIRList(dirname);
        for(int i=0;i<v.size();i++)
        {
            System.out.println(v.get(i));
        }
        
        System.out.println(".........");
        
        //d.dirclist(dirname);
        /*
        if (f1.mkdir()) {
            System.out.println("Created Directory" + dirname);
        } else {
            System.out.println("Failed to create directory" + dirname);
        }

        
        if (f1.isDirectory()) {
            System.out.println("Directory of " + dirname);
            String s[] = f1.list();

            for (int i = 0; i < s.length; i++) {
                File f = new File(dirname + "/" + s[i]);
                if (f.isDirectory()) {
                    System.out.println(s[i] + " is a directory");
                } else {
                    System.out.println(s[i] + " is a file");
                }
            }
        } else {
            System.out.println(dirname + " is not a directory");
        }
         * 
         */
    }

    private void dirlist(String fname) {
        File dir = new File(fname);
        String[] chld = dir.list();
        if (dir.isFile()) {
            System.out.println(fname + "/" + dir.getName());
           

        } else if (dir.isDirectory()) {
            System.out.println(fname);//.substring(fname.lastIndexOf("/")));
            for (int i = 0; i < chld.length; i++) {
                dirlist(fname + "/" + chld[i]);
            }
        }
    }
   
    public ArrayList getDIRList(String name)
    {
        list = new ArrayList();
        dirclist(name);
        String[] dir = new String[list.size()];
        for(int i=0;i<list.size();i++)
        {
            dir[i] = "" + list.get(i);
        }
        
        return list;
    }
     public void dirclist(String fname) {
         
         
        File dir = new File(fname);
        String[] chld = dir.list();
        if (dir.isFile()) {
            //System.out.println(fname);
            list.add(fname);
          

        } else if (dir.isDirectory()) {
            //System.out.println(fname);//.substring(fname.lastIndexOf("/")));
            list.add(fname+"/");
            for (int i = 0; i < chld.length; i++) {
                dirclist(fname + "/" + chld[i]);
            }
        }
    }
    
}
