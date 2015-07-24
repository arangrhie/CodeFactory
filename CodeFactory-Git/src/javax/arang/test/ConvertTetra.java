package javax.arang.test;

public class ConvertTetra {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int a = 16;
		System.out.println("a in decimal: " + a);
		
		int b = 0;
		int c = 1;
		while (a > 0) {
			b += c*(a % 4);
			a /= 4;
			c *= 10;
		}
		System.out.println("a in tetra(4진수): " + b);
		
		int hexa = 0x16;
		System.out.println(hexa);
		
	}

}
