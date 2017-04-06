package javax.arang.genome.fasta;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class SplitByName extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		FileMaker fm = null;
		String seqName;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			// Extract the fa seq name for the out file name
			if (line.startsWith(">")) {
				seqName = line.substring(1);
				tokens = seqName.split(RegExp.WHITESPACE);
				if (tokens.length > 1) {
					seqName = tokens[0];
				}
				System.err.println("\t" + seqName);
				
				if (fm != null) {
					fm.closeMaker();
				}
				fm = new FileMaker(outDir, seqName + ".fa");
			}
			fm.writeLine(line);
		}
		if (fm != null) {
			fm.closeMaker();
		}
	}
	

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaSplitByName.jar <in.fasta> <out-dir>");
		System.out.println("Splits a fasta file by sequence names (first token split by a whitespace)");
		System.out.println("\t<in.fasta>: Any fasta file");
		System.out.println("\t<out-dir>: <seq_name>.fa files will be made in this directory");
		System.out.println("Arang Rhie, 2015-10-22. arrhie@gmail.com");
	}

	private static String outDir;
	public static void main(String[] args) {
		if (args.length == 2) {
			outDir = args[1];
			new SplitByName().go(args[0]);
		} else {
			new SplitByName().printHelp();
		}
	}

}
