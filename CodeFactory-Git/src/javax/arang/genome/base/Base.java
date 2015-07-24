package javax.arang.genome.base;

public class Base {
	
	public final static int CHR = 0;
	public final static int POS = 1;
	public final static int REF = 2;
	public final static int COUNT_A = 3;
	public final static int COUNT_C = 4;
	public final static int COUNT_G = 5;
	public final static int COUNT_T = 6;
	public final static int QUAL_AVG = 7;
	public final static int TOTAL_COUNT = 8;
	
	public final static int REF_COUNT = 0;
	public final static int ALLELE_BASE1 = 1;
	public final static int ALLELE_BASE2 = 2;
	
	public String refChr = null;
	public int pos;
	public char ref;
	
	private int qual = 0;
	public int Acount = 0;
	public int Ccount = 0;
	public int Gcount = 0;
	public int Tcount = 0;
	
	
	
	public int getTotalBaseCount() {
		return Acount + Ccount + Gcount + Tcount;
	}
	
	public void addQual(int qualAdd) {
		qual += qualAdd;
	}
	
	public float getQualAvg() {
		return (float) qual / getTotalBaseCount();
	}
	
	public static String getAlleleBase(int base) {
		switch(base) {
		case COUNT_A: return "A";
		case COUNT_C: return "C";
		case COUNT_G: return "G";
		case COUNT_T: return "T";
		}
		return "N";
	}
	
	
	public static int[] getAllelCount(String refBase,
			int Acount, int Ccount,
			int Gcount, int Tcount) {
		int[] counts = new int[3];
		if (refBase.equals("A")) {
			counts[REF_COUNT] = Acount;
			int maxIdx = getMaxIdx(Ccount, Gcount, Tcount);
			switch(maxIdx) {
			case 1: counts[ALLELE_BASE1] = COUNT_C;	break;
			case 2: counts[ALLELE_BASE1] = COUNT_G;	break;
			case 3: counts[ALLELE_BASE1] = COUNT_T;	break;
			case 4: counts[ALLELE_BASE1] = COUNT_C;
					counts[ALLELE_BASE2] = COUNT_G;	break;
			case 5:	counts[ALLELE_BASE1] = COUNT_G;
					counts[ALLELE_BASE2] = COUNT_T;	break;
			case 6: counts[ALLELE_BASE1] = COUNT_C;
					counts[ALLELE_BASE2] = COUNT_T;	break;
			}
			return counts;
		}
		if (refBase.equals("C")) {
			counts[REF_COUNT] = Ccount;
			int maxIdx = getMaxIdx(Acount, Gcount, Tcount);
			switch(maxIdx) {
			case 1: counts[ALLELE_BASE1] = COUNT_A;	break;
			case 2: counts[ALLELE_BASE1] = COUNT_G;	break;
			case 3: counts[ALLELE_BASE1] = COUNT_T;	break;
			case 4: counts[ALLELE_BASE1] = COUNT_A;
					counts[ALLELE_BASE2] = COUNT_G;	break;
			case 5:	counts[ALLELE_BASE1] = COUNT_G;
					counts[ALLELE_BASE2] = COUNT_T;	break;
			case 6: counts[ALLELE_BASE1] = COUNT_A;
					counts[ALLELE_BASE2] = COUNT_T;	break;
			}
			return counts;
		}
		if (refBase.equals("G")) {
			counts[REF_COUNT] = Gcount;
			int maxIdx = getMaxIdx(Ccount, Acount, Tcount);
			switch(maxIdx) {
			case 1: counts[ALLELE_BASE1] = COUNT_C;	break;
			case 2: counts[ALLELE_BASE1] = COUNT_A;	break;
			case 3: counts[ALLELE_BASE1] = COUNT_T;	break;
			case 4: counts[ALLELE_BASE1] = COUNT_C;
					counts[ALLELE_BASE2] = COUNT_A;	break;
			case 5:	counts[ALLELE_BASE1] = COUNT_A;
					counts[ALLELE_BASE2] = COUNT_T;	break;
			case 6: counts[ALLELE_BASE1] = COUNT_C;
					counts[ALLELE_BASE2] = COUNT_T;	break;
			}
			return counts;
		}
		if (refBase.equals("T")) {
			counts[REF_COUNT] = Tcount;
			int maxIdx = getMaxIdx(Ccount, Gcount, Acount);
			switch(maxIdx) {
			case 1: counts[ALLELE_BASE1] = COUNT_C;	break;
			case 2: counts[ALLELE_BASE1] = COUNT_G;	break;
			case 3: counts[ALLELE_BASE1] = COUNT_A;	break;
			case 4: counts[ALLELE_BASE1] = COUNT_C;
					counts[ALLELE_BASE2] = COUNT_G;	break;
			case 5:	counts[ALLELE_BASE1] = COUNT_G;
					counts[ALLELE_BASE2] = COUNT_A;	break;
			case 6: counts[ALLELE_BASE1] = COUNT_A;
					counts[ALLELE_BASE2] = COUNT_C;	break;
			}
			return counts;
		}
		return counts;
	}

	private static int getMaxIdx(int count1, int count2, int count3) {
		// count1 >= count2
		if (count1 >= count2) {
			if (count1 > count3) {
				if (count1 == count2) {
					// count1 == count2 > count3
					return 4;
				}
				// count1 > count2, count3
				return 1;
			} else {
				if (count1 == count3) {
					// count1 == count3 > count2
					return 6;
				}
				// count3 > count1 > count2
				return 3;
			}
		}
		// count2 > count1
		else {
			if (count2 > count3) {
				// count2 > count1, count3
				return 2;
			} else {
				if (count2 == count3) {
					return 5;
				}
				// count3 > count2 > count1 
				return 3;
			}
		}
	}
}
