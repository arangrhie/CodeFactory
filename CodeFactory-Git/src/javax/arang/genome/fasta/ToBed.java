package javax.arang.genome.fasta;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToBed extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		
		String line;
		boolean hasNonNBases = false;	// has non-N bases to report
		String ref = "";
		int start = 0;
		int end = 1;
		char base;
		int baseIdx = 0;	// How many bases read so far
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith(">")) {
				if (hasNonNBases) {
					System.out.println(ref + "\t" + start + "\t" + end);
				}
				// extract ref
				line = line.substring(1);
				if (line.contains(" ") || line.contains("\t")) {
					line = line.split(RegExp.WHITESPACE)[0];
				}
				ref = line;

				// initialize variables
				start = 0;
				end = 1;
				hasNonNBases = false;
				baseIdx = 0;
			} else {
				for (int i = 0; i < line.length(); i++) {
					base = line.charAt(i);
					if (base == 'n' || base == 'N') {
						// Meets N base
						if (hasNonNBases) {
							// had non-N bases
							System.out.println(ref + "\t" + start + "\t" + end);
							hasNonNBases = false;
						} else {
							// had N bases
							
						}
					} else {
						// Meets non-N base
						if (hasNonNBases) {
							// continue read
							end++;
						} else {
							// first time to meet non-N base
							start = baseIdx + i;
							end = start + 1;
							hasNonNBases = true;
						}
						
						
					}
				}
				baseIdx += line.length();
			}
			
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaToBed.jar <in.fa>");
		System.out.println("\t<in.fa>: Regular fasta file");
		System.out.println("\t<stdout>: Bed file containing regions of non-N bases");
		System.out.println("Arang Rhie, 2017-03-27. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ToBed().go(args[0]);
		} else {
			new ToBed().printHelp();
		}
	}

}
