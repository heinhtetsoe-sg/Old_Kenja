// kanji=����
/*
 * $Id: MkSchResultDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/05/02 11:36:05 - JST
 * �쐬��: takaesu
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
 * ���k���я��e�[�u���B
 * @author takaesu
 * @version $Id: MkSchResultDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkSchResultDat extends Mk {
    /* �f�o�b�O(�m�F���)�ɖ𗧂�SQL
     * �����|�[�g�_���֘A�ȊO
     * SELECT schregno, seqno, subclasscd, grade, comp_credit, schooling, vqsc_norma, vqsc_attend, vqsc_remain, grad_value, get_credit, create_date FROM sch_result_dat;
     *
     * �����|�[�g�_���֘A
     * SELECT schregno, seqno, subclasscd, grade, standard_num, non_present_reports, report01, report02, report03 FROM sch_result_dat;
     */

    /*pkg*/static final Log log = LogFactory.getLog(MkSchResultDat.class);

    /** ���|�[�g�ő�񐔁B */
    private static final int MAX_REPORT_NUM = 18;

    /** VQS�̃X�N�[�����O���. */
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
        log.debug("����:�f�[�^��=" + list.size());

        saveVqs(list, vqs);
    }

    public List loadKnj(final Database knj) throws SQLException {
        final List rtn = new ArrayList();

        final Map students = Student.loadStudent(knj, _param);
        log.info("���k��=" + students.size());

        // ���k�̗��C�Ȗڂ𓾂�
        for (final Iterator it = students.values().iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            setSubClassesAndSchooling(knj, student);
            setReportData(knj, student);
            setCreditAdmits(knj, student);
            setSchoolingDat(knj, student);

            // rtn �Ƀf�[�^���Z�b�g
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
                log.error("VQS�ɍX�V�ŃG���[");
                throw e;
            }
            vqs.commit();
        }
        log.debug("VQS:�f�[�^��=" + count);
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
        log.debug("���ȃ}�X�^����=" + _classMst.size());

        _subclassMst = loadSubClass(knj, param._year);
        log.debug("�Ȗڃ}�X�^����=" + _subclassMst.size());
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
                final String msg = "����+����ے�CD+�Ȗ�=" + clazzCd + curriculumCd + code;
                log.warn(msg + ":���|�[�g�񐔂�VQS�ɋL�^�ł��鐔(" + MAX_REPORT_NUM + ")�𒴂��Ă�̂Ő؋l�߂��B:" + reportSeq);
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
            log.error("���k�̃��|�[�g���擾�ŃG���[" ,e);
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

            // �����Z�b�g
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
            log.error("���k�̗��C�Ȗڎ擾�ŃG���[" ,e);
            throw e;
        }
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map element = (Map) it.next();
            final String key = (String) element.get("key");
            final Integer compCredit = (Integer) element.get("comp_credit");
            final Date commitedDate = (Date) element.get("commited_e");
            final Integer rate = (Integer) element.get("rate");
            final MySubClass subClass = (MySubClass) _subclassMst.get(key);

            // �����Z�b�g
            final MySubClass studentSubClass = new MySubClass(subClass);    // ���k���ɉȖڂ̃C���X�^���X������
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
            log.error("�P�ʔF����̎擾�ŃG���[" ,e);
            throw e;
        }
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map element = (Map) it.next();
            final String key = (String) element.get("key");
            final MySubClass subClass = (MySubClass) student._subClasses.get(key);
            if (null == subClass) {
                continue;
            }
            // �����Z�b�g
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
            log.error("���k�̒ʐM�X�N�[�����O���т̎擾�ŃG���[" ,e);
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
        /** 03=�l�� */
        private static final String STUDENT_DIV = "03";

        private final String _schregno;
        private final String _name;
        /** �N�� */
        private final String _annual;

        /** ���C�ȖځB<classcd+curriculum_cd+subclasscd,MySubClass> */
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

        /** �\�����B(�Ȗڃ}�X�^.�ʒm�\�p�\����) */
        private final String _showOrder;

        /** �N�ԃ��|�[�g�񐔁B */
        private final Integer _reportCount;

        /** �N�ԃX�N�[�����O�񐔁B */
        private final Integer _schoolingCount;

        /**
         * ���C�P�ʁB
         * @deprecated sch_result_dat.comp_credit �Ɏg���Ă������A�d�l�ύX�̈�
         */
        private Integer _compCredit;

        /** �W���X�N�[�����O�ɏo�Ȃ��Ă��邩? */
        private boolean _hasSchooling;

        /** �P�ʐ�. */
        private Integer _credits;

        /**
         * ��.
         * @deprecated sch_result_dat.vqsc_norma �̎Z�o�Ɏg���Ă������A�d�l�ύX�̈�
         */
        private Integer _rate;

        /** �]��. */
        private Integer _gradValue;

        /** �C���P��. */
        private Integer _getCredit;

        /** ���|�[�g. */
        private final Report[] _reports = new Report[MAX_REPORT_NUM];

        /** �C�����ԕ��̒ʎZ�B */
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
         * ���Ȃ𓾂�B
         * @return ����
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
        /** �J�É�B */
        private final int _seq;
        /** ��1��o���_�B */
        private final Integer _score1;
        /** ��2��o���_�B */
        private final Integer _score2;

        Report(final int seq, final Integer score1, final Integer score2) {
            _seq = seq;
            _score1 = score1;
            _score2 = score2;
        }

        /**
         * ���_�𓾂�B<br>
         * �Ē�o��30�_�ȏ��30�_�B
         * �����Ƃ�30�_�����̏ꍇ�͑傫�����B
         * @return ���_
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
     * VQS���̐��k���я��e�[�u����1���R�[�h�Ԃ�B
     */
    private class SchResultDat {
        private final String _schregNo;
        private String _seqNo;
        private final String _classCd;
        private final String _subclassCd;
        private final String _year;
        private String _grade;
        /** ���C�P�ʐ�. */
        private Integer _compCredit;
        /** �o�Ȃ��Ă��邩? 1=�o�ȍς�, 0=���o��. */
        private String _schooling;

        /** �o�������ƋK���. */
        private final Integer _vqscNorma = new Integer(1);

        /** �o�������Ǝ�u��. */
        private Integer _vqscAttend;

        /** �o�������Ǝ�u�c��. */
        private Integer _vqscRemain;

        /*
         * ���|�[�g�֘A
         */
        /** ���. */
        private Integer _standardNum;

        /** ����o�c����. */
        private Integer _nonPresentReports;

        /** ���|�[�g����. */
        private Integer[] _report = new Integer[MAX_REPORT_NUM];

        /** �]��. */
        private Integer _gradValue;
        /** �C���P�ʐ�. */
        private Integer _getCredit;
        /** �쐬��. */
        private String _createDate;

        /**
         * �R���X�g���N�^�B<br>
         * �v���C�}���L�[�������Ƃ���B
         * @param schregNo �w�Дԍ�
         * @param classCd ���ȃR�[�h
         * @param subclassCd �ȖڃR�[�h
         * @param year �N�x
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

            int enableCount = 0;// xx�_�ȏ�̃��|�[�g���o������(�L���ȃ��|�[�g��)
            for (int i = 0; i < mySubclass._reports.length; i++) {
                if (null != mySubclass._reports[i]) {
                    // ���|�[�g�f�[�^������ꍇ
                    _report[i] = mySubclass._reports[i].getScore();
                    if (_report[i].intValue() > 30) {
                        enableCount++;
                    }
                } else {
                    // ���|�[�g�f�[�^�������ꍇ
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
         * �v���C�}���L�[�̕�����.
         * @return �v���C�}���L�[�̕�����
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
