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
public class AddBasePairs extends IOwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.Rwrapper#hooker(javax.arang.IO.FileReader)
	 */
	@Override
	public void hooker(FileReader fr, FileMaker fm) {

		String line;
		String[] tokens;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			if (tokens.length < 3) {
				// skip header line
				fm.writeLine(line);
				continue;
			}
			long start = Long.parseLong(tokens[Bed.START]);
			long end = Long.parseLong(tokens[Bed.END]);
			if (start > basesToAdd)	start -= basesToAdd;
			else start = 0;
			end += basesToAdd;
			fm.writeLine(tokens[Bed.CHROM] + "\t" + start + "\t" + end);
		}
		System.out.println("Do bedReduceIntervals.jar " + fm.getFileName() + " <out.bed>");
	}
	
	/* (non-Javadoc)
	 * @see javax.arang.IO.Rwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedAddBasePairs.jar <in.bed> <out.bed> <bases_to_add>");
		System.out.println("\tAdds <bases_to_add> on the START, END to make a more flexible covered region.");
		System.out.println("\t<out.bed>: output file.");
		System.out.println("\tUse bedReduceIntervals.jar after this process is over.");
		System.out.println("Arang Rhie, 2014-03-18. arrhie@gmail.com");
	}
	
	static int basesToAdd = 0;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 3) {
			new AddBasePairs().printHelp();
		} else {
			basesToAdd = Integer.parseInt(args[2]);
			new AddBasePairs().go(args[0], args[1]);
		}
	}

}
