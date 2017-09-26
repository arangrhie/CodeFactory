package javax.arang.kmer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/***
 * This table is for simple unique kmer handling; i.e. exact matchs
 * @author rhiea
 *
 */
public class KmerQryTable {
	private HashMap<String, HashSet<String>> kmerTable;
	private HashSet<String> postfixSet;
	private int kSize;
	private int prefixLen;
	private int size;
	private boolean tableChanged = true;
	
	public KmerQryTable(int k) {
		this.kSize = k;
		this.prefixLen = k/2;
		kmerTable = new HashMap<String, HashSet<String>>();
	}
	private String prefix;
	private String postfix;
	
	public void addTable(String seq) {
		prefix = Kmer.toKmer(seq.substring(0, prefixLen));
		postfix = Kmer.toKmer(seq.substring(prefixLen));
		if (kmerTable.containsKey(prefix)) {
			postfixSet = kmerTable.get(prefix);
		} else {
			postfixSet = new HashSet<String>();
			kmerTable.put(prefix, postfixSet);
		}
		if (postfixSet.add(postfix)) {
			tableChanged = true;
		}
	}
	
	/***
	 * only update table size if table has an update (insert)
	 */
	private void updateTableSize() {
		if (tableChanged) {
			for (String key : kmerTable.keySet()) {
				size += kmerTable.get(key).size();
			}
		}
		tableChanged = false;
	}
	
	/***
	 * Get the total number of kmers in this table
	 * @return
	 */
	public int getTableSize() {
		updateTableSize();
		return size;
	}
	
	public Set<String> getPrefixSet() {
		return kmerTable.keySet();
	}
	
	public Set<String> getPostfixSet(String prefix) {
		return kmerTable.get(prefix);
	}
	
	public void clearTable() {
		kmerTable.clear();
	}

	public void computeKmers(String seq) {
		if (seq.length() < kSize)	return;
		for (int i = 0; i < seq.length() - kSize; i++) {
			this.addTable(seq.substring(i, i + kSize));
		}
	}
	
	public void printKmers() {
		for (String pre : this.kmerTable.keySet()) {
			prefix = Kmer.toBases(pre, prefixLen);
			for (String post : this.kmerTable.get(pre)) {
				postfix = Kmer.toBases(post, kSize - prefixLen);
				System.out.println(prefix + postfix);
			}
		}
	}
}
