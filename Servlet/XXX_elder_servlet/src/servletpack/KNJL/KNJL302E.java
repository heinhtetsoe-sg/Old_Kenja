/*
 * $Id: 910232258bc053b71853118ad1691be26a7d9dfa $
 *
 * 作成日: 2018/11/07
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

public class KNJL302E {

    private static final Log log = LogFactory.getLog(KNJL302E.class);

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
        svf.VrSetForm("KNJL302E.frm", 1);

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
                aftCd = rs.getString("EXAMHALLCD");
                final boolean diffCd = _param._perHallCdPrintFlg && (null != befCd && !"".equals(befCd) && !befCd.equals(aftCd) || null == befCd && null == aftCd);
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
                svf.VrsOutn("FINSCHOOL_NAME", lineCnt, StringUtils.defaultString(rs.getString("FINSCHOOL_NAME_ABBV")));
                //評定値
                if (rs.getString("CONF_AVG5") != null) {
                    String avg = new BigDecimal(rs.getString("CONF_AVG5")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    svf.VrsOutn("DEVI", lineCnt, avg);
                }
                //コース
                svf.VrsOutn("COURSE", lineCnt, rs.getString("DDIVNAME"));
                //受験区分
                svf.VrsOutn("DIV", lineCnt, StringUtils.defaultString(rs.getString("TDIVNAME")));
                //資格内容
                if (rs.getString("QUALIFYDETAIL") != null && !"".equals(rs.getString("QUALIFYDETAIL"))) {
                    String[] quawk = KNJ_EditEdit.get_token(rs.getString("QUALIFYDETAIL"), 80, 3);
                	svf.VrsOutn("QUALIFY_CONTENT1", lineCnt, StringUtils.defaultString(quawk[0]));
                    svf.VrsOutn("QUALIFY_CONTENT2", lineCnt, StringUtils.defaultString(quawk[1]));
                    svf.VrsOutn("QUALIFY_CONTENT3", lineCnt, StringUtils.defaultString(quawk[2]));
                }
                //資格
                svf.VrsOutn("QUALIFY", lineCnt, StringUtils.defaultString(rs.getString("QUALIFY")));
                //実技
                svf.VrsOutn("PRACTICE", lineCnt, StringUtils.defaultString(rs.getString("SKILL")));
                //判定
                svf.VrsOutn("JUDGE", lineCnt, StringUtils.defaultString(rs.getString("JUDGE")));
                //併願
                svf.VrsOutn("SH_DIV", lineCnt, StringUtils.defaultString(rs.getString("SHDIV")));
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
        svf.VrsOut("TITLE", _param._nendo + "　" + _param._testdivName1 + "　推薦受験者一覧表");
        svf.VrsOut("DATE", _param._loginDateStr);
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        //stb.append("  ROW_NUMBER() OVER (ORDER BY H1.EXAMHALLGROUP_ORDER) AS NO_, "); //行番号※CSV用で、Java未使用。
        stb.append("  H1.EXAMHALLCD, "); //改ページ判定で利用。
        stb.append("  T1.EXAMNO, ");
        stb.append("  H2.EXAMHALL_NAME, ");
        stb.append("  T1.NAME, ");
        stb.append("  T1.NAME_KANA, ");
        stb.append("  T1.BIRTHDAY, ");
        stb.append("  FM.FINSCHOOL_NAME_ABBV, ");
        stb.append("  CASE WHEN T3.TOTAL5 IS NULL AND T3_001.REMARK10 IS NULL AND T3_002.REMARK10 IS NULL THEN NULL ");
        stb.append("       ELSE ROUND((VALUE(T3.TOTAL5, 0) + INT(VALUE(T3_001.REMARK10, '0')) + INT(VALUE(T3_002.REMARK10, '0'))) / 15.0 , 1) END AS CONF_AVG5, ");
        stb.append("  L045.ABBV1 AS TDIVNAME, ");
        stb.append("  (CASE WHEN T1.SHDIV = '2' THEN '★' ELSE '' END) AS SHDIV, ");
        stb.append("  L058.NAME1 AS DDIVNAME, ");
        stb.append("  T2.REMARK10 AS QUALIFYDETAIL, ");
        stb.append("  CASE WHEN T2.REMARK2 = '1' THEN '実績' WHEN T2.REMARK2 = '2' THEN '推薦' ELSE '' END AS QUALIFY, ");
        stb.append("  CASE WHEN T2.REMARK3 = '1' THEN '―' WHEN T2.REMARK3 = '2' THEN '有り' WHEN T2.REMARK3 = '3' THEN '無し' ELSE '' END AS SKILL, ");
        stb.append("  CASE WHEN T2.REMARK4 = '1' THEN '―' WHEN T2.REMARK4 = '2' THEN '可' WHEN T2.REMARK4 = '3' THEN '否' ELSE '' END AS JUDGE, ");
        stb.append("  H1.EXAMHALLGROUPCD, ");
        stb.append("  L027_1.NAME1 AS INTERVIEW1, ");
        stb.append("  L027_2.NAME1 AS INTERVIEW2, ");
        stb.append("  T4.REMARK1 AS CIRCLECNT, ");
        stb.append("  (CASE WHEN T3.ABSENCE_DAYS3 >= 16 THEN CHAR(T3.ABSENCE_DAYS3) ELSE '―' END) AS ABSENCE_DAYS, ");
        stb.append("  T3.REMARK1 AS REMARK ");
        stb.append(" FROM ");
        stb.append("  ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("  LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ");
        stb.append("    ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("   AND T2.SEQ = '034' ");
        stb.append("  LEFT JOIN ENTEXAM_HALL_GROUP_DAT H1 ");
        stb.append("    ON H1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND H1.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND H1.TESTDIV = T1.TESTDIV ");
        stb.append("   AND H1.EXAMNO = T1.EXAMNO ");
        stb.append("   AND H1.EXAMHALL_TYPE = '1' ");  //1:面接(固定で指定)
        stb.append("  LEFT JOIN ENTEXAM_HALL_YDAT H2 ");
        stb.append("    ON H2.ENTEXAMYEAR = H1.ENTEXAMYEAR ");
        stb.append("   AND H2.APPLICANTDIV = H1.APPLICANTDIV ");
        stb.append("   AND H2.TESTDIV = H1.TESTDIV ");
        stb.append("   AND H2.EXAM_TYPE = H1.EXAMHALL_TYPE ");
        stb.append("   AND H2.EXAMHALLCD = H1.EXAMHALLCD ");
        stb.append("  LEFT JOIN ENTEXAM_INTERVIEW_DAT M1 ");
        stb.append("    ON M1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND M1.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND M1.TESTDIV = T1.TESTDIV ");
        stb.append("   AND M1.EXAMNO = T1.EXAMNO ");
        stb.append("  LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T3 ");
        stb.append("    ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND T3.EXAMNO = T1.EXAMNO ");
        stb.append("  LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T3_001 ");
        stb.append("    ON T3_001.ENTEXAMYEAR = T3.ENTEXAMYEAR ");
        stb.append("   AND T3_001.APPLICANTDIV = T3.APPLICANTDIV ");
        stb.append("   AND T3_001.EXAMNO = T3.EXAMNO ");
        stb.append("   AND T3_001.SEQ = '001' ");
        stb.append("  LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T3_002 ");
        stb.append("    ON T3_002.ENTEXAMYEAR = T3.ENTEXAMYEAR ");
        stb.append("   AND T3_002.APPLICANTDIV = T3.APPLICANTDIV ");
        stb.append("   AND T3_002.EXAMNO = T3.EXAMNO ");
        stb.append("   AND T3_002.SEQ = '002' ");
        stb.append("  LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T4 ");
        stb.append("    ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND T4.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND T4.EXAMNO = T1.EXAMNO ");
        stb.append("   AND T4.SEQ = '003' ");
        stb.append("  LEFT JOIN NAME_MST L045 ");
        stb.append("    ON L045.NAMECD1 = 'L045' ");
        stb.append("   AND L045.NAMECD2 = T1.TESTDIV1 ");
        stb.append("  LEFT JOIN NAME_MST L058 ");
        stb.append("    ON L058.NAMECD1 = 'L058' ");
        stb.append("   AND L058.NAMECD2 = T1.DESIREDIV ");
        stb.append("  LEFT JOIN NAME_MST L027_1 ");
        stb.append("    ON L027_1.NAMECD1 = 'L027' ");
        stb.append("   AND L027_1.NAMECD2 = M1.INTERVIEW_A ");
        stb.append("  LEFT JOIN NAME_MST L027_2 ");
        stb.append("    ON L027_2.NAMECD1 = 'L027' ");
        stb.append("   AND L027_2.NAMECD2 = M1.INTERVIEW_B ");
        stb.append("  LEFT JOIN FINSCHOOL_MST FM ");
        stb.append("    ON FM.FINSCHOOLCD = T1.FS_CD ");
        stb.append(" WHERE ");
        stb.append("   T1.ENTEXAMYEAR = '" + _param._entExamYear + "' ");
        stb.append("   AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("   AND T1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("  H1.EXAMHALLCD, ");
        stb.append("  H1.EXAMHALLGROUPCD, ");
        stb.append("  INT(H1.EXAMHALLGROUP_ORDER), ");
        stb.append("  T1.EXAMNO ");

        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 64507 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entExamYear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _loginDate;
        final String _applicantdivName;
        final String _testdivName1;
        final boolean _testScorePrintFlg;
        final boolean _perHallCdPrintFlg;
        final String _nendo;
        final String _loginDateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
        	_entExamYear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _loginDate = request.getParameter("LOGIN_DATE");
            _testDiv = request.getParameter("TESTDIV");
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdivName1 = StringUtils.defaultString(getNameMst(db2, "NAME1", "L004", _testDiv));
            _testScorePrintFlg = "1".equals(StringUtils.defaultString(request.getParameter("TESTSCORE_PRINT"), ""));
            _perHallCdPrintFlg = "1".equals(StringUtils.defaultString(request.getParameter("PERHALLCD_PRINT"), ""));
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_entExamYear)) + "年度 ";
            _loginDateStr = KNJ_EditDate.getAutoFormatDate(db2, _loginDate);
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
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

