/**
 * 
 */
package javax.arang.test;

/**
 * @author Arang Rhie
 *
 */
public class CallByReference {
	
	
	Integer value;
	
	private void callByReferenceTest(String[] tokens) {
		tokens[2] = "ddd"; 
	}
	
	private void callByReferenceTest(Boolean[] tokens) {
		tokens[2] = false; 
	}
	
	private void callByValueTest(Integer val) {
		val++;
	}
	
	private Integer[][] initTable(Integer[][] table) {
		table = new Integer[5][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 5; j++) {
				table[j][i] = 10 * i + j;
				//System.out.print(table[j][i] + " ");
			}
			//System.out.println();
		}
		return table;
	}
	
	private void go() {
		String[] tokens = new String[3];
		tokens[0] = "aaa";
		tokens[1] = "bbb";
		tokens[2] = "ccc";
		System.out.println("Before:");
		System.out.println(tokens[0] + " " + tokens[1] + " " + tokens[2]);
		callByReferenceTest(tokens);
		System.out.println("After: callByReferenceTest(String[] tokens)");
		System.out.println(tokens[0] + " " + tokens[1] + " " + tokens[2]);
		System.out.println();
		
		Boolean[] tokens2 = new Boolean[3];
		tokens2[0] = true;
		tokens2[1] = true;
		tokens2[2] = true;
		System.out.println("Before:");
		System.out.println(tokens2[0] + " " + tokens2[1] + " " + tokens2[2]);
		callByReferenceTest(tokens2);
		System.out.println("After: callByReferenceTest(Boolean[] tokens)");
		System.out.println(tokens2[0] + " " + tokens2[1] + " " + tokens2[2]);
		System.out.println();
		
		value = 0;
		System.out.println("Before: value = " + value);
		callByValueTest(value);
		System.out.println("After: value = " + value);
		
		System.out.println("int[][] and Integer[][] table is also called as \"callByValue\".");
		Integer[][] test = new Integer[5][3];
		
		System.out.println("Before initTable: Filled with null");
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 5; j++) {
				System.out.print(test[j][i] + " ");
			}
			System.out.println();
		}
		
		System.out.println("-- initTable --");
		test = initTable(test);
		System.out.println("After initTable");
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 5; j++) {
				System.out.print(test[j][i] + " ");
			}
			System.out.println();
		}
		
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new CallByReference().go();

	}

}
