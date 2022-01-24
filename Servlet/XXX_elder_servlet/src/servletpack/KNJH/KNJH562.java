// kanji=漢字
/*
 * $Id: d11e68c9383dd3ced476dc46d556fbed6b34b0c2 $
 *
 * 作成日: 2012/03/06 14:04:42 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.math.BigDecimal;
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
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: d11e68c9383dd3ced476dc46d556fbed6b34b0c2 $
 */
public class KNJH562 {

    private static final Log log = LogFactory.getLog("KNJH562.class");

    private boolean _hasData;
    private static final String RANK_KANSAN = "03";
    private static final String RANK_GOUKEI = "01";
    private static final String RANK_GRADE = "01";
    private static final String AVG_GRADE = "01";
    private static final String AVG_HR = "02";
    private static final String AVG_COURSE = "03";
    private static final String AVG_MAJOR= "04";
    private static final String AVG_COURSEGROUP = "05";
    private static final String AVG_DATA_SCORE = "1";
    private static final int MAX_RETU1 = 6;
    private static final int MAX_RETU2 = 8;
    private static final String GROUP_GRADE = "1";
    private static final String GROUP_HR = "2";
    private static final String GROUP_COURSE = "3";
    private static final String GROUP_MAJOR = "4";
    private static final String GROUP_COURSEGROUP = "5";
    private static final String FROM_TO_MARK = "\uFF5E";
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

            printMain(svf, db2);

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

    private void printMain(final Vrw32alp svf, DB2UDB db2) throws SQLException {
        final List groupList = Group.getGroupList(db2, _param);
        log.debug(" groupList = " + groupList);
        for (final Iterator it = groupList.iterator(); it.hasNext();) {
            final Group group = (Group) it.next();

            for (int i = 0; i < _param._categorySelected.length; i++) {
                final ProficiencySubclass proficiencySubclass = new ProficiencySubclass(db2, _param._categorySelected[i], group);

                final List printHrList = HrClassData.getPrintHrList(db2, _param, proficiencySubclass, group);

                for (final Iterator itHr = printHrList.iterator(); itHr.hasNext();) {
                    final HrClassData hrClassData = (HrClassData) itHr.next();
                    if (!hrClassData.hasData()) {
                        itHr.remove();
                    }
                }
                if (printHrList.size() == 0) {
                    // 点数データがあるクラスが1件もない科目は出力しない
                    log.debug(" no data");
                    continue;
                }

                printSubclass(svf, printHrList, proficiencySubclass, group._name);
            }
        }
    }

    private static String setScale(final String avg, final int scale) {
        if (null == avg) {
            return null;
        }
        return new BigDecimal(avg).setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private List getPageList(final List hrList) {
        final List pageList = new ArrayList();
        List current = null;
        if (hrList.size() <= MAX_RETU1) {
            current = new ArrayList(hrList);
            pageList.add(current);
            return pageList;
        }
        for (final Iterator it = hrList.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= MAX_RETU2) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(o);
        }
        if (current.size() > MAX_RETU1) {
            current = new ArrayList();
            pageList.add(current); // 最後のページに分布表を表示するため空リストを追加
        }
        return pageList;
    }

