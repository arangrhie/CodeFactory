package javax.arang.phasing;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedSNP;
import javax.arang.phasing.util.PhasedSV;

public class PhasedSVtoMarkerSnp extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		char a = '0';
		char b = '0';
		String pos = null;
		String haplotype;
		HashMap<String, PhasedSNP> markers = new HashMap<String, PhasedSNP>();
		ArrayList<String> posList = new ArrayList<String>();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			if (PhasedSV.isSubstitution(tokens[PhasedSV.TYPE])) {
				haplotype = tokens[PhasedSV.HAPLPOTYPE];
				if (haplotype.equals("A")) {
					a = tokens[PhasedSV.ALLELES].charAt(2);
					b = tokens[PhasedSV.ALLELES].charAt(0);
				} else if (haplotype.equalsIgnoreCase("B")) {
					a = tokens[PhasedSV.ALLELES].charAt(0);
					b = tokens[PhasedSV.ALLELES].charAt(2);
				} else {
					// H?
					System.out.println("No haplotype available. check input file. should have A or B.");
					System.exit(-1);
				}
				addMarkerSnp(markers, tokens[PhasedSV.CONTIG], tokens[PhasedSV.END], a, b);
				posList.add(tokens[PhasedSV.END]);
			} else if (PhasedSV.isDeletion(tokens[PhasedSV.TYPE])) {
				haplotype = tokens[PhasedSV.HAPLPOTYPE];
				float af = (float) Float.parseFloat(tokens[PhasedSV.DEPTH_ALLELE]) / (float) Float.parseFloat(tokens[PhasedSV.DEPTH_TOTAL]);
				if (af < 60) continue;
				if (tokens[PhasedSV.ALLELES].equals("1")) {
					pos = tokens[PhasedSV.END];
				} else {
					int newPos = (Integer.parseInt(tokens[PhasedSV.END]) + Integer.parseInt(tokens[PhasedSV.START]));
					newPos = (newPos / 2) + 1;
					pos = newPos + "";
				}
				if (haplotype.equals("A")) {
					addMarkerSnp(markers, tokens[PhasedSV.CONTIG], pos, 'D', 'N');
				} else if (haplotype.equals("B")) {
					addMarkerSnp(markers, tokens[PhasedSV.CONTIG], pos, 'N', 'D');
				}
				posList.add(pos);
			}
		}
		
		PhasedSNP snp;
		for (int i = 0; i < posList.size(); i++) {
			if (markers.containsKey(posList.get(i))) {
				snp = markers.get(posList.get(i));
				fm.writeLine(snp.getChr() + "\t" + snp.getPos() + "\t" + snp.getHaplotypeA() + "\t" + snp.getHaplotypeB());
			}
		}
	}
	
	private void addMarkerSnp(HashMap<String, PhasedSNP> markers, String contig, String pos, char hapA, char hapB) {
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
		} else {
			PhasedSNP snp = new PhasedSNP(contig, Integer.parseInt(pos), hapA + "", hapB + "");
			markers.put(pos, snp);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingPhasedSVtoMarkerSnp.jar <in.sv> <out.snp>");
		System.out.println("\tTake SUBSTITUTION and DELETION to build marker SNPs.");
		System.out.println("\t<in.sv>: generated with phasingPhasedBaseToSV.jar");
		System.out.println("\t\tCONTIG\tSTART\tEND\tTYPE(SUBSTITUTION/DELETION/MULTI_ALLELE/UNPHASED)\tALLELES\tALLELE_DEPTH\tTOTAL_DEPTH\tHAPLOTYPE");
		System.out.println("\t\tDELETION region will be transfered into a marker, in the midle of the region");
		System.out.println("\t<out.snp>: CONTIG\tPOS\tHAP_A\tHAP_B");
		System.out.println("Arang Rhie, 2015-12-05. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new PhasedSVtoMarkerSnp().go(args[0], args[1]);
		} else {
			new PhasedSVtoMarkerSnp().printHelp();
		}
	}

}
