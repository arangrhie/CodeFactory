/**
 * 
 */
package javax.arang.bam.util;

/**
 * @author Arang Rhie
 *
 */
public enum SeqBase {
	eq,A,C,M,G,R,S,V,T,W,Y,H,K,D,B,N;

	/**
	 * @param base
	 * @return
	 */
	public static SeqBase valueOf(int base) {
		if (base == 0)	return eq;
		if (base == 1)	return A;
		if (base == 2)	return C;
		if (base == 3)	return M;
		if (base == 4)	return G;
		if (base == 5)	return R;
		if (base == 6)	return S;
		if (base == 7)	return V;
		if (base == 8)	return T;
		if (base == 9)	return W;
		if (base == 10)	return Y;
		if (base == 11)	return H;
		if (base == 12)	return K;
		if (base == 13)	return D;
		if (base == 14)	return B;
		if (base == 15)	return N;
		return null;
	}
}
