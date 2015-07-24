package javax.arang.txt;

import java.util.ArrayList;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class PasteColumns extends INOwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtPasteColumns.jar <col_to_lookup> <col_to_paste> <out.txt> [*input.txt]");
		System.out.println("\t<out.txt>: file containing <col_to_paste> from each file.");
		System.out.println("\t<col_to_lookup>: 1-based, key column idx to look up. DEFAULT=1");
		System.out.println("\t<col_to_paste>: 1-based, column idx to paste. DEFAULT=2");
		System.out.println("\t*Currently, <col_to_lookup> from the first input file will be pated.");
		System.out.println("Arang Rhie, 2015-03-20. arrhie@gmail.com");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		for (FileReader fr : frs) {
			fm.write("\t" + fr.getFileName());
		}
		fm.writeLine();

		String line;
		String[] tokens;
		while (frs.get(0).hasMoreLines()) {
			line = frs.get(0).readLine();
			tokens = line.split(RegExp.WHITESPACE);
			fm.write(tokens[colToLookup] + "\t" + tokens[colToPaste]);
			for (int i = 1; i < frs.size(); i++) {
				FileReader fr = frs.get(i);
				fm.write("\t" + fr.readLine().split(RegExp.WHITESPACE)[colToPaste]);
			}	
			fm.writeLine();
		}
	}

	private static int colToLookup = 0;
	private static int colToPaste = 1;
	public static void main(String[] args) {
		if (args.length > 3) {
			String[] infiles = new String[args.length - 3];
			for (int i = 3; i < args.length; i++) {
				infiles[i-3] = args[i];
			}
			colToLookup = Integer.parseInt(args[0]) - 1;
			colToPaste = Integer.parseInt(args[1]) - 1;
			new PasteColumns().go(infiles, args[2]);
		} else {
			new PasteColumns().printHelp();
		}
	}

}
