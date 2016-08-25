/**
 * 
 */
package javax.arang.annovar;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.annovar.util.ANNOVAR;

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
		int chrIdx = 0;
		boolean idxSet = false;
		int lineNum = 0;
		boolean headerSet = false;
		while (fr.hasMoreLines()) {
			line = fr.readLine().trim();
			lineNum++;
			tokens = line.split("\t");
			if (!idxSet) {
				for (int i = 0; i < tokens.length; i++) {
					if (tokens[i].startsWith("chr")) {
						chrIdx = i;
						idxSet = true;
						break;
					}
				}
			}
			if (!idxSet) {
				key = "KEY";
				fm.writeLine(key + "\t" + line);
				System.out.println(":: WARNING :: Suspected header line detected: " + line + " on line " + lineNum);
				System.out.println(":: WARNING :: This line will be printed as KEY\t" + line);
				headerSet = true;
			} else {
				if (!headerSet) {
					fm.write("KEY\tCHR\tSTART\tSTOP\tREF\tALT\tID");
					for (int i = 6; i < tokens.length; i++) {
						fm.write("\tCOL_" + i);
					}
					fm.writeLine();
					headerSet = true;
				}
				key = tokens[ANNOVAR.CHR + chrIdx]
						+ "_" + tokens[ANNOVAR.POS_FROM + chrIdx]
						+ "_" + tokens[ANNOVAR.ALT + chrIdx]; 
				fm.write(key + "\t" + line.substring(line.indexOf(tokens[chrIdx])));
				for (int i = 0; i < chrIdx; i++) {
					fm.write("\t" + tokens[i]);
				}
				fm.writeLine();
			}
			if (!headerSet) {
				fm.write("KEY\tCHR\tSTART\tSTOP\tREF\tALT\tID");
				for (int i = 6; i < tokens.length; i++) {
					fm.write("\tCOL_" + i);
				}
				fm.writeLine();
				headerSet = true;
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar annovarGenerateKey.jar <in.avout>");
		System.out.println("\t<out>: <in.avout.key>. First column is the key: chr_start_alt");
		System.out.println("\t\tOther annotated columns are attached to the end of the line.");
		System.out.println("\t\tArang Rhie, 2014-01-21. arrhie@gmail.com");

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
