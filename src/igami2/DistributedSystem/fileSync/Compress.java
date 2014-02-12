/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.fileSync;

/**
 *
 * @author VIDYA
 */
import java.io.*;
import java.rmi.RMISecurityManager;
import java.util.ArrayList;
import java.util.zip.*;

public class Compress {

    static String dir="swat";
        
    static String location ="F:/SWAT/swat_dirs/";
    static final int BUFFER = 1024;

    public static void main(String argv[]) {
        
        
        
        File f = new File(location+dir);
        
        Compress c = new Compress();
        try {
            //c.compressFile(f, dst);
            c.compressDIR(location,dir);
            
            
            /*
            try {
                
                String location = "CloudDIR/dir1/";
                BufferedInputStream origin = null;
                FileOutputStream dest = new FileOutputStream("data.zip");
                CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
                ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(checksum));
                //out.setMethod(ZipOutputStream.DEFLATED);
                byte data[] = new byte[BUFFER];
                // get a list of files from current directory
                File f = new File(location+".");
                String files[] = f.list();
                
                if (System.getSecurityManager() == null) {
                    System.setSecurityManager(new RMISecurityManager());
                }

                for (int i = 0; i < files.length; i++) {
                    System.out.println("Adding: " + files[i]);
                    FileInputStream fi = new FileInputStream(location+files[i]);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(files[i]);
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0,
                            BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
                out.close();
                System.out.println("checksum: " + checksum.getChecksum().getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
             * 
             */
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void compressFile(File src, String dst) throws Exception
    {
            BufferedInputStream origin = null;
            File res = new File(dst);
            FileOutputStream dest = new FileOutputStream(res);
            CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(checksum));

            byte data[] = new byte[BUFFER];
            
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new RMISecurityManager());
            }

            FileInputStream fi = new FileInputStream(src);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(src.getName());
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0,BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();            
            out.close();
            //System.out.println("Original Size of File "+src.length());
            //System.out.println("Compressed Size is "+res.length());
            //System.out.println("checksum: " + checksum.getChecksum().getValue());
        
    }
    
    public void compressDIR(String location,String dir)
    {
        try{
                //String location = dst;
            
                BufferedInputStream origin = null;
                File f2 = new File(location+dir+".zip");
                FileOutputStream dest = new FileOutputStream(f2);
                CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
                ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(checksum));
                //out.setMethod(ZipOutputStream.DEFLATED);
                byte data[] = new byte[BUFFER];
                // get a list of files from current directory
                //File f = new File(location);
                //String files[] = f.list();
                
                
                if (System.getSecurityManager() == null) {
                    System.setSecurityManager(new RMISecurityManager());
                }
                
                DirectoryListing dirlst = new DirectoryListing();
                ArrayList lst = dirlst.getDIRList(location+dir);
                

                int i=0;
                while(i<lst.size())
                {
                    String st = (String) lst.get(i++);
                    
                    File f1 = new File(st);
                    System.out.println("Adding: "+dir+"/"+f1.getName());
                    if(!f1.isDirectory())//its a file
                    {
                    FileInputStream fi = new FileInputStream(f1);
                    
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(dir+"/"+f1.getName());
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0,
                            BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                    }
                    else //its a directory
                    {
                        ZipEntry entry = new ZipEntry(f1.getName()+"/");
                        out.putNextEntry(entry);
                    }                        
                }                
                out.close();
                dest.close();
                //System.out.println("checksum: " + checksum.getChecksum().getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
}
