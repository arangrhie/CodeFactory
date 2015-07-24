/**
 * 
 */
package javax.arang.annovar;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class GetCols extends IOwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		StringBuffer newLine = new StringBuffer();
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			for (int idx : colNums) {
				newLine.append(tokens[idx] + "\t");
			}
			fm.writeLine(newLine.toString().trim());
			newLine = new StringBuffer();
		}
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar colGetColumns.jar <in.file> <out.file> <colum_idx_1> <colum_idx_2> ...");
		System.out.println("Return colum_idx_1, colum_idx_2, ... seperated with tabs");
		System.out.println("\tcolumn_idx starts with 0");
	}

	static int[] colNums;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 2) {
			colNums = new int[args.length - 2];
			for (int i = 2; i < args.length; i++) {
				colNums[i-2] = Integer.parseInt(args[i]);
				System.out.println(args[i]);
			}
			new GetCols().go(args[0], args[1]);
		}
	}

}
