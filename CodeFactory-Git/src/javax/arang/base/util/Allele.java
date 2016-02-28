package javax.arang.base.util;

import java.util.ArrayList;
import java.util.Collections;

public class Allele {

	private int numAlleles;
	private ArrayList<Character> alleles;
	private ArrayList<Integer> alleleDepths;
	private ArrayList<Float> alleleFrequencies;
	private ArrayList<Integer> alleleRankIndex;
	
	/***
	 * Add allele info based on Base coverage
	 * @param a
	 * @param c
	 * @param g
	 * @param t
	 * @param d
	 */
	public Allele(String a, String c, String g, String t, String d) {
		initAlleleFrequencies(a, c, g, t, d, 0.0f, 0.0f);
	}
	
	/***
	 * Add allele info based on Base coverage
	 * @param a
	 * @param c
	 * @param g
	 * @param t
	 * @param d
	 */
	public Allele(String a, String c, String g, String t, String d, float minAf, float minAfDel) {
		initAlleleFrequencies(a, c, g, t, d, minAf, minAfDel);
	}
	
	private void initAlleleFrequencies(String a, String c, String g, String t, String d, float minAf, float minAfDel) {
		int A = Integer.parseInt(a);
		int C = Integer.parseInt(c);
		int G = Integer.parseInt(g);
		int T = Integer.parseInt(t);
		int D = Integer.parseInt(d);
		int totalDepth = A + C + G + T + D;
		
		numAlleles = 0;
		alleles = new ArrayList<Character>();
		alleleDepths = new ArrayList<Integer>();
		alleleFrequencies = new ArrayList<Float>();
		alleleRankIndex = new ArrayList<Integer>();
		
		ArrayList<Integer> sortedAlleleDepth = new ArrayList<Integer>();
		
		float af = (float) A / (float) totalDepth;
		if (A > 0 && af >= minAf) {
			alleles.add('A');
			alleleFrequencies.add((float) A / (float) totalDepth);
			numAlleles++;
			alleleDepths.add(A);
			sortedAlleleDepth.add(A);
		}
		
		af = (float) C / (float) totalDepth;
		if (C > 0 && af >= minAf) {
			alleles.add('C');
			alleleFrequencies.add(af);
			numAlleles++;
			alleleDepths.add(C);
			sortedAlleleDepth.add(C);
		}
		
		af = (float) G / (float) totalDepth;
		if (G > 0 && af >= minAf) {
			alleles.add('G');
			alleleFrequencies.add(af);
			numAlleles++;
			alleleDepths.add(G);
			sortedAlleleDepth.add(G);
		}
		
		af = (float) T / (float) totalDepth;
		if (T > 0 && af >= minAf) {
			alleles.add('T');
			alleleFrequencies.add(af);
			numAlleles++;
			alleleDepths.add(T);
			sortedAlleleDepth.add(T);
		}
		
		af = (float) D / (float) totalDepth;
		if (D > 0 && af >= minAfDel) {
			alleles.add('D');
			alleleFrequencies.add(af);
			numAlleles++;
			alleleDepths.add(D);
			sortedAlleleDepth.add(D);
		}
		
		
		Collections.sort(sortedAlleleDepth);
		for (int i = numAlleles - 1; i >= 0 ; i--) {
			alleleRankIndex.add(alleleDepths.indexOf(sortedAlleleDepth.get(i)));
		}
	}
	
	public int getNumAlleles() {
		return numAlleles;
	}
	
	public ArrayList<Character> getAlleles() {
		return alleles;
	}
	
	public String getAllelesInString() {
		String alleles = this.alleles.get(0) + "";
		if (numAlleles > 1) {
			for (int i = 1; i < numAlleles; i++) {
				alleles = alleles + "|" + this.alleles.get(i);
			}
		}
		return alleles;
	}
	
	public ArrayList<Float> getAlleleFrequencies() {
		return alleleFrequencies;
	}
	
	public char getMaxAlleleFreqBase() {
		return alleles.get(alleleRankIndex.get(0));
	}
	
	/***
	 * check for numAlleles > 1 before running this code
	 * @return
	 */
	public char getSecondMaxAlleleFreqBase() {
		return alleles.get(alleleRankIndex.get(1));
	}
	
	public float getMaxAlleleFreq() {
		return alleleFrequencies.get(alleleRankIndex.get(0));
	}
	
	public String getAlleleDepthsInString() {
		String depths = this.alleleDepths.get(0) + "";
		if (numAlleles > 1) {
			for (int i = 1; i < numAlleles; i++) {
				depths = depths + "|" + this.alleleDepths.get(i);
			}
		}
		return depths;
	}
	
	/***
	 * check for numAlleles > 1 before running this code
	 * @return
	 */
	public float getSecondMaxAlleleFreq() {
		return alleleFrequencies.get(alleleRankIndex.get(1));
	}

}
