package javax.arang.falcon;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ExtractFirstTwoPreads extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String contig = "";
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			if (!contig.equals(tokens[0])) {
				fm.writeLine(line);
				contig = tokens[0];
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar falconExtractFirstTwoPreads.jar <p_ctg_tiling_path> <p_unused_tiling_path>");
		System.out.println("\t<p_ctg_tiling_path>: Output of falcon. May use <a_ctg_tiling_path> also.");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ExtractFirstTwoPreads().go(args[0], args[1]);
		} else {
			new ExtractFirstTwoPreads().printHelp();
		}
	}

}
