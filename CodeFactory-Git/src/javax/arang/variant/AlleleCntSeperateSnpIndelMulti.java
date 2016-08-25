package javax.arang.variant;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class AlleleCntSeperateSnpIndelMulti extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		FileMaker fmSnp = new FileMaker(prefix + ".snp");
		FileMaker fmIndel = new FileMaker(prefix + ".indel");
		FileMaker fmSnpMulti = new FileMaker(prefix + ".snp.multi");
		FileMaker fmIndelMulti = new FileMaker(prefix + ".indel.multi");
		String[] alts;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			if (tokens[AlleleCount.REF].length() > 1
					|| tokens[AlleleCount.ALT].length() > 1
					|| tokens[AlleleCount.REF].equals("-")
					|| tokens[AlleleCount.ALT].equals("-")) {
				if (tokens[AlleleCount.ALT].contains(",")) {
					// Multi
					alts = tokens[AlleleCount.ALT].split(",");
					// not biallelic?
					if (alts.length > 2) {
						// do nothing
						continue;
					}
					if (alts[0].length() == 1 && alts[1].length() == 1) {
						fmSnpMulti.writeLine(line);
					} else {
						fmIndelMulti.writeLine(line);
					}
				} else {
					fmIndel.writeLine(line);
				}
			} else if (tokens[AlleleCount.REF].length() == 1
					&& tokens[AlleleCount.ALT].length() == 1) {
					fmSnp.writeLine(line);
			} else {
				
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar variantAlleleCntSeperateSnpIndelMulti.jar <in.allele.cnt>");
		System.out.println("\t<in.allele.cnt>: generated with vcfToAlleleCount.jar");
		System.out.println("\t<out>: <in.allele.cnt>.snp, <in.allele.cnt>.indel and <in.allele.cnt>.snp.multi will be generated");
		System.out.println("Arang Rhie, 2016-02-05. arrhie@gmail.com");
	}

	private static String prefix; 
	public static void main(String[] args) {
		if (args.length == 1) {
			prefix = args[0];
			new AlleleCntSeperateSnpIndelMulti().go(args[0]);
		} else {
			new AlleleCntSeperateSnpIndelMulti().printHelp();
		}
	}

}
