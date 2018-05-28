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
			tokens = line.split(RegExp.TAB);
			start = Integer.parseInt(tokens[Bed.START]);
			end = Integer.parseInt(tokens[Bed.END]);
			len = end - start;
			lenSum += len;
			lenArr.add(len);
		}
		
		Collections.sort(lenArr);
		double n50 = Util.getN50(lenArr, lenSum);
		System.err.println("Num. blocks:\t" + Format.numbersToDecimal(lenArr.size()));
		System.err.println("Genome covered bases:\t" + Format.numbersToDecimal(lenSum));
		System.err.println("Min: \t" + Format.numbersToDecimal(lenArr.get(0)));
		System.err.println("Avg. block size:\t" + Format.numbersToDecimal((float)lenSum / lenArr.size()));
		System.err.println("N50:\t" + Format.numbersToDecimal(n50));
		System.err.println("Longest block (contig) size:\t" + Format.numbersToDecimal(lenArr.get(lenArr.size() - 1)));
		
		System.out.println("Num. Blocks\tGenome Covered (bp)\tMin. Block\tAvg. Block Size\tBlock Size N50\tLongest Block Size");
		System.out.println(Format.numbersToDecimal(lenArr.size())
				+ "\t" + Format.numbersToDecimal(lenSum)
				+ "\t" + Format.numbersToDecimal(lenArr.get(0))
				+ "\t" + Format.numbersToDecimal((float)lenSum / lenArr.size())
				+ "\t" + Format.numbersToDecimal(n50)
				+ "\t" + Format.numbersToDecimal(lenArr.get(lenArr.size() - 1))
				);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingBedN50.jar <sort.merged.bed>");
		System.out.println("Simple tools for calculating num. blocks, N50, L50, longest block size, genome covered bases.");
		System.out.println("\t<in.bed>: bedSort.jar <in.bed> <sort.bed>, bedtools merge -i <sort.bed> > <sort.merged.bed>");
		System.out.println("Arang Rhie, 2018-05-01. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new CalcN50().go(args[0]);
		} else {
			new CalcN50().printHelp();
		}
	}

}
