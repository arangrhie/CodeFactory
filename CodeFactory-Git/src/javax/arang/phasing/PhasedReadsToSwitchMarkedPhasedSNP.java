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

@Deprecated
public class PhasedReadsToSwitchMarkedPhasedSNP extends I2Owrapper {
	
	private static short COV = 0;
	private static short DEL = 1;
	private static short SWITCH = 2;

	@Override
	public void hooker(FileReader frRead, FileReader frSNP, FileMaker fm) {
		String line;
		String[] tokens;
		
		ArrayList<Integer> posList = new ArrayList<Integer>();
		HashMap<Integer, String> posToSNP = new HashMap<Integer, String>();
		HashMap<Integer, Integer[]> posToCount = new HashMap<Integer, Integer[]>();
		HashMap<Integer, Boolean> posToAssigned = new HashMap<Integer, Boolean>();
		
		// Read SNPs
		int pos;
		while (frSNP.hasMoreLines()) {
			line = frSNP.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			
			pos = Integer.parseInt(tokens[PhasedSNP.POS]);
			posList.add(pos);
			posToSNP.put(pos, line);
			posToCount.put(pos, Util.initArr(3));
		}
		
		int a = 0;
		int b = 0;
		String haplotypeList = "";
		char haplotype;
		int haplotypeIdx;
		char baseToSwitch = 'B';
		char baseNotToSwitch = 'A';
		Integer[] count = new Integer[2];
		int prevAssignedSNP = 0;
		int isAgreeing = 0;
		int isNotAgreeing = 0;
		int countNoSNP = 0;
		int countSkipped = 0;
		int countDel = 0;
		int countBACs = 0;
		READ_BAC_LOOP : while (frRead.hasMoreLines()) {
			line = frRead.readLine();
			tokens = line.split(RegExp.TAB);
			
			// Read is containing NO SNP
			if (tokens.length == PhasedRead.HAPLOTYPE) {
				countNoSNP++;
				continue READ_BAC_LOOP;
			}

			a = Integer.parseInt(tokens[PhasedRead.NUM_A]);
			b = Integer.parseInt(tokens[PhasedRead.NUM_B]);
			
			if (a < b) {
				baseToSwitch = 'A';
				baseNotToSwitch ='B';
			} else {
				baseToSwitch = 'B';
				baseNotToSwitch = 'A';
			}
			
			// Search for overlapping SNPs from previous assigned SNPs
			haplotypeIdx = 0;
			haplotypeList = tokens[PhasedRead.HAPLOTYPE];
			
			isAgreeing = 0;
			isNotAgreeing = 0;
			
			// Skip if BAC is not improving phasing the SNPs
			if (prevAssignedSNP >= Integer.parseInt(tokens[tokens.length - 1]) && skipShortBACs) {
				//System.out.println("[DEBUG] :: This BAC is skipped: " + tokens[PhasedRead.READ_ID] + " ( len: " + tokens[PhasedRead.LEN] + " )");
				countSkipped++;
				continue READ_BAC_LOOP;
			}
			
			SEARCH_LOOP : for (int i = PhasedRead.SNP_POS_LIST; i < tokens.length; i++) {
				pos = Integer.parseInt(tokens[i]);
				if (pos > prevAssignedSNP) {
					if (isAgreeing < isNotAgreeing) {
						System.out.println("[DEBUG] :: " + tokens[PhasedRead.READ_ID] + "\t" + isAgreeing + "\t" + isNotAgreeing);
						char tmp = baseNotToSwitch;
						baseNotToSwitch = baseToSwitch;
						baseToSwitch = tmp;
					} else if (isAgreeing == isNotAgreeing && isAgreeing > 0) {
						// Report this BAC as an 'un-reliable' BAC
						System.out.println("[DEBUG] :: " + tokens[PhasedRead.READ_ID] + "\t" + isAgreeing + "\t" + isNotAgreeing + "\tUNRELIABLE");
						//System.out.println("[DEBUG] :: This BAC is un-reliable, so remove: " + tokens[PhasedRead.READ_ID] + " ( len: " + tokens[PhasedRead.LEN] + " )");
						continue READ_BAC_LOOP;
					}
					break SEARCH_LOOP;
				}
				else {
					// if (pos <= prevAssignedSNP) {
					if (!posToAssigned.containsKey(pos)) {
						haplotypeIdx++;
						continue SEARCH_LOOP;
					}
					haplotype = haplotypeList.charAt(haplotypeIdx);
					
					if (haplotype == baseToSwitch) {
						if (posToCount.get(pos)[SWITCH] > 0) {
							isAgreeing++;
						} else if (posToCount.get(pos)[SWITCH] == 0 && posToCount.get(pos)[COV] > 0) {
							isNotAgreeing++;
						}
					} else if (haplotype == baseNotToSwitch) {
						if (posToCount.get(pos)[SWITCH] > 0) {
							isNotAgreeing++;
						} else if (posToCount.get(pos)[SWITCH] == 0 && posToCount.get(pos)[COV] > 0) {
							isAgreeing++;
						}
					}
					haplotypeIdx++;
				}
			}
			
			
			haplotypeIdx = 0;
			for (int i = PhasedRead.SNP_POS_LIST; i < tokens.length; i++) {
				pos = Integer.parseInt(tokens[i]);
				haplotype = haplotypeList.charAt(haplotypeIdx);
				count = posToCount.get(pos);
				
				if (haplotype == 'A' || haplotype == 'B') {
					count[COV]++;
					if (!posToAssigned.containsKey(pos)) {
						posToAssigned.put(pos, true);
						if (pos > prevAssignedSNP) {
							prevAssignedSNP = pos;
						}
					}
					if (haplotype == baseToSwitch) {
						count[SWITCH]++;
					}
				} else if (haplotype == 'D') {
					count[DEL]++;
				}
				
				haplotypeIdx++;
			}
			//System.out.println("[DEBUG] :: " + tokens[PhasedRead.READ_ID] + "\tprevAssignedSNP\t" + prevAssignedSNP);
			countBACs++;
		}
		
		
		int countSwitched = 0;
		int countAmbiguous = 0;
		for (int i = 0; i < posList.size(); i++) {
			pos = posList.get(i);
			if (posToCount.get(pos)[COV] == 0 && !isToReportAllSNPs && posToCount.get(pos)[DEL] >= 0) {
				continue;
			}
			if (posToCount.get(pos)[DEL] > 0) {
				countDel++;
				continue;
			}
			fm.write(posToSNP.get(pos) + "\t" +
					+ posToCount.get(pos)[COV] + "\t" + posToCount.get(pos)[DEL] + "\t" + posToCount.get(pos)[SWITCH]);
			if (posToCount.get(pos)[SWITCH] == posToCount.get(pos)[COV] && posToCount.get(pos)[SWITCH] > 0) {
				countSwitched++;
				fm.write("\tSwitch");
			} else if (posToCount.get(pos)[SWITCH] < posToCount.get(pos)[COV] && posToCount.get(pos)[SWITCH] > 0) {
				countAmbiguous++;
				fm.write("\tAmbiguous");
			}
			fm.writeLine();
		}
		System.out.println();
		System.out.println(countSwitched + "\tMarked to Switched");
		System.out.println(countAmbiguous + "\tMarked as Ambiguous");
		System.out.println(countDel + "\tSNPs with deletion (removed)");
		System.out.println();
		System.out.println(countBACs + "\tBACs used for phasing SNPs");
		System.out.println(countNoSNP + "\tBACs with no SNPs");
		System.out.println(countSkipped + "\tBACs skipped");
		System.out.println();
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingReadsToSwitchMarkedPhasedSNP.jar <in.read> <in.snp> <out.phased.snp> [ALL] [skipShortBACs]");
		System.out.println("\tConvert phased base count to phased SNPs.");
		System.out.println("\tBAC-based phasing: Assumes all the phased snps are true,");
		System.out.println("\t\tno switch error are assumed within a BAC.");
		System.out.println("\tDeletions will be removed.");
		System.out.println("\tSNPs reported in .base will be put into haplotye A first.");
		System.out.println("\t[ALL]: DEFAULT = FALSE");
		System.out.println("\t\tTRUE: Include all SNPs not covered in BAC: these SNPs will be reported as it is.");
		System.out.println("\t\tFALSE: Include only SNPs within a BAC covered.");
		System.out.println("\t[skipShortBACs]: Skip short BACs not improving phasing the SNPs. DEFUALT = true. Make false for 2nd round.");
		System.out.println("Arang Rhie, 2015-09-10. arrhie@gmail.com");
	}

	private static boolean isToReportAllSNPs = false;
	private static boolean skipShortBACs = true;
	public static void main(String[] args) {
		if (args.length == 3) {
			new PhasedReadsToSwitchMarkedPhasedSNP().go(args[0], args[1], args[2]);
		} else if (args.length == 4) {
			isToReportAllSNPs = Boolean.parseBoolean(args[3]);
			new PhasedReadsToSwitchMarkedPhasedSNP().go(args[0], args[1], args[2]);
		} else if (args.length == 5) {
			isToReportAllSNPs = Boolean.parseBoolean(args[3]);
			skipShortBACs = Boolean.parseBoolean(args[4]);
			new PhasedReadsToSwitchMarkedPhasedSNP().go(args[0], args[1], args[2]);
		} else {
			new PhasedReadsToSwitchMarkedPhasedSNP().printHelp();
		}
	}

}
