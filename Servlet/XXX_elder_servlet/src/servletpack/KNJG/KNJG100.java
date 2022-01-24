// kanji=漢字
/*
 * $Id: a8f27174819fcac134d09105671cb25088ca2c21 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJG;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;


/*
 *
 *  学校教育システム 賢者 [事務管理]
 *
 *      在学証明書（和）     Form-ID:KNJG010_4   証明書種別:004
 */

public class KNJG100 {
    
    private static final Log log = LogFactory.getLog(KNJG100.class);

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2(); // 帳票におけるＳＶＦおよびＤＢ２の設定

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;

        sd.setSvfInit(request, response, svf);

        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error! ");
            return;
        }
        final Param param = getParam(request, db2);

        // 印刷処理
        boolean nonedata = printSvf(db2, svf, param);

        // 終了処理
        sd.closeSvf(svf, nonedata);
        sd.closeDb(db2);
    }

    private Param getParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }
    
    private boolean printSvf(final DB2UDB db2, final Vrw32alp svf, final Param param) {

        boolean nonedata = false;
        
        final KNJG010_1T printObj = new KNJG010_1T(db2, svf, param._definecode);
        final String flgs = ("3".equals(param._kubun) ? "1" : "0") + "";
        printObj.pre_stat(flgs);

        final List students = Student.createStudents(db2, param);
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final String[] aparam = new String[101];
            
            String certifKind = null;
            if ("P".equals(student._schoolKind)) {
                certifKind = "024";
            } else if ("J".equals(student._schoolKind)) {
                certifKind = "012";
            } else if ("H".equals(student._schoolKind)) {
                certifKind = "004";
            }
            
            aparam[0] = student._schregno;
            aparam[1] = certifKind;
            if ("3".equals(param._kubun)) {
                aparam[2] = String.valueOf(Integer.parseInt(param._ctrlYear) + 1);
                aparam[3] = "1";
            } else {
                aparam[2] = param._ctrlYear;
                aparam[3] = param._ctrlSemester;
            }
            aparam[4] = null;
            aparam[5] = null;
            aparam[6] = null;
            aparam[7] = null;
            aparam[8] = param._kisaiDate;
            aparam[9] = null;
            aparam[10] = null;
            aparam[11] = param._ctrlYear;
            aparam[12] = null;
            aparam[13] = null;
            aparam[14] = null;
            aparam[15] = null;
            aparam[16] = null;
            aparam[17] = null;
            if (printObj.printSvfMain(aparam, param._ctrlYear)) {
                nonedata = true;
            }
        }
        
        printObj.pre_stat_f();

        return nonedata;
    }
    
    private static class Student {
        final String _schregno;
        final String _name;
        final String _schoolKind;
        private Student(final String schregno, final String name, final String schoolKind) {
            _schregno = schregno;
            _name = name;
            _schoolKind = schoolKind;
        }

        public static List createStudents(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                if ("3".equals(param._kubun)) {
                    stb.append(" SELECT ");
                    stb.append("     T1.SCHREGNO, ");
                    stb.append("     T1.NAME, ");
                    stb.append("     T3.SCHOOL_KIND ");
                    stb.append(" FROM ");
                    stb.append("     FRESHMAN_DAT T1 ");
                    stb.append("     INNER JOIN CLASS_FORMATION_DAT T2 ON T2.YEAR = T1.ENTERYEAR AND T2.SCHREGNO = T1.SCHREGNO ");
                    stb.append("     LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.ENTERYEAR AND T3.GRADE = T2.GRADE ");
                    stb.append(" WHERE ");
                    stb.append("     T1.ENTERYEAR = '" + String.valueOf(Integer.parseInt(param._ctrlYear) + 1) + "' ");
                    stb.append("     AND T2.GRADE = '" + param._grade + "' ");
                    stb.append(" ORDER BY ");
                    stb.append("     T1.HR_CLASS, ");
                    stb.append("     T1.ATTENDNO, ");
                    stb.append("     T1.NAME_KANA ");
                } else if ("1".equals(param._kubun) || "2".equals(param._kubun)) {
                    stb.append("SELECT T1.SCHREGNO, T2.NAME, T3.SCHOOL_KIND ");
                    stb.append("FROM SCHREG_REGD_DAT T1 ");
                    stb.append("INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
                    stb.append("LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ");
                    stb.append("WHERE T1.YEAR = '" + param._ctrlYear + "' ");
                    stb.append("AND T1.SEMESTER = '" + param._ctrlSemester + "' ");
                    if ("1".equals(param._kubun)) {
                        stb.append("AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, param._classSelected) + " ");
                    } else {
                        final String[] schregno = new String[param._classSelected.length];
                        for (int i = 0; i < param._classSelected.length; i++) {
                            schregno[i] = StringUtils.split(param._classSelected[i], "-")[0];
                        }
                        stb.append("AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, schregno) + " ");
                    }
                    stb.append("ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
                }
                //log.debug(" sql = " + stb.toString());
                ps = db2.prepareStatement(stb.toString()); // 任意のHR組の学籍番号取得用
                rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(new Student(rs.getString("SCHREGNO"), rs.getString("NAME"), rs.getString("SCHOOL_KIND")));
                }
                log.debug(" print list size = " + list.size());
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return list;
        }
    }

    private static class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String[] _classSelected;
        final String _grade;
        final String _gradeHrClass;
        final String _kisaiDate;
        final String _kubun;
        final KNJDefineSchool _definecode;

        public Param(final HttpServletRequest request, final DB2UDB db2) {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _grade = request.getParameter("GRADE");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _kisaiDate = request.getParameter("KISAI_DATE");
            _kubun = request.getParameter("KUBUN");
            _definecode = new KNJDefineSchool();
            try {
                _definecode.defineCode(db2, _ctrlYear);
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
        }
    }
}    
