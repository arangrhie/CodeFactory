package javax.arang.genome.fasta;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ExtractRegion extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		String contig = region.substring(0, region.lastIndexOf(":"));
		int start = Integer.parseInt(region.substring(region.lastIndexOf(":") + 1, region.lastIndexOf("-")));
		int end = Integer.parseInt(region.substring(region.lastIndexOf("-") + 1));
		
		Seeker faSeeker = new Seeker(fr);
		if (faSeeker.goToContig(contig)) {
			fm.writeLine(">" + contig + ":" + start + "-" + end);
			fm.writeLine(faSeeker.getBases(start, (end - start)));
		} else {
			System.err.println("[ERROR] :: " + contig + " does not exists. Check the fasta file: " + fr.getFileName());
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaExtractRegion.jar <in.fa> <in.region> <out.fa>");
		System.out.println("\tExtract specificed region out of <in.fa>");
		System.out.println("\t<in.fa>: fasta file");
		System.out.println("\t<in.region>: CHR:START-END in bed format (START:0-based, END:1-based)");
		System.out.println("\t<out.fa>: output fa file, named after <in.region>");
		System.out.println("2016-01-05. arrhie@gmail.com");
	}

	private static String region;
	public static void main(String[] args) {
		if (args.length == 3) {
			region = args[1];
			new ExtractRegion().go(args[0], args[2]);
		} else {
			new ExtractRegion().printHelp();
		}
	}

}
