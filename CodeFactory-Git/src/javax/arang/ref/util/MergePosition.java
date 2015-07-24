package javax.arang.ref.util;

import java.util.HashMap;
import java.util.StringTokenizer;

import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class MergePosition {
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String fastaFile = "C://Documents and Settings/아랑/바탕 화면/mito/ref/035233_D_DNAFront_BCBottom_20110630.fasta";
		String bedFile = "C://Documents and Settings/아랑/바탕 화면/mito/ref/035233_D_BED_20110630.bed";
		new MergePosition().mergePositions(fastaFile, bedFile);
	}

	public void mergePositions(String fastaFile, String bedFile) {
		FileReader fastaFr = new FileReader(fastaFile);
		FileReader bedFr = new FileReader(bedFile);
		String path = fastaFile.substring(0, fastaFile.lastIndexOf("/"));
		String fileName = "mitocon_hg19.fasta";
		FileMaker fm = new FileMaker(path, fileName);
		
		HashMap<String, String> idTable = new HashMap<String, String>();
		
		String line;
		StringTokenizer st;
		while (bedFr.hasMoreLines()) {
			line = bedFr.readLine().toString();
			st = new StringTokenizer(line);
			String position = st.nextToken() + "_" + st.nextToken() + "-" + st.nextToken();
			idTable.put(st.nextToken(), position);
		}
		
		while (fastaFr.hasMoreLines()) {
			line = fastaFr.readLine().toString();
			st = new StringTokenizer(line);
			fm.write(st.nextToken());	// ">"
			String id = st.nextToken();
			fm.write(idTable.get(id)+ "_" + id + "_");
			fm.writeLine(st.nextToken() + st.nextToken());
			fm.writeLine(fastaFr.readLine().toString());
		}
	}
}
