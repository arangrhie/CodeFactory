package javax.arang.phasing.util;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.genome.util.Util;

public class PhasedBlock implements Comparable<PhasedBlock> {
	
	public static short CHR = 0;
	public static short START = 1;
	public static short END = 2;
	public static short ID = 3;
	private String chr;
	private int blockStart;
	private int blockEnd;
	private String blockID;
	private int lastHetMarker;
	
	public PhasedBlock(String chr, int start, int lastHetMarker, int end, String blockID) {
		this.chr = chr;
		this.blockStart = start;
		this.blockEnd = end;
		this.blockID = blockID;
		this.lastHetMarker = lastHetMarker;
	}
	
	public PhasedBlock(String chr, int start, int end, String blockID) {
		this.chr = chr;
		this.blockStart = start;
		this.blockEnd = end;
		this.blockID = blockID;
	}

	public int getLastHetMarker() {
		return lastHetMarker;
	}
	
	public int getStart() {
		return blockStart;
	}

	public int getEnd() {
		return blockEnd;
	}
	
	public int getLen() {
		return blockEnd - blockStart;
	}

	public String getPS() {
		return blockID;
	}

	public String getChr() {
		return this.chr;
	}

	@Override
	public int compareTo(PhasedBlock otherBlock) {
		return this.blockEnd - otherBlock.getStart();
	}

	public void setBlockEnd(int end) {
		this.blockEnd = end;
	}
	
	public void setBlockStart(int start) {
		this.blockStart = start;
	}
	
	private boolean mark = false;
	
	public boolean isMarked() {
		return mark;
	}
	
	public void setMarked(boolean mark) {
		this.mark = mark;
	}
	
	/***
	 * Merge two blocks, if the id are identical.
	 * @param start
	 * @param end
	 * @param id
	 * @return true if merged, false if not merge-able
	 */
	public boolean merge(int start, int end, String id) {
		if (!this.blockID.equals(id)) {
			//System.out.println("[DEBUG] :: Not merge-able: Different block id. " + this.blockID + " vs. " + id);
			return false;
		}
		if (start < this.blockStart) {
			this.setBlockStart(start);
		}
		if (end > this.blockEnd) {
			this.setBlockEnd(end);
		}
		return true;
	}
	
	public static String getPS(int pos, ArrayList<Integer> startList, HashMap<Integer, Integer> startToEnd, HashMap<Integer, String> startToPS) {
		int closestStart = Util.getRegionStartContainingPos(startList, pos);
		if (closestStart < 0)	return "Unphased";
		if (pos <= startToEnd.get(closestStart)) {
			return startToPS.get(closestStart);
		} else {
			return "Unphased";
		}
	}

	public void setLastHetMarker(int endHetPos) {
		this.lastHetMarker = endHetPos;
	}
}
