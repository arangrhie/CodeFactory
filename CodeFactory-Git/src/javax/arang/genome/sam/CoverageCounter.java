package javax.arang.genome.sam;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class CoverageCounter extends IOwrapper {

	static String CHR = "chr20";
	static int FROM = 1;	// 31728063;
	static int TO = 100;	// 31813318;
	static int RANGE = 100; // 86256;
	static int SAM_OFFSET = 0;	// 31727563;
	
	@Override
	public void hooker(FileReader samReader, FileMaker fm) {
		String line;
		String[] tokens;
		int pos = 1;
		HashMap<Integer, Integer> seqDepth = new HashMap<Integer, Integer>();
		
		while (samReader.hasMoreLines()) {
			line = samReader.readLine();
			if (line.startsWith("@"))	continue;
			if (line.length() < 5)	continue;
			tokens = line.split("\t");
			if (!tokens[Sam.RNAME].equals(CHR))	continue;
			if (tokens[Sam.CIGAR].equals("*"))	continue;
			if (SAMUtil.isSecondaryAlignment(Integer.parseInt(tokens[Sam.FLAG])))	continue;
			pos = Integer.parseInt(tokens[Sam.POS]) + SAM_OFFSET;
			String mdtag = Sam.getMDTAG(tokens);
			if (mdtag == null) {
				System.out.println("No MD tag");
				System.out.println(line);
				System.exit(0);
			}
			if (pos + tokens[Sam.SEQ].length() < FROM)	continue;
			if (pos > TO)	break;
			mdtag = mdtag.substring(mdtag.lastIndexOf(":") + 1);
			String refSeq = Sam.getRefFromRead(tokens[Sam.SEQ], tokens[Sam.CIGAR], mdtag);
			for (int i = 0; i < refSeq.length(); i++) {
				int posKey = pos + i;
				if (seqDepth.containsKey(posKey)) {
					seqDepth.put(posKey, seqDepth.get(posKey) + 1);
				} else {
					seqDepth.put(posKey, 1);
				}
			}
		}

		int targetRange = TO - FROM + 1;
		System.out.println("Sequencing Range\t" + targetRange);

		int validRange =  0;
		for (int i = FROM; i <= TO; i++) {
			if (seqDepth.containsKey(i)) {
				fm.writeLine(CHR + "\t" + i + "\t" + seqDepth.get(i));
				validRange++;
			} else {
				fm.writeLine(CHR + "\t" + i + "\t" + 0);
			}
		}
		System.out.println("Valid Positions\t" + validRange);
		System.out.println();
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samCoverageCounter.jar <in.sam> <ref_chr> <from> <to> [sam_offset]");
		System.out.println("\t<in.sam>: input sam file");
		System.out.println("\t<in.cov>: output containing coverage information");
		System.out.println("\t\toutput format: <position>\t<seq_depth>");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 4) {
			CHR = args[1];
			FROM = Integer.parseInt(args[2]);
			TO = Integer.parseInt(args[3]);
			RANGE = FROM - TO + 1;
			SAM_OFFSET = 0;
			new CoverageCounter().go(args[0], args[0].replace(".sam", ".cov"));
		} else if (args.length == 5) {
			CHR = args[1];
			FROM = Integer.parseInt(args[2]);
			TO = Integer.parseInt(args[3]);
			RANGE = FROM - TO + 1;
			SAM_OFFSET = Integer.parseInt(args[4]);
			new CoverageCounter().go(args[0], args[0].replace(".sam", ".cov"));	
		} else {
			new CoverageCounter().printHelp();
		}
		
//		if (args.length == 1) {
//			new CoverageCounter().go(args[0], args[0].replace(".sam", ".cov"));
//		} else if (args.length == 2) {
//			RANGE = Integer.parseInt(args[1]);
//			from = 1;
//			to = from + RANGE - 1;
//			SAM_OFFSET = 0;
//			new CoverageCounter().go(args[0], args[0].replace(".sam", ".cov"));
//		} else if (args.length == 3) {
//			RANGE = Integer.parseInt(args[1]);
//			from = Integer.parseInt(args[2]);
//			to = from + RANGE - 1;
//			SAM_OFFSET = 0;
//			new CoverageCounter().go(args[0], args[0].replace(".sam", ".cov"));
//		} else if (args.length == 4){
//			RANGE = Integer.parseInt(args[1]);
//			from = Integer.parseInt(args[2]);
//			to = from + RANGE - 1;
//			SAM_OFFSET = Integer.parseInt(args[3]);
//			new CoverageCounter().go(args[0], args[0].replace(".sam", ".cov"));
//		} else {
//			new CoverageCounter().printHelp();
//		}

	}

}
