package javax.arang.phasing;

import java.util.HashMap;

import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class Haps {

	public static final int CHR = 0;
	public static final int RS_ID = 1;
	public static final int POS = 2;
	public static final int ALLELE_A = 3;
	public static final int ALLELE_B = 4;
	public static final int HAPLOTYPE_A = 5;
	public static final int HAPLOTYPE_B = 6;
	
	public static HashMap<Integer, PhasedSNP> readHapsStoreSNPs(FileReader frHaps) {
		HashMap<Integer, PhasedSNP> snpPosToPhasedSNPmap = new HashMap<Integer, PhasedSNP>();
		String line;
		String[] tokens;
		int pos;
		PhasedSNP snp;
		String a;
		String b;
		while (frHaps.hasMoreLines()) {
			line = frHaps.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			//if (tokens[Haps.HAPLOTYPE_A].equals(tokens[Haps.HAPLOTYPE_B]))	continue;	// exclude homozygotes
			pos = Integer.parseInt(tokens[Haps.POS]);
			if (tokens[Haps.HAPLOTYPE_A].equals("0")) {
				a = tokens[Haps.ALLELE_A];
			} else {
				a = tokens[Haps.ALLELE_B];
			}
			if (tokens[Haps.HAPLOTYPE_B].equals("0")) {
				b = tokens[Haps.ALLELE_A];
			} else {
				b = tokens[Haps.ALLELE_B];
			}
			snp = new PhasedSNP(tokens[Haps.CHR], pos, a, b, tokens[Haps.POS]);
			snpPosToPhasedSNPmap.put(pos, snp);
		}
		return snpPosToPhasedSNPmap;
	}
	
}
