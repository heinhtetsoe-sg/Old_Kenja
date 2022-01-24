// kanji=漢字
/*
 * $Id: 500dee0f9bafa3304de119344b163b6f646d798e $
 *
 * 作成日: 2020/04/24 10:51:30 - JST
 * 作成者: yogi
 *
 * Copyright(C) 2004-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

/**
 * 定期試験度数分布表
 * @author yogi
 * @version $Id: 500dee0f9bafa3304de119344b163b6f646d798e $
 */
public class KNJD625F {

    /**
     * 得点データ区切り
     */
    private static final String SCOREDIV1 = "01";  //中間(TESTIK=0101)/期末(TESTIK=9900)試験成績データ
    private static final String SCOREDIV2 = "02";  //平常点
    private static final String SCOREDIV8 = "08";  //学期末試験成績
    private static final String SCOREDIV9 = "09";  //評定

    /**
     * 降順ソート用
     */

    private static final Log log = LogFactory.getLog(KNJD625F.class);

    private static final String FORM_FILE = "KNJD625F.frm";

    private static final String TESTIK_FST = "0101";
    private static final String TESTIK_SND = "0201";
    private static final String TESTIK_FINAL = "9900";

    Param _param;

    private boolean _hasData = false;
    private static final String TOTAL_ID_SCHREG = "ZZZZZZ";   //合計データの取得時にMapキーとして利用

    /**
     * KNJD.classから呼ばれる処理。
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
        final HttpServletRequest request,
        final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();    //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        try {
            init(response, svf);

            // DB接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);  // パラメータ作成

            // 印字メイン
            _hasData = false;   // 該当データ無しフラグ
            printMain(db2, svf);
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            close(svf, db2);
        }
    }

    private void init(final HttpServletResponse response, final Vrw32alp svf) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void close(final Vrw32alp svf, final DB2UDB db2) {
        if (null != svf) {
            svf.VrQuit();
        }
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** 印刷処理メイン */
    private boolean printMain(
            final DB2UDB db2,
            final Vrw32alp svf
    ) throws Exception {
        boolean rtnflg = false;

        // 出力データ取得
        final Map infoMap = getCollegeData(db2);

        svf.VrSetForm(FORM_FILE,  4);
        for (Iterator ite = infoMap.keySet().iterator();ite.hasNext(); ) {  //大学コード単位
        	final String k1Str = (String)ite.next();
        	final Map fstMap = (Map)infoMap.get(k1Str);
        	setHead(db2, svf);
            for (Iterator itr = fstMap.keySet().iterator();itr.hasNext(); ) {  //大学コード＆学部学科
            	final String k2Str = (String)itr.next();
            	final CollegeInfo ciObj = (CollegeInfo)fstMap.get(k2Str);
            	boolean fstPrintCollegeFlg = false;
                for (Iterator itt = ciObj._students.keySet().iterator();itt.hasNext(); ) {  //生徒
                	final String k3Str = (String)itt.next();
                	final StudentInfo student = (StudentInfo)ciObj._students.get(k3Str);
                	student.subclsSort();
                	boolean fstPrintNameFlg = false;
                	boolean printColTitleFlg = false;
                    for (Iterator ity = student._gradeInfos.keySet().iterator();ity.hasNext(); ) {  //試験コード(考査or模試)
                    	final String k4Str = (String)ity.next();
                    	final GradeInfo grObj = (GradeInfo)student._gradeInfos.get(k4Str);
                		List srchList = "J".equals(grObj._grade_SK) ? student._subclsCdListJ : student._subclsCdListH;
                    	boolean fstPrintGradeFlg = false;
                    	if (!printColTitleFlg) {
                    	    printColTitleFlg = printColTitle(svf, student, grObj._grade_SK, srchList, printColTitleFlg);
                    	}
                        if (!printColTitleFlg) {
                        	svf.VrsOut("BLANK", "b");
                        	printColTitleFlg = true;
                        }
                		svf.VrEndRecord();
                		if (!fstPrintCollegeFlg) {
                            //svf.VrsOut("HOPE_COLLEGE_NAME1", ciObj.getCollegeName());
                			fstPrintCollegeFlg = true;
                		}
                		if (!fstPrintNameFlg) {
                            svf.VrsOut("HOPE_COLLEGE_NAME1", ciObj.getCollegeName());
                            svf.VrsOut("NAME1", student._name);
                            svf.VrsOut("JUDGE1", student._state);
                			fstPrintNameFlg = true;
                		}
                		if (!fstPrintGradeFlg) {
                            svf.VrsOut("GRADE1", grObj._grade_Name2);
                            svf.VrsOut("HR_NAME1", grObj._hr_Nameabbv);
                            svf.VrsOut("ATTENDNO1", grObj._attendno);
                		}
                        for (Iterator itu = grObj._tests.keySet().iterator();itu.hasNext(); ) {  //試験コード(考査or模試)
                        	final String k5Str = (String)itu.next();
                        	final TestInfo tObj = (TestInfo)grObj._tests.get(k5Str);
                            svf.VrsOut("TEST_NAME1", tObj._testitemabbv1);
                            int inColLine = 0;
                    		for (Iterator ito = srchList.iterator();ito.hasNext();) {
                    			final String subclsKey = (String)ito.next();
                        	    final SubclsInfo sObj = (SubclsInfo)tObj._subclses.get(subclsKey);
                    	    	inColLine++;
                        	    if (sObj != null) {
                        	    	if (sObj._score != null) {
                        	    		final int scoreOutLine = Integer.parseInt(grObj._grade_CD);
                        	    		if ("J".equals(grObj._grade_SK)) {
                                            svf.VrsOut("SCORE1_"+inColLine, sObj._score);
                        	    		} else {
                                            svf.VrsOut("SCORE2_"+inColLine, sObj._score);
                        	    		}
                        	    	}
                        	    }
                    		}
                    		svf.VrEndRecord();
                        }
                    }
                }
            }
            svf.VrEndPage();
            _hasData = true;
        }

        return rtnflg;
    }
    private void setHead(final DB2UDB db2, final Vrw32alp svf) {
    	svf.VrsOut("TITLE", "卒業生"+("2".equals(_param._testType) ? "模試" : "考査")+"成績一覧表");
    }

