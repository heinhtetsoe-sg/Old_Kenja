package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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

public class KNJL341Q {

    private static final Log log = LogFactory.getLog(KNJL341Q.class);

    private final SimpleDateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final DecimalFormat _df02 = new DecimalFormat("00");

    private boolean _hasData;
    private final static String SCHOOL_KIND_J = "J";
    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

        log.fatal("$Revision: 56657 $ $Date: 2017-10-23 21:03:29 +0900 (月, 23 10 2017) $"); // CVSキーワードの取り扱いに注意

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final String form = "KNJL341Q.frm";
        final int maxLine = 40;

        final List prischoolList = Prischool.getPrischoolList(db2, _param);

        for (int prii = 0; prii < prischoolList.size(); prii++) {
            final Prischool pri = (Prischool) prischoolList.get(prii);

            final List pageList = getPageList(pri._applicantList, maxLine);

            for (int pi = 0; pi < pageList.size(); pi++) {

                svf.VrSetForm(form, 4);

                svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　入学試験結果報告"); // タイトル
                svf.VrsOut("PAGE1", String.valueOf(pi + 1)); // ページ
                svf.VrsOut("PAGE2", String.valueOf(pageList.size())); // ページ
                svf.VrsOut("JUKU_NAME", "塾名：" + StringUtils.defaultString(pri._prischoolName)); // 塾名
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 日付

                final List applicantList = (List) pageList.get(pi);
                for (int i = 0; i < applicantList.size(); i++) {
                    final Applicant appl = (Applicant) applicantList.get(i);

                    svf.VrsOut("NO", String.valueOf(pi * maxLine + i + 1)); // 連番
                    svf.VrsOut("EXAM_NO", appl._examno); // 受験番号
                    svf.VrsOut("DIV", appl._testdiv0Name); // 区分
                    svf.VrsOut("NAME", appl._name); // 氏名
                    svf.VrsOut("SEX", appl._sexName); // 性別
                    svf.VrsOut("FINSCHOOL_NAME", appl._finschoolNameAbbv); // 出身中学名
                    svf.VrsOut("JUDGE", appl._judgementName); // 判定
//                    svf.VrsOut("REMARK", null); // 備考
                    svf.VrEndRecord();

                }
                _hasData = true;
            }
        }
    }

    private static class Prischool {
        String _jukucd;
        String _prischoolName;
        final List _applicantList = new ArrayList();

        private static List getPrischoolList(final DB2UDB db2, final Param param) {
            final Map prischoolMap = new HashMap();
            final List prischoolList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String jukucd = rs.getString("JUKUCD");
                    if (null == prischoolMap.get(jukucd)) {
                        final Prischool pri = new Prischool();
                        pri._jukucd = jukucd;
                        pri._prischoolName = rs.getString("PRISCHOOL_NAME");
                        prischoolList.add(pri);
                        prischoolMap.put(jukucd, pri);
                    }

                    final Prischool pri = (Prischool) prischoolMap.get(jukucd);

                    final Applicant appl = new Applicant();
                    appl._examno = rs.getString("EXAMNO");
                    appl._testdiv0 = rs.getString("TESTDIV0");
                    appl._testdiv0Name = rs.getString("TESTDIV0_NAME");
                    appl._name = rs.getString("NAME");
                    appl._sex = rs.getString("SEX");
                    appl._sexName = rs.getString("SEX_NAME");
                    appl._fsCd = rs.getString("FS_CD");
                    appl._finschoolNameAbbv = rs.getString("FINSCHOOL_NAME_ABBV");
                    appl._judgement = rs.getString("JUDGEMENT");
                    appl._judgementName = rs.getString("JUDGEMENT_NAME");
                    pri._applicantList.add(appl);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return prischoolList;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     BASE.JUKUCD ");
            stb.append("   , PRI.PRISCHOOL_NAME ");
            stb.append("   , BASE.EXAMNO ");
            if (SCHOOL_KIND_J.equals(param._schoolKind)) {
                stb.append("   , BASE.TESTDIV AS TESTDIV0 ");
            } else {
                stb.append("   , BASE.TESTDIV0 ");
            }
            stb.append("   , L045.ABBV1 AS TESTDIV0_NAME ");
            stb.append("   , BASE.NAME ");
            stb.append("   , BASE.SEX ");
            stb.append("   , Z002.ABBV1  AS SEX_NAME ");
            stb.append("   , BASE.FS_CD ");
            stb.append("   , FINSCH.FINSCHOOL_NAME_ABBV ");
            stb.append("   , BASE.JUDGEMENT ");
            stb.append("   , L013.NAME1 AS JUDGEMENT_NAME ");
            stb.append(" FROM V_ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("   LEFT JOIN PRISCHOOL_MST PRI ON PRI.PRISCHOOLCD = BASE.JUKUCD ");
            stb.append("   LEFT JOIN FINSCHOOL_MST FINSCH ON FINSCH.FINSCHOOLCD = BASE.FS_CD ");
            stb.append("   LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND Z002.NAMECD2 = BASE.SEX ");
            if (SCHOOL_KIND_J.equals(param._schoolKind)) {
                stb.append("   LEFT JOIN NAME_MST L045 ON L045.NAMECD1 = '" + param._nameMstTestDiv + "' AND L045.NAMECD2 = BASE.TESTDIV ");
            } else {
                stb.append("   LEFT JOIN NAME_MST L045 ON L045.NAMECD1 = '" + param._nameMstTestDiv + "' AND L045.NAMECD2 = BASE.TESTDIV0 ");
            }
            stb.append("   LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' AND L013.NAMECD2 = BASE.JUDGEMENT ");
            stb.append(" WHERE ");
            stb.append("   BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("   AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("   AND BASE.JUKUCD IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("     BASE.JUKUCD ");
            stb.append("   , BASE.EXAMNO ");
            return stb.toString();
        }
    }

    private static class Applicant {
        String _examno;
        String _testdiv0;
        String _testdiv0Name;
        String _name;
        String _sex;
        String _sexName;
        String _fsCd;
        String _finschoolNameAbbv;
        String _judgement;
        String _judgementName;
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

    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv0;
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
            _testdiv0 = request.getParameter("TESTDIV");
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
                    if ("2".equals(rs.getString("NAME1"))) seirekiFlg = true; //西暦
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return seirekiFlg;
        }

    }
}//クラスの括り
