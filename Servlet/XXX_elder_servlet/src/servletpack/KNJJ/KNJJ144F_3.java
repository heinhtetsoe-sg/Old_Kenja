// kanji=漢字
package servletpack.KNJJ;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: f84d41d066f5871b0e3a7a16f5b99bd07d22d3ab $
 */
public class KNJJ144F_3 {

    private static final Log log = LogFactory.getLog("KNJJ144F_3.class");

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

            _hasData = printMain(db2, svf);
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

    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

    	final int MaxLine = 50;
    	final Map gh_Map = getStudentList(db2); //生徒成績

    	if(gh_Map.isEmpty()) {
        	return false;
        }
		svf.VrSetForm("KNJJ144F_3.frm", 1);
		svf.VrsOut("NUM", _param._eventCnt); // 回数
		svf.VrsOut("TITLE", _param._schoolName + " " + _param._eventName); // タイトル
		svf.VrsOut("DATE", h_format_Seireki_MD(_param._eventDate) + " 実施"); // 実施日

		int line = 1;
		for(int no = 1; MaxLine >= no; no++) {
			svf.VrsOutn("NO",line, String.valueOf(no)); // NO
			line++;
		}
		String grade = "";
		int col = 1; // 列
		for (Iterator ite = gh_Map.keySet().iterator(); ite.hasNext();) {
			final String getKey = (String)ite.next();
        	final GradeHrCls hrClSObj = (GradeHrCls)gh_Map.get(getKey);

        	if("".equals(grade)) {
        		grade = String.valueOf(Integer.parseInt(hrClSObj._gradeCd));
        		svf.VrsOut("GRADE", " " + grade + "年"); // 学年
        	}

        	svf.VrsOut("HR_NAME" + String.valueOf(col), hrClSObj._hrName); // 組

            line = 1; //　行
        	for (Iterator stuite = hrClSObj._schregMap.keySet().iterator(); stuite.hasNext();) {
        		final String stuKey = (String)stuite.next();
        		final Student student = (Student)hrClSObj._schregMap.get(stuKey);

        		if(student._marathon._attend_Cd == null) {
        			svf.VrsOutn("RANK" + String.valueOf(col),line, student._marathon._rank); // 順位
        		} else {
        			svf.VrsOutn("RANK" + String.valueOf(col),line, student._marathon._absent); // 出席情報
        		}
        		line++;
        	}

        	svf.VrsOutn("RANK" + String.valueOf(col),51, hrClSObj._boyCount); // 走者男
        	svf.VrsOutn("RANK" + String.valueOf(col),52, hrClSObj._girlCount); // 走者女
        	svf.VrsOutn("RANK" + String.valueOf(col),53, hrClSObj._boyRank); // 順計男
        	svf.VrsOutn("RANK" + String.valueOf(col),54, hrClSObj._girlRank); // 順計女
        	svf.VrsOutn("RANK" + String.valueOf(col),55, hrClSObj._goukei); // 順計全
        	svf.VrsOutn("RANK" + String.valueOf(col),56, hrClSObj._allRank); // 年順位

        	col++;
		}
		svf.VrEndPage();

