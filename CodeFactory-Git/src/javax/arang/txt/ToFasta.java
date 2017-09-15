package javax.arang.txt;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToFasta extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith(">")) {
				tokens = line.split(RegExp.WHITESPACE);
				System.out.println(">" + tokens[1]);
			} else {
				if (line.contains(" ")) {
					tokens = line.split(RegExp.WHITESPACE);
					System.out.println(tokens[0] + "NNNNN" + tokens[1]);
				} else {
					System.out.println(line);
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtToFasta.jar <in.txt>");
		System.out.println("\t<stdout>: formatted fasta file");
		System.out.println("\t*This code is specifically made to transfer Alex's exon2, 3 HLA gene sequences");
		System.out.println("Arang Rhie, 2017-08-01. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ToFasta().go(args[0]);
		} else {
			new ToFasta().printHelp();
		}
	}

}
