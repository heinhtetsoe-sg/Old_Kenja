// kanji=漢字
/*
 * $Id: e299bd7a7c7f39e5f70ddb62538f1bcb5ba466ff $
 *
 * 作成日: 2010/03/01 18:27:09 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.WithusUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: e299bd7a7c7f39e5f70ddb62538f1bcb5ba466ff $
 */
public class KNJWD800 {

    private static final Log log = LogFactory.getLog("KNJWD800.class");

    private static final String FORM_FILE = "KNJWD800.frm";
    private static final int MAX_LINE = 50;
    private boolean _hasData;

    Param _param;

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        svf.VrSetForm(FORM_FILE, 1);
        String befBelong = "";
        int lineCnt = 1;
        final List printStudent = getPrintStuedent(db2);
        for (final Iterator iter = printStudent.iterator(); iter.hasNext();) {
            final PrintData printData = (PrintData) iter.next();
            if ((!"".equals(befBelong) && !befBelong.equals(printData._belongingName)) ||
                 lineCnt > MAX_LINE
            ) {
                svf.VrEndPage();
                lineCnt = 1;
            }
            svf.VrsOut("BELONGING_NAME", "所属:" + printData._belongingName);
            svf.VrsOut("DATE", _param._sakuseiDate);

            svf.VrsOutn("SCHREGNO", lineCnt, printData._schregNo);
            final String nameField = printData._name.length() > 10 ? "2" : "1";
            svf.VrsOutn("NAME1_" + nameField, lineCnt, printData._name);
            svf.VrsOutn("STUDENT_DIV", lineCnt, printData._stdivName);
            svf.VrsOutn("CLASS", lineCnt, printData._className);
            final String subClassField = printData._subClassName.length() > 10 ? "2" : "1";
            svf.VrsOutn("SUBCLASS1_" + subClassField, lineCnt, printData._subClassName);

            svf.VrsOutn("CREDIT", lineCnt, printData._credits);
            svf.VrsOutn("CREDIT_DIV", lineCnt, printData._syutokuDiv);
            svf.VrsOutn("COMP_NENDO", lineCnt, printData._year);
            lineCnt++;
            befBelong = printData._belongingName;
            _hasData = true;
        }
        if (lineCnt > 1) {
            svf.VrEndPage();
        }
    }

    private List getPrintStuedent(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final String sql = getStuedentSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String gradValue = rs.getString("GRAD_VALUE");
                final String year = "0".equals(gradValue) ? "" : rs.getString("YEAR");
                final String schregNo = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String name = rs.getString("NAME");
                final String stdivName = rs.getString("STDIV_NAME");
                final String className = rs.getString("CLASSNAME");
                final String subClassName = rs.getString("SUBCLASSNAME");
                final String credits = rs.getString("CREDITS");
                final String belongingName = rs.getString("SCHOOLNAME1");
                final String syutokuDiv = "0".equals(gradValue) ? "未" : "履";
                final PrintData printData = new PrintData(year, schregNo, grade, name, stdivName, className, subClassName, gradValue, credits, belongingName, syutokuDiv); 
                retList.add(printData);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStuedentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     CRE_A.YEAR, ");
        stb.append("     SCH_R.SCHREGNO, ");
        stb.append("     SCH_R.GRADE, ");
        stb.append("     SCH_B.NAME, ");
        stb.append("     STDIV.NAME AS STDIV_NAME, ");
        stb.append("     CL_M.CLASSNAME, ");
        stb.append("     SUB_M.SUBCLASSNAME, ");
        stb.append("     VALUE(CRE_A.GRAD_VALUE, 0) AS GRAD_VALUE, ");
        stb.append("     SUB_D.CREDITS, ");
        stb.append("     BEL_M.SCHOOLNAME1 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT SCH_R ");
        stb.append("     INNER JOIN SCHREG_BASE_MST SCH_B ON SCH_R.SCHREGNO = SCH_B.SCHREGNO ");
        stb.append("     LEFT JOIN STUDENTDIV_MST STDIV ON SCH_R.COURSE_DIV = STDIV.COURSE_DIV ");
        stb.append("          AND SCH_R.STUDENT_DIV = STDIV.STUDENT_DIV ");
        stb.append("     LEFT JOIN BELONGING_MST BEL_M ON SCH_R.GRADE = BEL_M.BELONGING_DIV ");
        stb.append("     INNER JOIN REC_CREDIT_ADMITS CRE_A ON SCH_R.YEAR = CRE_A.YEAR ");
        stb.append("           AND SCH_R.SCHREGNO = CRE_A.SCHREGNO ");
        stb.append("           AND CRE_A.CLASSCD || CRE_A.CURRICULUM_CD || CRE_A.SUBCLASSCD NOT IN ('" + WithusUtils.PHYSICAL_EDUCATION_CLASS_CD + WithusUtils.PHYSICAL_EDUCATION_OLD_CURRICULUM + WithusUtils.PHYSICAL_EDUCATION_SUBCLASS_CD + "', '" + WithusUtils.PHYSICAL_EDUCATION_CLASS_CD + WithusUtils.PHYSICAL_EDUCATION_CURRICULUM + WithusUtils.PHYSICAL_EDUCATION_SUBCLASS_CD + "', '" + WithusUtils.PHYSICAL_EDUCATION_CLASS_CD + WithusUtils.PHYSICAL_EDUCATION_NEW_CURRICULUM + WithusUtils.PHYSICAL_EDUCATION_SUBCLASS_CD + "') ");
        stb.append("           AND VALUE(CRE_A.GRAD_VALUE, 0) <= 1 ");
        stb.append("     LEFT JOIN CLASS_MST CL_M ON CRE_A.CLASSCD = CL_M.CLASSCD ");
        stb.append("     LEFT JOIN SUBCLASS_MST SUB_M ON CRE_A.CLASSCD = SUB_M.CLASSCD ");
        stb.append("          AND CRE_A.CURRICULUM_CD = SUB_M.CURRICULUM_CD ");
        stb.append("          AND CRE_A.SUBCLASSCD = SUB_M.SUBCLASSCD ");
        stb.append("     LEFT JOIN SUBCLASS_DETAILS_MST SUB_D ON CRE_A.YEAR = SUB_D.YEAR ");
        stb.append("          AND CRE_A.CLASSCD = SUB_D.CLASSCD ");
        stb.append("          AND CRE_A.CURRICULUM_CD = SUB_D.CURRICULUM_CD ");
        stb.append("          AND CRE_A.SUBCLASSCD = SUB_D.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     SCH_R.YEAR = '" + _param._year + "' AND ");
        stb.append("     SCH_R.SEMESTER = '" + _param._semester + "' AND ");
        if ("1".equals(_param._div)) {
            stb.append("     SCH_R.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        } else {
            stb.append("     SCH_R.GRADE IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        }
        stb.append(" ORDER BY ");
        stb.append("     SCH_R.GRADE, ");
        stb.append("     SCH_R.SCHREGNO, ");
        stb.append("     CRE_A.SUBCLASSCD ");
        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class PrintData {
        final String _year;
        final String _schregNo;
        final String _grade;
        final String _name;
        final String _stdivName;
        final String _className;
        final String _subClassName;
        final String _gradValue;
        final String _credits;
        final String _belongingName;
        final String _syutokuDiv;

        public PrintData(
                final String year,
                final String schregNo,
                final String grade,
                final String name,
                final String stdivName,
                final String className,
                final String subClassName,
                final String gradValue,
                final String credits,
                final String belongingName,
                final String syutokuDiv
        ) {
            _year = year;
            _schregNo = schregNo;
            _grade = grade;
            _name = name;
            _stdivName = stdivName;
            _className = className;
            _subClassName = subClassName;
            _gradValue = gradValue;
            _credits = credits;
            _belongingName = belongingName;
            _syutokuDiv = syutokuDiv;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _div;
        private final String _grade;
        private final String _studentDiv;
        private final String _annual;
        private final String[] _categorySelected;
        private final String _loginDate;
        private final String _sakuseiDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _div = request.getParameter("DIV");
            _grade = request.getParameter("GRADE");
            _studentDiv = request.getParameter("STUDENT_DIV");
            _annual = request.getParameter("ANNUAL");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");

            _loginDate = request.getParameter("LOGIN_DATE");
            _sakuseiDate = KNJ_EditDate.h_format_JP(_loginDate);
        }

    }
}

// eof
