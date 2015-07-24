/**
 * 
 */
package javax.arang.vcf;

import java.util.ArrayList;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.genome.util.Util;

/**
 * @author Arang Rhie
 *
 */
public class Merge extends INOwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.INOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar vcfMerge.jar -o <out.vcf> -i <in1.vcf> <in2.vcf> ... <inN.vcf>");
		System.out.println("\tMerge multiple .vcf files into <out.vcf>.");
		System.out.println("\tThis is a simple merging process, retrieving header lines starting with \'#\'");
		System.out.println("\tand concatinating all the files in the chromosome order.");
		System.out.println("Arang Rhie, 2014-03-18. arrhie@gmail.com");
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.INOwrapper#hooker(java.util.ArrayList, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {

		boolean writeHeader = true;
		String line;
		
		frs = Util.sortFilesInChromOrder(frs);
		
		for (FileReader fr : frs) {
			System.out.println("Adding " + fr.getFileName());
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				if (line.startsWith("#") && writeHeader) {
					// Write the header of the first file
					fm.writeLine(line);
				} else {
					writeHeader = false;
					if (line.startsWith("#"))	continue;
					fm.writeLine(line);
				}
			}
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 4) {
			new Merge().printHelp();
		} else {
			String[] inFiles = new String[args.length - 3];
			String outFile = "";
			String arg = "";
			boolean isIn = false;
			int inIdx = 0;
			for (int i = 0; i < args.length; i++) {
				arg = args[i];
				if (arg.equals("-o")) {
					isIn = false;
					outFile = args[i+1];
					i++;
					continue;
				}
				else if (arg.equals("-i")) {
					isIn = true;
				}
				else if (isIn) {
					inFiles[inIdx++] = arg;
				}
				
			}
			new Merge().go(inFiles, outFile);
		}
	}

}
