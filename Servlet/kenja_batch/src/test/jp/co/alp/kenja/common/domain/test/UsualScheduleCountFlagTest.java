// kanji=漢字
/*
 * $Id: UsualScheduleCountFlagTest.java 74566 2020-05-27 13:15:39Z maeshiro $
 *
 * 作成日: 2005/05/02 20:12:50 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.util.HashMap;
import java.util.Map;

import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.GroupClass;
import jp.co.alp.kenja.common.domain.HomeRoom;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Period;
import jp.co.alp.kenja.common.domain.SubClass;
import jp.co.alp.kenja.common.domain.UsualSchedule;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

import junit.framework.TestCase;

/**
 * 通常時間割の「集計フラグ」のテスト。
 * あいまい且つNPE発生し得る実装だったので、「集計フラグ」だけをテスト。
 *
 * @author tamura
 * @version $Id: UsualScheduleCountFlagTest.java 74566 2020-05-27 13:15:39Z maeshiro $
 */
public class UsualScheduleCountFlagTest extends TestCase {

    private MyEnum.Category _category;
    private HomeRoom _hr1, _hr2, _hr3;
    private KenjaDateImpl _d0502;
    private Period _ps, _p1;
    private SubClass _subClass;
    private Chair _chair;

    public UsualScheduleCountFlagTest(final String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        _category = new MyEnum.Category();
        _hr1 = HomeRoom.create(_category, "01", "J01", "1年J01組", "1J01");
        _hr2 = HomeRoom.create(_category, "01", "J02", "1年J02組", "1J02");
        _hr3 = HomeRoom.create(_category, "01", "J03", "1年J03組", "1J03");

        _d0502 = KenjaDateImpl.getInstance(2005, 5, 2);
        _ps = Period.create(_category, "1", "ＳＨａ", "Sa", null, null);
        _p1 = Period.create(_category, "2", "１校時", "1", null, null);
        _subClass = SubClass.create(_category, "1", "H", "2", "1234", "体育科目", "体育");
        _chair = Chair.create(_category, "1234567", GroupClass.ZERO, _subClass, "体育1", new Integer(3), new Integer(1), false);
        _chair.addHomeRoom(_hr1);
        _chair.addHomeRoom(_hr2);
        _chair.addHomeRoom(_hr3);
    }

    private static UsualSchedule createUsualSchedule(
            final KenjaDateImpl date,
            final Period period,
            final Chair chair
    ) {
        return new UsualSchedule(
                date,
                period,
                chair,
                UsualSchedule.RollCalledDiv.NOTYET,
                UsualSchedule.DataDiv.BASIC
        );
    }

    public void testGetCountFlags() {
        final Map<HomeRoom, Boolean> map = new HashMap<HomeRoom, Boolean>();
        map.put(_hr1, Boolean.TRUE);
//        sch.setCountFlags(map);
    }

    public void testGetCountFlag0() {
        final UsualSchedule sch = createUsualSchedule(_d0502, _p1, _chair);

        try {
            assertEquals(false, sch.getCountFlag(null));
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(NullPointerException.class, e.getClass());
        }

        assertEquals(true, sch.getCountFlag(_hr1));
        assertEquals(true, sch.getCountFlag(_hr2));
        assertEquals(true, sch.getCountFlag(_hr3));
    }

    public void testGetCountFlag1() {
        final UsualSchedule sch = createUsualSchedule(_d0502, _p1, _chair);

        try {
            assertEquals(false, sch.getCountFlag(null));
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(NullPointerException.class, e.getClass());
        }

        assertEquals(true, sch.getCountFlag(_hr1));
        assertEquals(false, sch.getCountFlag(_hr2));
        assertEquals(true, sch.getCountFlag(_hr3));
    }

    public void testGetCountFlag2() {
        final UsualSchedule sch = createUsualSchedule(_d0502, _p1, _chair);

        // 「true:集計する」と「false:集計しない」を1件づつ計2件追加する
        sch.setCountFlag(_hr1, false);
        sch.setCountFlag(_hr2, true);

        assertEquals(false, sch.getCountFlag(_hr1));
        assertEquals(true, sch.getCountFlag(_hr2));
        assertEquals(true, sch.getCountFlag(_hr3));
    }

    public void testGetCountFlagNull() {
        final UsualSchedule sch = createUsualSchedule(_d0502, _p1, _chair);
        try {
            assertEquals(false, sch.getCountFlag(null));
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(NullPointerException.class, e.getClass());
        }
    }

    public void testSetCountFlagNull() {
        final UsualSchedule sch = createUsualSchedule(_d0502, _p1, _chair);
        try {
            sch.setCountFlag(null, false);
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(NullPointerException.class, e.getClass());
        }

        try {
            sch.setCountFlag(null, true);
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertSame(NullPointerException.class, e.getClass());
        }
    }
} // UsualScheduleCountFlagTest

// eof
