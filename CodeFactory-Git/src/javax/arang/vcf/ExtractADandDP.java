package javax.arang.vcf;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ExtractADandDP extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			// skip header lines
			if (line.startsWith("##")) continue;
			
			tokens = line.split(RegExp.TAB);
			
			// keep sample info in the new header line
			if (line.startsWith("#CHROM")) {
				System.out.print("CHROM\tPOS");
				for (int i = VCF.SAMPLE; i < tokens.length; i++) {
					String sample = tokens[i];
					System.out.print("\t" + sample + "_AD_REF" + "\t" + sample + "_DP");
				}
				System.out.println();
				continue;
			}
			
			// for each variants
			System.out.print(tokens[VCF.CHROM] + "\t" + tokens[VCF.POS]);
			for (int i = VCF.SAMPLE; i < tokens.length; i++) {
				String sample = tokens[i];
				String ref = VCF.parseSAMPLE(tokens[VCF.FORMAT], "AD", sample).split(RegExp.COMMA)[0];
				if (ref.equals(".")) ref = "0";
				String dp  = VCF.parseSAMPLE(tokens[VCF.FORMAT], "DP", sample);
				if (dp.equals("."))  dp = "0";
				System.out.print("\t" + ref + "\t" + dp);
			}
			System.out.println();
		}
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar vcfExtractADandDP.jar in.vcf");
		System.err.println();
		System.err.println("Extract position, reference allelic depth (first AD) and total depth (DP) per samples");
		System.err.println("  in.vcf regular VCF file");
		System.err.println("  sysout CHROM <tab> POS [<tab> AD(REF) <tab> DP]+. First line contains header.");
		System.err.println("    \".\" will become \"0\".");
		System.err.println("Arang Rhie, 2022-11-18. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ExtractADandDP().go(args[0]);
		} else {
			new ExtractADandDP().printHelp();
		}
	}

}
