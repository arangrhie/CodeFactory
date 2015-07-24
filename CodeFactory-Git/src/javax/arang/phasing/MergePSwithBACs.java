package javax.arang.phasing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.Format;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;
import javax.arang.genome.util.Util;

public class MergePSwithBACs extends I2Owrapper {

	private static final short PS1 = 1;
	private static final short PS2 = 2;
	
	private static final short PS = 3;
	private static final short LEN = 4;
	
	@Override
	public void hooker(FileReader frPhasedBed, FileReader frBAC, FileMaker fmMergedBed) {
		String line;
		String[] tokens;
		
		ArrayList<String> block1 = new ArrayList<String>();
		ArrayList<String> block2 = new ArrayList<String>();
		ArrayList<String> haplotypeBlock1 = new ArrayList<String>();
		ArrayList<String> haplotypeBlock2 = new ArrayList<String>();
		
		FileReader frSnp = new FileReader(phasedSnpPath);
		FileMaker fmMergedSnp = new FileMaker(mergedPhasedSnp);
		String ps;
		String psToWrite = "";
		String haplotype;
		
		// Add PS1:Haplotype1 PS2:Haplotype2 into list
		while (frBAC.hasMoreLines()) {
			line = frBAC.readLine();
			tokens = line.split(RegExp.TAB);
			
			// PS1
			ps = tokens[PS1].split(":")[0];
			haplotype = tokens[PS1].split(":")[1];
			block1.add(ps);
			haplotypeBlock1.add(haplotype);
			
			// PS2
			ps = tokens[PS2].split(":")[0];
			haplotype = tokens[PS2].split(":")[1];
			block2.add(ps);
			haplotypeBlock2.add(haplotype);
		}
		
		// Proceed phased.bed
		fmMergedBed.writeLine(frPhasedBed.readLine());
		String prevChr = null;
		String prevPS = null;
		int len;
		int prevLen = 0;
		HashMap<String, String> mergedPhaseBlockMap = new HashMap<String, String>();	// PS PS1_PS2 : <prev_ps> <merged_ps>
		ArrayList<Integer> blockLenArr = new ArrayList<Integer>();
		int connectedCount = 0;
		int numBlocks = 0;
		double blockLenSum = 0;
		String mergedPS = "";
		int prevStart = 0;
		int prevEnd = 0;
		int start;
		int end;
		
		boolean isFirstLine = true;
		while (frPhasedBed.hasMoreLines()) {
			line = frPhasedBed.readLine();
			tokens = line.split(RegExp.TAB);
			
			
			if (isFirstLine) {
				// read and store
				prevChr = tokens[Bed.CHROM];
				prevPS = tokens[PS];
				psToWrite = prevPS;
				prevStart = Integer.parseInt(tokens[Bed.START]);
				prevEnd = Integer.parseInt(tokens[Bed.END]);
				prevLen = Integer.parseInt(tokens[LEN]);
				isFirstLine = false;
				continue;
			}
			
			// collect chr, range, ps
			start = Integer.parseInt(tokens[Bed.START]);
			end = Integer.parseInt(tokens[Bed.END]);
			ps = tokens[PS];
			len = Integer.parseInt(tokens[LEN]);

			// check for connectability of prevPS and ps: if (prevChr.equals(tokens[Bed.CHR]))
			if (prevChr.equals(tokens[Bed.CHROM]) && getBlockIdx(block1, block2, prevPS, ps) > -1) {
				// merge prevPS and ps
				mergedPS = psToWrite + "_" + ps;
				if (psToWrite.contains("_")) {
					String[] merged = psToWrite.split("_");
					for (int i = 0; i < merged.length - 1; i++) {
						mergedPhaseBlockMap.put(merged[i], mergedPS);
					}
				} else {
					mergedPS = prevPS + "_" + ps;
				}
				mergedPhaseBlockMap.put(prevPS, mergedPS);
				mergedPhaseBlockMap.put(ps, mergedPS);
				prevEnd = end;
				psToWrite = mergedPS;
				prevLen += len;
				connectedCount++;
			} else {
				// Write previous ps block to output if not merge-able
				fmMergedBed.writeLine(prevChr + "\t" + prevStart + "\t" + prevEnd + "\t" + psToWrite + "\t" + prevLen);
				mergedPS = "";
				psToWrite = ps;
				numBlocks++;
				blockLenSum += prevLen;
				blockLenArr.add(prevLen);
				prevStart = start;
				prevEnd = end;
				prevLen = len;
			}
			prevPS = ps;
			prevChr = tokens[Bed.CHROM];
		}
		// Don't forget the last block to write
		fmMergedBed.writeLine(prevChr + "\t" + prevStart + "\t" + prevEnd + "\t" + psToWrite + "\t" + (prevEnd - prevStart));
		numBlocks++;
		blockLenArr.add(prevLen);

		System.out.println("Num. of merged blocks: " + Format.numbersToDecimal(connectedCount));
		System.out.println("Num. of phased blocks after merging: " + Format.numbersToDecimal(numBlocks));
		
		// Calculate n50
		Collections.sort(blockLenArr);
		int n50 = Util.getN50(blockLenArr, blockLenSum);
		System.out.println("N50: " + Format.numbersToDecimal(n50 + 1));
		
		System.out.println();
		
		// Print merged PS
		for (String psMerged : mergedPhaseBlockMap.values()) {
			System.out.println("[DEBUG] :: PS1_PS2 : " + psMerged);
		}
		
		// Proceed phased.snp
		while (frSnp.hasMoreLines()) {
			line = frSnp.readLine();
			if (line.startsWith("#")) {
				fmMergedSnp.writeLine(line);
				continue;
			}
			tokens = line.split(RegExp.TAB);
			ps = tokens[SNP_PS];
			// add merged block PS instead of ps
			if (mergedPhaseBlockMap.containsKey(ps)) {
				mergedPS = mergedPhaseBlockMap.get(ps);
				
				// get haplotype info
				if (isSwitched(mergedPhaseBlockMap, ps, mergedPS, block1, block2, haplotypeBlock1, haplotypeBlock2)) {
					// PS1:A PS2:B => swap PS2 haplotype
					fmMergedSnp.writeLine(tokens[CHR] + "\t" + tokens[POS] + "\t" + tokens[B] + "\t" + tokens[A] + "\t" + mergedPS);
				} else {
					fmMergedSnp.writeLine(tokens[CHR] + "\t" + tokens[POS] + "\t" + tokens[A] + "\t" + tokens[B] + "\t" +  mergedPS);
				}
				
			} else {
				fmMergedSnp.writeLine(tokens[CHR] + "\t" + tokens[POS] + "\t" + tokens[A] + "\t" + tokens[B] + "\t" +  ps);
			}
			
		}
		
		frSnp.closeReader();
		fmMergedSnp.closeMaker();
	}
	
