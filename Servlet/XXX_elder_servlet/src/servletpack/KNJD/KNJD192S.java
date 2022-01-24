/*
 * $Id: 44fc0434a7ab8fcdde211e21ef350a21e3270ca4 $
 *
 * 作成日: 2016/12/09
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

/**
 * 駿台甲府中学　再試通知
 */
public class KNJD192S {

    private static final Log log = LogFactory.getLog(KNJD192S.class);

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
        final List studentList = Student.getStudentList(db2, _param);
        final String form = "KNJD192S.frm";

        for (int pi = 0; pi < studentList.size(); pi++) {
            final Student student = (Student) studentList.get(pi);

            boolean hasSaishi = false;
            for (int i = 0; i < student._subclassList.size(); i++) {
                final Map subclassMap = (Map) student._subclassList.get(i);
                if (isSaishi(subclassMap, _param)) {
                    hasSaishi = true;
                }
            }
            if (!hasSaishi) {
                // 再試がなければ表示しない
                continue;
            }
            svf.VrSetForm(form, 1);

            final String attendno = (NumberUtils.isDigits(student._attendno) ? Integer.valueOf(student._attendno).toString() : StringUtils.defaultString(student._attendno)) + "番";

            svf.VrsOut("HR_NAME1", StringUtils.defaultString(student._hrname) + attendno); // 年組版
            svf.VrsOut("HR_NAME2", StringUtils.defaultString(student._hrname) + attendno); // 年組版
            
            svf.VrsOut("NAME1_" + (KNJ_EditKinsoku.getMS932ByteCount(student._name) <= 24 ? "1" : "2"), student._name); // 氏名
            svf.VrsOut("NAME2_" + (KNJ_EditKinsoku.getMS932ByteCount(student._name) <= 24 ? "1" : "2"), student._name); // 氏名
//            svf.VrsOut("GUARD_NAME" + (KNJ_EditKinsoku.getMS932ByteCount(guard_name) <= 24 ? "1" : "2"), guard_name); // 保護者氏名

//            svf.VrsOut("LEAD_DATE", "指導日　" + KNJ_EditDate.h_format_JP(_param._leadDate) + "　" + _param.getLeadTime());
//            svf.VrsOut("RETEST_DATE", "再試日　" + KNJ_EditDate.h_format_JP(_param._retestDate) + "　" + _param.getReTestTime());

            for (int j = 0; j < student._subclassList.size(); j++) {
                final Map subclassMap = (Map) student._subclassList.get(j);
                final String subclassname = (String) subclassMap.get("SUBCLASSNAME");
                final String score = (String) subclassMap.get("SCORE");
                String judge = "";
                String judgeAttribute = null;
                if (isSaishi(subclassMap, _param)) {
                    judge = "再試";
                    judgeAttribute = "Palette=9"; // 赤
                } else if (isTyui(subclassMap, _param)) {
                    judge = "注意";
                    judgeAttribute = "Palette=12"; // 青
                }
                final int line = j + 1;
                svf.VrsOutn("SUBJECT", line, subclassname); // 科目
                svf.VrsOutn("POINT", line, score); // 点数
                svf.VrsOutn("JUDGE", line, judge); // 判定
                if (null != judgeAttribute) {
                    svf.VrAttributen("JUDGE", line, judgeAttribute); // 判定
                }
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }
    
    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes( "MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    // 科目が再試（再試点未満）か
    private static boolean isSaishi(final Map subclassMap, final Param param) {
        final String score = (String) subclassMap.get("SCORE");
        if (NumberUtils.isDigits(score)) {
            final int passScore = Integer.parseInt((String) subclassMap.get("PASS_SCORE"));
            if (Integer.parseInt(score) < passScore) {
                return true;
            }
        }
        return false;
    }

    // 科目が注意（再試点以上注意点以下）か
    private static boolean isTyui(final Map subclassMap, final Param param) {
        final String score = (String) subclassMap.get("SCORE");
        if (NumberUtils.isDigits(score)) {
            final int passScore = Integer.parseInt((String) subclassMap.get("PASS_SCORE"));
            if (Integer.parseInt(score) < passScore) {
                return false;
            } else if (passScore <= Integer.parseInt(score) && Integer.parseInt(score) < (passScore + 10)) {
                return true;
            }
        }
        return false;
    }

    private static class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrname;
        final String _attendno;
        final String _name;
        final List _subclassList;

        Student(
            final String schregno,
            final String grade,
            final String hrClass,
            final String hrname,
            final String attendno,
            final String name
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrname = hrname;
            _attendno = attendno;
            _name = name;
            _subclassList = new ArrayList();
        }

        public static List getStudentList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            final Map studentMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    final boolean is5ka = "1".equals(rs.getString("IS_5KA"));
                    if (!is5ka) {
                        //log.info("5科目以外は除外:" + rs.getString("SUBCLASSCD"));
                        continue;
                    }

                    final int passScore = rs.getInt("PASS_SCORE");
                    final int passScoreTyui = rs.getInt("PASS_SCORE") + 10;
                    final int score = rs.getInt("SCORE");
                    if (score < passScore || score < passScoreTyui) {

                        final String schregno = rs.getString("SCHREGNO");
                        if (null == studentMap.get(schregno)) {
                            final String grade = rs.getString("GRADE");
                            final String hrClass = rs.getString("HR_CLASS");
                            final String hrname = rs.getString("HR_NAME");
                            final String attendno = rs.getString("ATTENDNO");
                            final String name = rs.getString("NAME");
                            final Student student = new Student(schregno, grade, hrClass, hrname, attendno, name);
                            list.add(student);
                            studentMap.put(schregno, student);
                        }

                        final Student student = (Student) studentMap.get(schregno);

                        final Map subclassMap = new HashMap();
                        student._subclassList.add(subclassMap);
                        subclassMap.put("SUBCLASSCD", rs.getString("SUBCLASSCD"));
                        subclassMap.put("SUBCLASSNAME", rs.getString("SUBCLASSNAME"));
                        subclassMap.put("SCORE", rs.getString("SCORE"));
                        subclassMap.put("PASS_SCORE", rs.getString("PASS_SCORE"));
                    }

                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SCORE.SCHREGNO ");
            stb.append("   , REGD.GRADE ");
            stb.append("   , REGD.HR_CLASS ");
            stb.append("   , REGDH.HR_NAME ");
            stb.append("   , REGD.ATTENDNO ");
            stb.append("   , BASE.NAME ");
            stb.append("   , SCORE.CLASSCD || '-' || SCORE.SCHOOL_KIND || '-' || SCORE.CURRICULUM_CD || '-' || SCORE.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("   , T3.SUBCLASSNAME ");
            stb.append("   , SCORE.SCORE ");
            stb.append("   , VALUE(PERFECT01.PASS_SCORE, PERFECT02.PASS_SCORE, PERFECT03.PASS_SCORE, PERFECT04.PASS_SCORE, 50) AS PASS_SCORE ");
            stb.append("   , CASE WHEN SUBGRP1.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS IS_5KA ");
            stb.append(" FROM RECORD_SCORE_DAT SCORE ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = SCORE.YEAR ");
            stb.append("     AND REGD.SEMESTER = '" + ("9".equals(param._semester) ? param._ctrlSeme : param._semester) + "' ");
            stb.append("     AND REGD.SCHREGNO = SCORE.SCHREGNO ");
            stb.append(" INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = SCORE.YEAR ");
            stb.append("     AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("     AND REGDH.GRADE = REGD.GRADE ");
            stb.append("     AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = SCORE.SCHREGNO ");
            stb.append(" INNER JOIN SUBCLASS_MST T3 ON T3.CLASSCD = SCORE.CLASSCD ");
            stb.append("     AND T3.SCHOOL_KIND = SCORE.SCHOOL_KIND ");
            stb.append("     AND T3.CURRICULUM_CD = SCORE.CURRICULUM_CD ");
            stb.append("     AND T3.SUBCLASSCD = SCORE.SUBCLASSCD ");
            stb.append(" LEFT JOIN PERFECT_RECORD_SDIV_DAT PERFECT01 ON SCORE.YEAR = PERFECT01.YEAR ");
            stb.append("       AND SCORE.SEMESTER = PERFECT01.SEMESTER ");
            stb.append("       AND SCORE.TESTKINDCD = PERFECT01.TESTKINDCD ");
            stb.append("       AND SCORE.TESTITEMCD = PERFECT01.TESTITEMCD ");
            stb.append("       AND PERFECT01.SCORE_DIV = '01' ");
            stb.append("       AND SCORE.CLASSCD = PERFECT01.CLASSCD ");
            stb.append("       AND SCORE.SCHOOL_KIND = PERFECT01.SCHOOL_KIND ");
            stb.append("       AND SCORE.CURRICULUM_CD = PERFECT01.CURRICULUM_CD ");
            stb.append("       AND SCORE.SUBCLASSCD = PERFECT01.SUBCLASSCD ");
            stb.append("       AND PERFECT01.DIV = '01' ");
            stb.append(" LEFT JOIN PERFECT_RECORD_SDIV_DAT PERFECT02 ON SCORE.YEAR = PERFECT02.YEAR ");
            stb.append("       AND SCORE.SEMESTER = PERFECT02.SEMESTER ");
            stb.append("       AND SCORE.TESTKINDCD = PERFECT02.TESTKINDCD ");
            stb.append("       AND SCORE.TESTITEMCD = PERFECT02.TESTITEMCD ");
            stb.append("       AND PERFECT02.SCORE_DIV = '01' ");
            stb.append("       AND SCORE.CLASSCD = PERFECT02.CLASSCD ");
            stb.append("       AND SCORE.SCHOOL_KIND = PERFECT02.SCHOOL_KIND ");
            stb.append("       AND SCORE.CURRICULUM_CD = PERFECT02.CURRICULUM_CD ");
            stb.append("       AND SCORE.SUBCLASSCD = PERFECT02.SUBCLASSCD ");
            stb.append("       AND PERFECT02.DIV = '02' ");
            stb.append("       AND PERFECT02.GRADE = '" + param._grade + "' ");
            stb.append("       AND PERFECT02.COURSECD || PERFECT02.MAJORCD = '0000' ");
            stb.append("       AND PERFECT02.COURSECODE = '0000' ");
            stb.append(" LEFT JOIN PERFECT_RECORD_SDIV_DAT PERFECT03 ON SCORE.YEAR = PERFECT03.YEAR ");
            stb.append("       AND SCORE.SEMESTER = PERFECT03.SEMESTER ");
            stb.append("       AND SCORE.TESTKINDCD = PERFECT03.TESTKINDCD ");
            stb.append("       AND SCORE.TESTITEMCD = PERFECT03.TESTITEMCD ");
            stb.append("       AND PERFECT03.SCORE_DIV = '01' ");
            stb.append("       AND SCORE.CLASSCD = PERFECT03.CLASSCD ");
            stb.append("       AND SCORE.SCHOOL_KIND = PERFECT03.SCHOOL_KIND ");
            stb.append("       AND SCORE.CURRICULUM_CD = PERFECT03.CURRICULUM_CD ");
            stb.append("       AND SCORE.SUBCLASSCD = PERFECT03.SUBCLASSCD ");
            stb.append("       AND PERFECT03.DIV = '03' ");
            stb.append("       AND REGD.GRADE = PERFECT03.GRADE ");
            stb.append("       AND REGD.COURSECD = PERFECT03.COURSECD ");
            stb.append("       AND REGD.MAJORCD = PERFECT03.MAJORCD ");
            stb.append("       AND REGD.COURSECODE = PERFECT03.COURSECODE ");
            stb.append(" LEFT JOIN COURSE_GROUP_CD_DAT GROUP_CD ON REGD.YEAR = GROUP_CD.YEAR ");
            stb.append("       AND REGD.GRADE = GROUP_CD.GRADE ");
            stb.append("       AND REGD.COURSECD = GROUP_CD.COURSECD ");
            stb.append("       AND REGD.MAJORCD = GROUP_CD.MAJORCD ");
            stb.append("       AND REGD.COURSECODE = GROUP_CD.COURSECODE ");
            stb.append(" LEFT JOIN PERFECT_RECORD_SDIV_DAT PERFECT04 ON SCORE.YEAR = PERFECT04.YEAR ");
            stb.append("       AND SCORE.SEMESTER = PERFECT04.SEMESTER ");
            stb.append("       AND SCORE.TESTKINDCD = PERFECT04.TESTKINDCD ");
            stb.append("       AND SCORE.TESTITEMCD = PERFECT04.TESTITEMCD ");
            stb.append("       AND PERFECT04.SCORE_DIV = '01' ");
            stb.append("       AND SCORE.CLASSCD = PERFECT04.CLASSCD ");
            stb.append("       AND SCORE.SCHOOL_KIND = PERFECT04.SCHOOL_KIND ");
            stb.append("       AND SCORE.CURRICULUM_CD = PERFECT04.CURRICULUM_CD ");
            stb.append("       AND SCORE.SUBCLASSCD = PERFECT04.SUBCLASSCD ");
            stb.append("       AND PERFECT04.DIV = '04' ");
            stb.append("       AND PERFECT04.GRADE = '" + param._grade + "' ");
            stb.append("       AND PERFECT04.COURSECD = '0' ");
            stb.append("       AND PERFECT04.MAJORCD = GROUP_CD.GROUP_CD ");
            stb.append("       AND PERFECT04.COURSECODE = '0000' ");
            stb.append(" LEFT JOIN REC_SUBCLASS_GROUP_DAT SUBGRP1 ON SUBGRP1.YEAR = SCORE.YEAR ");
            stb.append("       AND SUBGRP1.GROUP_DIV = '5' ");
            stb.append("       AND SUBGRP1.GRADE = REGD.GRADE ");
            stb.append("       AND SUBGRP1.COURSECD = REGD.COURSECD ");
            stb.append("       AND SUBGRP1.MAJORCD = REGD.MAJORCD ");
            stb.append("       AND SUBGRP1.COURSECODE = REGD.COURSECODE ");
            stb.append("       AND SUBGRP1.CLASSCD = SCORE.CLASSCD ");
            stb.append("       AND SUBGRP1.SCHOOL_KIND = SCORE.SCHOOL_KIND ");
            stb.append("       AND SUBGRP1.CURRICULUM_CD = SCORE.CURRICULUM_CD ");
            stb.append("       AND SUBGRP1.SUBCLASSCD = SCORE.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append(" SCORE.YEAR = '" + param._year + "' ");
            stb.append(" AND SCORE.SEMESTER = '" + param._semester + "' ");
            stb.append(" AND SCORE.SEMESTER || SCORE.TESTKINDCD || SCORE.TESTITEMCD || SCORE.SCORE_DIV = '" + param._subTestcd + "' ");
            stb.append(" AND REGD.GRADE = '" + param._grade + "' ");
            if ("2".equals(param._categoryIsClass)) {
                stb.append(" AND REGD.HR_CLASS = '" + param._hrClass + "' ");
                stb.append(" AND SCORE.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            } else {
                stb.append(" AND REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            }
            stb.append(" AND REGD.COURSECD || '-' || REGD.MAJORCD = '" + param._major + "' ");
            stb.append(" ORDER BY REGD.GRADE ");
            stb.append("     , REGD.HR_CLASS ");
            stb.append("     , REGD.ATTENDNO ");
            stb.append("     , SCORE.CLASSCD || '-' || SCORE.SCHOOL_KIND || '-' || SCORE.CURRICULUM_CD || '-' || SCORE.SUBCLASSCD ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSeme;
        final String _grade;
        final String _hrClass;
        final String _major;
        final String _categoryIsClass;
        final String[] _categorySelected;
        final String _subTestcd;
//        final String _leadDate;
//        final String _lfHour;
//        final String _lfMinute;
//        final String _ltHour;
//        final String _ltMinute;
//
//        final String _retestDate;
//        final String _rfHour;
//        final String _rfMinute;
//        final String _rtHour;
//        final String _rtMinute;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _major = request.getParameter("MAJOR");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _subTestcd = request.getParameter("SUB_TESTCD");
//            _leadDate = request.getParameter("LEADDATE");
//            _lfHour = request.getParameter("LFHOUR");
//            _lfMinute = request.getParameter("LFMINUTE");
//            _ltHour = request.getParameter("LTHOUR");
//            _ltMinute = request.getParameter("LTMINUTE");
//            _retestDate = request.getParameter("RETESTDATE");
//            _rfHour = request.getParameter("RFHOUR");
//            _rfMinute = request.getParameter("RFMINUTE");
//            _rtHour = request.getParameter("RTHOUR");
//            _rtMinute = request.getParameter("RTMINUTE");
        }

//        public String getLeadTime() {
//            String retStr = "";
//            if ((null == _lfHour || "".equals(_lfHour))
//                 && (null == _lfMinute || "".equals(_lfMinute))
//                 && (null == _ltHour || "".equals(_ltHour))
//                 && (null == _ltMinute || "".equals(_ltMinute))
//            ) {
//                retStr = "";
//            } else {
//                retStr  = getSpUme(_lfHour);
//                retStr += ":";
//                retStr += getSpUme(_lfMinute);
//                retStr += "\uFF5E";
//                retStr += getSpUme(_ltHour);
//                retStr += ":";
//                retStr += getSpUme(_ltMinute);
//            }
//            return retStr;
//        }
//
//        public String getReTestTime() {
//            String retStr = "";
//            if ((null == _rfHour || "".equals(_rfHour))
//                    && (null == _rfMinute || "".equals(_rfMinute))
//                    && (null == _rtHour || "".equals(_rtHour))
//                    && (null == _rtMinute || "".equals(_rtMinute))
//            ) {
//                retStr = "";
//            } else {
//                retStr  = getSpUme(_rfHour);
//                retStr += ":";
//                retStr += getSpUme(_rfMinute);
//                retStr += "\uFF5E";
//                retStr += getSpUme(_rtHour);
//                retStr += ":";
//                retStr += getSpUme(_rtMinute);
//            }
//            return retStr;
//        }
//
//        private String getSpUme(final String setStr) {
//            String retStr;
//            final String spStr = "  " + setStr;
//            retStr = spStr.substring(spStr.length() - 2, spStr.length());
//            return retStr;
//        }
    }
}

// eof

