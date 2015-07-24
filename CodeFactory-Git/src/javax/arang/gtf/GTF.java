package javax.arang.gtf;

public class GTF {

	public static final int SEQNAME = 0;
	public static final int SOURCE = 1;
	public static final int FEATURE = 2;
	public static final int START = 3;
	public static final int END = 4;
	public static final int SCORE = 5;
	public static final int STRAND = 6;
	public static final int FRAME = 7;
	public static final int ATTRIBUTE = 8;
	public static final int UNKNOWN = -1;
	
	public static int getColumn(String field) {
		if (field.equalsIgnoreCase("seqname") || field.equalsIgnoreCase("chr")) {
			return SEQNAME;
		} else if(field.equalsIgnoreCase("source")) {
			return SOURCE;
		} else if(field.equalsIgnoreCase("feature")) {
			return FEATURE;
		} else if(field.equalsIgnoreCase("start")) {
			return START;
		} else if(field.equalsIgnoreCase("end")) {
			return END;
		} else if(field.equalsIgnoreCase("score")) {
			return SCORE;
		} else if(field.equalsIgnoreCase("strand")) {
			return STRAND;
		} else if(field.equalsIgnoreCase("frame")) {
			return FRAME;
		} else if(field.equalsIgnoreCase("attribute")) {
			return ATTRIBUTE;
		} else {
			return UNKNOWN;
		}
	}
}
