package javax.arang.phasing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Phase {
	public static ArrayList<PhasedSNP> getSnpsInRead(int seqStart, int seqEnd,
			Integer[] snpPosList,
			HashMap<Integer, PhasedSNP> snpPosToPhasedSNPmap) {
		
		ArrayList<PhasedSNP> snpsInRead = new ArrayList<PhasedSNP>();
		int snpStartIdx = Arrays.binarySearch(snpPosList, seqStart);
		// snpStartIdx will be the closest, min SNP greater than the seqStart
		if (snpStartIdx < 0) {
			snpStartIdx += 1;	// to find 1 pos right to the seqStart 
			snpStartIdx *= -1;
		}
		// all snps are smaller than seqStart
		if (snpStartIdx == snpPosList.length) {
			return snpsInRead;
		}
		
		int snpEndIdx = Arrays.binarySearch(snpPosList, seqEnd);
		// snpEndIdx will be the closest, max SNP less than the seqEnd
		if (snpEndIdx < 0) {
			snpEndIdx += 2;	// to find 1 pos left to the seqEnd 
			snpEndIdx *= -1;
		}
		if (snpEndIdx == snpPosList.length) {
			snpEndIdx = snpPosList.length - 1;
		}
		
		for (int idx = snpStartIdx; idx <= snpEndIdx; idx++) {
			snpsInRead.add(snpPosToPhasedSNPmap.get(snpPosList[idx]));
		}
		
		return snpsInRead;
	}

}
