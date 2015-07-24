package javax.arang.genome.sam;

import java.util.StringTokenizer;

import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ExtractIndel {
	
	public static void main(String[] args) {
		if (args.length > 0) {
			new ExtractIndel().go(args[0], args[1]);
		} else {
			String inFile = "C://Documents and Settings/�꾨옉/諛뷀깢 �붾㈃/ak6_chr20.indel.test.sam";
			String outFile = "ak6_chr20.indel.sam";
			new ExtractIndel().go(inFile, outFile);
		}
	}
	
	public void go(String inFile, String outFileName) {
		FileReader fr = new FileReader(inFile);
		String path = fr.getDirectory();
		FileMaker fm = new FileMaker(path, outFileName);
		
		String line;
		String prevId;
		String id = "";
		String indelLines = "";
		boolean hasIndel = false;
		while (fr.hasMoreLines()) {
			line = fr.readLine().toString();
			if (line.startsWith("@")) {
				continue;
			}
			line = line.trim();
			StringTokenizer st = new StringTokenizer(line);
			prevId = id;
			id = st.nextToken();	// id
			if (!id.equals(prevId)) {
				if (hasIndel) {
					writeLines(fm, indelLines);
					hasIndel = false;
				}
				indelLines = "";
			}
			indelLines = indelLines + line + "\n";
			st.nextToken();	// flag
			st.nextToken();	// aligned_reference_id
			st.nextToken();	// size_length
			st.nextToken();	// foward_reverse
			String cigar = st.nextToken();
			if (cigar.contains("I") || cigar.contains("D")) {
				hasIndel = true;
			}
		}
		
		if (!indelLines.isEmpty() && hasIndel) {
			writeLines(fm, indelLines);
		}
	}
	
	private void writeLines(FileMaker fm, String lines) {
		fm.write(lines);
	}

}
