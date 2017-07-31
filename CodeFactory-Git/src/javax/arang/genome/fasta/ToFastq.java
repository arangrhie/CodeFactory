package javax.arang.genome.fasta;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToFastq extends Rwrapper {

	@Override
	public void hooker(FileReader frFasta) {
		String line;
		String[] tokens;
		StringBuffer quals = new StringBuffer();
		
		while (frFasta.hasMoreLines()) {
			line = frFasta.readLine();
			if (line.startsWith(">")) {
				if (quals.length() > 0) {
					System.out.println("+");
					System.out.print(quals.toString());
					quals = new StringBuffer();
				}
				tokens = line.split(RegExp.WHITESPACE);
				System.out.println("@" + tokens[0].substring(1));
			} else {
				System.out.println(line);
				for (int i = 0; i < line.length(); i++) {
					quals.append(qual);
				}
				quals.append("\n");
			}
		}
		if (quals.length() > 0) {
			System.out.println(quals.toString());
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaToFastq.jar <in.fasta> [qual=40]");
		System.out.println("\tThis code generates a fastq file out of the given fasta file");
		System.out.println("\t<in.fasta>: Original fasta file");
		System.out.println("\t[qual]: DEFAULT=40. In phred scale.");
		System.out.println("Originally inteded for running PBJelly, so it removed any tailing words in the fasta read id.");
		System.out.println("Arang Rhie, 2017-07-27. arrhie@gmail.com");
	}

	public static char qual = (char) 40 + 33;
	
	public static void main(String[] args) {
		if (args.length == 1) {
			new ToFastq().go(args[0]);
		} else if (args.length == 2) {
			qual = (char) (Integer.parseInt(args[2]) + 33);
			new ToFastq().go(args[0]);
		} else {
			new ToFastq().printHelp();
		}
	}

}
