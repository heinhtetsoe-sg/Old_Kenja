/*
 * $Id: caed776a726f5e2a1997c5973e7362702351127d $
 *
 * 作成日: 2017/02/28
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJM490N {

    private static final Log log = LogFactory.getLog(KNJM490N.class);

    private boolean _hasData;
//    private DecimalFormat dmf = new DecimalFormat();
//    private Calendar cal1 = Calendar.getInstance( );

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        //扱うデータが多いため、クラス毎に(学年・クラス順に並べ替えた順番で)処理を行う。
        for (Iterator itc = _param._selectClsMap.keySet().iterator();itc.hasNext();) {
        	final String hrClassCd = (String)itc.next();
        	//出力情報を取得
        	List students = getStudents(db2, hrClassCd);

        	if (students.size() > 0) {
                svf.VrSetForm("KNJM490N.frm", 4);
        		//出力
        		svf.VrsOut("TITLE", _param._nendo + "学習成績一覧表");
        		final String outDateStr = KNJ_EditDate.h_format_JP(db2, _param._ctrlDate);
        		svf.VrsOut("ymd1", outDateStr);
        		//印鑑出力(職位の引く順に、右から出力。)
        		int imgrest = 8;
        		for (Iterator itr = _param._jobStampNameList.iterator();itr.hasNext();) {
                    setImg_Obj(svf, imgrest, (String)itr.next(), _param._keninWakuImg);
                    imgrest = imgrest - 1;
        		}

        		final String teacherName = (String)_param._teacherNameMap.get(hrClassCd);
        		final int teachernlen = KNJ_EditEdit.getMS932ByteLength(teacherName);
        		final String tfield = teachernlen > 30 ? "2" : "";
        		svf.VrsOut("HR_TEACHER"+tfield, teacherName);

        		//Record以外を出力
        		int gyoIdx = 1;
        	    for (Iterator ite = students.iterator();ite.hasNext();) {
        	    	Student student = (Student) ite.next();
                    //No.
        	    	svf.VrsOutn("NUMBER", gyoIdx, String.valueOf(gyoIdx));
        	    	//クラス
        	    	svf.VrsOutn("HR_NAME", gyoIdx, student._hrName);
        	    	//学籍番号
        	    	svf.VrsOutn("SCHREG_NO", gyoIdx, student._schregNo);
        	    	//氏名
        	    	svf.VrsOutn("name1", gyoIdx, student._name);
        	    	if (student._totalInf != null) {
        	    	    //教科(合計)
        	    	    svf.VrsOutn("TOTAL1_1", gyoIdx, StringUtils.defaultString(student._totalInf._tounen_Get_Credit, ""));
        	    	    //教科(累計)
        	    	    svf.VrsOutn("TOTAL1_2", gyoIdx, StringUtils.defaultString(student._totalInf._ruiseki_Get_Credit, ""));
        	    	    //特活(合計)
        	    	    svf.VrsOutn("TOTAL2_1", gyoIdx, StringUtils.defaultString(student._totalInf._tounen_Creditval, ""));
        	    	   //特活(累計)
        	    	    svf.VrsOutn("TOTAL2_2", gyoIdx, StringUtils.defaultString(student._totalInf._ruiseki_Creditval, ""));
        	    	    //出校(合計)
        	    	    svf.VrsOutn("TOTAL3_1", gyoIdx, StringUtils.defaultString(student._totalInf._tounen_Syukkou_Cnt, ""));
        	    	    //出校(累計)
        	    	    svf.VrsOutn("TOTAL3_2", gyoIdx, StringUtils.defaultString(student._totalInf._ruiseki_Syukkou_Cnt, ""));
        	    	}
        	    	gyoIdx++;
        	    }
        		//Recordを出力
        	    for (Iterator its = _param._sammaryChairInfoMap.keySet().iterator();its.hasNext();) {
        	    	String subclasscd = (String)its.next();
        	    	ChairInfo coutwk = (ChairInfo)_param._sammaryChairInfoMap.get(subclasscd);
        		    //項目タイトル
        	    	//教科
        	    	svf.VrsOut("course1", StringUtils.defaultString(coutwk._className, ""));
        	    	//科目
        	    	svf.VrsOut("subject1", StringUtils.defaultString(coutwk._subclassName, ""));
        	    	//単位数
        	    	final String unitMinWk = StringUtils.defaultString(coutwk._credits_Min, "");
        	    	final String unitMaxWk = StringUtils.defaultString(coutwk._credits_Max, "");
        	    	if (unitMinWk.equals(unitMaxWk) || "".equals(unitMinWk)	|| "".equals(unitMaxWk)) {
        	    		if ("".equals(unitMinWk)) {
        	    	        svf.VrsOut("credit1", unitMaxWk);
        	    		} else {
        	    	        svf.VrsOut("credit1", unitMinWk);
        	    		}
        	    	} else {
        	    	    svf.VrsOut("credit1", unitMinWk + "～" + unitMaxWk);
        	    	}
        	    	int schCnt = 1;
                    for (Iterator itk = students.iterator();itk.hasNext();) {
            	    	Student student = (Student) itk.next();
                        if (student._resultInfoMap != null) {
                	    	ResultInfo routwk = (ResultInfo)student._resultInfoMap.get(subclasscd);
                	    	if (routwk != null) {
    	                        //前期
    	            	    	svf.VrsOut("SCORE1", StringUtils.defaultString(routwk._sem1Score, ""));
    	                    	//後期
    	            	    	svf.VrsOut("SCORE2", StringUtils.defaultString(routwk._sem2Score, ""));
    	                    	//R
    	            	    	svf.VrsOut("REPORT", StringUtils.defaultString(routwk._passreportCnt, ""));
    	                    	//S
    	            	    	svf.VrsOut("SCHOOL", StringUtils.defaultString(routwk._schoolingCnt, ""));
    	                    	//評定
    	            	    	svf.VrsOut("VAL", StringUtils.defaultString(routwk._sem9Score, ""));
                	    	}
                        }
        	        }
                    svf.VrEndRecord();
        	    }
        	    _hasData = true;
        	    svf.VrEndPage();
        	}
        }
    }

    private void setImg_Obj(final Vrw32alp svf, final int imgrest, final String jobName, final String imgfname) {
		final int jobnlen = KNJ_EditEdit.getMS932ByteLength(jobName);
		final String jobfield = jobnlen > 8 ? "_2" : "_1";
		svf.VrsOut("JOB" + imgrest + jobfield, jobName);
		svf.VrsOut("JOB_IMG" + imgrest, imgfname);
    }
    private List getStudents(final DB2UDB db2, final String hr_class) {
    	List retList = new ArrayList();
    	//先に詳細情報をクラスとして取得し、生徒情報を作成しつつ、紐づける。
    	//※chairInfoは生徒の取得科目。今回の帳票はクラス全部の科目出力なので、別途関数内で中でクラスの科目をまとめてparamに設定している。
    	Map chairInfo = getChairInfo(db2, hr_class);
    	Map resultInfo = getResultInfo(db2, hr_class);
    	Map totalInfo = getTotalInfo(db2, hr_class);

    	final String sql = getStudentsSql(hr_class);
        ResultSet rs = null;
        PreparedStatement ps1 = null;
        try {
            ps1 = db2.prepareStatement(sql);
            rs = ps1.executeQuery();
            while (rs.next()) {
            	final String schregNo = rs.getString("SCHREGNO");
            	final String grade = rs.getString("GRADE");
            	final String hrClass = rs.getString("HR_CLASS");
            	final String attendNo = rs.getString("ATTENDNO");
            	final String hrName = rs.getString("HR_NAMEABBV");
            	final String name = rs.getString("NAME");
            	final String staffname = rs.getString("STAFFNAME");
                Student addwk = new Student(schregNo, grade, hrClass, attendNo, hrName, name, staffname);
                addwk.setSubInfo((Map)chairInfo.get(schregNo), (Map)resultInfo.get(schregNo), (TotalInfo)totalInfo.get(schregNo));
                retList.add(addwk);
            }
        } catch (SQLException e) {
            log.error("Exception:", e);
        } finally {
            DbUtils.closeQuietly(null, ps1, rs);
        }
    	return retList;
    }

    private String getStudentsSql(final String hr_class) {
    	StringBuffer stb = new StringBuffer();

    	stb.append(" SELECT DISTINCT ");
    	stb.append("  T1.SCHREGNO, ");
    	stb.append("  T2.GRADE, ");
    	stb.append("  T2.HR_CLASS, ");
    	stb.append("  T1.ATTENDNO, ");
    	stb.append("  T2.HR_NAMEABBV, ");
    	stb.append("  T3.NAME, ");
    	stb.append("  M1.STAFFNAME ");
    	stb.append(" FROM ");
    	stb.append("  SCHREG_REGD_DAT T1 ");
    	stb.append("  LEFT JOIN SCHREG_REGD_HDAT T2 ");
    	stb.append("    ON T2.YEAR = T1.YEAR ");
    	stb.append("   AND T2.SEMESTER = T1.SEMESTER ");
    	stb.append("   AND T2.GRADE = T1.GRADE ");
    	stb.append("   AND T2.HR_CLASS = T1.HR_CLASS ");
    	stb.append("  LEFT JOIN STAFF_MST M1 ");
    	stb.append("    ON M1.STAFFCD = T2.TR_CD1 ");
    	stb.append("  LEFT JOIN SCHREG_BASE_MST T3 ");
    	stb.append("    ON T3.SCHREGNO = T1.SCHREGNO ");
    	stb.append(" WHERE ");
    	stb.append("  T1.YEAR = '" + _param._year + "' ");
    	stb.append("  AND T1.SEMESTER = '" + _param._semester + "' ");
    	stb.append("  AND T1.GRADE || T1.HR_CLASS = '" + hr_class + "' ");
    	stb.append(" ORDER BY ");
    	stb.append("  T2.GRADE, ");
    	stb.append("  T2.HR_CLASS, ");
    	stb.append("  T1.ATTENDNO ");

    	return stb.toString();
    }

    //科目情報 Map(schregno, Map(結合subclass, 科目情報クラス))
    private Map getChairInfo(final DB2UDB db2, final String hr_class) {
    	Map retMap = new HashMap();

    	final String sql = getChairInfoSql(hr_class);
        ResultSet rs = null;
        PreparedStatement ps1 = null;
        try {
            ps1 = db2.prepareStatement(sql);
            rs = ps1.executeQuery();
            while (rs.next()) {
            	final String schregNo = rs.getString("SCHREGNO");
            	final String classCd;
            	final String schoolKind;
            	final String curriculumCd;
                final String className;
            	if ("1".equals(_param._useCurriculumcd)) {
                    classCd = rs.getString("CLASSCD");
                    schoolKind = rs.getString("SCHOOL_KIND");
                    curriculumCd = rs.getString("CURRICULUM_CD");
                    className = rs.getString("CLASSNAME");
            	} else {
                    classCd = "";
                    schoolKind = "";
                    curriculumCd = "";
                    className = "";
            	}
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String credits_Min = rs.getString("CREDITS_MIN");
                final String credits_Max = rs.getString("CREDITS_MAX");

                ChairInfo addwk = new ChairInfo(classCd, schoolKind, curriculumCd, className, subclassCd, subclassName, credits_Min, credits_Max);
                Map subMap = (Map)retMap.get(schregNo);
                if (subMap == null) {
                	subMap = new LinkedMap();
                	retMap.put(schregNo, subMap);
                }
                final String subkey;
            	if ("1".equals(_param._useCurriculumcd)) {
            		subkey = classCd + "-" +  schoolKind + "-" +  curriculumCd + "-" + subclassCd;
            	} else {
                    subkey = subclassCd;
            	}
                subMap.put(subkey, addwk);

                ChairInfo chkwk = (ChairInfo)_param._sammaryChairInfoMap.get(subkey);
                if (chkwk == null) {
                	_param._sammaryChairInfoMap.put(subkey, addwk);
                }
            }
        } catch (SQLException e) {
            log.error("Exception:", e);
        } finally {
            DbUtils.closeQuietly(null, ps1, rs);
        }
    	return retMap;
    }

    private String getChairInfoSql(final String hr_class) {
    	StringBuffer stb = new StringBuffer();

    	stb.append(" SELECT ");
    	stb.append("  T1.SCHREGNO, ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("  M2.CLASSCD, ");
    	    stb.append("  M2.SCHOOL_KIND, ");
    	    stb.append("  M2.CURRICULUM_CD, ");
        	stb.append("  M3.CLASSNAME, ");
    	}
    	stb.append("  M2.SUBCLASSCD, ");
    	stb.append("  M4.SUBCLASSNAME, ");
    	stb.append("  MIN(M5.CREDITS) AS CREDITS_MIN, ");
    	stb.append("  MAX(M5.CREDITS) AS CREDITS_MAX ");
    	stb.append(" FROM ");
    	stb.append("  SCHREG_REGD_DAT T1 ");
    	stb.append("  LEFT JOIN CHAIR_STD_DAT M1 ");
    	stb.append("    ON M1.YEAR = T1.YEAR ");
    	stb.append("   AND M1.SEMESTER = T1.SEMESTER ");
    	stb.append("   AND M1.SCHREGNO = T1.SCHREGNO ");
    	stb.append("  LEFT JOIN CHAIR_DAT M2 ");
    	stb.append("    ON M2.YEAR = M1.YEAR ");
    	stb.append("   AND M2.SEMESTER = M1.SEMESTER ");
    	stb.append("   AND M2.CHAIRCD = M1.CHAIRCD ");
    	stb.append("  LEFT JOIN SUBCLASS_MST M4 ");
    	stb.append("    ON M4.SUBCLASSCD = M2.SUBCLASSCD ");
    	if ("1".equals(_param._useCurriculumcd)) {
        	stb.append("   AND M4.CLASSCD = M2.CLASSCD ");
        	stb.append("   AND M4.SCHOOL_KIND = M2.SCHOOL_KIND ");
        	stb.append("   AND M4.CURRICULUM_CD = M2.CURRICULUM_CD ");
    	    stb.append("  LEFT JOIN CLASS_MST M3 ");
    	    stb.append("    ON M3.CLASSCD = M2.CLASSCD ");
    	    stb.append("   AND M3.SCHOOL_KIND = M2.SCHOOL_KIND ");
    	}
    	stb.append("   LEFT JOIN CREDIT_MST M5 ");
    	stb.append("     ON M5.YEAR = M2.YEAR ");
    	stb.append("    AND M5.GRADE = '" + hr_class.substring(0, 2) + "' ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("    AND M5.CLASSCD = M2.CLASSCD ");
    	    stb.append("    AND M5.SCHOOL_KIND = M2.SCHOOL_KIND ");
    	    stb.append("    AND M5.CURRICULUM_CD = M2.CURRICULUM_CD ");
    	}
    	stb.append("    AND M5.SUBCLASSCD = M2.SUBCLASSCD ");
    	stb.append(" WHERE ");
    	stb.append("  T1.YEAR = '" + _param._year + "' ");
    	stb.append("  AND T1.GRADE || T1.HR_CLASS = '" + hr_class + "' ");
    	stb.append("  AND T1.SEMESTER <= '" + _param._semester + "' ");
    	stb.append(" GROUP BY ");
    	stb.append("  T1.SCHREGNO, ");
    	if ("1".equals(_param._useCurriculumcd)) {
            stb.append("  M2.CLASSCD, ");
            stb.append("  M2.SCHOOL_KIND, ");
            stb.append("  M2.CURRICULUM_CD, ");
        	stb.append("  M3.CLASSNAME, ");
    	}
    	stb.append("  M2.SUBCLASSCD, ");
    	stb.append("  M4.SUBCLASSNAME ");
    	stb.append(" ORDER BY ");
    	stb.append("  T1.SCHREGNO, ");
    	if ("1".equals(_param._useCurriculumcd)) {
            stb.append("  M2.CLASSCD, ");
            stb.append("  M2.SCHOOL_KIND, ");
            stb.append("  M2.CURRICULUM_CD, ");
    	}
    	stb.append("  M2.SUBCLASSCD ");

    	return stb.toString();
    }

    //成績情報 Map(schregno, Map(結合subclass, 成績情報クラス))
    private Map getResultInfo(final DB2UDB db2, final String hr_class) {
    	Map retMap = new HashMap();

    	final String sql = getResultInfoSql(hr_class);
        ResultSet rs = null;
        PreparedStatement ps1 = null;
        try {
            ps1 = db2.prepareStatement(sql);
            rs = ps1.executeQuery();
            while (rs.next()) {
            	final String schregNo = rs.getString("SCHREGNO");
            	final String subclassCd = rs.getString("SUBCLASSCD");

            	final String sem1Score = rs.getString("SEM1_SCORE");
            	final String sem2Score = rs.getString("SEM2_SCORE");
            	final String sem9Score = rs.getString("SEM9_SCORE");
            	final String passreportCnt = rs.getString("PASSREPORT_CNT");
            	final String schoolingCnt = rs.getString("SCHOOLING_CNT");

            	final String classCd;
            	final String schoolKind;
            	final String curriculumCd;
            	if ("1".equals(_param._useCurriculumcd)) {
                    classCd = rs.getString("CLASSCD");
                    schoolKind = rs.getString("SCHOOL_KIND");
                    curriculumCd = rs.getString("CURRICULUM_CD");
            	} else {
                    classCd = "";
                    schoolKind = "";
                    curriculumCd = "";
            	}

            	ResultInfo addwk = new ResultInfo(classCd, schoolKind, curriculumCd, subclassCd, sem1Score, sem2Score, sem9Score, passreportCnt, schoolingCnt);
                Map subMap = (Map)retMap.get(schregNo);
                if (subMap == null) {
                	subMap = new LinkedMap();
                	retMap.put(schregNo, subMap);
                }
                final String subkey;
            	if ("1".equals(_param._useCurriculumcd)) {
            		subkey = classCd + "-" +  schoolKind + "-" +  curriculumCd + "-" + subclassCd;
            	} else {
                    subkey = subclassCd;
            	}
                subMap.put(subkey, addwk);
            }
        } catch (SQLException e) {
            log.error("Exception:", e);
        } finally {
            DbUtils.closeQuietly(null, ps1, rs);
        }
    	return retMap;
    }

    private String getResultInfoSql(final String hr_class) {
    	StringBuffer stb = new StringBuffer();

    	//前期後期成績、評定値、合格Report数、スクーリング回数毎に取得していく。
    	//上記の順に取得してくが、スクーリングの前後にキーの整理を行う。
    	////前期後期成績
    	stb.append(" WITH HISTSCORE_WKTBL AS ( ");
    	stb.append(" SELECT ");
    	stb.append("   T1.YEAR, ");
    	stb.append("   T1.SEMESTER, ");
    	stb.append("   T1.SCHREGNO, ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("   T1.CLASSCD, ");
    	    stb.append("   T1.SCHOOL_KIND, ");
    	    stb.append("   T1.CURRICULUM_CD, ");
    	}
    	stb.append("   T1.SUBCLASSCD, ");
    	stb.append("   T1.SCORE ");
    	stb.append(" FROM ");
    	stb.append("   RECORD_SCORE_HIST_DAT T1 ");
    	stb.append("   INNER JOIN CHAIR_DAT F1 ");
    	stb.append("     ON F1.YEAR = T1.YEAR ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("    AND F1.CLASSCD = T1.CLASSCD ");
    	    stb.append("    AND F1.SCHOOL_KIND = T1.SCHOOL_KIND ");
    	    stb.append("    AND F1.CURRICULUM_CD = T1.CURRICULUM_CD ");
    	}
    	stb.append("    AND F1.SUBCLASSCD = T1.SUBCLASSCD ");
    	stb.append("    AND F1.SEMESTER <= '" + _param._semester + "' ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + _param._year + "' ");
    	stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '990008' ");
    	stb.append("   AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR = '" + _param._year + "' AND GRADE || HR_CLASS = '" + hr_class + "' ) ");
    	//// 評定値
    	stb.append(" ), HYOTEI_SCORE_WKTBL AS ( ");
    	stb.append(" SELECT ");
    	stb.append("   T1.YEAR, ");
    	stb.append("   T1.SCHREGNO, ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("   T1.CLASSCD, ");
    	    stb.append("   T1.SCHOOL_KIND, ");
    	    stb.append("   T1.CURRICULUM_CD, ");
    	}
    	stb.append("   T1.SUBCLASSCD, ");
    	stb.append("   T1.SCORE ");
    	stb.append(" FROM ");
    	stb.append("   RECORD_SCORE_HIST_DAT T1 ");
    	stb.append("   INNER JOIN CHAIR_DAT F1 ");
    	stb.append("     ON F1.YEAR = T1.YEAR ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("    AND F1.CLASSCD = T1.CLASSCD ");
    	    stb.append("    AND F1.SCHOOL_KIND = T1.SCHOOL_KIND ");
    	    stb.append("    AND F1.CURRICULUM_CD = T1.CURRICULUM_CD ");
    	}
    	stb.append("    AND F1.SUBCLASSCD = T1.SUBCLASSCD ");
    	stb.append("    AND F1.SEMESTER <= '" + _param._semester + "' ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + _param._year + "' ");
    	stb.append("   AND T1.SEMESTER = '9' ");
    	stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '990009' ");
    	stb.append("   AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR = '" + _param._year + "' AND GRADE || HR_CLASS = '" + hr_class + "' ) ");
    	//// 合格Report数
    	stb.append(" ), CURYEAR_REPCNT_WKTBL AS ( ");
    	stb.append(" SELECT ");
    	stb.append("  T1.YEAR, ");
    	stb.append("  T1.SCHREGNO, ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("  T1.CLASSCD, ");
    	    stb.append("  T1.SCHOOL_KIND, ");
    	    stb.append("  T1.CURRICULUM_CD, ");
    	}
    	stb.append("  T1.SUBCLASSCD, ");
    	stb.append("  count(L1.NAMESPARE1) AS PASSREPORT_CNT ");
    	stb.append(" FROM ");
    	stb.append("  REP_PRESENT_DAT T1 ");
    	stb.append("  INNER JOIN CHAIR_DAT F1 ");
    	stb.append("     ON F1.YEAR = T1.YEAR ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("    AND F1.CLASSCD = T1.CLASSCD ");
    	    stb.append("    AND F1.SCHOOL_KIND = T1.SCHOOL_KIND ");
    	    stb.append("    AND F1.CURRICULUM_CD = T1.CURRICULUM_CD ");
    	}
    	stb.append("    AND F1.SUBCLASSCD = T1.SUBCLASSCD ");
    	stb.append("    AND F1.SEMESTER <= '" + _param._semester + "' ");
    	stb.append("  LEFT JOIN NAME_MST L1 ");
    	stb.append("    ON L1.NAMECD1 = 'M003' ");
    	stb.append("   AND L1.NAMECD2 = T1.GRAD_VALUE ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + _param._year + "' ");
    	stb.append("   AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR = '" + _param._year + "' AND GRADE || HR_CLASS = '" + hr_class + "' ) ");  //★クラスに変更しないとNG
    	stb.append(" GROUP BY ");
    	stb.append("  T1.YEAR, ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("  T1.CLASSCD, ");
    	    stb.append("  T1.SCHOOL_KIND, ");
    	    stb.append("  T1.CURRICULUM_CD, ");
    	}
    	stb.append("  T1.SUBCLASSCD, ");
    	stb.append("  T1.SCHREGNO ");
    	//// キー項目を整理
    	stb.append(" ), MASTCD_MARGEWK2TBL AS ( ");
    	stb.append(" SELECT DISTINCT ");
    	stb.append("   COALESCE(T1.YEAR, T2.YEAR) AS YEAR, ");
    	stb.append("   COALESCE(T1.SCHREGNO, T2.SCHREGNO) AS SCHREGNO, ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("   COALESCE(T1.CLASSCD, T2.CLASSCD) AS CLASSCD, ");
    	    stb.append("   COALESCE(T1.SCHOOL_KIND, T2.SCHOOL_KIND) AS SCHOOL_KIND, ");
    	    stb.append("   COALESCE(T1.CURRICULUM_CD, T2.CURRICULUM_CD) AS CURRICULUM_CD, ");
    	}
    	stb.append("   COALESCE(T1.SUBCLASSCD, T2.SUBCLASSCD) AS SUBCLASSCD ");
    	stb.append(" FROM ");
    	stb.append("   HISTSCORE_WKTBL T1 ");
    	stb.append("   FULL OUTER JOIN CURYEAR_REPCNT_WKTBL T2 ");
    	stb.append("     ON T2.YEAR = T1.YEAR ");
    	stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("    AND T2.CLASSCD = T1.CLASSCD ");
    	    stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
    	    stb.append("    AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
    	}
    	stb.append("    AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
    	//// スクーリング回数
    	stb.append(" ), CURYEAR_SCHOOLING_WKTBL AS ( ");
    	stb.append(" SELECT ");
    	stb.append("   T1.YEAR, ");
    	stb.append("   T1.SCHREGNO, ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("   L2.CLASSCD, ");
    	    stb.append("   L2.SCHOOL_KIND, ");
    	    stb.append("   L2.CURRICULUM_CD, ");
    	}
    	stb.append("   L2.SUBCLASSCD, ");
    	stb.append("   MAX(SCHOOLING_SEQ) AS SCHOOLING_CNT ");
    	stb.append(" FROM ");
    	stb.append("   SCH_ATTEND_DAT T1 ");
    	stb.append("   INNER JOIN CHAIR_STD_DAT L1 ");
    	stb.append("      ON L1.YEAR = T1.YEAR ");
    	stb.append("     AND L1.SCHREGNO = T1.SCHREGNO ");
    	stb.append("     AND L1.CHAIRCD = T1.CHAIRCD ");
    	stb.append("     AND L1.SEMESTER <= '" + _param._semester + "' ");
    	stb.append("   LEFT JOIN CHAIR_DAT L2 ");
    	stb.append("     ON L2.YEAR = L1.YEAR ");
    	stb.append("    AND L2.SEMESTER = L1.SEMESTER ");
    	stb.append("    AND L2.CHAIRCD = L1.CHAIRCD ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + _param._year + "' ");
    	stb.append("   AND SCHOOLING_SEQ IS NOT NULL ");
    	stb.append("   AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR = '" + _param._year + "' AND GRADE || HR_CLASS = '" + hr_class + "' ) ");  //★クラスに変更しないとNG
    	stb.append(" GROUP BY ");
    	stb.append("   T1.YEAR, ");
    	stb.append("   T1.SCHREGNO, ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("   L2.CLASSCD, ");
    	    stb.append("   L2.SCHOOL_KIND, ");
    	    stb.append("   L2.CURRICULUM_CD, ");
        }
    	stb.append("   L2.SUBCLASSCD ");
    	//// キー項目を整理
    	stb.append(" ), MASTCD_MARGEWKTBL AS ( ");
    	stb.append(" SELECT DISTINCT ");
    	stb.append("   COALESCE(T1.YEAR, T2.YEAR) AS YEAR, ");
    	stb.append("   COALESCE(T1.SCHREGNO, T2.SCHREGNO) AS SCHREGNO, ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("   COALESCE(T1.CLASSCD, T2.CLASSCD) AS CLASSCD, ");
    	    stb.append("   COALESCE(T1.SCHOOL_KIND, T2.SCHOOL_KIND) AS SCHOOL_KIND, ");
    	    stb.append("   COALESCE(T1.CURRICULUM_CD, T2.CURRICULUM_CD) AS CURRICULUM_CD, ");
    	}
    	stb.append("   COALESCE(T1.SUBCLASSCD, T2.SUBCLASSCD) AS SUBCLASSCD ");
    	stb.append(" FROM ");
    	stb.append("   MASTCD_MARGEWK2TBL T1 ");
    	stb.append("   FULL OUTER JOIN CURYEAR_SCHOOLING_WKTBL T2 ");
    	stb.append("     ON T2.YEAR = T1.YEAR ");
    	stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("    AND T2.CLASSCD = T1.CLASSCD ");
    	    stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
    	    stb.append("    AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
    	}
    	stb.append("    AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
    	//メイン処理
    	stb.append(" ) ");
    	stb.append(" SELECT ");
    	stb.append("   T1.YEAR, ");
    	stb.append("   T1.SCHREGNO, ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("   T1.CLASSCD, ");
    	    stb.append("   T1.SCHOOL_KIND, ");
    	    stb.append("   T1.CURRICULUM_CD, ");
    	}
    	stb.append("   T1.SUBCLASSCD, ");
    	stb.append("   T2.SCORE AS SEM1_SCORE, ");
    	stb.append("   T3.SCORE AS SEM2_SCORE, ");
    	stb.append("   T4.SCORE AS SEM9_SCORE, ");
    	stb.append("   T5.PASSREPORT_CNT, ");
    	stb.append("   T6.SCHOOLING_CNT ");
    	stb.append(" FROM ");
    	stb.append("   MASTCD_MARGEWKTBL T1 ");
    	stb.append("   LEFT JOIN HISTSCORE_WKTBL T2 ");
    	stb.append("     ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("    AND T2.CLASSCD = T1.CLASSCD ");
    	    stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
    	    stb.append("    AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
    	}
    	stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
    	stb.append("    AND T2.YEAR = T1.YEAR ");
    	stb.append("    AND T2.SEMESTER = '1' ");
    	stb.append("    AND T2.SEMESTER <= '" + _param._semester + "' ");
    	stb.append("   LEFT JOIN HISTSCORE_WKTBL T3 ");
    	stb.append("     ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("    AND T3.CLASSCD = T1.CLASSCD ");
    	    stb.append("    AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
    	    stb.append("    AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
    	}
    	stb.append("    AND T3.SCHREGNO = T1.SCHREGNO ");
    	stb.append("    AND T3.YEAR = T1.YEAR ");
    	stb.append("    AND T3.SEMESTER = '2' ");
    	stb.append("    AND T3.SEMESTER <= '" + _param._semester + "' ");
    	stb.append("   LEFT JOIN HYOTEI_SCORE_WKTBL T4 ");
    	stb.append("     ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("    AND T4.CLASSCD = T1.CLASSCD ");
    	    stb.append("    AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
    	    stb.append("    AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
    	}
    	stb.append("    AND T4.SCHREGNO = T1.SCHREGNO ");
    	stb.append("    AND T4.YEAR = T1.YEAR ");
    	stb.append("   LEFT JOIN CURYEAR_REPCNT_WKTBL T5 ");
    	stb.append("     ON T5.YEAR = T1.YEAR ");
    	stb.append("    AND T5.SCHREGNO = T1.SCHREGNO ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("    AND T5.CLASSCD = T1.CLASSCD ");
    	    stb.append("    AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
    	    stb.append("    AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
    	}
    	stb.append("    AND T5.SUBCLASSCD = T1.SUBCLASSCD ");
    	stb.append("   LEFT JOIN CURYEAR_SCHOOLING_WKTBL T6 ");
    	stb.append("     ON T6.YEAR = T1.YEAR ");
    	stb.append("    AND T6.SCHREGNO = T1.SCHREGNO ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("    AND T6.CLASSCD = T1.CLASSCD ");
    	    stb.append("    AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
    	    stb.append("    AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
    	}
    	stb.append("    AND T6.SUBCLASSCD = T1.SUBCLASSCD ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + _param._year + "' ");
    	stb.append(" ORDER BY ");
    	stb.append("   T1.YEAR, ");
    	stb.append("   T1.SCHREGNO, ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("   T1.CLASSCD, ");
    	    stb.append("   T1.SCHOOL_KIND, ");
    	    stb.append("   T1.CURRICULUM_CD, ");
    	}
    	stb.append("   T1.SUBCLASSCD ");

    	return stb.toString();
    }

    //合計情報 Map(schregno, 合計情報クラス))
    private Map getTotalInfo(final DB2UDB db2, final String hr_class) {
    	Map retMap = new HashMap();

    	final String sql = getTotalInfoSql(hr_class);
        ResultSet rs = null;
        PreparedStatement ps1 = null;
        try {
            ps1 = db2.prepareStatement(sql);
            rs = ps1.executeQuery();
            while (rs.next()) {
            	final String schregNo = rs.getString("SCHREGNO");
            	final String tounen_Get_Credit = rs.getString("TOUNEN_GET_CREDIT");
            	final String ruiseki_Get_Credit = rs.getString("RUISEKI_GET_CREDIT");
            	final String tounen_Creditval = rs.getString("TOUNEN_CREDITVAL");
            	final String ruiseki_Creditval = rs.getString("RUISEKI_CREDITVAL");
            	final String tounen_Syukkou_Cnt = rs.getString("TOUNEN_SYUKKOU_CNT");
            	final String ruiseki_Syukkou_Cnt = rs.getString("RUISEKI_SYUKKOU_CNT");
            	TotalInfo addwk = new TotalInfo(tounen_Get_Credit, ruiseki_Get_Credit, tounen_Creditval, ruiseki_Creditval, tounen_Syukkou_Cnt, ruiseki_Syukkou_Cnt);
            	retMap.put(schregNo, addwk);
            }
        } catch (SQLException e) {
            log.error("Exception:", e);
        } finally {
            DbUtils.closeQuietly(null, ps1, rs);
        }
    	return retMap;
    }

    private String getTotalInfoSql(final String hr_class) {
    	StringBuffer stb = new StringBuffer();

    	//当年度分の教科の合計
    	stb.append(" WITH CURYEAR_CREDIT_WKTBL AS ( ");
    	stb.append(" SELECT ");
    	stb.append("   T1.SCHREGNO, ");
    	stb.append("   SUM(T1.GET_CREDIT) AS TOTAL_GET_CREDIT ");
    	stb.append(" FROM ");
    	stb.append("   RECORD_SCORE_HIST_DAT T1 ");
    	stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ");
    	stb.append("     ON T2.YEAR = T1.YEAR ");
    	stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
    	stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '2012' ");
    	stb.append("   AND T1.SEMESTER <= '" + _param._semester + "' ");
    	stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '990008' ");
    	stb.append("   AND SCHOOL_KIND = 'H' ");
    	stb.append("   AND T2.GRADE || T2.HR_CLASS = '" + hr_class + "' ");
    	stb.append(" GROUP BY ");
    	stb.append("  T1.SCHREGNO ");
    	//過年度分の教科の合計
    	stb.append(" ), PASTYEAR_CREDIT_WKTBL AS ( ");
    	stb.append(" select ");
    	stb.append("  T1.SCHREGNO, ");
    	stb.append("  SUM(T1.GET_CREDIT) AS TOTAL_GET_CREDIT ");
    	stb.append(" FROM ");
    	stb.append("  RECORD_SCORE_HIST_DAT T1 ");
    	stb.append(" WHERE ");
    	stb.append("  T1.YEAR < '2012' ");
    	stb.append("  AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '990008' ");
    	stb.append("  AND SCHOOL_KIND = 'H' ");
    	stb.append("  AND EXISTS( SELECT 'X' FROM SCHREG_REGD_DAT TW1 WHERE TW1.YEAR = '2012' AND TW1.SCHREGNO = T1.SCHREGNO ");
    	stb.append("              AND TW1.GRADE || TW1.HR_CLASS = '" + hr_class + "' ");
    	stb.append("            ) ");
    	stb.append(" GROUP BY ");
    	stb.append("  T1.SCHREGNO ");
        //特別活動時数のベース
    	stb.append(" ), CURYEAR_SPACTBASE_WKTBL AS ( ");
    	stb.append(" SELECT ");
    	stb.append("  T1.YEAR, ");
    	stb.append("  T1.SEMESTER, ");
    	stb.append("  T1.SCHREGNO, ");
    	stb.append("  T1.CREDIT_TIME ");
    	stb.append(" FROM ");
    	stb.append("  SPECIALACT_ATTEND_DAT T1 ");
    	stb.append("  INNER JOIN V_NAME_MST M1 ");
    	stb.append("    ON M1.YEAR = T1.YEAR ");
    	stb.append("   AND M1.NAMECD1 = 'M026' ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("   AND M1.NAME1 = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
    	} else {
    	    stb.append("   AND M1.NAME1 = T1.SUBCLASSCD ");
    	}
    	stb.append(" WHERE ");
    	stb.append("   T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR = '2012' AND GRADE || HR_CLASS = '" + hr_class + "') ");
        //当年度分の特別活動時数の合計
    	stb.append(" ), CURYEAR_SPACT_WKTBL AS ( ");
    	stb.append(" SELECT ");
    	stb.append("  T1.SCHREGNO, ");
    	stb.append("  SUM(T1.CREDIT_TIME) AS CREDITVAL ");
    	stb.append(" FROM ");
    	stb.append("  CURYEAR_SPACTBASE_WKTBL T1 ");
    	stb.append(" WHERE ");
    	stb.append("  T1.YEAR = '2012' ");
    	stb.append("   AND T1.SEMESTER <= '" + _param._semester + "' ");
    	stb.append(" GROUP BY ");
    	stb.append("  T1.SCHREGNO ");
        //過年度分の特別活動時数の合計
    	stb.append(" ), PASTYEAR_SPACT_WKTBL AS ( ");
    	stb.append(" SELECT ");
    	stb.append("  T1.SCHREGNO, ");
    	stb.append("  SUM(T1.CREDIT_TIME) AS CREDITVAL ");
    	stb.append(" FROM ");
    	stb.append("  CURYEAR_SPACTBASE_WKTBL T1 ");
    	stb.append(" WHERE ");
    	stb.append("  T1.YEAR < '2012' ");
    	stb.append(" GROUP BY ");
    	stb.append("  T1.SCHREGNO ");
        //出校のベース
    	stb.append(" ), CURYEAR_ATTENDCNTBASE_WKTBL AS ( ");
    	stb.append(" SELECT DISTINCT ");
    	stb.append("  T1.YEAR, ");
    	stb.append("  T1.SCHREGNO, ");
    	stb.append("  T1.EXECUTEDATE ");
    	stb.append(" FROM ");
    	stb.append("  SCH_ATTEND_DAT T1 ");
    	stb.append("  LEFT JOIN NAME_MST M1 ");
    	stb.append("    ON M1.NAMECD1 = 'M001' ");
    	stb.append("   AND M1.NAMECD2 = T1.SCHOOLINGKINDCD ");
    	stb.append(" WHERE ");
    	stb.append("  M1.NAMESPARE1 = '1' ");
    	stb.append("  AND EXISTS(SELECT 'X' FROM SCHREG_REGD_DAT TW1 WHERE TW1.SCHREGNO = T1.SCHREGNO AND TW1.YEAR = '2012') ");
    	stb.append("  AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR = '2012' AND GRADE || HR_CLASS = '" + hr_class + "') ");
    	stb.append(" ORDER BY ");
    	stb.append("  T1.SCHREGNO ");
    	//当年度分の出校日数合計
    	stb.append(" ), CURYEAR_ATTENDCNT_WKTBL AS ( ");
    	stb.append(" SELECT ");
    	stb.append("   T1.SCHREGNO, ");
    	stb.append("   COUNT(T1.EXECUTEDATE) AS CNT ");
    	stb.append(" FROM ");
    	stb.append("   CURYEAR_ATTENDCNTBASE_WKTBL T1 ");
    	stb.append(" WHERE ");
    	stb.append("  T1.YEAR = '2012' ");
    	stb.append(" GROUP BY ");
    	stb.append("   T1.SCHREGNO ");
    	//過年度分の出校日数合計
    	stb.append(" ), PASTYEAR_ATTENDCNT_WKTBL AS ( ");
    	stb.append(" SELECT ");
    	stb.append("   T1.SCHREGNO, ");
    	stb.append("   COUNT(T1.EXECUTEDATE) AS CNT ");
    	stb.append(" FROM ");
    	stb.append("   CURYEAR_ATTENDCNTBASE_WKTBL T1 ");
    	stb.append(" WHERE ");
    	stb.append("  T1.YEAR < '2012' ");
    	stb.append(" GROUP BY ");
    	stb.append("   T1.SCHREGNO ");
    	stb.append(" ) ");
    	//まとめ
    	stb.append(" SELECT ");
    	stb.append("   COALESCE(COALESCE(T1.SCHREGNO, T2.SCHREGNO), T3.SCHREGNO) AS SCHREGNO, ");
    	stb.append("   T1.TOTAL_GET_CREDIT AS TOUNEN_GET_CREDIT, ");
    	stb.append("   (VALUE(T1.TOTAL_GET_CREDIT, 0) + VALUE(T1P.TOTAL_GET_CREDIT, 0)) AS RUISEKI_GET_CREDIT, ");
    	stb.append("   T2.CREDITVAL AS TOUNEN_CREDITVAL, ");
    	stb.append("   (T2.CREDITVAL + T2P.CREDITVAL) AS RUISEKI_CREDITVAL, ");
    	stb.append("   T3.CNT AS TOUNEN_SYUKKOU_CNT, ");
    	stb.append("   (VALUE(T3.CNT, 0) + VALUE(T3P.CNT, 0)) AS RUISEKI_SYUKKOU_CNT ");
    	stb.append(" FROM ");
    	stb.append("   CURYEAR_CREDIT_WKTBL T1 ");
    	stb.append("     LEFT JOIN PASTYEAR_CREDIT_WKTBL T1P ");
    	stb.append("       ON T1P.SCHREGNO = T1.SCHREGNO ");
    	stb.append("   FULL OUTER JOIN CURYEAR_SPACT_WKTBL T2 ");
    	stb.append("           ON T2.SCHREGNO = T1.SCHREGNO ");
    	stb.append("     LEFT JOIN PASTYEAR_SPACT_WKTBL T2P ");
    	stb.append("       ON T2P.SCHREGNO = T2.SCHREGNO ");
    	stb.append("   FULL OUTER JOIN CURYEAR_ATTENDCNT_WKTBL T3 ");
    	stb.append("           ON T3.SCHREGNO = T1.SCHREGNO ");
    	stb.append("     LEFT JOIN PASTYEAR_ATTENDCNT_WKTBL T3P ");
    	stb.append("       ON T3P.SCHREGNO = T3.SCHREGNO ");
    	stb.append(" ORDER BY ");
    	stb.append("   T1.SCHREGNO ");

    	return stb.toString();
    }

    private class Student {
    	private final String _schregNo;
    	private final String _grade;
    	private final String _hrClass;
    	private final String _attendNo;
    	private final String _hrName;
    	private final String _name;
    	private final String _staffName;
    	private Map _chairInfoMap;
    	private Map _resultInfoMap;
    	private TotalInfo _totalInf;

    	public Student(final String schregNo, final String grade, final String hrClass, final String attendNo, final String hrName, final String name, final String staffName) 	{
    	    _schregNo = schregNo;
    	    _grade = grade;
    	    _hrClass = hrClass;
    	    _attendNo = attendNo;
    	    _hrName = hrName;
    	    _name = name;
    	    _staffName = staffName;
    	}
    	public void setSubInfo(final Map chairInfoMap, final Map resultInfoMap, final TotalInfo totalInf) {
    		_chairInfoMap = chairInfoMap;
    		_resultInfoMap = resultInfoMap;
    		_totalInf = totalInf;
    	}
    }

    private class ChairInfo {
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _className;
        final String _subclassCd;
        final String _subclassName;
        final String _credits_Min;
        final String _credits_Max;
        public ChairInfo (final String classCd, final String schoolKind, final String curriculumCd, final String className, final String subclassCd, final String subclassName, final String credits_Min, final String credits_Max)
        {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _className = className;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _credits_Min = credits_Min;
            _credits_Max = credits_Max;
        }
    }

    private class ResultInfo {
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _sem1Score;
        final String _sem2Score;
        final String _sem9Score;
        final String _passreportCnt;
        final String _schoolingCnt;
        public ResultInfo (final String classCd, final String schoolKind, final String curriculumCd, final String subclassCd, final String sem1Score, final String sem2Score, final String sem9Score, final String passreportCnt, final String schoolingCnt)
        {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _sem1Score = sem1Score;
            _sem2Score = sem2Score;
            _sem9Score = sem9Score;
            _passreportCnt = passreportCnt;
            _schoolingCnt = schoolingCnt;
        }
    }

    private class TotalInfo {
        final String _tounen_Get_Credit;
        final String _ruiseki_Get_Credit;
        final String _tounen_Creditval;
        final String _ruiseki_Creditval;
        final String _tounen_Syukkou_Cnt;
        final String _ruiseki_Syukkou_Cnt;
        public TotalInfo (final String tounen_Get_Credit, final String ruiseki_Get_Credit, final String tounen_Creditval, final String ruiseki_Creditval, final String tounen_Syukkou_Cnt, final String ruiseki_Syukkou_Cnt)
        {
            _tounen_Get_Credit = tounen_Get_Credit;
            _ruiseki_Get_Credit = ruiseki_Get_Credit;
            _tounen_Creditval = tounen_Creditval;
            _ruiseki_Creditval = ruiseki_Creditval;
            _tounen_Syukkou_Cnt = tounen_Syukkou_Cnt;
            _ruiseki_Syukkou_Cnt = ruiseki_Syukkou_Cnt;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 68662 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _ctlsemester;
        private final String[] _selectClass;
        private final String _ctrlDate;
        private final Map _selectClsMap;  //学年・クラスコードで出力順番を並べ替え

        private final String _useCurriculumcd;
        private String _nendo;
        private Map _teacherNameMap;

        private Map _sammaryChairInfoMap;

        private final String _documentRoot;
        private final String _imageDir;
        private String _keninWakuImg;

        private List _jobStampNameList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _selectClass = request.getParameterValues("CLASS_SELECTED");
            _selectClsMap = new TreeMap();
            for (int ii = 0;ii < _selectClass.length;ii++) {
            	_selectClsMap.put(_selectClass[ii], _selectClass[ii]);
            }
            _ctlsemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _sammaryChairInfoMap = new TreeMap();

            _documentRoot = request.getParameter("DOCUMENTROOT"); // 陰影保管場所 NO001
            final KNJ_Control.ReturnVal value = new KNJ_Control().Control(db2);
            _imageDir = value.val4;

            _keninWakuImg = null;
            _jobStampNameList = new ArrayList();
            try{
                //年度
                _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度 ";
                _teacherNameMap = new HashMap();
                for (int ii = 0;ii < _selectClass.length;ii++) {
                	_teacherNameMap.put(_selectClass[ii], getTeacherName(db2, _selectClass[ii]));
                }
                _jobStampNameList = getJobStampName(db2);
                _keninWakuImg = getImageFile("KNJD615_keninwaku2.jpg");

            } catch( Exception ex ){
                log.error("setHead error!" + ex);
            }

            if (_nendo == null) {
                _nendo = "";
            }
        }

        private String getTeacherName(final DB2UDB db2, final String hrClass) {
        	String retStr = "";
        	StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("   L1.STAFFNAME ");
        	stb.append(" FROM");
        	stb.append("   SCHREG_REGD_HDAT T1");
        	stb.append("   LEFT JOIN STAFF_MST L1 ");
        	stb.append("     ON L1.STAFFCD = T1.TR_CD1 ");
        	stb.append(" WHERE ");
        	stb.append("   T1.YEAR = '" + _year + "' ");
        	stb.append("   AND T1.SEMESTER = '" + _semester + "' ");
        	stb.append("   AND T1.GRADE || T1.HR_CLASS = '" + hrClass + "' ");
        	stb.append(" ORDER BY ");
        	stb.append("   T1.SEMESTER DESC ");

        	final String sql = stb.toString();
            ResultSet rs = null;
            PreparedStatement ps1 = null;
            try {
                ps1 = db2.prepareStatement(sql);
                rs = ps1.executeQuery();
                if (rs.next()) {
                	retStr = rs.getString("STAFFNAME");
                }
            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                DbUtils.closeQuietly(null, ps1, rs);
            }
        	return retStr;
        }

        private List getJobStampName(final DB2UDB db2) {
        	List retList = new ArrayList();
        	StringBuffer stb = new StringBuffer();
        	//職位の低い人から出力していくので、並びを逆にしているので、注意。
        	stb.append(" SELECT ");
        	stb.append("   NAME1 ");
        	stb.append(" FROM");
        	stb.append("   V_NAME_MST ");
        	stb.append(" WHERE ");
        	stb.append("   YEAR = '" + _year + "' ");
        	stb.append("   AND NAMECD1 = 'D055' ");
        	stb.append(" ORDER BY ");
        	stb.append("   NAMECD2 DESC ");

        	final String sql = stb.toString();
            ResultSet rs = null;
            PreparedStatement ps1 = null;
            try {
                ps1 = db2.prepareStatement(sql);
                rs = ps1.executeQuery();
                while (rs.next()) {
                	retList.add(StringUtils.defaultString(rs.getString("NAME1"), ""));
                }
            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                DbUtils.closeQuietly(null, ps1, rs);
            }
        	return retList;
        }

        /**
         * 写真データファイルの取得
         */
        private String getImageFile(final String filename) {
            if (null == _documentRoot) {
                return null;
            } // DOCUMENTROOT
            if (null == _imageDir) {
                return null;
            }
            StringBuffer stb = new StringBuffer();
            stb.append(_documentRoot);
            stb.append("/");
            stb.append(_imageDir);
            stb.append("/");
            stb.append(filename);
            File file1 = new File(stb.toString());
            if (!file1.exists()) {
            	log.warn("ImageFile not exist:"+stb.toString());
                return null;
            } // 写真データ存在チェック用
            return stb.toString();
        }
    }
}

// eof

