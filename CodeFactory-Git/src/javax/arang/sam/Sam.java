package javax.arang.sam;

import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Pattern;

public class Sam {
	
	/***
	 * Query read id
	 */
	public static final short QNAME = 0;
	public static final short FLAG = 1;
	
	/***
	 * Reference name (Chromosome/Contig)
	 */
	public static final short RNAME = 2;	// AKA Chromosome
	public static final short POS = 3;
	public static final short MAPQ = 4;
	public static final short CIGAR = 5;
	public static final short RNEXT = 6;
	public static final short PNEXT = 7;
	public static final short TLEN = 8;
	public static final short SEQ = 9;
	public static final short QUAL = 10;
	public static final short TAG = 11;
	private static short MDTAG = 11;	// ion torrent

	public static final short COUNT = 0;
	public static final short OP = 1;
	private static Vector<String> deletions = new Vector<String>();
	
//	private Queue<String> deletions = new LinkedList<String>();
	
	public static String getMDTAG(String[] tokens) {
		if (!tokens[MDTAG].startsWith("MD")) {
			for (MDTAG = QUAL + 1; MDTAG < tokens.length; MDTAG++) {
				if (tokens[MDTAG].startsWith("MD")) {
					//System.out.println(tokens[MDTAG]);
					return tokens[MDTAG];
				}
			}
		} else {
			return tokens[MDTAG];
		}
		System.out.println("No MD tag found in " + tokens[0]);
		return null;
	}
	
	public static ArrayList<String[]> parseArr(String arr) {
		String count = "";
		String cigarOp = null;
		ArrayList<String[]> array = new ArrayList<String[]>();
		deletions.clear();
		arr = arr.substring(arr.lastIndexOf(":") + 1);
		
		for( int idx=0; idx<arr.length(); idx++ ){
			if( Pattern.matches("[A-Z^]", Character.toString(arr.charAt(idx))) ){
				cigarOp = Character.toString(arr.charAt(idx));
				String[] tmp = new String[2];
				tmp[COUNT] = count;		// int
				tmp[OP] = cigarOp;		// [A-Z^]
				while (idx + 1 < arr.length() && Pattern.matches("[A-Z^]", Character.toString(arr.charAt(idx + 1))) ) {
					if (arr.charAt(idx + 1) == '^') {
						break;
					}
					tmp[OP] = tmp[OP] + arr.charAt(++idx);
				}
				if (cigarOp.startsWith("^")) {
					String delBases = tmp[OP].substring(1);
					deletions.add(delBases);
					//System.out.println("DEBUG :: del : " + delBases);
				}
				//System.out.println("DEBUG :: parseArr COUNT-" + tmp[COUNT] + ", OP-" + tmp[OP]);
				array.add(tmp);
				count = "";
			} else {
				count = count + arr.charAt(idx);
			}
		}
		return array;
	}
	
	public static ArrayList<String> getDeletionBases(String arr) {
		String count = "";
		String cigarOp = null;
		ArrayList<String> deletion = new ArrayList<String>();
		arr = arr.substring(arr.lastIndexOf(":") + 1);
		
		for( int idx=0; idx<arr.length(); idx++ ){
			if( Pattern.matches("[A-Z^]", Character.toString(arr.charAt(idx))) ){
				cigarOp = Character.toString(arr.charAt(idx));
				String[] tmp = new String[2];
				tmp[COUNT] = count;		// int
				tmp[OP] = cigarOp;		// [A-Z^]
				while (idx + 1 < arr.length() && Pattern.matches("[A-Z^]", Character.toString(arr.charAt(idx + 1))) ) {
					if (arr.charAt(idx + 1) == '^') {
						break;
					}
					tmp[OP] = tmp[OP] + arr.charAt(++idx);
				}
				if (cigarOp.startsWith("^")) {
					String delBases = tmp[OP].substring(1);
					deletion.add(delBases);
					//System.out.println("DEBUG :: del : " + delBases);
				}
				//System.out.println("DEBUG :: parseArr COUNT-" + tmp[COUNT] + ", OP-" + tmp[OP]);
				count = "";
			} else {
				count = count + arr.charAt(idx);
			}
		}
		return deletion;
	}
	
