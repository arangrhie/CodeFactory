/**
 * 
 */
package javax.arang.aCGH;


/**
 * @author Arang Rhie
 *
 */
public class GetCountsLargeRegionDriver extends GetCountsLargeRegion {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GetCountsLargeRegion obj = new GetCountsLargeRegion();
		// String file = "C://Documents and Settings/Administrator/바탕 화면/이비인후과/array_data_all.txt";
		String file = args[0];
		for (int i = 2; i <= 20; i++) {
			columnFrom = 6;
			moreThan = i;
			obj.go(file, file.replace(".txt", "_counts_" + String.format("%02d", moreThan) + ".txt"));
		}
	}

}
