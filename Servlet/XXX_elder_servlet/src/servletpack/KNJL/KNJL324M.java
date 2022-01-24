// kanji=漢字
/*
 * $Id: 4f92f2cc3a0c74c7ff065cfdffa6fac3327e8ab6 $
 *
 * 作成日: 2009/12/22 1:37:07 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 4f92f2cc3a0c74c7ff065cfdffa6fac3327e8ab6 $
 */
public class KNJL324M {

    private static final Log log = LogFactory.getLog("KNJL324M.class");

    private boolean _hasData;
    private static final String FORMNAME1 = "KNJL324M_1.frm";
    private static final String FORMNAME2 = "KNJL324M_2.frm";
    private static final String FORMNAME4 = "KNJL324M_4.frm";
    private static final int MAX_LINE = 10;

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
        final List printData = getPrintData(db2);
        printGoukaku(svf, printData);
        printTyui(db2, svf);
    }

    private void printGoukaku(final Vrw32alp svf, final List printData) {
        final String form;
        final int MAX_RETU;
        if (printData.size() > 180) {
            form = FORMNAME2;
            MAX_RETU = 10;
        } else {
            form = FORMNAME1;
            MAX_RETU = 9;
        }
        svf.VrSetForm(form, 1);
        int lineCnt = 1;
        int retuCnt = 1;
        int totalCnt = 1;
        boolean firstFlg = true;
        boolean printFlg = false;
        for (final Iterator itPrint = printData.iterator(); itPrint.hasNext();) {
            if (firstFlg) {
                svf.VrsOut("HEADER", "合格した人の番号は次の通りです");
            }
            if (totalCnt >= printData.size()) {
                svf.VrsOut("FOOTER", "以上" + KNJ_EditEdit.convertKansuuji(printData.size()) + "名");
                printFlg = false;
            }
            final Student student = (Student) itPrint.next();
            if (lineCnt > MAX_LINE) {
                lineCnt = 1;
                retuCnt++;
            }
            if (retuCnt > MAX_RETU) {
                svf.VrEndPage();
                printFlg = true;
                lineCnt = 1;
                retuCnt = 1;
            }
            svf.VrsOutn("EXAMNO" + retuCnt, lineCnt, student._examNo);
            lineCnt++;
            totalCnt++;
            firstFlg = false;
            _hasData = true;
        }
        if (!printFlg) {
            svf.VrsOut("FOOTER", "以上" + KNJ_EditEdit.convertKansuuji(printData.size()) + "名");
            svf.VrEndPage();
        }
    }

    private void printTyui(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        svf.VrSetForm(FORMNAME4, 1);
        final String yukoDate = KNJ_EditDate.h_format_JP_MD(_param._yukoDate);
        final String yukoWeek = KNJ_EditDate.h_format_W(_param._yukoDate);
        svf.VrsOut("START_DATE", KNJ_EditEdit.convertKansuuji(yukoDate) + "（" + yukoWeek + "）");
        final String printDay = KNJ_EditDate.h_format_JP(_param._date);
        svf.VrsOut("DATE", KNJ_EditEdit.getKansuujiWareki(printDay));
        svf.VrsOut("SCHOOLNAME", _param._certifSchoolDat._schoolName);
        final String limitDay = KNJ_EditDate.h_format_JP_MD(_param._limitDate);
        final String limitWeek = KNJ_EditDate.h_format_W(_param._limitDate);
        final String limitHour = _param.changeHour(_param._limitTime);
        svf.VrsOut("END_DATE", KNJ_EditEdit.convertKansuuji(limitDay));
        svf.VrsOut("END_WEEKDAY", "（" + limitWeek + "）");
        svf.VrsOut("END_TIME", limitHour + "時");
        _hasData = true;
        svf.VrEndPage();
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
                final String subOrder = rsStudent.getString("SUB_ORDER");
                final String addr1 = rsStudent.getString("ADDRESS1");
                final String addr2 = rsStudent.getString("ADDRESS2");
                final Student student = new Student(examNo, name, birthDay, subOrder, addr1, addr2);
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
        stb.append("     T1.SUB_ORDER, ");
        stb.append("     L1.ADDRESS1, ");
        stb.append("     L1.ADDRESS2 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT L1 ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ");
        stb.append("          AND T1.EXAMNO = L1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND T1.TESTDIV = '1' ");
        stb.append("     AND T1.SHDIV = '1' ");
        stb.append("     AND T1.DESIREDIV = '1' ");
        stb.append("     AND T1.JUDGEMENT IN (SELECT I1.NAMECD2 FROM NAME_MST I1 WHERE NAMECD1 = 'L013' AND NAMESPARE1 = '1') ");
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ");

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
        final String _subOrder;
        final String _addr1;
        final String _addr2;

        public Student(
                final String examNo,
                final String name,
                final String birthDay,
                final String subOrder,
                final String addr1,
                final String addr2
        ) {
            _examNo = examNo;
            _name = name;
            _birthDay = birthDay;
            _subOrder = subOrder;
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
        private final String _yukoDate;
        private final String _limitDate;
        private final String _limitTime;
        private final CertifSchoolDat _certifSchoolDat;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _date = request.getParameter("DATE");
            _yukoDate = request.getParameter("YUKO_DATE");
            _limitDate = request.getParameter("LIMIT_DATE");
            _limitTime = request.getParameter("LIMIT_TIME");
            _certifSchoolDat = getCertifSchoolDat(db2, _ctrlYear, "105");
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

        private String changeHour(final String hour) {
            String retVal = "";
            if (null != hour && !"".equals(hour)) {
                final int hourInt = Integer.parseInt(hour);
                retVal = hourInt > 11 && hourInt < 24 ? "午後" : "午前";
                retVal += changeKansuuji(hourInt);
            }
            return retVal;
        }

        private String changeKansuuji(final int hour) {
            final Map kansuujiMap = new HashMap();
            kansuujiMap.put("0", "零");
            kansuujiMap.put("1", "一");
            kansuujiMap.put("2", "二");
            kansuujiMap.put("3", "三");
            kansuujiMap.put("4", "四");
            kansuujiMap.put("5", "五");
            kansuujiMap.put("6", "六");
            kansuujiMap.put("7", "七");
            kansuujiMap.put("8", "八");
            kansuujiMap.put("9", "九");
            kansuujiMap.put("10", "十");
            kansuujiMap.put("11", "十一");
            kansuujiMap.put("12", "十二");
            int setHour = hour;
            if (hour == 24) {
                setHour = 0;
            } else if (hour > 12) {
                setHour = hour - 12;
            }
            return (String) kansuujiMap.get(String.valueOf(setHour));
        }

        private String changeYear(final String year) {
            return KNJ_EditDate.h_format_JP_N(year + "-01-01");
        }
    }

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

    private class JudgementHistory {
        private final String _judgement;
        private final int _judgeCnt;

        JudgementHistory(
                final String judgement,
                final int judgeCnt
        ) throws SQLException {
            _judgement = judgement;
            _judgeCnt = judgeCnt;
        }

    }
}

// eof