    return true;
    }

    private Map getStudentList(final DB2UDB db2) throws SQLException {
    	Map retMap = new LinkedMap();
    	PreparedStatement ps = null;
        ResultSet rs = null;
        GradeHrCls ghCls = null;

		try {
			final StringBuffer stb = new StringBuffer();
			stb.append(" SELECT ");
			stb.append("     T1.YEAR, ");
			stb.append("     T4.SEQ, ");
			stb.append("     T3.SCHREGNO, ");
			stb.append("     T2.GRADE_CD, ");
			stb.append("     T1.HR_CLASS, ");
			stb.append("     T1.HR_CLASS_NAME1, ");
			stb.append("     T3.ATTENDNO, ");
			stb.append("     T5.NAME, ");
			stb.append("     T5.SEX, ");
			stb.append("     T7.TIME_H, ");
			stb.append("     T7.TIME_M, ");
			stb.append("     T7.TIME_S, ");
			stb.append("     T4.ATTEND_CD, ");
			stb.append("     T6.NAME1, ");
			stb.append("     T7.GRADE_RANK_SEX ");
			stb.append(" FROM SCHREG_REGD_HDAT T1");
			stb.append(" LEFT JOIN SCHREG_REGD_GDAT T2 ON T1.YEAR = T2.YEAR AND T2.GRADE = T1.GRADE ");
			stb.append(" LEFT JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T2.YEAR AND T3.SEMESTER = '" + _param._semester + "' AND T3.GRADE = T2.GRADE AND T3.HR_CLASS = T1.HR_CLASS ");
			stb.append(" LEFT JOIN MARATHON_EVENT_DAT T4 ON T4.YEAR = T3.YEAR AND T4.SCHREGNO = T3.SCHREGNO AND T4.SEQ = '00'");
			stb.append(" LEFT JOIN SCHREG_BASE_MST T5 ON T5.SCHREGNO = T3.SCHREGNO ");
			stb.append(" LEFT JOIN NAME_MST T6 ON T6.NAMECD1 = 'J010' AND T4.ATTEND_CD = T6.NAMECD2 ");
			stb.append(" LEFT JOIN MARATHON_EVENT_RANK_DAT T7 ON T7.YEAR = T4.YEAR AND T7.SCHREGNO = T4.SCHREGNO AND T7.SEQ = T4.SEQ");
			stb.append(" WHERE T1.YEAR = '" + _param._year + "' AND T1.GRADE = '" + _param._grade + "' AND T1.SEMESTER = '" + _param._semester + "'");
			stb.append(" ORDER BY HR_CLASS,ATTENDNO ");

			log.debug(" studentList sql =" + stb.toString());

			ps = db2.prepareStatement(stb.toString());
			rs = ps.executeQuery();
		     //生徒成績データ
			while (rs.next()) {
				final String schregno = rs.getString("SCHREGNO");
				final String gradeCd = rs.getString("GRADE_CD");
				final String hrclass = rs.getString("HR_CLASS");
				final String attendno = rs.getString("ATTENDNO");
				final String hrName = rs.getString("HR_CLASS_NAME1");
				final String name = rs.getString("NAME");
				final String sex = rs.getString("SEX");
				final String rank = rs.getString("GRADE_RANK_SEX");
				final String time_H = rs.getString("TIME_H");
				final String time_M = rs.getString("TIME_M");
				final String time_S = rs.getString("TIME_S");
				final String attend_Cd = rs.getString("ATTEND_CD");
				final String absent = rs.getString("NAME1");

				final Marathon marathon = new Marathon(time_H, time_M, time_S, rank, attend_Cd, absent);
				final Student student = new Student(schregno, gradeCd, hrclass, attendno, hrName, name, sex, marathon);

				final String ghrkey = _param._grade + hrclass;

				if (retMap.containsKey(ghrkey)) {
					ghCls = (GradeHrCls)retMap.get(ghrkey);
				} else {
					ghCls = new GradeHrCls(_param._grade, gradeCd, hrclass, hrName);
					ghCls.setHrinfo(db2,"1");
					retMap.put(ghrkey, ghCls);
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
    	final String _gradeCd;
    	final String _hrclass;
    	final String _hrName;
    	final Map _schregMap;
    	String _boyCount;
    	String _girlCount;
    	String _boyRank;
    	String _girlRank;
    	String _goukei;
    	String _allRank;
    	GradeHrCls(
    			final String grade,
            	final String gradeCd,
            	final String hrclass,
            	final String hrName
    			) {
    		_grade = grade;
        	_gradeCd = gradeCd;
        	_hrclass = hrclass;
        	_hrName = hrName;
        	_schregMap = new LinkedMap();
    	}

    	public void setHrinfo(final DB2UDB db2, final String flg) throws SQLException {
    		Map retMap = new LinkedMap();
        	PreparedStatement ps = null;
            ResultSet rs = null;

    		try {
    			final StringBuffer stb = new StringBuffer();
    			stb.append(" WITH RANK AS ( ");
    			stb.append(" SELECT ");
    			stb.append("     T1.YEAR, ");
    			stb.append("     T4.SEQ, ");
    			stb.append("     T3.SCHREGNO, ");
    			stb.append("     T2.GRADE_CD, ");
    			stb.append("     T1.HR_CLASS, ");
    			stb.append("     T1.HR_CLASS_NAME1, ");
    			stb.append("     T3.ATTENDNO, ");
    			stb.append("     T5.NAME, ");
    			stb.append("     T5.SEX, ");
    			stb.append("     T4.TIME_H, ");
    			stb.append("     T4.TIME_M, ");
    			stb.append("     T4.TIME_S, ");
    			stb.append("     T4.ATTEND_CD, ");
    			stb.append("     T6.NAME1, ");
    			stb.append("     T7.GRADE_RANK_SEX ");
    			stb.append(" FROM SCHREG_REGD_HDAT T1");
    			stb.append(" LEFT JOIN SCHREG_REGD_GDAT T2 ON T1.YEAR = T2.YEAR AND T2.GRADE = T1.GRADE ");
    			stb.append(" LEFT JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T2.YEAR AND T3.SEMESTER = '" + _param._semester + "' AND T3.GRADE = T2.GRADE AND T3.HR_CLASS = T1.HR_CLASS ");
    			stb.append(" LEFT JOIN MARATHON_EVENT_DAT T4 ON T4.YEAR = T3.YEAR AND T4.SCHREGNO = T3.SCHREGNO AND T4.SEQ = '00'");
    			stb.append(" LEFT JOIN SCHREG_BASE_MST T5 ON T5.SCHREGNO = T3.SCHREGNO ");
    			stb.append(" LEFT JOIN NAME_MST T6 ON T6.NAMECD1 = 'J010' AND T4.ATTEND_CD = T6.NAMECD2 ");
    			stb.append(" LEFT JOIN MARATHON_EVENT_RANK_DAT T7 ON T7.YEAR = T4.YEAR AND T7.SCHREGNO = T4.SCHREGNO AND T7.SEQ = T4.SEQ");
    			stb.append(" WHERE T1.YEAR = '" + _param._year + "' AND T1.GRADE = '" + _param._grade + "' AND T1.SEMESTER = '" + _param._semester + "'");
    			stb.append(" ORDER BY HR_CLASS,ATTENDNO ");
    			stb.append(" ), KEKKA_CNT AS ( ");
    			stb.append("   SELECT ");
    			stb.append("     HR_CLASS, ");
    			stb.append("     SEX, ");
    			stb.append("     COUNT(SEX) AS COUNT, ");
    			stb.append("     DECIMAL(ROUND(AVG(CAST(GRADE_RANK_SEX AS double)),2),4,1) AS AVG ");
    			stb.append(" FROM ");
    			stb.append("     RANK ");
    			stb.append(" WHERE ");
    			stb.append("     ATTEND_CD IS NULL ");
    			stb.append(" GROUP BY ");
    			stb.append("     HR_CLASS, ");
    			stb.append("     SEX ");
    			stb.append(" ), KEKKA_ALL AS ( ");
    			stb.append(" SELECT  ");
    			stb.append("     HR_CLASS, ");
    			stb.append("     SUM(AVG) AS GOUKEI ");
    			stb.append(" FROM KEKKA_CNT ");
    			stb.append(" GROUP BY ");
    			stb.append("     HR_CLASS ");
    			stb.append(" ), KEKKA_RANK AS ( ");
    			stb.append(" SELECT ");
    			stb.append(" HR_CLASS,GOUKEI,DENSE_RANK() OVER(ORDER BY (GOUKEI) ASC) AS ALL_RANK ");
    			stb.append(" FROM KEKKA_ALL ");
    			stb.append(" ),KEKKA_BOY AS ( ");
    			stb.append(" SELECT ");
    			stb.append(" HR_CLASS,SEX,COUNT AS BOY_COUNT,AVG AS BOY_RANK ");
    			stb.append(" FROM KEKKA_CNT ");
    			stb.append(" WHERE SEX = '1' ");
    			stb.append(" ),KEKKA_GIRL AS ( ");
    			stb.append(" SELECT ");
    			stb.append(" HR_CLASS,SEX,COUNT AS GIRL_COUNT,AVG AS GIRL_RANK ");
    			stb.append(" FROM KEKKA_CNT ");
    			stb.append(" WHERE SEX = '2' ");
    			stb.append(" ) ");
    			stb.append("  SELECT ");
    			stb.append("     T1.HR_CLASS, ");
    			stb.append("     T1.GOUKEI, ");
    			stb.append("     T1.ALL_RANK, ");
    			stb.append("     T2.BOY_COUNT, ");
    			stb.append("     T2.BOY_RANK, ");
    			stb.append("     T3.GIRL_COUNT, ");
    			stb.append("     T3.GIRL_RANK ");
    			stb.append(" FROM ");
    			stb.append("     KEKKA_RANK T1 ");
    			stb.append(" LEFT JOIN ");
    			stb.append("     KEKKA_BOY T2 ON T1.HR_CLASS = T2.HR_CLASS ");
    			stb.append(" LEFT JOIN ");
    			stb.append("     KEKKA_GIRL T3 ON T1.HR_CLASS = T3.HR_CLASS ");


    			log.debug(" hrinfo sql =" + stb.toString());

    			ps = db2.prepareStatement(stb.toString());
    			rs = ps.executeQuery();
    			//男女別参加人数、順位の計
    			while (rs.next()) {
    				final String hrclass = rs.getString("HR_CLASS");
    				final String goukei = rs.getString("GOUKEI");
    				final String allrank = rs.getString("ALL_RANK");
    				final String boycount = rs.getString("BOY_COUNT");
    				final String boyrank = rs.getString("BOY_RANK");
    				final String girlcount = rs.getString("GIRL_COUNT");
    				final String girlrank = rs.getString("GIRL_RANK");

    				if(goukei != null) {
    					if (_hrclass.equals(hrclass)) {
        					_goukei = goukei;
        					_allRank = allrank;
            				_boyCount = boycount;
            				_boyRank = boyrank;
           					_girlCount = girlcount;
           					_girlRank = girlrank;
    					}
    				}
    			}
    		} catch (final SQLException e) {
    			log.error("生徒の基本情報取得でエラー", e);
    			throw e;
    		} finally {
    			db2.commit();
    			DbUtils.closeQuietly(null, ps, rs);
    		}
    	}
    }

    private class Marathon {
    	final String _time_H; //タイム（時）
    	final String _time_M; //タイム（分）
    	final String _time_S; //タイム（秒）
    	final String _rank; //学年別性別順位
    	final String _attend_Cd; //出欠CD
    	final String _absent; //出席情報

    	public Marathon(
				final String time_H, final String time_M,
				final String time_S, final String rank, final String attend_Cd, final String absent) {
    		_time_H = time_H;
    		_time_M = time_M;
    		_time_S = time_S;
    	    _rank = rank;
    	    _attend_Cd = attend_Cd;
    	    _absent = absent;
    	}
    }

    private class Student {
    	final String _schregno; //学籍番号
    	final String _grade; //学年
    	final String _hr_Class; //クラス
    	final String _attendno; //番号
    	final String _hr_Name; // クラス名
    	final String _name; //氏名
    	final String _sex; //性別
    	final Marathon _marathon; //大会データ

    	public Student(
				final String schregno, final String grade,
				final String hr_Class, final String attendno, final String hr_Name, final String name,final String sex, final Marathon marathon) {
    	    _schregno = schregno;
    	    _grade = grade;
    	    _hr_Class = hr_Class;
    	    _attendno = attendno;
    	    _hr_Name = hr_Name;
    	    _name = name;
    	    _sex = sex;
    	    _marathon = marathon;
    	}
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75940 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _useCurriculumcd;
        final String _useSchool_KindField;
        final String _semester;
        final String _schoolKind;
        final String _schoolCd;
        final String _eventName; //マラソン大会名称
        final String _eventCnt;  //マラソン大会回数名
        final String _eventDate; //マラソン大会実施日
        final String _schoolName;
        final String _grade;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _schoolKind = request.getParameter("SCHOOLKIND");
            _schoolCd = request.getParameter("SCHOOLCD");
            _grade = request.getParameter("GRADE_3");
            _eventName = getEventName(db2);
            _eventCnt = getEventCnt(db2);
            _eventDate = getEventDate(db2);
            _schoolName = getSchoolName(db2);
            String seme = getSemester(db2);
            _semester = seme != null ? seme : request.getParameter("LOGIN_SEMESTER");

        }

        private String getEventName(final DB2UDB db2) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT EVENT_NAME FROM MARATHON_EVENT_MST WHERE YEAR = '" + _year + "'"));
        }
        private String getEventCnt(final DB2UDB db2) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NUMBER_OF_TIMES FROM MARATHON_EVENT_MST WHERE YEAR = '" + _year + "'"));
        }
        private String getEventDate(final DB2UDB db2) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT EVENT_DATE FROM MARATHON_EVENT_MST WHERE YEAR = '" + _year + "'"));
        }
        private String getSchoolName(final DB2UDB db2) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' AND SCHOOLCD = '" + _schoolCd + "' AND SCHOOL_KIND = '" + _schoolKind + "'"));
        }
        private String getSemester(final DB2UDB db2) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SEMESTER FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SDATE <= '" + _eventDate + "' AND EDATE >= '" + _eventDate + "'  ORDER BY SEMESTER"));
        }

    }

    private String h_format_Seireki_MD(final String date) {
        if (null == date || "".equals(date)) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        String retVal = "";
        sdf.applyPattern("yyyy年M月d日");
        retVal = sdf.format(java.sql.Date.valueOf(date));

        return retVal;
    }

}

// eof
