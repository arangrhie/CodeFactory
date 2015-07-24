package javax.arang.genome.sam;

import java.util.ArrayList;
import java.util.Vector;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class CountGCLength extends R2wrapper {

	private static int GC_LENGTH = 5;
	static final int FA_OFFSET = 31728063;
	static final int SAM_OFFSET = 31727563;
	Vector<Integer> gcFaPositions = new Vector<Integer>();
	Vector<String> gcFaBases = new Vector<String>();
	String result;
	private String rformResult = "";

	@Override
	public void hooker(FileReader samReader, FileReader fr) {
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
				if (!(leftSeq == 'G' || leftSeq == 'C')) {
					pointer++;
					pos++;
					continue;
				}
				String gcToken = String.valueOf(leftSeq);
				while (rightSeq == 'G' || rightSeq == 'C') {
					len++;
					pointer++;
					pos++;
					gcToken = gcToken + String.valueOf(rightSeq);
					if (pointer >= line.length() && fr.hasMoreLines()) {
						line = fr.readLine();
						pointer = 0;
						if (line.startsWith(">")) {
							if (len == GC_LENGTH) {
								gcFaPositions.add(FA_OFFSET + pos - GC_LENGTH);
								gcFaBases.add(gcToken);
							}
							// initialize other vars
							isNewRef = true;
							pos++;
							break SEQ_COMP_LOOP;
						}
					}
					rightSeq = line.charAt(pointer);
				}
				if (len == GC_LENGTH) {
					gcFaPositions.add(FA_OFFSET + pos - GC_LENGTH);
					gcFaBases.add(gcToken);
				}
				pointer++;
				pos++;
			}
		}

		result = GC_LENGTH + "\t" + gcFaPositions.size();

		if (gcFaPositions.size() == 0) {
			System.out.println("No " + GC_LENGTH
					+ " size GC contents detected in reference. Return 0.");
			return;
		}
