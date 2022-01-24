/*
 * $Id: 65fbbf5188f7e659babb671bebdd025a6041de89 $
 *
 * 作成日: 2016/12/09
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * 駿台甲府中学　再試結果
 */
public class KNJD192R {

    private static final Log log = LogFactory.getLog(KNJD192R.class);

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

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final int maxLine = 5;
        final List studentListAll = Student.getStudentList(db2, _param);
        final List pageList = getPageList(studentListAll, maxLine);
        final String form = "KNJD192R.frm";
        
        for (int pi = 0; pi < pageList.size(); pi++) {

            final List dataList = (List) pageList.get(pi);
            svf.VrSetForm(form, 1);

            for (int j = 0; j < dataList.size(); j++) {
                final Student student = (Student) dataList.get(j);
                final int line = j + 1;
                
                final String attendno = (NumberUtils.isDigits(student._attendno) ? Integer.valueOf(student._attendno).toString() : StringUtils.defaultString(student._attendno)) + "番"; 

                svf.VrsOutn("NENDO", line, KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度"); // 年度
                svf.VrsOutn("SEMESTER", line, _param._semestername); // 学期
                svf.VrsOutn("TESTNAME", line, _param._testitemname); // テスト名
                svf.VrsOutn("HR_NAME", line, StringUtils.defaultString(student._hrname) + attendno); // 年組版
                svf.VrsOutn("NAME", line, student._name); // 生徒氏名
                for (int si = 0; si < student._subclassList.size(); si++) {
                    final Map subclassMap = (Map) student._subclassList.get(si);
                    final String score = (String) subclassMap.get("SCORE");
                    final String scoreDi = (String) subclassMap.get("SCORE_DI");
                    final String ssi = String.valueOf(si + 1);
                    svf.VrsOutn("SCORE" + ssi, line, "*".equals(scoreDi) ? scoreDi : score); // 得点
                    svf.VrsOutn("SUBCLASS" + ssi, line, (String) subclassMap.get("SUBCLASSNAME")); // 科目
                }
            }
            svf.VrEndPage();
            _hasData = true;
        }
        
    }
    
    private static class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrname;
        final String _attendno;
        final String _name;
        final List _subclassList;

        Student(
            final String schregno,
            final String grade,
            final String hrClass,
            final String hrname,
            final String attendno,
            final String name
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrname = hrname;
            _attendno = attendno;
            _name = name;
            _subclassList = new ArrayList();
        }

        public static List getStudentList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            final Map studentMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    if (null == studentMap.get(schregno)) {
                        final String grade = rs.getString("GRADE");
                        final String hrClass = rs.getString("HR_CLASS");
                        final String hrname = rs.getString("HR_NAME");
                        final String attendno = rs.getString("ATTENDNO");
                        final String name = rs.getString("NAME");
                        final Student student = new Student(schregno, grade, hrClass, hrname, attendno, name);
                        list.add(student);
                        studentMap.put(schregno, student);
                    }
                    
                    final Student student = (Student) studentMap.get(schregno);
                    
                    final Map subclassMap = new HashMap();
                    student._subclassList.add(subclassMap);
                    subclassMap.put("SUBCLASSCD", rs.getString("SUBCLASSCD"));
                    subclassMap.put("SUBCLASSNAME", rs.getString("SUBCLASSNAME"));
                    subclassMap.put("SCORE", rs.getString("SCORE"));
                    subclassMap.put("SCORE_DI", rs.getString("SCORE_DI"));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO ");
            stb.append("   , REGD.GRADE ");
            stb.append("   , REGD.HR_CLASS ");
            stb.append("   , REGDH.HR_NAME ");
            stb.append("   , REGD.ATTENDNO ");
            stb.append("   , BASE.NAME ");
            stb.append("   , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("   , T3.SUBCLASSNAME ");
            stb.append("   , T1.SCORE ");
            stb.append("   , T1.SCORE_DI ");
            stb.append(" FROM SUPP_EXA_SDIV_DAT T1 ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR ");
            stb.append("     AND REGD.SEMESTER = '" + ("9".equals(param._semester) ? param._ctrlSeme : param._semester) + "' ");
            stb.append("     AND REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append(" INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = T1.YEAR ");
            stb.append("     AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("     AND REGDH.GRADE = REGD.GRADE ");
            stb.append("     AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            stb.append(" INNER JOIN SUBCLASS_MST T3 ON T3.CLASSCD = T1.CLASSCD ");
            stb.append("     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("     AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append(" T1.YEAR = '" + param._year + "' ");
            stb.append(" AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append(" AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + param._subTestcd + "' ");
            stb.append(" AND REGD.GRADE = '" + param._grade + "' ");
            if ("2".equals(param._categoryIsClass)) {
                stb.append(" AND REGD.HR_CLASS = '" + param._hrClass + "' ");
                stb.append(" AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            } else {
                stb.append(" AND REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            }
            stb.append(" ORDER BY REGD.GRADE ");
            stb.append("     , REGD.HR_CLASS ");
            stb.append("     , REGD.ATTENDNO ");
            stb.append("     , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 62833 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSeme;
        final String _grade;
        final String _hrClass;
        final String _categoryIsClass;
        final String[] _categorySelected;
        final String _subTestcd;
        final String _semestername;
        final String _testitemname;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _subTestcd = request.getParameter("SUB_TESTCD");
            _semestername = getSemestername(db2);
            _testitemname = getTestname(db2);
        }
        
        private String getSemestername(final DB2UDB db2) {
            final String sql = " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SEMESTERNAME");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
            return rtn;
        }
        
        private String getTestname(final DB2UDB db2) {
            String sql = " SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV WHERE YEAR = '" + _year + "' AND SEMESTER || TESTKINDCD || TESTITEMCd || SCORE_DIV = '" + _subTestcd + "' ";
            sql += " ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTITEMNAME");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
            return rtn;
        }
    }
}

// eof

