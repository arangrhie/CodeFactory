package javax.arang.vcf;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.bed.util.Bed;

public class InBed extends I2Owrapper {

	@Override
	public void hooker(FileReader frInVcf, FileReader frInBed, FileMaker fm) {
		Bed bed = new Bed(frInBed);
		
		String line;
		String[] tokens;
		String chr;
		String prevChr = "";
		int prevIdx = 0;
		long pos;
		int regionSize = 0;
		
		while (frInVcf.hasMoreLines()) {
			line = frInVcf.readLine();
			if (line.startsWith("#")) {
				fm.writeLine(line);
				continue;
			}
			tokens = line.split("\t");
			chr = tokens[VCF.CHROM];
			pos = Long.parseLong(tokens[VCF.POS]);
			if (chr.equals(prevChr))	{
				prevIdx = checkRegion(fm, prevIdx, regionSize, bed, chr, line, pos);
			} else {
				prevChr = chr;
				prevIdx = 0;
				if (!bed.hasChromosome(chr)) {
					continue;
				}
				regionSize = bed.getStarts(chr).size();
				prevIdx = checkRegion(fm, prevIdx, regionSize, bed, chr, line, pos);
			}
		}
	}
	
	private int checkRegion(FileMaker fm, int idx, int size, Bed bed, String chr, String line, long pos) {
		long[] region;
		for (int i = idx; i < size; i++) {
			region = bed.getRegion(chr, i);
			if (region[Bed.REGION_START] < pos && pos <= region[Bed.REGION_END]) {
				fm.writeLine(line);
				return i;
			}
		}
		return idx;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar vcfInBed.jar <in.vcf> <in.bed> <out.vcf>");
		System.out.println("Selects variants from <in.vcf> in <in.bed>, and prints out on <out.vcf>.");
		System.out.println("\t<in.vcf>: input vcf, with header lines starting with \'#\'");
		System.out.println("\t<in.bed>: Region in bed format");
		System.out.println("\t<out.vcf>: variants in <in.bed> with header lines");
		System.out.println("Arang Rhie, 2014-03-24. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length < 3) {
			new InBed().printHelp();
		} else {
			new InBed().go(args[0], args[1], args[2]);
		}
		
	}

}
