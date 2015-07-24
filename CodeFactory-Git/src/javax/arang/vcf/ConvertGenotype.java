package javax.arang.vcf;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ConvertGenotype extends IOwrapper {

	static int num_samples = 0;
	static int col_num = 1;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line = fr.readLine();
		String[] tokens;
		
		fm.writeLine(line);
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			fm.write(tokens[0]);
			for (int i = 1; i < col_num; i++) {
				fm.write("\t" + tokens[i]);
			}
			for (int i = col_num; i < col_num + num_samples - 1; i++) {
				fm.write("\t" + VCF.parseGT("GT", tokens[i]));
			}
			if (col_num + num_samples < tokens.length - 1) {
				for (int i = col_num + num_samples + 1; i < tokens.length - 1; i++) {
					fm.write("\t" + tokens[i]);
				}
			}
			fm.writeLine();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar vcfConvertGenotype.jar <in> <out> <num_samples> <col_num>");
		System.out.println("\tConvert VCF formatted genotypes into 0 1 2 format");
		System.out.println("\t<in>: Any tab-deliminated text file");
		System.out.println("\t<out>: ./. or .|. converted to NA, 0/0 to 0, 0/1 to 1, 1/1 to 2. Other multiallels to 3.");
		System.out.println("Arang Rhie, 2014-10-10. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 4) {
			num_samples = Integer.parseInt(args[2]);
			col_num = Integer.parseInt(args[3]);
			new ConvertGenotype().go(args[0], args[1]);
		} else {
			new ConvertGenotype().printHelp();
		}
	}

}
