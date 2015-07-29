package javax.arang.phasing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.genome.util.Util;

public class MergePSwithSubreads extends R2wrapper {

	private static final int PS = 3;
	private static final int AA_OR_BB = 4;
	private static final int AB_OR_BA = 5;
	private static final int AMBIGUOUS = 6;
	
	@Override
	public void hooker(FileReader frGapBlock, FileReader frSNP) {
		String line;
		String[] tokens;
		
		int notToSwitch = 0;
		int toSwitch = 0;
		int ambiguous = 0;
		
		HashMap<String, String> ps1BlocksToConnectMap = new HashMap<String, String>();
		HashMap<String, String> ps2BlocksToConnectMap = new HashMap<String, String>();
		HashMap<String, Boolean> ps2BlocksToConnectWithSwitchMap = new HashMap<String, Boolean>();
		
		ArrayList<String> ps1List = new ArrayList<String>();
		
		// Read block based subread merged .bed
		String[] psBlocks;
		while (frGapBlock.hasMoreLines()) {
			line = frGapBlock.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			notToSwitch = Integer.parseInt(tokens[AA_OR_BB]);
			toSwitch = Integer.parseInt(tokens[AB_OR_BA]);
			ambiguous = Integer.parseInt(tokens[AMBIGUOUS]);
			if (notToSwitch == toSwitch)	continue;
			if (ambiguous > Math.max(notToSwitch, toSwitch))	continue;
			psBlocks = tokens[PS].split(":");
			ps1BlocksToConnectMap.put(psBlocks[0], psBlocks[1]);
			ps1List.add(psBlocks[0]);
			ps2BlocksToConnectMap.put(psBlocks[1], psBlocks[0]);
			if (notToSwitch < toSwitch) {
				ps2BlocksToConnectWithSwitchMap.put(psBlocks[1], true);
			} else {
				ps2BlocksToConnectWithSwitchMap.put(psBlocks[1], false);
			}
		}
		
		
		// Generate block PS to merged PS names
		HashMap<String, String> psToNewPS = new HashMap<String, String>();
		String newPS = "";
		String ps;
		
		ArrayList<String> psListToAddNewPs = new ArrayList<String>();
		
		int numBlocks = 0;
		for (int i = 0; i < ps1List.size(); i++) {
			ps = ps1List.get(i);
			if (psToNewPS.containsKey(ps))	continue;
			newPS = ps;
			psListToAddNewPs.clear();
			psListToAddNewPs.add(ps);
			while (ps1BlocksToConnectMap.containsKey(ps)) {
				ps = ps1BlocksToConnectMap.get(ps);
				newPS = newPS + "_" + ps;
				psListToAddNewPs.add(ps); 
			}
			for (int j = 0; j < psListToAddNewPs.size(); j++) {
				psToNewPS.put(psListToAddNewPs.get(j), newPS);
			}
			numBlocks++;
		}
		System.out.println((ps1List.size() + 1) + " block gaps will be merged into " + numBlocks + " blocks.");
		
		
		FileMaker fmSnp = new FileMaker(outprefix + ".snp");
		FileMaker fmBed = new FileMaker(outprefix + ".bed");
		boolean switchHaplotypes = false;
		String prevPS = "";
		String prevNewPS = "";
		int prevStart = 0;
		String prevEnd = "";
		boolean isFirstLine = true;
		ArrayList<Integer> blockLenArr = new ArrayList<Integer>();
		double lenSum = 0;
		while (frSNP.hasMoreLines()) {
			line = frSNP.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			ps = tokens[PhasedSNP.PS];
			if (!ps.equals(prevPS)) {
				prevPS = ps;
				if (psToNewPS.containsKey(ps)) {
					newPS = psToNewPS.get(ps);
				} else {
					switchHaplotypes = false;
					newPS = ps;
				}
				if (!newPS.equals(prevNewPS)) {
					if (isFirstLine) {
						isFirstLine = false;
						prevStart = Integer.parseInt(tokens[PhasedSNP.POS]) - 1;
						fmBed.write(tokens[PhasedSNP.CHR] + "\t" + prevStart);
					} else {
						int len = Integer.parseInt(prevEnd) - prevStart;
						lenSum += len;
						blockLenArr.add(len);
						fmBed.writeLine("\t" + prevEnd + "\t" + prevNewPS + "\t" + len);
						prevStart = Integer.parseInt(tokens[PhasedSNP.POS]) - 1;
						fmBed.write(tokens[PhasedSNP.CHR] + "\t" + prevStart);
					}
					prevNewPS = newPS;
				}
				if (ps2BlocksToConnectWithSwitchMap.containsKey(ps) && ps2BlocksToConnectWithSwitchMap.get(ps)) {
					switchHaplotypes = !switchHaplotypes;
				}
			}
			if (switchHaplotypes) {
				fmSnp.writeLine(tokens[PhasedSNP.CHR] + "\t" + tokens[PhasedSNP.POS] + "\t" + tokens[PhasedSNP.HAPLOTYPE_B] + "\t" + tokens[PhasedSNP.HAPLOTYPE_A]  + "\t" + newPS);
			} else {
				fmSnp.writeLine(tokens[PhasedSNP.CHR] + "\t" + tokens[PhasedSNP.POS] + "\t" + tokens[PhasedSNP.HAPLOTYPE_A] + "\t" + tokens[PhasedSNP.HAPLOTYPE_B]  + "\t" + newPS);
			}
			prevEnd = tokens[PhasedSNP.POS];
		}
		int len = Integer.parseInt(prevEnd) - prevStart;
		lenSum += len;
		blockLenArr.add(len);
		fmBed.writeLine("\t" + prevEnd + "\t" + prevNewPS + "\t" + len);
		
		Collections.sort(blockLenArr);
		System.out.println("Num. Phased Blocks: " + blockLenArr.size());
		System.out.println("Genome Coverage: " + lenSum);
		System.out.println("N50: " + Util.getN50(blockLenArr, lenSum));
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingMergePSwithSubreads.jar <BAC_gap_merged.block.bed> <hg19_AK1_10X_phase_merge.snp> <out_prefix>");
		System.out.println("\t<BAC_gap_merged.block>: Output of FindPSgapsCoveredWithSubreads.jar");
		System.out.println("\t<hg19_AK1_10X_phase_merge.snp>: Output of MergePSwithBACs.jar");
		System.out.println("\t<out_prefix>: ex. hg19_AK1_10X_phase_BAC_subread. .snp and .block.bed file will be made.");
		System.out.println("Arang Rhie, 2015-07-28. arrhie@gmail.com");
	}

	private static String outprefix;
	public static void main(String[] args) {
		if (args.length == 3) {
			outprefix = args[2];
			new MergePSwithSubreads().go(args[0], args[1]);
		} else {
			new MergePSwithSubreads().printHelp();
		}
	}

}
