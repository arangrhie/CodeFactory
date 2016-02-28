package javax.arang.genome.fasta;

import java.util.ArrayList;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.IOUtil;

public class ConcatinateFasta extends INOwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaConcatinateFasta.jar <out-seq-name> <num-N-padds> <*.fasta>");
		System.out.println("\t<out-seq-name>: out fasta file name.");
		System.out.println("\t\t<out-seq-name>.fasta will be generated with a header ><out-seq-name>.");
		System.out.println("\t<num-N-padds>: num. of N bases between fasta contigs");
		System.out.println("2015-10-22. arrhie@gmail.com");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		fm.writeLine(">" + seqName);
		String line;
		boolean isFirst = true;
		for (int i = 0; i < frs.size() - 1; i++) {
			while (frs.get(i).hasMoreLines()) {
				line = frs.get(i).readLine();
				if (line.startsWith(">")) {
					if (isFirst) {
						// do nothing
						isFirst = false;
					} else {
						for (int j = 0; j < numNs; j++) {
							fm.write("N");
						}
						fm.writeLine();
					}
				} else {
					fm.writeLine(line);
				}
			}

		}
		while (frs.get(frs.size() - 1).hasMoreLines()) {
			line = frs.get(frs.size() - 1).readLine();
			if (line.startsWith(">")) {
				if (isFirst) {
					// do nothing
					isFirst = false;
				} else {
					for (int j = 0; j < numNs; j++) {
						fm.write("N");
					}
					fm.writeLine();
				}
			} else {
				fm.writeLine(line);
			}
		}
	}
	
	private static String seqName;
	private static int numNs = 0;

	public static void main(String[] args) {
		if (args.length > 2) {
			seqName = IOUtil.retrieveFileName(args[0]);
			numNs = Integer.parseInt(args[1]);
			String[] inFastaList = new String[args.length - 2];
			for (int i = 2; i < args.length; i++) {
				inFastaList[i - 2] = args[i]; 
			}
			new ConcatinateFasta().go(inFastaList, args[0] + ".fa");
		} else {
			new ConcatinateFasta().printHelp();
		}
		
	}

}
