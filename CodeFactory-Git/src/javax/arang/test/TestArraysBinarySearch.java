package javax.arang.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.arang.genome.util.Util;

public class TestArraysBinarySearch {

	public static void main(String[] args) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 1; i < 50; i+=3) {
			list.add(i * 4);
			System.out.print(" " + i*4);
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
	
	private static void searchFor(ArrayList<Integer> list, int pos) {
		int closestStart = Util.getRegionStartContainingPos(list, pos);
		System.out.println(" Search for " + pos + " : " + closestStart + " " + Collections.binarySearch(list, pos));
	}
	
}
