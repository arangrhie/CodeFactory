package javax.arang.bed;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class MergeByContigSimple extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String prevChr = "";
		String prevContigName = "";
		boolean isFirst = true;
		int start = -1;
		int end = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			
			if (!tokens[Bed.NOTE].equals(prevContigName) || !tokens[Bed.CHROM].equals(prevChr)) {
				if (isFirst) {
					isFirst = false;
					System.out.println("#CHR\tSTART\tEND\tCONTIG_NAME");
				} else {
					// write down
					System.out.println(prevChr + "\t" + start + "\t" + end + "\t" + prevContigName);
				}
				
				// initialize
				prevChr = tokens[Bed.CHROM];
				start = Integer.parseInt(tokens[Bed.START]);
				end = Integer.parseInt(tokens[Bed.END]);
				prevContigName = tokens[Bed.NOTE];
				continue;
			}
			
			
			prevChr = tokens[Bed.CHROM];
			start = Math.min(start, Integer.parseInt(tokens[Bed.START]));
			end = Math.max(end, Integer.parseInt(tokens[Bed.END]));
			prevContigName = tokens[Bed.NOTE];
			
			
		}
		if (!isFirst) {
			System.out.println(prevChr + "\t" + start + "\t" + end + "\t" + prevContigName);
		}
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar bedMergeByContigSimple.jar <in.bed>");
		System.err.println("\tCollapse regions comming from same contig (chromosome) per contig name.");
		System.err.println("\t<in.bed>: bed file. CHR\tSTART\tEND\tCONTIG_NAME");
		System.err.println("\t\tDoes not require to be sorted by coordinates. Sort by CONTIG_NAME.");
		System.err.println("\t<stdout>: CHR\tSTART\tEND\tCONTIG_NAME");
		System.err.println("Arang Rhie, 2018-01-10. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new MergeByContigSimple().go(args[0]);
		} else {
			new MergeByContigSimple().printHelp();
		}
	}

}
