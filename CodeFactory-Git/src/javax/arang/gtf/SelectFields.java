package javax.arang.gtf;

import java.util.ArrayList;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class SelectFields extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		System.out.print("[DEBUG] Following fields are selected:");
		for (String args : SelectFields.fieldsAndAttrisToSelect) {
			int col = GTF.getColumn(args);
			if (col != GTF.UNKNOWN) {
				System.out.print(" " + args + "(field) ");
			} else {
				System.out.print(" " + args + "(attribute) ");
			}
		}
		System.out.println();
		
		int col;
		int numLines = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.WHITESPACE);
			numLines++;
			for (int i = 0; i < fieldsAndAttrisToSelect.size(); i++) {
				col = GTF.getColumn(fieldsAndAttrisToSelect.get(i));
				if (col != GTF.UNKNOWN) {
					fm.write(tokens[col]);
				} else {
					ATTRIBUTE_LOOP : for (int j = GTF.ATTRIBUTE; j < tokens.length; j+=2) {
						if (fieldsAndAttrisToSelect.get(i).equals(tokens[j])) {
							tokens[j+1] = tokens[j+1].replace("\"", "");
							tokens[j+1] = tokens[j+1].replace(";", "");
							fm.write(tokens[j+1]);
							break ATTRIBUTE_LOOP;
						}
					}
				}
				if (i < fieldsAndAttrisToSelect.size() - 1) {
					fm.write("\t");
				}
			}
			fm.writeLine();
		}
		System.out.println(numLines + " are selected");
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar gtfSelectFields.jar <in.gtf> <out.txt> <field_or_attri_name> [field_or_attri_name]");
		System.out.println("\tSelect <field_or_attri_name> value(s) from <in.gtf> and write to <out.txt> as a tab-delimited file.");
		System.out.println("\t<field_or_attri_name> could be one of: seqname or chr, source, feature, start, end, score, strand, frame,\n"
				+ "\t\tattribute, gene_id, transcript_id, ...");
		System.out.println("Arang Rhie, 2015-03-19. arrhie@gmail.com");
	}

	static ArrayList<String> fieldsAndAttrisToSelect =  new ArrayList<String>();
	
	public static void main(String[] args) {
		if (args.length >= 3) {
			for (int i = 2; i < args.length; i++) {
				fieldsAndAttrisToSelect.add(args[i]);
			}
			new SelectFields().go(args[0], args[1]);
		} else {
			new SelectFields().printHelp();
		}
	}

}
