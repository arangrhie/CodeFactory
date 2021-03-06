package javax.arang.phasing.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.base.util.Base;
import javax.arang.sam.SAMUtil;
import javax.arang.sam.Sam;

public abstract class Phase extends R2wrapper {
	
	protected static String outPrefix = "";
	protected static int NUM_SNPS_WITHIN_READ = 0;
	
	public void readBaseDetermineSNP(FileReader frBase, HashMap<Integer, PhasedSNP> snpPosToPhasedSNPmap) {
		String line;
		String[] tokens;
		String readID = frBase.getFileName();
		ArrayList<PhasedSNP> snpsInRead = new ArrayList<PhasedSNP>();
		
		boolean isStart = true;
		int seqStart = 0;
		int seqEnd = 1;
		char baseInRead;
		StringBuffer haplotypes = new StringBuffer();
		int countA = 0;
		int countB = 0;
		int countO = 0;
		int pos;
		PhasedSNP snp;
		ArrayList<Integer> snpsInReadPosList = new ArrayList<Integer>();
		while (frBase.hasMoreLines()) {
			line = frBase.readLine();
			if (line.startsWith("#")){
				continue;
			}
			tokens = line.split(RegExp.TAB);
			pos = Integer.parseInt(tokens[Base.POS]);
			if (isStart) {
				seqStart = pos;
				seqEnd = pos;
				isStart = false;
			} else {
				seqEnd = pos;
			}
			if (snpPosToPhasedSNPmap.containsKey(pos)) {
				snp = snpPosToPhasedSNPmap.get(pos);
				baseInRead = Base.maxLikelyBase(tokens[Base.CHR], pos,
						tokens[Base.A], tokens[Base.C], tokens[Base.G], tokens[Base.T], tokens[Base.D]).charAt(0);
				if (snp.isHom()) {
					haplotypes.append("H");
				} else if (baseInRead == snp.getHaplotypeA().charAt(0)) {
					countA++;
					haplotypes.append("A");
				} else if (baseInRead == snp.getHaplotypeB().charAt(0)) {
					countB++;
					haplotypes.append("B");
				} else if (baseInRead == 'D') {
					countO++;
					haplotypes.append("D");
				} else {
					countO++;
					haplotypes.append(Character.toLowerCase(baseInRead));
				}
				snpsInReadPosList.add(pos);
				snpsInRead.add(snp);
			}
		}
		determineRead(readID, readID, countA, countB, countO, seqStart, seqEnd, snpsInRead, haplotypes.toString(), snpsInReadPosList);
	}
	
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
		int countO = 0;
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
			snpsInRead = getPosInRead(seqStart, seqEnd, snpPosList, snpPosToPhasedSNPmap);
			//System.out.println(readID + " " + seqStart + " - " + seqEnd + " " + snpsInRead.size());
			haplotypes = new StringBuffer();
			countA = 0;
			countB = 0;
			countO = 0;
			snpsInReadPosList = new ArrayList<Integer>();
			if (snpsInRead.size() > 0) {
				for (int i = 0; i < snpsInRead.size(); i++) {
					snp = snpsInRead.get(i);
					pos = snp.getPos();
					baseInRead = SAMUtil.getBaseAtPos(pos, seqStart, seqData);
					if (snp.isHom()) {
						haplotypes.append("H");
					} else if (baseInRead == snp.getHaplotypeA().charAt(0)) {
						countA++;
						haplotypes.append("A");
					} else if (baseInRead == snp.getHaplotypeB().charAt(0)) {
						countB++;
						haplotypes.append("B");
					} else if (baseInRead == 'D') {
						countO++;
						haplotypes.append("D");
					} else {
						countO++;
						haplotypes.append(Character.toLowerCase(baseInRead));
					}
					snpsInReadPosList.add(pos);
				}
			}
			determineRead(line, readID, countA, countB, countO, seqStart, seqEnd, snpsInRead, haplotypes.toString(), snpsInReadPosList);
		}
	}
	
	public static void writeOut(FileMaker fm, String readID,
			int countA, int countB, int countO, int seqStart, int seqEnd,
			String haplotypePattern, ArrayList<Integer> snpPosList) {
		fm.write(readID + "\t" + countA + "\t" + countB + "\t" + countO + "\t"
			+ seqStart + "\t" + seqEnd + "\t" + (seqEnd - seqStart + 1) + "\t" + haplotypePattern);
		
		for (int i = 0; i < snpPosList.size(); i++) {
			fm.write("\t" + snpPosList.get(i));
		}
		fm.writeLine();
	}
	public abstract void readSamHeader(String line);
	public abstract void determineRead(String line, String readID, int countA, int countB, int countO, int seqStart, int seqEnd, ArrayList<PhasedSNP> snpsInRead, String haplotype, ArrayList<Integer> snpsInReadPosList);
	public abstract void writePhasedSNP(String chr, int pos, String haplotypeA, String haplotypeB);
	
	public HashMap<Integer, PhasedSNP> readSNPsStoreSNPs(FileReader frSNPs) {
		HashMap<Integer, PhasedSNP> snpPosToPhasedSNPmap = new HashMap<Integer, PhasedSNP>();
		String line;
		String[] tokens;
		int pos;
		PhasedSNP snp;
		String a;
		String b;
		while (frSNPs.hasMoreLines()) {
			line = frSNPs.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.WHITESPACE);
			pos = Integer.parseInt(tokens[PhasedSNP.POS]);
			a = tokens[PhasedSNP.HAPLOTYPE_A];
			b = tokens[PhasedSNP.HAPLOTYPE_B];
			snp = new PhasedSNP(tokens[PhasedSNP.CHR], pos, a, b, tokens[PhasedSNP.POS]);
			snpPosToPhasedSNPmap.put(pos, snp);
			writePhasedSNP(tokens[PhasedSNP.CHR], pos, a, b);
		}
		return snpPosToPhasedSNPmap;
	}
	
	public HashMap<Integer, PhasedSNP> readHapsStoreSNPs(FileReader frHaps) {
		HashMap<Integer, PhasedSNP> snpPosToPhasedSNPmap = new HashMap<Integer, PhasedSNP>();
		String line;
		String[] tokens;
		int pos;
		PhasedSNP snp;
		String a;
		String b;
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
			snpPosToPhasedSNPmap.put(pos, snp);
			writePhasedSNP(tokens[Haps.CHR], pos, a, b);
		}
		return snpPosToPhasedSNPmap;
	}
	
	public static ArrayList<PhasedSNP> getPosInRead(int seqStart, int seqEnd,
			Integer[] snpPosList,
			HashMap<Integer, PhasedSNP> snpPosToPhasedSNPmap) {
		
		ArrayList<PhasedSNP> snpsInRead = new ArrayList<PhasedSNP>();
		int snpStartIdx = Arrays.binarySearch(snpPosList, seqStart);
		// snpStartIdx will be the closest, min SNP equals or greater than the seqStart
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
		} else if (snpEndIdx == snpPosList.length) {
			snpEndIdx = snpPosList.length - 1;
		}
		
		if (snpEndIdx < 0)	return snpsInRead;	// smallest SNP is greater than seqEnd
		
		for (int idx = snpStartIdx; idx <= snpEndIdx; idx++) {
			snpsInRead.add(snpPosToPhasedSNPmap.get(snpPosList[idx]));
		}
		
		return snpsInRead;
	}

}
