// kanji=漢字
/*
 * 作成日: 2021/01/04
 * 作成者: shimoji
 *
 * Copyright(C) 2004-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJA240G {

    private static final Log log = LogFactory.getLog("KNJA240A.class");

    private boolean _hasData;

    private Param _param;

    private final int HR_LINE_MAX = 11;
    private final int GRADE_COL_MAX = 3;
    private final String WHITE_SPACE_FILE_NAME = "whitespace.png";
    private final String ABSENCE = "2";

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
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        svf.VrSetForm("KNJA240G.frm", 1);

        Map<String, List<Map<String, Grade>>> schoolKindMap = getSchoolKindMap(db2);

        for (List<Map<String, Grade>> gradeList : schoolKindMap.values()) {
            // 校種内での合計を算出する
            int mailCntSchoolKind = 0;
            int exchMailCntSchoolKind = 0;
            int femailCntSchoolKind = 0;
            int exchFemailCntSchoolKind = 0;
            int totalSchoolKind = 0;
            int exchTotalSchoolKind = 0;
            for (Map<String, Grade> gradeMap : gradeList) {
                for (Grade grade : gradeMap.values()) {
                    for (Hr hr : grade._hrList) {
                        mailCntSchoolKind       += Integer.parseInt(hr._mailCnt);
                        exchMailCntSchoolKind   += Integer.parseInt(hr._exchMailCnt);
                        femailCntSchoolKind     += Integer.parseInt(hr._femailCnt);
                        exchFemailCntSchoolKind += Integer.parseInt(hr._exchFemailCnt);
                        totalSchoolKind         += Integer.parseInt(hr._total);
                        exchTotalSchoolKind     += Integer.parseInt(hr._exchTotal);
                    }
                }
            }

            for (int i = 0; i < gradeList.size(); i++) {
                Map<String, Grade> gradeMap = gradeList.get(i);

                // 印字する３学年内で最大頁数を算出する
                int lineMax = 0;
                for (Grade grade : gradeMap.values()) {
                    lineMax = lineMax < grade._hrList.size() ? grade._hrList.size() : lineMax;
                }
                int pageMax = (int)Math.ceil((double)lineMax / (double)HR_LINE_MAX);

                // 最大頁数分だけ繰り返す。
                for (int pageCnt = 0; pageCnt < pageMax; pageCnt++) {
                    svf.VrsOut("TITLE", _param._year + "年度　生徒人数表");
                    svf.VrsOut("PRINT_DATE", "出力日：" + _param._loginDate);
                    svf.VrsOut("CURRENT_DATE", _param._date + "　現在");

                    // 校種内で最後に出力する要素の場合は右下の合計欄を出力する
                    if ((i == (gradeList.size() - 1)) && (pageCnt == (pageMax - 1))) {
                        svf.VrsOut("NUM4_1",   String.valueOf(mailCntSchoolKind));
                        svf.VrsOut("NUM4_2",   String.valueOf(exchMailCntSchoolKind));
                        svf.VrsOut("NUM4_2_2", "(   )");
                        svf.VrsOut("NUM4_3",   String.valueOf(femailCntSchoolKind));
                        svf.VrsOut("NUM4_4",   String.valueOf(exchFemailCntSchoolKind));
                        svf.VrsOut("NUM4_4_2", "(   )");
                        svf.VrsOut("NUM4_5",   String.valueOf(totalSchoolKind));
                        svf.VrsOut("NUM4_6",   String.valueOf(exchTotalSchoolKind));
                        svf.VrsOut("NUM4_6_2", "(   )");
                    } else {
                        if (_param._isWhiteSpaceFilePathExsits) {
                            svf.VrsOut("BLANK", _param._whiteSpaceFilePath);
                        }
                    }

                    int colCnt = 1;

                    for (Grade grade : gradeMap.values()) {
                        int lineCnt = 1;

                        for (int j = pageCnt * HR_LINE_MAX; j < (pageCnt + 1) * HR_LINE_MAX; j++) {
                            if (grade._hrList.size() - 1 < j) {
                                break;
                            }
                            Hr hr = grade._hrList.get(j);

                            svf.VrsOutn("HR_NAME" + colCnt, lineCnt, hr._hrClassName);
                            int staffNameFieldByte = KNJ_EditEdit.getMS932ByteLength(hr._staffName);
                            final String staffNameFieldStr = staffNameFieldByte > 14 ? "2" : "1";
                            svf.VrsOutn("TR_NAME" + colCnt + "_" + staffNameFieldStr, lineCnt, hr._staffName);
                            svf.VrsOutn("NUM" + colCnt + "_1",   lineCnt, hr._mailCnt);
                            svf.VrsOutn("NUM" + colCnt + "_2",   lineCnt, hr._exchMailCnt);
                            svf.VrsOutn("NUM" + colCnt + "_2_2", lineCnt, "(   )");
                            svf.VrsOutn("NUM" + colCnt + "_3",   lineCnt, hr._femailCnt);
                            svf.VrsOutn("NUM" + colCnt + "_4",   lineCnt, hr._exchFemailCnt);
                            svf.VrsOutn("NUM" + colCnt + "_4_2", lineCnt, "(   )");
                            svf.VrsOutn("NUM" + colCnt + "_5",   lineCnt, hr._total);

                            lineCnt++;
                            _hasData = true;
                        }

                        colCnt++;
                    }

                    // 最終頁の場合は学年の合計を出力する
                    if (pageCnt == (pageMax - 1)) {
                        int totalLine = HR_LINE_MAX + 1;

                        colCnt = 1;

                        for (Grade grade : gradeMap.values()) {
                            svf.VrsOutn("HR_NAME" + colCnt,          totalLine, "合計");
                            svf.VrsOutn("NUM"     + colCnt + "_1",   totalLine, String.valueOf(grade._mailCnt));
                            svf.VrsOutn("NUM"     + colCnt + "_2",   totalLine, String.valueOf(grade._exchMailCnt));
                            svf.VrsOutn("NUM"     + colCnt + "_2_2", totalLine, "(   )");
                            svf.VrsOutn("NUM"     + colCnt + "_3",   totalLine, String.valueOf(grade._femailCnt));
                            svf.VrsOutn("NUM"     + colCnt + "_4",   totalLine, String.valueOf(grade._exchFemailCnt));
                            svf.VrsOutn("NUM"     + colCnt + "_4_2", totalLine, "(   )");
                            svf.VrsOutn("NUM"     + colCnt + "_5",   totalLine, String.valueOf(grade._total));

                            colCnt++;
                        }
                    }

                    svf.VrEndPage();
                }
            }
        }
    }

    private Map<String, List<Map<String, Grade>>> getSchoolKindMap(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, List<Map<String, Grade>>> schoolKindMap = new LinkedHashMap<String, List<Map<String, Grade>>>();
        List<Map<String, Grade>> gradeList = new ArrayList<Map<String, Grade>>();
        Map<String, Grade> gradeMap = new LinkedHashMap<String, Grade>();
        //Grade gradeC = null;
        Hr hr = null;

        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrClassName = rs.getString("HR_NAME");
                final String staffName = rs.getString("STAFFNAME");
                final String mailCnt = rs.getString("MAIL_CNT");
                final String exchMailCnt = rs.getString("EXCH_MAIL_CNT");
                final String femailCnt = rs.getString("FEMAIL_CNT");
                final String exchFemailCnt = rs.getString("EXCH_FEMAIL_CNT");
                final String total = rs.getString("TOTAL");
                final String exchTotal = rs.getString("EXCH_TOTAL");

                hr = new Hr(schoolKind, grade, hrClass, hrClassName, staffName, mailCnt, exchMailCnt, femailCnt, exchFemailCnt, total, exchTotal);
                if (schoolKindMap.containsKey(schoolKind)) {
                    gradeList = schoolKindMap.get(schoolKind);
                } else {
                    gradeList = new ArrayList<Map<String, Grade>>();
                    gradeMap = new LinkedHashMap<String, Grade>();
                    gradeList.add(gradeMap);
                    schoolKindMap.put(schoolKind, gradeList);
                }

                Grade gradeC = null;
                for (Map<String, Grade> refMap : gradeList) {
                    if (refMap.containsKey(grade)) {
                        gradeC = refMap.get(grade);
                    }
                }
                if (gradeC == null) {
                    gradeC = new Grade();

                    if (GRADE_COL_MAX <= gradeMap.size()) {
                        gradeMap = new LinkedHashMap<String, Grade>();
                        gradeList.add(gradeMap);
                    }

                    gradeMap.put(grade, gradeC);
                }

                gradeC.add(hr);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return schoolKindMap;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH DAT AS ( ");
        stb.append("     SELECT ");
        stb.append("         DAT.YEAR, ");
        stb.append("         DAT.SEMESTER, ");
        stb.append("         DAT.GRADE, ");
        stb.append("         DAT.HR_CLASS, ");
        stb.append("         CASE WHEN BASE.SEX = '1' THEN 1 ELSE 0 END AS MAIL_CNT, ");
        stb.append("         CASE WHEN BASE.SEX = '1' AND TRANSFER.TRANSFERCD = '1' THEN 1 ELSE 0 END AS EXCH_MAIL_CNT, ");
        stb.append("         CASE WHEN BASE.SEX = '2' THEN 1 ELSE 0 END AS FEMAIL_CNT, ");
        stb.append("         CASE WHEN BASE.SEX = '2' AND TRANSFER.TRANSFERCD = '1' THEN 1 ELSE 0 END AS EXCH_FEMAIL_CNT ");

        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT DAT ");

        stb.append("         INNER JOIN SCHREG_BASE_MST BASE ON ");
        stb.append("                    BASE.SCHREGNO = DAT.SCHREGNO ");
        stb.append("                AND ( ");
        stb.append("                        ( ");
        stb.append("                            BASE.GRD_DATE IS NOT NULL AND ");
        stb.append("                            ('" + _param._date + "' BETWEEN BASE.ENT_DATE AND BASE.GRD_DATE) ");
        stb.append("                        ) ");
        stb.append("                     OR ( ");
        stb.append("                            BASE.GRD_DATE IS NULL AND ");
        stb.append("                            (BASE.ENT_DATE <= '" + _param._date + "') ");
        stb.append("                        ) ");
        stb.append("                    ) ");

        stb.append("         LEFT JOIN SCHREG_TRANSFER_DAT TRANSFER ON ");
        stb.append("                   TRANSFER.SCHREGNO       = DAT.SCHREGNO ");
        stb.append("               AND ('" + _param._date + "' BETWEEN TRANSFER.TRANSFER_SDATE AND TRANSFER.TRANSFER_EDATE) ");

        stb.append("     WHERE ");
        stb.append("          DAT.YEAR     = '" + _param._year + "' AND ");
        stb.append("          DAT.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("          VALUE(TRANSFER.TRANSFERCD, 0) <> '" + ABSENCE + "' ");
        stb.append(" ), ");
        stb.append(" GRP_DAT AS ( ");
        stb.append("     SELECT ");
        stb.append("         YEAR, ");
        stb.append("         SEMESTER, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         SUM(MAIL_CNT) AS MAIL_CNT, ");
        stb.append("         SUM(EXCH_MAIL_CNT) AS EXCH_MAIL_CNT, ");
        stb.append("         SUM(FEMAIL_CNT) AS FEMAIL_CNT, ");
        stb.append("         SUM(EXCH_FEMAIL_CNT) AS EXCH_FEMAIL_CNT ");
        stb.append("     FROM ");
        stb.append("         DAT ");
        stb.append("     GROUP BY ");
        stb.append("         YEAR, ");
        stb.append("         SEMESTER, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     GDAT.SCHOOL_KIND, ");
        stb.append("     GRP_DAT.GRADE, ");
        stb.append("     GRP_DAT.HR_CLASS, ");
        stb.append("     HDAT.HR_NAME, ");
        stb.append("     STAFF.STAFFNAME, ");
        stb.append("     GRP_DAT.MAIL_CNT, ");
        stb.append("     GRP_DAT.EXCH_MAIL_CNT, ");
        stb.append("     GRP_DAT.FEMAIL_CNT, ");
        stb.append("     GRP_DAT.EXCH_FEMAIL_CNT, ");
        stb.append("     GRP_DAT.MAIL_CNT + GRP_DAT.FEMAIL_CNT AS TOTAL, ");
        stb.append("     GRP_DAT.EXCH_MAIL_CNT + GRP_DAT.EXCH_FEMAIL_CNT AS EXCH_TOTAL ");

        stb.append(" FROM ");
        stb.append("     GRP_DAT ");

        stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON ");
        stb.append("                GDAT.YEAR  = GRP_DAT.YEAR ");
        stb.append("            AND GDAT.GRADE = GRP_DAT.GRADE ");

        stb.append("     INNER JOIN V_NAME_MST A023 ON ");
        stb.append("                A023.YEAR     = GDAT.YEAR ");
        stb.append("            AND A023.NAMECD1  = 'A023' ");
        stb.append("            AND A023.NAME1   IN (GDAT.SCHOOL_KIND) ");

        stb.append("     INNER JOIN SCHREG_REGD_HDAT HDAT ON ");
        stb.append("                HDAT.YEAR     = GRP_DAT.YEAR ");
        stb.append("            AND HDAT.SEMESTER = GRP_DAT.SEMESTER ");
        stb.append("            AND HDAT.GRADE    = GRP_DAT.GRADE ");
        stb.append("            AND HDAT.HR_CLASS = GRP_DAT.HR_CLASS ");

        stb.append("     INNER JOIN STAFF_MST STAFF ON ");
        stb.append("                STAFF.STAFFCD = HDAT.TR_CD1 ");

        stb.append(" ORDER BY ");
        stb.append("     A023.ABBV2, ");
        stb.append("     GRP_DAT.GRADE, ");
        stb.append("     GRP_DAT.HR_CLASS ");
        return stb.toString();
    }

    private class Grade {
        final List<Hr> _hrList;
        int _mailCnt;
        int _exchMailCnt;
        int _femailCnt;
        int _exchFemailCnt;
        int _total;
        int _exchTotal;

        Grade() {
            _hrList = new ArrayList<Hr>();
            _mailCnt       = 0;
            _exchMailCnt   = 0;
            _femailCnt     = 0;
            _exchFemailCnt = 0;
            _total         = 0;
            _exchTotal     = 0;
        }

        void add(Hr hr) {
            _hrList.add(hr);
            _mailCnt       += Integer.parseInt(hr._mailCnt);
            _exchMailCnt   += Integer.parseInt(hr._exchMailCnt);
            _femailCnt     += Integer.parseInt(hr._femailCnt);
            _exchFemailCnt += Integer.parseInt(hr._exchFemailCnt);
            _total         += Integer.parseInt(hr._total);
            _exchTotal     += Integer.parseInt(hr._exchTotal);
        }
    }

    private class Hr {
        final String _schoolKind;
        final String _grade;
        final String _hrClass;
        final String _hrClassName;
        final String _staffName;
        final String _mailCnt;
        final String _exchMailCnt;
        final String _femailCnt;
        final String _exchFemailCnt;
        final String _total;
        final String _exchTotal;

        Hr(final String schoolKind,
           final String grade,
           final String hrClass,
           final String hrClassName,
           final String staffName,
           final String mailCnt,
           final String exchMailCnt,
           final String femailCnt,
           final String exchFemailCnt,
           final String total,
           final String exchTotal
        ) {
            _schoolKind    = schoolKind;
            _grade         = grade;
            _hrClass       = hrClass;
            _hrClassName   = hrClassName;
            _staffName     = staffName;
            _mailCnt       = mailCnt;
            _exchMailCnt   = exchMailCnt;
            _femailCnt     = femailCnt;
            _exchFemailCnt = exchFemailCnt;
            _total         = total;
            _exchTotal     = exchTotal;
        }

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _loginDate;
        private final String _date;
        private final String _documentroot;
        private final String _whiteSpaceFilePath;
        private final boolean _isWhiteSpaceFilePathExsits;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year         = request.getParameter("YEAR");
            _semester     = request.getParameter("SEMESTER");
            _loginDate    = request.getParameter("LOGIN_DATE");
            _date         = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _documentroot = request.getParameter("DOCUMENTROOT");

            _whiteSpaceFilePath = _documentroot + "/image/" + WHITE_SPACE_FILE_NAME;
            _isWhiteSpaceFilePathExsits = (new File(_whiteSpaceFilePath)).exists();
        }
    }
}

// eof
