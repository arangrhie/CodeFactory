package javax.arang.phasing.TenX;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.vcf.VCF;

public class HolesInPhasedBlock extends IOwrapper {

	private static final int CHR = 0;
	private static final int POS = 1;
	private static final int FORMAT = 2;
	private static final int SAMPLE = 3;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;

		//String prevChr = "";
		String chr;
		int pos;
		String gt;
		String prevPS = "";
		String ps;
		boolean hasUnphasedHet = false;
		int start = -1;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			chr = tokens[CHR];
			pos = Integer.parseInt(tokens[POS]);
			gt = VCF.parseSAMPLE(tokens[FORMAT], "GT", tokens[SAMPLE]);
			ps = VCF.parseSAMPLE(tokens[FORMAT], "PS", tokens[SAMPLE]);
			if (gt.equals("1|1"))	continue;
			
			if (prevPS.equals(ps)) {
				// Within ps block
				if (gt.contains("|")) {
					if (start > -1) {
						if (hasUnphasedHet) {
							fm.writeLine(chr + "\t" + (start - 1) + "\t" + pos + "\t" + prevPS);
						}
						hasUnphasedHet = false;
					}
					start = pos;
				} else {
					if (start > -1) {
						hasUnphasedHet = true;
					}
				}
				
			} else {
				// PS changed
				hasUnphasedHet = false;
				start = -1;
				if (gt.contains("|")) {
					start = pos;
				}
			}
			//prevChr = chr;
			prevPS = ps;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasing10xHolesInPhasedBlock.jar <in.tdf> <out.bed>");
		System.out.println("\t<in.tdf>: CHR\tPOS\tFORMAT\tGT:PS");
		System.out.println("\t<out.bed>: Makes a bed file for unphased variants within same PS block.");
		System.out.println("Arang Rhie, 2016-01-20. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new HolesInPhasedBlock().go(args[0], args[1]);
		} else {
			new HolesInPhasedBlock().printHelp();
		}
	}

}
