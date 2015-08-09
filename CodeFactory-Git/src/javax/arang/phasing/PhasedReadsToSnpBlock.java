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

	private static final short IS_A = 0;
	private static final short IS_B = 1;
	private static final short IS_HOMO = 2; 
	
	@Override
	public void hooker(FileReader frPhasedReads, FileReader frSNPs, FileMaker fm) {
		
		// Read snp data from frHaps
		HashMap<Integer, PhasedSNP> snpPosToPhasedSNPmap = PhasedSNP.readSNPsStoreSNPs(frSNPs, false);
		HashMap<Integer, Integer[]> snpPosToCountsMap = new HashMap<Integer, Integer[]>();	// POS, A_COUNT B_COUNT SWITCHED_FROM_PREV_SNP SAME_HAPLOTYPE_FROM_PREV_SNP
		
		String line;
		String[] tokens;
		for (int pos : snpPosToPhasedSNPmap.keySet()) {
			snpPosToCountsMap.put(pos, initArr(PhasedSNP.FILTER));
		}
		
		
		// Read frPhasedReads
		String chr;
		int pos;
		int seqStart;
		int seqEnd;
		int prevBlockEnd = -1;
		PhasedSNP snp;
		String ps = "";
		Integer[] counts;
		int idx = 0;
		ArrayList<PhasedBlock> phasedBlocks = new ArrayList<PhasedBlock>();
		PhasedBlock block = null;
		int countA;
		int countB;
		int countO;
		int haplotypeLen = 0;
		int prevHaplotype = PhasedSNP.NOT_SWITCHED_FROM_PREV_A_AND_IS_A;
		int currentHaplotype;
		ArrayList<PhasedSNP> homoSNPsToPhase = new ArrayList<PhasedSNP>();;
		while (frPhasedReads.hasMoreLines()) {
			line = frPhasedReads.readLine();
			tokens = line.split(RegExp.TAB);
			seqStart = Integer.parseInt(tokens[PhasedRead.START]);
			seqEnd = Integer.parseInt(tokens[PhasedRead.END]);
			countA = Integer.parseInt(tokens[PhasedRead.NUM_A]);
			countB = Integer.parseInt(tokens[PhasedRead.NUM_B]);
			countO = Integer.parseInt(tokens[PhasedRead.NUM_O]);
			haplotypeLen = tokens[PhasedRead.HAPLOTYPE].length();
			pos = Integer.parseInt(tokens[PhasedRead.SNP_POS_LIST]);
			snp = snpPosToPhasedSNPmap.get(pos);	// 1st SNP
			chr = snp.getChr();
			counts = snpPosToCountsMap.get(pos);
			
			// SNPs are homo : undeterminable
			if (countA == 0 && countB == 0 && countO > 0) {
				for (int posIdx = PhasedRead.SNP_POS_LIST; posIdx < PhasedRead.SNP_POS_LIST + haplotypeLen; posIdx++) {
					pos = Integer.parseInt(tokens[posIdx]);
					counts = snpPosToCountsMap.get(pos);
					snp = snpPosToPhasedSNPmap.get(pos);
					if (!snp.isPSset()) {
						snp.setPS("Homo");
					}
					counts[PhasedSNP.IS_UNDETERMINABLE_HOMO]++;
				}
				continue;	// this SNP will not be used for phased block extension
			}
			
			// phased block extension
			if (prevBlockEnd < seqStart && !snp.isPSset()) {
				ps = tokens[PhasedRead.START];
				block = new PhasedBlock(chr, seqStart, seqEnd, ps);
				phasedBlocks.add(block);
				prevBlockEnd = seqEnd;
			} else if (block.getEnd() < seqEnd) {
				block.setBlockEnd(seqEnd);
				prevBlockEnd = seqEnd;
			}
			
			
			homoSNPsToPhase.clear();
			idx = 0;
			
			// determine the 1st snp
			if (snp.isHom()) {
				// Homo
				prevHaplotype = IS_HOMO;
				if (!snp.isPSset()) {
					snp.setPS("Homo");
				}
				homoSNPsToPhase.add(snp);
			} else {
				// Hetero
				prevHaplotype = (tokens[PhasedRead.HAPLOTYPE].charAt(idx) == 'A') ? IS_A : IS_B;
				if (prevHaplotype == IS_A) {
					if (haplotypeLen == 1) {
						counts[PhasedSNP.IS_SINGLE_A]++;
					} else {
						counts[PhasedSNP.IS_FIRST_A]++;
					}
				} else {
					if (haplotypeLen == 1) {
						counts[PhasedSNP.IS_SINGLE_B]++;
					} else {
						counts[PhasedSNP.IS_FIRST_B]++;
					}
				}
				snp.setPS(ps);
				snp.setPSset(true);
			}
			
			// determine the rest of the snps
			if (haplotypeLen > 1) {
				for (int posIdx = PhasedRead.SNP_POS_LIST + 1; posIdx < PhasedRead.SNP_POS_LIST + haplotypeLen; posIdx++) {
					idx++;
					pos = Integer.parseInt(tokens[posIdx]);
					counts = snpPosToCountsMap.get(pos);
					snp = snpPosToPhasedSNPmap.get(pos);

					// currentHaplotype IS_HOMO
					if (snp.isHom()) {
						if (prevHaplotype == IS_HOMO) {
							// prevHaplotype and currentHaplotype are both IS_HOMO
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
								counts[PhasedSNP.NOT_SWITCHED_FROM_PREV_A_AND_IS_A]++;
							} else if (currentHaplotype == IS_B) {
								counts[PhasedSNP.NOT_SWITCHED_FROM_PREV_B_AND_IS_B]++;
							}
						}
						// prevHaplotype does not change.
					} else {
						// currentHaplotype snp is not Homo. IS_A or IS_B.
						currentHaplotype = (tokens[PhasedRead.HAPLOTYPE].charAt(idx) == 'A') ? IS_A : IS_B;
						if (prevHaplotype == IS_HOMO) {
							snp.setPS(ps);
							snp.setPSset(true);
							if (currentHaplotype == IS_A) {
								counts[PhasedSNP.NOT_SWITCHED_FROM_PREV_A_AND_IS_A]++;
							} else if (currentHaplotype == IS_B) {	// IS_B
								counts[PhasedSNP.NOT_SWITCHED_FROM_PREV_B_AND_IS_B]++;
							}
							// set previous "homo" marked snps to currentHaplotype block.
							for (int i = 0; i < homoSNPsToPhase.size(); i++) {
								snp = homoSNPsToPhase.get(i);
								snp.setPS(ps);
								snp.setPSset(true);
								counts = snpPosToCountsMap.get(snp.getPos());
								if (currentHaplotype == IS_A) {
									counts[PhasedSNP.NOT_SWITCHED_FROM_PREV_A_AND_IS_A]++;
								} else if (currentHaplotype == IS_B){
									counts[PhasedSNP.NOT_SWITCHED_FROM_PREV_B_AND_IS_B]++;
								}
							}
							homoSNPsToPhase.clear();
						} else {
							// prevHaplotype IS_A or IS_B
							if (currentHaplotype != prevHaplotype) {
								if (currentHaplotype == IS_A) {
									counts[PhasedSNP.SWITCHED_FROM_PREV_B_TO_A]++;
								} else if (currentHaplotype == IS_B){
									counts[PhasedSNP.SWITCHED_FROM_PREV_A_TO_B]++;
								}
							} else if (currentHaplotype == prevHaplotype) {
								if (currentHaplotype == IS_A) {
									counts[PhasedSNP.NOT_SWITCHED_FROM_PREV_A_AND_IS_A]++;
								} else if (currentHaplotype == IS_B) {
									counts[PhasedSNP.NOT_SWITCHED_FROM_PREV_B_AND_IS_B]++;
								}
							}
							snp.setPS(ps);
							snp.setPSset(true);
						}
						prevHaplotype = currentHaplotype;
					}
				}
				if (homoSNPsToPhase.size() > 0) {
					for (int i = 0; i < homoSNPsToPhase.size(); i++) {
						snp = homoSNPsToPhase.get(i);
						counts = snpPosToCountsMap.get(snp.getPos());
						counts[PhasedSNP.IS_UNDETERMINABLE_HOMO]++;
					}
				}
			}
		}
		
		Integer[] snpPosList = snpPosToPhasedSNPmap.keySet().toArray(new Integer[0]);
		Arrays.sort(snpPosList);
		
		String countText = "";
