package javax.arang.phasing.snp;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedSNP;

public class PhasedSnpToPhasedIndel extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			if (tokens[PhasedSNP.HAPLOTYPE_A].length() > 1 || tokens[PhasedSNP.HAPLOTYPE_B].length() > 1) {
				fm.writeLine(line);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingPhasedSnpToPhasedIndel.jar <in.snp> <out.indel>");
		System.out.println("\t<in.snp>: converted from vcf with phasingExtractPhasedSnp.jar");
		System.out.println("\t<out.indel>: list of indels out of <in.snp>");
		System.out.println("Arang Rhie, 2015-12-28. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new PhasedSnpToPhasedIndel().go(args[0], args[1]);
		} else {
			new PhasedSnpToPhasedIndel().printHelp();
		}
	}

}
