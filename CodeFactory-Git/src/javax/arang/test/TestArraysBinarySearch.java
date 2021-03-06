package javax.arang.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.arang.genome.util.Util;

public class TestArraysBinarySearch {

	public static void main(String[] args) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		System.out.print("list:");
		for (int i = 1; i <= 5; i++) {
			list.add(i*2);
			System.out.print(" " + i*2);
		}
		System.out.println();
		System.out.println("Collections.binarySearch(list, 1): " + Collections.binarySearch(list, 1));
		System.out.println("Collections.binarySearch(list, 2): " + Collections.binarySearch(list, 2));
		System.out.println("Collections.binarySearch(list, 3): " + Collections.binarySearch(list, 3));
		System.out.println("Collections.binarySearch(list, 10): " + Collections.binarySearch(list, 10));
		System.out.println("Collections.binarySearch(list, 13): " + Collections.binarySearch(list, 13));
		System.out.println();
		
		for (int i = 1; i < 50; i+=3) {
			list.add(i * 4);
		}
		
		Collections.sort(list);
		System.out.print("list:");
		for (int i = 0; i < list.size(); i++) {
			System.out.print(" " + list.get(i));
		}
		System.out.println();
//		searchFor(list, 2);
//		searchFor(list, 10);
//		searchFor(list, 100);
//		searchFor(list, 190);
//		searchFor(list, 200);
//		searchFor(list, 205);
//		System.out.println();
		
		Integer[] listArr = list.toArray(new Integer[0]);
		System.out.println("listArr.length: " + listArr.length);
		System.out.println();
		
		System.out.println("Equal or Greater");
		searchForEqualOrGreater(2, listArr);
		searchForEqualOrGreater(10, listArr);
		searchForEqualOrGreater(100, listArr);
		searchForEqualOrGreater(190, listArr);
		searchForEqualOrGreater(200, listArr);
		searchForEqualOrGreater(205, listArr);
		System.out.println();
		
		System.out.println("Less or Equal");
		searchForLessOrEqual(2, listArr);
		searchForLessOrEqual(10, listArr);
		searchForLessOrEqual(100, listArr);
		searchForLessOrEqual(190, listArr);
		searchForLessOrEqual(200, listArr);
		searchForLessOrEqual(205, listArr);
		System.out.println();
		
		System.out.println("Less or Equal");
		searchFor(2, list);
		searchFor(4, list);
		searchFor(10, list);
		searchFor(100, list);
		searchFor(190, list);
		searchFor(200, list);
		searchFor(205, list);
		System.out.println();
	}

	private static void searchForEqualOrGreater(int key, Integer[] listArr) {
		int idx = Arrays.binarySearch(listArr, key);
		if (idx < 0) {
			idx += 1;	// to find 1 pos right to the seqStart 
			idx *= -1;
		}
		// all snps are smaller than seqStart
		if (idx == listArr.length) {
			System.out.println(" Not containing " + key);
			return;
		} 
		System.out.println(" search for " + key + " : " + idx);
	}
	
	private static void searchForLessOrEqual(int key, Integer[] listArr) {
		int idx = Arrays.binarySearch(listArr, key);
		if (idx < 0) {
			idx += 2;	// to find 1 pos left to the seqEnd 
			idx *= -1;
		}
		if (idx < 0) {
			System.out.println(" Not containing " + key);
			return;
		}
		else if (key == listArr.length) {
			idx = listArr.length - 1;
		}
		System.out.println(" search for " + key + " : " + idx);
	}
	
	private static void searchFor(int pos, ArrayList<Integer> list) {
		int closestStart = Util.getRegionStartContainingPos(list, pos);
		System.out.println(" Search for " + pos + " : " + closestStart + " " + Collections.binarySearch(list, pos));
	}
	
}
