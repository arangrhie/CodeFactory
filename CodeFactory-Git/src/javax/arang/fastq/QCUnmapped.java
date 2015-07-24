package javax.arang.fastq;

import java.util.ArrayList;
import java.util.Arrays;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.IOUtil;

public class QCUnmapped extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String seq;
		String qual;
		ArrayList<Long> meanQualVals = new ArrayList<Long>();
		int numReads = 0;
		int[][] atgcnCounts = new int[5][];
		int seqLen = 0;
		boolean isFirstRead = true;
		long qualSum = 0;
		long meanQual = 0;
		int numNAQual = 0;
		int baseQual = 0;
		int len = 0;
		int countCG = 0;
		int numCG70 = 0;
		while (fr.hasMoreLines()) {
			// read ID
			fr.readLine();
			numReads++;
			
			// sequence
			seq = fr.readLine();
			if (isFirstRead) {
				seqLen = seq.length();
				for (int i = 0; i < 5; i++) {
					atgcnCounts[i] = new int[seqLen];
					for (int j = 0; j < seqLen; j++) {
						atgcnCounts[i][j] = 0;
					}
				}
				isFirstRead = false;
			}
			
			for (int i = 0; i < seqLen; i++) {
				atgcnCounts[getBase(seq.charAt(i))][i]++;
				countCG = 0;
				if (seq.charAt(i) == 'C' || seq.charAt(i) == 'G') {
					countCG++;
				}
			}
			if (countCG / seqLen > 70) {
				numCG70++;
			}
			
			
			// qual + line
			fr.readLine();
			
			// quality
			qual = fr.readLine();
			qualSum = 0;
			len = 0;
			for (int i = 0; i < seqLen; i++) {
				if (qual.charAt(i) == '#') {
					numNAQual++;
				} else {
					len++;
					baseQual = (int)qual.charAt(i) - 33;
					qualSum += baseQual;
				}
			}
			if (len > 0) {
				meanQualVals.add(qualSum / len);
			}
		}
		
		fm.writeLine("Number of reads processed: " + numReads);
		fm.writeLine("A/C/G/T/N counts");
		
		for (int i = 0; i < 5; i++) {
			if (i == 0)	fm.write("A");
			if (i == 1)	fm.write("C");
			if (i == 2)	fm.write("G");
			if (i == 3)	fm.write("T");
			if (i == 4)	fm.write("N");
			for (int j = 0; j < seqLen; j++) {
				fm.write("\t" + atgcnCounts[i][j]);
			}
			fm.writeLine();
		}
		fm.writeLine("Draw a bar chart for these values by your self.");
		fm.writeLine();
		fm.writeLine("Number of reads with GC content > 70%: " + numCG70);
		fm.writeLine();
		fm.writeLine("Quality distribution");
		fm.writeLine("Number of qualities marked as \'#\' (Unknown): " + numNAQual);
		qualSum = 0;
		for (int i = 0; i < meanQualVals.size(); i++) {
			qualSum += meanQualVals.get(i);
		}
		meanQual = (long) qualSum / numReads;
		fm.writeLine("Mean of the quality value excluding \'#\': " + meanQual);
		Long[] tempArr = new Long[meanQualVals.size()];
		tempArr = meanQualVals.toArray(tempArr);
		Arrays.sort(tempArr);
		fm.writeLine("Mean quality min: " + tempArr[0]);
		fm.writeLine("Mean quality 1Q: " + tempArr[(tempArr.length - 1)/4]);
		fm.writeLine("Mean quality median: " + tempArr[(tempArr.length - 1)/2]);
		fm.writeLine("Mean quality 3Q: " + tempArr[3 * (tempArr.length - 1)/4]);
		fm.writeLine("Mean quality max: " + tempArr[tempArr.length - 1]);
	}
	
	private int getBase(char base) {
		switch(base) {
		case 'A':	return 0;
		case 'C':	return 1;
		case 'G': 	return 2;
		case 'T':	return 3;
		case 'N':	return 4;
		}
		return 5;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastqQCUnmapped.jar <in.fastq>");
		System.out.println("This program is written to answear 3 questions:");
		System.out.println("\t1. Is the primer included at the end of the read?");
		System.out.println("\t2. Is the sequence high in GC content?");
		System.out.println("\t3. What is the mean quality distribution on each read?");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new QCUnmapped().go(args[0], "./" + IOUtil.retrieveFileName(args[0]) + ".qc");
		} else {
			new QCUnmapped().printHelp();
		}
	}

}
