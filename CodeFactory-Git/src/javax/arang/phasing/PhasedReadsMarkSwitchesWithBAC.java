package javax.arang.phasing;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.genome.util.Util;
import javax.arang.phasing.util.PhasedRead;
import javax.arang.phasing.util.PhasedSNP;
import javax.arang.phasing.util.PhasedSNPMark;

public class PhasedReadsMarkSwitchesWithBAC extends I2Owrapper {

	@Override
	public void hooker(FileReader frRead, FileReader frSNP, FileMaker fm) {
		String line;
		String[] tokens;
		
		ArrayList<Integer> posList = new ArrayList<Integer>();
		HashMap<Integer, String> posToSNP = new HashMap<Integer, String>();
		HashMap<Integer, Integer[]> posToCount = new HashMap<Integer, Integer[]>();
		HashMap<Integer, ArrayList<String>> posToBAC = new HashMap<Integer, ArrayList<String>>();
		HashMap<Integer, String> posToNotes = new HashMap<Integer, String>();
		
		// Read SNPs
		int pos;
		while (frSNP.hasMoreLines()) {
			line = frSNP.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			
			pos = Integer.parseInt(tokens[PhasedSNP.POS]);
			posList.add(pos);
			posToSNP.put(pos, line);
			posToNotes.put(pos, "");
			posToBAC.put(pos, new ArrayList<String>());
			posToCount.put(pos, Util.initArr(PhasedSNPMark.SNP_MARK_COUNT_LEN));
		}
		
		// Process BACs
		String haplotypeList = "";
		int haplotypeIdx;
		int countBACs = 0;
		int countNoSNP = 0;
		//State: H, AorH, BorH
		int state = -1;
		int prevState = -1;
		int posIdxAllSnp;
		while (frRead.hasMoreLines()) {
			line = frRead.readLine();
			tokens = line.split(RegExp.TAB);
			
			// Read is containing NO SNP
			if (tokens.length == PhasedRead.HAPLOTYPE) {
				countNoSNP++;
				continue;
			}
			pos = Integer.parseInt(tokens[PhasedRead.SNP_POS_LIST]);
			haplotypeIdx = 0;
			haplotypeList = tokens[PhasedRead.HAPLOTYPE];
			if (haplotypeList.charAt(haplotypeIdx) == 'H') {
				state = PhasedSNPMark.STATE_H;
			} else if(haplotypeList.charAt(haplotypeIdx) == 'A') {
				state = PhasedSNPMark.STATE_A;
			} else if(haplotypeList.charAt(haplotypeIdx) == 'B') {
				state = PhasedSNPMark.STATE_B;
			} else {
				state = PhasedSNPMark.STATE_OTHER;
			}
			
			posToCount.get(pos)[state]++;
			posToCount.get(pos)[PhasedSNPMark.COV]++;
			posToBAC.get(pos).add(tokens[PhasedRead.READ_ID]);
			prevState = state;
			posIdxAllSnp = posList.indexOf(pos);
			
			for (int i = PhasedRead.SNP_POS_LIST + 1; i < tokens.length; i++) {
				posIdxAllSnp++;
				pos = Integer.parseInt(tokens[i]);
				
				// Remove uncovered SNPs within a BAC
				while (posList.get(posIdxAllSnp) < pos) {
					posToNotes.put(posList.get(posIdxAllSnp), "Ambiguous:LowCov");
					posIdxAllSnp++;
				}
				posToBAC.get(pos).add(tokens[PhasedRead.READ_ID]);
				haplotypeIdx++;
				
				if(haplotypeList.charAt(haplotypeIdx) == 'A') {
					if (prevState == PhasedSNPMark.STATE_B) {
						posToCount.get(pos)[PhasedSNPMark.SWITCH]++;
					} else if (prevState == PhasedSNPMark.STATE_A) {
						posToCount.get(pos)[PhasedSNPMark.NO_SWITCH]++;
					} else {
						// prevState == H or prevState == UNKNOWN : Do nothing
					}
					state = PhasedSNPMark.STATE_A;
				} else if(haplotypeList.charAt(haplotypeIdx) == 'B') {
					if (prevState == PhasedSNPMark.STATE_A) {
						posToCount.get(pos)[PhasedSNPMark.SWITCH]++;
					} else if (prevState == PhasedSNPMark.STATE_B) {
						posToCount.get(pos)[PhasedSNPMark.NO_SWITCH]++;
					} else {
						// prevState == H or prevState == UNKNOWN : Do nothing
					}
					state = PhasedSNPMark.STATE_B;
				} else if (haplotypeList.charAt(haplotypeIdx) == 'H') {
					if (prevState == PhasedSNPMark.STATE_H || prevState == PhasedSNPMark.STATE_OTHER) {
						state = PhasedSNPMark.STATE_H;
					} else {
						// prevState == A or prevState == B 
						state = prevState;
					}
				} else {
					// 'n' or other base
					posToCount.get(pos)[PhasedSNPMark.STATE_OTHER]++;
					state = prevState;
				}
				posToCount.get(pos)[state]++;
				posToCount.get(pos)[PhasedSNPMark.COV]++;
				prevState = state;
			}
			countBACs++;
		}
		
		int countSwitched = 0;
		int countAmbiguous = 0;
		int upPos;
		
		for (int i = 1; i < posList.size(); i++) {
			pos = posList.get(i);
			if (posToNotes.get(pos).startsWith("Ambiguous")) {
				countAmbiguous++;
				continue;
			}
			if (posToCount.get(pos)[PhasedSNPMark.SWITCH] > 0) {
				if (posToCount.get(pos)[PhasedSNPMark.NO_SWITCH] == 0) {
					countSwitched++;
					posToNotes.put(pos, "SwiFromHere");
				} else if (posToCount.get(pos)[PhasedSNPMark.NO_SWITCH] > 0) {
					if (posToCount.get(pos)[PhasedSNPMark.STATE_A] == 0 || posToCount.get(pos)[PhasedSNPMark.STATE_B] == 0) {
						countAmbiguous++;
						posToNotes.put(pos, "Ambiguous");
					} else {
						UP_LOOP : for (int j = i-1; j >= 0; j--) {
							upPos = posList.get(j);
							tokens = posToSNP.get(upPos).split(RegExp.TAB);
							if (!tokens[PhasedSNP.HAPLOTYPE_A].equals(tokens[PhasedSNP.HAPLOTYPE_B])) {
								if (posToCount.get(upPos)[PhasedSNPMark.STATE_A] == 0 || posToCount.get(upPos)[PhasedSNPMark.STATE_B] == 0) {
									countAmbiguous++;
									posToNotes.put(upPos, "Ambiguous");
								}
								break UP_LOOP;
							}
						}
					}
				}
			}
		}
		
		for (int i = 0; i < posList.size(); i++) {
			pos = posList.get(i);

			fm.writeLine(posToSNP.get(pos)
					+ "\t" + posToCount.get(pos)[PhasedSNPMark.COV]
					+ "\t" + posToCount.get(pos)[PhasedSNPMark.STATE_H]
					+ "\t" + posToCount.get(pos)[PhasedSNPMark.STATE_A]
					+ "\t" + posToCount.get(pos)[PhasedSNPMark.STATE_B]
					+ "\t" + posToCount.get(pos)[PhasedSNPMark.STATE_OTHER]
					+ "\t" + posToCount.get(pos)[PhasedSNPMark.SWITCH]
					+ "\t" + posToCount.get(pos)[PhasedSNPMark.NO_SWITCH]
					+ "\t" + listToString(posToBAC.get(pos))
					+ "\t" + posToNotes.get(pos));
		}
		System.out.println();
		System.out.println(countSwitched + "\tMarked to Switch");
		System.out.println(countAmbiguous + "\tMarked as Ambiguous");
		System.out.println();
		System.out.println(countBACs + "\tBACs used for phasing SNPs");
		System.out.println(countNoSNP + "\tBACs with no SNPs");
		System.out.println();
	}

	private String listToString(ArrayList<String> arrayList) {
		String listValues = "";
		for (int i = 0; i < arrayList.size(); i++) {
			listValues += arrayList.get(i) + ",";
		}
		return listValues;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingPhasedReadsMarkSwitchWithBAC.jar <in.read> <in.snp> <out.read.mark>");
		System.out.println("\t<in.read>: generated with phasingBaseCallPhasing.jar and BACs sequenced with hiseq.");
		System.out.println("\t<in.snp>: CHR\tPOS\tHapA\tHapB\t...");
		System.out.println("\t<out.read.mark>: <in.snp>\tCOV\tSTATE_H\tSTATE_A\tSTATE_B\tSTATE_OTHER\tSWITCH\tNO_SWITCH\tCategory(SwiFromHere)");
		System.out.println("Arang Rhie, 2015-09-17. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new PhasedReadsMarkSwitchesWithBAC().go(args[0], args[1], args[2]);
		} else {
			new PhasedReadsMarkSwitchesWithBAC().printHelp();
		}
	}

}
