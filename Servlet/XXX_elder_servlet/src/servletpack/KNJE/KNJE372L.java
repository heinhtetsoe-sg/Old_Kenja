// kanji=漢字
/*
 * $Id: 69a644ed8b7267d6ea6e238068f147389d3620b4 $
 *
 * 作成日: 2009/10/21 8:52:18 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 69a644ed8b7267d6ea6e238068f147389d3620b4 $
 */
public class KNJE372L {

    private static final Log log = LogFactory.getLog("KNJE372L.class");

    private static final String ATTRIBUTE_NORMAL = "Paint=(0,0,0),Bold=0";
    private static final String ATTRIBUTE_HEIGAN = "Paint=(1,90,1),Bold=1";

    private boolean _hasData;

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
        final List printDataList = getPrintData(db2);

        svf.VrSetForm("KNJE372L.frm", 1);
        svf.VrsOut("TITLE", _param._nendo + "　学科振り分け順位表"); //タイトル
        svf.VrsOut("SUBTITLE", "（駒澤大学推薦の換算値）"); //サブタイトル
        svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(_param._ctrlDate)); //出力日付
        for (int no = 1; no <= 60; no++) {
            svf.VrsOutn("NO", no, String.valueOf(no));
        }

        int len = 0;
        for (final Iterator it = printDataList.iterator(); it.hasNext();) {
            final PrintData printData = (PrintData)it.next();
            len++;
            svf.VrsOutn("DEP_NO", len, printData._departmentS); //専願学科番号
            svf.VrsOutn("DEP_NAME", len, printData._departmentAbbv2); //学科略称2
            svf.VrsOutn("FRAME", len, printData._limitCount); //学科枠数

            int line = 0;
            for (final Iterator itRank = printData._rankList.iterator(); itRank.hasNext();) {
                final RankData rankData = (RankData)itRank.next();
                line++;
                if ("1".equals(rankData._amikakeFlg)) {
                    svf.VrAttributen("CONVERT" + len, line, ATTRIBUTE_HEIGAN);
                }
                svf.VrsOutn("CONVERT" + len, line, rankData._convertRank); //換算値順位
            }
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private List getPrintData(final DB2UDB db2) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getPrintSql();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String recommendationCd = StringUtils.defaultString(rs.getString("RECOMMENDATION_CD"));
                final String departmentS = StringUtils.defaultString(rs.getString("DEPARTMENT_S"));
                final String departmentH = StringUtils.defaultString(rs.getString("DEPARTMENT_H"));
                final String departmentAbbv2 = StringUtils.defaultString(rs.getString("DEPARTMENT_ABBV2"));
                final String limitCount = StringUtils.defaultString(rs.getString("LIMIT_COUNT"));

                final PrintData printData = new PrintData(recommendationCd, departmentS, departmentH, departmentAbbv2, limitCount);
                printData.setRankData(db2);
                rtnList.add(printData);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getPrintSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RECOMMENDATION_CD, ");
        stb.append("     DEPARTMENT_S, ");
        stb.append("     DEPARTMENT_H, ");
        stb.append("     DEPARTMENT_ABBV2, ");
        stb.append("     VALUE(LIMIT_COUNT_S, 0) + VALUE(LIMIT_COUNT_H, 0) AS LIMIT_COUNT ");
        stb.append(" FROM ");
        stb.append("     AFT_RECOMMENDATION_LIMIT_MST ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append(" ORDER BY ");
        stb.append("     DISP_ORDER ");

        return stb.toString();
    }

    private String getScoreDatSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     CONVERT_RANK, ");
        stb.append("     CASE WHEN RECOMMENDATION_DEPARTMENT_CD = '51' THEN '1' END AS AMIKAKE_FLG ");
        stb.append(" FROM ");
        stb.append("     AFT_SCHREG_CONVERT_SCORE_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND (RECOMMENDATION_DEPARTMENT_CD = '1' OR RECOMMENDATION_DEPARTMENT_CD = '51') ");
        stb.append(" ORDER BY ");
        stb.append("     CONVERT_RANK ");

        return stb.toString();
    }

    private class PrintData {
        final String _recommendationCd;
        final String _departmentS;
        final String _departmentH;
        final String _departmentAbbv2;
        final String _limitCount;
        final List _rankList;

        PrintData(final String recommendationCd,
                final String departmentS,
                final String departmentH,
                final String departmentAbbv2,
                final String limitCount
        ) {
            _recommendationCd = recommendationCd;
            _departmentS = departmentS;
            _departmentH = departmentH;
            _departmentAbbv2 = departmentAbbv2;
            _limitCount = limitCount;
            _rankList = new ArrayList();
        }

        private void setRankData(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getRankSql();
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                    final String convertRank = StringUtils.defaultString(rs.getString("CONVERT_RANK"));
                    final String amikakeFlg = StringUtils.defaultString(rs.getString("AMIKAKE_FLG"));

                    final RankData rankData = new RankData(schregno, convertRank, amikakeFlg);
                    _rankList.add(rankData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getRankSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     CONVERT_RANK, ");
            stb.append("     CASE WHEN RECOMMENDATION_DEPARTMENT_CD = '" + _departmentH + "' THEN '1' END AS AMIKAKE_FLG ");
            stb.append(" FROM ");
            stb.append("     AFT_SCHREG_CONVERT_SCORE_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' ");
            stb.append("     AND (RECOMMENDATION_DEPARTMENT_CD = '" + _departmentS + "' OR RECOMMENDATION_DEPARTMENT_CD = '" + _departmentH + "') ");
            stb.append(" ORDER BY ");
            stb.append("     CONVERT_RANK ");

            return stb.toString();
        }
    }

    private class RankData {
        final String _schregno;
        final String _convertRank;
        final String _amikakeFlg;

        RankData(final String schregno,
                final String convertRank,
                final String amikakeFlg
        ) {
            _schregno = schregno;
            _convertRank = convertRank;
            _amikakeFlg = amikakeFlg;
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
        log.fatal("$Revision: 75995 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _ctrlSemester;
        final String _ctrlDate;

        final String _nendo;
        final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlSemester = request.getParameter("LOGIN_SEMESTER");
            _ctrlDate = request.getParameter("LOGIN_DATE");

            _nendo = _year + "年度";
            _schoolName = getSchoolName(db2);
        }
        private String getSchoolName(final DB2UDB db2) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' AND SCHOOL_KIND = 'H'"));
        }
    }
}

// eof
