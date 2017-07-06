package javax.arang.juicer;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.sam.SAMUtil;

public class ToBed extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		int start1;
		int end1;
		int start2;
		int end2;
		int dist;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			start1 = Integer.parseInt(tokens[JuicerHiC.POS1]) - 1;
			end1 = start1 + SAMUtil.getMatchedBases(tokens[JuicerHiC.CIGAR1]);
			start2 = Integer.parseInt(tokens[JuicerHiC.POS2]) - 1;
			end2 = start2 + SAMUtil.getMatchedBases(tokens[JuicerHiC.CIGAR2]);
			
			if (tokens[JuicerHiC.CHR1].equals(tokens[JuicerHiC.CHR2])) {
				dist = Math.max(end1, end2) - Math.min(start1, start2);
			} else {
				dist = -1;
			}
			System.out.println(tokens[JuicerHiC.CHR1] + "\t" + start1 + "\t" + end1 + "\t" + tokens[JuicerHiC.READ1] + "\t" + tokens[JuicerHiC.MAPQ1] + "\t" + dist);
			System.out.println(tokens[JuicerHiC.CHR2] + "\t" + start2 + "\t" + end2 + "\t" + tokens[JuicerHiC.READ2] + "\t" + tokens[JuicerHiC.MAPQ2] + "\t" + dist);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usgae: java -jar juicerToBed.jar merged_nodups.txt > <stdout>");
		System.out.println("\tConverts merged_nodups.txt to a bed file, parsing the CIGAR array for mapped region");
		System.out.println("\t<stdout>: Contig\tStart\tEnd\tR1/R2\tMQ\tDist");
		System.out.println("Arang Rhie, 2017-06-08. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ToBed().go(args[0]);
		} else {
			new ToBed().printHelp();
		}
	}

}
