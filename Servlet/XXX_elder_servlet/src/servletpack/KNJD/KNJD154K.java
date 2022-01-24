// kanji=漢字
/*
 * $Id: ad1c946e1140d2f6ef750a98825feabba3e64f42 $
 *
 * 作成日: 2011/05/02 14:53:38 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: ad1c946e1140d2f6ef750a98825feabba3e64f42 $
 */
public class KNJD154K {

    private static final Log log = LogFactory.getLog("KNJD154K.class");

    Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        try {
            _param = createParam(request);
            KNJD154K_Abstract obj = null;
            final int parseInt = Integer.parseInt(_param._gradeHr[0]);
            if (3 < parseInt) {
                obj = new KNJD154K_HIGHSCHOOL();
            } else {
                obj = new KNJD154K_JUNIOR();
            }
            obj.svf_out(request, response);
            obj.printMain();
        } catch (final Exception e) {
            log.error("Exception:", e);
        }

    }

    /** パラメータ取得処理 */
    private Param createParam(final HttpServletRequest request) throws Exception {
        final Param param = new Param(request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    protected class Param {
        final String _gradeHrClass;
        final String[] _gradeHr;

        Param(final HttpServletRequest request) throws SQLException {
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _gradeHr = StringUtils.split(_gradeHrClass, '-');
        }

    }
}

// eof
