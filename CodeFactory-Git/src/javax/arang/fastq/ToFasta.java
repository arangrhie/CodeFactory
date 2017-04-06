package javax.arang.fastq;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

public class ToFasta extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		boolean isRead = false;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (Fastq.isReadID(line)) {
				System.out.println(">" + line.substring(1));
				isRead = true;
			} else if (Fastq.isQualSep(line)) {
				isRead = false;
			} else if (isRead) {
				System.out.println(line);
			} else {
				// do nothing
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastqToFasta.jar <in.fastq> > out.fasta");
		System.out.println("\tPrints out fastq reads without the quality.");
		System.out.println("\t*Designed for running blasr*");
		System.out.println("Arang Rhie, 2017-02-06. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ToFasta().go(args[0]);
		} else {
			new ToFasta().printHelp();
		}
	}

}
