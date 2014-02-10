/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.Optimization.DistributedNSGAII.wrestore.SWAT_BMPs_NonInteracOptim;

import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.FitnessFunction;
import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @author MBS-Admin
 */
public class SWAT_BMP_IndividualsRatingFitnessFunction implements FitnessFunction,Serializable {

    //int RankMax = 3; //use to scale the rank with other fitness functions
    
    @Override
    public double evaluate(Individual individual, String base) throws IOException {
        return (float)individual.rating;//RankMax;
    }
    
}
