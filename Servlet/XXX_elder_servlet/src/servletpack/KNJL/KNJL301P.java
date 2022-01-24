/*
 * $Id: 2adfd8e42fc2fe8f3e2920cf7272752d3615ce57 $
 *
 * 作成日: 2017/07/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL301P {

    private static final Log log = LogFactory.getLog(KNJL301P.class);

    private static final String CYUGAKU = "1";
    private static final String KOUKOU = "2";

    private static final String KENGAI_ZENKI = "1";
    private static final String KENNAI_ZENKI = "2";
    private static final String KENNAI_KOUKI = "3";

    private static final String KENNAI_SUISEN = "1";
    private static final String KENNAI_IPPAN = "2";

    private static final String GANSYO = "1";
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List studentList = getStudentList(db2);
        if (GANSYO.equals(_param._printDiv)) {
            if (CYUGAKU.equals(_param._applicantDiv) && KENGAI_ZENKI.equals(_param._testDiv)) {
                svf.VrSetForm("KNJL301P_1_2.frm", 4);
            } else {
                svf.VrSetForm("KNJL301P_1.frm", 4);
            }
            printOutFrm301P_1(svf, studentList);
        } else {
            svf.VrSetForm("KNJL301P_2.frm", 4);
            printOutFrm301P_2(svf, studentList);
        }
    }

    private void printOutFrm301P_1(final Vrw32alp svf, final List studentList) {
        final int maxCnt = 16;
        final int pageCnt = studentList.size() >= maxCnt ? studentList.size() / maxCnt : 0;
        final int pageAmari = studentList.size() % maxCnt > 0 ? 1 : 0;
        final int allPage = pageCnt + pageAmari;
        setTitle(svf);

        int lineCnt = 1;
        final int maxLine = 16;
        int page = 1;
        for (Iterator itStudent = studentList.iterator(); itStudent.hasNext();) {
            if (lineCnt > maxLine) {
                lineCnt = 1;
                page++;
                svf.VrEndPage();
                setTitle(svf);
            }
            svf.VrsOut("PAGE1", String.valueOf(page));
            svf.VrsOut("PAGE2", String.valueOf(allPage));

            final Student student = (Student) itStudent.next();
            svf.VrsOut("EXAM_NO", student._examNo);
            svf.VrsOut("NAME", student._name);
            final String kanaField = getMS932ByteLength(student._nameKana) > 20 ? "_2" : "_1";
            svf.VrsOut("NAME_KANA" + kanaField, student._nameKana);
            svf.VrsOut("BIRTHDAY", StringUtils.replaceChars(student._birthday, "-", "/"));
            svf.VrsOut("SEX", student._sex);
            svf.VrsOut("TEL_NO", student._telno);
            svf.VrsOut("ZIP_NO", student._zipcd);
            svf.VrsOut("ADDR1", student._address1);
            svf.VrsOut("ADDR2", student._address2);
            svf.VrsOut("FS_NAME", student._finschoolName);
            svf.VrsOut("GRD", student._grddivName);
            svf.VrsOut("FZIP_NO", student._finschoolZipCd);
            svf.VrsOut("FADDR1", student._finschoolAddr1);
            svf.VrsOut("FADDR2", student._finschoolAddr2);
            svf.VrsOut("FTELNO", student._finschoolTelNo);
            svf.VrsOut("FPRINCIPAL_NAME", student._finschoolPrincname);
            svf.VrsOut("GUARD_NAME", student._gname);
            final String gkanaField = getMS932ByteLength(student._gkana) > 20 ? "_2" : "_1";
            svf.VrsOut("GUARD_KANA" + gkanaField, student._gkana);
            svf.VrsOut("GTELNO", student._gtelno);
            svf.VrsOut("GTELNO2", student._gtelno2);
            if (CYUGAKU.equals(_param._applicantDiv) && KENNAI_KOUKI.equals(_param._testDiv)) {
                svf.VrsOut("EXAM_NO_NAME", "前期受験番号");
            }
            final String kyoudaiField = getMS932ByteLength(student._simaiName) > 30 ? "_2" : "_1";
            svf.VrsOut("SIMAI_NAME" + kyoudaiField, student._simaiName);
            svf.VrsOut("DORMITORY", student._tsugaku);
            svf.VrsOut("DAI1_RECEPTNO", student._dai1Receptno);
            svf.VrsOut("PRISCHOOL_NAME", student._prischoolName);
            svf.VrsOut("PTELNO", student._prischoolTelNo);
            svf.VrsOut("PZIP_NO", student._prischoolZipCd);
            svf.VrsOut("PADDR1", student._prischoolAddr1);
            svf.VrsOut("PADDR2", student._prischoolAddr2);

            svf.VrEndRecord();
            lineCnt++;
            _hasData = true;
        }
    }

    private void printOutFrm301P_2(final Vrw32alp svf, final List studentList) {
        final int maxCnt = 13;
        final int pageCnt = studentList.size() >= maxCnt ? studentList.size() / maxCnt : 0;
        final int pageAmari = studentList.size() % maxCnt > 0 ? 1 : 0;
        final int allPage = pageCnt + pageAmari;
        setTitle(svf);

        int lineCnt = 1;
        final int maxLine = 13;
        int page = 1;
        for (Iterator itStudent = studentList.iterator(); itStudent.hasNext();) {
            if (lineCnt > maxLine) {
                lineCnt = 1;
                page++;
                svf.VrEndPage();
                setTitle(svf);
            }
            svf.VrsOut("PAGE1", String.valueOf(page));
            svf.VrsOut("PAGE2", String.valueOf(allPage));

            final Student student = (Student) itStudent.next();
            svf.VrsOut("EXAM_NO", student._examNo);
            svf.VrsOut("NAME", student._name);
            for (Iterator itScore = student._confrptMap.keySet().iterator(); itScore.hasNext();) {
                final String key = (String) itScore.next();
                final Confrpt confrpt = (Confrpt) student._confrptMap.get(key);
                if ("001".equals(key)) {
                    printConfrpt(svf, confrpt, "SCORE1");
                }
                if ("002".equals(key)) {
                    printConfrpt(svf, confrpt, "SCORE2");
                }
                if ("DAT".equals(key)) {
                    printConfrpt(svf, confrpt, "SCORE3");
                    printSyoken(svf, confrpt._bikou, "REMARK1_", 40, 4);
                }
                if ("004".equals(key)) {
                    printSyoken(svf, confrpt._remark3, "HEALTH_", 50, 2);
                    printSyoken(svf, confrpt._remark4, "TOTALREMARK_", 50, 2);
                    printSyoken(svf, confrpt._remark5, "OTHERACT_", 50, 2);
                }
                if ("005".equals(key)) {
                    printSyoken(svf, confrpt._remark1, "SPECIALACT1_", 50, 2);
                    printSyoken(svf, confrpt._remark2, "SPECIALACT2_", 50, 2);
                    printSyoken(svf, confrpt._remark3, "SPECIALACT3_", 50, 2);
                    printSyoken(svf, confrpt._remark4, "TOTALSTUDY1_", 50, 2);
                    printSyoken(svf, confrpt._remark5, "TOTALSTUDY2_", 50, 2);
                    printSyoken(svf, confrpt._remark6, "TOTALSTUDY3_", 50, 2);
                }
                if ("006".equals(key)) {
                    final String grade = "1";
                    printAttend(svf, confrpt, grade);
                }
                if ("007".equals(key)) {
                    final String grade = "2";
                    printAttend(svf, confrpt, grade);
                }
                if ("008".equals(key)) {
                    final String grade = "3";
                    printAttend(svf, confrpt, grade);
                }
            }

            svf.VrEndRecord();
            lineCnt++;
            _hasData = true;
        }
    }

    private void printAttend(final Vrw32alp svf, final Confrpt confrpt, final String grade) {
        svf.VrsOut("ATTEND" + grade, confrpt._remark1);
        svf.VrsOut("KESSEKI" + grade, confrpt._remark2);
        svf.VrsOut("LATE" + grade, confrpt._remark3);
        svf.VrsOut("EARLY" + grade, confrpt._remark4);
        svf.VrsOut("ATTEND_REMARK" + grade, confrpt._remark5);
    }

    private void printSyoken(final Vrw32alp svf, final String printStr, final String fieldName, final int lineSize, final int lineCnt) {
        final String[] health = KNJ_EditEdit.get_token(printStr, lineSize, lineCnt);
        if (health != null) {
            for (int healthCnt = 0; healthCnt < health.length; healthCnt++) {
                svf.VrsOut(fieldName + (healthCnt + 1), health[healthCnt]);
            }
        }
    }

    private void printConfrpt(final Vrw32alp svf, final Confrpt confrpt, final String fieldName) {
        svf.VrsOut(fieldName + "_1", confrpt._remark1);
        svf.VrsOut(fieldName + "_2", confrpt._remark2);
        svf.VrsOut(fieldName + "_3", confrpt._remark3);
        svf.VrsOut(fieldName + "_4", confrpt._remark4);
        svf.VrsOut(fieldName + "_5", confrpt._remark5);
        svf.VrsOut(fieldName + "_6", confrpt._remark6);
        svf.VrsOut(fieldName + "_7", confrpt._remark7);
        svf.VrsOut(fieldName + "_8", confrpt._remark8);
        svf.VrsOut(fieldName + "_9", confrpt._remark9);
    }

    private void setTitle(final Vrw32alp svf) {
        final String setTitleData = GANSYO.equals(_param._printDiv) ? "願書" : "調査書";
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._entexamYear + "-04-01") + "度　" + _param._applicantDivName + "入試 " + _param._testDivName + "　" + setTitleData);
        svf.VrsOut("DATE", "実施日：" + KNJ_EditDate.h_format_JP(_param._testDate));

        int subclassCnt = 1;
        for (Iterator itSubclass = _param._subclassMap.keySet().iterator(); itSubclass.hasNext();) {
            final String subclassCd = (String) itSubclass.next();
            final Subclass subclass = (Subclass) _param._subclassMap.get(subclassCd);
            svf.VrsOut("CLASS_NAME" + subclassCnt, subclass._subclassName);
            svf.VrsOut("CLASS_NAME" + subclassCnt, subclass._subclassName);
            subclassCnt++;
        }
    }

    private List getStudentList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = studentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String birthday = rs.getString("BIRTHDAY");
                final String zipcd = rs.getString("ZIPCD");
                final String address1 = rs.getString("ADDRESS1");
                final String address2 = rs.getString("ADDRESS2");
                final String telno = rs.getString("TELNO");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String finschoolPrincname = rs.getString("PRINCNAME");
                final String finschoolTelNo = rs.getString("FINSCHOOL_TELNO");
                final String finschoolZipCd = rs.getString("FINSCHOOL_ZIPCD");
                final String finschoolAddr1 = rs.getString("FINSCHOOL_ADDR1");
                final String finschoolAddr2 = rs.getString("FINSCHOOL_ADDR2");
                final String prischoolName = rs.getString("PRISCHOOL_NAME");
                final String prischoolTelNo = rs.getString("PRISCHOOL_TELNO");
                final String prischoolZipCd = rs.getString("PRISCHOOL_ZIPCD");
                final String prischoolAddr1 = rs.getString("PRISCHOOL_ADDR1");
                final String prischoolAddr2 = rs.getString("PRISCHOOL_ADDR2");
                final String grddivName = rs.getString("GRDDIV_NAME");
                final String gname = rs.getString("GNAME");
                final String gkana = rs.getString("GKANA");
                final String relation = rs.getString("RELATION");
                final String gzipcd = rs.getString("GZIPCD");
                final String gaddress1 = rs.getString("GADDRESS1");
                final String gaddress2 = rs.getString("GADDRESS2");
                final String gtelno = rs.getString("GTELNO");
                final String gtelno2 = rs.getString("GTELNO2");
                final String tsugaku = rs.getString("TSUGAKU");
                final String goodTreat = rs.getString("GOOD_TREAT");
                final String simaiName = rs.getString("SIMAI_NAME");
                final String remark1 = rs.getString("REMARK1");
                final String age = rs.getString("AGE");
                final String dai1Receptno = rs.getString("DAI1_RECEPTNO");

                final Student student = new Student(examNo, name, nameKana, sex, birthday, zipcd, address1, address2, telno, finschoolName, finschoolPrincname, finschoolTelNo, finschoolZipCd, finschoolAddr1, finschoolAddr2, prischoolName, prischoolTelNo, prischoolZipCd, prischoolAddr1, prischoolAddr2, grddivName, gname, gkana, relation, gzipcd, gaddress1, gaddress2, gtelno, gtelno2, tsugaku, goodTreat, simaiName, remark1, age, dai1Receptno);
                student.setConfrpt(db2);
                retList.add(student);
            }

            db2.commit();
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retList;
    }

    private String studentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     Z002.ABBV1 AS SEX, ");
        stb.append("     BASE.BIRTHDAY, ");
        stb.append("     ADDR.ZIPCD, ");
        stb.append("     ADDR.ADDRESS1, ");
        stb.append("     ADDR.ADDRESS2, ");
        stb.append("     ADDR.TELNO, ");
        stb.append("     VALUE(L001.NAME1, '') || VALUE(FINSCHOOL.FINSCHOOL_NAME, '') AS FINSCHOOL_NAME, ");
        stb.append("     FINSCHOOL.PRINCNAME, ");
        stb.append("     FINSCHOOL.FINSCHOOL_TELNO, ");
        stb.append("     FINSCHOOL.FINSCHOOL_ZIPCD, ");
        stb.append("     FINSCHOOL.FINSCHOOL_ADDR1, ");
        stb.append("     FINSCHOOL.FINSCHOOL_ADDR2, ");
        stb.append("     VALUE(PRISCHOOL.PRISCHOOL_NAME, '') || '　' || VALUE(PRISCHOOL_C.PRISCHOOL_NAME, '') AS PRISCHOOL_NAME, ");
        stb.append("     PRISCHOOL.PRISCHOOL_TELNO, ");
        stb.append("     PRISCHOOL.PRISCHOOL_ZIPCD, ");
        stb.append("     PRISCHOOL.PRISCHOOL_ADDR1, ");
        stb.append("     PRISCHOOL.PRISCHOOL_ADDR2, ");
        stb.append("     L016.NAME1 AS GRDDIV_NAME, ");
        stb.append("     ADDR.GNAME, ");
        stb.append("     ADDR.GKANA, ");
        stb.append("     H201.NAME1 AS RELATION, ");
        stb.append("     ADDR.GZIPCD, ");
        stb.append("     ADDR.GADDRESS1, ");
        stb.append("     ADDR.GADDRESS2, ");
        stb.append("     ADDR.GTELNO, ");
        stb.append("     ADDR.GTELNO2, ");
        stb.append("     L047.NAME1 AS TSUGAKU, ");
        stb.append("     CASE WHEN BASE.SIMAI_DIV = '1' THEN 'レ' ELSE '' END AS GOOD_TREAT, ");
        stb.append("     BASE.SIMAI_NAME, ");
        stb.append("     BASE.REMARK1, ");
        stb.append("     BASE.REMARK2 AS AGE, ");
        stb.append("     BASE.DAI1_RECEPTNO ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND BASE.SEX = Z002.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON BASE.ENTEXAMYEAR = ADDR.ENTEXAMYEAR ");
        stb.append("          AND BASE.APPLICANTDIV = ADDR.APPLICANTDIV ");
        stb.append("          AND BASE.EXAMNO = ADDR.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST H201 ON H201.NAMECD1 = 'H201' ");
        stb.append("          AND ADDR.RELATIONSHIP = H201.NAMECD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON BASE.FS_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST L001 ON L001.NAMECD1 = 'L001' ");
        stb.append("          AND FINSCHOOL.FINSCHOOL_DISTCD = L001.NAMECD2 ");
        stb.append("     LEFT JOIN PRISCHOOL_MST PRISCHOOL ON BASE.JUKUCD = PRISCHOOL.PRISCHOOLCD ");
        stb.append("     LEFT JOIN PRISCHOOL_CLASS_MST PRISCHOOL_C ON BASE.JUKUCD = PRISCHOOL_C.PRISCHOOLCD ");
        stb.append("          AND BASE.KYOUSHITSU_CD= PRISCHOOL_C.PRISCHOOL_CLASS_CD ");
        stb.append("     LEFT JOIN NAME_MST L016 ON L016.NAMECD1 = 'L016' ");
        stb.append("          AND BASE.FS_GRDDIV = L016.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L047 ON L047.NAMECD1 = 'L047' ");
        stb.append("          AND BASE.DORMITORY_FLG = L047.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     BASE.EXAMNO ");
        return stb.toString();
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
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
        final String _applicantDiv;
        final String _applicantDivName;
        final String _testDiv;
        final String _testDivName;
        final String _testDate;
        final String _entexamYear;
        final String _printDiv;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final Map _subclassMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _entexamYear = request.getParameter("ENTEXAMYEAR");
            _printDiv = request.getParameter("PRINTDIV");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _applicantDivName = getNameMst(db2, "L003", _applicantDiv, "NAME1");
            final String testNameCd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            _testDivName = getNameMst(db2, testNameCd1, _testDiv, "NAME1");
            _testDate = getNameMst(db2, testNameCd1, _testDiv, "NAMESPARE1");
            _subclassMap = getSubclassMap(db2);
        }

        private String getNameMst(final DB2UDB db2, final String nameCd1, final String nameCd2, final String fieldName) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getNameMstSql(nameCd1, nameCd2);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString(fieldName);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getNameMstSql(final String namecd1, final String namecd2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = '" + namecd1 + "' ");
            stb.append("     AND NAMECD2 = '" + namecd2 + "' ");
            return stb.toString();
        }

        private Map getSubclassMap(final DB2UDB db2) throws SQLException {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getNameMstL008Sql();
                log.debug(" sql =" + sql);

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String nameCd2 = rs.getString("NAMECD2");
                    final String abbv = rs.getString("ABBV1");
                    if (null != abbv && !"".equals(abbv)) {
                        final Subclass subclass = new Subclass(nameCd2, abbv);
                        retMap.put(nameCd2, subclass);
                    }
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        private String getNameMstL008Sql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     L008.NAMECD2, ");
            stb.append("     L008.ABBV1 ");
            stb.append(" FROM ");
            stb.append("     NAME_MST L008 ");
            stb.append(" WHERE ");
            stb.append("     L008.NAMECD1 = 'L008' ");
            stb.append(" ORDER BY ");
            stb.append("     L008.NAMECD2 ");
            return stb.toString();
        }

    }

    /** 生徒クラス */
    private class Student {
        final String _examNo;
        final String _name;
        final String _nameKana;
        final String _sex;
        final String _birthday;
        final String _zipcd;
        final String _address1;
        final String _address2;
        final String _telno;
        final String _finschoolName;
        final String _finschoolPrincname;
        final String _finschoolTelNo;
        final String _finschoolZipCd;
        final String _finschoolAddr1;
        final String _finschoolAddr2;
        final String _prischoolName;
        final String _prischoolTelNo;
        final String _prischoolZipCd;
        final String _prischoolAddr1;
        final String _prischoolAddr2;
        final String _grddivName;
        final String _gname;
        final String _gkana;
        final String _relation;
        final String _gzipcd;
        final String _gaddress1;
        final String _gaddress2;
        final String _gtelno;
        final String _gtelno2;
        final String _tsugaku;
        final String _goodTreat;
        final String _simaiName;
        final String _remark1;
        final String _age;
        final String _dai1Receptno;
        Map _confrptMap;

        public Student(
                final String examNo,
                final String name,
                final String nameKana,
                final String sex,
                final String birthday,
                final String zipcd,
                final String address1,
                final String address2,
                final String telno,
                final String finschoolName,
                final String finschoolPrincname,
                final String finschoolTelNo,
                final String finschoolZipCd,
                final String finschoolAddr1,
                final String finschoolAddr2,
                final String prischoolName,
                final String prischoolTelNo,
                final String prischoolZipCd,
                final String prischoolAddr1,
                final String prischoolAddr2,
                final String grddivName,
                final String gname,
                final String gkana,
                final String relation,
                final String gzipcd,
                final String gaddress1,
                final String gaddress2,
                final String gtelno,
                final String gtelno2,
                final String tsugaku,
                final String goodTreat,
                final String simaiName,
                final String remark1,
                final String age,
                final String dai1Receptno
        ) throws SQLException {
            _examNo = examNo;
            _name = name;
            _nameKana = nameKana;
            _sex = sex;
            _birthday = birthday;
            _zipcd = zipcd;
            _address1 = address1;
            _address2 = address2;
            _telno = telno;
            _finschoolName = finschoolName;
            _finschoolPrincname = finschoolPrincname;
            _finschoolTelNo = finschoolTelNo;
            _finschoolZipCd = finschoolZipCd;
            _finschoolAddr1 = finschoolAddr1;
            _finschoolAddr2 = finschoolAddr2;
            _prischoolName = prischoolName;
            _prischoolTelNo = prischoolTelNo;
            _prischoolZipCd = prischoolZipCd;
            _prischoolAddr1 = prischoolAddr1;
            _prischoolAddr2 = prischoolAddr2;
            _grddivName = grddivName;
            _gname = gname;
            _gkana = gkana;
            _relation = relation;
            _gzipcd = gzipcd;
            _gaddress1 = gaddress1;
            _gaddress2 = gaddress2;
            _gtelno = gtelno;
            _gtelno2 = gtelno2;
            _tsugaku = tsugaku;
            _goodTreat = goodTreat;
            _simaiName = simaiName;
            _remark1 = remark1;
            _age = age;
            _dai1Receptno = dai1Receptno;
        }

        public void setConfrpt(final DB2UDB db2) throws SQLException {
            _confrptMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getConfrptDat();
                log.debug(" sql =" + sql);

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String seq = rs.getString("SEQ");
                    final String remark1 = rs.getString("REMARK1");
                    final String remark2 = rs.getString("REMARK2");
                    final String remark3 = rs.getString("REMARK3");
                    final String remark4 = rs.getString("REMARK4");
                    final String remark5 = rs.getString("REMARK5");
                    final String remark6 = rs.getString("REMARK6");
                    final String remark7 = rs.getString("REMARK7");
                    final String remark8 = rs.getString("REMARK8");
                    final String remark9 = rs.getString("REMARK9");
                    final String remark10 = rs.getString("REMARK10");
                    final String bikou = rs.getString("BIKOU");
                    final Confrpt confrpt = new Confrpt(remark1, remark2, remark3, remark4, remark5, remark6, remark7, remark8, remark9, remark10, bikou);
                    _confrptMap.put(seq, confrpt);
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            try {
                final String sql = getConfrptDetail();
                log.debug(" sql =" + sql);

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String seq = rs.getString("SEQ");
                    final String remark1 = rs.getString("REMARK1");
                    final String remark2 = rs.getString("REMARK2");
                    final String remark3 = rs.getString("REMARK3");
                    final String remark4 = rs.getString("REMARK4");
                    final String remark5 = rs.getString("REMARK5");
                    final String remark6 = rs.getString("REMARK6");
                    final String remark7 = rs.getString("REMARK7");
                    final String remark8 = rs.getString("REMARK8");
                    final String remark9 = rs.getString("REMARK9");
                    final String remark10 = rs.getString("REMARK10");
                    final String bikou = rs.getString("BIKOU");
                    final Confrpt confrpt = new Confrpt(remark1, remark2, remark3, remark4, remark5, remark6, remark7, remark8, remark9, remark10, bikou);
                    _confrptMap.put(seq, confrpt);
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getConfrptDat() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     'DAT' AS SEQ, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT01 AS REMARK1, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT02 AS REMARK2, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT03 AS REMARK3, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT04 AS REMARK4, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT05 AS REMARK5, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT06 AS REMARK6, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT07 AS REMARK7, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT08 AS REMARK8, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT09 AS REMARK9, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT10 AS REMARK10, ");
            stb.append("     CONFRPT.REMARK1 AS BIKOU ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ");
            stb.append(" WHERE ");
            stb.append("     CONFRPT.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
            stb.append("     AND CONFRPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND CONFRPT.EXAMNO = '" + _examNo + "' ");
            return stb.toString();
        }

        private String getConfrptDetail() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CONFRPT.SEQ, ");
            stb.append("     CONFRPT.REMARK1, ");
            stb.append("     CONFRPT.REMARK2, ");
            stb.append("     CONFRPT.REMARK3, ");
            stb.append("     CONFRPT.REMARK4, ");
            stb.append("     CONFRPT.REMARK5, ");
            stb.append("     CONFRPT.REMARK6, ");
            stb.append("     CONFRPT.REMARK7, ");
            stb.append("     CONFRPT.REMARK8, ");
            stb.append("     CONFRPT.REMARK9, ");
            stb.append("     CONFRPT.REMARK10, ");
            stb.append("     '' AS BIKOU ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONFRPT ");
            stb.append(" WHERE ");
            stb.append("     CONFRPT.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
            stb.append("     AND CONFRPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND CONFRPT.EXAMNO = '" + _examNo + "' ");
            return stb.toString();
        }

    }

    /** APPLICANTCONFRPT */
    private class Confrpt {
        final String _remark1;
        final String _remark2;
        final String _remark3;
        final String _remark4;
        final String _remark5;
        final String _remark6;
        final String _remark7;
        final String _remark8;
        final String _remark9;
        final String _remark10;
        final String _bikou;

        public Confrpt(
                final String remark1,
                final String remark2,
                final String remark3,
                final String remark4,
                final String remark5,
                final String remark6,
                final String remark7,
                final String remark8,
                final String remark9,
                final String remark10,
                final String bikou
        ) {
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _remark4 = remark4;
            _remark5 = remark5;
            _remark6 = remark6;
            _remark7 = remark7;
            _remark8 = remark8;
            _remark9 = remark9;
            _remark10 = remark10;
            _bikou = bikou;
        }
    }

    /** 科目 */
    private class Subclass {
        final String _subclassCd;
        final String _subclassName;

        public Subclass(
                final String subclassCd,
                final String subclassName
        ) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
        }
    }
}

// eof

