package javax.arang.kmer;

import javax.arang.IO.basic.Wrapper;

public class GenerateKmers extends Wrapper {

	private static int k = 16;
	
	private void generate() {
		
		double allKmers = Math.pow(4, k);
		int i;
		int j;
		int val;
		int base;
		
		for (i = 0; i < allKmers; i++) {
			val = i;
			//System.out.print(val + "\t");
			for (j = 0; j < k; j++) {
				base = val % 4;
				printBase(base);
				//System.out.print(val % 4);
				val = val / 4;
				//System.out.print(val);
			}
			System.out.println("\t1");
		}
	}
	
	private void printBase(int base) {
		if (base == 0) {
			System.out.print("A");
		} else if (base == 1) {
			System.out.print("T");
		} else if (base == 2) {
			System.out.print("G");
		} else if (base == 3) {
			System.out.print("C");
		}
	}
	public static void main(String[] args) {
		GenerateKmers generator = new GenerateKmers();
		if (args.length == 1) {
			k=Integer.parseInt(args[0]);
			generator.startTiming();
			generator.generate();
			generator.printTiming();
		} else {
			generator.printHelp();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar kmerGenerateKmers.jar <k-size>");
		System.out.println("\tGenerate any kmer size of <k-size>.");
		System.out.println("\t<stdout>: kmer\t1");
		System.out.println("\t*This code was made to generate all kmer sets to import in meryl.");
		System.out.println("Arang Rhie, 2019-03-05. arrhie@gmail.com");
	}

}
