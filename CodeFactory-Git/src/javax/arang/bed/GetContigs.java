package javax.arang.bed;

import java.util.HashMap;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class GetContigs extends R2wrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2) {
		String[] tokens;
		String seqid;
		Double seqlen;
		
		HashMap<String, Double> scaffoldLen = new HashMap<String, Double>();
		while (fr1.hasMoreLines()) {
			tokens = fr1.readLine().split(RegExp.TAB);
			seqid = tokens[0];
			seqlen = Double.parseDouble(tokens[1]);
			scaffoldLen.put(seqid, seqlen);
		}
		
		String prevSeq = "";
		Double start = (double) 0;
		Double end = (double) 0;
		
		while (fr2.hasMoreLines()) {
			tokens = fr2.readLine().split(RegExp.TAB);
			seqid = tokens[Bed.CHROM];
			start = Double.parseDouble(tokens[Bed.START]);
			end = Double.parseDouble(tokens[Bed.END]);
			if (!prevSeq.equals(seqid)) {
				if (!prevSeq.equals("")) {
					System.out.println("\t" + String.format("%.0f", scaffoldLen.get(prevSeq)));
				}
				System.out.print(seqid + "\t0");
			}
			System.out.println("\t" + String.format("%.0f", start));
			System.out.print(seqid + "\t" + String.format("%.0f", end));
			prevSeq = seqid;
		}
		System.out.println("\t" + String.format("%.0f", scaffoldLen.get(prevSeq)));
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedGetContigs.jar <fasta.len> <fasta.gaps>");
		System.out.println("\t<fasta.len>: generated with fastaContigSize.jar");
		System.out.println("\t<fasta.gaps>: generated with fastaGetGaps.jar");
		System.out.println("\t<stdout>: contigs.bed");
		System.out.println("Arang Rhie, 2018-09-08. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new GetContigs().go(args[0], args[1]);
		} else {
			new GetContigs().printHelp();
		}
	}

}
