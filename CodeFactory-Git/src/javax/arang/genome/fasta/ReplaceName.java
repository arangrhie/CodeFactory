package javax.arang.genome.fasta;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

public class ReplaceName extends Rwrapper {

	@Override
	public void hooker(FileReader frFa) {
		
		String line;
		int count = 1;
		while (frFa.hasMoreLines()) {
			line = frFa.readLine();
			if (line.startsWith(">")) {
				System.out.println(">" + prefix + String.format("%0" + n + "d", count));
				count++;
			} else {
				System.out.println(line);
			}
		}

	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaReplaceName.jar <in.fasta> <prefix> [n]");
		System.out.println("\t<in.fasta>: fasta file");
		System.out.println("\t<prefix>: prefix for the read names. <prefix>NNN will be assigned.");
		System.out.println("\t[n]: Number of digits for N. ex. 4 will give you read names from <prefix>0001 to <prefix>9999. DEFAULT = 3");
		System.out.println("\t<out>: standard output");
		System.out.println("Arang Rhie, 2017-01-25. arrhie@gmail.com");
	}

	private static String prefix;
	private static int n = 3;
	public static void main(String[] args) {
		if (args.length == 2) {
			prefix = args[1];
			new ReplaceName().go(args[0]);
		} else if (args.length == 3) {
			prefix = args[1];
			n = Integer.parseInt(args[2]);
			new ReplaceName().go(args[0]);
		} else {
			new ReplaceName().printHelp();
		}
	}

}
