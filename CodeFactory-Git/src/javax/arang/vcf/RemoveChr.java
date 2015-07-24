/**
 * 
 */
package javax.arang.vcf;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class RemoveChr extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("##contig=")) {
				line = line.replace("ID=chr", "ID=");
			}
			if (line.startsWith("#")) {
				fm.writeLine(line);
				continue;
			}
			line = line.replace("chr", "");
			fm.writeLine(line);
		}
	}

	@Override
	public void printHelp() {
		// TODO Auto-generated method stub
		System.out.println("Usage: java -jar vcfRemoveChr.jar <file>.vcf");
		System.out.println("\tRemove \"chr\" from vcf");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new RemoveChr().go(args[0], args[0].replace("vcf", "wo.chr.vcf"));
		} else {
			new RemoveChr().printHelp();
		}
	}

}
