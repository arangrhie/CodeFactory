package javax.arang.agp;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class Modify extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		int idx = 1;
		int start;
		int size;
		line = fr.readLine();
		fm.writeLine(line);
		String obj = "";
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			start = Integer.parseInt(tokens[AGP.OBJ_START]);
			if (tokens[AGP.COMPONENT_TYPE].equals("N")) {
				size = Integer.parseInt(tokens[AGP.COMPONENT_ID]);
			} else {
				size = Integer.parseInt(tokens[AGP.COMPONENT_END]);
			}
			if (start == 1) {
				System.out.println(obj + "\t" + idx);
				idx = 0;
			}
			size = size - 2;
			idx++;
			fm.write(tokens[AGP.OBJ_NAME] + "\t" + idx + "\t" + (idx + size));
			for (int i = AGP.PART_NUM; i <= AGP.COMPONENT_SIZE; i++) {
				fm.write("\t" + tokens[i]);
			}
			fm.writeLine();
			idx += size;
			obj = tokens[AGP.OBJ_NAME];
		}
		System.out.println(obj + "\t" + idx);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar agpModify.jar <in.agp> <out.agp>");
		System.out.println("Subtract 1 bp to correct error");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new Modify().go(args[0], args[1]);
		} else {
			new Modify().printHelp();
		}
	}

}
