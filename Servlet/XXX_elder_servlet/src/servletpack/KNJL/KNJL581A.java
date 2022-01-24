/*
 * $Id: dad3dde921f65397d8c04be051c2f8ff9de30b0c $
 *
 * 作成日: 2019/10/01
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJL581A {

    private static final Log log = LogFactory.getLog(KNJL581A.class);

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
    	final Map subclsCdMap = getNameMstIdName(db2);
        final Map printDataMap = getPrintDataMap(db2, subclsCdMap);
        for (Iterator itr = printDataMap.keySet().iterator();itr.hasNext();) {
            final String fscd = (String)itr.next();
            svf.VrSetForm("KNJL581A.frm", 4);
        	printList(db2, svf, fscd, (List)printDataMap.get(fscd));
            svf.VrEndPage();
        }
    }

    private void printList(final DB2UDB db2, final Vrw32alp svf, final String fscd, final List printList) {
        //★出力は学校毎なので、"全て"を選択した場合、各学校ごとに出力処理を行う。
        int recCnt = 0;
        int p1OutCnt = 1;
        int lrFlg = 1;
        final int maxRowCnt = 18;
        final int maxColCnt = 2;
        final int maxCnt = maxRowCnt * maxColCnt;
        setTitle(svf, db2, fscd);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
        	if (p1OutCnt > maxCnt) {
        		p1OutCnt = 1;
                //setTitle(svf, db2);
            	//svf.VrsOut("TOTAL_CNT", data);
                //svf.VrEndRecord();
        		svf.VrsOut("KARA", "aaa");
                svf.VrEndRecord();
                svf.VrsOut("TEST_DATE", KNJ_EditDate.h_format_JP_MD(_param._testDate));
                svf.VrEndRecord();
        	    svf.VrEndPage();
                svf.VrSetForm("KNJL581A.frm", 4);
                setTitle(svf, db2, fscd);
        	}
        	//受験番号
            final PrintData printData = (PrintData) iterator.next();
            svf.VrsOut("EXAMNO"+lrFlg, printData._receptNo);
            //氏名
            final int nlen = KNJ_EditEdit.getMS932ByteLength(printData._name);
            final String nfield = nlen > 30 ? "_3" : nlen > 20 ? "_2" : "";
            svf.VrsOut("NAME"+lrFlg + nfield, printData._name);
            //合否
            svf.VrsOut("JUDGE"+lrFlg, printData._gouhi);
            //種別
            svf.VrsOut("KIND"+lrFlg, printData._gkind);

            if (lrFlg == 1) {
            	lrFlg = 2;
            } else {
            	lrFlg = 1;
                svf.VrEndRecord();
            }
            recCnt++;
            p1OutCnt++;
            _hasData = true;
        }
        if (_hasData) {
        	//ページ終了処理
    		svf.VrsOut("KARA", "aaa");
        	svf.VrsOut("TOTAL_CNT", "志願者総数 " + String.valueOf(recCnt) + " 名");
            svf.VrEndRecord();
            svf.VrsOut("TEST_DATE", KNJ_EditDate.h_format_JP(db2, _param._testDate));
            svf.VrEndRecord();
        }
    }

    private void setTitle(final Vrw32alp svf, final DB2UDB db2, final String fscd) {
        final String setYear = KNJ_EditDate.h_format_JP_N(db2, _param._ObjYear + "/04/01");
        final Map fsInfo = getFinSchoolInfo(db2, fscd);
        svf.VrsOut("NENDO", setYear+"度 ");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._setDate));
        svf.VrsOut("FINSCHOOL_NAME", StringUtils.defaultString((String)fsInfo.get(fscd), "") + "長　　様");
        svf.VrsOut("SCHOOL_NAME", StringUtils.defaultString((String)fsInfo.get("SCHOOL_NAME"), ""));
        svf.VrsOut("JOB_NAME", StringUtils.defaultString((String)fsInfo.get("JOB_NAME"), ""));
        svf.VrsOut("PRINCIPAL_NAME", StringUtils.defaultString((String)fsInfo.get("PRINCIPAL_NAME"), ""));
    	svf.VrsOut("TITLE", setYear + "度 桜花学園高等学校入学試験の結果について（ご通知）");
        svf.VrsOut("SENTENCE", setYear+"度入学試験における貴校受験者は下記の結果になりましたので、ご通知申し ");
        svf.VrsOut("CODE", StringUtils.defaultString(fscd, ""));
    }

    private Map getFinSchoolInfo(final DB2UDB db2, final String fscd) {
    	Map retMap = new HashMap();
    	StringBuffer stb = new StringBuffer();
    	stb.append(" SELECT ");
    	stb.append("   T2.* ");
    	stb.append(" FROM ");
    	stb.append("   ENTEXAM_SCHOOL_MST T1 ");
    	stb.append("   LEFT JOIN FINSCHOOL_MST T2 ");
    	stb.append("     ON T2.FINSCHOOLCD = T1.FINSCHOOLCD ");
    	stb.append(" WHERE ");
    	stb.append("   T1.ENTEXAMYEAR = '" + _param._ObjYear + "' ");
    	stb.append("   AND T1.ENTEXAM_SCHOOLCD = '" + fscd + "' ");
    	log.debug(stb.toString());
    	final List recordList = KnjDbUtils.query(db2, stb.toString());
        final String keyColumn = "FINSCHOOLCD";
        final String valueColumn = "FINSCHOOL_NAME";
        for (final Iterator it = recordList.iterator(); it.hasNext();) {
            final Map row = (Map) it.next();
            retMap.put(fscd, KnjDbUtils.getString(row, valueColumn));
        }

    	StringBuffer stf = new StringBuffer();
    	stf.append(" SELECT ");
    	stf.append("   T1.* ");
    	stf.append(" FROM ");
    	stf.append("   CERTIF_SCHOOL_DAT T1 ");
    	stf.append(" WHERE ");
    	stf.append("   T1.CERTIF_KINDCD = '106' ");
    	stf.append("   AND T1.YEAR = '" + _param._loginYear + "' ");
    	log.debug(stf.toString());
    	final List recordList2 = KnjDbUtils.query(db2, stf.toString());
        final String keyColumn2 = "SCHOOL_NAME";
        final String keyColumn3 = "JOB_NAME";
        final String keyColumn4 = "PRINCIPAL_NAME";
        for (final Iterator its = recordList2.iterator(); its.hasNext();) {
            final Map row = (Map) its.next();
            retMap.put(keyColumn2, KnjDbUtils.getString(row, keyColumn2));
            retMap.put(keyColumn3, KnjDbUtils.getString(row, keyColumn3));
            retMap.put(keyColumn4, KnjDbUtils.getString(row, keyColumn4));
        }

    	return retMap;
    }

    private Map getPrintDataMap(final DB2UDB db2, final Map subclsCdMap) {
        final Map retMap = new LinkedMap();
        List addList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql(subclsCdMap);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String fsCd = rs.getString("FS_CD");
            	final String receptNo = rs.getString("RECEPTNO");
            	final String name = rs.getString("NAME");
            	final String gouhi = rs.getString("GOUHI");
            	final String gkind = rs.getString("GKIND");
                final PrintData printData = new PrintData(fsCd, receptNo, name, gouhi, gkind);
                if (!retMap.containsKey(fsCd)) {
                    addList = new ArrayList();
                    retMap.put(fsCd, addList);
                }
                addList.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private Map getNameMstIdName(final DB2UDB db2) {
        Map retMap = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getNameMstIdNameSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	retMap.put(rs.getString("NAMECD2"), rs.getString("ABBV2"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getNameMstIdNameSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     L008.NAMECD2, ");
        stb.append("     L008.ABBV2 ");
        stb.append(" FROM ");
        stb.append("     NAME_MST L008 ");
        stb.append(" WHERE ");
        stb.append("     L008.NAMECD1 = 'L008' ");
        stb.append("     AND L008.NAMESPARE1 = '1' ");
        stb.append(" ORDER BY L008.NAMECD2 ");
        return stb.toString();
    }
    private String getSql(final Map subclsCdMap) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T2.FS_CD, ");  //4桁
        stb.append("     T1.RECEPTNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     CASE WHEN T2.JUDGEMENT = '1' AND T2.SUC_COURSECODE IS NOT NULL THEN ");  //合否確定条件
        stb.append("          CASE WHEN BASEDTL01.REMARK10 = T2.SUC_COURSECODE THEN HOPEMST_SUC.PASS_NAME ");   //合格
        stb.append("               ELSE L013.NAME1 || '(' || HOPEMST_SUC.NOT_PASS_NAME || ')' ");  //不合格(他に合格)
        stb.append("               END ");
        stb.append("     WHEN T2.JUDGEMENT = '2' THEN L013.NAME1 ");  //不合格        
        stb.append("     WHEN T1.JUDGEDIV = '4' OR T1.JUDGEDIV IS NULL THEN L013_2.NAME1 ");  //欠席
        stb.append("     ELSE NULL ");  //不合格, それ以外
        stb.append("     END AS GOUHI, "); //合否
        stb.append("     VALUE(NML025.NAME2, '') || VALUE(NML066.NAME2, '') AS GKIND ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T2 ");
        stb.append("       ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("      AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND T2.TESTDIV = T1.TESTDIV AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL01 ");
        stb.append("       ON BASEDTL01.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("      AND BASEDTL01.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("      AND BASEDTL01.EXAMNO = T2.EXAMNO ");
        stb.append("      AND BASEDTL01.SEQ = '001' ");
        stb.append("     LEFT JOIN ENTEXAM_HOPE_COURSE_MST HOPEMST_SUC ");
        stb.append("       ON HOPEMST_SUC.HOPE_COURSECODE = BASEDTL01.REMARK10 ");
        stb.append("     LEFT JOIN NAME_MST L013 ");
        stb.append("       ON L013.NAMECD1 = 'L013' ");
        stb.append("      AND L013.NAMECD2 = T2.JUDGEMENT ");
        stb.append("     LEFT JOIN NAME_MST L013_2 ");
        stb.append("       ON L013_2.NAMECD1 = 'L013' ");
        stb.append("      AND L013_2.NAMECD2 = VALUE(T1.JUDGEDIV, '4') ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL30 ");
        stb.append("       ON BASEDTL30.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("      AND BASEDTL30.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("      AND BASEDTL30.EXAMNO = T2.EXAMNO ");
        stb.append("      AND BASEDTL30.SEQ = '030' ");
        stb.append("     LEFT JOIN NAME_MST NML025 ");
        stb.append("       ON NML025.NAMECD1 = 'L025' ");
        stb.append("      AND NML025.NAMECD2 = BASEDTL30.REMARK1 ");
        stb.append("     LEFT JOIN NAME_MST NML066 ");
        stb.append("       ON NML066.NAMECD1 = 'L066' ");
        stb.append("      AND NML066.NAMECD2 = BASEDTL30.REMARK2 ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._ObjYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "' ");
        if ("2".equals(_param._output)) {
        	stb.append("  AND T2.FS_CD = '" + _param._fscd + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T2.FS_CD, ");  //4桁
        stb.append("     T1.RECEPTNO ");

        return stb.toString();
    }

    private class PrintData {
		final String _fsCd;
		final String _receptNo;
		final String _name;
		final String _gouhi;
		final String _gkind;
		public PrintData(
        		final String fsCd,
        		final String receptNo,
        		final String name,
        		final String gouhi,
        		final String gkind
        ) {
            _fsCd = fsCd;
            _receptNo = receptNo;
            _name = name;
            _gouhi = gouhi;
            _gkind = gkind;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 73852 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginDate;

        private final String _ObjYear;
        private final String _applicantDiv;
        private final String _testDiv;

        private final String _output;
        private final String _fscd;
        private final String _setDate;
        private final String _testDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear       = request.getParameter("LOGIN_YEAR");
            _loginDate       = request.getParameter("LOGIN_DATE");
            _ObjYear         = request.getParameter("ENTEXAMYEAR");
            _applicantDiv    = request.getParameter("APPLICANTDIV");
            _testDiv    	 = request.getParameter("TESTDIV");
            _output          = request.getParameter("OUTPUT");
            _fscd            = request.getParameter("FS_CD");
            _setDate         = request.getParameter("SET_DATE").replace('/', '-');
            _testDate        = request.getParameter("TEST_DATE").replace('/', '-');
        }
    }
}

// eof
