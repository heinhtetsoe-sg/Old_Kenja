/*
 * $Id: f1c5da429ff005771c7b717246b629a1a97282db $
 *
 * 作成日: 2016/11/03
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL333F {

    private static final Log log = LogFactory.getLog(KNJL333F.class);

    private static final String SCHOOL_J = "1";
    private static final String SCHOOL_H = "2";
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List printList = getList(db2);
        if (SCHOOL_J.equals(_param._applicantdiv)) {
            printOutJ(db2, svf, printList);
        } else {
            printOutH(db2, svf, printList);
        }
    }

    private void printOutJ(final DB2UDB db2, final Vrw32alp svf, final List printList) {
        svf.VrSetForm("KNJL333F_J1.frm", 1);
        setTitle(db2, svf);
        int renBan = 1;
        int lineCnt = 1;
        for (int line = 0; line < printList.size(); line++) {
            final PrintData printData = (PrintData) printList.get(line);
            if (lineCnt > 50) {
                svf.VrEndPage();
                setTitle(db2, svf);
                lineCnt = 1;
            }
            svf.VrsOutn("NO", lineCnt, String.valueOf(renBan++));
            svf.VrsOutn("EXAM_NO", lineCnt, printData._receptno);
            svf.VrsOutn("NAME", lineCnt, printData._name);
            svf.VrsOutn("KANA", lineCnt, printData._nameKana);
            svf.VrsOutn("BIRTHDAY", lineCnt, KNJ_EditDate.h_format_JP(db2, printData._birthday));
            svf.VrsOutn("SCHOOL_NAME", lineCnt, printData._finschoolName);
            if ("1".equals(_param._print)) {
                svf.VrsOutn("OTHER_MONEY", lineCnt, printData._expPayMoney);
                svf.VrsOutn("OTHER_MONEY_DATE", lineCnt, KNJ_EditDate.h_format_JP(db2, printData._expPayDate));
            }
            svf.VrsOutn("OTHER_PROCEDURE", lineCnt, printData._entName);
            svf.VrsOutn("REMARK", lineCnt, printData._remark);
            svf.VrsOutn("ANOTHER_EXAM_NO1", lineCnt, printData._tyoufuku1);
            svf.VrsOutn("ANOTHER_EXAM_NO2", lineCnt, printData._tyoufuku2);
            svf.VrsOutn("ANOTHER_EXAM_NO3", lineCnt, printData._tyoufuku3);
            svf.VrsOutn("ANOTHER_EXAM_NO4", lineCnt, printData._tyoufuku4);
            svf.VrsOutn("ANOTHER_EXAM_NO5", lineCnt, printData._tyoufuku5);
            svf.VrsOutn("ANOTHER_EXAM_NO6", lineCnt, printData._tyoufuku17);

            //思考力1～3、IEE1～3
            svf.VrsOutn("ANOTHER_EXAM_NO9", lineCnt, printData._tyoufuku9);
            svf.VrsOutn("ANOTHER_EXAM_NO10", lineCnt, printData._tyoufuku10);
            svf.VrsOutn("ANOTHER_EXAM_NO11", lineCnt, printData._tyoufuku11);
            svf.VrsOutn("ANOTHER_EXAM_NO12", lineCnt, printData._tyoufuku12);
            svf.VrsOutn("ANOTHER_EXAM_NO13", lineCnt, printData._tyoufuku13);
            svf.VrsOutn("ANOTHER_EXAM_NO14", lineCnt, printData._tyoufuku14);
            lineCnt++;
            _hasData = true;
        }
        if (lineCnt > 1) {
            svf.VrEndPage();
        }
    }

    private void printOutH(final DB2UDB db2, final Vrw32alp svf, final List printList) {
        svf.VrSetForm("KNJL333F_H1.frm", 4);
        setTitle(db2, svf);
        int renBan = 1;
        for (int line = 0; line < printList.size(); line++) {
            final PrintData printData = (PrintData) printList.get(line);
            svf.VrsOut("NO", String.valueOf(renBan++));
            svf.VrsOut("EXAM_NO", printData._receptno);
            svf.VrsOut("NAME", printData._name);
            svf.VrsOut("CITY_NAME", printData._fincity);
            svf.VrsOut("SCHOOL_DUV", printData._finseturitu);
            svf.VrsOut("SCHOOL_NAME", printData._finschoolName);
            if ("1".equals(_param._print)) {
                svf.VrsOut("OTHER_MONEY", printData._expPayMoney);
                svf.VrsOut("OTHER_MONEY_DATE", KNJ_EditDate.h_format_JP(db2, printData._expPayDate));
            }
            svf.VrsOut("OTHER_PROCEDURE", printData._entName);
            svf.VrsOut("REMARK", printData._remark);
            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度　" + _param._applicantdivName + "入試　" + _param._testdivName1 + "　入学者一覧");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate));
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String receptno = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String birthday = rs.getString("BIRTHDAY");
                final String fsCd = rs.getString("FS_CD");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String fincity = rs.getString("FINCITY");
                final String finseturitu = rs.getString("FINSETURITU");
                final String expPayMoney = rs.getString("EXP_PAY_MONEY");
                final String expPayDate = rs.getString("EXP_PAY_DATE");
                final String entName = rs.getString("ENTNAME");
                final String tyoufuku1 = rs.getString("TYOUFUKU1");
                final String tyoufuku2 = rs.getString("TYOUFUKU2");
                final String tyoufuku3 = rs.getString("TYOUFUKU3");
                final String tyoufuku4 = rs.getString("TYOUFUKU4");
                final String tyoufuku5 = rs.getString("TYOUFUKU5");
                final String tyoufuku6 = rs.getString("TYOUFUKU6");
                final String tyoufuku7 = rs.getString("TYOUFUKU7");
                final String tyoufuku9 = rs.getString("TYOUFUKU9");
                final String tyoufuku10 = rs.getString("TYOUFUKU10");
                final String tyoufuku11 = rs.getString("TYOUFUKU11");
                final String tyoufuku12 = rs.getString("TYOUFUKU12");
                final String tyoufuku13 = rs.getString("TYOUFUKU13");
                final String tyoufuku14 = rs.getString("TYOUFUKU14");
                final String tyoufuku17 = rs.getString("TYOUFUKU17");
                final String tyoufukuDiv1 = rs.getString("TYOUFUKU_DIV1");
                final String tyoufukuDiv2 = rs.getString("TYOUFUKU_DIV2");
                final String tyoufukuDiv3 = rs.getString("TYOUFUKU_DIV3");
                final String tyoufukuDiv4 = rs.getString("TYOUFUKU_DIV4");
                final String tyoufukuDiv5 = rs.getString("TYOUFUKU_DIV5");
                final String tyoufukuDiv6 = rs.getString("TYOUFUKU_DIV6");
                final String tyoufukuDiv7 = rs.getString("TYOUFUKU_DIV7");
                final String tyoufukuDiv17 = rs.getString("TYOUFUKU_DIV17");
                final String remark = rs.getString("REMARK");
                final PrintData printData = new PrintData(receptno, name, nameKana, birthday, fsCd, finschoolName, fincity, finseturitu, expPayMoney, expPayDate, entName, tyoufuku1, tyoufuku2, tyoufuku3, tyoufuku4, tyoufuku5, tyoufuku6, tyoufuku7, tyoufuku9, tyoufuku10, tyoufuku11, tyoufuku12, tyoufuku13, tyoufuku14, tyoufuku17, tyoufukuDiv1, tyoufukuDiv2, tyoufukuDiv3, tyoufukuDiv4, tyoufukuDiv5, tyoufukuDiv6, tyoufukuDiv7, tyoufukuDiv17, remark);
                retList.add(printData);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RECEPT.RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     BASE.BIRTHDAY, ");
        stb.append("     BASE.FS_CD, ");
        stb.append("     FSCHOOL.FINSCHOOL_NAME, ");
        stb.append("     L001.NAME1 AS FINCITY, ");
        stb.append("     L015.NAME1 AS FINSETURITU, ");
        stb.append("     EMONEY.EXP_PAY_MONEY, ");
        stb.append("     EMONEY.EXP_PAY_DATE, ");
        stb.append("     L012.NAME1 AS ENTNAME, ");
        stb.append("     BDETAIL012.REMARK1 AS TYOUFUKU1, ");
        if (SCHOOL_J.equals(_param._applicantdiv)) {
            stb.append("     BDETAIL012.REMARK16 AS TYOUFUKU2, ");
            stb.append("     BDETAIL012.REMARK2 AS TYOUFUKU3, ");
            stb.append("     BDETAIL012.REMARK3 AS TYOUFUKU4, ");
            stb.append("     BDETAIL012.REMARK17 AS TYOUFUKU17, ");
        } else {
            stb.append("     BDETAIL012.REMARK2 AS TYOUFUKU2, ");
            stb.append("     BDETAIL012.REMARK3 AS TYOUFUKU3, ");
            stb.append("     BDETAIL012.REMARK4 AS TYOUFUKU4, ");
            stb.append("     '' AS TYOUFUKU17, ");
        }
        stb.append("     BDETAIL012.REMARK5 AS TYOUFUKU5, ");
        stb.append("     BDETAIL012.REMARK6 AS TYOUFUKU6, ");
        stb.append("     BDETAIL012.REMARK7 AS TYOUFUKU7, ");
        stb.append("     BDETAIL012.REMARK9 AS TYOUFUKU9, ");
        stb.append("     BDETAIL012.REMARK10 AS TYOUFUKU10, ");
        stb.append("     BDETAIL012.REMARK11 AS TYOUFUKU11, ");
        stb.append("     BDETAIL012.REMARK12 AS TYOUFUKU12, ");
        stb.append("     BDETAIL012.REMARK13 AS TYOUFUKU13, ");
        stb.append("     BDETAIL012.REMARK14 AS TYOUFUKU14, ");
        stb.append("     BDETAIL010.REMARK1 AS TYOUFUKU_DIV1, ");
        if (SCHOOL_J.equals(_param._applicantdiv)) {
            stb.append("     BDETAIL010.REMARK16 AS TYOUFUKU_DIV2, ");
            stb.append("     BDETAIL010.REMARK2 AS TYOUFUKU_DIV3, ");
            stb.append("     BDETAIL010.REMARK3 AS TYOUFUKU_DIV4, ");
            stb.append("     BDETAIL010.REMARK17 AS TYOUFUKU_DIV17, ");
        } else {
            stb.append("     BDETAIL010.REMARK2 AS TYOUFUKU_DIV2, ");
            stb.append("     BDETAIL010.REMARK3 AS TYOUFUKU_DIV3, ");
            stb.append("     BDETAIL010.REMARK4 AS TYOUFUKU_DIV4, ");
            stb.append("     '' AS TYOUFUKU_DIV17, ");
        }
        stb.append("     BDETAIL010.REMARK5 AS TYOUFUKU_DIV5, ");
        stb.append("     BDETAIL010.REMARK6 AS TYOUFUKU_DIV6, ");
        stb.append("     BDETAIL010.REMARK7 AS TYOUFUKU_DIV7, ");
        if ("1".equals(_param._applicantdiv)) {
            stb.append("     L025.NAME1 AS REMARK ");
        } else {
            stb.append("     L025.NAME2 AS REMARK ");
        }
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND RECEPT.EXAMNO = BASE.EXAMNO ");
        stb.append("           AND BASE.PROCEDUREDIV = '1' ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FSCHOOL ON BASE.FS_CD = FSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST L001 ON L001.NAMECD1 = 'L001' ");
        stb.append("          AND FSCHOOL.FINSCHOOL_DISTCD = L001.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L015 ON L015.NAMECD1 = 'L015' ");
        stb.append("          AND FSCHOOL.FINSCHOOL_DIV = L015.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_MONEY_DAT EMONEY ON RECEPT.ENTEXAMYEAR = EMONEY.ENTEXAMYEAR ");
        stb.append("          AND RECEPT.APPLICANTDIV = EMONEY.APPLICANTDIV ");
        stb.append("          AND RECEPT.EXAMNO = EMONEY.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST L011 ON L011.NAMECD1 = 'L011' ");
        stb.append("          AND BASE.PROCEDUREDIV = L011.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L012 ON L012.NAMECD1 = 'L012' ");
        stb.append("          AND BASE.ENTDIV = L012.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BDETAIL012 ON RECEPT.ENTEXAMYEAR = BDETAIL012.ENTEXAMYEAR ");
        stb.append("          AND RECEPT.EXAMNO = BDETAIL012.EXAMNO ");
        stb.append("          AND BDETAIL012.SEQ = '012' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BDETAIL010 ON RECEPT.ENTEXAMYEAR = BDETAIL010.ENTEXAMYEAR ");
        stb.append("          AND RECEPT.EXAMNO = BDETAIL010.EXAMNO ");
        stb.append("          AND BDETAIL010.SEQ = '010' ");
        stb.append("     LEFT JOIN NAME_MST L025 ON L025.NAMECD1 = 'L025' ");
        stb.append("          AND BASE.JUDGE_KIND = L025.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT TRDET003 ON TRDET003.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("         AND TRDET003.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("         AND TRDET003.TESTDIV = RECEPT.TESTDIV ");
        stb.append("         AND TRDET003.EXAM_TYPE = RECEPT.EXAM_TYPE ");
        stb.append("         AND TRDET003.RECEPTNO = RECEPT.RECEPTNO ");
        stb.append("         AND TRDET003.SEQ = '003' ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        if ("1".equals(_param._applicantdiv)) {
            if ("5".equals(_param._testdiv)) {
                stb.append("     AND VALUE(BASE.GENERAL_FLG, '') <> '1' ");
            }
        } else if ("2".equals(_param._applicantdiv)) {
            stb.append("     AND TRDET003.REMARK1 = '" + _param._testdiv0 + "' ");
            if ("3".equals(_param._testdiv)) {
                stb.append("     AND VALUE(BASE.GENERAL_FLG, '') <> '1' ");
            }
        }
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testdiv + "' ");
        stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("     AND RECEPT.JUDGEDIV = '1' ");
        stb.append(" ORDER BY ");
        stb.append("     RECEPT.RECEPTNO ");
        return stb.toString();
    }

    private class PrintData {
        final String _receptno;
        final String _name;
        final String _nameKana;
        final String _birthday;
        final String _fsCd;
        final String _finschoolName;
        final String _fincity;
        final String _finseturitu;
        final String _expPayMoney;
        final String _expPayDate;
        final String _entName;
        final String _tyoufuku1;
        final String _tyoufuku2;
        final String _tyoufuku3;
        final String _tyoufuku4;
        final String _tyoufuku5;
        final String _tyoufuku6;
        final String _tyoufuku7;
        final String _tyoufuku9;
        final String _tyoufuku10;
        final String _tyoufuku11;
        final String _tyoufuku12;
        final String _tyoufuku13;
        final String _tyoufuku14;
        final String _tyoufuku17;
        final String _tyoufukuDiv1;
        final String _tyoufukuDiv2;
        final String _tyoufukuDiv3;
        final String _tyoufukuDiv4;
        final String _tyoufukuDiv5;
        final String _tyoufukuDiv6;
        final String _tyoufukuDiv7;
        final String _tyoufukuDiv17;
        final String _remark;
        final List _tyoufukuList;

        public PrintData(
                final String receptno,
                final String name,
                final String nameKana,
                final String birthday,
                final String fsCd,
                final String finschoolName,
                final String fincity,
                final String finseturitu,
                final String expPayMoney,
                final String expPayDate,
                final String entName,
                final String tyoufuku1,
                final String tyoufuku2,
                final String tyoufuku3,
                final String tyoufuku4,
                final String tyoufuku5,
                final String tyoufuku6,
                final String tyoufuku7,
                final String tyoufuku9,
                final String tyoufuku10,
                final String tyoufuku11,
                final String tyoufuku12,
                final String tyoufuku13,
                final String tyoufuku14,
                final String tyoufuku17,
                final String tyoufukuDiv1,
                final String tyoufukuDiv2,
                final String tyoufukuDiv3,
                final String tyoufukuDiv4,
                final String tyoufukuDiv5,
                final String tyoufukuDiv6,
                final String tyoufukuDiv7,
                final String tyoufukuDiv17,
                final String remark
        ) {
            _receptno = receptno;
            _name = name;
            _nameKana = nameKana;
            _birthday = birthday;
            _fsCd = fsCd;
            _finschoolName = finschoolName;
            _fincity = fincity;
            _finseturitu = finseturitu;
            _expPayMoney = expPayMoney;
            _expPayDate = expPayDate;
            _entName = entName;
            _tyoufuku1 = tyoufuku1;
            _tyoufuku2 = tyoufuku2;
            _tyoufuku3 = tyoufuku3;
            _tyoufuku4 = tyoufuku4;
            _tyoufuku5 = tyoufuku5;
            _tyoufuku6 = tyoufuku6;
            _tyoufuku7 = tyoufuku7;
            _tyoufukuDiv1 = tyoufukuDiv1;
            _tyoufukuDiv2 = tyoufukuDiv2;
            _tyoufukuDiv3 = tyoufukuDiv3;
            _tyoufukuDiv4 = tyoufukuDiv4;
            _tyoufukuDiv5 = tyoufukuDiv5;
            _tyoufukuDiv6 = tyoufukuDiv6;
            _tyoufukuDiv7 = tyoufukuDiv7;
            _tyoufukuDiv17 = tyoufukuDiv17;
            _tyoufuku9 = tyoufuku9;
            _tyoufuku10 = tyoufuku10;
            _tyoufuku11 = tyoufuku11;
            _tyoufuku12 = tyoufuku12;
            _tyoufuku13 = tyoufuku13;
            _tyoufuku14 = tyoufuku14;
            _tyoufuku17 = tyoufuku17;
            _remark = remark;

            _tyoufukuList = getTyoufuku(new String[]{_tyoufuku1, _tyoufuku2, _tyoufuku3, _tyoufuku4, _tyoufuku5, _tyoufuku6, _tyoufuku7, _tyoufuku17}, new String[]{_tyoufukuDiv1, _tyoufukuDiv2, _tyoufukuDiv3, _tyoufukuDiv4, _tyoufukuDiv5, _tyoufukuDiv6, _tyoufukuDiv7, _tyoufukuDiv17});
        }

        private List getTyoufuku(final String[] tyoufuku, final String[] tyoufukuDiv) {
            final List retList = new ArrayList();
            int tyoufukuCnt = 0;
            String[] setStr = new String[4];
            retList.add(setStr);
            for (int i = 0; i < tyoufuku.length; i++) {
                final String checkTyoufuku = tyoufuku[i];
                if (null != checkTyoufuku && !"".equals(checkTyoufuku)) {
                    if (tyoufukuCnt > 3) {
                        setStr = new String[4];
                        retList.add(setStr);
                        tyoufukuCnt = 0;
                    }
                    setStr[tyoufukuCnt] = tyoufukuDiv[i];
                    tyoufukuCnt++;
                    setStr[tyoufukuCnt] = checkTyoufuku;
                    tyoufukuCnt++;
                }
            }
            return retList;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70821 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _applicantdiv;
        private final String _testdiv;
        private final String _testdiv0;
        private final String _print;
        private final String _entexamyear;
        private final String _loginYear;
        private final String _loginSemester;
        private final String _loginDate;
        private final String _printLogStaffcd;

        private final String _applicantdivName;
        private final String _testdivName1;
        private final String _testdivAbbv1;
        private final String _testdivAbbv2;
        private final String _testdivAbbv3;
        private final String _dateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantdiv       = request.getParameter("APPLICANTDIV");
            _testdiv            = request.getParameter("TESTDIV");
            _testdiv0           = request.getParameter("TESTDIV0");
            _print              = request.getParameter("PRINT");
            _entexamyear        = request.getParameter("ENTEXAMYEAR");
            _loginYear          = request.getParameter("LOGIN_YEAR");
            _loginSemester      = request.getParameter("LOGIN_SEMESTER");
            _loginDate          = request.getParameter("LOGIN_DATE");
            _printLogStaffcd    = request.getParameter("PRINT_LOG_STAFFCD");

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            final String testName = SCHOOL_J.equals(_applicantdiv) ? "L024" : "L004";
            _testdivName1 = StringUtils.defaultString(getNameMst(db2, "NAME1", testName, _testdiv));
            _testdivAbbv1 = StringUtils.defaultString(getNameMst(db2, "ABBV1", testName, _testdiv));
            _testdivAbbv2 = StringUtils.defaultString(getNameMst(db2, "ABBV2", testName, _testdiv));
            _testdivAbbv3 = StringUtils.defaultString(getNameMst(db2, "ABBV3", testName, _testdiv));
            _dateStr = getDateStr(_loginDate);
        }

        private String getDateStr(final String date) {
            if (null == date) {
                return null;
            }
            return KNJ_EditDate.h_format_JP(date);
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

    }
}

// eof

