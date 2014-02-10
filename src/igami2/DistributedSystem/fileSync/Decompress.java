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
import java.util.zip.*;

public class Decompress {

    static int BUFFER = 1024;

    public static void main(String argv[]) {
        
        String dst="C:/vidya/SWAT/";
        String src = "swat";
        
        File f = new File(src);
        
        Decompress d = new Decompress();
        try {
            //d.decompressFile(f, dst);
            d.decompressDIR(dst, src);
        /*
        try {
        
        String location = "CloudDIR/dir1/";
        String zfile = "data.zip";
        BufferedOutputStream dest = null;
        FileInputStream fis = new FileInputStream(zfile);
        CheckedInputStream checksum = new CheckedInputStream(fis, new Adler32());
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(checksum));
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
        System.out.println("Extracting: " + entry);
        int count;
        byte data[] = new byte[BUFFER];
        // write the files to the disk
        File f = new File(location+entry.getName());
        FileOutputStream fos = new FileOutputStream(f);
        dest = new BufferedOutputStream(fos,BUFFER);
        while ((count = zis.read(data, 0,BUFFER)) != -1) {
        dest.write(data, 0, count);
        }
        dest.flush();
        dest.close();
        }
        zis.close();
        System.out.println("Checksum: " + checksum.getChecksum().getValue());
        } catch (Exception e) {
        e.printStackTrace();
        }
         * 
         */
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void decompressFile(File src, String dst) throws Exception {

        BufferedOutputStream dest = null;
        
        FileInputStream fis = new FileInputStream(src);
        CheckedInputStream checksum = new CheckedInputStream(fis, new Adler32());
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(checksum));
        ZipEntry entry;

        if (System.getSecurityManager() == null) {
                System.setSecurityManager(new RMISecurityManager());
            }
        
        entry = zis.getNextEntry();
        System.out.println("Extracting: " + entry);
        int count;
        byte data[] = new byte[BUFFER];
        // write the files to the disk
        File f = new File(dst);
        FileOutputStream fos = new FileOutputStream(f);
        dest = new BufferedOutputStream(fos, BUFFER);
        while ((count = zis.read(data, 0, BUFFER)) != -1) {
            dest.write(data, 0, count);
        }
        dest.flush();
        dest.close();
        zis.close();
        checksum.close();
        fis.close();
        
        //System.out.println("Original Size of File "+src.length());
        //System.out.println("DeCompressed Size is "+f.length());
        //System.out.println("Checksum: " + checksum.getChecksum().getValue());

    }
    
    public void decompressDIR(String location,String dir)
    {
        try {
        
        String zfile = location+dir+".zip";//name of the zip file
        BufferedOutputStream dest = null;
        File f1 = new File(zfile);
        FileInputStream fis = new FileInputStream(f1);
        CheckedInputStream checksum = new CheckedInputStream(fis, new Adler32());
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(checksum));
        ZipEntry entry;
        File f2 = new File(location+dir);

        if(!f2.exists())
            f2.mkdirs();
        
        while ((entry = zis.getNextEntry()) != null) {
        int count;
        byte data[] = new byte[BUFFER];
        // write the files to the disk
 
        File f = new File(location+entry.getName());
        if(!entry.isDirectory())
        {        
        FileOutputStream fos = new FileOutputStream(f);
        dest = new BufferedOutputStream(fos,BUFFER);
        while ((count = zis.read(data, 0,BUFFER)) != -1) {
        dest.write(data, 0, count);
        }
        dest.flush();
        dest.close();
        }
        else//its a directory
        {
            f.mkdir();
        }
        System.out.println("Extracted: "+entry.getName());
        
        }
        zis.close();
        fis.close();
        
        //System.out.println("Checksum: " + checksum.getChecksum().getValue());
        } catch (Exception e) {
        e.printStackTrace();
        }
    }
}
