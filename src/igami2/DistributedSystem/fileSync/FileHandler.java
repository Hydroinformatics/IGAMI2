/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.fileSync;

import java.io.*;

/**
 *
 * @author VIDYA
 */
public class FileHandler {
    
    public static FileInputStream in;
    public static FileOutputStream out;
    String filename ="";

    public FileHandler() {
    }
    
    public boolean createFile(String name, byte[] buff) throws IOException {
        boolean res = false;
                File nfile = new File(name);
                out = new FileOutputStream(nfile);
                out.write(buff);
                out.close();
                return res;        
    }
    
    public void openFileReader(String name) throws FileNotFoundException
    {
        System.out.println("File Open for reading at "+name);
        File file = new File(name);
        in = new FileInputStream(file);
        
    }
    
    public void openFileWriter(String name) throws FileNotFoundException
    {
        System.out.println("File Open for writing at "+name);
        File file = new File(name);
        out = new FileOutputStream(file);        
    }
    
    public void closeFile() throws IOException
    {
        out.flush();
        out.close();        
    }
    
    public void closeFileReader() throws IOException
    {
        in.close();        
    }
    
    public void writeFile(byte[] buff) throws FileNotFoundException, IOException
    {
        out.write(buff);
        out.flush();
    }
    
    public byte[] readFile(int len) throws FileNotFoundException, IOException
    {
       byte[] buff = new byte[len];

       byte[] res=null;
       
      int l = in.read(buff);

      if(l==len)
      {
        res = new byte[len];
        res = buff;
      }
      else if(l>0)//rest of bytes
      {
          res = new byte[l];
          for(int i=0;i<l;i++)
          {
              res[i] = buff[i];
          }
      }
      if (res != null) {
                System.out.println("read bytes " + res.length);
            }
      return res;
    }
    
    public boolean createDir(String dirname)
    {
        boolean res = false;
        File f1 = new File(dirname);
        res = f1.mkdirs();
        return res;
    }
    
    public void browseDIR()
    {
        String dirname = "CloudDIR";
        File f1 = new File(dirname);
        if(f1.isDirectory())
        {
            System.out.println("Directory of "+ dirname);
            String s[] = f1.list();
            
            for(int i=0;i<s.length;i++)
            {
                File f = new File(dirname +"/"+s[i]);
                if(f.isDirectory())
                {
                    System.out.println(s[i]+ " is a directory");                    
                } else
                {
                    System.out.println(s[i]+ " is a file");
                }
            }        
        } else {
            System.out.println(dirname + " is not a directory");
        }
    }
}
