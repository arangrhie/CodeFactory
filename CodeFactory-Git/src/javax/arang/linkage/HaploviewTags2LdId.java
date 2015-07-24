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
public class HaploviewTags2LdId extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		int numSnpInLd = 0;
		int ldID = 0;
		boolean ready = false;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (!ready && line.equals("Test\tAlleles Captured")) {
				ready = true;
				continue;
			}
			if (ready) {
				tokens = line.split("[\t,]");
				if (tokens.length < 3) {
					continue;
				}
				ldID++;
				for (int i = 1; i < tokens.length; i++) {
					numSnpInLd++;
					if (tokens[i].contains(".")) tokens[i] = tokens[i].substring(0, tokens[i].indexOf("."));
					// .replace("_", "\t")
					fm.writeLine(tokens[i] + "\tLD_" + String.format("%05d", ldID));
				}
			}
		}
		System.out.println("Total # of LDs: " + ldID);
		System.out.println("Total # of SNPs collapsed into LD: " + numSnpInLd);
	}
	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar haploviewTags2LdId.jar <.tags>");
		System.out.println("\tOutput: <chr pos>\t<LD-Id> with .ld file extension");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new HaploviewTags2LdId().go(args[0], args[0].replace(".tags", ".ld"));
		} else {
			new HaploviewTags2LdId().printHelp();
		}
	}

}
