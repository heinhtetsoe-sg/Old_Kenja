/*
 * $Id: bd5570e12153b2588def98072adf504124606b02 $
 *
 * 作成日: 2019/12/23
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJL629F {

    private static final Log log = LogFactory.getLog(KNJL629F.class);

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
        final Map fsMap = getTotalMap(db2);

    	for (Iterator ite = fsMap.keySet().iterator();ite.hasNext();) {
    		final String fsCd = (String)ite.next();
    		final FinSchInfo finSchInfo = (FinSchInfo)fsMap.get(fsCd);
    		printSub(db2, svf, finSchInfo);
    	}
    }
    private void printSub(final DB2UDB db2, final Vrw32alp svf, final FinSchInfo finSchInfo) {

    	final int maxLine = 10;
        svf.VrSetForm("KNJL629F.frm", 1);
        setTitle(db2, svf, finSchInfo);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql(finSchInfo._fs_Cd);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            int prtCnt = 1;
            while (rs.next()) {
            	if (prtCnt > maxLine) {
                    svf.VrEndPage();
                    setTitle(db2, svf, finSchInfo);
            		prtCnt = 1;
            	}
            	final String receptno = rs.getString("RECEPTNO");
            	final String name = rs.getString("NAME");
//            	final String fs_Cd = rs.getString("FS_CD");
//            	final String judgediv = rs.getString("JUDGEDIV");
            	final String course1 = rs.getString("COURSE1");
            	final String shdiv = rs.getString("SHDIV");
            	final String dispjdgtxt = rs.getString("DISPJDGTXT");
//            	final String suc_Course = rs.getString("SUC_COURSE");
//            	final String disptype = rs.getString("DISPTYPE");

                //共通
                svf.VrsOutn("COURSE_NAME", prtCnt, course1); //コース
                svf.VrsOutn("DIV", prtCnt, shdiv); //専併
                svf.VrsOutn("EXAMNO", prtCnt, receptno); //受験番号
                final int nlen = KNJ_EditEdit.getMS932ByteLength(name);
                final String nfield = nlen > 30 ? "3" : nlen > 20 ? "2" : "1";
                svf.VrsOutn("NAME"+nfield, prtCnt, name); //氏名
                svf.VrsOutn("JUDGE", prtCnt, dispjdgtxt); //判定
                prtCnt++;
                _hasData = true;
            }
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final FinSchInfo finSchInfo) {
        String[] nendo = KNJ_EditDate.tate_format4(db2,_param._entexamyear + "-12-31");
        final String prtDate = KNJ_EditDate.h_format_JP(db2, _param._printDate.replace('/', '-'));
        svf.VrsOut("DATE", prtDate);  //日付

        svf.VrsOut("SCHOOL_NAME", (String)_param._certifSchoolMap.get("SCHOOL_NAME"));  //学校名(証明書系の学校名)
        svf.VrsOut("JOBNAME", (String)_param._certifSchoolMap.get("JOB_NAME"));
        svf.VrsOut("STAFFNAME", (String)_param._certifSchoolMap.get("PRINCIPAL_NAME"));  //校長名

        svf.VrsOut("FINSCHOOL_NAME", finSchInfo._finSchoolName);  //出身中学?小学?
        svf.VrsOut("NENDO", nendo[0] + nendo[1] + "年度"); //年度(文章中の年度)
        svf.VrsOut("KIND", _param._testDivName);  //入試名称(入試区分L004 or L024)

        String footerStr = "";
		final String[] printTitlefield = {"合格", "条件", "不合格", "未受験", "合計"};
		final String[] printfieldid = {"1", "3", "2", "4", "9"};
		for (int cnt = 0;cnt < printfieldid.length;cnt++) {
    	    final int chkVal;
	        if (finSchInfo._TotalDataMap.containsKey(printfieldid[cnt])) {
	    	    TotalData prtwk = (TotalData)finSchInfo._TotalDataMap.get(printfieldid[cnt]);
	    	    chkVal = Integer.parseInt(prtwk._reccnt);
	        } else {
	        	chkVal = 0;
	        }

    	    final String yohaku;
    	    if ("9".equals(printfieldid[cnt])) {
	    	    yohaku = chkVal > 1000 ? "" : chkVal > 100 ? " " : chkVal > 10 ? "  " : "   ";  //合計は4桁想定。
    	    } else {
	    	    yohaku = chkVal > 100 ? "" : chkVal > 10 ? " " : "  "; //個別は3桁想定。
    	    }
            //出力位置を特定して出力。3桁を想定して出力する。
    	    footerStr += printTitlefield[cnt] + yohaku + chkVal + "名";  //合否/欠席 集計結果
    	    if (cnt < printfieldid.length - 1) {
    	    	footerStr += "  ";  //区切りの余白
    	    }
		}
		svf.VrsOut("FOOTER", footerStr);
    }

    private String sql(final String fsCd) {
    	final StringBuffer stb = new StringBuffer();
    	stb.append(" SELECT ");
    	stb.append("     BASEDAT.FS_CD, ");
    	stb.append("     RECEPT.JUDGEDIV, ");
    	stb.append("     value(ECM2.EXAMCOURSE_ABBV, '') AS COURSE1, ");
    	stb.append("     L006.NAME1 AS SHDIV, ");
    	stb.append("     RECEPT.RECEPTNO, ");
    	stb.append("     BASEDAT.NAME, ");
    	stb.append("     CASE WHEN (L013.NAMESPARE1 = '1' AND RECEPT.JUDGEDIV = '1') THEN '合格'  ");
    	stb.append("          WHEN (L013.NAMESPARE1 <> '1' OR RECEPT.JUDGEDIV = '4') THEN '欠席' ");
    	stb.append("          WHEN (L013.NAMESPARE1 = '1' AND RECEPT.JUDGEDIV = '3') THEN value(ECM3.EXAMCOURSE_ABBV, '') ");
    	stb.append("          WHEN (L013.NAMESPARE1 <> '1' OR L013.NAMESPARE1 IS NULL) THEN '不合格' ");
    	stb.append("          ELSE '' END AS DISPJDGTXT, ");
    	stb.append("     value(ECM1.EXAMCOURSE_ABBV, '') AS SUC_COURSE, ");
    	stb.append("     CASE WHEN (L013.NAMESPARE1 = '1' AND RECEPT.JUDGEDIV = '1') THEN 1  ");
    	stb.append("          WHEN (L013.NAMESPARE1 <> '1' OR RECEPT.JUDGEDIV = '4') THEN 4 ");
    	stb.append("          WHEN (L013.NAMESPARE1 = '1' AND RECEPT.JUDGEDIV = '3') THEN 3 ");
    	stb.append("          WHEN (L013.NAMESPARE1 <> '1' OR L013.NAMESPARE1 IS NULL) THEN 2 ");
    	stb.append("          ELSE '' END AS DISPTYPE ");
    	stb.append(" FROM ");
    	stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
    	stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASEDAT ");
    	stb.append("       ON BASEDAT.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
    	stb.append("      AND BASEDAT.APPLICANTDIV = RECEPT.APPLICANTDIV ");
    	stb.append("      AND BASEDAT.EXAMNO = RECEPT.EXAMNO ");
    	stb.append("     LEFT JOIN NAME_MST L006 ");
    	stb.append("       ON L006.NAMECD1 = 'L006' ");
    	stb.append("      AND L006.NAMECD2 = BASEDAT.SHDIV ");
    	stb.append("     LEFT JOIN NAME_MST L013 ");
    	stb.append("       ON L013.NAMECD2 = RECEPT.JUDGEDIV ");
    	stb.append("      AND L013.NAMECD1 = 'L013' ");
    	stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RDETAIL001 ");
    	stb.append("       ON RDETAIL001.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
    	stb.append("      AND RDETAIL001.APPLICANTDIV = RECEPT.APPLICANTDIV ");
    	stb.append("      AND RDETAIL001.TESTDIV = RECEPT.TESTDIV ");
    	stb.append("      AND RDETAIL001.RECEPTNO = RECEPT.RECEPTNO ");
    	stb.append("      AND RDETAIL001.SEQ = '001' ");
    	stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL001 ");
    	stb.append("       ON BDETAIL001.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
    	stb.append("      AND BDETAIL001.APPLICANTDIV = RECEPT.APPLICANTDIV ");
    	stb.append("      AND BDETAIL001.EXAMNO = RECEPT.EXAMNO ");
    	stb.append("      AND BDETAIL001.SEQ = '001' ");
    	stb.append("     LEFT JOIN ENTEXAM_COURSE_MST ECM1 ");
    	stb.append("       ON ECM1.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
    	stb.append("      AND ECM1.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("      AND ECM1.TESTDIV = '1' ");
    	stb.append("      AND ECM1.COURSECD = RDETAIL001.REMARK1 ");
    	stb.append("      AND ECM1.MAJORCD = RDETAIL001.REMARK2 ");
    	stb.append("      AND ECM1.EXAMCOURSECD = RDETAIL001.REMARK3 ");
    	stb.append("     LEFT JOIN ENTEXAM_COURSE_MST ECM2 ");
    	stb.append("       ON ECM2.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
    	stb.append("      AND ECM2.APPLICANTDIV = RECEPT.APPLICANTDIV ");
    	stb.append("      AND ECM2.TESTDIV = '1' ");
    	stb.append("      AND ECM2.COURSECD = BDETAIL001.REMARK8 ");
    	stb.append("      AND ECM2.MAJORCD = BDETAIL001.REMARK9 ");
    	stb.append("      AND ECM2.EXAMCOURSECD = BDETAIL001.REMARK10 ");
    	stb.append("     LEFT JOIN ENTEXAM_COURSE_MST ECM3 ");
    	stb.append("       ON ECM3.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
    	stb.append("      AND ECM3.APPLICANTDIV = RECEPT.APPLICANTDIV ");
    	stb.append("      AND ECM3.TESTDIV = '1' ");
    	stb.append("      AND ECM3.COURSECD = BASEDAT.SUC_COURSECD ");
    	stb.append("      AND ECM3.MAJORCD = BASEDAT.SUC_MAJORCD ");
    	stb.append("      AND ECM3.EXAMCOURSECD = BASEDAT.SUC_COURSECODE ");
    	stb.append(" WHERE ");
    	stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
    	stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
    	stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
    	stb.append("     AND FS_CD = '" + fsCd + "' ");
        stb.append(" ORDER BY ");
        stb.append("     VALUE(RECEPT.RECEPTNO, 0) ");
        return stb.toString();
    }

    private Map getTotalMap(final DB2UDB db2) {
    	final Map retMap = new LinkedMap();
    	FinSchInfo addCls = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = totalsql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String fs_Cd = rs.getString("FS_CD");
            	final String finschool_Name = rs.getString("FINSCHOOL_NAME");
            	final String disptype = String.valueOf(rs.getInt("DISPTYPE"));
            	final String reccnt = rs.getString("RECCNT");
            	TotalData addwk = new TotalData(fs_Cd, disptype, reccnt);
            	if (retMap.containsKey(fs_Cd)) {
            		addCls = (FinSchInfo)retMap.get(fs_Cd);
            	} else {
            		addCls = new FinSchInfo(fs_Cd, finschool_Name);
            		retMap.put(fs_Cd, addCls);
            	}
            	addCls._TotalDataMap.put(disptype, addwk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    	return retMap;
    }

    private String totalsql() {
    	final StringBuffer stb = new StringBuffer();
    	stb.append(" WITH PU_DATA_BASE AS ( ");
    	stb.append(" SELECT ");
    	stb.append("     BASEDAT.FS_CD, ");
    	stb.append("     FM.FINSCHOOL_NAME, ");
    	stb.append("     CASE WHEN (L013.NAMESPARE1 = '1' AND RECEPT.JUDGEDIV = '1') THEN 1  ");
    	stb.append("          WHEN (L013.NAMESPARE1 <> '1' OR RECEPT.JUDGEDIV = '4') THEN 4 ");
    	stb.append("          WHEN (L013.NAMESPARE1 = '1' AND RECEPT.JUDGEDIV = '3') THEN 3 ");
    	stb.append("          WHEN (L013.NAMESPARE1 <> '1' OR L013.NAMESPARE1 IS NULL) THEN 2 ");
    	stb.append("          ELSE '' END AS DISPTYPE, ");
    	stb.append("     SUM(1) AS RECCNT ");
    	stb.append(" FROM ");
    	stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
    	stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASEDAT ");
    	stb.append("       ON BASEDAT.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
    	stb.append("      AND BASEDAT.APPLICANTDIV = RECEPT.APPLICANTDIV ");
    	stb.append("      AND BASEDAT.EXAMNO = RECEPT.EXAMNO ");
    	stb.append("     LEFT JOIN NAME_MST L013 ");
    	stb.append("       ON L013.NAMECD2 = RECEPT.JUDGEDIV ");
    	stb.append("      AND L013.NAMECD1 = 'L013' ");
    	stb.append("     LEFT JOIN FINSCHOOL_MST FM ");
    	stb.append("       ON FM.FINSCHOOLCD = BASEDAT.FS_CD ");
    	stb.append(" WHERE ");
    	stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
    	stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
    	stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
    	stb.append("     AND BASEDAT.FS_CD IS NOT NULL ");
    	stb.append(" GROUP BY ");
    	stb.append("     BASEDAT.FS_CD, ");
    	stb.append("     FM.FINSCHOOL_NAME, ");
    	stb.append("     L013.NAMESPARE1, ");
    	stb.append("     RECEPT.JUDGEDIV ");
    	stb.append(" ) ");
    	stb.append(" SELECT ");
    	stb.append("     * ");
    	stb.append(" FROM ");
    	stb.append("     PU_DATA_BASE ");
    	stb.append(" UNION ");
    	stb.append(" SELECT ");
    	stb.append("     PD.FS_CD, ");
    	stb.append("     PD.FINSCHOOL_NAME, ");
    	stb.append("     '9' AS DISPTYPE, ");
    	stb.append("     SUM(PD.RECCNT) AS RECCNT ");
    	stb.append(" FROM ");
    	stb.append("     PU_DATA_BASE PD ");
    	stb.append(" GROUP BY ");
    	stb.append("     PD.FS_CD, ");
    	stb.append("     PD.FINSCHOOL_NAME ");
    	stb.append(" ORDER BY ");
    	stb.append("     FS_CD, ");
    	stb.append("     DISPTYPE ");
    	return stb.toString();
    }

    private class FinSchInfo {
    	final String _fs_Cd;
    	final String _finSchoolName;
    	final Map _TotalDataMap;
    	public FinSchInfo(final String fs_Cd, final String finSchoolName) {
    		_fs_Cd = fs_Cd;
    		_finSchoolName = finSchoolName;
    		_TotalDataMap = new LinkedMap();
    	}
    }
    private class TotalData {
        final String _fs_Cd;
        final String _disptype;
        final String _reccnt;
        public TotalData (final String fs_Cd, final String disptype, final String reccnt)
        {
            _fs_Cd = fs_Cd;
            _disptype = disptype;
            _reccnt = reccnt;
        }
    }
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72283 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _testDivName;
        private final String _printDate;

        private final Map _certifSchoolMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _printDate = request.getParameter("PRINT_DATE");
            _certifSchoolMap = getCertifScholl(db2);
            _testDivName = getTestDivName(db2);
        }

        private String getTestDivName(final DB2UDB db2) {
        	final StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("   ABBV1 ");
        	stb.append(" FROM ");
        	stb.append("   NAME_MST");
        	stb.append(" WHERE ");
        	if ("1".equals(_applicantDiv)) {
        	    stb.append(" NAMECD1 = 'L024' ");
            } else {
        	    stb.append(" NAMECD1 = 'L004' ");
            }
        	stb.append(" AND NAMECD2 = '" + _testDiv + "' ");
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2,stb.toString()));
        }

        private Map getCertifScholl(final DB2UDB db2) {
            final Map rtnMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String cdStr = "1".equals(_applicantDiv) ? "105" : "106";
                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '" + cdStr + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnMap.put("CORP_NAME", rs.getString("REMARK6"));
                    rtnMap.put("SCHOOL_NAME", rs.getString("SCHOOL_NAME"));
                    rtnMap.put("JOB_NAME", rs.getString("JOB_NAME"));
                    rtnMap.put("PRINCIPAL_NAME", rs.getString("PRINCIPAL_NAME"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtnMap;
        }

    }
}

// eof

