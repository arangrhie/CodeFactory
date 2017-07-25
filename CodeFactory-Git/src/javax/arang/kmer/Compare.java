package javax.arang.kmer;

import javax.arang.IO.basic.Wrapper;

public class Compare extends Wrapper {

	public static int kSize = 31;
	public void compare(String f1, String f2) {
		Count f1Count = new Count(kSize);
		f1Count.go(f1);
		Count f2Count = new Count(kSize);
		f2Count.go(f2);
		
	}
	
	public static void main(String[] args) {
		if (args.length == 2) {
			new Compare().compare(args[0], args[1]);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar kmerCompare.jar <file1> <file2> [k_size]");
		System.out.println("\t<file1>: fasta or fastq file");
		System.out.println("\t<file2>: fasta or fastq file");
		System.out.println("\t[k_size]: DEFAULT=31. Integer.");
		System.out.println("\t<sysout>: basic stats.");
		System.out.println("\t\tTotal no. of k-mers");
		System.out.println("\t\tShared no. of k-mers");
		System.out.println("Arang Rhie, 2017-07-22. arrhie@gmail.com");
	}

}
