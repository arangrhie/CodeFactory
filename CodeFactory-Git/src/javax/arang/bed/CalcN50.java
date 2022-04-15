package javax.arang.bed;

import java.util.ArrayList;
import java.util.Collections;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.Format;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;
import javax.arang.genome.util.Util;

public class CalcN50 extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		double start;
		double end;
		double len;
		ArrayList<Double> lenArr = new ArrayList<Double>();
		double lenSum = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			if (line.startsWith("track"))	continue;
			tokens = line.split(RegExp.TAB);
			start = Integer.parseInt(tokens[Bed.START]);
			end = Integer.parseInt(tokens[Bed.END]);
			len = end - start;
			lenSum += len;
			lenArr.add(len);
		}
		
		Collections.sort(lenArr);
		
		double n50;
		if (isNG) {
			n50 = Util.getN50(lenArr, gSize);
		} else {
			n50 = Util.getN50(lenArr, lenSum);
		}
		System.err.println("Num. blocks:\t" + Format.numbersToDecimal(lenArr.size()));
		System.err.println("Genome covered bases:\t" + Format.numbersToDecimal(lenSum));
		System.err.println("Min: \t" + Format.numbersToDecimal(lenArr.get(0)));
		System.err.println("Avg. block size:\t" + Format.numbersToDecimal((float)lenSum / lenArr.size()));
		System.err.println("N50:\t" + Format.numbersToDecimal(n50));
		System.err.println("Longest block (contig) size:\t" + Format.numbersToDecimal(lenArr.get(lenArr.size() - 1)));
		
		if (isNG) {
			System.out.println("Num. Blocks\tGenome Covered (bp)\tMin. Block\tAvg. Block Size\tBlock Size NG50\tLongest Block Size");
		} else {
			System.out.println("Num. Blocks\tGenome Covered (bp)\tMin. Block\tAvg. Block Size\tBlock Size N50\tLongest Block Size");
		}
		System.out.println(Format.numbersToDecimal(lenArr.size())
				+ "\t" + Format.numbersToDecimal(lenSum)
				+ "\t" + Format.numbersToDecimal(lenArr.get(0))
				+ "\t" + Format.numbersToDecimal((float)lenSum / lenArr.size())
				+ "\t" + Format.numbersToDecimal(n50)
				+ "\t" + Format.numbersToDecimal(lenArr.get(lenArr.size() - 1))
				);
	}

	private static boolean isNG = false;
	private static double  gSize = 0;
	
	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar bedCalcN50.jar <sort.merged.bed> [genome_size]");
		System.err.println("Simple tool for calculating num. blocks, N50, L50, longest block size, genome covered bases.");
		System.err.println("  <in.bed>      Sorted bed file.");
		System.err.println("                Use bedSort.jar <in.bed> <sort.bed> or bedtools sort -i <in.bed> > <sort.bed>");
		System.err.println("  [genome_size] Genome size for obtaining NG values.");
		System.err.println("Arang Rhie, 2021-10-01. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new CalcN50().go(args[0]);
		} else if (args.length == 2) {
			isNG = true;
			gSize = Double.parseDouble(args[1]);
			new CalcN50().go(args[0]);
		} else {
			new CalcN50().printHelp();
		}
	}

}
