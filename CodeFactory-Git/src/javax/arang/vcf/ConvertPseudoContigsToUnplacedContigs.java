package javax.arang.vcf;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ConvertPseudoContigsToUnplacedContigs extends I2Owrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar vcfConvertPseudoContigsToUnplacedContigs.jar <in.map> <vcf_to_convert.vcf> <out.vcf>");
		System.out.println("\t<in.map>: CONTIG\tCONTIG_LEN");
		System.out.println("\t\tThis file must be written in the contig merged order. 100bp insertion of N's is assumed.");
		System.out.println("\t<out.vcf>: CHROM will be written as Unplaced_contigs");
		System.out.println("\t<vcf_to_convert.vcf>: vcf file to convert");
		System.out.println("\t\tAll contigs < 10kb will be droped-out.");
		System.out.println("Arang Rhie, 2015-11-16. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new ConvertPseudoContigsToUnplacedContigs().go(args[0], args[1], args[2]);
		} else {
			new ConvertPseudoContigsToUnplacedContigs().printHelp();
		}
	}

	@Override
	public void hooker(FileReader frMap, FileReader frVCF, FileMaker fmVCF) {
		String line;
		String[] tokens;
		
		HashMap<String, Integer> contigToOffset = new HashMap<String, Integer>();
		int offset = 0;
		
		while (frMap.hasMoreLines()) {
			line = frMap.readLine();
			tokens = line.split(RegExp.TAB);
			System.out.println("[DEBUG] :: Adding (" + tokens[0] + ", " + offset + ")");
			contigToOffset.put(tokens[0], offset);
			offset = offset + 100 + Integer.parseInt(tokens[1]);
		}
		
		System.out.println("contigToOffset.size() " + contigToOffset.size());
		
		String annoContig;
		int pos;
		int numContigsConverted = 0;
		int annoContigLen;
		while (frVCF.hasMoreLines()) {
			line = frVCF.readLine();
			if (line.startsWith("##")) {
				if (line.startsWith("##contig")) {
					annoContig = line.substring(line.indexOf("ID=") + 3, line.indexOf(","));
					if (!contigToOffset.containsKey(annoContig)) {
						annoContigLen = Integer.parseInt(line.substring(line.lastIndexOf("=") + 1, line.length() - 1));
						if (annoContigLen >= 10000) {
							fmVCF.writeLine(line);
						}
					} else {
						System.out.print(" " + annoContig);
						numContigsConverted++;
					}
				} else if (line.startsWith("##reference")) {
					System.out.println();
					fmVCF.writeLine("##contig=<ID=Unplaced_contigs,length=" + (offset - 100) + ">");
					System.out.println("##contig=<ID=Unplaced_contigs,length=" + (offset - 100) + ">");
					fmVCF.writeLine(line);
				} else {
					fmVCF.writeLine(line);
				}
			} else if (line.startsWith("#CHROM")){
				fmVCF.writeLine(line);
			} else {				
				// Variant
				tokens = line.split(RegExp.TAB);
				annoContig = tokens[VCF.CHROM];
				pos = Integer.parseInt(tokens[VCF.POS]);
				fmVCF.write("Unplaced_contigs" + "\t" + (pos +  contigToOffset.get(annoContig)));
				for (int i = VCF.ID; i < tokens.length; i++) {
					fmVCF.write("\t" + tokens[i]);
				}
				fmVCF.writeLine();
			}
		}
		System.out.println("[DEBUG] :: numContigsConverted = " + numContigsConverted);
	}

}
