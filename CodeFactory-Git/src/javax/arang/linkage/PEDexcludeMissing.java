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
public class PEDexcludeMissing extends IOwrapper{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			PED.NOTE = Integer.parseInt(args[1]);
			new PEDexcludeMissing().go(args[0], args[0].replace(".", "_wo_missing."));
		}
	}
	
	

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			if (tokens[PED.NOTE].equals("")) {
				continue;
			} else {
				fm.writeLine(line);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar PEDexcludeMissing.jar <in.ped> <col_num>");
		System.out.println("\tExclude individuals with missing value in column <col_num>");
		System.out.println("\t<out>: . will be replaced with _wo_missing.");
	}

}
