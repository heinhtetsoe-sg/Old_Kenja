// kanji=漢字
/*
 * $Id: KNJServlet.java 56925 2017-11-03 16:49:54Z maeshiro $
 *
 * 作成日: 2004/10/09 17:20:03 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.NDC;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * 賢者サーブレット。
 * @author tamura
 * @version $Id: KNJServlet.java 56925 2017-11-03 16:49:54Z maeshiro $
 */
public abstract class KNJServlet extends HttpServlet {
    private static final String DBNAME = "DBNAME";
    private static final String PRGID = "PRGID";
    private static final String PROPERTIES = "KNJServlet.properties";
    private static final String SVFOUT = "svf_out";
    private static final Class[] SVFOUT_PARAM_TYPES = new Class[] {HttpServletRequest.class, HttpServletResponse.class};
    private static final String ALP_PDF_PRINT = "print";
    private static final Class[] ALP_PDF_PRINT_PARAM_TYPES = new Class[] {String.class, HttpServletRequest.class, OutputStream.class};
    private static final Log DEFAULT_LOG = LogFactory.getLog(KNJServlet.class);

    private final boolean _usingProperties;
    private final String _defaultPRGID;
    private final Log _log;

    private KNJServlet(
            final boolean usingProperties,
            final String defaultPRGID,
            final Log log
    ) {
        super();
        _usingProperties = usingProperties;
        _defaultPRGID = defaultPRGID.trim();
        _log = log;
    }

    /**
     * コンストラクタ。
     */
    protected KNJServlet() {
        this(false, "", DEFAULT_LOG);
    }

    /**
     * コンストラクタ。
     * @param defaultPRGID デフォルトの「プログラムID」
     * @param log log
     */
    protected KNJServlet(
            final String defaultPRGID,
            final Log log
    ) {
        this(true, defaultPRGID, log);
    }

    /**
     * {@inheritDoc}
     */
    protected final void service(
            final HttpServletRequest req,
            final HttpServletResponse resp
    ) throws ServletException, IOException {
        try {
            if (null == req) {
                NDC.push("request is null");
            } else {
                final StringBuffer sb = new StringBuffer(80);
                sb.append(req.getRemoteAddr());
                sb.append(',').append(req.getParameter(DBNAME));

                // セッションがあれば取得する。なければ作成させずにnullを貰う
                final HttpSession session = req.getSession(false);
                if (null != session) {
                    // セッションがあればセッションIDを得る
                    sb.append(',').append(session.getId());
                }
                NDC.push(sb.toString());
            }

            // service
            super.service(req, resp);
        } finally {
            NDC.remove();
        }
    }

