// kanji=漢字
/*
 * $Id$
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJE;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/*
 *  学校教育システム 賢者 [進路情報管理]  調査書表紙
 */

public class KNJE070H {

    private static final Log log = LogFactory.getLog(KNJE070H.class);

    private Param _param;
    private boolean _hasData;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        KNJServletUtils.debugParam(request, log);

        //  print設定
        response.setContentType("application/pdf");

        Vrw32alp svf = new Vrw32alp();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                         //Databaseクラスを継承したクラス

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception e) {
            log.error("[KNJE080]DB2 open error!", e);
        }

        // ＳＶＦ設定
        svf.VrInit(); //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());    //PDFファイル名の設定

        final Param param = new Param(db2, request);

        try {

            final List<Map<String, String>> schregnoMapList = param.getSchregnoMapList(db2);

            for (int i = 0; i < schregnoMapList.size(); i++) {
                final Map<String, String> schregnoMap = schregnoMapList.get(i);

                svf.VrSetForm("KNJE070H.frm", 1);
                svf.VrsOut("HEADER_SCHREGNO", schregnoMap.get("SCHREGNO"));
                svf.VrsOut("HEADER_NAME", schregnoMap.get("NAME"));
                svf.VrsOut("HEADER_DATE", KNJ_EditDate.h_format_SeirekiJP(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
                svf.VrEndPage();
                _hasData = true;
            }
        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            //  終了処理
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
            db2.commit();
            db2.close();                // DBを閉じる
        }
    }

    private static class Param {
        final String _ctrlYear; //年度
        final String _ctrlSemester; //学期
        final String _output;  //出力指定 (1:生徒 2:クラス)
        final String[] _category_selected; // 生徒 or クラス

        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _output = request.getParameter("OUTPUT");
            _category_selected = request.getParameterValues("category_selected");
        }

        /**学籍番号の並べ替え**/
        private List<Map<String, String>> getSchregnoMapList(final DB2UDB db2) {
            final List<Map<String, String>> list = new ArrayList();
            final StringBuilder stb = new StringBuilder();
            stb.append("SELECT ");
            stb.append("    T1.SCHREGNO ");
            stb.append("  , BASE.NAME ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_DAT T1 ");
            stb.append("    INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            stb.append("WHERE ");
            stb.append("   T1.YEAR = '" + _ctrlYear + "' ");
            stb.append("   AND T1.SEMESTER = '" + _ctrlSemester + "' ");
            if ("1".equals(_output)) {
                stb.append("   AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _category_selected));
            } else if ("2".equals(_output)) {
                stb.append("   AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _category_selected));
            }
            stb.append(" ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SCHREGNO");

            list.addAll(KnjDbUtils.query(db2, stb.toString()));

            return list;
        }
    }

}
