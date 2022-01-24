// kanji=漢字
/*
 * $Id: MkSchRegistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/04/30 14:55:56 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.vqsServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.Database;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.withus.WithusUtils;

/**
 * 生徒履修情報テーブル。
 * @author takaesu
 * @version $Id: MkSchRegistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkSchRegistDat extends Mk {
    /*pkg*/static final Log log = LogFactory.getLog(MkSchRegistDat.class);

    public MkSchRegistDat(final Param param, final Database knj, final Database vqs, final String title) throws SQLException {
        super(param, knj, vqs, title);

        final List list = loadKnj(knj);
        log.debug("賢者:データ数=" + list.size());

        saveVqs(list, vqs);
    }

    private void saveVqs(final List list, final Database vqs) throws SQLException {
        int count = 0;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Data data = (Data) it.next();

            final String sql = "UPDATE sch_regist_dat SET updated=current_timestamp"
                + " WHERE schregno=?"
                + " AND classcd=?"
                + " AND subclasscd=?"
                ;
            try {
                int cnt = _runner.update(vqs.conn, sql, data.toArray());
                if (0 == cnt) {
                    final String insertSql = "INSERT INTO sch_regist_dat VALUES (?, ?, ?, current_timestamp)";
                    cnt = _runner.update(vqs.conn, insertSql, data.toArray());
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

    private List loadKnj(final Database knj) throws SQLException {
        final List rtn = new ArrayList();

        final String sql;
        sql = "SELECT"
            + "  t1.schregno,"
            + "  t1.classcd,"
            + "  t1.curriculum_cd,"
            + "  t1.subclasscd"
            + " FROM"
            + "  comp_regist_dat t1 INNER JOIN schreg_regd_dat t2 ON t1.year=t2.year AND t1.schregno=t2.schregno"
            + " WHERE"
            + "  t1.year='" + _param._year + "' AND"
            + "  t2.semester='" + _param._semester + "' AND"
            + "  (t2.student_div='03' OR t2.seat_col='1')"    // student_div="03:のみ生"
            ;
        log.debug("sql=" + sql);

        final MultiMap studentPhysicalEducations = new MultiHashMap();  // 生徒の体育1～体育7の情報

        ResultSet rs = null;
        try {
            knj.query(sql);
            rs = knj.getResultSet();
            while (rs.next()) {
                final String schregno = rs.getString("schregno");
                final String classCd = rs.getString("classcd");
                final String curriculumCd = rs.getString("curriculum_cd");
                final String subclassCd = rs.getString("subclasscd");
                final Data data = new Data(schregno, classCd, subclassCd);
                // 体育1～7は含めない
                if (WithusUtils.isPhysicalEdu(classCd, curriculumCd, data._subclassCd)) {
                    studentPhysicalEducations.put(data._schregno, data);
                } else {
                    rtn.add(data);
                }
            }
        } catch (final SQLException e) {
            log.error("生徒履修情報テーブルでエラー", e);
            throw e;
        } finally {
            knj.commit();
            DbUtils.closeQuietly(null, null, rs);
        }

        if (studentPhysicalEducations.size() != 0) {
            for (final Iterator it = studentPhysicalEducations.values().iterator(); it.hasNext();) {
                final Data data = (Data) it.next();
                log.warn("体育1～7を持っている生徒の情報: " + data);
            }
        }

        // 「体育」をセットする
        for (final Iterator it = studentPhysicalEducations.keySet().iterator(); it.hasNext();) {
            final String schregno = (String) it.next();
            final List list = (List) studentPhysicalEducations.get(schregno);
            final Data data = (Data) list.get(0);
            final Data hoge = new Data(data._schregno, data._classCd, WithusUtils.PHYSICAL_EDUCATION_SUBCLASS_CD);
            rtn.add(hoge);
        }

        return rtn;
    }

    private class Data {
        private final String _schregno;
        private final String _classCd;
        private final String _subclassCd;
        Data(final String schregno, final String classCd, final String subclassCd) {
            _schregno = schregno;
            _classCd = classCd;
            _subclassCd = subclassCd;
        }

        public Object[] toArray() {
            final String[] rtn = {
                    _schregno,
                    _classCd,
                    _subclassCd,
            };
            return rtn;
        }

        public String toString() {
            return _schregno + "/" + _classCd + "/" + _subclassCd;
        }
    }
} // MkSchRegistDat

// eof
