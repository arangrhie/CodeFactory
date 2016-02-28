package javax.arang.agp;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToEdge extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		int cCount = 0;
		String scaffoldName = "";
		String nextScaffoldName = "";
		String contig1 = "";
		String contig2 = "";
		String orientation = "";
		String contig1Ort = "";
		String contig2Ort = "";
		String support = "1000000";
		
		int start = 0;
		int end = 0;
		int len = 0;
		int start2 = 0;
		int end2 = 0;
		int len2 = 0;
		boolean isFirst = true;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			if (isFirst) {
				isFirst = false;
			} else {
				nextScaffoldName = tokens[AGP.OBJ_NAME];
				if (scaffoldName.equals(nextScaffoldName)) {
					contig2 = tokens[AGP.COMPONENT_ID];
					if (contig2.contains(":")) {
						String[] contigComp = contig2.split(":");
						contig2 = contigComp[0];
						start2 = Integer.parseInt(contigComp[1].split("-")[0]) + (Integer.parseInt(tokens[AGP.COMPONENT_START]) - 1);
						end2 = Integer.parseInt(contigComp[1].split("-")[1]);
						contig2Ort = "C" + (cCount + 1);
					} else {
						start2 = Integer.parseInt(tokens[AGP.COMPONENT_START]) - 1;
						end2 = Integer.parseInt(tokens[AGP.COMPONENT_END]);
					}
					len2 = end2 - start2;
					
					if (tokens[AGP.ORIENTATION].equals("+")) {
						contig2Ort += "B";
					} else {
						contig2Ort += "E";
					}
					fm.writeLine(contig1 + ":" + contig1Ort + "\t" + contig2 + ":" + contig2Ort + "\t" + support + "\t" + start + "\t" + end + "\t" + len + "\t" + start2 + "\t" + end2 + "\t" + len2 + "\t" + scaffoldName + "\t" + tokens[AGP.ORIENTATION + 1]);
					//fm.writeLine(contig1 + "\t" + orientation + "\t" + contig1 + ":" + contig1Ort + "\t" + contig2 + "\t" + tokens[AGP.ORIENTATION] + "\t" + contig2 + ":" + contig2Ort + "\t" + support + "\t" + start + "\t" + end + "\t" + len + "\t" + start2 + "\t" + end2 + "\t" + len2 + "\t" + scaffoldName);
					//fm.writeLine(contig2 + "\t" + tokens[AGP.ORIENTATION] + "\t" + contig2 + ":" + contig2Ort + "\t" + contig1 + ":" + contig1Ort + "\t" + support + "\t" + start2 + "\t" + end2 + "\t" + len2 + "\t" + scaffoldName + "\t" + tokens[AGP.ORIENTATION + 1]);
				}
			}
			scaffoldName = tokens[AGP.OBJ_NAME];
			contig1 = tokens[AGP.COMPONENT_ID];
			orientation = tokens[AGP.ORIENTATION];
			
			contig1Ort = "";
			contig2Ort = "";
			if (contig1.contains(":")) {
				String[] contigComp = contig1.split(":");
				contig1 = contigComp[0];
				start = Integer.parseInt(contigComp[1].split("-")[0]) + (Integer.parseInt(tokens[AGP.COMPONENT_START]) - 1);
				end = Integer.parseInt(contigComp[1].split("-")[1]);
				cCount++;
				contig1Ort = "C" + cCount;
			} else {
				start = Integer.parseInt(tokens[AGP.COMPONENT_START]) - 1;
				end = Integer.parseInt(tokens[AGP.COMPONENT_END]);
			}
			len = end - start;
			
			if (orientation.equals("+")) {
				contig1Ort += "E";
			} else {
				contig1Ort += "B";
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar agpToEdge.jar <in.agp> <out.edge>");
		System.out.println("\t<in.agp>: contigs in scaffolds will be converted to nodes");
		System.out.println("\t\tcontigs split while scaffolding will be named contig:C1 contig:C2 ...");
		System.out.println("\t<out.edge>: contig1\t+/-\tcontig1:B/E/A/C\tcontig2:B/E/A/C\tnum_supports\tcontig1_start\tcontig1_end\tcontig1_len\tevidence(:evidence2:¡¦)");
		System.out.println("Arang Rhie, 2015-10-01. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ToEdge().go(args[0], args[1]);
		} else {
			new ToEdge().printHelp();
		}
	}

}
