package javax.arang.bed;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

@Deprecated
public class ReduceTwoPooled extends IOwrapper {

	static final short CHR_1 = 0;
	static final short START_1 = 1;
	static final short END_1 = 2;
	static final short NUM_READS_1 = 3;
	static final short LEN_1 = 4;
	static final short POOL_FRAGMENT_ID_1=5;
	static final short CHR_2 = 6;
	static final short START_2 = 7;
	static final short END_2 = 8;
	static final short NUM_READS_2 = 9;
	static final short LEN_2 = 10;
	static final short POOL_FRAGMENT_ID_2=11;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		String nextLine;
		String[] nextTokens;

		line = fr.readLine();
		tokens = line.split(RegExp.TAB);
		String prevPoolID = tokens[POOL_FRAGMENT_ID_1];
		int len1 = Integer.parseInt(tokens[LEN_1]);
		int len2 = Integer.parseInt(tokens[LEN_2]);
		if (len1 < len2) {
			prevPoolID = tokens[POOL_FRAGMENT_ID_2];
		}
		int newStart = min(tokens[START_1], tokens[START_2]);
		int newEnd = max(tokens[END_1], tokens[END_2]);
		
		int numReads;
		numReads = Integer.parseInt(tokens[NUM_READS_1]) + Integer.parseInt(tokens[NUM_READS_2]);
		
		String poolID = "";
		String chr = tokens[CHR_1];
		while (fr.hasMoreLines()) {
			nextLine = fr.readLine();
			nextTokens = line.split(RegExp.TAB);
			
			int start = min(nextTokens[START_1], nextTokens[START_2]);
			int end = max(nextTokens[END_1], nextTokens[END_2]);
			
			if (tokens[POOL_FRAGMENT_ID_1].equals(nextTokens[POOL_FRAGMENT_ID_1])) {
				poolID = tokens[POOL_FRAGMENT_ID_1];
				numReads += Integer.parseInt(nextTokens[NUM_READS_2]);
				newStart = Math.min(newStart, start);
				newEnd = Math.min(newEnd, end);
			} else if (tokens[POOL_FRAGMENT_ID_2].equals(nextTokens[POOL_FRAGMENT_ID_2])) {
				poolID = tokens[POOL_FRAGMENT_ID_2];
				numReads += Integer.parseInt(nextTokens[NUM_READS_1]);
				newStart = Math.min(newStart, start);
				newEnd = Math.min(newEnd, end);
			} else {
				String poolH = tokens[POOL_FRAGMENT_ID_1].substring(0, tokens[POOL_FRAGMENT_ID_1].indexOf("_"));
				String poolV = tokens[POOL_FRAGMENT_ID_2].substring(0, tokens[POOL_FRAGMENT_ID_2].indexOf("_"));
				fm.writeLine(chr + "\t" + newStart + "\t" + newEnd + "\t" + numReads + "\t" + (newEnd - newStart) + "\t" + prevPoolID
						+ "\t" + poolH + "\t" + poolV + "\t" + poolH + "_" + poolV);
				len1 = Integer.parseInt(nextTokens[LEN_1]);
				len2 = Integer.parseInt(nextTokens[LEN_2]);
				poolID = nextTokens[POOL_FRAGMENT_ID_1];
				if (len1 < len2) {
					poolID = nextTokens[POOL_FRAGMENT_ID_2];
				}
				chr = nextTokens[CHR_1];
				numReads = Integer.parseInt(nextTokens[NUM_READS_1]) + Integer.parseInt(nextTokens[NUM_READS_2]);
				newStart = start;
				newEnd = end;
			}

			line = nextLine;
			tokens = nextTokens;
			prevPoolID = poolID;
		}
		
		String poolH = tokens[POOL_FRAGMENT_ID_1].substring(0, tokens[POOL_FRAGMENT_ID_1].indexOf("_"));
		String poolV = tokens[POOL_FRAGMENT_ID_2].substring(0, tokens[POOL_FRAGMENT_ID_2].indexOf("_"));
		fm.writeLine(chr + "\t" + newStart + "\t" + newEnd + "\t" + numReads + "\t" + (newEnd - newStart) + "\t" + prevPoolID
				+ "\t" + poolH + "\t" + poolV + "\t" + poolH + "_" + poolV);
	}
	

	private int max(String end_1, String end_2) {
		int end1 = Integer.parseInt(end_1);
		int end2 = Integer.parseInt(end_2);
		if (end1 > end2)	return end1;
		else return end2;
	}

	private int min(String start_1, String start_2) {
		int start1 = Integer.parseInt(start_1);
		int start2 = Integer.parseInt(start_2);
		if (start1 < start2)	return start1;
		else	return start2;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar <poolN.bed> <out.bed>");
		System.out.println("\t<poolN.bed>: Output generated with bedtools intersect -a poolN -b poolA-Z -wo >> poolN");
		System.out.println("\t<out.bed>: Reduced with merging overlapping intervals");
		System.out.println("\t\tFormat: CHR\tSTART\tEND\tMERGED_FRAGMENTS\tLEN\tPOOL_FRAG_ID\tPOOL_H\tPOOL_V\tH_V");
		System.out.println("Arang Rhie, 2015-05-11. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ReduceTwoPooled().go(args[0], args[1]);
		} else {
			new ReduceTwoPooled().printHelp();
		}
	}

}
