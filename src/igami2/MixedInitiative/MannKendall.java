package igami2.MixedInitiative;

/*
 * Copyright (C) 2004-2007 Paolo Boldi, Massimo Santini and Sebastiano Vigna
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
// RELEASE-STATUS: DIST
/**
 * Computes Kendall's &tau; between two rankings. More precisely, the static
 * methods of this class compute the generalisation described by Kendall in
 * &ldquo;The treatment of ties in ranking problems&rdquo;, <i>Biometrika</i>
 * 33:239&minus;251, 1945.
 *
 * <p>Note that in the literature the 1945 generalisation is often called
 * &tau;<sub><i>b</i></sub>, and &tau; is reserved for the original coefficient
 * (&ldquo;A new measure of rank correlation&rdquo;, <i>Biometrika</i>
 * 30:81&minus;93, 1938). But this distinction is pointless, as the 1938 paper
 * defines &tau; only for rankings with no ties, and the generalisation in the
 * 1945 paper reduces exactly to the original definition if there are no ties.
 *
 * <P>Given two lists of doubles expressing two rankings for a list of items
 * (higher ranks come first), this class provides {@linkplain #compute(double[], double[]) static methods to compute efficiently Kendall's &tau;}
 * using an {@link ExchangeCounter}. It is also possible to require all
 * comparison are limited to a {@linkplain #compute(CharSequence, CharSequence, int) given number of binary digits}.
 *
 * <p>More precisely, given <var>r</var><sub><var>i</var></sub> and
 * <var>s</var><sub><var>i</var></sub> ( <var>i</var> = 1, 2,&nbsp;&hellip;,
 * <var>n</var> ), we say that a pair (<var>i</var>, <var>j</var>) is <ul>
 * <li><em>concordant</em> iff <var>r</var><sub><var>i</var></sub> &minus;
 * <var>r</var><sub><var>j</var></sub> and <var>s</var><sub><var>i</var></sub>
 * &minus; <var>s</var><sub><var>j</var></sub> are both non-zero and have the
 * same sign; <li><em>discordant</em> iff <var>r</var><sub><var>i</var></sub>
 * &minus; <var>r</var><sub><var>j</var></sub> and
 * <var>s</var><sub><var>i</var></sub> &minus;
 * <var>s</var><sub><var>j</var></sub> are both non-zero and have opposite
 * signs; <li> an <em><var>r</var>-tie</em> iff
 * <var>r</var><sub><var>i</var></sub> &minus;
 * <var>r</var><sub><var>j</var></sub> = 0; <li> an <em><var>s</var>-tie</em>
 * iff <var>s</var><sub><var>i</var></sub> &minus;
 * <var>s</var><sub><var>j</var></sub> = 0; <li> a <em>joint tie</em> iff
 * <var>r</var><sub><var>i</var></sub> &minus;
 * <var>r</var><sub><var>j</var></sub> = 0 and
 * <var>s</var><sub><var>i</var></sub> &minus;
 * <var>s</var><sub><var>j</var></sub> = 0. </ul>
 *
 * <P>Let <var>C</var>, <var>D</var>, <var>T<sub>r</sub></var>,
 * <var>T<sub>s</sub></var>, <var>J</var> be the number of concordant pairs,
 * discordant pairs, <var>r</var>-ties, <var>s</var>-ties and joint ties,
 * respectively, and <var>N</var> = <var>n</var>(<var>n</var> &minus; 1)/2. Of
 * course
 * <var>C</var>+<var>D</var>+<var>T<sub>r</sub></var>+<var>T<sub>s</sub></var>
 * &minus; <var>J</var> = <var>N</var>. Kendall's &tau; is now <blockquote>
 * &tau; = (<var>C</var> &minus; <var>D</var>) / [(<var>N</var> &minus;
 * <var>T<sub>r</sub></var>)(<var>N</var> &minus;
 * <var>T<sub>s</sub></var>)]<sup>1/2</sup> </blockquote>
 *
 * <p>A main method is provided for command-line usage.
 */
import java.util.LinkedList;

/**
 *
 * @author VIDYA
 */
public class MannKendall {

    double meanConfThreshold = 0.75;
    // This list stores all S values of MannKendall test done on confidence ratings trends
    public LinkedList Svalues_MannKendall = new LinkedList();
    // This list stores all Z values of MannKendall test done on confidence ratings trends
    public LinkedList Zvalues_MannKendall = new LinkedList();

    public int S = 0;
    public double Z = 0.0;
    
    public void addSvalues_MannKendall(double Svalue) {
        Svalues_MannKendall.add(new Double(Svalue));
    }

    public double getSvalues_MannKendall(int i) {
        return ((Double) Svalues_MannKendall.get(i)).doubleValue();
    }

    public void addZvalues_MannKendall(double Zvalue) {
        Zvalues_MannKendall.add(new Double(Zvalue));
    }

    public double getZvalues_MannKendall(int i) {
        return ((Double) Zvalues_MannKendall.get(i)).doubleValue();
    }
    // for now I am setting this as hard code. In future, if anyone wants a general list of the theoretical values,
    // then can make this input friendly. These arrays are for one tailed distrib
    double[] normalDistribAlphaProb = {0.100, 0.050, 0.025, 0.010, 0.005};
    double[] normalDistribZ = {1.282, 1.645, 1.960, 2.326, 2.576};

    public MannKendall() {
    }

