// kanji=漢字
/*
 * $Id: MkSeitoRishuKadaiJisseki.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/03/24 16:23:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.markServer;

import java.util.ArrayList;
import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;

import jp.co.alp.kenja.batch.withus.Curriculum;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * 生徒履修課題実績データ。
 * @author takaesu
 * @version $Id: MkSeitoRishuKadaiJisseki.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkSeitoRishuKadaiJisseki extends Mk {
    private final static String _FILE = "MK_SEITO_RISHU_KADAI_JISSEKI.csv";

    public MkSeitoRishuKadaiJisseki(final DB2UDB db, final Param param, final String title) throws SQLException {
        super(db, param, title);

        final List list = new ArrayList();
        
        // ヘッダを設定
        setHead(list);

        // DBから取り込む
        getScoreFromTest(param, list);
        getScoreFromReport(param, list);

        // CSVファイルに書く
        toCsv("生徒履修課題実績", _FILE, list);
    }

    void setHead(List list) {
        final String[] header = {
                "学校区分",
                "学籍番号",
                "課程コード",
                "教育課程適用年度コード",
                "年度コード",
                "教科コード",
                "科目コード",
                "履修期間コード",
                "履修課題種別コード",
                "実施番号",
                "課題レベル",
                "提出年月日",
                "得点",
                "初回得点",
                "追試得点",
                "提出回数",
                "職員コード",
                "更新日",
        };
        list.add(header);
    }
    
    private void getScoreFromReport(final Param param, final List list) throws SQLException {
        ResultSet rs = null;
        try {
            final String sql = getSqlReport();
            log.debug("sql=" + sql);
            _db.query(sql);
            rs = _db.getResultSet();
            while(rs.next()) {
                final String score1 = rs.getString("commited_score1");  // 初回得点
                final String score2 = rs.getString("commited_score2");  // 追試得点
                final String count = (null == score2) ? "01" : "02";    // 提出回数
                final String score = getScore(score1, score2);  // 得点
                
                final String[] fields = {
                        param.getSchoolDiv(),
                        rs.getString("schregno"),
                        "1",    // TODO: 学籍在籍データの課程コード（coursecd)をセット
                        Curriculum.getCurriculumYear(rs.getString("curriculum_cd")), 
                        _param.getYear(),
                        rs.getString("classcd"),
                        cutSubclassCd(rs.getString("subclasscd")),
                        "1",    // 履修期間コード: 1=通年
                        "1",    // 履修課題種別コード: 1=レポート, 2=テスト
                        rs.getString("report_seq"),
                        "0", // 課題レベル
                        cutDateDelimit2(rs.getString("commited_date")),   // 提出年月日
                        score,
                        score1,
                        score2,
                        count,
                        convStaffCd(rs.getString("registercd")),
                        cutDateDelimit(param.getUpdate()),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("生徒履修課題実績(レポート)の情報取得でエラー");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }
        log.info("生徒履修課題実績(テスト+レポート)のレコード数=" + list.size());
    }

    public static String cutDateDelimit2(final String dateStr) {
        if (null == dateStr) {
            return null;
        }
        final String rtn = dateStr.substring(0, 4) + dateStr.substring(5, 7) + dateStr.substring(8, 10);
        return rtn;
    }

    /**
     * 得点を得る。
     * @param score1 初回得点
     * @param score2 追試得点
     * @return 追試がある場合、得点は最高で30．それを下回る場合、どちらか大きい方。<br>追試なければ初回得点
     */
    public String getScore(final String score1, final String score2) {
        if (null == score2) {
            return score1;
        }

        final int value2 = Integer.valueOf(score2).intValue();
        if (value2 >= 30) {
            return "30";
        }

        final int value1 = Integer.valueOf(score1).intValue();
        
        return String.valueOf(Math.max(value1, value2));
    }

    private String getSqlReport() {
        final String sql;
        sql = "SELECT"
            + "  schregno,"
            + "  curriculum_cd,"
            + "  classcd,"
            + "  subclasscd,"
            + "  report_seq,"
            + "  CASE WHEN commited_date1 >= commited_date2 THEN commited_date1"
            + "       WHEN commited_date1 < commited_date2 THEN commited_date2"
            + "       ELSE commited_date1 END as commited_date,"
            + "  commited_score1,"
            + "  commited_score2,"
            + "  registercd"
            + " FROM"
            + "  rec_report_dat"
            + " WHERE"
            + "  commited_score1 is not null AND"
            + "  year='" + _param.getYear() + "'"
            ;
        return sql;
    }

    private void getScoreFromTest(final Param param, final List list) throws SQLException {
        ResultSet rs = null;
        try {
            _db.query(getSqlTest());
            rs = _db.getResultSet();
            while(rs.next()) {
                final String[] fields = {
                        param.getSchoolDiv(),
                        rs.getString("schregno"),
                        "1",    // TODO: 学籍在籍データの課程コード（coursecd)をセット
                        Curriculum.getCurriculumYear(rs.getString("curriculum_cd")), 
                        _param.getYear(),
                        rs.getString("classcd"),
                        cutSubclassCd(rs.getString("subclasscd")),
                        "1",    // 履修期間コード: 1=通年
                        "2",    // 履修課題種別コード: 1=レポート, 2=テスト
                        getTestCode(rs.getString("month")),
                        "0", // 課題レベル
                        cutDateDelimit2(rs.getString("updated")),   // 提出年月日
                        rs.getString("score"),  // 得点
                        rs.getString("score"),  // 初回得点
                        "", // 追試得点
                        "01", // 提出回数
                        convStaffCd(rs.getString("registercd")),
                        cutDateDelimit(param.getUpdate()),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("生徒履修課題実績(テスト)の情報取得でエラー");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }
        log.info("生徒履修課題実績(テスト)のレコード数=" + list.size());
    }

    private String getSqlTest() {
        final String sql;
        sql = "SELECT"
            + "  schregno,"
            + "  curriculum_cd,"
            + "  classcd,"
            + "  subclasscd,"
            + "  month,"
            + "  updated,"
            + "  score,"
            + "  registercd"
            + " FROM"
            + "  rec_test_dat"
            + " WHERE"
            + "  score is not null AND"
            + "  year='" + _param.getYear() + "'"
            ;
        return sql;
    }

    private static String getTestCode(final String month) {
        // 9月?
        if ("09".equals(month)) {
            return "1";
        }
        // 3月?
        if ("03".equals(month)) {
            return "2";
        }
        log.warn("テストで想定外の不明な月=" + month);
        return "?";
    }
} // MkSeitoRishuKadaiJisseki

// eof
