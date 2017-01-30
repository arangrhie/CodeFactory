package javax.arang.phasing;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedSNP;
import javax.arang.phasing.util.PhasedSNPBase;

public class FilterPhasedSNPs extends IOwrapper {

	private static final short IS_A = 0;
	private static final short IS_B = 1;
	private static int MAX_TOTAL_DEPTH_FOR_PHASING = 120;
	private static int MIN_TOTAL_DEPTH_REQUIRED = 0;	// H + A + B < MIN_TOTAL_DEPTH_REQUIRED will be marked as ToRemove on the snp list
	private static int MIN_DEPTH_FOR_OTHER = 30;
	private static int MIN_DEPTH_FOR_ERROR = 15;
	private static int MIN_FRAC_FOR_ERROR = 15;	// % will be removed for HET>HOM

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		String nextLine;
		String[] nextTokens;

		int AtoA = 0;
		int BtoB = 0;
		int AtoB = 0;
		int BtoA = 0;
		int H = 0;
		int A = 0;
		int B = 0;
		int D = 0;
		int O = 0;
		int toRemove = 0;
		int toCorrect = 0;
		char correctedBase;
		int correctedCount;
		int changedToHom = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			if (line.startsWith("#")) {
				writeSNP(fm, tokens, "NOTE");
				continue;
			}

			//homo
			if (tokens[PhasedSNPBase.HAPLOTYPE_A].equals(tokens[PhasedSNPBase.HAPLOTYPE_B])) {
				writeSNP(fm, tokens, "HOM");
				continue;
			}

			H = Integer.parseInt(tokens[PhasedSNPBase.OFFSET + PhasedSNPBase.H]);
			A = Integer.parseInt(tokens[PhasedSNPBase.OFFSET + PhasedSNPBase.A]);
			B = Integer.parseInt(tokens[PhasedSNPBase.OFFSET + PhasedSNPBase.B]);
			D = Integer.parseInt(tokens[PhasedSNPBase.OFFSET + PhasedSNPBase.D + 1]);

			// Coverage > MAX_TOTAL_DEPTH_FOR_PHASING then REMOVE
			if ((H + A + B) > MAX_TOTAL_DEPTH_FOR_PHASING) {
				toRemove++;
				writeSNP(fm, tokens, "ToRemove");
			}
			
			// Coverage == 0
			if (A + B == 0) {
				writeSNP(fm, tokens, "HET");
				continue;
			}

			if (Math.min(A, B) < D  && D > MIN_DEPTH_FOR_OTHER || (H + A + B) < MIN_TOTAL_DEPTH_REQUIRED) {
				toRemove++;
				writeSNP(fm, tokens, "ToRemove");
				continue;
			}

			// Switch one haplotype to another base
			O = Integer.parseInt(tokens[PhasedSNPBase.OFFSET + PhasedSNPBase.O]);
			if (Math.min(A, B) < (O - D) && (O - D) > MIN_DEPTH_FOR_OTHER) {
				correctedBase = getCorrectedBase(tokens);
				correctedCount = getCorrectedCount(tokens, correctedBase);
				if (Math.min(A, B) < correctedCount) {
					if (A < B) {
						toCorrect++;
						writeSNP(fm, tokens, "Ato" + correctedBase);
					} else if (B < A) {
						writeSNP(fm, tokens, "Bto" + correctedBase);
						toCorrect++;
					} else {
						writeSNP(fm, tokens, "HET");
					}
					continue;
				}
			}

