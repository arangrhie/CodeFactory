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
		// TODO Auto-generated method stub
		return blockStart;
	}

	public int getEnd() {
		// TODO Auto-generated method stub
		return blockEnd;
	}

	public String getPS() {
		// TODO Auto-generated method stub
		return blockID;
	}

	public String getChr() {
		// TODO Auto-generated method stub
		return this.chr;
	}

	@Override
	public int compareTo(PhasedBlock otherBlock) {
		// TODO Auto-generated method stub
		return this.blockEnd - otherBlock.getStart();
	}

	public void setBlockEnd(int end) {
		this.blockEnd = end;
	}
	
}
