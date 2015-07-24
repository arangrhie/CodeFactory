package javax.arang.phasing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.genome.sam.SAMUtil;
import javax.arang.genome.sam.Sam;

public class SubreadBasedPhasedSNP extends I2Owrapper {
	
	HashMap<Integer, PhasedSNP> snpPosToPhasedSNPmap = new HashMap<Integer, PhasedSNP>();
	Integer[] snpPosList;
	private static int NUM_SNPS_WITHIN_READ = 1;
	
	@Override
	public void hooker(FileReader frSam, FileReader frSNPs, FileMaker fm) {
		if (frSNPs.getFileName().endsWith(".haps")) {
			// Read snp data from SHAPEIT
			System.out.println("Read snp data from SHAPEIT .haps");
			readHapsStoreSNPs(fm, frSNPs);
		} else {
			System.out.println("Read snp data from 10X phased, no-indel snps");
			readSNPsStoreSNPs(fm, frSNPs);
		}
		snpPosList = snpPosToPhasedSNPmap.keySet().toArray(new Integer[0]);
		Arrays.sort(snpPosList);
		
		// Read alignment file frSam, determine each snps in record
		System.out.println("Num of SNPs to be counted in reads: " + NUM_SNPS_WITHIN_READ);
		readSamDetermineSNP(frSam, fm);
		
	}
	
	private void readSNPsStoreSNPs(FileMaker fm, FileReader frSNPs) {
		String line;
		String[] tokens;
		int pos;
		PhasedSNP snp;
		String a;
		String b;
		FileMaker fmSnp = new FileMaker(fm.getDir(), fm.getFileName() + ".snp");
		while (frSNPs.hasMoreLines()) {
			line = frSNPs.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			pos = Integer.parseInt(tokens[PhasedSNP.POS]);
			a = tokens[PhasedSNP.HAPLOTYPE_A];
			b = tokens[PhasedSNP.HAPLOTYPE_B];
			snp = new PhasedSNP(tokens[PhasedSNP.CHR], pos, a, b, tokens[PhasedSNP.POS]);
			fmSnp.writeLine(tokens[PhasedSNP.CHR] + "\t" + pos + "\t" + a + "\t" + b);
			snpPosToPhasedSNPmap.put(pos, snp);
		}
		fmSnp.closeMaker();
	}

	private void readHapsStoreSNPs(FileMaker fm, FileReader frHaps) {
		String line;
		String[] tokens;
		int pos;
		PhasedSNP snp;
		String a;
		String b;
		FileMaker fmSnp = new FileMaker(fm.getDir(), fm.getFileName() + ".snp");
		while (frHaps.hasMoreLines()) {
			line = frHaps.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			//if (tokens[Haps.HAPLOTYPE_A].equals(tokens[Haps.HAPLOTYPE_B]))	continue;	// exclude homozygotes
			pos = Integer.parseInt(tokens[Haps.POS]);
			if (tokens[Haps.HAPLOTYPE_A].equals("0")) {
				a = tokens[Haps.ALLELE_A];
			} else {
				a = tokens[Haps.ALLELE_B];
			}
			if (tokens[Haps.HAPLOTYPE_B].equals("0")) {
				b = tokens[Haps.ALLELE_A];
			} else {
				b = tokens[Haps.ALLELE_B];
			}
			snp = new PhasedSNP(tokens[Haps.CHR], pos, a, b, tokens[Haps.POS]);
			fmSnp.writeLine(tokens[Haps.CHR] + "\t" + tokens[Haps.POS] + "\t" + a + "\t" + b);
			snpPosToPhasedSNPmap.put(pos, snp);
		}
		fmSnp.closeMaker();
	}
	
