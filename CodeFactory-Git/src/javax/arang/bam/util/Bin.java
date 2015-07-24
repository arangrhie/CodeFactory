/**
 * 
 */
package javax.arang.bam.util;

import java.util.Vector;

/**
 * @author Arang Rhie
 *
 */
public class Bin {

	// Bin
	private long bin = -1;
	
	// Chunk
	public static final int SHIFT_AMOUNT = 16;
	public static final int OFFSET_MASK = 0xffff;
	public static final long ADDRESS_MASK = 0xFFFFFFFFFFFFL;
	private Vector<Long> chunkCoffsetBegs;
	private Vector<Long> chunkCoffsetEnds;
	private Vector<Integer> chunkUoffsetBegs;
	private Vector<Integer> chunkUoffsetEnds;
	
	public static short C_OFFSET_BEGIN = 0;
	public static short U_OFFSET_BEGIN = 1;
	
	public Bin(long bin) {
		this.bin = bin;
		chunkCoffsetBegs = new Vector<Long>();
		chunkCoffsetEnds = new Vector<Long>();
		chunkUoffsetBegs = new Vector<Integer>();
		chunkUoffsetEnds = new Vector<Integer>();
	}
	
	public long getBin() {
		return bin;
	}
	
	public void addChunk(long cOffsetBeg, int uOffsetBeg, long cOffsetEnd, int uOffsetEnd) {
		chunkCoffsetBegs.add(cOffsetBeg & 0xFFFFFFFFFFFFL);
		chunkUoffsetBegs.add(uOffsetBeg);
		chunkCoffsetEnds.add(cOffsetEnd & 0xFFFFFFFFFFFFL);
		chunkUoffsetEnds.add(uOffsetEnd);
	}
	
	public Long[] getChunk(int numChunk) {
		Long[] chunk = {
				chunkCoffsetBegs.get(numChunk),
				(long)chunkUoffsetBegs.get(numChunk),
				chunkCoffsetEnds.get(numChunk),
				(long)chunkUoffsetEnds.get(numChunk)};
		return chunk;
	}

	/**
	 * @return
	 */
	public int getNumChunks() {
		// TODO Auto-generated method stub
		return chunkCoffsetBegs.size();
	}
}
