/*
 * $Id$
 *
 * 作成日: 2016/01/07
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import nao_package.db.DB2UDB;

public class CsvUtils {

    public static List newLine(final List lines) {
        final List line = new ArrayList();
        lines.add(line);
        return line;
    }

    /**
     * CSVの元データを作成する
     * @param isOutputHeader ヘッダを出力するか
     * @param columnMapList 列データのリスト ex) [{"FIELD": "SCHREGNO", "LABEL" : "学籍番号"}, {"FIELD" : "NAME", "LABEL" : "氏名"}]
     * @param dataMapList データのリスト ex) [{"SCHREGNO": "00000001", "NAME" : "太郎"}, {"SCHREGNO" : "00000002", "NAME" : "花子"}]
     * @return CSVの元データ ex) [["00000001", "太郎"], ["00000002", "花子"]]
     */
    public static List getDataOutputLines(final boolean isOutputHeader, final List /* List<Map<String, String>> */ columnMapList, final List /* List<Map<String, String>> */ dataMapList) {
        final List<List<String>> lines = new ArrayList<List<String>>();
        if (isOutputHeader) {
            final List<String> headerLine = newLine(lines);
            for (int i = 0; i < columnMapList.size(); i++) {
                final Map columnMap = (Map) columnMapList.get(i);
                if (null == columnMap) {
                    headerLine.add(null);
                } else {
                    headerLine.add((String) columnMap.get("LABEL"));
                }
            }
        }
        for (int di = 0; di < dataMapList.size(); di++) {
            final Map dataMap = (Map) dataMapList.get(di);
            final List<String> line = newLine(lines);

            for (int i = 0; i < columnMapList.size(); i++) {
                final Map headerMap = (Map) columnMapList.get(i);
                if (null == headerMap) {
                    line.add(null);
                } else {
                    final String field = (String) headerMap.get("FIELD");
                    line.add(null == dataMap.get(field) ? "" : dataMap.get(field).toString());
                }
            }
        }
        return lines;
    }

    public static List<List<String>> copy(final List<List<String>> lines) {
        final List<List<String>> rtn = new ArrayList<List<String>>();
        for (final List<String> line : lines) {
            rtn.add(new ArrayList<String>(line));
        }
        return rtn;
    }

    public static List /*List<List<String>>*/ horizontalUnionLines(final List /*List<List<String>>*/ lines1Src, final List /*List<String>*/ lines2Src) {
        final List<List<String>> lines1 = copy(lines1Src);
        final List<List<String>> lines2 = copy(lines2Src);
        final List<List<String>> lines = new ArrayList();
        if (null == lines1 && null == lines2) {
            return lines;
        } else if (null == lines1) {
            lines.addAll(lines2);
            return lines;
        } else if (null == lines2) {
            lines.addAll(lines1);
            return lines;
        }
        int lines1MaxColumn = 0;
        for (int i = 0; i < lines1.size(); i++) {
            lines1MaxColumn = Math.max(lines1MaxColumn, lines1.get(i).size());
        }

        int lines2MaxColumn = 0;
        for (int i = 0; i < lines2.size(); i++) {
            lines2MaxColumn = Math.max(lines2MaxColumn, lines2.get(i).size());
        }

        final int maxLine = Math.max(lines1.size(), lines2.size());

        lines.addAll(lines1);

        for (int li = 0; li < maxLine; li++) {
            final List<String> line;
            if (li < lines.size()) {
                line = lines.get(li);
            } else {
                line = newLine(lines);
            }
            for (int ci = line.size(); ci < lines1MaxColumn; ci++) {
                line.add(null);
            }
            if (li < lines2.size()) {
                line.addAll(lines2.get(li));
            }
            for (int ci = line.size(); ci < lines1MaxColumn + lines2MaxColumn; ci++) {
                line.add(null);
            }
        }

        return lines;
    }

    private static String join(final List /* List<Object> */ columns, final String spl1) {
        final StringBuffer stb = new StringBuffer();
        String spl = "";
        for (final Object s : columns) {
            stb.append(spl).append(null == s ? "" : StringUtils.defaultString(s.toString()));
            spl = spl1;
        }
        return stb.toString();
    }

    public static String columnListListToData(final List /* List<List<Object>> */ columnListList) {
        final List<String> rtn = new ArrayList<String>();
        for (int i = 0; i < columnListList.size(); i++) {
            final List columnList = (List) columnListList.get(i);
            rtn.add(join(columnList, ","));
        }
        return join(rtn, "\n");
    }

    private static boolean isIE(final Map parameter) {
        boolean isIE = true; // デフォルトはtrue
        if (null != parameter && null != parameter.get("HttpServletRequest")) {
            final HttpServletRequest req = (HttpServletRequest) parameter.get("HttpServletRequest");
            isIE = -1 != StringUtils.defaultString(req.getHeader("User-Agent")).indexOf("Trident");
        }
        return isIE;
    }

    public static void outputLines(final Log log, final HttpServletResponse response, final String filename, final List lines) {
        outputLines(log, response, filename, lines, new HashMap());
    }

    /**
     * HttpServletResponseにCSVを出力する
     * @param log
     * @param response
     * @param filename ブラウザに返すファイル名
     * @param lines CSVの元データ
     */
    public static void outputLines(final Log log, final HttpServletResponse response, final String filename, final List lines, final Map parameter) {
        log.info(" CsvUtils $Revision: 71502 $ ");
        OutputStream os = null;
        try {
            if (lines.size() == 0 && null != parameter && null == parameter.get("noErrorIfEmpty")) {
                response.setContentType("text/html; charset=UTF-8");

                String message = "データは存在していません。";
                if (null != parameter.get("DB2UDB")) {
                    final DB2UDB db2 = (DB2UDB) parameter.get("DB2UDB");
                    final String code = StringUtils.defaultString((String) parameter.get("ERROR_MSG_CD"), "MSG303");
                    final String msg = (String) parameter.get("ERROR_MSG");
                    final String errorMessage = errorMessage(db2, code, msg);
                    message = StringUtils.defaultString(errorMessage, message);
                }
                final String js = "<script text=\"javascript\">alert(\"" + message.replace("\"", "\\\"") + "\");window.open('', '_self').close();</script>";
                response.setContentLength(js.getBytes("UTF-8").length);

                final PrintStream printStream = new PrintStream(response.getOutputStream());
                printStream.println(js);
                return;
            }

            //log.info(" response charenc = " + response.getCharacterEncoding());
            final String outputData = columnListListToData(lines);
            boolean ie = isIE(parameter);
            log.info(" ie ? " + ie);
            final String filenameEncoding = ie ? "MS932" : "UTF-8";
            //log.info(" filenameEncoding = " + filenameEncoding);
            final byte[] data = outputData.getBytes("MS932");
            response.setContentType("text/octet-stream");
            response.setHeader("Accept-Ranges", "none");
            if (ie) {
                final String encodedFilename = new String(filename.getBytes(filenameEncoding), "ISO8859-1");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFilename + "\"");
            } else {
                response.setHeader("Content-disposition", "attachment; filename*=UTF-8''\"" + URLEncoder.encode(filename, "UTF-8") + "\"");
            }
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setHeader("Content-Length", String.valueOf(data.length));

            os = new BufferedOutputStream(response.getOutputStream());
            os.write(data);
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            try {
                if (null != os) {
                    os.close();
                }
            } catch (Exception ex) {
            }
        }
    }

    private static void appendJson(final StringBuffer stb, final Object o) {
        if (o instanceof Map) {
            stb.append(toJson((Map) o));
        } else if (o instanceof Collection) {
            stb.append(toJson((Collection) o));
        } else if (o == null) {
            stb.append("null");
        } else if (o instanceof String) {
            stb.append('"').append(o).append('"');
        } else {
            stb.append(o.toString());
        }
    }

    public static String toJson(final Collection col) {
        final StringBuffer stb = new StringBuffer();
        stb.append("[");
        String comma = "";
        for (final Object o : col) {
            stb.append(comma);
            appendJson(stb, o);
            comma = ", ";
        }
        stb.append("]");
        return stb.toString();
    }

    public static String toJson(final Map objects) {
        final StringBuffer stb = new StringBuffer();
        stb.append("{");
        String comma = "";
        for (final Iterator it = objects.entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = (Map.Entry) it.next();
            stb.append(comma);
            stb.append('"').append(e.getKey()).append('"');
            stb.append(':');
            appendJson(stb, e.getValue());
            comma = ", ";
        }
        stb.append("}");
        return stb.toString();
    }

    /**
     * HttpServletResponseにJSONを出力する
     * @param log
     * @param response
     * @param map JSONへ変換するデータ
     */
    public static void outputJson(final Log log, final HttpServletRequest request, final HttpServletResponse response, final String json, final Map parameter) {
        log.info(" CsvUtils $Revision: 71502 $ ");
        OutputStream os = null;
        try {
            final byte[] data = json.getBytes();
            response.setContentType("application/json");
            response.setHeader("Content-Length", String.valueOf(data.length));

            os = new BufferedOutputStream(response.getOutputStream());
            os.write(data);
            os.flush();
        } catch (Exception e) {
            log.error("exception!", e);
        }
    }

    private static String errorMessage(final DB2UDB db2, final String code, final String msg) {
        final String sql = " SELECT MSG_CONTENT FROM MESSAGE_MST WHERE MSG_CD = ? ";
        String msgContent = StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql, new Object[] {code})));
        msgContent = msgContent.replace("\r", "\\r");
        msgContent = msgContent.replace("\n", "\\n");
        return code + "\\r\\n\\r\\n" + msgContent + "\\r\\n" + StringUtils.defaultString(msg);
    }
}
