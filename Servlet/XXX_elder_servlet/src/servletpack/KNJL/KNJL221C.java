// kanji=漢字
/*
 * $Id: 05236242bad8b5e7283f7cfbedf6fd1330da5dcd $
 *
 * 作成日: 2011/10/07 15:05:15 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 05236242bad8b5e7283f7cfbedf6fd1330da5dcd $
 */
public class KNJL221C {

    private static final Log log = LogFactory.getLog("KNJL221C.class");

    private boolean _hasData;
    private final String FRM_NAME = "KNJL221C.frm";

    Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        svf.VrSetForm(FRM_NAME, 1);
        final List scoreList = getScoreList(db2);
        final int LEN_MAX = 4; //max列数
        final int ONE_LINE_MAX = 40; //1列のmax行数
        final int ALL_LINE_MAX = ONE_LINE_MAX * LEN_MAX; //1ページのmax行数
        final int pageMax = scoreList.size() / ALL_LINE_MAX + (scoreList.size() % ALL_LINE_MAX == 0 ? 0 : 1);
        int c = 0;
        int one_line_total = 0; //1列の小計
        for (final Iterator it = scoreList.iterator(); it.hasNext();) {
            final Score sco = (Score) it.next();

            svf.VrsOut("NENDO", _param.getYear());
            svf.VrsOut("APPLICANTDIV", _param._applicantdivname);
            svf.VrsOut("SCHOOLNAME", _param._schoolName);
            svf.VrsOut("PRE_TESTDIV", _param._preTestDivName);
            svf.VrsOut("SUBCLASS", _param._subclassName);
            svf.VrsOut("DATE", _param.getDate());

            c += 1;
            svf.VrsOut("PAGE", String.valueOf(c / ALL_LINE_MAX + (c % ALL_LINE_MAX == 0 ? 0 : 1)));
            svf.VrsOut("TOTAL_PAGE", String.valueOf(pageMax));

            int gyo = c % ONE_LINE_MAX == 0 ? ONE_LINE_MAX : c % ONE_LINE_MAX;
            int len = c / ONE_LINE_MAX + (c % ONE_LINE_MAX == 0 ? 0 : 1);
            int no = len % LEN_MAX == 0 ? LEN_MAX : len % LEN_MAX;
            svf.VrsOutn("EXAMNO" + String.valueOf(no), gyo, sco._receptno);
            svf.VrsOutn("POINT" + String.valueOf(no), gyo, sco._score);

            if (gyo == 1) one_line_total = 0;
            if (sco._score != null && !"".equals(sco._score)) one_line_total += Integer.parseInt(sco._score);
            svf.VrsOutn("EXAMNO" + String.valueOf(no), ONE_LINE_MAX + 1, "小計");
            svf.VrsOutn("POINT" + String.valueOf(no), ONE_LINE_MAX + 1, String.valueOf(one_line_total));

            _hasData = true;
            if (c == scoreList.size() || c % ALL_LINE_MAX == 0) {
                svf.VrEndPage();
            }
        }
    }

    private int getMS932ByteCount(final String str) {
        if (null == str) return 0;
        int ret = 0;
        try {
            ret = str.getBytes("MS932").length;
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return ret;
    }

    private List getScoreList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getScoreSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String receptno = rs.getString("PRE_RECEPTNO");
                final String score = rs.getString("SCORE");

                final Score s = new Score(receptno, score);
                retList.add(s);
            }

        } catch (Exception ex) {
            log.error("getScoreList error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getScoreSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.PRE_RECEPTNO, ");
        stb.append("     T1.SCORE ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_SCORE_PRE_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND T1.TESTSUBCLASSCD = '" + _param._subclassCd + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.PRE_RECEPTNO ");
        return stb.toString();
    }

    private class Score {
        final String _receptno;
        final String _score;

        public Score(final String receptno, final String score) {
            _receptno = receptno;
            _score = score;
        }

        public String toString() {
            return _receptno + " : 得点 = " + _score;
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70065 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _applicantdiv;
        private final String _applicantdivname;
        private final String _subclassCd;
        private final String _ctrlDate;

        private final String _preTestDiv;
        private final String _preTestDivName;
        private final String _subclassName;
        private final String _schoolName;
        private final boolean _seirekiFlg;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _applicantdiv = "1";
            _applicantdivname = getNameMst(db2, "L103", _applicantdiv);
            _preTestDiv = request.getParameter("PRE_TESTDIV");
            _subclassCd = request.getParameter("TESTSUBCLASSCD");
            _ctrlDate = request.getParameter("CTRL_DATE");

            _preTestDivName = getNameMst(db2, "L104", _preTestDiv);
            _subclassName = getSubclassName(db2);
            _schoolName = getSchoolName(db2);
            _seirekiFlg = getSeirekiFlg(db2);
        }

        private String getSubclassName(DB2UDB db2) {
            String name = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L109' AND NAMECD2 = '" + _subclassCd + "' AND ABBV3 = '" + _preTestDiv + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    name = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.error("getSubclassName Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name;
        }

        private String getSchoolName(DB2UDB db2) {
            String name = null;

            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME ");
            sql.append(" FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '"+_year+"' AND CERTIF_KINDCD = '105' ");
            try{
                PreparedStatement ps = db2.prepareStatement(sql.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                   name = rs.getString("SCHOOL_NAME");
                }
            } catch (SQLException ex) {
                log.error(ex);
            }

            return name;
        }
        /**
         * 日付表示の和暦(年号)/西暦使用フラグ
         * @param db2
         * @return
         */
        private boolean getSeirekiFlg(DB2UDB db2) {
            boolean seirekiFlg = false;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if ( rs.next() ) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            }
            return seirekiFlg;
        }

        private String getNameMst(final DB2UDB db2, final String namecd1, final String namecd2) {

            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT NAME1 ");
            sql.append(" FROM NAME_MST ");
            sql.append(" WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
            String name = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try{
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                   name = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name;
        }

        public String getYear() {
            return _seirekiFlg ? _year + "年度": KNJ_EditDate.h_format_JP_N(_year + "-01-01") + "度";
        }

        /**
         * 日付の名称を得る
         * @return 日付の名称
         */
        public String getDate() {
            return _seirekiFlg ?
                    (_ctrlDate.substring(0,4) + "年" + KNJ_EditDate.h_format_JP_MD(_ctrlDate)) : (KNJ_EditDate.h_format_JP(_ctrlDate));
        }

    }
}

// eof
