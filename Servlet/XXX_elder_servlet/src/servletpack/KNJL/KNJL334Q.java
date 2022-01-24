/*
 * $Id: f85317e19f5475c408b0a380b18cf143aa1d9789 $
 *
 * 作成日: 2017/04/06
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL334Q {

    private static final Log log = LogFactory.getLog(KNJL334Q.class);

    private boolean _hasData;
    private final static String SCHOOL_KIND_P = "P";
    private final static String SCHOOL_KIND_J = "J";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List dataList = getList(db2, sql());
        final String form = "KNJL334Q.frm";
        svf.VrSetForm(form, 1);

        final int poRowMax = 6;
        int poRow = Integer.parseInt(_param._poRow); //行
        int poCol = Integer.parseInt(_param._poCol); //列

        for (int i = 0; i < dataList.size(); i++) {
            final Map row = (Map) dataList.get(i);

            if (poCol > 3) {
                poCol = 1;
                poRow++;
                if (poRow > poRowMax) {
                    svf.VrEndPage();
                    poRow = 1;
                }
            }

            final String setZip = getString(row, "ZIPCD");
            final String setMark = getMS932Bytecount(setZip) > 0 ? "〒" : "";
            if (SCHOOL_KIND_J.equals(_param._schoolKind) || SCHOOL_KIND_P.equals(_param._schoolKind)) {
                svf.VrsOutn("ZIPCODE" + poCol,        poRow, setMark + setZip);
            } else {
                svf.VrsOutn("ZIPCODE" + poCol + "_2", poRow, setMark + setZip);
            }
            final String setAddr = getString(row, "ADDRESS");
            if (SCHOOL_KIND_J.equals(_param._schoolKind) || SCHOOL_KIND_P.equals(_param._schoolKind)) {
                final String setAddrSize = KNJ_EditEdit.getMS932ByteLength(setAddr) > 58 ? "6": KNJ_EditEdit.getMS932ByteLength(setAddr) > 50 ? "5": KNJ_EditEdit.getMS932ByteLength(setAddr) > 42 ? "4": KNJ_EditEdit.getMS932ByteLength(setAddr) > 34 ? "3": KNJ_EditEdit.getMS932ByteLength(setAddr) > 26 ? "2": "1";
                svf.VrsOutn("ADDRESS" + setAddrSize + "_" + poCol, poRow, setAddr);
            } else {
                final String setAddrSize = KNJ_EditEdit.getMS932ByteLength(setAddr) > 58 ? "6": KNJ_EditEdit.getMS932ByteLength(setAddr) > 50 ? "5": KNJ_EditEdit.getMS932ByteLength(setAddr) > 42 ? "4": KNJ_EditEdit.getMS932ByteLength(setAddr) > 34 ? "3": KNJ_EditEdit.getMS932ByteLength(setAddr) > 26 ? "2": "1";
                svf.VrsOutn("ADDRESS" + setAddrSize + "_" + poCol, poRow, setAddr);
            }
            if ("3".equals(_param._finschoolDiv)) {
                if (SCHOOL_KIND_J.equals(_param._schoolKind) || SCHOOL_KIND_P.equals(_param._schoolKind)) {
                    final String setName = getString(row, "NAME") + "　様";
                    final String setNameSize = KNJ_EditEdit.getMS932ByteLength(setName) > 30 ? "3": KNJ_EditEdit.getMS932ByteLength(setName) > 24 ? "2": "1";
                    svf.VrsOutn("NAME" + poCol + "_1_" + setNameSize, poRow, setName);
                    svf.VrsOutn("NAME_FOTTER" + poCol + "_1", poRow, "保 護 者 様");
                } else {
                    final String setName = getString(row, "NAME") + "　様";
                    final String setNameSize = KNJ_EditEdit.getMS932ByteLength(setName) > 30 ? "3": KNJ_EditEdit.getMS932ByteLength(setName) > 24 ? "2": "1";
                    svf.VrsOutn("NAME" + poCol + "_1_" + setNameSize, poRow, setName);
                }
            } else {
                if (SCHOOL_KIND_P.equals(_param._schoolKind)) {
                    final String setName = getString(row, "NAME");
                    final String setNameSize = KNJ_EditEdit.getMS932ByteLength(setName) > 30 ? "3": KNJ_EditEdit.getMS932ByteLength(setName) > 24 ? "2": "1";
                    svf.VrsOutn("NAME" + poCol + "_1_" + setNameSize, poRow, setName);
                    svf.VrsOutn("NAME_FOTTER" + poCol + "_1", poRow, "園 長 殿");
                } else if (SCHOOL_KIND_J.equals(_param._schoolKind)) {
                    final String setName = getString(row, "NAME");
                    final String setNameSize = KNJ_EditEdit.getMS932ByteLength(setName) > 30 ? "3": KNJ_EditEdit.getMS932ByteLength(setName) > 24 ? "2": "1";
                    svf.VrsOutn("NAME" + poCol + "_1_" + setNameSize, poRow, setName);
                    svf.VrsOutn("NAME_FOTTER" + poCol + "_1", poRow, "校 長 様");
                } else {
                    final String setName = getString(row, "NAME") + "　御中";
                    final String setNameSize = KNJ_EditEdit.getMS932ByteLength(setName) > 30 ? "3": KNJ_EditEdit.getMS932ByteLength(setName) > 24 ? "2": "1";
                    svf.VrsOutn("NAME" + poCol + "_1_" + setNameSize, poRow, setName);
                }
            }
            svf.VrsOutn("SCHREGNO" + poCol, poRow, getString(row, "EXAMNO"));//受験番号

            poCol++;
            _hasData = true;
        }
        if (poCol > 1) {
            svf.VrEndPage();
        }
    }

    private String h_finschoolname(final String finschoolname1, final String finschoolname2, final int maxKeta) {
        final StringBuffer finschoolname = new StringBuffer();
        try {
            if (finschoolname1 != null) {
                finschoolname.append(finschoolname1);
                final byte[] SendB = finschoolname1.getBytes("MS932");

                int j = 0;
                int spclen = 2;
                int name2len;
                if ("様".equals(finschoolname2)) {
                    name2len = 2;
                    if (SendB.length > maxKeta - spclen - name2len) j = 2;

                    for (int i = SendB.length; i < (maxKeta * j - name2len); i++) {
                        finschoolname.append(" ");
                    }
                } else {
                    name2len = 4;
                    if (SendB.length > maxKeta - spclen - name2len) j = 2;

                    for (int i = SendB.length; i < (maxKeta * j - name2len); i++) {
                        finschoolname.append(" ");
                    }
                }

                if (j == 0) {
                    finschoolname.append(" ");
                    finschoolname.append(" ");
                }
                if (finschoolname2 != null) {
                    finschoolname.append(finschoolname2);
                }
            }
        } catch (Exception ex) {
            log.error("h_finschoolname error!", ex);
        }
        return finschoolname.toString();
    }

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
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

    private static String getString(final Map m, final String field) {
        if (null == m || m.isEmpty()) {
            return null;
        }
        if (!m.containsKey(field)) {
            throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
        }
        return (String) m.get(field);
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private List getList(final DB2UDB db2, final String sql) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    m.put(meta.getColumnName(i), rs.getString(meta.getColumnName(i)));
                }
                list.add(m);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        if ("2".equals(_param._finschoolDiv)) {
            //塾
            stb.append(" WITH T_CD AS ( ");
            stb.append("     SELECT ");
            stb.append("         B1.JUKUCD AS PRISCHOOLCD ");
            stb.append("     FROM ");
            stb.append("         V_ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("     WHERE ");
            stb.append("         B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
            stb.append("         AND B1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            if (SCHOOL_KIND_J.equals(_param._schoolKind) || SCHOOL_KIND_P.equals(_param._schoolKind)) {
                stb.append("         AND B1.TESTDIV = '" + _param._testDiv0 + "' ");
            } else {
                stb.append("         AND B1.TESTDIV0 = '" + _param._testDiv0 + "' ");
            }
            stb.append("     GROUP BY ");
            stb.append("         B1.JUKUCD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.PRISCHOOLCD, ");
            stb.append("     VALUE(T1.PRISCHOOL_ZIPCD,'') AS ZIPCD, ");
            stb.append("     VALUE(T1.PRISCHOOL_ADDR1,'') || VALUE(T1.PRISCHOOL_ADDR2,'') AS ADDRESS, ");
            stb.append("     VALUE(T1.PRISCHOOL_NAME,'') AS NAME, ");
            stb.append("     '' AS EXAMNO ");
            stb.append(" FROM ");
            stb.append("     PRISCHOOL_MST T1 ");
            stb.append("     INNER JOIN T_CD F1 ON F1.PRISCHOOLCD = T1.PRISCHOOLCD ");
            stb.append(" ORDER BY  ");
            stb.append("     T1.PRISCHOOLCD ");
        } else if ("3".equals(_param._finschoolDiv)) {
            //個人
            stb.append(" SELECT ");
            if ("5".equals(_param._kojinShitei)) {
                //保護者
                stb.append("     VALUE(ADDR.GZIPCD,'') AS ZIPCD, ");
                stb.append("     VALUE(ADDR.GADDRESS1,'') || VALUE(ADDR.GADDRESS2,'') AS ADDRESS, ");
                stb.append("     VALUE(ADDR.GNAME,'') AS NAME, ");
            } else {
                stb.append("     VALUE(ADDR.ZIPCD,'') AS ZIPCD, ");
                stb.append("     VALUE(ADDR.ADDRESS1,'') || VALUE(ADDR.ADDRESS2,'') AS ADDRESS, ");
                stb.append("     VALUE(BASE.NAME,'') AS NAME, ");
            }
            if (SCHOOL_KIND_P.equals(_param._schoolKind)) {
                stb.append("     'No.' || BASE.EXAMNO AS EXAMNO ");
            } else if (SCHOOL_KIND_J.equals(_param._schoolKind)) {
                stb.append("     'No.' || BASE.EXAMNO || CASE WHEN BASE.SEX = '1' THEN '※' ELSE '' END AS EXAMNO ");
            } else {
                stb.append("     '(' || BASE.EXAMNO || ')' AS EXAMNO ");
            }
            stb.append(" FROM ");
            stb.append("     V_ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("         LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR  ");
            stb.append("                  ON ADDR.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
            stb.append("                 AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("                 AND ADDR.EXAMNO       = BASE.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("         BASE.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
            stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            if (SCHOOL_KIND_J.equals(_param._schoolKind) || SCHOOL_KIND_P.equals(_param._schoolKind)) {
                stb.append("         AND BASE.TESTDIV = '" + _param._testDiv0 + "' ");
            } else {
                stb.append("         AND BASE.TESTDIV0 = '" + _param._testDiv0 + "' ");
            }
            if ("1".equals(_param._kojinShitei)) {
                stb.append("     AND BASE.JUDGEMENT = '1' ");//合格者
            } else if ("2".equals(_param._kojinShitei)) {
                stb.append("     AND BASE.JUDGEMENT = '2' ");//不合格者
            } else if ("3".equals(_param._kojinShitei)) {
                stb.append("     AND BASE.SCHOLAR_KIBOU IN ('1', '2') ");//スカラーシップ希望者
            } else if ("4".equals(_param._kojinShitei)) {
                stb.append("     AND BASE.SCHOLAR_SAIYOU = '1' ");//スカラーシップ採用者
            } else if ("5".equals(_param._kojinShitei)) {
                stb.append("     AND BASE.JUDGEMENT <> '4' ");//保護者(欠席者除く全保護者)
            }
            if (!"".equals(_param._fromExamNo) && !"".equals(_param._toExamNo)) {
                stb.append("     AND BASE.EXAMNO BETWEEN '" + _param._fromExamNo + "' AND '" + _param._toExamNo + "' ");
            } else if (!"".equals(_param._fromExamNo) && "".equals(_param._toExamNo)) {
                stb.append("     AND BASE.EXAMNO = '" + _param._fromExamNo + "' ");
            } else if ("".equals(_param._fromExamNo) && !"".equals(_param._toExamNo)) {
                stb.append("     AND BASE.EXAMNO = '" + _param._toExamNo + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     BASE.EXAMNO ");
        } else {
            //出身校
            stb.append(" WITH T_CD AS ( ");
            stb.append("     SELECT ");
            stb.append("         B1.FS_CD AS FINSCHOOLCD ");
            stb.append("     FROM ");
            stb.append("         V_ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("     WHERE ");
            stb.append("         B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
            stb.append("         AND B1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            if (SCHOOL_KIND_J.equals(_param._schoolKind) || SCHOOL_KIND_P.equals(_param._schoolKind)) {
                stb.append("         AND B1.TESTDIV = '" + _param._testDiv0 + "' ");
            } else {
                stb.append("         AND B1.TESTDIV0 = '" + _param._testDiv0 + "' ");
            }
            stb.append("     GROUP BY ");
            stb.append("         B1.FS_CD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.FINSCHOOLCD, ");
            stb.append("     T1.FINSCHOOL_TYPE, ");
            stb.append("     VALUE(T1.FINSCHOOL_ZIPCD,'') AS ZIPCD, ");
            stb.append("     VALUE(T1.FINSCHOOL_ADDR1,'') || VALUE(T1.FINSCHOOL_ADDR2,'') AS ADDRESS, ");
            stb.append("     VALUE(T1.FINSCHOOL_NAME, '') AS NAME, ");
            stb.append("     '' AS EXAMNO ");
            stb.append(" FROM ");
            stb.append("     FINSCHOOL_MST T1 ");
            stb.append("     INNER JOIN T_CD F1 ON F1.FINSCHOOLCD = T1.FINSCHOOLCD ");
            stb.append("     LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'L019' AND T2.NAMECD2 = T1.FINSCHOOL_TYPE ");
            stb.append(" WHERE ");
            if (SCHOOL_KIND_P.equals(_param._schoolKind)) {
                stb.append("     T1.FINSCHOOL_TYPE = '1' ");//1:幼稚園
            } else if (SCHOOL_KIND_J.equals(_param._schoolKind)) {
                stb.append("     T1.FINSCHOOL_TYPE = '2' ");//2:小学
            } else {
                stb.append("     T1.FINSCHOOL_TYPE = '3' ");//3:中学
            }
            stb.append(" ORDER BY ");
            stb.append("     T1.FINSCHOOLCD ");
        }
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70267 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv0;
        private final String _finschoolDiv;
        private final String _kojinShitei;
        private final String _fromExamNo;
        private final String _toExamNo;
        private final String _poRow;
        private final String _poCol;
        private final String _loginDate;

        final String _applicantdivName;
        final String _testdiv0Name1;
        final String _schoolKind;
        final String _nameMstTestDiv;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _schoolKind = request.getParameter("SCHOOLKIND");
            _nameMstTestDiv = SCHOOL_KIND_P.equals(_schoolKind) ? "LP24" : SCHOOL_KIND_J.equals(_schoolKind) ? "L024" : "L045";
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv0 = request.getParameter("TESTDIV");
            _finschoolDiv = request.getParameter("TAISYOU");//1:出身校、2:塾、3:個人
            _kojinShitei = request.getParameter("KOJIN_SHITEI");//1:合格者,2:不合格者,3:スカラーシップ希望者,4:スカラーシップ採用者,5:保護者
            _fromExamNo =request.getParameter("F_EXAMNO");
            _toExamNo =request.getParameter("T_EXAMNO");
            _poRow = request.getParameter("POROW");//行1-6
            _poCol = request.getParameter("POCOL");//列1-3
            _loginDate = request.getParameter("LOGIN_DATE");

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdiv0Name1 = StringUtils.defaultString(getNameMst(db2, "NAME1", _nameMstTestDiv, _testDiv0));
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

    }
}

// eof

