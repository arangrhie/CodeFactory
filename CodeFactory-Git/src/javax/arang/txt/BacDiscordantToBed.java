package javax.arang.txt;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class BacDiscordantToBed extends IOwrapper {

	private static final int BAC_ID = 0;
	private static final int BAC_P1_CHR = 1;
	private static final int BAC_P1_START = 2;
	private static final int BAC_P1_END = 3;
	private static final int BAC_P1_MQ = 4;
	private static final int BAC_P2_CHR = 5;
	private static final int BAC_P2_START = 6;
	private static final int BAC_P2_END = 7;
	private static final int BAC_P2_MQ = 8;
	private static final int BAC_ORIENTATION = 9;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		String p1Chr;
		int p1Start = -1;
		int p1End = -1;
		
		String p2Chr;
		int p2Start = -1;
		int p2End = -1;
		
		String bacId;
		String orientation;
		
		int start;
		int end;
		
		String p1MQ = "";
		String p2MQ = "";
		
		String p1Or;	// p1 orientation
		String p2Or;	// p2 orientation
		
		String line;
		String[] tokens;
		boolean hasPair1 = true;
		boolean hasPair2 = true;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			bacId = tokens[BAC_ID];
			p1Chr = tokens[BAC_P1_CHR];
			if (p1Chr.equalsIgnoreCase(".")) {
				hasPair1 = false;
			} else {
				hasPair1 = true;
				p1Start = Integer.parseInt(tokens[BAC_P1_START]);
				p1End = Integer.parseInt(tokens[BAC_P1_END]);
				p1MQ = tokens[BAC_P1_MQ];
			}
		
			orientation = tokens[BAC_ORIENTATION];

			p2Chr = tokens[BAC_P2_CHR];
			if (p2Chr.equals(".")) {
				hasPair2 = false;
			} else {
				hasPair2 = true;
				p2Start = Integer.parseInt(tokens[BAC_P2_START]);
				p2End = Integer.parseInt(tokens[BAC_P2_END]);
				p2MQ = tokens[BAC_P2_MQ];
			}
			
			
			if (hasPair1 && hasPair2 && p1Chr.equals(p2Chr)) {
				start = Math.min(p1Start, p2Start);
				end = Math.max(p1End, p2End);
				fm.writeLine(p1Chr + "\t" + start + "\t" + end + "\t" + bacId + "\t" + p1MQ + "|" + p2MQ + "\t" + orientation);
				
			} else {
				if (hasPair1) {
					p1Or = orientation.substring(0, 1);
					fm.writeLine(p1Chr + "\t" + p1Start + "\t" + p1End + "\t" + bacId + "\t" + p1MQ + "\t" + p1Or);
				}
				if (hasPair2) {
					p2Or = orientation.substring(1, 2);
					fm.writeLine(p2Chr + "\t" + p2Start + "\t" + p2End + "\t" + bacId + "\t" + p2MQ + "\t" + p2Or);
				}
			}
			
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtBacDiscordantToBed.jar <in.discordant.bac.txt> <bac.bed>");
		System.out.println("\tConverts the BAC mapped table to BAC spanning coordinates.");
		System.out.println("\t<in.discordant.bac.txt>: BAC-ID\tBAC_P1_Chr\tBAC_P1_Start\tBAC_P1_END\tBAC_P1_MQ\tBAC_P2_Chr\tBAC_P2_Start\tBAC_P2_END\tBAC_P2_MQ\t...");
		System.out.println("\t<bac.bed>: BAC_CHR\tBAC_START\tBAC_END\tBAC_ID");
		System.out.println("\t\tBAC_START: minimum coordinate of the bac");
		System.out.println("\t\tBAC_END: maximum coordinate of the bac");
		System.out.println("Arang Rhie, 2016-06-03. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new BacDiscordantToBed().go(args[0], args[1]);
		} else {
			new BacDiscordantToBed().printHelp();
		}
	}

}
