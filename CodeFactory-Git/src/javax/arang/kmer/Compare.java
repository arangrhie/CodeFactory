package javax.arang.kmer;

import java.util.HashMap;
import java.util.Set;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;

public class Compare extends R2wrapper {

	public static int kSize = 21;

	@Override
	public void hooker(FileReader fr1, FileReader fr2) {
		Count f1Count = new Count(kSize);
		f1Count.hooker(fr1);
		Count f2Count = new Count(kSize);
		f2Count.hooker(fr2);
		
		
	}

	
	public static int numOverlaps(HashMap<String, Integer> table1, HashMap<String, Integer> table2) {
		int count = 0;
		for (String hash : table1.keySet()) {
			if (table2.containsKey(hash)) {
				count++;
			}
		}
		return count;
	}
	
	public static int numOverlaps(KmerQryTable kmerTable, KmerCountQryTable kmerCountTable) {
		int count = 0;
		Set<String> postfixTable;
		for (String prefix : kmerTable.getPrefixSet()) {
			postfixTable = kmerTable.getPostfixSet(prefix);
			for (String postfix : postfixTable) {
				if (kmerCountTable.queryTable(prefix, postfix) > 0) {
					count++;
				}
			}
		}
		return count;
	}

	
	public static void main(String[] args) {
		if (args.length == 3) {
			kSize = Integer.parseInt(args[2]);
			new Compare().go(args[0], args[1]);
		} else if (args.length == 2) {
			new Compare().go(args[0], args[1]);
		} else {
			new Compare().printHelp();
		}
	}

	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar kmerCompare.jar <file1> <file2> [k_size=21]");
		System.out.println("\t<file1>: fasta or fastq, or counts file");
		System.out.println("\t<file2>: fasta or fastq, or counts file");
		System.out.println("\t[k_size]: DEFAULT=21. Integer.");
		System.out.println("\t<sysout>: basic stats.");
		System.out.println("\t\tTotal no. of k-mers");
		System.out.println("\t\tShared no. of k-mers");
		System.out.println("Arang Rhie, 2017-07-22. arrhie@gmail.com");
	}
}
