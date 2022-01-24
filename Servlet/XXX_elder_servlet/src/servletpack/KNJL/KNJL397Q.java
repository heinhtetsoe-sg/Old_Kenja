package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *
 *    学校教育システム 賢者 [SATシステム] 返却資料
 *
 **/

public class KNJL397Q {

    private static final Log log = LogFactory.getLog(KNJL397Q.class);
    
    private boolean _hasData;
    private Param _param;

    private static final String FROM_TO_MARK = "\uFF5E";

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
            log.fatal("$Revision: 62148 $ $Date: 2018-09-07 10:31:49 +0900 (金, 07 9 2018) $"); // CVSキーワードの取り扱いに注意
            KNJServletUtils.debugParam(request, log);
            _param = new Param(db2, request);

            //SVF出力
            printMain(db2, svf); //帳票出力のメソッド

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

    /**
     * フォームの出力
     * @param db2
     * @param svf
     * @return
     */
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List list = getList(db2, sql(_param));
        for (int i = 0; i < list.size(); i++) {
            final Map data = (Map) list.get(i);
            if (!NumberUtils.isDigits(getString(data, "INOUT_KUBUN"))) {
                continue;
            }
            final int kubun = Integer.parseInt(getString(data, "INOUT_KUBUN"));
            final String form;
            if (kubun == 1) {
                // 県内 = 特待生候補文書 山梨県内
                form = "KNJL397Q_2.frm";
            } else if (kubun == 2) {
                // 県内扱 = 特待生候補文書 長野
                form = "KNJL397Q_2.frm";
            } else if (kubun == 3) {
                // 国内 = 特待生候補文書 県外(山梨、長野以外)
                form = "KNJL397Q_2.frm";
            } else if (kubun == 4) {
                // 海外 = 特待生候補文書 海外
                form = "KNJL397Q_3.frm";
            } else {
                continue;
            }
            svf.VrSetForm(form, 1);
            
            svf.VrsOut("EXAM_NO", getString(data, "SAT_NO")); // 受験番号
            final String kunsan = "1".equals(getString(data, "SEX")) ? "　君" : "2".equals(getString(data, "SEX")) ? "　さん" : "";
            final String name = StringUtils.defaultString(getString(data, "NAME1")) + kunsan;
            svf.VrsOut("NAME", name);
            svf.VrsOut(getMS932Bytecount(name) <= 24 ? "NAME1" : "NAME2", name);
            
            svf.VrEndPage();
            _hasData = true;
        }

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
    
    private static String getString(final Map m, final String field) {
        if (null == m || m.isEmpty()) {
            return null;
        }
        if (!m.containsKey(field)) {
            throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
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
    
    private String sql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        //データ取得
        //ポストしたCHECKのvalueを文字列にしてから
        stb.append(" SELECT ");
        stb.append("     SAT_NO, ");     //受験番号
        stb.append("     NAME1, ");       //氏名
        stb.append("     SEX, ");       //性別
        stb.append("     INOUT_KUBUN ");       //区分
        stb.append(" FROM ");
        stb.append("     SAT_APP_FORM_MST ");
        stb.append(" WHERE ");
        stb.append("     SAT_NO in " + SQLUtils.whereIn(true, _param._CHECK) + " AND ");
        stb.append("     YEAR = '" + _param._CTRL_YEAR + "' ");
        if (!StringUtils.isBlank(param._KUBUN)) {
        	if ("1".equals(param._KUBUN)) {
        		// 県内
        		stb.append("     AND (INOUT_KUBUN = '" + _param._KUBUN + "' ");
        		stb.append("      OR PREFCD = '19' "); // 区分が県内以外でも山梨県は県内に含める
        		stb.append("         ) ");
        	} else {
        		stb.append("     AND (INOUT_KUBUN = '" + _param._KUBUN + "' ");
        		stb.append("      AND PREFCD <> '19' ");
        		stb.append("         ) ");
        	}
        }
        stb.append(" ORDER BY ");
        stb.append("     SAT_NO ");
        return stb.toString();
    }

    private static class Param {
        final String[] _CHECK;
        final String _CHECK_CNT;
        final String _CTRL_DATE;
        final String _CTRL_SEMESTER;
        final String _CTRL_YEAR;
        final String _KUBUN;
        final String _PRGID;
        final String _cmd;
        private Param(final DB2UDB db2, final HttpServletRequest request) {
            _CHECK = request.getParameterValues("CHECK[]");
            _CHECK_CNT = request.getParameter("CHECK_CNT");
            _CTRL_DATE = request.getParameter("CTRL_DATE");
            _CTRL_SEMESTER = request.getParameter("CTRL_SEMESTER");
            _CTRL_YEAR = request.getParameter("CTRL_YEAR");
            _KUBUN = request.getParameter("KUBUN");
            _PRGID = request.getParameter("PRGID");
            _cmd = request.getParameter("cmd");
        }
    }
}//クラスの括り
