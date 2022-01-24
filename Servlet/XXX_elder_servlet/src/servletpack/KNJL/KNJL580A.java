/*
 * $Id: 79e1d0a1d13ba1db552eeb7b49a175b945faeaeb $
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

public class KNJL580A {

    private static final Log log = LogFactory.getLog(KNJL580A.class);
    private static final String CONST_SELALL = "99999";
    private static final String CONST_FAILER = "99998";

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
        svf.VrSetForm("KNJL580A.frm", 4);
    	final Map subclsCdMap = getNameMstIdName(db2);
        final List printList = getList(db2, subclsCdMap);

        int colCnt = 1;
        final int maxCnt = 50;
        setTitle(svf, db2, subclsCdMap);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
        	if (colCnt > maxCnt) {
        	    //svf.VrEndPage();
        	    colCnt = 1;
                //setTitle(svf, db2);
        	}
        	//受験番号
            final PrintData printData = (PrintData) iterator.next();
            svf.VrsOut("EXAMNO", printData._receptNo);
            //氏名
            final int nlen = KNJ_EditEdit.getMS932ByteLength(printData._name);
            final String nfield = nlen > 14 ? "_3" : nlen > 10 ? "_2" : "";
            svf.VrsOut("NAME" + nfield, printData._name);
            //出身中学
            final int fnlen = KNJ_EditEdit.getMS932ByteLength(printData._finschoolNameAbbv);
            final String fnfield = fnlen > 14 ? "_3" : fnlen > 10 ? "_2" : "";
            svf.VrsOut("FINSCHOOLNAME" + fnfield, printData._finschoolNameAbbv);
            //(内申)5科
            svf.VrsOut("N_TOTAL5", printData._total5);
            //内申
            int idx = 1;
            //内申点につていは5科目用なので、DBからデータ取得時に格納先を詰めているので、注意。
            if (subclsCdMap.size() > 0) {
            	svf.VrsOut("N_SCORE" + idx, StringUtils.defaultString(printData._confidential_Rpt01, "0"));
            	idx++;
            }
            if (subclsCdMap.size() > 1) {
            	svf.VrsOut("N_SCORE" + idx, StringUtils.defaultString(printData._confidential_Rpt02, "0"));
            	idx++;
            }
            if (subclsCdMap.size() > 2) {
            	svf.VrsOut("N_SCORE" + idx, StringUtils.defaultString(printData._confidential_Rpt03, "0"));
            	idx++;
            }
            if (subclsCdMap.size() > 3) {
            	svf.VrsOut("N_SCORE" + idx, StringUtils.defaultString(printData._confidential_Rpt04, "0"));
            	idx++;
            }
            if (subclsCdMap.size() > 4) {
            	svf.VrsOut("N_SCORE" + idx, StringUtils.defaultString(printData._confidential_Rpt05, "0"));
            	idx++;
            }
            if (subclsCdMap.size() > 5) {
            	svf.VrsOut("N_SCORE" + idx, StringUtils.defaultString(printData._confidential_Rpt06, "0"));
            	idx++;
            }
            if (subclsCdMap.size() > 6) {
            	svf.VrsOut("N_SCORE" + idx, StringUtils.defaultString(printData._confidential_Rpt07, "0"));
            	idx++;
            }
            if (subclsCdMap.size() > 7) {
            	svf.VrsOut("N_SCORE" + idx, StringUtils.defaultString(printData._confidential_Rpt08, "0"));
            	idx++;
            }
            if (subclsCdMap.size() > 8) {
            	svf.VrsOut("N_SCORE" + idx, StringUtils.defaultString(printData._confidential_Rpt09, "0"));
            	idx++;
            }
            //(得点)合計
            svf.VrsOut("TOTAL3", printData._total4);
            //順位
            svf.VrsOut("RANK3", printData._total_Rank4);
            //確約者
            svf.VrsOut("KAKUYAKU", printData._promisePaper);
            //基準外
            svf.VrsOut("KIJUNGAI", printData._notStandard);
            //希望
            svf.VrsOut("HOPE_COURSE", printData._hope_Name);
            //判定結果
            svf.VrsOut("JUDGE", printData._result);
            //奨学生
            svf.VrsOut("SCHOLARSHIP", printData._shougaku);
            //特進勧誘
            svf.VrsOut("INVITATION", printData._tokutaikanyuu);

            svf.VrEndRecord();
            colCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final Vrw32alp svf, final DB2UDB db2, final Map subclsCdMap) {
        final String setYear = KNJ_EditDate.h_format_JP_N(db2, _param._ObjYear + "/04/01");
        svf.VrsOut("NENDO", setYear+"度 ");
        svf.VrsOut("PASS_COURSE", (CONST_SELALL.equals(_param._passCourse) ? "全て" : CONST_FAILER.equals(_param._passCourse) ? "不合格" : _param._searchCourseName));
        svf.VrsOut("SORT", "2".equals(_param._sort) ? "得点合計順" : "受験番号順");

    	int idxCnt = 1;
    	for (Iterator ite = subclsCdMap.keySet().iterator();ite.hasNext();) {
    		final String kStr = (String)ite.next();
    		final String colName = (String)subclsCdMap.get(kStr);
    	    svf.VrsOut("CLASSNAME" + idxCnt, colName);
    	    idxCnt++;
    	}
    }

    private List getList(final DB2UDB db2, final Map subclsCdMap) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql(subclsCdMap);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String receptNo = rs.getString("RECEPTNO");
            	final String name = rs.getString("NAME");
            	final String finschoolNameAbbv = rs.getString("FINSCHOOL_ABBV");
            	final String total5 = rs.getString("TOTAL5");

            	Iterator ite = subclsCdMap.keySet().iterator();
            	final String crwk1 = getMapCode(subclsCdMap, ite, 0);
            	final String confidential_Rpt01 = !"".equals(crwk1) ? rs.getString("CONFIDENTIAL_RPT" + crwk1) : "";
            	final String crwk2 = getMapCode(subclsCdMap, ite, 1);
            	final String confidential_Rpt02 = !"".equals(crwk2) ? rs.getString("CONFIDENTIAL_RPT" + crwk2) : "";
            	final String crwk3 = getMapCode(subclsCdMap, ite, 2);
            	final String confidential_Rpt03 = !"".equals(crwk3) ? rs.getString("CONFIDENTIAL_RPT" + crwk3) : "";
            	final String crwk4 = getMapCode(subclsCdMap, ite, 3);
            	final String confidential_Rpt04 = !"".equals(crwk4) ? rs.getString("CONFIDENTIAL_RPT" + crwk4) : "";
            	final String crwk5 = getMapCode(subclsCdMap, ite, 4);
            	final String confidential_Rpt05 = !"".equals(crwk5) ? rs.getString("CONFIDENTIAL_RPT" + crwk5) : "";
            	final String crwk6 = getMapCode(subclsCdMap, ite, 5);
            	final String confidential_Rpt06 = !"".equals(crwk6) ? rs.getString("CONFIDENTIAL_RPT" + crwk6) : "";
            	final String crwk7 = getMapCode(subclsCdMap, ite, 6);
            	final String confidential_Rpt07 = !"".equals(crwk7) ? rs.getString("CONFIDENTIAL_RPT" + crwk7) : "";
            	final String crwk8 = getMapCode(subclsCdMap, ite, 7);
            	final String confidential_Rpt08 = !"".equals(crwk8) ? rs.getString("CONFIDENTIAL_RPT" + crwk8) : "";
            	final String crwk9 = getMapCode(subclsCdMap, ite, 8);
            	final String confidential_Rpt09 = !"".equals(crwk9) ? rs.getString("CONFIDENTIAL_RPT" + crwk9) : "";
            	final String total4 = rs.getString("TOTAL4");
            	final String total_Rank4 = rs.getString("TOTAL_RANK4");
            	final String promisePaper = rs.getString("PROMISEPAPER");
            	final String notStandard = rs.getString("NOT_STANDARD");
            	final String hope_Name = rs.getString("HOPE_NAME");
            	final String result = rs.getString("RESULT");
            	final String shougaku = rs.getString("SHOUGAKU");
            	final String tokutaikanyuu = rs.getString("TOKUTAIKANYUU");

                final PrintData printData = new PrintData(receptNo, name, finschoolNameAbbv, total5, confidential_Rpt01, confidential_Rpt02,
                                                           confidential_Rpt03, confidential_Rpt04, confidential_Rpt05, confidential_Rpt06, confidential_Rpt07,
                                                           confidential_Rpt08, confidential_Rpt09, total4, total_Rank4, promisePaper, notStandard, hope_Name,
                                                           result, shougaku, tokutaikanyuu);
                retList.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getMapCode(final Map subclsCdMap, final Iterator ite, final int chkCnt) {
    	String retStr = "";
    	if (subclsCdMap.size() > chkCnt) {
    		retStr = (String)ite.next();
    	} else {
    		retStr = "";
    	}
    	return retStr;
    }

    private Map getNameMstIdName(final DB2UDB db2) {
        Map retMap = new LinkedMap();
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
        stb.append(" WITH CRPT_DETAIL_CHL AS ( ");
        stb.append("     SELECT ");
        stb.append("       CRPT.ENTEXAMYEAR, ");
        stb.append("       CRPT.APPLICANTDIV, ");
        stb.append("       CRPT.EXAMNO, ");
        stb.append("       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '01' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT01 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_01, ");
        stb.append("       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '02' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT02 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_02, ");
        stb.append("       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '03' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT03 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_03, ");
        stb.append("       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '04' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT04 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_04, ");
        stb.append("       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '05' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT05 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_05, ");
        stb.append("       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '06' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT06 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_06, ");
        stb.append("       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '07' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT07 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_07, ");
        stb.append("       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '08' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT08 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_08, ");
        stb.append("       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '09' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT09 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_09, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '01' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT01 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_01, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '02' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT02 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_02, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '03' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT03 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_03, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '04' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT04 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_04, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '05' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT05 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_05, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '06' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT06 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_06, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '07' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT07 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_07, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '08' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT08 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_08, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '09' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT09 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_09, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '01') AND CRPT.CONFIDENTIAL_RPT01 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_01, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '02') AND CRPT.CONFIDENTIAL_RPT02 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_02, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '03') AND CRPT.CONFIDENTIAL_RPT03 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_03, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '04') AND CRPT.CONFIDENTIAL_RPT04 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_04, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '05') AND CRPT.CONFIDENTIAL_RPT05 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_05, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '06') AND CRPT.CONFIDENTIAL_RPT06 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_06, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '07') AND CRPT.CONFIDENTIAL_RPT07 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_07, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '08') AND CRPT.CONFIDENTIAL_RPT08 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_08, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '09') AND CRPT.CONFIDENTIAL_RPT09 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_09 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RCPT ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("       ON BASE.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
        stb.append("      AND BASE.APPLICANTDIV = RCPT.APPLICANTDIV ");
        stb.append("      AND BASE.TESTDIV = RCPT.TESTDIV ");
        stb.append("      AND BASE.EXAMNO = RCPT.EXAMNO ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDAT ");
        stb.append("      ON BASEDAT.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
        stb.append("     AND BASEDAT.APPLICANTDIV = RCPT.APPLICANTDIV ");
        stb.append("     AND BASEDAT.EXAMNO = RCPT.EXAMNO ");
        stb.append("     AND BASEDAT.SEQ = '001' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CRPT ");
        stb.append("       ON CRPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("      AND CRPT.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("      AND CRPT.EXAMNO = BASE.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_CONFRPT_BASE_MST ECBM ");
        stb.append("       ON ECBM.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("      AND ECBM.TESTDIV = BASE.TESTDIV ");
        stb.append("      AND ECBM.HOPE_COURSECODE = BASEDAT.REMARK10 ");
        stb.append(" WHERE ");
        stb.append("     RCPT.ENTEXAMYEAR = '" + _param._ObjYear + "' ");
        stb.append("     AND RCPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.RECEPTNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     FM.FINSCHOOL_NAME_ABBV AS FINSCHOOL_ABBV, ");
        stb.append("     CRPT.TOTAL5, ");
        //5科目だが、5科目がどの番号の物かは別テーブル管理なので、別途取得したリストを利用して設定。
        for (Iterator ite = subclsCdMap.keySet().iterator();ite.hasNext();) {
        	final String val = (String)ite.next();
        	stb.append("     CRPT.CONFIDENTIAL_RPT" + val + ", ");
        }
        stb.append("     T1.TOTAL4, ");
        stb.append("     T1.TOTAL_RANK4, ");
        stb.append("     CASE WHEN BASEDTL04.REMARK8 = '1' THEN 'あり' ELSE '' END AS PROMISEPAPER, ");
        stb.append("     CASE WHEN ");
        stb.append("          (CASE WHEN CRPT_CHK.CRPT_CHKC_01 = 2 OR (CRPT_CHK.CRPT_CHKC_01 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT01, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHKC_02 = 2 OR (CRPT_CHK.CRPT_CHKC_02 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT02, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHKC_03 = 2 OR (CRPT_CHK.CRPT_CHKC_03 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT03, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHKC_04 = 2 OR (CRPT_CHK.CRPT_CHKC_04 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT04, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHKC_05 = 2 OR (CRPT_CHK.CRPT_CHKC_05 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT05, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHKC_06 = 2 OR (CRPT_CHK.CRPT_CHKC_06 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT06, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHKC_07 = 2 OR (CRPT_CHK.CRPT_CHKC_07 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT07, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHKC_08 = 2 OR (CRPT_CHK.CRPT_CHKC_08 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT08, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHKC_09 = 2 OR (CRPT_CHK.CRPT_CHKC_09 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT09, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ");
        stb.append("          ) > 0 THEN '＊' ");
        stb.append("          WHEN ");
        stb.append("          (CASE WHEN CRPT_CHK.CRPT_CHK5_01 = 1 THEN CRPT.CONFIDENTIAL_RPT01 ELSE 0 END  ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK5_02 = 1 THEN CRPT.CONFIDENTIAL_RPT02 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK5_03 = 1 THEN CRPT.CONFIDENTIAL_RPT03 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK5_04 = 1 THEN CRPT.CONFIDENTIAL_RPT04 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK5_05 = 1 THEN CRPT.CONFIDENTIAL_RPT05 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK5_06 = 1 THEN CRPT.CONFIDENTIAL_RPT06 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK5_07 = 1 THEN CRPT.CONFIDENTIAL_RPT07 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK5_08 = 1 THEN CRPT.CONFIDENTIAL_RPT08 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK5_09 = 1 THEN CRPT.CONFIDENTIAL_RPT09 ELSE 0 END ");
        stb.append("          ) < ECBM.SCORE5 THEN '＊' ");
        stb.append("          WHEN ");
        stb.append("          (CASE WHEN CRPT_CHK.CRPT_CHK9_01 = 1 THEN CRPT.CONFIDENTIAL_RPT01 ELSE 0 END  ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK9_02 = 1 THEN CRPT.CONFIDENTIAL_RPT02 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK9_03 = 1 THEN CRPT.CONFIDENTIAL_RPT03 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK9_04 = 1 THEN CRPT.CONFIDENTIAL_RPT04 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK9_05 = 1 THEN CRPT.CONFIDENTIAL_RPT05 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK9_06 = 1 THEN CRPT.CONFIDENTIAL_RPT06 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK9_07 = 1 THEN CRPT.CONFIDENTIAL_RPT07 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK9_08 = 1 THEN CRPT.CONFIDENTIAL_RPT08 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK9_09 = 1 THEN CRPT.CONFIDENTIAL_RPT09 ELSE 0 END ");
        stb.append("          ) < ECBM.SCORE9 THEN '＊' ");
        stb.append("          ELSE '' ");
        stb.append("          END AS NOT_STANDARD, ");
        stb.append("     HOPEMST_SUC.HOPE_NAME, ");
        stb.append("     L013.NAME1 ");
        stb.append("     || CASE WHEN T2.JUDGEMENT = '1' AND T2.SUC_COURSECODE IS NOT NULL ");
        stb.append("                  AND BASEDTL01.REMARK10 <> T2.SUC_COURSECODE ");
        stb.append("                  THEN '(' || HOPEMST_SUC.NOT_PASS_NAME || ')' ");
        stb.append("             ELSE '' ");
        stb.append("        END AS RESULT, ");
        stb.append("     NML025.NAME2 AS SHOUGAKU, ");
        stb.append("     NML066.NAME1 AS TOKUTAIKANYUU ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T2 ");
        stb.append("       ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("      AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND T2.TESTDIV = T1.TESTDIV AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_SCHOOL_MST ESM ");
        stb.append("       ON ESM.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("      AND ESM.ENTEXAM_SCHOOLCD = T2.FS_CD ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FM ");
        stb.append("       ON FM.FINSCHOOLCD = ESM.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL01 ");
        stb.append("       ON BASEDTL01.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("      AND BASEDTL01.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("      AND BASEDTL01.EXAMNO = T2.EXAMNO ");
        stb.append("      AND BASEDTL01.SEQ = '001' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CRPT ");
        stb.append("       ON CRPT.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("      AND CRPT.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("      AND CRPT.EXAMNO = T2.EXAMNO ");
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
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL04 ");
        stb.append("       ON BASEDTL04.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("      AND BASEDTL04.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("      AND BASEDTL04.EXAMNO = T2.EXAMNO ");
        stb.append("      AND BASEDTL04.SEQ = '004' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL22 ");
        stb.append("       ON BASEDTL22.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("      AND BASEDTL22.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("      AND BASEDTL22.EXAMNO = T2.EXAMNO ");
        stb.append("      AND BASEDTL22.SEQ = '022' ");
        stb.append("     LEFT JOIN ENTEXAM_CONFRPT_BASE_MST ECBM ");
        stb.append("       ON ECBM.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("      AND ECBM.TESTDIV = T2.TESTDIV ");
        stb.append("      AND ECBM.HOPE_COURSECODE = BASEDTL01.REMARK10 ");
        stb.append("     LEFT JOIN ENTEXAM_HOPE_COURSE_MST HOPEMST_SUC ");
        stb.append("       ON HOPEMST_SUC.HOPE_COURSECODE = BASEDTL01.REMARK10 ");
        stb.append("     LEFT JOIN NAME_MST L013 ");
        stb.append("       ON L013.NAMECD1 = 'L013' ");
        stb.append("      AND L013.NAMECD2 = T2.JUDGEMENT ");
        stb.append("     LEFT JOIN CRPT_DETAIL_CHL CRPT_CHK ");
        stb.append("       ON CRPT_CHK.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("      AND CRPT_CHK.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND CRPT_CHK.EXAMNO = T1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._ObjYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!CONST_SELALL.equals(_param._passCourse)) {
            if (!"".equals(_param._passCourse) && !CONST_FAILER.equals(_param._passCourse)) { //CONST_FAILER("99998") != "0004"なので、問題無いはず。
            	stb.append("     AND T2.SUC_COURSECODE = '" + _param._passCourse+ "' ");
            } else {
            	stb.append("     AND T2.JUDGEMENT IN ('2', '4') ");
            }
        }
        stb.append(" ORDER BY ");
        if ("2".equals(_param._sort)) {
            stb.append(" T1.TOTAL4 IS NULL ASC, ");
            stb.append(" T1.TOTAL4 DESC, ");
        }
        stb.append(" T1.RECEPTNO ");

        return stb.toString();
    }

    private class PrintData {
    	final String _receptNo;
    	final String _name;
    	final String _finschoolNameAbbv;
    	final String _total5;
    	final String _confidential_Rpt01;
    	final String _confidential_Rpt02;
    	final String _confidential_Rpt03;
    	final String _confidential_Rpt04;
    	final String _confidential_Rpt05;
    	final String _confidential_Rpt06;
    	final String _confidential_Rpt07;
    	final String _confidential_Rpt08;
    	final String _confidential_Rpt09;
    	final String _total4;
    	final String _total_Rank4;
    	final String _promisePaper;
    	final String _notStandard;
    	final String _hope_Name;
    	final String _result;
    	final String _shougaku;
    	final String _tokutaikanyuu;
    	public PrintData(
        		final String receptNo,
        		final String name,
        		final String finschoolNameAbbv,
        		final String total5,
        		final String confidential_Rpt01,
        		final String confidential_Rpt02,
        		final String confidential_Rpt03,
        		final String confidential_Rpt04,
        		final String confidential_Rpt05,
        		final String confidential_Rpt06,
        		final String confidential_Rpt07,
        		final String confidential_Rpt08,
        		final String confidential_Rpt09,
        		final String total4,
        		final String total_Rank4,
        		final String promisePaper,
        		final String notStandard,
        		final String hope_Name,
        		final String result,
        		final String shougaku,
        		final String tokutaikanyuu
        ) {
    		_receptNo = receptNo;
    		_name = name;
    		_finschoolNameAbbv = finschoolNameAbbv;
    		_total5 = total5;
    		_confidential_Rpt01 = confidential_Rpt01;
    		_confidential_Rpt02 = confidential_Rpt02;
    		_confidential_Rpt03 = confidential_Rpt03;
    		_confidential_Rpt04 = confidential_Rpt04;
    		_confidential_Rpt05 = confidential_Rpt05;
    		_confidential_Rpt06 = confidential_Rpt06;
    		_confidential_Rpt07 = confidential_Rpt07;
    		_confidential_Rpt08 = confidential_Rpt08;
    		_confidential_Rpt09 = confidential_Rpt09;
    		_total4 = total4;
    		_total_Rank4 = total_Rank4;
    		_promisePaper = promisePaper;
    		_notStandard = notStandard;
    		_hope_Name = hope_Name;
    		_result = result;
    		_shougaku = shougaku;
    		_tokutaikanyuu = tokutaikanyuu;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70852 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginSemester;
        private final String _loginDate;

        private final String _ObjYear;
        private final String _applicantDiv;

        private final String _passCourse;
        private final String _searchCourseName;
        private final String _sort;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear       = request.getParameter("LOGIN_YEAR");
            _loginSemester   = request.getParameter("LOGIN_SEMESTER");
            _loginDate       = request.getParameter("LOGIN_DATE");
            _ObjYear         = request.getParameter("ENTEXAMYEAR");
            _applicantDiv    = request.getParameter("APPLICANTDIV");
            _passCourse         = request.getParameter("PASS_COURSE");
            _sort          = request.getParameter("SORT");
            _searchCourseName = getHopeCourseCodeStr(db2);
        }

        private String getHopeCourseCodeStr(final DB2UDB db2) {
        	String retStr = "";
        	if (!CONST_SELALL.equals(_passCourse) && !CONST_FAILER.equals(_passCourse)) {
	        	StringBuffer stb = new StringBuffer();
	        	stb.append(" SELECT DISTINCT ");
	        	stb.append("   T2.HOPE_NAME ");  // 志望区分コード
	        	stb.append(" FROM ");
	        	stb.append("   ENTEXAM_HOPE_COURSE_YDAT T1 ");
	        	stb.append("   LEFT JOIN ENTEXAM_HOPE_COURSE_MST T2 ");
	        	stb.append("     ON T2.HOPE_COURSECODE = T1.HOPE_COURSECODE ");
	        	stb.append(" WHERE ");
	        	stb.append("   ENTEXAMYEAR = '" + _ObjYear + "' ");
	        	stb.append("   AND T1.HOPE_COURSECODE = '" + _passCourse + "' ");
	        	final String sql =  stb.toString();

	            PreparedStatement ps = null;
	            ResultSet rs = null;
	            try {
	                ps = db2.prepareStatement(sql);
	                rs = ps.executeQuery();
	                while (rs.next()) {
	                	retStr = rs.getString("HOPE_NAME");
	                }
	            } catch (Exception e) {
	                log.error("exception!", e);
	            } finally {
	                DbUtils.closeQuietly(null, ps, rs);
	                db2.commit();
	            }
            }
            return retStr;
        }
    }
}

// eof
