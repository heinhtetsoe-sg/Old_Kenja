package servletpack.KNJD;

import java.io.IOException;
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


/**
 * 皆勤・精勤者度数分布表
 *
 * @author yogi
 *
 */
public class KNJD669N {
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJD669N.class);

    /**
     * KNJD.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            init(response, svf);

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
            close(db2, svf);
        }

    }

    /**
     * 印刷処理メイン
     * @param db2	ＤＢ接続オブジェクト
     * @param svf	帳票オブジェクト
     * @return		無し
     * @throws Exception
     */
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {

    	//成績取得
    	Map resultMap = getScoreDat(db2);

    	svf.VrSetForm("KNJD669N.frm", 4);
    	int rowCnt = 0;
    	for (Iterator its = resultMap.keySet().iterator();its.hasNext();) {
        	String schregNo = (String)its.next();
        	rowCnt++;
        	int colCnt = 0;
        	List putobj = (List)resultMap.get(schregNo);
        	for (Iterator itc = putobj.iterator();itc.hasNext();) {
        		colCnt++;
        		PrintData outobj = (PrintData)itc.next();
        		if (colCnt == 1) {
            		if (colCnt == 1 && rowCnt == 1) {
            	    	setTitle(db2, svf);
            		}
          		    //列処理の最初に出力
        		    svf.VrsOut("HR_NAME", outobj._hr_Class_Name1);  //組
        		    svf.VrsOut("NO", String.valueOf(Integer.parseInt(outobj._attendNo)));  //出席番号
        		    final int nlen = KNJ_EditEdit.getMS932ByteLength(outobj._name);
        		    final String nfield = nlen > 30 ? "3" : nlen > 20 ? "2" : "1";
        		    svf.VrsOut("NAME" + nfield, outobj._name);  //氏名
        		}
        		//先頭行出力の際に処理
        		if (rowCnt == 1) {
                    //科目名称出力
        	        final int snlen = KNJ_EditEdit.getMS932ByteLength(outobj._subclassName);
        	        final String snField = snlen > 20 ? "2" : "1";
        	        svf.VrsOutn("SUBCLASS_NAME" + snField, colCnt, outobj._subclassName);  //科目名称
        	        //単位数取得&出力
        		    final String kStr = outobj._courseCode + "-" + outobj._subclassCd;
        		    if (_param._creditMap.containsKey(kStr)) {
        			    final String creditStr = (String)_param._creditMap.get(kStr);
        			    svf.VrsOutn("CREDIT", colCnt, creditStr);  //単位数
        		    }
        		}
        		svf.VrsOutn("VALUE", colCnt, outobj._chk_Val);  //不信評価合計

        	}
        	svf.VrEndRecord();
        	_hasData = true;
        }
    	if (_hasData) {
    	    svf.VrEndPage();
    	}
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
        final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._loginDate) + "度";
        svf.VrsOut("TITLE", nendo + " " + _param._testTermName + " " + "定期考査評価合計一覧");
        svf.VrsOut("SUBJECT", "対象:" + _param._borderVal + "以下");
    }

    //成績取得処理
    private Map getScoreDat(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        List addList = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
			String sql = getScoreDatSql();
			log.debug("sql = " + sql);
			db2.query(sql);
			rs = db2.getResultSet();
			while( rs.next() ){
				final String courseCode = rs.getString("COURSECODE");
				final String courseName = rs.getString("COURSECODENAME");
				final String schregNo = rs.getString("SCHREGNO");
				final String name = rs.getString("NAME");
				final String hr_Class_Name1 = rs.getString("HR_CLASS_NAME1");
				final String attendNo = rs.getString("ATTENDNO");
				final String subclassName = rs.getString("SUBCLASSNAME");
				final String subclassCd = rs.getString("SUBCLASSCD");
				final String chk_Val = rs.getString("CHK_VAL");
				PrintData addobj = new PrintData(courseCode, courseName, schregNo, name, hr_Class_Name1, attendNo, subclassName, subclassCd, chk_Val);
				if (!retMap.containsKey(schregNo)) {
					addList = new ArrayList();
					retMap.put(schregNo, addList);
				} else {
					addList = (List)retMap.get(schregNo);
				}
				addList.add(addobj);

			}
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;
    }

    private String getScoreDatSql() {
		StringBuffer stb = new StringBuffer();

		stb.append(" WITH SUMMARY_SCHREGNO AS ( ");
		stb.append(" SELECT ");
		stb.append("     T1.CLASSCD, ");
		stb.append("     T1.SCHOOL_KIND, ");
		stb.append("     T1.CURRICULUM_CD, ");
		stb.append("     T1.SUBCLASSCD, ");
		stb.append("     T1.SCHREGNO, ");
		stb.append("     SUM(T1.SCORE) AS CHK_VAL ");
		stb.append(" FROM ");
		stb.append("     RECORD_RANK_SDIV_DAT T1 ");
		stb.append("     LEFT JOIN SCHREG_REGD_DAT T2 ");
		stb.append("       ON T2.YEAR = T1.YEAR ");
		stb.append("      AND T2.SEMESTER = T1.SEMESTER ");
		stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
		stb.append(" WHERE ");
		stb.append("     T1.YEAR = '" + _param._year + "' ");
		stb.append("     AND T2.GRADE = '" + _param._grade + "' ");
		if (!"ALL".equals(_param._hrClass)) {
		    stb.append("     AND T2.HR_CLASS = '" + _param._hrClass + "' ");
		}
		stb.append("     AND (T1.SEMESTER < '" + _param._semester + "' ");
		stb.append("          OR ");
		stb.append("          (T1.SEMESTER = '" + _param._semester + "' AND T1.TESTKINDCD || T1.TESTITEMCD <= '" + _param._testKind_ItemCd + "') ");
		stb.append("         ) ");
		stb.append("     AND T1.SCORE_DIV = '" + _param._score_Div + "' ");
		stb.append("     AND T1.SUBCLASSCD NOT IN ('333333','555555', '99999A', '99999B', '999999') ");
		stb.append(" GROUP BY ");
		stb.append("     T1.CLASSCD, ");
		stb.append("     T1.SCHOOL_KIND, ");
		stb.append("     T1.CURRICULUM_CD, ");
		stb.append("     T1.SUBCLASSCD, ");
		stb.append("     T1.SCHREGNO ");
		stb.append(" ), FILTER_SCGHREGNO AS ( ");
		stb.append(" SELECT ");
		stb.append("     T1.* ");
		stb.append(" FROM ");
		stb.append("     SUMMARY_SCHREGNO T1 ");
		stb.append(" WHERE ");
		stb.append("     T1.CHK_VAL <= " + _param._borderVal + " ");
		stb.append(" ), TARGET_SCHREGNO AS ( ");
		stb.append(" SELECT DISTINCT ");
		stb.append("     T1.SCHREGNO ");
		stb.append(" FROM ");
		stb.append("     FILTER_SCGHREGNO T1 ");
		stb.append(" ), BASE_TBLDATA AS ( ");
		stb.append(" SELECT distinct ");
		stb.append("     T0.SCHREGNO, ");
		stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
		stb.append(" FROM ");
		stb.append("     TARGET_SCHREGNO T0, ");
		stb.append("     FILTER_SCGHREGNO T1 ");
		stb.append(" WHERE ");
		stb.append("     T1.CHK_VAL IS NOT NULL ");
		stb.append(" ORDER BY ");
		stb.append("     SUBCLASSCD ");
		stb.append(" ) ");
		stb.append(" SELECT ");
		stb.append("     T2.COURSECD || '-' || T2.MAJORCD || '-' || T2.COURSECODE AS COURSECODE, ");
		stb.append("     T7.COURSECODENAME, ");
		stb.append("     T1.SCHREGNO, ");
		stb.append("     T4.NAME, ");
		stb.append("     T3.HR_CLASS_NAME1, ");
		stb.append("     T2.ATTENDNO, ");
		stb.append("     T5.SUBCLASSNAME, ");
		stb.append("     T1.SUBCLASSCD, ");
		stb.append("     T6.CHK_VAL ");
		stb.append(" FROM ");
		stb.append("     BASE_TBLDATA T1 ");
		stb.append("     LEFT JOIN SCHREG_BASE_MST T4 ");
		stb.append("       ON T4.SCHREGNO = T1.SCHREGNO ");
		stb.append("     LEFT JOIN SCHREG_REGD_DAT T2 ");
		stb.append("       ON T2.YEAR = '" + _param._year + "' ");
		stb.append("      AND T2.SEMESTER = '" + _param._semester + "' ");
		stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
		stb.append("     LEFT JOIN SCHREG_REGD_HDAT T3 ");
		stb.append("       ON T3.YEAR = T2.YEAR ");
		stb.append("      AND T3.SEMESTER = T2.SEMESTER ");
		stb.append("      AND T3.GRADE = T2.GRADE ");
		stb.append("      AND T3.HR_CLASS = T2.HR_CLASS ");
		stb.append("     LEFT JOIN SUBCLASS_MST T5 ");
		stb.append("       ON T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || T5.SUBCLASSCD = T1.SUBCLASSCD ");
		stb.append("     LEFT JOIN FILTER_SCGHREGNO T6 ");
		stb.append("       ON T6.CLASSCD || '-' || T6.SCHOOL_KIND || '-' || T6.CURRICULUM_CD || '-' || T6.SUBCLASSCD = T1.SUBCLASSCD ");
		stb.append("      AND T6.SCHREGNO = T1.SCHREGNO ");
		stb.append("     LEFT JOIN COURSECODE_MST T7 ");
		stb.append("       ON T7.COURSECODE = T2.COURSECODE ");
		stb.append(" ORDER BY ");
		stb.append("     T2.HR_CLASS,");
		stb.append("     T2.ATTENDNO, ");
		stb.append("     T5.SUBCLASSCD ");

    	return stb.toString();
    }

	private class PrintData {
		final String _courseCode;
		final String _courseName;
	    final String _schregNo;
	    final String _name;
	    final String _hr_Class_Name1;
	    final String _attendNo;
	    final String _subclassName;
	    final String _subclassCd;
	    final String _chk_Val;

	    public PrintData (final String courseCode, final String courseName, final String schregNo, final String name, final String hr_Class_Name1,
	    		           final String attendNo, final String subclassName, final String subclassCd, final String chk_Val) {
	    	_courseCode = courseCode;
	    	_courseName = courseName;
	        _schregNo = schregNo;
	        _name = name;
	        _hr_Class_Name1 = hr_Class_Name1;
	        _attendNo = attendNo;
	        _subclassName = subclassName;
	        _subclassCd = subclassCd;
	        _chk_Val = chk_Val;
		}
	}

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70491 $ $Date: 2019-10-31 18:14:47 +0900 (木, 31 10 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
    	private final String _year;
    	private final String _semester;
    	private final String _ctrlSemester;
    	private final String _grade;
    	private final String _hrClass;
    	private final String _testKind_ItemCd;
    	private final String _score_Div;
    	private final String _testName;
    	private final String _programid;
    	private final String _loginDate;
    	private final String _schoolCd;
    	private final String _staffCd;
    	private final String _borderVal;
    	private final String _testTermName;
    	private String _CourseMajorCd;
    	private Map _creditMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _testKind_ItemCd = request.getParameter("TESTKIND_ITEMCD").substring(0, 4);
            _score_Div = request.getParameter("TESTKIND_ITEMCD").substring(4);
            _programid = request.getParameter("PRGID");
            _loginDate = request.getParameter("LOGIN_DATE");
            _schoolCd = request.getParameter("SCHOOLCD");
            _staffCd = request.getParameter("STAFFCD");
            _hrClass = request.getParameter("HR_CLASS");
            _borderVal = request.getParameter("BORDERVAL");

            _testName = getTestName(db2);
            _creditMap = getCreditMap(db2);
            _testTermName = getTestTermName(db2);
        }

        private Map getCreditMap(final DB2UDB db2) {
        	Map retMap = new HashMap();
        	StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("   COURSECD || '-' || MAJORCD || '-' || COURSECODE || '-' || CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS CONCAT_CD, ");
        	stb.append("   CREDITS ");
        	stb.append(" FROM CREDIT_MST ");
        	stb.append(" WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ");
        	log.debug(" sql = " + stb.toString());
        	retMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "CONCAT_CD", "CREDITS");
        	return retMap;
        }

        private String getTestName(final DB2UDB db2) {
        	String retStr = "";
        	StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ");
        	stb.append(" WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' AND SCORE_DIV = '" + _score_Div + "' ");
        	stb.append("   AND TESTKINDCD || TESTITEMCD = '" + _testKind_ItemCd + "' ");
        	log.debug(" sql = " + stb.toString());
        	retStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
        	return StringUtils.defaultString(retStr, "");
        }

        private String getTestTermName(final DB2UDB db2) {
        	String retStr = "";
        	StringBuffer stb = new StringBuffer();
        	stb.append(" WITH TEST_GETNAME_DAT AS ( ");
        	stb.append(" SELECT  ");
        	stb.append("   T1.* ");
        	stb.append(" FROM ");
        	stb.append("   TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
        	stb.append(" WHERE ");
        	stb.append("   T1.YEAR = '2006' ");
        	stb.append("   AND (T1.SEMESTER < '3' ");
        	stb.append("        OR ");
        	stb.append("        T1.SEMESTER = '3' AND T1.TESTKINDCD || T1.TESTITEMCD <= '0101' ");
        	stb.append("        ) ");
        	stb.append("   AND T1.SCORE_DIV = '" + _score_Div + "' ");
        	stb.append(" ORDER BY ");
        	stb.append("   T1.SEMESTER, ");
        	stb.append("   T1.TESTKINDCD, ");
        	stb.append("   T1.TESTITEMCD ");
        	stb.append(" ) ");
        	stb.append(" ( ");
        	stb.append(" SELECT ");
        	stb.append("   '0' AS ID, ");
        	stb.append("   TESTITEMNAME ");
        	stb.append(" FROM  ");
        	stb.append("   TEST_GETNAME_DAT ");
        	stb.append(" FETCH FIRST 1 ROWS ONLY ");
        	stb.append(" ) ");
        	stb.append(" UNION ALL ");
        	stb.append(" ( ");
        	stb.append(" SELECT ");
        	stb.append("   '1' AS ID, ");
        	stb.append("   TF.TESTITEMNAME ");
        	stb.append(" FROM ");
        	stb.append("   TEST_GETNAME_DAT TF ");
        	stb.append(" WHERE ");
        	stb.append("   TF.SEMESTER = '1' AND TF.TESTKINDCD || TF.TESTITEMCD = '0101' ");
        	stb.append(" ) ");
        	log.debug(" sql = " + (String)stb.toString());
        	Map qMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "ID", "TESTITEMNAME");
        	retStr = (String)(qMap.size() > 0 ? (String)qMap.get("0") + " ～ " : "") + (qMap.size() > 1 ? (String)qMap.get("1") : "");
        	return StringUtils.defaultString(retStr, "");
        }
    }

    private void close(final DB2UDB db2, final Vrw32alp svf) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
        if (null != svf) {
            svf.VrQuit();
        }
    }
}
