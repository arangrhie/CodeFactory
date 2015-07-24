package javax.arang.genome.indel;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.INwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.genome.base.Base;

public class IndelCall extends INwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar baseIndelCall.jar <sample.sam.red.indel.chr*> <sample.sorted.sam.red.bas.chr*>");
		System.out.println("<output> format is as follows:");
		System.out.println("\tchrom\tstart\tend\treference\tobserved\tins/del" +
				"			\tAQofINDEL\tMaxCountOfINDEL" +
				"			\tMaxAQofBases\tMaxCountOfBases\tratio\tHet/Hom");
	}
	
	int numCount = 0;
	final static int PADD = 100;
	final static int CAPACITY = 3000;

	@Override
	public void hooker(ArrayList<FileReader> frs) {
		
		ArrayList<FileReader> indelReaders = new ArrayList<FileReader>();
		ArrayList<FileReader> baseReaders = new ArrayList<FileReader>();
		
		for (int i = 0; i < frs.size()/2 - 1; i++){
			indelReaders.add(frs.get(i));
		}
		
		for (int i = frs.size()/2; i < frs.size(); i++) {
			baseReaders.add(frs.get(i));
		}
		
		String indelLine;
		String[] indelTokens;
		String baseLine;
		String[] baseTokens;
		
		HashMap<String, String[]> basePosMap = new HashMap<String, String[]>();
		    
		for (int i = 0; i < indelReaders.size(); i++) {
			FileReader indelFr = indelReaders.get(i);
			FileReader baseFr = baseReaders.get(i);
			FileMaker fm = new FileMaker("indel", indelFr.getFileName() + ".indel");
			basePosMap.clear();
			
			/***
			 * begin with |-100-start-|-end-1900-|
			 * add new bases from baseFr when |-1900-start-|-end-100-| is reached
			 */
			int begin = 0;
			int to = CAPACITY;
			
			READ_INDEL : while (indelFr.hasMoreLines()) {
				indelLine = indelFr.readLine();
				indelTokens = indelLine.split("\t");
				float qual = Float.parseFloat(indelTokens[Indel.QUAL_AVG]);
				if (qual < 20)	continue READ_INDEL;
				if (Integer.parseInt(indelTokens[Indel.TOTAL_COUNT]) < 5) continue READ_INDEL;
				
				int start = Integer.parseInt(indelTokens[Indel.START]);
				int end = Integer.parseInt(indelTokens[Indel.END]);
				
				// initialize basePosMap if its empty or
				// start is exceeding the basePosMap
				if (basePosMap.isEmpty() || start > to) {
					basePosMap.clear();
					begin = start - PADD;
					to = begin + CAPACITY;
					//System.out.println("initialize basePosMap on " + indelTokens[Indel.CHR] + " " + begin + "-" + to);
					READ_BASE : while (baseFr.hasMoreLines()) {
						baseLine = baseFr.readLine();
						baseTokens = baseLine.split("\t");
						int pos = Integer.parseInt(baseTokens[Base.POS]);
						if (pos < begin)	continue READ_BASE;
						else if (pos < to){
							// read 2000 base positions
							basePosMap.put(baseTokens[Base.POS], baseTokens);
						} else {
							break READ_BASE;
						}
					}
				}
				
				if (end > (to - PADD)) {
					// remove up to start - PADD
					for (int j = begin; j < (start - PADD); j++) {
						basePosMap.remove(j);
					}
					begin = start - PADD;
					to = begin + CAPACITY;
					
					// add up to new 'to'
					READ_BASE : while (baseFr.hasMoreLines()) {
						baseLine = baseFr.readLine();
						baseTokens = baseLine.split("\t");
						int pos = Integer.parseInt(baseTokens[Base.POS]);
						if (pos < begin)	continue READ_BASE;
						else if (pos < to){
							// read 2000 base positions
							basePosMap.put(baseTokens[Base.POS], baseTokens);
						} else {
							break READ_BASE;
						}
					}
				}
				
				try {
					if (indelTokens[Indel.TYPE].equals("ins")) {
						// INSERTION
						float leftQual = Float.parseFloat(basePosMap.get(indelTokens[Indel.START])[Base.QUAL_AVG]);
						float rightQual = Float.parseFloat(basePosMap.get(String.valueOf(end+1))[Base.QUAL_AVG]);
						float qualMax = Math.max(leftQual, rightQual);
						if (qualMax < 20)	continue READ_INDEL;
						int leftCount = Integer.parseInt(basePosMap.get(indelTokens[Indel.START])[Base.TOTAL_COUNT]);
						int rightCount = Integer.parseInt(basePosMap.get(String.valueOf(end + 1))[Base.TOTAL_COUNT]);
						float countMax = Math.max(leftCount, rightCount);
						float ratio = (100*Float.parseFloat(indelTokens[Indel.TOTAL_COUNT])) / countMax;
						if (ratio < 20)	continue READ_INDEL;
						fm.writeLine(getOutLine(indelLine, qualMax, countMax, ratio));
					} else {
						// DELETION
						float leftQual = Float.parseFloat(basePosMap.get(String.valueOf(start - 1))[Base.QUAL_AVG]);
						float rightQual = Float.parseFloat(basePosMap.get(String.valueOf(end+1))[Base.QUAL_AVG]);
						float qualMax = Math.max(leftQual, rightQual);
						if (qualMax < 20)	continue READ_INDEL;
						int leftCount = Integer.parseInt(basePosMap.get(String.valueOf(start - 1))[Base.TOTAL_COUNT]);
						int rightCount = Integer.parseInt(basePosMap.get(String.valueOf(end + 1))[Base.TOTAL_COUNT]);
						float countMax = Math.max(leftCount, rightCount);
						float ratio = (100*Float.parseFloat(indelTokens[Indel.TOTAL_COUNT])) / countMax;
						if (ratio < 20)	continue READ_INDEL;
						fm.writeLine(getOutLine(indelLine, qualMax, countMax, ratio));
					}
				} catch (NullPointerException e) {
					e.getMessage();
					System.out.println(indelTokens[Indel.START]);
					throw e;
				}
				numCount++;	
				
			}
			
			System.out.println(indelFr.getFileName() + " completed");
			fm.closeMaker();
			basePosMap.clear();
		}
		System.out.println("# of Indels\t" + numCount );
	}

	private String getOutLine(String indelLine, float qualMax, float countMax, float ratio) {
		String line = "";
		if (ratio > 80) {
			line = indelLine + "\t" + qualMax + "\t" + countMax + "\t" + String.format("%,.2f", ratio) + "\t" + "Hom";
		} else {
			line = indelLine + "\t" + qualMax + "\t" + countMax + "\t" + String.format("%,.2f", ratio) + "\t" + "Het";
		}
		return line;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			new IndelCall().printHelp();
		} else {
			new IndelCall().go(args);
		}
	}

}
