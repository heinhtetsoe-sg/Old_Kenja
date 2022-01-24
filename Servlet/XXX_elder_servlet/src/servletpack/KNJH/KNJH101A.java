// kanji=漢字
package servletpack.KNJH;

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
import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * 
 * @author nakamoto
 * @version $Id$
 */
public class KNJH101A {

    private static final Log log = LogFactory.getLog("KNJH101A.class");

    private boolean _hasData;

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

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
        final TreeMap studentMap = getStudentMap(db2);
        boolean hasdata = false;
        int Maxline = 25;

        if (studentMap.isEmpty()) {
            return hasdata;
        }


        for (Iterator itr = studentMap.keySet().iterator(); itr.hasNext();) {
            final String key = (String) itr.next();
            final TreeMap printStudentMap = (TreeMap) studentMap.get(key);
            List schPrtList = reBuildSchMap(printStudentMap, Maxline); // 1ページあたりの生徒数

            for (Iterator stuitr = schPrtList.iterator(); stuitr.hasNext();) {
                final Map prtMap = (Map) stuitr.next();
                svf.VrSetForm("KNJH101A.frm", 1);
                svf.VrsOut("TITLE", "生活指導一覧"); // タイトル
                svf.VrsOut("PRINT_DATE", _param._date.replace("-", "/")); // 出力日付
                int line = 1;
                for (Iterator printitr = prtMap.keySet().iterator(); printitr.hasNext();) {
                    final String stukey = (String) printitr.next();
                    final Student student = (Student) prtMap.get(stukey);
                    svf.VrsOutn("DATE", line, student._occurrence_date == null ? "" : student._occurrence_date.replace("-", "/")); // 発生日
                    if (student._occurrence_Place.length() > 10) {
                        svf.VrsOutn("PLACE2_1", line, student._occurrence_Place); // 場所
                    } else {
                        svf.VrsOutn("PLACE1", line, student._occurrence_Place); // 場所
                    }
                    svf.VrsOutn("CONTENT", line, student._detail); // 内容
                    svf.VrsOutn("GRADE", line, student._grade); // 学年
                    svf.VrsOutn("HR_NAME", line, student._hr_Class); // クラス
                    svf.VrsOutn("NO", line, student._attendno); // 番号
                    svf.VrsOutn("NAME", line, student._name); // 氏名
                    svf.VrsOutn("MEETING_DATE1", line, student._std_Guid_Mtg_Date == null ? "" : student._std_Guid_Mtg_Date.replace("-", "/")); // 生徒指導部会議
                    svf.VrsOutn("DRAFT", line, student._original_Plan); // 原案
                    svf.VrsOutn("MEETING_DATE2", line, student._staff_Mtg_Date == null ? "" : student._staff_Mtg_Date.replace("-", "/")); // 職員会議
                    svf.VrsOutn("PUNISH", line, student._punish); // 処分
                    svf.VrsOutn("DIARY", line, "1".equals(StringUtils.defaultString(student._diary_Flg)) ? "完了" : ""); // 日誌
                    svf.VrsOutn("COVENANT", line, "1".equals(StringUtils.defaultString(student._written_Oath_Flg)) ? "完了" : ""); // 誓約書
                    svf.VrsOutn("REPORT", line, "1".equals(StringUtils.defaultString(student._report_Flg)) ? "完了" : ""); // 調書
                    if (student._remark.length() > 30) {
                        svf.VrsOutn("REMARK2_1", line, student._remark); // 備考
                    } else {
                        svf.VrsOutn("REMARK1", line, student._remark); // 備考
                    }

                    hasdata = true;
                    line++;
                }
                svf.VrEndPage();
            }
        }
        return hasdata;
    }

    private List reBuildSchMap(final Map srcMap, final int prtMax) {
        final List retList = new ArrayList();
        Map wkMap = new LinkedMap();
        int cnt = 0;
        for (Iterator ite = srcMap.keySet().iterator(); ite.hasNext();) {
            final String kStr = (String) ite.next();
            final Student mvObj = (Student) srcMap.get(kStr);
            if (cnt >= prtMax) {
                retList.add(wkMap);
                wkMap = new LinkedMap();
                cnt = 0;
            }
            wkMap.put(kStr, mvObj);
            cnt++;
        }
        if (wkMap.size() > 0) {
            retList.add(wkMap);
        }
        return retList;
    }

    private TreeMap getStudentMap(final DB2UDB db2) throws SQLException {
        TreeMap retMap = new TreeMap();
        TreeMap j_Map = new TreeMap();
        TreeMap h_Map = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SCHREGNO, ");
            stb.append("     T1.OCCURRENCE_DATE, ");
            stb.append("     VALUE(T1.OCCURRENCE_PLACE,' ') AS OCCURRENCE_PLACE, ");
            stb.append("     T5.GRADE_NAME1, ");
            stb.append("     T4.HR_CLASS_NAME1, ");
            stb.append("     T2.ATTENDNO, ");
            stb.append("     T5.SCHOOL_KIND, ");
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
            stb.append(" (SELECT ST1.NAME1 FROM NAME_MST ST1 WHERE ST1.NAMECD1 = 'H304' AND ST1.NAMECD2= T1.DETAILCD) AS DETAIL,");
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
            stb.append(" INNER JOIN SCHREG_DETAILHIST_DAT T1 ON T1.YEAR = T2.YEAR AND T1.SCHREGNO = T2.SCHREGNO AND T1.DETAIL_DIV = '2' AND (T1.DETAILCD is null OR T1.DETAILCD BETWEEN '" + _param._batsuFrom + "' AND '" + _param._batsuTo + "') AND T1.OCCURRENCE_DATE BETWEEN '" + _param._sDate + "' AND '" + _param._eDate + "' ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT T5 ON T2.YEAR = T5.YEAR AND T2.GRADE = T5.GRADE  ");
            stb.append(" WHERE ");
            stb.append("     T2.GRADE || T2.HR_CLASS  IN " + SQLUtils.whereIn(true, _param._categorySelected));
            stb.append("     AND T2.YEAR      = '" + _param._year + "' ");
            stb.append("     AND T2.SEMESTER  = '" + _param._semester + "' ");
            stb.append(" ORDER BY  ");
            if ("1".equals(_param._sort)) {
                stb.append("     T5.SCHOOL_KIND, ");
                stb.append("     T1.OCCURRENCE_DATE, ");
                stb.append("     T2.GRADE, ");
                stb.append("     T2.HR_CLASS, ");
                stb.append("     T2.ATTENDNO ");
            } else {
                stb.append("     T5.SCHOOL_KIND, ");
                stb.append("     T2.GRADE, ");
                stb.append("     T2.HR_CLASS, ");
                stb.append("     T2.ATTENDNO, ");
                stb.append("     T1.OCCURRENCE_DATE ");
            }

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
                final String school_Kind = rs.getString("SCHOOL_KIND");
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

                final Student student = new Student(schregno, occurrence_Date, occurrence_Place, grade, hr_Class, attendno, school_Kind, name, investiation_Date, investiation_Staff1, investiation_Staff2, investiation_Staff3, investiation_Staff4, written_Staff, std_Guid_Mtg_Date, staff_Mtg_Date, original_Plan, punish, diary_Flg, written_Oath_Flg, report_Flg, detail, content, remark);

                if ("J".equals(school_Kind)) {
                    j_Map.put(String.valueOf(no), student);
                } else if ("H".equals(school_Kind)) {
                    h_Map.put(String.valueOf(no), student);
                }
                no++;
            }
        } catch (final SQLException e) {
            log.error("生徒の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        // 1:中学 2:高校
        retMap.put("1", j_Map);
        retMap.put("2", h_Map);

        return retMap;
    }

    private class Student {
        final String _schregno; // 学籍番号
        final String _occurrence_date; // 発生日
        final String _occurrence_Place; // 場所
        final String _grade; // 学年
        final String _hr_Class; // クラス
        final String _attendno; // 番号
        final String _hr_Nameabbv; // クラス名
        final String _name; // 氏名
        final String _investigation_Date; // 調査日付
        final String _investigation_Staff1; // 調査職員1
        final String _investigation_Staff2; // 調査職員2
        final String _investigation_Staff3; // 調査職員3
        final String _investigation_Staff4; // 調査職員4
        final String _written_Staff; // 資料作成者
        final String _std_Guid_Mtg_Date; // 生徒指導部会議
        final String _staff_Mtg_Date; // 職員会議
        final String _original_Plan; // 原案
        final String _punish; // 処分
        final String _diary_Flg; // 日誌
        final String _written_Oath_Flg; // 誓約書
        final String _report_Flg; // 調書
        final String _detail; // 内容
        final String _content; // 詳細内容
        final String _remark; // 備考

        public Student(final String schregno, final String occurrence_date, final String occurrence_Place, final String grade, final String hr_Class, final String attendno, final String hr_Nameabbv, final String name, final String investigation_Date, final String investigation_Staff1, final String investigation_Staff2, final String investigation_Staff3, final String investigation_Staff4, final String written_Staff, final String std_Guid_Mtg_Date, final String staff_Mtg_Date,
                final String original_Plan, final String punish, final String diary_Flg, final String written_Oath_Flg, final String report_Flg, final String detail, final String content, final String remark) {
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
        log.fatal("$Revision: 75630 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _date;
        final String _sDate;
        final String _eDate;
        final String _sort; // 1:アルファベット順,2:席次順
        final String _batsuFrom;
        final String _batsuTo;
        final String _semester;
        final String[] _categorySelected;
        Map<String, List<String>> _subclassNameList = new HashMap();
        Map<String, Map<String, String>> _classList = new HashMap();


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _date = request.getParameter("CTRL_DATE");
            _sDate = request.getParameter("SDATE").replace("/", "-");
            _eDate = request.getParameter("EDATE").replace("/", "-");
            _semester = request.getParameter("CTRL_SEMESTER");
            _sort = request.getParameter("DISP");
            _batsuFrom = request.getParameter("BATSU_FROM");
            _batsuTo = request.getParameter("BATSU_TO");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");

        }

    }
}

// eof