	public static String getNextDelBases() {
		String del = deletions.get(0);
		deletions.remove(0);
		return del;
	}
	
	
	public static final int CIGAR_POS_REF_START = 0;
	public static final int CIGAR_POS_REF_END = 1;
	public static final int CIGAR_POS_ALGN_RANGE_START = 2;
	public static final int CIGAR_POS_ALGN_RANGE_END = 3;
	public static final int CIGAR_POS_TYPE = 4;
	
	public static final int M = 0;
	public static final int S = 1;
	public static final int D = 2;
	public static final int I = 3;
	public static final int N = 4;
	
			
	
	/***
	 * Get matched positions (fragments)
	 * @param sam
	 * @return
	 */
	public static ArrayList<int[]> getPosition(int position, String cigar){
		ArrayList<int[]> output = new ArrayList<int[]>();
		
		// 1-based coordinate
		int alignRangeStart = 1;	// inclusive
		int alignRangeEnd = 0;		// inclusive

		int pos = position;
		int refStartPos = pos;
		int refEndPos = pos - 1;
		int offset;
		
		
		ArrayList<String[]> cigarArray = parseArr(cigar);
		for (String[] cigarOp : cigarArray) {
			// Skip
			if( cigarOp[OP].equals("S")) {
				offset = Integer.parseInt(cigarOp[COUNT]);
				alignRangeStart += offset;
				alignRangeEnd += offset;
			}
			// Match
			else if ( cigarOp[OP].equals("M") ){
				offset = Integer.parseInt(cigarOp[COUNT]);
				alignRangeEnd += offset;
				refEndPos += offset;
				
				int[] tmp = new int[4];
				tmp[CIGAR_POS_REF_START] = refStartPos;
				tmp[CIGAR_POS_REF_END] = refEndPos;
				tmp[CIGAR_POS_ALGN_RANGE_START] = alignRangeStart;
				tmp[CIGAR_POS_ALGN_RANGE_END] = alignRangeEnd;
				output.add(tmp);
				
				alignRangeStart += offset;
				refStartPos += offset;
			}
			
			// splice site - treat it as a Deletion
			else if( cigarOp[OP].equals("N") || cigarOp[OP].equals("D") ){
				try {
					offset = Integer.parseInt(cigarOp[COUNT]);
					refStartPos += offset;
					refEndPos += offset;
				} catch (RuntimeException e) {
					System.out.println(":: DEBUG :: no deletion base detected");
					System.out.println(":: DEBUG :: " + cigar);
					throw e;
				}
			}
			
			// Insertion
			else if( cigarOp[OP].equals("I") ){
				offset = Integer.parseInt(cigarOp[COUNT]);
				alignRangeStart += offset;
				alignRangeEnd += offset;
			}
		}
		return output;
	}
	
