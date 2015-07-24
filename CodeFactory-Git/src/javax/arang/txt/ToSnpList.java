package javax.arang.txt;

import java.io.File;
import java.util.StringTokenizer;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ToSnpList extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		StringTokenizer st;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.length() < 5)	continue;
			st = new StringTokenizer(line, ".,m> ");
			String pos = st.nextToken();
			String ref = pos.substring(pos.length() - 1);
			pos = pos.substring(0, pos.length() - 1);
			String obs = st.nextToken();
			String homo = st.nextToken();
			fm.write(pos + "\t" + ref + "\t" + obs + "\t");
			if (homo.startsWith("ho")) {
				fm.writeLine("2");
			} else {
				fm.writeLine("1");
			}
		}
	}
	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar toSnpList.jar <in.txt>");
		System.out.println("\t<output>: snp list");
		System.out.println("\t\tposition\tref\tobserved\tHom/Het(2/1)");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new ToSnpList().go(args[0], args[0].substring(args[0].lastIndexOf(File.separator)).replace(".snp", ".snp.list"));
		} else {
			new ToSnpList().printHelp();
		}

	}


}
