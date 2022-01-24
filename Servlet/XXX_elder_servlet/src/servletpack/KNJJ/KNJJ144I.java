// kanji=漢字
package servletpack.KNJJ;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
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
 * @version $Id: 9d00f0200fb4822318f36a42b0620cbc8ed48879 $
 */
public class KNJJ144I {

    private static final Log log = LogFactory.getLog("KNJJ144I.class");

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

    	final int MaxLine = 45;
    	final Map kaikin_Map = getStudentList(db2); //皆勤者Map
    	final Map gh_Map = getHrClass(db2); //クラスMap

    	if(gh_Map.isEmpty()) {
        	return false;
        }

		int line = 1; // 印字行
		int col = 1; // 列
		for (Iterator ite = gh_Map.keySet().iterator(); ite.hasNext();) {

        	if(col == 1 || col > 12) {
        		if(col > 12) {
        			svf.VrEndPage();
        			col = 1;
        			line = 1;
        		}
        		svf.VrSetForm("KNJJ144I.frm", 1);

        		String title = _param._year + "年度 " + "蠟八摂心 ";
            	if(Integer.parseInt(_param._gradeCd) > 1) {
            		title += _param._gradeCd + "年間";
            	}
            	title += "皆勤者 (" + _param._gradeCd + "年生)";

        		svf.VrsOut("TITLE", title); // タイトル
        		svf.VrsOut("DATE", _param._date); // 実施日
        		svf.VrsOut("TOTAL_NUM", "皆勤者総数 " + String.valueOf(_param._count) + " 人"); // 学年皆勤者数

        		for(int no = 1; MaxLine >= no; no++) {
        			svf.VrsOutn("NO",line, String.valueOf(no)); // NO
        			line++;
        		}
        	}

        	final String getKey = (String)ite.next();
			svf.VrsOut("HR_NAME" + String.valueOf(col), (String)gh_Map.get(getKey) + "組"); // 組
        	final GradeHrCls hrClSObj = (GradeHrCls)kaikin_Map.get(getKey);
        	if(hrClSObj == null) {
        		col++;
        		continue;
        	}

        	line = 1;

        	for (Iterator stuite = hrClSObj._schregMap.keySet().iterator(); stuite.hasNext();) {
        		final String stuKey = (String)stuite.next();
            	final Student student = (Student)hrClSObj._schregMap.get(stuKey);

            	svf.VrsOutn("ATTENDNO" + String.valueOf(col),line, String.valueOf(Integer.parseInt(student._attendno))); // 出席番号
            	svf.VrsOutn("NAME" + String.valueOf(col), line, student._name); // 氏名
            	line++;
        	}

        	svf.VrsOut("NUM" + String.valueOf(col), String.valueOf(line - 1)); // クラス皆勤者数
        	col++;

		}
		svf.VrEndPage();

