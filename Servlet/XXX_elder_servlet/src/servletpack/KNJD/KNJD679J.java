/*
 * $Id: 8f9ffe11a78ab5eaa59e383dffdc0b001af4b89c $
 *
 * 作成日: 2015/08/04
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 文京学園　中学通知票
 */
public class KNJD679J {

    private static final Log log = LogFactory.getLog(KNJD679J.class);

    private boolean _hasData;

    private static String SEMEALL = "9";
    private static String SUBCLASSCD_333333 = "333333";
    private static String SUBCLASSCD_555555 = "555555";
    private static String SUBCLASSCD_777777 = "777777";
    private static String SUBCLASSCD_888888 = "888888";
    private static String SUBCLASSCD_999999 = "999999";

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

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List viewClassList = ViewClass.getViewClassList(db2, _param);

        final List studentList = getStudentList(db2, _param);

        for (final Iterator sit = studentList.iterator(); sit.hasNext();) {
            final Student student = (Student) sit.next();

            log.debug(" schregno = " + student._schregno);

            final boolean isPrintShuryosho = "9".equals(_param._semester);
            final String form = isPrintShuryosho ? "KNJD679J_2.frm" : "KNJD679J_1.frm";
            svf.VrSetForm(form, 4);

            printHeader(db2, svf, student);

            printSeiseki(svf, student); // 学期評価、学年評定

            printAttendance(svf, student); // 1日出欠

            printTokubetsuKatsudo(svf, student); // 朝間（特別活動）

            if(SEMEALL.equals(_param._semester)) {
                printMoral(svf, student); // 道徳
            }

            printSogo(svf, student); // 総合的な学習の時間所見

            printCommitteeClub(svf, student); // 委員会、クラブ

            printBehavior(svf, student); // 行動の状況

            if (isPrintShuryosho) {
                printShuryosho(db2, svf);
            }

            printView(svf, student, viewClassList);

            _hasData = true;
        }
    }

    private void printView(final Vrw32alp svf, final Student student, final List viewClassList) {
        int count = 0;
        for (int vci = 0; vci < viewClassList.size(); vci++) {
            final ViewClass vc = (ViewClass) viewClassList.get(vci);

            String subclassname = StringUtils.defaultString(vc._subclassname);
            final int viewlength = Math.max(subclassname.length(), vc._viewList.size());
            final String space = StringUtils.repeat(" ", (viewlength - subclassname.length()) / 2);
            subclassname = space + subclassname + space;
            subclassname = subclassname + StringUtils.repeat(" ", viewlength - subclassname.length());

            for (int vi = 0; vi < viewlength; vi++) {
                svf.VrsOut("VIEW_GPR", String.valueOf(vci + 1)); // 観点科目グループ
                if (vi < vc._viewList.size()) {
                    final String[] arr = (String[]) vc._viewList.get(vi);
                    final String viewcd = arr[0];
                    final String viewname = arr[1];
                    svf.VrsOut("VIEW_NAME", viewname); // 観点名称

                    final List viewRecordList = ViewRecord.getViewList(student._viewRecordList, vc._subclasscd, viewcd);
                    for (final Iterator it = viewRecordList.iterator(); it.hasNext();) {
                        final ViewRecord vr = (ViewRecord) it.next();
                        if ("1".equals(vr._semester)) {
                            svf.VrsOut("VIEW1", vr._status); // 観点
                        } else if ("9".equals(vr._semester)) {
                            svf.VrsOut("VIEW2", vr._status); // 観点
                        }
                    }
                }
                svf.VrsOut("VIEW_SUBCLASS", String.valueOf(subclassname.charAt(vi))); // 観点科目
                svf.VrEndRecord();
                count += 1;
            }
        }
        for (int vci = count; vci < 21 * 2; vci++) {
            svf.VrsOut("VIEW_GPR", "X"); // 観点科目グループ
            svf.VrEndRecord();
        }
    }

    private void printBehavior(final Vrw32alp svf, final Student student) {
        for (int j = 0; j < Math.min(10, student._behaviorList.size()); j++) {
            final Map row = (Map) student._behaviorList.get(j);
            if (!NumberUtils.isDigits((String) row.get("CODE"))) {
                continue;
            }
            final int line = Integer.parseInt((String) row.get("CODE"));
            svf.VrsOutn("BEHAVIOR_NAME", line, (String) row.get("NAME1")); // 行動の記録名称
            svf.VrsOutn("BEHAVIOR", line, (String) row.get("RECORD_NAME")); // 行動の記録
        }
    }

    private void printShuryosho(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrsOut("FIN_GRADE", "本校第" + _param._gradeCdStr + "学年の課程を"); // 修了学年
        final String finDate = KNJ_EditDate.h_format_JP(db2, _param._outputDate);
        svf.VrsOut("FIN_DATE", "" + (null == _param._outputDate ? "　　　年　月　日" : finDate)); // 修了日付

        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName); // 学校名
        if (null != _param._schoolStampImagePath) {
            svf.VrsOut("STAFFBTM", _param._schoolStampImagePath); //
        }
        svf.VrsOut("JOB_NAME", _param._certifSchoolJobName); // 役職名
        svf.VrsOut("PRINCIPAL_NAME", _param._certifSchoolPrincipalName); // 校長名
    }

    private void printSeiseki(final Vrw32alp svf, final Student student) {
        for (int tii = 0; tii < _param._testitemList.size(); tii++) {
            final Testitem testitem = (Testitem) _param._testitemList.get(tii);

            final Score s = (Score) getMappedMap(student._scoreMap, testitem._semester + testitem._testcd).get(SUBCLASSCD_999999);
            if (null != s) {
                final int line = tii + 1;
                svf.VrsOutn("AVERAGE1", line, sishagonyu(s._avg)); // 総平均点
            }
        }

        student._subclassMap.remove(SUBCLASSCD_333333);
        student._subclassMap.remove(SUBCLASSCD_555555);
        student._subclassMap.remove(SUBCLASSCD_777777);
        student._subclassMap.remove(SUBCLASSCD_888888);
        student._subclassMap.remove(SUBCLASSCD_999999);
        final List subclassList = new ArrayList(student._subclassMap.values());
        Collections.sort(subclassList);

        for (int subi = 0; subi < Math.min(16, subclassList.size()); subi++) {
            final Subclass subclass = (Subclass) subclassList.get(subi);

            if (subclass._subclasscd.startsWith("9")) {
                continue;
            }

            final List subclassNameTokenList = getTokenList(subclass._subclassname, 6);
            for (int i = 0; i < subclassNameTokenList.size(); i++) {
                svf.VrsOutn("SUBCLASS_NAME" + String.valueOf(i + 1), subi + 1, (String) subclassNameTokenList.get(i)); // 科目名
            }
            for (int tii = 0; tii < _param._testitemList.size(); tii++) {
                final Testitem testitem = (Testitem) _param._testitemList.get(tii);

                if (null == testitem._semester || Integer.parseInt(testitem._semester) > Integer.parseInt(_param._semester)) {
                    continue;
                }

                final Score s = (Score) getMappedMap(student._scoreMap, testitem._semester + testitem._testcd).get(subclass._subclasscd);
                if (null != s) {
                    if ("9990009".equals(testitem._semester + testitem._testcd)) {
                        svf.VrsOutn("SCORE" + String.valueOf(tii + 1), subi + 1, null == s._score ? "-" : s._score); // 素点
                    } else {
                        svf.VrsOutn("SCORE" + String.valueOf(tii + 1), subi + 1, s._score); // 素点
                    }
                } else {
                    if ("9990009".equals(testitem._semester + testitem._testcd)) {
                        svf.VrsOutn("SCORE" + String.valueOf(tii + 1), subi + 1, "-"); // 素点
                    }
                }
            }
        }
    }

    private void printCommitteeClub(final Vrw32alp svf, final Student student) {
        //log.debug(" committee = " + getMappedList(student._debugDataMap, "COMMITTEE"));
        for (final Iterator it = student._semesCommitteeListMap.entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = (Map.Entry) it.next();
            final String semester = (String) e.getKey();
            final int iSemester = Integer.parseInt(semester);
            final Map committeeFlgMap = (Map) e.getValue();
            for (final Iterator fit = committeeFlgMap.entrySet().iterator(); fit.hasNext();) {
                Map.Entry fe = (Map.Entry) fit.next();
                final String committeeFlg = (String) fe.getKey();
                final List textList = (List) fe.getValue();

                final String field = "2".equals(committeeFlg) ? "CHARGE" : "COMMITTEE";
                final int keta = "2".equals(committeeFlg) ? 18 : 10;
                final List tokenList = getTokenList(mkString(textList, "、"), keta);

                for (int j = 0; j < tokenList.size(); j++) {
                    svf.VrsOutn(field + String.valueOf(j + 1), iSemester, (String) tokenList.get(j)); // 委員会
                }
            }
        }

        if (!"1".equals(_param._semester)) {
            final List clubTextList = getTokenList(mkString(getMappedList(student._clubListMap, "DUMMY"), "、"), 30);
            for (int j = 0; j < Math.min(4, clubTextList.size()); j++) {
                final int line = j + 1;
                svf.VrsOutn("CLUB", line, (String) clubTextList.get(j)); // 部活動
            }
        }
    }

    private void printSogo(final Vrw32alp svf, final Student student) {
        final ShokenSize shokenSize = ShokenSize.getShokenSize(_param._RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_J, 22, 6);
        final List totalstudyactTokenList = getTokenList(student._recordTotalstudyact, shokenSize._mojisu * 2);
        for (int j = 0; j < totalstudyactTokenList.size(); j++) {
            final int line = j + 1;
            svf.VrsOutn("TOTAL_ACT", line, (String) totalstudyactTokenList.get(j)); // 総合的な学習の時間
        }
    }

    private void printMoral(final Vrw32alp svf, final Student student) {
        final ShokenSize shokenSize = ShokenSize.getShokenSize(_param._RECORD_TOTALSTUDYTIME_DAT_REMARK1_SIZE_J, 14, 5);
        final List remark1TokenList = getTokenList(student._recordRemark1, shokenSize._mojisu * 2);
        for (int j = 0; j < remark1TokenList.size(); j++) {
            final int line = j + 1;
            svf.VrsOutn("MORAL", line, (String) remark1TokenList.get(j)); // 道徳
        }
    }

    private void printTokubetsuKatsudo(final Vrw32alp svf, final Student student) {
        //log.debug(" special score = " + getMappedList(student._debugDataMap, "SPECIALSCORE"));

        for (int j = 0; j < Math.min(student._specialScoreSubclassList.size(), 4); j++) {
            final int line = j + 1;
            final Map m = (Map) student._specialScoreSubclassList.get(j);
            final String subclasscd = (String) m.get("SUBCLASSCD");
            final String subclassname = (String) m.get("SUBCLASSNAME");

            if (getMS932ByteLength(subclassname) > 4) {
                final String[] token = KNJ_EditEdit.get_token(subclassname, 4, 2);
                if (null != token) {
                    for (int i = 0; i < token.length; i++) {
                        svf.VrsOutn("MORNING2_" + String.valueOf(i + 1), line, token[i]); // 朝間
                    }
                }
            } else {
                svf.VrsOutn("MORNING1", line, subclassname); // 朝間
            }

            for (final Iterator it = getMappedMap(student._semesSpecialScoreMap, subclasscd).entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final String semester = (String) e.getKey();
                final String valueName = (String) e.getValue();
                svf.VrsOutn("MORNING_VALUE" + semester, line, valueName); // 朝間評価
            }
        }
    }

    private void printAttendance(final Vrw32alp svf, final Student student) {
        for (int semeline = 1; semeline <= 3; semeline++) {
            final int isemes = 3 == semeline ? 9 : semeline;
            if (isemes > Integer.parseInt(_param._semester)) {
                continue;
            }

            final Map attendSemes = getMappedMap(student._attendMap, String.valueOf(isemes));
            svf.VrsOutn("LESSON", semeline, toString(attendSemes.get("LESSON"))); // 授業日数
            svf.VrsOutn("SUSPEND", semeline, toString(attendSemes.get("SUSPEND"))); // 出停日数
            svf.VrsOutn("MOURNING", semeline, toString(attendSemes.get("MOURNING"))); // 忌引等日数
            svf.VrsOutn("PRESENT", semeline, toString(attendSemes.get("MLESSON"))); // 出席すべき日数
            svf.VrsOutn("ABSENCE1", semeline, toString(attendSemes.get("SICK_ONLY"))); // 欠席日数
            svf.VrsOutn("ABSENCE2", semeline, toString(attendSemes.get("NONOTICE_ONLY"))); // 欠席日数
            svf.VrsOutn("ABSENCE3", semeline, toString(attendSemes.get("SICK_NONOTICE"))); // 欠席日数
            svf.VrsOutn("ATTEND", semeline, toString(attendSemes.get("PRESENT"))); // 出席日数
            svf.VrsOutn("EARLY", semeline, toString(attendSemes.get("EARLY"))); // 早退回数
            svf.VrsOutn("LATE", semeline, toString(attendSemes.get("LATE"))); // 遅刻回数
        }
    }


    // prestringをstrのブランクの後に配置
    private String prepend(final String str, final String prestring) {
        final StringBuffer rtn = new StringBuffer();
        if (null == str) {
            rtn.append(prestring);
        } else {
            int subidx = -1;
            for (int i = 0; i < str.length(); i++) {
                final String c = String.valueOf(str.charAt(i));
                if (StringUtils.isBlank(c)) {
                    rtn.append(c);
                } else {
                    rtn.append(prestring);
                    rtn.append(c);
                    subidx = i;
                    break;
                }
            }
            if (-1 == subidx) {
                rtn.append(prestring);
            } else if (subidx + 1 <= str.length() - 1){
                rtn.append(str.substring(subidx + 1));
            }
        }
        return rtn.toString();
    }

    private void printHeader(final DB2UDB db2, final Vrw32alp svf, final Student student) {
    	final String title = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度　通知表";
        svf.VrsOut("TITLE", title); // タイトル
        svf.VrsOut("HR_NAME", StringUtils.defaultString(student._hrName) + "　" + (NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno)) + "番"); // 年組

        if (null != student._substaffname) {
            svf.VrsOut("TEACHER_NAME", StringUtils.defaultString(_param._certifSchoolHrJobName) + "　" + StringUtils.defaultString(student._staffname)); // 担任名
            svf.VrsOut("TEACHER_NAME_MARU1", "○"); // 学期名
            svf.VrsOut("TEACHER_NAME_IN1", "印"); // 学期名

            svf.VrsOut("TEACHER_NAME2", prepend(StringUtils.defaultString(_param._certifSchoolHrJobName), "副") + "　" + StringUtils.defaultString(student._substaffname)); // 担任名
            svf.VrsOut("TEACHER_NAME_MARU2", "○"); // 学期名
            svf.VrsOut("TEACHER_NAME_IN2", "印"); // 学期名
        } else {
            svf.VrsOut("TEACHER_NAME2", StringUtils.defaultString(_param._certifSchoolHrJobName) + "　" + StringUtils.defaultString(student._staffname)); // 担任名
            svf.VrsOut("TEACHER_NAME_MARU2", "○"); // 学期名
            svf.VrsOut("TEACHER_NAME_IN2", "印"); // 学期名
        }
        svf.VrsOut("NAME", "氏名　" + StringUtils.defaultString(student._name)); // 氏名

        svf.VrsOut("SEMESTER_NAME1_3", "学年"); // 学期名
        svf.VrsOut("SEMESTER_NAME2_3", "学年計"); // 学期名
        svf.VrsOut("SEMESTER_NAME4_3", "学年"); // 学期名
        svf.VrsOut("SEMESTER_NAME5_3", "学年"); // 学期名

        for (int i = 1; i <= 6; i++) {
            final String n = String.valueOf(i) + "_";
            for (int seme = 1; seme <= 2; seme++) {
                final String semester = String.valueOf(seme);
                String semestername = (String) _param._semesterNameMap.get(semester);
                svf.VrsOut("SEMESTER_NAME" + n + semester, semestername); // 学期名
            }
        }
    }

    private static List getTokenList(final String s, final int keta) {
        return KNJ_EditKinsoku.getTokenList(s, keta);
    }

    private static String toString(final Object o) {
        return null == o ? null : o.toString();
    }

    private static String mkString(final List textList, final String comma1) {
        final StringBuffer stb = new StringBuffer();
        String comma = "";
        for (final Iterator it = textList.iterator(); it.hasNext();) {
            final String text = (String) it.next();
            if (StringUtils.isBlank(text)) {
                continue;
            }
            stb.append(comma).append(text);
            comma = comma1;
        }
        return stb.toString();
    }

    private List getGroupList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static List getMappedList(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static String sishagonyu(final BigDecimal bd) {
        if (null == bd) {
            return null;
        }
        return bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String avg(final List scoreBdList) {
        if (scoreBdList.size() == 0) {
            return null;
        }
        BigDecimal sum = new BigDecimal(0);
        int count = 0;
        for (final Iterator it = scoreBdList.iterator(); it.hasNext();) {
            final BigDecimal scoreBd = (BigDecimal) it.next();
            if (null != scoreBd) {
                sum = sum.add(scoreBd);
                count += 1;
            }
        }
        if (count == 0) {
            return null;
        }
        return sum.divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static List getStudentList(final DB2UDB db2, final Param param) {
        final List studentList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH REGD AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SEMESTER, ");
        stb.append("   GDAT.GRADE_NAME1, ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.COURSECD, ");
        stb.append("   T1.MAJORCD, ");
        stb.append("   T1.COURSECODE, ");
        stb.append("   HDAT.HR_NAME, ");
        stb.append("   HDAT.HR_CLASS_NAME1, ");
        stb.append("   HRSTF.STAFFNAME, ");
        stb.append("   HRSUBSTF.STAFFNAME AS SUBSTAFFNAME, ");
        stb.append("   T1.ATTENDNO, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   BASE.NAME ");
        stb.append(" FROM SCHREG_REGD_DAT T1 ");
        stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
        stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
        stb.append("     AND GDAT.GRADE = T1.GRADE ");
        stb.append(" INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = T1.YEAR ");
        stb.append("     AND HDAT.SEMESTER = T1.SEMESTER ");
        stb.append("     AND HDAT.GRADE = T1.GRADE ");
        stb.append("     AND HDAT.HR_CLASS = T1.HR_CLASS ");
        stb.append(" LEFT JOIN STAFF_MST HRSTF ON HRSTF.STAFFCD = HDAT.TR_CD1 ");
        stb.append(" LEFT JOIN STAFF_MST HRSUBSTF ON HRSUBSTF.STAFFCD = HDAT.SUBTR_CD1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + param.getRegdSemester() + "' ");
        stb.append("     AND T1.GRADE = '" + param._grade + "' ");
//        stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
        stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._schregSelected) + " ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.GRADE_NAME1, ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.HR_NAME, ");
        stb.append("   T1.HR_CLASS_NAME1, ");
        stb.append("   T1.STAFFNAME, ");
        stb.append("   T1.SUBSTAFFNAME, ");
        stb.append("   T1.ATTENDNO, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.NAME, ");
        stb.append("   T2.CHAIRCD, ");
        stb.append("   CSTFM.STAFFCD AS CHAIR_STAFFCD, ");
        stb.append("   CSTFM.STAFFNAME AS CHAIR_STAFFNAME, ");
        stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("   VALUE(SUBM.SUBCLASSORDERNAME2, SUBM.SUBCLASSNAME) AS SUBCLASSNAME, ");
        stb.append("   SUBM.SUBCLASSABBV, ");
//        stb.append("   CRE.REQUIRE_FLG, ");
        stb.append("   TREC.VALUE_DI, ");
        stb.append("   TREC.SEMESTER || TREC.TESTKINDCD || TREC.TESTITEMCD || TREC.SCORE_DIV AS SEM_TESTCD, ");
        stb.append("   TRANK.SCORE, ");
        stb.append("   TRANK.AVG, ");
        stb.append("   TRANK.CLASS_RANK, ");
        stb.append("   TRANK.COURSE_RANK, ");
        stb.append("   TRANK.MAJOR_RANK ");
        stb.append(" FROM REGD T1 ");
        stb.append(" LEFT JOIN CHAIR_STD_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER <= T1.SEMESTER ");
        stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN CHAIR_DAT T3 ON T3.YEAR = T2.YEAR ");
        stb.append("     AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("     AND T3.CHAIRCD = T2.CHAIRCD ");
        stb.append("     AND T3.CLASSCD < '90' ");
        stb.append(" LEFT JOIN CHAIR_STF_DAT CSTF ON CSTF.YEAR = T3.YEAR ");
        stb.append("     AND CSTF.SEMESTER = T3.SEMESTER ");
        stb.append("     AND CSTF.CHAIRCD = T3.CHAIRCD ");
        stb.append("     AND CSTF.CHARGEDIV = 1 ");
        stb.append(" LEFT JOIN STAFF_MST CSTFM ON CSTFM.STAFFCD = CSTF.STAFFCD ");
        stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = T3.CLASSCD ");
        stb.append("     AND SUBM.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("     AND SUBM.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("     AND SUBM.SUBCLASSCD = T3.SUBCLASSCD ");
//        stb.append(" LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = T1.YEAR ");
//        stb.append("     AND CRE.COURSECD = T1.COURSECD ");
//        stb.append("     AND CRE.MAJORCD = T1.MAJORCD ");
//        stb.append("     AND CRE.GRADE = T1.GRADE ");
//        stb.append("     AND CRE.COURSECODE = T1.COURSECODE ");
//        stb.append("     AND CRE.CLASSCD = T3.CLASSCD ");
//        stb.append("     AND CRE.SCHOOL_KIND = T3.SCHOOL_KIND ");
//        stb.append("     AND CRE.CURRICULUM_CD = T3.CURRICULUM_CD ");
//        stb.append("     AND CRE.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append(" LEFT JOIN RECORD_SCORE_DAT TREC ON TREC.YEAR = T3.YEAR ");
        stb.append("     AND TREC.SEMESTER <= '" + param._semester + "' ");
        stb.append("     AND  (TREC.SEMESTER <> '9' AND TREC.TESTKINDCD || TREC.TESTITEMCD || TREC.SCORE_DIV = '990008' ");
        stb.append("        OR TREC.SEMESTER  = '9' AND TREC.TESTKINDCD || TREC.TESTITEMCD || TREC.SCORE_DIV = '990009') ");
        stb.append("     AND TREC.CLASSCD = T3.CLASSCD ");
        stb.append("     AND TREC.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("     AND TREC.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("     AND TREC.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("     AND TREC.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT TRANK ON TRANK.YEAR = TREC.YEAR ");
        stb.append("     AND TRANK.SEMESTER = TREC.SEMESTER ");
        stb.append("     AND TRANK.TESTKINDCD = TREC.TESTKINDCD ");
        stb.append("     AND TRANK.TESTITEMCD = TREC.TESTITEMCD ");
        stb.append("     AND TRANK.SCORE_DIV = TREC.SCORE_DIV ");
        stb.append("     AND TRANK.CLASSCD = TREC.CLASSCD ");
        stb.append("     AND TRANK.SCHOOL_KIND = TREC.SCHOOL_KIND ");
        stb.append("     AND TRANK.CURRICULUM_CD = TREC.CURRICULUM_CD ");
        stb.append("     AND TRANK.SUBCLASSCD = TREC.SUBCLASSCD ");
        stb.append("     AND TRANK.SCHREGNO = TREC.SCHREGNO ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("   T1.GRADE_NAME1, ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.HR_NAME, ");
        stb.append("   T1.HR_CLASS_NAME1, ");
        stb.append("   T1.STAFFNAME, ");
        stb.append("   T1.SUBSTAFFNAME, ");
        stb.append("   T1.ATTENDNO, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.NAME, ");
        stb.append("   CAST(NULL AS VARCHAR(1)) AS CHAIRCD, ");
        stb.append("   CAST(NULL AS VARCHAR(1)) AS CHAIR_STAFFCD, ");
        stb.append("   CAST(NULL AS VARCHAR(1)) AS CHAIR_STAFFNAME, ");
        stb.append("   TRANK.SUBCLASSCD, ");
        stb.append("   CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
        stb.append("   CAST(NULL AS VARCHAR(1)) AS SUBCLASSABBV, ");
//        stb.append("   CAST(NULL AS VARCHAR(1)) AS REQUIRE_FLG, ");
        stb.append("   CAST(NULL AS VARCHAR(1)) AS VALUE_DI, ");
        stb.append("   TRANK.SEMESTER || TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV AS SEM_TESTCD, ");
        stb.append("   TRANK.SCORE, ");
        stb.append("   TRANK.AVG, ");
        stb.append("   TRANK.CLASS_RANK, ");
        stb.append("   TRANK.COURSE_RANK, ");
        stb.append("   TRANK.MAJOR_RANK ");
        stb.append(" FROM REGD T1 ");
        stb.append(" INNER JOIN RECORD_RANK_SDIV_DAT TRANK ON TRANK.YEAR = T1.YEAR ");
        stb.append("     AND TRANK.SEMESTER <= '" + param._semester + "' ");
        stb.append("     AND  (TRANK.SEMESTER <> '9' AND TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV = '990008' ");
        stb.append("        OR TRANK.SEMESTER  = '9' AND TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV = '990009') ");
        stb.append("     AND TRANK.SUBCLASSCD = '" + SUBCLASSCD_999999 + "' ");
        stb.append("     AND TRANK.SCHREGNO = T1.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     ATTENDNO, ");
        stb.append("     SUBCLASSCD ");

        final Map studentMap = new HashMap();
        try {
            final String sql = stb.toString();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                if (null == schregno) {
                    continue;
                }

                if (null == studentMap.get(schregno)) {
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String hrClassName1 = rs.getString("HR_CLASS_NAME1");
                    final String staffname = rs.getString("STAFFNAME");
                    final String substaffname = rs.getString("SUBSTAFFNAME");

                    final String attendno = rs.getString("ATTENDNO");
                    final String name = rs.getString("NAME");

                    final Student student = new Student(grade, hrClass, hrName, hrClassName1, staffname, substaffname, attendno, schregno, name);

                    studentList.add(student);
                    studentMap.put(schregno, student);
                }
                final Student student = (Student) studentMap.get(schregno);

                final String subclasscd = rs.getString("SUBCLASSCD");
                if (null == subclasscd) {
                    continue;
                }

                if (null == student._subclassMap.get(subclasscd)) {
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String subclassabbv = rs.getString("SUBCLASSABBV");
                    final String requireFlg = null; // rs.getString("REQUIRE_FLG");

                    final Subclass subclass = new Subclass(subclasscd, subclassname, subclassabbv, requireFlg);
                    student._subclassMap.put(subclass._subclasscd, subclass);
                }

                final String semtestcd = rs.getString("SEM_TESTCD");

                if (null != semtestcd) {
                    final String score = null != rs.getString("VALUE_DI") ? rs.getString("VALUE_DI") : rs.getString("SCORE");

                    final Score s = new Score(subclasscd, score, rs.getBigDecimal("AVG"), rs.getString("CLASS_RANK"), rs.getString("COURSE_RANK"), rs.getString("MAJOR_RANK"));

                    getMappedMap(student._scoreMap, semtestcd).put(subclasscd, s);
                }

            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        loadAttendance(db2, param, studentMap);
        setCommittee(db2, param, studentMap);
        setClub(db2, param, studentMap);
        setSpecialScore(db2, param, studentMap);
        setRecordTotalstudytime(db2, param, studentMap);
        setRecordTotalstudytimeRemark1(db2, param, studentMap);
        setBehaviorList(db2, param, studentMap);
        ViewRecord.setViewRecordList(db2, param, studentMap);

        return studentList;
    }

    private static void loadAttendance(
            final DB2UDB db2,
            final Param param,
            final Map studentMap
    ) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = AttendAccumulate.getAttendSemesSql(param._year, param._semester, null, param._date, param._attendParamMap);
            //log.debug(" attend sql = " + sql);
            ps = db2.prepareStatement(sql);

            for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final Student student = (Student) e.getValue();

                ps.setString(1, student._schregno);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Map semes = getMappedMap(student._attendMap, rs.getString("SEMESTER"));

                    semes.put("LESSON", rs.getString("LESSON"));
                    semes.put("MLESSON", rs.getString("MLESSON"));
                    semes.put("SUSPEND", rs.getString("SUSPEND"));
                    semes.put("MOURNING", rs.getString("MOURNING"));
                    semes.put("SICK_ONLY", rs.getString("SICK_ONLY"));
                    semes.put("NONOTICE_ONLY", rs.getString("NONOTICE_ONLY"));
                    semes.put("SICK_NONOTICE", add(rs.getString("SICK_ONLY"), rs.getString("NONOTICE_ONLY")));
                    semes.put("PRESENT", rs.getString("PRESENT"));
                    semes.put("LATE", rs.getString("LATE"));
                    semes.put("EARLY", rs.getString("EARLY"));
                    semes.put("TRANSFER_DATE", rs.getString("TRANSFER_DATE"));
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

    /**
     * 数値を加算して文字列（両方nullの場合、null）で返す
     * @param num1
     * @param num2
     * @return
     */
    private static String add(String num1, String num2) {
        if (NumberUtils.isNumber(num2)) {
            if (NumberUtils.isNumber(num1)) {
                num1 = new BigDecimal(num1).add(new BigDecimal(num2)).toString();
            } else {
                num1 = num2;
            }
        }
        return num1;
    }

    private static Integer toInteger(final String v) {
        if (NumberUtils.isDigits(v)) {
            return Integer.valueOf(v);
        }
        return null;
    }

    private static void setCommittee(final DB2UDB db2, final Param param,
            final Map studentMap) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.COMMITTEE_FLG, ");
        stb.append("     T1.COMMITTEECD, ");
        stb.append("     T1.CHARGENAME, ");
        stb.append("     T2.COMMITTEENAME ");
        stb.append(" FROM SCHREG_COMMITTEE_HIST_DAT T1 ");
        stb.append(" LEFT JOIN COMMITTEE_MST T2 ON T2.COMMITTEE_FLG = T1.COMMITTEE_FLG ");
        stb.append("     AND T2.COMMITTEECD = T1.COMMITTEECD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER <> '9' ");
        stb.append("     AND T1.SEMESTER <= '" + param._paramSemester + "' ");
        stb.append("     AND T1.COMMITTEE_FLG IN ('1', '2') ");
        stb.append("     AND T1.SCHREGNO = ? ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.COMMITTEE_FLG, ");
        stb.append("     T1.COMMITTEECD ");
        try {
            ps = db2.prepareStatement(stb.toString());

            for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final Student student = (Student) e.getValue();

                ps.setString(1, student._schregno);
                rs = ps.executeQuery();

                while (rs.next()) {

                    final String semester = rs.getString("SEMESTER");
                    final String committeeFlg = rs.getString("COMMITTEE_FLG");
                    String name = null;
//                    if ("2".equals(committeeFlg)) {
//                        name = rs.getString("CHARGENAME");
//                    } else if ("1".equals(committeeFlg)) {
                        name = rs.getString("COMMITTEENAME");
//                    }
                    if (StringUtils.isBlank(name)) {
                        continue;
                    }
                    getMappedList(getMappedMap(student._semesCommitteeListMap, semester), committeeFlg).add(name);

                    getMappedList(student._debugDataMap, "COMMITTEE").add(rsToMap(rs));
                }
                DbUtils.closeQuietly(rs);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private static void setClub(final DB2UDB db2, final Param param,
            final Map studentMap) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final StringBuffer stb = new StringBuffer();
        stb.append("  ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.CLUBCD, ");
        stb.append("     T2.CLUBNAME, ");
        stb.append("     CASE WHEN T1.SDATE BETWEEN TSEM.SDATE AND TSEM.EDATE OR ");
        stb.append("                       VALUE(T1.EDATE, '9999-12-31') BETWEEN TSEM.SDATE AND TSEM.EDATE OR ");
        stb.append("                       TSEM.SDATE <= T1.SDATE AND T1.EDATE <= TSEM.EDATE OR ");
        stb.append("                       T1.SDATE <= TSEM.SDATE AND TSEM.EDATE <=  VALUE(T1.EDATE, TSEM.EDATE) THEN 1 END AS FLG ");
        stb.append(" FROM SCHREG_CLUB_HIST_DAT T1 ");
        stb.append(" INNER JOIN CLUB_MST T2 ON T2.CLUBCD = T1.CLUBCD ");
        stb.append(" INNER JOIN SEMESTER_MST TSEM ON TSEM.YEAR = '" + param._year + "' ");
        stb.append("     AND TSEM.SEMESTER <> '9' ");
        stb.append("     AND TSEM.SEMESTER <= '" + param._paramSemester + "' ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = ? ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.CLUBCD ");
        try {
            ps = db2.prepareStatement(stb.toString());

            for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final Student student = (Student) e.getValue();

                ps.setString(1, student._schregno);
                rs = ps.executeQuery();

                while (rs.next()) {

                    final String clubname = rs.getString("CLUBNAME");
                    final String flg = rs.getString("FLG");

                    if (!"1".equals(flg) || StringUtils.isBlank(clubname)) {
                        continue;
                    }
                    getMappedList(student._clubListMap, "DUMMY").add(clubname);

                    getMappedList(student._debugDataMap, "CLUB").add(rsToMap(rs));
                }
                DbUtils.closeQuietly(rs);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private static Map rsToMap(final ResultSet rs) throws SQLException {
        final Map map = new TreeMap();
        final ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            final String field = meta.getColumnName(i);
            final String data = rs.getString(field);
            map.put(field, data);
        }
        return map;
    }

    private static void setSpecialScore(final DB2UDB db2, final Param param, final Map studentMap) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String field = "J".equals(param._schoolKind) ? "NMD061.NAME1" : "NMD061.NAME2";

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     " + field + " AS SUBCLASSCD, ");
        stb.append("     SUBM.SUBCLASSNAME, ");
        stb.append("     TSC.SEMESTER,  ");
        stb.append("     TSC.SCHREGNO, ");
        stb.append("     TSC.VALUE, ");
        stb.append("     NMD060.NAME1 AS VALUE_NAME ");
        stb.append(" FROM NAME_MST NMD061 ");
        stb.append(" INNER JOIN NAME_YDAT T2 ON T2.NAMECD1 = NMD061.NAMECD1 ");
        stb.append("     AND T2.NAMECD2 = NMD061.NAMECD2 ");
        stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD || '-' || SUBM.SCHOOL_KIND || '-' || SUBM.CURRICULUM_CD || '-' || SUBM.SUBCLASSCD = " + field + " ");
        stb.append(" LEFT JOIN SPECIALACT_SCORE_DAT TSC ON TSC.YEAR = T2.YEAR ");
        stb.append("     AND TSC.SEMESTER <= '" + param._semester + "' ");
        stb.append("     AND TSC.SCHREGNO = ? ");
        stb.append("     AND TSC.CLASSCD || '-' || TSC.SCHOOL_KIND || '-' || TSC.CURRICULUM_CD || '-' || TSC.SUBCLASSCD = " + field + " ");
        stb.append(" LEFT JOIN NAME_MST NMD060 ON NMD060.NAMECD1 = 'D060' ");
        stb.append("     AND NMD060.NAMECD2 = CHAR(TSC.VALUE) ");
        stb.append(" WHERE ");
        stb.append("     NMD061.NAMECD1 = 'D061' ");
        stb.append("     AND T2.YEAR = '" + param._year + "' ");
        stb.append("     AND " + field + " IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("     NMD061.NAMECD2 ");
        try {
            ps = db2.prepareStatement(stb.toString());

            for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final Student student = (Student) e.getValue();

                ps.setString(1, student._schregno);
                rs = ps.executeQuery();

                List subclasscds = new ArrayList();

                while (rs.next()) {

                    final String subclasscd = rs.getString("SUBCLASSCD");
                    if (!subclasscds.contains(subclasscd)) {
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final Map m = new HashMap();
                        m.put("SUBCLASSCD", subclasscd);
                        m.put("SUBCLASSNAME", subclassname);
                        student._specialScoreSubclassList.add(m);
                        subclasscds.add(subclasscd);
                    }
                    final String semester = rs.getString("SEMESTER");
                    if (null != semester) {
                        final String valueName = rs.getString("VALUE_NAME");
                        getMappedMap(student._semesSpecialScoreMap, subclasscd).put(semester, valueName);
                    }

                    getMappedList(student._debugDataMap, "SPECIALSCORE").add(rsToMap(rs));
                }
                DbUtils.closeQuietly(rs);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private static void setRecordTotalstudytime(final DB2UDB db2, final Param param, final Map studentMap) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.TOTALSTUDYACT ");
        stb.append(" FROM RECORD_TOTALSTUDYTIME_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER = '9' ");
        stb.append("     AND T1.SCHREGNO = ? ");
        stb.append("     AND T1.CLASSCD = '90' ");
        stb.append("     AND T1.SCHOOL_KIND = 'J' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD ");
        try {
            ps = db2.prepareStatement(stb.toString());

            for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final Student student = (Student) e.getValue();

                ps.setString(1, student._schregno);
                rs = ps.executeQuery();

                final StringBuffer totalstudyactTotal = new StringBuffer();

                String newline = "";
                while (rs.next()) {
                    String totalstudyact = rs.getString("TOTALSTUDYACT");
                    if (!StringUtils.isBlank(totalstudyact)) {
                        totalstudyactTotal.append(newline).append(totalstudyact);
                        newline = "\n";
                    }

                }

                student._recordTotalstudyact = totalstudyactTotal.toString();

                DbUtils.closeQuietly(rs);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private static void setRecordTotalstudytimeRemark1(final DB2UDB db2, final Param param, final Map studentMap) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.REMARK1 ");
        stb.append(" FROM ");
        stb.append("     RECORD_TOTALSTUDYTIME_DAT T1 ");
        stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = T1.YEAR ");
        stb.append("         AND N1.NAMECD1 = 'A040' ");
        stb.append("         AND N1.NAMECD2 = '10' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER = '9' ");
        stb.append("     AND T1.SCHREGNO = ? ");
        stb.append("     AND T1.CLASSCD = N1.NAMESPARE1 ");
        stb.append("     AND T1.SCHOOL_KIND = N1.NAMESPARE2 ");
        stb.append(" ORDER BY ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD ");
        try {
            ps = db2.prepareStatement(stb.toString());

            for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final Student student = (Student) e.getValue();

                ps.setString(1, student._schregno);
                rs = ps.executeQuery();

                final StringBuffer remark1Total = new StringBuffer();

                String newline = "";
                while (rs.next()) {
                    String remark1 = rs.getString("REMARK1");
                    if (!StringUtils.isBlank(remark1)) {
                        remark1Total.append(newline).append(remark1);
                        newline = "\n";
                    }
                }

                student._recordRemark1 = remark1Total.toString();

                DbUtils.closeQuietly(rs);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private static void setBehaviorList(final DB2UDB db2, final Param param, final Map studentMap) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.NAMECD2 AS CODE, ");
        stb.append("   T1.NAME1, ");
        stb.append("   T2.RECORD, ");
        stb.append("   CASE WHEN T2.RECORD = '1' THEN '○' END AS RECORD_NAME ");
        stb.append(" FROM V_NAME_MST T1 ");
        stb.append(" LEFT JOIN BEHAVIOR_SEMES_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER = '9' ");
        stb.append("     AND T2.SCHREGNO = ? ");
        stb.append("     AND T2.CODE = T1.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + param._year + "' AND T1.NAMECD1 = 'D035' ");
        stb.append(" ORDER BY ");
        stb.append("   T1.NAMECD2 ");

        try {
            ps = db2.prepareStatement(stb.toString());

            for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final Student student = (Student) e.getValue();

                ps.setString(1, student._schregno);
                rs = ps.executeQuery();

                while (rs.next()) {
                    student._behaviorList.add(rsToMap(rs));
                }
                DbUtils.closeQuietly(rs);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private static class Score {
        final String _subclasscd;
        final String _score;
        final BigDecimal _avg;
        final String _classRank;
        final String _courseRank;
        final String _majorRank;

        Score(
                final String subclasscd,
                final String score,
                final BigDecimal avg,
                final String classRank,
                final String courseRank,
                final String majorRank) {
            _subclasscd = subclasscd;
            _score = score;
            _avg = avg;
            _classRank = classRank;
            _courseRank = courseRank;
            _majorRank = majorRank;
        }
    }

    private static class Subclass implements Comparable {
        final String _subclasscd;
        final String _subclassname;
        final String _subclassabbv;
//        final String _requireFlg;
        public Subclass(final String subclasscd, final String subclassname, final String subclassabbv, final String requireFlg) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
//            _requireFlg = requireFlg;
        }
        public int compareTo(Object o) {
            final Subclass subclass = (Subclass) o;
//            String requireFlg1 = StringUtils.defaultString(_requireFlg, "0");
//            String requireFlg2 = StringUtils.defaultString(subclass._requireFlg, "0");
//            if (!"3".equals(requireFlg1) && "3".equals(requireFlg2)) {
//                return -1;
//            } else if ("3".equals(requireFlg1) && !"3".equals(requireFlg2)) { // 選択科目は後
//                return 1;
//            }
            return _subclasscd.compareTo(subclass._subclasscd);
        }
        private static Subclass getSubclass(final String subclasscd, final List subclassList) {
            for (final Iterator it = subclassList.iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                if (subclasscd.equals(subclass._subclasscd)) {
                    return subclass;
                }
            }
            return null;
        }
    }

    /**
     * 観点の教科
     */
    private static class ViewClass {
        final String _classcd;
        final String _classname;
        final String _subclasscd;
        final String _subclassname;
        final String _electDiv;
        final List _viewList;
        ViewClass(
                final String classcd,
                final String classname,
                final String subclasscd,
                final String subclassname,
                final String electDiv) {
            _classcd = classcd;
            _classname = classname;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _electDiv = electDiv;
            _viewList = new ArrayList();
        }

        public void addView(final String viewcd, final String viewname) {
            _viewList.add(new String[]{viewcd, viewname});
        }

        public String getViewCd(final int i) {
            return ((String[]) _viewList.get(i))[0];
        }

        public String getViewName(final int i) {
            return ((String[]) _viewList.get(i))[1];
        }

        public int getViewSize() {
            return _viewList.size();
        }

        public static List getViewClassList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewClassSql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String electDiv = rs.getString("ELECTDIV");
                    final String viewcd = rs.getString("VIEWCD");
                    final String viewname = rs.getString("VIEWNAME");

                    ViewClass viewClass = null;
                    for (final Iterator it = list.iterator(); it.hasNext();) {
                        final ViewClass viewClass0 = (ViewClass) it.next();
                        if (viewClass0._subclasscd.equals(subclasscd)) {
                            viewClass = viewClass0;
                            break;
                        }
                    }

                    if (null == viewClass) {
                        viewClass = new ViewClass(classcd, classname, subclasscd, subclassname, electDiv);
                        list.add(viewClass);
                    }

                    viewClass.addView(viewcd, viewname);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getViewClassSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE, ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     T3.CLASSNAME, ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     VALUE(T4.SUBCLASSORDERNAME2, T4.SUBCLASSNAME) AS SUBCLASSNAME, ");
            stb.append("     VALUE(T4.ELECTDIV, '0') AS ELECTDIV, ");
            stb.append("     T1.VIEWCD, ");
            stb.append("     T1.VIEWNAME ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("     INNER JOIN CLASS_MST T3 ON T3.CLASSCD = SUBSTR(T1.VIEWCD, 1, 2) ");
            stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T4.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T3.CLASSCD < '90' ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE(T4.ELECTDIV, '0'), ");
            stb.append("     VALUE(T3.SHOWORDER3, -1), ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     VALUE(T1.SHOWORDER, -1), ");
            stb.append("     T1.VIEWCD ");
            return stb.toString();
        }
        public String toString() {
            return _subclasscd + ":" + _subclassname + " " + _electDiv;
        }
    }

    /**
     * 学習の記録（観点）
     */
    private static class ViewRecord {

        final String _semester;
        final String _viewcd;
        final String _status;
        final String _grade;
        final String _subclasscd;
        ViewRecord(
                final String semester,
                final String viewcd,
                final String status,
                final String grade,
                final String subclasscd) {
            _semester = semester;
            _viewcd = viewcd;
            _status = status;
            _grade = grade;
            _subclasscd = subclasscd;
        }

        /**
         * 観点コードの観点のリストを得る
         * @param subclasscd 科目コード
         * @param viewcd 観点コード
         * @return 観点コードの観点のリスト
         */
        public static List getViewList(final List viewRecordList, final String subclasscd, final String viewcd) {
            final List rtn = new ArrayList();
            if (null != viewcd) {
                for (final Iterator it = viewRecordList.iterator(); it.hasNext();) {
                    final ViewRecord viewRecord = (ViewRecord) it.next();
                    if (viewRecord._subclasscd.equals(subclasscd) && viewcd.equals(viewRecord._viewcd)) {
                        rtn.add(viewRecord);
                    }
                }
            }
            return rtn;
        }

        public static void setViewRecordList(final DB2UDB db2, final Param param, final Map studentMap) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getViewRecordSql(param);
                log.debug(" view record sql = "+  sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final Student student = (Student) e.getValue();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {

                        final String semester = rs.getString("SEMESTER");
                        final String viewcd = rs.getString("VIEWCD");
                        final String status;
                        if ("9".equals(semester)) {
                            status = rs.getString("D028NAME1");
                        } else {
                            status = rs.getString("D029NAME1");
                        }
                        final String grade = rs.getString("GRADE");
                        final String subclasscd = rs.getString("SUBCLASSCD");

                        final ViewRecord viewRecord = new ViewRecord(semester, viewcd, status, grade, subclasscd);

                        //log.debug(" view record = " + rsToMap(rs));

                        student._viewRecordList.add(viewRecord);
                    }
                    DbUtils.closeQuietly(rs);
                    db2.commit();
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
            stb.append("     , T1.VIEWCD ");
            stb.append("     , T3.YEAR ");
            stb.append("     , T3.SEMESTER ");
            stb.append("     , T3.SCHREGNO ");
            stb.append("     , NMD028.NAME1 AS D028NAME1 ");
            stb.append("     , NMD029.NAME1 AS D029NAME1 ");
            stb.append("     , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
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
            stb.append("         AND T3.SEMESTER <= '" + param._semester + "' ");
            stb.append("         AND T3.SCHREGNO = ? ");
            stb.append("     LEFT JOIN V_NAME_MST NMD028 ON NMD028.YEAR = '" + param._year + "' ");
            stb.append("         AND NMD028.NAMECD1 = 'D028' ");
            stb.append("         AND NMD028.ABBV1 = T3.STATUS  ");
            stb.append("     LEFT JOIN V_NAME_MST NMD029 ON NMD029.YEAR = '" + param._year + "' ");
            stb.append("         AND NMD029.NAMECD1 = 'D029' ");
            stb.append("         AND NMD029.ABBV1 = T3.STATUS  ");
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T3.YEAR ");
            stb.append("     , T3.SEMESTER ");
            return stb.toString();
        }
    }

    private static class Student {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrClassName1;
        final String _staffname;
        final String _substaffname;
        final String _attendno;
        final String _schregno;
        final String _name;
        final Map _subclassMap = new HashMap();
        final Map _scoreMap = new HashMap();
        final Map _chairStfMap = new HashMap();
        final Map _attendMap = new HashMap();
        final Map _semesCommitteeListMap = new HashMap();
        final Map _clubListMap = new HashMap();
        final List _specialScoreSubclassList = new ArrayList();
        final Map _semesSpecialScoreMap = new HashMap();
        final List _behaviorList = new ArrayList();
        final List _viewRecordList = new ArrayList();
        String _recordTotalstudyact;
        String _recordRemark1;

        final Map _debugDataMap = new HashMap();

        Student(
            final String grade,
            final String hrClass,
            final String hrName,
            final String hrClassName1,
            final String staffname,
            final String substaffname,
            final String attendno,
            final String schregno,
            final String name
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrClassName1 = hrClassName1;
            _staffname = staffname;
            _substaffname = substaffname;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
        }

        public String getChairStaffname(final String subclasscd) {
            final Map chaircdChairStaffMap = (Map) getMappedMap(_chairStfMap, subclasscd);
            if (chaircdChairStaffMap.isEmpty()) {
                return null;
            }
            final Object minChaircd = new TreeMap(chaircdChairStaffMap).firstKey();
            final Map chairStaffMap = (Map) chaircdChairStaffMap.get(minChaircd);
            if (null == chairStaffMap || chairStaffMap.isEmpty()) {
                return null;
            }
            final Object minChairStaffcd = new TreeMap(chairStaffMap).firstKey();
            return (String) chairStaffMap.get(minChairStaffcd);

        }
    }

    private static class Testitem {
        final String _semester;
        final String _testcd;
        final String _semestername;
        final String _testitemname;
        public Testitem(final String semester, final String testcd, final String semestername, final String testitemname) {
            _semester = semester;
            _testcd = testcd;
            _testitemname = testitemname;
            _semestername = semestername;
        }

        public static Testitem getTestItem(final String semTestcd, final List testitemList) {
            for (final Iterator it = testitemList.iterator(); it.hasNext();) {
                final Testitem ti = (Testitem) it.next();
                if ((ti._semester + ti._testcd).equals(semTestcd)) {
                    return ti;
                }
            }
            return null;
        }

        public static List getTestitemList(final DB2UDB db2, final Param param) {
            String sql = "";
            sql += " SELECT T1.SEMESTER, T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, T2.SEMESTERNAME, T1.TESTITEMNAME ";
            sql += " FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
            sql += " INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ";
            sql += " WHERE ";
            sql += "   T1.YEAR = '" + param._year + "' ";
            sql += "   AND T1.TESTKINDCD = '99' ";
            sql += " ORDER BY T1.SEMESTER, T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV ";
            Map cdMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String testcd = rs.getString("TESTCD");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String testitemname = rs.getString("TESTITEMNAME");
                    final Testitem testitem = new Testitem(semester, testcd, semestername, testitemname);
                    cdMap.put(semester + testcd, testitem);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            final String[] cds = {"1990008", "2990008", "9990009"};
            List rtn = new ArrayList();
            for (int i = 0; i < cds.length; i++) {
                Testitem testitem = (Testitem) cdMap.get(cds[i]);
                if (null == testitem) {
                    testitem = new Testitem(cds[i].substring(0, 1), cds[i].substring(1), null, null);
                }
                rtn.add(testitem);
            }
            return rtn;
        }
    }

    private static class ShokenSize {
        int _mojisu;
        int _gyo;

        ShokenSize(final int mojisu, final int gyo) {
            _mojisu = mojisu;
            _gyo = gyo;
        }

        private static ShokenSize getShokenSize(final String paramString, final int mojisuDefault, final int gyoDefault) {
            final int mojisu = ShokenSize.getParamSizeNum(paramString, 0);
            final int gyo = ShokenSize.getParamSizeNum(paramString, 1);
            if (-1 == mojisu || -1 == gyo) {
                return new ShokenSize(mojisuDefault, gyoDefault);
            }
            return new ShokenSize(mojisu, gyo);
        }

        /**
         * "[w] * [h]"サイズタイプのパラメータのwもしくはhを整数で返す
         * @param param サイズタイプのパラメータ文字列
         * @param pos split後のインデクス (0:w, 1:h)
         * @return "[w] * [h]"サイズタイプのパラメータのwもしくはhの整数値
         */
        private static int getParamSizeNum(final String param, final int pos) {
            int num = -1;
            if (StringUtils.isBlank(param)) {
                return num;
            }
            final String[] nums = StringUtils.split(StringUtils.replace(param, "+", " "), " * ");
            if (StringUtils.isBlank(param) || !(0 <= pos && pos < nums.length)) {
                num = -1;
            } else {
                try {
                    num = Integer.valueOf(nums[pos]).intValue();
                } catch (Exception e) {
                    log.error("Exception!", e);
                }
            }
            return num;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 72606 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _paramSemester;
        final String _ctrlSemester;
        final String _grade;
        final String _gradeHrclass;
        final String[] _schregSelected;
        final String _loginDate;
        final String _date;
        final String _documentRoot;
        final String _imagePath;
        final String _extension;
        final String _outputDate;
        final String _schoolStampImagePath;

        final List _testitemList;
        final String _gradeCdStr;

        final String _semestername;
        final String _schoolKind;
        final String _RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_J;
        final String _RECORD_TOTALSTUDYTIME_DAT_REMARK1_SIZE_J;

        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス
        private Map _semesterNameMap;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;
        private String _certifSchoolHrJobName;

        private final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _paramSemester = request.getParameter("SEMESTER");
            _definecode = createDefineCode(db2);
            if (String.valueOf(_definecode.semesdiv).equals(_paramSemester)) {
                _semester = "9"; // 最終学期は学年末
            } else {
                _semester = _paramSemester;
            }
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _schregSelected = request.getParameterValues("SCHREG_SELECTED");
            _loginDate = request.getParameter("LOGIN_DATE");
            _date = request.getParameter("DATE").replace('/', '-');
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _imagePath = request.getParameter("IMAGEPATH");
            _RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_J = request.getParameter("RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_J");
            _RECORD_TOTALSTUDYTIME_DAT_REMARK1_SIZE_J = request.getParameter("RECORD_TOTALSTUDYTIME_DAT_REMARK1_SIZE_J");

            final KNJ_Control.ReturnVal returnval = getDocumentroot(db2);
            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子
            _schoolStampImagePath = getImagePath();
            log.debug(" schoolStampImagePath = " + _schoolStampImagePath);
            _outputDate = request.getParameter("OUTPUT_DATE");

            _semestername = getSemestername(db2, _paramSemester);
            _schoolKind = getSchoolKind(db2);
            _testitemList = Testitem.getTestitemList(db2, this);

            loadSemester(db2, _year);
            setCertifSchoolDat(db2);
            _gradeCdStr = getGradeCd(db2, _grade);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _gradeHrclass.substring(0, 2));
            _attendParamMap.put("hrClass", _gradeHrclass.substring(2));
            _attendParamMap.put("schregno", "?");
            _attendParamMap.put("useCurriculumcd", "1");
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("absenceDiv", "2");
        }

        public String getImagePath() {
            final String path = _documentRoot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + "SCHOOLSTAMP_J." + _extension;
            if (new java.io.File(path).exists()) {
                return path;
            }
            log.warn(" path not exists: " + path);
            return null;
        }

        /**
         * 写真データ格納フォルダの取得 --NO001
         */
        private KNJ_Control.ReturnVal getDocumentroot(final DB2UDB db2) {
            KNJ_Control.ReturnVal returnval = null;
            try {
                KNJ_Control imagepath_extension = new KNJ_Control(); // 取得クラスのインスタンス作成
                returnval = imagepath_extension.Control(db2);
            } catch (Exception ex) {
                log.error("getDocumentroot error!", ex);
            }
            return returnval;
        }

        public String getRegdSemester() {
            return "9".equals(_semester) ? _ctrlSemester : _semester;
        }

        /** 作成日 */
        public String getNow(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(KNJ_EditDate.gengou(db2, Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日H時m分");
            stb.append(sdf.format(date));
            return stb.toString();
        }

        private String getSchoolKind(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT ";
            sql += " WHERE ";
            sql += "   YEAR = '" + _year + "' ";
            sql += "   AND GRADE = '" + _grade + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOL_KIND");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private String getSemestername(final DB2UDB db2, final String semester) {
            String sql = "";
            sql += " SELECT SEMESTERNAME FROM SEMESTER_MST ";
            sql += " WHERE ";
            sql += "   YEAR = '" + _year + "' ";
            sql += "   AND SEMESTER = '" + semester + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(
                final DB2UDB db2
        ) {
            final KNJDefineSchool definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _year);         //各学校における定数等設定
            log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);
            return definecode;
        }

        /**
         * 年度の開始日を取得する
         */
        private void loadSemester(final DB2UDB db2, String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _semesterNameMap = new HashMap();
            try {
                ps = db2.prepareStatement(sqlSemester(year));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    _semesterNameMap.put(semester, name);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sqlSemester(String year) {
            final String sql;
            sql = "select"
                + "   SEMESTER,"
                + "   SEMESTERNAME,"
                + "   SDATE"
                + " from"
                + "   SEMESTER_MST"
                + " where"
                + "   YEAR='" + year + "'"
                + " order by SEMESTER"
            ;
            return sql;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '103' ");
            log.debug("certif_school_dat sql = " + sql.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _certifSchoolSchoolName = rs.getString("SCHOOL_NAME");
                    _certifSchoolJobName = rs.getString("JOB_NAME");
                    _certifSchoolPrincipalName = rs.getString("PRINCIPAL_NAME");
                    _certifSchoolHrJobName = rs.getString("REMARK2");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            _certifSchoolSchoolName = StringUtils.defaultString(_certifSchoolSchoolName);
            _certifSchoolJobName = StringUtils.defaultString(_certifSchoolJobName, "学校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(_certifSchoolPrincipalName);
            _certifSchoolHrJobName = StringUtils.defaultString(_certifSchoolHrJobName, "担任");
        }

        private static String hankakuToZenkaku(final String str) {
            if (null == str) {
                return null;
            }
            final String[] nums = new String[]{"０", "１", "２", "３", "４", "５", "６", "７", "８", "９"};
            final StringBuffer stb = new StringBuffer();
            for (int i = 0; i < str.length(); i++) {
                final String s = String.valueOf(str.charAt(i));
                if (NumberUtils.isDigits(s)) {
                    final int j = Integer.parseInt(s);
                    stb.append(nums[j]);
                } else {
                    stb.append(s);
                }
            }
            return stb.toString();
        }

        private String getGradeCd(final DB2UDB db2, final String grade) {
            String gradeCd = "　";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_GDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.GRADE = '" + grade + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String tmp = rs.getString("GRADE_CD");
                    if (NumberUtils.isDigits(tmp)) {
                        gradeCd = hankakuToZenkaku(String.valueOf(Integer.parseInt(tmp)));
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradeCd;
        }
    }
}

// eof