    public boolean startMannKendallTest(LinkedList stddevList) throws Exception {
        double[] stddevConf = new double[stddevList.size()];
        for (int i = 0; i < stddevList.size(); i++) {
            stddevConf[i] = (Double) stddevList.get(i);
        }
        return (doMannKendallTest(stddevConf));
    }

    /*
     * return true for HDM and false for SDM
     */
    public boolean doMannKendallTest(double[] stddevConf) throws Exception {

        boolean realExpert = true;//default is Real Human
        


        // calculate S and Z for standard deviations in confidence ratings using mann kendall for all past CBM cycles
        S = getS_mannKendallTest(stddevConf);
        Z = getZ_mannKendallTest(S, stddevConf);

        int size = Svalues_MannKendall.size();
        Integer s=0;
        double z=0;
        double s1=0;
        if(size>0) //assumption that s and z values can't be same for the last two session, use to filter the resume values
        {
            s = (Integer) Svalues_MannKendall.get(size-1);
            z = (Double) Zvalues_MannKendall.get(size-1);
                     
        }
        if(s!=S && z!=Z)
        {
                Svalues_MannKendall.add(new Integer(S));
                Zvalues_MannKendall.add(new Double(Z));
        }
        //else use the previous values

        if (Zvalues_MannKendall.size() > 1) {
            System.out.println("Previous " + ", S : " + Svalues_MannKendall.get(Svalues_MannKendall.size() - 2) + " Z: " + Zvalues_MannKendall.get(Zvalues_MannKendall.size() - 2));
        }
        System.out.println("current " + ", S : " + S + " Z: " + Z);
        // start only if morethan 2 available.
        if (S < 0 && Zvalues_MannKendall.size() > 1) {//we are not interested in S> or == Zero
            // now check for any significant drop in Z of latest entry for standard deviations of confidence,
            // compared to significance level of previous introspection Z
            int numZInList = Zvalues_MannKendall.size();
            int currSigLevel = 0;
            int prevSigLevel = 0;
            if (numZInList > 1) {
                currSigLevel = getSignificLevel(((Double) (Zvalues_MannKendall.get(numZInList - 1))).doubleValue());
                prevSigLevel = getSignificLevel(((Double) (Zvalues_MannKendall.get(numZInList - 2))).doubleValue());
            } else {
                currSigLevel = getSignificLevel(((Double) (Zvalues_MannKendall.get(numZInList - 1))).doubleValue());
                prevSigLevel = -1;
            }

            System.out.println(" currSigLevel  : " + currSigLevel + " & prevSigLevel: " + prevSigLevel);

            // check for drop in significance levels, which indicates that significant uncertainty has arisen in confidence ratings
            if (currSigLevel < prevSigLevel) {
                System.out.println(" currSigLevel < prevSigLevel : " + currSigLevel + " < " + prevSigLevel);
                System.out.println("Thus realExpert = true ");
                realExpert = true;
            } // If stdev has a decreasing trend and its Z is above the minimum significant level of 0.1
            else if ((S < 0) && (Z >= normalDistribZ[0])) {
                System.out.println(" (S < 0) && (Z >= normalDistribZ[0]) " + ", S : " + Svalues_MannKendall.get(Svalues_MannKendall.size() - 1) + " Z: " + Zvalues_MannKendall.get(Zvalues_MannKendall.size() - 1));
                System.out.println("Thus realExpert = false ");// Now use the SDM
                realExpert = false;
            } // otherwise continue to using humans till Z of stdev of confidence ratings is above the minimum required level normalDistribZ[0]
            else {
                realExpert = true;
            }
        }
        System.out.println("HDM Selected is " + realExpert);
        return realExpert;
    }

    public int getS_mannKendallTest(double[] data) {
        int S = 0;
        int n = data.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int val = sign(data[j] - data[i]);
                S += val;
            }
        }
        return S;

    }

    public double getZ_mannKendallTest(int S, double[] data) {

        int n = data.length;
        double varianceS = (n / 18.) * (n - 1.) * (2. * n + 5.);
        //Integer SInt = new Integer(Math.abs(S));
        //Actual value is to be used instead of its abs
        double Z=0; //for S=0
        if(S<0)
            //Z = (SInt.doubleValue() + 1) / Math.sqrt(varianceS);
            Z = (S+1) / Math.sqrt(varianceS);
        else if(S>0)
            //Z = (SInt.doubleValue() - 1) / Math.sqrt(varianceS);
            Z = (S-1) / Math.sqrt(varianceS);
        return Math.abs(Z);
    }

    /**
     * Return 1 if positive, 0 if zero, -1 if negative.
     */
    int sign(double num) {
        if (num > 0.0) {
            return 1;
        } else if (num < 0.0) {
            return -1;
        }
        return 0;
    }

    /**
     * Returns the 'n' element id in arrays "normalDistribAlphaProb" = { 0.100,
     * 0.050, 0.025, 0.010, 0.005} and "normalDistribZ" = { 1.282, 1.645, 1.960,
     * 2.326, 2.576}, for which Z of data is greater than the nth entry in
     * normalDistribZ but smaller than n+1th entry in normalDistribZ. If Z <
     * normalDistribZ[0], then n = -1. "normalDistribAlphaProb" stores the
     * corresponding significance levels for z values in "normalDistribZ". *
     */
    public int getSignificLevel(double zz) {
        int sigLevel = -1;
        for (int i = 0; i < normalDistribZ.length; i++) {
            if (zz> normalDistribZ[i]) {
                sigLevel = i;
            }
        }
        return sigLevel;
    }
}
