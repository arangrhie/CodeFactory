package javax.arang.bed;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class MergeByDistance extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		int prevStart = 0;
		int prevEnd = 0;
		int start = 0;
		int end = 0;
		
		String prevContig = "";
		String contig = "";
		int regionStart = -1;
		int regionEnd = -1;
		boolean isFirst = true;
		boolean hasMergedRegion = false;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			contig = tokens[Bed.CHROM];
			start = Integer.parseInt(tokens[Bed.START]);
			end = Integer.parseInt(tokens[Bed.END]);
			
			if (isFirst) {
				prevContig = contig;
				prevStart = start;
				prevEnd = end;	
				isFirst = false;
				continue;
			}
			
			if (!prevContig.equals(contig)) {
				if (hasMergedRegion) {
					// write down regions
					System.out.println(contig + "\t" + regionStart + "\t" + regionEnd);
				} else {
					System.out.println(contig + "\t" + prevStart + "\t" + prevEnd);
				}
				hasMergedRegion = false;
			} else {
				if (start - prevEnd < distance) {
					if (hasMergedRegion) {
						regionEnd = end;	
					} else {
						hasMergedRegion = true;
						regionStart = prevStart;
						regionEnd = end;
					}
				} else {
					if (hasMergedRegion) {
						// write down regions
						System.out.println(contig + "\t" + regionStart + "\t" + regionEnd);
					} else {
						System.out.println(contig + "\t" + prevStart + "\t" + prevEnd);
					}
					hasMergedRegion = false;
				}
			}
			
			prevContig = contig;
			prevStart = start;
			prevEnd = end;
		}
		
		if (!isFirst && hasMergedRegion) {
			// write down regions
			System.out.println(contig + "\t" + regionStart + "\t" + regionEnd);
		} else {
			System.out.println(contig + "\t" + prevStart + "\t" + prevEnd);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedMergeByDistance.jar <in.bed> [distance]");
		System.out.println("\t<in.bed>: any bed formatted file");
		System.out.println("\t<stdout>: output bed formatted file, regions merged when closer than [distance]");
		System.out.println("\t[distance]: integer. DEFAULT=30");
		System.out.println("Arang Rhie, 2018-05-01. arrhie@gmail.com");
	}

	private static int distance = 30;
	public static void main(String[] args) {
		if (args.length == 2) {
			distance = Integer.parseInt(args[1]);
			new MergeByDistance().go(args[0]);
		} else if (args.length == 1) {
			new MergeByDistance().go(args[0]);
		} else {
			new MergeByDistance().printHelp();
		}
	}

}
