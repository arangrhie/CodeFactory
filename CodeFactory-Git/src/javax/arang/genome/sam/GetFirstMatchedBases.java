package javax.arang.genome.sam;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class GetFirstMatchedBases extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		String[] seqData = new String[2];
		int pos;
		int seqStart;
		int count = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@"))	continue;
			count++;
			tokens = line.split(RegExp.TAB);
			seqData[0] = tokens[Sam.CIGAR];
			seqData[1] = tokens[Sam.SEQ];
			seqStart = Integer.parseInt(tokens[Sam.POS]);
			System.out.print(tokens[Sam.QNAME] + "\t" + tokens[Sam.FLAG] + "\t" + tokens[Sam.RNAME] + "\t" + tokens[Sam.POS] + "\t");
			for (int i = 0; i < numBases; i++) {
				pos = seqStart + i;
				char baseInRead = SAMUtil.getBaseAtPos(pos, seqStart, seqData);
				System.out.print(String.valueOf(baseInRead));
			}
			System.out.print("\t");
			
			System.out.println();
			if (count == numLines)	break;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samGetFirstMatched10Bases.jar <in.sam> <numBases> [NUM_LINES]");
		System.out.println("\t\tReturns the first <numBases> bases, matched to the reference.");
		System.out.println("\t\tThis is a test code, so will only print the first NUM_LINES sam lines");
		System.out.println("Arang Rhie, 2015-08-03. arrhie@gmail.com");
		
	}

	private static int numBases = 10;
	private static int numLines = 5;
	public static void main(String[] args) {
		if (args.length == 3) {
			numBases = Integer.parseInt(args[1]);
			numLines = Integer.parseInt(args[2]);
			new GetFirstMatchedBases().go(args[0]);
		} else if (args.length == 2) {
			numBases = Integer.parseInt(args[1]);
			new GetFirstMatchedBases().go(args[0]);
		} else {
			new GetFirstMatchedBases().printHelp();
		}
	}

}