			// Switch to Homo
			if (Math.min(A, B) < MIN_DEPTH_FOR_ERROR && (Math.min(A, B) * 100 / (A + B)) <= MIN_FRAC_FOR_ERROR && fr.hasMoreLines()) {
				nextLine = fr.readLine();
				nextTokens = nextLine.split(RegExp.TAB);
				if (nextTokens[PhasedSNPBase.HAPLOTYPE_A].equals(nextTokens[PhasedSNPBase.HAPLOTYPE_B])) {
					// nextToken is HOM
					if (A > B) {
						writeHomozygotes(fm, tokens, IS_A);
						changedToHom++;
					} else if (B < A) {
						writeHomozygotes(fm, tokens, IS_B);
						changedToHom++;
					} else {
						writeSNP(fm, tokens, "HET");
					}
					writeSNP(fm, nextTokens, "HOM");
					continue;
				} else {
					// nextToken is HET 
					AtoA = Integer.parseInt(nextTokens[PhasedSNPBase.OFFSET + PhasedSNPBase.AA + 2]);
					BtoB = Integer.parseInt(nextTokens[PhasedSNPBase.OFFSET + PhasedSNPBase.BB + 2]);
					AtoB = Integer.parseInt(nextTokens[PhasedSNPBase.OFFSET + PhasedSNPBase.AB + 2]);
					BtoA = Integer.parseInt(nextTokens[PhasedSNPBase.OFFSET + PhasedSNPBase.BA + 2]);
					
					if (A > B && AtoB > BtoB) {
						// B may be changed to A; AA
						writeHomozygotes(fm, tokens, IS_A);
						changedToHom++;
					} else if (B > A && BtoA > AtoA) {
						// A may be changed to B; BB
						writeHomozygotes(fm, tokens, IS_B);
						changedToHom++;
					} else {
						writeSNP(fm, tokens, "HET");
					}
					writeSNP(fm, nextTokens, "HET");
					continue;
				}
			}

