// kanji=漢字
/*
 * $Id$
 *
 */
package servletpack.KNJZ.detail.dao;

import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * @version $Id$
 */
public class AttendAccumulateIO {

    private static Log log = LogFactory.getLog(AttendAccumulateIO.class);
    private static String revision = "$Revision: 62377 $ $Date: 2018-09-19 22:04:42 +0900 (水, 19 9 2018) $++";

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private static String KIND_SEMES = "ATTEND_SEMES_DAT";
    private static String KIND_SUBCLASS = "ATTEND_SUBCLASS_DAT";

    /**
     * @param request
     * @param response
     */
    public void json(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        PrintStream ps = null;
        try {
            response.setContentType("application/json");
            response.setStatus(200);

            final Map resultMap = common(request);
            final String json = toJson(resultMap);

            final int length = json.length();
            log.info(" length = " + length);
            response.setContentLength(length);

            ps = new PrintStream(response.getOutputStream());
            ps.println(json);
            ps.flush();

        } catch (Exception e) {
            log.error("exception!", e);
            if (null != ps) {
                ps.println("error: " + e.getMessage());
            }
        }
    }

    private static String toJson(final Object o) {
        final StringBuffer stb = new StringBuffer();
        if (o instanceof Map) {
            final Map m = (Map) o;
            String comma = "";
            stb.append("{");
            for (final Iterator it = m.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final Object key = e.getKey();
                final Object val = e.getValue();
                stb.append(comma).append(toJson(key)).append(": ").append(toJson(val));
                comma = ", ";
            }
            stb.append("}");
        } else if (o instanceof List) {
            stb.append("[");
            String comma = "";
            final List l = (List) o;
            for (int i = 0; i < l.size(); i++) {
                final Object e = l.get(i);
                stb.append(comma).append(toJson(e));
                comma = ", ";
            }
            stb.append("]");

        } else if (null == o) {
            stb.append("null");
        } else if (o instanceof String) { // UTF-8対応してない
            stb.append("\"").append(o).append("\"");
        } else {
            stb.append(o);
        }
        return stb.toString();
    }

    private Map common(
            final HttpServletRequest request
    ) throws Exception {
        Map m = new HashMap();
        DB2UDB db2 = null;
        Param param = null;
        try {
            KNJServletUtils.debugParam(request, log);
            // ＤＢ接続
            try {
                db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
                db2.open();
            } catch(Exception ex) {
                log.error("db2 instancing exception! ", ex);
                return null;
            }
            param = new Param(request, db2);

            process(db2, param, m);

        } catch (Exception e) {
            log.error("exception!", e);
            throw e;
        } finally {
            if (null != param) {
                DbUtils.closeQuietly(param._ps);
            }

            if (null != db2) {
                db2.close();
            }
        }
        return m;
    }

    private void process(final DB2UDB db2, final Param param, final Map m) {
        ResultSet rs = null;
        try {
            log.info(" queryParamList = " + param._queryParamList);
            for (int i = 0; i < param._queryParamList.size(); i++) {
                final TreeMap params = (TreeMap) param._queryParamList.get(i);

                final List rowList = KnjDbUtils.query(db2, param._ps, getArray(params));
                log.info(" query " + params + " : " + rowList.size());
                for (final Iterator it = rowList.iterator(); it.hasNext();) {
                    final Map row = (Map) it.next();
                    if (!"9".equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                        continue;
                    }
                    for (final Iterator rit = row.keySet().iterator(); rit.hasNext();) {
                        final Object key = rit.next();
                        if (key instanceof Integer) {
                            rit.remove();
                        }
                    }
                    getMappedList(m, KnjDbUtils.getString(row, "SCHREGNO")).add(row);
                }
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(rs);
        }
    }

    private Object[] getArray(final TreeMap params) {
        final Integer max = (Integer) params.lastKey();
        final Object[] arr = new Object[max.intValue()];
        for (final Iterator it = params.keySet().iterator(); it.hasNext();) {
            final Integer idx = (Integer) it.next();
            arr[idx.intValue() - 1] = params.get(idx);
        }
        return arr;
    }

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static class Param {
        final String _attendDiv;
        final String _year;
        final String _semester;
        final String _sdate;
        final String _date;
        String[] _gradeHrClass; // オプション1
        String[] _schregno; // オプション2
        String _grade; // オプション3
        String[] _hrClass; // オプション3
        final List _queryParamList;

        private PreparedStatement _ps;
        final Map _attendParamMap;
        final boolean _isOutputDebug;

        Param(final HttpServletRequest request, final DB2UDB db2) throws SQLException {
            _attendDiv = request.getParameter("ATTEND_DIV");
            _year = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("CTRL_SEMESTER");
            _sdate = StringUtils.isBlank(request.getParameter("SDATE")) ? null : request.getParameter("SDATE").replace('/', '-');
            _date = request.getParameter("DATE").replace('/', '-');

            _gradeHrClass = StringUtils.split(request.getParameter("GRADE_HR_CLASS"), ",");
            _grade = request.getParameter("GRADE");
            _schregno = StringUtils.split(request.getParameter("SCHREGNO"), ",");
            _hrClass = StringUtils.split(request.getParameter("HR_CLASS"), ",");

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);

            _queryParamList = new ArrayList();
            if (!isEmptyParameter(_gradeHrClass)) {
                for (int i = 0; i < _gradeHrClass.length; i++) {
                    final Map paramMap = new TreeMap();
                    paramMap.put(new Integer(1), _gradeHrClass[i].substring(0, 2));
                    paramMap.put(new Integer(2), _gradeHrClass[i].substring(2));
                    _queryParamList.add(paramMap);
                }
                _attendParamMap.put("grade", "?");
                _attendParamMap.put("hrClass", "?");
            } else if (!isEmptyParameter(_schregno)) {
                for (int i = 0; i < _schregno.length; i++) {
                    final Map paramMap = new TreeMap();
                    paramMap.put(new Integer(1), _schregno[i]);
                    _queryParamList.add(paramMap);
                }
                _attendParamMap.put("schregno", "?");
            } else if (!isEmptyParameter(_hrClass)) {
                for (int i = 0; i < _hrClass.length; i++) {
                    final Map paramMap = new TreeMap();
                    paramMap.put(new Integer(1), _hrClass[i]);
                    _queryParamList.add(paramMap);
                }
                _attendParamMap.put("grade", _grade);
                _attendParamMap.put("hrClass", "?");
            }

            String sql = null;
            if (KIND_SEMES.equals(_attendDiv)) {
                sql = AttendAccumulate.getAttendSemesSql(
                        _year,
                        "9",
                        _sdate,
                        _date,
                        _attendParamMap
                );
                if (_isOutputDebug) {
                    log.info("get AttendSemes sql = " + sql);
                }
                _ps = db2.prepareStatement(sql);
            } else if (KIND_SUBCLASS.equals(_attendDiv)) {
                // 時数単位
                sql = AttendAccumulate.getAttendSubclassSql(
                        _year,
                        "9",
                        _sdate,
                        _date,
                        _attendParamMap
                        );
                if (_isOutputDebug) {
                    log.info("get AttendSubclass sql = " + sql);
                }
                _ps = db2.prepareStatement(sql);
            }
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'AttendAccumulateIO' AND NAME = '" + propName + "' "));
        }

        // パラメータがブランクか
        private boolean isEmptyParameter(final String[] p) {
            if (null == p) {
                return true;
            }
            for (int i = 0; i < p.length; i++) {
                if (!StringUtils.isBlank(p[i])) {
                    return false;
                }
            }
            return true;
        }
    }

} // AttendAccumulateIO

// eof
