/**
 * 
 */
package javax.arang.bam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Vector;

import javax.arang.IO.bam.BamBaiIFileOwrapper;
import javax.arang.IO.bambasic.BamReader;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.bam.util.Bai;
import javax.arang.bam.util.BamRecord;
import javax.arang.bam.util.Bin;
import javax.arang.bam.util.Cigar;
import javax.arang.bam.util.RefInfo;
import javax.arang.bed.util.Bed;

/**
 * @author Arang Rhie
 *
 */
public class BaseDepth extends BamBaiIFileOwrapper {

	String chr;
	PriorityQueue<Integer> posQ = new PriorityQueue<Integer>();
	// pos	All (A C G T D)
	HashMap<Integer, Integer[]> depthMap = new HashMap<Integer, Integer[]>();
		
	
	/* (non-Javadoc)
	 * @see javax.arang.IO.BamBaiFileIBinaryOwrapper#hooker(javax.arang.IO.BamReader,
	 *  javax.arang.bam.Bai,
	 *  javax.arang.IO.FileReader,
	 *  javax.arang.IO.BinaryFileMaker)
	 */
	@Override
	public void hooker(BamReader bamFr, Bai bai, FileReader bedFr, FileMaker fm) {
		Bed bed = new Bed(bedFr);
		RefInfo refInfo = bamFr.getRefInfo();
		String chr;
		fm.writeLine("#Chromosome\tPosition\tA\tC\tG\tT\tD");
		ArrayList<String> chrList = bed.getChrStringList();
		for (int chrIdx = 0; chrIdx < chrList.size(); chrIdx++) {
			chr = chrList.get(chrIdx);
			int refId = refInfo.getRefID(chr);
			this.chr = chr;
			if (!prevChr.equals("")) {
				writeBaseCoverage(fm, -1);
			}
			prevChr = chr;
			for (int i = 0; i < bed.getNumRegions(chr); i++) {
				long start = bed.getStartFromIdx(chr, i);
				long end = bed.getEndFromIdx(chr, i);
				Vector<Long[]> chunks = bai.getChunks(refId, start, end);
				if (chunks == null) {
					System.out.println("Bam file does not contain " + chr + " " + start + " - " + end);
					continue;
				}
				for (Long[] chunk : chunks) {
					bamFr.goTo(chunk[Bin.C_OFFSET_BEGIN], chunk[Bin.U_OFFSET_BEGIN]);
					READ_RECORD_LOOP : while (bamFr.hasMoreAlignmentRecord()) {
						BamRecord record = bamFr.getNextAlignmentRecord();
						if (record.getPos() > end)	break READ_RECORD_LOOP;
						//System.out.println(chr + " " + record.getPos());
//						if (SAMUtil.isDuplicate(record.getFlag()))	continue;
//						if (SAMUtil.isSecondaryAlignment(record.getFlag()))	continue;
//						if (SAMUtil.isUnderQual(record.getFlag()))	continue;
//						if (SAMUtil.isUnmapped(record.getFlag()))	continue;
						if (record.getPos() + record.getSeqLength() > start) {
							getBaseCoverage(record, fm);
						}
					}
				}
			}
		}
		writeBaseCoverage(fm, -1);
	}


	/* (non-Javadoc)
	 * @see javax.arang.IO.BamBaiFileIBinaryOwrapper#hooker(javax.arang.IO.BamReader,
	 *  javax.arang.bam.Bai,
	 *   javax.arang.IO.BinaryFileMaker)
	 */
	@Override
	public void hooker(BamReader bamFr, Bai bai, FileMaker fm) {
		RefInfo refInfo = bamFr.getRefInfo();
		BamRecord record = null;
		fm.writeLine("#Chromosome\tPos\tA\tC\tG\tT\tD");
		while (bamFr.hasMoreAlignmentRecord()) {
			record = bamFr.getNextAlignmentRecord();
//			System.out.println(refInfo.getRefName(record.getRefID()) + " " + record.getPos()
//					+ "\t" + record.getSeq());
			//System.out.println("\t\t" + record.getQual());
			this.chr = refInfo.getRefName(record.getRefID());
			if (!prevChr.equals(chr)) {
				System.out.println(".. Write " + chr);
				writeBaseCoverage(fm, -1);
				prevChr = chr;
			}
			if (prevChr.equals("")) {
				prevChr = chr;
			}
			if (chr.equals("*"))	break;
//			if (SAMUtil.isDuplicate(record.getFlag()))	continue;
//			if (SAMUtil.isSecondaryAlignment(record.getFlag()))	continue;
//			if (SAMUtil.isUnderQual(record.getFlag()))	continue;
//			if (SAMUtil.isUnmapped(record.getFlag()))	continue;
			//System.out.println("[DEBUG] :: " + record.getReadName());
			getBaseCoverage(record, fm);
		}
		writeBaseCoverage(fm, -1);
	}
	
