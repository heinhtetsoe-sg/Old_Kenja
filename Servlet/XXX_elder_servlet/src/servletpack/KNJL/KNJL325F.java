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

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL325F {

    private static final Log log = LogFactory.getLog(KNJL325F.class);

    private boolean _hasData;
    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

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

    private void setSvfMain(final DB2UDB db2, final Vrw32alp svf) {
        final List dataListAll = getList(db2);
        final List pageList = getPageList(dataListAll, 50 * 2);
        final String form = "KNJL325F_H.frm";
        
        final String title0 = KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度 " + StringUtils.defaultString(_param._applicantdivname) + "入試 " + StringUtils.defaultString(_param._testdivname);

        svf.VrSetForm(form, 1);
        
        for (int pi = 0; pi < pageList.size(); pi++) {
            svf.VrsOut("TITLE", title0 + " 転科合格者報告書"); // タイトル
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 日付
            svf.VrsOut("PAGE", String.valueOf(pi + 1) + "頁"); // ページ
            
            final List dataList = (List) pageList.get(pi);
            for (int j = 0; j < dataList.size(); j++) {
                final Map m = (Map) dataList.get(j);
                final int line = j - (j >= 50 ? 50 : 0) + 1;
                final String div = j >= 50 ? "2" : "1";
                svf.VrsOutn("NO" + div, line, String.valueOf(pi * 100 + j + 1)); // 番号
                svf.VrsOutn("EXAM_NO" + div, line, getString(m, "EXAMNO")); // 受験番号
                svf.VrsOutn("NAME" + div, line, getString(m, "NAME")); // 氏名
                svf.VrsOutn("EXAM_DIV" + div, line, _param._testdiv0name); // 選考区分
                svf.VrsOutn("OLD_COURSE" + div, line, getString(m, "DET001_EXAMCOURSE_NAME")); // 旧コース
                svf.VrsOutn("NEW_COURSE" + div, line, getString(m, "SUC_EXAMCOURSE_NAME")); // 新コース
            }
            svf.VrEndPage();
            _hasData = true;
        }
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
    
    private List getList(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        ResultSetMetaData meta;
        try {
            final String sql = sql(_param);
            log.debug(" sql = " + sql);
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

    private String sql(final Param param) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     APBASE.NAME ");
        stb.append("     ,APBASE.EXAMNO ");
        stb.append("     ,APBASE.NAME_KANA ");
        stb.append("     ,APBASE.SUC_COURSECD || APBASE.SUC_MAJORCD || APBASE.SUC_COURSECODE AS SUC_EXAMCOURSE ");
        stb.append("     ,APD001.REMARK8 || APD001.REMARK9 || APD001.REMARK10 AS DET001_EXAMCOURSE ");
        stb.append("     ,ECMS.EXAMCOURSE_NAME AS SUC_EXAMCOURSE_NAME ");
        stb.append("     ,ECMD.EXAMCOURSE_NAME AS DET001_EXAMCOURSE_NAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT APBASE ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT APD001 ON APD001.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
        stb.append("         AND APD001.EXAMNO = APBASE.EXAMNO");
        stb.append("         AND APD001.SEQ = '001' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST ECMS ON ECMS.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
        stb.append("         AND ECMS.APPLICANTDIV = APBASE.APPLICANTDIV ");
        stb.append("         AND ECMS.TESTDIV = '1' ");
        stb.append("         AND ECMS.COURSECD = APBASE.SUC_COURSECD ");
        stb.append("         AND ECMS.MAJORCD = APBASE.SUC_MAJORCD ");
        stb.append("         AND ECMS.EXAMCOURSECD = APBASE.SUC_COURSECODE ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST ECMD ON ECMD.ENTEXAMYEAR = APBASE.ENTEXAMYEAR ");
        stb.append("         AND ECMD.APPLICANTDIV = APBASE.APPLICANTDIV ");
        stb.append("         AND ECMD.TESTDIV = '1' ");
        stb.append("         AND ECMD.COURSECD = APD001.REMARK8 ");
        stb.append("         AND ECMD.MAJORCD = APD001.REMARK9 ");
        stb.append("         AND ECMD.EXAMCOURSECD = APD001.REMARK10 ");
        stb.append(" WHERE ");
        stb.append("         APBASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
        stb.append("     AND APBASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
        stb.append("     AND APBASE.TESTDIV = '" + param._testdiv + "' ");
        stb.append("     AND APBASE.TESTDIV0 = '" + param._testdiv0 + "' ");
        stb.append("     AND APBASE.SUC_COURSECD || APBASE.SUC_MAJORCD || APBASE.SUC_COURSECODE <> APD001.REMARK8 || APD001.REMARK9 || APD001.REMARK10 ");
        stb.append(" ORDER BY APBASE.EXAMNO ");
        return stb.toString();
    }

    private static class Param {
        final String _entexamyear;
        final String _loginDate;
        final String _applicantdiv;
        final String _testdiv;
        final String _testdiv0; // 入試回数
        
        final String _z010Name1;
        final String _applicantdivname;
        final String _testdivname;
        final String _testdiv0name;
        
        private boolean _seirekiFlg;
        private String _principalName = "";
        private String _jobName = "";
        private String _schoolName = "";
        private String _schoolNamePath;
        private String _schoolStampPath;
        private String _schoolLogoPath;
        
        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _entexamyear  = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv      = request.getParameter("TESTDIV");
            _testdiv0     = request.getParameter("TESTDIV0");
            _loginDate    = request.getParameter("LOGIN_DATE");
            _z010Name1 = getSchoolName(db2);
            
            setCertifSchoolDat(db2);
            _seirekiFlg = getSeirekiFlg(db2);
            _applicantdivname = getApplicantdivName(db2);
            _testdivname = getTestDivName(db2);
            _testdiv0name = getTestdiv0Name(db2);
        }
        
        private String getApplicantdivName(DB2UDB db2) {
            String schoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantdiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  schoolName = rs.getString("NAME1"); 
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }
        
        private String getTestDivName(DB2UDB db2) {
            String testDivName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String namecd1 = "L004";
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
        
        private String getTestdiv0Name(DB2UDB db2) {
            String schoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L034' AND NAMECD2 = '" + _testdiv0 + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  schoolName = rs.getString("NAME1"); 
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final String certifKindCd;
            if (_applicantdiv.equals("1")) {
                certifKindCd = "105";
            } else {
                certifKindCd = "106";
            }

            final String sql = "SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '" + certifKindCd + "' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _principalName = rs.getString("PRINCIPAL_NAME");
                    _jobName = rs.getString("JOB_NAME");
                    _schoolName = rs.getString("SCHOOL_NAME");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
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
