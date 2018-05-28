package javax.arang.sam;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ClassifyRead extends Rwrapper {

	private static String prefix1;
	private static String prefix2;
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String[] altAlignment;
		String ref;
		int flag;
		String score;
		
		String[] tags;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			flag = Integer.parseInt(tokens[Sam.FLAG]);
			if (SAMUtil.isSecondaryAlignment(flag) || SAMUtil.isSupplementary(flag)) {
				continue;
			}
			
			System.out.print(tokens[Sam.QNAME] + "\t");
			
			score = "\t" + 0;
			
			if (SAMUtil.isUnmapped(flag)) {
				System.out.println("NA" + score);
			} else {
				if (tokens[Sam.RNAME].startsWith(prefix1)) {
					ref = prefix1;
				} else if (tokens[Sam.RNAME].startsWith(prefix2)) {
					ref = prefix2;
				} else {
					ref = tokens[Sam.RNAME];
				}
				
				tags = line.substring(line.indexOf("NM:")).split(RegExp.WHITESPACE);
				score = "\t" + (SAMUtil.getMappedBases(tokens[Sam.CIGAR]) - Integer.parseInt(SAMUtil.parseTag(tags[0])));
				
				if (line.contains("SA")) {
					System.out.println("Chim" + score);
				} else if (line.contains("XA")) {
					line = line.substring(line.indexOf("XA") + 5);
					altAlignment = line.split(";");
					if (altAlignment.length > 1) {
						System.out.println("Amb" + score);
					} else if (tokens[Sam.RNAME].startsWith(prefix1) && altAlignment[0].startsWith(prefix2)
							|| tokens[Sam.RNAME].startsWith(prefix2) && altAlignment[0].startsWith(prefix1)) {
						System.out.println("Hom" + score);
					} else {
						System.out.println(ref + score);
					}
				} else {
					System.out.println(ref + score);
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samClassifyRead.jar <in.sam> <prefix_1> <prefix_2>");
		System.out.println("\t<in.sam>: Single read alignment");
		System.out.println("\t\twith alternate alignment contained in XA field");
		System.out.println("\t<prefix_*>: Prefix in the contig name, to distinguish haplotype. d or s, or m(mom) d(dad).");
		System.out.println("\t<sysout>: ReadID\tClass\tMQ");
		System.out.println("\t\tClass: <prefix_1>/<prefix_2>/NA/Hom/Chim/Amb");
		System.out.println("\t\tNA: Unmapped");
		System.out.println("\t\tHom: Has equal MAPQ for alignments to both <prefix>s.");
		System.out.println("\t\tChim: Read has split alignment");
		System.out.println("\t\tAmb: Too many alternate alignemnts");
		System.out.println("Arang Rhie, 2017-12-20. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			prefix1 = args[1];
			prefix2 = args[2];
			new ClassifyRead().go(args[0]);
		} else {
			new ClassifyRead().printHelp();
		}
	}

}
