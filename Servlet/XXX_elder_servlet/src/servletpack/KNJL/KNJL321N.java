/*
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２１Ｎ＞  成績一覧表
 **/
public class KNJL321N {

    private static final Log log = LogFactory.getLog(KNJL321N.class);

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
    	return KNJ_EditEdit.getMS932ByteLength(s);
    }

    private static String sishagonyu(final String s) {
        if (!NumberUtils.isNumber(s)) {
            return s;
        }
        return new BigDecimal(s).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        String form = "KNJL321N.frm";

        final int maxLine = 35;
        final List applicantAllList = Applicant.getApplicantList(db2, _param);
        final List divList = ("1".equals(_param._order)) ? getDivList(applicantAllList) : getDivListOrder2(applicantAllList);

        final String[] subclasscds = {"1", "2", "3", "4", "5"};

        int pastPage = 0;
        for (int di = 0; di < divList.size(); di++) {
            final List divapplList = (List) divList.get(di);

            String shdivName = null;
            String examcourseName = null;
            String kyoikuName = null;
            if ("1".equals(_param._order)) {
            	for (int j = 0; j < divapplList.size(); j++) {
            		final Applicant appl = (Applicant) divapplList.get(j);
            		shdivName = appl._shdivName;
            		examcourseName = appl._examcourseName;
            		kyoikuName = appl._kyoikuName;
            	}
            }
            final String title = ("1".equals(_param._order)) ? StringUtils.defaultString(shdivName) + "・" + StringUtils.defaultString(examcourseName) : "受験番号順全受験者";
            final StringBuffer subTitle = new StringBuffer();
            subTitle.append(_param._entexamyear).append("年度").append("　　");
            subTitle.append("入試制度：").append(StringUtils.defaultString(_param._applicantdivAbbv1)).append("　　");
            subTitle.append("入試回数：").append(StringUtils.defaultString(_param._testdivAbbv1)).append("　　");
            if ("1".equals(_param._order)) {
            	subTitle.append("教育相談：").append(StringUtils.defaultString(kyoikuName)).append("　　");
            }
            if (null != _param._scoreTotal) {
                subTitle.append("合計点：").append(_param._scoreTotal).append("点以下　　");
            }

            final List pageList = getPageList(divapplList, maxLine);
            for (int i = 0; i < pageList.size(); i++) {
                final List applList = (List) pageList.get(i);

                svf.VrSetForm(form, 1);
                svf.VrsOut("TITLE", "成績一覧表（" + title + "）"); // タイトル
                svf.VrsOut("SUB_TITLE", subTitle.toString()); // サブタイトル
                svf.VrsOut("PAGE1", String.valueOf(pastPage + i + 1));
                svf.VrsOut("DATE", "作成日：" + _param._dateStr); // 作成日

                for (int si = 0; si < subclasscds.length; si++) {
                    svf.VrsOut("SUBJECT" + String.valueOf(si + 1), (String) _param._testSubclassNameMap.get(subclasscds[si])); // 科目名
                }

                for (int j = 0; j < applList.size(); j++) {
                    final Applicant appl = (Applicant) applList.get(j);
                    final int line = j + 1;
                    svf.VrsOutn("EDU", line, appl._kyoikuName); // 教育
                    svf.VrsOutn("COURSE", line, appl._sucExamcourseName); // コース
                    svf.VrsOutn("DIV", line, appl._shdivName); // 区分
                    svf.VrsOutn("EXAM_NO", line, appl._examno); // 受験番号

                    final int nameKeta = getMS932ByteLength(appl._name);
                    if (nameKeta <= 16) {
                        svf.VrsOutn("NAME1", line, appl._name); // 氏名
                    } else if (nameKeta <= 24) {
                        svf.VrsOutn("NAME2", line, appl._name); // 氏名
                    } else {
                        svf.VrsOutn("NAME3", line, appl._name); // 氏名
                    }

                    svf.VrsOutn("FINSCHOOL_NAME", line, appl._finschoolName); // 中学校名
                    svf.VrsOutn("ABSENCE", line, appl._kessekiJitaiName); // 欠席辞退

                    for (int si = 0; si < subclasscds.length; si++) {
                        svf.VrsOutn("SCORE" + String.valueOf(si + 1), line, (String) appl._subclassScoreMap.get(subclasscds[si])); // 点数
                    }

                    svf.VrsOutn("TOTAL", line, appl._total4); // 合計
                    svf.VrsOutn("RANK", line, appl._divRank4); // 順位
                    svf.VrsOutn("SECOND_HOPE", line, appl._examcourseName2); // 第二志望コース
                    final String remark1 = StringUtils.defaultString(appl._remark7Name) + (StringUtils.isBlank(appl._remark1) ? "" : "(" + appl._remark1 + ")");
                    print1(svf, line, remark1,       10, 16, new String[] {"REMARK1_1", "REMARK1_2", "REMARK1_3", "REMARK1_4"}); // 備考
                    print1(svf, line, appl._remark2, 20, 30, new String[] {"REMARK2_1", "REMARK2_2", "REMARK2_3", "REMARK2_4"}); // 備考
                    print1(svf, line, appl._remark3, 20, 30, new String[] {"REMARK3_1", "REMARK3_2", "REMARK3_3", "REMARK3_4"}); // 備考
                    print1(svf, line, appl._remark4, 20, 30, new String[] {"REMARK4_1", "REMARK4_2", "REMARK4_3", "REMARK4_4"}); // 備考
                    print1(svf, line, appl._remark6, 20, 30, new String[] {"REMARK6_1", "REMARK6_2", "REMARK6_3", "REMARK6_4"}); // 備考
                    print1(svf, line, appl._clubName, 12, 18, new String[] {"CLUB1", "CLUB2", "CLUB3_1", "CLUB3_2"}); // クラブ
                    svf.VrsOutn("TOTAL_VAL", line, appl._totalAll); // 評定合計
                    svf.VrsOutn("TOTAL5_VAL", line, appl._total5); // 5科目評定合計
                    svf.VrsOutn("STAGE", line, appl._kasantenAll); // 段階
                }
                _hasData = true;
                svf.VrEndPage();
            }
            pastPage += pageList.size();
        }
    }

    private static String getShiryouNo(final Set kyoikuSet, final String shdiv, final String examcoursecd) {
        String rtn = "";
        if ("1".equals(shdiv) || "2".equals(shdiv) || "9".equals(shdiv)) {
            if (examcoursecd.endsWith("0001")) {
                rtn = "01";
            } else if (examcoursecd.endsWith("0002")) {
                rtn = "02";
            } else if (examcoursecd.endsWith("0003")) {
                rtn = "03";
            } else if (examcoursecd.endsWith("0004")) {
                rtn = "04";
            } else if (examcoursecd.endsWith("0005")) {
                rtn = "05";
            }
        } else if ("3".equals(shdiv)) {
            if (examcoursecd.endsWith("0001")) {
                rtn = "07";
            } else if (examcoursecd.endsWith("0002")) {
                rtn = "08";
            } else if (examcoursecd.endsWith("0003")) {
                rtn = "09";
            } else if (examcoursecd.endsWith("0004")) {
                rtn = "10";
            } else if (examcoursecd.endsWith("0005")) {
                rtn = "11";
            }
        }
        if (kyoikuSet.contains("2")) { // ×
            rtn = rtn + "_3";
        } else if (kyoikuSet.contains("4")) { // ▲
            rtn = rtn + "_2";
        }
        return rtn;
    }

    public void print1(final Vrw32alp svf, final int line, final String data, final int keta1, final int keta2, final String[] fields) {
        final int keta = getMS932ByteLength(data);
        if (keta < keta1) {
            svf.VrsOutn(fields[0], line, data);
        } else if (keta < keta2) {
            svf.VrsOutn(fields[1], line, data);
        } else {
            final String[] token = KNJ_EditEdit.get_token(data, keta2, 2);
            if (null != token) {
                for (int k = 0; k < Math.min(token.length, fields.length - 2); k++) {
                    svf.VrsOutn(fields[2 + k], line, data);
                }
            }
        }
    }

    private static String bikoField(final String data) {
        final int keta = getMS932ByteLength(data);
        final String field;
        if (keta <= 20) {
            field = "1";
        } else if (keta <= 40) {
            field = "2";
        } else {
            field = "3";
        }
        return field;
    }

    private static List getDivList(final List list) {
        final List rtn = new ArrayList();
        String currentShdiv = null;
        String currentkyoiku = null;
        String currentExamcourse = null;
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Applicant appl = (Applicant) it.next();
            final boolean diffShDiv = !(null == currentShdiv && null == appl._shdiv || null != currentShdiv && currentShdiv.equals(appl._shdiv));
            final boolean diffKyoiku = !(null == currentkyoiku && null == appl._kyoiku || null != currentkyoiku && currentkyoiku.equals(appl._kyoiku));
            final boolean diffCourse = !(null == currentExamcourse && null == appl._examcourse || null != currentExamcourse && currentExamcourse.equals(appl._examcourse));
            if (null == current || diffShDiv || diffKyoiku || diffCourse) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(appl);
            currentShdiv = appl._shdiv;
            currentkyoiku = appl._kyoiku;
            currentExamcourse = appl._examcourse;
        }
        return rtn;
    }

    private static List getDivListOrder2(final List list) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Applicant appl = (Applicant) it.next();
            if (null == current) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(appl);
        }
        return rtn;
    }

    private static List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Applicant appl = (Applicant) it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(appl);
        }
        return rtn;
    }

    private static class Applicant {
        final String _examcourse;
        final String _examcourseName;
        final String _kyoikuDiv; // '2'は'▲'、'3'は'×'(BDETAIL9.REMARK9 = '2') それ以外は1
        final String _kyoiku;
        final String _kyoikuName;
        final String _sucExamcourseName;
        final String _shdiv;
        final String _shdivName;
        final String _examno;
        final String _name;
        final String _finschoolName;
        final String _kessekiJitaiName;
        final String _total4;
        final String _divRank4;
        final String _examcourseName2;
        final String _remark7;
        final String _remark7Name;
        final String _remark1;
        final String _remark2;
        final String _remark3;
        final String _remark4;
        final String _remark8;
        final String _remark8Name;
        final String _remark5;
        final String _remark6;
        final String _clubName;
        final String _totalAll;
        final String _total5;
        final String _kasantenAll;
        final Map _subclassScoreMap = new HashMap();

        Applicant(
            final String examcourse,
            final String examcourseName,
            final String kyoikuDiv,
            final String kyoiku,
            final String kyoikuName,
            final String sucExamcourseName,
            final String shdiv,
            final String shdivName,
            final String examno,
            final String name,
            final String finschoolName,
            final String kessekiJitaiName,
            final String total4,
            final String divRank4,
            final String examcourseName2,
            final String remark7,
            final String remark7Name,
            final String remark1,
            final String remark2,
            final String remark3,
            final String remark4,
            final String remark8,
            final String remark8Name,
            final String remark5,
            final String remark6,
            final String clubName,
            final String totalAll,
            final String total5,
            final String kasantenAll
        ) {
            _examcourse = examcourse;
            _examcourseName = examcourseName;
            _kyoikuDiv = kyoikuDiv;
            _kyoiku = kyoiku;
            _kyoikuName = kyoikuName;
            _sucExamcourseName = sucExamcourseName;
            _shdiv = shdiv;
            _shdivName = shdivName;
            _examno = examno;
            _name = name;
            _finschoolName = finschoolName;
            _kessekiJitaiName = kessekiJitaiName;
            _total4 = total4;
            _divRank4 = divRank4;
            _examcourseName2 = examcourseName2;
            _remark7 = remark7;
            _remark7Name = remark7Name;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _remark4 = remark4;
            _remark8 = remark8;
            _remark8Name = remark8Name;
            _remark5 = remark5;
            _remark6 = remark6;
            _clubName = clubName;
            _totalAll = totalAll;
            _total5 = total5;
            _kasantenAll = kasantenAll;
        }

        public static List getApplicantList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            Map applMap = new HashMap();
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String examno = rs.getString("EXAMNO");
                    if (null == applMap.get(examno)) {
                        final String examcourse = rs.getString("EXAMCOURSE");
                        final String examcourseName = rs.getString("EXAMCOURSE_NAME");
                        final String kyoikuDiv = rs.getString("KYOIKU_DIV");
                        final String kyoiku = rs.getString("KYOIKU");
                        final String kyoikuName = rs.getString("KYOIKU_NAME");
                        final String sucExamcourseName = rs.getString("SUC_EXAMCOURSE_NAME");
                        final String shdiv = rs.getString("SHDIV");
                        final String shdivName = rs.getString("SHDIV_NAME");
                        final String name = rs.getString("NAME");
                        final String finschoolName = rs.getString("FINSCHOOL_NAME");
                        final String kessekiJitaiName = rs.getString("KESSEKI_JITAI_NAME");
                        final String total4 = rs.getString("TOTAL4");
                        final String divRank4 = rs.getString("DIV_RANK4");
                        final String examcourseName2 = rs.getString("EXAMCOURSE_NAME2");
                        final String remark7 = rs.getString("REMARK7");
                        final String remark7Name = rs.getString("REMARK7_NAME");
                        final String remark1 = rs.getString("REMARK1");
                        final String remark2 = rs.getString("REMARK2");
                        final String remark3 = rs.getString("REMARK3");
                        final String remark4 = rs.getString("REMARK4");
                        final String remark8 = rs.getString("REMARK8");
                        final String remark8Name = rs.getString("REMARK8_NAME");
                        final String remark5 = rs.getString("REMARK5");
                        final String remark6 = rs.getString("REMARK6");
                        final String clubName = rs.getString("CLUB_NAME");
                        final String totalAll = rs.getString("TOTAL_ALL");
                        final String total5 = rs.getString("TOTAL5");
                        final String kasantenAll = rs.getString("KASANTEN_ALL");
                        final Applicant applicant = new Applicant(examcourse, examcourseName, kyoikuDiv, kyoiku, kyoikuName, sucExamcourseName, shdiv, shdivName, examno, name, finschoolName, kessekiJitaiName, total4, divRank4, examcourseName2, remark7, remark7Name, remark1, remark2, remark3, remark4, remark8, remark8Name, remark5, remark6, clubName, totalAll, total5, kasantenAll);
                        list.add(applicant);
                        applMap.put(examno, applicant);
                    }

                    final String testsubclasscd = rs.getString("TESTSUBCLASSCD");

                    if (null != testsubclasscd) {
                        final String attendFlg = rs.getString("ATTEND_FLG");
                        final String score = rs.getString("SCORE");
                        final Applicant appl = (Applicant) applMap.get(examno);
                        appl._subclassScoreMap.put(testsubclasscd, "0".equals(attendFlg) ? "*" : score);
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
            stb.append(" SELECT  ");
            stb.append("      BDETAIL1.REMARK8 || BDETAIL1.REMARK9 || BDETAIL1.REMARK10 AS EXAMCOURSE,  ");
            stb.append("      CRS1.EXAMCOURSE_NAME, ");
            stb.append("      CASE WHEN BDETAIL9.REMARK9 = '1' THEN 1 ");
            stb.append("           WHEN BDETAIL9.REMARK9 = '3' THEN 2 ");
            stb.append("           WHEN BDETAIL9.REMARK9 = '4' THEN 3 ");
            stb.append("           WHEN BDETAIL9.REMARK9 = '2' THEN 4 ");
            stb.append("           ELSE 5 ");
            stb.append("      END AS KYOIKU_SORT, ");
            stb.append("      CASE WHEN BDETAIL9.REMARK9 = '4' THEN 2 ");
            stb.append("           WHEN BDETAIL9.REMARK9 = '2' THEN 3 ");
            stb.append("           ELSE 1 ");
            stb.append("      END AS KYOIKU_DIV, ");
            stb.append("      BDETAIL9.REMARK9 AS KYOIKU,  ");
            stb.append("      NML026.NAME1 AS KYOIKU_NAME, ");
            stb.append("      CRS2.EXAMCOURSE_NAME AS SUC_EXAMCOURSE_NAME, ");
            stb.append("      BASE.SHDIV, ");
            stb.append("      NML006.NAME1 AS SHDIV_NAME, ");
            stb.append("      BASE.EXAMNO,  ");
            stb.append("      BASE.NAME,  ");
            stb.append("      FIN.FINSCHOOL_NAME_ABBV AS FINSCHOOL_NAME,  ");
            stb.append("      NML013.NAME1 AS KESSEKI_JITAI_NAME, ");
            stb.append("      RECEPT.TOTAL4,  ");
            stb.append("      RECEPT.DIV_RANK4,  ");
            stb.append("      CRS17.EXAMCOURSE_NAME AS EXAMCOURSE_NAME2, ");
            stb.append("      BDETAIL9.REMARK7,  ");
            stb.append("      NML025.NAME1 AS REMARK7_NAME, ");
            stb.append("      BDETAIL9.REMARK1,  ");
            stb.append("      BDETAIL9.REMARK2,  ");
            stb.append("      BDETAIL9.REMARK3,  ");
            stb.append("      BDETAIL9.REMARK4,  ");
            stb.append("      BDETAIL9.REMARK8,  ");
            stb.append("      NML025_2.NAME1 AS REMARK8_NAME, ");
            stb.append("      BDETAIL9.REMARK5,  ");
            stb.append("      BDETAIL9.REMARK6,  ");
            stb.append("      BDETAIL4.REMARK1 AS CLUB_NAME, ");
            stb.append("      CONFRPT.TOTAL_ALL,  ");
            stb.append("      CONFRPT.TOTAL5,  ");
            stb.append("      CONFRPT.KASANTEN_ALL,  ");
            stb.append("      TSCORE.TESTSUBCLASSCD AS TESTSUBCLASSCD,  ");
            stb.append("      TSCORE.ATTEND_FLG,  ");
            stb.append("      TSCORE.SCORE  ");
            stb.append("  FROM  ");
            stb.append("      ENTEXAM_APPLICANTBASE_DAT BASE  ");
            stb.append("      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL1 ON BDETAIL1.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("          AND BDETAIL1.EXAMNO = BASE.EXAMNO  ");
            stb.append("          AND BDETAIL1.SEQ = '001'  ");
            stb.append("      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL4 ON BDETAIL4.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("          AND BDETAIL4.EXAMNO = BASE.EXAMNO  ");
            stb.append("          AND BDETAIL4.SEQ = '004'  ");
            stb.append("      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL9 ON BDETAIL9.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("          AND BDETAIL9.EXAMNO = BASE.EXAMNO  ");
            stb.append("          AND BDETAIL9.SEQ = '009'  ");
            stb.append("      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL17 ON BDETAIL17.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("          AND BDETAIL17.EXAMNO = BASE.EXAMNO  ");
            stb.append("          AND BDETAIL17.SEQ = '017'  ");
            stb.append("      LEFT JOIN ENTEXAM_COURSE_MST CRS1 ON CRS1.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("          AND CRS1.APPLICANTDIV = BASE.APPLICANTDIV  ");
            stb.append("          AND CRS1.TESTDIV = BASE.TESTDIV  ");
            stb.append("          AND CRS1.COURSECD = BDETAIL1.REMARK8  ");
            stb.append("          AND CRS1.MAJORCD = BDETAIL1.REMARK9  ");
            stb.append("          AND CRS1.EXAMCOURSECD = BDETAIL1.REMARK10 ");
            stb.append("      LEFT JOIN ENTEXAM_COURSE_MST CRS2 ON CRS2.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("          AND CRS2.APPLICANTDIV = BASE.APPLICANTDIV  ");
            stb.append("          AND CRS2.TESTDIV = BASE.TESTDIV  ");
            stb.append("          AND CRS2.COURSECD = BASE.SUC_COURSECD ");
            stb.append("          AND CRS2.MAJORCD = BASE.SUC_MAJORCD  ");
            stb.append("          AND CRS2.EXAMCOURSECD = BASE.SUC_COURSECODE ");
            stb.append("      LEFT JOIN ENTEXAM_COURSE_MST CRS17 ON CRS17.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("          AND CRS17.APPLICANTDIV = BASE.APPLICANTDIV  ");
            stb.append("          AND CRS17.TESTDIV = BASE.TESTDIV  ");
            stb.append("          AND CRS17.COURSECD = BDETAIL17.REMARK1  ");
            stb.append("          AND CRS17.MAJORCD = BDETAIL17.REMARK2  ");
            stb.append("          AND CRS17.EXAMCOURSECD = BDETAIL17.REMARK3 ");
            stb.append("      LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("          AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV  ");
            stb.append("          AND RECEPT.TESTDIV = BASE.TESTDIV  ");
            stb.append("          AND RECEPT.EXAM_TYPE = '1'  ");
            stb.append("          AND RECEPT.EXAMNO = BASE.EXAMNO  ");
            stb.append("      LEFT JOIN ENTEXAM_SCORE_DAT TSCORE ON TSCORE.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("          AND TSCORE.APPLICANTDIV = BASE.APPLICANTDIV  ");
            stb.append("          AND TSCORE.TESTDIV = BASE.TESTDIV  ");
            stb.append("          AND TSCORE.EXAM_TYPE = RECEPT.EXAM_TYPE  ");
            stb.append("          AND TSCORE.RECEPTNO = RECEPT.RECEPTNO  ");
            stb.append("      LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = BASE.FS_CD  ");
            stb.append("      LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ON CONFRPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("          AND CONFRPT.EXAMNO = BASE.EXAMNO  ");
            stb.append("      LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013'  ");
            stb.append("          AND NML013.NAMECD2 = BASE.JUDGEMENT ");
            stb.append("          AND BASE.JUDGEMENT IN ('4', '5') ");
            stb.append("      LEFT JOIN NAME_MST NML006 ON NML006.NAMECD1 = 'L006'  ");
            stb.append("          AND NML006.NAMECD2 = BASE.SHDIV ");
            stb.append("      LEFT JOIN NAME_MST NML026 ON NML026.NAMECD1 = 'L026'  ");
            stb.append("          AND NML026.NAMECD2 = BDETAIL9.REMARK9 ");
            stb.append("      LEFT JOIN NAME_MST NML025 ON NML025.NAMECD1 = 'L025'  ");
            stb.append("          AND NML025.NAMECD2 = BDETAIL9.REMARK7 ");
            stb.append("      LEFT JOIN NAME_MST NML025_2 ON NML025_2.NAMECD1 = 'L025'  ");
            stb.append("          AND NML025_2.NAMECD2 = BDETAIL9.REMARK8 ");
            stb.append("  WHERE  ");
            stb.append("      BASE.ENTEXAMYEAR = '" + param._entexamyear + "'  ");
            stb.append("      AND BASE.APPLICANTDIV = '" + param._applicantdiv + "'  ");
            stb.append("      AND BASE.TESTDIV = '" + param._testdiv + "'  ");
            if (null != param._scoreTotal) {
                stb.append("      AND VALUE(RECEPT.TOTAL4, -1) <= " + param._scoreTotal + "  ");
                stb.append("      AND VALUE(BDETAIL9.REMARK9, '') <> '2' "); // 教育相談×は表示しない
            }
            stb.append("  ORDER BY  ");
            stb.append("      BASE.SHDIV,  ");
            if ("1".equals(param._order)) {
            	stb.append("      KYOIKU_SORT,  ");
            	stb.append("      BDETAIL1.REMARK10,  ");
                stb.append("      VALUE(RECEPT.TOTAL4, -1) DESC,  ");
            }
            stb.append("      BASE.EXAMNO  ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65201 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;
        final String _scoreTotal;
        final String _order;

        final String _applicantdivAbbv1;
        final String _testdivAbbv1;
        final String _dateStr;
        final Map _testSubclassNameMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _order = request.getParameter("ORDER");
            _scoreTotal = NumberUtils.isDigits(request.getParameter("SCORE_TOTAL")) ? request.getParameter("SCORE_TOTAL") : null;
            _date = request.getParameter("CTRL_DATE").replace('/', '-');
            _dateStr = getDateStr(db2, _date);

            _applicantdivAbbv1 = getNameMst(db2, "ABBV1", "L003", _applicantdiv);
            _testdivAbbv1 = getNameMst(db2, "ABBV1", "L004", _testdiv);
            _testSubclassNameMap = getTestSubclassName(db2);
        }

        private String getDateStr(final DB2UDB db2, final String date) {
//            final Calendar cal = Calendar.getInstance();
//            final DecimalFormat df = new DecimalFormat("00");
//            final int hour = cal.get(Calendar.HOUR_OF_DAY);
//            final int minute = cal.get(Calendar.MINUTE);
//            return KNJ_EditDate.h_format_JP(date) + "　" + df.format(hour) + "時" + df.format(minute) + "分現在";
            return KNJ_EditDate.h_format_JP(db2, date);
        }

        private Map getTestSubclassName(final DB2UDB db2) {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'L009' ";
//                if ("1".equals(_testdiv)) {
//                    sql += " AND NAMESPARE2 = '1' ";
//                } else if ("2".equals(_testdiv)) {
//                    sql += " AND NAMESPARE3 = '1' ";
//                }
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
    }
}

// eof

