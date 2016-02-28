package javax.arang.sam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class BaseDepth extends IOwrapper {

	String chr;
	PriorityQueue<Integer> posQ = new PriorityQueue<Integer>();
	// pos	All (A C G T D)
	HashMap<Integer, Integer[]> depthMap = new HashMap<Integer, Integer[]>();
		
	private static int qualFilter = 0;
//	private static int numThreads = 0;
	
	static boolean hasNoQual = false;
	String prevChr = "";
	int prevPos = -1;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] record;
		fm.writeLine("#Chromosome\tPos\tA\tC\tG\tT\tD");
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@"))	continue;
			record = line.split(RegExp.TAB);
			this.chr = record[Sam.RNAME];
			if (!prevChr.equals(chr)) {
				System.out.println("..Running " + chr);
				writeBaseCoverage(fm, -1);
				prevChr = chr;
			}
			if (prevChr.equals("")) {
				prevChr = chr;
			}
			if (chr.equals("*"))	break;
			getBaseCoverage(record, fm);
		}
		writeBaseCoverage(fm, -1);
	}
	
	/***
	 * Write out depthQ from prevPos to pos-1. Empty the depthQ till pos-1.
	 * @param fm	BinaryFileMaker to write out coverage.
	 * @param pos	Position where the depth will be taken from next alignment.
	 * If pos is -1, all the depthQ will be written and cleared.
	 */
	private void writeBaseCoverage(FileMaker fm, int pos) {
		// Write all the depthQ and clear it.
		if (pos == -1) {
			while(!posQ.isEmpty()) {
				//System.out.print(prevChr + "\t" + position);
				int position = posQ.remove();
				fm.write(prevChr + "\t" + position);
				Integer[] depths = depthMap.get(position);
				for (int i = 0; i < depths.length; i++) {
					//System.out.print("\t" + depths[i]);
					fm.write("\t" + depths[i]);
				}
				fm.writeLine();
			}
			depthMap.clear();
		} else {
			// Write depths until pos-1.
			if (posQ.peek() == null)	return;
			int position = posQ.peek();
			Integer[] depths = depthMap.get(position);
			while (position < pos) {
				//System.out.print(prevChr + "\t" + position);
				fm.write(prevChr + "\t" + position);
				for (int i = 0; i < depths.length; i++) {
					//System.out.print("\t" + depths[i]);
					fm.write("\t" + depths[i]);
				}
				fm.writeLine();
				//System.out.println();
				posQ.remove();
				depthMap.remove(position);
				if (posQ.peek() == null)	return;
				position = posQ.peek();
				depths = depthMap.get(position);
			}
		}
	}
	
	public void getBaseCoverage(String[] record, FileMaker fm) {
		int pos = Integer.parseInt(record[Sam.POS]);
		
		// write new depth when pos from record exceeds previous base.
		if (pos != prevPos) {
			//System.out.println(prevPos + " -> " + pos);
			writeBaseCoverage(fm, pos);
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
	
	private final short OFFSET_A = 0;
	private final short OFFSET_C = 1;
	private final short OFFSET_G = 2;
	private final short OFFSET_T = 3;
	private final short OFFSET_D = 4;
	
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
		System.out.println("Usage: java -jar bamBaseDepth.jar <in.sam> <out.base> [-qual Q-filter]");
		System.out.println("\t<in.sam>: filtered, comprehensive sam");
		System.out.println("\t<out.base>: chr\tpos(int)\tDepth of > Q-filter (A C G T D) (int)\n" +
				"\t\tQ is phred-scaled base quality (assumes bam quality string is Phred+33).");
		System.out.println("\t[-qual Q-filter]: [DEFAULT=0 (no filter applied)] Phred scaled quality score. samBaseDepth will report only the depth over Q-filter.");
		System.out.println("\t\t*Set to -1 if qual field does not exist. (Ex. PacBio Subreads)");
		//System.out.println("\t[-bed region.bed]: [DEFAULT=ALL] target to make <output>.");
		//TODO: Later, implement -t option for multi-threading
//		System.out.println("\t[-t numThreads]: Number of multi-threads to use. Works only when -bed is specified.");
//		System.out.println("\t\tif numThreads > 1, temporary output files will be written and merged after the process is all over.");		
		System.out.println("\t*Flag Filter: Duplicate, SecondaryAlignment, UnderQual, Unmapped.");
		System.out.println("\t*Run baseCoverage.jar after this process is finished. <= Not any more after 2015-04-02.");
		System.out.println("Arang Rhie, 2015-11-20. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new BaseDepth().go(args[0], args[1]);
		} else if (args.length > 3) {
			boolean hasBed = false;
			String targetBed = "";
			for (int i = 2; i < args.length; i+=2) {
				if (args[i].startsWith("-q")) {
					qualFilter = Integer.parseInt(args[i+1]);
					if (qualFilter == -1) {
						hasNoQual = true;
						System.out.println("Assumes no Quality Filter to be expected.");
					} else {
						System.out.println("Q-filter: " + qualFilter + " (" + (qualFilter + 33) +"=" + (char) (qualFilter + 33) + ") in ASCII code");
					}
				}
				else if (args[i].startsWith("-b")) {
					hasBed = true;
					targetBed = args[i+1];
					System.out.println("Region Bed: " + targetBed);
				}
				else {
					System.out.println("Unrecognized arg " + args[i]);
					new BaseDepth().printHelp();
					System.exit(0);
				}
			}
			if (hasBed) {
				System.out.println("*** Sorry, this version is currently not available ***");
				//new BaseDepth().go(args[0], targetBed, args[1]);
			} else {
				new BaseDepth().go(args[0], args[1]);
			}
		} else {
			new BaseDepth().printHelp();
		}

	}

}
