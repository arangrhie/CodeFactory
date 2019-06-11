package javax.arang.kmer;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class HistToMedian extends Rwrapper {

	private static int startIdx = 0;
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens = null;
		
		int count;
		double freq;
		double freqSum = 0;
		HashMap<Double, Integer> countRange = new HashMap<Double, Integer>();
		ArrayList<Double> freqRangeList = new ArrayList<Double>();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			count = Integer.parseInt(tokens[startIdx]);
			freq = Integer.parseInt(tokens[startIdx + 1]);
			freqSum+=freq;
			countRange.put(freqSum, count);
			freqRangeList.add(freqSum);
		}
		//System.err.println("[ DEBUG ] :: freqSum :: " + String.format("%,.0f", freqSum));
		double medianSum = (double) freqSum/2;
		
		for (int i = 0; i < freqRangeList.size(); i++) {
			if (medianSum < freqRangeList.get(i)) {
				//System.err.println("[ DEBUG ] :: medianSum = " + String.format("%,.0f", medianSum));
				//System.err.println("[ DEBUG ] :: freqRangeList.get(i) = " + String.format("%,.0f", freqRangeList.get(i)));
				if (startIdx > 0) {
					System.out.print(tokens[0] + "\t");
				}
				System.out.println(countRange.get(freqRangeList.get(i-1)));
				break;
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar kmerHistToMedian.jar <hist> [startIdx=1]");
		System.out.println("Get the median coverage for any given histogram");
		System.out.println("\t<hist>: Any coverage histogram");
		System.out.println("\t[startIdx]: 1-base, from left. Column num. containing coverage, startIdx+1 containing frequency.");
		System.out.println("Arang Rhie, 2018-08-08. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new HistToMedian().go(args[0]);
		} else if (args.length == 2) {
			startIdx = Integer.parseInt(args[1]) - 1;
			new HistToMedian().go(args[0]);
		} else {
			new HistToMedian().printHelp();
		}
	}

}
