package javax.arang.txt;

import java.util.LinkedList;
import java.util.Queue;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ShiftLiftovers extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		Queue<String> linesToShift = new LinkedList<String>();
		boolean shift = false;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			if (tokens[0].startsWith("#Partially")
					|| tokens[0].startsWith("#Deleted")
					|| tokens[0].startsWith("#Split")
					|| tokens[0].startsWith("#Duplicated")
					|| tokens[0].startsWith("#Boundary")) {
				shift = true;
				fm.writeLine(tokens[0]);
				if (tokens.length > 1) {
					linesToShift.add(tokens[1] + "\t" + tokens[2] + "\t" + tokens[3]);
				}
			} else {
				if (shift && !linesToShift.isEmpty()) {
					fm.writeLine(tokens[0] + "\t" + linesToShift.remove());
					if (tokens.length > 1) {
						linesToShift.add(tokens[1] + "\t" + tokens[2] + "\t" + tokens[3]);
					}
					if (linesToShift.isEmpty()) {
						shift = false;
					}
				} else {
					fm.writeLine(line);
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtShiftLiftovers.jar <in.txt> <out.txt>");
		System.out.println("Shift liftover file");
		System.out.println("\t<in.txt>: #(Error message or N/A)\tchr\tstart\tend");
		System.out.println("\t<out.txt>: #Error message <nothing, the chr, start, end will be shifted to the next line>");
		System.out.println("\t\t#N/A\tchr\tstart\tend");
		System.out.println("Arang Rhie, 2014-04-15. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ShiftLiftovers().go(args[0], args[1]);
		} else {
			new ShiftLiftovers().printHelp();
		}
	}

}