    return true;
    }

    private Map getHrClass(final DB2UDB db2) throws SQLException {
    	Map retMap = new LinkedMap();
    	PreparedStatement ps = null;
        ResultSet rs = null;

        try {
        	final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     HR_CLASS, ");
            stb.append("     HR_CLASS_NAME1 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' AND ");
            stb.append("     GRADE = '" + _param._grade + "' AND ");
            stb.append("     SEMESTER = '" + _param._semester + "' ");
            stb.append(" ORDER BY HR_CLASS ");

            log.debug(" hrclass sql =" + stb.toString());

			ps = db2.prepareStatement(stb.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				final String hrclass = rs.getString("HR_CLASS");
				final String hrclass_name = rs.getString("HR_CLASS_NAME1");

				if (!retMap.containsKey(hrclass)) {
					retMap.put(hrclass, hrclass_name);
				}
			}

        } catch (final SQLException e) {
			log.error("クラスの基本情報取得でエラー", e);
			throw e;
		} finally {
			db2.commit();
			DbUtils.closeQuietly(null, ps, rs);
		}
        return retMap;
    }

    private Map getStudentList(final DB2UDB db2) throws SQLException {
    	Map retMap = new LinkedMap();
    	PreparedStatement ps = null;
        ResultSet rs = null;
        GradeHrCls ghCls = null;

        final String kanendo = String.valueOf(Integer.parseInt(_param._year) - (Integer.parseInt(_param._gradeCd) -1));


		try {
			final StringBuffer stb = new StringBuffer();
			stb.append(" WITH KAIKIN_COUNT AS ( ");
			stb.append(" SELECT ");
			stb.append("     SCHREGNO,COUNT(SCHREGNO) AS COUNT ");
			stb.append(" FROM ");
			stb.append("     ROU_HATSU_SESSIN_KAI_DAT ");
			stb.append(" WHERE ");
			stb.append("     KAIKIN_FLG = '1' AND YEAR BETWEEN " + kanendo + " AND " + _param._year);
			stb.append(" GROUP BY ");
			stb.append("  SCHREGNO ");
			stb.append(" ) ");
			stb.append(" SELECT  ");
			stb.append("     T1.SCHREGNO, ");
			stb.append("     T1.COUNT, ");
			stb.append("     T2.GRADE, ");
			stb.append("     T3.GRADE_CD, ");
			stb.append("     T4.HR_CLASS, ");
			stb.append("     T4.HR_CLASS_NAME1, ");
			stb.append("     T2.ATTENDNO, ");
			stb.append("     T5.NAME ");
			stb.append(" FROM KAIKIN_COUNT T1 ");
			stb.append(" INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = '" + _param._year + "' AND T1.SCHREGNO = T2.SCHREGNO AND T2.SEMESTER = '" + _param._semester + "' AND T2.GRADE = '" + _param._grade + "' ");
			stb.append(" LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T2.YEAR AND T3.GRADE = T2.GRADE ");
			stb.append(" LEFT JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR = T3.YEAR AND T4.GRADE = T3.GRADE AND T4.SEMESTER = T2.SEMESTER AND T4.HR_CLASS = T2.HR_CLASS ");
			stb.append(" LEFT JOIN SCHREG_BASE_MST T5 ON T5.SCHREGNO = T2.SCHREGNO ");
			stb.append(" WHERE T1.COUNT = '" + _param._gradeCd + "' ");
			stb.append(" ORDER BY T4.HR_CLASS,T2.ATTENDNO ");

			log.debug(" studentList sql =" + stb.toString());

			ps = db2.prepareStatement(stb.toString());
			rs = ps.executeQuery();

			int cnt = 0; //皆勤者数
			while (rs.next()) {
				final String schregno = rs.getString("SCHREGNO");
				final String gradeCd = rs.getString("GRADE_CD");
				final String hrclass = rs.getString("HR_CLASS");
				final String attendno = rs.getString("ATTENDNO");
				final String hrName = rs.getString("HR_CLASS_NAME1");
				final String name = rs.getString("NAME");
				final String count = rs.getString("COUNT");

				final Student student = new Student(schregno, gradeCd, hrclass, attendno, hrName, name, count);
				final String hrkey = hrclass;

				if (retMap.containsKey(hrkey)) {
					ghCls = (GradeHrCls)retMap.get(hrkey);
				} else {
					ghCls = new GradeHrCls(_param._grade, gradeCd, hrclass, hrName);
					retMap.put(hrkey, ghCls);
				}
                if (!ghCls._schregMap.containsKey(schregno)) {
                	ghCls._schregMap.put(schregno, student);
                	cnt++;
                }
			}

			_param._count = cnt;

		} catch (final SQLException e) {
			log.error("皆勤者の基本情報取得でエラー", e);
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
    }

    private class Student {
    	final String _schregno; //学籍番号
    	final String _grade; //学年
    	final String _hr_Class; //クラス
    	final String _attendno; //番号
    	final String _hr_Name; // クラス名
    	final String _name; //氏名
    	final String _count; //皆勤回数

    	public Student(
				final String schregno, final String grade,
				final String hr_Class, final String attendno, final String hr_Name, final String name,final String count) {
    	    _schregno = schregno;
    	    _grade = grade;
    	    _hr_Class = hr_Class;
    	    _attendno = attendno;
    	    _hr_Name = hr_Name;
    	    _name = name;
    	    _count = count;
    	}
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 76037 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _useCurriculumcd;
        final String _useSchool_KindField;
        final String _semester;
        final String _date;
        final String _schoolKind;
        final String _schoolCd;
        final String _grade;
        final String _gradeCd;
        int _count; //皆勤者数


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _schoolKind = request.getParameter("SCHOOLKIND");
            _schoolCd = request.getParameter("SCHOOLCD");
            _grade = request.getParameter("GRADE");
            _gradeCd = String.valueOf(Integer.parseInt(getGradeCd(db2)));
            _semester = request.getParameter("LOGIN_SEMESTER");

            String date = request.getParameter("LOGIN_DATE");
            _date = date != null ? date.replace("-", "/") : "";
        }

        private String getGradeCd(final DB2UDB db2) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "'"));
        }

    }
}

// eof
