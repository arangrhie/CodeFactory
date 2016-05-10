package javax.arang.annovar;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.vcf.VCF;

public class ConvertVCFToGenotype extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		// Start reading
		String line = fr.readLine();

		// Copy header line
		fm.writeLine(line);
		
		String[] tokens = line.split(RegExp.TAB);
		int colSampleStart = tokens.length - numSamples;
		int colAlt = colSampleStart - 5;
		int numMulti = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			if (tokens[colAlt].contains(",")) {
				numMulti++;
				continue;
			}
			fm.write(tokens[0]);
			for (int i = 1; i < colSampleStart; i++) {
				fm.write(RegExp.TAB + tokens[i]);
			}
			
			for (int i = 0; i < numSamples; i++) {
				fm.write(RegExp.TAB + VCF.parseGT(tokens[colSampleStart - 1], tokens[colSampleStart + i]));
			}
			fm.writeLine();
			
		}
		System.out.println("Num. of multi-allic sites discarded: " + numMulti);
	}
	

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar annovarConvertVCFToGenotype.jar <in.avout> <out.avout.gt> [num_samples]");
		System.out.println("\tConvert OtherInfo field's genotype into number of altered alleles: 0, 1, 2, and 3 (other).");
		System.out.println("\t<in.avout>: annotated with ANNOVAR using convert2annovar.pl --format vcf4 -allsample -withfreq --includeinfo");
		System.out.println("\t\tModify the header line to match the number of columns with sample id.");
		System.out.println("\t<out.avout.gt>: replace the SAMPLE column GT:..:..:..:.. into 0 / 1 / 2 and 3.");
		System.out.println("\t\tMultiallelic sites will be discarded.");
		System.out.println("\t[num_samples]: DEFAULT=1. num_samples columns from the end will be replaced.");
		System.out.println("Arang Rhie, 2016-05-07. arrhie@gmail.com");
	}

	public static int numSamples = 1;
	public static void main(String[] args) {
		if (args.length == 2) {
			new ConvertVCFToGenotype().go(args[0], args[1]);
		} else if (args.length == 3) {
			numSamples = Integer.parseInt(args[2]);
			new ConvertVCFToGenotype().go(args[0], args[1]);
		} else {
			new ConvertVCFToGenotype().printHelp();
		}
	}

}
