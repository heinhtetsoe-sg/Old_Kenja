// kanji=漢字
/*
 * $Id: AccumulateScheduleTest.java 74566 2020-05-27 13:15:39Z maeshiro $
 *
 * 作成日: 2007/01/08 11:28:42 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.test;

import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.Grade;
import jp.co.alp.kenja.common.domain.GroupClass;
import jp.co.alp.kenja.common.domain.HomeRoom;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Period;
import jp.co.alp.kenja.common.domain.SubClass;
import jp.co.alp.kenja.common.domain.UsualSchedule;
import jp.co.alp.kenja.common.domain.UsualSchedule.RollCalledDiv;
import jp.co.alp.kenja.common.lang.enums.MyEnum;
import jp.co.alp.kenja.batch.accumulate.AccumulateSchedule;
import junit.framework.TestCase;

public class AccumulateScheduleTest extends TestCase {
    private MyEnum.Category category;

    private AccumulateSchedule _schedule;
    private Grade grade;
    private HomeRoom homeRoom;

    public AccumulateScheduleTest() {}

    protected void setUp() throws Exception {
        category = new MyEnum.Category();
        final SubClass s1 = SubClass.create(category, "01", "H", "2", "010000", "国語I", "国I");
        final KenjaDateImpl date = KenjaDateImpl.getInstance(2006, 11, 10);
        final Period period = Period.create(category, "2", "2校時", "2校時", null, null);
        final Chair chair = Chair.create(category, "1", GroupClass.ZERO, s1, "国語", null, null, false);
        final RollCalledDiv rollCall = RollCalledDiv.FINISHED;
        final UsualSchedule.DataDiv dataDiv = UsualSchedule.DataDiv.USUAL;
        
        grade = Grade.create(category, "1年");
        homeRoom = HomeRoom.create(category, grade, "1組", "1年1組", "1-1");

        chair.addHomeRoom(homeRoom);
        _schedule = new AccumulateSchedule(date, period, chair, rollCall, dataDiv);
    }

    protected void tearDown() throws Exception {
        _schedule = null;
    }

    public void testCountFlag() throws Exception {
        // 年組に該当しない場合は、講座の集計フラグ
        boolean countFlag = _schedule.countFlag(null);
        assertFalse(countFlag);

        // 講座の持っている年組と一致しないので、講座の集計フラグ
        final HomeRoom otherHomeRoom = HomeRoom.create(category, grade, "x組", "1年x組", "1-x");

        _schedule.setCountFlag(otherHomeRoom, true);
        assertFalse(_schedule.countFlag(otherHomeRoom));

        _schedule.setCountFlag(otherHomeRoom, false);
        assertFalse(_schedule.countFlag(otherHomeRoom));

        // 明示的に年組に集計フラグを設定する
        _schedule.setCountFlag(homeRoom, true);
        assertTrue(_schedule.countFlag(homeRoom));

        _schedule.setCountFlag(homeRoom, false);
        assertFalse(_schedule.countFlag(homeRoom));
    }

    public void testHasCountFlag() throws Exception {
        assertFalse(_schedule.hasCountFlag(homeRoom));

        _schedule.setCountFlag(homeRoom, false);
        assertTrue(_schedule.hasCountFlag(homeRoom));

        _schedule.setCountFlag(homeRoom, true);
        assertTrue(_schedule.hasCountFlag(homeRoom));
    }

    public void testIsRollCalledDivNOTYET() throws Exception {
        assertFalse(_schedule.isRollCalledDivNOTYET());
    }
} // AccumulateScheduleTest

// eof
