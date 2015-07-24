package javax.arang.test;

import java.util.ArrayList;
import java.util.Arrays;

public class TestArraysBinarySearch {

	public static void main(String[] args) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 1; i < 50; i+=3) {
			list.add(i * 4);
			System.out.print(" " + i*4);
		}
		System.out.println();
		Integer[] listArr = list.toArray(new Integer[0]);
		searchFor(10, listArr);
		searchFor(100, listArr);
		searchFor(190, listArr);
		searchFor(200, listArr);
		searchFor(205, listArr);
		System.out.println("listArr.length: " + listArr.length);
	}

	private static void searchFor(int key, Integer[] listArr) {
		int idx = Arrays.binarySearch(listArr, key);
		System.out.println(" search for " + key + " : " + idx);
	}
}
