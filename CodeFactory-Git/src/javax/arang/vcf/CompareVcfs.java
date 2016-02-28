package javax.arang.vcf;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class CompareVcfs extends I2Owrapper {
	
	private static final short ALT = 3;

	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		HashMap<String, String> posToAlt = new HashMap<String, String>();
		String line;
		String[] tokens;
		
		String pos;
		String alt;
		String alt1;
		int fm2OnlyCount = 0;
		
		FileMaker fm1Only = new FileMaker(prefix + "." + fr1.getFileName() + ".only");
		FileMaker fm2Only = new FileMaker(prefix + "." + fr2.getFileName() + ".only");
		ArrayList<String> coveredWithFr2 = new ArrayList<String>();
		
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			pos = tokens[VCF.POS];
			alt = tokens[ALT];
			posToAlt.put(pos, alt);
		}
		System.out.println(":: [DEBUG] :: fr1 size: " + posToAlt.size());
		
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			pos = tokens[VCF.POS];
			alt = tokens[ALT];
			if (posToAlt.containsKey(pos)) {
				alt1 = posToAlt.get(pos);
				if (alt1.contains(",")) {
					tokens = alt1.split(RegExp.TAB);
					for (int i = 0; i < tokens.length; i++) {
						if (tokens[i].equals(alt1)) {
							fm.writeLine(line);
							coveredWithFr2.add(pos);
							break;
						}
					}
				} else {
					if (alt.equals(alt1)) {
						fm.writeLine(line);
						coveredWithFr2.add(pos);
					}
				}
			} else {
				fm2Only.writeLine(line);
				fm2OnlyCount++;
			}
		}
		
		System.out.println(":: Position intersect : " + coveredWithFr2.size());
		for (String positionCovered : coveredWithFr2) {
			if (posToAlt.containsKey(positionCovered)) {
				posToAlt.remove(positionCovered);
			}
		}
		
		System.out.println(":: Position left from " + fr1.getFileName() + " : " + posToAlt.size());
		for (String posLeft : posToAlt.keySet()) {
			fm1Only.writeLine(posLeft + "\t" + posToAlt.get(posLeft));
		}
		
		System.out.println(":: Position left from " + fr2.getFileName() + " : " + fm2OnlyCount);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar variantCompareVcfs.jar <in1.vcf> <in2.vcf> <out_prefix>");
		System.out.println("\t<out_prefix>: <out_prefix>.in1.only, <out_prefix>.in2.only <out_prefix>.intersect");
		System.out.println("\t*Run this per chromosome");
		System.out.println("Arang Rhie, 2016-02-04. arrhie@gmail.com");
	}
	
	private static String prefix;

	public static void main(String[] args) {
		if (args.length == 3) {
			prefix = args[2];
			new CompareVcfs().go(args[0], args[1], args[2]+ ".intersect");
		} else {
			new CompareVcfs().printHelp();
		}
	}

}