	private void readSamDetermineSNP(FileReader frSam, FileMaker fm) {
		String readID;
		ArrayList<PhasedSNP> snpsInRead;
		int seqStart;
		int seqEnd;
		int matchedLen;
		char baseInRead;
		String[] seqData = new String[2];
		StringBuffer haplotypes;
		ArrayList<Integer> snpsInReadPosList;
		int countA = 0;
		int countB = 0;
		int numSnps = 0;
		String line;
		String[] tokens;
		int pos;
		PhasedSNP snp;
		while (frSam.hasMoreLines()) {
			line = frSam.readLine();
			if (line.startsWith("@"))	continue;
			tokens = line.split(RegExp.TAB);
			readID = tokens[Sam.QNAME];
			seqStart = Integer.parseInt(tokens[Sam.POS]);
			matchedLen = SAMUtil.getMatchedBases(tokens[Sam.CIGAR]);
			seqEnd = seqStart + matchedLen - 1;
			seqData[SAMUtil.CIGAR] = tokens[Sam.CIGAR];
			seqData[SAMUtil.SEQ] = tokens[Sam.SEQ];
			snpsInRead = getSnpsInRead(seqStart, seqEnd, snpPosList, snpPosToPhasedSNPmap);
			if (snpsInRead.size() > 0) {
				haplotypes = new StringBuffer();
				countA = 0;
				countB = 0;
				snpsInReadPosList = new ArrayList<Integer>();
				for (int i = 0; i < snpsInRead.size(); i++) {
					snp = snpsInRead.get(i);
					pos = snp.getPos();
					baseInRead = SAMUtil.getBaseAtPos(pos, seqStart, seqData);
					if (baseInRead == snp.getHaplotypeA().charAt(0)) {
						countA++;
						haplotypes.append("A");
						snpsInReadPosList.add(pos);
					} else if (baseInRead == snp.getHaplotypeB().charAt(0)) {
						countB++;
						haplotypes.append("B");
						snpsInReadPosList.add(pos);
					}
				}
				numSnps = countA + countB;
				if (numSnps >= NUM_SNPS_WITHIN_READ) {	// if read contains at least NUM_SNPS_WITHIN_READ A / B determinable snp
					writeOut(fm, readID, countA, countB, seqStart, seqEnd, numSnps, haplotypes.toString(), snpsInReadPosList);
				}
			}
		}
	}
	
	private void writeOut(FileMaker fm, String readID,
			int countA, int countB, int seqStart, int seqEnd,
			int numSnps, String haplotypePattern, ArrayList<Integer> snpPosList) {
		fm.write(readID + "\t" + countA + "\t" + countB + "\t" + seqStart + "\t" + seqEnd + "\t" + (seqEnd - seqStart + 1) + "\t" + numSnps + "\t" + haplotypePattern);
		for (int i = 0; i < snpPosList.size(); i++) {
			fm.write("\t" + snpPosList.get(i));
		}
		fm.writeLine();
	}

	private ArrayList<PhasedSNP> getSnpsInRead(int seqStart, int seqEnd,
			Integer[] snpPosList,
			HashMap<Integer, PhasedSNP> snpPosToPhasedSNPmap) {
		
		ArrayList<PhasedSNP> snpsInRead = new ArrayList<PhasedSNP>();
		int snpStartIdx = Arrays.binarySearch(snpPosList, seqStart);
		// snpStartIdx will be the closest, min SNP greater than the seqStart
		if (snpStartIdx < 0) {
			snpStartIdx += 1;	// to find 1 pos right to the seqStart 
			snpStartIdx *= -1;
		}
		// all snps are smaller than seqStart
		if (snpStartIdx == snpPosList.length) {
			return snpsInRead;
		}
		
		int snpEndIdx = Arrays.binarySearch(snpPosList, seqEnd);
		// snpEndIdx will be the closest, max SNP less than the seqEnd
		if (snpEndIdx < 0) {
			snpEndIdx += 2;	// to find 1 pos left to the seqEnd 
			snpEndIdx *= -1;
		}
		if (snpEndIdx == snpPosList.length) {
			snpEndIdx = snpPosList.length - 1;
		}
		
		for (int idx = snpStartIdx; idx <= snpEndIdx; idx++) {
			snpsInRead.add(snpPosToPhasedSNPmap.get(snpPosList[idx]));
		}
		
		return snpsInRead;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: phasingSubreadBasedPhasedSNP.jar <in.sam> <snp_by_shapeit.haps/10X_noindel.snp> <phased.reads> [NUM_SNPS_WITHIN_READ]");
		System.out.println("\t<in.sam>: Subreads aligned to reference (hg19)");
		System.out.println("\t<snp_by_shapeit.haps>: .haps file generated with shapeit");
		System.out.println("\t\tCHR\tRS_ID\tPOS\tAlleleOfA\tAlleleOfB\tHaplotypeA\tHaplotypeB");
		System.out.println("\t<phased.reads>: READ_ID\tCOUNT_A\tCOUNT_B\tREAD_START_POS\tREAD_END_POS\tREAD_LEN\tNUM_SNPS\tHAPLOTYPE_PATTERN\tSNP_POS");
		System.out.println("\t\tNUM_SNPS = COUNT_A + COUNT_B");
		System.out.println("\t[NUM_SNPS_WITHIN_READ]: Read info with at least [NUM_SNPS_WITHIN_READ] will be printed. DEFUALT=2");
		System.out.println("\tRun this code on each chr seperately.");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new SubreadBasedPhasedSNP().go(args[0], args[1], args[2]);
		} else if (args.length == 4) {
			NUM_SNPS_WITHIN_READ = Integer.parseInt(args[3]);
			new SubreadBasedPhasedSNP().go(args[0], args[1], args[2]);
		} else {
			new SubreadBasedPhasedSNP().printHelp();
		}
	}

}