			writeSNP(fm, tokens, "HET");
		}

		System.out.println("Num. SNPs changed to homozygotes: " + changedToHom);
		System.out.println("Num. SNPs to remove: " + toRemove);
		System.out.println("Num. SNPs to correct: " + toCorrect);
	}

	private int getCorrectedCount(String[] tokens, char correctedBase) {

		switch (correctedBase) {
		case 'A': return Integer.parseInt(tokens[PhasedSNPBase.OFFSET + PhasedSNPBase.a + 1]);
		case 'C': return Integer.parseInt(tokens[PhasedSNPBase.OFFSET + PhasedSNPBase.c + 1]);
		case 'G': return Integer.parseInt(tokens[PhasedSNPBase.OFFSET + PhasedSNPBase.g + 1]);
		case 'T': return Integer.parseInt(tokens[PhasedSNPBase.OFFSET + PhasedSNPBase.t + 1]);
		}
		return -1;
	}

	private char getCorrectedBase(String[] tokens) {
		int a = Integer.parseInt(tokens[PhasedSNPBase.OFFSET + PhasedSNPBase.a + 1]);
		int c = Integer.parseInt(tokens[PhasedSNPBase.OFFSET + PhasedSNPBase.c + 1]);
		int g = Integer.parseInt(tokens[PhasedSNPBase.OFFSET + PhasedSNPBase.g + 1]);
		int t = Integer.parseInt(tokens[PhasedSNPBase.OFFSET + PhasedSNPBase.t + 1]);
		if (a > c) {
			if (g > t) {
				if (a > g) {
					return 'A';
				} else {
					return 'G';
				}
			} else {
				if (a > t) {
					return 'A';
				} else {
					return 'T';
				}
			}
		} else {
			if (g > t) {
				if (c > g) {
					return 'C';
				} else {
					return 'G';
				}
			} else {
				if (c > t) {
					return 'C';
				} else {
					return 'T';
				}
			}
		}
	}

	private void writeSNP(FileMaker fm, String[] tokens, String note) {
		fm.write(tokens[PhasedSNP.CHR]);
		for (int i = PhasedSNP.POS; i < PhasedSNP.PS; i++) {
			fm.write("\t" + tokens[i]);
		}
		fm.write("\t" + note);
		writeInfo(fm, tokens);
	}

	private void writeInfo(FileMaker fm, String[] tokens) {
		for (int i = PhasedSNPBase.OFFSET + PhasedSNPBase.H; i < tokens.length; i++) {
			fm.write("\t" + tokens[i]);
		}
		fm.writeLine();
	}

	private void writeHomozygotes(FileMaker fm, String[] tokens, int haplotype) {
		//fm.write(tokens[PhasedSNP.CHR] + "\t" + tokens[PhasedSNP.POS] + "\t");
		String homopattern = "";
		if (haplotype == IS_A) {
			//fm.write(tokens[PhasedSNP.HAPLOTYPE_A] + "\t" + tokens[PhasedSNP.HAPLOTYPE_A]);
			homopattern = "A|A";
		} else if (haplotype == IS_B) {
			//fm.write(tokens[PhasedSNP.HAPLOTYPE_B] + "\t" + tokens[PhasedSNP.HAPLOTYPE_B]);
			homopattern = "B|B";
		}
		writeSNP(fm, tokens, homopattern);
		//writeInfo(fm, tokens);
	}

	@Override
	public void printHelp() {
		System.out.println("Useage: java -jar phasingFilterPhasedSNPs.jar <in.base> <out.snp> [MAX_TOTAL_DEPTH_FOR_PHASING] [MIN_TOTAL_DEPTH_REQUIRED] [MIN_DEPTH_FOR_OTHER] [MIN_FRAC_FOR_ERROR]");
		System.out.println("\t<in.phased.snp>: generated with phasingPhaedReadsToSnpBaseCount.jar");
		System.out.println("\t<out.phased_filt.snp>: filter homozygotes snps and set both haplotype A and B to one allele.");
		System.out.println("\t\tHomozygote SNPs will NOT BE REMOVED. Just the haplotypes are re-set.");
		System.out.println("\t[MAX_TOTAL_DEPTH_FOR_PHASING] : SNPs with coverage (H+A+B) > [MAX_TOTAL_DEPTH_FOR_PHASING] will BE REMOVED.");
		System.out.println("\t[MIN_TOTAL_DEPTH_REQUIRED]: SNPs with coverage (H+A+B) < [MIN_TOTAL_DEPTH_REQUIRED] will BE REMOVED.");
		System.out.println("\t[MIN_DEPTH_FOR_OTHER]: Math.min(A, B) < D  && D > [MIN_DEPTH_FOR_OTHER] will be REMOVED.");
		System.out.println("\t\tMath.min(A, B) < (O - D) && (O - D) > [MIN_DEPTH_FOR_OTHER] : Switch to other haplotype.");
		System.out.println("\t[MIN_FRAC_FOR_ERROR]: unit: %. ex., 15 for 15%.");
		System.out.println("\tRecommended setting for PacBio subreads (101x):");
		System.out.println("\t\tMIN_TOTAL_DEPTH_REQUIRED = 0");
		System.out.println("\t\tMIN_DEPTH_FOR_OTHER = 30");
		System.out.println("\t\tMIN_DEPTH_FOR_ERROR = 15");
		System.out.println("\t\tMIN_FRAC_FOR_ERROR = 15");
		System.out.println("\tRecommended setting for illumina reads for BACs (30x):");
		System.out.println("\t\tMIN_TOTAL_DEPTH_REQUIRED = 0");
		System.out.println("\t\tMIN_DEPTH_FOR_OTHER = 2");
		System.out.println("\t\tMIN_DEPTH_FOR_ERROR = 15");
		System.out.println("\t\tMIN_FRAC_FOR_ERROR = 15");
		System.out.println("Arang Rhie, 2017-01-17. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			printOptions();
			new FilterPhasedSNPs().go(args[0], args[1]);
		} else if (args.length == 7) {
			MAX_TOTAL_DEPTH_FOR_PHASING = Integer.parseInt(args[2]);
			MIN_TOTAL_DEPTH_REQUIRED = Integer.parseInt(args[3]);
			MIN_DEPTH_FOR_OTHER = Integer.parseInt(args[4]);
			MIN_DEPTH_FOR_ERROR = Integer.parseInt(args[5]);
			MIN_FRAC_FOR_ERROR = Integer.parseInt(args[6]);
			printOptions();
			new FilterPhasedSNPs().go(args[0], args[1]);
		} else {
			new FilterPhasedSNPs().printHelp();
		}
	}
	
	private static void printOptions() {
		System.out.println("Coverage threashold for removing SNPs with cov less than: " + MIN_TOTAL_DEPTH_REQUIRED);
		System.out.println("H + A + B > " + MAX_TOTAL_DEPTH_FOR_PHASING + " will be marked as ToRemove");
		System.out.println("H + A + B < " + MIN_TOTAL_DEPTH_REQUIRED + " will be marked as ToRemove");
		System.out.println("When min(A, B) < D && " + MIN_DEPTH_FOR_OTHER + " < D will be marked as ToRemove");
		System.out.println("min(A, B) < (O - D) && (O - D) > " + MIN_DEPTH_FOR_OTHER + " will be marked as AtoO or BtoO.");
		System.out.println("min(A, B) < " + MIN_DEPTH_FOR_ERROR + " && (min(A, B) * 100 / (A + B)) <= " + MIN_FRAC_FOR_ERROR + " will be marked as Hom instead of Het.");
	}

}
