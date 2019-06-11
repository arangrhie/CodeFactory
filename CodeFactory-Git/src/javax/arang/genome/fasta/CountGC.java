package javax.arang.genome.fasta;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

public class CountGC extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		double gcBases = 0;
		double totalBases = 0;
		boolean hasCounted = false;
		char base;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			// if it's fasta entry
			if (line.startsWith(">")) {
				if (!hasCounted) {
					hasCounted = true;
				} else {
					System.out.println("\t" + String.format("%,.0f", gcBases) + "\t" + String.format("%,.0f", totalBases) + "\t" + String.format("%.2f", ((float) gcBases) / totalBases));
				}
				// initialize counts
				gcBases = 0;
				totalBases = 0;
				
				// begin a new scaffold line
				System.out.print(line.substring(1));
			}
			// count
			else {
				line = line.toLowerCase();
			
				for (int i = 0; i < line.length(); i++) {
					base = line.charAt(i);
					if (base == 'g' || base == 'c' ) {
						gcBases++;
						totalBases++;
					}
					// ignore the N bases
					else if (base != 'n') {
						totalBases++;
					}
				}
			}
		}
		
		System.out.println("\t" + String.format("%,.0f", gcBases) + "\t" + String.format("%,.0f", totalBases) + "\t" + String.format("%.2f", ((float) gcBases) / totalBases));
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaCountGC.jar <in.fasta>");
		System.out.println("Count the number of GC bases vs. total bases.");
		System.out.println("\t<sysout>: FASTA_entry\tGC_bases\ttotal_bases\tGC%");
		System.out.println("Arang Rhie, 2018-08-08.arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new CountGC().go(args[0]);
		} else {
			new CountGC().printHelp();
		}

	}

}
