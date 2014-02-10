package igami2.Optimization.DPLA;


import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Random;

public class PursuitAutomata implements GenericAutomata {

    int id;
    int nact;//number of actions
    int iter = 0;//number of iterations
    double[] d;//environment success probabilities
    double[][] p;//p vector at different iterations
    int[] chosen;//no of times this action was chosen
    int[] rewarded;//no of times this action was rewarded
    int[] unit;
    int[] maxIndex;
    double[] dhat;
    double a;//step size
    int response;//response from the environment (reward or penalty)
    int select = 0;//action selected
    Writer output = null;
    String text;
    String filePath;
    File file;
    static Random r = new Random(System.currentTimeMillis());
    double rand;
    double acc = 0.0;

    PursuitAutomata(int id1, int nact1, double a1) {
        int j;
        nact = nact1;

        id = id1;
        d = new double[nact];
        p = new double[nact][2];
        chosen = new int[nact];
        rewarded = new int[nact];
        dhat = new double[nact];
        unit = new int[nact];
        maxIndex = new int[nact];
        a = a1;


        //setup the output file
        text = "Iteration[n] p1[n] p2[n]\n";
        filePath = "C:\\" + "automata" + new Integer(id).toString() + ".txt";
        //file = new File(filePath);
        try {
            //output = new BufferedWriter(new FileWriter(file));
            //output.write(text);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //initialize

        for (j = 0; j < nact; j++) {
            p[j][0] = 1.0 / nact;
            chosen[j] = 0;
            rewarded[j] = 0;
            dhat[j] = 0.0;
            unit[j] = 0;
        }

        //System.out.print("Automata "+id+ " ");
        for (j = 0; j < nact; j++) {
            //System.out.print(p[j][0]+ " ");
            if (j == 0) {
                text = new Integer(iter).toString() + " " + new Double(p[j][0]).toString();
            } else {
                text = text + " " + new Double(p[j][0]).toString();
            }
        }
        //System.out.println();
        text = text + "\n";
        try {
            //output.write(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("==========================");
        text = "";

    }

    PursuitAutomata(int id1, int nact1, double a1, double[] din) {
        int j;
        nact = nact1;

        id = id1;
        d = new double[nact];
        p = new double[nact][2];
        chosen = new int[nact];
        rewarded = new int[nact];
        dhat = new double[nact];
        unit = new int[nact];
        a = a1;

        for (j = 0; j < nact; j++) {
            d[j] = din[j];
        }

        //initialize

        for (j = 0; j < nact; j++) {
            p[j][0] = 1.0 / nact;
            chosen[j] = 0;
            rewarded[j] = 0;
            dhat[j] = 0.0;
            unit[j] = 0;
        }

    }

    int max(double[] data) {
        int result = 0, j, i;
        double maximum = data[0];   // start with the first value
        for (i = 1; i < data.length; i++) {
            if (data[i] > maximum) {
                maximum = data[i];
                result = i;   // new maximum
            }
        }
        return result;
    }

    int getPayoff() {
        int response = 0;
        double dtemp;
        rand = random();
        dtemp = d[select];
        if (rand <= dtemp) {
            response = 1;//reward

        } else {
            response = 0;//penalty

        }
        return response;
    }

    public int selectAction() {
        int j;
        //select action

        rand = random();

        /*
         * VIDYA
         * Seems to be biased towards later actions as the later actions will replace the previous
         */
        acc = 0.0;
        for (j = 0; j < nact; j++) {
            acc = acc + p[j][0];
            if (rand <= acc) {
                select = j; //this particular region selected or not based on the rand value less than the action prob vector
                break;
            }
        }
        acc = 0.0;
        chosen[select] = chosen[select] + 1;
        return select;
    }

    public void doLearning(int response1, boolean print) {
        int maxIndex, j;

        if (response1 == 1)//reward
        {
            rewarded[select] = rewarded[select] + 1;
        }

        dhat[select] = rewarded[select] / (double) chosen[select];

        for (j = 0; j < nact; j++) {
            unit[j] = 0;
        }

        maxIndex = max(dhat);
        unit[maxIndex] = 1;

        for (j = 0; j < nact; j++) {
            p[j][1] = p[j][0] + a * (unit[j] - p[j][0]);
        }

        //copy the values from 1 back to 0 zero for next iteration
        for (j = 0; j < nact; j++) {
            p[j][0] = p[j][1];
            p[j][1] = 0.0;
        }

        if (print) {
            System.out.print("Automata " + id + " ");
        }
        for (j = 0; j < nact; j++) {
            if (print) {
                System.out.print(p[j][0] + " ");
            }

            if (j == 0) {
                text = new Integer(iter).toString() + " " + new Double(p[j][0]).toString();
            } else {
                text = text + " " + new Double(p[j][0]).toString();
            }
        }
        if (print) {
            System.out.println();
        }

        //try{output.write(text+ "\n");}catch(Exception e){e.printStackTrace();}
        //System.out.println("==========================");
        text = "";
        iter++;

    }

    public void doLearningBySettingDhat(double[] dhat1, boolean print) {
        int maxIndex, j;

        for (j = 0; j < dhat1.length; j++) {
            dhat[j] = dhat1[j];
        }



        for (j = 0; j < nact; j++) {
            unit[j] = 0;
        }

        maxIndex = max(dhat);
        unit[maxIndex] = 1;

        for (j = 0; j < nact; j++) {
            p[j][1] = p[j][0] + a * (unit[j] - p[j][0]);
        }

        //copy the values from 1 back to 0 zero for next iteration
        for (j = 0; j < nact; j++) {
            p[j][0] = p[j][1];
            p[j][1] = 0.0;
        }

        if (print) {
            System.out.print("Automata " + id + " ");
        }
        for (j = 0; j < nact; j++) {
            if (print) {
                System.out.print(p[j][0] + " ");
            }

            if (j == 0) {
                text = new Integer(iter).toString() + " " + new Double(p[j][0]).toString();
            } else {
                text = text + " " + new Double(p[j][0]).toString();
            }
        }
        if (print) {
            System.out.println();
        }

        //try{output.write(text+ "\n");}catch(Exception e){e.printStackTrace();}
        //System.out.println("==========================");
        text = "";
        iter++;

    }

    void doLearningAlt(int unitIndex, boolean print) {
        int j;

        for (j = 0; j < nact; j++) {
            unit[j] = 0;
        }

        unit[unitIndex] = 1;


        for (j = 0; j < nact; j++) {
            p[j][1] = p[j][0] + a * (unit[j] - p[j][0]);
        }

        //copy the values from 1 back to 0 zero for next iteration
        for (j = 0; j < nact; j++) {
            p[j][0] = p[j][1];
            p[j][1] = 0.0;
        }

        if (print) {
            System.out.print("Automata " + id + " ");
        }

        for (j = 0; j < nact; j++) {
            if (print) {
                System.out.print(p[j][0] + " ");
            }
            //if(j==0)text = new Double(p[j][0]).toString();
            //else text = text + " " + new Double(p[j][0]).toString();
        }
        if (print) {
            System.out.println();
        }

        text = text + "\n";
        try {
            //output.write(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("====================");
        text = "";

    }//doLearning

    static double random() {
        double result;
        result = r.nextDouble();
        return result;
    }

    public int convergedTo() {
        int result = 0;
        double maximum = p[0][0];   // start with the first value
        for (int i = 1; i < nact; i++) {
            if (p[i][0] > maximum) {
                maximum = p[i][0];
                result = i;   // new maximum
            }
        }
        return result;
    }

    public boolean hasConverged(double threshold) {
        boolean result = false;
        for (int i = 0; i < nact; i++) {
            if (p[i][0] >= threshold) {
                result = true;
                return result;
            }
        }
        return result;

    }

    double maxActionValue() {
        double max;
        max = p[0][0];
        for (int i = 0; i < nact; i++) {
            if (p[i][0] > max) {
                max = p[i][0];
            }
        }
        return max;

    }

    double getP(int index) {
        return p[index][0];
    }

    void setP(int index, double value) {
        p[index][0] = value;

    }

    void setP(double[] value) {
        int i;
        if (value.length == p.length) {
            for (i = 0; i < p.length; i++) {
                p[i][0] = value[i];
            }

        }

    }

    double getDHat(int index) {
        return dhat[index];
    }

    int getUnit(int index) {
        return unit[index];
    }

    int whichUnit() {
        int result = nact + 1;
        for (int i = 0; i < nact; i++) {
            if (unit[i] == 1) {
                result = i + 1;
            }
        }
        return result;
    }

    void reset() {
        for (int j = 0; j < nact; j++) {
            chosen[j] = 0;
            rewarded[j] = 0;
            dhat[j] = 0.0;
        }

    }

    int getSelectedAction() {
        return select;
    }

    public void printP() {
        System.out.print("Automaton " + id + " ");
        for (int i = 0; i < nact; i++) {
            System.out.print(p[i][0] + " ");
        }
        System.out.println();
    }

    void closeFile() {
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String argv[]) throws IOException {
        int payoff, counter, noOfExperiments = 100, avg = 0;



        for (int i = 0; i < noOfExperiments; i++) {

            PursuitAutomata p = new PursuitAutomata(0, 3, 0.005, new double[]{0.4, 0.3, 0.9});
            counter = 0;
            while (!p.hasConverged(0.99)) //while(true)
            {
                counter++;
                p.selectAction();
                payoff = p.getPayoff();
                p.doLearning(payoff, false);
            }//while end
            avg = avg + counter;
            //System.out.println(p.convergedTo());

            //p.printP();
        }

        avg = avg / noOfExperiments;
        System.out.println(avg);
        //for(int i=0;i<nact;i++)
        //{
        //System.out.println(p.getDHat(i));
        //}


    }//main ends
}//ApproximatePursuitAutomata