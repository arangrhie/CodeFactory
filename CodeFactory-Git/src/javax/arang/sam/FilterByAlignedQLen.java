package javax.arang.sam;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class FilterByAlignedQLen extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		
		String line;
		String[] tokens;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			
			if (line.startsWith("@")) {
				// print header
				System.out.println(line);
				
			} else {
				// check CIGAR string
				tokens = line.split(RegExp.TAB);
				if (Sam.getQLen(tokens[Sam.CIGAR]) > len) {
					System.out.println(line);
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar samFilterByAlignedQLen.jar len in.sam");
		System.err.println();
		System.err.println("Filter in.sam by aligned query sequence length (M + I).");
		System.err.println("  len    Length filter. In bp. INT");
		System.err.println("  in.sam Input sam. - for stdin.");
	}

	private static int len = 1000;
	
	public static void main(String[] args) {
		if (args.length != 2) {
			new FilterByAlignedQLen().printHelp();
		} else {
			len = Integer.parseInt(args[0]);
			new FilterByAlignedQLen().go(args[1]);
		}
	}

}
