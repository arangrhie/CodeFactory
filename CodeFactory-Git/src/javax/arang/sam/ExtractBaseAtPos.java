package javax.arang.sam;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ExtractBaseAtPos extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		
		String line;
		String[] tokens;
		String[] seqData = new String[2];
		char baseInRead;
		int seqEnd;
		int seqStart;
		int matchedLen;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@"))	continue;
			tokens = line.split(RegExp.TAB);
			seqData[0] = tokens[Sam.CIGAR];
			seqData[1] = tokens[Sam.SEQ];
			seqStart = Integer.parseInt(tokens[Sam.POS]);
			if (seqStart > pos)	break;
			matchedLen = SAMUtil.getMatchedBases(tokens[Sam.CIGAR]);
			seqEnd = seqStart + matchedLen - 1;
			if (seqEnd < pos)	continue;
			baseInRead = SAMUtil.getBaseAtPos(pos, seqStart, seqData);
			System.out.println(baseInRead + " " + pos + " " + tokens[Sam.QNAME]);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samExtractBaseAtPos.jar <in.sort.sam> <pos>");
	}

	private static int pos;
	public static void main(String[] args) {
		if (args.length == 2) {
			pos = Integer.parseInt(args[1]);
			new ExtractBaseAtPos().go(args[0]);
		} else {
			new ExtractBaseAtPos().printHelp();
		}
	}

}
