// kanji=漢字
/*
 * $Id: b6c3e8d03a9122569ec743c12c0749223dcf3cf9 $
 *
 * 作成日: 2007/04/09 13:41:18 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * 禁則処理。
 * @author takaesu
 * @version $Id: b6c3e8d03a9122569ec743c12c0749223dcf3cf9 $
 */
public class JapaneseHyphenation {
    private static final String KINSOKU = "、";

    /**
     * 行頭禁則。
     */
    public static String beginLine(final String input, final int byteColumn) {
        final StringBuffer result = new StringBuffer();

        final List lines = split(input, '\n');
        for (final Iterator it = lines.iterator(); it.hasNext();) {
            final String line = (String) it.next();

            final String output = beginHyphenation(line, byteColumn);
            result.append(output);
        }

        return result.toString();
    }
//  rtn.append(line);
//  StringUtils.indexOf(line, KINSOKU, start)

//    private static String beginHyphenation(final String str, final int byteColumn) {
//        final StringBuffer rtn = new StringBuffer();
//
//        final int len = str.length();
//        int start = 0;  // 検索開始位置。ゼロ始まり
//        while (start < len) {
//            int hit = StringUtils.indexOf(str, KINSOKU, start); // 検索開始位置からの相対位置
//            if (hit != -1) {
//                if ((byteColumn / 2) == (hit - start)) {
//                    rtn.append(str.substring(start, hit - 1));  // 改行を入れる手前までコピー
//                    rtn.append('\n');
//                    final String substring = str.substring(hit + start -1, hit + start - 1 + 2);
//                    rtn.append(substring);
//                    start = (start + hit - 1);
//                } else {
//                    rtn.append(str.substring(start, start + hit + 1));
//                    start = start + hit + 1;
//                }
//            } else {
//                rtn.append(str.substring(start));
//                break;
//            }
//        }
//
//        return rtn.toString();
//    }

    private static String beginHyphenation(final String str, final int byteColumn) {
        int start = 0;
        int len = str.length();
        StringBuffer rtn = new StringBuffer();

        final int maxColumn = (byteColumn / 2);
        for (start = 0; start < len; start += maxColumn) {
            // str から start番目の文字を抽出
            // その文字が禁則文字か？
            String hoge = str.substring(start);
            if (isKinsoku(hoge, start, maxColumn)) {
                int hit = StringUtils.indexOf(hoge, KINSOKU, start);
                rtn.append(str.substring(start, start + hit - 1));
                rtn.append('\n');
                start -= 1;
            } else {
                // データがカラムに満たない場合
                if (maxColumn > len) {
                    rtn.append(str.substring(start));
                    break;
                }

                if (start + maxColumn < len) {
                    rtn.append(str.substring(start));   // 最後の残りをコピー
                    break;
                } else {
                    rtn.append(str.substring(start, start + maxColumn));  // データの中間部分
                }
            }
        }

        return rtn.toString();
    }

    private static boolean isKinsoku(String str, int start, int maxColumn) {
        int hit = StringUtils.indexOf(str, KINSOKU, start);
        if (hit == -1) {
            return false;
        }
        if (hit % maxColumn == 0) {
            return true;
        }
        return false;
    }

    private static boolean match(int start, String str) {
        int hit = StringUtils.indexOf(str, KINSOKU, start);
        return -1 == hit;
    }
    /*
     * 参考: org.apache.commons.lang.StringUtils.split
     */
    private static List split(final String str, final char separatorChar) {
        final List list = new ArrayList();
        final int len = str.length();
        int i = 0, start = 0;
        boolean match = (str.charAt(0) == separatorChar);
        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (match) {
                    list.add(str.substring(start, i + 1));  // 区切り文字も含める
                    match = false;
                }
                start = ++i;
                continue;
            }
            match = true;
            i++;
        }
        if (match) {
            list.add(str.substring(start, i));
        }
        return list;
    }

    private static boolean isSpecial(final char c) {
        final int result = KINSOKU.indexOf(c);
        return result >= 0;
    }

    final static char[] CCC = new char[1];

    private static int count(final char it) {
        CCC[0] = it;
        String s = new String(CCC);
        return s.getBytes().length;
    }

    // 行末禁則
    // 分割禁則
} // JapaneseHyphenation

// eof
