package javax.arang.genome.fasta;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ReverseOrientation extends IOwrapper {

	public static void main(String[] args) {
		if (args.length == 2) {
			new ReverseOrientation().go(args[0], args[1]);
		} else {
			new ReverseOrientation().printHelp();
		}
	}
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		StringBuffer newSeq = new StringBuffer();
		char base;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith(">")) {
				fm.writeLine(line);
				if (newSeq.length() > 0) {
					fm.writeLine(newSeq.toString());
				}
				newSeq = new StringBuffer();
				continue;
			}
			line = line.trim();
			for (int i = 0; i < line.length(); i++) {
				base = line.charAt(i);
				newSeq.insert(0, base);
			}
		}
		fm.writeLine(newSeq.toString());
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaReverseOrientation.jar <in.fa> <out.fa>");
		System.out.println("\tReverse order of <in.fa> to <out.fa>");
		System.out.println("Arang Rhie, 2016-03-25. arrhie@gmail.com");
	}

}
