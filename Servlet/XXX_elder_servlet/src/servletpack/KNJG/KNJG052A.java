// kanji=漢字
/*
 * $Id: d26b315455ef33b33e2d773cbb57dc811b9b0025 $
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJG;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;

/**
 *  学校教育システム 賢者 [KNJG管理] 修了証明書
 */

public class KNJG052A {

    private static final Log log = LogFactory.getLog(KNJG052A.class);

    private boolean _hasData;

    public void svf_out(
            HttpServletRequest request,
            HttpServletResponse response)
    throws ServletException, IOException {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定

        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        
        // パラメータの取得
        final Param param = createParam(request);
        // print svf設定
        sd.setSvfInit(request, response, svf);
        // ＤＢ接続
        DB2UDB db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error");
            return;
        }
        // 印刷処理
        printSvfMain(db2, svf, param);        //SVF-FORM出力処理
        // 終了処理
        sd.closeSvf(svf, _hasData);
        sd.closeDb(db2);
    }

    /** 
     *  SVF-FORM メイン出力処理 
     */
    private void printSvfMain(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Param param
    ) {
        final KNJDefineSchool definecode = new KNJDefineSchool();
        definecode.defineCode(db2, param._ctrlYear);
        
        final KNJG010_1T pobj = (KNJG010_1T) new KNJG010_1T(db2, svf, definecode);
        final Map preStatMap = new HashMap();
        pobj.pre_stat("", preStatMap);
        
        final String[] paramArray = new String[20]; // KNJG010_1Tのパラメータ
        paramArray[1] = "015"; // 修了証明書
        paramArray[2] = param._ctrlYear;
        paramArray[3] = param._semester;
        paramArray[8] = param._noticeday;
        paramArray[9] = null; // 発行番号
        paramArray[11] = param._ctrlYear;
        paramArray[12] = param._documentroot;
        paramArray[19] = param._certifPrintRealName;

        for (int i = 0; i < param._classSelected.length; i++) {
            final String[] split = StringUtils.split(param._classSelected[i], "-");
            if (null == split || split.length <= 1) {
                continue;
            }
            paramArray[0] = split[1]; // 学籍番号
            if (pobj.printSvfMain(paramArray, param._ctrlYear)) {
                _hasData = true;
            }
        }
        pobj.pre_stat_f();
    }
    
    private Param createParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56821 $ $Date: 2017-10-27 19:54:53 +0900 (金, 27 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request);
        return param;
    }

    private static class Param {
        final String _ctrlYear;  //年度
        final String _semester;  //学期
        final String _cmbClass;  //学年or学年・組
        final String _noticeday;  //記載日付
        final String[] _classSelected;
        final String _certifNoSyudou; //証明書発行番号は手入力の値を表示するか
        final String _documentroot;
        final String _certifPrintRealName; // 証明書は戸籍名を出力する
        
        Param(final HttpServletRequest request) {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _cmbClass = request.getParameter("CMBCLASS");
            _noticeday = request.getParameter("NOTICEDAY");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _certifNoSyudou = request.getParameter("certifNoSyudou");
            _documentroot = request.getParameter("DOCUMENTROOT");
            _certifPrintRealName = request.getParameter("certifPrintRealName");
        }
    }
}
