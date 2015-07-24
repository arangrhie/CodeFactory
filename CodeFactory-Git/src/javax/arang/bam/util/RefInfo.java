package javax.arang.bam.util;

import java.util.Vector;

import javax.arang.IO.bambasic.BinaryUtil;

public class RefInfo {

	Vector<String>	ref = null;
	private byte[] refInfoBytes = null;
	
	public void setRefInfo(byte[] refinfobytes, int l_text, int n_ref) {
		this.refInfoBytes = refinfobytes;
		ref = new Vector<String>();
		int offset = 12 + l_text;
		StringBuffer refName = null;
		for (int i = 0; i < n_ref; i++) {
			int l_name = BinaryUtil.toInt32(refinfobytes[offset], refinfobytes[offset + 1],
					refinfobytes[offset + 2], refinfobytes[offset + 3]);
			offset += 4;
			refName = new StringBuffer();
			for (int j = 0; j < l_name - 1; j++) {
				refName.append((char)refinfobytes[offset + j]);
			}
			ref.add(refName.toString());
			offset += l_name + 4;
		}
	}
	
	public byte[] getRefInfo() {
		return this.refInfoBytes;
	}
	
	public String getRefName(int refID) {
		if (refID == -1) return "*";
		else return ref.get(refID);
	}
	
	public int getRefID(String refName) {
		return ref.indexOf(refName);
	}
	
}
