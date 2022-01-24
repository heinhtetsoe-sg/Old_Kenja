/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 *
 * 作成日: 2021/03/09
 * 作成者: ishimine
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD626H {

    private static final Log log = LogFactory.getLog(KNJD626H.class);

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

    //成績原簿 印刷
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final Map printMap = getPrintMap(db2);

        if (printMap.isEmpty()) return;
        for (Iterator iterator = printMap.keySet().iterator(); iterator.hasNext();) {
            final String chaircd = (String) iterator.next();
            final Chair chair = (Chair) printMap.get(chaircd);

            svf.VrSetForm("KNJD626H.frm" , 4);
            printTitle(svf, chair);

            int cnt = 1; //繰返し回数
            //生徒毎のループ
            for (Iterator stuIte = chair.studentMap.values().iterator(); stuIte.hasNext();) {
                final Student student = (Student) stuIte.next();

                svf.VrsOut("CREDIT", student._credits); //単位
                svf.VrsOut("SEQ", String.valueOf(cnt)); //SEQ
                svf.VrsOut("SEX", "1".equals(student._sex) ? "○" : ""); //性別
                svf.VrsOut("HR", student._hrNameAbbv); //HR
                svf.VrsOut("NO", String.valueOf(Integer.parseInt(student._attendno))); //No.

                //氏名
                final int nameKeta = KNJ_EditEdit.getMS932ByteLength(student._name);
                final String nameField = nameKeta <= 20 ? "1" : nameKeta <= 30 ? "2" : "3";
                svf.VrsOut("NAME" + nameField, student._name);

                svf.VrsOut("SCORE", getMarkHyoutei10(student, student._hyoutei)); //10段階評定
                svf.VrsOut("SICK", student._sick); //欠席時数
                svf.VrsOut("LESSON", student._lesson); //授業時数

                //備考 GRD優先
                if (student._grdName != null) {
                    svf.VrsOut("REMARK", student._grdName);
                } else if (student._transferName != null) {
                    svf.VrsOut("REMARK", student._transferName);
                }

                svf.VrsOut("DIV", getMarkHyoutei5(student, student._hyoutei, student._hyoutei5)); //5段階評定
                if (student._get_Credit != null) { //入力があれば印字
                    svf.VrsOut("GET_CREDIT", student._get_Credit); //修得単位
                }

                svf.VrEndRecord();
                _hasData = true;
                cnt++;
            }
            svf.VrEndPage();
        }
    }

    private void printTitle(final Vrw32alp svf, final Chair chair) {
        svf.VrsOut("NENDO", _param._nendo); //年度

        final int chairKeta = KNJ_EditEdit.getMS932ByteLength(chair._chairName);
        final String chairField = chairKeta <= 44 ? "1" : "2";
        svf.VrsOut("CHAIR_NAME" + chairField, String.valueOf(chair._chairName)); //講座名

        svf.VrsOut("PRINT_DATE", _param._date + " " + _param._time); //作成日付

        int gyo = 1; //印字行

        //担当者
        for (Iterator stfIte = chair.staffMap.keySet().iterator(); stfIte.hasNext();) {
            final String stfCd = (String) stfIte.next();
            final String staffName = (String) chair.staffMap.get(stfCd);
            final int stfKeta = KNJ_EditEdit.getMS932ByteLength(staffName);
            final String stfField = stfKeta <= 20 ? "1" : stfKeta <= 30 ? "2" : "3";
            svf.VrsOutn("TR_NAME" + stfField, gyo, staffName); //担当者名

            final String path = getStaffImageFilePath(stfCd);
            if (path != null) {
                svf.VrsOut("STAFFBTM_" + gyo, path); //担当印があれば印字
            }
            gyo++;
        }

        //男女カウント
        int boy = 0;
        int girl = 0;
        for (Iterator stuIte = chair.studentMap.values().iterator(); stuIte.hasNext();) {
            final Student student = (Student) stuIte.next();
            if ("1".equals(student._sex)) {
                boy++;
            } else {
                girl++;
            }
        }
        svf.VrsOut("CHAIR_NUM1", String.valueOf(boy)); //男
        svf.VrsOut("CHAIR_NUM2", String.valueOf(girl)); //女
        svf.VrsOut("CHAIR_NUM3", String.valueOf(boy + girl)); //男女
        svf.VrsOut("SEMESTER_NAME", _param._semeName); //学期
    }

    //10段階評定算出
    private String getMarkHyoutei10(final Student student, final String hyoutei) {
        final String retHyoutei;

        if (isKekkaOver(student._lesson, student._sick, "3")) { //授業時数1/3より多い欠席なら
            if ("1".equals(_param._semester) || (!"03".equals(student._grade_Cd) && "2".equals(_param._semester))) { //「全学年の1学期」又は「1・2年の2学期」の場合
                if (hyoutei != null) {
                    if (3 >= Integer.parseInt(hyoutei)) {
                        retHyoutei = "*" + hyoutei; //評定3以下なら頭に*
                    } else {
                        retHyoutei = "*保"; //評定4以上なら*保
                    }
                } else {
                    retHyoutei = "*0"; //未入力なら*0
                }
            } else { //「全学年の3学期」又は「3年の2学期」の場合
                if (isKekkaOver(student._lesson, student._sick, "2"))  {
                    retHyoutei = "×"; //授業時数1/2より多い欠席は×
                } else {
                    if (hyoutei != null) {
                        retHyoutei = "*" + hyoutei; //授業時数1/3の欠席なら頭に*
                    } else {
                        retHyoutei = "*0"; //未入力なら*0
                    }
                }
            }
        } else {
            if (hyoutei != null) {
                retHyoutei = hyoutei; //評定
            } else {
                retHyoutei = "0"; //未入力なら0
            }
        }
        return retHyoutei;
    }

    //5段階評定算出
    private String getMarkHyoutei5(final Student student, final String hyoutei, final String hyoutei5) {
        if (hyoutei5 != null) {
            return hyoutei5;
        }
        final String retHyoutei;
        if (isKekkaOver(student._lesson, student._sick, "3")) { //授業時数1/3より多い欠席なら
            if ("1".equals(_param._semester) || (!"03".equals(student._grade_Cd) && "2".equals(_param._semester))) { //「全学年の1学期」又は「1・2年の2学期」の場合
                if (hyoutei != null) {
                    if (2 >= Integer.parseInt(hyoutei)) {
                        retHyoutei = "1"; //評定2以下なら1
                    } else {
                        retHyoutei = "2"; //評定3以上なら2
                    }
                } else {
                    retHyoutei = "保留"; //未入力なら保留
                }
            } else { //「全学年の3学期」又は「3年の2学期」の場合
                if (isKekkaOver(student._lesson, student._sick, "2"))  { //授業時数1/2より多い
                    if ("3".equals(_param._semester)) {
                        retHyoutei = ""; //全学年の3学期は非表示
                    } else {
                        retHyoutei = "1"; //3年の2学期は1
                    }
                } else { //授業時数1/3の欠席なら
                    if (hyoutei != null) {
                        if (3 >= Integer.parseInt(hyoutei)) { //評定3以下なら
                            if (1 == Integer.parseInt(hyoutei)) {
                                retHyoutei = "1"; //評定1なら1
                            } else {
                                retHyoutei = "2"; //以外なら2
                            }
                        } else {
                            retHyoutei = String.valueOf(Integer.parseInt(hyoutei) / 2);
                        }
                    } else {
                        retHyoutei = "保留"; //未入力なら保留
                    }
                }
            }
        } else {
            retHyoutei = "保留"; //保留
        }
        return retHyoutei;
    }

    /**
     * 授業時数の1/2、又は1/3より多い欠席時数を判定
     */
    private boolean isKekkaOver(final String lesson, final String sick, final String divisor) {
        final BigDecimal bd1 = new BigDecimal(lesson);
        final BigDecimal border = bd1.divide(new BigDecimal(divisor), 2, BigDecimal.ROUND_HALF_UP);
        final int result = border.compareTo(new BigDecimal(sick));
        return result == -1 ? true : false;
    }

    private Map getPrintMap(final DB2UDB db2) {
        final Map<String, Chair> retMap = new LinkedHashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getStudentSql();
        log.debug(" sql =" + sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String chaircd = rs.getString("CHAIRCD");
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String grade = rs.getString("GRADE");
                final String grade_Cd = rs.getString("GRADE_CD");
                final String hrNameAbbv = rs.getString("HR_NAMEABBV");
                final String attendno = rs.getString("ATTENDNO");
                final String grdName = rs.getString("GRD_NAME");
                final String grdDate = rs.getString("GRD_DATE");
                final String transferName = rs.getString("TRANSFER_NAME");
                final String transferSdate = rs.getString("TRANSFER_SDATE");
                final String transferEdate = rs.getString("TRANSFER_EDATE");
                final String credits = rs.getString("CREDITS");
                final String hyoutei = rs.getString("HYOUTEI");
                final String hyoutei5 = rs.getString("HYOUTEI5");
                final String get_Credit = rs.getString("GET_CREDIT");
                final String lesson = rs.getString("LESSON");
                final String sick = rs.getString("SICK");

                if (!retMap.containsKey(chaircd)) {
                    retMap.put(chaircd, getChair(db2, chaircd));
                }

                final Chair chair = retMap.get(chaircd);

                if (!chair.studentMap.containsKey(schregno)) {
                    final Student student = new Student(schregno, name, sex, grade, grade_Cd, hrNameAbbv, attendno, grdName,
                            grdDate, transferName, transferSdate, transferEdate, credits, hyoutei, hyoutei5, get_Credit,
                            lesson, sick);
                    chair.studentMap.put(schregno, student);
                }

            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();

        //対象学期内の学籍異動
        stb.append(" WITH TRF_T AS ( ");
        stb.append("     SELECT ");
        stb.append("         TRF1.SCHREGNO, ");
        stb.append("         TRF1.TRANSFERCD, ");
        stb.append("         TRF1.TRANSFER_SDATE, ");
        stb.append("         TRF1.TRANSFER_EDATE, ");
        stb.append("         N1.NAME1 AS TRANSFER_NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_TRANSFER_DAT TRF1 ");
        stb.append("         INNER JOIN ( ");
        stb.append("             SELECT ");
        stb.append("                 SCHREGNO, ");
        stb.append("                 TRANSFERCD, ");
        stb.append("                 MAX(TRANSFER_SDATE) AS TRANSFER_SDATE ");
        stb.append("             FROM ");
        stb.append("                 SCHREG_TRANSFER_DAT ");
        stb.append("             WHERE ");
        stb.append("                 (TO_DATE('" + _param._semeSdate + "', 'YYYY/MM/DD') BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE) ");
        stb.append("             OR  (TRANSFER_SDATE <= TO_DATE('" + _param._semeSdate + "', 'YYYY/MM/DD') AND TRANSFER_EDATE IS NULL) ");
        stb.append("             OR  (TO_DATE('" + _param._semeEdate + "', 'YYYY/MM/DD') BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE) ");
        stb.append("             OR  (TRANSFER_SDATE BETWEEN TO_DATE('" + _param._semeSdate + "', 'YYYY/MM/DD') AND TO_DATE('" + _param._semeEdate + "', 'YYYY/MM/DD')) ");
        stb.append("             GROUP BY ");
        stb.append("                 SCHREGNO, ");
        stb.append("                 TRANSFERCD ");
        stb.append("         ) TRF2 ON ");
        stb.append("             TRF1.SCHREGNO       = TRF2.SCHREGNO ");
        stb.append("         AND TRF1.TRANSFERCD     = TRF2.TRANSFERCD ");
        stb.append("         AND TRF1.TRANSFER_SDATE = TRF2.TRANSFER_SDATE ");
        stb.append("         INNER JOIN ");
        stb.append("             V_NAME_MST N1 ");
        stb.append("              ON N1.YEAR = '" + _param._year + "' ");
        stb.append("             AND N1.NAMECD1 = 'A004' ");
        stb.append("             AND N1.NAMECD2 = TRF1.TRANSFERCD ");
        stb.append(" ),");
        //対象生徒
        stb.append(" SCHREG_BASE AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T2.COURSECD, ");
        stb.append("     T2.MAJORCD, ");
        stb.append("     T2.COURSECODE, ");
        stb.append("     T3.NAME, ");
        stb.append("     T3.SEX, ");
        stb.append("     T3.GRD_DATE, ");
        stb.append("     N1.NAME1 AS GRD_NAME, ");
        stb.append("     T4.TRANSFER_NAME, ");
        stb.append("     T4.TRANSFER_SDATE, ");
        stb.append("     T4.TRANSFER_EDATE, ");
        stb.append("     T5.GRADE_CD, ");
        stb.append("     T6.HR_NAMEABBV ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STD_DAT T1 ");
        stb.append(" INNER JOIN ");
        stb.append("     SCHREG_REGD_DAT T2 ");
        stb.append("      ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" INNER JOIN ");
        stb.append("     SCHREG_BASE_MST T3 ");
        stb.append("      ON T3.SCHREGNO = T2.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     V_NAME_MST N1 ");
        stb.append("      ON N1.YEAR = T1.YEAR ");
        stb.append("     AND N1.NAMECD1 = 'A003' ");
        stb.append("     AND N1.NAMECD2 = T3.GRD_DIV ");
        stb.append(" LEFT JOIN ");
        stb.append("     TRF_T T4 ");
        stb.append("      ON T4.SCHREGNO = T2.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     SCHREG_REGD_GDAT T5 ");
        stb.append("      ON T5.YEAR = T2.YEAR ");
        stb.append("     AND T5.GRADE = T2.GRADE ");
        stb.append("     AND T5.SCHOOL_KIND = 'H' "); //高校固定
        stb.append(" LEFT JOIN ");
        stb.append("     SCHREG_REGD_HDAT T6 ");
        stb.append("      ON T6.YEAR = T2.YEAR ");
        stb.append("     AND T6.SEMESTER = T2.SEMESTER ");
        stb.append("     AND T6.GRADE = T2.GRADE ");
        stb.append("     AND T6.HR_CLASS = T2.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' AND ");
        stb.append("     T1.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("     T1.CHAIRCD  IN " + SQLUtils.whereIn(true, _param._categorySelected));
        stb.append(" ),");
        //科目、単位
        stb.append(" SUBCLASS_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.CLASSCD, ");
        stb.append("     T2.SCHOOL_KIND, ");
        stb.append("     T2.CURRICULUM_CD, ");
        stb.append("     T2.SUBCLASSCD, ");
        stb.append("     T3.CREDITS ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE T1 ");
        stb.append(" INNER JOIN ");
        stb.append("     CHAIR_DAT T2 ");
        stb.append("      ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     CREDIT_MST T3 ");
        stb.append("      ON T3.YEAR = T1.YEAR ");
        stb.append("     AND T3.COURSECD = T1.COURSECD ");
        stb.append("     AND T3.MAJORCD = T1.MAJORCD ");
        stb.append("     AND T3.GRADE = T1.GRADE ");
        stb.append("     AND T3.COURSECODE = T1.COURSECODE ");
        stb.append("     AND T3.CLASSCD = T2.CLASSCD ");
        stb.append("     AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("     AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("     AND T3.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append(" ),");
        //対象学期の評定
        stb.append(" RECORD_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.SCORE AS HYOUTEI ");
        stb.append(" FROM ");
        stb.append("     SUBCLASS_T T1 ");
        stb.append(" LEFT JOIN ");
        stb.append("     RECORD_SCORE_DAT T2 ");
        stb.append("      ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T2.TESTKINDCD = '99' ");
        stb.append("     AND T2.TESTITEMCD = '00' ");
        stb.append("     AND T2.SCORE_DIV = '08' ");
        stb.append("     AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("     AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" ),");
        //9学期評定、修得単位
        stb.append(" RECORD_9 AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.SCORE AS HYOUTEI5, ");
        stb.append("     T2.GET_CREDIT ");
        stb.append(" FROM ");
        stb.append("     SUBCLASS_T T1 ");
        stb.append(" LEFT JOIN ");
        stb.append("     RECORD_SCORE_DAT T2 ");
        stb.append("      ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER = '9' ");
        stb.append("     AND T2.TESTKINDCD = '99' ");
        stb.append("     AND T2.TESTITEMCD = '00' ");
        stb.append("     AND T2.SCORE_DIV = '09' ");
        stb.append("     AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("     AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" ),");
        //対象学期の出欠状況
        stb.append(" ATTEND_T AS (  ");
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     SUM(VALUE(T2.LESSON, 0)) AS LESSON, ");
        stb.append("     SUM(VALUE(T2.OFFDAYS, 0)) AS OFFDAYS, ");
        stb.append("     SUM(VALUE(T2.ABROAD, 0)) AS ABROAD, ");
        stb.append("     SUM(VALUE(T2.SICK, 0)) AS SICK, ");
        stb.append("     SUM(VALUE(T2.NOTICE, 0)) AS NOTICE, ");
        stb.append("     SUM(VALUE(T2.NONOTICE, 0)) AS NONOTICE, ");
        stb.append("     SUM(VALUE(T2.ABSENT, 0)) AS ABSENT, ");
        stb.append("     SUM(VALUE(T2.MOURNING, 0)) AS MOURNING ");
        stb.append(" FROM ");
        stb.append("     SUBCLASS_T T1 ");
        stb.append(" INNER JOIN ");
        stb.append("     ATTEND_SUBCLASS_DAT T2 ");
        stb.append("      ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER <= T1.SEMESTER ");
        stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("     AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" GROUP BY ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" ) ");
        //メイン表
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.SEX, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.GRADE_CD, ");
        stb.append("     T1.HR_NAMEABBV, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.GRD_NAME, ");
        stb.append("     T1.GRD_DATE, ");
        stb.append("     T1.TRANSFER_NAME, ");
        stb.append("     T1.TRANSFER_SDATE, ");
        stb.append("     T1.TRANSFER_EDATE, ");
        stb.append("     T2.CREDITS, ");
        stb.append("     T3.HYOUTEI, ");
        stb.append("     T4.HYOUTEI5, ");
        stb.append("     T4.GET_CREDIT, ");
//        stb.append("     VALUE(T5.LESSON, 0) - VALUE(T5.OFFDAYS, 0) - VALUE(T5.ABROAD, 0) AS LESSON, ");
        stb.append("     VALUE(T5.LESSON, 0) - VALUE(T5.OFFDAYS, 0) - VALUE(T5.ABROAD, 0)  - VALUE(T5.ABSENT, 0)  - VALUE(T5.MOURNING, 0) AS LESSON, ");
        stb.append("     VALUE(T5.SICK, 0) + VALUE(T5.NOTICE, 0) + VALUE(T5.NONOTICE, 0) AS SICK");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE T1 ");
        stb.append(" LEFT JOIN ");
        stb.append("     SUBCLASS_T T2 ");
        stb.append("      ON T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     RECORD_T T3 ");
        stb.append("      ON T3.CHAIRCD = T1.CHAIRCD ");
        stb.append("     AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     RECORD_9 T4 ");
        stb.append("      ON T4.CHAIRCD = T1.CHAIRCD ");
        stb.append("     AND T4.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     ATTEND_T T5 ");
        stb.append("      ON T5.CHAIRCD = T1.CHAIRCD ");
        stb.append("     AND T5.SCHREGNO = T1.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");

        return stb.toString();
    }

    private class Student {
        final String _schregno;
        final String _name;
        final String _sex;
        final String _grade;
        final String _grade_Cd;
        final String _hrNameAbbv;
        final String _attendno;
        final String _grdName;
        final String _grdDate;
        final String _transferName;
        final String _transferSdate;
        final String _transferEdate;
        final String _credits;
        final String _hyoutei;
        final String _hyoutei5;
        final String _get_Credit;
        final String _lesson;
        final String _sick;

        public Student(final String schregno, final String name, final String sex, final String grade,
                final String grade_Cd, final String hrNameAbbv, final String attendno, final String grdName,
                final String grdDate, final String transferName, final String transferSdate, final String transferEdate,
                final String credits, final String hyoutei, final String hyoutei5, final String get_Credit,
                final String lesson, final String sick) {
            _schregno = schregno;
            _name = name;
            _sex = sex;
            _grade = grade;
            _grade_Cd = grade_Cd;
            _hrNameAbbv = hrNameAbbv;
            _attendno = attendno;
            _grdName = grdName;
            _grdDate = grdDate;
            _transferName = transferName;
            _transferSdate = transferSdate;
            _transferEdate = transferEdate;
            _credits = credits;
            _hyoutei = hyoutei;
            _hyoutei5 = hyoutei5;
            _get_Credit = get_Credit;
            _lesson = lesson;
            _sick = sick;

        }
    }

    private Chair getChair(final DB2UDB db2, final String chairCd) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = setChairSql(chairCd);
        log.debug(" chair sql = " + sql);
        Chair chair = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                if (chair == null) {
                    chair = new Chair(rs.getString("CHAIRNAME"), rs.getString("SUBCLASSNAME"));
                }
                chair.staffMap.put(rs.getString("STAFFCD"), rs.getString("STAFFNAME"));
            }
            DbUtils.closeQuietly(rs);
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }

        return chair;
    }

    private String setChairSql(final String chairCd) {

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRNAME, ");
        stb.append("     T2.STAFFCD, ");
        stb.append("     T3.STAFFNAME, ");
        stb.append("     T4.SUBCLASSNAME ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT T1 ");
        stb.append(" LEFT JOIN ");
        stb.append("     CHAIR_STF_DAT T2 ");
        stb.append("      ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("     AND T2.CHARGEDIV = '1' ");
        stb.append(" LEFT JOIN ");
        stb.append("     STAFF_MST T3 ");
        stb.append("      ON T3.STAFFCD = T2.STAFFCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     SUBCLASS_MST T4 ");
        stb.append("      ON T4.CLASSCD = T1.CLASSCD ");
        stb.append("     AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("     AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("     AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' AND ");
        stb.append("     T1.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("     T1.CHAIRCD = '" + chairCd + "' ");

        return stb.toString();
    }

    //講座
    private class Chair {
        final String _chairName;
        final String _subclassname;
        final Map<String, Student> studentMap = new LinkedHashMap();
        final Map<String, String> staffMap = new LinkedHashMap();

        public Chair(final String chairName, final String subclassName) {
            _chairName = chairName;
            _subclassname = subclassName;
        }
    }

    private String getStaffImageFilePath(final String staffCd) {
        final String stampNo = (String) _param._stampMap.get(staffCd);
        final String path = _param._documentRoot + "/image/stamp/" + stampNo + ".bmp";
        final boolean exists = new java.io.File(path).exists();
        log.warn(" path " + path + " exists: " + exists);
        if (exists) {
            return path;
        }
        return null;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String[] _categorySelected;
        final String _semester;
        final String _year;
        final String _date;
        final String _time;
        final String _nendo;
        final String _documentRoot;
        final String _semeName;
        final String _semeSdate;
        final String _semeEdate;

        final Map _stampMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException, UnsupportedEncodingException {
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _semester = request.getParameter("SEMESTER");
            _year = request.getParameter("CTRL_YEAR");
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度";

            final Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            _date = KNJ_EditDate.getAutoFormatDate(db2, sdf.format(date));

            sdf = new SimpleDateFormat("hh時mm分");
            _time = sdf.format(date);

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT * FROM SEMESTER_MST WHERE YEAR='" + _year+ "' AND SEMESTER = '" + _semester + "' "));
            _semeName = KnjDbUtils.getString(row, "SEMESTERNAME");
            _semeSdate = KnjDbUtils.getString(row, "SDATE");
            _semeEdate = KnjDbUtils.getString(row, "EDATE");

            _stampMap = getStampNoMap(db2);

        }

        private Map getStampNoMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     STAFFCD, ");
            stb.append("     MAX(STAMP_NO) AS STAMP_NO ");
            stb.append(" FROM ");
            stb.append("     ATTEST_INKAN_DAT ");
            stb.append(" GROUP BY ");
            stb.append("     STAFFCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map retMap = new HashMap();
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String staffcd = rs.getString("STAFFCD");
                    final String stampNo = rs.getString("STAMP_NO");
                    retMap.put(staffcd, stampNo);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }
    }
}
// eof
