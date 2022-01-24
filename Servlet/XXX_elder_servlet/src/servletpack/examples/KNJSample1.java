// kanji=漢字
/*
 * $Id: 250449447c56e85f8e16ad8b07c1fb65a2aa0c97 $
 *
 * 作成日: 2007/02/15 16:03:11 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.examples;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 賢者の帳票アプリのサンプル１。
 * @author takaesu
 * @version $Id: 250449447c56e85f8e16ad8b07c1fb65a2aa0c97 $
 */
public class KNJSample1 {
    /*pkg*/static final Log log = LogFactory.getLog(KNJSample1.class);

    private Vrw32alp _svf;

    /**
     * <code>KNJServlet</code>のサブクラス(KNJAなど)から、リフレクションで呼ばれるメソッド。
     * @param req リクエスト
     * @param resp レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        response.setContentType("application/pdf");

        _svf = new Vrw32alp();
        _svf.VrInit();
        _svf.VrSetSpoolFileStream(response.getOutputStream());
        if (false) {
            _svf.VrSetForm("hachusho.frm", 1);
            _svf.VrEndPage();
        } else {
            _svf.VrSetForm("hachusho.frm", 4);
            _svf.VrEndRecord();
            _svf.VrPrint();
        }
        _svf.VrQuit();
    }
} // KNJSample1

// eof
