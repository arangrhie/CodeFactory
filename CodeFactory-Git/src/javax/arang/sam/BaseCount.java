package javax.arang.sam;

import java.util.ArrayList;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

public class BaseCount extends Rwrapper {

	static String ref = "";
	static int position = 0;

	int countA = 0;
	int countC = 0;
	int countG = 0;
	int countT = 0;
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		String prevId = "";
		String id;
		boolean isMatch = false;
		
		char refBase = '0';
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.length() < 5)	continue;
			if (line.startsWith("@")) continue;
			tokens = line.split("\t");
			id = tokens[Sam.QNAME];
			if (id.equals(prevId) && isMatch)	{
				//writeLine(line, fm, tokens[Sam.POS]);
			} else {
				// new read id
				isMatch = false;
				prevId = id;
			}
			if (!tokens[Sam.RNAME].equals(ref))	continue;
			int pos = Integer.parseInt(tokens[Sam.POS]);
			if (pos + tokens[Sam.SEQ].length() < position)	continue;
			if (pos > position)	continue;
			isMatch = true;
			int matchedLength = Sam.getSoftclippedBasesLen(tokens[Sam.CIGAR])
					+ Sam.getMatchedBasesLen(tokens[Sam.CIGAR])
					+ Sam.getInsertedBasesLen(tokens[Sam.CIGAR]);
			if (isInRange(pos, matchedLength)) {
				String cigar = tokens[Sam.CIGAR];
				String mdTag =  Sam.getMDTAG(tokens);
				String read = tokens[Sam.SEQ];
				String reference = Sam.makeRefSequence(read, cigar, mdTag);
				ArrayList<int[]> matchedPos = Sam.getPosition(position, cigar);
				MATCH_LOOP : for (int[] posArr : matchedPos) {
					if (position < posArr[Sam.CIGAR_POS_REF_START]) 	continue;
					if (position > posArr[Sam.CIGAR_POS_REF_END])	break;
					// ref_start_pos < position < ref_end_pos
					int posI = pos;
					for (int i = posArr[Sam.CIGAR_POS_ALGN_RANGE_START]; i <= posArr[Sam.CIGAR_POS_ALGN_RANGE_END]; i++) {
//						System.out.println("AlignRange: " + i);
						if (posI == position) {
							if (tokens[Sam.QUAL].charAt(i - 1) < 50) {
								break MATCH_LOOP;
							}
							refBase = reference.charAt(i - 1);
							char allele = read.charAt(i - 1);
//							System.out.println(read);
//							System.out.println(reference);
//							System.out.println(pos + "\t" + cigar + "\t" + mdTag);
//							System.out.println(i + "\t" + posI + "\t" + refBase + "\t" + allele);
							switch (allele) {
							case 'A':	countA++;	break MATCH_LOOP;
							case 'T':	countT++;	break MATCH_LOOP;
							case 'G':	countG++;	break MATCH_LOOP;
							case 'C':	countC++;	break MATCH_LOOP;
							}
						}
						posI++;
					}
					break;
				}
				isMatch = true;
			}
		}
		
		char obs = getMaxNcountBase(refBase, countA, countT, countG, countC);
		int snpCount = getSNPcount(obs);
		int totalCount = countA +  countC + countG + countT;
		System.out.println(ref + "\t" + position + "\t" + refBase + "\t" + obs + "\t"
				+ countA + "\t" + countC + "\t" + countG + "\t" + countT + "\t"
				+ snpCount + "\t" + totalCount + "\t"
				+ String.format("%,.2f", ((float) snpCount * 100) / totalCount));

	}
	
	static final char A = 'A';
	static final char C = 'C';
	static final char G = 'G';
	static final char T = 'T';
	
	public static char getMaxNcountBase(char refAllele, int aCount, int tCount, int gCount, int cCount) {
		switch(refAllele) {
		case A:	// refAllele = A
			if (tCount > gCount) {
				if (tCount > cCount) return T;
				else return C;
			} else {
				if (gCount > cCount) return G;
				else return C;
			}
		case T:	// refAllele = T
			if (aCount > gCount) {
				if (aCount > cCount) return A;
				else return C;
			} else {
				if (gCount > cCount) return G;
				else return C;
			}
		case G:	// refAllele = G
			if (tCount > aCount) {
				if (tCount > cCount) return T;
				else return C;
			} else {
				if (aCount > cCount) return A;
				else return C;
			}
		case C: // refAllele = C
			if (aCount > tCount) {
				if (aCount > gCount) return A;
				else return G;
			} else {
				if (tCount > gCount) return T;
				else return G;
			}
		}
		return 'N';
	}
	
	
	private int getSNPcount(char base) {
		switch(base) {
		case 'A':
			return countA;
		case 'T':
			return countT;
		case 'G':
			return countG;
		case 'C':
			return countC;
		}
		System.out.println("DEBUG :: wrong snp count!!");
		return -1;
	}
	
	private boolean isInRange(int pos, int length) {
		if (pos < position && position < (pos + length)) {
			return true;
		}
		return false;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samBaseCount.jar <in.sam> <ref> [position_to_count]");
		System.out.println("\t<output>: <pos> <ref_base> <obs_A> <obs_C> <obs_G> <obs_T> <allele_count> <other_count> <AF%>");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3) {
			ref = args[1];
			position = Integer.parseInt(args[2]);
			new BaseCount().go(args[0]);
		} else {
			new BaseCount().printHelp();
		}
	}

}
