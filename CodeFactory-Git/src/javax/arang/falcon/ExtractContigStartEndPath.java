package javax.arang.falcon;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ExtractContigStartEndPath extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line = fr.readLine();
		String[] tokens = line.split(RegExp.WHITESPACE);
		String contig = tokens[0];
		fm.writeLine(line);
		String prevLine = line;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			if (!contig.equals(tokens[0])) {
				fm.writeLine(prevLine);
				fm.writeLine(line);
				contig = tokens[0];
			}
			prevLine = line;
		}
		fm.writeLine(line);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar falconExtractContigStartEndPath.jar <p_ctg_tiling_path> <p_ctg_tiling_path_start_end>");
		System.out.println("\tExtract the starting pread and end pread");
		System.out.println("\t<p_ctg_tiling_path>: input primary tiling path from falcon. could be a_ctg_tiling_path.");
		System.out.println("\t<p_ctg_tiling_path_start_end>: first and last lines of each contig tiling path");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ExtractContigStartEndPath().go(args[0], args[1]);
		} else {
			new ExtractContigStartEndPath().printHelp();
		}
	}

}
