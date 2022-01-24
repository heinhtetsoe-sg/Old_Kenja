// kanji=漢字
/*
 * $Id: MkSeitoRishuKamoku.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/03/24 16:23:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.markServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;

import jp.co.alp.kenja.batch.withus.Curriculum;

/**
 * 生徒履修科目データ。
 * @author takaesu
 * @version $Id: MkSeitoRishuKamoku.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkSeitoRishuKamoku extends Mk {
    private final static String _FILE = "MK_SEITO_RISHU_KAMOKU.csv";
    private final static String _履修期間コード = "1";
    private final static String _課題レベル = "0";

    /** 学籍在籍の課程コード */
    final Map _studentCoursecd = new HashMap();

    public MkSeitoRishuKamoku(final DB2UDB db, final Param param, final String title) throws SQLException {
        super(db, param, title);

        final List list = new ArrayList();

        // ヘッダを設定
        setHead(list);

        loadStudentCoursecd();

        // DBから取り込む
        ResultSet rs = null;
        try {
            _db.query(getSql());
            rs = _db.getResultSet();
            while(rs.next()) {
                final String schregno = rs.getString("schregno");
                final String coursecd = (String) _studentCoursecd.get(schregno); 
                if (null == coursecd) {
                    log.warn("生徒の課程コードが取得出来ない。:" + schregno);
                    continue;
                }
                final String[] fields = {
                        param.getSchoolDiv(),
                        schregno,
                        coursecd,
                        Curriculum.getCurriculumYear(rs.getString("curriculum_cd")), 
                        rs.getString("year"),
                        rs.getString("classcd"),
                        rs.getString("subclasscd"),
                        _履修期間コード,
                        _課題レベル,
                        cutDateDelimit(param.getUpdate()),
                };
                list.add(fields);
            }
        } catch (final SQLException e) {
            log.fatal("生徒履修科目の情報取得でエラー");
            throw e;
        } finally {
            _db.commit();
            DbUtils.closeQuietly(null, null, rs);
        }

        // CSVファイルに書く
        toCsv("生徒履修科目", _FILE, list);
    }
    
    private void loadStudentCoursecd() throws SQLException {
        ResultSet rs = null;
        try {
            _db.query("SELECT DISTINCT schregno, coursecd FROM schreg_regd_dat WHERE year='" + _param.getYear() + "'");
            rs = _db.getResultSet();
            while (rs.next()) {
                final String schregno = rs.getString("schregno");
                final String coursecd = rs.getString("coursecd");
                _studentCoursecd.put(schregno, coursecd);
            }
        } catch (final SQLException e) {
            log.fatal("生徒の課程コード取得でエラー");
            throw e;
        } finally {
            DbUtils.closeQuietly(null, null, rs);
        }
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
                "課題レベル",
                "更新日",
        };
        list.add(header);
    }

    private String getSql() {
        final String sql;
        sql = "SELECT"
            + "  schregno,"
            + "  curriculum_cd,"
            + "  year,"
            + "  classcd,"
            + "  SUBSTR(subclasscd,3,4) AS subclasscd"
            + " FROM"
            + "  comp_regist_dat"
            + " WHERE"
            + "  year='" + _param.getYear() + "'"
            ;
        return sql;
    }
} // MkSeitoRishuKamoku

// eof
