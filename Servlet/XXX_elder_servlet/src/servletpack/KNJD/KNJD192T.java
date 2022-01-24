/*
 * $Id: 6c821153977e2d3ced49bb993b57f802e8673305 $
 *
 * 作成日: 2018/06/20
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 駿台甲府中学　再試通知
 */
public class KNJD192T {

    private static final Log log = LogFactory.getLog(KNJD192T.class);

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
        final String form = "KNJD192T.frm";

        for (int pi = 0; pi < studentList.size(); pi++) {
            final Student student = (Student) studentList.get(pi);

            boolean hasSaishi = false;
            for (int i = 0; i < student._subclassList.size(); i++) {
                final Map subclassMap = (Map) student._subclassList.get(i);
                if (isSaishi(subclassMap, _param) || isSaishiKesseki(subclassMap, _param)) {
                    hasSaishi = true;
                }
            }
            if (!hasSaishi) {
                // 再試がなければ表示しない
                continue;
            }

            svf.VrSetForm(form, 1);
            svf.VrsOut("DATE", _param.getKisaiDate());
            final String attendno = (NumberUtils.isDigits(student._attendno) ? Integer.valueOf(student._attendno).toString() : StringUtils.defaultString(student._attendno)) + "番";
            svf.VrsOut("HR_NAME2", StringUtils.defaultString(student._hrname) + attendno); // 年組番
            svf.VrsOut("NAME" + (getMS932ByteLength(student._name) <= 24 ? "1" : "2"), student._name); // 氏名

            for (int j = 0; j < student._subclassList.size(); j++) {
                final Map subclassMap = (Map) student._subclassList.get(j);
                final String subclassname = (String) subclassMap.get("SUBCLASSNAME");
                final String score = (String) subclassMap.get("SCORE");
                final String scoreDi = (String) subclassMap.get("SCORE_DI");
                final int line = j + 1;
                svf.VrsOutn("CLASS_NAME" + (getMS932ByteLength(subclassname) <= 6 ? "1" : getMS932ByteLength(subclassname) <= 8 ? "2" : "3"), line, subclassname); // 科目
                svf.VrsOutn("SCORE", line, isSaishiKesseki(subclassMap, _param) ? scoreDi : isSaishi(subclassMap, _param) ? score : "-"); // 点数
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }
    
    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes( "MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    // 科目が再試か
    private static boolean isSaishi(final Map subclassMap, final Param param) {
        final String score = (String) subclassMap.get("SCORE");
        return NumberUtils.isDigits(score);
    }

    // 科目が再試欠席か
    private static boolean isSaishiKesseki(final Map subclassMap, final Param param) {
        final String scoreDi = (String) subclassMap.get("SCORE_DI");
        return "*".equals(scoreDi);
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
                    
                    final boolean is5ka = "1".equals(rs.getString("IS_5KA"));
                    if (!is5ka) {
                        //log.info("5科目以外は除外:" + rs.getString("SUBCLASSCD"));
                        continue;
                    }

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
            stb.append("     SCORE.SCHREGNO ");
            stb.append("   , REGD.GRADE ");
            stb.append("   , REGD.HR_CLASS ");
            stb.append("   , REGDH.HR_NAME ");
            stb.append("   , REGD.ATTENDNO ");
            stb.append("   , BASE.NAME ");
            stb.append("   , SCORE.CLASSCD || '-' || SCORE.SCHOOL_KIND || '-' || SCORE.CURRICULUM_CD || '-' || SCORE.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("   , SUBM.SUBCLASSNAME ");
            stb.append("   , SUPP.SCORE ");
            stb.append("   , SUPP.SCORE_DI ");
            stb.append("   , CASE WHEN SUBGRP.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS IS_5KA ");
            stb.append(" FROM RECORD_SCORE_DAT SCORE ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = SCORE.YEAR ");
            stb.append("     AND REGD.SEMESTER = '" + ("9".equals(param._semester) ? param._ctrlSeme : param._semester) + "' ");
            stb.append("     AND REGD.SCHREGNO = SCORE.SCHREGNO ");
            stb.append(" INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = SCORE.YEAR ");
            stb.append("     AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("     AND REGDH.GRADE = REGD.GRADE ");
            stb.append("     AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = SCORE.SCHREGNO ");
            stb.append(" INNER JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = SCORE.CLASSCD ");
            stb.append("     AND SUBM.SCHOOL_KIND = SCORE.SCHOOL_KIND ");
            stb.append("     AND SUBM.CURRICULUM_CD = SCORE.CURRICULUM_CD ");
            stb.append("     AND SUBM.SUBCLASSCD = SCORE.SUBCLASSCD ");
            stb.append(" LEFT JOIN SUPP_EXA_SDIV_DAT SUPP ON SUPP.YEAR = SCORE.YEAR ");
            stb.append("       AND SUPP.SEMESTER = SCORE.SEMESTER ");
            stb.append("       AND SUPP.TESTKINDCD = SCORE.TESTKINDCD ");
            stb.append("       AND SUPP.TESTITEMCD = SCORE.TESTITEMCD ");
            stb.append("       AND SUPP.SCORE_DIV = SCORE.SCORE_DIV ");
            stb.append("       AND SUPP.CLASSCD = SCORE.CLASSCD ");
            stb.append("       AND SUPP.SCHOOL_KIND = SCORE.SCHOOL_KIND ");
            stb.append("       AND SUPP.CURRICULUM_CD = SCORE.CURRICULUM_CD ");
            stb.append("       AND SUPP.SUBCLASSCD = SCORE.SUBCLASSCD ");
            stb.append("       AND SUPP.SCHREGNO = SCORE.SCHREGNO ");
            stb.append(" LEFT JOIN REC_SUBCLASS_GROUP_DAT SUBGRP ON SUBGRP.YEAR = SCORE.YEAR ");
            stb.append("       AND SUBGRP.GROUP_DIV = '5' ");
            stb.append("       AND SUBGRP.GRADE = REGD.GRADE ");
            stb.append("       AND SUBGRP.COURSECD = REGD.COURSECD ");
            stb.append("       AND SUBGRP.MAJORCD = REGD.MAJORCD ");
            stb.append("       AND SUBGRP.COURSECODE = REGD.COURSECODE ");
            stb.append("       AND SUBGRP.CLASSCD = SCORE.CLASSCD ");
            stb.append("       AND SUBGRP.SCHOOL_KIND = SCORE.SCHOOL_KIND ");
            stb.append("       AND SUBGRP.CURRICULUM_CD = SCORE.CURRICULUM_CD ");
            stb.append("       AND SUBGRP.SUBCLASSCD = SCORE.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append(" SCORE.YEAR = '" + param._year + "' ");
            stb.append(" AND SCORE.SEMESTER = '" + param._semester + "' ");
            stb.append(" AND SCORE.SEMESTER || SCORE.TESTKINDCD || SCORE.TESTITEMCD || SCORE.SCORE_DIV = '" + param._subTestcd + "' ");
            stb.append(" AND REGD.GRADE = '" + param._grade + "' ");
            if ("2".equals(param._categoryIsClass)) {
                stb.append(" AND REGD.HR_CLASS = '" + param._hrClass + "' ");
                stb.append(" AND SCORE.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            } else {
                stb.append(" AND REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            }
            stb.append(" AND REGD.COURSECD || '-' || REGD.MAJORCD = '" + param._major + "' ");
            stb.append(" ORDER BY REGD.GRADE ");
            stb.append("     , REGD.HR_CLASS ");
            stb.append("     , REGD.ATTENDNO ");
            stb.append("     , SCORE.CLASSCD || '-' || SCORE.SCHOOL_KIND || '-' || SCORE.CURRICULUM_CD || '-' || SCORE.SUBCLASSCD ");
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
        final String _major;
        final String _categoryIsClass;
        final String[] _categorySelected;
        final String _subTestcd;
        final String _kisaiDate;
        final boolean _isSeireki;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _major = request.getParameter("MAJOR");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _subTestcd = request.getParameter("SUB_TESTCD");
            _kisaiDate = request.getParameter("KISAI_DATE");
            _isSeireki = "2".equals(getNameMst(db2, "Z012", "01"));
        }

        private String getNameMst(final DB2UDB db2, final String namecd1, final String namecd2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT * FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String getKisaiDate() {
            if (_isSeireki) {
                return _year + "年" + KNJ_EditDate.h_format_JP_MD(_kisaiDate);
            } else {
                return KenjaProperties.gengou(Integer.parseInt(_year)) + "年" + KNJ_EditDate.h_format_JP_MD(_kisaiDate);
            }
        }

    }
}

// eof

