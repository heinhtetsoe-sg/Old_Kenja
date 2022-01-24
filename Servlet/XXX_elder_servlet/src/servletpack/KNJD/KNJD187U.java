/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 *
 * 作成日: 2021/02/05
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD187U {

    private static final Log log = LogFactory.getLog(KNJD187U.class);

    private static final String SEMEALL = "9";
    private static final String SEME1 = "前期";
    private static final String SEME2 = "後期";
    private static final String SEME9 = "学年";

    private static final String SELECT_CLASSCD_UNDER = "90";
    private static final String HR_SUBCLASSCD = "910100"; //ホームルーム活動　成績つかない科目


    private static final String SDIV010101 = "010101"; //中間1
    private static final String SDIV010201 = "010201"; //中間2(1、2年のみ)
    private static final String SDIV020101 = "020101"; //期末
    private static final String SDIV990008 = "990008"; //学年末

    private static final String CHUKAN1    = "1-010101"; //前期中間素点
    private static final String CHUKAN1_2  = "1-010201"; //前期中間素点2(1、2年のみ)
    private static final String KIMATSU1   = "1-020101"; //前期期末素点
    private static final String KIMATSU1_8 = "1-990008"; //前期期末評価
    private static final String KIMATSU1_9 = "1-990009"; //前期期末評定
    private static final String CHUKAN2    = "2-010101"; //後期中間素点
    private static final String KIMATSU2   = "2-020101"; //後期期末素点
    private static final String KIMATSU2_8 = "2-990008"; //後期期末評価
    private static final String KIMATSU2_9 = "2-990009"; //後期期末評定
    private static final String KIMATSU9_8 = "9-990008"; //学年末評価
    private static final String KIMATSU9_9 = "9-990009"; //学年末評定

    private static final String KOUSASOTEN   = "考査素点報告書";
    private static final String KOUSASEISEKI = "考査成績通知書";
    private static final String SEISEKI      = "成績通知書";

    private static final String ALL3  = "333333";
    private static final String ALL5  = "555555";
    private static final String ALL9  = "999999";
    private static final String ALL9A = "99999A";
    private static final String ALL9B = "99999B";

    private static final String TEST1 = "1回目";
    private static final String TEST2 = "2回目";
    private static final String TEST3 = "3回目";
    private static final String TEST4 = "4回目";
    private static final String TEST5 = "5回目";

    private static final String Pink = "Paint=(13,70,2)"; //ピンク色

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List studentList = getList(db2);

        if (studentList.isEmpty()) return;
        final Map scoreMap = getScore(db2, false);
        final Map totalMap = getScore(db2, true);

        for (DateRange range : _param._semesterList) {
            Attendance.load(db2, _param, studentList, range);
        }

        //高校3年のfrmはU_2を使う
        final String form = "03".equals(_param._gradeCd)  ? "KNJD187U_2.frm" : "KNJD187U_1.frm";

        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();
            svf.VrSetForm(form , 4);
            //明細部以外を印字
            printTitle(svf, student);
            printAttend(svf, student);
            printRemark(svf);

            printScore(svf, student, scoreMap, totalMap);

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printScore(final Vrw32alp svf, final Student student, final Map scoreMap, final Map totalMap) {
        final int maxLine = 23; //最大枠数

        //合計、平均、学級順位
        int gyo = 1;
        String seme = "";
        for (String testcd : _param._testCd) {
            if (!testcd.substring(0,1).equals(seme)) {
                seme = testcd.substring(0,1);
                gyo = 1;
            }
            final String totalKey = student._schregno + "-" + testcd + "-99-H-99-999999";
            final Score score = (Score)totalMap.get(totalKey);
            if (score != null) {
                svf.VrsOut("TOTAL_SCORE" + seme + "_" + gyo, score._score);
                svf.VrsOut("SCORE_AVERAGE" + seme + "_" + gyo, score._avg);
                svf.VrsOut("HR_CLASS_RANK" + seme + "_" + gyo, score._classRank);
            }
            gyo++;
        }

        int cnt = 1;
        //科目毎のループ
        for (Iterator ite = student._printSubclassMap.keySet().iterator(); ite.hasNext();) {
            final String subclassKey = (String)ite.next();
            final Map<String, String> printSubclassMap = (Map)student._printSubclassMap.get(subclassKey);
            final String subclassName = printSubclassMap.get("SUBCLASSABBV");

            if (subclassName != null) {
                final int len = subclassName.length();
                if(len > 12) {
                    svf.VrsOut("SUBCLASSNAME2_1", subclassName.substring(0,12));
                    svf.VrsOut("SUBCLASSNAME2_2", subclassName.substring(12,subclassName.length()));
                } else {
                    svf.VrsOut("SUBCLASSNAME", subclassName);
                }
            }

            svf.VrsOut("CREDIT", printSubclassMap.get("CREDITS"));

            gyo = 1;
            seme = "";
            for (String testcd : _param._testCd) {
                final String testKey = student._schregno + "-" + testcd + "-" + subclassKey;
                final Score score = (Score)scoreMap.get(testKey);

                if (!testcd.substring(0,1).equals(seme)) {
                    seme = testcd.substring(0,1);
                    gyo = 1;
                }

                if (score != null) {
                    if("*".equals(score._valueDi)) {
                        svf.VrsOut("SCORE" + seme + "_" + gyo, score._valueDi);
                    } else {
                        svf.VrsOut("SCORE" + seme + "_" + gyo, score._score);
                    }
                    svf.VrsOut("AVERAGE" + seme + "_" + gyo, score._hrAvg);
                }
                gyo++;
            }
            printAvgColor(svf);

            //ホームルーム活動は"修"を印字。 複数取得された場合を想定し、印字後BREAK
            final String subclassCd = subclassKey.substring(subclassKey.length() -6);
            if (HR_SUBCLASSCD.equals(subclassCd)) {
                svf.VrsOut("SCORE9_2", "修");
                svf.VrEndRecord();
                cnt++;
                break;
            }
            svf.VrEndRecord();
            cnt++;
        }

        //残りの空欄も色付けする
        for (int line = cnt; line <= maxLine; line++) {
            printAvgColor(svf);
            svf.VrEndRecord();
        }
    }

    //クラス平均はピンク色
    private void printAvgColor(final Vrw32alp svf) {
        svf.VrAttribute("AVERAGE1_1", Pink);
        svf.VrAttribute("AVERAGE1_2", Pink);
        svf.VrAttribute("AVERAGE1_3", Pink);
        svf.VrAttribute("AVERAGE2_1", Pink);
        svf.VrAttribute("AVERAGE2_2", Pink);
    }

    private void printTitle(final Vrw32alp svf, final Student student) {
        svf.VrsOut("ZIPCD", "〒" + student._zipcd);

        final int addr1Keta = KNJ_EditEdit.getMS932ByteLength(student._addr1);
        final String addr1Field = addr1Keta <= 40 ? "1" : "1_2";
        svf.VrsOut("ADDR" + addr1Field, student._addr1);
        final int addr2Keta = KNJ_EditEdit.getMS932ByteLength(student._addr2);
        final String addr2Field = addr2Keta <= 40 ? "2" : "2_2";
        svf.VrsOut("ADDR" + addr2Field, student._addr2);

        final int nameKeta = KNJ_EditEdit.getMS932ByteLength(student._name + " 様");
        final String nameField = nameKeta <= 34 ? "" : "2";
        svf.VrsOut("ADDRESSEE" + nameField, student._name + " 様");

        svf.VrsOut("SCHOOLNAME", _param._schoolName);
        svf.VrsOut("STAFFNAME1", "校　長　　" + _param._principalName);
        svf.VrsOut("STAFFNAME2", "担　任　　" + student._staffname);
        svf.VrsOut("NENDO", _param._nendo + " " + _param._semeTitle + _param._testTitle);

        final String nen = Integer.valueOf(_param._gradeCd).toString();
        final String kumi = Integer.valueOf(student._hr_Class).toString();
        final String ban = Integer.valueOf(student._attendno).toString();

        final String schInfo = nen + "年 " + kumi + "組 " + ban + "番 " + student._name;
        final int siKeta = KNJ_EditEdit.getMS932ByteLength(schInfo);
        final String siField = siKeta <= 50 ? "1" : "2";

        svf.VrsOut("SCH_INFO" + siField, schInfo);
        svf.VrsOut("DATE", _param._wareki);

        svf.VrsOut("TESTNAME1_1", TEST1);
        svf.VrsOut("TESTNAME1_2", TEST2);
        if ("03".equals(_param._gradeCd)) {
            svf.VrsOut("TESTNAME1_3", SEME1);
            svf.VrsOut("TESTNAME2_1", TEST3);
            svf.VrsOut("TESTNAME2_2", TEST4);
            svf.VrsOut("TESTNAME2_3", SEME2);
        } else {
            svf.VrsOut("TESTNAME1_3", TEST3);
            svf.VrsOut("TESTNAME1_4", SEME1);
            svf.VrsOut("TESTNAME2_1", TEST4);
            svf.VrsOut("TESTNAME2_2", TEST5);
            svf.VrsOut("TESTNAME2_3", SEME2);
        }
        svf.VrsOut("TESTNAME9", SEME9);

        //色付け
        final String[] seme = new String[] {"1","2"};
        for (final String semeField : seme) {
            for (int cnt = 1; cnt <= 4; cnt++) {
                svf.VrAttribute("TESTNAME" + semeField + "_" + cnt, Pink);
            }
        }
        svf.VrAttribute("TESTNAME9", Pink);
        svf.VrAttribute("SAMPLE_COLOR", Pink);
        svf.VrsOut("RECEIPT", _param._semeTitle + _param._testTitle + "を確かに受け取りました。");
    }

    //出欠
    private void printAttend(final Vrw32alp svf, final Student student) {
        svf.VrsOutn("ATTEND_SEMESTER", 1,  SEME1);
        svf.VrsOutn("ATTEND_SEMESTER", 2,  SEME2);
        svf.VrsOutn("ATTEND_SEMESTER", 3,  SEME9);
        svf.VrAttributen("ATTEND_SEMESTER", 1, Pink);
        svf.VrAttributen("ATTEND_SEMESTER", 2, Pink);
        svf.VrAttributen("ATTEND_SEMESTER", 3, Pink);

        for (Iterator ite = student._attendMap.keySet().iterator(); ite.hasNext();) {
            final String key = (String)ite.next();

            if (Integer.valueOf(key) > Integer.valueOf(_param._semester)) {
                break;
            }

            final Attendance attend = (Attendance)student._attendMap.get(key);
            final int gyo;
            if ("1".equals(key)) {
                gyo = 1;
            } else if("2".equals(key)) {
                gyo = 2;
            } else {
                gyo = 3;
            }
            svf.VrsOutn("LESSON", gyo,  String.valueOf(attend._lesson));
            svf.VrsOutn("SUSPEND", gyo,  String.valueOf(attend._suspend + attend._mourning));
            final int subeki = attend._lesson - (attend._suspend + attend._mourning + attend._abroad);
            svf.VrsOutn("PRESENT", gyo,  String.valueOf(subeki));
            svf.VrsOutn("SICK", gyo,  String.valueOf(attend._absent));
            svf.VrsOutn("ATTEND", gyo,  String.valueOf(subeki - attend._absent));
            svf.VrsOutn("LATE", gyo,  String.valueOf(attend._late));
            svf.VrsOutn("EARLY", gyo,  String.valueOf(attend._early));

            final String remark = (String)student._attendRemarkMap.get(key);
            if (remark != null) {
                final int keta = KNJ_EditEdit.getMS932ByteLength(remark);
                final String field = keta <= 30 ? "1" : "2";
                svf.VrsOutn("ATTEND_REMARK" + field, gyo,  remark);
            }
        }
    }

    //追記事項
    private void printRemark(final Vrw32alp svf) {
        svf.VrsOut("COMMUNICATION_TITLE", "追　　記　　事　　項");
        svf.VrAttribute("COMMUNICATION_TITLE", Pink);

        if (_param._remark1.length() > 0) {
            final String[] remark1 = KNJ_EditEdit.get_token(_param._remark1, 46, 4);
            for (int i = 0; i < remark1.length; i++) {
                svf.VrsOutn("COMMUNICATION", i + 1, remark1[i]);
            }
        }
        if (_param._remark2.length() > 0) {
            final String[] remark2 = KNJ_EditEdit.get_token(_param._remark2, 46, 2);
            for (int i = 0; i < remark2.length; i++) {
                svf.VrsOutn("COMMUNICATION2", i + 1, remark2[i]);
            }
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getStudentSql();
        log.debug(" sql =" + sql);
        try {

            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                if ("1".equals(rs.getString("LEAVE"))){
                    continue; //異動フラグ生徒は対象外
                }
                final String schregno = rs.getString("SCHREGNO");
                final String semester = rs.getString("SEMESTER");
                final String hr_Class = rs.getString("HR_CLASS");
                final String name = rs.getString("NAME");
                final String hr_Name = rs.getString("HR_NAME");
                final String staffname = rs.getString("STAFFNAME");
                final String attendno = rs.getString("ATTENDNO");
                final String grade = rs.getString("GRADE");
                final String course = rs.getString("COURSE");
                final String hr_Class_Name1 = rs.getString("HR_CLASS_NAME1");
                final String zipcd = StringUtils.defaultString(rs.getString("ZIPCD"));
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                final Student student = new Student(schregno, semester, hr_Class, name, hr_Name, staffname, attendno, grade, course, hr_Class_Name1, zipcd, addr1, addr2);

                student.setSubclass(db2, student._course);

                retList.add(student);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        //           寮生
        stb.append(" WITH DOMITORY_DATA AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         SCHREGNO, ");
        stb.append("         '1' AS DOMITORY ");
        stb.append("     FROM ");
        stb.append("         SCHREG_DOMITORY_HIST_DAT ");
        stb.append("     WHERE ");
        stb.append("         '" + _param._date + "' BETWEEN DOMI_ENTDAY AND value(DOMI_OUTDAY, '9999-12-31') ");
        stb.append(" ) ");
        stb.append("     SELECT  W1.SCHREGNO");
        stb.append("            ,W1.SEMESTER ");
        stb.append("            ,W1.GRADE ");
        stb.append("            ,W1.HR_CLASS ");
        stb.append("            ,W1.ATTENDNO ");
        stb.append("            ,W7.NAME ");
        stb.append("            ,W8.STAFFNAME ");
        stb.append("            ,W1.COURSECD || W1.MAJORCD || W1.COURSECODE AS COURSE");
        stb.append("            ,W6.HR_NAME ");
        stb.append("            ,W6.HR_CLASS_NAME1 ");
        stb.append("            ,DOMI.DOMITORY ");
        stb.append("            ,CASE WHEN DOMI.DOMITORY = '1' THEN GADDR.GUARD_ZIPCD ELSE ADDR.ZIPCD END AS ZIPCD");
        stb.append("            ,CASE WHEN DOMI.DOMITORY = '1' THEN GADDR.GUARD_ADDR1 ELSE ADDR.ADDR1 END AS ADDR1");
        stb.append("            ,CASE WHEN DOMI.DOMITORY = '1' THEN GADDR.GUARD_ADDR2 ELSE ADDR.ADDR2 END AS ADDR2");
        stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
        stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
        stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
        stb.append("                  ELSE 0 END AS LEAVE ");
        stb.append("     FROM    SCHREG_REGD_DAT W1 ");
        stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = '" + _param._year + "' AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = '" + _param._gradeHrclass.substring(0,2) + "' ");
        //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
        stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
        stb.append("                  AND W3.GRD_DIV IN('2','3') ");
        stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + _param._date + "' THEN W2.EDATE ELSE '" + _param._date + "' END ");
        //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
        stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
        stb.append("                  AND W4.ENT_DIV IN('4','5') ");
        stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + _param._date + "' THEN W2.EDATE ELSE '" + _param._date + "' END ");
        //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
        stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
        stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
        stb.append("                  AND CASE WHEN W2.EDATE < '" + _param._date + "' THEN W2.EDATE ELSE '" + _param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT W6 ON W6.YEAR = W1.YEAR ");
        stb.append("                  AND W6.SEMESTER = W1.SEMESTER ");
        stb.append("                  AND W6.GRADE = W1.GRADE ");
        stb.append("                  AND W6.HR_CLASS = W1.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EGHIST ON EGHIST.SCHREGNO = W1.SCHREGNO AND EGHIST.SCHOOL_KIND = 'H' ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST W7 ON W7.SCHREGNO = W1.SCHREGNO ");
        stb.append("     LEFT JOIN STAFF_MST W8 ON W8.STAFFCD = W6.TR_CD1 ");
        stb.append("     LEFT JOIN DOMITORY_DATA DOMI ON DOMI.SCHREGNO = W1.SCHREGNO ");
        stb.append("     LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM SCHREG_ADDRESS_DAT GROUP BY SCHREGNO) L_ADDR ");
        stb.append("            ON L_ADDR.SCHREGNO = W1.SCHREGNO  ");
        stb.append("     LEFT JOIN SCHREG_ADDRESS_DAT ADDR ");
        stb.append("            ON ADDR.SCHREGNO  = L_ADDR.SCHREGNO ");
        stb.append("           AND ADDR.ISSUEDATE = L_ADDR.ISSUEDATE ");
        stb.append("     LEFT JOIN GUARDIAN_DAT GUARDIAN ");
        stb.append("            ON GUARDIAN.SCHREGNO = W1.SCHREGNO  ");
        stb.append("     LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_ADDRESS_DAT GROUP BY SCHREGNO) L_GADDR ");
        stb.append("            ON L_GADDR.SCHREGNO = GUARDIAN.SCHREGNO  ");
        stb.append("     LEFT JOIN GUARDIAN_ADDRESS_DAT GADDR ");
        stb.append("            ON GADDR.SCHREGNO  = L_GADDR.SCHREGNO ");
        stb.append("           AND GADDR.ISSUEDATE = L_GADDR.ISSUEDATE ");
        stb.append("     WHERE   W1.YEAR = '" + _param._year + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND W1.SEMESTER = '" + _param._ctrlSeme + "' ");
        } else {
            stb.append("     AND W1.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("         AND W1.GRADE || W1.HR_CLASS = '" + _param._gradeHrclass + "' ");
        stb.append("         AND W1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        stb.append("     ORDER BY ");
        stb.append("         W1.ATTENDNO ");

        return stb.toString();
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private class Student {
        final String _schregno;
        final String _semester;
        final String _hr_Class;
        final String _name;
        final String _hr_Name;
        final String _staffname;
        final String _attendno;
        final String _grade;
        final String _course;
        final String _hr_Class_Name1;
        final String _zipcd;
        final String _addr1;
        final String _addr2;
        final Map _attendMap = new TreeMap();
        final Map _printSubclassMap = new TreeMap();
        final Map _scoreMap = new TreeMap();
        final Map _attendRemarkMap = new TreeMap();

        public Student(final String schregno, final String semester, final String hr_Class, final String name,
                final String hr_Name, final String staffname, final String attendno, final String grade,
                final String course, final String hr_Class_Name1, final String zipcd, final String addr1, final String addr2) {
            _schregno = schregno;
            _semester = semester;
            _hr_Class = hr_Class;
            _name = name;
            _hr_Name = hr_Name;
            _staffname = staffname;
            _attendno = attendno;
            _grade = grade;
            _course = course;
            _hr_Class_Name1 = hr_Class_Name1;
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
        }

        private String setSubclassSql(final String course) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     STD.YEAR, ");
            stb.append("     STD.CHAIRCD, ");
            stb.append("     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS SUBCLASSKEY, ");
            stb.append("     SUBCLASS.SUBCLASSABBV, ");
            stb.append("     CREDIT.CREDITS ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT STD ");
            stb.append(" INNER JOIN ");
            stb.append("     CHAIR_DAT CHAIR ");
            stb.append("      ON CHAIR.YEAR = STD.YEAR ");
            stb.append("     AND CHAIR.SEMESTER = STD.SEMESTER ");
            stb.append("     AND CHAIR.CHAIRCD = STD.CHAIRCD ");
            stb.append("     AND (CHAIR.CLASSCD < '" + SELECT_CLASSCD_UNDER + "' OR CHAIR.SUBCLASSCD = '" + HR_SUBCLASSCD + "')  ");
            stb.append(" INNER JOIN ");
            stb.append("     SUBCLASS_MST SUBCLASS ");
            stb.append("      ON SUBCLASS.CLASSCD = CHAIR.CLASSCD ");
            stb.append("     AND SUBCLASS.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            stb.append("     AND SUBCLASS.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            stb.append("     AND SUBCLASS.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            stb.append(" LEFT JOIN ");
            stb.append("     CREDIT_MST CREDIT ");
            stb.append("      ON CREDIT.YEAR = STD.YEAR ");
            stb.append("     AND CREDIT.COURSECD || CREDIT.MAJORCD || CREDIT.COURSECODE = '" + course + "' ");
            stb.append("     AND CREDIT.GRADE = '" + _param._grade + "' ");
            stb.append("     AND CREDIT.CLASSCD = SUBCLASS.CLASSCD ");
            stb.append("     AND CREDIT.SCHOOL_KIND = SUBCLASS.SCHOOL_KIND ");
            stb.append("     AND CREDIT.CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ");
            stb.append("     AND CREDIT.SUBCLASSCD = SUBCLASS.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     STD.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND STD.YEAR = '" + _param._year + "' ");
            stb.append(" ORDER BY ");
            stb.append("     STD.YEAR, SUBCLASSKEY ");

            return stb.toString();
        }

        private void setSubclass(final DB2UDB db2, final String course) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = setSubclassSql(course);
            log.debug(" subclass sql = " + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Map subclass;
                    if (_printSubclassMap.containsKey(rs.getString("SUBCLASSKEY"))) {
                        continue;
                    } else {
                        _printSubclassMap.put(rs.getString("SUBCLASSKEY"), new TreeMap());
                    }
                    subclass = (Map)_printSubclassMap.get(rs.getString("SUBCLASSKEY"));

                    subclass.put("SUBCLASSKEY", rs.getString("SUBCLASSKEY"));
                    subclass.put("SUBCLASSABBV", rs.getString("SUBCLASSABBV"));
                    subclass.put("CREDITS", rs.getString("CREDITS"));
                }
                DbUtils.closeQuietly(rs);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }

    private Map getScore(final DB2UDB db2, final boolean total) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Map retMap = new LinkedMap();

        try {
            final String sql = setScoreSql(total);
            log.debug("scoreSql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String semester = rs.getString("SEMESTER");
                final String testkindcd = rs.getString("TESTKINDCD");
                final String testitemcd = rs.getString("TESTITEMCD");
                final String score_Div = rs.getString("SCORE_DIV");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String score = rs.getString("SCORE");
                final String avg = rs.getString("AVG");
                final String classRank = rs.getString("CLASS_RANK");
                final String hrAvg = rs.getString("HR_AVG");
                final String valueDi = rs.getString("VALUE_DI");

                final String key = schregno + "-" + semester + "-" + testkindcd + testitemcd + score_Div + "-" + subclasscd;

                if (!retMap.containsKey(key)) {
                    final Score wk = new Score(score, avg, classRank, hrAvg, valueDi);
                    retMap.put(key, wk);
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
        return retMap;
    }

    private String setScoreSql(final boolean total) {
        final String seme = SEMEALL.equals(_param._semester) ? _param._ctrlSeme : _param._semester;
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH  SCHNO_A AS(SELECT ");
        stb.append("     W1.SCHREGNO, ");
        stb.append("     W1.YEAR, ");
        stb.append("     W1.SEMESTER, ");
        stb.append("     W1.GRADE, ");
        stb.append("     W1.HR_CLASS, ");
        stb.append("     W1.COURSECD, ");
        stb.append("     W1.MAJORCD, ");
        stb.append("     W1.COURSECODE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT W1 ");
        stb.append(" WHERE ");
        stb.append("     W1.YEAR = '" + _param._year + "' AND ");
        stb.append("     W1.SEMESTER = '" + seme + "' AND ");
        stb.append("     W1.GRADE || W1.HR_CLASS = '"+ _param._gradeHrclass + "' AND ");
        stb.append("     W1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        stb.append(" )  ");
        stb.append(" SELECT ");
        if (total) { //合計、平均、順位
            stb.append("     W3.SCHREGNO, ");
            stb.append("     W3.SEMESTER, ");
            stb.append("     W3.TESTKINDCD, ");
            stb.append("     W3.TESTITEMCD, ");
            stb.append("     W3.SCORE_DIV, ");
            stb.append("     W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' ||      W3.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     W3.SCORE, ");
            stb.append("     DECIMAL(ROUND(FLOAT(W3.AVG)*10,0)/10,5,1) AS AVG, ");
            stb.append("     W3.CLASS_RANK, ");
            stb.append("     '' AS HR_AVG, ");
            stb.append("     '' AS VALUE_DI ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT W3      ");
            stb.append(" INNER JOIN  ");
            stb.append("     SCHNO_A W1  ");
            stb.append("      ON W1.YEAR = W3.YEAR AND W1.SCHREGNO = W3.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     W3.SEMESTER || '-' || W3.TESTKINDCD || W3.TESTITEMCD ||  W3.SCORE_DIV IN " + SQLUtils.whereIn(true, _param._testCd) + " AND ");
            stb.append("     W3.SUBCLASSCD = '" + ALL9 + "' ");
        } else { //素点、成績、評定、クラス平均
            stb.append("     W3.SCHREGNO, ");
            stb.append("     W3.SEMESTER, ");
            stb.append("     W3.TESTKINDCD, ");
            stb.append("     W3.TESTITEMCD, ");
            stb.append("     W3.SCORE_DIV, ");
            stb.append("     W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' ||      W3.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     S1.SCORE, ");
            stb.append("     '' AS AVG, ");
            stb.append("     '' AS CLASS_RANK, ");
            stb.append("     DECIMAL(ROUND(FLOAT(T_AVG2.AVG)*10,0)/10,5,1) AS HR_AVG, ");
            stb.append("     W3.VALUE_DI ");
            stb.append(" FROM ");
            stb.append("     RECORD_SCORE_DAT W3      ");
            stb.append(" INNER JOIN  ");
            stb.append("     SCHNO_A W1  ");
            stb.append("      ON W1.YEAR = W3.YEAR AND W1.SCHREGNO = W3.SCHREGNO ");
            stb.append(" LEFT JOIN ");
            stb.append("     RECORD_AVERAGE_SDIV_DAT T_AVG2 ");
            stb.append("      ON T_AVG2.YEAR = W3.YEAR ");
            stb.append("     AND T_AVG2.SEMESTER = W3.SEMESTER ");
            stb.append("     AND T_AVG2.TESTKINDCD = W3.TESTKINDCD ");
            stb.append("     AND T_AVG2.TESTITEMCD = W3.TESTITEMCD ");
            stb.append("     AND T_AVG2.SCORE_DIV = W3.SCORE_DIV ");
            stb.append("     AND T_AVG2.GRADE = '" + _param._grade + "' ");
            stb.append("     AND T_AVG2.CLASSCD = W3.CLASSCD ");
            stb.append("     AND T_AVG2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("     AND T_AVG2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("     AND T_AVG2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("     AND T_AVG2.AVG_DIV = '2' "); //クラス平均
            stb.append("     AND T_AVG2.HR_CLASS = W1.HR_CLASS ");
            stb.append(" LEFT JOIN ");
            stb.append("     RECORD_RANK_SDIV_DAT S1 ");
            stb.append("      ON S1.YEAR = W3.YEAR ");
            stb.append("     AND S1.SEMESTER = W3.SEMESTER ");
            stb.append("     AND S1.TESTKINDCD = W3.TESTKINDCD ");
            stb.append("     AND S1.TESTITEMCD = W3.TESTITEMCD ");
            stb.append("     AND S1.SCORE_DIV = W3.SCORE_DIV ");
            stb.append("     AND S1.CLASSCD = W3.CLASSCD ");
            stb.append("     AND S1.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("     AND S1.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("     AND S1.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("     AND S1.SCHREGNO = W3.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     W3.SEMESTER || '-' || W3.TESTKINDCD || W3.TESTITEMCD ||  W3.SCORE_DIV IN " + SQLUtils.whereIn(true, _param._testCd) + " AND ");
            stb.append("     W3.SUBCLASSCD NOT IN ('" + ALL3 + "','" + ALL5 + "','"+ ALL9A + "','"  + ALL9B + "') ");
        }
        stb.append(" ORDER BY ");
        stb.append("     W3.SCHREGNO, W3.SEMESTER, W3.TESTKINDCD, W3.TESTITEMCD, W3.SCORE_DIV, W3.SUBCLASSCD ");

        return stb.toString();
    }

    private class Score {
        final String _score;
        final String _avg;
        final String _classRank;
        final String _hrAvg;
        final String _valueDi;

        public Score(final String score, final String avg, final String classRank, final String hrAvg, final String valueDi) {
            _score = score;
            _avg = avg;
            _classRank = classRank;
            _hrAvg = hrAvg;
            _valueDi = valueDi;
        }
    }

    private static class Attendance {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _abroad;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int abroad,
                final int absent,
                final int present,
                final int late,
                final int early
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _abroad = abroad;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
        }

        Attendance add(final Attendance att) {
            return new Attendance(
                    _lesson + att._lesson,
                    _mLesson + att._mLesson,
                    _suspend + att._suspend,
                    _mourning + att._mourning,
                    _abroad + att._abroad,
                    _absent + att._absent,
                    _present + att._present,
                    _late + att._late,
                    _early + att._early
                    );
        }

        public String toString() {
            return "Att(les=" + _lesson + ",mles=" + _mLesson + ",susp" + _suspend + ",mourn=" + _mourning  + ",abroad=" + _abroad + ",absent=" + _absent + ",prese=" + _present + ",late=" + _late + ",early=" + _early + ")";
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List<Student> studentList,
                final DateRange dateRange
        ) {
            log.info(" attendance = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );
                log.debug(" attend sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Student student : studentList) {

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {

                        final Attendance attendance = new Attendance(
                                rs.getInt("LESSON"),
                                rs.getInt("MLESSON"),
                                rs.getInt("SUSPEND"),
                                rs.getInt("MOURNING"),
                                rs.getInt("TRANSFER_DATE"),
                                rs.getInt("SICK"),
                                rs.getInt("PRESENT"),
                                rs.getInt("LATE"),
                                rs.getInt("EARLY")
                        );
                        student._attendMap.put(dateRange._key, attendance);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            final Map hasuuMap = AttendAccumulate.getHasuuMap(db2, param._year, dateRange._sdate, edate);
            loadRemark(db2, param, (String) hasuuMap.get("attendSemesInState"), studentList, dateRange);
        }

        private static void loadRemark(final DB2UDB db2, final Param param, final String attendSemesInState, final List<Student> studentList, final DateRange dateRange) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT T1.MONTH, T1.SEMESTER, T1.SCHREGNO, T1.REMARK1 ");
                stb.append(" FROM ATTEND_SEMES_REMARK_DAT T1 ");
                stb.append(" INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append("   T1.COPYCD = '0' ");
                stb.append("   AND T1.YEAR = '" + param._year + "' ");
                stb.append("   AND T1.SEMESTER || T1.MONTH IN " + attendSemesInState + " ");
                stb.append("   AND T1.SCHREGNO = ? ");
                stb.append("   AND T1.REMARK1 IS NOT NULL ");
                stb.append(" ORDER BY T1.SEMESTER, INT(T1.MONTH) + CASE WHEN INT(T1.MONTH) < 4 THEN 12 ELSE 0 END ");

                ps = db2.prepareStatement(stb.toString());

                for (final Student student : studentList) {

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    final List<String> remark = new ArrayList<String>();
                    while (rs.next()) {
                        if (null != rs.getString("REMARK1")) {
                            remark.add(rs.getString("REMARK1"));
                        }
                    }
                    if (remark.size() != 0) {
                        student._attendRemarkMap.put(dateRange._key, mkString(remark, "、"));
                    }

                    DbUtils.closeQuietly(rs);
                }

            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }

    private static class DateRange {
        final String _key;
        final String _semester;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String semester, final String name, final String sdate, final String edate) {
            _key = key;
            _semester = semester;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public boolean isNull() {
            return null == _sdate || null == _edate;
        }
        public boolean equals(final Object o) {
            if (null == o) {
                return false;
            }
            if (!(o instanceof DateRange)) {
                return false;
            }
            final DateRange dr = (DateRange) o;
            return _key.equals(dr._key) && StringUtils.defaultString(_name).equals(StringUtils.defaultString(dr._name)) && rangeEquals(dr);
        }
        public boolean rangeEquals(final DateRange dr) {
            return StringUtils.defaultString(_sdate).equals(StringUtils.defaultString(dr._sdate)) && StringUtils.defaultString(_edate).equals(StringUtils.defaultString(dr._edate));
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _name + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private static String mkString(final List<String> list, final String comma) {
        final StringBuffer stb = new StringBuffer();
        if (null == list) {
            return stb.toString();
        }
        String comma0 = "";
        for (final String s : list) {
            if (null == s || s.length() == 0) {
                continue;
            }
            stb.append(comma0).append(s);
            comma0 = comma;
        }
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Id$");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String[] _categorySelected;
        final String _gradeHrclass;
        final String _schoolCd;
        final String _semester;
        final String _ctrlSeme;
        final String _prgid;
        final String[] _testCd;
        final String _year;
        final String _grade;
        final String _date;
        final String _outputDate;
        final String _remark1;
        final String _remark2;
        final String _gradeCd;
        final String _documentRoot;
        final String _schoolName;
        final String _principalName;
        final String _nendo;
        final String _wareki;
        final List<DateRange> _semesterList;
        final String _semeTitle;
        final String _testTitle;

        private boolean _isOutputDebug;
        private final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException, UnsupportedEncodingException {
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrclass.substring(0,2);
            _schoolCd = request.getParameter("SCHOOLCD");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _prgid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _date = request.getParameter("DATE").replace('/', '-');
            _outputDate = request.getParameter("OUTPUT_DATE").replace('/', '-');

            _remark1 = new String(StringUtils.defaultString(request.getParameter("REMARK1")).getBytes("ISO8859-1"));
            _remark2 = new String(StringUtils.defaultString(request.getParameter("REMARK2")).getBytes("ISO8859-1"));
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _semesterList = getSemesterList(db2);
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            _wareki = KNJ_EditDate.h_format_JP(db2, _outputDate);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("schregno", "?");
            _attendParamMap.put("useCurriculumcd", "1");
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("absenceDiv", "1");

            _gradeCd = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND SCHOOL_KIND = 'H' AND GRADE = '" + _grade + "'"));
            _schoolName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' AND SCHOOLCD = '" + _schoolCd + "' AND SCHOOL_KIND = 'H' "));
            _principalName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT PRINCIPAL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '104' "));

            //対象考査
            final String testCd = request.getParameter("TESTCD");
            if(_semester.equals("1")) {
                if (testCd.equals(SDIV010101)) {
                    _testCd = new String[] {CHUKAN1};
                    _testTitle = KOUSASOTEN;
                    _semeTitle = TEST1;
                } else if (testCd.equals(SDIV010201)) {
                    _testCd = new String[] {CHUKAN1,CHUKAN1_2};
                    _testTitle = KOUSASOTEN;
                    _semeTitle = TEST2;
                } else {
                    if (!_gradeCd.equals("03")) {
                        _testCd = new String[] {CHUKAN1, CHUKAN1_2, KIMATSU1, KIMATSU1_8};
                        _semeTitle = TEST3;
                    } else {
                        _testCd = new String[] {CHUKAN1, KIMATSU1, KIMATSU1_8, KIMATSU1_9};
                        _semeTitle = TEST2;
                    }
                    _testTitle = KOUSASEISEKI;
                }
            } else if (_semester.equals("2")) {
                if (testCd.equals(SDIV010101)) {
                    if (!_gradeCd.equals("03")) {
                        _testCd = new String[] {CHUKAN1, CHUKAN1_2, KIMATSU1, KIMATSU1_8, CHUKAN2};
                        _semeTitle = TEST4;
                    } else {
                        _testCd = new String[] {CHUKAN1, KIMATSU1, KIMATSU1_8, KIMATSU1_9, CHUKAN2};
                        _semeTitle = TEST3;
                    }
                    _testTitle = KOUSASOTEN;
                } else {
                    if (!_gradeCd.equals("03")) {
                        _testCd = new String[] {CHUKAN1, CHUKAN1_2, KIMATSU1, KIMATSU1_8, CHUKAN2, KIMATSU2, KIMATSU2_8};
                        _semeTitle = TEST5;
                    } else {
                        _testCd = new String[] {CHUKAN1, KIMATSU1, KIMATSU1_8, KIMATSU1_9, CHUKAN2, KIMATSU2, KIMATSU2_8, KIMATSU2_9};
                        _semeTitle = TEST4;
                    }
                    _testTitle = KOUSASEISEKI;
                }
            } else {
                if (!_gradeCd.equals("03")) {
                    _testCd = new String[] {CHUKAN1, CHUKAN1_2, KIMATSU1, KIMATSU1_8, CHUKAN2, KIMATSU2, KIMATSU2_8, KIMATSU9_8, KIMATSU9_9};
                } else {
                    _testCd = new String[] {CHUKAN1, KIMATSU1, KIMATSU1_8, KIMATSU1_9, CHUKAN2, KIMATSU2, KIMATSU2_8, KIMATSU2_9, KIMATSU9_8, KIMATSU9_9};
                }
                _semeTitle = "学年末";
                _testTitle = SEISEKI;
            }
        }

        protected List<DateRange> getSemesterList(
                final DB2UDB db2
        ) {
            final List<DateRange> rtnList= new ArrayList<DateRange>();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT T1.SEMESTER, T1.SEMESTERNAME  "
                                 + "  ,T1.SDATE "
                                 + "  ,T1.EDATE "
                                 + " FROM SEMESTER_MST T1 "
                                 + " WHERE T1.YEAR = '" + _year + "' "
                                 + " ORDER BY T1.SEMESTER ";
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnList.add(new DateRange(rs.getString("SEMESTER"), rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnList;
        }
    }
}
// eof
