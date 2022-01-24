package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
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

public class KNJL311F {

    private static final Log log = LogFactory.getLog(KNJL311F.class);

    private boolean _hasData;
    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        log.fatal("$Revision: 63564 $");

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
            print1(db2, svf); //帳票出力のメソッド

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

    private static List getReceptExamtypeGroupPageList(final List list) {
        final List rtn = new ArrayList();
        List current = null;
        String oldExamtype = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map m = (Map) it.next();
            if (null == current || null == oldExamtype || !oldExamtype.equals(getString(m, "EXAM_TYPE"))) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(m);
            oldExamtype = getString(m, "EXAM_TYPE");
        }
        return rtn;
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

    private static Map getMappedHashMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap());
        }
        return (Map) map.get(key1);
    }

    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static int getMS932ByteLength(String str) {
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

    private static List query(final DB2UDB db2, final String sql) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        ResultSetMetaData meta;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            meta = rs.getMetaData();
            while (rs.next()) {
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

    private String getReceptDataSql(final Param param) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.RECEPTNO ");
        stb.append("     ,APBASE.NAME AS NAME ");
        stb.append("     ,TRDET011.REMARK" + param._testdiv + " AS EXAM_TYPE ");
        stb.append("     ,NML005.NAME1 AS EXAM_TYPE_NAME ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_RECEPT_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT APBASE ON APBASE.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("                                    AND APBASE.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("                                    AND APBASE.EXAMNO       = T1.EXAMNO ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT TRDET011 ON TRDET011.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND TRDET011.EXAMNO = T1.EXAMNO ");
        stb.append("         AND TRDET011.SEQ = '011' ");
        stb.append("     LEFT JOIN NAME_MST NML005 ON NML005.NAMECD1 = 'L005' ");
        stb.append("         AND NML005.NAMECD2 = TRDET011.REMARK" + param._testdiv + " ");
        stb.append(" WHERE ");
        stb.append("         T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + param._testdiv + "' ");
        stb.append("     AND T1.EXAM_TYPE = '1' ");
        stb.append("     AND VALUE(T1.JUDGEDIV, '') NOT IN ('4') ");
//        stb.append("     AND VALUE(APBASE.JUDGEMENT, '') NOT IN ('4') ");
        if ("5".equals(param._testdiv)) {
        } else {
            stb.append("     AND T1.EXAMNO NOT IN (SELECT EXAMNO FROM V_ENTEXAM_RECEPT_DAT ");
            stb.append("                           WHERE ");
            stb.append("                                T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("                            AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("                            AND T1.TESTDIV < '" + param._testdiv + "' ");
            stb.append("                            AND T1.EXAM_TYPE = '1' ");
            stb.append("                       ) ");
        }
        stb.append(" ORDER BY TRDET011.REMARK" + param._testdiv + ", T1.RECEPTNO ");
        return stb.toString();
    }

    private void print1(final DB2UDB db2, final Vrw32alp svf) {

        final int maxLine = 20;

        final List receptExamtypeGroupList = getReceptExamtypeGroupPageList(query(db2, getReceptDataSql(_param)));

        for (int eti = 0; eti < receptExamtypeGroupList.size(); eti++) {
            final List receptExamtypeGroup = (List) receptExamtypeGroupList.get(eti);

            final List pageList = getPageList(receptExamtypeGroup, maxLine);

            final Map m0 = (Map) receptExamtypeGroup.get(0);
            final String examType = getString(m0, "EXAM_TYPE");

            final List printSubclassList = _param.getSubclassList(db2, examType);

            for (int subi = 0; subi < printSubclassList.size(); subi++) {
                final Map printSubclassMap = (Map) printSubclassList.get(subi);
                final List childSubclasscdList = getMappedList(printSubclassMap, "CHILD_SUBCLASSLIST");

                final String form;
                if (childSubclasscdList.size() == 0) {
                    form = "KNJL311F_1.frm";
                } else {
                    form = "KNJL311F_2.frm";
                }

                for (int pi = 0; pi < pageList.size(); pi++) {
                    svf.VrSetForm(form, 1);
                    svf.VrsOut("SUBCLASS_NAME", getString(printSubclassMap, "PRINT_SUBCLASSNAME")); // 科目名
                    if (childSubclasscdList.size() != 0) {
                        for (int i = 0; i < childSubclasscdList.size(); i++) {
                            final String childSubclasscd = (String) childSubclasscdList.get(i);
                            svf.VrsOut("SUBCLASS_NO" + String.valueOf(i + 1), getString(printSubclassMap, "SUBCLASS" + childSubclasscd + ".NAME")); // 科目番号
                        }
                    }
                    final List dataList = (List) pageList.get(pi);
                    for (int j = 0; j < dataList.size(); j++) {
                        final int line = j + 1;
                        final Map recept = (Map) dataList.get(j);
                        svf.VrsOut("TITLE", StringUtils.defaultString(_param._testdivname) + "入試　" + StringUtils.defaultString(getString(recept, "EXAM_TYPE_NAME")) + "　採点表"); // タイトル
                        svf.VrsOutn("NO", line, String.valueOf(pi * maxLine + j + 1)); // 番号
                        svf.VrsOutn("TIMES", line, _param._testdiv); // 回数
                        svf.VrsOutn("EXAM_NO", line, getString(recept, "RECEPTNO")); // 受験番号

                        final int ketaName = getMS932ByteLength(getString(recept, "NAME"));
                        svf.VrsOutn(ketaName <= 16 ? "NAME1" : ketaName <= 20 ? "NAME2" : ketaName <= 30 ? "NAME3" : "NAME4", line, getString(recept, "NAME"));
                    }
                    svf.VrEndPage();
                    _hasData = true;
                }
            }
        }
    }

    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
//        final String _examcourse;
        String _examcourse4;
//        final String _examcoursename;
        final String _testdiv;
//        final String _testdiv0;
        final String _testsubclasscd;
        final String _loginDate;
        final String _applicantdivname;
        final String _testdivname;
//        final String _testdiv0name;
//        final String _currentTime;

//        final String _z010Name1;

        private boolean _seirekiFlg;

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _entexamyear  = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv      = request.getParameter("TESTDIV");
//            _examcourse   = request.getParameter("EXAMCOURSE");
//            _testdiv0     = request.getParameter("TESTDIV0");
            _testsubclasscd = StringUtils.isEmpty(request.getParameter("TESTSUBCLASSCD")) ? null : request.getParameter("TESTSUBCLASSCD");
            _loginDate    = request.getParameter("LOGIN_DATE");
//            _z010Name1 = getSchoolName(db2);
            _applicantdivname = getApplicantdivName(db2);
            _testdivname = getTestdivName(db2);
//            _testdiv0name = getTestdiv0Name(db2);
//            _examcoursename = getExamcoursename(db2);
//            _currentTime = currentTime();

            _seirekiFlg = getSeirekiFlg(db2);

//            final String[] split = StringUtils.split(_examcourse, "-");
//            if (null != split && split.length >= 3) {
//                _examcourse4  = split[2];
//            }
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

//        private String getExamcoursename(DB2UDB db2) {
////            if ("1".equals(_examcourse)) {
////                return "理数キャリア";
////            } else if ("2".equals(_examcourse)) {
////                return "国際教養";
////            } else if ("3".equals(_examcourse)) {
////                return "スポーツ科学";
////            }
////            return "";
//            final StringBuffer stb = new StringBuffer();
//            stb.append(" SELECT DISTINCT ");
//            stb.append("     EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ");
//            stb.append(" FROM ");
//            stb.append("     ENTEXAM_COURSE_MST ");
//            stb.append(" WHERE ");
//            stb.append("     ENTEXAMYEAR  = '" + _entexamyear + "' AND ");
//            stb.append("     APPLICANTDIV = '" + _applicantdiv + "' AND ");
//            stb.append("     TESTDIV      = '1' ");
//            stb.append("     AND COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD = '" + _examcourse + "'");
//
//            String rtn = null;
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                ps = db2.prepareStatement(stb.toString());
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                    rtn = rs.getString("LABEL");
//                }
//            } catch (Exception e) {
//                log.error("getSchoolName Exception", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//            return rtn;
//        }

//        private String getSchoolName(DB2UDB db2) {
//            final StringBuffer stb = new StringBuffer();
//            stb.append(" SELECT ");
//            stb.append("   NAME1 ");
//            stb.append(" FROM ");
//            stb.append("   NAME_MST T1 ");
//            stb.append(" WHERE ");
//            stb.append("   T1.NAMECD1 = 'Z010' ");
//            stb.append("   AND T1.NAMECD2 = '00' ");
//
//            String name1 = null;
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                ps = db2.prepareStatement(stb.toString());
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                    name1 = rs.getString("NAME1");
//                }
//            } catch (Exception e) {
//                log.error("getSchoolName Exception", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//            return name1;
//        }

        private String gethiduke(final String inputDate) {
            // 西暦か和暦はフラグで判断
            String date;
            if (null != inputDate) {
                if (_seirekiFlg) {
                    date = inputDate.toString().substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(inputDate);
                } else {
                    date = KNJ_EditDate.h_format_JP(inputDate);
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
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testdiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  testDivName = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testDivName;
        }

        private List getSubclassList(final DB2UDB db2, final String examType) {

            final List printSubclassList = new ArrayList();

            final String name = "1".equals(_applicantdiv) ? "NAME1" : "NAME2";

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH TMP AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.NAMECD2 AS TESTSUBCLASSCD ");
            stb.append("     , T1." + name + " AS NAME ");
            stb.append("     , T1.NAMESPARE2 ");
            stb.append("     , T2.NAMECD2 AS PARENT_TESTSUBCLASSCD ");
            stb.append("     , T2." + name + " AS PARENT_NAME ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST T1 ");
            stb.append("     LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'L009' ");
            stb.append("         AND T2.NAMECD2 = T1.NAMESPARE2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR        = '" + _entexamyear + "' ");
            stb.append("     AND T1.NAMECD1     = 'L009' ");
            if (null != _testsubclasscd) {
                stb.append("     AND T1.NAMECD2 = '" + _testsubclasscd + "' ");
            }
            stb.append("     AND T1." + name + " IS NOT NULL ");
            //満点マスタの設定が前提
            stb.append("     AND T1.NAMECD2 IN (SELECT ");
            stb.append("                     TESTSUBCLASSCD ");
            stb.append("                 FROM ");
            stb.append("                     ENTEXAM_PERFECT_EXAMTYPE_MST ");
            stb.append("                 WHERE ");
            stb.append("                     ENTEXAMYEAR     = '" + _entexamyear + "' ");
            stb.append("                     AND APPLICANTDIV    = '" + _applicantdiv + "' ");
            stb.append("                     AND TESTDIV         = '" + _testdiv + "' ");
            stb.append("                     AND EXAM_TYPE         = '" + examType + "' ");
//            if ("2".equals(_applicantdiv)) {
//                stb.append("                 AND COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD = '" + examcoursecd + "' ");
//            }
            stb.append("                 ) ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     VALUE(T1.PARENT_TESTSUBCLASSCD, T1.TESTSUBCLASSCD) AS PRINT_SUBCLASSCD ");
            stb.append("   , CASE WHEN T1.PARENT_TESTSUBCLASSCD IS NOT NULL THEN T1.PARENT_NAME ");
            stb.append("         ELSE NAME");
            stb.append("     END AS PRINT_SUBCLASSNAME ");
            stb.append("   , RTRIM(CAST(ROW_NUMBER() OVER(PARTITION BY VALUE(T1.PARENT_TESTSUBCLASSCD, T1.TESTSUBCLASSCD) ORDER BY T1.TESTSUBCLASSCD) AS CHAR(2))) AS CHILDID ");
            stb.append("   , CASE WHEN T1.PARENT_TESTSUBCLASSCD IS NOT NULL THEN RTRIM(CAST(ROW_NUMBER() OVER(PARTITION BY T1.PARENT_TESTSUBCLASSCD ORDER BY T1.TESTSUBCLASSCD) AS CHAR(2))) ");
            stb.append("         ELSE NAME");
            stb.append("     END AS SUBCLASSNAME ");
            stb.append("   , T1.TESTSUBCLASSCD ");
            stb.append("   , T1.PARENT_TESTSUBCLASSCD ");
            stb.append(" FROM TMP T1 ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE(T1.PARENT_TESTSUBCLASSCD, T1.TESTSUBCLASSCD) ");
            stb.append("     , T1.TESTSUBCLASSCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            ResultSetMetaData meta;
            try {
                log.info(" sql = " + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                meta = rs.getMetaData();
                while (rs.next()) {
                    final String printSubclasscd = rs.getString("PRINT_SUBCLASSCD");

                    if (null != rs.getString("PARENT_TESTSUBCLASSCD")) {
                        boolean addChild = false;
                        addChild = setParent(printSubclassList, rs.getString("TESTSUBCLASSCD"), printSubclasscd, addChild);
                        if (addChild) {
                            continue;
                        }

                        final Map m = createMap(rs, meta);
                        printSubclassList.add(m);

                        addChild = setParent(printSubclassList, rs.getString("TESTSUBCLASSCD"), printSubclasscd, addChild);

                    } else {

                        final Map m = createMap(rs, meta);
                        printSubclassList.add(m);

                        if ("2".equals(getString(m, "TESTSUBCLASSCD"))) {
                            getMappedList(m, "CHILD_SUBCLASSLIST").add("7");
                            getMappedList(m, "CHILD_SUBCLASSLIST").add("8");
                            m.put("SUBCLASS" + "7" + ".NAME", "No.1");
                            m.put("SUBCLASS" + "8" + ".NAME", "No.2");
                        }
                        if ("5".equals(_testdiv)) {
                            if ("3".equals(getString(m, "TESTSUBCLASSCD"))) {
                                getMappedList(m, "CHILD_SUBCLASSLIST").add("A");
                                getMappedList(m, "CHILD_SUBCLASSLIST").add("B");
                                m.put("SUBCLASS" + "A" + ".NAME", "1");
                                m.put("SUBCLASS" + "B" + ".NAME", "2");
                            } else if ("4".equals(getString(m, "TESTSUBCLASSCD"))) {
                                getMappedList(m, "CHILD_SUBCLASSLIST").add("A");
                                getMappedList(m, "CHILD_SUBCLASSLIST").add("B");
                                m.put("SUBCLASS" + "A" + ".NAME", "3");
                                m.put("SUBCLASS" + "B" + ".NAME", "4");
                            }
                        }
                    }

                }
            } catch (Exception e) {
                log.debug("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return printSubclassList;
        }

        private boolean setParent(final List printSubclassList, final String testsubclasscd, final String printSubclasscd, boolean addChild) throws SQLException {
            for (final Iterator it = printSubclassList.iterator(); it.hasNext();) {
                final Map sm = (Map) it.next();
                if (StringUtils.defaultString(getString(sm, "PRINT_SUBCLASSCD"), "-").equals(printSubclasscd)) {
                    final String childSubclasscd = testsubclasscd;
                    getMappedList(sm, "CHILD_SUBCLASSLIST").add(childSubclasscd);

                    String subclassname = "";
                    if ("A".equals(childSubclasscd)) {
                        subclassname = "1";
                    } else if ("B".equals(childSubclasscd)) {
                        subclassname = "2";
                    } else if ("C".equals(childSubclasscd)) {
                        subclassname = "3";
                    } else if ("D".equals(childSubclasscd)) {
                        subclassname = "4";
                    } else if ("E".equals(childSubclasscd)) {
                        subclassname = "5";
                    } else if ("F".equals(childSubclasscd)) {
                        subclassname = "6";
                    } else if ("7".equals(childSubclasscd)) {
                        subclassname = "No.1";
                    } else if ("8".equals(childSubclasscd)) {
                        subclassname = "No.2";
                    }
                    sm.put("SUBCLASS" + childSubclasscd + ".NAME", subclassname);
                    addChild = true;
                    break;
                }
            }
            return addChild;
        }

        private Map createMap(ResultSet rs, ResultSetMetaData meta) throws SQLException {
            final Map m = new HashMap();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                final String columnName = meta.getColumnLabel(i);
                final String data = rs.getString(columnName);
                m.put(columnName, data);
            }
            return m;
        }

//        private String getTestdiv0Name(final DB2UDB db2) {
//            final String namecd1 = "L034";
//            String testDiv0Name = "";
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testdiv0 + "'");
//                rs = ps.executeQuery();
//                if (rs.next() && null != rs.getString("NAME1")) {
//                  testDiv0Name = rs.getString("NAME1");
//                }
//            } catch (SQLException e) {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//            return testDiv0Name;
//        }

//        private static String currentTime() {
//            final Calendar cal = Calendar.getInstance();
//            final int year = cal.get(Calendar.YEAR);
//            final int month = cal.get(Calendar.MONTH) + 1;
//            final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
//            final String dow = String.valueOf(" 日月火水木金土".charAt(cal.get(Calendar.DAY_OF_WEEK)));
//            final int hour = cal.get(Calendar.HOUR_OF_DAY);
//            final int min = cal.get(Calendar.MINUTE);
//            final DecimalFormat df = new DecimalFormat("00");
//            return KNJ_EditDate.h_format_JP(year + "-" + month + "-" + dayOfMonth) + "(" + dow + ") " + df.format(hour) + ":" + df.format(min);
//        }
    }
}//クラスの括り
