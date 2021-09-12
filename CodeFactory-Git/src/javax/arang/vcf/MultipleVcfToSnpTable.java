package javax.arang.vcf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class MultipleVcfToSnpTable extends INOwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar vcfToSnpTable.jar <in1.vcf> <in2.vcf> .. <inN.vcf> <outfile>");
		System.out.println("\tMerge vcf files according to its genome position");
		System.out.println("\tFilter genotypes under GQ < 17");
		System.out.println("\tFilter genotypes under GQ < 17");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		Vector<String> chrVec = new Vector<String>();
		HashMap<String, Integer> chrMin = new HashMap<String, Integer>();
		HashMap<String, Integer> chrMax = new HashMap<String, Integer>();
		HashMap<String, HashMap<Integer, String>> genotypeMap = new HashMap<String, HashMap<Integer, String>>(); 
		HashMap<Integer, String> genoMap;
		HashMap<String, String> formatTable = null;

		int sampleNum = 0;
		//boolean isFirst = true;
		for (FileReader fr : frs) {
			sampleNum++;
			READ_LINE : while(fr.hasMoreLines()) {
				String line = fr.readLine();
				if (line.startsWith("#"))	continue;
				String[] tokens = line.split("\t");
				String chr = tokens[VCF.CHROM];
				if (!chrVec.contains(chr)) {
					chrVec.add(chr);
					genotypeMap.put(chr, new HashMap<Integer, String>());
					chrMin.put(chr, Integer.MAX_VALUE);
					chrMax.put(chr, -1);
				}

				// filter out if not fits conditions
				// skip INDELs
				HashMap<String, String> infoTable = VCF.parseInfo(tokens[VCF.INFO]);
				if (infoTable.containsKey("INDEL")) {
					continue READ_LINE;
				}
				
				genoMap = genotypeMap.get(chr);
				int pos = Integer.parseInt(tokens[VCF.POS]);
				
				String preString = "";
				// padd 0 if not contained in first vcf file
				if (!genoMap.containsKey(pos)) {
					preString = tokens[VCF.REF] + "\t" + tokens[VCF.ALT] + padd(sampleNum, "");
				} else if (genoMap.containsKey(pos)) {
					preString = genoMap.get(pos) + padd(sampleNum, genoMap.get(pos));
				}
				
				for (int i = VCF.SAMPLE; i < tokens.length; i++) {
					formatTable = VCF.parseFormatSample(tokens[VCF.FORMAT], tokens[i]);
					// GT == NA
//					if (formatTable.containsKey("GT")) {
//						int genotype = Integer.parseInt(formatTable.get("GT"));
//						if (genotype == -1)	{
//							if (genoMap.containsKey(pos)) {
//								genoMap.remove(pos);
//							}
//							continue READ_LINE;
//						}
//					}
					// genotype quality (GQ) < 17
					if (formatTable.containsKey("GQ")) {
						int gq = Integer.parseInt(formatTable.get("GQ"));
						int genotype = Integer.parseInt(formatTable.get("GT"));
						if (gq < 17)	{
							preString = preString + (--genotype);
						} else {
							preString = preString + formatTable.get("GT");
						}
					}
				}
				
				if (pos < chrMin.get(chr)) {
					chrMin.put(chr, pos);
				}
				if (pos > chrMax.get(chr)) {
					chrMax.put(chr, pos);
				}
				genoMap.put(pos, preString);
			}
			//isFirst = false;
		}
		
		int totalVars = 0;
		for (String chr : chrVec) {
			int vars = 0;
			HashMap<Integer, String> genMap = genotypeMap.get(chr);
			for (int i = chrMin.get(chr); i <= chrMax.get(chr); i++) {
				if (genMap.containsKey(i) && isCommon(genMap.get(i))) {
					fm.writeLine(chr + "\t" + i + "\t" + i + "\t" + genMap.get(i));
					vars++;
					totalVars++;
				}
			}
			System.out.println(chr + " snps\t" + vars);
		}
		System.out.println("Total # of SNVs\t" + totalVars);
	}

	private String padd(int sampleNum, String value) {
		String[] tokens = value.split("\t");
		int numVals = 0;
		if (tokens.length > 2) {
			numVals = tokens.length - 2;
		}
		String padds = "\t";
		if (sampleNum - 1 > numVals) {
			for (int i = 0; i < sampleNum - 1 - numVals; i++) {
				padds = padds + "0\t";
			}
		}
		return padds;
	}

	private boolean isCommon(String line) {
		String[] tokens = line.split("\t");
		if (tokens.length - 2 == this.getNumFiles()) {
			return true;
		}
		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			new MultipleVcfToSnpTable().printHelp();
		} else {
			new MultipleVcfToSnpTable().go(args);
		}

	}

}
