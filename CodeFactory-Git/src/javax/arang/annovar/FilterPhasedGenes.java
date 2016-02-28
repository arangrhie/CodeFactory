package javax.arang.annovar;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class FilterPhasedGenes extends IOwrapper {

	HashMap<String, Integer[]> geneToCountMap = new HashMap<String, Integer[]>();
	HashMap<String, String> geneToPS = new HashMap<String, String>();
	ArrayList<String> blackList = new ArrayList<String>();

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String prevGene = "";
		String gene;
		String startPS = "";
		String ps;
		String exonic;
		
		int countA = 0;
		int countB = 0;
		
		
		Integer[] counts;
		boolean isFirst = true;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			gene = tokens[geneNameIdx];
			ps = tokens[tokens.length - 1];
			exonic = tokens[exonicIdx];
			
			if (isFirst) {
				isFirst = false;
				// initialize variables
				prevGene = gene;
				startPS = ps;
				countA = Integer.parseInt(tokens[tokens.length - 3]);
				countB = Integer.parseInt(tokens[tokens.length - 2]);
				addToMap(exonic, countA, countB, prevGene, startPS);
				continue;
			}
			if (!gene.equals(prevGene) && startPS.equals(ps)) {
				// initialize variables
				startPS = ps;
				countA = Integer.parseInt(tokens[tokens.length - 3]);
				countB = Integer.parseInt(tokens[tokens.length - 2]);
				addToMap(exonic, countA, countB, gene, ps);
			} else if (gene.equals(prevGene) && !startPS.equals(ps)) {
				// skip this gene
				if (!blackList.contains(gene)) {
					blackList.add(gene);
					if (geneToCountMap.containsKey(gene)) {
						geneToCountMap.remove(gene);
					}
				}
				// do nothing
				// initialize variables
				startPS = ps;
			} else if (gene.equals(prevGene) && startPS.equals(ps)) {
				if (blackList.contains(gene)) {
					// do nothing
					
				} else {
					// record other things
					countA = Integer.parseInt(tokens[tokens.length - 3]);
					countB = Integer.parseInt(tokens[tokens.length - 2]);
					addToMap(exonic, countA, countB, gene, ps);
				}
			} else if (!gene.equals(prevGene) && !startPS.equals(ps)) {
				// new beginning
				// initialize variables
				startPS = ps;
				countA = Integer.parseInt(tokens[tokens.length - 3]);
				countB = Integer.parseInt(tokens[tokens.length - 2]);
				addToMap(exonic, countA, countB, gene, startPS);
			}
			prevGene = gene;
		}
		
		for (String geneItem : geneToCountMap.keySet()) {
			counts = geneToCountMap.get(geneItem);
			fm.writeLine(geneItem + "\t" + counts[0] + "\t" + counts[1] + "\t" + counts[2] + "\t" + counts[3] + "\t" + geneToPS.get(geneItem));
		}
	}

	public static Integer[] initCounts(int countA, int countB, int countFuncA, int countFuncB) {
		Integer[] counts = new Integer[4];
		counts[0] = countA;
		counts[1] = countB;
		counts[2] = countFuncA;
		counts[3] = countFuncB;
		return counts;
	}
	
	public static Integer[] addCounts(Integer[] counts, int countA, int countB, int countFuncA, int countFuncB) {
		counts[0] += countA;
		counts[1] += countB;
		counts[2] += countFuncA;
		counts[3] += countFuncB;
		return counts;
	}
	
	private void addToMap(String exonic, int countA, int countB, String prevGene, String startPS) {
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
		
		for (String geneItem : prevGene.split(",")) {
			if (blackList.contains(geneItem))	continue;
			if (geneToCountMap.containsKey(geneItem)) {
				counts = geneToCountMap.get(geneItem);
				counts = addCounts(counts, countA, countB, countFuncA, countFuncB);
			} else {
				counts = initCounts(countA, countB, countFuncA, countFuncB);
			}
			if (geneToPS.containsKey(geneItem)) {
				if (geneToPS.get(geneItem).equals(startPS)) {
					geneToCountMap.put(geneItem, counts);
				} else {
					geneToCountMap.remove(geneItem);
					geneToPS.remove(geneItem);
					blackList.add(geneItem);
				}
			} else {
				geneToPS.put(geneItem, startPS);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar annovarFilterPhasedGenes.jar <in.avout> <out.avout.filt> <gene_name_idx> <exonic_func_idx>");
		System.out.println("\t<in.avout>: Annovar annotated variants, generated with table_annovar.pl and annovarAnnotateHetPS.jar");
		System.out.println("\t<out.avout.filt>: Genes with beginning / ending position within a PS block");
		System.out.println("\t\tGENE_NAME\tA\tB\tAA.A\tAA.B");
		System.out.println("\t<gene_name_idx>: 1-based, column index containing gene name identifier");
		System.out.println("\t<exonic_func_idx>: 1-based, column index containing exonic function; ex. nonsynonymous SNV, stopgain, etc.");
		System.out.println("2015-12-21. arrhie@gmail.com");
	}

	private static int geneNameIdx = 6;
	private static int exonicIdx = 6;
	public static void main(String[] args) {
		if (args.length == 4) {
			geneNameIdx = Integer.parseInt(args[2]) - 1;
			exonicIdx = Integer.parseInt(args[3]) - 1;
			new FilterPhasedGenes().go(args[0], args[1]);
		} else {
			new FilterPhasedGenes().printHelp();
		}
	}

}
