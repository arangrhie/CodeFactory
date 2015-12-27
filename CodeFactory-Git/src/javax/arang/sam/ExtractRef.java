package javax.arang.sam;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ExtractRef extends IOwrapper {

	private static String ref = "chr20";
	private static boolean isPaired = true;
	
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		int totalNumReads = 0;
		int totalAlignedReads = 0;
		
		String prevKey = "";
		boolean isPair1Aligned = false;
		String prevLine = "";
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@")) {
				if (line.contains(ref))	fm.writeLine(line);
				continue;
			}
			if (line.equals(""))	continue;
			tokens = line.split("\t");
			totalNumReads++;
			if (SAMUtil.isSecondaryAlignment(Integer.parseInt(tokens[Sam.FLAG])))	continue;
			if (tokens[Sam.CIGAR].equals("*"))	continue;
			if (tokens[Sam.RNAME].equals("*"))	continue;
			if (!tokens[Sam.RNAME].startsWith(ref))	continue;
			if (isPaired && !tokens[Sam.RNEXT].startsWith("="))	continue;
			String key = tokens[Sam.QNAME];
			if (!key.equals(prevKey)) {
				// new key
				if (!isPaired) {
					fm.writeLine(line);
					totalAlignedReads++;
				} else {
					prevKey = key;
					prevLine = line;
					isPair1Aligned = true;
				}
			} else if (isPaired && key.equals(prevKey) && isPair1Aligned) {
				totalAlignedReads++;
				fm.writeLine(prevLine + "\n" + line);
				isPair1Aligned = false;
			}
		}
		
		System.out.println("Total # of read(pair)s\t" + totalNumReads);
		System.out.println("Total # of aligned read(pair)s\t" + totalAlignedReads);
		System.out.println("Total # of not aligned read(pair)s\t" + (totalNumReads - totalAlignedReads));
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samExtractRef.jar <in.sam> <ref> [TRUE]");
		System.out.println("\t<in.sam>: input sam file");
		System.out.println("\t<ref>: reference (starts with) to pick out");
		System.out.println("\t[isPaired]: [OPTION]. set FALSE if the sample is single-end read. DEFAULT=TRUE");
		System.out.println("\t<output>: sam aligned on in_[ref].sam");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			ref = args[1];
			new ExtractRef().go(args[0], args[0].replace(".sam", "_" + args[1]) + ".sam");
		} else if (args.length == 3) {
			ref = args[1];
			if (args[2].toLowerCase().equals("false")) {
				isPaired = false; 
			}
			new ExtractRef().go(args[0], args[0].replace(".sam", "_" + args[1]) + ".sam");
		}else {
			new ExtractRef().printHelp();
		}

	}

}