	public static String getCigarType(int cigarType) {
		switch (cigarType) {
		case M: return "M";
		case S: return "S";
		case D: return "D";
		case I: return "I";
		case N: return "N";
		}
		return "Cigar type unknown?? " + cigarType;
	}
	/***
	 * Get matched and soft-clipped positions (fragments)
	 * @param sam
	 * @return ArrayList<int[]> of positions: int[CIGAR_POS_REF_START, CIGAR_POS_REF_END, CIGAR_POS_ALGN_RANGE_START, CIGAR_POS_ALGN_RANGE_END, CIGAR_POS_TYPE(M/S/D/I)]
	 * If CIGAR_TYPE is D: no assumptions can be made for ALIGN_RANGE_START and ALIGN_RANGE_END.
	 * If CIGAR_TYPE is I: no assumptions can be made for REF_START_POS and REF_END_POS.
	 * posArr[Sam.CIGAR_POS_ALGN_RANGE_START]: 0-based start
	 * 
	 */
	public static ArrayList<int[]> getAllPosition(int position, String cigar){
		ArrayList<int[]> output = new ArrayList<int[]>();
		
		// 0-based coordinate
		int alignRangeStart = 0;	// inclusive
		int alignRangeEnd = -1;		// inclusive

		// 1-based
		int pos = position;
		int refStartPos = pos;		// 1-base
		int refEndPos = pos - 1;
		int offset;
		
		boolean isFirstSofclip = true;
		
		ArrayList<String[]> cigarArray = parseArr(cigar);
		for (String[] cigarOp : cigarArray) {
			// Skip
			if( cigarOp[OP].equals("S")) {
				offset = Integer.parseInt(cigarOp[COUNT]);
				alignRangeEnd += offset;
				
				if (isFirstSofclip) {
					refStartPos = refStartPos - offset;
				} else {
					refEndPos += offset;
				}
				
				int[] tmp = new int[5];
				tmp[CIGAR_POS_REF_START] = refStartPos;
				tmp[CIGAR_POS_REF_END] = refEndPos;
				tmp[CIGAR_POS_ALGN_RANGE_START] = alignRangeStart;
				tmp[CIGAR_POS_ALGN_RANGE_END] = alignRangeEnd;
				tmp[CIGAR_POS_TYPE] = S;
				output.add(tmp);
				
				alignRangeStart += offset;
				if (isFirstSofclip) {
					refStartPos = refStartPos + offset;
				} 
			}
			// Match
			else if ( cigarOp[OP].equals("M") ){
				offset = Integer.parseInt(cigarOp[COUNT]);
				alignRangeEnd += offset;
				refEndPos += offset;
				
				int[] tmp = new int[5];
				tmp[CIGAR_POS_REF_START] = refStartPos;
				tmp[CIGAR_POS_REF_END] = refEndPos;
				tmp[CIGAR_POS_ALGN_RANGE_START] = alignRangeStart;
				tmp[CIGAR_POS_ALGN_RANGE_END] = alignRangeEnd;
				tmp[CIGAR_POS_TYPE] = M;
				output.add(tmp);
				
				alignRangeStart += offset;
				refStartPos += offset;
			}
			
			// Deletion
			else if( cigarOp[OP].equals("D") ){
				try {
					offset = Integer.parseInt(cigarOp[COUNT]);
					refEndPos += offset;
					
					int[] tmp = new int[5];
					tmp[CIGAR_POS_REF_START] = refStartPos;
					tmp[CIGAR_POS_REF_END] = refEndPos;
					tmp[CIGAR_POS_ALGN_RANGE_START] = alignRangeStart;
					tmp[CIGAR_POS_ALGN_RANGE_END] = alignRangeEnd;
					tmp[CIGAR_POS_TYPE] = D;
					output.add(tmp);

					refStartPos += offset;
					
				} catch (RuntimeException e) {
					System.out.println(":: DEBUG :: no deletion base detected");
					System.out.println(":: DEBUG :: " + cigar);
					throw e;
				}
			}
			
			// splice site - treat it as N
			else if( cigarOp[OP].equals("N") || cigarOp[OP].equals("D") ){
				try {
					offset = Integer.parseInt(cigarOp[COUNT]);
					refEndPos += offset;
					
					int[] tmp = new int[5];
					tmp[CIGAR_POS_REF_START] = refStartPos;
					tmp[CIGAR_POS_REF_END] = refEndPos;
					tmp[CIGAR_POS_ALGN_RANGE_START] = alignRangeStart;
					tmp[CIGAR_POS_ALGN_RANGE_END] = alignRangeEnd;
					tmp[CIGAR_POS_TYPE] = N;
					output.add(tmp);

					refStartPos += offset;
					
				} catch (RuntimeException e) {
					System.out.println(":: DEBUG :: no deletion base detected");
					System.out.println(":: DEBUG :: " + cigar);
					throw e;
				}
			}
			
			// Insertion
			else if( cigarOp[OP].equals("I") ){
				offset = Integer.parseInt(cigarOp[COUNT]);
				alignRangeEnd += offset;
				int[] tmp = new int[5];
				tmp[CIGAR_POS_REF_START] = refStartPos;
				tmp[CIGAR_POS_REF_END] = refEndPos;
				tmp[CIGAR_POS_ALGN_RANGE_START] = alignRangeStart;
				tmp[CIGAR_POS_ALGN_RANGE_END] = alignRangeEnd;
				tmp[CIGAR_POS_TYPE] = I;
				output.add(tmp);
				alignRangeStart += offset;
			}
			isFirstSofclip = false;
		}
		return output;
	}
	/***
	 * Get deleted positions (fragments)
	 * @param sam
	 * @return
	 * @deprecated Use getAllPosition CIGAR_TYPE field == I
	 */
	public static ArrayList<int[]> getInsPosition(int position, String cigar){
		ArrayList<int[]> output = new ArrayList<int[]>();
		
		// 1-based coordinate
		int alignRangeStart = 1;	// inclusive
		int alignRangeEnd = 0;		// inclusive

		int pos = position;
		int refStartPos = pos;
		int refEndPos = pos - 1;
		int offset;
		
		
		ArrayList<String[]> cigarArray = parseArr(cigar);
		for (String[] cigarOp : cigarArray) {
			// Skip
			if( cigarOp[OP].equals("S")) {
				offset = Integer.parseInt(cigarOp[COUNT]);
				alignRangeStart += offset;
				alignRangeEnd += offset;
			}
			// Match
			else if ( cigarOp[OP].equals("M") ){
				offset = Integer.parseInt(cigarOp[COUNT]);
				refEndPos += offset;
				refStartPos += offset;
				alignRangeEnd += offset;
				alignRangeStart += offset;
			}
			
			// splice site - treat it as a Deletion
			else if( cigarOp[OP].equals("N") || cigarOp[OP].equals("D") ){
				try {
					offset = Integer.parseInt(cigarOp[COUNT]);
					refStartPos += offset;
					refEndPos += offset;
				} catch (RuntimeException e) {
					System.out.println(":: DEBUG :: no deletion base detected");
					System.out.println(":: DEBUG :: " + cigar);
					throw e;
				}
			}
			
			// Insertion
			else if( cigarOp[OP].equals("I") ){
				offset = Integer.parseInt(cigarOp[COUNT]);
				alignRangeEnd += offset;
				int[] tmp = new int[4];
				tmp[CIGAR_POS_REF_START] = refStartPos - 1;
				tmp[CIGAR_POS_REF_END] = refEndPos;
				tmp[CIGAR_POS_ALGN_RANGE_START] = alignRangeStart;
				tmp[CIGAR_POS_ALGN_RANGE_END] = alignRangeEnd;
				output.add(tmp);
				alignRangeStart += offset;
			}
		}
		return output;
	}
	
