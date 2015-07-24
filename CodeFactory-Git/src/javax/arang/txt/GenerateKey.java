/**
 * 
 */
package javax.arang.txt;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class GenerateKey extends IOwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		String key;
		int lineNum = 0;
		boolean headerSet = false;
		while (fr.hasMoreLines()) {
			line = fr.readLine().trim();
			lineNum++;
			tokens = line.split("\t");
			if (!headerSet) {
				key = "KEY";
				fm.writeLine(key + "\t" + line);
				System.out.println(":: WARNING :: Suspected header line detected: " + line + " on line " + lineNum);
				System.out.println(":: WARNING :: This line will be printed as KEY\t" + line);
				headerSet = true;
			}
			else if (headerSet) {
				key = tokens[0]
						+ "_" + tokens[1]
						+ "_" + tokens[2]
						+ "_" + tokens[3];
				fm.writeLine(key + "\t" + line);
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtGenerateKey.jar <in.txt>");
		System.out.println("\t<out>: <in.key>. First 4 column is the key");
		System.out.println("\t\tOther annotated columns are attached to the end of the line.");
		System.out.println("\t\tArang Rhie, 2015-03-24. arrhie@gmail.com");

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new GenerateKey().go(args[0], args[0] + ".key");
		} else {
			new GenerateKey().printHelp();
		}
	}

}
