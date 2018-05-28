package javax.arang.sam;

import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.arang.algorithm.Alignment;

public class SAMUtil {
	
	private static final int MASK_ALIGNED = 1;
	private static final int MASK_CONCORDANT = 2;							// 0x2
	private static final int MASK_UNMAPPED = (int) Math.pow(2, 2);
	private static final int MASK_NEXT_UNMAPPED = (int) Math.pow(2, 3);
	private static final int MASK_REVERSE_COMPLEMENTED = (int) Math.pow(2, 4);
	private static final int MASK_NEXT_REVERSE_COMPLEMENTED = (int) Math.pow(2, 5);
	private static final int MASK_PAIR1 = (int) Math.pow(2, 6);
	private static final int MASK_PAIR2 = (int) Math.pow(2, 7);
	private static final int MASK_SECONDARY = (int) Math.pow(2, 8);
	private static final int MASK_QUAL_CONTROL = (int) Math.pow(2, 9);
	private static final int MASK_DUP = (int) Math.pow(2, 10);
	private static final int MASK_SUPPLEMENTARY = (int) Math.pow(2, 11);
	
	public static int setReverseComplemented(int flag) {
		return (flag | MASK_REVERSE_COMPLEMENTED);
	}
	
	public static int setFirstSegmentInTemplate(int flag) { 
		return (flag | MASK_PAIR1);
	}

	/***
	 * Template having multiple segments in sequencing
	 * Bit: 0x1 = 1 = 1
	 * @param flag
	 * @return in binary, 0x1 == 1?
	 */
	public static boolean isAligned(int flag) {
		return (flag & MASK_ALIGNED) == MASK_ALIGNED;
	}
	
	/***
	 * Each fragment properly aligned according to the aligner
	 * Bit: 0x2 = 10 = 2
	 * @param flag
	 * @return in binary, 0x2 == 1?
	 */
	public static boolean isBothAligned(int flag) {
		return (flag & MASK_CONCORDANT) == MASK_CONCORDANT;
	}
	
	/***
	 * Segment Unmapped
	 * Bit: 0x4 = 100 = 8
	 * @param flag
	 * @return in binary, 0x4 == 1?
	 */
	public static boolean isUnmapped(int flag) {
		return (flag & MASK_UNMAPPED) == MASK_UNMAPPED;
	}
	
	/***
	 * Next segment in the alignment unmapped
	 * Bit: 0x8 = 1000 = 16
	 * @param flag
	 * @return in binary, 0x8 == 1?
	 */
	public static boolean isNextUnmapped(int flag) {
		return (flag & MASK_NEXT_UNMAPPED) == MASK_NEXT_UNMAPPED;
	}
	
	/***
	 * SEQ being reverse complemented
	 * Bit: 0x10 = 1 0000 = 32
	 * @param flag
	 * @return in binary, 0x10 == 1?
	 */
	public static boolean isReverseComplemented(int flag) {
		return (flag & MASK_REVERSE_COMPLEMENTED) == MASK_REVERSE_COMPLEMENTED;
	}	

	/***
	 * SEQ of the next segment in the template being reversed
	 * Bit: 0x20 = 10 0000 = 64
	 * @param flag
	 * @return in binary, 0x20 == 1?
	 */
	public static boolean isNextReverseComplemented(int flag) {
		return (flag & MASK_NEXT_REVERSE_COMPLEMENTED) == MASK_NEXT_REVERSE_COMPLEMENTED;
	}
	
	
	/***
	 * Is 1st fragment in the template
	 * Bit: 0x40 = 100 0000 = 64
	 * @param flag
	 * @return in binary, 0x40 == 1 ?
	 */
	public static boolean isPair1(int flag){
		return (flag & MASK_PAIR1) == MASK_PAIR1;
	}
	
	/***
	 * Is 2nd fragment in the template
	 * Bit: 0x80 = 1000 0000 = 128
	 * @param flag
	 * @return in binary, 0x80 == 1 ?
	 */
	public static boolean isPair2(int flag){
		return (flag & MASK_PAIR2) == MASK_PAIR2;
	}
	
	/***
	 * Is secondary alignment (multiple alignment)
	 * Bit: 0x100 = 1 0000 0000 = 256
	 * @param flag
	 * @return in binary, 0x100 == 1 ?
	 */
	public static boolean isSecondaryAlignment(int flag) {
		return (flag & MASK_SECONDARY) == MASK_SECONDARY;
	}
	
