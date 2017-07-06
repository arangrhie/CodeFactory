package javax.arang.bed;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class ToDistance extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String prevReadId = "";
		String readId;
		String prevContig = "";
		String contig;
		int mq;
		
		int distStart = 0;
		int distEnd = 0;
		int start;
		int end;
		boolean hasDist = false;
		
		// for debugging
		int countReads = 0;
		int countDist = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			mq = Integer.parseInt(tokens[Bed.MQ]);
			if ( mq < mqFilter ) {
				continue;
			}
			readId = tokens[Bed.NOTE];
			if (readId.endsWith("/1") || readId.endsWith("/2")) {
				readId = readId.split("/")[0];
			}
			contig = tokens[Bed.CHROM];
			start = Integer.parseInt(tokens[Bed.START]);
			end = Integer.parseInt(tokens[Bed.END]);

			if (prevReadId.equals(readId) && prevContig.equals(contig)) {
				// same readId & contig
				distStart = Math.min(distStart, start);
				distEnd = Math.max(distEnd, end);
				hasDist = true;
			} else {
				if (hasDist) {
					System.out.println((distEnd - distStart));
					countDist++;
				}
				hasDist = false;
				distStart = start;
				distEnd = end;
			}
			
			// for debugging
			if (!prevContig.equals(contig)) {
				countReads++;
			}

			prevReadId = readId;
			prevContig = contig;
			
		}
		
		if (hasDist) {
			System.out.println((distEnd - distStart));
			countDist++;
		}
		
		System.err.println("Total reads processed: " + countReads);
		System.err.println("Total written distances: " + countDist);
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar bedToDistance.jar <in.bed> [MQfilter] > distance.list");
		System.err.println("Get distance of any read pairs on a contig, including split read alignments");
		System.err.println("If multiple alignment exists on the same contig, the farest pair is choosen and reported.");
		System.err.println("Orphan reads (reads with pair not mapped on the same contig) will not be counted.");
		System.err.println("\t<in.bed>: generated with bedtools bamtobed -split");
		System.err.println("\t[MQfilter]: Checking for the 5th column to be >= MQfilter. DEFAULT=5");
		System.err.println("Arang Rhie, 2017-04-23. arrhie@gmail.com");
	}
	
	private static int mqFilter = 5;
	public static void main(String[] args) {
		if (args.length == 1) {
			new ToDistance().go(args[0]);
		} else if (args.length == 2) {
			mqFilter = Integer.parseInt(args[1]);
			new ToDistance().go(args[0]);
		} else {
			new ToDistance().printHelp();
		}
	}

}
