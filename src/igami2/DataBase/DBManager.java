/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DataBase;


import igami2.DataBase.hibernateconfig.*;
import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import igami2.UserData;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author VIDYA
 */
public class DBManager {

    private ResultSet rs;
    String base = "";
    int ffCount = 6; //total number of fitness functions
    DBQuery db; //use to query MySql Database through JDBC
    private HQLQueryManager HQLManager; //use the query MySql Database through Hibernate Framework
    int UserId = 0;
    public int searchId = 0;
    public int[] chosenFF;
    private int NULL = 0;
    private int omittedSub = 3;
    private int subbasinUsed = 127;
    private ResultSet rs1;
    
    //different table names
    private String sdm = "SDM";
    private String cbm = "cbm";
    private String hdmarchivelog = "hdmarchive";
    private String hdmarchive = "hdmarchive_children";
    private String hdmarchiveNonDominated = "hdmarchive_nondominated";
    private String fitnessf = "F";//with id 0 to 5
    private String userstats = "USERSTATS";
    private String sdm_data = "SDM_DATA";
    private String takefeedback = "TAKEFEEDBACK";
    private String users_feedback = "USERS_FEEDBACK";
    private String master_event = "IGAMI2_EVENT_MASTER";
    private String server_event = "IGAMI2_EVENT_SERVER";
    private String newuser = "Newuser";
    private String abort_table = "abort_search";
    private String kendalstats = "KENDALLSTATS";
    private String users = "USERS";
    private String cbm_initial = "cbm_initial";
    private String cbm_initial_f = "cbm_initial_f";
    private String session_info = "session_info"; 
    private String users_activity = "users_activity";
    private String abortuserthread = "abortuserthread";
    private String sdm_modelling_data = "sdm_modelling_data";
    private String sdm_modelling_data_RandomData = "sdm_modelling_data_RandomData";
    private String sdm_modelling_data_RandomDataCumNew = "sdm_modelling_data_RandomDataCumNew";
    private String users_feedback_timing = "users_feedback_timing";
    private String userstats_feedback_timing = "userstats_feedback_timing";
    private String biasindvdata = "biasindvdata";
    private String supercomputer_onhold = "supercomputer_onhold";
    private String kendallstats_user_data = "kendallstats_user_data";
    private String introspection_individuals = "introspection_individuals";
    private String computercluster_info = "computercluster_info";

    public DBManager() {
        db = new DBQuery();
        HQLManager = new HQLQueryManager();
    }

    public DBManager(int UserId, int[] chosenFF) {
        this.UserId = UserId;
        db = new DBQuery();
        searchId = getSearchId();
        this.chosenFF = chosenFF;
        HQLManager = new HQLQueryManager();
    }
    
    public HQLQueryManager getHQLManager()
    {
        return this.HQLManager;
    }
    
    /*
     * Save data to CBM
     */

