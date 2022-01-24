// kanji=漢字
/*
 * 作成日: 2021/01/12
 * 作成者: shimoji
 *
 * Copyright(C) 2005-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.File;
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＡ１４３Ｕ＿８＞  生徒証（東京女学館）
 **/

public class KNJA143U_8 {

    private static final Log log = LogFactory.getLog(KNJA143U_8.class);

    private boolean _hasData;

    private Param _param;

    private static final String SCHOOL_KIND_H = "101";

    private static final String SCHOOL_KIND_J = "102";

    private static final int STUDENT_LINE_MAX = 4;

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        svf.VrSetForm(_param._useFormNameA143U + ".frm", 1);

        List<List<Student>> studentLists = getStudentList(db2);

        for (List<Student> sList : studentLists) {
            int lineCnt = 1;

            for (Student student : sList) {
                svf.VrsOutn("TITLE", lineCnt, "身 分 証 明 書");

                /**
                 * 通学情報の印字
                 */
                svf.VrsOutn("LIMIT2", lineCnt, KNJ_EditDate.h_format_JP(db2, _param._limitDate) + "まで有効");

                int fieldByte = KNJ_EditEdit.getMS932ByteLength(student._josya1);
                String fieldName = fieldByte > 14 ? "3" : fieldByte > 10 ? "2" : "1";
                svf.VrsOutn("SECTION1_1_" + fieldName, lineCnt, student._josya1);

                fieldByte = KNJ_EditEdit.getMS932ByteLength(student.getGesya1());
                fieldName = fieldByte > 14 ? "3" : fieldByte > 10 ? "2" : "1";
                svf.VrsOutn("SECTION1_2_" + fieldName, lineCnt, student.getGesya1());

                fieldByte = KNJ_EditEdit.getMS932ByteLength(student.getKeiyu1());
                fieldName = fieldByte > 14 ? "3" : fieldByte > 10 ? "2" : "1";
                svf.VrsOutn("SECTION1_3_" + fieldName, lineCnt, student.getKeiyu1());

                fieldByte = KNJ_EditEdit.getMS932ByteLength(student._josya3);
                fieldName = fieldByte > 14 ? "3" : fieldByte > 10 ? "2" : "1";
                svf.VrsOutn("SECTION2_1_" + fieldName, lineCnt, student._josya3);

                fieldByte = KNJ_EditEdit.getMS932ByteLength(student.getGesya2());
                fieldName = fieldByte > 14 ? "3" : fieldByte > 10 ? "2" : "1";
                svf.VrsOutn("SECTION2_2_" + fieldName, lineCnt, student.getGesya2());

                fieldByte = KNJ_EditEdit.getMS932ByteLength(student.getKeiyu2());
                fieldName = fieldByte > 14 ? "3" : fieldByte > 10 ? "2" : "1";
                svf.VrsOutn("SECTION2_3_" + fieldName, lineCnt, student.getKeiyu2());

                /**
                 * 生徒情報の印字
                 */
                svf.VrsOutn("COURSE_NAME", lineCnt, student.getKatei());
                svf.VrsOutn("SCHOOL_KIND", lineCnt, student.getSchoolKindName());
                svf.VrsOutn("PHOTO_BMP", lineCnt, student.getPhotoBmp());
                svf.VrsOutn("SCHREGNO", lineCnt, student._schregnoShow);
                svf.VrsOutn("GRADE", lineCnt, student.getGrade());
                if (_param._nextYearGradeFlg == null) {
                    svf.VrsOutn("HR_NAME", lineCnt, StringUtils.defaultString(student._hrClassName1) + student._attendno);
                }
                fieldByte = KNJ_EditEdit.getMS932ByteLength(student.getName()) + 6;
                String name = student.getName();
                int spaceCnt = 0;
                if (fieldByte > 30) {
                    fieldName = "3";
                    spaceCnt = 34 - student.getNameFullWidthCnt();
                } else if (fieldByte > 24) {
                    fieldName = "2";
                    spaceCnt = 24 - student.getNameFullWidthCnt();
                } else {
                    fieldName = "1";
                    spaceCnt = 18 - student.getNameFullWidthCnt();
                }
                name = String.format("%-" + spaceCnt + "s", name) + "(" + student._age + "才)";
                svf.VrsOutn("NAME1_" + fieldName, lineCnt, name);
                String[] warekiBirthday = KNJ_EditDate.tate_format4(db2, student._birthday);
                svf.VrsOutn("ERA1", lineCnt, warekiBirthday[0]);
                svf.VrsOutn("BIRTHDAY1", lineCnt, warekiBirthday[1]);
                svf.VrsOutn("BIRTHDAY2", lineCnt, warekiBirthday[2]);
                svf.VrsOutn("BIRTHDAY3", lineCnt, warekiBirthday[3]);
                svf.VrsOutn("ADDRESS1", lineCnt, student.getAddr1());
                svf.VrsOutn("ADDRESS2", lineCnt, student.getAddr2());
                String[] warekiIssueDate = KNJ_EditDate.tate_format4(db2, _param._issueDate);
                svf.VrsOutn("ERA2", lineCnt, warekiIssueDate[0]);
                svf.VrsOutn("SDATE1", lineCnt, warekiIssueDate[1]);
                svf.VrsOutn("SDATE2", lineCnt, warekiIssueDate[2]);
                svf.VrsOutn("SDATE3", lineCnt, warekiIssueDate[3]);
                /**
                 * 学校情報の印字
                 */
                fieldByte = KNJ_EditEdit.getMS932ByteLength(student._schoolAddr);
                fieldName = fieldByte > 32 ? "2" : "1";
                svf.VrsOutn("SCHOOLADDRESS" + fieldName, lineCnt, student._schoolAddr);
                svf.VrsOutn("SCHOOLNAME1", lineCnt, student._schoolName);
                fieldByte = KNJ_EditEdit.getMS932ByteLength(student._principalName);
                fieldName = fieldByte > 18 ? "2" : "1";
                svf.VrsOutn("PRINCIPAL_NAME" + fieldName, lineCnt, student._principalName);
                svf.VrsOutn("TELNO", lineCnt, student._schoolTelno);
                svf.VrsOutn("STAMP", lineCnt, student.getStampFilePath());

                lineCnt++;
                _hasData = true;
            }

            svf.VrEndPage();
        }
    }

    private List<List<Student>> getStudentList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<List<Student>> studentLists = new ArrayList<List<Student>>();
        List<Student> sList = new ArrayList<Student>();;
        studentLists.add(sList);

        try {
            final String studentSql = getStudentSql();
            log.debug(" sql =" + studentSql);
            ps = db2.prepareStatement(studentSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String josya1 = rs.getString("JOSYA_1");
                final String gesya1 = rs.getString("GESYA_1");
                final String josya2 = rs.getString("JOSYA_2");
                final String gesya2 = rs.getString("GESYA_2");
                final String josya3 = rs.getString("JOSYA_3");
                final String gesya3 = rs.getString("GESYA_3");
                final String josya4 = rs.getString("JOSYA_4");
                final String gesya4 = rs.getString("GESYA_4");
                final String schregno = rs.getString("SCHREGNO");
                final String schregnoShow = rs.getString("SCHREGNO_SHOW");
                final String grade = rs.getString("GRADE");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String hrClassName1 = rs.getString("HR_CLASS_NAME1");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String realName = rs.getString("REAL_NAME");
                final String setupExistsFlg = rs.getString("SETUP_EXISTS_FLG");
                final String birthday = rs.getString("BIRTHDAY");
                final String age = rs.getString("AGE");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                final String schoolAddr = rs.getString("SCHOOL_ADDR");
                final String schoolName = rs.getString("SCHOOL_NAME");
                final String principalName = rs.getString("PRINCIPAL_NAME");
                final String schoolTelno = rs.getString("SCHOOL_TELNO");

                final Student student = new Student(
                        josya1, gesya1, josya2, gesya2,
                        josya3, gesya3, josya4, gesya4,
                        schregno, schregnoShow, grade,
                        schoolKind, hrClassName1, attendno,
                        name, realName, setupExistsFlg,
                        birthday, age, addr1, addr2,
                        schoolAddr, schoolName, principalName, schoolTelno);

                if (STUDENT_LINE_MAX <= sList.size()) {
                    sList = new ArrayList<Student>();
                    studentLists.add(sList);
                }

                sList.add(student);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return studentLists;
    }

    /**生徒又は学校情報**/
    private String getStudentSql()
    {
        String gradePlusFlg = "1".equals(_param._nextYearGradeFlg) ? "1" : "0";
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAX_ADDRESS AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ADDRESS_DAT ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO ");
        stb.append(" ), ");
        stb.append(" ADDRESS AS ( ");
        stb.append("     SELECT ");
        stb.append("         ADDR.SCHREGNO, ");
        stb.append("         ADDR.ADDR1, ");
        stb.append("         ADDR.ADDR2 ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ADDRESS_DAT ADDR ");
        stb.append("         INNER JOIN MAX_ADDRESS ON ");
        stb.append("                    MAX_ADDRESS.SCHREGNO  = ADDR.SCHREGNO ");
        stb.append("                AND MAX_ADDRESS.ISSUEDATE = ADDR.ISSUEDATE ");
        stb.append(" ), ");
        // 次年度チェックボックスがONなら＋１学年の情報を、OFFなら元の学年の情報を取得する。
        // 中学３年のように＋１学年が高校１年なら、高校１年の情報を取得する。（校種は J ではなく H を取得する）
        // 高校３年のように＋１学年がない場合は元の学年を取得する
        stb.append(" GRADE_P1 AS ( ");
        stb.append("     SELECT ");
        stb.append("         YEAR, ");
        stb.append("         GRADE, ");
        stb.append("         CASE WHEN GRADE >= '09' THEN ");
        stb.append("             CAST(CAST(GRADE AS SMALLINT) + 1 AS VARCHAR(2)) ");
        stb.append("         ELSE ");
        stb.append("             '0' || CAST(CAST(GRADE AS SMALLINT) + 1 AS VARCHAR(2)) ");
        stb.append("         END AS GRADE_P1 ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_GDAT ");
        stb.append(" ), ");
        stb.append(" GRADE_T AS ( ");
        stb.append("     SELECT ");
        stb.append("         GRADE_P1.YEAR, ");
        stb.append("         GRADE_P1.GRADE, ");
        stb.append("         CASE WHEN '" + gradePlusFlg + "' = '1' THEN CASE WHEN GDAT_S.GRADE       IS NOT NULL THEN GDAT_S.GRADE       ELSE GDAT_M.GRADE       END ELSE GDAT_M.GRADE       END AS GRADE_HENKAN, ");
        stb.append("         CASE WHEN '" + gradePlusFlg + "' = '1' THEN CASE WHEN GDAT_S.SCHOOL_KIND IS NOT NULL THEN GDAT_S.SCHOOL_KIND ELSE GDAT_M.SCHOOL_KIND END ELSE GDAT_M.SCHOOL_KIND END AS SCHOOL_KIND_HENKAN ");
        stb.append("     FROM ");
        stb.append("         GRADE_P1 ");
        stb.append("         LEFT JOIN SCHREG_REGD_GDAT GDAT_M ON ");
        stb.append("                   GDAT_M.YEAR  = GRADE_P1.YEAR ");
        stb.append("               AND GDAT_M.GRADE = GRADE_P1.GRADE ");
        stb.append("         LEFT JOIN SCHREG_REGD_GDAT GDAT_S ON ");
        stb.append("                   GDAT_S.YEAR  = GRADE_P1.YEAR ");
        stb.append("               AND GDAT_S.GRADE = GRADE_P1.GRADE_P1 ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     CASE WHEN ENVIR.FLG_1 = '1' THEN STATION_J1.STATION_NAME ELSE ENVIR.JOSYA_1 END AS JOSYA_1, ");
        stb.append("     CASE WHEN ENVIR.FLG_1 = '1' THEN STATION_G1.STATION_NAME ELSE ENVIR.GESYA_1 END AS GESYA_1, ");
        stb.append("     ENVIR.FLG_1, ");
        stb.append("     CASE WHEN ENVIR.FLG_2 = '1' THEN STATION_J2.STATION_NAME ELSE ENVIR.JOSYA_2 END AS JOSYA_2, ");
        stb.append("     CASE WHEN ENVIR.FLG_2 = '1' THEN STATION_G2.STATION_NAME ELSE ENVIR.GESYA_2 END AS GESYA_2, ");
        stb.append("     ENVIR.FLG_2, ");
        stb.append("     CASE WHEN ENVIR.FLG_3 = '1' THEN STATION_J3.STATION_NAME ELSE ENVIR.JOSYA_3 END AS JOSYA_3, ");
        stb.append("     CASE WHEN ENVIR.FLG_3 = '1' THEN STATION_G3.STATION_NAME ELSE ENVIR.GESYA_3 END AS GESYA_3, ");
        stb.append("     ENVIR.FLG_3, ");
        stb.append("     CASE WHEN ENVIR.FLG_4 = '1' THEN STATION_J4.STATION_NAME ELSE ENVIR.JOSYA_4 END AS JOSYA_4, ");
        stb.append("     CASE WHEN ENVIR.FLG_4 = '1' THEN STATION_G4.STATION_NAME ELSE ENVIR.GESYA_4 END AS GESYA_4, ");
        stb.append("     ENVIR.FLG_4, ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     RIGHT(BASE.SCHREGNO, 7) AS SCHREGNO_SHOW, ");
        stb.append("     VALUE(GRADE_T.GRADE_HENKAN, 0) GRADE, ");
        stb.append("     GRADE_T.SCHOOL_KIND_HENKAN AS SCHOOL_KIND, ");
        stb.append("     HDAT.HR_CLASS_NAME1, ");
        stb.append("     VALUE(DAT.ATTENDNO, 0) AS ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.REAL_NAME, ");
        stb.append("     CASE WHEN SETUP.SCHREGNO IS NULL THEN '0' ELSE '1' END AS SETUP_EXISTS_FLG, ");
        stb.append("     BASE.BIRTHDAY, ");
        stb.append("     CASE WHEN BASE.BIRTHDAY IS NOT NULL THEN YEAR('" + _param._issueDate + "' - BIRTHDAY) END AS AGE, ");
        stb.append("     ADDRESS.ADDR1, ");
        stb.append("     ADDRESS.ADDR2, ");
        stb.append("     CERTIF.REMARK1 AS SCHOOL_ADDR, ");
        stb.append("     CERTIF.SCHOOL_NAME, ");
        stb.append("     CERTIF.PRINCIPAL_NAME, ");
        stb.append("     CERTIF.REMARK3 AS SCHOOL_TELNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST BASE ");
        stb.append("     LEFT JOIN SCHREG_ENVIR_DAT ENVIR ON ");
        stb.append("               ENVIR.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN STATION_NETMST STATION_J1 ON ");
        stb.append("               STATION_J1.STATION_CD = ENVIR.JOSYA_1 ");
        stb.append("           AND ENVIR.FLG_1 = '1' ");
        stb.append("     LEFT JOIN STATION_NETMST STATION_G1 ON ");
        stb.append("               STATION_G1.STATION_CD = ENVIR.GESYA_1 ");
        stb.append("           AND ENVIR.FLG_1 = '1' ");
        stb.append("     LEFT JOIN STATION_NETMST STATION_J2 ON ");
        stb.append("               STATION_J2.STATION_CD = ENVIR.JOSYA_2 ");
        stb.append("           AND ENVIR.FLG_2 = '1' ");
        stb.append("     LEFT JOIN STATION_NETMST STATION_G2 ON ");
        stb.append("               STATION_G2.STATION_CD = ENVIR.GESYA_2 ");
        stb.append("           AND ENVIR.FLG_2 = '1' ");
        stb.append("     LEFT JOIN STATION_NETMST STATION_J3 ON ");
        stb.append("               STATION_J3.STATION_CD = ENVIR.JOSYA_3 ");
        stb.append("           AND ENVIR.FLG_3 = '1' ");
        stb.append("     LEFT JOIN STATION_NETMST STATION_G3 ON ");
        stb.append("               STATION_G3.STATION_CD = ENVIR.GESYA_3 ");
        stb.append("           AND ENVIR.FLG_3 = '1' ");
        stb.append("     LEFT JOIN STATION_NETMST STATION_J4 ON ");
        stb.append("               STATION_J4.STATION_CD = ENVIR.JOSYA_4 ");
        stb.append("           AND ENVIR.FLG_4 = '1' ");
        stb.append("     LEFT JOIN STATION_NETMST STATION_G4 ON ");
        stb.append("               STATION_G4.STATION_CD = ENVIR.GESYA_4 ");
        stb.append("           AND ENVIR.FLG_4 = '1' ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT DAT ON ");
        stb.append("               DAT.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON ");
        stb.append("               HDAT.YEAR     = DAT.YEAR ");
        stb.append("           AND HDAT.SEMESTER = DAT.SEMESTER ");
        stb.append("           AND HDAT.GRADE    = DAT.GRADE ");
        stb.append("           AND HDAT.HR_CLASS = DAT.HR_CLASS ");
        stb.append("     LEFT JOIN GRADE_T ON ");
        stb.append("               GRADE_T.YEAR  = HDAT.YEAR ");
        stb.append("           AND GRADE_T.GRADE = HDAT.GRADE ");
        stb.append("     LEFT JOIN CERTIF_SCHOOL_DAT CERTIF ON ");
        stb.append("               CERTIF.YEAR          = GRADE_T.YEAR ");
        stb.append("           AND CERTIF.CERTIF_KINDCD = DECODE(GRADE_T.SCHOOL_KIND_HENKAN, 'H', '" + SCHOOL_KIND_H + "', '" + SCHOOL_KIND_J + "') ");
        stb.append("     LEFT JOIN SCHREG_NAME_SETUP_DAT SETUP ON ");
        stb.append("               SETUP.SCHREGNO = BASE.SCHREGNO ");
        stb.append("           AND SETUP.DIV      = '01' ");
        stb.append("     LEFT JOIN ADDRESS ON ");
        stb.append("               ADDRESS.SCHREGNO = BASE.SCHREGNO ");

        stb.append(" WHERE ");
        stb.append("     DAT.YEAR     = '" + _param._year + "' ");
        stb.append(" AND DAT.SEMESTER = '" + _param._semester + "' ");
        stb.append(" AND ");
        if ("1".equals(_param._disp)) {
            stb.append(SQLUtils.whereIn(true, "DAT.GRADE || DAT.HR_CLASS", _param._category_selected));
        } else {
            stb.append(SQLUtils.whereIn(true, "DAT.SCHREGNO", _param._category_selected));
        }

        stb.append(" ORDER BY ");
        stb.append("     VALUE(DAT.GRADE, 0), ");
        stb.append("     DAT.HR_CLASS, ");
        stb.append("     VALUE(DAT.ATTENDNO, 0) ");

        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class Student {
        final String _josya1;
        final String _gesya1;
        final String _josya2;
        final String _gesya2;
        final String _josya3;
        final String _gesya3;
        final String _josya4;
        final String _gesya4;
        final String _schregno;
        final String _schregnoShow;
        final String _grade;
        final String _schoolKind;
        final String _hrClassName1;
        final String _attendno;
        final String _name;
        final String _realName;
        final String _setupExistsFlg;
        final String _birthday;
        final String _age;
        final String _addr1;
        final String _addr2;
        final String _schoolAddr;
        final String _schoolName;
        final String _principalName;
        final String _schoolTelno;

        Student(
            final String josya1,
            final String gesya1,
            final String josya2,
            final String gesya2,
            final String josya3,
            final String gesya3,
            final String josya4,
            final String gesya4,
            final String schregno,
            final String schregnoShow,
            final String grade,
            final String schoolKind,
            final String hrClassName1,
            final String attendno,
            final String name,
            final String realName,
            final String setupExistsFlg,
            final String birthday,
            final String age,
            final String addr1,
            final String addr2,
            final String schoolAddr,
            final String schoolName,
            final String principalName,
            final String schoolTelno
        ) {
            _josya1 = josya1;
            _gesya1 = gesya1;
            _josya2 = josya2;
            _gesya2 = gesya2;
            _josya3 = josya3;
            _gesya3 = gesya3;
            _josya4 = josya4;
            _gesya4 = gesya4;
            _schregno = schregno;
            _schregnoShow = schregnoShow;
            _grade = grade;
            _schoolKind = schoolKind;
            _hrClassName1 = hrClassName1;
            _attendno = attendno;
            _name = name;
            _realName = realName;
            _setupExistsFlg = setupExistsFlg;
            _birthday = birthday;
            _age = age;
            _addr1 = addr1;
            _addr2 = addr2;
            _schoolAddr = schoolAddr;
            _schoolName = schoolName;
            _principalName = principalName;
            _schoolTelno = schoolTelno;
        }

        String getGesya1() {
            String rtn = "";

            if (_gesya1 != null) {
                if (_gesya1.equals(_josya2)) {
                    rtn = _gesya2;
                } else {
                    rtn = _gesya1;
                }
            }

            return rtn;
        }

        String getKeiyu1() {
            String rtn = "";

            if (_gesya1 != null) {
                if (_gesya1.equals(_josya2)) {
                    rtn = _gesya1;
                }
            }

            return rtn;
        }

        String getGesya2() {
            String rtn = "";

            if (_gesya3 != null) {
                if (_gesya3.equals(_josya4)) {
                    rtn = _gesya4;
                } else {
                    rtn = _gesya3;
                }
            }

            return rtn;
        }

        String getKeiyu2() {
            String rtn = "";

            if (_gesya3 != null) {
                if (_gesya3.equals(_josya4)) {
                    rtn = _gesya3;
                }
            }

            return rtn;
        }

        String getPhotoBmp() {
            String rtn = "";

            final String photoBmp = _param.getImageFilePath("P" + _schregno + "." + _param._extension);
            if (null != photoBmp) {
                rtn = photoBmp;
            }

            return rtn;
        }

        String getGrade() {
            String rtn = "";

            if (_grade != null) {
                if ("H".equals(_schoolKind)) {
                    rtn = String.valueOf(Integer.parseInt(_grade) - 3);
                } else {
                    rtn = _grade;
                }
            }

            return rtn;
        }

        String getName() {
            String rtn = "";

            if ("1".equals(_setupExistsFlg)) {
                rtn = StringUtils.defaultString(_realName);
            } else {
                rtn = StringUtils.defaultString(_name);
            }

            return rtn;
        }

        /**
         * 名前の全角文字数を返す。
         *
         * @return 名前の全角文字数
         */
        int getNameFullWidthCnt() {
            String name = getName();
            int nameLength = 0;

            for (int i = 0; i < name.length(); i++) {
                String one = name.substring(i, i + 1);
                int oneLength = KNJ_EditEdit.getMS932ByteLength(one);
                if (oneLength > 1) {
                    nameLength++;
                }
            }
            return nameLength;
        }

        String getAddr1() {
            String rtn = "";

            if (_addr2 != null) {
                rtn = _addr1;
            }

            return rtn;
        }

        String getAddr2() {
            String rtn = "";

            if (_addr2 == null) {
                rtn = _addr1;
            } else {
                rtn = _addr2;
            }

            return rtn;
        }

        String getKatei() {
            return "H".equals(_schoolKind) ? "高等課程" : "義務課程";
        }

        String getSchoolKindName() {
            return "H".equals(_schoolKind) ? "高等学校" : "中 学 校";
        }

        String getStampFilePath() {
            String rtn = "";

            final String stampFilePath = _param.getImageFilePath("SCHOOLSTAMP_" + _schoolKind + ".bmp");
            if (null != stampFilePath) {
                rtn = stampFilePath;
            }

            return rtn;
        }
    }

    private static class Param {
        private final String _year;
        private final String _semester;
        private final String _disp;
        private final String[] _category_selected;

        private final String _issueDate;
        private final String _limitDate;

        private final String _documentRoot;
        private String _imagepath;
        private String _extension;
        private final String _useFormNameA143U;
        private final String _nextYearGradeFlg;

        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("CTRL_YEAR"); //年度
            _semester = request.getParameter("SEMESTER"); //学期

            _disp = request.getParameter("DISP"); // 1:クラス指定、2:個人指定

            _category_selected = request.getParameterValues("CATEGORY_SELECTED");
            for (int i = 0; i < _category_selected.length; i++) {
                _category_selected[i] = StringUtils.split(_category_selected[i], "-")[0];
            }

            _issueDate = StringUtils.defaultString(request.getParameter("ISSUE_DATE").replace('/', '-'));
            _limitDate = StringUtils.defaultString(request.getParameter("LIMIT_DATE").replace('/', '-'));

            _useFormNameA143U = request.getParameter("USEFORMNAMEA143U");
            _nextYearGradeFlg = request.getParameter("NEXT_YEAR_GRADE_FLG");

            _documentRoot = request.getParameter("DOCUMENTROOT");
            //  写真データ
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            try {
                returnval = getinfo.Control(db2);
                _imagepath = returnval.val4;      //格納フォルダ
                _extension = returnval.val5;      //拡張子
            } catch (Exception e) {
                log.error("setHeader set error!", e);
            } finally {
                getinfo = null;
                returnval = null;
            }
        }

        /**
         * 写真データファイルの取得
         */
        private String getImageFilePath(final String filename) {
            if (null == _documentRoot || null == _imagepath || null == filename) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer path = new StringBuffer();
            path.append(_documentRoot).append("/").append(_imagepath).append("/").append(filename);
            final File file = new File(path.toString());
            if (!file.exists()) {
                log.warn("画像ファイル無し:" + path);
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }
    }
}//クラスの括り
