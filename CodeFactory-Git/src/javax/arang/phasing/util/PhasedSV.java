package javax.arang.phasing.util;


public class PhasedSV {
	public static final short CONTIG = 0;
	public static final short START = 1;
	public static final short END = 2;
	public static final short TYPE = 3;
	public static final short ALLELES = 4;
	public static final short DEPTH_ALLELE = 5;
	public static final short DEPTH_TOTAL = 6;
	public static final short HAPLPOTYPE = 7;
	
	public static boolean isSubstitution(String type) {
		if (type.equals("SUBSTITUTION"))	return true;
		return false;
	}
	
	public static boolean isDeletion(String type) {
		if (type.equals("DELETION"))	return true;
		return false;
	}
	
	public static boolean isMultiAllele(String type) {
		if (type.equals("MULTI_ALLELE"))	return true;
		return false;
	}
}
