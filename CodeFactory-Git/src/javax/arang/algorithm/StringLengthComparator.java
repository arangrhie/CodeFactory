package javax.arang.algorithm;

import java.util.Comparator;

public class StringLengthComparator  implements Comparator<String>
{
    @Override
    public int compare(String x, String y)
    {
    	return (y.length() - x.length() > 0) ? 1 : -1;
    }

}
