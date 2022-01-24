// kanji=漢字
/*
 * $Id: c00191bd00b9952fa22cf4eca079157b5566a15a $
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJE;

import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJE.KNJE070_1.HyoteiHeikin;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KnjDbUtils;

/*
 *  学校教育システム 賢者 [進路情報管理] 高校用調査書評定平均取得
 */

public class KNJE370IHyoteiHeikin {

	private static final Log log = LogFactory.getLog(KNJE370IHyoteiHeikin.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void fetchHyoteiHeikin(HttpServletRequest request, HttpServletResponse response) throws Exception {

    	log.fatal("$Revision: 72413 $ $Date: 2020-02-14 23:27:10 +0900 (金, 14 2 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        final Map paramMap = new KNJE070().createParamMap(request);

        // ＤＢ接続
        final String dbname = request.getParameter("DBNAME");
        final DB2UDB db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2); //Databaseクラスを継承したクラス
        try {
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        final Param param = new Param(db2, request, paramMap);
        
    	PrintStream os = null;
    	try {
    		final String json = toJson(getResultMap(db2, param, paramMap));
    		
        	response.setContentType("application/json");
        	response.setContentLength(json.length());
        	
        	os = new PrintStream(response.getOutputStream());
        	os.println(json);
    		
    	} catch (Exception e) {
    		log.error("exception!", e);
    		if (null != os) {
    			os.println("error: " + e.getMessage());
    		}
    	} finally {
    		db2.commit();
    		db2.close();
    	}
    }

	private Map<String, Map<String, List<Map<String, String>>>> getResultMap(final DB2UDB db2, final Param param, final Map paramap) {
		
    	final Map<String, String> grdDateYearSemesterMap = getGrdDateYearSemesterMap(db2, param);

		final Map<String, Map<String, List<Map<String, String>>>> resultMap = new HashMap();
		final KNJE070_1 knje070_1 = new KNJE070_1(db2, (Vrw32alp) null, (KNJDefineSchool) null, (String) null);
		
		for (final String schregno : param._schregnoArray) {
			
			final String[] split = grdDateYearSemesterMap.get(schregno).split("-");
			
			final Map callParamMap1 = new HashMap(paramap);
			callParamMap1.put("hyoteiheikin.table", "RECORD_SCORE_DAT");
			callParamMap1.put("hyoteiheikin.table.RECORD_SCORE_DAT.SEMESTER", "1");
		    final String year = split[0];
			for (final KNJE070_1.HyoteiHeikin h : knje070_1.getHyoteiHeikinList(schregno, year, "1", callParamMap1)) {
		    	h._row.remove("CLASSNAME");
		    	getMappedList(getMappedMap(resultMap, schregno), "1").add(h._row);
		    }

			final Map callParamMap2 = new HashMap(paramap);
			KNJE070_1.HyoteiHeikin total = null;
			final List<Map<String, String>> honHyoteiList = new ArrayList<Map<String, String>>();
		    final List<HyoteiHeikin> hyoteiHeikinList = knje070_1.getHyoteiHeikinList(schregno, year, split[1], callParamMap2);
	    	if (param._isOutputDebug) {
	    		log.info(" " + schregno + " hyoteiHeikinList = " + hyoteiHeikinList);
	    	}
			for (final KNJE070_1.HyoteiHeikin h : hyoteiHeikinList) {
		    	if ("TOTAL".equals(KnjDbUtils.getString(h._row, "CLASSCD"))) {
		    		total = h;
		    		continue;
		    	}
		    	for (final KNJE070_1.Grades g : h._gradesList) {
		    		if (year.equals(g._year) && !"1".equals(g._provFlg)) {
		    			honHyoteiList.add(h._row);
		    		}
		    	}
		    }
	    	if (param._isOutputDebug) {
	    		log.info("本評定数 " + schregno + " = " + honHyoteiList.size());
	    	}
		    if (null != total) {
		    	if (honHyoteiList.size() == 0) {
		    		total._row.put("AVG", null); // 評定平均をセットしない
		    		total._row.put("GAIHYO", null); // 概評をセットしない
		    	}
		    	getMappedList(getMappedMap(resultMap, schregno), "9").add(total._row);
		    }
		}
		return resultMap;
	}

	private Map<String, String> getGrdDateYearSemesterMap(final DB2UDB db2, final Param param) {
		final Map<String, String> grdDateYearSemesterMap = new HashMap<String ,String>();
    	PreparedStatement ps = null;
    	log.info(" hyoteiHeikinJson " + ArrayUtils.toString(param._schregnoArray));
    	try {
        	final StringBuffer sql = new StringBuffer();
        	sql.append(" SELECT ");
        	sql.append("     GRD_DATE ");
        	sql.append("   , SEME.YEAR ");
        	sql.append("   , SEME.SEMESTER ");
        	sql.append(" FROM SCHREG_ENT_GRD_HIST_DAT T1 ");
        	sql.append(" INNER JOIN SEMESTER_MST SEME ON SEME.SEMESTER <> '9' ");
        	sql.append("     AND T1.GRD_DATE BETWEEN SEME.SDATE AND SEME.EDATE ");
        	sql.append(" WHERE ");
        	sql.append("     SCHREGNO = ? ");
        	sql.append("     AND SCHOOL_KIND = 'H' ");
        	
        	ps = db2.prepareStatement(sql.toString());
        	for (final String schregno : param._schregnoArray) {
        		final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, ps, new Object[] { schregno}));
        		if (row.isEmpty()) {
        			grdDateYearSemesterMap.put(schregno, param._year + "-" + param._gakki);
        		} else {
        			grdDateYearSemesterMap.put(schregno, KnjDbUtils.getString(row, "YEAR") + "-" + KnjDbUtils.getString(row, "SEMESTER"));
        		}
        	}
    		
    	} catch (Exception e) {
    		log.error("exception!", e);
    	} finally {
    		DbUtils.closeQuietly(ps);
    	}
		return grdDateYearSemesterMap;
	}
    
