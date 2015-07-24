/**
 * 
 */
package javax.arang.vcf;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @deprecated Use ToOneAnnovar
 * @author Arang Rhie
 *
 */
public class ToAnnovar extends IOwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		StringBuffer newLine;
		
		int varCounts = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("##"))	continue;
			if (line.startsWith("#CHR")) {
				// Write down the head
				newLine = new StringBuffer();
				newLine.append("CHR\tSTART\tSTOP\tREF\tALT\tID");
				tokens = line.split("\t");
				for (int i = VCF.SAMPLE; i < tokens.length; i++) {
					newLine.append("\t" + tokens[i]);
				}
				fm.writeLine(newLine.toString());
			} else {
				tokens = line.split("\t");
				newLine = new StringBuffer();
//				if (!tokens[VCF.CHROM].contains("chr")) {
//					// add "chr"
//					tokens[VCF.CHROM] = "chr" + tokens[VCF.CHROM];
//				}
				newLine.append(
						tokens[VCF.CHROM] + "\t" +
						tokens[VCF.POS] + "\t" +
						tokens[VCF.POS] + "\t" +
						tokens[VCF.REF] + "\t" +
						tokens[VCF.ALT] + "\t" +
						tokens[VCF.ID]);
				
				for (int i = VCF.SAMPLE; i < tokens.length; i++) {
					String genotype = VCF.parseGT(tokens[VCF.FORMAT], tokens[i]);
					newLine.append("\t" + genotype);
				}
				fm.writeLine(newLine.toString());
				varCounts++;
			}
		}
		System.out.println("Total of " + varCounts + " variants have been processed.");
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar vcfToAnnovar.jar <in.vcf>");
		System.out.println("\tConvert a vcf file to ANNOVAR input format.");
		System.out.println("\t<out>: <in.avinput>");
		System.out.println("\t\tGT (Genotype) 1/1 -> 2, 0/1 -> 1, 0/0 -> 0. Same for phased (|) data.");
		System.out.println("\t\t\t ./. or .|. are coded as \"NA\".");
		System.out.println("Arang Rhie, 2014-01-20. arrhie@gmail.com");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new ToAnnovar().go(args[0], args[0].replace(".vcf", ".avinput"));
		} else {
			new ToAnnovar().printHelp();
		}
	}

}
