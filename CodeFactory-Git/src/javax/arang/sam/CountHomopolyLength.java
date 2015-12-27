package javax.arang.sam;

import java.util.ArrayList;
import java.util.Vector;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class CountHomopolyLength extends I2Owrapper {

	static int POLY_LENGTH = 5;
	static final int FA_OFFSET = 31728063;
	static final int SAM_OFFSET = 31727563;
	Vector<Integer> homFaPositions = new Vector<Integer>();
	Vector<String> homFaBases = new Vector<String>();
	Vector<String> homBases = new Vector<String>();
	String result;
	private String rformResult = "";
	
	@Override
	public void hooker(FileReader samReader, FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		int pointer = 1;
		int pos = 1;
		char leftSeq;
		char rightSeq = '0';
		boolean isNewRef = false;
		while (fr.hasMoreLines()) {
			line = fr.readLine().trim();
			if (isNewRef) {
				rightSeq = line.charAt(0);
				isNewRef = false;
			}
			pointer = 1;
			if (line.startsWith(">")) {
				// initialize other vars
				isNewRef = true;
				pos = 1;
				continue;
			}
			pos++;
			SEQ_COMP_LOOP: while (pointer < line.length()) {
				leftSeq = rightSeq;
				rightSeq = line.charAt(pointer);
				int len = 1;
				while (leftSeq == rightSeq) {
					len++;
					pointer++;
					pos++;
					if (pointer >= line.length() && fr.hasMoreLines()) {
						line = fr.readLine();
						pointer = 0;
						if (line.startsWith(">")) {
							if (len == POLY_LENGTH) {
								homFaPositions.add(FA_OFFSET + pos - POLY_LENGTH);
								homFaBases.add(String.valueOf(leftSeq));
							}
							// initialize other vars
							isNewRef = true;
							pos = 0;
							break SEQ_COMP_LOOP;
						}
					}
					rightSeq = line.charAt(pointer);
				}
				
				if (len == POLY_LENGTH) {
					homFaPositions.add(FA_OFFSET + pos - POLY_LENGTH);
					homFaBases.add(String.valueOf(leftSeq));
				}
				pointer++;
				pos++;
			}
		}
		
		fm.writeLine("# of poly-" + POLY_LENGTH + "s:\t" + homFaPositions.size());
		result = POLY_LENGTH + "\t" + homFaPositions.size();
		if (homFaPositions.size() == 0) {
			System.out.println("No " + POLY_LENGTH + "-polymers detected in reference. Return 0.");
			return;
		}
		
		int[] refHomCounts = new int[4];
		for (int i = 0; i < homFaPositions.size(); i++) {
			refHomCounts[getBase(homFaBases.get(i))]++;
//			fm.writeLine(homFaPositions.get(i) + "\t" + homFaBases.get(i));
		}
		
		for (int i = 0; i < 4; i++) {
			fm.writeLine(getCharBase(i) + "\t" + refHomCounts[i]);
		}
		fm.writeLine("");
		
		final int MAX_COUNT = 60; 
		int[][] homBaseCounter = new int[4][MAX_COUNT];
		
		// parse sam
		while (samReader.hasMoreLines()) {
			line = samReader.readLine();
			if (line.startsWith("@"))	continue;
			if (line.length() < 5)	continue;
			tokens = line.split("\t");
			if (tokens[Sam.CIGAR].equals("*"))	continue;
			if (SAMUtil.isSecondaryAlignment(Integer.parseInt(tokens[Sam.FLAG])))	continue;
			pos = Integer.parseInt(tokens[Sam.POS]) + SAM_OFFSET;
			String mdtag = Sam.getMDTAG(tokens);
			if (mdtag == null) {
				System.out.println("No MD tag");
				System.out.println(line);
				System.exit(0);
			}
			mdtag = mdtag.substring(mdtag.lastIndexOf(":") + 1);
			String refSeq = getRefFromRead(tokens[Sam.SEQ], tokens[Sam.CIGAR], mdtag);
			Vector<Integer> homPosInRef = getHomPos(refSeq, pos, homFaPositions);
			for (int i = 0; i < homPosInRef.size(); i++) {
				Integer posInRef = homPosInRef.get(i);
				String sequencedBases = basesAtRefPos(refSeq, tokens[Sam.SEQ], tokens[Sam.CIGAR], posInRef, homBases.get(i));
				int diff = sequencedBases.length() - POLY_LENGTH;
				if (POLY_LENGTH + diff >= MAX_COUNT) {
					System.out.println("Alignment error in " + POLY_LENGTH);
					System.out.println(line);
					System.out.println(pos + " " + posInRef + " " + tokens[Sam.CIGAR] + " " + mdtag + "\n" + refSeq + "\n" + tokens[Sam.SEQ]);
					System.out.println(refSeq.substring(posInRef, posInRef + POLY_LENGTH) + " " + sequencedBases);
					continue;
				}
				
				homBaseCounter[getBase(homBases.get(i))][POLY_LENGTH + diff]++;
//				fm.writeLine(homBases.get(i) + "\t" + diff);
//				if (diff <= 2 - POLY_LENGTH) {
//					fm.writeLine(homBases.get(i) + "\t" + diff + otherBases(sequencedBases, homBases.get(i)));
//					fm.writeLine(line);
//					fm.writeLine(pos + " " + posInRef + " " + tokens[Sam.CIGAR] + " " + mdtag + "\n" + refSeq + "\n" + tokens[Sam.SEQ]);
//					fm.writeLine(refSeq.substring(posInRef, posInRef + POLY_LENGTH) + " " + sequencedBases);
//				}
			}
		}
		
		fm.write("Poly" + POLY_LENGTH);
		for (int j = 0; j < POLY_LENGTH*2; j++) {
			fm.write("\t" + (j - POLY_LENGTH));
		}
		fm.writeLine("");
		
		int[] totalCount = new int[4];
		
		for (int i = 0; i < 4; i++) {
			fm.write(getCharBase(i));
			for (int j = 0; j < POLY_LENGTH*2; j++) {
				fm.write("\t" + homBaseCounter[i][j]);
				totalCount[i] += homBaseCounter[i][j];
			}
			fm.writeLine("");
		}
		
		fm.writeLine("");
		
		rformResult = "Poly" + POLY_LENGTH + " (%)"; 
		fm.write("Poly" + POLY_LENGTH + " (%)");
		for (int j = 0; j < POLY_LENGTH*2; j++) {
			rformResult = rformResult + "\t" + (j - POLY_LENGTH); 
			fm.write("\t" + (j - POLY_LENGTH));
		}
		rformResult = rformResult + "\n";
		fm.writeLine("");
		
		for (int i = 0; i < 4; i++) {
			if (totalCount[i] == 0) {
				continue;
			}
			rformResult = rformResult + getCharBase(i);
			fm.write(getCharBase(i));
			for (int j = 0; j < POLY_LENGTH*2; j++) {
				rformResult = rformResult + "\t" + String.format("%,.2f", ((float)(homBaseCounter[i][j]*100)/totalCount[i]));
				fm.write("\t" + String.format("%,.2f", ((float)(homBaseCounter[i][j]*100)/totalCount[i])));
			}
			rformResult = rformResult + "\n";
			fm.writeLine("");
		}
	}
	
//	private String otherBases(String sequencedBases, String base) {
//		String otherBases = "";
//		for (int i = 0; i < sequencedBases.length(); i++) {
//			if (sequencedBases.charAt(i) != base.charAt(0)) {
//				otherBases = "\t" + sequencedBases.charAt(i) + "\t" + i;
//			}
//		}
//		
//		return otherBases + "\t" + sequencedBases;
//	}

	private String getCharBase(int base) {
		switch (base) {
			case 0: return "A";
			case 1: return "T";
			case 2: return "G";
			case 3: return "C";
		}
		return null;
	}

	private int getBase(String base) {
		char baseChar = base.charAt(0);
		switch (baseChar) {
			case 'A': return 0;
			case 'T': return 1;
			case 'G': return 2;
			case 'C': return 3;
		}
		return 4;
	}

	public String basesAtRefPos(String refSeq, String readSeq, String cigar, int pos, String base) {
		String bases = "";
		ArrayList<String[]> cigarArray = Sam.parseArr(cigar);
		
		int refAlignIdx = 0;
		int readIdx = 0;
		
		for (String[] cigarOp : cigarArray) {
			if (cigarOp[Sam.OP].equals("S")) {
				readIdx += Integer.parseInt(cigarOp[Sam.COUNT]);
			} else if (cigarOp[Sam.OP].equals("M")) {
				refAlignIdx += Integer.parseInt(cigarOp[Sam.COUNT]);
				readIdx += Integer.parseInt(cigarOp[Sam.COUNT]);
				if (refAlignIdx >= pos) {
					int offset = refAlignIdx - pos;
					int left = readIdx - offset;
					return seekRight(left, readSeq, base.charAt(0), refSeq, pos);
				}
			} else if (cigarOp[Sam.OP].equals("I")) {
				readIdx += Integer.parseInt(cigarOp[Sam.COUNT]);
			} else if (cigarOp[Sam.OP].equals("D")) {
				refAlignIdx += Integer.parseInt(cigarOp[Sam.COUNT]);
				if (refAlignIdx >= pos) {
					return seekRight(readIdx, readSeq, base.charAt(0), refSeq, pos);
				}
			}
		}
		return bases;
	}
	
	public String seekRight(int right, String readSeq, char base, String refSeq, int pos) {
		String rightBases = "";
		int nonBaseCount = 0;
		int baseCount = 0;
		for (int i = right; i < readSeq.length(); i++) {
			if (readSeq.charAt(i) == base) {
				rightBases = rightBases + String.valueOf(base);
				baseCount++;
			} else {
				if (i-right < POLY_LENGTH) { // && refSeq.charAt(pos + i - right + 2) != base) {
					nonBaseCount++;
					if (nonBaseCount > 1 && baseCount > 2)	break;
					rightBases = rightBases + readSeq.charAt(i);
				} else {
					break;
				}
			}
		}
		
		// trim from both ends
		rightBases = trimNonBaseEnds(rightBases, base);
//		System.out.println("rightBases trimmed: " + rightBases);

		// check if a non-base is in between
		int nonBaseLeftIdx = indexOfNonBaseLeft(rightBases, base);
		int nonBaseRightIdx = indexOfNonBaseRight(rightBases, base);
		if (nonBaseLeftIdx == -1 && nonBaseRightIdx == -1) {
			return rightBases;
		}
		
		String leftSite = "";
		String rightSite = "";
		if (nonBaseLeftIdx > 0) {
			leftSite = rightBases.substring(0, nonBaseLeftIdx);
		}
		if (nonBaseRightIdx > 0) {
			rightSite = rightBases.substring(nonBaseRightIdx + 1);
		}
		
		if (leftSite.length() > rightSite.length()) {
			return leftSite;
		} else {
			return rightSite;
		}
	}

	public String trimNonBaseEnds(String rightBases, char base) {
		for (int i = 0; i < rightBases.length(); i++) {
			if (rightBases.charAt(0) == base) {
				for (int j = rightBases.length() - 1; j >= 0; j--) {
					if (rightBases.charAt(j) == base) {
						return rightBases;
					} else {
						rightBases = rightBases.substring(0, j + 1);
					}
				}
				break;
			} else {
				rightBases = rightBases.substring(1);
			}
		}
		return "";
	}

	private int indexOfNonBaseLeft(String rightBases, char base) {
		for (int i = 0; i < rightBases.length() ; i++) {
			if (rightBases.charAt(i) != base) {
				return i;
			}
		}
		return -1;
	}
	
	private int indexOfNonBaseRight(String rightBases, char base) {
		for (int i = rightBases.length() - 1; i >= 0 ; i--) {
			if (rightBases.charAt(i) != base) {
				return i;
			}
		}
		return -1;
	}

	private Vector<Integer> getHomPos(String refSeq, int pos, Vector<Integer> homFaPositions) {
		Vector<Integer> homPositions = new Vector<Integer>();
		homBases.clear();
		for (int i = 0; i < refSeq.length() - POLY_LENGTH; i++) {
			if (homFaPositions.contains(pos + i)) {
				homPositions.add(i);
				int faPosIdx = homFaPositions.indexOf(pos + i);
				homBases.add(homFaBases.get(faPosIdx));
			}
		}
		return homPositions;
	}

	public String getRefFromRead(String readSeq, String cigar, String mdTag) {
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
//				System.out.println(originalRead + " " + cigarOp[Sam.COUNT] + "S " + shift + " " + offset + " " + cigar + " " + mdTag);
				originalRead = originalRead.replace(shift, shift+offset, "");
//				System.out.println(originalRead);
//				alignRangeStart += offset;
//				alignRangeEnd += offset;
//				shift += offset;
				
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
//						System.out.println("Is there a ^? " + mismatch[Sam.OP]);
						mismatchIdx = mismatchIdx + mismatch[Sam.OP].length() - 1;
						continue;
					} else if (mismatchIdx < matchedIdxStart) {
						mismatchIdx += mismatch[Sam.OP].length();
						continue;
					} else if (matchedIdxStart <= mismatchIdx &&  mismatchIdx <= matchedIdxEnd ){
						try {
							originalRead = originalRead.replace(mismatchIdx, mismatchIdx + mismatch[Sam.OP].length(), mismatch[Sam.OP]);
						} catch(StringIndexOutOfBoundsException e) {
							System.out.println(shift + ", " + mismatchIdx + " > " + originalRead.length());
							System.out.println(cigar + " " + mdTag);
							System.out.println(originalRead);
							System.out.println(readSeq);
							System.out.println();
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
//				alignRangeStart -= offset;
//				alignRangeEnd -= offset;
//				shift += offset;
			}
			
			else {
				continue;
			}
		}
		
//		fm.writeLine(originalRead + "");
		return (originalRead + "");
	}
	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samCountHomopolyLength.jar <in.sam> <ref.fa> <from> <to>");
		System.out.println("\t<in.sam>: the input sam file");
		System.out.println("\t<ref.fa>: the reference fa file");
		System.out.println("\t<from> ~ <to>: the polymer length to measure");
		System.out.println("\t<output>: the report of homopolymer counts to see the sequencing quality");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 4) {
			int from = Integer.parseInt(args[2]);
			int to = Integer.parseInt(args[3]);
			FileMaker summary = new FileMaker("HOM_report_" + from + "_" + to + ".txt");
			summary.writeLine("Poly len\t#ofPositions\tCorrectlyCalled(%)\tMaxBasesCalled\tMaxBasesCalled(%)");
			FileMaker gcRfm = new FileMaker("hom_cont.txt"); 
			for (int i = from; i <= to; i++ ) {
				POLY_LENGTH = i;
				CountHomopolyLength homInst = new CountHomopolyLength();
				homInst.go(args[0], args[1], "hom_len_" + i + ".txt");
				summary.writeLine(homInst.getResult());
				gcRfm.writeLine(homInst.getRformResult());
			}
		} else {
			new CountHomopolyLength().printHelp();
		}

	}
	

	private String getRformResult() {
		return rformResult;
	}

	private String getResult() {
		return result;
	}

}
