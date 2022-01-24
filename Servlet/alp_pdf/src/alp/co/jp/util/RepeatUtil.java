package alp.co.jp.util;

public class RepeatUtil {
	public enum REP_PLACE {
		BODY("B"), FACTER("b"), HEDER("H"), FOOTER("F");
		private String name;

		REP_PLACE(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public static REP_PLACE getValue(String str) {
		if (str != null) {
			for (REP_PLACE p : REP_PLACE.values()) {
				if (str.equals(p.getName()))
					return p;
			}
		}
		return REP_PLACE.BODY;
	}
}
