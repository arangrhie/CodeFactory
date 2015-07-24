package javax.arang.vcf;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ToOneAnnovar extends IOwrapper {


	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		int countMulti = 0;
		int countMultiSNP = 0;
		int countMultiIndel = 0;
		int countBiSNP = 0;
		int countBiIndel = 0;
		
		StringBuffer newLine = new StringBuffer();;
		
		String line;
		String[] tokens;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("##"))	continue;

			tokens = line.split("\t");
			if (tokens.length < VCF.SAMPLE)	break;
			
			if (line.startsWith("#CHR")) {
				newLine.append("CHR\tSTART\tSTOP\tREF\tALT\tID");
				for (int i = VCF.SAMPLE; i < tokens.length; i++) {
					newLine.append("\t" + tokens[i]);
				}
				fm.writeLine(newLine.toString());
			} else {
				int STOP = Integer.parseInt(tokens[VCF.POS]);
				
				// write multiple alleles
				if (tokens[VCF.ALT].contains(",")) {
					countMulti++;
					String[] multiAlleles = tokens[VCF.ALT].split(",");
					int numAlleles = multiAlleles.length;
					HashMap<String, StringBuffer> genotypes = new HashMap<String, StringBuffer>();
					for (int i = 0; i < numAlleles; i++) {
						if (tokens[VCF.REF].length() > 1 || multiAlleles[i].length() > 1) {
							countMultiIndel++;
							STOP += tokens[VCF.REF].length() - 1;
						} else {
							countMultiSNP++;
						}
						StringBuffer genotype = new StringBuffer(
								tokens[VCF.CHROM] + "\t" +
										tokens[VCF.POS] + "\t" +
										STOP + "\t" +
										tokens[VCF.REF] + "\t" +
										multiAlleles[i] + "\t" +
										tokens[VCF.ID]);
						genotypes.put(multiAlleles[i], genotype);
					}
					for (int i = VCF.SAMPLE; i < tokens.length; i++) {
						for (int j = 0; j < numAlleles; j++) {
							genotypes.put(multiAlleles[j], genotypes.get(multiAlleles[j]).append("\t" + VCF.parseGT(tokens[VCF.FORMAT], tokens[i], (j + 1))));
						}
					}
					for (int i = 0; i < numAlleles; i++) {
						fm.writeLine(genotypes.get(multiAlleles[i]).toString());
					}
				} else {
					// write biallelic sites
					if (tokens[VCF.ALT].length() == 1 && tokens[VCF.REF].length() == 1)	countBiSNP++;
					else {
						STOP += tokens[VCF.REF].length() - 1;
						countBiIndel++;
					}
					newLine.append(
							tokens[VCF.CHROM] + "\t" +
							tokens[VCF.POS] + "\t" +
							STOP + "\t" +
							tokens[VCF.REF] + "\t" +
							tokens[VCF.ALT] + "\t" +
							tokens[VCF.ID]);
					for (int i = VCF.SAMPLE; i < tokens.length; i++) {
						String genotype = VCF.parseGT(tokens[VCF.FORMAT], tokens[i]);
						newLine.append("\t" + genotype);
					}
					fm.writeLine(newLine.toString());
				}
			}
			newLine = new StringBuffer();
		}
		System.out.println("Bi-Allelic Sites: " + (countBiSNP + countBiIndel));
		System.out.println("\tBi-Allelic SNP: " + countBiSNP);
		System.out.println("\tBi-Allelic INDEL: " + countBiIndel);
		System.out.println("Multi-Allelic Sites: " + countMulti);
		System.out.println("\tMulti-Allelic SNP: " + countMultiSNP);
		System.out.println("\tMulti-Allelic INDEL: " + countMultiIndel);
	}


	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar vcfToOneAnnovar.jar <in.vcf>");
		System.out.println("\t<in.vcf>: bi- or multi- allelic indel or snp containing vcf file");
		System.out.println("\t<output>: <in.vcf.ann>.");
		System.out.println("\t\tGT (Genotype) 1/1 -> 2, 0/1 -> 1, 0/0 -> 0. Same for phased (|) data.");
		System.out.println("\t\t\t ./. or .|. are coded as \"NA\".");
		System.out.println("\t\tSites with multiple altered alleles will be appearing in different lines.");
		System.out.println("\t\t\"3\" will be applied if the genotype is in a different line.");
		System.out.println("Arang Rhie, 2014-11-21. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ToOneAnnovar().go(args[0], args[0] + ".ann");
		} else {
			new ToOneAnnovar().printHelp();
		}
	}

}
