package javax.arang.chain;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToGapBed extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		
		String line;
		String[] tokens;
		
		String tName = null;
		int tStart = 0;
		char tStrand = '0';
		
		String qName = null;
		int qStart = 0;
		char qStrand = '0';
		
		int blockSize;
		int dt;
		int dq;
		
		String prevTName = "";
		String prevQName = "";
		int prevTStart = 0;
		
		// Target
		FileMaker fmRef = new FileMaker(prefix + ".ref");
		
		// Qry
		FileMaker fmQry = new FileMaker(prefix + ".qry");
		
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			
			if (line.startsWith("#"))	continue;
			if (line.equals("")) continue;
			if (tokens[Chain.CHAIN].startsWith("chain")) {
				tName = tokens[Chain.T_NAME];
				tStrand = tokens[Chain.T_STRAND].charAt(0);
				tStart = Integer.parseInt(tokens[Chain.T_START]);
				if (tStrand != '+') {
					System.err.println("Not supported for non + alignments yet! Found target " + tStrand);
					System.exit(-1);
				}
				
				qName = tokens[Chain.Q_NAME];
				qStrand = tokens[Chain.Q_STRAND].charAt(0);
				if (qStrand != '+') {
					System.err.println("Not supported for non + alignments yet! Found qerey " + qStrand);
					System.exit(-1);
				}
				qStart = Integer.parseInt(tokens[Chain.Q_START]);
				

				// Gap at the beginning
				if (tName.equals(prevTName) && tStart > prevTStart ||
					!tName.equals(prevTName) && tStart > 0) {
					printFormat(fmRef, tName, 0, tStart);
				}
				if (!qName.equals(prevQName) && qStart > 0) {
					printFormat(fmQry, qName, 0, tStart);
				}
				
			} else if (tokens.length == 3){
				blockSize = Integer.parseInt(tokens[Chain.BLOCK_SIZE]);
				dt = Integer.parseInt(tokens[Chain.BLOCK_DT]);
				dq = Integer.parseInt(tokens[Chain.BLOCK_DQ]);
				
				tStart += blockSize;
				qStart += blockSize;
				
				if (dt > 0) {
					printFormat(fmRef, tName, tStart, tStart + dt);
				}
				tStart += dt;
				
				if (dq > 0) {
					printFormat(fmQry, qName, qStart, qStart + dq);
				}
				qStart += dq;
			} else if (tokens.length == 1) {
				// last block
				blockSize = Integer.parseInt(tokens[Chain.BLOCK_SIZE]);
				tStart += blockSize;
				qStart += blockSize;
				
				prevTName = tName;
				prevQName = qName;
				prevTStart = tStart;
			}
		}
	}

	private void printFormat(FileMaker fm, String name, int start, int end) {
		fm.writeLine(name + "\t" + start + "\t" + end);
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar chainToGapBed.jar <in.chain> <out-prefix>");
		System.err.println();
		System.err.println("Collect gapped coordinates in reference and query sequences in bed format.");
		System.err.println("  in.chain  input chain file");
		System.err.println("  out-prefix.ref.bed    gaps in ref");
		System.err.println("  out-prefix.qry.bed    gaps in qry");
		System.err.println("  * not tested for - aligned blocks. Not outputting gaps for distinct qury chains. *");
		System.err.println();
		System.err.println("2022-11-03, Arang Rhie. arrhie@gmail.com");

	}

	private static String prefix = "out";
	public static void main(String[] args) {
		if (args.length == 2) {
			prefix = args[1];
			new ToGapBed().go(args[0]);
		}
		else {
			new ToGapBed().printHelp();
		}
	}

}
