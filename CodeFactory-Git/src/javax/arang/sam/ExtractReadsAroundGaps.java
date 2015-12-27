package javax.arang.sam;

import java.util.Vector;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.bed.util.Bed;

public class ExtractReadsAroundGaps extends I2Owrapper {

	Vector<Integer[]> start_end = new Vector<Integer[]>();
	private static String chr = "chr20";
	private static int gapStartPos = 0;
	private static int gapEndPos = 0;

	private static final int START = 0;
	private static final int END = 1;
	private static int gapIdx = 0;
	
	private void getNextGap() {
		gapStartPos = start_end.get(gapIdx)[START];
		gapEndPos = start_end.get(gapIdx)[END];
		System.out.println("[DEBUG] :: GAP " + chr + ":" + gapStartPos + "-" + gapEndPos);
		gapIdx++;
	}

	private boolean hasNextGap() {
		return (gapIdx < start_end.size());
	}

	@Override
	public void hooker(FileReader frSam, FileReader frGap, FileMaker fm) {
		String line;
		String[] tokens;
		int numReads = 0;
		
		while (frGap.hasMoreLines()) {
			line = frGap.readLine();
			if (line.startsWith("CHR"))	continue;
			tokens = line.split("\t");
			chr = tokens[Bed.CHROM];
			gapStartPos = Integer.parseInt(tokens[Bed.START]);
			gapEndPos = Integer.parseInt(tokens[Bed.END]);
			Integer[] position = new Integer[] {gapStartPos, gapEndPos};
			start_end.addElement(position);
		}
		
		System.out.println("[DEBUG] :: Number of gaps: " + start_end.size());
		
		if (hasNextGap()) {
			getNextGap();
		}
		
		while (frSam.hasMoreLines()) {
			line = frSam.readLine();
			if (line.startsWith("@"))	continue;
			tokens = line.split("\t");
			
			if (!tokens[Sam.RNAME].equals(chr))	continue;
			
			/***
			 *  GAP			|--------|
			 *  Alignment			  1|---(Sam.POS)--
			 */
			if (Integer.parseInt(tokens[Sam.POS]) > gapEndPos + 1) {
				// do the gap alignment here
				if (hasNextGap()) {
					getNextGap();
				}
			}
			
			/***
			 *   GAP			    |---------|
			 *   Alignment	------|1
			 */
			if ((Integer.parseInt(tokens[Sam.POS])
					+ Integer.parseInt(tokens[Sam.TLEN])
					+ Sam.getEndSoftclip(tokens[Sam.CIGAR])) < Math.max(1, gapStartPos - 1)) {
				continue;
			}
			
			/***
			 * Alignment				  <|-----
			 * GAP               |---------|		or
			 * Alignment   ------|>
			 */
			if ((Integer.parseInt(tokens[Sam.POS]) - Sam.getLeftSoftclippedBasesLen(tokens[Sam.CIGAR])
						<= gapEndPos + 1)
					|| (Integer.parseInt(tokens[Sam.POS])
							+ Integer.parseInt(tokens[Sam.TLEN])
							+ Sam.getEndSoftclip(tokens[Sam.CIGAR]))
							>= Math.max(1, gapStartPos - 1)) {
				fm.writeLine(line);
				numReads++;
			}
		}
		System.out.println("Total reads written: " + String.format("%,d", numReads));
	}


	@Override
	public void printHelp() {
		System.out.println("java -jar samExtractReadsAroundGaps.jar <in.sam> <in.gaps> <out.sam>");
		System.out.println("\tExtract reads from gaps specified in <in.gaps>");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new ExtractReadsAroundGaps().go(args[0], args[1], args[2]);
		} else {
			new ExtractReadsAroundGaps().printHelp();
		}
	}


}
