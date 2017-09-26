package javax.arang.phasing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.genome.util.Util;
import javax.arang.phasing.util.PhasedBlock;

public class MergePhasedBlockBedWi10XpsMap extends I2Owrapper {

	private static final short PS1 = 0;
	private static final short PS2 = 1;
	@Override
	public void hooker(FileReader fr1, FileReader frMap, FileMaker fm) {
		String line;
		String[] tokens;
		
		String ps1;
		String ps2;
		String prevPs1 = "";
		String prevPs2 = "";
		
		line = frMap.readLine();
		tokens = line.split(RegExp.TAB);
		prevPs1 = tokens[PS1];
		prevPs2 = tokens[PS2];

		HashMap<String, String> ps1ToMergedPS1Map = new HashMap<String, String>();
		HashMap<String, String> ps2ToMergedPS2Map = new HashMap<String, String>();
		String mergedPS1 = "";
		String mergedPS2 = "";
		int numPhasedBlocks = 0;
		while (frMap.hasMoreLines()) {
			line = frMap.readLine();
			tokens = line.split(RegExp.TAB);
			ps1 = tokens[PS1];
			ps2 = tokens[PS2];
			
			if (ps2ToMergedPS2Map.containsKey(ps2)) {
				ps2 = ps2ToMergedPS2Map.get(ps2);
			}
			
			if (ps1.equals("."))	continue;
			if (ps1.equals("Unphased"))	continue;
			if (ps2.equals("."))	continue;
			if (ps1.equals(prevPs1) && ps2.equals(prevPs2))	continue;
			
			if (ps1.equals(prevPs1) && !ps2.equals(prevPs2)) {
				if (mergedPS2.equals("")) {
					mergedPS2 = prevPs2;
				}
				ps2ToMergedPS2Map.put(ps2, mergedPS2);
			} else if (!ps1.equals(prevPs1) && ps2.equals(prevPs2)) {
				if (mergedPS1.equals("")) {
					mergedPS1 = prevPs1;
				}
				ps1ToMergedPS1Map.put(ps1, mergedPS1);
			} else if (!ps1.equals(prevPs1) && !ps2.equals(prevPs2)) {
				mergedPS1 = ps1;
				mergedPS2 = ps2;
				numPhasedBlocks++;
			}
			
//			System.out.print("[DEBUG] :: " + line + "\t");
//			if (ps1ToPs2Map.containsKey(ps1)) {
//				System.out.println(ps1ToPs2Map.get(ps1));
//			} else {
//				System.out.println(ps1);
//			}
			
			prevPs1 = ps1;
			prevPs2 = ps2;
		}
		numPhasedBlocks++;
		System.out.println("Num. phased blocks: " + numPhasedBlocks);
		
		String ps;
		PhasedBlock block = null;
		ArrayList<Double> lenArr = new ArrayList<Double>();
		
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			tokens = line.split(RegExp.TAB);
			ps = tokens[PhasedBlock.ID];
			if (ps1ToMergedPS1Map.containsKey(ps)) {
				ps = ps1ToMergedPS1Map.get(ps);
			}
			
			if (block == null) {
				block = new PhasedBlock(tokens[PhasedBlock.CHR],
						Integer.parseInt(tokens[PhasedBlock.START]),
						Integer.parseInt(tokens[PhasedBlock.END]), ps);
			}
			else{
				if (block.merge(Integer.parseInt(tokens[PhasedBlock.START]), Integer.parseInt(tokens[PhasedBlock.END]), ps)) {
					// merged
				} else {
					// not merge-able
					fm.writeLine(block.getChr() + "\t" + block.getStart() + "\t" + block.getEnd() + "\t" + block.getPS() + "\t" + block.getLen());
					lenArr.add(block.getLen());
					block = new PhasedBlock(tokens[PhasedBlock.CHR],
						Integer.parseInt(tokens[PhasedBlock.START]),
						Integer.parseInt(tokens[PhasedBlock.END]), ps);
					
				}
				
			}
		}
		fm.writeLine(block.getChr() + "\t" + block.getStart() + "\t" + block.getEnd() + "\t" + block.getPS() + "\t" + String.format("%,.0f", block.getLen()));
		lenArr.add(block.getLen());
		
		Collections.sort(lenArr);
		double len;
		double lenSum = 0;
		for (int i = 0; i < lenArr.size(); i++) {
			len = lenArr.get(i);
			lenSum += len;
		}
		double n50 = Util.getN50(lenArr, lenSum);
		System.out.println("Longest Block Size: " + String.format("%,2d", lenArr.get(lenArr.size() - 1)));
		System.out.println("N50: " + String.format("%,2d", n50));
		System.out.println("Num. Blocks: " + numPhasedBlocks);
		System.out.println("Genome Covered Bases: " + String.format("%,.0f", lenSum));
		System.out.println("For simplicity; order is longest block size, block N50, num. blocks, genome covered bases:");
		System.out.println(lenArr.get(lenArr.size() - 1));
		System.out.println(String.format("%.0f", n50));
		System.out.println(lenArr.size());
		System.out.println(String.format("%.0f", lenSum));
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingMergePhasedBlockBedWi10XpsMap.jar <in.phased.bed> <ps_map> <out.phased.bed>");
		System.out.println("\t<in.phased.bed>: 01_iterate_phasing_correction/2/AK1_chrN.phased.bed, generated with phasingPhasedReadsToPhasedBlocks.jar");
		System.out.println("\t<ps_map>: ps pair of <in.phased.bed> to 10X PS. Generated with ");
		System.out.println("Arang Rhie, 2015-08-17. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new MergePhasedBlockBedWi10XpsMap().go(args[0], args[1], args[2]);
		} else {
			new MergePhasedBlockBedWi10XpsMap().printHelp();
		}
	}

}
