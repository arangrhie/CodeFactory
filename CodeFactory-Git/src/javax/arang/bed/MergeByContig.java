package javax.arang.bed;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class MergeByContig extends Rwrapper {

	private static int DISCORDANT_THRESHOLD = 1000;
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String prevChr = "";
		String prevContigName = "";
		String prevStrand = "";
		boolean isFirst = true;
		String start = "";
		String end = "";
		int prevEnd = 0;
		boolean isTooFar = false;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			if (!isFirst && tokens[Bed.CHROM].equals(prevChr)) {
				if (Integer.parseInt(tokens[Bed.START]) - prevEnd <= DISCORDANT_THRESHOLD
						&& tokens[Bed.NOTE + 2].equals(prevStrand)) {
					isTooFar = false;
				} else {
					isTooFar = true;
				}
			}
			
			if (!tokens[Bed.NOTE].equals(prevContigName) || !tokens[Bed.CHROM].equals(prevChr) || isTooFar) {
				if (isFirst) {
					isFirst = false;
					System.out.println("#CHR\tSTART\tEND\tCONTIG_NAME\tMQ\tSTRAND");
				} else {
					// write down
					System.out.println(prevChr + "\t" + start + "\t" + end + "\t" + prevContigName + "\t60\t" + prevStrand);
				}
				
				// initialize
				start = tokens[Bed.START];
				end = tokens[Bed.END];
				prevChr = tokens[Bed.CHROM];
				prevContigName = tokens[Bed.NOTE];
				prevStrand = tokens[Bed.NOTE + 2];
				prevEnd = Integer.parseInt(end);
				isTooFar = false;
				continue;
			}
			
			
			prevChr = tokens[Bed.CHROM];
			prevContigName = tokens[Bed.NOTE];
			prevStrand = tokens[Bed.NOTE + 2];
			end = tokens[Bed.END];
			prevEnd = Integer.parseInt(end);
			
		}
		System.out.println(prevChr + "\t" + start + "\t" + end + "\t" + prevContigName + "\t60\t" + prevStrand);
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar bedMergeByContigPerReadName.jar <in.bed> [DISCORDANT_THRESHOLD]");
		System.err.println("\tCollapse regions comming from same contig (chromosome) per contig name.");
		System.err.println("\t<in.bed>: sorted bed file. CHR\tSTART\tEND\tCONTIG_NAME\tMQ\tSTRAND");
		System.err.println("\t<stdout>: CHR\tSTART\tEND\tCONTIG_NAME\tMQ\tSTRAND");
		System.err.println("\t[DISCORDANT_THRESHOLD]: DEFAULT = 1000. when blocks are apart > DISCORDANT_THRESHOLD bp, these blocks will not be merged.");
		System.err.println("Arang Rhie, 2017-08-14. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new MergeByContig().go(args[0]);
		} else if (args.length == 2) {
			DISCORDANT_THRESHOLD = Integer.parseInt(args[1]);
			new MergeByContig().go(args[0]);
		} else {
			new MergeByContig().printHelp();
		}
	}

}
