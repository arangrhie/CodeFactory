package javax.arang.txt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ColumnHistogram2 extends Rwrapper {

	private static int colIdx;
	private static int valIdx;
	private static int binWidth;
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		ArrayList<Integer> binStartIdxList = new ArrayList<Integer>();
		HashMap<Integer, Integer> binValue = new HashMap<Integer, Integer>();
		int coord;
		int value;
		
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			coord = Integer.parseInt(tokens[colIdx]);
			value = Integer.parseInt(tokens[valIdx]);
			
			if (!binStartIdxList.contains(coord/binWidth)) {
				binStartIdxList.add(coord/binWidth);
				binValue.put(coord/binWidth, value);
			} else {
				binValue.put(coord/binWidth, binValue.get(coord/binWidth) + value);
			}
		}
		
		Collections.sort(binStartIdxList);
		for (int i = binStartIdxList.get(0); i <= binStartIdxList.get(binStartIdxList.size() - 1); i++) {
			if (binStartIdxList.contains(i)) {
				System.out.println((i * binWidth) + "\t" + binValue.get(i));
			} else {
				System.out.println((i * binWidth) + "\t0");
			}
		}
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar txtColumnHistogram2.jar <in.tdf> <coordIdx> <valueIdx> <binWidth>");
		System.err.println("  in.tdf    input text file in coord <tab> value format");
		System.err.println("  coordIdx  column with genomic coordinate. 1-base");
		System.err.println("  valueIdx> column with values to aggregate. 1-base");
		System.err.println("  binWidth> bin width for the histogram");
		System.err.println("  STDOUT    binStartCoord(Starts with minimum bin with data) \tValue");
		System.err.println("Arang Rhie, 2022-08-22. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 4) {
			colIdx   = Integer.parseInt(args[1]) - 1;
			valIdx   = Integer.parseInt(args[2]) - 1;
			binWidth = Integer.parseInt(args[3]);
			new ColumnHistogram2().go(args[0]);
		} else {
			new ColumnHistogram2().printHelp();
		}
	}

}
