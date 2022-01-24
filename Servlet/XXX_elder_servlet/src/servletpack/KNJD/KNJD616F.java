// kanji=漢字
/*
 * $Id: 8b5276b47e477f67efec8a4ab8c683ec8d2b009b $
 */
package servletpack.KNJD;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD616F {

    private static final Log log = LogFactory.getLog(KNJD616F.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String CLASSCD99 = "99";

    private boolean _hasData;
    Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            response.setContentType("application/pdf");

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            _param = createParam(request, db2);

            printMain(db2, svf);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (null != db2) {
                try {
                    db2.commit();
                    db2.close();
                } catch (Exception ex) {
                    log.error("db close error!", ex);
                }
            }
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    private void printMain(
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
    	final Map prtSubClsMap = new TreeMap();
    	final Map scoreInfoMap = getScoreInfo(db2, prtSubClsMap);
    	final Map dTotalMap = getDownTotalInfo(db2);
    	final Map kekkaMap = loadSubclassAttend(db2);
        final Map hrClasses = getHrClassMap(db2, scoreInfoMap, dTotalMap, kekkaMap);
        if (hrClasses.isEmpty()) {
            return;
        }

        for (Iterator ite = hrClasses.keySet().iterator();ite.hasNext();) {
        	final String grHrStr = (String)ite.next();
        	final HrClsInfo hrCls = (HrClsInfo)hrClasses.get(grHrStr);
        	if (hrCls._students.size() == 0) continue;

            printMain(svf, hrCls, prtSubClsMap);
        }
    }
	private void printMain(final Vrw32alp svf, final HrClsInfo hrCls, final Map prtSubClsMap) {
        svf.VrSetForm("KNJD616F.frm", 4);
		setTitle(svf, hrCls);
		final int rowMax = 45;
		List subList = rebuildList(hrCls._students, rowMax);  //1ページ分で再構成する。
		int rowCnt = 0;
		//一度、科目別成績"以外"を出力する。
		for (final Iterator ite = subList.iterator();ite.hasNext();) {
			List studentSubList = (List)ite.next();
	        for (final Iterator it = studentSubList.iterator(); it.hasNext();) {
	            final Student student = (Student) it.next();
	            log.info(" schregno = " + student._schregno);
	            rowCnt++;
	            svf.VrsOutn("NUMBER", rowCnt ,String.valueOf(Integer.parseInt(student._attendno)));  //出席番号
	            final int nlen = KNJ_EditEdit.getMS932ByteLength(student._name);
	            final String nfield = nlen > 30 ? "2" : "1";
	            svf.VrsOutn("NAME" + nfield, rowCnt, student._name);  //氏名

	            ScoreInfo prtwk = null;
	            String findStr1 = CLASSCD99 + "-" + _param._schoolKind + "-09-" + SUBCLASSCD999999;
	            String findStr2 = CLASSCD99 + "-" + _param._schoolKind + "-99-" + SUBCLASSCD999999;
	            if (student._scoreMap.containsKey(findStr2)) {  //"09"が通常利用されている可能性があるので"99"を優先チェック
	            	prtwk = (ScoreInfo)student._scoreMap.get(findStr2);
	            } else if (student._scoreMap.containsKey(findStr1)) {
	            	prtwk = (ScoreInfo)student._scoreMap.get(findStr1);
	            }
	            if (prtwk != null) {
		            svf.VrsOutn("TOTAL1", rowCnt,prtwk._score09);        //評定合計
		            svf.VrsOutn("AVERAGE1", rowCnt, prtwk._avg09);      //評定平均
		            svf.VrsOutn("CLASS_RANK1", rowCnt, prtwk._class_Rank09);   //評定順位(クラス)
		            svf.VrsOutn("RANK1", rowCnt, prtwk._course_Rank09);         //評定順位(コース)
	            }
	            if (student._attendMap.containsKey(SEMEALL)) {
	                Attendance att = (Attendance)student._attendMap.get(SEMEALL);
	                svf.VrsOutn("SUSPEND_DAY1", rowCnt, String.valueOf(att._suspend + att._mourning));  //出停・忌引
	                svf.VrsOutn("PRESENT1", rowCnt, String.valueOf(att._mLesson));   //要出席日数
	                svf.VrsOutn("ABSENCE1", rowCnt, String.valueOf(att._sick));      //欠席日数
	                svf.VrsOutn("ATTEND1", rowCnt, String.valueOf(att._present));    //出席日数
	                svf.VrsOutn("LATE1", rowCnt, String.valueOf(att._late));         //遅刻
	                svf.VrsOutn("EARLY1", rowCnt, String.valueOf(att._early));       //早退
	                svf.VrsOutn("KEKKA1", rowCnt, String.valueOf(att._kekkaJisu));   //総欠課時数
	            }
	        }
	        //以下、record内の出力
			for (Iterator itr = prtSubClsMap.keySet().iterator();itr.hasNext();) {  //教科分回す。
				final String clscd = (String)itr.next();
				if (CLASSCD99.equals(clscd)) {
					continue;
				}
				final Map subMap = (Map)prtSubClsMap.get(clscd);
				for (Iterator its = subMap.keySet().iterator();its.hasNext();) {
					final String subclscd = (String)its.next();
					final SubclsInfo sinfo = (SubclsInfo)subMap.get(subclscd);
				    final String searchSubclsCd = sinfo.getSubclsCd(true);
				    final String normSubclsCd = sinfo.getSubclsCd(false);
				    if (_param._d026List.contains(normSubclsCd)) continue;  //名称MD029に入っているなら、出力しない。
					svf.VrsOut("course1", sinfo._subclassName);
					svf.VrsOut("HR_T", sinfo._subclassName);
				    int rowScoreCnt = 1;
		            for (final Iterator it = studentSubList.iterator(); it.hasNext();) {
		                final Student student = (Student) it.next();
		                if (student._scoreMap.containsKey(searchSubclsCd)) {
		                	ScoreInfo prtwk = (ScoreInfo)student._scoreMap.get(searchSubclsCd);
	        	            svf.VrsOut("GRADING1_" + rowScoreCnt, prtwk._score08);   //評価
	        	            svf.VrsOut("VALUE1_" + rowScoreCnt, prtwk._score09);     //評定
                            if (student._attendSubclsMap.containsKey(normSubclsCd)) {
                            	Attendance attwk = (Attendance)student._attendSubclsMap.get(normSubclsCd);
                            	if (attwk._kekkaJisu > (attwk._lesson/3)) {
                            		svf.VrAttribute("ABSENT" + rowScoreCnt + "_1", "Paint=(1,50,1),Bold=1");
                            		svf.VrAttribute("ABSENT" + rowScoreCnt + "_2", "Paint=(1,50,1),Bold=1");
    	        	                svf.VrsOut("ABSENT" + rowScoreCnt + "_2", "*");    //欠課時数
                            	}
	        	                svf.VrsOut("ABSENT" + rowScoreCnt + "_1", String.valueOf(attwk._kekkaJisu));    //欠課時数
                            }
		                }
		                rowScoreCnt++;
	                }
		            if (hrCls._dTotalMap.containsKey(searchSubclsCd)) {
		            	DownTotalInfo pwk = (DownTotalInfo)hrCls._dTotalMap.get(searchSubclsCd);
		                svf.VrsOut("TOTAL_GRADING1", pwk._score08);  //(科目別)評価合計
		                svf.VrsOut("TOTAL_VALUE1", pwk._score09);    //(科目別)評定合計
		                svf.VrsOut("TOTAL_GRADING2", pwk._count08);  //(科目別)評価人数
		                svf.VrsOut("TOTAL_VALUE2", pwk._count09);    //(科目別)評定人数
		                //svf.VrsOut("TOTAL_ABSENT2", "");
		                svf.VrsOut("TOTAL_GRADING3", pwk._avg08);            //(科目別)評価平均
		                svf.VrsOut("TOTAL_VALUE3", pwk._avg09);              //(科目別)評定平均
		            }
			        svf.VrEndRecord();
		        }
			}
			svf.VrEndPage();
			if (prtSubClsMap.size() > 0) {  //出力する教科自体が無いなら、出力しない。setFormが4指定で設定しているので、vrEndRecordしないと出力されない場合があるため。
				_hasData = true;
			}
		}
	}
	private void setTitle(final Vrw32alp svf, final HrClsInfo hrCls) {
		svf.VrsOut("year2", _param._year + "年度");
		svf.VrsOut("TITLE", "教科別成績一覧表");
		svf.VrsOut("COURSE_NAME", hrCls._cousecodename);
		svf.VrsOut("HR_NAME", hrCls._hrName);
		final int trlen = KNJ_EditEdit.getMS932ByteLength(hrCls._staffName);
		final String trfield = trlen > 30 ? "2" : "";
		svf.VrsOut("HR_TEACHER" + trfield, hrCls._staffName);
		svf.VrsOut("SCHOOL_NAME", _param._cerifSchoolName);
	}

	private List rebuildList(final List studentList, final int cntMax) {
		List retList = new ArrayList();
		List addwk = new ArrayList();
		retList.add(addwk);
		for (final Iterator ite = studentList.iterator();ite.hasNext();) {
			Student mvwk = (Student)ite.next();
			if (addwk.size() >= cntMax) {
				addwk = new ArrayList();
				retList.add(addwk);
			}
			addwk.add(mvwk);
		}
		return retList;
	}

    /**
     * 生徒を取得
     */
    public Map getHrClassMap(final DB2UDB db2, final Map scoreInfoMap, final Map dTotalMap, final Map kekkaMap) {
        final String sql = getHrClassMapSql();
        if (_param._isOutputDebug) {
        	log.info(" student sql = " + sql);
        }

        final Map hrClasses = new LinkedMap();
        HrClsInfo hrCls = null;

        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
        	final Map row = (Map) it.next();
            final String attendno = NumberUtils.isDigits(KnjDbUtils.getString(row, "ATTENDNO")) ? String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "ATTENDNO"))) : KnjDbUtils.getString(row, "ATTENDNO");
            final String staffname = StringUtils.defaultString(KnjDbUtils.getString(row, "STAFFNAME"));

            final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
            final String name = KnjDbUtils.getString(row, "NAME");
            final String hr_Name = KnjDbUtils.getString(row, "HR_NAME");
            final String grade = KnjDbUtils.getString(row, "GRADE");
            final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
            final String coursecd = KnjDbUtils.getString(row, "COURSECD");
            final String majorcd = KnjDbUtils.getString(row, "MAJORCD");
            final String course = KnjDbUtils.getString(row, "COURSE");
            final String majorname = KnjDbUtils.getString(row, "MAJORNAME");
            final String cousecodename = KnjDbUtils.getString(row, "COURSECODENAME");
            final String grade_Cd = KnjDbUtils.getString(row, "GRADE_CD");
            final String grade_Name1 = KnjDbUtils.getString(row, "GRADE_NAME1");
            final String hr_Class_Name1 = KnjDbUtils.getString(row, "HR_CLASS_NAME1");
            final String ent_Year = KnjDbUtils.getString(row, "ENT_YEAR");

            final Student student = new Student(schregno, grade_Cd, grade_Name1, hr_Name, staffname, attendno, grade, hrClass, coursecd, majorcd, course, majorname, cousecodename, hr_Class_Name1, ent_Year);

            student._name = null != KnjDbUtils.getString(row, "NAME_SETUP") ? KnjDbUtils.getString(row, "REAL_NAME") : KnjDbUtils.getString(row, "NAME");
            if (scoreInfoMap.containsKey(schregno)) {
            	student._scoreMap = (Map)scoreInfoMap.get(schregno);
            }
            if (kekkaMap.containsKey(schregno)) {
            	student._attendSubclsMap = (Map)kekkaMap.get(schregno);
            }

            loadAttend(db2, student);

            final String sKey = grade + hrClass;
            if (hrClasses.containsKey(sKey)) {
            	hrCls = (HrClsInfo)hrClasses.get(sKey);
            } else {
            	hrCls = new HrClsInfo(grade, hrClass, grade_Cd, grade_Name1, hr_Name, hr_Class_Name1, staffname, course, cousecodename);
            	hrClasses.put(sKey, hrCls);
            }
            hrCls._students.add(student);
            if (dTotalMap.containsKey(sKey)) {
            	hrCls._dTotalMap = (Map)dTotalMap.get(sKey);
            }
        }
        return hrClasses;
    }

    private String getHrClassMapSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("     SELECT  REGD.SCHREGNO");
        stb.append("            ,REGD.SEMESTER ");
        stb.append("            ,BASE.NAME ");
        stb.append("            ,BASE.REAL_NAME ");
        stb.append("            ,NAMESD.SCHREGNO AS NAME_SETUP ");
        stb.append("            ,REGDH.HR_NAME ");
        stb.append("            ,STFM.STAFFNAME ");
        stb.append("            ,REGD.ATTENDNO ");
        stb.append("            ,REGD.GRADE ");
        stb.append("            ,REGD.HR_CLASS ");
        stb.append("            ,REGD.COURSECD ");
        stb.append("            ,REGD.MAJORCD ");
        stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
        stb.append("            ,W9.MAJORNAME ");
        stb.append("            ,W10.COURSECODENAME ");
        stb.append("            ,REGDG.GRADE_CD ");
        stb.append("            ,REGDG.GRADE_NAME1 ");
        stb.append("            ,REGDH.HR_CLASS_NAME1 ");
        stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
        stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
        stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
        stb.append("                  WHEN TRANSF.SCHREGNO IS NOT NULL THEN 1 ");
        stb.append("                  ELSE 0 END AS LEAVE ");
        stb.append("            ,BASE.BIRTHDAY ");
        stb.append("     FROM    SCHREG_REGD_DAT REGD ");
        stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = '" + _param._year + "' AND W2.SEMESTER = REGD.SEMESTER AND W2.GRADE = '" + _param._grade + "' ");
        //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
        stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = REGD.SCHREGNO ");
        stb.append("                  AND W3.GRD_DIV IN('2','3') ");
        stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + _param._date + "' THEN W2.EDATE ELSE '" + _param._date + "' END ");
        //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
        stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = REGD.SCHREGNO ");
        stb.append("                  AND W4.ENT_DIV IN('4','5') ");
        stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + _param._date + "' THEN W2.EDATE ELSE '" + _param._date + "' END ");
        //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
        stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT TRANSF ON TRANSF.SCHREGNO = REGD.SCHREGNO ");
        stb.append("                  AND TRANSF.TRANSFERCD IN ('1','2') ");
        stb.append("                  AND CASE WHEN W2.EDATE < '" + _param._date + "' THEN W2.EDATE ELSE '" + _param._date + "' END BETWEEN TRANSF.TRANSFER_SDATE AND TRANSF.TRANSFER_EDATE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
        stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR AND REGDG.GRADE = REGD.GRADE ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_NAME_SETUP_DAT NAMESD ON NAMESD.SCHREGNO = REGD.SCHREGNO AND NAMESD.DIV = '03' ");
        stb.append("     LEFT JOIN STAFF_MST STFM ON STFM.STAFFCD = REGDH.TR_CD1 ");
        stb.append("     LEFT JOIN MAJOR_MST W9 ON W9.COURSECD = REGD.COURSECD ");
        stb.append("                  AND W9.MAJORCD = REGD.MAJORCD ");
        stb.append("     LEFT JOIN COURSECODE_MST W10 ON W10.COURSECODE = REGD.COURSECODE ");
        stb.append("     WHERE   REGD.YEAR = '" + _param._year + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        } else {
            stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
            stb.append("                    WHERE S1.SCHREGNO = REGD.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
        }
        stb.append("         AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("     ORDER BY ");
        stb.append("         REGD.GRADE ");
        stb.append("       , REGD.HR_CLASS ");
        stb.append("       , REGD.ATTENDNO ");
        return stb.toString();
    }

    private Map getScoreInfo(final DB2UDB db2, final Map sortSubclsMap) {
    	final Map retMap = new LinkedMap();
    	final String query = getScoreInfoSql();

        PreparedStatement ps = null;
        Map addwk = null;
        Map addclsMap = null;
        try {
            ps = db2.prepareStatement(query);

            for (final Iterator rit = KnjDbUtils.query(db2, ps, null).iterator();rit.hasNext();) {
           	    final Map row = (Map) rit.next();

           	    final String kStr = KnjDbUtils.getString(row, "SCHREGNO");
           	    final String classcd = KnjDbUtils.getString(row, "CLASSCD");
           	    final String schoolkind = KnjDbUtils.getString(row, "SCHOOL_KIND");
           	    final String curriculumcd = KnjDbUtils.getString(row, "CURRICULUM_CD");
           	    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
           	    final String classname = KnjDbUtils.getString(row, "CLASSNAME");
           	    final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
           	    final String kwkcurriculumcd = (curriculumcd == null || "".equals(curriculumcd)) ? "" : (curriculumcd.length() > 1 ? curriculumcd : "0" + curriculumcd);  //TreeMapの自動ソートで、桁違いのソートを合わせる為に"0"埋め
           	    final String kSubcls = classcd + "-" + schoolkind + "-" + kwkcurriculumcd + "-" + subclasscd;
                final ScoreInfo sinfo = new ScoreInfo(
                	kStr,
                	classcd,
                	schoolkind,
                	curriculumcd,
                	subclasscd,
                    KnjDbUtils.getString(row, "SCORE08"),
                    KnjDbUtils.getString(row, "SCORE09"),
                    KnjDbUtils.getString(row, "AVG08"),
                    KnjDbUtils.getString(row, "AVG09"),
                    KnjDbUtils.getString(row, "CLASS_RANK09"),
                    KnjDbUtils.getString(row, "COURSE_RANK09")
                );
                final SubclsInfo scls = new SubclsInfo(classcd, schoolkind, curriculumcd, subclasscd, classname, subclassname);
                if (kSubcls != null && !"---".equals(kSubcls)) { //科目コードの無いデータは出力できないので除外
                	//データの登録
                    if (retMap.containsKey(kStr)) {
                	    addwk = (Map)retMap.get(kStr);
                    } else {
                	    addwk = new LinkedMap();
                	    retMap.put(kStr, addwk);
                    }
                    addwk.put(kSubcls, sinfo);
                    //科目コードの登録
                    if (sortSubclsMap.containsKey(classcd)) {
                    	addclsMap = (Map)sortSubclsMap.get(classcd);
                    } else {
                    	addclsMap = new TreeMap();
                    	sortSubclsMap.put(classcd, addclsMap);
                    }
                	addclsMap.put(subclasscd, scls);
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }

    	return retMap;

    }

    private String getScoreInfoSql() {
    	final StringBuffer stb = new StringBuffer();
    	stb.append(" WITH SCORE_BASE8 AS ( ");
    	stb.append(" SELECT ");
    	stb.append("     T1.* ");
    	stb.append(" FROM ");
    	stb.append("     RECORD_RANK_SDIV_DAT T1 ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + _param._year + "' ");
    	stb.append("   AND T1.SEMESTER = '" + SEMEALL + "' ");
    	stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '990008' ");
    	stb.append("   AND T1.SCHREGNO IN (SELECT ");
    	stb.append("                         SCHREGNO ");
    	stb.append("                     FROM ");
    	stb.append("                         SCHREG_REGD_DAT ");
    	stb.append("                     WHERE ");
    	stb.append("                         YEAR = '" + _param._year + "' AND ");
    	stb.append("                         SEMESTER = '" + _param._ctrlSemester + "' AND ");
    	stb.append("                         GRADE || HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
    	stb.append("                     ) ");
    	stb.append("   AND (T1.CLASSCD < '90' OR T1.CLASSCD = '" + CLASSCD99 + "' ) ");
    	stb.append("   AND T1.SUBCLASSCD NOT IN ('333333', '555555', '99999A', '99999B') ");
    	stb.append("   AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ( ");
    	stb.append("         SELECT ");
    	stb.append("           ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS IGNORE_SUBCLASS ");
    	stb.append("         FROM ");
    	stb.append("           SUBCLASS_REPLACE_DAT ");
    	stb.append("         WHERE ");
    	stb.append("           REPLACECD = '1' ");
    	stb.append("           AND YEAR = '" + _param._year + "' ");
    	stb.append("           AND ANNUAL = '" + _param._gradeCd + "' ");
    	stb.append("           AND ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD <> GRADING_CLASSCD || '-' || GRADING_SCHOOL_KIND || '-' || GRADING_CURRICULUM_CD || '-' || GRADING_SUBCLASSCD ");
    	stb.append("     ) ");
    	stb.append(" ), SCORE_BASE9 AS ( ");
    	stb.append(" SELECT ");
    	stb.append("     T1.* ");
    	stb.append(" FROM ");
    	stb.append("     RECORD_RANK_SDIV_DAT T1 ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + _param._year + "' ");
    	stb.append("   AND T1.SEMESTER = '" + SEMEALL + "' ");
    	stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '990009' ");
    	stb.append("   AND T1.SCHREGNO IN (SELECT ");
    	stb.append("                         SCHREGNO ");
    	stb.append("                     FROM ");
    	stb.append("                         SCHREG_REGD_DAT ");
    	stb.append("                     WHERE ");
    	stb.append("                         YEAR = '" + _param._year + "' AND ");
    	stb.append("                         SEMESTER = '" + _param._ctrlSemester + "' AND ");
    	stb.append("                         GRADE || HR_CLASS IN  " + SQLUtils.whereIn(true, _param._categorySelected));
    	stb.append("                     ) ");
    	stb.append("   AND (T1.CLASSCD < '90' OR T1.CLASSCD = '" + CLASSCD99 + "') ");
    	stb.append("   AND T1.SUBCLASSCD NOT IN ('333333', '555555', '99999A', '99999B') ");
    	stb.append("   AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ( ");
    	stb.append("         SELECT ");
    	stb.append("           ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS IGNORE_SUBCLASS ");
    	stb.append("         FROM ");
    	stb.append("           SUBCLASS_REPLACE_DAT ");
    	stb.append("         WHERE ");
    	stb.append("           REPLACECD = '1' ");
    	stb.append("           AND YEAR = '" + _param._year + "' ");
    	stb.append("           AND ANNUAL = '" + _param._gradeCd + "' ");
    	stb.append("           AND ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD <> GRADING_CLASSCD || '-' || GRADING_SCHOOL_KIND || '-' || GRADING_CURRICULUM_CD || '-' || GRADING_SUBCLASSCD ");
    	stb.append("     ) ");
    	stb.append(" ), SCORE_BASE AS ( ");
    	stb.append(" SELECT ");
    	stb.append("     CASE WHEN T2.SCHREGNO IS NOT NULL THEN T2.SCHREGNO ELSE T1.SCHREGNO END AS SCHREGNO, ");
    	stb.append("     CASE WHEN T2.CLASSCD IS NOT NULL THEN T2.CLASSCD ELSE T1.CLASSCD END AS CLASSCD, ");
    	stb.append("     CASE WHEN T2.SCHOOL_KIND IS NOT NULL THEN T2.SCHOOL_KIND ELSE T1.SCHOOL_KIND END AS SCHOOL_KIND, ");
    	stb.append("     CASE WHEN T2.CURRICULUM_CD IS NOT NULL THEN T2.CURRICULUM_CD ELSE T1.CURRICULUM_CD END AS CURRICULUM_CD, ");
    	stb.append("     CASE WHEN T2.SUBCLASSCD IS NOT NULL THEN T2.SUBCLASSCD ELSE T1.SUBCLASSCD END AS SUBCLASSCD, ");
    	stb.append("     T1.SCORE AS SCORE08, ");
    	stb.append("     T2.SCORE AS SCORE09, ");
    	stb.append("     DECIMAL(INT(T1.AVG*10.0+0.5)/10.0, 3, 1) AS AVG08, ");
    	stb.append("     DECIMAL(INT(T2.AVG*10.0+0.5)/10.0, 3, 1) AS AVG09, ");
    	stb.append("     T2.CLASS_RANK AS CLASS_RANK09, ");
    	stb.append("     T2.COURSE_RANK AS COURSE_RANK09 ");
    	stb.append("      ");
    	stb.append(" FROM ");
    	stb.append("     SCORE_BASE8 T1 ");
    	stb.append("     FULL JOIN SCORE_BASE9 T2 ");
    	stb.append("       ON T2.YEAR = T1.YEAR ");
    	stb.append("      AND T2.SEMESTER = T1.SEMESTER ");
    	stb.append("      AND T2.TESTKINDCD = T1.TESTKINDCD ");
    	stb.append("      AND T2.TESTITEMCD = T1.TESTITEMCD ");
    	stb.append("      AND T2.CLASSCD = T1.CLASSCD ");
    	stb.append("      AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
    	stb.append("      AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
    	stb.append("      AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
    	stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
    	stb.append(" ) ");
    	stb.append(" SELECT ");
    	stb.append("   T1.*, ");
    	stb.append("   T2.CLASSNAME, ");
    	stb.append("   T3.SUBCLASSNAME ");
    	stb.append(" FROM ");
    	stb.append("   SCORE_BASE T1 ");
    	stb.append("   LEFT JOIN CLASS_MST T2 ");
    	stb.append("     ON T2.CLASSCD = T1.CLASSCD ");
    	stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
    	stb.append("   LEFT JOIN SUBCLASS_MST T3 ");
    	stb.append("     ON T3.CLASSCD = T1.CLASSCD ");
    	stb.append("    AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
    	stb.append("    AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
    	stb.append("    AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
    	stb.append(" ORDER BY ");
    	stb.append("   T1.SCHREGNO, ");
    	stb.append("   T1.SUBCLASSCD ");
    	return stb.toString();
    }

    private Map getDownTotalInfo(final DB2UDB db2) {
    	final Map retMap = new LinkedMap();
    	final String query = getDownTotalInfoSql();

        PreparedStatement ps = null;
        Map addclsMap = null;
        try {
            ps = db2.prepareStatement(query);

            for (final Iterator rit = KnjDbUtils.query(db2, ps, null).iterator();rit.hasNext();) {
           	    final Map row = (Map) rit.next();
           	    final String grade = KnjDbUtils.getString(row, "GRADE");
           	    final String hr_Class = KnjDbUtils.getString(row, "HR_CLASS");
           	    final String classcd = KnjDbUtils.getString(row, "CLASSCD");
           	    final String school_Kind = KnjDbUtils.getString(row, "SCHOOL_KIND");
           	    final String curriculum_Cd = KnjDbUtils.getString(row, "CURRICULUM_CD");
           	    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
           	    final String score08 = KnjDbUtils.getString(row, "SCORE08");
           	    final String score09 = KnjDbUtils.getString(row, "SCORE09");
           	    final String count08 = KnjDbUtils.getString(row, "COUNT08");
           	    final String count09 = KnjDbUtils.getString(row, "COUNT09");
           	    final String avg08 = KnjDbUtils.getString(row, "AVG08");
           	    final String avg09 = KnjDbUtils.getString(row, "AVG09");
           	    DownTotalInfo dtInfo = new DownTotalInfo(grade, hr_Class, classcd, school_Kind, curriculum_Cd, subclasscd, score08, score09, count08, count09, avg08, avg09);

           	    if (retMap.containsKey(grade+hr_Class)) {
           	    	addclsMap = (Map)retMap.get(grade+hr_Class);
           	    } else {
           	    	addclsMap = new LinkedMap();
           	    	retMap.put(grade+hr_Class, addclsMap);
           	    }
                addclsMap.put(dtInfo.getSubclsCd(true), dtInfo);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }
    	return retMap;
    }

    private String getDownTotalInfoSql() {
    	final StringBuffer stb = new StringBuffer();
    	stb.append(" WITH SCORE_BASE08 AS ( ");
    	stb.append(" SELECT ");
    	stb.append("   T1.* ");
    	stb.append(" FROM ");
    	stb.append("   RECORD_AVERAGE_SDIV_DAT T1 ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + _param._year + "' ");
    	stb.append("   AND T1.SEMESTER = '9' ");
    	stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '990008' ");
    	stb.append("   AND T1.AVG_DIV = '2' ");
    	stb.append("   AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
    	stb.append("   AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '00000000' ");
    	stb.append("   AND T1.CLASSCD < '90' ");
    	stb.append("   AND T1.SUBCLASSCD NOT IN ('333333', '555555', '99999A', '99999B') ");
    	stb.append("   AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ( ");
    	stb.append("         SELECT ");
    	stb.append("           ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS IGNORE_SUBCLASS ");
    	stb.append("         FROM ");
    	stb.append("           SUBCLASS_REPLACE_DAT ");
    	stb.append("         WHERE ");
    	stb.append("           REPLACECD = '1' ");
    	stb.append("           AND YEAR = '" + _param._year + "' ");
    	stb.append("           AND ANNUAL = '" + _param._gradeCd + "' ");
    	stb.append("           AND ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD <> GRADING_CLASSCD || '-' || GRADING_SCHOOL_KIND || '-' || GRADING_CURRICULUM_CD || '-' || GRADING_SUBCLASSCD ");
    	stb.append("   ) ");
    	stb.append(" ), SCORE_BASE09 AS ( ");
    	stb.append(" SELECT ");
    	stb.append("   T1.* ");
    	stb.append(" FROM ");
    	stb.append("   RECORD_AVERAGE_SDIV_DAT T1 ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + _param._year + "' ");
    	stb.append("   AND T1.SEMESTER = '9' ");
    	stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '990009' ");
    	stb.append("   AND T1.AVG_DIV = '2' ");
    	stb.append("   AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
    	stb.append("   AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '00000000' ");
    	stb.append("   AND T1.CLASSCD < '90' ");
    	stb.append("   AND T1.SUBCLASSCD NOT IN ('333333', '555555', '99999A', '99999B') ");
    	stb.append("   AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ( ");
    	stb.append("         SELECT ");
    	stb.append("           ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS IGNORE_SUBCLASS ");
    	stb.append("         FROM ");
    	stb.append("           SUBCLASS_REPLACE_DAT ");
    	stb.append("         WHERE ");
    	stb.append("           REPLACECD = '1' ");
    	stb.append("           AND YEAR = '" + _param._year + "' ");
    	stb.append("           AND ANNUAL = '" + _param._gradeCd + "' ");
    	stb.append("           AND ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD <> GRADING_CLASSCD || '-' || GRADING_SCHOOL_KIND || '-' || GRADING_CURRICULUM_CD || '-' || GRADING_SUBCLASSCD ");
    	stb.append("   ) ");
    	stb.append(" ), SCORE_BASE AS ( ");
    	stb.append(" SELECT ");
    	stb.append("     CASE WHEN T2.GRADE IS NOT NULL THEN T2.GRADE ELSE T1.GRADE END AS GRADE, ");
    	stb.append("     CASE WHEN T2.HR_CLASS IS NOT NULL THEN T2.HR_CLASS ELSE T1.HR_CLASS END AS HR_CLASS, ");
    	stb.append("     CASE WHEN T2.CLASSCD IS NOT NULL THEN T2.CLASSCD ELSE T1.CLASSCD END AS CLASSCD, ");
    	stb.append("     CASE WHEN T2.SCHOOL_KIND IS NOT NULL THEN T2.SCHOOL_KIND ELSE T1.SCHOOL_KIND END AS SCHOOL_KIND, ");
    	stb.append("     CASE WHEN T2.CURRICULUM_CD IS NOT NULL THEN T2.CURRICULUM_CD ELSE T1.CURRICULUM_CD END AS CURRICULUM_CD, ");
    	stb.append("     CASE WHEN T2.SUBCLASSCD IS NOT NULL THEN T2.SUBCLASSCD ELSE T1.SUBCLASSCD END AS SUBCLASSCD, ");
    	stb.append("     T1.SCORE AS SCORE08, ");
    	stb.append("     T2.SCORE AS SCORE09, ");
    	stb.append("     T1.COUNT AS COUNT08, ");
    	stb.append("     T2.COUNT AS COUNT09, ");
    	stb.append("     DECIMAL(INT(T1.AVG*10.0+0.5)/10.0, 4, 1) AS AVG08, ");
    	stb.append("     DECIMAL(INT(T2.AVG*10.0+0.5)/10.0, 4, 1) AS AVG09 ");
    	stb.append(" FROM ");
    	stb.append("     SCORE_BASE08 T1 ");
    	stb.append("     FULL OUTER JOIN SCORE_BASE09 T2 ");
    	stb.append("       ON T2.YEAR = T1.YEAR ");
    	stb.append("      AND T2.SEMESTER = T1.SEMESTER ");
    	stb.append("      AND T2.TESTKINDCD = T1.TESTKINDCD ");
    	stb.append("      AND T2.TESTITEMCD = T1.TESTITEMCD ");
    	stb.append("      AND T2.SCORE_DIV = '09' ");
    	stb.append("      AND T2.AVG_DIV = T1.AVG_DIV  ");
    	stb.append("      AND T2.CLASSCD = T1.CLASSCD ");
    	stb.append("      AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
    	stb.append("      AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
    	stb.append("      AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
    	stb.append("      AND T2.GRADE = T1.GRADE ");
    	stb.append("      AND T2.HR_CLASS = T1.HR_CLASS ");
    	stb.append("      AND T2.COURSECD = T1.COURSECD ");
    	stb.append("      AND T2.MAJORCD = T1.MAJORCD ");
    	stb.append("      AND T2.COURSECODE = T1.COURSECODE ");
    	stb.append(" ) ");
    	stb.append(" SELECT ");
    	stb.append("   T1.* ");
    	stb.append(" FROM ");
    	stb.append("   SCORE_BASE T1 ");
    	stb.append("   LEFT JOIN CLASS_MST T2 ");
    	stb.append("     ON T2.CLASSCD = T1.CLASSCD ");
    	stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
    	stb.append("   LEFT JOIN SUBCLASS_MST T3 ");
    	stb.append("     ON T3.CLASSCD = T1.CLASSCD ");
    	stb.append("    AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
    	stb.append("    AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
    	stb.append("    AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
    	stb.append(" ORDER BY ");
    	stb.append("   T1.HR_CLASS, ");
    	stb.append("   T1.SUBCLASSCD ");
    	return stb.toString();
    }

    private class HrClsInfo {
        final String _grade;
        final String _hrClass;
        final String _gradeCd;
        final String _gradeName1;
        final String _hrName;
        final String _hrClassName1;
        final String _staffName;
        final String _course;
        final String _cousecodename;
        Map _dTotalMap;
        final List _students;
        HrClsInfo(
                final String grade,
                final String hrClass,
                final String gradeCd,
                final String gradeName1,
                final String hrName,
                final String hrClassName1,
                final String staffName,
                final String course,
                final String cousecodename
        		) {
            _grade = grade;
            _hrClass = hrClass;
            _gradeCd = gradeCd;
            _gradeName1 = gradeName1;
            _hrName = hrName;
            _hrClassName1 = hrClassName1;
            _staffName = staffName;
            _course = course;
            _cousecodename = cousecodename;
            _dTotalMap = new LinkedMap();
            _students = new ArrayList();
        }
    }
    /**
     * 生徒
     */
    private class Student {
        final String _schregno;
        String _name;
        final String _attendno;
        final String _gradeCd;
        final String _gradeName1;
        final String _hrName;
        final String _staffName;
        final String _grade;
        final String _hrClass;
        final String _coursecd;
        final String _majorcd;
        final String _course;
        final String _majorname;
        final String _cousecodename;
        final String _hrClassName1;

        final Map _attendMap;
        Map _attendSubclsMap;
        Map _scoreMap;

        Student(final String schregno, final String gradeCd, final String gradeName1, final String hrName, final String staffName, final String attendno, final String grade, final String hrClass, final String coursecd, final String majorcd, final String course, final String majorname, final String cousecodename, final String hrClassName1, final String entyear) {
            _schregno = schregno;
            _gradeCd = gradeCd;
            _gradeName1 = gradeName1;
            _hrName = hrName;
            _staffName = staffName;
            _attendno = attendno;
            _grade = grade;
            _hrClass = hrClass;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _course = course;
            _majorname = majorname;
            _cousecodename = cousecodename;
            _hrClassName1 = hrClassName1;
            _attendMap = new TreeMap();
            _scoreMap = new LinkedMap();
            _attendSubclsMap = new LinkedMap();
        }

    }

    private class ScoreInfo {
        final String _schregno;
        final String _classcd;
        final String _school_Kind;
        final String _curriculum_Cd;
        final String _subclasscd;
        final String _score08;
        final String _score09;
        final String _avg08;
        final String _avg09;
        final String _class_Rank09;
        final String _course_Rank09;
        public ScoreInfo (final String schregno, final String classcd, final String school_Kind, final String curriculum_Cd, final String subclasscd, final String score08, final String score09, final String avg08, final String avg09, final String class_Rank09, final String course_Rank09)
        {
            _schregno = schregno;
            _classcd = classcd;
            _school_Kind = school_Kind;
            _curriculum_Cd = curriculum_Cd;
            _subclasscd = subclasscd;
            _score08 = score08;
            _score09 = score09;
            _avg08 = avg08;
            _avg09 = avg09;
            _class_Rank09 = class_Rank09;
            _course_Rank09 = course_Rank09;
        }
    }

    private class SubclsInfo {
   	    final String _classCd;
   	    final String _schoolKind;
   	    final String _curriculumCd;
   	    final String _subclassCd;
   	    final String _className;
   	    final String _subclassName;
   	    SubclsInfo(
   	    	    final String classCd,
   	    	    final String schoolKind,
   	    	    final String curriculumCd,
   	    	    final String subclassCd,
   	    	    final String className,
   	    	    final String subclassName
            ) {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _className = className;
            _subclassName = subclassName;
        }
   	    private String getSubclsCd(final boolean zeroPlsFlg) {
   	    	final String wkStr = (_curriculumCd == null || "".equals(_curriculumCd)) ? "" : ((!zeroPlsFlg || _curriculumCd.length() > 1) ? _curriculumCd : "0" + _curriculumCd);
   	    	return _classCd + "-" + _schoolKind + "-" + wkStr + "-" + _subclassCd;
   	    }
    }

    private class DownTotalInfo {
        final String _grade;
        final String _hr_Class;
        final String _classcd;
        final String _school_Kind;
        final String _curriculum_Cd;
        final String _subclasscd;
        final String _score08;
        final String _score09;
        final String _count08;
        final String _count09;
        final String _avg08;
        final String _avg09;
        public DownTotalInfo (final String grade, final String hr_Class, final String classcd, final String school_Kind, final String curriculum_Cd, final String subclasscd, final String score08, final String score09, final String count08, final String count09, final String avg08, final String avg09)
        {
            _grade = grade;
            _hr_Class = hr_Class;
            _classcd = classcd;
            _school_Kind = school_Kind;
            _curriculum_Cd = curriculum_Cd;
            _subclasscd = subclasscd;
            _score08 = score08;
            _score09 = score09;
            _count08 = count08;
            _count09 = count09;
            _avg08 = avg08;
            _avg09 = avg09;
        }
   	    private String getSubclsCd(final boolean zeroPlsFlg) {
   	    	final String wkStr = (_curriculum_Cd == null || "".equals(_curriculum_Cd)) ? "" : ((!zeroPlsFlg || _curriculum_Cd.length() > 1) ? _curriculum_Cd : "0" + _curriculum_Cd);
   	    	return _classcd + "-" + _school_Kind + "-" + wkStr + "-" + _subclasscd;
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
        final int _kekkaJisu;
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
                final int kekkaJisu,
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
            _kekkaJisu = kekkaJisu;
            _transDays = transDays;
        }
    }

    private void loadAttend(
            final DB2UDB db2,
            final Student student
    ) {
        PreparedStatement ps = null;
        try {
            _param._attendParamMap.put("schregno", "?");

            final String sql = AttendAccumulate.getAttendSemesSql(
                    _param._year,
                    _param._semester,
                    _param._sdate,
                    _param._date,
                    _param._attendParamMap
            );
            ps = db2.prepareStatement(sql);

            final Integer zero = new Integer(0);

            for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
           	final Map row = (Map) rit.next();

            final Attendance attendance = new Attendance(
                    KnjDbUtils.getString(row, "SEMESTER"),
                    KnjDbUtils.getInt(row, "LESSON", zero).intValue(),     // 授業日数
                    KnjDbUtils.getInt(row, "MLESSON", zero).intValue(),    // 出席すべき日数
                    KnjDbUtils.getInt(row, "SUSPEND", zero).intValue(),    // 停止数
                    KnjDbUtils.getInt(row, "MOURNING", zero).intValue(),   // 忌引数
                    KnjDbUtils.getInt(row, "SICK", zero).intValue(),       // 欠席日数(※別項目の出席日数は、 出席日数 = 出席すべき日数 - 欠席日数)
                    KnjDbUtils.getInt(row, "SICK_ONLY", zero).intValue(),
                    KnjDbUtils.getInt(row, "NOTICE_ONLY", zero).intValue(),
                    KnjDbUtils.getInt(row, "NONOTICE_ONLY", zero).intValue(),
                    KnjDbUtils.getInt(row, "PRESENT", zero).intValue(),
                    KnjDbUtils.getInt(row, "LATE", zero).intValue(),       // 遅刻数
                    KnjDbUtils.getInt(row, "EARLY", zero).intValue(),      // 早退数
                    KnjDbUtils.getInt(row, "KEKKA_JISU", zero).intValue(), // 欠課時数
                    KnjDbUtils.getInt(row, "TRANSFER_DATE", zero).intValue()
                );
                student._attendMap.put(KnjDbUtils.getString(row, "SEMESTER"), attendance);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }
    }

    private Map loadSubclassAttend(
            final DB2UDB db2
    ) {
    	Map retMap = new TreeMap();
    	Map subMap = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String SSEMESTER = "1";
        _param._attendSubclsParamMap.put("grade", _param._grade);
        _param._attendSubclsParamMap.put("hrClass", "?");
        _param._attendSubclsParamMap.put("sSemester", SSEMESTER);
        String edate = _param._date;
        String sdate = (String)_param._sdate;
        if (sdate.compareTo(edate) < 0) {
            final String sqlAttendSubclass = AttendAccumulate.getAttendSubclassSql(
            		_param._year,
            		SEMEALL,
                    (String)sdate,
                    (String)edate,
                    _param._attendSubclsParamMap
                    );
            try {
                ps = db2.prepareStatement(sqlAttendSubclass);
            	for (int clsCnt = 0;clsCnt < _param._categorySelected.length;clsCnt++) {
            		final String cutStr = _param._categorySelected[clsCnt].length() > 3 ? _param._categorySelected[clsCnt].substring(_param._categorySelected[clsCnt].length() - 3, _param._categorySelected[clsCnt].length()) : _param._categorySelected[clsCnt];
            		ps.setString(1, cutStr);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                    	if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                    		continue;
                    	}
                	    final String schregno = rs.getString("SCHREGNO");
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final Attendance attendance = new Attendance(
                        		rs.getString("SEMESTER"),
                        		rs.getInt("LESSON"),
                        		rs.getInt("MLESSON"),
                        		rs.getInt("SUSPEND"),
                        		rs.getInt("MOURNING"),
                        		rs.getInt("SICK1"),
                        		0,
                        		0,
                        		0,
                        		0,
                        		rs.getInt("LATE"),
                        		rs.getInt("EARLY"),
                        		rs.getInt("SICK2"),
                        		0
                        );
                        if (retMap.containsKey(schregno)) {
                        	subMap = (Map)retMap.get(schregno);
                        } else {
                        	subMap = new LinkedMap();
                        	retMap.put(schregno, subMap);
                        }
                        subMap.put(subclasscd, attendance);
                    }
                    if (clsCnt+1 < _param._categorySelected.length) {  //最終じゃなければ、psを再利用するので、rsのみ閉じる。最後はfinallyで。
                        DbUtils.closeQuietly(rs);
                    }
            	}
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        return retMap;
    }

    protected Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 73781 $ $Date: 2020-04-16 13:16:40 +0900 (木, 16 4 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    private class Param {
        final String _year;
        final String _semester;
        final String _ctrlSemester;

        final String _grade;
        final String _gradeCd;
        final String[] _categorySelected;
        final String _sdate;
        final String _date;

        final String _useCurriculumcd;

        final String _schoolKind;
        final String _schoolCd;

        private String _cerifSchoolName;

//        private boolean _isNoPrintMoto;
        final Map _attendParamMap;
        final Map _attendSubclsParamMap;
        final boolean _isOutputDebug;

        final List _d026List = new ArrayList();

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _sdate = KNJ_EditDate.H_Format_Haifun(request.getParameter("SDATE"));
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));

            _schoolCd = request.getParameter("SCHOOLCD");
            _schoolKind = StringUtils.defaultString(getSchregRegdGdat(db2, "SCHOOL_KIND"));

            _useCurriculumcd = request.getParameter("useCurriculumcd");

            setCertifSchoolDat(db2);

            loadNameMstD026(db2);
//            loadNameMstD016(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

            _attendSubclsParamMap = new HashMap();
            _attendSubclsParamMap.put("DB2UDB", db2);
            _attendSubclsParamMap.put("HttpServletRequest", request);
            _attendSubclsParamMap.put("useCurriculumcd", _useCurriculumcd);

            _isOutputDebug = "1".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD616F' AND NAME = 'outputDebug' ")));
            _gradeCd = getSchregRegdGdat(db2, "GRADE_CD");

        }

        private String getSchregRegdGdat(final DB2UDB db2, final String field) {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  ");
            stb.append("        " + field + " ");
            stb.append("FROM    SCHREG_REGD_GDAT T1 ");
            stb.append("WHERE   T1.YEAR = '" + _year + "' ");
            stb.append(    "AND T1.GRADE = '" + _grade + "' ");
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
        }

//        private void loadNameMstD016(final DB2UDB db2) {
//            final String sql = "SELECT NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
//            final String namespare1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
//            _isNoPrintMoto = "Y".equals(namespare1);
//            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
//        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" select SCHOOLNAME1 FROM SCHOOL_MST ");
            sql.append(" WHERE YEAR = '" + _year + "' AND SCHOOL_KIND = '" + _schoolKind + "' AND SCHOOLCD = '" + _schoolCd + "' ");
            log.debug("certif_school_dat sql = " + sql.toString());

        	final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));
            _cerifSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOLNAME1"));
        }

        private void loadNameMstD026(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            final String field;
            if ("1".equals(_semester)) {
                field = "ABBV1";
            } else if ("2".equals(_semester)) {
                field = "ABBV2";
            } else {
                field = "ABBV3";
            }
            sql.append(" SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D026' AND " + field + " = '1' OR NAMESPARE1 = '1' ");

            _d026List.clear();
            _d026List.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "NAME1"));
        }
    }
}
