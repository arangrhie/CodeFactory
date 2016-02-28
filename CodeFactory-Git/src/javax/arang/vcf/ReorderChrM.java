package javax.arang.vcf;

import java.util.ArrayList;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ReorderChrM extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String chrMline = "";
		boolean isAfterY = false;
		boolean isAfterYvar = false;
		boolean isFirstNonHeader = true;
		ArrayList<String> chrMlines = new ArrayList<String>();
		
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("##contig=<ID=")) {
				if (line.startsWith("##contig=<ID=chrM")) {
					chrMline = line;
					if (isAfterY) {
						fm.writeLine(line);
					}
				} else {
					if (line.startsWith("##contig=<ID=chrY")) {
						isAfterY = true;
						fm.writeLine(line);
						if (!chrMline.equals("")) {
							System.out.println("[DEBUG] :: Header reordered");
							fm.writeLine(chrMline);
						}
					} else {
						fm.writeLine(line);
					}
				}
			} else if (line.startsWith("#")) {
				fm.writeLine(line);
			} else {
				if (isFirstNonHeader) {
					System.out.println("[DEBUG] :: First line of variants proceeding..");
					isFirstNonHeader = false;
				}
				tokens = line.split(RegExp.TAB);
				if (tokens[VCF.CHROM].equals("chrM")) {
					chrMlines.add(line + "\n");
				} else if (tokens[VCF.CHROM].equals("chrY")) {
					isAfterYvar = true;
					fm.writeLine(line);
				} else {
					if (isAfterYvar) {
						System.out.println("[DEBUG] :: chrM variants: " + chrMlines.size());
						for (int i = 0; i < chrMlines.size(); i++) {
							fm.write(chrMlines.get(i));
						}
						isAfterYvar = false;
					}
					fm.writeLine(line);
				}
				
			}
		}
		
		if (isAfterYvar) {
			System.out.println("[DEBUG] :: chrM variants: " + chrMlines.size());
			for (int i = 0; i < chrMlines.size(); i++) {
				fm.write(chrMlines.get(i));
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar vcfReorderChrM.jar <in.vcf> <out.vcf>");
		System.out.println("\t<in.vcf>: GATK resources hg19 bundle");
		System.out.println("\t<out.vcf>: Chromosomes sorted as chr1, chr2, chr3, ..., chr22, chrX, chrY, chrM (, ... and others)");
		System.out.println("Arang Rhie, 2015-10-28. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ReorderChrM().go(args[0], args[1]);
		} else {
			new ReorderChrM().printHelp();
		}
	}

}
