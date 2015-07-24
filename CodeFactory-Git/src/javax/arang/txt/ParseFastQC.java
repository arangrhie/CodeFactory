/**
 * 
 */
package javax.arang.txt;

import java.io.File;

import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class ParseFastQC {

	private void go(String root) {
		File dirRoot = new File(root);
		FileMaker fm = new FileMaker(dirRoot.getAbsolutePath(), "fastqc_summary.txt");
		fm.writeLine("SampleID\tTotal Sequences\tSequence Length\t%GC");
		System.out.println(dirRoot.getAbsolutePath());
		File[] subDirs = dirRoot.listFiles();
		for (File subDir : subDirs) {
			if (subDir.isFile())	continue;
			System.out.println(subDir.getName());
			String file = dirRoot.getAbsolutePath() + "/" + subDir.getName() + "/fastqc_data.txt";
			FileReader fr = new FileReader(file);
			String line;
			String[] tokens;
			fr.readLine();	//##FastQC	0.10.1
			fr.readLine();	//>>Basic Statistics	pass
			fr.readLine();	//#Measure	Value	
			line = fr.readLine();	//Filename	08-1263.sorted.dp.bam
			tokens = line.split("\t");
			fm.write(tokens[1].replace(".sorted.dp.bam", "") + "\t");
			fr.readLine();	//File type	Conventional base calls	
			fr.readLine();	//Encoding	Sanger / Illumina 1.9
			line = fr.readLine();	//Total Sequences	71091332
			tokens = line.split("\t");
			fm.write(tokens[1] + "\t");
			fr.readLine();	//Filtered Sequences	0	
			line = fr.readLine();	//Sequence length	101
			tokens = line.split("\t");
			fm.write(tokens[1] + "\t");
			line = fr.readLine();	//%GC	40
			tokens = line.split("\t");
			fm.writeLine(tokens[1]);
			fr.closeReader();
		}
		fm.closeMaker();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new ParseFastQC().go(args[0]);
		} else {
			new ParseFastQC().printHelp();
		}

	}
	
	private void printHelp() {
		System.out.println("Usage: java -jar txtParseFASTQC.jar <dir_root>");
		System.out.println("\tlooks into sub-directories containing fastqc_data.txt");
		System.out.println("\tparses Total Sequences and Sequence length out.");
		System.out.println("\t<fastqc_summary.txt>: <sampleID>\tTotal Sequence\tSequence Length\t%GC");
	}

}
