/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem;

import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import java.util.LinkedList;

/**
 *
 * @author VIDYA
 */
public interface DistributedSystem extends java.rmi.Remote {   
     boolean JobAsyncResult(LinkedList<Individual> indv, int AgentId) throws java.rmi.RemoteException;     
     LinkedList<Individual> JobSync(LinkedList<Individual> indv) throws java.rmi.RemoteException;     
     boolean initializeAgent(int number) throws java.rmi.RemoteException;    
     boolean openFileWriter(String name) throws java.rmi.RemoteException;
     boolean closeFile(String name) throws java.rmi.RemoteException;
     boolean putData(String name, byte[] buff) throws java.rmi.RemoteException;
     boolean synDirs() throws java.rmi.RemoteException;
     boolean distributejob(LinkedList<Individual> a) throws java.rmi.RemoteException;
     void exitSystem()throws java.rmi.RemoteException;
     int addUser(String host,int [] chosenFF)throws java.rmi.RemoteException;
     LinkedList<Individual> takeFeedback(LinkedList<Individual> indv) throws java.rmi.RemoteException;
     boolean showResult(LinkedList<Individual> indv, double[] SDMRank, double[] data) throws java.rmi.RemoteException;
     LinkedList<Individual> doIntrospection(LinkedList<Individual> IndvPop) throws java.rmi.RemoteException;
     boolean asyncMsg(int id, String type, boolean val) throws java.rmi.RemoteException;
}
