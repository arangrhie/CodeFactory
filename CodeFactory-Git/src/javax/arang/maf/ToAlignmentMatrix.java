package javax.arang.maf;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToAlignmentMatrix extends INOwrapper {

	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		String line;
		String[] tokens;
		
		HashMap<String, String>	chrContigsMap = new HashMap<String, String>();
		ArrayList<String> chrList = new ArrayList<String>();
		ArrayList<String> contigList = new ArrayList<String>();
		String chr = "";
		boolean isSrc = false;
		FILE_READING : for (FileReader fr : frs) {
			String contig = fr.getFileName().substring(fr.getFileName().lastIndexOf(".") + 1).trim();
			contigList.add(contig);
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				if (line.startsWith("#"))	continue;
				if (line.startsWith("a")) {
					isSrc = true;
					continue; 	// score
				}
				if (line.startsWith("s")) {
					tokens = line.split(RegExp.WHITESPACE);
					// System.out.println(tokens[MAF.SRC]);
					if (isSrc) {
						if (!chrList.contains(tokens[MAF.SRC])) {
							chrList.add(tokens[MAF.SRC]);
							chr = tokens[MAF.SRC];
						}
						isSrc = false;
					} else {
						if (!contigList.contains(tokens[MAF.SRC])) {
							contigList.add(tokens[MAF.SRC]);
						}
						if (chrContigsMap.containsKey(chr)) {
							if (!chrContigsMap.get(chr).contains(tokens[MAF.SRC])) {
								chrContigsMap.put(chr, chrContigsMap.get(chr) + RegExp.TAB + tokens[MAF.SRC]);
								continue FILE_READING;
							}
						} else {
							chrContigsMap.put(chr, tokens[MAF.SRC]);
							continue FILE_READING;
						}
					}
				}
			}
		}
		
		for (int i = 0; i < chrList.size(); i++) {
			fm.write(RegExp.TAB + chrList.get(i));
			//System.out.println("[DEBUG] :: " + chrContigsMap.get(chr));
		}
		fm.writeLine();
		
		String contig;
		for (int j = 0; j < contigList.size(); j++) {
			contig = contigList.get(j);
			fm.write(contig);
			for (int i = 0; i < chrList.size(); i++) {
				chr = chrList.get(i);
				fm.write(RegExp.TAB);
				if (chrContigsMap.get(chr).contains(contig)) {
					fm.write("O");
				} else {
					fm.write("X");
				}
			}
			fm.writeLine();
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar mafToAlignmentMatrix.jar <in.maf>");
		System.out.println("\tOutput file will be written in a file named Alignment.matrix:");
		System.out.println("\t\t<Contig raws> <Reference cols> filled with O X to see the contig is uniquely aligned");
		System.out.println("Arang Rhie, 2015-03-06. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			new ToAlignmentMatrix().printHelp();
		} else {
			new ToAlignmentMatrix().go(args);
		}
	}


}