//		printGCpositions(fm);
		
		// parse sam
		
		final int MAX_COUNT = GC_LENGTH * 2; 
		int[] gcCounter = new int[MAX_COUNT];
		
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
			String refSeq = Sam.getRefFromRead(tokens[Sam.SEQ], tokens[Sam.CIGAR], mdtag);
			Vector<String> gcBases = new Vector<String>();
			Vector<Integer> gcPosInRef = getGCpos(refSeq, pos, gcFaPositions, gcBases);
			for (int i = 0; i < gcPosInRef.size(); i++) {
				Integer posInRef = gcPosInRef.get(i);
				String sequencedBases = basesAtRefPos(refSeq, tokens[Sam.SEQ], tokens[Sam.CIGAR], posInRef, gcBases.get(i));
				int diff = sequencedBases.length() - GC_LENGTH;
				if (GC_LENGTH + diff >= MAX_COUNT) {
					System.out.println("Alignment error in " + GC_LENGTH);
					System.out.println(line);
					System.out.println(pos + " " + posInRef + " " + tokens[Sam.CIGAR] + " " + mdtag + "\n" + refSeq + "\n" + tokens[Sam.SEQ]);
					System.out.println(refSeq.substring(posInRef, posInRef + GC_LENGTH) + " " + sequencedBases);
					continue;
				}
				
				gcCounter[GC_LENGTH + diff]++;
//				// test code
//				fm.writeLine(homBases.get(i) + "\t" + diff);
//				if (diff <= 2 - GC_LENGTH) {
//					fm.writeLine(gcBases.get(i) + "\t" + diff + sequencedBases);
//					fm.writeLine(line);
//					fm.writeLine(pos + " " + posInRef + " " + tokens[Sam.CIGAR] + " " + mdtag + "\n" + refSeq + "\n" + tokens[Sam.SEQ]);
//					fm.writeLine(diff + " " + refSeq.substring(posInRef, posInRef + GC_LENGTH) + " " + sequencedBases);
//				}
			}
		}
		
		printSummary(gcCounter);
		


	}
	
	private void printSummary(int[] gcCounter) {
		int sumCount = 0;
		for (int i = 0; i < gcCounter.length; i++) {
			sumCount += gcCounter[i];
			
		}
		
		rformResult = "Poly" + GC_LENGTH;
		for (int i = 0; i < GC_LENGTH*2; i++) {
			rformResult = rformResult +  "\t" + (i - GC_LENGTH);
		}
		rformResult = rformResult + "\nGC";
		
		float maxPercentage = 0;
		int maxIdx = 0;
//		fm.write("GC len " + GC_LENGTH);
		for (int i = 0; i < gcCounter.length; i++) {
			float percentage = (float)gcCounter[i] * 100 / sumCount;
			if (sumCount == 0) {
				rformResult = rformResult + "\t" + 0.00;
			} else {
				rformResult = rformResult + "\t" + String.format("%,.2f", percentage);
			}
			if (maxPercentage < percentage) {
				maxPercentage = percentage;
				maxIdx = i;
			}
//			fm.write("\t" + String.format("%,.2f", percentage));
		}
		rformResult = rformResult + "\n\n";

		if (sumCount > 0) {
			result = result + "\t"
			+ String.format("%,.2f", ((float)gcCounter[GC_LENGTH]*100/sumCount)) + "\t"
			+ maxIdx + "\t"
			+ String.format("%,.2f", maxPercentage);
			
		} else {
			result = result + "\t" + 0 + "\t" + 0 + "\t" + 0;
		}
		
		
		
	}

	private String basesAtRefPos(String refSeq, String readSeq, String cigar,
			Integer posInRef, String gcBases) {
		ArrayList<String[]> cigarArray = Sam.parseArr(cigar);
		
		int refAlignIdx = 0;
		int readIdx = 0;
		
		for (String[] cigarOp : cigarArray) {
			if (cigarOp[Sam.OP].equals("S")) {
				readIdx += Integer.parseInt(cigarOp[Sam.COUNT]);
			} else if (cigarOp[Sam.OP].equals("M")) {
				refAlignIdx += Integer.parseInt(cigarOp[Sam.COUNT]);
				readIdx += Integer.parseInt(cigarOp[Sam.COUNT]);
				if (refAlignIdx >= posInRef) {
					int offset = refAlignIdx - posInRef;
					int left = readIdx - offset;
					return seekRight(left, readSeq, gcBases, refSeq, posInRef);
				}
			} else if (cigarOp[Sam.OP].equals("I")) {
				readIdx += Integer.parseInt(cigarOp[Sam.COUNT]);
			} else if (cigarOp[Sam.OP].equals("D")) {
				refAlignIdx += Integer.parseInt(cigarOp[Sam.COUNT]);
				if (refAlignIdx >= posInRef) {
					return seekRight(readIdx, readSeq, gcBases, refSeq, posInRef);
				}
			}
		}
		return "";
	}

	private String seekRight(int right, String readSeq, String gcBases,
								String refSeq, Integer posInRef) {
		String rightBases = "";
		int nonBaseCount = 0;
		int baseCount = 0;
		int j = 0;
		for (int i = right; i < readSeq.length(); i++) {
			if (gcBases.contains(String.valueOf(readSeq.charAt(i)))) {
				if (j >= gcBases.length()) {
					if (i-right < GC_LENGTH)	{
						nonBaseCount++;
//						if (nonBaseCount > 1 && baseCount > 2)	break;
						rightBases = rightBases + readSeq.charAt(i);
					} else {
						break;
					}
				} else {
					rightBases = rightBases + readSeq.charAt(i);
					baseCount++;
					j++;
				}
			} else {
				if (i-right < GC_LENGTH) { // && refSeq.charAt(pos + i - right + 2) != base) {
					nonBaseCount++;
					if (nonBaseCount > 1)	break;
					rightBases = rightBases + readSeq.charAt(i);
				} else {
					break;
				}
			}
		}
		
		int nonBaseIdx = indexOfNonBase(rightBases);
		if (nonBaseIdx == -1)	return rightBases;
		
		String leftPart = rightBases.substring(0, nonBaseIdx);
		String rightPart = rightBases.substring(nonBaseIdx + 1);
		if (leftPart.length() > rightPart.length())	return leftPart;
		else return rightPart;
	}

	private int indexOfNonBase(String bases) {
		for (int i = 0; i < bases.length(); i++) {
			if (!(bases.charAt(i) == 'C' || bases.charAt(i) == 'G')) {
				return i;
			}
		}
		return -1;
	}
	
	private Vector<Integer> getGCpos(String refSeq, int pos,
										Vector<Integer> gcFaPositions,
										Vector<String> gcBases) {
		Vector<Integer> homPositions = new Vector<Integer>();
		for (int i = 0; i < refSeq.length() - GC_LENGTH; i++) {
			if (gcFaPositions.contains(pos + i)) {
				homPositions.add(i);
				int faPosIdx = gcFaPositions.indexOf(pos + i);
				gcBases.add(gcFaBases.get(faPosIdx));
			}
		}
		return homPositions;
	}

//	private void printGCpositions(FileMaker fm) {
//		fm.writeLine("# of " + GC_LENGTH + " size GC contents:\t"
//				+ gcFaPositions.size());
//		for (int i = 0; i < gcFaPositions.size(); i++) {
//			fm.writeLine("\t" + gcFaPositions.get(i));
//		}
//	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samCountGcLength.jar <in.sam> <ref.fa> <gcLenFrom> <gcLenTo>");
		System.out.println("\t<in.sam>: the input sam file");
		System.out.println("\t<ref.fa>: the reference fa file");
		System.out.println("\t<gcLength>: the gc content length to measure [OPTION]");
		System.out
				.println("\t<output>: the report of gc counts to see the sequencing quality");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 4) {
			int from = Integer.parseInt(args[2]);
			int to = Integer.parseInt(args[3]);
			FileMaker summary = new FileMaker("GC_report_" + from + "_" + to + ".txt");
			summary.writeLine("GC len\t#ofPositions\tCorrectlyCalled(%)\tMaxBasesCalled\tMaxBasesCalled(%)");
			FileMaker gcRfm = new FileMaker("gc_cont.txt"); 
			for (int i = from; i <= to; i++ ) {
				GC_LENGTH = i;
				CountGCLength gcInst = new CountGCLength();
				gcInst.go(args[0], args[1]);
				summary.writeLine(gcInst.getResult());
				gcRfm.writeLine(gcInst.getRformResult());
			}
		} else {
			new CountGCLength().printHelp();
		}

	}

	private String getRformResult() {
		return rformResult;
	}

	private String getResult() {
		return result;
	}

}
