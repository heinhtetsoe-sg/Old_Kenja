/*
 * $Id: 6a8b0ae6bf59a1108f59c30959afe1a3d8e6493e $
 *
 * 作成日: 2015/08/05
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 熊本県 評価・評定のミスマッチ検出機能（一覧表）
 */
public class KNJD629 {

    private static final Log log = LogFactory.getLog(KNJD629.class);

    private boolean _hasData;

    private static String TESTCD = "9990000"; //評価・評定のテストコード

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

    private int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private List getPageList(final List list, final int max) {
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

        final int MAX_STUDENT = 50;

        final List studentAllList = getStudentList(db2);

        String hrKeep = "";
        List hrList = new ArrayList();
        List curentHrList = new ArrayList();
        for (final Iterator it = studentAllList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final String grade_hr_class = student._grade + student._hrClass;
            if (!hrKeep.equals(grade_hr_class)) {
                curentHrList = new ArrayList();
                hrList.add(curentHrList);
                hrKeep = grade_hr_class;
            }
            curentHrList.add(student);
        }

        for (final Iterator hit = hrList.iterator(); hit.hasNext();) {
            final List studentHrList = (List) hit.next();
            final List studentPageList = getPageList(studentHrList, MAX_STUDENT);

            for (final Iterator pit = studentPageList.iterator(); pit.hasNext();) {
                final List studentList = (List) pit.next();

                final String form = "KNJD629.frm";
                svf.VrSetForm(form, 4);

                for (final Iterator sit = studentList.iterator(); sit.hasNext();) {
                    final Student student = (Student) sit.next();

                    log.debug(" schregno = " + student._schregno);

                    svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　評価・評定相違一覧"); // タイトル
                    svf.VrsOut("PRINT_DATE", "作成日：" + KNJ_EditDate.h_format_JP(_param._loginDate)); //
                    svf.VrsOut("HR_NAME", student._hrName); // 年組

                    svf.VrsOut("ATTEND_NO", NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : student._attendno); // 番号
                    if (getMS932ByteLength(student._name) > 30) {
                        svf.VrsOut("NAME3", student._name); // 氏名
                    } else if (getMS932ByteLength(student._name) > 20) {
                        svf.VrsOut("NAME2", student._name); // 氏名
                    } else {
                        svf.VrsOut("NAME1", student._name); // 氏名
                    }
                    svf.VrsOut("SUBCLASS_NAME1", student._subclassname); // 科目
                    svf.VrsOut("VALUE1", student._score); // 評価
                    svf.VrsOut("CONVERT", student._assesslevel); // 換算値
                    svf.VrsOut("VALUE2", student._value); // 評定
                    svf.VrsOut("PREVALUE", "1".equals(student._provFlg) ? "レ" : ""); // 仮評定フラグ

                    svf.VrEndRecord();
                }
                _hasData = true;
            }
        }
    }

