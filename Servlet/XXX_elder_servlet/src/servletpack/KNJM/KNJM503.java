// kanji=漢字
/*
 * 作成日: 2013/03/13 13:16:02 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2013 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJObjectAbs;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 */
public class KNJM503 {

    private static final Log log = LogFactory.getLog("KNJM503.class");

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
        final List studentList = getStudentInfo(db2);
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();
            setHead(svf);
            printStudent(svf, student);
            printRecordData(svf, student);
            _hasData = true;
        }
    }

    protected void setHead(final Vrw32alp svf) {
        svf.VrSetForm("KNJM503.frm", 4);
        final String nendo = _param.changePrintYear();
        svf.VrsOut("NENDO", nendo);
        if (null != _param._certifSchool) {
            svf.VrsOut("SCHOOL_NAME", _param._certifSchool._schoolName);
            svf.VrsOut("JOB_NAME", _param._certifSchool._jobName);
            svf.VrsOut("PRINCIPAL_NAME", _param._certifSchool._principalName);
        }
    }

    private int getMS932ByteLength(final String str) {
        int len = 0;
        if (null != str) {
            try {
                len = str.getBytes("MS932").length;
            } catch (final Exception e) {
                log.debug("exception!", e);
            }
        }
        return len;
    }

    protected void printStudent(final Vrw32alp svf, final Student student) {
        log.debug(student);
        if (null != _param._certifSchool && null != _param._certifSchool._remark2) {
            svf.VrsOut("STAFFNAME", _param._certifSchool._remark2 + student._hrStaffName);
        }
        svf.VrsOut("HR_NAME", student._hrName + " " + student._schregno);
        svf.VrsOut("COURSE", student._courseName + student._majorName);
        svf.VrsOut("NAME", student._name);
        svf.VrsOut("SEM_TESTNAME1", "前期成績");
        svf.VrsOut("SEM_TESTNAME2", "後期成績");
        if (null != _param._addrPrint) {
            svf.VrsOut("ZIPCD", student._gZip);
            if ("1".equals(_param._useAddrField2) && (getMS932ByteLength(student._gAddr1) > 50 || getMS932ByteLength(student._gAddr2) > 50)) {
                svf.VrsOut("ADDR1_2", student._gAddr1);
                svf.VrsOut("ADDR22", student._gAddr2);
            } else {
                svf.VrsOut("ADDR1", student._gAddr1);
                svf.VrsOut("ADDR2", student._gAddr2);
            }
            if (!StringUtils.isBlank(student._gName)) {
                svf.VrsOut("ADDRESSEE", student._gName + "  様");
            }
        }
        svf.VrsOut("SP_COUNT", String.valueOf(student._tokkatuSitei));
        svf.VrsOut("ALL_SP_COUNT", String.valueOf(student._tokkatuAll));
        svf.VrsOut("SCHOOL_COUNT", String.valueOf(student._syukkouSitei));
        svf.VrsOut("ALL_SCHOOL_COUNT", String.valueOf(student._syukkouAll));
        printHreport(svf, "COMMUNICATION", student._communication, 20, 10);
    }

    private void printHreport(
            final Vrw32alp svf,
            final String fieldName,
            final String str,
            final int size,
            final int lineCnt
    ) {
        KNJObjectAbs knjobj = new KNJEditString();
        ArrayList arrlist = knjobj.retDividString(str, size, lineCnt);
        if ( arrlist != null ) {
            for (int i = 0; i < arrlist.size(); i++) {
                svf.VrsOutn(fieldName, i+1,  (String)arrlist.get(i) );
            }
        }
    }

    private void printRecordData(final Vrw32alp svf, final Student student) {
        boolean endRecFlg = false;
        for (final Iterator itSub = student._subclassList.iterator(); itSub.hasNext();) {
            final SubclassData subclassData = (SubclassData) itSub.next();

            svf.VrsOut("CLASS", subclassData._className);
            if (subclassData._subclassName != null) {
                if (16 < subclassData._subclassName.length()) {
                    svf.VrsOut("SUBCLASS2_1", subclassData._subclassName.substring(0, 8));
                    svf.VrsOut("SUBCLASS2_2", subclassData._subclassName.substring(8, 16));
                } else if (8 < subclassData._subclassName.length()) {
                    svf.VrsOut("SUBCLASS2_1", subclassData._subclassName.substring(0, 8));
                    svf.VrsOut("SUBCLASS2_2", subclassData._subclassName.substring(8));
                } else if (6 < subclassData._subclassName.length()) {
                    svf.VrsOut("SUBCLASS2_1", subclassData._subclassName);
                } else {
                    svf.VrsOut("SUBCLASS1", subclassData._subclassName);
                }
            }
            if (null != subclassData._recordData) {
                if (!_param._disableValueClassList.contains(subclassData._classCd)) {
                    svf.VrsOut("SEM_SCORE1", subclassData._recordData._seme1Val);
                    svf.VrsOut("SEM_SCORE2", subclassData._recordData._seme2Val);
                    svf.VrsOut("GRAD_VALUE5", subclassData._recordData._gradVal);
                }
                svf.VrsOut("COMP_CREDIT", subclassData._recordData._getCre);
            }
            String kisyutokuCredit = StringUtils.defaultString(student._kisyutokuCredit, "0");
            String totalGetCredit = StringUtils.defaultString(student._totalGetCredit, "0");
            int allGetCregit = Integer.parseInt(kisyutokuCredit) + Integer.parseInt(totalGetCredit);
            svf.VrsOut("ALRE_GET_CREDIT", kisyutokuCredit);
            svf.VrsOut("ALL_GET_CREDIT", String.valueOf(allGetCregit));
            svf.VrsOut("GET_CREDIT", totalGetCredit);
            svf.VrEndRecord();
            endRecFlg = true;
        }
        if (!endRecFlg) {
            svf.VrEndRecord();
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private List getStudentInfo(final DB2UDB db2) throws SQLException  {
        final List rtnStudent = new ArrayList();
        final String sql = getStudentInfoSql(_param.getAddrField(), _param.getAddrTable(), _param.getAddrTableOn());
        log.debug(" student sql = " + sql);
        PreparedStatement ps  =null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Student student = new Student(rs.getString("SCHREGNO"),
                                              rs.getString("GRADE"),
                                              rs.getString("HR_CLASS"),
                                              rs.getString("ATTENDNO"),
                                              rs.getString("HR_NAME"),
                                              rs.getString("HR_NAMEABBV"),
                                              rs.getString("NAME"),
                                              rs.getString("COURSECD"),
                                              rs.getString("COURSENAME"),
                                              rs.getString("MAJORCD"),
                                              rs.getString("MAJORNAME"),
                                              rs.getString("COURSECODE"),
                                              rs.getString("COURSECODENAME"),
                                              rs.getString("GNAME"),
                                              rs.getString("GZIP"),
                                              rs.getString("GADDR1"),
                                              rs.getString("GADDR2"),
                                              rs.getString("SPECIALACTREMARK"),
                                              rs.getString("SPECIALACTREMARK_SEM1"),
                                              rs.getString("SPECIALACTREMARK_SEM2"),
                                              rs.getString("COMMUNICATION"),
                                              rs.getString("TOTALSTUDYTIME"),
                                              rs.getString("STAFFNAME"),
                                              rs.getString("KISYUTOKU_GET_CREDIT"),
                                              rs.getString("TOTAL_GETCREDIT"));
                student.setStudentInfo(db2);
                rtnStudent.add(student);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rs);
        }
        return rtnStudent;
    }

    class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendNo;
        final String _hrName;
        final String _hrNameAbbv;
        final String _name;
        final String _courseCd;
        final String _courseName;
        final String _majorCd;
        final String _majorName;
        final String _courseCode;
        final String _courseCodeName;
        final String _gName;
        final String _gZip;
        final String _gAddr1;
        final String _gAddr2;
        final String _specialactremark;
        final String _specialactremarkSem1;
        final String _specialactremarkSem2;
        final String _communication;
        final String _totalstudytime;
        final String _hrStaffName;

        String _tokkatuSitei = "0";
        String _tokkatuAll = "0";
        int _syukkouSitei = 0;
        int _syukkouAll = 0;
        final List _subclassList;
        final String _kisyutokuCredit;
        final String _totalGetCredit;
        /**
         * コンストラクタ。
         */
        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String hrName,
                final String hrNameAbbv,
                final String name,
                final String courseCd,
                final String courseName,
                final String majorCd,
                final String majorName,
                final String courseCode,
                final String courseCodeName,
                final String gName,
                final String gZip,
                final String gAddr1,
                final String gAddr2,
                final String specialactremark,
                final String specialactremarkSem1,
                final String specialactremarkSem2,
                final String communication,
                final String totalstudytime,
                final String hrStaffName,
                final String kisyutokuGetCredit,
                final String totalGetCredit
                ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _name = name;
            _courseCd = courseCd;
            _courseName = courseName;
            _majorCd = majorCd;
            _majorName = majorName;
            _courseCode = courseCode;
            _courseCodeName = courseCodeName;
            _gName = gName;
            _gZip = gZip;
            _gAddr1 = gAddr1;
            _gAddr2 = gAddr2;
            _specialactremark = specialactremark;
            _specialactremarkSem1 = specialactremarkSem1;
            _specialactremarkSem2 = specialactremarkSem2;
            _communication = communication;
            _totalstudytime = totalstudytime;
            _hrStaffName = hrStaffName;
            _subclassList = new ArrayList();
            _kisyutokuCredit = kisyutokuGetCredit;
            _totalGetCredit = totalGetCredit;
        }

        /**
         * @param db2
         */
        public void setStudentInfo(final DB2UDB db2) throws SQLException {

            setTokkatu(db2);
            setSyukkou(db2);
            final String subclassSql = getSubclass();
            log.debug(" subclass sql(" + _schregno + ") = " + subclassSql);
            PreparedStatement psSub = null;
            ResultSet rsSub = null;
            try {
                psSub = db2.prepareStatement(subclassSql);
                rsSub = psSub.executeQuery();
                while (rsSub.next()) {
                    final SubclassData subclassData = new SubclassData(
                            rsSub.getString("CLASSCD"),
                            rsSub.getString("SCHOOL_KIND"),
                            rsSub.getString("CURRICULUM_CD"),
                            rsSub.getString("SUBCLASSCD"),
                            rsSub.getString("CLASSNAME"),
                            rsSub.getString("SUBCLASSNAME")
                            );
                    subclassData.setRecord(db2, _schregno);
                    _subclassList.add(subclassData);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, psSub, rsSub);
            }

        }

        public void setSyukkou(final DB2UDB db2) throws SQLException {
            final String syukkouSiteiSql = getSyukkouSql("SITEI");
            PreparedStatement psSitei = null;
            ResultSet rsSitei = null;
            try {
                psSitei = db2.prepareStatement(syukkouSiteiSql);
                rsSitei = psSitei.executeQuery();
                while (rsSitei.next()) {
                    _syukkouSitei++;
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, psSitei, rsSitei);
            }

            final String syukkouAllSql = getSyukkouSql("ALL");
            PreparedStatement psAll = null;
            ResultSet rsALL = null;
            try {
                psAll = db2.prepareStatement(syukkouAllSql);
                rsALL = psAll.executeQuery();
                while (rsALL.next()) {
                    _syukkouAll++;
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, psAll, rsALL);
            }
        }

        public String getSyukkouSql(final String div) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     EXECUTEDATE,PERIODCD ");
            stb.append(" FROM ");
            stb.append("     HR_ATTEND_DAT ");
            stb.append(" WHERE ");
            stb.append("     SCHREGNO = '" + _schregno + "' AND ");
            if (div.equals("SITEI")) {
                stb.append("     YEAR = '" + _param._ctrlYear + "' AND ");
            }
            stb.append("     EXECUTEDATE <= '" + _param._sKijun.replace('/','-') + "' AND ");
            stb.append("     CHAIRCD LIKE '92%' ");
            stb.append(" GROUP BY ");
            stb.append("     EXECUTEDATE,PERIODCD ");

            return stb.toString();
        }


        public void setTokkatu(final DB2UDB db2) throws SQLException {
            final String tokkatuSiteiSql = getTokkatuSql("SITEI");
            PreparedStatement psSitei = null;
            ResultSet rsSitei = null;
            try {
                psSitei = db2.prepareStatement(tokkatuSiteiSql);
                rsSitei = psSitei.executeQuery();
                while (rsSitei.next()) {
                    _tokkatuSitei = rsSitei.getString("CREDIT_TIME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, psSitei, rsSitei);
            }

            final String tokkatuAllSql = getTokkatuSql("ALL");
            PreparedStatement psAll = null;
            ResultSet rsALL = null;
            try {
                psAll = db2.prepareStatement(tokkatuAllSql);
                rsALL = psAll.executeQuery();
                while (rsALL.next()) {
                    _tokkatuAll = rsALL.getString("CREDIT_TIME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, psAll, rsALL);
            }
        }

        private String getTokkatuSql(final String div) {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     VALUE(SUM(CREDIT_TIME), 0) AS CREDIT_TIME ");
            stb.append(" FROM ");
            stb.append("     SPECIALACT_ATTEND_DAT ");
            stb.append(" WHERE ");
            stb.append("     SCHREGNO = '" + _schregno + "' ");
            if (div.equals("SITEI")) {
                stb.append("     AND YEAR = '" + _param._ctrlYear + "' ");
            }
            stb.append("     AND ATTENDDATE <= '" + _param._sKijun.replace('/', '-') + "' ");

            return stb.toString();
        }

        /**
         * @param db2
         */
        public String getSubclass() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CLS_M.CLASSCD, ");
            stb.append("     SUB_M.SCHOOL_KIND, ");
            stb.append("     SUB_M.CURRICULUM_CD, ");
            stb.append("     SUB_M.SUBCLASSCD, ");
            stb.append("     CLS_M.CLASSNAME, ");
            stb.append("     SUB_M.SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT C_STD ");
            stb.append("     LEFT JOIN CHAIR_DAT C_DAT ON C_STD.YEAR = C_DAT.YEAR ");
            stb.append("          AND C_STD.SEMESTER = C_DAT.SEMESTER ");
            stb.append("          AND C_STD.CHAIRCD = C_DAT.CHAIRCD ");
            stb.append("     LEFT JOIN CLASS_MST CLS_M ON C_DAT.CLASSCD = CLS_M.CLASSCD ");
            stb.append("          AND C_DAT.SCHOOL_KIND = CLS_M.SCHOOL_KIND ");
            stb.append("     INNER JOIN SUBCLASS_MST SUB_M ON C_DAT.CLASSCD = SUB_M.CLASSCD ");
            stb.append("           AND C_DAT.SCHOOL_KIND = SUB_M.SCHOOL_KIND ");
            stb.append("           AND C_DAT.CURRICULUM_CD = SUB_M.CURRICULUM_CD ");
            stb.append("           AND C_DAT.SUBCLASSCD = SUB_M.SUBCLASSCD ");
            stb.append("           AND SUB_M.CLASSCD <= '90' ");
            stb.append(" WHERE ");
            stb.append("     C_STD.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND C_STD.SEMESTER <= '" + _param._semester + "' ");
            stb.append("     AND C_STD.SCHREGNO = '" + _schregno + "' ");
            stb.append(" GROUP BY ");
            stb.append("     CLS_M.CLASSCD, ");
            stb.append("     SUB_M.SCHOOL_KIND, ");
            stb.append("     SUB_M.CURRICULUM_CD, ");
            stb.append("     SUB_M.SUBCLASSCD, ");
            stb.append("     CLS_M.CLASSNAME, ");
            stb.append("     SUB_M.SUBCLASSNAME ");
            stb.append(" ORDER BY ");
            stb.append("     CLS_M.CLASSCD, ");
            stb.append("     SUB_M.SCHOOL_KIND, ");
            stb.append("     SUB_M.SUBCLASSCD, ");
            stb.append("     SUB_M.CURRICULUM_CD ");

            return stb.toString();
        }

        public String toString() {
            return "学籍：" + _schregno + " 名前：" + _name;
        }
    }
    private String getStudentInfoSql(final String fieldName, final String tableName, final String tableOn) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VSCH.SCHREGNO, ");
        stb.append("     VSCH.GRADE, ");
        stb.append("     VSCH.HR_CLASS, ");
        stb.append("     VSCH.ATTENDNO, ");
        stb.append("     VALUE(VSCH.HR_NAME, '') AS HR_NAME, ");
        stb.append("     VALUE(VSCH.HR_NAMEABBV, '') AS HR_NAMEABBV, ");
        stb.append("     VSCH.NAME, ");
        stb.append("     VSCH.COURSECD, ");
        stb.append("     VALUE(L1.COURSENAME, '') AS COURSENAME, ");
        stb.append("     VSCH.MAJORCD, ");
        stb.append("     VALUE(L1.MAJORNAME, '') AS MAJORNAME, ");
        stb.append("     VSCH.COURSECODE, ");
        stb.append("     VALUE(L2.COURSECODENAME, '') AS COURSECODENAME, ");
        stb.append("     L3." + fieldName + "_NAME AS GNAME, ");
        stb.append("     L3." + fieldName + "_ZIPCD AS GZIP, ");
        stb.append("     L3." + fieldName + "_ADDR1 AS GADDR1, ");
        stb.append("     L3." + fieldName + "_ADDR2 AS GADDR2, ");
        stb.append("     L4.SPECIALACTREMARK, ");
        stb.append("     L6.SPECIALACTREMARK AS SPECIALACTREMARK_SEM1, ");
        stb.append("     L7.SPECIALACTREMARK AS SPECIALACTREMARK_SEM2, ");
        stb.append("     L4.COMMUNICATION, ");
        stb.append("     L4.TOTALSTUDYTIME, ");
        stb.append("     (SELECT W1.STAFFNAME FROM STAFF_MST W1 WHERE W1.STAFFCD=L5.TR_CD1) AS STAFFNAME, ");
        stb.append("     L8.KISYUTOKU_GET_CREDIT, ");
        stb.append("     L9.TOTAL_GETCREDIT ");
        stb.append(" FROM ");
        stb.append("     V_SCHREG_INFO VSCH ");
        stb.append("     LEFT JOIN V_COURSE_MAJOR_MST L1 ON VSCH.YEAR = L1.YEAR ");
        stb.append("          AND VSCH.COURSECD = L1.COURSECD ");
        stb.append("          AND VSCH.MAJORCD = L1.MAJORCD ");
        stb.append("     LEFT JOIN V_COURSECODE_MST L2 ON VSCH.YEAR = L2.YEAR ");
        stb.append("          AND VSCH.COURSECODE = L2.COURSECODE ");
        stb.append("     LEFT JOIN " + tableName + " L3 ON VSCH.SCHREGNO = L3.SCHREGNO " + tableOn);
        stb.append("     LEFT JOIN HREPORTREMARK_DAT L4 ON VSCH.YEAR = L4.YEAR ");
        stb.append("          AND L4.SEMESTER = '" + _param._semester + "' ");
        stb.append("          AND VSCH.SCHREGNO = L4.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT L5 ON VSCH.YEAR = L5.YEAR ");
        stb.append("          AND VSCH.SEMESTER = L5.SEMESTER ");
        stb.append("          AND VSCH.GRADE = L5.GRADE ");
        stb.append("          AND VSCH.HR_CLASS = L5.HR_CLASS ");
        stb.append("     LEFT JOIN HREPORTREMARK_DAT L6 ON VSCH.YEAR = L6.YEAR ");
        stb.append("          AND L6.SEMESTER = '1' ");
        stb.append("          AND VSCH.SCHREGNO = L6.SCHREGNO ");
        stb.append("     LEFT JOIN HREPORTREMARK_DAT L7 ON VSCH.YEAR = L7.YEAR ");
        stb.append("          AND L7.SEMESTER = '2' ");
        stb.append("          AND VSCH.SCHREGNO = L7.SCHREGNO ");
        stb.append("     LEFT JOIN (SELECT SCHREGNO, SUM(GET_CREDIT) AS KISYUTOKU_GET_CREDIT ");
        stb.append("                FROM SCHREG_STUDYREC_DAT ");
        stb.append("                WHERE YEAR < '" + _param._ctrlYear + "' ");
        stb.append("                GROUP BY SCHREGNO ");
        stb.append("               ) L8 ON VSCH.SCHREGNO = L8.SCHREGNO ");

        stb.append("     LEFT JOIN (SELECT ");
        stb.append("                    C_STD.YEAR, ");
        stb.append("                    C_STD.SEMESTER, ");
        stb.append("                    C_STD.SCHREGNO, ");
        stb.append("                    SUM(RECORD.GET_CREDIT) AS TOTAL_GETCREDIT ");
        stb.append("                FROM ");
        stb.append("                    CHAIR_STD_DAT C_STD ");
        stb.append("                    LEFT JOIN CHAIR_DAT C_DAT ON C_STD.YEAR = C_DAT.YEAR ");
        stb.append("                         AND C_STD.SEMESTER = C_DAT.SEMESTER ");
        stb.append("                         AND C_STD.CHAIRCD  = C_DAT.CHAIRCD ");
        stb.append("                    LEFT JOIN CLASS_MST CLS_M ON C_DAT.CLASSCD = CLS_M.CLASSCD ");
        stb.append("                         AND C_DAT.SCHOOL_KIND = CLS_M.SCHOOL_KIND ");
        stb.append("                    INNER JOIN SUBCLASS_MST SUB_M ON C_DAT.CLASSCD = SUB_M.CLASSCD ");
        stb.append("                          AND C_DAT.SCHOOL_KIND   = SUB_M.SCHOOL_KIND ");
        stb.append("                          AND C_DAT.CURRICULUM_CD = SUB_M.CURRICULUM_CD ");
        stb.append("                          AND C_DAT.SUBCLASSCD    = SUB_M.SUBCLASSCD ");
        stb.append("                          AND SUB_M.CLASSCD      <= '90' ");
        stb.append("                    INNER JOIN RECORD_DAT RECORD ON C_STD.YEAR = RECORD.YEAR ");
        stb.append("                          AND SUB_M.CLASSCD       = RECORD.CLASSCD ");
        stb.append("                          AND SUB_M.SCHOOL_KIND   = RECORD.SCHOOL_KIND ");
        stb.append("                          AND SUB_M.CURRICULUM_CD = RECORD.CURRICULUM_CD ");
        stb.append("                          AND SUB_M.SUBCLASSCD    = RECORD.SUBCLASSCD ");
        stb.append("                          AND RECORD.TAKESEMES    = '0' ");
        stb.append("                          AND C_STD.SCHREGNO      = RECORD.SCHREGNO ");
        stb.append("                GROUP BY ");
        stb.append("                    C_STD.YEAR, ");
        stb.append("                    C_STD.SEMESTER, ");
        stb.append("                    C_STD.SCHREGNO ");
        stb.append("               ) L9 ON VSCH.YEAR     = L9.YEAR ");
        stb.append("                   AND VSCH.SEMESTER = L9.SEMESTER ");
        stb.append("                   AND VSCH.SCHREGNO = L9.SCHREGNO ");

        stb.append("  ");
        stb.append(" WHERE ");
        stb.append("     VSCH.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND VSCH.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     AND VSCH.SCHREGNO IN " + _param._inState + " ");
        stb.append(" ORDER BY ");
        stb.append("     VSCH.ATTENDNO ");

        return stb.toString();
    }

    private class SubclassData {
        private final String _classCd;
        private final String _schoolKind;
        private final String _curriculumCd;
        private final String _subclassCd;
        private final String _className;
        private final String _subclassName;

        private RecordData _recordData;
        SubclassData(
                final String classCd,
                final String schoolKind,
                final String curriculumCd,
                final String subclassCd,
                final String className,
                final String subclassName
        ) {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _className = className;
            _subclassName = subclassName;
            _recordData = null;
        }

        public void setRecord(final DB2UDB db2, final String schregNo) throws SQLException {
            final String recSql = getRecordSql(schregNo);
            log.debug(" record sql (" + _subclassCd + ") = " + recSql);
            PreparedStatement psRec = null;
            ResultSet rsRec = null;
            try {
                psRec = db2.prepareStatement(recSql);
                rsRec = psRec.executeQuery();
                while (rsRec.next()) {
                    _recordData = new RecordData(
                            rsRec.getString("SEM1_VALUE"),
                            rsRec.getString("SEM2_VALUE"),
                            rsRec.getString("GRAD_VALUE"),
                            rsRec.getString("GET_CREDIT"));
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, psRec, rsRec);
            }
        }

        private String getRecordSql(final String schregNo) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     RECORD_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND CLASSCD = '" + _classCd + "' ");
            stb.append("     AND SCHOOL_KIND = '" + _schoolKind + "' ");
            stb.append("     AND CURRICULUM_CD = '" + _curriculumCd + "' ");
            stb.append("     AND SUBCLASSCD = '" + _subclassCd + "' ");
            stb.append("     AND TAKESEMES = '0' ");
            stb.append("     AND SCHREGNO = '" + schregNo + "' ");

            return stb.toString();
        }
    }

    private class RecordData {
        private final String _seme1Val;
        private final String _seme2Val;
        private final String _gradVal;
        private final String _getCre;
        public RecordData(final String seme1Val, final String seme2Val, final String gradVal, final String getCre) {
            _seme1Val = seme1Val;
            _seme2Val = seme2Val;
            _gradVal = gradVal;
            _getCre = getCre;
        }

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _semester;
        private final String _gradeHr;
        private final String[] _schregNo;
        final String _inState;
        private final String _addrPrint;
        private final String _addrDiv;
        private final String _sKijun;
        private final String _tKijun;
        private final String _useCurriculumcd;
        private final String _useAddrField2;

        String _z012 = "";
        final boolean _isSeireki;
        final List _disableValueClassList;

        CertifSchool _certifSchool;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _semester = request.getParameter("SEMESTER");
            _gradeHr = request.getParameter("GRADE_HR_CLASS");
            _schregNo = request.getParameterValues("category_selected");
            String sep = "";
            final StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int ia = 0; ia<_schregNo.length; ia++) {
                stb.append(sep + "'" + _schregNo[ia] + "'");
                sep = ",";
            }
            stb.append(")");
            _inState = stb.toString();

            _addrPrint = request.getParameter("ADDR_PRINT");
            _addrDiv = request.getParameter("ADDR_DIV");

            _sKijun = request.getParameter("SKIJUN");
            _tKijun = request.getParameter("TKIJUN");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useAddrField2 = request.getParameter("useAddrField2");

            _z012 = setNameMst(db2, "Z012", "01", "NAME1");
            _isSeireki = _z012.equals("2") ? true : false;
            _disableValueClassList = getDisableValueClassList(db2);

            // 証明書学校データ
            PreparedStatement psCertif = null;
            ResultSet rsCertif = null;
            try {
                final String sqlCertif = "SELECT * FROM CERTIF_SCHOOL_DAT " + "WHERE YEAR='" + _ctrlYear + "' AND CERTIF_KINDCD = '104'";
                psCertif = db2.prepareStatement(sqlCertif);
                rsCertif = psCertif.executeQuery();
                while (rsCertif.next()) {
                    _certifSchool = new CertifSchool(rsCertif.getString("SYOSYO_NAME"), rsCertif.getString("SYOSYO_NAME2"), rsCertif.getString("SCHOOL_NAME"), rsCertif.getString("JOB_NAME"), rsCertif
                            .getString("PRINCIPAL_NAME"), rsCertif.getString("REMARK1"), rsCertif.getString("REMARK2"), rsCertif.getString("REMARK3"), rsCertif.getString("REMARK4"), rsCertif
                            .getString("REMARK5"), rsCertif.getString("REMARK6"), rsCertif.getString("REMARK7"), rsCertif.getString("REMARK8"), rsCertif.getString("REMARK9"), rsCertif
                            .getString("REMARK10"));
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, psCertif, rsCertif);
            }
        }

        private String setNameMst(final DB2UDB db2, final String namecd1, final String namecd2, final String field) throws SQLException {
            String rtnSt = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String nameMstSql = getNameMst(_ctrlYear, namecd1, namecd2, null);
            try {
                ps = db2.prepareStatement(nameMstSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnSt = rs.getString(field);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtnSt;
        }


        /**
         * 評定を無いものとして扱う教科コードを設定する。
         */
        private List getDisableValueClassList(final DB2UDB db2) {
            // 名称マスタから取得する
            ResultSet rs = null;
            final List novalueclasslist = new ArrayList();
            try {
                final String sql;
//                if ("1".equals(_useCurriculumcd) && "1".equals(_useClassDetailDat)) {
//                    sql = "SELECT classcd || '-' || school_kind as namecd2, class_remark7 as namespare1 FROM class_detail_dat WHERE year='" + _year + "' AND class_seq='003' ";
//                } else {
                    sql = "SELECT namecd2, namespare1 FROM v_name_mst WHERE year='" + _ctrlYear + "' AND namecd1='D008' AND namecd2 IS NOT NULL";
//                }
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    novalueclasslist.add(rs.getString("namecd2"));
                }
            } catch (SQLException e) {
                log.error("評定を無いものとして扱う教科コードの取得エラー", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, null, rs);
            }
            return novalueclasslist;
        }

        private String getNameMst(final String year, final String namecd1, final String namecd2, final String where) {
            final String rtnSql = " SELECT "
                                + "     * "
                                + " FROM "
                                + "     V_NAME_MST "
                                + " WHERE "
                                + "     YEAR = '" + year + "' "
                                + "     AND NAMECD1 = '" + namecd1 + "' "
                                + (null != namecd2 ? ("     AND NAMECD2 = '" + namecd2 + "'") : "")
                                + (null != where ? where : "")
                                + " ORDER BY "
                                + "     NAMECD2 ";
            return rtnSql;
        }

        public String changePrintYear() {
            if (_isSeireki) {
                return _ctrlYear + "年度";
            } else {
                return nao_package.KenjaProperties.gengou(Integer.parseInt(_ctrlYear)) + "年度";
            }
        }

        private String getAddrField() {
            return _addrDiv.equals("3") ? "SEND" : _addrDiv.equals("1") ? "GUARD" : "GUARANTOR";
        }

        private String getAddrTable() {
            return _addrDiv.equals("3") ? "SCHREG_SEND_ADDRESS_DAT" : "GUARDIAN_DAT";
        }

        private String getAddrTableOn() {
            return _addrDiv.equals("3") ? " AND L3.DIV = '1' " : "";
        }

    }

    static class CertifSchool {
        final String _syosyoName;
        final String _syosyoName2;
        final String _schoolName;
        final String _jobName;
        final String _principalName;
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

        CertifSchool(
                final String syosyoName,
                final String syosyoName2,
                final String schoolName,
                final String jobName,
                final String principalName,
                final String remark1,
                final String remark2,
                final String remark3,
                final String remark4,
                final String remark5,
                final String remark6,
                final String remark7,
                final String remark8,
                final String remark9,
                final String remark10
        ) {
            _syosyoName = syosyoName;
            _syosyoName2 = syosyoName2;
            _schoolName = schoolName;
            _jobName = jobName;
            _principalName = principalName;
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
        }
    }
}

// eof
