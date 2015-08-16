/**
 * 
 */
package javax.arang.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import javax.arang.IO.BinaryIFileOwrapper;
import javax.arang.IO.bambasic.BinaryReader;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.bed.util.Bed;

/**
 * @author Arang Rhie
 *
 */
public class Coverage extends BinaryIFileOwrapper {

	private static boolean hasBed = false;
	private static String bedFile = "";
	private Vector<Float> cov1 = new Vector<Float>();
	private Vector<Float> cov2 = new Vector<Float>();
	long totalDepthSum = 0;
	long totalCoveredBasesSum = 0;
	long totalTargetBasesSum = 0;
	
	/* (non-Javadoc)
	 * @see javax.arang.IO.BinaryIFileOwrapper#hooker(javax.arang.IO.BinaryReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(BinaryReader br, FileMaker fm) {

		Bed bed = null;
		if (hasBed) {
			FileReader bedFr = new FileReader(bedFile);
			System.out.println("Processing file " + bedFr.getFileName());
			bed = new Bed(bedFr);
			bedFr.closeReader();
		}
		int refLen = br.readByte();
		String prevChr = "";
		long startPos = 0;
		long prevPos = 0;
		long depthSum = 0;
		int coveredBases = 0;
		
		System.out.println("Starting to calculate coverage...");
		writeHeader(fm);

		if (hasBed) {
			// hasBed = true
			ArrayList<Integer> starts = null;
			ArrayList<Integer> ends = null;
			int bedIdx = 0;
			boolean isInBedRegion = false;
			boolean skipChr = false;
			
			while (refLen != -1) {
				String chr = br.readChars(refLen);
				int pos = br.readInt();
				//System.out.println(chr + " " + pos);
				if (!prevChr.equals(chr)) {
					if (isInBedRegion) {
						writeCoverage(fm, prevChr, starts.get(bedIdx), ends.get(bedIdx), depthSum, coveredBases);
						isInBedRegion = false;
						
					}
					starts = bed.getStarts(chr);
					if (starts == null) {
						skipChr = true;
						prevChr = chr;
						br.skipBytes(5 * 4);
						refLen = br.readByte();
						continue;
					}
					ends = bed.getEnds(chr);
					bedIdx = 0;
					depthSum = 0;
					coveredBases = 0;
					startPos = pos;
					skipChr = false;
				}
				if (skipChr) {
					br.skipBytes(5 * 4);
					refLen = br.readByte();
					continue;
				}
				if (pos <= starts.get(bedIdx)) {
					// do nothing
					isInBedRegion = false;
					br.skipBytes(5 * 4);
				} else if (pos > starts.get(bedIdx) && pos < ends.get(bedIdx)) {
					isInBedRegion = true;
					coveredBases++;
					for (int i = 0; i < 5; i++) {
						depthSum += br.readInt();
					}
				} else if (pos >= ends.get(bedIdx)) {
					if (isInBedRegion) {
						writeCoverage(fm, prevChr, starts.get(bedIdx), ends.get(bedIdx), depthSum, coveredBases);
						isInBedRegion = false;
					} else {
						writeCoverage(fm, chr, starts.get(bedIdx), ends.get(bedIdx), 0, 0);
					}
					totalDepthSum += depthSum;
					totalCoveredBasesSum += coveredBases;
					totalTargetBasesSum += (ends.get(bedIdx) - starts.get(bedIdx));
					depthSum = 0;
					coveredBases = 0;
					bedIdx++;
					if (bedIdx >= starts.size()) {
						skipChr = true;
						br.skipBytes(5 * 4);
					}
					else if (pos > starts.get(bedIdx)) {
						isInBedRegion = true;
						coveredBases++;
						for (int i = 0; i < 5; i++) {
							depthSum += br.readInt();
						}
					} else {
						br.skipBytes(5 * 4);
					}
				}
				prevChr = chr;
				refLen = br.readByte();
			}
			if (isInBedRegion) {
				writeCoverage(fm, prevChr, starts.get(bedIdx), ends.get(bedIdx), depthSum, coveredBases);
			}
		} else {
			// hasBed = false
			while (refLen != -1) {
				String chr = br.readChars(refLen);
				int pos = br.readInt();
				if (prevChr.equals("")) {
					startPos = pos;
				}else if (!prevChr.equals(chr)) {
					depthSum = 0;
					coveredBases = 0;
					writeCoverage(fm, prevChr, startPos, prevPos, depthSum, coveredBases);
					startPos = pos;
				}
				coveredBases++;
				for (int i = 0; i < 5; i++) {
					depthSum += br.readInt();
				}
				//System.out.println(pos + " " + coveredBases + " " + depthSum);
				prevChr = chr;
				prevPos = pos;
				refLen = br.readByte();
			}
			writeCoverage(fm, prevChr, startPos, prevPos, depthSum, coveredBases);
		}
		writeBasicStat(fm, "MeanDepth (DepthSum / CoveredBases)", cov1);
		writeBasicStat(fm, "MeanDepth (DepthSum / (End - Start)", cov2);
		writeTotalStat(fm, totalDepthSum, totalCoveredBasesSum, totalTargetBasesSum);
		System.out.println("Finished!");
	}
	
	/**
	 * @param fm
	 */
	public static void writeBasicStat(FileMaker fm, String type, Vector<Float> coverage) {
		Object[] cov1Arr = coverage.toArray();
		Arrays.sort(cov1Arr);
		float covSum = 0f;
		for (float cov : coverage) {
			covSum += cov;
		}
		
		fm.writeLine(type + "\tmin:\t" + String.format("%,.3f", cov1Arr[0])
				+ "\tq1:\t" + String.format("%,.3f", cov1Arr[coverage.size()/4])
				+ "\tq2(median):\t" + String.format("%,.3f", cov1Arr[coverage.size()/2])
				+ "\tq3:\t" + String.format("%,.3f", cov1Arr[3*(coverage.size()/4)])
				+ "\tmax:\t" + String.format("%,.3f", cov1Arr[coverage.size() - 1])
				+ "\tmean:\t" + String.format("%,.3f", (covSum / coverage.size())));
	}
	
