package javax.arang.sam;

import java.util.ArrayList;
import java.util.PriorityQueue;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.algorithm.Alignment;
import javax.arang.algorithm.StringLengthComparator;
import javax.arang.bed.util.Bed;

public class PacBioGapFilling extends IOwrapper {
	
	private static String chr = "chr20";
	private static int gapStartPos = 0;
	private static int gapEndPos = 0;
	
	private static boolean isFileInput = false;
	private static FileReader gapBedFr;
	private static String nType;
	private ArrayList<String[]> chr_nType = new ArrayList<String[]>();
	private ArrayList<Integer[]> start_end = new ArrayList<Integer[]>();
	private Alignment spanAlignment = new Alignment();
	private Alignment leftFlankAlignment = new Alignment();
	private Alignment rightFlankAlignment = new Alignment();

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		fm.writeLine("CHR\tGAP_START\tGAP_END\tGAP_LEN\tN_TYPE\tFILL_TYPE\tNEW_SEQ_LEN\tTOTAL_NUM_READS\tSEQ\tDEPTH");
		if (!isFileInput) {
			String[] chr_n = new String[] {chr, nType};
			Integer[] position = new Integer[] {gapStartPos, gapEndPos};
			chr_nType.add(chr_n);
			start_end.add(position);
			doAlignment(fr, fm);
		} else {
			String line = null;
			String[] tokens;
			while (gapBedFr.hasMoreLines()) {
				line = gapBedFr.readLine();
				if (line.startsWith("CHR"))	continue;
				tokens = line.split("\t");
				chr = tokens[Bed.CHROM];
				nType = tokens[4];
				String[] chr_n = new String[] {chr, nType}; 
				chr_nType.add(chr_n);
				gapStartPos = Integer.parseInt(tokens[Bed.START]);
				gapEndPos = Integer.parseInt(tokens[Bed.END]);
				Integer[] position = new Integer[] {gapStartPos, gapEndPos};
				start_end.add(position);
			}
			doAlignment(fr, fm);
		}
	}
	
	private static final int START = 0;
	private static final int END = 1;
	private static final int CHR = 0;
	private static final int N_TYPE = 1;
	private static int gapIdx = 0;
	private String gapPositionOut = "";
	
	/***
	 * Get the next gapStartPos and gapEndPos.
	 * @param fm
	 */
	private void getNextGap(FileMaker fm) {
		chr = chr_nType.get(gapIdx)[CHR];
		gapStartPos = start_end.get(gapIdx)[START];
		gapEndPos = start_end.get(gapIdx)[END];
		nType = chr_nType.get(gapIdx)[N_TYPE];
		gapPositionOut = chr + "\t" + gapStartPos + "\t" + gapEndPos + "\t" + (gapEndPos - gapStartPos) + "\t" + nType + "\t";
		System.out.println("[DEBUG] :: GAP " + chr + ":" + gapStartPos + "-" + gapEndPos);
		gapIdx++;
	}
	
	private boolean hasNextGap() {
		return (gapIdx < chr_nType.size());
	}

	
	public void doAlignment(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		String read;
		PriorityQueue<String> spanReads = new PriorityQueue<String>(1, new StringLengthComparator());
		PriorityQueue<String> leftFlankReads = new PriorityQueue<String>(1, new StringLengthComparator());
		PriorityQueue<String> rightFlankReads = new PriorityQueue<String>(1, new StringLengthComparator());

		String[] seqData = null;	// = new String[] { CIGAR, SEQ, FILL_TYPE }
		
		if (hasNextGap()) {
			getNextGap(fm);
		}
		
		seqData = new String[]{"", "", "open"};
		//int multiple = 0;
		READ_SAM_LOOP : while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@"))	continue;
			tokens = line.split("\t");
			if (SAMUtil.isMultiple(Integer.parseInt(tokens[Sam.FLAG]))) {
				//multiple++;
				continue;
			}
			//if (!tokens[Sam.RNAME].equals(chr))	continue;

			// read is far before the gap: continue reading
			if ((Integer.parseInt(tokens[Sam.POS])
					+ Integer.parseInt(tokens[Sam.TLEN])
					+ Sam.getEndSoftclip(tokens[Sam.CIGAR])) < Math.max(1, gapStartPos)) {
//				System.out.println("[DEBUG] :: read is far before the gap: continue reading "
//					+ Integer.parseInt(tokens[Sam.POS]) + "+" + Integer.parseInt(tokens[Sam.TLEN]) + "+" +
//					+ Sam.getEndSoftclip(tokens[Sam.CIGAR])+ " < " + gapStartPos);
				
				continue;
			}
			
			// read passed the gap to the right side: check if overlaps the next gap; otherwise continue reading
			if ((Integer.parseInt(tokens[Sam.POS]) - Sam.getLeftSoftclippedBasesLen(tokens[Sam.CIGAR])) > gapEndPos + 1) {
				//System.out.println("[DEBUG] :: read passed the gap to the right side " + Integer.parseInt(tokens[Sam.POS]) + "-" + Sam.getLeftSoftclippedBasesLen(tokens[Sam.CIGAR]) + " > " + gapEndPos);
				
				// do the gap alignment here
				NEXT_GAP_LOOP : while (hasNextGap()) {
					alignment(fm, spanReads, leftFlankReads, rightFlankReads);
					getNextGap(fm);
					spanReads = new PriorityQueue<String>(10, new StringLengthComparator());
					leftFlankReads = new PriorityQueue<String>(10, new StringLengthComparator());
					rightFlankReads = new PriorityQueue<String>(10, new StringLengthComparator());
					spanAlignment.initAlignment();
					leftFlankAlignment.initAlignment();
					rightFlankAlignment.initAlignment();
					
					// read end is not reaching the next gap
					if ((Integer.parseInt(tokens[Sam.POS])
							+ Integer.parseInt(tokens[Sam.TLEN])
							+ Sam.getEndSoftclip(tokens[Sam.CIGAR])) < Math.max(1, gapStartPos)) {
						continue READ_SAM_LOOP;
					} else {
						// read is exceeding the next gap to the right side
						if ((Integer.parseInt(tokens[Sam.POS]) - Sam.getLeftSoftclippedBasesLen(tokens[Sam.CIGAR])) > gapEndPos + 1) {
							continue NEXT_GAP_LOOP;
						} else {
							break NEXT_GAP_LOOP;
						}
					}
				}
				if (!hasNextGap()) {
					break READ_SAM_LOOP;
				}
				
			}
			seqData = new String[]{tokens[Sam.CIGAR], tokens[Sam.SEQ], "open"};
			read = SAMUtil.getRead(Integer.parseInt(tokens[Sam.POS]), seqData, Math.max(1, gapStartPos), gapEndPos + 1);
			if (read.length() == 0)	continue;
			//System.out.println("[DEBUG] :: doAlignment() " + seqData[Alignment.TYPE] + " " + tokens[Sam.POS] + " " + startPos + "-" + endPos);
			if (seqData[Alignment.TYPE].equals(Alignment.SPAN)) {
				spanReads.add(read);
			} else if (seqData[Alignment.TYPE].equals(Alignment.LEFT_FLANK)) {
				leftFlankReads.add(read);
			} else if (seqData[Alignment.TYPE].equals(Alignment.RIGHT_FLANK)) {
				rightFlankReads.add(read);
			}
		}
		// For the last gap
		System.out.println("[DEBUG] :: For the last gap");
		alignment(fm, spanReads, leftFlankReads, rightFlankReads);
		
		while (hasNextGap()) {
			getNextGap(fm);
			spanAlignment.initAlignment();
			leftFlankAlignment.initAlignment();
			rightFlankAlignment.initAlignment();
			alignment(fm, spanReads, leftFlankReads, rightFlankReads);
		}
	}
	
	private void alignment(FileMaker fm, PriorityQueue<String> spanReads, PriorityQueue<String> leftFlankReads, PriorityQueue<String> rightFlankReads) {
		System.out.println("[DEBUG] :: alignment() spanReads.size()=" + spanReads.size() + " leftFlankReads.size()=" + leftFlankReads.size() + " rightFlankReads.size()=" + rightFlankReads.size());
		if (spanReads.size() == 0 && leftFlankReads.size() == 0 && rightFlankReads.size() == 0) {
			// No reads overlapping the gap
			writeResult(fm, "", 0, Alignment.OPEN);
			fm.writeLine();
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
				writeResult(fm, reverseString(readQueue.poll()), 1, type);
			} else {
				writeResult(fm, readQueue.poll(), 1, type);
			}
		} else {
			readToAlign = readQueue.poll();
			for (String readToCompare : readQueue) {
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
		for (int i = depthCov.size() - 1; i >= 0 ; i--) {
			fm.write((char) (depthCov.get(i) + 33));
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
		System.out.println("\t<gaps.bed> should be written per chr.");
		System.out.println("\t*All the span, left-flank, right-flank will be re-aligned, if spanning reads < 3.");
		System.out.println("Arang Rhie, 2015-11-02. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 6) {
			chr = args[2];
			gapStartPos = Integer.parseInt(args[3]);
			gapEndPos = Integer.parseInt(args[4]);
			nType = args[5];
			new PacBioGapFilling().go(args[0], args[1]);
		} else if (args.length == 3) {
			isFileInput = true;
			gapBedFr = new FileReader(args[2]);
			new PacBioGapFilling().go(args[0], args[1]);
//		} else if (args.length == 4) {
//			isFileInput = true;
//			gapBedFr = new FileReader(args[2]);
//			callSpanningGapOnly = Boolean.parseBoolean(args[3]);
//			new PacBioGapFilling().go(args[0], args[1]);
		} else {
			new PacBioGapFilling().printHelp();
		}
	}

}
