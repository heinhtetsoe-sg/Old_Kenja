/*
 *
 * 作成日: 2020/03/26
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;


import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;

public class KNJE390M {

    private static final Log log = LogFactory.getLog(KNJE390M.class);

    private static final String FORM_A1 = "KNJE390M_A_1_2.frm";
    private static final String FORM_B1 = "KNJE390M_B_1_2.frm";
    private static final String FORM_D1 = "KNJE390M_D_1_2.frm";
    private boolean _hasData;

    /** 中央寄せ */
    private static final String ATTR_CENTERING = "Hensyu=3";

    KNJE390M_sien _sienObj = null;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            Param _param = createParam(db2, request);

            _hasData = false;

            if ("1".equals(_param._printB)) {
                _sienObj = new KNJE390M_sien();
                _sienObj.svf_out(request, response, db2);
            }

            printMain(db2, svf, _param);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Param param) {

        final List formList = new ArrayList();
        int printBIdx = -1;
        if ("1".equals(param._printA)) formList.add(new KNJE390MB(param)); // Ｂ．プロフィール
        if ("1".equals(param._printB)) {
            formList.add(new KNJE390MC(param)); // Ｃ．支援計画  ※登録順番に依存しているので、入れ替えは注意。また、このクラスの実際の処理はKNJE390M_sienにあるので注意。
            printBIdx = formList.size();
        }
        if ("1".equals(param._printC)) formList.add(new KNJE390MD(param)); // Ｄ．移行支援計画
        if ("1".equals(param._printD)) formList.add(new KNJE390MA(param)); // Ａ．アセスメント票
        if ("1".equals(param._printE)) formList.add(new KNJE390ME(param)); //サポートブック
        if ("1".equals(param._printF)) formList.add(new KNJE390MF(param)); //引継資料(担任)
        if ("1".equals(param._printG)) formList.add(new KNJE390MG(param)); //引継資料(事業者)
        if ("1".equals(param._printH)) formList.add(new KNJE390MH(param)); //関係者間資料
        if ("1".equals(param._printI)) formList.add(new KNJE390MI(param)); //実態表

        final String studentSql = getStudentSql(param);
        if (param._isOutputDebug) {
            log.info(" studentSql = " + studentSql);
        }
        final List studentList = new ArrayList();
        PreparedStatement ps = null;
        try {
            ps = db2.prepareStatement(studentSql);

            for (int i = 0; i < param._categorySelected.length; i++) {
                final String schregno = param._categorySelected[i];

                final Map student = KnjDbUtils.firstRow(KnjDbUtils.query(db2, ps, new Object[] {schregno}));

                studentList.add(student);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Map student = (Map) it.next();
            for (int cnt = 0;cnt < formList.size();cnt++) {
                //Bを印刷指定していて、KNJE390M_sienが生成済みで、Bの出力順になったら処理。
                if ("1".equals(param._printB) && _sienObj != null && cnt == printBIdx-1) {
                    _sienObj.printMain(db2, svf, (String)student.get("SCHREGNO"));
                } else {
                    //final KNJE390M_0 form = (KNJE390M_0) fit.next();
                    final KNJE390M_0 form = (KNJE390M_0) formList.get(cnt);
                    form.printMain(svf, db2, student);
                }
                _hasData = true;
            }
        }

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 77085 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    private static abstract class KNJE390M_0 {
        String ATTR_PAINT_GRAY_FILL = "PAINT=(0,85,2)";

        final Param _param;

        KNJE390M_0(final Param param) {
            _param = param;
        }
        public abstract void printMain(final Vrw32alp svf, final DB2UDB db2, final Map student);

        protected static List minLength(final List list, final int minLength) {
            for (int i = 0, count = minLength - list.size(); i < count; i++) {
                list.add(null);
            }
            return list;
        }

        /**
         * @param source 元文字列
         * @param bytePerLine 1行あたりのバイト数
         * @return bytePerLineのバイト数ごとの文字列リスト
         */
        protected static List getTokenList(final String source0, final int bytePerLine) {

            if (source0 == null || source0.length() == 0 || bytePerLine == -1) {
                return new ArrayList();
            }
            return KNJ_EditKinsoku.getTokenList(source0, bytePerLine);
        }

        protected static List getTokenList(final String source0, final int bytePerLine, final int gyo) {

            if (source0 == null || source0.length() == 0) {
                return new ArrayList();
            }
            return KNJ_EditKinsoku.getTokenList(source0, bytePerLine, gyo);
        }


        protected static void svfVrListOut(
                final Form form,
                final String field,
                final List tokenList
        ) {
            svfVrListOutWithStart(form, field, 1, tokenList);
        }

        protected static void svfVrListOutWithStart(
                final Form form,
                final String field,
                final int start,
                final List tokenList
        ) {
            for (int j = 0; j < tokenList.size(); j++) {
                if (null == tokenList.get(j)) {
                    continue;
                }
                form.VrsOut(field + String.valueOf(start + j), (String) tokenList.get(j));
            }
        }

        protected static void svfVrListOutn(
                final Form form,
                final String field,
                final List tokenList
        ) {
            for (int j = 0; j < tokenList.size(); j++) {
                if (null == tokenList.get(j)) {
                    continue;
                }
                form.VrsOutn(field, j + 1, (String) tokenList.get(j));
            }
        }

        protected static void svfVrListOutn(
                final Form form,
                final String field,
                final int n,
                final List tokenList
        ) {
            for (int j = 0; j < tokenList.size(); j++) {
                if (null == tokenList.get(j)) {
                    continue;
                }
                form.VrsOutn(field + String.valueOf(j + 1), n, (String) tokenList.get(j));
            }
        }

        protected static void svfVrsOutWithSize(
                final Form form,
                final String fieldHead,
                final String[] field,
                final int[] byteSize,
                final String data
        ) {
            final int bsize = KNJ_EditEdit.getMS932ByteLength(data);
            boolean out = false;
            for (int i = 0; i < field.length - 1; i++) {
                if (bsize <= byteSize[i]) {
                    form.VrsOut(fieldHead + field[i], data);
                    out = true;
                    break;
                }

            }
            if (!out) {
                form.VrsOut(fieldHead + field[field.length - 1], data);
            }
        }

        protected static void svfVrsOutnWithSize(
                final Form form,
                final String fieldHead,
                final String[] field,
                final int[] byteSize,
                final int n,
                final String data
        ) {
            final int bsize = KNJ_EditEdit.getMS932ByteLength(data);
            boolean out = false;
            for (int i = 0; i < field.length - 1; i++) {
                if (bsize <= byteSize[i]) {
                    form.VrsOutn(fieldHead + field[i], n, data);
                    out = true;
                    break;
                }

            }
            if (!out) {
                form.VrsOutn(fieldHead + field[field.length - 1], n, data);
            }
        }

        protected static String getString(final String field, final Map m) {
            if (null == m) {
                log.info("unimplemented? " + field);
                return null;
            }
            if (m.isEmpty()) {
                return null;
            }
            if (!m.containsKey(field)) {
                try {
                    throw new IllegalStateException("フィールドなし:" + field + ", " + m);
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
            final String retStr = StringUtils.defaultString((String) m.get(field));
            return retStr;
        }

        protected static Map getFirstRow(final List list) {
            if (list.size() == 0) {
                return new HashMap();
            }
            return (Map) list.get(0);
        }

        protected static List withDummy(final List list) {
            if (list.isEmpty()) {
                list.add(new HashMap());
            }
            return list;
        }

        protected static Map getRowMap(final DB2UDB db2, final String sql, final String keyField) {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                final Integer[] idxs = new Integer[meta.getColumnCount() + 1];
                for (int col = 1; col <= meta.getColumnCount(); col++) {
                    idxs[col] = new Integer(col);
                }
                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int col = 1; col <= meta.getColumnCount(); col++) {
                        final String val = rs.getString(col);
                        m.put(meta.getColumnLabel(col), val);
                        m.put(idxs[col], val);
                    }
                    rtn.put(rs.getString(keyField), m);
                }
            } catch (SQLException e) {
                log.error("exception! sql = " + sql, e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        protected static List getRowList(final DB2UDB db2, final String sql) {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                final Integer[] idxs = new Integer[meta.getColumnCount() + 1];
                for (int col = 1; col <= meta.getColumnCount(); col++) {
                    idxs[col] = new Integer(col);
                }
                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int col = 1; col <= meta.getColumnCount(); col++) {
                        final String val = rs.getString(col);
                        m.put(meta.getColumnLabel(col), val);
                        m.put(idxs[col], val);
                    }
                    rtn.add(m);
                }
            } catch (SQLException e) {
                log.error("exception! sql = " + sql, e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        protected static Map createCondMap(final String[] keyval) {
            if (keyval.length % 2 != 0) {
                throw new IllegalArgumentException("引数の個数が奇数:" + ArrayUtils.toString(keyval));
            }
            final Map m = new HashMap();
            for (int i = 0; i < keyval.length; i += 2) {
                m.put(keyval[i], keyval[i + 1]);
            }
            return m;
        }

        protected static List filterList(final List rowList, final Map cond) {
            final List rtn = new ArrayList();
            for (final Iterator it = rowList.iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                boolean notMatched = false;
                for (final Iterator ci = cond.entrySet().iterator(); ci.hasNext();) {
                    final Map.Entry e = (Map.Entry) ci.next();
                    final String dataVal = getString((String) e.getKey(), row);
                    final String val = (String) e.getValue();
                    if (null == val && null == dataVal || null != val && val.equals(dataVal)) {
                    } else {
                        notMatched = true;
                    }
                }
                if (!notMatched) {
                    rtn.add(row);
                }
            }
            return rtn;
        }

        protected static String getOne(final DB2UDB db2, final String sql) throws SQLException {
            final Map row = getFirstRow(getRowList(db2, sql));
            if (row.isEmpty()) {
                return null;
            }
            return (String) row.get(new Integer(1));
        }

        protected static String formatDate(final DB2UDB db2, final String date) {
            if (null == date) {
                return null;
            }
            if ("".equals(date)) {
                return "";
            }
            final String[] tateFormat = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, date));
            final String nengo = KNJ_EditDate.gengouAlphabetMarkOfDate(db2, StringUtils.replace(date, "-", "/"));
            return nengo + ("元".equals(tateFormat[1]) ? "1" : tateFormat[1]) + "." + tateFormat[2] + "." + tateFormat[3];
        }

        protected static String formatDateYearMonth(final DB2UDB db2, final String date) {
            if (null == date) {
                return null;
            }
            if ("".equals(date)) {
                return "";
            }
            final String[] tateFormat = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, date));
            final String nengo = KNJ_EditDate.gengouAlphabetMarkOfDate(db2, StringUtils.replace(date, "-", "/"));
            return nengo + ("元".equals(tateFormat[1]) ? "1" : tateFormat[1]) + "." + tateFormat[2];
        }

        protected static LinkedList singleton(final String s) {
            final LinkedList l = new LinkedList();
            l.add(s);
            return l;
        }

        protected static LinkedList asList(final String[] array) {
            final LinkedList l = new LinkedList();
            for (int i = 0; i < array.length; i++) {
                l.add(array[i]);
            }
            return l;
        }

        protected static List seq(final int startInclusive, final int endExcludive) {
            final List l = new ArrayList();
            for (int i = startInclusive; i < endExcludive; i++) {
                l.add(String.valueOf(i));
            }
            return l;
        }

        protected static List groupByCount(final List list, final int max) {
            final List rtn = new ArrayList();
            List current = null;
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Object o = it.next();
                if (null == current || current.size() >= max) {
                    current = new ArrayList();
                    rtn.add(current);
                }
                current.add(o);
            }
            return rtn;
        }

        public static String prepend(final String prep, final Object o) {
            return null == o || StringUtils.isBlank(o.toString()) ? "" : prep + o.toString();
        }

        public static String append(final Object o, final String app) {
            return null == o || StringUtils.isBlank(o.toString()) ? "" : o.toString() + app;
        }

        // 実態
        protected static String[] getJittaiTitleArray2(final String dataDiv) {
            String[] title = new String[] {""};
            if ("00".equals(dataDiv)) {
                title = new String[] {"概要"};
            } else if ("01".equals(dataDiv)) {
                title = new String[] {"基本的生活習慣", "身辺自立"};
            } else if ("02".equals(dataDiv)) {
                title = new String[] {"行動・社会性"};
            } else if ("03".equals(dataDiv)) {
                title = new String[] {"言語", "コミュニケーション"};
            } else if ("04".equals(dataDiv)) {
                title = new String[] {"身体・運動"};
            } else if ("05".equals(dataDiv)) {
                title = new String[] {"学習"};
            } else if ("06".equals(dataDiv)) {
                title = new String[] {"興味・強み"};
            }
            return title;
        }

        protected Map getUpdateHistMap(final DB2UDB db2, final String dataType, final String year, final String schregno, final int max, final String writingDate) {

            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     RECORD_DATE AS UPDATE_DATE, ");
            sql.append("     RECORD_STAFFNAME AS UPDATE_STAFFNAME ");
            sql.append(" FROM ");
            sql.append("     SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_MAIN_DAT ");
            sql.append(" WHERE ");
            sql.append("     SCHREGNO = '" + schregno + "' AND ");
            sql.append("     RECORD_DATE = (SELECT ");
            sql.append("                         MAX(RECORD_DATE) ");
            sql.append("                     FROM ");
            sql.append("                         SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_MAIN_DAT ");
            sql.append("                     WHERE ");
            sql.append("                         SCHREGNO = '" + schregno + "' ");
            sql.append("                     ) ");

            //log.debug(" update time sql = " + sql.toString());
            final List resultList = getRowList(db2, sql.toString());
            final Map rtn = new HashMap();

            String createName = null;
            if (resultList.size() > 0) {
                final Map row = (Map) resultList.get(0);
                createName = getString("UPDATE_STAFFNAME", row);
            }
            rtn.put("CREATE_STAFFNAME", createName);
            rtn.put("CREATE_DATE",  writingDate);

            if (writingDate != null && !"".equals(writingDate)) {
                final StringBuffer sql3 = new StringBuffer();
                sql3.append(" SELECT ");
                sql3.append("     RECORD_DATE, ");
                sql3.append("     RECORD_STAFFNAME ");
                sql3.append(" FROM ");
                sql3.append("     SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT ");
                sql3.append(" WHERE ");
                sql3.append("     SCHREGNO ='" + schregno + "' AND");
                sql3.append("     WRITING_DATE ='" + writingDate + "'");

                //log.debug(" update time sql = " + sql.toString());
                final List resultList3 = getRowList(db2, sql3.toString());

                String recordStaffName = null;
                String recordDate = null;
                if (resultList3.size() > 0) {
                    final Map row = (Map) resultList3.get(0);
                    recordStaffName = getString("RECORD_STAFFNAME", row);
                    recordDate = getString("RECORD_DATE", row);
                }
                rtn.put("RECORD_STAFFNAME", recordStaffName);
                rtn.put("RECORD_DATE",  recordDate);
            }

            final StringBuffer sql2 = new StringBuffer();
            sql2.append("SELECT ");
            sql2.append("   DATE(T1.RECORD_DATE) AS UPDATE_DATE ");
            sql2.append("   , T2.STAFFNAME AS UPDATE_STAFFNAME ");
            sql2.append(" FROM CHALLENGED_TABLE_UPDATE_LOG T1 ");
            sql2.append(" INNER JOIN (SELECT RECORD_DATE, MIN(UPDATED) AS UPDATED FROM CHALLENGED_TABLE_UPDATE_LOG T0 ");
            sql2.append("   WHERE ");
            sql2.append("     T0.DATA_TYPE = '" + dataType + "' ");
            sql2.append("     AND T0.SCHREGNO = '" + schregno + "' ");
            if (null != year) {
                sql2.append("   AND T0.YEAR = '" + year + "' ");
            }
            sql2.append("   AND T0.RECORD_DATE <> 'NEW' ");
            sql2.append("   GROUP BY RECORD_DATE ");
            sql2.append(" ) T3 ON T3.UPDATED = T1.UPDATED ");
            sql2.append(" LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
            sql2.append(" ORDER BY 1 ");

            //log.debug(" update time2 sql = " + sql2.toString());
            final List resultList2 = getRowList(db2, sql2.toString()); // 更新日付昇順

            final List list = new ArrayList(); // resultList2の最後から最大max件日付昇順
            for (int start = Math.max(resultList2.size() - max, 0), i = start; i < start + max; i++) {
                if (i < resultList2.size()) {
                    list.add(resultList2.get(i));
                }
            }

            for (int n = 1; n <= max; n++) {
                String updateNameN = null, updateDateN = null;
                if (n - 1 < list.size()) {
                    final Map row = (Map) list.get(n - 1);
                    updateNameN = getString("UPDATE_STAFFNAME", row);
                    updateDateN = getString("UPDATE_DATE", row);
                }
                rtn.put("UPDATE_STAFFNAME" + String.valueOf(n), updateNameN);
                rtn.put("UPDATE_DATE" + String.valueOf(n),  updateDateN);
            }
            return rtn;
        }

        protected List addLineIfLessThanCount(final List tokenList, final int count) {
            final List list = new ArrayList();
            if (null != tokenList) {
                list.addAll(tokenList);
            }
            for (int i = 0; i < count - tokenList.size(); i++) {
                list.add("");
            }
            return list;
        }

        protected static List extendStringList(final List list, final int size, final boolean centering) {
            final LinkedList rtn = new LinkedList();
            final int msize = Math.max(list.size(), size);
            if (centering) {
                final int blankCount = (msize - list.size()) / 2;
                for (int i = 0; i < blankCount; i++) {
                    rtn.add(null);
                }
            }
            rtn.addAll(list);
            for (int i = rtn.size(); i < msize; i++) {
                rtn.add(null);
            }
            return rtn;
        }

        protected static Map getMappedMap(final Map map, final String key) {
            if (null == map.get(key)) {
                map.put(key, new TreeMap());
            }
            return (Map) map.get(key);
        }

        protected static List getMappedList(final Map map, final String key) {
            if (null == map.get(key)) {
                map.put(key, new ArrayList());
            }
            return (List) map.get(key);
        }

        protected static List repeat(final String data, final int count) {
            final List list = new ArrayList();
            for (int i = 0; i < count; i++) {
                list.add(data);
            }
            return list;
        }

        protected static void setRecordFieldDataAll(final List printRecordList, final String fieldDivName, final String data) {
            setRecordFieldDataList(printRecordList, fieldDivName, repeat(data, printRecordList.size()));
        }

        protected static void setRecordFieldDataList(final List printRecordList, final String fieldDivName, final List dataList) {
            for (int j = 0, max = printRecordList.size(); j < max; j++) {
                final Map record = (Map) printRecordList.get(j);
                final Map fieldDivNameMap = getMappedMap(record, FieldData.FIELD_DIV_NAME);
                if (null == fieldDivNameMap.get(fieldDivName)) {
                    log.warn("no field " + fieldDivName + " in " + fieldDivNameMap + " / " + record);
                    continue;
                }
                record.put(fieldDivNameMap.get(fieldDivName), dataList.get(j));
            }
        }

        protected String getPrintStudentHrname(final Map student) {
            return getString("2".equals(_param._printHrClassType) ? "GHR_NAMEABBV" : "HR_NAMEABBV", student);
        }
        protected String getPrintStudentGrHrname(final Map student) {
            return getString("GRADE_NAME2", student) + getString("2".equals(_param._printHrClassType) ? "GHR_NAMEABBV" : "HR_CLASS_NAME2", student);
        }
    }

    // プロフィール
    private static class KNJE390MB extends KNJE390M_0 {

        private String useD; // コードチェック用
        private String D1 = "1";
        private String D2 = "2";
        private String D3 = "3";
        private String D4 = "4";
        private String D5 = "5";
        private String D6 = "6";
        private String D7 = "7";
        private String D8 = "8";
        private String D9 = "9";
        private String D10 = "10";
        private String DCARE = "CARE";
        private String ATTR_COLOR_GRAY = "Palette=8";
        private String ATTR_COLOR_WHITE = "Palette=13";

        public KNJE390MB(final Param param) {
            super(param);
        }

        public void printMain(final Vrw32alp svf, final DB2UDB db2, final Map student) {
            final String schregno = getString("SCHREGNO", student);

            final Form form = new Form(_param, svf);
            form._form1 = !"".equals(_param._formA_1) ? _param._formA_1 : "KNJE390M_A_1.frm";
            form._form2 = !"".equals(_param._formA_2) ? _param._formA_2 : "KNJE390M_A_2.frm";
            form._form2n = 4;
            form._recMax1 = 12;
            form._recMax2 = 43;
            form._isForm1 = true;
            form.setForm(form._form1, 4);
            for (int i = 0; i <= 60; i++) {
                form._svf.VrAttribute("HEAD_" + String.valueOf(i), ATTR_PAINT_GRAY_FILL);
            }
            form._svf.VrAttribute("HEAD_19_2" , ATTR_PAINT_GRAY_FILL);
            form._svf.VrAttribute("HEAD_15_3" , ATTR_PAINT_GRAY_FILL);
            form._svf.VrAttribute("HEAD_16_3" , ATTR_PAINT_GRAY_FILL);
            form._svf.VrAttribute("HEAD_17_3" , ATTR_PAINT_GRAY_FILL);
            form._svf.VrAttribute("HEAD_18_3" , ATTR_PAINT_GRAY_FILL);
            form._svf.VrAttribute("HEAD_17_2" , ATTR_PAINT_GRAY_FILL);
            form._svf.VrAttribute("HEAD_18_2" , ATTR_PAINT_GRAY_FILL);

            printStudent(db2, student, schregno, form);

            form._recMax = form._recMax1;

            printBSaigaiji(db2, schregno, form);

            printBRecordKyoikureki(db2, schregno, form);

            blank();

            if (form._isForm1) {
                form.setForm2();
            }

            printBRecordIryou(db2, schregno, form);

            printBTraining(db2, schregno, form);
            printBRecordKenkouKanri(db2, schregno, form);
            printBEyes(db2, schregno, form);
            printBRecordFukushi(db2, schregno, form);

            printBRecordBiko(db2, schregno, form);
            if ("1".equals(_param._aoutputA1)) {
                printBSeiiku(db2, schregno, form);
            }
        }

        public void printStudent(final DB2UDB db2, final Map student, final String schregno, final Form form) {
            form.VrsOut("SCHOOL_NAME", _param._knjSchoolMst._schoolName1); // 学校名
            form.VrsOut("FACULTY_NAME", "学部"); // 学部名
            form.VrsOut("DEPARTMENT_NAME", StringUtils.defaultString(getString("COURSENAME", student))); // 学科名

            form.VrsOut("GRADE_NAME", getPrintStudentGrHrname(student)); // 学年・組

            final Map main = getAmain(db2, schregno);

            final Map updateMap = getUpdateHistMap(db2, "B", null, schregno, 1, getString("WRITING_DATE", main));

            if (!FORM_A1.equals(_param._formA_1)) {
                form.VrsOut("MAKE_DATE", formatDate(db2, getString("CREATE_DATE", updateMap))); // 作成日
                svfVrsOutWithSize(form, "MAKER", new String[] {"1", "2"}, new int[] {10, 16}, getString("CREATE_STAFFNAME", updateMap));  // 作成者
            }
            svfVrsOutWithSize(form, "ENTRY_PERSON", new String[] {"1", "2"}, new int[] {10, 16}, getString("UPDATE_STAFFNAME1", updateMap));  // 記入者
            form.VrsOut("UPDATE", formatDate(db2, getString("UPDATE_DATE1", updateMap))); // 更新日
            final String RSName = getString("RECORD_STAFFNAME", main);
            final int mlen = KNJ_EditEdit.getMS932ByteLength(RSName);
            if (mlen > 30) {
                final String[] cutStr = KNJ_EditEdit.get_token(RSName, 40, 2);
                form.VrsOut("MAKER3_2", cutStr[0]);
                if (mlen > 40) {
                    form.VrsOut("MAKER3_3", cutStr[1]);
                }
            } else {
                form.VrsOut("MAKER3", RSName); // 作成者
            }

            svfVrsOutWithSize(form, "KANA", new String[] {"2", "3"}, new int[] {30, 40}, getString("NAME_KANA", student)); // 氏名かな
            svfVrsOutWithSize(form, "NAME", new String[] {"2", "3"}, new int[] {26, 48}, getString("NAME", student)); // 氏名
            svfVrsOutWithSize(form, "GUARD_KANA", new String[] {"2", "3"}, new int[] {30, 40}, getString("GUARD_KANA", student)); // 氏名かな
            svfVrsOutWithSize(form, "GUARD_NAME", new String[] {"2", "3"}, new int[] {26, 48}, getString("GUARD_NAME", student)); // 保護者氏名

            form.VrsOut("SEX", getString("SEX_NAME", student)); // 性別
            form.VrsOut("BIRTHDAY", formatDate(db2, getString("BIRTHDAY", student))); // 生年月日
            form.VrsOut("GUARD_RELATION", getString("GUARD_RELATIONSHIP_NAME", student)); // 保護者続柄

            form.VrsOut("ZIP1", StringUtils.isBlank(getString("ZIPCD", student)) ? "" : "〒" + getString("ZIPCD", student)); // 郵便番号
            final int a11 = KNJ_EditEdit.getMS932ByteLength(getString("ADDR1", student));
            final int a12 = KNJ_EditEdit.getMS932ByteLength(getString("ADDR2", student));
            final String a1field = (a11 > 38 || a12 > 38) ? "_2" : "";
            form.VrsOut("ADDR1_1" + a1field, getString("ADDR1", student)); // 生徒住所
            form.VrsOut("ADDR1_2" + a1field, getString("ADDR2", student)); // 生徒住所

            form.VrsOut("ZIP2", StringUtils.isBlank(getString("GUARD_ZIPCD", student)) ? "" : "〒" + getString("GUARD_ZIPCD", student)); // 郵便番号

            final int a21 = KNJ_EditEdit.getMS932ByteLength(getString("GUARD_ADDR1", student));
            String a2field = "";
            if ("1".equals(getString("GUARD_ADDR_FLG", student))) {
                final int a22 = KNJ_EditEdit.getMS932ByteLength(getString("GUARD_ADDR2", student));
                a2field = (a21 > 38 || a22 > 38) ? "_2" : "";
                form.VrsOut("ADDR2_2" + a2field, getString("GUARD_ADDR2", student)); // 保護者住所
            } else {
                a2field = a21 > 38 ? "_2" : "";
            }
            form.VrsOut("TELNO1", getString("TELNO", student));    // 自宅連絡先
            form.VrsOut("TELNO1_2", getString("TELNO2", student)); // 自宅連絡先
            form.VrsOut("CONTACT_NAME1", getString("TELNO_MEMO", student)); // 備考
            form.VrsOut("CONTACT_NAME1_2", getString("TELNO2_MEMO", student)); // 備考
            form.VrsOut("CONTACT_REMARK1", getString("EMAIL", student));  //自宅(EMAIL)

            form.VrsOut("ADDR2_1" + a2field, getString("GUARD_ADDR1", student)); // 保護者住所
            form.VrsOut("TELNO1_3", getString("GUARD_TELNO", student));  //保護者(電話番号2)
            form.VrsOut("TELNO1_4", getString("GUARD_TELNO2", student));  //保護者(電話番号2)
            form.VrsOut("CONTACT_REMARK1_3", getString("GUARD_E_MAIL", student));   //保護者(E-Mail)

            form.VrsOut("TELNO2", getString("EMERGENCYTELNO", student));      // 連絡先1
            form.VrsOut("TELNO2_2", getString("EMERGENCYTELNO_2", student));  // 連絡先2
            form.VrsOut("TELNO3", getString("EMERGENCYTELNO2", student));     // 連絡先3
            form.VrsOut("TELNO3_2", getString("EMERGENCYTELNO2_2", student)); // 連絡先4

            form.VrsOut("CONTACT_NAME2", getString("EMERGENCYMEMO1_1", student));   // 備考1
            form.VrsOut("CONTACT_NAME2_2", getString("EMERGENCYMEMO1_2", student)); // 備考2
            form.VrsOut("CONTACT_NAME3", getString("EMERGENCYMEMO2_1", student));   // 備考3
            form.VrsOut("CONTACT_NAME3_2", getString("EMERGENCYMEMO2_2", student)); // 備考4

            final String crStr1 = StringUtils.defaultString(getString("EMERGENCYCALL", student)) + " " + StringUtils.defaultString(getString("EMERGENCYNAME", student)) + " " + StringUtils.defaultString(getString("EMERGENCYRELA_NAME", student));
            final String[] crCutStr1 = KNJ_EditEdit.get_token(crStr1, 56, 2);
            final String crStr2 = StringUtils.defaultString(getString("EMERGENCYCALL2", student)) + " " + StringUtils.defaultString(getString("EMERGENCYNAME2", student)) + " " + StringUtils.defaultString(getString("EMERGENCYRELA_NAME2", student));
            final String[] crCutStr2 = KNJ_EditEdit.get_token(crStr2, 56, 2);

            form.VrsOut("CONTACT_REMARK2", crCutStr1 != null ? crCutStr1[0] : "");
            form.VrsOut("CONTACT_REMARK2_2", (crCutStr1 != null && crCutStr1.length > 1) ? crCutStr1[1] : "");
            form.VrsOut("CONTACT_REMARK3", crCutStr2 != null ? crCutStr2[0] : "");
            form.VrsOut("CONTACT_REMARK3_2", (crCutStr2 != null && crCutStr2.length > 1) ? crCutStr2[1] : "");

            //form.VrsOut("CONTACT_NAME2", intersparse(new String[] {getString("EMERGENCYCALL", student), getString("EMERGENCYNAME", student)}, " ")); // 備考

            final StringBuffer sqlSchregRelaDat = new StringBuffer();
            sqlSchregRelaDat.append("SELECT ");
            sqlSchregRelaDat.append("   T1.* ");
            sqlSchregRelaDat.append("   , H201.NAME1 AS RELATIONSHIP_NAME ");
            sqlSchregRelaDat.append(" FROM SCHREG_RELA_DAT T1 ");
            sqlSchregRelaDat.append(" LEFT JOIN NAME_MST H201 ON H201.NAMECD1 = 'H201' AND H201.NAMECD2 = T1.RELATIONSHIP ");
            sqlSchregRelaDat.append(" WHERE T1.SCHREGNO = '" + schregno + "' ");
            sqlSchregRelaDat.append(" ORDER BY T1.RELANO ");
            final List schregRelaDatList = getRowList(db2, sqlSchregRelaDat.toString());
            for (int j = 0; j < Math.min(schregRelaDatList.size(), 10); j++) {
                final int line;
                final String lr;
                if (j >= 5) {
                    line = j - 5 + 1;
                    lr = "2";
                } else {
                    line = j + 1;
                    lr = "1";
                }
                final Map m = (Map) schregRelaDatList.get(j);
                final String name = getString("RELANAME", m);
                final int size = KNJ_EditEdit.getMS932ByteLength(name);
                if (size <= 16) {
                    form.VrsOutn("FAMILY_NAME" + lr + "_1", line, name); // 家族氏名
                } else if (size <= 32) {
                    form.VrsOutn("FAMILY_NAME" + lr + "_2", line, name); // 家族氏名
                } else {
                    svfVrListOutn(form, "FAMILY_NAME" + lr + "_3_", line, getTokenList(name, 32)); // 家族氏名
                }
                form.VrsOutn("FAMILY_NAME_KANA" + lr , line, getString("RELAKANA", m)); // 家族氏名
                final String relname = getString("RELATIONSHIP_NAME", m);
                final int relnamesize = KNJ_EditEdit.getMS932ByteLength(relname);
                if (relnamesize <= 4) {
                    form.VrsOutn("FAMILY_RELATION" + lr, line, relname); // 家族続柄
                } else if (relnamesize <= 6) {
                    form.VrsOutn("FAMILY_RELATION" + lr + "_2", line, relname); // 家族続柄
                } else {
                    form.VrsOutn("FAMILY_RELATION" + lr + "_3_1", line, relname); // 家族続柄
                }
                form.VrsOutn("FAMILY_REMARK" + lr, line, getString("REMARK", m)); // 家族備考
            }

            form.VrsOut("ATTEND_METHOD1", getString("TSUUGAKU_DIV1_NAME", main)); // 通学方法
            form.VrsOut("ATTEND_METHOD2", getString("TSUUGAKU_DIV2_NAME", main)); // 通学方法
            form.VrsOut("ATTEND_REMARK1", getString("TSUUGAKU_DIV1_REMARK", main)); // 通学方法
            form.VrsOut("ATTEND_REMARK2", getString("TSUUGAKU_DIV2_REMARK", main)); // 通学方法

            svfVrListOut(form, "DIAG_NAME", getTokenList(getString("CHALLENGED_NAMES", main), form.fieldKeta("DIAG_NAME1"))); // 障害名診断名

            svfVrListOut(form, "HANDI_CONDITION", getTokenList(getString("CHALLENGED_STATUS", main), form.fieldKeta("HANDI_CONDITION1"))); // 障害の実態・特性

            form.VrsOut("HANDBOOK_NAME", getString("CHALLENGED_CARD_DIV_NAME", main)); // 手帳名

            form.VrsOut("HANDI_KIND", getString("CHALLENGED_CARD_CLASS_NAME", main)); // 障害・種
            form.VrsOut("HANDI_GRADE", getString("CHALLENGED_CARD_RANK_NAME", main)); // 障害・級

            final String cStr1 = StringUtils.defaultString(getString("CARDNAME1", main));
            final String cStr2 = StringUtils.defaultString(getString("CARDNAME2", main));
            final String cStr3 = StringUtils.defaultString(getString("CARDNAME3", main));
            final String cStr4 = StringUtils.defaultString(getString("CARDNAME4", main));
            final String cStr5 = StringUtils.defaultString(getString("CARDNAME5", main));
            final String cardname = cStr1 + prepend(" ", cStr2) + prepend(" ", cStr3 + prepend(" ", cStr4) + prepend(" ", cStr5));
            form.VrsOutSelect(new String[][] {{"HANDI_NAME"}, {"HANDI_NAME2"}, {"HANDI_NAME3_1", "HANDI_NAME3_2"}}, cardname); // 身体障碍者手帳

            form.VrsOut("CHALLENGED_CARD_NAME", getString("CHALLENGED_CARD_NAME", main)); // 精神障碍者保健福祉手帳
            form.VrsOut("CHALLENGED_CARD_REMARK", StringUtils.defaultString(getString("CHALLENGED_CARD_REMARK_NAME", main), "　") + " 級"); // 精神障碍者保健福祉手帳

            form.VrsOut("NEXT_JUDGE", formatYearMonth(db2, getString("CHALLENGED_CARD_CHECK_YM", main))); // 次回判定
            // 次回認定
            if ("1".equals(getString("CHALLENGED_CARD_GRANT_FLG", main))) {
                form.VrsOut("NEXT_JUDGE2", "次回認定なし");
            } else {
                final String grantYm = formatYearMonth(db2, getString("CHALLENGED_CARD_GRANT_YM", main));
                form.VrsOut("NEXT_JUDGE2", StringUtils.defaultString(grantYm));
            }
            if (!"".equals(getString("CHALLENGED_CARD_BAST_YM", main))) {
                form.VrsOut("NEXT_JUDGE3", formatYearMonth(db2, getString("CHALLENGED_CARD_BAST_YM", main))); // 有効期限
            }

            form.VrsOut("UMU3", "1".equals(getString("WELFARE_MEDICAL_RECEIVE_FLG", main)) ? "有" : "無"); // 有無
            String certifName = StringUtils.defaultString(getString("CERTIFNAME1", main)) + prepend(" ", getString("CERTIFNAME2", main)) + prepend(" ", getString("CERTIFNAME3", main)) + prepend(" ", getString("CERTIFNAME4", main)) + prepend(" ", getString("CERTIFNAME5", main));
            form.VrsOutSelect(new String[][] {{"RECEPT_RANK"}, {"RECEPT_RANK_2"}}, certifName); // 障害支援区分

            form.VrsOut("ELE_SCHOOL_NAME", getString("P_FINSCHOOL_NAME", main)); // 小学校名
            form.VrsOut("JH_SCHOOL_NAME", getString("J_FINSCHOOL_NAME", main)); // 中学校名
        }

        private Map getAmain(final DB2UDB db2, final String schregno) {
            final StringBuffer sql1 = new StringBuffer();
            sql1.append("SELECT ");
            sql1.append("   T1.* ");
            sql1.append("   , NM_E035.NAME1 AS WELFARE_MEDICAL_RECEIVE_DIV_NM ");
            sql1.append("   , PFIN.FINSCHOOL_NAME AS P_FINSCHOOL_NAME ");
            sql1.append("   , JFIN.FINSCHOOL_NAME AS J_FINSCHOOL_NAME ");
            sql1.append("   , E031.NAME1 AS CHALLENGED_CARD_CLASS_NAME ");
            sql1.append("   , E032.NAME1 AS CHALLENGED_CARD_RANK_NAME ");
            sql1.append("   , E036.NAME1 AS TSUUGAKU_DIV1_NAME ");
            sql1.append("   , E036_2.NAME1 AS TSUUGAKU_DIV2_NAME ");
            sql1.append("   , E041.NAME1 AS CHALLENGED_CARD_DIV_NAME ");
            sql1.append("   , E061.NAME1 AS CHALLENGED_CARD_NAME ");
            sql1.append("   , E063.NAME1 AS CHALLENGED_CARD_REMARK_NAME ");
            sql1.append("   , CCNM1.CARDNAME AS CARDNAME1 ");
            sql1.append("   , CCNM2.CARDNAME AS CARDNAME2 ");
            sql1.append("   , CCNM3.CARDNAME AS CARDNAME3 ");
            sql1.append("   , CCNM4.CARDNAME AS CARDNAME4 ");
            sql1.append("   , CCNM5.CARDNAME AS CARDNAME5 ");
            sql1.append("   , CCENM1.CERTIFNAME AS CERTIFNAME1 ");
            sql1.append("   , CCENM2.CERTIFNAME AS CERTIFNAME2 ");
            sql1.append("   , CCENM3.CERTIFNAME AS CERTIFNAME3 ");
            sql1.append("   , CCENM4.CERTIFNAME AS CERTIFNAME4 ");
            sql1.append("   , CCENM5.CERTIFNAME AS CERTIFNAME5 ");
            sql1.append(" FROM SCHREG_CHALLENGED_PROFILE_MAIN_DAT T1 ");
            sql1.append(" LEFT JOIN NAME_MST NM_E035 ON NM_E035.NAMECD1 = 'E035' AND NM_E035.NAMECD2 = T1.WELFARE_MEDICAL_RECEIVE_DIV ");
            sql1.append(" LEFT JOIN FINSCHOOL_MST PFIN ON PFIN.FINSCHOOLCD = T1.P_SCHOOL_CD ");
            sql1.append(" LEFT JOIN FINSCHOOL_MST JFIN ON JFIN.FINSCHOOLCD = T1.J_SCHOOL_CD ");
            sql1.append(" LEFT JOIN NAME_MST E031 ON E031.NAMECD1 = 'E031' AND E031.NAMECD2 = T1.CHALLENGED_CARD_CLASS ");
            sql1.append(" LEFT JOIN NAME_MST E032 ON E032.NAMECD1 = 'E032' AND E032.NAMECD2 = T1.CHALLENGED_CARD_RANK ");
            sql1.append(" LEFT JOIN NAME_MST E036 ON E036.NAMECD1 = 'E036' AND E036.NAMECD2 = T1.TSUUGAKU_DIV1 ");
            sql1.append(" LEFT JOIN NAME_MST E036_2 ON E036_2.NAMECD1 = 'E036' AND E036_2.NAMECD2 = T1.TSUUGAKU_DIV2 ");
            sql1.append(" LEFT JOIN NAME_MST E041 ON E041.NAMECD1 = 'E041' AND E041.NAMECD2 = T1.CHALLENGED_CARD_DIV ");
            sql1.append(" LEFT JOIN NAME_MST E061 ON E061.NAMECD1 = 'E061' AND E061.NAMECD2 = T1.CHALLENGED_CARD_NAME ");
            sql1.append(" LEFT JOIN NAME_MST E063 ON E063.NAMECD1 = 'E063' AND E063.NAMECD2 = T1.CHALLENGED_CARD_REMARK ");
            sql1.append(" LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.REGISTERCD ");
            sql1.append(" LEFT JOIN CHALLENGED_CARD_NAME_MST CCNM1 ON CCNM1.CARDNAME_CD = T1.CHALLENGED_CARD_AREA_NAME ");
            sql1.append(" LEFT JOIN CHALLENGED_CARD_NAME_MST CCNM2 ON CCNM2.CARDNAME_CD = T1.CHALLENGED_CARD_AREA_NAME2 ");
            sql1.append(" LEFT JOIN CHALLENGED_CARD_NAME_MST CCNM3 ON CCNM3.CARDNAME_CD = T1.CHALLENGED_CARD_AREA_NAME3 ");
            sql1.append(" LEFT JOIN CHALLENGED_CARD_NAME_MST CCNM4 ON CCNM4.CARDNAME_CD = T1.CHALLENGED_CARD_AREA_NAME4 ");
            sql1.append(" LEFT JOIN CHALLENGED_CARD_NAME_MST CCNM5 ON CCNM5.CARDNAME_CD = T1.CHALLENGED_CARD_AREA_NAME5 ");
            sql1.append(" LEFT JOIN CHALLENGED_CERTIF_NAME_MST CCENM1 ON CCENM1.CERTIFNAME_CD = T1.WELFARE_MEDICAL_RECEIVE_DIV ");
            sql1.append(" LEFT JOIN CHALLENGED_CERTIF_NAME_MST CCENM2 ON CCENM2.CERTIFNAME_CD = T1.WELFARE_MEDICAL_RECEIVE_DIV2 ");
            sql1.append(" LEFT JOIN CHALLENGED_CERTIF_NAME_MST CCENM3 ON CCENM3.CERTIFNAME_CD = T1.WELFARE_MEDICAL_RECEIVE_DIV3 ");
            sql1.append(" LEFT JOIN CHALLENGED_CERTIF_NAME_MST CCENM4 ON CCENM4.CERTIFNAME_CD = T1.WELFARE_MEDICAL_RECEIVE_DIV4 ");
            sql1.append(" LEFT JOIN CHALLENGED_CERTIF_NAME_MST CCENM5 ON CCENM5.CERTIFNAME_CD = T1.WELFARE_MEDICAL_RECEIVE_DIV5 ");
            sql1.append(" WHERE ");
            sql1.append("   T1.SCHREGNO = '" + schregno + "' ");
            sql1.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_PROFILE_MAIN_DAT WHERE SCHREGNO = '" + schregno + "') ");
            if (_param._isOutputDebug) {
                log.info(" Amain sql = " + sql1.toString());
            }
            final Map main = getFirstRow(getRowList(db2, sql1.toString()));
            return main;
        }

        private void blank() {

        }

        private static String formatYearMonth(final DB2UDB db2, final String yearMonth) {
            if (null == yearMonth) {
                return null;
            }
            final String[] split = StringUtils.split(yearMonth, "-");
            if (split.length != 2 || !NumberUtils.isDigits(split[0]) || !NumberUtils.isDigits(split[1])) {
                log.warn("invalid year month format: " + yearMonth);
                return null;
            }
            final Calendar cal = Calendar.getInstance(); // 指定付きの最終日を取得する
            cal.set(Calendar.YEAR, Integer.parseInt(split[0]));
            cal.set(Calendar.MONTH, Integer.parseInt(split[1]) + 1);
            cal.add(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            final DecimalFormat df = new DecimalFormat("00");
            final String date = yearMonth + "-" + df.format(cal.get(Calendar.DAY_OF_MONTH));
            final String[] tate_format = KNJ_EditDate.tate_format4(db2, date);
            return KNJ_EditDate.gengouAlphabetMarkOfDate(db2, date) + ("元".equals(tate_format[1]) ? "1" : tate_format[1]) + "年" + String.valueOf(Integer.parseInt(split[1])) + "月";
        }

        private String yearMonthfromTo(final DB2UDB db2, final String yearMonthFrom, final String yearMonthTo) {
            if (null == yearMonthFrom && null == yearMonthTo) {
                return "";
            }
            final StringBuffer stb = new StringBuffer();
            if (null != yearMonthFrom) {
                stb.append(StringUtils.defaultString(formatYearMonth(db2, yearMonthFrom)));
            }
            stb.append("～");
            if (null != yearMonthTo) {
                stb.append(StringUtils.defaultString(formatYearMonth(db2, yearMonthTo)));
            }
            return stb.toString();
        }

        private void printBSaigaiji(final DB2UDB db2, final String schregno, final Form form) {

            final Map main = getAmain(db2, schregno);

            final List saigaiHeadArray = asList(new String[] {"災害時", "避難場所"});
            final FieldDataGroup dg = new FieldDataGroup();
            dg.addAll("GRPSAIGAI_1", "--"); // グループ1
            dg.attribute("GRPSAIGAI_1", ATTR_COLOR_GRAY); // グループ3
            dg.addCenter("DIVIDESAIGAI_1", saigaiHeadArray); // 区分1
            dg.attribute("DIVIDESAIGAI_1", ATTR_PAINT_GRAY_FILL); // グループ3

            final String[] divs = {"避難場所", "留意事項"};
            final String[] fields = {"EVACUATION_AREA", "IMPORTANT_NOTICE"};

            for (int i = 0; i < divs.length; i++) {

                final String si = "S" + String.valueOf(i);
                final FieldData d = dg.newFieldData();

                d.addAll("GRPSAIGAI_2", si); // グループ3
                d.attribute("GRPSAIGAI_2", ATTR_COLOR_GRAY); // グループ3
                d.addCenter("DIVIDESAIGAI_2", getTokenList(divs[i], form.fieldKeta("DIVIDESAIGAI_2")));
                d.attribute("DIVIDESAIGAI_2", ATTR_PAINT_GRAY_FILL); // グループ3

                d.addAll("GRPSAIGAI_3", si); // グループ5
                d.add("FACILITYSAIGAI", getTokenList(getString(fields[i], main), form.fieldKeta("FACILITYSAIGAI")));
            }

            FieldData.svfPrintRecordList(dg.getPrintRecordList(_param), form);

            form._svf.VrsOut("LINE_DUMMY", "1");
            form._svf.VrEndRecord();
        }

        private void printBSeiiku(final DB2UDB db2, final String schregno, final Form form) {
            final StringBuffer sql2 = new StringBuffer();
            sql2.append("SELECT ");
            sql2.append("   T1.REMARK6 ");
            sql2.append(" FROM SCHREG_CHALLENGED_PROFILE_OTHER_RECORD_DAT T1 ");
            sql2.append(" WHERE ");
            sql2.append("   T1.SCHREGNO = '" + schregno + "' ");
            sql2.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_PROFILE_OTHER_RECORD_DAT WHERE SCHREGNO = '" + schregno + "' AND RECORD_DIV = '001' AND RECORD_SEQ = '1') ");
            sql2.append("   AND T1.RECORD_DIV = '001' ");
            sql2.append("   AND T1.RECORD_SEQ = '1' ");

            final FieldData d = new FieldData();
            d.addAll("GRPBIKO_1", "CA"); // グループ2
            d.attribute("GRPBIKO_1", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ2
            d.addCenter("DIVIDEBIKO_1", singleton("生育歴")); // グループ3
            d.addAll("GRPBIKO_3", "CA"); // 区分2
            d.addAll("GRPBIKO_4", "CA"); // 区分2
            d.addAll("GRPBIKO_5", "CA"); // 区分2
            d.addAll("GRPBIKO_6", "CA"); // 区分2
            try {
                final String edu0 = getOne(db2, sql2.toString());  //1行取得
                d.add("BIKO", getTokenList(edu0, 100)); // 区分2
            } catch  (SQLException ex) {
                log.warn("printBSeiiku SQL Error!", ex);
            }

            FieldData.svfPrintRecordList(d.getPrintRecordList(), form);

            form.VrEndRecord();
        }

        private void printBRecordKyoikureki(final DB2UDB db2, final String schregno, final Form form) {
            final StringBuffer sql2 = new StringBuffer();
            sql2.append("SELECT ");
            sql2.append("   T1.* ");
            sql2.append("   , FIN.FINSCHOOL_NAME AS FINSCHOOL_NAME ");
            sql2.append("   , E038.NAME1 AS CLASS_SHUBETSU_NAME ");
            sql2.append(" FROM SCHREG_CHALLENGED_PROFILE_EDUCATE_RECORD_DAT T1 ");
            sql2.append(" LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = T1.P_J_SCHOOL_CD");
            sql2.append(" LEFT JOIN NAME_MST E038 ON E038.NAMECD1 = 'E038' AND E038.NAMECD2 = T1.CLASS_SHUBETSU");
            sql2.append(" WHERE ");
            sql2.append("   T1.SCHREGNO = '" + schregno + "' ");
            sql2.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_PROFILE_EDUCATE_RECORD_DAT WHERE SCHREGNO = '" + schregno + "' )");
            sql2.append(" ORDER BY VALUE(T1.S_YEAR_MONTH, ''), VALUE(T1.E_YEAR_MONTH, ''), T1.RECORD_DIV, T1.RECORD_NO, T1.GRADE, T1.RECORD_SEQ ");
            final List edu0 = getRowList(db2, sql2.toString());

            final List kyouikurekiArray = asList(new String[] {"療育・", "教育歴", "(就学前", "～現在)"});
            final FieldDataGroup dg = new FieldDataGroup();
            dg.addAll("GRPSCH_1", "--"); // グループ1
            dg.attribute("GRPSCH_1", ATTR_COLOR_GRAY);
            dg.addCenter("DIVIDESCH_1", kyouikurekiArray); // 区分1
            dg.attribute("DIVIDESCH_1", ATTR_PAINT_GRAY_FILL);
            dg.setFieldDiv("GRP1", "GRPSCH_1");
            dg.setFieldDiv("DIV1", "DIVIDESCH_1");

            final FieldData hd = new FieldData();
            hd.addAll("GRPSCH_3", "hd"); // グループ3
            hd.attribute("GRPSCH_3", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY);
            hd.addWithKeta("SCHOOLNAMESCH", "保育所・幼稚園・学校名等", form); // 施設名
            hd.attribute("SCHOOLNAMESCH", ATTR_CENTERING);
            hd.addAll("GRPSCH_5", "hd"); // グループ5
            hd.attribute("GRPSCH_5", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY);
            hd.addWithKeta("F_YEARSCH3", "在籍期間", form); // 開始年
            hd.attribute("F_YEARSCH3", ATTR_CENTERING);
            hd.addAll("GRPSCH_4", "hd"); // グループ7
            hd.attribute("GRPSCH_4", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY);
            hd.addWithKeta("GRADESCH2", "学部・学年・支援学級在籍等", form); // 訓練内容
            dg.addFieldData(hd);

            if (edu0.size() > 0) {
                final String divRyoyo = "RYOYO";
                final String divMae = "MAE";
                final String divGakko1 = "GAKKO1";
                final String divTukyu1 = "TUKYU1";
                final String divGakko2 = "GAKKO2";
                final String divTukyu2 = "TUKYU2";
                final String divShien = "SHIEN";
                final String divKoko = "KOKO";
                final String divDaigaku = "DAIGAKU";

                int grpCnt = 1;
                for (int j = 0; j < edu0.size(); j++) {
                    final Map dataMap = (Map) edu0.get(j);
                    final String recordPart;
                    final String dataRecordDiv = (String) dataMap.get("RECORD_DIV");
                    final String dataRecordNo = (String) dataMap.get("RECORD_NO");

                    List edu = Collections.EMPTY_LIST;
                    if ("3".equals(dataRecordDiv)) {
                        recordPart = divRyoyo;
                    } else if ("1".equals(dataRecordDiv) || "2".equals(dataRecordDiv)) {
                        recordPart = divMae;
                    } else if ("4".equals(dataRecordDiv) && "1".equals(dataRecordNo)) {
                        recordPart = divGakko1;
                    } else if ("4".equals(dataRecordDiv) && "2".equals(dataRecordNo)) {
                        recordPart = divTukyu1;
                    } else if ("5".equals(dataRecordDiv) && "1".equals(dataRecordNo)) {
                        recordPart = divGakko2;
                    } else if ("5".equals(dataRecordDiv) && "2".equals(dataRecordNo)) {
                        recordPart = divTukyu2;
                    } else if ("6".equals(dataRecordDiv)) {
                        recordPart = divShien;
                    } else if ("7".equals(dataRecordDiv)) {
                        recordPart = divKoko;
                    } else if ("8".equals(dataRecordDiv)) {
                        recordPart = divDaigaku;
                    } else {
                        continue;
                    }
                    edu = new ArrayList();
                    edu.add(dataMap);

                    if (divRyoyo.equals(recordPart)) {
                        // 教育歴 療養施設
                        for (int i = 0; i < edu.size(); i++) {
                            final Map row = (Map) edu.get(i);
                            final String si = String.valueOf(grpCnt);
                            final FieldData d = new FieldData();

                            d.addAll("GRPSCH_3", si); // グループ3
                            d.attribute("GRPSCH_3", ATTR_COLOR_WHITE);
                            d.addWithKeta("SCHOOLNAMESCH", getString("SCHOOL_NAME", row), form); // 施設名

                            d.addAll("GRPSCH_5", si); // グループ5
                            d.attribute("GRPSCH_5", ATTR_COLOR_WHITE);
                            d.add("F_YEARSCH3", singleton(yearMonthfromTo(db2, getString("S_YEAR_MONTH", row), getString("E_YEAR_MONTH", row)))); // 開始年

                            d.addAll("GRPSCH_4", si); // グループ7
                            d.attribute("GRPSCH_4", ATTR_COLOR_WHITE);
                            d.addWithKeta("GRADESCH2", getString("REMARK", row), form); // 訓練内容
                            dg.addFieldData(d);
                        }
                    } else if (divMae.equals(recordPart)) {
                        // 教育歴 就学前

                        for (int i = 0; i < edu.size(); i++) {
                            final Map row = (Map) edu.get(i);
                            final String si = String.valueOf(grpCnt);
                            final FieldData d = new FieldData();

                            d.addAll("GRPSCH_3", si); // グループ3
                            d.attribute("GRPSCH_3", ATTR_COLOR_WHITE);
                            d.addWithKeta("SCHOOLNAMESCH", getString("SCHOOL_NAME", row), form); // 施設名

                            d.addAll("GRPSCH_5", si); // グループ5
                            d.attribute("GRPSCH_5", ATTR_COLOR_WHITE);
                            d.add("F_YEARSCH3", singleton(yearMonthfromTo(db2, getString("S_YEAR_MONTH", row), getString("E_YEAR_MONTH", row)))); // 開始年

                            d.addAll("GRPSCH_4", si); // グループ7
                            d.attribute("GRPSCH_4", ATTR_COLOR_WHITE);
                            d.addWithKeta("GRADESCH2", getString("REMARK", row), form); // 訓練内容

                            dg.addFieldData(d);
                            grpCnt++;
                        }
                    } else if (recordPart.startsWith("GAKKO")) {
                        // 教育歴 学校
                        for (int i = 0; i < edu.size(); i++) {
                            final Map row = (Map) edu.get(i);
                            final String si = String.valueOf(grpCnt);

                            final FieldData d = new FieldData();
                            d.addAll("GRPSCH_3", si); // グループ3
                            d.attribute("GRPSCH_3", ATTR_COLOR_WHITE);
                            d.addWithKeta("SCHOOLNAMESCH", getString("FINSCHOOL_NAME", row), form); // 施設名

                            d.addAll("GRPSCH_5", si); // グループ5
                            d.attribute("GRPSCH_5", ATTR_COLOR_WHITE);
                            d.add("F_YEARSCH3", singleton(yearMonthfromTo(db2, getString("S_YEAR_MONTH", row), getString("E_YEAR_MONTH", row)))); // 開始年

                            d.addAll("GRPSCH_4", si); // グループ7
                            d.attribute("GRPSCH_4", ATTR_COLOR_WHITE);
                            d.addWithKeta("GRADESCH2", getString("REMARK", row), form); // 訓練内容
                            dg.addFieldData(d);
                            grpCnt++;
                        }
                    } else if (recordPart.startsWith("TUKYU")) {
                        // 教育歴 学校 通級指導
                        for (int i = 0; i < edu.size(); i++) {
                            final Map row = (Map) edu.get(i);
                            final String si = String.valueOf(grpCnt);

                            final FieldData d = new FieldData();
                            d.addAll("GRPSCH_3", si); // グループ3
                            d.attribute("GRPSCH_3", ATTR_COLOR_WHITE);
                            d.addWithKeta("SCHOOLNAMESCH", getString("FINSCHOOL_NAME", row), form); // 施設名

                            d.addAll("GRPSCH_5", si); // グループ5
                            d.attribute("GRPSCH_5", ATTR_COLOR_WHITE);
                            d.add("F_YEARSCH3", singleton(yearMonthfromTo(db2, getString("S_YEAR_MONTH", row), getString("E_YEAR_MONTH", row)))); // 開始年

                            d.addAll("GRPSCH_4", si); // グループ7
                            d.attribute("GRPSCH_4", ATTR_COLOR_WHITE);
                            d.addWithKeta("GRADESCH2", getString("REMARK", row), form); // 訓練内容
                            dg.addFieldData(d);
                            grpCnt++;
                        }

                    } else if (divShien.equals(recordPart)) {

                        for (int i = 0; i < edu.size(); i++) {
                            final Map row = (Map) edu.get(i);
                            final String si = String.valueOf(grpCnt);

                            final FieldData d = new FieldData();
                            // 教育歴 特別支援
                            d.addAll("GRPSCH_3", si); // グループ3
                            d.attribute("GRPSCH_3", ATTR_COLOR_WHITE);
                            d.addWithKeta("SCHOOLNAMESCH", getString("FINSCHOOL_NAME", row), form); // 施設名

                            d.addAll("GRPSCH_5", si); // グループ5
                            d.attribute("GRPSCH_5", ATTR_COLOR_WHITE);
                            d.add("F_YEARSCH3", singleton(yearMonthfromTo(db2, getString("S_YEAR_MONTH", row), getString("E_YEAR_MONTH", row)))); // 開始年

                            d.addAll("GRPSCH_4", si); // グループ7
                            d.attribute("GRPSCH_4", ATTR_COLOR_WHITE);
                            d.addWithKeta("GRADESCH2", getString("REMARK", row), form); // 訓練内容
                            dg.addFieldData(d);
                            grpCnt++;
                        }
                    } else if (divKoko.equals(recordPart)) {

                        for (int i = 0; i < edu.size(); i++) {
                            final Map row = (Map) edu.get(i);
                            final String si = String.valueOf(grpCnt);

                            final FieldData d = new FieldData();
                            // 教育歴 高校
                            d.addAll("GRPSCH_3", si); // グループ3
                            d.attribute("GRPSCH_3", ATTR_COLOR_WHITE);
                            d.addWithKeta("SCHOOLNAMESCH", getString("FINSCHOOL_NAME", row), form); // 施設名

                            d.addAll("GRPSCH_5", si); // グループ5
                            d.attribute("GRPSCH_5", ATTR_COLOR_WHITE);
                            d.add("F_YEARSCH3", singleton(yearMonthfromTo(db2, getString("S_YEAR_MONTH", row), getString("E_YEAR_MONTH", row)))); // 開始年

                            d.addAll("GRPSCH_4", si); // グループ7
                            d.attribute("GRPSCH_4", ATTR_COLOR_WHITE);
                            d.addWithKeta("GRADESCH2", getString("REMARK", row), form); // 訓練内容
                            dg.addFieldData(d);
                            grpCnt++;
                        }
                    } else if (divDaigaku.equals(recordPart)) {

                        for (int i = 0; i < edu.size(); i++) {
                            final Map row = (Map) edu.get(i);
                            final String si = String.valueOf(grpCnt);

                            final FieldData d = new FieldData();
                            // 教育歴 大学
                            d.addAll("GRPSCH_3", si); // グループ3
                            d.attribute("GRPSCH_3", ATTR_COLOR_WHITE);
                            d.addWithKeta("SCHOOLNAMESCH", getString("SCHOOL_NAME", row), form); // 施設名

                            d.addAll("GRPSCH_5", si); // グループ5
                            d.attribute("GRPSCH_5", ATTR_COLOR_WHITE);
                            d.add("F_YEARSCH3", singleton(yearMonthfromTo(db2, getString("S_YEAR_MONTH", row), getString("E_YEAR_MONTH", row)))); // 開始年

                            d.addAll("GRPSCH_4", si); // グループ7
                            d.attribute("GRPSCH_4", ATTR_COLOR_WHITE);
                            d.addWithKeta("GRADESCH2", getString("REMARK", row), form); // 訓練内容
                            dg.addFieldData(d);
                            grpCnt++;
                        }
                    }
                    grpCnt++;
                }
            }

            final List kyoikuAll = dg.getPrintRecordList(_param);
            setRecordFieldDataAll(kyoikuAll, "GRP1", "FF");
            setRecordFieldDataList(kyoikuAll, "DIV1", extendStringList(kyouikurekiArray, kyoikuAll.size(), true));

            FieldData.svfPrintRecordList(kyoikuAll, form);
        }

        private void printBRecordIryou(final DB2UDB db2, final String schregno, final Form form) {
            final StringBuffer sql31 = new StringBuffer();
            sql31.append("SELECT ");
            sql31.append("   T1.* ");
            sql31.append("   , T3.NAME AS DEPARTMENT_NAME ");
            sql31.append(" FROM SCHREG_CHALLENGED_PROFILE_MEDICAL_RECORD_DAT T1 ");
            sql31.append(" LEFT JOIN MEDICAL_DEPARTMENTS_MST T3 ON T3.NAMECD = T1.NAMECD");
            sql31.append(" WHERE ");
            sql31.append("   T1.SCHREGNO = '" + schregno + "' ");
            sql31.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_PROFILE_MEDICAL_RECORD_DAT WHERE SCHREGNO = '" + schregno + "' ) ");
            sql31.append(" ORDER BY T1.RECORD_DIV, T1.RECORD_SEQ ");
            final List med0 = getRowList(db2, sql31.toString());

            // 医療
            useD = D7;
            final FieldDataGroup dmed = new FieldDataGroup();
            dmed.addAll("GRP7_1", "IR"); // グループ1
            dmed.attribute("GRP7_1", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ3
            dmed.addCenter("DIVIDE7_1", singleton("医療")); // 区分1
            final List medRecordDivList = asList(new String[] {"header", "1", "2", "3"});
            for (int k = 0; k < medRecordDivList.size(); k++) {
                final String recordDiv = (String) medRecordDivList.get(k);
                if ("header".equals(recordDiv)) {

                }

                final List med1 = withDummy(filterList(med0, createCondMap(new String[] {"RECORD_DIV", recordDiv})));
                final FieldDataGroup dmed1 = new FieldDataGroup();
                List div2name = Collections.EMPTY_LIST;
                if ("header".equals(recordDiv)) {
                    div2name = singleton("種類");

                    final String si = String.valueOf(k) + String.valueOf("0");
                    final FieldData d = new FieldData();
                    d.addAll("GRP7_2", recordDiv); // グループ2
                    d.attribute("GRP7_2", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ3

                    d.addAll("GRP7_3", si); // グループ3
                    d.attribute("GRP7_3", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ3
                    d.addWithKeta("DIVIDE7_3", "診療科", form); // 区分3
                    d.attribute("DIVIDE7_3", ATTR_CENTERING);
                    d.addWithKeta("FACILITY7", "医療機関名", form); // 施設名
                    d.attribute("FACILITY7", ATTR_CENTERING);
                    d.addAll("GRP7_4", si); // グループ4
                    d.attribute("GRP7_4", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ3
                    d.addAll("GRP7_7", si); // グループ7
                    d.attribute("GRP7_7", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ3
                    d.addWithKeta("REMARK4", "担当医師・通院の状況・既往歴等", form); // 備考
                    d.attribute("REMARK4", ATTR_CENTERING);
                    dmed.addFieldData(d);

                    continue;
                } else if ("1".equals(recordDiv)) {
                    div2name = singleton("主治医");
                } else if ("2".equals(recordDiv)) {
                    div2name = !form._fieldInfoMap.containsKey("DIVIDE7_2") || form.fieldKeta("DIVIDE7_2") < 0 ? null : getTokenList("かかりつけ医", form.fieldKeta("DIVIDE7_2"));
                } else if ("3".equals(recordDiv))  {
                    div2name = !form._fieldInfoMap.containsKey("DIVIDE7_2") || form.fieldKeta("DIVIDE7_2") < 0 ? null : getTokenList("入院・既往歴", form.fieldKeta("DIVIDE7_2"));
                }
                if (med1.size() > 0) {
                    final Map rowFst = (Map) med1.get(0);
                    if (rowFst.size() > 0) {
                        dmed1.addAll("GRP7_2", recordDiv); // グループ2
                        dmed1.attribute("GRP7_2", ATTR_COLOR_WHITE);
                        if (div2name != null) {
                            dmed1.addCenter("DIVIDE7_2", div2name); // 区分2
                        }
                        for (int i = 0; i < med1.size(); i++) {
                            final Map row = (Map) med1.get(i);
                            final String si = String.valueOf(k) + String.valueOf(i);
                            final FieldData d = new FieldData();
                            d.addAll("GRP7_3", si); // グループ3
                            d.attribute("GRP7_3", ATTR_COLOR_WHITE);
                            d.addWithKeta("DIVIDE7_3", getString("DEPARTMENT_NAME", row), form); // 区分3
                            d.addWithKeta("FACILITY7", getString("CENTER_NAME", row), form); // 施設名
                            d.addAll("GRP7_4", si); // グループ4
                            d.attribute("GRP7_4", ATTR_COLOR_WHITE);
                            d.addAll("GRP7_7", si); // グループ7
                            d.attribute("GRP7_7", ATTR_COLOR_WHITE);
                            d.addWithKeta("REMARK4", getString("ATTEND_STATUS", row), form); // 備考
                            dmed1.addFieldData(d);
                        }
                        dmed.addFieldData(dmed1);
                    }
                }
            }
            FieldData.svfPrintRecordList(dmed.getPrintRecordList(_param), form);
        }

        private void printBTraining(final DB2UDB db2, final String schregno, final Form form) {
            final StringBuffer sql2 = new StringBuffer();
            sql2.append("SELECT ");
            sql2.append("   T1.REMARK1 ");
            sql2.append(" FROM SCHREG_CHALLENGED_PROFILE_OTHER_RECORD_DAT T1 ");
            sql2.append(" WHERE ");
            sql2.append("   T1.SCHREGNO = '" + schregno + "' ");
            sql2.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_PROFILE_OTHER_RECORD_DAT WHERE SCHREGNO = '" + schregno + "' AND T1.RECORD_DIV = '002' AND T1.RECORD_SEQ = '1') ");
            sql2.append("   AND T1.RECORD_DIV = '002' ");
            sql2.append("   AND T1.RECORD_SEQ = '1' ");
            final FieldDataGroup dTraining = new FieldDataGroup();
            dTraining.addAll("GRPBIKO_1", "--"); // グループ1
            dTraining.attribute("DIVIDEBIKO_1", ATTR_PAINT_GRAY_FILL); // グループ3
            dTraining.addCenter("DIVIDEBIKO_1", singleton("訓練機関")); // 区分1
            dTraining.addAll("GRPBIKO_3", "--"); // グループ2
            dTraining.addAll("GRPBIKO_4", "--"); // グループ2
            dTraining.addAll("GRPBIKO_5", "--"); // グループ2
            dTraining.addAll("GRPBIKO_6", "--"); // グループ2

            try {
                final String prtStr = getOne(db2, sql2.toString());  //1行取得
                final String[] cutStr = KNJ_EditEdit.get_token(prtStr, 100, 4);  //formのレコードMAXではなく、入力MAXで改行想定。
                if (cutStr != null) {
                    for (int cnt = 0;cnt < cutStr.length;cnt++) {
                        //1行は出力しないといけないので注意。
                        if (cnt > 0 && "".equals(StringUtils.defaultString(cutStr[cnt], ""))) continue;
                        if (!"".equals(StringUtils.defaultString(cutStr[cnt], ""))) {
                            final FieldData d = new FieldData();
                            d.addAll("BIKO", cutStr[cnt]); // 区分1
                            dTraining.addFieldData(d);
                        }
                        //form.VrEndRecord();
                    }
                } else {
                    final FieldData d = new FieldData();
                    d.addAll("BIKO", ""); // 区分1
                      dTraining.addFieldData(d);
                }
                FieldData.svfPrintRecordList(dTraining.getPrintRecordList(_param), form);
            } catch  (SQLException ex) {
                log.warn("printBSeiiku SQL Error!", ex);
            }
        }

        private void printBRecordKenkouKanri(final DB2UDB db2, final String schregno, final Form form) {
            final StringBuffer sql4 = new StringBuffer();
            sql4.append("SELECT ");
            sql4.append("   T1.* ");
            sql4.append("   , T2.NAME AS MEDICAL_CARE_NAME ");
            sql4.append(" FROM SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT T1 ");
            sql4.append(" LEFT JOIN MEDICAL_CARE_NAME_MST T2 ON T2.NAMECD = T1.MEDICAL_NAMECD ");
            sql4.append(" WHERE ");
            sql4.append("   T1.SCHREGNO = '" + schregno + "' ");
            sql4.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT WHERE SCHREGNO = '" + schregno + "' AND T1.RECORD_DIV = '1' ) ");
            sql4.append("   AND T1.RECORD_DIV = '1' ");
            sql4.append(" ORDER BY T1.RECORD_SEQ ");
            final List kenkou = withDummy(getRowList(db2, sql4.toString()));

            final String kenkoukanri = "健康管理";

            // 健康管理 服薬状況
            useD = D8;
            final String odiv = "O";
            final FieldDataGroup dFukuyaku = new FieldDataGroup();
            dFukuyaku.addAll("GRP8" + odiv + "_1", "KK"); // グループ1
            dFukuyaku.attribute("GRP8" + odiv + "_1", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ1
            dFukuyaku.addCenter("DIVIDE8" + odiv + "_1", singleton(kenkoukanri)); // 区分1
            dFukuyaku.addAll("GRP8" + odiv + "_2", "01"); // グループ2
            dFukuyaku.attribute("GRP8" + odiv + "_2", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ2
            dFukuyaku.addCenter("DIVIDE8" + odiv + "_2", singleton("服薬")); // 区分2
            for (int i = 0; i < kenkou.size() + 1; i++) {  //0は固定処理。1以降からデータ取得。
                final Map row = i == 0 ? null : (Map)kenkou.get(i-1);
                final String si = String.valueOf(i);
                final FieldData d = new FieldData();
                d.addAll("GRP8" + odiv + "_3", si); // グループ3
                d.addAll("GRP8" + odiv + "_4", si); // グループ4
                if (i == 0) {
                    d.addAll("MEDICINE" + odiv, "薬品名"); // 薬品名・病名
                    d.attribute("MEDICINE" + odiv, ATTR_PAINT_GRAY_FILL + "," + ATTR_CENTERING); // + "," + ATTR_COLOR_GRAY); // グループ3
                } else {
                    d.addWithKeta("MEDICINE" + odiv, getString("MEDICINE_NAME", row), form); // 飲ませ方・配慮事項
                }
                d.addAll("GRP8" + odiv + "_5", si);   // グループ5
                d.addAll("GRP8" + odiv + "_5_2", si); // グループ5
                if (i == 0) {
                    d.addAll("HOWTO_NAME" + odiv, "病名・症状"); // 服用状況留意点
                    d.attribute("HOWTO_NAME" + odiv, ATTR_PAINT_GRAY_FILL + "," + ATTR_CENTERING);
                } else {
                    d.addWithKeta("HOWTO_NAME" + odiv, getString("DISEASE_CONDITION_NAME", row), form); // 飲ませ方・配慮事項
                }
                d.addAll("GRP8" + odiv + "_6_1", si); // グループ6
                d.addAll("GRP8" + odiv + "_6_2", si); // グループ6
                if (i == 0) {
                    d.addAll("HOWTO" + odiv, "服用状況・留意点"); // 飲ませ方・配慮事項
                    d.attribute("HOWTO" + odiv, ATTR_PAINT_GRAY_FILL + "," + ATTR_CENTERING);
                } else {
                    d.addWithKeta("HOWTO" + odiv, getString("CARE_WAY", row), form); // 飲ませ方・配慮事項
                }
                dFukuyaku.addFieldData(d);
            }
            dFukuyaku.setFieldDiv("GRP1", "GRP8" + odiv + "_1");
            dFukuyaku.setFieldDiv("DIV1", "DIVIDE8" + odiv + "_1");
            dFukuyaku.setFieldDiv("GRP2", "GRP8" + odiv + "_2");
            dFukuyaku.setFieldDiv("DIV2", "DIVIDE8" + odiv + "_2");

            final StringBuffer sql5 = new StringBuffer();
            sql5.append("SELECT ");
            sql5.append("   T1.* ");
            sql5.append("   , T2.NAME AS MEDICAL_CARE_NAME ");
            sql5.append(" FROM SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT T1 ");
            sql5.append(" LEFT JOIN MEDICAL_CARE_NAME_MST T2 ON T2.NAMECD = T1.MEDICAL_NAMECD ");
            sql5.append(" WHERE ");
            sql5.append("   T1.SCHREGNO = '" + schregno + "' ");
            sql5.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT WHERE SCHREGNO = '" + schregno + "' AND T1.RECORD_DIV = '2' ) ");
            sql5.append("   AND T1.RECORD_DIV = '2' ");
            sql4.append(" ORDER BY T1.RECORD_SEQ ");
            final List iryoCareDataList = getRowList(db2, sql5.toString());

            // 健康管理 医療的ケア
            useD = DCARE;
            final FieldDataGroup dIryoCare = new FieldDataGroup();
            dIryoCare.addAll("GRPCARE" + odiv + "_1", "KK"); // グループ1
            dIryoCare.attribute("GRPCARE" + odiv + "_1", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ3
            dIryoCare.addCenter("DIVIDECARE" + odiv + "_1", singleton(kenkoukanri)); // 区分1
            dIryoCare.addAll("GRPCARE" + odiv + "_2", "02"); // グループ2
            dIryoCare.attribute("GRPCARE" + odiv + "_2", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
            dIryoCare.addCenterWithKeta("DIVIDECARE" + odiv + "_2", "医療的ケア", form); // 区分2

            // 医療的ケア
            final String[] iryoDivs = {"学校", "家庭", "事業所"};
            final String[] iryoDivFields = {"SCHOOL_CARE", "HOUSE_CARE", "CENTER_CARE"};
            final int iryoCareMaxCol = 2;
            final FieldData head = new FieldData();
            dIryoCare.addFieldData(head);
            for (int ci = 0; ci < iryoCareMaxCol; ci++) {
                final String scol = String.valueOf(ci + 1);
                head.addAll("GRPCARE" + odiv + "_S" + scol, "H" + String.valueOf(ci)); // グループ
                head.attribute("GRPCARE" + odiv + "_S" + scol, ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                head.addWithKeta("SERVICECARE" + odiv + "1_" + scol, "ケアの種類", form); // グループ
                head.attribute("SERVICECARE" + odiv + "1_" + scol, ATTR_CENTERING);
                for (int i = 0; i < iryoDivs.length; i++) {
                    head.addAll("GRPCARE" + odiv + "_D" + String.valueOf(i + 1) + "_" + scol, "H" + String.valueOf(i)); // グループ
                    head.attribute("GRPCARE" + odiv + "_D" + String.valueOf(i + 1) + "_" + scol, ATTR_PAINT_GRAY_FILL); // グループ
                    if (i == 1) {
                        head.add("DIVIDECARE" + odiv + "_DIV" + String.valueOf(i + 1) + "_" + scol + "_2", singleton(iryoDivs[i]));
                        head.add("DIVIDECARE" + odiv + "_DIV" + String.valueOf(i + 1) + "_" + scol + "_3", singleton("病院"));
                    } else {
                        head.add("DIVIDECARE" + odiv + "_DIV" + String.valueOf(i + 1) + "_" + scol + (KNJ_EditEdit.getMS932ByteLength(iryoDivs[i]) > 4 ? "_2" : ""), singleton(iryoDivs[i]));
                    }
                    head.attribute("DIVIDECARE" + odiv + "_DIV" + String.valueOf(i + 1) + "_" + scol + (KNJ_EditEdit.getMS932ByteLength(iryoDivs[i]) > 4 ? "_2" : ""), ATTR_PAINT_GRAY_FILL); // グループ
                }
            }

            final String checkMark = "〇";
            for (int gyoi = 0; gyoi < iryoCareDataList.size() / iryoCareMaxCol + (iryoCareDataList.size() % iryoCareMaxCol == 0 ? 0 : 1); gyoi++) {
                final FieldData d = new FieldData();
                dIryoCare.addFieldData(d);
                for (int ci = 0; ci < Math.min(iryoCareMaxCol, iryoCareDataList.size() - gyoi * iryoCareMaxCol); ci++) {
                    final String scol = String.valueOf(ci + 1);
                    final Map row = (Map) iryoCareDataList.get(gyoi * iryoCareMaxCol + ci);
                    d.addAll("GRPCARE" + odiv + "_S" + scol, String.valueOf(gyoi) + String.valueOf(ci)); // グループ
                    d.attribute("GRPCARE" + odiv + "_S" + scol, ATTR_COLOR_WHITE); // グループ
                    d.addWithKeta("SERVICECARE" + odiv + "1_" + scol, getString("MEDICAL_CARE_NAME", row), form);
                    for (int i = 0; i < iryoDivs.length; i++) {
                        final String div = String.valueOf(i + 1);
                        d.addAll("GRPCARE" + odiv + "_D" + div + "S" + scol, String.valueOf(gyoi) + String.valueOf(i)); // グループ
                        d.attribute("GRPCARE" + odiv + "_D" + div + "S" + scol, ATTR_COLOR_WHITE); // グループ
                        d.add("DIVIDECARE" + odiv + "_DIV" + div + "_" + scol, singleton("1".equals(getString(iryoDivFields[i], row)) ? checkMark : ""));
                    }
                }
            }

            dIryoCare.setFieldDiv("GRP1", "GRPCARE" + odiv + "_1");
            dIryoCare.setFieldDiv("DIV1", "DIVIDECARE" + odiv + "_1");
            dIryoCare.setFieldDiv("GRP2", "GRPCARE" + odiv + "_2");
            dIryoCare.setFieldDiv("DIV2", "DIVIDECARE" + odiv + "_2");

            final StringBuffer sql6 = new StringBuffer();
            sql6.append("SELECT ");
            sql6.append("   T1.* ");
            sql6.append("   , T2.NAME AS MEDICAL_CARE_NAME ");
            sql6.append(" FROM SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT T1 ");
            sql6.append(" LEFT JOIN MEDICAL_CARE_NAME_MST T2 ON T2.NAMECD = T1.MEDICAL_NAMECD ");
            sql6.append(" WHERE ");
            sql6.append("   T1.SCHREGNO = '" + schregno + "' ");
            sql6.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT WHERE SCHREGNO = '" + schregno + "' AND RECORD_DIV = '3' AND RECORD_SEQ = 1) ");
            sql6.append("   AND T1.RECORD_DIV = '3' ");
            sql6.append("   AND T1.RECORD_SEQ = 1 ");
            final Map arerugiRow = getFirstRow(getRowList(db2, sql6.toString()));

            // 健康管理 食物アレルギー
            final FieldDataGroup dgArerugi = new FieldDataGroup();
            dgArerugi.addAll("GRP2_TRANS", "02"); // グループ2
            dgArerugi.attribute("GRP2_TRANS", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
            dgArerugi.addCenter("DIV2_TRANS", getTokenList("食物アレルギー", form.fieldKeta("DIVIDEARRE_2"))); // 区分2

            for (int i = 0; i < 2; i++) {
                final FieldData d = new FieldData();
                final String si = String.valueOf(i);
                if (i == 0) {
                    d.setOutputTranslate("GRP1_TRANS", "GRPARRE_1");
                    d.setOutputTranslate("DIV1_TRANS", "DIVIDEARRE_1");
                    d.setOutputTranslate("GRP2_TRANS", "GRPARRE_2");
                    d.setOutputTranslate("DIV2_TRANS", "DIVIDEARRE_2");

                    d.attribute("GRPARRE_2", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                    d.addAll("GRPARRE_3", si); // グループ3
                    d.attribute("GRPARRE_3", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                    d.add("DIVIDEARRE_3", singleton("除去する食品")); // 区分3
                    d.attribute("DIVIDEARRE_3", ATTR_PAINT_GRAY_FILL); // グループ
                    d.addWithKeta("REMARKARRE", getString("ALLERGIA_FOOD_CAT", arerugiRow), form);

                } else if (i == 1) {
                    d.setOutputTranslate("GRP1_TRANS", "GRPARREC_1");
                    d.setOutputTranslate("DIV1_TRANS", "DIVIDEARREC_1");
                    d.setOutputTranslate("GRP2_TRANS", "GRPARREC_2");
                    d.setOutputTranslate("DIV2_TRANS", "DIVIDEARREC_2");

                    d.attribute("GRPARREC_2", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                    d.addAll("GRPARREC_3", si); // グループ3
                    d.attribute("GRPARREC_3", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                    d.add("DIVIDEARREC_3", singleton("取組プラン")); // 区分3
                    d.addAll("GRPARREC_4", si); // グループ4
                    d.add("DIVIDEARREC_4", singleton("1".equals(getString("ALLERGIA_PLAN", arerugiRow)) ? "有" : "無")); // 区分3
                    d.addAll("GRPARREC_5", si); // グループ4
                    d.attribute("GRPARREC_5", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                    d.add("DIVIDEARREC_5", singleton("特記事項")); // 区分3
                    d.addWithKeta("REMARKARREC", getString("ALLERGIA_SPECIAL_REPORT", arerugiRow), form);
                    d.addAll("GRPARREC_7", si); // グループ7
                }

                dgArerugi.setFieldDiv("GRPARRE_1", "GRP1");
                dgArerugi.setFieldDiv("DIVIDEARRE_1", "DIV1");
                dgArerugi.setFieldDiv("GRPARRE_2", "GRP2");
                dgArerugi.setFieldDiv("DIVIDEARRE_2", "DIV2");
                dgArerugi.setFieldDiv("GRPARREC_1", "GRP1");
                dgArerugi.setFieldDiv("DIVIDEARREC_1", "DIV1");
                dgArerugi.setFieldDiv("GRPARREC_2", "GRP2");
                dgArerugi.setFieldDiv("DIVIDEARREC_2", "DIV2");

                dgArerugi.addFieldData(d);
            }

            // 健康管理 その他のアレルギー
            final FieldDataGroup dgSonotanoArerugi = new FieldDataGroup();
            dgSonotanoArerugi.addAll("GRPSHOKU_1", "SH2"); // グループ1
            dgSonotanoArerugi.attribute("GRPSHOKU_1", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
            dgSonotanoArerugi.addCenter("DIVIDESHOKU_1", singleton(kenkoukanri)); // 区分1
            dgSonotanoArerugi.addAll("GRPSHOKU_2", "B02"); // グループ2
            dgSonotanoArerugi.addAll("GRPSHOKU_2_2", "B02"); // グループ2
            dgSonotanoArerugi.addAll("GRPSHOKU_2_3", "B02"); // グループ2
            dgSonotanoArerugi.attribute("GRPSHOKU_2", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
            dgSonotanoArerugi.addCenter("DIVIDESHOKU_2", singleton("その他のアレルギー")); // 区分2
            dgSonotanoArerugi.addAll("GRPSHOKU2_2", "SH3"); // グループ1

            final String siArerugi = String.valueOf(4) + String.valueOf(1);
            dgSonotanoArerugi.addAll("GRPSHOKU_7", siArerugi); // グループ7
            dgSonotanoArerugi.addAll("GRPSHOKU_7_2", siArerugi); // グループ7
            dgSonotanoArerugi.addAll("GRPSHOKU_7_3", siArerugi); // グループ7
            dgSonotanoArerugi.addAll("GRPSHOKU_7_4", siArerugi); // グループ7
            dgSonotanoArerugi.addWithKeta("REMARKSHOKU", getString("ALLERGIA_REMARK", arerugiRow), form); // 備考

            dgSonotanoArerugi.setFieldDiv("GRP1", "GRPSHOKU_1");
            dgSonotanoArerugi.setFieldDiv("DIV1", "DIVIDESHOKU_1");
            dgSonotanoArerugi.setFieldDiv("GRP2", "GRPSHOKU_2");
            dgSonotanoArerugi.setFieldDiv("DIV2", "DIVIDESHOKU_2");


            //発作(RECORDDIV='4', SEQ=1。その他アレルギーと同じ取得条件。)
            final String hdiv = "C2";
            final FieldDataGroup dgHossa = new FieldDataGroup();
            if ("1".equals(_param._aoutputA2)) {
                final StringBuffer sql7 = new StringBuffer();
                sql7.append("SELECT ");
                sql7.append("   T1.* ");
                sql7.append("   , T2.NAME AS MEDICAL_CARE_NAME ");
                sql7.append(" FROM SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT T1 ");
                sql7.append(" LEFT JOIN MEDICAL_CARE_NAME_MST T2 ON T2.NAMECD = T1.MEDICAL_NAMECD ");
                sql7.append(" WHERE ");
                sql7.append("   T1.SCHREGNO = '" + schregno + "' ");
                sql7.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT WHERE SCHREGNO = '" + schregno + "' AND RECORD_DIV = '4') ");
                sql7.append("   AND T1.RECORD_DIV = '4' ");
                final List hossaRow = getRowList(db2, sql7.toString());

                ///////////DIVIDEARREC2_1,DIVIDEARREC2_2,DIVIDEARREC2_3
                dgHossa.addAll("GRPARRE" + hdiv + "_1", "SH"); // グループ1
                dgHossa.attribute("GRPARRE" + hdiv + "_1", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ1
                dgHossa.addCenter("DIVIDEARRE" + hdiv + "_1", singleton("")); // 区分1
                dgHossa.addAll("GRPARRE" + hdiv + "_2", "01"); // グループ2
                dgHossa.attribute("GRPARRE" + hdiv + "_2", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ2
                dgHossa.addCenter("DIVIDEARRE" + hdiv + "_2", singleton("発作")); // 区分2
                dgHossa.addAll("GRPARRE" + hdiv + "_3", "01"); // グループ2
                //dgHossa.attribute("GRPARRE" + hdiv + "_3", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ3
                dgHossa.addCenter("DIVIDEARRE" + hdiv + "_3", hossaRow.size() == 0 ? singleton("無") : singleton("有")); // 区分3
                for (Iterator iteh = hossaRow.iterator();iteh.hasNext();) {
                    final Map hossaMap = (Map)iteh.next();
                    final FieldData d = new FieldData();
                    final String[] kaigyoWk = KNJ_EditEdit.get_token(StringUtils.defaultString(getString("REMARK3", hossaMap), ""), 58, 4);
                    int ngyoCnt = 0;
                    if (kaigyoWk != null && kaigyoWk.length > 0) {
                        for (int cnt = 0;cnt < kaigyoWk.length;cnt++) {
                            if (kaigyoWk[cnt] != null) {
                                ngyoCnt++;
                            }
                        }
                    }
                    if (ngyoCnt == 0) {
                        ngyoCnt++;  //最低1行は出力
                    }
                    List seqList = seq(0, 2);  //0,1のみリスト登録し、2は同じ値を配列数(改行分)分登録
                    List ttlList = asList(new String[] {"頻度", "症状"});  //発作時の対応はループ内で中央位置を判定して登録
                    List datList = asList(new String[] {StringUtils.defaultString(getString("REMARK1", hossaMap), ""), StringUtils.defaultString(getString("REMARK2", hossaMap), "")});  //配列数(改行分)分ループ内で登録
                    for (int addCnt = 0; addCnt < ngyoCnt;addCnt++) {
                        seqList.add("2");
                        if ((ngyoCnt <= 2 && addCnt == 0) || (ngyoCnt > 2 && addCnt == 1)) {
                            ttlList.add("発作時の対応");
                        } else {
                            ttlList.add("");
                        }
                        if (kaigyoWk != null && kaigyoWk.length > addCnt) {
                            datList.add(kaigyoWk[addCnt]);
                        } else if (ngyoCnt == 0) {
                            datList.add("");
                        }
                    }
                    d.add("GRPARRE" + hdiv + "_4", seqList); // グループ4
                    d.attribute("GRPARRE" + hdiv + "_4", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ4
                    d.add("DIVIDEARRE" + hdiv + "_4", ttlList); // 服用状況留意点
                    d.add("GRPARRE" + hdiv + "_7", seqList); // グループ5
                    d.add("REMARKARRE" + hdiv + "", datList); // 服用状況留意点
                    dgHossa.addFieldData(d);
                }
            }



            final StringBuffer sql7 = new StringBuffer();
            sql7.append("SELECT ");
            sql7.append("   T1.* ");
            sql7.append("   , T2.NAME AS MEDICAL_CARE_NAME ");
            sql7.append(" FROM SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT T1 ");
            sql7.append(" LEFT JOIN MEDICAL_CARE_NAME_MST T2 ON T2.NAMECD = T1.MEDICAL_NAMECD ");
            sql7.append(" WHERE ");
            sql7.append("   T1.SCHREGNO = '" + schregno + "' ");
            sql7.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT WHERE SCHREGNO = '" + schregno + "' AND RECORD_DIV = '5' AND RECORD_SEQ = 1) ");
            sql7.append("   AND T1.RECORD_DIV = '5' ");
            sql7.append("   AND T1.RECORD_SEQ = 1 ");
            final Map hairyoRow = getFirstRow(getRowList(db2, sql7.toString()));

            // 健康管理 配慮事項
            final FieldDataGroup dgHairyojiko = new FieldDataGroup();
            dgHairyojiko.addAll("GRP83_1", "SH"); // グループ1
            dgHairyojiko.attribute("GRP83_1", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
            dgHairyojiko.addCenter("DIVIDE83_1", singleton(kenkoukanri)); // 区分1
            dgHairyojiko.addAll("GRP83_2", "02"); // グループ2
            dgHairyojiko.attribute("GRP83_2", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
            dgHairyojiko.addCenter("DIVIDE83_2", singleton("配慮事項")); // 区分2

            final String si = String.valueOf(4) + String.valueOf(0);
            final FieldData dhairyoJiko = new FieldData();
            dhairyoJiko.addAll("GRP83_3", si); // グループ7
            dhairyoJiko.addAll("GRP83_4_2", si); // グループ7
            dhairyoJiko.addAll("GRP83_4_3", si); // グループ7
            dhairyoJiko.addAll("GRP83_5", si); // グループ7
            dhairyoJiko.addAll("GRP83_5_2", si); // グループ7
            dhairyoJiko.addAll("GRP83_6", si); // グループ7
            dhairyoJiko.addAll("GRP83_7", si); // グループ7
            dhairyoJiko.addWithKeta("DIVIDE83_3", getString("REMARK4", hairyoRow), form); // 配慮事項
            dgHairyojiko.addFieldData(dhairyoJiko);

            dgHairyojiko.setFieldDiv("GRP1", "GRP83_1");
            dgHairyojiko.setFieldDiv("DIV1", "DIVIDE83_1");
            dgHairyojiko.setFieldDiv("GRP2", "GRP83_2");
            dgHairyojiko.setFieldDiv("DIV2", "DIVIDE83_2");

            //健康管理の出力をまとめる
            final List all = new ArrayList();
            final List dFukuyaku1 = dFukuyaku.getPrintRecordList(_param);
            final List dIryo1 = dIryoCare.getPrintRecordList(_param);
            final List dArerugi = dgArerugi.getPrintRecordList(_param);
            final List dSonotanoArerugi = dgSonotanoArerugi.getPrintRecordList(_param);
            final List dHossa = dgHossa.getPrintRecordList(_param);
            final List dHairyo = dgHairyojiko.getPrintRecordList(_param);
            all.addAll(dFukuyaku1);
            all.addAll(dIryo1);
            all.addAll(dArerugi);
            all.addAll(dSonotanoArerugi);
            if ("1".equals(_param._aoutputA2)) {
                all.addAll(dHossa);
            }
            all.addAll(dHairyo);

            if (all.size() > 0) {
                final Map last = (Map) all.get(all.size() - 1);
                Map changeFieldMap = null;
                if (dFukuyaku1.contains(last)) {
                    changeFieldMap = new HashMap();
                    changeFieldMap.put("GRP8" + odiv + "_1", "GRP8" + odiv + "C_1");
                    changeFieldMap.put("DIVIDE8" + odiv + "_1", "DIVIDE8" + odiv + "C_1");
                    changeFieldMap.put("GRP8" + odiv + "_2", "GRP8" + odiv + "C_2");
                    changeFieldMap.put("DIVIDE8" + odiv + "_2", "DIVIDE8" + odiv + "C_2");
                    changeFieldMap.put("GRP8" + odiv + "_3", "GRP8" + odiv + "C_3");
                    changeFieldMap.put("DIVIDE8" + odiv + "_3", "DIVIDE8" + odiv + "C_3");
                    changeFieldMap.put("GRP8" + odiv + "_4", "GRP8" + odiv + "C_4");
                    changeFieldMap.put("MEDICINE" + odiv + "", "MEDICINE" + odiv + "C");
                    changeFieldMap.put("GRP8" + odiv + "_5", "GRP8" + odiv + "C_5");
                    changeFieldMap.put("HOWTO_NAME" + odiv + "", "HOWTO_NAME" + odiv + "C");
                    changeFieldMap.put("GRP8" + odiv + "_6_1", "GRP8" + odiv + "C_6_1");
                    changeFieldMap.put("GRP8" + odiv + "_6_2", "GRP8" + odiv + "C_6_2");
                    changeFieldMap.put("HOWTO" + odiv + "", "HOWTO" + odiv + "C");

                } else if (dIryo1.contains(last)) {
                    changeFieldMap = new HashMap();
                    changeFieldMap.put("GRP9" + odiv + "_1", "GRP9" + odiv + "C_1");
                    changeFieldMap.put("DIVIDE9" + odiv + "_1", "DIVIDE9" + odiv + "C_1");
                    changeFieldMap.put("GRP9" + odiv + "_2", "GRP9" + odiv + "C_2");
                    changeFieldMap.put("DIVIDE9" + odiv + "_2", "DIVIDE9" + odiv + "C_2");
                    changeFieldMap.put("GRP9" + odiv + "_3", "GRP9" + odiv + "C_3");
                    changeFieldMap.put("DIVIDE9" + odiv + "_3", "DIVIDE9" + odiv + "C_3");
                    changeFieldMap.put("GRP9" + odiv + "_4_1", "GRP9" + odiv + "C_4_1");
                    changeFieldMap.put("GRP9" + odiv + "_4_2", "GRP9" + odiv + "C_4_2");
                    changeFieldMap.put("GRP9" + odiv + "_4_3", "GRP9" + odiv + "C_4_3");
                    changeFieldMap.put("SERVICE" + odiv + "", "SERVICE" + odiv + "C");
                }
                if (null != changeFieldMap) {
                    FieldData.changeField(last, changeFieldMap);
                }
            }


            List rtn = extendStringList(singleton(kenkoukanri), all.size(), true);

            // 9行のとき健康管理が表示されないため
            if(all.size() == 9) {
                for(int i = 0; i < all.size(); i++) {
                    rtn.set(i, null);
                }
                rtn.set(3, kenkoukanri);
            }
            setRecordFieldDataList(all, "DIV1", rtn);
            FieldData.svfPrintRecordList(all, form);
        }
        private void printBEyes(final DB2UDB db2, final String schregno, final Form form) {
            if (!"1".equals(_param._aoutputA3) && !"1".equals(_param._aoutputA4)) {
                return;
            }
            final StringBuffer sql6 = new StringBuffer();
            sql6.append(" SELECT ");
            sql6.append("     T1.R_BAREVISION, ");
            sql6.append("     T1.L_BAREVISION, ");
            sql6.append("     T1.R_VISION, ");
            sql6.append("     T1.L_VISION, ");
            sql6.append("     T1.R_EAR_DB, ");
            sql6.append("     T1.L_EAR_DB, ");
            sql6.append("     T2.DATE, ");
            sql6.append("     T3.DET_REMARK1 AS DBLEYE_BARE, ");
            sql6.append("     T3.DET_REMARK2 AS DBLEYE_VISION, ");
            sql6.append("     T3.DET_REMARK3 AS DBLEAR_DB, ");
            sql6.append("     T3.DET_REMARK4, ");
            sql6.append("     T3.DET_REMARK5, ");
            sql6.append("     T3.DET_REMARK6, ");
            sql6.append("     T3.DET_REMARK7 ");
            sql6.append(" FROM ");
            sql6.append("     MEDEXAM_DET_DAT T1 ");
            sql6.append("     LEFT JOIN MEDEXAM_HDAT T2 ");
            sql6.append("       ON T2.YEAR = T1.YEAR ");
            sql6.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
            sql6.append("     LEFT JOIN MEDEXAM_DET_DETAIL_DAT T3 ");
            sql6.append("       ON T3.YEAR = T1.YEAR ");
            sql6.append("      AND T3.SCHREGNO = T1.SCHREGNO ");
            sql6.append("      AND T3.DET_SEQ = '020' ");
            sql6.append(" WHERE ");
            sql6.append("     T1.YEAR   = '" + _param._year + "' ");
            sql6.append("     AND T1.SCHREGNO   = '" + schregno + "' ");

            final FieldDataGroup fgd1 = new FieldDataGroup();
            fgd1.addAll("GRPEYE_1", "EYE" + "H1"); // グループ1
            fgd1.attribute("GRPEYE_1", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
            fgd1.addAll("GRPEYE_3", "EYE" + "H2"); // グループ1
            fgd1.attribute("GRPEYE_3", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
            fgd1.addAll("GRPEYE_4", "EYE" + "H3"); // グループ1
            fgd1.attribute("GRPEYE_4", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
            fgd1.addAll("GRPEYE_5", "EYE" + "H4"); // グループ1
            fgd1.attribute("GRPEYE_5", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
            fgd1.addAll("GRPEYE_6", "EYE" + "H5"); // グループ1
            fgd1.attribute("GRPEYE_6", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ

            final FieldDataGroup fgd1_2 = new FieldDataGroup();
            fgd1_2.addAll("GRPEYE2_1", "EYE" + "H1"); // グループ1
            fgd1_2.attribute("GRPEYE2_1", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
            fgd1_2.addAll("GRPEYE2_3", "EYE" + "H2"); // グループ1
            fgd1_2.attribute("GRPEYE2_3", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
            fgd1_2.addAll("GRPEYE2_6", "EYE" + "H3"); // グループ1
            fgd1_2.attribute("GRPEYE2_6", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ

            final List eye_ear = getRowList(db2, sql6.toString());
            Map row = null;
            for (Iterator ite = eye_ear.iterator();ite.hasNext();) {
                row = (Map)ite.next();
                break;  //1件しかないので、1件取れれば良い。
            }
            if ("1".equals(_param._aoutputA3)) {
                final FieldData d1 = new FieldData();
                final FieldData d2 = new FieldData();
                d1.add("DIVIDEEYE_1", asList(new String[] {"視力"}));
                d1.add("DIVIDEEYE_2", asList(new String[] {"右(裸眼)"}));
                if (row != null) {
                    final String rEyeBStrOrg = (String)row.get("R_BAREVISION");  //右(裸眼)
                    final String rEyeBStr = rEyeBStrOrg == null ? "" : rEyeBStrOrg;
                    d1.add("EYE2", asList(new String[] {rEyeBStr}));
                }

                d1.add("DIVIDEEYE_3", asList(new String[] {"左(裸眼)"}));
                if (row != null) {
                    final String lEyeBStrOrg = (String)row.get("L_BAREVISION");  //左(裸眼)
                    final String lEyeBStr = lEyeBStrOrg == null ? "" : lEyeBStrOrg;
                    d1.add("EYE3", asList(new String[] {lEyeBStr}));
                }

                d1.add("DIVIDEEYE_4", asList(new String[] {"右(矯正)"}));
                if (row != null) {
                    final String rEyeStrOrg = (String)row.get("R_VISION");  //右(矯正)
                    final String rEyeStr = rEyeStrOrg == null ? "" : rEyeStrOrg;
                    d1.add("DIVIDEEYE_4", asList(new String[] {"右(矯正)"}));
                    d1.add("EYE4", asList(new String[] {rEyeStr}));
                }

                d1.add("DIVIDEEYE_5", asList(new String[] {"左(矯正)"}));
                if (row != null) {
                    final String lEyeStrOrg = (String)row.get("L_VISION");  //左(矯正)
                    final String lEyeStr = lEyeStrOrg == null ? "" : lEyeStrOrg;
                    d1.add("EYE5", asList(new String[] {lEyeStr}));
                }

                d2.add("DIVIDEEYE2_2", asList(new String[] {"両目(裸眼)"}));
                if (row != null) {
                    final String o1 = (String)row.get("DBLEYE_BARE");  //両目(裸眼)
                    final String dblEyeBStr = o1 == null ? "" : o1;
                    d2.add("EYE2_1", asList(new String[] {dblEyeBStr}));
                }

                d2.add("DIVIDEEYE2_3", asList(new String[] {"両目(矯正)"}));
                if (row != null) {
                    final String o2 = (String)row.get("DBLEYE_VISION");  //両目(矯正)
                    final String dblEyeStr = o2 == null ? "" : o2;
                    d2.add("EYE2_2", asList(new String[] {dblEyeStr}));
                }
                fgd1.addFieldData(d1);
                FieldData.svfPrintRecordList(fgd1.getPrintRecordList(_param), form);
                fgd1_2.addFieldData(d2);
                FieldData.svfPrintRecordList(fgd1_2.getPrintRecordList(_param), form);
            }

            if ("1".equals(_param._aoutputA4)) {
                final FieldDataGroup fgd2 = new FieldDataGroup();
                fgd2.addAll("GRPEAR_1", "H1"); // グループ1
                fgd2.attribute("GRPEAR_1", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                fgd2.addAll("GRPEAR_3", "H2"); // グループ1
                fgd2.attribute("GRPEAR_3", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                fgd2.addAll("GRPEAR_4", "H3"); // グループ1
                fgd2.addAll("GRPEAR_5", "H4"); // グループ1
                fgd2.attribute("GRPEAR_5", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                fgd2.addAll("GRPEAR_6", "H5"); // グループ1
                fgd2.attribute("GRPEAR_6", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ

                final FieldDataGroup fgd2_2 = new FieldDataGroup();
                fgd2_2.addAll("GRPEAR2_1", "H1"); // グループ1
                fgd2_2.attribute("GRPEAR2_1", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                fgd2_2.addAll("GRPEAR2_3", "H2"); // グループ1
                fgd2_2.attribute("GRPEAR2_3", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                fgd2_2.addAll("GRPEAR2_4", "H3"); // グループ1
                fgd2_2.addAll("GRPEAR2_5", "H4"); // グループ1
                fgd2_2.attribute("GRPEAR2_5", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                fgd2_2.addAll("GRPEAR2_6", "H5"); // グループ1

                final FieldDataGroup fgd2_3 = new FieldDataGroup();
                fgd2_3.addAll("GRPEAR3_1", "H1"); // グループ1
                fgd2_3.attribute("GRPEAR3_1", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                fgd2_3.addAll("GRPEAR3_3", "X2"); // グループ1
                fgd2_3.attribute("GRPEAR3_3", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                fgd2_3.addAll("GRPEAR3_4", "X3"); // グループ1
                fgd2_3.addAll("GRPEAR3_5", "X4"); // グループ1
                fgd2_3.addAll("GRPEAR3_6", "X5"); // グループ1

                final FieldDataGroup fgd2_4 = new FieldDataGroup();
                fgd2_4.addAll("GRPEAR3_1", "H1"); // グループ1
                fgd2_4.attribute("GRPEAR3_1", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                fgd2_4.addAll("GRPEAR3_3", "Y2"); // グループ1
                fgd2_4.attribute("GRPEAR3_3", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                fgd2_4.addAll("GRPEAR3_4", "Y3"); // グループ1
                fgd2_4.addAll("GRPEAR3_5", "Y4"); // グループ1
                fgd2_4.addAll("GRPEAR3_6", "Y5"); // グループ1

                final FieldData d2 = new FieldData();

                d2.add("DIVIDEEAR_2", asList(new String[] {"右"}));
                if (row != null) {
                    final String rEarBStrOrg = (String)row.get("R_EAR_DB");
                    final String rEarBStr = rEarBStrOrg == null ? "" : rEarBStrOrg + "dB";
                    d2.add("EAR1", asList(new String[] {rEarBStr}));
                }
                d2.add("DIVIDEEAR_3", asList(new String[] {"左"}));
                if (row != null) {
                    final String lEarBStrOrg = (String)row.get("L_EAR_DB");
                    final String lEarBStr = lEarBStrOrg == null ? "" : lEarBStrOrg + "dB";
                    d2.add("EAR2", asList(new String[] {lEarBStr}));
                }
                d2.add("DIVIDEEAR_4", asList(new String[] {"両耳"}));
                if (row != null) {
                    final String dblEarBStrOrg = (String)row.get("DBLEAR_DB");
                    final String dblEarBStr = dblEarBStrOrg == null ? "" : dblEarBStrOrg + "dB";
                    d2.add("EAR4", asList(new String[] {dblEarBStr}));
                }

                final FieldData d2_2 = new FieldData();
                d2_2.add("DIVIDEEAR2_1", asList(new String[] {"聴力"}));
                d2_2.add("DIVIDEEAR2_2", asList(new String[] {"閾値(補聴器装用時)"}));
                if (row != null) {
                    final String sikii1 = (String)row.get("DET_REMARK4");
                    final String sikii1_Str = sikii1 == null ? "" : sikii1 + "dB";
                    d2_2.add("EAR2_1", asList(new String[] {sikii1_Str}));
                }
                d2_2.add("DIVIDEEAR2_3", asList(new String[] {"閾値(人工内耳)"}));
                if (row != null) {
                    final String sikii2 = (String)row.get("DET_REMARK5");
                    final String sikii2_Str = sikii2 == null ? "" : sikii2 + "dB";
                    d2_2.add("EAR2_2", asList(new String[] {sikii2_Str}));
                }

                final FieldData d2_3 = new FieldData();
                d2_3.add("DIVIDEEAR3_2", asList(new String[] {"メーカー・機種・シリアル番号"}));
                if (row != null) {
                    final String mts = (String)row.get("DET_REMARK6");
                    final List mtsLst = mts == null ? new ArrayList() : KNJ_EditKinsoku.getTokenList(mts, 64, 2);
                    d2_3.add("EAR3_1", mtsLst);
                }

                final FieldData d2_4 = new FieldData();
                d2_4.add("DIVIDEEAR3_2", asList(new String[] {"主なコミュニケーション手段"}));
                if (row != null) {
                    final String coms = (String)row.get("DET_REMARK7");
                    final String coms_Str = coms == null ? "" : coms;
                    d2_4.add("EAR3_1", asList(new String[] {coms_Str}));
                }

                fgd2.addFieldData(d2);
                FieldData.svfPrintRecordList(fgd2.getPrintRecordList(_param), form);
                fgd2_2.addFieldData(d2_2);
                FieldData.svfPrintRecordList(fgd2_2.getPrintRecordList(_param), form);
                fgd2_3.addFieldData(d2_3);
                FieldData.svfPrintRecordList(fgd2_3.getPrintRecordList(_param), form);
                fgd2_4.addFieldData(d2_4);
                FieldData.svfPrintRecordList(fgd2_4.getPrintRecordList(_param), form);
            }
        }

        public static String mkString(final List list, final String comma) {
            final StringBuffer stb = new StringBuffer();
            String comma0 = "";
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final String s = (String) it.next();
                if (null == s || s.length() == 0) {
                    continue;
                }
                stb.append(comma0).append(s);
                comma0 = comma;
            }
            return stb.toString();
        }

        private void printBRecordFukushi(final DB2UDB db2, final String schregno, final Form form) {
            final StringBuffer sql6 = new StringBuffer();
            sql6.append("SELECT ");
            sql6.append("   T1.* ");
            sql6.append("   , T4.NAME AS WELFARE_ADVICE_CENTER_NAME ");
            sql6.append(" FROM SCHREG_CHALLENGED_PROFILE_WELFARE_RECORD_DAT T1 ");
            sql6.append(" LEFT JOIN WELFARE_ADVICE_CENTER_MST T4 ON T4.CENTERCD = T1.CENTERCD ");
            sql6.append(" WHERE ");
            sql6.append("   T1.SCHREGNO = '" + schregno + "' ");
            sql6.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_PROFILE_WELFARE_RECORD_DAT WHERE SCHREGNO = '" + schregno + "') ");
            sql6.append(" ORDER BY T1.RECORD_DIV, T1.RECORD_NO, T1.RECORD_SEQ ");

            final StringBuffer sql6_2 = new StringBuffer();
            sql6_2.append("SELECT ");
            sql6_2.append("   T1.* ");
            sql6_2.append(" FROM SCHREG_CHALLENGED_PROFILE_WELFARE_RECORD_ITEMCD_DAT T1 ");
            sql6_2.append(" WHERE ");
            sql6_2.append("   T1.SCHREGNO = '" + schregno + "' ");
            sql6_2.append("   AND T1.RECORD_DATE = ( SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_PROFILE_WELFARE_RECORD_ITEMCD_DAT WHERE SCHREGNO = '" + schregno + "' ) ");
            sql6_2.append(" ORDER BY T1.RECORD_DIV, T1.RECORD_NO, T1.RECORD_SEQ, ITEMDIV ");

            final Map prostheticsNameMst = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD, NAME FROM MEDICAL_PROSTHETICS_NAME_MST "), "NAMECD", "NAME");
            final Map medicalDailywantsNameMst = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD, NAME FROM MEDICAL_DAILYWANTS_NAME_MST "), "NAMECD", "NAME");
            final Map nameMstE039Div1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'E039' AND NAMESPARE1 = '1' "), "NAMECD2", "NAME1");
            final Map nameMstE039Div2Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'E039' AND NAMESPARE1 = '2' "), "NAMECD2", "NAME1");

            final String grp1Fukushi = "福祉";
            final String[] grp2Hosougu = {"補装具等の", "給付"};
            final String grp2SoudanShienJigyousho = "相談支援事業所";
            final String[] grp2JigyoushoCerviceNoRiyou = {"事業所サー", "ビスの利用"};

            final List fukushiRecordDivList = asList(new String[] {"1", "2", "3"});
            final List fukushi0 = getRowList(db2, sql6.toString());
            final List hosougu = getRowList(db2, sql6_2.toString());
            //log.info(" hosougu = " + hosougu);
            final Map recordDivFieldDataGroupListMap = new TreeMap();
            final String odiv = "O";
            final String odiv2 = "O2";
            final String odiv3 = "O3";
            boolean bodiv3strtFlg = false;
            for (int k = 0; k < fukushiRecordDivList.size(); k++) {
                final String recordDiv = (String) fukushiRecordDivList.get(k);
                final List fukushi = withDummy(filterList(fukushi0, createCondMap(new String[] {"RECORD_DIV", recordDiv})));
                if ("1".equals(recordDiv)) {
                    final FieldDataGroup gda = new FieldDataGroup();
                    final FieldDataGroup gdb = new FieldDataGroup();
                    useD = D8;
                    gda.addAll("GRP82" + (odiv2) + "_1", "S" + "H"); // グループ1
                    gda.attribute("GRP82" + (odiv2) + "_1", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                    gda.addCenter("DIVIDE82" + (odiv2) + "_1", singleton(grp1Fukushi)); // 区分1
                    gda.addAll("GRP82" + (odiv2) + "_2", recordDiv + "H"); // グループ2
                    gda.attribute("GRP82" + (odiv2) + "_2", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                    gda.addCenter("DIVIDE82" + (odiv2) + "_2", asList(grp2Hosougu)); // 区分2
                    gdb.addAll("GRP82" + (odiv) + "_1", "S" + "H"); // グループ1
                    gdb.attribute("GRP82" + (odiv) + "_1", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                    gdb.addCenter("DIVIDE82" + (odiv) + "_1", singleton(grp1Fukushi)); // 区分1
                    gdb.addAll("GRP82" + (odiv) + "_2", recordDiv + "H"); // グループ2
                    gdb.attribute("GRP82" + (odiv) + "_2", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                    gdb.addCenter("DIVIDE82" + (odiv) + "_2", singleton("")); // 区分2

                    //final List fukushiRecordNoList = asList(new String[] {"1", "2", "3"});
                    final List fukushiRecordNoList = asList(new String[] {"2", "3"});
                    for (int l = 0; l < fukushiRecordNoList.size(); l++) {
                        final String recordNo = (String) fukushiRecordNoList.get(l);
                        final List fukushi1 = withDummy(filterList(fukushi, createCondMap(new String[] {"RECORD_NO", recordNo})));
                        if ("1".equals(recordNo)) {
                        } else if ("2".equals(recordNo)) {
                            // 福祉　支援内容 補装具
                            final FieldDataGroup gd2 = new FieldDataGroup();
                            gda.addFieldData(gd2);

                            gd2.setFieldDiv("GRP1", "GRP82" + odiv2 + "_1");
                            gd2.setFieldDiv("DIV1", "DIVIDE82" + odiv2 + "_1");
                            gd2.setFieldDiv("GRP2", "GRP82" + odiv2 + "_2");
                            gd2.setFieldDiv("DIV2", "DIVIDE82" + odiv2 + "_2");

                            gd2.addAll("GRP82" + odiv2 + "_3", recordNo); // グループ3
                            gd2.attribute("GRP82" + odiv2 + "_3", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                            gd2.addCenter("DIVIDE82" + odiv2 + "_3", singleton("補装具")); // 区分3

                            final String siw = String.valueOf(recordNo + -1);
                            final FieldData d1 = new FieldData();
                            d1.addAll("GRP82" + odiv2 + "_4", siw); // グループ4
                            d1.add("REMARK82" + odiv2 + "", asList(new String[] {"種類"})); // 項目名称
                            d1.attribute("GRP82" + odiv2 + "_6", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                            d1.attribute("GRP82" + odiv2 + "_4", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                            d1.add("DIVIDE82" + odiv2 + "_4", asList(new String[] {"申請年月"})); // 項目名称
                            d1.attribute("GRP82" + odiv2 + "_5", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                            d1.add("DIVIDE82" + odiv2 + "_5", asList(new String[] {"製作会社"})); // 項目名称
                            gd2.addFieldData(d1);
                            for (int i = 0; i < fukushi1.size(); i++) {
                                final Map row = (Map) fukushi1.get(i);
                                final String recordSeq = getString("RECORD_SEQ", row);

                                final List hosougu1 = withDummy(filterList(hosougu, createCondMap(new String[] {"RECORD_DIV", recordDiv, "RECORD_NO", recordNo, "RECORD_SEQ", recordSeq})));
                                //log.info(" hosougu1 (" + recordNo + ") = " + hosougu1);
                                final List nameList = new ArrayList();
                                for (final Iterator it = hosougu1.iterator(); it.hasNext();) {
                                    final Map hrow = (Map) it.next();
                                    for (int j = 1; j <= 10; j++) {
                                        final String name = (String) prostheticsNameMst.get(getString("ITEMCD" + String.valueOf(j), hrow));
                                        if (null != name) {
                                            nameList.add(name);
                                        }
                                    }
                                }

                                final String name = mkString(nameList, " ");
                                if (null == name) {
                                    continue;
                                }
                                final String si = recordNo + String.valueOf(i);
                                final FieldData d = new FieldData();
                                d.addAll("GRP82" + odiv2 + "_4", si); // グループ4
                                d.addAll("GRP82" + odiv2 + "_4_2", si); // グループ4
                                d.addAll("GRP82" + odiv2 + "_4_3", si); // グループ4
                                d.addAll("GRP82" + odiv2 + "_5", si); // グループ4
                                d.addAll("GRP82" + odiv2 + "_6", si); // グループ4
                                d.addWithKeta("REMARK82" + odiv2 + "", name, form); // 補装具
                                final String ymOrg = StringUtils.defaultString(getString("SUPPLY_DATE", row), "");
                                final String corpOrg = StringUtils.defaultString(getString("SERVICE_CENTER_TEXT", row), "");
                                d.add("DIVIDE82" + odiv2 + "_4", asList(new String[] {ymOrg})); // 製造年月
                                final int colen = KNJ_EditEdit.getMS932ByteLength(corpOrg);
                                final String coField = colen > 10 ? "_2" : "";
                                d.add("DIVIDE82" + odiv2 + "_5" + coField, asList(new String[] {corpOrg})); // 製作会社
                                gd2.addFieldData(d);
                            }

                        } else if ("3".equals(recordNo)) {
                            // 福祉　支援内容 日常生活用具
                            final FieldDataGroup gd2 = new FieldDataGroup();
                            gdb.addFieldData(gd2);

                            gd2.setFieldDiv("GRP1", "GRP82" + odiv + "_1");
                            gd2.setFieldDiv("DIV1", "DIVIDE82" + odiv + "_1");
                            gd2.setFieldDiv("GRP2", "GRP82" + odiv + "_2");
                            gd2.setFieldDiv("DIV2", "DIVIDE82" + odiv + "_2");

                            gd2.addAll("GRP82" + odiv + "_3", recordNo); // グループ3
                            gd2.attribute("GRP82" + odiv + "_3", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                            gd2.addCenter("DIVIDE82" + odiv + "_3", singleton("日常生活用具")); // 区分3

                            final String siw = String.valueOf(recordNo + -1);
                            final FieldData d2 = new FieldData();
                            d2.addAll("GRP82" + odiv + "_4", siw); // グループ4
                            d2.add("REMARK82" + odiv + "", asList(new String[] {"種類"})); // 項目名称
                            d2.attribute("GRP82" + odiv + "_6", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                            d2.attribute("GRP82" + odiv + "_4", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                            d2.attribute("GRP82" + odiv + "_5", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                            d2.add("REMARK82" + odiv + "_2", asList(new String[] {"備考"})); // 項目名称
                            gd2.addFieldData(d2);
                            for (int i = 0; i < fukushi1.size(); i++) {
                                final Map row = (Map) fukushi1.get(i);
                                final String recordSeq = getString("RECORD_SEQ", row);

                                final List hosougu1 = withDummy(filterList(hosougu, createCondMap(new String[] {"RECORD_DIV", recordDiv, "RECORD_NO", recordNo, "RECORD_SEQ", recordSeq})));
                                //log.info(" hosougu1 (" + recordNo + ") = " + hosougu1);
                                final List nameList = new ArrayList();
                                for (final Iterator it = hosougu1.iterator(); it.hasNext();) {
                                    final Map nrow = (Map) it.next();
                                    for (int j = 1; j <= 10; j++) {
                                        final String name = (String) medicalDailywantsNameMst.get(getString("ITEMCD" + String.valueOf(j), nrow));
                                        if (null != name) {
                                            nameList.add(name);
                                        }
                                    }
                                }

                                final String name = mkString(nameList, " ");
                                if (null == name) {
                                    continue;
                                }
                                final String si = recordNo + String.valueOf(i);
                                final FieldData d = new FieldData();
                                d.addAll("GRP82" + odiv + "_4", si); // グループ4
                                d.addWithKeta("REMARK82" + odiv + "", name, form); //日常生活用品(種類)
                                d.addWithKeta("REMARK82" + odiv + "_2", getString("WELFARE_REMARK", row), form); //日常生活用品(備考)
                                d.addAll("GRP82" + odiv + "_5", si); // グループ4
                                gd2.addFieldData(d);
                            }
                        }

                    }
                    gda.setFieldDiv("GRP1", "GRP82" + odiv2 + "_1");
                    gda.setFieldDiv("DIV1", "DIVIDE82" + odiv2 + "_1");
                    gda.setFieldDiv("GRP2", "GRP82" + odiv2 + "_2");
                    gda.setFieldDiv("DIV2", "DIVIDE82" + odiv2 + "_2");
                    gdb.setFieldDiv("GRP1", "GRP82" + odiv + "_1");
                    gdb.setFieldDiv("DIV1", "DIVIDE82" + odiv + "_1");
                    gdb.setFieldDiv("GRP2", "GRP82" + odiv + "_2");
                    gdb.setFieldDiv("DIV2", "DIVIDE82" + odiv + "_2");
                    final FieldDataGroup gd = new FieldDataGroup();
                    if ("1".equals(_param._aoutputA5)) {
                        gd.addFieldData(gda);
                        gd.addFieldData(gdb);
                    }
                    getMappedList(recordDivFieldDataGroupListMap, recordDiv).add(gd);

                } else if ("2".equals(recordDiv)) {
                    // 相談支援事業所
                    final FieldDataGroup gd = new FieldDataGroup();
                    useD = D10;
                    gd.addAll("GRPFKSSDNSEN_1", "SH"); // グループ1
                    gd.attribute("GRPFKSSDNSEN_1", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                    gd.addCenter("DIVIDEFKSSDNSEN_1", singleton(grp1Fukushi)); // 区分1
                    gd.addAll("GRPFKSSDNSEN_2", "02"); // グループ2
                    gd.attribute("GRPFKSSDNSEN_2", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                    gd.addAll("GRPFKSSDNSEN_2_2", "02"); // グループ2
                    gd.attribute("GRPFKSSDNSEN_2_2", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                    gd.addCenter("DIVIDEFKSSDNSEN_2", singleton(grp2SoudanShienJigyousho)); // 区分2


                    for (int i = 0; i < fukushi.size(); i++) {
                        final Map row = (Map) fukushi.get(i);
                        final String si = recordDiv + String.valueOf(i);

                        final FieldData dJigyousho = new FieldData();
                        dJigyousho.addAll("GRPFKSSDNSEN_7", si); // グループ7
                        dJigyousho.addAll("GRPFKSSDNSEN_7_2", si); // グループ7
                        dJigyousho.addWithKeta("REMARKFKSSDNSEN", getString("SERVICE_CENTER_TEXT", row), form); // 事業所

                        dJigyousho.addAll("GRPFKSSDNSEN_3", si); // グループ7
                        dJigyousho.addAll("GRPFKSSDNSEN_4", si); // グループ7
                        dJigyousho.attribute("GRPFKSSDNSEN_3", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                        dJigyousho.addCenter("STAFFFKSSDNSEN", singleton("担当者")); // グループ7

                        dJigyousho.addWithKeta("STAFFNAMEFKSSDNSEN", getString("SERVICE_CHARGE", row), form); // 備考

                        gd.addFieldData(dJigyousho);
                    }

                    gd.setFieldDiv("GRP1", "GRPFKSSDNSEN_1");
                    gd.setFieldDiv("DIV1", "DIVIDEFKSSDNSEN_1");
                    gd.setFieldDiv("GRP2", "GRPFKSSDNSEN_2");
                    gd.setFieldDiv("DIV2", "DIVIDEFKSSDNSEN_2");

                    getMappedList(recordDivFieldDataGroupListMap, recordDiv).add(gd);
                } else if ("3".equals(recordDiv)) {
                    // 事業所サービスの利用
                    final FieldDataGroup gd = new FieldDataGroup();
                    log.info(" " + fukushi);
                    useD = D8;
                    gd.addAll("GRP82" + odiv3 + "_1", "S" + "H"); // グループ1
                    gd.attribute("GRP82" + odiv3 + "_1", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                    gd.addCenter("DIVIDE82" + odiv3 + "_1", singleton(grp1Fukushi)); // 区分1
                    gd.addAll("GRP82" + odiv3 + "_2", recordDiv + "H"); // グループ2
                    gd.attribute("GRP82" + odiv3 + "_2", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                    gd.addCenter("DIVIDE82" + odiv3 + "_2", asList(grp2JigyoushoCerviceNoRiyou)); // 区分2
                    for (int i = 0; i < fukushi.size(); i++) {
                        final Map row = (Map) fukushi.get(i);

                        if (!bodiv3strtFlg) {
                            final String siw = "1" + String.valueOf(-1);
                            final FieldData d3 = new FieldData();
                            d3.add("DIVIDE82" + odiv3 + "_3", asList(new String[] {"事業所"})); // 項目名称
                            d3.addAll("GRP82" + odiv3 + "_3", siw); // グループ4
                            d3.attribute("GRP82" + odiv3 + "_3", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                            d3.addAll("GRP82" + odiv3 + "_4", siw); // グループ4
                            d3.attribute("GRP82" + odiv3 + "_4", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                            d3.attribute("GRP82" + odiv3 + "_4_2", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                            d3.attribute("GRP82" + odiv3 + "_4_3", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                            d3.add("DIVIDE82" + odiv3 + "_4", asList(new String[] {"サービス内容"})); // 項目名称
                            d3.attribute("GRP82" + odiv3 + "_5", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                            d3.addAll("GRP82" + odiv3 + "_5_2", siw); // グループ4
                            d3.add("DIVIDE82" + odiv3 + "_5", asList(new String[] {"支給量"})); // 項目名称
                            d3.attribute("GRP82" + odiv3 + "_6", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ
                            d3.add("DIVIDE82" + odiv3 + "_6", asList(new String[] {"頻度"})); // 項目名称
                            d3.addAll("GRP82" + odiv3 + "_6", siw); // グループ4
                            gd.addFieldData(d3);
                            bodiv3strtFlg = true;
                        }
                        final String recordSeq = getString("RECORD_SEQ", row);

                            final FieldData d = new FieldData();

                            final StringBuffer text = new StringBuffer();
                            final Map item1Map = getFirstRow(filterList(hosougu, createCondMap(new String[] {"RECORD_DIV", recordDiv, "RECORD_NO", "1", "RECORD_SEQ", recordSeq, "ITEMDIV", "1"})));
                            for (int cd = 1; cd <= 3; cd++) {
                                final String itemcd1 = KnjDbUtils.getString(item1Map, "ITEMCD" + String.valueOf(cd));
                                final String itemname = KnjDbUtils.getString(nameMstE039Div1Map, itemcd1);
                                if (!StringUtils.isBlank(itemname)) {
                                    if (!StringUtils.isBlank(text.toString())) {
                                        text.append(" ");
                                    }
                                    text.append(itemname);
                                }
                            }
                            final Map item2Map = getFirstRow(filterList(hosougu, createCondMap(new String[] {"RECORD_DIV", recordDiv, "RECORD_NO", "1", "RECORD_SEQ", recordSeq, "ITEMDIV", "2"})));
                            for (int cd = 1; cd <= 3; cd++) {
                                final String itemcd2 = KnjDbUtils.getString(item2Map, "ITEMCD" + String.valueOf(cd));
                                final String itemname = KnjDbUtils.getString(nameMstE039Div2Map, itemcd2);
                                if (!StringUtils.isBlank(itemname)) {
                                    if (!StringUtils.isBlank(text.toString())) {
                                        text.append(" ");
                                    }
                                    text.append(itemname);
                                }
                            }

                            d.addAll("GRP82" + odiv3 + "_3", String.valueOf(i)); // グループ3
                            d.addAll("GRP82" + odiv3 + "_4", String.valueOf(i)); // グループ4
                            d.addAll("GRP82" + odiv3 + "_4_2", String.valueOf(i)); // グループ4
                            d.addAll("GRP82" + odiv3 + "_4_3", String.valueOf(i)); // グループ4
                            d.addAll("GRP82" + odiv3 + "_5", String.valueOf(i)); // グループ5
                            d.addAll("GRP82" + odiv3 + "_5_2", String.valueOf(i)); // グループ5
                            d.addAll("GRP82" + odiv3 + "_6", String.valueOf(i)); // グループ5
                            final String svtxt = StringUtils.defaultString(getString("SERVICE_CENTER_TEXT", row), "");
                            final String suppId = StringUtils.defaultString(getString("ITEMCD2", row), "");
                            final String supplyTerm = "2".equals(suppId) ? "(時間/月)" : "(日/月)";
                            final String corpOrg = StringUtils.defaultString(getString("ITEMCD", row), "");
                            final String remOrg = StringUtils.defaultString(getString("WELFARE_REMARK", row), "");
                            d.addWithKeta("DIVIDE82" + odiv3 + "_3", svtxt.toString(), form); // 事業所サービス(事業所)
                            d.addWithKeta("DIVIDE82" + odiv3 + "_4", text.toString(), form); // 事業所サービス(サービス内容)
                            d.addWithKeta("DIVIDE82" + odiv3 + "_5", corpOrg + supplyTerm, form); // 事業所サービス(支給量)
                            d.addWithKeta("DIVIDE82" + odiv3 + "_6", remOrg, form); // 事業所サービス(頻度)
                            gd.addFieldData(d);
                    }

                    gd.setFieldDiv("GRP1", "GRP82" + odiv3 + "_1");
                    gd.setFieldDiv("DIV1", "DIVIDE82" + odiv3 + "_1");
                    gd.setFieldDiv("GRP2", "GRP82" + odiv3 + "_2");
                    gd.setFieldDiv("DIV2", "DIVIDE82" + odiv3 + "_2");
                    getMappedList(recordDivFieldDataGroupListMap, recordDiv).add(gd);

                }
            }

            final List fukushiPrintRecordList = new ArrayList();
            final List recordDivList = new ArrayList(recordDivFieldDataGroupListMap.keySet());
            for (int i = 0, size = recordDivList.size(); i < size; i++) {
                final String recordDiv = (String) recordDivList.get(i);
                final List fieldDataGroupList = getMappedList(recordDivFieldDataGroupListMap, recordDiv);

                final List printRecordList = new ArrayList();
                for (final Iterator it = fieldDataGroupList.iterator(); it.hasNext();) {
                    final FieldDataGroup gd = (FieldDataGroup) it.next();
                    printRecordList.addAll(gd.getPrintRecordList(_param));
                }
                fukushiPrintRecordList.addAll(printRecordList);

                // 行数によるセンタリング処理
                List divname2 = Collections.EMPTY_LIST;
                if ("1".equals(recordDiv)) {
                    divname2 = asList(grp2Hosougu);
                } else if ("2".equals(recordDiv)) {
                    divname2 = singleton(grp2SoudanShienJigyousho);
                } else if ("3".equals(recordDiv)) {
                    divname2 = asList(grp2JigyoushoCerviceNoRiyou);
                }
                setRecordFieldDataAll(printRecordList, "GRP2", recordDiv);
                setRecordFieldDataList(printRecordList, "DIV2", extendStringList(divname2, printRecordList.size(), true));

            }

            // 行数によるセンタリング処理
            setRecordFieldDataAll(fukushiPrintRecordList, "GRP1", "FF");
            setRecordFieldDataList(fukushiPrintRecordList, "DIV1", extendStringList(singleton(grp1Fukushi), fukushiPrintRecordList.size(), true));

            FieldData.svfPrintRecordList(fukushiPrintRecordList, form);
        }

        private void printBRecordBiko(final DB2UDB db2, final String schregno, final Form form) {

            final Map main = getAmain(db2, schregno);

            final FieldData d = new FieldData();
            d.addAll("GRPBIKO_1", "BK"); // グループ2
            d.attribute("GRPBIKO_1", ATTR_PAINT_GRAY_FILL + "," + ATTR_COLOR_GRAY); // グループ2
            d.addCenter("DIVIDEBIKO_1", singleton("備考")); // グループ3
            d.addAll("GRPBIKO_3", "BK"); // 区分2
            d.addAll("GRPBIKO_4", "BK"); // 区分2
            d.addAll("GRPBIKO_5", "BK"); // 区分2
            d.addAll("GRPBIKO_6", "BK"); // 区分2
            d.add("BIKO", getTokenList(getString("REMARK", main), 100)); // 区分2

            FieldData.svfPrintRecordList(d.getPrintRecordList(), form);
        }
    }

    // 支援計画
    private static class KNJE390MC extends KNJE390M_0 {
        public KNJE390MC(final Param param) {
            super(param);
        }
        public void printMain(final Vrw32alp svf, final DB2UDB db2, final Map student) {
            final String schregno = getString("SCHREGNO", student);

            final Form form = new Form(_param, svf);
            form._form1 = !"".equals(_param._formB) ? _param._formB : "KNJE390M_B_1.frm";
            form._form2 = null;
            form._form2n = 4;
            form._recMax1 = 38;
            form._recMax2 = 43;
            form.setForm(form._form1, 4);
            form._isForm1 = true;
            form._recMax = form._recMax1; // 1ページ目の行数

            final StringBuffer sql1 = new StringBuffer();
            sql1.append("SELECT ");
            sql1.append("   T1.*, ");
            sql1.append("   PROF.CHALLENGED_NAMES ");
            sql1.append(" FROM SCHREG_CHALLENGED_PROFILE_MAIN_DAT PROF ");
            sql1.append(" LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT T1 ON T1.YEAR = '" + _param._year + "' ");
            sql1.append("      AND PROF.SCHREGNO = T1.SCHREGNO ");
            sql1.append("      AND PROF.RECORD_DATE = T1.RECORD_DATE ");
            sql1.append(" LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.REGISTERCD ");
            sql1.append(" WHERE ");
            sql1.append("   PROF.SCHREGNO = '" + schregno + "' ");
            sql1.append("   AND PROF.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_PROFILE_MAIN_DAT WHERE SCHREGNO = '" + schregno + "' ) ");
            final Map main = getFirstRow(getRowList(db2, sql1.toString()));

            form.VrsOut("TITLE", "個別の教育支援計画");
            form.VrsOut("SUBTITLE", "Ｃ　支援内容・計画");

            printStudent(db2, schregno, student, form, main);

            printMain(db2, schregno, student, form, main);

        }

        public void printStudent(final DB2UDB db2, final String schregno, final Map student, final Form form, final Map main) {
            form.VrsOut("SCHOOL_NAME", _param._knjSchoolMst._schoolName1); // 学校名
            form.VrsOut("DEPARTMENT_NAME", StringUtils.defaultString(getString("COURSENAME", student))); // 学科名
            form.VrsOut("GRADE_NAME", getPrintStudentGrHrname(student)); // 学年・組

            final int entryMaxLine = 1;
            final Map updateMap = getUpdateHistMap(db2, "C", _param._year, schregno, entryMaxLine, getString("WRITING_DATE", main));
            if (!FORM_B1.equals(_param._formB)) {
                form.VrsOut("MAKE_DATE", formatDate(db2, getString("CREATE_DATE", updateMap))); // 作成日
                svfVrsOutWithSize(form, "MAKER", new String[] {"1", "2"}, new int[] {10, 16}, getString("CREATE_STAFFNAME", updateMap));  // 作成者
            }

            svfVrsOutWithSize(form, "KANA", new String[] {"2", "3"}, new int[] {30, 40}, getString("NAME_KANA", student)); // 氏名かな
            svfVrsOutWithSize(form, "NAME", new String[] {"2", "3"}, new int[] {26, 48}, getString("NAME", student)); // 氏名

            form.VrsOut("SEX", getString("SEX_NAME", student)); // 性別
            form.VrsOut("BIRTHDAY", formatDate(db2, getString("BIRTHDAY", student))); // 生年月日

            if (FORM_B1.equals(_param._formB)) {
                String[] get_token = KNJ_EditEdit.get_token(getString("CHALLENGED_NAMES", main), 34, 4);
                if (null != get_token) {
                    List wkStr = Arrays.asList(get_token);  //昔の長い入力データが改行されて余計に出ないよう、制限。
                    svfVrListOutn(form, "DIAG_NAME", wkStr); // 障害名診断名
                }
            } else {
                svfVrListOutn(form, "DIAG_NAME", getTokenList(getString("CHALLENGED_NAMES", main), 30)); // 障害名診断名
            }

            form.VrsOut("UPDATE", formatDate(db2, getString("UPDATE_DATE1", updateMap))); // 更新日
            svfVrsOutWithSize(form, "ENTRY_PERSON", new String[] {"1", "2"}, new int[] {10, 16}, getString("UPDATE_STAFFNAME1", updateMap));  // 記入者

            for (int i = 0; i <= 10; i++) {
                form._svf.VrAttribute("HEAD_" + String.valueOf(i), ATTR_PAINT_GRAY_FILL);
            }

            form.VrEndRecord();
        }

        private void printMain(final DB2UDB db2, final String schregno, final Map student, final Form form, final Map main) {
            String statusName = null;
            String status2Name = null;
            String status3Name = null;
            String status4Name = null;
            final TreeMap dataDivMap = new TreeMap();
            for (final Iterator it = KnjDbUtils.query(db2, " SELECT * FROM CHALLENGED_SUPPORTPLAN_STATUS_DAT WHERE YEAR = '" + _param._year + "' ").iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String dataDiv = KnjDbUtils.getString(row, "DATA_DIV");
                if ("0".equals(dataDiv)) {
                    statusName = KnjDbUtils.getString(row, "STATUS_NAME");
                    status2Name = KnjDbUtils.getString(row, "STATUS2_NAME");
                    status3Name = KnjDbUtils.getString(row, "STATUS3_NAME");
                    status4Name = KnjDbUtils.getString(row, "STATUS4_NAME");
                } else {
                    dataDivMap.put(Integer.valueOf(dataDiv), KnjDbUtils.getString(row, "DATA_DIV_NAME"));
                }
            }

            final StringBuffer sql1 = new StringBuffer();
            sql1.append("SELECT ");
            sql1.append("   T1.* ");
            sql1.append(" FROM SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT T1 ");
            sql1.append(" WHERE T1.YEAR = '" + _param._year + "' ");
            sql1.append("   AND T1.SCHREGNO = '" + schregno + "' ");
            sql1.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT WHERE YEAR = '" + _param._year + "' AND SCHREGNO = '" + schregno + "') ");
            final Map status = KnjDbUtils.getKeyMap(getRowList(db2, sql1.toString()), "DATA_DIV");


            form.VrsOut("BLANK_DUMMY", "DUMMY");
            form.VrEndRecord();

            if (FORM_B1.equals(_param._formB)) {
                final String[] ttlArry = {"学校生活", "への期待","や願い"};
                final List ttlList = Arrays.asList(ttlArry);
                final String[] outStr = KNJ_EditEdit.get_token(getString("ONES_HOPE_PRESENT", main), 100, 3);
                int maxLine = Math.max(ttlList.size(), (outStr == null ? 0 : outStr.length));
                for (int cnt=0;cnt < maxLine;cnt++) {
                    if (cnt < ttlList.size()) {
                        form.VrsOutn("R20_HEAD", cnt+1, (String)ttlList.get(cnt));
                    }
                    form.VrAttributen("R20_HEAD", cnt+1, ATTR_PAINT_GRAY_FILL);
                    if (outStr != null && cnt < outStr.length && outStr[cnt] != null) {
                        form.VrsOutn("R20_TEXT", cnt+1, outStr[cnt]);
                    }
                }
                form.VrEndRecord();
            } else {
                form.VrsOut("TITLE_WHOLE_DIV", "0");
                form.VrAttribute("TITLE_WHOLE", ATTR_CENTERING + "," + ATTR_PAINT_GRAY_FILL);
                form.VrsOut("TITLE_WHOLE", "学校生活への期待や願い");
                form.VrEndRecord();

                form.VrsOut("R19_HEAD", "本人");
                form.VrAttribute("R19_HEAD", ATTR_PAINT_GRAY_FILL);
                form.VrsOut("R19_TEXT", getString("ONES_HOPE_PRESENT", main));
                form.VrEndRecord();

                form.VrsOut("R19_HEAD", "保護者");
                form.VrAttribute("R19_HEAD", ATTR_PAINT_GRAY_FILL);
                form.VrsOut("R19_TEXT", getString("GUARDIAN_HOPE_PRESENT", main));
                form.VrEndRecord();

                form.VrsOut("BLANK_DUMMY", "DUMMY");
                form.VrEndRecord();
            }

            form.VrsOut("R19_HEAD", "支援目標");
            form.VrAttribute("R19_HEAD", ATTR_PAINT_GRAY_FILL);
            final List r19TextList;
            if (FORM_B1.equals(_param._formB)) {
                r19TextList = KNJE390M_0.getTokenList(getString("SUPPORT_GOAL", main), form.fieldKeta("R19_TEXT"), 4);
            } else {
                r19TextList = KNJE390M_0.getTokenList(getString("SUPPORT_GOAL", main), form.fieldKeta("R19_TEXT"));
            }
            for (Iterator itR19 = r19TextList.iterator(); itR19.hasNext();) {
                form.VrsOut("R19_HEAD", "支援目標");
                form.VrAttribute("R19_HEAD", ATTR_PAINT_GRAY_FILL);

                form.VrsOut("R19_HEAD_DIV", "1");
                form.VrsOut("R19_TEXT_DIV", "1");

                final String r19Text = (String) itR19.next();
                form.VrsOut("R19_TEXT", r19Text);
                form.VrEndRecord();
            }

            form.VrsOut("BLANK_DUMMY", "DUMMY");
            form.VrEndRecord();

            form.VrsOut("R129_HEAD_DIV", "0"); // グループ3
            form.VrAttribute("R129_HEAD_DIV", ATTR_PAINT_GRAY_FILL); // グループ3
            final String minDataDiv = dataDivMap.isEmpty() ? "" : ((Integer) dataDivMap.firstKey()).toString();
            form.VrsOut("R129_HEAD2_DIV", minDataDiv); // グループ3
            form.VrAttribute("R129_HEAD2_DIV", ATTR_PAINT_GRAY_FILL); // グループ3
            form.VrsOut("R129_TEXT1", statusName);
            form.VrAttribute("R129_TEXT1", ATTR_CENTERING + "," + ATTR_PAINT_GRAY_FILL);
            form.VrsOut("R129_TEXT2", status2Name);
            form.VrAttribute("R129_TEXT2", ATTR_CENTERING + "," + ATTR_PAINT_GRAY_FILL);
            form.VrsOut("R129_TEXT3", status3Name);
            form.VrAttribute("R129_TEXT3", ATTR_CENTERING + "," + ATTR_PAINT_GRAY_FILL);
            form.VrEndRecord();

            final List recordList = new ArrayList();
            for (final Iterator it = dataDivMap.keySet().iterator(); it.hasNext();) {
                final Integer iDataDiv = (Integer) it.next();
                final String dataDivName = (String) dataDivMap.get(iDataDiv);
                final String dataDiv = iDataDiv.toString();

                final Map row = (Map) status.get(dataDiv);

                final FieldData d = new FieldData();
                d.setFieldDiv("R129_HEAD", "R129_HEAD");
                d.setFieldDiv("R129_HEAD_DIV", "R129_HEAD_DIV");
                d.attribute("R129_HEAD", ATTR_PAINT_GRAY_FILL); // グループ3
                d.attribute("R129_HEAD2_DIV", ATTR_PAINT_GRAY_FILL); // グループ3
                d.addAll("R129_HEAD2_DIV", dataDiv); // グループ3
                d.addAll("R129_TEXT1_DIV", dataDiv); // グループ3
                d.addAll("R129_TEXT2_DIV", dataDiv); // グループ3
                d.addAll("R129_TEXT3_DIV", dataDiv); // グループ3
                d.addCenterWithKeta("R129_HEAD2", dataDivName, form); // 区分1
                if (FORM_B1.equals(_param._formB)) {
                    //MAXが15->10行で既存データの長さだと15行超える可能性があるので、制限をかける。
                    d.addWithKeta("R129_TEXT1", getString("STATUS", row), form, 10);
                    d.addWithKeta("R129_TEXT2", getString("STATUS2", row), form, 10);
                    d.addWithKeta("R129_TEXT3", getString("STATUS3", row), form, 10);
                } else {
                    d.addWithKeta("R129_TEXT1", getString("STATUS", row), form);
                    d.addWithKeta("R129_TEXT2", getString("STATUS2", row), form);
                    d.addWithKeta("R129_TEXT3", getString("STATUS3", row), form);
                }
                recordList.addAll(d.getPrintRecordList());
            }

            setRecordFieldDataAll(recordList, "R129_HEAD_DIV", "0");
            setRecordFieldDataList(recordList, "R129_HEAD", extendStringList(getTokenList("具体的な支援", 2), recordList.size(), true));
            FieldData.svfPrintRecordList(recordList, form);

            form.VrsOut("BLANK_DUMMY", "DUMMY");
            form.VrEndRecord();

            form.VrsOut("TITLE_WHOLE_DIV", "1");
            form.VrsOut("TITLE_WHOLE", status4Name);
            form.VrAttribute("TITLE_WHOLE", ATTR_CENTERING + "," + ATTR_PAINT_GRAY_FILL);
            form.VrEndRecord();

            for (final Iterator it = minLength(getTokenList(getString("RECORD", main), 105), 1).iterator(); it.hasNext();) {
                final String token = (String) it.next();
                form.VrsOut("TITLE_WHOLE_DIV", "2");
                form.VrsOut("TITLE_WHOLE", token);
                form.VrEndRecord();
            }
        }
    }

    private static class KNJE390MD extends KNJE390M_0 {
        public KNJE390MD(final Param param) {
            super(param);
        }
        public void printMain(final Vrw32alp svf, final DB2UDB db2, final Map student) {
            final String schregno = getString("SCHREGNO", student);

            final Form form = new Form(_param, svf);
            form._form1 = "KNJE390M_C_1.frm";
            form._form2 = "KNJE390M_C_2.frm";
            form._form2n = 4;
            form._recMax1 = 29;
            form._recMax2 = 43;
            form._isForm1 = true;

            form.setForm(form._form1, 4);

            for (int i = 0; i <= 60; i++) {
                form._svf.VrAttribute("HEAD_" + String.valueOf(i), ATTR_PAINT_GRAY_FILL);
            }

            final StringBuffer sql1 = new StringBuffer();
            sql1.append("SELECT ");
            sql1.append("   T1.* ");
            sql1.append(" FROM SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_MAIN_DAT T1 ");
            sql1.append(" LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.REGISTERCD ");
            sql1.append(" WHERE ");
            sql1.append("   T1.SCHREGNO = '" + schregno + "' ");
            sql1.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_MAIN_DAT WHERE SCHREGNO = '" + schregno + "' ) ");
            final Map main1 = getFirstRow(getRowList(db2, sql1.toString()));

            printStudent(db2, schregno, student, form, main1);

            form._recMax = form._recMax1;

            printDRecordJittai(db2, schregno, form);

            printDRecordKiboNegai(form, main1);

            printDRecordShien(form, main1);

            printDRecordSoudannnoKiroku(db2, schregno, form);

            if (form._isForm1) {
                form.setForm2();
            }

            printDRecordSinroSidouKeikaku(db2, schregno, student, form);

            printDRecordHikitsugi(form, main1);

        }
        public void printStudent(final DB2UDB db2, final String schregno, final Map student, final Form form, final Map main1) {
            form.VrsOut("SCHOOL_NAME", _param._knjSchoolMst._schoolName1); // 学校名
            form.VrsOut("FACULTY_NAME", getString("SCHOOL_KIND_NAME", student) + (StringUtils.isBlank(getString("SCHOOL_KIND_NAME", student)) ? "" : "部")); // 学部名
            form.VrsOut("DEPARTMENT_NAME", StringUtils.defaultString(getString("COURSENAME", student)) + StringUtils.defaultString(getString("MAJORNAME", student))); // 学科名
            form.VrsOut("COURSE_NAME", getString("COURSECODENAME", student)); // コース名
            form.VrsOut("GRADE_NAME", getPrintStudentGrHrname(student)); // 学年・組

            final int entryMaxLine = 4;
            final Map updateMap = getUpdateHistMap(db2, "D", null, schregno, entryMaxLine, getString("WRITING_DATE", main1));
            form.VrsOut("MAKE_DATE", formatDate(db2, getString("CREATE_DATE", updateMap))); // 作成日
            final String csName = getString("CREATE_STAFFNAME", updateMap);
            final int cnlen = KNJ_EditEdit.getMS932ByteLength(csName);
            if (cnlen <= 30) {  //1行出力
                form.VrsOut("MAKER3", csName); //作成者
            } else {
                if (cnlen > 40) {  //上半分で足りない
                    final String[] cutStr = KNJ_EditEdit.get_token(csName, 40, 2);
                    if (cutStr[0] != null) form.VrsOut("MAKER3_2", cutStr[0]); //作成者
                    if (cutStr[1] != null) form.VrsOut("MAKER3_3", cutStr[1]); //作成者
                } else {
                    form.VrsOut("MAKER3_2", csName); //作成者
                }
            }

            form.VrsOutSelect(new String[][] {{"KANA1"}, {"KANA2"}, {"KANA3"}}, getString("NAME_KANA", student)); // 氏名かな
            form.VrsOutSelect(new String[][] {{"NAME1"}, {"NAME2"}, {"NAME3"}}, getString("NAME", student)); // 氏名

            form.VrsOut("SEX", getString("SEX_NAME", student)); // 性別
            form.VrsOut("BIRTHDAY", formatDate(db2, getString("BIRTHDAY", student))); // 生年月日
            form.VrsOut("FIN_SCHOOL_NAME", getString("JUNIOR_FINSCHOOL_NAME", student)); // 出身中学校名

            svfVrListOut(form, "DIAG_NAME", getTokenList(getString("CHALLENGED_NAMES", main1), 30)); // 障害名診断名

            svfVrListOut(form, "HANDI_CONDITION", getTokenList(getString("CHALLENGED_STATUS", main1), 60)); // 障害の実態・特性

            form.VrsOut("ZIP1", StringUtils.isBlank(getString("ZIPCD", student)) ? "" : "〒" + getString("ZIPCD", student)); // 郵便番号
            final int a11 = KNJ_EditEdit.getMS932ByteLength(getString("ADDR1", student));
            final int a12 = KNJ_EditEdit.getMS932ByteLength(getString("ADDR2", student));
            final String a1field = (a11 > 40 || a12 > 40) ? "_2" : "";
            form.VrsOut("ADDR1_1" + a1field, getString("ADDR1", student)); // 生徒住所
            form.VrsOut("ADDR1_2" + a1field, getString("ADDR2", student)); // 生徒住所

            svfVrsOutWithSize(form, "GUARD_KANA", new String[] {"2", "3"}, new int[] {30, 40}, getString("GUARD_KANA", student)); // 氏名かな
            svfVrsOutWithSize(form, "GUARD_NAME", new String[] {"2", "3"}, new int[] {26, 48}, getString("GUARD_NAME", student)); // 保護者氏名
            form.VrsOut("GUARD_RELATION", getString("GUARD_RELATIONSHIP_NAME", student)); // 保護者続柄

            form.VrsOut("ZIP2", StringUtils.isBlank(getString("GUARD_ZIPCD", student)) ? "" : "〒" + getString("GUARD_ZIPCD", student)); // 郵便番号
            final int a21 = KNJ_EditEdit.getMS932ByteLength(getString("GUARD_ADDR1", student));
            String a2field = "";
            if ("1".equals(getString("GUARD_ADDR_FLG", student))) {
                final int a22 = KNJ_EditEdit.getMS932ByteLength(getString("GUARD_ADDR2", student));
                a2field = (a21 > 40 || a22 > 40) ? "_2" : "";
                // svfVrListOutWithStart(form, "ADDR2_2_", 2, getTokenList(getString("GUARD_ADDR2", student), 60)); // 保護者住所
                form.VrsOut("ADDR2_2" + a2field, getString("GUARD_ADDR2", student)); // 保護者住所
            } else {
                a2field = a21 > 40 ? "_2" : "";
            }
            form.VrsOut("ADDR2_1" + a2field, getString("GUARD_ADDR1", student)); // 保護者住所

            form.VrsOut("TELNO", getString("TELNO", student)); // 連絡先
        }

        private void printDRecordJittai(final DB2UDB db2, final String schregno, final Form form) {
            final StringBuffer sql2 = new StringBuffer();
            sql2.append("SELECT ");
            sql2.append("   T1.* ");
            sql2.append(" FROM SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CHECK_RECORD_DAT T1 ");
            sql2.append(" WHERE ");
            sql2.append("   T1.SCHREGNO = '" + schregno + "' ");
            sql2.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CHECK_RECORD_DAT WHERE SCHREGNO = '" + schregno + "') ");
            sql2.append(" ORDER BY T1.RECORD_SEQ ");
            final List kensaList = withDummy(getRowList(db2, sql2.toString()));

            final FieldDataGroup dJittaiKensa = new FieldDataGroup();
            dJittaiKensa.addAll("GRP1_1", "--"); // グループ1
            form._svf.VrAttribute("GRP1_1", ATTR_PAINT_GRAY_FILL);
            dJittaiKensa.addCenter("DIVIDE1_1", singleton("検査")); // 区分1
            dJittaiKensa.addAll("GRP1_2", "//"); // グループ2
            form._svf.VrAttribute("GRP1_2", ATTR_PAINT_GRAY_FILL);
            dJittaiKensa.addCenter("DIVIDE1_2", singleton("検査日")); // 区分2
            for (int i = 0; i < kensaList.size(); i++) {
                // 実態　検査
                final Map row = (Map) kensaList.get(i);
                final String si = String.valueOf(i);

                final FieldData d = new FieldData();
                d.addAll("GRP1_3", si); // グループ3
                d.add("DIVIDE1_3", getTokenList("検査結果\n・所見等", 10)); // 結果
                d.addAll("GRP1_4", si); // グループ4
                d.addCenter("DATE1", singleton(formatDate(db2, getString("CHECK_DATE", row)))); // 日付
                d.addAll("GRP1_5", si); // グループ5
                form._svf.VrAttribute("GRP1_4", ATTR_PAINT_GRAY_FILL);
                d.addAll("GRP1_6", si); // グループ6
                d.add("RESULT", getTokenList(getString("CHECK_REMARK", row), 50)); // 結果
                dJittaiKensa.addFieldData(d);
            }
            FieldData.svfPrintRecordList(dJittaiKensa.getPrintRecordList(_param), form);

            final StringBuffer sql3 = new StringBuffer();
            sql3.append("SELECT ");
            sql3.append("   T1.* ");
            sql3.append(" FROM SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_STATUS_DAT T1 ");
            sql3.append(" WHERE ");
            sql3.append("   T1.SCHREGNO = '" + schregno + "' ");
            sql3.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_STATUS_DAT WHERE SCHREGNO = '" + schregno + "') ");
            sql3.append(" ORDER BY T1.DATA_DIV ");
            final List welfare1List = getRowList(db2, sql3.toString());

            // 実態　基本的生活習慣身辺自立、行動・社会性、言語・コミュニケーション、身体・運動、学習、興味・強み
            final FieldDataGroup dwelfare = new FieldDataGroup();
            dwelfare.addAll("GRP2_1", "--"); // グループ1
            dwelfare.attribute("GRP2_1", ATTR_PAINT_GRAY_FILL);
            dwelfare.addCenter("DIVIDE2_1", singleton("実態")); // 区分1

            for (final Iterator dIt = asList(new String[] {"01", "02", "03", "04", "05", "06"}).iterator(); dIt.hasNext();) {
                final String dataDiv = (String) dIt.next();
                final List welfareList = withDummy(filterList(welfare1List, createCondMap(new String[] {"DATA_DIV", dataDiv})));
                final String[] title = getJittaiTitleArray2(dataDiv);
                for (final Iterator it = welfareList.iterator(); it.hasNext();) {
                    final Map row = (Map) it.next();

                    final FieldData d = new FieldData();

                    d.addAll("GRP2_2_1", dataDiv); // グループ2
                    d.attribute("GRP2_2_1", ATTR_PAINT_GRAY_FILL); // グループ2
                    d.addAll("GRP2_2_2", dataDiv); // グループ2
                    d.addCenter("DIVIDE2_2", asList(title)); // 区分2
                    d.addAll("GRP2_3_1", dataDiv); // グループ3
                    d.addAll("GRP2_3_2", dataDiv); // グループ3
                    d.addAll("GRP2_3_3", dataDiv); // グループ3
                    d.add("CONDITION", getTokenList(getString("STATUS", row), 72)); // 施設名
                    dwelfare.addFieldData(d);
                }
            }

            FieldData.svfPrintRecordList(dwelfare.getPrintRecordList(_param), form);
        }

        private void printDRecordKiboNegai(final Form form, final Map main1) {
            // 希望・願い ヘッダ
            form.VrsOut("GRP3_1", "--"); // グループ1
            form._svf.VrAttribute("GRP3_1", ATTR_PAINT_GRAY_FILL);
            form.VrsOut("DIVIDE3_1", "希望・願い"); // 区分1
            form.VrsOut("GRP3_2", "01"); // グループ2
            form._svf.VrAttribute("GRP3_2", ATTR_PAINT_GRAY_FILL);
            form.VrsOut("DIVIDE3_2", "本人"); // 区分2
            form.VrsOut("GRP3_3", "02"); // グループ3
            form._svf.VrAttribute("GRP3_3", ATTR_PAINT_GRAY_FILL);
            form.VrsOut("DIVIDE3_3", "保護者"); // 区分3

            form.VrEndRecord();

            for (int i = 0; i < 3; i++) {
                final String si = String.valueOf(i);
                String name = "";
                String self = "";
                String guard = "";
                if (i == 0) {
                    name = "現在";
                    self = "ONES_HOPE_PRESENT";
                    guard = "GUARDIAN_HOPE_PRESENT";
                } else if (i == 1) {
                    name = "進路";
                    self = "ONES_HOPE_CAREER";
                    guard = "GUARDIAN_HOPE_CAREER";
                } else if (i == 2) {
                    name = "卒業後";
                    self = "ONES_HOPE_AFTER_GRADUATION";
                    guard = "GUARDIAN_HOPE_AFTER_GRADUATION";
                }
                final FieldData d = new FieldData();
                d.addAll("GRP4_1", si); // グループ1
                d.attribute("GRP4_1", ATTR_PAINT_GRAY_FILL); // グループ1
                d.addCenter("DIVIDE4_1", singleton(name)); // 区分1
                d.addAll("GRP4_2", si); // グループ2
                d.add("SELF", getTokenList(getString(self, main1), 40)); // 本人
                d.addAll("GRP4_3", si); // グループ3
                d.add("GUARD", getTokenList(getString(guard, main1), 50)); // 保護者

                FieldData.svfPrintRecordList(d.getPrintRecordList(), form);
            }
        }

        private void printDRecordShien(final Form form, final Map main1) {
            form.VrsOut("GRP5_1", "--"); // グループ1
            form._svf.VrAttribute("GRP5_1", ATTR_PAINT_GRAY_FILL);
            form.VrsOut("DIVIDE5_1", "スムーズな移行に向けての支援"); // 区分1
            form.VrEndRecord();

            form.VrsOut("GRP6_1", "-0"); // グループ1
            form._svf.VrAttribute("GRP6_1", ATTR_PAINT_GRAY_FILL);
            form.VrsOut("DIVIDE6_1", ""); // 区分1
            form.VrsOut("GRP6_3", "-2"); // グループ3
            form._svf.VrAttribute("GRP6_3", ATTR_PAINT_GRAY_FILL);
            form.VrsOut("DIVIDE6_3", "卒業後の姿・目標"); // 区分3
            form.VrsOut("GRP6_4", "-3"); // グループ4
            form._svf.VrAttribute("GRP6_4", ATTR_PAINT_GRAY_FILL);
            form.VrsOut("DIVIDE6_4", "実態・課題"); // 区分4
            form.VrsOut("GRP6_5", "-4"); // グループ5
            form._svf.VrAttribute("GRP6_5", ATTR_PAINT_GRAY_FILL);
            form.VrsOut("DIVIDE6_5", "短期目標"); // 区分5
            form.VrsOut("GRP6_6", "-5"); // グループ6
            form._svf.VrAttribute("GRP6_6", ATTR_PAINT_GRAY_FILL);
            form.VrsOut("DIVIDE6_6", "具体的な支援・手立て"); // 区分6

            form.VrEndRecord();

            for (int i = 0; i < 4; i++) {
                final String si = String.valueOf(i);
                List div1 = Collections.EMPTY_LIST;
                String hope = "";
                String subject = "";
                String shortHope = "";
                String support = "";
                if (i == 0) {
                    div1 = singleton("医療");
                    hope = getString("MEDICAL_GOAL_AFTER_GRADUATION", main1);
                    subject = getString("MEDICAL_STATUS", main1);
                    shortHope = getString("MEDICAL_SHORT_TERM_GOAL", main1);
                    support = getString("MEDICAL_TANGIBLE_SUPPORT", main1);
                } else if (i == 1) {
                    div1 = singleton("福祉");
                    hope = getString("WELFARE_GOAL_AFTER_GRADUATION", main1);
                    subject = getString("WELFARE_STATUS", main1);
                    shortHope = getString("WELFARE_SHORT_TERM_GOAL", main1);
                    support = getString("WELFARE_TANGIBLE_SUPPORT", main1);
                } else if (i == 2) {
                    div1 = singleton("労働");
                    hope = getString("WORK_GOAL_AFTER_GRADUATION", main1);
                    subject = getString("WORK_STATUS", main1);
                    shortHope = getString("WORK_SHORT_TERM_GOAL", main1);
                    support = getString("WORK_TANGIBLE_SUPPORT", main1);
                } else if (i == 3) {
                    div1 = asList(new String[] {"家庭", "地域", "生活"});
                    hope = getString("COMMU_GOAL_AFTER_GRADUATION", main1);
                    subject = getString("COMMU_STATUS", main1);
                    shortHope = getString("COMMU_SHORT_TERM_GOAL", main1);
                    support = getString("COMMU_TANGIBLE_SUPPORT", main1);
                }
                final FieldData d = new FieldData();
                d.addAll("GRP7_1", si); // グループ1
                form._svf.VrAttribute("GRP7_1", ATTR_PAINT_GRAY_FILL);
                d.addCenter("DIVIDE7_1", div1); // 区分1
                d.addAll("GRP7_3", si); // グループ3
                d.add("GRD_HOPE", getTokenList(hope, 20)); // 卒業後の目標
                d.addAll("GRP7_4", si); // グループ4
                d.add("SUBJECT", getTokenList(subject, 20)); // 実態：課題
                d.addAll("GRP7_5", si); // グループ5
                d.add("SHORT_HOPE", getTokenList(shortHope, 20)); // 短期目標
                d.addAll("GRP7_6", si); // グループ6
                d.add("SUPPORT", getTokenList(support, 20)); // 支援
                FieldData.svfPrintRecordList(d.getPrintRecordList(), form);
            }
        }

        private void printDRecordSoudannnoKiroku(final DB2UDB db2, final String schregno, final Form form) {
            form.VrsOut("GRP8_1", "-0"); // グループ1
            form._svf.VrAttribute("GRP8_1", ATTR_PAINT_GRAY_FILL);
            form.VrsOut("DIVIDE8_1", ""); // 区分1
            form.VrsOut("GRP8_2", "-1"); // グループ2
            form._svf.VrAttribute("GRP8_2", ATTR_PAINT_GRAY_FILL);
            form.VrsOut("DIVIDE8_2", "会議名"); // 区分2
            form.VrsOut("GRP8_3", "-2"); // グループ3
            form._svf.VrAttribute("GRP8_3", ATTR_PAINT_GRAY_FILL);
            form.VrsOut("DIVIDE8_3", "日時"); // 区分3
            form.VrsOut("GRP8_4", "-3"); // グループ4
            form._svf.VrAttribute("GRP8_4", ATTR_PAINT_GRAY_FILL);
            form.VrsOut("DIVIDE8_4", "構成員"); // 区分4
            form.VrsOut("GRP8_5", "-4"); // グループ5
            form._svf.VrAttribute("GRP8_5", ATTR_PAINT_GRAY_FILL);
            form.VrsOut("DIVIDE8_5", "概要"); // 区分5

            form.VrEndRecord();

            final StringBuffer sql4 = new StringBuffer();
            sql4.append("SELECT ");
            sql4.append("   T1.* ");
            sql4.append(" FROM SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CONSULTATION_RECORD_DAT T1 ");
            sql4.append(" WHERE ");
            sql4.append("   T1.SCHREGNO = '" + schregno + "' ");
            sql4.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CONSULTATION_RECORD_DAT WHERE SCHREGNO = '" + schregno + "' ) ");
            sql4.append(" ORDER BY T1.RECORD_SEQ ");
            final List soudanList = getRowList(db2, sql4.toString());

            final FieldDataGroup dSoudan = new FieldDataGroup();
            dSoudan.addAll("GRP9_1", "--"); // グループ1
            dSoudan.addCenter("DIVIDE9_1", asList(new String[] {"相談の", "記録"})); // 区分1
            for (int i = 0; i < soudanList.size(); i++) {
                // 相談の記録
                final Map row = (Map) soudanList.get(i);
                final String si = String.valueOf(i);
                final FieldData d = new FieldData();
                d.addAll("GRP9_2", si); // グループ2
                d.add("MEETING", getTokenList(getString("MEETING_NAME", row), 20)); // 会議名
                d.addAll("GRP9_3", si); // グループ3
                d.add("DATE2", singleton(formatDate(db2, getString("MEETING_DATE", row)))); // 日付
                d.addAll("GRP9_4", si); // グループ4
                d.add("CHARGE", getTokenList(getString("TEAM_MEMBERS", row), 20)); // 担当
                d.addAll("GRP9_5", si); // グループ5
                d.add("OUTLINE", getTokenList(getString("MEETING_SUMMARY", row), 40)); // 概要
                dSoudan.addFieldData(d);
            }
            FieldData.svfPrintRecordList(dSoudan.getPrintRecordList(_param), form);
        }

        private void printDRecordSinroSidouKeikaku(final DB2UDB db2, final String schregno, final Map student, final Form form) {
            // 進路指導計画
            form.VrsOut("GRP21_1", "--"); // グループ1
            form._svf.VrAttribute("GRP21_1", ATTR_PAINT_GRAY_FILL);
            form.VrsOut("DIVIDE21_1", "進路指導計画"); // 区分1
            form.VrEndRecord();

            final StringBuffer sql14 = new StringBuffer();
            sql14.append("SELECT ");
            sql14.append("   T1.* ");
            sql14.append(" FROM SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CAREERGUIDANCE_RECORD_DAT T1 ");
            sql14.append(" WHERE ");
            sql14.append("   T1.SCHREGNO = '" + schregno + "' ");
            sql14.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CAREERGUIDANCE_RECORD_DAT WHERE SCHREGNO = '" + schregno + "') ");
            sql14.append(" ORDER BY T1.RECORD_DIV, T1.RECORD_NO, T1.RECORD_SEQ ");
            final List sidoList0 = getRowList(db2, sql14.toString());

            final List list12345 = new ArrayList();
            final List list6 = new ArrayList();

            for (final Iterator dIt = asList(new String[] {"1", "2", "3", "4", "5", "6"}).iterator(); dIt.hasNext();) {
                final String recordDiv = (String) dIt.next();
                String[] condKeyVal = {};
                if ("1".equals(recordDiv)) {
                    condKeyVal = new String[] {"RECORD_DIV", "1", "RECORD_NO", "1"};
                } else if ("2".equals(recordDiv)) {
                    condKeyVal = new String[] {"RECORD_DIV", "2", "RECORD_NO", "1"};
                } else if ("3".equals(recordDiv)) {
                    condKeyVal = new String[] {"RECORD_DIV", "3", "RECORD_NO", "1"};
                } else if ("4".equals(recordDiv)) {
                    condKeyVal = new String[] {"RECORD_DIV", "4", "RECORD_NO", "1"};
                } else if ("5".equals(recordDiv)) {
                    condKeyVal = new String[] {"RECORD_DIV", "5", "RECORD_NO", "1", "RECORD_SEQ", "1"};
                } else if ("6".equals(recordDiv)) {
                    condKeyVal = new String[] {"RECORD_DIV", "6"};
                }
                final List sidoList = withDummy(filterList(sidoList0, createCondMap(condKeyVal)));

                if ("1".equals(recordDiv)) {
                    final String recordNo = "1";
                    // 相談懇談
                    for (int i = 0; i < sidoList.size(); i++) {
                        // 相談の記録
                        final Map row = (Map) sidoList.get(i);
                        final String si = String.valueOf(i);

                        for (int j = 0; j < 2; j++) {
                            final String sj = String.valueOf(j);
                            if (j == 0) {
                                final FieldData d = new FieldData();
                                // 相談懇談　期日、構成員
                                d.addAll("GRP29_1", "--"); // グループ1
                                d.attribute("GRP29_1", ATTR_PAINT_GRAY_FILL);
                                d.addAll("DIVIDE29_1", ""); // 区分1
                                d.addAll("GRP29_2", recordDiv + recordNo); // グループ2
                                d.attribute("GRP29_2", ATTR_PAINT_GRAY_FILL);
                                d.addCenter("DIVIDE29_2", singleton("相談懇談" + (i+1))); // 区分2
                                d.addAll("GRP29_3", si); // グループ3
                                d.add("DIVIDE29_3", getTokenList(getString("MEETING_NAME", row), 10)); // 区分3
                                d.addAll("GRP29_4", sj); // グループ4
                                d.attribute("GRP29_4", ATTR_PAINT_GRAY_FILL);
                                d.addCenter("DIVIDE29_4", singleton("期日")); // 区分4
                                d.addAll("GRP29_5", sj); // グループ5
                                d.add("DATE29", singleton(formatDate(db2, getString("MEETING_DATE", row)))); // 日付
                                d.addAll("GRP29_6", sj); // グループ6
                                d.attribute("GRP29_6", ATTR_PAINT_GRAY_FILL);
                                d.addCenter("DIVIDE29_5", singleton("構成員")); // 区分5
                                d.addAll("GRP29_7", sj); // グループ7
                                d.add("CHARGE29", getTokenList(getString("TEAM_MEMBERS", row), 40)); // 担当

                                d.setFieldDiv("GRP1", "GRP29_1");
                                d.setFieldDiv("DIV1", "DIVIDE29_1");

                                list12345.addAll(d.getPrintRecordList());
                            } else if (j == 1) {
                                final FieldData d = new FieldData();
                                // 相談懇談　概要
                                d.addAll("GRP23_1", "--"); // グループ1
                                d.attribute("GRP23_1", ATTR_PAINT_GRAY_FILL);
                                d.addCenter("DIVIDE23_1", singleton("")); // 区分1
                                d.addAll("GRP23_2", recordDiv + recordNo); // グループ2
                                d.addAll("GRP23_3", si); // グループ3
                                d.addAll("GRP23_4", sj); // グループ4
                                d.attribute("GRP23_4", ATTR_PAINT_GRAY_FILL);
                                d.addCenter("DIVIDE23_4", singleton("概要")); // 区分4
                                d.addAll("GRP23_5_1", sj); // グループ5
                                d.addAll("GRP23_5_2", sj); // グループ5
                                d.addAll("GRP23_5_3", sj); // グループ5
                                d.add("OUTLINE23", getTokenList(getString("MEETING_SUMMARY", row), 62)); // 概要

                                d.setFieldDiv("GRP1", "GRP23_1");
                                d.setFieldDiv("DIV1", "DIVIDE23_1");

                                list12345.addAll(d.getPrintRecordList());
                            }
                        }
                    }
                } else if ("2".equals(recordDiv)) {
                    final String recordNo = "1";
                    for (int i = 0; i < sidoList.size(); i++) {
                        // 相談の記録
                        final Map row = (Map) sidoList.get(i);
                        final String si = String.valueOf(i);

                        final FieldData d0 = new FieldData();
                        // 校内実習　期日、内容

                        d0.addAll("GRP22_1", "--"); // グループ1
                        d0.attribute("GRP22_1", ATTR_PAINT_GRAY_FILL);
                        d0.addCenter("DIVIDE22_1", singleton("")); // 区分1
                        d0.addAll("GRP22_2", recordDiv + recordNo); // グループ2
                        d0.attribute("GRP22_2", ATTR_PAINT_GRAY_FILL);
                        d0.addCenter("DIVIDE22_2", singleton("校内実習" + (i+1))); // 区分2
                        d0.addAll("GRP22_3", si); // グループ3
                        d0.addAll("GRP22_4", "0"); // グループ4
                        d0.attribute("GRP22_3", ATTR_PAINT_GRAY_FILL);
                        d0.addCenter("DIVIDE22_4", singleton("期日")); // 区分4
                        d0.addAll("GRP22_5", "0"); // グループ5
                        d0.add("DATE22", getTokenList(formatDate(db2, getString("WORK_TRAINING_S_DATE", row)), 10)); // 日付
                        d0.addAll("GRP22_6", "0"); // グループ6
                        d0.attribute("GRP22_5", ATTR_PAINT_GRAY_FILL);
                        d0.addCenter("DIVIDE22_5", singleton("内容")); // 区分5
                        d0.addAll("GRP22_7", "0"); // グループ7
                        d0.add("CHARGE22", getTokenList(getString("WORK_TRAINING_CONTENTS", row), 40)); // 担当

                        d0.setFieldDiv("GRP1", "GRP22_1");
                        d0.setFieldDiv("DIV1", "DIVIDE22_1");

                        list12345.addAll(d0.getPrintRecordList());

                        // 校内実習
                        final FieldDataGroup kounai = new FieldDataGroup();
                        // 校内実習　目標、支援・手立て、成果、課題
                        kounai.addAll("GRP24_1", "--"); // グループ1
                        kounai.attribute("GRP24_1", ATTR_PAINT_GRAY_FILL);
                        kounai.addCenter("DIVIDE24_1", singleton("")); // 区分1
                        kounai.addAll("GRP24_2", recordDiv + recordNo); // グループ2
                        kounai.attribute("GRP24_2", ATTR_PAINT_GRAY_FILL);
                        for (int j = 1; j < 5; j++) {
                            final String sj = String.valueOf(j);

                            List name = Collections.EMPTY_LIST;
                            String field = "";
                            if (j == 1) {
                                name = singleton("目標");
                                field = "WORK_TRAINING_GOAL";
                            } else if (j == 2) {
                                name = asList(new String[] {"支援・手", "立て"});
                                field = "WORK_TRAINING_SUPPORT";
                            } else if (j == 3) {
                                name = singleton("成果");
                                field = "WORK_TRAINING_RESULT";
                            } else if (j == 4) {
                                name = singleton("課題");
                                field = "WORK_TRAINING_CHALLENGE";
                            }
                            final FieldData d = new FieldData();
                            d.addAll("GRP24_3", sj); // グループ3
                            d.attribute("GRP24_3", ATTR_PAINT_GRAY_FILL);
                            d.addCenter("DIVIDE24_3", name); // 区分3
                            d.addAll("GRP24_4_1", sj); // グループ4
                            d.addAll("GRP24_4_2", sj); // グループ4
                            d.addAll("GRP24_4_3", sj); // グループ4
                            d.addAll("GRP24_4_4", sj); // グループ4
                            d.addAll("GRP24_4_5", sj); // グループ4
                            d.addAll("GRP24_4_6", sj); // グループ4
                            d.addAll("GRP24_4_7", sj); // グループ4
                            d.add("HOPE24", getTokenList(getString(field, row), 72)); // 目標


                            d.setFieldDiv("GRP1", "GRP24_1");
                            d.setFieldDiv("DIV1", "DIVIDE24_1");

                            kounai.addFieldData(d);
                        }
                        list12345.addAll(kounai.getPrintRecordList(_param));
                    }
                } else if (("3".equals(recordDiv) || "4".equals(recordDiv))) {
                    int skCnt = 1;
                    int sjCnt = 1;
                    for (int i = 0; i < sidoList.size(); i++) {
                        // 相談の記録
                        final Map row = (Map) sidoList.get(i);

                        final String recordNo = "1";
                        // 職場見学 or 職場実習
                        String name0 = "";
                        String nameBasho = "";
                        if ("3".equals(recordDiv)) {
                            name0 = "職場見学" + (skCnt);
                            nameBasho = "見学場所";
                            skCnt++;
                        } else if ("4".equals(recordDiv)) {
                            name0 = "職場実習" + (sjCnt);
                            nameBasho = "実習場所";
                            sjCnt++;
                        }
                        for (int j = 0; j < 5; j++) {
                            final String sj = String.valueOf(j);
                            if (j == 0) {
                                final FieldData d = new FieldData();
                                // 職場見学　期日、見学場所、内容
                                d.addAll("GRP25_1", "--"); // グループ1
                                d.attribute("GRP25_1", ATTR_PAINT_GRAY_FILL);
                                d.addCenter("DIVIDE25_1", singleton("")); // 区分1
                                d.addAll("GRP25_2", recordDiv + recordNo); // グループ2
                                d.attribute("GRP25_2", ATTR_PAINT_GRAY_FILL);
                                d.addCenter("DIVIDE25_2", singleton(name0)); // 区分2
                                d.addAll("GRP25_3", sj); // グループ3
                                d.attribute("GRP25_3", ATTR_PAINT_GRAY_FILL);
                                d.addCenter("DIVIDE25_3", singleton("期日")); // 区分3
                                d.addAll("GRP25_4", sj); // グループ4
                                d.addCenter("DATE25", singleton(formatDate(db2, getString("WORK_TRAINING_S_DATE", row)))); // 日付
                                d.addAll("GRP25_5", sj); // グループ5
                                d.attribute("GRP25_5", ATTR_PAINT_GRAY_FILL);
                                d.addCenter("DIVIDE25_4", singleton(nameBasho)); // 区分4
                                d.addAll("GRP25_6", sj); // グループ6
                                d.add("PLACE25", getTokenList(getString("WORK_TRAINING_PLACE", row), 20)); // 場所
                                d.addAll("GRP25_7", sj); // グループ7
                                d.attribute("GRP25_7", ATTR_PAINT_GRAY_FILL);
                                d.addCenter("DIVIDE25_5", singleton("内容")); // 区分5
                                d.addAll("GRP25_8", sj); // グループ8
                                d.add("CONTENT25", getTokenList(getString("WORK_TRAINING_CONTENTS", row), 20)); // 内容

                                d.setFieldDiv("GRP1", "GRP25_1");
                                d.setFieldDiv("DIV1", "DIVIDE25_1");

                                list12345.addAll(d.getPrintRecordList());

                            } else if ("3".equals(recordDiv) && j == 2) {
                                final FieldData d = new FieldData();
                                // 職場見学　支援・手立て
                                d.addAll("GRP26_1", "--"); // グループ1
                                d.attribute("GRP26_1", ATTR_PAINT_GRAY_FILL);
                                d.addCenter("DIVIDE26_1", singleton("")); // 区分1
                                d.addAll("GRP26_2", recordDiv + recordNo); // グループ2
                                d.attribute("GRP26_2", ATTR_PAINT_GRAY_FILL);
                                d.addAll("GRP26_3", sj); // グループ3
                                d.attribute("GRP26_3", ATTR_PAINT_GRAY_FILL);
                                d.addCenter("DIVIDE26_3", asList(new String[] {"支援・手", "立て"})); // 区分3
                                d.addAll("GRP26_4", sj); // グループ4
                                d.add("HOPE26", getTokenList(getString("WORK_TRAINING_SUPPORT", row), 62)); // 目標
                                d.addAll("GRP26_5", sj); // グループ5
                                d.add("DIVIDE26_6", getTokenList(getString("WORK_TRAINING_SUPPORT_TARGET", row), 10)); // 区分6

                                d.setFieldDiv("GRP1", "GRP26_1");
                                d.setFieldDiv("DIV1", "DIVIDE26_1");

                                list12345.addAll(d.getPrintRecordList());
                            } else {
                                String[] name = null;
                                String field = "";
                                if (j == 1) {
                                    name = new String[] {"目標"};
                                    field = "WORK_TRAINING_GOAL";
                                } else if (j == 2) {
                                    name = new String[] {"支援・手", "立て"};
                                    field = "WORK_TRAINING_SUPPORT";
                                } else if (j == 3) {
                                    name = new String[] {"成果"};
                                    field = "WORK_TRAINING_RESULT";
                                } else if (j == 4) {
                                    name = new String[] {"課題"};
                                    field = "WORK_TRAINING_CHALLENGE";
                                }
                                final FieldData d = new FieldData();
                                // 職場見学　目標、成果、課題
                                d.addAll("GRP24_1", "--"); // グループ1
                                d.attribute("GRP24_1", ATTR_PAINT_GRAY_FILL);
                                d.addCenter("DIVIDE24_1", singleton("")); // 区分1
                                d.attribute("GRP24_2", ATTR_PAINT_GRAY_FILL);
                                d.addAll("GRP24_2", recordDiv + recordNo); // グループ2
                                d.addAll("GRP24_3", sj); // グループ3
                                d.attribute("GRP24_3", ATTR_PAINT_GRAY_FILL);
                                d.addCenter("DIVIDE24_3", asList(name)); // 区分3
                                d.addAll("GRP24_4_1", sj); // グループ4
                                d.addAll("GRP24_4_2", sj); // グループ4
                                d.addAll("GRP24_4_3", sj); // グループ4
                                d.addAll("GRP24_4_4", sj); // グループ4
                                d.addAll("GRP24_4_5", sj); // グループ4
                                d.addAll("GRP24_4_6", sj); // グループ4
                                d.addAll("GRP24_4_7", sj); // グループ4

                                d.add("HOPE24", getTokenList(getString(field, row), 70)); // 目標

                                d.setFieldDiv("GRP1", "GRP24_1");
                                d.setFieldDiv("DIV1", "DIVIDE24_1");

                                list12345.addAll(d.getPrintRecordList());
                            }
                        }
                    }
                } else if ("5".equals(recordDiv)) {

                    for (int i = 0; i < sidoList.size(); i++) {
                        // 相談の記録
                        final Map row = (Map) sidoList.get(i);

                        for (int j = 0; j < 2; j++) {
                            final String sj = String.valueOf(j);
                            // 年間の成果、年間の課題
                            String name = "";
                            String field = "";
                            if (j == 0) {
                                name = "年間の成果";
                                field = "CAREER_GUIDANCE_RESULT";
                            } if (j == 1) {
                                name = "年間の課題";
                                field = "CAREER_GUIDANCE_CHALLENGE";
                            }
                            final FieldData d = new FieldData();
                            d.addAll("GRP13_1", "--"); // グループ1
                            d.attribute("GRP13_1", ATTR_PAINT_GRAY_FILL);
                            d.addCenter("DIVIDE13_1", singleton("")); // 区分1
                            d.addAll("GRP13_2", sj); // グループ2
                            d.attribute("GRP13_2", ATTR_PAINT_GRAY_FILL);
                            d.addCenter("DIVIDE13_2", singleton(name)); // 区分2
                            d.addAll("GRP13_3", sj); // グループ3
                            d.add("RESULT13", getTokenList(getString(field, row), 72)); // 成果
                            d.addAll("GRP13_4", sj); // グループ3

                            d.setFieldDiv("GRP1", "GRP13_1");
                            d.setFieldDiv("DIV1", "DIVIDE13_1");

                            list12345.addAll(d.getPrintRecordList());
                        }
                    }
                } else if ("6".equals(recordDiv)) {
                    boolean fstChkFlg = false;
                    int fstLineMaxRow = 1;
                    for (final Iterator nIt = asList(new String[] {"1", "2", "3"}).iterator(); nIt.hasNext();) {
                        final String recordNo = (String) nIt.next();

                        final List sidoList2 = withDummy(filterList(sidoList, createCondMap(new String[] {"RECORD_NO", recordNo})));
                        final FieldDataGroup g = new FieldDataGroup();
                        List lWk = null;
                        if (0 < sidoList2.size()) {
                            final Map row = (Map) sidoList2.get(0);
                            lWk = getTokenList(getString("COURSE_CONTENTS", row), 50); // 内容
                        }
                        if (!fstChkFlg) {
                            fstLineMaxRow = lWk == null ? sidoList2.size() : Math.max(sidoList2.size(), lWk.size());
                            fstChkFlg = true;
                        }
                        final List name0;
                        if (fstLineMaxRow > 1) {
                            name0 = "1".equals(recordNo) ? asList(new String[] {"決定進路", "内容"}) : Collections.EMPTY_LIST;
                        } else {
                            name0 = "1".equals(recordNo) ? asList(new String[] {"決定進路"}) : "2".equals(recordNo) ? asList(new String[] {"内容"}) : Collections.EMPTY_LIST;
                        }
                        if ("1".equals(recordNo) || "2".equals(recordNo)) {
                            String name = "";
                            if ("1".equals(recordNo)) {
                                name = "事業所";
                            } else if ("2".equals(recordNo)) {
                                name = "福祉利用";
                            }
                            // 決定進路内容　事業所、福祉利用
                            g.addAll("GRP16_1", "F" + recordNo); // グループ1
                            g.attribute("GRP16_1", ATTR_PAINT_GRAY_FILL);
                            g.addCenter("DIVIDE16_1", name0); // 区分1
                            g.addAll("GRP16_2", recordNo); // グループ2
                            g.attribute("GRP16_2", ATTR_PAINT_GRAY_FILL);
                            g.addCenter("DIVIDE16_2", singleton(name)); // 区分2
                        } else if ("3".equals(recordNo)) {
                            g.addAll("GRP27_1", "F" + recordNo); // グループ1
                            g.attribute("GRP27_1", ATTR_PAINT_GRAY_FILL);
                            g.addCenter("DIVIDE27_1", name0); // 区分1
                            g.addAll("GRP27_2", recordNo); // グループ2
                            g.attribute("GRP27_2", ATTR_PAINT_GRAY_FILL);
                            g.addCenter("DIVIDE27_2", singleton("その他")); // 区分2
                        }
                        for (int i = 0; i < sidoList2.size(); i++) {
                            // 相談の記録
                            final Map row = (Map) sidoList2.get(i);
                            final String si = recordNo + String.valueOf(i);

                            if ("1".equals(recordNo) || "2".equals(recordNo)) {
                                final FieldData d = new FieldData();
                                // 決定進路内容　事業所、福祉利用
                                d.addAll("GRP16_3", si); // グループ3
                                d.add("FACULTY16", getTokenList(getString("DETERMINED_COURSE", row), 20)); // 事業所
                                d.addAll("GRP16_4", si); // グループ4
                                d.attribute("GRP16_5", ATTR_PAINT_GRAY_FILL);
                                d.addCenter("DIVIDE16_3", singleton("内容")); // 区分3
                                d.addAll("GRP16_5", si); // グループ5
                                d.add("CONTENT16", getTokenList(getString("COURSE_CONTENTS", row), 50)); // 内容
                                d.addAll("GRP16_6", si); // グループ6

                                d.setFieldDiv("GRP1", "GRP16_1");
                                d.setFieldDiv("DIV1", "DIVIDE16_1");

                                g.addFieldData(d);
                            } else if ("3".equals(recordNo)) {
                                // 決定進路内容　その他
                                final FieldData d = new FieldData();
                                d.addAll("GRP27_3", si); // グループ3
                                d.add("ETC", getTokenList(getString("REMARK", row), 82)); // その他
                                d.addAll("GRP27_4", si); // グループ3
                                d.addAll("GRP27_5", si); // グループ3
                                d.addAll("GRP27_6", si); // グループ3

                                d.setFieldDiv("GRP1", "GRP27_1");
                                d.setFieldDiv("DIV1", "DIVIDE27_1");

                                g.addFieldData(d);
                            }
                        }
                        list6.addAll(g.getPrintRecordList(_param));
                    }
                }
            }

            // 行数によるセンタリング処理
            setRecordFieldDataAll(list12345, "GRP1", "RD");
            setRecordFieldDataList(list12345, "DIV1", extendStringList(singleton("進路指導"), list12345.size(), true));


            final List listAll = new ArrayList();
            listAll.addAll(list12345);
            listAll.addAll(list6);
            log.debug(" list12345 size = " + list12345.size());
            FieldData.svfPrintRecordList(listAll, form);
        }

        private void printDRecordHikitsugi(final Form form, final Map main1) {
            final FieldData dHikitsugi = new FieldData();
            // 引継ぎ事項
            dHikitsugi.addAll("GRP28_1", "--"); // グループ1
            dHikitsugi.attribute("GRP28_1", ATTR_PAINT_GRAY_FILL);
            dHikitsugi.addCenter("DIVIDE28_1", asList(new String[] {"引継ぎ", "事項"})); // 区分1
            dHikitsugi.addAll("GRP28_2_1", "--"); // グループ2
            dHikitsugi.addAll("GRP28_2_2", "--"); // グループ2
            dHikitsugi.addAll("GRP28_2_3", "--"); // グループ2
            dHikitsugi.add("TAKEOVER", getTokenList(getString("TAKEOVER", main1), 94)); // 引継
            FieldData.svfPrintRecordList(dHikitsugi.getPrintRecordList(), form);
        }
    }

    // アセスメント票
    private static class KNJE390MA extends KNJE390M_0 {
        public KNJE390MA(final Param param) {
            super(param);
        }
        public void printMain(final Vrw32alp svf, final DB2UDB db2, final Map student) {
            final String schregno = getString("SCHREGNO", student);

            final Form form = new Form(_param, svf);
            form._form1 = "KNJE390M_D_1.frm";
            form._recMax1 = 37; // 1ページ目の行数
            form._isForm1 = true;
            form.setForm(form._form1, 4);
            form._form2 = form._form1;
            form._form2n = 4;
            form._recMax2 = 42; // 1ページ目の行数
            form._recMax = form._recMax1; // 1ページ目の行数

            final StringBuffer sql1 = new StringBuffer();
            sql1.append("SELECT ");
            sql1.append("   T1.* ");
            sql1.append(" FROM SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT T1 ");
            sql1.append(" LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.REGISTERCD ");
            sql1.append(" WHERE T1.YEAR = '" + _param._year + "' ");
            sql1.append("   AND T1.SCHREGNO = '" + schregno + "' ");
            sql1.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) ");
            sql1.append("                         FROM SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT ");
            sql1.append("                         WHERE YEAR = T1.YEAR ");
            sql1.append("                           AND SCHREGNO = T1.SCHREGNO ");
            sql1.append("                        )");
            final Map main1 = getFirstRow(getRowList(db2, sql1.toString()));

            form._svf.VrsOut("TITLE", "個別の教育支援計画");
            form.VrsOut("SUBTITLE", "アセスメント");

            printStudent(db2, schregno, student, form, main1);

            if("1".equals(_param._dOutput1)) {
                //障害名等
                printARecordShougaiMei(main1, form);
            }

            if("1".equals(_param._dOutput2)) {
                //実態概要・障害の特性
                printARecordShougaiNoJittai(main1, form);
            }

            final String recordDate = getString("RECORD_DATE", main1);

            log.info(schregno + " D recordDate = " + recordDate);

            if("1".equals(_param._dOutput3)) {
                //検査
                printARecordHattatsuKensa(db2, schregno, recordDate, form);
            }

            printARecordJittaiBunseki(db2, schregno, recordDate, form);
        }

        public void printStudent(final DB2UDB db2, final String schregno, final Map student, final Form form, final Map main1) {
            form.VrsOut("SCHOOL_NAME", _param._knjSchoolMst._schoolName1); // 学校名
            form.VrsOut("DEPARTMENT_NAME", StringUtils.defaultString(getString("COURSENAME", student))); // 学科名
            form.VrsOut("GRADE_NAME", getPrintStudentGrHrname(student)); // 学年・組

            final int entryMaxLine = 1;
            final Map updateMap = getUpdateHistMap(db2, "A", _param._year, schregno, entryMaxLine, getString("WRITING_DATE", main1));
            if (!FORM_D1.equals(_param._formD) && getString("WRITING_DATE", main1) != null) {
                if (updateMap.containsKey("RECORD_DATE")) form.VrsOut("MAKE_DATE", formatDate(db2, getString("RECORD_DATE", updateMap))); // 作成日
                if (updateMap.containsKey("RECORD_STAFFNAME")) {
                    final String RSName = getString("RECORD_STAFFNAME", updateMap);
                    final int mlen = KNJ_EditEdit.getMS932ByteLength(RSName);
                    if (mlen > 30) {
                        final String[] cutStr = KNJ_EditEdit.get_token(RSName, 40, 2);
                        if (cutStr[0] != null) form.VrsOutn("MAKER1", 1,cutStr[0]); // 作成者
                        if (cutStr[1] != null) form.VrsOutn("MAKER1", 2,cutStr[1]);
                    } else {
                        form.VrsOutn("MAKER1", 1,RSName); // 作成者
                    }
                }
            }

            svfVrsOutWithSize(form, "KANA", new String[] {"2", "3"}, new int[] {30, 40}, getString("NAME_KANA", student)); // 氏名かな
            svfVrsOutWithSize(form, "NAME", new String[] {"2", "3"}, new int[] {26, 48}, getString("NAME", student)); // 氏名

            form.VrsOut("SEX", getString("SEX_NAME", student)); // 性別
            form.VrsOut("BIRTHDAY", formatDate(db2, getString("BIRTHDAY", student))); // 生年月日

            form.VrsOut("UPDATE", formatDate(db2, getString("UPDATE_DATE1", updateMap))); // 更新日
            svfVrsOutWithSize(form, "ENTRY_PERSON", new String[] {"1", "2"}, new int[] {10, 16}, getString("UPDATE_STAFFNAME1", updateMap));  // 記入者

            for (int i = 0; i <= 10; i++) {
                form._svf.VrAttribute("HEAD_" + String.valueOf(i), ATTR_PAINT_GRAY_FILL);
            }

            form.VrEndRecord();
        }

        private void printARecordShougaiMei(final Map main1, final Form form) {
            // 障害名等
            final FieldDataGroup dGaiyou = new FieldDataGroup();
            dGaiyou.addAll("GRP5_1", "0"); // グループ1
            dGaiyou.attribute("GRP5_1", ATTR_PAINT_GRAY_FILL); // グループ1
            dGaiyou.addCenter("DIVIDE5_1", asList(new String[] {"障害名等"})); // 区分1
            dGaiyou.addAll("GRP5_2", "0"); // グループ1
            dGaiyou.addWithKetaMinLen("DIVIDE5_2", getString("CHALLENGED_NAMES", main1), form, 3);
            dGaiyou.addAll("GRP5_3", "0"); // グループ1
            dGaiyou.addAll("GRP5_4", "0"); // グループ1
            dGaiyou.addAll("GRP5_5", "0"); // グループ1
            dGaiyou.addAll("GRP5_6", "0"); // グループ1
            dGaiyou.addAll("GRP5_7", "0"); // グループ1
            dGaiyou.addAll("GRP5_8", "0"); // グループ1
            dGaiyou.addAll("GRP5_9", "0"); // グループ1
            dGaiyou.addAll("GRP5_10", "0"); // グループ1
            dGaiyou.addAll("GRP5_11", "0"); // グループ1
            dGaiyou.addAll("GRP5_12", "0"); // グループ1
            dGaiyou.addAll("GRP5_13", "0"); // グループ1
            dGaiyou.addAll("GRP5_14", "0"); // グループ1
            FieldData.svfPrintRecordList(dGaiyou.getPrintRecordList(_param), form);
        }

        private void printARecordShougaiNoJittai(final Map main1, final Form form) {
            // 障害の実態・特性
            final FieldDataGroup dGaiyou = new FieldDataGroup();
            dGaiyou.addAll("GRP5_1", "1"); // グループ1
            dGaiyou.attribute("GRP5_1", ATTR_PAINT_GRAY_FILL); // グループ1
            dGaiyou.addCenter("DIVIDE5_1", asList(new String[] {"実態概要", "・障害の", "特性"})); // 区分1
            dGaiyou.addAll("GRP5_2", "1"); // グループ1
            dGaiyou.addWithKetaMinLen("DIVIDE5_2", getString("CHALLENGED_STATUS", main1), form, 3);
            dGaiyou.addAll("GRP5_3", "1"); // グループ1
            dGaiyou.addAll("GRP5_4", "1"); // グループ1
            dGaiyou.addAll("GRP5_5", "1"); // グループ1
            dGaiyou.addAll("GRP5_6", "1"); // グループ1
            dGaiyou.addAll("GRP5_7", "1"); // グループ1
            dGaiyou.addAll("GRP5_8", "1"); // グループ1
            dGaiyou.addAll("GRP5_9", "1"); // グループ1
            dGaiyou.addAll("GRP5_10", "1"); // グループ1
            dGaiyou.addAll("GRP5_11", "1"); // グループ1
            dGaiyou.addAll("GRP5_12", "1"); // グループ1
            dGaiyou.addAll("GRP5_13", "1"); // グループ1
            dGaiyou.addAll("GRP5_14", "1"); // グループ1
            FieldData.svfPrintRecordList(dGaiyou.getPrintRecordList(_param), form);
        }

        private void printARecordHattatsuKensa(final DB2UDB db2, final String schregno, final String recordDate, final Form form) {
            // 発達検査
            final StringBuffer sql2 = new StringBuffer();
            sql2.append("SELECT ");
            sql2.append("   T1.* ");
            sql2.append(" FROM SCHREG_CHALLENGED_ASSESSMENT_CHECK_RECORD_DAT T1 ");
            sql2.append(" WHERE T1.YEAR = '" + _param._year + "' ");
            sql2.append("   AND T1.SCHREGNO = '" + schregno + "' ");
            sql2.append("   AND T1.RECORD_DATE = '" + recordDate + "' ");
            sql2.append(" ORDER BY T1.RECORD_SEQ ");
            final List check1List = withDummy(getRowList(db2, sql2.toString()));

            final FieldDataGroup dHattatsuKensa = new FieldDataGroup();

            for (final Iterator it = check1List.iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String recordSeq = getString("RECORD_SEQ", row);

                for (int i = 1; i <= 2; i++) {
                    final FieldDataGroup d0 = new FieldDataGroup();
                    if (i == 1) {
                        d0.setFieldDiv("GRP1", "GRP1_1");
                        d0.setFieldDiv("DIV1", "DIVIDE1_1");

                        d0.addAll("GRP1_2", recordSeq); // グループ2
                        d0.addAll("GRP1_3", recordSeq); // グループ3
                        d0.addAll("GRP1_4", recordSeq); // グループ4
                        d0.addAll("GRP1_5", recordSeq); // グループ5
                        d0.addAll("GRP1_6", recordSeq); // グループ6
                        d0.addAll("GRP1_7", recordSeq); // グループ7
                        d0.addAll("GRP1_8", recordSeq); // グループ8
                        d0.addAll("GRP1_9", recordSeq); // グループ9
                        d0.addAll("GRP1_10", recordSeq); // グループ10
                        d0.attribute("DIVIDE1_1", ATTR_PAINT_GRAY_FILL); // グループ1
                        d0.add("DIVIDE1_2", singleton("検査日")); // 区分2
                        d0.attribute("DIVIDE1_2", ATTR_PAINT_GRAY_FILL); // グループ1
                        d0.addWithKeta("DATE1", formatDateYearMonth(db2, getString("CHECK_DATE", row)), form); // 日付

                        d0.addCenter("DIVIDE1_3", singleton("検査機関")); // 区分3
                        d0.attribute("DIVIDE1_3", ATTR_PAINT_GRAY_FILL); // グループ1
                        d0.addWithKeta("RESULT1",  getString("CHECK_CENTER_TEXT", row), form); // 結果

                    } else if (i == 2) {

                        d0.setFieldDiv("GRP1", "GRP6_1");
                        d0.setFieldDiv("DIV1", "DIVIDE6_1");

                        d0.addAll("GRP6_2", recordSeq); // グループ2
                        d0.addAll("GRP6_3", recordSeq); // グループ3
                        d0.addAll("GRP6_4", recordSeq); // グループ4
                        d0.addAll("GRP6_5", recordSeq); // グループ5
                        d0.addAll("GRP6_6", recordSeq); // グループ6
                        d0.addAll("GRP6_7", recordSeq); // グループ7
                        d0.attribute("DIVIDE6_1", ATTR_PAINT_GRAY_FILL); // グループ1
                        d0.attribute("GRP6_2", ATTR_PAINT_GRAY_FILL); // グループ1
                        d0.addCenter("DIVIDE6_2", singleton("検査名")); // 区分3
                        d0.addWithKeta("RESULT6",  getString("CHECK_NAME", row), form); // 結果
                    }

                    dHattatsuKensa.addFieldData(d0);
                }
            }

            final List recordList = dHattatsuKensa.getPrintRecordList(_param);

            setRecordFieldDataAll(recordList, "GRP1", "0");
            setRecordFieldDataList(recordList, "DIV1", extendStringList(singleton("検査"), recordList.size(), true));

            FieldData.svfPrintRecordList(recordList, form);
        }

        private void printARecordJittaiBunseki(final DB2UDB db2, final String schregno, final String recordDate, final Form form) {
            String statusName = "";
            String growupName = "";
            final Map dataDivMap = new TreeMap();
            for (final Iterator it = KnjDbUtils.query(db2, " SELECT * FROM CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT WHERE YEAR = '" + _param._year + "' ").iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String dataDiv = KnjDbUtils.getString(row, "DATA_DIV");
                if ("0".equals(dataDiv)) {
                    statusName = StringUtils.defaultString(KnjDbUtils.getString(row, "STATUS_NAME"));
                    growupName = StringUtils.defaultString(KnjDbUtils.getString(row, "GROWUP_NAME"));

                } else {
                    dataDivMap.put(Integer.valueOf(dataDiv), StringUtils.defaultString(KnjDbUtils.getString(row, "DATA_DIV_NAME")));
                }
            }

            // 実態・分析、課題・つけたい力
            final StringBuffer sql3 = new StringBuffer();
            sql3.append("SELECT ");
            sql3.append("   T1.* ");
            sql3.append(" FROM SCHREG_CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT T1 ");
            sql3.append(" WHERE T1.YEAR = '" + _param._year + "' ");
            sql3.append("   AND T1.SCHREGNO = '" + schregno + "' ");
            sql3.append("   AND T1.RECORD_DATE = '" + recordDate + "' ");
            sql3.append(" ORDER BY T1.DATA_DIV ");
            final List kadai1List = getRowList(db2, sql3.toString());

            if("".equals(growupName)){
                //帳票パターン:1枠
                // 実態・分析、課題・つけたい力　ヘッダ
                form.VrsOut("DIVIDE7", statusName); // 区分1
                form.VrAttribute("DIVIDE7", ATTR_PAINT_GRAY_FILL); // グループ1
                form.VrEndRecord();

                for (final Iterator it = dataDivMap.keySet().iterator(); it.hasNext();) {
                    final Integer iDataDiv = (Integer) it.next();
                    final String dataDivName = (String) dataDivMap.get(iDataDiv);
                    final String dataDiv = iDataDiv.toString();

                    final Map row = getFirstRow(filterList(kadai1List, createCondMap(new String[] {"DATA_DIV", dataDiv})));

                    final FieldData d = new FieldData();
                    d.addAll("GRP5_1", dataDiv); // グループ1
                    d.addAll("GRP5_2", dataDiv); // グループ1
                    d.addAll("GRP5_3", dataDiv); // グループ1
                    d.addAll("GRP5_4", dataDiv); // グループ1
                    d.addAll("GRP5_5", dataDiv); // グループ1
                    d.addAll("GRP5_6", dataDiv); // グループ1
                    d.addAll("GRP5_7", dataDiv); // グループ1
                    d.addAll("GRP5_8", dataDiv); // グループ1
                    d.addAll("GRP5_9", dataDiv); // グループ1
                    d.addAll("GRP5_10", dataDiv); // グループ1
                    d.addAll("GRP5_11", dataDiv); // グループ1
                    d.addAll("GRP5_12", dataDiv); // グループ1
                    d.addAll("GRP5_13", dataDiv); // グループ1
                    d.addAll("GRP5_14", dataDiv); // グループ1
                    d.attribute("GRP5_1", ATTR_PAINT_GRAY_FILL); // グループ1
                    d.addCenterWithKeta("DIVIDE5_1", dataDivName, form); // 区分1
                    if (FORM_D1.equals(_param._formD)) {
                        //30->15行のため、既存データが余計に出力されないよう、制限
                        d.addWithKeta("DIVIDE5_2", getString("STATUS", row), form, 15); // 実態
                    } else {
                        d.addWithKeta("DIVIDE5_2", getString("STATUS", row), form); // 実態
                    }
                    FieldData.svfPrintRecordList(d.getPrintRecordList(), form);
                }

                //各教科の実態
                boolean firstFlg = true;
                final Map assesmentMap = getAssessmentStatus(db2, schregno);
                for (final Iterator it = assesmentMap.keySet().iterator(); it.hasNext();) {
                    //ヘッダ
                    if(firstFlg) {
                        form.VrsOut("DIVIDE7", "実態"); // 区分1
                        form.VrAttribute("DIVIDE7", ATTR_PAINT_GRAY_FILL); // グループ1
                        form.VrEndRecord();
                        firstFlg = false;
                    }

                    //明細
                    final String subclass = (String) it.next();
                    final Map map = (Map) assesmentMap.get(subclass);
                    final FieldData d = new FieldData();
                    d.addAll("GRP5_1", subclass); // グループ1
                    d.addAll("GRP5_2", subclass); // グループ1
                    d.addAll("GRP5_3", subclass); // グループ1
                    d.addAll("GRP5_4", subclass); // グループ1
                    d.addAll("GRP5_5", subclass); // グループ1
                    d.addAll("GRP5_6", subclass); // グループ1
                    d.addAll("GRP5_7", subclass); // グループ1
                    d.addAll("GRP5_8", subclass); // グループ1
                    d.addAll("GRP5_9", subclass); // グループ1
                    d.addAll("GRP5_10", subclass); // グループ1
                    d.addAll("GRP5_11", subclass); // グループ1
                    d.addAll("GRP5_12", subclass); // グループ1
                    d.addAll("GRP5_13", subclass); // グループ1
                    d.addAll("GRP5_14", subclass); // グループ1
                    d.attribute("GRP5_1", ATTR_PAINT_GRAY_FILL); // グループ1
                    d.addCenterWithKeta("DIVIDE5_1", (String) map.get("SUBCLASSNAME"), form); // 区分1
                    if (FORM_D1.equals(_param._formD)) {
                        //30->15行のため、既存データが余計に出力されないよう、制限
                        d.addWithKeta("DIVIDE5_2", (String) map.get("STATUS"), form, 15); // 実態
                    } else {
                        d.addWithKeta("DIVIDE5_2", (String) map.get("STATUS"), form); // 実態
                    }
                    FieldData.svfPrintRecordList(d.getPrintRecordList(), form);
                }
            } else {
                //帳票パターン:2枠
                // 実態・分析、課題・つけたい力　ヘッダ
                form.VrsOut("GRP2_1", "--"); // グループ1
                form.VrsOut("DIVIDE2_1", ""); // 区分1
                form.VrsOut("GRP2_2", "--"); // グループ2
                form.VrsOut("DIVIDE2_2", statusName); // 区分2
                form.VrAttribute("DIVIDE2_2", ATTR_PAINT_GRAY_FILL); // グループ1
                form.VrsOut("GRP2_3", "--"); // グループ3
                form.VrsOut("DIVIDE2_3", growupName); // 区分3
                form.VrAttribute("DIVIDE2_3", ATTR_PAINT_GRAY_FILL); // グループ1
                form.VrEndRecord();

                for (final Iterator it = dataDivMap.keySet().iterator(); it.hasNext();) {
                    final Integer iDataDiv = (Integer) it.next();
                    final String dataDivName = (String) dataDivMap.get(iDataDiv);
                    final String dataDiv = iDataDiv.toString();

                    final Map row = getFirstRow(filterList(kadai1List, createCondMap(new String[] {"DATA_DIV", dataDiv})));

                    final FieldData d = new FieldData();
                    d.addAll("GRP3_1", dataDiv); // グループ1
                    d.attribute("GRP3_1", ATTR_PAINT_GRAY_FILL); // グループ1
                    d.addCenterWithKeta("DIVIDE3_1", dataDivName, form); // 区分1
                    d.addAll("GRP3_2", dataDiv); // グループ2
                    if (FORM_D1.equals(_param._formD)) {
                        //30->15行のため、既存データが余計に出力されないよう、制限
                        d.addWithKeta("CONDITION", getString("STATUS", row), form, 15); // 実態
                        d.addWithKeta("SUBJECT", getString("GROWUP", row), form, 15); // 課題
                    } else {
                        d.addWithKeta("CONDITION", getString("STATUS", row), form); // 実態
                        d.addWithKeta("SUBJECT", getString("GROWUP", row), form); // 課題
                    }
                    d.addAll("GRP3_3", dataDiv); // グループ3
                    FieldData.svfPrintRecordList(d.getPrintRecordList(), form);
                }

                //各教科の実態・支援
                boolean firstFlg = true;
                final Map assesmentMap = getAssessmentStatus(db2, schregno);
                for (final Iterator it = assesmentMap.keySet().iterator(); it.hasNext();) {
                    //ヘッダ
                    if(firstFlg) {
                        form.VrsOut("GRP2_1", "00"); // グループ1
                        form.VrsOut("DIVIDE2_1", ""); // 区分1
                        form.VrsOut("GRP2_2", "00"); // グループ2
                        form.VrsOut("DIVIDE2_2", "実態"); // 区分2
                        form.VrAttribute("DIVIDE2_2", ATTR_PAINT_GRAY_FILL); // グループ1
                        form.VrsOut("GRP2_3", "00"); // グループ3
                        form.VrsOut("DIVIDE2_3", "支援"); // 区分3
                        form.VrAttribute("DIVIDE2_3", ATTR_PAINT_GRAY_FILL); // グループ1
                        form.VrEndRecord();
                        firstFlg = false;
                    }

                    //明細
                    final String subclass = (String) it.next();
                    final Map map = (Map) assesmentMap.get(subclass);
                    final FieldData d = new FieldData();
                    d.addAll("GRP3_1", subclass); // グループ1
                    d.attribute("GRP3_1", ATTR_PAINT_GRAY_FILL); // グループ1
                    d.addCenterWithKeta("DIVIDE3_1", (String) map.get("SUBCLASSNAME"), form); // 区分1
                    d.addAll("GRP3_2", subclass); // グループ2
                    if (FORM_D1.equals(_param._formD)) {
                        //30->15行のため、既存データが余計に出力されないよう、制限
                        d.addWithKeta("CONDITION", (String) map.get("STATUS"), form, 15); // 実態
                        d.addWithKeta("SUBJECT", (String) map.get("FUTURE_CARE"), form, 15); // 支援
                    } else {
                        d.addWithKeta("CONDITION", (String) map.get("STATUS"), form); // 実態
                        d.addWithKeta("SUBJECT", (String) map.get("FUTURE_CARE"), form); // 支援
                    }
                    d.addAll("GRP3_3", subclass); // グループ3
                    FieldData.svfPrintRecordList(d.getPrintRecordList(), form);
                }
            }
        }

        //各教科の実態・支援を取得
        private Map getAssessmentStatus(final DB2UDB db2, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.RECORD_DATE, ");
            stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("   T2.SUBCLASSNAME, ");
            stb.append("   T1.STATUS, ");
            stb.append("   T1.FUTURE_CARE ");
            stb.append(" FROM ");
            stb.append("   SCHREG_CHALLENGED_ASSESSMENT_STATUS_SUBCLASS_DAT T1 ");
            stb.append("   INNER JOIN SUBCLASS_MST T2 ");
            stb.append("           ON T2.CLASSCD       = T1.CLASSCD ");
            stb.append("          AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("          AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("          AND T2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("       T1.YEAR     = '"+ _param._year +"' ");
            stb.append("   AND T1.SCHREGNO = '"+ schregno +"' ");
            stb.append("   AND (T1.STATUS IS NOT NULL OR FUTURE_CARE IS NOT NULL) ");
            stb.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) ");
            stb.append("                           FROM SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT ");
            stb.append("                          WHERE YEAR     = T1.YEAR ");
            stb.append("                            AND SCHREGNO = T1.SCHREGNO ");
            stb.append("                        ) ");
            stb.append(" ORDER BY SUBCLASSCD ");
            final String sql =  stb.toString();

            PreparedStatement ps = null;
            ResultSet rs = null;
            Map resultMap = new TreeMap();

            try {
                log.debug(sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    Map map = new TreeMap();

                    map.put("RECORD_DATE", StringUtils.defaultString(rs.getString("RECORD_DATE")));
                    map.put("SUBCLASSCD", StringUtils.defaultString(rs.getString("SUBCLASSCD")));
                    map.put("SUBCLASSNAME", StringUtils.defaultString(rs.getString("SUBCLASSNAME")));
                    map.put("STATUS", StringUtils.defaultString(rs.getString("STATUS")));
                    map.put("FUTURE_CARE", StringUtils.defaultString(rs.getString("FUTURE_CARE")));

                    final String key = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    resultMap.put(key, map);
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return resultMap;
        }
    }

    private static class KNJE390ME extends KNJE390M_0 {

        public KNJE390ME(final Param param) {
            super(param);
        }

        public void printMain(final Vrw32alp svf, final DB2UDB db2, final Map student) {
            final String schregno = getString("SCHREGNO", student);

            final Form form = new Form(_param, svf);
            form._form1 = "KNJE390M_E.frm";
            form._recMax1 = 999; // 28;
            form._isForm1 = true;
            form.setForm(form._form1, 4);

            form.VrsOut("SCHOOL_NAME", _param._knjSchoolMst._schoolName1); // 学校名

            form.VrsOut("PRINT_DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate)); // 印刷日
            form.VrsOut("TITLE", "サポートブック"); // タイトル
            final String courseName = StringUtils.defaultString(getString("COURSENAME", student));
            final String hrName = StringUtils.defaultString(getPrintStudentGrHrname(student));
            final String attendno = NumberUtils.isDigits(getString("ATTENDNO", student)) ? String.valueOf(Integer.parseInt(getString("ATTENDNO", student))) + "番" : "";
            final String studentName = StringUtils.defaultString(getString("NAME", student));
            form.VrsOut("NAME1", courseName + " " + hrName + " " + attendno); //学部・年組
            form.VrsOut("SCHOOL_TELNO", StringUtils.isBlank(_param._knjSchoolMst._schoolTelNo) ? "" : "(" + _param._knjSchoolMst._schoolTelNo + ")"); // 学校電話番号

            form.VrsOut("PIC", _param.getStudentImageFilePath(schregno)); //
            form.VrsOut("MEETING_NAME", studentName); // 会議名
            form.VrsOut("BLOOD_TYPE", getString("BLOODTYPE", student)); // 血液型

            form.VrsOut("MEETING_PERSON1", StringUtils.defaultString(getString("GUARD_ADDR1", student)) + ("1".equals(getString("GUARD_ADDR_FLG", student)) ? StringUtils.defaultString(getString("GUARD_ADDR2", student)) : "")); // 打合せ参加者
            form.VrsOut("MEETING_PERSON2", getString("GUARD_NAME", student)); // 打合せ参加者
            form.VrsOut("TELNO2", getString("EMERGENCYTELNO", student)); // 連絡先
            form.VrsOut("TELNO3", getString("EMERGENCYTELNO2", student)); // 連絡先

            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT ");
            sql.append("   T1.* ");
            sql.append(" FROM SCHREG_CHALLENGED_SUPPORTBOOK_MAIN_DAT T1 ");
            sql.append(" WHERE ");
            sql.append("   T1.YEAR = '" + _param._year + "' ");
            sql.append("   AND T1.SCHREGNO = '" + schregno + "' ");
            sql.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) ");
            sql.append("                         FROM SCHREG_CHALLENGED_SUPPORTBOOK_MAIN_DAT ");
            sql.append("                         WHERE YEAR = T1.YEAR ");
            sql.append("                           AND SCHREGNO = T1.SCHREGNO ");
            sql.append("                        )");

            final Map main = getFirstRow(getRowList(db2, sql.toString()));
            form.VrsOut("MEETING_TIME", getString("CHALLENGED_POPULAR_NAME", main));

            svfVrListOut(form, "DIAG_NAME", getTokenList(getString("CHALLENGED_NAMES", main), 80)); // 障害名診断名

            for (int i = 0; i < 4; i++) {
                final String cd = String.valueOf(i);
                String title = null, field = null;
                if (i == 0) {
                    title = "持病名";
                    field = "CHRONIC_DISEASE";
                } else if (i == 1) {
                    title = "コミュニケーション（会話）の仕方";
                    field = "HOW_COMMUNICATION";
                } else if (i == 2) {
                    title = "困った行動と対処法";
                    field = "TROUBLED_BEHAVIOR_SUPPORT";
                } else if (i == 3) {
                    title = "不調のサイン";
                    field = "BAD_CONDITION_SIGN";
                }

                final FieldData d = new FieldData();
                d.addCenter("TTEM", getTokenList(title, 18)); // 区分1
                d.add("CONTENT", addLineIfLessThanCount(getTokenList(getString(field, main), 80), 5));
                for (int j = 1; j <= 2; j++) {
                    d.addAll("GRP" + String.valueOf(j), cd);
                }
                FieldData.svfPrintRecordList(d.getPrintRecordList(), form);
            }
        }
    }

    private static class KNJE390MF extends KNJE390M_0 {

        public KNJE390MF(final Param param) {
            super(param);
        }

        public void printMain(final Vrw32alp svf, final DB2UDB db2, final Map student) {
            final String schregno = getString("SCHREGNO", student);

            final Form form = new Form(_param, svf);
            form._form1 = "KNJE390M_F.frm";
            form._recMax1 = 999; // 40;
            form._isForm1 = true;
            form.setForm(form._form1, 4);

            form.VrsOut("PRINT_DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate)); // 印刷日
            form.VrsOut("TITLE", "担任等引継資料"); // タイトル

            final String courseName = StringUtils.defaultString(getString("COURSENAME", student));
            final String hrName = StringUtils.defaultString(getPrintStudentGrHrname(student));
            final String attendno = NumberUtils.isDigits(getString("ATTENDNO", student)) ? String.valueOf(Integer.parseInt(getString("ATTENDNO", student))) + "番" : "";
            final String studentName = StringUtils.defaultString(getString("NAME", student));
            form.VrsOut("NAME1", courseName + " "+ hrName + " " + attendno + " " + studentName); // 氏名

            form.VrsOut("TEACHER_NAME", StringUtils.defaultString(getString("GHR_STAFFNAME", student), getString("HR_STAFFNAME", student))); // 担任名
            form.VrsOut("CONTENT_NAME1", "支援なしでできること"); // 内容名
            form.VrsOut("CONTENT_NAME2", "一部支援でできること"); // 内容名
            form.VrsOut("CONTENT_NAME3", "手立て"); // 内容名
            form.VrsOut("CONTENT_NAME4", "短期目標"); // 内容名
            form.VrsOut("CONTENT_NAME5", "将来的に目ざす"); // 内容名
            form.VrsOut("CONTENT_NAME6", ""); // 内容名

            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT ");
            sql.append("   T1.NAMECD2, ");
            sql.append("   T1.NAME1, ");
            sql.append("   T2.CAN_BE_NO_SUPPORT, ");
            sql.append("   T2.CAN_BE_SOME_SUPPORT, ");
            sql.append("   T2.MEANS, ");
            sql.append("   T2.SHORT_TERM_GOAL, ");
            sql.append("   T2.GOAL_FUTURE ");
            sql.append(" FROM NAME_MST T1 ");
            sql.append(" LEFT JOIN SCHREG_CHALLENGED_TEACHER_TAKEOVER_DOCUMENTS_DAT T2 ON T2.YEAR = '" + _param._year + "' ");
            sql.append("   AND T2.SCHREGNO = '" + schregno + "' ");
            sql.append("   AND T2.DATA_DIV = T1.NAMECD2 ");
            sql.append(" WHERE ");
            sql.append("   T1.NAMECD1 = 'E042' ");
            sql.append("   AND (T2.RECORD_DATE IS NULL OR T2.RECORD_DATE = (SELECT MAX(RECORD_DATE) ");
            sql.append("                         FROM SCHREG_CHALLENGED_TEACHER_TAKEOVER_DOCUMENTS_DAT ");
            sql.append("                         WHERE ");
            sql.append("                           YEAR = T2.YEAR ");
            sql.append("                           AND SCHREGNO = T2.SCHREGNO ");
            sql.append("                        ) ");
            sql.append("        ) ");
            sql.append(" ORDER BY ");
            sql.append("   T1.NAMECD2 ");
            final List e042cdList = getRowList(db2, sql.toString());

            for (final Iterator it = e042cdList.iterator(); it.hasNext();) {
                final Map data = (Map) it.next();

                final String cd = getString("NAMECD2", data);
                final String title = getString("NAME1", data);

                form.VrsOut("TTEM", null); // 項目
                form.VrsOut("GRP", cd); // グループ用フィールド

                final FieldData d = new FieldData();
                d.addCenter("TTEM", getTokenList(title, 22)); // 区分1
                d.add("CONTENT1", addLineIfLessThanCount(getTokenList(getString("CAN_BE_NO_SUPPORT", data), 22), 5));
                d.add("CONTENT2", getTokenList(getString("CAN_BE_SOME_SUPPORT", data), 22));
                d.add("CONTENT3", getTokenList(getString("MEANS", data), 22));
                d.add("CONTENT4", getTokenList(getString("SHORT_TERM_GOAL", data), 22));
                d.add("CONTENT5", getTokenList(getString("GOAL_FUTURE", data), 22));
                for (int j = 1; j <= 7; j++) {
                    d.addAll("GRP" + String.valueOf(j), cd);
                }
                FieldData.svfPrintRecordList(d.getPrintRecordList(), form);
            }
        }
    }

    private static class KNJE390MG extends KNJE390M_0 {

        public KNJE390MG(final Param param) {
            super(param);
        }

        public void printMain(final Vrw32alp svf, final DB2UDB db2, final Map student) {
            final String schregno = getString("SCHREGNO", student);

            final Form form = new Form(_param, svf);
            form._form1 = "KNJE390M_G.frm";
            form._recMax1 = 999; // 43;
            form._isForm1 = true;
            form.setForm(form._form1, 4);

            form.VrsOut("PRINT_DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate)); // 印刷日
            form.VrsOut("TITLE", "引継資料(事業者)"); // タイトル

            final String courseName = StringUtils.defaultString(getString("COURSENAME", student));
            final String hrName = StringUtils.defaultString(getPrintStudentGrHrname(student));
            final String attendno = NumberUtils.isDigits(getString("ATTENDNO", student)) ? String.valueOf(Integer.parseInt(getString("ATTENDNO", student))) + "番" : "";
            final String studentName = StringUtils.defaultString(getString("NAME", student));
            form.VrsOut("NAME1", courseName + " " + hrName + " " + attendno + " " + studentName); // 氏名
            form.VrsOut("TEACHER_NAME", StringUtils.defaultString(getString("GHR_STAFFNAME", student), getString("HR_STAFFNAME", student))); // 担任名
            form.VrsOut("CONTENT_NAME1", "概要"); // 内容名
            form.VrsOut("CONTENT_NAME2", "方策"); // 内容名
            form.VrsOut("CONTENT_NAME3", "今後の展望"); // 内容名
            form.VrsOut("CONTENT_NAME4", "備考"); // 内容名

            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT ");
            sql.append("   T1.NAMECD2, ");
            sql.append("   T1.NAME1, ");
            sql.append("   T2.OUTLINE, ");
            sql.append("   T2.MEASURE, ");
            sql.append("   T2.FUTURE_DEVELOPMENT, ");
            sql.append("   T2.REMARK ");
            sql.append(" FROM NAME_MST T1 ");
            sql.append(" LEFT JOIN SCHREG_CHALLENGED_OPERATOR_TAKEOVER_DOCUMENTS_DAT T2 ON T2.YEAR = '" + _param._year + "' ");
            sql.append("   AND T2.SCHREGNO = '" + schregno + "' ");
            sql.append("   AND T2.DATA_DIV = T1.NAMECD2 ");
            sql.append(" WHERE ");
            sql.append("   T1.NAMECD1 = 'E043' ");
            sql.append("   AND (T2.RECORD_DATE IS NULL OR T2.RECORD_DATE = (SELECT MAX(RECORD_DATE) ");
            sql.append("                         FROM SCHREG_CHALLENGED_OPERATOR_TAKEOVER_MAIN_DAT ");
            sql.append("                         WHERE ");
            sql.append("                           YEAR = T2.YEAR ");
            sql.append("                           AND SCHREGNO = T2.SCHREGNO ");
            sql.append("                        ) ");
            sql.append("       ) ");
            sql.append(" ORDER BY ");
            sql.append("   T1.NAMECD2 ");

            final List e043cdList = getRowList(db2, sql.toString());
            for (final Iterator it = e043cdList.iterator(); it.hasNext();) {
                final Map data = (Map) it.next();

                final String cd = getString("NAMECD2", data);
                final String title = getString("NAME1", data);

                final FieldData d = new FieldData();
                d.addCenter("TTEM", singleton(title)); // 区分1
                d.add("CONTENT1", addLineIfLessThanCount(getTokenList(getString("OUTLINE", data), 26), 5));
                d.add("CONTENT2", getTokenList(getString("MEASURE", data), 26));
                d.add("CONTENT3", getTokenList(getString("FUTURE_DEVELOPMENT", data), 26));
                d.add("CONTENT4", getTokenList(getString("REMARK", data), 26));
                for (int j = 1; j <= 6; j++) {
                    d.addAll("GRP" + String.valueOf(j), cd);
                }
                FieldData.svfPrintRecordList(d.getPrintRecordList(), form);
            }
        }
    }

    private static class KNJE390MH extends KNJE390M_0 {
        static final SimpleDateFormat df = new SimpleDateFormat("yyyy/M/d");

        public KNJE390MH(final Param param) {
            super(param);
        }

        public void printMain(final Vrw32alp svf, final DB2UDB db2, final Map student) {
            final String schregno = getString("SCHREGNO", student);

            final Form form = new Form(_param, svf);
            form._form1 = "KNJE390M_H.frm";
            form._recMax1 = 999; // 47;
            form._isForm1 = true;
            form.setForm(form._form1, 4);

            form.VrsOut("SCHOOL_NAME", _param._knjSchoolMst._schoolName1); // 学校名
            form.VrsOut("TITLE", "関係者間資料"); // タイトル

            final String courseName = StringUtils.defaultString(getString("COURSENAME", student));
            final String hrName = StringUtils.defaultString(getPrintStudentGrHrname(student));
            final String attendno = NumberUtils.isDigits(getString("ATTENDNO", student)) ? String.valueOf(Integer.parseInt(getString("ATTENDNO", student))) + "番" : "";
            final String studentName = StringUtils.defaultString(getString("NAME", student));
            form.VrsOut("NAME1", courseName + " " + hrName + " " + attendno + " " + studentName); // 氏名

            final StringBuffer sql1 = new StringBuffer();
            sql1.append("SELECT ");
            sql1.append("   T1.* ");
            sql1.append(" FROM SCHREG_CHALLENGED_OFFICIAL_DATA_MAIN_DAT T1 ");
            sql1.append(" WHERE ");
            sql1.append("   T1.YEAR = '" + _param._year + "' ");
            sql1.append("   AND T1.SCHREGNO = '" + schregno + "' ");
            sql1.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) ");
            sql1.append("                         FROM SCHREG_CHALLENGED_OFFICIAL_DATA_MAIN_DAT ");
            sql1.append("                         WHERE ");
            sql1.append("                               YEAR = T1.YEAR ");
            sql1.append("                           AND SCHREGNO = T1.SCHREGNO ");
            sql1.append("                        ) ");

            final Map main = getFirstRow(getRowList(db2, sql1.toString()));

            form.VrsOut("ENTRY_NAME", getString("WRITER", main)); // 記入者
            form.VrsOut("MEETING_NAME", getString("MEETING_NAME", main)); // 会議名
            form.VrsOut("MEETING_TIME", dateTime(getString("MEETING_DATE", main), getString("MEETING_SHOUR", main), getString("MEETING_SMINUTES", main), getString("MEETING_EHOUR", main), getString("MEETING_EMINUTES", main))); // 打合せ日時
            form.VrsOut("MEETING_PLACE", getString("MEETING_PALCE", main)); // 打合せ場所
            svfVrListOut(form, "MEETING_PERSON", getTokenList(getString("MEETING_PARTICIPANT", main), 80)); // 打合せ参加者

            final StringBuffer sql2 = new StringBuffer();
            sql2.append("SELECT ");
            sql2.append("   T1.* ");
            sql2.append(" FROM SCHREG_CHALLENGED_OFFICIAL_DATA_DOCUMENTS_DAT T1 ");
            sql2.append(" WHERE ");
            sql2.append("   T1.YEAR = '" + _param._year + "' ");
            sql2.append("   AND T1.SCHREGNO = '" + schregno + "' ");
            sql2.append("   AND T1.RECORD_DATE = '" + getString("RECORD_DATE", main) + "' ");
            sql2.append(" ORDER BY T1.DATA_DIV ");

            final List docList = getRowList(db2, sql2.toString());
            final Map divDocumentsMap = new HashMap();
            for (final Iterator it = docList.iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                divDocumentsMap.put(getString("DATA_DIV", m), getString("DOCUMENTS", m));
            }

            for (int i = 1; i <= 7; i++) {
                final String div = "0" + String.valueOf(i);

                String item = null;
                if (i == 1) {
                    item = "問題事象";
                } else if (i == 2) {
                    item = "事象分析";
                } else if (i == 3) {
                    item = "課題対応";
                } else if (i == 4) {
                    item = "指導者の役割等";
                } else if (i == 5) {
                    item = "保護者連携";
                } else if (i == 6) {
                    item = "福祉連携";
                } else if (i == 7) {
                    item = "その他";
                }

                final FieldData d = new FieldData();
                d.addCenter("TTEM", singleton(item)); // 区分1
                d.add("CONTENT", addLineIfLessThanCount(getTokenList(getString(div, divDocumentsMap), 80), 5)); // 実態
                for (int j = 1; j <= 2; j++) {
                    d.addAll("GRP" + String.valueOf(j), div);
                }
                FieldData.svfPrintRecordList(d.getPrintRecordList(), form);
            }
        }

        private static String dateTime(final String date, final String shour, final String smin, final String ehour, final String emin) {
            final StringBuffer stb = new StringBuffer();
            if (null != date) {
                stb.append(df.format(Date.valueOf(date)));
            } else {
                stb.append("　　　　　");
            }
            stb.append(" ");
            stb.append((NumberUtils.isDigits(shour) ? String.valueOf(Integer.parseInt(shour)) : "  ") + ":" + StringUtils.defaultString(smin, "  "));
            stb.append("-");
            stb.append((NumberUtils.isDigits(ehour) ? String.valueOf(Integer.parseInt(ehour)) : "  ") + ":" + StringUtils.defaultString(emin, "  "));
            return stb.toString();
        }

    }

    // 実態表
    private static class KNJE390MI extends KNJE390M_0 {

        public KNJE390MI(final Param param) {
            super(param);
        }

        public void printMain(final Vrw32alp svf, final DB2UDB db2, final Map student) {
            if (student.size() == 0) return;
            final String schregno = getString("SCHREGNO", student);

            final Form form = new Form(_param, svf);
            form._form1 = "KNJE390M_I.frm";
            form._recMax1 = 37; // 1ページ目の行数
            form._isForm1 = true;
            form.setForm(form._form1, 4);
            form._recMax = form._recMax1; // 1ページ目の行数

            for (int i = 0; i <= 60; i++) {
                form._svf.VrAttribute("HEAD_" + String.valueOf(i), ATTR_PAINT_GRAY_FILL);
            }

            printStudent(db2, student, schregno, form);

            //実態表
            int rCnt = printJittai(db2, student, schregno, form);
            //実習
            rCnt += printTraining(db2, student, schregno, form);

            int maxRow = 27; //32行出力で、内5行は出力予定の文章分
            int blnk = maxRow - rCnt;
            for (int cnt = 0;cnt < blnk;cnt++) {
                form.VrsOut("BLANK2", "aa");
                form.VrEndRecord();
            }
            //コメント
            printComment(db2, student, form);

        }

        public void printStudent(final DB2UDB db2, final Map student, final String schregno, final Form form) {
            form.VrsOut("TITLE", "実態表");
            form.VrsOut("SCHOOL_NAME", _param._knjSchoolMst._schoolName1); // 学校名
            form.VrsOut("FACULTY_NAME", "学部"); // 学部名
            form.VrsOut("DEPARTMENT_NAME", StringUtils.defaultString(getString("COURSENAME", student))); // 学科名

            form.VrsOut("GRADE_NAME", getPrintStudentGrHrname(student)); // 学年・組

            final Map main = getAmain(db2, schregno);

            final Map updateMap = getUpdateHistMap(db2, "B", null, schregno, 1, getString("WRITING_DATE_AST", main));
            final Map recDataMap = getRecDataMap(db2, schregno);

            if (!FORM_A1.equals(_param._formA_1) && getString("WRITING_DATE_AST", main) != null) {
                if (recDataMap.containsKey("RECORD_DATE")) form.VrsOut("MAKE_DATE", !isDate(getString("RECORD_DATE", recDataMap)) ? "" : formatDate(db2, getString("RECORD_DATE", recDataMap))); // 作成日

                if (recDataMap.containsKey("RECORD_STAFFNAME")) {
                    final String[] mCutStr = KNJ_EditEdit.get_token(getString("RECORD_STAFFNAME", recDataMap), 18, 3);
                    if (mCutStr != null) {
                        for (int cnt = 0;cnt < mCutStr.length;cnt++) {
                            if (mCutStr[cnt] != null) {
                                form.VrsOutn("MAKER1", cnt+1, mCutStr[cnt]); // 作成者
                            }
                        }
                    }
                }
            }
            svfVrsOutWithSize(form, "ENTRY_PERSON", new String[] {"1", "2"}, new int[] {10, 16}, getString("UPDATE_STAFFNAME1", updateMap));  // 記入者
            form.VrsOut("UPDATE", formatDate(db2, getString("UPDATE_DATE1", updateMap))); // 更新日

            svfVrsOutWithSize(form, "KANA", new String[] {"2", "3"}, new int[] {30, 40}, getString("NAME_KANA", student)); // 氏名かな
            svfVrsOutWithSize(form, "NAME", new String[] {"2", "3"}, new int[] {26, 48}, getString("NAME", student)); // 氏名
            svfVrsOutWithSize(form, "GUARD_KANA", new String[] {"2", "3"}, new int[] {30, 40}, getString("GUARD_KANA", student)); // 氏名かな
            svfVrsOutWithSize(form, "GUARD_NAME", new String[] {"2", "3"}, new int[] {26, 48}, getString("GUARD_NAME", student)); // 保護者氏名

            form.VrsOut("SEX", getString("SEX_NAME", student)); // 性別
            form.VrsOut("BIRTHDAY", formatDate(db2, getString("BIRTHDAY", student))); // 生年月日
            form.VrsOut("GUARD_RELATION", getString("GUARD_RELATIONSHIP_NAME", student)); // 保護者続柄

            form.VrsOut("ZIP2", StringUtils.isBlank(getString("GUARD_ZIPCD", student)) ? "" : "〒" + getString("GUARD_ZIPCD", student)); // 郵便番号
            final int ad1len = KNJ_EditEdit.getMS932ByteLength(getString("GUARD_ADDR1", student));
            String adField = "";
            if ("1".equals(getString("GUARD_ADDR_FLG", student))) {
                final int ad2len = KNJ_EditEdit.getMS932ByteLength(getString("GUARD_ADDR2", student));
                adField = (ad1len > 40 || ad2len > 40) ? "_2" : "";
                form.VrsOut("ADDR2_2" + adField, getString("GUARD_ADDR2", student)); // 保護者住所
            } else {
                adField = (ad1len > 40) ? "_2" : "";
            }
            form.VrsOut("ADDR2_1" + adField, getString("GUARD_ADDR1", student)); // 保護者住所

            final String pictName = _param.getStudentImageFilePath(schregno);
            if (pictName != null) {
                form.VrsOut("PIC", pictName);    //生徒画像
            }

            form.VrsOut("TELNO1", getString("TELNO", student)); // 連絡先
            form.VrsOut("TELNO1_2", getString("TELNO2", student)); // 連絡先

            svfVrListOut(form, "DIAG_NAME", getTokenList(getString("CHALLENGED_NAMES", recDataMap), form.fieldKeta("DIAG_NAME1"))); // 障害名診断名

            form.VrsOut("HANDI_KIND", getString("CHALLENGED_CARD_CLASS_NAME", main)); // 障害・種
            form.VrsOut("HANDI_GRADE", getString("CHALLENGED_CARD_RANK_NAME", main)); // 障害・級

            form.VrsOut("CHALLENGED_CARD_NAME", getString("CHALLENGED_CARD_NAME", main)); // 精神障碍者保健福祉手帳
        }

        private boolean isDate(final String chkDStr) {
            if ("".equals(StringUtils.defaultString(chkDStr, ""))) return false;
            boolean retbl = false;
            try {
                if (chkDStr.indexOf('/') >= 0) {
                    final String[] cutStr = StringUtils.split(chkDStr, '/');
                    if (cutStr.length != 3) return false;
                    final String chkCnvStr = cutStr[0] + "/" + (cutStr[1].length() == 1 ? "0" + cutStr[1] : cutStr[1]) + "/" + (cutStr[2].length() == 1 ? "0" + cutStr[2] : cutStr[2]);
                    DateFormat df1 = new SimpleDateFormat("yyyy/MM/dd");
                    df1.setLenient(false);
                    String convStr1 = df1.format(df1.parse(chkCnvStr));
                    if (chkCnvStr.equals(convStr1)) {
                        retbl = true;
                    }
                } else {
                    final String[] cutStr = StringUtils.split(chkDStr, '-');
                    if (cutStr.length != 3) return false;
                    final String chkCnvStr = cutStr[0] + "-" + (cutStr[1].length() == 1 ? "0" + cutStr[1] : cutStr[1]) + "-" + (cutStr[2].length() == 1 ? "0" + cutStr[2] : cutStr[2]);
                    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                    df2.setLenient(false);
                    String convStr2 = df2.format(df2.parse(chkCnvStr));
                    if (chkCnvStr.equals(convStr2)) {
                        retbl = true;
                    }
                }
            } catch (ParseException p) {
                p.printStackTrace();
            }
            return retbl;
        }

        private Map getRecDataMap(final DB2UDB db2, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.* ");
            stb.append(" FROM ");
            stb.append("   SCHREG_CHALLENGED_STATUSSHEET_MAIN_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _param._year + "' ");
            stb.append("   AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append("   AND T1.RECORD_DATE = ( ");
            stb.append("                      SELECT ");
            stb.append("                        MAX(T2.RECORD_DATE) ");
            stb.append("                      FROM ");
            stb.append("                        SCHREG_CHALLENGED_STATUSSHEET_MAIN_DAT T2 ");
            stb.append("                      WHERE ");
            stb.append("                        T2.YEAR = T1.YEAR ");
            stb.append("                        AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("                     ) ");
            final Map retMap = getFirstRow(getRowList(db2, stb.toString()));
            return retMap;
        }

        public int  printJittai(final DB2UDB db2, final Map student, final String schregno, final Form form) {
            int retCnt = 0;
            //★1列/2列の実態表出力、項目名称の取得も。
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   DATA_DIV, ");
            stb.append("   STATUS_NAME, ");
            stb.append("   GROWUP_NAME, ");
            stb.append("   DATA_DIV_NAME ");
            stb.append(" FROM ");
            stb.append("   CHALLENGED_STATUSSHEET_ITEM_NAME_DAT ");
            stb.append(" WHERE ");
            stb.append("   YEAR = '" + _param._year + "' ");
            stb.append(" ORDER BY VALUE(DATA_DIV, 0) ");
            final List kmkList = KnjDbUtils.query(db2, stb.toString());

            final StringBuffer stb2 = new StringBuffer();
            stb2.append(" SELECT ");
            stb2.append("     DATA_DIV, ");
            stb2.append("     STATUS, ");
            stb2.append("     GROWUP ");
            stb2.append(" FROM ");
            stb2.append("     SCHREG_CHALLENGED_STATUSSHEET_STATUS_GROWUP_DAT ");
            stb2.append(" WHERE ");
            stb2.append("     YEAR = '" + _param._year + "' ");
            stb2.append("     AND SCHREGNO = '" + schregno + "' ");
            stb2.append("     AND RECORD_DATE = ( ");
            stb2.append("                        SELECT ");
            stb2.append("                          MAX(RECORD_DATE) ");
            stb2.append("                        FROM ");
            stb2.append("                          SCHREG_CHALLENGED_STATUSSHEET_STATUS_GROWUP_DAT ");
            stb2.append("                        WHERE ");
            stb2.append("                          YEAR = '" + _param._year + "' ");
            stb2.append("                          AND SCHREGNO = '" + schregno + "' ");
            stb2.append("                       ) ");
            stb2.append(" ORDER BY VALUE(DATA_DIV, 0) ");
            final List dList = KnjDbUtils.query(db2, stb2.toString());

            int colCnt = 1;
            String headSName = "";
            String headGName = "";
            String fldId = "5";
            int recCnt = 1;
            String grpStr = "";
            for (final Iterator ite = kmkList.iterator(); ite.hasNext();) {
                final FieldData dHed = new FieldData();
                final Map row = (Map) ite.next();
                final String div = KnjDbUtils.getString(row, "DATA_DIV");
                if ("0".equals(div)) {
                    grpStr = "00";
                    //横タイトルの出力
                    final String sName = KnjDbUtils.getString(row, "STATUS_NAME");
                    if (sName != null && !"".equals(sName)) {
                        headSName = sName;
                    }
                    final String gupName = KnjDbUtils.getString(row, "GROWUP_NAME");

                    if (gupName != null && !"".equals(gupName)) {
                        colCnt = 2;
                        headGName = gupName;
                        fldId = "6";
                    }
                    dHed.addAll("GRP" + fldId + "_1", "00"); // グループ
                    dHed.attribute("GRP" + fldId + "_1", ATTR_PAINT_GRAY_FILL); // グループ1
                    dHed.addCenterWithKeta("DIVIDE" + fldId + "_1", "項目", form); // 項目
                    dHed.attribute("GRP" + fldId + "_2", ATTR_PAINT_GRAY_FILL); // グループ1
                    dHed.addCenterWithKeta("DIVIDE" + fldId + "_2", headSName, form); // 実態
                    dHed.attribute("DIVIDE" + fldId + "_2", ATTR_CENTERING);
                    if (colCnt == 2) {
                        dHed.attribute("GRP" + fldId + "_11", ATTR_PAINT_GRAY_FILL); // グループ1
                        dHed.addCenterWithKeta("DIVIDE" + fldId + "_3", headGName, form); // 実態
                        dHed.attribute("DIVIDE" + fldId + "_3", ATTR_CENTERING);
                    }
                    retCnt++;
                } else {
                    grpStr = recCnt < 10 ? "0" + recCnt : String.valueOf(recCnt);
                    //縦項目名
                    final String ddName = KnjDbUtils.getString(row, "DATA_DIV_NAME");
                    //項目1
                    String putdat1 = "";
                    final String putdat1Wk = getDList(dList, "DATA_DIV", div, "STATUS");
                    if (putdat1Wk != null && !"".equals(putdat1Wk)) {
                        putdat1 = putdat1Wk;
                    }
                    String putdat2 = "";
                    if (colCnt == 2) {
                        //項目2
                        final String putdat2Wk = getDList(dList, "DATA_DIV", div, "GROWUP");
                        if (putdat2Wk != null && !"".equals(putdat2Wk)) {
                            putdat2 = putdat2Wk;
                        }
                    }
                    dHed.addAll("GRP" + fldId + "_1", (grpStr)); // グループ
                    dHed.attribute("GRP" + fldId + "_1", ATTR_PAINT_GRAY_FILL); // グループ1
                    dHed.addWithKeta("DIVIDE" + fldId + "_1", ddName, form); // 項目
                    final int lCnt_A = calcRowWithKeta("DIVIDE" + fldId + "_1", ddName, form);
                    dHed.addWithKeta("DIVIDE" + fldId + "_2", putdat1, form); // 実態
                    final int lCnt_B = calcRowWithKeta("DIVIDE" + fldId + "_2", putdat1, form);
                    int lCnt_C = 0;
                    if (colCnt == 2) {
                        dHed.addWithKeta("DIVIDE" + fldId + "_3", putdat2, form); // 実態
                        lCnt_C = calcRowWithKeta("DIVIDE" + fldId + "_3", putdat2, form);
                    }
                    retCnt += Math.max(lCnt_A, Math.max(lCnt_B, lCnt_C));
                    recCnt++;
                }
//                dHed.addAll("GRP" + fldId + "_1", "00"); // グループ
                dHed.addAll("GRP" + fldId + "_2", grpStr); // グループ
                dHed.addAll("GRP" + fldId + "_3", grpStr); // グループ
                dHed.addAll("GRP" + fldId + "_4", grpStr); // グループ
                dHed.addAll("GRP" + fldId + "_5", grpStr); // グループ
                dHed.addAll("GRP" + fldId + "_6", grpStr); // グループ
                dHed.addAll("GRP" + fldId + "_7", grpStr); // グループ
                dHed.addAll("GRP" + fldId + "_8", grpStr); // グループ
                dHed.addAll("GRP" + fldId + "_9", grpStr); // グループ
                dHed.addAll("GRP" + fldId + "_10", grpStr); // グループ
                dHed.addAll("GRP" + fldId + "_11", grpStr); // グループ
                dHed.addAll("GRP" + fldId + "_12", grpStr); // グループ
                dHed.addAll("GRP" + fldId + "_13", grpStr); // グループ
                dHed.addAll("GRP" + fldId + "_14", grpStr); // グループ
                dHed.addAll("GRP" + fldId + "_15", grpStr); // グループ
                dHed.addAll("GRP" + fldId + "_16", grpStr); // グループ
                dHed.addAll("GRP" + fldId + "_17", grpStr); // グループ
                FieldData.svfPrintRecordList(dHed.getPrintRecordList(), form);
            }
            return retCnt;
        }
        public int printTraining(final DB2UDB db2, final Map student, final String schregno, final Form form) {
            int retCnt = 0;
            final String fldId = "7";//FieldDataGroup
            final FieldDataGroup dg = new FieldDataGroup();
            final FieldData dHed = new FieldData();
            dHed.addAll("DIVIDE" + fldId + "_2", "実習先");
            dHed.addAll("DIVIDE" + fldId + "_3", "期間");
            dHed.addAll("DIVIDE" + fldId + "_4", "備考");
            dHed.attribute("DIVIDE" + fldId + "_2", ATTR_CENTERING);
            dHed.attribute("DIVIDE" + fldId + "_3", ATTR_CENTERING);
            dHed.attribute("DIVIDE" + fldId + "_4", ATTR_CENTERING);
            dHed.attribute("GRP" + fldId + "_1", ATTR_PAINT_GRAY_FILL); // グループ1
            dHed.attribute("GRP" + fldId + "_2", ATTR_PAINT_GRAY_FILL); // グループ1
            dHed.attribute("GRP" + fldId + "_3", ATTR_PAINT_GRAY_FILL); // グループ1
            dHed.attribute("GRP" + fldId + "_6", ATTR_PAINT_GRAY_FILL); // グループ1
            //dHed.attribute("GRP" + fldId + "_13", ATTR_PAINT_GRAY_FILL); // グループ1
            dHed.addAll("GRP" + fldId + "_1", "00"); // グループ
            dHed.addAll("GRP" + fldId + "_2", "00"); // グループ
            dHed.addAll("GRP" + fldId + "_3", "00"); // グループ
            dHed.addAll("GRP" + fldId + "_4", "00"); // グループ
            dHed.addAll("GRP" + fldId + "_5", "00"); // グループ
            dHed.addAll("GRP" + fldId + "_6", "00"); // グループ
            dHed.addAll("GRP" + fldId + "_7", "00"); // グループ
            dHed.addAll("GRP" + fldId + "_8", "00"); // グループ
            dHed.addAll("GRP" + fldId + "_9", "00"); // グループ
            dHed.addAll("GRP" + fldId + "_10", "00"); // グループ
            dHed.addAll("GRP" + fldId + "_11", "00"); // グループ
            dHed.addAll("GRP" + fldId + "_12", "00"); // グループ
            dHed.addAll("GRP" + fldId + "_13", "00"); // グループ
            dHed.addAll("GRP" + fldId + "_14", "00"); // グループ

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   COMPANY_NAME, ");
            stb.append("   START_DATE, ");
            stb.append("   FINISH_DATE, ");
            stb.append("   REMARK ");
            stb.append(" FROM ");
            stb.append("   SCHREG_CHALLENGED_STATUSSHEET_TRAINING_DAT ");
            stb.append(" WHERE ");
            stb.append("   SCHREGNO = '" + schregno + "' ");
            final List kmkList = KnjDbUtils.query(db2, stb.toString());

            final BigDecimal bVal = new BigDecimal((kmkList.size() + 1.0)/2.0).setScale(0, BigDecimal.ROUND_CEILING);
            int putTtlPt = bVal.intValue();
            if (putTtlPt == 1) {
                dHed.addAll("DIVIDE" + fldId + "_1", "実習");
            }
            dg.addFieldData(dHed);
            retCnt++;

            int recCnt = 0;
            for (final Iterator ite = kmkList.iterator(); ite.hasNext();) {
                recCnt++;
                final Map row = (Map) ite.next();
                final List companyName = getValidStrArry(KnjDbUtils.getString(row, "COMPANY_NAME"), 30, 3);
                final String strtDateWk = KnjDbUtils.getString(row, "START_DATE");
                final String strtDate = convertOmitStr(db2, strtDateWk);
                final String finDateWk = KnjDbUtils.getString(row, "FINISH_DATE");
                final String finDate = convertOmitStr(db2, finDateWk);
                final List remark = getValidStrArry(KnjDbUtils.getString(row, "REMARK"), 30, 3);
                final int mxCnt = Math.max(companyName.size(), remark.size());
                for (int sCnt = 0;sCnt < mxCnt;sCnt++) {
                    final FieldData dDat = new FieldData();
                    if (recCnt+sCnt+1 == putTtlPt) {
                        dDat.addAll("DIVIDE" + fldId + "_1", "実習");
                    }
                    if (sCnt < companyName.size()) dDat.addAll("DIVIDE" + fldId + "_2", (String)companyName.get(sCnt));
                    if (sCnt == 0) dDat.addAll("DIVIDE" + fldId + "_3", strtDate + "～" + finDate);
                    if (sCnt < remark.size())dDat.addAll("DIVIDE" + fldId + "_4", (String)remark.get(sCnt));
                    dDat.attribute("GRP" + fldId + "_1", ATTR_PAINT_GRAY_FILL); // グループ1
                    dDat.addAll("GRP" + fldId + "_1", "00"); // グループ
                    dDat.addAll("GRP" + fldId + "_2", ""+recCnt); // グループ
                    dDat.addAll("GRP" + fldId + "_3", ""+recCnt); // グループ
                    dDat.addAll("GRP" + fldId + "_4", ""+recCnt); // グループ
                    dDat.addAll("GRP" + fldId + "_5", ""+recCnt); // グループ
                    dDat.addAll("GRP" + fldId + "_6", ""+recCnt); // グループ
                    dDat.addAll("GRP" + fldId + "_7", ""+recCnt); // グループ
                    dDat.addAll("GRP" + fldId + "_8", ""+recCnt); // グループ
                    dDat.addAll("GRP" + fldId + "_9", ""+recCnt); // グループ
                    dDat.addAll("GRP" + fldId + "_10", ""+recCnt); // グループ
                    dDat.addAll("GRP" + fldId + "_11", ""+recCnt); // グループ
                    dDat.addAll("GRP" + fldId + "_12", ""+recCnt); // グループ
                    dDat.addAll("GRP" + fldId + "_13", ""+recCnt); // グループ
                    dDat.addAll("GRP" + fldId + "_14", ""+recCnt); // グループ
                    dg.addFieldData(dDat);
                    retCnt++;
                }
            }
            FieldData.svfPrintRecordList(dg.getPrintRecordList(_param), form);
            return retCnt;
        }
        public void printComment(final DB2UDB db2, final Map student, final Form form) {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     COMMENTS ");
            stb.append(" FROM ");
            stb.append("     CHALLENGED_STATUSSHEET_ITEM_NAME_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' ");
            stb.append("     AND DATA_DIV = '0' ");
            final List kmkList = KnjDbUtils.query(db2, stb.toString());
            int recCnt = 0;
            for (final Iterator ite = kmkList.iterator(); ite.hasNext();) {
                recCnt++;
                final Map row = (Map) ite.next();
                if (row != null) {
                    final String[] putStrs = KNJ_EditEdit.get_token(getString("COMMENTS", row), 104, 5);
                    if (putStrs != null) {
                        for (int cnt = 0;cnt < putStrs.length;cnt++) {
                            if (!"".equals(StringUtils.defaultString(putStrs[cnt]))) form.VrsOut("NOTICE" + (cnt+1), putStrs[cnt]);
                        }
                        form.VrEndRecord();
                    }
                }
                break;  //1データ分出力すれば良い。
            }

        }
        public int calcRowWithKeta(final String fieldname, final String data, final Form form) {
            int retCnt = 0;
            final int dLen = KNJ_EditEdit.getMS932ByteLength(data);
            if (dLen > 0) {
                retCnt = new BigDecimal(dLen*1.0 / form.fieldKeta(fieldname) * 1.0).setScale(0, BigDecimal.ROUND_CEILING).intValue();
            }
            return retCnt;
        }

        private String getDList(final List dList, final String chkElm, final String div, final String getElm) {
            String retStr = "";
            for (final Iterator ite = dList.iterator(); ite.hasNext();) {
                final Map row = (Map) ite.next();
                final String cDiv = KnjDbUtils.getString(row, chkElm);
                if (cDiv.equals(div)) {
                    retStr = KnjDbUtils.getString(row, getElm);
                    break;
                }

            }
            return retStr;
        }
        private List getValidStrArry(final String chkStr, final int len, final int cnt) {
            List retLst = new ArrayList();
            if ("".equals(StringUtils.defaultString(chkStr))) return retLst;
            final String[] cutChkStr = KNJ_EditEdit.get_token(chkStr, len, cnt);
            //一度、最後の文字列の箇所を探す(改行のみの行を考慮)。
            int chkLastPtr = 0;
            for (int cCnt = 0;cCnt < cutChkStr.length;cCnt++) {
                if (cutChkStr[cCnt] != null) {
                    chkLastPtr = cCnt;
                }
            }
            for (int gCnt = 0;gCnt <= chkLastPtr;gCnt++) {
                retLst.add(cutChkStr[gCnt] == null ? "" : cutChkStr[gCnt]);
            }
            return retLst;
        }
        private String convertOmitStr(final DB2UDB db2, final String convBaseStr) {
            if (convBaseStr == null) return "";
            String retStr = "";
            final String[] cutStr = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, convBaseStr));
            final String omitStr = KNJ_EditDate.gengouAlphabetMarkOfDate(db2, convBaseStr);
            retStr = cutStr == null || omitStr == null ? "" : omitStr + cutStr[1] + "." + cutStr[2] + "." + cutStr[3];
            return retStr;
        }

        private Map getAmain(final DB2UDB db2, final String schregno) {
            final StringBuffer sql1 = new StringBuffer();
            sql1.append("SELECT ");
            sql1.append("   T1.* ");
            sql1.append("   , NM_E035.NAME1 AS WELFARE_MEDICAL_RECEIVE_DIV_NM ");
            sql1.append("   , PFIN.FINSCHOOL_NAME AS P_FINSCHOOL_NAME ");
            sql1.append("   , JFIN.FINSCHOOL_NAME AS J_FINSCHOOL_NAME ");
            sql1.append("   , E031.NAME1 AS CHALLENGED_CARD_CLASS_NAME ");
            sql1.append("   , E032.NAME1 AS CHALLENGED_CARD_RANK_NAME ");
            sql1.append("   , E036.NAME1 AS TSUUGAKU_DIV1_NAME ");
            sql1.append("   , E036_2.NAME1 AS TSUUGAKU_DIV2_NAME ");
            sql1.append("   , E041.NAME1 AS CHALLENGED_CARD_DIV_NAME ");
            sql1.append("   , E061.NAME1 AS CHALLENGED_CARD_NAME ");
            sql1.append("   , E063.NAME1 AS CHALLENGED_CARD_REMARK_NAME ");
            sql1.append("   , CCNM1.CARDNAME AS CARDNAME1 ");
            sql1.append("   , CCNM2.CARDNAME AS CARDNAME2 ");
            sql1.append("   , CCNM3.CARDNAME AS CARDNAME3 ");
            sql1.append("   , CCNM4.CARDNAME AS CARDNAME4 ");
            sql1.append("   , CCNM5.CARDNAME AS CARDNAME5 ");
            sql1.append("   , AST.WRITING_DATE AS WRITING_DATE_AST ");
            sql1.append(" FROM SCHREG_CHALLENGED_PROFILE_MAIN_DAT T1 ");
            sql1.append(" LEFT JOIN NAME_MST NM_E035 ON NM_E035.NAMECD1 = 'E035' AND NM_E035.NAMECD2 = T1.WELFARE_MEDICAL_RECEIVE_DIV ");
            sql1.append(" LEFT JOIN FINSCHOOL_MST PFIN ON PFIN.FINSCHOOLCD = T1.P_SCHOOL_CD ");
            sql1.append(" LEFT JOIN FINSCHOOL_MST JFIN ON JFIN.FINSCHOOLCD = T1.J_SCHOOL_CD ");
            sql1.append(" LEFT JOIN NAME_MST E031 ON E031.NAMECD1 = 'E031' AND E031.NAMECD2 = T1.CHALLENGED_CARD_CLASS ");
            sql1.append(" LEFT JOIN NAME_MST E032 ON E032.NAMECD1 = 'E032' AND E032.NAMECD2 = T1.CHALLENGED_CARD_RANK ");
            sql1.append(" LEFT JOIN NAME_MST E036 ON E036.NAMECD1 = 'E036' AND E036.NAMECD2 = T1.TSUUGAKU_DIV1 ");
            sql1.append(" LEFT JOIN NAME_MST E036_2 ON E036_2.NAMECD1 = 'E036' AND E036_2.NAMECD2 = T1.TSUUGAKU_DIV2 ");
            sql1.append(" LEFT JOIN NAME_MST E041 ON E041.NAMECD1 = 'E041' AND E041.NAMECD2 = T1.CHALLENGED_CARD_DIV ");
            sql1.append(" LEFT JOIN NAME_MST E061 ON E061.NAMECD1 = 'E061' AND E061.NAMECD2 = T1.CHALLENGED_CARD_NAME ");
            sql1.append(" LEFT JOIN NAME_MST E063 ON E063.NAMECD1 = 'E063' AND E063.NAMECD2 = T1.CHALLENGED_CARD_REMARK ");
            sql1.append(" LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.REGISTERCD ");
            sql1.append(" LEFT JOIN CHALLENGED_CARD_NAME_MST CCNM1 ON CCNM1.CARDNAME_CD = T1.CHALLENGED_CARD_AREA_NAME ");
            sql1.append(" LEFT JOIN CHALLENGED_CARD_NAME_MST CCNM2 ON CCNM2.CARDNAME_CD = T1.CHALLENGED_CARD_AREA_NAME2 ");
            sql1.append(" LEFT JOIN CHALLENGED_CARD_NAME_MST CCNM3 ON CCNM3.CARDNAME_CD = T1.CHALLENGED_CARD_AREA_NAME3 ");
            sql1.append(" LEFT JOIN CHALLENGED_CARD_NAME_MST CCNM4 ON CCNM4.CARDNAME_CD = T1.CHALLENGED_CARD_AREA_NAME4 ");
            sql1.append(" LEFT JOIN CHALLENGED_CARD_NAME_MST CCNM5 ON CCNM5.CARDNAME_CD = T1.CHALLENGED_CARD_AREA_NAME5 ");
            sql1.append(" LEFT JOIN SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT AST ");
            sql1.append("        ON AST.YEAR        = '" + _param._year + "' ");
            sql1.append("       AND AST.SCHREGNO    = T1.SCHREGNO ");
            sql1.append("       AND AST.RECORD_DATE = (SELECT MAX(RECORD_DATE) ");
            sql1.append("                                FROM SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT ");
            sql1.append("                               WHERE YEAR     = AST.YEAR ");
            sql1.append("                                 AND SCHREGNO = AST.SCHREGNO ");
            sql1.append("                              )");
            sql1.append(" WHERE ");
            sql1.append("   T1.SCHREGNO = '" + schregno + "' ");
            sql1.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) FROM SCHREG_CHALLENGED_PROFILE_MAIN_DAT WHERE SCHREGNO = '" + schregno + "') ");
            if (_param._isOutputDebug) {
                log.info(" Amain sql = " + sql1.toString());
            }
            final Map main = getFirstRow(getRowList(db2, sql1.toString()));
            return main;
        }
    }


    private static class Form {
        final Param _param;
        final Vrw32alp _svf;
        String _form1;
        String _form2;
        int _form2n;
        int _recMax2;
        int _recMax1;
        int _recMax;
        int recLine;
        boolean _isForm1;
        String _currentform;
        Map _fieldInfoMap;

        private void VrAttributen(final String field, final int gyo, final String data) {
            if (_param._isOutputDebug) {
                log.info(" VrAttribute(\"" + field + "\", 「" + data + "」)");
            }
            _svf.VrAttributen(field, gyo, data);
        }

        private void VrAttribute(final String field, final String data) {
            if (_param._isOutputDebug) {
                log.info(" VrAttribute(\"" + field + "\", 「" + data + "」)");
            }
            _svf.VrAttribute(field, data);
        }

        private void VrsOut(final String field, final String data) {
            if (_param._isOutputDebug) {
                log.info(" VrsOut(\"" + field + "\", 「" + data + "」)");
            }
            _svf.VrsOut(field, data);
        }

        private void VrsOutn(final String field, final int gyo, final String data) {
            _svf.VrsOutn(field, gyo, data);
        }

        private Form(final Param param, final Vrw32alp svf) {
            _param = param;
            _svf = svf;
        }

        protected void setForm(final String form, int div) {
            _svf.VrSetForm(form, div);
            log.info(" form " + form);
            if (null == _currentform || !_currentform.equals(form)) {
                _currentform = form;
                _fieldInfoMap = SvfField.getSvfFormFieldInfoMapGroupByName(_svf);
            }
        }

        private SvfField getSvfField(final String fieldname) {
            return (SvfField) _fieldInfoMap.get(fieldname);
        }

        protected void VrsOutSelect(final String[][] fieldLists, final String data) {
            final int datasize = KNJ_EditEdit.getMS932ByteLength(data);
            String[] fieldFound = null;
            boolean output = false;
            searchField:
            for (int i = 0; i < fieldLists.length; i++) {
                final String[] fieldnameList = fieldLists[i];
                int totalKeta = 0;
                int ketaMin = -1;
                for (int j = 0; j < fieldnameList.length; j++) {
                    final String fieldname = fieldnameList[j];
                    final SvfField svfField = getSvfField(fieldname);
                    if (null == svfField) {
                        continue searchField;
                    }
                    totalKeta += svfField._fieldLength;
                    if (ketaMin == -1) {
                        ketaMin = svfField._fieldLength;
                    } else {
                        ketaMin = Math.min(ketaMin, svfField._fieldLength);
                    }
                    fieldFound = fieldnameList;
                }
                if (datasize <= totalKeta) {
                    final List tokenList = KNJ_EditKinsoku.getTokenList(data, ketaMin); // fieldListの桁数はすべて同じ前提
                    if (tokenList.size() <= fieldnameList.length) {
                        for (int j = 0; j < Math.min(tokenList.size(), fieldnameList.length); j++) {
                            VrsOut(fieldnameList[j], (String) tokenList.get(j));
                        }
                        output = true;
                        break searchField;
                    }
                }
            }
            if (!output && null != fieldFound) {
                final String[] fieldnameList = fieldFound;
                int ketaMin = -1;
                for (int j = 0; j < fieldnameList.length; j++) {
                    final String fieldname = fieldnameList[j];
                    final SvfField svfField = getSvfField(fieldname);
                    if (ketaMin == -1) {
                        ketaMin = svfField._fieldLength;
                    } else {
                        ketaMin = Math.min(ketaMin, svfField._fieldLength);
                    }
                    fieldFound = fieldnameList;
                }
                final List tokenList = KNJ_EditKinsoku.getTokenList(data, ketaMin);
                for (int j = 0; j < Math.min(tokenList.size(), fieldnameList.length); j++) {
                    VrsOut(fieldnameList[j], (String) tokenList.get(j));
                }
                output = true;
            }
        }

        private int fieldKeta(final String fieldname) {
            SvfField field = (SvfField) _fieldInfoMap.get(fieldname);
            if (null == field) {
                if (_param._isOutputDebug) {
                    log.warn("no such field : " + fieldname);
                }
                return -1;
            }
            return field._fieldLength;
        }

        private void setForm2() {
            setForm(_form2, _form2n);
            _recMax = _recMax2;
            recLine = 0;
            _isForm1 = false;
        }

        private void VrEndRecord() {
            if (_param._isOutputDebug) {
                log.info(" VrEndRecord.");
            }
            _svf.VrEndRecord();
            recLine += 1;
            if (_recMax != -1 && recLine >= _recMax && null != _form2) {
                setForm2();
            }
        }
    }

    private static class FieldData {
        static final String FIELD_DIV_NAME = "FIELD_NAME";
        final Map _fieldDivNameMap = new HashMap();
        final List _recordDataList = new LinkedList(); // List<HashMap<>>
        final Map _addAllMap = new HashMap(); // List<String, String>
        final Map _addCenterMap = new HashMap();

        private void create(final int min) {
            for (int i = _recordDataList.size(); i < min; i++) {
                _recordDataList.add(newRecord());
            }
        }

        public void setFieldDiv(final String src, final String dest) {
            _fieldDivNameMap.put(src, dest);
        }

        public static void changeField(final Map record, final Map changeFieldMap) {
            final Map newContents = newRecord0();
            for (final Iterator it = record.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final String key = (String) e.getKey();
                if (FIELD_DIV_NAME.equals(key)) {
                    final Map fieldDivNameMap = (Map) e.getValue();
                    final Map newFieldDivNameMap = new HashMap();
                    for (final Iterator fdIt = fieldDivNameMap.entrySet().iterator(); fdIt.hasNext();) {
                        final Map.Entry divEntry = (Map.Entry) fdIt.next();
                        newFieldDivNameMap.put(divEntry.getKey(), changeFieldMap.get(divEntry.getValue()));
                        newFieldDivNameMap.put(divEntry.getKey() + "__ATTRIBUTE", changeFieldMap.get(divEntry.getValue()) + "__ATTRIBUTE");
                    }
                    newContents.put(FIELD_DIV_NAME, newFieldDivNameMap);
                    continue;
                }
                if (null == changeFieldMap.get(key) && (!key.endsWith("__ATTRIBUTE") || key.endsWith("__ATTRIBUTE") && null == changeFieldMap.get(key.substring(0, key.length() - "__ATTRIBUTE".length())))) {
                    try {
                        throw new IllegalStateException("変換先無し! key = " + key);
                    } catch (Exception ex) {
                        log.warn("変換先無し! src = " + record + ", change = " + changeFieldMap, ex);
                    }
                    newContents.put(key, e.getValue());
                } else {
                    if (null == changeFieldMap.get(key)) {
                        newContents.put(changeFieldMap.get(key.substring(0, key.length() - "__ATTRIBUTE".length())) + "__ATTRIBUTE", e.getValue());
                    } else {
                        newContents.put(changeFieldMap.get(key), e.getValue());
                    }
                }
            }
            record.clear();
            record.putAll(newContents);
        }

        public void add(final String fieldname, final List dataLines) { // 文言等、dataLinesの各行のfieldnameにdataをセット
            create(dataLines.size());
            for (int i = 0; i < dataLines.size(); i++) {
                final Map record = (Map) _recordDataList.get(i);
                record.put(fieldname, dataLines.get(i));
            }
        }

        public void addWithKeta(final String fieldname, final String data, final Form form) {
            add(fieldname, KNJE390M_0.getTokenList(data, form.fieldKeta(fieldname)));
        }
        public void addWithKeta(final String fieldname, final String data, final Form form, final int gyo) {
            add(fieldname, KNJE390M_0.getTokenList(data, form.fieldKeta(fieldname), gyo));
        }
        public void addWithKetaMinLen(final String fieldname, final String data, final Form form, final int minLength) {
            add(fieldname, KNJE390M_0.minLength(KNJE390M_0.getTokenList(data, form.fieldKeta(fieldname)), minLength));
        }
        public void addCenterWithKeta(final String fieldname, final String data, final Form form) {
            addCenter(fieldname, KNJE390M_0.getTokenList(data, form.fieldKeta(fieldname)));
        }

        /**
         * 本FieldDataのすべてのfieldnameフィールドにSVF属性を指定する
         * @param fieldname フィールド名
         * @param attribute SVF属性
         */
        public void attribute(final String fieldname, final String attribute) {
            addAll(fieldname + "__ATTRIBUTE", attribute);
        }

        public void setOutputTranslate(final String fieldname, final String translateField) {
            addAll(fieldname + "__TRANSLATE_NAME", translateField);
        }

        public List getPrintRecordList() {
            create(1);
            for (final Iterator cit = _addCenterMap.entrySet().iterator(); cit.hasNext();) {
                final Map.Entry e = (Map.Entry) cit.next();
                final String fieldname = (String) e.getKey();
                final List dataLines = (List) e.getValue();
                add(fieldname, center(dataLines, _recordDataList.size()));
            }
            for (final Iterator cit = _addAllMap.entrySet().iterator(); cit.hasNext();) {
                final Map.Entry e = (Map.Entry)cit.next();
                final String fieldname = (String) e.getKey();
                final String data = (String) e.getValue();
                add(fieldname, repeat(data, _recordDataList.size()));
            }
            for (final Iterator rit = _recordDataList.iterator(); rit.hasNext();) {
                final Map record = (Map) rit.next();
                ((Map) record.get(FIELD_DIV_NAME)).putAll(_fieldDivNameMap);
            }
            //log.debug(" recordDataList = " + _recordDataList);
            return _recordDataList;
        }

        public static void svfPrintRecordList(final List recordList, final Form form) {
            for (final Iterator it = recordList.iterator(); it.hasNext();) {
                final Map record = (Map) it.next();
                svfPrintRecord(record, form);
                form.VrEndRecord();
            }
        }

        private static void svfPrintRecord(final Map record, final Form form) {
            for (final Iterator fit = record.keySet().iterator(); fit.hasNext();) {
                final String field = (String) fit.next();
                if (null == field) {
                    continue;
                }
                final Object data = record.get(field);
                final String trans = (String) record.get(field + "__TRANSLATE_NAME");
                if (data instanceof String) {
                    if (null != trans) {
                        form.VrsOut(trans, data.toString());
                    } else if (field.endsWith("__ATTRIBUTE")) {
                        form.VrAttribute(field.substring(0, field.indexOf("__ATTRIBUTE")), data.toString());
                    } else {
                        form.VrsOut(field, data.toString());
                    }
                }
            }
        }

        public void addAll(final String fieldname, final String data) { // DIV等、全ての行のfieldnameにdataをセット
            _addAllMap.put(fieldname, data);
        }
        public void addCenter(final String fieldname, final List dataLines) { // タイトル等、全ての行の中央にdataLinesをセット
            _addCenterMap.put(fieldname, dataLines);
        }

        public String toString() {
            return "FieldData(" + _recordDataList + ", all = " + _addAllMap + ", center = " + _addCenterMap + ")";
        }

        private static final Map newRecord0() {
            return new TreeMap();
        }
        protected final Map newRecord() {
            final Map r = newRecord0();
            r.put(FIELD_DIV_NAME, _fieldDivNameMap);
            return r;
        }

        static List center(final List s, final int size) { // lをsize行の中央行にセット
            final LinkedList l = new LinkedList(s);
            for (int i = 0, max = (size - l.size()) / 2; i < max; i++) {
                l.addFirst("");
            }
            for (int i = 0; i < size - l.size(); i++) {
                l.addLast("");
            }
            return l;
        }

        static List repeat(final String s, final int size) {
            final List l = new ArrayList();
            for (int i = 0; i < size; i++) {
                l.add(s);
            }
            return l;
        }
    }


    private static class FieldDataGroup {

        final List _fieldDataList = new ArrayList();
        final FieldData _super = new FieldData();

        public FieldData newFieldData() {
            FieldData d = new FieldData();
            _fieldDataList.add(d);
            return d;
        }

        public void addFieldData(final FieldData fieldData) {
            _fieldDataList.add(fieldData);
        }

        public void addFieldData(final FieldDataGroup fieldDataGroup) {
            _fieldDataList.add(fieldDataGroup);
        }

        public void setFieldDiv(final String src, final String dest) {
            _super.setFieldDiv(src, dest);
        }

        private void mergeRecordDataList(final Param param) {
            final List allChidrenDataList = new ArrayList();
            for (int fi = 0; fi < _fieldDataList.size(); fi++) {
                final Object o = _fieldDataList.get(fi);
                if (param._isOutputDebug) {
                    log.info(" FG list[" + fi + "] = " + o);
                }
                if (o instanceof FieldDataGroup) {
                    final FieldDataGroup fdg = (FieldDataGroup) o;
                    List groupChildren = fdg.getPrintRecordList(param);
                    if (param._isOutputDebug) {
                        log.info(" add FDG children size = " + groupChildren.size());
                    }
                    allChidrenDataList.addAll(groupChildren);
                } else {
                    final FieldData fd = (FieldData) o;
                    fd._fieldDivNameMap.putAll(_super._fieldDivNameMap);
                    allChidrenDataList.addAll(fd.getPrintRecordList());
                }
            }
            final int count = allChidrenDataList.size() - _super._recordDataList.size();
            for (int i = 0; i < count; i++) {
                _super._recordDataList.add(_super.newRecord());
            }
            for (int i = 0; i < allChidrenDataList.size(); i++) {
                ((Map) _super._recordDataList.get(i)).putAll((Map) allChidrenDataList.get(i));
            }
        }
        public void add(final String fieldname, final List dataLines) { // 文言等、dataLinesの各行のfieldnameにdataをセット
            _super.add(fieldname, dataLines);
        }
        public void addWithKeta(final String fieldname, final String data, final Form form) {
            _super.addWithKeta(fieldname, data, form);
        }
        public void addWithKetaMinLen(final String fieldname, final String data, final Form form, final int minLength) {
            _super.addWithKetaMinLen(fieldname, data, form, minLength);
        }
        public void addCenter(final String fieldname, final List dataLines) { // タイトル等、全ての行の中央にdataLinesをセット
            _super.addCenter(fieldname, dataLines);
        }
        public void addCenterWithKeta(final String fieldname, final String data, final Form form) {
            _super.addCenterWithKeta(fieldname, data, form);
        }
        public void addAll(final String fieldname, final String data) { // DIV等、全ての行のfieldnameにdataをセット
            _super.addAll(fieldname, data);
        }
        public void attribute(final String fieldname, final String data) {
            _super.attribute(fieldname, data);
        }

        public List getPrintRecordList(final Param param) {
            mergeRecordDataList(param);
            if (param._isOutputDebug) {
                for (final Iterator it = _super._recordDataList.iterator(); it.hasNext();) {
                    log.info(" merged : " + it.next());
                }
            }
            for (final Iterator cit = _super._addCenterMap.entrySet().iterator(); cit.hasNext();) {
                final Map.Entry e = (Map.Entry) cit.next();
                final String fieldname = (String) e.getKey();
                final List dataLines = (List) e.getValue();
                _super.add(fieldname, FieldData.center(dataLines, _super._recordDataList.size()));
            }
            for (final Iterator cit = _super._addAllMap.entrySet().iterator(); cit.hasNext();) {
                final Map.Entry e = (Map.Entry) cit.next();
                final String fieldname = (String) e.getKey();
                final String data = (String) e.getValue();
                for (final Iterator fit = _super._recordDataList.iterator(); fit.hasNext();) {
                    final Map record = (Map) fit.next();
                    record.put(fieldname, data);
                }
            }
            if (param._isOutputDebug) {
                for (int i = 0, size = _super._recordDataList.size(); i < size; i++) {
                    log.debug("  # grouped record i = " + i + " / " + size + ", " + _super._recordDataList.get(i));
                }
            }

            return _super._recordDataList;
        }
    }


    private static String getStudentSql(final Param param) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH SCHREGNOS(SCHREGNO) AS ( ");
        stb.append(" VALUES(CAST(? AS VARCHAR(8))) ");
        stb.append(" ), ADDRESS AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ZIPCD, ");
        stb.append("     T1.ADDR1, ");
        stb.append("     T1.ADDR2, ");
        stb.append("     T1.TELNO, ");
        stb.append("     T1.TELNO2 ");
        if (param._hasSCHREG_ADDRESS_DAT_TELNO_MEMO) {
            stb.append("     , T1.TELNO_MEMO ");
        } else {
            stb.append("     , CAST(NULL AS VARCHAR(1)) AS TELNO_MEMO ");
        }
        if (param._hasSCHREG_ADDRESS_DAT_TELNO2_MEMO) {
            stb.append("     , T1.TELNO2_MEMO ");
        } else {
            stb.append("     , CAST(NULL AS VARCHAR(1)) AS TELNO2_MEMO ");
        }
        stb.append("     ,T1.EMAIL ");
        stb.append(" FROM  SCHREG_ADDRESS_DAT T1 ");
        stb.append(" INNER JOIN (SELECT T1.SCHREGNO, MAX(T1.ISSUEDATE) AS ISSUEDATE ");
        stb.append("             FROM SCHREG_ADDRESS_DAT T1 ");
        stb.append("             WHERE FISCALYEAR(T1.ISSUEDATE) <= '" + param._year + "' ");
        stb.append("             GROUP BY T1.SCHREGNO ");
        stb.append("            ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ISSUEDATE = T1.ISSUEDATE ");
        stb.append(" WHERE T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHREGNOS) ");
        stb.append(" ), GUARD_ADDRESS AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GUARD_ZIPCD, ");
        stb.append("     T1.GUARD_ADDR1, ");
        stb.append("     T1.GUARD_ADDR2, ");
        stb.append("     T1.GUARD_TELNO, ");
        stb.append("     T1.GUARD_ADDR_FLG, ");
        stb.append("     T1.GUARD_TELNO2, ");
        stb.append("     T1.GUARD_E_MAIL ");
        stb.append(" FROM  GUARDIAN_ADDRESS_DAT T1 ");
        stb.append(" INNER JOIN (SELECT T1.SCHREGNO, MAX(T1.ISSUEDATE) AS ISSUEDATE ");
        stb.append("             FROM GUARDIAN_ADDRESS_DAT T1 ");
        stb.append("             WHERE FISCALYEAR(T1.ISSUEDATE) <= '" + param._year + "' ");
        stb.append("             GROUP BY T1.SCHREGNO ");
        stb.append("            ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ISSUEDATE = T1.ISSUEDATE ");
        stb.append(" WHERE T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHREGNOS) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     GDAT.GRADE_NAME1, ");
        stb.append("     T5.HR_NAMEABBV, ");
        stb.append("     T5.HR_CLASS_NAME2, ");
        stb.append("     STFHR.STAFFNAME AS HR_STAFFNAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T4.GHR_CD, ");
        stb.append("     T3.GHR_NAMEABBV, ");
        stb.append("     STFGHR.STAFFNAME AS GHR_STAFFNAME, ");
        stb.append("     T4.GHR_ATTENDNO, ");
        stb.append("     GDAT.GRADE_CD, ");
        stb.append("     GDAT.GRADE_NAME2, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.NAME_KANA, ");
        stb.append("     Z002.NAME2 AS SEX_NAME, ");
        stb.append("     T2.BIRTHDAY, ");
        stb.append("     AD1.ZIPCD, ");
        stb.append("     AD1.ADDR1, ");
        stb.append("     AD1.ADDR2, ");
        stb.append("     AD1.TELNO, ");
        stb.append("     AD1.TELNO2, ");
        stb.append("     AD1.TELNO_MEMO, ");
        stb.append("     AD1.TELNO2_MEMO, ");
        stb.append("     AD1.EMAIL, ");  //自宅EMAIL
        stb.append("     GUARD.GUARD_NAME, ");
        stb.append("     GUARD.GUARD_KANA, ");
        stb.append("     GAD1.GUARD_ZIPCD, ");
        stb.append("     GAD1.GUARD_ADDR1, ");
        stb.append("     GAD1.GUARD_ADDR2, ");
        stb.append("     GAD1.GUARD_TELNO, ");
        stb.append("     GAD1.GUARD_ADDR_FLG, ");
        stb.append("     GAD1.GUARD_TELNO2, ");      //保護者電話番号2
        stb.append("     GAD1.GUARD_E_MAIL, ");       //保護者EMAIL
        stb.append("     H201.NAME1 AS GUARD_RELATIONSHIP_NAME, ");
        stb.append("     T2.EMERGENCYTELNO, ");      //急用連絡先1_1
        stb.append("     BD012.BASE_REMARK1 AS EMERGENCYTELNO_2, ");  //急用連絡先1_2
        stb.append("     T2.EMERGENCYCALL, ");       //急用連絡先(名称)
        stb.append("     T2.EMERGENCYNAME, ");       //急用連絡先(氏名)
        stb.append("     T2.EMERGENCYRELA_NAME, ");  //急用連絡先1(続柄)
        stb.append("     T2.EMERGENCYTELNO2, ");     //急用連絡先2_1
        stb.append("     BD012.BASE_REMARK2 AS EMERGENCYTELNO2_2, ");  //急用連絡先2_2
        stb.append("     T2.EMERGENCYCALL2, ");       //急用連絡先(名称)
        stb.append("     T2.EMERGENCYNAME2, ");       //急用連絡先(氏名)
        stb.append("     T2.EMERGENCYRELA_NAME2, ");  //急用連絡先2(続柄)
        stb.append("     BD012.BASE_REMARK3 AS EMERGENCYMEMO1_1, ");
        stb.append("     BD012.BASE_REMARK4 AS EMERGENCYMEMO1_2, ");
        stb.append("     BD012.BASE_REMARK5 AS EMERGENCYMEMO2_1, ");
        stb.append("     BD012.BASE_REMARK6 AS EMERGENCYMEMO2_2, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T6.COURSENAME, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T7.MAJORNAME, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     T8.COURSECODENAME, ");
        stb.append("     A023.ABBV1 AS SCHOOL_KIND_NAME, ");
        stb.append("     JFIN.FINSCHOOL_NAME AS JUNIOR_FINSCHOOL_NAME, ");
        stb.append("     T2.BLOODTYPE ");
        stb.append(" FROM  SCHREG_REGD_DAT T1 ");
        stb.append(" INNER JOIN V_SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_REGD_GHR_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T4.YEAR = T1.YEAR ");
        stb.append("     AND T4.SEMESTER = T1.SEMESTER ");
        stb.append(" LEFT JOIN SCHREG_REGD_GHR_HDAT T3 ON T3.YEAR = T4.YEAR ");
        stb.append("     AND T3.SEMESTER = T4.SEMESTER ");
        stb.append("     AND T3.GHR_CD = T4.GHR_CD ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT T5 ON T5.YEAR = T1.YEAR ");
        stb.append("     AND T5.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T5.GRADE = T1.GRADE ");
        stb.append("     AND T5.HR_CLASS = T1.HR_CLASS ");
        stb.append(" LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
        stb.append("     AND GDAT.GRADE = T1.GRADE ");
        stb.append(" LEFT JOIN COURSE_MST T6 ON T6.COURSECD = T1.COURSECD ");
        stb.append(" LEFT JOIN MAJOR_MST T7 ON T7.COURSECD = T1.COURSECD ");
        stb.append("     AND T7.MAJORCD = T1.MAJORCD ");
        stb.append(" LEFT JOIN COURSECODE_MST T8 ON T8.COURSECODE = T1.COURSECODE ");
        stb.append(" LEFT JOIN GUARDIAN_DAT GUARD ON GUARD.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND Z002.NAMECD2 = T2.SEX ");
        stb.append(" LEFT JOIN NAME_MST H201 ON H201.NAMECD1 = 'H201' AND H201.NAMECD2 = GUARD.RELATIONSHIP ");
        stb.append(" LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' AND A023.NAME1 = GDAT.SCHOOL_KIND ");
        stb.append(" LEFT JOIN ADDRESS AD1 ON AD1.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN GUARD_ADDRESS GAD1 ON GAD1.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD_H ON ENTGRD_H.SCHREGNO = T1.SCHREGNO AND ENTGRD_H.SCHOOL_KIND = 'H' ");
        stb.append(" LEFT JOIN FINSCHOOL_MST JFIN ON JFIN.FINSCHOOLCD = ENTGRD_H.FINSCHOOLCD ");
        stb.append(" LEFT JOIN STAFF_MST STFGHR ON STFGHR.STAFFCD = T3.TR_CD1 ");
        stb.append(" LEFT JOIN STAFF_MST STFHR ON STFHR.STAFFCD = T5.TR_CD1 ");
        stb.append(" LEFT JOIN SCHREG_BASE_DETAIL_MST BD012 ON BD012.SCHREGNO = T1.SCHREGNO AND BD012.BASE_SEQ = '012' ");
        stb.append(" LEFT JOIN SCHREG_BASE_DETAIL_MST BD013 ON BD013.SCHREGNO = T1.SCHREGNO AND BD013.BASE_SEQ = '013' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + param._gakki + "'  ");
        stb.append("     AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHREGNOS) ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _gakki;
        final String _ctrlDate;
        final String _printHrClassType;
        final String[] _categorySelected;
        final String _printA;
        final String _printB;
        final String _printC;
        final String _printD;
        final String _printE;
        final String _printF;
        final String _printG;
        final String _printH;
        final String _printI;
        final String _dOutput1;
        final String _dOutput2;
        final String _dOutput3;

        final String _documentRoot;
        final String _imageDir;
        final String _imageExt;
        final boolean _isOutputDebug;
        final boolean _hasSCHREG_ADDRESS_DAT_TELNO_MEMO;
        final boolean _hasSCHREG_ADDRESS_DAT_TELNO2_MEMO;
        KNJSchoolMst _knjSchoolMst;
        final String _formA_1;
        final String _formA_2;
        final String _formB;
        final String _formD;
        final String _aoutputA1;
        final String _aoutputA2;
        final String _aoutputA3;
        final String _aoutputA4;
        final String _aoutputA5;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _gakki = request.getParameter("GAKKI");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _printHrClassType = request.getParameter("PRINT_HR_CLASS_TYPE");
            _categorySelected = request.getParameterValues("category_selected");
            _printA = request.getParameter("PRINT_A");
            _printB = request.getParameter("PRINT_B");
            _printC = request.getParameter("PRINT_C");
            _printD = request.getParameter("PRINT_D");
            _printE = request.getParameter("PRINT_E");
            _printF = request.getParameter("PRINT_F");
            _printG = request.getParameter("PRINT_G");
            _printH = request.getParameter("PRINT_H");
            _printI = request.getParameter("PRINT_I");
            _aoutputA1 = request.getParameter("A_OUTPUT1");  //生育歴
            _aoutputA2 = request.getParameter("A_OUTPUT2");  //発作
            _aoutputA3 = request.getParameter("A_OUTPUT3");  //視力
            _aoutputA4 = request.getParameter("A_OUTPUT4");  //視力
            _aoutputA5 = request.getParameter("A_OUTPUT5");  //視力
            _dOutput1 = request.getParameter("D_OUTPUT1"); // 障害名等
            _dOutput2 = request.getParameter("D_OUTPUT2"); // 実態概要・障害の特性
            _dOutput3 = request.getParameter("D_OUTPUT3"); // 検査
            _knjSchoolMst = new KNJSchoolMst(db2, _year);

            _formA_1 = StringUtils.defaultString(request.getParameter("useFormNameE390M_A_1"), "");
            _formA_2 = StringUtils.defaultString(request.getParameter("useFormNameE390M_A_2"), "");
            _formB = StringUtils.defaultString(request.getParameter("useFormNameE390M_B_1"), "");
            _formD = StringUtils.defaultString(request.getParameter("useFormNameE390M_D_1"), "");

            _documentRoot = request.getParameter("DOCUMENTROOT"); // 陰影保管場所 NO001

            _hasSCHREG_ADDRESS_DAT_TELNO_MEMO = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_ADDRESS_DAT", "TELNO_MEMO");
            _hasSCHREG_ADDRESS_DAT_TELNO2_MEMO = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_ADDRESS_DAT", "TELNO2_MEMO");

            final KNJ_Control.ReturnVal value = new KNJ_Control().Control(db2);
            _imageDir = value.val4;
            _imageExt = value.val5;
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJE390' AND NAME = '" + propName + "' "));
        }

        public String getStudentImageFilePath(final String schregno) {
            if (null == _documentRoot) {
                return null;
            } // DOCUMENTROOT
            if (null == _imageDir) {
                return null;
            }
            if (null == _imageExt) {
                return null;
            }
            StringBuffer stb = new StringBuffer();
            stb.append(_documentRoot);
            stb.append("/");
            stb.append(_imageDir);
            stb.append("/");
            stb.append("P" + schregno);
            stb.append(".");
            stb.append(_imageExt);
            final File file1 = new File(stb.toString());
            if (!file1.exists()) {
                log.fatal(" path = " + stb.toString());
                return null;
            } // 写真データ存在チェック用
            return stb.toString();
        }
    }
}

// eof

