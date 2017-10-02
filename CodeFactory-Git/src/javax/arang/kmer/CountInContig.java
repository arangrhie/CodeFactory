package javax.arang.kmer;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;

public class CountInContig extends R2wrapper {

	@Override
	public void hooker(FileReader frMerylFasta, FileReader frAsmFasta) {
		KmerQryTable merylCount = new KmerQryTable(k);
		merylCount.readKmerFasta(frMerylFasta);
		
		KmerQryTable asmCount = new KmerQryTable(k);
		String line;
		String tmp = "";
		String contig = null;
		while (frAsmFasta.hasMoreLines()) {
			line = frAsmFasta.readLine();
			if (line.startsWith(">")) {
				// print kmers
				if (contig != null) {
					System.out.println(contig + "\t" + Compare.numOverlaps(asmCount, merylCount) + "\t" + asmCount.getTableSize());
				}
				tmp = "";
				contig = line.substring(1);
				asmCount.clearTable();
				continue;
			}
			asmCount.computeKmers(tmp + line);
			tmp = "";

			// add end bases
			if (line.length() >= k) {
				tmp = line.substring(line.length() - k + 1, line.length());
			}
		}
		if (contig != null) {
			System.out.println(contig + "\t" + Compare.numOverlaps(asmCount, merylCount) + "\t" + asmCount.getTableSize());
		}
	}


	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar kmerCountInContig.jar <meryl.count> <contig.fasta> [k=21]");
		System.out.println("\t<meryl.count>: meryl dump fastq file with counts in the fasta name or a simple kmer count");
		System.out.println("\t<contig.fasta>: assembly.fasta");
		System.out.println("\t<sysout>: Reports the number of kmers found in each contig intersecting meryl.fasta");
		System.out.println("Arang Rhie, 2017-09-22. arrhie@gmail.com");
	}

	public static int k = 21;
	public static void main(String[] args) {
		if (args.length == 3) {
			k = Integer.parseInt(args[2]);
			new CountInContig().go(args[0], args[1]);
		} else if (args.length == 2) {
			new CountInContig().go(args[0], args[1]);
		} else {
			new CountInContig().printHelp();
		}
	}

}
