package javax.arang.gene;

import java.util.Comparator;

public class ExonUnionComparator implements Comparator<ExonUnion> {

	@Override
	public int compare(ExonUnion exonUnion1, ExonUnion exonUnion2) {
		if(exonUnion1.getMin() < exonUnion2.getMin()) {
			return 1;
		}
		if (exonUnion1.getMin() > exonUnion2.getMin()) {
			return -1;
		}
		return 0;
	}

	
}
