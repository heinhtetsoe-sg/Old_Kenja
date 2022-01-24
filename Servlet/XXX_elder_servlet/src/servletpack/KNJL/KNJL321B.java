/*
 * $Id: c69c12c8df5e6e40677f1f291f1c2e2d38abf9b2 $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２１Ｂ＞  合否判定資料
 **/
public class KNJL321B {

    private static final Log log = LogFactory.getLog(KNJL321B.class);

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

    private static int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.debug("exception!", e);
            }
        }
        return rtn;
    }

    private static String sishagonyu(final String s) {
        if (!NumberUtils.isNumber(s)) {
            return s;
        }
        return new BigDecimal(s).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String sishagonyu(final String s, int keta) {
        if (!NumberUtils.isNumber(s)) {
            return s;
        }
        return new BigDecimal(s).setScale(keta, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        String form = null;
        if ("1".equals(_param._testdiv)) {
            form = "KNJL321B_1.frm";
        } else if ("2".equals(_param._testdiv)) {
            form = "KNJL321B_2.frm";
        }

        final int maxLine = 50;
        final List applicantAllList = Applicant.load(db2, _param);
        final List pageList = getPageList(applicantAllList, maxLine);

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List applicantList = (List) pageList.get(pi);

            svf.VrSetForm(form, 1);

            svf.VrsOut("ENTEXAMYEAR", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度"); // 入試年度
            svf.VrsOut("TESTDIV", _param._testdivAbbv1); // 入試区分
            // svf.VrsOut("DESIREDIV", null); // 志望所属
            for (int i = 1; i <= 5; i++) {
                final String testsubclasscd = String.valueOf(i);
                svf.VrsOut("CLASSNAME" + testsubclasscd, (String) _param._testSubclassNameMap.get(testsubclasscd)); // 試験教科名
            }
            svf.VrsOut("DATE", _param._dateStr); // 作成日
            svf.VrsOut("SORT", "2".equals(_param._sort) ? "成績順" : "受験番号順");
            svf.VrsOut("PAGE1", String.valueOf(pi + 1));
            svf.VrsOut("PAGE2", String.valueOf(pageList.size()));

            int divNum = 1;
            for (final Iterator it = _param._nameL008Map.values().iterator(); it.hasNext();) {
            	final Map map = (Map) it.next();
                svf.VrsOut("CLASS_DIV_NAME" + divNum, (String) map.get("ABBV1") + "評定"); // 内申点 教科名
                divNum++;
            }

            for (int j = 0; j < applicantList.size(); j++) {
                final Applicant appl = (Applicant) applicantList.get(j);
                final int line = j + 1;
                svf.VrsOutn("PASS_DIV", line, appl._judgedivName); // 合否区分
                svf.VrsOutn("RANK", line, appl._totalRank4); // 順位
                svf.VrsOutn("HOPE_COURSE", line, appl._selectSubclassDivAbbv2); // 希望コース
                svf.VrsOutn("EXAMNO", line, appl._examno); // 受験番号
                svf.VrsOutn("NAME", line, appl._name); // 氏名
                svf.VrsOutn("SEX", line, appl._sexName); // 性別
                svf.VrsOutn("SCHOOL_NAME", line, appl._finschoolName); // 出身中略称
                for (int i = 1; i <= 5; i++) {
                    final String testsubclasscd = String.valueOf(i);
                    svf.VrsOutn("SCORE" + testsubclasscd, line, (String) appl._subclassScoreMap.get(testsubclasscd)); // 得点
                }
                if ("1".equals(_param._testdiv)) {
                    svf.VrsOutn("3TOTAL", line, appl._total3); // ３科合計
                    svf.VrsOutn("5TOTAL", line, appl._total5); // ５科合計
                    svf.VrsOutn("3AVERAGE", line, appl._avarage3); // ３科平均
                    svf.VrsOutn("ORAL_EXAM1", line, appl._interviewA); // 面接
                    svf.VrsOutn("ORAL_EXAM2", line, appl._interviewB); // 面接
                    svf.VrsOutn("ORAL_EXAM3", line, appl._interviewC); // 面接
                }
                if ("2".equals(_param._testdiv)) {
                    svf.VrsOutn("3TOTAL", line, appl._total3); // ３科合計
                    svf.VrsOutn("5TOTAL", line, appl._total5); // ５科合計
                    svf.VrsOutn("3AVERAGE", line, appl._avarage3); // ３科平均
                    svf.VrsOutn("5AVERAGE", line, appl._avarage1); // ５科平均
                    svf.VrsOutn("JUDGE_SCORERATE", line, appl._avarage4); // 判定得点率
                }
                final int remark1len = getMS932ByteLength(appl._remark1);
                svf.VrsOutn("THEDAY_CHECK" + (remark1len <= 20 ? "1" : remark1len <= 30 ? "2" : "3"), line, appl._remark1); // 当日チェック
                svf.VrsOutn("DIV1", line, (String) appl._divMap.get("DIV1")); // 内申点 教科1
                svf.VrsOutn("DIV2", line, (String) appl._divMap.get("DIV2")); // 内申点 教科2
                svf.VrsOutn("DIV3", line, (String) appl._divMap.get("DIV3")); // 内申点 教科3
                svf.VrsOutn("DIV4", line, (String) appl._divMap.get("DIV4")); // 内申点 教科4
                svf.VrsOutn("DIV5", line, (String) appl._divMap.get("DIV5")); // 内申点 教科5
                svf.VrsOutn("AVE_DIV", line, appl._aveDiv); // 内申点 5教科平均
                svf.VrsOutn("VALUE_AVG", line, sishagonyu(appl._averageAll)); // 評定平均
                svf.VrsOutn("VALUE1", line, appl._confidentialRptHas1); // 評定１
                if (appl._hasConfrptDat) {
                    svf.VrsOutn("REPORT_ACT", line, (appl._flgMaruCount < 2 ? "1" : "")); // 行動
                    svf.VrsOutn("REPORT_BASIC", line, appl._baseFlgShow); // 基本○無
                }
                svf.VrsOutn("ABSENT1", line, appl._absenceDays); // 欠席
                svf.VrsOutn("ABSENT2", line, appl._absenceDays2); // 欠席
                svf.VrsOutn("ABSENT3", line, appl._absenceDays3); // 欠席
                final String absentRemarkAll = appl.getAbsentRemarkAll();
                final int absentRemarkLen = getMS932ByteLength(absentRemarkAll);
                svf.VrsOutn("ABSENT_REMARK" + (absentRemarkLen <= 24 ? "1" : absentRemarkLen <= 36 ? "2" : "3"), line, absentRemarkAll); // 欠席理由
                svf.VrsOutn("BEFORE_SCORE1", line, appl._baseDetail4Remark3); // 入試相談点
                svf.VrsOutn("BEFORE_SCORE2", line, appl._baseDetail4Remark4); // 入試相談点
                if ("1".equals(_param._testdiv)) {
                    svf.VrsOutn("EXAMCOURSE_ABBV", line, appl._promiseCourseAbbv); // 確約区分
                }
                if ("2".equals(_param._testdiv)) {
                    svf.VrsOutn("PROMISE_DIV", line, appl._promiseCourseAbbv); // 確約区分
                }
                svf.VrsOutn("CLUBNAME", line, appl._baseDetail4Remark1); // 部活動名
                if ("1".equals(_param._testdiv)) {
                    final int bDetailRemark2Len = getMS932ByteLength(appl._baseDetail4Remark2);
                    svf.VrsOutn("RECOMMENDATION" + (bDetailRemark2Len <= 20 ? "1" : "2"), line, appl._baseDetail4Remark2); // 推薦理由
                }

                final int confrptRemark1Len = getMS932ByteLength(appl._confrptRemark1);
                String field = "REMARK" + (confrptRemark1Len <= 42 ? "1" : "2");
                if ("1".equals(_param._testdiv)) {
                    field = "REMARK" + (confrptRemark1Len > 46 ? "3" : confrptRemark1Len > 32 ? "2" : "1");
                }
                svf.VrsOutn(field, line, appl._confrptRemark1); // 調査書備考
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private static List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private static class Applicant {
        final String _judgedivName;
        final String _totalRank4;
        final String _selectSubclassDivAbbv2;
        final String _examno;
        final String _name;
        final String _sexName;
        final String _finschoolName;
        final String _total3;
        final String _total5;
        final String _avarage3;
        final String _avarage1;
        final String _avarage4;
        final String _interviewA;
        final String _interviewB;
        final String _interviewC;
        final String _remark1;
        final boolean _hasConfrptDat;
        final String _averageAll;
        final String _confidentialRptHas1;
        final int _flgMaruCount;
        final String _baseFlgShow;
        final String _absenceDays;
        final String _absenceDays2;
        final String _absenceDays3;
        final String _absenceRemark;
        final String _absenceRemark2;
        final String _absenceRemark3;
        final String _baseDetail4Remark3;
        final String _baseDetail4Remark4;
        final String _promiseCourseAbbv;
        final String _baseDetail4Remark1;
        final String _baseDetail4Remark2;
        final String _confrptRemark1;
        final Map _divMap;
        final String _aveDiv;
        final Map _subclassScoreMap = new HashMap();

        Applicant(
            final String judgedivName,
            final String totalRank4,
            final String selectSubclassDivAbbv2,
            final String examno,
            final String name,
            final String sexName,
            final String finschoolName,
            String total5,
            final String total3,
            final String avarage3,
            final String avarage1,
            final String avarage4,
            final String interviewA,
            final String interviewB,
            final String interviewC,
            final String remark1,
            final boolean hasConfrptDat,
            final String averageAll,
            final String confidentialRptHas1,
            final int flgMaruCount,
            final String baseFlgShow,
            final String absenceDays,
            final String absenceDays2,
            final String absenceDays3,
            final String absenceRemark,
            final String absenceRemark2,
            final String absenceRemark3,
            final String baseDetail4Remark3,
            final String baseDetail4Remark4,
            final String promiseCourseAbbv,
            final String baseDetail4Remark1,
            final String baseDetail4Remark2,
            final String confrptRemark1,
            final Map divMap,
            final String aveDiv
        ) {
            _judgedivName = judgedivName;
            _totalRank4 = totalRank4;
            _selectSubclassDivAbbv2 = selectSubclassDivAbbv2;
            _examno = examno;
            _name = name;
            _sexName = sexName;
            _finschoolName = finschoolName;
            _total3 = total3;
            _total5 = total5;
            _avarage3 = avarage3;
            _avarage1 = avarage1;
            _avarage4 = avarage4;
            _interviewA = interviewA;
            _interviewB = interviewB;
            _interviewC = interviewC;
            _remark1 = remark1;
            _hasConfrptDat = hasConfrptDat;
            _averageAll = averageAll;
            _confidentialRptHas1 = confidentialRptHas1;
            _flgMaruCount = flgMaruCount;
            _baseFlgShow = baseFlgShow;
            _absenceDays = absenceDays;
            _absenceDays2 = absenceDays2;
            _absenceDays3 = absenceDays3;
            _absenceRemark = absenceRemark;
            _absenceRemark2 = absenceRemark2;
            _absenceRemark3 = absenceRemark3;
            _baseDetail4Remark3 = baseDetail4Remark3;
            _baseDetail4Remark4 = baseDetail4Remark4;
            _promiseCourseAbbv = promiseCourseAbbv;
            _baseDetail4Remark1 = baseDetail4Remark1;
            _baseDetail4Remark2 = baseDetail4Remark2;
            _confrptRemark1 = confrptRemark1;
            _divMap = divMap;
            _aveDiv = aveDiv;

        }

        public String getAbsentRemarkAll() {
            final StringBuffer stb = new StringBuffer();
            final String[] rmks = {_absenceRemark, _absenceRemark2, _absenceRemark3};
            String spl = "";
            for (int i = 0; i < rmks.length; i++) {
                if (null != rmks[i]) {
                    stb.append(spl).append(rmks[i]);
                    spl = "、";
                }
            }
            return stb.toString();
        }

        public String nyushiSoudanten() {
            if (null == _baseDetail4Remark3 && null == _baseDetail4Remark4) {
                return null;
            }
            final int iRemark3 = Integer.parseInt(StringUtils.defaultString(_baseDetail4Remark3, "0"));
            final int iRemark4 = Integer.parseInt(StringUtils.defaultString(_baseDetail4Remark4, "0"));
            return String.valueOf(Math.max(iRemark3, iRemark4));
        }

        private static Applicant getApplicant(final List list, final String examno) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Applicant appl = (Applicant) it.next();
                if (appl._examno.equals(examno)) {
                    return appl;
                }
            }
            return null;
        }

        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String examno = rs.getString("EXAMNO");
                    if (null == getApplicant(list, examno)) {
                        final String judgedivName = rs.getString("JUDGEDIV_NAME");
                        final String totalRank4 = rs.getString("TOTAL_RANK4");
                        final String selectSubclassDivAbbv2 = rs.getString("SELECT_SUBCLASS_DIV_ABBV2");
                        final String name = rs.getString("NAME");
                        final String sexName = rs.getString("SEX_NAME");
                        final String finschoolName = rs.getString("FINSCHOOL_NAME");
                        final String total3 = rs.getString("TOTAL3");
                        final String total5 = rs.getString("TOTAL1");
                        final String avarage3 = rs.getString("AVARAGE3");
                        final String avarage1 = rs.getString("AVARAGE1");
                        final String avarage4 = rs.getString("AVARAGE4");
                        final String interviewA = rs.getString("INTERVIEW_A");
                        final String interviewB = rs.getString("INTERVIEW_B");
                        final String interviewC = rs.getString("INTERVIEW_C");
                        final String remark1 = rs.getString("REMARK1");
                        final boolean hasConfrptDat = 1 == rs.getInt("HAS_CONFRPT_DAT");
                        final String averageAll = rs.getString("AVERAGE_ALL");
                        String confidentialRptHas1 = null;
                        for (int i = 1; i <= 9; i++) {
                            final String confRptn = rs.getString("CONFIDENTIAL_RPT0" + String.valueOf(i));
                            if ("1".equals(confRptn)) {
                                confidentialRptHas1 = "1";
                            }
                        }
                        int flgMaruCount = 0;
                        for (int i = 1; i <= 10; i++) {
                            final String flgn = rs.getString("F" + String.valueOf(i));
                            if ("1".equals(flgn)) {
                                flgMaruCount += 1;
                            }
                        }
                        final String baseFlgShow = rs.getString("BASE_FLG_SHOW");
                        final String absenceDays = rs.getString("ABSENCE_DAYS");
                        final String absenceDays2 = rs.getString("ABSENCE_DAYS2");
                        final String absenceDays3 = rs.getString("ABSENCE_DAYS3");
                        final String absenceRemark = rs.getString("ABSENCE_REMARK");
                        final String absenceRemark2 = rs.getString("ABSENCE_REMARK2");
                        final String absenceRemark3 = rs.getString("ABSENCE_REMARK3");
                        final String baseDetail4Remark3 = rs.getString("BASE_DETAIL4_REMARK3");
                        final String baseDetail4Remark4 = rs.getString("BASE_DETAIL4_REMARK4");
                        final String promiseCourseAbbv = rs.getString("PROMISE_COURSE_ABBV");
                        final String baseDetail4Remark1 = rs.getString("BASE_DETAIL4_REMARK1");
                        final String baseDetail4Remark2 = rs.getString("BASE_DETAIL4_REMARK2");
                        final String confrptRemark1 = rs.getString("CONFRPT_REMARK1");

                        //内申点
                        final Map divMap = new HashMap();
                        String aveDiv = ""; //5教科平均
                        int totalDiv = 0;
                        int divNum = 1;
                        for (final Iterator it = param._nameL008Map.values().iterator(); it.hasNext();) {
                        	final Map map = (Map) it.next();
                            final String key = (String) map.get("NAMECD2");
                            final String nameSpare1 = (String) map.get("NAMESPARE1");
                            if("1".equals(nameSpare1)) {
                            	final String field = "CONFIDENTIAL_RPT"+key;
                            	final String div = StringUtils.defaultString(rs.getString(field),"0");
                                totalDiv = totalDiv + Integer.parseInt(div); //NAMESPARE1 = '1' の教科を加算
                                divMap.put("DIV"+divNum, div);
                                divNum++;
                            }
                        }
                        if(totalDiv != 0) {
                            double ave = (double) totalDiv / 5; //5教科平均
                            aveDiv = String.valueOf(ave);
                            aveDiv = sishagonyu(aveDiv, 1);
                        }
                        final Applicant applicant = new Applicant(judgedivName, totalRank4, selectSubclassDivAbbv2, examno, name, sexName, finschoolName, total5, total3, avarage3, avarage1, avarage4, interviewA, interviewB, interviewC, remark1, hasConfrptDat, averageAll, confidentialRptHas1, flgMaruCount, baseFlgShow, absenceDays, absenceDays2, absenceDays3, absenceRemark, absenceRemark2, absenceRemark3, baseDetail4Remark3, baseDetail4Remark4, promiseCourseAbbv, baseDetail4Remark1, baseDetail4Remark2, confrptRemark1, divMap, aveDiv);
                        list.add(applicant);
                    }

                    final Applicant applicant = getApplicant(list, examno);
                    final String testsubclasscd = rs.getString("TESTSUBCLASSCD");
                    final String score = rs.getString("SCORE");
                    applicant._subclassScoreMap.put(testsubclasscd, score);
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
            stb.append("  ");
            stb.append(" SELECT ");
            stb.append("     CASE WHEN RDET1.REMARK4 IN ('1', '2') THEN ");
            stb.append("       CASE WHEN RDET2.REMARK4 = '1' THEN CRSJG2.JUDGMENT_COURSE_ABBV ");
            stb.append("            WHEN RDET1.REMARK4 = '1' THEN CRSJG1.JUDGMENT_COURSE_ABBV ");
            stb.append("       ELSE NML013.NAME1 END ");
            stb.append("     ELSE ");
            stb.append("         NML013_2.NAME1 ");
            stb.append("     END AS JUDGEDIV_NAME, ");
            stb.append("     RECEPT.TOTAL_RANK4, ");
            stb.append("     NML033.ABBV2 AS SELECT_SUBCLASS_DIV_ABBV2, ");
            stb.append("     BASE.EXAMNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     NMZ002.NAME2 AS SEX_NAME,     ");
            stb.append("     FIN.FINSCHOOL_NAME_ABBV AS FINSCHOOL_NAME, ");
            stb.append("     RECEPT.TOTAL1, ");
            stb.append("     RECEPT.TOTAL3, ");
            stb.append("     RECEPT.AVARAGE3, ");
            stb.append("     RECEPT.AVARAGE1, ");
            stb.append("     RECEPT.AVARAGE4, ");
            stb.append("     INTV.INTERVIEW_A, ");
            stb.append("     INTV.INTERVIEW_B, ");
            stb.append("     INTV.INTERVIEW_C, ");
            stb.append("     BASE.REMARK1, ");
            stb.append("     CASE WHEN CONFRPT.EXAMNO IS NOT NULL THEN 1 ELSE 0 END AS HAS_CONFRPT_DAT, ");
            stb.append("     CONFRPT.AVERAGE_ALL, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT01, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT02, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT03, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT04, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT05, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT06, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT07, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT08, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT09, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT10, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT11, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT12, ");
            stb.append("     CONFRPT.BASE_FLG AS F1, ");
            stb.append("     CONFRPT.HEALTH_FLG AS F2, ");
            stb.append("     CONFRPT.ACTIVE_FLG AS F3, ");
            stb.append("     CONFRPT.RESPONSIBLE_FLG AS F4, ");
            stb.append("     CONFRPT.ORIGINAL_FLG AS F5, ");
            stb.append("     CONFRPT.MIND_FLG AS F6, ");
            stb.append("     CONFRPT.NATURE_FLG AS F7, ");
            stb.append("     CONFRPT.WORK_FLG AS F8, ");
            stb.append("     CONFRPT.JUSTICE_FLG AS F9, ");
            stb.append("     CONFRPT.PUBLIC_FLG AS F10, ");
            stb.append("     CASE WHEN VALUE(CONFRPT.BASE_FLG, '') <> '1' THEN '1' END AS BASE_FLG_SHOW,  ");
            stb.append("     CONFRPT.ABSENCE_DAYS, ");
            stb.append("     CONFRPT.ABSENCE_DAYS2, ");
            stb.append("     CONFRPT.ABSENCE_DAYS3, ");
            stb.append("     CONFRPT.ABSENCE_REMARK, ");
            stb.append("     CONFRPT.ABSENCE_REMARK2, ");
            stb.append("     CONFRPT.ABSENCE_REMARK3, ");
            stb.append("     BDETAIL4.REMARK3 AS BASE_DETAIL4_REMARK3, ");
            stb.append("     BDETAIL4.REMARK4 AS BASE_DETAIL4_REMARK4, ");
            stb.append("     CRS.PROMISE_COURSE_ABBV, ");
            stb.append("     BDETAIL4.REMARK1 AS BASE_DETAIL4_REMARK1, ");
            stb.append("     BDETAIL4.REMARK2 AS BASE_DETAIL4_REMARK2, ");
            stb.append("     CONFRPT.REMARK1 AS CONFRPT_REMARK1, ");
            stb.append("     NML009.NAMECD2 AS TESTSUBCLASSCD, ");
            stb.append("     TSCORE.SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL4 ON BDETAIL4.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND BDETAIL4.EXAMNO = BASE.EXAMNO ");
            stb.append("         AND BDETAIL4.SEQ = '004' ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND RECEPT.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND RECEPT.EXAM_TYPE = '1' ");
            stb.append("         AND RECEPT.EXAMNO = BASE.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RDET1 ON RDET1.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND RDET1.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND RDET1.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND RDET1.EXAM_TYPE = '1' ");
            stb.append("         AND RDET1.RECEPTNO = RECEPT.RECEPTNO ");
            stb.append("         AND RDET1.SEQ = '001' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST CRSJG1 ON CRSJG1.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND (BASE.TESTDIV = '1' AND CRSJG1.TAKE_RECOMMEND_TEST_FLG = '1'");
            stb.append("           OR BASE.TESTDIV = '2' AND CRSJG1.TAKE_GENERAL_TEST_FLG = '1') ");
            stb.append("         AND CRSJG1.NORMAL_PASSCOURSECD = RDET1.REMARK1 ");
            stb.append("         AND CRSJG1.NORMAL_PASSMAJORCD = RDET1.REMARK2 ");
            stb.append("         AND CRSJG1.NORMAL_PASSEXAMCOURSECD = RDET1.REMARK3 ");
            stb.append("         AND CRSJG1.EARLY_PASSCOURSECD IS NULL ");
            stb.append("         AND CRSJG1.EARLY_PASSMAJORCD IS NULL ");
            stb.append("         AND CRSJG1.EARLY_PASSEXAMCOURSECD IS NULL ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RDET2 ON RDET2.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND RDET2.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND RDET2.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND RDET2.EXAM_TYPE = '1' ");
            stb.append("         AND RDET2.RECEPTNO = RECEPT.RECEPTNO ");
            stb.append("         AND RDET2.SEQ = '002' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST CRSJG2 ON CRSJG2.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND BASE.TESTDIV = '2' "); // 一般入試のみ
            stb.append("         AND CRSJG2.CHANGE_SINGLE_TEST_FLG = '1' ");
            stb.append("         AND CRSJG2.EARLY_PASSCOURSECD = RDET2.REMARK1 ");
            stb.append("         AND CRSJG2.EARLY_PASSMAJORCD = RDET2.REMARK2 ");
            stb.append("         AND CRSJG2.EARLY_PASSEXAMCOURSECD = RDET2.REMARK3 ");
            stb.append("     LEFT JOIN NAME_MST NML009 ON NML009.NAMECD1 = 'L009' ");
            stb.append("         AND ( ");
            stb.append("              (BASE.TESTDIV = '1' AND NML009.NAMESPARE2 = '1') OR ");
            stb.append("              (BASE.TESTDIV = '2' AND NML009.NAMESPARE3 = '1') ");
            stb.append("         ) ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT TSCORE ON TSCORE.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND TSCORE.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND TSCORE.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND TSCORE.EXAM_TYPE = RECEPT.EXAM_TYPE ");
            stb.append("         AND TSCORE.RECEPTNO = RECEPT.RECEPTNO ");
            stb.append("         AND TSCORE.TESTSUBCLASSCD = NML009.NAMECD2 ");
            stb.append("     LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = BASE.FS_CD ");
            stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTV ON INTV.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND INTV.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND INTV.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND INTV.EXAMNO = BASE.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ON CONFRPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND CONFRPT.EXAMNO = BASE.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST CRS ON CRS.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND CRS.JUDGMENT_DIV = BDETAIL4.REMARK8 ");
            stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' ");
            stb.append("         AND NML013.NAMECD2 = RDET1.REMARK4 ");
            stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' ");
            stb.append("         AND NMZ002.NAMECD2 = BASE.SEX ");
            stb.append("     LEFT JOIN NAME_MST NML013_2 ON NML013_2.NAMECD1 = 'L013' ");
            stb.append("         AND NML013_2.NAMECD2 = RECEPT.JUDGEDIV ");
            stb.append("     LEFT JOIN NAME_MST NML033 ON NML033.NAMECD1 = 'L033' ");
            stb.append("         AND NML033.NAMECD2 = BASE.SELECT_SUBCLASS_DIV ");
            stb.append(" WHERE ");
            stb.append("     BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND BASE.TESTDIV = '" + param._testdiv + "' ");
            if ("1".equals(param._specialReasonDiv)) {
                stb.append("     AND BASE.SPECIAL_REASON_DIV IS NOT NULL ");
            }
            stb.append(" ORDER BY ");
            if ("2".equals(param._sort)) {
                stb.append("     RECEPT.TOTAL_RANK4, ");
            }
            stb.append("     BASE.EXAMNO ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;
        final String _sort;
        final String _specialReasonDiv;

        final String _testdivAbbv1;
        final String _dateStr;
        final Map _testSubclassNameMap;
        final Map _nameL008Map;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _sort = request.getParameter("SORT");
            _date = request.getParameter("CTRL_DATE").replace('/', '-');
            _dateStr = getDateStr(db2, _date);
            _specialReasonDiv = request.getParameter("SPECIAL_REASON_DIV");

            _testdivAbbv1 = getNameMst(db2, "ABBV1", "L004", _testdiv);
            _testSubclassNameMap = getTestSubclassName(db2);
            _nameL008Map = getNameMstL008(db2);
        }

        private String getDateStr(final DB2UDB db2, final String date) {
            final Calendar cal = Calendar.getInstance();
            final DecimalFormat df = new DecimalFormat("00");
            final int hour = cal.get(Calendar.HOUR_OF_DAY);
            final int minute = cal.get(Calendar.MINUTE);
            return KNJ_EditDate.tate_format4(db2, date) + "　" + df.format(hour) + "時" + df.format(minute) + "分現在";
        }

        private Map getTestSubclassName(final DB2UDB db2) {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'L009' ";
                if ("1".equals(_testdiv)) {
                    sql += " AND NAMESPARE2 = '1' ";
                } else if ("2".equals(_testdiv)) {
                    sql += " AND NAMESPARE3 = '1' ";
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
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

        private Map getNameMstL008(final DB2UDB db2) {
            final Map rtn = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT * ");
                stb.append(" FROM NAME_MST ");
                stb.append(" WHERE NAMECD1    = 'L008' ");
                stb.append("   AND NAMESPARE1 = '1' ");
                stb.append(" ORDER BY NAMECD2 ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map map = new HashMap();
                    final String key = StringUtils.defaultString(rs.getString("NAMECD2"));
                    map.put("NAMECD2", key);
                    map.put("NAME1", StringUtils.defaultString(rs.getString("NAME1")));
                    map.put("ABBV1", StringUtils.defaultString(rs.getString("ABBV1")));
                    map.put("NAMESPARE1", StringUtils.defaultString(rs.getString("NAMESPARE1")));

                    rtn.put(key, map);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof

