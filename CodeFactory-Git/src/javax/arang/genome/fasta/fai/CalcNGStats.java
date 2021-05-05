package javax.arang.genome.fasta.fai;

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
		
		ArrayList<Double> lenList = new ArrayList<Double>();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			lenList.add(Double.parseDouble(tokens[lenIdx]));
		}
		
		Collections.sort(lenList);
		double sum = 0;
		
		ArrayList<Double> nVal = new ArrayList<Double>();
		for (int i = 5; i <= 100; i += 5) {
			nVal.add((genomeSize / 100) * i);
		}
		
		int nValIdx = 0;
		System.err.println();
		System.err.println("NG-XX values");
		System.err.println("NG\tSize\tIdx\tSum\tNGVal");
		String n;
		String n_list = "";
		String l_list = "";
		int i;
		for (int j = 0; j < lenList.size(); j++) {
			i = lenList.size() - j - 1;
			sum += lenList.get(i);
			while (nValIdx < nVal.size() && sum >= nVal.get(nValIdx)) {
				n=String.format("%,.0f", lenList.get(i));
				System.err.println("NG" + (nValIdx + 1) * 5 + "\t" + String.format("%,.0f", lenList.get(i)) + "\t" + (j + 1) + "\t" + String.format("%,.0f", sum) + "\t" + String.format("%,.0f", nVal.get(nValIdx)));
				nValIdx++;
				n_list += n + "\t";
				l_list += (j + 1) + "\t";
				if (nValIdx == nVal.size()) {
					break;
				}
			}
		}
		
		System.err.println();
		System.err.println("Total num. bases:\t" + String.format("%,.0f", sum));
		System.err.println("Total num. contigs (scaffolds):\t" + String.format("%,d", lenList.size()));
		System.err.println("Max contig (scaffold) size:\t" + String.format("%,.0f", lenList.get(lenList.size() - 1)));
		System.err.println();
		
		if (printHeader) {
			System.out.println("Assembly\tTotal\tMax\tNG5\tNG10\tNG15\tNG20\tNG25\tNG30\tNG35\tNG40\tNG45\tNG50\tNG55\tNG60\tNG65\tNG70\tNG75\tNG80\tNG85\tNG90\tNG95\tNG100");
		}
		System.out.println(fr.getFileName() + "_NG-xx" + "\t" + String.format("%,.0f", sum) + "\t" + String.format("%,.0f", lenList.get(lenList.size() - 1)) + "\t" + n_list);
		System.out.println(fr.getFileName() + "_LG-xx" + "\t" + String.format("%,d", lenList.size()) + "\t1\t" + l_list);
		
		double totalBp = sum;
		System.err.println();
		System.err.println("N-XX values");
		System.err.println("N\tSize\tIdx\tSum\tNVal");
		nVal.clear();
		for (i = 5; i <= 100; i += 5) {
			//System.err.println("NG" + i + ": " + String.format("%,.0f", (genomeSize / 100) * i));
			nVal.add((totalBp / 100) * i);
		}
		nValIdx = 0;

		sum = 0;
		n_list = "";
		l_list = "";
		for (int j = 0; j < lenList.size(); j++) {
			i = lenList.size() - j - 1;
			sum += lenList.get(i);
			while (nValIdx < nVal.size() && sum >= nVal.get(nValIdx)) {
				n=String.format("%,.0f", lenList.get(i));
				System.err.println("N" + (nValIdx + 1) * 5 + "\t" + String.format("%,.0f", lenList.get(i)) + "\t" + (j + 1) + "\t" + String.format("%,.0f", sum) + "\t" + String.format("%,.0f", nVal.get(nValIdx)));
				nValIdx++;
				n_list += n + "\t";
				l_list += (j + 1) + "\t";
				if (nValIdx == nVal.size()) {
					break;
				}
			}
		}
		
		if (printHeader) {
			System.out.println("Assembly\tTotal\tMax\tN5\tN10\tN15\tN20\tN25\tN30\tN35\tN40\tN45\tN50\tN55\tN60\tN65\tN70\tN75\tN80\tN85\tN90\tN95\tN100");
		}
		System.out.println(fr.getFileName() + "_N-xx" + "\t" + String.format("%,.0f", sum) + "\t" + String.format("%,.0f", lenList.get(lenList.size() - 1)) + "\t" + n_list);
		System.out.println(fr.getFileName() + "_L-xx" + "\t" + String.format("%,d", lenList.size()) + "\t1\t" + l_list);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar faiCalcNGStats.jar <in.fasta.fai> <genome_size> [print_header=F]");
		System.out.println("Print the NG values and N values in stdout.");
		System.out.println("\t<in.fasta.fai>: generated with samtools faidx");
		System.out.println("\t<genome_size>: numbers. (deprecated to use g/m/k notations)");
		System.out.println("\t\tMaximum genome size: " + Double.MAX_VALUE);
		System.out.println("\t[print_header]: DEFAULT=FALSE. Set to TRUE to print headers before the 1-line summary.");
		System.out.println("\t<stderr>: Verbal summary, printed nicely");
		System.out.println("\t<stdout>: 1-line summaries of the NG-xx, LG-xx, N-xx, and L-xx values. Easier to parse in bulk");
		System.out.println("Arang Rhie, 2020-11-08. arrhie@gmail.com");
	}

	private static double genomeSize;
	private static int lenIdx = 1;
	private static boolean printHeader = false;
	
	private static double getGenomeSize(String inSize) {
		inSize = inSize.toLowerCase().replace(",", "");
		double genomeSize = Double.parseDouble(inSize.substring(0, inSize.length() - 1));
		if (inSize.endsWith("k")) {
			genomeSize *= 1000;
		} else if (inSize.endsWith("m")) {
			genomeSize = genomeSize * 1000000;
		} else if (inSize.endsWith("g")) {
			genomeSize = genomeSize * 1000000000;
		} else {
			genomeSize = Double.parseDouble(inSize);
		}
		return genomeSize;
	}
	
	public static void main(String[] args) {
		if (args.length == 2 || args.length == 3) {
			genomeSize = getGenomeSize(args[1]);
			System.err.println("Genome Size: " + String.format("%,.0f", genomeSize) + " bp");
			if (args.length == 3) {
				printHeader = Boolean.parseBoolean(args[2]);
			}
			new CalcNGStats().go(args[0]);
		} else {
			new CalcNGStats().printHelp();
		}
	}

}
