/**
 * 
 */
package javax.arang.sam;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class RemoveSecondaryAlignments extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		int multipleReadNum = 0;
		int totalReadNum = 0;
		
		String line;
		String[] tokens;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@"))	 {
				fm.writeLine(line);
				continue;
			}
			if (line.equals(""))	continue;
			tokens = line.split("\t");
			
			totalReadNum++;
			int flag = Integer.parseInt(tokens[Sam.FLAG]);
			if (SAMUtil.isSecondaryAlignment(flag)) {
				multipleReadNum++;
				continue;
			} else {
				fm.writeLine(line);
			}
		}
		
		System.out.println("Total # of reads\t" + totalReadNum);
		System.out.println("Secondary aligned reads\t" + multipleReadNum);
		System.out.println("Reads after removing secondary alignments\t" + (totalReadNum - multipleReadNum));
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samRemoveSecondaryAlignments.jar <in.sam>");
		System.out.println("\t<out>: in.sam.u -> all secondary aligned flagged reads are discarded.");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new RemoveSecondaryAlignments().go(args[0], args[0] + ".u");
		} else {
			new RemoveSecondaryAlignments().printHelp();
		}
	}
}
