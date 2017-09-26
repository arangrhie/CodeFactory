package javax.arang.phasing.snp;

import java.util.ArrayList;
import java.util.Collections;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.genome.util.Util;
import javax.arang.phasing.util.PhasedSNP;

public class AverageDistance extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		ArrayList<Double> distList = new ArrayList<Double>();
		
		int prevPos;
		int pos;
		double dist;
		double totalLen = 0;
		line = fr.readLine();
		tokens = line.split(RegExp.TAB);
		prevPos = Integer.parseInt(tokens[PhasedSNP.POS]);
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			pos = Integer.parseInt(tokens[PhasedSNP.POS]);
			dist = (pos - prevPos);
			distList.add(dist);
			totalLen += dist;
			prevPos = pos;
		}
		
		Collections.sort(distList);
		double n50 = Util.getN50(distList, totalLen);
		System.out.println("average: " + (totalLen / distList.size()));
		System.out.println("n50: " + n50);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingSnpAverageDistance.jar <in.snp>");
		System.out.println("Average distance between SNPs (or certain positions)");
		System.out.println("\t<in.snp>: CHR\tPOS. must be sorted by position, per chromosome.");
		System.out.println("\tOut: Average distance");
		System.out.println("Arang Rhie, 2015-09-16. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new AverageDistance().go(args[0]);
		} else {
			new AverageDistance().printHelp();
		}
	}

}
