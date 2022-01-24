/*
 *
 * 作成日: 2020/12/17
 * 作成者: shimoji
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;


/**
 * 一覧
 */
public class KNJA226B {

    private static final Log log = LogFactory.getLog(KNJA226B.class);
    private boolean _hasData;
    private Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

        } catch (final Exception e) {
            log.error("Exception:", e);
            return;
        }

        _param = createParam(db2, request);

        Vrw32alp svf = new Vrw32alp();
        try {
            response.setContentType("application/pdf");

            if (svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            svf.VrSetSpoolFileStream(response.getOutputStream());

            printMain(db2, svf);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            final int ret = svf.VrQuit();
            log.info("===> VrQuit():" + ret);

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        if (_param._useFormName == null || "".equals(_param._useFormName)) {
            printSiblingList(db2, svf);
        } else {
            printSiblingShimaneList(db2, svf);
        }

        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void printSiblingList(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        svf.VrSetForm("KNJA226B.frm", 1);

        //生徒単位で回す
        final int MAX_LEN = 50;
        int len = 0;
        final List studentList = getStudenList(db2);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            //改ページ
            if (len == MAX_LEN) {
                svf.VrEndPage();
                len = 0;
            }
            if (len == 0) {
                //タイトル
                final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._ctrlDate) + "度";
                svf.VrsOut("TITLE", nendo + "　兄弟姉妹一覧");
                svf.VrsOut("GRADE", _param._gradeName);
                svf.VrsOut("DATE", _param._ctrlDate.replace("-","/"));
            }
            len++;

            final Student student = (Student) it.next();

            //クラス
            svf.VrsOutn("HR_NAME", len, student._hrName);

            //氏名
            final int nameByte = getMS932ByteLength(student._name);
            final String fieldNo = nameByte <= 20 ? "1" : nameByte <= 30 ? "2" : "3";
            svf.VrsOutn("NAME"+fieldNo, len, student._name);

            //兄弟姉妹
            final String brosisName = student.getBrosisName();
            svf.VrsOutn("BROSIS_NAME", len, brosisName);

            _hasData = true;
        }
        if (0 < len) {
            svf.VrEndPage();
        }

    }

    /**
     * 兄弟姉妹一覧（島根版）を出力する。
     * @param db2
     * @param svf
     * @throws Exception
     */
    private void printSiblingShimaneList(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        svf.VrSetForm(_param._useFormName + ".frm", 4);

        //生徒単位で回す
        final Map<String, Guardian> studentShimaneMap = getStudenShimaneMap(db2);

        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度";
        svf.VrsOut("TITLE", nendo + "　兄弟姉妹一覧表");

        int lineCnt = 1;
        for (final Guardian guardian : studentShimaneMap.values()) {
            //保護者
            final int addr1Byte = KNJ_EditEdit.getMS932ByteLength(guardian._addr1);
            final String addr1FiledName = addr1Byte > 50 ? "4" : addr1Byte > 40 ? "3" : addr1Byte > 30 ? "2" : "1";
            svf.VrsOut("ADDR1_" + addr1FiledName, guardian._addr1);

            final int addr2Byte = KNJ_EditEdit.getMS932ByteLength(guardian._addr2);
            final String addr2FiledName = addr2Byte > 50 ? "4" : addr2Byte > 40 ? "3" : addr2Byte > 30 ? "2" : "1";
            svf.VrsOut("ADDR2_" + addr2FiledName, guardian._addr2);

            final int gnameByte = KNJ_EditEdit.getMS932ByteLength(guardian._gname);
            final String gnameFiledName = gnameByte > 30 ? "3" : gnameByte > 24 ? "2" : "1";
            svf.VrsOut("GUARD_NAME" + gnameFiledName, guardian._gname);

            for (int i = 0; i< guardian.size(); i++) {
                StudentShimane studentShimane = guardian.get(i);

                svf.VrsOut("NO1", String.valueOf(lineCnt));

                //クラス
                svf.VrsOut("GRADE", studentShimane._gradeName);
                svf.VrsOut("HR_NAME", studentShimane._hrName);
                svf.VrsOut("NO2", studentShimane._attendNo);

                //氏名
                final int nameByte = KNJ_EditEdit.getMS932ByteLength(studentShimane._name);
                final String fieldFieldName = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
                svf.VrsOut("NAME" + fieldFieldName, studentShimane._name);

                svf.VrsOut("SEX", studentShimane._sex);

                svf.VrsOut("GRPCD1", String.valueOf(lineCnt));
                svf.VrsOut("GRPCD2", String.valueOf(lineCnt));

                svf.VrEndRecord();
                _hasData = true;
            }
            lineCnt++;
        }
    }

    private static int getMS932ByteLength(final String str) {
        int len = 0;
        if (null != str) {
            try {
                len = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return len;
    }

    private List getStudenList(final DB2UDB db2)  throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List studentList = new ArrayList();
        try {
            final String sql = getStudentSql();
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            String befSchno = "";
            Student student = null;
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String hrName = rs.getString("HR_NAME");
                final String name = rs.getString("NAME");
                final String relSchno = rs.getString("RELA_SCHREGNO");
                final String relHrName = rs.getString("RELA_HR_NAME");
                final String relName = rs.getString("RELANAME");
                final String remark = rs.getString("REMARK");
                final String zaisekiFlgStr = rs.getString("ZAISEKIFLG");
                final boolean zaisekiFlg = "1".equals(zaisekiFlgStr) ? true : false ;

                if (!schregno.equals(befSchno)) {
                    student = new Student(schregno, hrName, name);
                    studentList.add(student);
                }
                final Student relStudent = new Student(relSchno, relHrName, relName);
                relStudent._zaisekiFlg = zaisekiFlg;
                relStudent._remark = remark;
                student.relList.add(relStudent);

                befSchno = schregno;
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return studentList;
    }

    private Map<String, Guardian> getStudenShimaneMap(final DB2UDB db2)  throws SQLException {
        Map<String, Guardian> studentShimaneMap = new LinkedHashMap<String, Guardian>();
        Guardian guardian = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String studenShimaneSql = getStudenShimaneSql();
            log.debug(" sql =" + studenShimaneSql);
            ps = db2.prepareStatement(studenShimaneSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String gradeName = rs.getString("GRADE_NAME");
                final String hrName = rs.getString("HR_CLASS_NAME");
                final String attendNo = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String relaGradeName = rs.getString("RELAGRADE_NAME");
                final String relaHrName = rs.getString("RELAHR_CLASS");
                final String relaAttendNo = rs.getString("RELAATTENDNO");
                final String relaName = rs.getString("RELANAME");
                final String relaSex = rs.getString("RELASEX");
                final String addr1 = rs.getString("GUARD_ADDR1");
                final String addr2 = rs.getString("GUARD_ADDR2");
                final String gname = rs.getString("GUARD_NAME");

                if (!studentShimaneMap.containsKey(schregno)) {
                    // 対象者は初めの一回で処理することで、帳票の先頭にくるようにする。
                    guardian = new Guardian(gradeName, hrName, attendNo, name, sex, addr1, addr2, gname);
                    studentShimaneMap.put(schregno, guardian);
                } else {
                    guardian = studentShimaneMap.get(schregno);
                }

                guardian.add(relaGradeName, relaHrName, relaAttendNo, relaName, relaSex);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return studentShimaneMap;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("   WITH MAX_SEMES AS ( ");
        stb.append("     SELECT ");
        stb.append("       YEAR ");
        stb.append("       , MAX(SEMESTER) AS SEMESTER ");
        stb.append("       , SCHREGNO ");
        stb.append("     FROM ");
        stb.append("       SCHREG_REGD_DAT ");
        stb.append("     WHERE ");
        stb.append("       YEAR = '" + _param._year + "' ");
        stb.append("       AND GRADE = '" + _param._grade + "' ");
        stb.append("     GROUP BY ");
        stb.append("       YEAR ");
        stb.append("       , SCHREGNO ");
        stb.append("   ) ");
        stb.append("   , REGD_DATA AS ( ");
        stb.append("     SELECT ");
        stb.append("       T1.YEAR ");
        stb.append("       , T1.SEMESTER ");
        stb.append("       , T1.SCHREGNO ");
        stb.append("       , T1.GRADE ");
        stb.append("       , T1.HR_CLASS ");
        stb.append("       , T1.ATTENDNO ");
        stb.append("     FROM ");
        stb.append("       SCHREG_REGD_DAT T1 ");
        stb.append("       INNER JOIN MAX_SEMES T2 ");
        stb.append("         ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   ) ");
        stb.append("   , FAMILY_BASE AS ( ");
        stb.append("     SELECT ");
        stb.append("       T1.YEAR ");
        stb.append("       , T1.SCHREGNO ");
        stb.append("       , T1.GRADE ");
        stb.append("       , T1.HR_CLASS ");
        stb.append("       , T3.RELANO ");
        stb.append("       , T3.RELA_SCHREGNO ");
        stb.append("       , T3.RELANAME ");
        stb.append("       , T3.REMARK ");
        stb.append("     FROM ");
        stb.append("       REGD_DATA T1 ");
        stb.append("       INNER JOIN SCHREG_BASE_DETAIL_MST T2 ");
        stb.append("         ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.BASE_SEQ = '009' ");
        stb.append("       INNER JOIN FAMILY_DAT T3 ");
        stb.append("         ON T3.FAMILY_NO = T2.BASE_REMARK1 ");
        stb.append("         AND VALUE (T3.RELA_SCHREGNO, 'DUMMY') <> T1.SCHREGNO ");
        stb.append("   ) ");
        stb.append("   , FAMILY_BASE1 AS ( ");
        stb.append("     SELECT ");
        stb.append("       T2.SCHREGNO AS MAIN_SCHREGNO ");
        stb.append("       , T2.RELANO ");
        stb.append("       , T1.SCHREGNO AS RELA_SCHREGNO ");
        stb.append("       , T3.GRADE AS RELA_GRADE ");
        stb.append("       , T3.HR_CLASS AS RELA_HR_CLASS ");
        stb.append("       , T3.HR_NAME AS RELA_HR_NAME ");
        stb.append("       , T4.NAME AS RELANAME ");
        stb.append("       , T2.REMARK ");
        stb.append("       , '1' AS ZAISEKIFLG ");
        stb.append("     FROM ");
        stb.append("       SCHREG_REGD_DAT T1 ");
        stb.append("       INNER JOIN FAMILY_BASE T2 ");
        stb.append("         ON T2.RELA_SCHREGNO = T1.SCHREGNO ");
        stb.append("       LEFT JOIN SCHREG_REGD_HDAT T3 ");
        stb.append("         ON T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T3.GRADE = T1.GRADE ");
        stb.append("         AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("       LEFT JOIN SCHREG_BASE_MST T4 ");
        stb.append("         ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("       T1.YEAR = '" + _param._year + "' ");
        stb.append("       AND T1.SEMESTER = '"+_param._semester+"' ");
        stb.append("   ) ");
        stb.append("   , FAMILY_BASE2 AS ( ");
        stb.append("     SELECT ");
        stb.append("       SCHREGNO AS MAIN_SCHREGNO ");
        stb.append("       , RELANO ");
        stb.append("       , RELA_SCHREGNO ");
        stb.append("       , '' AS RELA_HR_NAME ");
        stb.append("       , RELANAME ");
        stb.append("       , REMARK ");
        stb.append("       , '0' AS ZAISEKIFLG ");
        stb.append("     FROM ");
        stb.append("       FAMILY_BASE ");
        stb.append("     WHERE ");
        stb.append("       RELA_SCHREGNO IS NULL ");
        stb.append("   ) ");
        stb.append("   , FAMILY_DATA AS ( ");
        stb.append("     SELECT ");
        stb.append("       MAIN_SCHREGNO ");
        stb.append("       , RELANO ");
        stb.append("       , RELA_SCHREGNO ");
        stb.append("       , RELA_GRADE ");
        stb.append("       , RELA_HR_CLASS ");
        stb.append("       , RELA_HR_NAME ");
        stb.append("       , RELANAME ");
        stb.append("       , REMARK ");
        stb.append("       , ZAISEKIFLG ");
        stb.append("     FROM ");
        stb.append("       FAMILY_BASE1 ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("       MAIN_SCHREGNO ");
        stb.append("       , RELANO ");
        stb.append("       , RELA_SCHREGNO ");
        stb.append("       , '' AS RELA_GRADE ");
        stb.append("       , '' AS RELA_HR_CLASS ");
        stb.append("       , RELA_HR_NAME ");
        stb.append("       , RELANAME ");
        stb.append("       , REMARK ");
        stb.append("       , ZAISEKIFLG ");
        stb.append("     FROM ");
        stb.append("       FAMILY_BASE2 ");
        stb.append("   ) ");
        stb.append("   SELECT ");
        stb.append("     T1.SCHREGNO ");
        stb.append("     , T1.GRADE ");
        stb.append("     , T1.HR_CLASS ");
        stb.append("     , T1.ATTENDNO ");
        stb.append("     , T3.HR_NAME ");
        stb.append("     , T4.NAME ");
        stb.append("     , T2.RELANO ");
        stb.append("     , T2.RELA_SCHREGNO ");
        stb.append("     , T2.RELA_HR_NAME ");
        stb.append("     , T2.RELANAME ");
        stb.append("     , T2.REMARK ");
        stb.append("     , T2.ZAISEKIFLG ");
        stb.append("   FROM ");
        stb.append("     REGD_DATA T1 ");
        stb.append("     LEFT JOIN FAMILY_DATA T2 ");
        stb.append("       ON T2.MAIN_SCHREGNO = T1.SCHREGNO ");
        stb.append("       AND ( ");
        stb.append("         (T2.ZAISEKIFLG = '1') ");
        stb.append("         OR (T2.ZAISEKIFLG = '0' AND T2.REMARK IS NOT NULL) ");
        stb.append("       ) ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT T3 ");
        stb.append("       ON T3.YEAR = T1.YEAR ");
        stb.append("       AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("       AND T3.GRADE = T1.GRADE ");
        stb.append("       AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST T4 ");
        stb.append("       ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("   ORDER BY ");
        stb.append("     T1.HR_CLASS ");
        stb.append("     , T1.ATTENDNO ");
        stb.append("     , T2.RELA_GRADE ");
        stb.append("     , T2.RELA_HR_CLASS ");
        stb.append("     , T2.RELANO ");

        return stb.toString();
    }

    private String getStudenShimaneSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAX_GUARDIAN_ADDRESS AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("     FROM ");
        stb.append("         GUARDIAN_ADDRESS_DAT ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO ");
        stb.append(" ),  ");
        stb.append(" GUARDIAN_ADDRESS AS ( ");
        stb.append("     SELECT ");
        stb.append("         T7.* ");
        stb.append("     FROM ");
        stb.append("         GUARDIAN_ADDRESS_DAT T7 ");
        stb.append("         INNER JOIN MAX_GUARDIAN_ADDRESS W7 ");
        stb.append("              ON W7.SCHREGNO  = T7.SCHREGNO ");
        stb.append("             AND W7.ISSUEDATE = T7.ISSUEDATE ");
        stb.append(" ),  ");
        stb.append(" MIN_GUARDIAN AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         MIN(RELATIONSHIP) AS RELATIONSHIP ");
        stb.append("     FROM ");
        stb.append("         GUARDIAN_DAT ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO ");
        stb.append(" ),  ");
        stb.append(" GUARDIAN AS ( ");
        stb.append("     SELECT ");
        stb.append("         T8.* ");
        stb.append("     FROM ");
        stb.append("         GUARDIAN_DAT T8 ");
        stb.append("         INNER JOIN MIN_GUARDIAN W8 ");
        stb.append("              ON W8.SCHREGNO     = T8.SCHREGNO ");
        stb.append("             AND W8.RELATIONSHIP = T8.RELATIONSHIP ");
        stb.append(" ), ");
        stb.append(" SR_DAT AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         YEAR, ");
        stb.append("         SEMESTER, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR         = '" + _param._year + "' ");
        stb.append("         AND SEMESTER = '" + _param._semester + "' ");
        stb.append("         AND GRADE    = '" + _param._grade + "' ");
        stb.append(" )  ");
        stb.append(" SELECT ");
        stb.append("     SR_DAT.SCHREGNO, ");
        stb.append("     SR_GDAT.GRADE_NAME2 AS GRADE_NAME, ");
        stb.append("     SR_HDAT.HR_CLASS_NAME1 AS HR_CLASS_NAME, ");
        stb.append("     VALUE(SR_DAT.ATTENDNO, 0) || '番' AS ATTENDNO, ");
        stb.append("     BASE_MST.NAME, ");
        stb.append("     Z002.NAME1 AS SEX, ");
        stb.append("     SRR_GDAT.GRADE_NAME2 AS RELAGRADE_NAME, ");
        stb.append("     SRR_HDAT.HR_CLASS_NAME1 AS RELAHR_CLASS, ");
        stb.append("     VALUE(SRR_DAT.ATTENDNO, 0) || '番' AS RELAATTENDNO, ");
        stb.append("     RELA_DAT.RELANAME, ");
        stb.append("     R_Z002.NAME1 AS RELASEX, ");
        stb.append("     GADDR.GUARD_ADDR1, ");
        stb.append("     GADDR.GUARD_ADDR2, ");
        stb.append("     GUARDIAN.GUARD_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_RELA_DAT RELA_DAT ");
        stb.append("     INNER JOIN SR_DAT ");
        stb.append("             ON SR_DAT.SCHREGNO    = RELA_DAT.SCHREGNO ");
        stb.append("     LEFT  JOIN SCHREG_REGD_GDAT SR_GDAT ");
        stb.append("             ON SR_GDAT.YEAR       = SR_DAT.YEAR ");
        stb.append("            AND SR_GDAT.GRADE      = SR_DAT.GRADE ");
        stb.append("     LEFT  JOIN SCHREG_REGD_HDAT SR_HDAT ");
        stb.append("             ON SR_HDAT.YEAR       = SR_DAT.YEAR ");
        stb.append("            AND SR_HDAT.SEMESTER   = SR_DAT.SEMESTER ");
        stb.append("            AND SR_HDAT.GRADE      = SR_DAT.GRADE ");
        stb.append("            AND SR_HDAT.HR_CLASS   = SR_DAT.HR_CLASS ");
        stb.append("     LEFT  JOIN SCHREG_BASE_MST BASE_MST ");
        stb.append("             ON BASE_MST.SCHREGNO  = RELA_DAT.SCHREGNO ");
        stb.append("     LEFT  JOIN V_NAME_MST Z002 ");
        stb.append("             ON Z002.YEAR          = SR_DAT.YEAR ");
        stb.append("            AND Z002.NAMECD1       = 'Z002' ");
        stb.append("            AND Z002.NAMECD2       = BASE_MST.SEX ");
        stb.append("     LEFT  JOIN SCHREG_REGD_DAT SRR_DAT ");
        stb.append("             ON SRR_DAT.SCHREGNO   = RELA_DAT.RELA_SCHREGNO ");
        stb.append("            AND SRR_DAT.YEAR       = SR_DAT.YEAR ");
        stb.append("            AND SRR_DAT.SEMESTER   = SR_DAT.SEMESTER ");
        stb.append("     LEFT  JOIN SCHREG_REGD_GDAT SRR_GDAT ");
        stb.append("             ON SRR_GDAT.YEAR      = SRR_DAT.YEAR ");
        stb.append("            AND SRR_GDAT.GRADE     = SRR_DAT.GRADE ");
        stb.append("     LEFT  JOIN SCHREG_REGD_HDAT SRR_HDAT ");
        stb.append("             ON SRR_HDAT.YEAR      = SRR_DAT.YEAR ");
        stb.append("            AND SRR_HDAT.SEMESTER  = SRR_DAT.SEMESTER ");
        stb.append("            AND SRR_HDAT.HR_CLASS  = SRR_DAT.HR_CLASS ");
        stb.append("            AND SRR_HDAT.GRADE     = SRR_DAT.GRADE ");
        stb.append("     LEFT  JOIN V_NAME_MST R_Z002 ");
        stb.append("             ON R_Z002.YEAR        = SR_DAT.YEAR ");
        stb.append("            AND R_Z002.NAMECD1     = 'Z002' ");
        stb.append("            AND R_Z002.NAMECD2     = RELA_DAT.RELASEX ");
        stb.append("     LEFT  JOIN GUARDIAN_ADDRESS GADDR ");
        stb.append("             ON GADDR.SCHREGNO     = RELA_DAT.SCHREGNO ");
        stb.append("     LEFT  JOIN GUARDIAN ");
        stb.append("             ON GUARDIAN.SCHREGNO  = RELA_DAT.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     RELA_DAT.RELA_SCHREGNO IS NOT NULL ");
        stb.append("     AND RELA_DAT.REGD_GRD_FLG = '1' "); // 1:在籍 固定
        stb.append(" ORDER BY ");
        stb.append("     SR_DAT.HR_CLASS, ");
        stb.append("     SR_DAT.ATTENDNO, ");
        stb.append("     SRR_DAT.GRADE, ");
        stb.append("     SRR_DAT.HR_CLASS, ");
        stb.append("     SRR_DAT.ATTENDNO ");
        return stb.toString();
    }

    private class Student {
        final String _schregno;
        final String _hrName;
        final String _name;
        List relList = null;
        String _remark = null;
        boolean _zaisekiFlg = false;
        Student(
                final String schregno,
                final String hrName,
                final String name
        ) {
            _schregno = schregno;
            _hrName   = hrName;
            _name     = name;
            relList   = new ArrayList();
        }
        public String getBrosisName() {
            if (relList == null) return "";
            StringBuffer stb = new StringBuffer();
            String sep = "";
            for (final Iterator it = relList.iterator(); it.hasNext();) {
                final Student relStudent = (Student) it.next();
                final String remark = (relStudent._remark != null) ? relStudent._remark :  "";
                stb.append(sep);
                if (relStudent._zaisekiFlg) {
                    final String[] relSeiMei = (relStudent._name).replace("　", " ").split(" ");
                    final String brosis1 = relStudent._hrName + " " + relSeiMei[1];

                    stb.append(remark);
                    if (!"".equals(remark) && !"".equals(brosis1)) {
                        stb.append("　");
                    }
                    stb.append(brosis1);
                } else {
                    stb.append(remark);
                }
                    sep = "　";
            }

            final String brosisName = stb.toString();

            return brosisName;
        }
    }

    private class Guardian {
        final String _addr1;
        final String _addr2;
        final String _gname;
        final private List<StudentShimane> _studentShimaneList;

        Guardian(
                final String gradeName,
                final String hrName,
                final String attendNo,
                final String name,
                final String sex,
                final String addr1,
                final String addr2,
                final String gname
        ) {
            _studentShimaneList = new ArrayList<StudentShimane>();
            StudentShimane studentShimane = new StudentShimane(gradeName, hrName, attendNo, name, sex);
            _studentShimaneList.add(studentShimane);
            _addr1 = addr1;
            _addr2 = addr2;
            _gname = gname;
        }

        void add(
                final String gradeName,
                final String hrName,
                final String attendNo,
                final String name,
                final String sex
        ) {
            StudentShimane studentShimane = new StudentShimane(gradeName, hrName, attendNo, name, sex);
            _studentShimaneList.add(studentShimane);
        }

        StudentShimane get(int i) {
            return _studentShimaneList.get(i);
        }

        int size() {
            return _studentShimaneList.size();
        }
    }
    private class StudentShimane {
        final String _gradeName;
        final String _hrName;
        final String _attendNo;
        final String _name;
        final String _sex;

        StudentShimane(
                final String gradeName,
                final String hrName,
                final String attendNo,
                final String name,
                final String sex
        ) {
            _gradeName = gradeName;
            _hrName = hrName;
            _attendNo = attendNo;
            _name = name;
            _sex = sex;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlDate;
        final String _grade;
        final String _gradeName;
        final String _schoolKind;
        final String _useFamilyDat;
        final String _useFormName;

        private boolean _seirekiFlg;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _grade = request.getParameter("GRADE");
            _gradeName = getGradeName(db2);
            _schoolKind = getSchoolKind(db2);
            _useFamilyDat = request.getParameter("useFamilyDat");
            _useFormName = request.getParameter("useFormNameA226B");
        }

        private String getGradeName(final DB2UDB db2) {
            String gradeName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    gradeName = rs.getString("GRADE_NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradeName;
        }

        private String getSchoolKind(final DB2UDB db2) {
            String schoolKind = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    schoolKind = rs.getString("SCHOOL_KIND");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolKind;
        }

    }

}// クラスの括り
