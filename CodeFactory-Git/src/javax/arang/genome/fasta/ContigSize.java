package javax.arang.genome.fasta;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ContigSize extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String contigId = "";
		int contigLen = 0;
		int contigNonNLen = 0;
		boolean isFirst = true;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith(">")) {
				if (!isFirst) {
					fm.writeLine(contigId + "\t" + contigLen + "\t" + contigNonNLen);
				}
				isFirst = false;
				contigId = line.replace(">", "").trim();
				contigLen = 0;
				contigNonNLen = 0;
			} else {
				contigLen += line.trim().length();
				contigNonNLen += countNonNs(line.trim());
			}
		}
		fm.writeLine(contigId + "\t" + contigLen + "\t" + contigNonNLen);
	}

	private int countNonNs(String faSeq) {
		int seqLen = faSeq.length();
		int numN = 0;
		for (int i = 0; i < seqLen; i++) {
			if (faSeq.charAt(i) == 'N' || faSeq.charAt(i) == 'n') {
				numN++;
			}
		}
		return seqLen - numN;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaContigSize.jar <in.fa>");
		System.out.println("\t<in.fa.len>: contig_id\tlength\tnon-N length");
		System.out.println("Arang Rhie, 2015-04-03. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ContigSize().go(args[0], args[0] + ".len");
		} else {
			new ContigSize().printHelp();
		}

	}

}
