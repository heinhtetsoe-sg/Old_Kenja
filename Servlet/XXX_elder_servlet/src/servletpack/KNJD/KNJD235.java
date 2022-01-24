// kanji=漢字
/*
 * $Id: 8063a270ed632fae53fb5fb079652ad76ced26ba $
 *
 * 作成日: 2007/07/02 17:19:09 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2007-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * クラス別履修・修得単位／評定平均チェックリスト。
 * @author nakamoto
 * @version $Id: 8063a270ed632fae53fb5fb079652ad76ced26ba $
 */
public class KNJD235 {

    private static final String FORM_NAME = "KNJD235.frm";

    private static final Log log = LogFactory.getLog(KNJD235.class);

    Param _param;
    /**
     * KNJD.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(request, db2);
            boolean hasData = false;

            svf.VrSetForm(FORM_NAME, 4);
            log.debug("印刷するフォーム:" + FORM_NAME);
            //クラス毎に改ページ
            svf.VrAttribute("HR_NAME","FF=1");

            for (int i = 0; i < _param._hrClass.length; i++) {
                if (printMain(db2, svf, _param._hrClass[i])) hasData = true;
            }
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            close(db2, svf);
        }
    }

    /** 印刷処理メイン */
    private boolean printMain(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String hrClass
    ) throws Exception {
        boolean rtnflg = false;

        final List students = createStudents(db2, hrClass);
        log.debug("生徒数=" + students.size());

        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            //ヘッダ等
            printHeader(svf, student);
            //履修単位・修得単位・評定平均
            printCreditAndValue(db2, svf, student);
            
            svf.VrEndRecord();
            rtnflg = true;
        }

