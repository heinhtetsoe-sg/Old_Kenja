// kanji=漢字
/*
 * $Id: Period36Test.java 74566 2020-05-27 13:15:39Z maeshiro $
 *
 * 作成日: 2004/08/16 15:12:56 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.util.List;

import jp.co.alp.kenja.common.domain.Period;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import junit.framework.TestCase;

/**
 * 校時が、校時コードに0から35までの「36件」使えるテスト。
 * @author tamura
 * @version $Id: Period36Test.java 74566 2020-05-27 13:15:39Z maeshiro $
 */
public class Period36Test extends TestCase {
    MyEnum.Category category;

    public Period36Test(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        category = new MyEnum.Category();
        Period.create(category, "0", "0-name", "0-short-name", null, null);
        Period.create(category, "1", "1-name", "1-short-name", null, null);
        Period.create(category, "2", "2-name", "2-short-name", null, null);
        Period.create(category, "3", "3-name", "3-short-name", null, null);
        Period.create(category, "4", "4-name", "4-short-name", null, null);
        Period.create(category, "5", "5-name", "5-short-name", null, null);
        Period.create(category, "6", "6-name", "6-short-name", null, null);
        Period.create(category, "7", "7-name", "7-short-name", null, null);
        Period.create(category, "8", "8-name", "8-short-name", null, null);
        Period.create(category, "9", "9-name", "9-short-name", null, null);
        Period.create(category, "A", "A-name", "A-short-name", null, null);
        Period.create(category, "B", "B-name", "B-short-name", null, null);
        Period.create(category, "C", "C-name", "C-short-name", null, null);
        Period.create(category, "D", "D-name", "D-short-name", null, null);
        Period.create(category, "E", "E-name", "E-short-name", null, null);
        Period.create(category, "F", "F-name", "F-short-name", null, null);
        Period.create(category, "G", "G-name", "G-short-name", null, null);
        Period.create(category, "H", "H-name", "H-short-name", null, null);
        Period.create(category, "I", "I-name", "I-short-name", null, null);
        Period.create(category, "J", "J-name", "J-short-name", null, null);
        Period.create(category, "K", "K-name", "K-short-name", null, null);
        Period.create(category, "L", "L-name", "L-short-name", null, null);
        Period.create(category, "M", "M-name", "M-short-name", null, null);
        Period.create(category, "N", "N-name", "N-short-name", null, null);
        Period.create(category, "O", "O-name", "O-short-name", null, null);
        Period.create(category, "P", "P-name", "P-short-name", null, null);
        Period.create(category, "Q", "Q-name", "Q-short-name", null, null);
        Period.create(category, "R", "R-name", "R-short-name", null, null);
        Period.create(category, "S", "S-name", "S-short-name", null, null);
        Period.create(category, "T", "T-name", "T-short-name", null, null);
        Period.create(category, "U", "U-name", "U-short-name", null, null);
        Period.create(category, "V", "V-name", "V-short-name", null, null);
        Period.create(category, "W", "W-name", "W-short-name", null, null);
        Period.create(category, "X", "X-name", "X-short-name", null, null);
        Period.create(category, "Y", "Y-name", "Y-short-name", null, null);
        Period.create(category, "Z", "Z-name", "Z-short-name", null, null);
    }

    protected void tearDown() throws Exception {
//        Period.clearAll(category);
    }

    public void testSize() {
        assertEquals(36, Period.size(category));
    }

