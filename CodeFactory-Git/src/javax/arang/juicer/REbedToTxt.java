package javax.arang.juicer;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class REbedToTxt extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String prevContig = "";
		String contig;
		int pos;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			contig = tokens[0];
			pos = Integer.parseInt(tokens[1]) + 1;
			if (prevContig.equals(contig)) {
				System.out.print(" " + pos);
			} else {
				if (!prevContig.equalsIgnoreCase("")) {
					System.out.println();
				}
				System.out.print(contig + " " + pos);
			}
			prevContig = contig;
		}
		System.out.println();
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar juicerREbedToTxt.jar <assembly.resite.bed>");
		System.out.println("Convert RE site bed format to juicer's RE.txt format.");
		System.out.println("\t<assembly.resite.bed>: generated with fastaFindSequence.jar");
		System.out.println("\t<stdout>: <contig>[ <1-based start pos>]+");
		System.out.println("Arang Rhie, 2018-03-15. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new REbedToTxt().go(args[0]);
		} else {
			new REbedToTxt().printHelp();
		}
	}

}