    private void printSubclass(final Vrw32alp svf, final List printHrListAll, final ProficiencySubclass proficiencySubclass, final String groupname) throws SQLException {
        final int distPerfect;
        final int kizami;

        if ("1".equals(_param._knjh562PrintDisritubtionScore)) {
            distPerfect = Integer.parseInt(proficiencySubclass._highPerfect);
            if (20 < distPerfect / 10) {
                // lines = 20;
                kizami = 10;
            } else { // if (distPerfect / 10 <= 20) {
                if (20 < distPerfect / 5) {
                    // lines = 20;
                    kizami = 10;
                } else if (10 < distPerfect / 5) {
                    // lines = 20;
                    kizami = 5;
                } else { // distPerfect / 5 <= 10
                    // lines = 10;
                    kizami = 5;
                }
            }
        } else {
            distPerfect = 100;
            final int lines = _param._isMiyagi ? 20 : 10;
            kizami = distPerfect / lines;
        }
        final List bunpuList = new ArrayList();
        bunpuList.add(new Bunpu(distPerfect, distPerfect));
        for (int low = distPerfect - kizami; low >= 0; low-= kizami) {
            final int high = low + kizami - 1;
            bunpuList.add(new Bunpu(low, high));
        }

        final List pageList = getPageList(printHrListAll);

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List printHrList = (List) pageList.get(pi);
            final boolean isLastPage = pageList.size() - 1 == pi;
            final String form;
            if (isLastPage) {
                if (bunpuList.size() > 11) {
                    // 20行
                	if(printHrListAll.size() > 6) {
                		form = "KNJH562_4.frm";
                	}else {
                		form = "KNJH562_3.frm";
                	}
                } else {
                	// 10行
                	form = "KNJH562.frm";
                }
            } else {
                form = "KNJH562_2.frm";
            }

            svf.VrSetForm(form, 1);

            final int DANSHI = 1;
            final int JYOSHI = 2;
            final int GOUKEI = 3;
            final int ZENTAI = "KNJH562_4.frm".equals(form) ? 14 : 9;

            svf.VrsOut("TITLE", _param._title); /* 年度 テスト名称 */
            svf.VrsOut("GRADE", groupname); /* 学年・コース・グループ */
            svf.VrsOut("SUBJECT", proficiencySubclass._proficiencySubclassName); /* 科目名称 */
            svf.VrsOut("PERFECT", proficiencySubclass.getPerfectString()); /* 満点 */

            for (int hri = 0, hrmax = printHrListAll.size(); hri < hrmax; hri++) {
                final HrClassData hrClassData = (HrClassData) printHrListAll.get(hri);
                final AvgDat gradeAvgDat = (AvgDat) hrClassData._avgDatMap.get("GRADE");
                if (null != gradeAvgDat) {
                    //分布の合計欄
                    svf.VrsOut("GRADE_AVERAGE", setScale(gradeAvgDat._avg, 1));
                    break;
                }
            }

            for (int hri = 0, retumax = printHrList.size(); hri < retumax; hri++) {
                final HrClassData hrClassData = (HrClassData) printHrList.get(hri);
                final int retu = hri + 1;
                //基本部分印字
                svf.VrsOutn("CLASS1", retu, hrClassData._hrName);
                final AvgDat hrAvgDat = (AvgDat) hrClassData._avgDatMap.get("HR_CLASS");
                if (null != hrAvgDat) {
                    svf.VrsOutn("TOTAL_POINT1", retu, hrAvgDat._score);
                    svf.VrsOutn("TOTAL_NUM1", retu, hrAvgDat._cnt);
                    svf.VrsOutn("TOTAL_AVERAGE1", retu, setScale(hrAvgDat._avg, 1));
                }

                //生徒データ印字
                for (int i = 0, stmax = hrClassData._students.size(); i < stmax; i++) {
                    final Student student = (Student) hrClassData._students.get(i);
                    final int stLine = i + 1;
                    svf.VrsOutn("ATTENDNO" + stLine, retu, student._attendNo);
                    svf.VrsOutn("NAME" + stLine, retu, student._name);
                    svf.VrsOutn("SCORE" + stLine, retu, student._score);
                }
                _hasData = true;
            }
            if (isLastPage && _hasData) {
                if ("1".equals(_param._knjh562PrintDisritubtionScore)) {
                    for (int i = 0; i < bunpuList.size(); i++) {
                        final Bunpu bunpu = (Bunpu) bunpuList.get(i);
                        final String bunpuScore;
                        if (bunpu._high == bunpu._low) {
                            bunpuScore = String.valueOf(bunpu._high);
                        } else {
                            final String high = (bunpu._high < 10 ? "  " : bunpu._high < 100 ? " " : "") + String.valueOf(bunpu._high);
                            final String low = (bunpu._low < 10 ? "  " : bunpu._low < 100 ? " " : "") + String.valueOf(bunpu._low);
                            bunpuScore = high + FROM_TO_MARK + low;
                        }
                        svf.VrsOut("DISTSCOREHR" + String.valueOf(i), bunpuScore);
                    }
                    for (int i = bunpuList.size(); i <= 20; i++) {
                        svf.VrsOut("DISTSCOREHR" + String.valueOf(i), "　");
                    }
                }

                svf.VrsOutn("CLASS3", DANSHI, "男子");
                svf.VrsOutn("CLASS3", JYOSHI, "女子");
                svf.VrsOutn("CLASS3", GOUKEI, "学年");
                svf.VrsOutn("CLASS2", ZENTAI, "全体");

                //クラス分布合計印字
                for (int i = 0, max = bunpuList.size(); i < max; i++) {
                    final Bunpu bunpu = (Bunpu) bunpuList.get(i);
                    final int hrBunpuLine = i + 1;
                    final Stat total = new Stat();
                    for (int hri = 0, hrmax = printHrListAll.size(); hri < hrmax; hri++) {
                        final HrClassData hrClassData = (HrClassData) printHrListAll.get(hri);
                        final int retu = hri + 1;
                        svf.VrsOutn("CLASS2", retu, hrClassData._hrClassName);
                        final Stat bunpuCnt = hrClassData.getBunpuCnt(bunpu._low, bunpu._high, _param);
                        svf.VrsOutn("CLASS_NUM" + hrBunpuLine, retu, String.valueOf(bunpuCnt._total.size()));
                        total.addAll(bunpuCnt);
                    }
                    svf.VrsOutn("CLASS_NUM" + hrBunpuLine, ZENTAI, String.valueOf(total._total.size()));
                }

                for (int hri = 0, hrmax = printHrListAll.size(); hri < hrmax; hri++) {
                    final HrClassData hrClassData = (HrClassData) printHrListAll.get(hri);
                    final int retu = hri + 1;

                    final AvgDat hrAvgDat = (AvgDat) hrClassData._avgDatMap.get("HR_CLASS");
                    if (null != hrAvgDat) {
                        svf.VrsOutn("TOTAL_NUM2", retu, hrAvgDat._cnt);
                        final String avg;
                        if ("1".equals(_param._knjh562PrintDisritubtionScore)) {
                            avg = hrAvgDat._avg;
                        } else {
                            avg = hrAvgDat._avgKansan;
                        }
                        svf.VrsOutn("TOTAL_AVERAGE2", retu, setScale(avg, 0));
                    }

                    final AvgDat gradeAvgDat = (AvgDat) hrClassData._avgDatMap.get("GRADE");
                    if (null != gradeAvgDat) {
                        //分布の合計欄
                        svf.VrsOutn("TOTAL_POINT2", GOUKEI, gradeAvgDat._scoreKansan);
                        svf.VrsOutn("TOTAL_POINT3", GOUKEI, gradeAvgDat._scoreKansan);

                        svf.VrsOutn("TOTAL_NUM2", ZENTAI, gradeAvgDat._cnt);
                        svf.VrsOutn("TOTAL_NUM3", GOUKEI, gradeAvgDat._cnt);
                        final String avg;
                        if ("1".equals(_param._knjh562PrintDisritubtionScore)) {
                            avg = gradeAvgDat._avg;
                        } else {
                            avg = gradeAvgDat._avgKansan;
                        }
                        svf.VrsOutn("TOTAL_AVERAGE2", ZENTAI, setScale(avg, 0));
                        svf.VrsOutn("TOTAL_AVERAGE3", GOUKEI, setScale(avg, 1));
                    }
                }

                //男女分布印字
                final Stat total = new Stat();
                for (int i = 0, max = bunpuList.size(); i < max; i++) {
                    final Bunpu bunpu = (Bunpu) bunpuList.get(i);
                    final String hrBunpuLine = String.valueOf(i + 1);

                    final Stat danjo = new Stat();
                    for (final Iterator itHr = printHrListAll.iterator(); itHr.hasNext();) {
                        final HrClassData hrClassData = (HrClassData) itHr.next();
                        final Stat bunpuCnt = hrClassData.getBunpuCnt(bunpu._low, bunpu._high, _param);
                        danjo.addAll(bunpuCnt);
                        total.addAll(bunpuCnt);
                    }
                    svf.VrsOutn("FIELD" + hrBunpuLine, DANSHI, String.valueOf(danjo._danshi.size()));
                    svf.VrsOutn("FIELD" + hrBunpuLine, JYOSHI, String.valueOf(danjo._jyoshi.size()));
                    svf.VrsOutn("FIELD" + hrBunpuLine, GOUKEI, String.valueOf(danjo._total.size()));
                }
                svf.VrsOutn("TOTAL_POINT3", DANSHI, String.valueOf(Stat.sum(total._danshi)));
                svf.VrsOutn("TOTAL_POINT3", JYOSHI, String.valueOf(Stat.sum(total._jyoshi)));
                svf.VrsOutn("TOTAL_NUM3", DANSHI, String.valueOf(total._danshi.size()));
                svf.VrsOutn("TOTAL_NUM3", JYOSHI, String.valueOf(total._jyoshi.size()));
                svf.VrsOutn("TOTAL_AVERAGE3", DANSHI, Stat.avgStr(total._danshi));
                svf.VrsOutn("TOTAL_AVERAGE3", JYOSHI, Stat.avgStr(total._jyoshi));
            }
            svf.VrEndPage();
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }


