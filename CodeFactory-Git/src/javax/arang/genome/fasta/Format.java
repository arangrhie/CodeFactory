package javax.arang.genome.fasta;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class Format extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line = "";
		StringBuffer seq = new StringBuffer();
		int bases = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.charAt(0) == '>') {
				// write the rest of the seq if is any left
				if (seq.length() > 0) {
					fm.writeLine(seq.toString());
				}
				seq.replace(0, seq.length(), "");
				// Write the > line
				fm.writeLine(line.toString());
			} else {
				// add the line to the seq
				seq.append(line.trim());
				bases = seq.length();
				// if the seq >= 70
				while (bases >= 70) {
					// write it and remove it from seq buffer
					fm.writeLine(seq.substring(0, 70));
					seq.replace(0, 70, "");
					bases = seq.length();
				}
			}
		}
		
		if (seq.length() > 0) {
			fm.writeLine(seq.toString());
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaFormat.jar <inFile> <outFile>");
		System.out.println("\t<inFile>: fasta file with some line length difference");
		System.out.println("\t<outFile>: fasta file with sequence length fit to 70 bp");
		System.out.println("Arang Rhie, 2016-03-25. arrhie@gmail.com");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new Format().go(args[0], args[1]);
		} else {
			new Format().printHelp();
		}
	}

}
