package javax.arang.phasing;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.Hap;
import javax.arang.phasing.util.Haps;
import javax.arang.phasing.util.PhasedBlock;
import javax.arang.snp.SNP;

public class HapsHomBlockLen20kbCount extends IOwrapper {

	private static int MIN_HOM_BLOCK_LEN_BOUND = 20000;

	@Override
	public void hooker(FileReader frHap, FileMaker fm) {
		
		FileReader frSample = new FileReader(samplePath);
		String line;
		String[] tokens;
		int pos;
		SNP snp;
		String sample;
		int snpCount = 0;
		int prevPos = 0;
		boolean isFirstHap = true;
		int HapA;
		int HapB;
		PhasedBlock block;
		
		if (!isHaps) {

			FileReader frLegend = new FileReader(legendPath);
			frLegend.readLine();	// header

			HashMap<Integer, SNP> snpPosToSNPmap = new HashMap<Integer, SNP>();
			ArrayList<Integer> posList = new ArrayList<Integer>();
			while (frLegend.hasMoreLines()) {
				line = frLegend.readLine();
				tokens = line.split(RegExp.WHITESPACE);
				pos = Integer.parseInt(tokens[Hap.POS]);
				snp = new SNP(pos, tokens[Hap.ALLELE_A], tokens[Hap.ALLELE_B], tokens[Hap.RS_ID]);
				snpPosToSNPmap.put(pos, snp);
				posList.add(pos);
			}
			System.out.println(posList.size() + " snps");

			ArrayList<String> sampleList = new ArrayList<String>();
			HashMap<String, Integer> sampleToCountMap = new HashMap<String, Integer>();
			
			fm.writeLine(frSample.readLine() + "\t" + frLegend.getFileName());
			
			while (frSample.hasMoreLines()) {
				sample = frSample.readLine().trim();
				sampleList.add(sample);
				sampleToCountMap.put(sample, 0);
			}
			int numSamples = sampleList.size();
			System.out.println(numSamples + " samples");

			ArrayList<PhasedBlock> blockListBySample = new ArrayList<PhasedBlock>();
			for (int i = 0; i < numSamples; i++) {
				blockListBySample.add(new PhasedBlock(sampleList.get(i), 0, 0, "Hom"));
			}
			
			while (frHap.hasMoreLines()) {
				line = frHap.readLine();
				tokens = line.split(RegExp.WHITESPACE);
				pos = posList.get(snpCount);

				// init prevPos
				if (isFirstHap) {
					prevPos = pos;
					isFirstHap = false;
					for (int i = 0; i < numSamples; i++) {
						block = blockListBySample.get(i);
						block.setBlockStart(pos);
						block.setBlockEnd(pos);
					}
				}

				// Get blocks on each sample
				for (int i = 0; i < numSamples; i++) {
					HapA = i*2;
					HapB = i*2 + 1;
					block = blockListBySample.get(i);
					sample = sampleList.get(i);

					// SNP distance > 19kb: split it here
					if (pos - prevPos > MIN_HOM_BLOCK_LEN_BOUND - 1000) {
						if (block.getLen() > MIN_HOM_BLOCK_LEN_BOUND) {
							sampleToCountMap.put(sample, sampleToCountMap.get(sample) + 1);
						}
						block.setMarked(false);
						block.setBlockStart(pos);
						block.setBlockEnd(pos); 
					}
					if (tokens[HapA].equals(tokens[HapB])) {
						// Hom
						block.setBlockEnd(pos);
						block.setMarked(true);
					} else {
						// Het
						block.setBlockEnd(pos); 
						if (block.getLen() > MIN_HOM_BLOCK_LEN_BOUND) {
							sampleToCountMap.put(sample, sampleToCountMap.get(sample) + 1);
						}
						block.setMarked(false);
						block.setBlockStart(pos);
					}
				}
				prevPos = posList.get(snpCount);
				snpCount++;
			}
			frHap.closeReader();

			for (int i = 0; i < numSamples; i++) {
				// add the last un-added block info, if any
				block = blockListBySample.get(i);
				sample = sampleList.get(i);
				if (block.isMarked() && (block.getLen() > MIN_HOM_BLOCK_LEN_BOUND)) {
					sampleToCountMap.put(sample, sampleToCountMap.get(sample) + 1);
				}
				fm.writeLine(sampleList.get(i) + "\t" + sampleToCountMap.get(sampleList.get(i)));
			}
		} else {
			// for AK1
			
			// Read .sample
			sample = frSample.readLine().trim();
			fm.writeLine(sample + "\t" + frHap.getFileName());
			
			// Read .haps
			int start = 0;
			int end = 0;
			int len = 0;
			int count = 0;
			boolean isMarked = false;
			isFirstHap = true;
			while (frHap.hasMoreLines()) {
				line = frHap.readLine();
				tokens = line.split(RegExp.WHITESPACE);
				pos = Integer.parseInt(tokens[Haps.POS]);
				if (isFirstHap) {
					prevPos = pos;
					isFirstHap = false;
					start = pos;
					end = pos;
				}
				// If snp distance > 19kb: Split it
				if (pos - prevPos > MIN_HOM_BLOCK_LEN_BOUND - 1000) {
					len = end - start;
					if (len > MIN_HOM_BLOCK_LEN_BOUND) {
						count++;
					}
					//System.out.println("[DEBUG] :: len of hom: " + len);
					isMarked = false;
					start = pos;
					end = pos;
				}
				if (tokens[Haps.HAPLOTYPE_A].equals(tokens[Haps.HAPLOTYPE_B])) {
					// Hom
					end = pos;
					isMarked = true;
				} else {
					// Het
					end = pos;
					len = end - start;
					if (isMarked) {
						if (len > MIN_HOM_BLOCK_LEN_BOUND) {
						count++;
						}
						//System.out.println("[DEBUG] :: len of hom: " + len);
					}
					start = pos;
					isMarked = false;
				}
				prevPos = pos;
			}
			
			if (isMarked) {
				if (len > MIN_HOM_BLOCK_LEN_BOUND) {
					count++;
				}
			}
			sample = frSample.readLine().trim();
			fm.writeLine(sample + "\t" + count);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingHapsHomBlockLen20kbCount.jar <in.sample> <in.chr.legend> <in.chr.hap> <out.sample.chr>");
		System.out.println("\t\tor java -jar phasingHapsHomBlockLen20kbCount.jar <in.sample> <in.chr.haps> <out.sample.chr>");
		System.out.println("\tCounts the number of homozygote blocks > 20kb.");
		System.out.println("\t<out.sample.chr>: <in.sample> with 1 more column containing [block counts] with block len > 20kb.");
		System.out.println("Arang Rhie, 2015-08-04. arrhie@gmail.com");
	}

	private static String samplePath = "";
	private static String legendPath = "";
	private static boolean isHaps = false;

	public static void main(String[] args) {
		if (args.length == 4) {
			samplePath = args[0];
			legendPath = args[1];
			new HapsHomBlockLen20kbCount().go(args[2], args[3]);
		} else if (args.length == 3) {
			isHaps = true;
			samplePath = args[0];
			new HapsHomBlockLen20kbCount().go(args[1], args[2]);
		} else {
			new HapsHomBlockLen20kbCount().printHelp();
		}
	}

}
