// kanji=漢字
/*
 * $Id: 38c13cb8a63bf4226d3dbf33419d9e5d924e775b $
 *
 * 作成日: 2006/03/15 17:05:00 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */

package servletpack.examples;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author takaesu
 * @version $Id: 38c13cb8a63bf4226d3dbf33419d9e5d924e775b $
 */
public class Main {
    public static final String LINE_SEPA = System.getProperty("line.separator", "\n");

    /*pkg*/static final Log log = LogFactory.getLog(Main.class);

    private static final String KENJA = "KENJA";
    private static final String SVFOUT = "svf_out";
    private static final Class[] SVFOUT_PARAM_TYPES = new Class[] {HttpServletRequest.class, HttpServletResponse.class};

    /**
     * 起動するクラスの名前をシステムプロパティから得て svf_out()を実行する。(例:VM引数として「-DKENJA=servletpack.KNJD.KNJD102H」)
     * @param args コマンドライン引数。(例:「DBNAME=//tokio:50000/R1214TE category_selected=20051013 category_selected=20051023 YEAR=2005 GAKKI=2 GRADE_HR_CLASS=01001 TESTKINDCD=01 DATE=2006/01/12」)
     */
    public static void main(final String[] args) throws ServletException, IOException {
        log.debug(LINE_SEPA + "----" + LINE_SEPA);
        final String kenja = System.getProperty(KENJA);
        final File dir = dirName();
        final String fileName = fileName(kenja);

        info(kenja, dir, fileName, args);

        final Req req = new Req(args);
        final Resp resp = new Resp(dir, fileName);
        if (invoke(kenja, req, resp)) {
            final File output = resp.getFile();

            final String exec = System.getProperty("EXEC", "true");
            log.debug("exec=" + exec);
            if (BooleanUtils.toBoolean(exec)) {
                exec(output);
            }
        }
    }

    private static File dirName() {
        final String dirname = System.getProperty("DIR", ".");
        final File dir = new File(dirname);
        dir.mkdirs();
        return dir;
    }

    private static String fileName(final String kenja) {
        final SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd-HHmmss");
        final String date = fmt.format(new Date());
        return "debug-" + ClassUtils.getShortClassName(kenja) + "-" + date;
    }

