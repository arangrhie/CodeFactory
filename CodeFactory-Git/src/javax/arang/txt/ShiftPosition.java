package javax.arang.txt;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ShiftPosition extends IOwrapper {

	static int COL_N = 0;
	static int OFFSET = 0;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			if (line.length() < 2)	continue;
			tokens = line.split("\t");
			String newLine = "";
			for (int i = 0; i < COL_N; i++) {
				newLine = newLine + (tokens[i] + "\t");
			}
			try {
				newLine = newLine + ((Integer.parseInt(tokens[COL_N]) + OFFSET) + "\t");
			} catch (NumberFormatException e) {
				System.out.println(tokens[COL_N] + " is not an integer value. skip this line.");
				newLine = newLine + tokens[COL_N] + "\t";
			}
			if (COL_N < tokens.length - 1) {
				for (int i = COL_N + 1; i < tokens.length; i++) {
					newLine = newLine + (tokens[i] + "\t");
				}
			}
			fm.writeLine(newLine.trim());
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar shiftPositon.jar <in> <num_column> <shift_offset>");
		System.out.println("\t<num_column>: 0-based column number");
		System.out.println("\t<output>: in.<shift_offset>");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3) {
			COL_N = Integer.parseInt(args[1]);
			OFFSET = Integer.parseInt(args[2]);
			new ShiftPosition().go(args[0], args[0] + "." + args[2]);
		} else {
			new ShiftPosition().printHelp();
		}
	}

}
