package javax.arang.bed;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class MergeByDistance extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
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
					fm.writeLine(contig + "\t" + regionStart + "\t" + regionEnd);
				} else {
					fm.writeLine(contig + "\t" + prevStart + "\t" + prevEnd);
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
						fm.writeLine(contig + "\t" + regionStart + "\t" + regionEnd);
					} else {
						fm.writeLine(contig + "\t" + prevStart + "\t" + prevEnd);
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
			fm.writeLine(contig + "\t" + regionStart + "\t" + regionEnd);
		} else {
			fm.writeLine(contig + "\t" + prevStart + "\t" + prevEnd);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedMergeByDistance.jar <in.bed> <out.bed> [distance]");
		System.out.println("\t<in.bed>: any bed formatted file");
		System.out.println("\t<out.bed>: output file merged when regions are closer than [distance]");
		System.out.println("\t[distance]: integer. DEFAULT=30");
		System.out.println("Arang Rhie, 2015-12-17. arrhie@gmail.com");
	}

	private static int distance = 30;
	public static void main(String[] args) {
		if (args.length == 3) {
			distance = Integer.parseInt(args[2]);
			new MergeByDistance().go(args[0], args[1]);
		} else if (args.length == 2) {
			new MergeByDistance().go(args[0], args[1]);
		} else {
			new MergeByDistance().printHelp();
		}
	}

}
