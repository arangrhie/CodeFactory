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
public class ToBed extends IOwrapper{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new ToBed().go(args[0], args[0].replace(".snp", ".bed"));
		} else if (args.length == 2) {
			new ToBed().go(args[0], args[1]);
		} else {
			new ToBed().printHelp();
		}

	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String[] tokens; 
		while (fr.hasMoreLines()) {
			tokens = fr.readLine().split("\t");
			fm.writeLine(tokens[ANNOVAR.CHR] + "\t" + tokens[ANNOVAR.POS_FROM] + "\t" + (Long.parseLong(tokens[ANNOVAR.POS_TO] + 1)));
		}
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar annovarToBed.jar <in.snp>");
		System.out.println("Converts ANNOVAR formatted <in.snp> into <in.bed>");
		System.out.println("Use bedReduceIntervals.jar to reduce intervalse of <in.bed>");
	}

}
