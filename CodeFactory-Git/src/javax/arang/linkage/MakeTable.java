/**
 * 
 */
package javax.arang.linkage;

import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class MakeTable extends Rwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		HashMap<String, String> pedigree = new HashMap<String, String>();
		//		HashMap<String, String> phenotype = new HashMap<String, String>();
		HashMap<Integer, HashMap<String, String>> phenotypes = new HashMap<Integer, HashMap<String, String>>();
		for (int colIdx : colIdxs) {
			HashMap<String, String> newPheno = new HashMap<String, String>();
			phenotypes.put(colIdx, newPheno);
		}

		// read file, construct pedigree and phenotype
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			if (!tokens[PED.FATHER_ID].equals("") && !tokens[PED.MOTHER_ID].equals("") &&
					!tokens[PED.FATHER_ID].equals("0") && !tokens[PED.MOTHER_ID].equals("0")) {
				pedigree.put(tokens[PED.SAMPLE_ID], tokens[PED.FATHER_ID] + ";" + tokens[PED.MOTHER_ID]);
			}
			for (int colIdx : colIdxs) {
				if (tokens[colIdx].equals("") || tokens[colIdx].equals("0")) {
					continue;
				}
				HashMap<String, String> phenotype = phenotypes.get(colIdx);
				phenotype.put(tokens[PED.SAMPLE_ID], tokens[colIdx]);
			}
		}

		for (int colIdx : colIdxs) {
			System.out.println(phenotypes.get(colIdx).size() + " unique samples with phenotype of " + colIdx);
		}
		System.out.println(pedigree.size() + " samples in pedigree with father, mother ID");
		System.out.println();

		// make a 3 x 3 table for counting values
		Integer[][] counts = new Integer[3][2];
		Integer[][] counts2 = new Integer[3][2];

		final int PARENTS_1_1 = 0;
		final int PARENTS_1_2 = 1;
		final int PARENTS_2_2 = 2;

		final int OFFSPRING_1 = 0;
		final int OFFSPRING_2 = 1;

		// For category output fm
		HashMap<String, String> category = new HashMap<String, String>();


		// Check for trio and put into counts table
		HashMap<String, String> firstPhenotype = phenotypes.get(firstPheno);
		for (int colIdx : colIdxs) {
			HashMap<String, String> phenotype = phenotypes.get(colIdx);
			int count_f_pheno_only = 0;
			int count_m_pheno_only = 0;
			int count_full_ped = 0;

			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 2; j++) {
					counts[i][j] = 0;
					counts2[i][j] = 0;
				}
			}
			for (String sample : pedigree.keySet()) {
				//System.out.println(sample + " : " + pedigree.get(sample));
				tokens = pedigree.get(sample).split(";");
				
				if (!phenotype.containsKey(sample)) continue;
				if (!firstPhenotype.containsKey(sample)) continue;
				final int FAT = 0;
				final int MOT = 1;

				if (firstPhenotype.containsKey(tokens[FAT])
						&& firstPhenotype.containsKey(tokens[MOT])
						&& !phenotype.get(sample).equals("0")) {
					if (colIdx == firstPheno) {
						count_full_ped++;
					}

					if (firstPhenotype.get(tokens[FAT]).equals("1") && firstPhenotype.get(tokens[MOT]).equals("1")) {
						// parents: 1 1
						category.put(sample, "1_1");
						if (firstPhenotype.get(sample).equals("1") && !phenotype.get(sample).equals("")) {
							counts[PARENTS_1_1][OFFSPRING_1]++;
							if (colIdx != firstPheno) {
								counts2[PARENTS_1_1][OFFSPRING_1] += Integer.parseInt(phenotype.get(sample));
							}
						} else if (firstPhenotype.get(sample).equals("2") && !phenotype.get(sample).equals("")) {
							counts[PARENTS_1_1][OFFSPRING_2]++;
							if (colIdx != firstPheno) {
								counts2[PARENTS_1_1][OFFSPRING_2] += Integer.parseInt(phenotype.get(sample));
							}
						}
					} else if (firstPhenotype.get(tokens[FAT]).equals("2") && firstPhenotype.get(tokens[MOT]).equals("2")) {
						// 2 2
						category.put(sample, "2_2");
						if (firstPhenotype.get(sample).equals("1") && !phenotype.get(sample).equals("")) {
							counts[PARENTS_2_2][OFFSPRING_1]++;
							if (colIdx != firstPheno) {
								counts2[PARENTS_2_2][OFFSPRING_1] += Integer.parseInt(phenotype.get(sample));
							}
						} else if (firstPhenotype.get(sample).equals("2") && !phenotype.get(sample).equals("")) {
							counts[PARENTS_2_2][OFFSPRING_2]++;
							if (colIdx != firstPheno) {
								counts2[PARENTS_2_2][OFFSPRING_2] += Integer.parseInt(phenotype.get(sample));
							}
						}
					} else {
						// 1 2 or 2 1
						category.put(sample, "1_2");
						if (firstPhenotype.get(sample).equals("1")
								&& !phenotype.get(sample).equals("")) {
							counts[PARENTS_1_2][OFFSPRING_1]++;
							if (colIdx != firstPheno) {
								counts2[PARENTS_1_2][OFFSPRING_1] += Integer.parseInt(phenotype.get(sample));
							}
						} else if (firstPhenotype.get(sample).equals("2") && !phenotype.get(sample).equals("")){
							counts[PARENTS_1_2][OFFSPRING_2]++;
							if (colIdx != firstPheno) {
								counts2[PARENTS_1_2][OFFSPRING_2] += Integer.parseInt(phenotype.get(sample));
							}
						}
					}
				} else if (firstPhenotype.containsKey(tokens[0]) && (colIdx == firstPheno)){
					count_f_pheno_only++;
				} else if (firstPhenotype.containsKey(tokens[1]) && (colIdx == firstPheno)){
					count_m_pheno_only++;
				}
			}

			if (colIdx == firstPheno) {
				System.out.println("1 phenotype in parent, father only: " + count_f_pheno_only);
				System.out.println("1 phenotype in parent, mother only: " + count_m_pheno_only);
				System.out.println("Total # of trios with full phenotype: " + count_full_ped);
				System.out.println();

				System.out.println("Parents\tOffspring 1\tOffspring 2\t% of Pheno 1");
				float perc = 100.0f *(float)counts[PARENTS_1_1][OFFSPRING_1] / (counts[PARENTS_1_1][OFFSPRING_1] + counts[PARENTS_1_1][OFFSPRING_2]);
				System.out.println("1 x 1\t" + counts[PARENTS_1_1][OFFSPRING_1] + "\t" + counts[PARENTS_1_1][OFFSPRING_2] + "\t" + String.format("%,.2f",perc));
				perc = 100.0f *(float)counts[PARENTS_1_2][OFFSPRING_1] / (counts[PARENTS_1_2][OFFSPRING_1] + counts[PARENTS_1_2][OFFSPRING_2]);
				System.out.println("1 x 2\t" + counts[PARENTS_1_2][OFFSPRING_1] + "\t" + counts[PARENTS_1_2][OFFSPRING_2] + "\t" + String.format("%,.2f",perc));
				perc = 100.0f *(float)counts[PARENTS_2_2][OFFSPRING_1] / (counts[PARENTS_2_2][OFFSPRING_1] + counts[PARENTS_2_2][OFFSPRING_2]);
				System.out.println("2 x 2\t" + counts[PARENTS_2_2][OFFSPRING_1] + "\t" + counts[PARENTS_2_2][OFFSPRING_2] + "\t" + String.format("%,.2f",perc));
				System.out.println();
			} else {
				System.out.println("Parents\tOffspring 1\tOffspring 2");
				float meanPheno1 = (float)counts2[PARENTS_1_1][OFFSPRING_1] / counts[PARENTS_1_1][OFFSPRING_1];
				float meanPheno2 = (float)counts2[PARENTS_1_1][OFFSPRING_2] / counts[PARENTS_1_1][OFFSPRING_2];
				System.out.println("1 x 1\t" + meanPheno1 + "\t" + meanPheno2);
				meanPheno1 = (float)counts2[PARENTS_1_2][OFFSPRING_1] / counts[PARENTS_1_2][OFFSPRING_1];
				meanPheno2 = (float)counts2[PARENTS_1_2][OFFSPRING_2] / counts[PARENTS_1_2][OFFSPRING_2];
				System.out.println("1 x 2\t" + meanPheno1 + "\t" + meanPheno2);
				meanPheno1 = (float)counts2[PARENTS_2_2][OFFSPRING_1] / counts[PARENTS_2_2][OFFSPRING_1];
				meanPheno2 = (float)counts2[PARENTS_2_2][OFFSPRING_2] / counts[PARENTS_2_2][OFFSPRING_2];
				System.out.println("2 x 2\t" + meanPheno1 + "\t" + meanPheno2);
				System.out.println();
			}

		}

		fr = new FileReader(fr.getFullPath());
		FileMaker fm = new FileMaker(fr.getDirectory(), "categoric.ped");
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			if (category.containsKey(tokens[PED.SAMPLE_ID])) {
				fm.writeLine(line + "\t" + category.get(tokens[PED.SAMPLE_ID]));
			} else {
				//fm.writeLine(line);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar linkageMakeTable.jar <in.ped> <0-based col_num> .. <0-based col_num>");
		System.out.println("\tConstructs a table for binary ( and continous ) phenotype(s).");
		System.out.println("\t<out>: Parent Phenotype (x) | Offspring Pheno 1 | Offspring Pheno 2 | % of Pheno 1");
		System.out.println("\tTongueRolling; 1 = R, 2 = NR");
	}

	static int firstPheno = 1;
	static int[] colIdxs;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length >= 2) {
			firstPheno = Integer.parseInt(args[1]);
			colIdxs = new int[args.length - 1];
			for (int colIdx = 1; colIdx < args.length; colIdx++) {
				colIdxs[colIdx - 1] = Integer.parseInt(args[colIdx]);
			}
			new MakeTable().go(args[0]);
		} else {
			new MakeTable().printHelp();
		}
	}

}
