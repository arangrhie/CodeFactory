package javax.arang.genome.snp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class IonSnpMerge extends INOwrapper {

	private static int min = Integer.MAX_VALUE;
	private static int max = 0;
	private static int numSamples = 0;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length >= 2) {
			new IonSnpMerge().go(args);
		} else {
			new IonSnpMerge().printHelp();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar ionSnpMerge.jar <inFile1> ... <inFileN> <outFile.snp>");
		System.out.println("\t<inFile>: Chromosome\tPosition\tVarType\tPolidy\tRef\tVariant\tVarFreq\tP-value\tCoverage\tRefCov\tVarCov");
		System.out.println("\t<outFile.snp>: ANNOVAR input file format; Chromosome\tPosition\tPosition\t<sample1>\t<sample2>\t...\t<sampleN>");
		System.out.println("\t\t1: Het, 2: Hom");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		String header = "Chromosome\tPosition\tPosition\tRef\tVariant\t";
		HashMap<Integer, String> totalSnps = new HashMap<Integer, String>();
		for (FileReader fr : frs) {
			header = header + fr.getFileName() + "\t";
			if (totalSnps.size() == 0) {
				totalSnps = parseFirstTable(fr);
			} else {
				totalSnps = parseTable(fr, totalSnps);
			}
		}

		fm.writeLine(header.trim());
		Vector<String> refNames = new Vector<String>();
		Vector<FileMaker> refSnpFms = new Vector<FileMaker>();
		Vector<FileMaker> refIndelFms = new Vector<FileMaker>();
		for (int i = min; i <= max; i++) {
			if (totalSnps.containsKey(i)) {
				String content = totalSnps.get(i);
				String contents[] = content.split("\t");
				String line = "";
				if (contents.length < numSamples + HEAD_LEN) {
					line = totalSnps.get(i) + paddZeros(numSamples - contents.length + HEAD_LEN + 1);
				} else {
					line = totalSnps.get(i);
				}
				if (!refNames.contains(contents[CHROM])) {
					refNames.add(contents[CHROM]);
					refSnpFms.add(new FileMaker(fm.getFileName().replace(".", "_" + contents[CHROM] + ".")));
					refSnpFms.get(refNames.indexOf(contents[CHROM])).writeLine(header);
					refIndelFms.add(new FileMaker(fm.getFileName().replace(".snp", "_" + contents[CHROM] + ".indel")));
					refIndelFms.get(refNames.indexOf(contents[CHROM])).writeLine(header);
				}
				if (contents[3].length() != contents[4].length()) {
					refIndelFms.get(refNames.indexOf(contents[CHROM])).writeLine(line);
				} else {
					refSnpFms.get(refNames.indexOf(contents[CHROM])).writeLine(line);
				}
				fm.writeLine(line);
			}
		}
		System.out.println("Total # of SNVs:\t" + totalSnps.size());
	}
	
	private static final short CHROM = 0;
	private static final short POS = 1;
	private static short POLIDY = 4;
	private static short REF = 5;
	private static short VARIANT = 6;
	
	private static final short HEAD_LEN = 5;
	private static final short SNP_ALLELE = 4;
	
	/***
	 * Parse an iontorrent snp list, put them in SnpTable.
	 * @param fr
	 */
	private HashMap<Integer, String> parseTable(FileReader fr, HashMap<Integer, String> snpTable) {
		numSamples++;
		String line;
		String[] tokens;
		fr.readLine();	// 1st line
		boolean isAnnovarFormat = true;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			if (tokens.length < 3)	continue;
			int key = Integer.parseInt(tokens[POS]);
			try {
				Integer.parseInt(tokens[POS+1]);
				POLIDY = 12;
				REF = 3;
				VARIANT = 4;
				isAnnovarFormat = true;
			} catch (NumberFormatException e) {
				if (isAnnovarFormat){
					System.out.println(fr.getFileName() + "is not an ANNOVAR input format: OK");
					isAnnovarFormat = false;
					POLIDY = 3;
					REF = 4;
					VARIANT = 5;
				}
			}
			if (snpTable.containsKey(key)) {
				String content = snpTable.get(key);
				String[] contents = content.split("\t");
				String snpAllele = contents[SNP_ALLELE];
				String tab = "\t";
				if (contents.length == numSamples + HEAD_LEN) {
					tab = "/";
				}
				if (snpAllele.equals(tokens[VARIANT])) {
					snpTable.put(Integer.parseInt(tokens[POS]), content + paddZeros(numSamples - contents.length + HEAD_LEN) + tab + getIntPosidy(tokens[POLIDY]));
				} else {
					snpTable.put(Integer.parseInt(tokens[POS]), content + paddZeros(numSamples - contents.length + HEAD_LEN) + tab + getIntPosidy(tokens[POLIDY]) + ":" + tokens[VARIANT]);
				}
			} else {
				int position = Integer.parseInt(tokens[POS]);
				snpTable.put(position,
						tokens[CHROM] + "\t" + tokens[POS] + "\t" + tokens[POS] + "\t"
						+ tokens[REF] + "\t" + tokens[VARIANT]
						+ paddZeros(numSamples) + "\t"
						+ getIntPosidy(tokens[POLIDY]));
				if (position < min) {
					min = position;
				}
				if (position > max) {
					max = position;
				}
			}
		}
		return snpTable;
	}
	
	private String paddZeros(int num) {
		String zeros = "";
		for (int i = 1; i < num; i++) {
			zeros += "\t0";
		}
		return zeros;
	}
	
	private int getIntPosidy(String polidy) {
		if (polidy.equals("Het"))	return 1;
		else if (polidy.equals("Hom"))	return 2;
		return 0;
	}
	
	/***
	 * Parse an iontorrent snp list, put them in SnpTable.
	 * @param fr
	 */
	private HashMap<Integer, String> parseFirstTable(FileReader fr) {
		numSamples++;
		HashMap<Integer, String> snpTable = new HashMap<Integer, String>();
		String line;
		String[] tokens;
		fr.readLine();	// 1st line
		boolean isAnnovarFormat = true;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			if (tokens.length < 3)	continue;
			int position = Integer.parseInt(tokens[POS]);
			try {
				Integer.parseInt(tokens[POS+1]);
				POLIDY = 12;
				REF = 3;
				VARIANT = 4;
				isAnnovarFormat = true;
			} catch (NumberFormatException e) {
				if (isAnnovarFormat){
					System.out.println(fr.getFileName() + "is not an ANNOVAR input format: OK");
					isAnnovarFormat = false;
					POLIDY = 3;
					REF = 4;
					VARIANT = 5;
				}
			}
			snpTable.put(position,
					tokens[CHROM] + "\t" + tokens[POS] + "\t" + tokens[POS] + "\t" 
					+ tokens[REF] + "\t" + tokens[VARIANT] + "\t" + getIntPosidy(tokens[POLIDY]));
			if (position < min) {
				min = position;
			}
			if (position > max) {
				max = position;
			}
		}
		return snpTable;
	}

}