	/***
	 * Write out depthQ from prevPos to pos-1. Empty the depthQ till pos-1.
	 * @param fm	BinaryFileMaker to write out coverage.
	 * @param pos	Position where the depth will be taken from next alignment.
	 * If pos is -1, all the depthQ will be written and cleared.
	 */
	private void writeBaseCoverage(FileMaker fm, int pos) {
		// Write all the depthQ and clear it.
		if (pos == -1) {
			while(!posQ.isEmpty()) {
				//System.out.print(prevChr + "\t" + position);
				int position = posQ.remove();
				fm.write(prevChr + "\t" + position);
				Integer[] depths = depthMap.get(position);
				for (int i = 0; i < depths.length; i++) {
					//System.out.print("\t" + depths[i]);
					fm.write("\t" + depths[i]);
				}
				fm.writeLine();
			}
			depthMap.clear();
		} else {
			// Write depths until pos-1.
			if (posQ.peek() == null)	return;
			int position = posQ.peek();
			Integer[] depths = depthMap.get(position);
			while (position < pos) {
				//System.out.print(prevChr + "\t" + position);
				fm.write(prevChr + "\t" + position);
				for (int i = 0; i < depths.length; i++) {
					//System.out.print("\t" + depths[i]);
					fm.write("\t" + depths[i]);
				}
				fm.writeLine();
				//System.out.println();
				posQ.remove();
				depthMap.remove(position);
				if (posQ.peek() == null)	return;
				position = posQ.peek();
				depths = depthMap.get(position);
			}
		}
	}

	String prevChr = "";
	int prevPos = -1;
	public void getBaseCoverage(BamRecord record, FileMaker fm) {
		int pos = record.getPos();
		
		// write new depth when pos from record exceeds previous base.
		if (pos != prevPos) {
			//System.out.println(prevPos + " -> " + pos);
			writeBaseCoverage(fm, pos);
			// init vars
			prevPos = pos;
		}
		
		// add to depthQ
		ArrayList<Integer[]> cigarArr = record.getCigar();
		int seqIdx = 0;
		int currPos = pos + seqIdx;
		for (Integer[] cigar : cigarArr) {
			//System.out.println("cigar: " + cigar[Cigar.COUNT] + " " + cigar[Cigar.OP]);
			// S: skip COUNT bases
			if (cigar[Cigar.OP] == Cigar.OP_S || cigar[Cigar.OP] == Cigar.OP_I) {
				seqIdx += cigar[Cigar.COUNT];
			}
			// D: add 'D'
			else if (cigar[Cigar.OP] == Cigar.OP_D) {
				for (int i = 0; i < cigar[Cigar.COUNT]; i++) {
					// add D
					if (depthMap.containsKey(currPos)) {
						if (hasNoQual) {
						depthMap.put(currPos, 
								addDepth(depthMap.get(currPos), currPos, 'D'));
						} else {
							depthMap.put(currPos,
								addDepth(depthMap.get(currPos), currPos,
										'D',
										(char) (record.getQual().charAt(seqIdx) + record.getQual().charAt(seqIdx) / 2)));
						}
					} else {
						posQ.add(currPos);
						Integer[] newDepth = new Integer[5];
						for (int j = 0; j < newDepth.length; j++) {
							newDepth[j] = 0;
						}
						if (hasNoQual) {
						depthMap.put(currPos, 
								addDepth(newDepth, currPos, 'D'));
						} else {
							depthMap.put(currPos,
								addDepth(newDepth, currPos,
										'D',
										(char) (record.getQual().charAt(seqIdx) + record.getQual().charAt(seqIdx) / 2)));
						}
					}
					currPos++;
				}
			}
			// M, = or X: add COUNT bases
			else if (cigar[Cigar.OP] == Cigar.OP_M || cigar[Cigar.OP] == Cigar.OP_EQ || cigar[Cigar.OP] == Cigar.OP_X) {
				for (int i = 0; i < cigar[Cigar.COUNT]; i++) {
					if (depthMap.containsKey(currPos)) {
						
						if (hasNoQual) {
							depthMap.put(currPos, 
									addDepth(depthMap.get(currPos), currPos,
											record.getSeq().charAt(seqIdx)));
						} else {
							depthMap.put(currPos, 
									addDepth(depthMap.get(currPos), currPos,
											record.getSeq().charAt(seqIdx),
											record.getQual().charAt(seqIdx)));
						}
					} else {
						posQ.add(currPos);
						Integer[] newDepth = new Integer[5];
						for (int j = 0; j < newDepth.length; j++) {
							newDepth[j] = 0;
						}
						if (hasNoQual) {
							depthMap.put(currPos, 
									addDepth(newDepth, currPos,
										record.getSeq().charAt(seqIdx)));
							
						} else {
							depthMap.put(currPos, 
									addDepth(newDepth, currPos,
											record.getSeq().charAt(seqIdx),
											record.getQual().charAt(seqIdx)));
						}
					}
					seqIdx++;
					currPos++;
				}
			}
			// N: jump the currPos.
			else if (cigar[Cigar.OP] == Cigar.OP_N) {
				currPos += cigar[Cigar.COUNT];
			}
			// H: do nothing. seq is already clipped.
		}
	}
	