	private boolean isSwitched(HashMap<String, String> mergedPhaseBlockMap,	String ps, String mergedPS,
			ArrayList<String> block1, ArrayList<String> block2,
			ArrayList<String> haplotypeBlock1, ArrayList<String> haplotypeBlock2) {
		
		// Split merged
		String[] mergedPSs = mergedPS.split("_");
		//System.out.println(mergedPSs[0] + " " + mergedPSs[1]);
		
		// Check if current ps is the 'next' phased block
		if (!mergedPSs[1].equals(ps)) {
			return false;
		}
		
		int i = getBlockIdx(block1, block2, mergedPSs[0], mergedPSs[1]);
		String haploA = haplotypeBlock1.get(i);
		String haploB = haplotypeBlock2.get(i);
		if (!haploA.equals(haploB)) {
			return true;
		}
		return false;
	}
	
	private int getBlockIdx(ArrayList<String> block1, ArrayList<String> block2, String prevPS, String ps) {
		for (int i = 0; i < block1.size(); i++) {
			if (block1.get(i).equals(prevPS) && block2.get(i).equals(ps)
			 || block2.get(i).equals(prevPS) && block1.get(i).equals(ps)) {
				return i;
			}
		}
		return -1;
	}

	private static short CHR = 0;
	private static short POS = 1;
	private static short A = 2;
	private static short B = 3;
	private static short SNP_PS = 4;
	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar <phasingMergePSwithBACs.jar"
				+ "<in.phased.bed> <bac_phased_block_bound.bpf> <phased.snp> <merged.phased.bed> <merged.phased.snp>");
		System.out.println("\t<in.phased.bed>: generated with phasingExtractPhasedSnp.jar");
		System.out.println("\t<bac_phased_block_bound.bpf>: BAC_ID PS:Haplotype");
		System.out.println("\t<phased.snp>: generated with phasingExtractPhasedSnp.jar");
		System.out.println("\tPrint N50, Longest block size, Average block size after merging with BAC");
		System.out.println("\tCorrect inverted haplotype blocks during merging");
		System.out.println("Arang Rhie, 2015-07-16. arrhie@gmail.com");
	}

	private static String phasedSnpPath;
	private static String mergedPhasedSnp;
	
	public static void main(String[] args) {
		if (args.length == 5) {
			phasedSnpPath = args[2];
			mergedPhasedSnp = args[4];
			new MergePSwithBACs().go(args[0], args[1], args[3]);
		} else {
			new MergePSwithBACs().printHelp();
		}
	}

}
