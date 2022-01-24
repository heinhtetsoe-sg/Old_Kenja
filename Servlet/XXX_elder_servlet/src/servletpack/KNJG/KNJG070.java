// kanji=漢字
/*
 * $Id: 9de1efc8ac9841e4b7744aa15aeaba0cb70eae1e $
 *
 * 作成日: 2009/11/12 15:52:06 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJG;

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

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 9de1efc8ac9841e4b7744aa15aeaba0cb70eae1e $
 */
public class KNJG070 {

    private static final Log log = LogFactory.getLog("KNJG070.class");

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
        final List printStudents = getPrintStudent(db2);
        for (final Iterator it = printStudents.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            //フォームおよび文
            printStudent(svf, student);
            //学校
            printSchool(svf);
            //出力
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printStudent(final Vrw32alp svf, final Student student) {
        //学科名の文字数によりフォームと文を切り替える
        String major1 = student._majorName;
        String major2 = null;
        String form = null;
        boolean useLine3 = false, useLine2_1 = false, useLine2_2 = false, useLine2_3 = false;
        boolean nospace = false;
        int spaceIndex = major1 != null ? major1.indexOf(' ') : -1;
        if (spaceIndex == -1) {
            spaceIndex = major1 != null ? major1.indexOf('　') : -1;
        }
        // 名称の最後以外に半角スペースか全角スペースがあれば
        if (spaceIndex != -1 && spaceIndex != major1.length() - 1) {
            major2 = major1.substring(spaceIndex + 1);
            major1 = major1.substring(0, spaceIndex);

            // スペース以降の文字数によってフォームを切り替える。
            if (major2.length() <= 4) { 
                useLine2_3 = true;
            } else if (major2.length() <= 8) { 
                useLine2_1 = true;
            } else if (major2.length() <= 10) {
                useLine2_2 = true;
            } else {
                useLine3 = true;
            }
        } else {
            nospace = true;
        }
        //フォームおよび文
        if (useLine3) {
            form = "KNJG070_2TORI.frm"; // 3行フォーム
            svf.VrSetForm(form, 1);
            svf.VrsOut("MAIN1", "　本校の " + student._courseName + " " + major1);
            svf.VrsOut("MAIN2", major2 + " " + student._gradeName + " への" + student._nyugakuName + "を");
            svf.VrsOut("MAIN3", "許可する");
        } else if (useLine2_1) {
            form = "KNJG070_1TORI.frm"; // 2行フォーム
            svf.VrSetForm(form, 1);
            svf.VrsOut("MAIN1", "　本校の " + student._courseName + " " + major1  + " " + major2);
            svf.VrsOut("MAIN2", student._gradeName + " への" + student._nyugakuName + "を許可する");
        } else if (useLine2_2) {
            form = "KNJG070_3TORI.frm"; // 2行フォーム2
            svf.VrSetForm(form, 1);
            svf.VrsOut("MAIN1", "　本校の " + student._courseName + " " + major1 + " " + major2);
            svf.VrsOut("MAIN2", student._gradeName + " への" + student._nyugakuName + "を許可する");
        } else if (useLine2_3) {
            form = "KNJG070_1TORI.frm"; // 2行フォーム
            svf.VrSetForm(form, 1);
            svf.VrsOut("MAIN1", "　本校の " + student._courseName + " " + major1 + " " + major2 + " " + student._gradeName);
            svf.VrsOut("MAIN2", " への" + student._nyugakuName + "を許可する");
        } else if (nospace) {
            form = "KNJG070_1TORI.frm"; // 2行フォーム
            svf.VrSetForm(form, 1);
            svf.VrsOut("MAIN1", "　本校の " + student._courseName + " " + major1 + " " + student._gradeName + " への");
            svf.VrsOut("MAIN2", student._nyugakuName + "を許可する");
        }
        //生徒
        svf.VrsOut(student.getFieldName(), student._name);
        svf.VrsOut("BIRTHDAY", student.getBirthday());
    }

    private void printSchool(final Vrw32alp svf) {
        //学校
        svf.VrsOut("DATE", _param._entryDate);
        svf.VrsOut("SCHOOLNAME", _param._schoolName);
        svf.VrsOut("JOBNAME", _param._jobName);
        svf.VrsOut("STAFFNAME", _param._principalName);
        svf.VrsOut("SYOSYO_NAME", _param._syosyoName);//証書名
        svf.VrsOut("SYOSYO_NAME2", _param._syosyoName2);//証書名2
        if ("0".equals(_param._certifNo)) {
            svf.VrsOut("CERTIF_NO", "");//証書番号の印刷 0:あり,1:なし
        }
    }

    private List getPrintStudent(final DB2UDB db2) throws SQLException {
        final List rtnList = new ArrayList();
        final int schlen = (_param._printSinnyu) ? 1 : _param._schregno.length;
        for (int i = 0; i < schlen; i++) {
            final String schno = (_param._printSinnyu) ? "" : _param._schregno[i];
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = getStudentSql(schno);
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                    final String birthday = rs.getString("BIRTHDAY");
                    final String majorName = rs.getString("MAJORNAME");
                    final String courseName = rs.getString("COURSENAME") != null ? rs.getString("COURSENAME") + "課程" : "";
                    final String gradeName = "第 " + rs.getString("GRADE") + " 学年";
                    final String nyugakuName = rs.getString("NYUGAKU_NAME");
                    final Student student = new Student(
                            schregno,
                            name,
                            birthday,
                            majorName,
                            courseName,
                            gradeName,
                            nyugakuName
                            );
                    rtnList.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        return rtnList;
    }

    private String getStudentSql(final String schno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     smallint(T1.GRADE) as GRADE, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.REAL_NAME, ");
        stb.append("     T2.BIRTHDAY, ");
        stb.append("     L1.MAJORNAME, ");
        stb.append("     L2.COURSENAME, ");
        stb.append("     (CASE WHEN L3.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
        if (_param._printSinnyu) {
            stb.append(" '入学' AS NYUGAKU_NAME ");
        } else {
            stb.append(" A002.NAME1 AS NYUGAKU_NAME ");
        }
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ");
        if (_param._printSinnyu) {
            stb.append("                              AND T2.ENT_DIV NOT IN ('4','5') ");
        }
        stb.append("     LEFT JOIN NAME_MST A002 ON A002.NAMECD1 = 'A002' AND A002.NAMECD2 = T2.ENT_DIV ");
        stb.append("     LEFT JOIN MAJOR_MST L1 ON L1.COURSECD = T1.COURSECD AND L1.MAJORCD = T1.MAJORCD ");
        stb.append("     LEFT JOIN COURSE_MST L2 ON L2.COURSECD = T1.COURSECD ");
        stb.append("     LEFT JOIN SCHREG_NAME_SETUP_DAT L3 ON L3.SCHREGNO = T1.SCHREGNO AND L3.DIV = '01' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        if (_param._printSinnyu) {
            stb.append(" AND T1.GRADE = '01' ");
        } else {
            stb.append(" AND T1.SCHREGNO = '" + schno + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHREGNO ");
        return stb.toString();
    }

    private class Student {
        final String _schregno;
        final String _name;
        final String _birthday;
        final String _majorName;
        final String _courseName;
        final String _gradeName;
        final String _nyugakuName;

        Student(final String schregno,
                final String name,
                final String birthday,
                final String majorName,
                final String courseName,
                final String gradeName,
                final String nyugakuName
        ) {
            _schregno = schregno;
            _name = name;
            _birthday = birthday;
            _majorName = majorName;
            _courseName = courseName;
            _gradeName = gradeName;
            _nyugakuName = nyugakuName;
        }

        private String getFieldName() {
            if (null == _name) {
                return "NAME1";
            } else {
                return 12 < _name.length() ? "NAME2" : "NAME1";
            }
        }

        private String getBirthday() {
            final String date = _birthday;
            if (null == date) {
                return "";
            }
            if (_param._isSeireki) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date) + "生";
            } else {
                return KNJ_EditDate.h_format_JP(date) + "生";
            }
        }

    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
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
        private final boolean _printSinnyu;
        private final boolean _printTenHennyu;
        private String[] _schregno;
        private String _sDate;
        private String _eDate;
        private final String _entryDate;
        private final String _entryYear;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private boolean _isSeireki;
        private String _schoolName;
        private String _jobName;
        private String _principalName;
        private String _syosyoName;
        private String _syosyoName2;
        private String _certifNo;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            String output   = request.getParameter("SINNYU_TENNYU"); // 1:新入生, 2:転入生または編入生
            _printSinnyu    = "1".equals(output);
            _printTenHennyu = "2".equals(output);
            if (_printTenHennyu) {
                _schregno = request.getParameterValues("CATEGORY_SELECTED");
                final String sDate = request.getParameter("SDATE");
                _sDate = sDate.replace('/', '-');
                final String eDate = request.getParameter("EDATE");
                _eDate = eDate.replace('/', '-');
            }
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            setSeirekiFlg(db2);
            final String entryDate = request.getParameter("ENTRY_DATE");
            _entryDate = printDate(entryDate);
            _entryYear = getEntryYear(entryDate);
            setSchoolInfo(db2);
        }

        private void setSeirekiFlg(final DB2UDB db2) {
            try {
                _isSeireki = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    final String str = rs.getString("NAME1");
                    if ("2".equals(str)) _isSeireki = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        private String printDate(final String date) {
            if (null == date) {
                return "";
            }
            if (_isSeireki) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(date);
            }
        }

        private void setSchoolInfo(final DB2UDB db2) {
            try {
                _schoolName = "";
                _jobName = "";
                _principalName = "";
                _syosyoName = "";
                _syosyoName2 = "";
                _certifNo = "";
                String sql = "SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, SYOSYO_NAME, SYOSYO_NAME2, CERTIF_NO " +
                             "FROM CERTIF_SCHOOL_DAT " +
                             "WHERE YEAR = '" + _entryYear + "' AND CERTIF_KINDCD = '114' ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    _schoolName = rs.getString("SCHOOL_NAME");
                    _jobName = rs.getString("JOB_NAME");
                    _principalName = rs.getString("PRINCIPAL_NAME");
                    _syosyoName = rs.getString("SYOSYO_NAME");
                    _syosyoName2 = rs.getString("SYOSYO_NAME2");
                    _certifNo = rs.getString("CERTIF_NO");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        private String getEntryYear(final String date) {
            if (null == date || "".equals(date)) {
                return _ctrlYear;
            }
            int month = Integer.parseInt(date.substring(5, 7));
            if (month < 4) {
                int year = Integer.parseInt(date.substring(0, 4)) - 1;
                return String.valueOf(year);
            } else {
                return date.substring(0, 4);
            }
        }

    }
}

// eof
