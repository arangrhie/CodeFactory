package javax.arang.test;

import java.util.Vector;

public class VectorTest {

	public static void main(String[] args) {
		Vector<Integer> testVec = new Vector<Integer>();
		for (int i = 0; i < 10; i++) {
			testVec.add(i);
		}
		
		for (int i = 5; i < 9; i++) {
			testVec.set(i, testVec.get(i) + 3);
		}
		
		for (int i = 0; i < testVec.size(); i++) {
			System.out.print(testVec.get(i) + " ");
		}

	}

}
