package org.gmi.fx.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class UniXonTables {
	
	private static final String HD_FILE_DIR = "MetaData/";
	private static final String UNIXON_TABLE = "unixons.obj";
	private static final String STARTS_TABLE = "starts.obj";
	private static final String ENDS_TABLE = "ends.obj";
	
	private static volatile UniXonTables uniXonTables;
	
	HashMap<Long, Integer[]> unixons ;
	HashMap<Integer, Vector<Long>> starts = new HashMap<Integer, Vector<Long>>();
	HashMap<Integer, Vector<Long>> ends = new HashMap<Integer, Vector<Long>>();
	
	ArrayList<Integer> sortedStartList;
	ArrayList<Integer> sortedEndList;
	
	public static UniXonTables getInstance () {
		if (uniXonTables == null) {
			synchronized (UniXonTables.class) {
				if (uniXonTables == null) {
					uniXonTables = new UniXonTables();
				}
			}
		}
		return uniXonTables;
	}
	
	private UniXonTables() {
		starts = loadFromHDFS(HD_FILE_DIR + STARTS_TABLE);
		ends = loadFromHDFS(HD_FILE_DIR + ENDS_TABLE);
		unixons = loadUnixonsFromHDFS(HD_FILE_DIR + UNIXON_TABLE);
		sortedStartList = makeSortedList(starts);
		sortedEndList = makeSortedList(ends);
	}
	
	private static final short START = 0;
	private static final short END = 1;
	

	public Vector<Long> getExonsContaining(int genomePos) {
		Vector<Long> exonList = new Vector<Long>();
		int startIdx = getMaxSmallerThan(genomePos, sortedStartList);
		int endIdx = getMinGreaterThan(genomePos, sortedEndList);

		if (startIdx < 0 || endIdx < 0) {
			return null;
		}
		
		Vector<Long> startUnixonIDs;
		Vector<Long> endUnixonIDs;
		boolean hasStartExon = true;
		boolean hasEndExon = true;
		
		while(hasStartExon || hasEndExon) {
			startUnixonIDs = starts.get(sortedStartList.get(startIdx));
			endUnixonIDs = ends.get(sortedEndList.get(endIdx));

			if (hasStartExon) {
				boolean hasExon = false;
				for (Long unixonID : startUnixonIDs) {
					if (unixons.get(unixonID)[START] <= genomePos && genomePos <= unixons.get(unixonID)[END]) {
						if (!exonList.contains(unixonID)) {
							exonList.add(unixonID);
						}
						hasExon = true;
					}
				}
				if (hasExon) {
					startIdx--;
					if (startIdx < 0)	hasStartExon = false;
				} else hasStartExon = false;
			}

			if (hasEndExon) {
				boolean hasExon = false;
				for (Long unixonID : endUnixonIDs) {
					if (unixons.get(unixonID)[START] <= genomePos && genomePos <= unixons.get(unixonID)[END]) {
						if (!exonList.contains(unixonID)) {
							exonList.add(unixonID);
						}
						hasExon = true;
					}
				}
				if (hasExon) {
					endIdx++;
					if (endIdx >= sortedEndList.size()) hasEndExon = false;
				} else hasEndExon = false;
			}
		}
		
		return exonList;
	}
	
	
	/***
	 * Doing binary search for finding the minimum element index greater or equal to posVal.
	 * @param posVal
	 * @param list
	 * @return minimum value of list that is greater or equal to posVal
	 * or -1 if the greatest value in the list is smaller than posVal.
	 */
	public int getMinGreaterThan(Integer posVal, ArrayList<Integer> list) {
		int len = list.size();
		if (posVal > list.get(len-1)) {
			return -1;	// intron or intergenic
		}
		
		if (posVal == list.get(len-1)) {
			return len-1;
		}
		
		if (posVal <= list.get(0)) {
			return 0;
		}
		
		int i = len/2;
		int st = 0;
		int en = len-1;
		while(st <= i && i < en) {
			// System.out.println("i = " + i + " | " + list.get(i) + ", " + list.get(i+1));
			if (list.get(i) < posVal && posVal <= list.get(i+1)) {
				return i+1;
			}
			if (list.get(i) < posVal) {
				st = i + 1;
				i += (en - st + 1)/2;
			} else {
				en = i;
				i -= (en - st + 1)/2;
			}
		}
		return i;
	}
	
	/***
	 * Doing binary search for finding the maximum element index smaller or equal to posVal
	 *  in the given list
	 * @param posVal
	 * @param list
	 * @return maximum value of list that is smaller than posVal 
	 * or -1 if the smallest value in list is greater than value.
	 */
	public int getMaxSmallerThan(Integer posVal, ArrayList<Integer> list) {
		int len = list.size();
		if (posVal < list.get(0)) {
			return -1; // intron or intergenic
		}
		if (posVal >= list.get(len-1)) {
			return len-1;
		}
		
		int i = len/2;
		int st = 0;
		int en = len-1;
		while(st <= i && i < en) {
			//	System.out.println("i = " + i + " | " + list.get(i) + ", " + list.get(i+1));
			if (list.get(i) <= posVal && posVal < list.get(i+1)) {
				return i;
			}
			if (list.get(i) < posVal) {
				st = i + 1;
				i += (en - st + 1)/2;
			} else {
				en = i;
				i -= (en - st + 1)/2;
			}
		}
		return -1;
	}
	
	private ArrayList<Integer> makeSortedList(HashMap<Integer, Vector<Long>> mapToSort) {
		ArrayList<Integer> sortedList = new ArrayList<Integer>();
		Set<Integer> set = mapToSort.keySet();
		for (Integer key : set) {
			sortedList.add(key);
		}
		Collections.sort(sortedList);
		return sortedList;
	}

	@SuppressWarnings("unchecked")
	public HashMap<Integer, Vector<Long>> loadFromHDFS(String file) {
		HashMap<Integer, Vector<Long>> table = null;
		FileSystem hdfs = null;
		try {
			hdfs = FileSystem.get(new Configuration());
			FSDataInputStream dis = hdfs.open(new Path(file));
			ObjectInputStream objIn = new ObjectInputStream(dis);
			table = (HashMap<Integer, Vector<Long>>) objIn.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return table;
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<Long, Integer[]>  loadUnixonsFromHDFS(String file) {
		HashMap<Long, Integer[]> table = null;
		FileSystem hdfs = null;
		try {
			hdfs = FileSystem.get(new Configuration());
			FSDataInputStream dis = hdfs.open(new Path(file));
			ObjectInputStream objIn = new ObjectInputStream(dis);
			table = (HashMap<Long, Integer[]>) objIn.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return table;
	}
	
	public ArrayList<Integer> getSortedStartList() {
		return sortedStartList;
	}
	
	public ArrayList<Integer> getSortedEndList() {
		return sortedEndList;
	}

	/**
	 * @return the unixons
	 */
	public HashMap<Long, Integer[]> getUnixons() {
		return unixons;
	}

	/**
	 * @return the starts
	 */
	public HashMap<Integer, Vector<Long>> getStarts() {
		return starts;
	}

	/**
	 * @return the ends
	 */
	public HashMap<Integer, Vector<Long>> getEnds() {
		return ends;
	}

}