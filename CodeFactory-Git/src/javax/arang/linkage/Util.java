/**
 * 
 */
package javax.arang.linkage;


/**
 * @author Arang Rhie
 *
 */
public class Util {
	
	/***
	 * Return location basepares in double formatted digital number.
	 * @param chr
	 * @param pos
	 * @return chr * 10^10
	 */
	public static double chrPosToDouble(String chr, String pos) {
		Double newPos = 0d;
		Double shifter = 100000000000d;
		chr = chr.replace("chr", "");
		if (chr.equals("X")) {
			newPos = 23 * shifter;
		}else if (chr.equals("X")) {
			newPos = 24 * shifter;
		} else if (chr.equals("M")) {
			newPos = 25 * shifter;
		} else {
			newPos = Integer.parseInt(chr) * shifter;
		}
		newPos += Integer.parseInt(pos);
		return newPos;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double num = Util.chrPosToDouble("chr3", "240003870");
		System.out.println(String.format("%.0f", num));
	}

}
