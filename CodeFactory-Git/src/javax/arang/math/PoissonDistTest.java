package javax.arang.math;

import javax.arang.IO.basic.Wrapper;

import org.apache.commons.math3.distribution.PoissonDistribution;

public class PoissonDistTest extends Wrapper {
	
	PoissonDistribution pd = new PoissonDistribution(50);
	private void test() {
		System.out.println(Integer.MAX_VALUE);
		startTiming();
		for (int i = 1; i <= 1000000000; i++) {
			pd.probability(i);
		}
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