	/***
	 * Is not passing quality control
	 * Bit: 0x200 = 10 0000 0000 = 512
	 * @param flag
	 * @return in binary, 0x200 == 1 ?
	 */
	public static boolean isUnderQual(int flag) {
		return (flag & MASK_QUAL_CONTROL) == MASK_QUAL_CONTROL;
	}
	
	/***
	 * PCR or optical duplicate
	 * Bit: 0x400 = 100 0000 0000 = 1024
	 * @param flag
	 * @return in binary, 0x400 == 1 ?
	 */
	public static boolean isDuplicate(int flag) {
		return (flag & MASK_DUP) == MASK_DUP;
	}
	
	/***
	 * Supplementary alignment, corresponding alignment line is part of a chimeric alignment
	 * Bit: 0x800 = 1000 0000 0000 = 1024
	 * @param flag
	 * @return in binary, 0x800 == 1 ?
	 */
	public static boolean isSupplementary(int flag) {
		return (flag & MASK_SUPPLEMENTARY) == MASK_SUPPLEMENTARY;
	}
	
	public static boolean isUnpaired(int flag) {
		return (flag & MASK_UNMAPPED) == MASK_UNMAPPED || (flag & MASK_NEXT_UNMAPPED) == MASK_NEXT_UNMAPPED;
	}
	
	public static boolean isConcordant(int flag) {
		return isPrimaryAlignment(flag)
				&& isBothAligned(flag)
				&& !isSupplementary(flag);
	}
	
	public static boolean isPrimaryAlignment(int flag) {
		return !isUnmapped(flag) && !isSecondaryAlignment(flag);
	}
	
	public static boolean isUnique(int flag) {
		return !isSecondaryAlignment(flag);
	}
	
	public static boolean isMultiple(int flag) {
		return isSecondaryAlignment(flag);
	}
	
	public static boolean isOnePairUniqueOnePairUnmapped(int flag) {
		return !isSecondaryAlignment(flag)
				&& !isSupplementary(flag)
				&& isNextUnmapped(flag);
	}
	
	public static int getMismatchScore(String cigar, String mdTag) {
		int mismatch = Sam.getSoftclippedBasesLen(cigar);
		
//		if (cigar.lastIndexOf("S") == cigar.length() - 1) {
//			String skipped = cigar.substring(cigar.lastIndexOf("M") + 1, cigar.lastIndexOf("S"));
//			mismatch += Integer.parseInt(skipped);
//		} else if (cigar.indexOf("S") > 0) {
//			String skipped = cigar.substring(0, cigar.indexOf("S"));
//			mismatch += Integer.parseInt(skipped);
//		}
		
		// has insertion or deletion
		if (cigar.contains("I") || cigar.contains("D")) mismatch += 3;
		
		// mismatches
		boolean isDel = false;
		for (Character c : mdTag.toCharArray()) {
			if (Pattern.matches("A-Z", Character.toString(c))) {
				if (!isDel) mismatch++;
			} else if (c.equals('^')) {
				isDel = true;
			} else if (Pattern.matches("1-9", Character.toString(c))) {
				isDel = false;
			}
		}
		
		return mismatch;
	}
	
	public static final int CIGAR = 0;
	public static final int SEQ = 1;
	public static final int TYPE = 2;
	
