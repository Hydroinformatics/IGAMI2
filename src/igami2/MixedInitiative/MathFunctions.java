/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.MixedInitiative;

/**
 *
 * @author VIDYA
 */
public class MathFunctions {

    public MathFunctions() {
    }

    public static double median(double[] m) {// take sorted array
        int middle = m.length / 2;
        if (m.length % 2 == 1) {
            return m[middle];
        } else {
            return (m[middle - 1] + m[middle]) / 2.0;
        }
    }

    public double Mean(double array[]) {
        double total = 0;
        for (int i = 0; i < array.length; i++) {
            total = total + array[i];
        }
        double mean = total / array.length;
        return mean;
    }

    public double StandardDeviation(double array[]) {
        double res = 0;
        double mean = Mean(array);
        //System.out.println("Mean is: " + mean);
        double d1 = 0;
        double d2 = 0;
        double sum = 0;
        for (int i = 0; i < array.length; i++) {
            d2 = (mean - array[i]) * (mean - array[i]);
            d1 = d2 + d1;
        }
        res = Math.sqrt((d1 / (array.length - 1)));
        //System.out.println("Standard Deviation: " + res);
        return res;
    }
}
