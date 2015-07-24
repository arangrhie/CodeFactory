package javax.arang.genome.coverage;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

public class ColumnSum extends Rwrapper {

	static int COL_N = 0;
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		int totalSum = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			if (line.equals(""))	continue;
			tokens = line.split("\t");
			totalSum += Integer.parseInt(tokens[COL_N]);
		}
		
		System.out.println("Total sum of column " + (COL_N + 1) + " in " + fr.getFileName() + "\t" + totalSum);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar columnSum.jar <in.cov> <column_num>");
		System.out.println("\t<in.cov>: coverage file, or any tab-delemeted file containing numbers");
		System.out.println("\t<column_num>: the 1-based column number");
		System.out.println("\toutput is the total sum of the column.");

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			COL_N = Integer.parseInt(args[1]) - 1;
			new ColumnSum().go(args[0]);
		} else {
			new ColumnSum().printHelp();
		}
	}

}
