package javax.arang.genome.sam;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class Sort extends IOwrapper {

	final static int OFFSET = 10000;
	static int FROM = 0 * OFFSET;
	static int TO = FROM + OFFSET;
	static int MAX = 0;
	static boolean isFirstTime = true;
	static int totalReadPairs = 0;
	static int totalUniquePairs = 0;
	static boolean isPaired = true;
	static String refName = "";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3 && args[2].toLowerCase().startsWith("f")) {
			isPaired = false;
		}
		if (args.length >= 1) {
			System.out.println("From~To\t# of read pairs\t# of read pairs with unique starts");
			refName = args[1];
			new Sort().go(args[0], args[0].replace(".sam", ".sort.01.sam"));
			isFirstTime = false;
			int fileNum = 2;
			String fileNumStr = "02";
			for (int i = TO; i < MAX; i+= OFFSET) {
				FROM = i;
				TO = i + OFFSET;
				new Sort().go(args[0], args[0].replace(".sam", ".sort." + fileNumStr + ".sam"));
				fileNum++;
				if (fileNum < 10) {
					fileNumStr = "0";
				} else {
					fileNumStr = "";
				}
				fileNumStr = fileNumStr + fileNum;
			}
			System.out.println("Total Sum\t" + totalReadPairs + "\t" + totalUniquePairs);
			System.out.println("Maximum position\t" + MAX);
			System.out.println("Finished. Type in" +
					"\tcat " + args[0].replace(".sam", ".sort.*.sam") + " > " + args[0].replace(".sam", ".sort.sam"));
		} else {
			new Sort().printHelp();
		}
	}

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String prevKey = "";
		String key = "";
		int prevPos = 0;
		boolean hasAdded = false;
		int readPairNum = 0;

		HashMap<Integer, String> alignResults = new HashMap<Integer, String>();

		String line;
		String[] tokens;
		String pair1 = "-1";
		while (fr.hasMoreLines()) {
			line = fr.readLine().toString();
			if (line.equals("")) continue;
			if (line.startsWith("@")) continue;
			tokens = line.split("\t");
			if (SAMUtil.isSecondaryAlignment(Integer.parseInt(tokens[Sam.FLAG])))	continue;
			if (tokens[Sam.CIGAR].equals("*"))	continue;
			if (tokens[Sam.RNAME].equals("*"))	continue;
			if (!tokens[Sam.RNAME].startsWith(refName))	continue;
			if (isPaired && !tokens[Sam.RNEXT].startsWith("="))	continue;
			key = tokens[Sam.QNAME];
			if (!prevKey.equals(key)) {
				int pos = Integer.parseInt(tokens[Sam.POS]) - Sam.getStartSoftclip(tokens[Sam.CIGAR]);
				if (isFirstTime && pos > MAX) {
					MAX = pos;
					hasAdded = false;
				}
				if (FROM <= pos && pos < TO) {
					prevKey = key;
					if (!isPaired) {
						if (alignResults.containsKey(pos)) {
							alignResults.put(pos, alignResults.get(pos) + "\n" + line);
						} else {
							alignResults.put(pos, line);
						}
						readPairNum++;
					} else {
						pair1 = line;
						prevPos = pos;
						hasAdded = true;
					}
				} else {
					hasAdded = false;
				}
			} else if (isPaired && prevKey.equals(key) && hasAdded) {
				if (alignResults.containsKey(prevPos)) {
					alignResults.put(prevPos, alignResults.get(prevPos) + "\n" + pair1 + "\n" + line);
				} else {
					alignResults.put(prevPos, pair1 + "\n" + line);
				}
				readPairNum++;
				hasAdded = false;
			} else if (!isPaired && isFirstTime) {
				// skip this part
				System.out.println("contains multiple alignment result: prevKey=" + prevKey + ", key=" + key);
			}
		}

		totalReadPairs += readPairNum;
		totalUniquePairs += alignResults.size();
		System.out.print(FROM + "~" + TO + "\t" + readPairNum + "\t" + alignResults.size());

		for (int i = FROM; i < TO; i++) {
			if (alignResults.containsKey(i)) {
				fm.writeLine(alignResults.get(i));
			}
		}

		System.out.println("\tWritten on " + fm.getFileName());
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samSort.jar <in.sam> <refName> [isPairedEnd=TRUE]");
		System.out.println("\t<isPairedEnd>: [OPTIONAL, DEFAULT=TRUE] type false if your sam reads are single-ended");
		System.out.println("\t<output>: sorted sam file, with the name of <in.sort.sam>");
		System.out.println("\t\tThe output is sorted according to pair1's start position.");
	}

}