    public void saveCBM(int userId, LinkedList<Individual> Indvs,int feedbackId, int global_sessionId, int local_sessionId, int sessionId, int session_type) {
        //System.out.println("Saving to CBM");
        String str = "";

        for (int i = 0; i < Indvs.size(); i++) {
            Individual in = Indvs.get(i);
            str = "INSERT INTO " + cbm + " VALUES('" + getIndvSaveStmt(userId,in,feedbackId,i, global_sessionId, local_sessionId, sessionId, session_type) + "')";;
            try {
                try {
                    db.addRows(str);
                } catch (com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException e) {
                    System.out.println("Indv Already Exist " + in.IndvId);
                    e.printStackTrace();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /*
     * Save Data to HdmArchinve children
     */

    public void saveArchive(int userId,LinkedList<Individual> Indvs, LinkedList<Individual> NondominatedIndvs, int feedbackId, int global_sessionId, int local_sessionId, int sessionId, int session_type) {
        //System.out.println("Saving to Archive");
        String str = "";
        for (int i = 0; i < Indvs.size(); i++) {
            Individual in = Indvs.get(i);
            str = "INSERT INTO " + hdmarchive + " VALUES('" + getIndvSaveStmt(userId,in,feedbackId,i, global_sessionId, local_sessionId, sessionId, session_type) + "')";;
            try {
                db.addRows(str);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }


        this.chosenFF = Indvs.get(0).chosenFF;
        
        for (int j = 0; j < ffCount; j++) {
            if (chosenFF[j] == 1) {
                for (int i = 0; i < Indvs.size(); i++) {
                    Individual in = Indvs.get(i);
                    double[][] subbasinFF = in.subbasinsFF;
                    String str1 = "INSERT INTO " + fitnessf + j + " VALUES('" + in.UserId + "','" + searchId + "','" + in.IndvId + "'";
                    //int noOfSubbasins = in.subbasinsFF.length; //108


                    int c = 0;
                    for (int k = 0; k <  subbasinFF.length+omittedSub; k++) //for 130 subbasins
                    {

                        if (c < subbasinFF.length) //only 127 selected subbasins
                        {
                            double val = subbasinFF[c][j];
                            str1 = str1 + ",'" + val + "'";
                            c++;
                        } else {
                            str1 = str1 + ",'" + NULL + "'";
                        }
                    }
                    str1 = str1 + ")";
                    try {
                        db.addRows(str1);
                    } catch (SQLException ex) {
                        //System.out.println("Got Exception for "+in.IndvId);
                        //Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        
        //save the non-dominated Individuals
        if(NondominatedIndvs!=null)
        {
        for (int i = 0; i < NondominatedIndvs.size(); i++) {
            Individual in = NondominatedIndvs.get(i);
            str = "INSERT INTO " + this.hdmarchiveNonDominated + " VALUES('" + in.UserId + "','"
                + searchId + "','"                
                + global_sessionId + "','"
                + local_sessionId + "','"
                + sessionId + "','"
                + session_type + "','"
                + in.IndvId + "','"
                + in.rating + "','"
                + in.confidence + "')";
            
            try {
                db.addRows(str);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        }
        //System.out.println("Column Length is"+len);
    }
  
    private String getIndvSaveStmt(int userId,Individual in,int feedbackId, int i, int global_sessionId, int local_sessionId, int sessionId, int session_type) {

        String str = "";
        str = userId + "','"
                + searchId + "','"
                + feedbackId + "','"
                + global_sessionId + "','"
                + local_sessionId + "','"
                + sessionId + "','"
                + session_type + "','"
                + i + "','"
                + in.IndvId + "','"               
                + in.rating + "','"
                + in.confidence + "','"
                + getstrInt(in.chosenFF) + "','"
                + getstrInt(in.regionSubbasinId) + "','"
                + getstrInt(in.chosenBMPs) + "','"
                + getstrDouble(in.assignments) + "','"
                + in.fitnessValues[0] + "','"
                + in.fitnessValues[1] + "','"
                + in.fitnessValues[2] + "','"
                + in.fitnessValues[3] + "','"
                + in.fitnessValues[4] + "','"
                + in.fitnessValues[5];
        return str;
    }

    public void saveUserData(int Uid, String type, String date) {
        String str = "";
        if (type.compareToIgnoreCase("BEGIN") == 0) {
            System.out.println("\nA new User is Added into the system with id" + Uid + " at Time: " + date);
            str = "INSERT INTO " + userstats + " VALUES('"
                    + Uid + "','"
                    + searchId + "','"
                    + date + "',NULL,NULL)";
        } else if (type.compareToIgnoreCase("FINISHED") == 0) {
            str = "UPDATE " + userstats + " SET SEARCH_FINISH='" + date + "' WHERE USERID ='" + Uid + "' AND SEARCHID='" + searchId + "'";
            System.out.println("Search Completed for User " + this.UserId + " at Time: " + date);
        } else if (type.compareToIgnoreCase("ABORT") == 0) {
            str = "UPDATE " + userstats + " SET SEARCH_ABORT='" + date + "' WHERE USERID ='" + Uid + "' AND SEARCHID='" + searchId + "'";
            System.out.println("Search Aborted for User " + this.UserId + " at Time: " + date);
        }
        
        System.out.println(str);

        try {
            db.addRows(str);
        } catch (SQLException ex) {
            //ex.printStackTrace();
            //exception means Inset failed as the user's previous search wasn't started, so just update the search values as new and discard the previous
            str = "UPDATE " + userstats + " SET SEARCH_BEGIN='" + date + "', SEARCH_ABORT=NULL WHERE USERID ='" + Uid + "' AND SEARCHID='" + searchId + "'";     //       
            try {
            db.addRows(str);
        } catch (SQLException e) {
            e.printStackTrace();
        }
            
        }
    }

    public void saveUserFeedbackTiming(int Uid, int feedbackId, int global_sessionId, int local_sessionId, int sessionId, int feedback_type, LinkedList data) {
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++) {
                String strx = (String) data.get(i);
                String[] strAr = strx.split(",");
                String event = strAr[0];
                String eventTime = strAr[1]; //can be transformed to another date time format
                int pageno = Integer.parseInt(strAr[2]);
                int id = Integer.parseInt(strAr[3]);
                String indvid = strAr[4];


                String str = "INSERT INTO " + userstats_feedback_timing + " VALUES('"
                        + Uid + "','"
                        + searchId + "','"
                        + feedbackId + "','"
                        + global_sessionId + "','"
                        + local_sessionId + "','"
                        + sessionId + "','"
                        + feedback_type + "','"
                        + id + "','"
                        + pageno + "','"
                        + event + "','"
                        + eventTime + "','"
                        + indvid
                        + "')";
                //System.out.println("Saved UserData "+str);    
                try {
                    db.addRows(str);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
  

    public void saveKendallStats(int Uid, int global_sessionId, int local_sessionId, double S, double Z, int hdm) {
        String str = "INSERT INTO " + kendalstats + " VALUES('"
                + Uid + "','"
                + searchId + "','"
                + global_sessionId + "','"
                + local_sessionId + "','"
                + S + "','"
                + Z + "','"
                + hdm + "')"; //1 if Human Selected
        //System.out.println("Saved UserData "+str);    
        try {
            db.addRows(str);
        } catch (SQLException ex) {
            System.out.println("MySQLIntegrityConstraintViolationException: Duplicate entry ");    
            //ex.printStackTrace();
        }
    }
    
    public void saveUserStatsKendall(int global_sessionId, int local_sessionId, int hdmGenerationId, double mean, double stdDeviation) {
        String str = "INSERT INTO " + kendallstats_user_data + " VALUES('"
                + this.UserId + "','"
                + searchId + "','"
                + global_sessionId + "','"
                + local_sessionId + "','"
                + hdmGenerationId + "','"
                + mean + "','"
                + stdDeviation + "')"; //1 if Human Selected
        //System.out.println("Saved UserData "+str);    
        try {
            db.addRows(str);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public LinkedList<double[]> loadKendallStats(int global_sessionId) {
        LinkedList<double[]> res = new LinkedList<double[]>();
        
        String str = "SELECT * FROM "+kendalstats+" WHERE USERID="+ this.UserId +" AND SEARCHID= "+ this.searchId + " AND GLOBAL_SESSIONID="+global_sessionId;
        //System.out.println("Saved UserData "+str);    
        try {
            this.rs = db.getRSStmt(str);
            while(rs.next())
            {
                double[] ob = new double[2];
                ob[0] = rs.getDouble("S");
                ob[1] = rs.getDouble("Z");
                res.add(ob);
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return res;
    }
    
    public LinkedList<Double> loadUserStatsKendall(int global_sessionId, int local_sessionId, int hdmGenerationId) {
        LinkedList<Double> res = new LinkedList<Double>();        
        String str = "SELECT * FROM "+kendallstats_user_data+" WHERE USERID="+ this.UserId +" AND SEARCHID= "+ this.searchId + " AND GLOBAL_SESSIONID="+global_sessionId+ " AND LOCAL_SESSIONID="+local_sessionId;       
        try {
           this.rs = db.getRSStmt(str);
           while(rs.next())
           {
              Double std = rs.getDouble("STD_DEVIATION");
              res.add(std);
           }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return res;
    }


    private String getstrInt(int[] dat) {
        String res = "";

        for (int i = 0; i < dat.length; i++) {
            res = res + dat[i] + ",";
        }
        //System.out.println("len "+res.length()+" data"+res);
        res = res.substring(0, res.length() - 1);
        return res;
    }

    private String getstrDouble(double[] dat) {
        String res = "";
        for (int i = 0; i < dat.length; i++) {
            //if(dat[i]==1)
            //res = res+regionSubbasinId[i]+",";
            res = res + dat[i] + ",";
        }
        res = res.substring(0, res.length() - 1);
        //System.out.println("len "+res.length()+" data "+res);

        return res;
    }
    
    private int[] getIntStr(String str) {
        int[] res = null;

        String[] buf = str.split(",");
        res = new int[buf.length];
        
        for (int i = 0; i < res.length; i++) {
           res[i] = Integer.parseInt(buf[i]); 
        }
        return res;
    }

    private double[] getDoubleStr(String str) {
        double[] res = null;

        String[] buf = str.split(",");
        res = new double[buf.length];
        
        for (int i = 0; i < res.length; i++) {
           res[i] = Double.parseDouble(buf[i]); 
        }
        

        return res;
    }
    /*
     * Get All the case based individuals for the User
     */
    public LinkedList<IndividualDesign> getCBMIndv(int Uid, int type, int global, int[] chosenFF) {

        LinkedList<IndividualDesign> res = new LinkedList();

        //load individual ffs

        String str = "";
        if (type == 1) //SDM based CBM needed
        {
            str = "SELECT *FROM " + cbm + " WHERE USERID='" + Uid + "' AND SEARCHID='" + searchId + "' AND GLOBAL_SESSIONID='" + global + "' AND SESSION_TYPE='" + type + "'";

        } else //All CBM needed
        {
            str = "SELECT *FROM " + cbm + " WHERE USERID='" + Uid + "' AND SEARCHID='" + searchId + "'";
        }
        //System.out.println("Saved UserData "+str);
        try {
            rs = db.getRSStmt(str);
            while (rs.next()) {
                IndividualDesign in = new IndividualDesign();
                String assignments = rs.getString("ASSIGNMENTS");
                double[] ffvalues = new double[ffCount + 1];

                for (int i = 0; i < ffCount; i++) {
                    ffvalues[i] = rs.getDouble(fitnessf + i);
                }
                ffvalues[ffCount] = 0; //last ff is init 0
                in.IndvId = rs.getInt("INDVID");
                in.rating = rs.getInt("RATING");
                in.confidence = rs.getInt("CONFIDENCE");

                String buf[] = assignments.split(",");
                double[] assign = new double[buf.length];
                for (int i = 0; i < buf.length; i++) {
                    assign[i] = Double.parseDouble(buf[i]);
                }
                in.assignments = assign;
                in.fitnessValues = ffvalues;

                     
                double[][] subbasinFF = new double[subbasinUsed][chosenFF.length];
                for (int i = 0; i < ffCount; i++) {
                    if (chosenFF[i] == 1) {

                        String str1 = "SELECT *FROM " + fitnessf + i + " WHERE  USERID ='" + Uid + "' AND SEARCHID='" + this.searchId + "' AND INDVID ='" + in.IndvId + "'";
                        //System.out.println(str1);
                        
                        try {
                            rs1 = db.getRSStmt(str1);
                            if (rs1.first()) {
                                int j = 0;
                                for (int k = 0; k < subbasinUsed; k++) //for 1 subbasins
                                {
                                        double val = rs1.getDouble("S" + k);
                                        subbasinFF[j][i] = val;
                                        j++;
                                }

                            }

                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                in.subbasinsFF = subbasinFF;
                res.add(in);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();

        }

        //add the rs to res

        return res;
    }

    public void saveSDM(int UserId, int SDMCount, Object currentNNM, Object currentNNMExtended, Object cumNNM, Object cumNNMExtended, Object currentANFIS, int[] currentLinearModelErrors) {
        
        String str = "INSERT INTO " + sdm + " VALUES('"
                + UserId + "','"
                + SDMCount + "','"
                + currentNNM + "','"
                + currentNNMExtended + "','"
                + cumNNM + "','"
                + cumNNMExtended + "','"
                + currentANFIS + "','"
                + getstrInt(currentLinearModelErrors)       
                + "')";

        //System.out.println("Saved UserData " + str);
        try {
            db.addRows(str);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public LinkedList<Object[]> getSDM() {
        LinkedList<Object[]> res = new LinkedList<Object[]>();
        String str = "SELECT * FROM " + sdm + " WHERE USERID="+this.UserId;
        try {
            this.rs = db.getRSStmt(str);
            while(rs.next())
            {
                Object[] NNLst = new Object[5]; //4 NNs and 1 ANFIS, no need of Linear Models
                NNLst[0] = rs.getObject("NNM");
                NNLst[1] = rs.getObject("NNM_EXT");
                NNLst[2] = rs.getObject("NNM_CUM");
                NNLst[3] = rs.getObject("NNM_CUM_EXT");
                NNLst[4] = rs.getObject("ANFIS");
                res.add(NNLst);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return res;
    }


    
    public void saveSDMData(int UserId,int global_sessionId, int SDMCount, int NNwin, double minError, double[] minErrorNNVals, double[] minErrorNNID, double linearModelMinError, int currentLinearId, int[] linearModelErrors, double minANFIS, int minANFISID, double[][] range) {

        String stRange = "";
        if (range.length > 0) {
            stRange = getstrDouble(range[0]);
            for (int j = 1; j < range.length; j++) {
                stRange = stRange + "," + getstrDouble(range[j]);
            }
        }
        
        String str = "INSERT INTO " + sdm_data + " VALUES('"
                + UserId + "','"
                + this.searchId + "','"
                + global_sessionId+ "','"
                + SDMCount + "','"
                + NNwin + "','"
                + minError + "','"
                + getstrDouble(minErrorNNVals) + "','"
                + getstrDouble(minErrorNNID) + "','"
                + linearModelMinError + "','"
                + currentLinearId + "','"
                + getstrInt(linearModelErrors) + "','"
                + minANFIS + "','"
                + minANFISID + "','"
                +stRange 
                + "')";
        System.out.println("Saved UserData "+str);    
        try {
            db.addRows(str);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

   
    void updateCBM(LinkedList<Individual> res, int global_sessionId, int local_sessionId, int sessionId, int session_type) {
        for (int i = 0; i < res.size(); i++) {
            Individual in = res.get(i);
            String str = "UPDATE " + cbm + " SET RATING='" + in.rating + "', CONFIDENCE='" + in.confidence + "' WHERE USERID='" + in.UserId + "' AND SEARCHID='" + searchId + "' AND INDVID='" + in.IndvId + "'";
            //System.out.println(str);
            try {
                db.update(str);
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public LinkedList<UserData> getNewUser() {
        LinkedList<UserData> usr = new LinkedList();
        String str = "SELECT * FROM " + newuser;
        try {
            rs = db.getRSStmt(str);
            while (rs.next()) {
                int uid = rs.getInt("USERID");
                String bmp = rs.getString("BMP");
                String ff = rs.getString("OBJECTIVE_FUN");
                String[] bmpTmp = bmp.split(",");
                int[] chosenBMP = new int[bmpTmp.length];
                System.out.println("Chosen BMP is ");
                for (int i = 0; i < chosenBMP.length; i++) {
                    chosenBMP[i] = Integer.parseInt(bmpTmp[i]);
                    System.out.print(chosenBMP[i]);
                }
                String[] ffTmp = ff.split(",");
                int[] chosenFF = new int[ffTmp.length];
                for (int i = 0; i < chosenFF.length; i++) {
                    chosenFF[i] = Integer.parseInt(ffTmp[i]);
                }
                usr.add(new UserData(uid, chosenBMP, chosenFF));
                deleteFromTableUserLevel(newuser, uid);
            }
        } catch (SQLException ex) {
            //System.out.println("Got Exception");
            ex.printStackTrace();
        }
        return usr;
    }

    public UserData getUserPersonalData(int UserId) {
        UserData res = null;

        String str = "SELECT * FROM " + users + "WHERE USERID = " + UserId;

        try {
            rs = db.getRSStmt(str);

            if (rs.first()) {
                String farm_own = rs.getString("SUBBASIN_OWN_FARM");
                String farm_crop = rs.getString("SUBBASIN_FARM_CROP");
                String farm_cash = rs.getString("SUBBASIN_FARM_CASH");

                String[] Farm_ownTmp = farm_own.split(",");
                int[] Farm_own = new int[Farm_ownTmp.length];

                for (int i = 0; i < Farm_own.length; i++) {
                    Farm_own[i] = Integer.parseInt(Farm_ownTmp[i]);
                }

                String[] Farm_cropTmp = farm_crop.split(",");
                int[] Farm_crop = new int[Farm_cropTmp.length];

                for (int i = 0; i < Farm_crop.length; i++) {
                    Farm_crop[i] = Integer.parseInt(Farm_cropTmp[i]);
                }

                String[] Farm_cashTmp = farm_cash.split(",");
                int[] Farm_cash = new int[Farm_cashTmp.length];

                for (int i = 0; i < Farm_cash.length; i++) {
                    Farm_cash[i] = Integer.parseInt(Farm_cashTmp[i]);
                }

                res = new UserData(Farm_own, Farm_crop, Farm_cash);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return res;
    }

    /*
     * Use to Insert the data for feedback in the takefeedback table
     */
    public void putFeedbackData(LinkedList<Individual> indv) {
        int[] regionSubbasinId = indv.get(0).regionSubbasinId;
        int[] chosenFF = indv.get(0).chosenFF;
        for (int i = 0; i < indv.size(); i++) {
            Individual in = indv.get(i);            
            String[] sbbasinFFs = new String[130];
            int j = 0;
            double [][] subBasinFFs = in.subbasinsFF;
            for (int k = 0; k < subBasinFFs.length+omittedSub; k++) //for 127 subbasins
            {
                String st = "";

                if (k<subBasinFFs.length) //only selected subbasins
                {
                    for (int m = 0; m < chosenFF.length - 1; m++) {
                        if (chosenFF[m] == 1) {
                            double val = subBasinFFs[k][m];
                            if (m == 0) {
                                val = val * 2.47;
                            } else if (m == 1) {
                                val = val * 35.31;
                            }

                            st = st + val + ",";
                        } else {
                            st = st + 0 + ","; //put zero if ff is not used
                        }
                    }
                    st = st.substring(0, st.length() - 1); //remove the last comma
                    j++;
                }
                sbbasinFFs[k] = st;
            }

            ///////////////////
            // Added by Meghna Babbar-Sebens, to convert the assignment array values into english units, Oct 19th 2012.
            double[] assignments_EngUnits = new double[in.assignments.length];
            for (int k = 0; k < in.assignments.length; k++) {
                assignments_EngUnits[k] = in.assignments[k];
            }
            // identify if filterstrips are true, and if yes then find the starting location of filter width in assignments array.
            int filterstripwidth_loc = -99;
            if (in.chosenBMPs[3] == 1) {
                filterstripwidth_loc = 0;
                for (int l = 0; l < 3; l++) {
                    if (in.chosenBMPs[l] == 1) {
                        filterstripwidth_loc++;
                    }
                }
                // Now convert the values of filter widths to feet from the default units of meters
                for (int k = in.regionSubbasinId.length * filterstripwidth_loc; k < in.regionSubbasinId.length * (filterstripwidth_loc + 1); k++) {
                    assignments_EngUnits[k] = this.truncateDouble(assignments_EngUnits[k] * 3.28, 2); // convert meters into feet
                }
            }


            // identify if wetlands are true, and if yes then find the starting location of wetland area in assignments array.
            int wetlandArea_loc = -99;
            if (in.chosenBMPs[7] == 1) {
                wetlandArea_loc = 0;
                for (int l = 0; l < 7; l++) {
                    if (in.chosenBMPs[l] == 1) {
                        wetlandArea_loc++;
                    }
                }
                // Now convert the values of wetland area to acres from the default units of meters^2.
                for (int k = in.regionSubbasinId.length * wetlandArea_loc; k < in.regionSubbasinId.length * (wetlandArea_loc + 1); k++) {
                    assignments_EngUnits[k] = this.truncateDouble(assignments_EngUnits[k] * 2.47, 2); // convert hectares into acres
                }
            }


            String str = "INSERT INTO " + takefeedback + " VALUES('";
            str = str + in.UserId + "','"
                    //+ in.IndvId + "','"
                    + i + "','" //use the location of the individual
                    + in.rating + "','"
                    + in.confidence + "','"
                    + getstrInt(in.chosenFF) + "','"
                    + getstrInt(in.regionSubbasinId) + "','"
                    + getstrInt(in.chosenBMPs) + "','"
                    + getstrDouble(assignments_EngUnits) + "','" //Added by Meghna Babbar-Sebens, Oct 19th 2012.
                    //+ getstrDouble(in.assignments) + "','" //Commented by Meghna-Babbar-Sebens, Oct 19th 2012.
                    + in.fitnessValues[0] * 2.47 + "','" // Meghna Babbar-Sebens, a factor of 2.47 included to convert hectares into acres. Modified on Oct 19th 2012.
                    + in.fitnessValues[1] * 35.31 + "','" // Meghna Babbar-Sebens, a factor of 35.31 included to convert hectares into acres. Modified on Oct 19th 2012.
                    + in.fitnessValues[2] + "','"
                    + in.fitnessValues[3] + "','"
                    + in.fitnessValues[4] + "','"
                    + in.fitnessValues[5]
                    + getSubbasinFFStr(sbbasinFFs)
                    + "')";
            //System.out.println(str);
            try {
                db.addRows(str);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        //notify the WebServer to proceed
        setWebServerEvent(this.UserId);
    }
    
    public void putFeedbackDataAllUsers(LinkedList<Individual> indv, int[] userIds) {
        int[] regionSubbasinId = indv.get(0).regionSubbasinId;
        int[] chosenFF = indv.get(0).chosenFF;
        for (int i = 0; i < indv.size(); i++) {
            Individual in = indv.get(i);            
            String[] sbbasinFFs = new String[130];
            int j = 0;
            double [][] subBasinFFs = in.subbasinsFF;
            for (int k = 0; k < subBasinFFs.length+omittedSub; k++) //for 127 subbasins
            {
                String st = "";

                if (k<subBasinFFs.length) //only selected subbasins
                {
                    for (int m = 0; m < chosenFF.length - 1; m++) {
                        if (chosenFF[m] == 1) {
                            double val = subBasinFFs[k][m];
                            if (m == 0) {
                                val = val * 2.47;
                            } else if (m == 1) {
                                val = val * 35.31;
                            }

                            st = st + val + ",";
                        } else {
                            st = st + 0 + ","; //put zero if ff is not used
                        }
                    }
                    st = st.substring(0, st.length() - 1); //remove the last comma
                    j++;
                }
                sbbasinFFs[k] = st;
            }

            ///////////////////
            // Added by Meghna Babbar-Sebens, to convert the assignment array values into english units, Oct 19th 2012.
            double[] assignments_EngUnits = new double[in.assignments.length];
            for (int k = 0; k < in.assignments.length; k++) {
                assignments_EngUnits[k] = in.assignments[k];
            }
            // identify if filterstrips are true, and if yes then find the starting location of filter width in assignments array.
            int filterstripwidth_loc = -99;
            if (in.chosenBMPs[3] == 1) {
                filterstripwidth_loc = 0;
                for (int l = 0; l < 3; l++) {
                    if (in.chosenBMPs[l] == 1) {
                        filterstripwidth_loc++;
                    }
                }
                // Now convert the values of filter widths to feet from the default units of meters
                for (int k = in.regionSubbasinId.length * filterstripwidth_loc; k < in.regionSubbasinId.length * (filterstripwidth_loc + 1); k++) {
                    assignments_EngUnits[k] = this.truncateDouble(assignments_EngUnits[k] * 3.28, 2); // convert meters into feet
                }
            }


            // identify if wetlands are true, and if yes then find the starting location of wetland area in assignments array.
            int wetlandArea_loc = -99;
            if (in.chosenBMPs[7] == 1) {
                wetlandArea_loc = 0;
                for (int l = 0; l < 7; l++) {
                    if (in.chosenBMPs[l] == 1) {
                        wetlandArea_loc++;
                    }
                }
                // Now convert the values of wetland area to acres from the default units of meters^2.
                for (int k = in.regionSubbasinId.length * wetlandArea_loc; k < in.regionSubbasinId.length * (wetlandArea_loc + 1); k++) {
                    assignments_EngUnits[k] = this.truncateDouble(assignments_EngUnits[k] * 2.47, 2); // convert hectares into acres
                }
            }


            //INSERT the values to all users
            for (int n = 0; n < userIds.length; n++) {
                String str = "INSERT INTO " + takefeedback + " VALUES('";
                str = str + userIds[n] + "','"
                        //+ in.IndvId + "','"
                        + i + "','" //use the location of the individual
                        + in.rating + "','"
                        + in.confidence + "','"
                        + getstrInt(in.chosenFF) + "','"
                        + getstrInt(in.regionSubbasinId) + "','"
                        + getstrInt(in.chosenBMPs) + "','"
                        + getstrDouble(assignments_EngUnits) + "','" //Added by Meghna Babbar-Sebens, Oct 19th 2012.
                        //+ getstrDouble(in.assignments) + "','" //Commented by Meghna-Babbar-Sebens, Oct 19th 2012.
                        + in.fitnessValues[0] * 2.47 + "','" // Meghna Babbar-Sebens, a factor of 2.47 included to convert hectares into acres. Modified on Oct 19th 2012.
                        + in.fitnessValues[1] * 35.31 + "','" // Meghna Babbar-Sebens, a factor of 35.31 included to convert hectares into acres. Modified on Oct 19th 2012.
                        + in.fitnessValues[2] + "','"
                        + in.fitnessValues[3] + "','"
                        + in.fitnessValues[4] + "','"
                        + in.fitnessValues[5]
                        + getSubbasinFFStr(sbbasinFFs)
                        + "')";
                //System.out.println(str);
                try {
                    db.addRows(str);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

        //notify the WebServer to proceed
        for(int k=0;k<userIds.length;k++)
        {
            setWebServerEvent(userIds[k]);
        }
    }

    /*
     * This method truncates a double "number" to a double with "numDigits"
     * after decimal point. Added by Meghna Babbar-Sebens, Oct 25th 2012.
     */
    double truncateDouble(double number, int numDigits) {
        double result = number;
        String arg = "" + number;
        int idx = arg.indexOf('.');
        if (idx != -1) {
            if (arg.length() > idx + numDigits) {
                arg = arg.substring(0, idx + numDigits + 1);
                result = Double.parseDouble(arg);
            }
        }
        return result;
    }

    /*
     * Use to get back the feedback data from the users_feedback table After
     * reading it, delete all the entries from the table for this user
     *
     * returns only IndvId, Rating and Confidence values
     */
    public LinkedList<IndividualDesign> getFeedbackData(int userId) {
        LinkedList<IndividualDesign> res = new LinkedList<IndividualDesign>();
        //read the ResultSet
        //String str = "SELECT ASSIGNMENTS FROM CBM WHERE USERID=1";
        String str = "SELECT *FROM " + users_feedback + " WHERE USERID='" + userId + "' ORDER BY INDVID ASC";
        //System.out.println("Saved UserData "+str);
        try {
            rs = db.getRSStmt(str);
            while (rs.next()) {
                IndividualDesign in = new IndividualDesign();

                in.IndvId = rs.getInt("INDVID");
                in.rating = rs.getInt("RATING");
                in.confidence = rs.getDouble("CONFIDENCE");
                res.add(in);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();

        }

        //Delete the entries
        deleteFromTableUserLevel(users_feedback, userId);


        return res;
    }
    
    public LinkedList getFeedbackEventTimeData(LinkedList<Integer> IndvIds, int userId) {
         LinkedList res = new LinkedList();
        
        //read the ResultSet
        //String str = "SELECT ASSIGNMENTS FROM CBM WHERE USERID=1";
        String str = "SELECT *FROM " + users_feedback_timing + " WHERE USERID='" + userId + "'";
        //System.out.println("Saved UserData "+str);
        
        int maxSize = IndvIds.size();
        try {
            rs = db.getRSStmt(str);            
            while (rs.next()) {                
                String ID = rs.getString("ID");
                String PAGE = rs.getString("PAGE");               
                int pageNo = Integer.parseInt(PAGE);
                String EVENT = rs.getString("EVENT");
                Integer IndvId = -1;//save -1 as default value
                int IndvIdPos=-1;
                if(EVENT!=null)
                {
                    if (EVENT.compareToIgnoreCase("Map1") == 0 || EVENT.compareToIgnoreCase("rating1") == 0 || EVENT.compareToIgnoreCase("slider1") == 0) //first Indv
                    {
                        IndvIdPos = (pageNo - 1) * 2;
                        if(IndvIdPos<=maxSize)
                            IndvId = IndvIds.get(IndvIdPos);
                    } else if (EVENT.compareToIgnoreCase("Map2") == 0 || EVENT.compareToIgnoreCase("rating2") == 0 || EVENT.compareToIgnoreCase("slider2") == 0) {
                        IndvIdPos = (pageNo - 1) * 2 + 1;
                        if(IndvIdPos<=maxSize)
                            IndvId = IndvIds.get(IndvIdPos);
                    }
                }
                    
                String EVENTTIME = rs.getString("EVENTTIME");   
                
                String strx = EVENT+","+EVENTTIME+","+PAGE+","+ID+","+IndvId;
                res.add(strx);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();

        }
        

        //Delete the entries
        deleteFromTableUserLevel(users_feedback_timing, userId);
        return res;
    }

    /*
     * Use to inform the WebServer that data is ready to be fetched
     *
     */
    public void setWebServerEvent(int userId) {
        int bit = 1;
        String str = "INSERT INTO " + server_event + " VALUES('" + userId + "'," + bit + ")";
        try {
            db.addRows(str);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    /*
     * Check if the Master Event has any entry or not if there is an entry then
     * delete that entry and return true
     *
     * Further Optimization Use a Event Monitor thread, which will act as Event
     * Negotiator, all the threads will register this Negotiator and sleep, this
     * negotiator will awake that particular thread when the event is received
     *
     */

    public boolean getMasterEvent(int userId) {
        boolean res = false;

        String str = "SELECT * FROM " + master_event + " WHERE USERID = " + userId;
        try {
            rs = db.getRSStmt(str);
            if (rs.next()) //Event entry has been made
            {
                res = true;
            }
        } catch (SQLException ex) {
            res = false;
            ex.printStackTrace();
        }

        if (res)//delete that event entry
        {
            deleteFromTableUserLevel(master_event, userId);
        }
        return res;
    }

    /*
     * Delete all the entries for that user
     */
    public boolean deleteFromTableUserLevel(String tableName, int UserId) {
        boolean res = true;
        String str = "DELETE FROM " + tableName + " WHERE USERID='" + UserId + "'";
        try {
            db.executeQuery(str);
        } catch (SQLException ex) {
            ex.printStackTrace();
            res = false;
        }
        return res;
    }

    /*
     * Delete all the entries for that user
     */
    public boolean deleteFromTableSearchLevel(String tableName) {
        boolean res = true;
        String str = "DELETE FROM " + tableName + " WHERE USERID='" + UserId + "' AND SEARCHID='" + searchId + "'";
        try {
            db.executeQuery(str);
        } catch (SQLException ex) {
            ex.printStackTrace();
            res = false;
        }
        return res;
    }

    int getDuplicateId(Individual in) {
        int res = 0;
        int[] chosenFF = in.chosenFF;
        String str = "SELECT INDVID FROM " + hdmarchive + " WHERE USERID='" + UserId + "' AND SEARCHID='" + searchId + "'";
        for (int i = 0; i < chosenFF.length - 1; i++) {
            if (chosenFF[i] == 1) {
                str = str + "' AND F" + i + "='" + in.fitnessValues[i];
            }
        }
        str = str + "'";
        //"' AND F1='"+in.fitnessValues[1]+"' AND F2='"+in.fitnessValues[2]
        //System.out.println(str);
        try {
            rs = db.getRSStmt(str);
            while (rs.next()) {
                res = rs.getInt("INDVID");
                //System.out.println("Found a matchINDVID " + res);
            }
        } catch (SQLException ex) {
            //ex.printStackTrace();
        }

        return res;
    }

    boolean[] checkAbort() {
        boolean[] res = new boolean[2];
        res[0] = false;
        res[1] = false;

        String str = "SELECT * FROM " + abort_table + " WHERE USERID='" + UserId + "'";
        try {
            rs = db.getRSStmt(str);
            if (rs.first()) //there is some entry
            {
                int abort = rs.getInt("ABORT");
                int discard = rs.getInt("DISCARD_DATA");
                if (abort == 1) {
                    res[0] = true;
                    if (discard == 0) {
                        res[1] = false;
                        //res[0] = false; //temporary
                    } else {
                        res[1] = true;
                    }
                    System.out.println("Search is aborted by the User " + UserId + " Data Discard is " + res[1]);
                }


                deleteFromTableUserLevel(abort_table, UserId);

            }
        } catch (SQLException ex) {
            //ex.printStackTrace();
        }

        return res;
    }

    private int getSearchId() {
        int res = 0;
        try {
            String str = "SELECT SEARCHID FROM " + this.cbm + " WHERE USERID='" + UserId + "'";
            rs = db.getRSStmt(str);
            if (rs.last()) {
                res = rs.getInt("SEARCHID");
                res++;
            }
        } catch (Exception ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println("New Search Id is " + res);
        return res;
    }

    /*
     * Clean any local tables
     */
    public void doLocalCleanUp(int UserId) {
        //deleteFromTableUserLevel(sdm, UserId);
        deleteFromTableUserLevel(this.takefeedback, UserId);
        deleteFromTableUserLevel(this.users_feedback, UserId);
        deleteFromTableUserLevel(this.master_event, UserId);
        deleteFromTableUserLevel(this.server_event, UserId);
        deleteFromTableUserLevel(this.abort_table, UserId);
        deleteFromTableUserLevel(session_info, UserId);
        deleteFromTableUserLevel(this.users_activity, UserId);
        deleteFromTableUserLevel(this.users_feedback_timing, UserId);
    }

    public void CleanUpSystemWide() {
        //deleteTable(sdm);
        deleteTable(takefeedback);
        deleteTable(users_feedback);
        deleteTable(master_event);
        deleteTable(server_event);
        deleteTable(abort_table);
        deleteTable(session_info);
        deleteTable(users_activity);
        deleteTable(users_feedback_timing);
    }

     public void doLocalCleanUpExit(int UserId) {
        deleteFromTableUserLevel(sdm, UserId);
     }
    private void deleteTable(String tableName) {
        String str = "DELETE FROM " + tableName;

        try {
            db.executeQuery(str);
        } catch (SQLException ex) {
            ex.printStackTrace();

        }
    }

    public void saveCBMInitial(LinkedList<Individual> Indvs, int tenure_type, int BMPID) {

        System.out.println("Saving to Intial CBM Data");
        String str = "";
        Individual in0 = Indvs.get(0);
        String regionSubbasinIdStr = getstrInt(in0.regionSubbasinId);
        String chosenff = getstrInt(in0.chosenFF);
        for (int i = 0; i < Indvs.size(); i++) {
            Individual in = Indvs.get(i);
            str = "INSERT INTO " + cbm_initial + " VALUES('"
                    + tenure_type + "','"
                    + BMPID + "','"
                    + in.IndvId + "','"
                    + chosenff + "','"
                    + regionSubbasinIdStr + "','"
                    + getstrInt(in.chosenBMPs) + "','"
                    + getstrDouble(in.assignments) + "','"
                    + in.fitnessValues[0] + "','"
                    + in.fitnessValues[1] + "','"
                    + in.fitnessValues[2] + "','"
                    + in.fitnessValues[3] + "','"
                    + in.fitnessValues[4] + "','"
                    + in.fitnessValues[5]
                    + "')";
            try {
                db.addRows(str);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }



        this.chosenFF = Indvs.get(0).chosenFF;
        //int[] regionSubbasinId = Indvs.get(0).regionSubbasinId;
        for (int j = 0; j < ffCount; j++) {
            if (chosenFF[j] == 1) {
                for (int i = 0; i < Indvs.size(); i++) {
                    Individual in = Indvs.get(i);
                    String save1 = "";
                    String str1 = "INSERT INTO " + cbm_initial_f + j + " VALUES('" + tenure_type + "','" + BMPID + "','" + in.IndvId + "'";
                    save1 = "" + cbm_initial_f + j + "," + tenure_type + "," + BMPID + "," + in.IndvId + ",";


                    int c = 0;
                   
                    double [][] subBasinFFs = in.subbasinsFF;
                    for (int k = 0; k < subBasinFFs.length+omittedSub; k++) //for 130 subbasins
                    {
                        if (k<subBasinFFs.length) //only selected subbasins
                        {
                            double val = in.subbasinsFF[k][j];
                            str1 = str1 + ",'" + val + "'";            
                        } else {
                            str1 = str1 + ",'" + NULL + "'";
                        }                        
                    }
                    str1 = str1 + ")";

                    try {
                        db.addRows(str1);
                    } catch (SQLException ex) {
                        //System.out.println("Got Exception for "+in.IndvId);
                        //Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }
    }

    private int returnTotNumBMPs(int[] chosens) {
        int totals = 0;
        for (int i = 0; i < chosens.length; i++) {
            totals = totals + chosens[i];
        }
        return totals;
    }

    public LinkedList<IndividualDesign> getInitCBMIndv(int bmpId, int tenure_type, int[] chosenFF) {
        LinkedList<IndividualDesign> res = new LinkedList();

        //load all the subbasin ffs
        //int nffs = returnTotNumBMPs(chosenFF);

        ResultSet[] rsf = new ResultSet[chosenFF.length]; //create RS for each chosenFFs

        for (int i = 0; i < chosenFF.length; i++) {
            if (chosenFF[i] == 1 && i < 5) {
                String str1 = "SELECT *FROM " + cbm_initial_f + i + " WHERE TENURE_TYPE='" + tenure_type + "' AND BMPID ='" + bmpId + "'";
                //System.out.println("Saved UserData "+str);
                try {
                    rsf[i] = db.getRSStmt(str1);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }



        //String str = "SELECT ASSIGNMENTS FROM CBM WHERE USERID=1";
        String str = "SELECT *FROM " + cbm_initial + " WHERE TENURE_TYPE='" + tenure_type + "' AND BMPID ='" + bmpId + "'";
        //System.out.println("Saved UserData "+str);

        try {
            rs = db.getRSStmt(str);
            int k = 0; //kth individual
            while (rs.next()) {

                IndividualDesign in = new IndividualDesign();
                String assignments = rs.getString("ASSIGNMENTS");
                double[] ffvalues = new double[ffCount + 1];

                for (int i = 0; i < ffCount; i++) {
                    if (chosenFF[i] == 1) {
                        ffvalues[i] = rs.getDouble(fitnessf + i);
                    } else {
                        ffvalues[i] = 0;
                    }
                }
                ffvalues[ffCount] = 0;
                in.IndvId = rs.getInt("INDVID");

                String buf[] = assignments.split(",");
                double[] assign = new double[buf.length];
                for (int i = 0; i < buf.length; i++) {
                    assign[i] = Double.parseDouble(buf[i]);
                }
                in.assignments = assign;
                in.fitnessValues = ffvalues;
                
                /*
                 * double[][] subbasinFF = new double[subbasinUsed][chosenFF.length];
                for (int i = 0; i < ffCount; i++) {
                    if (chosenFF[i] == 1) {

                        String str1 = "SELECT *FROM " + fitnessf + i + " WHERE  USERID ='" + Uid + "' AND SEARCHID='" + this.searchId + "' AND INDVID ='" + in.IndvId + "'";
                        //System.out.println(str1);
                        
                        try {
                            rs = db.getRSStmt(str1);
                            if (rs.first()) {
                                int j = 0;
                                for (int k = 0; k < subbasinUsed; k++) //for 1 subbasins
                 */
                double[][] subbasinsFF = new double[subbasinUsed][chosenFF.length];
                for (int i = 0; i < ffCount; i++) {
                    if (chosenFF[i] == 1 && i < 5) {
                        rsf[i].next();
                        int indvid = rsf[i].getInt("INDVID");
                        if (indvid == k) {
                            for (int j = 0; j < subbasinUsed; j++) {
                                double val = rsf[i].getDouble("S" + j); //Id in db is 1 less than actual
                                subbasinsFF[j][i] = val;
                            }
                        }
                    }
                }
                in.subbasinsFF = subbasinsFF;
                in.confidence = 50;//to be considered as low
                k++; //for next individual                    
                res.add(in);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();

        }

        //add the rs to res

        return res;
    }

    void addFirstCBM(int userId,LinkedList<Individual> Indvs,int feedbackid, int global_sessionId, int local_sessionId, int sessionId, int session_type) {
        System.out.println("Saving to CBM");
        String str = "";

        int[] chosenFF = Indvs.get(0).chosenFF; //first indv
        for (int i = 0; i < Indvs.size(); i++) {

            Individual in = Indvs.get(i);

            str = "INSERT INTO " + cbm + " VALUES('" + getIndvSaveStmt(userId,in,feedbackid,i, global_sessionId, local_sessionId, sessionId, session_type) + "')";;

            try {
                db.addRows(str);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        int[] regionSubbasinId = Indvs.get(0).regionSubbasinId;
        for (int j = 0; j < ffCount; j++) {
            if (chosenFF[j] == 1) {
                for (int i = 0; i < Indvs.size(); i++) {
                    Individual in = Indvs.get(i);
                    String str1 = "INSERT INTO " + fitnessf + j + " VALUES('" + userId + "','" + searchId + "','" + in.IndvId + "'";
                    //int noOfSubbasins = in.subbasinsFF.length;

                    int c = 0;
                   
                    double [][] subBasinFFs = in.subbasinsFF;
                    for (int k = 0; k < subBasinFFs.length+omittedSub; k++) //for 130 subbasins
                    {
                        if (k<subBasinFFs.length) //only selected subbasins
                        {
                            double val = in.subbasinsFF[k][j];
                            str1 = str1 + ",'" + val + "'";
                            c++;                            
                        } else {
                            str1 = str1 + ",'" + NULL + "'";
                        }                        
                    }
                    str1 = str1 + ")";
                    
                    try {
                        db.addRows(str1);
                    } catch (SQLException ex) {
                        //System.out.println("Got Exception for "+in.IndvId);
                        //Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }


    }

    private String getSubbasinFFStr(String[] sbbasinFFs) {
        String res = "";

        for (int i = 0; i < sbbasinFFs.length; i++) {
            res = res + "','" + sbbasinFFs[i];
        }
        //System.out.println("len "+sbbasinFFs.length+" "+res);
        return res;
    }

    public UserData getUserInfo(int UserId) {

        UserData res = null;
        int[] localSubbasin;

        String str = "SELECT * FROM " + users + " WHERE USERID='" + UserId + "'";
        try {

            rs = db.getRSStmt(str);
            if (rs.first()) //there is some entry
            {
                res = new UserData();//if user exist
                String Fname = rs.getString("FNAME");
                String Lname = rs.getString("LNAME");
                String emailId = rs.getString("EMAIL");
                String sub_own = rs.getString("SUBBASIN_OWN_FARM");
                String sub_crop = rs.getString("SUBBASIN_FARM_CROP");
                String sub_cash = rs.getString("SUBBASIN_FARM_CASH");
                
                String[] substrs_own = sub_own.split(",");
                String[] substrs_crop = sub_crop.split(",");
                String[] substrs_cash = sub_cash.split(",");
                int[] Subbasin_own = new int[substrs_own.length];
                int[] Subbasin_crop = new int[substrs_crop.length];
                int[] Subbasin_cash = new int[substrs_cash.length];

                List lst = new LinkedList();
               
                for (int i = 0; i < Subbasin_own.length; i++) {
                    int val = Integer.parseInt(substrs_own[i].trim());
                    Subbasin_own[i] = val;
                    lst.add(val);
                }
                
                for (int i = 0; i < Subbasin_crop.length; i++) {
                    int val = Integer.parseInt(substrs_crop[i].trim());
                    Subbasin_crop[i] = val;
                    lst.add(val);
                }
                
                for (int i = 0; i < Subbasin_cash.length; i++) {
                    int val = Integer.parseInt(substrs_cash[i].trim());
                    Subbasin_cash[i] = val;
                    lst.add(val);
                }
                
                Collections.sort(lst); //sort the list

                //remove duplicates
                int val =0;
                LinkedList selectedSub = new LinkedList();
                for(int i=0;i<lst.size();i++)
                {
                    int valCur = Integer.parseInt(""+lst.get(i));
                    if(val!=valCur)
                    {
                        selectedSub.add(valCur);
                       val = valCur;
                    }                        
                }
                res.FName = Fname;
                res.LName = Lname;
                res.email = emailId;
                localSubbasin = new int[selectedSub.size()];
                
                
                for (int i = 0; i < localSubbasin.length; i++) {
                   localSubbasin[i] = Integer.parseInt(""+selectedSub.get(i));
                    
                }
                
                
                res.localSubbasin = localSubbasin;
            }
        } catch (SQLException ex) {
            //ex.printStackTrace();
        }

        return res;
    }

    
    
    public void setUserActive(int userId)
    {
        //checkSuperComputerActive();
        int bit =1;
        String str = "INSERT INTO " + users_activity + " VALUES('" + userId +"'," +bit + ")";
        try {
            db.addRows(str);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public void setUserInActive(int userId)
    {
        String str = "DELETE FROM " + users_activity + " WHERE USERID='" + userId+"'";
        try {
            db.executeQuery(str);
        } catch (SQLException ex) {
            //ex.printStackTrace();
        }
    }

    void putSessionInfo(int userId, int type, int local_sessionId, int GenerationId, int session_type, int nowait, int leave_forday, int jump, int stop) {

        String str = "INSERT INTO " + session_info + " VALUES('" + userId
                + "','" + type
                + "','" + (local_sessionId + 1)
                + "','" + (GenerationId + 1)
                + "','" + session_type
                + "','" + nowait
                + "','" + leave_forday
                + "','" + jump
                + "'," + stop
                + ")";
        try {
            db.addRows(str);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public Vector checkAbortThread() {
        Vector res = new Vector();
        
        //String str = "SELECT * FROM " + abortuserthread;
        try{
            rs = db.getRSTable(abortuserthread);
            while(rs.next())
            {
                int uid = rs.getInt("USERID");
                res.add(uid);
            }
            this.deleteTable(abortuserthread);

        } catch (SQLException ex){
            
        }
        
        return res;
            
    }
    
    

    public void saveModellingData(double[][] NormalizedData, int global_sessionId,int sdm_id, LinkedList<Individual> Indv, double[][] rdataTrainNew, double[][] rdataTrainCumNew) {
        
         for (int i = 0; i < Indv.size(); i++) {
             Individual in = Indv.get(i);
            String str = "INSERT INTO " + sdm_modelling_data + " VALUES('"
                    + UserId + "','"
                    + this.searchId + "','"
                    + global_sessionId + "','"
                    + sdm_id + "','"
                    + in.IndvId + "','"
                    + in.rating + "','"
                    + in.confidence + "','"
                    + getstrDouble(NormalizedData[i])                
                    + "')";
            //System.out.println("Saved UserData "+str);    
            try {
                db.addRows(str);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
         
         //save into another table
         
         //save rdataTrainNew
         for(int i=0;i<rdataTrainNew.length;i++)
         {
             String str = "INSERT INTO "+ sdm_modelling_data_RandomData + " VALUES('"
                    + UserId + "','"
                    + this.searchId + "','"
                    + global_sessionId + "','"
                    + sdm_id + "','"
                    + i + "','"
                    + getstrDouble(rdataTrainNew[i])
                    + "')";
            //System.out.println("Saved UserData "+str);    
            try {
                db.addRows(str);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
         }
         
         //save rdataTrainCumNew
         if(rdataTrainCumNew!=null)
         {
         for(int i=0;i<rdataTrainCumNew.length;i++)
         {
             String str = "INSERT INTO "+ sdm_modelling_data_RandomDataCumNew + " VALUES('"
                    + UserId + "','"
                    + this.searchId + "','"
                    + global_sessionId + "','"
                    + sdm_id + "','"
                    + i + "','"
                    + getstrDouble(rdataTrainCumNew[i])
                    + "')";
            //System.out.println("Saved UserData "+str);    
            try {
                db.addRows(str);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
         }
         }
    }

    void saveBiasIndvData(int UserId, int global_sessionId, int local_sessionId, int sessionId, int session_type_search, LinkedList<IndividualDesign> BiasMonitorIndvsInfo, LinkedList<IndividualDesign> BiasMonitorIndvsInfoFeedback) {

        for (int i = 0; i < BiasMonitorIndvsInfo.size(); i++) {
            IndividualDesign inActual = BiasMonitorIndvsInfo.get(i);
            IndividualDesign inFeedback = BiasMonitorIndvsInfoFeedback.get(i);
            String str = "INSERT INTO " + biasindvdata + " VALUES('"
                    + UserId + "','"
                    + searchId + "','"
                    + global_sessionId + "','"
                    + local_sessionId + "','"
                    + sessionId + "','"
                    + session_type_search + "','"
                    + i + "','" //Indv count
                    + inActual.IndvId + "','" //the actual Indv that was used
                    + inActual.rating + "','"
                    + inFeedback.rating + "','"
                    + inActual.confidence + "','"
                    + inFeedback.confidence
                    + "')";

            //System.out.println("Saved UserData "+str);    
            try {
                db.addRows(str);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public LinkedList<IndividualDesign> getResumeIndv() {
        
        LinkedList<IndividualDesign> res = null;
        String query = "from HdmarchiveChildren where userid="+this.UserId + " group by searchid";//HQL Query
        List resultList = HQLManager.executeHQLQuery(query);
        
        
        //get the searchId;
        if(resultList.size()>0)
        {          
            HdmarchiveChildren userParam = (HdmarchiveChildren)resultList.get(resultList.size()-1);
            int curSearchId = this.getSearchId()-1;
            int SearchId = userParam.getId().getSearchid();
            if(curSearchId!=SearchId) //P1 Failure
            {
                //this.searchId = 
                this.searchId = SearchId;
                res=null;
                return res;
            }
            res = new LinkedList();
        query = "from HdmarchiveChildren where userid="+this.UserId +" and searchid ="+SearchId;//HQL Query
        resultList = HQLManager.executeHQLQuery(query);  
        userParam = (HdmarchiveChildren)resultList.get(resultList.size()-1);
        
        int globalSessionId = userParam.getId().getGlobalSessionid();
        int localSessionId = userParam.getId().getLocalSessionid();
        this.searchId = SearchId; //reset the searchid for the crashed search
        query = "from HdmarchiveChildren where userid="+this.UserId + " and searchid ="+SearchId +" and GLOBAL_SESSIONID="+globalSessionId+" and LOCAL_SESSIONID="+localSessionId;
        
        resultList = HQLManager.executeHQLQuery(query);
        this.chosenFF = null;
        
        for(Object o : resultList) {
        
            HdmarchiveChildren indv = (HdmarchiveChildren)o;
            IndividualDesign in = new IndividualDesign();
            in.IndvId = (int) indv.getId().getIndvid();
            in.assignments = getDoubleStr(indv.getAssignments());
            in.chosenBMPs = getIntStr(indv.getChosenbmp());
            in.chosenFF = getIntStr(indv.getChosenff());
            in.confidence = indv.getConfidence();
            in.rating = indv.getRating();
            
            if(chosenFF==null)
            {
                this.chosenFF = in.chosenFF; //reset the chosenFF from the previous search
            }
            
            in.fitnessValues = new double[chosenFF.length];
            in.fitnessValues[0] = indv.getF0();
            in.fitnessValues[1] = indv.getF1();
            in.fitnessValues[2] = indv.getF2();
            in.fitnessValues[3] = indv.getF3();
            in.fitnessValues[4] = indv.getF4();
            in.fitnessValues[5] = indv.getF5();
            
            
            
            double[][] subbasinFF = new double[subbasinUsed][chosenFF.length-1];
                for (int i = 0; i < chosenFF.length-1; i++) {
                    if (chosenFF[i] == 1) {

                        String str1 = "SELECT *FROM " + fitnessf + i + " WHERE  USERID ='" + this.UserId + "' AND SEARCHID='" + SearchId + "' AND INDVID ='" + in.IndvId + "'";
                        //System.out.println(str1);
                        
                        try {
                            rs1 = db.getRSStmt(str1);
                            if (rs1.first()) {
                                int j = 0;
                                for (int k = 0; k < subbasinUsed; k++) //for 1 subbasins
                                {
                                        double val = rs1.getDouble("S" + k);
                                        subbasinFF[j][i] = val;
                                        j++;
                                }
                            }

                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                in.subbasinsFF = subbasinFF;
                
            res.add(in);
        }
        
        HdmarchiveChildren indv = (HdmarchiveChildren)resultList.get(resultList.size()-1); //ge the last indv
        HdmarchiveChildrenId id = indv.getId();
        res.get(0).searcInfo = id; //send the search info
        
        }
        return res;
        
    }

    public boolean checkUnfinishedSearch() {
        String str = "FROM Userstats WHERE USERID="+this.UserId;
        boolean res=false;
        List results = this.HQLManager.executeHQLQuery(str);
        if(results.size()>0)
        {
            Userstats u = (Userstats) results.get(results.size()-1);
            java.sql.Timestamp abort = (java.sql.Timestamp) u.getSearchAbort();
            java.sql.Timestamp finish = (java.sql.Timestamp) u.getSearchFinish();
            if(abort==null && finish==null) //search wasn't finished properly, resume the search
            {
                res = true;
            }
        }
        return res;
        
    }

    public LinkedList getSDMModellingData() {
        LinkedList res = new LinkedList();
        
        String str = "FROM SdmModellingData WHERE USERID="+this.UserId + " AND SEARCHID="+this.searchId;
        List results = this.HQLManager.executeHQLQuery(str);
        
        Iterator itr = results.iterator();
        LinkedList<LinkedList<double[]>> Lst = new LinkedList();
        int sdmId=-1;//startId
        LinkedList<double[]> tempLst = new LinkedList();
        while(itr.hasNext())
        {
            SdmModellingData o = (SdmModellingData) itr.next();
            SdmModellingDataId id = o.getId();
            int curSdmId = id.getSdmId();
            if(curSdmId!=sdmId)
            {
                sdmId = curSdmId;
                tempLst = new LinkedList();   
                Lst.add(tempLst);
            }                       
            double[] tmp = this.getDoubleStr(o.getDatavalues());           
            tempLst.add(tmp);
        }
        for(int i=0;i<Lst.size();i++)
        {
            LinkedList<double[]> tm = Lst.get(i);
            double[][] dar = new double[tm.size()][tm.get(0).length];
            for(int j=0;j<dar.length;j++)
            {
                dar[j] = tm.get(j);
            }
            res.add(dar);
        }
        
        return res;
    }

    public LinkedList<SdmData> getSDMData() {
        LinkedList<SdmData> res = new LinkedList();
        String str = "FROM SdmData WHERE USERID="+this.UserId + " AND SEARCHID="+this.searchId;
        List results = this.HQLManager.executeHQLQuery(str);
        for(int i=0;i<results.size();i++)
        {
            res.add((SdmData)results.get(i)); 
        }
        return res;
    }

    public void dumpData(String name) {
        String query = "";
        List res = null;  
        String file="";
        String location="C:/Users/MBS-Admin/Dropbox/Experiment_Data/User "+this.UserId+" "+name+"/"+this.searchId+"/";
        File f = new File(location);
        if(!f.exists())
        {
            f.mkdirs();
        }
        
        //BiasIndvdata
        file = location+"biasindvdata.csv";
        query = "FROM Biasindvdata WHERE USERID="+this.UserId + " AND SEARCHID="+this.searchId;
        res = this.HQLManager.executeHQLQuery(query);
        try {
            FileWriter fr = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fr);
            
            //Add Header
            String header = "USERID,SEARCHID,GLOBAL_SESSIONID,LOCAL_SESSIONID,SESSIONID,SESSION_TYPE,INDV_COUNT,INDVID,RATING_ACTUAL,RATING_NEW,CONFIDENCE_ACTUAL,CONFIDENCE_NEW";
            bw.write(header);
            bw.newLine();
            Iterator itr = res.iterator();
            while(itr.hasNext())
            {
                Biasindvdata ob = (Biasindvdata) itr.next();
                String str = "";
                str = ob.getId().getUserid()+",";
                str = str+ ob.getId().getSearchid()+",";               
                str = str+ ob.getId().getGlobalSessionid()+",";          
                str = str+ ob.getId().getLocalSessionid()+",";
                str = str+ ob.getId().getSessionid()+",";
                str = str+ ob.getId().getSessionType()+",";
                str = str+ ob.getId().getIndvCount()+",";
                str = str+ ob.getIndvid()+",";
                str = str+ ob.getRatingActual()+",";
                str = str+ ob.getRatingNew()+",";
                str = str+ ob.getConfidenceActual()+",";
                str = str+ ob.getConfidenceNew();
                bw.write(str);
                bw.newLine();
            }
            bw.flush();
            bw.close();
            fr.close();
            
        } catch (IOException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //CBM
        file = location+"cbm.csv";
        query = "SELECT * FROM Cbm WHERE USERID="+this.UserId + " AND SEARCHID="+this.searchId;
        try {
            rs = db.getRSStmt(query);
            
            FileWriter fr = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fr);
            String header="USERID,SEARCHID,FEEDBACKID,GLOBAL_SESSIONID,LOCAL_SESSIONID,SESSIONID,SESSION_TYPE,ORDERID,INDVID,RATING,CONFIDENCE,CHOSENFF,REGIONSUBBASINID,CHOSENBMP,ASSIGNMENTS,F0,F1,F2,F3,F4,F5";
            bw.write(header);
            bw.newLine();
            while (rs.next()) 
            {          
                String str = "";
                str = str+rs.getString("USERID")+",";
                str = str+rs.getString("SEARCHID")+",";
                str = str+rs.getString("FEEDBACKID")+",";
                str = str+rs.getString("GLOBAL_SESSIONID")+",";
                str = str+rs.getString("LOCAL_SESSIONID")+",";
                str = str+rs.getString("SESSIONID")+",";
                str = str+rs.getString("SESSION_TYPE")+",";
                str = str+rs.getString("ORDERID")+",";
                str = str+rs.getString("INDVID")+",";
                str = str+rs.getString("RATING")+",";
                str = str+rs.getString("CONFIDENCE")+",\"";
                str = str+rs.getString("CHOSENFF")+"\",\"";
                str = str+rs.getString("REGIONSUBBASINID")+"\",\"";
                str = str+rs.getString("CHOSENBMP")+"\",\"";
                str = str+rs.getString("ASSIGNMENTS")+"\",";
                str = str+rs.getString("F0")+",";
                str = str+rs.getString("F1")+",";
                str = str+rs.getString("F2")+",";
                str = str+rs.getString("F3")+",";
                str = str+rs.getString("F4")+","; 
                str = str+rs.getString("F5");
                bw.write(str);
                bw.newLine();
            }
            bw.flush();
            bw.close();
            fr.close();
            
        }catch (SQLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //f1
        file = location+"ff1.csv";
        query = "FROM F1 WHERE USERID="+this.UserId + " AND SEARCHID="+this.searchId;
        res = this.HQLManager.executeHQLQuery(query);
        try {
            FileWriter fr = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fr);
            
            //Add Header
            String header="USERID,SEARCHID,INDVID,S0,S1,S2,S3,S4,S5,S6,S7,S8,S9,S10,S11,S12,S13,S14,S15,S16,S17,S18,S19,S20,S21,S22,S23,S24,S25,S26,S27,S28,S29,S30,S31,S32,S33,S34,S35,S36,S37,S38,S39,S40,S41,S42,S43,S44,S45,S46,S47,S48,S49,S50,S51,S52,S53,S54,S55,S56,S57,S58,S59,S60,S61,S62,S63,S64,S65,S66,S67,S68,S69,S70,S71,S72,S73,S74,S75,S76,S77,S78,S79,S80,S81,S82,S83,S84,S85,S86,S87,S88,S89,S90,S91,S92,S93,S94,S95,S96,S97,S98,S99,S100,S101,S102,S103,S104,S105,S106,S107,S108,S109,S110,S111,S112,S113,S114,S115,S116,S117,S118,S119,S120,S121,S122,S123,S124,S125,S126,S127,S128,S129";
            bw.write(header);
            bw.newLine();
            Iterator itr = res.iterator();
            while(itr.hasNext())
            {
                F1 ob = (F1) itr.next();
                String str = "";
                str = ob.getId().getUserid()+",";
                str = str+ ob.getId().getSearchid()+",";               
                str = str+ ob.getId().getIndvid()+",";   
                str = str+ ob.getCSVString();
                bw.write(str);
                bw.newLine();
            }
            bw.flush();
            bw.close();
            fr.close();            
        } catch (IOException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //f2
        file = location+"ff2.csv";
        query = "FROM F2 WHERE USERID="+this.UserId + " AND SEARCHID="+this.searchId;
        res = this.HQLManager.executeHQLQuery(query);
        try {
            FileWriter fr = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fr);
            
            //Add Header
            String header="USERID,SEARCHID,INDVID,S0,S1,S2,S3,S4,S5,S6,S7,S8,S9,S10,S11,S12,S13,S14,S15,S16,S17,S18,S19,S20,S21,S22,S23,S24,S25,S26,S27,S28,S29,S30,S31,S32,S33,S34,S35,S36,S37,S38,S39,S40,S41,S42,S43,S44,S45,S46,S47,S48,S49,S50,S51,S52,S53,S54,S55,S56,S57,S58,S59,S60,S61,S62,S63,S64,S65,S66,S67,S68,S69,S70,S71,S72,S73,S74,S75,S76,S77,S78,S79,S80,S81,S82,S83,S84,S85,S86,S87,S88,S89,S90,S91,S92,S93,S94,S95,S96,S97,S98,S99,S100,S101,S102,S103,S104,S105,S106,S107,S108,S109,S110,S111,S112,S113,S114,S115,S116,S117,S118,S119,S120,S121,S122,S123,S124,S125,S126,S127,S128,S129";
            bw.write(header);
            bw.newLine();
            Iterator itr = res.iterator();
            while(itr.hasNext())
            {
                F2 ob = (F2) itr.next();
                String str = "";
                str = ob.getId().getUserid()+",";
                str = str+ ob.getId().getSearchid()+",";               
                str = str+ ob.getId().getIndvid()+",";   
                str = str+ ob.getCSVString();
                bw.write(str);
                bw.newLine();
            }
            bw.flush();
            bw.close();
            fr.close();            
        } catch (IOException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f3
        file = location+"ff3.csv";
        query = "FROM F3 WHERE USERID="+this.UserId + " AND SEARCHID="+this.searchId;
        res = this.HQLManager.executeHQLQuery(query);
        try {
            FileWriter fr = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fr);
            
            //Add Header
            String header="USERID,SEARCHID,INDVID,S0,S1,S2,S3,S4,S5,S6,S7,S8,S9,S10,S11,S12,S13,S14,S15,S16,S17,S18,S19,S20,S21,S22,S23,S24,S25,S26,S27,S28,S29,S30,S31,S32,S33,S34,S35,S36,S37,S38,S39,S40,S41,S42,S43,S44,S45,S46,S47,S48,S49,S50,S51,S52,S53,S54,S55,S56,S57,S58,S59,S60,S61,S62,S63,S64,S65,S66,S67,S68,S69,S70,S71,S72,S73,S74,S75,S76,S77,S78,S79,S80,S81,S82,S83,S84,S85,S86,S87,S88,S89,S90,S91,S92,S93,S94,S95,S96,S97,S98,S99,S100,S101,S102,S103,S104,S105,S106,S107,S108,S109,S110,S111,S112,S113,S114,S115,S116,S117,S118,S119,S120,S121,S122,S123,S124,S125,S126,S127,S128,S129";
            bw.write(header);
            bw.newLine();
            Iterator itr = res.iterator();
            while(itr.hasNext())
            {
                F3 ob = (F3) itr.next();
                String str = "";
                str = ob.getId().getUserid()+",";
                str = str+ ob.getId().getSearchid()+",";               
                str = str+ ob.getId().getIndvid()+",";   
                str = str+ ob.getCSVString();
                bw.write(str);
                bw.newLine();
            }
            bw.flush();
            bw.close();
            fr.close();            
        } catch (IOException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f4
        file = location+"ff4.csv";
        query = "FROM F4 WHERE USERID="+this.UserId + " AND SEARCHID="+this.searchId;
        res = this.HQLManager.executeHQLQuery(query);
        try {
            FileWriter fr = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fr);
            
            //Add Header
            String header="USERID,SEARCHID,INDVID,S0,S1,S2,S3,S4,S5,S6,S7,S8,S9,S10,S11,S12,S13,S14,S15,S16,S17,S18,S19,S20,S21,S22,S23,S24,S25,S26,S27,S28,S29,S30,S31,S32,S33,S34,S35,S36,S37,S38,S39,S40,S41,S42,S43,S44,S45,S46,S47,S48,S49,S50,S51,S52,S53,S54,S55,S56,S57,S58,S59,S60,S61,S62,S63,S64,S65,S66,S67,S68,S69,S70,S71,S72,S73,S74,S75,S76,S77,S78,S79,S80,S81,S82,S83,S84,S85,S86,S87,S88,S89,S90,S91,S92,S93,S94,S95,S96,S97,S98,S99,S100,S101,S102,S103,S104,S105,S106,S107,S108,S109,S110,S111,S112,S113,S114,S115,S116,S117,S118,S119,S120,S121,S122,S123,S124,S125,S126,S127,S128,S129";
            bw.write(header);
            bw.newLine();
            Iterator itr = res.iterator();
            while(itr.hasNext())
            {
                F4 ob = (F4) itr.next();
                String str = "";
                str = ob.getId().getUserid()+",";
                str = str+ ob.getId().getSearchid()+",";               
                str = str+ ob.getId().getIndvid()+",";   
                str = str+ ob.getCSVString();
                bw.write(str);
                bw.newLine();
            }
            bw.flush();
            bw.close();
            fr.close();            
        } catch (IOException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
         
        //hdmarchive_children
        file = location+"hdmarchive_children.csv";
        query = "SELECT * FROM hdmarchive_children WHERE USERID="+this.UserId + " AND SEARCHID="+this.searchId;
        try {
            rs = db.getRSStmt(query);
            
            FileWriter fr = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fr);
            String header="USERID,SEARCHID,FEEDBACKID,GLOBAL_SESSIONID,LOCAL_SESSIONID,SESSIONID,SESSION_TYPE,ORDERID,INDVID,RATING,CONFIDENCE,CHOSENFF,REGIONSUBBASINID,CHOSENBMP,ASSIGNMENTS,F0,F1,F2,F3,F4,F5";
            bw.write(header);
            bw.newLine();
            while (rs.next()) 
            {          
                String str = "";
                str = str+rs.getString("USERID")+",";
                str = str+rs.getString("SEARCHID")+",";
                str = str+rs.getString("FEEDBACKID")+",";
                str = str+rs.getString("GLOBAL_SESSIONID")+",";
                str = str+rs.getString("LOCAL_SESSIONID")+",";
                str = str+rs.getString("SESSIONID")+",";
                str = str+rs.getString("SESSION_TYPE")+",";
                str = str+rs.getString("ORDERID")+",";
                str = str+rs.getString("INDVID")+",";
                str = str+rs.getString("RATING")+",";
                str = str+rs.getString("CONFIDENCE")+",\"";
                str = str+rs.getString("CHOSENFF")+"\",\"";
                str = str+rs.getString("REGIONSUBBASINID")+"\",\"";
                str = str+rs.getString("CHOSENBMP")+"\",\"";
                str = str+rs.getString("ASSIGNMENTS")+"\",";
                str = str+rs.getString("F0")+",";
                str = str+rs.getString("F1")+",";
                str = str+rs.getString("F2")+",";
                str = str+rs.getString("F3")+",";
                str = str+rs.getString("F4")+","; 
                str = str+rs.getString("F5");
                bw.write(str);
                bw.newLine();
            }
            bw.flush();
            bw.close();
            fr.close();
            
        }catch (SQLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        //hdmarchive_nondomiated
        
        file = location+"hdmarchive_nondomiated.csv";
        query = "FROM HdmarchiveNondominated WHERE USERID="+this.UserId + " AND SEARCHID="+this.searchId;
        res = this.HQLManager.executeHQLQuery(query);
        try {
            FileWriter fr = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fr);
            String header="USERID,SEARCHID,GLOBAL_SESSIONID,LOCAL_SESSIONID,SESSIONID,SESSION_TYPE,INDVID,RATING,CONFIDENCE";
            bw.write(header);
            bw.newLine();
            Iterator itr = res.iterator();
            while(itr.hasNext())
            {
                HdmarchiveNondominated ob = (HdmarchiveNondominated) itr.next();
                String str = "";
                str = ob.getId().getUserid()+",";
                str = str+ ob.getId().getSearchid()+",";               
                str = str+ ob.getId().getGlobalSessionid()+",";          
                str = str+ ob.getId().getLocalSessionid()+",";
                str = str+ ob.getId().getSessionid()+",";
                str = str+ ob.getId().getSessionType()+",";
                str = str+ ob.getId().getIndvid()+",";
                str = str+ ob.getRating()+",";
                str = str+ ob.getConfidence();               
                bw.write(str);
                bw.newLine();
            }
            bw.flush();
            bw.close();
            fr.close();
            
        } catch (IOException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //kendallstats
        file = location+"kendallstats.csv";
        query = "FROM Kendallstats WHERE USERID="+this.UserId + " AND SEARCHID="+this.searchId;
        res = this.HQLManager.executeHQLQuery(query);
        try {
            FileWriter fr = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fr);
            String header="USERID,SEARCHID,GLOBAL_SESSIONID,LOCAL_SESSIONID,S,Z,HDMSelected";
            bw.write(header);
            bw.newLine();
            Iterator itr = res.iterator();
            while(itr.hasNext())
            {
                Kendallstats ob = (Kendallstats) itr.next();
                String str = "";
                str = ob.getId().getUserid()+",";
                str = str+ ob.getId().getSearchid()+",";               
                str = str+ ob.getId().getGlobalSessionid()+",";          
                str = str+ ob.getId().getLocalSessionid()+",";
                str = str+ ob.getS()+","; 
                str = str+ ob.getZ()+","; 
                str = str+ ob.getHdmselected(); 
                bw.write(str);
                bw.newLine();
            }
            bw.flush();
            bw.close();
            fr.close();
            
        } catch (IOException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //kendallstats_user_data
        file = location+"kendallstats_user_data.csv";
        query = "FROM KendallstatsUserData WHERE USERID="+this.UserId + " AND SEARCHID="+this.searchId;
        res = this.HQLManager.executeHQLQuery(query);
        try {
            FileWriter fr = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fr);
            String header="USERID,SEARCHID,GLOBAL_SESSIONID,LOCAL_SESSIONID,SESSIONID,MEAN,STD_DEVIATION";
            bw.write(header);
            bw.newLine();
            Iterator itr = res.iterator();
            while(itr.hasNext())
            {
                KendallstatsUserData ob = (KendallstatsUserData) itr.next();
                String str = "";
                str = ob.getId().getUserid()+",";
                str = str+ ob.getId().getSearchid()+",";               
                str = str+ ob.getId().getGlobalSessionid()+",";          
                str = str+ ob.getId().getLocalSessionid()+",";
                str = str+ ob.getId().getSessionid()+",";
                str = str+ ob.getMean()+","; 
                str = str+ ob.getStdDeviation();              
                bw.write(str);
                bw.newLine();
            }
            bw.flush();
            bw.close();
            fr.close();
            
        } catch (IOException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //sdm_data       
        file = location+"sdm_data.csv";
        query = "FROM SdmData WHERE USERID="+this.UserId + " AND SEARCHID="+this.searchId;
        res = this.HQLManager.executeHQLQuery(query);
        try {
            FileWriter fr = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fr);
            String header="";
            bw.write(header);
            bw.newLine();
            Iterator itr = res.iterator();
            while(itr.hasNext())
            {
                SdmData ob = (SdmData) itr.next();
                String str = "USERID,SEARCHID,SDM_GENERATIONID,SDM_ID,NNwin,minErrorNN,minErrorNNVals,minErrorNNID,linearModelMinError,currentLinearId,linearModelErrors,minANFIS,minANFISID";
                str = ob.getId().getUserid()+",";             
                str = str+ ob.getId().getSearchid()+",";
                str = str+ ob.getId().getSdmGenerationid()+",";
                str = str+ ob.getId().getSdmId()+",";
                str = str+ ob.getNnwin()+",";
                str = str+ ob.getMinErrorNn()+",\"";
                str = str+ ob.getMinErrorNnvals()+"\",\"";
                str = str+ ob.getMinErrorNnid()+"\",";
                str = str+ ob.getLinearModelMinError()+",";
                str = str+ ob.getCurrentLinearId()+",\"";
                str = str+ ob.getLinearModelErrors()+"\",";
                str = str+ ob.getMinAnfis()+",";
                str = str+ ob.getMinAnfisid()+",";
                str = str+ ob.getRange();
                
                bw.write(str);
                bw.newLine();
            }
            bw.flush();
            bw.close();
            fr.close();
            
        } catch (IOException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //userstats_feedback_timing
        
        file = location+"userstats_feedback_timing.csv";
        query = "SELECT * FROM userstats_feedback_timing WHERE USERID="+this.UserId + " AND SEARCHID="+this.searchId;
        
        try {
            rs = db.getRSStmt(query);
            FileWriter fr = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fr);
            String header="USERID,SEARCHID,FEEDBACKID,GLOBAL_SESSIONID,LOCAL_SESSIONID,SESSIONID,INTROSPECTION_TYPE,ID,PAGE,EVENT,EVENTTIME,INDVID";
            bw.write(header);
            bw.newLine();
            while (rs.next()) 
            {          
                String str = "";
                str = str+rs.getString("USERID")+",";
                str = str+rs.getString("SEARCHID")+",";
                str = str+rs.getString("FEEDBACKID")+",";
                str = str+rs.getString("GLOBAL_SESSIONID")+",";
                str = str+rs.getString("LOCAL_SESSIONID")+",";
                str = str+rs.getString("SESSIONID")+",";
                str = str+rs.getString("INTROSPECTION_TYPE")+",";
                str = str+rs.getString("ID")+",";
                str = str+rs.getString("PAGE")+",";
                str = str+rs.getString("EVENT")+","; 
                str = str+rs.getString("EVENTTIME")+",";          
                str = str+rs.getString("INDVID");
                bw.write(str);
                bw.newLine();
            }
            bw.flush();
            bw.close();
            fr.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ComputerclusterInfo[] checkClusterChange() {
        ComputerclusterInfo[] cluster = null;
        String str = "FROM ComputerclusterInfo";
       
        List results = this.HQLManager.executeHQLQuery(str);
        
        if(results.size()>0)
        {
            cluster = new ComputerclusterInfo[results.size()];
            for(int i=0;i<results.size();i++)
            {
                ComputerclusterInfo u = (ComputerclusterInfo) results.get(i);
                cluster[i] = u;
            }    
            deleteTable(computercluster_info);
        }   
        return cluster;
    }
    
   
}
