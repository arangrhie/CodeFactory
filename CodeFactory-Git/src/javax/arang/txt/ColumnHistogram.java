package javax.arang.txt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ColumnHistogram extends Rwrapper {

	private static int colIdx;
	private static double binWidth;
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		ArrayList<Double> binStartIdxList = new ArrayList<Double>();
		HashMap<Double, Double> binCounts = new HashMap<Double, Double>();
		HashMap<Double, Double> binValueSum = new HashMap<Double, Double>();
		Double value;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			value = Double.parseDouble(tokens[colIdx]);
			
			if (!binStartIdxList.contains(value/binWidth)) {
				binStartIdxList.add(value/binWidth);
				binCounts.put(value/binWidth, 1d);
				binValueSum.put(value/binWidth, value);
			} else {
				binCounts.put(value/binWidth, binCounts.get(value/binWidth) + 1d);
				binValueSum.put(value/binWidth, binValueSum.get(value/binWidth) + value);
			}
		}
		
		Collections.sort(binStartIdxList); // 1, 2, 3, ..., N
		for (double i = binStartIdxList.get(0); i <= binStartIdxList.get(binStartIdxList.size() - 1); i++) {
			if (binStartIdxList.contains(i)) {
				System.out.println(String.format("%.0f", (i * binWidth)) + "\t"
						+ String.format("%.0f", binCounts.get(i)) + "\t"
						+ String.format("%.2f", binValueSum.get(i)));
			} else {
				System.out.println(String.format("%.0f", (i * binWidth)) + "\t0\t0");
			}
		}
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar txtColumnHistogram.jar <in.tdf> <columnIdx> <binWidth>");
		System.err.println("  in.tdf    input text file");
		System.err.println("  columnIdx target column index. 1-based");
		System.err.println("  binWidth  bin width in the histogram");
		System.err.println("  STDOUT    binStartValue(Starts with 0) <tab> count <tab> valueSum");
		System.err.println("Arang Rhie, 2022-12-27. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			colIdx = Integer.parseInt(args[1]) - 1;
			binWidth = Double.parseDouble(args[2]);
			new ColumnHistogram().go(args[0]);
		} else {
			new ColumnHistogram().printHelp();
		}
	}

}
