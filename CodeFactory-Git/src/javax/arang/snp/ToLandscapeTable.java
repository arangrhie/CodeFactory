package javax.arang.snp;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ToLandscapeTable extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line = fr.readLine();
		String[] tokens = line.split("\t");
		fm.write("Gene\tNum_Individual_with_Mutation");
		for (int i = sampleStartIdx; i < sampleStartIdx + numSamples; i++) {
			fm.write("\t" + tokens[i]);
		}
		fm.writeLine();
		
		HashMap<String, Integer> mutCode = new HashMap<String, Integer>();
		HashMap<Integer, String> mutCodeDesc = new HashMap<Integer, String>();
		HashMap<String, String[]> mutTable = new HashMap<String, String[]>();
		
		mutCode.put("NA", -1);
		mutCodeDesc.put(-1, "NA");
		
		int[] countPerSampleSyn = new int[numSamples];
		int[] countPerSampleNonsyn = new int[numSamples];
		for (int i = 0; i < numSamples; i++) {
			countPerSampleSyn[i] = 0;
			countPerSampleNonsyn[i] = 0;
		}
		
		String[] table;
		String newCode;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			if (tokens[mutTypeIdx].equals("."))	continue;
			newCode = getMutationCodes(mutCode, tokens[mutTypeIdx], mutCodeDesc);
			if(mutTable.containsKey(tokens[geneColIdx])) {
				// insert into mutTable
				table = mutTable.get(tokens[geneColIdx]);
				//System.out.print(tokens[geneColIdx] + "\t" + newCode);
				for (int i = 0; i < numSamples; i++) {
					if (isGenotyped(tokens[i + sampleStartIdx])) {
						if (tokens[mutTypeIdx].startsWith("nonsynonymous")) {
							countPerSampleNonsyn[i]++;
						} else if (tokens[mutTypeIdx].startsWith("synonymous")){
							countPerSampleSyn[i]++;
						}
						table[i] = getNewCodes(table[i], newCode);
					}
					//System.out.print("\t" + table[i] + "/" + tokens[i + sampleStartIdx]);
				}
				mutTable.put(tokens[geneColIdx], table);
				//System.out.println();
			} else {
				// initialize mutTable for the gene
				String[] newTable = new String[numSamples];
				//System.out.print(tokens[geneColIdx] + "\t" + newCode);
				for (int i = 0; i < numSamples; i++) {
					if (isGenotyped(tokens[i + sampleStartIdx])) {
						newTable[i] = newCode;
					} else if (tokens[sampleStartIdx + i].contains("NA")) {
						newTable[i] = "-1";
					} else {
						newTable[i] = "0";
					}
					//System.out.print("\t" + newTable[i] + "/" + tokens[i + sampleStartIdx]);
				}
				//System.out.println();
				
				// put into mutTable
				mutTable.put(tokens[geneColIdx], newTable);
			}
		}
		
		// write mutTable
		int numIndiv = 0;
		int countFiltered = 0;
		int countTotal = 0;
		WRITE_LOOP : for (String gene : mutTable.keySet()) {
			numIndiv = 0;
			table = mutTable.get(gene);
			for (int i = 0; i < numSamples; i++) {
				if (!table[i].equals("0") && !table[i].equals("-1")) {
					numIndiv++;
				}
			}
			if (numIndiv == 0) {
				countFiltered++;
				continue WRITE_LOOP;
			}
			fm.write(gene + "\t" + numIndiv);
			for (int i = 0; i < numSamples; i++) {
				fm.write("\t" + table[i]);
			}
			fm.writeLine();
			countTotal++;
		}
		fm.writeLine();
		
		// write mutation counts
		StringBuffer mutCountBuff = new StringBuffer();
		int countSum = 0;
		fm.write("Syn.");
		for (int i = 0; i < numSamples; i++) {
			mutCountBuff.append("\t" + countPerSampleSyn[i]);
			countSum += countPerSampleSyn[i];
		}
		fm.write("\t" + String.format("%,.2f", (float) countSum / numSamples));
		fm.writeLine(mutCountBuff.toString());
		
		mutCountBuff = new StringBuffer();
		countSum = 0;
		fm.write("Nonsyn.");
		for (int i = 0; i < numSamples; i++) {
			mutCountBuff.append("\t" + countPerSampleNonsyn[i]);
			countSum += countPerSampleNonsyn[i];
		}
		fm.write("\t" + String.format("%,.2f", (float) countSum / numSamples));
		fm.writeLine(mutCountBuff.toString());
		
		mutCountBuff = new StringBuffer();
		countSum = 0;
		fm.write("Total");
		for (int i = 0; i < numSamples; i++) {
			mutCountBuff.append("\t" + (countPerSampleSyn[i] + countPerSampleNonsyn[i]));
			countSum += (countPerSampleSyn[i] + countPerSampleNonsyn[i]);
		}
		fm.write("\t" + String.format("%,.2f", (float) countSum / numSamples));
		fm.writeLine(mutCountBuff.toString());
		
		System.out.println("Total number of genes containing no mutation in " + numSamples + " samples: " + countFiltered);
		System.out.println("Total number of mutated genes: " + countTotal);
		
		// write mutCode description
		for (int codeIdx : mutCodeDesc.keySet()) {
			fm.writeLine(codeIdx + " : " + mutCodeDesc.get(codeIdx));
		}
	}
	
	private String getNewCodes(String oldCode, String codeToInsert) {
		String[] codesToInsert = codeToInsert.split(";");
		if (oldCode.equals("0") || oldCode.equals("-1"))	return codeToInsert;
		for (String newCode : codesToInsert) {
			if (!oldCode.contains(newCode)) {
				oldCode += ";" + newCode;
			}
		}
		return oldCode;
	}

	private boolean isGenotyped(String genotype) {
		//System.out.print("*" + genotype);
		if(genotype.equals("1") || genotype.equals("2") || genotype.equals("3"))	return true;
		return false;
	}
	
	private String getMutationCodes(HashMap<String, Integer> mutCode, String mutTypeField, HashMap<Integer, String> mutCodeDesc) {
		String newCode = "";
		String[] mutTypes = mutTypeField.split(";");
		
		for (int i = 0; i < mutTypes.length; i++) {
			if (!mutCode.containsKey(mutTypes[i])) {
				int code = mutCode.size() + 1;
				mutCode.put(mutTypes[i], code);
				mutCodeDesc.put(code, mutTypes[i]);
			}
			if(newCode.length() == 0) {
				newCode += mutCode.get(mutTypes[i]);
			} else {
				newCode += ";" + mutCode.get(mutTypes[i]);
			}
		}
		return newCode;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar snpToLandscapeTable.jar <in> <gene_col_idx> <mut_type_idx> <sample_start_idx> <num_samples>");
		System.out.println("\t<out.gene>: Gene\tNum_Individual_with_Mutation\tMutation_Table (one column indicates each sample)");
		System.out.println("\t\tMutation_Table: Mutation type will be printed in code number.");
		System.out.println("\t\tAt the end of file, mutation code will be printed with description.");
		System.out.println("\t<gene_col_idx>: Column index containing gene name. 0-based.");
		System.out.println("\t<mut_type_idx>: Column index containing mutation type; ex. Nonsynonymous SNV / Synonymous SNV / stopgain / frameshift ...");
		System.out.println("\t<sample_start_idx>: Cloumn index containing the first sample's genotype (0 1 2 or . NA - etc.). 0-based.");
		System.out.println("\t<num_samples>: Number of samples. <sample_start_idx> to <sample_start_idx> + <num_samples> - 1 will be looked up.");
		System.out.println("Arang Rhie, 2014-11-25. arrhie@gmail.com");
	}

	private static int geneColIdx = 0;
	private static int mutTypeIdx = 0;
	private static int sampleStartIdx = 0;
	private static int numSamples = 1;
	
	public static void main(String[] args) {
		if (args.length == 5) {
			geneColIdx = Integer.parseInt(args[1]);
			mutTypeIdx = Integer.parseInt(args[2]);
			sampleStartIdx = Integer.parseInt(args[3]);
			numSamples = Integer.parseInt(args[4]);
			new ToLandscapeTable().go(args[0], args[0] + ".gene");
		} else {
			new ToLandscapeTable().printHelp();
		}
		
	}

}
