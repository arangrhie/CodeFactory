/**
 * 
 */
package javax.arang.test;

import java.util.Vector;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class FindDuplicates extends Rwrapper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new FindDuplicates().go(args[0]);
		} else {
			new FindDuplicates().printHelp();
		}

	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr) {
		String line;
		Vector<String> subjects = new Vector<String>();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (subjects.contains(line)) {
				System.out.println(line + " is duplicate");
			} else {
				subjects.add(line);
			}
		}
	
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Print duplicated subjects in the file.");
		System.out.println("Usage:  jave -jar findDuplicates.jar <in.file>");
	}

}
