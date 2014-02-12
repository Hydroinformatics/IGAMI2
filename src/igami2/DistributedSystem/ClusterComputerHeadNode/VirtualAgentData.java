/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.ClusterComputerHeadNode;

import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import java.util.LinkedList;

/**
 *
 * @author VIDYA
 */
public class VirtualAgentData {
    
    public int AgentId=0;
    public String HostName="";
    public int ActiveUserCount =0;
    int UserMax = 5;//max number of simultaenous users
    LinkedList<Individual>[] ArrayIndvList;
    public boolean [] Users;
    public int[] UserId;//to which user this set belong
    public boolean status = false;
    public VirtualAgentData()
    {
        Users = new boolean[UserMax];
        UserId = new int[UserMax];
        ArrayIndvList = new LinkedList[UserMax];
        for(int i=0;i<UserMax;i++)
        {
            ArrayIndvList[i] = new LinkedList<Individual>();
            Users[i] = true;//slot is free
            UserId[i] = 999;//no user
        }      
    }
    
}
