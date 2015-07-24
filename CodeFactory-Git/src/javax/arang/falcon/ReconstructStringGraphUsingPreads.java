package javax.arang.falcon;

import java.util.ArrayList;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ReconstructStringGraphUsingPreads extends I2Owrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		ArrayList<String> preads = new ArrayList<String>();
		String line;
		
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			preads.add(line);
		}
		
		System.out.println("[DEBUG] :: " + preads.size() + " preads to include");
		
		String[] tokens;
		
		String edge = "";
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			edge = tokens[Falcon.NODE_BEGIN].substring(0, tokens[Falcon.NODE_BEGIN].indexOf(":"));
			if (preads.contains(edge)) {
				fm.writeLine(line);
			} else if (preads.contains(tokens[Falcon.EDGE])) {
				fm.writeLine(line);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar falconReconstructStringGraphUsingPreads.jar <pread.list> <ctg_tiling_path_coord_len> <new_tiling_path>");
		System.out.println("\t<pread.list>: List of preads (no :B :E) to include in new string graph");
		System.out.println("\t<ctg_tiling_path_coord_len>: cat p_ctg_tiling_path_coord_len a_ctg_tiling_path_coord_len");
		System.out.println("\t\t*generate *_ctg_tiling_path_coord_len with of falconAddCoordLen.jar");
		System.out.println("\t<new_tiling_path>: Reconstructed string graph with preads in <pread.list>");
		System.out.println("\t\t*Run falconInsertReducedEdges.jar to insert intermediate \'reduced\' edges will be added between tiling path");
		System.out.println("Arang Rhie, 2015-06-26. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new ReconstructStringGraphUsingPreads().go(args[0], args[1], args[2]);
		} else {
			new ReconstructStringGraphUsingPreads().printHelp();
		}
	}

}
