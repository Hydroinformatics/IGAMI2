/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2;

import java.io.Serializable;

/**
 *
 * @author VIDYA
 */
public class UserData implements Serializable{
    public int userId; 
    public int watershedId;

    public int [] chosenFF = new int[] {1,1,1,1,1,0,1};
    String host = "";
    public int[] chosenBMP = new int[] {1,1,1,1,1,1,0,1,1,0};
    public int[] farm_own;
    public int[] farm_crop;
    public int[] farm_cash;
    
    public String FName = "";
    public String LName = "";
    public String email ="";
    public String gender = "";
    public int[] localSubbasin;
    //public User usr=null;
    
    
    public UserData()
    {
        
    }
    public UserData(int userId,int[] chosenFF, String host)
    {
        this.userId = userId;
        this.chosenFF = chosenFF;
        this.host = host;        
    }  
    
    public UserData(int userId, int watershedId, int[] chosenBMP, int[] chosenFF)
    {
        this.userId = userId;
        this.chosenFF = chosenFF;
        this.chosenBMP = chosenBMP;  
        this.watershedId = watershedId;
      
    }
    
    public UserData(int[] farm1, int[] farm2, int[] farm3)
    {     
        this.farm_own = farm1;
        this.farm_crop = farm2;
        this.farm_cash = farm3;
    }
    
}
