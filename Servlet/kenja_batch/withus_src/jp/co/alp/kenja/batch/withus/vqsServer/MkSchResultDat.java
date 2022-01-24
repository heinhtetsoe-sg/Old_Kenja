// kanji=漢字
/*
 * $Id: MkSchResultDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/05/02 11:36:05 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.vqsServer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.Database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.withus.Clazz;
import jp.co.alp.kenja.batch.withus.SubClass;
import jp.co.alp.kenja.batch.withus.WithusUtils;

/**
 * 生徒成績情報テーブル。
 * @author takaesu
 * @version $Id: MkSchResultDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkSchResultDat extends Mk {
    /* デバッグ(確認作業)に役立つSQL
     * ◆レポート点数関連以外
     * SELECT schregno, seqno, subclasscd, grade, comp_credit, schooling, vqsc_norma, vqsc_attend, vqsc_remain, grad_value, get_credit, create_date FROM sch_result_dat;
     *
     * ◆レポート点数関連
     * SELECT schregno, seqno, subclasscd, grade, standard_num, non_present_reports, report01, report02, report03 FROM sch_result_dat;
     */

    /*pkg*/static final Log log = LogFactory.getLog(MkSchResultDat.class);

    /** レポート最大回数。 */
    private static final int MAX_REPORT_NUM = 18;

    /** VQSのスクーリング種別. */
    private static final String SCHOOLING_TYPE = "M3";// TAKAESU: hard cording!

    private Map _classMst;
    private Map _subclassMst;
    final String _yesterday;

    /*
     * hoge=# \d sch_result_dat
     *                  Table "public.sch_result_dat"
     *        Column        |            Type             | Modifiers
     * ---------------------+-----------------------------+-----------
     *  schregno            | character varying(8)        | not null
     *  seqno               | integer                     | not null
     *  classcd             | character varying(2)        | not null
     *  subclasscd          | character varying(6)        | not null
     *  year                | integer                     | not null
     *  grade               | integer                     | not null
     *  comp_credit         | integer                     | not null
     *  schooling           | integer                     | not null
     *  vqsc_norma          | integer                     | not null
     *  vqsc_attend         | integer                     | not null
     *  vqsc_remain         | integer                     | not null
     *  standard_num        | integer                     | not null
     *  non_present_reports | integer                     | not null
     *  report01            | integer                     | not null
     *  report02            | integer                     | not null
     *  report03            | integer                     | not null
     *  report04            | integer                     | not null
     *  report05            | integer                     | not null
     *  report06            | integer                     | not null
     *  report07            | integer                     | not null
     *  report08            | integer                     | not null
     *  report09            | integer                     | not null
     *  report10            | integer                     | not null
     *  report11            | integer                     | not null
     *  report12            | integer                     | not null
     *  report13            | integer                     | not null
     *  report14            | integer                     | not null
     *  report15            | integer                     | not null
     *  report16            | integer                     | not null
     *  report17            | integer                     | not null
     *  report18            | integer                     | not null
     *  grad_value          | integer                     | not null
     *  get_credit          | integer                     | not null
     *  create_date         | timestamp without time zone | not null
     *  updated             | timestamp without time zone | not null
     * Indexes:
     *     "pk_sch_result_dat" primary key, btree (schregno, classcd, subclasscd, "year")
     */

    public MkSchResultDat(final Param param, final Database knj, final Database vqs, final String title) throws SQLException {
        super(param, knj, vqs, title);

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(_param.getUpdate());
        calendar.add(Calendar.DATE, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        _yesterday = new SimpleDateFormat("yyyy-MM-dd H:m:s").format(calendar.getTime());

        loadMasterData(param, knj);

        final List list = loadKnj(knj);
        log.debug("賢者:データ数=" + list.size());

        saveVqs(list, vqs);
    }

    public List loadKnj(final Database knj) throws SQLException {
        final List rtn = new ArrayList();

        final Map students = Student.loadStudent(knj, _param);
        log.info("生徒数=" + students.size());

        // 生徒の履修科目を得る
        for (final Iterator it = students.values().iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            setSubClassesAndSchooling(knj, student);
            setReportData(knj, student);
            setCreditAdmits(knj, student);
            setSchoolingDat(knj, student);

            // rtn にデータをセット
            for (final Iterator it2 = student._subClasses.values().iterator(); it2.hasNext();) {
                final MySubClass mySubclass = (MySubClass) it2.next();

                final SchResultDat data = new SchResultDat(student, mySubclass, _param._year);
                rtn.add(data);
            }
        }

        return rtn;
    }

    private void saveVqs(final List list, final Database vqs) throws SQLException {
        int count = 0;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final SchResultDat data = (SchResultDat) it.next();
            log.info(data.getKeyString());

            try {
                int cnt = _runner.update(vqs.conn, getUpdateSql(), data.toUpdateArray());
                if (0 == cnt) {
                    final String sql;
                    sql = "INSERT INTO sch_result_dat"
                        + "  VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,current_timestamp)";
                    cnt = _runner.update(vqs.conn, sql, data.toInsertArray());
                }
                count += cnt;
            } catch (final SQLException e) {
                log.error("VQSに更新でエラー");
                throw e;
            }
            vqs.commit();
        }
        log.debug("VQS:データ数=" + count);
    }

    private String getUpdateSql() {
        final String rtn;
        rtn = "UPDATE sch_result_dat SET"
            + " seqno=?,"
            + " grade=?,"
            + " comp_credit=?,"
            + " schooling=?,"
            + " vqsc_norma=?,"
            + " vqsc_attend=?,"
            + " vqsc_remain=?,"
            + " standard_num=?,"
            + " non_present_reports=?,"
            + " report01=?,"
            + " report02=?,"
            + " report03=?,"
            + " report04=?,"
            + " report05=?,"
            + " report06=?,"
            + " report07=?,"
            + " report08=?,"
            + " report09=?,"
            + " report10=?,"
            + " report11=?,"
            + " report12=?,"
            + " report13=?,"
            + " report14=?,"
            + " report15=?,"
            + " report16=?,"
            + " report17=?,"
            + " report18=?,"
            + " grad_value=?,"
            + " get_credit=?,"
            + " create_date=?,"
            + " updated=current_timestamp"
            + " WHERE schregno=?"
            + " AND classcd=?"
            + " AND subclasscd=?"
            + " AND year='" + _param._year + "'"
            ;
        return rtn;
    }

    private void loadMasterData(final Param param, final Database knj) throws SQLException {
        _classMst = Clazz.loadClassMst(knj, param._year);
        log.debug("教科マスタ件数=" + _classMst.size());

        _subclassMst = loadSubClass(knj, param._year);
        log.debug("科目マスタ件数=" + _subclassMst.size());
    }

    private Map loadSubClass(final Database db, final String year) throws SQLException {
        final Map rtn = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db.prepareStatement(sqlSubclass(year));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String clazzCd = rs.getString("classcd");
            final String curriculumCd = rs.getString("curriculum_cd");
            final String code = rs.getString("subclasscd");
            final String name = rs.getString("subclassname");
            final String abbv = rs.getString("subclassabbv");
            final String showOrder = rs.getString("showorder3");
            final Integer credits = new Integer(rs.getString("credits"));
            Integer reportSeq = new Integer(rs.getString("report_seq"));
            if (null != reportSeq && reportSeq.intValue() > MAX_REPORT_NUM) {
                final String msg = "教科+教育課程CD+科目=" + clazzCd + curriculumCd + code;
                log.warn(msg + ":レポート回数がVQSに記録できる数(" + MAX_REPORT_NUM + ")を超えてるので切詰めた。:" + reportSeq);
                reportSeq = new Integer(MAX_REPORT_NUM);
            }
            final Integer schoolingSeq = new Integer(rs.getString("schooling_seq"));

            final MySubClass mySubclass = new MySubClass(clazzCd, curriculumCd, code, name, abbv, showOrder, reportSeq, schoolingSeq, credits);
            rtn.put(clazzCd + curriculumCd + code, mySubclass);
        }
        return rtn;
    }

    private String sqlSubclass(final String year) {
        final String sql;
        sql = "SELECT"
            + "  t1.classcd,"
            + "  t1.curriculum_cd,"
            + "  t1.subclasscd,"
            + "  t1.subclassname,"
            + "  t1.subclassabbv,"
            + "  t1.showorder3,"
            + "  t2.credits,"
            + "  t2.report_seq,"
            + "  t2.schooling_seq"
            + " FROM v_subclass_mst t1 INNER JOIN subclass_details_mst t2 ON"
            + "  t1.year = t2.year AND"
            + "  t1.classcd = t2.classcd AND"
            + "  t1.curriculum_cd = t2.curriculum_cd AND"
            + "  t1.subclasscd = t2.subclasscd"
            + " WHERE t1.year='" + year + "'";
        return sql;
    }

    private void setReportData(final Database knj, final Student student) throws SQLException {
        final String sql = sqlReportData();

        final List result;
        try {
            result = (List) _runner.query(knj.conn, sql, student._schregno, _handler);
        } catch (final SQLException e) {
            log.error("生徒のレポート情報取得でエラー" ,e);
            throw e;
        }
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map element = (Map) it.next();
            final String key = (String) element.get("key");
            final MySubClass subClass = (MySubClass) student._subClasses.get(key);
            if (null == subClass) {
                continue;
            }

            final Integer seq = (Integer) element.get("report_seq");
            final Integer score1 = (Integer) element.get("commited_score1");
            final Integer score2 = (Integer) element.get("commited_score2");
            final Report report = new Report(seq.intValue(), score1, score2);

            // 情報をセット
            subClass._reports[seq.intValue() - 1] = report;
        }
    }

    private String sqlReportData() {
        final String sql;
            sql = "SELECT"
                + "  classcd || curriculum_cd || subclasscd AS key,"
                + "  report_seq,"
                + "  commited_score1,"
                + "  commited_score2"
                + " FROM"
                + "  rec_report_dat"
                + " WHERE"
                + "  year='" + _param._year + "' AND"
                + "  schregno=?"
                ;
        return sql;
    }

    private void setSubClassesAndSchooling(final Database knj, final Student student) throws SQLException {
        final String sql = sqlSubClassesAndSchooling();

        final List result;
        try {
            result = (List) _runner.query(knj.conn, sql, student._schregno, _handler);
        } catch (final SQLException e) {
            log.error("生徒の履修科目取得でエラー" ,e);
            throw e;
        }
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map element = (Map) it.next();
            final String key = (String) element.get("key");
            final Integer compCredit = (Integer) element.get("comp_credit");
            final Date commitedDate = (Date) element.get("commited_e");
            final Integer rate = (Integer) element.get("rate");
            final MySubClass subClass = (MySubClass) _subclassMst.get(key);

            // 情報をセット
            final MySubClass studentSubClass = new MySubClass(subClass);    // 生徒毎に科目のインスタンスを持つ
            studentSubClass._compCredit = compCredit;
            studentSubClass._hasSchooling = (null != commitedDate);
            studentSubClass._rate = rate;
            student._subClasses.put(key, subClass);
        }
        log.debug(student + ", " + student._subClasses.values());
    }

    private String sqlSubClassesAndSchooling() {
        final String sql;
        sql = "SELECT"
            + "  t1.classcd || t1.curriculum_cd || t1.subclasscd AS key,"
            + "  t1.comp_credit,"
            + "  t2.commited_e,"
            + "  t2.rate"
            + " FROM"
            + "  comp_regist_dat t1 LEFT OUTER JOIN rec_schooling_rate_dat t2 ON"
            + "    t1.year=t2.year AND"
            + "    t1.classcd=t2.classcd AND"
            + "    t1.curriculum_cd=t2.curriculum_cd AND"
            + "    t1.subclasscd=t2.subclasscd AND"
            + "    t1.schregno=t2.schregno AND"
            + "    t2.schooling_type='" + SCHOOLING_TYPE + "'"
            + " WHERE"
            + "  t1.year='" + _param._year + "' AND"
            + "  t1.schregno=?"
            ;
        return sql;
    }

    private void setCreditAdmits(final Database knj, final Student student) throws SQLException {
        final String sql = sqlCreditAdmits(student);
        final List result;
        try {
            result = (List) _runner.query(knj.conn, sql, _handler);
        } catch (final SQLException e) {
            log.error("単位認定情報の取得でエラー" ,e);
            throw e;
        }
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map element = (Map) it.next();
            final String key = (String) element.get("key");
            final MySubClass subClass = (MySubClass) student._subClasses.get(key);
            if (null == subClass) {
                continue;
            }
            // 情報をセット
            subClass._gradValue = (Integer) element.get("grad_value");
            subClass._getCredit = (Integer) element.get("get_credit");
        }
    }

    private String sqlCreditAdmits(final Student student) {
        final String sql;
        sql = "SELECT"
            + "  classcd || curriculum_cd || subclasscd AS key,"
            + "  grad_value,"
            + "  get_credit"
            + " FROM"
            + "  rec_credit_admits"
            + " WHERE"
            + "  year='" + _param._year + "' AND"
            + "  schregno='" + student._schregno + "'"
            ;
        return sql;
    }

    private void setSchoolingDat(final Database knj, final Student student) throws SQLException {
        final String sql = sqlSchoolingDat();
        final List result;
        try {
            result = (List) _runner.query(knj.conn, sql, student._schregno, _handler);
        } catch (final SQLException e) {
            log.error("生徒の通信スクーリング実績の取得でエラー" ,e);
            throw e;
        }
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map element = (Map) it.next();
            final String key = (String) element.get("key");
            final MySubClass subClass = (MySubClass) student._subClasses.get(key);
            if (null == subClass) {
                continue;
            }

            subClass._totalGetValue = (Integer) element.get("total_get_value");
        }
    }

    private String sqlSchoolingDat() {
        final String sql;
            sql = "SELECT"
                + "  classcd || curriculum_cd || subclasscd AS key,"
                + "  SUM(get_value) AS total_get_value"
                + " FROM"
                + "  rec_schooling_dat"
                + " WHERE"
                + "  year='" + _param._year + "' AND"
                + "  schregno=? AND"
                + "  schooling_type='" + SCHOOLING_TYPE + "' AND"
                + "  get_value IS NOT NULL"
                + " GROUP BY classcd, curriculum_cd, subclasscd"
                ;
        return sql;
    }

    private static class Student {
        /** 03=個人生 */
        private static final String STUDENT_DIV = "03";

        private final String _schregno;
        private final String _name;
        /** 年次 */
        private final String _annual;

        /** 履修科目。<classcd+curriculum_cd+subclasscd,MySubClass> */
        private final Map _subClasses = new HashMap();

        public Student(
                final String schregno,
                final String name,
                final String annual
        ) {
            _schregno = schregno;
            _name = name;
            _annual = annual;
        }

        public String toString() {
            return _schregno + "/" + _name;
        }

        static Map loadStudent(final Database db, final Param param) throws SQLException {
            final Map rtn = new HashMap();
            ResultSet rs = null;
            db.query(getSql(param));
            rs = db.getResultSet();
            while (rs.next()) {
                final String schregno = rs.getString("schregno");
                final String name = rs.getString("name");
                final String annual = rs.getString("annual");

                final Student student = new Student(schregno, name, annual);
                rtn.put(schregno, student);
            }
            return rtn;
        }

        static String getSql(final Param param) {
            final String sql;
            sql = "SELECT"
                + "  t1.schregno,"
                + "  t1.name_show as name,"
                + "  t1.annual"
                + " FROM"
                + "  v_schreg_info t1 INNER JOIN schreg_regd_dat t2"
                + " ON t1.schregno=t2.schregno AND t1.year=t2.year AND t1.semester=t2.semester"
                + " WHERE"
                + "  t2.student_div='" + STUDENT_DIV + "' AND"
                + "  t1.year='" + param._year + "' AND"
                + "  t1.semester='" + param._semester + "'"
                ;
            return sql;
        }
    }

    private class MySubClass {
        private final SubClass _subClass;

        /** 表示順。(科目マスタ.通知表用表示順) */
        private final String _showOrder;

        /** 年間レポート回数。 */
        private final Integer _reportCount;

        /** 年間スクーリング回数。 */
        private final Integer _schoolingCount;

        /**
         * 履修単位。
         * @deprecated sch_result_dat.comp_credit に使っていたが、仕様変更の為
         */
        private Integer _compCredit;

        /** 集中スクーリングに出席しているか? */
        private boolean _hasSchooling;

        /** 単位数. */
        private Integer _credits;

        /**
         * 割.
         * @deprecated sch_result_dat.vqsc_norma の算出に使っていたが、仕様変更の為
         */
        private Integer _rate;

        /** 評定. */
        private Integer _gradValue;

        /** 修得単位. */
        private Integer _getCredit;

        /** レポート. */
        private final Report[] _reports = new Report[MAX_REPORT_NUM];

        /** 修得時間分の通算。 */
        private Integer _totalGetValue;

        public MySubClass(
                final String clazzCd,
                final String curriculumCd,
                final String code,
                final String name,
                final String abbv,
                final String showOrder,
                final Integer reportSeq,
                final Integer schoolingSeq,
                final Integer credits
        ) {
            _subClass = new SubClass(clazzCd, curriculumCd, code, name, abbv);
            _showOrder = showOrder;
            _reportCount = reportSeq;
            _schoolingCount = schoolingSeq;
            _credits = credits;
        }

        public MySubClass(final MySubClass org) {
            _subClass = org._subClass;
            _showOrder = org._showOrder;
            _reportCount = org._reportCount;
            _schoolingCount = org._schoolingCount;
            _credits = org._credits;
        }

        /**
         * 教科を得る。
         * @return 教科
         */
        public Clazz getClazz() {
            final Clazz rtn = (Clazz) _classMst.get(_subClass.getClassCd());
            return rtn;
        }

        public String toString() {
            return _subClass.toString();
        }
    }

    private static class Report {
        /** 開催回。 */
        private final int _seq;
        /** 第1提出得点。 */
        private final Integer _score1;
        /** 第2提出得点。 */
        private final Integer _score2;

        Report(final int seq, final Integer score1, final Integer score2) {
            _seq = seq;
            _score1 = score1;
            _score2 = score2;
        }

        /**
         * 得点を得る。<br>
         * 再提出で30点以上は30点。
         * 両方とも30点未満の場合は大きい方。
         * @return 得点
         */
        public Integer getScore() {
            if (null == _score1 && null == _score2) {
                return new Integer(-1);
            }

            final int score1 = _score1.intValue();
            if (null != _score2) {
                final int score2 = _score2.intValue();
                if (score2 >= 30) {
                    return new Integer(30);
                } else {
                    return (score1 > score2) ? _score1 : _score2;
                }
            }
            return _score1;
        }
    }

    /**
     * VQS側の生徒成績情報テーブルの1レコードぶん。
     */
    private class SchResultDat {
        private final String _schregNo;
        private String _seqNo;
        private final String _classCd;
        private final String _subclassCd;
        private final String _year;
        private String _grade;
        /** 履修単位数. */
        private Integer _compCredit;
        /** 出席しているか? 1=出席済み, 0=未出席. */
        private String _schooling;

        /** 双方向授業規定回数. */
        private final Integer _vqscNorma = new Integer(1);

        /** 双方向授業受講回数. */
        private Integer _vqscAttend;

        /** 双方向授業受講残回数. */
        private Integer _vqscRemain;

        /*
         * レポート関連
         */
        /** 基準回数. */
        private Integer _standardNum;

        /** 未提出残枚数. */
        private Integer _nonPresentReports;

        /** レポート成績. */
        private Integer[] _report = new Integer[MAX_REPORT_NUM];

        /** 評定. */
        private Integer _gradValue;
        /** 修得単位数. */
        private Integer _getCredit;
        /** 作成日. */
        private String _createDate;

        /**
         * コンストラクタ。<br>
         * プライマリキーを引数とする。
         * @param schregNo 学籍番号
         * @param classCd 教科コード
         * @param subclassCd 科目コード
         * @param year 年度
         */
        public SchResultDat(final String schregNo, final String classCd, final String subclassCd, final String year) {
            _schregNo = schregNo;
            _classCd = classCd;
            _subclassCd = subclassCd;
            _year = year;
        }

        public SchResultDat(final Student student, final MySubClass mySubclass, final String year) {
            this(student._schregno, mySubclass._subClass.getClassCd(), mySubclass._subClass.getCode(), year);

            final Integer dummy = new Integer(-1);

            _seqNo = mySubclass._showOrder;
            _grade = student._annual;
            _compCredit = (null == mySubclass._credits) ? dummy : mySubclass._credits;
            _schooling = mySubclass._hasSchooling ? "1" : "0";

            if (null == mySubclass._totalGetValue) {
                _vqscAttend = dummy;
            } else {
                final int vqscAttend = mySubclass._totalGetValue.intValue() / WithusUtils.PERIOD_MINUTE;
                _vqscAttend = new Integer(vqscAttend);
            }

            if (null != _vqscNorma && null != _vqscAttend) {
                _vqscRemain = new Integer(_vqscNorma.intValue() - _vqscAttend.intValue());
            } else {
                _vqscRemain = dummy;
            }

            _standardNum = mySubclass._reportCount;

            int enableCount = 0;// xx点以上のレポートを提出した回数(有効なレポート数)
            for (int i = 0; i < mySubclass._reports.length; i++) {
                if (null != mySubclass._reports[i]) {
                    // レポートデータがある場合
                    _report[i] = mySubclass._reports[i].getScore();
                    if (_report[i].intValue() > 30) {
                        enableCount++;
                    }
                } else {
                    // レポートデータが無い場合
                    _report[i] = dummy;
                }
            }
            _nonPresentReports = (null == mySubclass._reportCount) ? null : new Integer(mySubclass._reportCount.intValue() - enableCount);

            final Integer gradValue = mySubclass._gradValue;
            _gradValue = (null == gradValue) ? dummy : gradValue;

            final Integer getCredit = mySubclass._getCredit;
            _getCredit = (null == getCredit) ? dummy : getCredit;

            _createDate = _yesterday;
        }

        /**
         * プライマリキーの文字列.
         * @return プライマリキーの文字列
         */
        public String getKeyString() {
            return "schregno=" + _schregNo + ", classcd=" + _classCd + ", subclasscd=" + _subclassCd + ", year=" + _year;
        }

        public Object[] toUpdateArray() {
            final Object[] rtn = {
                    _seqNo,
                    _grade,
                    _compCredit,
                    _schooling,
                    _vqscNorma,
                    _vqscAttend,
                    _vqscRemain,
                    _standardNum,
                    _nonPresentReports,
                    _report[0],
                    _report[1],
                    _report[2],
                    _report[3],
                    _report[4],
                    _report[5],
                    _report[6],
                    _report[7],
                    _report[8],
                    _report[9],
                    _report[10],
                    _report[11],
                    _report[12],
                    _report[13],
                    _report[14],
                    _report[15],
                    _report[16],
                    _report[17],
                    _gradValue,
                    _getCredit,
                    _createDate,
                    _schregNo,
                    _classCd,
                    _subclassCd,
            };
            return rtn;
        }

        public Object[] toInsertArray() {
            final Object[] rtn = {
                    _schregNo,
                    _seqNo,
                    _classCd,
                    _subclassCd,
                    _year,
                    _grade,
                    _compCredit,
                    _schooling,
                    _vqscNorma,
                    _vqscAttend,
                    _vqscRemain,
                    _standardNum,
                    _nonPresentReports,
                    _report[0],
                    _report[1],
                    _report[2],
                    _report[3],
                    _report[4],
                    _report[5],
                    _report[6],
                    _report[7],
                    _report[8],
                    _report[9],
                    _report[10],
                    _report[11],
                    _report[12],
                    _report[13],
                    _report[14],
                    _report[15],
                    _report[16],
                    _report[17],
                    _gradValue,
                    _getCredit,
                    _createDate,
            };
            return rtn;
        }
    }
} // MkSchResultDat

// eof
