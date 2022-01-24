/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 9bb7f996d31ef47cd93a6f0551424bbd53ba6f36 $
 *
 * 作成日: 2019/10/25
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.math.BigDecimal;
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
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJA061A {

    private static final Log log = LogFactory.getLog(KNJA061A.class);

    private static final String SEMEALL = "9";
    private static final String ALL9 = "999999";

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
        final String form = "KNJA061A.frm";
        svf.VrSetForm(form , 1);
        final int MAX_LINE = 5;
        final int MAX_RETU = 2;


    	int line = 1;
    	int idx = 1;
        final List studentList = getList(db2);
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
        	if(idx > MAX_RETU) {
        		idx = 1;
        		line++;
        	}

        	if(line > MAX_LINE) {
        		svf.VrEndPage();
        		svf.VrSetForm(form , 1);
        		line = 1;
        	}

            final Student student = (Student) iterator.next();
            svf.VrsOutn("SCHREGNO"+idx, line, student._examno); //受験番号
            svf.VrsOutn("NAME"+idx, line, student._name); //氏名
            svf.VrsOutn("KANA"+idx, line, student._kana); //かな
            svf.VrsOutn("SCORE"+idx, line, student._total4); //得点
            svf.VrsOutn("SCORE_DIV"+idx, line, student._score_div); //得点
            svf.VrsOutn("RANK"+idx+"_1", line, student._rank4); //順位
            svf.VrsOutn("SEX"+idx, line, student._sex); //性別
            svf.VrsOutn("RANK"+idx+"_2", line, ""); //順位
            svf.VrsOutn("INTERVIEW"+idx, line, student._score); //面接
            svf.VrsOutn("REMARK"+idx+"_1", line, student._position); //役職
            svf.VrsOutn("REMARK"+idx+"_2", line, student._club); //部活動
            svf.VrsOutn("ATTEND"+idx, line, student._absence); //欠席
            svf.VrsOutn("FINSCHOOL_NAME"+idx, line, student._finschool_name); //出身学校
            svf.VrsOutn("REMARK"+idx+"_3", line, student._eiken); //英検

            int yearLine = 1;
            for (Iterator it = student._studentHistList.iterator(); it.hasNext();) {
                final StudentHist studentHist = (StudentHist) it.next();
                final String hrName = studentHist._hr_name + studentHist._attendno;
                svf.VrsOutn("HR_NAME"+idx+"_"+yearLine, line, hrName); //年組番
                svf.VrsOutn("DOMITRY"+idx+"_"+yearLine, line, studentHist._domi_name); //寮・通学
                svf.VrsOutn("GRADE_SCORE"+idx+"_"+yearLine, line, studentHist._score); //学年成績

                yearLine++;
            }

            _hasData = true;
            idx++;
        }
        svf.VrEndPage();
    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final String[] value) {
        if (null != value) {
            for (int i = 0 ; i < value.length; i++) {
                svf.VrsOutn(field, i + 1, value[i]);
            }
        }
    }

    private int getSemeLine(final String semester) {
        final int line;
        if (SEMEALL.equals(semester)) {
            line = 3;
        } else {
            line = Integer.parseInt(semester);
        }
        return line;
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
        	final String sql = ("2".equals(_param._disp)) ? getFreshmanSql() : getStudentSql();
            log.debug(" sql =" + sql);

            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student();
                student._schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                student._applicantdiv = StringUtils.defaultString(rs.getString("APPLICANTDIV"));
                student._examno = StringUtils.defaultString(rs.getString("EXAMNO"));
                student._testdiv = StringUtils.defaultString(rs.getString("TESTDIV"));
                student._score_div = StringUtils.defaultString(rs.getString("SCORE_DIV"));
                student._name = StringUtils.defaultString(rs.getString("NAME"));
                student._kana = StringUtils.defaultString(rs.getString("NAME_KANA"));
                student._sex = StringUtils.defaultString(rs.getString("SEX"));
                student._exam_type = StringUtils.defaultString(rs.getString("EXAM_TYPE"));
                student._receptno = StringUtils.defaultString(rs.getString("RECEPTNO"));
                student._total4 = StringUtils.defaultString(rs.getString("TOTAL4"));
                student._rank4 = StringUtils.defaultString(rs.getString("TOTAL_RANK4"));
                student._score = StringUtils.defaultString(rs.getString("SCORE"));
                student._specialactrec = StringUtils.defaultString(rs.getString("SPECIALACTREC"));
                student._finschool_name = StringUtils.defaultString(rs.getString("FINSCHOOL_NAME"));
                student._position = StringUtils.defaultString(rs.getString("POSITION"));
                student._totalstudytime = StringUtils.defaultString(rs.getString("TOTALSTUDYTIME"));
                student._club = StringUtils.defaultString(rs.getString("CLUB"));
                student._absence_days3 = StringUtils.defaultString(rs.getString("ABSENCE_DAYS3"));
                student._absence = StringUtils.defaultString(rs.getString("ABSENCE"));
                student._eiken = StringUtils.defaultString(rs.getString("EIKEN"));
                student._studentHistList = student.setHistList(db2);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH RECEPT_MIN AS ( ");
        stb.append("   SELECT ");
        stb.append("     ENTEXAMYEAR ");
        stb.append("     , EXAM_TYPE ");
        stb.append("     , APPLICANTDIV ");
        stb.append("     , MIN(TESTDIV) TESTDIV ");
        stb.append("     , RECEPTNO ");
        stb.append("     , EXAMNO ");
        stb.append("   FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT ");
        stb.append("   WHERE ");
        stb.append("     JUDGEDIV = '1' ");
        stb.append("   GROUP BY ");
        stb.append("     ENTEXAMYEAR ");
        stb.append("     , APPLICANTDIV ");
        stb.append("     , EXAM_TYPE ");
        stb.append("     , RECEPTNO ");
        stb.append("     , EXAMNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   REGD.SCHREGNO, ");
        stb.append("   BASE.APPLICANTDIV, ");
        stb.append("   BASE.EXAMNO, ");
        stb.append("   BASE.TESTDIV, ");
        stb.append("   CASE WHEN BASE.APPLICANTDIV = '1' THEN L024.NAME1 ELSE L004.NAME1 END AS SCORE_DIV, ");
        stb.append("   SBM.NAME, ");
        stb.append("   SBM.NAME_KANA, ");
        stb.append("   Z002.NAME1 AS SEX, ");
        stb.append("   RECEPT.EXAM_TYPE, ");
        stb.append("   RECEPT.RECEPTNO, ");
        stb.append("   RECEPT.TOTAL4, ");
        stb.append("   RECEPT.TOTAL_RANK4, ");
        stb.append("   SCORE.SCORE, ");
        stb.append("   CONF.SPECIALACTREC, ");
        stb.append("   FINSCHOOL.FINSCHOOL_NAME, ");
        stb.append("   L068.NAME2 AS POSITION, ");
        stb.append("   CONF.TOTALSTUDYTIME, ");
        stb.append("   L069.NAME2 AS CLUB, ");
        stb.append("   CONF.ABSENCE_DAYS3, ");
        stb.append("   L067.NAME2 AS ABSENCE, ");
        stb.append("   L055.NAME2 AS EIKEN ");
        stb.append(" FROM  ");
        stb.append("   SCHREG_REGD_DAT REGD ");
        stb.append("   LEFT JOIN SCHREG_BASE_DETAIL_MST BD003 ");
        stb.append("           ON BD003.SCHREGNO = REGD.SCHREGNO ");
        stb.append("          AND BD003.BASE_SEQ = '003' ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST SBM ");
        stb.append("          ON SBM.SCHREGNO = REGD.SCHREGNO ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("           ON BASE.ENTEXAMYEAR  = YEAR(SBM.ENT_DATE) ");
        stb.append("          AND BASE.APPLICANTDIV = '"+ _param._applicantdiv +"' ");
        stb.append("          AND BASE.EXAMNO = BD003.BASE_REMARK1 ");
        stb.append("          AND BASE.TESTDIV =BD003.BASE_REMARK2 ");

//        stb.append("   LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ");
//        stb.append("          ON RECEPT.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
//        stb.append("         AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
//        stb.append("         AND RECEPT.TESTDIV      = BASE.TESTDIV ");
//        stb.append("         AND RECEPT.EXAM_TYPE    = '1' ");
//        stb.append("         AND RECEPT.EXAMNO = BASE.EXAMNO ");

        stb.append("   LEFT JOIN RECEPT_MIN ");
        stb.append("          ON RECEPT_MIN.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("         AND RECEPT_MIN.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("         AND RECEPT_MIN.EXAMNO = BASE.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("          ON RECEPT.ENTEXAMYEAR  = RECEPT_MIN.ENTEXAMYEAR ");
        stb.append("         AND RECEPT.APPLICANTDIV = RECEPT_MIN.APPLICANTDIV ");
        stb.append("         AND RECEPT.TESTDIV      = RECEPT_MIN.TESTDIV ");
        stb.append("         AND RECEPT.EXAM_TYPE    = '1' ");
        stb.append("         AND RECEPT.EXAMNO = RECEPT_MIN.EXAMNO ");

        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SCORE ");
        stb.append("          ON SCORE.ENTEXAMYEAR    = RECEPT.ENTEXAMYEAR ");
        stb.append("         AND SCORE.APPLICANTDIV   = RECEPT.APPLICANTDIV ");
        stb.append("         AND SCORE.TESTDIV        = RECEPT.TESTDIV ");
        stb.append("         AND SCORE.EXAM_TYPE      = RECEPT.EXAM_TYPE ");
        stb.append("         AND SCORE.RECEPTNO       = RECEPT.RECEPTNO ");
        stb.append("         AND SCORE.TESTSUBCLASSCD = 'A' ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF    ");
        stb.append("          ON CONF.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("         AND CONF.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("         AND CONF.EXAMNO       = BASE.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ");
        stb.append("          ON DETAIL.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("         AND DETAIL.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("         AND DETAIL.EXAMNO = BASE.EXAMNO ");
        stb.append("         AND DETAIL.SEQ = '005' ");
        stb.append("   LEFT JOIN FINSCHOOL_MST FINSCHOOL ");
        stb.append("          ON FINSCHOOL.FINSCHOOLCD = SBM.FINSCHOOLCD ");
        stb.append("   LEFT JOIN NAME_MST Z002 ");
        stb.append("          ON Z002.NAMECD2 = SBM.SEX ");
        stb.append("         AND Z002.NAMECD1 = 'Z002' ");

        stb.append("   LEFT JOIN NAME_MST L004 ");
        stb.append("          ON L004.NAMECD2 = RECEPT.TESTDIV ");
        stb.append("         AND L004.NAMECD1 = 'L004' ");
        stb.append("   LEFT JOIN NAME_MST L024 ");
        stb.append("          ON L024.NAMECD2 = RECEPT.TESTDIV ");
        stb.append("         AND L024.NAMECD1 = 'L024' ");

        stb.append("   LEFT JOIN NAME_MST L068 ");
        stb.append("          ON L068.NAMECD2 = CONF.SPECIALACTREC ");
        stb.append("         AND L068.NAMECD1 = 'L068' ");
        stb.append("   LEFT JOIN NAME_MST L069 ");
        stb.append("          ON L069.NAMECD2 = CONF.TOTALSTUDYTIME ");
        stb.append("         AND L069.NAMECD1 = 'L069' ");
        stb.append("   LEFT JOIN NAME_MST L067 ");
        stb.append("          ON L067.NAMECD2 = CONF.ABSENCE_DAYS3 ");
        stb.append("         AND L067.NAMECD1 = 'L067' ");
        stb.append("   LEFT JOIN NAME_MST L055 ");
        stb.append("          ON L055.NAMECD2 = DETAIL.REMARK3 ");
        stb.append("         AND L055.NAMECD1 = 'L055' ");
        stb.append(" WHERE ");
        stb.append("       REGD.YEAR     = '"+ _param._year +"' ");
        stb.append("   AND REGD.SEMESTER = '"+ _param._semester +"' ");
        stb.append("   AND REGD.GRADE || REGD.HR_CLASS = '"+ _param._gradeHrClass +"' ");
        if(!"ALL".equals(_param._output)) {
        	//全て以外を選択
            stb.append("   AND SBM.SEX = '"+ _param._sex +"' ");
        }
        stb.append("   AND REGD.SCHREGNO IN " + _param._selectedIn + " ");

        stb.append(" ORDER BY EXAMNO,");
        stb.append("   REGD.GRADE, ");
        stb.append("   REGD.HR_CLASS, ");
        stb.append("   REGD.ATTENDNO, ");
        stb.append("   REGD.SCHREGNO, ");
        stb.append("   BASE.EXAMNO ");

        return stb.toString();
    }


    private String getFreshmanSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH RECEPT_MIN AS ( ");
        stb.append("   SELECT ");
        stb.append("     ENTEXAMYEAR ");
        stb.append("     , EXAM_TYPE ");
        stb.append("     , APPLICANTDIV ");
        stb.append("     , MIN(TESTDIV) TESTDIV ");
        stb.append("     , RECEPTNO ");
        stb.append("     , EXAMNO ");
        stb.append("   FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT ");
        stb.append("   WHERE ");
        stb.append("     JUDGEDIV = '1' ");
        stb.append("   GROUP BY ");
        stb.append("     ENTEXAMYEAR ");
        stb.append("     , APPLICANTDIV ");
        stb.append("     , EXAM_TYPE ");
        stb.append("     , RECEPTNO ");
        stb.append("     , EXAMNO ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("   FRESH.SCHREGNO, ");
        stb.append("   BASE.APPLICANTDIV, ");
        stb.append("   BASE.EXAMNO, ");
        stb.append("   BASE.TESTDIV, ");
        stb.append("   CASE WHEN BASE.APPLICANTDIV = '1' THEN L024.NAME1 ELSE L004.NAME1 END AS SCORE_DIV, ");
        stb.append("   FRESH.NAME, ");
        stb.append("   FRESH.NAME_KANA, ");
        stb.append("   Z002.NAME1 AS SEX, ");
        stb.append("   RECEPT.EXAM_TYPE, ");
        stb.append("   RECEPT.RECEPTNO, ");
        stb.append("   RECEPT.TOTAL4, ");
        stb.append("   RECEPT.TOTAL_RANK4, ");
        stb.append("   SCORE.SCORE, ");
        stb.append("   CONF.SPECIALACTREC, ");
        stb.append("   FINSCHOOL.FINSCHOOL_NAME, ");
        stb.append("   L068.NAME2 AS POSITION, ");
        stb.append("   CONF.TOTALSTUDYTIME, ");
        stb.append("   L069.NAME2 AS CLUB, ");
        stb.append("   CONF.ABSENCE_DAYS3, ");
        stb.append("   L067.NAME2 AS ABSENCE, ");
        stb.append("   L055.NAME2 AS EIKEN ");
        stb.append(" FROM  ");
        stb.append("   FRESHMAN_DAT FRESH ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("           ON BASE.ENTEXAMYEAR  = FRESH.ENTERYEAR ");
        stb.append("          AND BASE.APPLICANTDIV = '"+ _param._applicantdiv +"' ");
        stb.append("          AND BASE.EXAMNO       = FRESH.EXAMNO ");
//        stb.append("          AND BASE.TESTDIV      = FRESH.TESTDIV ");

//        stb.append("   LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ");
//        stb.append("          ON RECEPT.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
//        stb.append("         AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
//        stb.append("         AND RECEPT.TESTDIV      = BASE.TESTDIV ");
//        stb.append("         AND RECEPT.EXAM_TYPE    = '1' ");
//        stb.append("         AND RECEPT.EXAMNO = BASE.EXAMNO ");

        stb.append("   LEFT JOIN RECEPT_MIN ");
        stb.append("          ON RECEPT_MIN.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("         AND RECEPT_MIN.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("         AND RECEPT_MIN.EXAMNO = BASE.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("          ON RECEPT.ENTEXAMYEAR  = RECEPT_MIN.ENTEXAMYEAR ");
        stb.append("         AND RECEPT.APPLICANTDIV = RECEPT_MIN.APPLICANTDIV ");
        stb.append("         AND RECEPT.TESTDIV      = RECEPT_MIN.TESTDIV ");
        stb.append("         AND RECEPT.EXAM_TYPE    = '1' ");
        stb.append("         AND RECEPT.EXAMNO = RECEPT_MIN.EXAMNO ");

        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SCORE ");
        stb.append("          ON SCORE.ENTEXAMYEAR    = RECEPT.ENTEXAMYEAR ");
        stb.append("         AND SCORE.APPLICANTDIV   = RECEPT.APPLICANTDIV ");
        stb.append("         AND SCORE.TESTDIV        = RECEPT.TESTDIV ");
        stb.append("         AND SCORE.EXAM_TYPE      = RECEPT.EXAM_TYPE ");
        stb.append("         AND SCORE.RECEPTNO       = RECEPT.RECEPTNO ");
        stb.append("         AND SCORE.TESTSUBCLASSCD = 'A' ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF    ");
        stb.append("          ON CONF.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("         AND CONF.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("         AND CONF.EXAMNO       = BASE.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ");
        stb.append("          ON DETAIL.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("         AND DETAIL.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("         AND DETAIL.EXAMNO = BASE.EXAMNO ");
        stb.append("         AND DETAIL.SEQ = '005' ");
        stb.append("   LEFT JOIN FINSCHOOL_MST FINSCHOOL ");
        stb.append("          ON FINSCHOOL.FINSCHOOLCD = BASE.FS_CD ");
        stb.append("   LEFT JOIN NAME_MST Z002 ");
        stb.append("          ON Z002.NAMECD2 = FRESH.SEX ");
        stb.append("         AND Z002.NAMECD1 = 'Z002' ");

        stb.append("   LEFT JOIN NAME_MST L004 ");
        stb.append("          ON L004.NAMECD2 = RECEPT.TESTDIV ");
        stb.append("         AND L004.NAMECD1 = 'L004' ");
        stb.append("   LEFT JOIN NAME_MST L024 ");
        stb.append("          ON L024.NAMECD2 = RECEPT.TESTDIV ");
        stb.append("         AND L024.NAMECD1 = 'L024' ");

        stb.append("   LEFT JOIN NAME_MST L068 ");
        stb.append("          ON L068.NAMECD2 = CONF.SPECIALACTREC ");
        stb.append("         AND L068.NAMECD1 = 'L068' ");
        stb.append("   LEFT JOIN NAME_MST L069 ");
        stb.append("          ON L069.NAMECD2 = CONF.TOTALSTUDYTIME ");
        stb.append("         AND L069.NAMECD1 = 'L069' ");
        stb.append("   LEFT JOIN NAME_MST L067 ");
        stb.append("          ON L067.NAMECD2 = CONF.ABSENCE_DAYS3 ");
        stb.append("         AND L067.NAMECD1 = 'L067' ");
        stb.append("   LEFT JOIN NAME_MST L055 ");
        stb.append("          ON L055.NAMECD2 = DETAIL.REMARK3 ");
        stb.append("         AND L055.NAMECD1 = 'L055' ");
        stb.append(" WHERE ");
        stb.append("       FRESH.ENTERYEAR  = '"+ _param._year +"' ");

        stb.append("   AND FRESH.SCHREGNO IN " + _param._selectedIn + " ");

        stb.append(" ORDER BY EXAMNO,RECEPTNO ");

        return stb.toString();
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private class Student {
    	String _schregno;
    	String _applicantdiv;
    	String _examno;
    	String _testdiv;
    	String _name;
    	String _kana;
    	String _sex;
    	String _exam_type;
    	String _receptno;
    	String _total4;
    	String _rank4;
    	String _score;
    	String _score_div;
    	String _specialactrec;
    	String _finschool_name;
    	String _position;
    	String _totalstudytime;
    	String _club;
    	String _absence_days3;
    	String _absence;
    	String _eiken;
        List _studentHistList;

        private List setHistList(final DB2UDB db2) {
            final List retList = new ArrayList();
            //新入生の場合は学年組・寮通学・成績は取得なし
            if ("2".equals(_param._disp)) {
                return retList;
            }
            final String sql = studentHistSql();
            log.debug(" studentHistSql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	final String year = StringUtils.defaultString(rs.getString("YEAR"));
                	final String grade = StringUtils.defaultString(rs.getString("GRADE"));
                	final String hr_class = StringUtils.defaultString(rs.getString("HR_CLASS"));
                	final String hr_name = Integer.parseInt(rs.getString("GRADE_CD")) + "年" + rs.getString("HR_CLASS_NAME1") + "組" ;
                	final String attendno = StringUtils.defaultString(rs.getString("ATTENDNO"));
                	final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                	final String domi_cd = StringUtils.defaultString(rs.getString("DOMI_CD"));
                	final String domi_name = StringUtils.defaultString(rs.getString("DOMI_NAME"));
                	final String score = StringUtils.defaultString(rs.getString("SCORE"));
                    final StudentHist studentHist = new StudentHist(year, grade, hr_class, hr_name, attendno, schregno, domi_cd, domi_name, score, "", "");

                    retList.add(studentHist);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return retList;
        }

        private String studentHistSql() {

            final StringBuffer stb = new StringBuffer();
            //3年分の生徒情報
            stb.append(" WITH SCHNO AS( ");
            stb.append("   SELECT DISTINCT ");
            stb.append("     REGD.YEAR, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     GDAT.GRADE_CD, ");
            stb.append("     HDAT.HR_CLASS_NAME1, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     REGD.SCHREGNO ");
            stb.append("   FROM  ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ");
            stb.append("            ON GDAT.YEAR     = REGD.YEAR ");
            stb.append("           AND GDAT.GRADE    = REGD.GRADE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ");
            stb.append("            ON HDAT.YEAR     = REGD.YEAR ");
            stb.append("           AND HDAT.SEMESTER = REGD.SEMESTER ");
            stb.append("           AND HDAT.GRADE    = REGD.GRADE ");
            stb.append("           AND HDAT.HR_CLASS = REGD.HR_CLASS ");
            stb.append("   WHERE  ");
            stb.append("         REGD.YEAR    <= '"+ _param._year +"' ");
            stb.append("     AND REGD.SEMESTER = '"+ _param._semester +"'  ");
            stb.append("     AND REGD.SCHREGNO = '"+ _schregno +"'  ");
            stb.append("   ORDER BY YEAR DESC ");
            stb.append("   FETCH FIRST 3 ROWS ONLY ");
            //寮・通学
            stb.append(" ), SCHNO_DOMITORY_HIST AS( ");
            stb.append("   SELECT  ");
            stb.append("     YEAR(T2.DOMI_ENTDAY) AS YEAR, ");
            stb.append("     T2.SCHREGNO, ");
            stb.append("     T2.DOMI_CD ");
            stb.append("   FROM  ");
            stb.append("     (SELECT MAX(DOMI_ENTDAY) AS DOMI_ENTDAY, ");
            stb.append("             SCHREGNO  ");
            stb.append("        FROM SCHREG_DOMITORY_HIST_DAT ");
            stb.append("       GROUP BY YEAR(DOMI_ENTDAY),SCHREGNO ");
            stb.append("     ) T1 ");
            stb.append("   INNER JOIN SCHREG_DOMITORY_HIST_DAT T2 ");
            stb.append("           ON T2.SCHREGNO    = T1.SCHREGNO ");
            stb.append("          AND T2.DOMI_ENTDAY = T1.DOMI_ENTDAY ");
            stb.append(" ) ");
            //メイン
            stb.append(" SELECT ");
            stb.append("   T1.YEAR, ");
            stb.append("   T1.GRADE, ");
            stb.append("   T1.HR_CLASS, ");
            stb.append("   T1.GRADE_CD, ");
            stb.append("   VALUE(T1.HR_CLASS_NAME1, '') AS HR_CLASS_NAME1, ");
            stb.append("   T1.ATTENDNO, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   SDH.DOMI_CD, ");
            stb.append("   CASE WHEN SDH.DOMI_CD IS NOT NULL ");
            stb.append("        THEN DOMI.DOMI_NAME ");
            stb.append("        ELSE '通学生' ");
            stb.append("   END AS DOMI_NAME, ");
            stb.append("   SDIV.AVG AS SCORE ");
            stb.append(" FROM ");
            stb.append("   SCHNO T1 ");
            stb.append("   LEFT JOIN SCHNO_DOMITORY_HIST SDH ");
            stb.append("          ON SDH.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN DOMITORY_MST DOMI ");
            stb.append("          ON DOMI.DOMI_CD = SDH.DOMI_CD ");
            stb.append("   LEFT JOIN RECORD_RANK_SDIV_DAT SDIV ");
            stb.append("          ON SDIV.YEAR          = T1.YEAR ");
            stb.append("         AND SDIV.SEMESTER      = '"+SEMEALL+"' ");
            stb.append("         AND SDIV.TESTKINDCD    = '99' ");
            stb.append("         AND SDIV.TESTITEMCD    = '00' ");
            stb.append("         AND SDIV.SCORE_DIV     = '08' ");
            stb.append("         AND SDIV.CLASSCD       = '99' ");
            stb.append("         AND SDIV.SCHOOL_KIND   = '"+_param._schoolKind+"' ");
            stb.append("         AND SDIV.CURRICULUM_CD = '99' ");
            stb.append("         AND SDIV.SUBCLASSCD    = '"+ALL9+"' ");
            stb.append("         AND SDIV.SCHREGNO = T1.SCHREGNO ");
            if("1".equals(_param._output)) {
                //寮のみ
                stb.append(" WHERE SDH.DOMI_CD IS NOT NULL ");
            } else if("2".equals(_param._output)) {
                //通学生のみ
                stb.append(" WHERE SDH.DOMI_CD IS NULL ");
            }
            stb.append(" ORDER BY YEAR ");

            return stb.toString();
        }

        private String freshmanSqlHistSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHNO AS( ");
            stb.append("   SELECT DISTINCT ");
            stb.append("     FRESH.ENTERYEAR AS YEAR, ");
            stb.append("     FRESH.GRADE, ");
            stb.append("     FRESH.HR_CLASS, ");
            stb.append("     GDAT.GRADE_CD, ");
            stb.append("     HDAT.HR_CLASS_NAME1, ");
            stb.append("     FRESH.ATTENDNO, ");
            stb.append("     FRESH.SCHREGNO ");
            stb.append("   FROM  ");
            stb.append("     FRESHMAN_DAT FRESH ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ");
            stb.append("            ON GDAT.YEAR     = FRESH.ENTERYEAR ");
            stb.append("           AND GDAT.GRADE    = FRESH.GRADE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ");
            stb.append("            ON HDAT.YEAR     = FRESH.ENTERYEAR ");
            stb.append("           AND HDAT.SEMESTER = '"+ _param._semester +"' ");
            stb.append("           AND HDAT.GRADE    = FRESH.GRADE ");
            stb.append("           AND HDAT.HR_CLASS = FRESH.HR_CLASS ");
            stb.append("   WHERE  ");
            stb.append("         FRESH.ENTERYEAR <= '"+ _param._year +"' ");
            stb.append("     AND FRESH.SCHREGNO   = '"+ _schregno +"'  ");
            stb.append("   ORDER BY ENTERYEAR DESC ");
            stb.append("   FETCH FIRST 3 ROWS ONLY ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.YEAR, ");
            stb.append("   T1.GRADE, ");
            stb.append("   T1.HR_CLASS, ");
            stb.append("   T1.GRADE_CD, ");
            stb.append("   T1.HR_CLASS_NAME1, ");
            stb.append("   T1.ATTENDNO, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   '' AS DOMI_CD, ");
            stb.append("   CASE WHEN RD007.RECEPTNO IS NOT NULL ");
            stb.append("        THEN '寮' ");
            stb.append("        ELSE '通学生' ");
            stb.append("   END AS DOMI_NAME, ");
            stb.append("   SDIV.SCORE ");
            stb.append(" FROM ");
            stb.append("   SCHNO T1 ");
            stb.append("   LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD007 "); //寮・通学生
            stb.append("          ON RD007.ENTEXAMYEAR  = T1.YEAR ");
            stb.append("         AND RD007.APPLICANTDIV = '"+ _applicantdiv +"' ");
            stb.append("         AND RD007.TESTDIV      = '"+ _testdiv +"' ");
            stb.append("         AND RD007.EXAM_TYPE    = '"+ _exam_type +"' ");
            stb.append("         AND RD007.RECEPTNO     = '"+ _receptno +"' ");
            stb.append("         AND RD007.SEQ          = '007' ");
            stb.append("         AND RD007.REMARK5      = '1' ");
            stb.append("   LEFT JOIN RECORD_RANK_SDIV_DAT SDIV ");
            stb.append("          ON SDIV.YEAR          = T1.YEAR ");
            stb.append("         AND SDIV.SEMESTER      = '"+ SEMEALL +"' ");
            stb.append("         AND SDIV.TESTKINDCD    = '99' ");
            stb.append("         AND SDIV.TESTITEMCD    = '00' ");
            stb.append("         AND SDIV.SCORE_DIV     = '08' ");
            stb.append("         AND SDIV.CLASSCD       = '99' ");
            stb.append("         AND SDIV.SCHOOL_KIND   = '"+ _param._schoolKind +"' ");
            stb.append("         AND SDIV.CURRICULUM_CD = '99' ");
            stb.append("         AND SDIV.SUBCLASSCD    = '"+ ALL9 +"' ");
            stb.append("         AND SDIV.SCHREGNO      = T1.SCHREGNO ");
            if("1".equals(_param._output)) {
                //寮のみ
            	stb.append(" WHERE RD007.RECEPTNO IS NOT NULL ");
            } else if("2".equals(_param._output)) {
                //通学生のみ
                stb.append(" WHERE RD007.RECEPTNO IS NULL ");
            }
            stb.append(" ORDER BY T1.YEAR ");


            return stb.toString();
        }
    }


    private class StudentHist {
    	final String _year;
    	final String _grade;
    	final String _hr_class;
    	final String _hr_name;
    	final String _attendno;
    	final String _schregno;
    	final String _domi_cd;
    	final String _domi_name;
    	final String _score;
    	final String _eiken;
    	final String _eiken_score;

        private StudentHist(
        		final String year,
        		final String grade,
        		final String hr_class,
        		final String hr_name,
        		final String attendno,
        		final String schregno,
        		final String domi_cd,
        		final String domi_name,
        		final String score,
        		final String eiken,
        		final String eiken_score
        ) {
        	_year = year;
        	_grade = grade;
        	_hr_class = hr_class;
        	_hr_name = hr_name;
        	_attendno = attendno;
        	_schregno = schregno;
        	_domi_cd = domi_cd;
        	_domi_name = domi_name;
        	_score = score;
        	_eiken = eiken;
        	_eiken_score = eiken_score;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
    	log.fatal("$Revision: 74090 $");
    	KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _disp;
        final String[] _categorySelected;
        final String _gradeHrClass;
        final String _grade;
        final String _year;
        final String _semester;
        final String _schoolKind;
        final String _coursecd;
        final String _sex;
        final String _output;
        final String _applicantdiv;
        final String _nendo;
        final boolean _semes2Flg;
        final boolean _semes3Flg;
        final boolean _semes9Flg;

        private String _selectedIn = "";

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;

        private Map _semesterMap;
        private final Map _semesterDetailMap;

        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _backSlashImagePath;
        final String _whiteSpaceImagePath;
        final String _schoolStampPath;


        private boolean _isOutputDebug;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
        	_disp = request.getParameter("DISP");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _grade = request.getParameter("GRADE");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _coursecd = request.getParameter("COURSECD");
            _sex = request.getParameter("SEX");
            _output = request.getParameter("OUTPUT");

            if ("2".equals(_disp)) {
                _applicantdiv = ("1".equals(_coursecd)) ? "2" : "1"; //中学:1 高校:2
            } else {
                _applicantdiv = ("H".equals(_schoolKind)) ? "2" : "1"; //中学:1 高校:2
            }
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            _semes2Flg = "2".equals(_semester) || "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes3Flg = "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes9Flg = "9".equals(_semester) ? true : false;

            _selectedIn = "(";
            for (int i = 0; i < _categorySelected.length; i++) {
                if (_categorySelected[i] == null)
                    break;
                if (i > 0)
                    _selectedIn = _selectedIn + ",";
                _selectedIn = _selectedIn + "'" + _categorySelected[i] + "'";
            }
            _selectedIn = _selectedIn + ")";

            setCertifSchoolDat(db2);

            _semesterDetailMap = new HashMap();

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            _backSlashImagePath = getImageFilePath("slash_bs.jpg");
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
            _schoolStampPath = getImageFilePath("SCHOOLSTAMP.bmp");
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD187N' AND NAME = '" + propName + "' "));
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
        	final String certifKindcd = "J".equals(_schoolKind) ? "103" : "104";
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '"+ certifKindcd +"' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
        }

        public String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }
    }
}

// eof
