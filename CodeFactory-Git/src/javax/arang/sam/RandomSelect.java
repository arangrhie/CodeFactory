package javax.arang.sam;

import java.util.HashSet;
import java.util.Random;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class RandomSelect extends IOwrapper {

	static long TOTAL_NUM_READS = 0;
	static long RANDOM_READS = 0;
	static String refName = "";
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		HashSet<Long> keySet = new HashSet<Long>();
		
		Random rand = new Random(System.currentTimeMillis());
		long loop = RANDOM_READS;
		for (long i = 0; i < loop; i++) {
			long randKey = Math.abs(rand.nextLong() % TOTAL_NUM_READS);
			if (!keySet.contains(randKey)) {
				keySet.add(randKey);
			} else {
				loop++;
			}
		}

		String line;
		String[] tokens;
		String prevKey = "";
		boolean isRandId = false;
		long writtenReads = 0;
		Long lineNum = 0l;
		while (fr.hasMoreLines()) {	// READ_LOOP
			line = fr.readLine();
			if (line.startsWith("@"))	continue;
			if (line.equals(""))	continue;
			tokens = line.split("\t");
			if (SAMUtil.isSecondaryAlignment(Integer.parseInt(tokens[Sam.FLAG])))	continue;
			if (tokens[Sam.CIGAR].equals("*"))	continue;
			if (tokens[Sam.RNAME].equals("*"))	continue;
			if (!tokens[Sam.RNAME].startsWith(refName))	continue;
			String key = tokens[Sam.QNAME];
			if (prevKey.equals(key)) {
				if (isRandId) {
					fm.writeLine(line);
					isRandId = false;
				}
			} else {
//				if (lineNum > TOTAL_NUM_READS)	break READ_LOOP;
				if (keySet.contains(lineNum)) {
					fm.writeLine(line);
					isRandId = true;
					writtenReads++;
					printProgress(writtenReads);
				}
				lineNum++;
			}
			prevKey = key;
		} // READ_LOOP
		System.out.println("Finished");
		System.out.println("Total Reads Passed: " + lineNum);
		System.out.println("Total Reads: " + writtenReads);
	}

	static int progress = 0;
	private void printProgress(long written) {
		int prog = (int) (written * 100 / RANDOM_READS);
		if (progress == prog)	return;
		System.out.println(prog + "% complete");
		progress = prog;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samRandomSelect.jar <in.sam> <total_reads> <random_select> <refName>");
		System.out.println("\tOutput: <in.random.<random_select>.sam");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 4) {
			TOTAL_NUM_READS = Long.parseLong(args[1]);
			RANDOM_READS = Long.parseLong(args[2]);
			refName = args[3];
			System.out.println("java - jar samRandomSelect.jar " + args[0] + " " + args[1] + " " + args[2]);
			System.out.println("\tRandomly select " + args[2] + " (paired) reads out of " + args[1]);
			new RandomSelect().go(args[0], args[0].replace(".sam", ".random." + args[2] + ".sam"));
		} else {
			new RandomSelect().printHelp();
		}
	}

}
