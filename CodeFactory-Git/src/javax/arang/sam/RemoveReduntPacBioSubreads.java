package javax.arang.sam;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class RemoveReduntPacBioSubreads extends IOwrapper {

	@Override
	public void hooker(FileReader frSam, FileMaker fm) {
		FileMaker fmUniqueList = new FileMaker(uniqueListPath);
		
		// part1. find unique read ids mapped
		HashMap<String, String> readIdToReadName = new HashMap<String, String>();	// key: m141229_192237_42175_c100760142550000001823166007221583_s1_p0/72652
		HashMap<String, Integer> readIdToLen = new HashMap<String, Integer>();		// key: m141229_192237_42175_c100760142550000001823166007221583_s1_p0/72652
		String line;
		String[] tokens;
		String readName;
		String readId;
		int matchedLen;
		int totalReads = 0;
		
		while (frSam.hasMoreLines()) {
			line = frSam.readLine();
			if (line.startsWith("@"))	continue;
			tokens = line.split(RegExp.TAB);
			readName = tokens[Sam.QNAME];
			readId = readName.substring(0, readName.lastIndexOf("/"));
			matchedLen = SAMUtil.getMatchedBases(tokens[Sam.CIGAR]);
			if (!readIdToReadName.containsKey(readId) || readIdToReadName.containsKey(readId) && readIdToLen.get(readId) < matchedLen) {
				readIdToReadName.put(readId, readName);
				readIdToLen.put(readId, matchedLen);
			}
			totalReads++;
		}
		
		// when reaching the end, write the unique longest reads
		ArrayList<String> uniqueReadNames = new ArrayList<String>();
		System.out.println(readIdToLen.size() + "\tUnique reads will be written to " + fmUniqueList.getFileName());
		System.out.println(totalReads + "\tOut of total reads");
		
		for (String read : readIdToLen.keySet()) {
			readName = readIdToReadName.get(read);
			matchedLen = readIdToLen.get(read);
			fmUniqueList.writeLine(readName + "\t" + matchedLen);
			uniqueReadNames.add(readName);
		}
		fmUniqueList.closeMaker();
		
		// Part2. re-write the sam file
		frSam.reset();
		while (frSam.hasMoreLines()) {
			line = frSam.readLine();
			if (line.startsWith("@")) {
				// write header
				fm.writeLine(line);
				continue;
			}
			tokens = line.split(RegExp.TAB);
			readName = tokens[Sam.QNAME];
			if (uniqueReadNames.contains(readName)) {
				fm.writeLine(line);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar -Xmx2g samRemoveRedundantPacBioSubreads.jar <in.sam> <out.unique_readid.list> <out.sam>");
		System.out.println("\tRetain only the longest matched (M + D) read id when multiple reads are presented.");
		System.out.println("\tThis process is similar to making CCS.fa + long_read.fa.");
		System.out.println("Arang Rhie, 2015-12-07. arrhie@gmail.com");
	}

	private static String uniqueListPath;
	public static void main(String[] args) {
		if (args.length == 3) {
			uniqueListPath = args[1];
			new RemoveReduntPacBioSubreads().go(args[0], args[2]);
		} else {
			new RemoveReduntPacBioSubreads().printHelp();
		}
	}

}
