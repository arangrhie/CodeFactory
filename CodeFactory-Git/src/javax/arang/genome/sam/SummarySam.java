package javax.arang.genome.sam;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class SummarySam extends IOwrapper {
	HashMap<Integer, Integer> seqNumTable = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> mappedNumTable = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> skippedNumTable = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> mismatchNumTable = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> delNumTable = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> hardclippedNumTable = new HashMap<Integer, Integer>();
	
	HashMap<Integer, Float> mappedLenTable = new HashMap<Integer, Float>();
	HashMap<Integer, Float> softclippedLenTable = new HashMap<Integer, Float>();	// S
	HashMap<Integer, Float> mismatchLenTable = new HashMap<Integer, Float>();	// mismatches
	HashMap<Integer, Float> deletionLenTable = new HashMap<Integer, Float>();	// D
	HashMap<Integer, Float> hardclippedLenTable = new HashMap<Integer, Float>();	// H
	
	static String refName = "";
	

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		int max = 0;
		float totalReads = 0;
		double totalBases = 0;
		float totalSecondaryAlignedReads = 0;
		double totalSecondaryAlignedBases = 0;
		float totalNonrefAlignedReads = 0;
		double totalNonrefAlignedBases = 0;
		float totalAlignedReads = 0;
		double totalAlignedBases = 0;
		int maxSeqLen = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.length() < 5)	continue;
			if (line.startsWith("@"))	continue;
			tokens = line.split("\t");
			String seq = tokens[Sam.SEQ];
			if (SAMUtil.isSecondaryAlignment(Integer.parseInt(tokens[Sam.FLAG]))) {
				totalSecondaryAlignedReads++;
				totalSecondaryAlignedBases += seq.length();
				continue;
			}
			totalReads++;
			totalBases += seq.length();
			if (tokens[Sam.CIGAR].equals("*"))	continue;
			if (tokens[Sam.RNAME].equals("*"))	continue;
			if (!tokens[Sam.RNAME].startsWith(refName)) {
				totalNonrefAlignedReads++;
				totalNonrefAlignedBases += seq.length();
				continue;
			}
			totalAlignedReads++;
			totalAlignedBases += seq.length();
			if (maxSeqLen < seq.length()) {
				maxSeqLen = seq.length();
			}
			if (seqNumTable.containsKey(seq.length())) {
				seqNumTable.put(seq.length(), seqNumTable.get(seq.length()) + 1);
			} else {
				seqNumTable.put(seq.length(), 1);
//				mappedLenTable.put(seq.length(), (float)Sam.getMatchedBases(tokens[Sam.CIGAR]));
//				skippedLenTable.put(seq.length(), (float)Sam.getSkippedBases(tokens[Sam.CIGAR]));
//				mismatchLenTable.put(seq.length(), (float)Sam.getMismatchedBases(tokens[Sam.MDTAG]));
//				deletionLenTable.put(seq.length(), (float)Sam.getDeletedBases(tokens[Sam.CIGAR]));
				if (max < seq.length()) {
					max = seq.length();
				}
			}
			putInTable(seq.length(), Sam.getMatchedBasesLen(tokens[Sam.CIGAR]), mappedLenTable, mappedNumTable);
			putInTable(seq.length(), Sam.getSoftclippedBasesLen(tokens[Sam.CIGAR]), softclippedLenTable, skippedNumTable);
			try {
				putInTable(seq.length(), Sam.getMismatchedBasesLen(Sam.getMDTAG(tokens)), mismatchLenTable, mismatchNumTable);
			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.println(line);
				throw e;
			}
			putInTable(seq.length(), Sam.getDeletedBasesLen(tokens[Sam.CIGAR]), deletionLenTable, delNumTable);
			putInTable(seq.length(), Sam.getHardclippedBasesLen(tokens[Sam.CIGAR]), hardclippedLenTable, hardclippedNumTable);
		}

		fm.writeLine("Total # of Reads on\t" + String.format("%.0f", totalReads));
		fm.writeLine("Total # of Bases on\t" + String.format("%.0f", totalBases));
		fm.writeLine("Total # of Secondary Aligned Reads\t" + String.format("%.0f", totalSecondaryAlignedReads));
		fm.writeLine("Total # of Secondary Aligned Reads\t" + String.format("%.0f", totalSecondaryAlignedBases));
		fm.writeLine("Total # of Reads not Aligned on " + refName + "\t" + String.format("%.0f", totalNonrefAlignedReads));
		fm.writeLine("Total # of Reads not Aligned on " + refName + "\t" + String.format("%.0f", totalNonrefAlignedBases));
		fm.writeLine("Total # of Aligned Reads on " + refName + "\t" + String.format("%.0f", totalAlignedReads));
		fm.writeLine("Total # of Aligned Bases on " + refName + "\t" + String.format("%.0f", totalAlignedBases));
		
		fm.writeLine("Number of reads in length");
		fm.writeLine("From\tTo\t#ofReads\t" +
				"#ofReadsW/S\t#ofReadsW/Mismatch\t#ofReadsW/D\t#ofReadsW/H\t" +
				"Mapping%(Total)\tMappedBases(%)\tSkippedBases(%)\t" +
				"MappedButMismatchedBases(%)\tDeletion(%)\tHardclipped(%)");
		for (int i = 1; i <= maxSeqLen; i+=50) {
			writeOnReport(i, i + 50, fm, totalAlignedReads);	
		}
		fm.writeLine("");
		fm.writeLine("Read Length\t#ofReads\t#ofMappedBases\t#ofSkippedBases\t#ofMappedButMismatchedBases\t#ofDeletedBases\t#ofHardclippedBases");
		for (int i = 1; i <= max; i++) {
			if (seqNumTable.containsKey(i)) {
				
				fm.write(i + "\t" + seqNumTable.get(i) + "\t"
						+ String.format("%,.2f", (mappedLenTable.get(i)/mappedNumTable.get(i))) + "\t");
				if (skippedNumTable.containsKey(i)) {
					fm.write(String.format("%,.2f", (softclippedLenTable.get(i)/skippedNumTable.get(i))) + "\t");
				} else {
					fm.write("0.0\t");
				}
				if (mismatchLenTable.containsKey(i)) {
					fm.write(String.format("%,.2f", (mismatchLenTable.get(i)/mismatchNumTable.get(i))) + "\t");
				} else {
					fm.write("0.0\t");
				}
				if (deletionLenTable.containsKey(i)) {
					fm.write(String.format("%,.2f", (deletionLenTable.get(i)/delNumTable.get(i))) + "\t");
				} else {
					fm.write("0.0\t");
				}
				if (hardclippedLenTable.containsKey(i)) {
					fm.writeLine(String.format("%,.2f", (hardclippedLenTable.get(i)/hardclippedNumTable.get(i))));
				} else {
					fm.writeLine("0.0");
				}
			}
		}
	}

	private void putInTable(int length, int matchedBases,
			HashMap<Integer, Float> targetTable,
			HashMap<Integer, Integer> targetNumTable) {
		if (matchedBases > 0) {
			if (targetNumTable.containsKey(length)) {
				targetTable.put(length, targetTable.get(length) + matchedBases);
				targetNumTable.put(length, targetNumTable.get(length) + 1);
			} else {
				targetTable.put(length, (float)matchedBases);
				targetNumTable.put(length, 1);
			}
		}
		
	}

	private void writeOnReport(int from, int to, FileMaker fm, float totalAlignedReads) {
		fm.write(from + "\t" + to + "\t");
		int shortReads = 0;
		int softclippedReads = 0;
		int mismatchReads = 0;
		int delReads = 0;
		int hardclippedReads = 0;
		float mappedBases = 0f;
		float softclippedBases = 0f;
		float mismatchedBases = 0f;
		float deletedBases = 0f;
		float hardclippedBases = 0f;
		int mappedNumReads = 0;
		int softclippedNumReads = 0;
		int mismatchedNumReads = 0;
		int deletedNumReads = 0;
		int hardclippedNumReads = 0;
		for (int i = from; i < to; i++) {
			if (seqNumTable.containsKey(i)) {
				shortReads += seqNumTable.get(i);
				if (mappedNumTable.containsKey(i)) {
					mappedNumReads++;
					mappedBases += ((mappedLenTable.get(i)*100 / mappedNumTable.get(i))/i);
				}
				if (skippedNumTable.containsKey(i)) {
					softclippedNumReads++;
					softclippedReads += skippedNumTable.get(i);
					softclippedBases += ((softclippedLenTable.get(i)*100 / skippedNumTable.get(i))/i);
				}
				if (mismatchNumTable.containsKey(i)) {
					mismatchedNumReads++;
					mismatchReads += mismatchNumTable.get(i);
					mismatchedBases += ((mismatchLenTable.get(i)*100 / mismatchNumTable.get(i))/i);
				}
				if (delNumTable.containsKey(i)) {
					deletedNumReads++;
					delReads += delNumTable.get(i);
					deletedBases += ((deletionLenTable.get(i)*100 / delNumTable.get(i))/i);
				}
				if (hardclippedNumTable.containsKey(i)) {
					hardclippedNumReads++;
					hardclippedReads += hardclippedNumTable.get(i);
					hardclippedBases += ((hardclippedLenTable.get(i)*100 / hardclippedNumTable.get(i))/i);
				}
			}
		}
		fm.writeLine(shortReads + "\t" + softclippedReads + "\t" + mismatchReads + "\t" + delReads + "\t" + hardclippedReads + "\t"
				+ (String.format("%,.2f", ((float)shortReads*100/totalAlignedReads))) + "\t"
				+ String.format("%,.2f", (mappedBases / mappedNumReads)) + "\t"
				+ String.format("%,.2f", (softclippedBases / softclippedNumReads)) + "\t"
				+ String.format("%,.2f", (mismatchedBases / mismatchedNumReads)) + "\t"
				+ String.format("%,.2f", (deletedBases / deletedNumReads)) + "\t"
				+ String.format("%,.2f", (hardclippedBases / hardclippedNumReads)) + "\t");
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samSummary.jar <in.sam> <rname>");
		System.out.println("\t<in.sam>: sam file to make the summary report");
		System.out.println("\t<rname>: reference name to count");
		System.out.println("\t<output>: output txt file containing the summary");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			refName = args[1];
			System.out.println("samSummary.jar " + args[0] + " " + args[1]);
			new SummarySam().go(args[0], args[0] + "." + args[1] + ".summary");
		} else {
			new SummarySam().printHelp();
		}
	}


}
