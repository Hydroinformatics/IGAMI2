/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.MixedInitiative;

import igami2.DataBase.DBManager;
import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import java.util.LinkedList;

/**
 *
 * @author VIDYA
 */
public class StatisticsManager {
    
    double mean=0;
    double stdDeviation=0;
    
    DBManager dbm;
    MathFunctions fun;
    
    private int SessionNo;
    public LinkedList<Double> Stddev;
    
    public StatisticsManager(DBManager db)
    {
        this.dbm = db;
        
        SessionNo = 0;
        Stddev = new LinkedList<Double>();
        fun = new MathFunctions();
    }
    
    /*
     * Evaluate Confidence
     */

    public void doEvaluationConfidence(LinkedList<Individual> indv) {

        double[] confidenceAr = new double[indv.size()];
        
        
        for(int i=0;i<indv.size();i++)
        {
            confidenceAr[i] = indv.get(i).confidence;
        }
        
        double mean1 = fun.Mean(confidenceAr);
        double stddev = fun.StandardDeviation(confidenceAr);
        //save std for Kendal
        Stddev.add(stddev);
        this.mean = mean1;
        this.stdDeviation = stddev;
        
    }  
    public int loadKendalStats(MannKendall kendall, int global_sessionId, int local_sessionId, int sessionId)
    {
        int res=0;
        //load SD Values
        LinkedList<double[]> kendallStats = dbm.loadKendallStats(global_sessionId);
        for(int i=0;i<kendallStats.size();i++)
        {
            double[] ob = kendallStats.get(i);
            int a = (int) ob[0];
            kendall.Svalues_MannKendall.add(a);
            kendall.Zvalues_MannKendall.add(ob[1]);
        }
        
        LinkedList<Double> SDs = dbm.loadUserStatsKendall(global_sessionId, local_sessionId, sessionId);
        resetStddev();
        for(int i=0;i<SDs.size();i++)
        {
            Stddev.add(SDs.get(i));
        }
        //load S and Z values for Kendall
        res = Stddev.size();
        
        return res;        
    }
    public void resetStddev()
    {
        Stddev = new LinkedList();
    }
    
    public double[] getStddev()
    {
        double[] res = null;
        res = new double[Stddev.size()];
       //System.out.println("Sddev Are ");
        for(int i=0;i<Stddev.size();i++)
        {
            res[i] = Stddev.get(i);
            //System.out.println(res[i]);
        }
        
        return res;
    }

    void UserStatKendal(int global_sessionId, int local_sessionId, int hdmGenerationId) {    
        dbm.saveUserStatsKendall(global_sessionId, local_sessionId, hdmGenerationId, mean, stdDeviation);     
    }

    
}
