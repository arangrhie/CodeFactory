package javax.arang.snp;

import java.util.HashMap;
import java.util.Vector;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class FilterSomaticVariants extends IOwrapper {

	private static int sampleIdx = SNP.SAMPLE_START;
	private static boolean hasTwoHeaderLines = true;
	private static final String NORM_PREFIX = "N";
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line = fr.readLine();
		String[] tokens = line.split("\t");
		
		if (hasTwoHeaderLines) {
			fm.write(tokens[0]);
			for (int i = 1; i < sampleIdx; i++) {
				fm.write("\t" + tokens[i]);
			}
			
			line = fr.readLine();
			tokens = line.split("\t");
			for (int i = sampleIdx; i < tokens.length; i++) {
				fm.write("\t" + tokens[i]);
			}
			fm.writeLine();
		} else {
			fm.writeLine(line);
		}
		
		// find matched samples
		HashMap<Integer, String> sampleList = new HashMap<Integer, String>();
		Vector<Integer> groupCIdx = new Vector<Integer>();
		Vector<Integer> groupNIdx = new Vector<Integer>();
		for (int i = sampleIdx; i < tokens.length; i++) {
			if (tokens[i].startsWith(NORM_PREFIX)) {
				groupNIdx.add(i);
			} else {
				groupCIdx.add(i);
			}
			if (!sampleList.containsValue(tokens[i].substring(1))) {
				sampleList.put(i, tokens[i].substring(1));
			}
		}
		int numSamples = sampleList.size();
		System.out.println("Total Number of Samples: " + numSamples);
		
		int countSomatics = 0;
		int filteredCount = 0;
		int totalCount = 0;
		
		// Write Mutation Table
		LINE_LOOP : while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			countSomatics = 0;
			
			for (int i = 0; i <numSamples; i++) {
				if (tokens[groupNIdx.get(i)].equals("1") || tokens[groupNIdx.get(i)].equals("2")) {
					// filter out
					filteredCount++;
					continue LINE_LOOP;
				}
				if (tokens[groupCIdx.get(i)].contains("NA") && tokens[groupNIdx.get(i)].contains("NA")) {
					// do nothing
				} else if(tokens[groupCIdx.get(i)].equals(tokens[groupNIdx.get(i)])) {
					tokens[groupCIdx.get(i)] = "-";
					tokens[groupNIdx.get(i)] = "-";
				} else if (tokens[groupCIdx.get(i)].contains("NA")) {
					tokens[groupNIdx.get(i)] = "-";
				} else if (tokens[groupNIdx.get(i)].contains("NA")) {
					tokens[groupCIdx.get(i)] = "-";
				} else {
					countSomatics++;
					
				}
			}
			if (countSomatics == 0) {
				filteredCount++;
				continue LINE_LOOP;
			}
			fm.write(tokens[SNP.CHR]);
			for (int i = 1; i < tokens.length; i++) {
				fm.write("\t" + tokens[i]);
			}
			fm.writeLine();
			totalCount++;
		}
		
		System.out.println(filteredCount + " sites has been filtered out with no somatic mutations.");
		System.out.println("Total Number of mutations: " + totalCount);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar snpFilterSomaticVariants.jar <in.txt> [sample_idx] [has_2_headers]");
		System.out.println("Mask variants to - where one of paired C / N is missing, or paired C / N have the same genotype.");
		System.out.println("\t<in.txt>: Annovar input formatted variation table.");
		System.out.println("\t<has_2_headers>: has 2 header lines. DEFAULT = TRUE");
		System.out.println("\t<sample_idx>: Starting idx of sample column. 0-based.");
		System.out.println("\t\tPrefix \'C\' and \'N\' will be used to find pairs.");
		System.out.println("\t\tSamples starting with N will be counted as paired normal samples,");
		System.out.println("\t\t\tand mustations appearing in normal samples will be discarded.");
		System.out.println("Arang Rhie, 2014-11-21. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new FilterSomaticVariants().go(args[0], args[0].replace(".txt", "_somatic.txt"));
		} else if (args.length == 2) {
			sampleIdx = Integer.parseInt(args[1]);
			new FilterSomaticVariants().go(args[0], args[0].replace(".txt", "_somatic.txt"));
		} else if (args.length == 3) {
			sampleIdx = Integer.parseInt(args[1]);
			hasTwoHeaderLines = Boolean.parseBoolean(args[2]);
			new FilterSomaticVariants().go(args[0], args[0].replace(".txt", "_somatic.txt"));
		} else {
			new FilterSomaticVariants().printHelp();
		}
	}

}
