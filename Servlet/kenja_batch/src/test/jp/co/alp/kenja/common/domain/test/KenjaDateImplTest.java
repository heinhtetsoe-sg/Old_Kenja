// kanji=漢字
/*
 * $Id: KenjaDateImplTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2004/05/18 15:55:52 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import jp.co.alp.kenja.common.domain.DayOfWeek;
import jp.co.alp.kenja.common.domain.KenjaDate;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;

import junit.framework.TestCase;

public class KenjaDateImplTest extends TestCase {
    public KenjaDateImplTest(String name) {
        super(name);
    }

    private KenjaDateImpl date19700101;
    private KenjaDateImpl date20041231;

    protected void setUp() throws Exception {
        date19700101 = KenjaDateImpl.getInstance(1970, 1, 1);
        date20041231 = KenjaDateImpl.getInstance(2004, 12, 31);
    }

    protected void tearDown() throws Exception {
        date19700101 = null;
        date20041231 = null;
    }

    public void testCalendar() {
        final Calendar cal = KenjaDateImpl.calendar(2004, 12, 31);
        assertEquals(2004, cal.get(Calendar.YEAR));
        assertEquals(12 - 1, cal.get(Calendar.MONTH));
        assertEquals(31, cal.get(Calendar.DATE));
    }

    public void testLenient() {
        try {
            KenjaDateImpl.getInstance(2004, 12, 32);
            fail("例外が発生するので、ここには来ない");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

    public void testToString() {
        assertEquals((Object) "2004-12-31(金)", date20041231.toString());
    }

    public void testEquals() {
        final KenjaDateImpl date = KenjaDateImpl.getInstance(2004, 12, 31);
        assertEquals(date, date20041231);
    }

    public void testSame() {
        final KenjaDateImpl date = KenjaDateImpl.getInstance(1104451200000L);
        assertSame(date, date20041231);
    }

    public void testGetYear() {
        assertEquals(2004, date20041231.getYear());
    }

    public void testGetMonth() {
        assertEquals(12, date20041231.getMonth());
    }

    public void testGetDay() {
        assertEquals(31, date20041231.getDay());
    }

    public void testGetSQLDate() {
        final Object obj = date20041231.getSQLDate();
        assertNotNull(obj);
        assertSame(java.sql.Date.class, obj.getClass());
    }

    public void testGetTimeInMillis() {
        assertEquals(1104451200000L, date20041231.getTimeInMillis());
    }

    public void testGetIndex() {
        // 金曜日の表示順は「4」。
        assertEquals(4, date20041231.getIndex());

        // ちなみに2004-12-27は月曜日で、月曜日の表示順は「0」。
        final KenjaDateImpl date1227 = KenjaDateImpl.getInstance(2004, 12, 27);
        assertEquals(DayOfWeek.MONDAY, date1227.getDayOfWeek());
        assertEquals(0, date1227.getIndex());
    }

    public void testNextDate() {
        final KenjaDateImpl next = date20041231.nextDate();
        assertEquals("2005-01-01(土)", next.toString());

        assertEquals(1000 * 60 * 60 * 24L, next.getTimeInMillis() - date20041231.getTimeInMillis());
    }

    public void testAdd() {
        final KenjaDateImpl day = date20041231.add(3);
        assertEquals("2005-01-03(月)", day.toString());

        final long milli = 3 * (1000 * 60 * 60 * 24L);
        assertEquals(milli, day.getTimeInMillis() - date20041231.getTimeInMillis());
    }

    public void testAddMinus() {
        final KenjaDateImpl day = date20041231.add(-5);
        assertEquals("2004-12-26(日)", day.toString());

        final long milli = -5 * (1000 * 60 * 60 * 24L);
        assertEquals(milli, day.getTimeInMillis() - date20041231.getTimeInMillis());
    }

    public void testAddZero() {
        final KenjaDateImpl day = date20041231.add(0);
        assertSame("インスタンスは同じはずだ。", date20041231, day);
    }

    public void testGetSevenDaysMonday() {
        final KenjaDateImpl monday = KenjaDateImpl.getInstance(2004, 8, 2); // 2004-8-2は月
        final KenjaDate[] w = monday.getSevenDays();
        assertEquals(7, w.length);
        assertEquals(w[0], KenjaDateImpl.getInstance(2004, 8, 2)); // 月
        assertEquals(w[1], KenjaDateImpl.getInstance(2004, 8, 3)); // 火
        assertEquals(w[2], KenjaDateImpl.getInstance(2004, 8, 4)); // 水
        assertEquals(w[3], KenjaDateImpl.getInstance(2004, 8, 5)); // 木
        assertEquals(w[4], KenjaDateImpl.getInstance(2004, 8, 6)); // 金
        assertEquals(w[5], KenjaDateImpl.getInstance(2004, 8, 7)); // 土
        assertEquals(w[6], KenjaDateImpl.getInstance(2004, 8, 8)); // 日
    }


    public void testGetSevenDaysSaturday() {
        final KenjaDateImpl saturday = KenjaDateImpl.getInstance(2004, 8, 7); // 2004-8-7は土
        final KenjaDate[] w = saturday.getSevenDays();
        assertEquals(7, w.length);
        assertEquals(w[0], KenjaDateImpl.getInstance(2004, 8, 2)); // 月
        assertEquals(w[1], KenjaDateImpl.getInstance(2004, 8, 3)); // 火
        assertEquals(w[2], KenjaDateImpl.getInstance(2004, 8, 4)); // 水
        assertEquals(w[3], KenjaDateImpl.getInstance(2004, 8, 5)); // 木
        assertEquals(w[4], KenjaDateImpl.getInstance(2004, 8, 6)); // 金
        assertEquals(w[5], KenjaDateImpl.getInstance(2004, 8, 7)); // 土
        assertEquals(w[6], KenjaDateImpl.getInstance(2004, 8, 8)); // 日
    }

    public void testGetSevenDaysSunday() {
        final KenjaDateImpl sunday = KenjaDateImpl.getInstance(2004, 8, 8); // 2004-8-8は日
        final KenjaDate[] w = sunday.getSevenDays();
        assertEquals(7, w.length);
        assertEquals(w[0], KenjaDateImpl.getInstance(2004, 8, 2)); // 月
        assertEquals(w[1], KenjaDateImpl.getInstance(2004, 8, 3)); // 火
        assertEquals(w[2], KenjaDateImpl.getInstance(2004, 8, 4)); // 水
        assertEquals(w[3], KenjaDateImpl.getInstance(2004, 8, 5)); // 木
        assertEquals(w[4], KenjaDateImpl.getInstance(2004, 8, 6)); // 金
        assertEquals(w[5], KenjaDateImpl.getInstance(2004, 8, 7)); // 土
        assertEquals(w[6], KenjaDateImpl.getInstance(2004, 8, 8)); // 日
    }

    public void testGetWeekOfYear() {
        assertEquals(1, date19700101.getElapseWeeks());
        assertEquals(1, KenjaDateImpl.getInstance(1970, 1, 4).getElapseWeeks());

        assertEquals(2, KenjaDateImpl.getInstance(1970, 1, 5).getElapseWeeks());
        assertEquals(2, KenjaDateImpl.getInstance(1970, 1, 11).getElapseWeeks());

        final KenjaDateImpl monday = KenjaDateImpl.getInstance(2004, 10, 11); // 2004-10-11は月曜日
        final int wno = monday.getElapseWeeks(); // 週番号
        assertEquals(wno, monday.add(0).getElapseWeeks()); // 月 -- その日
        assertEquals(wno, monday.add(1).getElapseWeeks()); // 火
        assertEquals(wno, monday.add(2).getElapseWeeks()); // 水
        assertEquals(wno, monday.add(3).getElapseWeeks()); // 木
        assertEquals(wno, monday.add(4).getElapseWeeks()); // 金
        assertEquals(wno, monday.add(5).getElapseWeeks()); // 土
        assertEquals(wno, monday.add(6).getElapseWeeks()); // 日
        assertEquals(wno + 1, monday.add(7).getElapseWeeks()); // 月 -- 月曜日から違う週。翌週。
    }

    public void testCompareTo() {
        final KenjaDateImpl d1 = KenjaDateImpl.getInstance(2004, 1, 1);
        final KenjaDateImpl d2 = KenjaDateImpl.getInstance(2004, 1, 2);
        final KenjaDateImpl d3 = KenjaDateImpl.getInstance(2004, 1, 3);

        assertTrue(d1.compareTo(d1) == 0);
        assertTrue(d1.compareTo(d2) <  0); // 1月1日 < 1月2日
        assertTrue(d1.compareTo(d3) <  0);

        assertTrue(d2.compareTo(d1) >  0);
        assertTrue(d2.compareTo(d2) == 0);
        assertTrue(d2.compareTo(d3) <  0);

        assertTrue(d3.compareTo(d1) >  0);
        assertTrue(d3.compareTo(d2) >  0);
        assertTrue(d3.compareTo(d3) == 0);
    }

    public void testCompareTo_型ちがい() {
        final KenjaDateImpl d1 = KenjaDateImpl.getInstance(2004, 1, 1);
        assertEquals(-1, d1.compareTo("hello"));
    }

    public void testGetElapseDays() {
        assertEquals(0L, date19700101.getElapseDays());
        assertEquals(365L, KenjaDateImpl.getInstance(1971, 1, 1).getElapseDays());
        assertEquals(12783L, date20041231.getElapseDays());
    }

    public void testGetSevenDaysList() {
        /*
         *       10月 2004
         * 日 月 火 水 木 金 土
         *                 1  2
         *  3  4  5 (6  7  8  9  // 1週目
         * 10 11 12 13 14 15 16  // 2週目
         * 17 18 19 20 21 22 23  // 3週目
         * 24 25 26 27 28 29 30  // 4週目
         * 31
         *
         *       11月 2004
         * 日 月 火 水 木 金 土
         *     1  2  3  4) 5  6  // 5週目
         *  7  8  9 10 11 12 13
         * 14 15 16 17 18 19 20
         * 21 22 23 24 25 26 27
         * 28 29 30
         */
        final List<KenjaDate[]> list = KenjaDateImpl.getSevenDaysList(
                KenjaDateImpl.getInstance(2004, 10, 6),
                KenjaDateImpl.getInstance(2004, 11, 4)
        );
        assertEquals(5, list.size());

        // 1週目
        {
            final KenjaDateImpl[] w0 = (KenjaDateImpl[]) list.get(0);
            assertEquals(7, w0.length);
            assertSame(KenjaDateImpl.getInstance(2004, 10,  4), w0[0]); // mon
            assertSame(KenjaDateImpl.getInstance(2004, 10, 10), w0[6]); // sun
        }

        // 2週目
        {
            final KenjaDateImpl[] w1 = (KenjaDateImpl[]) list.get(1);
            assertEquals(7, w1.length);
            assertSame(KenjaDateImpl.getInstance(2004, 10, 11), w1[0]); // mon
            assertSame(KenjaDateImpl.getInstance(2004, 10, 17), w1[6]); // sun
        }

        // 3週目
        {
            final KenjaDateImpl[] w2 = (KenjaDateImpl[]) list.get(2);
            assertEquals(7, w2.length);
            assertSame(KenjaDateImpl.getInstance(2004, 10, 18), w2[0]); // mon
            assertSame(KenjaDateImpl.getInstance(2004, 10, 24), w2[6]); // sun
        }

        // 4週目
        {
            final KenjaDateImpl[] w3 = (KenjaDateImpl[]) list.get(3);
            assertEquals(7, w3.length);
            assertSame(KenjaDateImpl.getInstance(2004, 10, 25), w3[0]); // mon
            assertSame(KenjaDateImpl.getInstance(2004, 10, 31), w3[6]); // sun
        }

        // 5週目
        {
            final KenjaDateImpl[] w4 = (KenjaDateImpl[]) list.get(4);
            assertEquals(7, w4.length);
            assertSame(KenjaDateImpl.getInstance(2004, 11,  1), w4[0]); // mon
            assertSame(KenjaDateImpl.getInstance(2004, 11,  7), w4[6]); // sun
        }
    }

    public void testGetSevenDaysListSameWeek() {
        /*
         *       10月 2004
         * 日 月 火 水 木 金 土
         *                 1  2
         *  3  4  5  6  7  8  9
         * 10 11 12(13 14 15)16
         * 17 18 19 20 21 22 23
         * 24 25 26 27 28 29 30
         * 31
         */
        final List<KenjaDate[]> list = KenjaDateImpl.getSevenDaysList(
                KenjaDateImpl.getInstance(2004, 10, 13),
                KenjaDateImpl.getInstance(2004, 10, 15)
        );
        assertEquals(1, list.size());

        // 1週目
        KenjaDateImpl[] w0 = (KenjaDateImpl[]) list.get(0);
        assertEquals(7, w0.length);
        assertSame(KenjaDateImpl.getInstance(2004, 10, 11), w0[0]); // mon
        assertSame(KenjaDateImpl.getInstance(2004, 10, 17), w0[6]); // sun
    }

    public void testGetSevenDaysListSameDate() {
        /*
         *       11月 2004
         * 日 月 火 水 木 金 土
         *     1  2  3  4  5  6
         *  7  8  9 10 11(12)13
         * 14 15 16 17 18 19 20
         * 21 22 23 24 25 26 27
         * 28 29 30
         */
        final List<KenjaDate[]> list = KenjaDateImpl.getSevenDaysList(
                KenjaDateImpl.getInstance(2004, 11, 12),
                KenjaDateImpl.getInstance(2004, 11, 12)
        );
        assertEquals(1, list.size());

        // 1週目
        KenjaDateImpl[] w0 = (KenjaDateImpl[]) list.get(0);
        assertEquals(7, w0.length);
        assertSame(KenjaDateImpl.getInstance(2004, 11,  8), w0[0]); // mon
        assertSame(KenjaDateImpl.getInstance(2004, 11, 14), w0[6]); // sun
    }

    public void testGetSevenDaysListIArE() {
        try {
            KenjaDateImpl.getSevenDaysList(
                    KenjaDateImpl.getInstance(2004, 10, 6),
                    KenjaDateImpl.getInstance(2004, 10, 5)
            );
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalArgumentException.class, e.getClass());
        }
    }

    public void testGetTerm() {
        /*
         *       10月 2004
         * 日 月 火 水 木 金 土
         *                 1  2
         *  3  4 (5  6  7  8  9 // 1週目 =  5火 ... 10日
         * 10 11 12 13 14 15 16 // 2週目 = 11月 ... 17日
         * 17 18 19 20 21 22 23 // 3週目 = 18月 ... 24日
         * 24 25 26 27 28 29)30 // 4週目 = 25月 ... 29金
         * 31
         */
        final KenjaDateImpl s = KenjaDateImpl.getInstance(2004, 10,  5);
        final KenjaDateImpl e = KenjaDateImpl.getInstance(2004, 10, 29);
        final List<KenjaDate[]> list = KenjaDateImpl.getSevenDaysList(s, e);
        assertEquals("足かけ4週間だ", 4, list.size());

        // 1週目
        {
            final KenjaDateImpl[] term = KenjaDateImpl.getTerm(s, e, (KenjaDateImpl[]) list.get(0));
            assertEquals(2, term.length);
            assertSame(KenjaDateImpl.getInstance(2004, 10,  5), term[0]); //  5火
            assertSame(KenjaDateImpl.getInstance(2004, 10, 10), term[1]); // 10日
        }

        // 2週目
        {
            final KenjaDateImpl[] term = KenjaDateImpl.getTerm(s, e, (KenjaDateImpl[]) list.get(1));
            assertEquals(2, term.length);
            assertSame(KenjaDateImpl.getInstance(2004, 10, 11), term[0]); // 11月
            assertSame(KenjaDateImpl.getInstance(2004, 10, 17), term[1]); // 17日
        }

        // 3週目
        {
            final KenjaDateImpl[] term = KenjaDateImpl.getTerm(s, e, (KenjaDateImpl[]) list.get(2));
            assertEquals(2, term.length);
            assertSame(KenjaDateImpl.getInstance(2004, 10, 18), term[0]); // 18月
            assertSame(KenjaDateImpl.getInstance(2004, 10, 24), term[1]); // 24日
        }

        // 4週目
        {
            final KenjaDateImpl[] term = KenjaDateImpl.getTerm(s, e, (KenjaDateImpl[]) list.get(3));
            assertEquals(2, term.length);
            assertSame(KenjaDateImpl.getInstance(2004, 10, 25), term[0]); // 25月
            assertSame(KenjaDateImpl.getInstance(2004, 10, 29), term[1]); // 29金
        }
    }

    public void testGetTermSameWeek() {
        /*
         *       11月 2004
         * 日 月 火 水 木 金 土
         *     1 (2  3  4  5  6)
         *  7  8  9 10 11 12 13
         * 14 15 16 17 18 19 20
         * 21 22 23 24 25 26 27
         * 28 29 30
         */
        final KenjaDateImpl s = KenjaDateImpl.getInstance(2004, 11,  2);
        final KenjaDateImpl e = KenjaDateImpl.getInstance(2004, 11,  6);
        final List<KenjaDate[]> list = KenjaDateImpl.getSevenDaysList(s, e);
        assertEquals("1週間だ", 1, list.size());

        // 1週目
        {
            final KenjaDateImpl[] week = (KenjaDateImpl[]) list.get(0);
            assertEquals(7, week.length);

            assertSame(KenjaDateImpl.getInstance(2004, 11,  1), week[0]); //  1月
            assertSame(KenjaDateImpl.getInstance(2004, 11,  7), week[6]); //  7日

            final KenjaDateImpl[] term = KenjaDateImpl.getTerm(s, e, week);
            assertEquals(2, term.length);
            assertSame(KenjaDateImpl.getInstance(2004, 11,  2), term[0]); //  2火
            assertSame(KenjaDateImpl.getInstance(2004, 11,  6), term[1]); //  6土
        }
    }

    public void testGetTermSameDate() {
        /*
         *       11月 2004
         * 日 月 火 水 木 金 土
         *     1  2  3  4  5  6
         *  7  8  9(10)11 12 13
         * 14 15 16 17 18 19 20
         * 21 22 23 24 25 26 27
         * 28 29 30
         */
        final KenjaDateImpl s = KenjaDateImpl.getInstance(2004, 11, 10);
        final KenjaDateImpl e = s;
        assertSame(s, e);

        final List<KenjaDate[]> list = KenjaDateImpl.getSevenDaysList(s, e);
        assertEquals("1週間だ", 1, list.size());

        // 1週目
        {
            final KenjaDateImpl[] week = (KenjaDateImpl[]) list.get(0);
            assertEquals(7, week.length);

            assertSame(KenjaDateImpl.getInstance(2004, 11,  8), week[0]); //  8月
            assertSame(KenjaDateImpl.getInstance(2004, 11, 14), week[6]); // 14日

            final KenjaDateImpl[] term = KenjaDateImpl.getTerm(s, e, week);
            assertEquals(2, term.length);
            assertSame(KenjaDateImpl.getInstance(2004, 11, 10), term[0]); // 10水
            assertSame(KenjaDateImpl.getInstance(2004, 11, 10), term[1]); // 10水
        }
    }

    public void testGetTermIArE() {
        /*
         *       10月 2004
         * 日 月 火 水 木 金 土
         *                 1  2
         *  3  4 (5  6  7  8  9 // 1週目 =  5火 ... 10日
         * 10 11 12 13 14 15 16 // 2週目 = 11月 ... 17日
         * 17 18 19 20 21 22 23 // 3週目 = 18月 ... 24日
         * 24 25 26 27 28 29)30 // 4週目 = 25月 ... 29金
         * 31
         */
        final KenjaDateImpl s = KenjaDateImpl.getInstance(2004, 10,  5);
        final KenjaDateImpl e = KenjaDateImpl.getInstance(2004, 10, 29);
        final List<KenjaDate[]> list = KenjaDateImpl.getSevenDaysList(s, e);
        assertEquals("足かけ4週間だ", 4, list.size());

        // 1週目
        {
            try {
                // sとeの順序を「故意に」入れ替えてみる。
                KenjaDateImpl.getTerm(e, s, (KenjaDateImpl[]) list.get(0));
                fail("例外が発生するので、ここには来ない");
            } catch (final Exception ex) {
                assertSame(IllegalArgumentException.class, ex.getClass());
            }
        }
    }

    public void testGetInstanceStr() {
        try {
            final KenjaDateImpl d = KenjaDateImpl.getInstance("2005-02-17");
            assertEquals("2005-02-17(木)", d.toString());
        } catch (final Exception e) {
            fail("ここには来ない");
        }

        try {
            final KenjaDateImpl d = KenjaDateImpl.getInstance("2005/02/17");
            assertEquals("2005-02-17(木)", d.toString());
        } catch (final Exception e) {
            fail("ここには来ない");
        }

        try {
            KenjaDateImpl.getInstance("2005/02/31");
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception ex) {
            assertSame(ParseException.class, ex.getClass());
        }
    }

    public void testGetInstance_Date_NULL() {
        try {
            KenjaDateImpl.getInstance((Date) null);
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

    public void testGetInstance_UtilDate() {
        try {
            KenjaDateImpl.getInstance((Date) null);
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

    public void testGetInstance_SqlDate() {
        java.sql.Date d = new java.sql.Date(0L);
        assertEquals("1970-01-01(木)", KenjaDateImpl.getInstance(d).toString());
    }

    /**
     * JSTなカレンダーオブジェクトを渡すテスト。
     */
    public void testTimeZone() {
        final int mon = 1;
        final KenjaDateImpl expected = KenjaDateImpl.getInstance(2005, mon, 1);
        final Calendar cal = Calendar.getInstance();

        cal.set(2005, mon-1, 1, 0, 0, 0);
        assertEquals(expected, KenjaDateImpl.getInstance(cal));

        cal.set(2005, mon-1, 1, 8, 0, 0);
        assertEquals(expected, KenjaDateImpl.getInstance(cal));

        cal.set(2005, mon-1, 1, 10, 0, 0);
        assertEquals(expected, KenjaDateImpl.getInstance(cal));

        cal.set(2005, mon-1, 1, 17, 0, 0);
        assertEquals(expected, KenjaDateImpl.getInstance(cal));

        cal.set(2005, mon-1, 1, 18, 0, 0);
        assertEquals(expected, KenjaDateImpl.getInstance(cal));

        cal.set(2005, mon-1, 1, 23, 59, 59);
        assertEquals(expected, KenjaDateImpl.getInstance(cal));

        cal.set(2005, mon-1, 1, 24, 0, 0);  // 1月1日 24:00:00 は 1月1日ではない。
        assertTrue(!expected.equals(KenjaDateImpl.getInstance(cal)));
    }

    public void testFindNearestPast_Failed() {
        try {
            date19700101.findNearestPast(null);
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalArgumentException.class, e.getClass());
        }

        try {
            final Set<KenjaDateImpl> s = new TreeSet<KenjaDateImpl>();
            assertTrue(s.isEmpty());
            date19700101.findNearestPast(s);
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalArgumentException.class, e.getClass());
        }
    }

    public void testFindNearestPast() {
        final KenjaDateImpl d0101 = KenjaDateImpl.getInstance(2006, 1, 1);
        final KenjaDateImpl d0111 = KenjaDateImpl.getInstance(2006, 1, 11);
        final KenjaDateImpl d0121 = KenjaDateImpl.getInstance(2006, 1, 21);
        final KenjaDateImpl d0131 = KenjaDateImpl.getInstance(2006, 1, 31);
        final Set<KenjaDateImpl> set = new TreeSet<KenjaDateImpl>();
        set.add(d0101);
        set.add(d0111);
        set.add(d0121);
        set.add(d0131);
        assertEquals(4, set.size());

        assertEquals(d0101, d0101.findNearestPast(set));
        assertEquals(d0111, d0111.findNearestPast(set));
        assertEquals(d0121, d0121.findNearestPast(set));
        assertEquals(d0131, d0131.findNearestPast(set));

        assertEquals(d0101, KenjaDateImpl.getInstance(1999, 12, 31).findNearestPast(set));
        assertEquals(d0101, KenjaDateImpl.getInstance(2005, 12, 31).findNearestPast(set));
        assertEquals(d0101, KenjaDateImpl.getInstance(2006,  1,  2).findNearestPast(set));
        assertEquals(d0111, KenjaDateImpl.getInstance(2006,  1, 12).findNearestPast(set));
        assertEquals(d0121, KenjaDateImpl.getInstance(2006,  1, 22).findNearestPast(set));
        assertEquals(d0131, KenjaDateImpl.getInstance(2006,  2,  1).findNearestPast(set));
        assertEquals(d0131, KenjaDateImpl.getInstance(2099, 12, 31).findNearestPast(set));
    }

    public void testFindNearestPastAsList() {
        final KenjaDateImpl d0101 = KenjaDateImpl.getInstance(2006, 1, 1);
        final KenjaDateImpl d0111 = KenjaDateImpl.getInstance(2006, 1, 11);
        final KenjaDateImpl d0121 = KenjaDateImpl.getInstance(2006, 1, 21);
        final KenjaDateImpl d0131 = KenjaDateImpl.getInstance(2006, 1, 31);
        final List<KenjaDateImpl> list = new ArrayList<KenjaDateImpl>();
        list.add(d0101);
        list.add(d0111);
        list.add(d0121);
        list.add(d0131);
        assertEquals(4, list.size());

        assertEquals(d0101, d0101.findNearestPast(list));
        assertEquals(d0111, d0111.findNearestPast(list));
        assertEquals(d0121, d0121.findNearestPast(list));
        assertEquals(d0131, d0131.findNearestPast(list));

        assertEquals(d0101, KenjaDateImpl.getInstance(1999, 12, 31).findNearestPast(list));
        assertEquals(d0101, KenjaDateImpl.getInstance(2005, 12, 31).findNearestPast(list));
        assertEquals(d0101, KenjaDateImpl.getInstance(2006,  1,  2).findNearestPast(list));
        assertEquals(d0111, KenjaDateImpl.getInstance(2006,  1, 12).findNearestPast(list));
        assertEquals(d0121, KenjaDateImpl.getInstance(2006,  1, 22).findNearestPast(list));
        assertEquals(d0131, KenjaDateImpl.getInstance(2006,  2,  1).findNearestPast(list));
        assertEquals(d0131, KenjaDateImpl.getInstance(2099, 12, 31).findNearestPast(list));
    }

    public void testMax() {
        final KenjaDateImpl d0101 = KenjaDateImpl.getInstance(2006, 1, 1);
        final KenjaDateImpl d0222 = KenjaDateImpl.getInstance(2006, 2, 22);
        assertSame(d0222, d0101.max(d0222));
        assertSame(d0222, d0222.max(d0101));
        assertSame(d0222, d0222.max(null));
        assertSame(d0101, d0101.max(null));
    }
} // KenjaDateImplTest

// eof
