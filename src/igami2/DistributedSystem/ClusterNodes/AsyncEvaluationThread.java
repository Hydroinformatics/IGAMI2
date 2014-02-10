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
public class AsyncEvaluationThread implements Runnable{

    Thread t;
    int p=4;
    int ArIdx;
    LinkedList<Individual> indv;
    public AsyncEvaluationThread(int ArIdx,int p)
    {
        this.ArIdx = ArIdx;
        this.p = p;
        t = new Thread(this,"AsyncTh"+ArIdx);
        t.start();
    }
    
    @Override
    public void run() {
        
         indv = AgentMain.ArrayIndvList[ArIdx];
        
        
        EvaluateIndividualThread[] para = new EvaluateIndividualThread[p];

        
        //Evaluate p Children parallely

        int n = indv.size();
        int n1 = n / p;
        int n2 = n % p; //any extra children are evaluated separately
        System.out.println("\nEvaluating " + p + " Children parallel");
        int i = 0;
        for (; i < n1; i++) {
            for (int j = 0; j < p; j++) {
                para[j] = new EvaluateIndividualThread(i * p, j, ArIdx, p); //running total p threads parallel
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
                para[j] = new EvaluateIndividualThread(i * p, j, ArIdx, p); //running total n2 threads parallel
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
        
        AgentMain.AsyncReturn(ArIdx); //tell agent to return the result

        System.out.println("Job Completed Sucessfully");        
    }
    
}
