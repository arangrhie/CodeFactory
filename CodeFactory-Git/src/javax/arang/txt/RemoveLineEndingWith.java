package javax.arang.txt;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class RemoveLineEndingWith extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.endsWith(pattern)) {
				// do nothing
			} else {
				fm.writeLine(line);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtRemoveLineEndingWith.jar <in.txt> <out.txt> <pattern>");
		System.out.println("\tRemove lines ending with <pattern>");
		System.out.println("Arang Rhie, 2015-12-16. arrhie@gmail.com");
	}

	public static String pattern;
	
	public static void main(String[] args) {
		if (args.length == 3) {
			pattern = args[2];
			new RemoveLineEndingWith().go(args[0], args[1]);
		} else {
			new RemoveLineEndingWith().printHelp();
		}
	}

}
