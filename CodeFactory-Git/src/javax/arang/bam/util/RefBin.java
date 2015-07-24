/**
 * 
 */
package javax.arang.bam.util;

import java.util.HashMap;
import java.util.Vector;


/**
 * @author Arang Rhie
 *
 */
public class RefBin {

	private HashMap<Long, Bin> bins = null;
	private Vector<Long> intvIOffsets = null;
	
	public RefBin() {
		this.bins = new HashMap<Long, Bin>();
		this.intvIOffsets = new Vector<Long>(); 
	}
	
	public void addBin(long binID) {
		Bin bin = new Bin(binID);
		bins.put(binID, bin);
	}
	
	public void addChunk(long bin, long chunkBeg, long chunkEnd) {
		bins.get(bin).addChunk(
				(chunkBeg >> Bin.SHIFT_AMOUNT) & Bin.ADDRESS_MASK,
				(int)chunkBeg & Bin.OFFSET_MASK,
				(chunkEnd >> Bin.SHIFT_AMOUNT) & Bin.ADDRESS_MASK,
				(int)chunkEnd & Bin.OFFSET_MASK);
	}
	
//	public void addChunk(long bin, int uOffsetBeg, long cOffsetBeg,
//			int uOffsetEnd, long cOffsetEnd) {
//		bins.get(bin).addChunk(cOffsetBeg, uOffsetBeg, cOffsetEnd, uOffsetEnd);
//	}
	
	
	public Bin getBin(long binID) {
		return bins.get(binID);
	}
	
	public void addIntvIoffset(Long ioffset) {
		intvIOffsets.add(ioffset);
	}
	
	public int getNumBins() {
		return bins.size();
	}
	
}
