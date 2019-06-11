package javax.arang.cov;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToRegionOverXBed extends Rwrapper {

	public static final int CHROM = 0;
	public static final int POS = 1;
	public static int COV = 2;
	
	private static int COV_THREASHOLD = 0;
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String chrom = "";
		String newChrom = "";
		double pos = 0;
		double cov = 0;
		double min = Double.MAX_VALUE;
		double max = -1;
		
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
					System.out.println(chrom + "\t" + String.format("%.0f", start) + "\t" + String.format("%.0f", end) + "\t"
							+ String.format("%.1f", min) + "\t" + String.format("%.1f", max));
				}
				
				isFirst = false;
				
				// initialize
				chrom = newChrom;
				start = - 1;
				end = 0;
			}
			
			// if cov is over COV, start the region
			cov = Double.parseDouble(tokens[COV]);
			if (cov > COV_THREASHOLD) {
				if (end != pos || end == 0) {
					
					if (end != 0) {
						// write down the forma region
						System.out.println(chrom + "\t" + String.format("%.0f", start) + "\t" + String.format("%.0f", end) + "\t"
								+ String.format("%.1f", min) + "\t" + String.format("%.1f", max));
					}
					
					// begin a new region
					start = pos;
					min = cov;
					max = cov;
				}
				if (cov < min) {
					min = cov;
				}
				if (cov > max) {
					max = cov;
				}
				end = pos + 1;
			}
			
		}
		if (!isFirst && end != 0) {
			// write down the region
			System.out.println(chrom + "\t" + String.format("%.0f", start) + "\t" + String.format("%.0f", end) + "\t"
					+ String.format("%.1f", min) + "\t" + String.format("%.1f", max));
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar covToRegionOverXBed.jar <MIN_COV> <in.cov> [cov_idx]");
		System.out.println("Get coverage region in bed format");
		System.out.println("\t<MIN_COV>: Minimum depth to include in the region. DEFAULT=0 (include all > 0x)");
		System.out.println("\t<in.cov>: Chr\tPos [0-based]\tCov(or some values)");
		System.out.println("\t[cov_idx]: Index of cloumn containing the coverage (or some value). 1-based.");
		System.out.println("Arang Rhie, 2019-02-07. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			COV_THREASHOLD = Integer.parseInt(args[0]);
			COV = Integer.parseInt(args[2]) - 1;
			new ToRegionOverXBed().go(args[1]);
		}
		else if (args.length == 2) {
			COV_THREASHOLD = Integer.parseInt(args[0]);
			new ToRegionOverXBed().go(args[1]);
		} else {
			new ToRegionOverXBed().printHelp();
		}
	}
}