    /*
     * プロパティファイルを読み込む。
     */
    private Map load(final String filename) {
        InputStream is = null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream(filename);
            if (null != is) {
                final Properties prop = new Properties();
                try {
                    prop.load(is);
                    _log.debug("prop.size()=" + prop.size());
                    return prop;
                } catch (final IOException e) {
                    _log.error("load", e);
                }
            }
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (final IOException e) {
                    _log.error("close", e);
                }
            }
        }
        return null;
    }
    
    private String makeDefaultFqcn(final String prgid) {
        String fqcn = null;
        try {
            if (null != prgid) {
                if (prgid.startsWith("KNJ")) {
                    int lastHeadAlphabet = 0;
                    for (int i = 0; i < prgid.length(); i++) {
                        final char ch = prgid.charAt(i);
                        if (!('A' <= ch && ch <= 'Z')) {
                            break;
                        }
                        lastHeadAlphabet = i;
                    }
                    fqcn = "servletpack." + prgid.substring(0, lastHeadAlphabet + 1) + "." + prgid;
                }
            }
        } catch (Exception e) {
            _log.error("exception! prgid = " + prgid, e);
        }
        return fqcn;
    }

    /**
     * {@inheritDoc}
     */
    public void doPost(
            final HttpServletRequest req,
            final HttpServletResponse resp
    ) throws ServletException, IOException {
        doGet(req, resp);
    }

    /**
     * {@inheritDoc}
     */
    public void doGet(
            final HttpServletRequest req,
            final HttpServletResponse resp
    ) throws ServletException, IOException {
        String prgid = null;
        
        insertAccessLogDetail(req);
        
        if (null != req.getParameter("CALL_DEST_JAVA_PRGID")) {
            final String classname = req.getParameter("CALL_DEST_JAVA_PRGID");
            final String methodname = req.getParameter("METHOD_NAME");
            final Object[] methodInfo = { methodname, SVFOUT_PARAM_TYPES, new Object[] {req, resp}};
            boolean hasException = invoke1(classname, req, resp, methodInfo, true);
            if (hasException) {
                _log.info("hasException1 = " + hasException);
            }
            return;
        }
        
    checkPRGID:
        {
            try {
                final String tmpid = req.getParameter(PRGID).trim();
                if (null == tmpid || 0 == tmpid.length()) {
                    prgid = _defaultPRGID;
                    _log.warn("PRGID is null or empty. use defaultPRGID");
                } else {
                    prgid = tmpid;
                }
            } catch (Exception e) {
                _log.error("exception!", e);
                KNJServletUtils.debugParam(req, _log);
            }
        }
        _log.info("PRGID=" + prgid);

        final Map map;
        if (_usingProperties) {
            map = load(PROPERTIES);
        } else {
            map = null;
        }
        
        final java.util.Collection prgIdException = Arrays.asList(new String[] {"KNJB0045B"});

        if (null != prgid && !prgIdException.contains(prgid)) {
            boolean propertiesFound = false;
            String fqcn = null;
            if (null != map) {
                fqcn = (String) map.get(prgid);
            }
            if (null == fqcn) {
                fqcn = makeDefaultFqcn(prgid);
                _log.warn("no property. makeDefaultFqcn=[" + fqcn + "]");
            } else {
                propertiesFound = true;
            }
            if (null != fqcn) {
                fqcn = fqcn.trim();
                String basePathHome = "";
                try {
                    basePathHome = getBasePathHome(req);
                } catch (Exception e) {
                    _log.info("getBasePathHome:" + e.getClass().getName());
                    basePathHome = "/usr/local/development/src/";
                }
                if (StringUtils.isBlank(basePathHome)) {
                    basePathHome = StringUtils.defaultString(req.getParameter("BASEPATH_HOME"));
                }
                _log.info("found id=[" + prgid + "], fqcn=[" + fqcn + "]");
                boolean hasException = false;
                boolean invokeAnother = true;
                final String basePath = basePathHome + "/../pdf_design/";
                if (invokeAnother && !propertiesFound) {
                    final Object[] methodInfo = { ALP_PDF_PRINT, ALP_PDF_PRINT_PARAM_TYPES, new Object[] {basePath, req, resp.getOutputStream()}};
                    hasException = invoke1(fqcn + "_AlpPdf", req, resp, methodInfo, true);
                    if (hasException) {
                        _log.info("hasException1 = " + hasException);
                    }
                    if (hasException == false) {
                        invokeAnother = false;
                    }
                }
                if (invokeAnother) {
                    final Object[] methodInfo = { ALP_PDF_PRINT, ALP_PDF_PRINT_PARAM_TYPES, new Object[] {basePath, req, resp.getOutputStream()}};
                    hasException = invoke1(fqcn, req, resp, methodInfo, true);
                    if (hasException) {
                        _log.info("hasException2 = " + hasException);
                    }
                    if (hasException == false) {
                        invokeAnother = false;
                    }
                }
                if (invokeAnother) {
                    _log.info("invokeAnother3");
                    final Object[] methodInfo = {SVFOUT, SVFOUT_PARAM_TYPES, new Object[] {req, resp, }};
                    invoke(fqcn, req, resp, methodInfo, true);
                }
                return;
            }
        }

        doGet(prgid, req, resp);
    }

    private void insertAccessLogDetail(final HttpServletRequest request) {
        if (null == request.getParameter("DBNAME")) {
            return;
        }
        PreparedStatement ps = null;
        DB2UDB db2 = null;
        StringBuffer sql = null;
        try {
            sql = new StringBuffer();
            sql.append(" INSERT INTO ACCESS_LOG_DETAIL (UPDATED, USERID, STAFFCD, PROGRAMID, PCNAME, IPADDRESS, MACADDRESS, ACCESS_CD, SUCCESS_CD, POST_DATA) ");
            sql.append(" VALUES( ");
            sql.append("  CURRENT TIMESTAMP "); // UPDATED
            sql.append("  , (SELECT USERID FROM USER_MST WHERE STAFFCD = '" + request.getParameter("PRINT_LOG_STAFFCD") + "') "); // USERID
            sql.append("  , '" + request.getParameter("PRINT_LOG_STAFFCD") + "' "); // STAFFCD
            sql.append("  , '" + request.getParameter(PRGID) + "' "); // PROGRAMID
            sql.append("  , '" + request.getParameter("PRINT_LOG_REMOTE_IDENT") + "' "); // PCNAME
            sql.append("  , '" + request.getParameter("PRINT_LOG_REMOTE_ADDR") + "' "); // IPADDRESS
            sql.append("  , CAST(NULL AS VARCHAR(1))"); // MACADDRESS
            sql.append("  , 'P' ");  // ACCESS_CD
            sql.append("  ,  0 "); // SUCCESS_CD
            sql.append("  , '" + createPostData(request) + "' "); // POST_DATA
            sql.append(" ) ");
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            ps = db2.prepareStatement(sql.toString());
            ps.executeUpdate();
            
        } catch (final SQLException e) {
            _log.info("ACCESS_LOG_DETAILの保存に失敗しました。");
        } catch (final Exception e) {
            _log.fatal("exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
            if (null != db2) {
                db2.commit();
            }
        }
    }

    private static String createPostData(final HttpServletRequest request) {
        final Set paramSet = new TreeSet();
        final StringBuffer stb = new StringBuffer();
        for (final Enumeration en = request.getParameterNames(); en.hasMoreElements();) {
            final String paramName = (String) en.nextElement();
            if (null != paramName) {
                paramSet.add(paramName);
            }
        }
        String sep = "";
        for (final Iterator it = paramSet.iterator(); it.hasNext();) {
            final String paramName = (String) it.next();
            final String[] values = request.getParameterValues(paramName);
            String comma = "";
            final StringBuffer valueString = new StringBuffer();
            for (int i = 0; i < values.length; i++) {
                valueString.append(comma).append(values[i]);
                comma = ",";
            }
            stb.append(sep).append(paramName).append(" = ").append(valueString.toString());
            sep = "#";
        }
        return stb.toString();
    }

    /*
     * svf_out(req, resp)を起動する。
     */
    private boolean invoke1(
            final String className,
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final Object[] methodInfo,
            final boolean isOutputLog
    ) {
        _log.info("create " + className + "...");
        final Class clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            if (isOutputLog) {
                _log.error(className + ":" + e.getClass().getName());
            }
            return true;
        }

        final Object instance;
        try {
            instance = clazz.newInstance();
        } catch (Exception e) {
            if (isOutputLog) {
                _log.error(className + ":" + e.getClass().getName());
            }
            return true;
        }

        boolean hasException = false;
        try {
            resp.setContentType("application/pdf");
            final String methodName = (String) methodInfo[0];
            final Class[] methodParamClass = (Class[]) methodInfo[1];
            final Object[] methodParam = (Object[]) methodInfo[2];
            final Object retVal = invokeReflect(clazz, instance, methodName, methodParamClass, methodParam, true);
            hasException = null != retVal && Boolean.TRUE.equals(retVal);
        } catch (Exception e) {
            if (isOutputLog) {
                _log.fatal("exception!", e);
            }
            hasException = true;
        }
        return hasException;
    }

    /*
     * svf_out(req, resp)を起動する。
     */
    private boolean invoke(
            final String className,
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final Object[] methodInfo,
            final boolean isOutputLog
    ) {
        _log.info("create " + className + "...");
        final Class clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            if (isOutputLog) {
                _log.error(className + ":" + e.getClass().getName());
            }
            return true;
        }

        final Object instance;
        try {
            instance = clazz.newInstance();
        } catch (Exception e) {
            if (isOutputLog) {
                _log.error(className + ":" + e.getClass().getName());
            }
            return true;
        }
        final String methodName = (String) methodInfo[0];
        final Class[] methodParamClass = (Class[]) methodInfo[1];
        final Object[] methodParam = (Object[]) methodInfo[2];

        invokeReflect(clazz, instance, methodName, methodParamClass, methodParam, isOutputLog);
        return false;
    }

    private Object invokeReflect(
            final Class clazz,
            final Object instance,
            final String methodName,
            final Class[] methodParamTypes,
            final Object[] args,
            final boolean isOutputLog
    ) {
        _log.info("invoke " + clazz.getName() + "." + methodName + "()...");
        final String name = clazz.getName();
        Object retVal = null;
        try {
            retVal = clazz.getMethod(methodName, methodParamTypes).invoke(instance, args);
            _log.info("finish " + name + "." + methodName + "()");
        } catch (IllegalArgumentException e) {
            if (isOutputLog) {
                _log.error(name + ":" + e.getClass().getName());
            }
            retVal = Boolean.TRUE;
        } catch (SecurityException e) {
            if (isOutputLog) {
                _log.error(name + ":" + e.getClass().getName());
            }
            retVal = Boolean.TRUE;
        } catch (IllegalAccessException e) {
            if (isOutputLog) {
                _log.error(name + ":" + e.getClass().getName());
            }
            retVal = Boolean.TRUE;
        } catch (NoSuchMethodException e) {
            if (isOutputLog) {
                _log.error(name + ":" + e.getClass().getName());
            }
            retVal = Boolean.TRUE;
        } catch (InvocationTargetException e) {
            if (isOutputLog) {
                _log.error(name, e);
            }
            retVal = Boolean.TRUE;
        } catch (Exception e) {
            if (isOutputLog) {
                _log.error(name, e);
            }
            retVal = Boolean.TRUE;
        }
        return retVal;
    }
    
    private String getBasePathHome(final HttpServletRequest req) throws Exception {
        String aliasName = "";
        final String referer = req.getHeader("Referer"); // 呼び出し元
        if (null != referer && referer.startsWith("http://")) {
            final String withouthttp = referer.substring(7); // "http://"以降
            final String[] split = StringUtils.split(withouthttp, "/");
            aliasName = "/" + split[1];
        }
        final String httpdConfPath = "/opt/IBM/HTTPServer/conf/httpd.conf";
        final Map aliasMap = getAliasDocumentRootMap(httpdConfPath);
//        _log.info(" aliasbasePathHomeMap = " + aliasMap);
//        _log.info(" basePathHome = " + aliasMap.get(aliasName));
        return (String) aliasMap.get(aliasName);
    }

    private Map getAliasDocumentRootMap(final String httpdConfPath) throws IOException {
        final Map aliasDocumentRootMap = new HashMap();
        BufferedReader fr = null;
//        final Pattern pat = Pattern.compile("Alias\\s+([A-Za-z0-9_/]+)\\s+\"([A-Za-z0-9_/]+)\"");
        try {
            fr = new BufferedReader(new FileReader(httpdConfPath));
            String line;
            while ((line = fr.readLine()) != null) {
//                final Matcher matcher = pat.matcher(line);
//                if (matcher.matches()) {
//                    final String aliasName = matcher.group(1);
//                    final String documentRoot = matcher.group(2);
//                    aliasDocumentRootMap.put(aliasName, documentRoot);
//                }
            }
        } catch (Exception e) {
            _log.fatal("getDocumentRoot:" + e.getClass().getName());
        } finally {
            if (null != fr) {
                fr.close();
            }
        }
        return aliasDocumentRootMap;
    }
    

    /**
     * 処理できなかったプログラムIDを処理します。
     * @param prgid プログラムID
     * @param req リクエスト
     * @param resp レスポンス
     * @throws ServletException Servlet例外
     * @throws IOException IO例外
     */
    public void doGet(
            final String prgid,
            final HttpServletRequest req,
            final HttpServletResponse resp
    ) throws ServletException, IOException {
        if (null == prgid) {
            throw new ServletException("");
        }
        _log.error("unknown " + PRGID + ":" + prgid);
    }
} // KNJServlet

// eof
