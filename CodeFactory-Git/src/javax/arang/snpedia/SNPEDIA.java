/**
 * 
 */
package javax.arang.snpedia;

/**
 * @author Arang Rhie
 *
 */
public class SNPEDIA {

	public static final int RS_ID_GT = 0;
	public static final int MAGNITUDE = 1;
	public static final int REPUTE = 2;
	public static final int SUMMARY = 3;
	
	/***
	 * Returns the SNPEDIA RS_ID_GT type key
	 * @param rsID
	 * @param gt1 first allele
	 * @param gt2 second allele
	 * @return
	 */
	public static String getRsIdGt(String rsID, String gt1, String gt2){ 
		return rsID + "(" + gt1 + ";" + gt2 + ")";
	}

	/**
	 * @param rsIdKey
	 * @return
	 */
	public static String getComplement(String rsIdKey) {
		String rsId = rsIdKey.substring(0, rsIdKey.indexOf("("));
		String gt1 = rsIdKey.substring(rsIdKey.indexOf("(") + 1, rsIdKey.indexOf(";"));
		gt1 = getComplementBase(gt1);
		String gt2 = rsIdKey.substring(rsIdKey.indexOf(";") + 1, rsIdKey.indexOf(")"));
		gt2 = getComplementBase(gt2);
		return rsId + "(" + gt1 + ";" + gt2 + ")";
	}
	
	public static String getComplementBase(String base) {
		if (base.equals("A")) {
			return "T";
		} else if (base.equals("T")) {
			return "A";
		} else if (base.equals("G")) {
			return "C";
		} else if (base.equals("C")) {
			return "G";
		}
		return "";
	}
	
}