    private boolean printColTitle(final Vrw32alp svf, final StudentInfo student, final String grade_SK, final List srchList, boolean printColTitleFlg) {
        int inColLine = 0;
		for (Iterator ito = srchList.iterator();ito.hasNext();) {
			final String subclsKey = (String)ito.next();
	        for (Iterator ity = student._gradeInfos.keySet().iterator();ity.hasNext(); ) {  //試験コード(考査or模試)
	        	final String k4Str = (String)ity.next();
	        	final GradeInfo grObj = (GradeInfo)student._gradeInfos.get(k4Str);
	        	if (!grade_SK.equals(grObj._grade_SK)) continue;
	        	//全試験通しての科目名称を出力する
	            for (Iterator itu = grObj._tests.keySet().iterator();itu.hasNext(); ) {  //試験コード(考査or模試)
	            	final String k5Str = (String)itu.next();
	            	final TestInfo tObj = (TestInfo)grObj._tests.get(k5Str);
            	    final SubclsInfo sObj = (SubclsInfo)tObj._subclses.get(subclsKey);
            	    if (sObj != null) {
            	    	inColLine++;
            	    	if (sObj._subclassabbv != null) {
            	    		if ("J".equals(grObj._grade_SK) || student.isHOnly()) {
                                svf.VrsOut("SUBCLASS_NAME1_"+inColLine, sObj._subclassabbv);
            	    		} else {
                                svf.VrsOut("SUBCLASS_NAME2_"+inColLine, sObj._subclassabbv);
            	    		}
            	    		printColTitleFlg = true;
            	    	}
            	    }
        		}
            }
        }
        if (!printColTitleFlg && ("J".equals(grade_SK) || student.isHOnly())) {
        	svf.VrsOut("BLANK", "b");
        	printColTitleFlg = true;
        }
    	svf.VrEndRecord();
    	return printColTitleFlg;
    }
    //生徒データ
    private Map getCollegeData(final DB2UDB db2) {
    	final Map retMap = new LinkedMap();
        final String sql = getCollegeDataSql();
        log.debug("getCollegeData sql:"+sql);
        ResultSet rs = null;
        PreparedStatement ps = null;
        Map addwk1stMap = new LinkedMap();
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	//大学関連情報
            	final String stat_Cd = rs.getString("STAT_CD");
            	final String school_Name = rs.getString("SCHOOL_NAME");
            	final String school_Name_Show1 = rs.getString("SCHOOL_NAME_SHOW1");
            	final String facultycd = rs.getString("FACULTYCD");
            	final String facultyname = rs.getString("FACULTYNAME");
            	final String facultyname_Show1 = rs.getString("FACULTYNAME_SHOW1");
            	final String departmentcd = rs.getString("DEPARTMENTCD");
            	final String departmentname = rs.getString("DEPARTMENTNAME");
            	final String departmentname_Show1 = rs.getString("DEPARTMENTNAME_SHOW1");
            	//生徒情報
            	final String year = rs.getString("YEAR");
            	final String schregno = rs.getString("SCHREGNO");
            	final String grade = rs.getString("GRADE");
            	final String hr_Class = rs.getString("HR_CLASS");
            	final String attendno = rs.getString("ATTENDNO");
            	final String grade_SK = rs.getString("GRADE_SK");
            	final String grade_CD = rs.getString("GRADE_CD");
            	final String grade_Name1 = rs.getString("GRADE_NAME1");
            	final String grade_Name2 = rs.getString("GRADE_NAME2");
            	final String hr_Name = rs.getString("HR_NAME");
            	final String hr_Nameabbv = rs.getString("HR_NAMEABBV");
            	final String name = rs.getString("NAME");
            	final String state = rs.getString("STATE");  //合否
            	//詳細情報
            	final String testcd = rs.getString("TESTCD");    //考査種別or模試コード
            	final String testitemname = rs.getString("TESTITEMNAME");
            	final String testitemabbv1 = rs.getString("TESTITEMABBV1");
            	//科目コード
            	final String subclasscd = rs.getString("SUBCLASSCD");    //科目コードor模試科目コード
            	final String subclassname = rs.getString("SUBCLASSNAME");
            	final String subclassabbv = rs.getString("SUBCLASSABBV");
            	final String score = rs.getString("SCORE");

            	final String mapFstKey = stat_Cd;
    		    if (retMap.containsKey(mapFstKey)) {
    		    	addwk1stMap = (Map)retMap.get(mapFstKey);
    		    } else {
    		    	addwk1stMap = new LinkedMap();
        		    retMap.put(mapFstKey, addwk1stMap);
    		    }

    		    final String map2ndKey = stat_Cd + "-" +  facultycd + "-" + departmentcd;
            	CollegeInfo caddwk = null;
    		    if (addwk1stMap.containsKey(map2ndKey)) {
    		    	caddwk = (CollegeInfo)addwk1stMap.get(map2ndKey);
    		    } else {
                	caddwk = new CollegeInfo(stat_Cd, school_Name, school_Name_Show1, facultycd, facultyname, facultyname_Show1, departmentcd, departmentname, departmentname_Show1);
        		    addwk1stMap.put(map2ndKey, caddwk);
    		    }

    		    final String map3rdKey = schregno;
            	StudentInfo staddwk = null;
    		    if (caddwk._students.containsKey(map3rdKey)) {
    		    	staddwk = (StudentInfo)caddwk._students.get(map3rdKey);
    		    } else {
                	staddwk = new StudentInfo(schregno, name, state);
                	caddwk._students.put(map3rdKey, staddwk);
    		    }

    		    final String map4thKey = year + "-" + grade + "-" + hr_Class;
    		    GradeInfo graddwk = null;
    		    if (staddwk._gradeInfos.containsKey(map4thKey)) {
    		    	graddwk = (GradeInfo)staddwk._gradeInfos.get(map4thKey);
    		    } else {
    		    	graddwk = new GradeInfo(year, schregno,  grade, hr_Class, attendno, grade_SK, grade_CD, grade_Name1, grade_Name2, hr_Name, hr_Nameabbv);
    		    	staddwk._gradeInfos.put(map4thKey, graddwk);
    		    }

    		    final String map5thKey = testcd;
            	TestInfo taddwk = null;
    		    if (graddwk._tests.containsKey(map5thKey)) {
    		    	taddwk = (TestInfo)graddwk._tests.get(map5thKey);
    		    } else {
                	taddwk = new TestInfo(testcd, testitemname, testitemabbv1);
                	graddwk._tests.put(map5thKey, taddwk);
    		    }

    		    final String map6thKey = subclasscd;
    		    staddwk.setSubclsCd(subclasscd, grade_SK);
    		    SubclsInfo suaddwk = null;
    		    if (!taddwk._subclses.containsKey(map6thKey)) {
    		    	suaddwk = new SubclsInfo(subclasscd, subclassname, subclassabbv, score);
    		    	taddwk._subclses.put(map6thKey, suaddwk);
    		    }
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getCollegeDataSql() {
    	final StringBuffer stb = new StringBuffer();
    	stb.append(" WITH SCH_MAXYEAR AS ( ");
    	stb.append(" SELECT ");
    	stb.append("   SCHREGNO, ");
    	stb.append("   GRADE, ");
    	stb.append("   MAX(YEAR) AS YEAR ");
    	stb.append(" FROM ");
    	stb.append("   SCHREG_REGD_DAT ");
    	stb.append(" GROUP BY ");
    	stb.append("   GRADE, ");
    	stb.append("   SCHREGNO ");
    	stb.append(" ORDER BY ");
    	stb.append("   SCHREGNO, ");
    	stb.append("   GRADE ");
    	stb.append(" ), SCH_BASE_WKDAT AS ( ");
    	stb.append(" SELECT ");
    	stb.append("   T1.SCHREGNO, ");
    	stb.append("   T1.GRADE, ");
    	stb.append("   T1.HR_CLASS, ");
    	stb.append("   T1.ATTENDNO, ");
    	stb.append("   T1.YEAR, ");
    	stb.append("   T1.SEMESTER, ");
    	stb.append("   T3.NAME ");
    	stb.append(" FROM ");
    	stb.append("   SCHREG_REGD_DAT T1 ");
    	stb.append("   INNER JOIN SCH_MAXYEAR T2 ");
    	stb.append("     ON T2.SCHREGNO = T1.SCHREGNO ");
    	stb.append("    AND T2.YEAR = T1.YEAR ");
    	stb.append("    AND T2.GRADE = T1.GRADE ");
    	stb.append("   LEFT JOIN SCHREG_BASE_MST T3 ");
    	stb.append("     ON T3.SCHREGNO = T1.SCHREGNO ");
    	stb.append(" ORDER BY ");
    	stb.append("   SCHREGNO, ");
    	stb.append("   GRADE, ");
    	stb.append("   YEAR, ");
    	stb.append("   SEMESTER ");
    	if ("2".equals(_param._testType)) {
    		//注意：ここでの検索は、「指示画面で指定した模試コードの名称に一致するデータを取得する」なので、
    		//      一度、指示画面から来たコードに対応する名称を引っ張り、それに紐づく模試コード情報を用意しておく。
    		//      ちなみに、指示画面で指定している模試コードは「その模試の最新年度のコード」
        	stb.append(" ), MOCK_WKMST AS ( ");
        	stb.append(" SELECT ");
        	stb.append("   T1.* ");
        	stb.append(" FROM ");
        	stb.append("   MOCK_MST T1 ");
        	stb.append(" WHERE ");
        	stb.append("   MOCKNAME1 IN ( ");
        	stb.append("                     SELECT ");
        	stb.append("                       MOCKNAME1 ");
        	stb.append("                     FROM ");
        	stb.append("                       MOCK_MST ");
        	stb.append("                     WHERE ");
        	stb.append("                       MOCKCD IN " + SQLUtils.whereIn(true, _param._testInfos));
        	stb.append("   ) ");
    	}
    	stb.append(" ), GET_DATA_WK AS ( ");
    	stb.append(" SELECT ");
    	stb.append("   T1.STAT_CD, ");
    	stb.append("   N1.SCHOOL_NAME, ");
    	stb.append("   N1.SCHOOL_NAME_SHOW1, ");
    	stb.append("   T1.FACULTYCD, ");
    	stb.append("   N2.FACULTYNAME, ");
    	stb.append("   N2.FACULTYNAME_SHOW1, ");
    	stb.append("   T1.DEPARTMENTCD, ");
    	stb.append("   N3.DEPARTMENTNAME, ");
    	stb.append("   N3.DEPARTMENTNAME_SHOW1, ");
    	stb.append("   T1.SCHREGNO, ");
    	stb.append("   T2.GRADE, ");
    	stb.append("   GDAT.SCHOOL_KIND AS GRADE_SK, ");
    	stb.append("   GDAT.GRADE_CD AS GRADE_CD, ");
    	stb.append("   GDAT.GRADE_NAME1, ");
    	stb.append("   GDAT.GRADE_NAME2, ");
    	stb.append("   T2.HR_CLASS, ");
    	stb.append("   HDAT.HR_NAME, ");
    	stb.append("   HDAT.HR_NAMEABBV, ");
    	stb.append("   T2.ATTENDNO, ");
    	stb.append("   T2.YEAR, ");
    	stb.append("   T2.NAME, ");
    	stb.append("   E005.NAME1 AS STATE, ");
    	if ("2".equals(_param._testType)) {
        	stb.append("   T3.MOCKCD AS TESTCD, ");
        	stb.append("   T4.MOCKNAME1 AS TESTITEMNAME, ");
        	stb.append("   T4.MOCKNAME2 AS TESTITEMABBV1, ");
        	stb.append("   T3.MOCK_SUBCLASS_CD AS SUBCLASSCD, ");
        	stb.append("   T5.SUBCLASS_NAME AS SUBCLASSNAME, ");
        	stb.append("   T5.SUBCLASS_ABBV AS SUBCLASSABBV, ");
    	} else {
        	stb.append("   T3.SEMESTER || '-' || T3.TESTKINDCD || T3.TESTITEMCD AS TESTCD, ");
        	stb.append("   T4.TESTITEMNAME, ");
        	stb.append("   T4.TESTITEMABBV1, ");
        	stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
        	stb.append("   T5.SUBCLASSNAME, ");
        	stb.append("   T5.SUBCLASSABBV, ");
    	}
    	stb.append("   T3.SCORE ");
    	stb.append(" FROM ");
    	stb.append("   AFT_GRAD_COURSE_DAT T1 ");
    	stb.append("   LEFT JOIN COLLEGE_MST N1 ");
    	stb.append("     ON N1.SCHOOL_CD = T1.STAT_CD ");
    	stb.append("   LEFT JOIN COLLEGE_FACULTY_MST N2 ");
    	stb.append("     ON N2.SCHOOL_CD = T1.STAT_CD ");
    	stb.append("    AND N2.FACULTYCD = T1.FACULTYCD ");
    	stb.append("   LEFT JOIN COLLEGE_DEPARTMENT_MST N3 ");
    	stb.append("     ON N3.SCHOOL_CD = T1.STAT_CD ");
    	stb.append("    AND N3.FACULTYCD = T1.FACULTYCD ");
    	stb.append("    AND N3.DEPARTMENTCD = T1.DEPARTMENTCD ");
    	stb.append("   LEFT JOIN NAME_MST E005 ");
    	stb.append("     ON E005.NAMECD1 = 'E005' ");
    	stb.append("    AND E005.NAMECD2 = T1.DECISION ");
    	stb.append("   LEFT JOIN SCH_BASE_WKDAT T2 ");
    	stb.append("     ON T2.SCHREGNO = T1.SCHREGNO ");
    	stb.append("   LEFT JOIN SCHREG_REGD_GDAT GDAT ");
    	stb.append("     ON GDAT.YEAR = T2.YEAR ");
    	stb.append("    AND GDAT.GRADE = T2.GRADE ");
    	stb.append("   LEFT JOIN SCHREG_REGD_HDAT HDAT ");
    	stb.append("     ON HDAT.YEAR = GDAT.YEAR ");
    	stb.append("    AND HDAT.SEMESTER = T2.SEMESTER ");
    	stb.append("    AND HDAT.GRADE = GDAT.GRADE ");
    	stb.append("    AND HDAT.HR_CLASS = T2.HR_CLASS ");
    	if ("2".equals(_param._testType)) {
        	stb.append("   LEFT JOIN MOCK_RANK_RANGE_DAT T3 ");
        	stb.append("     ON T3.YEAR = T2.YEAR ");
        	stb.append("    AND T3.SCHREGNO = T2.SCHREGNO ");
        	stb.append("    AND T3.MOCK_SUBCLASS_CD IN (SELECT MOCK_SUBCLASS_CD FROM MOCK_RANK_RANGE_DAT ) ");
        	stb.append("    AND T3.RANK_RANGE = '1' ");
        	stb.append("    AND T3.RANK_DIV = '02' ");
        	stb.append("    AND T3.MOCKDIV = '1' ");
        	stb.append("   INNER JOIN MOCK_WKMST T4 ");
        	stb.append("     ON T4.MOCKCD = T3.MOCKCD ");
        	stb.append("   LEFT JOIN MOCK_SUBCLASS_MST T5 ");
        	stb.append("     ON T5.MOCK_SUBCLASS_CD = T3.MOCK_SUBCLASS_CD ");
    	} else {
        	stb.append("   LEFT JOIN RECORD_RANK_SDIV_DAT T3 ");
        	stb.append("     ON T3.YEAR = T2.YEAR ");
        	stb.append("    AND T3.SEMESTER = T2.SEMESTER ");
        	stb.append("    AND T3.TESTKINDCD || T3.TESTITEMCD IN ('" + TESTIK_FST + "', '" + TESTIK_SND + "') ");
        	stb.append("    AND T3.SCORE_DIV = '" + _param._scoreDiv + "' ");
        	stb.append("    AND T3.SCHREGNO = T2.SCHREGNO ");
        	stb.append("   LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T4 ");
        	stb.append("     ON T4.YEAR = T3.YEAR ");
        	stb.append("    AND T4.SEMESTER = T3.SEMESTER ");
        	stb.append("    AND T4.TESTKINDCD = T3.TESTKINDCD ");
        	stb.append("    AND T4.TESTITEMCD = T3.TESTITEMCD ");
        	stb.append("    AND T4.SCORE_DIV = T3.SCORE_DIV ");
        	stb.append("   LEFT JOIN SUBCLASS_MST T5 ");
        	stb.append("     ON T5.CLASSCD = T3.CLASSCD ");
        	stb.append("    AND T5.SCHOOL_KIND = T3.SCHOOL_KIND ");
        	stb.append("    AND T5.CURRICULUM_CD = T3.CURRICULUM_CD ");
        	stb.append("    AND T5.SUBCLASSCD = T3.SUBCLASSCD ");
    	}
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR BETWEEN '" + _param._strtYear + "' AND '" + _param._endYear + "' ");
    	stb.append("   AND T1.STAT_CD IN " + SQLUtils.whereIn(true, _param._colleges));
    	stb.append("   AND T1.SENKOU_KIND = '0' ");
    	if ("1".equals(_param._passOnly)) {
    	    stb.append("   AND T1.DECISION = '1' ");
    	}
    	stb.append("   AND N1.SCHOOL_NAME IS NOT NULL ");
    	stb.append("   AND GDAT.SCHOOL_KIND IS NOT NULL ");
    	stb.append("   AND GDAT.GRADE_CD IS NOT NULL ");
    	stb.append(" ) ");
    	stb.append(" SELECT ");
    	stb.append("   * ");
    	stb.append(" FROM ");
    	stb.append("   GET_DATA_WK ");
    	stb.append(" ORDER BY ");
    	stb.append("   STAT_CD, ");
    	stb.append("   FACULTYCD, ");
    	stb.append("   DEPARTMENTCD, ");
    	stb.append("   SCHREGNO, ");
    	stb.append("   GRADE, ");
    	stb.append("   HR_CLASS, ");
    	stb.append("   TESTCD, ");
    	stb.append("   SUBCLASSCD ");
    	return stb.toString();

    }

    private class CollegeInfo {
        final String _stat_Cd;
        final String _school_Name;
        final String _school_Name_Show1;
        final String _facultycd;
        final String _facultyname;
        final String _facultyname_Show1;
        final String _departmentcd;
        final String _departmentname;
        final String _departmentname_Show1;
        Map _students;
        public CollegeInfo (final String stat_Cd, final String school_Name, final String school_Name_Show1, final String facultycd, final String facultyname, final String facultyname_Show1, final String departmentcd, final String departmentname, final String departmentname_Show1)
        {
            _stat_Cd = stat_Cd;
            _school_Name = school_Name;
            _school_Name_Show1 = school_Name_Show1;
            _facultycd = facultycd;
            _facultyname = facultyname;
            _facultyname_Show1 = facultyname_Show1;
            _departmentcd = departmentcd;
            _departmentname = departmentname;
            _departmentname_Show1 = departmentname_Show1;
            _students = new LinkedMap();
        }
        private String getCollegeName() {
        	final String delim = "・";
        	return StringUtils.defaultString(_school_Name_Show1, "") + ("".equals(StringUtils.defaultString(_facultyname_Show1, "")) ? "" : delim + _facultyname_Show1) + ("".equals(StringUtils.defaultString(_departmentname_Show1, "")) ? "" : delim + _departmentname_Show1);
        	//return StringUtils.defaultString(_stat_Cd, "") + ("".equals(StringUtils.defaultString(_facultycd, "")) ? "" : delim + _facultycd) + ("".equals(StringUtils.defaultString(_departmentcd, "")) ? "" : delim + _departmentcd);
        }
    }
    private class StudentInfo {
        final String _schregno;
        final String _name;
        final String _state;
        final Map _gradeInfos;
        List _subclsCdListJ;
        List _subclsCdListH;
        String _JHKindStr;
        public StudentInfo(final String schregno, final String name, final String state) {
        	_schregno = schregno;
        	_name = name;
        	_state = state;
        	_gradeInfos = new LinkedMap();
            _subclsCdListJ = new ArrayList();
            _subclsCdListH = new ArrayList();
            _JHKindStr = "";
        }
        private void setSubclsCd(final String cd, final String schoolKind) {
        	if ("2".equals(_param._testType) && _param._subclasses != null && _param._subclasses.length > 0) {
        	    if ("J".equals(schoolKind) ) {
        	    	_subclsCdListJ.add(cd);
        	    }
        	    if ("H".equals(schoolKind)) {
        	    	_subclsCdListH.add(cd);
        	    }
        	} else {
        	    if (cd != null && "J".equals(schoolKind) && !_subclsCdListJ.contains(cd)) {
        	    	_subclsCdListJ.add(cd);
        	    }
        	    if (cd != null && "H".equals(schoolKind) && !_subclsCdListH.contains(cd)) {
        	    	_subclsCdListH.add(cd);
        	    }
        	}
        	if (_JHKindStr.indexOf(schoolKind) < 0) {
        		_JHKindStr += schoolKind;
        	}
        }
        private void subclsSort() {
        	List sortListJ = new ArrayList();
        	List sortListH = new ArrayList();
        	if ("2".equals(_param._testType) && _param._subclasses != null) {
    		    for (int cnt = 0;cnt < _param._subclasses.length;cnt++) {
    			    final String srchKey = _param._subclasses[cnt];
    			    if (_subclsCdListJ.contains(srchKey)) {
    			        sortListJ.add(srchKey);
    			    }
    			    if (_subclsCdListH.contains(srchKey)) {
    			        sortListH.add(srchKey);
    			    }
        	    }
    		    _subclsCdListJ = sortListJ;
    		    _subclsCdListH = sortListH;
        	} else if ("1".equals(_param._testType) ) {
        		if (_subclsCdListJ != null && _subclsCdListJ.size() > 0) {
        		    Collections.sort(_subclsCdListJ);
        		}
        		if (_subclsCdListH != null && _subclsCdListH.size() > 0) {
        		    Collections.sort(_subclsCdListH);
        		}
        	}
        }
        private boolean isHOnly() {
        	boolean bRet = "H".equals(_JHKindStr);
        	return bRet;
        }
    }
    private class GradeInfo {
        final String _schregno;
        final String _year;
        final String _grade;
        final String _hr_Class;
        final String _attendno;
        final String _grade_SK;
        final String _grade_CD;
        final String _grade_Name1;
        final String _grade_Name2;
        final String _hr_Name;
        final String _hr_Nameabbv;
        final Map _tests;
        public GradeInfo (final String year, final String schregno, final String grade, final String hr_Class, final String attendno, final String grade_SK, final String grade_CD, final String grade_Name1, final String grade_Name2, final String hr_Name, final String hr_Nameabbv)
        {
            _schregno = schregno;
            _grade = grade;
            _grade_SK = grade_SK;
            _grade_CD = grade_CD;
            _grade_Name1 = grade_Name1;
            _grade_Name2 = grade_Name2;
            _hr_Class = hr_Class;
            _hr_Name = hr_Name;
            _hr_Nameabbv = hr_Nameabbv;
            _attendno = attendno;
            _year = year;
            _tests = new LinkedMap();
        }
    }
    private class TestInfo {
        final String _testcd;
        final String _testitemname;
        final String _testitemabbv1;
        Map _subclses;
        public TestInfo (final String testcd, final String testitemname, final String testitemabbv1)
        {
        _testcd = testcd;
        _testitemname = testitemname;
        _testitemabbv1 = testitemabbv1;
        _subclses = new LinkedMap();
        }
    }
    private class SubclsInfo {
        final String _subclasscd;
        final String _subclassname;
        final String _subclassabbv;
        final String _score;
        public SubclsInfo (final String subclasscd, final String subclassname, final String subclassabbv, final String score)
        {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
            _score = score;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        dumpParam(request, param);
        return param;
    }

    /** パラメータダンプ */
    private void dumpParam(final HttpServletRequest request, final Param param) {
        log.fatal("$Revision: 74012 $ $Date: 2020-04-27 18:24:03 +0900 (月, 27 4 2020) $"); // CVSキーワードの取り扱いに注意
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.info("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlSemester;
        private final String _date;
        private final String _strtYear;
        private final String _endYear;
        private final String _testType;
        private final String _passOnly;

        private final String[] _colleges;
        private final String[] _testInfos;
        private final String[] _subclasses;
        private final String  _scoreDiv;

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) throws Exception {
        	_strtYear = request.getParameter("STRT_YEAR");
        	_endYear = request.getParameter("END_YEAR");
        	_testType = request.getParameter("TESTTYPE");
        	_year = request.getParameter("YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _date = request.getParameter("DATE") != null ? request.getParameter("DATE").replace('/', '-') : "";//KNJ_EditDate.h_format_US_Y(returnval.val3) + "年" + KNJ_EditDate.h_format_JP_MD(returnval.val3);
        	_passOnly = request.getParameter("PASS_ONLY");

            _colleges = request.getParameterValues("COLLEGE_SELECTED");
            _testInfos = request.getParameterValues("TESTINFO_SELECTED");
            _subclasses = request.getParameterValues("SUBCLASS_SELECTED");

            _scoreDiv = SCOREDIV1;
        }

    }

}
