package javax.arang.gff;

public class GFF {
	
	public static final int CHR = 0;
	public static final int SOURCE = 1;
	public static final int TYPE = 2;
	public static final int START = 3;
	public static final int END = 4;
	public static final int NOTE = 8;
	
	public static final String parseField(String note, String fieldName) {
		String name = null;
		
		String[] notes = note.split(";");
		for(int i = 0; i < notes.length; i++) {
			if (notes[i].startsWith(fieldName + "=")) {
				name = notes[i].substring(notes[i].indexOf("=") + 1);
				break;
			}
		}
		return name;
	}
}
