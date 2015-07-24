package javax.arang.genome.snp;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class FilterSynonymous extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		while (fr.hasMoreLines()) {
			String line = fr.readLine();
			String[] tokens = line.split("\t");
			if (tokens[1].startsWith("synonymous")) {
				continue;
			}
			
			fm.writeLine(line.substring(line.indexOf("chr")) + "\t" + tokens[1] + "\t" + tokens[2]);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar annovarFilterSynonymous.jar <in.annovar.exonic_variant_function>");

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new FilterSynonymous().go(args[0], args[0].replace(".exonic_variant_function", ".exonic_nonSyn"));
		} else {
			new FilterSynonymous().printHelp();
		}
	}

}
