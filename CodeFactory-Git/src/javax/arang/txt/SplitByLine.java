package javax.arang.txt;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class SplitByLine extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		int counter = 1;
		int fileCounter = 1;
		FileMaker fm = new FileMaker(fr.getDirectory(), fr.getFileName().replace(".", "_" + fileCounter + "."));
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			fm.writeLine(line);
			counter++;
			if (counter > numSplitLines) {
				counter = 1;
				fm.closeMaker();
				fileCounter++;
				fm = new FileMaker(fr.getDirectory(), fr.getFileName().replace(".", "_" + fileCounter + "."));
			}
		}
		fm.closeMaker();
		System.out.println("File has been split into " + fileCounter + " files");
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtSplitByLine.jar <in.file> <num_lines>");
		System.out.println("\tSplit <in.file> into <in_1.file>, <in_2.file>, ... , <in_N.file>");
		System.out.println("\tEach file contains <num_lines>");
	}

	private static int numSplitLines;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			numSplitLines = Integer.parseInt(args[1]);
			new SplitByLine().go(args[0]);
		} else {
			new SplitByLine().printHelp();
		}
	}

}
