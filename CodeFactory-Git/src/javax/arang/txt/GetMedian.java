package javax.arang.txt;

import java.util.ArrayList;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class GetMedian extends Rwrapper {

	private static int startColIdx = 1;
	private static int endColIdx = 10;
	private static boolean isIdxGiven = false;
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		if (!isIdxGiven) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			endColIdx = tokens.length - 1;
			fr.reset();
		}
		
		ArrayList<Double> numbers = new ArrayList<Double>();
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtGetMedian.jar <in.file> [start-col-idx] [end-col-idx]");
		System.out.println("\t<stdout>: print median value at the end of each given line");
		System.out.println("\t<in.file>: any tab/space delimited file");
		System.out.println("\t[start-col-idx]: 1-based. DEFAULT=2");
		System.out.println("\t[end-col-idx]: 1-based. DEFAULT=last column");
		System.out.println("Arang Rhie, 2017-11-24. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			isIdxGiven = false;
			new GetMedian().go(args[0]);
		} else if (args.length == 3) {
			startColIdx = Integer.parseInt(args[1]) - 1;
			endColIdx = Integer.parseInt(args[2]) - 1;
			isIdxGiven = true;
			new GetMedian().go(args[0]);
		}
	}

}
