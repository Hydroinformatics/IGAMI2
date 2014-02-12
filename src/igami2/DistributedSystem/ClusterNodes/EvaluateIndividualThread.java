/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.ClusterNodes;

import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;

/**
 *
 * @author VIDYA
 */
class EvaluateIndividualThread implements Runnable {

    int idx = 0; //Can be used to track number of threads
    int idy = 0;
    int ArIdx=0;//for default
    Thread t;
    private int p;
   
    EvaluateIndividualThread(int idx, int idy, int ArIdx, int p) //idx Id of the parent thread and idy is the id of currnet thread
    {
        this.idx = idx;
        this.idy = idy;
        this.ArIdx = ArIdx;
        this.p = p;
        String thName = "Th" +ArIdx*p+"."+ idx + "." + idy;
        t = new Thread(this, thName);
        t.start();
    }

    public void run() {       
            System.out.println("\nJob Runby Thread " + ArIdx*p+"."+ idx + "." + idy);
            //System.out.println("Running Th:"+idx+"."+idy);
            
                //Individual individual1 = (Individual) VirtualAgent.ArrayIndvList[ArIdx].get(idx+idy).clone();
                Individual individual1 = VirtualAgent.ArrayIndvList[ArIdx].get(idx+idy);
                individual1.evaluateIndividual(ArIdx*p+idy);//Evaluate the individual
                VirtualAgent.ArrayIndvList[ArIdx].set(idx+idy, individual1);
    }
}
