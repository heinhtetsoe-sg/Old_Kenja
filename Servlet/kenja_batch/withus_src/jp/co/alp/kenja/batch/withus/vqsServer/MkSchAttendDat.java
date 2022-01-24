// kanji=漢字
/*
 * $Id: MkSchAttendDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/05/02 11:39:36 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.vqsServer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import nao_package.db.Database;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;

import jp.co.alp.kenja.batch.withus.Curriculum;
import jp.co.alp.kenja.batch.withus.WithusUtils;

/**
 * 生徒出席情報テーブル。
 * @author takaesu
 * @version $Id: MkSchAttendDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkSchAttendDat extends Mk {
    /** DBレコードをInsertする際の識別文字列 */
    public static final String VqsServerString = "VqsSvr";

    public final String _schoolingType = "M3";  // TODO: VQSのスクーリング種別を得る手段は?

    /*
     * hoge=# \d sch_attend_dat
     *             Table "public.sch_attend_dat"
     *    Column   |            Type             | Modifiers
     * ------------+-----------------------------+-----------
     *  schregno   | character varying(8)        | not null
     *  classcd    | character varying(2)        | not null
     *  subclasscd | character varying(6)        | not null
     *  attenddate | timestamp without time zone | not null
     *  attendance | integer                     | not null
     *  staffcd    | character varying(8)        | not null
     *  updated    | timestamp without time zone | not null
     * Indexes:
     *     "pk_sch_attend_dat" primary key, btree (schregno, classcd, subclasscd, attenddate)
     */

    /*pkg*/static final Log log = LogFactory.getLog(MkSchAttendDat.class);

    /** 更新日. */
    final String _date = new SimpleDateFormat("yyyy-MM-dd").format(_param.getUpdate());

    /** 読替先(体育)を持っている生徒. <schregno, Student> */
    public final Map _physicalEducationStudents = new HashMap();

    final String _curriculumCd = Curriculum.getCurriculumCd(_param.getYear());

    public MkSchAttendDat(final Param param, final Database knj, final Database vqs, final String title) throws SQLException {
        super(param, knj, vqs, title);

        final List list = loadVqs(vqs);
        log.warn("VQS側:データ数=" + list.size());
        log.warn("読替先(体育)を持っている生徒=" + _physicalEducationStudents.keySet());

        final List converted = convert(list);

        saveKnj(converted, knj);
        updateKnj(converted, knj);
        deleteVqs(vqs);
    }

    /**
     * 生徒の体育の読替元をセットする。
     */
    private void set読替元() throws SQLException {
        if (_physicalEducationStudents.isEmpty()) {
            return;
        }

        final String[] schregnos = new String[_physicalEducationStudents.size()];
        _physicalEducationStudents.keySet().toArray(schregnos);

        final String sql;
        sql = "SELECT"
            + "  schregno,"
            + "  subclasscd"
            + " FROM"
            + "  comp_regist_dat"
            + " WHERE"
            + "  year='" + _param._year + "' AND"
            + "  classcd='" + WithusUtils.PHYSICAL_EDUCATION_CLASS_CD + "' AND"
            + "  curriculum_cd='" + WithusUtils.PHYSICAL_EDUCATION_CURRICULUM + "' AND"
            + "  subclasscd like '0602%' AND"   // 体育1～7の科目コードの共通部分
            + "  schregno IN " + SQLUtils.whereIn(true, schregnos)
            + " ORDER BY schregno, subclasscd"
            ;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = _knj.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("schregno");
                final String subclasscd = rs.getString("subclasscd");

                final Student student = (Student) _physicalEducationStudents.get(schregno);
                student._subclasses.add(subclasscd);
            }
        } catch (final SQLException e) {
            log.fatal("生徒の体育の読替元取得でエラー", e);
            throw e;
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private List loadVqs(final Database vqs) throws SQLException {
        final List rtn = new ArrayList();

        final String sql = "SELECT * FROM sch_attend_dat";
        log.debug("sql=" + sql);

        final List result;
        try {
            result = (List) _runner.query(vqs.conn, sql, _handler);
        } catch (final SQLException e) {
            log.error("生徒出席情報テーブルのVQS取込みでエラー", e);
            throw e;
        }

        final Set schregnos = new HashSet();
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();

            final String schregno = (String) map.get("schregno");
            final String classCd = (String) map.get("classcd");
            final String subclassCd = (String) map.get("subclasscd");
            final Timestamp attendDate = (Timestamp) map.get("attenddate");
            final Integer attendance = (Integer) map.get("attendance");
            final String stasffCd = (String) map.get("staffcd");

            final Data data = new Data(schregno, classCd, subclassCd, attendDate, attendance, stasffCd);
            if (!"1".equals(attendance.toString())) {
                log.warn("回数が 1 以外のデータ。無視します: " + data);
                continue;
            }

            // 読替元を持っている生徒を記憶しておく
            if (WithusUtils.PHYSICAL_EDUCATION_SUBCLASS_CD.equals(subclassCd)) {
                schregnos.add(schregno);
            }

            rtn.add(data);
            log.debug("VQSデータ=" + data);
        }

        for (final Iterator it = schregnos.iterator(); it.hasNext();) {
            final String schregno = (String) it.next();
            _physicalEducationStudents.put(schregno, new Student(schregno));
        }

        return rtn;
    }

    /**
     * 読替先のデータは読替元に変換する
     * @param list 読替先が含まれているハズ
     * @return 変換後の結果。(先は含まれていない)
     * @throws SQLException
     */
    private List convert(final List list) throws SQLException {
        final List rtn = new ArrayList();
        set読替元();

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Data data = (Data) it.next();
            rtn.add(data);

            if (WithusUtils.PHYSICAL_EDUCATION_SUBCLASS_CD.equals(data._subclassCd)) {
                // 元科目を得る
                final Student student = (Student) _physicalEducationStudents.get(data._schregno);
                final String subclasscd = student.getTargetSubclass();

                // 先⇒元 変換
                final Data motoData = new Data(
                        data._schregno,
                        data._classCd,
                        subclasscd,
                        data._attendDate,
                        data._attendance,
                        data._staffCd
                );
                rtn.add(motoData);
                log.warn("先(" + data._subclassCd + ")⇒元(" + subclasscd + ")変換! VQSデータ=" + data);
            }
        }
        return rtn;
    }

    private void saveKnj(final List list, final Database knj) throws SQLException {
        int totalCnt = 0;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Data data = (Data) it.next();

            int seq = 1;
            try {
                final String seqSql = getSeqSql(data, _curriculumCd);
                final Integer maxseq = (Integer) _runner.query(knj.conn, seqSql, new ScalarHandler());
                if (null != maxseq) {
                    seq = maxseq.intValue() + 1;
                }
            } catch (final SQLException e) {
                log.error("最大値の取得でエラー", e);
                throw e;
            }

            final String insertSql = "INSERT INTO rec_schooling_dat VALUES (?,?,?,?,?,?,?,?,?,?,?,current timestamp)";
            final Object[] dataArray = data.getArray(seq, _curriculumCd);
            try {
                final int insCnt = _runner.update(knj.conn, insertSql, dataArray);
                log.debug(insertSql + ", " + Arrays.asList(dataArray));
                if (1 != insCnt) {
                    throw new IllegalStateException("INSERT件数が1件以外!:" + insCnt);
                }
                totalCnt += insCnt;
            } catch (final SQLException e) {
                log.error("賢者へのINSERTでエラー", e);
                throw e;
            }
        }
        knj.commit();
        log.warn("賢者挿入件数=" + totalCnt);
    }

    private String getSeqSql(final Data data, final String curriculumCd) {
        final String sql;
        sql = "SELECT"
            + "  max(seq) as maxseq"
            + " FROM"
            + "  rec_schooling_dat"
            + " WHERE"
            + "  year='" + _param.getYear() + "' AND"
            + "  classcd='" + data._classCd + "' AND"
            + "  curriculum_cd='" + curriculumCd + "' AND"
            + "  subclasscd='" + data._subclassCd + "' AND"
            + "  schregno='" + data._schregno + "' AND"
            + "  schooling_type='" + _schoolingType + "'"
            ;
        return sql;
    }

    private void updateKnj(final List datas, final Database knj) throws SQLException {
        // TAKAESU: 同一の生徒・科目はまとめたい
        int totalCnt = 0;
        for (final Iterator it = datas.iterator(); it.hasNext();) {
            final Data data = (Data) it.next();

            final String updateSql = updateSql(data);
            try {
                final int updateCnt = _runner.update(knj.conn, updateSql);
                log.debug(updateSql);
                if (1 < updateCnt) {
                    knj.conn.rollback();
                    throw new IllegalStateException("UPDATE件数が1件以上!rollbackした!:" + updateCnt);
                }
                totalCnt += updateCnt;
            } catch (final SQLException e) {
                log.error("賢者へのUPDATEでエラー", e);
                throw e;
            }
        }
        knj.commit();
        log.warn("スクーリング完了となった件数=" + totalCnt);
    }

    private String updateSql(final Data data) {
        final String sql;
        sql = "UPDATE"
            + "   rec_schooling_rate_dat"
            + " SET"
            + "   commited_s='" + _date + "',"
            + "   commited_e='" + _date + "',"
            + "   registercd='" + VqsServerString + "',"
            + "   updated=current timestamp"
            + " WHERE"
            + "   year='" + _param._year + "' AND"
            + "   classcd='" + data._classCd + "' AND"
            + "   curriculum_cd='" + _curriculumCd + "' AND"
            + "   subclasscd='" + data._subclassCd + "' AND"
            + "   schregno='" + data._schregno + "' AND"
            + "   schooling_type='" + _schoolingType + "'"
            ;
        return sql;
    }

    private void deleteVqs(final Database vqs) throws SQLException {
        final String sql = "DELETE FROM sch_attend_dat";
        try {
            final int cnt = _runner.update(vqs.conn, sql);
            log.warn("VQS削除件数=" + cnt);
        } catch (final SQLException e) {
            log.error("sch_attend_dat の削除でエラー", e);
            throw e;
        }
        vqs.commit();
    }

    private class Data {
        private final String _schregno;
        private final String _classCd;
        private final String _subclassCd;
        private final Timestamp _attendDate;
        /** 出席時間. 単位は回数. */
        private final Integer _attendance;
        private final String _staffCd;

        public Data(
                final String schregno,
                final String classCd,
                final String subclassCd,
                final Timestamp attendDate,
                final Integer attendance,
                final String staffCd
        ) {
            _schregno = schregno;
            _classCd = classCd;
            _subclassCd = subclassCd;
            _attendDate = attendDate;
            _attendance = attendance;
            _staffCd = staffCd;
        }

        public Object[] getArray(final int seq, final String curriculumCd) {
            int minute = 0;
            if (null != _attendance) {
                minute = _attendance.intValue() * WithusUtils.PERIOD_MINUTE;
            }

            final Object[] rtn = {
                    _param.getYear(),
                    _classCd,
                    curriculumCd,
                    _subclassCd,
                    _schregno,
                    _schoolingType,
                    new Integer(seq),
                    _attendDate,
                    new Integer(minute),
                    null,   // Null=バッチ入力。 NotNull=手入力。
                    VqsServerString,
            };
            return rtn;
        }

        public String toString() {
            return _schregno + "/" + _classCd + "/" + _subclassCd + "/" + _attendDate + "/" + _attendance + "/" + _staffCd;
        }
    }

    private class Student {
        private final String _schregno;
        /** 履修している読替元の科目. */
        private final MyList _subclasses = new MyList();

        Student(final String schregno) {
            _schregno = schregno;
        }

        public String toString() {
            return _schregno + "/" + _subclasses;
        }

        /**
         * セットすべき読替元の科目コードを得る。
         * 通信スクーリング実績の最新の科目の「次」。
         * 通信スクーリング実績のレコードが無い場合は「一番若い科目コード」。
         * @return セットすべき読替元の科目コード
         * @throws SQLException
         */
        private String getTargetSubclass() throws SQLException {
            // 初回はDBから算出
            if (!_subclasses.hasNext()) {
                final String baseCd = get起点科目fromDb();
                if (null == baseCd) {
                    // DBに無いので一番若いコード
                    _subclasses.setIndex(0);
                    return (String) _subclasses.get(0);
                } else {
                    // 起点の次
                    _subclasses.setIndex(baseCd);
                    return _subclasses.nextGet();
                }
            }

            // 2回目以降は、(メモリ上で)順繰り
            return _subclasses.nextGet();
        }

        private String get起点科目fromDb() throws SQLException {
            final List wrk = new ArrayList();
            final String sql = getSql各々の最新科目();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = _knj.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("subclasscd");
                    wrk.add(subclasscd);
                    break;  // 起点のみ
                }
            } catch (final SQLException e) {
                log.fatal("生徒の体育の読替元取得でエラー", e);
                throw e;
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.info("生徒=" + _schregno + ", 履修読替元科目=" + _subclasses + ", 登録済み元科目=" + wrk);
            if (wrk.size() == 0) {
                return null;
            }
            return (String) wrk.get(0);
        }

        private String getSql各々の最新科目() {
            final String[] subclasses = new String[_subclasses.size()];
            _subclasses.toArray(subclasses);

            final String sql;
            sql = "WITH a_t AS ("
                + "    SELECT"
                + "        classcd,"
                + "        curriculum_cd,"
                + "        subclasscd,"
                + "        schregno,"
                + "        schooling_type,"
                + "        MAX(seq) AS seq"
                + "    FROM"
                + "        rec_schooling_dat"
                + "    WHERE"
                + "        year='" + _param._year + "' AND"
                + "        schooling_type='" + _schoolingType + "'"
                + "    GROUP BY"
                + "        classcd,"
                + "        curriculum_cd,"
                + "        subclasscd,"
                + "        schregno,"
                + "        schooling_type"
                + " )"
                + " SELECT"
                + "    t1.subclasscd,"
                + "    t1.updated"
                + " FROM"
                + "    rec_schooling_dat t1 INNER JOIN  a_t t2 ON"
                + "    t1.classcd=t2.classcd AND"
                + "    t1.curriculum_cd=t2.curriculum_cd AND"
                + "    t1.subclasscd=t2.subclasscd AND"
                + "    t1.schregno=t2.schregno AND"
                + "    t1.schooling_type=t2.schooling_type AND"
                + "    t1.seq=t2.seq"
                + " WHERE"
                + "    t1.year = '" + _param._year + "' AND"
                + "    t1.schregno = '" + _schregno + "' AND"
                + "    t1.schooling_type = '" + _schoolingType + "' AND"
                + "    t1.classcd = '" + WithusUtils.PHYSICAL_EDUCATION_CLASS_CD + "' AND"
                + "    t1.curriculum_cd='" + WithusUtils.PHYSICAL_EDUCATION_CURRICULUM + "' AND"
                + "    t1.subclasscd IN" + SQLUtils.whereIn(true, subclasses)
                + " ORDER BY updated DESC"
                ;
            log.info("各々の最新科目SQL=" + sql);
            return sql;
        }

        private class MyList extends ArrayList {
            private int _index = -1;

            public MyList() {
                super();
            }

            boolean hasNext() {
                return (-1 != _index);
            }

            String nextGet() {
                _index++;
                if (_index >= size()) {
                    _index = 0;
                }
                final int next = _index;
                return (String) get(next);
            }

            private void setIndex(final String subclassCd) {
                _index = indexOf(subclassCd);
            }
            private void setIndex(final int index) {
                _index = index;
            }
        }
    }
} // MkSchAttendDat

// eof