        return rtnflg;
    }

    private List createStudents(final DB2UDB db2, final String hrClass) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = sqlStudents(hrClass);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String attendNo = rs.getString("ATTENDNO");
                final String hrName = rs.getString("HR_NAME");
                final String staffName = rs.getString("STAFFNAME");

                final Student student = new Student(schregno, name, attendNo, hrName, staffName);
                rtn.add(student);
            }
        } catch (final Exception ex) {
            log.error("生徒のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }
    
    private String sqlStudents(final String hrClass) {
        final String sql;
        sql = "select"
            + "    T1.SCHREGNO,"
            + "    T1.ATTENDNO,"
            + "    T2.NAME,"
            + "    T3.HR_NAME,"
            + "    case when S1.STAFFNAME is not null and S2.STAFFNAME is not null and S3.STAFFNAME is not null"
            + "         then S1.STAFFNAME || '、' || S2.STAFFNAME || '、' || S3.STAFFNAME"
            + "         when S1.STAFFNAME is not null and S2.STAFFNAME is not null"
            + "         then S1.STAFFNAME || '、' || S2.STAFFNAME"
            + "         when S1.STAFFNAME is not null"
            + "         then S1.STAFFNAME"
            + "         else null end as STAFFNAME"
            + "  from SCHREG_REGD_DAT T1"
            + "       inner join SCHREG_BASE_MST T2 on T2.SCHREGNO = T1.SCHREGNO"
            + "       inner join SCHREG_REGD_HDAT T3 on T3.YEAR = T1.YEAR"
            + "                                     and T3.SEMESTER = T1.SEMESTER"
            + "                                     and T3.GRADE = T1.GRADE"
            + "                                     and T3.HR_CLASS = T1.HR_CLASS"
            + "       left join STAFF_MST S1 on S1.STAFFCD = T3.TR_CD1"
            + "       left join STAFF_MST S2 on S2.STAFFCD = T3.TR_CD2"
            + "       left join STAFF_MST S3 on S3.STAFFCD = T3.TR_CD3"
            + "  where"
            + "    T1.YEAR = '" + _param._year + "' and"
            + "    T1.SEMESTER = '" + _param._semester + "' and"
            + "    T1.GRADE || T1.HR_CLASS = '" + hrClass + "'"
            + "  order by"
            + "    T1.GRADE, T1.HR_CLASS, T1.ATTENDNO"
            ;
        return sql;
    }

    private void printHeader(final Vrw32alp svf, final Student student) {
        //ヘッダ
        String gengou = _param._gengou;
        String loginDate = _param._loginDate;
        String staffName = student._staffName;
//        log.debug("gengou="+gengou+", loginDate="+loginDate+", staffName="+staffName);
        svf.VrsOut("NENDO", gengou);
        svf.VrsOut("DATE", loginDate);
        svf.VrsOut("STAFFNAME", staffName);
        //明細
        String hrName = student._hrName;
        String attendNo = student.getAttendNo();
        String name = student._name;
        String fieldNo = (15 < name.length()) ? "2" : "1";
//        log.debug("hrName="+hrName+", attendNo="+attendNo+", name="+name+", nameLen="+name.length());
        svf.VrsOut("HR_NAME", hrName);
        svf.VrsOut("ATTENDNO", attendNo);
        svf.VrsOut("NAME" + fieldNo, name);
    }

    private void printCreditAndValue(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlCreditAndValue(student);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String schregno = rs.getString("SCHREGNO");
                String compCredit = rs.getString("COMP_CREDIT");
                String getCredit = rs.getString("GET_CREDIT");
                String addCredit = rs.getString("ADD_CREDIT");
                String valuation = rs.getString("VALUATION");
                String credit = "";
                if (getCredit != null && addCredit != null) {
                    credit = String.valueOf(Integer.parseInt(getCredit) + Integer.parseInt(addCredit));
                } else if (getCredit != null) {
                    credit = String.valueOf(Integer.parseInt(getCredit));
                } else if (addCredit != null) {
                    credit = String.valueOf(Integer.parseInt(addCredit));
                }
                log.debug(schregno+", "+compCredit+", "+getCredit+", "+addCredit+", "+valuation+", "+credit);
                svf.VrsOut("COMP_CREDIT", compCredit);
                svf.VrsOut("GET_CREDIT", credit);
                svf.VrsOut("AVERAGE", valuation);
            }
        } catch (final Exception ex) {
            log.error("履修・修得・評定のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }
    
    private String sqlCreditAndValue(final Student student) {
        final StringBuffer stb = new StringBuffer();
        
        final String valuationCase = "case when 0 < VALUATION then VALUATION end";
        final String credit = " (CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END) ";
        final String creditCase = "case when 0 < VALUATION then " + credit + " end";

        stb.append(" WITH TBL1 AS ( ");
        stb.append(" SELECT  SCHREGNO, ");
        stb.append("         T1.YEAR, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || VALUE(SUBM.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD, ");
        } else {
            stb.append("         VALUE(SUBM.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD, ");
        }
        stb.append("      case when COUNT(*) = 1 then MAX(VALUATION)");//１レコードの場合、評定はそのままの値。
        stb.append("           when T2.GVAL_CALC = '0' then ROUND(AVG(FLOAT("+valuationCase+")),0)");
        stb.append("           when T2.GVAL_CALC = '1' and 0 < SUM("+creditCase+") then ROUND(FLOAT(SUM(("+valuationCase+")* " + credit + "))/SUM("+creditCase+"),0)");
        stb.append("           else MAX(VALUATION) end AS VALUATION ");

        stb.append(" FROM    SCHREG_STUDYREC_DAT T1 ");
        stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("   AND  SUBM.CLASSCD = T1.CLASSCD ");
            stb.append("   AND  SUBM.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("   AND  SUBM.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append(" LEFT JOIN SCHOOL_MST T2 ON T2.YEAR = T1.YEAR ");
        stb.append(" WHERE   T1.YEAR < '" + _param._year + "' ");
        stb.append("   AND   SCHREGNO = '" + student._schregno + "' ");
        stb.append("   AND  (COMP_CREDIT IS NOT NULL ");
        stb.append("    OR   GET_CREDIT IS NOT NULL ");
        stb.append("    OR   ADD_CREDIT IS NOT NULL ");
        stb.append("    OR   VALUATION IS NOT NULL) ");
        stb.append(" GROUP BY  SCHREGNO, ");
        stb.append("           T1.YEAR, ");
        stb.append("           T2.GVAL_CALC, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || VALUE(SUBM.SUBCLASSCD2, T1.SUBCLASSCD) ");
        } else {
            stb.append("         VALUE(SUBM.SUBCLASSCD2, T1.SUBCLASSCD) ");
        }
        stb.append(" UNION ALL ");
        stb.append(" SELECT  SCHREGNO, ");
        stb.append("         YEAR, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("         SUBCLASSCD, ");
        }
        stb.append("         case when '90' <= SUBSTR(SUBCLASSCD,1,2) then null else GRAD_VALUE end AS VALUATION ");
        stb.append(" FROM    RECORD_DAT ");
        stb.append(" WHERE   YEAR = '" + _param._year + "' ");
        stb.append("   AND   SCHREGNO = '" + student._schregno + "' ");
        if ("1".equals(_param._useCurriculumcd) && "1".equals(_param._useClassDetailDat)) {
            stb.append("   AND   CLASSCD || '-' || SCHOOL_KIND NOT IN (SELECT CLASSCD || '-' || SCHOOL_KIND ");
            stb.append("           FROM CLASS_DETAIL_DAT WHERE YEAR='" + _param._year + "' AND CLASS_SEQ = '003') ");
        } else {
            stb.append("   AND   SUBSTR(SUBCLASSCD,1,2) NOT IN (SELECT NAMECD2 FROM V_NAME_MST WHERE YEAR='" + _param._year + "' AND NAMECD1='" + _param._d008Namecd1 + "') ");
        }
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("   AND   CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD NOT IN (SELECT ");
            stb.append("         ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD ");
        } else {
            stb.append("   AND   SUBCLASSCD NOT IN (SELECT ");
            stb.append("         ATTEND_SUBCLASSCD ");
        }
        stb.append("                            FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR='" + _param._year + "' AND REPLACECD='1') ");
        stb.append("   AND  (COMP_CREDIT IS NOT NULL ");
        stb.append("    OR   GET_CREDIT IS NOT NULL ");
        stb.append("    OR   ADD_CREDIT IS NOT NULL ");
        stb.append("    OR   GRAD_VALUE IS NOT NULL) ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT  SCHREGNO, ");
        stb.append("         YEAR, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("         SUBCLASSCD, ");
        }
        stb.append("         CASE WHEN SCHREGNO IS NULL THEN 0 ELSE NULL END AS VALUATION ");
        stb.append(" FROM    SCHREG_QUALIFIED_DAT ");
        stb.append(" WHERE   YEAR = '" + _param._year + "' ");
        stb.append("   AND   SCHREGNO = '" + student._schregno + "' ");
        stb.append("   AND   CREDITS IS NOT NULL ");
        stb.append(" ) ");
        
        stb.append(" , TBL2 AS ( ");
        stb.append(" SELECT  SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("         SUBCLASSCD, ");
        }
        stb.append("         COMP_CREDIT, ");
        stb.append("         GET_CREDIT, ");
        stb.append("         ADD_CREDIT ");
        stb.append(" FROM    SCHREG_STUDYREC_DAT ");
        stb.append(" WHERE   YEAR < '" + _param._year + "' ");
        stb.append("   AND   SCHREGNO = '" + student._schregno + "' ");
        stb.append("   AND  (COMP_CREDIT IS NOT NULL ");
        stb.append("    OR   GET_CREDIT IS NOT NULL ");
        stb.append("    OR   ADD_CREDIT IS NOT NULL ");
        stb.append("    OR   VALUATION IS NOT NULL) ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT  SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("         SUBCLASSCD, ");
        }
        stb.append("         COMP_CREDIT, ");
        stb.append("         GET_CREDIT, ");
        stb.append("         ADD_CREDIT ");
        stb.append(" FROM    RECORD_DAT ");
        stb.append(" WHERE   YEAR = '" + _param._year + "' ");
        stb.append("   AND   SCHREGNO = '" + student._schregno + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("   AND   CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD NOT IN (SELECT ");
            stb.append("         ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD ");
        } else {
            stb.append("   AND   SUBCLASSCD NOT IN (SELECT ");
            stb.append("         ATTEND_SUBCLASSCD ");
        }
        stb.append("                            FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR='" + _param._year + "' AND REPLACECD='1') ");
        stb.append("   AND  (COMP_CREDIT IS NOT NULL ");
        stb.append("    OR   GET_CREDIT IS NOT NULL ");
        stb.append("    OR   ADD_CREDIT IS NOT NULL ");
        stb.append("    OR   GRAD_VALUE IS NOT NULL) ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT  SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("         SUBCLASSCD, ");
        }
        stb.append("         CREDITS AS COMP_CREDIT, ");
        stb.append("         CREDITS AS GET_CREDIT, ");
        stb.append("         CASE WHEN SCHREGNO IS NULL THEN 0 ELSE NULL END AS ADD_CREDIT ");
        stb.append(" FROM    SCHREG_QUALIFIED_DAT ");
        stb.append(" WHERE   YEAR = '" + _param._year + "' ");
        stb.append("   AND   SCHREGNO = '" + student._schregno + "' ");
        stb.append("   AND   CREDITS IS NOT NULL ");
        stb.append(" ), TBL AS ( ");
        stb.append(" SELECT  SCHREGNO ");
        stb.append(" FROM    TBL1 ");
        stb.append(" UNION ");
        stb.append(" SELECT  SCHREGNO ");
        stb.append(" FROM    TBL2 ");
        stb.append(" ) ");
        stb.append(" SELECT  T1.SCHREGNO, ");
        stb.append("         L2.COMP_CREDIT, ");
        stb.append("         L2.GET_CREDIT, ");
        stb.append("         L2.ADD_CREDIT, ");
        stb.append("         L1.SUM_VAL, ");
        stb.append("         L1.CNT_VAL, ");
        stb.append("         L1.VALUATION ");
        stb.append(" FROM     TBL T1 ");
        stb.append(" LEFT JOIN (SELECT SCHREGNO, DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,5,1) AS VALUATION, SUM(VALUATION) AS SUM_VAL, COUNT(VALUATION) AS CNT_VAL FROM TBL1 GROUP BY SCHREGNO) L1 ON L1.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN (SELECT SCHREGNO, SUM(GET_CREDIT) AS GET_CREDIT, ");
        stb.append("                             SUM(COMP_CREDIT) AS COMP_CREDIT, ");
        stb.append("                             SUM(ADD_CREDIT) AS ADD_CREDIT FROM TBL2 GROUP BY SCHREGNO) L2 ON L2.SCHREGNO = T1.SCHREGNO ");
        return stb.toString();
    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void close(final DB2UDB db2, final Vrw32alp svf) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
        if (null != svf) {
            svf.VrQuit();
        }
    }

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 74345 $ $Date: 2020-05-15 19:32:44 +0900 (金, 15 5 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String grade = request.getParameter("GRADE");
        final String hrClass[] = request.getParameterValues("CLASS_SELECTED");
        final String loginDate = request.getParameter("LOGIN_DATE");
        final String useCurriculumcd = request.getParameter("useCurriculumcd");
        final String useClassDetailDat = request.getParameter("useClassDetailDat");

        final Param param = new Param(
        		db2,
                year,
                semester,
                grade,
                hrClass,
                loginDate,
                useCurriculumcd,
                useClassDetailDat
        );
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _gengou;
        private final String _semester;
        private final String _grade;
        private final String _schoolKind;
        private final String _loginDate;
        private final String[] _hrClass;
        private final String _useCurriculumcd;
        private final String _useClassDetailDat;
        private final String _d008Namecd1;

        public Param(
        		final DB2UDB db2,
                final String year,
                final String semester,
                final String grade,
                final String[] hrClass,
                final String loginDate,
                final String useCurriculumcd,
                final String useClassDetailDat
        ) {
            _year = year;
            _semester = semester;
            _grade = grade;
            _hrClass = hrClass;
            _loginDate = KNJ_EditDate.h_format_JP(loginDate);
            _useCurriculumcd = useCurriculumcd;
            _useClassDetailDat = useClassDetailDat;
            final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(year));
            _gengou = gengou + "年度";

    		_schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
            final String tmpD008Cd = "D" + _schoolKind + "08";
            String d008Namecd2CntStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COUNT(*) FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + tmpD008Cd + "' "));
            int d008Namecd2Cnt = Integer.parseInt(StringUtils.defaultIfEmpty(d008Namecd2CntStr, "0"));
            _d008Namecd1 = d008Namecd2Cnt > 0 ? tmpD008Cd : "D008";
        }
    }

    private class Student {
        private final String _schregno;
        private final String _name;
        private final String _attendNo;
        private final String _hrName;
        private final String _staffName;

        public Student(
                final String schregno,
                final String name,
                final String attendNo,
                final String hrName,
                final String staffName
        ) {
            _schregno = schregno;
            _name = name;
            _attendNo = attendNo;
            _hrName = hrName;
            _staffName = staffName;
        }

        public String getAttendNo() {
            return String.valueOf(Integer.parseInt(_attendNo));
        }

        public String toString() {
            return _schregno + ":" + _name;
        }
    }
}
