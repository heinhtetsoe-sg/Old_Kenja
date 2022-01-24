// kanji=漢字
/*
 * $Id: e856125662abd7d86c340c21273e79fcf5b27ad0 $
 *
 * 作成日: 2011/09/30 15:12:18 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJS;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: e856125662abd7d86c340c21273e79fcf5b27ad0 $
 */
public class KNJS310 {

    private static final Log log = LogFactory.getLog("KNJS310.class");

    private boolean _hasData;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        final Vrw32alp svf = new Vrw32alp();
        init(response, svf);

        KNJS310NenkanKoma koma = new KNJS310NenkanKoma();
        final boolean hasDataKoma = koma.svf_out(request, response, svf);

        KNJS310Nenkan1 nenkan1 = new KNJS310Nenkan1();
        final boolean hasDataNenkan1 = nenkan1.svf_out(request, response, svf);

        if (!hasDataKoma && !hasDataNenkan1) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note", "note");
            svf.VrEndPage();
        }

        if (null != svf) {
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }
}

// eof
