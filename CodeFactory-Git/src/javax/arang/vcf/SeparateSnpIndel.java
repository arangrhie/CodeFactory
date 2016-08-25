package javax.arang.vcf;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class SeparateSnpIndel extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fmSnp) {
		FileMaker fmIndel = new FileMaker(fr.getDirectory(), fr.getFileName().replace(".vcf", ".indel.vcf"));
		FileMaker fmMsnp = new FileMaker(fr.getDirectory(), fr.getFileName().replace(".vcf", ".msnp.vcf"));
		FileMaker fmMindel = new FileMaker(fr.getDirectory(), fr.getFileName().replace(".vcf", ".mindel.vcf"));
		FileMaker fmSV = new FileMaker(fr.getDirectory(), fr.getFileName().replace(".vcf", ".sv.vcf"));
		System.out.println("Into " + fmIndel.getFileName());
		System.out.println("Into " + fmMsnp.getFileName());
		System.out.println("Into " + fmMindel.getFileName());
		System.out.println("Into " + fmSV.getFileName());
		
		String line;
		String[] tokens;
		String[] alteredAlleles;
		
		READ_LINE: while (fr.hasMoreLines()) {
			line = fr.readLine();
			
			// Write header lines
			if (line.startsWith("#"))	{
				fmSnp.writeLine(line);
				fmIndel.writeLine(line);
				fmMsnp.writeLine(line);
				fmMindel.writeLine(line);
				fmSV.writeLine(line);
				continue;
			} else {
				tokens = line.split("\t");

				// CNVs
				if (tokens[VCF.ALT].contains("<")) {
					fmSV.writeLine(line);
					continue;
				}
				
				
				// filter multi allelic sites
				if (tokens[VCF.ALT].contains(",")) {
					// ref is indel
					if (tokens[VCF.REF].length() > 1) {
						fmMindel.writeLine(line);
						continue READ_LINE;
					}
					
					// alt is indel
					alteredAlleles = tokens[VCF.ALT].split(",");
					for (String allele : alteredAlleles) {
						if (allele.length() > 1) {
							fmMindel.writeLine(line);
							continue READ_LINE;
						}
					}
					
					// mSNP
					fmMsnp.writeLine(line);
					continue READ_LINE;
				}
				
				// filter indel
				if (tokens[VCF.REF].length() > 1 || tokens[VCF.ALT].length() > 1
						// filter out SNPs where VT=SNP
						|| tokens[VCF.INFO].contains("VT")
						&& !VCF.parseINFO(tokens[VCF.INFO], "VT").equals("SNP")) {
					fmIndel.writeLine(line);
					continue;
				}
				fmSnp.writeLine(line);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar vcfSeparateSnpIndelSV.jar <in.vcf> [out.snp.vcf]");
		System.out.println("\tFile will be split into:");
		System.out.println("\t\t<in.snp.vcf>: vcf containing biallelic SNPs");
		System.out.println("\t\t<in.indel.vcf>: vcf containing indels");
		System.out.println("\t\t<in.msnp.vcf>: vcf containing multiallelic SNPs. indels ");
		System.out.println("\t\t<in.mindel.vcf>: vcf containing multiallelic indels");
		System.out.println("\t\t<in.sv.vcf>: vcf containing SVs");
		System.out.println("Arang Rhie, 2016-07-12. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new SeparateSnpIndel().go(args[0], args[0].replace(".vcf", ".snp.vcf"));
		} else if (args.length == 2) {
			new SeparateSnpIndel().go(args[0], args[1]);
		} else {
			new SeparateSnpIndel().printHelp();
		}
	}

}
