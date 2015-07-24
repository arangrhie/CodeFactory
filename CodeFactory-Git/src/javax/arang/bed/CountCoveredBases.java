/**
 * 
 */
package javax.arang.bed;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.bed.util.Bed;

/**
 * @author Arang Rhie
 *
 */
public class CountCoveredBases extends Rwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.Rwrapper#hooker(javax.arang.IO.FileReader)
	 */
	@Override
	public void hooker(FileReader fr) {
		String[] tokens;
		int totalBaseCount = 0;
		while (fr.hasMoreLines()) {
			tokens = fr.readLine().split("\t");
			if (tokens.length < 3)	continue;
			totalBaseCount += Integer.parseInt(tokens[Bed.END]) - Integer.parseInt(tokens[Bed.START]);
		}
		System.out.println("Total number of bases covered:\t" + String.format("%,d", totalBaseCount));
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.Rwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedCountCoveredBases.jar <in.bed>");
		System.out.println("\tReports total number of bases covered in <in.bed>");
		System.out.println("Arang Rhie, 2014-03-12. arrhie@gmail.com ");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length == 1) {
			new CountCoveredBases().go(args[0]);
		} else {
			new CountCoveredBases().printHelp();
		}

	}

}
