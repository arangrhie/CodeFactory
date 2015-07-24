package javax.arang.vcf;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ExtractAA extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		String aa;
		String[] aaTokens;
		
		fm.writeLine("#CHR\tStart\tStop\tAA\tREF\tALT\tIndelType");
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			// skip header lines
			if (line.startsWith("#"))	continue;
			aa = VCF.parseINFO(tokens[VCF.INFO], "AA");
			aaTokens = aa.split("|");
			fm.write(tokens[VCF.CHROM] + "\t" + tokens[VCF.POS] + "\t" + tokens[VCF.POS] + "\t" + aaTokens[1]);
			if (aa.length() > 4) {
				for (int i = 2; i < aaTokens.length; i++) {
					if (aaTokens.equals("|"))	continue;
					fm.write("\t" + aaTokens[i]);
				}
			}
			fm.writeLine();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("usage: java -jar vcfExtractAA.jar <in.vcf>");
		System.out.println("Extract AA sites, definded in 1KG:");
		System.out.println("\tAA|REF|ALT|IndelType. AA: Ancestral allele, REF:Reference Allele, ALT:Alternate Allele, IndelType:Type of Indel (REF, ALT and IndelType are only defined for indels");
		System.out.println("\t<out>: <in_AA.bed>");
		System.out.println("\t\tCHR\tStart\tStop\tAA\t[REF\tALT\tIndelType]");
		System.out.println("Arang Rhie, 2014-11-28. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ExtractAA().go(args[0], args[0].replace(".vcf", "_AA.bed"));
		} else {
			new ExtractAA().printHelp();
		}
	}

}
