package javax.arang.vcf;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class SeperateSnpIndel extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		FileMaker fmIndel = new FileMaker(fr.getDirectory(), fr.getFileName().replace(".vcf", ".indel"));
		FileMaker fmMsnp = new FileMaker(fr.getDirectory(), fr.getFileName().replace(".vcf", ".msnp"));
		System.out.println("Into " + fmIndel.getFileName());
		System.out.println("Into " + fmMsnp.getFileName());
		
		String line;
		String[] tokens;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	{
				fm.writeLine(line);
				fmIndel.writeLine(line);
				fmMsnp.writeLine(line);
				continue;
			} else {
				tokens = line.split("\t");

				// filter multi alleleic snp
				if (tokens[VCF.ALT].contains(",")) {
					fmMsnp.writeLine(line);
					continue;
				}
				
				// filter indel
				if (tokens[VCF.REF].length() > 1 || tokens[VCF.ALT].length() > 1
						// filter out SNPs where VT=SNP
						|| tokens[VCF.INFO].contains("VT") && !VCF.parseINFO(tokens[VCF.INFO], "VT").equals("SNP")) {
					fmIndel.writeLine(line);
					continue;
				}
							
				fm.writeLine(line);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar vcfSeperateSnpIndel.jar <in.vcf>");
		System.out.println("\tThree files will be generated:");
		System.out.println("\t\t<in_snp.vcf>: vcf containing biallelic SNPs");
		System.out.println("\t\t<in_mnp.vcf>: vcf containing multiallelic SNPs. indels ");
		System.out.println("\t\t<in_indel.vcf>: vcf containing indels");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new SeperateSnpIndel().go(args[0], args[1]);
		} else {
			new SeperateSnpIndel().printHelp();
		}
	}

}
