package javax.arang.sam;

import java.util.ArrayList;
import java.util.Arrays;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.base.util.Base;

public class ToBeads extends Rwrapper {

	private static boolean hasBase = false;
	private static String inBase = "";
	
	public static void main(String[] args) {
		if (args.length == 1) {
			new ToBeads().go(args[0]);
		} else if (args.length == 2) {
			hasBase = true;
			inBase = args[1];
			new ToBeads().go(args[0]);
		} else {
			new ToBeads().printHelp();
		}
	}

	@Override
	public void hooker(FileReader fr) {

		ArrayList<Integer> posList = new ArrayList<Integer>();
		String line;
		String[] tokens;
		
		
		if (hasBase) {
			FileReader frBase = new FileReader(inBase);
			System.err.println("Start reading " + inBase);
			while (frBase.hasMoreLines()) {
				line = frBase.readLine();
				tokens = line.split(RegExp.TAB);
				if (tokens[Base.POS].equals("Pos"))	continue;
				posList.add(Integer.parseInt(tokens[Base.POS]));
			}
			frBase.closeReader();
		} else {
			System.err.println("Get snp positions...");
			BaseDepth baseDepth = new BaseDepth();
			BaseDepth.isSnpOnly = true;
			baseDepth.printStdout = false;
			baseDepth.hooker(fr);
			posList = baseDepth.getPosList();
			fr.reset();
		}
		System.err.println("Done. " + posList.size() + " positions collected.");
		Integer[] snpPosAll = new Integer[posList.size()];
		snpPosAll = posList.toArray(snpPosAll);
		
		String readID;
		int seqStart;
		int seqEnd;
		int matchedLen;
		StringBuffer basesInRead = new StringBuffer();
		ArrayList<Integer> posesInRead = new ArrayList<Integer>();
		String[] seqData = new String[2];
		boolean isFirstSnp = true;
		int firstSnp = 0;
		int pos;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@")){
				continue;
			}
			tokens = line.split(RegExp.TAB);
			if (tokens[Sam.SEQ].equals("*"))	continue;
			readID = tokens[Sam.QNAME];
			seqStart = Integer.parseInt(tokens[Sam.POS]);
			matchedLen = SAMUtil.getMatchedBases(tokens[Sam.CIGAR]);
			seqEnd = seqStart + matchedLen - 1;
			seqData[SAMUtil.CIGAR] = tokens[Sam.CIGAR];
			seqData[SAMUtil.SEQ] = tokens[Sam.SEQ];
			posesInRead = getPosInRead(seqStart, seqEnd, snpPosAll);
			basesInRead = new StringBuffer();
			isFirstSnp = true;
			for (int i = 0; i < posesInRead.size(); i++) {
				pos = posesInRead.get(i);
				if (isFirstSnp) {
					firstSnp = pos;
					isFirstSnp = false;
				}
				basesInRead.append(SAMUtil.getBaseAtPos(readID, pos, seqStart, seqData));
			}
			if (basesInRead.length() > 0) {
				printLine(readID, seqStart, firstSnp, basesInRead);
			}
		}
	}
	
	private void printLine(String readid, int pos, int firstSnp, StringBuffer basesInRead) {
		System.out.println(readid + "\t" + pos + "\t" + firstSnp + "\t" + basesInRead.toString());
	}
	
	private static ArrayList<Integer> getPosInRead(int seqStart, int seqEnd, Integer[] snpPosList) {
		ArrayList<Integer> snpsInRead = new ArrayList<Integer>();
		int snpStartIdx = Arrays.binarySearch(snpPosList, seqStart);
		// snpStartIdx will be the closest, min SNP equals or greater than the seqStart
		if (snpStartIdx < 0) {
			snpStartIdx += 1;	// to find 1 pos right to the seqStart 
			snpStartIdx *= -1;
		}
		// all snps are smaller than seqStart
		if (snpStartIdx == snpPosList.length) {
			return snpsInRead;
		}
		
		int snpEndIdx = Arrays.binarySearch(snpPosList, seqEnd);
		// snpEndIdx will be the closest, max SNP less than the seqEnd
		if (snpEndIdx < 0) {
			snpEndIdx += 2;	// to find 1 pos left to the seqEnd 
			snpEndIdx *= -1;
		} else if (snpEndIdx == snpPosList.length) {
			snpEndIdx = snpPosList.length - 1;
		}
		
		if (snpEndIdx < 0)	return snpsInRead;	// smallest SNP is greater than seqEnd
		
		for (int idx = snpStartIdx; idx <= snpEndIdx; idx++) {
			snpsInRead.add(snpPosList[idx]);
		}
		
		return snpsInRead;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samToBeads.jar <in.sam> [in.base]");
		System.out.println("\t<in.sam>: sam file. - for stdin (works only when in.base provided).");
		System.out.println("\t[in.base]: generated with samBaseDepth.jar <in.sam> -snp -qual 0");
		System.out.println("\t<stdout>: READ_ID\tPOS\tOBSERVED_BASES\tBEAD_POSITIONS");
		System.out.println("\t*This code assumes sam/bam is given for the same reference");
		System.out.println("Arang Rhie, 2017-09-25. arrhie@gmail.com");
	}

}
