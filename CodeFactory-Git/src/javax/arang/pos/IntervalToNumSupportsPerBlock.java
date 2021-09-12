package javax.arang.pos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;
import math.Stat;

public class IntervalToNumSupportsPerBlock extends R2wrapper {

	private static String asm = "asm";
	
	public static void main(String[] args) {
		if (args.length == 3) {
			asm = args[0];
			new IntervalToNumSupportsPerBlock().go(args[1], args[2]);
		} else {
			new IntervalToNumSupportsPerBlock().printHelp();
		}
	}

	@Override
	public void hooker(FileReader frIntervals, FileReader frBlocks) {
		
		String line;
		String[] tokens;
		
		Bed blocks = new Bed();
		blocks.parseBed(frBlocks);
		
		System.err.println("[DEBUG] :: Num. blocks read from " + frBlocks.getFileName() + " : " + blocks.getNumRegions(asm));
	
		String seqId;
		String asmFrom;
		String asmTo;
		String posInAsm;
		int blockStart;	// integer type of start position from a block
		int blockEnd;	// integer type of end position from a block
		int intervalFrom;
		int intervalTo;
		
		int intervalAsm;
		int intervalSeq;
		
		ArrayList<String> intervalPosInAsm = new ArrayList<String>();	// Intervals are stored as asmFrom_asmTo
		HashMap<String, ArrayList<String>> intervalPosToSeqIDdistPairs = new HashMap<String, ArrayList<String>>();
		HashMap<String, Integer> intervalPosToAsmDistance = new HashMap<String, Integer>();
		ArrayList<String> distPairs = new ArrayList<String>();	// distance-pair are stored as seqId:intervalSeq
		
		/***
		 * Input: seqId	asmFrom	asmTo	intervalAsm	intervalSeq
		 */
		
		while (frIntervals.hasMoreLines()) {
			line = frIntervals.readLine();
			tokens = line.split(RegExp.TAB);
			
			seqId = tokens[0];
			asmFrom = tokens[1];
			asmTo = tokens[2];
			intervalAsm = Integer.parseInt(tokens[3]);
			intervalSeq = Integer.parseInt(tokens[4]);
			
			// get block covering asmFrom and asmTo
			blockStart = blocks.getClosestStart(asm, Integer.parseInt(asmFrom));
			intervalFrom = blocks.getEndFromStart(asm, blockStart);
			
			intervalTo =  blocks.getClosestStart(asm, Integer.parseInt(asmTo));
			blockEnd = blocks.getEndFromStart(asm, intervalTo);
			
			// exclude 'within a block' intervals
			if (blockStart == intervalTo) {
				continue;
			}
			
			posInAsm = blockStart + ":" + intervalFrom + "_" + intervalTo + ":" + blockEnd;
			if (!intervalPosInAsm.contains(posInAsm)) {
				intervalPosInAsm.add(posInAsm);
				intervalPosToAsmDistance.put(posInAsm, intervalAsm);
				distPairs = new ArrayList<String>();
				intervalPosToSeqIDdistPairs.put(posInAsm, distPairs);
			} else {
				distPairs = intervalPosToSeqIDdistPairs.get(posInAsm);
			}
			distPairs.add(seqId + ":" + intervalSeq);
			
		}
		System.err.println("[DEBUG] :: Unique intervals = " + intervalPosInAsm.size());
		
		Collections.sort(intervalPosInAsm);
		
		int numDistPairs;
		Double dist;
		Double distSum;
		String distPair;
		Double distAvg;
		ArrayList<Double> distances;
		Double standardDeviation;
		
		// For each intervals
		for (int i = 0; i < intervalPosInAsm.size(); i++) {
			posInAsm = intervalPosInAsm.get(i);
			distPairs = intervalPosToSeqIDdistPairs.get(posInAsm);
			
			// The distance seen in the asm
			tokens = posInAsm.split("_");
			asmFrom = tokens[0];
			asmTo = tokens[1];
			intervalAsm = intervalPosToAsmDistance.get(posInAsm);
			
			// Let's get the avg. of the distance seen in the sequence
			
			// initialize variables...
			numDistPairs = distPairs.size();
			distSum = 0d;
			distances = new ArrayList<Double>();
			// for each distPair, retrieve the distance and get the sum
			for (int j = 0; j < numDistPairs; j++) {
				distPair = distPairs.get(j);
				dist = Double.parseDouble(distPairs.get(j).substring(distPair.indexOf(":") + 1));
				distances.add(dist);
				distSum += dist;
			}
						
			// this is the average
			distAvg = distSum / numDistPairs;
			
			standardDeviation = Stat.getSD(distances, distAvg);
			
			// print output
			System.out.println(asm + "\t" + asmFrom + "\t" + asmTo + "\t" 
					+ numDistPairs + "\t" + intervalAsm + "\t" + String.format("%.2f", distAvg) + "\t" + String.format("%.2f", standardDeviation) + "\t"
					+ distPairs.toString());
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar posIntervalToNumSupportsPerBlock.jar <asm> <in.intervals> <in.blocks>");
		System.out.println("\t<asm>: assembly name (chromosome). Note this code assumes we have only 1 asm.");
		System.out.println("\t<in.intervals>: generated with posToInterval.jar\n"
				+ "\tseqID\tposAsmFrom\tposAsmTo\tintervalInAsm\tIntervalInSeq");
		System.out.println("\t<in.blocks>: bed formatted block on asm coordinate");
		System.out.println("\t<stdout>: Similar to posIntervalToNumSupports.jar, this time each posAsm being the block coordinates.\n"
				+ "\t\tasm\tposAsmFrom\tposAsmTo\tNum.ReadsSupporting\tintervalInAsm\tintervalAvg.InReads\tSD\tinterval1:interval2:..:intervalN\treadId1:readId2:..:readIdN");
		System.out.println("Arang Rhie, 2019-03-04. arrhie@gmail.com");
		
	}

}
