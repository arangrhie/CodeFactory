package javax.arang.kmer;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ContainsInFastq extends R2wrapper {

	private static String r2fastq = "";
	private static boolean isPairedEnd = false; 
	private static boolean reportNotContained = false;
			
	private static int SEQ = 0;
	
	@Override
	public void hooker(FileReader frCnt, FileReader frR1) {
		
		FileMaker fmR1 = new FileMaker(frR1.getFullPath().replace(".fastq", ".filt.fastq"));
		FileReader frR2 = null;
		FileMaker fmR2 = null;
		if (isPairedEnd) {
			frR2 = new FileReader(r2fastq);
			fmR2 = new FileMaker(frR2.getFullPath().replace(".fastq", ".filt.fastq"));
		}
		
		String[] tokens = frCnt.readLine().split(RegExp.WHITESPACE);
		int k = tokens[SEQ].length();
		System.err.println("K=" + k);
		KmerQryTable kmersToFind = new KmerQryTable(k);
		kmersToFind.addTable(tokens[SEQ]);
		while (frCnt.hasMoreLines()) {
			tokens = frCnt.readLine().split(RegExp.WHITESPACE);
			kmersToFind.addTable(tokens[SEQ]);
		}
		System.err.println("Loaded " + kmersToFind.getTableSize() + " kmers from " + frCnt.getFileName());

		boolean hasOverlap = false;
		if (reportNotContained) {
			hasOverlap = true;
		}
		
		String seq1 = null;
		String seq2 = null;
		int lineNum = 0;
		StringBuffer seqBuff1 = new StringBuffer();
		StringBuffer seqBuff2 = new StringBuffer();
		double totalCount = 0;
		double filteredCount = 0;
		while (frR1.hasMoreLines()) {
			seq1 = frR1.readLine();
			if (isPairedEnd) {
				seq2 = frR2.readLine();
			}
			lineNum++;
			if (lineNum %4 == 1) {
				// beginning of a new sequence. read id line.
				totalCount++;
				if ((hasOverlap && !reportNotContained) || (!hasOverlap && reportNotContained)) {
					fmR1.write(seqBuff1.toString());
					if (isPairedEnd) {
						fmR2.write(seqBuff2.toString());
					}
					filteredCount++;
				}
				hasOverlap = false;
				seqBuff1 = new StringBuffer(seq1 + "\n");
				if (isPairedEnd) {
					seqBuff2 = new StringBuffer(seq2 + "\n");
				}
			} else if (lineNum %4 == 2) {
				// actual sequence.
				if (Compare.hasOverlap(kmersToFind, seq1, k) || (isPairedEnd && Compare.hasOverlap(kmersToFind, seq2, k))) {
					//System.err.println(seq1);
					seqBuff1.append(seq1 + "\n");
					if (isPairedEnd) {
						seqBuff2.append(seq2 + "\n");
					}
					hasOverlap = true;
				} else if (!hasOverlap && reportNotContained){
					seqBuff1.append(seq1 + "\n");
					if (isPairedEnd) {
						seqBuff2.append(seq2 + "\n");
					}
				}
			} else {
				if ((hasOverlap && !reportNotContained) || (!hasOverlap && reportNotContained)) {
					seqBuff1.append(seq1 + "\n");
					if (isPairedEnd) {
						seqBuff2.append(seq2 + "\n");
					}
				}
			}
			
			if (lineNum %4 == 0) {
				lineNum = 0;
			}
		}
		if ((hasOverlap && !reportNotContained) || (!hasOverlap && reportNotContained)) {
			fmR1.write(seqBuff1.toString());
			if (isPairedEnd) {
				fmR2.write(seqBuff2.toString());
			}
			filteredCount++;
		}
		
		if (isPairedEnd) {
			frR2.closeReader();
		}
		
		if (!reportNotContained) {
			System.err.println("Total reads containing kmers from " + kmersToFind.getTableSize() + " kmers:");
		} else {
			System.err.println("Total reads NOT containing kmers from " + kmersToFind.getTableSize() + " kmers:");
		}
		System.err.println(String.format("%,.0f", filteredCount) + " / " + String.format("%,.0f", totalCount));
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar kmerContainsInFastq.jar <meryl.count> <r1.fastq> [r2.fastq] [-v]");
		System.out.println("\t<meryl.count>: kmer\tcnt");
		System.out.println("\t<r1.fastq>: illumina style fastq file");
		System.out.println("\t[r2.fastq]: read 2 if paired end read needs to be looked up");
		System.out.println("\t[-v]: contains NOT. Filteres out reads containing kmers from <meryl.count>");
		System.out.println("\tReturn any fastq reads that contains kmers in <meryl.count>.");
		System.out.println("Arang Rhie, 2017-10-25. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ContainsInFastq().go(args[0], args[1]);			
		} else if (args.length == 3) {
			if (args[2].equals("-v")) {
				reportNotContained = true;
			} else {
				isPairedEnd = true;
				r2fastq = args[2];
			}
			new ContainsInFastq().go(args[0], args[1]);
		} else if (args.length == 4) {
			for (int i = 2; i < 4; i++) {
				if (args[i].equals("-v")) {
					reportNotContained = true;
				} else {
					isPairedEnd = true;
					r2fastq = args[i];
				}
			}
			new ContainsInFastq().go(args[0], args[1]);
		} else {
			new ContainsInFastq().printHelp();
		}
	}

}
