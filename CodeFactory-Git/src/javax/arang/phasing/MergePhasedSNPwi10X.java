package javax.arang.phasing;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedSNP;

public class MergePhasedSNPwi10X extends IOwrapper {

	private static int OFFSET = 7;
	private static int PS1 = PhasedSNP.PS;
	private static int PS2 = OFFSET + PhasedSNP.PS;
	private static int switchedCount = 0;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String ps1;
		String ps2;
		String prevPs1 = "";
		String prevPs2 = "";
		
		HashMap<String, String> ps1ToMergedPS1Map = new HashMap<String, String>();
		HashMap<String, String> ps2ToMergedPS2Map = new HashMap<String, String>();
		int numPhasedBlocks = 1;
//		String prevLine = "";

		line = fr.readLine();
		tokens = line.split(RegExp.TAB);
		prevPs1 = tokens[PS1];
		prevPs2 = tokens[PS2];
		String mergedPS1 = prevPs1;
		String mergedPS2 = prevPs2;
		String prevFlag = tokens[tokens.length - 1];
		String flag = tokens[tokens.length - 1];
		boolean switchHere = false;
		if (!flag.equals("Diff")) {
			if (!flag.equals(prevFlag)) {
				switchHere = !switchHere;
				writeOut(fm, prevPs1, prevPs1,  "SwitchThisBlock");
			} else {
				writeOut(fm, prevPs1, prevPs1);
			}
		}
		
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			ps1 = tokens[PS1];
			ps2 = tokens[PS2];
			flag = tokens[tokens.length - 1];
			if (flag.equals("Diff")) continue;
			
			if (ps2ToMergedPS2Map.containsKey(ps2)) {
				ps2 = ps2ToMergedPS2Map.get(ps2);
			}
			
			if (ps1.equals(prevPs1) && !ps2.equals(prevPs2)) {
				if (mergedPS2.equals("")) {
					mergedPS2 = prevPs2;
				}
				ps2ToMergedPS2Map.put(ps2, mergedPS2);
			} else if (!ps1.equals(prevPs1) && ps2.equals(prevPs2)) {
				ps1ToMergedPS1Map.put(ps1, mergedPS1);
				if (!flag.equals(prevFlag)) {
					switchHere = !switchHere;
				}

				if (switchHere) {
					writeOut(fm, ps1, (ps1ToMergedPS1Map.containsKey(ps1) ? ps1ToMergedPS1Map.get(ps1) : ps1),  "SwitchThisBlock");
				} else {
					writeOut(fm, ps1, (ps1ToMergedPS1Map.containsKey(ps1) ? ps1ToMergedPS1Map.get(ps1) : ps1));
				}
				
				//// For debugging ///
//				System.out.println(prevLine);
//				System.out.print("[DEBUG] :: " + tokens[PhasedSNP.CHR] + "\t" + tokens[PhasedSNP.POS]
//						+ "\t" + tokens[PhasedSNP.HAPLOTYPE_A] + "\t" + tokens[PhasedSNP.HAPLOTYPE_B]
//						+ "\t" + ps1 + "\t" + ps2 + "\t" + tokens[tokens.length - 1] + "\t");
//				if (ps1ToMergedPS1Map.containsKey(ps1)) {
//					System.out.println(ps1ToMergedPS1Map.get(ps1));
//				} else {
//					System.out.println(ps1);
//				}
				/////////////////////
			
			} else if (!ps1.equals(prevPs1) && !ps2.equals(prevPs2)) {
				mergedPS1 = ps1;
				mergedPS2 = ps2;
				numPhasedBlocks++;
				switchHere = false;
				writeOut(fm, ps1, (ps1ToMergedPS1Map.containsKey(ps1) ? ps1ToMergedPS1Map.get(ps1) : ps1));
			}
			
//			prevLine = "[DEBUG] :: " + tokens[PhasedSNP.CHR] + "\t" + tokens[PhasedSNP.POS]
//					+ "\t" + tokens[PhasedSNP.HAPLOTYPE_A] + "\t" + tokens[PhasedSNP.HAPLOTYPE_B]
//					+ "\t" + ps1 + "\t" + ps2 + "\t" + tokens[tokens.length - 1] + "\t";
//			if (ps1ToMergedPS1Map.containsKey(ps1)) {
//				prevLine = prevLine + (ps1ToMergedPS1Map.get(ps1));
//			} else {
//				prevLine = prevLine + (ps1);
//			}
			
			prevFlag = flag;
			prevPs1 = ps1;
			prevPs2 = ps2;
		}
		System.out.println("Num. phased blocks: " + numPhasedBlocks);
		System.out.println("Num. switched blocks: " + switchedCount);
	}

	private void writeOut(FileMaker fm, String ps1,  String psMerged, String flag) {
		switchedCount++;
		fm.writeLine(ps1 + "\t" + psMerged + "\t" + flag);
	}

	private void writeOut(FileMaker fm, String ps1,  String psMerged) {
		fm.writeLine(ps1 + "\t" + psMerged);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingMergePhasedSNPwi10X.jar <in.mark.snp> <out.ps_to_switch.list> [OFFSET]");
		System.out.println("Merge phased SNPs with 10X SNPs, by switching (if needed) & merging blocks");
		System.out.println("\t<in.mark.snp>: Generated using _mark_filt_merge_ps.sh");
		System.out.println("\t<out.ps_to_switch.list>: List of PS blocks to switch. PS1\tPS1_Merged\tSwitchThisBlock");
		System.out.println("\t[OFFSET]: Column offset for 10X SNPs starting idx. DEFAULT = 7");
		System.out.println("Arang Rhie, 2015-08-21. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new MergePhasedSNPwi10X().go(args[0], args[1]);
		} else if (args.length == 3) {
			OFFSET = Integer.parseInt(args[2]);
			PS2 = OFFSET + PhasedSNP.PS;
			new MergePhasedSNPwi10X().go(args[0], args[1]);
		} else {
			new MergePhasedSNPwi10X().printHelp();
		}
	}

}
