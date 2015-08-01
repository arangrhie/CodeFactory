package javax.arang.phasing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class SeperateSubreadsByPhasedSNPs extends Phase {

	private static String outPrefix;
	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingSeperateSubreadsByPhasedSNPs.jar <in.sam> <in.phased.snp> <out_prefix>");
		System.out.println("\t<in.bam>: hg19_subreads.sam");
		System.out.println("\t<in.phased.snp>: phased SNPs. CHR POS BaseOfHaplotypeA BaseOfHaplotypeB");
		System.out.println("\t<out_prefix>: <out_prefix>.HaplotypeA.list, <out_prefix>.HaplotypeB.list, <out_prefix>.unphased.list\n"
				+ "\t\twill be generated containing the Read ID of fasta.");
		System.out.println("\t\tEach list consists of: <Read_ID> <Total num. phased SNPs in this read> <Num. haplotype A SNPs> <Num. haplotype B SNPs>");
		System.out.println("Arang Rhie, 2015-07-16. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			outPrefix = args[2];
			new SeperateSubreadsByPhasedSNPs().go(args[0], args[1], args[2] + ".unphased.list");
		} else {
			new SeperateSubreadsByPhasedSNPs().printHelp();
		}
	}

	private FileMaker fmHaplotypeA;
	private FileMaker fmHaplotypeB;
	private FileMaker fmUnknown;
	private FileMaker fmAmbiguous;
	private FileMaker fmReadHaplotypeA;
	private FileMaker fmReadHaplotypeB;
	private FileMaker fmReadUnknown;
	private FileMaker fmReadAmbiguous;
	
	
	@Override
	public void hooker(FileReader frSam, FileReader frSNPs, FileMaker fm) {
		
		HashMap<Integer, PhasedSNP> snpPosToPhasedSNPmap = Phase.readSNPsStoreSNPs(frSNPs);
		Integer[] snpPosList = snpPosToPhasedSNPmap.keySet().toArray(new Integer[0]);
		Arrays.sort(snpPosList);
		
		fmHaplotypeA = new FileMaker(outPrefix + ".haplotypeA.sam");
		fmHaplotypeB = new FileMaker(outPrefix + ".haplotypeB.sam");
		fmUnknown = new FileMaker(outPrefix + ".haplotypeUnknown.sam");
		fmAmbiguous = new FileMaker(outPrefix + ".haplotypeAmbiguous.sam");
		fmReadHaplotypeA = new FileMaker(outPrefix + ".haplotypeA.read");
		fmReadHaplotypeB = new FileMaker(outPrefix + ".haplotypeB.read");
		fmReadUnknown = new FileMaker(outPrefix + ".haplotypeUnknown.read");
		fmReadAmbiguous = new FileMaker(outPrefix + ".haplotypeAmbiguous.read");
		readSamDetermineSNP(frSam, snpPosList, snpPosToPhasedSNPmap);
		fmHaplotypeA.closeMaker();
		fmHaplotypeB.closeMaker();
		fmUnknown.closeMaker();
		fmAmbiguous.closeMaker();
		fmReadHaplotypeA.closeMaker();
		fmReadHaplotypeB.closeMaker();
		fmReadUnknown.closeMaker();
		fmReadAmbiguous.closeMaker();
	}
	
	

	@Override
	public void determineRead(String line, String readID, int countA, int countB,
			int seqStart, int seqEnd, ArrayList<PhasedSNP> snpsInRead,
			String haplotype, ArrayList<Integer> snpsInReadPosList) {
		if (snpsInRead.size() == 0) {
			// no snps available: unknown
			fmUnknown.writeLine(line);
			fmReadUnknown.writeLine(readID);
		} else if (countB == 0) {
			// Haplotype A
			fmHaplotypeA.writeLine(line);
			fmReadHaplotypeA.writeLine(readID);
		} else if (countA == 0) {
			// Haplotype B
			fmHaplotypeB.writeLine(line);
			fmReadHaplotypeB.writeLine(readID);
		} else {
			// Ambiguous read
			fmAmbiguous.writeLine(line);
			fmReadAmbiguous.writeLine(readID);
		}
	}

	@Override
	public void readSamHeader(String line) {
		// TODO Auto-generated method stub
		fmHaplotypeA.writeLine(line);
		fmHaplotypeB.writeLine(line);
		fmUnknown.writeLine(line);
		fmAmbiguous.writeLine(line);
	}

	

}
