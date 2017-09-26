package javax.arang.graph.gfa;

public class GFA {
	public static final int S_NAME = 1;
	public static final int S_LEN = 3;
	public static final int L_SEG1 = 1;
	public static final int L_SEG1_DIR = 2;
	public static final int L_SEG2 = 3;
	public static final int L_SEG2_DIR = 4;
	
	public static final boolean IS_POSITIVE = true;
	public static final boolean IS_FORWARD = true;
	
	public static int getLen(String len) {
		//System.err.println("[DEBUG] :: " + len);
		return Integer.parseInt(len.substring(5));
	}
	
	public static boolean isForward(char dir) {
		if (dir == '+') {
			return IS_FORWARD;
		}
		return !IS_FORWARD;
	}
	
	public static boolean isCircularOrPalendromic(Segment seg1, char dir1, Segment seg2, char dir2) {
		if (seg1.getName().equals(seg2.getName())) {
			return true;
		}
		return false;
	}
	
	public static char switchDirection(char dir) {
		if (dir == '+') {
			return '-';
		} else {
			return '+';
		}
	}
}
