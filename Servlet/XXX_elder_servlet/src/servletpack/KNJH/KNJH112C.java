/*
 * $Id: e78ea282121ceea1a7c01501d4a4ee2d6383e524 $
 *
 * 作成日: 2017/12/14
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJH112C {

    private static final Log log = LogFactory.getLog(KNJH112C.class);

    private boolean _hasData;

    private Param _param;

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
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            final List sikakuTorokuList = Sikaku.getSikakuList(db2, _param);

            if (_param._isCsv) {
                outputCsv(response, sikakuTorokuList);
            } else {
                response.setContentType("application/pdf");

                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());

                if (!sikakuTorokuList.isEmpty()) {
                    printMain(svf, sikakuTorokuList);
                }
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (null != _param && _param._isCsv) {
            } else {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void outputCsv(final HttpServletResponse response, final List sikakuTorokuList) {
        final List lines = getCsvOutputLines(sikakuTorokuList);

        CsvUtils.outputLines(log, response, getTitle() + " " + getSubtitle() + ".csv" , lines);
    }

    private List getCsvOutputLines(final List sikakuTorokuList) {
        final List lines = new ArrayList();

        String befHrName = "";
        newLine(lines).addAll(Arrays.asList(new String[] {getTitle() + " " + getSubtitle(), "", getPrintDate()}));
        newLine(lines).addAll(Arrays.asList(new String[] {_param._sikakuName}));
        final List header1 = newLine(lines);
        header1.addAll(Arrays.asList(new String[] {"年組", "番号", "氏名", "性別", "受験日", "級・段位", "受験料", "受験結果", "取得級",}));

        for (Iterator iterator = sikakuTorokuList.iterator(); iterator.hasNext();) {
            final Sikaku sikaku = (Sikaku) iterator.next();

            if (!"".equals(befHrName) && !befHrName.equals(sikaku._hrName)) {
                newLine(lines).addAll(Arrays.asList(new String[] {""})); //クラスが変わったら改行
            }
            final List line = newLine(lines);

            line.add(sikaku._hrName);        // 年組
            line.add(sikaku._attendNo);      // 出席番号
            line.add(sikaku._name);          // 氏名
            line.add(sikaku._sex);           // 性別
            line.add(KNJ_EditDate.h_format_thi(sikaku._testDate, 0));      // 受験日
            line.add(sikaku._testNameAbbv);  // 級・段位
            line.add(sikaku._testFee);       // 受験料
            line.add(sikaku._result);        // 受験結果
            line.add(sikaku._syutokuKyu);    // 取得級

            befHrName = sikaku._hrName;
        }

        _hasData = true;

        return lines;
    }

    private List newLine(final List lines) {
        final List line = new ArrayList();
        lines.add(line);
        return line;
    }

    private String getTitle() {
        final String title = KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　受験申込一覧";
        return title;
    }

    private String getSubtitle() {
        final String setFjDate = KNJ_EditDate.h_format_JP(_param._fjDate);
        final String[] partsFjDate = KNJ_EditDate.tate_format(setFjDate);
        final String setTjDate = KNJ_EditDate.h_format_JP(_param._tjDate);
        final String[] partsTjDate = KNJ_EditDate.tate_format(setTjDate);
        final String subtitle = "(受験期間" + partsFjDate[2] + "/" + partsFjDate[3]  + "～" + partsTjDate[2] + "/" + partsTjDate[3] +  ")";
        return subtitle;
    }

    private String getPrintDate() {
        final String printDate = "作成日：" + KNJ_EditDate.h_format_JP(_param._ctrlDate);
        return printDate;
    }

    private void printMain(final Vrw32alp svf, final List sikakuTorokuList) {
        svf.VrSetForm("KNJH112C.frm", 1);

        final int maxLine = 50;
        int printLine = 1;
        String befHrName = "";

        for (Iterator iterator = sikakuTorokuList.iterator(); iterator.hasNext();) {
            final Sikaku sikaku = (Sikaku) iterator.next();
            if (!"".equals(befHrName) && !befHrName.equals(sikaku._hrName)) {
                svf.VrEndPage();
                printLine = 1;
            }
            if (printLine > maxLine) {
                svf.VrEndPage();
                printLine = 1;
            }
            svf.VrsOut("TITLE", getTitle());
            svf.VrsOut("SUBTITLE", getSubtitle());
            svf.VrsOut("HR_NAME", sikaku._hrName);// 年組
            svf.VrsOut("QUALIFY", _param._sikakuName);
            svf.VrsOut("DATE", getPrintDate());

            svf.VrsOutn("NO", printLine, sikaku._attendNo);// 出席番号
            final String nameField = KNJ_EditEdit.getMS932ByteLength(sikaku._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(sikaku._name) > 20 ? "2" : "1";
            svf.VrsOutn("NAME" + nameField, printLine,  sikaku._name);// 氏名
            svf.VrsOutn("SEX", printLine, sikaku._sex);// 性別
            svf.VrsOutn("EXAM_DATE", printLine, KNJ_EditDate.h_format_thi(sikaku._testDate, 0));// 受験日
            final String rank1Field = KNJ_EditEdit.getMS932ByteLength(sikaku._testNameAbbv) > 12 ? "2" : "1";
            svf.VrsOutn("RANK1_" + rank1Field, printLine, sikaku._testNameAbbv);// 級・段位
            svf.VrsOutn("EXAM_FEE", printLine, sikaku._testFee);// 受験料
            svf.VrsOutn("RESULT", printLine, sikaku._result);// 受験結果
            final String rank2Field = KNJ_EditEdit.getMS932ByteLength(sikaku._syutokuKyu) > 12 ? "2" : "1";
            svf.VrsOutn("RANK2_" + rank2Field, printLine, sikaku._syutokuKyu);// 取得級

            printLine++;
            befHrName = sikaku._hrName;

            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private static class Sikaku {
        final String _qualifiedName;
        final String _hrName;
        final String _attendNo;
        final String _name;
        final String _sex;
        final String _testDate;
        final String _testNameAbbv;
        final String _testFee;
        final String _result;
        final String _syutokuKyu;
        Sikaku(
            final String qualifiedName,
            final String hrName,
            final String attendNo,
            final String name,
            final String sex,
            final String testDate,
            final String testNameAbbv,
            final String testFee,
            final String result,
            final String syutokuKyu
        ) {
            _qualifiedName   = qualifiedName;
            _hrName          = hrName;
            _attendNo        = attendNo;
            _name            = name;
            _sex             = sex;
            _testDate        = testDate;
            _testNameAbbv    = testNameAbbv;
            _testFee         = testFee;
            _result          = result;
            _syutokuKyu      = syutokuKyu;
        }

        public static List getSikakuList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.info(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String qualifiedName   = rs.getString("QUALIFIED_NAME");
                    final String hrName          = rs.getString("HR_NAME");
                    final String attendNo        = rs.getString("ATTENDNO");
                    final String name            = rs.getString("NAME");
                    final String sex             = rs.getString("SEX");
                    final String testDate        = rs.getString("TEST_DATE");
                    final String testNameAbbv    = rs.getString("TEST_NAME_ABBV");
                    final String testFee         = rs.getString("TEST_FEE");
                    final String result          = rs.getString("RESULT");
                    final String syutokuKyu      = rs.getString("SYUTOKU_KYU");

                    final Sikaku recept = new Sikaku(qualifiedName, hrName, attendNo, name, sex, testDate, testNameAbbv, testFee, result, syutokuKyu);
                    list.add(recept);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return list;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     QU_M.QUALIFIED_NAME, ");
            stb.append("     HDAT.HR_NAME, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     Z002.NAME1 AS SEX, ");
            stb.append("     TEST.TEST_DATE, ");
            stb.append("     TS_M.TEST_NAME_ABBV, ");
            stb.append("     TS_M.TEST_FEE, ");
            stb.append("     CASE ");
            stb.append("         WHEN TEST.RESULT_CD = '9999' THEN '不合格' ");
            stb.append("         WHEN TEST.RESULT_CD = '8888' THEN '欠席' ");
            stb.append("         WHEN TEST.RESULT_CD IS NULL THEN '' ");
            stb.append("         ELSE '合格' ");
            stb.append("     END AS RESULT, ");
            stb.append("     VALUE(TS_M2.TEST_NAME_ABBV, '') AS SYUTOKU_KYU ");
            stb.append(" FROM ");
            stb.append("     SCHREG_QUALIFIED_TEST_DAT TEST ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON TEST.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON TEST.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                                   AND TEST.YEAR     = REGD.YEAR ");
            stb.append("                                   AND REGD.SEMESTER = '" + param._semester + "' ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON TEST.YEAR     = HDAT.YEAR ");
            stb.append("                                    AND REGD.SEMESTER = HDAT.SEMESTER ");
            stb.append("                                    AND REGD.GRADE    = HDAT.GRADE ");
            stb.append("                                    AND REGD.HR_CLASS = HDAT.HR_CLASS ");
            stb.append("     LEFT JOIN QUALIFIED_TEST_MST TS_M ON TEST.YEAR         = TS_M.YEAR ");
            stb.append("                                      AND TEST.QUALIFIED_CD = TS_M.QUALIFIED_CD ");
            stb.append("                                      AND TEST.TEST_CD      = TS_M.TEST_CD ");
            stb.append("     LEFT JOIN QUALIFIED_RESULT_MST RS_M ON TEST.YEAR         = RS_M.YEAR ");
            stb.append("                                        AND TEST.QUALIFIED_CD = RS_M.QUALIFIED_CD ");
            stb.append("                                        AND TEST.RESULT_CD    = RS_M.RESULT_CD ");
            stb.append("     LEFT JOIN (SELECT ");
            stb.append("                    SCHREGNO, ");
            stb.append("                    MIN(RESULT_CD) AS RESULT_CD ");
            stb.append("                FROM ");
            stb.append("                    SCHREG_QUALIFIED_TEST_DAT ");
            stb.append("                WHERE ");
            stb.append("                        RESULT_CD NOT IN ('8888', '9999') ");
            stb.append("                    AND RESULT_CD IS NOT NULL ");
            stb.append("                    AND QUALIFIED_CD = '" + param._sikakuCd + "' ");
            stb.append("                GROUP BY ");
            stb.append("                    SCHREGNO ");
            stb.append("               ) GET_D ON REGD.SCHREGNO = GET_D.SCHREGNO ");
            stb.append("     LEFT JOIN QUALIFIED_TEST_MST TS_M2 ON TEST.YEAR          = TS_M2.YEAR ");
            stb.append("                                       AND TS_M2.QUALIFIED_CD = '" + param._sikakuCd + "' ");
            stb.append("                                       AND GET_D.RESULT_CD    = TS_M2.TEST_CD ");
            stb.append("     LEFT JOIN QUALIFIED_MST QU_M ON TEST.QUALIFIED_CD = QU_M.QUALIFIED_CD ");
            stb.append("     LEFT JOIN V_NAME_MST Z002 ON TEST.YEAR    = Z002.YEAR ");
            stb.append("                              AND Z002.NAMECD1 = 'Z002' ");
            stb.append("                              AND BASE.SEX     = Z002.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("         TEST.YEAR         = '" + param._year + "' ");
            stb.append("     AND TEST.QUALIFIED_CD = '" + param._sikakuCd + "' ");
            stb.append("     AND TEST.TEST_DATE BETWEEN '" + param._fjDate + "' AND '" + param._tjDate + "' ");
            if ("1".equals(param._taisyou)) {
                stb.append("     AND HDAT.GRADE || HDAT.HR_CLASS IN " + param._categorySelectedIn + " ");
            } else {
                stb.append("     AND TEST.SCHREGNO IN " + param._categorySelectedIn + " ");
            }
            stb.append(" ORDER BY ");
            stb.append("     HDAT.GRADE || HDAT.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     TEST.TEST_DATE ");

            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 57847 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _cmd;
        private final boolean _isCsv;
        private final String _year;
        private final String _semester;
        private final String _ctrlDate;
        private final String _sikakuCd;
        private final String _sikakuName;
        private final String _taisyou;
        private final String _fjDate;
        private final String _tjDate;
        private final String _categorySelectedIn;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _cmd          = request.getParameter("cmd");
            _isCsv        = "csv".equals(_cmd);
            _year         = request.getParameter("YEAR");
            _semester     = "9".equals(request.getParameter("SEMESTER")) ? request.getParameter("CTRL_SEMESTER"): request.getParameter("SEMESTER");
            _ctrlDate     = request.getParameter("LOGIN_DATE");
            _sikakuCd     = request.getParameter("SIKAKUCD");
            _sikakuName   = getSikakuName(db2, "QUALIFIED_NAME");
            _taisyou      = request.getParameter("TAISYOU");
            _fjDate       = KNJ_EditDate.H_Format_Haifun(request.getParameter("FJ_DATE"));
            _tjDate       = KNJ_EditDate.H_Format_Haifun(request.getParameter("TJ_DATE"));
            final String[] categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _categorySelectedIn = getCategorySelectedIn(categorySelected);
        }

        private String getCategorySelectedIn(final String[] categorySelected) {
            StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int i = 0; i < categorySelected.length; i++) {
                if (0 < i) stb.append(",");
                stb.append("'" + categorySelected[i] + "'");
            }
            stb.append(")");
            return stb.toString();
        }

        private String getSikakuName(final DB2UDB db2, final String field) {
            String sikakuName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM QUALIFIED_MST WHERE QUALIFIED_CD = '" + _sikakuCd + "' ");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString(field)) {
                  sikakuName = rs.getString(field);
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return sikakuName;
        }
    }
}

// eof
