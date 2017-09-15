package mummer.nucmer;

import java.util.HashMap;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class CoordsToCoverage extends R2wrapper {

	private static final int CONTIG = 0;
	private static final int GENE = 3;
	private static final int START = 1;
	private static final int END = 2;
	
	@Override
	public void hooker(FileReader frCoords, FileReader frRegion) {

		String line;
		String[] tokens;
		
		HashMap<String, Double> quereyCovBp = new HashMap<String, Double>();
		HashMap<String, Double> quereyLen = new HashMap<String, Double>();

		// For region to look up
		String target;
		String querey;
		double start;
		double end;
		
		double tStart;
		double tEnd;
		
		while (frRegion.hasMoreLines()) {
			line = frRegion.readLine();
			tokens = line.split(RegExp.TAB);
			target = tokens[CONTIG];
			querey = tokens[GENE];
			start = Double.parseDouble(tokens[START]);
			end = Double.parseDouble(tokens[END]);
			
			while (frCoords.hasMoreLines()) {
				line = frCoords.readLine();
				tokens = line.split(RegExp.WHITESPACE);
				if (tokens[COORDS.T_NAME].equals(target) && tokens[COORDS.Q_NAME].startsWith(querey)) {
					tStart = Double.parseDouble(tokens[COORDS.T_START]);
					tEnd = Double.parseDouble(tokens[COORDS.T_END]);
					if (start < tStart && tEnd < end) {
						if (!quereyLen.containsKey(tokens[COORDS.Q_NAME])) {
							quereyLen.put(tokens[COORDS.Q_NAME], Double.parseDouble(tokens[COORDS.Q_LEN]));
							quereyCovBp.put(tokens[COORDS.Q_NAME], Double.parseDouble(tokens[COORDS.Q_COV_BP]));
						} else {
							quereyCovBp.put(tokens[COORDS.Q_NAME],
									quereyCovBp.get(tokens[COORDS.Q_NAME])
									+ Double.parseDouble(tokens[COORDS.Q_COV_BP]));
						}
					}
				}
			}
			
			for (String gene : quereyLen.keySet()) {
				System.out.println(gene + "\t" + quereyCovBp.get(gene) + " / "+ quereyLen.get(gene) + "\t" + quereyCovBp.get(gene) / quereyLen.get(gene));
			}
			quereyLen.clear();
			quereyCovBp.clear();
			frCoords.reset();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar coordsToCoverage.jar <in.coords> <in.bed>");
		System.out.println("\t<in.coords>: mummer coords file");
		System.out.println("\t<in.bed>: region of interest, with last 4th column being the prefix for querey name. (startsWith search)");
		System.out.println("\t<stdout>: HLA Haplotype (last column of .coords file)");
		System.out.println("Arang Rhie, 2017-08-01. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new CoordsToCoverage().go(args[0], args[1]);
		} else {
			new CoordsToCoverage().printHelp();
		}
	}

}
