package javax.arang.ncbi.alt;

public class Placement {

	public static final int ALT_ASM_NAME = 0;
	public static final int PRIM_ASM_NAME = 1;
	public static final int ALT_SCAF_NAME = 2;
	public static final int PARENT_TYPE = 3;
	public static final int PARENT_NAME = 4;
	public static final int ORI = 5;
	public static final int ALT_SCAF_START = 6;
	public static final int ALT_SCAF_STOP = 7;
	public static final int PARENT_START = 8;
	public static final int PARENT_STOP = 9;
	public static final int ALT_START_TAIL = 10;
	public static final int ALT_STOP_TAIL = 11;
	
	private String altAsmName;
	private String primAsmName;
	private String altScafName;
	private String parentName;
	private String ori;
	private int altScafStart;
	private int altScafStop;
	private int parentStart;
	private int parentStop;
	private int altStartTail;
	private int altStopTail;
	private int altMatchedBases;
	//private int parentMatchedBases;
	
	boolean isEmpty = false;
	
	public Placement(String[] placementTokens) {
		this.altAsmName = placementTokens[ALT_ASM_NAME];
		this.primAsmName = placementTokens[PRIM_ASM_NAME];
		this.altScafName = placementTokens[ALT_SCAF_NAME];
		this.parentName = placementTokens[PARENT_NAME];
		if (this.parentName.equals("na")) {
			isEmpty = true;
		} else {
			isEmpty = false;
			this.ori = placementTokens[ORI];
			this.altScafStart = Integer.parseInt(placementTokens[ALT_SCAF_START]);
			this.altScafStop = Integer.parseInt(placementTokens[ALT_SCAF_STOP]);
			this.parentStart = Integer.parseInt(placementTokens[PARENT_START]);
			this.parentStop = Integer.parseInt(placementTokens[PARENT_STOP]);
			this.altStartTail = Integer.parseInt(placementTokens[ALT_START_TAIL]);
			this.altStopTail = Integer.parseInt(placementTokens[ALT_STOP_TAIL]);
			this.altMatchedBases = altScafStop - altScafStart + 1;
			//this.parentMatchedBases = parentStop - parentStart + 1;
		}
	}
	
	public int getMatchedBases() {
		return this.altMatchedBases;
	}

	public String getLine() {
		String line = altAsmName + "\t" + primAsmName + "\t" + altScafName + "\tSCAFFOLD\t"
				+ parentName + "\t";
		if (isEmpty) {
			line = line + "na\tna\tna\tna\tna\tna\tna";
		} else {
			line = line + ori + "\t" + altScafStart + "\t" + altScafStop 
					+ "\t" + parentStart + "\t" + parentStop + "\t" + altStartTail + "\t" + altStopTail;
		}
		return line;
	}
	
	public boolean isMergableWith(Placement placement) {
		if (!this.parentName.equals("na") && this.parentName.equals(placement.parentName)) {
			return true;
		}
		return false;
	}
	
	public Placement mergeWith(Placement placement) {
		if (this.getMatchedBases() < placement.getMatchedBases()) {
			this.ori = placement.ori;
		}
		this.altScafStart = Math.min(this.altScafStart, placement.altScafStart);
		this.altScafStop = Math.max(this.altScafStop, placement.altScafStop);
		this.parentStart = Math.min(this.parentStart, placement.parentStart);
		this.parentStop = Math.max(this.parentStop, placement.parentStop);
		this.altStartTail = Math.min(this.altStartTail, placement.altStartTail);
		this.altStopTail = Math.min(this.altStopTail, placement.altStopTail);
		this.altMatchedBases = altScafStop - altScafStart + 1;
		//this.parentMatchedBases = parentStop - parentStart + 1;
		return this;
	}
	
	
}
