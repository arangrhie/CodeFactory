package javax.arang.phasing;

import java.util.ArrayList;

import javax.arang.IO.INwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedSNP;

public class CountLongRangeSwitch extends INwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingCountLongRangeSwitch.jar <ps_idx> <in1.intersect.marked> <in2.intersect.marked> ...");
		System.out.println("\t<ps_idx>: 1-based. phaseds block identifier (ps) column.");
		System.out.println("\t<in.intersect.marked>: generated with phasingPhasedSnpIntersect.jar and phasingPhasedSnpMarkSwitch.jar");
		System.out.println("\t<out>: stdout. Total number of snps, number of long range switches, short range switches, switch rate are printed.");
		System.out.println("\t\tSwitches between phased blocks (ps) of right side snp of <in.intersect.marked> are ignored.");
		System.out.println("\t\tShort range switches are reported by allowing 1, 2 het snp switches in a line.");
		System.out.println("Arang Rhie, 2015-11-06. arrhie@gmail.com");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs) {
		String line;
		String[] tokens;
		
		int numSNP = 0;
		int numShortSwitch1 = 0;
		int numShortSwitch2 = 0;
		int numLongRangeSwitch = 0;
		
		String prevSameOrSwitch = "";
		String sameOrSwitch;
		boolean isFirst = true;
		int switchInterval = 0;
		boolean isFirstSwitch = true;
		
		String prevPs1 = "";
		String prevPs2 = "";
		String ps1;
		String ps2;
		
		//HashMap<String, String> posToSwitchMap = new HashMap<String, String>();
		//String pos = "";
		String prevType = "Short";
		//String lastSwitchedPos = "";
		
		for (FileReader fr : frs) {
			isFirst = true;
			switchInterval = 0;
			isFirstSwitch = true;
			prevType = "Short";
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				tokens = line.split(RegExp.TAB);
				ps1 = tokens[PhasedSNP.PS];
				ps2 = tokens[psIdx];
				if (!tokens[tokens.length - 1].equals("Same") && !tokens[tokens.length - 1].equals("Switch")) {
					// Homozygous or indel-suspected sites are not counted in totaling snps
					continue;
				}
				sameOrSwitch = tokens[tokens.length - 1];
				if (isFirst) {
					isFirst = false;
					prevPs1 = ps1;
					prevPs2 = ps2;
					prevSameOrSwitch = sameOrSwitch;
					//pos = tokens[PhasedSNP.POS];
					continue;
				}
				// within a block
				if (prevPs1.equals(ps1) || prevPs2.equals(ps2)) {
					numSNP++;
					
					// switch occurred (Same -> Switch or Switch -> Same)
					if (!prevSameOrSwitch.equals(sameOrSwitch)) {
						if (!isFirstSwitch) {
							if (switchInterval == 1) {
								numShortSwitch1++;
								//posToSwitchMap.put(pos, "Short1");
								prevType = "Short";
								//System.out.println("[DEBUG] :: " + lastSwitchedPos + "\tshort(1)");
							} else if(switchInterval == 2) {
								numShortSwitch2++;
								//posToSwitchMap.put(pos, "Short2");
								prevType = "Short";
								//System.out.println("[DEBUG] :: " + lastSwitchedPos + "\tshort(2)");
							} else if (switchInterval > 2) {
								if (!prevType.equals("Short")) {
									numLongRangeSwitch++;
									//posToSwitchMap.put(pos, "Long");
									prevType = "Long";
									//System.out.println("[DEBUG] :: " + lastSwitchedPos + "\tlong");
								} else {
									prevType = "Long";
								}
							}
						} else {
							isFirstSwitch = false;
						}
						
						//pos = tokens[PhasedSNP.POS];
						
						// reset interval
						switchInterval = 1;
					} else {
						switchInterval++;
					}
				}
				// PS block changed
				else {
					// Before beginning a new block, count the switches.
					// Don't forget to count the last long-range switch!
					if (switchInterval == 1) {
						numShortSwitch1++;
						//posToSwitchMap.put(pos, "Short1");
						prevType = "Short";
						//System.out.println("[DEBUG] :: " + lastSwitchedPos + "\tshort(1)");
					} else if(switchInterval == 2) {
						numShortSwitch2++;
						//posToSwitchMap.put(pos, "Short2");
						prevType = "Short";
						//System.out.println("[DEBUG] :: " + lastSwitchedPos + "\tshort(2)");
					} else if (switchInterval > 2) {
						if (!prevType.equals("Short")) {
							numLongRangeSwitch++;
							//posToSwitchMap.put(pos, "Long");
							prevType = "Long";
							//System.out.println("[DEBUG] :: " + lastSwitchedPos + "\tlong");
						} else {
							prevType = "Short";
						}
					}
					
					// reset interval
					isFirstSwitch = true;
					switchInterval = 0;
					
					//pos = tokens[PhasedSNP.POS];
				}
				
				prevPs1 = ps1;
				prevPs2 = ps2;
				prevSameOrSwitch = sameOrSwitch;
			}
		}
		
		System.out.println(numShortSwitch1 + "\tShortRangeSwitch(1)");
		System.out.println(numShortSwitch2 + "\tShortRangeSwitch(2)");
		System.out.println(numLongRangeSwitch + "\tLongRangeSwitch(>2)");
		System.out.println(numSNP + "\tTotalNumSnpExcludingFirstSNPofEachPS");
		System.out.println(String.format("%,.4f", ((float)(100*numLongRangeSwitch)/numSNP)) + "\tSwitchError(%)");
		
	}

	private static int psIdx = 0;
	public static void main(String[] args) {
		if (args.length > 1) {
			psIdx = Integer.parseInt(args[0]) - 1;
			String[] inFiles = new String[args.length - 1];
			for (int i = 0; i < inFiles.length; i++) {
				inFiles[i] = args[i+1];
			}
			new CountLongRangeSwitch().go(inFiles);
		} else {
			new CountLongRangeSwitch().printHelp();
		}
	}

}
