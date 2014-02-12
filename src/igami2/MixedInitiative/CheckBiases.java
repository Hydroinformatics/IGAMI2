/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.MixedInitiative;

import igami2.DataBase.IndividualDesign;
import igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2.Individual;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author VIDYA
 */
public class CheckBiases {
    
    public int countBiasIndv; //save how many Bias Indv added
    
    private int noOfBiasIndvPairNeeded;
    private LinkedList<Individual> BiasMonitorIndvs;
    private HashMap<Integer,Integer> selectIndv; //save the slected Indv
    private HashMap<Integer,Integer> selectIndvIds; //save the Ids of the Selected Indvs
    public LinkedList<IndividualDesign> BiasMonitorIndvsInfo;
    public LinkedList<IndividualDesign> BiasMonitorIndvsInfoFeedback; //data received from feedback
    private int[] position;
    private LinkedList<Integer> IndvIds;
    
    
    public CheckBiases()
    {
       
       noOfBiasIndvPairNeeded=2;    
       resetBiasIndv();
    }

    public LinkedList<Integer> getIndvIds() {
        return IndvIds;
    }
    
    /*
     * Add bias indv to the feedback data
     */
    void resetBiasIndv()
    {
        position = new int[noOfBiasIndvPairNeeded];
        BiasMonitorIndvsInfo = new LinkedList();
        BiasMonitorIndvsInfoFeedback = new LinkedList();
        countBiasIndv=0;
        selectIndv = new HashMap();
        selectIndvIds = new HashMap();
        BiasMonitorIndvs = new LinkedList();
    }
   
    /*
     * add the same bias pair as before
     */
    
    //Swapping the ids of the bias indv with the new indvs
    
    public synchronized LinkedList<Individual> addBiasIndvFeedback(LinkedList<Individual> Indv) {
        LinkedList<Individual> res = new LinkedList();
        if (BiasMonitorIndvs.size() >= noOfBiasIndvPairNeeded) {
            
            int getMaxId = Indv.getLast().IndvId; //assume the last Indv has max id as generated earlier by the tool
            //modify the ids of the bias Indvs
            for (int i = 0; i < noOfBiasIndvPairNeeded; i++) {
                //save the original Ids
                BiasMonitorIndvs.get(i).IndvId = getMaxId + 1 + i; // to give new unique Id ,why???
                BiasMonitorIndvs.get(i).rating = 0; //reset default
                BiasMonitorIndvs.get(i).confidence = 50;//reset default
            }

            //scramble the Indvs

            Random rn = new Random();
            int len = Indv.size();
            HashMap hp = new HashMap();
            int numIndvPair = 0;
            int countIndv=0;

            while (numIndvPair < 2) {
                int num = Math.abs(rn.nextInt()) % len;
                
                //make even
                if(num%2!=0)
                {
                    num = (num+1)%len; //circular                    
                }

                
                if (!hp.containsKey(num)) {
                    position[numIndvPair] = num; //find two locations where to put the old indvs
                    countIndv = countIndv+2;
                    hp.put(num, countIndv);
                    numIndvPair++;
                }
            }
            
            //sort
            int val = position[0];
            if(position[1]<val)
            {
                position[0]=position[1];
                position[1]=val;
            }
            
            int countPos=0;
            IndvIds = new LinkedList(); //save the actual IndvIds
            for(int i=0;i<Indv.size();i++)
            {
                if(countPos<noOfBiasIndvPairNeeded && i!=position[countPos])
                {
                    res.add(Indv.get(i));
                    IndvIds.add(Indv.get(i).IndvId); //auto boxing
                }                    
                else if(countPos<noOfBiasIndvPairNeeded && i==position[countPos]) //add the
                {
                    //Three Indvs are added, one at this position and other two are bias Indvs
                    Individual in1 = BiasMonitorIndvs.get(countPos*2);
                    in1.rating = 0;
                    in1.confidence =50;
                    res.add(in1);
                    Individual in2 = BiasMonitorIndvs.get(countPos*2+1);
                    in2.rating = 0;
                    in2.confidence = 50;
                    
                    res.add(in2);
                    
                    res.add(Indv.get(i));//current Indv
                    
                    //Add IndvIds
                    IndvIds.add(BiasMonitorIndvsInfo.get(countPos*2).IndvId); //save the orignial Ids
                    IndvIds.add(BiasMonitorIndvsInfo.get(countPos*2+1).IndvId);
                    IndvIds.add(Indv.get(i).IndvId);
                    countPos++;
                }                
                else
                {
                    res.add(Indv.get(i));
                    IndvIds.add(Indv.get(i).IndvId); //auto boxing
                }
                
            }
        } //without Bias Indvs
        else
        {
            IndvIds = new LinkedList(); //save the actual IndvIds
            for(Individual in:Indv)
            {
                IndvIds.add(in.IndvId); //auto boxing
            }          
            res = Indv;
        }
   

        return res;
    }
    
