// kanji=漢字
/*
 * $Id: cc35e3ec5aaf4096aecba2333dc39035c7f5160d $
 *
 * 作成日: 2009/11/06 15:40:56 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: cc35e3ec5aaf4096aecba2333dc39035c7f5160d $
 */
public class KNJF131A {

    private static final Log log = LogFactory.getLog("KNJF131A.class");

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

            for (int i = 0; i < _param._division.length; i++) {
                final String division = _param._division[i];
                if (null == division) break;
                log.fatal("区分=" + division);
                printMain(db2, svf, division);
            }

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final String division) throws SQLException {
        final List printDataCnts = getPrintDataCnt(db2, division);
        svf.VrSetForm("KNJF131A.frm", 4);
        svf.VrsOut("DIVISION", _param.printKubun(division));
        String gradeFlg = "";
        for (final Iterator it = printDataCnts.iterator(); it.hasNext();) {
            final DataCnt dataCnt = (DataCnt) it.next();
            //学年出力
            if (!gradeFlg.equals("") && !gradeFlg.equals(dataCnt._grade)) {
                svf.VrEndRecord();
            }
            //ヘッダ
            svf.VrsOut("PERIOD", _param.printKikan());
            svf.VrsOut("DATE1", _param._printDate);
            //明細
            svf.VrsOut("GDAT", dataCnt._gradeName);
            svf.VrsOutn("SEX" + dataCnt._sex, dataCnt.getRetuNo(), dataCnt._cnt);
            //保持
            gradeFlg = dataCnt._grade;
        }
        if (!gradeFlg.equals("")) {
            //合計出力
            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private List getPrintDataCnt(final DB2UDB db2, final String division) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getDataCntSql(division);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String gradeName = rs.getString("GRADE_NAME");
                final String sex = rs.getString("SEX");
                final String daycd = rs.getString("DAYCD");
                final String cnt = rs.getString("CNT");
                final DataCnt dataCnt = new DataCnt(
                        grade,
                        gradeName,
                        sex,
                        daycd,
                        cnt);
                rtnList.add(dataCnt);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getDataCntSql(final String division) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_VISITREC AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.VISIT_DATE, ");
        stb.append("         DAYOFWEEK(T1.VISIT_DATE) AS DAYCD, ");
        stb.append("         T2.GRADE, ");
        stb.append("         T3.SEX ");
        stb.append("     FROM ");
        stb.append("         NURSEOFF_VISITREC_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("                                      AND T2.YEAR = '" + _param._year + "' ");
        stb.append("         INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         T1.VISIT_DATE BETWEEN date('" + _param._sDate + "') AND date('" + _param._eDate + "') ");
        stb.append("         AND T2.SEMESTER = (SELECT MAX(SEMESTER) FROM SCHREG_REGD_DAT ");
        stb.append("                           WHERE SCHREGNO = T1.SCHREGNO AND YEAR = T2.YEAR) ");
        if (!"9".equals(division)) {
            stb.append("     AND T1.TYPE = '" + division + "' ");
        }
        stb.append("     ) ");
        stb.append(" , CNT_VISITREC AS ( ");
        stb.append("     SELECT ");
        stb.append("         value(GRADE, '99') as GRADE, ");
        stb.append("         value(SEX, '9') as SEX, ");
        stb.append("         value(DAYCD, 9) as DAYCD, ");
        stb.append("         COUNT(*) AS CNT ");
        stb.append("     FROM ");
        stb.append("         T_VISITREC ");
        stb.append("     GROUP BY ");
        stb.append("         GROUPING SETS ( ");
        stb.append("         (DAYCD, GRADE, SEX), ");
        stb.append("         (DAYCD, GRADE), ");
        stb.append("         (DAYCD, SEX), ");
        stb.append("         (GRADE, SEX), ");
        stb.append("         (SEX), ");
        stb.append("         (GRADE), ");
        stb.append("         (DAYCD), ");
        stb.append("         ()) ");
        stb.append("     ) ");
        stb.append(" ,T_GDAT(GRADE, GRADE_NAME) AS ( ");
        stb.append("     SELECT ");
        stb.append("         GRADE, ");
        stb.append("         GRADE_NAME1 ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_GDAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._year + "' AND ");
        stb.append("         SCHOOL_KIND IN ('J','H') ");
        if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append("   AND SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        stb.append("     UNION ");
        stb.append("     VALUES('99', '合計') ");
        stb.append("     ) ");

        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.GRADE_NAME, ");
        stb.append("     L1.SEX, ");
        stb.append("     L1.DAYCD, ");
        stb.append("     L1.CNT ");
        stb.append(" FROM ");
        stb.append("     T_GDAT T1 ");
        stb.append("     LEFT JOIN CNT_VISITREC L1 ON L1.GRADE = T1.GRADE ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     L1.SEX, ");
        stb.append("     L1.DAYCD ");
        return stb.toString();
    }

    private class DataCnt {
        final String _grade;
        final String _gradeName;
        final String _sex;
        final String _daycd;
        final String _cnt;

        DataCnt(final String grade,
                final String gradeName,
                final String sex,
                final String daycd,
                final String cnt
        ) {
            _grade = grade;
            _gradeName = gradeName;
            _sex = sex;
            _daycd = daycd;
            _cnt = cnt;
        }

        private int getRetuNo() {
            if (null == _daycd) {
                return 0;
            }
            if ("9".equals(_daycd)) {
                return 8;
            } else if ("1".equals(_daycd)) {
                return 7;
            } else {
                return Integer.parseInt(_daycd) - 1;
            }
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
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String[] _division;
        private final String _sDate; //利用期間・開始日
        private final String _eDate; //利用期間・終了日
        final String _useSchool_KindField;
        final String _SCHOOLKIND;

        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _printDate; //作成日
        private boolean _isSeireki;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _division = request.getParameterValues("CATEGORY_SELECTED");
            final String sDate = request.getParameter("SDATE");
            _sDate = sDate.replace('/', '-');
            final String eDate = request.getParameter("EDATE");
            _eDate = eDate.replace('/', '-');
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");

            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            setSeirekiFlg(db2);
            _printDate = printDate(_ctrlDate);
        }

        private void setSeirekiFlg(final DB2UDB db2) {
            try {
                _isSeireki = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while( rs.next() ){
                    if (rs.getString("NAME1").equals("2")) _isSeireki = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        private String printDate(final String date) {
            if (null == date) {
                return "";
            }
            if (_isSeireki) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(date);
            }
        }

        private String printWeek(final String date) {
            if (null == date) {
                return "（）";
            }
            return "（" + KNJ_EditDate.h_format_W(date) + "）";
        }

        private String printKikan() {
            return printDate(_sDate) + printWeek(_sDate) + " \uFF5E " + printDate(_eDate) + printWeek(_eDate);
        }

        private String printKubun(final String division) {
            if (null == division) {
                return "";
            }
            if ("1".equals(division)) {
                return "内科";
            } else if ("2".equals(division)) {
                return "外科";
            } else if ("3".equals(division)) {
                return "その他";
            } else if ("4".equals(division)) {
                return "生徒以外";
            } else if ("5".equals(division)) {
                return "健康相談";
            } else if ("9".equals(division)) {
                return "全て";
            } else {
                return "";
            }
        }

    }
}

// eof
