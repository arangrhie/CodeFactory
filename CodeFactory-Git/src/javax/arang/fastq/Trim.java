package javax.arang.fastq;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class Trim extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String id;
		String read;
		String qual;
		int seqLen = 0;
		while (fr.hasMoreLines()) {
			// @
			id = fr.readLine();
			
			// read
			read = fr.readLine();
			
			// +
			fr.readLine();
			
			// qual
			qual = fr.readLine();
			
			for (int i = qual.length() - 1; i > 0; i--) {
				if (qual.charAt(i) - qualBase >= qualThreshold) {
					seqLen = i;
					break;
				}
			}
			
			if (seqLen > 0) {
				fm.writeLine(id);
				fm.writeLine(read.substring(0, seqLen + 1));
				fm.writeLine("+");
				fm.writeLine(qual.substring(0, seqLen + 1));
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usgae: java -jar fastqTrim.jar <in.fastq> <out.fastq> [qual] [qual_base]");
		System.out.println("\tTrim bases from the end of the read < [qual].");
		System.out.println("\tThis code assumes quality field is based on Phred+33.");
		System.out.println("\tSet [qual_base] 64 for sequences containing 'B' for Phred score 2.");
		System.out.println("\t[qual]: DEFAUL=20");
		System.out.println("\t[qual_base]: DEFAULT=33");
		System.out.println("Arang Rhie, 2014-04-11. arrhie@gmail.com");
	}
	
	private static int qualThreshold = 20;
	private static int qualBase = 33;

	public static void main(String[] args) {
		if (args.length == 2) {
			new Trim().go(args[0], args[1]);
		} else if (args.length == 3) {
			qualThreshold = Integer.parseInt(args[2]);
			new Trim().go(args[0], args[1]);
		} else if (args.length == 4) {
			qualThreshold = Integer.parseInt(args[2]);
			qualBase = Integer.parseInt(args[3]);
			new Trim().go(args[0], args[1]);
		} else {
			new Trim().printHelp();
		}
	}

}
