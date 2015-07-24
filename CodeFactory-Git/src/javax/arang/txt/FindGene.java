package javax.arang.txt;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class FindGene extends I2Owrapper {

	@Override
	public void hooker(FileReader frGeneID, FileReader frgmt, FileMaker fm) {
		String line;
		String[] tokens;
		
		HashMap<String, ArrayList<String>> msigDB = new HashMap<String, ArrayList<String>>();
		ArrayList<String> pathwayNames = new ArrayList<String>();
		
		while (frgmt.hasMoreLines()) {
			line = frgmt.readLine();
			tokens = line.split("\t");
			ArrayList<String> genes = new ArrayList<String>();
			for (int i = 0; i < tokens.length - 2; i++) {
				genes.add(tokens[i + 2]);
			}
			msigDB.put(tokens[1].replace("> ", ""), genes);
			pathwayNames.add(tokens[1].replace("> ", ""));
		}
		System.out.println(msigDB.size() + " pathways in " + frgmt.getFileName());
		
		
		fm.write("ENSGENE\tOFFICIAL_SYMBOL\tPATHWAYS");
//		for (int i = 0; i < pathwayNames.size(); i++) {
//			fm.write("\t" + pathwayNames.get(i));
//		}
		fm.writeLine();

		
		while (frGeneID.hasMoreLines()) {
			line = frGeneID.readLine();
			tokens = line.split("\t");
			fm.write(tokens[0] + "\t" + tokens[1] + "\t");
			for (int i = 0; i < pathwayNames.size(); i++) {
				if (msigDB.get(pathwayNames.get(i)).contains(tokens[1])) {
					fm.write(pathwayNames.get(i) + ", ");
				}
//				if (msigDB.get(pathwayNames.get(i)).contains(tokens[1])) {
//					fm.write("\tO");
//				} else {
//					fm.write("\tX");
//				}
			}
			fm.writeLine();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtFindGene.txt <in.geneid> <MSigDB.gmt> <out.txt>");
		System.out.println("\t<in.geneid>: ENSEMBL_ID\tOFFICIAL_SYMBOL");
		System.out.println("\t<MSigDB.gmt>: PATHWAY\tDESCRIPTION\tGENES_SEPERATED_IN_TABS");
		System.out.println("\t<out.txt>: ENSEMBL_ID\tOFFICIAL_SYMBOL\tDESCRIPTION in one column");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new FindGene().go(args[0], args[1], args[2]);
		} else {
			new FindGene().printHelp();
		}
	}

}
