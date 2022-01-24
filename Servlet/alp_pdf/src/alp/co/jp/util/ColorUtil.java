package alp.co.jp.util;

import java.awt.Color;

public class ColorUtil {
	final static int max_Color = 32;
	static String[] value={"白","黒","赤","緑","黄","青","紫","シアン","グレー","ピンク","橙","明灰"
		,"暗紫","明空","明緑","明明灰","黄肌","肌","","","","","","","","","","","","","",""
		};	//造色を追加
	static Color midnightparple = new Color(0xBA55D3);
	static Color lightsky = new Color(0x87CEFA);
	static Color lightgreen = new Color(0x90EE90);
	static Color lightlightgray = new Color(0xD3D3D3);
	static Color yellowskin = new Color(0xF0E68C);
	static Color skin = new Color(0xFAC090);
	static Color[] colorRGB={Color.WHITE,Color.BLACK,Color.RED,Color.GREEN,Color.YELLOW,Color.BLUE
		,Color.MAGENTA,Color.CYAN,Color.GRAY,Color.PINK,Color.ORANGE,Color.LIGHT_GRAY
		,midnightparple,lightsky,lightgreen,lightlightgray,yellowskin,skin
		,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK
		,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK};
	static int count = 18;

	public static Color getValue(String color) {
		for(int i=0; i<count; i++) {
			if (color.equals(value[i])) {
				return colorRGB[i];
			}
		}
		return Color.BLACK;
	}

	public static String getName(Color color) {
		for(int i=0; i<count; i++) {
			if (color.equals(colorRGB[i]))
				return value[i];
		}
		return "";
	}

	public static int addColor(String name, Color color) {
		if (count<max_Color)
			count++;
		value[count-1] = name;
		colorRGB[count-1] = color;
		return count;
	}

	public static int getCount() {
		return count;
	}
	public static int getMax_Color() {
		return max_Color;
	}
}
