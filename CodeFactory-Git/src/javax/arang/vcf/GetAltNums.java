package javax.arang.vcf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class GetAltNums extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		String[] alts;
		
		ArrayList<Integer> altCount =  new ArrayList<Integer>();
		HashMap<Integer, Integer> altCountMap = new HashMap<Integer, Integer>();
		altCountMap.put(1, 0);
		altCount.add(1);
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			
			tokens = line.split(RegExp.TAB);
			if (tokens[VCF.ALT].contains(",")) {
				// multi-allelic
				alts = tokens[VCF.ALT].split(",");
				if (altCountMap.containsKey(alts.length)) {
					altCountMap.put(alts.length, altCountMap.get(alts.length) + 1);
				} else {
					altCount.add(alts.length);
					altCountMap.put(alts.length, 1);
				}
			} else {
				// bi-allelic
				altCountMap.put(1, altCountMap.get(1) + 1);
			}
		}
		
		fm.writeLine("Alt-Count\tNum_Sites");
		Collections.sort(altCount);
		for (int i : altCount) {
			fm.writeLine(i + "\t" + altCountMap.get(i));
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar vcfGetAltNums.jar <in.vcf> [out]");
		System.out.println("\tGet numbers of bi-allelic, tri-allelic, ..., N-allelic sites");
		System.out.println("\t<in.vcf>: To see number of sites by SNP, INDEL, SVs,");
		System.out.println("\t\trun vcfSeparateSnpIndelSV.jar before running this code.");
		System.out.println("\t[out]: DEFAULT=<in.vcf.alt_cnt>");
		System.out.println("Arang Rhie, 2016-07-12. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new GetAltNums().go(args[0], args[0] + ".alt_cnt");
		} else if (args.length == 2) {
			new GetAltNums().go(args[0], args[1]);
		} else {
			new GetAltNums().printHelp();
		}
	}

}
