package javax.arang.phasing;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.vcf.VCF;

public class VCFtoArtificialPhasedSNP extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String gt;
		String[] alts;
		String[] genotypes;
		String hapA;
		String hapB;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#")) {
				System.out.println(line);
				continue;
			}
			
			tokens = line.split(RegExp.TAB);
			gt = VCF.parseSAMPLE(tokens[VCF.FORMAT], "GT", tokens[VCF.SAMPLE]);
			if (gt.equals("./.") || gt.equals("."))	continue;
			
			alts = tokens[VCF.ALT].split(RegExp.COMMA);
			int pos = Integer.parseInt(tokens[VCF.POS]);
			
			if (gt.contains("/")) {
				genotypes = gt.split("/");
			} else if (gt.contains("|")){
				genotypes = gt.split("|");
			} else {
				// genotype is already haplotype
				System.out.println(line);
				continue;
			}
			
			if (genotypes.length != 2) {
				System.out.println(line);
				continue;
			}
			
			// get gtIdx: 0 / 1 / 2 / 3 / ...
			//  0: ref
			//  1~ : alt[0]~
			hapA = getAllele(Integer.parseInt(genotypes[0]), tokens[VCF.REF], alts);
			hapB = getAllele(Integer.parseInt(genotypes[1]), tokens[VCF.REF], alts);
			
			
			writePhasedVariant(fm, tokens[VCF.CHROM], pos, tokens[VCF.REF], hapA, hapB);
			
		}
	}

	private void writePhasedVariant(FileMaker fm, String chr, int pos, String ref, String hapA, String hapB) {
		
		char refAllele;
		char hapAallele;
		char hapBallele;
		
		for (int i = 0; i < ref.length(); i++) {
			refAllele = ref.charAt(i);
					
			// compare hapA
			if (i <= hapA.length() - 1) {
				// ref idx i is in bound of hapA
				hapAallele = hapA.charAt(i);
			} else {
				// Deletion
				hapAallele = 'D';
			}
			
			// compare hapB
			if (i <= hapB.length() - 1) {
				// ref idx i is in bound of hapA
				hapBallele = hapB.charAt(i);
			} else {
				// Deletion
				hapBallele = 'D';
			}
			
			// skip if refAllele == hapAallele == hapBallele
			if (refAllele == hapAallele
					&& hapAallele == hapBallele) {
				// do nothing
			} else {
				fm.writeLine(chr + "\t" + (pos + i) + "\t" + hapAallele + "\t" + hapBallele + "\t" + pos);
			}
			
		}
		
	}

	private String getAllele(int gtIdx, String ref, String[] alts) {
		if (gtIdx == 0) {
			return ref;
		} else {
			return alts[gtIdx - 1];
		}
	}

	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingVCFtoArtificialPhasedSNP.jar <in.vcf> <out.phased.snp>");
		System.out.println("\tConvert <in.vcf> to an artificial <out.phased.snp> containing Substitutions and Deletions.");
		System.out.println("\tMulti-variant sites or sites that are already haplotypes (chrX, chrY) will be reported in stdout.");
		System.out.println("Arang Rhie, 2016-07-25. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new VCFtoArtificialPhasedSNP().go(args[0], args[1]);
		} else {
			new VCFtoArtificialPhasedSNP().printHelp();
		}
		
	}

}
