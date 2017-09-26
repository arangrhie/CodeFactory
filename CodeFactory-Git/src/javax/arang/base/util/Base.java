package javax.arang.base.util;

import javax.arang.sam.BaseDepth;

public class Base {
	public static final short CHR = 0;
	public static final short POS = 1;
	public static final short A = 2;
	public static final short C = 3;
	public static final short G = 4;
	public static final short T = 5;
	public static final short D = 6;
	
	/***
	 * Get the maximum likely base at pos.
	 *  When depth==0, N will be returned.
	 *  When max < D, D will be returned.
	 *  When multiple alleles are having the same maximum depth, those alleles will be returned.
	 * @param chr
	 * @param pos
	 * @param a
	 * @param c
	 * @param g
	 * @param t
	 * @param d
	 * @return
	 */
	public static String maxLikelyBase(String chr, int pos, String a, String c, String g,
			String t, String d) {
		int A = Integer.parseInt(a);
		int C = Integer.parseInt(c);
		int G = Integer.parseInt(g);
		int T = Integer.parseInt(t);
		int D = Integer.parseInt(d);
		int max = Math.max(Math.max(A, C), Math.max(G, T));
		String base = "";
		if (max < D && D > 0) {
			return "D";
		} else {
			if (max == 0) {
				return "N";
			}
			if (max > 0) {
				if (max == A) {
					base = base + "A";
				}
				if (max == C) {
					base = base + "C";
				}
				if (max == G) {
					base = base + "G";
				}
				if (max == T) {
					base = base + "T";
				}
				if (max == D) {
					base = base + "D";
				}
				return base;
			}
		}
		return "N";
	}
	
	public static int maxLikelyBaseCov(String a, String c, String g,
			String t, String d) {
		int A = Integer.parseInt(a);
		int C = Integer.parseInt(c);
		int G = Integer.parseInt(g);
		int T = Integer.parseInt(t);
		int D = Integer.parseInt(d);
		int max = Math.max(Math.max(A, C), Math.max(G, T));
		if (max < D && D > 0) {
			return D;
		} else {
			return max;
		}
	}
	
	public static int getTotalDepth(String a, String c, String g, String t, String d) {
		return (Integer.parseInt(a)
				+ Integer.parseInt(c)
				+ Integer.parseInt(g)
				+ Integer.parseInt(t)
				+ Integer.parseInt(d));
	}
	
	/***
	 * baseDepth from samBaseDepth.jar:
	 * baseDepth[BaseDepth.OFFSET_A] : A
	 * baseDepth[BaseDepth.OFFSET_C] : C
	 * baseDepth[BaseDepth.OFFSET_G] : G
	 * baseDepth[BaseDepth.OFFSET_T] : T
	 * baseDepth[BaseDepth.OFFSET_D] : D
	 * @param depths
	 * @return
	 */
	public static boolean isSnp(Integer[] depths) {
		if (depths[BaseDepth.OFFSET_A] > 0) {
			if (depths[BaseDepth.OFFSET_C] > 0 || depths[BaseDepth.OFFSET_G] > 0 || depths[BaseDepth.OFFSET_T] > 0) {
				return true;
			} else {
				return false;
			}
		} else if (depths[BaseDepth.OFFSET_C] > 0) {
			if (depths[BaseDepth.OFFSET_G] > 0 || depths[BaseDepth.OFFSET_T] > 0) {
				return true;
			} else {
				return false;
			}
		} else if (depths[BaseDepth.OFFSET_G] > 0 && depths[BaseDepth.OFFSET_T] > 0){
			return true;
		}
		return false;
	}

}
