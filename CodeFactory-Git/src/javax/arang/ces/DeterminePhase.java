package javax.arang.ces;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class DeterminePhase extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		FileMaker fmPhased = new FileMaker(outPrefix + ".phased");
		FileMaker fmToCheck = new FileMaker(outPrefix + ".tocheck");
		
		String line;
		String[] tokens;
		String prevCes = "";
		String ces;
		String prevPhase = "";
		String phase;
		
		String prevLine = "";
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			ces = tokens[Bed.NOTE];
			phase = tokens[Bed.CHROM].substring(0, tokens[Bed.CHROM].indexOf("_"));
			if (ces.equals(prevCes)) {
				if (prevPhase.equals(phase)) {
					fmPhased.writeLine(prevLine);
					fmPhased.writeLine(line);
				} else {
					fmToCheck.writeLine(prevLine);
					fmToCheck.writeLine(line);
				}
			} else {
				prevCes = ces;
				prevPhase = phase;
				prevLine = line;
			}
			
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar cesDeterminePhase.jar <in.ces.mapped.bed> <out_prefix>");
		System.out.println("\t<in.ces.mapped.bed>: map CES paired-end sequenced BACs with bwa mem");
		System.out.println("\t\tconvert to bed format with bedtools bamtobed -cigar option");
		System.out.println("\t\tsort by read name, split /s to tab.");
		System.out.println("\t<out_prefix>: <out_prefix>.phased and <out_prefix>.tocheck will be generated.");
		System.out.println("\t\tphased : both ends are starting with AH_ or B_");
		System.out.println("\t\ttocheck : the rest of it");
		System.out.println("Arang Rhie, 2016-02-03. arrhie@gmail.com");
	}

	private static String outPrefix;
	public static void main(String[] args) {
		if (args.length == 2) {
			outPrefix = args[1];
			new DeterminePhase().go(args[0]);
		} else {
			new DeterminePhase().printHelp();
		}
	}

}
