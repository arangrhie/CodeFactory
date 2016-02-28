package javax.arang.phasing;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedSNP;
import javax.arang.phasing.util.PhasedSV2ndCycle;

public class Phased2ndCycleSVtoMarkerSnp extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		char a = '0';
		char b = '0';
		String haplotype;
		HashMap<String, PhasedSNP> markers = new HashMap<String, PhasedSNP>();
		ArrayList<String> posList = new ArrayList<String>();
		ArrayList<String> posMultiList = new ArrayList<String>();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			if (PhasedSV2ndCycle.isSubstitution(tokens[PhasedSV2ndCycle.TYPE])) {
				haplotype = tokens[PhasedSV2ndCycle.HAPLPOTYPE];
				if (haplotype.equals("A")) {
					a = tokens[PhasedSV2ndCycle.ALLELES].charAt(2);
					b = tokens[PhasedSV2ndCycle.ALLELES].charAt(0);
				} else if (haplotype.equalsIgnoreCase("B")) {
					a = tokens[PhasedSV2ndCycle.ALLELES].charAt(0);
					b = tokens[PhasedSV2ndCycle.ALLELES].charAt(2);
				} else {
					// H?
					System.out.println("No haplotype available. check input file. should have A or B.");
					System.exit(-1);
				}
				if (addMarkerSnp(markers, tokens[PhasedSV2ndCycle.CONTIG], tokens[PhasedSV2ndCycle.END], a, b)) {
					posList.add(tokens[PhasedSV2ndCycle.END]);
				}
			} else if (PhasedSV2ndCycle.isMultiAllele(tokens[PhasedSV2ndCycle.TYPE])) {
				posMultiList.add(tokens[PhasedSV2ndCycle.END]);
			}
		}
		
		PhasedSNP snp;
		for (int i = 0; i < posList.size(); i++) {
			if (posMultiList.contains(posList.get(i)))	continue;
			if (markers.containsKey(posList.get(i))) {
				snp = markers.get(posList.get(i));
				fm.writeLine(snp.getChr() + "\t" + snp.getPos() + "\t" + snp.getHaplotypeA() + "\t" + snp.getHaplotypeB());
			}
		}
	}
	
	private boolean addMarkerSnp(HashMap<String, PhasedSNP> markers, String contig, String pos, char hapA, char hapB) {
		if (markers.containsKey(pos)) {
			System.out.print("[DEBUG] :: Check for " + contig + "\t" + pos
					+ "\t" + markers.get(pos).getHaplotypeA() + ":" + hapA
					+ "\t" + markers.get(pos).getHaplotypeB() + ":"+ hapB);
			if (markers.get(pos).getHaplotypeA().charAt(0) == hapB
					|| markers.get(pos).getHaplotypeB().charAt(0) == hapA) {
				markers.remove(pos);
				System.out.println("\tREMOVED");
			} else {
				// do nothing
				System.out.println("\tRemained");
			}
			return false;
		} else {
			PhasedSNP snp = new PhasedSNP(contig, Integer.parseInt(pos), hapA + "", hapB + "");
			markers.put(pos, snp);
			return true;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingPhasedSVtoMarkerSnp.jar <in.sv> <out.snp>");
		System.out.println("\tTake SUBSTITUTION and DELETION to build marker SNPs.");
		System.out.println("\t<in.sv>: generated with phasingPhasedBaseToSV.jar");
		System.out.println("\t\tCONTIG\tSTART\tEND\tTYPE(SUBSTITUTION/MULTI_ALLELE)\tHAPLOTYPE\tNUM_ALLELES\tALLELES\tALLELE_DEPTH\tTOTAL_DEPTH");
		System.out.println("\t<out.snp>: CONTIG\tPOS\tHAP_A\tHAP_B");
		System.out.println("Arang Rhie, 2015-12-11. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new Phased2ndCycleSVtoMarkerSnp().go(args[0], args[1]);
		} else {
			new Phased2ndCycleSVtoMarkerSnp().printHelp();
		}
	}

}