	/***
	 * Retrieve the read out of the Sam.SEQ, from the given position.
	 * @param pos	The 1st 'real', not relevant, position in the reference sequence
	 * @param cigar The cigar string from bam/sam
	 * @param seqData	The Sam.seq
	 * @param startPos	The 'real' starting position to retrieve
	 * @param endPos	The 'real' end position to retrieve
	 * @return	sub-sequences from the 'real' startPos to endPos
	 */
	public static String getRead(int pos, String[] seqData, int startPos, int endPos) {
		int seqStartPos = 0;
		int seqEndPos = seqData[SEQ].length() - 1;
		ArrayList<int[]> cigArr = Sam.getAllPosition(pos, seqData[CIGAR]);
		boolean isLeftFlanked = false;
		boolean isRightFlanked = false;
		for (int[] posArr : cigArr) {
//			System.out.println("[DEBUG] :: "
//					+ posArr[Sam.REF_START_POS] + "," + posArr[Sam.REF_END_POS] + " | "
//					+ posArr[Sam.ALGN_RANGE_START] + "," + posArr[Sam.ALGN_RANGE_END] + " | " + posArr[Sam.CIGAR_TYPE]);
			if (posArr[Sam.CIGAR_POS_REF_START] <= startPos && startPos <= posArr[Sam.CIGAR_POS_REF_END]) {
				isLeftFlanked = true;
				if (posArr[Sam.CIGAR_POS_TYPE] == Sam.D) {
					seqStartPos = posArr[Sam.CIGAR_POS_ALGN_RANGE_START];
				} else {
					seqStartPos = posArr[Sam.CIGAR_POS_ALGN_RANGE_START] + (startPos - posArr[Sam.CIGAR_POS_REF_START]);
				}
				seqEndPos = seqStartPos;
				//System.out.println(seqStartPos + " " + seqEndPos);
			}
			if (posArr[Sam.CIGAR_POS_REF_START] <= endPos && endPos <= posArr[Sam.CIGAR_POS_REF_END]) {
				isRightFlanked = true;
				if (posArr[Sam.CIGAR_POS_TYPE] == Sam.D) {
					seqEndPos = posArr[Sam.CIGAR_POS_ALGN_RANGE_END];	// ALGN_RANGE_END is the last matched base's align-range-end
				} else {
					seqEndPos = posArr[Sam.CIGAR_POS_ALGN_RANGE_END] - ((posArr[Sam.CIGAR_POS_REF_END] - endPos));
				}
				//System.out.println(seqStartPos + " " + seqEndPos);
			}
			if (posArr[Sam.CIGAR_POS_TYPE] != Sam.D && endPos > posArr[Sam.CIGAR_POS_REF_END]) {
				seqEndPos = posArr[Sam.CIGAR_POS_ALGN_RANGE_END];
			}
		}
		boolean isSpanning = isLeftFlanked && isRightFlanked && true;
		if (isSpanning) {
			seqData[TYPE] = Alignment.SPAN;
		} else if (isLeftFlanked) {
			seqData[TYPE] = Alignment.LEFT_FLANK;
		} else if (isRightFlanked) {
			seqData[TYPE] = Alignment.RIGHT_FLANK;
		} else {
			return "";
		}
		seqData[SEQ] = seqData[SEQ].substring(seqStartPos, seqEndPos + 1);
		System.out.println("[DEBUG] :: getRead() " + seqData[TYPE] + " (seqStartPos, seqEndPos) : ( "
		+ seqStartPos + ", " + (seqEndPos) + " ) (len: " + seqData[SEQ].length() + ")"
				+ " pos: " + pos + " | " + startPos + "-" + endPos + " (" + (endPos - startPos + 1) + " bp)");
		return seqData[SEQ];
	}

