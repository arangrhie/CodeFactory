package javax.arang.genome.fasta;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ToReverseComplement extends IOwrapper {

	public static void main(String[] args) {
		if (args.length == 2) {
			new ToReverseComplement().go(args[0], args[1]);
		} else {
			new ToReverseComplement().printHelp();
		}
	}
	
	public static char toComplement(char base) {
		switch(base) {
		case 'a': return 't';
		case 'A': return 'T';
		case 'c': return 'g';
		case 'C': return 'G';
		case 'g': return 'c';
		case 'G': return 'C';
		case 't': return 'a';
		case 'T': return 'A';
		}
		return 'n';
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
				newSeq.insert(0, toComplement(base));
			}
		}
		fm.writeLine(newSeq.toString());
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaToReverseComplement.jar <in.fa> <out.reverse.fa>");
		System.out.println("\tReverse complement <in.fa> to <out.reverse.fa>");
		System.out.println("Arang Rhie, 2016-01-07. arrhie@gmail.com");
	}

}
