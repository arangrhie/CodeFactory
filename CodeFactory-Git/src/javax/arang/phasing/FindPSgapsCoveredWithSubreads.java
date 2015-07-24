package javax.arang.phasing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.bambasic.BamReader;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.IOUtil;
import javax.arang.IO.basic.RegExp;
import javax.arang.bam.util.BamRecord;
import javax.arang.bam.util.RefInfo;
import javax.arang.genome.sam.SAMUtil;
import javax.arang.genome.sam.Sam;

public class FindPSgapsCoveredWithSubreads extends IOwrapper {
	
	private static String mergedSnpPath;
	private static String outPrefix;
	private static String inSam;
	private static boolean isBam = false;
	
	private final static int NUM_SNPS_TO_COMPARE = 1;
	
	static final int LEFT_A = 0;
	static final int LEFT_B = 1;
	static final int LEFT_OTHER = 2;
	static final int RIGHT_A = 3;
	static final int RIGHT_B = 4;
	static final int RIGHT_OTHER = 5;
	
	static final int AAorBB = 0;
	static final int ABorBA = 1;
	static final int AMBIGUOUS = 2;
	static final int UNKNOWN = 3;
	
	HashMap<PhasedSNP, ArrayList<PhasedBlock>> phasedSnpToGapsMap = new HashMap<PhasedSNP, ArrayList<PhasedBlock>>();	// SNP, <Gap> list
	HashMap<String, HashMap<Integer, PhasedSNP>> chrPosOfSnpListMap = new HashMap<String, HashMap<Integer, PhasedSNP>>();

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingFindPSgapsCoveredWithSubreads.jar <subreads.region.sam> <ps_gaps.bed> <BAC_merged.snp> <out_prefix>");
		System.out.println("\tSame as MergePSwithBACs, use subread's SNP info instead of BACs");
		System.out.println("\t<ps_gaps.bed>: format CHR START END PS_PS");
		System.out.println("\t\tRemove phase block gaps that is caused by reference's gaps");
		System.out.println("\t<BAC_merged.bed>: CHR START END PS LEN");
		System.out.println("\t<BAC_merged.snp>: CHR POS HaplotypeA HaplotypeB PS");
		System.out.println("\t<out_prefix>.bed, <out_prefix>.snp");
	}

	public static void main(String[] args) {
		if (args.length == 4) {
			inSam = args[0];
			mergedSnpPath = args[2];
			outPrefix = args[3];
			if (args[0].endsWith(".bam")) {
				isBam = true;
			} else {
				isBam = false;
			}
			new FindPSgapsCoveredWithSubreads().go(args[1], outPrefix + ".bed");
		} else {
			new FindPSgapsCoveredWithSubreads().printHelp();
		}
	}
	
	private ArrayList<PhasedBlock> readGaps(FileReader frGapsBed) {
		String line;
		String[] tokens;
		ArrayList<PhasedBlock> gapList = new ArrayList<PhasedBlock>();
		PhasedBlock block;
		
		// Read the gap info: target PS to PS to resolve haplotype and merge
		while (frGapsBed.hasMoreLines()) {
			line = frGapsBed.readLine();
			tokens = line.split(RegExp.TAB);
			block = new PhasedBlock(tokens[PhasedBlock.CHR],
					Integer.parseInt(tokens[PhasedBlock.START]),
					Integer.parseInt(tokens[PhasedBlock.END]),
					tokens[PhasedBlock.ID] + ":" + tokens[PhasedBlock.ID + 1]);
			gapList.add(block);
		}
		return gapList;
	}
	
	private HashMap<String, ArrayList<PhasedSNP>> readAllPhasedSNPs() {
		FileReader frMergedSnp = new FileReader(mergedSnpPath);
		HashMap<String, ArrayList<PhasedSNP>> chrSnpMap = new HashMap<String, ArrayList<PhasedSNP>>();		
		ArrayList<PhasedSNP> snpList = null;
		PhasedSNP snp;
		String chr;
		String line;
		String[] tokens;
		
		while (frMergedSnp.hasMoreLines()) {
			line = frMergedSnp.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			chr = tokens[PhasedSNP.CHR];
			snp = new PhasedSNP(chr,
					Integer.parseInt(tokens[PhasedSNP.POS]),
					tokens[PhasedSNP.HAPLOTYPE_A], tokens[PhasedSNP.HAPLOTYPE_B],
					tokens[PhasedSNP.PS]);
			if (!chrSnpMap.containsKey(chr)) {
				ArrayList<PhasedSNP> newSnpList = new ArrayList<PhasedSNP>();
				chrSnpMap.put(chr, newSnpList);
			}
			snpList = chrSnpMap.get(chr);
			snpList.add(snp);
		}
		frMergedSnp.closeReader();
		return chrSnpMap;
	}
	
	private HashMap<PhasedBlock, ArrayList<PhasedSNP>> getGapToSnpMap(ArrayList<PhasedBlock> gapList, HashMap<String,ArrayList<PhasedSNP>> chrSnpMap) {
		HashMap<PhasedBlock, ArrayList<PhasedSNP>> gapToSnpMap = new HashMap<PhasedBlock, ArrayList<PhasedSNP>>();
		PhasedSNP snp;
		String chr;
		int start;
		int end;
		int pos;
		String ps1;
		String ps2;
		int numLeftSnps;	// left of gap
		int numRightSnps;	// right of gap
		ArrayList<PhasedBlock> snpGapList;
		ArrayList<PhasedSNP> snpsAroundGap;	// snp list to look for gap in gapToSnpMap
		HashMap<Integer, PhasedSNP> posSnpsAroundGaps = null;
		ArrayList<PhasedSNP> snpList = null;
		for (PhasedBlock gap : gapList) {
			chr = gap.getChr();
			snpsAroundGap = new ArrayList<PhasedSNP>();
			gapToSnpMap.put(gap, snpsAroundGap);
			if (!chrPosOfSnpListMap.containsKey(chr)) {
				posSnpsAroundGaps  = new HashMap<Integer, PhasedSNP>();
				chrPosOfSnpListMap.put(chr, posSnpsAroundGaps);
			}
			posSnpsAroundGaps = chrPosOfSnpListMap.get(chr);
			start = gap.getStart();
			end = gap.getEnd();
			ps1 = gap.getPS().split(":")[0];
			ps2 = gap.getPS().split(":")[1];
			snpList = chrSnpMap.get(chr);
			numLeftSnps = 0;
			numRightSnps = 0;
			for (int i = snpList.size() - 1; i > 0; i--) {
				snp = snpList.get(i);
				pos = snp.getPos();
				if (!ps1.equals(snp.getPS()))	continue;
				if (pos <= start && numLeftSnps < NUM_SNPS_TO_COMPARE) {
					//System.out.println("[DEBUG] :: ps:" + ps1 + " vs. " + snp.getPS() + " " + snp.getChr() + " " + pos + " " + snp.getHaplotypeA());
					posSnpsAroundGaps.put(pos, snp);
					numLeftSnps++;
					if (!phasedSnpToGapsMap.containsKey(snp)) {
						snpGapList = new ArrayList<PhasedBlock>();
						phasedSnpToGapsMap.put(snp, snpGapList);
					}
					snpGapList = phasedSnpToGapsMap.get(snp);
					snpGapList.add(gap);
					snpsAroundGap.add(snp);
					if (numLeftSnps == NUM_SNPS_TO_COMPARE)	break;
				}
			}
			for (int i = 0; i < snpList.size(); i++) {
				snp = snpList.get(i);
				pos = snp.getPos();
				if (!ps2.equals(snp.getPS()))	continue;
				if (pos >= end && numRightSnps < NUM_SNPS_TO_COMPARE) {
					//System.out.println("[DEBUG] :: ps:" + ps1 + " vs. " + snp.getPS() + " " + snp.getChr() + " " + pos + " " + snp.getHaplotypeA());
					posSnpsAroundGaps.put(pos, snp);
					numRightSnps++;
					if (!phasedSnpToGapsMap.containsKey(snpList.get(i))) {
						snpGapList = new ArrayList<PhasedBlock>();
						phasedSnpToGapsMap.put(snp, snpGapList);
					}
					snpGapList = phasedSnpToGapsMap.get(snp);
					snpGapList.add(gap);
					snpsAroundGap.add(snp);
					if (numRightSnps == NUM_SNPS_TO_COMPARE)	break;
				}
			}
			//System.out.println("[DEBUG] :: " + gap.getStart() + " - " + gap.getEnd() + " " + (gapToSnpMap.get(gap).size()) + " SNPs found around gap " + gap.getPS() + " (left: " + numLeftSnps + ", right: " + numRightSnps + ")");
		}
		return gapToSnpMap;
	}

	@Override
	public void hooker(FileReader frGapsBed, FileMaker fm) {
		String line;
		String[] tokens;
		
		// Read gaps
		System.out.println("[DEBUG] :: Reading phased block gap bed file...");
		ArrayList<PhasedBlock> gapList = readGaps(frGapsBed);
		System.out.println("Num. of phased block gaps: " + gapList.size());
		
		
		// Read mergedSnps and add to chrSnpMap
		System.out.println("[DEBUG] :: Reading phased snp file...");
		HashMap<String, ArrayList<PhasedSNP>> chrSnpMap = readAllPhasedSNPs();	// list of all snps

		// Test
		ArrayList<PhasedSNP> snpList;
		for (String chrom : chrSnpMap.keySet()) {
			snpList = chrSnpMap.get(chrom);
			System.out.println("\tNum. of snps in " + chrom + " : " + snpList.size());
		}
		
		// Collect closest SNPs to the gap that needs to be seen from sam / bam
		System.out.println("[DEBUG] :: Collecting closest " + NUM_SNPS_TO_COMPARE + " snp(s) of each gap");
		HashMap<Integer, PhasedSNP> posSnpsAroundGaps = null;
		HashMap<PhasedBlock, ArrayList<PhasedSNP>> gapToSnpMap = getGapToSnpMap(gapList, chrSnpMap);
		
		// Sort by chr, pos to speed up search time
		HashMap<String, Integer[]> chrPosArrOfSnpsAroundGapMap = new HashMap<String, Integer[]>();
		Integer[] posArr;
		System.out.println("Total num. of SNPs to look for : " + phasedSnpToGapsMap.size());
		for (String chrom : chrPosOfSnpListMap.keySet()) {
			posSnpsAroundGaps = chrPosOfSnpListMap.get(chrom);
			posArr = posSnpsAroundGaps.keySet().toArray(new Integer[0]);
			Arrays.sort(posArr);
			chrPosArrOfSnpsAroundGapMap.put(chrom, posArr);
			System.out.println("[DEBUG] :: " + chrom + " (" + posArr.length + " snps) : " + posArr[0] + "-" + posArr[posArr.length - 1]);
		}
		
		int posAligned;
		String[] seqData = new String[2];
		int matchedLen;
		ArrayList<PhasedBlock> gapsToLookUp;
		int readCount = 0;
		
		/***
		 * key:PhasedBlock GAP
		 * value: read count of HaplotypeA, B of before / after gap
		 * 	in haplotype = new Integer[4] -> [Unswitched: AA or BB, Switched: AB or BA, Ambiguous, Unknown]
		 */
		HashMap<PhasedBlock, Integer[]> gapHaplotypeMap = new HashMap<PhasedBlock, Integer[]>();
		String chr;

		if (!isBam) {
			FileReader frSam = new FileReader(inSam);
			while (frSam.hasMoreLines()) {
				line = frSam.readLine();
				if (line.startsWith("@"))	continue;
				readCount++;
				tokens = line.split(RegExp.TAB);
				if (SAMUtil.isMultiple(Integer.parseInt(tokens[Sam.FLAG])))	continue;
				chr = tokens[Sam.RNAME];
				posAligned = Integer.parseInt(tokens[Sam.POS]);
				seqData[SAMUtil.SEQ] = tokens[Sam.SEQ];
				seqData[SAMUtil.CIGAR] = tokens[Sam.CIGAR];
				matchedLen = SAMUtil.getMatchedBases(seqData[SAMUtil.CIGAR]);
				
				//System.out.println("[DEBUG] :: chrPosArrOfSnpsAroundGapMap.get(chr).length : " + chrPosArrOfSnpsAroundGapMap.get(chr).length + " chrPosOfSnpListMap.get(chr).size(): " + chrPosOfSnpListMap.get(chr).size());
				gapsToLookUp = getGapsToLookup(posAligned, posAligned + matchedLen - 1, chrPosArrOfSnpsAroundGapMap.get(chr), chrPosOfSnpListMap.get(chr), phasedSnpToGapsMap);
				
				// if the read contains phased snp positions to look up
				if (gapsToLookUp.size() > 0) {
					//System.out.println("[DEBUG] :: gapsToLookUp.size(): " + gapsToLookUp.size());
					countGapCoveringReads(gapsToLookUp, gapToSnpMap, posAligned, seqData, tokens[Sam.QNAME], gapHaplotypeMap);
				}
				
				if (readCount % 50000 == 0) {
					System.out.println("[DEBUG] :: 50000 reads processed. Current pos: " + posAligned + " " + tokens[Sam.QNAME]);
					readCount = 0;
				}
			}
			frSam.closeReader();
		} else {
			System.out.println("Processing file " + IOUtil.retrieveFileName(inSam));
			BamReader bamFr = new BamReader(inSam);
			RefInfo refInfo = bamFr.getRefInfo();
			BamRecord record;
			String readName;
			while (bamFr.hasMoreAlignmentRecord()) {
				readCount++;
				record = bamFr.getNextAlignmentRecord();
				if (SAMUtil.isMultiple(record.getFlag()))	continue;
				readName = record.getReadName();
				chr = record.getRefName(refInfo);
				posAligned = record.getPos();
				seqData[SAMUtil.SEQ] = record.getSeq();
				seqData[SAMUtil.CIGAR] = record.getCigarString();
				matchedLen = SAMUtil.getMatchedBases(seqData[SAMUtil.CIGAR]);
				
				//System.out.println("[DEBUG] :: chrPosArrOfSnpsAroundGapMap.get(chr).length : " + chrPosArrOfSnpsAroundGapMap.get(chr).length + " chrPosOfSnpListMap.get(chr).size(): " + chrPosOfSnpListMap.get(chr).size());
				gapsToLookUp = getGapsToLookup(posAligned, posAligned + matchedLen - 1, chrPosArrOfSnpsAroundGapMap.get(chr), chrPosOfSnpListMap.get(chr), phasedSnpToGapsMap);
				
				// if the read contains phased snp positions to look up
				if (gapsToLookUp.size() > 0) {
					//System.out.println("[DEBUG] :: gapsToLookUp.size(): " + gapsToLookUp.size());
					countGapCoveringReads(gapsToLookUp, gapToSnpMap, posAligned, seqData, readName, gapHaplotypeMap);
				}
				
				if (readCount % 50000 == 0) {
					System.out.println("[DEBUG] :: 50000 reads processed. Current pos: " + posAligned + " " + readName);
					readCount = 0;
				}
			}
		}
		
		// Integrate into gapCovered
		int numGapCovered = 0;
		System.out.println("[DEBUG] :: gaps with haplotype AAorBB / ABorBA / Ambiguous / Unknown info - gapHaplotypeMap.size(): " + gapHaplotypeMap.size());
		for (PhasedBlock gapCovered : gapHaplotypeMap.keySet()) {
			numGapCovered++;
			fm.writeLine(gapCovered.getChr() + "\t"
					+ gapCovered.getStart() + "\t" + gapCovered.getEnd() + "\t" 
					+ gapCovered.getPS() + "\t"
					+ gapHaplotypeMap.get(gapCovered)[AAorBB] + "\t" + gapHaplotypeMap.get(gapCovered)[ABorBA] + "\t"
					+ gapHaplotypeMap.get(gapCovered)[AMBIGUOUS] + "\t" + gapHaplotypeMap.get(gapCovered)[UNKNOWN]);
		}
		
		int numMergedBlocks = 0;
		System.out.println("Num. of Gaps Covered: " + numGapCovered  + " / " + gapList.size());
		System.out.println("\tMerged: " + numMergedBlocks);
	}
	
	private void countGapCoveringReads(ArrayList<PhasedBlock> gapsToLookUp,
			HashMap<PhasedBlock, ArrayList<PhasedSNP>> gapToSnpMap,
			int posAligned, String[] seqData, String readName,
			HashMap<PhasedBlock, Integer[]> gapHaplotypeMap) {
		/***
		 * value: leftA, leftB, leftOther, rightA, rightB, rightOther
		 */
		Integer[] haplotypeCounts = initArr(6);

		PhasedBlock gap;
		Character baseInRead;
		ArrayList<PhasedSNP> snps;
		int pos;
		
		Integer[] haplotype = new Integer[4];
		
		// for each gap
		for (int i = 0; i < gapsToLookUp.size(); i++) {
			gap = gapsToLookUp.get(i);
			snps = gapToSnpMap.get(gap);
//			System.out.println("[DEBUG] :: gap " + gap.getChr() + " " + gap.getStart() + "-" + gap.getEnd() + " " + gap.getPS()
//					+ " snp " + snps.get(0).getPos() + " .. " + snps.get(1).getPos());
			// Add counts
			for (PhasedSNP snpInGap : snps) {
				pos = snpInGap.getPos();
				baseInRead = SAMUtil.getBaseAtPos(pos, posAligned, seqData);
				if (pos <= gap.getStart()) {
					if (snpInGap.getHaplotypeA().charAt(0) == baseInRead) {
						haplotypeCounts[LEFT_A]++;
					} else if (snpInGap.getHaplotypeB().charAt(0) == baseInRead) {
						haplotypeCounts[LEFT_B]++;
					} else {
						haplotypeCounts[LEFT_OTHER]++;
					}
				}
				if (pos >= gap.getEnd()) {
					if (snpInGap.getHaplotypeA().charAt(0) == baseInRead) {
						haplotypeCounts[RIGHT_A]++;
					} else if (snpInGap.getHaplotypeB().charAt(0) == baseInRead) {
						haplotypeCounts[RIGHT_B]++;
					} else {
						haplotypeCounts[RIGHT_OTHER]++;
					}
				}
			}
			// Check
//			System.out.println("[DEBIG] :: " + gap.getChr() + " " + gap.getStart() + " " + gap.getEnd() + " " + gap.getPS()
//					+ " AA | BB: (" + haplotypeCounts[LEFT_A] + " " + haplotypeCounts[RIGHT_A] + " | " + haplotypeCounts[LEFT_B] + " " + haplotypeCounts[RIGHT_B] + ") "
//					+ tokens[Sam.QNAME]);
			
			// Define AAorBB / ABorBA / Ambiguous / Unknown
			if (hasLeftAndRightPhasedSNPs(haplotypeCounts)) {
				if (!gapHaplotypeMap.containsKey(gap)) {
					haplotype = initArr(4);
					gapHaplotypeMap.put(gap, haplotype);
				}
				haplotype = gapHaplotypeMap.get(gap);

				System.out.println("[DEBIG] :: " + gap.getChr() + " " + gap.getStart() + " " + gap.getEnd() + " " + gap.getPS()
						+ " AA | BB: (" + haplotypeCounts[LEFT_A] + " " + haplotypeCounts[RIGHT_A] + " | " + haplotypeCounts[LEFT_B] + " " + haplotypeCounts[RIGHT_B] + ") "
						+ readName);
				
				if (isAAorBB(haplotypeCounts)) {
					// AA or BB
					haplotype[AAorBB]++;
				} else if (isABorBA(haplotypeCounts)) {
					// AB or BA
					haplotype[ABorBA]++;
				} else if (isAmbiguous(haplotypeCounts)) {
					// Ambiguous
					haplotype[AMBIGUOUS]++;
				} else {
					// Unknown: Both or one side is covered with not properly genotyped bases
					haplotype[UNKNOWN]++;
				}
				gapHaplotypeMap.put(gap, haplotype);
			}
		}
	}

	private Integer[] initArr(int len) {
		Integer[] arr = new Integer[len];
		for (int i = 0; i < len; i++) {
			arr[i] = 0;
		}
		return arr;
	}

	public static boolean hasLeftAndRightPhasedSNPs(Integer[] haplotypeCounts) {
		return haplotypeCounts[LEFT_A] + haplotypeCounts[LEFT_B] > 0
				&& haplotypeCounts[RIGHT_A] + haplotypeCounts[RIGHT_B] > 0;
	}
	
	public static boolean isAAorBB(Integer[] haplotypeCounts) {
		return (haplotypeCounts[LEFT_A] > haplotypeCounts[LEFT_B] && haplotypeCounts[RIGHT_A] > haplotypeCounts[RIGHT_B])
				|| (haplotypeCounts[LEFT_A] < haplotypeCounts[LEFT_B] && haplotypeCounts[RIGHT_A] < haplotypeCounts[RIGHT_B]);
	}
	
	public static boolean isABorBA(Integer[] haplotypeCounts) {
		return (haplotypeCounts[LEFT_A] > haplotypeCounts[LEFT_B] && haplotypeCounts[RIGHT_A] < haplotypeCounts[RIGHT_B])
				|| (haplotypeCounts[LEFT_A] < haplotypeCounts[LEFT_B] && haplotypeCounts[RIGHT_A] > haplotypeCounts[RIGHT_B]);
	}
	
	public static boolean isAmbiguous(Integer[] haplotypeCounts) {
		return (haplotypeCounts[LEFT_A] == haplotypeCounts[LEFT_B] && haplotypeCounts[LEFT_A] > 0)
				|| haplotypeCounts[RIGHT_A] == haplotypeCounts[RIGHT_B] && haplotypeCounts[RIGHT_A] > 0;
	}
	
	/***
	 * 
	 * @param seqStart seq start position of the read
	 * @param seqEnd seq start + mapped bases - 1
	 * @param snpPosArr snps around gap
	 * @param posSnpsAroundGaps	pos to snps map for snps around gaps
	 * @param phasedSnpToGapMap	snp to gap ArrayList map (Some snps are markers of > 1 gaps)
	 * @return
	 */
	private ArrayList<PhasedBlock> getGapsToLookup(int seqStart, int seqEnd, Integer[] snpPosArr,
			HashMap<Integer, PhasedSNP> posSnpsAroundGaps, HashMap<PhasedSNP, ArrayList<PhasedBlock>> phasedSnpToGapMap) {
		ArrayList<PhasedBlock> gapsToLookup = new ArrayList<PhasedBlock>();
		PhasedSNP snp = null;
		ArrayList<PhasedBlock> gaps;
		int pos = 0;
		for (int i = 0; i < snpPosArr.length; i++) {
			pos = snpPosArr[i];
			if (seqEnd < pos)	break;
			if (pos < seqStart)	continue;
			if (seqStart <= pos && pos <= seqEnd) {
				snp = posSnpsAroundGaps.get(pos);
				gaps = phasedSnpToGapMap.get(snp);
				for (PhasedBlock gap : gaps) {
					if (seqStart <= gap.getStart() && gap.getEnd() <= seqEnd && !gapsToLookup.contains(gap)) {
						gapsToLookup.add(gap);
					}
				}
			}
		}
		return gapsToLookup;
	}

}
