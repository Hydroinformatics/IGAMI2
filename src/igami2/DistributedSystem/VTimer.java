package igami2.DistributedSystem;


import java.util.Date;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author VIDYA
 */
public class VTimer {
    
    int day=0;
    int hr=0;
    int min=0;    
    int sec=0;
    int tsec=0;
    
    long strt=0;
    long end=0;
    
    Date dt;
    
    public VTimer()
    {
        
    }
    
    public void startTimer()
    {
         dt = new Date();
        strt = dt.getTime();        
    }
    
    public void endTimer()
    {
        dt = new Date();
        end = dt.getTime();        
        calculateTime();
    }
    
    public int getMin()
    {       
        
        return min;
    }
    
    public int getSec()
    {
        return sec;
    }
    
    public int getHour()
    {
        return hr;
    }
    
    public int totalSec()
    {
        return tsec;
    }
    
    public String getTimeMMSS()
    {
        String st = "MMSS="+min+":"+sec;
        return st;
    }
    
    public String getTimeHHMMSS()
    {
        String st = "HHMMSS="+hr+":"+min+":"+sec;
        return st;        
    }
    
    public String getTimeDDHHMMSS()
    {
        String st = "DDHHMMSS="+day+":"+hr+":"+min+":"+sec;
        return st;       
    }

    private void calculateTime() {
        tsec = (int) (end - strt)/1000;
        int tmp =tsec;
        day = tmp/86400;
        tmp = tmp%86400;
        hr = tmp/3600;
        tmp = tmp%3600;
        min = tmp/60;
        sec = tmp%60;        
    }
    
    public void printTime(String type)
    {
        if(type.compareToIgnoreCase("m")==0)
            System.out.println("Took Time "+this.getTimeMMSS());
        else if(type.compareToIgnoreCase("h")==0)
            System.out.println("Took Time "+this.getTimeHHMMSS());
        else if(type.compareToIgnoreCase("d")==0)
            System.out.println("Took Time "+this.getTimeDDHHMMSS());
        else
            System.out.println("Took Time Sec " + this.totalSec());
    }
    
    /*
     * Check if the current time is in between these time
     */
    
    public boolean checkTimeWithIn(String start, String end)
    {
        boolean res = false;
        dt = new Date();
        long currentTime = dt.getTime();
        
        return res;
    }
    
}