	/***
	 * Retrieve the read out of the Sam.SEQ, from the given position.
	 * @param pos	The 1st 'real', not relevant, position in the reference sequence
	 * @param cigar The cigar string from bam/sam
	 * @param seqData	The Sam.seq
	 * @param startPos	The 'real' starting position to retrieve
	 * @param endPos	The 'real' end position to retrieve
	 * @return	sub-sequences from the 'real' startPos to endPos
	 */
	public static String getReadInMapped(int pos, String[] seqData, int startPos, int endPos) {
		int seqStartPos = 0;
		int seqEndPos = seqData[SEQ].length() - 1;
		ArrayList<int[]> cigArr = Sam.getAllPosition(pos, seqData[CIGAR]);
		boolean isLeftFlanked = false;
		boolean isRightFlanked = false;
		for (int[] posArr : cigArr) {
			if (posArr[Sam.CIGAR_POS_TYPE] != Sam.M) {
				continue;
			}
//			System.out.println("[DEBUG] :: "
//					+ posArr[Sam.REF_START_POS] + "," + posArr[Sam.REF_END_POS] + " | "
//					+ posArr[Sam.ALGN_RANGE_START] + "," + posArr[Sam.ALGN_RANGE_END] + " | " + posArr[Sam.CIGAR_TYPE]);
			if (posArr[Sam.CIGAR_POS_REF_START] <= startPos && startPos <= posArr[Sam.CIGAR_POS_REF_END]) {
				isLeftFlanked = true;
				if (posArr[Sam.CIGAR_POS_TYPE] == Sam.D) {
					seqStartPos = posArr[Sam.CIGAR_POS_ALGN_RANGE_START];
				} else {
					seqStartPos = posArr[Sam.CIGAR_POS_ALGN_RANGE_START] + (startPos - posArr[Sam.CIGAR_POS_REF_START]);
				}
				seqEndPos = seqStartPos;
				//System.out.println(seqStartPos + " " + seqEndPos);
			}
			if (posArr[Sam.CIGAR_POS_REF_START] <= endPos && endPos <= posArr[Sam.CIGAR_POS_REF_END]) {
				isRightFlanked = true;
				if (posArr[Sam.CIGAR_POS_TYPE] == Sam.D) {
					seqEndPos = posArr[Sam.CIGAR_POS_ALGN_RANGE_END];	// ALGN_RANGE_END is the last matched base's align-range-end
				} else {
					seqEndPos = posArr[Sam.CIGAR_POS_ALGN_RANGE_END] - ((posArr[Sam.CIGAR_POS_REF_END] - endPos));
				}
				//System.out.println(seqStartPos + " " + seqEndPos);
			}
			if (posArr[Sam.CIGAR_POS_TYPE] != Sam.D && endPos > posArr[Sam.CIGAR_POS_REF_END]) {
				seqEndPos = posArr[Sam.CIGAR_POS_ALGN_RANGE_END];
			}
		}
		boolean isSpanning = isLeftFlanked && isRightFlanked && true;
		if (isSpanning) {
			seqData[TYPE] = Alignment.SPAN;
			seqData[SEQ] = seqData[SEQ].substring(seqStartPos, seqEndPos + 1);
			System.out.println("[DEBUG] :: getRead() " + seqData[TYPE] + " (seqStartPos, seqEndPos) : ( "
					+ seqStartPos + ", " + (seqEndPos) + " ) (len: " + seqData[SEQ].length() + ")"
					+ " pos: " + pos + " | " + startPos + "-" + endPos + " (" + (endPos - startPos + 1) + " bp)");
		} else if (isLeftFlanked) {
			seqData[TYPE] = Alignment.LEFT_FLANK;
		} else if (isRightFlanked) {
			seqData[TYPE] = Alignment.RIGHT_FLANK;
		} else {
			return "";
		}
		return seqData[SEQ];
	}
	/***
	 * Retrieve the base at pos from an alignment record.
	 * Assumes record is in desired chromosome.
	 * @param pos	Reference-based position to retrieve. 1-base
	 * @param posAligned	Pos of sam record
	 * @param seqData String[CIGAR, SEQ]
	 * @return the base retrieved from record
	 */
	public static Character getBaseAtPos(double pos, double posAligned, String[] seqData) {
		ArrayList<int[]> cigArr = Sam.getAllPosition((int)posAligned, seqData[CIGAR]);
		double seqPos = -1;
		for (int[] posArr : cigArr) {
			//System.out.println("[DEBUG] :: " + posArr[Sam.REF_START_POS] + "," + posArr[Sam.REF_END_POS]);
			if (posArr[Sam.CIGAR_POS_TYPE] == Sam.M && posArr[Sam.CIGAR_POS_REF_START] <= pos && pos <= posArr[Sam.CIGAR_POS_REF_END]) {
				seqPos = posArr[Sam.CIGAR_POS_ALGN_RANGE_START] + (pos - posArr[Sam.CIGAR_POS_REF_START]);
				return seqData[SEQ].charAt((int)seqPos);
			} else if (posArr[Sam.CIGAR_POS_TYPE] == Sam.D && posArr[Sam.CIGAR_POS_REF_START] <= pos && pos <= posArr[Sam.CIGAR_POS_REF_END]) {
				return 'D';
			}
		}
		return 'N';
	}
	
	private static String readId = "";
	private static String mockRead;
	/***
	 * For computational speed-up, keep the last cigArr on heap
	 * @param readid
	 * @param pos	position to look up
	 * @param posAligned	position the read is aligned in [POS] field
	 * @param seqData
	 * @return
	 */
	public static Character getBaseAtPos(String readid, int pos, int posAligned, String[] seqData) {
		if (!readId.equals(readid)) {
			mockRead = Sam.makeMockSequence(seqData[SEQ], seqData[CIGAR]);
			readId = readid;
		}
		return mockRead.charAt(pos - posAligned);
	}
	
	/***
	 * M only
	 * @param cigar
	 * @return bases mappeds
	 */
	public static int getMappedBases(String cigar) {
		ArrayList<String[]> cigarArr = Sam.parseArr(cigar);
		int matchs = 0;
		
		for (int i = 0; i < cigarArr.size(); i++) { 
			if (cigarArr.get(i)[Sam.OP] != null) {
				if (cigarArr.get(i)[Sam.OP].equals("M")) {
					matchs += Integer.parseInt(cigarArr.get(i)[Sam.COUNT]);
				}
			}
		}
		
		return matchs;
	}
	
