package javax.arang.bed;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class MergeByDistance extends Rwrapper {
	
	private Double merged = 0d;
	private boolean isQuiet = false;
	
	public Double getMergedLen() {
		return merged;
	}

	public void setDistance(int dist) {
		distance = dist;
	}
	
	/***
	 * Don't print the output stdout
	 */
	public void setOutQuiet() {
		isQuiet = true;
	}
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		int start = 0;
		int end = 0;
		merged = 0d;
		
		String prevContig = "";
		String contig = "";
		int regionStart = -1;
		int regionEnd = -1;
		boolean isFirst = true;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			contig = tokens[Bed.CHROM];
			start = Integer.parseInt(tokens[Bed.START]);
			end = Integer.parseInt(tokens[Bed.END]);
			
			if (!prevContig.equals(contig)) {
				if (! isFirst) {
					// write down region
					if (! isQuiet) {
						System.out.println(prevContig + "\t" + regionStart + "\t" + regionEnd);
					}
					merged += (regionEnd - regionStart);

				}
				isFirst = false;
				
				// initialize
				prevContig = contig;
				regionStart = start;
				regionEnd = end;
				
				merged = 0d;
			} else {
				// same contig
				
				// is overlapping?
				if ( start - regionEnd < distance) {
					regionEnd = end;
				} else {
					if (! isQuiet ) {
						System.out.println(contig + "\t" + regionStart + "\t" + regionEnd);
					}
					merged += (regionEnd - regionStart);
					regionStart = start;
					regionEnd = end;
				}
			}
			
		}
		
		if (! isQuiet) {
			System.out.println(prevContig + "\t" + regionStart + "\t" + regionEnd);
		}
		merged += (regionEnd - regionStart);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedMergeByDistance.jar <in.bed> [distance]");
		System.out.println("\t<in.bed>: any bed formatted file");
		System.out.println("\t<stdout>: output bed formatted file, regions merged when closer than [distance]");
		System.out.println("\t[distance]: integer. DEFAULT=30");
		System.out.println("Arang Rhie, 2018-12-30. arrhie@gmail.com");
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
