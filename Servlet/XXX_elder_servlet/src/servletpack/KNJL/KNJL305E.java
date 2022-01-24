/*
 * $Id: b5057c8eae6d30fc204dcb7d08c3501eeffa7152 $
 *
 * 作成日: 2018/11/12
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
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

public class KNJL305E {

    private static final Log log = LogFactory.getLog(KNJL305E.class);

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
        svf.VrSetForm("KNJL305E.frm", 1);
        final List printList = getList(db2);
        final int maxLine = 40;
        int printLine = 1;
//        int recordcnt = 1;

        setTitle(db2, svf);//ヘッダ
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            if (printLine > maxLine) {
                svf.VrEndPage();
                setTitle(db2, svf);//ヘッダ
                printLine = 1;
            }

            //データ
            //受験番号
            svf.VrsOutn("EXAM_NO" , printLine, StringUtils.defaultString(printData._examNo));
            //氏名
            svf.VrsOutn("NAME1" , printLine, StringUtils.defaultString(printData._name));
            //中学校
            svf.VrsOutn("FINSCHOOL_NAME" , printLine, StringUtils.defaultString(printData._finSchool_Name_Abbv));
            //受験コース
            svf.VrsOutn("DESIREDIV" , printLine, StringUtils.defaultString(printData._desiredivname) + (StringUtils.isBlank(printData._heiganAbbv) ? "" : "(" + printData._heiganAbbv + ")"));
            //受験区分
            svf.VrsOutn("DIV" , printLine, StringUtils.defaultString(printData._tDivName));
            //評定値
            svf.VrsOutn("VAL" , printLine, StringUtils.defaultString(printData._avgAll));
            //○の数
            svf.VrsOutn("NUM" , printLine, StringUtils.defaultString(printData._maruCnt));
            //面接
            svf.VrsOutn("INTERVIEW_A", printLine, StringUtils.defaultString(printData._interview1));
            svf.VrsOutn("INTERVIEW_B", printLine, StringUtils.defaultString(printData._interview2));
            //成績1
            svf.VrsOutn("SCORE1", printLine, StringUtils.defaultString(printData._score1));
            //成績2
            svf.VrsOutn("SCORE2", printLine, StringUtils.defaultString(printData._score2));
            //成績3
            svf.VrsOutn("SCORE3", printLine, StringUtils.defaultString(printData._score3));
            //合計
            svf.VrsOutn("TOTAL4", printLine, StringUtils.defaultString(printData._total4));
            //順位
            svf.VrsOutn("RANK4", printLine, StringUtils.defaultString(printData._total_Rank4));

            printLine++;
//            recordcnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
        String setYear = KNJ_EditDate.getAutoFormatYearNen(db2, _param._entExamYear + "/04/01");
        final String printDateTime = KNJ_EditDate.getAutoFormatDate(db2, _param._loginDate);// + "　" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
        svf.VrsOut("DATE", printDateTime);
        svf.VrsOut("TITLE", setYear + "度　" +  _param._testdivName + "　成績一覧");
        if (_param._subclsNameList.size() > 0) {
            svf.VrsOut("SUBCLASS_NAME1", StringUtils.defaultString((String)_param._subclsNameList.get(0)));
        }
        if (_param._subclsNameList.size() > 1) {
            svf.VrsOut("SUBCLASS_NAME2", StringUtils.defaultString((String)_param._subclsNameList.get(1)));
        }
        if (_param._subclsNameList.size() > 2) {
            svf.VrsOut("SUBCLASS_NAME3", StringUtils.defaultString((String)_param._subclsNameList.get(2)));
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String judgement = rs.getString("JUDGEMENT");
                final String judge = rs.getString("JUDGE");
                final String examNo = rs.getString("EXAMNO");
                final String examHallCd = rs.getString("EXAMHALLCD");
                final String examHallName = rs.getString("EXAMHALL_NAME");
                final String name = rs.getString("NAME");
                final String fs_Cd = rs.getString("FS_CD");
                final String finSchool_Name_Abbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String testDiv = rs.getString("TESTDIV");
                final String tDivName = rs.getString("TDIVNAME");
                final String heiganAbbv = rs.getString("HEIGAN_ABBV");
                final String desirediv = rs.getString("DESIREDIV");
                final String desiredivname = rs.getString("DESIREDIVNAME");
                //final String avgAll = rs.getString("AVERAGE_ALL");
                String avgAll = "";
                if (rs.getString("AVERAGE_ALL") != null && 0.0 != rs.getDouble("AVERAGE_ALL")) {
                    avgAll = new DecimalFormat("0.0").format(rs.getBigDecimal("AVERAGE_ALL"));
                }
                final String maruCnt = rs.getString("MARU_CNT");
                final String interview1 = rs.getString("INTERVIEW1");
                final String interview2 = rs.getString("INTERVIEW2");
                final String score1 = rs.getString("SCORE1");
                final String score2 = rs.getString("SCORE2");
                final String score3 = rs.getString("SCORE3");
                final String total2 = rs.getString("TOTAL2");
                final String total_Rank2 = rs.getString("TOTAL_RANK2");
                final String total4 = rs.getString("TOTAL4");
                final String total_Rank4 = rs.getString("TOTAL_RANK4");

                final PrintData printData = new PrintData(judgement, judge, examNo, examHallCd, examHallName, name, fs_Cd, finSchool_Name_Abbv, testDiv, tDivName, desirediv, desiredivname, heiganAbbv, avgAll, maruCnt, interview1, interview2, score1, score2, score3, total2, total_Rank2, total4, total_Rank4);
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

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SUM_SUBCLSSCORE AS ( ");
        stb.append(" SELECT ");
        stb.append("  T1.ENTEXAMYEAR, ");
        stb.append("  T1.APPLICANTDIV, ");
        stb.append("  T1.EXAMNO, ");
        stb.append("  VALUE(INT(S001.REMARK10), 0) AS RPT01, ");
        stb.append("  VALUE(INT(S002.REMARK10), 0) AS RPT02, ");
        stb.append("  VALUE(INT(S003.TOTAL5), 0) AS RPT03 ");
        stb.append(" FROM ");
        stb.append(" ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append(" LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT S001 ");
        stb.append("   ON S001.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("  AND S001.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("  AND S001.EXAMNO = T1.EXAMNO ");
        stb.append("  AND S001.SEQ = '001' ");
        stb.append(" LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT S002 ");
        stb.append("   ON S002.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("  AND S002.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("  AND S002.EXAMNO = T1.EXAMNO ");
        stb.append("  AND S002.SEQ = '002' ");
        stb.append(" LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT S003 ");
        stb.append("   ON S003.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("  AND S003.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("  AND S003.EXAMNO = T1.EXAMNO ");
        stb.append(" ), AVERAGE_TOTAL5 AS ( ");
        stb.append(" SELECT ");
        stb.append("   S_DAT.ENTEXAMYEAR, ");
        stb.append("   S_DAT.APPLICANTDIV, ");
        stb.append("   S_DAT.EXAMNO, ");
        stb.append("   R003.REMARK1 AS MARU_CNT, ");
        stb.append("   ( (S_DAT.RPT01 + S_DAT.RPT02 + S_DAT.RPT03) / (3.0 * 5.0) ) AS AVERAGE5");
        stb.append(" FROM ");
        stb.append("   SUM_SUBCLSSCORE S_DAT ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT R003 ");
        stb.append("   ON R003.ENTEXAMYEAR = S_DAT.ENTEXAMYEAR ");
        stb.append("   AND R003.APPLICANTDIV = S_DAT.APPLICANTDIV ");
        stb.append("   AND R003.EXAMNO = S_DAT.EXAMNO ");
        stb.append("   AND R003.SEQ = '003' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.JUDGEMENT, ");
        stb.append("   L013.NAME1 AS JUDGE, ");
        stb.append("   T1.EXAMNO, ");
        stb.append("   H1.EXAMHALLCD, ");
        stb.append("   H2.EXAMHALL_NAME, ");
        stb.append("   T1.NAME, ");
        stb.append("   T1.FS_CD, ");
        stb.append("   FM1.FINSCHOOL_NAME_ABBV, ");
        stb.append("   T1.TESTDIV, ");
        stb.append("   L045.NAME1 AS TDIVNAME, ");
        stb.append("   CASE WHEN VALUE(L004_2.NAMESPARE3, '') = '2' THEN L061.ABBV1 END AS HEIGAN_ABBV, "); // 音楽家の併願略称
        stb.append("   T1.DESIREDIV, ");
        stb.append("   L058.NAME1 AS DESIREDIVNAME, ");
        stb.append("   L027_1.NAME1 AS INTERVIEW1, ");
        stb.append("   L027_2.NAME1 AS INTERVIEW2, ");
        //stb.append("   CASE WHEN ATOTAL.AVERAGE5 = 0.0 THEN '' ELSE CHAR(ROUND(ATOTAL.AVERAGE5, 1)) END AS AVERAGE_ALL, ");
        stb.append("   ROUND(ATOTAL.AVERAGE5, 1) AS AVERAGE_ALL, ");
        stb.append("   ATOTAL.MARU_CNT, ");
        stb.append("   S1_1.SCORE AS SCORE1, ");
        stb.append("   S1_2.SCORE AS SCORE2, ");
        stb.append("   S1_3.SCORE AS SCORE3, ");
        stb.append("   S2.TOTAL2, ");
        stb.append("   S2.TOTAL_RANK2, ");
        stb.append("   S2.TOTAL4, ");
        stb.append("   S2.TOTAL_RANK4 ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("   LEFT JOIN ENTEXAM_HALL_GROUP_DAT H1 ");
        stb.append("      ON H1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("     AND H1.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("     AND H1.TESTDIV = T1.TESTDIV ");
        stb.append("     AND H1.EXAMNO = T1.EXAMNO ");
        stb.append("     AND H1.EXAMHALL_TYPE = '2' ");  //2(固定で指定)
        stb.append("    LEFT JOIN ENTEXAM_HALL_YDAT H2 ");
        stb.append("      ON H2.ENTEXAMYEAR = H1.ENTEXAMYEAR ");
        stb.append("     AND H2.APPLICANTDIV = H1.APPLICANTDIV ");
        stb.append("     AND H2.TESTDIV = H1.TESTDIV ");
        stb.append("     AND H2.EXAM_TYPE = H1.EXAMHALL_TYPE ");
        stb.append("     AND H2.EXAMHALLCD = H1.EXAMHALLCD ");
        stb.append("   LEFT JOIN ENTEXAM_RECEPT_DAT S2 ");
        stb.append("     ON S2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND S2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND S2.TESTDIV = T1.TESTDIV ");
        stb.append("    AND S2.EXAMNO = T1.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT S1_1 ");
        stb.append("     ON S1_1.ENTEXAMYEAR = S2.ENTEXAMYEAR ");
        stb.append("    AND S1_1.APPLICANTDIV = S2.APPLICANTDIV ");
        stb.append("    AND S1_1.TESTDIV = S2.TESTDIV ");
        stb.append("    AND S1_1.EXAM_TYPE = S2.EXAM_TYPE ");
        stb.append("    AND S1_1.RECEPTNO = S2.RECEPTNO ");
        stb.append("    AND S1_1.TESTSUBCLASSCD = '1' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT S1_2 ");
        stb.append("     ON S1_2.ENTEXAMYEAR = S2.ENTEXAMYEAR ");
        stb.append("    AND S1_2.APPLICANTDIV = S2.APPLICANTDIV ");
        stb.append("    AND S1_2.TESTDIV = S2.TESTDIV ");
        stb.append("    AND S1_2.EXAM_TYPE = S2.EXAM_TYPE ");
        stb.append("    AND S1_2.RECEPTNO = S2.RECEPTNO ");
        stb.append("    AND S1_2.TESTSUBCLASSCD = '2' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT S1_3 ");
        stb.append("     ON S1_3.ENTEXAMYEAR = S2.ENTEXAMYEAR ");
        stb.append("    AND S1_3.APPLICANTDIV = S2.APPLICANTDIV ");
        stb.append("    AND S1_3.TESTDIV = S2.TESTDIV ");
        stb.append("    AND S1_3.EXAM_TYPE = S2.EXAM_TYPE ");
        stb.append("    AND S1_3.RECEPTNO = S2.RECEPTNO ");
        stb.append("    AND S1_3.TESTSUBCLASSCD = '3' ");
        stb.append("   LEFT JOIN ENTEXAM_INTERVIEW_DAT M1 ");
        stb.append("     ON M1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND M1.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND M1.TESTDIV = T1.TESTDIV ");
        stb.append("    AND M1.EXAMNO = T1.EXAMNO ");
        stb.append("   LEFT JOIN FINSCHOOL_MST FM1 ");
        stb.append("     ON T1.FS_CD = FM1.FINSCHOOLCD ");
        stb.append("   LEFT JOIN NAME_MST L013 ");
        stb.append("     ON L013.NAMECD1 = 'L013' ");
        stb.append("    AND L013.NAMECD2 = T1.JUDGEMENT ");
        stb.append("   LEFT JOIN NAME_MST L045 ");
        stb.append("     ON L045.NAMECD1 = 'L045' ");
        stb.append("    AND L045.NAMECD2 = T1.TESTDIV1 ");
        stb.append("   LEFT JOIN NAME_MST L027_1 ");
        stb.append("     ON L027_1.NAMECD1 = 'L027' ");
        stb.append("    AND L027_1.NAMECD2 = M1.INTERVIEW_A ");
        stb.append("   LEFT JOIN NAME_MST L027_2 ");
        stb.append("     ON L027_2.NAMECD1 = 'L027' ");
        stb.append("    AND L027_2.NAMECD2 = M1.INTERVIEW_B ");
        stb.append("   LEFT JOIN AVERAGE_TOTAL5 ATOTAL ");
        stb.append("     ON ATOTAL.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND ATOTAL.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND ATOTAL.EXAMNO = T1.EXAMNO ");
        stb.append("   LEFT JOIN NAME_MST L058 ");
        stb.append("     ON L058.NAMECD1 = 'L058' ");
        stb.append("    AND L058.NAMECD2 = T1.DESIREDIV ");
        stb.append("   INNER JOIN V_NAME_MST L004 ON L004.YEAR       = T1.ENTEXAMYEAR ");
        stb.append("                              AND L004.NAMECD1    = 'L004' ");
        stb.append("                              AND L004.NAMECD2    = T1.TESTDIV ");
        stb.append("                              AND L004.NAMESPARE1 = '" + _param._testDiv + "' ");
        stb.append("   LEFT JOIN V_NAME_MST L004_2 ");
        stb.append("     ON L004_2.YEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND L004_2.NAMECD1 = 'L004' ");
        stb.append("    AND L004_2.NAMECD2 = T1.TESTDIV ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD33 ");
        stb.append("     ON BD33.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND BD33.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND BD33.EXAMNO = T1.EXAMNO ");
        stb.append("    AND BD33.SEQ = '033' ");
        stb.append("   LEFT JOIN V_NAME_MST L061 ");
        stb.append("     ON L061.YEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND L061.NAMECD1 = 'L061' ");
        stb.append("    AND L061.NAMECD2 = BD33.REMARK3 ");
        stb.append(" WHERE ");
        stb.append("   T1.ENTEXAMYEAR = '" + _param._entExamYear + "'   AND ");
        stb.append("   T1.APPLICANTDIV = '" + _param._applicantDiv + "'   AND ");
        stb.append("   NOT (VALUE(L004.NAMESPARE3, '') = '2' AND VALUE(L061.NAMESPARE1, '') <> '1') "); // 音楽家の単願（2科目合計のみ）を除く
        stb.append(" ORDER BY ");
        if ("2".equals(_param._outputOrder)) {
            stb.append("   CASE WHEN S2.TOTAL_RANK4 IS NULL THEN 999999 ELSE S2.TOTAL_RANK4 END, ");
        }
        stb.append("   T1.EXAMNO ");

        return stb.toString();
    }

    private class PrintData {
        final String _judgement;
        final String _judge;
        final String _examNo;
        final String _examHallCd;
        final String _examHallName;
        final String _name;
        final String _fs_Cd;
        final String _finSchool_Name_Abbv;
        final String _testDiv;
        final String _tDivName;
        final String _desirediv;
        final String _desiredivname;
        final String _heiganAbbv;
        final String _avgAll;
        final String _maruCnt;
        final String _interview1;
        final String _interview2;
        final String _score1;
        final String _score2;
        final String _score3;
        final String _total2;
        final String _total_Rank2;
        final String _total4;
        final String _total_Rank4;

        public PrintData(
                final String judgement,
                final String judge,
                final String examNo,
                final String examHallCd,
                final String examHallName,
                final String name,
                final String fs_Cd,
                final String finSchool_Name_Abbv,
                final String testDiv,
                final String tDivName,
                final String desirediv,
                final String desiredivname,
                final String heiganAbbv,
                final String avgAll,
                final String maruCnt,
                final String interview1,
                final String interview2,
                final String score1,
                final String score2,
                final String score3,
                final String total2,
                final String total_Rank2,
                final String total4,
                final String total_Rank4
        ) {
            _judgement = judgement;
            _judge = judge;
            _examNo = examNo;
            _examHallCd = examHallCd;
            _examHallName = examHallName;
            _name = name;
            _fs_Cd = fs_Cd;
            _finSchool_Name_Abbv = finSchool_Name_Abbv;
            _testDiv = testDiv;
            _tDivName = tDivName;
            _desirediv = desirediv;
            _desiredivname = desiredivname;
            _heiganAbbv = heiganAbbv;
            _avgAll = avgAll;
            _maruCnt = maruCnt;
            _interview1 = interview1;
            _interview2 = interview2;
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;
            _total2 = total2;
            _total_Rank2 = total_Rank2;
            _total4 = total4;
            _total_Rank4 = total_Rank4;
          }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65509 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginDate;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _entExamYear;
        private final String _testdivName;
        private final String _outputOrder;
        private final List _subclsNameList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginDate     = request.getParameter("LOGIN_DATE");
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _entExamYear   = request.getParameter("ENTEXAMYEAR");
            _applicantDiv  = request.getParameter("APPLICANTDIV");
            _testDiv       = request.getParameter("TESTDIV");
            _testdivName = "2".equals(_testDiv) ? "Ａ日程": "Ｂ日程";
            _outputOrder =  request.getParameter("OUTPUT_ORDER");
            _subclsNameList = getNameMstList(db2, "NAME1", "L009");
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                if ("".equals(namecd2)) {
                    ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' ");
                } else {
                    ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                }
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private List getNameMstList(final DB2UDB db2, final String field, final String namecd1) {
            List rtnList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' ORDER BY NAMECD2");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnList.add(rs.getString(field));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtnList;
        }
    }
}

// eof
