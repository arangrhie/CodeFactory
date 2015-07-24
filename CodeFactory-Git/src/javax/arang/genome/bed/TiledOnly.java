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
public class TiledOnly extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		boolean tiled = false;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.contains("tiled")) {
				tiled = true;
				continue;
			}
			if (tiled) {
				fm.writeLine(line);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedTiledOnly.jar <in.bed>");
		System.out.println("\t<output>: <in_tiled_only.bed>");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new TiledOnly().go(args[0], args[0].replace(".bed", "_tiled_only.bed"));
		} else {
			new TiledOnly().printHelp();
		}
	}

}
