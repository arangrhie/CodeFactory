package javax.arang.bed;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class DeterminHeterozygousSV extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		int delBasesA;
		int delBasesB;
		float svLenAp;
		float svLenBp;
		int svLen;
		String heterozygosityA = "";
		String heterozygosityB = "";
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			heterozygosityA = "";
			heterozygosityB = "";
			if (tokens[tokens.length - 4].equalsIgnoreCase("UNPHASED")) {
				heterozygosityA = "UNPHASED";
			} else {
				svLen = Integer.parseInt(tokens[Bed.END]) - Integer.parseInt(tokens[Bed.START]);
				delBasesA = Integer.parseInt(tokens[tokens.length - 4]);
				svLenAp = Float.parseFloat(tokens[tokens.length - 3]);
				if (delBasesA == 0) {
					heterozygosityA = "A";
				} else if (delBasesA > svLen) {
					if (svLenAp < 90) {
						heterozygosityA = "UNPHASED";
					}
					// else do nothing: Is deleted
				} else {
					if (svLenAp < 30) {
						heterozygosityA = "A";
					}
					// do nothing: Is deleted
					//heterozygosityA = "DELETED";
				}
			}
			if (tokens[tokens.length - 2].equalsIgnoreCase("UNPHASED")) {
				heterozygosityB = "UNPHASED";
			} else {
				svLen = Integer.parseInt(tokens[Bed.END]) - Integer.parseInt(tokens[Bed.START]);
				delBasesB = Integer.parseInt(tokens[tokens.length - 2]);
				svLenBp = Float.parseFloat(tokens[tokens.length - 1]);
				if (delBasesB == 0) {
					heterozygosityB = "B";
				} else if (delBasesB > svLen) {
					if (svLenBp < 90) {
						heterozygosityB = "UNPHASED";
					}
					//else do nothing: Is deleted
					//heterozygosityB = "DELETED";
				} else {
					if (svLenBp < 30) {
						heterozygosityB = "B";
					}
					// do nothing: Is deleted
				}
			}
			
			if (heterozygosityA.equals("A") && heterozygosityB.equals("B")) {
				fm.writeLine(line + "\tH");
			} else if ((heterozygosityA.equals("") && heterozygosityB.equals(""))
					|| (heterozygosityA.equals("") && heterozygosityB.equalsIgnoreCase("UNPHASED"))
					|| (heterozygosityB.equals("") && heterozygosityA.equalsIgnoreCase("UNPHASED"))) {
				fm.writeLine(line + "\tFP");
			} else {
				if (heterozygosityA.equalsIgnoreCase("UNPHASED") && heterozygosityB.equalsIgnoreCase("UNPHASED")){
					fm.writeLine(line + "\tUNPHASED");
				} else if (!heterozygosityB.equals("UNPHASED") && (heterozygosityA.equals("") || heterozygosityA.equals("UNPHASED"))) {
					fm.writeLine(line + "\t" + heterozygosityB);
				} else if (!heterozygosityA.equals("UNPHASED") && (heterozygosityB.equals("") || heterozygosityB.equals("UNPHASED"))) {
					fm.writeLine(line + "\t" + heterozygosityA);
				} else {
					fm.writeLine(line + "\tFP");
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedDeterminHeterozygousSV.jar <in.bed> <out.bed>");
		System.out.println("\t<in.bed>: $NF-3, $NF-1: delBases of haplotype A / B, $NF-2, $NF: % of deleted bases");
		System.out.println("\t<out.bed>: At the end of <in.bed>, H/A/B/UNPHASED will be added.");
		System.out.println("\t\tdelBases==0: H or A, B");
		System.out.println("\t\t((float)svLen * 100) / bases) < 90%: UNPHASED");
		System.out.println("Arang Rhie, 2015-12-20. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new DeterminHeterozygousSV().go(args[0], args[1]);
		} else {
			new DeterminHeterozygousSV().printHelp();
		}
	}

}
