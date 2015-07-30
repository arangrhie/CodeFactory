package javax.arang.phasing;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class FilterHomoOfPhasedReadsToSnpBlock extends IOwrapper {

	private static final int NOT_SWITCHED_FROM_PREV_A_AND_IS_A = 5;
	private static final int NOT_SWITCHED_FROM_PREV_B_AND_IS_B = 6;
	private static final int SWITCHED_FROM_PREV_B_TO_A = 7;
	private static final int SWITCHED_FROM_PREV_A_TO_B = 8;
	
	private static final short IS_A = 0;
	private static final short IS_B = 1;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		int AtoA = 0;
		int BtoB = 0;
		int AtoB = 0;
		int BtoA = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#")) continue;
			tokens = line.split(RegExp.TAB);
			AtoA = Integer.parseInt(tokens[NOT_SWITCHED_FROM_PREV_A_AND_IS_A]);
			BtoB = Integer.parseInt(tokens[NOT_SWITCHED_FROM_PREV_B_AND_IS_B]);
			AtoB = Integer.parseInt(tokens[SWITCHED_FROM_PREV_A_TO_B]);
			BtoA = Integer.parseInt(tokens[SWITCHED_FROM_PREV_B_TO_A]);
			
			if (AtoA > 15 && BtoB <= 15 && BtoA > 15 && AtoB <= 15) {
				// A Homozygotes
				writeHomozygotes(fm, tokens, IS_A);
			} else if (AtoA <= 15 && BtoB > 15 && AtoB <= 15 && BtoA > 15) {
				// B Homozygotes
				writeHomozygotes(fm, tokens, IS_B);
			} else {
				fm.writeLine(line);
			}
		}
	}
	
	private void writeHomozygotes(FileMaker fm, String[] tokens, int haplotype) {
		fm.write(tokens[PhasedSNP.CHR] + "\t" + tokens[PhasedSNP.POS] + "\t");
		if (haplotype == IS_A) {
			fm.write(tokens[PhasedSNP.HAPLOTYPE_A] + "\t" + tokens[PhasedSNP.HAPLOTYPE_A]);
		} else if (haplotype == IS_B) {
			fm.write(tokens[PhasedSNP.HAPLOTYPE_B] + "\t" + tokens[PhasedSNP.HAPLOTYPE_B]);
		}
		
		for (int i = PhasedSNP.PS; i < tokens.length; i++) {
			fm.write("\t" + tokens[i]);
		}
		fm.writeLine();
	}

	@Override
	public void printHelp() {
		System.out.println("Useage: java -jar phasingFilterHomoOfPhasedReadsToSnpBlock.jar <in.phased.snp> <out.phased_filt.snp>");
		System.out.println("\t<in.phased.snp>: generated with phasingPhaedReadsToSnpBlock.jar");
		System.out.println("\t<out.phased_filt.snp>: filter homozygotes snps and set both haplotype A and B to one allele.");
		System.out.println("\t\tHomozygote SNPs will NOT BE REMOVED. Just the haplotypes are re-set.");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new FilterHomoOfPhasedReadsToSnpBlock().go(args[0], args[1]);
		} else {
			new FilterHomoOfPhasedReadsToSnpBlock().printHelp();
		}
	}

}