    private static class Bunpu {
        final int _low;
        final int _high;
        Bunpu(final int low, final int max) {
            _low = low;
            _high = max;
        }
    }

    private static class Stat {
        final List _danshi = new ArrayList();
        final List _jyoshi = new ArrayList();
        final List _total = new ArrayList();
        public void addAll(Stat statDj) {
            _danshi.addAll(statDj._danshi);
            _jyoshi.addAll(statDj._jyoshi);
            _total.addAll(statDj._total);
        }

        public static String avgStr(List scoreList) {
            if (sum(scoreList) <= 0 || scoreList.size() == 0) {
                return null;
            }
            return new BigDecimal(sum(scoreList)).divide(new BigDecimal(scoreList.size()), 1, BigDecimal.ROUND_HALF_UP).toString();
        }
        public static int sum(List scoreList) {
            int sum = 0;
            for (int i = 0; i < scoreList.size(); i++) {
                final Integer score = (Integer) scoreList.get(i);
                sum += score.intValue();
            }
            return sum;
        }
    }

    private static class HrClassData {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrClassName;
        private List _students;
        private Map _avgDatMap;

        public HrClassData(
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrClassName
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrClassName = hrClassName;
        }

        public void load(
                final Param param,
                final DB2UDB db2,
                final ProficiencySubclass proficiencySubclaas,
                final Group group
                ) throws SQLException  {
            _avgDatMap = AvgDat.getAvgDat(db2, param, _grade, _hrClass, proficiencySubclaas, group);
            _students = HrClassData.getStudents(db2, param, _grade, _hrClass, proficiencySubclaas, group);
        }

