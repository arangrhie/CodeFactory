package javax.arang.test.instances;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class Split60000 extends IOwrapper {

	final static int CHR = 0;
	final static int START = 1;
	final static int END = 2;
	final static int LEN = 3;
	
	static int lenToSplit = 60000;
	static int smallPart = 10000;
	static int largePart = 50000;

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		line = fr.readLine();
		fm.writeLine(line);
		
		String lenToSplitStr = String.valueOf(lenToSplit);
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			if (tokens[LEN].equals(lenToSplitStr)) {
				System.out.println(tokens[LEN]);
				if (tokens[START].equals("1")) {
					int newEnd = Integer.parseInt(tokens[START]) + smallPart;
					fm.writeLine(tokens[CHR] + "\t" + tokens[START] + "\t" + newEnd + "\t" + smallPart);
					fm.writeLine(tokens[CHR] + "\t" + newEnd + "\t" + tokens[END] + "\t" + largePart);
				} else {
					int newEnd = Integer.parseInt(tokens[START]) + largePart;
					fm.writeLine(tokens[CHR] + "\t" + tokens[START] + "\t" + newEnd + "\t" + largePart);
					fm.writeLine(tokens[CHR] + "\t" + newEnd + "\t" + tokens[END] + "\t" + smallPart);
				}
			} else {
				fm.writeLine(line);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar testSplit60000.jar <in.txt> <out.txt>");
		System.out.println("Split the line containing length of 60000 into 2 lines: 10000 and 50000 for begin=1, else 50000 and 10000");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new Split60000().go(args[0], args[1]);
		} else if (args.length == 4) {
			lenToSplit = Integer.parseInt(args[2]);
			smallPart = Integer.parseInt(args[3]);
			largePart = lenToSplit - smallPart;
			System.out.println("[DEBUG] :: " + lenToSplit + " " + smallPart + " " + largePart);
			new Split60000().go(args[0], args[1]);
		} else {
			new Split60000().printHelp();
		}
	}

}
