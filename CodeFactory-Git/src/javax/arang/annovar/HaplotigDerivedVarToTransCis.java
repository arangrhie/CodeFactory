package javax.arang.annovar;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class HaplotigDerivedVarToTransCis extends IOwrapper {

	private HashMap<String, Integer[]> geneToCountMap = new HashMap<String, Integer[]>();
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String gene;
		String exonicFunc;
		String hap;
		String hethom;
		int countA = 0;
		int countB = 0;
		
		boolean isFirst = true;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			if (isFirst) {
				if (gene_idx < 0) {
					gene_idx = tokens.length + gene_idx;
				}
				if (hap_idx < 0) {
					hap_idx = tokens.length + hap_idx;
				}
				if (het_hom_idx < 0) {
					het_hom_idx = tokens.length + het_hom_idx;
				}
				isFirst = false;
				System.out.println("gene_idx: " + gene_idx);
				System.out.println("hap_idx: " + hap_idx);
				System.out.println("het_hom_idx: " + het_hom_idx);
			}
			
			gene = tokens[gene_idx];
			exonicFunc = tokens[gene_idx + 2];
			hap = tokens[hap_idx];
			hethom = tokens[het_hom_idx];
			
			countA = 0;
			countB = 0;
			
			if (hethom.equalsIgnoreCase("HOM")) {
				countA = 1;
				countB = 1;
			} else {
				if (hap.startsWith("A")) {
					countA = 1;
					countB = 0;
				} else if (hap.startsWith("B")) {
					countA = 0;
					countB = 1;
				} else {
					System.err.println("[DEBUG] :: hap: " + hap);
				}
			}
			addToMap(gene, exonicFunc, countA, countB);
		}
		
		Integer[] counts;
		for (String geneItem : geneToCountMap.keySet()) {
			counts = geneToCountMap.get(geneItem);
			if (counts[2] * counts[3] > 0) {
				fm.writeLine(geneItem + "\t" + counts[0] + "\t" + counts[1] + "\t" + counts[2] + "\t" + counts[3] + "\tTRANS");
			} else if (counts[2] == counts[3]) {
				fm.writeLine(geneItem + "\t" + counts[0] + "\t" + counts[1] + "\t" + counts[2] + "\t" + counts[3] + "\tNoAA.Change");
			} else {
				fm.writeLine(geneItem + "\t" + counts[0] + "\t" + counts[1] + "\t" + counts[2] + "\t" + counts[3] + "\tCIS");
			}
		}
		
	}
	
	private void addToMap(String gene, String exonic, int countA, int countB) {
		int countFuncA;
		int countFuncB;
		Integer[] counts;
		
		if (exonic.equals("NA") || exonic.startsWith("synonymous") || exonic.startsWith("unknown")) {
			countFuncA = 0;
			countFuncB = 0;
		} else {
			countFuncA = countA;
			countFuncB = countB;
		}
		
		for (String geneItem : gene.split(",")) {
			if (geneToCountMap.containsKey(geneItem)) {
				counts = geneToCountMap.get(geneItem);
				counts = FilterPhasedGenes.addCounts(counts, countA, countB, countFuncA, countFuncB);
			} else {
				counts = FilterPhasedGenes.initCounts(countA, countB, countFuncA, countFuncB);
			}
			geneToCountMap.put(geneItem, counts);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar annovarHaplotigDerivedVarToTransCis.jar <in.avout.het.hom.func> <out.trans_cis> <gene_idx> <hap_idx> <het_hom_idx>");
		System.out.println("\t<in.avout.het.hom>: select genes covered within both phased blocks, annotate with annovar, add haplotype and het/hom info.");
		System.out.println("\t<out.trans_cis>: GENE_NAME\tA\tB\tAA.A\tAA.B");
		System.out.println("\t<gene_idx>: gene name column. sep=\",\" 1-based index, <=0 when starting from the end");
		System.out.println("\t<hap_idx>: Column containing A(AH) or B. 1-based index, <=0 when starting from the end");
		System.out.println("\t<het_hom_idx>: Column containing HET/HOM. 1-based index, <=0 when starting from the end");
		System.out.println("Arang Rhie, 2016-02-12. arrhie@gmail.com");
	}

	private static int gene_idx = 0;
	private static int hap_idx = 0;
	private static int het_hom_idx = 0;
	
	public static void main(String[] args) {
		if (args.length == 5) {
			gene_idx = Integer.parseInt(args[2]) - 1;
			hap_idx =  Integer.parseInt(args[3]) - 1;
			het_hom_idx = Integer.parseInt(args[4]) - 1;
			new HaplotigDerivedVarToTransCis().go(args[0], args[1]);
		} else {
			new HaplotigDerivedVarToTransCis().printHelp();
		}
	}

}
