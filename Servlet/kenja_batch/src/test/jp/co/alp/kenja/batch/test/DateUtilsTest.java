package jp.co.alp.kenja.batch.test;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;

import jp.co.alp.kenja.batch.domain.DateUtils;

import junit.framework.TestCase;

public class DateUtilsTest extends TestCase {

    final KenjaDateImpl _d20061110 = KenjaDateImpl.getInstance(2006, 11, 10);

    public void testMonthCount同じ年() throws Exception {
        KenjaDateImpl f;
        KenjaDateImpl t;

        f = _d20061110;
        t = KenjaDateImpl.getInstance(2006, 12, 20);
        assertEquals(2, DateUtils.monthCount(f, t));

        f = KenjaDateImpl.getInstance(2006, 1, 10);
        assertEquals(12, DateUtils.monthCount(f, t));

        // 引数の大小が逆転パターン
        f = KenjaDateImpl.getInstance(2006, 12, 20);
        t = _d20061110;
        assertEquals(2, DateUtils.monthCount(f, t));
    }

    public void testMonthCount年が異なる() throws Exception {
        KenjaDateImpl f;
        KenjaDateImpl t;

        // 1年違う
        f = _d20061110;
        t = KenjaDateImpl.getInstance(2007, 2, 20);
        assertEquals(4, DateUtils.monthCount(f, t));

        f = KenjaDateImpl.getInstance(2006, 3, 10);
        t = KenjaDateImpl.getInstance(2007, 10, 20);
        assertEquals(20, DateUtils.monthCount(f, t));

        // 2年違う
        f = _d20061110;
        t = KenjaDateImpl.getInstance(2008, 2, 20);
        assertEquals(16, DateUtils.monthCount(f, t));

        f = KenjaDateImpl.getInstance(2006, 3, 20);
        t = KenjaDateImpl.getInstance(2008, 3, 10);
        assertEquals(25, DateUtils.monthCount(f, t));

        // 5年違う
        f = _d20061110;
        t = KenjaDateImpl.getInstance(2011, 2, 20);
        assertEquals(2 + 48 + 2, DateUtils.monthCount(f, t)); // 最初の年の月数＋年間の月数＋最後の年の月数
    }

    public void testGetLastdayOfMonth() throws Exception {
        KenjaDateImpl date;

        date = _d20061110;
        assertEquals("2006-11-30(木)", DateUtils.getLastdayOfMonth(date).toString());
        assertEquals("2006-11-10(金)", date.toString()); // 元の内容は変化しない事の確認

        date = KenjaDateImpl.getInstance(2004, 2, 10);
        assertEquals("2004-02-29(日)", DateUtils.getLastdayOfMonth(date).toString());

        date = KenjaDateImpl.getInstance(2005, 2, 10);
        final String D_2005_02_28 = "2005-02-28(月)";
        assertEquals(D_2005_02_28, DateUtils.getLastdayOfMonth(date).toString());

        date = KenjaDateImpl.getInstance(2005, 2, 1);
        assertEquals(D_2005_02_28, DateUtils.getLastdayOfMonth(date).toString());

        date = KenjaDateImpl.getInstance(2005, 2, 28);
        assertEquals(D_2005_02_28, DateUtils.getLastdayOfMonth(date).toString());
    }

    public void testAddMonthOfFirstDay() throws Exception {
        KenjaDateImpl date;

        date = DateUtils.addMonthOfFirstDay(_d20061110, 0);
        assertEquals("2006-11-01(水)", date.toString());

        date = DateUtils.addMonthOfFirstDay(_d20061110, 1);
        assertEquals("2006-12-01(金)", date.toString());

        date = DateUtils.addMonthOfFirstDay(_d20061110, 2);
        assertEquals("2007-01-01(月)", date.toString());

        date = DateUtils.addMonthOfFirstDay(_d20061110, -1);
        assertEquals("2006-10-01(日)", date.toString());
    }
}
