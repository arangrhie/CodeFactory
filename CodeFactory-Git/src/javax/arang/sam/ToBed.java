package javax.arang.sam;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToBed extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		
		String line;
		String[] tokens;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@"))	continue;
			tokens = line.split(RegExp.WHITESPACE);
			System.out.println(tokens[Sam.RNAME] +
					"\t" + (Integer.parseInt(tokens[Sam.POS]) - 1) +
					"\t" + (Integer.parseInt(tokens[Sam.POS]) - 1 + Sam.getMatchedBasesLen(tokens[Sam.CIGAR]) + Sam.getDeletedSplicedBasesLen(tokens[Sam.CIGAR])) +
					"\t" + tokens[Sam.QNAME] +
					"\t" + tokens[Sam.FLAG] +
					"\t" + tokens[Sam.MAPQ] +
					"\t" + tokens[Sam.CIGAR]);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samToBed.jar <in.sam>");
		System.out.println("\tExtract the alignment region in sam, with sequence read-id, cigar string, flag, mq");
		System.out.println("\t<sysout>: contig\tstart\tend\tread-id\tflag\tmq\tcigar");
		System.out.println("Arang Rhie, 2017-06-12. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ToBed().go(args[0]);
		} else {
			new ToBed().printHelp();
		}
	}

}
