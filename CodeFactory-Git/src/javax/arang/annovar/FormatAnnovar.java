package javax.arang.annovar;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class FormatAnnovar extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		while (fr.hasMoreLines()) {
			String line = fr.readLine();
			fm.writeLine(line.substring(line.indexOf("chr")) + "\t" + line.substring(0, line.indexOf("chr")).trim());
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar formatAnnovar.jar <annovar.annotated>");
		System.out.println("\t<output>: <annovar.annotated>.format");
		System.out.println("\tputs the chromosome at the first column.");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new FormatAnnovar().go(args[0], args[0] + ".format");
		} else {
			new FormatAnnovar().printHelp();
		}
	}

}
