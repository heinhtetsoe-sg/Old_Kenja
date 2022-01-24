// kanji=漢字
package servletpack.KNJJ;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 49fc865415f22b46456627810dbaeb15653fc787 $
 */
public class KNJJ144F_2 {

    private static final Log log = LogFactory.getLog("KNJJ144F_2.class");

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
    	final Map studentMap = getStudentList(db2); //　生徒マラソン成績
    	if(studentMap.isEmpty()) {
        	return false;
        }
		svf.VrSetForm("KNJJ144F_2.frm", 1);
		svf.VrsOut("NUM", _param._eventCnt); // 回数
		svf.VrsOut("TITLE", _param._schoolName + " " + _param._eventName); // タイトル
		svf.VrsOut("DATE", h_format_Seireki_MD(_param._eventDate) + " 実施"); // 実施日
		svf.VrsOut("SUBTITLE", "女子上位者一覧表"); // サブタイトル

		boolean boyFlg = false; //男女改ページフラグ

		for(Iterator itr = studentMap.keySet().iterator(); itr.hasNext(); ) {
			final String key = (String)itr.next();
			final List<Student> studentList = (List) studentMap.get(key);
			int line = 1; // 帳票印字位置

			for (final Student student : studentList) {
				if("1".equals(student._sex) && boyFlg == false) {
					boyFlg = true;
					svf.VrEndPage();
					svf.VrSetForm("KNJJ144F_2.frm", 1);
					svf.VrsOut("NUM", _param._eventCnt); // 回数
					svf.VrsOut("TITLE", _param._schoolName + " " + _param._eventName); // タイトル
					svf.VrsOut("DATE", h_format_Seireki_MD(_param._eventDate) + " 実施"); // 実施日
					svf.VrsOut("SUBTITLE", "男子上位者一覧表"); // サブタイトル
				}
				String gradeField = String.valueOf(Integer.parseInt(student._grade));
				String fieldName = "1_" + gradeField;
				svf.VrsOutn("RANK1_" + gradeField, line, student._marathon._rank); // 順位
				svf.VrsOutn("HR_NAME" + fieldName, line, student._hr_Name); // 組
				svf.VrsOutn("NAME" + fieldName, line, student._name); // 氏名
				String time = "";
				if (student._marathon._time_M != null && !"0".equals(student._marathon._time_M))
					time += String.valueOf(student._marathon._time_M) + "分";
				if (student._marathon._time_S != null)
					time += String.valueOf(student._marathon._time_S) + "秒";
				svf.VrsOutn("TIME" + fieldName, line, time); // タイム
				line++;
			}
		}
		svf.VrEndPage();
    return true;
    }

    private Map getStudentList(final DB2UDB db2) throws SQLException {
    	Map retMap = new LinkedMap();
    	PreparedStatement ps = null;
        ResultSet rs = null;

		try {
			final StringBuffer stb = new StringBuffer();
			stb.append(" SELECT ");
			stb.append("     T1.YEAR, ");
			stb.append("     T1.SEQ, ");
			stb.append("     T1.SCHREGNO, ");
			stb.append("     T1.TIME_H, ");
			stb.append("     T1.TIME_M, ");
			stb.append("     T1.TIME_S, ");
			stb.append("     T2.ATTENDNO, ");
			stb.append("     T2.GRADE, ");
			stb.append("     T3.GRADE_CD, ");
			stb.append("     T4.HR_CLASS, ");
			stb.append("     T4.HR_CLASS_NAME1, ");
			stb.append("     T5.NAME, ");
			stb.append("     T5.SEX, ");
			stb.append("     T1.GRADE_RANK_SEX ");
			stb.append(" FROM MARATHON_EVENT_RANK_DAT T1 ");
			stb.append(" LEFT JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR AND T1.SCHREGNO = T2.SCHREGNO AND T2.SEMESTER = '" + _param._semester + "' ");
			stb.append(" LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T2.YEAR AND T2.GRADE = T3.GRADE  ");
			stb.append(" LEFT JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR = T3.YEAR AND T4.GRADE = T3.GRADE AND T4.SEMESTER = T2.SEMESTER AND T4.HR_CLASS = T2.HR_CLASS ");
			stb.append(" LEFT JOIN SCHREG_BASE_MST T5 ON T5.SCHREGNO = T1.SCHREGNO ");
			stb.append(" WHERE T1.YEAR = '" + _param._year + "' AND T1.SEQ = '00' AND T1.GRADE_RANK_SEX <= 25 ");
			stb.append(" ORDER BY T5.SEX DESC,T3.GRADE_CD,T1.GRADE_RANK_SEX,T4.HR_CLASS,T2.ATTENDNO ");

			log.debug(" studentList sql =" + stb.toString());

			ps = db2.prepareStatement(stb.toString());
			rs = ps.executeQuery();
			Map rankMap = null;
			List<Student> list = null;

			//生徒成績データ
			while (rs.next()) {
				final String schregno = rs.getString("SCHREGNO");
				final String grade = rs.getString("GRADE_CD");
				final String hr_Class = rs.getString("HR_CLASS");
				final String attendno = rs.getString("ATTENDNO");
				final String hr_Name = rs.getString("HR_CLASS_NAME1");
				final String name = rs.getString("NAME");
				final String sex = rs.getString("SEX");
				final String rank = rs.getString("GRADE_RANK_SEX");
				final String time_H = rs.getString("TIME_H");
				final String time_M = rs.getString("TIME_M");
				final String time_S = rs.getString("TIME_S");

				final Marathon marathon = new Marathon(time_H, time_M, time_S, rank);
				final Student student = new Student(schregno, grade, hr_Class, attendno, hr_Name, name, sex, marathon);

				final String gskey = grade + "-" + sex;

				if (retMap.containsKey(gskey)) {
					list = (List) retMap.get(gskey);
				} else {
					list = new ArrayList<Student>();
					retMap.put(gskey, list);
				}
				list.add(student);
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
    	final String _rank; //学年別性別順位

    	public Marathon(
				final String time_H, final String time_M,
				final String time_S, final String rank) {
    		_time_H = time_H;
    		_time_M = time_M;
    		_time_S = time_S;
    	    _rank = rank;
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
        log.fatal("$Revision: 75971 $");
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

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _schoolKind = request.getParameter("SCHOOLKIND");
            _schoolCd = request.getParameter("SCHOOLCD");
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
