package javax.arang.txt;

import java.util.HashSet;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class SplitByFile extends R2wrapper {

	private static int colIdx = 0;
	private static String out = "";
	
	@Override
	public void hooker(FileReader fr1, FileReader fr2) {
		HashSet<String> splitBy = new HashSet<String>();
		
		while (fr2.hasMoreLines()) {
			splitBy.add(fr2.readLine());
		}
		
		String line;
		String[] tokens;
		int fIdx = 1;
		FileMaker fm = new FileMaker(out + fIdx++);
		
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			tokens = line.split(RegExp.TAB);
			if (splitBy.contains(tokens[colIdx])) {
				// Contains the specified line: Write to a new file
				fm.closeMaker();
				fm = new FileMaker(out + fIdx++);
				splitBy.remove(tokens[colIdx]);
			}
			fm.writeLine(line);
		}
		fm.closeMaker();
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar txtSplitByFile.jar <input> <split_by> <col_idx> <out>");
		System.err.println("\t<input>: Target file to split. Tab delemited. Use - for stdin.");
		System.err.println("\t<split_by>: File containing unique strings to match <col_idx> in <input>.");
		System.err.println("\t<col_idx>: Column index (1-based) to look up in <input>.");
		System.err.println("\t<out>: Output file prefix");
		System.err.println("\tOutput file(s) will be generated as <out>.#, with # beginning from 1.");
		System.err.println("\tLines beginning with <split_by> will be stored in a different output file.");
		System.err.println("Arang Rhie, 2020-07-01. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length != 4) {
			new SplitByFile().printHelp();
		} else {
			colIdx = Integer.parseInt(args[2]) - 1;
			out = args[3] + ".";
			new SplitByFile().go(args[0], args[1]);
		}
	}

}
