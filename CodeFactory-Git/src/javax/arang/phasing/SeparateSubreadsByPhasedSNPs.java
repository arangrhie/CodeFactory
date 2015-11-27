package javax.arang.phasing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.phasing.util.Phase;
import javax.arang.phasing.util.PhasedSNP;

public class SeparateSubreadsByPhasedSNPs extends Phase {

	private static String outPrefix;
	private static boolean writeSam = true;
	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingSeperateSubreadsByPhasedSNPs.jar <in.sam> <in.phased.snp> <out_prefix> [write_sam=TRUE]");
		System.out.println("\t<in.bam>: hg19_subreads.sam");
		System.out.println("\t<in.phased.snp>: phased SNPs. CHR POS BaseOfHaplotypeA BaseOfHaplotypeB");
		System.out.println("\t<out_prefix>: <out_prefix>.readid.haplotypeA, <out_prefix>.readid.haplotypeB, <out_prefix>.readid.haplotypeHomogenic, <out_prefix>.readid.haplotypeAmbiguous\n"
				+ "\t\twill be generated containing the Read ID of fasta.");
		System.out.println("\t\tEach list consists of: <Read_ID> <Total num. phased SNPs in this read> <Num. haplotype A SNPs> <Num. haplotype B SNPs>");
		System.out.println("\t[write_sam]: TRUE or FALSE. write output as well as in .sam format only. DEFUALT=TRUE");
		System.out.println("Arang Rhie, 2015-11-23. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 4) {
			outPrefix = args[2];
			writeSam = Boolean.parseBoolean(args[3]);
			new SeparateSubreadsByPhasedSNPs().go(args[0], args[1]);
		} else if (args.length == 3) {
			outPrefix = args[2];
			new SeparateSubreadsByPhasedSNPs().go(args[0], args[1]);
		} else {
			new SeparateSubreadsByPhasedSNPs().printHelp();
		}
	}

	private FileMaker fmHaplotypeA;
	private FileMaker fmHaplotypeB;
	private FileMaker fmHomogenic;
	private FileMaker fmAmbiguous;
	private FileMaker fmReadHaplotypeA;
	private FileMaker fmReadHaplotypeB;
	private FileMaker fmReadHomogenic;
	private FileMaker fmReadAmbiguous;
	
	
	@Override
	public void hooker(FileReader frSam, FileReader frSNPs) {
		
		HashMap<Integer, PhasedSNP> snpPosToPhasedSNPmap = readSNPsStoreSNPs(frSNPs);
		Integer[] snpPosList = snpPosToPhasedSNPmap.keySet().toArray(new Integer[0]);
		Arrays.sort(snpPosList);
		System.out.println(snpPosList.length + " SNPs to be processed");
		if (writeSam) {
			fmHaplotypeA = new FileMaker(outPrefix + ".haplotypeA.sam");
			fmHaplotypeB = new FileMaker(outPrefix + ".haplotypeB.sam");
			fmHomogenic = new FileMaker(outPrefix + ".haplotypeHomogenic.sam");
			fmAmbiguous = new FileMaker(outPrefix + ".haplotypeAmbiguous.sam");
		}
		fmReadHaplotypeA = new FileMaker(outPrefix + ".readid.haplotypeA");
		fmReadHaplotypeB = new FileMaker(outPrefix + ".readid.haplotypeB");
		fmReadHomogenic = new FileMaker(outPrefix + ".readid.haplotypeHomogenic");
		fmReadAmbiguous = new FileMaker(outPrefix + ".readid.haplotypeAmbiguous");
		readSamDetermineSNP(frSam, snpPosList, snpPosToPhasedSNPmap);
		if (writeSam) {
			fmHaplotypeA.closeMaker();
			fmHaplotypeB.closeMaker();
			fmHomogenic.closeMaker();
			fmAmbiguous.closeMaker();
		}
		fmReadHaplotypeA.closeMaker();
		fmReadHaplotypeB.closeMaker();
		fmReadHomogenic.closeMaker();
		fmReadAmbiguous.closeMaker();
	}
	
	

	@Override
	public void determineRead(String line, String readID, int countA, int countB, int countO,
			int seqStart, int seqEnd, ArrayList<PhasedSNP> snpsInRead,
			String haplotype, ArrayList<Integer> snpsInReadPosList) {
		if (countB == 0 && countA > 0) {
			// Haplotype A
			writeHaplotype(fmReadHaplotypeA, fmHaplotypeA, line, readID,
					countA, countB, countO, seqStart, seqEnd, snpsInRead, haplotype, snpsInReadPosList);
		} else if (countA == 0 && countB > 0) {
			// Haplotype B
			writeHaplotype(fmReadHaplotypeB, fmHaplotypeB, line, readID,
					countA, countB, countO, seqStart, seqEnd, snpsInRead, haplotype, snpsInReadPosList);
		} else if (countA == 0 && countB == 0) {
//			if (haplotype.length() > 0) {
				// HOM or no snps available
				writeHaplotype(fmReadHomogenic, fmHomogenic, line, readID,
						countA, countB, countO, seqStart, seqEnd, snpsInRead, haplotype, snpsInReadPosList);
//			} else if (haplotype.length() == 0) {
//				// no snps available
//				writeHaplotype(fmReadNoSNP, fmNoSnp, line, readID,
//						countA, countB, countO, seqStart, seqEnd, snpsInRead, haplotype, snpsInReadPosList);
//			} 
		} else {
			// Ambiguous read
			writeHaplotype(fmReadAmbiguous, fmAmbiguous, line, readID,
					countA, countB, countO, seqStart, seqEnd, snpsInRead, haplotype, snpsInReadPosList);
		}
	}
	
	private void writeHaplotype(FileMaker fmRead, FileMaker fmSAM, String line, String readID,
			int countA, int countB, int countO, int seqStart, int seqEnd,
			ArrayList<PhasedSNP> snpsInRead, String haplotype,
			ArrayList<Integer> snpsInReadPosList) {
		if (writeSam) {
			fmSAM.writeLine(line);
		}
		writeOut(fmRead, readID, countA, countB, countO, seqStart, seqEnd, haplotype, snpsInReadPosList);
	}

	@Override
	public void readSamHeader(String line) {
		if (!writeSam) return;
		fmHaplotypeA.writeLine(line);
		fmHaplotypeB.writeLine(line);
		fmHomogenic.writeLine(line);
		fmAmbiguous.writeLine(line);
	}

	@Override
	public void writePhasedSNP(String chr, int pos, String haplotypeA,
			String haplotypeB) {
		
	}

	

}
