/**
 * 
 */
package javax.arang.linkage;

import java.util.Vector;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.annovar.ANNOVAR;

/**
 * @author Arang Rhie
 *
 */
public class Annovar2SnpSpD extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		line = fr.readLine().trim();
		tokens = line.split("\t");
		
//		Vector<String> phenotype = new Vector<String>();
		Vector<String> sampleId = new Vector<String>();
		
		// collect sample phenotype quantile
//		for (int i = 0; i < numSamples; i++) {
//			if (isCaseControl) {
//				phenotype.add("2");
//			} else {
//				phenotype.add(tokens[i]);
//			}
//		}
//		
//		for (int i = 1; i <= numSamples; i++) {
//			if (isCaseControl) {
//				phenotype.add("1");
//			} else {
//				phenotype.add(tokens[tokens.length - i]);
//			}
//		}
		
		line = fr.readLine();
		tokens = line.split("\t");
		
		// collect sample id
		for (int i = 0; i < leftNumSamples; i++) {
			sampleId.add(tokens[ANNOVAR.NOTE + i]);
		}
		
		for (int i = 1; i <= rightNumSamples; i++) {
			sampleId.add(tokens[ANNOVAR.NOTE + totalSamples - i]);
		}
		
		Vector<Vector<String>> genotype = new Vector<Vector<String>>();
		for (int i = 0; i < (leftNumSamples+rightNumSamples); i++) {
			genotype.add(new Vector<String>());
		}
		
		// Make 2 input files
//		String cc = (isCaseControl)? "CC" : "Qassoc";
//		String fileName = fr.getFileName() + "." + numSamples + "_" + totalSamples + "_" + cc;
		String fileName = fr.getFileName() + "." + leftNumSamples + "_" + totalSamples;
		FileMaker fmPre = new FileMaker(fr.getDirectory(), fileName + ".pre");
		FileMaker fmMap = new FileMaker(fr.getDirectory(), fileName + ".map");
		fmMap.writeLine("MARKERID\tNAME\tLOCATION");
		int familyId = 1;
		int makerId = 1;
		int novelNum = 1;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine().trim();
			tokens = line.split("\t");
			String rs = tokens[ANNOVAR.NOTE + totalSamples];	// dbSNP
			if (rs.startsWith("novel")) {
				rs = "novel" + novelNum++;
			}
			fmMap.writeLine(makerId++ + "\t" + rs + "\t"
			+ String.format("%.0f", Util.chrPosToDouble(tokens[ANNOVAR.CHR], tokens[ANNOVAR.POS_FROM])));
			
			for (int i = 0; i < leftNumSamples; i++) {
				genotype.get(i).add(getGenotype(tokens[ANNOVAR.REF],
						tokens[ANNOVAR.ALT],
						tokens[ANNOVAR.NOTE + i]));
			}
			for (int i = 1; i <= rightNumSamples; i++) {
				genotype.get(totalSamples - rightNumSamples + i-1).add(getGenotype(tokens[ANNOVAR.REF],
						tokens[ANNOVAR.ALT],
						tokens[ANNOVAR.NOTE + totalSamples - i]));
			}
		}
		
		for (int i = 0; i < (leftNumSamples+rightNumSamples); i++) {
			fmPre.write(familyId++ + "\t" + sampleId.get(i)
					+ "\t0\t0\t1\t");
					//+ "\t0\t0\t1\t" + phenotype.get(i) + "\t");
			for (int j = 0; j < genotype.get(i).size(); j++) {
				fmPre.write(genotype.get(i).get(j) + "\t");
			}
			fmPre.writeLine("");
		}
	}

	private String getGenotype(String ref, String snp, String type) {
		if (type.equals("2")) {
			return getGenotypeNum(snp) + " " + getGenotypeNum(snp);
		} else if (type.equals("1")) {
			return getGenotypeNum(ref) + " " + getGenotypeNum(snp);
		} else if (type.equals("0")){
			return getGenotypeNum(ref) + " " + getGenotypeNum(ref);
		} else {	// genotype is NA
			return 0 + " " + 0;
		}
	}
	
	private String getGenotypeNum(String genotype) {
		if (genotype.equals("A")) {
			return "1";
		}
		if (genotype.equals("C")) {
			return "2";
		}
		if (genotype.equals("G")) {
			return "3";
		}
		if (genotype.equals("T")) {
			return "4";
		}
		return "0";
	}
	
	/* (non-Javadoc)
	 * @see javax.arang.IO.Rwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar annovar2SnpSpD.jar <snptable.snp> [left_num_samples=10] [right_num_samples=10] [total_samples=42]");
		System.out.println("Generate input .pre and .map files for using SNPSpD.");
		System.out.println("Details are in http://gump.qimr.edu.au/general/daleN/SNPSpD/");
		System.out.println("\t<SNP talbe format> : chr\tstart\tstop\tsnp\talt\t0/1/2/NA genotypes for total_samples\trs_umber");
		System.out.println("\t\tPhenotype should appear in the header, sample names in the next line");
		System.out.println("\t\t[left_num_samples]: left_num_samples from the left end will be used to generate output files. DEFAULT=10");
		System.out.println("\t\t[right_num_samples]: right_num_samples from the right end will be used to generate output files. DEFAULT=10");
		System.out.println("\t\t[total_samples]: total number of samples. DEFAULT=42");
		System.out.println("\t\t[CC]: mark each num_samples as case/control. DEFAULT=Quantitative");
		System.out.println("\t.pre : famid\tpid\tfatid(0)\tmotid(0)\tsex(male=1,female=2)\tgenotype");
		System.out.println("\t.map : markerid\tname\tlocation");
	}
	
	public static boolean isCaseControl = false;
	static int leftNumSamples = 10;
	static int rightNumSamples = 10;
	static int totalSamples = 42;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			new Annovar2SnpSpD().printHelp();
		} else {
			if (args.length > 1) {
				leftNumSamples = Integer.parseInt(args[1]);
				rightNumSamples = Integer.parseInt(args[2]);
				totalSamples = Integer.parseInt(args[3]);
			}

			new Annovar2SnpSpD().go(args[0]);
		}

	}

}
