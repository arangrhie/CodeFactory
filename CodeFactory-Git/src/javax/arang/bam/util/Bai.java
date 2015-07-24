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
public class Bai {
	
	/***
	 * HashMap<refID, refBin>
	 */
	private HashMap<Integer, RefBin> refBins;
	
	public Bai() {
		refBins = new HashMap<Integer, RefBin>();
	}
	
	public void addRefBin(int refID, RefBin refBin) {
		refBins.put(refID, refBin);
	}
	
	public int getNumRefBins() {
		return refBins.size();
	}
	
	public int getNumBins(int refID) {
		return refBins.get(refID).getNumBins();
	}
	
	public RefBin getRefBin(int refId) {
		return refBins.get(refId);
	}
	
	/***
	 * 
	 * @param refId
	 * @param beg
	 * @param end
	 * @return HashMap<binID, Vector<Long[chunk_beg], Long[chunk_end]>>
	 */
	public Vector<Long[]> getChunks(int refId, long beg, long end) {
		RefBin refBin = this.getRefBin(refId);
		
		Vector<Integer> binIDs = regionTo16KbBins(beg, end);
		Vector<Long[]> chunks = new Vector<Long[]>(); 
		for (int binID : binIDs) {
			Bin bin = refBin.getBin(binID);
			if (bin == null)	return null;
			for (int i = 0; i < bin.getNumChunks(); i++) {
				chunks.add(bin.getChunk(i));
			}
		}
		return chunks;
	}
	
	
	public static long region2Bin(long beg, long end) {
		--end;
		if (beg >>> 14 == end >>> 14)	return ((1 << 15) - 1)/7 + (beg >>> 14);
		if (beg >>> 17 == end >>> 17)	return ((1 << 12) - 1)/7 + (beg >>> 17);
		if (beg >>> 20 == end >>> 20)	return ((1 << 9) - 1)/7 + (beg >>> 20);
		if (beg >>> 23 == end >>> 23)	return ((1 << 6) - 1)/7 + (beg >>> 23);
		if (beg >>> 26 == end >>> 26)	return ((1 << 3) - 1)/7 + (beg >>> 26);
		return 0;
	}
	
	/***
	 * bin 1 – 8 : 64Mbp
	 * bin 9 – 72 : 8Mbp
	 * bin 73 – 584 : 1Mbp
	 * bin 585 – 4680 : 128Kbp
	 * bin 4681 – 37449 : 16Kbp
	 * @param beg
	 * @param end
	 * @return All bins containing region [beg - end).
	 */
	public static Vector<Integer> region2Bins(long beg, long end) {
		Vector<Integer> list = new Vector<Integer>();
		int k = 0;
		--end;
		list.add(0);
		for (k = (int) (1 + (beg >>> 26)); k <= 1 + (end >>> 26); ++k)	list.add(k);
		for (k = (int) (9 + (beg >>> 23)); k <= 9 + (end >>> 23); ++k)	list.add(k);
		for (k = (int) (73 + (beg >>> 20)); k <= 73 + (end >>> 20); ++k)	list.add(k);
		for (k = (int) (585 + (beg >>> 17)); k <= 585 + (end >>> 17); ++k)	list.add(k);
		for (k = (int) (4681 + (beg >>> 14)); k <= 4681 + (end >>> 14); ++k)	list.add(k);
		return list;
	}
	
	public static Vector<Integer> regionTo16KbBins(long beg, long end) {
		Vector<Integer> list = new Vector<Integer>();
		int k = 0;
		--end;
		for (k = (int) (4681 + (beg >>> 14)); k <= 4681 + (end >>> 14); ++k)	list.add(k);
		return list;
	}
	
}
