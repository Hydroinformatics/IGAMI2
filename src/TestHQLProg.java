
import igami2.DataBase.HQLQueryManager;

import igami2.DataBase.hibernateconfig.HdmarchiveChildren;
import igami2.DataBase.hibernateconfig.NewuserParamters;
import java.util.List;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author VIDYA
 */
public class TestHQLProg {
    HQLQueryManager HQLManager;
    String query = "";
    public TestHQLProg()
    {
        HQLManager = new HQLQueryManager();
    }
    
    void runCommand()
    {
        query = "from HdmarchiveChildren where userid=100 group by searchid";//HQL Query
        List resultList = HQLManager.executeHQLQuery(query);
        
        
        for(Object o : resultList) {
        HdmarchiveChildren userParam = (HdmarchiveChildren)o;
        
       System.out.println("Done");
    }
     //System.out.println("Done");   
    }
    
    //
    
    void saveData()
    {
        NewuserParamters userParam = new NewuserParamters();
        userParam.setGlobalSessionSize(0);
        userParam.setHdmGeneration(5);
        userParam.setHdmPopulationSize(40);
        userParam.setLearningType(0);
        userParam.setOptimizationType(1);
        userParam.setSdmGeneration(5);
        userParam.setSdmPopulationSize(120);
        userParam.setUserid(2);
        HQLManager.updateOneObject(userParam);        
        System.out.println("Done");
        
    }
    
    public static void main(String args[])
    {
        
        TestHQLProg ob = new TestHQLProg();
        ob.runCommand();
        //ob.saveData();
        
    }
}
