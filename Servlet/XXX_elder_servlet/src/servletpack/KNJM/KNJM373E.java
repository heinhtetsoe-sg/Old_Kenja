/*
 * $Id: cad7c9f90907dca25b03aa6da82f439e6390e553 $
 *
 * 作成日: 2019/04/25
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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

public class KNJM373E {

    private static final Log log = LogFactory.getLog(KNJM373E.class);

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
        svf.VrSetForm("KNJM373E.frm", 1);

        //クラス単位でループ
        for (int ii = 0;ii < _param._hrclass.size();ii++) {
        	final String hrClassCd = (String)_param._hrclass.get(ii);
        	//生徒情報を取得
            final List printList = getStudentList(db2, hrClassCd);
            //SQLで利用する学籍番号を検索する文字列を作成
            String schregNoInState = " ( '";
            String delimstr = "";
	        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
	            Student chkwk = (Student)iterator.next();
	            schregNoInState += delimstr + chkwk._schregno;
	            delimstr = "', '";
	        }
            schregNoInState += "' ) ";
            //科目リストを作成
            final List printSubject = getSubjectList(db2, schregNoInState);
            //期限切れレポートデータを取得
            final Map printReport = getReportMap(db2, schregNoInState);

            final int maxLineCnt = 30; //1ページの出力生徒数
            final int maxColCnt = 11;  //1ページ出力列数
            //生徒をページ単位に切り直し
            final List pageList = new ArrayList();
            List wklist = new ArrayList();
            for (int kk = 0;kk < printList.size();kk++) {
            	if (kk == maxLineCnt) {
            		pageList.add(wklist);
            		wklist = new ArrayList();
            	}
            	wklist.add(printList.get(kk));
            }
            //半端分を追加
    		pageList.add(wklist);

        	//タイトル出力
    		setTitle(db2, svf, hrClassCd);

            //ページ分けしたグループ単位でループ
    		for (int pp = 0;pp < pageList.size();pp++) {
    			List schList = (List)pageList.get(pp);
    			boolean outputschregInfoFlg = false;
            	boolean chkOutputSchInfo = false;
                //項目ごとにループ
                int printCol = 1;
                int printLine = 1;
                for (Iterator its=printSubject.iterator();its.hasNext();) {
                	SubclassInfo prtSubject =(SubclassInfo)its.next();
                	//項目名
                    svf.VrsOut("SUBCLASS_NAME"+printCol, prtSubject._subclassAbbv);
                	chkOutputSchInfo = false;
                    printLine = 1;
                    if (printCol > maxColCnt) {
                    	svf.VrEndPage();
                    	//タイトル出力
                		setTitle(db2, svf, hrClassCd);
            			outputschregInfoFlg = false;
                    	printCol = 1;
                    }
                	for (Iterator itt=schList.iterator();itt.hasNext();) {
                		Student prtStudent = (Student)itt.next();
                		if (!outputschregInfoFlg) {
                			chkOutputSchInfo = true;
                			//生徒情報出力
                			printStudentInfo(svf, printLine, prtStudent);
                		}
                		//期限切れレポート情報を取得
                		String outReportVal = "0";
                		Map subMap = (Map)printReport.get(prtStudent._schregno);
                		if (subMap != null) {
                			final String subkey = prtSubject._classCd + "-" + prtSubject._schoolKind + "-" + prtSubject._curriculumCd + "-" + prtSubject._subclassCd;
                			ReportInfo prtReport = (ReportInfo)subMap.get(subkey);
                			if (prtReport != null) {
                				outReportVal = prtReport._cnt;
                			}
                		}
                		svf.VrsOutn("EXPIRE" + printCol, printLine, outReportVal);
                		prtStudent._totalCnt += Integer.parseInt(outReportVal);
                		printLine++;
                	}
                	if (!outputschregInfoFlg && chkOutputSchInfo) {
                		chkOutputSchInfo = true;
                	}
                	printCol++;
                }
                if (printCol > maxColCnt) {
                	svf.VrEndPage();
                	//タイトル出力
            		setTitle(db2, svf, hrClassCd);
        			outputschregInfoFlg = false;
                	printCol = 1;
                }
                //合計を出力
                //項目名
                svf.VrsOut("SUBCLASS_NAME11", "合計");
                printLine = 1;
            	chkOutputSchInfo = false;
            	for (Iterator itt=schList.iterator();itt.hasNext();) {
            		Student prtStudent = (Student)itt.next();
            		if (!outputschregInfoFlg) {
            			chkOutputSchInfo = true;
            			//生徒情報出力
            			printStudentInfo(svf, printLine, prtStudent);
            		}
            		svf.VrsOutn("EXPIRE11", printLine, String.valueOf(prtStudent._totalCnt));
            		printLine++;
            	}
    		}
            _hasData = true;
	        svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final String hrClassCd) {
        String setYear = KNJ_EditDate.h_format_Seireki_N(_param._year + "-04-01");
    	svf.VrsOut("TITLE", setYear + "度 期限切れレポート回数一覧");
    	svf.VrsOut("SEMESTER", _param._semesterName);
    	svf.VrsOut("HR_NAME", (String)_param._hrClassNameMap.get(hrClassCd));
    }

    private void printStudentInfo(final Vrw32alp svf, final int printLine, final Student prtStudent) {
		svf.VrsOutn("ATTEND_NO", printLine, prtStudent._attendno);
		svf.VrsOutn("SCHREG_NO", printLine, prtStudent._schregno);
		final int nlen = KNJ_EditEdit.getMS932ByteLength(prtStudent._name);
		final String nfield = nlen > 30 ? "3" : nlen > 20 ? "2" : "1";
		svf.VrsOutn("NAME"+ nfield, printLine, prtStudent._name);
    }

    private List getStudentList(final DB2UDB db2, final String gradeHrClass) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getStudentSql(gradeHrClass);
        log.debug("getStudentList:"+sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String grade = rs.getString("GRADE");
                final String hrclass = rs.getString("HR_CLASS");
                final String attendNo = rs.getString("ATTENDNO");
                final String hrName = rs.getString("HR_NAME");

                final Student addwk = new Student(schregNo, name, grade, hrclass, hrName, attendNo);
                retList.add(addwk);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private List getSubjectList(final DB2UDB db2, final String schregNoInState) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getSubclassSql(schregNoInState);
        log.debug("getSubjectList:"+sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String classCd;
            	final String schoolKind;
            	final String curriculumCd;
            	final String subclassCd;

                if ("1".equals(_param._useCurriculumcd)) {
                	classCd = rs.getString("CLASSCD");
                	schoolKind = rs.getString("SCHOOL_KIND");
                	curriculumCd = rs.getString("CURRICULUM_CD");
                	subclassCd = rs.getString("SUBCLASSCD");
                } else {
                	classCd = "";
                	schoolKind = "";
                	curriculumCd = "";
                	subclassCd = rs.getString("SUBCLASSCD");
                }
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String subclassAbbv = rs.getString("SUBCLASSABBV");

                final SubclassInfo addwk = new SubclassInfo(classCd, schoolKind, curriculumCd, subclassCd, subclassName, subclassAbbv);
                retList.add(addwk);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    //期限切れレポート情報を取得。
    //2重になっているので注意。構成は、(schregno, Map(subclasscd, 情報))
    private Map getReportMap(final DB2UDB db2, final String schregNoInState) {
        final Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getReportSql(schregNoInState);
        log.debug("getReportMap:"+sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String classcd;
            	final String schoolKind;
            	final String curriculumCd;
                if ("1".equals(_param._useCurriculumcd)) {
                	classcd = rs.getString("CLASSCD");
                	schoolKind = rs.getString("SCHOOL_KIND");
                	curriculumCd = rs.getString("CURRICULUM_CD");
                } else {
                	classcd = "";
                	schoolKind = "";
                	curriculumCd = "";
                }
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String schregNo = rs.getString("SCHREGNO");
                final String cnt = rs.getString("CNT");

                final ReportInfo addwk = new ReportInfo(classcd, schoolKind, curriculumCd, subclassCd, schregNo, cnt);
                Map subMap = (Map)retMap.get(schregNo);
                if (subMap == null) {
                	subMap = new LinkedMap();
                    retMap.put(schregNo, subMap);
                }
                final String keySubClassCd = classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclassCd;
                subMap.put(keySubClassCd, addwk);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getStudentSql(final String gradeHrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("  T1.SCHREGNO, ");
        stb.append("  T2.NAME, ");
        stb.append("  T1.GRADE, ");
        stb.append("  T1.HR_CLASS, ");
        stb.append("  T1.ATTENDNO, ");
        stb.append("  T3.HR_NAME ");
        stb.append(" FROM ");
        stb.append("  SCHREG_REGD_DAT T1 ");
        stb.append("  LEFT JOIN SCHREG_BASE_MST T2 ");
        stb.append("    ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("  LEFT JOIN SCHREG_REGD_HDAT T3 ");
        stb.append("    ON T3.YEAR = T1.YEAR ");
        stb.append("   AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("   AND T3.GRADE = T1.GRADE ");
        stb.append("   AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("  T1.YEAR = '" + _param._year + "' ");
        stb.append("  AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("  AND T1.GRADE || T1.HR_CLASS = '" + gradeHrClass + "' ");
        stb.append(" ORDER BY ");
        stb.append("   T1.ATTENDNO ");

        return stb.toString();
    }

    private String getSubclassSql(final String schregNoInState) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("  T1.CLASSCD, ");
            stb.append("  T1.SCHOOL_KIND, ");
            stb.append("  T1.CURRICULUM_CD, ");
        }
        stb.append("  T1.SUBCLASSCD, ");
        stb.append("  T2.SUBCLASSNAME, ");
        stb.append("  T2.SUBCLASSABBV ");
        stb.append(" FROM ");
        stb.append("   CHAIR_CORRES_DAT T1 ");
        stb.append("   LEFT JOIN V_SUBCLASS_MST T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("    AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("    AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("    AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.REP_SEQ_ALL > 0 ");
        stb.append("   AND T1.CHAIRCD IN ( ");
        stb.append("                      SELECT DISTINCT ");
        stb.append("                        TW.CHAIRCD ");
        stb.append("                      FROM ");
        stb.append("                        CHAIR_STD_DAT TW ");
        stb.append("                      WHERE ");
        stb.append("                        TW.YEAR = T1.YEAR ");
        stb.append("                        AND TW.SEMESTER = '" + _param._semester + "' ");
        stb.append("                        AND TW.SCHREGNO IN " + schregNoInState + " ");
        stb.append("                     ) ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("  T1.CLASSCD, ");
            stb.append("  T1.SCHOOL_KIND, ");
            stb.append("  T1.CURRICULUM_CD, ");
        }
        stb.append("  T1.SUBCLASSCD ");

        return stb.toString();
    }

    private String getReportSql(final String schregNoInState) {
        final StringBuffer stb = new StringBuffer();
        //生徒が提出期限をオーバーしたデータの"提出期限日"を取得
        stb.append(" WITH GET_OVERDATE_TBL AS ( ");
        stb.append(" SELECT DISTINCT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("   T1.CLASSCD, ");
            stb.append("   T1.SCHOOL_KIND, ");
            stb.append("   T1.CURRICULUM_CD, ");
        }
        stb.append("   T1.SUBCLASSCD, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T2.RETURN_DATE ");
        stb.append(" FROM ");
        stb.append("   REP_PRESENT_DAT T1 ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("   LEFT JOIN REP_STANDARDDATE_COURSE_DAT T2 ");
        } else {
            stb.append("   LEFT JOIN REP_STANDARDDATE_DAT T2 ");
        }
        stb.append("     ON T2.YEAR = T1.YEAR ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("    AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("    AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("    AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("    AND T2.STANDARD_SEQ = T1.STANDARD_SEQ ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '"+_param._year+"' ");
        stb.append("   AND T1.SCHREGNO IN " + schregNoInState + " ");
        stb.append("   AND T1.REPRESENT_SEQ > 0 ");
        stb.append("   AND T2.RETURN_DATE < T1.RECEIPT_DATE ");
        stb.append("   AND EXISTS( ");
        stb.append("         SELECT ");
        stb.append("           'X' ");
        stb.append("         FROM ");
        stb.append("           REP_PRESENT_DAT TW ");
        stb.append("         WHERE ");
        stb.append("           TW.YEAR = T1.YEAR ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND TW.CLASSCD = T1.CLASSCD ");
            stb.append("           AND TW.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("           AND TW.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("           AND TW.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("           AND TW.STANDARD_SEQ = T1.STANDARD_SEQ ");
        stb.append("           AND TW.SCHREGNO = T1.SCHREGNO ");
        stb.append("           AND TW.REPRESENT_SEQ = 0 ");
        stb.append("         ) ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("   T1.CLASSCD, ");
            stb.append("   T1.SCHOOL_KIND, ");
            stb.append("   T1.CURRICULUM_CD, ");
        }
        stb.append("   T1.SUBCLASSCD, ");
        stb.append("   T1.SCHREGNO ");
        stb.append(" ) ");
        //上記から、提出オーバーした日数を集計(これを期限切れレポート回数としている)
        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("   TA.CLASSCD, ");
            stb.append("   TA.SCHOOL_KIND, ");
            stb.append("   TA.CURRICULUM_CD, ");
            stb.append("   TA.SUBCLASSCD, ");
        }
        stb.append("   TA.SCHREGNO, ");
        stb.append("   COUNT(TA.RETURN_DATE) AS CNT ");
        stb.append(" FROM ");
        stb.append("   GET_OVERDATE_TBL TA ");
        stb.append(" GROUP BY ");
        stb.append("   TA.SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("   TA.CLASSCD, ");
            stb.append("   TA.SCHOOL_KIND, ");
            stb.append("   TA.CURRICULUM_CD, ");
        }
        stb.append("   TA.SUBCLASSCD ");

        return stb.toString();
    }

    private class Student {
    	final String _schregno;
        final String _name;
        final String _grade;
        final String _hr_class;
        final String _hr_className;
        final String _attendno;
        int _totalCnt;
        Student(
            	final String schregno,
                final String name,
                final String grade,
                final String hr_class,
                final String hr_className,
                final String attendno
        		) {
        	_schregno = schregno;
            _name = name;
            _grade = grade;
            _hr_class = hr_class;
            _hr_className = hr_className;
            _attendno = attendno;
            _totalCnt = 0;
        }
    }

    private class SubclassInfo {
    	final String _classCd;
    	final String _schoolKind;
    	final String _curriculumCd;
    	final String _subclassCd;
        final String _subclassName;
        final String _subclassAbbv;

        public SubclassInfo(
            	final String classCd,
            	final String schoolKind,
            	final String curriculumCd,
            	final String subclassCd,
                final String subclassName,
                final String subclassAbbv
                ) {
        	_classCd = classCd;
        	_schoolKind = schoolKind;
        	_curriculumCd = curriculumCd;
        	_subclassCd = subclassCd;
            _subclassName = subclassName;
            _subclassAbbv = subclassAbbv;
        }
    }

    private class ReportInfo {
    	final String _classcd;
    	final String _schoolKind;
    	final String _curriculumCd;
        final String _subclassCd;
        final String _schregNo;
        final String _cnt;
        ReportInfo (
            	final String classcd,
            	final String schoolKind,
            	final String curriculumCd,
                final String subclassCd,
                final String schregNo,
                final String cnt
        		) {
        	_classcd = classcd;
        	_schoolKind = schoolKind;
        	_curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _schregNo = schregNo;
            _cnt = cnt;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74260 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _useCurriculumcd;
        final String _semesterName;
        List _hrclass = new ArrayList();
        Map _hrClassNameMap;
        final String _useRepStandarddateCourseDat;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            String[] hrclswk = request.getParameterValues("category_selected");     //学年・組
            if (hrclswk.length > 0) {
                _hrclass = Arrays.asList(hrclswk);
            }
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useRepStandarddateCourseDat = request.getParameter("useRepStandarddateCourseDat");

            _semesterName = getSemesterName(db2);
            _hrClassNameMap = getHrClassMap(db2);
        }

        private String getSemesterName(final DB2UDB db2) {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '"+ _semester +"' ";
            log.debug("getSemesterName:"+sql);
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                	retStr = rs.getString("SEMESTERNAME");
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

        private Map getHrClassMap(final DB2UDB db2) {
        	Map retMap = new LinkedMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            String hrclassInState = " ( '";
            String delimstr = "";
            for (int ii = 0;ii < _hrclass.size();ii++) {
            	hrclassInState += delimstr + (String)_hrclass.get(ii);
                delimstr = "', '";
            }
            hrclassInState += "' ) ";

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT GRADE || HR_CLASS AS GHCD, HR_NAME ");
            stb.append(" FROM SCHREG_REGD_HDAT ");
            stb.append(" WHERE YEAR = '" + _year + "' AND SEMESTER = '"+ _semester +"' ");
            stb.append("      AND GRADE || HR_CLASS IN " + hrclassInState + " ");
            final String sql = stb.toString();

            log.debug("getHrClassMap:"+sql);
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                	retMap.put(rs.getString("GHCD"), rs.getString("HR_NAME"));
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }
    }

}

// eof

