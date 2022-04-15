package javax.arang.genome.snp;

import java.util.StringTokenizer;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ShiftPosition extends IOwrapper {

	private static int shift;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			new ShiftPosition().printHelp();
			System.exit(-1);
		}
		shift = Integer.parseInt(args[1]);
		new ShiftPosition().go(args[0], args[2]);
	}

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		StringTokenizer st;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#")) {
				fm.writeLine(line);
				continue;
			}
			st = new StringTokenizer(line);
			String chr = st.nextToken();
			int pos1 = Integer.parseInt(st.nextToken()) + shift;
			int pos2 = Integer.parseInt(st.nextToken()) + shift;
			String left = "";
			while (st.hasMoreTokens()) {
				left += "\t" + st.nextToken();
			}
			fm.writeLine(chr + "\t" + pos1 + "\t" + pos2 + left);
			// fm.writeLine("chr20\t" + pos1 + left);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar shiftPostition.jar <inFile> <shiftN> <outFile>");
		System.out.println("\t<inFile>: variant file, where positions are wrongly written");
		System.out.println("\t<shiftN>: shift N positions");
		System.out.println("\t<outFile>: corrected out file");
	}

}