    private String sqlMismatch() {
        final StringBuffer stb = new StringBuffer();
        final String assesslevel = "1".equals(_param._useAssessCourseMst) || "1".equals(_param._useAssessSubclassMst) ? "VALUE(L2.ASSESSLEVEL, L1.ASSESSLEVEL)" : "L1.ASSESSLEVEL";
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T2.HR_NAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T3.NAME, ");
        stb.append("     R1.CLASSCD||'-'||R1.SCHOOL_KIND||'-'||R1.CURRICULUM_CD||'-'||R1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     S1.SUBCLASSNAME, ");
        stb.append("     R1.SCORE, "); //評価（学年成績：１００段階）
        stb.append("     R1.VALUE, "); //評定（学年評定：５段階）
        stb.append("     " + assesslevel + " AS ASSESSLEVEL, "); //評定（学年成績から５段階に換算）
        stb.append("     P1.PROV_FLG "); //仮評定フラグ
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("             AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("             AND T2.GRADE = T1.GRADE ");
        stb.append("             AND T2.HR_CLASS = T1.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN RECORD_SCORE_DAT R1 ON R1.YEAR = T1.YEAR ");
        stb.append("             AND R1.SEMESTER||R1.TESTKINDCD||R1.TESTITEMCD||R1.SCORE_DIV = '" + TESTCD + "' ");
        stb.append("             AND R1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN RECORD_PROV_FLG_DAT P1 ON P1.YEAR = R1.YEAR ");
        stb.append("             AND P1.CLASSCD = R1.CLASSCD ");
        stb.append("             AND P1.SCHOOL_KIND = R1.SCHOOL_KIND ");
        stb.append("             AND P1.CURRICULUM_CD = R1.CURRICULUM_CD ");
        stb.append("             AND P1.SUBCLASSCD = R1.SUBCLASSCD ");
        stb.append("             AND P1.SCHREGNO = R1.SCHREGNO ");
        stb.append("     LEFT JOIN V_SUBCLASS_MST S1 ON S1.YEAR = R1.YEAR ");
        stb.append("             AND S1.CLASSCD = R1.CLASSCD ");
        stb.append("             AND S1.SCHOOL_KIND = R1.SCHOOL_KIND ");
        stb.append("             AND S1.CURRICULUM_CD = R1.CURRICULUM_CD ");
        stb.append("             AND S1.SUBCLASSCD = R1.SUBCLASSCD ");
        stb.append("     LEFT JOIN ASSESS_MST L1 ON L1.ASSESSCD = '3' ");
        stb.append("             AND R1.SCORE BETWEEN L1.ASSESSLOW AND L1.ASSESSHIGH ");
        if ("1".equals(_param._useAssessCourseMst)) {
            stb.append("     LEFT JOIN ASSESS_COURSE_MST L2 ON L2.ASSESSCD = '3' ");
            stb.append("             AND L2.COURSECD = T1.COURSECD ");
            stb.append("             AND L2.MAJORCD = T1.MAJORCD ");
            stb.append("             AND L2.COURSECODE = T1.COURSECODE ");
            stb.append("             AND R1.SCORE BETWEEN L2.ASSESSLOW AND L2.ASSESSHIGH ");
        } else if ("1".equals(_param._useAssessSubclassMst)) {
            stb.append("     LEFT JOIN ASSESS_SUBCLASS_MST L2 ON L2.YEAR = T1.YEAR ");
            stb.append("             AND L2.GRADE = T1.GRADE ");
            stb.append("             AND L2.COURSECD = T1.COURSECD ");
            stb.append("             AND L2.MAJORCD = T1.MAJORCD ");
            stb.append("             AND L2.COURSECODE = T1.COURSECODE ");
            stb.append("             AND L2.CLASSCD = R1.CLASSCD ");
            stb.append("             AND L2.SCHOOL_KIND = R1.SCHOOL_KIND ");
            stb.append("             AND L2.CURRICULUM_CD = R1.CURRICULUM_CD ");
            stb.append("             AND L2.SUBCLASSCD = R1.SUBCLASSCD ");
            stb.append("             AND R1.SCORE BETWEEN L2.ASSESSLOW AND L2.ASSESSHIGH ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
        //評価・評定のミスマッチ
        stb.append("     AND VALUE(R1.VALUE, -1) != VALUE(" + assesslevel + ", -1) ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     SUBCLASSCD ");
        return stb.toString();
    }

    private List getStudentList(final DB2UDB db2) {
        final List studentList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sqlMismatch();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");

                final String subclasscd = rs.getString("SUBCLASSCD");
                final String subclassname = rs.getString("SUBCLASSNAME");
                final String score = rs.getString("SCORE");
                final String value = rs.getString("VALUE");
                final String assesslevel = rs.getString("ASSESSLEVEL");
                final String provFlg = rs.getString("PROV_FLG");

                final Student student = new Student(grade, hrClass, hrName, attendno, schregno, name, subclasscd, subclassname, score, value, assesslevel, provFlg);
                studentList.add(student);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return studentList;
    }

    private class Student {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _attendno;
        final String _schregno;
        final String _name;
        final String _subclasscd;
        final String _subclassname;
        final String _score;
        final String _value;
        final String _assesslevel;
        final String _provFlg;

        Student(
            final String grade,
            final String hrClass,
            final String hrName,
            final String attendno,
            final String schregno,
            final String name,
            final String subclasscd,
            final String subclassname,
            final String score,
            final String value,
            final String assesslevel,
            final String provFlg) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _score = score;
            _value = value;
            _assesslevel = assesslevel;
            _provFlg = provFlg;
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
    public class Param {
        final String _year;
        final String _semester;
        final String _grade;
        final String _loginDate;
        final String _prgid;
        final String _useCurriculumcd;
        final String _useAssessCourseMst;
        final String _useAssessSubclassMst;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _loginDate = request.getParameter("LOGIN_DATE");
            _prgid = request.getParameter("PRGID");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useAssessCourseMst = request.getParameter("useAssessCourseMst");
            _useAssessSubclassMst = request.getParameter("useAssessSubclassMst");
        }

        /** 作成日 */
        public String getNow() {
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(nao_package.KenjaProperties.gengou(Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日H時m分");
            stb.append(sdf.format(date));
            return stb.toString();
        }
    }
}

// eof

