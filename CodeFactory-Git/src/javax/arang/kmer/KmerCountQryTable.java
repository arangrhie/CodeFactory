package javax.arang.kmer;

import java.util.HashMap;

import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

/***
 * Use this class to build & call k-mer table for heavy searching of specific k-mer queries.
 * @author rhiea
 *
 */
public class KmerCountQryTable {

	/***
	 * kmerTable = <prefix, postfixTable<postfix, count>
	 */
	private HashMap<String, HashMap<String, Integer>> kmerTable;
	private HashMap<String, Integer> postfixTable;
	private int prefixLen;
	private int size;
	private boolean tableChanged = true;
	
	public KmerCountQryTable(int k) {
		this.prefixLen = k/2;
		kmerTable = new HashMap<String, HashMap<String, Integer>>();
	}
	private String prefix;
	private String postfix;
	
	public void addTable(String seq) {
		prefix = Kmer.toKmer(seq.substring(0, prefixLen));
		postfix = Kmer.toKmer(seq.substring(prefixLen));
		
		if (kmerTable.containsKey(prefix)) {
			postfixTable = kmerTable.get(prefix);
			postfixTable.put(postfix, postfixTable.get(postfix) + 1);
		} else {
			postfixTable = new HashMap<String, Integer>();
			postfixTable.put(postfix, 1);
		}
		
		this.kmerTable.put(prefix, postfixTable);
		tableChanged = true;
	}
	
	/***
	 * For fast reading, if we know the kmer and count, add it to the table without checking
	 * @param seq
	 * @param count
	 */
	public void addTable(String seq,int count) {
		prefix = Kmer.toKmer(seq.substring(0, prefixLen));
		postfix = Kmer.toKmer(seq.substring(prefixLen));
		
		if (kmerTable.containsKey(prefix)) {
			postfixTable = kmerTable.get(prefix);
		} else {
			postfixTable = new HashMap<String, Integer>();
		}
		postfixTable.put(postfix, count);
		
		this.kmerTable.put(prefix, postfixTable);
		tableChanged = true;
	}
	
	/***
	 * 
	 * @param seq
	 * @return count of kmer seq in this table, or 0 if not presented.
	 */
	public int queryTableCount(String seq) {
		prefix = Kmer.toKmer(seq.substring(0, prefixLen));
		postfix = Kmer.toKmer(seq.substring(prefixLen));
		
		if (kmerTable.containsKey(prefix) && kmerTable.get(prefix).containsKey(postfix)) {
			return kmerTable.get(prefix).get(postfix);
		}
		return 0;
	}
	
	public int queryTable(String prefix, String postfix) {
		if (kmerTable.containsKey(prefix) && kmerTable.get(prefix).containsKey(postfix)) {
			return kmerTable.get(prefix).get(postfix);
		}
		return 0;
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

	/***
	 * Reads in meryl dump kmer fasta count
	 * @param fr
	 */
	public void readKmerFasta(FileReader fr) {
		String line;
		int count = 0;
		if (fr.getFileName().endsWith(".fa") || fr.getFileName().endsWith(".fasta")) {
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				if (line.startsWith(">")) {
					count = Integer.parseInt(line.substring(1));
				} else {
					this.addTable(line, count);
				}
			}
		} else if (fr.getFileName().endsWith(".counts") || fr.getFileName().endsWith(".count")) {
			String[] tokens;
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				tokens = line.split(RegExp.WHITESPACE);
				this.addTable(tokens[0], Integer.parseInt(tokens[1]));
			}
		}
		System.err.println("Successfully loaded " + this.getTableSize() + " kmers.");
	}
}
