// kanji=漢字
/*
 * $Id: AccumulateSubclassTest.java 74566 2020-05-27 13:15:39Z maeshiro $
 *
 * 作成日: 2007/01/08 11:22:39 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.test;

import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.batch.accumulate.Attendance;
import jp.co.alp.kenja.batch.accumulate.AccumulateSubclass;
import junit.framework.TestCase;

public class AccumulateSubclassTest extends TestCase {
    private final AccumulateSubclass accum = new AccumulateSubclass();

    public void test最初は全てゼロ() throws Exception {
        assertEquals(0, accum.getAbroad());
        assertEquals(0, accum.getAbsent());
        assertEquals(0, accum.getEarly());
        assertEquals(0, accum.getLate());
        assertEquals(0, accum.getLesson());
        assertEquals(0, accum.getMourning());
        assertEquals(0, accum.getNonotice());
        assertEquals(0, accum.getNotice());
        assertEquals(0, accum.getNurseoff());
        assertEquals(0, accum.getSick());
        assertEquals(0, accum.getSuspend());
    }

    public void testCalc() throws Exception {
        final Student student = AccumulateTestConstants.STUDENT1;
        final Attendance attendance = AccumulateTestConstants.ATTENDANCE1;
        
        //
        accum.calc(student, attendance);
        assertEquals(0, accum.getAbroad());
        assertEquals(1, accum.getAbsent()); // +1
        assertEquals(0, accum.getEarly());
        assertEquals(0, accum.getLate());
        assertEquals(1, accum.getLesson()); // +1
        assertEquals(0, accum.getMourning());
        assertEquals(0, accum.getNonotice());
        assertEquals(0, accum.getNotice());
        assertEquals(0, accum.getNurseoff());
        assertEquals(0, accum.getSick());
        assertEquals(0, accum.getSuspend());
    }
    
    public void testLateNonotice() throws Exception {
        final Student student = AccumulateTestConstants.STUDENT1;
        final Attendance attendance = new Attendance(
                AccumulateTestConstants.CATEOGRY,
                AccumulateTestConstants.STUDENT1,
                AccumulateTestConstants.SCHEDULE0,
                AccumulateTestConstants.KINTAI21,
                AccumulateTestConstants.BLANK_REMARK
        );
        //
        accum.calc(student, attendance);
        assertEquals(0, accum.getAbroad());
        assertEquals(0, accum.getAbsent());
        assertEquals(0, accum.getEarly());
        assertEquals(0, accum.getLate());
        assertEquals(1, accum.getLesson()); // +1
        assertEquals(0, accum.getMourning());
        assertEquals(0, accum.getNonotice());
        assertEquals(0, accum.getNotice());
        assertEquals(0, accum.getNurseoff());
        assertEquals(0, accum.getSick());
        assertEquals(0, accum.getSuspend());
        assertEquals(1, accum.getLateNonotice()); // +1
        assertEquals(0, accum.getEarlyNonotice());
    }
    
    public void testEarlyNonotice() throws Exception {
        final Student student = AccumulateTestConstants.STUDENT1;
        final Attendance attendance = new Attendance(
                AccumulateTestConstants.CATEOGRY,
                AccumulateTestConstants.STUDENT1,
                AccumulateTestConstants.SCHEDULE0,
                AccumulateTestConstants.KINTAI22,
                AccumulateTestConstants.BLANK_REMARK
        );
        //
        accum.calc(student, attendance);
        assertEquals(0, accum.getAbroad());
        assertEquals(0, accum.getAbsent());
        assertEquals(0, accum.getEarly());
        assertEquals(0, accum.getLate());
        assertEquals(1, accum.getLesson()); // +1
        assertEquals(0, accum.getMourning());
        assertEquals(0, accum.getNonotice());
        assertEquals(0, accum.getNotice());
        assertEquals(0, accum.getNurseoff());
        assertEquals(0, accum.getSick());
        assertEquals(0, accum.getSuspend());
        assertEquals(0, accum.getLateNonotice());
        assertEquals(1, accum.getEarlyNonotice()); // +1
    }

    public void testNoCount() throws Exception {
        final Student student = AccumulateTestConstants.STUDENT1;
        final Attendance attendance = new Attendance(
                AccumulateTestConstants.CATEOGRY,
                AccumulateTestConstants.STUDENT1,
                AccumulateTestConstants.SCHEDULE0,
                AccumulateTestConstants.KINTAI27,
                AccumulateTestConstants.BLANK_REMARK
        );
        //
        accum.calc(student, attendance);
        assertEquals(0, accum.getLesson());
    }
} // AccumulateSubclassTest

// eof
