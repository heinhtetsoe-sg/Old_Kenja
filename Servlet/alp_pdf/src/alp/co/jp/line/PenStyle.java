package alp.co.jp.line;

public class PenStyle {

	public enum style {
		NULL		(""),
		STRAIGHT	("実線"),
		DASH		("破線"),
		DOT			("点線"),
		DASHDOT		("１点鎖線"),
		DASHDOTDOT	("２点鎖線"),
		LDASH		("長破線"),
		DOUBLE		("２重線");
		private String name;
		style(String name){ this.name = name; }
		public String getName() { return name; }
	}
	public static style getValue(String str) {
		for(style s: style.values()) {
			if (str.equals(s.getName()))
				return s;
		}
		return style.STRAIGHT;
	}
}