        public Stat getBunpuCnt(final int lowScore, final int highScore, final Param param) {
            Stat sta1 = new Stat();
            for (final Iterator iter = _students.iterator(); iter.hasNext();) {
                final Student student = (Student) iter.next();
                final String sscore;
                if ("1".equals(param._knjh562PrintDisritubtionScore)) {
                    sscore = student._score;
                } else {
                    sscore = student._scoreKansan;
                }
                if (null != sscore) {
                    final int score = Integer.parseInt(sscore);
                    if (lowScore <= score && score <= highScore) {
                        if ("1".equals(student._sex)) {
                            sta1._danshi.add(new Integer(score));
                        } else if ("2".equals(student._sex)) {
                            sta1._jyoshi.add(new Integer(score));
                        }
                        sta1._total.add(new Integer(score));
                    }
                }
            }
            return sta1;
        }

        private static List getPrintHrList(final DB2UDB db2, final Param param, final ProficiencySubclass proficiencySubclaas, final Group group) throws SQLException {
            final List retHr = new ArrayList();
            final String HrSql = getHrSql(param, group);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(HrSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String hrClassName = rs.getString("HR_CLASS_NAME1");
                    final HrClassData hrClassData = new HrClassData(grade, hrClass, hrName, hrClassName);
                    retHr.add(hrClassData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            try {
                for (final Iterator it = retHr.iterator(); it.hasNext();) {
                    final HrClassData hrClassData = (HrClassData) it.next();
                    hrClassData.load(param, db2, proficiencySubclaas, group);
                }
            } finally {
                db2.commit();
            }
            return retHr;
        }

        private static String getHrSql(final Param param, final Group group) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T0.GRADE, ");
            stb.append("     T0.HR_CLASS, ");
            stb.append("     T0.HR_NAME, ");
            stb.append("     T0.HR_CLASS_NAME1 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T0 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T1 ");
            stb.append("         ON  T1.YEAR = T0.YEAR ");
            stb.append("         AND T1.SEMESTER = T0.SEMESTER ");
            stb.append("         AND T1.GRADE = T0.GRADE ");
            stb.append("         AND T1.HR_CLASS = T0.HR_CLASS ");
            if (GROUP_COURSEGROUP.equals(param._formGroupDiv)) {
                stb.append("     INNER JOIN COURSE_GROUP_CD_DAT T2 ");
                stb.append("         ON  T2.YEAR = T1.YEAR ");
                stb.append("         AND T2.GRADE = T1.GRADE ");
                stb.append("         AND T2.COURSECD = T1.COURSECD ");
                stb.append("         AND T2.MAJORCD = T1.MAJORCD ");
                stb.append("         AND T2.COURSECODE = T1.COURSECODE ");
                stb.append("        AND T2.GROUP_CD = '" + group._code + "' ");
            }
            stb.append(" WHERE ");
            stb.append("     T0.YEAR = '" + param._year + "' ");
            stb.append("     AND T0.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T0.GRADE = '" + param._grade + "' ");
            if (GROUP_HR.equals(param._formGroupDiv)) {
                stb.append("        AND T0.HR_CLASS = '" + group._code + "' ");
            } else if (GROUP_MAJOR.equals(param._formGroupDiv)) {
                stb.append("        AND T1.COURSECD || T1.MAJORCD = '" + group._code + "' ");
            } else if (GROUP_COURSE.equals(param._formGroupDiv)) {
                stb.append("        AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '" + group._code + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T0.GRADE, ");
            stb.append("     T0.HR_CLASS ");

            return stb.toString();
        }

        private static List getStudents(
                final DB2UDB db2,
                final Param param,
                final String grade,
                final String hrClass,
                final ProficiencySubclass proficiencySubclaas,
                final Group group
        ) throws SQLException {
            final List retStudent = new ArrayList();
            final String studentSql = getStudentsSql(param, grade, hrClass, proficiencySubclaas, group);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(studentSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregNo = rs.getString("SCHREGNO");
                    final String attendNo = rs.getString("ATTENDNO");
                    final String name = rs.getString("NAME");
                    final String sex = rs.getString("SEX");
                    final String score = rs.getString("SCORE");
                    final String scoreKansan = rs.getString("SCORE_KANSAN");
                    final String courseCd = rs.getString("COURSECD");
                    final String majorCd = rs.getString("MAJORCD");
                    final String courseCode = rs.getString("COURSECODE");
                    final Student student = new Student(schregNo, attendNo, name, sex, score, scoreKansan, courseCd, majorCd, courseCode);
                    retStudent.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStudent;
        }

        private static String getStudentsSql(final Param param, final String grade, final String hrClass, final ProficiencySubclass proficiencySubclaas, final Group group) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.SEX, ");
            stb.append("     L1.SCORE, ");
            stb.append("     L2.SCORE AS SCORE_KANSAN, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.COURSECODE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND T1.SEMESTER = L1.SEMESTER ");
            stb.append("          AND L1.PROFICIENCYDIV = '" + param._proficiencyDiv + "' ");
            stb.append("          AND L1.PROFICIENCYCD = '" + param._proficiencyCd + "' ");
            stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("          AND L1.PROFICIENCY_SUBCLASS_CD = '" + proficiencySubclaas._proficiencySubclassCd + "' ");
            stb.append("          AND L1.RANK_DATA_DIV = '" + RANK_GOUKEI + "' ");
            stb.append("          AND L1.RANK_DIV = '" + RANK_GRADE + "' ");
            stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT L2 ON T1.YEAR = L2.YEAR ");
            stb.append("          AND T1.SEMESTER = L2.SEMESTER ");
            stb.append("          AND L2.PROFICIENCYDIV = '" + param._proficiencyDiv + "' ");
            stb.append("          AND L2.PROFICIENCYCD = '" + param._proficiencyCd + "' ");
            stb.append("          AND T1.SCHREGNO = L2.SCHREGNO ");
            stb.append("          AND L2.PROFICIENCY_SUBCLASS_CD = '" + proficiencySubclaas._proficiencySubclassCd + "' ");
            stb.append("          AND L2.RANK_DATA_DIV = '" + RANK_KANSAN + "' ");
            stb.append("          AND L2.RANK_DIV = '" + RANK_GRADE + "' ");
            stb.append("     LEFT JOIN COURSE_GROUP_CD_DAT T2 ");
            stb.append("         ON  T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("         AND T2.COURSECD = T1.COURSECD ");
            stb.append("         AND T2.MAJORCD = T1.MAJORCD ");
            stb.append("         AND T2.COURSECODE = T1.COURSECODE ");
            if (GROUP_HR.equals(param._formGroupDiv)) {
                stb.append("        AND T1.HR_CLASS = '" + group._code + "' ");
            } else if (GROUP_COURSE.equals(param._formGroupDiv)) {
                stb.append("        AND T2.COURSECD || T2.MAJORCD || T2.COURSECODE = '" + group._code + "' ");
            } else if (GROUP_MAJOR.equals(param._formGroupDiv)) {
                stb.append("        AND T2.COURSECD || T2.MAJORCD = '" + group._code + "' ");
            } else if (GROUP_COURSEGROUP.equals(param._formGroupDiv)) {
                stb.append("        AND T2.GROUP_CD = '" + group._code + "' ");
            } else {
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.ATTENDNO ");

            return stb.toString();
        }

        private boolean hasData() {
            for (final Iterator iter = _students.iterator(); iter.hasNext();) {
                final Student student = (Student) iter.next();
                if (null != student._score) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class AvgDat {
        final String _score;
        final String _scoreKansan;
        final String _cnt;
        final String _avg;
        final String _avgKansan;

        public AvgDat(
                final String score,
                final String scoreKansan,
                final String cnt,
                final String avg,
                final String avgKansan
        ) {
            _score = score;
            _scoreKansan = scoreKansan;
            _cnt = cnt;
            _avg = avg;
            _avgKansan = avgKansan;
        }

        private static Map getAvgDat(
                final DB2UDB db2,
                final Param param,
                final String grade,
                final String hrClass,
                final ProficiencySubclass proficiencySubclaas,
                final Group group
        ) {
            final Map retAvgMap = new HashMap();
            final String avgSql = getAvgSql(param, grade, hrClass, proficiencySubclaas, group);
            log.debug(" avgSql = " + avgSql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(avgSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String score = rs.getString("SCORE");
                    final String scoreKansan = rs.getString("SCORE_KANSAN");
                    final String cnt = rs.getString("COUNT");
                    final String avg = rs.getString("AVG");
                    final String avgKansan = rs.getString("AVG_KANSAN");
                    final AvgDat avgDat = new AvgDat(score, scoreKansan, cnt, avg, avgKansan);
                    final String div = rs.getString("DIV");
                    retAvgMap.put(div, avgDat);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retAvgMap;
        }

        private static String getAvgSql(final Param param, final String grade, final String hrClass, final ProficiencySubclass proficiencySubclaas, final Group group) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     'GRADE' AS DIV, ");
            stb.append("     SCORE, ");
            stb.append("     SCORE_KANSAN, ");
            stb.append("     COUNT, ");
            stb.append("     AVG, ");
            stb.append("     AVG_KANSAN ");
            stb.append(" FROM ");
            stb.append("     PROFICIENCY_AVERAGE_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER = '" + param._semester + "' ");
            stb.append("     AND PROFICIENCYDIV = '" + param._proficiencyDiv + "' ");
            stb.append("     AND PROFICIENCYCD = '" + param._proficiencyCd + "' ");
            stb.append("     AND PROFICIENCY_SUBCLASS_CD = '" + proficiencySubclaas._proficiencySubclassCd + "' ");
            stb.append("     AND DATA_DIV = '" + AVG_DATA_SCORE + "' ");
            stb.append("     AND GRADE = '" + param._grade + "' ");
            if (GROUP_GRADE.equals(param._formGroupDiv)) {
                stb.append("     AND AVG_DIV = '" + AVG_GRADE + "' ");
            } else if (GROUP_HR.equals(param._formGroupDiv)) {
                stb.append("     AND AVG_DIV = '" + AVG_HR + "' ");
                stb.append("     AND HR_CLASS = '" + group._code + "' ");
            } else if (GROUP_COURSE.equals(param._formGroupDiv)) {
                stb.append("     AND AVG_DIV = '" + AVG_COURSE + "' ");
                stb.append("     AND COURSECD || MAJORCD || COURSECODE = '" + group._code + "' ");
            } else if (GROUP_MAJOR.equals(param._formGroupDiv)) {
                stb.append("     AND AVG_DIV = '" + AVG_MAJOR + "' ");
                stb.append("     AND COURSECD || MAJORCD = '" + group._code + "' ");
            } else if (GROUP_COURSEGROUP.equals(param._formGroupDiv)) {
                stb.append("     AND AVG_DIV = '" + AVG_COURSEGROUP + "' ");
                stb.append("     AND COURSECD || MAJORCD || COURSECODE = '0' || '" + group._code + "' || '0000' ");
            }
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     'HR_CLASS' AS DIV, ");
            stb.append("     SCORE, ");
            stb.append("     SCORE_KANSAN, ");
            stb.append("     COUNT, ");
            stb.append("     AVG, ");
            stb.append("     AVG_KANSAN ");
            stb.append(" FROM ");
            stb.append("     PROFICIENCY_AVERAGE_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER = '" + param._semester + "' ");
            stb.append("     AND PROFICIENCYDIV = '" + param._proficiencyDiv + "' ");
            stb.append("     AND PROFICIENCYCD = '" + param._proficiencyCd + "' ");
            stb.append("     AND PROFICIENCY_SUBCLASS_CD = '" + proficiencySubclaas._proficiencySubclassCd + "' ");
            stb.append("     AND DATA_DIV = '" + AVG_DATA_SCORE + "' ");
            stb.append("     AND AVG_DIV = '" + AVG_HR + "' ");
            stb.append("     AND GRADE = '" + param._grade + "' ");
            stb.append("     AND HR_CLASS = '" + hrClass + "' ");
            return stb.toString();
        }
    }

    private static class Student {
        final String _schregNo;
        final String _attendNo;
        final String _name;
        final String _sex;
        final String _score;
        final String _scoreKansan;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;

        public Student (
                final String schregNo,
                final String attendNo,
                final String name,
                final String sex,
                final String score,
                final String scoreKansan,
                final String courseCd,
                final String majorCd,
                final String courseCode
        ) {
            _schregNo = schregNo;
            _attendNo = attendNo;
            _name = name;
            _sex = sex;
            _score = score;
            _scoreKansan = scoreKansan;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74810 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _semesterName;
        final String _ctrlDate;
        final String _proficiencyDiv;
        final String _proficiencyCd;
        final String _proficiencyName;
        final String _grade;
        final String _gradeName;
        final String _formGroupDiv; //帳票パターン
        final String[] _categorySelected;
        final String _knjh562PrintDisritubtionScore; // 1:素点で分布を表示する 1以外:換算点で分布を表示する
        private boolean _isMiyagi;
        final String _title;
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _semesterName = getSemesterName(db2, _year, _semester);
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _proficiencyDiv = request.getParameter("PROFICIENCYDIV");
            _proficiencyCd = request.getParameter("PROFICIENCYCD");
            _proficiencyName = getProficiencyName(db2, _proficiencyDiv, _proficiencyCd);
            _grade = request.getParameter("GRADE");
            _gradeName = getGradeName(db2, _year, _grade);
            _formGroupDiv = request.getParameter("FORM_GROUP_DIV");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _knjh562PrintDisritubtionScore = request.getParameter("KNJH562PrintDistributionScore");
            setZ010(db2);
            _title = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度" + (_semesterName + "  " + _proficiencyName + "分布表");
        }

        private String getSemesterName(final DB2UDB db2, final String year, final String semester) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "'"));
        }

        private String getProficiencyName(final DB2UDB db2, final String proficiencyDiv, final String proficiencyCd) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT PROFICIENCYNAME1 FROM PROFICIENCY_MST WHERE PROFICIENCYDIV = '" + proficiencyDiv + "' AND PROFICIENCYCD = '" + proficiencyCd + "'"));
        }

        private String getGradeName(final DB2UDB db2, final String year, final String grade) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "'"));
        }

        private void setZ010(final DB2UDB db2) {
            final String name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2 = '00'"));
            _isMiyagi = "miyagiken".equals(name1);
        }
    }

    private static class Group {
        final String _code;
        final String _name;
        public Group(final String code, final String name) {
            _code = code;
            _name = name;
        }

        public String toString() {
            return "Group(" + _code + ":" + _name + ")";
        }

        private static List getGroupList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sqlGroup(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("GROUP");
                    final String name = rs.getString("NAME");
                    if (null == code) {
                        continue;
                    }
                    list.add(new Group(code, name));
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String sqlGroup(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            if (GROUP_HR.equals(param._formGroupDiv)) {
                stb.append("     T1.HR_CLASS, ");
                stb.append("     L5.HR_NAME AS NAME ");
            } else if (GROUP_COURSE.equals(param._formGroupDiv)) {
                stb.append("     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS GROUP, ");
                stb.append("     VALUE(L2.MAJORNAME, '') || VALUE(L3.COURSECODENAME, '') AS NAME ");
            } else if (GROUP_MAJOR.equals(param._formGroupDiv)) {
                stb.append("     T1.COURSECD || T1.MAJORCD AS GROUP, ");
                stb.append("     L2.MAJORNAME AS NAME ");
            } else if (GROUP_COURSEGROUP.equals(param._formGroupDiv)) {
                stb.append("     T2.GROUP_CD AS GROUP, ");
                stb.append("     L4.GROUP_NAME AS NAME ");
            } else {
                stb.append("     T1.GRADE AS GROUP, ");
                stb.append("     L1.GRADE_NAME1 AS NAME ");
            }
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN COURSE_GROUP_CD_DAT T2 ");
            stb.append("         ON  T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("         AND T2.COURSECD = T1.COURSECD ");
            stb.append("         AND T2.MAJORCD = T1.MAJORCD ");
            stb.append("         AND T2.COURSECODE = T1.COURSECODE ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT L1 ");
            stb.append("         ON  L1.YEAR = T1.YEAR ");
            stb.append("         AND L1.GRADE = T1.GRADE ");
            stb.append("     LEFT JOIN MAJOR_MST L2 ");
            stb.append("         ON  L2.COURSECD = T1.COURSECD ");
            stb.append("         AND L2.MAJORCD = T1.MAJORCD ");
            stb.append("     LEFT JOIN COURSECODE_MST L3 ");
            stb.append("         ON  L3.COURSECODE = T1.COURSECODE ");
            stb.append("     LEFT JOIN COURSE_GROUP_CD_HDAT L4 ");
            stb.append("         ON  L4.YEAR = T2.YEAR ");
            stb.append("         AND L4.GRADE = T2.GRADE ");
            stb.append("         AND L4.GROUP_CD = T2.GROUP_CD ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT L5 ");
            stb.append("         ON  L5.YEAR = T1.YEAR ");
            stb.append("         AND L5.SEMESTER = T1.SEMESTER ");
            stb.append("         AND L5.GRADE = T1.GRADE ");
            stb.append("         AND L5.HR_CLASS = T1.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append("         T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     GROUP ");
            return stb.toString();
        }
    }

    private class ProficiencySubclass {
        private final String _proficiencySubclassCd;
        private final String _proficiencySubclassName;
        private String _highPerfect;
        private String _lowPerfect;

        public ProficiencySubclass(
                final DB2UDB db2,
                final String proficiencySubclassCd,
                final Group group
        ) {
            _proficiencySubclassCd = proficiencySubclassCd;
            _proficiencySubclassName = getSubclassName(db2);
            setPerfect(db2, group);
        }

        public String getPerfectString() {
            return _highPerfect.equals(_lowPerfect) ? _highPerfect : _lowPerfect + FROM_TO_MARK + _highPerfect;
        }

        private String getSubclassName(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SUBCLASS_NAME FROM PROFICIENCY_SUBCLASS_MST WHERE PROFICIENCY_SUBCLASS_CD = '" + _proficiencySubclassCd + "'"));
        }

        private void setPerfect(
                final DB2UDB db2,
                final Group group
        ) {
            _highPerfect = "";
            _lowPerfect = "";
            final String perfectSql = getPerfectSql(group);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(perfectSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _highPerfect = rs.getString("MAX_PERFECT");
                    _lowPerfect = rs.getString("MIN_PERFECT");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (null == _lowPerfect && null == _highPerfect) {
                _lowPerfect = "100";
                _highPerfect = "100";
            }
        }

        private String getPerfectSql(
                final Group group
        ) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     VALUE(MAX(PC1.PERFECT), 100) AS MAX_PERFECT, ");
            stb.append("     VALUE(MIN(PC1.PERFECT), 100) AS MIN_PERFECT ");
            stb.append(" FROM ");
            stb.append("     PROFICIENCY_PERFECT_COURSE_DAT PC1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR  = PC1.YEAR ");
            stb.append("        AND REGD.SEMESTER = PC1.SEMESTER ");
            stb.append("        AND REGD.GRADE = '" + _param._grade + "' ");
            if (GROUP_HR.equals(_param._formGroupDiv)) {
            } else if (GROUP_COURSE.equals(_param._formGroupDiv)) {
                stb.append("        AND REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE = '" + group._code + "' ");
            } else if (GROUP_MAJOR.equals(_param._formGroupDiv)) {
            } else if (GROUP_COURSEGROUP.equals(_param._formGroupDiv)) {
            } else {
            }
            stb.append("     LEFT JOIN COURSE_GROUP_CD_DAT CG1 ON  CG1.YEAR  = PC1.YEAR ");
            stb.append("        AND CG1.GRADE = '" + _param._grade + "' ");
            if (GROUP_HR.equals(_param._formGroupDiv)) {
            } else if (GROUP_COURSE.equals(_param._formGroupDiv)) {
                stb.append("        AND CG1.COURSECD || CG1.MAJORCD || CG1.COURSECODE = '" + group._code + "' ");
            } else if (GROUP_MAJOR.equals(_param._formGroupDiv)) {
            } else if (GROUP_COURSEGROUP.equals(_param._formGroupDiv)) {
                stb.append("        AND CG1.GROUP_CD = '" + group._code + "' ");
            } else {
            }
            stb.append(" WHERE ");
            stb.append("     PC1.YEAR = '" + _param._year + "' ");
            stb.append("     AND PC1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND PC1.PROFICIENCYDIV = '" + _param._proficiencyDiv + "' ");
            stb.append("     AND PC1.PROFICIENCYCD = '" + _param._proficiencyCd + "' ");
            stb.append("     AND PC1.PROFICIENCY_SUBCLASS_CD = '" + _proficiencySubclassCd + "' ");
            stb.append("     AND PC1.GRADE = CASE WHEN PC1.DIV = '01' THEN '00' ELSE '" + _param._grade + "' END ");
            stb.append("     AND PC1.COURSECD = CASE WHEN PC1.DIV = '03' THEN REGD.COURSECD ELSE '0' END ");
            stb.append("     AND PC1.MAJORCD = CASE WHEN PC1.DIV = '03' THEN REGD.MAJORCD WHEN PC1.DIV = '04' THEN CG1.GROUP_CD ELSE '000' END ");
            stb.append("     AND PC1.COURSECODE = CASE WHEN PC1.DIV = '03' THEN REGD.COURSECODE ELSE '0000' END ");

            return stb.toString();
        }
    }
}

// eof
