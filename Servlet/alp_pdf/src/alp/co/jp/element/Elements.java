package alp.co.jp.element;

import java.util.ArrayList;

import alp.co.jp.util.StringUtil;

public class Elements {
	AElement Ele; // 処理対象要素コピー
	ArrayList<String> checkfactor; // 検索要素名称
	int maxlevel; // 検索要素階層
	int idx; // 検索 №
	int count; // 検索カウント

	public Elements(AElement ele) {
		super();
		Ele = ele; // 処理対象要素をコピー
		checkfactor = new ArrayList<String>();
	}

	public String getName() {
		return Ele.getName();
	}

	public AElement getElement() {
		return Ele;
	}

	public void getValues(String fieldname, ArrayList<String> strs) {
		StringUtil.getPathArray(fieldname, checkfactor);
		maxlevel = checkfactor.size();
		getValues_(Ele, strs, 0);
	}
	private void getValues_(AElement ele, ArrayList<String> strs, int level) {
		String lavel = checkfactor.get(level); // パラメータ指定要素名称
		int namelen = lavel.length();
		int start = 0;
		if (lavel.lastIndexOf(']') == (namelen - 1)) {
			int p = lavel.lastIndexOf('[');
			if (p >= 0) {
				String nmbstr = lavel.substring(p + 1, namelen - 1);
				start = Integer.parseInt(nmbstr) - 1;	//１～の配列 2015/11/17
				if (start < 0) start = 0;
				lavel = lavel.substring(0, p);
			}
		}
		ArrayList<AElement> child = ele.getElement_();
		for (AElement childele : child) {
			if (childele.getName().equals(lavel)) {
				String value = childele.getValue();
				if ((level + 1) == maxlevel) {
					strs.add(value);
				} else if (level < maxlevel) {
					if (start>0)
						start--;
					else
						getValues_(childele, strs, level + 1);
				}
			}
		}

	}

	// Svf 変換で使っているが、親から子タグ全てにテキストを埋め込んでいる。
	// 本来親タグはテキストを持たないので、将来的には廃止する。
	String[] Values;
	 public void addElement(String fieldname, String value) {
		StringUtil.getPathArray(fieldname, checkfactor);
		maxlevel = checkfactor.size();
		String[] values = new String[maxlevel];
		String[] pvalues = value.split("/");
		for (int i = 0; i < maxlevel; i++) {
			if (i < pvalues.length)
				values[i] = pvalues[i];
			else
				values[i] = "";
		}
		addElement_(Ele, values, 0);
	}
	private void addElement_(AElement ele, String[] value, int level) {
		ArrayList<AElement> lele = ele.getElement_();
		String fieldname = checkfactor.get(level);
		String text = value[level];
		for (AElement e : lele) {
			if ((e.equalName(fieldname)) && (e.eqalValue(text))) {
				if ((level + 1) < maxlevel) {
					addElement_(e, value, level + 1);
					return;
				} else
					// 重複でも作成する。
					break;
			}
		}
		AElement E = new AElement(fieldname, text);
		lele.add(E);
		if ((level + 1) < maxlevel)
			addElement_(E, value, level + 1);
	}

	public void setElementN(String fieldname, int number, String[] texts) {
		if (number > 0) {
			StringUtil.getPathArray(fieldname, checkfactor);
			maxlevel = checkfactor.size();
			idx = 0;
			setElement_(Ele, texts, 0, number);
		}
	}
	private void setElement_(AElement ele, String[] texts, int level, int number) {
		ArrayList<AElement> lele = ele.getElement_();
		int count = 0;
		int loop = 1;
		int p = 0;
		int start = 0;
		int textmax = texts.length;
		String fieldname = checkfactor.get(level);
		int namelen = fieldname.length();
		if (fieldname.lastIndexOf(']') == (namelen - 1)) {
			p = fieldname.lastIndexOf('[');
			if (p >= 0) {
				String nmbstr = fieldname.substring(p + 1, namelen - 1);
				start = Integer.parseInt(nmbstr) - 1;	//１～の配列 2015/11/17
				if (start < 0) start = 0;
				loop = start + number;
			}
		}
		if (p > 0)
			fieldname = fieldname.substring(0, p);
		for (AElement e : lele) {
			if (e.getName().equals(fieldname))
				count++;
		}
		for (; count < loop; count++) {
			if (((level + 1) < maxlevel) || (idx>=textmax))
				lele.add(new AElement(fieldname, ""));
			else
				lele.add(new AElement(fieldname, texts[idx++]));
		}
		if ((level + 1) < maxlevel) {	//まだターゲットレベルで無い
			for (AElement e : lele) {
				if (e.getName().equals(fieldname))
					setElement_(e, texts, level + 1, number);
			}
		}
	}

	public ArrayList<String> FindValue(String path, int max, int itemmax) {
		ArrayList<String> strings = new ArrayList<String>();
		StringUtil.getPathArray(path, checkfactor);
		int item = itemmax > 0 ? itemmax : max;
		count = 0;
		maxlevel = checkfactor.size();
		idx = count = 0;
		Find_(Ele, strings, 0, max, item);
		return strings;
	}
	private boolean Find_(AElement ele, ArrayList<String> strs, int level, int max, int itemmax) {
		String lavel = checkfactor.get(level); // パラメータ指定要素名称
		int namelen = lavel.length();
		int start = 0;
		if (lavel.lastIndexOf(']') == (namelen - 1)) {
			int p = lavel.lastIndexOf('[');
			if (p >= 0) {
				String nmbstr = lavel.substring(p + 1, namelen - 1);
				start = Integer.parseInt(nmbstr) - 1;	//１～の配列 2015/11/17
				if (start < 0) start = 0;
				lavel = lavel.substring(0, p);
				if (start > idx) {
					idx++;
					return false;
				}
			}
		}
		ArrayList<AElement> child = ele.getElement_();
		int item = 0;
		for (AElement childele : child) {
			if (childele.getName().equals(lavel)) {
				if ((level + 1) == maxlevel) {
					String value = childele.getValue();
					strs.add(value);
					if (++count >= max)
						return true;
					if (++item >= itemmax)
						return false;
				} else if (level < maxlevel)
					Find_(childele, strs, level + 1, max, itemmax);
			}
		}
		return true;
	}

	public void clearElement(String fieldname) {
		StringUtil.getPathArray(fieldname, checkfactor);
		maxlevel = checkfactor.size();
		clearElement_(Ele, 0);
	}
	private void clearElement_(AElement ele, int level) {
		ArrayList<AElement> lele = ele.getElement_();
		if ((level + 1) <= maxlevel) {
			ArrayList<AElement> ldel = new ArrayList<AElement>();
			String fieldname = checkfactor.get(level);
			for (AElement e : lele) {
				if (e.equalName(fieldname)) { // 指定タグツリーに合致
					clearElement_(e, level + 1);
					if ((level + 1) == maxlevel)
						ldel.add(e);
				}
			}
			for (AElement e : ldel) {
				e.getElement_().clear();
				lele.remove(e);
			}
		} else {
			// 指定タグツリーの子孫なので削除します。
			for (AElement e : lele)
				clearElement_(e, level + 1);
			lele.clear();
		}
	}
}
