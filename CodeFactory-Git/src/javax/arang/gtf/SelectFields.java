package javax.arang.gtf;

import java.util.ArrayList;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.gff.GFF;

public class SelectFields extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		System.err.print("[DEBUG] Following fields are selected:");
		for (String args : SelectFields.fieldsAndAttrisToSelect) {
			int col = GTF.getColumn(args);
			if (col != GTF.UNKNOWN) {
				System.err.print(" " + args + "(field) ");
			} else {
				System.err.print(" " + args + "(attribute) ");
			}
		}
		System.err.println();
		
		int col;
		int numLines = 0;
		int selected = 0;
		String outline = "";
		String attribute;
		READ_LOOP : while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			outline = "";
			numLines++;
			for (int i = 0; i < fieldsAndAttrisToSelect.size(); i++) {
				col = GTF.getColumn(fieldsAndAttrisToSelect.get(i));
				if (col != GTF.UNKNOWN) {
					outline += tokens[col];
					//fm.write(tokens[col]);
				} else {
					ATTRIBUTE_LOOP : for (int j = GTF.ATTRIBUTE; j < tokens.length; j++) {
						if (tokens[j].contains("=")) {
							attribute = GFF.parseField(tokens[GTF.ATTRIBUTE], fieldsAndAttrisToSelect.get(i));
							if (attribute == null) {
								continue READ_LOOP;
							} else {
								outline += attribute;
							}
							//fm.write(GFF.parseField(tokens[GTF.ATTRIBUTE], fieldsAndAttrisToSelect.get(i)));
							break ATTRIBUTE_LOOP;
						} else {
							if (fieldsAndAttrisToSelect.get(i).equals(tokens[j])) {
								tokens[j+1] = tokens[j+1].replace("\"", "");
								tokens[j+1] = tokens[j+1].replace(";", "");
								outline += tokens[j+1];
								//fm.write(tokens[j+1]);
								break ATTRIBUTE_LOOP;
							}
							j++;
						}
					}
				}
				selected++;
				if (i < fieldsAndAttrisToSelect.size() - 1) {
					outline += "\t";
					//fm.write("\t");
				}
			}
			//fm.writeLine();
			fm.writeLine(outline);
		}
		System.err.println(selected + " / " + numLines + " lines selected");
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar gtfSelectFields.jar <in.gtf> <out.txt> <field_or_attri_name> [field_or_attri_name]");
		System.out.println("\tSelect <field_or_attri_name> value(s) from <in.gtf> and write to <out.txt> as a tab-delimited file.");
		System.out.println("\t<field_or_attri_name> could be one of: seqname or chr, source, feature, start, end, score, strand, frame,\n"
				+ "\t\tattribute, gene_id, transcript_id, ...");
		System.out.println("\t\tTakes both GFF and GTF format.");
		System.out.println("Arang Rhie, 2016-06-24. arrhie@gmail.com");
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
