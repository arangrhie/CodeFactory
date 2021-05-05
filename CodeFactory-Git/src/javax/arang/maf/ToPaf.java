package javax.arang.maf;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToPaf extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		int lineNum = 0;
		
		String	qryName = "";
		int		qryLen = 0;
		int		qryStart = 0;	// 0-base
		int		qryEnd = 0;		// 0-base
		char	strand = '+';
		String	refName = "";
		int		refLen = 0;
		int		refStart = 0;	// 0-base
		int		refEnd = 0;		// 0-base
		int		matches = 0;
		int		blockLen = 0;
		int		mq = 60;	
		
		int		mismatches = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (lineNum % 4 == 1) {
				// Ref line
				tokens = line.split(RegExp.WHITESPACE);
				refName = tokens[MAF.SRC];
				refLen = Integer.parseInt(tokens[MAF.SRC_SIZE]);
				refStart = Integer.parseInt(tokens[MAF.START]);
				refEnd = refStart + Integer.parseInt(tokens[MAF.SIZE]);
				mismatches = getMismatches(tokens[MAF.SEQ]);
				matches = tokens[MAF.SEQ].length() - mismatches;
				
			} else if (lineNum % 4 == 2) {
				// Qry line
				tokens = line.split(RegExp.WHITESPACE);
				qryName = tokens[MAF.SRC];
				qryLen = Integer.parseInt(tokens[MAF.SRC_SIZE]);
				qryStart = Integer.parseInt(tokens[MAF.START]);
				qryEnd = qryStart + Integer.parseInt(tokens[MAF.SIZE]);
				mismatches += getMismatches(tokens[MAF.SEQ]);
				blockLen = matches + mismatches;
				strand = tokens[MAF.STRAND].toCharArray()[0];
				
				System.out.println(
						  qryName + "\t" + qryLen + "\t" + qryStart + "\t" + qryEnd + "\t" + strand + "\t"
						+ refName + "\t" + refLen + "\t" + refStart + "\t" + refEnd + "\t"
						+ matches + "\t" + blockLen + "\t" + mq);
			}
			lineNum++;
		}
		System.err.println("Finished converting " + (lineNum / 4) + " alignments.");
	}

	private int getMismatches(String seq) {
		int n = 0;
		for (char base : seq.toCharArray()) {
			if (base == '-') {
				n++;
			}
		}
		return n;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar maf2paf.jar <in.maf>");
		System.out.println("\tConvert maf to paf format.");
		System.out.println("\t<stdout> : out.paf");
		System.out.println("Arang Rhie, 2021-04-08.");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ToPaf().go(args[0]);
		} else {
			new ToPaf().printHelp();
		}
	
	}

}
