/*
 * $Id: b028c2180b6d27471b5b371dbda54d8e2a690ebf $
 *
 * 作成日: 2018/11/12
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

public class KNJL304E {

    private static final Log log = LogFactory.getLog(KNJL304E.class);

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
        svf.VrSetForm("KNJL304E.frm", 1);
        final List printList = getList(db2);
        final int maxLine = 40;
        int printLine = 1;

        setTitle(db2, svf);//ヘッダ
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            if (printLine > maxLine) {
                svf.VrEndPage();
                setTitle(db2, svf);//ヘッダ
                printLine = 1;
            }

            //データ
            //合格区分(出力は空なので、処理はなし。)
            //受験番号
            svf.VrsOutn("EXAM_NO" , printLine, StringUtils.defaultString(printData._examNo));
            //氏名
            svf.VrsOutn("NAME1" , printLine, StringUtils.defaultString(printData._name));
            //中学校
            svf.VrsOutn("FINSCHOOL_NAME" , printLine, StringUtils.defaultString(printData._finSchoolAbbv));
            //受験区分
            svf.VrsOutn("DIV" , printLine, StringUtils.defaultString(printData._tDivName));
            //5教科平均
            if (printData._confAvg5 != null && !"".equals(printData._confAvg5)) {
                //SQLで小数1位で四捨五入しているので、小数1位より下の余計な文字列を切り取る。
                int confavg5len = KNJ_EditEdit.getMS932ByteLength(printData._confAvg5);
                String confAvg5OutputStr = printData._confAvg5;
                int dotidx = printData._confAvg5.indexOf('.');
                if (dotidx >= 0 && confavg5len - dotidx + 1 > 1) {
                    confAvg5OutputStr = confAvg5OutputStr.substring(0, dotidx+2);
                } else {
                    //ドットが無いので、先頭5文字を出力(残りは見切れ扱い)
                    if (confavg5len > 5) {
                        confAvg5OutputStr = confAvg5OutputStr.substring(0, 5);
                    }
                }
                svf.VrsOutn("AVERAGE" , printLine, confAvg5OutputStr);
            }
            //面接
            svf.VrsOutn("INTERVIEW_A", printLine, StringUtils.defaultString(printData._interview1));
            svf.VrsOutn("INTERVIEW_B", printLine, StringUtils.defaultString(printData._interview2));
            //第一専攻
            svf.VrsOutn("HOPE1", printLine, StringUtils.defaultString(printData._major1));
            //評価
            svf.VrsOutn("EVA1_1", printLine, StringUtils.defaultString(printData._otherRemark1));
            //視唱
            svf.VrsOutn("EVA1_2", printLine, StringUtils.defaultString(printData._otherRemark2));
            //第二専攻
            svf.VrsOutn("HOPE2", printLine, StringUtils.defaultString(printData._major2));
            //評価
            svf.VrsOutn("EVA2_1", printLine, StringUtils.defaultString(printData._otherRemark4));
            //視唱
            svf.VrsOutn("EVA2_2", printLine, StringUtils.defaultString(printData._otherRemark5));

            printLine++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
        String setYear = KNJ_EditDate.getAutoFormatYearNen(db2, _param._entExamYear + "/04/01");
        final String printDateTime = KNJ_EditDate.getAutoFormatDate(db2, _param._loginDate);// + "　" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
        svf.VrsOut("DATE", printDateTime);
        svf.VrsOut("TITLE", setYear + "度　" + _param._testdivName + "　音楽科入学試験成績一覧");
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String judgement = rs.getString("JUDGEMENT");
            	final String examNo = rs.getString("EXAMNO");
            	final String name = rs.getString("NAME");
            	final String fs_Cd = rs.getString("FS_CD");
            	final String finSchoolAbbv = rs.getString("FINSCHOOL_NAME_ABBV");
            	final String tDivName = rs.getString("TDIVNAME");
            	final String interview1 = rs.getString("INTERVIEW1");
            	final String interview2 = rs.getString("INTERVIEW2");
            	final String major1 = rs.getString("MAJOR1");
            	final String otherRemark1 = rs.getString("OTHER_REMARK1");
            	final String otherRemark2 = rs.getString("OTHER_REMARK2");
            	final String otherRemark3 = rs.getString("OTHER_REMARK3");
            	final String major2 = rs.getString("MAJOR2");
            	final String otherRemark4 = rs.getString("OTHER_REMARK4");
            	final String otherRemark5 = rs.getString("OTHER_REMARK5");
            	final String otherRemark6 = rs.getString("OTHER_REMARK6");
            	final String app =  rs.getString("APP");
            	final String confAvg5 = rs.getString("CONF_AVG5");

                final PrintData printData = new PrintData(judgement, examNo, name, fs_Cd, finSchoolAbbv, tDivName, interview1, interview2, major1, otherRemark1, otherRemark2, otherRemark3, major2, otherRemark4, otherRemark5, otherRemark6, confAvg5, app);
                retList.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.JUDGEMENT, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.FS_CD, ");
        stb.append("     FM1.FINSCHOOL_NAME_ABBV, ");
        stb.append("     L045.ABBV1 AS TDIVNAME, ");
        stb.append("     L027_1.NAME1 AS INTERVIEW1, ");
        stb.append("     L027_2.NAME1 AS INTERVIEW2, ");
        stb.append("     T2.REMARK1 AS MAJOR1, ");
        stb.append("     M1.OTHER_REMARK1, ");
        stb.append("     M1.OTHER_REMARK2, ");
        stb.append("     M1.OTHER_REMARK3, ");
        stb.append("     T2.REMARK2 AS MAJOR2, ");
        stb.append("     M1.OTHER_REMARK4, ");
        stb.append("     M1.OTHER_REMARK5, ");
        stb.append("     M1.OTHER_REMARK6, ");
        //5教科の平均
        stb.append("     CASE WHEN T3.TOTAL5 IS NULL AND T3_001.REMARK10 IS NULL AND T3_002.REMARK10 IS NULL THEN NULL ");
        stb.append("          ELSE ROUND((VALUE(T3.TOTAL5, 0) + INT(VALUE(T3_001.REMARK10, '0')) + INT(VALUE(T3_002.REMARK10, '0'))) / 15.0 , 1) END AS CONF_AVG5, ");
        stb.append("     CASE WHEN L061.NAMESPARE1 = '1' THEN '併' ELSE '' END AS APP ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_HALL_GROUP_DAT H1 ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("      ON T1.ENTEXAMYEAR = H1.ENTEXAMYEAR ");
        stb.append("     AND T1.APPLICANTDIV = H1.APPLICANTDIV ");
        stb.append("     AND T1.TESTDIV = H1.TESTDIV ");
        stb.append("     AND T1.EXAMNO = H1.EXAMNO ");
        //stb.append("     AND T1.JUDGEMENT <> '3' ");  //欠席者を除外
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ");
        stb.append("      ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("     AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("     AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("     AND T2.SEQ = '033' ");
        stb.append("    LEFT JOIN FINSCHOOL_MST FM1 ");
        stb.append("      ON T1.FS_CD = FM1.FINSCHOOLCD ");
        stb.append("    LEFT JOIN NAME_MST L045 ");
        stb.append("      ON L045.NAMECD1 = 'L045' ");
        stb.append("     AND L045.NAMECD2 = T1.TESTDIV1 ");
        stb.append("    LEFT JOIN ENTEXAM_INTERVIEW_DAT M1 ");
        stb.append("      ON M1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("     AND M1.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("     AND M1.TESTDIV = T1.TESTDIV ");
        stb.append("     AND M1.EXAMNO = T1.EXAMNO ");
        stb.append("    LEFT JOIN NAME_MST L027_1 ");
        stb.append("      ON L027_1.NAMECD1 = 'L027' ");
        stb.append("     AND L027_1.NAMECD2 = M1.INTERVIEW_A ");
        stb.append("    LEFT JOIN NAME_MST L027_2 ");
        stb.append("      ON L027_2.NAMECD1 = 'L027' ");
        stb.append("     AND L027_2.NAMECD2 = M1.INTERVIEW_B ");
        stb.append("    LEFT JOIN NAME_MST L013 ");
        stb.append("      ON L013.NAMECD1 = 'L013' ");
        stb.append("     AND L013.NAMECD2 = T1.JUDGEMENT ");
        stb.append("    LEFT JOIN NAME_MST L061 ");
        stb.append("      ON L013.NAMECD1 = 'L061' ");
        stb.append("     AND L013.NAMECD2 = T2.REMARK3 ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T3 ");
        stb.append("      ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("     AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("     AND T3.EXAMNO = T1.EXAMNO ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T3_001 ");
        stb.append("      ON T3_001.ENTEXAMYEAR = T3.ENTEXAMYEAR ");
        stb.append("     AND T3_001.APPLICANTDIV = T3.APPLICANTDIV ");
        stb.append("     AND T3_001.EXAMNO = T3.EXAMNO ");
        stb.append("     AND T3_001.SEQ = '001' ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T3_002 ");
        stb.append("      ON T3_002.ENTEXAMYEAR = T3.ENTEXAMYEAR ");
        stb.append("     AND T3_002.APPLICANTDIV = T3.APPLICANTDIV ");
        stb.append("     AND T3_002.EXAMNO = T3.EXAMNO ");
        stb.append("     AND T3_002.SEQ = '002' ");
        stb.append(" WHERE ");
        stb.append("     H1.ENTEXAMYEAR = '" + _param._entExamYear + "' ");
        stb.append("     AND H1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND H1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND H1.EXAMHALL_TYPE = '1' ");  //1:面接(固定で指定)
        stb.append(" ORDER BY ");
        stb.append("     T1.ENTEXAMYEAR, ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T1.EXAMNO ");

        return stb.toString();
    }

    private class PrintData {
    	final String _judgement;
    	final String _examNo;
    	final String _name;
    	final String _fs_Cd;
    	final String _finSchoolAbbv;
    	final String _tDivName;
    	final String _interview1;
    	final String _interview2;
    	final String _major1;
    	final String _otherRemark1;
    	final String _otherRemark2;
    	final String _otherRemark3;
    	final String _major2;
    	final String _otherRemark4;
    	final String _otherRemark5;
    	final String _otherRemark6;
    	final String _confAvg5;
    	final String _app;

        public PrintData(
        		final String judgement,
        		final String examNo,
        		final String name,
        		final String fs_Cd,
        		final String finSchoolAbbv,
        		final String tDivName,
        		final String interview1,
        		final String interview2,
        		final String major1,
        		final String otherRemark1,
        		final String otherRemark2,
        		final String otherRemark3,
        		final String major2,
        		final String otherRemark4,
        		final String otherRemark5,
        		final String otherRemark6,
        		final String confAvg5,
        		final String app
        ) {
            _judgement = judgement;
            _examNo = examNo;
            _name = name;
            _fs_Cd = fs_Cd;
            _finSchoolAbbv = finSchoolAbbv;
            _tDivName = tDivName;
            _interview1 = interview1;
            _interview2 = interview2;
            _major1 = major1;
            _otherRemark1 = otherRemark1;
            _otherRemark2 = otherRemark2;
            _otherRemark3 = otherRemark3;
            _major2 = major2;
            _otherRemark4 = otherRemark4;
            _otherRemark5 = otherRemark5;
            _otherRemark6 = otherRemark6;
            _confAvg5 = confAvg5;
            _app = app;
          }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 64664 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginDate;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _entExamYear;
        private final String _testdivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginDate     = request.getParameter("LOGIN_DATE");
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _entExamYear   = request.getParameter("ENTEXAMYEAR");
            _applicantDiv  = request.getParameter("APPLICANTDIV");
            _testDiv       = request.getParameter("TESTDIV");
            _testdivName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L004", _testDiv));
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
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
