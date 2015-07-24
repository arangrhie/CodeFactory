/**
 * 
 */
package javax.arang.test;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class FindWord extends Rwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.Rwrapper#hooker(javax.arang.IO.FileReader)
	 */
	@Override
	public void hooker(FileReader fr) {
		String line;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.contains(pattern)) {
				System.out.println(line);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.Rwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar findWord.jar <in.file> [pattern]");
		System.out.println("\tOutputs lines containing [pattern]");
	}
	
	static String pattern;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			pattern = args[1];
			new FindWord().go(args[0]);
		} else {
			new FindWord().printHelp();
		}
	}

}
