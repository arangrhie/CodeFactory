package javax.arang.test;

import java.util.HashMap;
import java.util.Vector;

public class HashMapTest {

	public static void main(String[] args) {
		HashMap<String, Vector<String>> map = new HashMap<String, Vector<String>>();
		Vector<String> vec = new Vector<String>();
		
		map.put("Key1", vec);
		vec.add("ABC");
		
		System.out.println("Key1: " + map.get("Key1").get(0));
	}

}
