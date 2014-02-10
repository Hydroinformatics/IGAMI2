/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.Vizualization;

import igami2.DataBase.IndividualDesign;
import igami2.DataBase.IndividualDesignManager;
import java.util.LinkedList;

/**
 *
 * @author VIDYA
 * This class can be used to show 3D visualization of the data
 */
public class VizualizationManager {
    
    LinkedList <IndividualDesign> indv;
    IndividualDesignManager idm;
    
    public VizualizationManager(IndividualDesignManager idm)
    {
        this.idm = idm;
        
    }
    public void disply()
    {
        System.out.println("Display Images");
    }
    
    public void takeFeedback()
    {
        
        System.out.println("Taking feedback");
        //idm.takeFeedback();        
        
    }
}
