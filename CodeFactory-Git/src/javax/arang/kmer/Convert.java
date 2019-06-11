package javax.arang.kmer;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

public class Convert extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String count = "";
		boolean oddLine = true;
		double numKmers = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (oddLine) {
				count = line.substring(1);
			} else {
				System.out.println(line + "\t" + count);
			}
			oddLine = !oddLine;
			numKmers++;
		}
		
		System.err.println(String.format("%.0f", numKmers) + " kmers proceessed. Bye.");
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar kmerConvert.jar <kmer.dump>");
		System.out.println("\t<kmer.dump>: kmer dump from meryl -Dt -s <target>.mcdat with canu 1.7");
		System.out.println("\t<sysout>: .kmer that can be imported to meryl2 in canu 1.8");
		System.out.println("Arang Rhie, 05-10-2019");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new Convert().go(args[0]);
		} else {
			new Convert().printHelp();
		}
			
	}

}
