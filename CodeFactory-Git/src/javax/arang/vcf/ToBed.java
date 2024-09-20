package javax.arang.vcf;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToBed extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		
		String line;
		String[] tokens;
		
		String chr = "";
		
		int refPos = 0;	// original reference coordinate
		int altPos = 0;	// polished reference coordinate

		int pos;	// temporal pos parsed from VCF directly
		int block = 0;	// block of no variants
		int refD;		// REF delta (variant length on ref)
		int altD;		// ALT delta (variant length on alt)
		
		String prevChr = "";
		
		FileMaker fmOriginal = new FileMaker(prefix + ".original.bed");
		FileMaker fmLifted   = new FileMaker(prefix + ".lifted.bed");
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			// skip header lines
			if (line.startsWith("#"))	continue;
			
			
			// actual "changes"
			tokens = line.split(RegExp.TAB);
			chr = tokens[VCF.CHROM];
			// new chromosome
			if (!chr.equals(prevChr)) {
				refPos = 0;
				altPos = 0;
				block  = 0;
			}
			
			pos = Integer.parseInt(tokens[VCF.POS]);
			
			refD = tokens[VCF.REF].length();
			altD = tokens[VCF.ALT].length();


			// get the block before we set refPos
			block = pos - 1 - refPos;
			
			refPos = pos - 1;
			altPos += block;
			
			outputRegion(fmOriginal, chr, refPos, refD);
			outputRegion(fmLifted,   chr, altPos, altD);

			refPos += refD;
			altPos += altD;
			
			prevChr = chr;
		}
	}

	private void outputRegion(FileMaker fm, String chr, int pos, int delta) {
		fm.writeLine(chr + "\t" + pos + "\t" + (pos + delta));
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar vcfToBed.jar in.vcf out-prefix");
		System.err.println();
		System.err.println("Get regions before / after polishing.");
		System.err.println("  in.vcf     input vcf, sorted by position. Changes in the original reference coordinate.");
		System.err.println("  out-prefix output prefix for");
		System.err.println("    * out-prefix.origin.bed region on the original reference coordinate");
		System.err.println("    * out-prefix.lifted.bed   region on the lifted reference coordinate");
		System.err.println("2022-11-04. Arang Rhie, arrhie@gmail.com");
	}

	private static String prefix = "";
	public static void main(String[] args) {
		if (args.length == 2) {
			prefix = args[1];
			new ToBed().go(args[0]);
		} else {
			new ToBed().printHelp();
		}
	}

}
