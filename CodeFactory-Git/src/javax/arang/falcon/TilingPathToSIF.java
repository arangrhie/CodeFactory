package javax.arang.falcon;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class TilingPathToSIF extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		String prevLine = fr.readLine();
		String[] prevTokens = prevLine.split(RegExp.WHITESPACE);
		String prevContig = prevTokens[Falcon.CONTIG];
		String prevEdge = prevTokens[Falcon.EDGE];
		String orientPrevEdge = prevTokens[Falcon.NODE_END].substring(prevTokens[Falcon.NODE_END].indexOf(":") + 1);
		int prevContigEndIdx = Integer.parseInt(prevTokens[Falcon.CONTIG_END]);
		String nodeStart = prevTokens[Falcon.NODE_BEGIN].substring(0, prevTokens[Falcon.NODE_BEGIN].indexOf(":"));
		String orientNodeStart = prevTokens[Falcon.NODE_BEGIN].substring(prevTokens[Falcon.NODE_BEGIN].indexOf(":") + 1);
		String orientNodeEnd = prevTokens[Falcon.NODE_END].substring(prevTokens[Falcon.NODE_END].indexOf(":") + 1);
		int contigStartIdx = Integer.parseInt(prevTokens[Falcon.CONTIG_START]);
		int contigEndIdx = Integer.parseInt(prevTokens[Falcon.CONTIG_END]);
		int len = contigEndIdx - contigStartIdx + 1;
		fm.writeLine(nodeStart + "\t" + prevTokens[Falcon.CONTIG] + "_" + orientNodeStart + orientNodeEnd + "_" + len + "\t" + prevTokens[Falcon.EDGE]);
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			nodeStart = tokens[Falcon.NODE_BEGIN].substring(0, tokens[Falcon.NODE_BEGIN].indexOf(":"));
			orientNodeStart = tokens[Falcon.NODE_BEGIN].substring(tokens[Falcon.NODE_BEGIN].indexOf(":") + 1);
			orientNodeEnd = tokens[Falcon.NODE_END].substring(tokens[Falcon.NODE_END].indexOf(":") + 1);
			contigStartIdx = Integer.parseInt(tokens[Falcon.CONTIG_START]);
			contigEndIdx = Integer.parseInt(tokens[Falcon.CONTIG_END]);
			if (tokens[Falcon.CONTIG].equals(prevContig)) {
				if (!prevEdge.equals(nodeStart)) {
					// Insert reduced virtual preads
					len = contigStartIdx - prevContigEndIdx + 1;
					fm.writeLine(prevEdge + "\t" + prevContig + "_" + orientPrevEdge + orientNodeStart + "_" + len + "\t" + nodeStart);
				}
			}
			len = contigEndIdx - contigStartIdx + 1;
			fm.writeLine(nodeStart + "\t" + tokens[Falcon.CONTIG] + "_" + orientNodeStart + orientNodeEnd + "_" + len + "\t" + tokens[Falcon.EDGE]);
			prevContig = tokens[Falcon.CONTIG];
			prevEdge = tokens[Falcon.EDGE];
			orientPrevEdge = tokens[Falcon.NODE_END].substring(tokens[Falcon.NODE_END].indexOf(":") + 1);
			prevContigEndIdx = Integer.parseInt(tokens[Falcon.CONTIG_END]);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("java -jar falconTilingPathToSIF.jar <falcon_ctg_tiling_path_coord_len> <out.sif>");
		System.out.println("\tInsert intermediate reduced virtual preads if not connected within a contig");
		System.out.println("\t<out.sif>: <contig>\t<pread_1>\t<Contig_BB_Overlap_EdgeLen>\t<pread_2>");
		System.out.println("Arang Rhie, 2015-07-24. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new TilingPathToSIF().go(args[0], args[1]);
		} else {
			new TilingPathToSIF().printHelp();
		}
	}

}
