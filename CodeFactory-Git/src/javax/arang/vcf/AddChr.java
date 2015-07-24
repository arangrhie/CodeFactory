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
public class AddChr extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine().trim();
			if (line.startsWith("#"))	{
				if (line.startsWith("##contig=<ID=GL")) {
					fm.writeLine(line);
					continue;
				}
				if (line.startsWith("##contig=<ID=MT")) {
					fm.writeLine(line.replace("ID=MT", "ID=chrM"));
					continue;
				}
				if (line.startsWith("##contig=<ID")) {
					line = line.replace("=<ID=", "=<ID=chr");
				}
				fm.writeLine(line);
				continue;
			} else {
				if (line.startsWith("GL")) {
					fm.writeLine(line);
				} else {
					if (line.startsWith("MT\t")) {
						line = line.replace("MT\t", "M\t");
					}
					fm.writeLine("chr" + line);
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Add chr to 1, 2, 3, ..., X, Y and change MT to chrM");
		System.out.println("Usage: java -jar vcfAddChr.jar <in.vcf>");
		System.out.println("\t<output>: <in_wi_chr.vcf>");
		System.out.println("Arang Rhie, 2014-03-26. arrhie@gmail.com");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new AddChr().go(args[0], args[0].replace(".vcf", "_wi_chr.vcf"));
		} else {
			new AddChr().printHelp();
		}
		
	}

}
