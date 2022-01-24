package alp.co.jp.line;

public class RundCorner {

	public enum CORNER {
		ALL		("全て丸め"),
		UP		("上辺のみ"),
		BM		("下辺のみ"),
		LF		("左辺のみ"),
		RI		("右辺のみ"),
		LU		("左上のみ"),
		RU		("右上のみ"),
		LB		("左下のみ"),
		RB		("右下のみ"),
		D_LU	("左上除く"),
		D_RU	("右上除く"),
		D_LB	("左下除く"),
		D_RB	("右下除く"),
		ULBR	("左上・右下"),
		URBL	("右上・左下");
		private String name;
		CORNER(String name){ this.name = name; }
		public String getName() { return name; }
	}
	public static CORNER getValue(String str) {
		for(CORNER c: CORNER.values()) {
			if (str.equals(c.getName())) {
				return c;
			}
		}
		return CORNER.ALL;
	}
	
}
