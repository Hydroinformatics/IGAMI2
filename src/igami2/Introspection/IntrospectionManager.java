/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.Introspection;

import igami2.DataBase.DBManager;
import igami2.DataBase.IndividualDesign;
import igami2.DataBase.IndividualDesignManager;
import igami2.Vizualization.VizualizationManager;
import java.util.LinkedList;

/**
 *
 * @author VIDYA
 */
public class IntrospectionManager {
    
    VizualizationManager vm;
    DBManager dbm;
    IndividualDesignManager idm;
    
    public IntrospectionManager(IndividualDesignManager idm)
    {
        this.idm = idm;
        vm = new VizualizationManager(idm);
        this.dbm = idm.dbm;
    }
    
    
    
    //repeated after each optimization event
    public void doIntrospection(String type,LinkedList<IndividualDesign> resData,int global, int[]chosenFF)
    {
        //System.out.println("Doing Introspection");
        
        if(type.compareToIgnoreCase("iFistIntrospection")==0) //First Introspection
        {
            idm.doFirstIntrospectionCBM(resData);   
        }
        else if (type.compareToIgnoreCase("iSecondIntrospection")==0)
        {
            //LinkedList<IndividualDesign> restartData = dbm.getCBMIndv(idm.regionSubbasinId, idm.UserId);
            idm.doIntrospectionTotalBest();
            
        }
        else if(type.compareToIgnoreCase("iFinalIntrospection")==0) // interactive optimzation final introspection
        {
            LinkedList<IndividualDesign> restartData = dbm.getCBMIndv( idm.UserId,0,0, chosenFF);
            idm.doIntrospectionCBM(restartData,true);
            //idm.saveBestIndv(global_sessionId, sessionId, session_type_search);
            
        }
        //most recent SDM individuals 
        else if(type.compareToIgnoreCase("SDMfinalIntrospection")==0) //SDM Final introspection
        {
            LinkedList<IndividualDesign> restartData = dbm.getCBMIndv( idm.UserId,0,0, chosenFF);
            //modify to show only selected set of data instead of all
            
            idm.doLastIntrospectionCBM(restartData);
            //idm.saveBestIndv(global_sessionId, sessionId, session_type_search);
            
        }
    }
    
    
}

