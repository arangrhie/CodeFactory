package javax.arang.vcf;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ExtractGT extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		String chrom;
		String pos;
		String ref;
		String alt;
		String gt;
		String[] alts;
		int numSamples = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			if (line.startsWith("#CHROM")) {
				numSamples = tokens.length - 9;
				continue;
			}
			if (line.startsWith("#")) continue;
			chrom = tokens[VCF.CHROM];
			pos = tokens[VCF.POS];
			ref = tokens[VCF.REF];
			alt = tokens[VCF.ALT];
			alts = alt.split(RegExp.COMMA);
			
			System.out.print(chrom + "\t" + pos);
			for (int sample_i = 0; sample_i < numSamples; sample_i++) {
				gt = VCF.getGT(tokens[VCF.FORMAT], tokens[VCF.SAMPLE + sample_i]);
				if (gt.contains("|") || gt.contains("/")) {
					gt = gt.split(RegExp.BAR)[0];
					gt = gt.split(RegExp.SLASH)[0];
				}
				if (gt.equals("0")) {
					System.out.print("\t" + ref);
				} else if (gt.equals(".")) {
					// insert "NA" for the unknowns (.) ...
					System.out.print("\tNA");
				} else {
					alt = alts[Integer.parseInt(gt) - 1];
					// insert "ref" base for the unknowns (*) ...
					if (alt.equals("*")) {
						alt = "NA";
					}
					System.out.print("\t" + alt);
				}
			}
			System.out.println();
		}
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar vcfExtractGT.jar in.vcf");
		System.err.println("  Input : vcf or multi-sample vcf");
		System.err.println("  Output: CHROM <tab> POS <tab> GT");
		System.err.println("    GT: REF or ALT bases inferred from the GT field. If called in diploid mode, GT will be the first allele.");
		System.err.println("        NA if the GT is . or *");
		System.err.println("Arang Rhie, 2022-09-01. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new  ExtractGT().go(args[0]);
		} else {
			new ExtractGT().printHelp();
		}

	}

}
