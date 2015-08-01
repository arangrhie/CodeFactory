package javax.arang.phasing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.genome.sam.SAMUtil;
import javax.arang.genome.sam.Sam;

public abstract class Phase extends R2wrapper {
	
	public static int NUM_SNPS_WITHIN_READ = 1;
	
	public void readSamDetermineSNP(FileReader frSam, Integer[] snpPosList, HashMap<Integer, PhasedSNP> snpPosToPhasedSNPmap) {
		String readID;
		ArrayList<PhasedSNP> snpsInRead;
		int seqStart;
		int seqEnd;
		int matchedLen;
		char baseInRead;
		String[] seqData = new String[2];
		StringBuffer haplotypes = new StringBuffer();
		ArrayList<Integer> snpsInReadPosList = new ArrayList<Integer>();
		int countA = 0;
		int countB = 0;
		String line;
		String[] tokens;
		int pos;
		PhasedSNP snp;
		while (frSam.hasMoreLines()) {
			line = frSam.readLine();
			if (line.startsWith("@")){
				readSamHeader(line);
				continue;
			}
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
			}
			determineRead(line, readID, countA, countB, seqStart, seqEnd, snpsInRead, haplotypes.toString(), snpsInReadPosList);
		}
	}
	
	public abstract void readSamHeader(String line);
	public abstract void determineRead(String line, String readID, int countA, int countB, int seqStart, int seqEnd, ArrayList<PhasedSNP> snpsInRead, String haplotype, ArrayList<Integer> snpsInReadPosList);
	
	public static HashMap<Integer, PhasedSNP> readSNPsStoreSNPs(FileReader frSNPs) {
		HashMap<Integer, PhasedSNP> snpPosToPhasedSNPmap = new HashMap<Integer, PhasedSNP>();
		String line;
		String[] tokens;
		int pos;
		PhasedSNP snp;
		String a;
		String b;
		while (frSNPs.hasMoreLines()) {
			line = frSNPs.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			pos = Integer.parseInt(tokens[PhasedSNP.POS]);
			a = tokens[PhasedSNP.HAPLOTYPE_A];
			b = tokens[PhasedSNP.HAPLOTYPE_B];
			snp = new PhasedSNP(tokens[PhasedSNP.CHR], pos, a, b, tokens[PhasedSNP.POS]);
			snpPosToPhasedSNPmap.put(pos, snp);
		}
		return snpPosToPhasedSNPmap;
	}
	
	public static ArrayList<PhasedSNP> getSnpsInRead(int seqStart, int seqEnd,
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

}
