package javax.arang.bed;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class MergeByContigPerReadName extends IOwrapper {

	private static int DISCORDANT_THRESHOLD = 1000;
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String prevChr = "";
		String prevReadName = "";
		boolean isFirst = true;
		String start = "";
		String end = "";
		int prevEnd = 0;
		int len = 0;
		int readCount = 0;
//		float depthCount = 0;
//		int numMergedRegions = 0;
//		String chrLen = "";
		boolean isTooFar = false;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			if (!isFirst && tokens[Bed.CHROM].equals(prevChr)) {
				if (Integer.parseInt(tokens[Bed.START]) - prevEnd > DISCORDANT_THRESHOLD) {
					isTooFar = true;
				} else {
					isTooFar = false;
				}
			}
			
			if (!tokens[Bed.NOTE].equals(prevReadName) || !tokens[Bed.CHROM].equals(prevChr) || isTooFar) {
				if (isFirst) {
					isFirst = false;
					fm.writeLine("#CHR\tSTART\tEND\tREADNAME\tRANGE\tCOVERED_RANGE\tNUM_READS");
				} else {
					// write down
					int rangeSum = Integer.parseInt(end) - Integer.parseInt(start);
					fm.writeLine(prevChr + "\t" + start + "\t" + end + "\t" + prevReadName
							+ "\t" + rangeSum
							+ "\t" + len
							+ "\t" + readCount);
							//+ "\t" + String.format("%.3f", ((float) depthCount / numMergedRegions))
							//+ "\t" + chrLen);
				}
				
				// initialize
				start = tokens[Bed.START];
				end = tokens[Bed.END];
				len = Integer.parseInt(tokens[Bed.NOTE + 1]);
				readCount = Integer.parseInt(tokens[Bed.NOTE + 2]);
				prevChr = tokens[Bed.CHROM];
				prevReadName = tokens[Bed.NOTE];
				prevEnd = Integer.parseInt(end);
				//depthCount = 0;
				//numMergedRegions = 0;
				//chrLen = tokens[Bed.NOTE + 4];
				isTooFar = false;
				continue;
			}
			
			
			if (Integer.parseInt(tokens[Bed.START]) < prevEnd) {
				len += Integer.parseInt(tokens[Bed.NOTE + 1]) - (prevEnd - Integer.parseInt(tokens[Bed.START]));
			} else {
				len += Integer.parseInt(tokens[Bed.NOTE + 1]);
			}
			prevChr = tokens[Bed.CHROM];
			prevReadName = tokens[Bed.NOTE];
			end = tokens[Bed.END];
			readCount += Integer.parseInt(tokens[Bed.NOTE + 2]);
			//depthCount += Float.parseFloat(tokens[Bed.NOTE + 3]);
			//numMergedRegions++;
			prevEnd = Integer.parseInt(end);
			
		}
		int rangeSum = Integer.parseInt(end) - Integer.parseInt(start);
		fm.writeLine(prevChr + "\t" + start + "\t" + end + "\t" + prevReadName
				+ "\t" + rangeSum
				+ "\t" + len
				+ "\t" + readCount);
				//+ "\t" + String.format("%.3f", ((float) depthCount / numMergedRegions))
				//+ "\t" + chrLen);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedMergeByContigPerReadName.jar <in.bed> <out.merged.bed> [DISCORDANT_THRESHOLD]");
		System.out.println("\tCollapse regions comming from same contig (chromosome) per read name.");
		System.out.println("\tApply some filters before applying this code, to make sure only wanted regions are taken.");
		System.out.println("\t<in.bed>: sorted bed file. CHR\tSTART\tEND\tREADNAME\tLEN\tNUM_READS");
		System.out.println("\t<out.bed>: CHR\tSTART\tEND\tREADNAME\tRANGE\tCOVERED_RANGE\tNUM_READS");
		System.out.println("\t[DISCORDANT_THRESHOLD]: DEFAULT = 1000. when blocks are apart > DISCORDANT_THRESHOLD bp, these blocks will not be merged.");
		System.out.println("Arang Rhie, 2016-11-09. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new MergeByContigPerReadName().go(args[0], args[1]);
		} else if (args.length == 3) {
			DISCORDANT_THRESHOLD = Integer.parseInt(args[2]);
			new MergeByContigPerReadName().go(args[0], args[1]);
		} else {
			new MergeByContigPerReadName().printHelp();
		}
	}

}
