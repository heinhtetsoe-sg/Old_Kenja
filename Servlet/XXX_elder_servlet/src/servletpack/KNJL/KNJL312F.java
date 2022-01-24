package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
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


/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL312F {

    private static final Log log = LogFactory.getLog(KNJL312F.class);

    private boolean _hasData;
    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        log.fatal("$Revision: 65618 $");

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

        //print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //svf設定
        svf.VrInit();    //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

        //ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        try {
            KNJServletUtils.debugParam(request, log);
            _param = new Param(db2, request);

            //SVF出力
            setSvfMain(db2, svf); //帳票出力のメソッド

        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

            //終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();       //DBを閉じる
            outstrm.close();   //ストリームを閉じる
        }
    }

    private List getDataList(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        ResultSetMetaData meta;
        try {
            final String sql = getDataSql(_param);
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            meta = rs.getMetaData();
            while (rs.next()) {
                if (null == rs.getString("PERFECT")) {
                    continue;
                }
                final Map m = new HashMap();
                for (int i = 1; i<= meta.getColumnCount(); i++) {
                    final String columnName = meta.getColumnLabel(i);
                    final String data = rs.getString(columnName);
                    m.put(columnName, data);
                }
                list.add(m);
            }
        } catch (Exception e) {
            log.debug("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }


    private void setSvfMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List dataListAll = getDataList(db2);

        final String form = "KNJL312F.frm";
        final int maxLine = 25 * 3;

        final List pageList = getPageList(dataListAll, maxLine);

        for (int pi = 0; pi < pageList.size(); pi++) {
            svf.VrSetForm(form, 1);
            svf.VrsOut("DATE", _param._currentTime); // 作成日
            svf.VrsOut("TITLE", "得点入力確認表"); // タイトル
            svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度"); // 入試年度
            svf.VrsOut("KIND", _param._applicantdivname); // 入試制度
            if ("2".equals(_param._applicantdiv)) {
                svf.VrsOut("BELONG_HEADER", StringUtils.defaultString(_param._testdivname) + "(" + _param._testdiv0name + ")");
                svf.VrsOut("BELONG", _param._examcoursename); // 志望所属
            } else {
                svf.VrsOut("BELONG_HEADER", StringUtils.defaultString(_param._testdivname));
            }
            svf.VrsOut("SUBJECT", _param._testsubclassname); // 科目

            final List dataList = (List) pageList.get(pi);
            for (int j = 0; j < dataList.size(); j++) {
                final Map m = (Map) dataList.get(j);
                int line = j + 1;
                final String div;
                if (j < 25) {
                    div = "1";
                    line -= 0;
                } else if (j < 50) {
                    div = "2";
                    line -= 25;
                } else { // j < 75
                    div = "3";
                    line -= 50;
                }

                svf.VrsOutn("NO" + div, line, String.valueOf(pi * maxLine + j + 1)); // NO
                svf.VrsOutn("EXAM_NO" + div, line, getString(m, "RECEPTNO")); // 受験番号
                final int namelen = getMS932Bytecount(getString(m, "NAME"));
                if (namelen <= 10) {
                    svf.VrsOutn("NAME" + div + "_1", line, getString(m, "NAME")); // 氏名
                } else if (namelen <= 16) {
                    svf.VrsOutn("NAME" + div + "_2", line, getString(m, "NAME")); // 氏名
                } else if (namelen <= 20) {
                    svf.VrsOutn("NAME" + div + "_3", line, getString(m, "NAME")); // 氏名
                } else {
                    svf.VrsOutn("NAME" + div + "_4_1", line, getString(m, "NAME")); // 氏名
                }
                if ("0".equals(getString(m, "ATTEND_FLG"))) {
                    svf.VrsOutn("SCORE" + div, line, "*"); // 素点
                } else {
                    svf.VrsOutn("SCORE" + div, line, getString(m, "SCORE")); // 素点
                }
            }

            if (pi == pageList.size() -1) {
                svf.VrsOut("TOTAL", "合計" + String.valueOf(dataListAll.size()) + "名"); // 合計
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private static String getString(final Map m, final String field) {
        if (null == m) {
            return null;
        }
        if (!m.containsKey(field)) {
            log.error("not defined: " + field + " in " + m.keySet());
        }
        return (String) m.get(field);
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private String getDataSql(final Param param) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.RECEPTNO ");
        stb.append("     ,APBASE.NAME AS NAME ");
        stb.append("     ,TPEM.PERFECT ");
        stb.append("     ,TSCORE.SCORE ");
        stb.append("     ,TSCORE.ATTEND_FLG ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_RECEPT_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT APBASE ON APBASE.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("                                    AND APBASE.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("                                    AND APBASE.EXAMNO       = T1.EXAMNO ");
        if ("1".equals(param._specialReasonDiv)) {
            stb.append("                                    AND APBASE.SPECIAL_REASON_DIV IS NOT NULL ");
        }
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD001 ON APD001.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("                                    AND APD001.EXAMNO = T1.EXAMNO ");
        stb.append("                                    AND APD001.SEQ       = '001' ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECD003 ON RECD003.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("                                    AND RECD003.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("                                    AND RECD003.TESTDIV = T1.TESTDIV ");
        stb.append("                                    AND RECD003.EXAM_TYPE = '1' ");
        stb.append("                                    AND RECD003.RECEPTNO = T1.RECEPTNO ");
        stb.append("                                    AND RECD003.SEQ       = '003' ");
        stb.append("     LEFT JOIN V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT VEATD ON VEATD.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("                                    AND VEATD.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("                                    AND VEATD.EXAMNO       = T1.EXAMNO ");
        stb.append("                                    AND VEATD.TESTDIV       = T1.TESTDIV ");
        stb.append("                                    AND VEATD.RECEPTNO       = T1.RECEPTNO ");
        stb.append("     LEFT JOIN ENTEXAM_PERFECT_EXAMTYPE_MST TPEM ON TPEM.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("                                    AND TPEM.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("                                    AND TPEM.TESTDIV       = T1.TESTDIV ");
        stb.append("                                    AND TPEM.COURSECD       = APD001.REMARK8 ");
        stb.append("                                    AND TPEM.MAJORCD       = APD001.REMARK9 ");
        stb.append("                                    AND TPEM.EXAMCOURSECD       = APD001.REMARK10 ");
        if ("2".equals(param._applicantdiv)) {
            stb.append("                                    AND TPEM.EXAM_TYPE       = '1' ");
        } else {
            stb.append("                                    AND TPEM.EXAM_TYPE       = VEATD.EXAM_TYPE ");
        }
        stb.append("                                    AND TPEM.TESTSUBCLASSCD = '" + param._testsubclasscd + "' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT TSCORE ON TSCORE.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND TSCORE.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND TSCORE.TESTDIV = T1.TESTDIV ");
        stb.append("         AND TSCORE.RECEPTNO = T1.RECEPTNO ");
        stb.append("         AND TSCORE.TESTSUBCLASSCD = '" + param._testsubclasscd + "' ");
        stb.append(" WHERE ");
        stb.append("         T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + param._testdiv + "' ");
        if (!"1".equals(param._specialReasonDiv)) {
            stb.append("     AND VALUE(T1.JUDGEDIV,'') <> '4' ");
        }
        if ("2".equals(param._applicantdiv)) {
            stb.append("     AND RECD003.REMARK1 = '" + param._testdiv0 + "' ");
            stb.append("     AND APD001.REMARK8 || '-' || APD001.REMARK9 || '-' || APD001.REMARK10 = '" + param._examcourse + "' ");
            if (("3001".equals(param._examcourse4) && "1".equals(param._testdiv) && ("2".equals(param._testsubclasscd) || "5".equals(param._testsubclasscd))) ||
                ("7".equals(param._testdiv) && ("2".equals(param._testsubclasscd) || "5".equals(param._testsubclasscd) || "9".equals(param._testsubclasscd)))) {
                stb.append("     AND APBASE.SELECT_SUBCLASS_DIV = '" + param._testsubclasscd + "' ");
            }
        }
        stb.append("     AND T1.EXAM_TYPE = '1' ");
        stb.append(" ORDER BY T1.RECEPTNO ");
        return stb.toString();
    }

    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _examcourse;
        String _examcourse4;
        final String _examcoursename;
        final String _testdiv;
        final String _testdiv0;
        final String _testsubclasscd;
        final String _specialReasonDiv;
        final String _loginDate;
        final String _applicantdivname;
        final String _testdivname;
        final String _testdiv0name;
        final String _testsubclassname;
        final String _currentTime;

        final String _z010Name1;

        private boolean _seirekiFlg;

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _entexamyear  = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _examcourse   = request.getParameter("EXAMCOURSE");
            _testdiv      = request.getParameter("TESTDIV");
            _testdiv0     = request.getParameter("TESTDIV0");
            _testsubclasscd = request.getParameter("TESTSUBCLASSCD");
            _specialReasonDiv = request.getParameter("SPECIAL_REASON_DIV");
            _loginDate    = request.getParameter("LOGIN_DATE");
            _z010Name1 = getSchoolName(db2);
            _applicantdivname = getApplicantdivName(db2);
            _testdivname = getTestdivName(db2);
            _testdiv0name = getTestdiv0Name(db2);
            _testsubclassname = getTestsubclasName(db2);
            _examcoursename = getExamcoursename(db2);
            _currentTime = currentTime(db2);

            _seirekiFlg = getSeirekiFlg(db2);

            final String[] split = StringUtils.split(_examcourse, "-");
            if (null != split && split.length >= 3) {
                _examcourse4  = split[2];
            }
        }

        /* 西暦表示にするのかのフラグ  */
        private boolean getSeirekiFlg(final DB2UDB db2) {
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
            boolean seirekiFlg = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return seirekiFlg;
        }

        private String getExamcoursename(DB2UDB db2) {
//            if ("1".equals(_examcourse)) {
//                return "理数キャリア";
//            } else if ("2".equals(_examcourse)) {
//                return "国際教養";
//            } else if ("3".equals(_examcourse)) {
//                return "スポーツ科学";
//            }
//            return "";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_COURSE_MST ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR  = '" + _entexamyear + "' AND ");
            stb.append("     APPLICANTDIV = '" + _applicantdiv + "' AND ");
            stb.append("     TESTDIV      = '1' ");
            stb.append("     AND COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD = '" + _examcourse + "'");

            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("LABEL");
                }
            } catch (Exception e) {
                log.error("getSchoolName Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getSchoolName(DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   NAME1 ");
            stb.append(" FROM ");
            stb.append("   NAME_MST T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.NAMECD1 = 'Z010' ");
            stb.append("   AND T1.NAMECD2 = '00' ");

            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.error("getSchoolName Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        private String gethiduke(final DB2UDB db2, final String inputDate) {
            // 西暦か和暦はフラグで判断
            String date;
            if (null != inputDate) {
                if (_seirekiFlg) {
                    date = inputDate.toString().substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(inputDate);
                } else {
                    date = KNJ_EditDate.h_format_JP(db2, inputDate);
                }
                return date;
            }
            return null;
        }

        private String getApplicantdivName(final DB2UDB db2) {
            String applicantdivName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantdiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  applicantdivName = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return applicantdivName;
        }

        private String getTestdivName(final DB2UDB db2) {
            final String namecd1 = "1".equals(_applicantdiv) ? "L024" : "L004";
            String testDivName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testdiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("ABBV1")) {
                  testDivName = rs.getString("ABBV1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testDivName;
        }

        private String getTestsubclasName(final DB2UDB db2) {
            final String namecd1 = "L009";
            final String field = "1".equals(_applicantdiv) ? "NAME1" : "NAME2";
            String testDivName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testsubclasscd + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString(field)) {
                  testDivName = rs.getString(field);
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testDivName;
        }

        private String getTestdiv0Name(final DB2UDB db2) {
            final String namecd1 = "L034";
            String testDiv0Name = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testdiv0 + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  testDiv0Name = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testDiv0Name;
        }

        private static String currentTime(final DB2UDB db2) {
            final Calendar cal = Calendar.getInstance();
            final int year = cal.get(Calendar.YEAR);
            final int month = cal.get(Calendar.MONTH) + 1;
            final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            final String dow = String.valueOf(" 日月火水木金土".charAt(cal.get(Calendar.DAY_OF_WEEK)));
            final int hour = cal.get(Calendar.HOUR_OF_DAY);
            final int min = cal.get(Calendar.MINUTE);
            final DecimalFormat df = new DecimalFormat("00");
            return KNJ_EditDate.h_format_JP(db2, year + "-" + month + "-" + dayOfMonth) + "(" + dow + ") " + df.format(hour) + ":" + df.format(min);
        }
    }
}//クラスの括り
