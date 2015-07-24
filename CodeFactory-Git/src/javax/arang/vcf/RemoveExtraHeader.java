/**
 * 
 */
package javax.arang.vcf;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class RemoveExtraHeader extends IOwrapper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			numLinesToRemove = Integer.parseInt(args[1]);
			new RemoveExtraHeader().go(args[0], args[0].replace(".vcf", "_formatted.vcf"));
		} else if (args.length == 3) {
			numLinesToRemove = Integer.parseInt(args[1]);
			headerWord = args[2];
			new RemoveExtraHeader().go(args[0], args[0].replace(".vcf", "_formatted.vcf"));
		} else {
			new RemoveExtraHeader().printHelp();
		}
	}
	
	public static int numLinesToRemove = 0;
	public static String headerWord = "#CHR";

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		for (int i = 0; i < numLinesToRemove; i++) {
			fm.writeLine(fr.readLine());	// skip to remain real header
		}

		int totalNumVars = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith(headerWord)) {
				for (int i = 0; i < numLinesToRemove; i++) {
					// do nothing, skip this line
				}		
			} else {
				fm.writeLine(line);
				totalNumVars++;
			}
		}
		System.out.println(totalNumVars + "\tvariants in " + fr.getFileName());
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar vcfRemoveExtraHeader.jar <in.vcf> <lines to remove> [header_starting_word]");
		System.out.println("\tRemove extra header in VCF file, starting with <header_starting_word>");
		System.out.println("\t1st header line(s) will be remained.");
		System.out.println("\t<lines to remove>: lines to remove starting from the header line");
		System.out.println("\t<header_starting_word>: DEFAULT=#CHR");
		System.out.println("\t2013-07-02");
	}

}
