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
		//String[] tNameTokens;
		
		String tName = null;
		int tStart = 0;
		int tEnd = 0;
		char tStrand = '0';
		String tSize = "";
		
		String qName = null;
		int qStart = 0;
		int qEnd = 0;
		char qStrand = '0';
		String qSize = "";
		
		String as = "";
		
		int blockSize;
		int dt;
		int dq;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine().trim();
			tokens = line.split(RegExp.WHITESPACE);
			if (line.startsWith("#"))	continue;
			if (line.equals("")) continue;
			if (tokens[Chain.CHAIN].startsWith("chain")) {
				as = tokens[Chain.SCORE];
				tName = tokens[Chain.T_NAME];
				tStart = 0;
				tSize = tokens[Chain.T_SIZE];
//				if (tName.contains("-")) {
//					tNameTokens = tName.split("-");
//					if (tNameTokens.length > 2) {
//						tStart = Integer.parseInt(tNameTokens[tNameTokens.length - 2]);
//						tName = tNameTokens[0];
//						for (int i = 1; i < tNameTokens.length - 2; i++) {
//							tName = tName + "-" + tNameTokens[i];
//						}
//					}
//				}
				tStart += Integer.parseInt(tokens[Chain.T_START]);
				tStrand = tokens[Chain.T_STRAND].charAt(0);
				qName = tokens[Chain.Q_NAME];
				qStart = Integer.parseInt(tokens[Chain.Q_START]);
				qStrand = tokens[Chain.Q_STRAND].charAt(0);
				qSize = tokens[Chain.Q_SIZE];
				if (!outBlock) {
					tEnd = Integer.parseInt(tokens[Chain.T_END]);
					qEnd = Integer.parseInt(tokens[Chain.Q_END]);
					blockSize = qEnd - qStart;
					fm.writeLine(tName + "\t" + tStart + "\t" + tEnd + "\t" + tStrand + "\t" + tSize + "\t" +
							 qName + "\t" + qStart + "\t" + qEnd + "\t" + qStrand + "\t" + qSize + "\t" + blockSize + "\t" + as);
				}
			} else {
				if (outBlock) {
					blockSize = Integer.parseInt(tokens[Chain.BLOCK_SIZE]);
					tEnd = tStart + blockSize;
					qEnd = qStart + blockSize;
					fm.writeLine(tName + "\t" + tStart + "\t" + tEnd + "\t" + tStrand + "\t" + tSize + "\t" +
								 qName + "\t" + qStart + "\t" + qEnd + "\t" + qStrand + "\t" + qSize + "\t" + blockSize + "\t" + as);
					if (tokens.length > 2) {
						dt = Integer.parseInt(tokens[Chain.BLOCK_DT]);
						dq = Integer.parseInt(tokens[Chain.BLOCK_DQ]);
						tStart = tEnd + dt;
						qStart = qEnd + dq;
					}
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar chainToBed.jar <in.chain> <out.bed> [-block]");
		System.out.println("\tConvert a chain file to a bed file, containing alignment block headers.");
		System.out.println("\t<in.chain>: chain or overchain. If more than 2 - are recognized, parse the start as the string before last -.");
		System.out.println("\t<out.bed>: converted bed containing alignment blocks");
		System.out.println("\t\t<out.bed> format: tName\ttStart(0-base)\ttEnd(1-base)\ttStrand\tqName\tqStart(0-base)\tqEnd(1-base)\tqStrand\tqSize\tblockSize");
		System.out.println("\t\t[-block]: If given, output bed file will be for each block");
		System.out.println("Arang Rhie, 2016-03-19. arrhie@gmail.com");
	}

	private static boolean outBlock = false;
	
	public static void main(String[] args) {
		if (args.length == 2) {
			new ToBed().go(args[0], args[1]);
		} else if (args.length == 3) {
			if (args[2].equalsIgnoreCase("-block")) {
				outBlock = true;
			}
			new ToBed().go(args[0], args[1]);
		} else {
			new ToBed().printHelp();
		}
	}

}
