/*
 * $Id: cc2c75879469a76a426195ea35b96e1e1d7b8c0b $
 *
 * 作成日: 2016/07/15
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 文京学園 クラス別朝間結果一覧表
 */
public class KNJD682 {

    private static final Log log = LogFactory.getLog(KNJD682.class);

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
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final Map tmp = HrClass.getHrListMap(db2, _param);
        final List hrList = (List) tmp.get("HR_LIST");
        final List subclassMapList = (List) tmp.get("SUBCLASS_MAP_LIST");

        final int maxSubclass = 5;

        final String form = "J".equals(_param._schoolKind) ? "KNJD682J.frm" : "KNJD682.frm";
        
        for (int hri = 0; hri < hrList.size(); hri++) {
            final HrClass hr = (HrClass) hrList.get(hri);

            svf.VrSetForm(form, 4);
            svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._loginYear)) + "年度　クラス別朝間結果一覧表"); // タイトル
            svf.VrsOut("PAGE", String.valueOf(hri + 1) + "頁"); // ページ
            //svf.VrsOut("SUB_TITLE", null); // サブタイトル
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 
            svf.VrsOut("HR_NAME", hr._hrName); // 年組
            if (null != hr._substaffname) {
                svf.VrsOut("TEACHER_NAME", "担任名：" + StringUtils.defaultString(hr._staffname)); // 担任名
                
                svf.VrsOut("TEACHER_NAME2", "副担任名：" + StringUtils.defaultString(hr._substaffname)); // 担任名
            } else {
                
                svf.VrsOut("TEACHER_NAME2", "担任名：" + StringUtils.defaultString(hr._staffname)); // 担任名
            }

            for (int linei = 0; linei < hr._studentList.size(); linei++) {
                final int line = linei + 1;
                final Student student = (Student) hr._studentList.get(linei);
                
                svf.VrsOutn("NO", line, NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : student._attendno); // 番号
                svf.VrsOutn("NAME", line, student._name); // 氏名
                //svf.VrsOutn("REMARK", line, null); // 合計
            }
            
            for (int semesi = 0; semesi < 2; semesi++) {
                final String semester = String.valueOf(semesi + 1);
                final String semestername = StringUtils.defaultString((String) _param._semesternameMap.get(semester));
                final String[] semesternameArray = getSemesternameArray(semestername, maxSubclass);
                
                for (int coli = 0; coli < maxSubclass; coli++) {
                
                    svf.VrsOut("GRP", semester); // グループコード
                    svf.VrsOut("SELECT_MARK", semesternameArray[coli]); // 選択科目表示
                    if (subclassMapList.size() <= coli) {
                    } else {
                        final Map subclassMap = (Map) subclassMapList.get(coli);
                        final String subclasscd = (String) subclassMap.get("SUBCLASSCD");
                        final String subclassname = (String) subclassMap.get("SUBCLASSNAME");

                        svf.VrsOut("SUBCLASS_NAME", subclassname); // 科目名

                        for (int linei = 0; linei < hr._studentList.size(); linei++) {
                            final int line = linei + 1;
                            final Student student = (Student) hr._studentList.get(linei);
                            final String score = (String) student._subclassScoreMap.get(subclasscd + "|" + semester);
                            
                            if (null != score) {
                                svf.VrsOutn("SCORE", line, score + ":" + StringUtils.defaultString((String) _param._nameMstD060Name1.get(score))); // 素点
                            }
                        }
                    }
                    
                    svf.VrEndRecord();
                    _hasData = true;
                }
            }
        }
    }

    // 均等割り
    private String[] getSemesternameArray(final String semestername, final int maxSubclass) {
        final String[] array = new String[maxSubclass];
        for (int i = 0; i < array.length; i++) {
            array[i] = "　";
        }
        if (null == semestername || semestername.length() == 0) {
            return array;
        }
        if (semestername.length() >= maxSubclass - 1) {
            for (int i = 0; i < Math.min(maxSubclass, semestername.length()); i++) {
                array[i] = String.valueOf(semestername.charAt(i));
            }
            return array;
        }
        final int spaceCount = (maxSubclass - semestername.length()) / (semestername.length() + 1);
        for (int i = spaceCount, j = 0; j < semestername.length(); j++) {
            array[i] = String.valueOf(semestername.charAt(j));
            i += 1 + spaceCount;
        }
        return array;
    }

    private static class Student {
        final String _schregno;
        final String _attendno;
        final String _name;
        final Map _subclassScoreMap = new HashMap();
        
        Student(
                final String schregno,
                final String attendno,
                final String name
            ) {
            _schregno = schregno;
            _attendno = attendno;
            _name = name;
        }
    }

    private static class HrClass {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _staffname;
        final String _substaffname;
        final List _studentList;

        HrClass(
            final String grade,
            final String hrClass,
            final String hrName,
            final String staffname,
            final String substaffname
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _staffname = staffname;
            _substaffname = substaffname;
            _studentList = new ArrayList();
        }

        private static Map getHrListMap(final DB2UDB db2, final Param param) {
            final List hrList = new ArrayList();
            final Map hrMap = new HashMap();
            final Map studentMap = new HashMap();

            final Set subclassCdSet = new HashSet();
            final List subclassMapList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    if (!subclassCdSet.contains(subclasscd)) {
                        final Map subclass = new HashMap();
                        subclass.put("SUBCLASSCD", subclasscd);
                        subclass.put("SUBCLASSNAME", rs.getString("SUBCLASSNAME"));
                        subclassMapList.add(subclass);
                        subclassCdSet.add(subclasscd);
                    }
                    
                    final String schregno = rs.getString("SCHREGNO");
                    if (!studentMap.containsKey(schregno)) {
                        final String grade = rs.getString("GRADE");
                        final String hrClass = rs.getString("HR_CLASS");
                        final String hrKey = grade + "-" + hrClass;
                        if (!hrMap.containsKey(hrKey)) {
                            final HrClass hr = new HrClass(grade, hrClass, rs.getString("HR_NAME"), rs.getString("STAFFNAME"), rs.getString("SUBSTAFFNAME"));
                            hrList.add(hr);
                            hrMap.put(hrKey, hr);
                        }
                        final HrClass hr = (HrClass) hrMap.get(hrKey);
                        
                        final Student student = new Student(schregno, rs.getString("ATTENDNO"), rs.getString("NAME"));
                        hr._studentList.add(student);
                        studentMap.put(schregno, student);
                    }
                    final Student student = (Student) studentMap.get(schregno);
                    final String semester = rs.getString("SEMESTER");
                    final String value = rs.getString("VALUE");
                    student._subclassScoreMap.put(subclasscd + "|" + semester, value);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            final Map rtn = new HashMap();
            rtn.put("HR_LIST", hrList);
            rtn.put("SUBCLASS_MAP_LIST", subclassMapList);
            return rtn;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("   T2.SUBCLASSNAME, ");
            stb.append("   REGD.GRADE, ");
            stb.append("   REGD.HR_CLASS, ");
            stb.append("   REGDH.HR_NAME, ");
            stb.append("   STF.STAFFNAME, ");
            stb.append("   SUBSTF.STAFFNAME AS SUBSTAFFNAME, ");
            stb.append("   REGD.SCHREGNO, ");
            stb.append("   REGD.ATTENDNO, ");
            stb.append("   BASE.NAME, ");
            stb.append("   TSCORE.SEMESTER, ");
            stb.append("   TSCORE.VALUE ");
            stb.append(" FROM V_NAME_MST T1 ");
            if ("J".equals(param._schoolKind)) {
                stb.append(" INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = T1.NAME1  ");
            } else {
                stb.append(" INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = T1.NAME2  ");
            }
            stb.append(" INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '" + param._loginYear + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND REGD.GRADE || '-' || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append(" INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("     AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("     AND REGDH.GRADE = REGD.GRADE ");
            stb.append("     AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" LEFT JOIN SPECIALACT_SCORE_DAT TSCORE ON TSCORE.YEAR = '" + param._loginYear + "' ");
            stb.append("     AND TSCORE.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND TSCORE.CLASSCD = T2.CLASSCD ");
            stb.append("     AND TSCORE.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("     AND TSCORE.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("     AND TSCORE.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("     AND TSCORE.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" LEFT JOIN STAFF_MST STF ON STF.STAFFCD = REGDH.TR_CD1 ");
            stb.append(" LEFT JOIN STAFF_MST SUBSTF ON SUBSTF.STAFFCD = REGDH.SUBTR_CD1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._loginYear + "' ");
            stb.append("     AND T1.NAMECD1 LIKE 'D061' ");
            stb.append(" ORDER BY ");
            stb.append("   REGD.GRADE, ");
            stb.append("   REGD.HR_CLASS, ");
            stb.append("   REGD.ATTENDNO, ");
            stb.append("   T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _loginYear;
        final String _loginDate;
        final String _semester;
        final String _grade;
        final String[] _categorySelected;
        final String _schoolKind;
        final Map _nameMstD060Name1;
        final Map _semesternameMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginDate = request.getParameter("LOGIN_DATE");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _schoolKind = getSchoolKind(db2);
            _nameMstD060Name1 = getNameMstD060(db2);
            _semesternameMap = getSemesternameMap(db2);
        }
        
        private Map getNameMstD060(final DB2UDB db2) throws SQLException {
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR ='" + _loginYear + "' AND NAMECD1 = 'D060' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
                }
            } catch (final SQLException e) {
                log.error("名称マスタD060取得エラー。");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
        
        private String getSchoolKind(final DB2UDB db2) throws SQLException {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT school_kind FROM schreg_regd_gdat WHERE year = '" + _loginYear + "' AND grade = '" + _grade + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("school_Kind");
                }
            } catch (final SQLException e) {
                log.error("学校種別取得エラー。");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
        
        private Map getSemesternameMap(final DB2UDB db2) throws SQLException {
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT SEMESTER, SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _loginYear + "' AND SEMESTER <> '9' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"));
                }
            } catch (final SQLException e) {
                log.error("学期取得エラー。");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
    }
}

// eof

