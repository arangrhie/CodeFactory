package javax.arang.genome.fasta;

import java.util.ArrayList;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.IOUtil;

public class ConcatinateFasta extends INOwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaConcatinate.jar <out-seq-name> <num-N-padds> <*.fasta>");
		System.out.println("\t<out-seq-name>: out fasta file name.");
		System.out.println("\t\t<out-seq-name>.fa will be generated with a header ><out-seq-name>.");
		System.out.println("\t<num-N-padds>: num. of N bases between fasta contigs");
		System.out.println("\t<out-seq-name>.bed: bed formatted file containing the merged fasta region within the final <out-seq-name>.fa");
		System.out.println("2017-03-22. arrhie@gmail.com");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		fm.writeLine(">" + seqName);
		FileMaker fmBed = new FileMaker(fm.getDir(), fm.getFileName().replace(".fa", ".bed"));
		int start = 0;
		int end = 0;
		String mergedSeqName = "";
		
		String line;
		boolean isFirst = true;
		for (int i = 0; i < frs.size(); i++) {
			while (frs.get(i).hasMoreLines()) {
				line = frs.get(i).readLine();
				if (line.startsWith(">")) {
					if (isFirst) {
						isFirst = false;
					} else {
						fmBed.writeLine(seqName + "\t" + start + "\t" + end + "\t" + mergedSeqName);
						end += numNs;
						start = end;
						for (int j = 0; j < numNs; j++) {
							fm.write("N");
						}
						fm.writeLine();
					}
					mergedSeqName = line.substring(1);
				} else {
					fm.writeLine(line);
					end += line.length();
				}
			}
		}
		fmBed.writeLine(seqName + "\t" + start + "\t" + end + "\t" + mergedSeqName);
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
