/**
 * 
 */
package javax.arang.txt;

import java.io.File;

import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class PrintFileNames {

	/* (non-Javadoc)
	 * @see javax.arang.IO.INwrapper#printHelp()
	 */
	public void printHelp() {
		System.out.println("Usage:  java -jar txtPringFileNames.jar <in.files>");
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.INwrapper#hooker(java.util.ArrayList)
	 */
	public void go(String dir) {
		File directory = new File(dir);
		File[] frs = directory.listFiles();
		for (File fr : frs) {
			System.out.println(fr.getName());
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			new PrintFileNames().printHelp();
		} else {
			new PrintFileNames().go(args[0]);
		}

	}

}
