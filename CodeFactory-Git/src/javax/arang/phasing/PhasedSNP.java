package javax.arang.phasing;

public class PhasedSNP {

	public static short CHR = 0;
	public static short POS = 1;
	public static short HAPLOTYPE_A = 2;
	public static short HAPLOTYPE_B = 3;
	public static short PS = 4;
	
	private String chr;
	private int pos;
	private String haplotypeA;
	private String haplotypeB;
	private String ps;
	private boolean isPSset = false;
	
	public PhasedSNP(String chr, int pos, String haplotypeA, String haplotypeB,
			String PS) {
		this.chr = chr;
		this.setPos(pos);
		this.setHaplotypeA(haplotypeA);
		this.setHaplotypeB(haplotypeB);
		this.ps = PS;
	}
	
	public PhasedSNP(String chr, int pos, String haplotypeA, String haplotypeB) {
		this.chr = chr;
		this.setPos(pos);
		this.setHaplotypeA(haplotypeA);
		this.setHaplotypeB(haplotypeB);
	}

	public String getChr() {
		return this.chr;
	}

	public String getHaplotypeA() {
		return haplotypeA;
	}

	public void setHaplotypeA(String haplotypeA) {
		this.haplotypeA = haplotypeA;
	}

	public String getHaplotypeB() {
		return haplotypeB;
	}

	public void setHaplotypeB(String haplotypeB) {
		this.haplotypeB = haplotypeB;
	}

	public String getPS() {
		return ps;
	}

	public void setPS(String ps) {
		this.ps = ps;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public void setPSset(boolean b) {
		this.isPSset  = b;
	}
	
	public boolean isPSset() {
		return this.isPSset;
	}
}
