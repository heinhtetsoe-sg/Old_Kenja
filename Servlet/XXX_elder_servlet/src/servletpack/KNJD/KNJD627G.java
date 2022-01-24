//kanji=漢字
/*
 *
 * 作成日: 2021/01/27
 * 作成者: ishimine
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *   ＜KNJD627G:総点進級判定会議資料＞
 */

public class KNJD627G {

    private static final Log log = LogFactory.getLog(KNJD627G.class);
    private boolean _hasData = false;

    Param _param;
    private static String HYOUKA  = "9990008";    //評価
    private static String HYOUTEI = "9990009";    //評定
    private static String ALL9 = "99H99999999";   //全科目コード

    /**
     * @param request
     * @param response
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            if(!"".equals(_param._aRank)) {
                printMain(db2, svf);
            }

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJD627G.frm", 1);
        List<Student> studentList = getSchregList(db2);

        if(studentList.isEmpty()) return;

          int gyo = 1;
          final int maxGyo = 50;
          boolean flgB = false;
          setTitle(svf, flgB);
          for(Student student : studentList) {
              if(flgB == false && Integer.valueOf(student._souten) < Integer.valueOf(_param._aRank)) {
                  svf.VrEndPage();
                  gyo = 1;
                  flgB = true;
                  setTitle(svf, flgB);
              }
              if(gyo > maxGyo) {
                  svf.VrEndPage();
                  gyo = 1;
                  setTitle(svf, flgB);
              }

              final String no = student._attendno.length() == 1 ? " " + student._attendno : student._attendno;
              svf.VrsOutn("NO", gyo, student._hr_Class_Name1 + no); //クラス名＋番号

              final Integer keta = KNJ_EditEdit.getMS932ByteLength(student._name);
              final String fieldName = keta <= 20 ? "1" : keta <= 30 ? "2" : "3";
              svf.VrsOutn("NAME" + fieldName, gyo, student._name); //氏名
              svf.VrsOutn("TOTAL_SCORE", gyo, student._souten); //総点
              svf.VrsOutn("AVERAGE", gyo, student._avg); //平均

              //未修得科目
              final Integer keta2 = KNJ_EditEdit.getMS932ByteLength(student._remarks);
              final String fieldSubclass = keta2 <= 50 ? "1" : "2";
              svf.VrsOutn("LACK_SUBCLASS_NAME" + fieldSubclass, gyo, student._remarks);

              //1年時の総点　※2年時のみ印字
              if("02".equals(_param._gradeCd)) {
                  svf.VrsOutn("SCORE1", gyo, student._souten_Sub1);
              }

              gyo++;
              _hasData = true;
          }
           svf.VrEndPage();
    }

    private void setTitle(final Vrw32alp svf, final boolean flgB) {
        svf.VrsOut("TITLE", "進 級 判 定 会 議 資 料");
        svf.VrsOut("GRADE", _param._year + "年度 高校 " + Integer.valueOf(_param._gradeCd) +"学年");
        final String rankName;
        if(flgB) {
            rankName = "Ｂ      " + _param._aRank + "点未満0点以上";
        } else {
            rankName = "Ａ      " + _param._aRank + "点以上";
        }
        svf.VrsOut("RAANK_NAME", rankName);
    }

    private List getSchregList(final DB2UDB db2) {
        List addList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getSchregSoutenSql();
        log.debug(" sql =" + sql);

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String hr_Class_Name1 = StringUtils.defaultString(rs.getString("HR_CLASS_NAME1"));
                final String attendno = StringUtils.defaultString(rs.getString("ATTENDNO"));
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String souten = rs.getString("SOUTEN");
                final String souten_Sub1 = rs.getString("SOUTEN_SUB1");
                final String avg = rs.getString("AVG");
                final String remarks = rs.getString("REMARKS");

                Student student = new Student(hr_Class_Name1, attendno, schregno, name, souten, souten_Sub1, avg, remarks );
                addList.add(student);

            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return addList;
    }

    /**
     * @return
     */
    private String getSchregSoutenSql() {

        final StringBuffer stb = new StringBuffer();
        //対象学年クラス
        stb.append(" WITH GRADE_HR AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("   GDAT.YEAR, ");
        stb.append("   GDAT.GRADE, ");
        stb.append("   HDAT.HR_CLASS, ");
        stb.append("   HDAT.HR_CLASS_NAME1 ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_GDAT GDAT ");
        stb.append(" INNER JOIN ");
        stb.append("   SCHREG_REGD_HDAT HDAT ");
        stb.append("    ON HDAT.YEAR = GDAT.YEAR ");
        stb.append("   AND HDAT.GRADE = GDAT.GRADE ");
        stb.append("   AND HR_CLASS_NAME1 IS NOT NULL ");
        stb.append(" WHERE ");
        stb.append("   GDAT.YEAR = '" + _param._year + "' ");
        stb.append("   AND GDAT.SCHOOL_KIND = 'H' ");
        stb.append("   AND GDAT.GRADE_CD = '" + _param._gradeCd + "' ");
        //対象生徒
        stb.append(" ), SCHREG_BASE AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.HR_CLASS_NAME1, ");
        stb.append("   REGD.SEMESTER, ");
        stb.append("   REGD.SCHREGNO, ");
        stb.append("   REGD.ATTENDNO, ");
        stb.append("   BASE.NAME ");
        stb.append(" FROM ");
        stb.append("   GRADE_HR T1 ");
        stb.append(" INNER JOIN ");
        stb.append("   SCHREG_REGD_DAT REGD ");
        stb.append("    ON REGD.YEAR = T1.YEAR ");
        stb.append("   AND REGD.SEMESTER = '" + _param._lastSeme + "' ");
        stb.append("   AND REGD.GRADE = T1.GRADE ");
        stb.append("   AND REGD.HR_CLASS = T1.HR_CLASS ");
        stb.append(" INNER JOIN ");
        stb.append("   SCHREG_BASE_MST BASE ");
        stb.append("    ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("   REGD.HR_CLASS, REGD.ATTENDNO ");
        //対象生徒の前学年度クラス
        stb.append(" ), GRADE_PREV AS ( ");
        stb.append(" SELECT ");
        stb.append("   MAX(REGD.YEAR) AS YEAR, ");
        stb.append("   REGD.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("   SCHREG_BASE BASE ");
        stb.append(" INNER JOIN ");
        stb.append("   SCHREG_REGD_DAT REGD ");
        stb.append("    ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("   AND REGD.YEAR < '" + _param._year + "' ");
        stb.append("   AND REGD.GRADE < BASE.GRADE ");
        stb.append(" GROUP BY ");
        stb.append("   REGD.SCHREGNO ");
        //今学年度の修得単位
        stb.append(" ), CREDIT AS ( ");
        stb.append(" SELECT ");
        stb.append("   BASE.YEAR, ");
        stb.append("   BASE.SCHREGNO, ");
        stb.append("   T3.CLASSCD, ");
        stb.append("   T3.SCHOOL_KIND, ");
        stb.append("   T3.CURRICULUM_CD, ");
        stb.append("   T3.SUBCLASSCD, ");
        stb.append("   VALUE(T3.GET_CREDIT,0) AS CREDIT ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE BASE ");
        stb.append(" LEFT JOIN RECORD_SCORE_DAT T3 ");
        stb.append("      ON T3.YEAR = BASE.YEAR ");
        stb.append("     AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '" + HYOUTEI + "' ");
        stb.append("     AND T3.SCHREGNO = BASE.SCHREGNO ");
        //前学年度の修得単位
        stb.append(" ), CREDIT_PREV AS ( ");
        stb.append(" SELECT ");
        stb.append("   BASE.YEAR, ");
        stb.append("   BASE.SCHREGNO, ");
        stb.append("   T3.CLASSCD, ");
        stb.append("   T3.SCHOOL_KIND, ");
        stb.append("   T3.CURRICULUM_CD, ");
        stb.append("   T3.SUBCLASSCD, ");
        stb.append("   VALUE(T3.GET_CREDIT,0) AS CREDIT ");
        stb.append(" FROM ");
        stb.append("     GRADE_PREV BASE ");
        stb.append(" LEFT JOIN RECORD_SCORE_DAT T3 ");
        stb.append("      ON T3.YEAR = BASE.YEAR ");
        stb.append("     AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '" + HYOUTEI + "' ");
        stb.append("     AND T3.SCHREGNO = BASE.SCHREGNO ");
        //1、2年時の修得単位
        stb.append(" ), TOTAL_CREDIT AS ( ");
        stb.append(" SELECT * FROM CREDIT_PREV UNION SELECT * FROM CREDIT ");
        //未修得科目
        stb.append(" ), NOPASS_SUBCLS AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     SM.SUBCLASSABBV ");
        stb.append(" FROM ");
        stb.append("     TOTAL_CREDIT T1 ");
        stb.append(" LEFT JOIN V_SUBCLASS_MST SM ");
        stb.append("      ON SM.YEAR = T1.YEAR ");
        stb.append("     AND SM.CLASSCD = T1.CLASSCD ");
        stb.append("     AND SM.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("     AND SM.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("     AND SM.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.CREDIT < 1 ");
        stb.append(" ORDER BY T1.SCHREGNO, T1.YEAR, T1.SUBCLASSCD ");
        //未修得科目 連結
        stb.append(" ), MAKE_REMARKS AS ( ");
        stb.append(" SELECT ");
        stb.append("     NS.SCHREGNO, ");
        stb.append("     LISTAGG(NS.SUBCLASSABBV, ',') AS REMARKS ");
        stb.append(" FROM ");
        stb.append("    NOPASS_SUBCLS NS ");
        stb.append(" GROUP BY ");
        stb.append("     NS.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("     NS.SCHREGNO ");
        stb.append(" ) ");
        //メイン表
        stb.append(" SELECT ");
        stb.append("   BASE.YEAR AS YEAR2, ");
        stb.append("   BASE.HR_CLASS_NAME1, ");
        stb.append("   CAST(BASE.ATTENDNO AS INT) ATTENDNO, ");
        stb.append("   BASE.SCHREGNO, ");
        stb.append("   BASE.NAME, ");
        stb.append("   SOUTEN.TOTAL_POINT AS SOUTEN, ");
        stb.append("   T2.YEAR AS YEAR1, ");
        stb.append("   SOUTEN_PREV.TOTAL_POINT AS SOUTEN_SUB1, ");
        stb.append("   DECIMAL(INT(FLOAT(RANK.AVG) * 10 + 0.5)/10.0,5,1) AS AVG, ");
        stb.append("   REMARK.REMARKS ");
        stb.append(" FROM ");
        stb.append("   SCHREG_BASE BASE ");
        stb.append(" LEFT JOIN ");
        stb.append("   RECORD_RANK_SDIV_SOUTEN_DAT SOUTEN ");
        stb.append("    ON SOUTEN.YEAR = BASE.YEAR ");
        stb.append("   AND SOUTEN.SEMESTER || SOUTEN.TESTKINDCD || SOUTEN.TESTITEMCD || SOUTEN.SCORE_DIV = '" + HYOUKA + "' ");
        stb.append("   AND SOUTEN.CLASSCD || SOUTEN.SCHOOL_KIND || SOUTEN.CURRICULUM_CD || SOUTEN.SUBCLASSCD = '" + ALL9 + "' ");
        stb.append("   AND SOUTEN.SCHREGNO = BASE.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("   GRADE_PREV T2 ");
        stb.append("    ON T2.SCHREGNO = BASE.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("   RECORD_RANK_SDIV_SOUTEN_DAT SOUTEN_PREV ");
        stb.append("    ON SOUTEN_PREV.YEAR = T2.YEAR ");
        stb.append("   AND SOUTEN_PREV.SEMESTER || SOUTEN_PREV.TESTKINDCD || SOUTEN_PREV.TESTITEMCD || SOUTEN_PREV.SCORE_DIV = '" + HYOUKA + "' ");
        stb.append("   AND SOUTEN_PREV.CLASSCD || SOUTEN_PREV.SCHOOL_KIND || SOUTEN_PREV.CURRICULUM_CD || SOUTEN_PREV.SUBCLASSCD = '" + ALL9 + "' ");
        stb.append("   AND SOUTEN_PREV.SCHREGNO = T2.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("   RECORD_RANK_SDIV_DAT RANK ");
        stb.append("    ON RANK.YEAR = BASE.YEAR ");
        stb.append("   AND RANK.SEMESTER || RANK.TESTKINDCD || RANK.TESTITEMCD || RANK.SCORE_DIV = '" + HYOUKA + "' ");
        stb.append("   AND RANK.CLASSCD || RANK.SCHOOL_KIND || RANK.CURRICULUM_CD || RANK.SUBCLASSCD = '" + ALL9 + "' ");
        stb.append("   AND RANK.SCHREGNO = BASE.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("   MAKE_REMARKS REMARK ");
        stb.append("    ON REMARK.SCHREGNO = BASE.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("   SOUTEN.TOTAL_POINT IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("   SOUTEN DESC ");

        return stb.toString();
    }

    private class Student {
        final String _hr_Class_Name1;
        final String _attendno;
        final String _schregno;
        final String _name;
        final String _souten;
        final String _souten_Sub1;
        final String _avg;
        final String _remarks;

        public Student (final String hr_Class_Name1, final String attendno, final String schregno, final String name, final String souten, final String souten_Sub1, final String avg, final String remarks)
        {
            _hr_Class_Name1 = hr_Class_Name1;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
            _souten = souten;
            _souten_Sub1 = souten_Sub1;
            _avg = avg;
            _remarks = remarks;
        }
    }

    /** パラメータ取得*/
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _gradeCd;
        private final String _lastSeme;
        private final String _aRank;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("HID_YEAR");
            _gradeCd = request.getParameter("GRADE");
            _aRank = request.getParameter("A_RANK");
            _lastSeme = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SEMESTER FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER <> '9' ORDER BY SEMESTER DESC "));
        }
    }
}

// eof

