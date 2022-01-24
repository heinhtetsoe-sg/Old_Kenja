package alp.co.jp.util;

import java.io.File;
import java.util.ArrayList;

import com.lowagie.text.pdf.BaseFont;

public class StringUtil {

	// 引用符"間のテキストを取出します。
	public static String peel(String org) {
		int orglength = org.length();
		StringBuffer buff = new StringBuffer(orglength);
		int i, p, j;
		char CH;
		for (i = p = j = 0; i < orglength; i++) {
			CH = org.charAt(i);
			if ((j == 0) && (CH == ' ')) // 前スペースは無視
				continue;
			if (CH == '\\') {
				CH = org.charAt(++i);
			} else if (CH == '\"') {
				if (p == 0) { // 引用符開始
					p = j = i + 1;
				} else {
					break; // 引用符終了
				}
				continue;
			} else {
				j = i + 1;
			}
			buff.append(CH);
		}
		return buff.toString();
	}

	public static int getPathArray(String path, ArrayList<String> para) {
		if ((path == null) || (path.isEmpty()) || (para == null))
			return 0;
		String[] label = path.split("\\/");
		para.clear();
		for (int i = 0; i < label.length; i++)
			para.add(label[i]);
		return label.length;
	}

	// xml DTD ENTITY分離
	public static boolean getIntSubset(String intSubset, String[] inf) {
		String[] enti = intSubset.split(" ");
		if (enti.length >= 4) {
			inf[0] = enti[1];
			inf[1] = enti[2];
			String[] strs = enti[3].split("'");
			inf[2] = strs[1];
			return true;
		}
		return false;
	}

	// HTML風禁則処理
	public static int StringConfine(String ch, BaseFont basefont,
			ArrayList<String> str_list, float width, float width_M, float sz,
			float lineheight, float[] width_lst, float[] xpos_lst,
			float[] ypos_lst) {
		int idx = 0;
		int i, top, k, l, bef_top;
		int strlength = ch.length();
		float chwidth = 0;
		float chheight = 0;
		char[] Cs = { '（', '［', '｛', '(', '「', '[', '{', '"', '\'', '”', '’' };
		char[] Ce = { '）', '］', '｝', ')', '」', ']', '}', '"', '\'', '”', '’' };
		boolean cs = false;
		boolean ce = false;
		boolean ym = false;
		boolean bef_cs = false;
		// カウント用に文字列作成
		StringBuffer chbuffer = new StringBuffer();
		for (i = 0; i < strlength; i++) {
			char C = ch.charAt(i);
			if ('\uE000' <= C && C <= '\uF9FF') // E000～F9FF外字
				chbuffer.append('外');
			else
				chbuffer.append(C);
		}
		for (i = top = bef_top = 0; i < strlength; i++) {
			char C = ch.charAt(i);
			if (C == '\n') { // 改行
				chheight += lineheight;
				str_list.add(ch.substring(top, i));
				width_lst[idx] = chwidth;
				xpos_lst[idx + 1] = 0;
				ypos_lst[idx + 1] = chheight;
				idx++;
				top = i + 1;
				chwidth = 0;
			} else {
				ym = cs = ce = false;
				chwidth = basefont.getWidthPoint(
						chbuffer.substring(top, i + 1), sz) * width_M;
				chwidth = ((int) (chwidth * 100)) / 100f;
				if ((C == '。') || (C == '、')) {
					ym = true;
				} else {
					for (k = 0; k < Cs.length; k++)
						if (C == Cs[k]) {
							cs = true;
							break;
						}
					for (k = 0; k < Ce.length; k++)
						if (C == Ce[k]) {
							ce = true;
							break;
						}
				}
				if ((chwidth > width) && (i > top)) { // 行を下げる
					chheight += lineheight;
					if (bef_cs == true) { // 行末に括弧開始
						if (i > (top + 2)) {
							for (l = i - 2; l > (top + 1); l--) {
								C = ch.charAt(l);
								if ((C == '。') || (C == '、'))
									continue;
								for (k = Cs.length; k > 0; k--)
									if (C == Cs[k - 1])
										break;
								if (k == 0)
									break;
							}
							l = i - l;
							str_list.add(ch.substring(top, i - l));
							width_lst[idx] = width;
							xpos_lst[idx + 1] = 0;
							ypos_lst[idx + 1] = chheight;
							idx++;
							top = i - l; // 先頭を閉じ括弧－１にする。
							chwidth = 0;
							bef_cs = false; // 閉じ括弧が連続しても無視するようにする
							continue;
						}
						cs = false; // 閉じ括弧が連続しても無視するようにする
					} else if ((ym == true) || // 句読点禁則処理
							(ce == true)) { // 行頭に閉じ括弧
						if (i > (bef_top + 1)) {
							for (l = i - 1; l > (bef_top + 1); l--) {
								C = ch.charAt(l);
								if ((C == '。') || (C == '、'))
									continue;
								for (k = Cs.length; k > 0; k--)
									if (C == Cs[k - 1])
										break;
								if (k == 0)
									break;
							}
							l = i - l;
							str_list.add(ch.substring(top, i - l));
							width_lst[idx] = width;
							xpos_lst[idx + 1] = 0;
							ypos_lst[idx + 1] = chheight;
							idx++;
							top = i - 1;
							chwidth = 0;
							continue;
						}
					} else {
						str_list.add(ch.substring(top, i));
						width_lst[idx] = width;
						xpos_lst[idx + 1] = 0;
						ypos_lst[idx + 1] = chheight;
						idx++;
						bef_top = top;
						top = i;
						chwidth = 0;
					}
				}
				bef_cs = cs;
			}
		}
		if (top < strlength) {
			str_list.add(ch.substring(top));
			width_lst[idx] = chwidth;
			idx++;
		}
		return idx;
	}

	// ディレクトリ区切り文字検出
	public static boolean CheckDirectory(String path) {
		String separate = File.separator;
		if (path.indexOf(separate) >= 0) { // ディレクトリ区切検索
			return true;
		}
		else if (separate.equals("\\")) {
			if (path.indexOf("/") >= 0) {   // Windowsでのディレクトリ区切'/'検索
				return true;
			}
		}
		return false;
	}
}
