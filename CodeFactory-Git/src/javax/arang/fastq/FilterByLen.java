package javax.arang.fastq;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

public class FilterByLen extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {

		int lineNum = 0;
		String line;
		String seqId = "";
		int numPass = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (lineNum % 2 == 0) {
				seqId = line;
				lineNum++;
			} else if (lineNum % 2 == 1) {
				if (line.length() > len) {
					System.out.println(seqId);
					System.out.println(line);
					System.out.println(fr.readLine());
					System.out.println(fr.readLine());
					numPass++;
				} else {
					// skip
					fr.readLine();
					fr.readLine();
				}
				lineNum++;
			}
		}
		
		System.err.println(numPass + " / " + lineNum/2 + " entries passed.");
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar fastqFilterByLen.jar len in");
		System.err.println();
		System.err.println("Filter reads < len from in.");
		System.err.println("  len  length filter. in bp (INT)");
		System.err.println("  in   input file. - for stdin");
		System.err.println("Arang Rhie, 2021-08-26. arrhie@gmail.com");
	}

	private static int len = 0;
	
	public static void main(String[] args) {
		if (args.length != 2) {
			new FilterByLen().printHelp();
		} else {
			len=Integer.parseInt(args[0]);
			new FilterByLen().go(args[1]);
		}
	}

}
