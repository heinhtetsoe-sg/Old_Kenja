package alp.co.jp.util;

import java.awt.font.TextAttribute;

public class TextWidth {
	final static String[] value = { "", "狭", "少狭", "標準", "少広", "広" };

	public static Float getValue(String width) {
		if ((width != null) && (width.length() > 0)) {
			for (int i = 0; i < value.length; i++) {
				if (width.equals(value[i])) {
					switch (i) {
					case 1:
						return TextAttribute.WIDTH_CONDENSED;
					case 2:
						return TextAttribute.WIDTH_SEMI_CONDENSED;
					case 3:
						return TextAttribute.WIDTH_REGULAR;
					case 4:
						return TextAttribute.WIDTH_SEMI_EXTENDED;
					case 5:
						return TextAttribute.WIDTH_EXTENDED;
					}
				}
			}
		}
		char top = width.charAt(0);
		if ((top >= '0') && (top <= '9'))
			return Float.valueOf(width);
		return TextAttribute.WIDTH_REGULAR;
	}

	public static String getName(Float width) {
		if (width.floatValue()==0.0f)
			return value[0];
		if (width.equals(TextAttribute.WIDTH_CONDENSED))
			return value[1];
		else if (width.equals(TextAttribute.WIDTH_SEMI_CONDENSED))
			return value[2];
		else if (width.equals(TextAttribute.WIDTH_REGULAR))
			return value[3];
		else if (width.equals(TextAttribute.WIDTH_SEMI_EXTENDED))
			return value[4];
		else if (width.equals(TextAttribute.WIDTH_EXTENDED))
			return value[5];
		else
			return width.toString();
	}
}
