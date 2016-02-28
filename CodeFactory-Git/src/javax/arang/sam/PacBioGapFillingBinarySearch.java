package javax.arang.sam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.algorithm.Alignment;
import javax.arang.algorithm.StringLengthComparator;
import javax.arang.bed.util.Bed;
import javax.arang.genome.util.Util;

public class PacBioGapFillingBinarySearch extends IOwrapper {
	
	private static String chr = "chr20";
	private static int gapStartPos = 0;
	private static int gapEndPos = 0;
	
	private static boolean isFileInput = false;	
	private static FileReader gapBedFr;
	private static String nType;
	private ArrayList<String> nTypes = new ArrayList<String>();
	//private ArrayList<Integer[]> start_end = new ArrayList<Integer[]>();
	private ArrayList<Integer> starts = new ArrayList<Integer>();
	private ArrayList<Integer> ends = new ArrayList<Integer>();
	private Alignment spanAlignment = new Alignment();
	private Alignment leftFlankAlignment = new Alignment();
	private Alignment rightFlankAlignment = new Alignment();
	
	private final ArrayList<Integer> emptyList = new ArrayList<Integer>();
	//private ArrayList<Integer, Alignment>

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		fm.writeLine("CHR\tGAP_START\tGAP_END\tGAP_LEN\tN_TYPE\tFILL_TYPE\tNEW_SEQ_LEN\tTOTAL_NUM_READS\tSEQ\tDEPTH");
		if (!isFileInput) {
			nTypes.add(nType);
			starts.add(gapStartPos);
			ends.add(gapEndPos);
			doAlignment(fr, fm);
		} else {
			String line = null;
			String[] tokens;
			while (gapBedFr.hasMoreLines()) {
				line = gapBedFr.readLine();
				if (line.startsWith("CHR"))	continue;
				tokens = line.split("\t");
				chr = tokens[Bed.CHROM];
				nType = tokens[Bed.NOTE + 1];
				nTypes.add(nType);
				gapStartPos = Integer.parseInt(tokens[Bed.START]);
				gapEndPos = Integer.parseInt(tokens[Bed.END]);
				starts.add(gapStartPos);
				ends.add(gapEndPos);
			}
			doAlignment(fr, fm);
		}
	}
	
	private String gapPositionOut = "";
	
	private void getGap(int gapIdx) {
		gapStartPos = starts.get(gapIdx);
		gapEndPos = ends.get(gapIdx);
		nType = nTypes.get(gapIdx);
		gapPositionOut = chr + "\t" + gapStartPos + "\t" + gapEndPos + "\t" + (gapEndPos - gapStartPos) + "\t" + nType + "\t";
		System.out.println("[DEBUG] :: GAP " + chr + ":" + gapStartPos + "-" + gapEndPos);
	}
	

	private HashMap<Integer, PriorityQueue<String>> initReads(HashMap<Integer, PriorityQueue<String>> reads, int size) {
		
		reads = new HashMap<Integer, PriorityQueue<String>>();
		for (int i = 0; i < size; i++) {
			reads.put(i, new PriorityQueue<String>(1, new StringLengthComparator()));
		}
		return reads;
		
	}
	
	public void doAlignment(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		String read;
		
		HashMap<Integer, PriorityQueue<String>> spanReads = null;
		spanReads = initReads(spanReads, starts.size());
		HashMap<Integer, PriorityQueue<String>> leftFlankReads = null;
		leftFlankReads = initReads(leftFlankReads, starts.size());
		HashMap<Integer, PriorityQueue<String>> rightFlankReads = null;
		rightFlankReads = initReads(rightFlankReads, starts.size());

		String[] seqData = null;	// = new String[] { CIGAR, SEQ, FILL_TYPE }
		
		seqData = new String[]{"", "", "open"};
		int readStart;
		int readEnd;
		int gapStartIdx;
		int gapEndIdx;
		
		//int multiple = 0;
		READ_SAM_LOOP : while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@"))	continue;
			tokens = line.split("\t");
			
			readStart = (Integer.parseInt(tokens[Sam.POS]) - Sam.getLeftSoftclippedBasesLen(tokens[Sam.CIGAR]));
			readEnd = (Integer.parseInt(tokens[Sam.POS])
					+ Integer.parseInt(tokens[Sam.TLEN])
					+ Sam.getEndSoftclip(tokens[Sam.CIGAR]));
			
			gapStartIdx = Util.getRegionEndIdxContainingPos(ends, readStart + 1);
			gapEndIdx = Util.getRegionStartIdxContainingPos(starts, readEnd - 1);
			
			if (gapStartIdx > -1 && gapEndIdx > -1 && gapStartIdx <= gapEndIdx) {
				for (int idx = gapStartIdx; idx <= gapEndIdx; idx++) {
					getGap(idx);
					seqData = new String[]{tokens[Sam.CIGAR], tokens[Sam.SEQ], "open"};
					read = SAMUtil.getRead(Integer.parseInt(tokens[Sam.POS]), seqData, Math.max(1, gapStartPos), gapEndPos + 1);
					if (read.length() == 0)	continue;
					//System.out.println("[DEBUG] :: doAlignment() " + seqData[Alignment.TYPE] + " " + tokens[Sam.POS] + " " + startPos + "-" + endPos);
					if (seqData[Alignment.TYPE].equals(Alignment.SPAN)) {
						spanReads.get(idx).add(read);
					} else if (seqData[Alignment.TYPE].equals(Alignment.LEFT_FLANK)) {
						leftFlankReads.get(idx).add(read);
					} else if (seqData[Alignment.TYPE].equals(Alignment.RIGHT_FLANK)) {
						rightFlankReads.get(idx).add(read);
					}
				}
			} else {
				continue READ_SAM_LOOP;
			}
		}
		
		System.out.println("[DEBUG] :: Gap Alignment");
		for (int i = 0; i < starts.size(); i++) {
			spanAlignment.initAlignment();
			leftFlankAlignment.initAlignment();
			rightFlankAlignment.initAlignment();
			getGap(i);
			alignment(fm, spanReads.get(i), leftFlankReads.get(i), rightFlankReads.get(i));
		}
	
	}
	
	private void alignment(FileMaker fm, PriorityQueue<String> spanReads, PriorityQueue<String> leftFlankReads, PriorityQueue<String> rightFlankReads) {
		System.out.println("[DEBUG] :: alignment() spanReads.size()=" + spanReads.size() + " leftFlankReads.size()=" + leftFlankReads.size() + " rightFlankReads.size()=" + rightFlankReads.size());
		if (spanReads.size() == 0 && leftFlankReads.size() == 0 && rightFlankReads.size() == 0) {
			// No reads overlapping the gap
			writeResult(fm, "", 0, Alignment.OPEN, emptyList);
		} else {
			if (spanReads.size() > 0) {
				alignReadQueues(fm, spanReads, spanAlignment, Alignment.SPAN);
			}
			if (spanReads.size() < 3 && leftFlankReads.size() > 0) {
				alignReadQueues(fm, leftFlankReads, leftFlankAlignment, Alignment.LEFT_FLANK);
			}
			if (spanReads.size() < 3 && rightFlankReads.size() > 0) {
				alignReadQueues(fm, rightFlankReads, rightFlankAlignment, Alignment.RIGHT_FLANK);
			}
		}
	}
	
	private PriorityQueue<String> reverseQueue(PriorityQueue<String> readQueue) {
		PriorityQueue<String> newQueue = new PriorityQueue<String>(10, new StringLengthComparator());
		int queueSize = readQueue.size();
		String sequence;
		// if isLeft, reverse the reads
		for (int i = 0; i < queueSize; i++) {
			sequence = readQueue.poll();
			newQueue.add(reverseString(sequence));
		}
		return newQueue;
	}
	
	private static String reverseString(String sequence) {
		int len = sequence.length();
		char[] reversedSequence = new char[len];
		for (int j = 0; j < sequence.length(); j++) {
			reversedSequence[j] = sequence.charAt(len - j - 1);
		}
		return String.copyValueOf(reversedSequence);
	}
	
	private void alignReadQueues(FileMaker fm, PriorityQueue<String> readQueue, Alignment alignmentObj, String type) {
		String readToAlign = "";
		System.out.println("[DEBUG] :: alignReadQueues() " + type);
		boolean isReverse = false;
		if (type.equals(Alignment.LEFT_FLANK)) {
			isReverse = true;
			readQueue = reverseQueue(readQueue);
		}
			
		if (readQueue.size() == 1) {
			if (isReverse) {
				writeResult(fm, reverseString(readQueue.poll()), 1, type, emptyList);
			} else {
				writeResult(fm, readQueue.poll(), 1, type, emptyList);
			}
		} else {
			readToAlign = readQueue.poll();
			int numReadsToAlign = Math.min(readQueue.size(), 50);
			if (numReadsToAlign < readQueue.size()) {
				System.out.println("[DEBUG] :: only the top 50 reads will be realigned for gap filling.");
			}
			String readToCompare;
			for (int i = 0; i < numReadsToAlign; i++) {
				readToCompare = readQueue.poll();
				readToAlign = alignmentObj.globalAlign(readToAlign, readToCompare);
			}
			if (isReverse) {
				writeResult(fm, reverseString(readToAlign), readQueue.size() + 1, type, alignmentObj.reverseDepthCov());
			} else {
				writeResult(fm, readToAlign, readQueue.size() + 1, type, alignmentObj.depthCov);
			}
		}
	}
	
	private void writeResult(FileMaker fm, String read, int numReads, String fillType) {
		fm.write(gapPositionOut);
		fm.write(fillType + "\t"
				+ read.length() + "\t"
				+ numReads + "\t"
				+ read + "\t");
	}
	
	private void writeResult(FileMaker fm, String read, int numReads, String fillType, ArrayList<Integer> depthCov) {
		writeResult(fm, read, numReads, fillType);
		if (depthCov.size() > 0) {
			for (int i = depthCov.size() - 1; i >= 0 ; i--) {
				fm.write((char) (depthCov.get(i) + 33));
			}
		}
		fm.writeLine();
	}


	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samPacBioGapFilling.jar <in.sam> <out.txt> <chr> <posFrom> <posTo> <nType>");
		System.out.println("\tor: java -jar samPacBioGapFilling.jar <in.sam> <out.txt> <gaps.bed>");
		System.out.println("\tRealigns the skipped reads in the bam file on the given gaps.bed, according to their positions.");
		System.out.println("\tAssumes the last base is properly aligned.");
		System.out.println("\t\t(-1) GAP (+1) will be re-aligned.");
		System.out.println("\t\t\tSo, from the output, spanned bases contain 1 flanking base of both end of the gap.");
		System.out.println("\t<in.sam>: Filtered chromosomal sam file with tools such as samtools; recommend to use primary aligned reads only.");
		System.out.println("\t<gaps.bed> should be written as 1 chr / file");
		System.out.println("\t*All the span, left-flank, right-flank will be re-aligned, if spanning reads < 3.");
		System.out.println("\t When >50 reads are flanking or spanning the gap, only the top 50 longest reads will be used.");
		System.out.println("Arang Rhie, 2015-11-06. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 6) {
			chr = args[2];
			gapStartPos = Integer.parseInt(args[3]);
			gapEndPos = Integer.parseInt(args[4]);
			nType = args[5];
			new PacBioGapFillingBinarySearch().go(args[0], args[1]);
		} else if (args.length == 3) {
			isFileInput = true;
			gapBedFr = new FileReader(args[2]);
			new PacBioGapFillingBinarySearch().go(args[0], args[1]);
		} else {
			new PacBioGapFillingBinarySearch().printHelp();
		}
	}

}
