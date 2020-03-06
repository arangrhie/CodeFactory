package javax.arang.math;

import javax.arang.IO.basic.Wrapper;

import org.apache.commons.math3.distribution.PoissonDistribution;

public class PoissonDistTest extends Wrapper {
	
	PoissonDistribution pd = new PoissonDistribution(63);
	private void test() {
		//System.out.println(Integer.MAX_VALUE);
		startTiming();
		
		System.out.println(100*pd.probability(63));
	
		
		/***
		double sum = 0;
		for (int i = 1; i < 10; i++) {
			sum += pd.probability(i) * 25745942 * 2;
		}
		System.out.println(sum);
		***/
		printTiming();
	}

	public static void main(String[] args) {
		new PoissonDistTest().test();
	}

	@Override
	public void printHelp() {
		System.out.println("Test poisson distribution generation");
	}

}
