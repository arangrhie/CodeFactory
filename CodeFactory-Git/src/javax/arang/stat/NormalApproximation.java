package javax.arang.stat;

public class NormalApproximation {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new NormalApproximation().go();
	}
	
	private void go() {
		float pi1 = 0.3f;
		float pi2 = 0.055f;
		
		System.out.println("1.(1) ");
		System.out.println(getNormalZ(pi2, 100, 3));
		System.out.println("1.(2) " + getNormalZ(pi1, 150, 50) + " " + getNormalZ(pi2, 150, 15));
		
		pi1 = 0.3f;
		pi2 = 0.1f;
		
		System.out.println("Ex. " + getNormalZ(pi1, 10, 3) + " " + getNormalZ(pi2, 10, 3));
		
		float R = (float) ((0.6008 + Math.sqrt( (Math.pow(0.6008, 2) - 4 * 1.08*0.0678 ) )) / ( 2 * 1.08));
		System.out.println("3.(2) " + R);
		// R = 0.3879f;
		R = 0.3700f;
		float z = (float) ((R - 0.25 - (1/(2*48))) / Math.sqrt(((R*(1-R))/48)));
		System.out.println("3.(2) Z " + z);
		
		R = 0.1492f;
		z = (float) ((0.25 - R - (1/(2*48))) / Math.sqrt(((R*(1-R))/48)));
		System.out.println("3.(2) Z " + z);
		
	}
	
	public double getNormalZ(float pi, int n, int r) {
		double z = 0;
		System.out.println((r - (n * pi)));
		System.out.println((Math.sqrt(n * pi * (1 - pi))));
		z = ((r - (n * pi)) / (Math.sqrt(n * pi * (1 - pi))));
		return z;
	}
	
	public double getPoissonZ(float pi, int n, int r) {
		double z = 0;
		System.out.print((r - (n * pi)) + " / ");
		System.out.print((Math.sqrt(n * pi)) + " = ");
		z = ((r - (n * pi)) / (Math.sqrt(n * pi)));
		System.out.println(z);
		return z;
	}

}
