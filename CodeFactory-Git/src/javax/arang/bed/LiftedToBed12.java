package javax.arang.bed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class LiftedToBed12 extends Rwrapper {

	private static int TARGET_NAME = 3;
	private static int TARGET_STRAND = 4;
	private static int TARGET_FEATURE = 5;
	private static int liftedCoordIdx = 6;
	private static int LIFTED_CHR;
	private static int LIFTED_START;
	private static int LIFTED_END;
	private static int LIFTED_STRAND;
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
	
		String newChrom = "";
		int start = 0;	// what we read in
		int end = 0;		// what we read in
		
		String targetStrand = "";
		String liftedStrand = "";
		String newGeneName = "";
		String targetFeature;
		
		String chrom = "";
		int chromStart = 0;	// Bed12 feature
		int chromEnd = 0;	// Bed12 feature
		String geneName = "";
		String strand = "+";
		int thickStart = 0;
		int thickEnd = 0;
		String itemRgb = "0";
		int blockCount = 0;
		HashMap<Integer, Integer> blockSizes = null;
		ArrayList<Integer> blockStarts = null;
		String sizes = "";
		String starts = "";
		
		initialize();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			if (!(tokens.length > LIFTED_STRAND)) {
				continue;
			}
			newGeneName = tokens[TARGET_NAME];
			newChrom = tokens[LIFTED_CHR];
			if (!chrom.equals(newChrom) || !geneName.equals(newGeneName)) {
				if (!chrom.equals("")) {
					// New gene started here, so print what we had before
					starts = getStarts(blockStarts, chromStart);
					sizes = getSizes(blockSizes, blockStarts);
					System.out.println(chrom + "\t" + chromStart + "\t" + chromEnd
							+ "\t" + geneName + "\t0\t" + strand
							+ "\t" + thickStart + "\t" + thickEnd
							+ "\t" + itemRgb 
							+ "\t" + blockCount + "\t" + sizes + "\t" + starts);
				}
				
				// initialize
				blockSizes = new HashMap<Integer, Integer>();
				blockStarts = new ArrayList<Integer>();
				chrom = newChrom;
				geneName = newGeneName;
				chromStart = Integer.MAX_VALUE;
				chromEnd = 0;
				thickStart = Integer.MAX_VALUE;
				thickEnd = 0;
				blockCount = 0;
			}

			targetFeature = tokens[TARGET_FEATURE];
			start = Integer.parseInt(tokens[LIFTED_START]);
			end = Integer.parseInt(tokens[LIFTED_END]);
			
			if (targetFeature.equals("gene")) {
				// if feature = gene set the chromStart and chromEnd
				if (chromStart > start) {
					chromStart = start;
					thickStart = start;
					thickEnd = start;
				}
				if (chromEnd < end) {
					chromEnd = end;
				}
			} else if (targetFeature.equals("mRNA")) {
				// if feature = exon
				if (thickStart > start) {
					thickStart = start;
				}
				if (thickEnd < end) {
					thickEnd = end;
				}
			} else if (targetFeature.equals("exon")) {
				// Add to exon blocks if not already exist
				if (!blockStarts.contains(start)) {
					blockStarts.add(start);
					blockSizes.put(start, (end - start));
					blockCount++;
				}
			}

			targetStrand = tokens[TARGET_STRAND];
			liftedStrand = tokens[LIFTED_STRAND];
			if (targetStrand.equals(liftedStrand)) {
				strand = "+";
			} else {
				strand = "-";
			}
		}
		
		if (!chrom.equals("")) {
			// New gene started here, so print what we had before
			starts = getStarts(blockStarts, chromStart);
			sizes = getSizes(blockSizes, blockStarts);
			System.out.println(chrom + "\t" + chromStart + "\t" + chromEnd
					+ "\t" + geneName + "\t0\t" + strand
					+ "\t" + thickStart + "\t" + thickEnd
					+ "\t" + itemRgb 
					+ "\t" + blockCount + "\t" + sizes + "\t" + starts);
		}
	}
	
	private void initialize() {
		LIFTED_CHR = liftedCoordIdx;
		LIFTED_START = liftedCoordIdx + 1;
		LIFTED_END = liftedCoordIdx + 2;
		LIFTED_STRAND = liftedCoordIdx + 3;
	}
	
	private String getStarts(ArrayList<Integer> blockStarts, int chromStart) {
		String starts = "";
		if (blockStarts.size() == 1) {
			starts = (blockStarts.get(0) - chromStart) + "";
		} else if (blockStarts.size() > 1) {
			Collections.sort(blockStarts);
			for (int i = 0; i < blockStarts.size(); i++) {
				starts += (blockStarts.get(i) - chromStart) + ",";
			}
		}
		return starts;
	}
	
	private String getSizes(HashMap<Integer, Integer> blockSiezs, ArrayList<Integer> blockStarts) {
		String sizes = "";
		for (int i = 0; i < blockStarts.size(); i++) {
			sizes += blockSiezs.get(blockStarts.get(i)) + ",";
			
		}
		return sizes;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedLiftedToBed12.jar <in.lifted.bed> [lifted_coord_col_idx=7]");
		System.out.println("Convert lifted gene coordinates to bed12 format for visual representation");
		System.out.println("\t<in.lifted.bed>: output file of nucmerLiftOver.jar");
		System.out.println("\t<stdout>: bed12 format, 1 gene in 1 line");
		System.out.println("\t[lifted_coord_col_idx]: Lifted coordinate column idx. 1-based. DEFAULT=7");
		System.out.println("*This script ignors the missing exons and treats the truncated gene boundaries as the attribute boudaries.");
		System.out.println(" thickStart and thickEnd are based on mRNA boundaries.");
		System.out.println("Arang Rhie, 2019-01-07. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			liftedCoordIdx = Integer.parseInt(args[1]) - 1;
			new LiftedToBed12().go(args[0]);
		}
		else if (args.length == 1) {
			new LiftedToBed12().go(args[0]);
		} else {
			new LiftedToBed12().printHelp();
		}
	}

}
