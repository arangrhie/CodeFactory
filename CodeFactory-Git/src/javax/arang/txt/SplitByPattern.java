/**
 * 
 */
package javax.arang.txt;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class SplitByPattern extends Rwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.Rwrapper#hooker(javax.arang.IO.FileReader)
	 */
	@Override
	public void hooker(FileReader fr) {
		
		String line;
		FileMaker fm = null;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith(pattern)) {
				if (fm != null) {
					fm.closeMaker();
				}
				fm = new FileMaker(fr.getDirectory(), line.trim());
			}
			fm.writeLine(line);
		}

	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.Rwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Useage: java -jar txtSplitByPattern.jar <pattern> <in.txt>");
		System.out.println("\tSplit input file where line starts with <pattern>.\n" +
				"\tOutput files (splitted files) will be named after <pattern> appearing line.");
	}

	static String pattern;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			pattern = args[0];
			new SplitByPattern().go(args[1]);
		} else {
			new SplitByPattern().printHelp();
		}
	}

}
