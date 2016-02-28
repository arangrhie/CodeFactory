package javax.arang.genome.fasta;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class CleanEmptySeq extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String contig;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith(">")) {
				contig = line;
				line = fr.readLine();
				if (line.trim().equals("")) {
					System.out.println(contig + " is empty");
				} else {
					fm.writeLine(contig);
					fm.writeLine(line);
				}
			}
			else if (line.trim().equals("")) {
				continue;
			} else {
				fm.writeLine(line);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaCleanEmptySeq.jar <in.fa> <out.fa>");
		System.out.println("\tClean up empty fa sequences");
		System.out.println("\tMade for cleaning fa file generated with baseToFasta.jar");
		System.out.println("Arang Rhie, 2015-08-27. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new CleanEmptySeq().go(args[0], args[1]);
		} else {
			new CleanEmptySeq().printHelp();
		}
	}

}
