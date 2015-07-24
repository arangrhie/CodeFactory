package javax.arang.annovar;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class AddGenotype extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		System.out.println("[DEBUG] :: Write header");
		fm.writeLine(fr.readLine() + "\t\t\t\t\t\t\t\t\t\tGenotype");
		
		String line;
		String[] tokens;
		String genotype;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			genotype = tokens[tokens.length - 1];
			//System.out.println("[DEBUG] :: genotype = " + genotype);
			genotype = genotype.substring(0, 3);
			if (genotype.equals("0/1") || genotype.equals("0|1") || genotype.equals("1/0") || genotype.equals("1|0")) {
				genotype="1";
			} else if (genotype.equals("1/1") || genotype.equals("1|1")) {
				genotype="2";
			} else if (genotype.equals("0/0") || genotype.equals("0|0")) {
				genotype="0";
			} else {
				genotype="3";
			}
			fm.writeLine(line + "\t" + genotype);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar annovarAddGenotype.jar <in.annovar.out.txt or VCF> <out> [Other column idx=6]");
		System.out.println("\tAdd a column named [Genotype], transfering 0/0 0/1 1/1 to 0 1 2");
	}

	private static int otherIdx = 6;
	public static void main(String[] args) {
		if (args.length == 3) {
			otherIdx = Integer.parseInt(args[2]);
			new AddGenotype().go(args[0], args[1]);
		} else if (args.length == 2) {
			new AddGenotype().go(args[0], args[1]);
		} else {
			new AddGenotype().printHelp();
		}

	}

}
