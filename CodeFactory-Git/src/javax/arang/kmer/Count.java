package javax.arang.kmer;

import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class Count extends Rwrapper{

	private HashMap<String, Integer> countTable = null;
	public int kSize = 0;
	
	public Count(int k) {
		countTable = new HashMap<String, Integer>();
		kSize = k;
	}
	
	char[] kmerArr;
	
	public void addCount(String seq) {
		String kmer = null;
		for (int i = 0; i <= seq.length() - kSize; i++) {
			kmer = Kmer.toKmer(seq.substring(i, i + kSize));
			if (kmer == null) {
				continue;
			}
			kmer = String.valueOf(kmerArr);			
			if (this.countTable.containsKey(kmer)) {
				this.countTable.put(kmer, this.countTable.get(kmer) + 1);
			} else {
				this.countTable.put(kmer, 1);
			}
		}
	}
	
	public void addCount(String seq, int count) {
		this.countTable.put( Kmer.toKmer(seq), count);
	}
	
	/***
	 * Hashmap table for kmers.
	 * Key: kmer in byte[]
	 * Value: counts in Integer
	 * @return
	 */
	public HashMap<String, Integer> getTable() {
		return countTable;
	}
	
	@Override
	public void hooker(FileReader fr) {
		
		String line;
		
		if (fr.getFileName().endsWith(".fa") || fr.getFileName().endsWith(".fasta")) {
			String tmp = "";	// leftover from the previous fasta line (if any)
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				if (line.startsWith(">")) {
					tmp = "";
					continue;
				} else if (!tmp.equals("")){
					this.addCount(tmp + line.substring(0, this.kSize - 1));
					tmp = "";
				}
				this.addCount(line);
				// add end / beginning bases
				if (line.length() >= kSize - 1) {
					tmp = line.substring(line.length() - this.kSize + 1, line.length());
				}
			}
		} else if (fr.getFileName().endsWith(".fastq")) {
			short lineNum = -1;
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				if (lineNum % 4 == 0) {
					this.addCount(line);
					lineNum = 0;
				}
				lineNum++;
			}
		} else if (fr.getFileName().endsWith(".counts") || fr.getFileName().endsWith(".count")) {
			String[] tokens;
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				tokens = line.split(RegExp.WHITESPACE);
				this.addCount(tokens[0], Integer.parseInt(tokens[1]));
			}
			
		}
		
		System.err.println("K-mer loading completed for " + fr.getFileName() + ". " + this.getTable().size() + " entries");
	}
	
	public void printCounts() {
		for (String kmerKey : this.getTable().keySet()) {
			System.out.println(Kmer.toBases(kmerKey, kSize) + "\t" + this.getTable().get(kmerKey));
		}
	}
	
	public void clearTable() {
		this.countTable.clear();
	}
	
	public static void main(String[] args) {
		if (args.length == 2) {
			new Count(Integer.parseInt(args[1])).go(args[0]);
		} else {
			new Count(0).printHelp();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar kmerCount.jar <in> <k>");
		System.out.println("\t<in>: .fasta, .fa file / .fastq file / .counts or .count file");
		System.out.println("\t<k>: k-size");
		System.out.println("\t<stdout>: kmer count table. K-mer in bytes\tCount");
		System.out.println("Arang Rhie, 2017-07-14. arrhie@gmail.com");
	}



}
