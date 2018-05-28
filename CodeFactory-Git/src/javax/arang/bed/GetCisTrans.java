package javax.arang.bed;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class GetCisTrans extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String readid;
		String prevReadid = "";
		String contig;
		
		boolean hasMaternalR1 = false;
		boolean hasMaternalR2 = false;
		boolean hasPaternalR1 = false;
		boolean hasPaternalR2 = false;
		
		double numCis = 0;
		double numTrans = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			contig = tokens[Bed.CHROM];
			readid = tokens[Bed.NOTE];
			
			if (!prevReadid.equals("") && !prevReadid.equals(readid)) {
				if ((hasMaternalR1 && hasMaternalR2 && !hasPaternalR1 && !hasPaternalR2)
						|| (!hasMaternalR1 && !hasMaternalR2 && hasPaternalR1 && hasPaternalR2)) {
					System.out.println(prevReadid + "\tcis");
					numCis++;
				} else if ((hasMaternalR1 && !hasMaternalR2 && !hasPaternalR1 && hasPaternalR2)
						||(!hasMaternalR1 && hasMaternalR2 && hasPaternalR1 && !hasPaternalR2)) {
					System.out.println(prevReadid + "\ttrans");
					numTrans++;
				}
				
				hasMaternalR1 = false;
				hasMaternalR2 = false;
				hasPaternalR1 = false;
				hasPaternalR2 = false;
			}
			
			if (contig.startsWith(maternalPrefix)) {
				if (tokens[Bed.NOTE + 1].equals("1")) {
					hasMaternalR1 = true;
				} else {
					hasMaternalR2 = true;
				}
			} else if (contig.startsWith(paternalPrefix)){
				if (tokens[Bed.NOTE + 1].equals("1")) {
					hasPaternalR1 = true;
				} else {
					hasPaternalR2 = true;
				}
			}
			
			prevReadid = readid;
		}
		
		if (!prevReadid.equalsIgnoreCase("")) {
			if ((hasMaternalR1 && hasMaternalR2 && !hasPaternalR1 && !hasPaternalR2)
					|| (!hasMaternalR1 && !hasMaternalR2 && hasPaternalR1 && hasPaternalR2)) {
				System.out.println(prevReadid + "\tcis");
				numCis++;
			} else if ((hasMaternalR1 || hasPaternalR2) && (hasPaternalR1 || hasPaternalR2)) {
				System.out.println(prevReadid + "\ttrans");
				numTrans++;
			}
		}
		System.err.println("Num. Cis: " + String.format("%,.0f", numCis));
		System.err.println("Num. Trans: " + String.format("%,.0f", numTrans));
	}
	
	private static String maternalPrefix = "d_";
	private static String paternalPrefix = "s_";

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedGetCisTrans.jar <in.bamtobed.bed> [maternalPrefix paternalPrefix]");
		System.out.println("\t<in.bamtobed.bed>: split R1/R2 with sed \'s/\\/\\t/g\'");
		System.out.println("\t<stdout>: reports the num. cis and trans alignments");
		System.out.println("\t[maternalPrefix paternalPrefix]: DEFAULT=d_ s_");
		System.out.println("Arang Rhie, 2017-11-27. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new GetCisTrans().go(args[0]);
		} else if (args.length == 3) {
			maternalPrefix = args[1];
			paternalPrefix = args[2];
			new GetCisTrans().go(args[0]);
		} else {
			new GetCisTrans().printHelp();
		}
	}

}
