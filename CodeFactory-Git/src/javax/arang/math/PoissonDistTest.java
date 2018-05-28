package javax.arang.math;

import javax.arang.IO.basic.Wrapper;

import org.apache.commons.math3.distribution.PoissonDistribution;

public class PoissonDistTest extends Wrapper {
	
	PoissonDistribution pd = new PoissonDistribution(31);
	private void test() {
		//System.out.println(Integer.MAX_VALUE);
		startTiming();
		
		double sum = 0;
		for (int i = 1; i < 62; i++) {
			sum += pd.probability(i);
		}
		System.out.println(sum);
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
