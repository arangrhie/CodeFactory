package javax.arang.variant;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class AlleleCntSeperateSnpIndel extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		FileMaker fmSnp = new FileMaker(prefix + ".snp");
		FileMaker fmIndel = new FileMaker(prefix + ".indel");
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			if (tokens[AlleleCount.REF].length() > 1
					|| tokens[AlleleCount.ALT].length() > 1
					|| tokens[AlleleCount.REF].equals("-")
					|| tokens[AlleleCount.ALT].equals("-")) {
				fmIndel.writeLine(line);
			} else {
				fmSnp.writeLine(line);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar variantAlleleCntSeperateSnpIndel.jar <in.allele.cnt> <out_prefix>");
		System.out.println("\t<in.allele.cnt>: generated with vcfToAlleleCount.jar");
		System.out.println("\t<out_prefix>: <out_prefix>.snp and <out_prefix>.indel will be generated");
		System.out.println("Arang Rhie, 2016-02-05. arrhie@gmail.com");
	}

	private static String prefix; 
	public static void main(String[] args) {
		if (args.length == 2) {
			prefix = args[1];
			new AlleleCntSeperateSnpIndel().go(args[0]);
		} else {
			new AlleleCntSeperateSnpIndel().printHelp();
		}
	}

}