	/***
	 * Get deleted positions (fragments)
	 * @param sam
	 * @return
	 * @deprecated Use getAllPosition CIGAR_TYPE == D
	 */
	public static ArrayList<int[]> getDelPosition(int position, String cigar){
		ArrayList<int[]> output = new ArrayList<int[]>();
		
		// 1-based coordinate
		int alignRangeStart = 1;	// inclusive
		int alignRangeEnd = 0;		// inclusive
		
		int pos = position;
		int refStartPos = pos;
		int refEndPos = pos - 1;
		int offset;
		
		
		ArrayList<String[]> cigarArray = parseArr(cigar);
		for (String[] cigarOp : cigarArray) {
			// Skip
			if( cigarOp[OP].equals("S")) {
				offset = Integer.parseInt(cigarOp[COUNT]);
				alignRangeStart += offset;
				alignRangeEnd += offset;
			}
			// Match
			else if ( cigarOp[OP].equals("M") ){
				offset = Integer.parseInt(cigarOp[COUNT]);
				refEndPos += offset;
				refStartPos += offset;
				alignRangeEnd += offset;
				alignRangeStart += offset;
			}
			
			// splice site - treat it as a Deletion
			else if( cigarOp[OP].equals("N") || cigarOp[OP].equals("D") ){
				try {
					offset = Integer.parseInt(cigarOp[COUNT]);
					refEndPos += offset;

					int[] tmp = new int[4];
					tmp[CIGAR_POS_REF_START] = refStartPos;
					tmp[CIGAR_POS_REF_END] = refEndPos;
					tmp[CIGAR_POS_ALGN_RANGE_START] = alignRangeStart;
					tmp[CIGAR_POS_ALGN_RANGE_END] = alignRangeEnd;
					output.add(tmp);
					
					refStartPos += offset;
				} catch (RuntimeException e) {
					System.out.println(":: DEBUG :: no deletion base detected");
					System.out.println(":: DEBUG :: " + cigar);
					throw e;
				}
			}
			
			// Insertion
			else if( cigarOp[OP].equals("I") ){
				offset = Integer.parseInt(cigarOp[COUNT]);
				alignRangeEnd += offset;
				alignRangeStart += offset;
			}
		}
		return output;
	}
	
	public static int getMatchedBasesLen(String cigarArr) {
		int matchedBases = 0;
		ArrayList<String[]> cigarArray = parseArr(cigarArr);
		for (String[] cigar : cigarArray) {
			if (cigar[OP].equals("M")) {
				matchedBases += Integer.parseInt(cigar[COUNT]);
			}
		}
		return matchedBases;
	}
	
