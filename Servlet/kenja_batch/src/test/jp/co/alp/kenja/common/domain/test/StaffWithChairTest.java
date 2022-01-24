// kanji=漢字
/*
 * $Id: StaffWithChairTest.java 74566 2020-05-27 13:15:39Z maeshiro $
 *
 * 作成日: 2006/03/28 20:05:49 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.util.Set;

import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ChargeDiv;
import jp.co.alp.kenja.common.domain.GroupClass;
import jp.co.alp.kenja.common.domain.Staff;
import jp.co.alp.kenja.common.domain.SubClass;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import junit.framework.TestCase;

public class StaffWithChairTest extends TestCase {

    MyEnum.Category category;
    private Staff _staff1;
    private Staff _staff2;
    private Staff _staff3;
    private Chair _chairHR1;
    private Chair _chairGRP;
    private Chair _chairHR2;

    public StaffWithChairTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        category = new MyEnum.Category();
        _staff1 = Staff.create(category, "00100001", "00100001", "職員1", "職員1");
        _staff2 = Staff.create(category, "00100002", "00100002", "職員2", "職員2");
        _staff3 = Staff.create(category, "00100003", "00100003", "職員3", "職員3");

        _chairHR1 = Chair.create(category, "0002582", GroupClass.ZERO, SubClass.NULL, "講座HR1", new Integer(3), new Integer(3), false);

        _chairHR2 = Chair.create(category, "0004000", GroupClass.ZERO, SubClass.NULL, "講座HR2", new Integer(2), new Integer(2), false);

        // 職員1
        _chairHR1.addStaff(_staff1, ChargeDiv.REGULAR);
        _chairHR2.addStaff(_staff1, ChargeDiv.REGULAR);
        _chairGRP.addStaff(_staff1, ChargeDiv.REGULAR);

        // 職員2
        _chairHR2.addStaff(_staff2, ChargeDiv.VICE);

        // 職員3は担当なし
    }

    public void testGetChairs_booleanTRUE() {
        // true:正担当 && 群に属さない講座のみ

        // 職員1
        final Set<Chair> chairs1 = _staff1.getChairs(true);
        assertEquals(2, chairs1.size());
        assertTrue(chairs1.contains(_chairHR1));
        assertTrue(chairs1.contains(_chairHR2));

        // 職員2
        assertEquals(0, _staff2.getChairs(true).size());

        // 職員3
        assertEquals(0, _staff3.getChairs(true).size());
    }

    public void testGetChairs_booleanFALSE() {
        // false:正担当 && 群に属する講座のみ

        // 職員1
        final Set<Chair> chairs1 = _staff1.getChairs(false);
        assertEquals(1, chairs1.size());
        assertTrue(chairs1.contains(_chairGRP));

        // 職員2
        assertEquals(0, _staff2.getChairs(false).size());

        // 職員3
        assertEquals(0, _staff3.getChairs(false).size());
    }

    public void testGetChairs_REGULAR() {
        // 職員1
        final Set<Chair> chairs1 = _staff1.getChairs(ChargeDiv.REGULAR);
        assertEquals(3, chairs1.size());
        assertTrue(chairs1.contains(_chairHR1));
        assertTrue(chairs1.contains(_chairHR2));
        assertTrue(chairs1.contains(_chairGRP));

        // 職員2
        assertEquals(0, _staff2.getChairs(ChargeDiv.REGULAR).size());

        // 職員3
        assertEquals(0, _staff3.getChairs(ChargeDiv.REGULAR).size());
    }

    public void testGetChairs_VICE() {
        // 職員1
        assertEquals(0, _staff1.getChairs(ChargeDiv.VICE).size());

        // 職員2
        final Set<Chair> chairs2 = _staff2.getChairs(ChargeDiv.VICE);
        assertEquals(1, chairs2.size());
        assertTrue(chairs2.contains(_chairHR2));

        // 職員3
        assertEquals(0, _staff3.getChairs(ChargeDiv.VICE).size());
    }

    public void testIsRelgular() {
        assertTrue(_staff1.isRelgular());
        assertFalse(_staff2.isRelgular());
        assertFalse(_staff3.isRelgular());
    }

    public void testGetChairs() {
        // 職員1
        final Set<Chair> chairs1 = _staff1.getChairs();
        assertEquals(3, chairs1.size());
        assertTrue(chairs1.contains(_chairHR1));
        assertTrue(chairs1.contains(_chairHR2));
        assertTrue(chairs1.contains(_chairGRP));

        // 職員2
        final Set<Chair> chairs2 = _staff2.getChairs();
        assertEquals(1, chairs2.size());
        assertTrue(chairs2.contains(_chairHR2));

        // 職員3
        assertEquals(0, _staff3.getChairs().size());
    }

} // StaffWithChairTest

// eof