	private final short OFFSET_A = 0;
	private final short OFFSET_C = 1;
	private final short OFFSET_G = 2;
	private final short OFFSET_T = 3;
	private final short OFFSET_D = 4;
	
	private Integer[] addDepth(Integer[] depths, int pos, char base, char qual) {
		qual -= 33;
		short offset = OFFSET_A;
		switch(base) {
		case 'A': {
			offset = OFFSET_A;
			break;
		}
		case 'C': {
			offset = OFFSET_C;
			break;
		}
		case 'G': {
			offset = OFFSET_G;
			break;
		}
		case 'T': {
			offset = OFFSET_T;
			break;
		}
		case 'D': {
			offset = OFFSET_D;
			break;
		}
		}
		if (qual > qualFilter) {
			depths[offset]++;
		}
		return depths;
	}
	
	private Integer[] addDepth(Integer[] depths, int pos, char base) {

		short offset = OFFSET_A;
		switch(base) {
		case 'A': {
			offset = OFFSET_A;
			break;
		}
		case 'C': {
			offset = OFFSET_C;
			break;
		}
		case 'G': {
			offset = OFFSET_G;
			break;
		}
		case 'T': {
			offset = OFFSET_T;
			break;
		}
		case 'D': {
			offset = OFFSET_D;
			break;
		}
		}
		depths[offset]++;
		return depths;
	}
	
	/* (non-Javadoc)
	 * @see javax.arang.IO.BamBaiFileIBinaryOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bamBaseDepth.jar <in.bam> <out.base> [-qual Q-filter]");
		System.out.println("\t<out.base>: chr\tpos(int)\tDepth of > Q-filter (A C G T D) (int)\n" +
				"\t\tQ is phred-scaled base quality (assumes bam quality string is Phred+33).");
		System.out.println("\t[-qual Q-filter]: [DEFAULT=0 (no filter applied)] Phred scaled quality score. bamBaseDepth will report only the depth over Q-filter.");
		System.out.println("\t\t*Set to -1 if qual field does not exist. (Ex. PacBio Subreads)");
		//System.out.println("\t[-bed region.bed]: [DEFAULT=ALL] target to make <output>.");
		//TODO: Later, implement -t option for multi-threading
//		System.out.println("\t[-t numThreads]: Number of multi-threads to use. Works only when -bed is specified.");
//		System.out.println("\t\tif numThreads > 1, temporary output files will be written and merged after the process is all over.");		
		System.out.println("\t*Flag Filter: Duplicate, SecondaryAlignment, UnderQual, Unmapped.");
		System.out.println("\t*Run baseCoverage.jar after this process is finished. <= Not any more after 2015-04-02.");
		System.out.println("Arang Rhie, 2015-06-07. arrhie@gmail.com");
	}
	
	private static int qualFilter = 0;
//	private static int numThreads = 0;
	
	static boolean hasNoQual = false;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new BaseDepth().go(args[0], args[1]);
		} else if (args.length > 3) {
			boolean hasBed = false;
			String targetBed = "";
			for (int i = 2; i < args.length; i+=2) {
				if (args[i].startsWith("-q")) {
					qualFilter = Integer.parseInt(args[i+1]);
					if (qualFilter == -1) {
						hasNoQual = true;
						System.out.println("Assumes no Quality Filter to be expected.");
					} else {
						System.out.println("Q-filter: " + qualFilter + " (" + (qualFilter + 33) +"=" + (char) (qualFilter + 33) + ") in ASCII code");
					}
				}
				else if (args[i].startsWith("-b")) {
					hasBed = true;
					targetBed = args[i+1];
					System.out.println("Region Bed: " + targetBed);
				}
				else {
					System.out.println("Unrecognized arg " + args[i]);
					new BaseDepth().printHelp();
					System.exit(0);
				}
			}
			if (hasBed) {
				new BaseDepth().go(args[0], targetBed, args[1]);
			} else {
				new BaseDepth().go(args[0], args[1]);
			}
		} else {
			new BaseDepth().printHelp();
		}
	}

}