	public static void writeTotalStat(FileMaker fm, long depthSum, long coveredBasesSum, long targetBasesSum) {
		fm.writeLine("Total"
				+ "\tdepth:\t" + String.format("%,d", depthSum)
				+ "\tcovered:\t" + String.format("%,d", coveredBasesSum)
				+ "\ttarget:\t" + String.format("%,d", targetBasesSum)
				+ "\tcovered bases coverage:\t" + String.format("%,.3f", (depthSum / (float)coveredBasesSum))
				+ "\ttarget coverage:\t" + String.format("%,.3f", (depthSum / (float)targetBasesSum)));
	}
	
	public static void writeHeader(FileMaker fm) {
		fm.writeLine("Chr\tStart\tEnd\tDepthSum\tCoveredBases\tMeanDepth (DepthSum / CoveredBases)\tMeanDepth (DepthSum / (End - Start))");
	}
	
	private void writeCoverage(FileMaker fm, String chr, long start, long end, long depthSum, float coveredBases) {
		if (depthSum == 0) {
			fm.writeLine(chr + "\t" + start + "\t" + end + "\t" + depthSum + "\t" + (long)coveredBases + "\t0.0\t0.0");
			cov1.add(0f);
			cov2.add(0f);
		} else {
			fm.writeLine(chr + "\t" + start + "\t" + end + "\t" + depthSum + "\t" + (long)coveredBases + "\t"
					+ String.format("%,.3f", (depthSum / coveredBases)) + "\t"
					+ String.format("%,.3f", (depthSum / (float)(end - start))));
			cov1.add((depthSum / coveredBases));
			cov2.add(depthSum / (float)(end - start));
		}
		// save for total
	}
	
	public static void addCoverage(Vector<Float> cov, long depthSum, float bases) {
		if (depthSum == 0) {
			cov.add(0f);
		} else {
			cov.addElement(depthSum / bases);
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.arang.IO.BinaryIFileOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar baseCoverage.jar <in.base> <out.cov> [region.bed]");
		System.out.println("\t<in.base>: binary file.");
		System.out.println("\t\t(byte)chrLen\t(char)chr\t(int)pos\t(int)(A C G T D)");
		System.out.println("\t<out.cov>: chr\tstart\tend\tmean");
		System.out.println("Arang Rhie, 2014-03-14. arrhie@gmail.com");
	}

	public final static short CHROM = 0;
	public final static short START = 1;
	public final static short END = 2;
	public final static short DEPTH_SUM = 3;
	public final static short COVERED_BASES = 4;
	public final static short MEAN_DEPTH_OVER_COVERED_BASES = 5;
	public final static short MEAN_DEPTH_OVER_TARGET = 6;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new Coverage().go(args[0], args[1]);
		} else if (args.length == 3) {
			hasBed = true;
			bedFile = args[2];
			new Coverage().go(args[0], args[1]);
		} else {
			new Coverage().printHelp();
		}
	}

}
