package javax.arang.txt;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class LineLen extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			fm.writeLine(line.trim().length() + "");
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtLineLen.jar <in.txt> <out.txt>");
		System.out.println("\t<in.txt>: Any text file containging 1 word in 1 line");
		System.out.println("\t<out.txt>: String.length() of the word of <in.txt>");
		System.out.println("2015-09-14. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new LineLen().go(args[0], args[1]);
		} else {
			new LineLen().printHelp();
		}
	}

}
