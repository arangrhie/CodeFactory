package javax.arang.annovar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

import org.apache.commons.math3.stat.inference.TTest;

public class AddASE extends IOwrapper {

	HashMap<String, ArrayList<Double>> geneToCountA = new HashMap<String, ArrayList<Double>>();
	HashMap<String, ArrayList<Double>> geneToCountB = new HashMap<String, ArrayList<Double>>();
	HashMap<String, Integer> geneToSnpCount = new HashMap<String, Integer>();
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String haplotype;
		Double depthA;
		Double depthB;
		
		// Input file: CHR\tSTART\tEND\tREF_ALLELE\tALT_ALLELE\trsID\texonic/intronic/...\tGene\tAA_Change\tHaplotype\tRef_Depth\tAlt_Depth
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			String gene = tokens[geneIdx];
			haplotype = tokens[hapIdx];
			if (haplotype.equals("B")) {
				depthA = Double.parseDouble(tokens[rnaRefDepthIdx]);
				depthB = Double.parseDouble(tokens[rnaRefDepthIdx + 1]);
			} else {
				depthA = Double.parseDouble(tokens[rnaRefDepthIdx + 1]);
				depthB = Double.parseDouble(tokens[rnaRefDepthIdx]);
			}

			for (String geneItem : gene.split(",|;")) {
				int count = 0;
				ArrayList<Double> countA = new ArrayList<Double>();
				ArrayList<Double> countB = new ArrayList<Double>();
				if (geneToSnpCount.containsKey(geneItem)) {
					count = geneToSnpCount.get(geneItem);
					countA = geneToCountA.get(geneItem);
					countB = geneToCountB.get(geneItem);
				}
				countA.add(depthA);
				countB.add(depthB);
				geneToSnpCount.put(geneItem, count + 1);
				geneToCountA.put(geneItem, countA);
				geneToCountB.put(geneItem, countB);
			}
		}
		
		// Write results
		Set<String> genesWiASE = geneToSnpCount.keySet();
		System.out.println("Genes with ASE: " + genesWiASE.size());
		
		double[] countAarr = null;
		double[] countBarr = null;
		float countAsum = 0;
		float countBsum = 0;
		int count;
		double p;
		
		// for each gene, calculate p-value from the collected read depth A and B
		for (String gene : genesWiASE) {
			count = geneToSnpCount.get(gene); 
			if (count == 1) {
				fm.writeLine(gene + "\t1\t" + geneToCountA.get(gene).get(0) + "\t" + geneToCountB.get(gene).get(0) + "\tNA");
			} else {
				countAarr = new double[count];
				countBarr = new double[count];
				countAsum = 0;
				countBsum = 0;
				for (int i = 0; i < count; i++) {
					countAarr[i] = geneToCountA.get(gene).get(i);
					countBarr[i] = geneToCountB.get(gene).get(i);
					countAsum += geneToCountA.get(gene).get(i);
					countBsum += geneToCountB.get(gene).get(i);
				}
				p = new TTest().pairedTTest(countAarr, countBarr);
				fm.writeLine(gene + "\t" + count + "\t" + String.format("%,.2f", countAsum / count) + "\t" + String.format("%,.2f", countBsum / count) + "\t" + p);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar annovarAddASE.jar <in.rna.expressed> <out.gene_ase.txt> <gene_idx> <hap_idx> <rna_ref_depth_idx>");
		System.out.println("\t<in.rna.expreassed>: annovar annotated file with haplotype, rna seq depth by each allele");
		System.out.println("\t<out.gene_ase.txt>: Gene\tNumHetSNPsWiASE\tMeanA\tMeanB\tP-value");
		System.out.println("\t<gene_idx>: column containing gene name. could be several genes seperated with , or ;");
		System.out.println("\t<hap_idx>: colum containing haplotype A (or AH), B. 1-based");
		System.out.println("\t<rna_ref_depth_idx>: column containing reference allele read depth. 1-based");
		System.out.println("\t\taltered allele read depth column is assumed to be as <rna_ref_depth_idx>+1.");
		System.out.println("Arang Rhie, 2016-03-02. arrhie@gmail.com");
	}
	
	private static int geneIdx;
	private static int hapIdx;
	private static int rnaRefDepthIdx;
	
	public static void main(String[] args) {
		if (args.length == 5) {
			geneIdx = Integer.parseInt(args[2]) - 1;
			hapIdx = Integer.parseInt(args[3]) - 1;
			rnaRefDepthIdx = Integer.parseInt(args[4]) - 1;
			new AddASE().go(args[0], args[1]);
		} else {
			new AddASE().printHelp();
		}
	}

}
