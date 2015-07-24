package org.gmi.fx.util;

import java.io.Serializable;

public class Exome implements Serializable {
/**
	 * 
	 */
	private static final long serialVersionUID = 5050754536297799393L;
	private String geneID;
	private String chrom;
//	private int geneSize;
	private int exonCount;
	private int[] exonStarts;
	private int[] exonEnds;
	private int geneLen;
	
//	public Exome(String geneID, String chrom, int geneSize, int exonCount, int[] exonStarts, int[] exonEnds) {
//		this.geneID = geneID;
//		this.chrom = chrom;
//		this.geneSize = geneSize;
//		this.exonCount = exonCount;
//		this.exonStarts = exonStarts;
//		this.exonEnds = exonEnds;
//	}
	
	public Exome(String geneID, String chrom, int exonCount, int[] exonStarts, int[] exonEnds, int geneLen) {
		this.geneID = geneID;
		this.chrom = chrom;
		this.exonCount = exonCount;
		this.exonStarts = exonStarts;
		this.exonEnds = exonEnds;
		this.geneLen = geneLen;
	}
	
	public String getGeneId() {
		return geneID;
	}
	
	/**
	 * @return the chromosome
	 */
	public String getChrom() {
		return chrom;
	}

	/**
	 * @return the exonCount
	 */
	public int getExonCount() {
		return exonCount;
	}

	/**
	 * @return the exonStarts
	 */
	public int[] getExonStarts() {
		return exonStarts;
	}

	/**
	 * @return the exonEnds
	 */
	public int[] getExonEnds() {
		return exonEnds;
	}
	
	public int getGeneLen() {
		return geneLen;
	}
}
