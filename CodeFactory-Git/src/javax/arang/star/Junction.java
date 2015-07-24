package javax.arang.star;

public class Junction {
	public static final int DONOR_CHR = 0;
	public static final int DONOR_BASE = 1;
	public static final int DONOR_STRAND = 2;
	
	public static final int ACCEPTOR_CHR = 3;
	public static final int ACCEPTOR_BASE = 4;
	public static final int ACCEPTOR_STRAND = 5;
	
	public static final int JUNCTION_TYPE = 6;
	public static final int REPEAT_LEFT = 7;
	public static final int REPEAT_RIGHT = 8;
	public static final int READ_NAME = 9;
	public static final int FIRST_SEG_FIRST_BASE = 10;
	public static final int FIRST_SEG_CIGAR = 11;
	public static final int SECOND_SEG_FIRST_BASE = 12;
	public static final int SECOND_SEG_CIGAR = 13;
	
	public static boolean isChrM(String[] tokens) {
		if (tokens[DONOR_CHR].equals("chrM"))	return true;
		if (tokens[ACCEPTOR_CHR].equals("chrM"))	return true;
		if (tokens[DONOR_CHR].equals("MT"))	return true;
		if (tokens[ACCEPTOR_CHR].equals("MT"))	return true;
		return false;
	}
}
