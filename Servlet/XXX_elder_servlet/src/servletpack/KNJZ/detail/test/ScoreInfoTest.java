// kanji=漢字
/*
 * $Id: 3734163d92f314a71b0d9d238593c1321bae9b49 $
 *
 * 作成日: 2007/07/11 10:21:58 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail.test;

import servletpack.KNJZ.detail.ScoreInfo;
import junit.framework.TestCase;

public class ScoreInfoTest extends TestCase {
    private static final double DELTA = 0.1;
    ScoreInfo _scoreInfo;

    protected void setUp() throws Exception {
        _scoreInfo = new ScoreInfo();
    }

    protected void tearDown() throws Exception {
        _scoreInfo = null;
    }

    public void test0普通のパターン() {
        _scoreInfo.add(40);
        _scoreInfo.add(75);
        _scoreInfo.add(40);

        assertEquals(75, _scoreInfo.getHigh().intValue());
        assertEquals(40, _scoreInfo.getLow().intValue());
        assertEquals(51.6, _scoreInfo.getAvg().doubleValue(), DELTA);
        assertEquals(3, _scoreInfo.size());

        assertEquals(1, _scoreInfo.rank(75));   // 1位
        assertEquals(2, _scoreInfo.rank(40));   // 2位(2人)
        assertEquals(0, _scoreInfo.rank(99));   // 判定不能

        // 標準偏差
        assertEquals(16.49916, _scoreInfo.getStddev(), DELTA);  // expected値は MS-Excel の STDEVP関数を使うと良い。

        assertEquals(42.9289321, _scoreInfo.getStdScore(40), DELTA);
        assertEquals(64.1421356, _scoreInfo.getStdScore(75), DELTA);
    }

    public void test空っぽ() {
        assertNull(_scoreInfo.getHigh());
        assertNull(_scoreInfo.getLow());
        assertNull(_scoreInfo.getAvg());
        assertEquals(0, _scoreInfo.size());

        // 有り得ない順位は「ゼロ」
        assertEquals(0, _scoreInfo.rank(0));
        assertEquals(0, _scoreInfo.rank(100));
        assertEquals(0, _scoreInfo.rank(-50));
    }

    public void test1() {
        _scoreInfo.add(null);
        test空っぽ();
    }

    /**
     * @see http://tomari.org/main/java/hensa.html
     */
    public void test標準偏差と偏差値1() {
        _scoreInfo.add(10);
        _scoreInfo.add(20);
        _scoreInfo.add(30);
        _scoreInfo.add(40);
        _scoreInfo.add(50);
        _scoreInfo.add(60);
        _scoreInfo.add(70);
        _scoreInfo.add(80);
        _scoreInfo.add(90);
        _scoreInfo.add(100);
        assertEquals(28.72281, _scoreInfo.getStddev(), DELTA);

        assertEquals(34.3330109, _scoreInfo.getStdScore(10), DELTA);
        assertEquals(37.8145640, _scoreInfo.getStdScore(20), DELTA);
        assertEquals(41.2961172, _scoreInfo.getStdScore(30), DELTA);
        assertEquals(44.7776703, _scoreInfo.getStdScore(40), DELTA);
        assertEquals(48.2592234, _scoreInfo.getStdScore(50), DELTA);
        assertEquals(51.7407765, _scoreInfo.getStdScore(60), DELTA);
        assertEquals(55.2223296, _scoreInfo.getStdScore(70), DELTA);
        assertEquals(58.7038827, _scoreInfo.getStdScore(80), DELTA);
        assertEquals(62.1854359, _scoreInfo.getStdScore(90), DELTA);
        assertEquals(65.6669890, _scoreInfo.getStdScore(100), DELTA);
    }
} // ScoreInfoTest

// eof
