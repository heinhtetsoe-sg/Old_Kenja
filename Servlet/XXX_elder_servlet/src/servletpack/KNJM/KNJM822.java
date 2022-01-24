/*
 * $Id: d9ca631a04332b1ca15d85ba9dadc1f297419b93 $
 *
 * 作成日: 2012/11/29
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * 受講科目一覧
 */
public class KNJM822 {

    private static final Log log = LogFactory.getLog(KNJM822.class);

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
    
    private static int getMS932ByteLength(final String name) {
        int len = 0;
        if (null != name) {
            try {
                len = name.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return len;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final int maxSubcCol = 12;
        final int maxLine = 15;
        
        svf.VrSetForm("KNJM822.frm", 4);
        int page = 0;
        final List formLineList = FormLine.getFormLineList(maxSubcCol, getStudentList(db2));
        final int maxPage = formLineList.size() / maxLine + (formLineList.size() % maxLine == 0 ? 0 : 1);
        for (int j = 0; j < formLineList.size(); j++) {
            final FormLine fline = (FormLine) formLineList.get(j);
            if ((j + 1) % maxLine == 1) {
                page += 1;
                svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度"); // 年度
                svf.VrsOut("PAGE", String.valueOf(page)); // ページ
                svf.VrsOut("TOTAL_PAGE", String.valueOf(maxPage)); // 総ページ数
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate) + "現在");
            }
            if (null != fline._student) {
                svf.VrsOut("SCHREG_NO", fline._student._schregno); // 学籍番号
                svf.VrsOut("CREDIT_NAME", "累計"); // 累計表示名
                svf.VrsOut("CREDIT", fline._student._totalCredit); // 単位数
                svf.VrsOut("SCHE_GRAD", fline._student._sotsuyo); // 卒業予定出力
                if (getMS932ByteLength(fline._student._nameKana) > 28) {
                    svf.VrsOut("NAME_KANA2", fline._student._nameKana); // かな氏名
                } else {
                    svf.VrsOut("NAME_KANA1", fline._student._nameKana); // かな氏名
                }
                if (getMS932ByteLength(fline._student._name) > 20) {
                    final String[] token = KNJ_EditEdit.get_token(fline._student._name, 20, 2);
                    if (null != token) {
                        for (int i = 0; i < token.length; i++) {
                            svf.VrsOut("NAME3_" + (i + 1), token[i]); // 氏名
                        }
                    }
                } else if (getMS932ByteLength(fline._student._name) > 14) {
                    svf.VrsOut("NAME2", fline._student._name); // 氏名
                } else {
                    svf.VrsOut("NAME1", fline._student._name); // 氏名
                }
            }
            for (int k = 0; k < fline._subclassList.size(); k++) {
                final Subclass subclass = (Subclass) fline._subclassList.get(k);
                svf.VrsOutn("SUBCLASS_ABBV", (k + 1), subclass._subclassabbv); // 科目略称
                if (null != subclass._subclassCd && subclass._subclassCd.length() >= 2) { // 科目コード
                    svf.VrsOutn("CLASS_CD", (k + 1), subclass._subclassCd.substring(0, 2));
                    svf.VrsOutn("SUBCLASS_CD", (k + 1), subclass._subclassCd.substring(2));
                }
                if ("KOUNIN".equals(subclass._flg)) {
                    svf.VrsOutn("SEM_COMP", (k + 1), "高認");
                } else if ("ZOUTAN".equals(subclass._flg)) {
                    svf.VrsOutn("SEM_COMP", (k + 1), "増単");
                } else {
                    svf.VrsOutn("SEM_COMP", (k + 1), subclass._hokan); // 前期補完/後期補完表示
                    if ("1".equals(subclass._koukiHokanFlg)) {
                        // 後期補完
                        final String attr = "Paint=(0,85,2)";
                        svf.VrAttributen("SUBCLASS_ABBV", (k + 1), attr);
                        svf.VrAttributen("CLASS_CD", (k + 1), attr);
                        svf.VrAttributen("SUBCLASS_CD", (k + 1), attr);
                        svf.VrAttributen("SEM_COMP", (k + 1), attr);
                    }
                }
            }
            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private List getStudentList(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        // 履修科目をセット
        try {
            final String sql = getSubclassStdSelectSql();
            log.debug(" subclassStdSelectSql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                Student student = getStudent(list, rs.getString("SCHREGNO"));
                if (null == student) {
                    student = new Student(rs.getString("SCHREGNO"), rs.getString("NAME"), rs.getString("NAME_KANA"));
                    list.add(student);
                }
                student._subclassList.add(new Subclass(rs.getString("SUBCLASSABBV"), rs.getString("FLG"), rs.getString("CURRICULUM_CD"), rs.getString("SUBCLASSCD"), rs.getString("HOKAN"), rs.getString("KOUKI_HOKAN_FLG")));
            }
        } catch (SQLException ex) {
            log.fatal("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        // 累計単位をセット
        try {
            final String sql = getTotalCreditSql();
            log.debug(" totalCreditSql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                final Student student = getStudent(list, rs.getString("SCHREGNO"));
                if (null == student) {
                    continue;
                }
                final int sateiTanni = NumberUtils.isNumber(rs.getString("SATEI_TANNI")) ? Integer.parseInt(rs.getString("SATEI_TANNI")) : 0;
                final int totalCredit = NumberUtils.isNumber(rs.getString("TOTAL_CREDIT")) ? Integer.parseInt(rs.getString("TOTAL_CREDIT")) : 0;
                if (null != rs.getString("SATEI_TANNI") || null != rs.getString("TOTAL_CREDIT")) {
                    student._totalCredit = String.valueOf(sateiTanni + totalCredit);
                }
                if (null != rs.getString("GRD_YOTEI")) {
                    student._sotsuyo = "卒予";
                }
            }
        } catch (SQLException ex) {
            log.fatal("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }
    
    private String getSubclassStdSelectSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH SCHREGNOS AS (");
        stb.append("   SELECT  ");
        stb.append("     T1.SCHREGNO, T4.NAME, T4.NAME_KANA ");
        stb.append("   FROM SCHREG_REGD_DAT T1 ");
        stb.append("   INNER JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("   WHERE ");
        stb.append("       T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("       AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        if ("1".equals(_param._output)) {
            stb.append("             AND T1.SCHREGNO BETWEEN '" + _param._sSchregno + "' AND '" + _param._eSchregno + "'");
        }
        if (!_param._year.equals(_param._ctrlYear)) {
            stb.append("     UNION ");
            stb.append("     SELECT T2.SCHREGNO, T2.NAME, T2.NAME_KANA ");
            stb.append("     FROM FRESHMAN_DAT T2 ");
            stb.append("   WHERE ");
            stb.append("       T2.ENTERYEAR = '" + _param._year + "' ");
            if ("1".equals(_param._output)) {
                stb.append("           AND T2.SCHREGNO BETWEEN '" + _param._sSchregno + "' AND '" + _param._eSchregno + "'");
            }
        }
        stb.append(" ), SUBCLASSES AS ( ");
        stb.append("   SELECT  ");
        stb.append("       'STD_SELECT' AS FLG, ");
        stb.append("       T1.SCHREGNO, ");
        stb.append("       T2.CLASSCD, ");
        stb.append("       T2.SCHOOL_KIND, ");
        stb.append("       T2.CURRICULUM_CD, ");
        stb.append("       T2.SUBCLASSCD ");
        stb.append("   FROM SCHREGNOS T1 ");
        stb.append("   INNER JOIN SUBCLASS_STD_SELECT_DAT T2 ON T2.YEAR = '" + _param._year + "' ");
        if (!_param._year.equals(_param._ctrlYear)) {
            stb.append("       AND T2.SEMESTER = '1' ");
        } else {
            stb.append("       AND T2.SEMESTER = '" + _param._ctrlSemester + "' ");
        }
        stb.append("       AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   UNION ");
        stb.append("   SELECT  ");
        stb.append("       'KOUNIN' AS FLG, ");
        stb.append("       T1.SCHREGNO, ");
        stb.append("       T2.CLASSCD, ");
        stb.append("       T2.SCHOOL_KIND, ");
        stb.append("       T2.CURRICULUM_CD, ");
        stb.append("       T2.SUBCLASSCD ");
        stb.append("   FROM SCHREGNOS T1 ");
        stb.append("   INNER JOIN SCH_COMP_DETAIL_DAT T2 ON T2.YEAR = '" + _param._year + "' ");
        stb.append("       AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("       AND T2.KOUNIN = '1' ");
        stb.append("   UNION ");
        stb.append("   SELECT  ");
        stb.append("       'ZOUTAN' AS FLG, ");
        stb.append("       T1.SCHREGNO, ");
        stb.append("       T2.CLASSCD, ");
        stb.append("       T2.SCHOOL_KIND, ");
        stb.append("       T2.CURRICULUM_CD, ");
        stb.append("       T2.SUBCLASSCD ");
        stb.append("   FROM SCHREGNOS T1 ");
        stb.append("   INNER JOIN SCH_COMP_DETAIL_DAT T2 ON T2.YEAR = '" + _param._year + "' ");
        stb.append("       AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("       AND VALUE(T2.KOUNIN, '') <> '1' ");
        stb.append(" ) ");
        stb.append(" SELECT  ");
        stb.append("     SUBSTR(T1.SCHREGNO, 1, 4) , SUBSTR(T1.SCHREGNO, 5, 4), T1.SCHREGNO, T1.NAME, T1.NAME_KANA, ");
        stb.append("     T3.SUBCLASSABBV, ");
        stb.append("     T2.FLG, ");
        stb.append("     T2.CLASSCD, ");
        stb.append("     T2.SCHOOL_KIND, ");
        stb.append("     T2.CURRICULUM_CD, ");
        stb.append("     T2.SUBCLASSCD, ");
        stb.append("     CASE WHEN (T2.CLASSCD, T2.CURRICULUM_CD, T2.SUBCLASSCD, T2.SCHOOL_KIND) ");
        stb.append("                IN (SELECT NAME1, NAME2, NAME3, VALUE(ABBV1, 'H') FROM NAME_MST WHERE NAMECD1 = 'M015' AND NAMESPARE1 = '1') THEN '前期補完' ");
        stb.append("          WHEN (T2.CLASSCD, T2.CURRICULUM_CD, T2.SUBCLASSCD, T2.SCHOOL_KIND) ");
        stb.append("                IN (SELECT NAME1, NAME2, NAME3, VALUE(ABBV1, 'H') FROM NAME_MST WHERE NAMECD1 = 'M016' AND NAMESPARE1 = '1') THEN '後期補完' ");
        stb.append("     END AS HOKAN, ");
        stb.append("     CASE WHEN (T2.CLASSCD, T2.CURRICULUM_CD, T2.SUBCLASSCD, T2.SCHOOL_KIND) ");
        stb.append("                IN (SELECT NAME1, NAME2, NAME3, VALUE(ABBV1, 'H') FROM NAME_MST WHERE NAMECD1 = 'M015') THEN '0' ");
        stb.append("          WHEN (T2.CLASSCD, T2.CURRICULUM_CD, T2.SUBCLASSCD, T2.SCHOOL_KIND) ");
        stb.append("                IN (SELECT NAME1, NAME2, NAME3, VALUE(ABBV1, 'H') FROM NAME_MST WHERE NAMECD1 = 'M016') THEN '1' ");
        stb.append("     END AS KOUKI_HOKAN_FLG ");
        stb.append(" FROM SCHREGNOS T1 ");
        stb.append(" INNER JOIN SUBCLASSES T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" INNER JOIN SUBCLASS_MST T3 ON T3.CLASSCD = T2.CLASSCD ");
        stb.append("     AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("     AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("     AND T3.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append(" ORDER BY ");
        stb.append("     SUBSTR(T1.SCHREGNO, 1, 4) DESC, SUBSTR(T1.SCHREGNO, 5, 4), T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, T2.SUBCLASSCD ");
        return stb.toString();
    }

    private String getTotalCreditSql() {
        final StringBuffer stb = new StringBuffer();
        // 表示対象学籍番号（指定年度の履修科目登録がある生徒）
        stb.append(" WITH SCHREGNOS AS ( ");
        stb.append("     SELECT DISTINCT T1.SCHREGNO ");
        stb.append("     FROM SUBCLASS_STD_SELECT_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("         AND T2.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     WHERE T1.YEAR = '" + _param._year + "' ");
        if (!_param._year.equals(_param._ctrlYear)) {
            stb.append("         AND T1.SEMESTER = '1' ");
        } else {
            stb.append("         AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        }
        stb.append("         AND T1.CLASSCD <= '90' ");
        if ("1".equals(_param._output)) {
            stb.append("           AND T1.SCHREGNO BETWEEN '" + _param._sSchregno + "' AND '" + _param._eSchregno + "'");
        }
        if (!_param._year.equals(_param._ctrlYear)) {
            stb.append("     UNION ");
            stb.append("     SELECT DISTINCT T1.SCHREGNO ");
            stb.append("     FROM SUBCLASS_STD_SELECT_DAT T1 ");
            stb.append("     INNER JOIN FRESHMAN_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T2.ENTERYEAR = '" + _param._year + "' ");
            stb.append("     WHERE T1.YEAR = '" + _param._year + "' ");
            stb.append("         AND T1.SEMESTER = '1' ");
            stb.append("         AND T1.CLASSCD <= '90' ");
            if ("1".equals(_param._output)) {
                stb.append("           AND T1.SCHREGNO BETWEEN '" + _param._sSchregno + "' AND '" + _param._eSchregno + "'");
            }
        }
        // 成績（履修済み科目）の単位
        stb.append(" ), STUDYREC AS ( ");
        stb.append("     SELECT T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, ");
        stb.append("            SUM(CASE WHEN T1.GET_CREDIT IS NULL AND T1.ADD_CREDIT IS NULL THEN CAST(NULL AS SMALLINT) ");
        stb.append("                ELSE VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0) ");
        stb.append("                END ");
        stb.append("               ) AS CREDIT ");
        stb.append("     FROM SCHREG_STUDYREC_DAT T1 ");
        stb.append("     INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR <= '" + _param._ctrlYear + "' ");
        stb.append("         AND T1.CLASSCD <= '90' ");
        stb.append("     GROUP BY T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
        // 成績（今年度履修した科目）の単位
        stb.append(" ), RECORD AS ( ");
        stb.append("     SELECT T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD,  ");
        stb.append("            SUM(");
        stb.append("              CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0) ");
        stb.append("                   ELSE T1.GET_CREDIT ");
        stb.append("              END) AS CREDIT ");
        stb.append("     FROM RECORD_DAT T1 ");
        stb.append("     INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._ctrlYear + "' ");
        stb.append("         AND T1.CLASSCD <= '90' ");
        stb.append("     GROUP BY T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
        // 成績（履修登録科目）の単位（次年度指定の場合、次年度 + 今年度。今年度指定の場合、今年度のみ。）
        stb.append(" ), SUBCLASS_STD_SELECT AS ( ");
        stb.append("     SELECT T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, SUM(T3.CREDITS) AS CREDIT ");
        stb.append("     FROM SUBCLASS_STD_SELECT_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     INNER JOIN CREDIT_MST T3 ON T3.YEAR = T2.YEAR ");
        stb.append("         AND T3.COURSECD = T2.COURSECD ");
        stb.append("         AND T3.MAJORCD = T2.MAJORCD ");
        stb.append("         AND T3.GRADE = T2.GRADE ");
        stb.append("         AND T3.COURSECODE = T2.COURSECODE ");
        stb.append("         AND T3.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T3.SUBCLASSCD = T1.SUBCLASSCD     ");
        stb.append("     INNER JOIN SCHREGNOS T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     WHERE ");
        if (!_param._year.equals(_param._ctrlYear)) {
            stb.append("         (T1.YEAR = '" + _param._ctrlYear + "' AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
            stb.append("           OR T1.YEAR = '" + _param._year + "' AND T1.SEMESTER = '1') ");
        } else {
            stb.append("         (T1.YEAR = '" + _param._year + "' AND T1.SEMESTER = '" + _param._ctrlSemester + "') ");
        }
        stb.append("         AND T1.CLASSCD <= '90' ");
        stb.append("     GROUP BY T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
        // 高認
        stb.append("   UNION ALL ");
        stb.append("     SELECT T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, VALUE(T3.CREDITS, 0) AS CREDIT ");
        stb.append("     FROM SCH_COMP_DETAIL_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.YEAR = T1.YEAR ");
        stb.append("     INNER JOIN CREDIT_MST T3 ON T3.YEAR = T2.YEAR ");
        stb.append("         AND T3.COURSECD = T2.COURSECD ");
        stb.append("         AND T3.MAJORCD = T2.MAJORCD ");
        stb.append("         AND T3.GRADE = T2.GRADE ");
        stb.append("         AND T3.COURSECODE = T2.COURSECODE ");
        stb.append("         AND T3.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T3.SUBCLASSCD = T1.SUBCLASSCD     ");
        stb.append("     INNER JOIN SCHREGNOS T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     WHERE ");
        if (!_param._year.equals(_param._ctrlYear)) {
            stb.append("         (T1.YEAR = '" + _param._ctrlYear + "' AND T2.SEMESTER = '" + _param._ctrlSemester + "' ");
            stb.append("           OR T1.YEAR = '" + _param._year + "' AND T2.SEMESTER = '1') ");
        } else {
            stb.append("         (T1.YEAR = '" + _param._year + "' AND T2.SEMESTER = '" + _param._ctrlSemester + "') ");
        }
        stb.append("         AND T1.KOUNIN = '1' ");
        stb.append("         AND T1.CLASSCD <= '90' ");
        // 増単
        stb.append("   UNION ALL ");
        stb.append("     SELECT T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, VALUE(T1.ADD_CREDIT, 0) AS CREDIT ");
        stb.append("     FROM SCH_COMP_DETAIL_DAT T1 ");
        stb.append("     INNER JOIN SCHREGNOS T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     WHERE ");
        if (!_param._year.equals(_param._ctrlYear)) {
            stb.append("         (T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("           OR T1.YEAR = '" + _param._year + "') ");
        } else {
            stb.append("         (T1.YEAR = '" + _param._year + "') ");
        }
        stb.append("         AND VALUE(T1.KOUNIN, '') <> '1' ");
        stb.append("         AND T1.CLASSCD <= '90' ");

        stb.append(" ), YEAR_SUBCLASS AS ( ");
        stb.append("     SELECT YEAR, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD FROM STUDYREC ");
        stb.append("     UNION ");
        stb.append("     SELECT YEAR, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD FROM RECORD ");
        stb.append("     UNION ");
        stb.append("     SELECT YEAR, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD FROM SUBCLASS_STD_SELECT ");
        // メイン表
        stb.append(" ), CREDIT_MAIN AS ( ");
        stb.append("     SELECT  ");
        stb.append("         T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, ");
        stb.append("         T2.CREDIT AS STUDYREC_CREDIT, ");
        stb.append("         T3.CREDIT AS RECORD_CREDIT, ");
        stb.append("         T4.CREDIT AS SUBCLASS_STD_SELECT_CREDIT, ");
        // 単位の優先順位 => SCHREGNO_STUDYREC_DAT, RECORD_DAT, SUBCLASS_STD_SELECT(CREDIT_MST)
        stb.append("         CASE WHEN T2.CREDIT IS NOT NULL THEN T2.CREDIT ");
        stb.append("              WHEN T3.CREDIT IS NOT NULL THEN T3.CREDIT ");
        stb.append("              ELSE T4.CREDIT ");
        stb.append("         END AS CREDIT ");
        stb.append("     FROM ");
        stb.append("         YEAR_SUBCLASS T1     ");
        stb.append("         LEFT JOIN STUDYREC T2 ON T2.YEAR = T1.YEAR AND T2.SCHREGNO = T1.SCHREGNO AND T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         LEFT JOIN RECORD T3 ON T3.YEAR = T1.YEAR AND T3.SCHREGNO = T1.SCHREGNO AND T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         LEFT JOIN SUBCLASS_STD_SELECT T4 ON T4.YEAR = T1.YEAR AND T4.SCHREGNO = T1.SCHREGNO AND T4.CLASSCD = T1.CLASSCD AND T4.SCHOOL_KIND = T1.SCHOOL_KIND AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" ), BASE_YEAR_DETAIL AS ( ");
        stb.append("     SELECT MAIN.SCHREGNO, SC_YD.BASE_REMARK1 AS GRD_YOTEI ");
        stb.append("     FROM SCHREGNOS MAIN ");
        stb.append("     INNER JOIN SCHREG_BASE_YEAR_DETAIL_MST SC_YD ON SC_YD.SCHREGNO = MAIN.SCHREGNO ");
        stb.append("          AND SC_YD.YEAR = '" + _param._year + "' ");
        stb.append("          AND SC_YD.BASE_SEQ = '001' ");
        stb.append(" ), BASE_DETAIL AS ( ");
        stb.append("     SELECT MAIN.SCHREGNO, SC_D.BASE_REMARK1 AS SATEI_TANNI ");
        stb.append("     FROM SCHREGNOS MAIN ");
        stb.append("     INNER JOIN SCHREG_BASE_DETAIL_MST SC_D ON SC_D.SCHREGNO = MAIN.SCHREGNO ");
        stb.append("          AND SC_D.BASE_SEQ = '004' ");
        stb.append("          AND SC_D.BASE_REMARK1 IS NOT NULL ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, L1.GRD_YOTEI, L2.TOTAL_CREDIT, L3.SATEI_TANNI ");
        stb.append(" FROM ");
        stb.append("    (SELECT SCHREGNO FROM BASE_YEAR_DETAIL ");
        stb.append("     UNION ");
        stb.append("     SELECT SCHREGNO FROM BASE_DETAIL ");
        stb.append("     UNION ");
        stb.append("     SELECT SCHREGNO FROM CREDIT_MAIN ");
        stb.append("    ) T1 ");
        stb.append("    LEFT JOIN BASE_YEAR_DETAIL L1 ON L1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("    LEFT JOIN (SELECT SCHREGNO, SUM(CREDIT) AS TOTAL_CREDIT FROM CREDIT_MAIN ");
        stb.append("               GROUP BY SCHREGNO) L2 ON L2.SCHREGNO = T1.SCHREGNO ");
        stb.append("    LEFT JOIN BASE_DETAIL L3 ON L3.SCHREGNO = T1.SCHREGNO  ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHREGNO ");
        return stb.toString();
    }
    
    private static class Student {
        final String _schregno;
        final String _name;
        final String _nameKana;
        final List _subclassList = new ArrayList();
        private String _totalCredit = null; // 累計単位数
        private String _sotsuyo = null; // 卒業予定文言 
        Student(final String schregno, final String name, final String nameKana) {
            _schregno = schregno;
            _name = name;
            _nameKana = nameKana;
        }
    }
    
    private static class Subclass {
        final String _subclassabbv;
        final String _flg;
        final String _curriculumCd;
        final String _subclassCd;
        final String _hokan;
        final String _koukiHokanFlg;
        public Subclass(final String subclassabbv, final String flg, final String curriculumCd, final String subclassCd, final String hokan, final String koukiHokanFlg) {
            _subclassabbv = subclassabbv;
            _flg = flg;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _hokan = hokan;
            _koukiHokanFlg = koukiHokanFlg;
        }
    }
    
    private static class FormLine {
        final List _subclassList = new ArrayList();
        Student _student = null;
        
        private static List getFormLineList(final int maxSubcCol, final List studentList) {
            final List rtn = new ArrayList();
            FormLine current = null;
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                current = new FormLine();
                rtn.add(current);
                current._student = student;
                for (final Iterator subit = student._subclassList.iterator(); subit.hasNext();) {
                    final Subclass subclass = (Subclass) subit.next();
                    if (current._subclassList.size() >= maxSubcCol) {
                        current = new FormLine();
                        rtn.add(current);
                    }
                    current._subclassList.add(subclass);
                }
            }
            return rtn;
        }
    }

    
    private Student getStudent(final List studentList, final String schregno) {
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (student._schregno.equals(schregno)) {
                return student;
            }
        }
        return null;
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
        final String _year; // ログイン年度 もしくは ログイン年度 + 1
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _loginDate;
        final String _output;
        final String _sSchregno;
        final String _eSchregno;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _output = request.getParameter("OUTPUT");
            _sSchregno = request.getParameter("SSCHREGNO");
            _eSchregno = request.getParameter("ESCHREGNO");
        }

    }
}

// eof

