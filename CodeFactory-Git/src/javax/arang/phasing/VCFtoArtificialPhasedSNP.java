package javax.arang.phasing;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.vcf.VCF;

public class VCFtoArtificialPhasedSNP extends IOwrapper {

	private int pos = 0;	// to prevent overlapping variants, this var should be always increased
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String gt;
		String[] alts;
		String[] genotypes = {""};
		String hapA;
		String hapB;
		String prevChr = "";
		
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
			if (prevChr.equals(tokens[VCF.CHROM]) && this.pos >= pos) {
				System.out.println(line + " : position is overlapping");
				continue;
			}
			this.pos = pos;
			
			if (gt.contains("/")) {
				if (! takePhasedOnly) {
					genotypes = gt.split(RegExp.SLASH);
				} else {
					continue;
				}
			} else if (gt.contains("|")){
				genotypes = gt.split(RegExp.BAR);
			} else {
				// genotype is already haplotype
				System.out.println("Genotype is already haplotype: " + line);
				System.exit(-1);
				//continue;
			}
			
			if (genotypes.length != 2) {
				System.out.println("Genotypes length: " + genotypes.length + " : " + line);
				continue;
			}
			
			// get gtIdx: 0 / 1 / 2 / 3 / ...
			//  0: ref
			//  1~ : alt[0]~
			hapA = getAllele(Integer.parseInt(genotypes[0]), tokens[VCF.REF], alts);
			hapB = getAllele(Integer.parseInt(genotypes[1]), tokens[VCF.REF], alts);
			
			
			writePhasedVariant(fm, tokens[VCF.CHROM], tokens[VCF.REF], hapA, hapB);
			prevChr = tokens[VCF.CHROM];
		}
	}

	private void writePhasedVariant(FileMaker fm, String chr, String ref, String hapA, String hapB) {
		
		char refAllele;
		char hapAallele;
		char hapBallele;
		
		// increment interval by incrValBy when the ref.length() >= 10.
		int incrValBy = 1;
		if (ref.length() >= 10) {
			incrValBy = 5;
		}
		
		int i;
		for (i = 0; i < ref.length(); ) {
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
				// ref idx i is in bound of hapB
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
			
			// when reaching ref.length, quit
			if (i == ref.length() - 1)	break;
			
			// increment i by incrValBy within the boundary of ref.length.
			if (i+incrValBy < ref.length()) {
				i += incrValBy;
			} else {
				// get the boundary position when increment exceeds the boundary.
				i = ref.length() - 1;
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
		System.out.println("Usage: java -jar phasingVCFtoPhasedSNP.jar <in.vcf> <out.phased.snp> [take_phased_only]");
		System.out.println("\tConvert <in.vcf> to an artificial <out.phased.snp> containing Substitutions and Deletions.");
		System.out.println("\tMulti-variant sites or sites that are already haplotypes (chrX, chrY) will be reported in stdout.");
		System.out.println("\t[take_phased_only]: DEFAULT=TRUE. If you want to use all the unphased variants, give FALSE.");
		System.out.println("Arang Rhie, 2017-06-26. arrhie@gmail.com");
	}

	private static boolean takePhasedOnly = true;
	public static void main(String[] args) {
		if (args.length == 2) {
			new VCFtoArtificialPhasedSNP().go(args[0], args[1]);
		} else if (args.length == 3) {
			takePhasedOnly = Boolean.parseBoolean(args[2]);
			new VCFtoArtificialPhasedSNP().go(args[0], args[1]);
		} else {
			new VCFtoArtificialPhasedSNP().printHelp();
		}
		
	}

}
