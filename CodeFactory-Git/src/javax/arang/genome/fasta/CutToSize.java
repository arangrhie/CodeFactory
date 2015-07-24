package javax.arang.genome.fasta;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class CutToSize extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String readId = "dummy";
		int numSplit = 1;
		int baseCount = 0;
		boolean isInLine = false;
		double totalBases = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith(">")) {
				System.out.println("[DEBUG] :: " + readId + "_" + String.format("%02d", numSplit) + " : " + baseCount);
				readId = line.trim();
				numSplit = 1;
				baseCount=0;
				fm.writeLine(readId + "_" + String.format("%02d", numSplit));
			} else {
				for (int i = 0; i < line.length(); i++) {
					baseCount++;
					totalBases++;
					isInLine = true;
					fm.write(line.charAt(i));
					if (baseCount == maxSize) {
						fm.writeLine();
						System.out.println("[DEBUG] :: " + readId + "_" + String.format("%02d", numSplit) + " : " + baseCount);
						numSplit++;
						baseCount = 0;
						fm.writeLine(readId + "_" + String.format("%02d", numSplit));
						isInLine = false;
					}
				}
				if (isInLine) {
					fm.writeLine();
					isInLine = false;
				}
			}
		}
		System.out.println("[DEBUG] :: " + readId + "_" + String.format("%02d", numSplit) + " : " + baseCount);
		System.out.println("[DEBUG] :: Total bases: " + totalBases);
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaCutToSize.jar <in.fa> <out.fa> [max_size=10,000,000]");
		System.out.println("\tSplits the sequence into <max_size>, numbering according to the fa ID_nn");
		System.out.println("Arang Rhie, 2015-04-12. arrhie@gmail.com");
	}
	
	static int maxSize = 10000000;

	public static void main(String[] args) {
		if (args.length == 2) {
			new CutToSize().go(args[0], args[1]);
		} else if (args.length == 3) {
			maxSize = Integer.parseInt(args[2]);
			new CutToSize().go(args[0], args[1]);
		} else {
			new CutToSize().printHelp();
		}
	}

}