	/***
	 * M + D + N
	 * @param cigar
	 * @return bases matched + deleted
	 */
	public static int getMatchedBases(String cigar) {
		ArrayList<String[]> cigarArr = Sam.parseArr(cigar);
		int matchs = 0;
		
		for (int i = 0; i < cigarArr.size(); i++) { 
			if (cigarArr.get(i)[Sam.OP] != null) {
				if (cigarArr.get(i)[Sam.OP].equals("M") || cigarArr.get(i)[Sam.OP].equals("D") || cigarArr.get(i)[Sam.OP].equals("N")) {
					matchs += Integer.parseInt(cigarArr.get(i)[Sam.COUNT]);
				}
			}
		}
		
		return matchs;
	}
	
	/**
	 * Get the M, D, I base len from a read, in specified region <from> to <to>
	 * @param pos
	 * @param cigar
	 * @param from
	 * @param to
	 * @return int[M,D,I]
	 */
	public static int[] getMDI(int pos, String cigar, int from, int to) {
		int[] mdi = new int[3];
		final short M = 0;
		final short D = 1;
		final short I = 2;
		mdi[M] = 0;
		mdi[D] = 0;
		mdi[I] = 0;

		ArrayList<int[]> posArrList = Sam.getAllPosition(pos, cigar);
		int[] posArr;
		for (int i = 0; i < posArrList.size(); i++) {
			posArr = posArrList.get(i);
			if (posArr[Sam.CIGAR_POS_TYPE] == Sam.S)	continue;
			if (posArr[Sam.CIGAR_POS_REF_END] < from) {
				continue;
			}
			if (to < posArr[Sam.CIGAR_POS_REF_START]) {
				break;
			}
			//System.err.println("[DEBUG] :: " + Sam.getCigarType(posArr[Sam.CIGAR_POS_TYPE]) + "\t" + from + "-" + to + "\t" + posArr[Sam.CIGAR_POS_REF_START] + "-" + posArr[Sam.CIGAR_POS_REF_END]);
			if (posArr[Sam.CIGAR_POS_TYPE] == Sam.M) {
				if (posArr[Sam.CIGAR_POS_REF_START] <= from  && from <= posArr[Sam.CIGAR_POS_REF_END]) {
					if (to <= posArr[Sam.CIGAR_POS_REF_END]) {
						mdi[M] += to - from + 1;
					} else {
						mdi[M] += posArr[Sam.CIGAR_POS_REF_END] - from + 1;
					}
				} else if (posArr[Sam.CIGAR_POS_REF_START] <= to && to <= posArr[Sam.CIGAR_POS_REF_END]) {
					mdi[M] += to - posArr[Sam.CIGAR_POS_REF_START] + 1;
				} else if (from < posArr[Sam.CIGAR_POS_REF_START] && posArr[Sam.CIGAR_POS_REF_END] < to) {
					mdi[M] += posArr[Sam.CIGAR_POS_REF_END] - posArr[Sam.CIGAR_POS_REF_START] + 1;
				}
			}
			if (posArr[Sam.CIGAR_POS_TYPE] == Sam.D) {
				if (posArr[Sam.CIGAR_POS_REF_START] <= from  && from <= posArr[Sam.CIGAR_POS_REF_END]) {
					if (to <= posArr[Sam.CIGAR_POS_REF_END]) {
						mdi[D] += to - from + 1;
					} else {
						mdi[D] += posArr[Sam.CIGAR_POS_REF_END] - from + 1;
					}
				} else if (posArr[Sam.CIGAR_POS_REF_START] <= to && to <= posArr[Sam.CIGAR_POS_REF_END]) {
					mdi[D] += to - posArr[Sam.CIGAR_POS_REF_START] + 1;
				} else if (from < posArr[Sam.CIGAR_POS_REF_START] && posArr[Sam.CIGAR_POS_REF_END] < to) {
					mdi[D] += posArr[Sam.CIGAR_POS_REF_END] - posArr[Sam.CIGAR_POS_REF_START] + 1;
				}
			}
			if (posArr[Sam.CIGAR_POS_TYPE] == Sam.I) {
				if (from <= posArr[Sam.CIGAR_POS_REF_START] && posArr[Sam.CIGAR_POS_REF_START] <= to) {
					mdi[I] += posArr[Sam.CIGAR_POS_ALGN_RANGE_END] - posArr[Sam.CIGAR_POS_ALGN_RANGE_START] + 1;
				}
			}
		}
		
		return mdi;
	}
	
	/***
	 * Trim off the tag name and type.
	 * @param tag
	 * @return value of the tag
	 */
	public static String parseTag(String tag) {
		String[] tokens = tag.split(":");
		return tokens[2];
	}
	
}