package javax.arang.genome.fasta;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ReplaceNwithFills extends I2Owrapper {

	private static int GAP_START = 1;
	private static int GAP_END = 2;
	private static int GAP_LEN = 3;
	private static int N_TYPE = 4;
	private static int FILL_TYPE = 5;
	private static int NEW_SEQ_LEN = 6;
	private static int TOTAL_NUM_READS = 7;
	private static int SEQ = 8;
	
	String line;
	int lineIdx = 0;
	int refIdx = 0;
	int lineNum = 1;
	
	@Override
	public void hooker(FileReader faFr, FileReader fillFr, FileMaker fm) {
		HashMap<Integer, String>	spanBases = new HashMap<Integer, String>();
		HashMap<Integer, String>	leftFlankBases = new HashMap<Integer, String>();
		HashMap<Integer, String>	rightFlankBases = new HashMap<Integer, String>();
		HashMap<Integer, Integer> 	leftFlankLens = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> 	rightFlankLens = new HashMap<Integer, Integer>();
		HashMap<Integer, Character>	nType = new HashMap<Integer, Character>();
		HashMap<Integer, Integer>	gapLen = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer>	gapEnd = new HashMap<Integer, Integer>();
		
		String[] tokens;
		int start = 0;
		
		while (fillFr.hasMoreLines()) {
			line = fillFr.readLine();
			tokens = line.split("\t");
			if (tokens[FILL_TYPE].equals("open"))	continue;
			if (tokens[GAP_START].equals("GAP_START"))	continue;
			if (Integer.parseInt(tokens[TOTAL_NUM_READS]) < totalNumreadCutoff)	continue;
			start = Integer.parseInt(tokens[GAP_START]);
			if (tokens[FILL_TYPE].equals("span")) {
				spanBases.put(start, tokens[SEQ].substring(1, tokens[SEQ].length() - 1));
			} else if (tokens[FILL_TYPE].equals("leftFlank")) {
				leftFlankBases.put(start, tokens[SEQ].substring(1));
				leftFlankLens.put(start, Integer.parseInt(tokens[NEW_SEQ_LEN]) - 1);
			} else if (tokens[FILL_TYPE].equals("rightFlank")) {
				rightFlankBases.put(start, tokens[SEQ].substring(0, tokens[SEQ].length() - 1));
				rightFlankLens.put(start, Integer.parseInt(tokens[NEW_SEQ_LEN]) - 1);
			}
			nType.put(start, tokens[N_TYPE].charAt(0));
			gapLen.put(start, Integer.parseInt(tokens[GAP_LEN]));
			gapEnd.put(start, Integer.parseInt(tokens[GAP_END]));
		}
		
		System.out.println("[DEBUG] :: " + spanBases.size() + " spanning region");
		System.out.println("[DEBUG] :: " + leftFlankBases.size() + " leftFlank region");
		System.out.println("[DEBUG] :: " + rightFlankBases.size() + " rightFlank region");
		
		line = faFr.readLine();
		
		while (faFr.hasMoreLines()) {
			getNextBase(faFr, fm, true);
			refIdx++;
			if (spanBases.containsKey(refIdx)) {
				printGapStart("span");
				
				fm.write(spanBases.get(refIdx));
				jumpTo(faFr, gapEnd.get(refIdx), fm, false);
				printGapEnd();
			} else {
				if (leftFlankBases.containsKey(refIdx) && rightFlankBases.containsKey(refIdx)) {
					printGapStart("left right flank");
					fm.write(leftFlankBases.get(refIdx));
					if (leftFlankLens.get(refIdx) + rightFlankLens.get(refIdx) < gapLen.get(refIdx)) {
						insertN(fm, gapLen.get(refIdx) - (leftFlankLens.get(refIdx) + rightFlankLens.get(refIdx)), nType.get(refIdx));
						System.out.print(" :: insertN : " + (gapLen.get(refIdx) - (leftFlankLens.get(refIdx) + rightFlankLens.get(refIdx))) + " :: ");
					} else {
						insertN(fm, 10, nType.get(refIdx));
						System.out.print(" :: insertN : 10 :: ");
					}
					fm.writeLine(rightFlankBases.get(refIdx));
					jumpTo(faFr, gapEnd.get(refIdx), fm, false);
					printGapEnd();
				}
				else if (leftFlankBases.containsKey(refIdx)){
					printGapStart("left flank");
					fm.writeLine(leftFlankBases.get(refIdx));
					if (leftFlankLens.get(refIdx) <= gapLen.get(refIdx)) {
						insertN(fm, gapLen.get(refIdx) - leftFlankLens.get(refIdx), nType.get(refIdx));
						System.out.print(" :: insertN : " + (gapLen.get(refIdx) - leftFlankLens.get(refIdx)) + " :: ");
					} else {
						insertN(fm, 10, nType.get(refIdx));
						System.out.print(" :: insertN : 10 :: ");
					}
					jumpTo(faFr, gapEnd.get(refIdx), fm, false);
					printGapEnd();
				}  else if (rightFlankBases.containsKey(refIdx)) {
					if (rightFlankLens.get(refIdx) <= gapLen.get(refIdx)) {
						insertN(fm, (gapLen.get(refIdx) - rightFlankLens.get(refIdx)), nType.get(refIdx));
						System.out.println(" :: insertN : " + (gapLen.get(refIdx) - rightFlankLens.get(refIdx)) + " :: ");
					} else {
						insertN(fm, 10, nType.get(refIdx));
						System.out.println(" :: insertN : 10 :: ");
					}
					fm.write(rightFlankBases.get(refIdx));
					printGapStart("right flank");
					jumpTo(faFr, gapEnd.get(refIdx), fm, false);
					printGapEnd();
				} else {
					fm.write(line.charAt(lineIdx));
				}
			}
		}
	}
	
	private void printGapStart(String type) {
		if (lineIdx == 0) {
			System.out.print("[DEBUG] :: nu " + lineNum + " | lineIdx " + lineIdx + " | gapStart : " + refIdx + " | base: " + line.charAt(lineIdx) + " (" + type + ") ");
		} else {
			System.out.print("[DEBUG] :: nu " + lineNum + " | lineIdx " + lineIdx + " | gapStart : " + refIdx + " | base: " + line.charAt(lineIdx - 1) + line.charAt(lineIdx) + " (" + type + ") ");
		}
	}
	
	private void printGapEnd() {
		if (lineIdx < line.length() - 1) {
			System.out.println("" + line.charAt(lineIdx) + line.charAt(lineIdx + 1) + " | lineIdx " + lineIdx + " nu " + lineNum);
		} else {
			System.out.println("" + line.charAt(lineIdx) + " | lineIdx " + lineIdx + " nu " + lineNum);
		}
	}
	
	private void getNextBase(FileReader fr, FileMaker fm, boolean enter) {
		if (line.startsWith(">")) {
			fm.writeLine(line);
			line = fr.readLine();
			lineIdx = 0;
			lineNum++;
		} else if (lineIdx < line.length() - 1) {
			lineIdx++;
		} else {
			line = fr.readLine();
			lineIdx = 0;
			lineNum++;
			if (enter) {
				fm.writeLine();
			}
		}
	}
	
	private void insertN(FileMaker fm, int numN, char n) {
		for (int i = 0; i < numN; i++) {
			fm.write(n);
		}
	}

	private void jumpTo(FileReader fr, int toRefIdx, FileMaker fm, boolean enter) {
		int basesToGo = toRefIdx - refIdx - 1;
		for (int i = 0; i < basesToGo; i++) {
			getNextBase(fr, fm, enter);
		}
		refIdx = toRefIdx - 1;
	}
	
	

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaReplaceNwithFills.jar <ref.fa> <newref.fill> <out.fa> [totalNumreadCutoff=1]");
		System.out.println("\t<newref.fill> should be made with samPacBioGapFilling.jar");
		System.out.println("\t<totalNumreadCutoff> : TOTAL_NUM_READS < totalNumreadCutoff will be discarded.");
	}
	
	static int totalNumreadCutoff = 1;

	public static void main(String[] args) {
		if (args.length == 3) {
			new ReplaceNwithFills().go(args[0], args[1], args[2]);
		} else if (args.length == 4) {
			totalNumreadCutoff = Integer.parseInt(args[3]);
			new ReplaceNwithFills().go(args[0], args[1], args[2]);
		} else {
			new ReplaceNwithFills().printHelp();
		}
	}

}
