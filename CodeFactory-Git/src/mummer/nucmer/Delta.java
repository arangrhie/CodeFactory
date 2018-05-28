package mummer.nucmer;

public class Delta {
	public static final short R_NAME = 0;	// Ref
	public static final short Q_NAME = 1;
	public static final short R_LEN = 2;
	public static final short Q_LEN = 3;
	public static final int R_START = 0;
	public static final int R_END = 1;
	public static final int Q_START = 2;
	public static final int Q_END = 3;
	
	public static final short TYPE_UNSET = 0;
	public static final short TYPE_D = 1;
	public static final short TYPE_I = 2;
	
	public static final int EOA=0;
	
	public static boolean isHeader(String line) {
		if (line.startsWith(">")) {
			return true;
		}
		return false;
	}
	
	public static String getQueryName(String[] tokens) {
		return tokens[Q_NAME];
	}
	
	public static String getRefName(String[] tokens) {
		return tokens[R_NAME].substring(1);
	}
	
	public static boolean isReverse(String[] tokens) {
		int start = Integer.parseInt(tokens[Delta.Q_START]);
		int end = Integer.parseInt(tokens[Delta.Q_END]);
		
		if (end < start) {
			return true;
		} else {
			return false;
		}
	}

	/***
	 * Get type of indel. Deletion or Insertion to the reference.
	 * @param indel
	 * @return TYPE_D or TYPE_I
	 */
	public static short getIDType(int indel) {
		if (indel > 0) {
			return TYPE_D;
		} else {
			return TYPE_I;
		}
	}
	
	/***
	 * Get matched bases before the next indel occurs
	 * @param indel
	 * @return
	 */
	public static int getMatches(int indel) {
		if (indel < 0) {
			indel *= -1;
		}
		return indel - 1;
	}

	/***
	 * Get query start position. Does not check if the end < start.
	 * @param tokens
	 * @return
	 */
	public static int getQueryStart(String[] tokens) {
		return Integer.parseInt(tokens[Q_START]);
	}
	
	/***
	 * Get query end position. Does not check if the end < start.
	 * @param tokens
	 * @return
	 */
	public static int getQueryEnd(String[] tokens) {
		return Integer.parseInt(tokens[Q_END]);
	}

	/***
	 * Get the String representative value for I, D
	 * @param typeID
	 * @return
	 */
	public static String getStringValueOfTypeID(short typeID) {
		if (typeID == TYPE_D) {
			return "D";
		} else if (typeID == TYPE_I) {
			return "I";
		}
		return null;
	}

	/***
	 * Get query fa sequence length
	 * @param tokens
	 * @return
	 */
	public static int getQueryLen(String[] tokens) {
		return Integer.parseInt(tokens[Q_LEN]);
	}
	
	public static int getTargetLen(String[] tokens) {
		return Integer.parseInt(tokens[R_LEN]);
	}
	
	
}
