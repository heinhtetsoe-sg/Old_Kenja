/*
 * $Id: e283b4f7384aef0735487b49b1b6e673ce1f4a59 $
 *
 * 作成日: 2017/05/01
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJA227A {

    private static final Log log = LogFactory.getLog(KNJA227A.class);

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
        final String form = "KNJA227A.frm";
        final String nendo = KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度";
        final String title = "塾別在校生数・来校者数";

        final List datList_J = getList(db2, sqlDataList("J"));
        final List datList_H = getList(db2, sqlDataList("H"));
        final List mergeList_JH = mergeList(datList_J, datList_H);
        final List regdListJHPageList = getPageList(mergeList_JH, 35);
        if (regdListJHPageList.size() == 0) {
        	return;
        }
        final int maxPage = Math.max(regdListJHPageList.size(), 1);

        int line = 1;

        for (int dataPageIdx = 0; dataPageIdx < maxPage; dataPageIdx++) {
            svf.VrSetForm(form, 1);
            svf.VrsOut("TITLE", nendo + "　" + title);
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));

            line = 1;
            List pagelst = (List) regdListJHPageList.get(dataPageIdx);
            for (int pi = 0; pi < pagelst.size(); pi++) {
                final Map pMap = (Map) pagelst.get(pi);

                svf.VrsOutn("PRISCHOOL_CD", line, getString(pMap, "PRISCHOOLCD"));
                svf.VrsOutn("PRISCHOOL_CLASS_CD", line, getString(pMap, "PRISCHOOL_CLASS_CD"));
                String psnfield = getMS932Bytecount(getString(pMap, "PRISCHOOL_NAME")) <= 20 ? "1" : getMS932Bytecount(getString(pMap, "PRISCHOOL_NAME")) <= 30 ? "2" : "3";
                svf.VrsOutn("PRISCHOOL_NAME" + psnfield, line, getString(pMap, "PRISCHOOL_NAME"));
                String pscnfield = getMS932Bytecount(getString(pMap, "PRISCHOOL_CLASS_NAME")) <= 20 ? "1" : getMS932Bytecount(getString(pMap, "PRISCHOOL_CLASS_NAME")) <= 30 ? "2" : "3";
                svf.VrsOutn("PRISCHOOL_CLASS_NAME" + pscnfield, line, getString(pMap, "PRISCHOOL_CLASS_NAME"));
                final String setdistrict = getString(pMap, "DISTRICTCD");
                svf.VrsOutn("PRISCHOOL_ADDR", line, setdistrict);
                Set keyset = pMap.keySet();
                if (keyset.contains("ZAIKOCNT_J")) {
                    svf.VrsOutn("ENROLL1", line, getString(pMap, "ZAIKOCNT_J"));
                    svf.VrsOutn("VISIT1", line, getString(pMap, "COMINGCNT_J"));
                } else {
                    svf.VrsOutn("ENROLL1", line, "0");
                    svf.VrsOutn("VISIT1", line, "0");
                }
                if (keyset.contains("ZAIKOCNT_H")) {
                    svf.VrsOutn("ENROLL2", line, getString(pMap, "ZAIKOCNT_H"));
                    svf.VrsOutn("VISIT2", line, getString(pMap, "COMINGCNT_H"));
                } else {
                    svf.VrsOutn("ENROLL2", line, "0");
                    svf.VrsOutn("VISIT2", line, "0");
                }
                svf.VrsOutn("REMARK", line, "");
                line++;
                _hasData = true;
            }
            svf.VrEndPage();
        }
    }

    private List mergeList(final List list_a, final List list_b) {
    	final List retList = new ArrayList();
    	final List listwk_a = new ArrayList();
    	final List listwk_b = new ArrayList();
    	listwk_a.addAll(list_a);
    	listwk_b.addAll(list_b);
    	String getPriSCD_a = "";
    	String getPriSCCD_a = "";
    	int findidx = 0;
    	//listwk_aをベースに登録する(listwk_bの残データは後で登録)
        for (final Iterator it = listwk_a.iterator(); it.hasNext();) {
        	Map pMap = (Map) it.next();
        	getPriSCD_a = getString(pMap, "PRISCHOOLCD");
        	getPriSCCD_a = getString(pMap, "PRISCHOOL_CLASS_CD");
        	findidx = finddata(listwk_b, getPriSCD_a, getPriSCCD_a);
        	if (findidx >= 0) {
            	final Map getMap = (Map)listwk_b.get(findidx);
        		Set keyset = getMap.keySet();
        		String baseJH = "H";
        		String useJH = "J";
        		if (keyset.contains("ZAIKOCNT_H")) {
            		baseJH = "J";
        			useJH = "H";
        		}
        		final String chkzaicnt_a = (String)pMap.get("ZAIKOCNT_" + baseJH);
        		final String chkcomcnt_a = (String)pMap.get("COMINGCNT_" + baseJH);
        		final String chkzaicnt_b = (String)getMap.get("ZAIKOCNT_" + useJH);
        		final String chkcomcnt_b = (String)getMap.get("COMINGCNT_" + useJH);
        		//全データが0は除外
        		if (!"0".equals(chkzaicnt_a) || !"0".equals(chkzaicnt_b) || !"0".equals(chkcomcnt_a) || !"0".equals(chkcomcnt_b)) {
                    pMap.put("ZAIKOCNT_"+useJH, chkzaicnt_b);
                    pMap.put("COMINGCNT_"+useJH, chkcomcnt_b);
                    retList.add(pMap);
        		}
                //残を知るために、取ったデータは排除する。
        		listwk_b.remove(findidx);
        	} else {
        		Set keyset = pMap.keySet();
        		String baseJH = "J";
        		if (keyset.contains("ZAIKOCNT_H")) {
            		baseJH = "H";
        		}
        		final String chkzaicnt_a = (String)pMap.get("ZAIKOCNT_" + baseJH);
        		final String chkcomcnt_a = (String)pMap.get("COMINGCNT_" + baseJH);
        		if (!"0".equals(chkzaicnt_a) || !"0".equals(chkcomcnt_a)) {
                    retList.add(pMap);
        		}
        	}
    	}
        //listwk_bに残っているデータを登録
        for (final Iterator it = listwk_b.iterator(); it.hasNext();) {
        	Map pMap = (Map) it.next();
    		Set keyset = pMap.entrySet();
    		String baseJH = "J";
    		if (keyset.contains("ZAIKOCNT_H")) {
        		baseJH = "H";
    		}
    		final String chkzaicnt_b = (String)pMap.get("ZAIKOCNT_" + baseJH);
    		final String chkcomcnt_b = (String)pMap.get("COMINGCNT_" + baseJH);
    		//全データが0は除外
    		if (!"0".equals(chkzaicnt_b) || !"0".equals(chkcomcnt_b)) {
                retList.add(pMap);
            }
        }
    	return retList;
    }

	private int finddata(final List listwk_b, final String getPriSCD_a, final String getPriSCCD_a) {
		int findidx = -1;
		boolean findflg = false;
		int cnt = 0;
    	String getPriSCD_b = "";
    	String getPriSCCD_b = "";
        for (final Iterator it = listwk_b.iterator(); it.hasNext();) {
        	Map pMap = (Map) it.next();
        	getPriSCD_b = getString(pMap, "PRISCHOOLCD");
        	getPriSCCD_b = getString(pMap, "PRISCHOOL_CLASS_CD");
        	if (getPriSCD_a.equals(getPriSCD_b)
        			&& ((getPriSCCD_a == null && getPriSCCD_b == null)
        					|| (getPriSCCD_a != null && getPriSCCD_b != null && getPriSCCD_a.equals(getPriSCCD_b)))) {
        		findflg = true;
        		break;
        	}
        	cnt++;
        }
        if (findflg) {
        	findidx = cnt;
        }
		return findidx;
	}

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private static String getString(final Map m, final String field) {
        if (null == m || m.isEmpty()) {
            return null;
        }
        if (!m.containsKey(field)) {
            throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
        }
        return (String) m.get(field);
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

    private List getList(final DB2UDB db2, final String sql) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    m.put(meta.getColumnLabel(i), rs.getString(meta.getColumnLabel(i)));
                }
                list.add(m);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String sqlDataList(final String schkind) {
        final StringBuffer stb = new StringBuffer();
        //ZAIKOINFO:在校生カウントデータ
        //COMINGINFO:来校者カウントデータ
        //COUNTTBL:データを集計
        //最終:名称等を紐づけ
    	stb.append(" WITH ZAIKOINFO AS ( ");
    	stb.append(" SELECT ");
    	stb.append("     T2.PRISCHOOLCD, ");
    	stb.append("     B1.BASE_REMARK1 AS PRISCHOOL_CLASS_CD, ");
    	stb.append("     T1.SCHREGNO ");
    	stb.append(" FROM ");
    	stb.append("     SCHREG_REGD_DAT T1 ");
    	stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
    	stb.append("     INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ");
    	stb.append("     LEFT JOIN SCHREG_BASE_DETAIL_MST B1 ON B1.SCHREGNO = T1.SCHREGNO ");
    	stb.append("         AND B1.BASE_SEQ = '010' ");
    	stb.append(" WHERE ");
    	stb.append("     T1.YEAR = '" + _param._year + "' ");
    	stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
    	stb.append("     AND T2.PRISCHOOLCD IS NOT NULL ");
    	stb.append("     AND T3.SCHOOL_KIND = '" + schkind + "' ");
    	stb.append(" ORDER BY ");
    	stb.append("     T1.SCHREGNO ");
    	stb.append(" ), COMINGINFO AS ( ");
    	stb.append(" SELECT DISTINCT ");
    	stb.append("     R1.PRISCHOOLCD, ");
    	stb.append("     R1.PRISCHOOL_CLASS_CD, ");
    	stb.append("     R1.RECRUIT_NO ");
    	stb.append(" FROM ");
    	stb.append("     RECRUIT_DAT R1 ");
    	stb.append(" WHERE ");
    	stb.append("     R1.YEAR = '" + _param._entexamyear + "' ");
    	stb.append("     AND R1.PRISCHOOLCD IS NOT NULL ");
    	stb.append("     AND R1.SCHOOL_KIND = '" + schkind + "' ");
    	stb.append(" ORDER BY ");
    	stb.append("     R1.RECRUIT_NO ");
    	stb.append(" ), COUNTTBL AS ( ");
    	stb.append(" SELECT ");
    	stb.append("     P1.PRISCHOOLCD, ");
    	stb.append("     P2.PRISCHOOL_CLASS_CD, ");
    	stb.append("     COUNT(D1.SCHREGNO) AS ZAIKOCNT, ");
    	stb.append("     COUNT(D2.RECRUIT_NO) AS COMINGCNT ");
    	stb.append(" FROM ");
    	stb.append("     PRISCHOOL_MST P1 ");
    	stb.append("     LEFT JOIN PRISCHOOL_CLASS_MST P2 ");
    	stb.append("          ON P2.PRISCHOOLCD = P1.PRISCHOOLCD ");
    	stb.append("     LEFT JOIN ZAIKOINFO D1 ");
    	stb.append("          ON D1.PRISCHOOLCD = P2.PRISCHOOLCD ");
    	stb.append("         AND D1.PRISCHOOL_CLASS_CD = P2.PRISCHOOL_CLASS_CD ");
    	stb.append("     LEFT JOIN COMINGINFO D2 ");
    	stb.append("          ON D2.PRISCHOOLCD = P2.PRISCHOOLCD ");
    	stb.append("         AND D2.PRISCHOOL_CLASS_CD = P2.PRISCHOOL_CLASS_CD ");
    	stb.append(" GROUP BY ");
    	stb.append("     P1.PRISCHOOLCD, ");
    	stb.append("     P2.PRISCHOOL_CLASS_CD ");
    	stb.append(" ) ");
    	stb.append(" SELECT ");
    	stb.append("     P1.PRISCHOOLCD, ");
    	stb.append("     P2.PRISCHOOL_CLASS_CD, ");
    	stb.append("     P1.PRISCHOOL_NAME AS PRISCHOOL_NAME, ");
    	stb.append("     VALUE(P2.PRISCHOOL_NAME,'') AS PRISCHOOL_CLASS_NAME, ");
    	stb.append("     P2.DISTRICTCD, ");
    	stb.append("     P2.PRISCHOOL_ADDR1, ");
    	stb.append("     P2.PRISCHOOL_ADDR2, ");
    	stb.append("     PTBL.ZAIKOCNT AS ZAIKOCNT_" + schkind + ", ");
    	stb.append("     PTBL.COMINGCNT AS COMINGCNT_" + schkind + " ");
    	stb.append(" FROM  ");
    	stb.append("     COUNTTBL PTBL ");
    	stb.append("     LEFT JOIN PRISCHOOL_MST P1 ");
    	stb.append("          ON P1.PRISCHOOLCD = PTBL.PRISCHOOLCD ");
    	stb.append("     LEFT JOIN PRISCHOOL_CLASS_MST P2 ");
    	stb.append("          ON P2.PRISCHOOLCD = PTBL.PRISCHOOLCD ");
    	stb.append("         AND P2.PRISCHOOL_CLASS_CD = PTBL.PRISCHOOL_CLASS_CD ");
    	stb.append(" ORDER BY ");
    	stb.append("     P1.PRISCHOOLCD, ");
    	stb.append("     P2.PRISCHOOL_CLASS_CD ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 59774 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _year;
        private final String _semester;
        private final String _loginDate;
        private final String _entexamyear;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _loginDate = request.getParameter("CTRL_DATE");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
        }
    }
}

// eof

