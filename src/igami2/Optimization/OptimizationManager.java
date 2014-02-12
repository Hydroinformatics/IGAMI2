/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.Optimization;

import igami2.DataBase.IndividualDesignManager;
import igami2.Optimization.DPLA.DPLAHandler;
import java.io.IOException;

/**
 *
 * @author VIDYA
 */
public class OptimizationManager {

    public DPLAHandler dpla;
    public int noOfDPLA = 4;
    public boolean load = true;// use to load the individuals from some archive else will create a new set of individuals
    IndividualDesignManager idm;

    public OptimizationManager(IndividualDesignManager idm) {
        this.idm = idm;
        dpla = new DPLAHandler();
    }

    public void optimize(String type) {
        if (type.compareTo("NSGA") == 0) {
            try {
                idm.IndvPopulation = idm.nsga2.evolveOneGen(idm.IndvPopulation);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (type.compareTo("DPLA") == 0) {
            dpla.start();

        } else if (type.compareTo("PSO") == 0) {
            
        } else if (type.compareTo("MA") == 0) {
            
        }
        else if (type.compareTo("HYBRID") == 0) {
            
        }
    }
}
