package javax.arang.test;

import java.util.HashMap;

import javax.arang.genome.indel.Indel;

public class ObjectTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ObjectTest().go();
	}
	
	public void go() {
		HashMap<Integer, Indel> testMap = new HashMap<Integer, Indel>();
		
		Indel indel = new Indel();
		indel.count = 0;
		
		testMap.put(1, indel);
		
		System.out.println("Before adding: " + testMap.get(1).count);
		
		testMap.get(1).count+=5;
		
		System.out.println("After adding: " + testMap.get(1).count);
		
	}

}
