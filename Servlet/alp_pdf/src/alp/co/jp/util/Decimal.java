package alp.co.jp.util;

public class Decimal {

	public static String ToDecimalS2(int D) {
		StringBuffer decimal = new StringBuffer((new Integer(D)).toString());
		int length = decimal.length();
		if (D < 0) {
			if (length>3)
				decimal.insert(length-2, '.');
			else if (length==3)
				decimal.insert(1, "0.");
			else if (length==2)
				decimal.insert(1, "0.0");
		}
		else {
			if (length>2)
				decimal.insert(length-2, '.');
			else if (length==2)
				decimal.insert(0, "0.");
			else
				decimal.insert(0, "0.0");
		}
		return decimal.toString();
	}

	public static String ToDecimalS1(int D) {
		StringBuffer decimal = new StringBuffer((new Integer(D)).toString());
		int length = decimal.length();
		if (D < 0) {
			if (length>2)
				decimal.insert(length-1, '.');
			else if (length==2)
				decimal.insert(1, "0.");
		}
		else {
			if (length>1)
				decimal.insert(length-1, '.');
			else if (length==1)
				decimal.insert(0, "0.");
		}
		return decimal.toString();
	}

	public static int to100decimal(String numb) {
		String line = numb.trim();
		int max = line.length();
		int d = 0, j = 0;
		boolean nega = false;
		for(int i=0; i < max; i++) {
			char C = line.charAt(i);
			if ((C>='0')&&(C<='9')) {
				d *= 10;
				d += C - '0';
				if (j>0) {
					j++;
					if (j>2) {	//小数点以下２桁以下は切捨て
						break;
					}
				}
			}
			else if (C=='.') {
				j = 1;	//小数点以下有り
			}
			else if (C=='-')
				nega = true;
		}
		//小数点以下２桁補正
		if (j>0) j--;
		for(int i=j;i<2;i++)
			d *= 10;
		//負数補正
		if (nega==true)
			d *= -1;
		return d;
	}
	public static int to10decimal(String numb) {
		String line = numb.trim();
		int max = line.length();
		int d = 0, j = 0;
		boolean nega = false;
		for(int i=0; i < max; i++) {
			char C = line.charAt(i);
			if ((C>='0')&&(C<='9')) {
				d *= 10;
				d += C - '0';
				if (j>0) {
					j++;
					if (j>1) {	//小数点以下１桁以下は切捨て
						break;
					}
				}
			}
			else if (C=='.') {
				j = 1;
			}
			else if (C=='-')
				nega = true;
		}
		if (j<=1)
			d *= 10;
		if (nega==true)
			d *= -1;
		return d;
	}

	public static int From400To7200(String decimal) {
		Double d = Double.parseDouble(decimal)*7200;
		return (d.intValue() + 200) / 400;
	}
}
