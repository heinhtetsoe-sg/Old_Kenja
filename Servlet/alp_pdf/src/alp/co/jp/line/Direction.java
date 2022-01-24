package alp.co.jp.line;

public class Direction {

	public enum direct {
		U_H		("上・横"),
		L_V		("左・縦"),
		LH_SL	("左上から右下"),
		RH_SL	("右上から左下"),
		B_H		("下・横"),
		R_V		("右・縦");
		private String name;
		direct(String name){ this.name = name; }
		public String getName() { return name; }
	}
	public static direct getValue(String str) {
		for(direct d: direct.values()) {
			if (str.equals(d.getName()))
				return d;
		}
		return direct.U_H;
	}
}
