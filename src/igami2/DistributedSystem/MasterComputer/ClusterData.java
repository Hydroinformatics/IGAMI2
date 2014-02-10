/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.MasterComputer;

/**
 *
 * @author VIDYA
 */
public class ClusterData {
    
    private String Name;
    private int ClusterId;//clusterId
    private String HostName; //Complete RMI host name
    private int port; //RMI Port Address
    private int Nodes;//number of nodes
    private int P;//capacity(No of SWAT Model) for each node, processor count, given uniform cluster
    private boolean Status;//if cluster is available or not
    private int P_Max; //maximum capacity of the cluster   
    private int P_Avail;//currently Available
    private int User_Max;//maximum number of Active Users A system can handle
    private int User_Active;//Currently Active Users on the cluster

    public ClusterData(String Name, int ClusterId, String HostName, int port, int Nodes, int P, boolean Status, int User_Max) {
        this.Name = Name;
        this.ClusterId = ClusterId;
        this.HostName = HostName;
        this.port = port;
        this.Nodes = Nodes;
        this.P = P;
        this.Status = Status;
        P_Max = Nodes*P;
        P_Avail=P_Max;
        this.User_Max = User_Max;
        this.User_Active = 0;
    }
    
    public int getClusterId() {
        return ClusterId;
    }

    public String getHostName() {
        return HostName;
    }

    public String getName() {
        return Name;
    }

    public int getNodes() {
        return Nodes;
    }

    public int getP() {
        return P;
    }

    public int getPort() {
        return port;
    }
    
     public boolean getStatus() {
        return Status;
    }

    public void setStatus(boolean Status) {
        this.Status = Status;
    }
    
    public int getP_Avail() {
        return P_Avail;
    }

    public void decrementP_Avail(int val)
    {
        int v = this.P_Avail - val;
        if(v>=0)
        {
            this.P_Avail = v;
        }
    }
    
    public void incrementP_Avail(int val)
    {
        int v = this.P_Avail + val;
        if(v<=P_Max)
        {
            this.P_Avail = v;
        }
        else if(v>0)
        {
            this.P_Avail = P_Max;
        }
    }
    public void setP_Avail(int P_Avail) {
        this.P_Avail = P_Avail;
    }

    public int getP_Max() {
        return P_Max;
    }

    public void setP_Max(int P_Max) {
        if(P_Max<P*Nodes)
            this.P_Max = P_Max;
    }

    public int getUser_Active() {
        return User_Active;
    }

    public void setUser_Active(int User_Active) {
        this.User_Active = User_Active;
    }

    public int getUser_Max() {
        return User_Max;
    }

    public void setUser_Max(int User_Max) {
        this.User_Max = User_Max;
    }
    
    public void decrementUserCount()
    {
        User_Active--;
    }
    
    public void incrementUserCount()
    {
        User_Active++;
    }
    
    /*
     * Use to check if there is an Active User Slot for this cluster or not
     */
    public boolean checkActiveSlot()
    {
        boolean res=false;
        if(this.User_Active<this.User_Max)
        {
            res = true;
        }
        return res;
    }
    
}
