package mummer.nucmer;

import java.util.HashMap;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class SnpHaplotypeHLA extends R2wrapper {

	private static final int GENE = 0;
	private static final int START = 1;
	private static final int END = 2;

	@Override
	public void hooker(FileReader frSnp, FileReader frBed) {
		String line;
		String[] tokens;

		HashMap<String, Integer> geneDistance = new HashMap<String, Integer>();

		String gene;
		int start;
		int end;
		
		String obs;
		int pos;
		
		String snpLine;
		
		while (frBed.hasMoreLines()) {
			line = frBed.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			gene = tokens[GENE] + "*";
			start = Integer.parseInt(tokens[START]);
			end = Integer.parseInt(tokens[END]);
			
			while (frSnp.hasMoreLines()) {
				snpLine = frSnp.readLine();
				tokens = snpLine.trim().split(RegExp.WHITESPACE);
				pos = Integer.parseInt(tokens[0]);
				obs = tokens[tokens.length - 1];
				if (obs.startsWith(gene) && start < pos && pos < end) {
					if (geneDistance.containsKey(obs)) {
						geneDistance.put(obs, geneDistance.get(obs) + 1);
					} else {
						geneDistance.put(obs, 1);
					}
				}
			}
			frSnp.reset();
			for (String observed : geneDistance.keySet()) {
				System.out.println(observed + "\t" + geneDistance.get(observed));
			}
			geneDistance.clear();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar snpHaplotypeHLA.jar <in.nucmer.snp> <hla.bed>");
		System.out.println("\t<in.nucmer.snp>: nucmer alignemnt, show-snps with -H option");
		System.out.println("\t<hla.bed>: ex. A\t3104759\t3108452 where the coords are of the given reference");
		System.out.println("\t<stdout>: hla_gene\tedit_distance");
		System.out.println("\tFilters the <in.nucmer.snp> according to the <hla.bed>, and reports the edit distances.");
		System.out.println("\t\tFirst, last clomns are only considered.");
		System.out.println("Arang Rhie, 2017-08-01. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new SnpHaplotypeHLA().go(args[0], args[1]);
		} else {
			new SnpHaplotypeHLA().printHelp();
		}
	}

}
