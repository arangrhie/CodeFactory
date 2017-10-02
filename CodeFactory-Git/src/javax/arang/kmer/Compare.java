package javax.arang.kmer;

import java.util.HashMap;
import java.util.Set;

public class Compare {

	public static int numOverlaps(HashMap<String, Integer> table1, HashMap<String, Integer> table2) {
		int count = 0;
		for (String hash : table1.keySet()) {
			if (table2.containsKey(hash)) {
				count++;
			}
		}
		return count;
	}
	
	public static int numOverlaps(KmerQryTable kmerQryTable, KmerCountQryTable kmerTargetTable) {
		int count = 0;
		Set<String> postfixTable;
		for (String prefix : kmerQryTable.getPrefixSet()) {
			postfixTable = kmerQryTable.getPostfixSet(prefix);
			for (String postfix : postfixTable) {
				if (kmerTargetTable.queryTable(prefix, postfix) > 0) {
					count++;
				}
			}
		}
		return count;
	}
	
	public static int numOverlaps(KmerQryTable kmerQryTable, KmerQryTable kmerTargetTable) {
		int count = 0;
		Set<String> postfixTable;
		for (String prefix : kmerQryTable.getPrefixSet()) {
			postfixTable = kmerQryTable.getPostfixSet(prefix);
			for (String postfix : postfixTable) {
				if (kmerTargetTable.hasKmer(prefix, postfix)) {
					count++;
				}
			}
		}
		return count;
	}
	
	/***
	 * 
	 * @param kmerTable	generated with KmerQryTable.addTable()
	 * @param seq could be any string
	 * @param kSize
	 * @return true if seq contains a kmer from kmerTable
	 */
	public static boolean hasOverlap(KmerQryTable kmerTable, String seq, int kSize) {
		if (seq.length() > kSize) {
			for (int i = 0; i < seq.length() - kSize; i++) {
				if (kmerTable.hasKmer(seq.substring(i, i + kSize))) {
					return true;
				}
			}
		} else if (seq.length() == kSize) {
			return kmerTable.hasKmer(seq);
		}
		return false;
	}
	

	
}
