package javax.arang.phasing.snp;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedSNP;

public class AddLongRangeSwitchOnSnpIntersect extends IOwrapper {
	
	private static int psIdx = 0;

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
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
		//String lastSwitchedPos = "";
		
		HashMap<Integer, String> posToSwitchMap = new HashMap<Integer, String>();
		int posBlockStart = 0;	// beginning position of a block agreeing
		//int prevPos = 0;
		boolean isToDiscard = true;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			if (!tokens[tokens.length - 1].equals("Same") && !tokens[tokens.length - 1].equals("Switch")) {
				// Homozygous or indel-suspected sites are not counted in totaling snps
				continue;
			}
			ps1 = tokens[PhasedSNP.PS];
			ps2 = tokens[psIdx];
			sameOrSwitch = tokens[tokens.length - 1];
			if (isFirst) {
				isFirst = false;
				prevPs1 = ps1;
				prevPs2 = ps2;
				prevSameOrSwitch = sameOrSwitch;
				posBlockStart = Integer.parseInt(tokens[PhasedSNP.POS]);
				isToDiscard = false;
				continue;
			}
			// within a block
			if (prevPs1.equals(ps1) || prevPs2.equals(ps2)) {
				numSNP++;
				
				// switch occurred (Same -> Switch or Switch -> Same)
				if (!prevSameOrSwitch.equals(sameOrSwitch)) {
					if (!isFirstSwitch) {
						if (!isToDiscard) {
							if (switchInterval == 1) {
								numShortSwitch1++;
								posToSwitchMap.put(posBlockStart, "Short1");
								isToDiscard = true;
							} else if (switchInterval == 2){
								numShortSwitch2++;
								posToSwitchMap.put(posBlockStart, "Short2");
								isToDiscard = true;
							} else if (switchInterval > 2) {
								numLongRangeSwitch++;
								posToSwitchMap.put(posBlockStart, "Long");
							}
						} else {
							if (switchInterval > 2) {
								isToDiscard = false;
							}
						}
					} else {
						isFirstSwitch = false;
					}
					posBlockStart = Integer.parseInt(tokens[PhasedSNP.POS]);
						
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
				if (!isFirstSwitch && !isToDiscard) {
					if (switchInterval == 1) {
						numShortSwitch1++;
						posToSwitchMap.put(posBlockStart, "Short1");
						isToDiscard = true;
					} else if (switchInterval == 2) {
						numShortSwitch2++;
						posToSwitchMap.put(posBlockStart, "Short2");
						isToDiscard = true;
					} else if (switchInterval > 2) {
						numLongRangeSwitch++;
						posToSwitchMap.put(posBlockStart, "Long");
					}
				}
				
				// reset interval
				isFirstSwitch = true;
				switchInterval = 1;
				isToDiscard = false;
				
				//pos = tokens[PhasedSNP.POS];
			}
			
			prevPs1 = ps1;
			prevPs2 = ps2;
			prevSameOrSwitch = sameOrSwitch;
			//prevPos = Integer.parseInt(tokens[PhasedSNP.POS]);
		}
		
		System.out.println(numShortSwitch1 + "\tShortRangeSwitch(1)");
		System.out.println(numShortSwitch2 + "\tShortRangeSwitch(2)");
		System.out.println(numLongRangeSwitch + "\tLongRangeSwitch(>2)");
		System.out.println(numSNP + "\tTotalNumSnpExcludingFirstSNPofEachPS");
		System.out.println(String.format("%,.4f", ((float)(100*numLongRangeSwitch)/numSNP)) + "\tSwitchError(%)");
		
		fr.reset();
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			posBlockStart = Integer.parseInt(tokens[PhasedSNP.POS]);
			if (posToSwitchMap.containsKey(posBlockStart)) {
				fm.writeLine(line + "\t" + posToSwitchMap.get(posBlockStart));
			} else {
				fm.writeLine(line);
			}
					
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("java -jar phasingAddLongRangeSwitchOnSnpIntersect.jar <in.intersect.mark> <out.intersect.mark.lrswitch> <psIdx>");
		System.out.println("\t<in.intersect.mark>: generated with phasingPhasedSnpIntersect.jar and phasingPhasedSnpMarkSwitch.jar");
		System.out.println("\t<out.intersect.mark.lrswitch>: At the last column, Short1 / Short2 / Long will be added for switch sites.");
		System.out.println("\t<psIdx>: 2nd intersected snp PS idx. 1-based.");
		System.out.println("Arang Rhie, 2015-11-27. arrhie@gmail.com");
		
	}

	public static void main(String[] args) {
		if (args.length > 1) {
			psIdx = Integer.parseInt(args[2]) - 1;
			new AddLongRangeSwitchOnSnpIntersect().go(args[0], args[1]);
		} else {
			new AddLongRangeSwitchOnSnpIntersect().printHelp();
		}
	}

}
