package javax.arang.blast;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class FilterGapFlanked extends IOwrapper {
	
	private static final int Q_CHR = 0;
	private static final int Q_START = 1;
	private static final int Q_END = 2;
	private static final int Q_GAP_LEN = 3;
	private static final int Q_NEW_REF_LEN = 6;
	
	private static final int QUERY_ID = 0;
	private static final int QUERY_CHR = 1;
	private static final int QUERY_START = 6;
	private static final int QUERY_END = 7;
	private static final int BLAST_START = 8;
	private static final int BLAST_END = 9;
	

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		String[] queryTokens;
		String chr;
		int start;
		int end;
		int blastStart;
		int blastEnd;
		int qStart;
		int qEnd;
		int gapLen;
		int newrefLen;
		int flankOffset;
		int numResult = 0;
		
		String prevQID = "";
		String direction = "";
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split("\\s+");
			queryTokens = tokens[QUERY_ID].split("[:.]+");
			
			chr = queryTokens[Q_CHR];
			if (!chr.equals(tokens[QUERY_CHR]))	continue;
			gapLen = Integer.parseInt(queryTokens[Q_GAP_LEN]);
			newrefLen = Integer.parseInt(queryTokens[Q_NEW_REF_LEN]);
			if (Math.abs(newrefLen - gapLen) < 100)	continue;
			
			start = Integer.parseInt(queryTokens[Q_START]);
			end = Integer.parseInt(queryTokens[Q_END]);
			blastStart = Integer.parseInt(tokens[BLAST_START]);
			blastEnd = Integer.parseInt(tokens[BLAST_END]);
			
			if (start - 1 - flank_pad < blastEnd && blastEnd < start) {
				qEnd = Integer.parseInt(tokens[QUERY_END]);
				flankOffset = start - blastEnd;
				if (newrefLen - qEnd > flankOffset) {
					if (prevQID.equals(tokens[QUERY_ID]) && direction.equals("ExtractRight"))	continue;
					fm.writeLine(line + "\t" + "ExtractRight");
					numResult++;
					prevQID = tokens[QUERY_ID];
					direction = "ExtractRight";
				}
			} else if( end < blastStart && blastStart < end + 1 + flank_pad) {
				qStart = Integer.parseInt(tokens[QUERY_START]);
				flankOffset = blastStart - end;
				if (qStart > flankOffset) {
					if (prevQID.equals(tokens[QUERY_ID]) && direction.equals("ExtractLeft"))	continue;
					fm.writeLine(line + "\t" + "ExtractLeft");
					numResult++;
					prevQID = tokens[QUERY_ID];
					direction = "ExtractLeft";
				}
			}
			
			
		}
		System.out.println("Number of results left after applying filtering: " + numResult);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar blastFilterGapFlanked.jar <in.blast.out> <out> [flank_pad=1 (flanked next to it)]");
		System.out.println("\t<in.blast.out> : blast search output generated with -outfmt 7");
		System.out.println("\t[flank_pad] : [DEFUALT=1] (start - 1 - flank_pad < blastEnd && blastEnd < start) || ( end < blastStart && blastStart < end + 1 + flank_pad) will be written to <out>");
		System.out.println("\tFilter results showing the flanked region to the target gap");
		System.out.println("\tQuery name should be formatted as chr:start..end");
	}

	static int flank_pad = 1;
	public static void main(String[] args) {
		if (args.length == 2) {
			new FilterGapFlanked().go(args[0], args[1]);
		} else if (args.length == 3) {
			flank_pad = Integer.parseInt(args[2]);
			new FilterGapFlanked().go(args[0], args[1]);
		} else {
			new FilterGapFlanked().printHelp();
		}
	}

}
