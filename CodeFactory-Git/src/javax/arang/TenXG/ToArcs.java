package javax.arang.TenXG;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.sam.SAMUtil;
import javax.arang.sam.Sam;

public class ToArcs extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		String bc;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@")) {
				System.out.println(line);
				continue;
			}
			tokens = line.split(RegExp.WHITESPACE);
			bc = "";
			if (SAMUtil.isAligned(Integer.parseInt(tokens[Sam.FLAG]))) {
				for (int i = Sam.TAG; i < tokens.length; i++) {
					if (tokens[i].startsWith("BX:Z")) {
						bc = tokens[i].substring(5, tokens[i].length() - 2);
						break;
					}
				}
				
				if (!bc.equals("")) {
					System.out.println(tokens[Sam.QNAME] + "_" + bc + line.substring(tokens[Sam.QNAME].length()));
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar tenXGtoArcs.jar <in.sam>");
		System.out.println("\t<in.sam>: bam file generated with longranger wgs; with BX:Z tag for the barcodes");
		System.out.println("\t<stdout>: sam with read id appended with the _<BARCODE>");
		System.out.println("Arang Rhie, 2017-06-14. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ToArcs().go(args[0]);
		} else {
			new ToArcs().printHelp();
		}
	}

}
