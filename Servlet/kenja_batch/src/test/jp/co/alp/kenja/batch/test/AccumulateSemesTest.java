// kanji=漢字
/*
 * $Id: AccumulateSemesTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2007/01/08 16:02:31 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.test;

import java.util.ArrayList;
import java.util.Collection;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Kintai;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.domain.UsualSchedule;
import jp.co.alp.kenja.common.domain.UsualSchedule.RollCalledDiv;
import jp.co.alp.kenja.batch.accumulate.Attendance;
import jp.co.alp.kenja.batch.accumulate.AccumulateSchedule;
import jp.co.alp.kenja.batch.accumulate.AccumulateSemes;
import jp.co.alp.kenja.batch.accumulate.BatchSchoolMaster;
import jp.co.alp.kenja.batch.accumulate.KintaiManager;
import jp.co.alp.kenja.batch.accumulate.OnedayAttendanceJudge;
import junit.framework.TestCase;

public class AccumulateSemesTest extends TestCase {
    private AccumulateSemes accum;
    private final OnedayAttendanceJudge judge;
    private Collection<Attendance> coll;
    final Student student = AccumulateTestConstants.STUDENT1;
    final KenjaDateImpl d20061110 = KenjaDateImpl.getInstance(2006, 11, 10);

    protected void setUp() throws Exception {
        accum = new AccumulateSemes();
        coll = new ArrayList<Attendance>();
    }
    protected void tearDown() throws Exception {
        accum = null;
        coll = null;
    }

    public AccumulateSemesTest() {
        final KintaiManager kintaiManager = new KintaiManager(
                AccumulateTestConstants.KINTAI0,
                AccumulateTestConstants.KINTAI1,
                AccumulateTestConstants.KINTAI2,
                AccumulateTestConstants.KINTAI3,
                AccumulateTestConstants.KINTAI4,
                AccumulateTestConstants.KINTAI5,
                AccumulateTestConstants.KINTAI6,
                AccumulateTestConstants.KINTAI14,
                AccumulateTestConstants.KINTAI15,
                AccumulateTestConstants.KINTAI16,
                AccumulateTestConstants.KINTAI17,
                AccumulateTestConstants.KINTAI25,
                AccumulateTestConstants.KINTAI21,
                AccumulateTestConstants.KINTAI22,
                AccumulateTestConstants.KINTAI23,
                AccumulateTestConstants.KINTAI24,
                AccumulateTestConstants.KINTAI27,
                AccumulateTestConstants.KINTAI28
        );

        final BatchSchoolMaster batchSchoolMaster = AccumulateTestConstants.BATCHSCHOOLMASTER;
        judge = new OnedayAttendanceJudge(null, batchSchoolMaster, kintaiManager);
    }

    public void test最初は全てゼロ() throws Exception {
        assertEquals(0, accum.getAbroad().size());
        assertEquals(0, accum.getAbsent().size());
        assertEquals(0, accum.getEarly().size());
        assertEquals(0, accum.getLate().size());
        assertEquals(0, accum.getLesson().size());
        assertEquals(0, accum.getMourning().size());
        assertEquals(0, accum.getNonotice().size());
        assertEquals(0, accum.getNotice().size());
        assertEquals(0, accum.getSick().size());
        assertEquals(0, accum.getSuspend().size());
    }

    // XX = 公欠、出停、忌引
    public void testXXが1つ以上ある() throws Exception {
        AccumulateSemes semes = new AccumulateSemes();

        // 出欠が空っぽ
        semes.calc(null, student, coll, d20061110, judge);
        assertEquals(0, semes.getAbsent().size());

        // XXが複数
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE0, AccumulateTestConstants.KINTAI1));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE1, AccumulateTestConstants.KINTAI1));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE2, AccumulateTestConstants.KINTAI1));
        semes.calc(null, student, coll, d20061110, judge);
        assertEquals(1, semes.getAbsent().size());

        // XX以外
        semes = new AccumulateSemes();
        coll.clear();
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE0, AccumulateTestConstants.KINTAI0));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE1, AccumulateTestConstants.KINTAI4));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE2, AccumulateTestConstants.KINTAI5));
        semes.calc(null, student, coll, d20061110, judge);
        assertEquals(0, semes.getAbsent().size());

        // 混在
        semes = new AccumulateSemes();
        coll.clear();
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE0, AccumulateTestConstants.KINTAI0));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE1, AccumulateTestConstants.KINTAI1));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE2, AccumulateTestConstants.KINTAI5));
        semes.calc(null, student, coll, d20061110, judge);
        assertEquals(1, semes.getAbsent().size());
    }

    public void test全て欠席() throws Exception {
        AccumulateSemes semes = new AccumulateSemes();

        // 出欠が空っぽ
        semes.calc(null, student, coll, d20061110, judge);
        assertEquals(0, semes.getSick().size());
        assertEquals(0, semes.getNotice().size());
        assertEquals(0, semes.getNonotice().size());

        // 全て欠席＆先頭が病欠
        semes = new AccumulateSemes();
        coll.clear();
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE0, AccumulateTestConstants.KINTAI4));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE1, AccumulateTestConstants.KINTAI5));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE2, AccumulateTestConstants.KINTAI6));
        semes.calc(null, student, coll, d20061110, judge);
        assertEquals(1, semes.getSick().size());
        assertEquals(0, semes.getNotice().size());
        assertEquals(0, semes.getNonotice().size());

        // 全て欠席＆先頭が事故欠(届)
        semes = new AccumulateSemes();
        coll.clear();
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE0, AccumulateTestConstants.KINTAI5));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE1, AccumulateTestConstants.KINTAI4));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE2, AccumulateTestConstants.KINTAI6));
        semes.calc(null, student, coll, d20061110, judge);
        assertEquals(0, semes.getSick().size());
        assertEquals(1, semes.getNotice().size());
        assertEquals(0, semes.getNonotice().size());

        // 混在
        semes = new AccumulateSemes();
        coll.clear();
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE0, AccumulateTestConstants.KINTAI4));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE1, AccumulateTestConstants.KINTAI0));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE2, AccumulateTestConstants.KINTAI6));
        semes.calc(null, student, coll, d20061110, judge);
        assertEquals(0, semes.getSick().size());
        assertEquals(0, semes.getNotice().size());
        assertEquals(0, semes.getNonotice().size());
    }

    public void test遅刻() throws Exception {
        AccumulateSemes semes = new AccumulateSemes();

        // 出欠が空っぽ
        semes.calc(null, student, coll, d20061110, judge);
        assertEquals(0, semes.getLate().size());

        // 最初の授業が遅刻
        semes = new AccumulateSemes();
        coll.clear();
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE0, AccumulateTestConstants.KINTAI15));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE1, AccumulateTestConstants.KINTAI4));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE2, AccumulateTestConstants.KINTAI6));
        semes.calc(null, student, coll, d20061110, judge);
        assertEquals(1, semes.getLate().size());

        // 最初の授業が遅刻(無)
        semes = new AccumulateSemes();
        coll.clear();
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE0, AccumulateTestConstants.KINTAI21)); // ★
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE1, AccumulateTestConstants.KINTAI4));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE2, AccumulateTestConstants.KINTAI6));
        semes.calc(null, student, coll, d20061110, judge);
        assertEquals(1, semes.getLate().size() + semes.getLateNonotice().size());

        // 最初の授業=欠席:[病欠/事故欠(届)/事故欠(無)] && その他=出席がある
        semes = new AccumulateSemes();
        coll.clear();
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE0, AccumulateTestConstants.KINTAI4));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE1, AccumulateTestConstants.KINTAI5));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE2, AccumulateTestConstants.KINTAI0));
        semes.calc(null, student, coll, d20061110, judge);
        assertEquals(1, semes.getLate().size());
    }

    public void test早退() throws Exception {
        AccumulateSemes semes = new AccumulateSemes();

        // 出欠が空っぽ
        semes.calc(null, student, coll, d20061110, judge);
        assertEquals(0, semes.getEarly().size());

        // 最後の授業が早退
        semes = new AccumulateSemes();
        coll.clear();
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE0, AccumulateTestConstants.KINTAI15));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE1, AccumulateTestConstants.KINTAI4));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE2, AccumulateTestConstants.KINTAI16));
        semes.calc(null, student, coll, d20061110, judge);
        assertEquals(1, semes.getEarly().size());

        // 最後の授業が早退(無)
        semes = new AccumulateSemes();
        coll.clear();
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE0, AccumulateTestConstants.KINTAI15));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE1, AccumulateTestConstants.KINTAI14));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE2, AccumulateTestConstants.KINTAI22)); // ★
        semes.calc(null, student, coll, d20061110, judge);
        assertEquals(1, semes.getEarly().size() + semes.getEarlyNonotice().size());

        // 最後の授業=欠席:[病欠/事故欠(届)/事故欠(無)] && その他=出席がある
        semes = new AccumulateSemes();
        coll.clear();
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE0, AccumulateTestConstants.KINTAI0));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE1, AccumulateTestConstants.KINTAI1));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE2, AccumulateTestConstants.KINTAI4));
        semes.calc(null, student, coll, d20061110, judge);
        assertEquals(1, semes.getEarly().size());
    }

    // [Absent, Suspend, Mourning]
    public void testCalc() throws Exception {
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE0, AccumulateTestConstants.KINTAI1));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE1, AccumulateTestConstants.KINTAI2));
        coll.add(createAttendance(AccumulateTestConstants.SCHEDULE2, AccumulateTestConstants.KINTAI3));

        accum.calc(null, student, coll, d20061110, judge);
        assertEquals(0, accum.getAbroad().size());
        assertEquals(1, accum.getAbsent().size());
        assertEquals(0, accum.getEarly().size());
        assertEquals(0, accum.getLate().size());
        assertEquals(1, accum.getLesson().size());
        assertEquals(1, accum.getMourning().size());
        assertEquals(0, accum.getNonotice().size());
        assertEquals(0, accum.getNotice().size());
        assertEquals(0, accum.getSick().size());
        assertEquals(1, accum.getSuspend().size());
    }

    public void testKekka() throws Exception {
        KenjaDateImpl date1 = KenjaDateImpl.getInstance(2009, 1, 22);
        AccumulateSchedule schedule10 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD0, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule11 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD1, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule12 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD2, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule13 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD3, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule14 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD4, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule15 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD5, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);

        // 欠課が連続 => 欠課回数は1回
        // 欠課の勤怠コードは4(病欠), 5(事故欠(届)), 6(事故欠(無))

        coll.add(createAttendance(schedule10, AccumulateTestConstants.KINTAI4));
        coll.add(createAttendance(schedule11, AccumulateTestConstants.KINTAI5));
        coll.add(createAttendance(schedule12, AccumulateTestConstants.KINTAI6));
        coll.add(createAttendance(schedule13, AccumulateTestConstants.KINTAI6));
        coll.add(createAttendance(schedule14, AccumulateTestConstants.KINTAI5));
        coll.add(createAttendance(schedule15, AccumulateTestConstants.KINTAI4));
        accum.calc(null, student, coll, date1, judge);
        assertEquals(0, accum.getKekka().size()); // すべて欠課なら欠課回数は0
    }

    public void testKekka2() throws Exception {
        KenjaDateImpl date1 = KenjaDateImpl.getInstance(2009, 1, 22);
        AccumulateSchedule schedule10 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD0, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule11 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD1, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule12 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD2, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule13 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD3, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule14 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD4, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule15 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD5, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);

        // 欠課が連続 => 欠課回数は1回
        // 欠課の勤怠コードは4(病欠), 5(事故欠(届)), 6(事故欠(無))

        coll.add(createAttendance(schedule10, AccumulateTestConstants.KINTAI5)); // +1
        coll.add(createAttendance(schedule11, AccumulateTestConstants.KINTAI0));
        coll.add(createAttendance(schedule12, AccumulateTestConstants.KINTAI5)); // +1
        coll.add(createAttendance(schedule13, AccumulateTestConstants.KINTAI0));
        coll.add(createAttendance(schedule14, AccumulateTestConstants.KINTAI5)); // +1
        coll.add(createAttendance(schedule15, AccumulateTestConstants.KINTAI0));
        accum.calc(null, student, coll, date1, judge);
        assertEquals(3, accum.getKekka().size());
    }

    public void testKekka3() throws Exception {
        KenjaDateImpl date1 = KenjaDateImpl.getInstance(2009, 1, 22);
        AccumulateSchedule schedule10 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD0, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule11 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD1, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule12 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD2, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule13 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD3, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule14 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD4, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule15 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD5, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);

        // 欠課が連続 => 欠課回数は1回
        // 欠課の勤怠コードは4(病欠), 5(事故欠(届)), 6(事故欠(無))

        coll.add(createAttendance(schedule10, AccumulateTestConstants.KINTAI5)); // +1
        coll.add(createAttendance(schedule11, AccumulateTestConstants.KINTAI5));
        coll.add(createAttendance(schedule12, AccumulateTestConstants.KINTAI0));
        coll.add(createAttendance(schedule13, AccumulateTestConstants.KINTAI5)); // +1
        coll.add(createAttendance(schedule14, AccumulateTestConstants.KINTAI5));
        coll.add(createAttendance(schedule15, AccumulateTestConstants.KINTAI5));
        accum.calc(null, student, coll, date1, judge);
        assertEquals(2, accum.getKekka().size());
    }

    public void testNoCount() throws Exception {
        KenjaDateImpl date1 = KenjaDateImpl.getInstance(2009, 1, 22);
        AccumulateSchedule schedule10 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD0, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule11 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD1, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule12 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD2, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule13 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD3, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule14 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD4, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
        AccumulateSchedule schedule15 = new AccumulateSchedule(date1, AccumulateTestConstants.PERIOD5, AccumulateTestConstants.CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);

        // ひとつでもカウントなしがある場合、授業日数にもカウントしない0

        coll.add(createAttendance(schedule10, AccumulateTestConstants.KINTAI27));
        coll.add(createAttendance(schedule11, AccumulateTestConstants.KINTAI0));
        coll.add(createAttendance(schedule12, AccumulateTestConstants.KINTAI0));
        coll.add(createAttendance(schedule13, AccumulateTestConstants.KINTAI0));
        coll.add(createAttendance(schedule14, AccumulateTestConstants.KINTAI0));
        coll.add(createAttendance(schedule15, AccumulateTestConstants.KINTAI0));
        accum.calc(null, student, coll, date1, judge);
        assertEquals(0, accum.getLesson().size());
    }

    private Attendance createAttendance(
            final AccumulateSchedule schedule,
            final Kintai kintai
    ) {
        return new Attendance(
                AccumulateTestConstants.CATEOGRY,
                AccumulateTestConstants.STUDENT1,
                schedule,
                kintai,
                AccumulateTestConstants.BLANK_REMARK
        );
    }
} // AccumulateSemesTest

// eof
