/**
 * 
 */
package javax.arang.bam;

import javax.arang.IO.bam.BamRwrapper;
import javax.arang.IO.bambasic.BamReader;
import javax.arang.bam.util.BamRecord;

/**
 * @author Arang Rhie
 *
 */
public class PrintRecords extends BamRwrapper {

	
	@Override
	public void hooker(BamReader fr) {
		while (fr.hasMoreAlignmentRecord()) {
			BamRecord record = fr.getNextAlignmentRecord();
			System.out.println(record.getReadName());
			System.out.println(record.getRefName(fr.getRefInfo()) + " " + record.getPos());
			System.out.println(record.getBin());
			System.out.println(record.getSeq());
			System.out.println(record.getQual());
		}
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.BamRwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bamTest.jar <in.bam>");
		System.out.println("Prints containing alignment records (read_name, seq, qual).");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new PrintRecords().go(args[0]);
		} else {
			new PrintRecords().printHelp();
		}

	}

}
