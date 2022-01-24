// kanji=漢字
/*
 * $Id: MkRishuKadai.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/03/24 16:23:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.markServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;

import jp.co.alp.kenja.batch.withus.Curriculum;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * 履修課題マスタデータ。
 * @author takaesu
 * @version $Id: MkRishuKadai.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkRishuKadai extends Mk {
    private final static String _FILE = "MK_RISHU_KADAI.csv";

    private final String _curriculumCode;

    public MkRishuKadai(final DB2UDB db, final Param param, final String title) throws SQLException {
        super(db, param, title);

        // DBから取り込む
        _curriculumCode = loadCurriculumCd();
        final List detailsMsts = loadSubclassDetailsMst();

        final List list = new ArrayList();

        // ヘッダを設定
        setHead(list);
        
        for (final Iterator it = detailsMsts.iterator(); it.hasNext();) {
            final SubclassDetailsMst detail = (SubclassDetailsMst) it.next();

            createReportData(detail, list, param);
            if (detail._doTest) {
                createTestData(detail, list, param);
            }
        }

        // CSVファイルに書く
        toCsv("履修課題", _FILE, list);
    }

    void setHead(List list) {

        final String[] header = {
                "学校区分",
                "課程コード",
                "教育課程適用年度コード",
                "年度コード",
                "教科コード",
                "科目コード",
                "履修期間コード",
                "履修課題種別コード",
                "課題提出回数",
                "更新日",
        };
        list.add(header);
    }

    private void createTestData(final SubclassDetailsMst detail, final List list, Param param) {
        for (int i = 1; i <= 2; i++) {
            final String[] fields = {
                    _param.getSchoolDiv(),
                    _curriculumCode,
                    Curriculum.getCurriculumYear(detail._curriculumCd),
                    _param.getYear(),
                    detail._classcd,
                    detail._subclasscd,
                    "1",    // 履修期間コード。1固定
                    "2",    // 1=レポート, 2=テスト
                    String.valueOf(i),
                    cutDateDelimit(param.getUpdate()),
            };
            list.add(fields);
        }
    }

    private void createReportData(final SubclassDetailsMst detail, final List list, Param param) {
        for (int i = 1; i <= detail._reportSeq; i++) {
            final String[] fields = {
                    _param.getSchoolDiv(),
                    _curriculumCode,
                    Curriculum.getCurriculumYear(detail._curriculumCd), 
                    _param.getYear(),
                    detail._classcd,
                    detail._subclasscd,
                    "1",    // 履修期間コード。1固定
                    "1",    // 1=レポート, 2=テスト
                    String.valueOf(i),
                    cutDateDelimit(param.getUpdate()),
            };
            list.add(fields);
        }
    }

    private String loadCurriculumCd() throws SQLException {
        ResultSet rs = null;
        final String sql = "SELECT coursecd FROM course_ydat WHERE year='" + _param.getYear() + "' ORDER BY coursecd";
        try {
            _db.query(sql);
            rs = _db.getResultSet();
            while (rs.next()) {
                final String curriculumCd = rs.getString("coursecd");
                return curriculumCd;    // 最初のレコード
            }
        } catch (final SQLException e) {
            log.fatal("課程コードの情報取得でエラー");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }
        log.warn("課程マスタ年度データの取得エラー:" + sql);
        return null;
    }

    private List loadSubclassDetailsMst() throws SQLException {
        final List rtn = new ArrayList();
        ResultSet rs = null;
        try {
            _db.query(getSql());
            rs = _db.getResultSet();
            while(rs.next()) {
                final String curriculumCd = rs.getString("curriculum_cd");
                final String classcd = rs.getString("classcd");
                final String subclasscd = rs.getString("subclasscd");
                final int reportSeq = rs.getInt("report_seq");
                final String testFlg = rs.getString("test_flg");

                final SubclassDetailsMst detailsMst = new SubclassDetailsMst(
                        curriculumCd,
                        classcd,
                        subclasscd,
                        reportSeq,
                        testFlg
                );
                rtn.add(detailsMst);
            }
        } catch (final SQLException e) {
            log.fatal("生徒の情報取得でエラー");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }
        return rtn;
    }

    private String getSql() {
        final String sql;
        sql = "SELECT"
            + "  curriculum_cd,"
            + "  classcd,"
            + "  SUBSTR(subclasscd,3,4) AS subclasscd,"
            + "  report_seq,"
            + "  test_flg"
            + " FROM"
            + "  subclass_details_mst"
            + " WHERE"
            + "  year='" + _param.getYear() + "' AND"
            + "  report_seq IS NOT NULL AND"
            + "  inout_div='0'" // 自校内外区分: 0=自校内, 1=自校外
            ;
        return sql;
        /*

        SELECT
            t1.*
        FROM
            v_subclass_mst t1 inner join subclass_details_mst t2 on
                t1.year=t2.year and
                t1.classcd=t2.classcd and
                t1.curriculum_cd=t2.curriculum_cd and
                t1.subclasscd=t2.subclasscd
        WHERE
            t1.inout_div='0'

          */
    }

    private class SubclassDetailsMst {
        private final String _curriculumCd;
        private final String _classcd;
        private final String _subclasscd;
        private final int _reportSeq;
        /** テスト実施フラグ。true=実施 */
        private final boolean _doTest;

        public SubclassDetailsMst(
                final String curriculumCd,
                final String classcd,
                final String subclasscd,
                final int reportSeq,
                final String testFlg
        ) {
            _curriculumCd = curriculumCd;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _reportSeq = reportSeq;
            _doTest = (null != testFlg) ? true : false;
        }
    }
} // MkRishuKadai
// eof
