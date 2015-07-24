package javax.arang.bam;

import javax.arang.IO.bam.BamBaiFileRwrapper;
import javax.arang.IO.bambasic.BamReader;
import javax.arang.IO.basic.FileReader;
import javax.arang.bam.util.Bai;

public class CountReadsOverlappingBed extends BamBaiFileRwrapper {

	public static void main(String[] args) {
		if (args.length == 3) { 
			new CountReadsOverlappingBed().go(args[0], args[1]);
		} else {
			new CountReadsOverlappingBed().printHelp();
		}
	}

	@Override
	public void hooker(BamReader bamFr, Bai bai, FileReader fileFr) {
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bamCountReadsOverlappingBed.jar <in.bam> <in.bed> <out.bed>");
		System.out.println("\t<out.bed>: <in.bed> with number of overlapping reads counted in the last column");
		System.out.println("Arang Rhie, 2015-05-07. arrhie@gmail.com");
	}

}
