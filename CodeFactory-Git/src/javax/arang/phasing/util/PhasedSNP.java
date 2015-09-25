package javax.arang.phasing.util;

import java.util.HashMap;

import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class PhasedSNP {

	public static final short CHR = 0;
	public static final short POS = 1;
	public static final short HAPLOTYPE_A = 2;
	public static final short HAPLOTYPE_B = 3;
	public static final short PS = 4;
	public static final int OFFSET = 5;
	public static final int IS_FIRST_A = 0;
	public static final int IS_FIRST_B = 1;
	public static final int NOT_SWITCHED_FROM_PREV_A_AND_IS_A = 2;
	public static final int NOT_SWITCHED_FROM_PREV_B_AND_IS_B = 3;
	public static final int SWITCHED_FROM_PREV_B_TO_A = 4;
	public static final int SWITCHED_FROM_PREV_A_TO_B = 5;
	public static final int IS_SINGLE_A = 6;
	public static final int IS_SINGLE_B = 7;
	public static final int IS_UNDETERMINABLE_HOMO = 8;
	public static final int FILTER = 9;
	
	private String chr;
	private int pos;
	private String haplotypeA;
	private String haplotypeB;
	private String ps;
	private String filter;
	private boolean isPSset = false;
	
	public PhasedSNP(String chr, int pos, String haplotypeA, String haplotypeB,
			String PS) {
		this.chr = chr;
		this.setPos(pos);
		this.setHaplotypeA(haplotypeA);
		this.setHaplotypeB(haplotypeB);
		this.ps = PS;
	}
	
	public PhasedSNP(String chr, int pos, String haplotypeA, String haplotypeB,
			String PS, String filter) {
		this.chr = chr;
		this.setPos(pos);
		this.setHaplotypeA(haplotypeA);
		this.setHaplotypeB(haplotypeB);
		this.ps = PS;
		this.filter = filter;
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
	
	public boolean isHom() {
		return this.haplotypeA.equals(this.haplotypeB); 
	}
	
	public String getFilter() {
		return this.filter;
	}
	
	public static HashMap<Integer, PhasedSNP> readSNPsStoreSNPs(FileReader frSNPs, boolean isPhased) {
		HashMap<Integer, PhasedSNP> snpPosToPhasedSNPmap = new HashMap<Integer, PhasedSNP>();
		String line;
		String[] tokens;
		int pos;
		PhasedSNP snp;
		String a;
		String b;
		while (frSNPs.hasMoreLines()) {
			line = frSNPs.readLine();
			if (line.startsWith("#")) continue;
			tokens = line.split(RegExp.WHITESPACE);
			pos = Integer.parseInt(tokens[PhasedSNP.POS]);
			a = tokens[PhasedSNP.HAPLOTYPE_A];
			b = tokens[PhasedSNP.HAPLOTYPE_B];
			if (isPhased) {
				snp = new PhasedSNP(tokens[PhasedSNP.CHR], pos, a, b, tokens[PhasedSNP.PS]);
			} else {
				snp = new PhasedSNP(tokens[PhasedSNP.CHR], pos, a, b, tokens[PhasedSNP.POS]);
			}
			snpPosToPhasedSNPmap.put(pos, snp);
		}
		return snpPosToPhasedSNPmap;
	}
}
