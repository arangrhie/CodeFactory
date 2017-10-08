package javax.arang.kmer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

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
	private double size;
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
	protected void updateTableSize() {
		if (tableChanged) {
			size = 0;
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
	public double getTableSize() {
		updateTableSize();
		return size;
	}
	
	public String getTablePrintableSize() {
		updateTableSize();
		return String.format("%,.0f", size);
	}
	
	public Set<String> getPrefixSet() {
		return kmerTable.keySet();
	}
	
	public Set<String> getPostfixSet(String prefix) {
		return kmerTable.get(prefix);
	}
	
	public void clearTable() {
		for (String prefix : kmerTable.keySet()) {
			kmerTable.get(prefix).clear();
		}
		kmerTable.clear();
		tableChanged = true;
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
	
	public boolean hasKmer(String kmer) {
		prefix = Kmer.toKmer(kmer.substring(0, prefixLen));
		postfix = Kmer.toKmer(kmer.substring(prefixLen));
		if (kmerTable.containsKey(prefix) && kmerTable.get(prefix).contains(postfix)) {
			return true;
		}
		return false;
	}
	
	public boolean hasKmer(String prefix, String postfix) {
		if (kmerTable.containsKey(prefix) && kmerTable.get(prefix).contains(postfix)) {
			return true;
		}
		return false;
	}
	
	/***
	 * Reads in meryl dump kmer fasta count or list
	 * Assemes each line = 1 kmer
	 * @param fr
	 */
	public void readKmerFile(FileReader fr) {
		String line;
		if (fr.getFileName().endsWith(".fa") || fr.getFileName().endsWith(".fasta")) {
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				if (line.startsWith(">")) {
				} else {
					this.addTable(line);
				}
			}
		} else if (fr.getFileName().endsWith(".counts") || fr.getFileName().endsWith(".count")) {
			String[] tokens;
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				tokens = line.split(RegExp.WHITESPACE);
				this.addTable(tokens[0]);
			}
		} else if (fr.getFileName().endsWith(".list")) {
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				this.addTable(line);
			}
		}
		System.err.println("Successfully loaded " + this.getTableSize() + " kmers.");
	}
}
