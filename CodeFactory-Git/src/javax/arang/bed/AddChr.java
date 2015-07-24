/**
 * 
 */
package javax.arang.bed;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.bed.util.Bed;

/**
 * @author Arang Rhie
 *
 */
public class AddChr extends IOwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			if (line.startsWith("23")) {
				tokens = line.split("\t");
				if (tokens.length > Bed.NOTE) {
					fm.writeLine("chrX\t" + tokens[Bed.START] + "\t" + tokens[Bed.END] + "\t" + tokens[Bed.NOTE]);
				} else {
					fm.writeLine("chrX\t" + tokens[Bed.START] + "\t" + tokens[Bed.END]);
				}
			}
			else if (line.startsWith("24")) {
				tokens = line.split("\t");
				if (tokens.length > Bed.NOTE) {
					fm.writeLine("chrY\t" + tokens[Bed.START] + "\t" + tokens[Bed.END] + "\t" + tokens[Bed.NOTE]);
				} else {
					fm.writeLine("chrY\t" + tokens[Bed.START] + "\t" + tokens[Bed.END]);
				}
			}
			else if (line.startsWith("25")) {
				tokens = line.split("\t");
				if (tokens.length > Bed.NOTE) {
					fm.writeLine("chrM\t" + tokens[Bed.START] + "\t" + tokens[Bed.END] + "\t" + tokens[Bed.NOTE]);
				} else {
					fm.writeLine("chrM\t" + tokens[Bed.START] + "\t" + tokens[Bed.END]);
				}
			}
			else {
				fm.writeLine("chr" + line);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedAddChr.jar <in.bed>");
		System.out.println("\t<output>: <in_wi_chr.bed> that contains \'chr\' in the first column.");
		System.out.println("\tNo header lines expected. If appears, must start with a #, but will not be printed in <output>.");
		System.out.println("\t\tArang Rhie, 2014-01-21. arrhie@gmail.com");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new AddChr().go(args[0], args[0].replace(".bed", "_wi_chr.bed"));
		} else {
			new AddChr().printHelp();
		}
	}

}
