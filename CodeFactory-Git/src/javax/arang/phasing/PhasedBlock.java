package javax.arang.phasing;

public class PhasedBlock implements Comparable<PhasedBlock> {
	
	public static short CHR = 0;
	public static short START = 1;
	public static short END = 2;
	public static short ID = 3;
	private String chr;
	private int blockStart;
	private int blockEnd;
	private String blockID;
	
	public PhasedBlock(String chr, int start, int end, String blockID) {
		this.chr = chr;
		this.blockStart = start;
		this.blockEnd = end;
		this.blockID = blockID;
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
	
}
