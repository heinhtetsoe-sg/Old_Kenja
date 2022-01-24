/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 74d0d97c2c4a1495fe41c5c46dadd99c3d0a3db5 $
 *
 * 作成日: 2020/02/20
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJC;

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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJC171 {

    private static final Log log = LogFactory.getLog(KNJC171.class);

    private static final String SEMEALL = "9";

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
        final List chairList = getList(db2);
        for (Iterator iterator = chairList.iterator(); iterator.hasNext();) {
            final Chair chair = (Chair) iterator.next();

            //出欠表
            printSvfMain(db2, svf, chair);

        }
    }

    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final Chair chair) {
        final String form = "KNJC171.frm";
        svf.VrSetForm(form , 4);

        //明細部以外を印字
        printTitle(db2, svf, chair);

        //明細
        for (Iterator it = chair._studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            svf.VrsOut("HR_NAME", student._hr_name + student._attendno); //年組番
            svf.VrsOut("NAME", student._name); //氏名
            svf.VrsOut("SE", student._sex); //性別
            svf.VrsOut("KANA", student._name_kana); //ふりがな
            svf.VrsOut("ENG", student._name_eng); //ローマ字
            svf.VrEndRecord();
            _hasData = true;
        }
        svf.VrEndPage();
    }

    private void printTitle(final DB2UDB db2, final Vrw32alp svf, final Chair chair) {
        //明細部以外を印字

    	String kousyu = "";
        //ヘッダ
    	if(chair._subclasscd.contains("H")) {
    		kousyu = "高校";
    	}
    	if(chair._subclasscd.contains("J")) {
    		kousyu = "中学";
    	}

    	svf.VrsOut("TITLE", kousyu + "自習課題　出欠表"); //タイトル
        svf.VrsOut("TR_NAME", chair._staffname); //担任名
        svf.VrsOut("SUBCLASS_NAME", chair._subclassname); //科目名
        svf.VrsOut("CHAIR_NAME", chair._chairname); //科目名
        final String date = KNJ_EditDate.h_format_JP(db2, _param._date);
        final String week = KNJ_EditDate.h_format_W(_param._date);
        final String printDate = date+"("+week+") " + _param._periodName;
        svf.VrsOut("DATE", printDate); //日時
        svf.VrsOut("ROOM_NAME", chair._facilityname); //教室
        svf.VrsOut("NUM", "(" + chair._count + "人)"); //人数

    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final String[] value) {
        if (null != value) {
            for (int i = 0 ; i < value.length; i++) {
                svf.VrsOutn(field, i + 1, value[i]);
            }
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getChairSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Chair chair = new Chair();
                chair._year = StringUtils.defaultString(rs.getString("YEAR"));
                chair._semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                chair._chaircd = StringUtils.defaultString(rs.getString("CHAIRCD"));
                chair._chairname = StringUtils.defaultString(rs.getString("CHAIRNAME"));
                chair._staffcd = StringUtils.defaultString(rs.getString("STAFFCD"));
                chair._staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                chair._subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                chair._subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                chair._faccd = StringUtils.defaultString(rs.getString("FACCD"));
                chair._facilityname = StringUtils.defaultString(rs.getString("FACILITYNAME"));
                chair._count = StringUtils.defaultString(rs.getString("COUNT"));
                chair._studentList = chair.setScoreList(db2);
                retList.add(chair);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getChairSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SEMESTER, ");
        stb.append("   T1.CHAIRCD, ");
        stb.append("   T1.CHAIRNAME, ");
        stb.append("   T2.STAFFCD, ");
        stb.append("   T3.STAFFNAME, ");
        if("1".equals(_param._useCurriculumcd)) {
        	stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
        	stb.append("   T1.SUBCLASSCD AS SUBCLASSCD, ");
        }
        stb.append("   T4.SUBCLASSNAME, ");
        stb.append("   T5.FACCD, ");
        stb.append("   T6.FACILITYNAME, ");
        stb.append("   T7.COUNT ");
        stb.append(" FROM  ");
        stb.append("   CHAIR_DAT T1 ");
        stb.append("   LEFT JOIN ( SELECT YEAR,SEMESTER,CHAIRCD,MIN(STAFFCD) AS STAFFCD ");
        stb.append("                 FROM CHAIR_STF_DAT ");
        stb.append("                WHERE CHARGEDIV = '1' ");
        stb.append("                GROUP BY YEAR,SEMESTER,CHAIRCD ");
        stb.append("             ) T2 ");
        stb.append("          ON T2.YEAR      = T1.YEAR ");
        stb.append("         AND T2.SEMESTER  = T1.SEMESTER ");
        stb.append("         AND T2.CHAIRCD   = T1.CHAIRCD ");
        stb.append("   LEFT JOIN STAFF_MST T3 ");
        stb.append("          ON T3.STAFFCD = T2.STAFFCD ");
        stb.append("   LEFT JOIN SUBCLASS_MST T4 ");
        stb.append("          ON T4.SUBCLASSCD    = T1.SUBCLASSCD ");
        if("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND T4.CLASSCD       = T1.CLASSCD ");
            stb.append("         AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("   LEFT JOIN CHAIR_FAC_DAT T5 ");
        stb.append("          ON T5.YEAR     = T1.YEAR ");
        stb.append("         AND T5.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T5.CHAIRCD  = T1.CHAIRCD ");
        stb.append("   LEFT JOIN FACILITY_MST T6 ");
        stb.append("          ON T6.FACCD = T5.FACCD ");
        stb.append("   LEFT JOIN ( SELECT YEAR,SEMESTER,CHAIRCD,APPDATE,COUNT(SCHREGNO) AS COUNT ");
        stb.append("                 FROM CHAIR_STD_DAT ");
        stb.append("                WHERE YEAR = '"+ _param._year +"' ");
        stb.append("                      AND SEMESTER = '"+ _param._semester +"' ");
        stb.append("                      AND '"+ _param._date +"' BETWEEN APPDATE AND APPENDDATE ");
        stb.append("                GROUP BY YEAR,SEMESTER,CHAIRCD,APPDATE ");
        stb.append("             ) T7 ");
        stb.append("          ON T7.YEAR       = T1.YEAR ");
        stb.append("         AND T7.SEMESTER   = T1.SEMESTER ");
        stb.append("         AND T7.CHAIRCD    = T1.CHAIRCD ");
        stb.append(" WHERE  ");
        stb.append("       T1.YEAR     = '"+ _param._year +"'  ");
        stb.append("   AND T1.SEMESTER = '"+ _param._semester +"' ");
        if("1".equals(_param._useCurriculumcd)) {
            stb.append("   AND T1.CHAIRCD || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN " + SQLUtils.whereIn(true, _param._categorySelected));
        } else {
        	stb.append("   AND T1.CHAIRCD || '-' || T1.SUBCLASSCD IN " + SQLUtils.whereIn(true, _param._categorySelected));
        }
        stb.append(" ORDER BY  ");
        stb.append("   CHAIRCD, ");
        stb.append("   SUBCLASSCD ");
        log.debug(" chair sql = " + stb.toString());

        return stb.toString();
    }

    private class Chair {
        String _year;
        String _semester;
        String _chaircd;
        String _chairname;
        String _staffcd;
        String _staffname;
        String _subclasscd;
        String _subclassname;
        String _faccd;
        String _facilityname;
        String _count;
        List _studentList;

        private List setScoreList(final DB2UDB db2) {
            final List retList = new ArrayList();
            final String sql = studentSql();
            log.debug(" studentSql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = StringUtils.defaultString(rs.getString("YEAR"));
                    final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                    final String grade = StringUtils.defaultString(rs.getString("GRADE"));
                    final String hr_class = StringUtils.defaultString(rs.getString("HR_CLASS"));
                    final String hr_name = StringUtils.defaultString(rs.getString("HR_NAME"));
                    final String attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) + "番" : rs.getString("ATTENDNO");
                    final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                    final String name = StringUtils.defaultString(rs.getString("NAME"));
                    final String sex = StringUtils.defaultString(rs.getString("SEX"));
                    final String name_kana = StringUtils.defaultString(rs.getString("NAME_KANA"));
                    final String name_eng = StringUtils.defaultString(rs.getString("NAME_ENG"));
                    final Student student= new Student(year, semester, grade, hr_class, hr_name, attendno, schregno, name, sex, name_kana, name_eng);

                    retList.add(student);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return retList;
        }

        private String studentSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     REGD.YEAR, ");
            stb.append("     REGD.SEMESTER, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     HDAT.HR_NAME, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     Z002.ABBV1 AS SEX, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     BASE.NAME_ENG ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT T1 ");
            stb.append("     INNER JOIN CHAIR_STF_DAT T3 ");
            stb.append("             ON T3.YEAR       = T1.YEAR ");
            stb.append("            AND T3.SEMESTER   = T1.SEMESTER ");
            stb.append("            AND T3.CHAIRCD    = T1.CHAIRCD ");
            stb.append("     INNER JOIN SCH_CHR_DAT T4 ");
            stb.append("             ON T4.EXECUTEDATE = '"+ _param._date +"' ");
            stb.append("            AND T4.PERIODCD    = '"+ _param._period +"' ");
            stb.append("            AND T4.CHAIRCD     = T1.CHAIRCD ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ");
            stb.append("             ON REGD.YEAR     = T1.YEAR ");
            stb.append("            AND REGD.SEMESTER = T1.SEMESTER ");
            stb.append("            AND REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT HDAT ");
            stb.append("             ON HDAT.YEAR     = REGD.YEAR ");
            stb.append("            AND HDAT.SEMESTER = REGD.SEMESTER ");
            stb.append("            AND HDAT.GRADE    = REGD.GRADE ");
            stb.append("            AND HDAT.HR_CLASS = REGD.HR_CLASS ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ");
            stb.append("             ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN NAME_MST Z002 ");
            stb.append("            ON Z002.NAMECD2 = BASE.SEX ");
            stb.append("           AND Z002.NAMECD1 = 'Z002' ");
            stb.append(" WHERE ");
            stb.append("       T1.YEAR     = '"+ _param._year +"' ");
            stb.append("   AND T1.SEMESTER = '"+ _param._semester +"' ");
            stb.append("   AND T1.CHAIRCD  = '"+ _chaircd +"' ");
            stb.append("   AND '"+ _param._date +"' BETWEEN T1.APPDATE AND T1.APPENDDATE ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     REGD.SCHREGNO ");
            return stb.toString();
        }

    }

    private static class Student {
        final String _year;
        final String _semester;
        final String _grade;
        final String _hr_class;
        final String _hr_name;
        final String _attendno;
        final String _schregno;
        final String _name;
        final String _sex;
        final String _name_kana;
        final String _name_eng;
        private Student(
                final String year,
                final String semester,
                final String grade,
                final String hr_class,
                final String hr_name,
                final String attendno,
                final String schregno,
                final String name,
                final String sex,
                final String name_kana,
                final String name_eng
        ) {
            _year = year;
            _semester = semester;
            _grade = grade;
            _hr_class = hr_class;
            _hr_name = hr_name;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
            _sex = sex;
            _name_kana = name_kana;
            _name_eng = name_eng;
        }

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 75460 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _date;
        final String _period;
        final String _useCurriculumcd;
        final String[] _categorySelected;

        final boolean _semes2Flg;
        final boolean _semes3Flg;
        final boolean _semes9Flg;
        final String _nendo;
        String _periodName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");            //ログイン年度
            _semester = request.getParameter("SEMESTER");    //ログイン学期
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));    //日時
            _period = request.getParameter("PERIOD");        //校時
            _categorySelected = request.getParameterValues("category_selected");
            _useCurriculumcd = request.getParameter("useCurriculumcd"); //教育課程

            _semes2Flg = "2".equals(_semester) || "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes3Flg = "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes9Flg = "9".equals(_semester) ? true : false;
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";

            _periodName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'B001' AND NAMECD2 = '" + _period + "'"));
        }
    }

}

// eof
