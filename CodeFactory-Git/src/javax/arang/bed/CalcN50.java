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
		
		int start;
		int end;
		int len;
		ArrayList<Integer> lenArr = new ArrayList<Integer>();
		double lenSum = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			start = Integer.parseInt(tokens[Bed.START]);
			end = Integer.parseInt(tokens[Bed.END]);
			len = end - start;
			lenSum += len;
			lenArr.add(len);
		}
		
		Collections.sort(lenArr);
		System.err.println("Num. blocks:\t" + String.format("%,d", lenArr.size()));
		int n50 = Util.getN50(lenArr, lenSum);
		System.err.println("N50:\t" + String.format("%,d", n50));
		System.err.println("Avg. block size:\t" + String.format("%,.1f", ((float)lenSum / lenArr.size())));
		System.err.println("Genome covered bases:\t" + String.format("%,.0f", lenSum));
		System.err.println("Num. Blocks\tBlock Size N50\tAvg. Block Size\tLongest Block Size\tGenome Covered (bp)");
		System.out.println(String.format("%,d", lenArr.size())
				+ "\t" + String.format("%,d", n50)
				+ "\t" + String.format("%,.1f", ((float)lenSum / lenArr.size()))
				+ "\t" + Format.numbersToDecimal(lenArr.get(lenArr.size() - 1))
				+ "\t" + String.format("%,.0f", lenSum));
	}

	@Override
	public void printHelp() {
		System.out.println("Simple tools for calculating num. blocks, N50, longest block size, genome covered bases.");
		System.out.println("Usage: java -jar phasingBedN50.jar <sort.merged.bed>");
		System.out.println("\t<in.bed>: bedSort.jar <in.bed> <sort.bed>, bedtools merge -i <sort.bed> > <sort.merged.bed>");
		System.out.println("Arang Rhie, 2015-07-29. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new CalcN50().go(args[0]);
		} else {
			new CalcN50().printHelp();
		}
	}

}
