package javax.arang.samtools.depth;

import java.util.ArrayList;
import java.util.Collections;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;
import math.Stat;

public class SimpleStatByRegion extends R2wrapper {

	@Override
	public void hooker(FileReader frDepth, FileReader frBed) {
		String line;
		String[] tokens;
		
		System.err.println("Start parsing the region bed file");
		Bed regionOfInterest = new Bed(frBed);

		String chr;
		int pos;
		int closestStart;
		String prevChr = "";
		int prevRegionStart = -1;
		
		double depth;
		
		double depthMin = Double.MAX_VALUE;
		double depthMax = -1;
		double depthSum = 0;
		float depthAvg;
		double depthSD;
		double depthMedian;
		ArrayList<Double> depthValuesInRegion = new ArrayList<Double>();
		int n;
		
		while (frDepth.hasMoreLines()) {
			line = frDepth.readLine();
			tokens = line.split(RegExp.TAB);
			chr = tokens[Depth.CHR];
			pos = Integer.parseInt(tokens[Depth.POS]);
			if (regionOfInterest.isInRegion(chr, pos)) {
				closestStart = regionOfInterest.getClosestStart(chr, pos);
				if (prevRegionStart == -1) {
					prevChr = chr;
					prevRegionStart = closestStart;
				} else if (!prevChr.equals(chr) || prevRegionStart < closestStart) {
					// Print region and stat values: MIN	MAX	AVG.	SD	MEDIAN	N
					n = depthValuesInRegion.size();
					Collections.sort(depthValuesInRegion);
					depthAvg = (float) depthSum / n;
					if (n==1) {
						depthMedian = depthValuesInRegion.get(0);
					} else {
						depthMedian = depthValuesInRegion.get(n/2 - 1);
					}
					depthSD = Stat.getSD(depthValuesInRegion, depthAvg);
					System.out.println(regionOfInterest.getLine(prevChr, regionOfInterest.getStarts(prevChr).indexOf(prevRegionStart))
							+ "\t" + depthMin + "\t" + depthMax + "\t" + depthAvg + "\t" + depthSD + "\t" + depthMedian + "\t" + n);
					
					// Initialize variables
					prevChr = chr;
					prevRegionStart = closestStart;
					depthValuesInRegion.clear();
					depthMin = Integer.MAX_VALUE;
					depthMax = -1;
					depthSum = 0;
				}
				
				// Collect depth values
				depth = Integer.parseInt(tokens[Depth.DEPTH_OF_SAMPLE + SAMPLE_IDX]);
				if (depth < depthMin) {
					depthMin = depth;
				}
				if (depthMax < depth) {
					depthMax = depth;
				}
				depthSum += depth;
				depthValuesInRegion.add(depth);
			}
		}
		
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samtoolsDepthSimpleStatByRegion.jar <in.depth> <in.bed> [sampleIdx=1]");
		System.out.println("\t<in.depth>: Generated with samtools depth. FORMAT: CHR\tPOS\tDEPTH_OF_SAMPLE_1\t...\tDEPTH_OF_SAMPLE_N");
		System.out.println("\t<in.bed>: Region of interest in sorted, non-overlapping bed format");
		System.out.println("\t[sampleIdx]: Sample index from left. 1-based.");
		System.out.println("\t\t(e.g. the 3rd column is the 1st sample, so sampleIdx for the first sample is 1.)");
		System.out.println("\t<stdout>: At the end of <in.bed>, columns will be added as following:");
		System.out.println("\t\t(original columns in <in.bed>)\tMIN\tMAX\tAVG.\tSD\tMEDIAN\tN");
		System.out.println("Arang Rhie, 2016-11-25. arrhie@gmail.com");
	}

	private static int SAMPLE_IDX = 0;
	public static void main(String[] args) {
		if (args.length >= 2) {
			if (args.length == 3) {
				SAMPLE_IDX = Integer.parseInt(args[2]) - 1;
			}
			new SimpleStatByRegion().go(args[0], args[1]);
		} else {
			new SimpleStatByRegion().printHelp();
		}
	}

}
