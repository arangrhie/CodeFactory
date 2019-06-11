package javax.arang.pos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.math.Stat;

public class IntervalToNumSupports extends Rwrapper {

	private static String asm = "asm";
	
	@Override
	public void hooker(FileReader fr) {
		
		String line;
		String[] tokens;
		
		String seqId;
		String asmFrom;
		String asmTo;
		String posInAsm;
		
		int intervalAsm;
		int intervalSeq;
		
		ArrayList<String> intervalPosInAsm = new ArrayList<String>();	// Intervals are stored as asmFrom_asmTo
		HashMap<String, ArrayList<String>> intervalPosToSeqIDdistPairs = new HashMap<String, ArrayList<String>>();
		ArrayList<String> distPairs = new ArrayList<String>();	// distance-pair are stored as seqId:intervalSeq
		
		/***
		 * Input: seqId	asmFrom	asmTo	intervalAsm	intervalSeq
		 */
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			seqId = tokens[Pos.SEQ];
			asmFrom = tokens[1];
			asmTo = tokens[2];
			intervalSeq = Integer.parseInt(tokens[4]);
			
			posInAsm = asmFrom + "_" + asmTo;
			if (!intervalPosInAsm.contains(posInAsm)) {
				intervalPosInAsm.add(posInAsm);
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
			intervalAsm = (int) (Double.parseDouble(asmTo) - Double.parseDouble(asmFrom));
			
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
		System.out.println("Usage: java -jar posIntervalToNumSupports.jar <asm> <in.intervals>");
		System.out.println("\t<asm>: assembly name (chromosome). Note this code assumes we have only 1 asm.");
		System.out.println("\t<in.intervals>:generated with posToInterval.jar\n"
				+ "\tseqID\tposAsmFrom\tposAsmTo\tintervalInAsm\tIntervalInSeq");
		System.out.println("\t<stdout>:\t\tasm\tposAsmFrom\tposAsmTo\tNum.ReadsSupporting\tintervalInAsm\tintervalAvg.InReads\tSD\tinterval1:interval2:..:intervalN\treadId1:readId2:..:readIdN");
		System.out.println("Arang Rhie, 2019-03-04. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			asm = args[0];
			new IntervalToNumSupports().go(args[1]);
		} else {
			new IntervalToNumSupports().printHelp();
		}
	}

}
