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
public class ToSNPtable extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		FileMaker fmIndel = new FileMaker(fr.getDirectory(), fr.getFileName().replace(".vcf", ".indel.vcf"));
		FileMaker fmMsnp = new FileMaker(fr.getDirectory(), fr.getFileName().replace(".vcf", ".msnp.vcf"));
		System.out.println("Into " + fmIndel.getFileName());
		System.out.println("Into " + fmMsnp.getFileName());
		
		String line;
		String[] tokens;
		StringBuffer newLine;
		
		boolean isSnp = true;
		boolean isMsnp = false;
		
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
				fmIndel.writeLine(newLine.toString());
			} else {
				tokens = line.split("\t");
				newLine = new StringBuffer();
//				if (!tokens[VCF.CHROM].contains("chr")) {
//					// add "chr"
//					tokens[VCF.CHROM] = "chr" + tokens[VCF.CHROM];
//				}
				isSnp = true;
				isMsnp = false;
				newLine.append(
						tokens[VCF.CHROM] + "\t" +
						tokens[VCF.POS] + "\t" +
						tokens[VCF.POS] + "\t" +
						tokens[VCF.REF] + "\t" +
						tokens[VCF.ALT] + "\t" +
						tokens[VCF.ID]);
				

				// filter multi alleleic snp
				if (tokens[VCF.ALT].contains(",")) {
					fmMsnp.writeLine(line);
					continue;
				}
				
				// filter indel
				if (tokens[VCF.REF].length() > 1 || tokens[VCF.ALT].length() > 1
						// filter out SNPs where VT=SNP
						|| tokens[VCF.INFO].contains(";VT=") && !VCF.parseINFO(tokens[VCF.INFO], "VT").equals("SNP")) {
					isSnp = false;
				}
				
				
				for (int i = VCF.SAMPLE; i < tokens.length; i++) {
					String genotype = VCF.parseGT(tokens[VCF.FORMAT], tokens[i]);
					if (genotype.equals("3")) {
						isMsnp = true;
						break;
					}
					newLine.append("\t" + genotype);
				}
				
				if (isSnp) {
					if (isMsnp) {
						// multi allelic snp
						fmMsnp.writeLine(line);
					} else {
						fm.writeLine(newLine.toString());
					}
				} else {
					fmIndel.writeLine(newLine.toString());
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar vcfToSnpTable.jar <in.vcf>");
		System.out.println("\tConvert VCF4.1 (1000G phase1 format) to SNP and INDEL table.\n" +
				"\tThis code does not looks up for FILTER field.");
		System.out.println("\t\tGT (Genotype) 1/1 -> 2, 0/1 -> 1, 0/0 -> 0. Same for phased (|) data.");
		System.out.println("\t\t\t ./. or .|. are coded as \"NA\".");
		System.out.println("Arang Rhie, 2014-11-06. arrhie@gmail.com");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new ToSNPtable().go(args[0], args[0].replace(".vcf", ".snp"));
		} else {
			new ToSNPtable().printHelp();
		}
	}

}
