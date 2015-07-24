package javax.arang.falcon;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class TilingPathToContigPath extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String pread = fr.getFileName().split("_")[0];
		String contig = "";
		String pos = "";
		String direction = "";
		String prevEdge = "";
		boolean isFirstContig = true;
		fm.write(pread);
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			if (!contig.equals(tokens[Falcon.CONTIG])) {
				contig = tokens[Falcon.CONTIG];
				if (isFirstContig) {
					fm.write("\t" + contig);
					isFirstContig = false;
				} else {
					fm.write("\t" + pos + "\t" + direction + "\t" + contig);
				}
				prevEdge = tokens[Falcon.EDGE];
				pos = "";
				direction = "";
			} else {
				if (prevEdge.equals(pread) && tokens[Falcon.NODE_BEGIN].startsWith(prevEdge)) {
					continue;
				}
				prevEdge = tokens[Falcon.EDGE];
			}
			if (tokens[Falcon.NODE_BEGIN].startsWith(pread)) {
				if (tokens[Falcon.CONTIG_START].equals("0")) {
					pos = pos + "S";
				} else {
					pos = pos + "M";
				}
				direction = direction + getDirection(tokens[Falcon.NODE_BEGIN]);
			} else if (tokens[Falcon.NODE_END].startsWith(pread)) {
				if (tokens[Falcon.CONTIG_START].equals("0")) {
					pos = pos + "B";
				} else if (Integer.parseInt(tokens[Falcon.CONTIG_END]) == Integer.parseInt(tokens[Falcon.CONTIG_LEN]) - 1) {
					pos = pos + "E";
				} else {
					pos = pos + "M";
				}
				direction = direction + getDirection(tokens[Falcon.NODE_END]);
			}
			
		}
		fm.write("\t" + pos + "\t" + direction);
		fm.writeLine();
	}
	
	private String getDirection(String node) {
		String dir = "";
		if (node.endsWith(":B")) {
			dir = "-";
		} else if (node.endsWith(":E")) {
			dir = "+";
		}
		return dir;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar falconTilingPathToContigPath.jar <pread_ctg_tiling_path> <out>");
		System.out.println("\t<pread_ctg_tiling_path>: grep loop_pread falcon_ctg_tiling_path_coord_len");
		System.out.println("\t<out>: pread\tcontig1\tpread_pos as S/B/M/E\tcontig2\tpread_pos as S/B/M/E\tcontig3...");
		System.out.println("\t\tS/B/M/E: S=Start, B=Begin, M=Middle, E=End. +=:E, -=:B");
		System.out.println("Arang Rhie, 2015-07-07. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new TilingPathToContigPath().go(args[0], args[1]);
		} else {
			new TilingPathToContigPath().printHelp();
		}
	}

}
