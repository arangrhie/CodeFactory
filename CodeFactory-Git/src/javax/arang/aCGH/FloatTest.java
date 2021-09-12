/**
 * 
 */
package javax.arang.aCGH;

/**
 * @author Arang Rhie
 *
 */
public class FloatTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		float number = 0.00f;
		int integer = 0;
		for (int i = 0; i < 41; i++) {
			number += 0.05f;
			System.out.println(number + " : " + Math.floor(number/0.7));
			integer += 5;
			System.out.println((float)integer/100 + " " + number);
		}

	}

}
