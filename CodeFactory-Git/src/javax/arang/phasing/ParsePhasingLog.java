package javax.arang.phasing;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ParsePhasingLog extends Rwrapper {

	
	@Override
	public void hooker(FileReader fr) {
	
		String line;
		String[] tokens;
		int hetToHom = 0;
		int hetToDel = 0;
		int hetCorrect = 0;
		int iterationCount = 0;
		int switched = 0;
		int updatedSNPs = 0;
		int snps = 0;
				
		System.out.println("=====================" + fr.getFileName() + " =====================");
		System.out.println("longest block size, block N50, num. blocks, genome covered bases:");
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("For simplicity;")) {
				System.out.println("Iterating " + iterationCount);
				System.out.println(fr.readLine());
				System.out.println(fr.readLine());
				System.out.println(fr.readLine());
				System.out.println(fr.readLine());
				System.out.println();
			}
			
			if (line.startsWith("Num. SNPs changed to homozygotes:")) {
				tokens = line.split(RegExp.WHITESPACE);
				hetToHom += Integer.parseInt(tokens[tokens.length - 1]);
				line = fr.readLine();
				tokens = line.split(RegExp.WHITESPACE);
				hetToDel += Integer.parseInt(tokens[tokens.length - 1]);
				line = fr.readLine();
				tokens = line.split(RegExp.WHITESPACE);
				hetCorrect += Integer.parseInt(tokens[tokens.length - 1]);
				iterationCount++;
			}
			
			if (line.startsWith("Num. SNPs that will be switched:")) {
				switched = Integer.parseInt(fr.readLine().trim());
			}
			
			if (line.startsWith("SNPs:")) {
				tokens = line.split(RegExp.WHITESPACE);
				updatedSNPs = snps;
				snps = Integer.parseInt(tokens[tokens.length - 1]);
			}
		}
		
		System.out.println("Iterating " + iterationCount);
		System.out.println("Het>Hom\tHet>Del\tHet>Het\tUpdatedSNPs\tSwitched");
		System.out.println(hetToHom + "\t" + hetToDel + "\t" + hetCorrect + "\t" + updatedSNPs + "\t" + switched);
		System.out.println();
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingParsePhasingLog.jar <in.iterate_phasing_chr.log>");
		System.out.println("\tGathers stat information out of log file per chromosome");
		System.out.println("Arang Rhie, 2015-08-20. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ParsePhasingLog().go(args[0]);
		} else {
			new ParsePhasingLog().printHelp();
		}
	}

}
