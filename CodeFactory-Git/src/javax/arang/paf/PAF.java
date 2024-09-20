package javax.arang.paf;

public class PAF {

	public static final int Q_NAME	= 0;
	public static final int Q_LEN	= 1;
	public static final int Q_START	= 2;	// 0-based
	public static final int Q_END	= 3;	// 1-based
	public static final int STRAND	= 4;
	public static final int T_NAME	= 5;
	public static final int T_LEN	= 6;	// target length
	public static final int T_START	= 7;
	public static final int T_END	= 8;
	public static final int RESIDUAL_M = 9; // number of sequence matches
	public static final int BLOCK_LEN  = 10;// total number of sequence matches, mismatches, insertions and deletions
	public static final int MQ		= 11;	// 255 for missing
	
	public static boolean isPositive(String strand) {
		if (strand.equals("+")) return true;
		else	return false;
	}
}
