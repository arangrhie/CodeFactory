package javax.arang.chain;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToBed extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String tName = null;
		int tStart = 0;
		int tEnd = 0;
		char tStrand = '0';
		
		String qName = null;
		int qStart = 0;
		int qEnd = 0;
		char qStrand = '0';
		String qSize = "";
		
		int blockSize;
		int dt;
		int dq;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine().trim();
			tokens = line.split(RegExp.WHITESPACE);
			if (line.equals("")) continue;
			if (tokens[Chain.CHAIN].startsWith("chain")) {
				tName = tokens[Chain.T_NAME];
				tStart = Integer.parseInt(tokens[Chain.T_START]);
				tStrand = tokens[Chain.T_STRAND].charAt(0);
				qName = tokens[Chain.Q_NAME];
				qStart = Integer.parseInt(tokens[Chain.Q_START]);
				qStrand = tokens[Chain.Q_STRAND].charAt(0);
				qSize = tokens[Chain.Q_SIZE];
			} else {
				blockSize = Integer.parseInt(tokens[Chain.BLOCK_SIZE]);
				tEnd = tStart + blockSize;
				qEnd = qStart + blockSize;
				fm.writeLine(tName + "\t" + tStart + "\t" + tEnd + "\t" + tStrand + "\t" +
							 qName + "\t" + qStart + "\t" + qEnd + "\t" + qStrand + "\t" + qSize + "\t" + blockSize);
				if (tokens.length > 2) {
					dt = Integer.parseInt(tokens[Chain.BLOCK_DT]);
					dq = Integer.parseInt(tokens[Chain.BLOCK_DQ]);
					tStart = tEnd + dt;
					qStart = qEnd + dq;
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar chainToBed.jar <in.chain> <out.bed>");
		System.out.println("\tConvert a chain file to a bed file, containing all alignment blocks precisely.");
		System.out.println("\t<in.chain>: chain or overchain");
		System.out.println("\t<out.bed>: converted bed containing alignment blocks");
		System.out.println("\t\t<out.bed> format: tName\ttStart\ttEnd\ttStrand\tqName\tqStart\tqEnd\tqStrand\tqSize\tblockSize");
		System.out.println("Arang Rhie, 2015-07-05. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ToBed().go(args[0], args[1]);
		} else {
			new ToBed().printHelp();
		}
	}

}
