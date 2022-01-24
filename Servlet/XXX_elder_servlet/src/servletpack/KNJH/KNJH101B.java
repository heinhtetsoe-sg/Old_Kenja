// kanji=漢字
package servletpack.KNJH;

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
 * @version $Id: 9c7f3ef1118554ca05496523a21f5801fba1c899 $
 */
public class KNJH101B {

    private static final Log log = LogFactory.getLog("KNJH101B.class");

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
    	final TreeMap studentList = getStudentList(db2);

    	if(studentList.isEmpty()) {
    		return false;
    	}

    	for(Iterator itr = studentList.keySet().iterator(); itr.hasNext(); ) {
    		final String key = (String)itr.next();
    		final Student student = (Student)studentList.get(key);

        	svf.VrSetForm("KNJH101B.frm", 1);
			svf.VrsOut("TITLE", "生活指導部会議資料"); // タイトル
			svf.VrsOut("PRINT_DATE", _param._date.replace("-", "/")); // 出力日付
			svf.VrsOut("DATE1", h_format_Seireki_MD(student._occurrence_date)); // 発生日
			svf.VrsOut("DATE2", h_format_Seireki_MD(student._investigation_Date)); // 調査日付
			svf.VrsOut("WRITER_NAME", student._written_Staff); // 作成者
			svf.VrsOut("CHECK_STAFF_NAME1", student._investigation_Staff1); // 調査教員1
			svf.VrsOut("CHECK_STAFF_NAME2", student._investigation_Staff2); // 調査教員2
			svf.VrsOut("CHECK_STAFF_NAME3", student._investigation_Staff3); // 調査教員3
			svf.VrsOut("CHECK_STAFF_NAME4", student._investigation_Staff4); // 調査教員4

			String nenkumiban = student._grade + " " + student._hr_Class + " " + Integer.parseInt(student._attendno) + "番";
			svf.VrsOut("HR_NAME", nenkumiban); // 年組番
			svf.VrsOut("NAME", student._name); // 氏名
			svf.VrsOut("CONTENT", student._detail); // 内容
			svf.VrsOut("DRAFT", student._original_Plan); // 原案
			svf.VrsOut("PUNISH", student._punish); // 処分
			svf.VrsOut("PLACE", student._occurrence_Place); // 場所
			svf.VrsOut("MEETING_DATE1", h_format_Seireki_MD(student._std_Guid_Mtg_Date)); // 生徒指導部会議
			svf.VrsOut("MEETING_DATE2", h_format_Seireki_MD(student._staff_Mtg_Date)); // 職員会議

			List content = KNJ_EditKinsoku.getTokenList(student._content,100,6);
			for(int i = 0; content.size() > i; i++) {
				svf.VrsOut("DETAIL_CONTENT" + (i + 1), content.get(i).toString()); // 詳細内容
			}

			svf.VrsOut("REMARK", student._remark); // 備考
			svf.VrsOut("DIARY", "1".equals(StringUtils.defaultString(student._diary_Flg)) ? "完了" : ""); // 日誌
			svf.VrsOut("COVENANT", "1".equals(StringUtils.defaultString(student._written_Oath_Flg)) ? "完了" : ""); // 誓約書
			svf.VrsOut("REPORT", "1".equals(StringUtils.defaultString(student._report_Flg)) ? "完了" : ""); // 調書

			svf.VrEndPage();
    	}
        return true;
    }

    private TreeMap getStudentList(final DB2UDB db2) throws SQLException {
    	TreeMap retMap = new TreeMap();
    	PreparedStatement ps = null;
        ResultSet rs = null;

		try {
			final StringBuffer stb = new StringBuffer();
			stb.append(" SELECT ");
			stb.append("     T2.SCHREGNO, ");
			stb.append("     T1.OCCURRENCE_DATE, ");
			stb.append("     T1.OCCURRENCE_PLACE, ");
			stb.append("     T5.GRADE_NAME1, ");
			stb.append("     T4.HR_CLASS_NAME1, ");
			stb.append("     T2.ATTENDNO, ");
			stb.append("     T4.HR_NAMEABBV, ");
			stb.append("     VALUE(T3.NAME_SHOW,T3.NAME) AS NAME, ");
			stb.append("     T1.INVESTIGATION_DATE, ");
			stb.append("     (SELECT VALUE(ST1.STAFFNAME_SHOW,ST1.STAFFNAME) FROM STAFF_MST ST1 WHERE ST1.STAFFCD = T1.INVESTIGATION_STAFFCD1) AS INVESTIGATION_STAFF1, ");
			stb.append("     (SELECT VALUE(ST1.STAFFNAME_SHOW,ST1.STAFFNAME) FROM STAFF_MST ST1 WHERE ST1.STAFFCD = T1.INVESTIGATION_STAFFCD2) AS INVESTIGATION_STAFF2, ");
			stb.append("     (SELECT VALUE(ST1.STAFFNAME_SHOW,ST1.STAFFNAME) FROM STAFF_MST ST1 WHERE ST1.STAFFCD = T1.INVESTIGATION_STAFFCD3) AS INVESTIGATION_STAFF3, ");
			stb.append("     (SELECT VALUE(ST1.STAFFNAME_SHOW,ST1.STAFFNAME) FROM STAFF_MST ST1 WHERE ST1.STAFFCD = T1.INVESTIGATION_STAFFCD4) AS INVESTIGATION_STAFF4, ");
			stb.append("     (SELECT VALUE(ST1.STAFFNAME_SHOW,ST1.STAFFNAME) FROM STAFF_MST ST1 WHERE ST1.STAFFCD = T1.WRITTEN_STAFFCD) AS WRITTEN_STAFF, ");
			stb.append("     T1.STD_GUID_MTG_DATE, ");
			stb.append("     T1.STAFF_MTG_DATE, ");
			stb.append("     T1.DIARY_FLG, ");
			stb.append("     T1.WRITTEN_OATH_FLG, ");
			stb.append("     T1.REPORT_FLG, ");
			stb.append("     (SELECT ST1.NAME1 FROM NAME_MST ST1 WHERE ST1.NAMECD1 = 'H304' AND ST1.NAMECD2= T1.DETAILCD) AS DETAIL,");
			stb.append("     (SELECT ST1.NAME1 FROM NAME_MST ST1 WHERE ST1.NAMECD1 = 'H318' AND ST1.NAMECD2= T1.ORIGINAL_PLAN_CD) AS ORIGINAL_PLAN,");
			stb.append("     (SELECT ST1.NAME1 FROM NAME_MST ST1 WHERE ST1.NAMECD1 = 'H318' AND ST1.NAMECD2= T1.PUNISH_CD) AS PUNISH,");
			stb.append("     VALUE(T1.CONTENT,' ') AS CONTENT, ");
			stb.append("     VALUE(T1.REMARK,' ') AS REMARK ");
			stb.append(" FROM ");
			stb.append("     SCHREG_REGD_DAT T2 ");
			stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T2.SCHREGNO ");
			stb.append("     INNER JOIN SCHREG_REGD_HDAT T4 ");
			stb.append("         ON  T4.YEAR     = T2.YEAR ");
			stb.append("         AND T4.SEMESTER = T2.SEMESTER ");
			stb.append("         AND T4.GRADE    = T2.GRADE ");
			stb.append("         AND T4.HR_CLASS = T2.HR_CLASS ");
			stb.append("     INNER JOIN SCHREG_DETAILHIST_DAT T1 ON  T1.YEAR = T2.YEAR AND T1.SCHREGNO = T2.SCHREGNO AND T1.DETAIL_DIV = '2' AND T1.DETAILCD BETWEEN '" + _param._batsuFrom + "' AND '" + _param._batsuTo + "' ");
			stb.append("     INNER JOIN SCHREG_REGD_GDAT T5 ON T2.YEAR = T5.YEAR AND T2.GRADE = T5.GRADE  ");
			stb.append(" WHERE ");
			stb.append("     T2.GRADE || T2.HR_CLASS  IN " +  SQLUtils.whereIn(true, _param._categorySelected));
			stb.append("     AND T2.YEAR      = '" + _param._year + "' ");
			stb.append("     AND T2.SEMESTER  = '" + _param._semester + "' ");
			stb.append(" ORDER BY  ");
			stb.append("     T1.OCCURRENCE_DATE, ");
			stb.append("     T2.GRADE, ");
			stb.append("     T2.HR_CLASS, ");
			stb.append("     T2.ATTENDNO ");

			log.debug(" studentList sql =" + stb.toString());

			ps = db2.prepareStatement(stb.toString());
			rs = ps.executeQuery();
			int no = 1;
			while (rs.next()) {
				final String schregno = rs.getString("SCHREGNO");
				final String occurrence_Date = rs.getString("OCCURRENCE_DATE");
				final String occurrence_Place = rs.getString("OCCURRENCE_PLACE");
				final String grade = rs.getString("GRADE_NAME1");
				final String hr_Class = rs.getString("HR_CLASS_NAME1");
				final String attendno = rs.getString("ATTENDNO");
				final String hr_Nameabbv = rs.getString("HR_NAMEABBV");
				final String name = rs.getString("NAME");
				final String investiation_Date = rs.getString("INVESTIGATION_DATE");
				final String investiation_Staff1 = rs.getString("INVESTIGATION_STAFF1");
				final String investiation_Staff2 = rs.getString("INVESTIGATION_STAFF2");
				final String investiation_Staff3 = rs.getString("INVESTIGATION_STAFF3");
				final String investiation_Staff4 = rs.getString("INVESTIGATION_STAFF4");
				final String written_Staff = rs.getString("WRITTEN_STAFF");
				final String std_Guid_Mtg_Date = rs.getString("STD_GUID_MTG_DATE");
				final String staff_Mtg_Date = rs.getString("STAFF_MTG_DATE");
				final String original_Plan = rs.getString("ORIGINAL_PLAN");
				final String punish = rs.getString("PUNISH");
				final String diary_Flg = rs.getString("DIARY_FLG");
				final String written_Oath_Flg = rs.getString("WRITTEN_OATH_FLG");
				final String report_Flg = rs.getString("REPORT_FLG");
				final String detail = rs.getString("DETAIL");
				final String content = rs.getString("CONTENT");
				final String remark = rs.getString("REMARK");

				final Student student = new Student(schregno, occurrence_Date, occurrence_Place, grade, hr_Class, attendno,
						hr_Nameabbv, name, investiation_Date, investiation_Staff1, investiation_Staff2, investiation_Staff3, investiation_Staff4,
						written_Staff, std_Guid_Mtg_Date, staff_Mtg_Date, original_Plan, punish, diary_Flg, written_Oath_Flg,
						report_Flg, detail, content, remark);
				retMap.put(String.valueOf(no), student);
				no++;
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

    private class Student {
    	final String _schregno; // 学籍番号
    	final String _occurrence_date; //発生日
    	final String _occurrence_Place; //場所
    	final String _grade; //学年
    	final String _hr_Class; //クラス
    	final String _attendno; //番号
    	final String _hr_Nameabbv; // クラス名
    	final String _name; //氏名
    	final String _investigation_Date; //調査日付
    	final String _investigation_Staff1; //調査職員1
    	final String _investigation_Staff2; //調査職員2
    	final String _investigation_Staff3; //調査職員3
    	final String _investigation_Staff4; //調査職員4
    	final String _written_Staff; //資料作成者
    	final String _std_Guid_Mtg_Date; //生徒指導部会議
    	final String _staff_Mtg_Date; //職員会議
    	final String _original_Plan; //原案
    	final String _punish; //処分
    	final String _diary_Flg; //日誌
    	final String _written_Oath_Flg; //誓約書
    	final String _report_Flg; //調書
    	final String _detail; //内容
    	final String _content; //詳細内容
    	final String _remark; //備考

    	public Student(
				final String schregno, final String occurrence_date, final String occurrence_Place, final String grade,
				final String hr_Class, final String attendno, final String hr_Nameabbv, final String name,final String investigation_Date,final String investigation_Staff1,
				final String investigation_Staff2,final String investigation_Staff3, final String investigation_Staff4, final String written_Staff,
				final String std_Guid_Mtg_Date, final String staff_Mtg_Date, final String original_Plan, final String punish, final String diary_Flg,
				final String written_Oath_Flg, final String report_Flg, final String detail, final String content,
				final String remark) {
    	    _schregno = schregno;
    	    _occurrence_date = occurrence_date;
    	    _occurrence_Place = occurrence_Place;
    	    _grade = grade;
    	    _hr_Class = hr_Class;
    	    _attendno = attendno;
    	    _hr_Nameabbv = hr_Nameabbv;
    	    _name = name;
    	    _investigation_Date = investigation_Date;
    	    _investigation_Staff1 = investigation_Staff1;
    	    _investigation_Staff2 = investigation_Staff2;
    	    _investigation_Staff3 = investigation_Staff3;
    	    _investigation_Staff4 = investigation_Staff4;
    	    _written_Staff = written_Staff;
    	    _std_Guid_Mtg_Date = std_Guid_Mtg_Date;
    	    _staff_Mtg_Date = staff_Mtg_Date;
    	    _original_Plan = original_Plan;
    	    _punish = punish;
    	    _diary_Flg = diary_Flg;
    	    _written_Oath_Flg = written_Oath_Flg;
    	    _report_Flg = report_Flg;
    	    _detail = detail;
    	    _content = content;
    	    _remark = remark;

    	}
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75631 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _date;
        final String _batsuFrom;
        final String _batsuTo;
        final String _semester;
        final String[] _categorySelected;
        Map<String,List<String>> _subclassNameList = new HashMap();
        Map<String,Map<String,String>>  _classList = new HashMap();


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _date = request.getParameter("CTRL_DATE");
            _semester = request.getParameter("CTRL_SEMESTER");
            _batsuFrom = request.getParameter("BATSU_FROM");
            _batsuTo = request.getParameter("BATSU_TO");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");

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
