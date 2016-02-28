package javax.arang.phasing.indel;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class AssignHaplotype extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String hapA;
		String hapB;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			
			hapA = tokens[tokens.length - 4];
			hapB = tokens[tokens.length - 1];
			if (hapA.equals("A") && hapB.equals("B")) {
				fm.writeLine(line + "\tH");
			} else if (hapA.equals("A") && !hapB.equals("B")) {
				fm.writeLine(line + "\tA");
			} else if (!hapA.equals("A") && hapB.equals("B")) {
				fm.writeLine(line + "\tB");
			} else if (!hapA.equals("A") && !hapB.equals("B")) {
				fm.writeLine(line + "\tNA");
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingIndelAssignHaplotype.jar <region.span.determined.bed> <out.assigned.bed>");
		System.out.println("\t<region.span.determined.bed>: generated with phasingIndelReduceDeterminedToRegion.jar");
		System.out.println("\t<out.assigned.bed>: adding A/B/H/NA at the end of line");
		System.out.println("Arang Rhie, 2016-01-02. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new AssignHaplotype().go(args[0], args[1]);
		} else {
			new AssignHaplotype().printHelp();
		}
	}

}
