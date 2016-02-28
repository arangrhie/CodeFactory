package javax.arang.pbhoney;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ResultsToBed extends IOwrapper {

	private static short CONTIG = 0;
	private static short START = 1;
	private static short END = 2;
	private static short TYPE = 3;
	private static short SIZE = 4;
	@Override
	public void hooker(FileReader fr, FileMaker fmBed) {
		String line;
		String[] tokens;
		
		FileMaker fmSeq = new FileMaker(prefix + ".seq");
		
		String info;
		String[] infoFields;
		String seq;
		int start;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			start = Integer.parseInt(tokens[START]) - 1;
			info = tokens[5];
			infoFields = info.split(";");
			seq = parseInfo(infoFields, "seq");
			fmBed.writeLine(tokens[CONTIG] + "\t" + start + "\t" + tokens[END] + "\t" + tokens[TYPE] + "\t" + tokens[TYPE] + ":" + tokens[SIZE] + "\tpbhoney");
			if (tokens[TYPE].equals("INS") || tokens[TYPE].equals("INV")) {
				fmSeq.writeLine(tokens[CONTIG] + "\t" + tokens[END] + "\t" + tokens[SIZE] + "\t" + seq);
			}
		}
	}

	private String parseInfo(String[] infoFields, String seq) {
		for (int i = 0; i < infoFields.length; i++) {
			if (infoFields[i].startsWith(seq)) {
				return infoFields[i].replace(seq + "=", "");
			}
		}
		return "";
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar pbhoneyResultsToBed.jar <in.spot.txt> <prefix>");
		System.out.println("\tConverts pbhoney .spot into .sv.bed and ins.seq.");
		System.out.println("\t<in.txt>: CONTIG\tSTART(1-base)\tEND\tTYPE\tSIZE\tINFO");
		System.out.println("\t<prefix.bed>: CONTIG\tSTART(0-base)\tEND\tTYPE\tTYPE:SIZE\tpbhoney");
		System.out.println("\t<prefix.seq>: Only for INS. CONTIG\tPOS\tSIZE\tSEQ");
		System.out.println("Arang Rhie, 2015-12-18. arrhie@gmail.com");
	}

	private static String prefix;
	public static void main(String[] args) {
		if (args.length == 2) {
			prefix = args[1];
			new ResultsToBed().go(args[0], prefix + ".bed");
		} else {
			new ResultsToBed().printHelp();
		}
	}

}