	private static <T, K> List<T> getMappedList(final Map<K, List<T>> map, final K key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<T>());
        }
        return map.get(key1);
    }
	
    private static <K, T, U> Map<T, U> getMappedMap(final Map<K, Map<T, U>> map, final K key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap<T, U>());
        }
        return map.get(key1);
    }

	private static String toJson(final Object o) {
    	final StringBuffer stb = new StringBuffer();
    	if (o instanceof Map) {
    		final Map m = (Map) o;
    		String comma = "";
    		stb.append("{");
    		for (final Iterator it = m.entrySet().iterator(); it.hasNext();) {
    			final Map.Entry e = (Map.Entry) it.next();
    			final Object key = e.getKey();
    			final Object val = e.getValue();
    			stb.append(comma).append(toJson(key)).append(": ").append(toJson(val));
    			comma = ", ";
    		}
    		stb.append("}");
    	} else if (o instanceof List) {
    		stb.append("[");
    		String comma = "";
    		final List l = (List) o;
    		for (int i = 0; i < l.size(); i++) {
    			final Object e = l.get(i);
    			stb.append(comma).append(toJson(e));
    			comma = ", ";
    		}
    		stb.append("]");
    		
    	} else if (null == o) {
    		stb.append("null");
    	} else if (o instanceof String) { // 不完全対応
    		stb.append("\"").append(o).append("\"");
    	} else {
    		stb.append(o);
    	}
    	return stb.toString();
    }

    private static class Param {
        final String _year;
        final String _gakki;
        final String _hyotei;
        final String _cmd;
        final String _prgid;
        final String[] _schregnoArray;
        final boolean _isOutputDebug;

        public Param(final DB2UDB db2, final HttpServletRequest request, final Map paramMap) {
            _year = request.getParameter("YEAR");                            //年度
            _gakki = request.getParameter("SEMESTER");                           //学期
            _hyotei = StringUtils.defaultString(request.getParameter("HYOTEI"), "off");                          //評定の読み替え
            _cmd = request.getParameter("cmd");
            _prgid = request.getParameter("PRGID");
            
            _schregnoArray = request.getParameterValues("category_selected");   // 学籍番号
            _isOutputDebug = "1".equals(KnjDbUtils.getDbPrginfoProperties(db2, "KNJE370IHyoteiHeikin", "outputDebug"));
        }
    }
}
