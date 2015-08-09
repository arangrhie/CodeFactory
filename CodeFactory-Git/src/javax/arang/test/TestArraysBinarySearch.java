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
		searchFor(list, 10);
		searchFor(list, 100);
		searchFor(list, 190);
		searchFor(list, 200);
		searchFor(list, 205);
		
//		Integer[] listArr = list.toArray(new Integer[0]);
//		searchFor(10, listArr);
//		searchFor(100, listArr);
//		searchFor(190, listArr);
//		searchFor(200, listArr);
//		searchFor(205, listArr);
//		System.out.println("listArr.length: " + listArr.length);
	}

	private static void searchFor(int key, Integer[] listArr) {
		int idx = Arrays.binarySearch(listArr, key);
		System.out.println(" search for " + key + " : " + idx);
	}
	
	private static void searchFor(ArrayList<Integer> list, int pos) {
		int closestStart = Util.getRegionStartContainingPos(list, pos);
		System.out.println(" Search for " + pos + " : " + closestStart + " " + Collections.binarySearch(list, pos));
	}
	
}
