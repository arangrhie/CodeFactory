package javax.arang.phasing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.genome.util.Util;

public class PhasedReadsToSnpBlock extends I2Owrapper {

	private static final int SEQ_START = 3;
	private static final int SEQ_END = 4;
	private static final int HAPLOTYPES_LEN = 6;
	private static final int HAPLOTYPES = 7;
	private static final int SNP_POS_BEGIN = 8;
	
	private static final int NOT_SWITCHED_FROM_PREV_A_AND_IS_A = 0;
	private static final int NOT_SWITCHED_FROM_PREV_B_AND_IS_B = 1;
	private static final int SWITCHED_FROM_PREV_B_TO_A = 2;
	private static final int SWITCHED_FROM_PREV_A_TO_B = 3;
	
	private static final short IS_A = 0;
	private static final short IS_B = 1;
	private static final short IS_A_OR_B = 2; 
	
	@Override
	public void hooker(FileReader frPhasedReads, FileReader frSNPs, FileMaker fm) {
		
		// Read snp data from frHaps
		HashMap<Integer, PhasedSNP> snpPosToPhasedSNPmap = new HashMap<Integer, PhasedSNP>();
		HashMap<Integer, Integer[]> snpPosToCountsMap = new HashMap<Integer, Integer[]>();	// POS, A_COUNT B_COUNT SWITCHED_FROM_PREV_SNP SAME_HAPLOTYPE_FROM_PREV_SNP
		Integer[] snpPosList;
		String line;
		String[] tokens;
		int pos;
		PhasedSNP snp;
		String a;
		String b;
		String chr = "";
		while (frSNPs.hasMoreLines()) {
			line = frSNPs.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			//if (tokens[Haps.HAPLOTYPE_A].equals(tokens[Haps.HAPLOTYPE_B]))	continue;	// exclude homozygotes
			chr = tokens[PhasedSNP.CHR];
			pos = Integer.parseInt(tokens[PhasedSNP.POS]);
			a = tokens[PhasedSNP.HAPLOTYPE_A];
			b = tokens[PhasedSNP.HAPLOTYPE_B];
			snp = new PhasedSNP(chr, pos, a, b, tokens[PhasedSNP.POS]);
			snpPosToPhasedSNPmap.put(pos, snp);
			snpPosToCountsMap.put(pos, initArr(4));
		}
		
		
		// Read frPhasedReads
		int seqStart;
		int seqEnd;
		int prevBlockEnd = -1;
		String ps = "";
		Integer[] counts;
		int idx = 0;
		ArrayList<PhasedBlock> phasedBlocks = new ArrayList<PhasedBlock>();
		PhasedBlock block = null;
		int haplotypeLen = 0;
		int prevHaplotype = NOT_SWITCHED_FROM_PREV_A_AND_IS_A;
		int currentHaplotype;
		ArrayList<PhasedSNP> homoSNPsToPhase = new ArrayList<PhasedSNP>();;
		while (frPhasedReads.hasMoreLines()) {
			line = frPhasedReads.readLine();
			tokens = line.split(RegExp.TAB);
			seqStart = Integer.parseInt(tokens[SEQ_START]);
			seqEnd = Integer.parseInt(tokens[SEQ_END]);
			haplotypeLen = Integer.parseInt(tokens[HAPLOTYPES_LEN]);
			pos = Integer.parseInt(tokens[SNP_POS_BEGIN]);
			snp = snpPosToPhasedSNPmap.get(pos);
			
			if (haplotypeLen == 1 && snp.getHaplotypeA().equals(snp.getHaplotypeB())) {
				if (!snp.isPSset()) {
					snp.setPS("Homo");
				}
				continue;
			}
			
			if (prevBlockEnd < seqStart) {
				ps = tokens[SEQ_START];
				block = new PhasedBlock(chr, seqStart, seqEnd, ps);
				phasedBlocks.add(block);
				prevBlockEnd = seqEnd;
			} else if (block.getEnd() < seqEnd) {
				block.setBlockEnd(seqEnd);
				prevBlockEnd = seqEnd;
			}
			
			
			homoSNPsToPhase.clear();
			idx = 0;
			counts = snpPosToCountsMap.get(pos);
			// determine the 1st snp
			if (snp.getHaplotypeA().equals(snp.getHaplotypeB())) {
				prevHaplotype = IS_A_OR_B;
				if (!snp.isPSset()) {
					snp.setPS("Homo");
				}
				homoSNPsToPhase.add(snp);
			} else {
				prevHaplotype = (tokens[HAPLOTYPES].charAt(idx) == 'A') ? IS_A : IS_B;
				if (prevHaplotype == IS_A) {
					counts[NOT_SWITCHED_FROM_PREV_A_AND_IS_A]++;
				} else {
					counts[NOT_SWITCHED_FROM_PREV_B_AND_IS_B]++;
				}
				snp.setPS(ps);
			}
			
			if (haplotypeLen > 1) {
				for (int posIdx = SNP_POS_BEGIN + 1; posIdx < SNP_POS_BEGIN + haplotypeLen; posIdx++) {
					idx++;
					pos = Integer.parseInt(tokens[posIdx]);
					counts = snpPosToCountsMap.get(pos);
					snp = snpPosToPhasedSNPmap.get(pos);

					// currentHaplotype IS_A_OR_B
					if (snp.getHaplotypeA().equals(snp.getHaplotypeB())) {
						
						if (prevHaplotype == IS_A_OR_B) {
							// prevHaplotype and currentHaplotype are both IS_A_OR_B
							if (!snp.isPSset()) {
								snp.setPS("Homo");
							}
							homoSNPsToPhase.add(snp);
							continue;
						} else {
							// prevHaplotype is IS_A or IS_B and currentHaplotype is IS_A_OR_B: set currentHaplotype equal to prevHaplotype
							currentHaplotype = prevHaplotype;
							snp.setPS(ps);
							snp.setPSset(true);
							if (currentHaplotype == IS_A) {
								counts[NOT_SWITCHED_FROM_PREV_A_AND_IS_A]++;
							} else {
								counts[NOT_SWITCHED_FROM_PREV_B_AND_IS_B]++;
							}
						}
						// prevHaplotype does not change.
					}
					
					
					// currentHaplotype is not Homo. IS_A or IS_B.
					else {
						currentHaplotype = (tokens[HAPLOTYPES].charAt(idx) == 'A') ? IS_A : IS_B;
						if (prevHaplotype == IS_A_OR_B) {
							if (currentHaplotype == IS_A) {
								counts[NOT_SWITCHED_FROM_PREV_A_AND_IS_A]++;
							} else {	// IS_B
								counts[NOT_SWITCHED_FROM_PREV_B_AND_IS_B]++;
							}
							// set previous "homo" marked snps to currentHaplotype block.
							for (int i = 0; i < homoSNPsToPhase.size(); i++) {
								snp = homoSNPsToPhase.get(i);
								snp.setPS(ps);
								snp.setPSset(true);
								counts = snpPosToCountsMap.get(snp.getPos());
								if (currentHaplotype == IS_A) {
									counts[NOT_SWITCHED_FROM_PREV_A_AND_IS_A]++;
								} else if (currentHaplotype == IS_B){
									counts[NOT_SWITCHED_FROM_PREV_B_AND_IS_B]++;
								}
							}
							homoSNPsToPhase.clear();
						} else {
							// prevHaplotype IS_A or IS_B
							if (currentHaplotype != prevHaplotype) {
								if (currentHaplotype == IS_A) {
									counts[SWITCHED_FROM_PREV_B_TO_A]++;
								} else if (currentHaplotype == IS_B){
									counts[SWITCHED_FROM_PREV_A_TO_B]++;
								}
							} else if (currentHaplotype == prevHaplotype) {
								if (currentHaplotype == IS_A) {
									counts[NOT_SWITCHED_FROM_PREV_A_AND_IS_A]++;
								} else {
									counts[NOT_SWITCHED_FROM_PREV_B_AND_IS_B]++;
								}
							}
							snp.setPS(ps);
						}
						prevHaplotype = currentHaplotype;
					}
				}
			}
		}
		
		snpPosList = snpPosToPhasedSNPmap.keySet().toArray(new Integer[0]);
		Arrays.sort(snpPosList);
		
		String countText = "";
		int countSum = 0;
		for (int i = 0; i < snpPosList.length; i++) {
			pos = snpPosList[i];
			snp = snpPosToPhasedSNPmap.get(pos);
			counts = snpPosToCountsMap.get(pos);
			countText = snp.getChr() + "\t" + snp.getPos() + "\t" + snp.getHaplotypeA() + "\t" + snp.getHaplotypeB() + "\t" + snp.getPS();
			countSum = 0;
			for (int j = 0; j < counts.length; j++) {
				countText = countText + "\t" + counts[j];
				countSum += counts[j];
			}
			if (countSum > 0) {
				fm.writeLine(countText);
			}
		}
		
		FileMaker fmPhasedBlocks = new FileMaker(fm.getDir(), fm.getFileName() + ".blocks.bed");
		ArrayList<Integer> blockArr = new ArrayList<Integer>();
		double lenSum = 0;
		int len;
		for (int i = 0; i < phasedBlocks.size(); i++) {
			block = phasedBlocks.get(i);
			len = block.getEnd() - block.getStart() + 1;
			blockArr.add(len);
			lenSum += len;
			fmPhasedBlocks.writeLine(block.getChr() + "\t" + (block.getStart() - 1) + "\t" + block.getEnd() + "\t" + block.getPS() + "\t" + len);
		}
		fmPhasedBlocks.closeMaker();
		Collections.sort(blockArr);
		int n50 = Util.getN50(blockArr, lenSum);
		
		System.out.println("Phased block coverage: " + String.format("%,.0f", lenSum));
		System.out.println("Total num. of phased blocks: " + phasedBlocks.size());
		System.out.println("N50: " + String.format("%,d", n50));
		System.out.println("For simplicity; order is longest block size, block N50, num. blocks, genome covered bases:");
		System.out.println(blockArr.get(blockArr.size() - 1));
		System.out.println(n50);
		System.out.println(phasedBlocks.size());
		System.out.println(String.format("%.0f", lenSum));
		System.out.println();
		
	}
	
	private Integer[] initArr(int len) {
		Integer[] arr = new Integer[len];
		for (int i = 0; i < len; i++) {
			arr[i] = 0;
		}
		return arr;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingPhasedReadsToSnpBlock.jar <phased.reads> <phased.reads.snp> <out.phased.snp>");
		System.out.println("\t<phased.reads>, <phased.reads.snp>: generated with SubreadBasedPhasedSNP.jar");
		System.out.println("\t<out.phased.snp>: CHR POS HAPLOTYPE_A HAPLOTYPE_B PS NOT_SWITCHED_FROM_PREV_A_AND_IS_A NOT_SWITCHED_FROM_PREV_B_AND_IS_B SWITCHED_FROM_PREV_B_TO_A SWITCHED_FROM_PREV_A_TO_B");
		System.out.println("\t\tHomozygote SNPs are determined if previous or next heterozygote snp is available, and is set along with it's haplotype.");
		System.out.println("Arang Rhie, 2015-07-24. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new PhasedReadsToSnpBlock().go(args[0], args[1], args[2]);
		} else {
			new PhasedReadsToSnpBlock().printHelp();
		}
	}

}
