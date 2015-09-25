package javax.arang.base;

public class Base {
	public static final short CHR = 0;
	public static final short POS = 1;
	public static final short A = 2;
	public static final short C = 3;
	public static final short G = 4;
	public static final short T = 5;
	public static final short D = 6;
	
	public static String maxLikelyBase(String chr, int pos, String a, String c, String g,
			String t, String d) {
		int A = Integer.parseInt(a);
		int C = Integer.parseInt(c);
		int G = Integer.parseInt(g);
		int T = Integer.parseInt(t);
		int D = Integer.parseInt(d);
		int max = Math.max(Math.max(A, C), Math.max(G, T));
		if (max < D && D > 0) {
			return "D";
		} else {
			if (max == 0) {
				return "N";
			}
			if (max > 0
					&& ((max == A && (max == C || max == G || max == T))
					|| (max == C && (max == G || max == T))
					|| (max == G && max == T))) {
				System.out.println("[DEBUG] ::\t" + chr + ":" + pos + "\tA=" + A + "\tC=" + C + "\tG=" + G + "\tT=" + T);
				return "N";
			}
			if (A == max) {
				return "A";
			} else if (C == max) {
				return "C";
			} else if (G == max) {
				return "G";
			} else if (T == max) {
				return "T";
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
}
