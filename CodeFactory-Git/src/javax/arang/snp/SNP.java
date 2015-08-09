package javax.arang.snp;


public class SNP {
	public SNP(int pos, String alleleA, String alleleB, String id) {
		this.pos = pos;
		this.ref = alleleA;
		this.alt = alleleB;
		this.id = id;
	}

	public static String getHeaderString() {
		return "CHR\tSTART\tSTOP\tREF\tALT\tID";
	}
	
	public static final short CHR = 0;
	public static final short START = 1;
	public static final short STOP = 2;
	public static final short REF = 3;
	public static final short ALT = 4;
	public static final short ID = 5;
	public static final short SAMPLE_START = 6; 
	
	public static boolean isLowerChr(String chr1, String chr2) {
		if (getChrIntVal(chr1) < getChrIntVal(chr2)) {
			return true;
		}
		return false;
	}
	
	public static int getChrIntVal(String chr) {
		if (chr.contains("chr")) {
			chr = chr.replace("chr", "");
		}
		if (chr.equals("X"))	return 23;
		if (chr.equals("Y"))	return 24;
		if (chr.equals("M"))	return 25;
		return Integer.parseInt(chr);
	}
	
	public String chr;
	public int pos;
	public String ref;
	public String alt;
	public String id;

}
