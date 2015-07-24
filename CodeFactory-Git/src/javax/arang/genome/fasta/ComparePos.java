package javax.arang.genome.fasta;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ComparePos extends I2Owrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {

		long pos = 0;

		fr1.readLine();
		fr2.readLine();
		fm.writeLine("Pos\t" + fr1.getFullPath() + "\t" + fr2.getFullPath());
		while (fr1.hasMoreLines() && fr2.hasMoreLines()) {
			String line1 = fr1.readLine().toString();
			String line2 = fr2.readLine().toString();
			for (int i = 0; i < line1.length(); i++) {
				pos++;
				if (line1.charAt(i) != line2.charAt(i)) {
					fm.writeLine(pos + "\t" + line1.charAt(i) + "\t" + line2.charAt(i));
				}
			}
		}
		
	}
	
	private void localAlignment() {
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar compare.jar <inFile1> <inFile2>");
		System.out.println("\tCompare the bases on each positions between two files.");
		System.out.println("\tThe differences are reported in file <diff.txt>.");

	}

	
	public static void main(String[] args) {
		if (args.length == 2) {
			new ComparePos().go(args[0], args[1], "diff.txt");
		} else {
			new ComparePos().printHelp();
		}
	}
}
