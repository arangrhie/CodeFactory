package javax.arang.annovar;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.annovar.util.PhasedGenes;

import org.apache.commons.math3.stat.inference.TTest;

public class PhasedGenesAddExpAlleleCount extends I2Owrapper {

	@Override
	public void hooker(FileReader frGene, FileReader frExp, FileMaker fm) {
		String line;
		String[] tokens;
		ArrayList<String> phasedGeneList = new ArrayList<String>();
		
		while (frGene.hasMoreLines()) {
			line = frGene.readLine();
			if (line.startsWith("#") || line.startsWith("geneSymbol"))	continue;
			tokens = line.split(RegExp.TAB);
			phasedGeneList.add(tokens[PhasedGenes.GENE]);
		}
		
		System.out.println("Start processing " + phasedGeneList.size() + " genes");
		
		String[] genesInLine;
		HashMap<String, ArrayList<Double>> geneToRNAcntA = new HashMap<String, ArrayList<Double>>();
		HashMap<String, ArrayList<Double>> geneToRNAcntB = new HashMap<String, ArrayList<Double>>();
		HashMap<String, Integer> geneToRNAcnt = new HashMap<String, Integer>();
		
		double refCount;
		double altCount;
		int hapAtype;
		int hapBtype;
		int count = 0;
		ArrayList<Double> countA;
		ArrayList<Double> countB;
		while (frExp.hasMoreLines()) {
			line = frExp.readLine();
			tokens = line.split(RegExp.TAB);
			if (tokens[tokens.length - 2].equals("NA"))	continue;
			if (tokens[geneIdx + 2].startsWith("NA") || tokens[geneIdx + 2].startsWith("synonymous") || tokens[geneIdx + 2].startsWith("unknown"))	continue;
			
			refCount = (double) Integer.parseInt(tokens[tokens.length - 2]);
			altCount = (double) Integer.parseInt(tokens[tokens.length - 1]);
			hapAtype = Integer.parseInt(tokens[tokens.length - 5]);
			hapBtype = Integer.parseInt(tokens[tokens.length - 4]);
			genesInLine = tokens[geneIdx].split(",");
			for (String gene : genesInLine) {
				if (phasedGeneList.contains(gene)) {
					// initialize countA
					if (geneToRNAcntA.containsKey(gene)) {
						countA = geneToRNAcntA.get(gene);
						count = geneToRNAcnt.get(gene);
					} else {
						countA = new ArrayList<Double>();
						count = 0;
					}
					// initialize countB
					if (geneToRNAcntB.containsKey(gene)) {
						countB = geneToRNAcntB.get(gene);
					} else {
						countB = new ArrayList<Double>();
					}
					
					// add for each haplotype
					if (hapAtype == 0 && hapBtype == 1) {
						countA.add(refCount);
						countB.add(altCount);
						geneToRNAcntA.put(gene, countA);
						geneToRNAcntB.put(gene, countB);
						geneToRNAcnt.put(gene, count + 1);
					} else if (hapAtype == 1 && hapBtype == 0) {
						countA.add(altCount);
						countB.add(refCount);
						geneToRNAcntA.put(gene, countA);
						geneToRNAcntB.put(gene, countB);
						geneToRNAcnt.put(gene, count + 1);
					} else if (hapAtype == 1 && hapBtype == 1) {
						countA.add(altCount);
						countB.add(altCount);
						geneToRNAcntA.put(gene, countA);
						geneToRNAcntB.put(gene, countB);
						geneToRNAcnt.put(gene, count + 1);
					} else {
						System.out.println("[DEBUG] :: " + line);
						System.exit(-1);
					}
				}
			}
		}
		
		System.out.println("Genes with allele specific RNA expression: " + geneToRNAcntA.size());
		
		frGene.reset();
		
		line = frGene.readLine() + "\tRNA_Count_A_Avg\tRNA_Count_B_Avg\tN\tT-Test_P";
		fm.writeLine(line);
		double[] countAarr = null;
		double[] countBarr = null;
		double countAsum;
		double countBsum;
		double p;
		while (frGene.hasMoreLines()) {
			line = frGene.readLine();
			
			tokens = line.split(RegExp.TAB);
			String gene = tokens[PhasedGenes.GENE];
			if (geneToRNAcnt.containsKey(gene)) {
				count = geneToRNAcnt.get(gene);
				countA = geneToRNAcntA.get(gene);
				countB = geneToRNAcntB.get(gene);
				countAsum = 0;
				countBsum = 0;
				if (count > 1) {
					// type conversion from Double[] to double[] - remove this afterwords
					countAarr = new double[countA.size()];
					countBarr = new double[countB.size()];
					for (int i = 0; i < countA.size(); i++) {
						countAarr[i] = countA.get(i);
						countBarr[i] = countB.get(i);
						countAsum += countA.get(i);
						countBsum += countB.get(i);
					}
					p = new TTest().pairedTTest(countAarr, countBarr);
					fm.writeLine(line + "\t" + String.format("%,.2f", ((float) countAsum) / count) +"\t" + String.format("%,.2f", ((float) countBsum) / count) + "\t" + count + "\t" + p);
				} else {
					fm.writeLine(line + "\t" + countA.get(0) + "\t" + countB.get(0) + "\t" + count + "\tNA");
				}
			} else {
				fm.writeLine(line + "\t" + "NA" + "\t" + "NA" + "\t0\t" + "NA");
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar annovarPhasedGenesAddExpAlleleCount.jar <in.phased.genes.het.cnt> <in.avout.allele_cnt> <out.phased.genes.het.cnt> [gene_containing_column_idx]");
		System.out.println("\t<in.phased.genes.het.cnt>: generated with annovarFilterPhasedGenes.jar and fpkm added with txtVlookup.jar");
		System.out.println("\t\tAssumes the first line is header line");
		System.out.println("\t<in.avout.allele.cnt>: allele counts from RNA-seq allele.cnt added with annovarAddAlleleCount.jar.");
		System.out.println("\t\tLast 5 columns are assumed to be: haplotypeA-allele(0:ref, 1:alt), haplotypeB-allele(0:ref, 1:alt), ps, ref allele count and alt allele count");
		System.out.println("\t[gene_containing_column_idx]: 1-based. DEFAULT=8.");
		System.out.println("\t\t*Counts will be added for nonsynonymous SNV snps only. gene_containing_column_idx + 2 will be checked.");
		System.out.println("Arang Rhie, 2015-12-22. arrhie@gmail.com");
	}
	
	private static int geneIdx = 7;

	public static void main(String[] args) {
		if (args.length == 3) {
			new PhasedGenesAddExpAlleleCount().go(args[0], args[1], args[2]);
		} else if (args.length == 4) {
			geneIdx = Integer.parseInt(args[3]) - 1;
			new PhasedGenesAddExpAlleleCount().go(args[0], args[1], args[2]);
		} else {
			new PhasedGenesAddExpAlleleCount().printHelp();
		}
	}

}
