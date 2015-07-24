package javax.arang.base;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ParseUncoveredRegionFromLog extends IOwrapper {


	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar baseParseUncoveredRegionFromLog.jar <in.log> <out.bed>");
		System.out.println("Parse the log file generated from bamBaseDetph.jar and makes a file containing uncovered region.");
		System.out.println("\t<in.log>: log file generated from bamBaseDetph.jar");
		System.out.println("\t<out.bed>: bed formatted file containing uncovered region (no reads are aligned in this region).");
		System.out.println("Arang Rhie, 2014-03-26. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			new ParseUncoveredRegionFromLog().printHelp();
		} else {
			new ParseUncoveredRegionFromLog().go(args[0], args[1]);
		}
	}

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("Bam file")) {
				tokens = line.split(" ");
				fm.writeLine(tokens[5] + "\t" + tokens[6] + "\t" + tokens[8]);
			}
		}
	}

}
