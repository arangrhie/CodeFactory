package javax.arang.genome.indel;

import javax.arang.IO.basic.FileMaker;

public class Indel {
	public String chr = "";
	public int start = 0;
	public int end = 0;
	public String ref = "";
	public String obs = "";
	public int count = 0;
	public String type = "";
	public float qa = 0f;
	
	public final static String NON_OBS = "0";
	
	public final static int CHR = 0;
	public final static int START = 1;
	public final static int END = 2;
	public final static int REF = 3;
	public final static int OBS = 4;
	public final static int TYPE = 5;
	public final static int QUAL_AVG = 6;
	public final static int TOTAL_COUNT = 7;
	public final static int BASE_QUAL_AVG = 8;
	public final static int BASE_COUNT_AVG = 9;
	public final static int FREQUENCY = 10;
	public final static int GENOTYPE = 11;

	public final static int NOTE = 5;

	public static String getGenotype(String genotype) {
		if (genotype.equals("Het")) {
			return "1";
		} else if (genotype.equals("Hom")) {
			return "2";
		} else {
			return "0";
		}
	}

	public static void writeLine(FileMaker fm, String chr,
			int posStart, int posEnd, String ref, String obs,
			String genotype1, String genotype2) {
			fm.writeLine(chr + "\t" + posStart + "\t" + posEnd + "\t"
					+ ref + "\t" + obs + "\t"
					+ genotype1 + "\t" + genotype2);
	}
	
	public static void writeLine(FileMaker fm, String line, String genotype) {
		fm.writeLine(line + "\t" + genotype);
	}
}
