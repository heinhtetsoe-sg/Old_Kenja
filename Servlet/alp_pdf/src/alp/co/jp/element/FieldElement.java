package alp.co.jp.element;

import java.util.ArrayList;

public class FieldElement {

	/*
	 * 可変フィールドへ値を格納する関数
	 *   格納フィールドを検索して、指定の検出位置から指定数分格納します
	 *     パラメータ説明
	 *       ele		フィールド要素定義エレメント
	 *       label		格納対象フィールド要素の名称ツリーリスト
	 *       text		格納するフィールド要素値のリスト
	 *       start		格納対象フィールド要素の格納開始出現数
	 *       setnumber	格納する要素値の数
	 *     戻り値
	 *       格納した値の数
	 */
	public static int SetFieldText(AElement ele, String[] label, String[] text,
			int start, int setnumber) {
		int endcount = start + setnumber;
		if (text.length < setnumber)
			endcount = start + text.length;
		int n = _SetFieldText(ele, label, 0, text, start, endcount, 0);
		return n;
	}
	public static int SetFieldText(AElement ele, String labels, String[] text,
			int start, int setnumber) {
		String[] label = labels.split("/");
		return SetFieldText(ele, label, text, start, setnumber);
	}
	public static int SetFieldText(AElement ele, String labels, String atext) {
		String[] label = labels.split("/");
		String[] text = new String[1];
		text[0] = atext;
		return  SetFieldText(ele, label, text, 0, 1);
	}

	/*
	 * 可変フィールドへ値を格納する関数の実際の処理
	 *     パラメータ説明
	 *       elements	フィールド要素ツリーの作業配列
	 *       label		格納対象フィールド要素の名称ツリーリスト
	 *       checklevel	判定する要素ツリーのレベル
	 *       text		格納するフィールド要素値のリスト
	 *       start		格納対象フィールド要素の格納開始出現数
	 *       endcount	格納完了する要素値の数
	 *       counter	今までの検出格納対象フィールド数
	 *     戻り値
	 *       格納した値の数
	 */
	private static int _SetFieldText(AElement ele, String[] label,
			int checklevel, String[] text, int start, int endcount, int counter) {
		int count = 0;
		int levelno = label.length;
		ArrayList<AElement> elements = ele.getElement_();
		// 子供の兄弟レベルを検索
		for (AElement E : elements) {
			if (E.equalName(label[checklevel])) {
				// 格納する対象の要素レベルなら検索一致
				if ((checklevel + 1) >= levelno) {
					// 検索一致数が格納開始数以上なら格納
					if ((counter + count) >= start)
						E.setValue(text[counter + count]);
					// 検索一致数カウント
					count++;
				} else { // 未だ親レベルの一致なので孫へ移行
					count += _SetFieldText(E, label, checklevel + 1,
							text, start, endcount, counter + count);
				}
				if ((count + counter) >= endcount)
					break;
			}
		}
		return count;
	}
}
