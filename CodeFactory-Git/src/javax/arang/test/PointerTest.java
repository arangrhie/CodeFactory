/**
 * 
 */
package javax.arang.test;

/**
 * @author Arang Rhie
 *
 */
public class PointerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new PointerTest().test();

	}
	
	private int a;
	private void test() {
		a = 11;
		System.out.println("a = " + a);
		changeClassVar(a);
		System.out.println("a = " + a);
	}
	
	private void changeClassVar(int a) {
		a = 22;
	}

}
