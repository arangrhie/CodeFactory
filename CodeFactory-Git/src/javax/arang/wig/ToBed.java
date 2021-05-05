package javax.arang.wig;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToBed extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String chr = "";
		long start = 0, span = 0;
		
		fr.readLine();	// ignore the first header line
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			
			if (isComment(tokens[0])) {
				chr=tokens[1].split("=")[1];
				start=Long.parseLong(tokens[2].split("=")[1]) - 1;
				span=Long.parseLong(tokens[3].split("=")[1]);
			} else {
				System.out.println(chr + "\t" + start + "\t" + (start + span) + "\t" + tokens[0]);
				start += span;
			}
		}
	}

	private boolean isComment(String token) {
		if (token.equals("fixedStep")) {
			return true;
		}
		return false;
	}
	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar wigToBed.jar in.wig");
		System.out.println("\tConvert a .wig file to a .bed file");
		System.out.println("\tin.wig: fixedStep chrom= start= step= are expected to be all in one line");
		System.out.println("\tsysout: bed format");
		System.out.println("Arang Rhie, 2021-03-05. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			new ToBed().printHelp();
		} else {
			new ToBed().go(args[0]);
		}
		
	}

}
