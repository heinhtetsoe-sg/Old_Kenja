/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 41a43bb7aae16418e01dc9911e8d4e90755f835e $
 *
 * 作成日: 2019/01/24
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

public class KNJL511G {

    private static final Log log = LogFactory.getLog(KNJL511G.class);

    private boolean _hasData;
    private final int MAX_LINE = 25;

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
        svf.VrSetForm("KNJL511G.frm", 1);

        final List printList = getList(db2);
        final int maxPage1 = printList.size() / MAX_LINE;
        final int maxPage2 = printList.size() % MAX_LINE;
        final int setMaxPage = maxPage1 + (maxPage2 > 0 ? 1 : 0);
        int lineCnt = 1;
        int pageCnt = 1;
        int renban = 1;
        int payTotal = 0;
        setTitle(db2, svf, setMaxPage, pageCnt);
        final Map totalMap = new TreeMap();
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            if (lineCnt > MAX_LINE) {
                svf.VrEndPage();
                pageCnt++;
                setTitle(db2, svf, setMaxPage, pageCnt);
                lineCnt = 1;
            }
            svf.VrsOutn("NO", lineCnt, String.valueOf(renban));
            svf.VrsOutn("EXAMNO", lineCnt, printData._examno);
            final String nameField = KNJ_EditEdit.getMS932ByteLength(printData._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(printData._name) > 20 ? "2" : "1";
            svf.VrsOutn("NAME" + nameField, lineCnt, printData._name);
            final String kanaField = KNJ_EditEdit.getMS932ByteLength(printData._nameKana) > 50 ? "3" : KNJ_EditEdit.getMS932ByteLength(printData._nameKana) > 20 ? "2" : "1";
            svf.VrsOutn("NAME_KANA" + kanaField, lineCnt, printData._nameKana);
            svf.VrsOutn("SEX", lineCnt, printData._sex);
            final String schoolField = KNJ_EditEdit.getMS932ByteLength(printData._finschoolName) > 20 ? "2" : "1";
            svf.VrsOutn("FINSCHOOL_NAME" + schoolField, lineCnt, printData._finschoolName);
            svf.VrsOutn("EXAMCOURSE_NAME1", lineCnt, printData._examcourseName);
            svf.VrsOutn("PASSCOURSE_NAME1", lineCnt, printData._passCourse);
            svf.VrsOutn("JUDGE", lineCnt, printData._passName);
            svf.VrsOutn("PAYMENT_DAY", lineCnt, KNJ_EditDate.h_format_JP_MD(printData._procedureDate));
            svf.VrsOutn("MONEY", lineCnt, printData._payMoney);
            final List remarkList = new ArrayList();
            if (!StringUtils.isEmpty(printData._remark1)) {
            	remarkList.add(printData._remark1);
            }
            if (!StringUtils.isEmpty(printData._remark2)) {
            	remarkList.add(printData._remark2);
            }
            if (!StringUtils.isEmpty(printData._remark3)) {
            	remarkList.add(printData._remark3);
            }
            if (remarkList.size() > 2) {
            	final StringBuffer stb = new StringBuffer();
            	for (int i = 0; i < remarkList.size(); i++) {
            		if (stb.length() > 0) {
            			stb.append(" ");
            		}
            		stb.append(remarkList.get(i));
            	}
            	String[] array = KNJ_EditEdit.get_token(stb.toString(), 22, 2);
            	if (null != array) {
            		for (int i = 0; i < array.length; i++) {
                        svf.VrsOutn("REMARK" + String.valueOf(i + 1), lineCnt, array[i]);
            		}
            	}
            } else {
            	for (int i = 0; i < remarkList.size(); i++) {
                    svf.VrsOutn("REMARK" + String.valueOf(i + 1), lineCnt, (String) remarkList.get(i));
            	}
            }
            if (null != printData._procedureDate && !"".equals(printData._procedureDate)) {
                final TotalData totalData;
                if (totalMap.containsKey(printData._procedureDate)) {
                    totalData = (TotalData) totalMap.get(printData._procedureDate);
                } else {
                    totalData = new TotalData(printData._procedureDate);
                }
                totalData.addData(Integer.parseInt(printData._payMoney));
                totalMap.put(printData._procedureDate, totalData);
            }

            lineCnt++;
            renban++;
            _hasData = true;
        }
        if (_hasData) {
            setTotalMoney(svf, totalMap);
            svf.VrEndPage();
        }
    }

    private void setTotalMoney(final Vrw32alp svf, final Map totalMap) {
        int lineCnt = 1;
        int totalCnt = 0;
        int totalMoney = 0;
        int otherCnt = 0;
        int otherMoney = 0;
        final List sortList = new ArrayList(totalMap.values());
        Collections.sort(sortList);
        for (Iterator itSotr = sortList.iterator(); itSotr.hasNext();) {
            final TotalData totalData = (TotalData) itSotr.next();

            if (lineCnt <= 2) {
                svf.VrsOutn("CONFIRM_DAY", lineCnt, KNJ_EditDate.h_format_JP_MD(totalData._day));
                svf.VrsOutn("TOTAL_MONEY", lineCnt, String.valueOf(totalData._money));
                svf.VrsOutn("NUM", lineCnt, String.valueOf(totalData._cnt));
            } else {
                otherCnt += totalData._cnt;
                otherMoney += totalData._money;
            }
            totalCnt += totalData._cnt;
            totalMoney += totalData._money;
            lineCnt++;
        }
        svf.VrsOutn("CONFIRM_DAY", 3, "上記以外");
        svf.VrsOutn("TOTAL_MONEY", 3, String.valueOf(otherMoney));
        svf.VrsOutn("NUM", 3, String.valueOf(otherCnt));

        svf.VrsOutn("CONFIRM_DAY", 4, "計");
        svf.VrsOutn("TOTAL_MONEY", 4, String.valueOf(totalMoney));
        svf.VrsOutn("NUM", 4, String.valueOf(totalCnt));
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final int maxPage, final int pageCnt) {
        svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)));
        svf.VrsOut("TITLE", _param._applicantdivName);
        svf.VrsOut("SUBTITLE", _param._testdivName);
        svf.VrsOut("PAGE1", String.valueOf(pageCnt));
        svf.VrsOut("PAGE2", String.valueOf(maxPage));
        svf.VrsOut("DATE", _param._printDayTime);
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
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String examcourseName = rs.getString("EXAMCOURSE_NAME");
                final String passCourse = rs.getString("PASS_COURSE");
                final String passName = rs.getString("PASS_NAME");
                final String procedureDate = rs.getString("PROCEDUREDATE");
                final String payMoney = rs.getString("PAY_MONEY");
                final String remark1 = rs.getString("REMARK1");
                final String remark2 = rs.getString("REMARK2");
                final String remark3 = rs.getString("REMARK3");
                final PrintData printData = new PrintData(examno, name, nameKana, sex, finschoolName, examcourseName, passCourse, passName, procedureDate, payMoney, remark1, remark2, remark3);
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
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     CASE WHEN FINSCHOOL.FINSCHOOLCD is null ");
        stb.append("          THEN '（' || value(BD005.REMARK1, '') || '）' ");
        stb.append("          ELSE FINSCHOOL.FINSCHOOL_NAME ");
        stb.append("     END AS FINSCHOOL_NAME, ");
        stb.append("     COURSE.EXAMCOURSE_NAME, ");
        stb.append("     L036_SEIKYU.NAME2 AS PASS_COURSE, ");
        stb.append("     L013.NAME1 AS PASS_NAME, ");
        stb.append("     BASE.PROCEDUREDATE, ");
        stb.append("     BASE.PAY_MONEY, ");
        stb.append("     L025.NAME1 AS REMARK1, ");
        stb.append("     CASE WHEN BD018.REMARK1 IS NOT NULL ");
        stb.append("          THEN '卒・在' ");
        stb.append("          ELSE '' ");
        stb.append("     END AS REMARK2, ");
        stb.append("     CASE WHEN BD021.REMARK2 IS NOT NULL ");
        stb.append("          THEN '入学支度金利用' ");
        stb.append("          ELSE '' ");
        stb.append("     END AS REMARK3 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     INNER JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ");
        stb.append("           AND BASE.JUDGEMENT = L013.NAMECD2 ");
        stb.append("           AND L013.NAMESPARE1 = '1' ");
        stb.append("     LEFT JOIN NAME_MST L036_SEIKYU ON L036_SEIKYU.NAMECD1 = 'L036' ");
        stb.append("          AND SUBSTR(BASE.SUC_COURSECODE, 3, 1) = L036_SEIKYU.ABBV2 ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND BASE.SEX = Z002.NAMECD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON BASE.FS_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD005 ON BASE.ENTEXAMYEAR = BD005.ENTEXAMYEAR ");
        stb.append("          AND BASE.APPLICANTDIV = BD005.APPLICANTDIV ");
        stb.append("          AND BASE.EXAMNO = BD005.EXAMNO ");
        stb.append("          AND BD005.SEQ = '005' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD001 ON BASE.ENTEXAMYEAR = BD001.ENTEXAMYEAR ");
        stb.append("          AND BASE.APPLICANTDIV = BD001.APPLICANTDIV ");
        stb.append("          AND BASE.EXAMNO = BD001.EXAMNO ");
        stb.append("          AND BD001.SEQ = '001' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE ON BASE.ENTEXAMYEAR = COURSE.ENTEXAMYEAR ");
        stb.append("          AND BASE.APPLICANTDIV = COURSE.APPLICANTDIV ");
        stb.append("          AND BASE.TESTDIV = COURSE.TESTDIV ");
        stb.append("          AND BD001.REMARK8 = COURSE.COURSECD ");
        stb.append("          AND BD001.REMARK9 = COURSE.MAJORCD ");
        stb.append("          AND BD001.REMARK10 = COURSE.EXAMCOURSECD ");
        stb.append("     LEFT JOIN NAME_MST L025 ON L025.NAMECD1 = 'L025' ");
        stb.append("          AND BASE.JUDGE_KIND = L025.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD018 ON BASE.ENTEXAMYEAR = BD018.ENTEXAMYEAR ");
        stb.append("          AND BASE.APPLICANTDIV = BD018.APPLICANTDIV ");
        stb.append("          AND BASE.EXAMNO = BD018.EXAMNO ");
        stb.append("          AND BD018.SEQ = '018' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD021 ON BASE.ENTEXAMYEAR = BD021.ENTEXAMYEAR ");
        stb.append("          AND BASE.APPLICANTDIV = BD021.APPLICANTDIV ");
        stb.append("          AND BASE.EXAMNO = BD021.EXAMNO ");
        stb.append("          AND BD021.SEQ = '021' ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND BASE.TESTDIV = '" + _param._testdiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     BASE.EXAMNO ");

        return stb.toString();
    }

    private class PrintData {
        private final String _examno;
        private final String _name;
        private final String _nameKana;
        private final String _sex;
        private final String _finschoolName;
        private final String _examcourseName;
        private final String _passCourse;
        private final String _passName;
        private final String _procedureDate;
        private final String _payMoney;
        private final String _remark1;
        private final String _remark2;
        private final String _remark3;
        public PrintData(
                final String examno,
                final String name,
                final String nameKana,
                final String sex,
                final String finschoolName,
                final String examcourseName,
                final String passCourse,
                final String passName,
                final String procedureDate,
                final String payMoney,
                final String remark1,
                final String remark2,
                final String remark3
        ) {
            _examno = examno;
            _name = name;
            _nameKana = nameKana;
            _sex = sex;
            _finschoolName = finschoolName;
            _examcourseName = examcourseName;
            _passCourse = passCourse;
            _passName = passName;
            _procedureDate = procedureDate;
            _payMoney = payMoney;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
        }
    }

    private class TotalData implements Comparable {
        private final String _day;
        private int _cnt;
        private int _money;
        public TotalData(
                final String day
        ) {
            _day = day;
            _cnt = 0;
            _money = 0;
        }
        private void addData(final int money) {
            _cnt++;
            _money += money;
        }
        public int compareTo(Object o) {
            return -_day.compareTo(((TotalData) o)._day);
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 73258 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _applicantdiv;
        private final String _testdiv;
        private final String _login_date;
        final String _applicantdivName;
        final String _testdivName;
        final String _printDayTime;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _login_date = request.getParameter("LOGIN_DATE");

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            final String testNameCd = "1".equals(_applicantdiv) ? "L024" : "L004";
            _testdivName = getNameMst(db2, "NAME1", testNameCd, _testdiv);
            final Calendar cal = Calendar.getInstance();
            final String sysDate = String.valueOf(cal.get(Calendar.YEAR)) + "-" + String.valueOf(cal.get(Calendar.MONTH) + 1) + "-" + String.valueOf(cal.get(Calendar.DATE));
            _printDayTime = new SimpleDateFormat("yyyy年MM月dd日").format(java.sql.Date.valueOf(sysDate)) + cal.get(Calendar.HOUR_OF_DAY) + "時" + cal.get(Calendar.MINUTE) + "分";
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
