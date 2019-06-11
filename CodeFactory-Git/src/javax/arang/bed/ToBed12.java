package javax.arang.bed;

import java.util.ArrayList;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class ToBed12 extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String prevChr = "";
		String prevName = "";
		
		String chr = "";
		double start = 0;
		double end = 0;
		String name = "";
		int score = 0;		// 0 - 1000
		String strand = ".";	// . + -
		boolean isFirst = true;
		
		double chromStart = 0;
		double chromEnd = 0;
		ArrayList<Double> blockSizes = null;
		ArrayList<Double> blockStarts = null;
		
		double nameCount = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			chr = tokens[Bed.CHROM];
			name = tokens[Bed.NOTE];
			
			if (!prevName.equals(name)) {
				if (! isFirst) {
					// print previous block
					print(prevChr, chromStart, chromEnd, prevName, score, strand, blockSizes, blockStarts);
					nameCount++;
				}
				isFirst = false;
				
				// start a new block
				start = Integer.parseInt(tokens[Bed.START]);
				end = Integer.parseInt(tokens[Bed.END]);
				chromStart = start;
				score = 0;
				blockSizes = new ArrayList<Double>();
				blockStarts = new ArrayList<Double>();
				strand = ".";
			}

			if (tokens.length > Bed.STRAND) {
				// tokens[Bed.STRAND] is either + or -
				if (!tokens[Bed.STRAND].equals(".")) {
					if (strand.equals(".")) {
						strand = tokens[Bed.STRAND];
					} else {
						// strands don't agree
						if (!strand.equals(tokens[Bed.STRAND])) {
							System.err.println("Strand disagrees : line " + line + "  : " + strand + " > " + tokens[Bed.STRAND]);
							
							// break it
							print(prevChr, chromStart, chromEnd, prevName, score, strand, blockSizes, blockStarts);
							chromStart = start;
							score = 0;
							blockSizes = new ArrayList<Double>();
							blockStarts = new ArrayList<Double>();
							strand = tokens[Bed.STRAND];
						}
						// else leave the strand as it was
					}
				} // else do nothing
					
			} else {
				strand = ".";
			}
			start = Integer.parseInt(tokens[Bed.START]);
			end = Integer.parseInt(tokens[Bed.END]);
			chromEnd = end;
			score += Integer.parseInt(tokens[Bed.MQ]);
			blockSizes.add(end - start);
			blockStarts.add(start - chromStart);		
			
			prevChr = chr;
			prevName = name;
			
		}
		if (! isFirst) {
			print(prevChr, chromStart, chromEnd, prevName, score, strand, blockSizes, blockStarts);
			nameCount++;
		}
		
		System.err.println("Finished converting to " + nameCount + " distinct blocks.");
	}
	

	private void print(String chr, double chromStart, double chromEnd, String name, int score, String strand,
			ArrayList<Double> blockSizes, ArrayList<Double> blockStarts) {
		String blockSizeList = "";
		String blockStartList = "";
		for (int i = 0; i < blockSizes.size(); i++) {
			blockSizeList += String.format("%.0f", blockSizes.get(i)) + ",";
			blockStartList += String.format("%.0f", blockStarts.get(i)) + ",";
		}
		
		System.out.println(chr + "\t" 
			+ String.format("%.0f", chromStart) + "\t"
			+ String.format("%.0f", chromEnd) + "\t"
			+ name + "\t"
			+ score + "\t"
			+ strand + "\t"
			+ String.format("%.0f", chromStart) + "\t"
			+ String.format("%.0f", chromEnd) + "\t"
			+ "0\t"	// RRR,GGG,BBB
			+ blockSizes.size() + "\t"
			+ blockSizeList.substring(0, blockSizeList.length() - 1) + "\t"
			+ blockStartList.substring(0, blockStartList.length() - 1) + "\t"
			);
	}


	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedToBed12.jar <in.bed>");
		System.out.println("Transform to a bed12 format");
		System.out.println("\t<in.bed>: Strand is optional. + | - | . if not known.");
		System.out.println("\t\tformat: CHR\tStart\tEnd\tName\tScore\tStrand");
		System.out.println("Arang Rhie, 2019-02-15. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ToBed12().go(args[0]);
		} else {
			new ToBed12().printHelp();
		}
	}

}
