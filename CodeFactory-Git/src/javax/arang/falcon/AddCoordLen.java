package javax.arang.falcon;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class AddCoordLen extends I2Owrapper {

	@Override
	public void hooker(FileReader frCtgTiling, FileReader frFaLen, FileMaker fm) {
		String line;
		String[] tokens;
		
		HashMap<String, String> faLenMap = new HashMap<String, String>();
		
		while (frFaLen.hasMoreLines()) {
			line = frFaLen.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			faLenMap.put(tokens[0], tokens[tokens.length - 1]);
		}
		System.out.println("[DEBUG] :: number of contigs: " + faLenMap.size());
		
		String contig = "";
		int faIdxFrom = 0;
		int faIdxTo = 0;
		int edgeStart;
		int edgeEnd;
		int len;
		while (frCtgTiling.hasMoreLines()) {
			line = frCtgTiling.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			edgeStart = Integer.parseInt(tokens[Falcon.EDGE_START]);
			edgeEnd = Integer.parseInt(tokens[Falcon.EDGE_END]);
			len = Math.max(edgeStart, edgeEnd) - Math.min(edgeStart, edgeEnd);
			if (!contig.equals(tokens[Falcon.CONTIG])) {
				// new contig begins: initialize variables
				faIdxFrom = 0;
				contig = tokens[Falcon.CONTIG];
			}
			faIdxTo = faIdxFrom + len - 1;
			fm.writeLine(line + "\t" + faIdxFrom + "\t" + faIdxTo + "\t" + faLenMap.get(contig));
			faIdxFrom = faIdxTo + 1;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar falconAddCoordLen.jar <ctg_tiling_path> <ctg.fa.len> <ctg_tiling_path_ccord_len>");
		System.out.println("\t<ctg_tiling_path>: tiling path file of falcon");
		System.out.println("\t<ctg.fa.len>: output file of fastaContigSize.jar <ctg.fa>");
		System.out.println("\t<ctg_tiling_path_coord_len>: 3 columns added: start, end coord of <ctg.fa> and length of total contig from ctg.fa.len");
		System.out.println("Arang Rhie, 2015-06-25. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new AddCoordLen().go(args[0], args[1], args[2]);
		} else {
			new AddCoordLen().printHelp();
		}
	}

}
