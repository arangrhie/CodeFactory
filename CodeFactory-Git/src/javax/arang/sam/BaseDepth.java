package javax.arang.sam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.base.util.Base;

public class BaseDepth extends Rwrapper {

	String chr;
	PriorityQueue<Integer> posQ = new PriorityQueue<Integer>();
	// pos	All (A C G T D)
	HashMap<Integer, Integer[]> depthMap = new HashMap<Integer, Integer[]>();
	ArrayList<Integer> posList = new ArrayList<Integer>();
	
	private static int qualFilter = 0;
//	private static int numThreads = 0;
	
	static boolean hasNoQual = false;
	String prevChr = "";
	int prevPos = -1;
	
	public static boolean isSnpOnly = false;
	public boolean printStdout = true;
	public static final short OFFSET_A = 0;
	public static final short OFFSET_C = 1;
	public static final short OFFSET_G = 2;
	public static final short OFFSET_T = 3;
	public static final short OFFSET_D = 4;
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] record;
		if (printStdout) {
			System.out.println("#Chromosome\tPos\tA\tC\tG\tT\tD");
		}
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@"))	continue;
			record = line.split(RegExp.TAB);
			this.chr = record[Sam.RNAME];
			if (chr.equals("*"))	break;
			if (record[Sam.CIGAR].equals("*") || record[Sam.SEQ].equals("*"))	continue;
			if (prevChr.equals("")) {
				prevChr = chr;
			} else if (!prevChr.equals(chr)) {
				System.err.println(".. Reading " + chr);
				writeBaseCoverage(-1);
				prevChr = chr;
			}
			//System.err.println(line);
			getBaseCoverage(record);
		}
		writeBaseCoverage(-1);
	}
	
	/***
	 * Write out depthQ from prevPos to pos-1. Empty the depthQ till pos-1.
	 * @param pos	Position where the depth will be taken from next alignment.
	 * If pos is -1, all the depthQ will be written and cleared.
	 */
	private void writeBaseCoverage(int pos) {
		// Write all the depthQ and clear it.
		if (pos == -1) {
			while(!posQ.isEmpty()) {
				//System.out.print(prevChr + "\t" + position);
				int position = posQ.remove();
				Integer[] depths = depthMap.get(position);
				if (!isSnpOnly || (isSnpOnly && Base.isSnp(depths))) {
					print(position, depths);
				}
			}
			depthMap.clear();
		} else {
			// Write depths until pos-1.
			if (posQ.peek() == null)	return;
			int position = posQ.peek();
			Integer[] depths = depthMap.get(position);
			while (position < pos) {
				if (!isSnpOnly || (isSnpOnly && Base.isSnp(depths))) {
					print(position, depths);
				}
				posQ.remove();
				depthMap.remove(position);
				if (posQ.peek() == null)	return;
				position = posQ.peek();
				depths = depthMap.get(position);
			}
		}
	}
	
	private void print(int position, Integer[] depths) {
		if (printStdout) {
			System.out.print(prevChr + "\t" + position);
			for (int i = 0; i < depths.length; i++) {
				System.out.print("\t" + depths[i]);
			}
			System.out.println();
		} else {
			posList.add(position);
		}
	}
	
	public ArrayList<Integer> getPosList() {
		return posList;
	}
	
	public void getBaseCoverage(String[] record) {
		int pos = Integer.parseInt(record[Sam.POS]);
		
		// write new depth when pos from record exceeds previous base.
		if (pos != prevPos) {
			//System.out.println(prevPos + " -> " + pos);
			writeBaseCoverage(pos);
			// init vars
			prevPos = pos;
		}
		
		// add to depthQ
		ArrayList<String[]> cigarArr = Sam.parseArr(record[Sam.CIGAR]);
		int seqIdx = 0;
		int currPos = pos + seqIdx;
		for (String[] cigar : cigarArr) {
			//System.out.println("cigar: " + cigar[Cigar.COUNT] + " " + cigar[Cigar.OP]);
			// S: skip COUNT bases
			if (cigar[Sam.OP].equals("S") || cigar[Sam.OP].equals("I")) {
				seqIdx += Integer.parseInt(cigar[Sam.COUNT]);
			}
			// D: add 'D'
			else if (cigar[Sam.OP].equals("D")) {
				int cnt = Integer.parseInt(cigar[Sam.COUNT]);
				for (int i = 0; i < cnt; i++) {
					// add D
					if (depthMap.containsKey(currPos)) {
						if (hasNoQual) {
						depthMap.put(currPos, 
								addDepth(depthMap.get(currPos), currPos, 'D'));
						} else {
							depthMap.put(currPos,
								addDepth(depthMap.get(currPos), currPos,
										'D',
										// Deletion: no qual available - average of flanked base quality
										(char) ((record[Sam.QUAL].charAt(seqIdx - 1) + record[Sam.QUAL].charAt(seqIdx)) / 2)));
							
						}
					} else {
						posQ.add(currPos);
						Integer[] newDepth = new Integer[5];
						for (int j = 0; j < newDepth.length; j++) {
							newDepth[j] = 0;
						}
						if (hasNoQual) {
						depthMap.put(currPos, 
								addDepth(newDepth, currPos, 'D'));
						} else {
							depthMap.put(currPos,
								addDepth(newDepth, currPos,
										'D',
										// Deletion: no qual available - average of flanked base quality
										(char) ((record[Sam.QUAL].charAt(seqIdx - 1) + record[Sam.QUAL].charAt(seqIdx)) / 2)));
						}
					}
					currPos++;
				}
			}
			// M, = or X: add COUNT bases
			else if (cigar[Sam.OP].equals("M") || cigar[Sam.OP].equals("=") || cigar[Sam.OP].equals("X") ) {
				int cnt = Integer.parseInt(cigar[Sam.COUNT]);
				for (int i = 0; i < cnt; i++) {
					if (depthMap.containsKey(currPos)) {
						
						if (hasNoQual) {
							depthMap.put(currPos, 
									addDepth(depthMap.get(currPos), currPos,
											record[Sam.SEQ].charAt(seqIdx)));
						} else {
							depthMap.put(currPos, 
									addDepth(depthMap.get(currPos), currPos,
											record[Sam.SEQ].charAt(seqIdx),
											record[Sam.QUAL].charAt(seqIdx)));
						}
					} else {
						posQ.add(currPos);
						Integer[] newDepth = new Integer[5];
						for (int j = 0; j < newDepth.length; j++) {
							newDepth[j] = 0;
						}
						if (hasNoQual) {
							depthMap.put(currPos, 
									addDepth(newDepth, currPos,
											record[Sam.SEQ].charAt(seqIdx)));
							
						} else {
							depthMap.put(currPos, 
									addDepth(newDepth, currPos,
											record[Sam.SEQ].charAt(seqIdx),
											record[Sam.QUAL].charAt(seqIdx)));
						}
					}
					seqIdx++;
					currPos++;
				}
			}
			// N: jump the currPos.
			else if (cigar[Sam.OP].equals("N")) {
				currPos += Integer.parseInt(cigar[Sam.COUNT]);
			}
			// H: do nothing. seq is already clipped.
		}
	}
	
	private Integer[] addDepth(Integer[] depths, int pos, char base, char qual) {
		qual -= 33;
		short offset = OFFSET_A;
		switch(base) {
		case 'A': {
			offset = OFFSET_A;
			break;
		}
		case 'C': {
			offset = OFFSET_C;
			break;
		}
		case 'G': {
			offset = OFFSET_G;
			break;
		}
		case 'T': {
			offset = OFFSET_T;
			break;
		}
		case 'D': {
			offset = OFFSET_D;
			break;
		}
		}
		if (qual > qualFilter) {
			depths[offset]++;
		}
		return depths;
	}
	
	private Integer[] addDepth(Integer[] depths, int pos, char base) {

		short offset = OFFSET_A;
		switch(base) {
		case 'A': {
			offset = OFFSET_A;
			break;
		}
		case 'C': {
			offset = OFFSET_C;
			break;
		}
		case 'G': {
			offset = OFFSET_G;
			break;
		}
		case 'T': {
			offset = OFFSET_T;
			break;
		}
		case 'D': {
			offset = OFFSET_D;
			break;
		}
		}
		depths[offset]++;
		return depths;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bamBaseDepth.jar <in.sam> [-snp] [-qual Q-filter]");
		System.out.println("\t<in.sam>: filtered, comprehensive sam");
		System.out.println("\t<stdout>: chr\tpos(int)\tDepth of > Q-filter (A C G T D) (int)\n" +
				"\t\tQ is phred-scaled base quality (assumes bam quality string is Phred+33).");
		System.out.println("\t[-qual Q-filter]: [DEFAULT=0 (no filter applied)] Phred scaled quality score. samBaseDepth will report only the depth over Q-filter.");
		System.out.println("\t\t*Set to -1 if qual field does not exist. (Ex. PacBio Subreads)");
		System.out.println("\t[-snp]: Turn on if you want snp positions only. SNPs with at least 2 different bases found at the same position are reported.");
		System.out.println("\t*Run baseCoverage.jar after this process is finished. <= Not any more after 2015-04-02.");
		System.out.println("Arang Rhie, 2015-11-20. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			new BaseDepth().printHelp();
		} else {
			if (args.length > 1) {
				for (int i = 1; i< args.length; i++) {
					if (args[i].equals("-snp")) {
						isSnpOnly = true;
						System.err.println("Get snps only (>=1 alt)");
					} else if (args[i].startsWith("-q")) {
						qualFilter = Integer.parseInt(args[i+1]);
						if (qualFilter <= 0) {
							hasNoQual = true;
							System.err.println("Assumes no Quality Filter to be expected.");
						} else {
							System.err.println("Q-filter: " + qualFilter + " (" + (qualFilter + 33) +"=" + (char) (qualFilter + 33) + ") in ASCII code");
						}
					}
				}
			}
			new BaseDepth().go(args[0]);
		}
	}

}