	public static int getInsertedBasesLen(String cigarArr) {
		int matchedBases = 0;
		ArrayList<String[]> cigarArray = parseArr(cigarArr);
		for (String[] cigar : cigarArray) {
			if (cigar[OP].equals("I")) {
				matchedBases += Integer.parseInt(cigar[COUNT]);
			}
		}
		return matchedBases;
	}
	
	public static int getDeletedSplicedBasesLen(String cigarArr) {
		int deletedBases = 0;
		ArrayList<String[]> cigarArray = parseArr(cigarArr);
		for (String[] cigar : cigarArray) {
			if (cigar[OP].equals("N") || cigar[OP].equals("D")) {
				deletedBases += Integer.parseInt(cigar[COUNT]);
			}
		}
		return deletedBases;
		
	}
	
	public static int getSoftclippedBasesLen(String cigarArr) {
		int skippedBases = 0;
		ArrayList<String[]> cigarArray = parseArr(cigarArr);
		for (String[] cigar : cigarArray) {
			if (cigar[OP].equals("S")) {
				skippedBases += Integer.parseInt(cigar[COUNT]);
			}
		}
		return skippedBases;
	}
	
	public static int getLeftSoftclippedBasesLen(String cigarArr) {
		int skippedBases = 0;
		ArrayList<String[]> cigarArray = parseArr(cigarArr);
		String[] cigar = cigarArray.get(0);
		if (cigar[OP].equals("S")) {
			skippedBases += Integer.parseInt(cigar[COUNT]);
		}
		return skippedBases;
	}
	
	public static int getMismatchedBasesLen(String mdTag) {
		int mismatchedBases = 0;
		mdTag = mdTag.substring(mdTag.lastIndexOf(":") + 1);
		boolean isDel = false;
		for( int idx=0; idx<mdTag.length(); idx++ ){
			if( Pattern.matches("[A-Z^]", Character.toString(mdTag.charAt(idx))) ){
				if (mdTag.charAt(idx) == '^') {
					isDel = true;
					continue;
				}
				if (!isDel)	mismatchedBases++;
			} else {
				isDel = false;
			}
		}
		return mismatchedBases;
	}
	
	public static int getStartSoftclip(String cigarArr) {
		int posM = cigarArr.indexOf("M");
		int posS = cigarArr.indexOf("S");
		int posH = cigarArr.indexOf("H");
		int from = 0;
		if (posH > -1 && (posH < posS || posH < posM)) {
			from = Integer.parseInt(cigarArr.substring(0, posH));
		}
		if (posS == -1 || posM < posS) {
			return from;
		} else {
			if (posH > -1 && posH < posS) {
				return from + Integer.parseInt(cigarArr.substring(posH + 1, posS));
			} else {
				return from + Integer.parseInt(cigarArr.substring(0, posS));
			}
		}
	}
	
	public static int getEndSoftclip(String cigarArr) {
		
		if (cigarArr.charAt(cigarArr.length() - 1) == 'S') {
			for (int i = cigarArr.length() - 2; i >= 0; i--) {
				if (cigarArr.charAt(i) == 'M' || cigarArr.charAt(i) == 'H'
						|| cigarArr.charAt(i) == 'D' || cigarArr.charAt(i) == 'I'
						|| cigarArr.charAt(i) == '^') {
					return Integer.parseInt(cigarArr.substring(i + 1, cigarArr.length() - 1));
				}
			}
		}
		return 0;
		
//		int posM = cigarArr.lastIndexOf("M");
//		int posS = cigarArr.lastIndexOf("S");
//		int posH = cigarArr.lastIndexOf("H");
//		int to = 0;
//		
//		if (posH > posM) {
//			to = Integer.parseInt(cigarArr.substring(posH + 1));
//		}
//		if (posS == -1 || posM > posS) {
//			return to;
//		} else {
//			return Integer.parseInt(cigarArr.substring(posM + 1, posS)) + to;
//		}
	}
	
	public static int getDeletedBasesLen(String cigarArr) {
		int deletedBases = 0;
		ArrayList<String[]> cigarArray = parseArr(cigarArr);
		for (String[] cigar : cigarArray) {
			if (cigar[OP].equals("D")) {
				deletedBases += Integer.parseInt(cigar[COUNT]);
			}
		}
		return deletedBases;
	}
	
