package javax.arang.IO;

import java.util.ArrayList;
import java.util.Collections;

import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class SortReadIds extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		String line;
		ArrayList<String> ids = new ArrayList<String>();
		while (fr.hasMoreLines()) {
			line = fr.readLine().toString();
			ids.add(line);
		}
		
		Collections.sort(ids);
		
		for (String id : ids) {
			fm.writeLine(id);
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new SortReadIds().go(args[0], args[1]);
	}

	@Override
	public void printHelp() {
		// TODO Auto-generated method stub
		
	}

}