    public void testNotNull() {
        List<Period> list = Period.getEnumList(category);
        assertEquals(36, list.size());
        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);
            assertNotNull(o);
            Period period = (Period) o;
            assertEquals(1, period.getCodeAsString().length());
            assertTrue(period.getName().endsWith("-name"));
            assertTrue(period.getShortName().endsWith("-short-name"));
        }
    }

    public void testSame09() {
        assertSame(Period.getInstance(category, 0), Period.getInstance(category, "0"));
        assertSame(Period.getInstance(category, 1), Period.getInstance(category, "1"));
        assertSame(Period.getInstance(category, 2), Period.getInstance(category, "2"));
        assertSame(Period.getInstance(category, 3), Period.getInstance(category, "3"));
        assertSame(Period.getInstance(category, 4), Period.getInstance(category, "4"));
        assertSame(Period.getInstance(category, 5), Period.getInstance(category, "5"));
        assertSame(Period.getInstance(category, 6), Period.getInstance(category, "6"));
        assertSame(Period.getInstance(category, 7), Period.getInstance(category, "7"));
        assertSame(Period.getInstance(category, 8), Period.getInstance(category, "8"));
        assertSame(Period.getInstance(category, 9), Period.getInstance(category, "9"));
    }

    public void testSameAZ() {
        assertSame(Period.getInstance(category, 10), Period.getInstance(category, "A"));
        assertSame(Period.getInstance(category, 11), Period.getInstance(category, "B"));
        assertSame(Period.getInstance(category, 12), Period.getInstance(category, "C"));
        assertSame(Period.getInstance(category, 13), Period.getInstance(category, "D"));
        assertSame(Period.getInstance(category, 14), Period.getInstance(category, "E"));
        assertSame(Period.getInstance(category, 15), Period.getInstance(category, "F"));
        assertSame(Period.getInstance(category, 16), Period.getInstance(category, "G"));
        assertSame(Period.getInstance(category, 17), Period.getInstance(category, "H"));
        assertSame(Period.getInstance(category, 18), Period.getInstance(category, "I"));
        assertSame(Period.getInstance(category, 19), Period.getInstance(category, "J"));
        assertSame(Period.getInstance(category, 20), Period.getInstance(category, "K"));
        assertSame(Period.getInstance(category, 21), Period.getInstance(category, "L"));
        assertSame(Period.getInstance(category, 22), Period.getInstance(category, "M"));
        assertSame(Period.getInstance(category, 23), Period.getInstance(category, "N"));
        assertSame(Period.getInstance(category, 24), Period.getInstance(category, "O"));
        assertSame(Period.getInstance(category, 25), Period.getInstance(category, "P"));
        assertSame(Period.getInstance(category, 26), Period.getInstance(category, "Q"));
        assertSame(Period.getInstance(category, 27), Period.getInstance(category, "R"));
        assertSame(Period.getInstance(category, 28), Period.getInstance(category, "S"));
        assertSame(Period.getInstance(category, 29), Period.getInstance(category, "T"));
        assertSame(Period.getInstance(category, 30), Period.getInstance(category, "U"));
        assertSame(Period.getInstance(category, 31), Period.getInstance(category, "V"));
        assertSame(Period.getInstance(category, 32), Period.getInstance(category, "W"));
        assertSame(Period.getInstance(category, 33), Period.getInstance(category, "X"));
        assertSame(Period.getInstance(category, 34), Period.getInstance(category, "Y"));
        assertSame(Period.getInstance(category, 35), Period.getInstance(category, "Z"));
    }

    public void testSameSmall() {
        assertSame(Period.getInstance(category, 10), Period.getInstance(category, "a"));
        assertSame(Period.getInstance(category, 11), Period.getInstance(category, "b"));
        assertSame(Period.getInstance(category, 12), Period.getInstance(category, "c"));
        assertSame(Period.getInstance(category, 13), Period.getInstance(category, "d"));
        assertSame(Period.getInstance(category, 14), Period.getInstance(category, "e"));
        assertSame(Period.getInstance(category, 15), Period.getInstance(category, "f"));
        assertSame(Period.getInstance(category, 16), Period.getInstance(category, "g"));
        assertSame(Period.getInstance(category, 17), Period.getInstance(category, "h"));
        assertSame(Period.getInstance(category, 18), Period.getInstance(category, "i"));
        assertSame(Period.getInstance(category, 19), Period.getInstance(category, "j"));
        assertSame(Period.getInstance(category, 20), Period.getInstance(category, "k"));
        assertSame(Period.getInstance(category, 21), Period.getInstance(category, "l"));
        assertSame(Period.getInstance(category, 22), Period.getInstance(category, "m"));
        assertSame(Period.getInstance(category, 23), Period.getInstance(category, "n"));
        assertSame(Period.getInstance(category, 24), Period.getInstance(category, "o"));
        assertSame(Period.getInstance(category, 25), Period.getInstance(category, "p"));
        assertSame(Period.getInstance(category, 26), Period.getInstance(category, "q"));
        assertSame(Period.getInstance(category, 27), Period.getInstance(category, "r"));
        assertSame(Period.getInstance(category, 28), Period.getInstance(category, "s"));
        assertSame(Period.getInstance(category, 29), Period.getInstance(category, "t"));
        assertSame(Period.getInstance(category, 30), Period.getInstance(category, "u"));
        assertSame(Period.getInstance(category, 31), Period.getInstance(category, "v"));
        assertSame(Period.getInstance(category, 32), Period.getInstance(category, "w"));
        assertSame(Period.getInstance(category, 33), Period.getInstance(category, "x"));
        assertSame(Period.getInstance(category, 34), Period.getInstance(category, "y"));
        assertSame(Period.getInstance(category, 35), Period.getInstance(category, "z"));
    }
}
 // PeriodTest36

// eof