	public static int getHardclippedBasesLen(String cigarArr) {
		int hardClippedBases = 0;
		ArrayList<String[]> cigarArray = parseArr(cigarArr);
		for (String[] cigar : cigarArray) {
			if (cigar[OP].equals("H")) {
				hardClippedBases += Integer.parseInt(cigar[COUNT]);
			}
		}
		return hardClippedBases;
	}
	
	
	/***
	 * Convert back to ref-like sequence, with no inserted bases, deletions as Ds.
	 * @param seq
	 * @param cigar
	 * @return
	 */
	public static String makeMockSequence(String seq, String cigar) {
		StringBuffer mockSeqBuff = new StringBuffer();
		ArrayList<String[]> cigarArray = parseArr(cigar);
		
		int alignRangeStart = 0;	// 0-based
		int alignRangeEnd = 0;		// 1-based

		int offset;

		for (String[] cigarOp : cigarArray) {
			// Skip
			if( cigarOp[OP].equals("S")) {
				offset = Integer.parseInt(cigarOp[COUNT]);
				alignRangeEnd += offset;
				alignRangeStart += offset;
			}
			// Match
			else if ( cigarOp[OP].equals("M") || cigarOp[OP].equals("=") || cigarOp[OP].equals("X")){
				offset = Integer.parseInt(cigarOp[COUNT]);
				alignRangeEnd += offset;
				mockSeqBuff.append(seq.substring(alignRangeStart, alignRangeEnd));
				alignRangeStart += offset;
			}
			// Insertion
			else if (cigarOp[OP].equals("I")) {
				offset = Integer.parseInt(cigarOp[COUNT]);
				alignRangeStart += offset;
				alignRangeEnd += offset;
			}
			// Deletion
			else if( cigarOp[OP].equals("D") ){
				offset = Integer.parseInt(cigarOp[COUNT]);
				for (int i = 0; i < offset; i++) {
					mockSeqBuff.append('D');
				}
			}
			
			// N splicing
			else if (cigarOp[OP].equals("N")) {
				offset = Integer.parseInt(cigarOp[COUNT]);
				for (int i = 0; i < offset; i++) {
					mockSeqBuff.append('N');
				}
			}
		}
		return mockSeqBuff.toString();
	}
	
	
	/***
	 * Make Reference Sequence out of sam read sequence, cigar, MD, etc.
	 * @param sam
	 * @return
	 */
	public static String makeRefSequence(String seq, String cigar, String mdTag){
		String reference = seq;
		StringBuffer originalRead = new StringBuffer(reference);
		
		final ArrayList<String[]> cigarArray = parseArr(cigar);
		final ArrayList<String[]> mismatches = parseArr(mdTag);
		
		// no mismatch
		if (mismatches.isEmpty()) {
			return reference;
		}
		
		// contains at least one mismatch
		int mismatchIdx = 0;
		int matchedIdxStart = 0;
		int matchedIdxEnd = -1;
		int offset;
		int shift = 0;
		
		for (String[] cigarOp : cigarArray) {
			// Skip
			if( cigarOp[OP].equals("S")) {
				offset = Integer.parseInt(cigarOp[COUNT]);
				shift += offset;
			}
			// Match
			else if ( cigarOp[OP].equals("M") ){
				offset = Integer.parseInt(cigarOp[COUNT]);
				matchedIdxEnd += offset;

				//M:alignRangeStart..alignRangeEnd
				for (final String[] mismatch : mismatches) {
					if (!mismatch[Sam.COUNT].equals("")) {
						mismatchIdx += Integer.parseInt(mismatch[Sam.COUNT]);
					}
					// Deletion starting with ^A..
					if (mismatch[OP].startsWith("^")) {
						continue;
					} else if (mismatchIdx < matchedIdxStart) {
						mismatchIdx += mismatch[OP].length();
						continue;
					} else if (matchedIdxStart <= mismatchIdx &&  mismatchIdx <= matchedIdxEnd ){
						originalRead.replace(shift + mismatchIdx,
								shift + mismatchIdx+mismatch[OP].length(), mismatch[OP]);
						mismatchIdx += mismatch[OP].length();
					} else {
						break;
					}
				}
				matchedIdxStart += offset;
				mismatchIdx = 0;
			}
			
			// splice site - treat it as a Deletion
			else if( cigarOp[OP].equals("N") || cigarOp[OP].equals("D") ){
				// do nothing
			}
			
			// Insertion
			else if( cigarOp[OP].equals("I") ){
				offset = Integer.parseInt(cigarOp[COUNT]);
				shift += offset;
			}
		}
		return originalRead.toString();
	}

