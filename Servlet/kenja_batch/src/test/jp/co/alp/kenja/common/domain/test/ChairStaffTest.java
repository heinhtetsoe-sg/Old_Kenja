// kanji=漢字
/*
 * $Id: ChairStaffTest.java 74566 2020-05-27 13:15:39Z maeshiro $
 *
 * 作成日: 2004/06/08 17:15:15 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.util.Collection;
import java.util.Iterator;

import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ChargeDiv;
import jp.co.alp.kenja.common.domain.GroupClass;
import jp.co.alp.kenja.common.domain.Staff;
import jp.co.alp.kenja.common.domain.SubClass;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import junit.framework.TestCase;

public class ChairStaffTest extends TestCase {

    MyEnum.Category category;
    private SubClass _sub;
    private Staff _s1, _s2, _s3;
    private Chair _c1, _c2;

    public ChairStaffTest(String name) {
        super(name);
    }

    protected void setUp() {
        //
        category = new MyEnum.Category();
        _s1 = Staff.create(category, "00000001", "00000001", "教師1", "教1");
        _s2 = Staff.create(category, "00000002", "00000002", "教師2", "教2");
        _s3 = Staff.create(category, "00000003", "00000003", "教師3", "教3");

        _sub = SubClass.create(category, "01", "H", "2", "010001", "国語I", "国I");
        _c1 = Chair.create(category, "0001111", GroupClass.ZERO, _sub, "国語講座1", new Integer(4), new Integer(1), true);
        _c2 = Chair.create(category, "0002222", GroupClass.ZERO, _sub, "国語講座2", new Integer(3), new Integer(1), true);

        //
        /*
        _s1.putChair(_c1, ChargeDiv.REGULAR);
        _s1.putChair(_c2, ChargeDiv.REGULAR);
        _s1.putChair(_c2, ChargeDiv.REGULAR);

        _s2.putChair(_c1, ChargeDiv.REGULAR);
        _s2.putChair(_c2, ChargeDiv.VICE);

        _s3.putChair(_c1, ChargeDiv.VICE);
        */
        _c1.addStaff(_s1, ChargeDiv.REGULAR);
        _c1.addStaff(_s2, ChargeDiv.REGULAR);
        _c1.addStaff(_s3, ChargeDiv.VICE);

        _c2.addStaff(_s1, ChargeDiv.REGULAR);
        _c2.addStaff(_s2, ChargeDiv.VICE);
    }

    protected void tearDown() {
//        KenjaReInitializer.runAll(category);
//        Staff.clearAll(category);
//        SubClass.clearAll(category);
//        Chair.clearAll(category);
//        Section.clearAll(category);
//        GroupClass.clearAll(category);
    }

    public void testGetChairClearEx() {
        {
            final Collection<Chair> col = _s1.getChairs();
            try {
                col.clear();
                fail("例外が発生するので、ここには来ない");
            } catch (Exception e) {
                assertEquals(UnsupportedOperationException.class, e.getClass());
            }
        }

        {
            final Collection<Chair> col = _s1.getChairs(ChargeDiv.REGULAR);
            try {
                col.clear();
                fail("例外が発生するので、ここには来ない");
            } catch (Exception e) {
                assertEquals(UnsupportedOperationException.class, e.getClass());
            }
        }
    }

    //============

    // 教師1の正(R)
    public void testGetChairs_s1r() {
        final Collection<Chair> col_r = _s1.getChairs(ChargeDiv.REGULAR);
        assertNotNull(col_r);
        assertEquals(2, col_r.size());

        assertTrue(col_r.contains(_c1));
        assertTrue(col_r.contains(_c2));

        for (final Iterator<Chair> it = col_r.iterator(); it.hasNext();) {
            Object s = it.next();
            assertEquals(Chair.class, s.getClass());
        }
    }

    // 教師1の副(V)
    public void testGetChairs_s1v() {
        final Collection<Chair> col_v = _s1.getChairs(ChargeDiv.VICE);

        assertNotNull(col_v);
        assertEquals(0, col_v.size());

        assertFalse(col_v.contains(_c1));
        assertFalse(col_v.contains(_c2));

        for (final Iterator<Chair> it = col_v.iterator(); it.hasNext();) {
            Object s = it.next();
            assertEquals(Chair.class, s.getClass());
        }
    }
} // ChairStaffTest

// eof
