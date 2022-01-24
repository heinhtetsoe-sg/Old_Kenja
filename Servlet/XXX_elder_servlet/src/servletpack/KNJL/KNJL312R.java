/*
 * $Id: ff64c10aa13cf016e3f65f0183ea4c2d1bc6f7a3 $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.Date;
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３１２Ｒ＞  得点チェックリスト
 **/
public class KNJL312R {

    private static final Log log = LogFactory.getLog(KNJL312R.class);

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

        final String form = "KNJL312R.frm";
        final int maxLine = 40;
        final int maxCol = 3;
        final List hallList = Hall.load(db2, _param);

        for (final Iterator it = hallList.iterator(); it.hasNext();) {
            final Hall hall = (Hall) it.next();

            final List pageList = getPageList(hall._subclassScoreList, maxLine * maxCol);
            for (int pi = 0; pi < pageList.size(); pi++) {
                final List scoreList = (List) pageList.get(pi);

                svf.VrSetForm(form, 1);

                svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度"); // NENDO
                svf.VrsOut("APPLICANTDIV", _param._testdivName); // 入試制度
                svf.VrsOut("DATE", _param._dateStr); // 作成日
                svf.VrsOut("PAGE1", String.valueOf(pi + 1)); // ページ
                svf.VrsOut("PAGE2", String.valueOf(pageList.size())); // ページ
                final int sline = pi * maxLine * maxCol;

                for (int i = 0; i < scoreList.size(); i++) {
                    final SubclassScore subScore = (SubclassScore) scoreList.get(i);
                    //科目コードがNULLではない人の科目名称を取得し、設定する(必ず上書きだと、最後の人の科目コードがNULLだと空になるため)。
                    if (subScore._testsubclasscd != null && !"".equals(subScore._testsubclasscd)) {
                        svf.VrsOut("SUBCLASS", subScore._testsubclassname); // 科目
                    }
                    int line = i + 1;
                    final String col;
                    if (line <= maxLine * 1) {
                        col = "1";
                        line -= 0;
                    } else if (line <= maxLine * 2) {
                        col = "2";
                        line -= maxLine;
                    } else { // if (line <= maxLine * maxCol) {
                        col = "3";
                        line -= maxLine * 2;
                    }
                    svf.VrsOut("EXAM_PLACE" + col, hall._examhallName); // 試験会場
                    svf.VrsOutn("RECEPTNO" + col, line, String.valueOf(sline + line)); // 座席番号
                    if ("2".equals(_param._applicantdiv)) {
                        svf.VrsOutn("EXAMNO" + col, line, subScore._receptno); // 受験番号
                    } else {
                        svf.VrsOutn("EXAMNO" + col, line, subScore._examno); // 受験番号
                    }
                    svf.VrsOutn("POINT" + col, line, subScore._score); // 得点
                }

                if (pi == pageList.size() - 1) { // 最後のページ
                    svf.VrsOut("NOTE", getNote(hall)); // 備考
                }
                svf.VrEndPage();
                _hasData = true;
            }
        }
    }

    private String getNote(final Hall hall) {
        final Map scoreListMap = new HashMap();
        for (int i = 0; i < hall._subclassScoreList.size(); i++) {
            final SubclassScore subScore = (SubclassScore) hall._subclassScoreList.get(i);
            if (null == subScore._sex) {
                continue;
            }
            if (null == scoreListMap.get(subScore._sex)) {
                scoreListMap.put(subScore._sex, new ArrayList());
            }
            ((List) scoreListMap.get(subScore._sex)).add(subScore._examno);
        }
        final StringBuffer stb = new StringBuffer();
        int total = 0;
        String comma = "";
        for (final Iterator its = _param._sexNameMap.keySet().iterator(); its.hasNext();) {
            final String sex = (String) its.next();
            final String name = (String) _param._sexNameMap.get(sex);
            final List list = (List) scoreListMap.get(sex);
            if (null == name) {
                continue;
            }
            final int count = null == list ? 0 : list.size();
            stb.append(comma).append(name).append(count).append("名");
            total += count;
            comma = "、";
        }
        stb.append(comma).append("合計").append(total).append("名");
        final String note = stb.toString();
        return note;
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

    private static class Hall {
        final String _examType;
        final String _examhallcd;
        final String _sReceptno;
        final String _eReceptno;
        final String _examhallName;
        final List _subclassScoreList;

        Hall(
            final String examType,
            final String examhallcd,
            final String sReceptno,
            final String eReceptno,
            final String examhallName
        ) {
            _examType = examType;
            _examhallcd = examhallcd;
            _sReceptno = sReceptno;
            _eReceptno = eReceptno;
            _examhallName = examhallName;
            _subclassScoreList = new ArrayList();
        }

        private static Hall getHall(final List list, final String examhallcd) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Hall hall = (Hall) it.next();
                if (hall._examhallcd.equals(examhallcd)) {
                    return hall;
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
                    final String examhallcd = rs.getString("EXAMHALLCD");
                    if (null == getHall(list, examhallcd)) {
                        final String examType = rs.getString("EXAM_TYPE");
                        final String sReceptno = rs.getString("S_RECEPTNO");
                        final String eReceptno = rs.getString("E_RECEPTNO");
                        final String examhallName = rs.getString("EXAMHALL_NAME");
                        final Hall hall = new Hall(examType, examhallcd, sReceptno, eReceptno, examhallName);
                        list.add(hall);
                    }

                    final Hall hall = getHall(list, examhallcd);
                    final String receptno = rs.getString("RECEPTNO");
                    final String sex = rs.getString("SEX");
                    final String examno = rs.getString("EXAMNO");
                    final String testsubclasscd = rs.getString("TESTSUBCLASSCD");
                    final String testsubclassname = rs.getString("TESTSUBCLASSNAME");
                    final String score = rs.getString("SCORE");
                    final SubclassScore subclassScore = new SubclassScore(receptno, sex, examno, testsubclasscd, testsubclassname, score);
                    hall._subclassScoreList.add(subclassScore);
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
            stb.append("     T1.EXAM_TYPE, ");
            stb.append("     T1.EXAMHALLCD, ");
            stb.append("     T1.S_RECEPTNO, ");
            stb.append("     T1.E_RECEPTNO, ");
            stb.append("     T1.EXAMHALL_NAME, ");
            stb.append("     T2.RECEPTNO, ");
            stb.append("     BASE.SEX, ");
            stb.append("     NMZ002.NAME2 AS SEX_NAME, ");
            stb.append("     T2.EXAMNO, ");
            stb.append("     T3.TESTSUBCLASSCD, ");
            if ("2".equals(param._applicantdiv)) {
                stb.append("     NML009.NAME2 AS TESTSUBCLASSNAME, ");
            } else {
                stb.append("     NML009.NAME1 AS TESTSUBCLASSNAME, ");
            }
            stb.append("     T3.SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_HALL_YDAT T1 ");
            stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND T2.TESTDIV = T1.TESTDIV ");
            stb.append("         AND T2.EXAM_TYPE = T1.EXAM_TYPE ");
            stb.append("         AND T2.RECEPTNO BETWEEN T1.S_RECEPTNO AND T1.E_RECEPTNO ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
            stb.append("         AND BASE.APPLICANTDIV = T2.APPLICANTDIV ");
            stb.append("         AND BASE.EXAMNO       = T2.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T3 ON T3.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
            stb.append("         AND T3.APPLICANTDIV = T2.APPLICANTDIV ");
            stb.append("         AND T3.TESTDIV = T2.TESTDIV ");
            stb.append("         AND T3.EXAM_TYPE = T2.EXAM_TYPE ");
            stb.append("         AND T3.RECEPTNO = T2.RECEPTNO ");
            stb.append("         AND T3.TESTSUBCLASSCD = '" + param._testsubclasscd + "' ");
            stb.append("     LEFT JOIN NAME_MST NML009 ON NML009.NAMECD1 = 'L009' ");
            stb.append("         AND NML009.NAMECD2 = T3.TESTSUBCLASSCD ");
            stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' ");
            stb.append("         AND NMZ002.NAMECD2 = BASE.SEX ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND T1.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND T1.EXAM_TYPE = '1' "); // 固定
            stb.append("     AND T1.EXAMHALLCD IN " + SQLUtils.whereIn(true, param._categoryName) + " ");
            stb.append(" ORDER BY ");
            stb.append("     T1.EXAMHALLCD ");
            if ("2".equals(param._applicantdiv)) {
                stb.append("     , T2.RECEPTNO ");
            }
            stb.append("   , T2.EXAMNO ");
            return stb.toString();
        }
    }

    private static class SubclassScore {
        final String _receptno;
        final String _sex;
        final String _examno;
        final String _testsubclasscd;
        final String _testsubclassname;
        final String _score;

        SubclassScore(
            final String receptno,
            final String sex,
            final String examno,
            final String testsubclasscd,
            final String testsubclassname,
            final String score
        ) {
            _receptno = receptno;
            _sex = sex;
            _examno = examno;
            _testsubclasscd = testsubclasscd;
            _testsubclassname = testsubclassname;
            _score = score;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 64564 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _testsubclasscd;
        final String[] _categoryName;
        final String _date;

        final String _applicantdivName;
        final String _testdivName;
        final String _dateStr;
        final Map _sexNameMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _testsubclasscd = request.getParameter("TESTSUBCLASSCD");
            _categoryName = request.getParameterValues("category_name");
            for (int i = 0; i < _categoryName.length; i++) {
                _categoryName[i] = StringUtils.split(_categoryName[i], "-")[0];
            }
            _date = request.getParameter("CTRL_DATE");
            _dateStr = getDateStr(db2, _date);

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            if ("2".equals(_applicantdiv)) {
                _testdivName = getNameMst(db2, "NAME1", "L024", _testdiv);
            } else {
                _testdivName = getNameMst(db2, "NAME1", "L004", _testdiv);
            }
            _sexNameMap = getNameMstMap(db2, "NAME2", "Z002");
        }

        private String getDateStr(final DB2UDB db2, final String date) {
            if (null == date) {
                return null;
            }
            final Calendar cal = Calendar.getInstance();
            final DecimalFormat df = new DecimalFormat("00");
            final String hour = df.format(cal.get(Calendar.HOUR_OF_DAY));
            final String min = df.format(cal.get(Calendar.MINUTE));
            cal.setTime(Date.valueOf(date));
            final String youbi = StringUtils.defaultString(new String[] {null, "日", "月", "火", "水", "木", "金", "土"} [cal.get(Calendar.DAY_OF_WEEK)]);
            return KNJ_EditDate.h_format_JP(db2, date) + "（" + youbi + "） " + hour + ":" + min;
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

        private static Map getNameMstMap(final DB2UDB db2, final String field, final String namecd1) {
            Map rtn = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAMECD2, " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("NAMECD2"), rs.getString(field));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof

