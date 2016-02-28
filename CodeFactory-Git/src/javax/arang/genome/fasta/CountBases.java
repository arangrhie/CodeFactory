package javax.arang.genome.fasta;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

public class CountBases extends Rwrapper {

	private static final short BASE_A = 0;
	private static final short BASE_T = 1;
	private static final short BASE_G = 2;
	private static final short BASE_C = 3;
	private static final short BASE_N = 4;
	
	@Override
	public void hooker(FileReader fr) {
		double[] counts = new double[5];
		
		String line;
		char base;
		
		
		while (fr.hasMoreLines()) {
			line = fr.readLine().trim();
			if (line.startsWith(">")) {
				continue;
			}
			for (int i = 0; i < line.length(); i++) {
				base = line.charAt(i);
				counts[getBaseIdxToCount(base)]++;
			}
		}
		
		System.out.println("\tA\tT\tG\tC\tN");
		for (int i = 0; i < counts.length; i++) {
			System.out.print("\t" + counts[i]);
		}
		System.out.println();
	}

	private int getBaseIdxToCount(char base) {
		switch (base) {
		case 'A':
		case 'a':
			return BASE_A;
		case 'T':
		case 't':
			return BASE_T;
		case 'G':
		case 'g':
			return BASE_G;
		case 'C':
		case 'c':
			return BASE_C;
		case 'N':
		case 'n':
			return BASE_N;
		}
		return -1;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar -Xmx1g fastaCountBases.jar <in.fasta>");
		System.out.println("\tCount the number of A/T/G/C bases. Results will be printed out.");
		System.out.println("Arang Rhie, 2015-11-09. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new CountBases().go(args[0]);
		} else {
			new CountBases().printHelp();
		}
	}

}
