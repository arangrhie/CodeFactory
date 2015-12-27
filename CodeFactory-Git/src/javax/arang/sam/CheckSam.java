package javax.arang.sam;

import java.util.StringTokenizer;

import javax.arang.IO.basic.FileReader;

public class CheckSam {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String inFile = "C:\\\\Documents and Settings\\?•„?ž‘\\ë°”íƒ• ?™”ë©?\\FX\\sam_test.sam";
		new CheckSam().go(inFile);
	}
	
	public void go(String inFile) {
		FileReader fr = new FileReader(inFile);
		String line = "";
		while (fr.hasMoreLines()) {
			line = fr.readLine().toString();
			StringTokenizer st = new StringTokenizer(line);
			st.nextToken();
			Integer flag = Integer.parseInt(st.nextToken());
			System.out.println(flag
					+ " isConcordant? " + SAMUtil.isConcordant(flag)
					+ " isUnpaired? " + SAMUtil.isUnpaired(flag)
					+ " isPair1? " + SAMUtil.isPair1(flag) + " isPiar2? " + SAMUtil.isPair2(flag)
					+ " isSecondaryAlignment? " + SAMUtil.isSecondaryAlignment(flag)
					+ " isNotPassingQualityControls " + SAMUtil.isUnderQual(flag)
					+ " isDup " + SAMUtil.isDuplicate(flag)
					);
			if (!SAMUtil.isConcordant(flag)) continue;
			st.nextToken();
			st.nextToken();
			st.nextToken();
			String cigar = st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			String mdTag = st.nextToken();
			mdTag = mdTag.substring(mdTag.lastIndexOf(":") + 1);
			int mismatchScore = SAMUtil.getMismatchScore(cigar, mdTag);
			System.out.println("cigar: " + cigar + " mdTag: " + mdTag + " mismatchScore: " + mismatchScore);
		}
		fr.closeReader();
	}
	
}
