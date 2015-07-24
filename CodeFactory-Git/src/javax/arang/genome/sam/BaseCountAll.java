package javax.arang.genome.sam;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.basic.FileMaker;
import javax.arang.genome.base.Base;

/***
 * Write base coverage on each bases
 * 
 * @author Arang Rhie
 *
 */
public class BaseCountAll extends SortSamReader {

	HashMap<Integer, Base> posMap = new HashMap<Integer, Base>();
	
	static String fixedRef = "";
	static boolean isRefFixed = false;
	
	private String getBaseCov(Base base) {
		String line = base.refChr + "\t" 
						+ base.pos + "\t"
						+ base.ref + "\t"
						+ base.Acount + "\t"
						+ base.Ccount + "\t"
						+ base.Gcount + "\t"
						+ base.Tcount + "\t"
						+ String.format("%,.2f", base.getQualAvg()) + "\t"
						+ base.getTotalBaseCount();
		return line;
	}
	
	protected void removePosQ(FileMaker fm, int prevPos, int pos) {
		if (posMap.size() == 0)	return;
		for (int i = prevPos; i < pos; i++) {
			if (posMap.containsKey(i)) {
				fm.writeLine(getBaseCov(posMap.get(i)));
				posMap.remove(i);
			}
		}
	}
	
	protected void putPosMap(String[] tokens, int pos) {
		String cigar = tokens[Sam.CIGAR];
		String mdTag =  Sam.getMDTAG(tokens);
		String read = tokens[Sam.SEQ];
		String reference = Sam.makeRefSequence(read, cigar, mdTag);
		ArrayList<int[]> matchedPos = Sam.getPosition(pos, cigar);
		for (int[] posArr : matchedPos) {
			int refPos = posArr[Sam.REF_START_POS];
			for (int i = posArr[Sam.ALGN_RANGE_START]; i <= posArr[Sam.ALGN_RANGE_END]; i++) {
				if (reference.charAt(i - 1) == 'N') {
					refPos++;
					continue;
				}
				Base base;
				if (posMap.containsKey(refPos)) {
					base = posMap.get(refPos);
				} else {
					base = new Base();
					base.refChr = tokens[Sam.RNAME];
					base.pos = refPos;
					base.ref = reference.charAt(i - 1);
				}
				base.addQual(tokens[Sam.QUAL].charAt(i - 1) - QUAL_THRESHOLD);
				char allele = read.charAt(i - 1);
				BASE_LOOP : switch (allele) {
					case 'A':	base.Acount++;	break BASE_LOOP;
					case 'T':	base.Tcount++;	break BASE_LOOP;
					case 'G':	base.Gcount++;	break BASE_LOOP;
					case 'C':	base.Ccount++;	break BASE_LOOP;
				}
				posMap.put(refPos, base);
				refPos++;
			}
		}
		
	}
	

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samBaseCountAll.jar <in_sorted.sam> <out_path> [QUAL_THRESHOLD=33]");
		System.out.println("\t[QUAL_TRHESHOLD] default = 33");
		System.out.println("\t<out> in_sorted.bas.chr[_QUAL_THRESHOLD]: Base count of all positions");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			outPath = args[1];
			new BaseCountAll().go(args[0]);
		} else if (args.length == 3) {
			outPath = args[1];
			QUAL_THRESHOLD = Integer.parseInt(args[2]);
			new BaseCountAll().go(args[0]);
		} else {
			new BaseCountAll().printHelp();
		}

	}

	@Override
	protected String getFileName(String ref) {
		// TODO Auto-generated method stub
		return frName + ".bas." + ref;
	}
	

}
