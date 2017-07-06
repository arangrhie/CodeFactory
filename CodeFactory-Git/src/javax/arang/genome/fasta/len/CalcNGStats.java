package javax.arang.genome.fasta.len;

import java.util.ArrayList;
import java.util.Collections;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class CalcNGStats extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		ArrayList<Integer> lenList = new ArrayList<Integer>();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			lenList.add(Integer.parseInt(tokens[tokens.length - lenIdx]));
		}
		
		Collections.sort(lenList);
		int sum = 0;
		
		ArrayList<Double> ngVal = new ArrayList<Double>();
		for (int i = 5; i <= 100; i += 5) {
			//System.err.println("NG" + i + ": " + String.format("%,.0f", (genomeSize / 100) * i));
			ngVal.add((genomeSize / 100) * i);
		}
		
		int ngValIdx = 0;
		System.out.println("NG\tSize\tIdx\tSum\tNGVal");
		String ng;
		String ng_list = "";
		int i;
		for (int j = 0; j < lenList.size(); j++) {
			i = lenList.size() - j - 1;
			sum += lenList.get(i);
			while (ngValIdx < ngVal.size() && sum >= ngVal.get(ngValIdx)) {
				ng=String.format("%,d", lenList.get(i));
				System.out.println("NG" + (ngValIdx + 1) * 5 + "\t" + String.format("%,d", lenList.get(i)) + "\t" + (j + 1) + "\t" + String.format("%,d", sum) + "\t" + String.format("%,.0f", ngVal.get(ngValIdx)));
				ngValIdx++;
				ng_list += ng + "\t";
				if (ngValIdx == ngVal.size()) {
					break;
				}
			}
		}
		
		System.out.println();
		System.out.println("Total num. bases:\t" + String.format("%,d", sum));
		System.out.println("Total num. contigs (scaffolds):\t" + String.format("%,d", lenList.size()));
		System.out.println("Max contig (scaffold) size:\t" + String.format("%,d", lenList.get(lenList.size() - 1)));
		
		System.out.println();
		System.out.println("1-line Summary");
		System.out.println("TotalBP\tNum.Contigs(Scaffolds)\tMax\tNG5\tNG10\tNG15\tNG20\tNG25\tNG30\tNG35\tNG40\tNG45\tNG50\tNG55\tNG60\tNG65\tNG70\tNG75\tNG80\tNG85\tNG90\tNG95\tNG100");
		System.out.println(String.format("%,d", sum) + "\t" + String.format("%,d", lenList.size()) + "\t" + String.format("%,d", lenList.get(lenList.size() - 1)) + "\t" + ng_list);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar lenCalcNGStats.jar <in.fasta.len> <genome_size> [lenIdx]");
		System.out.println("\t<in.fasta.len>: generated with fastaContigSize.jar");
		System.out.println("\t<genome_size>: ex. 3.2G or 1.1G. Either in numbers or with k, m, g notation.");
		System.out.println("\t\tMaximum genome size: " + Double.MAX_VALUE);
		System.out.println("\t[lenIdx]: DEFAULT=2. 1-based. Column index to look up, starting from the END.");
		System.out.println("Arang Rhie, 2017-05-02. arrhie@gmail.com");
		
	}

	private static double genomeSize;
	private static int lenIdx = 2;
	
	private static double getGenomeSize(String inSize) {
		float genomeSize = Float.parseFloat(inSize.substring(0, inSize.length() - 1));
		inSize = inSize.toLowerCase();
		if (inSize.endsWith("k")) {
			genomeSize *= 1000;
		} else if (inSize.endsWith("m")) {
			genomeSize *= 1000000;
		} else if (inSize.endsWith("g")) {
			genomeSize *= 1000000000;
		} else {
			genomeSize *= 10;
			genomeSize += Integer.parseInt(inSize.substring(inSize.length() - 1));
		}
		return genomeSize;
	}
	
	public static void main(String[] args) {
		if (args.length == 2) {
			genomeSize = getGenomeSize(args[1]);
			System.err.println("Genome Size: " + String.format("%,d", (long) genomeSize) + " bp");
			new CalcNGStats().go(args[0]);
		} else if (args.length == 3) {
			genomeSize = getGenomeSize(args[1]);
			System.err.println("Genome Size: " + String.format("%,d", (long) genomeSize) + " bp");
			lenIdx = Integer.parseInt(args[2]);
			new CalcNGStats().go(args[0]);
		} else {
			new CalcNGStats().printHelp();
		}
	}

}
