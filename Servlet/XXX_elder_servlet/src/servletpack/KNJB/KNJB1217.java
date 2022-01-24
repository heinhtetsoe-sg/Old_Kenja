/*
 * $Id: 090133f73664e8feac8aa1029e4810c82d4ffa96 $
 *
 * 作成日: 2012/10/18
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;

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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * 教科書購入票
 */
public class KNJB1217 {

    private static final Log log = LogFactory.getLog(KNJB1217.class);

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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        final List studentList = getStudentList(db2);
        for (Iterator itStd = studentList.iterator(); itStd.hasNext();) {
            final Student student = (Student) itStd.next();

            if ("1".equals(_param._formType)) {
                if ("1".equals(_param._ryousyuu)) {
                    svf.VrSetForm("KNJB1217.frm", 1);
                    printHeader(db2, svf, student, "領収書");
                    printChairText(db2, svf, student);
                    printHeader(db2, svf, student, "領収書");
                    svf.VrEndPage();
                    _hasData = true;
                }
                if ("1".equals(_param._hikiwatashi)) {
                    svf.VrSetForm("KNJB1217_2.frm", 1);
                    printHeader(db2, svf, student, "引渡書");
                    printChairText(db2, svf, student);
                    printHeader(db2, svf, student, "引渡書");
                    svf.VrEndPage();
                    _hasData = true;
                }
                if ("1".equals(_param._meisai)) {
                    svf.VrSetForm("KNJB1217_4.frm", 1);
                    printHeader(db2, svf, student, "明細書");
                    printChairText(db2, svf, student);
                    printHeader(db2, svf, student, "明細書");
                    svf.VrEndPage();
                    _hasData = true;
                }
            } else {
                svf.VrSetForm("KNJB1217_3.frm", 1);
                printFirstInfo(db2, svf, student);
                svf.VrEndPage();
                _hasData = true;
            }

        }
    }

    /**
     * 生徒データ取得処理
     * @param db2           ＤＢ接続オブジェクト
     * @return              帳票出力対象データリスト
     * @throws Exception
     */
    private List getStudentList(final DB2UDB db2) throws SQLException {

        final List rtnList = new ArrayList();
        final String sql = getStudentInfoSql();
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String courseCd = rs.getString("COURSECD");
                final String majorCd = rs.getString("MAJORCD");
                final String courseCode = rs.getString("COURSECODE");
                final String name = rs.getString("NAME");
                final String hrName = rs.getString("HR_NAME");
                final Student studentInfo = new Student(schregNo, grade, hrClass, attendno, courseCd, majorCd, courseCode, name, hrName);
                rtnList.add(studentInfo);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getStudentInfoSql() {
        final StringBuffer stb = new StringBuffer();
        if ("2".equals(_param._output)) {
	        stb.append(" SELECT ");
	        stb.append("     REGD.SCHREGNO, ");
	        stb.append("     REGD.GRADE, ");
	        stb.append("     REGD.HR_CLASS, ");
	        stb.append("     REGD.ATTENDNO, ");
	        stb.append("     REGD.COURSECD, ");
	        stb.append("     REGD.MAJORCD, ");
	        stb.append("     REGD.COURSECODE, ");
	        stb.append("     BASE.NAME, ");
	        stb.append("     REGDH.HR_NAME ");
	        stb.append(" FROM ");
	        stb.append("     SCHREG_REGD_DAT REGD ");
	        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
	        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
	        stb.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
	        stb.append("          AND REGD.GRADE = REGDH.GRADE ");
	        stb.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
	        stb.append(" WHERE ");
	        stb.append("     REGD.YEAR = '" + _param._year + "' ");
	        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
	        stb.append("     AND REGD.SCHREGNO IN " + _param._studentInState + " ");
	        stb.append(" ORDER BY ");
	        stb.append("     REGD.GRADE, ");
	        stb.append("     REGD.HR_CLASS, ");
	        stb.append("     REGD.ATTENDNO ");
        } else {
        	stb.append(" SELECT ");
        	stb.append("   SCHREGNO, ");
        	stb.append("   GRADE, ");
        	stb.append("   HR_CLASS, ");
        	stb.append("   ATTENDNO, ");
	        stb.append("   COURSECD, ");
	        stb.append("   MAJORCD, ");
	        stb.append("   COURSECODE, ");
        	stb.append("   NAME, ");
        	stb.append("   '' AS HR_NAME ");
        	stb.append(" FROM ");
        	stb.append("   FRESHMAN_DAT ");
        	stb.append(" WHERE ");
        	stb.append("   ENTERYEAR = '" + (Integer.parseInt(_param._year) + 1) + "' ");

        }
        return stb.toString();
    }

    private void printHeader(final DB2UDB db2, final Vrw32alp svf, final Student student, final String title) {

        int m004Cnt = 1;
        String setTitle = "";
        String sep = "";
        final String courseKey = student._courseCd + student._majorCd + student._courseCode;
        final List m004List =  (_param._m004CourseMap.containsKey(courseKey) ? (List) _param._m004CourseMap.get(courseKey) : new ArrayList());
        for (Iterator itM004 = m004List.iterator(); itM004.hasNext();) {
            final TextBookDiv textBookDiv = (TextBookDiv) itM004.next();
            final String name1 = textBookDiv._textDivName;
            svf.VrsOut("TEXT_HEADER" + m004Cnt, name1);
            setTitle += sep + name1;
            m004Cnt++;
            sep = "・";
        }
        setTitle += " " + title;

        svf.VrsOut("CHECK_NO", student._schregNo.length() >= 2 ? student._schregNo.substring(2) : "");
        svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度　" + setTitle);
        svf.VrsOut("NO", student._schregNo);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._printDate));
        svf.VrsOut("SCHOOLNAME", _param._schoolname);

        if (!"".equals(student._hrName)) {
            svf.VrsOut("HR_NAME", "(" + student._hrName + ")");
        }
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getSql(student._schregNo);
            log.debug("address sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                svf.VrsOut("ZIP_NO", rs.getString("ZIPCD"));
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                if ("1".equals(_param._useAddrField2) && (getMS932ByteLength(addr1) > 50 || getMS932ByteLength(addr2) > 50)) {
                    svf.VrsOut("ADDRESS1_3", addr1);
                    svf.VrsOut("ADDRESS2_3", addr2);
                } else if ("1".equals(_param._useAddrField2) && (getMS932ByteLength(addr1) > 44 || getMS932ByteLength(addr2) > 44)) {
                    svf.VrsOut("ADDRESS1_2", addr1);
                    svf.VrsOut("ADDRESS2_2", addr2);
                } else {
                    svf.VrsOut("ADDRESS1", addr1);
                    svf.VrsOut("ADDRESS2", addr2);
                }
                svf.VrsOut("NAME", StringUtils.defaultString(rs.getString("NAME2")) + "　様");
            }
        } catch (final Exception ex) {
            log.error("set_detail1 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getSql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        if ("1".equals(_param._output)) {
            stb.append("SELECT ");
            stb.append("  t0.SCHREGNO, ");
            stb.append("  t0.ZIPCD,");
            stb.append("  t0.ADDR1,");
            stb.append("  t0.ADDR2,");
            stb.append("  t0.NAME, ");
            stb.append("  t0.NAME AS NAME2 ");
            stb.append(" FROM ");
            stb.append("   FRESHMAN_DAT t0 ");
            stb.append(" WHERE ");
            stb.append("  t0.ENTERYEAR = '" + (Integer.parseInt(_param._year) + 1) + "' ");
            stb.append("  AND t0.SCHREGNO = '" + schregno + "' ");
        } else {
            stb.append(" WITH SCHREG_ADDRESS AS ( ");
            stb.append("   SELECT  ");
            stb.append("      T3.NAME AS SCHREG_NAME, ");
            stb.append("      T1.*  ");
            stb.append("   FROM  ");
            stb.append("      SCHREG_ADDRESS_DAT T1  ");
            stb.append("      INNER JOIN ( ");
            stb.append("        SElECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM SCHREG_ADDRESS_DAT GROUP BY SCHREGNO ");
            stb.append("      ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ISSUEDATE = T1.ISSUEDATE ");
            stb.append("      INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(" ) ");

            stb.append("SELECT ");
            stb.append("  t0.SCHREGNO, ");
            stb.append("  CASE WHEN SEND_ADDR1 IS NOT NULL THEN SEND_ZIPCD ELSE t2.ZIPCD END AS ZIPCD,");
            stb.append("  CASE WHEN SEND_ADDR1 IS NOT NULL THEN SEND_ADDR1 ELSE t2.ADDR1 END AS ADDR1,");
            stb.append("  CASE WHEN SEND_ADDR1 IS NOT NULL THEN SEND_ADDR2 ELSE t2.ADDR2 END AS ADDR2,");
            stb.append("  CASE WHEN SEND_ADDR1 IS NOT NULL THEN SEND_NAME  ELSE t2.SCHREG_NAME END AS NAME,");
            stb.append("  t0.NAME AS NAME2 ");
            stb.append(" FROM ");
            stb.append("   SCHREG_BASE_MST t0 ");
            stb.append(" LEFT JOIN SCHREG_SEND_ADDRESS_DAT t1 ON t1.SCHREGNO = t0.SCHREGNO ");
            stb.append("   AND t1.DIV = '1' ");
            stb.append(" LEFT JOIN SCHREG_ADDRESS t2 ON t2.SCHREGNO = t0.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("  t0.SCHREGNO = '" + schregno + "' ");
        }
        return stb.toString();
    }

    private void printChairText(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getChairCountSql(student._schregNo);
            log.debug(" chair sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOut("SUBJECT", rs.getString("CNT"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        final List subclassList = new ArrayList();
        try {
            final String sql = sql(student);

            log.debug(" textbook sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                TextbookSubclass textBookSubclass = getTextBookSubclass(subclassList, rs.getString("CLASSCD"), rs.getString("SCHOOL_KIND"), rs.getString("CURRICULUM_CD"), rs.getString("SUBCLASSCD"));
                if (null == textBookSubclass) {
                    textBookSubclass = new TextbookSubclass(rs.getString("CLASSCD"), rs.getString("SCHOOL_KIND"), rs.getString("CURRICULUM_CD"), rs.getString("SUBCLASSCD"));
                    subclassList.add(textBookSubclass);
                }
                final Textbook textbook = new Textbook(rs.getString("TEXTBOOKCD"), rs.getString("TEXTBOOKDIV"), rs.getString("NAMESPARE1"), rs.getString("TEXTBOOKMS"), rs.getString("TEXTBOOKNAME"), rs.getString("TEXTBOOKUNITPRICE"));
                textBookSubclass._textBookList.add(textbook);
            }

        } catch (final SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        String[] textbookdiv = new String[] {"1", "2"};
        String[] textbookdiv2 = new String[] {"1", "2"};
        int divCnt = 0;
        final String courseKey = student._courseCd + student._majorCd + student._courseCode;
        final List m004List =  (_param._m004CourseMap.containsKey(courseKey) ? (List) _param._m004CourseMap.get(courseKey) : new ArrayList());
        for (Iterator itM004 = m004List.iterator(); itM004.hasNext();) {
            final TextBookDiv textBookDiv = (TextBookDiv) itM004.next();
            textbookdiv2[divCnt] = textBookDiv._textDiv;
            divCnt++;
        }
        final long[] pricedivtotal = new long[3];
        int line = 1;

        final int maxLine = 21;
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final TextbookSubclass textBookSubclass = (TextbookSubclass) it.next();
            int subline = 0;
            for (int i = 0; i < textbookdiv.length; i++) {
                final String setDivField = "1".equals(textbookdiv[i]) ? textbookdiv[i] : "2";
                final List textList = textBookSubclass.getTextList(textbookdiv2[i]);
                subline = Math.max(subline, textList.size());
                for (int j = 0; j < textList.size(); j++) {
                    if (maxLine < line + j) {
                        printHeader(db2, svf, student, "引渡書");
                        svf.VrEndPage();
                        line = 1;
                        subline -= j + 1;
                    }
                    final Textbook textbook = (Textbook) textList.get(j);
                    if ("1".equals(setDivField)) {
                        svf.VrsOutn("TEXTNO", line + j, textbook._textbookms);
                    }
                    svf.VrsOutn("TEXT" + setDivField + "_1", line + j, textbook._textbookname);
                    svf.VrsOutn("TEXT_PRICE" + ("1".equals(setDivField) ? "" : setDivField), line + j, textbook._textbookunitprice);
                    if (null != textbook._textbookunitprice) {
                        pricedivtotal[i] += Long.parseLong(textbook._textbookunitprice);
                    }
                }
            }
            line += subline;
        }
        if (line > maxLine) {
            printHeader(db2, svf, student, "引渡書");
            svf.VrEndPage();
            line = 1;
        }

        long pricetotal = 0;
        for (int i = 0; i < textbookdiv.length; i++) {
            svf.VrsOutn("TEXT_PRICE" + ("1".equals(textbookdiv[i]) ? "" : textbookdiv[i]), maxLine, String.valueOf(pricedivtotal[i]));
            pricetotal += pricedivtotal[i];
        }
        svf.VrsOut("TEXTBOOK", String.valueOf(pricetotal));
    }

    private void printFirstInfo(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final List subclassList = new ArrayList();
        try {
            final String sql = sql(student);

            log.debug(" textbook sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                TextbookSubclass textBookSubclass = getTextBookSubclass(subclassList, rs.getString("CLASSCD"), rs.getString("SCHOOL_KIND"), rs.getString("CURRICULUM_CD"), rs.getString("SUBCLASSCD"));
                if (null == textBookSubclass) {
                    textBookSubclass = new TextbookSubclass(rs.getString("CLASSCD"), rs.getString("SCHOOL_KIND"), rs.getString("CURRICULUM_CD"), rs.getString("SUBCLASSCD"));
                    subclassList.add(textBookSubclass);
                }
                final Textbook textbook = new Textbook(rs.getString("TEXTBOOKCD"), rs.getString("TEXTBOOKDIV"), rs.getString("NAMESPARE1"), rs.getString("TEXTBOOKMS"), rs.getString("TEXTBOOKNAME"), rs.getString("TEXTBOOKUNITPRICE"));
                textBookSubclass._textBookList.add(textbook);
            }

        } catch (final SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        String[] textbookdiv = new String[] {"1", "2"};
        String[] textbookdiv2 = new String[] {"1", "2"};
        int divCnt = 0;
        final String courseKey = student._courseCd + student._majorCd + student._courseCode;
        final List m004List =  (_param._m004CourseMap.containsKey(courseKey) ? (List) _param._m004CourseMap.get(courseKey) : new ArrayList());
        for (Iterator itM004 = m004List.iterator(); itM004.hasNext();) {
            final TextBookDiv textBookDiv = (TextBookDiv) itM004.next();
            textbookdiv2[divCnt] = textBookDiv._textDiv;
            divCnt++;
        }
        final long[] pricedivtotal = new long[3];

        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final TextbookSubclass textBookSubclass = (TextbookSubclass) it.next();
            for (int i = 0; i < textbookdiv.length; i++) {
                final List textList = textBookSubclass.getTextList(textbookdiv2[i]);
                for (int j = 0; j < textList.size(); j++) {
                    final Textbook textbook = (Textbook) textList.get(j);
                    if (null != textbook._textbookunitprice) {
                        pricedivtotal[i] += Long.parseLong(textbook._textbookunitprice);
                    }
                }
            }
        }

        //実際の印字、上段処理はprintChairTextと一緒
        svf.VrsOut("SCHREGNO", student._schregNo);
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 70 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name);

        long pricetotal = 0;
        for (int i = 0; i < textbookdiv.length; i++) {
            pricetotal += pricedivtotal[i];
        }
        int printLine = 1;
        printLine = printPrice(svf, pricetotal, printLine, "・教材費");

        printLine = printPrice(svf, Integer.parseInt(_param._kyousai), printLine, "・保険代");
        pricetotal += Integer.parseInt(_param._kyousai);

        printLine = printPrice(svf, Integer.parseInt(_param._kenkouShindan), printLine, "・健康診断費");
        pricetotal += Integer.parseInt(_param._kenkouShindan);

        printLine = printPrice(svf, Integer.parseInt(_param._shashin), printLine, "・写真代");
        pricetotal += Integer.parseInt(_param._shashin);

        printLine = printPrice(svf, Integer.parseInt(_param._locker), printLine, "・ロッカー代");
        pricetotal += Integer.parseInt(_param._locker);

        printLine = printPrice(svf, Integer.parseInt(_param._kaihi), printLine, "・立志会費");
        pricetotal += Integer.parseInt(_param._kaihi);

        svf.VrsOutn("TOTAL_NAME", printLine + 1, "合 計");
        svf.VrsOutn("COLLECT_MONEY_TOTAL", printLine + 1, String.valueOf(pricetotal));

        if (!StringUtils.isEmpty(_param._renrakuJikou)) {
            final String[] setRenraku = KNJ_EditEdit.get_token_1(_param._renrakuJikou, 70, 8);
            for (int i = 0; i < setRenraku.length; i++) {
                final String setStr = setRenraku[i];
                svf.VrsOutn("CONTACT_MATTER", i + 1, setStr);
            }
        }
    }

    private int printPrice(final Vrw32alp svf, long pricetotal, final int printLine, final String setTitle) {
        int retInt = printLine;
        if (pricetotal > 0) {
            svf.VrsOutn("COLLECT_NAME", printLine, setTitle);
            svf.VrsOutn("COLLECT_MONEY", printLine, String.valueOf(pricetotal));
            retInt++;
        }
        return retInt;
    }

    private static int getMS932ByteLength(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (final Exception e) {
                log.error("exception!", e);
            }
        }
        return 0;
    }

    private TextbookSubclass getTextBookSubclass(final List list, String classcd, String schoolkind, String curriculumcd, String subclasscd) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final TextbookSubclass tbs = (TextbookSubclass) it.next();
            if (tbs._classcd.equals(classcd) && tbs._schoolkind.equals(schoolkind) && tbs._curriculumcd.equals(curriculumcd) && tbs._subclasscd.equals(subclasscd)) {
                return tbs;
            }
        }
        return null;
    }

    private static class TextbookSubclass {
        final String _classcd;
        final String _schoolkind;
        final String _curriculumcd;
        final String _subclasscd;
        final List _textBookList = new ArrayList();
        public TextbookSubclass(String classcd, String schoolkind, String curriculumcd, String subclasscd) {
            _classcd = classcd;
            _schoolkind = schoolkind;
            _curriculumcd = curriculumcd;
            _subclasscd = subclasscd;
        }
        public List getTextList(String textbookdiv) {
            final List list = new ArrayList();
            for (final Iterator it = _textBookList.iterator(); it.hasNext();) {
                final Textbook tb = (Textbook) it.next();
                if (textbookdiv.equals(tb._textbookdiv)) {
                    list.add(tb);
                }
            }
            return list;
        }
    }

    private static class Textbook {
        final String _textbookcd;
        final String _textbookdiv;
        final String _namespare1;
        final String _textbookms;
        final String _textbookname;
        final String _textbookunitprice;
        public Textbook(String textbookcd, String textbookdiv, String namespare1, String textbookms, String textbookname, String textbookunitprice) {
            _textbookcd = textbookcd;
            _textbookdiv = textbookdiv;
            _namespare1 = namespare1;
            _textbookms = textbookms;
            _textbookname = textbookname;
            _textbookunitprice = textbookunitprice;
        }
    }

    public String sql(final Student student) {
        final String courseKey = student._courseCd + student._majorCd + student._courseCode;
        final List m004List =  (_param._m004CourseMap.containsKey(courseKey) ? (List) _param._m004CourseMap.get(courseKey) : new ArrayList());
        String textBookInState = "(";
        String sep = "";
        for (Iterator itM004 = m004List.iterator(); itM004.hasNext();) {
            final TextBookDiv textBookDiv = (TextBookDiv) itM004.next();
            textBookInState += sep + "'" + textBookDiv._textDiv + "'";
            sep = ",";
        }
        textBookInState += ")";

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.TEXTBOOKCD, ");
        stb.append("     T3.TEXTBOOKMS, ");
        stb.append("     T3.TEXTBOOKDIV, ");
        stb.append("     NMM004.NAMESPARE1, ");
        stb.append("     T3.TEXTBOOKNAME, ");
        stb.append("     T3.TEXTBOOKUNITPRICE ");
        stb.append(" FROM SCHREG_TEXTBOOK_SUBCLASS_DAT T1 ");
        stb.append(" INNER JOIN TEXTBOOK_MST T3 ON T3.TEXTBOOKCD = T1.TEXTBOOKCD ");
        stb.append(" INNER JOIN NAME_MST NMM004 ON NMM004.NAMECD1 = 'M004' ");
        stb.append("     AND NMM004.NAMECD2 = T3.TEXTBOOKDIV ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SCHREGNO = '" + student._schregNo + "' ");
        stb.append("     AND T3.TEXTBOOKDIV IN " + textBookInState + " ");
        stb.append(" ORDER BY ");
        stb.append("     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.TEXTBOOKCD ");
        return stb.toString();
    }

    public String getChairCountSql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAX_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        stb.append("     SEMESTER, ");
        stb.append("     SCHREGNO, ");
        stb.append("     MAX(RIREKI_CODE) AS RIREKI_CODE ");
        stb.append(" FROM ");
        stb.append("     SUBCLASS_STD_SELECT_RIREKI_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND SCHREGNO = '" + schregno + "' ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR, ");
        stb.append("     SEMESTER, ");
        stb.append("     SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     MAX_T, ");
        stb.append("     SUBCLASS_STD_SELECT_RIREKI_DAT RIREKI ");
        stb.append(" WHERE ");
        stb.append("     MAX_T.YEAR = RIREKI.YEAR ");
        stb.append("     AND MAX_T.SEMESTER = RIREKI.SEMESTER ");
        stb.append("     AND MAX_T.RIREKI_CODE = RIREKI.RIREKI_CODE ");
        stb.append("     AND MAX_T.SCHREGNO = RIREKI.SCHREGNO ");

        return stb.toString();
    }

    /** 生徒クラス */
    private class Student {
        final String _schregNo;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _name;
        final String _hrName;
        public Student(
                final String schregNo,
                final String grade,
                final String hrClass,
                final String attendno,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String name,
                final String hrName
        ) {
            _schregNo = schregNo;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _name = name;
            _hrName = hrName;
        }
    }

    /** テキスト区分クラス */
    private class TextBookDiv {
        final String _textDiv;
        final String _textDivName;
        public TextBookDiv(
                final String textDiv,
                final String textDivName
        ) {
            _textDiv = textDiv;
            _textDivName = textDivName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72484 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlDate;
        private final String _semester;
        private final String _output;
        private final String _studentInState;
        private final String _useAddrField2;
        private final String _formType;
        private final String _kyousai;
        private final String _kenkouShindan;
        private final String _shashin;
        private final String _locker;
        private final String _kaihi;
        private final String _renrakuJikou;
        private final String _ryousyuu;
        private final String _hikiwatashi;
        private final String _meisai;
        final String _schoolname;
        final String _useSchool_KindField;
        final String _SCHOOLCD;
        final String _SCHOOLKIND;
        final String _PRGID;
        final boolean _isMieken;
        private final Map _m004CourseMap;
        private final Map _documentPrgMap;
        private final String _printDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _semester = request.getParameter("SEMESTER");
            _output = request.getParameter("OUTPUT");
            _formType = request.getParameter("FORMTYPE");
            _printDate = request.getParameter("PRINTDATE");

            final String[] schregs = request.getParameterValues("category_name");
            String studentInstate = "(";
            String sep = "";
            for (int i = 0; i < schregs.length; i++) {
                studentInstate += sep + "'" + StringUtils.split(schregs[i], "-")[0] + "'";
                sep = ",";
            }
            studentInstate += ")";

            _studentInState = studentInstate;

            _ryousyuu = request.getParameter("RYOUSYUU");
            _hikiwatashi = request.getParameter("HIKIWATASHI");
            _meisai = request.getParameter("MEISAI");
            _useAddrField2 = request.getParameter("useAddrField2");
            _schoolname = getSchoolname(db2);
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD = request.getParameter("SCHOOLCD");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _PRGID = request.getParameter("PRGID");
            final String z010name1 = setZ010Name1(db2);
            _isMieken = "mieken".equals(z010name1);
            _m004CourseMap = getM004(db2);
            _documentPrgMap = getDocumentPrg(db2);
            _kyousai = getDocPrg("document01_1", "0");
            _kenkouShindan = getDocPrg("document01_2", "0");
            _shashin = getDocPrg("document01_3", "0");
            _locker = getDocPrg("document01_4", "0");
            _kaihi = getDocPrg("document01_5", "0");
            _renrakuJikou = getDocPrg("document02_5", "");
        }


        private String getDocPrg(final String keyName, final String defVal) {
            String retStr = defVal;
            if (_documentPrgMap.containsKey(keyName)) {
                retStr = StringUtils.isEmpty((String) _documentPrgMap.get(keyName)) ? defVal : (String) _documentPrgMap.get(keyName);
            }

            return retStr;
        }


        // 卒業認定単位数の取得
        private String getSchoolname(
                final DB2UDB db2
        ) {
            String schoolname1 = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "'";
                if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                    sql += "   AND SCHOOL_KIND = '" + _SCHOOLKIND + "' ";
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    schoolname1 = rs.getString("SCHOOLNAME1");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolname1;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(final DB2UDB db2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        private Map getM004(final DB2UDB db2) {
            final Map retMap = new HashMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     COURSE_D.COURSECD || COURSE_D.MAJORCD || COURSE_D.COURSECODE AS COURSE, ");
            stb.append("     TEXTBOOK.TEXTBOOKDIV, ");
            stb.append("     M004.NAME1 ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_TEXTBOOK_COURSE_DAT COURSE_D ");
            stb.append("     INNER JOIN TEXTBOOK_MST TEXTBOOK ON COURSE_D.TEXTBOOKCD = TEXTBOOK.TEXTBOOKCD ");
            stb.append("     INNER JOIN NAME_MST M004 ON M004.NAMECD1 = 'M004' ");
            stb.append("           AND TEXTBOOK.TEXTBOOKDIV = M004.NAMECD2 ");
            stb.append("           AND M004.NAMESPARE1 IS NOT NULL ");
            stb.append(" WHERE ");
            stb.append("     COURSE_D.YEAR = '" + _year + "' ");
            stb.append(" GROUP BY ");
            stb.append("     COURSE_D.COURSECD || COURSE_D.MAJORCD || COURSE_D.COURSECODE, ");
            stb.append("     TEXTBOOK.TEXTBOOKDIV, ");
            stb.append("     M004.NAME1 ");
            stb.append(" ORDER BY ");
            stb.append("     COURSE_D.COURSECD || COURSE_D.MAJORCD || COURSE_D.COURSECODE, ");
            stb.append("     TEXTBOOK.TEXTBOOKDIV ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String course = rs.getString("COURSE");
                    List setList = new ArrayList();
                    if (retMap.containsKey(course)) {
                        setList = (List) retMap.get(course);
                    } else {
                        retMap.put(course, setList);
                    }
                    final String textBookDivCd = rs.getString("TEXTBOOKDIV");
                    final String textDivName = rs.getString("NAME1");
                    final TextBookDiv teBookDiv = new TextBookDiv(textBookDivCd, textDivName);
                    setList.add(teBookDiv);
                }
            } catch (SQLException ex) {
                log.debug("getM004 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private Map getDocumentPrg(final DB2UDB db2) {
            final Map retMap = new HashMap();
            final String sql01 = getDocumentPrgSql("01");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql01);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String document1 = rs.getString("DOCUMENT1");
                    final String document2 = rs.getString("DOCUMENT2");
                    final String document3 = rs.getString("DOCUMENT3");
                    final String document4 = rs.getString("DOCUMENT4");
                    final String document5 = rs.getString("DOCUMENT5");
                    retMap.put("document01_1", document1);
                    retMap.put("document01_2", document2);
                    retMap.put("document01_3", document3);
                    retMap.put("document01_4", document4);
                    retMap.put("document01_5", document5);
                }
            } catch (SQLException ex) {
                log.debug("getM004 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            final String sql02 = getDocumentPrgSql("02");

            ps = null;
            rs = null;
            try {
                ps = db2.prepareStatement(sql02);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String document1 = rs.getString("DOCUMENT1");
                    final String document2 = rs.getString("DOCUMENT2");
                    final String document3 = rs.getString("DOCUMENT3");
                    final String document4 = rs.getString("DOCUMENT4");
                    final String document5 = rs.getString("DOCUMENT5");
                    retMap.put("document02_1", document1);
                    retMap.put("document02_2", document2);
                    retMap.put("document02_3", document3);
                    retMap.put("document02_4", document4);
                    retMap.put("document02_5", document5);
                }
            } catch (SQLException ex) {
                log.debug("getM004 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }


        private String getDocumentPrgSql(final String seq) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     DOCUMENT_PRG_DAT ");
            stb.append(" WHERE ");
            stb.append("     SCHOOLCD = '" + _SCHOOLCD + "' ");
            stb.append("     AND SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            stb.append("     AND PROGRAMID = '" + _PRGID + "' ");
            stb.append("     AND SEQ = '" + seq + "' ");
            return stb.toString();
        }

    }
}

// eof

