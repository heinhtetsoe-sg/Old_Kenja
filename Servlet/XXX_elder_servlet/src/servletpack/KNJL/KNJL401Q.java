package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *    学校教育システム 賢者 [SATシステム] 受験票
 *
 **/

public class KNJL401Q {

    private static final Log log = LogFactory.getLog(KNJL401Q.class);

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
            log.fatal("$Revision: 69974 $ $Date: 2019-10-02 01:18:04 +0900 (水, 02 10 2019) $"); // CVSキーワードの取り扱いに注意
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
    	final String sql = sql(_param);
    	//log.info(" sql = " + sql);

    	for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
    		
    		//log.info(" row = " + row);
			
        	svf.VrSetForm("KNJL401Q.frm", 1);

            svf.VrsOut("EXAMNO", KnjDbUtils.getString(row, "SAT_NO")); // 受験番号

            final String placename = KnjDbUtils.getString(row, "PLACENAME");
            final int ketaPlacename = KNJ_EditEdit.getMS932ByteLength(placename);
            svf.VrsOut("HALL_NAME" + (ketaPlacename <= 20 ? "1" : ketaPlacename <= 30 ? "2" : "3"), placename); // 会場名

            final String name1 = KnjDbUtils.getString(row, "NAME1");
            final int ketaName1 = KNJ_EditEdit.getMS932ByteLength(name1);
            svf.VrsOut("NAME" + (ketaName1 <= 20 ? "1" : ketaName1 <= 30 ? "2" : "3"), name1); // 氏名

            String comment = "";
            if ("1".equals(_param._FEE)) {
            	comment = "受験料未納（当日会場内で徴収します）";
            } else if ("2".equals(_param._FEE)) {
            	comment = "領収済";
            }
            svf.VrsOut("COMMENT1", comment); // コメント

        	svf.VrEndPage();
        	_hasData = true;
		}
    }

    private String sql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  ");
        stb.append("     T1.SAT_NO ");
        stb.append("   , T1.NAME1 ");
        stb.append("   , L1.PLACENAME ");
        stb.append("   , L2.SEND_KUBUN ");
        stb.append("   , L3.NAME1 AS SEND_KUBUN_NAME ");
        stb.append(" FROM SAT_APP_FORM_MST T1 ");
        stb.append(" LEFT JOIN SAT_EXAM_PLACE_DAT L1 ON ");
        stb.append("      L1.YEAR = T1.YEAR ");
        stb.append("    AND L1.PLACECD = T1.PLACECD ");
        stb.append(" LEFT JOIN SAT_EXAM_NO_DAT L2 ON ");
        stb.append("      L2.YEAR = T1.YEAR ");
        stb.append("    AND T1.SAT_NO BETWEEN L2.JUKEN_NO_FROM AND L2.JUKEN_NO_TO ");
        stb.append(" LEFT JOIN NAME_MST L3 ON ");
        stb.append("      L3.NAMECD1 = 'L203' ");
        stb.append("    AND L3.NAMECD2 = L2.SEND_KUBUN ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._CTRL_YEAR + "' ");
        if (!StringUtils.isBlank(param._BAN_EXAMNO) && !StringUtils.isBlank(param._EXAMNO)) {
        	stb.append("  AND T1.SAT_NO IN ('" + param._BAN_EXAMNO + "', '" + param._EXAMNO + "') ");
        	stb.append("  ORDER BY CASE WHEN T1.SAT_NO = '" + param._BAN_EXAMNO + "' THEN 0 ELSE 1 END, T1.SAT_NO ");
        } else if (!StringUtils.isBlank(param._BAN_EXAMNO)) {
        	stb.append("  AND T1.SAT_NO = '" + param._BAN_EXAMNO + "' ");
        } else {
        	stb.append("  AND T1.SAT_NO = '" + param._EXAMNO + "' ");
        }
        return stb.toString();
    }

    private static class Param {
    	final String _CTRL_YEAR;
    	final String _CTRL_SEMESTER;
        final String _CTRL_DATE;
        final String _EXAMNO;
        final String _BAN_EXAMNO;
        final String _FEE;
        final String _PRGID;
        final String _cmd;
        private Param(final DB2UDB db2, final HttpServletRequest request) {
            _CTRL_YEAR = request.getParameter("CTRL_YEAR");
            _CTRL_SEMESTER = request.getParameter("CTRL_SEMESTER");
            _CTRL_DATE = request.getParameter("CTRL_DATE");
            _EXAMNO = request.getParameter("EXAMNO");
            _BAN_EXAMNO = request.getParameter("BAN_EXAMNO");
            _FEE = request.getParameter("FEE");
            _PRGID = request.getParameter("PRGID");
            _cmd = request.getParameter("cmd");
        }
    }
}//クラスの括り
