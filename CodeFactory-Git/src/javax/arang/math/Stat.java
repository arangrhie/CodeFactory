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
	public static double getSD(ArrayList<Integer> values, double average) {
		double sa = 0;	// x - average
		double sd;
		int n = values.size();
		for (int i = 1; i < n; i++) {
			sa += Math.pow(values.get(i) - average, 2);
		}
		sd = Math.sqrt(sa) / n;
		return sd;
	}
}
