package javax.arang.sam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.arang.IO.basic.FileMaker;
import javax.arang.genome.indel.Indel;

public class BaseIndelCall extends SortSamReader {

	/***
	 * Integer: end position of indel var
	 * Indel: indel class containing variant properties
	 */
	HashMap<Integer, Vector<Indel>> posMap = new HashMap<Integer, Vector<Indel>>();
	
	
	@Override
	protected void removePosQ(FileMaker fm, int prevPos, int endPos) {
		if (posMap.size() == 0) return;
		for (int i = prevPos; i < endPos; i++) {
			if (posMap.containsKey(i)) {
				for (Indel indel : posMap.get(i)) {
					fm.writeLine(getIndelCov(indel));
				}
				posMap.remove(i);
			}
		}
	}

	private String getIndelCov(Indel indel) {
		String line = indel.chr + "\t" + indel.start + "\t" + indel.end + "\t"
						+ indel.ref + "\t" + indel.obs + "\t"
						+ indel.type + "\t" + indel.qa + "\t" + indel.count;
		return line;
	}

	@Override
	protected void putPosMap(String[] tokens, int pos) {
		String cigar = tokens[Sam.CIGAR];
		if (cigar.contains("I")) {
			ArrayList<int[]> insPositions = Sam.getInsPosition(pos, cigar);
			for (int[] insPos : insPositions) {
				Indel indel = new Indel();
				String ins = tokens[Sam.SEQ].substring(insPos[Sam.CIGAR_POS_ALGN_RANGE_START] - 1, insPos[Sam.CIGAR_POS_ALGN_RANGE_END]);
				indel.chr = tokens[Sam.RNAME];
				indel.ref = "-";
				indel.obs = ins;
				indel.start = insPos[Sam.CIGAR_POS_REF_START];
				indel.end = insPos[Sam.CIGAR_POS_REF_END];
				indel.count++;
				indel.type = "ins";
				String qual = tokens[Sam.QUAL].substring(insPos[Sam.CIGAR_POS_ALGN_RANGE_START] - 1, insPos[Sam.CIGAR_POS_ALGN_RANGE_END]);
				for (int i = 0; i < qual.length(); i++) {
					indel.qa += qual.charAt(i) - QUAL_THRESHOLD;
				}
				indel.qa = indel.qa/qual.length();
				if (indel.qa < 20)	continue;
				
				if (posMap.containsKey(indel.end)) {
					Vector<Indel> vecInd = posMap.get(indel.end);
					boolean hasAdded = false;
					for (Indel delList : vecInd) {
						if (delList.obs.equals(indel.obs)) {
							delList.count++;
							delList.qa += indel.qa;
							delList.qa = delList.qa/2;
							hasAdded = true;
							break;
						}
					}
					if (!hasAdded) {
						vecInd.add(indel);
					}
				} else {
					Vector<Indel> vecInd = new Vector<Indel>();
					vecInd.add(indel);
					posMap.put(indel.end, vecInd);
				}
			}
		}
		
		if (cigar.contains("D")) {
//			Sam.parseArr(Sam.getMDTAG(tokens));
			ArrayList<String> deletions = Sam.getDeletionBases(Sam.getMDTAG(tokens));
			ArrayList<int[]> delPositions = Sam.getDelPosition(pos, cigar);
			for (int i = 0; i < deletions.size(); i++) {
				Indel indel = new Indel();
				String del = deletions.get(i);
				indel.chr = tokens[Sam.RNAME];
				indel.ref = del;
				indel.obs = "-";
				indel.start = delPositions.get(i)[Sam.CIGAR_POS_REF_START];
				indel.end = delPositions.get(i)[Sam.CIGAR_POS_REF_END];
				indel.count++;
				indel.type = "del";
				indel.qa = tokens[Sam.QUAL].charAt(delPositions.get(i)[Sam.CIGAR_POS_ALGN_RANGE_START] - 2)- QUAL_THRESHOLD;
				indel.qa += tokens[Sam.QUAL].charAt(delPositions.get(i)[Sam.CIGAR_POS_ALGN_RANGE_END])- QUAL_THRESHOLD;
				indel.qa = indel.qa/2;
				if (indel.qa < 20)	continue;
				
				if (posMap.containsKey(indel.end)) {
					Vector<Indel> vecInd = posMap.get(indel.end);
					boolean hasAdded = false;
					for (Indel delList : vecInd) {
						if (delList.start == indel.start) {
							delList.count++;
							delList.qa += indel.qa;
							delList.qa = delList.qa/2;
							hasAdded = true;
							break;
						}
					}
					if (!hasAdded) {
						vecInd.add(indel);
					}
				} else {
					Vector<Indel> vecInd = new Vector<Indel>();
					vecInd.add(indel);
					posMap.put(indel.end, vecInd);
				}
			}
		}
	}

	@Override
	protected String getFileName(String ref) {
		return frName + ".indel." + ref;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samIndelCountAll.jar <in_sorted.sam> <out_path> [QUAL_THRESHOLD=33]");
		System.out.println("\t[QUAL_TRHESHOLD] default = 33");
		System.out.println("\t<out> in_sorted.indel.chr[_QUAL_THRESHOLD]: Indel count of all positions");
	}
	
	public static void main(String[] args) {
		if (args.length == 2) {
			outPath = args[1];
			new BaseIndelCall().go(args[0]);
		} else if (args.length == 3) {
			outPath = args[1];
			QUAL_THRESHOLD = Integer.parseInt(args[2]);
			new BaseIndelCall().go(args[0]);
		} else {
			new BaseIndelCall().printHelp();
		}
	}
}
