package javax.arang.genome.snp;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.annovar.ANNOVAR;

public class IndelHomoCount extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		line = fr.readLine();
		fm.writeLine(line + "\t#ofBases\tRegion\tBases");
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.length() < 5)	{
				continue;
			}
			tokens = line.split("\t");
			String ref = tokens[ANNOVAR.REF];
			char base = ref.charAt(0);
			boolean isHomo = false;
			for (int i = 1; i < ref.length(); i++) {
				if (base != ref.charAt(i)) {
					isHomo = false;
					break;
				} else {
					isHomo = true;
				}
			}
			if (isHomo) {
				fm.writeLine(line + "\t" + ref.length() + "\t"
						+ "Poly" + base + " (" + tokens[ANNOVAR.POS_FROM] + "-" + tokens[ANNOVAR.POS_TO] + ")\t" + ref);
			} else {
				fm.writeLine(line);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar indelHomoCount.jar <ANNOVAR_in> <ANNOVAR_out>");
		System.out.println("\tadd <# of bp>\t<PolyN (region)>\t<bases>");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new IndelHomoCount().go(args[0], args[1]);
		} else {
			new IndelHomoCount().printHelp();
		}
	}

}
