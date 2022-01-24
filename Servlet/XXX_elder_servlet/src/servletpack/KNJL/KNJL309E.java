/*
 * $Id: 66b9afe0a81e477f5835ccdf1be1d2dab4fd420c $
 *
 * 作成日: 2019/01/24
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL309E {

    private static final Log log = LogFactory.getLog(KNJL309E.class);

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
        svf.VrSetForm("KNJL309E.frm", 1);

        setTitle(svf, 1);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final int maxLine = 25;
            int recordcnt = 1;
            int lineCnt = 1;
            int pageCnt = 1;
            String aftCd = "";
            String befCd = "";
            while (rs.next()) {
                aftCd = StringUtils.defaultString(rs.getString("EXAMHALLCD"));
                final boolean diffCd = _param._perHallCdPrintFlg && (!"".equals(befCd) && !befCd.equals(aftCd));
                if (lineCnt > maxLine || diffCd) {
                    svf.VrEndPage();
                    setTitle(svf, pageCnt + 1);
                    lineCnt = 1;
                    pageCnt++;
                    if (diffCd) {
                        recordcnt = 1;
                    }
                }

                if (_param._perHallCdPrintFlg && lineCnt == 1) {
                    //室名称
                    svf.VrsOut("ROOM_NAME", StringUtils.defaultString(rs.getString("EXAMHALL_NAME")));
                }

                //No
                svf.VrsOutn("NO", lineCnt, String.valueOf(recordcnt));
                //受験番号
                svf.VrsOutn("EXAM_NO", lineCnt, rs.getString("EXAMNO"));
                //室
                svf.VrsOutn("ROOM", lineCnt, StringUtils.defaultString(rs.getString("EXAMHALL_NAME")));
                //氏名
                svf.VrsOutn("NAME1", lineCnt, StringUtils.defaultString(rs.getString("NAME")));
                //ふりがな
                svf.VrsOutn("KANA", lineCnt, StringUtils.defaultString(rs.getString("NAME_KANA")));
                //生年月日
                svf.VrsOutn("BIRTHDAY", lineCnt, StringUtils.defaultString(StringUtils.replace(rs.getString("BIRTHDAY"), "-", "/")));
                //中学校
                svf.VrsOutn("FINSCHOOL_NAME", lineCnt, StringUtils.defaultString(rs.getString("FINSCHOOL_NAME")));
                //評定値
                if (rs.getString("DEVI") != null) {
                    String avg = new BigDecimal(rs.getString("DEVI")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    svf.VrsOutn("DEVI", lineCnt, avg);
                }
                //同窓生推薦
                svf.VrsOutn("REUNION", lineCnt, rs.getString("REUNION"));
                //受験コース
                svf.VrsOutn("COURSE" , lineCnt, StringUtils.defaultString(rs.getString("COURSE")) + (StringUtils.isBlank(rs.getString("HEIGAN_ABBV")) ? "" : "(" + rs.getString("HEIGAN_ABBV") + ")"));
                //受験区分
                svf.VrsOutn("DIV", lineCnt, StringUtils.defaultString(rs.getString("TDIVNAME")));
                //グループ
                svf.VrsOutn("GROUP", lineCnt, rs.getString("EXAMHALLGROUPCD") == null ? "" : String.valueOf(Integer.parseInt(StringUtils.defaultString(rs.getString("EXAMHALLGROUPCD"), "0"))));
                //面接点
                if (_param._testScorePrintFlg) {
                    svf.VrsOutn("INTERVIEW_A", lineCnt, StringUtils.defaultString(rs.getString("INTERVIEW1")));
                    svf.VrsOutn("INTERVIEW_B", lineCnt, StringUtils.defaultString(rs.getString("INTERVIEW2")));
                }
                //○の数
                svf.VrsOutn("NUM", lineCnt, StringUtils.defaultString(rs.getString("CIRCLECNT")));
                //3年次欠席
                svf.VrsOutn("ABSENCE", lineCnt, StringUtils.defaultString(rs.getString("ABSENCE_DAYS")));
                //成績1
                svf.VrsOutn("SCORE1", lineCnt, StringUtils.defaultString(rs.getString("SCORE1")));
                //成績2
                svf.VrsOutn("SCORE2", lineCnt, StringUtils.defaultString(rs.getString("SCORE2")));
                //成績3
                svf.VrsOutn("SCORE3", lineCnt, StringUtils.defaultString(rs.getString("SCORE3")));
                //合計
                svf.VrsOutn("TOTAL4", lineCnt, StringUtils.defaultString(rs.getString("TOTAL4")));
                //順位
                svf.VrsOutn("RANK4", lineCnt, StringUtils.defaultString(rs.getString("TOTAL_RANK4")));
                //備考
                if (rs.getString("REMARK") != null && !"".equals(rs.getString("REMARK"))) {
                    String[] remarkwk = KNJ_EditEdit.get_token(rs.getString("REMARK"), 40, 2);
                    svf.VrsOutn("REMARK1", lineCnt, StringUtils.defaultString(remarkwk[0]));
                    svf.VrsOutn("REMARK2", lineCnt, StringUtils.defaultString(remarkwk[1]));
                }

                befCd = aftCd;
                lineCnt++;
                recordcnt++;
                _hasData = true;
            }

            if (_hasData) {
                svf.VrEndPage();
            }
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void setTitle(final Vrw32alp svf, final int pagenum) {
        //タイトル
        svf.VrsOut("TITLE", _param._nendo + "　" + _param._testdivName + "　受験者一覧表");
        //作成日
        svf.VrsOut("DATE", _param._loginDateStr);
        //科目名1
        svf.VrsOut("SUBCLASS_NAME1", StringUtils.defaultString((String)_param._subclsNameList.get(0)));
        //科目名2
        svf.VrsOut("SUBCLASS_NAME2", StringUtils.defaultString((String)_param._subclsNameList.get(1)));
        //科目名3
        svf.VrsOut("SUBCLASS_NAME3", StringUtils.defaultString((String)_param._subclsNameList.get(2)));
    }

    private String sql() {
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
        stb.append("   ( (S_DAT.RPT01 + S_DAT.RPT02 + S_DAT.RPT03) / (3.0 * 5.0) ) AS AVERAGE5");
        stb.append(" FROM ");
        stb.append("   SUM_SUBCLSSCORE S_DAT ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.EXAMNO, ");
        //SEQ002.REMARK1はデバッグ用。同一人物の特定するために利用。
        stb.append("   SEQ002.REMARK1 AS ID, ");
        stb.append("   H1.EXAMHALLCD, ");
        stb.append("   H2.EXAMHALL_NAME, ");
        stb.append("   T1.NAME, ");
        stb.append("   T1.NAME_KANA, ");
        stb.append("   T1.BIRTHDAY, ");
        stb.append("   FM1.FINSCHOOL_NAME_ABBV AS FINSCHOOL_NAME, ");
        stb.append("   ROUND(ATOTAL.AVERAGE5, 1) AS DEVI, ");
        stb.append("   CASE WHEN BD005.REMARK6 = '1' THEN '有' ELSE '' END AS REUNION, ");
        stb.append("   L058.NAME1 AS COURSE, ");
        stb.append("   CASE WHEN VALUE(L004_2.NAMESPARE3, '') = '2' THEN L061.ABBV1 END AS HEIGAN_ABBV, ");
        stb.append("   L045.NAME1 AS TDIVNAME, ");
        stb.append("   H1.EXAMHALLGROUPCD, ");
        stb.append("   L027_1.NAME1 AS INTERVIEW1, ");
        stb.append("   L027_2.NAME1 AS INTERVIEW2, ");
        stb.append("   CD003.REMARK1 AS CIRCLECNT, ");
        stb.append("   (CASE WHEN N3.ABSENCE_DAYS3 >= 16 THEN CHAR(N3.ABSENCE_DAYS3) ELSE '―' END) AS ABSENCE_DAYS, ");
        stb.append("   S1_1.SCORE AS SCORE1, ");
        stb.append("   S1_2.SCORE AS SCORE2, ");
        stb.append("   S1_3.SCORE AS SCORE3, ");
        stb.append("   S2.TOTAL4, ");
        stb.append("   S2.TOTAL_RANK4, ");
        stb.append("   N3.REMARK1 AS REMARK ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("   LEFT JOIN ENTEXAM_HALL_GROUP_DAT H1 ");
        stb.append("      ON H1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("     AND H1.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("     AND H1.TESTDIV = T1.TESTDIV ");
        stb.append("     AND H1.EXAMNO = T1.EXAMNO ");
        stb.append("     AND H1.EXAMHALL_TYPE = '1' ");
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
        stb.append("   LEFT JOIN FINSCHOOL_MST FM1 ");
        stb.append("     ON T1.FS_CD = FM1.FINSCHOOLCD ");
        stb.append("   LEFT JOIN NAME_MST L045 ");
        stb.append("     ON L045.NAMECD1 = 'L045' ");
        stb.append("    AND L045.NAMECD2 = T1.TESTDIV1 ");
        stb.append("   LEFT JOIN ENTEXAM_INTERVIEW_DAT M1 ");
        stb.append("     ON M1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND M1.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND M1.TESTDIV = T1.TESTDIV ");
        stb.append("    AND M1.EXAMNO = T1.EXAMNO ");
        stb.append("   LEFT JOIN NAME_MST L027_1 ");
        stb.append("     ON L027_1.NAMECD1 = 'L027' ");
        stb.append("    AND L027_1.NAMECD2 = M1.INTERVIEW_A ");
        stb.append("   LEFT JOIN NAME_MST L027_2 ");
        stb.append("     ON L027_2.NAMECD1 = 'L027' ");
        stb.append("    AND L027_2.NAMECD2 = M1.INTERVIEW_B ");
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
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD005 ");
        stb.append("     ON BD005.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("    AND BD005.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND BD005.EXAMNO       = T1.EXAMNO ");
        stb.append("    AND BD005.SEQ          = '005' ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT N3 ");
        stb.append("     ON N3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("    AND N3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND N3.EXAMNO       = T1.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ002 ");
        stb.append("     ON SEQ002.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("    AND SEQ002.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND SEQ002.EXAMNO       = T1.EXAMNO ");
        stb.append("    AND SEQ002.SEQ          = '002' ");
        stb.append("   LEFT JOIN AVERAGE_TOTAL5 ATOTAL ");
        stb.append("     ON ATOTAL.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND ATOTAL.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND ATOTAL.EXAMNO = T1.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD003 ");
        stb.append("     ON CD003.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("    AND CD003.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND CD003.EXAMNO       = T1.EXAMNO ");
        stb.append("    AND CD003.SEQ          = '003' ");
        stb.append(" WHERE ");
        stb.append("   T1.ENTEXAMYEAR = '" + _param._entExamYear + "'   AND ");
        stb.append("   T1.APPLICANTDIV = '" + _param._applicantDiv + "'  ");
        stb.append(" ORDER BY ");
        stb.append("     H1.EXAMHALLCD, ");
        stb.append("     H1.EXAMHALLGROUPCD, ");
        stb.append("     INT(H1.EXAMHALLGROUP_ORDER), ");
        stb.append("     T1.EXAMNO ");

        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65792 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entExamYear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _loginDate;
        final String _testdivName;
        final boolean _testScorePrintFlg;
        final boolean _perHallCdPrintFlg;
        final String _nendo;
        final String _loginDateStr;
        private final List _subclsNameList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
        	_entExamYear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _loginDate = request.getParameter("LOGIN_DATE");
            _testDiv = request.getParameter("TESTDIV");
            _testdivName = getNameMst(db2, "NAME1", _entExamYear, "L065", _testDiv);
            _testScorePrintFlg = "1".equals(StringUtils.defaultString(request.getParameter("TESTSCORE_PRINT"), ""));
            _perHallCdPrintFlg = "1".equals(StringUtils.defaultString(request.getParameter("PERHALLCD_PRINT"), ""));
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_entExamYear)) + "年度 ";
            _loginDateStr = KNJ_EditDate.getAutoFormatDate(db2, _loginDate);
            _subclsNameList = getNameMstList(db2, "NAME1", "L009");
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

        private String getNameMst(final DB2UDB db2, final String field, final String year, final String namecd1, final String namecd2) {
            String rtstr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final StringBuffer stb = new StringBuffer();
            try {
                stb.append(" SELECT ");
                stb.append("     " + field + " ");
                stb.append(" FROM ");
                stb.append("     V_NAME_MST ");
                stb.append(" WHERE ");
                stb.append("         YEAR    = '" + year + "' ");
                stb.append("     AND NAMECD1 = '" + namecd1 + "' ");
                stb.append("     AND NAMECD2 = '" + namecd2 + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtstr = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtstr;
        }

    }
}

// eof

