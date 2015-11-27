package javax.arang.algorithm;

import java.util.ArrayList;

public class Cell {
	public ArrayList<Integer> path = new ArrayList<Integer>();
	int score;
	int toAlignIdx;
	int toCompareIdx;
	
	public Cell(ArrayList<Integer> path, int cumScore, int toAlignIdx,
			int toCompareIdx) {
		this.path = path;
		this.score = cumScore;
		this.toAlignIdx = toAlignIdx;
		this.toCompareIdx = toCompareIdx;
	}
	public Cell() { }
	
	public String printPath() {
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < path.size(); i++) {
			out.append(path.get(i) + " ");
		}
		return out.toString();
	}
	
	public Cell clone() {
		return new Cell(path, score, toAlignIdx, toCompareIdx);
	}
}
