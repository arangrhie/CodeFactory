package javax.arang.kmer;

import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

public class Count extends Rwrapper{

	private HashMap<byte[], Integer> countTable = null;
	private int kSize = 0;
	
	public Count() {}
	
	public Count(int k) {
		countTable = new HashMap<byte[], Integer>();
		kSize = k;
	}
	
	public void addCount(String seq) {
		byte[] kmer;
		for (int i = 0; i < seq.length() - kSize; i++) {
			kmer = Kmer.toKmer(seq.substring(i, i + kSize));
			if (kmer == null) {
				continue;
			}
			if (this.countTable.containsKey(kmer)) {
				this.countTable.put(kmer, this.countTable.get(kmer) + 1);
			} else {
				this.countTable.put(kmer, 1);
			}
		}
	}
	
	/***
	 * Hashmap table for kmers.
	 * Key: kmer in byte[]
	 * Value: counts in Integer
	 * @return
	 */
	public HashMap<byte[], Integer> getTable() {
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
				if (line.length() > kSize) {
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
		}
		
		System.err.println("K-mer loading completed. Start to write out " + this.getTable().size() + " entries");
		
		for (byte[] kmerKey : this.getTable().keySet()) {
			System.out.println(Kmer.toBases(kmerKey) + "\t" + this.getTable().get(kmerKey));
		}
		
	}
	
	public static void main(String[] args) {
		if (args.length == 2) {
			new Count(Integer.parseInt(args[1])).go(args[0]);
		} else {
			new Count().printHelp();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar kmerCount.jar <in> <k>");
		System.out.println("\t<in>: .fasta, .fa file or .fastq file");
		System.out.println("\t<k>: k-size");
		System.out.println("\t<stdout>: kmer count table. K-mer in bytes\tCount");
		System.out.println("Arang Rhie, 2017-07-14. arrhie@gmail.com");
	}

}
