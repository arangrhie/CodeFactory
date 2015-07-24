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
public class ListToEmailAddresses extends IOwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		int counter = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			line = line.trim();
			counter++;
			fm.write(line);
			if (counter == 100) {
				fm.writeLine();
				counter = 0;
			}
			else {
				fm.write(", ");
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtListToEmailAddresses.jar <in.txt>");
		System.out.println("\tConverts enter seperated list of email addresses into comma seperated lines, 100 in each line");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new ListToEmailAddresses().go(args[0], args[0]+ ".maillist");
		} else {
			new ListToEmailAddresses().printHelp();
		}

	}

}
