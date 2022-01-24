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
import servletpack.KNJZ.detail.dao.AttendAccumulate;


/**
 * 皆勤・精勤者度数分布表
 *
 * @author yogi
 *
 */
public class KNJD668N {
    private boolean _hasData;
    Param _param;
    private static final String SEMEALL = "9";

    private static final Log log = LogFactory.getLog(KNJD668N.class);

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

    	svf.VrSetForm("KNJD668N.frm", 4);
        for (Iterator itr = resultMap.keySet().iterator();itr.hasNext();) {
        	String courseCode = (String)itr.next();
        	Map subMap = (Map)resultMap.get(courseCode);
        	int rowCnt = 0;
        	for (Iterator its = subMap.keySet().iterator();its.hasNext();) {
            	String schregNo = (String)its.next();
            	rowCnt++;
            	int colCnt = 0;
            	PrtSubCls putobj = (PrtSubCls)subMap.get(schregNo);
            	for (Iterator itc = putobj._prtarry.iterator();itc.hasNext();) {
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
            		svf.VrsOutn("VALUE", colCnt, outobj._chk_Val);  //欠席

            	}
            	Attendance att = (Attendance)putobj._attMap.get("9");
            	svf.VrsOut("LATE", String.valueOf(att._late));
            	svf.VrsOut("EARLY", String.valueOf(att._early));
            	svf.VrsOut("ABSENCE", String.valueOf(att._sickOnly + att._noticeOnly + att._nonoticeOnly));

            	svf.VrEndRecord();
        	}
        	svf.VrEndPage();
        	_hasData = true;
        }
    	if (_hasData) {
    	    svf.VrEndPage();
    	}
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
        final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._loginDate) + "度";
        svf.VrsOut("TITLE", nendo + " " + _param._semesterName + " " + _param._testName + " " + "定期考査欠席者一覧");
    }

    //成績取得処理
    private Map getScoreDat(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        Map retSubMap = new LinkedMap();
        PrtSubCls addcls = new PrtSubCls();

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
				if (!retMap.containsKey(courseCode)) {
					retSubMap = new LinkedMap();
					retMap.put(courseCode, retSubMap);
				} else {
					retSubMap = (Map)retMap.get(courseCode);
				}
				if (!retSubMap.containsKey(schregNo)) {
					addcls = new PrtSubCls();
					retSubMap.put(schregNo, addcls);
				} else {
					addcls = (PrtSubCls)retSubMap.get(schregNo);
				}
				addcls._prtarry.add(addobj);

			}
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        getAttendSemes(db2, retMap);
        return retMap;
    }

    private void getAttendSemes(final DB2UDB db2, final Map putMap) throws SQLException {
        if (null == _param._sdate || null == _param._edate || _param._sdate.compareTo(_param._edate) > 0) {
            return;
        }
        PreparedStatement psAtSeme = null;
        ResultSet rsAtSeme = null;
        _param._attendParamMap.put("schregno", "?");

        final String sql = AttendAccumulate.getAttendSemesSql(
                _param._year,
                _param._semester,
                _param._sdate,
                _param._edate,
                _param._attendParamMap
        );
        psAtSeme = db2.prepareStatement(sql);

        for (Iterator itr = putMap.keySet().iterator();itr.hasNext();) {
        	String courseCode = (String)itr.next();
        	Map subMap = (Map)putMap.get(courseCode);
        	for (Iterator its = subMap.keySet().iterator();its.hasNext();) {
            	String schregNo = (String)its.next();
            	PrtSubCls prtcls = (PrtSubCls)subMap.get(schregNo);

                try {
                psAtSeme.setString(1, schregNo);
                rsAtSeme = psAtSeme.executeQuery();
                while (rsAtSeme.next()) {
                    if (!SEMEALL.equals(rsAtSeme.getString("SEMESTER"))) {  //期間集計値だけ必要なので、
                        continue;
                    }

                    final Attendance attendance = new Attendance(
                    		rsAtSeme.getString("SEMESTER"),
                    		rsAtSeme.getInt("LESSON"),
                    		rsAtSeme.getInt("MLESSON"),
                    		rsAtSeme.getInt("SUSPEND"),
                    		rsAtSeme.getInt("MOURNING"),
                    		rsAtSeme.getInt("SICK"),
                    		rsAtSeme.getInt("SICK_ONLY"),
                    		rsAtSeme.getInt("NOTICE_ONLY"),
                    		rsAtSeme.getInt("NONOTICE_ONLY"),
                    		rsAtSeme.getInt("PRESENT"),
                    		rsAtSeme.getInt("LATE"),
                    		rsAtSeme.getInt("EARLY"),
                    		rsAtSeme.getInt("TRANSFER_DATE")
                    );
                    prtcls._attMap.put(SEMEALL, attendance);
                }
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(rsAtSeme);
                }
        	}
        }
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
		stb.append("     SUM(CASE WHEN T3.VALUE_DI = '*' THEN 1 ELSE 0 END) AS CHK_VALWK ");
		stb.append(" FROM ");
		stb.append("     RECORD_RANK_SDIV_DAT T1 ");
		stb.append("     LEFT JOIN SCHREG_REGD_DAT T2 ");
		stb.append("       ON T2.YEAR = T1.YEAR ");
		stb.append("      AND T2.SEMESTER = T1.SEMESTER ");
		stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
		stb.append("     LEFT JOIN RECORD_SCORE_DAT T3 ");
		stb.append("       ON T3.YEAR = T1.YEAR ");
		stb.append("      AND T3.SEMESTER = T1.SEMESTER ");
		stb.append("      AND T3.TESTKINDCD = T1.TESTKINDCD ");
		stb.append("      AND T3.TESTITEMCD = T1.TESTITEMCD ");
		stb.append("      AND T3.SCORE_DIV = T1.SCORE_DIV ");  //一致する前提。
		stb.append("      AND T3.CLASSCD = T1.CLASSCD ");
		stb.append("      AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
		stb.append("      AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
		stb.append("      AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
		stb.append("      AND T3.SCHREGNO = T1.SCHREGNO ");
		stb.append(" WHERE ");
		stb.append("     T1.YEAR = '" + _param._year + "' ");
		stb.append("     AND T2.GRADE = '" + _param._grade + "' ");
		stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
		stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _param._testKind_ItemCd + "' ");
		stb.append("     AND T1.SCORE_DIV = '" + _param._score_Div + "' ");  //対応するSCORE_DIVがRECORD_SCORE_DATに登録される前提。
		stb.append("     AND T1.SUBCLASSCD NOT IN ('333333','555555', '99999A', '99999B','999999') ");
		stb.append(" GROUP BY ");
		stb.append("     T1.CLASSCD, ");
		stb.append("     T1.SCHOOL_KIND, ");
		stb.append("     T1.CURRICULUM_CD, ");
		stb.append("     T1.SUBCLASSCD, ");
		stb.append("     T1.SCHREGNO ");
		stb.append(" ), FILTER_SCHREGNO AS ( ");
		stb.append(" SELECT ");
		stb.append("     T1.*, ");
		stb.append("     CASE WHEN CHK_VALWK > 0 THEN '欠' ELSE '' END AS CHK_VAL ");
		stb.append(" FROM ");
		stb.append("     SUMMARY_SCHREGNO T1 ");
		stb.append(" WHERE ");
		stb.append("     T1.CHK_VALWK > 0 ");
		stb.append(" ), TARGET_SCHREGNO AS ( ");
		stb.append(" SELECT DISTINCT ");
		stb.append("     T1.SCHREGNO ");
		stb.append(" FROM ");
		stb.append("     FILTER_SCHREGNO T1 ");
		stb.append(" ), BASE_TBLDATA AS ( ");
		stb.append(" SELECT distinct ");
		stb.append("     T0.SCHREGNO, ");
		stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
		stb.append(" FROM ");
		stb.append("     TARGET_SCHREGNO T0, ");
		stb.append("     FILTER_SCHREGNO T1 ");
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
		stb.append("     LEFT JOIN FILTER_SCHREGNO T6 ");
		stb.append("       ON T6.CLASSCD || '-' || T6.SCHOOL_KIND || '-' || T6.CURRICULUM_CD || '-' || T6.SUBCLASSCD = T1.SUBCLASSCD ");
		stb.append("      AND T6.SCHREGNO = T1.SCHREGNO ");
		stb.append("     LEFT JOIN COURSECODE_MST T7 ");
		stb.append("       ON T7.COURSECODE = T2.COURSECODE ");
		stb.append(" ORDER BY ");
		stb.append("     T2.COURSECD,");
		stb.append("     T2.MAJORCD,");
		stb.append("     T2.COURSECODE,");
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

	private class PrtSubCls {
		final List _prtarry;
		Map _attMap;
		PrtSubCls() {
			_prtarry = new ArrayList();
			_attMap = new LinkedMap();
		}
	}

    private class Attendance {
        final String _semester;
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _sick;
        final int _sickOnly;
        final int _noticeOnly;
        final int _nonoticeOnly;
        final int _present;
        final int _late;
        final int _early;
        final int _transDays;
        Attendance(
                final String semester,
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int sick,
                final int sickOnly,
                final int noticeOnly,
                final int nonoticeOnly,
                final int present,
                final int late,
                final int early,
                final int transDays
        ) {
            _semester = semester;
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
            _sickOnly = sickOnly;
            _noticeOnly = noticeOnly;
            _nonoticeOnly = nonoticeOnly;
            _present = present;
            _late = late;
            _early = early;
            _transDays = transDays;
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
        log.fatal("$Revision: 70490 $ $Date: 2019-10-31 18:14:26 +0900 (木, 31 10 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
    	private final String _year;
    	private final String _semester;
    	private final String _semesterName;
    	private final String _ctrlSemester;
    	private final String _grade;
    	private final String _testKind_ItemCd;
    	private final String _score_Div;
    	private final String _testName;
    	private final String _loginDate;
    	private final String _schoolCd;
    	private final String _staffCd;
    	private final String _sdate;
    	private final String _edate;
    	private Map _creditMap;
        private final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _testKind_ItemCd = request.getParameter("TESTKIND_ITEMCD").substring(0, 4);
            _score_Div = request.getParameter("TESTKIND_ITEMCD").substring(4);
            _loginDate = request.getParameter("LOGIN_DATE");
            _schoolCd = request.getParameter("SCHOOLCD");
            _staffCd = request.getParameter("STAFFCD");
            _sdate = request.getParameter("DATE_FROM").replace('/', '-');
            _edate = request.getParameter("DATE_TO").replace('/', '-');

            _semesterName = getSemesterName(db2);
            _testName = getTestName(db2);
            _creditMap = getCreditMap(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");
        }

        private String getSemesterName(final DB2UDB db2) {
        	String retStr = "";
        	StringBuffer stb = new StringBuffer();
        	stb.append(" select SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ");
        	log.debug(" sql = " + stb.toString());
        	retStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
        	return StringUtils.defaultString(retStr, "");

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