    /*
     * remove bisa indv from the feedback data
     */
    
    public synchronized LinkedList<Individual> removeBiasIndvFeedback(LinkedList<Individual> Indv)
    {
        LinkedList<Individual> res = new LinkedList();
        
        if(Indv.size()>2 && BiasMonitorIndvs.size() >0)
        {
            
            //remove the bias indvs
            int countPos=0;
            for(int i=0;i<Indv.size();i++)
            {
                if(countPos<noOfBiasIndvPairNeeded && i!=(position[countPos]+countPos*2))
                    res.add(Indv.get(i));
                else  if(countPos<noOfBiasIndvPairNeeded)
                {
                    Individual inBias1 = Indv.get(i); //Bias feedback indvs
                    IndividualDesign ind1 = new IndividualDesign(inBias1);
                    Individual inBias2 = Indv.get(i+1);
                    IndividualDesign ind2 = new IndividualDesign(inBias2);
                    BiasMonitorIndvsInfoFeedback.add(ind1);
                    BiasMonitorIndvsInfoFeedback.add(ind2);
                    
                    //reassign the previous Indvid, Rating and confidence values as it is used by Total Indvs
                    
                    Individual in1 = BiasMonitorIndvs.get(countPos*2);
                    IndividualDesign in1Info = BiasMonitorIndvsInfo.get(countPos*2);
                    in1.IndvId = in1Info.IndvId;
                    in1.rating = in1Info.rating;
                    in1.confidence = in1Info.confidence;
                    
                    Individual in2 = BiasMonitorIndvs.get(countPos*2+1);
                    IndividualDesign in2Info = BiasMonitorIndvsInfo.get(countPos*2+1);
                    
                    in2.IndvId = in2Info.IndvId;
                    in2.rating = in2Info.rating;
                    in2.confidence = in2Info.confidence;
                    
                    //res.add(BiasMonitorIndvs.get(countPos*2));
                    //res.add(BiasMonitorIndvs.get(countPos*2+1));
                    i++;//add i twice
                    countPos++;
                } 
                else
                {
                    res.add(Indv.get(i));
                }
                
            }
        }
        
        else
        {
            res = Indv;
        }
        
        
        
        return res;
    }


    //add 4 Indv from last session only, can be modified to add multiple as well
    
    public void addBiasIndv(LinkedList<Individual> IndvPop) 
    {
        resetBiasIndv(); //save only the previous 4 Indvs
        
        Random rn = new Random();
        int len = IndvPop.size();
        int numIndvPair = 0;
        HashMap hp = new HashMap();

        //Add two pairs
        if(len>=20)
        {
            while (numIndvPair < 2) {
            int num = Math.abs(rn.nextInt()) % len;            
            if (!hp.containsKey(num)) {
                //make number even
                if(num%2!=0)
                {
                    num=(num+1)%len;//circular
                }
                Individual in1 = (Individual) IndvPop.get(num); //create a separate indv
                Individual in2 = (Individual) IndvPop.get(num+1); //create a separate indv
                IndividualDesign ind1 = new IndividualDesign(in1);
                IndividualDesign ind2 = new IndividualDesign(in2);
                BiasMonitorIndvsInfo.add(ind1);
                BiasMonitorIndvsInfo.add(ind2);               
                hp.put(num, in1);
                hp.put(num+1, in2);
                BiasMonitorIndvs.add(in1);
                BiasMonitorIndvs.add(in2);
                numIndvPair++;
                countBiasIndv=countBiasIndv+2;
            }
            }
        }
    }
}
