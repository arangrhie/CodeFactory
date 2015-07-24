package javax.arang.txt.fill;

import java.util.HashMap;
import java.util.Vector;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ConvertToNewCoord extends IOwrapper {

	private static int CHR = 0;
	private static int GAP_START = 1;
	private static int GAP_END = 2;
	private static int GAP_LEN = 3;
	private static int N_TYPE = 4;
	private static int FILL_TYPE = 5;
	private static int NEW_SEQ_LEN = 6;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		HashMap<Integer, Integer>	spanLen = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> 	leftFlankLens = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> 	rightFlankLens = new HashMap<Integer, Integer>();
		HashMap<Integer, Character>	nType = new HashMap<Integer, Character>();
		HashMap<Integer, Integer>	gapLen = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer>	gapEnd = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer>	openLen = new HashMap<Integer, Integer>();
		
		int start;
		Vector<Integer> startList = new Vector<Integer>();
		String chr = "";
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			if (tokens[GAP_START].equals("GAP_START"))	continue;
			chr = tokens[CHR];
			start = Integer.parseInt(tokens[GAP_START]);
			if (!startList.contains(start)) {
				startList.add(start);
			}
			if (tokens[FILL_TYPE].equals("span")) {
				spanLen.put(start, Integer.parseInt(tokens[NEW_SEQ_LEN]) - 2);
			} else if (tokens[FILL_TYPE].equals("leftFlank")) {
				leftFlankLens.put(start, Integer.parseInt(tokens[NEW_SEQ_LEN]) - 1);
			} else if (tokens[FILL_TYPE].equals("rightFlank")) {
				rightFlankLens.put(start, Integer.parseInt(tokens[NEW_SEQ_LEN]) - 1);
			} else if (tokens[FILL_TYPE].equals("open")) {
				openLen.put(start, Integer.parseInt(tokens[GAP_LEN]));
			}
			nType.put(start, tokens[N_TYPE].charAt(0));
			gapEnd.put(start, Integer.parseInt(tokens[GAP_END]));
			gapLen.put(start, Integer.parseInt(tokens[GAP_LEN]));
		}
		
		System.out.println("[DEBUG] :: " + spanLen.size() + " spanning region");
		System.out.println("[DEBUG] :: " + leftFlankLens.size() + " leftFlank region");
		System.out.println("[DEBUG] :: " + rightFlankLens.size() + " rightFlank region");
		
		fm.writeLine("CHR\tNEW_REF_START\tNEW_REF_END\tGAP_START\tGAP_END\tGAP_LEN\tFILL_TYPE\tN_TYPE\tetc");
		int newrefIdx = 0;
		int oldrefIdx = 0;
		String etc;
		for (int i = 0; i < startList.size(); i++) {
			start = startList.get(i);
			newrefIdx += (start - oldrefIdx);
			fm.write(chr + "\t" + newrefIdx + "\t");
			if (spanLen.containsKey(start)) {
				newrefIdx += spanLen.get(start);
				etc = spanLen.get(start) + "";
				writeLine(fm, newrefIdx, start, gapEnd.get(start), "span", nType.get(start), etc);
			} else if (leftFlankLens.containsKey(start) && rightFlankLens.containsKey(start)) {
				if (leftFlankLens.get(start) + rightFlankLens.get(start) < gapLen.get(start)) {
					newrefIdx += gapLen.get(start);
					etc = leftFlankLens.get(start) + " " + rightFlankLens.get(start);
				} else {
					newrefIdx += (leftFlankLens.get(start) + rightFlankLens.get(start) + 10);
					etc = leftFlankLens.get(start)  + " + " + 10 + " + " + rightFlankLens.get(start);
				}
				writeLine(fm, newrefIdx, start, gapEnd.get(start), "bothExtended", nType.get(start), etc);
			} else if (leftFlankLens.containsKey(start)) {
				if (leftFlankLens.get(start) <= gapLen.get(start)) {
					newrefIdx += gapLen.get(start);
					etc = gapLen.get(start) + "";
				} else {
					newrefIdx += (leftFlankLens.get(start) + 10);
					etc = gapLen.get(start) + " + " + 10;
				}
				writeLine(fm, newrefIdx, start, gapEnd.get(start), "leftFlank", nType.get(start), etc);
			} else if (rightFlankLens.containsKey(start)) {
				if (rightFlankLens.get(start) <= gapLen.get(start)) {
					newrefIdx += gapLen.get(start);
					etc = gapLen.get(start) + "";
				} else {
					newrefIdx += (rightFlankLens.get(start) + 10);
					etc = 10 + " + " + rightFlankLens.get(start);
				}
				writeLine(fm, newrefIdx, start, gapEnd.get(start), "rightFlank", nType.get(start), etc);
			} else if (openLen.containsKey(start)) {
				newrefIdx += gapLen.get(start);
				etc = "";
				writeLine(fm, newrefIdx, start, gapEnd.get(start), "open", nType.get(start), etc);
			}
			oldrefIdx = gapEnd.get(start);
		}
		
	}
	
	private void writeLine(FileMaker fm, int newrefEnd, int start, int end, String type, char n, String etc) {
		fm.writeLine(newrefEnd + "\t" + start + "\t" + end + "\t" + (end - start) + "\t" + type + "\t" + n + "\t" + etc);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fillConvertToNewCoord.jar <chr_newref.fill> <out_newcoord.bed>");
		System.out.println("\t<chr_newref.fill>: output file from samPacBioGapFilling.jar");
		System.out.println("\t<out_newcoord.bed>: <chr> <newref_start> <newref_end> <oldref_start> <oldref_end> <type> <n>");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ConvertToNewCoord().go(args[0], args[1]);
		} else {
			new ConvertToNewCoord().printHelp();
		}
	}

}
