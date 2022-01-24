// kanji=漢字
/*
 * $Id: 166192e7f776f15a182d9e250990251c96a99ee3 $
 *
 * 作成日: 2009/12/22 1:37:07 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.File;
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

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 166192e7f776f15a182d9e250990251c96a99ee3 $
 */
public class KNJL327M {

    private static final Log log = LogFactory.getLog("KNJL327M.class");

    private boolean _hasData;
    private static final String FORMNAME1 = "KNJL327M_1.frm";
    private static final String FORMNAME2 = "KNJL327M_2.frm";

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
        if (_param.isGoukakuTuuti()) {
            printGoukaku(svf, printData);
        } else {
            printHoin(db2, svf, printData);
        }
    }

    private void printGoukaku(final Vrw32alp svf, final List printData) {
        svf.VrSetForm(FORMNAME1, 1);
        for (final Iterator itPrint = printData.iterator(); itPrint.hasNext();) {
            final String date = KNJ_EditDate.h_format_JP(_param._date);
            svf.VrsOut("DATE", date);
            svf.VrsOut("SCHOOLNAME", _param._certifSchoolDat._schoolName);
            svf.VrsOut("JOBNAME", _param._certifSchoolDat._jobName);
            svf.VrsOut("STAFFNAME", _param._certifSchoolDat._principalName);
            svf.VrsOut("DATE", date);
            final Student student = (Student) itPrint.next();
            final int nameField = student._name.length() > 10 ? 2 : 1;
            svf.VrsOut("NAME" + nameField, student._name);
            svf.VrsOut("EXAM_NO", student._examNo);
            final String birthDay = KNJ_EditDate.h_format_JP_Bth(student._birthDay);
            svf.VrsOut("BIRTHDAY", birthDay);
            final String fileName = _param._documentRoot + "/" + _param._imagePath + "/SCHOOLSTAMP.bmp";
            if (fileName != null && new File(fileName).exists()) {
                svf.VrsOut("STAFFBTM", fileName); //学校長印
            }
            _hasData = true;
            svf.VrEndPage();
        }
    }

    private void printHoin(final DB2UDB db2, final Vrw32alp svf, final List printData) throws SQLException {
        svf.VrSetForm(FORMNAME2, 1);
        final int seikiNinsu = getSeikiNinsu(db2);
        for (final Iterator itPrint = printData.iterator(); itPrint.hasNext();) {
            final Student student = (Student) itPrint.next();
            final int nameField = student._name.length() > 10 ? 2 : 1;
            svf.VrsOut("NAME" + nameField, student._name);
            svf.VrsOut("EXAM_NO", student._examNo);
            svf.VrsOut("SUB_NO", student._subOrder);
            svf.VrsOut("PASS_NUM", String.valueOf(seikiNinsu));
            final String yukoDate = KNJ_EditDate.h_format_JP(_param._yukoDate);
            final String yukoWeek = KNJ_EditDate.h_format_W(_param._yukoDate);
            final String subHour = _param.changeHour(_param._yukoTime);
            svf.VrsOut("SUB_YEAR", yukoDate + "(" + yukoWeek + ")" + subHour);
            final String printDay = KNJ_EditDate.h_format_JP(_param._date);
            svf.VrsOut("PRINT_DAY", printDay);
            final String renrakuDay = KNJ_EditDate.h_format_JP_MD(_param._renrakuDate);
            final String renrakuWeek = KNJ_EditDate.h_format_W(_param._renrakuDate);
            svf.VrsOut("RENRAKU_DATE", renrakuDay + "(" + renrakuWeek + ")");
            setJudgeHist(svf);
            _hasData = true;
            svf.VrEndPage();
        }
    }

    private void setJudgeHist(final Vrw32alp svf) {
        final int lastYear = Integer.parseInt(_param._year) - 1;
        final int lastYear2 = Integer.parseInt(_param._year) - 3;
        int fieldCnt = 1;
        for (int year = lastYear2; year <= lastYear; year++) {
            final String nendoYear = String.valueOf(year);
            final String nendo = _param.changeYear(nendoYear);
            svf.VrsOut("YEAR" + fieldCnt, nendo + "度");
            final int passCnt = _param.getJudgeHistCnt(nendoYear + "1");
            final int hoinCnt = _param.getJudgeHistCnt(nendoYear + "2");
            final int hoinPassCnt = _param.getJudgeHistCnt(nendoYear + "4");
            svf.VrsOut("PASS" + fieldCnt, String.valueOf(passCnt));
            svf.VrsOut("SUB_PASS" + fieldCnt, String.valueOf(hoinPassCnt));
            svf.VrsOut("SUB" + fieldCnt, String.valueOf(hoinCnt));
            fieldCnt++;
        }
    }

    private int getSeikiNinsu(DB2UDB db2) throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND T1.TESTDIV = '1' ");
        stb.append("     AND T1.SHDIV = '1' ");
        stb.append("     AND T1.DESIREDIV = '1' ");
        stb.append("     AND T1.JUDGEMENT = '1' ");

        int retcnt = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                retcnt = rs.getInt("CNT");
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retcnt;
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
        if (_param.isGoukakuTuuti()) {
            if (_param._printRange.equals("2")) {
                stb.append("     AND T1.EXAMNO BETWEEN '" + _param._examnoFrom + "' AND '" + _param._examnoTo + "' ");
            }
            stb.append("     AND T1.JUDGEMENT IN (SELECT I1.NAMECD2 FROM NAME_MST I1 WHERE NAMECD1 = 'L013' AND NAMESPARE1 = '1') ");
        } else {
            stb.append("     AND T1.JUDGEMENT IN ('2', '4') ");
        }
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
        private final String _renrakuDate;
        private final String _yukoTime;
        private final String _examnoFrom;
        private final String _examnoTo;
        private final String _printRange;
        private final String _printType;
        private final CertifSchoolDat _certifSchoolDat;
        private final Map _judgeHist;
        private final String _documentRoot;
        private String _imagePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _date = request.getParameter("DATE");
            _yukoDate = request.getParameter("YUKO_DATE");
            _renrakuDate = request.getParameter("RENRAKU_DATE");
            _yukoTime = request.getParameter("YUKO_TIME");
            _examnoFrom = request.getParameter("EXAMNO_FROM");
            _examnoTo = request.getParameter("EXAMNO_TO");
            _printRange = request.getParameter("PRINT_RANGE");
            _printType = request.getParameter("PRINT_TYPE");
            _certifSchoolDat = getCertifSchoolDat(db2, _ctrlYear, "105");
            _judgeHist = getJudgeHist(db2, _year);
            _documentRoot = request.getParameter("DOCUMENTROOT");
            setImagePath(db2);
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

        private Map getJudgeHist(final DB2UDB db2, final String year) throws SQLException {
            final Map retMap = new HashMap();
            final int lastYear = Integer.parseInt(year) - 1;
            final int lastYear2 = Integer.parseInt(year) - 3;
            final String sql = "SELECT * FROM ENTEXAM_JUDGEMENT_HISTORY_DAT WHERE ENTEXAMYEAR BETWEEN '" + lastYear2 + "' AND '" + lastYear + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String entexamYear = rs.getString("ENTEXAMYEAR");
                    final String judgement = rs.getString("JUDGEMENT");
                    final int judgeCnt = rs.getInt("JUDGE_CNT");
                    final JudgementHistory history = new JudgementHistory(judgement, judgeCnt);
                    retMap.put(entexamYear + judgement, history);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private boolean isGoukakuTuuti() {
            return _printType.equals("1");
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

        public int getJudgeHistCnt(final String key) {
            if (_judgeHist.containsKey(key)) {
                final JudgementHistory history = (JudgementHistory) _judgeHist.get(key);
                return history._judgeCnt;
            } else {
                return 0;
            }
        }

        private String changeYear(final String year) {
            return KNJ_EditDate.h_format_JP_N(year + "-01-01");
        }
        
        private void setImagePath(final DB2UDB db2) throws SQLException {
            _imagePath = null;
            final String sql = "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _imagePath = rs.getString("IMAGEPATH");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
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
