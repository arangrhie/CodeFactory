package javax.arang.phasing;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedRead;

public class HaplotypeAtSNP extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		int start;
		int end;
		int pos;
		
		int haplotypeIdx = 0;
		boolean isContaining = false;
		String haplotype;
		char hap;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			start = Integer.parseInt(tokens[PhasedRead.START]);
			end = Integer.parseInt(tokens[PhasedRead.END]);
			
			if (position < start || end < position)	continue;	// not in region
			if (tokens.length <= PhasedRead.HAPLOTYPE)	continue;	// no snp
			
			isContaining = false;
			haplotypeIdx = 0;
			for (int i = PhasedRead.SNP_POS_LIST; i < tokens.length; i++) {
				pos = Integer.parseInt(tokens[i]);
				if (pos == position) {
					isContaining = true;
					break;
				}
				haplotypeIdx++;
			}
			
			if (isContaining) {
				haplotype = tokens[PhasedRead.HAPLOTYPE].toLowerCase();
				hap = Character.toUpperCase(haplotype.charAt(haplotypeIdx));
				String left = haplotype.substring(0, haplotypeIdx);
				String right = "";
				if (haplotypeIdx < haplotype.length() - 1) {
					right = haplotype.substring(haplotypeIdx + 1, haplotype.length());
				}
				System.out.println(position + "\t" + tokens[PhasedRead.READ_ID] + "\t" + hap + "\t" + left + hap + right);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingHaplotypeAtSNP.jar <in.read> <position>");
		System.out.println("\t<in.read>: Generated with phasingSubreadBasedPhasedSNP.jar");
		System.out.println("\t<position>: SNP position to look up");
		System.out.println("\t<output>: <position>\t<BAC_ID>\thaplotype\thaplotype(full length, softmarking other sites than position)");
		System.out.println("Arang Rhie, 2015-08-31. arrhie@gmail.com");
	}

	private static int position;
	public static void main(String[] args) {
		if (args.length == 2) {
			position = Integer.parseInt(args[1]);
			new HaplotypeAtSNP().go(args[0]);
		} else {
			new HaplotypeAtSNP().printHelp();
		}
	}

}
