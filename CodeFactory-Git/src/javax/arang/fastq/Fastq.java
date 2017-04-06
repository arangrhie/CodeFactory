package javax.arang.fastq;

public class Fastq {
	public static short READID_LINE = 0;
	public static short SEQ_LINE = 1;
	public static short QUALID_LINE = 2;
	public static short QUAL_LINE = 3;
	
	public static boolean isReadID(String line) {
		if (line.startsWith("@")) {
			return true;
		}
		return false;
	}
	
	public static boolean isQualSep(String line) {
		if (line.startsWith("+") && line.length() == 1) {
			return true;
		} else {
			return false;
		}
	}
}
