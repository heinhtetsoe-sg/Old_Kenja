/*
 * $Id: aae28715359257b293bffdd81213b2a1b87815e5 $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２６Ｎ＞  中学校宛結果通知
 **/
public class KNJL326N {

    private static final Log log = LogFactory.getLog(KNJL326N.class);

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

    public void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List list = Finschool.load(db2, _param);

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Finschool finschool = (Finschool) it.next();

            if ("2".equals(_param._outputKind)) {
                svf.VrSetForm("KNJL326N_3.frm", 1);

                svf.VrsOut("ZIPNO", finschool._finschoolZipcd); // 郵便番号

                final String address1 = replaceHyphenMinus(finschool._finschoolAddr1);
                final String address2 = replaceHyphenMinus(finschool._finschoolAddr2);
                if (StringUtils.defaultString(address1).length() > 20 || StringUtils.defaultString(address2).length() > 20) {
                    svf.VrsOut("ADDR1_2", address1); // 住所２
                    svf.VrsOut("ADDR2_2", address2); // 住所２
                } else {
                    svf.VrsOut("ADDR1_1", address1); // 住所１
                    svf.VrsOut("ADDR2_1", address2); // 住所１
                }
                final String name1 = replaceHyphenMinus(finschool._finschoolName);
                if (StringUtils.defaultString(name1).length() > 20) {
                    svf.VrsOut("NAME1_2", name1); // 氏名
                } else {
                    svf.VrsOut("NAME1_1", name1); // 氏名
                }
                //final String name2 = replaceHyphenMinus("校長　" + StringUtils.defaultString(finschool._princname) + "　様");
                final String name2 = "　学　校　長　様";
                if (StringUtils.defaultString(name2).length() > 20) {
                    svf.VrsOut("NAME2_2", name2); // 氏名
                } else {
                    svf.VrsOut("NAME2_1", name2); // 氏名
                }

                svf.VrEndPage();
            } else {
                svf.VrSetForm("KNJL326N_1.frm", 1);

                svf.VrsOut("DATE", _param._dateStr); // 日付
                svf.VrsOut("FINSCHOOL_NAME", finschool._finschoolName); // 出身集学校名
//                svf.VrsOut("FINSCHOOL_PRINCNAME", "校長　" + StringUtils.defaultString(finschool._princname) + "　様"); // 出身学校校長名
                svf.VrsOut("FINSCHOOL_PRINCNAME", "学　校　長　様"); // 出身学校校長名
                svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
                svf.VrsOut("SCHOOL_PRINCNAME", "校長　" + StringUtils.defaultString(_param._principalName)); // 校長名
                svf.VrsOut("NENDO", "　さて、" + KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度本校入学試験につきましては、格別のご支援"); // 年度
                svf.VrEndPage();

                final int maxLine = 15;
                final List pageList = getPageList(finschool._applicantList, maxLine);

                String title = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度　" + StringUtils.defaultString(_param._schoolName) + " 入試選考結果";
                if ("1".equals(_param._testdiv)) {
                    title += "(A日程)";
                } else if ("2".equals(_param._testdiv)) {
                    title += "(B日程)";
                }

                for (int pi = 0; pi < pageList.size(); pi++) {
                    final List applicantList = (List) pageList.get(pi);

                    svf.VrSetForm("KNJL326N_2.frm", 1);

                    svf.VrsOut("DATE", _param._dateStr); // 日付
                    svf.VrsOut("TITLE", title); // タイトル
                    svf.VrsOut("FINSCHOOL_NAME", finschool._finschoolName); // 出身学校名
                    svf.VrsOut("FINSCHOOL_CD", finschool._fsCd); // 出身学校コード

                    for (int j = 0; j < applicantList.size(); j++) {
                        final Applicant appl = (Applicant) applicantList.get(j);
                        final int line = j + 1;
                        svf.VrsOutn("COURSE_NAME", line, StringUtils.defaultString(appl._majorname) + "・" + StringUtils.defaultString(appl._examcourseName)); // コース名
                        svf.VrsOutn("EXAM_NO", line, appl._examno); // 受験番号
                        svf.VrsOutn("NAME", line, appl._name); // 氏名
                        svf.VrsOutn("JUDGE", line, appl._judgementName); // 合否

                        final StringBuffer biko = new StringBuffer();
                        if (!StringUtils.isBlank(appl._biko)) {
                            biko.append(appl._biko);
                        }
                        if (!StringUtils.isBlank(appl._remark8Name)) {
                            if (biko.length() > 0) {
                                biko.append("\n");
                            }
                            biko.append(appl._remark8Name);
                        }
                        if (!StringUtils.isBlank(appl._remark5)) {
                            if (biko.length() > 0) {
                                biko.append("\n");
                            }
                            biko.append("（").append(appl._remark5).append("）");
                        }
                        final List bikoLineList = new ArrayList();
                        final String[] tokens = KNJ_EditEdit.get_token(biko.toString(), 26, 99);
                        if (null != tokens) {
                            for (int i = 0; i < tokens.length; i++) {
                                if (!StringUtils.isBlank(tokens[i])) {
                                    bikoLineList.add(tokens[i]);
                                }
                            }
                        }
                        final String bikoFieldN = (bikoLineList.size() > 2) ? "2" : "1";
                        for (int i = 0; i < bikoLineList.size(); i++) {
                            final String text = (String) bikoLineList.get(i);
                            svf.VrsOutn("REMARK" + bikoFieldN + "_" + String.valueOf(i + 1), line, text); // 備考
                        }
                    }
                    svf.VrEndPage();
                }
            }

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

    // 横線を縦書きで縦線に表示されるように変換する
    private String replaceHyphenMinus(final String s) {
        if (null == s) {
            return null;
        }
        final char hyphenMinus = '-'; // NG−
//        final char zenkakuHyphen = '‐'; // OK
        final char minus =  '\u2212'; // NG
        final char zenkakuMinus = '\uFF0D'; // NG
        final char zenkakuDash =  '\u2015'; // OK
        return s.replace(minus, zenkakuDash).replace(hyphenMinus, zenkakuDash).replace(zenkakuMinus, zenkakuDash);
    }

    private static class Finschool {
        final String _fsCd;
        final String _finschoolName;
        final String _finschoolZipcd;
        final String _finschoolAddr1;
        final String _finschoolAddr2;
        final String _princname;
        final List _applicantList;

        Finschool(
            final String fsCd,
            final String finschoolName,
            final String finschoolZipcd,
            final String finschoolAddr1,
            final String finschoolAddr2,
            final String princname
        ) {
            _fsCd = fsCd;
            _finschoolName = finschoolName;
            _finschoolZipcd = finschoolZipcd;
            _finschoolAddr1 = finschoolAddr1;
            _finschoolAddr2 = finschoolAddr2;
            _princname = princname;
            _applicantList = new ArrayList();
        }

        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            final Map finschoolMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String fsCd = rs.getString("FS_CD");
                    if (null == finschoolMap.get(fsCd)) {
                        final String princname = rs.getString("PRINCNAME");
                        final Finschool finschool = new Finschool(fsCd, rs.getString("FINSCHOOL_NAME"), rs.getString("FINSCHOOL_ZIPCD"), rs.getString("FINSCHOOL_ADDR1"), rs.getString("FINSCHOOL_ADDR2"), princname);
                        finschoolMap.put(fsCd, finschool);
                        list.add(finschool);
                    }

                    final Finschool finschool = (Finschool) finschoolMap.get(fsCd);
                    final String examno = rs.getString("EXAMNO");
                    final String name = rs.getString("NAME");
                    final String judgement = rs.getString("JUDGEMENT");
                    final String examcourseName = rs.getString("EXAMCOURSE_NAME");
                    final String majorname = rs.getString("MAJORNAME");
                    final String judgementName = rs.getString("JUDGEMENT_NAME");
                    final String biko = rs.getString("BIKO");
                    final String remark5 = rs.getString("REMARK5");
                    final String remark8Name = rs.getString("REMARK8_NAME");
                    final Applicant applicant = new Applicant(examno, name, judgement, examcourseName, majorname, judgementName, biko, remark5, remark8Name);
                    finschool._applicantList.add(applicant);
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
            stb.append("     BASE.FS_CD, ");
            stb.append("     FIN.FINSCHOOL_NAME, ");
            stb.append("     FIN.FINSCHOOL_ZIPCD, ");
            stb.append("     FIN.FINSCHOOL_ADDR1, ");
            stb.append("     FIN.FINSCHOOL_ADDR2, ");
            stb.append("     FIN.PRINCNAME, ");
            stb.append("     BASE.EXAMNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.JUDGEMENT, ");
            stb.append("     CRS1.EXAMCOURSE_NAME, ");
            stb.append("     MAJ.MAJORNAME, ");
            stb.append("     CASE WHEN NML013.NAMESPARE1 = '1' THEN '合格' ELSE NML013.NAME1 END AS JUDGEMENT_NAME, ");
            stb.append("     CASE WHEN BASE.JUDGEMENT = '3' THEN ");
            stb.append("         CONCAT(CONCAT(VALUE(MAJ.MAJORNAME || '・', ''), CRS2.EXAMCOURSE_NAME), 'にまわし合格') ");
            stb.append("     END AS BIKO, ");
            stb.append("     BDETAIL9.REMARK5, ");
            stb.append("     NML025.NAME1 AS REMARK8_NAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND RECEPT.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND RECEPT.EXAMNO = BASE.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL1 ON BDETAIL1.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND BDETAIL1.EXAMNO = BASE.EXAMNO ");
            stb.append("         AND BDETAIL1.SEQ = '001'  ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL9 ON BDETAIL9.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND BDETAIL9.EXAMNO = BASE.EXAMNO ");
            stb.append("         AND BDETAIL9.SEQ = '009'  ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST CRS1 ON CRS1.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("         AND CRS1.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND CRS1.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND CRS1.COURSECD = BDETAIL1.REMARK8 ");
            stb.append("         AND CRS1.MAJORCD = BDETAIL1.REMARK9 ");
            stb.append("         AND CRS1.EXAMCOURSECD = BDETAIL1.REMARK10  ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST CRS2 ON CRS2.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("         AND CRS2.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND CRS2.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND CRS2.COURSECD = BASE.SUC_COURSECD ");
            stb.append("         AND CRS2.MAJORCD = BASE.SUC_MAJORCD ");
            stb.append("         AND CRS2.EXAMCOURSECD = BASE.SUC_COURSECODE ");
            stb.append("     LEFT JOIN V_MAJOR_MST MAJ ON MAJ.YEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND MAJ.COURSECD = BDETAIL1.REMARK8 ");
            stb.append("         AND MAJ.MAJORCD = BDETAIL1.REMARK9 ");
            stb.append("     LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = BASE.FS_CD ");
            stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' ");
            stb.append("         AND NML013.NAMECD2 = BASE.JUDGEMENT ");
            stb.append("     LEFT JOIN NAME_MST NML025 ON NML025.NAMECD1 = 'L025' ");
            stb.append("         AND NML025.NAMECD2 = BDETAIL9.REMARK8 ");
            stb.append(" WHERE ");
            stb.append("     BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND BASE.TESTDIV = '" + param._testdiv + "' ");
            if ("2".equals(param._output)) {
                stb.append("     AND BASE.FS_CD = '" + param._schoolcd + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     BASE.FS_CD, ");
            stb.append("     BASE.EXAMNO ");
            return stb.toString();
        }
    }

    private static class Applicant {
        final String _examno;
        final String _name;
        final String _judgement;
        final String _examcourseName;
        final String _majorname;
        final String _judgementName;
        final String _biko;
        final String _remark5;
        final String _remark8Name;

        Applicant(
            final String examno,
            final String name,
            final String judgement,
            final String examcourseName,
            final String majorname,
            final String judgementName,
            final String biko,
            final String remark5,
            final String remark8Name
        ) {
            _examno = examno;
            _name = name;
            _judgement = judgement;
            _examcourseName = examcourseName;
            _majorname = majorname;
            _judgementName = judgementName;
            _biko = biko;
            _remark5 = remark5;
            _remark8Name = remark8Name;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 58134 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;
        final String _outputKind;
        final String _output;
        final String _schoolcd;
        final String _documentroot;

        final String _testdivName1;
        final String _testdivAbbv1;
        final String _dateStr;
        final String _schoolName;
        final String _principalName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("TSUCHI_DATE");
            _dateStr = null == _date ? null : getDateStr(_date.replace('/', '-'));
            _outputKind = request.getParameter("OUTPUT_KIND");
            _output = request.getParameter("OUTPUT");
            _schoolcd = request.getParameter("SCHOOLCD");
            _documentroot = request.getParameter("DOCUMENTROOT");

            _testdivName1 = getNameMst(db2, "NAME1", "L004", _testdiv);
            _testdivAbbv1 = getNameMst(db2, "ABBV1", "L004", _testdiv);
            _schoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _principalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
        }

        private String getDateStr(final String date) {
            if (null == date) {
                return null;
            }
            return KNJ_EditDate.h_format_JP(date);
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

        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer sql = new StringBuffer();
                sql.append(" SELECT " + field + " ");
                sql.append(" FROM CERTIF_SCHOOL_DAT ");
                sql.append(" WHERE YEAR = '" + _entexamyear + "' ");
                sql.append("   AND CERTIF_KINDCD = '106' ");
                ps = db2.prepareStatement(sql.toString());
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

