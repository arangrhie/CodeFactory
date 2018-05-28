package javax.arang.bed;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class SummaryByInterval extends Rwrapper {

	private static double interval = 5000000;
	private static boolean extended = true;
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String contig = "";
		
		double outStart = 0;
		double outEnd = interval;
		
		double start;
		double end;
		double size;
		double sum = 0;
		double extendedInterval = interval;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#")) {
				continue;
			}
			
			tokens = line.split(RegExp.TAB);
			
			if (!contig.equals(tokens[Bed.CHROM]) && !contig.equals("")) {
				if (extended) {
					printOut(contig, outStart, outEnd, sum, extendedInterval);
				} else {
					printOut(contig, outStart, outEnd, sum);
				}
				outStart = 0;
				outEnd = interval;
				extendedInterval = interval;
				sum = 0;
			}
			
			contig = tokens[Bed.CHROM];
			start = Double.parseDouble(tokens[Bed.START]);
			end = Double.parseDouble(tokens[Bed.END]);
			size = Double.parseDouble(tokens[4]);
			while (outEnd < start) {
				if (extended) {
					printOut(contig, outStart, outEnd, sum, extendedInterval);
				} else {
					printOut(contig, outStart, outEnd, sum);
				}
				outStart += interval;
				outEnd += interval;
				extendedInterval = interval;
				sum = 0;
			}

			if (outStart <= start && end <= outEnd) {
				if (extended) {
					if (end - start < size) {
						sum += size;
						extendedInterval += size - (end - start);
					} else {
						sum += (end - start);
					}
				} else {
					sum += (end - start);
				}
				
			} else if (outStart <= start && end > outEnd) {
				// Only applicable for Deletions
				sum += (outEnd - start);
				if (extended) {
					printOut(contig, outStart, outEnd, sum, extendedInterval);
					if (end - start < size) {
						sum = size;
						extendedInterval = interval + size - (end - start);
					} else {
						sum = (end - start);
						extendedInterval = interval;
					}
				} else {
					printOut(contig, outStart, outEnd, sum);
					sum += (end - outEnd);
				}
				outStart += interval;
				outEnd += interval;
			} else {
				System.err.println("[DEBUG] :: outStart = " + String.format("%,.0f", outStart) + " start = " + String.format("%,.0f", start) + " end = " + String.format("%,.0f", end) + " outEnd = " + String.format("%,.0f", outEnd));
				System.err.println("Something is wrong here. Stop.");
				System.exit(-1);
			}
		}
		
		if (sum > 0) {
			if (extended) {
				printOut(contig, outStart, outEnd, sum, extendedInterval);
			} else {
				printOut(contig, outStart, outEnd, sum);
			}
		}
		
	}
	
	private void printOut(String contig, double outStart, double outEnd, double sum) {
		System.out.println(contig + "\t" + String.format("%.0f", outStart) + "\t" + String.format("%.0f", outEnd) + "\t" + String.format("%.0f", sum) + "\t" + String.format("%.2f", (100 * sum / interval)));
	}
	
	private void printOut(String contig, double outStart, double outEnd, double sum, double extendedIntverval) {
		System.out.println(contig + "\t" + String.format("%.0f", outStart) + "\t" + String.format("%.0f", outEnd) + "\t" + String.format("%.0f", sum) + "\t" + String.format("%.2f", (100 * sum / extendedIntverval)));
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedSummaryByInterval.jar <in.bed> [INTERVAL=5M] [extended=TRUE]");
		System.out.println("\t<in.bed>: Looks only the first 3 columns - CHR\tSTART\tEND.");
		System.out.println("\t[INTERVAL]: DEFAULT=5M. Collects the total length of the region in the given <in.bed> in each INTERVAL.");
		System.out.println("\t[EXTENDED]: DEFAULT=TRUE. For each interval, add the insertions to the SUM and interval to get the extended %.");
		System.out.println("\t\tUses the 5th column as the size of SV to extend.");
		System.out.println("\t<stdout>: CHR\tSTART\tEND\tSUM\t%");
	}

	private static double toDouble(String num) {
		num = num.toLowerCase();
		if (num.endsWith("m")) {
			num = num.replace("m", "000000");
		} else if (num.endsWith("g")) {
			num = num.replace("g", "000000000");
		} else if (num.endsWith("k")) {
			num = num.replace("k", "000");
		}
		
		return Double.parseDouble(num);
	}
	
	public static void main(String[] args) {
		if (args.length == 1) {
			new SummaryByInterval().go(args[0]);
		} else if (args.length == 2) {
			interval = toDouble(args[1]);
			new SummaryByInterval().go(args[0]);
		} else if (args.length == 3) {
			interval = toDouble(args[1]);
			extended = Boolean.parseBoolean(args[2]);
			new SummaryByInterval().go(args[0]);
		}
		else {
			new SummaryByInterval().printHelp();
		}
	}

}
