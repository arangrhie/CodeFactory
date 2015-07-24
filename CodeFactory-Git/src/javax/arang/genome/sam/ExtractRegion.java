package javax.arang.genome.sam;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ExtractRegion extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		String line;
		String prevId = "";
		String id;
		boolean isMatch = false;

		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.length() < 5)	continue;
			if (line.startsWith("@")) {
				fm.writeLine(line);
			}
			String[] tokens = line.split("\t");
			id = tokens[Sam.QNAME];
			if (id.equals(prevId) && isMatch)	{
				writeLine(line, fm, tokens[Sam.POS]);
			} else {
				// new read id
				isMatch = false;
				prevId = id;
			}
			if (!tokens[Sam.RNAME].equals(ref))	continue;
			isMatch = true;
			int matchedLength = Sam.getSoftclippedBasesLen(tokens[Sam.CIGAR])
					+ Sam.getMatchedBasesLen(tokens[Sam.CIGAR])
					+ Sam.getInsertedBasesLen(tokens[Sam.CIGAR]);
			if (isInRange(Integer.parseInt(tokens[Sam.POS]), matchedLength)) {
				writeLine(line, fm, tokens[Sam.POS]);
				isMatch = true;
			}
		}
	}
	
	private void writeLine(String line, FileMaker fm, String posToken) {
		line = line.replaceFirst(posToken, String.valueOf(Integer.parseInt(posToken) + offset));
		fm.writeLine(line);
	}

	private boolean isInRange(int pos, int length) {
		pos += offset;
		int minRange = position - range;
		int maxRange = position + range;
		if (pos < maxRange && (pos + length) > minRange) {
			return true;
		}
		return false;
	}

	@Override
	public void printHelp() {
		System.out.println("java -jar extractRegion.jar <inFile.sam> <ref> <offset> <position> <range>");
		System.out.println("\t<inFile>: sam file to extract region");
		System.out.println("\t<ref>");
		System.out.println("\t<offset>");
		System.out.println("\t<position>");
		System.out.println("\t<range>");
	}
	
	private static String ref = "";
	private static int offset = 0;
	private static int position = 0;
	private static int range = 0;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 5) {
			ref = args[1];
			offset = Integer.parseInt(args[2]);
			position = Integer.parseInt(args[3]);
			range = Integer.parseInt(args[4]);
			new ExtractRegion().go(args[0], args[0].replace(".sam", "_" + ref + "_" + position + "_" + range + "bp.sam"));
		} else {
			new ExtractRegion().printHelp();
		}
	}

}