//		int countSum = 0;
		fm.writeLine("#CHR\tPOS\tHapA\tHapB\tPS\tIS_FIRST_A\tIS_FIRST_B\tAA\tBB\tBA\tAB\tSingleA\tSingleB\tUndeterminableHomo");
		for (int i = 0; i < snpPosList.length; i++) {
			pos = snpPosList[i];
			snp = snpPosToPhasedSNPmap.get(pos);
			counts = snpPosToCountsMap.get(pos);
			countText = snp.getChr() + "\t" + snp.getPos() + "\t" + snp.getHaplotypeA() + "\t" + snp.getHaplotypeB() + "\t" + snp.getPS();
//			countSum = 0;
			for (int j = 0; j < counts.length; j++) {
				countText = countText + "\t" + counts[j];
//				countSum += counts[j];
			}
//			if (countSum > 0) {
				fm.writeLine(countText);
//			}
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
		System.out.println("\t<out.phased.snp>: CHR POS HAPLOTYPE_A HAPLOTYPE_B PS IS_FIRST_A IS_FIRST_B"
																			+ " NOT_SWITCHED_FROM_PREV_A_AND_IS_A"
																			+ " NOT_SWITCHED_FROM_PREV_B_AND_IS_B"
																			+ " SWITCHED_FROM_PREV_B_TO_A"
																			+ " SWITCHED_FROM_PREV_A_TO_B"
																			+ " SINGLE_A SINGLE_B"
																			+ " IS_UNDETERMINABLE_HOMO");
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
