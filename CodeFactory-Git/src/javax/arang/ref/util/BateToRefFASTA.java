package javax.arang.ref.util;

import java.util.StringTokenizer;

import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class BateToRefFASTA {
	
	static String path = "C://Documents and Settings/아랑/바탕 화면/3Platform/mitocon_bate/035233_1309497359366/TDT/035233_D_DNAFront_BCBottom_20110630/035233_D_DNAFront_BCBottom_20110630.txt";
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new BateToRefFASTA().makeFasta(path);
	}
	
	public void makeFasta(String filePath) {
		FileReader fr = new FileReader(filePath);
		String path = filePath.substring(0, filePath.lastIndexOf("/"));
		String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
		fileName = fileName.replaceAll(".txt", ".fasta");
		FileMaker fm = new FileMaker(path, fileName);

		fr.readLine();	// skip the 1st line
		
		String line;
		StringBuffer comment;
		String dna;
		while (fr.hasMoreLines()) {
			line = fr.readLine().toString();
			StringTokenizer st = new StringTokenizer(line);
			comment = new StringBuffer(st.nextToken());
			dna = st.nextToken();
			comment.append("\t" + st.nextToken());
			comment.append("\t" + st.nextToken());
			writeFasta(fm, comment.toString(), dna);
		}
		fr.closeReader();
		fm.closeMaker();
	}

	private void writeFasta(FileMaker fm, String comment, String dna) {
		fm.writeLine("> " + comment);
		fm.writeLine(dna);
	}

}
