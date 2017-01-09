package javax.arang.bed;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class FindOverlaps extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String prevLine = "";
		String line;
		String[] tokens;
		
		int prevEnd = -1;
		int start;
		int end;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			start = Integer.parseInt(tokens[Bed.START]);
			end = Integer.parseInt(tokens[Bed.END]);
			
			if (start < prevEnd) {
				System.out.println(prevLine);
				System.out.println(line);
				System.out.println("======================================");
			}
			prevEnd = end;
			prevLine = line;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedFindOverlaps.jar <in.sort.bed>");
		System.out.println("\tSimply checks if the previous and current lines are overlapping.");
		System.out.println("\t\tOverlapping lines are printed in stdout. ");
		System.out.println("\t<in.sort.bed>: use bedSort.jar if not already sorted.");
		System.out.println("Arang Rhie, 2016-10-21. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new FindOverlaps().go(args[0]);
		} else {
			new FindOverlaps().printHelp();
		}
	}

}
