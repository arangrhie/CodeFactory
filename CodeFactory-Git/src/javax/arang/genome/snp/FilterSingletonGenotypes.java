package javax.arang.genome.snp;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.annovar.util.ANNOVAR;

public class FilterSingletonGenotypes extends IOwrapper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new FilterSingletonGenotypes().go(args[0], args[0] + "_singleton");
		} else if (args.length == 3) {
			num_samples = Integer.parseInt(args[1]);
			maf_num = Integer.parseInt(args[2]);
			new FilterSingletonGenotypes().go(args[0], args[0] + "_maf_" + args[1] + "_" + args[2] );
		} else {
			new FilterSingletonGenotypes().printHelp();
		}
	}
	
	static int num_samples = 0;
	static int maf_num = 1;

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		int totalCount = 0;
		int filtered = 0;
		
		fm.writeLine(fr.readLine());

		int numSampleCol = -1;
		
		READ_LINE : while (fr.hasMoreLines()) {
			String line = fr.readLine();
			String[] tokens = line.split("\t");
			totalCount++;
			if (numSampleCol < 0) {
				numSampleCol = tokens.length;
			}
			int count0 = 0;
			int count1 = 0;
			int count2 = 0;
			
			if (num_samples == 0) {
				READ_GT : for (int i = ANNOVAR.NOTE + 1; i < numSampleCol; i++) {
					try {
						String gt = tokens[i];
						if (gt.equals("NA"))	continue READ_GT;
						int genoType = Integer.parseInt(gt);
						if (genoType == 0) {
							count0++;
						} else if (genoType == 1) {
							count1++;
						} else if (genoType == 2) {
							count2++;
						}
					} catch (NumberFormatException e) {
						numSampleCol = i;
						System.out.println("Non-genotype info detected in column " + i);
						fm.writeLine(line);
						continue READ_LINE;
					}
				}
			} else {
				READ_GT : for (int i = ANNOVAR.NOTE + 1; i < ANNOVAR.NOTE + 1 + num_samples; i++) {
					try {
						String gt = tokens[i];
						if (gt.equals("NA"))	continue READ_GT;
						int genoType = Integer.parseInt(gt);
						if (genoType == 0) {
							count0++;
						} else if (genoType == 1) {
							count1++;
						} else if (genoType == 2) {
							count2++;
						}
					} catch (NumberFormatException e) {
						numSampleCol = i;
						System.out.println("Non-genotype info detected in column " + i);
						fm.writeLine(line);
						continue READ_LINE;
					}
				}
			READ_GT : for (int i = numSampleCol - 1; i > numSampleCol - 1 - num_samples; i--) {
				try {
					String gt = tokens[i];
					if (gt.equals("NA"))	continue READ_GT;
					int genoType = Integer.parseInt(gt);
					if (genoType == 0) {
						count0++;
					} else if (genoType == 1) {
						count1++;
					} else if (genoType == 2) {
						count2++;
					}
				} catch (NumberFormatException e) {
					numSampleCol = i;
					System.out.println("Non-genotype info detected in column " + i);
					fm.writeLine(line);
					continue READ_LINE;
				}
			}

			}
			
			
			
			//System.out.println(count0 + "\t" + count1 + "\t" + count2);
			if (is1Singleton(count0, count1, count2)
					|| is1Singleton(count1, count0, count2)
					|| is1Singleton(count2, count1, count0)) {
				continue READ_LINE;
			}
			fm.writeLine(line);
			filtered++;
		}
		
		System.out.println(totalCount + "\t->\t" + filtered);
	}
	
	private boolean is1Singleton(int count1, int count2, int count3) {
		if (count1 == 0 && (count2 <= maf_num || count3 <= maf_num)) {
			return true;
		}
		return false;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar snpFilterSampleSpecGenotypes.jar <in.snp> [num_samples] [maf_num]");
		System.out.println("\t[num_samples]: number of samples to take into account from both ends");
		System.out.println("\t[maf_num]: number of samples having unique snps. DEFAULT=1 (singleton)");
		System.out.println("\t<output>: <in.snp>.unique_filtered");
		System.out.println("\tFilter snps that are uniquely genotyped while others are not.");
	}

}
