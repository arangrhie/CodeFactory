package javax.arang.gene;

import java.util.PriorityQueue;

public class ExonUnion implements Comparable<ExonUnion> {

	private PriorityQueue<Integer> borders = new PriorityQueue<Integer>(2);
	private String chr = "";
	private int min = Integer.MAX_VALUE;
	private int max = 0;
	private String geneID = "";
	private String strand = "";
	
	public ExonUnion(String chr, int start, int end, String geneID, String strand) {
		this.chr = chr;
		min = start;
		max = end;
		this.geneID = geneID;
		this.strand = strand;
		addBorder(start-1);
		addBorder(end);
	}
	
	public boolean isOverlapping(int start, int end) {
		if (this.min <= end && start <= this.max) {
			return true;
		}
		return false;
	}
	
	public void addExon(int start, int end) {
		if (start < this.min) {
			this.min = start;
		}
		
		if (this.max < end) {
			this.max = end;
		}
		
		addBorder(start-1);
		addBorder(end);
	}
	
	private void addBorder(int border) {
		if (!borders.contains(border)) {
			borders.add(border);
		}
	}
	
	public int getMin() {
		return min;
	}
	
	public int getMax() {
		return max;
	}
	
	public PriorityQueue<Integer> getBorders() {
		return borders;
	}
	
	public String getChr() {
		return chr;
	}
	
	public String getGeneID() {
		return geneID;
	}

	@Override
	public int compareTo(ExonUnion exonToCompare) {
		if (this.min < exonToCompare.getMin())	return -1;
		else return 1;
	}

	public String getStrand() {
		// TODO Auto-generated method stub
		return strand;
	}
}
