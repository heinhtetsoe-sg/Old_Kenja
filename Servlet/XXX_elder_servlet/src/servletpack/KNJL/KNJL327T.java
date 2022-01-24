// kanji=漢字
/*
 * $Id: 6ccf57aed69b945e0ab1e5995dfb2ac49cac2864 $
 *
 * 作成日: 2009/12/19 1:37:07 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

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
 * @author m-yama
 * @version $Id: 6ccf57aed69b945e0ab1e5995dfb2ac49cac2864 $
 */
public class KNJL327T {

    private static final Log log = LogFactory.getLog("KNJL327T.class");

    private boolean _hasData;
    private static final String FORMNAME = "KNJL327T.frm";

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
        svf.VrSetForm(FORMNAME, 1);
        final List printData = getPrintData(db2);
        for (final Iterator itPrint = printData.iterator(); itPrint.hasNext();) {
            setKoteiKoumoku(svf);
            final Student student = (Student) itPrint.next();
            final int nameField = student._name.length() > 12 ? 2 : 1;
            svf.VrsOut("NAME" + nameField, student._name);
            svf.VrsOut("EXAM_NO", String.valueOf(Integer.parseInt(student._examNo)));
            svf.VrsOut("ADDRESS1", student._addr1);
            svf.VrsOut("ADDRESS2", student._addr2);
            final String birthDay = KNJ_EditDate.h_format_JP_Bth(student._birthDay);
            svf.VrsOut("BIRTHDAY", birthDay);
            _hasData = true;
            svf.VrEndPage();
        }
    }

    private void setKoteiKoumoku(final Vrw32alp svf) {
        final String warekiYear = _param.changeYear(_param._year);
        svf.VrsOut("MAIN1", "　上記の者は、" + warekiYear + "度鳥取県立高等学校入学者");
        svf.VrsOut("MAIN2", "選抜検査に合格したことを証明する。");
        final String date = KNJ_EditDate.h_format_JP(_param._date);
        svf.VrsOut("DATE", date);
        if (_param._certifSchoolDat != null) {
            svf.VrsOut("SYOSYO_NAME", _param._certifSchoolDat._syosyoName);
            svf.VrsOut("SYOSYO_NAME2", _param._certifSchoolDat._syosyoName2);
            svf.VrsOut("SCHOOLNAME", _param._certifSchoolDat._schoolName);
            svf.VrsOut("JOBNAME", _param._certifSchoolDat._jobName);
            svf.VrsOut("STAFFNAME", _param._certifSchoolDat._principalName);
        }
    }

    private List getPrintData(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final String studentSql = getStudentSql();
        PreparedStatement psStudent = null;
        ResultSet rsStudent = null;
        try {
            psStudent = db2.prepareStatement(studentSql);
            rsStudent = psStudent.executeQuery();
            while (rsStudent.next()) {
                final String examNo = rsStudent.getString("EXAMNO");
                final String name = rsStudent.getString("NAME");
                final String birthDay = rsStudent.getString("BIRTHDAY");
                final String addr1 = rsStudent.getString("ADDRESS1");
                final String addr2 = rsStudent.getString("ADDRESS2");
                final Student student = new Student(examNo, name, birthDay, addr1, addr2);
                retList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(null, psStudent, rsStudent);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.BIRTHDAY, ");
        stb.append("     L1.ADDRESS1, ");
        stb.append("     L1.ADDRESS2 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT L1 ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ");
        stb.append("          AND T1.EXAMNO = L1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
        if (_param._printRange.equals("1")) {
            stb.append("     AND INT(T1.EXAMNO) BETWEEN " + _param._examnoFrom + " AND " + _param._examnoTo + " ");
        }
        stb.append("     AND T1.TESTDIV = '1' ");
        stb.append("     AND T1.SHDIV = '1' ");
        stb.append("     AND T1.DESIREDIV = '1' ");
        stb.append("     AND T1.JUDGEMENT IN (SELECT I1.NAMECD2 FROM NAME_MST I1 WHERE NAMECD1 = 'L013' AND NAMESPARE1 = '1') ");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    public class Student {
        final String _examNo;
        final String _name;
        final String _birthDay;
        final String _addr1;
        final String _addr2;

        public Student(
                final String examNo,
                final String name,
                final String birthDay,
                final String addr1,
                final String addr2
        ) {
            _examNo = examNo;
            _name = name;
            _birthDay = birthDay;
            _addr1 = addr1;
            _addr2 = addr2;
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
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _date;
        private final String _examnoFrom;
        private final String _examnoTo;
        private final String _printRange;
        private final String _printType;
        private final CertifSchoolDat _certifSchoolDat;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _date = request.getParameter("DATE");
            _examnoFrom = request.getParameter("EXAMNO_FROM");
            _examnoTo = request.getParameter("EXAMNO_TO");
            _printRange = request.getParameter("PRINT_RANGE");
            _printType = request.getParameter("PRINT_TYPE");
            _certifSchoolDat = getCertifSchoolDat(db2, _ctrlYear, "106");
        }

        private CertifSchoolDat getCertifSchoolDat(final DB2UDB db2, final String year, final String certifKindCd) throws SQLException {
            CertifSchoolDat certifSchoolDat = null;
            final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + year + "' AND CERTIF_KINDCD = '" + certifKindCd + "' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String syosyoName = rs.getString("SYOSYO_NAME");
                    final String syosyoName2 = rs.getString("SYOSYO_NAME2");
                    final String schoolName = rs.getString("SCHOOL_NAME");
                    final String jobName = rs.getString("JOB_NAME");
                    final String principalName = rs.getString("PRINCIPAL_NAME");
                    certifSchoolDat = new CertifSchoolDat(syosyoName, syosyoName2, schoolName, jobName, principalName);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return certifSchoolDat;
        }

        private String changeYear(final String year) {
            return KNJ_EditDate.h_format_JP_N(year + "-01-01");
        }
    }

    /** パラメータクラス */
    private class CertifSchoolDat {
        private final String _syosyoName;
        private final String _syosyoName2;
        private final String _schoolName;
        private final String _jobName;
        private final String _principalName;

        CertifSchoolDat(
                final String syosyoName,
                final String syosyoName2,
                final String schoolName,
                final String jobName,
                final String principalName
        ) throws SQLException {
            _syosyoName = syosyoName;
            _syosyoName2 = syosyoName2;
            _schoolName = schoolName;
            _jobName = jobName;
            _principalName = principalName;
        }

    }
}

// eof
