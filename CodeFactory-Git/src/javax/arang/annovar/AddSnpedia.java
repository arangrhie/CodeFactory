/**
 * 
 */
package javax.arang.annovar;

import java.util.HashMap;
import java.util.Vector;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.annovar.util.ANNOVAR;
import javax.arang.snpedia.SNPEDIA;

/**
 * @author Arang Rhie
 *
 */
public class AddSnpedia extends R2wrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.INwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar annovarAddSnpedia.jar <in.annotated> <snpedia_rs.txt>" +
				" <num_samples> <column_name_of_rsID=snp138> [sample_start_column=7, 0-based]");
		System.out.println("\t<in.annotated>: (with header) KEY\tCHR\tSTART\tSTOP\tREF\tALT\tID\tSample_1\t...");
		System.out.println("\t<snpedia_rs.txt>: (no header) rsID(GT1;GT2)\tMagnitude\tRepute\tSummary");
		System.out.println("\t<output>: <num_samples> of files will be generated with individual genotype.");
		System.out.println("Arang Rhie, 2014-01-23. arrhie@gmail.com");
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.INwrapper#hooker(java.util.ArrayList)
	 */
	@Override
	public void hooker(FileReader fr1, FileReader fr2) {
		// fr1 = <in.annotated>
		// fr2 = <snpedia_rs.txt>
		
		String line;
		String tokens[];
		
		
		// parse <snpedia_rs.txt>
		HashMap<String, String>	snpediaDisc = new HashMap<String, String>();	// rsID(GT1;GT2) : magnitude+repute+summary
		
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			tokens = line.split("\t");
			if (tokens.length > 3) {
				snpediaDisc.put(tokens[SNPEDIA.RS_ID_GT], tokens[SNPEDIA.MAGNITUDE]
										+ "\t" + tokens[SNPEDIA.REPUTE]
										+ "\t" + tokens[SNPEDIA.SUMMARY]);
				//System.out.println(":: DEBUG :: tokens[SNPEDIA.RS_ID_GT]: " + tokens[SNPEDIA.RS_ID_GT]);
			} else {
				snpediaDisc.put(tokens[SNPEDIA.RS_ID_GT], tokens[SNPEDIA.MAGNITUDE]
						+ "\t" + tokens[SNPEDIA.REPUTE]);
			}
		}
		
		System.out.println(":: INFO :: " + snpediaDisc.size() + " rsID(GT1;GT2) read from " + fr2.getFileName());
		
		// read fr1 header line, generate fms for each sample
		Vector<FileMaker> fms = new Vector<FileMaker>();
		line = fr1.readLine();
		tokens = line.split("\t");
		for (int i = 0; i < numSamples; i++) {
			//FileMaker fm = new FileMaker(fr1.getDirectory(), tokens[sampleStartColumn + i] + ".snpedia");
			FileMaker fm = new FileMaker(fr1.getDirectory(), fr1.getFileName().substring(0, fr1.getFileName().indexOf(".")) + ".snpedia");
			System.out.println(":: INFO :: Write output into " + fm.getFileName());
			StringBuffer lineCopy = new StringBuffer(line);
			sampleStartColumn = tokens.length - 1;	// Temporally - > export to annovarAddSnpediaToAVOUT.jar
//			StringBuffer lineCopy = new StringBuffer(tokens[ANNOTATED.CHR] + "\t" + tokens[ANNOTATED.POS_FROM] + "\t" + tokens[ANNOTATED.POS_TO]
//					 + "\t" + tokens[ANNOTATED.REF] + "\t" + tokens[ANNOTATED.ALT] + "\t" + tokens[sampleStartColumn + i]);
//			if (tokens.length > sampleStartColumn + numSamples) {
//				for (int j = numSamples + sampleStartColumn ; j < tokens.length; j++) {
//					lineCopy.append("\t" + tokens[j]);
//				}
//			}
			fm.writeLine(lineCopy.toString() + "\tSNPEDIA_GT\tMAGNITUDE\tREPUTE\tSUMMARY");
			fms.add(fm);
		}
		//
		sampleStartColumn=6;
//		if (!tokens[ANNOVAR.NOTE].equals(columnNameOfRsID)) {
			for (int i = sampleStartColumn + numSamples; i < tokens.length; i++) {
				if (tokens[i].equals(columnNameOfRsID)) {
					columnNumOfRsID = i;
					break;
				}
			}
