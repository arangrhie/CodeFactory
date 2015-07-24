package javax.arang.matrix;

public class Matrix {
	/***
	 * Padd n number of "\tX"
	 * @param n
	 * @return
	 */
	public static String paddX(int n) {
		if (n == 0) {
			return "";
		}
		StringBuffer out = new StringBuffer("");
		for (int i = 0; i < n; i++) {
			out.append("\tX");
		}
		return out.toString();
	}

	public static String getNumOs(String data) {
		int numOs = 0;
		for (int i = 0; i < data.length(); i++) {
			if (data.charAt(i) == 'O') {
				numOs++;
			}
		}
		return String.valueOf(numOs);
	}
}
