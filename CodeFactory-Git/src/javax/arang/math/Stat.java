package javax.arang.math;

import java.util.ArrayList;

public class Stat {
	
	/***
	 * Get the basic statistical standard deviation.
	 * sd = 1/n * square root of (sum of (power of (x - average) by 2))
	 * where
	 * n is the number of values,
	 * x is a value,
	 * average is the average of the values
	 * @param values assumes a sorted list of values
	 * @param average average value
	 * @return standard deviation of the given list of values
	 */
	public static double getSD(ArrayList<Double> values, double average) {
		Double standardDeviation = 0d;
		for(double dist: values) {
			standardDeviation += Math.pow(dist - average, 2);
        }

        return Math.sqrt(standardDeviation/values.size());
	}
}
