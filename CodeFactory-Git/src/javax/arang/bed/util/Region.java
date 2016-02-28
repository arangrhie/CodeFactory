package javax.arang.bed.util;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.genome.util.Util;

public class Region {
	private int start = -1;
	private int end = -1;
	private String notes = "";
	
	public Region(int start, int end, String notes) {
		this.start = start;
		this.end = end;
		this.notes = notes;
	}
	
	public boolean isInRegion(int pos) {
		if (start < pos && pos <= end) {
			return true;
		}
		return false;
	}
	
	public String getName() {
		return this.notes;
	}
	
	public int getStart() {
		return this.start;
	}
	
	public int getEnd() {
		return this.end;
	}
	
	public static boolean isInRegion(int pos, ArrayList<Integer> startList, HashMap<Integer, Integer> startToEnd) {
		int closestStart = Util.getRegionStartContainingPos(startList, pos);
		if (closestStart < 0)	return false;
		if (pos <= startToEnd.get(closestStart)) {
			return true;
		} else {
			return false;
		}
	}
}
