package javax.arang.gene.exp;

import java.util.StringTokenizer;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class SortUIgenes extends IOwrapper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		StringTokenizer st;
		while (fr.hasMoreLines()) {
			line = fr.readLine().toString();
			st = new StringTokenizer(line);
			String gene = st.nextToken();
			st.nextToken();	// gene length
			int exonCount = Integer.parseInt(st.nextToken());	// exon count
			String chr = st.nextToken();	// chr
			String starts = st.nextToken();	// starts
			String ends = st.nextToken();	// ends
			StringTokenizer start = new StringTokenizer(starts, ",");
			StringTokenizer end = new StringTokenizer(ends, ",");
			
			for (int i = 0; i < exonCount; i++) {
				fm.writeLine(chr + "\t" + start.nextToken() + "\t" + end.nextToken() + "\t" + gene);
			}
		}
		
	}

	@Override
	public void printHelp() {
		// TODO Auto-generated method stub
		
	}

}
