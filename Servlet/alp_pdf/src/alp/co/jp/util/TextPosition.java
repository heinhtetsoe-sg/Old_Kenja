package alp.co.jp.util;

public class TextPosition {

	public enum position {
		LEFT	("左詰"),
		RIGHT	("右詰"),
		CENTER	("中央"),
		FIT		("適合"),
		UPPER	("上詰"),
		BOTTOM	("下詰"),
		AVE     ("均等");
		private String name;
		position(String name){ this.name = name; }
		public String getName() { return name; }
	}
	public static position getValue(String str) {
		for(position p: position.values()) {
			if (str.equals(p.getName()))
				return p;
		}
		return position.LEFT;
	}
}
