/**
 * 
 */
package javax.arang.genome.bed;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class Bed2IntervalList extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			if (tokens.length < 3) {
				continue;
			}
			fm.writeLine(tokens[0] + ":" + tokens[1] + "-" + tokens[2]);
		}
	}

	@Override
	public void printHelp() {
		// TODO Auto-generated method stub
		System.out.println("Usage: java -jar bed2IntervalList.jar <in.bed>");
		System.out.println("\t<out>: <in.intervals>");
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			new Bed2IntervalList().printHelp();
		} else {
			new Bed2IntervalList().go(args[0], args[0].replace(".bed", ".intervals"));
		}
	}

}
