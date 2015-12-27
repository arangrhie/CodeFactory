package javax.arang.sam;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ReplaceRef extends IOwrapper {

	private static String from = "";
	private static String to = "";
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#")) {
				fm.writeLine(line);
			} else {
				fm.writeLine(line.replace(from, to));
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar replaceRef <token_to_replace> <token_replaced> <infile.sam> <outfile.sam>");
		System.out.println("Replace <token_to_replace> to <token_replaced>.");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 4) {
			from = args[0];
			to = args[1];
			new ReplaceRef().go(args[2], args[3]);
		} else {
			new ReplaceRef().printHelp();
			System.exit(-1);
		}
		
	}

}
