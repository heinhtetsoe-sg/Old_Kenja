/*
 * $Id: StudentAbsenceHigh.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2009/08/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.enums.ValuedEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 生徒欠課数上限
 * @version $Id: StudentAbsenceHigh.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public class StudentAbsenceHigh {

    /*pkg*/static final Log log = LogFactory.getLog(StudentAbsenceHigh.class);

    /** デフォルトの生徒欠課数上限 */
    private static final StudentAbsenceHigh DEFAULT_STUDENT_ABSENCE_HIGH = new StudentAbsenceHigh();

    static {
        DEFAULT_STUDENT_ABSENCE_HIGH.setAbsenceHigh(StudentAbsenceHigh.Div.BY_YEAR, AbsenceHigh.DEFAULT_ABSENCE_HIGH);
        DEFAULT_STUDENT_ABSENCE_HIGH.setAbsenceHigh(StudentAbsenceHigh.Div.BY_DATE, AbsenceHigh.DEFAULT_ABSENCE_HIGH);
    }

    private Map<StudentAbsenceHigh.Div, AbsenceHigh> _absenceHighs;

    /**
     * コンストラクタ
     */
    public StudentAbsenceHigh() {
        _absenceHighs = new TreeMap<StudentAbsenceHigh.Div, AbsenceHigh>();
    }

    /**
     * 区分の欠課数上限をセットする。
     * @param div 欠課数上限の区分
     * @param absenceHigh 欠課数上限
     */
    public void setAbsenceHigh(final StudentAbsenceHigh.Div div, final AbsenceHigh absenceHigh) {
        _absenceHighs.put(div, absenceHigh);
    }

    /**
     * 区分の欠課数上限を得る。
     * @param div 区分
     * @return 欠課数上限
     */
    public AbsenceHigh getAbsenceHigh(final StudentAbsenceHigh.Div div) {
        final AbsenceHigh absenceHigh = _absenceHighs.get(div);
        if (null == absenceHigh && DEFAULT_STUDENT_ABSENCE_HIGH.contains(div)) {
            return DEFAULT_STUDENT_ABSENCE_HIGH.getAbsenceHigh(div);
        }
        return absenceHigh;
    }

    private boolean contains(final StudentAbsenceHigh.Div div) {
        return _absenceHighs.containsKey(div);
    }

    /**
     * 欠課数上限
     */
    public static class AbsenceHigh {
        /** デフォルトの年間欠課数上限(上限値=0) */
        public static final AbsenceHigh DEFAULT_ABSENCE_HIGH = new AbsenceHigh(StudentAbsenceHigh.Div.DEFAULT, null, 0, 0, null);

        private final StudentAbsenceHigh.Div _div;
        private final double _compAbsenceHigh;
        private final double _getAbsenceHigh;
        private final KenjaDateImpl _date;

        private String _str;

        /**
         * コンストラクタ。
         * @param div 区分
         * @param student 生徒
         * @param compAbsenceHigh 欠課数上限値(履修)
         * @param getAbsenceHigh 欠課数上限値(修得)
         * @param date 日付
         */
        public AbsenceHigh(
                final StudentAbsenceHigh.Div div,
                final Student student,
                final double compAbsenceHigh,
                final double getAbsenceHigh,
                final KenjaDateImpl date
        ) {
            _div = div;
            _compAbsenceHigh = compAbsenceHigh;
            _getAbsenceHigh = getAbsenceHigh;
            _date = date;
            _str = _div.toString() + ":" + student + ": 欠課数上限値[履修, 修得] = [" + _compAbsenceHigh  + ", " + _getAbsenceHigh + "] (" + _date + ")";
        }

        /**
         * 区分を得る
         * @return 区分
         */
        public Div getDiv() {
            return _div;
        }

        /**
         * 欠課数上限値(履修) を得る
         * @return 欠課数上限値(履修)
         */
        public double getCompAbsenceHigh() {
            return _compAbsenceHigh;
        }

        /**
         * 欠課数上限値(修得) を得る
         * @return 欠課数上限値(修得)
         */
        public double getGetAbsenceHigh() {
            return _getAbsenceHigh;
        }

        /**
         * {@inheritDoc}
         */
        public String toTooltipString() {
            return "[履修, 修得] = [" + _compAbsenceHigh  + ", " + _getAbsenceHigh +  "]";
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _str;
        }
    }

    //========================================================================

    /**
     * 区分 : 年間=1 , 随時=2。
     */
    public static final class Div extends ValuedEnum {

        /** 年間 */
        public static final Div BY_YEAR = new Div("年間", 1);

        /** 随時 */
        public static final Div BY_DATE = new Div("随時", 2);

        /** デフォルト */
        public static final Div DEFAULT = new Div("デフォルト", -1);

        /*
         * コンストラクタ。
         */
        private Div(
                final String name,
                final int value
        ) {
            super(name, value);
        }

        /**
         * 区分を得る。
         * @param div 区分値
         * @return 区分
         */
        public static Div getDiv(final int div) {
            if (div == 2) {
                return BY_DATE;
            }
            return BY_YEAR;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return getName();
        }
    } // Div
}
