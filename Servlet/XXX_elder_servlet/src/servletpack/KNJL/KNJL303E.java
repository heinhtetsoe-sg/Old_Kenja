/*
 * $Id: 1e31108c0f2c075493d22e329f5935e5498f5d50 $
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

public class KNJL303E {

    private static final Log log = LogFactory.getLog(KNJL303E.class);

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
        svf.VrSetForm("KNJL303E.frm", 4);

        List printList = getList(db2);

        setTitle(svf, 1);
        String aftCd = "";
        String befCd = "";
        int lineCnt = 1;
        int pageCnt = 1;
        final int maxLine = 50;
//        int recordcnt = 1;
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            aftCd = printData._examHallCd;
            if (lineCnt > maxLine || (!"".equals(befCd) && !befCd.equals(aftCd))) {
                svf.VrEndPage();
                svf.VrSetForm("KNJL303E.frm", 4);
                setTitle(svf, pageCnt + 1);
                lineCnt = 1;
                pageCnt++;
//                if (!"".equals(befCd) && !befCd.equals(aftCd)) {
//          	        recordcnt = 1;
//                }
            }
            if (lineCnt == 1) {
                svf.VrsOut("ROOM_NAME", StringUtils.defaultString(printData._examHallName));
  	        }

            //グループ
            svf.VrsOut("GROUP", StringUtils.defaultString(printData._examHallGroupCd));
            //受験コース
            svf.VrsOut("DESIREDIV", StringUtils.defaultString(printData._desiredivName));
            //受験番号
            svf.VrsOut("EXAM_NO", StringUtils.defaultString(printData._examNo));
            //氏名かな
            svf.VrsOut("NAME_KANA1", StringUtils.defaultString(printData._nameKana));
            //氏名
            svf.VrsOut("NAME1", StringUtils.defaultString(printData._name));
            //中学校
            svf.VrsOut("FINSCHOOL_NAME", StringUtils.defaultString(printData._finSchoolNameAbbv));
            //受験区分
            svf.VrsOut("DIV", StringUtils.defaultString(printData._tDivName));
            //面接点
            if (_param._testScorePrintFlg) {
                svf.VrsOut("INTERVIEW_POINT1", StringUtils.defaultString(printData._interview1));
                svf.VrsOut("INTERVIEW_POINT2", StringUtils.defaultString(printData._interview2));
            }

            svf.VrEndRecord();
            befCd = aftCd;
            lineCnt++;
//            recordcnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private List getList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List retList = new ArrayList();
        try {

            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	PrintData addwk = new PrintData(rs.getString("EXAMHALLCD"), rs.getString("EXAMHALL_NAME"), rs.getString("EXAMHALLGROUPCD"), rs.getString("DESIREDIVNAME"), rs.getString("EXAMNO"), rs.getString("NAME"), rs.getString("NAME_KANA"), rs.getString("FS_CD"), rs.getString("FINSCHOOL_NAME_ABBV"), rs.getString("TDIVNAME"), rs.getString("INTERVIEW1"), rs.getString("INTERVIEW2"));
            	retList.add(addwk);
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retList;
    }

    private void setTitle(final Vrw32alp svf, final int pagenum) {
        svf.VrsOut("TITLE", _param._nendo + "　" + _param._testdivName1 + "　面接グループ別一覧表");
        svf.VrsOut("DATE", _param._loginDateStr);
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();

        //stb.append("  ROW_NUMBER() OVER (ORDER BY H1.EXAMHALLGROUP_ORDER) AS NO_, "); //行番号※CSV用で、Java未使用。
        stb.append(" SELECT ");
        stb.append("  H1.EXAMHALLCD, ");
        stb.append("  H1.EXAMHALLGROUPCD, ");
        stb.append("  H2.EXAMHALL_NAME, ");
        stb.append("  T1.DESIREDIV, ");
        stb.append("  L058.NAME1 AS DESIREDIVNAME, ");
        stb.append("  T1.EXAMNO, ");
        stb.append("  T1.NAME, ");
        stb.append("  T1.NAME_KANA, ");
        stb.append("  T1.FS_CD, ");
        stb.append("  FM1.FINSCHOOL_NAME_ABBV, ");
        stb.append("  L045.ABBV1 AS TDIVNAME, ");
        stb.append("  L027_1.NAME1 AS INTERVIEW1, ");
        stb.append("  L027_2.NAME1 AS INTERVIEW2 ");
        stb.append(" FROM ");
        stb.append("  ENTEXAM_HALL_GROUP_DAT H1 ");
        stb.append("  INNER JOIN ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("    ON T1.ENTEXAMYEAR = H1.ENTEXAMYEAR ");
        stb.append("   AND T1.APPLICANTDIV = H1.APPLICANTDIV ");
        stb.append("   AND T1.TESTDIV = H1.TESTDIV ");
        stb.append("   AND T1.EXAMNO = H1.EXAMNO ");
        stb.append("   AND value(T1.JUDGEMENT, '0') <> '3' ");  //欠席者を除外
        stb.append("  LEFT JOIN ENTEXAM_HALL_YDAT H2 ");
        stb.append("    ON H2.ENTEXAMYEAR = H1.ENTEXAMYEAR ");
        stb.append("   AND H2.APPLICANTDIV = H1.APPLICANTDIV ");
        stb.append("   AND H2.TESTDIV = H1.TESTDIV ");
        stb.append("   AND H2.EXAM_TYPE = H1.EXAMHALL_TYPE ");
        stb.append("   AND H2.EXAMHALLCD = H1.EXAMHALLCD ");
        stb.append("  LEFT JOIN FINSCHOOL_MST FM1 ");
        stb.append("    ON T1.FS_CD = FM1.FINSCHOOLCD ");
        stb.append("  LEFT JOIN NAME_MST L045 ");
        stb.append("    ON L045.NAMECD1 = 'L045' ");
        stb.append("   AND L045.NAMECD2 = T1.TESTDIV1 ");
        stb.append("  LEFT JOIN ENTEXAM_INTERVIEW_DAT M1 ");
        stb.append("    ON M1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND M1.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND M1.TESTDIV = T1.TESTDIV ");
        stb.append("   AND M1.EXAMNO = T1.EXAMNO ");
        stb.append("  LEFT JOIN NAME_MST L027_1 ");
        stb.append("    ON L027_1.NAMECD1 = 'L027' ");
        stb.append("   AND L027_1.NAMECD2 = M1.INTERVIEW_A ");
        stb.append("  LEFT JOIN NAME_MST L027_2 ");
        stb.append("    ON L027_2.NAMECD1 = 'L027' ");
        stb.append("   AND L027_2.NAMECD2 = M1.INTERVIEW_B ");
        stb.append("  LEFT JOIN NAME_MST L058 ");
        stb.append("    ON L058.NAMECD1 = 'L058' ");
        stb.append("   AND L058.NAMECD2 = T1.DESIREDIV ");
        stb.append(" WHERE ");
        stb.append("  H1.ENTEXAMYEAR = '" + _param._entExamYear + "' ");
        stb.append("  AND H1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("  AND H1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("  AND H1.EXAMHALL_TYPE = '1' "); //1:面接(固定で指定)
        stb.append(" ORDER BY ");
        stb.append("  H1.EXAMHALLCD, ");
        stb.append("  H1.EXAMHALLGROUPCD, ");
        stb.append("  INT(H1.EXAMHALLGROUP_ORDER), ");
        stb.append("  H1.EXAMNO ");

        return stb.toString();
    }

    private class PrintData {
    	final String _examHallCd;
    	final String _examHallName;
    	final String _examHallGroupCd;
    	final String _desiredivName;
    	final String _examNo;
    	final String _name;
    	final String _nameKana;
    	final String _fs_Cd;
    	final String _finSchoolNameAbbv;
    	final String _tDivName;
    	final String _interview1;
    	final String _interview2;

        public PrintData(
        		final String examHallCd,
        		final String examHallName,
        		final String examHallGroupCd,
        		final String desiredivName,
        		final String examNo,
        		final String name,
        		final String nameKana,
        		final String fs_Cd,
        		final String finSchoolNameAbbv,
        		final String tDivName,
        		final String interview1,
        		final String interview2
        ) {
        	_examHallCd = examHallCd;
        	_examHallName = examHallName;
        	_examHallGroupCd = examHallGroupCd;
        	_desiredivName = desiredivName;
        	_examNo = examNo;
        	_name = name;
        	_nameKana = nameKana;
        	_fs_Cd = fs_Cd;
        	_finSchoolNameAbbv = finSchoolNameAbbv;
        	_tDivName = tDivName;
        	_interview1 = interview1;
        	_interview2 = interview2;
          }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 64242 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entExamYear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _loginDate;
        private final String _loginDateStr;
        final String _applicantdivName;
        final String _testdivName1;
        final boolean _testScorePrintFlg;
        final String _nendo;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
        	_entExamYear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _loginDate = request.getParameter("LOGIN_DATE");
            _testDiv = request.getParameter("TESTDIV");
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdivName1 = StringUtils.defaultString(getNameMst(db2, "NAME1", "L004", _testDiv));
            _testScorePrintFlg = "1".equals(StringUtils.defaultString(request.getParameter("TESTSCORE_PRINT"), ""));
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

