// kanji=漢字
/*
 * $Id: 22c85164dce9ca3f9d440b7168193f517914e1d6 $
 *
 * 作成日: 2009/07/22 17:54:00 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 22c85164dce9ca3f9d440b7168193f517914e1d6 $
 */
public class KNJD672F {

    private static final Log log = LogFactory.getLog("KNJD667F.class");

    private Param _param;

    private static final String SUBCLASS3 = "333333";
    private static final String SUBCLASS5 = "555555";
    private static final String SUBCLASS7 = "777777";
    private static final String SUBCLASS8 = "888888";
    private static final String SUBCLASS9 = "999999";

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        boolean hasData = false;

        try {
            // print svf設定
            sd.setSvfInit(request, response, svf);
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            //SVF出力
            hasData = printMain(db2, svf);

        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            if (null != _param) {
                for (final Iterator it = _param._psMap.values().iterator(); it.hasNext();) {
                    PreparedStatement ps = (PreparedStatement) it.next();
                    DbUtils.closeQuietly(ps);
                }
            }
            sd.closeSvf(svf, hasData);
            sd.closeDb(db2);
        }

    }// doGetの括り

    private static boolean isSubclassAll(final String subclassCd) {
        return SUBCLASS3.equals(subclassCd) || SUBCLASS5.equals(subclassCd) || SUBCLASS9.equals(subclassCd);
    }

    private static String kirisute(final BigDecimal bd, final int keta) {
        return null == bd ? null : bd.setScale(keta, BigDecimal.ROUND_DOWN).toString();
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    /**
     * @param db2
     */
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException, ParseException {
        boolean hasData = false;

//        _param._viewClassList = ViewClass.getViewClassList(db2, _param);

        final List studentList = Student.getStudentList(db2, _param);
        log.debug(" studentList size = " + studentList.size());

        for (int i = 1; i <= studentList.size(); i++) {
            final Student student = (Student) studentList.get(i - 1);

            log.debug(" schregno = " + student._schregno);

            printStudent(svf, student);

            hasData = true;
        }
        return hasData;
    }

    private static List getMappedList(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    public static String kakko(final String v) {
        if (StringUtils.isBlank(v)) {
            return "";
        }
        return "【" + v + "】";
    }
    public void printStudent(final Vrw32alp svf, final Student student) {
        final String form = "KNJD672F.frm";
        svf.VrSetForm(form, 1);

        svf.VrsOut("HR_NAME", student._hrName); // クラス名
        svf.VrsOut("NO", NumberUtils.isDigits(student._attendNo) ? String.valueOf(Integer.parseInt(student._attendNo)) : student._attendNo); // 出席番号
        svf.VrsOut("NAME", student._name); // 氏名
        svf.VrsOut("ADDRESS" + (getMS932ByteLength(student._addr) > 50 ? "2" : "1"), student._addr); // 住所
        svf.VrsOut("TANTOU", _param._tantouStaffname); // 担当
        svf.VrsOut("CHARGE", _param._staffname); // 担任

        svf.VrsOut("ITEM1", ""); // アイテム
        printAttendance(svf, student);

        svf.VrsOut("ITEM2", ""); // アイテム
        VrsOutnRenban(svf, "CLUB", KNJ_EditEdit.get_token(student._clubCommitteeSikakuText, 40, 5));

        svf.VrsOut("ITEM3", kakko("内申成績")); // アイテム
        printKantenHyotei(svf, "1", student);

        //svf.VrsOut("ITEM4", null); // アイテム
        printKantenHyotei(svf, "2", student);

        svf.VrsOut("ITEM5", kakko("模試結果")); // アイテム
        printProficiency(svf, student);

        svf.VrsOut("ITEM6", kakko(_param._d064001Name1)); // アイテム
        VrsOutnRenban(svf, "DISCUSS1", KNJ_EditEdit.get_token((String) student._courseInterviewDatRemarkMap.get("001"), 90, 2)); // 懇談

        svf.VrsOut("ITEM7", kakko(_param._questionaireNames[0])); // アイテム
        printCourseHope(svf, 1, student, _param._shingakuSelected[0]);

        svf.VrsOut("ITEM8", kakko(_param._d064002Name1)); // アイテム
        VrsOutnRenban(svf, "DISCUSS2", KNJ_EditEdit.get_token((String) student._courseInterviewDatRemarkMap.get("002"), 90, 2)); // 懇談

        svf.VrsOut("ITEM9", kakko(_param._questionaireNames[1])); // アイテム
        printCourseHope(svf, 2, student, _param._shingakuSelected[1]);

        svf.VrsOut("ITEM10", kakko(_param._d064003Name1)); // アイテム
        VrsOutnRenban(svf, "DISCUSS3", KNJ_EditEdit.get_token((String) student._courseInterviewDatRemarkMap.get("003"), 90, 2)); // 懇談

        svf.VrEndPage();
    }

    private void printAttendance(final Vrw32alp svf, final Student student) {
        svf.VrsOutn("GRADE", 1, "１年"); // 学年
        svf.VrsOutn("GRADE", 2, "２年"); // 学年
        svf.VrsOutn("GRADE", 3, "３年"); // 学年

        for (final Iterator it = student._dayAttendanceMap.entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = (Map.Entry) it.next();
            final String grade = (String) e.getKey();
            final DayAttendance att = (DayAttendance) e.getValue();
            if (!NumberUtils.isDigits(grade)) {
                continue;
            }
            final int line = Integer.parseInt(grade);
            svf.VrsOutn("ABSENCE", line, String.valueOf(att._sick)); // 欠席
            svf.VrsOutn("LATE", line, String.valueOf(att._late + att._early)); // 遅刻早退
            att.setKaikinSeikin(_param);
            if (att._isKaikin) {
                svf.VrsOutn("ATTEND_REMARK", line, "皆勤"); // 出欠備考
            } else if (att._isSeikin) {
                svf.VrsOutn("ATTEND_REMARK", line, "精勤"); // 出欠備考
            }
        }
    }

    private void printKantenHyotei(final Vrw32alp svf, final String semester, final Student student) {
        svf.VrsOut("SEMESTER" + semester, (String) _param._semesterNameMap.get(semester)); // 学期名

//        for (int i = 0; i < _param._viewClassList.size(); i++) {
//            final ViewClass viewClass = (ViewClass) _param._viewClassList.get(i);
//
//            final String ssubi = String.valueOf(i + 1);
//
//            //log.debug(" seme = " + semester + " viewClass " + viewClass._subclasscd + " " + viewClass._subclassname);
//            svf.VrsOut("CLASS_NAME" + semester + "_" + ssubi, viewClass._subclassname); // 教科名
//
//            for (int vi = 0; vi < viewClass.getViewSize(); vi++) {
//                final String viewcd = viewClass.getViewCd(vi);
//
//                final ViewRecord vr = ViewRecord.getViewRecord(semester, viewClass._subclasscd, viewcd, student._viewRecordList);
//                if (null != vr) {
//                    svf.VrsOut("VAL" + semester + "_" + ssubi + "_" + String.valueOf(vi + 1), vr._status); // 評価
//                }
//            }
//
//            final ViewValuation vv = ViewValuation.getViewValuation(semester, viewClass._subclasscd, student._viewValuationList);
//            if (null != vv) {
//                svf.VrsOut("DIV" + semester + "_" + ssubi, vv._value); // 評定
//            }
//        }


        int pi = 0;
        for (int i = 0; i < student._viewValuationList.size(); i++) {
            final ViewValuation vv = (ViewValuation) student._viewValuationList.get(i);

            if (!semester.equals(vv._semester)) {
                continue;
            }
            if (vv._subclasscd.endsWith(SUBCLASS3) || vv._subclasscd.endsWith(SUBCLASS5) || vv._subclasscd.endsWith(SUBCLASS9) || vv._subclasscd.endsWith(SUBCLASS7) || vv._subclasscd.endsWith(SUBCLASS8)) {
                continue;
            }
            final String ssubi = String.valueOf(pi + 1);
            svf.VrsOut("CLASS_NAME" + semester + "_" + ssubi, vv._subclassname); // 教科名
            if (null != vv) {
                svf.VrsOut("DIV" + semester + "_" + ssubi, vv._value); // 評定
            }
            pi += 1;
        }
        final String score3 = StringUtils.defaultString((String) getMappedMap(student._testScoreMap, semester + "990008").get(SUBCLASS3), "  ");
        final String score5 = StringUtils.defaultString((String) getMappedMap(student._testScoreMap, semester + "990008").get(SUBCLASS5), "  ");
        final String score9 = StringUtils.defaultString((String) getMappedMap(student._testScoreMap, semester + "990008").get(SUBCLASS9), "  ");
        final String score4 = NumberUtils.isDigits(score9) ? String.valueOf(Integer.parseInt(score9) - (NumberUtils.isDigits(score5) ? Integer.parseInt(score5) : 0)) : "  ";

        String kansan1s = "";
        String kansan2s = "";
        if (NumberUtils.isNumber(score5) || NumberUtils.isNumber(score4)) {
            final String vscore5 = NumberUtils.isNumber(score5) ? score5 : "0";
            final String vscore4 = NumberUtils.isNumber(score4) ? score4 : "0";
            final BigDecimal kansan1 = new BigDecimal(vscore5).add(new BigDecimal(vscore4).multiply(new BigDecimal(2))); // kansan1 = score5 + score4 x 2 (5満点 x 5 + 5満点 x 4 x 2 = 65満点)
            final BigDecimal kansan2 = kansan1.multiply(new BigDecimal(300)).divide(new BigDecimal(65), 0, BigDecimal.ROUND_DOWN); // kansan1 x 300 / 65 切捨て
            kansan1s = kansan1.toString() + "点";
            kansan2s = kansan2.toString() + "点";
        } else {
            kansan1s = "  　";
            kansan2s = "   　";
        }
        //svf.VrsOut("RECORD_INFO" + semester, "３科 " + score3 + "　５科 " + score5 + "　４科 " + score4 + "　９科 " + score9 + "　換算 " + kansan1s + "　調査書点 " + kansan2s + ""); // 成績情報
        svf.VrsOut("RECORD_INFO" + semester, "３科 " + score3 + "　５科 " + score5 + "　９科 " + score9 + "　換算 " + kansan1s + "　調査書点 " + kansan2s + ""); // 成績情報
    }

    private void printProficiency(final Vrw32alp svf, final Student student) {
        final Map profMap = new HashMap();
        for (final Iterator it = student._proficiencyRankList.iterator(); it.hasNext();) {
            final ProficiencyRank pr = (ProficiencyRank) it.next();
            String key = null;
            if ("3".equals(pr._abbv1)) {
                key = "SUNDAI";
            } else if ("1".equals(pr._abbv1)) {
                key = "KOUNAI";
            } else if ("2".equals(pr._abbv1)) {
                key = "KAIJOU";
            }
            if (null == key) {
                continue;
            }
            getMappedList(getMappedMap(getMappedMap(profMap, key), pr._year), pr._mockcd).add(pr);
        }

        if (!getMappedMap(profMap, "SUNDAI").isEmpty()) {
            printProficiencyKind(svf, getMappedMap(profMap, "SUNDAI"), "MOCK1", "", "MOCK2", "", "EXAM_NAME3_");
        }
        if (!getMappedMap(profMap, "KOUNAI").isEmpty()) {
            printProficiencyKind(svf, getMappedMap(profMap, "KOUNAI"), "DEVIATION1_1", "TOTAL1_1", "DEVIATION1_2", "TOTAL1_2", "EXAM_NAME1_");
        }
        if (!getMappedMap(profMap, "KAIJOU").isEmpty()) {
            printProficiencyKind(svf, getMappedMap(profMap, "KAIJOU"), "DEVIATION2_1", "TOTAL2_1", "DEVIATION2_2", "TOTAL2_2", "EXAM_NAME2_");
        }
    }

    private void printProficiencyKind(final Vrw32alp svf, final Map profMap, final String fieldDev1, final String fieldScore1, final String fieldDev2, final String fieldScore2, final String fieldTestname) {
        int line = 1;
        for (final Iterator it = profMap.entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = (Map.Entry) it.next();
            final Map testMap = (Map) e.getValue();

            for (final Iterator tit = testMap.entrySet().iterator(); tit.hasNext();) {
                final Map.Entry te = (Map.Entry) tit.next();

                final List rankList = (List) te.getValue();

                //log.info(" " + te.getKey() + " / " + ((ProficiencyRank) rankList.get(0))._proficiencyname2);

                for (final Iterator rit = rankList.iterator(); rit.hasNext();) {
                    final ProficiencyRank pr = (ProficiencyRank) rit.next();
                    svf.VrsOutn(fieldTestname + (getMS932ByteLength(pr._mockname2) > 6 ? "2" : "1"), line, pr._mockname2);

                    svf.VrsOutn(fieldDev1, line, kirisute(pr._deviation333333, 0)); // 偏差値 3科
                    svf.VrsOutn(fieldScore1, line, pr._score333333); // 合計点 3科
                    svf.VrsOutn(fieldDev2, line, kirisute(pr._deviation555555, 0)); // 偏差値 5科
                    svf.VrsOutn(fieldScore2, line, pr._score555555); // 合計点 5科
                }
                line++;
            }
        }
    }

    private void printCourseHope(final Vrw32alp svf, final int n, final Student student, final String questionnairecd) {
        if (null == questionnairecd) {
            return;
        }
        final String sn = String.valueOf(n);
        for (int j = 0; j < student._courseHopeList.size(); j++) {
            final CourseHope ch = (CourseHope) student._courseHopeList.get(j);
            if (!questionnairecd.equals(ch._questionnairecd)) {
                continue;
            }
            final int line = Integer.parseInt(ch._hopeNum);
            svf.VrsOutn("HOPE_SCHOOL" + sn, line, ch._schoolName); // 志望高校
            svf.VrsOutn("HOPE_COURSE" + sn, line, StringUtils.defaultString(ch._facultyname) + StringUtils.defaultString(ch._departmentname)); // 志望コース
            svf.VrsOutn("REFERENCE" + sn, line, ch._baseScore); // 基準点
            svf.VrsOutn("NECESSARY" + sn, line, ch._necessaryScore); // 必要点
        }
    }

    private static void VrsOutnRenban(final Vrw32alp svf, final String field, final String[] list) {
        if (null != list) {
            for (int i = 0 ; i < list.length; i++) {
                svf.VrsOutn(field, i + 1, (String) list[i]);
            }
        }
    }

    private static class Student {

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
        final String _addr;
        final Map _testScoreMap;
        List _viewRecordList = new ArrayList(); // 観点
        List _viewValuationList = new ArrayList(); // 評定
        List _proficiencyRankList = Collections.EMPTY_LIST;
        List _courseHopeList = Collections.EMPTY_LIST;
        Map _dayAttendanceMap = new HashMap();
        Map _courseInterviewDatRemarkMap = Collections.EMPTY_MAP;
        String _clubCommitteeSikakuText = null;

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
                final String addr
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
            _addr = addr;
            _testScoreMap = new HashMap();
        }

        private static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                // log.info("sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = new Student(rs.getString("SCHREGNO"),
                                                  rs.getString("GRADE"),
                                                  rs.getString("HR_CLASS"),
                                                  rs.getString("ATTENDNO"),
                                                  rs.getString("HR_NAME"),
                                                  rs.getString("HR_NAMEABBV"),
                                                  "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME"),
                                                  rs.getString("COURSECD"),
                                                  rs.getString("COURSENAME"),
                                                  rs.getString("MAJORCD"),
                                                  rs.getString("MAJORNAME"),
                                                  rs.getString("COURSECODE"),
                                                  rs.getString("COURSECODENAME"),
                                                  StringUtils.defaultString(rs.getString("ADDR1")) + StringUtils.defaultString(rs.getString("ADDR2"))
                                                  );
                    studentList.add(student);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            Student.setClubCommitteeSikakuList(db2, param, studentList);
            TestScore.setScoreList(db2, param, studentList);
            ProficiencyRank.setProficiencyRankList(db2, param, studentList);
            ViewRecord.setViewRecordList(db2, param, studentList);
            ViewValuation.setViewValuationList(db2, param, studentList);
            DayAttendance.setAttendData(db2, param, studentList);
            CourseHope.setCourseHopeList(db2, param, studentList);
            Student.setCourseInterviewDat(db2, param, studentList);
            return studentList;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     VSCH.SCHREGNO, ");
            stb.append("     VSCH.GRADE, ");
            stb.append("     VSCH.HR_CLASS, ");
            stb.append("     VSCH.ATTENDNO, ");
            stb.append("     REGDH.HR_NAME, ");
            stb.append("     REGDH.HR_NAMEABBV, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.REAL_NAME, ");
            stb.append("     CASE WHEN L4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append("     VSCH.COURSECD, ");
            stb.append("     L1.COURSENAME, ");
            stb.append("     VSCH.MAJORCD, ");
            stb.append("     L1.MAJORNAME, ");
            stb.append("     VSCH.COURSECODE, ");
            stb.append("     L2.COURSECODENAME, ");
            stb.append("     TADDR.ADDR1, ");
            stb.append("     TADDR.ADDR2 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_FI_DAT VSCH ");
            stb.append("     INNER JOIN SCHREG_REGD_FI_HDAT REGDH ON REGDH.YEAR = VSCH.YEAR ");
            stb.append("          AND VSCH.SEMESTER = REGDH.SEMESTER ");
            stb.append("          AND VSCH.GRADE = REGDH.GRADE ");
            stb.append("          AND VSCH.HR_CLASS = REGDH.HR_CLASS ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = VSCH.SCHREGNO ");
            stb.append("     LEFT JOIN V_COURSE_MAJOR_MST L1 ON VSCH.YEAR = L1.YEAR ");
            stb.append("          AND VSCH.COURSECD = L1.COURSECD ");
            stb.append("          AND VSCH.MAJORCD = L1.MAJORCD ");
            stb.append("     LEFT JOIN V_COURSECODE_MST L2 ON VSCH.YEAR = L2.YEAR ");
            stb.append("          AND VSCH.COURSECODE = L2.COURSECODE ");
            stb.append("     LEFT JOIN GUARDIAN_DAT L3 ON VSCH.SCHREGNO = L3.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_NAME_SETUP_DAT L4 ON L4.SCHREGNO = VSCH.SCHREGNO AND L4.DIV = '04' ");
            stb.append("     LEFT JOIN (SELECT T1.SCHREGNO, ADDR1, ADDR2 ");
            stb.append("                FROM SCHREG_ADDRESS_DAT T1 ");
            stb.append("                INNER JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM SCHREG_ADDRESS_DAT WHERE YEAR(ISSUEDATE) <= " + param._year + "");
            stb.append("                            GROUP BY SCHREGNO) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("                    AND T2.ISSUEDATE = T1.ISSUEDATE ");
            stb.append("               ) TADDR ON TADDR.SCHREGNO = VSCH.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     VSCH.YEAR = '" + param._year + "' ");
            stb.append("     AND VSCH.SEMESTER = '" + param._regdSemester + "' ");
            stb.append("     AND VSCH.GRADE = '" + param._grade + "' ");
            stb.append("     AND VSCH.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append(" ORDER BY ");
            stb.append("     VSCH.GRADE, ");
            stb.append("     VSCH.HR_CLASS, ");
            stb.append("     VSCH.ATTENDNO ");

            return stb.toString();
        }

        private static void setCourseInterviewDat(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.INTERVIEW_DIV, ");
                stb.append("     T1.INTERVIEW_REMARK ");
                stb.append(" FROM COURSE_INTERVIEW_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("    T1.YEAR = '" + param._year + "' ");
                stb.append("    AND T1.SCHREGNO = ? ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._courseInterviewDatRemarkMap = new HashMap();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        student._courseInterviewDatRemarkMap.put(rs.getString("INTERVIEW_DIV"), rs.getString("INTERVIEW_REMARK"));
                    }

                    DbUtils.closeQuietly(rs);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private static void setClubCommitteeSikakuList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     '1_CLUB' AS DIV, ");
                stb.append("     '9' AS SEMESTER, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS SEMESTERNAME, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.CLUBCD AS ITEMCD, ");
                stb.append("     T2.CLUBNAME AS ITEMNAME, ");
                stb.append("     T3.NAME1 AS ITEMNAME2, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS ITEMNAME3, ");
                stb.append("     ROW_NUMBER() OVER(ORDER BY T1.SDATE, T1.CLUBCD) AS ORDER ");
                stb.append(" FROM SCHREG_CLUB_HIST_DAT T1 ");
                stb.append(" INNER JOIN CLUB_MST T2 ON T2.CLUBCD = T1.CLUBCD ");
                stb.append(" LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'J001' AND T3.NAMECD2 = T1.EXECUTIVECD ");
                stb.append(" WHERE ");
                stb.append("    '" + param._date + "' BETWEEN T1.SDATE AND VALUE(T1.EDATE, '9999-12-31') ");
                stb.append("     AND T1.SCHREGNO = ? ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     '2_COMMITTEE' AS DIV, ");
                stb.append("     T3.SEMESTER, ");
                stb.append("     T3.SEMESTERNAME, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.COMMITTEE_FLG || '-' || T1.COMMITTEECD AS ITEMCD, ");
                stb.append("     T2.COMMITTEENAME AS ITEMNAME, ");
                stb.append("     T4.NAME1 AS ITEMNAME2, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS ITEMNAME3, ");
                stb.append("     ROW_NUMBER() OVER(ORDER BY SEQ) AS ORDER ");
                stb.append(" FROM SCHREG_COMMITTEE_HIST_DAT T1 ");
                stb.append(" INNER JOIN COMMITTEE_MST T2 ON T2.COMMITTEE_FLG = T1.COMMITTEE_FLG ");
                stb.append("     AND T2.COMMITTEECD = T1.COMMITTEECD ");
                stb.append(" INNER JOIN SEMESTER_MST T3 ON T3.YEAR = T1.YEAR ");
                stb.append("     AND T3.SEMESTER = T1.SEMESTER ");
                stb.append(" LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'J003' AND T4.NAMECD2 = T1.EXECUTIVECD ");
                stb.append(" WHERE ");
                stb.append("    T1.YEAR = '" + param._year + "' ");
                stb.append("    AND T1.SCHREGNO = ? ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("   '3_SIKAKU' AS DIV, ");
                stb.append("   '9' AS SEMESTER, ");
                stb.append("   CAST(NULL AS VARCHAR(1)) AS SEMESTERNAME, ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   T2.QUALIFIED_CD AS ITEMCD, ");
                stb.append("   T2.QUALIFIED_NAME AS ITEMNAME, ");
                stb.append("   T3.NAME1 AS ITEMNAME2, ");
                stb.append("   T1.CONTENTS AS ITEMNAME3, ");
                stb.append("   ROW_NUMBER() OVER(ORDER BY SEQ) AS ORDER ");
                stb.append(" FROM SCHREG_QUALIFIED_HOBBY_DAT T1 ");
                stb.append(" LEFT JOIN QUALIFIED_MST T2 ON T2.QUALIFIED_CD = T1.QUALIFIED_CD ");
                stb.append(" LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'H312' ");
                stb.append("     AND T3.NAMECD2 = T1.RANK ");
                stb.append(" WHERE ");
                stb.append("     T1.SCHREGNO = ? ");
                stb.append(" ORDER BY ");
                stb.append("     DIV, ORDER ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._clubCommitteeSikakuText = null;

                    ps.setString(1, student._schregno);
                    ps.setString(2, student._schregno);
                    ps.setString(3, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String div = rs.getString("DIV");
                        final String semester = rs.getString("SEMESTER");
                        final String semestername = rs.getString("SEMESTERNAME");
                        final String itemname = rs.getString("ITEMNAME");
                        final String itemname2 = rs.getString("ITEMNAME2");
                        final String itemname3 = rs.getString("ITEMNAME3");

                        String text = null;
                        if ("1_CLUB".equals(div)) {
                            text = StringUtils.defaultString(itemname) + (StringUtils.isEmpty(itemname2) ? "" : "(" + StringUtils.defaultString(itemname2) + ")");
                        } else if ("2_COMMITTEE".equals(div)) {
                            text = StringUtils.defaultString(itemname) + ("9".equals(semester) || StringUtils.isEmpty(semestername) ? "" : "(" + StringUtils.defaultString(semestername) + ")");
                        } else if ("3_SIKAKU".equals(div)) {
                            if (StringUtils.isBlank(itemname)) {
                                text = StringUtils.defaultString(itemname3);
                            } else {
                                text = StringUtils.defaultString(itemname) + (StringUtils.isEmpty(itemname2) ? "" : "(" + StringUtils.defaultString(itemname2) + ")");
                            }
                        }
                        if (null != text) {
                            if (null == student._clubCommitteeSikakuText) {
                                student._clubCommitteeSikakuText = "";
                            } else {
                                student._clubCommitteeSikakuText += "\n";
                            }
                            student._clubCommitteeSikakuText += text;
                        }
                    }

                    DbUtils.closeQuietly(rs);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }

    /** 1日出欠カウント */
    private static class DayAttendance {
        /** 授業日数 */
        private int _lesson;
        /** 忌引日数 */
        private int _mourning;
        /** 出停日数 */
        private int _suspend;
        private int _virus;
        private int _koudome;
        /** 出席すべき日数 */
        private int _mlesson;
        /** 欠席日数 */
        private int _sick;
        /** 出席日数 */
        private int _attend;
        /** 遅刻日数 */
        private int _late;
        /** 早退日数 */
        private int _early;

        boolean _isKaikin;
        boolean _isSeikin;

        private void setKaikinSeikin(final Param param) {
            final DayAttendance da = this;
            if (da._lesson == 0) { // 対象外
                return;
            }
            final int kansanTikokuSoutai = da._sick * param._kessekiKansan + da._late + da._early;
            final int kaikinTikokuMax = param._kaikinKesseki * param._kessekiKansan + param._kaikinKaikinTikoku;
            final int seikinTikokuMax = param._seikinKesseki * param._kessekiKansan + param._kaikinSeikinTikoku;
            //log.debug(" tikoku max (kaikin, seikin = (" + kaikinTikokuMax + ", " + seikinTikokuMax + ")");
            _isKaikin = kansanTikokuSoutai <= kaikinTikokuMax;
            _isSeikin = kaikinTikokuMax < kansanTikokuSoutai && kansanTikokuSoutai <= seikinTikokuMax;
            //log.debug(" student = " + student._schregno + ", (lesson, sick, late + early, kansanTikokuSoutai, limit) = (" + da._lesson + ", " + da._sick + ", " + (da._late + da._early) + ", "+ kansanTikokuSoutai + ", " + limit + ")");
        }

        private static void setAttendData(final DB2UDB db2, final Param param, final Collection studentList) {
            PreparedStatement ps1 = null;
            ResultSet rs = null;
            String sql = null;
            try {
                String ATTEND = "ATTEND";
                String ATTEND_DAY = "ATTEND_DAY";
                if (null == param._psMap.get(ATTEND)) {
                    // 出欠の情報
                    final Map hasuuMap = AttendAccumulate.getHasuuMap(db2, param._year, param._sdate, param._date);
                    log.debug(" hasuuMap = " + hasuuMap);

                    final String semesInState = (String) hasuuMap.get("attendSemesInState");
                    // 1日単位
                    sql = getAttendSemesDat(param._year, param, semesInState);

                    param._psMap.put(ATTEND, db2.prepareStatement(sql));

                    final String aftDayFrom = (String) hasuuMap.get("aftDayFrom");
                    final String aftDayTo = (String) hasuuMap.get("aftDayTo");

                    if (null != aftDayFrom && null != aftDayTo) {
                        param._sqlAttend = getAttendDayDatSql(param, semesInState, param._semeMonth, aftDayFrom, aftDayTo);
                        param._psMap.put(ATTEND_DAY, db2.prepareStatement(param._sqlAttend));
                    }
                }
                PreparedStatement ps;
                ps = (PreparedStatement) param._psMap.get(ATTEND);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    final String key = param._grade;
                    if (null == student._dayAttendanceMap.get(key)) {
                        student._dayAttendanceMap.put(key, new DayAttendance());
                    }
                    DayAttendance att = (DayAttendance) student._dayAttendanceMap.get(key);

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final int lesson   = rs.getInt("LESSON");
                        final int sick     = rs.getInt("SICK"); // 病欠日数
                        att._lesson   += lesson;
                        att._sick     += sick;
                        att._late     += rs.getInt("LATE");
                        att._early    += rs.getInt("EARLY");

                    }
                    DbUtils.closeQuietly(rs);
                }

                if (null != param._psMap.get(ATTEND_DAY)) {
                    ps = (PreparedStatement) param._psMap.get(ATTEND_DAY);
                    ps.setString(1, param._grade);
                    ps.setString(2, param._hrClass.substring(2));

                    for (final Iterator it = studentList.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();

                        final String key = param._grade;
                        if (null == student._dayAttendanceMap.get(key)) {
                            student._dayAttendanceMap.put(key, new DayAttendance());
                        }
                        DayAttendance att = (DayAttendance) student._dayAttendanceMap.get(key);

                        ps.setString(3, student._schregno);

                        rs = ps.executeQuery();
                        while (rs.next()) {
                            final int lesson   = rs.getInt("LESSON");
                            final int sick     = rs.getInt("SICK"); // 病欠日数
                            att._lesson   += lesson;
                            att._sick     += sick;
                            att._late     += rs.getInt("LATE");
                            att._early    += rs.getInt("EARLY");
                        }
                        DbUtils.closeQuietly(rs);
                    }
                }

                final StringBuffer sqlPast = new StringBuffer();
                sqlPast.append(" SELECT T1.GRADE, SUM(LESSON) AS LESSON, SUM(VALUE(SICK, 0) + VALUE(NOTICE,0) + VALUE(NONOTICE, 0)) AS SICK, SUM(LATE) AS LATE, SUM(EARLY) AS EARLY ");
                sqlPast.append(" FROM (SELECT DISTINCT YEAR, SCHREGNO, GRADE FROM SCHREG_REGD_DAT WHERE YEAR < '" + param._year + "' AND SCHREGNO = ?) T1  ");
                sqlPast.append(" INNER JOIN ATTEND_SEMES_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR  ");
                sqlPast.append(" GROUP BY T1.GRADE ");

                ps1 = db2.prepareStatement(sqlPast.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps1.setString(1, student._schregno);

                    rs = ps1.executeQuery();
                    while (rs.next()) {

                        final String key = rs.getString("GRADE");
                        if (null == student._dayAttendanceMap.get(key)) {
                            student._dayAttendanceMap.put(key, new DayAttendance());
                        }
                        DayAttendance att = (DayAttendance) student._dayAttendanceMap.get(key);

                        att._lesson   += rs.getInt("LESSON");
                        att._sick     += rs.getInt("SICK");
                        att._late     += rs.getInt("LATE");
                        att._early    += rs.getInt("EARLY");
                    }
                    DbUtils.closeQuietly(rs);
                }

            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(ps1);
            }
        }

        private static String getAttendSemesDat(final String year, final Param param, final String semesInState) {
            //月別集計データから集計した表
            final StringBuffer stb = new StringBuffer();
            stb.append("    SELECT ");
            stb.append("        SCHREGNO, ");
            stb.append("        SUM( VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
            if (null != param._knjSchoolMst._semOffDays && param._knjSchoolMst._semOffDays.equals("1")) {
                stb.append("           + VALUE(OFFDAYS, 0) ");
            }
            stb.append("        ) AS LESSON, ");
            stb.append("        SUM(MOURNING) AS MOURNING, ");
            stb.append("        SUM(SUSPEND) AS SUSPEND, ");
            stb.append("        SUM(ABSENT) AS ABSENT, ");
            stb.append("        SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ");
            if (null != param._knjSchoolMst._semOffDays && param._knjSchoolMst._semOffDays.equals("1")) {
                stb.append("           + VALUE(OFFDAYS, 0) ");
            }
            stb.append("        ) AS SICK, ");
            stb.append("        SUM(LATE) AS LATE, ");
            stb.append("        SUM(EARLY) AS EARLY, ");
            if ("true".equals(param._useVirus)) {
                stb.append("        SUM(VIRUS) AS VIRUS, ");
            } else {
                stb.append("        0 AS VIRUS, ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append("        SUM(KOUDOME) AS KOUDOME, ");
            } else {
                stb.append("        0 AS KOUDOME, ");
            }
            stb.append("        SUM(ABROAD) AS ABROAD, ");
            stb.append("        SUM(OFFDAYS) AS OFFDAYS ");
            stb.append("    FROM ");
            stb.append("        V_ATTEND_SEMES_DAT W1 ");
            stb.append("    WHERE ");
            stb.append("        W1.YEAR = '" + year + "' ");
            stb.append("        AND W1.SEMESTER || W1.MONTH IN " + semesInState + " ");
            stb.append("        AND W1.SCHREGNO = ? ");
            stb.append("    GROUP BY ");
            stb.append("        W1.SCHREGNO ");
            return stb.toString();
        }

        private static String getAttendDayDatSql(final Param param, final String semesInState, final String semeMonth, final String sdate, final String edate) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHEDULES AS (SELECT ");
            stb.append("     T1.MONTH, ");
            stb.append("     SUM(T1.LESSON) AS LESSON ");
            stb.append("   FROM ");
            stb.append("     ATTEND_SEMES_LESSON_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER || T1.MONTH NOT IN " + semesInState + " ");
            stb.append("     AND T1.GRADE = ? ");
            stb.append("     AND T1.HR_CLASS = ? ");
            stb.append("     AND (CASE WHEN INT(T1.MONTH) < 4 THEN CAST((INT(T1.SEMESTER) + 1) AS CHAR(1)) ELSE T1.SEMESTER END) || T1.MONTH <= '" + semeMonth + "' ");
            stb.append("   GROUP BY ");
            stb.append("     T1.MONTH ");
            stb.append(" ), ATTEND_SEMES AS (SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     MONTH(T1.ATTENDDATE) AS IMONTH, ");
            stb.append("     MAX(VALUE(T4.LESSON,0)) AS LESSON, ");
            stb.append("     SUM(CASE WHEN T1.DI_CD IN ('2', '9') THEN 1 ELSE 0 END) AS SUSPEND, ");
            if ("true".equals(param._useVirus)) {
                stb.append("     SUM(CASE WHEN T1.DI_CD IN ('19', '20') THEN 1 ELSE 0 END) AS VIRUS, ");
            } else {
                stb.append("     0 AS VIRUS, ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append("     SUM(CASE WHEN T1.DI_CD IN ('25', '26') THEN 1 ELSE 0 END) AS KOUDOME, ");
            } else {
                stb.append("     0 AS KOUDOME, ");
            }
            stb.append("     SUM(CASE WHEN T1.DI_CD IN ('3', '10') THEN 1 ELSE 0 END) AS MOURNING, ");
            stb.append("     SUM(CASE WHEN T1.DI_CD IN ('4', '11') THEN 1 ELSE 0 END) AS SICK, ");
            stb.append("     SUM(CASE WHEN T1.DI_CD IN ('5', '12') THEN 1 ELSE 0 END) AS NOTICE, ");
            stb.append("     SUM(CASE WHEN T1.DI_CD IN ('6', '13') THEN 1 ELSE 0 END) AS NONOTICE, ");
            stb.append("     SUM(CASE WHEN T1.DI_CD IN ('15','23','24') THEN 1 ELSE 0 END) AS LATE, ");
            stb.append("     SUM(CASE WHEN T1.DI_CD IN ('16') THEN 1 ELSE 0 END) AS EARLY, ");
            stb.append("     0 AS KEKKA_JISU ");
            stb.append("   FROM ");
            stb.append("     SCHEDULES T4 ");
            stb.append("     LEFT JOIN ATTEND_DAY_DAT T1 ON T1.SCHREGNO = ? ");
            stb.append("         AND INT(T4.MONTH) = MONTH(T1.ATTENDDATE) ");
            stb.append("         AND T1.ATTENDDATE BETWEEN '" + sdate + "' AND '" + edate + "' ");
            stb.append("   GROUP BY ");
            stb.append("     T1.SCHREGNO, MONTH(T1.ATTENDDATE) ");
            stb.append(" ) ");
            stb.append("  SELECT ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    SUM(T1.LESSON) AS LESSON, ");
            stb.append("    SUM(T1.SUSPEND) AS SUSPEND, ");
            stb.append("    SUM(T1.VIRUS) AS VIRUS, ");
            stb.append("    SUM(T1.KOUDOME) AS KOUDOME, ");
            stb.append("    SUM(T1.MOURNING) AS MOURNING, ");
            stb.append("    SUM(T1.SICK) AS SICK, ");
            stb.append("    SUM(T1.NOTICE) AS NOTICE, ");
            stb.append("    SUM(T1.NONOTICE) AS NONOTICE, ");
            stb.append("    SUM(T1.LATE) AS LATE, ");
            stb.append("    SUM(T1.EARLY) AS EARLY, ");
            stb.append("    SUM(T1.KEKKA_JISU) AS KEKKA_JISU ");
            stb.append("  FROM ");
            stb.append("    ATTEND_SEMES T1 ");
            stb.append("  GROUP BY ");
            stb.append("    T1.SCHREGNO ");

            return stb.toString();
        }
    }

    private static class TestScore {

        public static void setScoreList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final String score = rs.getString("SCORE");

                        getMappedMap(student._testScoreMap, rs.getString("SEM_TESTCD")).put(subclasscd, score);
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   RANK.SCHREGNO, ");
            stb.append("   RANK.SEMESTER || RANK.TESTKINDCD || RANK.TESTITEMCD || RANK.SCORE_DIV AS SEM_TESTCD, ");
            stb.append("   RANK.SUBCLASSCD, ");
            stb.append("   RANK.SCORE ");
            stb.append(" FROM ");
            stb.append(" RECORD_RANK_SDIV_DAT RANK ");
            stb.append(" WHERE ");
            stb.append("   RANK.YEAR = '" + param._year + "' ");
            stb.append("   AND RANK.TESTKINDCD || RANK.TESTITEMCD || RANK.SCORE_DIV = '990008' "); // 学期評価
            stb.append("   AND RANK.SUBCLASSCD IN ('" + SUBCLASS3 + "', '" + SUBCLASS5 + "', '" + SUBCLASS9 + "') ");
            stb.append("   AND RANK.SCHREGNO = ? ");
            stb.append(" ORDER BY SUBCLASSCD ");
            return stb.toString();
        }
    }

//    /**
//     * 観点の教科
//     */
//    private static class ViewClass {
//        final String _classcd;
//        final String _subclasscd;
//        final String _subclassname;
//        final String _electDiv;
//        final List _viewList;
//        final List _valuationList;
//        ViewClass(
//                final String classcd,
//                final String subclasscd,
//                final String subclassname,
//                final String electDiv) {
//            _classcd = classcd;
//            _subclasscd = subclasscd;
//            _subclassname = subclassname;
//            _electDiv = electDiv;
//            _viewList = new ArrayList();
//            _valuationList = new ArrayList();
//        }
//
//        public void addView(final String viewcd, final String viewname) {
//            _viewList.add(new String[]{viewcd, viewname});
//        }
//
//        public String getViewCd(final int i) {
//            return ((String[]) _viewList.get(i))[0];
//        }
//
//        public String getViewName(final int i) {
//            return ((String[]) _viewList.get(i))[1];
//        }
//
//        public int getViewSize() {
//            return _viewList.size();
//        }
//
//        public static List getViewClassList(final DB2UDB db2, final Param param) {
//            final List list = new ArrayList();
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                final String sql = getViewClassSql(param);
//                // log.debug(" sql = " + sql);
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//
//                    final String classcd = rs.getString("CLASSCD");
//                    final String subclasscd = rs.getString("SUBCLASSCD");
//                    final String subclassname = rs.getString("SUBCLASSNAME");
//                    final String electDiv = rs.getString("ELECTDIV");
//                    final String viewcd = rs.getString("VIEWCD");
//                    final String viewname = rs.getString("VIEWNAME");
//
//                    ViewClass viewClass = null;
//                    for (final Iterator it = list.iterator(); it.hasNext();) {
//                        final ViewClass viewClass0 = (ViewClass) it.next();
//                        if (viewClass0._subclasscd.equals(subclasscd)) {
//                            viewClass = viewClass0;
//                            break;
//                        }
//                    }
//
//                    if (null == viewClass) {
//                        viewClass = new ViewClass(classcd, subclasscd, subclassname, electDiv);
//                        list.add(viewClass);
//                    }
//
//                    viewClass.addView(viewcd, viewname);
//                }
//            } catch (SQLException e) {
//                log.error("exception!", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//            return list;
//        }
//
//        private static String getViewClassSql(final Param param) {
//            final StringBuffer stb = new StringBuffer();
//            stb.append(" SELECT ");
//            stb.append("     T1.GRADE, ");
//            stb.append("     T3.CLASSCD, ");
//            stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
//            stb.append("     VALUE(T4.SUBCLASSORDERNAME2, T4.SUBCLASSNAME) AS SUBCLASSNAME, ");
//            stb.append("     VALUE(T4.ELECTDIV, '0') AS ELECTDIV, ");
//            stb.append("     T1.VIEWCD, ");
//            stb.append("     T1.VIEWNAME ");
//            stb.append(" FROM ");
//            stb.append("     JVIEWNAME_GRADE_MST T1 ");
//            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
//            stb.append("         AND T2.GRADE = T1.GRADE ");
//            stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
//            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
//            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
//            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
//            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
//            stb.append("     INNER JOIN CLASS_MST T3 ON T3.CLASSCD = SUBSTR(T1.VIEWCD, 1, 2) ");
//            stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND  ");
//            stb.append("     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
//            stb.append("         AND T4.CLASSCD = T1.CLASSCD  ");
//            stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND  ");
//            stb.append("         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD  ");
//            stb.append(" WHERE ");
//            stb.append("     T1.GRADE = '" + param._grade + "' ");
//            stb.append("     AND T3.CLASSCD < '90' ");
//            stb.append(" ORDER BY ");
//            stb.append("     VALUE(T4.ELECTDIV, '0'), ");
//            stb.append("     VALUE(T3.SHOWORDER3, -1), ");
//            stb.append("     T3.CLASSCD, ");
//            stb.append("     VALUE(T1.SHOWORDER, -1), ");
//            stb.append("     T1.VIEWCD ");
//            return stb.toString();
//        }
//        public String toString() {
//            return _subclasscd + ":" + _subclassname + " " + _electDiv;
//        }
//    }

    /**
     * 学習の記録（観点）
     */
    private static class ViewRecord {

        final String _semester;
        final String _viewcd;
        final String _status;
        final String _grade;
        final String _viewname;
        final String _classcd;
        final String _subclasscd;
        final String _classMstShoworder;
        final String _showorder;
        ViewRecord(
                final String semester,
                final String viewcd,
                final String status,
                final String grade,
                final String viewname,
                final String classcd,
                final String subclasscd,
                final String classMstShoworder,
                final String showorder) {
            _semester = semester;
            _viewcd = viewcd;
            _status = status;
            _grade = grade;
            _viewname = viewname;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classMstShoworder = classMstShoworder;
            _showorder = showorder;
        }

        public static ViewRecord getViewRecord(final String semester, final String subclasscd, final String viewcd, final List viewRecordList) {
            if (null == semester || null == subclasscd || null == viewcd) {
                return null;
            }
            for (final Iterator it = viewRecordList.iterator(); it.hasNext();) {
                final ViewRecord vr = (ViewRecord) it.next();
                if (semester.equals(vr._semester) && subclasscd.equals(vr._subclasscd) && viewcd.equals(vr._viewcd)) {
                    return vr;
                }
            }
            return null;
        }

        public static void setViewRecordList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewRecordSql(param);
                //log.debug(" view record sql = "+  sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {

                        final String semester = rs.getString("SEMESTER");
                        final String viewcd = rs.getString("VIEWCD");
                        final String status = rs.getString("STATUS");
                        final String grade = rs.getString("GRADE");
                        final String viewname = rs.getString("VIEWNAME");
                        final String classcd = rs.getString("CLASSCD");
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final String classMstShoworder = rs.getString("CLASS_MST_SHOWORDER");
                        final String showorder = rs.getString("SHOWORDER");

                        final ViewRecord viewRecord = new ViewRecord(semester, viewcd, status, grade, viewname, classcd, subclasscd, classMstShoworder, showorder);

                        student._viewRecordList.add(viewRecord);
                    }
                    DbUtils.closeQuietly(rs);
                }

            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getViewRecordSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE   ");
            stb.append("     , T1.VIEWNAME ");
            stb.append("     , T1.VIEWCD ");
            stb.append("     , T3.YEAR ");
            stb.append("     , T3.SEMESTER ");
            stb.append("     , T3.SCHREGNO ");
            stb.append("     , T3.STATUS ");
            stb.append("     , T4.CLASSCD ");
            stb.append("     ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     , T4.SHOWORDER AS CLASS_MST_SHOWORDER ");
            stb.append("     , T1.SHOWORDER ");
            stb.append(" FROM JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("     LEFT JOIN JVIEWSTAT_RECORD_DAT T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T3.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("         AND T3.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T3.YEAR = T2.YEAR ");
            //stb.append("         AND T3.SEMESTER <= '" + param._semester + "' ");
            stb.append("         AND T3.SCHREGNO = ? ");
            stb.append("     LEFT JOIN CLASS_MST T4 ON T4.CLASSCD = SUBSTR(T1.VIEWCD, 1, 2) ");
            stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T3.YEAR ");
            stb.append("     , T3.SEMESTER ");
            stb.append("     , VALUE(T4.SHOWORDER, 0) ");
            stb.append("     , T4.CLASSCD ");
            stb.append("     , VALUE(T1.SHOWORDER, 0) ");
            stb.append("     , T1.VIEWCD ");
            return stb.toString();
        }
    }

    /**
     * 学習の記録（評定）
     */
    private static class ViewValuation {
        final String _semester;
        final String _classcd;
        final String _subclasscd;
        final String _subclassname;
        final String _value;
        ViewValuation(
                final String semester,
                final String classcd,
                final String subclasscd,
                final String subclassname,
                final String value) {
            _semester = semester;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _value = value;
        }

        public static ViewValuation getViewValuation(final String semester, final String subclasscd, final List viewValuationList) {
            for (final Iterator it = viewValuationList.iterator(); it.hasNext();) {
                final ViewValuation vv = (ViewValuation) it.next();
                if (vv._semester.equals(semester) && vv._subclasscd.equals(subclasscd)) {
                    return vv;
                }
            }
            return null;
        }

        public static void setViewValuationList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewValuationSql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {

                        final String semester = rs.getString("SEMESTER");
                        final String classcd = rs.getString("CLASSCD");
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final String value = rs.getString("VALUE");
                        final ViewValuation viewValuation = new ViewValuation(semester, classcd, subclasscd, subclassname, value);

                        student._viewValuationList.add(viewValuation);
                    }
                    DbUtils.closeQuietly(rs);
                }

            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getViewValuationSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SEMESTER, ");
            stb.append("     SUBSTR(T2.SUBCLASSCD, 1, 2) AS CLASSCD, ");
            stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     VALUE(T4.SUBCLASSORDERNAME2, T4.SUBCLASSNAME) AS SUBCLASSNAME, ");
            stb.append("     T2.SCORE AS VALUE ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T2 ");
            stb.append("     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("         AND T4.CLASSCD = T2.CLASSCD  ");
            stb.append("         AND T4.SCHOOL_KIND = T2.SCHOOL_KIND  ");
            stb.append("         AND T4.CURRICULUM_CD = T2.CURRICULUM_CD  ");
            stb.append("     LEFT JOIN CLASS_MST T5 ON T5.CLASSCD = SUBSTR(T2.SUBCLASSCD, 1, 2) ");
            stb.append("         AND T5.SCHOOL_KIND = T2.SCHOOL_KIND  ");
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._year + "' ");
            //stb.append("     AND T2.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T2.TESTKINDCD = '99' ");
            stb.append("     AND T2.TESTITEMCD = '00' ");
            stb.append("     AND T2.SCORE_DIV = '08' "); // 学期評価
            stb.append("     AND T2.SCHREGNO = ? ");
//            if ("Y".equals(param._d016Namespare1)) {
//                stb.append("     AND NOT EXISTS ( ");
//                stb.append("         SELECT 'X' ");
//                stb.append("         FROM ");
//                stb.append("             SUBCLASS_REPLACE_COMBINED_DAT L1 ");
//                stb.append("         WHERE ");
//                stb.append("             L1.YEAR = T2.YEAR ");
//                stb.append("             AND L1.ATTEND_CLASSCD = T2.CLASSCD ");
//                stb.append("             AND L1.ATTEND_SCHOOL_KIND = T2.SCHOOL_KIND ");
//                stb.append("             AND L1.ATTEND_CURRICULUM_CD = T2.CURRICULUM_CD ");
//                stb.append("             AND L1.ATTEND_SUBCLASSCD = T2.SUBCLASSCD ");
//                stb.append("     ) ");
//            }
            stb.append(" ORDER BY ");
            stb.append("     T5.SHOWORDER3, ");
            stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD ");
            return stb.toString();
        }
    }

    private static class ProficiencyRank {
        final String _year;
        final String _mockcd;
        final String _mockname2;
        final String _schregno;
        final String _score333333;
        final BigDecimal _deviation333333;
        final String _score555555;
        final BigDecimal _deviation555555;
        final String _abbv1;

        ProficiencyRank(
            final String year,
            final String mockcd,
            final String mockname2,
            final String schregno,
            final String score333333,
            final BigDecimal deviation333333,
            final String score555555,
            final BigDecimal deviation555555,
            final String abbv1
        ) {
            _year = year;
            _mockcd = mockcd;
            _mockname2 = mockname2;
            _schregno = schregno;
            _score333333 = score333333;
            _deviation333333 = deviation333333;
            _score555555 = score555555;
            _deviation555555 = deviation555555;
            _abbv1 = abbv1;
        }

        public static void setProficiencyRankList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                //log.info(" sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._proficiencyRankList = new ArrayList();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String year = rs.getString("YEAR");
                        final String mockcd = rs.getString("MOCKCD");
                        final String mockname2 = rs.getString("MOCKNAME2");
                        final String schregno = rs.getString("SCHREGNO");
                        final String score333333 = rs.getString("SCORE333333");
                        final BigDecimal deviation333333 = rs.getBigDecimal("DEVIATION333333");
                        final String score555555 = rs.getString("SCORE555555");
                        final BigDecimal deviation555555 = rs.getBigDecimal("DEVIATION555555");
                        final String abbv1 = rs.getString("ABBV1");
                        final ProficiencyRank proficiencyrank = new ProficiencyRank(year, mockcd, mockname2, schregno, score333333, deviation333333, score555555, deviation555555, abbv1);
                        student._proficiencyRankList.add(proficiencyrank);
                    }
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH GRADE_YEAR_SET (GRADE, YEAR) AS (");
            stb.append(" " + param._proficiencyGradeYearString + " ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     YMST.YEAR, ");
            stb.append("     YMST.NAME1 AS MOCKCD, ");
            stb.append("     YMST.NAME2 AS GRADE, ");
            stb.append("     T1.MOCKNAME2, ");
            stb.append("     T2.SCHREGNO, ");
            stb.append("     T2.SINT_REMARK1 AS SCORE333333, ");
            stb.append("     T2.DECI_REMARK1 AS DEVIATION333333, ");
            stb.append("     T2.SINT_REMARK2 AS SCORE555555, ");
            stb.append("     T2.DECI_REMARK2 AS DEVIATION555555, ");
            stb.append("     YMST.NAME3 AS ABBV1 ");
            stb.append(" FROM ");
            stb.append("     MOCK_MST T1 ");
            stb.append("     INNER JOIN V_NAME_MST YMST ON YMST.NAMECD1 = 'D063' AND T1.MOCKCD = YMST.NAME1 ");
            stb.append("     LEFT JOIN MOCK_SCHREG_DAT T2 ON YMST.YEAR = T2.YEAR ");
            stb.append("         AND YMST.NAME1 = T2.MOCKCD ");
            stb.append("         AND T2.SCHREGNO = ? ");
            stb.append(" WHERE ");
            stb.append("     (YMST.NAME2, YMST.YEAR) IN (SELECT GRADE, YEAR FROM GRADE_YEAR_SET)");
            stb.append(" ORDER BY ");
            stb.append("    YMST.ABBV2, ");
            stb.append("    YMST.NAMECD2 ");
            return stb.toString();
        }
    }

    private static class CourseHope {
        final String _questionnairecd;
        final String _hopeNum;
        final String _schoolName;
        final String _facultyname;
        final String _departmentname;
        final String _baseScore;
        final String _necessaryScore;

        CourseHope(
            final String questionnairecd,
            final String hopeNum,
            final String schoolName,
            final String facultyname,
            final String departmentname,
            final String baseScore,
            final String necessaryScore
        ) {
            _questionnairecd = questionnairecd;
            _hopeNum = hopeNum;
            _schoolName = schoolName;
            _facultyname = facultyname;
            _departmentname = departmentname;
            _baseScore = baseScore;
            _necessaryScore = necessaryScore;
        }

        public static void setCourseHopeList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                //log.debug(" course hope sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._courseHopeList = new ArrayList();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String questionnairecd = rs.getString("QUESTIONNAIRECD");
                        final String hopeNum = rs.getString("HOPE_NUM");
                        final String schoolName = rs.getString("SCHOOL_NAME");
                        final String facultyname = rs.getString("FACULTYNAME");
                        final String departmentname = rs.getString("DEPARTMENTNAME");
                        final String baseScore = rs.getString("BASE_SCORE");
                        final String necessaryScore = rs.getString("NECESSARY_SCORE");
                        final CourseHope coursehope = new CourseHope(questionnairecd, hopeNum, schoolName, facultyname, departmentname, baseScore, necessaryScore);
                        student._courseHopeList.add(coursehope);
                    }
                }

                DbUtils.closeQuietly(rs);

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX_ENT AS ( ");
            stb.append("   SELECT ");
            stb.append("     T1.SCHREGNO, T1.QUESTIONNAIRECD, T1.ENTRYDATE, MAX(T1.SEQ) AS SEQ ");
            stb.append("   FROM COURSE_HOPE_DAT T1 ");
            stb.append("   INNER JOIN ( ");
            stb.append("     SELECT SCHREGNO, QUESTIONNAIRECD, MAX(ENTRYDATE) AS ENTRYDATE ");
            stb.append("     FROM COURSE_HOPE_DAT ");
            stb.append("     WHERE YEAR(ENTRYDATE) = " + param._year + " ");
            stb.append("     GROUP BY SCHREGNO, QUESTIONNAIRECD ");
            stb.append("   ) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("       AND T2.ENTRYDATE = T1.ENTRYDATE ");
            stb.append("       AND T2.QUESTIONNAIRECD = T1.QUESTIONNAIRECD ");
            stb.append("   WHERE T1.SCHREGNO = ? ");
            stb.append("     AND T1.QUESTIONNAIRECD IN " + SQLUtils.whereIn(true, param._shingakuSelected) + " ");
            stb.append("   GROUP BY T1.SCHREGNO, T1.QUESTIONNAIRECD, T1.ENTRYDATE ");
            stb.append(" ), COURSE_HOPE AS ( ");
            stb.append(" SELECT ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   1 AS HOPE_NUM, ");
            stb.append("   T1.QUESTIONNAIRECD, ");
            stb.append("   T1.SCHOOL_GROUP1 AS SCHOOL_GROUP, ");
            stb.append("   T1.FACULTY_GROUP1 AS FACULTY_GROUP, ");
            stb.append("   T1.DEPARTMENT_GROUP1 AS DEPARTMENT_GROUP, ");
            stb.append("   T1.SCHOOL_CD1 AS SCHOOL_CD, ");
            stb.append("   T1.FACULTYCD1 AS FACULTYCD, ");
            stb.append("   T1.DEPARTMENTCD1 AS DEPARTMENTCD, ");
            stb.append("   T1.HOWTOEXAM1 AS HOWTOEXAM, ");
            stb.append("   T1.JOBTYPE_LCD1 AS JOBTYPE_LCD, ");
            stb.append("   T1.JOBTYPE_MCD1 AS JOBTYPE_MCD, ");
            stb.append("   T1.JOBTYPE_SCD1 AS JOBTYPE_SCD, ");
            stb.append("   T1.WORK_AREA1 AS WORK_AREA, ");
            stb.append("   T1.INTRODUCTION_DIV1 AS INTRODUCTION_DIV ");
            stb.append(" FROM COURSE_HOPE_DAT T1 ");
            stb.append(" INNER JOIN MAX_ENT T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ENTRYDATE = T1.ENTRYDATE AND T2.SEQ = T1.SEQ AND T2.QUESTIONNAIRECD = T1.QUESTIONNAIRECD ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   2 AS HOPE_NUM, ");
            stb.append("   T1.QUESTIONNAIRECD, ");
            stb.append("   T1.SCHOOL_GROUP2 AS SCHOOL_GROUP, ");
            stb.append("   T1.FACULTY_GROUP2 AS FACULTY_GROUP, ");
            stb.append("   T1.DEPARTMENT_GROUP2 AS DEPARTMENT_GROUP, ");
            stb.append("   T1.SCHOOL_CD2 AS SCHOOL_CD, ");
            stb.append("   T1.FACULTYCD2 AS FACULTYCD, ");
            stb.append("   T1.DEPARTMENTCD2 AS DEPARTMENTCD, ");
            stb.append("   T1.HOWTOEXAM2 AS HOWTOEXAM, ");
            stb.append("   T1.JOBTYPE_LCD2 AS JOBTYPE_LCD, ");
            stb.append("   T1.JOBTYPE_MCD2 AS JOBTYPE_MCD, ");
            stb.append("   T1.JOBTYPE_SCD2 AS JOBTYPE_SCD, ");
            stb.append("   T1.WORK_AREA2 AS WORK_AREA, ");
            stb.append("   T1.INTRODUCTION_DIV1 AS INTRODUCTION_DIV ");
            stb.append(" FROM COURSE_HOPE_DAT T1 ");
            stb.append(" INNER JOIN MAX_ENT T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ENTRYDATE = T1.ENTRYDATE AND T2.SEQ = T1.SEQ AND T2.QUESTIONNAIRECD = T1.QUESTIONNAIRECD ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.HOPE_NUM, ");
            stb.append("   T2.QUESTIONNAIRECD, ");
            stb.append("   T1.SCHOOL_GROUP, ");
            stb.append("   T1.FACULTY_GROUP, ");
            stb.append("   T1.DEPARTMENT_GROUP, ");
            stb.append("   T1.SCHOOL_CD, ");
            stb.append("   T1.FACULTYCD, ");
            stb.append("   T1.DEPARTMENTCD, ");
            stb.append("   T1.HOWTOEXAM, ");
            stb.append("   T1.JOBTYPE_LCD, ");
            stb.append("   T1.JOBTYPE_MCD, ");
            stb.append("   T1.JOBTYPE_SCD, ");
            stb.append("   T1.WORK_AREA, ");
            stb.append("   T1.INTRODUCTION_DIV ");
            stb.append(" FROM COURSE_HOPE_DETAIL_DAT T1 ");
            stb.append(" INNER JOIN MAX_ENT T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ENTRYDATE = T1.ENTRYDATE AND T2.SEQ = T1.SEQ ");
            stb.append(" WHERE ");
            stb.append("   3 <= T1.HOPE_NUM  ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.QUESTIONNAIRECD, ");
            stb.append("   T1.HOPE_NUM, ");
            stb.append("   T2.SCHOOL_NAME, ");
            stb.append("   T2.BASE_SCORE, ");
            stb.append("   T2.NECESSARY_SCORE, ");
            stb.append("   T3.FACULTYNAME, ");
            stb.append("   T4.DEPARTMENTNAME ");
            stb.append(" FROM COURSE_HOPE T1 ");
            stb.append(" LEFT JOIN COLLEGE_MST T2 ON T2.SCHOOL_CD = T1.SCHOOL_CD ");
            stb.append(" LEFT JOIN COLLEGE_FACULTY_MST T3 ON T3.SCHOOL_CD = T1.SCHOOL_CD ");
            stb.append("     AND  T3.FACULTYCD = T1.FACULTYCD ");
            stb.append(" LEFT JOIN COLLEGE_DEPARTMENT_MST T4 ON T4.SCHOOL_CD = T1.SCHOOL_CD ");
            stb.append("     AND T4.FACULTYCD = T1.FACULTYCD ");
            stb.append("     AND T4.DEPARTMENTCD = T1.DEPARTMENTCD ");
            stb.append(" ORDER BY ");
            stb.append("   T1.HOPE_NUM ");
            return stb.toString();
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    private static class Param {
        final String _year;
        final String _testcd;
        final String _semester;
        final String _sdate;
        final String _date;
        final String _tantouStaffcd;
        final String _tantouStaffname;
        final String _dateMonth;
        final String _dateDayOfMonth;
        final String _grade;
        final String _hrClass;
        final String _staffname;
        final String[] _categorySelected;
        final String _regdSemester;
        final String[] _shingakuSelected;
        final String _useVirus;
        final String _useKoudome;
        final String _proficiencyGradeYearString;

        final boolean _isSeireki;
        final String _gradeName1;
        final String _z010;
        final Map _semesterNameMap;
        final String _d064001Name1;
        final String _d064002Name1;
        final String _d064003Name1;
        final String[] _questionaireNames;
        final Map _psMap = new HashMap();
        KNJSchoolMst _knjSchoolMst;
        final String _semeMonth;
        String _sqlAttend;
//        List _viewClassList;

        final int _kessekiKansan;
        final int _kaikinKesseki;
        final int _kaikinKaikinTikoku;
        final int _seikinKesseki;
        final int _kaikinSeikinTikoku;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("CTRL_YEAR");
            _testcd = request.getParameter("TESTCD");
            _semester = request.getParameter("SEMESTER");
            _regdSemester = "9".equals(_semester) ? request.getParameter("CTRL_SEMESTER") : _semester;
            _hrClass = request.getParameter("GRADE_HR_CLASS");
            _grade = _hrClass.substring(0, 2);
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            if (null == request.getParameterValues("SHINGAKU_SELECTED")) {
                _shingakuSelected = new String[2];
            } else {
                _shingakuSelected = new String[Math.max(2, request.getParameterValues("SHINGAKU_SELECTED").length)];
                for (int i = 0; i < request.getParameterValues("SHINGAKU_SELECTED").length; i++) {
                    _shingakuSelected[i] = request.getParameterValues("SHINGAKU_SELECTED")[i];
                }
            }
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _date = request.getParameter("DATE").replace('/', '-');
            _tantouStaffcd = request.getParameter("TANTOU");
            _dateMonth = StringUtils.split(_date, '-')[1];
            _dateDayOfMonth = StringUtils.split(_date, '-')[2];

            _tantouStaffname = getTantouStaffname(db2, _tantouStaffcd);
            _staffname = getStaffname(db2);
            _sdate = getSdate(db2);
            _semesterNameMap = getSemesterNameMap(db2);
            _gradeName1 = getGradeName1(db2);
            _z010 = setNameMst(db2, "Z010", "00", "NAME1");
            _isSeireki = "2".equals(setNameMst(db2, "Z012", "01", "NAME1"));
            _d064001Name1 = setNameMst(db2, "D064", "001", "NAME1");
            _d064002Name1 = setNameMst(db2, "D064", "002", "NAME1");
            _d064003Name1 = setNameMst(db2, "D064", "003", "NAME1");
            _questionaireNames = getQuestionnaireName(db2);
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (Exception ex) {
                log.error("Param load exception!", ex);
            }
            _semeMonth = getSemeMonth(db2, _date);

            _kessekiKansan = defval(request.getParameter("KESSEKI_KANSAN"), 3);
            _kaikinKesseki = defval(request.getParameter("KAIKIN_KESSEKI"), 0);
            _kaikinKaikinTikoku = defval(request.getParameter("KAIKIN_KAIKIN_TIKOKU"), -1);
            _seikinKesseki = defval(request.getParameter("SEIKIN_KESSEKI"), 3);
            _kaikinSeikinTikoku = defval(request.getParameter("KAIKIN_SEIKIN_TIKOKU"), -1);


            final StringBuffer stbGradeYear = new StringBuffer();
            final DecimalFormat d2 = new DecimalFormat("00");
            String comma = "";
            for (int p = 0; p < Integer.parseInt(_grade); p++) {
                final String grade = d2.format(Integer.parseInt(_grade) - p);
                final String year = String.valueOf(Integer.parseInt(_year) - p);
                stbGradeYear.append(comma).append("VALUES('" + grade + "', '" + year + "')");
                comma = " UNION ALL ";
            }
            _proficiencyGradeYearString = stbGradeYear.insert(0, "(").append(")").toString();
        }

        private static int defval(final String val, final int def) {
            return NumberUtils.isDigits(val) ? Integer.parseInt(val) : def;
        }

        private String getGradeName1(final DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("GRADE_NAME1");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String getTantouStaffname(final DB2UDB db2, final String staffcd) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT STAFFNAME FROM STAFF_MST T1 WHERE STAFFCD = '" + staffcd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("STAFFNAME");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String getStaffname(final DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT T2.STAFFNAME FROM SCHREG_REGD_FI_HDAT T1 "+
                                   " INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.TR_CD1 " +
                                   " WHERE T1.YEAR = '" + _year + "' AND T1.SEMESTER = '" + _semester + "' " +
                                   "   AND T1.GRADE || T1.HR_CLASS = '" + _hrClass + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("STAFFNAME");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private Map getSemesterNameMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Map rtn = new HashMap();
            try {
                final String sql = "SELECT SEMESTER, SEMESTERNAME, SDATE, EDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' ORDER BY SEMESTER ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"));
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String[] getQuestionnaireName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Map nameMap = new HashMap();
            try {
                final String sql = "SELECT QUESTIONNAIRECD, QUESTIONNAIRENAME FROM QUESTIONNAIRE_MST ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    nameMap.put(rs.getString("QUESTIONNAIRECD"), rs.getString("QUESTIONNAIRENAME"));
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            String[] rtn = new String[2];
            for (int i = 0; i < _shingakuSelected.length; i++) {
                rtn[i] = (String) nameMap.get(_shingakuSelected[i]);
            }
            return rtn;
        }

        private String setNameMst(final DB2UDB db2, final String namecd1, final String namecd2, final String field) {
            String rtnSt = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getNameMst(_year, namecd1, namecd2);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnSt = rs.getString(field);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtnSt;
        }

        private String getNameMst(final String year, final String namecd1, final String namecd2) {
            final String rtnSql = " SELECT "
                                + "     * "
                                + " FROM "
                                + "     V_NAME_MST "
                                + " WHERE "
                                + "     YEAR = '" + year + "' "
                                + "     AND NAMECD1 = '" + namecd1 + "' "
                                + "     AND NAMECD2 = '" + namecd2 + "'";
            return rtnSql;
        }

        private String changePrintYear() {
            if (_isSeireki) {
                return _year + "年度";
            } else {
                return nao_package.KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";
            }
        }

        private String getSdate(final DB2UDB db2) {
            String rtn = null;

            final String sql = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '1' ";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("SDATE");
                }
            } catch (Exception e) {
                log.error("exception!" + sql, e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String getSemeMonth(final DB2UDB db2, final String date) {
            final String sql = "SELECT SEMESTER, SDATE, EDATE, CASE WHEN '" + date + "' BETWEEN SDATE AND EDATE THEN '1' END AS HAS_BETWEEN FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER <> '9' ORDER BY SEMESTER ";

            String rsSemester = null;
            boolean hasBetween = false;
            String semFirstSdate = null;

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if ("1".equals(rs.getString("SEMESTER"))) {
                        semFirstSdate = rs.getString("SDATE");
                    }
                    rsSemester = rs.getString("SEMESTER");
                    if ("1".equals(rs.getString("HAS_BETWEEN"))) {
                        hasBetween = true;
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("exception!" + sql, e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            String semester = rsSemester;
            if (!hasBetween && null != semFirstSdate && date.compareTo(semFirstSdate) < 0) { // 1学期開始日より前日を指定した場合、1学期
                semester = "1";
            }
            String semeMonth;
            if (Integer.parseInt(_dateMonth) < 4) {
                semeMonth = String.valueOf(Integer.parseInt(semester) + 1) + _dateMonth;
            } else {
                semeMonth = semester + _dateMonth;
            }
            return semeMonth;
        }
    }
}

// eof
