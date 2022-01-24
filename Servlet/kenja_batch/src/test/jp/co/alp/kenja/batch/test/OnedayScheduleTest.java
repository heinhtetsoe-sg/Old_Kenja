// kanji=漢字
/*
 * $Id: OnedayScheduleTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2007/01/10 17:25:55 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.test;

import java.util.ArrayList;
import java.util.Collection;

import jp.co.alp.kenja.batch.accumulate.Attendance;
import jp.co.alp.kenja.batch.accumulate.BatchSchoolMaster;
import jp.co.alp.kenja.batch.accumulate.KintaiManager;
import jp.co.alp.kenja.batch.accumulate.OnedayAttendanceJudge;
import jp.co.alp.kenja.batch.accumulate.OnedaySchedule;
import jp.co.alp.kenja.common.domain.Kintai;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OnedayScheduleTest extends TestCase {
    
    private static Log log = LogFactory.getLog(OnedayScheduleTest.class);
    
    private MyOnedaySchedule oneDay1;

    private BatchSchoolMaster _batchSchoolMaster = AccumulateTestConstants.BATCHSCHOOLMASTER;
    
    private KintaiManager _kintaiManager;
    private OnedayAttendanceJudge _judge;

    protected void setUp() throws Exception {
        _kintaiManager = new KintaiManager(AccumulateTestConstants.CATEOGRY);
        try {
        Thread.sleep(1000);
        } finally {}
        _judge = new OnedayAttendanceJudge(null, _batchSchoolMaster, _kintaiManager);
    }

    protected void tearDown() throws Exception {
        oneDay1 = null;
    }

    public void testコンストラクタ() throws Exception {
        try {
            new OnedaySchedule(null, null, null);
            fail("例外が発生するので、ここには来ない");
        } catch (final NullPointerException e) {
            assertEquals(NullPointerException.class, e.getClass());
        }
    }

    public void testIsAllAbsence() throws Exception {
        // 空っぽの場合
        oneDay1 = new MyOnedaySchedule(new ArrayList<Attendance>());
        assertFalse(_judge.isKesseki(oneDay1));

        oneDay1 = new MyOnedaySchedule();
        // 出席／欠席が１つだけの場合
        oneDay1.add(AccumulateTestConstants.KINTAI0);
        assertFalse(_judge.isKesseki(oneDay1));

        oneDay1.clear();
        oneDay1.add(AccumulateTestConstants.KINTAI4);
        assertTrue(_judge.isKesseki(oneDay1));

        // 全て欠席以外の場合
        oneDay1.clear();
        oneDay1.add(AccumulateTestConstants.KINTAI1);
        oneDay1.add(AccumulateTestConstants.KINTAI2);
        assertFalse(_judge.isKesseki(oneDay1));

        // 全て欠席の場合
        oneDay1.clear();
        oneDay1.add(AccumulateTestConstants.KINTAI4);
        oneDay1.add(AccumulateTestConstants.KINTAI5);
        oneDay1.add(AccumulateTestConstants.KINTAI6);
        assertTrue(_judge.isKesseki(oneDay1));

        // 混在の場合
        oneDay1.clear();
        oneDay1.add(AccumulateTestConstants.KINTAI4);
        oneDay1.add(AccumulateTestConstants.KINTAI0);
        oneDay1.add(AccumulateTestConstants.KINTAI6);
        assertFalse(_judge.isKesseki(oneDay1));
    }

    public void testGet最初や最後のKintai() throws Exception {
        oneDay1 = new MyOnedaySchedule(new ArrayList<Attendance>());
        assertNull(oneDay1.getFirstKintai());
        assertNull(oneDay1.getLastKintai());

        oneDay1 = new MyOnedaySchedule();
        oneDay1.add(AccumulateTestConstants.KINTAI0);
        oneDay1.add(AccumulateTestConstants.KINTAI5);
        assertEquals(AccumulateTestConstants.KINTAI0, oneDay1.getFirstKintai());
        assertEquals(AccumulateTestConstants.KINTAI5, oneDay1.getLastKintai());
    }

    public void testContains() throws Exception {
        oneDay1 = new MyOnedaySchedule(new ArrayList<Attendance>());
        assertFalse(oneDay1.contains(null));
        assertFalse(oneDay1.contains(AccumulateTestConstants.KINTAI0));
        assertFalse(oneDay1.contains(AccumulateTestConstants.KINTAI1));

        oneDay1 = new MyOnedaySchedule();
        oneDay1.add(AccumulateTestConstants.KINTAI0);
        oneDay1.add(AccumulateTestConstants.KINTAI5);
        assertFalse(oneDay1.contains(AccumulateTestConstants.KINTAI1));
        assertTrue(oneDay1.contains(AccumulateTestConstants.KINTAI0));
        assertTrue(oneDay1.contains(AccumulateTestConstants.KINTAI5));
    }

    public void testOtherFirstContains() throws Exception {
        oneDay1 = new MyOnedaySchedule();
        oneDay1.add(AccumulateTestConstants.KINTAI0);
        oneDay1.add(AccumulateTestConstants.KINTAI1);
        oneDay1.add(AccumulateTestConstants.KINTAI2);
        oneDay1.add(AccumulateTestConstants.KINTAI3);
        oneDay1.add(AccumulateTestConstants.KINTAI4);

        
        assertFalse(_judge.saisyoIgainiSyusseki(oneDay1));
    }

    public void testOtherLastContains() throws Exception {
        oneDay1 = new MyOnedaySchedule();
        oneDay1.add(AccumulateTestConstants.KINTAI0);
        oneDay1.add(AccumulateTestConstants.KINTAI1);
        oneDay1.add(AccumulateTestConstants.KINTAI2);
        oneDay1.add(AccumulateTestConstants.KINTAI3);
        oneDay1.add(AccumulateTestConstants.KINTAI4);

        assertTrue(_judge.saigoIgainiSyusseki(oneDay1));
    }

    // ==========================

    class MyOnedaySchedule extends OnedaySchedule {
        public MyOnedaySchedule() {
            super(null, null, new ArrayList<Attendance>());
        }

        public MyOnedaySchedule(final Collection<Attendance> coll) {
            super(null, null, coll);
        }

        public void clear() {
            _kintaiList.clear();
        }

        public void add(final Kintai kintai) {
            if (null == kintai) {
                return;
            }
            _kintaiList.add(kintai);
        }
    }
} // OnedayScheduleTest

// eof
