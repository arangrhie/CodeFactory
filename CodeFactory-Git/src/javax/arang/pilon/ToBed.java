package javax.arang.pilon;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToBed extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		String[] afterTokens;
		int start;
		int end;
		String type = "";
		String contig;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			afterTokens = tokens[1].split(":");
			contig = tokens[1].substring(0, tokens[1].lastIndexOf(":"));
			if (afterTokens[afterTokens.length - 1].contains("-")) {
				start = Integer.parseInt(afterTokens[afterTokens.length - 1].split("-")[0]) - 1;
				end = Integer.parseInt(afterTokens[afterTokens.length - 1].split("-")[1]);
			} else {
				end = Integer.parseInt(afterTokens[afterTokens.length - 1]);
				start = end - 1;
			}
			if (tokens[2].equals(".")) {
				type = "INS-" + tokens[3].length();
			} else if (tokens[3].equals(".")) {
				type = "DEL-" + tokens[2].length();
			} else {
				type = "SUB-" + Math.max(tokens[2].length(), tokens[3].length());
			}
			fm.writeLine(contig.replace(":", "-") + "\t" + start + "\t" + end + "\t" + type + ":" + tokens[2] + ">" + tokens[3]);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar pilonChangesToBed.jar <in.pilon.changes> <out.bed>");
		System.out.println("\t<in.pilon.changes>: Output of pilon with --change option");
		System.out.println("\t<out.bed>: After pilon coordinates in bed format");
		System.out.println("Arang Rhie, 2015-11-13. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ToBed().go(args[0], args[1]);
		} else {
			new ToBed().printHelp();
		}
	}

}
