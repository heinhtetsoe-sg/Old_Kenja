/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 2024d752b3315995be804c654b34ac5db4ffe7b9 $
 *
 * 作成日: 2018/07/19
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD187B {

    private static final Log log = LogFactory.getLog(KNJD187B.class);

    /** 3教科科目コード */
    private static final String ALL3 = "333333";
    /** 5教科科目コード */
    private static final String ALL5 = "555555";
    /** 7教科科目コード */
    private static final String ALL7 = "777777";
    /** 9教科科目コード */
    private static final String ALL9 = "999999";
    /** 特殊科目コードA */
    private static final String ALL9A = "99999A";
    /** 特殊科目コードB */
    private static final String ALL9B = "99999B";

    private static final String SEMEALL = "9";

    private static final String SUBCLSCD_HR = "920300";  //HR
    private static final String SUBCLSCD_SCHOOLEVENT = "920100";  //学校行事
    private static final String SUBCLSCD_SPACT = "920200";

    private static final int MAXCOL = 26;
    private static final String FIXED_TESTCD = "9900";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        svf.VrSetForm("KNJD187B.frm", 1);
        final Map studentMap = getGHMapInfo(db2);
        for (Iterator ite = studentMap.keySet().iterator(); ite.hasNext();) {
        	final String getKey = (String)ite.next();
        	final GradeHrCls hrClSObj = (GradeHrCls)studentMap.get(getKey);
        	for (Iterator its = hrClSObj._schregMap.keySet().iterator();its.hasNext();) {
            	final String getSubKey = (String)its.next();
            	final Student student = (Student)hrClSObj._schregMap.get(getSubKey);
    			Map subSubclsAttMap = (Map)student._attendSubclsDatMap.get(_param._semester);

            	setExceptSpread(db2, svf, hrClSObj, student);
            	//表1 成績(レコードフォーマットへ変わる事前提で処理を配置)
            	final String scKey = "99-" + hrClSObj._schoolKind + "-99-" + ALL9;
    	    	int putCol = 1;
    	    	int pageCnt = 1;
            	for (Iterator itr = student._subclsInfoMap.keySet().iterator();itr.hasNext();) {
            		final String key1 = (String)itr.next();  //classcd - schkind
            		if (key1.startsWith("9")) continue;  //'9X'系は出力しない
            		final Map subMap = (Map)student._subclsInfoMap.get(key1);
            		for (Iterator itd = subMap.keySet().iterator();itd.hasNext();) {
            			final String key2 = (String)itd.next();  //subclassのFULLセットのキー
            			final SubclsInfo si = (SubclsInfo)subMap.get(key2);
                        if (putCol > MAXCOL) {
                            svf.VrEndPage();
                        	setExceptSpread(db2, svf, hrClSObj, student);
                        	putCol = 1;
                        	pageCnt++;
                        }
            			//教科名
            			svf.VrsOutn("CLASS_NAME", putCol, si._classAbbv);
            			svf.VrsOutn("SUBCLASS_NAME", putCol, si._subclassName);
            			svf.VrsOutn("CREDIT", putCol, si._credits);
            			final String sKey1 = _param._semester + FIXED_TESTCD + "08" + "-" + key2;
            			if (student._subclassRankMap.containsKey(sKey1)) {
            			    SubclassRank prtwk1 = (SubclassRank)student._subclassRankMap.get(sKey1);
            			    svf.VrsOutn("SCORE1", putCol, String.valueOf(prtwk1._score));  //成績

                        	//学年平均(コース平均を出力)
                        	final String gAvg = (!"".equals(StringUtils.defaultString(prtwk1._gAvg, ""))) ? prtwk1._gAvg : "0";
                        	svf.VrsOutn("GRADE_AVE", putCol, gAvg);

                        	//ヒストグラム
                        	if (pageCnt == 1) {
                        		//科目別コース別のヒストグラムのため、科目コード(混合)＋教育課程(混合)でキーを指定
                        		final String key3 = key2 + "-" + student._courseCd + "-" + student._majorCd + "-" + student._courseCode;
                        		if (_param._subclassBunpuMap.containsKey(key3)) {
                        	        final SubclassBunpu bunpu = (SubclassBunpu) _param._subclassBunpuMap.get(key3);
                        	        printRange(svf, bunpu._score9, 1, putCol);
                        	        printRange(svf, bunpu._score8, 2, putCol);
                        	        printRange(svf, bunpu._score7, 3, putCol);
                        	        printRange(svf, bunpu._score6, 4, putCol);
                        	        printRange(svf, bunpu._score5, 5, putCol);
                        	        printRange(svf, bunpu._score4, 6, putCol);
                        	        printRange(svf, bunpu._score3, 7, putCol);
                        	        printRange(svf, bunpu._score2, 8, putCol);
                        	        printRange(svf, bunpu._score1, 9, putCol);
                        	        printRange(svf, bunpu._score0, 10, putCol);
                        	        printRange(svf, bunpu.getTotal(), 11, putCol);
                        		}
                        	}
                        }
            			final String sKey2 = _param._semester + FIXED_TESTCD + "09" + "-" + key2;
            			if (student._subclassRankMap.containsKey(sKey2)) {
            			    SubclassRank prtwk2 = (SubclassRank)student._subclassRankMap.get(sKey2);
            			    svf.VrsOutn("SCORE2", putCol, String.valueOf(prtwk2._score));  //評定
            			}
            			if (subSubclsAttMap != null && subSubclsAttMap.containsKey(key2)) {
            			    SubclassAttendance prtSubclsAtt = (SubclassAttendance)subSubclsAttMap.get(key2);
            			    svf.VrsOutn("KEKKA", putCol, String.valueOf(prtSubclsAtt._sick));
            			    svf.VrsOutn("LATE", putCol, String.valueOf(prtSubclsAtt._late.add(prtSubclsAtt._early)));
            			    final String prtttlWk = String.valueOf(prtSubclsAtt._sick) + "-" + String.valueOf(prtSubclsAtt._late.add(prtSubclsAtt._early)) + "/" +String.valueOf(prtSubclsAtt._lesson);
            			    svf.VrsOutn("ATTEND_TOTAL", putCol, prtttlWk);
            			}
                		putCol++;
            		}
            	}

            	//上記では総計が出力されない(CHAIR_STD_DAT由来の科目コードには"99-校種-99-999999"は無い)ので、ここで出力
				//平均
				////成績
    			final String sKey1 = _param._semester + FIXED_TESTCD + "08" + "-" + scKey;
    			if (student._subclassRankMap.containsKey(sKey1)) {
    			    SubclassRank prtwk1 = (SubclassRank)student._subclassRankMap.get(sKey1);
    			    if (prtwk1 != null) {
    			        svf.VrsOut("TOTAL1", String.valueOf(prtwk1._score));  //合計
    			        svf.VrsOut("AVE1", String.valueOf(prtwk1._avg));  //平均
    			    }
        	    }
    			////5段階
    			final String sKey2 = _param._semester + FIXED_TESTCD + "09" + "-" + scKey;
    			if (student._subclassRankMap.containsKey(sKey2)) {
    			    SubclassRank prtwk2 = (SubclassRank)student._subclassRankMap.get(sKey2);
    			    if (prtwk2 != null) {
    			        svf.VrsOut("TOTAL2", String.valueOf(prtwk2._score));  //評定
    			        svf.VrsOut("AVE2", String.valueOf(prtwk2._avg));  //評定
    			    }
    			}

                svf.VrEndPage();
                _hasData = true;
        	}

        }
    }

    private void setExceptSpread(final DB2UDB db2, final Vrw32alp svf, final GradeHrCls hrClSObj, final Student student) {
    	//タイトル
        svf.VrsOut("SCHOOL_NAME1", (String)_param._certifInfo.get("SCHOOL_NAME"));

	    //校長名
	    final int pLen = KNJ_EditEdit.getMS932ByteLength((String)_param._certifInfo.get("PRINCIPAL_NAME"));
	    final String pField = pLen > 30 ? "3" : pLen > 20 ? "2" : "1";
	    svf.VrsOut("PRINCIPAL_NAME" + pField, (String)_param._certifInfo.get("PRINCIPAL_NAME"));

    	//担任名
		final int trLen = KNJ_EditEdit.getMS932ByteLength(hrClSObj._staffName);
		final String trField = trLen > 30 ? "3" : trLen > 20 ? "2" : "1";
		svf.VrsOut("TR_NAME"+trField, hrClSObj._staffName);

        //年度・学年・学期
		final String nendo = _param._year + "年度";
    	svf.VrsOut("NENDO", nendo);
		svf.VrsOut("GRADE1",  hrClSObj._gradeName2);
        svf.VrsOut("SEMESTER", _param._semesterName);

        //校種・年組番
		svf.VrsOut("SCHOOL_KIND", hrClSObj._skName);

    	//年組番
		svf.VrsOut("HR_NAME", student._hrName + " " + (student._attendNo.length() < 2 ? " " : "") +Integer.parseInt(student._attendNo) + "番");
		//氏名
		svf.VrsOut("NAME", student._name);

		//TOEFL_ITP(タイトル)
		svf.VrsOutn("REMARK2", 1, "TOEFL_ITP");

		//TOEFL_ITP
		svf.VrsOutn("REMARK2", 2, StringUtils.defaultString(student._toeflItpScore, ""));

    	for (Iterator itts = student._totalStudyLst.iterator();itts.hasNext();) {
    		final TotalStudyInfo prtWk = (TotalStudyInfo)itts.next();
            	//評価
        		if (!"".equals(prtWk._totalStudy_remark)) {
        	        final String[] sarwk = KNJ_EditEdit.get_token(prtWk._totalStudy_remark, 60, 2);
        		    svf.VrsOutn("TOTAL_ACT2", 1, sarwk[0]);
        		    if (sarwk.length > 1) {
        		        svf.VrsOutn("TOTAL_ACT2", 2, sarwk[1]);
        		    }
        		}
    	}

    	//出欠情報
    	AttendSemesDat att = (AttendSemesDat)student._attendSemesDatMap.get(_param._semester);

    	//授業日数
    	////欠席数
		svf.VrsOutn("LESSON", 1, String.valueOf(att._sick));
    	////総数
		svf.VrsOutn("LESSON", 2, String.valueOf(att._lesson));
   	    //学校行事
		if (student._attendSyukketuDatMap.containsKey(SUBCLSCD_SCHOOLEVENT)) {
			SubclassAttendance prtattwk = (SubclassAttendance)student._attendSyukketuDatMap.get(SUBCLSCD_SCHOOLEVENT);
   	        ////欠席数
	        svf.VrsOutn("EVENT", 1, prtattwk._sick == null ? "0" : String.valueOf(prtattwk._sick));
   	        ////総数
	        svf.VrsOutn("EVENT", 2, prtattwk._lesson == null ? "0" : String.valueOf(prtattwk._lesson));
		} else {
	        svf.VrsOutn("EVENT", 1, "0");
	        svf.VrsOutn("EVENT", 2, "0");
		}
   	    //特別活動
		if (student._attendSyukketuDatMap.containsKey(SUBCLSCD_SPACT)) {
			SubclassAttendance prtattwk = (SubclassAttendance)student._attendSyukketuDatMap.get(SUBCLSCD_SPACT);
   	        ////欠席数
	        svf.VrsOutn("SP_ACT", 1, prtattwk._sick == null ? "0" : String.valueOf(prtattwk._sick));
   	        ////総数
	        svf.VrsOutn("SP_ACT", 2, prtattwk._lesson == null ? "0" : String.valueOf(prtattwk._lesson));
		} else {
	        svf.VrsOutn("SP_ACT", 1, "0");
	        svf.VrsOutn("SP_ACT", 2, "0");
		}
	    //HR
		if (student._attendSyukketuDatMap.containsKey(SUBCLSCD_HR)) {
			SubclassAttendance prtattwk = (SubclassAttendance)student._attendSyukketuDatMap.get(SUBCLSCD_HR);
   	        ////欠席数
	        svf.VrsOutn("HR", 1, prtattwk._sick == null ? "0" : String.valueOf(prtattwk._sick));
   	        ////総数
	        svf.VrsOutn("HR", 2, prtattwk._lesson == null ? "0" : String.valueOf(prtattwk._lesson));
		} else {
	        svf.VrsOutn("HR", 1, "0");
	        svf.VrsOutn("HR", 2, "0");
		}
		svf.VrsOut("SUSPEND2", String.valueOf(att._suspend + att._mourning));
		svf.VrsOut("LATE2", String.valueOf(att._late));
		svf.VrsOut("EARLY2", String.valueOf(att._early));
    }

    private void printRange(final Vrw32alp svf, final int score, final int setField, final int colCnt) {
        svf.VrsOutn("DIST" + setField, colCnt, String.valueOf(score));
    }

    private Map getGHMapInfo(final DB2UDB db2) throws SQLException {
    	Map retMap = new LinkedMap();
    	GradeHrCls ghCls = null;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append("    WITH TOEFL_BEST AS ( ");
            stb.append("      SELECT ");
            stb.append("        SCHREGNO ");
            stb.append("        , MAX(SCORE) AS TOEFL_SCORE");
            stb.append("      FROM ");
            stb.append("        AFT_TOTAL_STUDY_TOEFL_DAT ");
            stb.append("      WHERE ");
            stb.append("        TEST_DATE <= '" + _param._attSemesDate + "'");
            stb.append("        AND TEST_DATE >= '" + _param._toeflSdate + "' ");
            stb.append("      GROUP BY ");
            stb.append("        SCHREGNO ");
            stb.append("    ) ");

            stb.append(" SELECT DISTINCT ");
            stb.append("  REGD.SCHREGNO, ");
            stb.append("  REGD.GRADE, ");
            stb.append("  GDAT.SCHOOL_KIND, ");
            stb.append("  A023.ABBV1 AS SKNAME, ");
            stb.append("  GDAT.GRADE_CD, ");
            stb.append("  GDAT.GRADE_NAME1, ");
            stb.append("  GDAT.GRADE_NAME2, ");
            stb.append("  REGD.HR_CLASS, ");
            stb.append("  HDAT.HR_NAME, ");
            stb.append("  HDAT.HR_NAMEABBV, ");
            stb.append("  SM.STAFFNAME, ");
            stb.append("  REGD.ATTENDNO, ");
            stb.append("  BASE.NAME, ");
            stb.append("  REGD.COURSECD, ");
            stb.append("  REGD.MAJORCD, ");
            stb.append("  REGD.COURSECODE, ");
            stb.append("  MAJR.MAJORNAME, ");
            stb.append("  CCODE.COURSECODENAME, ");
            stb.append("  TSBDAT.TOEFL_SCORE ");
            stb.append(" FROM ");
            stb.append("    SCHREG_REGD_DAT REGD ");
            stb.append("    INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("    INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ");
            stb.append("          AND GDAT.GRADE = REGD.GRADE ");
            stb.append("    INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ");
            stb.append("          AND HDAT.SEMESTER = REGD.SEMESTER ");
            stb.append("          AND HDAT.GRADE = REGD.GRADE ");
            stb.append("          AND HDAT.HR_CLASS = REGD.HR_CLASS ");
            stb.append("    LEFT JOIN MAJOR_MST MAJR ON REGD.COURSECD = MAJR.COURSECD ");
            stb.append("         AND REGD.MAJORCD = MAJR.MAJORCD ");
            stb.append("    LEFT JOIN COURSECODE_MST CCODE ON REGD.COURSECODE = CCODE.COURSECODE ");
            stb.append("    LEFT JOIN SEMESTER_MST SEM_MST ON SEM_MST.SEMESTER = REGD.SEMESTER ");
            stb.append("    LEFT JOIN STAFF_MST SM ON SM.STAFFCD = HDAT.TR_CD1 ");
            stb.append("    LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' AND A023.NAME1 = GDAT.SCHOOL_KIND ");
            stb.append("    LEFT JOIN TOEFL_BEST TSBDAT ON TSBDAT.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("  REGD.YEAR = '" + _param._year + "' ");
            stb.append("  AND REGD.SEMESTER = '" + ("9".equals(_param._semester) ? _param._maxSemester : _param._semester) + "' ");
            if ("1".equals(_param._categoryIsClass)) {
                stb.append("  AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
            } else {
                stb.append("  AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
            }
            stb.append("  AND ( ");
            stb.append("       BASE.GRD_DATE IS NULL OR (");
            stb.append("         BASE.GRD_DATE IS NOT NULL AND ( ");
            stb.append("           (BASE.GRD_DIV IN('2','3') AND BASE.GRD_DATE > (CASE WHEN SEM_MST.EDATE < '" + _param._ctrlDate + "' THEN SEM_MST.EDATE ELSE '" + _param._ctrlDate + "' END) ");
            stb.append("           OR (BASE.ENT_DIV IN('4','5') AND BASE.ENT_DATE <= CASE WHEN SEM_MST.SDATE < '" + _param._ctrlDate + "' THEN SEM_MST.SDATE ELSE '" + _param._ctrlDate + "' END)) ");
            stb.append("         )");
            stb.append("       )");
            stb.append("  )");
            stb.append(" ORDER BY ");
            stb.append("    REGD.GRADE, ");
            stb.append("    REGD.HR_CLASS, ");
            stb.append("    REGD.ATTENDNO  ");

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String skName = rs.getString("SKNAME");
                final String gradeCd = rs.getString("GRADE_CD");
                final String gradeName = rs.getString("GRADE_NAME1");
                final String gradeName2 = rs.getString("GRADE_NAME2");
                final String hrclass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String hrAbbv = rs.getString("HR_NAMEABBV");
                final String staffName = rs.getString("STAFFNAME");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String coursecd = rs.getString("COURSECD");
                final String majorcd = rs.getString("MAJORCD");
                final String coursecode = rs.getString("COURSECODE");
                final String majorName = rs.getString("MAJORNAME");
                final String courseCodeName = rs.getString("COURSECODENAME");
                final String toeflScore = rs.getString("TOEFL_SCORE");

                final Student student = new Student(
                        schregno,
                        grade,
                        hrclass,
                        hrName,
                        hrAbbv,
                        attendno,
                        name,
                        coursecd,
                        majorcd,
                        coursecode,
                        majorName,
                        courseCodeName,
                        toeflScore
                );
                student.setSubclassInfo(db2);
                student.setSubclassRank(db2);
                student.setTotalStudyAct(db2);
                AttendSemesDat.setAttendSemesDatList(db2, _param, student);
                SubclassAttendance.setAttendSubclsDatList(db2, _param, student);
                final String rmKey = grade + "-" + hrclass;
                if (retMap.containsKey(rmKey)) {
                	ghCls = (GradeHrCls)retMap.get(rmKey);
                } else {
                	ghCls = new GradeHrCls(grade, schoolKind, skName, gradeCd, gradeName, gradeName2, hrclass, hrName, hrAbbv, staffName, new LinkedMap());
                    retMap.put(rmKey, ghCls);
                }
                if (!ghCls._schregMap.containsKey(schregno)) {
                	ghCls._schregMap.put(schregno, student);
                }
            }
        } catch (final SQLException e) {
            log.error("生徒の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private class GradeHrCls {
    	final String _grade;
    	final String _schoolKind;
    	final String _skName;
    	final String _gradeCd;
    	final String _gradeName;
    	final String _gradeName2;
    	final String _hrClass;
    	final String _hrName;
    	final String _hrAbbv;
    	final String _staffName;
    	final Map _schregMap;
    	GradeHrCls(
            	final String grade,
            	final String schoolKind,
            	final String skName,
            	final String gradeCd,
            	final String gradeName,
            	final String gradeName2,
            	final String hrClass,
            	final String hrName,
            	final String hrAbbv,
            	final String staffName,
            	final Map schregMap
    			) {
        	_grade = grade;
        	_schoolKind = schoolKind;
        	_skName = skName;
        	_gradeCd = gradeCd;
        	_gradeName = gradeName;
        	_gradeName2 = gradeName2;
        	_hrClass = hrClass;
        	_hrName = hrName;
        	_hrAbbv = hrAbbv;
        	_staffName = staffName;
        	_schregMap = schregMap;
    	}
    }

    private class TotalStudyInfo {
        final String _totalStudy_subclsName;
        final String _totalStudy_remark;
        TotalStudyInfo(final String totalStudy_subclsName, final String totalStudy_remark) {
        	_totalStudy_subclsName = totalStudy_subclsName;
        	_totalStudy_remark = totalStudy_remark;

        }
    }

    private class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrAbbv;
        final String _attendNo;
        final String _name;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _majorName;
        final String _courseCodeName;
        final Map _subclassRankMap;
        final Map _subclsInfoMap;
        final Map _attendSemesDatMap;
        final Map _attendSubclsDatMap;
        final Map _attendSyukketuDatMap;
        final String _toeflItpScore;
        private List _totalStudyLst;

        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrAbbv,
                final String attendNo,
                final String name,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String majorName,
                final String courseCodeName,
                final String toeflItpScore
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrAbbv = hrAbbv;
            _attendNo = attendNo;
            _name = name;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _majorName = majorName;
            _courseCodeName = courseCodeName;
            _subclassRankMap = new LinkedMap();
            _subclsInfoMap = new LinkedMap();
            _attendSemesDatMap = new LinkedMap();
            _attendSubclsDatMap = new LinkedMap();
            _attendSyukketuDatMap = new LinkedMap();
            _toeflItpScore = toeflItpScore;
            _totalStudyLst = new ArrayList();
        }

        public void setSubclassInfo(final DB2UDB db2) throws SQLException {
        	final String useSemester = "9".equals(_param._semester) ? _param._maxSemester : _param._semester;
        	PreparedStatement ps = null;
            ResultSet rs = null;
            final String pskey = "setSubclassInfo";
            if (!_param._psBuffer.containsKey(pskey)) {
            	final StringBuffer stb = new StringBuffer();
            	stb.append(" WITH REPL AS ( ");
            	stb.append("   SELECT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD, CAST(NULL AS VARCHAR(1)) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _param._year + "' GROUP BY COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ");
            	stb.append(" UNION ");
            	stb.append("   SELECT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _param._year + "' GROUP BY ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ");
            	stb.append(" ) ");
            	stb.append(" SELECT DISTINCT ");
            	stb.append("   T1.SCHREGNO ");
            	stb.append("   ,T3.CLASSCD ");
            	stb.append("   ,T3.SCHOOL_KIND ");
            	stb.append("   ,T3.CURRICULUM_CD ");
            	stb.append("   ,T3.SUBCLASSCD ");
            	stb.append("   ,L1.CLASSNAME ");
            	stb.append("   ,L1.CLASSABBV ");
            	stb.append("   ,CASE WHEN L2.SUBCLASSORDERNAME2 IS NOT NULL THEN L2.SUBCLASSORDERNAME2 ELSE L2.SUBCLASSNAME END AS SUBCLASSNAME ");
            	stb.append("   ,L2.SUBCLASSABBV ");
            	stb.append("   ,T4.CREDITS ");
            	stb.append("   ,CASE WHEN L3.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI ");
            	stb.append("   ,CASE WHEN L4.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO ");
            	stb.append(" FROM ");
            	stb.append("   SCHREG_REGD_DAT T1 ");
            	stb.append("   LEFT JOIN CHAIR_STD_DAT T2 ");
            	stb.append("     ON T2.YEAR = T1.YEAR ");
            	stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
            	stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            	stb.append("   LEFT JOIN CHAIR_DAT T3 ");
            	stb.append("     ON T3.YEAR = T2.YEAR ");
            	stb.append("    AND T3.SEMESTER = T2.SEMESTER ");
            	stb.append("    AND T3.CHAIRCD = T2.CHAIRCD ");
            	stb.append("   LEFT JOIN CLASS_MST L1 ");
            	stb.append("     ON L1.CLASSCD = T3.CLASSCD ");
            	stb.append("    AND L1.SCHOOL_KIND = T3.SCHOOL_KIND ");
            	stb.append("   LEFT JOIN SUBCLASS_MST L2 ");
            	stb.append("     ON L2.CLASSCD = T3.CLASSCD ");
            	stb.append("    AND L2.SCHOOL_KIND = T3.SCHOOL_KIND ");
            	stb.append("    AND L2.CURRICULUM_CD = T3.CURRICULUM_CD ");
            	stb.append("    AND L2.SUBCLASSCD = T3.SUBCLASSCD ");
            	stb.append("   LEFT JOIN CREDIT_MST T4 ");
            	stb.append("     ON T4.YEAR = T3.YEAR ");
            	stb.append("    AND T4.GRADE = T1.GRADE ");
            	stb.append("    AND T4.COURSECD = T1.COURSECD ");
            	stb.append("    AND T4.MAJORCD = T1.MAJORCD ");
            	stb.append("    AND T4.COURSECODE = T1.COURSECODE ");
            	stb.append("    AND T4.CLASSCD = T3.CLASSCD ");
            	stb.append("    AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ");
            	stb.append("    AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ");
            	stb.append("    AND T4.SUBCLASSCD = T3.SUBCLASSCD ");
            	stb.append("   LEFT JOIN REPL L3 ");
            	stb.append("     ON L3.DIV = '1' ");
            	stb.append("    AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = L3.SUBCLASSCD ");
            	stb.append("   LEFT JOIN REPL L4 ");
            	stb.append("     ON L4.DIV = '2' ");
            	stb.append("    AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = L4.SUBCLASSCD ");
            	stb.append(" WHERE ");
            	stb.append("   T1.YEAR = '" + _param._year + "' ");
            	stb.append("   AND T1.SEMESTER = '" + useSemester + "' ");
            	stb.append("   AND T1.GRADE = '" + _param._grade + "' ");
                stb.append("   AND T1.SCHREGNO = ? ");
            	stb.append("   AND T3.CLASSCD IS NOT NULL ");
            	//'9X'は除外するが、特殊教科コードの学校行事(93),特別活動(95),HR(92)は出欠で利用するために取得する。
            	stb.append("   AND (T3.CLASSCD < '90' OR T3.SUBCLASSCD IN ('" + SUBCLSCD_HR + "', '" + SUBCLSCD_SCHOOLEVENT + "', '" + SUBCLSCD_SPACT + "')) ");
                if ("Y".equals(_param._d016Namespare1)) {
                    stb.append("     AND NOT EXISTS ( ");
                    stb.append("         SELECT 'X' ");
                    stb.append("         FROM ");
                    stb.append("             SUBCLASS_REPLACE_COMBINED_DAT L1 ");
                    stb.append("         WHERE ");
                    stb.append("             L1.YEAR = T3.YEAR ");
                    stb.append("             AND L1.ATTEND_CLASSCD = T3.CLASSCD ");
                    stb.append("             AND L1.ATTEND_SCHOOL_KIND = T3.SCHOOL_KIND ");
                    stb.append("             AND L1.ATTEND_CURRICULUM_CD = T3.CURRICULUM_CD ");
                    stb.append("             AND L1.ATTEND_SUBCLASSCD = T3.SUBCLASSCD ");
                    stb.append("     ) ");
                }            	stb.append(" ORDER BY ");
            	stb.append("   T1.SCHREGNO ");
            	stb.append("   ,T3.CLASSCD ");
            	stb.append("   ,T3.SCHOOL_KIND ");
            	stb.append("   ,T3.CURRICULUM_CD ");
            	stb.append("   ,T3.SUBCLASSCD ");
                _param._psBuffer.put(pskey, stb.toString());
            }
        	final String sql = (String)_param._psBuffer.get(pskey);

            Map subMap = null;
            try {
                ps = db2.prepareStatement(sql);
                ps.setString(1, _schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                	final String classcd = rs.getString("CLASSCD");
                	final String school_Kind = rs.getString("SCHOOL_KIND");
                	final String curriculum_Cd = rs.getString("CURRICULUM_CD");
                	final String subclasscd = rs.getString("SUBCLASSCD");
                	final String classname = rs.getString("CLASSNAME");
                	final String classabbv = rs.getString("CLASSABBV");
                	final String subclassname = rs.getString("SUBCLASSNAME");
                	final String subclassabbv = rs.getString("SUBCLASSABBV");
                	final String credits = rs.getString("CREDITS");
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                	SubclsInfo addwk = new SubclsInfo(classcd, school_Kind, curriculum_Cd, subclasscd, classname, classabbv, subclassname, subclassabbv, credits, isSaki, isMoto);
                	final String mKey = classcd + "-" + school_Kind;
                	if (_subclsInfoMap.containsKey(mKey)) {
                		subMap = (Map)_subclsInfoMap.get(mKey);
                	} else {
                		subMap = new LinkedMap();
                		_subclsInfoMap.put(mKey, subMap);
                	}
                	final String subKey = classcd + "-" + school_Kind + "-" + curriculum_Cd  + "-" + subclasscd;
                	subMap.put(subKey, addwk);
                }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void setTotalStudyAct(final DB2UDB db2) throws SQLException {
            final StringBuffer stb = new StringBuffer();
        	final String useSemester = SEMEALL.equals(_param._semester) ? _param._maxSemester : _param._semester;
            final String fixedTestCd = "990009";
            final String fixedClsCd = "90";
            stb.append(" WITH FIXED_PHRASE AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("      T1.YEAR ");
            stb.append("      ,T1.SEMESTER ");
            stb.append("      ,T1.CLASSCD ");
            stb.append("      ,T1.SCHOOL_KIND ");
            stb.append("      ,T1.CURRICULUM_CD ");
            stb.append("      ,T1.SUBCLASSCD ");
            stb.append("      ,T1.SCHREGNO ");
            stb.append("      ,T1.SCORE AS REMARK1 ");
            stb.append("      ,T3.REMARK ");
            stb.append(" FROM ");
            stb.append("      SCHREG_REGD_DAT T0 ");
            stb.append("      LEFT JOIN RECORD_SCORE_DAT T1 ");
            stb.append("        ON T1.YEAR = T0.YEAR ");
            stb.append("       AND T1.SEMESTER = T0.SEMESTER ");
            stb.append("       AND T1.SCHREGNO = T0.SCHREGNO ");
            stb.append("       AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + fixedTestCd + "' ");  //固定 ");
            stb.append("       AND T1.CLASSCD = '" + fixedClsCd + "' ");  //固定 ");
            stb.append("      LEFT JOIN HTRAINREMARK_TEMP_SEMES_COURSE_DAT T2 ");
            stb.append("        ON T2.YEAR = T1.YEAR ");
            stb.append("       AND T2.SEMESTER = '" + SEMEALL + "' ");  //固定 ");
            stb.append("       AND T2.GRADE = '" + _param._grade + "' ");  //学年 ");
            stb.append("       AND T2.COURSECD || T2.MAJORCD || T2.COURSECODE = T0.COURSECD || T0.MAJORCD || T0.COURSECODE ");
            stb.append("       AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
            stb.append("            = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            stb.append("      LEFT JOIN HTRAINREMARK_TEMP_SEMES_DAT T3 ");
            stb.append("        ON T3.YEAR = T2.YEAR ");
            stb.append("       AND T3.SEMESTER = T2.SEMESTER ");
            stb.append("       AND T3.GRADE = T2.GRADE ");
            stb.append("       AND T3.DATA_DIV = T2.DATA_DIV ");
            stb.append("       AND T3.PATTERN_CD = T1.SCORE ");
            stb.append(" WHERE ");
            stb.append("     T0.YEAR = '" + _param._year + "' ");  //年度 ");
            stb.append("     AND T0.SEMESTER = '" + useSemester + "' ");  //"9"ならlastSemester ");
            stb.append("     AND T0.SCHREGNO = '" + _schregno + "' ");  //学籍NO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("    T3.SUBCLASSNAME ");
            stb.append("   ,T3.SUBCLASSABBV ");
            stb.append("   ,T2.REMARK1 ");
            stb.append("   ,T2.REMARK ");
            stb.append(" FROM ");
            stb.append("   RECORD_SCORE_DAT T1 ");
            stb.append("   LEFT JOIN FIXED_PHRASE T2 ");
            stb.append("     ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("    AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("    AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN SUBCLASS_MST T3 ");
            stb.append("     ON T3.CLASSCD = T1.CLASSCD ");
            stb.append("    AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("    AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _param._year + "' ");
            stb.append("   AND T1.SEMESTER = '" + useSemester + "' ");  //"9"ならlastSemester
            stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + fixedTestCd + "' ");  //固定
            stb.append("   AND T1.SCHREGNO = '" + _schregno + "' ");
            stb.append("   AND T1.CLASSCD = '" + fixedClsCd + "' ");  //固定 ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            //log.warn("sql : "+ stb.toString());
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                	final String totalStudy_subclsName = StringUtils.defaultString(rs.getString("SUBCLASSNAME"), "");
                	final String totalStudy_remark = StringUtils.defaultString(rs.getString("REMARK"), "");
                	TotalStudyInfo addwk = new TotalStudyInfo(totalStudy_subclsName, totalStudy_remark);
                	_totalStudyLst.add(addwk);
                }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public void setSubclassRank(final DB2UDB db2) throws SQLException {
        	final String useSemester = "9".equals(_param._semester) ? _param._maxSemester : _param._semester;
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String pskey = "setSubclassRank";

            try {
                if (!_param._psBuffer.containsKey(pskey)) {
                    final StringBuffer stb = new StringBuffer();
                    stb.append(" SELECT ");
                    stb.append("     REC_RANK.SEMESTER || REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD || REC_RANK.SCORE_DIV AS TESTCD, ");
                    stb.append("     REC_RANK.CLASSCD, ");
                    stb.append("     REC_RANK.SCHOOL_KIND, ");
                    stb.append("     REC_RANK.CURRICULUM_CD, ");
                    stb.append("     REC_RANK.SUBCLASSCD, ");
                    stb.append("     SUBM.SUBCLASSNAME, ");
                    stb.append("     REC_RANK.SCORE, ");
                    stb.append("     DECIMAL(ROUND(REC_RANK.AVG*10,0)/10,5,1) AS AVG, ");
                    stb.append("     REC_AVG.COUNT AS GCNT, ");
                    stb.append("     DECIMAL(ROUND(REC_AVG.AVG*10,0)/10,5,1) AS GAVG ");
                    stb.append(" FROM ");
                    stb.append("     RECORD_RANK_SDIV_DAT REC_RANK ");
                    stb.append("     INNER JOIN SUBCLASS_MST SUBM ON REC_RANK.CLASSCD = SUBM.CLASSCD ");
                    stb.append("      AND REC_RANK.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
                    stb.append("      AND REC_RANK.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
                    stb.append("      AND REC_RANK.SUBCLASSCD = SUBM.SUBCLASSCD ");
                    stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ");
                    stb.append("       ON REGD.YEAR = REC_RANK.YEAR ");
                    stb.append("      AND REGD.SEMESTER = '" + useSemester + "' ");
                    stb.append("      AND REGD.SCHREGNO = REC_RANK.SCHREGNO ");
                    stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT REC_AVG ON REC_RANK.YEAR = REC_AVG.YEAR ");
                    stb.append("      AND REC_RANK.SEMESTER = REC_AVG.SEMESTER ");
                    stb.append("      AND REC_RANK.TESTKINDCD = REC_AVG.TESTKINDCD ");
                    stb.append("      AND REC_RANK.TESTITEMCD = REC_AVG.TESTITEMCD ");
                    stb.append("      AND REC_RANK.SCORE_DIV = REC_AVG.SCORE_DIV ");
                    stb.append("      AND REC_RANK.CLASSCD = REC_AVG.CLASSCD ");
                    stb.append("      AND REC_RANK.SCHOOL_KIND = REC_AVG.SCHOOL_KIND ");
                    stb.append("      AND REC_RANK.CURRICULUM_CD = REC_AVG.CURRICULUM_CD ");
                    stb.append("      AND REC_RANK.SUBCLASSCD = REC_AVG.SUBCLASSCD ");
                    stb.append("      AND REC_AVG.AVG_DIV = '3' ");  //固定 3:コース
                    stb.append("      AND REC_AVG.GRADE = '" + _param._grade + "' ");
                    stb.append("      AND REC_AVG.HR_CLASS = '000' ");
                    stb.append("      AND REC_AVG.COURSECD = REGD.COURSECD ");
                    stb.append("      AND REC_AVG.MAJORCD = REGD.MAJORCD ");
                    stb.append("      AND REC_AVG.COURSECODE = REGD.COURSECODE ");
                    stb.append(" WHERE ");
                    stb.append("     REC_RANK.YEAR = '" + _param._year + "' ");
                    stb.append("     AND REC_RANK.SEMESTER = '" + _param._semester + "' ");
                    stb.append("     AND REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD = '" + FIXED_TESTCD + "' ");  //SCORE_DIVの指定は出力時に指定。
                    stb.append("     AND REC_RANK.SCHREGNO = ? ");
                    stb.append("     AND REC_RANK.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL7 + "', '" + ALL9A + "', '" + ALL9B + "') ");
                    stb.append(" UNION ");
                    stb.append(" SELECT ");
                    stb.append("     REC_RANK.SEMESTER || REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD || REC_RANK.SCORE_DIV AS TESTCD, ");
                    stb.append("     REC_RANK.CLASSCD, ");
                    stb.append("     REC_RANK.SCHOOL_KIND, ");
                    stb.append("     REC_RANK.CURRICULUM_CD, ");
                    stb.append("     REC_RANK.SUBCLASSCD, ");
                    stb.append("     '全科目' AS SUBCLASSNAME, ");
                    stb.append("     REC_RANK.SCORE, ");
                    stb.append("     DECIMAL(ROUND(REC_RANK.AVG*10,0)/10,5,1) AS AVG, ");
                    stb.append("     0 AS GCNT, ");
                    stb.append("     0 AS GAVG ");
                    stb.append(" FROM ");
                    stb.append("     RECORD_RANK_SDIV_DAT REC_RANK ");
                    stb.append(" WHERE ");
                    stb.append("     REC_RANK.YEAR = '" + _param._year + "' ");
//                    stb.append("     AND REC_RANK.SCORE_DIV = '01' ");  //SCORE_DIVの指定は出力時に指定。
                    stb.append("     AND REC_RANK.SCHREGNO = ? ");
                    stb.append("     AND REC_RANK.SUBCLASSCD = '" + ALL9 + "' ");
                    stb.append(" ORDER BY ");
                    stb.append("     TESTCD, ");
                    stb.append("     CLASSCD, ");
                    stb.append("     SCHOOL_KIND, ");
                    stb.append("     CURRICULUM_CD, ");
                    stb.append("     SUBCLASSCD ");
                    _param._psBuffer.put(pskey, stb.toString());
                }
            	final String sql = (String)_param._psBuffer.get(pskey);
                ps = db2.prepareStatement(sql);

                ps.setString(1, _schregno);
                ps.setString(2, _schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String testcd = rs.getString("TESTCD");
                    final String classcd = StringUtils.defaultString(rs.getString("CLASSCD"));
                    final String schoolKind = StringUtils.defaultString(rs.getString("SCHOOL_KIND"));
                    final String curriculumCd = StringUtils.defaultString(rs.getString("CURRICULUM_CD"));
                    final String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    final int score = rs.getInt("SCORE");
                    final String avg = StringUtils.defaultString(rs.getString("AVG"));
                    final String gCnt = StringUtils.defaultString(rs.getString("GCNT"));
                    final String gAvg = StringUtils.defaultString(rs.getString("GAVG"));

                    final SubclassRank subclassRank = new SubclassRank(
                            testcd,
                            classcd,
                            schoolKind,
                            curriculumCd,
                            subclasscd,
                            subclassname,
                            score,
                            avg,
                            gCnt,
                            gAvg
                    );
                    final String mKey = testcd + "-" + classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd;
                    _subclassRankMap.put(mKey, subclassRank);
                }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
    /**
     * 出欠の記録
     */
    private static class AttendSemesDat {

        final String _semester;
        int _lesson;
        int _suspend;
        int _mourning;
        int _mlesson;
        int _sick;
        int _absent;
        int _present;
        int _late;
        int _early;
        int _transferDate;
        int _offdays;
        int _kekkaJisu;
        int _virus;
        int _koudome;

        public AttendSemesDat(
                final String semester
        ) {
            _semester = semester;
        }

        public void add(
                final AttendSemesDat o
        ) {
            _lesson += o._lesson;
            _suspend += o._suspend;
            _mourning += o._mourning;
            _mlesson += o._mlesson;
            _sick += o._sick;
            _absent += o._absent;
            _present += o._present;
            _late += o._late;
            _early += o._early;
            _transferDate += o._transferDate;
            _offdays += o._offdays;
            _kekkaJisu += o._kekkaJisu;
            _virus += o._virus;
            _koudome += o._koudome;
        }

        private static void setAttendSemesDatList(final DB2UDB db2, final Param param, final Student student) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String psKey = "attendPs";
            try {
            	final String sql;
            	if (!param._psBuffer.containsKey(psKey)) {
            		param._attendParamMap.put("schregno", "?");
                    sql = AttendAccumulate.getAttendSemesSql(
                            param._year,
                            param._semester,
                            null,
                            param._attSemesDate,
                            param._attendParamMap
                    );
                    param._psBuffer.put(psKey, sql);
            	} else {
            		sql = (String)param._psBuffer.get(psKey);
            	}
                ps = db2.prepareStatement(sql);

                ps.setString(1, student._schregno);
                rs = ps.executeQuery();

                while (rs.next()) {

                    final String semester = rs.getString("SEMESTER");
                    final int lesson = rs.getInt("LESSON");
                    final int suspend = rs.getInt("SUSPEND");
                    final int mourning = rs.getInt("MOURNING");
                    final int mlesson = rs.getInt("MLESSON");
                    final int sick = rs.getInt("SICK");
                    final int absent = rs.getInt("ABSENT");
                    final int present = rs.getInt("PRESENT");
                    final int late = rs.getInt("LATE");
                    final int early = rs.getInt("EARLY");
                    final int transferDate = rs.getInt("TRANSFER_DATE");
                    final int offdays = rs.getInt("OFFDAYS");
                    final int kekkaJisu = rs.getInt("KEKKA_JISU");
                    final int virus = rs.getInt("VIRUS");
                    final int koudome = rs.getInt("KOUDOME");

                    final AttendSemesDat attendSemesDat = new AttendSemesDat(semester);
                    attendSemesDat._lesson = lesson;
                    attendSemesDat._suspend = suspend;
                    attendSemesDat._mourning = mourning;
                    attendSemesDat._mlesson = mlesson;
                    attendSemesDat._sick = sick;
                    attendSemesDat._absent = absent;
                    attendSemesDat._present = present;
                    attendSemesDat._late = late;
                    attendSemesDat._early = early;
                    attendSemesDat._transferDate = transferDate;
                    attendSemesDat._offdays = offdays;
                    attendSemesDat._kekkaJisu = kekkaJisu;
                    attendSemesDat._virus = virus;
                    attendSemesDat._koudome = koudome;

                    student._attendSemesDatMap.put(semester, attendSemesDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

    }

    private class SubclassRank {
        final String _testcd;
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final int _score;
        final String _avg;
        final String _gCnt;
        final String _gAvg;

        public SubclassRank(
                final String testcd,
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String subclassname,
                final int score,
                final String avg,
                final String gCnt,
                final String gAvg
        ) {
            _testcd = testcd;
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _score = score;
            _avg = avg;
            _gCnt = gCnt;
            _gAvg = gAvg;
        }

        public String getKey() {
            return _classcd + _schoolKind + _curriculumCd + _subclasscd;
        }
    }

    private class SubclassBunpu {
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final int _score0;
        final int _score1;
        final int _score2;
        final int _score3;
        final int _score4;
        final int _score5;
        final int _score6;
        final int _score7;
        final int _score8;
        final int _score9;

        public SubclassBunpu(
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String subclassname,
                final int score0,
                final int score1,
                final int score2,
                final int score3,
                final int score4,
                final int score5,
                final int score6,
                final int score7,
                final int score8,
                final int score9
        ) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _score0 = score0;
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;
            _score4 = score4;
            _score5 = score5;
            _score6 = score6;
            _score7 = score7;
            _score8 = score8;
            _score9 = score9;
        }
        private int getTotal() {
        	return _score0 + _score1 + _score2 + _score3 + _score4 + _score5 + _score6 + _score7 + _score8 + _score9;
        }
    }

    private class SubclsInfo {
        final String _classCd;
        final String _school_Kind;
        final String _curriculum_Cd;
        final String _subclassCd;
        final String _className;
        final String _classAbbv;
        final String _subclassName;
        final String _subclassAbbv;
        final String _credits;
        final boolean _isSaki;
        final boolean _isMoto;
        public SubclsInfo (final String classCd, final String school_Kind, final String curriculum_Cd, final String subclassCd, final String className, final String classAbbv
        		            , final String subclassName, final String subclassAbbv, final String credits, final boolean isSaki, final boolean isMoto)
        {
            _classCd = classCd;
            _school_Kind = school_Kind;
            _curriculum_Cd = curriculum_Cd;
            _subclassCd = subclassCd;
            _className = className;
            _classAbbv = classAbbv;
            _subclassName = subclassName;
            _subclassAbbv = subclassAbbv;
            _credits = credits;
            _isSaki = isSaki;
            _isMoto = isMoto;
        }
    }

    private class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
        }
    }

    private class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static class SubclassAttendance {
        BigDecimal _lesson;
        BigDecimal _attend;
        BigDecimal _sick;
        BigDecimal _late;
        BigDecimal _early;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal attend, final BigDecimal sick, final BigDecimal late, final BigDecimal early) {
            _lesson = lesson;
            _attend = attend;
            _sick = sick;
            _late = late;
            _early = early;
        }

        public void add(final SubclassAttendance addobj) {
            _lesson = _lesson.add(addobj._lesson);
            _attend = _attend.add(addobj._attend);
            _sick = _attend.add(addobj._sick);
            _late = _attend.add(addobj._late);
            _early = _attend.add(addobj._early);
        }
        public String toString() {
            return "SubclassAttendance(" + _sick == null ? null : sishaGonyu(_sick.toString())  + "/" + _lesson + ")";
        }

        private static void setAttendSubclsDatList(final DB2UDB db2, final Param param, final Student student) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List chkClsCdArry = new ArrayList();
            chkClsCdArry.add(SUBCLSCD_HR); //HR
            chkClsCdArry.add(SUBCLSCD_SCHOOLEVENT); //学校行事
            chkClsCdArry.add(SUBCLSCD_SPACT); //特別活動
            Semester sems = (Semester)param._semesterMap.get(param._semester);
            final String edate = sems._dateRange._edate.compareTo(param._attSemesDate) > 0 ? param._attSemesDate : sems._dateRange._edate;
            final String psKey = "attendSc";
            try {
            	if (!param._psBuffer.containsKey(psKey)) {
            		param._attendSubParamMap.put("schregno", "?");
                    final String setsql = AttendAccumulate.getAttendSubclassSql(
                            param._year,
                            SEMEALL,
                            sems._dateRange._sdate,
                            edate,
                            param._attendSubParamMap
                    );
                    param._psBuffer.put(psKey, setsql);
            	}
          		final String sql = (String)param._psBuffer.get(psKey);
                ps = db2.prepareStatement(sql);

                ps.setString(1, student._schregno);
                rs = ps.executeQuery();

                while (rs.next()) {

                    final String subclasscd = rs.getString("SUBCLASSCD");
                    if (subclasscd == null || "".equals(subclasscd)) {
                        continue;
                    }
                    final String[] subclsCut = StringUtils.split(subclasscd, '-');
                    if (subclsCut.length < 4) {
                    	continue;
                    }
                	if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                		continue;
                	}
                    final String key1 = subclsCut[0] + "-" + subclsCut[1];
                    if (!student._subclsInfoMap.containsKey(key1)) {
                    	//教科コードが授業として登録されていない無視
                    	continue;
                    } else {
                    	Map subMap = (Map)student._subclsInfoMap.get(key1);
                    	if (!subMap.containsKey(subclasscd)) {
                    	    continue;
                    	} else {
                			final SubclsInfo si = (SubclsInfo)subMap.get(subclasscd);
                			//科目コードの存在確認が取れたので、設定
                            final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                            final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                            final BigDecimal sick = rs.getBigDecimal("SICK2");
                            final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
                            final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");
                            final BigDecimal late = rs.getBigDecimal("LATE");
                            final BigDecimal early = rs.getBigDecimal("EARLY");

                            final BigDecimal sick1 = si._isSaki ? rawReplacedSick : rawSick;
                            final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                            final BigDecimal sick2 = si._isSaki ? replacedSick : sick;
                        	if (chkClsCdArry.contains(subclsCut[3])) {
                                final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, attend, sick, late, early);

                                if (student._attendSyukketuDatMap.containsKey(subclsCut[3])) {
                                	//元先関係なく、教科単位で集計する。
                             	    SubclassAttendance addwk = (SubclassAttendance)student._attendSyukketuDatMap.get(subclsCut[3]);
                                    addwk.add(subclassAttendance);
                                } else {
                                    student._attendSyukketuDatMap.put(subclsCut[3], subclassAttendance);
                                }
                        	} else {
                        		//'9X'系の教科なら、無視
                            	//※'9X'の特殊なものについては、すぐ上の処理で吸収している。
                        		if (key1.startsWith("9")) {
                        			continue;
                        		}

                                final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, attend, sick2, late, early);
                                Map setSubAttendMap = null;
                                if (student._attendSubclsDatMap.containsKey(param._semester)) {
                                    setSubAttendMap = (Map) student._attendSubclsDatMap.get(param._semester);
                                } else {
                                    setSubAttendMap = new TreeMap();
                                    student._attendSubclsDatMap.put(param._semester, setSubAttendMap);
                                }
                                setSubAttendMap.put(subclasscd, subclassAttendance);

                        	}
                        }
                	}
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }


    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77051 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _categoryIsClass;
        final String _semester;
        final String _semesterName;
        final String _grade;
        final String _hrClass;
        final String[] _categorySelected;
        final String _year;
        final String _ctrlSeme;
        final String _attSemesDate;
        final String _maxSemester;
        final String _ctrlDate;
        final String _toeflSdate;

        final Map _attendParamMap;
        final Map _attendSubParamMap;

        final Map _subclassBunpuMap;
        private Map _semesterMap;

        final Map _certifInfo;
        final Map _psBuffer;
        final String _d016Namespare1;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _categoryIsClass = request.getParameter("DISP");
            _semester = request.getParameter("SEMESTER");
            _hrClass = request.getParameter("GRADE_HR_CLASS");
            if ("1".equals(_categoryIsClass)) {
                _grade = request.getParameter("GRADE");
            } else {
                _grade = StringUtils.substring(_hrClass, 0, 2);
            }
            String[] csCutWk = request.getParameterValues("CATEGORY_SELECTED");
            if (!"1".equals(_categoryIsClass)) {
            	for (int cnt=0;cnt < csCutWk.length;cnt++) {
            		final String[] schregno_split = StringUtils.split(csCutWk[cnt], '-');
            		csCutWk[cnt] = schregno_split[0];
            	}
            }
            _categorySelected = csCutWk;

            _year = request.getParameter("CTRL_YEAR");
            _ctrlSeme = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _maxSemester = request.getParameter("MAX_SEMESTER");
            _attSemesDate = StringUtils.replace(request.getParameter("ATTSEMES_DATE"), "/", "-");

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);

            _attendSubParamMap = new HashMap();
            _attendSubParamMap.put("DB2UDB", db2);
            _attendSubParamMap.put("HttpServletRequest", request);
            _attendSubParamMap.put("grade", _grade);
            _attendSubParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendSubParamMap.put("useCurriculumcd", "1");
            _psBuffer = new HashMap();

            _semesterName = getSemesterName(db2);
            _subclassBunpuMap = getSubclassBunpu(db2);
            _certifInfo = getCertifInfo(db2);
            _semesterMap = loadSemester(db2, _year, _grade);
            _d016Namespare1 = getNameMst(db2, _year, "D016", "01", "NAMESPARE1");
            _toeflSdate = get2YearsAgo();
        }

        private String getNameMst(final DB2UDB db2, final String year, final String namecd1, final String namecd2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("    " + field + " ");
                sql.append(" FROM NAME_MST T1 ");
                sql.append("    INNER JOIN NAME_YDAT T2 ON T2.YEAR = '" + year + "' AND T2.NAMECD1 = T1.NAMECD1 AND T2.NAMECD2 = T1.NAMECD2 ");
                sql.append(" WHERE ");
                sql.append("    T1.NAMECD1 = '" + namecd1 + "' ");
                sql.append("    AND T1.NAMECD2 = '" + namecd2 + "' ");
                sql.append("   ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString(field)) {
                        rtn = rs.getString(field);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private Map loadSemester(final DB2UDB db2, final String year, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   V_SEMESTER_GRADE_MST"
                        + " where"
                        + "   YEAR='" + year + "'"
                        + "   AND GRADE='" + grade + "'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("loadSemester exception!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }
        private String getSemesterName(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' "));
        }

        private Map getCertifInfo(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT * from CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '104' ");
                rs = ps.executeQuery();
                if (rs.next()) {  //1レコードのはずなので、先頭データだけ取得
                	final String kStr1 = "SCHOOL_NAME";
                	retMap.put(kStr1, rs.getString(kStr1));
                	final String kStr2 = "JOB_NAME";
                	retMap.put(kStr2, rs.getString(kStr2));
                	final String kStr3 = "PRINCIPAL_NAME";
                	retMap.put(kStr3, rs.getString(kStr3));
                }
            } catch (SQLException ex) {
                log.debug("getCertifInfo exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private String get2YearsAgo() {
        	String retStr = "";
        	try {
        	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date convWk = sdf.parse(_attSemesDate);
                Calendar cal = Calendar.getInstance();
        	    cal.setTime(convWk);
        	    cal.add(Calendar.YEAR, -2);
        	    cal.add(Calendar.DAY_OF_MONTH, -1);
        	    final String oginai = (cal.get(Calendar.MONTH)+1) > 9 ? "" : "0";
        	    retStr = cal.get(Calendar.YEAR) + "-" + oginai + (cal.get(Calendar.MONTH)+1) + "-" + cal.get(Calendar.DAY_OF_MONTH);
        	} catch (ParseException e) {
                log.error("exception!", e);
        	}
        	return retStr;
        }

        private Map getSubclassBunpu(final DB2UDB db2) {
        	final String useSemester = "9".equals(_semester) ? _maxSemester : _semester;
            final Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     REC_RANK.CLASSCD, ");
                stb.append("     REC_RANK.SCHOOL_KIND, ");
                stb.append("     REC_RANK.CURRICULUM_CD, ");
                stb.append("     REC_RANK.SUBCLASSCD, ");
                stb.append("     REGD.COURSECD, ");
                stb.append("     REGD.MAJORCD, ");
                stb.append("     REGD.COURSECODE, ");
                stb.append("     MAX(SUBM.SUBCLASSNAME) AS SUBCLASSNAME, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 0 AND 9 THEN 1 ELSE 0 END) AS SCORE0, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 10 AND 19 THEN 1 ELSE 0 END) AS SCORE1, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 20 AND 29 THEN 1 ELSE 0 END) AS SCORE2, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 30 AND 39 THEN 1 ELSE 0 END) AS SCORE3, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 40 AND 49 THEN 1 ELSE 0 END) AS SCORE4, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 50 AND 59 THEN 1 ELSE 0 END) AS SCORE5, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 60 AND 69 THEN 1 ELSE 0 END) AS SCORE6, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 70 AND 79 THEN 1 ELSE 0 END) AS SCORE7, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 80 AND 89 THEN 1 ELSE 0 END) AS SCORE8, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 90 AND 100 THEN 1 ELSE 0 END) AS SCORE9 ");
                stb.append(" FROM ");
                stb.append("     RECORD_RANK_SDIV_DAT REC_RANK ");
                stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ");
                stb.append("       ON REGD.YEAR = REC_RANK.YEAR ");
                stb.append("      AND REGD.SEMESTER = '" + useSemester + "' ");
                stb.append("      AND REGD.GRADE = '" + _grade + "' ");
                stb.append("      AND REGD.SCHREGNO = REC_RANK.SCHREGNO ");
                stb.append("     INNER JOIN SUBCLASS_MST SUBM ON REC_RANK.CLASSCD = SUBM.CLASSCD ");
                stb.append("           AND REC_RANK.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
                stb.append("           AND REC_RANK.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
                stb.append("           AND REC_RANK.SUBCLASSCD = SUBM.SUBCLASSCD ");
                stb.append(" WHERE ");
                stb.append("     REC_RANK.YEAR = '" + _year + "' ");
                stb.append("     AND REC_RANK.SEMESTER = '" + _semester + "' ");
                stb.append("     AND REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD || REC_RANK.SCORE_DIV = '" + FIXED_TESTCD + "08" + "' ");
                stb.append(" GROUP BY ");
                stb.append("     REC_RANK.CLASSCD, ");
                stb.append("     REC_RANK.SCHOOL_KIND, ");
                stb.append("     REC_RANK.CURRICULUM_CD, ");
                stb.append("     REC_RANK.SUBCLASSCD, ");
                stb.append("     REGD.COURSECD, ");
                stb.append("     REGD.MAJORCD, ");
                stb.append("     REGD.COURSECODE ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classcd = StringUtils.defaultString(rs.getString("CLASSCD"));
                    final String schoolKind = StringUtils.defaultString(rs.getString("SCHOOL_KIND"));
                    final String curriculumCd = StringUtils.defaultString(rs.getString("CURRICULUM_CD"));
                    final String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    final String coursecd = StringUtils.defaultString(rs.getString("COURSECD"));
                    final String majorcd = StringUtils.defaultString(rs.getString("MAJORCD"));
                    final String coursecode = StringUtils.defaultString(rs.getString("COURSECODE"));
                    final int score0 = rs.getInt("SCORE0");
                    final int score1 = rs.getInt("SCORE1");
                    final int score2 = rs.getInt("SCORE2");
                    final int score3 = rs.getInt("SCORE3");
                    final int score4 = rs.getInt("SCORE4");
                    final int score5 = rs.getInt("SCORE5");
                    final int score6 = rs.getInt("SCORE6");
                    final int score7 = rs.getInt("SCORE7");
                    final int score8 = rs.getInt("SCORE8");
                    final int score9 = rs.getInt("SCORE9");

                    final SubclassBunpu subclassBunpu = new SubclassBunpu(classcd, schoolKind, curriculumCd, subclasscd, subclassname, score0, score1, score2, score3, score4, score5, score6, score7, score8, score9);
                    final String setSubclsCrsCd = classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd + "-" + coursecd + "-" + majorcd + "-" + coursecode;
                    retMap.put(setSubclsCrsCd, subclassBunpu);
                }
            } catch (SQLException ex) {
                log.debug("getSubclassBunpu exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }
    }
}

// eof
