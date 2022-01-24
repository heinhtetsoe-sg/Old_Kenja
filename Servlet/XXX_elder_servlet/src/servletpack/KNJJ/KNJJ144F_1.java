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
 * @version $Id: 993d98f5645caa48ec3139560e0e20758032201e $
 */
public class KNJJ144F_1 {

    private static final Log log = LogFactory.getLog("KNJJ144F_1.class");

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
    	int hrSize = _param._categorySelected.length;
    	for(int i = 0; hrSize > i; i++) {
    		final TreeMap studentList = getStudentList(db2,_param._categorySelected[i]);

        	if(studentList.isEmpty()) {
        		return false;
        	}

        	svf.VrSetForm("KNJJ144F_1.frm", 1);
        	svf.VrsOut("NUM", _param._eventCnt); //回数
        	svf.VrsOut("TITLE", _param._schoolName +  " " + _param._eventName); //タイトル
        	svf.VrsOut("DATE", h_format_Seireki_MD(_param._eventDate) + " 実施"); //実施日
        	String hrname = "";
        	int line = 1;
        	for(Iterator itr = studentList.keySet().iterator(); itr.hasNext(); ) {
        		final String key = (String)itr.next();
        		final Student student = (Student)studentList.get(key);

        		if("".equals(hrname)) {
        			hrname = student._hr_Name;
        			svf.VrsOut("HR_NAME", hrname); //年組 ＋記録表
        		}

        		svf.VrsOutn("NO",line, String.valueOf(Integer.parseInt(student._attendno))); // NO
        		svf.VrsOutn("NAME",line, student._name); // 氏名

        		String time = "";
        		if(student._marathon._time_H != null && !"0".equals(student._marathon._time_H)) time += String.valueOf(student._marathon._time_H) + "時間";
        		if(student._marathon._time_M != null) time += String.valueOf(student._marathon._time_M) + "分";
        		if(student._marathon._time_S != null) time += String.valueOf(student._marathon._time_S) + "秒";
        		svf.VrsOutn("TIME",line, time); // タイム

        		svf.VrsOutn("RANK",line, student._marathon._rank); // 順位
        		svf.VrsOutn("ABSENT",line, student._marathon._attendance); // 欠席

        		String bukatsu = "";
        		if(student._bukatsu.size() > 0) {
        			for(int cnt = 0; student._bukatsu.size() > cnt; cnt++) {
        				if(!"".equals(bukatsu)) bukatsu += " ";
        				bukatsu += student._bukatsu.get(cnt);
        			}
        		}
        		svf.VrsOutn("CLUB",line, bukatsu); // 部活動
        		svf.VrsOutn("REMARK",line, student._marathon._remark); // 備考
        		line++;
        	}

//        	if(MaxLine > line) {
//        		for(int spline = line; MaxLine >= spline; spline++) {
//        			svf.VrsOutn("NO",spline, String.valueOf(spline)); // 空欄埋め
//        		}
//        	}

        	svf.VrEndPage();
    	}
    return true;
    }

    private TreeMap getStudentList(final DB2UDB db2, final String hrCd) throws SQLException {
    	TreeMap retMap = new TreeMap();
    	PreparedStatement ps = null;
        ResultSet rs = null;

		try {
			final StringBuffer stb = new StringBuffer();
			stb.append(" SELECT ");
			stb.append("     T2.SCHREGNO, ");
			stb.append("     T5.GRADE_NAME1, ");
			stb.append("     T4.HR_CLASS_NAME1, ");
			stb.append("     T2.ATTENDNO, ");
			stb.append("     T4.HR_NAME, ");
			stb.append("     T5.SCHOOL_KIND, ");
			stb.append("     T3.NAME,");
			stb.append("     T7.TIME_H,");
			stb.append("     T7.TIME_M,");
			stb.append("     T7.TIME_S,");
			stb.append("     T8.NAME1,");
			stb.append("     T6.REMARK,");
			stb.append("     T7.GRADE_RANK_SEX");
			stb.append(" FROM ");
			stb.append("     SCHREG_REGD_DAT T2 ");
			stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T2.SCHREGNO ");
			stb.append("     INNER JOIN SCHREG_REGD_HDAT T4 ");
			stb.append("         ON  T4.YEAR     = T2.YEAR ");
			stb.append("         AND T4.SEMESTER = T2.SEMESTER ");
			stb.append("         AND T4.GRADE    = T2.GRADE ");
			stb.append("         AND T4.HR_CLASS = T2.HR_CLASS ");
			stb.append("     INNER JOIN SCHREG_REGD_GDAT T5 ON T2.YEAR = T5.YEAR AND T2.GRADE = T5.GRADE ");
			stb.append("     LEFT JOIN MARATHON_EVENT_DAT T6 ON T6.YEAR = T2.YEAR AND T6.SCHREGNO = T2.SCHREGNO AND T6.SEQ = '00' ");
			stb.append("     LEFT JOIN MARATHON_EVENT_RANK_DAT T7 ON T7.YEAR = T2.YEAR AND T7.SCHREGNO = T2.SCHREGNO AND T7.SEQ = T6.SEQ ");
			stb.append("     LEFT JOIN NAME_MST T8 ON T8.NAMECD1 = 'J010' AND T8.NAMECD2 = T6.ATTEND_CD ");
			stb.append(" WHERE ");
			stb.append("     T2.GRADE || T2.HR_CLASS = '" +  hrCd + "'");
			stb.append("     AND T2.YEAR      = '" + _param._year + "' ");
			stb.append("     AND T2.SEMESTER  = '" + _param._semester + "' ");
			stb.append(" ORDER BY  ");
			stb.append("     T2.GRADE, ");
			stb.append("     T2.HR_CLASS, ");
			stb.append("     T2.ATTENDNO ");

			log.debug(" studentList sql =" + stb.toString());

			ps = db2.prepareStatement(stb.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				final String schregno = rs.getString("SCHREGNO");
				final String grade = rs.getString("GRADE_NAME1");
				final String hr_Class = rs.getString("HR_CLASS_NAME1");
				final String attendno = rs.getString("ATTENDNO");
				final String hr_Name = rs.getString("HR_NAME");
				final String name = rs.getString("NAME");
				final String school_kind = rs.getString("SCHOOL_KIND");
				final String time_H = rs.getString("TIME_H");
				final String time_M = rs.getString("TIME_M");
				final String time_S = rs.getString("TIME_S");
				final String attendance = rs.getString("NAME1");
				final String rank = rs.getString("GRADE_RANK_SEX");
				final String remark = rs.getString("REMARK");
				final Marathon marathon = new Marathon(time_H,time_M,time_S,attendance,rank,remark);
				final Student student = new Student(schregno, grade, hr_Class, attendno,
						hr_Name, name, marathon);
				student.setBukatsu(db2, schregno,school_kind);
				retMap.put(attendno, student);
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

    private class Marathon {
    	final String _time_H; //タイム（時）
    	final String _time_M; //タイム（分）
    	final String _time_S; //タイム（秒）
    	final String _attendance; //出欠情報
    	final String _rank; //学年別性別順位
    	final String _remark; //備考

    	public Marathon(
				final String time_H, final String time_M,
				final String time_S, final String attendance, final String rank, final String remark) {
    		_time_H = time_H;
    		_time_M = time_M;
    		_time_S = time_S;
    	    _attendance = attendance;
    	    _rank = rank;
    	    _remark = remark;
    	}
    }

    private class Student {
    	final String _schregno; //学籍番号
    	final String _grade; //学年
    	final String _hr_Class; //クラス
    	final String _attendno; //番号
    	final String _hr_Name; // クラス名
    	final String _name; //氏名
    	final List<String>_bukatsu = new ArrayList<String>(); //部活動
    	final Marathon _marathon; //大会データ

    	public Student(
				final String schregno, final String grade,
				final String hr_Class, final String attendno, final String hr_Name, final String name,final Marathon marathon) {
    	    _schregno = schregno;
    	    _grade = grade;
    	    _hr_Class = hr_Class;
    	    _attendno = attendno;
    	    _hr_Name = hr_Name;
    	    _name = name;
    	    _marathon = marathon;
    	}

    	private void setBukatsu(final DB2UDB db2,final String schregno, final String school_kind) throws SQLException {
			PreparedStatement ps = null;
			ResultSet rs = null;

			try {
				final StringBuffer stb = new StringBuffer();
				stb.append(" SELECT ");
				stb.append("     T1.SCHREGNO, ");
				stb.append("     T1.CLUBCD, ");
				stb.append("     T1.SDATE, ");
				stb.append("     T1.EDATE, ");
				stb.append("     T2.CLUBNAME ");
				stb.append(" FROM SCHREG_CLUB_HIST_DAT T1 ");
				stb.append(" LEFT JOIN CLUB_MST T2 ON T1.SCHOOLCD = T2.SCHOOLCD AND T1.SCHOOL_KIND = T2.SCHOOL_KIND AND T1.CLUBCD = T2.CLUBCD ");
				stb.append(" WHERE T1.SCHREGNO = '" + schregno + "' AND T1.SDATE <= '" + _param._eventDate + "' AND (T1.EDATE > '" + _param._eventDate + "' OR  T1.EDATE IS NULL) ");
				stb.append(" AND T1.SCHOOLCD = '" + _param._schoolCd + "' AND T1.SCHOOL_KIND = '" + school_kind + "'");
				stb.append(" ORDER BY T1.CLUBCD ");

				log.debug(" bukatsu sql =" + stb.toString());

				ps = db2.prepareStatement(stb.toString());
				rs = ps.executeQuery();
				while (rs.next()) {
					final String clubname = rs.getString("CLUBNAME");
					if(clubname != null) _bukatsu.add(clubname);
				}
			} catch (final SQLException e) {
				log.error("部活動の基本情報取得でエラー", e);
				throw e;
			} finally {
				db2.commit();
				DbUtils.closeQuietly(null, ps, rs);
			}
    	}

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75939 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _grade;
        final String _useCurriculumcd;
        final String _useSchool_KindField;
        final String _semester;
        final String _schoolKind;
        final String _schoolCd;
        final String[] _categorySelected;
        final String _eventName; //マラソン大会名称
        final String _eventCnt;  //マラソン大会回数名
        final String _eventDate; //マラソン大会実施日
        final String _schoolName;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _grade = request.getParameter("GRADE_1");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _schoolKind = request.getParameter("SCHOOLKIND");
            _schoolCd = request.getParameter("SCHOOLCD");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
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
