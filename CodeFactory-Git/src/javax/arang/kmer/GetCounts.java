package javax.arang.kmer;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class GetCounts extends R2wrapper {

	@Override
	public void hooker(FileReader frKmerCount, FileReader frFasta) {
		String line = frKmerCount.readLine();
		String[] tokens = line.split(RegExp.WHITESPACE);
		int k = tokens[0].length();
		if (frKmerCount.getFileName().endsWith(".fa") || frKmerCount.getFileName().endsWith(".fasta")) {
			line = frKmerCount.readLine();
			k = line.length();
		}
		System.err.println("[DEBUG] k: " + k);
		
		KmerCountQryTable counts = new KmerCountQryTable(k);
		frKmerCount.reset();
		counts.readKmerFile(frKmerCount);
		
		String tmp = "";
		String contig = null;
		String sequence;
		
		while (frFasta.hasMoreLines()) {
			line = frFasta.readLine();
			if (line.startsWith(">")) {
				// print kmers
				if (contig != null) {
					System.out.println();
				}
				tmp = "";
				contig = line.substring(1);
				System.out.print(contig);
				continue;
			}
			sequence = tmp + line;
			for (int i = 0; i <= sequence.length() - k; i++) {
				System.out.print(" " + counts.queryTableCount(sequence.substring(i, i + k)));
			}
			
			tmp = "";

			// add end bases
			if (line.length() >= k) {
				tmp = line.substring(line.length() - k + 1, line.length());
			}
		}
		if (contig != null) {
			System.out.println();
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar kmerGetCounts.jar <kmer.counts> <fasta>");
		System.out.println("\t<stdout>: counts from <kmer.counts> of k-mers in <fasta>");
		System.out.println("\t<kmer.counts>: K-MER\tCOUNT");
		System.out.println("\t<fasta>: Any fasta file");
		System.out.println("Arang Rhie, 11-22-2017. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new GetCounts().go(args[0], args[1]);
		} else {
			new GetCounts().printHelp();
		}
	}

}
