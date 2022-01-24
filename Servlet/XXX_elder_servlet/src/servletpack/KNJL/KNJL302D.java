/*
 * $Id: fb25aad58d495dac392c45cf1152acd88624ebfa $
 *
 * 作成日: 2018/01/10
 * 作成者: yogi
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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL302D {

    private static final Log log = LogFactory.getLog(KNJL302D.class);

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
        svf.VrSetForm("KNJL302D.frm", 1);

        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度 ";
        svf.VrsOut("TITLE", nendo + "　" + _param._testdivAbbv1 + "　志願者内申データチェックリスト");
        final String loginDateStr = KNJ_EditDate.h_format_JP(db2, _param._loginDate);
		svf.VrsOut("DATE", loginDateStr);
        svf.VrsOut("PAGE", String.valueOf(1) + "頁"); // ページ
        PreparedStatement ps_title = null;
        ResultSet rs_title = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List title_list = new ArrayList();

        try {
            final String titlesql = getNameMstL008Sql();
            log.debug(" titlesql =" + titlesql);
            ps_title = db2.prepareStatement(titlesql);
            rs_title = ps_title.executeQuery();
            while(rs_title.next()) {
            	title_list.add((String)rs_title.getString("ABBV1"));
            }


            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final int maxLine = 8;
            int lineCnt = 1;
            int pageCnt = 1;
            String aftCd = "";
            String befCd = "";
            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                String name = rs.getString("NAME");
                final String fschool_nameabbv = rs.getString("FSCHOOL_NAMEABBV");
                final String t401remark1 = rs.getString("T401_REMARK1");
                final String t401remark2 = rs.getString("T401_REMARK2");
                final String t401remark3 = rs.getString("T401_REMARK3");
                final String t401remark4 = rs.getString("T401_REMARK4");
                final String t401remark5 = rs.getString("T401_REMARK5");
                final String t401remark6 = rs.getString("T401_REMARK6");
                final String t401remark7 = rs.getString("T401_REMARK7");
                final String t401remark8 = rs.getString("T401_REMARK8");
                final String t401remark9 = rs.getString("T401_REMARK9");
                final String t401absence = rs.getString("T401_ABSENCEDAY");
                final String t402remark1 = rs.getString("T402_REMARK1");
                final String t402remark2 = rs.getString("T402_REMARK2");
                final String t402remark3 = rs.getString("T402_REMARK3");
                final String t402remark4 = rs.getString("T402_REMARK4");
                final String t402remark5 = rs.getString("T402_REMARK5");
                final String t402remark6 = rs.getString("T402_REMARK6");
                final String t402remark7 = rs.getString("T402_REMARK7");
                final String t402remark8 = rs.getString("T402_REMARK8");
                final String t402remark9 = rs.getString("T402_REMARK9");
                final String t402absence = rs.getString("T402_ABSENCEDAY");
                final String t3confrpt1 = rs.getString("T3_CONFRPT1");
                final String t3confrpt2 = rs.getString("T3_CONFRPT2");
                final String t3confrpt3 = rs.getString("T3_CONFRPT3");
                final String t3confrpt4 = rs.getString("T3_CONFRPT4");
                final String t3confrpt5 = rs.getString("T3_CONFRPT5");
                final String t3confrpt6 = rs.getString("T3_CONFRPT6");
                final String t3confrpt7 = rs.getString("T3_CONFRPT7");
                final String t3confrpt8 = rs.getString("T3_CONFRPT8");
                final String t3confrpt9 = rs.getString("T3_CONFRPT9");
                final String t3absence = rs.getString("T3_ABSENCEDAY");

                //行動の記録
                final String t403remark1 = rs.getString("T403_REMARK1");
                //検定
                final String t403remark2 = rs.getString("T403_REMARK2");
                final String t403remark3 = rs.getString("T403_REMARK3");
                final String t403remark4 = rs.getString("T403_REMARK4");

                //特別活動
                final String t403remark5 = rs.getString("T403_REMARK5");
                if (lineCnt > maxLine || (!"".equals(befCd) && !befCd.equals(aftCd))) {
                    svf.VrEndPage();
                    svf.VrsOut("TITLE", nendo + "　" + _param._testdivAbbv1 + "　志願者内申データチェックリスト");
                    svf.VrsOut("DATE", loginDateStr);
                    svf.VrsOut("PAGE", String.valueOf(pageCnt + 1) + "頁"); // ページ
                    lineCnt = 1;
                    pageCnt++;
                }

                svf.VrsOutn("EXAM_NO", lineCnt, examno);
                svf.VrsOutn("CLASS_NAME1", lineCnt, (String)title_list.get(0));
                svf.VrsOutn("CLASS_NAME2", lineCnt, (String)title_list.get(1));
                svf.VrsOutn("CLASS_NAME3", lineCnt, (String)title_list.get(2));
                svf.VrsOutn("CLASS_NAME4", lineCnt, (String)title_list.get(3));
                svf.VrsOutn("CLASS_NAME5", lineCnt, (String)title_list.get(4));
                svf.VrsOutn("CLASS_NAME6", lineCnt, (String)title_list.get(5));
                svf.VrsOutn("CLASS_NAME7", lineCnt, (String)title_list.get(6));
                svf.VrsOutn("CLASS_NAME8", lineCnt, (String)title_list.get(7));
                svf.VrsOutn("CLASS_NAME9", lineCnt, (String)title_list.get(8));
                if (20 >= KNJ_EditEdit.getMS932ByteLength(name)) {
                    svf.VrsOutn("NAME1", lineCnt, name);
                }else if (30 >= KNJ_EditEdit.getMS932ByteLength(name)) {
                    svf.VrsOutn("NAME2", lineCnt, name);
                }else {
                    svf.VrsOutn("NAME3", lineCnt, name);
                }
                svf.VrsOutn("VALUE1_1", lineCnt, t401remark1);
                svf.VrsOutn("VALUE1_2", lineCnt, t401remark2);
                svf.VrsOutn("VALUE1_3", lineCnt, t401remark3);
                svf.VrsOutn("VALUE1_4", lineCnt, t401remark4);
                svf.VrsOutn("VALUE1_5", lineCnt, t401remark5);
                svf.VrsOutn("VALUE1_6", lineCnt, t401remark6);
                svf.VrsOutn("VALUE1_7", lineCnt, t401remark7);
                svf.VrsOutn("VALUE1_8", lineCnt, t401remark8);
                svf.VrsOutn("VALUE1_9", lineCnt, t401remark9);
                svf.VrsOutn("ABSENCE1", lineCnt, t401absence);
                svf.VrsOutn("VALUE2_1", lineCnt, t402remark1);
                svf.VrsOutn("VALUE2_2", lineCnt, t402remark2);
                svf.VrsOutn("VALUE2_3", lineCnt, t402remark3);
                svf.VrsOutn("VALUE2_4", lineCnt, t402remark4);
                svf.VrsOutn("VALUE2_5", lineCnt, t402remark5);
                svf.VrsOutn("VALUE2_6", lineCnt, t402remark6);
                svf.VrsOutn("VALUE2_7", lineCnt, t402remark7);
                svf.VrsOutn("VALUE2_8", lineCnt, t402remark8);
                svf.VrsOutn("VALUE2_9", lineCnt, t402remark9);
                svf.VrsOutn("ABSENCE2", lineCnt, t402absence);
                svf.VrsOutn("FSCHOOL_NAME", lineCnt, fschool_nameabbv);
                svf.VrsOutn("VALUE3_1", lineCnt, t3confrpt1);
                svf.VrsOutn("VALUE3_2", lineCnt, t3confrpt2);
                svf.VrsOutn("VALUE3_3", lineCnt, t3confrpt3);
                svf.VrsOutn("VALUE3_4", lineCnt, t3confrpt4);
                svf.VrsOutn("VALUE3_5", lineCnt, t3confrpt5);
                svf.VrsOutn("VALUE3_6", lineCnt, t3confrpt6);
                svf.VrsOutn("VALUE3_7", lineCnt, t3confrpt7);
                svf.VrsOutn("VALUE3_8", lineCnt, t3confrpt8);
                svf.VrsOutn("VALUE3_9", lineCnt, t3confrpt9);
                svf.VrsOutn("ABSENCE3", lineCnt, t3absence);

                svf.VrsOutn("BEHAVIOR", lineCnt, t403remark1);
                svf.VrsOutn("APPROVAL_RANK1", lineCnt, t403remark2);
                svf.VrsOutn("APPROVAL_RANK2", lineCnt, t403remark3);
                svf.VrsOutn("APPROVAL_RANK3", lineCnt, t403remark4);

                svf.VrsOutn("SPECIAL_ACT1", lineCnt, t403remark5);

                String val_total = null;
                final String val4_1 = getSummaryThreeScore(t401remark1, t402remark1, t3confrpt1);
                if (null != val4_1) {
                    svf.VrsOutn("VALUE4_1", lineCnt, String.valueOf(val4_1));
                    val_total = String.valueOf((null == val_total ? 0 : Integer.parseInt(val_total)) + Integer.parseInt(val4_1));
                }
                final String val4_2 = getSummaryThreeScore(t401remark2, t402remark2, t3confrpt2);
                if (null != val4_2) {
                    svf.VrsOutn("VALUE4_2", lineCnt, String.valueOf(val4_2));
                    val_total = String.valueOf((null == val_total ? 0 : Integer.parseInt(val_total)) + Integer.parseInt(val4_2));
                }
                final String val4_3 = getSummaryThreeScore(t401remark3, t402remark3, t3confrpt3);
                if (null != val4_3) {
                    svf.VrsOutn("VALUE4_3", lineCnt, String.valueOf(val4_3));
                    val_total = String.valueOf((null == val_total ? 0 : Integer.parseInt(val_total)) + Integer.parseInt(val4_3));
                }
                final String val4_4 = getSummaryThreeScore(t401remark4, t402remark4, t3confrpt4);
                if (null != val4_4) {
                    svf.VrsOutn("VALUE4_4", lineCnt, String.valueOf(val4_4));
                    val_total = String.valueOf((null == val_total ? 0 : Integer.parseInt(val_total)) + Integer.parseInt(val4_4));
                }
                final String val4_5 = getSummaryThreeScore(t401remark5, t402remark5, t3confrpt5);
                if (null != val4_5) {
                    svf.VrsOutn("VALUE4_5", lineCnt, String.valueOf(val4_5));
                    val_total = String.valueOf((null == val_total ? 0 : Integer.parseInt(val_total)) + Integer.parseInt(val4_5));
                }
                final String val4_6 = getSummaryThreeScore(t401remark6, t402remark6, t3confrpt6);
                if (null != val4_6) {
                    svf.VrsOutn("VALUE4_6", lineCnt, String.valueOf(val4_6));
                    val_total = String.valueOf((null == val_total ? 0 : Integer.parseInt(val_total)) + Integer.parseInt(val4_6));
                }
                final String val4_7 = getSummaryThreeScore(t401remark7, t402remark7, t3confrpt7);
                if (null != val4_7) {
                    svf.VrsOutn("VALUE4_7", lineCnt, String.valueOf(val4_7));
                    val_total = String.valueOf((null == val_total ? 0 : Integer.parseInt(val_total)) + Integer.parseInt(val4_7));
                }
                final String val4_8 = getSummaryThreeScore(t401remark8, t402remark8, t3confrpt8);
                if (null != val4_8) {
                    svf.VrsOutn("VALUE4_8", lineCnt, String.valueOf(val4_8));
                    val_total = String.valueOf((null == val_total ? 0 : Integer.parseInt(val_total)) + Integer.parseInt(val4_8));
                }
                final String val4_9 = getSummaryThreeScore(t401remark9, t402remark9, t3confrpt9);
                if (null != val4_9) {
                    svf.VrsOutn("VALUE4_9", lineCnt, String.valueOf(val4_9));
                    val_total = String.valueOf((null == val_total ? 0 : Integer.parseInt(val_total)) + Integer.parseInt(val4_9));
                }
                if (null != val_total) {
                    svf.VrsOutn("VALUE_TOTAL", lineCnt, String.valueOf(val_total));
                }

                befCd = aftCd;
                lineCnt++;
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

    private static String getSummaryThreeScore(String Score1, String Score2, String Score3) {
    	int retsumscore = 0;
    	if (null != Score1) {
    		retsumscore += Integer.parseInt(Score1);
    	}
    	if (null != Score2) {
    		retsumscore += Integer.parseInt(Score2);
    	}
    	if (null != Score3) {
    		retsumscore += Integer.parseInt(Score3);
    	}
    	if (null == Score1 && null == Score2 && null == Score3) {
    		return null;
    	}
    	return String.valueOf(retsumscore);
    }
    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.FS_CD, ");
        stb.append("     FSCHOOL.FINSCHOOL_NAME_ABBV as FSCHOOL_NAMEABBV, ");
        stb.append("     T4_01.remark1 as T401_REMARK1, ");
        stb.append("     T4_01.remark2 as T401_REMARK2, ");
        stb.append("     T4_01.remark3 as T401_REMARK3, ");
        stb.append("     T4_01.remark4 as T401_REMARK4, ");
        stb.append("     T4_01.remark5 as T401_REMARK5, ");
        stb.append("     T4_01.remark6 as T401_REMARK6, ");
        stb.append("     T4_01.remark7 as T401_REMARK7, ");
        stb.append("     T4_01.remark8 as T401_REMARK8, ");
        stb.append("     T4_01.remark9 as T401_REMARK9, ");
        stb.append("     T3.ABSENCE_DAYS as T401_ABSENCEDAY, ");
        stb.append("     T4_02.remark1 as T402_REMARK1, ");
        stb.append("     T4_02.remark2 as T402_REMARK2, ");
        stb.append("     T4_02.remark3 as T402_REMARK3, ");
        stb.append("     T4_02.remark4 as T402_REMARK4, ");
        stb.append("     T4_02.remark5 as T402_REMARK5, ");
        stb.append("     T4_02.remark6 as T402_REMARK6, ");
        stb.append("     T4_02.remark7 as T402_REMARK7, ");
        stb.append("     T4_02.remark8 as T402_REMARK8, ");
        stb.append("     T4_02.remark9 as T402_REMARK9, ");
        stb.append("     T3.ABSENCE_DAYS2 as T402_ABSENCEDAY, ");
        stb.append("     T3.CONFIDENTIAL_RPT01 as T3_CONFRPT1, ");
        stb.append("     T3.CONFIDENTIAL_RPT02 as T3_CONFRPT2, ");
        stb.append("     T3.CONFIDENTIAL_RPT03 as T3_CONFRPT3, ");
        stb.append("     T3.CONFIDENTIAL_RPT04 as T3_CONFRPT4, ");
        stb.append("     T3.CONFIDENTIAL_RPT05 as T3_CONFRPT5, ");
        stb.append("     T3.CONFIDENTIAL_RPT06 as T3_CONFRPT6, ");
        stb.append("     T3.CONFIDENTIAL_RPT07 as T3_CONFRPT7, ");
        stb.append("     T3.CONFIDENTIAL_RPT08 as T3_CONFRPT8, ");
        stb.append("     T3.CONFIDENTIAL_RPT09 as T3_CONFRPT9, ");
        stb.append("     T3.ABSENCE_DAYS3 as T3_ABSENCEDAY, ");

        //行動の記録
        stb.append("     T4_03.remark1 as T403_REMARK1, ");
        //検定
        stb.append("     T4_03.remark2 as T403_REMARK2, ");
        stb.append("     T4_03.remark3 as T403_REMARK3, ");
        stb.append("     T4_03.remark4 as T403_REMARK4, ");

        //特別活動
        stb.append("     T4_03.remark5 as T403_REMARK5 ");

        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T3 ON T1.ENTEXAMYEAR = T3.ENTEXAMYEAR ");
        stb.append("          AND T1.APPLICANTDIV = T3.APPLICANTDIV ");
        stb.append("          AND T1.EXAMNO = T3.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T4_01 ON T1.ENTEXAMYEAR = T4_01.ENTEXAMYEAR ");
        stb.append("          AND T1.APPLICANTDIV = T4_01.APPLICANTDIV ");
        stb.append("          AND T1.EXAMNO = T4_01.EXAMNO ");
        stb.append("          AND T4_01.SEQ = '001' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T4_02 ON T1.ENTEXAMYEAR = T4_02.ENTEXAMYEAR ");
        stb.append("          AND T1.APPLICANTDIV = T4_02.APPLICANTDIV ");
        stb.append("          AND T1.EXAMNO = T4_02.EXAMNO ");
        stb.append("          AND T4_02.SEQ = '002' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T4_03 ON T1.ENTEXAMYEAR = T4_03.ENTEXAMYEAR ");
        stb.append("          AND T1.APPLICANTDIV = T4_03.APPLICANTDIV ");
        stb.append("          AND T1.EXAMNO = T4_03.EXAMNO ");
        stb.append("          AND T4_03.SEQ = '003' ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FSCHOOL ON T1.FS_CD = FSCHOOL.FINSCHOOLCD ");

        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ");
        return stb.toString();
    }

    private String getNameMstL008Sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     L008.NAMECD2, ");
        stb.append("     L008.ABBV1 ");
        stb.append(" FROM ");
        stb.append("     NAME_MST L008 ");
        stb.append(" WHERE ");
        stb.append("     L008.NAMECD1 = 'L008' ");
        stb.append(" ORDER BY ");
        stb.append("     L008.NAMECD2 ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71866 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _loginDate;
        final String _applicantdivName;
        final String _testdivAbbv1;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _loginDate = request.getParameter("LOGIN_DATE");
            _testDiv = request.getParameter("TESTDIV");
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdivAbbv1 = StringUtils.defaultString(getNameMst(db2, "ABBV1", "L004", _testDiv));
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