	/***
	 * Get reference from read: including deleted bases
	 * @param readSeq
	 * @param cigar
	 * @param mdTag
	 * @return
	 */
	public static String getRefFromRead(String readSeq, String cigar, String mdTag) {
		ArrayList<String[]> cigarArray = Sam.parseArr(cigar);
		ArrayList<String[]> mismatches = Sam.parseArr(mdTag);
		
		String reference = readSeq;
		StringBuffer originalRead = new StringBuffer(reference);
		
		// no mismatch
//		if (mismatches.isEmpty()) {
//			return reference;
//		}
		
		// contains at least one mismatch
		int mismatchIdx = 0;
		int matchedIdxStart = 0;
		int matchedIdxEnd = -1;
		int offset;
		int shift = 0;
		
		for (String[] cigarOp : cigarArray) {
			// Skip
			if( cigarOp[Sam.OP].equals("S")) {
				offset = Integer.parseInt(cigarOp[Sam.COUNT]);
				originalRead = originalRead.replace(shift, shift+offset, "");
			}
			// Match
			else if ( cigarOp[Sam.OP].equals("M") ){
				offset = Integer.parseInt(cigarOp[Sam.COUNT]);
				matchedIdxEnd += offset;

				//M:alignRangeStart..alignRangeEnd
				for (final String[] mismatch : mismatches) {
					if (!mismatch[Sam.COUNT].equals("")) {
						mismatchIdx += Integer.parseInt(mismatch[Sam.COUNT]);
					}
					// Deletion starting with ^A..
					if (mismatch[Sam.OP].startsWith("^")) {
						mismatchIdx = mismatchIdx + mismatch[Sam.OP].length() - 1;
						continue;
					} else if (mismatchIdx < matchedIdxStart) {
						mismatchIdx += mismatch[Sam.OP].length();
						continue;
					} else if (matchedIdxStart <= mismatchIdx &&  mismatchIdx <= matchedIdxEnd ){
						try {
							originalRead = originalRead.replace(mismatchIdx, mismatchIdx + mismatch[Sam.OP].length(), mismatch[Sam.OP]);
						} catch(StringIndexOutOfBoundsException e) {
							System.out.println(readSeq);
							throw e;
						}
						mismatchIdx += mismatch[Sam.OP].length();
					} else {
						break;
					}
				}
				matchedIdxStart += offset;
				shift += offset;
				mismatchIdx = 0;
			}
			
			// splice site - treat it as a Deletion
			else if( cigarOp[Sam.OP].equals("N") || cigarOp[Sam.OP].equals("D") ){
				String del = Sam.getNextDelBases();
				offset = Integer.parseInt(cigarOp[Sam.COUNT]);
				originalRead = originalRead.insert(shift, del);
				matchedIdxStart += offset;
				matchedIdxEnd += offset;
				
				shift += offset;
			}
			
			// Insertion
			else if( cigarOp[Sam.OP].equals("I") ){
				offset = Integer.parseInt(cigarOp[Sam.COUNT]);
				originalRead = originalRead.replace(shift, shift+offset, "");
			}
			
			else {
				continue;
			}
		}
		
//		fm.writeLine(originalRead + "");
		return (originalRead + "");
	}
	
//	public static void main(String[] args) {
//		System.out.println("Test 6M1D4M4D4M1D4M1I92M1D15M1S17H\tMD:Z:0C1G3^C4^CGGT2C1^A4C3A0G0C1A0G76G0C4^G15");
//		System.out.println("Num Mapped: " + getMatchedBasesLen("6M1D4M4D4M1D4M1I92M1D15M1S17H"));
//		System.out.println("Num Skipped: " + getSoftclippedBasesLen("6M1D4M4D4M1D4M1I92M1D15M1S17H"));
//		System.out.println("Num Mismatched: " + getMismatchedBasesLen("MD:Z:0C1G3^C4^CGGT2C1^A4C3A0G0C1A0G76G0C4^G15"));
//		System.out.println("Num Deleted: " + getDeletedBasesLen("6M1D4M4D4M1D4M1I92M1D15M1S17H"));
//	}
}
