package javax.arang.phasing;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.phasing.util.Phase;
import javax.arang.phasing.util.PhasedSNP;

public class BaseCallPhasing extends Phase {
	
	private static FileMaker fmRead;

	@Override
	public void readSamHeader(String line) {
		// Do nothing
	}

	@Override
	public void determineRead(String line, String readID, int countA, int countB,
			int countO, int seqStart, int seqEnd,
			ArrayList<PhasedSNP> snpsInRead, String haplotype,
			ArrayList<Integer> snpsInReadPosList) {
		if ((countA + countB) >= NUM_SNPS_WITHIN_READ) {
			// if read contains at least NUM_SNPS_WITHIN_READ A / B determinable snp
			writeOut(fmRead, readID, countA, countB, countO, seqStart, seqEnd, haplotype, snpsInReadPosList);
		}
	}

	@Override
	public void writePhasedSNP(String chr, int pos, String haplotypeA, String haplotypeB) {
	}

	@Override
	public void hooker(FileReader frBase, FileReader frSNPs) {
		HashMap<Integer, PhasedSNP> snpPosToPhasedSNPmap;
		
		if (frSNPs.getFileName().endsWith(".haps")) {
			// Read snp data from SHAPEIT
			System.out.println("Read snp data from .haps (SHAPEIT)");
			snpPosToPhasedSNPmap = readHapsStoreSNPs(frSNPs);
		} else {
			System.out.println("Read snp data from PhasedSNP");
			snpPosToPhasedSNPmap = readSNPsStoreSNPs(frSNPs);
		}

		// Read alignment file frSam, determine each snps in record
		System.out.println("Num of SNPs to be counted in reads (0: All) : " + NUM_SNPS_WITHIN_READ);
		fmRead = new FileMaker(outPrefix + ".read", true);
		readBaseDetermineSNP(frBase, snpPosToPhasedSNPmap);
		fmRead.closeMaker();
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingBaseCallPhasing.jar <in.base> <in.phased.snp> <out_prefix>");
		System.out.println("\t<in.base>: base call generated with bamBaseDepth.jar, for specific target (BACs or fosmid).");
		System.out.println("\t<in.phased.snp>: any SNPs formatted as CHR\tPOS\tHapAallele\tHapBallele");
		System.out.println("\t<out_prefix>: <out_prefix>.read will be made (or appended if already exists).");
		System.out.println("\tRun this code on each chromosome seperately.");
		System.out.println("Arang Rhie, 2015-09-08. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			outPrefix = args[2];
			new BaseCallPhasing().go(args[0], args[1]);
		} else {
			new BaseCallPhasing().printHelp();
		}
	}

}
