/*
 * $Id: 6c02efe003b34a5c2f864e2eb3088ede98b7ad79 $
 *
 * 作成日: 2012/12/13
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJG;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.ShugakuDate;

/**
 * 折衝記録一括出力
 */
public class KNJTG430 {

    private static final Log log = LogFactory.getLog(KNJTG430.class);

    private boolean _hasData;
    
    private static DecimalFormat df2 = new DecimalFormat("00");

    private Param _param;

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

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);
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
    
    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
    	return KNJ_EditEdit.getMS932ByteLength(str);
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final int maxLine = 20;
        final int bikoKeta = 70;
        final List list = getShuugakuseiList(db2);
        
        if ("2".equals(_param._shuugakuNoDiv) && !StringUtils.isBlank(_param._shuugakuNoStr)) {
            // パラメータの修学生番号リスト順にソートする
            log.info("sort.");
            Collections.sort(list, new Shuugakusei.ParamOrderComparator(_param));
        }
        
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Shuugakusei shuugakusei = (Shuugakusei) it.next();
            final List pageList = shuugakusei.getPageList(maxLine, bikoKeta);
            if (pageList.size() > 1) {
                log.fatal(" shuugaku " + shuugakusei._shuugakuNo + " list size = " + shuugakusei._sessyoList.size() + " page size = " + pageList.size());
            }
            for (int pi = 0, psize = pageList.size(); pi < psize; pi++) {
                final List sessyoList = (List) pageList.get(pi);
                svf.VrSetForm("KNJTG430.frm", 1);

                svf.VrsOut("NAME", "修学生番号　" + StringUtils.defaultString(shuugakusei._shuugakuNo) + ":" + StringUtils.defaultString(shuugakusei._name)); // 生徒名
                svf.VrsOut("TITLE", "折　衝　記　録"); // タイトル
                svf.VrsOut("PRINT_DATE", _param._shugakuDate.formatDate(_param._printDate, false)); // 印刷日
                svf.VrsOut("PAGE", String.valueOf(pi + 1) + "/" + String.valueOf(psize) + "頁"); // ページ
                
                for (int li = 0, lsize = sessyoList.size(); li < lsize; li++) {
                    final int line = li + 1;
                    final Sessyo sessyo = (Sessyo) sessyoList.get(li);
                    svf.VrsOutn("EXE_DATE", line, _param._shugakuDate.formatDate(sessyo._jissiDate, false)); // 実施日
                    svf.VrsOutn("INPUT_DATE", line, _param._shugakuDate.formatDate(sessyo._inputDate, false)); // 入力日
                    svf.VrsOutn("NEGO_CODE", line, sessyo._sessyoCode); // 折衝コード
                    svf.VrsOutn("NEGO_CONTENT", line, sessyo._sessyoName); // 折衝内容
                    final List sessyoBikouList = sessyo.getSessyouBikoLineList(bikoKeta);
                    for (int bi = 0; bi < sessyoBikouList.size(); bi++) {
                        svf.VrsOutn("REMARK" + String.valueOf(bi + 1), line, sessyoBikouList.get(bi).toString()); // 備考
                    }
                }
                
                svf.VrEndPage();
                _hasData = true;
            }
        }
    }

    private static class Shuugakusei {
        final String _shuugakuNo;
        final String _name;
        final List _sessyoList = new ArrayList();
        Shuugakusei(final String shuugakuNo, final String name) {
            _shuugakuNo = shuugakuNo;
            _name = name;
        }
        
        private List getPageList(final int count, final int bikoKeta) {
            final List pageList = new ArrayList();
            List current = null;
            for (final Iterator it = _sessyoList.iterator(); it.hasNext();) {
                final Sessyo s = (Sessyo) it.next();
                if (null == current || current.size() >= count) {
                    current = new ArrayList();
                    pageList.add(current);
                }
                current.add(s);
            }
            return pageList;
        }
        
        private static class ParamOrderComparator implements Comparator {
            final Map _orderMap = new HashMap();
            public ParamOrderComparator(final Param param) {
                for (int i = 0, size = param._shuugakuNoList.size(); i < size; i++) {
                    final String shuugakuNo = (String) param._shuugakuNoList.get(i);
                    if (!_orderMap.containsKey(shuugakuNo)) {
                        _orderMap.put(shuugakuNo, new Integer(i));
                    }
                }
            }
            public Integer getOrder(final String shuugakuNo) {
                return (Integer) _orderMap.get(shuugakuNo);
            }
            public int compare(final Object o1, final Object o2) {
                final Integer ord1 = getOrder(((Shuugakusei) o1)._shuugakuNo);
                final Integer ord2 = getOrder(((Shuugakusei) o2)._shuugakuNo);
                return ord1.compareTo(ord2);
            }
        }
    }
    
    private static class Sessyo {
        final String _sessyoCode;
        final String _sessyoName;
        final String _inputDate;
        final String _jissiDate;
        final String _sessyoBikou;
        Sessyo(
            final String sessyoCode,
            final String sessyoName,
            final String inputDate,
            final String jissiDate,
            final String sessyoBikou
        ) {
            _sessyoCode = sessyoCode;
            _sessyoName = sessyoName;
            _inputDate = inputDate;
            _jissiDate = jissiDate;
            _sessyoBikou = sessyoBikou;
        }
        
        /**
         * 備考をbikoKetaで改行したリスト
         */
        public List getSessyouBikoLineList(final int bikoKeta) {
            if (StringUtils.isBlank(_sessyoBikou)) return Collections.singletonList(new StringBuffer());
            final List list = new ArrayList();
            int cnt = 0;
            StringBuffer stb = null;
            for (int i = 0; i < _sessyoBikou.length(); i++) {
                final String chs = String.valueOf(_sessyoBikou.charAt(i));
                final int k = getMS932ByteLength(chs);
                if (null == stb || cnt + k > bikoKeta) {
                    stb = new StringBuffer();
                    list.add(stb);
                    cnt = 0;
                }
                stb.append(chs);
                cnt += k;
            }
            return list;
        }
    }

    public List getShuugakuseiList(final DB2UDB db2) {
        final List shuugakuseiList = new ArrayList();
        final Map shuugakuseiMap = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                if (null == shuugakuseiMap.get(shuugakuNo)) {
                    final String name = rs.getString("NAME");
                    final Shuugakusei shuugakusei = new Shuugakusei(shuugakuNo, name);
                    shuugakuseiList.add(shuugakusei);
                    shuugakuseiMap.put(shuugakuNo, shuugakusei);
                }
                final Shuugakusei shuugakusei = (Shuugakusei) shuugakuseiMap.get(shuugakuNo);
                final String sessyoCode = rs.getString("SESSYO_CODE");
                final String sessyoName = rs.getString("SESSYO_NAME");
                final String inputDate = rs.getString("INPUT_DATE");
                final String jissiDate = rs.getString("JISSI_DATE");
                final String sessyoBikou = rs.getString("SESSYO_BIKOU");
                shuugakusei._sessyoList.add(new Sessyo(sessyoCode, sessyoName, inputDate, jissiDate, sessyoBikou));
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return shuugakuseiList;
    }

    public String sql() {
        final StringBuffer stb = new StringBuffer();
        String where = "WHERE";
        String and = "";
        stb.append(" SELECT  ");
        stb.append("      T1.SHUUGAKU_NO,  ");
        stb.append("      T3.FAMILY_NAME || '　' || T3.FIRST_NAME AS NAME,  ");
        stb.append("      T1.SESSYO_CODE,  ");
        stb.append("      T4.NAME1 AS SESSYO_NAME,  ");
        stb.append("      T1.INPUT_DATE,  ");
        stb.append("      T1.JISSI_DATE,  ");
        stb.append("      T1.SESSYO_BIKOU  ");
        stb.append("  FROM  ");
        stb.append("      HENKAN_SESSYO T1  ");
        stb.append("      LEFT JOIN SAIKEN_DAT T2 ON T1.SHUUGAKU_NO = T2.SHUUGAKU_NO  ");
        stb.append("      LEFT JOIN V_KOJIN_HIST_DAT T3 ON T2.KOJIN_NO = T3.KOJIN_NO  ");
        stb.append("      LEFT JOIN NAME_MST T4 ON T1.SESSYO_CODE = T4.NAMECD2 AND T4.NAMECD1 = 'T037'  ");
        
        if (!StringUtils.isBlank(_param._shuugakuNoStr)) {
            stb.append(where);
            stb.append(and);
            stb.append("      T1.SHUUGAKU_NO IN " + _param._shuugakuNoStr);
            where = "";
            and = " AND ";
        }
        if (!StringUtils.isEmpty(_param._searchText1)) {
            stb.append(where);
            stb.append(and);
            stb.append("      T1.SESSYO_BIKOU LIKE  '%" + _param._searchText1 + "%' ");
            where = "";
            and = " AND ";
        }
        if (!StringUtils.isEmpty(_param._searchText2)) {
            stb.append(where);
            stb.append(and);
            stb.append("      T1.SESSYO_BIKOU LIKE  '%" + _param._searchText2 + "%' ");
            where = "";
            and = " AND ";
        }
        if (!StringUtils.isBlank(_param._dateFrom) && !StringUtils.isBlank(_param._dateTo)) {
            stb.append(where);
            stb.append(and);
            stb.append("      T1.JISSI_DATE BETWEEN '" + _param._dateFrom + "' AND '" + _param._dateTo + "'  ");
            where = "";
            and = " AND ";
        } else if (!StringUtils.isBlank(_param._dateFrom)) {
            stb.append(where);
            stb.append(and);
            stb.append("      T1.JISSI_DATE >=  '" + _param._dateFrom + "' ");
            where = "";
            and = " AND ";
        } else if (!StringUtils.isBlank(_param._dateTo)) {
            stb.append(where);
            stb.append(and);
            stb.append("      T1.JISSI_DATE <=  '" + _param._dateTo + "' ");
            where = "";
            and = " AND ";
        }
        stb.append("   ORDER BY   ");
        stb.append("      T1.SHUUGAKU_NO,   ");
        stb.append("      T1.JISSI_DATE ASC,   ");
        stb.append("      T1.INPUT_DATE ASC,   ");
        stb.append("      T1.SESSYO_CODE,  ");
        stb.append("      T1.SESSYO_KEY ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 67221 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _shuugakuNoDiv;
        final List _shuugakuNoList = new ArrayList();
        final String _shuugakuNoStr;
        final String _dateFrom;
        final String _dateTo;
        final String _printDate;
        final ShugakuDate _shugakuDate;
        String _searchText1;
        String _searchText2;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _shuugakuNoDiv = request.getParameter("SHUUGAKU_NO_DIV");
            if ("2".equals(_shuugakuNoDiv)) {
                final String[] splits = StringUtils.split(request.getParameter("SHUUGAKU_NO_LIST"), ",");
                if (null == splits) {
                    _shuugakuNoStr = null;
                } else {
                    final StringBuffer stb = new StringBuffer();
                    String comma = "";
                    stb.append("(");
                    for (int i = 0; i < splits.length; i++) {
                        _shuugakuNoList.add(splits[i]);
                        stb.append(comma);
                        stb.append("'").append(splits[i]).append("'");
                        comma = ",";
                    }
                    stb.append(")");
                    _shuugakuNoStr = stb.toString();
                }
            } else {
                if (StringUtils.isBlank(request.getParameter("SHUUGAKU_NO"))) {
                    _shuugakuNoStr = null;
                } else {
                    final StringBuffer stb = new StringBuffer();
                    _shuugakuNoList.add(request.getParameter("SHUUGAKU_NO"));
                    stb.append("(");
                    stb.append("'").append(request.getParameter("SHUUGAKU_NO")).append("'");
                    stb.append(")");
                    _shuugakuNoStr = stb.toString();
                }
            }
            _shugakuDate = new ShugakuDate(db2);
            _dateFrom = _shugakuDate.d7toDateStr(request.getParameter("DATE_FROM"));
            _dateTo = _shugakuDate.d7toDateStr(request.getParameter("DATE_TO"));
            _printDate = _shugakuDate.d7toDateStr(request.getParameter("PRINT_DATE"));
            _searchText1 = "";
            if (!StringUtils.isEmpty(request.getParameter("SEARCH_TEXT1"))) {
                try {
                    _searchText1 = new String(request.getParameter("SEARCH_TEXT1").getBytes("ISO_8859_1"));
                } catch (Exception e) {
                    log.fatal("exception!", e);
                }
                log.info(" searchText1 = " + _searchText1);
            }
            if (!StringUtils.isEmpty(request.getParameter("SEARCH_TEXT2"))) {
                try {
                    _searchText2 = new String(request.getParameter("SEARCH_TEXT2").getBytes("ISO_8859_1"));
                } catch (Exception e) {
                    log.fatal("exception!", e);
                }
                log.info(" searchText2 = " + _searchText2);
            }
        }
    }
}

// eof

