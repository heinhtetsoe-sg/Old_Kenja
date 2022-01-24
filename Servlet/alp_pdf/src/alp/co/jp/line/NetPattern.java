package alp.co.jp.line;

public class NetPattern {

	public enum pattern {
		PATTERN1	("パターン１", 0.95f),
		PATTERN2	("パターン２", 0.9f),
		PATTERN3	("パターン３", 0.85f),
		PATTERN4	("パターン４", 0.8f);
		private String name;
		private float node;
		pattern(String name, float node){ this.name = name; this.node = node; }
		public String getName() { return name; }
		public float getNode() { return node; }
	}
	public static pattern getValue(String str) {
		for(pattern p: pattern.values()) {
			if (str.equals(p.getName()))
				return p;
		}
		return pattern.PATTERN1;
	}
	public static float getNode(pattern ptn) {
		return ptn.getNode();
	}
}
