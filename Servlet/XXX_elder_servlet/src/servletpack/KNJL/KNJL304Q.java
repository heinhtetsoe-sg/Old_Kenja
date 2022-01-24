package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL304Q {

    private static final Log log = LogFactory.getLog(KNJL304Q.class);

    private boolean _hasData;
    private final String SCHOOL_KIND_J = "J";
    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

        log.fatal("$Revision: 57061 $ $Date: 2017-11-13 13:40:05 +0900 (月, 13 11 2017) $"); // CVSキーワードの取り扱いに注意

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

            printMain(db2, svf);

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

    private static String getString(final Map m, final String field) {
        if (null == m) {
            return null;
        }
        try {
            if (!m.containsKey(field)) {
                throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
            }
        } catch (Exception e) {
            log.fatal("exception!", e);
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

    private static List getList(final DB2UDB db2, final String sql) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List list = new ArrayList();
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                list.add(m);
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    m.put(meta.getColumnName(i), rs.getString(i));
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final String sql = sql(_param);
        final int maxLine = 34;
        final List pageList = getPageList(getList(db2, sql), maxLine);

        for (int pi = 0; pi < pageList.size(); pi++) {

            final String form = "KNJL304Q.frm";
            svf.VrSetForm(form, 4);

            svf.VrsOut("PAGE1", String.valueOf(pi + 1)); // ページ
            svf.VrsOut("PAGE2", String.valueOf(pageList.size())); // ページ
            svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　スカラー志願者"); // タイトル
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 日付

            final List dataList = (List) pageList.get(pi);
            for (int i = 0; i < dataList.size(); i++) {
                final Map m = (Map) dataList.get(i);
                svf.VrsOut("EXAM_NO", getString(m, "EXAMNO")); // 受験番号
                svf.VrsOut("NAME", getString(m, "NAME")); // 氏名
                svf.VrsOut("EXAM_DIV", getString(m, "TESTDIV0_ABBV1")); // 入試区分
                final String setExamCourseName = KNJ_EditEdit.getMS932ByteLength(getString(m, "EXAMCOURSE_NAME")) > 16 ? "3" : KNJ_EditEdit.getMS932ByteLength(getString(m, "EXAMCOURSE_NAME")) > 14 ? "2" : "";
                svf.VrsOut("COURSE_NAME" + setExamCourseName, getString(m, "EXAMCOURSE_NAME")); // コース名
                svf.VrsOut("JHSCHOOL_NAME", getString(m, "FINSCHOOL_NAME")); // 中学校名称
                svf.VrsOut("SCHOLAR", getString(m, "SCHOLAR_KIBOU_NAME")); // スカラ
                svf.VrEndRecord();
                _hasData = true;
            }
        }
    }

    private String sql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  ");
        if (SCHOOL_KIND_J.equals(_param._schoolKind)) {
            stb.append("     B1.TESTDIV AS TESTDIV0, ");
        } else {
            stb.append("     B1.TESTDIV0, ");
        }
        stb.append("      N1.ABBV1 AS TESTDIV0_ABBV1,  ");
        stb.append("      B1.DAI1_COURSECD || B1.DAI1_MAJORCD || B1.DAI1_COURSECODE AS DAI1_COURSE,  ");
        stb.append("      C1.EXAMCOURSE_NAME, ");
        stb.append("      B1.EXAMNO, ");
        stb.append("      B1.NAME, ");
        stb.append("      TFIN.FINSCHOOL_NAME, ");
        stb.append("      B1.SCHOLAR_KIBOU, ");
        stb.append("      CASE WHEN B1.SCHOLAR_KIBOU = '1' THEN '特別'  ");
        stb.append("           WHEN B1.SCHOLAR_KIBOU = '2' THEN '一般' ");
        stb.append("      END AS SCHOLAR_KIBOU_NAME ");
        stb.append("  FROM  ");
        stb.append("      V_ENTEXAM_APPLICANTBASE_DAT B1  ");
        stb.append("      LEFT JOIN ENTEXAM_APPLICANTADDR_DAT A1 ON A1.ENTEXAMYEAR = B1.ENTEXAMYEAR  ");
        stb.append("              AND A1.APPLICANTDIV = B1.APPLICANTDIV  ");
        stb.append("              AND A1.EXAMNO = B1.EXAMNO  ");
        if (SCHOOL_KIND_J.equals(param._schoolKind)) {
            stb.append("      LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = '" + param._nameMstTestDiv + "' AND N1.NAMECD2 = B1.TESTDIV  ");
        } else {
            stb.append("      LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = '" + param._nameMstTestDiv + "' AND N1.NAMECD2 = B1.TESTDIV0  ");
        }
        stb.append("      LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = B1.ENTEXAMYEAR  ");
        stb.append("              AND C1.APPLICANTDIV = B1.APPLICANTDIV  ");
        stb.append("              AND C1.TESTDIV = B1.TESTDIV  ");
        stb.append("              AND C1.COURSECD = B1.DAI1_COURSECD  ");
        stb.append("              AND C1.MAJORCD = B1.DAI1_MAJORCD  ");
        stb.append("              AND C1.EXAMCOURSECD = B1.DAI1_COURSECODE  ");
        stb.append("      LEFT JOIN FINSCHOOL_MST TFIN ON TFIN.FINSCHOOLCD = B1.FS_CD ");
        stb.append("  WHERE  ");
        stb.append("      B1.ENTEXAMYEAR = '" + param._entexamyear + "'  ");
        stb.append("      AND B1.APPLICANTDIV = '" + param._applicantdiv + "'  ");
        if (SCHOOL_KIND_J.equals(_param._schoolKind)) {
            stb.append("      AND B1.TESTDIV = '" + param._testdiv + "'  ");
        } else {
            stb.append("      AND B1.TESTDIV0 = '" + param._testdiv + "'  ");
        }
        stb.append("      AND B1.SCHOLAR_KIBOU IN ('1', '2') ");
        stb.append("  ORDER BY  ");
        stb.append("      B1.EXAMNO  ");
        return stb.toString();
    }

    private class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _loginDate;
        final boolean _seirekiFlg;
        final String _schoolKind;
        final String _nameMstTestDiv;

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _schoolKind = request.getParameter("SCHOOLKIND");
            _nameMstTestDiv = SCHOOL_KIND_J.equals(_schoolKind) ? "L024" : "L045";
            _entexamyear  = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _loginDate    = request.getParameter("LOGIN_DATE");
            _seirekiFlg = getSeirekiFlg(db2);
        }

        /* 西暦表示にするのかのフラグ  */
        private boolean getSeirekiFlg(final DB2UDB db2) {
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' AND NAME1 IS NOT NULL ";
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

    }
}//クラスの括り