//		}
		System.out.println(":: INFO :: Column num of " + columnNameOfRsID + " : " + columnNumOfRsID);
		sampleStartColumn = tokens.length - 1;	// Temporally - > export to annovarAddSnpediaToAVOUT.jar
		
		String rsIdKey = "";
		
		// read fr1, write for each sample's fm
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			tokens = line.split("\t");
			rsIdKey = "";
			for (int i = 0; i < numSamples; i++) {
				StringBuffer lineCopy = new StringBuffer(line);
//				StringBuffer lineCopy = new StringBuffer(tokens[ANNOTATED.CHR] + "\t" + tokens[ANNOTATED.POS_FROM] + "\t" + tokens[ANNOTATED.POS_TO]
//						+ "\t" + tokens[ANNOTATED.REF] + "\t" + tokens[ANNOTATED.ALT] + "\t" + tokens[sampleStartColumn + i]);
//				if (tokens.length > sampleStartColumn + numSamples) {
//					for (int j = numSamples + sampleStartColumn ; j < tokens.length; j++) {
//						lineCopy.append("\t" + tokens[j]);
//					}
//				}
				// Hom
				if (tokens[sampleStartColumn + i].equals("2")) {
					rsIdKey = SNPEDIA.getRsIdGt(tokens[columnNumOfRsID], tokens[ANNOVAR.ALT], tokens[ANNOVAR.ALT]);
					//System.out.println(rsIdKey);
					if (snpediaDisc.containsKey(rsIdKey)) {
						fms.get(i).writeLine(lineCopy.toString() + "\t" + rsIdKey + "\t" + snpediaDisc.get(rsIdKey));
						continue;
//					} else {
//						rsIdKey = SNPEDIA.getComplement(rsIdKey);
//						if (snpediaDisc.containsKey(rsIdKey)) {
//							fms.get(i).writeLine(lineCopy.toString() + "\t" + rsIdKey + "\t" + snpediaDisc.get(rsIdKey));
//							continue;
//						}
					}
				}
				// Het
				else if (tokens[sampleStartColumn + i].equals("1")) {
					if (tokens[ANNOVAR.REF].charAt(0) < tokens[ANNOVAR.ALT].charAt(0)) {
						rsIdKey = SNPEDIA.getRsIdGt(tokens[columnNumOfRsID], tokens[ANNOVAR.REF], tokens[ANNOVAR.ALT]);
						if (snpediaDisc.containsKey(rsIdKey)) {
							fms.get(i).writeLine(lineCopy.toString() + "\t" + rsIdKey + "\t" + snpediaDisc.get(rsIdKey));
							continue;
						}
					} else {
						rsIdKey = SNPEDIA.getRsIdGt(tokens[columnNumOfRsID], tokens[ANNOVAR.ALT], tokens[ANNOVAR.REF]);
						if (snpediaDisc.containsKey(rsIdKey)) {
							fms.get(i).writeLine(lineCopy.toString() + "\t" + rsIdKey + "\t" + snpediaDisc.get(rsIdKey));
							continue;
						}
					}
				}
				// WildType
				else if (tokens[sampleStartColumn + i].equals("0")) {
					rsIdKey = SNPEDIA.getRsIdGt(tokens[columnNumOfRsID], tokens[ANNOVAR.REF], tokens[ANNOVAR.REF]);
					if (snpediaDisc.containsKey(rsIdKey)) {
						fms.get(i).writeLine(lineCopy.toString() + "\t" + rsIdKey + "\t" + snpediaDisc.get(rsIdKey));
						continue;
					} else {
//						rsIdKey = SNPEDIA.getComplement(rsIdKey);
//						if (snpediaDisc.containsKey(rsIdKey)) {
//							fms.get(i).writeLine(lineCopy.toString() + "\t" + rsIdKey + "\t" + snpediaDisc.get(rsIdKey));
//							continue;
//						} else {						
							rsIdKey = SNPEDIA.getRsIdGt(tokens[columnNumOfRsID], "-", "-");
							if (snpediaDisc.containsKey(rsIdKey)) {
								fms.get(i).writeLine(lineCopy.toString() + "\t" + rsIdKey + "\t" + snpediaDisc.get(rsIdKey));
								continue;
							}
//						}
					}
				}
				// NA
				else if (tokens[sampleStartColumn + i].equals("NA")) {
					// do nothing
				}
			}
			
		}
	}

	private static int numSamples = 1;
	private static int sampleStartColumn = ANNOTATED.SAMPLE_START;
	private static String columnNameOfRsID = "snp138";
	private static int columnNumOfRsID = ANNOTATED.NOTE;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 3) {
			numSamples = Integer.parseInt(args[2]);
			columnNameOfRsID = args[3];
			if (args.length == 5) {
				sampleStartColumn = Integer.parseInt(args[4]);
			}
			new AddSnpedia().go(args[0], args[1]);
		} else {
			new AddSnpedia().printHelp();
		}

	}

}
