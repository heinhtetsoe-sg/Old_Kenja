// kanji=漢字
/*
 * $Id: 9ae765a1467d3387a3471e90d1047e2c8713e30a $
 *
 * 作成日: 2008/01/16 20:09:29 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWP;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * KNJWP300のパラメータ。
 * @author takaesu
 * @version $Id: 9ae765a1467d3387a3471e90d1047e2c8713e30a $
 */
public class KNJWP300Param {
    /*pkg*/static final Log log = LogFactory.getLog(KNJWP300Param.class);

    /** DBホスト名 */
    public static final String PARAM_DBHOST = "DBHOST";

    /** データベース名 */
    public static final String PARAM_DBNAME = "DBNAME";

    /** 職員コード */
    public static final String PARAM_STAFFCD = "STAFFCD";

    public static final String PARAM_YEAR = "YEAR";
    public static final String PARAM_LOGIN_DATE = "LOGIN_DATE";
    public static final String PARAM_YEAR_MONTH = "YEAR_MONTH";
    public static final String PARAM_LAST_YEAR_MONTH = "LAST_YEAR_MONTH";
    public static final String PARAM_FROM_DATE = "FROM_DATE";
    public static final String PARAM_TO_DATE = "TO_DATE";
    public static final String PARAM_TIGHTENS = "TIGHTENS";
    /** 業務ロジックを実行しないか? true なら実行しない */
    public static final String DISABLE_LOGIC = "DISABLE_LOGIC";

    final HttpServletResponse _response;
    protected String _dbhost;
    protected String _dbname;
    protected String _staffcd;
    
    protected final String _year;
    protected final String _loginDate;
    protected final String _yearMonth;
    protected final String _lastYearMonth;
    protected final String _fromData;
    protected final String _toDate;
    protected final String _tightens;

    protected final boolean _disableLogic;

    private final Map _map = new LinkedMap();

    public KNJWP300Param(final String[] args) {
        _response = null;

        int pos;
        String k, v;
        for (int i = 0; i < args.length; i++) {
            pos = args[i].indexOf('=');
            if (0 <= pos) {
                k = StringUtils.trimToEmpty(args[i].substring(0, pos));
                v = StringUtils.trimToEmpty(args[i].substring(pos + 1));
                _map.put(k.toUpperCase(), v);   // キーを大文字で
            }
        }
        _dbhost = (String) _map.get(PARAM_DBHOST);
        _dbname = (String) _map.get(PARAM_DBNAME);
        _staffcd = (String) _map.get(PARAM_STAFFCD);

        _year = (String) _map.get(PARAM_YEAR);
        _loginDate = ((String) _map.get(PARAM_LOGIN_DATE)).replace('/', '-');
        _yearMonth = (String) _map.get(PARAM_YEAR_MONTH);
        _lastYearMonth = (String) _map.get(PARAM_LAST_YEAR_MONTH);
        _fromData = ((String) _map.get(PARAM_FROM_DATE)).replace('/', '-');
        _toDate = ((String) _map.get(PARAM_TO_DATE)).replace('/', '-');
        _tightens = (String) _map.get(PARAM_TIGHTENS);

        _disableLogic = _map.containsKey(DISABLE_LOGIC);
    }

    public KNJWP300Param(
            final HttpServlet servlet,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        KNJServletUtils.debugParam(request, log);
        _response = response;

        _dbhost = request.getParameter(PARAM_DBHOST);
        _dbname = request.getParameter(PARAM_DBNAME);
        _staffcd = request.getParameter(PARAM_STAFFCD);
        _loginDate = request.getParameter(PARAM_LOGIN_DATE).replace('/', '-');
//        _year = "2006";
//        _yearMonth = "200610";
//        _lastYearMonth = "200609";
//        _fromData = "2006-10-01";
//        _toDate = "2006-10-31";
        _year = request.getParameter(PARAM_YEAR);
        _yearMonth = request.getParameter(PARAM_YEAR_MONTH);
        _lastYearMonth = request.getParameter(PARAM_LAST_YEAR_MONTH);
        _fromData = request.getParameter(PARAM_FROM_DATE).replace('/', '-');
        _toDate = request.getParameter(PARAM_TO_DATE).replace('/', '-');
        _tightens = request.getParameter(PARAM_TIGHTENS);

        _disableLogic = (null != request.getParameter(DISABLE_LOGIC));
    }

    public DB2UDB createDb() {
        return new DB2UDB(_dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
    }

    public PrintWriter getPrintWriter() {
        if (null != _response) {
            try {
                final ServletOutputStream outputStream = _response.getOutputStream();
                return new PrintWriter(new OutputStreamWriter(outputStream, "SJIS"));
            } catch (final IOException e) {
                log.error("サーブレットの出力先の取得失敗", e);
            }
        }

        return new PrintWriter(System.out);
    }

    public String toString() {
        return "response=" + _response;
    }

} // KNJWP300Param

// eof
