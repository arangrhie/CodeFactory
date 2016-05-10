package javax.arang.sam;

import java.util.ArrayList;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.genome.fasta.Seeker;

public class GetMismatches extends I2Owrapper {

	@Override
	public void hooker(FileReader frRef, FileReader frSam, FileMaker fm) {
		String line;
		String[] tokens;
		
		int pos;
		int posRef;
		int posRead;
		String readName;
		String cigar;
		String seq;
		char refBase;
		char readBase;
		ArrayList<int[]> cigarPosList;
		int[] cigarArr;
		Seeker refFaSeeker; //= new Seeker(frRef);
		int delLen;
		String bases;
		
		int matched;
		while (frSam.hasMoreLines()) {
			line = frSam.readLine();
			if (line.startsWith("@")) {
				//System.out.println("[DEBUG] :: " + line);
				continue;
			}
			frRef.reset();
			refFaSeeker = new Seeker(frRef);
			System.out.println("Start reading " + refFaSeeker.getFaName());
			//System.out.println("[DEBUG] :: " + line);
			tokens = line.split(RegExp.TAB);
			readName = tokens[Sam.QNAME];
			pos = Integer.parseInt(tokens[Sam.POS]);
			posRef = pos;
			posRead = pos;
			cigar = tokens[Sam.CIGAR];
			seq = tokens[Sam.SEQ];
			matched = SAMUtil.getMappedBases(cigar);
			if (!isRegionGiven) {
				start = pos;
				end = start + matched;
			} else if (isRegionGiven) {
				matched = (end - start);
			}
			System.out.println("Fasta template length: " + matched);
			
			cigarPosList = Sam.getAllPosition(pos, cigar);
			for (int i = 0; i < cigarPosList.size(); i++) {
				cigarArr = cigarPosList.get(i);
				if (cigarArr[Sam.CIGAR_POS_TYPE] == Sam.S) {
					continue;
				}
				if (cigarArr[Sam.CIGAR_POS_TYPE] == Sam.M) {
					posRef = cigarArr[Sam.CIGAR_POS_REF_START];
					for (posRead = cigarArr[Sam.CIGAR_POS_ALGN_RANGE_START]; posRead <= cigarArr[Sam.CIGAR_POS_ALGN_RANGE_END]; posRead++) {
						refBase = refFaSeeker.baseAt(posRef);
						readBase = seq.charAt(posRead);
						if (readBase != refBase) {
							//drawMismatch(panel, posRef, "M",  refBase, readBase);
							writeResult(fm, readName, posRef, "M", Character.toString(refBase), Character.toString(readBase));
							}
						posRef++;
					}
				}
				if (cigarArr[Sam.CIGAR_POS_TYPE] == Sam.I) {
					posRef = cigarArr[Sam.CIGAR_POS_REF_END];
					//drawIndel(panel, posRef, "I", seq.substring(cigarArr[Sam.CIGAR_POS_ALGN_RANGE_START], cigarArr[Sam.CIGAR_POS_ALGN_RANGE_END] + 1));
					bases=seq.substring(cigarArr[Sam.CIGAR_POS_ALGN_RANGE_START], cigarArr[Sam.CIGAR_POS_ALGN_RANGE_END] + 1);
					writeResult(fm, readName, posRef,  "I", "-", bases);
				}
				if (cigarArr[Sam.CIGAR_POS_TYPE] == Sam.D) {
					posRef = cigarArr[Sam.CIGAR_POS_REF_START];
					delLen = cigarArr[Sam.CIGAR_POS_REF_END] - cigarArr[Sam.CIGAR_POS_REF_START] + 1;
					//drawIndel(panel, posRef, "D", refFaSeeker.getBases(posRef, delLen));
					bases=refFaSeeker.getBases(posRef, delLen);
					writeResult(fm, readName, posRef,  "D", bases, "-");
				}
			}
		}
	}
	
	private static void writeResult(FileMaker fm, String readName, int posRef, String type, String refBase, String readBase) {
		fm.writeLine(readName + "\t" + posRef + "\t" +  type + "\t" +  Math.max(refBase.length(), readBase.length()) + "\t" + refBase + "\t" + readBase);
	}
	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samGetMismatches.jar <in.ref.fa> <in.sam> <out.mismatches> [start end]");
		System.out.println("\t<in.ref.fa>: reference fasta");
		System.out.println("\t<in.sam>: sam file");
		System.out.println("\t<out.mismatchs>: TYPE: M - mismatch | I - insertion | D - deletion");
		System.out.println("\t\tREAD_NAME\tPOS\tTYPE\tLEN\tREF_BASE\tQUERY_BASE");
		System.out.println("Arang Rhie, 2016-03-25. arrhie@gmail.com");
	}

	private static boolean isRegionGiven = false;
	private static int start = 0;
	private static int end;
	
	public static void main(String[] args) {
		if (args.length == 3) {
			new GetMismatches().go(args[0], args[1], args[2]);
		} else if (args.length == 5) {
			isRegionGiven = true;
			start = Integer.parseInt(args[3].replace(",", ""));
			end = Integer.parseInt(args[4].replace(",", ""));
			new GetMismatches().go(args[0], args[1], args[2]);
		} else {
			new GetMismatches().printHelp();
		}

	}

}
