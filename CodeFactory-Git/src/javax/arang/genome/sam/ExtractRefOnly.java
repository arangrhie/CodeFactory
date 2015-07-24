/**
 * 
 */
package javax.arang.genome.sam;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class ExtractRefOnly extends IOwrapper {

	static String ref = null;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@SQ") && line.contains(ref)) {
				fm.writeLine(line);
				continue;
			} else if (line.startsWith("@SQ")){
				continue;
			} else if (line.startsWith("@")) {
				fm.writeLine(line);
				continue;
			}
			if (line.equals("")) continue;
			tokens = line.split("\t");
			if (!tokens[Sam.RNAME].startsWith(ref))	continue;
			if (tokens[Sam.CIGAR].equals("*"))	continue;
			fm.writeLine(line);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samExtractRefOnly.jar <in.sam> <ref>");
		System.out.println("\t<output>: <in.sam.ref>.");
		System.out.println("\t\tSAM formatted file, containing reads only aligned on <ref>.");
		System.out.println("\t\tUnpaired reads are discarded, although RNAME field indicates <ref>.");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			ref = args[1];
			new ExtractRefOnly().go(args[0], args[0] + "." + args[1]);
		} else {
			new ExtractRefOnly().printHelp();
		}
	}

}
