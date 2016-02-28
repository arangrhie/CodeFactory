package javax.arang.phasing.indel;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class DetermineTrueFalse extends IOwrapper {
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		int len;
		int m;
		int d;
		int i;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			len = Integer.parseInt(tokens[SpannedSVRead.LEN]);
			if (tokens[SpannedSVRead.SV_KEY].startsWith("AK1")) {
				if (tokens[SpannedSVRead.TYPE].equals(SpannedSVRead.DEL)) {
					m = Integer.parseInt(tokens[SpannedSVRead.M]);
					d = Integer.parseInt(tokens[SpannedSVRead.D]);
					if (m < len * (1 - threshold) && d >= len * threshold) {
						fm.writeLine(line + "\tT");
					} else {
						fm.writeLine(line + "\tF");
					}
				} else if (tokens[SpannedSVRead.TYPE].equals(SpannedSVRead.INS)) {
					i = Integer.parseInt(tokens[SpannedSVRead.I]);
					if (i > len * threshold) {
						fm.writeLine(line + "\tT");
					} else {
						fm.writeLine(line + "\tF");
					}
				} else { // INV, COMPLEX
					m = Integer.parseInt(tokens[SpannedSVRead.M]);
					if (m < len * (1-threshold)) {
						fm.writeLine(line + "\tT");
					} else {
						fm.writeLine(line + "\tF");
					}
				}
			} else {
				if (tokens[SpannedSVRead.TYPE].equals(SpannedSVRead.DEL)) {
					i = Integer.parseInt(tokens[SpannedSVRead.I]);
					if (i < len * (1 - threshold)) {
						fm.writeLine(line + "\tT");
					} else {
						fm.writeLine(line + "\tF");
					}
				} else if (tokens[SpannedSVRead.TYPE].equals(SpannedSVRead.INS)){
					m = Integer.parseInt(tokens[SpannedSVRead.M]);
					d = Integer.parseInt(tokens[SpannedSVRead.D]);
					if (m >= len * threshold && d < len * (1-threshold)) {
						fm.writeLine(line + "\tT");
					} else {
						fm.writeLine(line + "\tF");
					}
				} else {
					m = Integer.parseInt(tokens[SpannedSVRead.M]);
					if (m >= len * threshold) {
						fm.writeLine(line + "\tT");
					} else {
						fm.writeLine(line + "\tF");
					}
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingIndelDetermineTrueFalse.jar <in.A.span> <out.A.span> [threshold=0.8]");
		System.out.println("\t<in.A.span>: haplotype A spanning reads of indel region.");
		System.out.println("\t\tGenerated with samPacBioExtractRegionSpanningReads.jar");
		System.out.println("\t\tColumn $6: Reference. AK1| ... or hg19| ... or hg38| ...");
		System.out.println("\t\t$4: Type. INS / DEL / COMPLEX / INV");
		System.out.println("\t\t$5: Len.");
		System.out.println("\t\t$10: M");
		System.out.println("\t\t$11: D");
		System.out.println("\t\t$12: I");
		System.out.println("\t<out.A.span>: Adding T/F/NA at the end of the line. COMPLEX, INV will be treated as hg19 ins.");
		System.out.println("\t[threshold]: DEFAULT=0.8. True will be assigned when:");
		System.out.println("\t\tFor ref AK1");
		System.out.println("\t\t\tDEL: M < Len*(1-threshold) && D >= Len*threshold\tINS: I > Len*threshold\tCOMPLEX, INV: M < Len*(1-threshold)");
		System.out.println("\t\tFor ref hg19, hg38");
		System.out.println("\t\t\tDEL: I < Len*(1-threshold)\tINS: M >= Len*threshold && D < Len*(1-threshold)\tCOMPLEX, INV: M >= Len*threshold");
		System.out.println("Arang Rhie, 2015-01-03. arrhie@gmail.com");
	}

	private static float threshold = 0.8f;
	public static void main(String[] args) {
		if (args.length == 3) {
			threshold = Float.parseFloat(args[2]);
			new DetermineTrueFalse().go(args[0], args[1]);
		} else if (args.length == 2) {
			new DetermineTrueFalse().go(args[0], args[1]);
		} else {
			new DetermineTrueFalse().printHelp();
		}
	}

}
