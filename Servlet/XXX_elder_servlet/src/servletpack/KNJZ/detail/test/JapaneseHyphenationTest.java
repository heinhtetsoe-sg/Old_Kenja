// kanji=漢字
/*
 * $Id: 82166de5acf5e3763107a7a44fb1f70032495f39 $
 *
 * 作成日: 2007/04/09 13:54:25 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail.test;

import servletpack.KNJZ.detail.JapaneseHyphenation;
import junit.framework.TestCase;

public class JapaneseHyphenationTest extends TestCase {
    final int column = 6;   // 全角で3文字
    String result;

    /* 以下のパターン結果は IE6 を動かして確認した */

    /*
     * 結果が変化しないパターン
     */

    // ノーマル
    final String notChange1 = "１２３４";

    // 変換対象ではない
    final String notChange2 = "１２、３４";

    // 改行コードの直後の禁則文字はそのまま
    final String notChange3 = "１２３\n、４";  // "１２３\n、４"

    // 最大カラム数に満たない
    final String notChange4 = "１２";

    // IEでは禁則処理しない（できない）。
    final String unChanged = "１、、、、２３４";  // "１、、、、２３４"

    /*
     * 結果が変化するパターン
     */

    // 禁則処理対象
    final String changed1 = "１２３、４";  // "１２\n３、４"

    // 改行コードが含まれているパターン
    final String changed2 = "\n１２３、４";  // "\n１２\n３、４"

    // 最初の規則処理によって、次の禁則文字も対象になる
    final String changed3 = "１２３、４、５６７";   // "１２\n３\n４、５６７"

    // 禁則文字が連続しているので、さかのぼって処理する
    final String changed4 = "１２、、３４";  // "１\n２、、３４"

    // =================================================================

    protected void setUp() throws Exception {
        result = null;
    }

    // =================================================================

    public void test変化しない() throws Exception {
        result = JapaneseHyphenation.beginLine(notChange1, column);
        assertEquals("１２３４", result);

        result = JapaneseHyphenation.beginLine(notChange2, column);
        assertEquals("１２、３４", result);

        result = JapaneseHyphenation.beginLine(notChange3, column);
        assertEquals("１２３\n、４", result);

        result = JapaneseHyphenation.beginLine(notChange4, column);
        assertEquals("１２", result);
    }

    public void test変化する() throws Exception {
        result = JapaneseHyphenation.beginLine(changed1, column);
        assertEquals("１２\n３、４", result);

        result = JapaneseHyphenation.beginLine(changed2, column);
        assertEquals("\n１２\n３、４", result);

        result = JapaneseHyphenation.beginLine(changed3, column);
        assertEquals("１２\n３、\n４、５６７", result);

        result = JapaneseHyphenation.beginLine("１２３、４、５６７８、９", column);
        assertEquals("１２\n３、\n４、５６７\n８、９", result);
    }

} // JapaneseHyphenationTest

// eof
