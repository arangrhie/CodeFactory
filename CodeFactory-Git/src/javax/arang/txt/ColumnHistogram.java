package javax.arang.txt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ColumnHistogram extends Rwrapper {

	private static int colIdx;
	private static int binWidth;
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		ArrayList<Integer> binStartIdxList = new ArrayList<Integer>();
		HashMap<Integer, Integer> binCounts = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> binValueSum = new HashMap<Integer, Integer>();
		int value;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			value = Integer.parseInt(tokens[colIdx]);
			
			if (!binStartIdxList.contains(value/binWidth)) {
				binStartIdxList.add(value/binWidth);
				binCounts.put(value/binWidth, 1);
				binValueSum.put(value/binWidth, value);
			} else {
				binCounts.put(value/binWidth, binCounts.get(value/binWidth) + 1);
				binValueSum.put(value/binWidth, binValueSum.get(value/binWidth) + value);
			}
		}
		
		Collections.sort(binStartIdxList);
		for (int i = binStartIdxList.get(0); i <= binStartIdxList.get(binStartIdxList.size() - 1); i++) {
			if (binStartIdxList.contains(i)) {
				System.out.println((i * binWidth) + "\t" + binCounts.get(i) + "\t" + binValueSum.get(i));
			} else {
				System.out.println((i * binWidth) + "\t0\t0");
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtColumnHistogram.jar <in.tdf> <columnIdx> <binWidth>");
		System.out.println("\t<in.tdf>: input text file");
		System.out.println("\t<columnIdx>: target column index. 1-based");
		System.out.println("\t<binWidth>: bin width in the histogram");
		System.out.println("\tSTDOUT: binStartValue(Starts with 0) \tcount");
		System.out.println("Arang Rhie, 2016-11-15. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			colIdx = Integer.parseInt(args[1]) - 1;
			binWidth = Integer.parseInt(args[2]);
			new ColumnHistogram().go(args[0]);
		} else {
			new ColumnHistogram().printHelp();
		}
	}

}
