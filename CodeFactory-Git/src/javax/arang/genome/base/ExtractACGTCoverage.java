package javax.arang.genome.base;

import java.util.ArrayList;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ExtractACGTCoverage extends INOwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar baseACGTCoverage.jar <chr> <position> <*.bas.chrN>");
		System.out.println("\t<chr> <position>: genomic position of interest. must match the .bas.chrN file.");
		System.out.println("\t<*.bas.chrN>: base file containing [chr\tposition\tref_allele\tA_count\tC_count\tG_count\tT_count\tAVG_QUAL\tTotal_Count]");
		System.out.println("\t<output>: chr_position.cov containing\n" +
				"\t\t[sample\tref_allele\tA_count\tC_count\tG_count\tT_count\tAVG_QUAL\tTotal_Count]");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		
		String line;
		
		for (FileReader fr : frs) {
			String sampleName = fr.getFileName().substring(0, fr.getFileName().indexOf("_"));
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				if (line.startsWith(chr + "\t" + position)) {
					fm.writeLine(sampleName + "\t" + line.replace(chr + "\t" + position + "\t", ""));
					break;
				} else {
					continue;
				}
			}
		}
	}

	static String chr = "";
	static int position = 0;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 4) {
			new ExtractACGTCoverage().printHelp();
		} else {
			chr = args[0];
			position = Integer.parseInt(args[1]);
			String[] inFiles = new String[args.length - 2];
			for (int i = 2; i < args.length; i++) {
				inFiles[i-2] = args[i];
			}
			new ExtractACGTCoverage().go(inFiles, chr + "_" + position + ".cov");
		}
	}

}
