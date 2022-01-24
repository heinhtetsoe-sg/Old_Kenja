/*
 * $Id: c5b86eacb21d8682ebb098ab26469e79195660fd $
 *
 * 作成日: 2017/07/06
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

public class KNJL327W {

    private static final Log log = LogFactory.getLog(KNJL327W.class);

    private static final String KEKKA = "1";
    private static final String GOUKAKU_NAITEI = "2";
    private static final String KAIJI = "3";
    private static final String GOUKAKU_TSUCHI = "4";
   
    private static final String ZENKI = "1";
    private static final String RENKEI = "2";
    private static final String SPORT = "4";
    private static final String TOKUBETU = "3";

    private static final String COURSE_NASHI = "0000";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        if (KEKKA.equals(_param._style)) {
            printForm1(db2, svf);
        } else if (GOUKAKU_NAITEI.equals(_param._style)) {
            printForm2(db2, svf);
        } else if (KAIJI.equals(_param._style)) {
            printForm3(db2, svf);
        } else if (GOUKAKU_TSUCHI.equals(_param._style)) {
            printForm4(db2, svf);
        }
    }

    private void printForm1(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        String groupField = "FS_CD";

        final int maxLineForm1 = 25;
        final int maxLine = 2500;
        final List pageList = getPageList(groupField, getList(db2, sql1()), maxLine);
        final String form1 = "KNJL327W_1.frm";
        final String form2 = "KNJL327W_1_2.frm";

        final String kisaiDate = KNJ_EditEdit.convertZenkakuSuuji(KNJ_EditDate.h_format_JP(db2, _param._kisaiDate));

        String strdir1 = "circle.bmp";
        String strdir2 = _param._documentroot + "/" + _param._folder + "/" + strdir1;
        File f1 = new File(strdir2);   //丸データ存在チェック用

        //学校ごと
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List dataList = (List) pageList.get(pi);
            final Map row0 = (Map) dataList.get(0);

            svf.VrSetForm(form1, 4);
            //ヘッダー部
            svf.VrsOut("DATE", kisaiDate); //記載日
            svf.VrsOut("JSCHOOL_NAME", StringUtils.defaultString(getString(row0, "FINSCHOOL_NAME")) + "長　様"); //学校名(中学)＋"長　様"
            svf.VrsOut("HSCHOOL_NAME", _param._schoolName + "長"); //学校名(高校)＋"長"
            //本文
            setTextForm1(db2, svf);
            //丸
            if (f1.exists()) {
                if (ZENKI.equals(_param._testDiv)) {
                    svf.VrsOut("CIRCLE1", strdir2);
                    svf.VrsOut("CIRCLE1_2", strdir2);
                } else if (RENKEI.equals(_param._testDiv)) {
                    svf.VrsOut("CIRCLE2", strdir2);
                    svf.VrsOut("CIRCLE2_2", strdir2);
                } else if (SPORT.equals(_param._testDiv)) {
                    svf.VrsOut("CIRCLE3", strdir2);
                    svf.VrsOut("CIRCLE3_2", strdir2);
                }
            } else {
                System.out.println("not bmp fail");
            }

            //データ部
            for (int j = 0; j < dataList.size(); j++) {
                final Map row = (Map) dataList.get(j);

                if (j == maxLineForm1) {
                    svf.VrSetForm(form2, 4);
                }

                final String setMC = getString(row, "MAJOR_COURSE");
                final String mcField = getMS932ByteLength(setMC) <= 14 ? "1" : getMS932ByteLength(setMC) <= 20 ? "2" : "3";
                svf.VrsOut("DEPART_NAME" + mcField, setMC); //学科・コース
                svf.VrsOut("EXAM_NO", getString(row, "EXAMNO")); //受検番号
                final String setName = getString(row, "NAME");
                final String nameField = getMS932ByteLength(setName) <= 32 ? "1" : getMS932ByteLength(setName) <= 40 ? "2" : "3";
                svf.VrsOut("NAME" + nameField, setName); //志願者名
                svf.VrsOut("JUDGE", getString(row, "JUDGE")); //合否

                svf.VrEndRecord();
                _hasData = true;
            }
        }
    }

    private void printForm2(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        String groupField = null;

        final int maxLine = 1;
        final List pageList = getPageList(groupField, getList(db2, sql2()), maxLine);
        final String form = "KNJL327W_2.frm";
        svf.VrSetForm(form, 1);

        final String kisaiDate = KNJ_EditEdit.convertZenkakuSuuji(KNJ_EditDate.h_format_JP(db2, _param._kisaiDate));

        String strdir1 = "circle.bmp";
        String strdir2 = _param._documentroot + "/" + _param._folder + "/" + strdir1;
        File f1 = new File(strdir2);   //丸データ存在チェック用

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List dataList = (List) pageList.get(pi);
            final Map row0 = (Map) dataList.get(0);

            //ヘッダー部
            svf.VrsOut("DATE", kisaiDate); //記載日
            svf.VrsOut("JSCHOOL_NAME", getString(row0, "FINSCHOOL_NAME")); //学校名(中学)
            svf.VrsOut("HSCHOOL_NAME", _param._schoolName + "長"); //学校名(高校)＋"長"
            svf.VrsOut("EXAM_NO", getString(row0, "EXAMNO")); //受検番号
            final String setName = getString(row0, "NAME") + "　様";
            final String nameField = getMS932ByteLength(setName) <= 32 ? "1" : getMS932ByteLength(setName) <= 40 ? "2" : "3";
            svf.VrsOut("NAME" + nameField, setName); //名前
            //本文
            setTextForm2(db2, svf, row0);
            //丸
            if (f1.exists()) {
                if (ZENKI.equals(_param._testDiv)) {
                    svf.VrsOut("CIRCLE1", strdir2);
                    svf.VrsOut("CIRCLE1_2", strdir2);
                } else if (RENKEI.equals(_param._testDiv)) {
                    svf.VrsOut("CIRCLE2", strdir2);
                    svf.VrsOut("CIRCLE2_2", strdir2);
                } else if (SPORT.equals(_param._testDiv)) {
                    svf.VrsOut("CIRCLE3", strdir2);
                    svf.VrsOut("CIRCLE3_2", strdir2);
                } else if (TOKUBETU.equals(_param._testDiv)) {
                    svf.VrsOut("CIRCLE4", strdir2);
                    svf.VrsOut("CIRCLE4_2", strdir2);
                }
            } else {
                System.out.println("not bmp fail");
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void setTextForm1(final DB2UDB db2, final Vrw32alp svf) {
        final String zenkakuNendo = KNJ_EditEdit.convertZenkakuSuuji(KNJ_EditDate.h_format_JP_N(db2, _param._gengoBaseDate));
        svf.VrsOut("YEAR", "　" + zenkakuNendo + "度");
    }

    private void setTextForm2(final DB2UDB db2, final Vrw32alp svf, final Map row0) {
        final String zenkakuNendo = KNJ_EditEdit.convertZenkakuSuuji(KNJ_EditDate.h_format_JP_N(db2, _param._gengoBaseDate));
        svf.VrsOut("YEAR", zenkakuNendo + "度");
        svf.VrsOut("COURSE_NAME", getString(row0, "SUC_KATEI"));
        if (COURSE_NASHI.equals(getString(row0, "SUC_COURSECODE"))) {
            svf.VrsOut(getMS932ByteLength(getString(row0, "SUC_MAJOR")) > 24 ? "MAJOR_ONLY2" : "MAJOR_ONLY", getString(row0, "SUC_MAJOR"));
        } else {
        	if (getMS932ByteLength(getString(row0, "SUC_MAJOR")) > 24 || getMS932ByteLength(getString(row0, "SUC_COURSE")) > 24) {
                svf.VrsOut("MAJOR2", getString(row0, "SUC_MAJOR"));
                svf.VrsOut("COURSECODE_NAME2", getString(row0, "SUC_COURSE"));
        	} else {
                svf.VrsOut("MAJOR", getString(row0, "SUC_MAJOR"));
                svf.VrsOut("COURSECODE_NAME", getString(row0, "SUC_COURSE"));
        	}
        }

        final String sucDate = KNJ_EditEdit.convertZenkakuSuuji(KNJ_EditDate.h_format_JP(db2, _param._sucDate));
        final String sucWeek = KNJ_EditEdit.convertZenkakuSuuji(KNJ_EditDate.h_format_W(_param._sucDate));
        final String sucTime = KNJ_EditEdit.convertZenkakuSuuji(_param._sucHour + "時" + _param._sucMinute + "分");
        final String setText5 = "　　１　合格内定通知書の交付を受けた者については、" + sucDate + "（" + sucWeek + "）";
        final String setText6 = "　　　" + _param._sucAmPm + sucTime + "から志願先高等学校で、合格者として発表します。";
        final String setText7 = "　　２　合格内定通知書の交付を受けた者は、三重県立高等学校を改めて志願するこ";
        final String setText8 = "　　　とはできません。";
        svf.VrsOut("TEXT5", setText5);
        svf.VrsOut("TEXT6", setText6);
        svf.VrsOut("TEXT7", setText7);
        svf.VrsOut("TEXT8", setText8);
    }

    private static String getString(final Map m, final String field) {
        if (null == m || m.isEmpty()) {
            return null;
        }
        if (!m.containsKey(field)) {
            throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
        }
        return (String) m.get(field);
    }

    private static List getPageList(final String groupField, final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        String oldGroupVal = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map row = (Map) it.next();
            final boolean isDiffGroup = null != groupField && (null == oldGroupVal && null != getString(row, groupField) || null != oldGroupVal && !oldGroupVal.equals(getString(row, groupField)));
            if (null == current || current.size() >= max || isDiffGroup) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(row);
            if (null != groupField) {
                oldGroupVal = getString(row, groupField);
            }
        }
        return rtn;
    }

    private List getList(final DB2UDB db2, final String sql) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    m.put(meta.getColumnName(i), rs.getString(meta.getColumnName(i)));
                }
                list.add(m);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    //結果通知書
    private String sql1() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.FS_CD, ");
        stb.append("     F1.FINSCHOOL_NAME, ");
        stb.append("     CASE WHEN N1.NAMESPARE1 = '1' ");
        stb.append("          THEN M2.MAJORNAME || CASE WHEN VALUE(T1.LAST_DAI1_COURSECODE, '0000') = '" + COURSE_NASHI + "' THEN '' ELSE C2.EXAMCOURSE_NAME END ");
        stb.append("          ELSE M1.MAJORNAME || CASE WHEN VALUE(T1.LAST_DAI1_COURSECODE, '0000') = '" + COURSE_NASHI + "' THEN '' ELSE C1.EXAMCOURSE_NAME END ");
        stb.append("     END AS MAJOR_COURSE, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        if ("4".equals(_param._testDiv)) {
            stb.append("     CASE WHEN VALUE(T1.JUDGEMENT, '') = '3' THEN '不合格' ELSE N1.NAME1 END AS JUDGE ");
        } else if ("1".equals(_param._testDiv)) {
            stb.append("     CASE WHEN VALUE(T1.JUDGEMENT, '') = '3' THEN '合格' ELSE N1.NAME1 END AS JUDGE ");
        } else {
            stb.append("     N1.NAME1 AS JUDGE ");
        }
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = T1.FS_CD ");
        stb.append("     LEFT JOIN MAJOR_MST M1 ON M1.COURSECD = T1.LAST_DAI1_COURSECD AND M1.MAJORCD = T1.LAST_DAI1_MAJORCD ");
        stb.append("     LEFT JOIN MAJOR_MST M2 ON M2.COURSECD = T1.SUC_COURSECD AND M2.MAJORCD = T1.SUC_MAJORCD ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST C1 ");
        stb.append("          ON C1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND C1.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND C1.TESTDIV = T1.TESTDIV ");
        stb.append("         AND C1.COURSECD = T1.LAST_DAI1_COURSECD ");
        stb.append("         AND C1.MAJORCD = T1.LAST_DAI1_MAJORCD ");
        stb.append("         AND C1.EXAMCOURSECD = T1.LAST_DAI1_COURSECODE ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST C2 ");
        stb.append("          ON C2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND C2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND C2.TESTDIV = T1.TESTDIV ");
        stb.append("         AND C2.COURSECD = T1.SUC_COURSECD ");
        stb.append("         AND C2.MAJORCD = T1.SUC_MAJORCD ");
        stb.append("         AND C2.EXAMCOURSECD = T1.SUC_COURSECODE ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L013' AND N1.NAMECD2 = T1.JUDGEMENT ");
        stb.append(" WHERE ");
        stb.append("         T1.ENTEXAMYEAR  = '" + _param._entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if ("1".equals(_param._testDiv)) {
            stb.append("     AND (T1.TESTDIV = '" + _param._testDiv + "' ");
            stb.append("          OR ");
            stb.append("          (T1.TESTDIV = '4' AND VALUE(T1.JUDGEMENT, '') in ('2', '3') ) ");
            stb.append("         ) ");
        } else {
            stb.append("     AND T1.TESTDIV      = '" + _param._testDiv + "' ");
        }
        stb.append("     AND VALUE(T1.JUDGEMENT, '') != '5' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.FS_CD, ");
        stb.append("     T1.EXAMNO ");
        return stb.toString();
    }

    //合格内定通知書
    private String sql2() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     F1.FINSCHOOL_NAME, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.SUC_COURSECODE, ");
        stb.append("     K1.COURSENAME AS SUC_KATEI, ");
        stb.append("     M1.MAJORNAME AS SUC_MAJOR, ");
        stb.append("     C1.EXAMCOURSE_NAME AS SUC_COURSE ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = T1.FS_CD ");
        stb.append("     LEFT JOIN COURSE_MST K1 ON K1.COURSECD = T1.SUC_COURSECD ");
        stb.append("     LEFT JOIN MAJOR_MST M1 ON M1.COURSECD = T1.SUC_COURSECD AND M1.MAJORCD = T1.SUC_MAJORCD ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST C1 ");
        stb.append("          ON C1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND C1.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND C1.TESTDIV = T1.TESTDIV ");
        stb.append("         AND C1.COURSECD = T1.SUC_COURSECD ");
        stb.append("         AND C1.MAJORCD = T1.SUC_MAJORCD ");
        stb.append("         AND C1.EXAMCOURSECD = T1.SUC_COURSECODE ");
        stb.append(" WHERE ");
        stb.append("         T1.ENTEXAMYEAR  = '" + _param._entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.EXAMNO IN " + SQLUtils.whereIn(true, _param._category_selected) + " ");
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ");
        return stb.toString();
    }

    private void printForm3(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        final List studentList = getStudentList(db2);
        svf.VrSetForm("KNJL327W_3.frm", 1);


        for (Iterator itStudent = studentList.iterator(); itStudent.hasNext();) {
            final Student student = (Student) itStudent.next();

            //ヘッダー部
            svf.VrsOut("NAME", student._name + "　様");
            svf.VrsOut("EXAM_NO", student._examNo + "　番");
            svf.VrsOut("SCHOOL_NAME", _param._schoolName); //学校名(高校)＋"長"
            svf.VrsOut("DEPARTMENT_NAME", student._courseName);
            svf.VrsOut("DIV", student._testDivName + student._testDiv2Name);

            int tyousaCnt = 1;
            for (Iterator itTyousa = _param._nameL008List.iterator(); itTyousa.hasNext();) {
                final Kamoku kamoku = (Kamoku) itTyousa.next();
                svf.VrsOutn("CLASS_NAME1", tyousaCnt, kamoku._name);
                tyousaCnt++;
            }
            svf.VrsOutn("VAL1", 1, student._rpt01);
            svf.VrsOutn("VAL1", 2, student._rpt02);
            svf.VrsOutn("VAL1", 3, student._rpt03);
            svf.VrsOutn("VAL1", 4, student._rpt04);
            svf.VrsOutn("VAL1", 5, student._rpt05);
            svf.VrsOutn("VAL1", 6, student._rpt06);
            svf.VrsOutn("VAL1", 7, student._rpt07);
            svf.VrsOutn("VAL1", 8, student._rpt08);
            svf.VrsOutn("VAL1", 9, student._rpt09);

            int gakuCnt = 1;
            for (Iterator itGaku = _param._nameL057GakuList.iterator(); itGaku.hasNext();) {
                final Kamoku kamoku = (Kamoku) itGaku.next();
                svf.VrsOutn("CLASS_NAME2", gakuCnt, kamoku._name);
                if (student._kaijiMap.containsKey(kamoku._kamokuCd)) {
                    final String score = (String) student._kaijiMap.get(kamoku._kamokuCd);
                    svf.VrsOutn("VAL2", gakuCnt, StringUtils.defaultString(score));
                }
                gakuCnt++;
            }

            int sonotaCnt = 1;
            for (Iterator itSonota = _param._nameL057SonotaList.iterator(); itSonota.hasNext();) {
                final Kamoku kamoku = (Kamoku) itSonota.next();
                if (KNJ_EditEdit.getMS932ByteLength(kamoku._name) > 8) {
                    svf.VrsOutn("CLASS_NAME3_2_1", sonotaCnt, kamoku._name);
                } else {
                    svf.VrsOutn("CLASS_NAME3_1", sonotaCnt, kamoku._name);
                }
                if (student._kaijiMap.containsKey(kamoku._kamokuCd)) {
                    final String score = (String) student._kaijiMap.get(kamoku._kamokuCd);
                    svf.VrsOutn("VAL3", sonotaCnt, StringUtils.defaultString(score));
                }
                sonotaCnt++;
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private List getStudentList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final String studentSql = studentSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(studentSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String testDivName = rs.getString("TESTDIV_NAME");
                final String testDiv2Name = rs.getString("TESTDIV2_NAME");
                final String rpt01 = rs.getString("CONFIDENTIAL_RPT01");
                final String rpt02 = rs.getString("CONFIDENTIAL_RPT02");
                final String rpt03 = rs.getString("CONFIDENTIAL_RPT03");
                final String rpt04 = rs.getString("CONFIDENTIAL_RPT04");
                final String rpt05 = rs.getString("CONFIDENTIAL_RPT05");
                final String rpt06 = rs.getString("CONFIDENTIAL_RPT06");
                final String rpt07 = rs.getString("CONFIDENTIAL_RPT07");
                final String rpt08 = rs.getString("CONFIDENTIAL_RPT08");
                final String rpt09 = rs.getString("CONFIDENTIAL_RPT09");
                final String courseName = StringUtils.defaultString(rs.getString("KATEI")) + StringUtils.defaultString(rs.getString("MAJOR")) + StringUtils.defaultString(rs.getString("COURSE_NAME"));
                final Student student = new Student(examNo, name, testDivName, testDiv2Name, rpt01, rpt02, rpt03, rpt04, rpt05, rpt06, rpt07, rpt08, rpt09, courseName);
                student.setKaijiMap(db2);
                retList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retList;
    }

    //開示資料
    private String studentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     L004.NAME1 AS TESTDIV_NAME, ");
        stb.append("     CASE WHEN T1.TESTDIV2 = '1' THEN '（追検査）' ELSE '' END AS TESTDIV2_NAME, ");
        stb.append("     CONFRPT.CONFIDENTIAL_RPT01, ");
        stb.append("     CONFRPT.CONFIDENTIAL_RPT02, ");
        stb.append("     CONFRPT.CONFIDENTIAL_RPT03, ");
        stb.append("     CONFRPT.CONFIDENTIAL_RPT04, ");
        stb.append("     CONFRPT.CONFIDENTIAL_RPT05, ");
        stb.append("     CONFRPT.CONFIDENTIAL_RPT06, ");
        stb.append("     CONFRPT.CONFIDENTIAL_RPT07, ");
        stb.append("     CONFRPT.CONFIDENTIAL_RPT08, ");
        stb.append("     CONFRPT.CONFIDENTIAL_RPT09, ");
        stb.append("     K1.COURSENAME AS KATEI, ");
        stb.append("     M1.MAJORNAME AS MAJOR, ");
        stb.append("     CASE WHEN VALUE(T1.LAST_DAI1_COURSECODE, '0000') = '" + COURSE_NASHI + "' THEN '' ELSE C1.EXAMCOURSE_NAME END AS COURSE_NAME ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN NAME_MST L004 ON L004.NAMECD1 = 'L004' ");
        stb.append("          AND T1.TESTDIV = L004.NAMECD2 ");
        stb.append("     LEFT JOIN COURSE_MST K1 ON K1.COURSECD = T1.LAST_DAI1_COURSECD ");
        stb.append("     LEFT JOIN MAJOR_MST M1 ON M1.COURSECD = T1.LAST_DAI1_COURSECD AND M1.MAJORCD = T1.LAST_DAI1_MAJORCD ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST C1 ");
        stb.append("          ON C1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND C1.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND C1.TESTDIV = T1.TESTDIV ");
        stb.append("         AND C1.COURSECD = T1.LAST_DAI1_COURSECD ");
        stb.append("         AND C1.MAJORCD = T1.LAST_DAI1_MAJORCD ");
        stb.append("         AND C1.EXAMCOURSECD = T1.LAST_DAI1_COURSECODE ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ");
        stb.append("          ON CONFRPT.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND CONFRPT.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND CONFRPT.EXAMNO = T1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("         T1.ENTEXAMYEAR  = '" + _param._entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append("     AND T1.EXAMNO IN " + SQLUtils.whereIn(true, _param._category_selected) + " ");
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ");
        return stb.toString();
    }

    // 4:合格通知書
    private void printForm4(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        String groupField = null;

        final int maxLine = 1;
        final List pageList = getPageList(groupField, getList(db2, sql4()), maxLine);
        final String form = "KNJL327W_4.frm";
        svf.VrSetForm(form, 1);

        final String kisaiDate = KNJ_EditEdit.convertZenkakuSuuji(KNJ_EditDate.h_format_JP(db2, _param._kisaiDate));
        
        final List commentTokenList = KNJ_EditKinsoku.getTokenList(_param._comment, 38 * 2);

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List dataList = (List) pageList.get(pi);
            final Map row0 = (Map) dataList.get(0);

            //ヘッダー部
            svf.VrsOut("DATE", kisaiDate); //記載日
            svf.VrsOut("JSCHOOL_NAME", getString(row0, "FINSCHOOL_NAME")); //学校名(中学)
            svf.VrsOut("EXAM_NO", getString(row0, "EXAMNO")); //受検番号
            final String setName = getString(row0, "NAME") + "　様";
            final String nameField = getMS932ByteLength(setName) <= 32 ? "1" : getMS932ByteLength(setName) <= 40 ? "2" : "3";
            svf.VrsOut("NAME" + nameField, setName); //名前
            svf.VrsOut("HSCHOOL_NAME", _param._schoolName + "長"); //学校名(高校)＋"長"

            //本文
            final String zenkakuNendo = KNJ_EditEdit.convertZenkakuSuuji(KNJ_EditDate.h_format_JP_N(db2, _param._gengoBaseDate));
            svf.VrsOut("YEAR", zenkakuNendo + "度");
            if (COURSE_NASHI.equals(getString(row0, "SUC_COURSECODE"))) {
                
            	String text = "本校 " + StringUtils.defaultString(getString(row0, "SUC_KATEI")) + " 課程の ";
            	text += StringUtils.defaultString(getString(row0, "SUC_MAJOR")) + " に合格が決定しましたので通知します。";

            	final List textTokenList = KNJ_EditKinsoku.getTokenList(text, 38 * 2);
            	for (int i = 0; i < textTokenList.size(); i++) {
                    svf.VrsOut("TEXT" + String.valueOf(i + 1), (String) textTokenList.get(i));
            	}
            } else {
            	
            	String text = "本校 " + StringUtils.defaultString(getString(row0, "SUC_KATEI")) + " 課程の ";
            	final String space1 = StringUtils.replace(StringUtils.repeat(" ", getMS932ByteLength(text)), "  ", "　");
                svf.VrsOut("TEXT1_UPPER", space1 + StringUtils.defaultString(getString(row0, "SUC_MAJOR")));
                svf.VrsOut("TEXT1_LOWER", space1 + StringUtils.defaultString(getString(row0, "SUC_COURSE")));

            	final String space2 = StringUtils.replace(StringUtils.repeat(" ", Math.max(getMS932ByteLength(getString(row0, "SUC_MAJOR")), getMS932ByteLength(getString(row0, "SUC_COURSE")))), "  ", "　");
				text += space2 + " に合格が決定しましたので通知します。";

            	final List textTokenList = KNJ_EditKinsoku.getTokenList(text, 38 * 2);
            	for (int i = 0; i < textTokenList.size(); i++) {
                    svf.VrsOut("TEXT" + String.valueOf(i + 1), (String) textTokenList.get(i));
            	}
            }

            for (int i = 0; i < commentTokenList.size(); i++) {
            	final String token = (String) commentTokenList.get(i);
                svf.VrsOutn("COMMENT", i + 1, token);
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    // 4:合格通知書
    private String sql4() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     FINSCH.FINSCHOOL_NAME, ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.SUC_COURSECODE, ");
        stb.append("     COURSE.COURSENAME AS SUC_KATEI, ");
        stb.append("     MAJOR.MAJORNAME AS SUC_MAJOR, ");
        stb.append("     ENT_COURSE.EXAMCOURSE_NAME AS SUC_COURSE ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCH ON FINSCH.FINSCHOOLCD = BASE.FS_CD ");
        stb.append("     LEFT JOIN COURSE_MST COURSE ON COURSE.COURSECD = BASE.SUC_COURSECD ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR ON MAJOR.COURSECD = BASE.SUC_COURSECD ");
        stb.append("                              AND MAJOR.MAJORCD  = BASE.SUC_MAJORCD  ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST ENT_COURSE ON ENT_COURSE.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("                                            AND ENT_COURSE.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("                                            AND ENT_COURSE.TESTDIV      = BASE.TESTDIV ");
        stb.append("                                            AND ENT_COURSE.COURSECD     = BASE.SUC_COURSECD ");
        stb.append("                                            AND ENT_COURSE.MAJORCD      = BASE.SUC_MAJORCD ");
        stb.append("                                            AND ENT_COURSE.EXAMCOURSECD = BASE.SUC_COURSECODE ");
        stb.append(" WHERE ");
        stb.append("         BASE.ENTEXAMYEAR  = '" + _param._entexamYear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if ("1".equals(_param._testDiv)) {
            stb.append("     AND ((BASE.TESTDIV = '" + _param._testDiv + "' AND VALUE(BASE.JUDGEMENT, '') = '1') "); // 前期選抜受験者で合格
            stb.append("          OR ");
            stb.append("          (BASE.TESTDIV = '4' AND VALUE(BASE.JUDGEMENT, '') = '3') "); // スポーツ特別枠選抜受験者で前期選抜合格
            stb.append("         ) ");
        } else {
            stb.append("     AND BASE.TESTDIV      = '" + _param._testDiv + "' ");
            stb.append("     AND value(BASE.JUDGEMENT, '') = '1' "); // 合格
        }
        stb.append(" ORDER BY ");
        stb.append("     BASE.EXAMNO ");
        return stb.toString();
    }

    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private class Student {
        final String _examNo;
        final String _name;
        final String _testDivName;
        final String _testDiv2Name;
        final String _rpt01;
        final String _rpt02;
        final String _rpt03;
        final String _rpt04;
        final String _rpt05;
        final String _rpt06;
        final String _rpt07;
        final String _rpt08;
        final String _rpt09;
        final String _courseName;
        final Map _kaijiMap;

        public Student(
                final String examNo,
                final String name,
                final String testDivName,
                final String testDiv2Name,
                final String rpt01,
                final String rpt02,
                final String rpt03,
                final String rpt04,
                final String rpt05,
                final String rpt06,
                final String rpt07,
                final String rpt08,
                final String rpt09,
                final String courseName
        ) {
            _examNo = examNo;
            _name = name;
            _testDivName = testDivName;
            _testDiv2Name = testDiv2Name;
            _rpt01 = rpt01;
            _rpt02 = rpt02;
            _rpt03 = rpt03;
            _rpt04 = rpt04;
            _rpt05 = rpt05;
            _rpt06 = rpt06;
            _rpt07 = rpt07;
            _rpt08 = rpt08;
            _rpt09 = rpt09;
            _courseName = courseName;
            _kaijiMap = new HashMap();
        }

        private void setKaijiMap(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getKaijiSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String cd = rs.getString("KAIJI_SUBCLASSCD");
                    final String score = StringUtils.defaultString(rs.getString("SCORE"));
                    _kaijiMap.put(cd, score);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getKaijiSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_KAIJI_DAT ");
            stb.append(" WHERE ");
            stb.append("         ENTEXAMYEAR  = '" + _param._entexamYear + "' ");
            stb.append("     AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND TESTDIV      = '" + _param._testDiv + "' ");
            stb.append("     AND RECEPTNO     = '" + _examNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     KAIJI_SUBCLASSCD ");
            return stb.toString();
        }
    }

    private class Kamoku {
        final String _kamokuCd;
        final String _name;
        public Kamoku(
                final String kamokuCd,
                final String name
        ) {
            _kamokuCd = kamokuCd;
            _name = name;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70986 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _entexamYear;
        final String _applicantDiv;
        final String _testDiv;
        final String _style;
        final String[] _category_selected;
        final String _kisaiDate;
        final String _sucDate;
        final String _sucAmPm;
        final String _sucHour;
        final String _sucMinute;
        final String _loginYear;
        final String _useSchool_KindField;
        final String _SCHOOLCD;
        final String _SCHOOLKIND;
        final String _gengoBaseDate;
        final String _documentroot;
        final String _folder;
        String _comment;

        final String _testDivName;
        final String _schoolName;
        final List _nameL008List;
        final List _nameL057GakuList;
        final List _nameL057SonotaList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _style = request.getParameter("STYLE");
            _category_selected = request.getParameterValues("LEFT_LIST");
            _kisaiDate = request.getParameter("KISAI_DATE");
            _sucDate = request.getParameter("SUC_DATE");
            _sucAmPm = "1".equals(request.getParameter("SUC_AM_PM")) ? "午前" : "午後";
            _sucHour = request.getParameter("SUC_HOUR");
            _sucMinute = request.getParameter("SUC_MINUTE");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD = request.getParameter("SCHOOLCD");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");

            _documentroot = request.getParameter("DOCUMENTROOT");
            KNJ_Control imagepath_extension = new KNJ_Control();                //取得クラスのインスタンス作成
            KNJ_Control.ReturnVal returnval = imagepath_extension.Control(db2);
            _folder = returnval.val4;                                           //丸データ格納フォルダ
            try {
            	if (!StringUtils.isBlank(request.getParameter("COMMENT"))) {
            		_comment = new String(request.getParameter("COMMENT").getBytes("ISO8859-1"));
            	}
            } catch (Exception e) {
                log.error("exception!", e);
            }

            _testDivName = getNameMst(db2, "L004", _testDiv, "NAME1");
            _nameL008List = getNameMstList(db2, "L008", "NAME1", "");
            _nameL057GakuList = getNameMstList(db2, "L057", "NAME1", "1");
            _nameL057SonotaList = getNameMstList(db2, "L057", "NAME1", "2");
            _schoolName = getSchoolName(db2);
            _gengoBaseDate = "2019".equals(_entexamYear) ? "2019-05-01" : _entexamYear+"-04-01";
            log.fatal("(_entexamYear:)"+_entexamYear); 
            log.fatal("(basedate:)"+_gengoBaseDate);
            
        }

         
         // 日付文字列指定でDate型を生成
         private Date toDate(String str) {
             // 日付フォーマットを作成
             SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

             // Date型へ変換
             try {
                 return dateFormat.parse(str);
             } catch ( ParseException e ) {
                 return null;
             }
         }
        
        private String getNameMst(final DB2UDB db2, final String nameCd1, final String nameCd2, final String fieldName) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getNameMstSql(nameCd1, nameCd2, "");
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString(fieldName);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getNameMstSql(final String namecd1, final String namecd2, final String nameSpare1) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = '" + namecd1 + "' ");
            if (!"".equals(namecd2)) {
                stb.append("     AND NAMECD2 = '" + namecd2 + "' ");
            }
            if (!"".equals(nameSpare1)) {
                stb.append("     AND NAMESPARE1 = '" + nameSpare1 + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     NAMECD2 ");
            return stb.toString();
        }

        private List getNameMstList(final DB2UDB db2, final String nameCd1, final String fieldName, final String nameSpare1) throws SQLException {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getNameMstSql(nameCd1, "", nameSpare1);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String kamokuCd = rs.getString("NAMECD2");
                    final String name = rs.getString(fieldName);
                    final Kamoku kamoku = new Kamoku(kamokuCd, name);
                    retList.add(kamoku);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retList;
        }

        private String getSchoolName(final DB2UDB db2) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSchoolNameSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("FINSCHOOL_NAME");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getSchoolNameSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.FINSCHOOL_NAME ");
            stb.append(" FROM ");
            stb.append("     V_SCHOOL_MST T1, ");
            stb.append("     FINSCHOOL_MST T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _loginYear + "' ");
            stb.append("     AND T1.KYOUIKU_IINKAI_SCHOOLCD = T2.FINSCHOOLCD ");
            if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append("     AND T1.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("     AND T1.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            return stb.toString();
        }

    }
}

// eof

