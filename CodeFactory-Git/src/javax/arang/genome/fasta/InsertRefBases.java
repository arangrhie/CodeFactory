package javax.arang.genome.fasta;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class InsertRefBases extends I2Owrapper {

	@Override
	public void hooker(FileReader faFr, FileReader contigFr, FileMaker fm) {
		String line;
		String[] tokens;
		
		int startIdx = 0;
		int endIdx = 0;
		String read = "";
		while (contigFr.hasMoreLines()) {
			line = contigFr.readLine();
			tokens = line.split("\t");
			startIdx = Integer.parseInt(tokens[1]);
			endIdx = Integer.parseInt(tokens[2]);
			read = tokens[4];
		}
		
		int faIdx = 0;
		int readIdx = 0;
		while (faFr.hasMoreLines()) {
			line = faFr.readLine().trim();
			if (line.startsWith(">")) {
				fm.writeLine(line);
				continue;
			}
			for (int i = 0; i < line.length(); i++) {
				faIdx++;
				if (faIdx >= startIdx && endIdx >= faIdx) {
					fm.write(String.valueOf(read.charAt(readIdx++)));
				} else {
					fm.write(String.valueOf(line.charAt(i)));
				}
			}
			fm.writeLine();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaInsertRefBases.jar <in.original.fa> <new.contig.txt> <out.fa>");
		System.out.println("\t<new.contig.txt>: chr\tstart\tend\tlength\tsequence");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new InsertRefBases().go(args[0], args[1], args[2]);
		} else {
			new InsertRefBases().printHelp();
		}
	}

}
