// kanji=漢字
/*
 * $Id: a75c7e7ea199833881805288664a2e4904ac9a09 $
 *
 * 作成日: 2013/01/17 14:38:44 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2013 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: a75c7e7ea199833881805288664a2e4904ac9a09 $
 */
public class KNJA223M {

    private static final Log log = LogFactory.getLog("KNJA223M.class");

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
        final int maxRowCnt = 3;
        final int maxCnt = "sagaken".equals(_param._schoolName) ? 50 : 8;//佐賀通信制:50件　佐賀通信制以外:8件
        final List printHrList = getPrintHrList(db2);

        if("sagaken".equals(_param._schoolName)){
            //佐賀通信制
            int rowCnt = 1;
            svf.VrSetForm("KNJA223M_2.frm", 1);
            for (final Iterator iter = printHrList.iterator(); iter.hasNext();) {
                _hasData = true;
                final HrClass hrClass = (HrClass) iter.next();
                for (int i = 0; i < Integer.parseInt(_param._kensuu); i++) {
                    for (final Iterator itpg = getPageList(hrClass._studentList, maxRowCnt * maxCnt).iterator(); itpg.hasNext();) {
                        if(rowCnt > maxRowCnt) {
                            svf.VrEndPage();
                            rowCnt = 1;
                        }

                        //ヘッダー
                        svf.VrsOut("HR_NAME" + rowCnt, hrClass._hrName); //学級
                        svf.VrsOut("STAFFNAME" + rowCnt + "_1", hrClass._staffName); //担任名

                        final List page = (List) itpg.next();

                        for (int cnt = 1; cnt <= maxCnt; cnt++) {

                            final int idx = cnt - 1;
                            if (page.size() <= idx) {
                                continue;
                            }

                            final Student student = (Student) page.get(idx);
                            svf.VrsOutn("ATTENDNO" + rowCnt, cnt, student._attendNo); // 出席番号

                            if("on".equals(_param._mark)) {
                                // 男:空白 女:'*'
                                svf.VrsOutn("MARK" + rowCnt, cnt, student._sex); // 性別区分
                            }

                            // 生徒漢字・規則に従って出力
                            final String names = StringUtils.defaultString(student._name);
                            final int z = names.indexOf("　"); // 空白文字の位置
                            String strx = "";
                            String stry = "";
                            String field1 = null;
                            String field2 = null;
                            if (z != -1) {
                                strx = names.substring(0, z); // 姓
                                stry = names.substring(z + 1); // 名
                                if (strx.length() == 1) {
                                    field1 = "LNAME" + rowCnt + "_2"; // 姓１文字
                                } else {
                                    field1 = "LNAME" + rowCnt + "_1"; // 姓２文字以上
                                }
                                if (stry.length() == 1) {
                                    field2 = "FNAME" + rowCnt + "_2"; // 名１文字
                                } else {
                                    field2 = "FNAME" + rowCnt + "_1"; // 名２文字以上
                                }
                            }
                            if (z != -1 && strx.length() <= 4 && stry.length() <= 4) {
                                svf.VrsOutn(field1, cnt, strx); //性
                                svf.VrsOutn(field2, cnt, stry); //名
                            } else {
                                svf.VrsOutn("NAME" + rowCnt, cnt, names); //空白がない
                            }

                            if ("2".equals(_param._kanaOutType)) {
                                svf.VrsOutn("KANA" + rowCnt + "", cnt, student._schregNo); // ふりがな
                            } else {
                                final String kanaField = getMS932ByteLength(student._nameKana) > 22 ? "_2" : "";
                                svf.VrsOutn("KANA" + rowCnt + kanaField, cnt, student._nameKana); // ふりがな
                            }

                        }
                        rowCnt++;
                    }
                }
            }
            svf.VrEndPage();
        }else {
            //佐賀通信制 以外
            svf.VrSetForm("KNJA223M.frm", 4);
            for (final Iterator iter = printHrList.iterator(); iter.hasNext();) {
                _hasData = true;
                final HrClass hrClass = (HrClass) iter.next();
                for (int i = 0; i < Integer.parseInt(_param._kensuu); i++) {
                    setHead(svf, hrClass);
                    for (final Iterator itpg = getPageList(hrClass._studentList, maxRowCnt * maxCnt).iterator(); itpg.hasNext();) {
                        final List page = (List) itpg.next();

                        for (int cnt = 1; cnt <= maxCnt; cnt++) {
                            for (int rowCnt = 1; rowCnt <= maxRowCnt; rowCnt++) {
                                final int idx = (rowCnt - 1) * maxCnt + (cnt - 1);
                                if (page.size() <= idx) {
                                    continue;
                                }

                                final Student student = (Student) page.get(idx);
                                svf.VrsOut("SCHREGNO_" + rowCnt, student._schregNo);
                                svf.VrsOut("AGE_" + rowCnt, student._age);
                                if ("1".equals(_param._naRetu)) {
                                    svf.VrsOut("KANA_" + rowCnt, student._nameKana);
                                }
                                svf.VrsOut("NAME_" + rowCnt, student._name);
                                svf.VrsOut("CREDIT_" + rowCnt, String.valueOf(student._totalCredit));
                                svf.VrsOut("REMARK1_" + rowCnt, student._grdYotei);
                                svf.VrsOut("REMARK2_" + rowCnt, student._handicapName);
                                svf.VrsOut("TEL1_" + rowCnt, student._tell1);
                                svf.VrsOut("TEL2_" + rowCnt, student._tell2);
                            }
                            svf.VrEndRecord();
                        }
                    }
                }
            }
        }

    }

    /**
     * @param svf
     * @param hrClass
     */
    private void setHead(final Vrw32alp svf, final HrClass hrClass) {
        svf.VrsOut("HR_NAME", hrClass._hrName);
        svf.VrsOut("STAFFNAME", hrClass._staffName);
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

    /**
     * @return
     */
    private List getPrintHrList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        for (int ia = 0; ia < _param._classcd.length; ia++) {
            final String getHrSql = getHrSql(_param._classcd[ia]);
            try {
                ps = db2.prepareStatement(getHrSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrClassCd = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String hrNameAbbv = rs.getString("HR_NAMEABBV");
                    final String trCd1 = rs.getString("TR_CD1");
                    final String staffName = rs.getString("STAFFNAME");
                    final HrClass hrClass = new HrClass(grade, hrClassCd, hrName, hrNameAbbv, trCd1, staffName);
                    hrClass.setStudent(db2);
                    retList.add(hrClass);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        return retList;
    }

    /**
     * @return
     */
    private String getHrSql(final String hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.HR_NAME, ");
        stb.append("     T1.HR_NAMEABBV, ");
        stb.append("     T1.TR_CD1, ");
        stb.append("     ST.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT T1 ");
        stb.append("     LEFT JOIN STAFF_MST ST ON T1.TR_CD1 = ST.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SEMESTER = '1' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + hrClass + "' ");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private List getPageList(final List list, final int size) {
        final List pagelist = new ArrayList();
        List page = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            Object o = it.next();
            if (page == null || page.size() >= size) {
                page = new ArrayList();
                pagelist.add(page);
            }
            page.add(o);
        }
        return pagelist;
    }

    private class HrClass {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrNameAbbv;
        final String _trCd1;
        final String _staffName;
        final List _studentList;
        /**
         * コンストラクタ。
         */
        HrClass(
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrNameAbbv,
                final String trCd1,
                final String staffName
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _trCd1 = trCd1;
            _staffName = staffName;
            _studentList = new ArrayList();
        }
        /**
         * @param db2
         */
        public void setStudent(final DB2UDB db2) throws SQLException {
            final String studentSql = getStudentSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(studentSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregNo = rs.getString("SCHREGNO");
                    final String attendNo = rs.getString("ATTENDNO");
                    final String name = rs.getString("NAME");
                    final String nameKana = rs.getString("NAME_KANA");
                    final String sex = rs.getString("SEX");
                    final String age = rs.getString("AGE");
                    final String tell1 = rs.getString("SEND_TELNO");
                    final String tell2 = rs.getString("SEND_TELNO2");
                    final String grdYotei = rs.getString("GRD_YOTEI");
                    final String handicapName = rs.getString("HANDICAP_NAME");
                    final int totalCredit = rs.getInt("TOTAL_CREDIT");
                    final Student student = new Student(schregNo, attendNo, name, nameKana, sex, age, tell1, tell2, grdYotei, handicapName, totalCredit);
                    _studentList.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        /**
         * @return
         */
        private String getStudentSql() {
            final StringBuffer stb = new StringBuffer();

            // 表示対象学籍番号（指定年度の履修科目登録がある生徒）
            stb.append(" WITH SCHREGNOS AS ( ");
            stb.append("     SELECT DISTINCT T1.SCHREGNO ");
            stb.append("     FROM SUBCLASS_STD_SELECT_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T2.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("         AND T2.SEMESTER = '" + _param._ctrlSemester + "' ");
            stb.append("         AND T2.GRADE || T2.HR_CLASS = '" + _grade + _hrClass + "' ");
            stb.append("     WHERE T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("         AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
            stb.append("         AND T1.CLASSCD <= '90' ");
            // 成績（履修済み科目）の単位
            stb.append(" ), STUDYREC AS ( ");
            stb.append("     SELECT T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, ");
            stb.append("            SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS CREDIT ");
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
            stb.append("         AND T2.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("         AND T2.SEMESTER = '" + _param._ctrlSemester + "' ");
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
            stb.append("         (T1.YEAR = '" + _param._ctrlYear + "' AND T1.SEMESTER = '" + _param._ctrlSemester + "') ");
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
            stb.append("         (T1.YEAR = '" + _param._ctrlYear + "' AND T2.SEMESTER = '" + _param._ctrlSemester + "') ");
            stb.append("         AND T1.KOUNIN = '1' ");
            stb.append("         AND T1.CLASSCD <= '90' ");
            // 増単
            stb.append("   UNION ALL ");
            stb.append("     SELECT T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, VALUE(T1.ADD_CREDIT, 0) AS CREDIT ");
            stb.append("     FROM SCH_COMP_DETAIL_DAT T1 ");
            stb.append("     INNER JOIN SCHREGNOS T4 ON T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("         (T1.YEAR = '" + _param._ctrlYear + "') ");
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
            stb.append(" ), BASE_DETAIL AS ( ");
            stb.append("     SELECT MAIN.SCHREGNO, SC_D.BASE_REMARK1 AS SATEI_TANNI ");
            stb.append("     FROM SCHREGNOS MAIN ");
            stb.append("     INNER JOIN SCHREG_BASE_DETAIL_MST SC_D ON SC_D.SCHREGNO = MAIN.SCHREGNO ");
            stb.append("          AND SC_D.BASE_SEQ = '004' ");
            stb.append("          AND SC_D.BASE_REMARK1 IS NOT NULL ");
            stb.append(" ), CREDITS AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         SUM(CASE WHEN L2.TOTAL_CREDIT IS NOT NULL OR L3.SATEI_TANNI IS NOT NULL THEN ");
            stb.append("             VALUE(L2.TOTAL_CREDIT, 0) + INT(VALUE(L3.SATEI_TANNI, '0')) END) AS CREDIT ");
            stb.append("     FROM ");
            stb.append("        (SELECT SCHREGNO FROM BASE_DETAIL ");
            stb.append("         UNION ");
            stb.append("         SELECT SCHREGNO FROM CREDIT_MAIN ");
            stb.append("        ) T1 ");
            stb.append("        LEFT JOIN (SELECT SCHREGNO, SUM(CREDIT) AS TOTAL_CREDIT FROM CREDIT_MAIN ");
            stb.append("                   GROUP BY SCHREGNO) L2 ON L2.SCHREGNO = T1.SCHREGNO ");
            stb.append("        LEFT JOIN BASE_DETAIL L3 ON L3.SCHREGNO = T1.SCHREGNO  ");
            stb.append("     GROUP BY ");
            stb.append("         T1.SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     CASE WHEN BASE.SEX = '2' THEN '*' ELSE '' END AS SEX, ");
            stb.append("     CASE WHEN BASE.BIRTHDAY IS NOT NULL THEN YEAR(DATE('" + _param._ctrlYear + "-04-01') - 1 day - BASE.BIRTHDAY) END AS AGE, ");
            stb.append("     SEND_ADDR.SEND_TELNO, ");
            stb.append("     SEND_ADDR.SEND_TELNO2, ");
            stb.append("     CASE WHEN VALUE(SC_YD.BASE_REMARK1, '0') = '1' ");
            stb.append("          THEN '卒予' ");
            stb.append("          ELSE '' ");
            stb.append("     END AS GRD_YOTEI, ");
            stb.append("     NMA025.NAME1 AS HANDICAP_NAME, ");
            stb.append("     VALUE(CRE.CREDIT, 0) AS TOTAL_CREDIT ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST SC_YD ON T1.SCHREGNO = SC_YD.SCHREGNO ");
            stb.append("          AND T1.YEAR = SC_YD.YEAR ");
            stb.append("          AND SC_YD.BASE_SEQ = '001' ");
            stb.append("     LEFT JOIN SCHREG_SEND_ADDRESS_DAT SEND_ADDR ON T1.SCHREGNO = SEND_ADDR.SCHREGNO ");
            stb.append("          AND SEND_ADDR.DIV = '1' ");
            stb.append("     LEFT JOIN NAME_MST NMA025 ON NMA025.NAMECD1 = 'A025' ");
            stb.append("          AND NMA025.NAMECD2 = BASE.HANDICAP ");
            stb.append("     LEFT JOIN CREDITS CRE ON CRE.SCHREGNO = T1.SCHREGNO ");
            stb.append("  ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + _grade + _hrClass + "' ");
            stb.append(" ORDER BY ");
            if ("sagaken".equals(_param._schoolName)) {
                stb.append("     ATTENDNO ");
            } else {
                stb.append("     SUBSTR(T1.SCHREGNO, 1, 4) DESC, ");
                stb.append("     SUBSTR(T1.SCHREGNO, 5, 4) ");
            }

            return stb.toString();
        }

    }

    private class Student {
        final String _schregNo;
        final String _attendNo;
        final String _name;
        final String _nameKana;
        final String _sex;
        final String _age;
        final String _tell1;
        final String _tell2;
        final String _grdYotei;
        final String _handicapName;
        final int _totalCredit;
        /**
         * コンストラクタ。
         */
        Student(
                final String schregNo,
                final String attendNo,
                final String name,
                final String nameKana,
                final String sex,
                final String age,
                final String tell1,
                final String tell2,
                final String grdYotei,
                final String handicapName,
                final int totalCredit
        ) {
            _schregNo = schregNo;
            _attendNo = attendNo;
            _name = name;
            _nameKana = nameKana;
            _sex = sex;
            _age = age;
            _tell1 = tell1;
            _tell2 = tell2;
            _grdYotei = grdYotei;
            _handicapName = handicapName;
            _totalCredit = totalCredit;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 73711 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _naRetu;
        private final String _kensuu;
        private final String[] _classcd;
        private final String _mark;
        private final String _schoolName;
        private final String _kanaOutType;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _naRetu = request.getParameter("OUTPUT");
            _kensuu = request.getParameter("KENSUU");
            _classcd = request.getParameterValues("CLASS_SELECTED");
            _mark = request.getParameter("MARK");
            _schoolName = getNameMst(db2, "NAME1", "Z010", "00");
            _kanaOutType = request.getParameter("KANA_OUTTYPE");
        }

        public String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

    }
}

// eof
