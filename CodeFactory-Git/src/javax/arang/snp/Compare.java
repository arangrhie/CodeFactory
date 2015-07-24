package javax.arang.snp;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;


public abstract class Compare extends I2Owrapper {
	
	protected FileReader fr1;
	protected FileReader fr2;
	protected FileMaker fm;

	protected String line1;
	protected String[] tokens1;
	
	protected String line2;
	protected String[] tokens2;
	/***
	 * Compare two SNP files according to their position
	 * @param fr1
	 * @param fr2
	 */
	public void compareTwoFiles(FileReader fr1, FileReader fr2, FileMaker fm) {
		
		this.fr1 = fr1;
		this.fr2 = fr2;
		this.fm = fm;
		
		line2 = fr2.readLine();
		if (line2.startsWith("CHR")) {
			chrLine2(line2);
			line2 = fr2.readLine();	
		}
		tokens2 = line2.split("\\s+");
		
		
		int chr1 = 0;
		int pos1 = 0;
		int chr2 = SNP.getChrIntVal(tokens2[SNP.CHR]);
		int pos2 = Integer.parseInt(tokens2[SNP.STOP]);
		
		FR1_LOOP: while (fr1.hasMoreLines()) {
			line1 = fr1.readLine();
			if (line1.startsWith("#")) {
				continue;
			}
			if (line1.startsWith("CHR")) {
				chrLine1(line1);
				continue;
			}
			tokens1 = line1.split("\\s+");
			if (tokens1.length < 2) {
				break FR1_LOOP;
			}
			chr1 = SNP.getChrIntVal(tokens1[SNP.CHR]);
			if (chr1 == chr2) {
				pos1 = Integer.parseInt(tokens1[SNP.STOP]);
				if (pos1 == pos2) {
					fr1IsEqualToFr2(tokens1, tokens2);
					if (!fr2.hasMoreLines()) {
						break FR1_LOOP;
					}
					line2 = fr2.readLine();
					tokens2 = line2.split("\\s+");
					chr2 = SNP.getChrIntVal(tokens2[SNP.CHR]);
					pos2 = Integer.parseInt(tokens2[SNP.STOP]);
					continue FR1_LOOP;
				}
				
				else if (pos1 < pos2) {
					fr1IsLessThanFr2(tokens1, tokens2);
					continue FR1_LOOP;
				}
				else {
					while (pos2 < pos1) {
						fr2IsMoreThanFr1(tokens1, tokens2);
						if (!fr2.hasMoreLines()) {
							break FR1_LOOP;
						}
						line2 = fr2.readLine();
						tokens2 = line2.split("\\s+");
						chr2 = SNP.getChrIntVal(tokens2[SNP.CHR]);
						pos2 = Integer.parseInt(tokens2[SNP.STOP]);
						if (chr1 != chr2) continue FR1_LOOP;
					}
				}
			}
			else if (chr1 < chr2) {
				fr1IsLessThanFr2(tokens1, tokens2);
				continue FR1_LOOP;
			}
			else {
				while (chr2 < chr1) {
					fr2IsMoreThanFr1(tokens1, tokens2);
					if (!fr2.hasMoreLines()) {
						break FR1_LOOP;
					}
					line2 = fr2.readLine();
					tokens2 = line2.split("\\s+");
					chr2 = SNP.getChrIntVal(tokens2[SNP.CHR]);
					pos2 = Integer.parseInt(tokens2[SNP.STOP]);
				}
			}
		}
		
		//System.out.println("[DEBUG] chr1: " + chr1 + "\tpos1: " + pos1 + "\nchr2: " + chr2 + "\tpos2: " + pos2);

		if (isFr1ProceedToEnd()) {
			while (fr1.hasMoreLines()) {
				line1 = fr1.readLine();
				fr1ToTheEnd(line1);
			}
		}
		
		if (isFr2ProceedToEnd()) {
			while (fr2.hasMoreLines()) {
				line2 = fr2.readLine();
				fr2ToTheEnd(line2);
			}
		}
		
	}
	
	protected abstract void chrLine1(String line);
	protected abstract void chrLine2(String line);

	protected abstract void fr1IsLessThanFr2(String[] tokens1, String[] tokens2);
	
	protected abstract void fr2IsMoreThanFr1(String[] tokens1, String[] tokens2);
	
	protected abstract void fr1IsEqualToFr2(String[] tokens1, String[] tokens2);
	
	protected abstract void fr1ToTheEnd(String line1);
	
	protected abstract void fr2ToTheEnd(String line2);
	
	protected abstract boolean isFr1ProceedToEnd();
	protected abstract boolean isFr2ProceedToEnd();
	

}
