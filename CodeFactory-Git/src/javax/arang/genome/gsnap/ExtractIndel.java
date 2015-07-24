package javax.arang.genome.gsnap;

import java.util.StringTokenizer;

import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ExtractIndel {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			new ExtractIndel().go(args[0], args[1]);
		} else {
			String inFile = "C://Documents and Settings/아랑/바탕 화면/ak6_chr20.indel.result";
			String outFile = "ak6_chr20.indel.gsnap";
			new ExtractIndel().go(inFile, outFile);
		}
	}
	
	public void go(String inFile, String outFileName) {
		FileReader fr = new FileReader(inFile);
		String path = inFile.substring(0, inFile.lastIndexOf("/"));
		FileMaker fm = new FileMaker(path, outFileName);
		
		String line;
		String indelLines = "";
		boolean hasIndel = false;
		while (fr.hasMoreLines()) {
			line = fr.readLine().toString();
			line = line.trim();
			StringTokenizer st = new StringTokenizer(line);
			
			if (line.equals("")) continue;
			
			if (line.startsWith(">")) {
				if (hasIndel) {
					writeLines(fm, indelLines);
				}
				hasIndel = false;
				indelLines = "";
			}
			
			if (line.startsWith(">") || line.startsWith("<")) {
				if (indelLines.equals("")) {
					indelLines = indelLines + line + "\n";
				} else {
					indelLines = indelLines + "\n" + line + "\n";
				}
				continue;
			} else {
				st.nextToken();	// sequence
				st.nextToken();	// range
				st.nextToken();	// reference
				if (!line.startsWith(",")) {
					st.nextToken();	// sub
					String segs = st.nextToken();	// segs:2,align_score:5,mapq:0
					StringTokenizer seg = new StringTokenizer(segs, ":,");
					seg.nextToken();
					if (Integer.parseInt(seg.nextToken()) > 1) {
						hasIndel = true;
					}
				}
				indelLines = indelLines + line + "\n";
			}
		}
		
		if (!indelLines.isEmpty() && hasIndel) {
			writeLines(fm, indelLines);
		}
		
		fr.closeReader();
		fm.closeMaker();
	}
	
	private void writeLines(FileMaker fm, String lines) {
		fm.write(lines);
	}

}
