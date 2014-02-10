package igami2.DataBase;


import java.io.*;
import java.sql.SQLException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author VIDYA
 * not used, can be used in future
 */
public class OutPutRCHFileSaver extends Thread{

     String base = "../SWAT/swat0/";
    // String base = "C:\\SWAT\\";
     String rchFileName = "output.rch";
     FileInputStream rchFIS;
     InputStreamReader rchISR;
     BufferedReader rchBR;
     DataInputStream rchDIS;
     String rchData;
     private  int counter;
     String [] data;
     DBQuery db;
     String table = "output_rch";
     int Uid;
     
     Thread t;
     
     public OutPutRCHFileSaver(String base, int Uid)//base location and userId of the individual
     {
         this.base = base;
         this.Uid = Uid;
         t = new Thread(this,"RCH"+Uid);
         t.start();         
     }

    public void run() 
    {
        try {
             db = new DBQuery();

            rchFIS = new FileInputStream(base + rchFileName);
            rchISR = new InputStreamReader(rchFIS);
            rchBR = new BufferedReader(rchISR);
            while (((rchData = rchBR.readLine()) != null)) 
            {//read entire contents of pond file in data variable
                if (rchData.contains("REACH")) {
                    counter++;
                }
                //System.out.println(pndData);
                if (counter > 1300) {
                    //System.out.println(pndData);
                    if (rchData.contains("REACH")) {
                       data = rchData.split("  "); 
                    }
                    System.out.println("Row"+counter+" :");
                    for(int i=0;i<data.length;i++)
                    {
                    //    System.out.print(" "+data[i]);
                    }
                    
                    saveToDB(counter,data);
                }
            }//while end
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private  void saveToDB(int rowid,String[] data) {
        String str= "";
        str = "INSERT INTO "+table+" VALUES('"+rowid+"','";
        String st="";
        int i=1;
        Double doubleNumber1 = new Double(0.0);
        
        for(;i<8;i++)
        {
             st = data[i];
            if((st.length()!=0))
            {
                if(st.charAt(0)==32)
                   st = st.substring(1);
                    //remove the first wihtespace
               str = str + st+"','"; 
            }  
        } 
            
        for(;i<data.length-1;i++)
        {
            st = data[i];
            doubleNumber1 = new Double(st);
            
            if(i>7)
            str = str + doubleNumber1.doubleValue()+"','";            
        }
        //last
        st = data[i];
            doubleNumber1 = new Double(st);
        str = str+ doubleNumber1.doubleValue() + "')";
        
        try {
            db.addRows(str);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        System.out.println(str);
    }
}
