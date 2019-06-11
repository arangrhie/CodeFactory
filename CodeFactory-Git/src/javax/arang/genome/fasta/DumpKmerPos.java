package javax.arang.genome.fasta;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

public class DumpKmerPos extends Rwrapper {

	private static int kSize = 18;
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		boolean hasHeader = true;
		double pos = 1;
		int i;
		String buffer = null;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith(">")) {
				System.err.println("Start reading " + line);
				if (!hasHeader) {
					System.err.println("Second fasta entry detected. Stop reading.");
					System.err.println("[ERROR] :: Only the first fasta entry is dumped. Terminating...");
					System.exit(0);
				}
				hasHeader = false;
				pos = 1;
				buffer = null;
			} else {
				// adding buffered bases if we have
				if (buffer != null) {
					line = buffer + line;
				}
				
				if (line.length() < kSize) {
					// skip
					buffer = line;
					continue;
				}
				
				// reading the first line
				for (i = 0; i < line.length() - kSize + 1; i++) {
					System.out.println(line.substring(i, i + kSize) + "\t" + String.format("%.0f", pos));
					pos++;
				}
				
				// adding the last k-1 bases to the buffer
				buffer = line.substring(i, line.length());
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaDumpKmerPos.jar <k-size> <fasta>");
		System.out.println("Dump k-mers with position as values.");
		System.out.println("\t<k-size>: size of k-mer collected");
		System.out.println("\t<fasta>: Single-entry fasta.");
		System.out.println("\t<sysout>: kmer\tposition");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			kSize = Integer.parseInt(args[0]);
			new DumpKmerPos().go(args[1]);
		} else {
			new DumpKmerPos().printHelp();
		}
	}

}
