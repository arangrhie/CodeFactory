package javax.arang.vcf;

import java.util.ArrayList;
import java.util.HashMap;

public class VCF {
	public static final int CHROM = 0;
	public static final int POS = 1;
	public static final int ID = 2;
	public static final int REF = 3;
	public static final int ALT = 4;
	public static final int QUAL = 5;
	public static final int FILTER = 6;
	public static final int INFO = 7;
	public static final int FORMAT = 8;
	public static final int SAMPLE = 9;
	
	public static final short TYPE_SNP = 0;
	public static final short TYPE_INSERTION = 1;
	public static final short TYPE_DELETION = 2;
	public static final short TYPE_SUBSTITUTION = 3;
	public static final short TYPE_MULTI = 4;
	
	
	public static String parseINFO(String info, String fieldToRetrieve) {
		String value = "";
		String[] infoFields = info.split(";");
		fieldToRetrieve = fieldToRetrieve + "=";
		for (String field : infoFields) {
			if (field.startsWith(fieldToRetrieve)) {
				value = field.substring(field.indexOf("=") + 1);
				break;
			}
		}
		return value;
	}
	
	/***
	 * Retrieve the desired field (fieldToRetrieve) out of sample
	 * @param format
	 * @param fieldToRetrieve
	 * @param sample
	 * @return
	 */
	public static String parseSAMPLE(String format, String fieldToRetrieve, String sample) {
		String[] formatFields = format.split(":");
		String[] sampleFields = sample.split(":");
		int idxToRetrieve = 0;
		String field;
		for (idxToRetrieve = 0; idxToRetrieve < formatFields.length; idxToRetrieve++) {
			field = formatFields[idxToRetrieve];
			if (field.equals(fieldToRetrieve)) {
				return sampleFields[idxToRetrieve];
			}
		}
		return "NA:NoField";
	}
	
	public static Boolean isTransition(String ref, String alt) {
		if (ref.equals("A") && alt.equals("G")
				|| ref.equals("G") && alt.equals("A")
				|| ref.equals("C") && alt.equals("T")
				|| ref.equals("T") && alt.equals("C")) {
			return true;
		}
		return false;
	}
	
	public static HashMap<String, String> parseFormatSample(String format, String sample) {
		HashMap<String, String> valueTable = new HashMap<String, String>();
		String[] formatTokens = format.split(":");
		String[] sampleTokens = sample.split(":");
		for (int i = 0; i < formatTokens.length; i++) {
			String form = formatTokens[i];
			String value = sampleTokens[i];
			if (form.equals("GT")) {
				if (value.equals("1/1") || value.equals("1|1")) {
					value = "2";	// AA
				} else if (value.equals("0/1") || value.equals("0|1") || value.equals("1|0")) {
					value = "1";	// RA
				} else if (value.equals("0/0") || value.equals("0|0")) {
					value = "0";	// RR
				} else if (value.contains(".")){	// cannot be called at this position
					value = "NA";	// NA
				} else {
					if (value.contains("/")) {
						value = Integer.parseInt(value.substring(0, value.indexOf("/")))
								+ Integer.parseInt(value.substring(value.indexOf("/") + 1)) + "";
					} else if (value.contains("|")) {
						value = Integer.parseInt(value.substring(0, value.indexOf("|")))
								+ Integer.parseInt(value.substring(value.indexOf("|") + 1)) + "";
					}
				}
			}
			valueTable.put(form, value);
		}
		return valueTable;
	}
	
	public static String getGT(String format, String sample) {
		String[] formatTokens = format.split(":");
		String[] sampleTokens = sample.split(":");
		for (int i = 0; i < formatTokens.length; i++) {
			String form = formatTokens[i];
			String value = sampleTokens[i];
			if (form.equals("GT")) {
				return value;
			}
		}
		return "NA";				
	}
	
	public static String parseGT(String format, String sample) {
		String[] formatTokens = format.split(":");
		String[] sampleTokens = sample.split(":");
		for (int i = 0; i < formatTokens.length; i++) {
			String form = formatTokens[i];
			String value = sampleTokens[i];
			if (form.equals("GT")) {
				if (value.equals("1/1") || value.equals("1|1")) {
					value = "2";	// AA
				} else if (value.equals("0/1") || value.equals("0|1") || value.equals("1|0")) {
					value = "1";	// RA
				} else if (value.equals("0/0") || value.equals("0|0")) {
					value = "0";	// RR
				} else if (value.contains(".")){	// cannot be called at this position
					value = "NA";	// NA                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
				} else {
					value = "3";
				}
				return value;
			}
		}
		return "";
	}
	
	public static String parseGT(String format, String sample, int mutltiIdx) {
		String[] formatTokens = format.split(":");
		String[] sampleTokens = sample.split(":");
		for (int i = 0; i < formatTokens.length; i++) {
			String form = formatTokens[i];
			String value = sampleTokens[i];
			if (form.equals("GT")) {
				if (value.equals(mutltiIdx + "/" + mutltiIdx) || value.equals(mutltiIdx + "|" + mutltiIdx)) {
					value = "2";	// AA
				} else if (value.equals("0/" + mutltiIdx) || value.equals("0|" + mutltiIdx) || value.equals(mutltiIdx + "|0")) {
					value = "1";	// RA
				} else if (value.equals("0/0") || value.equals("0|0")) {
					value = "0";	// RR
				} else if (value.contains(".")){	// cannot be called at this position
					value = "NA";	// NA                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
				} else {
					value = "3";
				}
				return value;
			}
		}
		return "#N/A";
	}
	
	public static HashMap<String, String> parseInfo(String info) {
		HashMap<String, String> valueTable = new HashMap<String, String>();
		String[] infoTokens = info.split(";");
		for (int i = 0; i < infoTokens.length; i++) {
			String[] valueTokens = infoTokens[i].split("=");
			if (valueTokens.length == 2) {
				valueTable.put(valueTokens[0], valueTokens[1]);
			} else {
				valueTable.put(valueTokens[0], "");
			}
		}
		return valueTable;
	}
	
	public static String getHeaderString() {
		return "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT";
	}
	
	
	public static short getType(String ref, String alt) {
		int numAlts = getNumAltAlleles(alt);
		if (numAlts == 1) {
			if (ref.length() == 1 && alt.length() == 1) {
				return TYPE_SNP;
			} else if (alt.length() == 1 && ref.length() > alt.length()) {
				return TYPE_DELETION;
			} else if (ref.length() == 1 && ref.length() < alt.length()){
				return TYPE_INSERTION;
			} else {
				return TYPE_SUBSTITUTION;
			}
		} else {
			return TYPE_MULTI;
		}
	}
	
	public static ArrayList<Short> getMultiType(String ref, String[] alts) {
		ArrayList<Short> altTypes = new ArrayList<Short>();
		for (int i = 0; i < alts.length; i++) {
			altTypes.add(getType(ref, alts[i]));
		}
		return altTypes;
	}
	
	public static boolean isMultiAllele(String alt) {
		if (alt.contains(",")) {
			return true;
		} else {
			return false;
		}
	}
	
	public static int getNumAltAlleles(String alt) {
		if (alt.contains(",")) {
			String[] alts = alt.split(",");
			return alts.length;
		} else {
			return 1;
		}
	}
}
