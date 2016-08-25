package javax.arang.annovar;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.annovar.util.ANNOVAR;

public class ConvertNovel extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String[] tokens;
		int novelNum = 1;
		while (fr.hasMoreLines()) {
			tokens = fr.readLine().split("\t");
			if (tokens[ANNOVAR.NOTE].equals(".")) {
				tokens[ANNOVAR.NOTE] = "novel" + novelNum++;
			}
			
			fm.write(tokens[ANNOVAR.CHR]);
			for (int i = 1; i < tokens.length; i++) {
				fm.write("\t" + tokens[i]);
			}
			fm.writeLine();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar annovarConvertNovel.jar <in.snp> <out.snp>");
		System.out.println("\t<in.snp>: ANNOVAR formatted. NOTE field should contain snp id. [.] are converted to novelN.");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new ConvertNovel().go(args[0], args[1]);
		} else {
			new ConvertNovel().printHelp();
		}
	}

}
