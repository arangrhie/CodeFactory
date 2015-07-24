package javax.arang.linkage;

import java.util.Vector;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.annovar.ANNOVAR;

public class Annovar2PedMap extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		line = fr.readLine();
		tokens = line.split("\t");
		
		int SAMPLE_START = ANNOVAR.NOTE + 1;
		int SAMPLE_END = SAMPLE_START + totalSamples - 1;
		int DBSNP_ID = ANNOVAR.NOTE;

		Vector<String> phenotype = new Vector<String>();
		Vector<String> sampleId = new Vector<String>();
		
		
		// collect sample phenotype quantile
		for (int i = 0; i < leftNumSamples; i++) {
			if (isCaseControl) {
				phenotype.add("2");
			} else {
				phenotype.add(tokens[SAMPLE_START + i]);
			}
		}
		
		for (int i = 0; i < rightNumSamples; i++) {
			if (isCaseControl) {
				phenotype.add("1");
			} else {
				phenotype.add(tokens[SAMPLE_END - i]);
			}
		}
		
		System.out.println(phenotype.size() + " sample phenotypes added");
		
		line = fr.readLine();
		tokens = line.split("\t");		
		
		// collect sample id
		for (int i = 0; i < leftNumSamples; i++) {
			sampleId.add(tokens[SAMPLE_START + i]);
		}
		
		for (int i = 0; i < rightNumSamples; i++) {
			sampleId.add(tokens[SAMPLE_END - i]);
		}
		
		Vector<Vector<String>> genotype = new Vector<Vector<String>>();
		for (int i = 0; i < (leftNumSamples+rightNumSamples); i++) {
			genotype.add(new Vector<String>());
		}

		
		
		// Make 3 output files
		String cc = (isCaseControl)? "CC" : "Qassoc";
		String fileName = fr.getFileName() + "." + leftNumSamples + "_" + rightNumSamples + "_" + totalSamples + "_" + cc;
		FileMaker fmPed = new FileMaker(fr.getDirectory(), fileName + ".ped");
		FileMaker fmMap = new FileMaker(fr.getDirectory(), fileName + ".map");
		FileMaker fmMark = new FileMaker(fr.getDirectory(), fileName + ".marker");
		
		int familyId = 1;
		int novelNum = 1;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine().trim();
			tokens = line.split("\t");
			String rs = tokens[DBSNP_ID];	// dbSNP
			if (rs.startsWith("novel") || rs.startsWith(".")) {
				rs = "novel" + novelNum++;
			}
			String chromosome = tokens[ANNOVAR.CHR];
			try {
				chromosome = tokens[ANNOVAR.CHR].replace("chr", "");
				chromosome = Integer.valueOf(chromosome) + "";
			} catch (Exception e) {
				// do nothing
			}
			
			fmMap.writeLine(chromosome 
					+ "\t" + rs
					+ "\t" + "0"
					+ "\t" +
					tokens[ANNOVAR.POS_FROM]);
			
			String nsSnp = "";
			if (tokens.length-1 > SAMPLE_END) {
				nsSnp = tokens[SAMPLE_END + 1];	// nsSNP
				if (nsSnp.equals("#N/A")) {
					nsSnp = "";
				}
			}
//			fmMark.writeLine(tokens[ANNOVAR.CHR] + "_" + tokens[ANNOVAR.POS_FROM]
//					+ "\t" + tokens[ANNOVAR.POS_FROM] + "\t" + nsSnp);
			fmMark.writeLine(rs + "\t" + tokens[ANNOVAR.POS_FROM] + "\t" + nsSnp);
			
			for (int i = 0; i < leftNumSamples; i++) {
				genotype.get(i).add(getGenotype(tokens[ANNOVAR.REF],
						tokens[ANNOVAR.ALT],
						tokens[SAMPLE_START + i]));
			}
			for (int i = 0; i < rightNumSamples; i++) {
				genotype.get(leftNumSamples + i).add(getGenotype(tokens[ANNOVAR.REF],
						tokens[ANNOVAR.ALT],
						tokens[SAMPLE_END - i]));
			}
		}
		
		
		for (int i = 0; i < phenotype.size(); i++) {
			fmPed.write("F" + familyId++ + "\t" + sampleId.get(i)
					+ "\t0\t0\t1\t" + phenotype.get(i));
			for (int j = 0; j < genotype.get(i).size(); j++) {
				fmPed.write("\t" + genotype.get(i).get(j));
			}
			fmPed.writeLine("");
		}
	}
	
	private String getGenotype(String ref, String snp, String type) {
		if (type.equals("2")) {
			return snp + " " + snp;
		} else if (type.equals("1")) {
			return ref + " " + snp;
		} else if (type.equals("0")){
			return ref + " " + ref;
		} else {	// genotype is NA
			return 0 + " " + 0;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar annovar2PedMap.jar <in.annovar> [left_num_samples] [right_num_samples] [total_samples] [CC]");
		System.out.println("\t<in.annovar>: chr\tpos\tpos\tref\tsnp\trs#\t<sample1_genotype>\t...\t<sampleN_genotype>\t[nsSNP]");
		System.out.println("\t\t[left_num_samples]: left_num_samples from the left end will be used to generate output files. DEFAULT=10");
		System.out.println("\t\t[right_num_samples]: right_num_samples from the right end will be used to generate output files. DEFAULT=10");
		System.out.println("\t\t[total_samples]: total number of samples. DEFAULT=42");
		System.out.println("\t\t[CC]: mark each num_samples as case/control. DEFAULT=Quantitative");
		System.out.println("\t<out>: 2 files will be generated: <in.ped> and <in.map>.");
		System.out.println("\t\tUse these files for PLINK or HaploView.");
	}

	public static boolean isCaseControl = false;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			new Annovar2PedMap().printHelp();
		} else {
			if (args.length > 1) {
				leftNumSamples = Integer.parseInt(args[1]);
				rightNumSamples = Integer.parseInt(args[2]);
				totalSamples = Integer.parseInt(args[3]);
				System.out.println(leftNumSamples + " / " + rightNumSamples + " TOTAL: " + totalSamples);
			}
			if (args[args.length - 1].equals("CC")) {
				isCaseControl = true;
				System.out.println("Case-control design applied");
			}
			new Annovar2PedMap().go(args[0]);
		}
	}
	
	static int leftNumSamples = 10;
	static int rightNumSamples = 10;
	static int totalSamples = 42;

}
