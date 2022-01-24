/*
 * $Id: af21b6a2ab92aac8e7b8a17422b60135b5908e6d $
 *
 * 作成日: 2019/04/10
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 教科書購入票
 */
public class KNJP1218 {

    private static final Log log = LogFactory.getLog(KNJP1218.class);

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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        final List studentList = getStudentList(db2);
        for (Iterator itStd = studentList.iterator(); itStd.hasNext();) {
            final Student student = (Student) itStd.next();
            Map colInfoSubMap = (Map)_param._collectDataMap.get(student._schregNo);

            if (colInfoSubMap != null) {
                for (Iterator ite = colInfoSubMap.keySet().iterator(); ite.hasNext();) {
            	    String kstr = (String)ite.next();
            	    Map colInfoMap = (Map)colInfoSubMap.get(kstr);

            	    svf.VrSetForm("KNJP1218.frm", 4);
                    printHeader(db2, svf, student, "領収書", kstr);
                    printChairText(db2, svf, student, colInfoMap);
                    svf.VrEndPage();
                    _hasData = true;
                }
            }
        }
    }

    /**
     * 生徒データ取得処理
     * @param db2           ＤＢ接続オブジェクト
     * @return              帳票出力対象データリスト
     * @throws Exception
     */
    private List getStudentList(final DB2UDB db2) throws SQLException {

        final List rtnList = new ArrayList();
        final String sql = getStudentInfoSql();
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String courseCd = rs.getString("COURSECD");
                final String majorCd = rs.getString("MAJORCD");
                final String courseCode = rs.getString("COURSECODE");
                final String name = rs.getString("NAME");
                final String hrName = rs.getString("HR_NAME");
                final Student studentInfo = new Student(schregNo, grade, hrClass, attendno, courseCd, majorCd, courseCode, name, hrName);
                rtnList.add(studentInfo);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getStudentInfoSql() {
        final StringBuffer stb = new StringBuffer();
        if ("2".equals(_param._output)) {
	        stb.append(" SELECT ");
	        stb.append("     REGD.SCHREGNO, ");
	        stb.append("     REGD.GRADE, ");
	        stb.append("     REGD.HR_CLASS, ");
	        stb.append("     REGD.ATTENDNO, ");
	        stb.append("     REGD.COURSECD, ");
	        stb.append("     REGD.MAJORCD, ");
	        stb.append("     REGD.COURSECODE, ");
	        stb.append("     BASE.NAME, ");
	        stb.append("     REGDH.HR_NAME ");
	        stb.append(" FROM ");
	        stb.append("     SCHREG_REGD_DAT REGD ");
	        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
	        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
	        stb.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
	        stb.append("          AND REGD.GRADE = REGDH.GRADE ");
	        stb.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
	        stb.append(" WHERE ");
	        stb.append("     REGD.YEAR = '" + _param._year + "' ");
	        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
	        stb.append("     AND REGD.SCHREGNO IN " + _param._studentInState + " ");
	        stb.append(" ORDER BY ");
	        stb.append("     REGD.GRADE, ");
	        stb.append("     REGD.HR_CLASS, ");
	        stb.append("     REGD.ATTENDNO ");
        } else {
        	stb.append(" SELECT ");
        	stb.append("   SCHREGNO, ");
        	stb.append("   GRADE, ");
        	stb.append("   HR_CLASS, ");
        	stb.append("   ATTENDNO, ");
	        stb.append("   COURSECD, ");
	        stb.append("   MAJORCD, ");
	        stb.append("   COURSECODE, ");
        	stb.append("   NAME, ");
        	stb.append("   '' AS HR_NAME ");
        	stb.append(" FROM ");
        	stb.append("   FRESHMAN_DAT ");
        	stb.append(" WHERE ");
        	stb.append("   ENTERYEAR = '" + (Integer.parseInt(_param._year) + 1) + "' ");

        }
        return stb.toString();
    }

    private void printHeader(final DB2UDB db2, final Vrw32alp svf, final Student student, final String title, final String checkNo) {

        String setTitle = "諸費 "+ title;
        String sep = "";

        svf.VrsOut("CHECK_NO", checkNo.length() > 0 ? checkNo : "");
        svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度　" + setTitle);
        svf.VrsOut("NO", student._schregNo);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._printDate));
        svf.VrsOut("SCHOOLNAME", _param._schoolname);

        if (!"".equals(student._hrName)) {
            svf.VrsOut("HR_NAME", "(" + student._hrName + ")");
        }
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getSql(student._schregNo);
            log.debug("address sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                svf.VrsOut("ZIP_NO", rs.getString("ZIPCD"));
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                if ("1".equals(_param._useAddrField2) && (getMS932ByteLength(addr1) > 50 || getMS932ByteLength(addr2) > 50)) {
                    svf.VrsOut("ADDRESS1_3", addr1);
                    svf.VrsOut("ADDRESS2_3", addr2);
                } else if ("1".equals(_param._useAddrField2) && (getMS932ByteLength(addr1) > 44 || getMS932ByteLength(addr2) > 44)) {
                    svf.VrsOut("ADDRESS1_2", addr1);
                    svf.VrsOut("ADDRESS2_2", addr2);
                } else {
                    svf.VrsOut("ADDRESS1", addr1);
                    svf.VrsOut("ADDRESS2", addr2);
                }
                svf.VrsOut("NAME", StringUtils.defaultString(rs.getString("NAME2")) + "　様");
            }
        } catch (final Exception ex) {
            log.error("set_detail1 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getSql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        if ("1".equals(_param._output)) {
            stb.append("SELECT ");
            stb.append("  t0.SCHREGNO, ");
            stb.append("  t0.ZIPCD,");
            stb.append("  t0.ADDR1,");
            stb.append("  t0.ADDR2,");
            stb.append("  t0.NAME, ");
            stb.append("  t0.NAME AS NAME2 ");
            stb.append(" FROM ");
            stb.append("   FRESHMAN_DAT t0 ");
            stb.append(" WHERE ");
            stb.append("  t0.ENTERYEAR = '" + (Integer.parseInt(_param._year) + 1) + "' ");
            stb.append("  AND t0.SCHREGNO = '" + schregno + "' ");
        } else {
            stb.append(" WITH SCHREG_ADDRESS AS ( ");
            stb.append("   SELECT  ");
            stb.append("      T3.NAME AS SCHREG_NAME, ");
            stb.append("      T1.*  ");
            stb.append("   FROM  ");
            stb.append("      SCHREG_ADDRESS_DAT T1  ");
            stb.append("      INNER JOIN ( ");
            stb.append("        SElECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM SCHREG_ADDRESS_DAT GROUP BY SCHREGNO ");
            stb.append("      ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ISSUEDATE = T1.ISSUEDATE ");
            stb.append("      INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(" ) ");

            stb.append("SELECT ");
            stb.append("  t0.SCHREGNO, ");
            stb.append("  CASE WHEN SEND_ADDR1 IS NOT NULL THEN SEND_ZIPCD ELSE t2.ZIPCD END AS ZIPCD,");
            stb.append("  CASE WHEN SEND_ADDR1 IS NOT NULL THEN SEND_ADDR1 ELSE t2.ADDR1 END AS ADDR1,");
            stb.append("  CASE WHEN SEND_ADDR1 IS NOT NULL THEN SEND_ADDR2 ELSE t2.ADDR2 END AS ADDR2,");
            stb.append("  CASE WHEN SEND_ADDR1 IS NOT NULL THEN SEND_NAME  ELSE t2.SCHREG_NAME END AS NAME,");
            stb.append("  t0.NAME AS NAME2 ");
            stb.append(" FROM ");
            stb.append("   SCHREG_BASE_MST t0 ");
            stb.append(" LEFT JOIN SCHREG_SEND_ADDRESS_DAT t1 ON t1.SCHREGNO = t0.SCHREGNO ");
            stb.append("   AND t1.DIV = '1' ");
            stb.append(" LEFT JOIN SCHREG_ADDRESS t2 ON t2.SCHREGNO = t0.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("  t0.SCHREGNO = '" + schregno + "' ");
        }
        return stb.toString();
    }

    private void printChairText(final DB2UDB db2, final Vrw32alp svf, final Student student, final Map colInfoMap) {
    	//一度、合計を出すために集計。
    	long totalmoney = 0;
    	if (colInfoMap != null && colInfoMap.size() > 0) {
    	    for (Iterator its = colInfoMap.keySet().iterator();its.hasNext();) {
    		    String kstr = (String)its.next();
    		    CollectInfo outinfo = (CollectInfo)colInfoMap.get(kstr);
    		    totalmoney += Integer.parseInt(outinfo._collectMoney);
    	    }
    	}
    	svf.VrsOut("TOTAL_PRICE2", String.valueOf(totalmoney));

    	//詳細を出力。
    	if (colInfoMap != null && colInfoMap.size() > 0) {
    	    for (Iterator ite = colInfoMap.keySet().iterator();ite.hasNext();) {
    		    String kstr = (String)ite.next();
    		    CollectInfo outinfo = (CollectInfo)colInfoMap.get(kstr);
    		    svf.VrsOut("ITEM", outinfo._collectName);
    		    svf.VrsOut("PRICE", outinfo._collectMoney);
    		    svf.VrEndRecord();
    	    }
    	}
		svf.VrsOut("TOTAL_PRICE", String.valueOf(totalmoney));
		svf.VrEndRecord();
		svf.VrEndPage();
    }

    private static int getMS932ByteLength(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (final Exception e) {
                log.error("exception!", e);
            }
        }
        return 0;
    }

    /** 生徒クラス */
    private class Student {
        final String _schregNo;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _name;
        final String _hrName;
        public Student(
                final String schregNo,
                final String grade,
                final String hrClass,
                final String attendno,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String name,
                final String hrName
        ) {
            _schregNo = schregNo;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _name = name;
            _hrName = hrName;
        }
    }

    /** 出力データクラス */
    private class CollectInfo {
        final String _collectLCd;
        final String _collectMCd;
        final String _collectName;
        final String _collectMoney;
        public CollectInfo(
                final String collectLCd,
                final String collectMCd,
                final String collectName,
                final String collectMoney
        ) {
            _collectLCd = collectLCd;
            _collectMCd = collectMCd;
            _collectName = collectName;
            _collectMoney = collectMoney;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 66991 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlDate;
        private final String _semester;
        private final String _output;
        private final String _studentInState;
        private final String _useAddrField2;
        final String _schoolname;
        final String _useSchool_KindField;
        final String _SCHOOLCD;
        final String _SCHOOLKIND;
        final String _PRGID;
        private final String _printDate;
        private final Map _collectDataMap; // 右記の3段MAPとなっている。(SCHREGNO, (SLIP_NO, (COLLECT_M_CD, 出力データ)))

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _semester = request.getParameter("SEMESTER");
            _output = request.getParameter("OUTPUT");
            _printDate = request.getParameter("PRINTDATE");

            final String[] schregs = request.getParameterValues("category_name");
            String studentInstate = "(";
            String sep = "";
            for (int i = 0; i < schregs.length; i++) {
                studentInstate += sep + "'" + StringUtils.split(schregs[i], "-")[0] + "'";
                sep = ",";
            }
            studentInstate += ")";

            _studentInState = studentInstate;

            _useAddrField2 = request.getParameter("useAddrField2");
            _schoolname = getSchoolname(db2);
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD = request.getParameter("SCHOOLCD");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _PRGID = request.getParameter("PRGID");
            _collectDataMap = getInvoiceDat(db2);
        }

        // 卒業認定単位数の取得
        private String getSchoolname(
                final DB2UDB db2
        ) {
            String schoolname1 = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "'";
                if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                    sql += "   AND SCHOOL_KIND = '" + _SCHOOLKIND + "' ";
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    schoolname1 = rs.getString("SCHOOLNAME1");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolname1;
        }

        private Map getInvoiceDat(final DB2UDB db2) {
            final Map retMap = new HashMap();
            final String sql = getInvoiceDatSql();
            log.debug("sql = "+sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String slipno = rs.getString("SLIP_NO");
                    final String collectLcd = rs.getString("COLLECT_L_CD");
                    final String collectMcd = rs.getString("COLLECT_M_CD");
                    final String collectname = rs.getString("COLLECT_M_NAME");
                    final String collectmoney = rs.getString("COLLECT_MONEY");
                    // 3段MAPを設定。(SCHREGNO, (SLIP_NO, (COLLECT_M_CD, 出力データ)))
                    //1段目(SCHREGNO)
                    Map addSubMap = (Map)retMap.get(schregno);
                    if (addSubMap == null) {
                    	addSubMap = new LinkedMap();
                        retMap.put(schregno, addSubMap);
                    }
                    //2段目(SLIP_NO)
                    Map addMap = (Map)addSubMap.get(slipno);
                    if (addMap == null) {
                    	addMap = new LinkedMap();
                    	addSubMap.put(slipno, addMap);
                    }
                    CollectInfo addwk = new CollectInfo(collectLcd, collectMcd, collectname, collectmoney);
                    //3段目(COLLECT_M_CD)
                    addMap.put(collectLcd + collectMcd, addwk);
                }
            } catch (SQLException ex) {
                log.debug("getM004 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private String getInvoiceDatSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SLIP_T AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     SLIPM.SCHOOLCD, ");
            stb.append("     SLIPM.SCHOOL_KIND, ");
            stb.append("     SLIPM.YEAR, ");
            stb.append("     SLIPM.SLIP_NO ");
            stb.append(" FROM ");
            stb.append("     COLLECT_SLIP_M_DAT SLIPM ");
            stb.append("     LEFT JOIN COLLECT_SLIP_DAT COSD ");
            stb.append("        ON COSD.SCHOOLCD = SLIPM.SCHOOLCD ");
            stb.append("       AND COSD.SCHOOL_KIND = SLIPM.SCHOOL_KIND ");
            stb.append("       AND COSD.YEAR = SLIPM.YEAR ");
            stb.append("       AND COSD.SLIP_NO = SLIPM.SLIP_NO ");
            stb.append("     INNER JOIN COLLECT_M_MST COLM ");
            stb.append("        ON SLIPM.SCHOOLCD = COLM.SCHOOLCD ");
            stb.append("       AND SLIPM.SCHOOL_KIND = COLM.SCHOOL_KIND ");
            stb.append("       AND SLIPM.YEAR = COLM.YEAR ");
            stb.append("       AND SLIPM.COLLECT_L_CD = COLM.COLLECT_L_CD ");
            stb.append("       AND SLIPM.COLLECT_M_CD = COLM.COLLECT_M_CD ");
            stb.append("       AND COLM.TEXTBOOKDIV IS NOT NULL ");
            stb.append(" WHERE ");
            stb.append("     SLIPM.SCHOOLCD = '" + _SCHOOLCD + "' ");
            if ("2".equals(_output)) {
                stb.append("     AND SLIPM.YEAR = '" + _year + "' ");
            } else {
                stb.append("     AND SLIPM.YEAR = '" + (Integer.parseInt(_year) + 1) + "' ");
            }
            stb.append("     AND COSD.CANCEL_DATE IS NULL ");
            stb.append("     AND SLIPM.SCHREGNO IN " + _studentInState);
            stb.append("     AND NOT EXISTS(SELECT ");
            stb.append("                     'X' ");
            stb.append("                   FROM ");
            stb.append("                     COLLECT_SLIP_MONEY_PAID_M_DAT SLIPMPMD ");
            stb.append("                   WHERE ");
            stb.append("                     SLIPMPMD.SCHOOLCD = SLIPM.SCHOOLCD ");
            stb.append("                     AND SLIPMPMD.SCHOOL_KIND = SLIPM.SCHOOL_KIND ");
            stb.append("                     AND SLIPMPMD.YEAR = SLIPM.YEAR ");
            stb.append("                     AND SLIPMPMD.SLIP_NO = SLIPM.SLIP_NO ");
            stb.append("                   ) ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     SLIPM.SCHREGNO, ");
            stb.append("     SLIPM.SLIP_NO, ");
            stb.append("     SLIPM.COLLECT_L_CD, ");
            stb.append("     SLIPM.COLLECT_M_CD, ");
            stb.append("     COLM.COLLECT_M_NAME, ");
            stb.append("     SLIPM.COLLECT_MONEY ");
            stb.append(" FROM ");
            stb.append("     SLIP_T, ");
            stb.append("     COLLECT_SLIP_M_DAT SLIPM ");
            stb.append("     LEFT JOIN COLLECT_M_MST COLM ON SLIPM.SCHOOLCD = COLM.SCHOOLCD ");
            stb.append("          AND SLIPM.SCHOOL_KIND = COLM.SCHOOL_KIND ");
            stb.append("          AND SLIPM.YEAR = COLM.YEAR ");
            stb.append("          AND SLIPM.COLLECT_L_CD = COLM.COLLECT_L_CD ");
            stb.append("          AND SLIPM.COLLECT_M_CD = COLM.COLLECT_M_CD ");
            stb.append(" WHERE ");
            stb.append("     SLIP_T.SCHOOLCD = SLIPM.SCHOOLCD ");
            stb.append("     AND SLIP_T.SCHOOL_KIND = SLIPM.SCHOOL_KIND ");
            stb.append("     AND SLIP_T.YEAR = SLIPM.YEAR ");
            stb.append("     AND SLIP_T.SLIP_NO = SLIPM.SLIP_NO ");
            stb.append("     AND SLIP_T.SCHOOLCD = SLIPM.SCHOOLCD ");
            stb.append("     AND COLM.TEXTBOOKDIV IS NULL ");
            stb.append(" ORDER BY ");
            stb.append("     SLIPM.SCHREGNO, ");
            stb.append("     SLIPM.SLIP_NO, ");
            stb.append("     SLIPM.COLLECT_M_CD ");
            return stb.toString();
        }
    }
}

// eof

