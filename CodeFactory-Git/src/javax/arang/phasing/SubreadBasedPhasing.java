package javax.arang.phasing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.phasing.util.Phase;
import javax.arang.phasing.util.PhasedSNP;

public class SubreadBasedPhasing extends Phase {
	
	private static FileMaker fmSNP;
	private static FileMaker fmRead;
	
	@Override
	public void hooker(FileReader frSam, FileReader frSNPs) {
		HashMap<Integer, PhasedSNP> snpPosToPhasedSNPmap;
		Integer[] snpPosList;
		
		fmSNP = new FileMaker(outPrefix + ".snp");
		if (frSNPs.getFileName().endsWith(".haps")) {
			// Read snp data from SHAPEIT
			System.out.println("Read snp data from .haps (SHAPEIT)");
			snpPosToPhasedSNPmap = readHapsStoreSNPs(frSNPs);
		} else {
			System.out.println("Read snp data from PhasedSNP");
			snpPosToPhasedSNPmap = readSNPsStoreSNPs(frSNPs);
		}
		fmSNP.closeMaker();

		snpPosList = snpPosToPhasedSNPmap.keySet().toArray(new Integer[0]);
		Arrays.sort(snpPosList);
		
		// Read alignment file frSam, determine each snps in record
		System.out.println("Num of SNPs to be counted in reads (0: All) : " + NUM_SNPS_WITHIN_READ);
		fmRead = new FileMaker(outPrefix + ".read");
		readSamDetermineSNP(frSam, snpPosList, snpPosToPhasedSNPmap);
		fmRead.closeMaker();
		
	}
	
	@Override
	public void writePhasedSNP(String chr, int pos, String haplotypeA, String haplotypeB) {
		fmSNP.writeLine(chr + "\t" + pos + "\t" + haplotypeA + "\t" + haplotypeB);
	};

	@Override
	public void determineRead(String line, String readID,
			int countA, int countB, int countO, int seqStart, int seqEnd,
			ArrayList<PhasedSNP> snpsInRead, String haplotype,
			ArrayList<Integer> snpsInReadPosList) {
		if ((countA + countB) >= NUM_SNPS_WITHIN_READ) {
			// if read contains at least NUM_SNPS_WITHIN_READ A / B determinable snp
			writeOut(fmRead, readID, countA, countB, countO, seqStart, seqEnd, haplotype, snpsInReadPosList);
		}
		
	}


	@Override
	public void printHelp() {
		System.out.println("Usage: phasingSubreadBasedPhasing.jar <in.sam> <snp_by_shapeit.haps/10X_noindel.snp> <phased.reads> [NUM_SNPS_WITHIN_READ]");
		System.out.println("\t<in.sam>: Subreads aligned to reference (hg19)");
		System.out.println("\t<snp_by_shapeit.haps>: .haps file generated with shapeit");
		System.out.println("\t\tCHR\tRS_ID\tPOS\tAlleleOfA\tAlleleOfB\tHaplotypeA\tHaplotypeB");
		System.out.println("\t<phased.reads>: READ_ID\tCOUNT_A\tCOUNT_B\tCOUNT_O\tREAD_START_POS\tREAD_END_POS\tREAD_LEN\tHAPLOTYPE_PATTERN\tSNP_POS");
		System.out.println("\t[NUM_SNPS_WITHIN_READ]: Read info with at least [NUM_SNPS_WITHIN_READ] will be printed. DEFUALT=0");
		System.out.println("\tRun this code on each chr seperately.");
		System.out.println("Arang Rhie, 2015-08-09. arrhie@gmail.com");
	}


	public static void main(String[] args) {
		if (args.length == 3) {
			outPrefix = args[2];
			new SubreadBasedPhasing().go(args[0], args[1]);
		} else if (args.length == 4) {
			outPrefix = args[2];
			NUM_SNPS_WITHIN_READ = Integer.parseInt(args[3]);
			new SubreadBasedPhasing().go(args[0], args[1]);
		} else {
			new SubreadBasedPhasing().printHelp();
		}
	}

	@Override
	public void readSamHeader(String line) {
		// TODO Auto-generated method stub
		
	}

}
