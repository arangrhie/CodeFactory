package javax.arang.genome.snp;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.annovar.util.ANNOVAR;

public class FilterHomoGenotypes extends IOwrapper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new FilterHomoGenotypes().go(args[0], args[0] + "_hom");
		} else if (args.length == 2) {
			num_samples = Integer.parseInt(args[1]);
			new FilterHomoGenotypes().go(args[0], args[0] + "_hom_" + args[1]);
		} else {
			new FilterHomoGenotypes().printHelp();
		}
	}
	
	static int num_samples = 0;

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		int totalCount = 0;
		int filtered = 0;
		
		fm.writeLine(fr.readLine());
		
		READ_LINE : while (fr.hasMoreLines()) {
			String line = fr.readLine();
			String[] tokens = line.split("\t");
			totalCount++;
			String prevGt = tokens[ANNOVAR.NOTE];
			
			if (num_samples == 0) {
				READ_GT : for (int i = ANNOVAR.NOTE + 1; i < tokens.length; i++) {
					String gt = tokens[i];
					if (gt.equals("NA"))	continue READ_GT;
					if (!prevGt.equals(gt)) {
						fm.writeLine(line);
						filtered++;
						continue READ_LINE;
					}
				}
			} else {
				READ_GT : for (int i = ANNOVAR.NOTE + 1; i < ANNOVAR.NOTE + 1 + num_samples; i++) {
					String gt = tokens[i];
					if (gt.equals("NA"))	continue READ_GT;
					if (!prevGt.equals(gt)) {
						fm.writeLine(line);
						filtered++;
						continue READ_LINE;
					}
				}
				
				READ_GT : for (int i = tokens.length - 1; i > tokens.length - 1 - num_samples; i--) {
					String gt = tokens[i];
					if (gt.equals("NA"))	continue READ_GT;
					if (!prevGt.equals(gt)) {
						fm.writeLine(line);
						filtered++;
						continue READ_LINE;
					}
				}
			}
			
		}
		
		System.out.println(totalCount + "\t->\t" + filtered);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar snpFilterHomoGenotyped.jar <in.snp> [num_samples]");
		System.out.println("\tFilter snps that are equally genotyped among all samples");
		System.out.println("\t[num_samples]: number of samples to take into account from both ends");
	}

}