    private static void info(final String kenja, final File dir, final String fileName, final String[] args) {
        final File file = new File(dir, fileName + ".log");

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file)));

            // 絶対パス
            pw.println("# " + file.getAbsolutePath());

            // 日付
            pw.println("# " + new Date());

            // システムプロパティ
            pw.println("-D" + KENJA + "=" + kenja);

            // コマンドライン
            String spc = "";
            for (int i = 0; i < args.length; i++) {
                pw.print(spc + args[i]);
                //
                spc = " ";
            }
            pw.println();
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException", e);
        } finally {
            if (null != pw) {
                pw.close();
            }
        }
    }

    /*
     * svf_out(req, resp)を起動する。
     */
    private static boolean invoke(
            final String name,
            final HttpServletRequest req,
            final HttpServletResponse resp
    ) {
        log.info("invoke " + name + "." + SVFOUT + "()...");
        try {
            final Class clazz = Class.forName(name);
            final Object instance = clazz.newInstance();
            clazz.getMethod(SVFOUT, SVFOUT_PARAM_TYPES).invoke(instance, new Object[] {req, resp, });
            log.info("finish " + name + "." + SVFOUT + "()");
            return true;
        } catch (final Exception e) {
            log.error(name, e);
            return false;
        }
    }

    private static void exec(final File file) {
        log.debug("os.name=" + System.getProperty("os.name"));
        if (SystemUtils.IS_OS_WINDOWS) {
            log.debug("YES! I'm run on Windows!");
            try {
                Runtime.getRuntime().exec("cmd.exe /C start " + file.getAbsolutePath());
            } catch (IOException e) {
                log.error("IOException", e);
            }
        }
    }

    // =======================================================================

    private static class Req extends NullHttpServletRequest {
        private Map _map;

        public Req(final String[] args) {
            _map = Collections.unmodifiableMap(convert(createMap(args)));
            log.debug(toStr(_map));
        }

        private static String toStr(final Map map) {
            final StringBuffer sb = new StringBuffer();
            sb.append("{");

            String comma = "";
            for (final Iterator it = map.entrySet().iterator(); it.hasNext();) {
                final Map.Entry element = (Map.Entry) it.next();
                final String key = (String) element.getKey();
                final String[] array = (String[]) element.getValue();

                sb.append(comma).append(key).append("=");
                append(sb, array);
                //
                comma = ", ";
            }

            sb.append("}");
            return sb.toString();
        }

        private static void append(final StringBuffer sb, final String[] array) {
            sb.append("[");
            String comma = "";
            for (int i = 0; i < array.length; i++) {
                sb.append(comma).append(array[i]);
                //
                comma = ", ";
            }
            sb.append("]");
        }

        private static Map createMap(final String[] args) {
            final Map map = new HashMap();
            for (int i = 0; i < args.length; i++) {
                final int indexOf = args[i].indexOf('=');
                final String key = args[i].substring(0, indexOf);
                final String val = args[i].substring(indexOf + 1);

                List list = (List) map.get(key);
                if (null == list) {
                    list = new LinkedList();
                    map.put(key, list);
                }
                list.add(val);
            }
            return map;
        }

        private static Map convert(final Map map) {
            final Map rtn = new HashMap();
            for (final Iterator it = map.entrySet().iterator(); it.hasNext();) {
                final Map.Entry element = (Map.Entry) it.next();
                final String key = (String) element.getKey();
                final List list = (List) element.getValue();
                final String[] val = (String[]) list.toArray(new String[list.size()]);
                rtn.put(key, val);
            }
            return rtn;
        }

        public String getParameter(final String name) {
            final String[] array = (String[]) _map.get(name);
            if (null != array && 0 != array.length) {
                return array[0];
            }

            log.fatal("#### [" + name + "] is not found! ####");
            return null;
        }

        public String[] getParameterValues(final String name) {
            final String[] array = (String[]) _map.get(name);
            if (null != array) {
                return array;
            }

            log.fatal("#### [" + name + "] is not found! ####");
            return null;
        }

        public Enumeration getParameterNames() {
            return IteratorUtils.asEnumeration(_map.keySet().iterator());
        }
    } // Req

    // =======================================================================

    private static class Resp extends NullHttpServletResponse {
        private File _file;
        private File _dir;
        private final String _baseName;
        private String _contentType;
        private MyStream _strm;
        private PrintWriter _pwt;
        private String _lock;

        public Resp(final File dir, final String fileName) {
            _dir = dir;
            _baseName = fileName;
        }

        public void setContentType(final String type) {
            _contentType = type;
        }

        public File getFile() {
            return _file;
        }

        private String getExtName() {
            // type の値の例:
            // - "text/html;charset=Shift_JIS"
            // - "text/html"
            // - "application/pdf"

            if (null == _contentType) {
                log.fatal("#### contentType is null! --> default 'pdf' ####");
                return "pdf";
            }

            if (_contentType.startsWith("text/html")) {
                return "html";
            }
            if (_contentType.startsWith("application/pdf")) {
                return "pdf";
            }

            if (_contentType.startsWith("application/vnd.ms-excel")) {
                return "xls";
            }

            if (_contentType.startsWith("text/octet-stream")) {
                return "csv";
            }

            log.fatal("#### [" + _contentType + "] unknown type! ####");

            return "txt";
        }

        private File createFile() {
            final String extName = getExtName();
            final File file = new File(_dir, _baseName + "." + extName);
            log.debug("file=" + file.getAbsolutePath());
            return file;
        }

        private synchronized void lock(final String name) {
            if (null == _lock) {
                _lock = name;
            } else if (!_lock.equals(name)) {
                throw new IllegalStateException ();
            }
        }

        public synchronized ServletOutputStream getOutputStream() throws IOException {
            lock("getOutputStream");
            if (null == _strm) {
                _file = createFile();
                _strm = new MyStream(_file);
            }
            return _strm;
        }

        public synchronized PrintWriter getWriter() throws IOException {
            lock("getWriter");

            if (null == _pwt) {
                _file = createFile();
                _pwt = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(_file)), "euc-jp"));
            }
            return _pwt;
        }

        private void close() {
            if (null != _strm) {
                try {
                    _strm.flush();
                } catch (IOException e) {
                    log.error("IOException", e);
                }
            }

            if (null != _pwt) {
                _pwt.flush();
            }
        }

        protected void finalize() throws Throwable {
            try {
                close();
            } finally {
                super.finalize();
            }
        }
    } // Resp

    // =======================================================================

    private static class MyStream extends ServletOutputStream {
        private OutputStream _os;

        public MyStream(final File file) throws FileNotFoundException {
            super();
            _os = new BufferedOutputStream(new FileOutputStream(file));
        }

        public void write(final int b) throws IOException {
            _os.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            _os.write(b, off, len);
        }

        public void write(byte[] b) throws IOException {
            _os.write(b);
        }

        public void close() throws IOException {
            _os.close();
        }

        public void flush() throws IOException {
            try {
                _os.flush();
            } finally {
                super.flush();
            }
        }

        protected void finalize() throws Throwable {
            try {
                flush();
            } finally {
                super.finalize();
            }
        }
    } // MyStream
} // Main

// eof
