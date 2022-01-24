/*
 * $Id: f5fa0986c783f20cbd122016599144b9fe6ab3c6 $
 *
 * 作成日: 2018/09/11
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * 学校教育システム 賢者 [成績管理] 小学通知票
 */

public class KNJE152A {

    private static final Log log = LogFactory.getLog(KNJE152A.class);
    private static final String COMMSYSSCHOOL = "1";
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

        final List studentList = Student.getStudentList(db2, _param);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.debug(" student = " + student._schregno + " : " + student._name);

            if (!NumberUtils.isNumber((String) _param._gradeCdStrMap.get(student._grade))) {
                return;
            }
            final String form= "KNJE152A.frm";
            svf.VrSetForm(form, 4);

            final int maxLine = 50;

            setTitle(db2, svf, student);

            printSvfMainSeiseki(db2, svf, maxLine, student);
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final Student student) {
    	if (COMMSYSSCHOOL.equals(_param._commsysschoolflg)) {
    		//通信制
        	svf.VrsOut("TITLE", "成績レポート");
        	svf.VrsOut("HR_CLASS", "学籍番号：" + student._schregno);
        	svf.VrsOut("TEACHER_NAME", "チューター：" + student._staffName);
    	} else {
        	svf.VrsOut("TITLE", "調査書成績データ");
        	svf.VrsOut("HR_CLASS", "年組番：" + student._hrName + " "+ String.valueOf(Integer.parseInt(student._attendNo)) + "番");
        	svf.VrsOut("TEACHER_NAME","担任：" + student._staffName);
    	}
    	int namelen = KNJ_EditEdit.getMS932ByteLength(student._name);
    	final String namefield = namelen > 30 ? "3" : namelen > 20 ? "2" : "";
    	svf.VrsOut("NAME" + namefield, student._name);
    	svf.VrsOut("ENT_YEAR", student._curriculumYear);
    	svf.VrsOut("DATE", _param._date);
    }

    /**
     * 学習の記録を印字する
     * @param svf
     * @param student
     */
    private void printSvfMainSeiseki(final DB2UDB db2, final Vrw32alp svf, final int maxLine, final Student student) {

        int line = 0; // 行数
        int sum_tani = 0;
        int reccnt = 0;
        int sum_hyotei = 0;
        for (final Iterator it = student._stdRecInfoList.iterator(); it.hasNext();) {
            final StdRecInfo stdRedInfo = (StdRecInfo) it.next();

            svf.VrsOut("CLASS1", stdRedInfo._c_Cd);
            final String subField1 = KNJ_EditEdit.getMS932ByteLength(stdRedInfo._subclassName1) > 28 ? "_2" : "";
            svf.VrsOut("SUBCLASS1" + subField1, stdRedInfo._subclassName1);
            final String subField2 = KNJ_EditEdit.getMS932ByteLength(stdRedInfo._subclassName2) > 28 ? "_2" : "";
            svf.VrsOut("SUBCLASS2" + subField2, stdRedInfo._subclassName2);
            svf.VrsOut("SELECT_DIV", stdRedInfo._courseDivName);
            svf.VrsOut("GET_YEAR", stdRedInfo._courseYear);
            svf.VrsOut("VALUE1", stdRedInfo._valuation);
            svf.VrsOut("F_CREDIT1", stdRedInfo._credit);
            if (stdRedInfo._valuation != null && !"".equals(stdRedInfo._valuation)) {
                sum_hyotei += Integer.parseInt(StringUtils.defaultString(stdRedInfo._valuation, "0"));
                reccnt++;
            }
            sum_tani += Integer.parseInt(stdRedInfo._credit);
            svf.VrEndRecord();
            line++;

            _hasData = true;
        }
        if (reccnt > 0) {
            BigDecimal setval = new BigDecimal(0.0);
            final BigDecimal div_bunsi = new BigDecimal(sum_hyotei);
            final BigDecimal div_bonbo = new BigDecimal(reccnt);
            setval = div_bunsi.divide(div_bonbo, 1, BigDecimal.ROUND_HALF_UP);
            svf.VrsOut("AVERAGE_VALUE1", String.valueOf(setval));
            svf.VrsOut("TOTAL_F_CREDIT1", String.valueOf(sum_tani));
            svf.VrEndRecord();
        }
    }

    private static class Student {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _attendNo;
        final String _schregno;
        final String _name;
        final String _curriculumYear;
        final String _staffName;
        List _stdRecInfoList = Collections.EMPTY_LIST; // 生活・特別活動のようす

        public Student(final String grade, final String hrClass, final String hrName, final String attendNo, final String schregno, final String name, final String curriculumYear, final String staffName) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendNo = attendNo;
            _schregno = schregno;
            _name = name;
            _curriculumYear = curriculumYear;
            _staffName = staffName;
        }

        private static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentSql(param);
                log.info(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                	final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String attendno = rs.getString("ATTENDNO");
                    final String schregno = rs.getString("SCHREGNO");
                    final String name =  rs.getString("NAME");
                    final String curriculumyear = rs.getString("CURRICULUM_YEAR");
                    final String staffname = rs.getString("STAFFNAME");
                    final Student student = new Student(grade, hrClass, hrName, attendno, schregno, name, curriculumyear, staffname);
                    studentList.add(student);
                }

            } catch (SQLException e) {
                log.error("exception!!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            StdRecInfo.setStdRecInfoList(db2, param, studentList);

            return studentList;
        }

        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T4.GRADE, ");
            stb.append("   T7.GRADE_CD, ");
            stb.append("   T4.HR_CLASS, ");
            stb.append("   T6.HR_NAME, ");
            stb.append("   T4.ATTENDNO, ");
            stb.append("   T4.SCHREGNO, ");
            stb.append("   T8.NAME, ");
            stb.append("   T9.CURRICULUM_YEAR, ");
            stb.append("   T10.STAFFNAME ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT T4 ");
            stb.append("   LEFT JOIN SCHREG_REGD_HDAT T6 ");
            stb.append("      ON T6.YEAR = T4.YEAR ");
            stb.append("     AND T6.SEMESTER = T4.SEMESTER ");
            stb.append("     AND T6.GRADE = T4.GRADE ");
            stb.append("     AND T6.HR_CLASS = T4.HR_CLASS ");
            stb.append("   LEFT JOIN SCHREG_REGD_GDAT T7 ");
            stb.append("      ON T7.YEAR = T4.YEAR ");
            stb.append("     AND T7.GRADE = T4.GRADE ");
            stb.append("   LEFT JOIN SCHREG_BASE_MST T8 ");
            stb.append("      ON T8.SCHREGNO = T4.SCHREGNO ");
            stb.append("   LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T9 ");
            stb.append("      ON T9.SCHREGNO = T4.SCHREGNO ");
            stb.append("     AND T9.SCHOOL_KIND = T7.SCHOOL_KIND ");
            stb.append("   LEFT JOIN STAFF_MST T10 ");
            stb.append("      ON T10.STAFFCD = T6.TR_CD1");
            stb.append(" WHERE ");
            stb.append("   T4.YEAR = '" + param._year + "' ");
            stb.append("   AND T4.SEMESTER = '" + param._semester + "' ");
            if ("1".equals(param._use_prg_schoolkind)) {
                stb.append("   AND T7.SCHOOL_KIND = '" + param._schoolkind + "' ");
            } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._schoolkind)) {
                stb.append("   AND T7.SCHOOL_KIND = '" + param._schoolkind + "' ");
            }
            stb.append("   AND T4.SCHREGNO IN "+ SQLUtils.whereIn(true, param._categorySelected) +" ");
            stb.append(" ORDER BY ");
            stb.append("   T4.SCHREGNO ");
            return stb.toString();
        }
    }

    /**
     * 学校生活のようす
     */
    private static class StdRecInfo {

    	final String _schregno;
        final String _classCd;
        final String _c_Cd;
        final String _subclassName1;
        final String _subclassName2;
        final String _courseDivName;
        final String _courseYear;
        final String _valuation;
        final String _credit;

        public StdRecInfo(
            	final String schregno,
                final String classCd,
                final String c_Cd,
                final String subclassName1,
                final String subclassName2,
                final String courseDivName,
                final String courseYear,
                final String valuation,
                final String credit) {
            _schregno =schregno;
            _classCd = classCd;
            _c_Cd = c_Cd;
            _subclassName1 = subclassName1;
            _subclassName2 = subclassName2;
            _courseDivName = courseDivName;
            _courseYear = courseYear;
            _valuation = valuation;
            _credit = credit;
        }

        public static void setStdRecInfoList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getBehaviorSemesDatSql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._stdRecInfoList = new ArrayList();
                    //ps.setString(1, student._grade);
                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String schregno = rs.getString("SCHREGNO");
                        final String classcd = rs.getString("CLASSCD");
                        final String c_cd = rs.getString("C_CD");
                        final String subclassName1 = rs.getString("SUBCLASSNAME1");
                        final String subclassName2 = rs.getString("SUBCLASSNAME2");
                        final String coursedivname = rs.getString("COURSEDIVNAME");
                        final String courseyear = rs.getString("YEAR");
                        final String valuation = rs.getString("VALUATION");
                        final String credit = rs.getString("CREDIT");

                        final StdRecInfo stdRecInf = new StdRecInfo(schregno, classcd, c_cd, subclassName1, subclassName2, coursedivname,
                        		                                      courseyear, valuation, credit);

                        student._stdRecInfoList.add(stdRecInf);
                    }

                    DbUtils.closeQuietly(rs);
                }

            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getBehaviorSemesDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append(" T1.SCHREGNO, ");
            stb.append(" T1.CLASSCD, ");
            stb.append(" T1.SCHOOL_KIND, ");
            stb.append(" T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS C_CD, ");
            stb.append(" T3.SUBCLASSNAME AS SUBCLASSNAME1, ");
            stb.append(" CASE WHEN T3.SUBCLASSORDERNAME1 IS NOT NULL THEN T3.SUBCLASSORDERNAME1 ELSE T3.SUBCLASSNAME END AS SUBCLASSNAME2, ");
            stb.append(" NMZ011.NAME1 AS COURSEDIVNAME, ");
            stb.append(" T1.YEAR, ");
            stb.append(" T1.VALUATION, ");
            stb.append(" (VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS CREDIT ");
            stb.append(" FROM ");
            stb.append(" SCHREG_STUDYREC_DAT T1 ");
            stb.append(" LEFT JOIN CLASS_MST T2 ");
            stb.append("    ON T2.CLASSCD = T1.CLASSCD ");
            stb.append("   AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ");
            stb.append("    ON T3.CLASSCD = T1.CLASSCD ");
            stb.append("   AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("   AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("   AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" LEFT JOIN SCHREG_REGD_DAT T4 ");
            stb.append("    ON T4.YEAR = '" + param._year + "' ");
            stb.append("   AND T4.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN CREDIT_MST T5 ");
            stb.append("    ON T5.YEAR = T4.YEAR ");
            stb.append("   AND T5.GRADE = T4.GRADE ");
            stb.append("   AND T5.COURSECD = T4.COURSECD ");
            stb.append("   AND T5.MAJORCD = T4.MAJORCD ");
            stb.append("   AND T5.COURSECODE = T4.COURSECODE ");
            stb.append("   AND T5.CLASSCD = T3.CLASSCD ");
            stb.append("   AND T5.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("   AND T5.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("   AND T5.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append(" LEFT JOIN NAME_MST NMZ011 ");
            stb.append("     ON NMZ011.NAMECD1 = 'Z011' ");
            stb.append("    AND NMZ011.NAMECD2 = T5.REQUIRE_FLG ");
            stb.append(" WHERE ");
            if ("1".equals(param._use_prg_schoolkind)) {
                stb.append("   T1.SCHOOL_KIND = '" + param._schoolkind + "' ");
            } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._schoolkind)) {
                stb.append("   T1.SCHOOL_KIND = '" + param._schoolkind + "' ");
            }
            stb.append("   AND T1.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("   T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.YEAR ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 62317 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _date;
        final String _gradeHrclass;
        final String[] _categorySelected;

        final String _schoolkind;
        final String _use_prg_schoolkind;
        final String _useSchool_KindField;
        final String _commsysschoolflg;  //通信制判定フラグ

       /** 各学校における定数等設定 */
        KNJSchoolMst _knjSchoolMst;

        final Map _gradeCdStrMap;

        private Map _semesterMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("category_selected");

            _gradeCdStrMap = getGradeCdMap(db2);

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            _semesterMap = getNameMst(db2, _year, "DP78", "", "NAME1");
            _schoolkind = request.getParameter("SCHOOL_KIND");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _commsysschoolflg = getStrNameMst(db2, _year, "Z010", "00", "NAMESPARE3");
        }

    	//検索結果の先頭1行だけを取得
        private String getStrNameMst(final DB2UDB db2, final String year, final String namecd1, final String namecd2, final String field) {
        	Map getmap = getNameMst(db2, year, namecd1, namecd2, field);
        	String retstr = "";
        	for (final Iterator it = getmap.keySet().iterator(); it.hasNext();) {
        		final String keystr = (String)it.next();
        		retstr = (String)getmap.get(keystr);
        		break;
        	}
        	return retstr;

        }
        private Map getNameMst(final DB2UDB db2, final String year, final String namecd1, final String namecd2, final String field) {
            Map rtnmap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("    T1.NAMECD2,  ");
                sql.append("    T1." + field + " ");
                sql.append(" FROM NAME_MST T1 ");
                sql.append("    INNER JOIN NAME_YDAT T2 ON T2.YEAR = '" + year + "' AND T2.NAMECD1 = T1.NAMECD1 AND T2.NAMECD2 = T1.NAMECD2 ");
                sql.append(" WHERE ");
                sql.append("    T1.NAMECD1 = '" + namecd1 + "' ");
                if (!"".equals(namecd2)) {
                    sql.append("    AND T1.NAMECD2 = '" + namecd2 + "' ");
                }
                sql.append("   ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (null != rs.getString(field)) {
                    	rtnmap.put(rs.getString("NAMECD2"), rs.getString(field));
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnmap;
        }

        private Map getGradeCdMap(final DB2UDB db2) {
            Map gradeCdMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_GDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _year + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String tmp = rs.getString("GRADE_CD");
                    if (NumberUtils.isDigits(tmp)) {
                    	gradeCdMap.put(rs.getString("GRADE"), String.valueOf(Integer.parseInt(tmp)));
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradeCdMap;
        }
    }
}

// eof

