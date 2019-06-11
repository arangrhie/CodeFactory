package javax.arang.cov;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToRegionBed extends Rwrapper {

	public static final int CHROM = 0;
	public static final int POS = 1;
	public static final int COV = 2;
	
	private static int COV_THREASHOLD = 200;
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String chrom = "";
		String newChrom = "";
		double pos = 0;
		int cov = 0;
		
		double start = -1;
		double end = -1;
		
		boolean isFirst = true;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			newChrom = tokens[CHROM];
			pos = Double.parseDouble(tokens[POS]);
						
			if (!chrom.equals(newChrom)) {
				if (!isFirst) {
					// write down the region
					System.out.println(chrom + "\t" + String.format("%.0f", start) + "\t" + String.format("%.0f", end));
				}
				
				isFirst = false;
				
				// initialize
				chrom = newChrom;
				start = - 1;
				end = 0;
			}
			
			// if cov is over COV, start the region
			cov = Integer.parseInt(tokens[COV]);
			if (cov < COV_THREASHOLD) {
				if (end + 1 != pos || end == 0) {
					
					if (end != 0) {
						// write down the forma region
						System.out.println(chrom + "\t" + String.format("%.0f", start) + "\t" + String.format("%.0f", end));
					}
					
					// begin a new region
					start = pos - 1;
				}
				end = pos;
			}
			
		}
		if (!isFirst && end != 0) {
			// write down the region
			System.out.println(chrom + "\t" + String.format("%.0f", start) + "\t" + String.format("%.0f", end));
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar covToRegionBed.jar <MAX_COV> <in.cov>");
		System.out.println("Get coverage region in bed format");
		System.out.println("\t<MAX_COV>: Maximum depth to include in the region. Suggested default = 200");
		System.out.println("\t<in.cov>: bedtools genomecov -d -ibam <bam>");
		System.out.println("Arang Rhie, 2019-01-09. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			COV_THREASHOLD = Integer.parseInt(args[0]);
			new ToRegionBed().go(args[1]);
		} else {
			new ToRegionBed().printHelp();
		}
	}
}
