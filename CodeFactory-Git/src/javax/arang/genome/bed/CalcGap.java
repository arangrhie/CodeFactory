package javax.arang.genome.bed;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class CalcGap extends IOwrapper {

	static final int FROM = 0;
	static final int TO = 1;
	static final int COV = 2;
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		int gapSize = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			if (tokens[COV].equals("0")) {
				int size = Integer.parseInt(tokens[TO]) - Integer.parseInt(tokens[FROM]) + 1;
				if (size > 200)	gapSize += size;
			}
		}
		fm.writeLine("Total gap size\t" + gapSize);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedCalcGap.jar <in.bed.cov>");
		System.out.println("\t<output>: out.bed.gap");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new CalcGap().go(args[0], args[0].replace(".bed.cov", ".bed.gap"));
		} else {
			new CalcGap().printHelp();
		}

	}

}
