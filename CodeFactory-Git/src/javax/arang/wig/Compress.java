package javax.arang.wig;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class Compress extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String chrom = "";
		int start = 0;
		int span = 0;
		String value = "";
		
		
		/***
		 * Example:
		 * fixedStep chrom=chr10 start=1 step=1
		 * 0.500
		 * 0.500
		 * 0.500
		 * 0.500
		 * 0.500
		 * 
		 * Will become
		 * variableStep chrom=chm13#1#chr10 start=1 span=5
		 * 0.500
		 */
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			
			// header line
			if(tokens[0].equals("fixedStep")) {
				// print if there were any to print
				if (span > 0) {
					System.out.println("variableStep " + chrom + " start=" + start + " span=" + span);
					System.out.println(value);
				}
				
				// initialize variables
				span = 0;
				value = "";
				chrom = tokens[1];
				start = Integer.parseInt(tokens[2].split("=")[1]);
			}			
			else {
				// first value line
				if (span == 0) {
					value = tokens[0];
					span = 1;
				} else {
					// non-first value lines
					if (value.equals(tokens[0])) {
						span++;
					} else {
						// not the same value
						if (span > 0) {
							System.out.println("variableStep " + chrom + " start=" + start + " span=" + span);
							System.out.println(value);
						}
						start += span;
						span = 1;
						value = tokens[0];
					}
				}
			}
		}
		if (span > 0) {
			System.out.println("variableStep " + chrom + " start=" + start + " span=" + span);
			System.out.println(value);
		}
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar wigCompress.jar in.wig");
		System.err.println("Compress fixedStep to variableStep if value is the same");
		System.err.println("  in.wig  fixedStep wiggle format with span=1");
		System.err.println("  stdout  variableStep wiggle format, collapsing region to span");
		System.err.println("Arang Rhie, 2024-08-");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new Compress().go(args[0]);
		} else {
			new Compress().printHelp();
		}
	}

}
