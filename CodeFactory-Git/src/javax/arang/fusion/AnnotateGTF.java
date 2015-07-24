package javax.arang.fusion;

import java.util.HashMap;
import java.util.Vector;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class AnnotateGTF extends I2Owrapper {
	
	static final short CHR = 0;
	static final short SOURCE = 1;
	static final short TYPE = 2;
	static final short START = 3;
	static final short END = 4;
	static final short QUAL = 5;
	static final short STRAND = 6;
	static final short ANNO = 7;

	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		
		String line;
		String[] tokens;
		Vector<String> chrList = new Vector<String>();
		HashMap<String, Vector<Integer>> chrStart = new HashMap<String, Vector<Integer>>();
		HashMap<String, Vector<Integer>> chrEnd = new HashMap<String, Vector<Integer>>();
		HashMap<String, Vector<String>> chrAnnotation = new HashMap<String, Vector<String>>();
		
		// Load fr2 onto memory
		System.out.println("Start loading " + fr2.getFileName() + " onto memory...");
		Vector<Integer> starts = null;
		Vector<Integer> ends = null;
		Vector<String> anns = null;
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			if (line.startsWith("##")) {
				continue;
			}
			tokens = line.split("\t");
			if (!chrList.contains(tokens[CHR])) {
				chrList.add(tokens[CHR]);
				starts = new Vector<Integer>();
				ends = new Vector<Integer>();
				anns = new Vector<String>();
				chrStart.put(tokens[CHR], starts);
				chrEnd.put(tokens[CHR], ends);
				chrAnnotation.put(tokens[CHR], anns);
			}
			starts.add(Integer.parseInt(tokens[START]));
			ends.add(Integer.parseInt(tokens[END]));
			line = line.replace(" \"", "\t");
			line = line.replace("\"; ", "\t");
			line = line.replace(";","");
			anns.add(line);
		}
		System.out.println("..Finished Loading!");
		
		// print number of annotations in .GTF
		System.out.println("<Number of Annotations>");
		for (int i = 0; i < chrList.size(); i++) {
			System.out.println("\t" + chrList.get(i) + ": " + chrStart.get(chrList.get(i)).size());
		}
		System.out.println();
		
		// process fr1
		System.out.println("Start Annotating " + fr1.getFileName());
		String prevChr = "";
		String chr;
		int pos;
		boolean noAnn = true;
		String doner = "";
		String acceptor = "";
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			if (line.startsWith("#")) {
				fm.writeLine(line);
				continue;
			}
			tokens = line.split("\t");
			chr = tokens[0];
			pos = Integer.parseInt(tokens[1]);
			noAnn = true;
			doner = "";
			acceptor = "";
			if (!chr.equals(prevChr)) {
				starts = chrStart.get(chr);
				ends = chrEnd.get(chr);
				anns = chrAnnotation.get(chr);
				prevChr = chr;
			}
			if (starts != null) {
				for (int i = 0; i < starts.size(); i++) {
					if (starts.get(i) <= pos && pos <= ends.get(i)) {
						doner = anns.get(i);
						//fm.writeLine(line + "\t" + anns.get(i));
						noAnn = false;
					}
					if (starts.get(i) > pos)	break;
				}
			}
			chr = tokens[2];
			pos = Integer.parseInt(tokens[3]);
			
			if (!chr.equals(prevChr)) {
				starts = chrStart.get(chr);
				ends = chrEnd.get(chr);
				anns = chrAnnotation.get(chr);
				prevChr = chr;
			}
			if (starts != null) {
				for (int i = 0; i < starts.size(); i++) {
					if (starts.get(i) <= pos && pos <= ends.get(i)) {
						//fm.writeLine(line + "\t" + anns.get(i));
						acceptor=anns.get(i);
						noAnn = false;
					}
					if (starts.get(i) > pos)	break;
				}
			}
			fm.write(line);
			if (!noAnn) {
				fm.write("\tDONER\t" + doner + "\tACCEPTOR\t" + acceptor);
			}
			fm.writeLine();
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar starFusionAnnotateEncode.jar <in.fusion> <data.gtf>");
		System.out.println("\t<out>: in.fusion_ann with lines annotated at the end");
		System.out.println("\t<in.fusion>: chr\tdonor_pos\tchr\tacceptor_pos\tnum_evidences");
		System.out.println("\t<data.gtf> example path: /gmi-l1/_90.User_Data/serena/PublicData/ENCODE/gencode.v21.chr_patch_hapl_scaff.annotation.gtf");
		System.out.println("\t<data.gtf> will be loaded to memory.");
		System.out.println("Arang Rhie, 2014-12-08. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new AnnotateGTF().go(args[0], args[1], args[0] + "_ann");
		} else {
			new AnnotateGTF().printHelp();
		}
	}

}
