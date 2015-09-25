package javax.arang.phasing.util;

import javax.arang.IO.basic.FileMaker;

public class PhasedSNPBase {
	public static final String header =
			"#CHR\tPOS\tHapA\tHapB\tPS\tH\tA\tB\tO\t"
			+ "|\t"
			+ "a\tc\tg\tt\tn\tD\t"
			+ "|\t"
			+ "FIRST_A\tFIRST_B\tHH\tAA\tBB\t"
			+ "BA\tAB\tHAorAH\tOAorAO\t"
			+ "HBorBH\tOBorBO\t"
			+ "OHorHO\tOO\tSINGLE";
	
	public static final short CHR = 0;
	public static final short POS = 1;
	public static final short HAPLOTYPE_A = 2;
	public static final short HAPLOTYPE_B = 3;
	public static final short PS = 4;
	public static final short OFFSET = 5;
	
	public static final short H = 0;	// Homozygote SNP
	public static final short A = 1;
	public static final short B = 2;
	public static final short O = 3;
	
	public static final short a = 4;	// Base a
	public static final short c = 5;	// Base c
	public static final short g = 6;	// Base g
	public static final short t = 7;	// Base t
	public static final short n = 8;
	public static final short D = 9;	// Deletion
	
	public static final short FIRST_A = 10;
	public static final short FIRST_B = 11;
	
	public static final short HH = 12;
	public static final short AA = 13;
	public static final short BB = 14;
	
	public static final short BA = 15;
	public static final short AB = 16;
	
	public static final short HA_or_AH = 17;
	public static final short OA_or_AO = 18;
	
	public static final short HB_or_BH = 19;
	public static final short OB_or_BO = 20;
	
	public static final short OH_or_HO = 21;
	public static final short OO = 21;
	public static final short SINGLE = 22;
	
	public static final short SIZE = 23;
	
	public static void writeSNP(FileMaker fm, String[] tokens, String note) {
		fm.write(tokens[CHR]);
		for (int i = POS; i < PS; i++) {
			fm.write("\t" + tokens[i]);
		}
		fm.write("\t" + note);
		writeInfo(fm, tokens);
	}

	public static void writeInfo(FileMaker fm, String[] tokens) {
		for (int i = PhasedSNPBase.OFFSET + PhasedSNPBase.H; i < tokens.length; i++) {
			fm.write("\t" + tokens[i]);
		}
		fm.writeLine();
	}

	
}
