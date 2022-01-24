// kanji=漢字
/*
 * $Id: AccumulateTestConstants.java 74566 2020-05-27 13:15:39Z maeshiro $
 *
 * 作成日: 2007/01/08 14:49:27 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.test;

import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.Gender;
import jp.co.alp.kenja.common.domain.Grade;
import jp.co.alp.kenja.common.domain.GroupClass;
import jp.co.alp.kenja.common.domain.HomeRoom;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Kintai;
import jp.co.alp.kenja.common.domain.Period;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.domain.SubClass;
import jp.co.alp.kenja.common.domain.UsualSchedule;
import jp.co.alp.kenja.common.domain.Student.GrdDiv;
import jp.co.alp.kenja.common.domain.UsualSchedule.RollCalledDiv;
import jp.co.alp.kenja.common.lang.enums.MyEnum;
import jp.co.alp.kenja.batch.accumulate.Attendance;
import jp.co.alp.kenja.batch.accumulate.AccumulateSchedule;
import jp.co.alp.kenja.batch.accumulate.BatchSchoolMaster;

/**
 * <<クラスの説明>>。
 * @author takaesu
 * @version $Id: AccumulateTestConstants.java 74566 2020-05-27 13:15:39Z maeshiro $
 */
public class AccumulateTestConstants {
    public static final MyEnum.Category CATEOGRY = new MyEnum.Category();;

    // 勤怠
    /** SEATED */
    public static Kintai KINTAI0 =  Kintai.create(CATEOGRY, "0",  "出席", "−", "0");
    /** ABSENT */
    public static Kintai KINTAI1 =  Kintai.create(CATEOGRY, "1",  "公欠", "公", "1");
    /** SUSPEND */
    public static Kintai KINTAI2 =  Kintai.create(CATEOGRY, "2",  "出停", "△", "2");
    /** MOURNING */
    public static Kintai KINTAI3 =  Kintai.create(CATEOGRY, "3",  "忌引", "キ", "3");
    /** SICK */
    public static Kintai KINTAI4 =  Kintai.create(CATEOGRY, "4",  "病欠", "病", "4");
    /** NOTICE */
    public static Kintai KINTAI5 =  Kintai.create(CATEOGRY, "5",  "事故欠(届)", "届", "5");
    /** NONOTICE */
    public static Kintai KINTAI6 =  Kintai.create(CATEOGRY, "6",  "事故欠(無)", "無", "6");
    /** NURSEOFF */
    public static Kintai KINTAI14 = Kintai.create(CATEOGRY, "14", "保健室欠課", "ホ", "14");
    /** LATE */
    public static Kintai KINTAI15 = Kintai.create(CATEOGRY, "15", "遅刻", "チ", "15");
    /** EARLY */
    public static Kintai KINTAI16 = Kintai.create(CATEOGRY, "16", "早退", "ソ", "16");
    /** VIRUS */
    public static Kintai KINTAI17 = Kintai.create(CATEOGRY, "17", "出停(伝染病)", "伝", "17");
    /** KOUDOME */
    public static Kintai KINTAI25 = Kintai.create(CATEOGRY, "25", "出停(交止)", "交", "25");
    /** LATE_NONOTICE */
    public static Kintai KINTAI21 = Kintai.create(CATEOGRY, "21", "遅刻(無)", "チ", "21");
    /** EARLY_NONOTICE */
    public static Kintai KINTAI22 = Kintai.create(CATEOGRY, "22", "早退(無)", "ソ", "22");
    /** LATE2 */
    public static Kintai KINTAI23 = Kintai.create(CATEOGRY, "23", "遅刻2", "チ2", "23");
    /** LATE3 */
    public static Kintai KINTAI24 = Kintai.create(CATEOGRY, "24", "遅刻3", "チ3", "24");
    /** NO_COUNT */
    public static Kintai KINTAI27 = Kintai.create(CATEOGRY, "27", "カウントなし", "NC", "27");
    /** NO_COUNT2 */
    public static Kintai KINTAI28 = Kintai.create(CATEOGRY, "28", "カウントなし", "NC2", "28");

    // 校時
    public static Period PERIOD0 = Period.create(CATEOGRY, "0", "0校時", "0校時", null, null);
    public static Period PERIOD1 = Period.create(CATEOGRY, "1", "1校時", "1校時", null, null);
    public static Period PERIOD2 = Period.create(CATEOGRY, "2", "2校時", "2校時", null, null);
    public static Period PERIOD3 = Period.create(CATEOGRY, "3", "3校時", "3校時", null, null);
    public static Period PERIOD4 = Period.create(CATEOGRY, "4", "4校時", "4校時", null, null);
    public static Period PERIOD5 = Period.create(CATEOGRY, "5", "5校時", "5校時", null, null);

    public static Grade GRADE1 = Grade.create(CATEOGRY, "1年");
    public static HomeRoom HR1_1 = HomeRoom.create(CATEOGRY, GRADE1, "1組", "1年1組", "1-1");
    public static Student STUDENT1 = Student.create(
            CATEOGRY,
            "12345678",
            "0",
            "太郎",
            "タロウ",
            "taro",
            Gender.MALE,
            GrdDiv.NORMAL,
            null,
            HR1_1,
            "1234",
            true
    );
    public static SubClass SUBCLASS1 = SubClass.create(CATEOGRY, "01", "H", "2", "010000", "国語I", "国I");
    public static Chair CHAIR1 = Chair.create(
            CATEOGRY,
            "1",
            GroupClass.ZERO,
            SUBCLASS1,
            "国語",
            new Integer(1),
            new Integer(1),
            false
    );

    // 時間割
    private static KenjaDateImpl date = KenjaDateImpl.getInstance(2006, 11, 10);
    public static final String BLANK_REMARK = "";
    public static AccumulateSchedule SCHEDULE0 = new AccumulateSchedule(date, PERIOD0, CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
    public static AccumulateSchedule SCHEDULE1 = new AccumulateSchedule(date, PERIOD1, CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);
    public static AccumulateSchedule SCHEDULE2 = new AccumulateSchedule(date, PERIOD2, CHAIR1, RollCalledDiv.FINISHED, UsualSchedule.DataDiv.USUAL);

    public static Attendance ATTENDANCE1 = new Attendance(
            CATEOGRY,
            STUDENT1,
            SCHEDULE1,
            KINTAI1,
            BLANK_REMARK
    );

    static {
        CHAIR1.addHomeRoom(HR1_1);
    }

    private AccumulateTestConstants() {}

    public static Attendance createAccumulateAttendance() {
        return new Attendance(
                CATEOGRY,
                STUDENT1,
                null,
                null,
                BLANK_REMARK
        );
    }
    
    public static BatchSchoolMaster BATCHSCHOOLMASTER = new BatchSchoolMaster(0, 0, 0, 0, 0, 0, 1, 1, 3, 1, 5, 1, 3, 1, 5, 50, 60, 35, 0, 0, null);

//    public static Period createPeriod1(final Category category, final String code) {
//        return Period.create(category, code, code + "校時", code + "校時", null);
//    }

} // AccumulateTestConstants

// eof
