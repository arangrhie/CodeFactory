/**
 * 
 */
package javax.arang.linkage;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class ConvertNovel extends IOwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String[] tokens;
		int novelCount = 0;
		while (fr.hasMoreLines()) {
			tokens = fr.readLine().split("\t");
			if (tokens[1].equals("novel") || tokens[1].equals(".")) {
				fm.writeLine(tokens[MAP.CHR] + "\tnovel" + ++novelCount +
						"\t" + tokens[MAP.CM] + "\t" + tokens[MAP.BP]);
			}
			else {
				fm.writeLine(tokens[MAP.CHR] + "\t" + tokens[MAP.ID] +
						"\t" + tokens[MAP.CM] + "\t" + tokens[MAP.BP]);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Convert novel SNPs to give a unique ID");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new ConvertNovel().go(args[0], args[1]);
		} else {
			new ConvertNovel().printHelp();
		}
	}

}
