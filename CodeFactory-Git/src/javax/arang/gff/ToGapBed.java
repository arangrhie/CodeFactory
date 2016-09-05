package javax.arang.gff;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToGapBed extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String prevChr = "";
		String chr;
		int start;
		int end = 0;
		String prevTargetContig = "";
		String targetContig;
		boolean isFirst = true;
		boolean skip = false;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#")) {
				continue;
			}
			
			tokens = line.split(RegExp.TAB);
			chr = tokens[GFF.CHR];
			targetContig = GFF.parseField(tokens[GFF.NOTE], "Name");
			start = Integer.parseInt(tokens[GFF.START]);
			
			if (isFirst) {
				prevChr = chr;
				prevTargetContig = targetContig;
				skip = true;
				isFirst = false;
			}
			
			if (!chr.equals(prevChr) || !targetContig.equals(prevTargetContig)) {
				// do nothing
				skip = true;
				prevChr = chr;
				prevTargetContig = targetContig;
				end = Integer.parseInt(tokens[GFF.END]) - 1;
				continue;
			}
			
			if (!skip) {
				if (start >= end) {
					fm.writeLine(chr + "\t" + end + "\t" + start + "\t" + targetContig);
				}
			}
			skip = false;
			prevChr = chr;
			prevTargetContig = targetContig;
			end = Integer.parseInt(tokens[GFF.END]) - 1;
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar gffToGapBed.jar <overchain.block.gff> <out.bed>");
		System.out.println("\tOverchain block to gapped region.");
		System.out.println("\t<overchain.block.gff>: overchain blocked format");
		System.out.println("\t<out.bed>: gap region .bed");
		System.out.println("Arang Rhie, 2016-06-07. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ToGapBed().go(args[0], args[1]);
		} else {
			new ToGapBed().printHelp();
		}
	}

}
