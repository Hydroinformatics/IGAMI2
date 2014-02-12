/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.ClusterNodes;

import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import java.util.LinkedList;

/**
 *
 * @author VIDYA
 */
public class DistributeJob {

    public static LinkedList<Individual> a2;
    int n;
    public static Individual[] a1;
    

    public DistributeJob() {
    }

    public Individual[] makeParallel(int p, LinkedList<Individual> a2) //p number of SMP processors
    {
        this.a2 = a2;
        n = a2.size();
        a1 = new Individual[n];
        //Modyfying to make it more parallel, one thread for each child
        //make binarytournament in parallel

        EvaluateIndividualThread[] para = new EvaluateIndividualThread[p];

        
        //Evaluate p Children parallely

        int n1 = n / p;
        int n2 = n % p; //any extra children are evaluated separately
        System.out.println("\nEvaluating " + p + " Children parallel");
        int i = 0;
        for (; i < n1; i++) {
            for (int j = 0; j < p; j++) {
                para[j] = new EvaluateIndividualThread(i * p, j,0,0); //running total p threads parallel
            }
            //wait them to join
            for (int k = 0; k < p; k++) { //wait for all the threads to join back
                try {
                    para[k].t.join(); //wait for the threads to join the current thread
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

            }
        
        }
        if (n2 > 0)//some children are left
        {
            for (int j = 0; j < n2; j++) {
                para[j] = new EvaluateIndividualThread(i * p, j,0,0); //running total n2 threads parallel
            }
            //wait them to join
            for (int k = 0; k < n2; k++) { //wait for all the threads to join back
                try {
                    para[k].t.join(); //wait for the threads to join the current thread
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return a1;
    }
}
