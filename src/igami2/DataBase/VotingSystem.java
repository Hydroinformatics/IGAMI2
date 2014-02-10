/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DataBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author VIDYA
 */
public class VotingSystem {

    private int finalVote;
    private double finalConfidence;

    public VotingSystem() {
    }

    public int getFinalVote() {
        return finalVote;
    }

    public double getFinalConfidence() {
        return finalConfidence;
    }

    void performVoting(LinkedList<IndividualDesign>[] Indvs,int idx) {
        finalVote=0;
        finalConfidence=0;
        List<VoteCount> lst = new ArrayList<VoteCount>();
        VoteCount vote1 = new VoteCount(1, "Rating1");
        VoteCount vote2 = new VoteCount(2, "Rating2");
        VoteCount vote3 = new VoteCount(3, "Rating3");

        for (int i = 0; i < Indvs.length; i++) {
            if (Indvs[i].get(idx).rating == 2) {
                vote2.addVote(i, Indvs[i].get(idx).confidence);
            }else if(Indvs[i].get(idx).rating==3) {
                vote3.addVote(i, Indvs[i].get(idx).confidence);
            }
            else //(Indvs[i].rating == 1) //for no rating or 0 rating assume rating 1
            {
                vote1.addVote(i, Indvs[i].get(idx).confidence);
            }
        }
        vote1.findAverageConf();
        vote2.findAverageConf();
        vote3.findAverageConf();

        lst.add(vote1);
        lst.add(vote2);
        lst.add(vote3);

        Collections.sort(lst);
        int count1 = lst.get(0).getCount();
        int count2 = lst.get(1).getCount();
        if (count1 > count2) {
            this.finalVote = lst.get(0).value;
            this.finalConfidence = lst.get(0).averageConf;
        } else if (count1 == count2)//case of tie
        {
            //Run tie Breaker
            double conf1 = lst.get(0).averageConf;
            double conf2 = lst.get(1).averageConf;
            if (conf1 > conf2) {
                this.finalVote = lst.get(0).value;
                this.finalConfidence = lst.get(0).averageConf;
            } else if (conf2 > conf1) {
                this.finalVote = lst.get(1).value;
                this.finalConfidence = lst.get(1).averageConf;
            } else //choose a random selection
            {
                Random rn = new Random();
                int sel = Math.abs(rn.nextInt(2));
                this.finalVote = lst.get(sel).value;
                this.finalConfidence = lst.get(sel).averageConf;
            }
        } else {
            System.out.println("Something went wrong in the Voting");
        }
    }

    class VoteCount implements Comparable<VoteCount> {

        private int value;
        private int count;
        private String name;
        private List<Integer> candidate;//list of the candidates
        private List<Double> confidence;//their confidence Values
        private double averageConf;

        public VoteCount(int value, String name) {
            this.value = value;
            this.name = name;
            count = 0;
            averageConf = 0;
            candidate = new LinkedList();
            confidence = new LinkedList();
        }

        public int getCount() {
            return count;
        }

        public List<Integer> getCandidate() {
            return candidate;
        }

        public List<Double> getConfidence() {
            return confidence;
        }

        private void findAverageConf() {
            double sum = 0;
            for (int i = 0; i < confidence.size(); i++) {
                sum = sum + confidence.get(i);
            }
            this.averageConf = sum / count;
        }

        public void addVote(int cand, double conf) {
            candidate.add(cand);
            confidence.add(conf);
            count++; //one more vote added
        }

        @Override
        public int compareTo(VoteCount o) {
            return o.count - count;
        }
    }
}
