package javax.arang.sam;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToBedWiBX extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		
		String line;
		String[] tokens;
		
		String bc = "";
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@"))	continue;
			tokens = line.split(RegExp.WHITESPACE);
			bc = "";
			if (SAMUtil.isAligned(Integer.parseInt(tokens[Sam.FLAG]))) {
				for (int i = Sam.TAG; i < tokens.length; i++) {
					if (tokens[i].startsWith("BX:Z")) {
						bc = tokens[i].substring(5);
						break;
					}
				}
				
				if (!bc.equals("")) {
					System.out.println(tokens[Sam.RNAME] +
						"\t" + (Integer.parseInt(tokens[Sam.POS]) - 1) +
						"\t" + (Integer.parseInt(tokens[Sam.POS]) - 1 + Sam.getMatchedBasesLen(tokens[Sam.CIGAR]) + Sam.getDeletedSplicedBasesLen(tokens[Sam.CIGAR])) +
						"\t" + tokens[Sam.QNAME] +
						"\t" + tokens[Sam.FLAG] +
						"\t" + tokens[Sam.MAPQ] +
						"\t" + tokens[Sam.CIGAR] +
						"\t" + bc);
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samToBedWiBX.jar <in.sam>");
		System.out.println("\tExtract the alignment region in bam, with sequence read-id, cigar string, flag, mq, and BX");
		System.out.println("\t<sysout>: contig\tstart\tend\tread-id\tflag\tmq\tcigar\tBX");
		System.out.println("Arang Rhie, 2017-06-12. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ToBedWiBX().go(args[0]);
		} else {
			new ToBedWiBX().printHelp();
		}
	}

}
