// kanji=漢字
/*
 * $Id: ControlMasterTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2004/05/24 16:42:51 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Semester;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import junit.framework.TestCase;

public class ControlMasterTest extends TestCase {
    MyEnum.Category category;
    private Semester _sem;
    private ControlMaster _cm;

    public ControlMasterTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        category = new MyEnum.Category();
        _sem = Semester.create(category, 1, "一学期", KenjaDateImpl.getInstance(2004, 4, 7), KenjaDateImpl.getInstance(2004, 7, 21));
        _cm = new ControlMaster(category, 2004, _sem.getCode(), KenjaDateImpl.getInstance(2004, 4, 1), ControlMaster.DISPLAY_SUBCLASS, null);
    }

    protected void tearDown() throws Exception {
        _cm = null;
//        Semester.clearAll(category);
        _sem = null;
    }

    public void testNewFail() {
        try {
            new ControlMaster(category, 0, _sem.getCode(), KenjaDateImpl.getInstance(2004, 4, 1), ControlMaster.DISPLAY_SUBCLASS, null);
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalArgumentException.class, e.getClass());
        }

        try {
            new ControlMaster(category, 2004, 0, KenjaDateImpl.getInstance(2004, 4, 1), ControlMaster.DISPLAY_SUBCLASS, null);
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalArgumentException.class, e.getClass());
        }

        try {
            new ControlMaster(category, 2004, 2, KenjaDateImpl.getInstance(2004, 4, 1), null, null);
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(IllegalArgumentException.class, e.getClass());
        }
    }

    public void testNewWithNullDate() {
        final ControlMaster cm = new ControlMaster(category, 2004, _sem.getCode(), null, ControlMaster.DISPLAY_SUBCLASS, null);
        assertNotNull(cm);
        assertNull(cm.getCurrentDate());
        cm.setCurrentDate(KenjaDateImpl.getInstance(2004, 4, 11)); // nullなので、設定できる
        assertEquals(KenjaDateImpl.getInstance(2004, 4, 11), cm.getCurrentDate());
        cm.setCurrentDate(KenjaDateImpl.getInstance(2004, 4, 12)); // すでに非nullなので、設定は無視される
        assertEquals(KenjaDateImpl.getInstance(2004, 4, 11), cm.getCurrentDate());
    }

    public void testSetCurrentDate() {
        final ControlMaster cm = new ControlMaster(category, 2004, _sem.getCode(), null, ControlMaster.DISPLAY_SUBCLASS, null);
        assertNotNull(cm);

        assertNull(cm.getCurrentDate());
        cm.setCurrentDate(KenjaDateImpl.getInstance(2004, 4, 11)); // nullなので、設定できる
        assertEquals(KenjaDateImpl.getInstance(2004, 4, 11), cm.getCurrentDate());

        cm.setCurrentDate(KenjaDateImpl.getInstance(2004, 4, 12)); // すでに非nullなので、設定は無視される
        assertEquals(KenjaDateImpl.getInstance(2004, 4, 11), cm.getCurrentDate());
    }

    public void testEquals() {
        final ControlMaster cm = new ControlMaster(category, 2004, _sem.getCode(), KenjaDateImpl.getInstance(2004, 4, 1), ControlMaster.DISPLAY_SUBCLASS, null);
        // 同値だ
        assertTrue(cm.equals(_cm));
        // しかし、同一ではない
        assertNotSame(cm, _cm);
    }

    public void testToString() {
        assertEquals((Object)"[2004年度,1:一学期[2004-04-07(水),2004-07-21(水)],2004-04-01(木),出欠制御日付=2004-04-01(木)]", _cm.toString());
    }

    public void testToString2() {
        final ControlMaster cm = new ControlMaster(category, 2004, 2, KenjaDateImpl.getInstance(2004, 12, 1), ControlMaster.DISPLAY_SUBCLASS, null);
        assertEquals("[2004年度,学期コード=2,2004-12-01(水),出欠制御日付=2004-12-01(水)]", cm.toString());
    }

    public void testGetCategory() {
        assertSame(category, _cm.getCategory());
    }

    public void testGetCurrentYear() {
        assertEquals(2004, _cm.getCurrentYear());
    }

    public void testGetCurrentYearAsString() {
        assertEquals("2004", _cm.getCurrentYearAsString());
    }

    public void testGetCurrentSemester() {
        assertEquals(1, _cm.getCurrentSemester().getCode());
    }

    public void testGetCurrentDate() {
        assertEquals(KenjaDateImpl.getInstance(2004, 4, 1), _cm.getCurrentDate());
    }

    public void testHashCode() {
        assertTrue(0 != _cm.hashCode());

        final ControlMaster cm = new ControlMaster(category, 2004, _sem.getCode(), KenjaDateImpl.getInstance(2004, 4, 1), ControlMaster.DISPLAY_SUBCLASS, null);
        assertEquals(cm.hashCode(), _cm.hashCode());
    }

    public void testAttendCtrlDate() {
        final KenjaDateImpl ctrlDate = KenjaDateImpl.getInstance(2004, 4, 1);
        final KenjaDateImpl attendDate = KenjaDateImpl.getInstance(2005, 11, 10);

        ControlMaster cm;
        cm = new ControlMaster(category, 2004, _sem.getCode(), ctrlDate, attendDate, ControlMaster.DISPLAY_SUBCLASS, null);
        assertEquals(KenjaDateImpl.getInstance(2005, 11, 10), cm.getAttendCtrlDate());

        cm = new ControlMaster(category, 2004, _sem.getCode(), ctrlDate, ControlMaster.DISPLAY_SUBCLASS, null);
        assertEquals(KenjaDateImpl.getInstance(2004, 4, 1), cm.getCurrentDate());
        assertEquals(cm.getCurrentDate(), cm.getAttendCtrlDate());
    }

    public void testIsUsingSubClass() {
        assertTrue(_cm.isUsingSubClass());
    }

    public void testIsUsingSubClass2() {
        final KenjaDateImpl d = KenjaDateImpl.getInstance(2004, 4, 1);
        final ControlMaster cm = new ControlMaster(category, 2004, 2, d, ControlMaster.DISPLAY_CHAIR, null);
        assertFalse(cm.isUsingSubClass());
    }

    public void testIsUsingSubClass3() {
        final KenjaDateImpl d = KenjaDateImpl.getInstance(2004, 4, 1);
        final ControlMaster cm = new ControlMaster(category, 2004, 2, d, "科目名か講座名かの区分は1か2", null);
        assertTrue(cm.isUsingSubClass());
    }
} // ControlMasterTest

// eof
