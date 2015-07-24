package javax.arang.genome.coverage;

import java.util.StringTokenizer;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

public class TotalBases extends Rwrapper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new TotalBases().go(args[0]);
		} else {
			new TotalBases().printHelp();
		}
	}

	@Override
	public void hooker(FileReader fr) {
		String line;
		StringTokenizer st;
		String chr = "chr";
		int totalBases = 0;
		
		while(fr.hasMoreLines()) {
			line = fr.readLine().toString();
			st = new StringTokenizer(line);
			chr = st.nextToken();	// chr
			totalBases += (Integer.parseInt(st.nextToken())) * (Integer.parseInt(st.nextToken()));
		}
		System.out.println("Total # of bases (" + chr + ")\t" + totalBases);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar totalBases.jar <inFile>");
		System.out.println("\t<inFile>: list of numbers by coverage");
		System.out.println("\tExample: chr20\t29\t13 means");
		System.out.println("\t\t13 base positions have sequencing depth of 29");
	}

}
