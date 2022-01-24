// kanji=漢字
/*
 * $Id: 75cbd0bf05701df42b32bde38df251e9b7367429 $
 *
 * 作成日: 2003/11/17
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/*
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＭ８１２＞  住所タックシール印刷
 *
 */

public class KNJM812 extends HttpServlet {
    private static final Log log = LogFactory.getLog(KNJM812.class);

    private static final String PRG_KNJM812A = "KNJM812A";
    private static final String PRG_KNJM812B = "KNJM812B";
    private static final String PRG_KNJM812C = "KNJM812C";
    private static final String PRG_KNJM550M = "KNJM550M";

    private static final String OUT_HOGO = "1";     // 保護者
    private static final String OUT_FUTAN = "2";    // 負担者
    private static final String OUT_SONOTA1 = "3";  // その他1
    private static final String OUT_SONOTA2 = "4";  // その他2
    private static final String OUT_SEITO = "5";    // 生徒

    private final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    private boolean _nonedata;           // 該当データなしフラグ
    private DB2UDB db2;
    private Param _param;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) {

        try {
            response.setContentType("application/pdf");

            svf.VrInit();                         //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());    //PDFファイル名の設定

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            try {
                db2.open();
            } catch (final Exception ex) {
                log.error("DB2 open error!", ex);
            }

            // パラメータの取得
            _param = createParam(db2, request);

            _nonedata = false;

            printMain();

        } catch (final Exception e) {
            log.error("exception!", e);
        } finally {
            //該当データ無しフォーム出力
            if(_nonedata == false){
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            // 終了処理
            db2.close();        // DBを閉じる
            svf.VrQuit();
        }
    }

    private static int getMS932ByteLength(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (final Exception e) {
                log.error("exception!", e);
            }
        }
        return 0;
    }

    private static String[][] split(final String[] array, final int splitCount) {
        if (array.length <= splitCount) {
            final String[][] rtn = new String[1][1];
            rtn[0] = array;
            return rtn;
        }
        final int len = array.length / splitCount + (array.length % splitCount == 0 ? 0 : 1);
        final String[][] rtn = new String[len][splitCount];
        for (int i = 0; i < len; i++) {
            final int alen = (array.length - i * splitCount) > splitCount ? splitCount : (array.length - i * splitCount);
            rtn[i] = new String[alen];
            for (int j = 0; j < alen; j++) {
                rtn[i][j] = array[j + i * splitCount];
            }
        }
        return rtn;
    }

    private String getSql(final String[] categoryName) {
        final StringBuffer stb = new StringBuffer();
        final String table, fldZipcd, fldAreacd, fldAddr1, fldAddr2, fldName;
        if (PRG_KNJM812B.equals(_param._prgid) && (OUT_SEITO.equals(_param._output) || OUT_FUTAN.equals(_param._output) || OUT_HOGO.equals(_param._output))) {
            table = "SCHREG_ADDRESS";
            fldZipcd = "ZIPCD";
            fldAreacd = "AREACD";
            if (OUT_FUTAN.equals(_param._output)) {
                fldAddr1 = "GUARANTOR_ADDR1";
                fldAddr2 = "GUARANTOR_ADDR2";
                fldName = "GUARANTOR_NAME";
            } else if (OUT_HOGO.equals(_param._output)) {
                fldAddr1 = "GUARD_ADDR1";
                fldAddr2 = "GUARD_ADDR2";
                fldName ="GUARD_NAME";
            } else { //  if (OUT_SEITO.equals(_param._output)) {
                fldAddr1 = "ADDR1";
                fldAddr2 = "ADDR2";
                fldName ="NAME";
            }
        } else if ((OUT_SONOTA1.equals(_param._output) || OUT_SONOTA2.equals(_param._output))) {
            table = "SCHREG_SEND_ADDRESS_DAT";
            fldZipcd = "SEND_ZIPCD";
            fldAreacd = "SEND_AREACD";
            fldAddr1 = "SEND_ADDR1";
            fldAddr2 = "SEND_ADDR2";
            fldName = "SEND_NAME";
        } else if (OUT_FUTAN.equals(_param._output)) {
            table = " GUARDIAN_DAT";
            fldZipcd = "t1.GUARANTOR_ZIPCD";
            fldAreacd = "''";
            fldAddr1 = "t1.GUARANTOR_ADDR1";
            fldAddr2 = "t1.GUARANTOR_ADDR2";
            fldName = "t1.GUARANTOR_NAME";
        } else if (OUT_HOGO.equals(_param._output)) {
            table =  "GUARDIAN_DAT";
            fldZipcd = "t1.GUARD_ZIPCD";
            fldAreacd = "''";
            fldAddr1 = "t1.GUARD_ADDR1";
            fldAddr2 = "t1.GUARD_ADDR2";
            fldName ="t1.GUARD_NAME";
        } else { // if (OUT_SEITO.equals(_param._output)) {
            table = "SCHREG_ADDRESS";
            fldZipcd = "ZIPCD";
            fldAreacd = "AREACD";
            fldAddr1 = "ADDR1";
            fldAddr2 = "ADDR2";
            fldName = "SCHREG_NAME";
        }

        stb.append(" WITH SCHREG_ADDRESS AS ( ");
        stb.append("   SELECT  ");
        stb.append("      T3.NAME AS SCHREG_NAME, ");
        stb.append("      T1.*  ");
        stb.append("   FROM  ");
        stb.append("      SCHREG_ADDRESS_DAT T1  ");
        stb.append("      INNER JOIN ( ");
        stb.append("        SElECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM SCHREG_ADDRESS_DAT GROUP BY SCHREGNO ");
        stb.append("      ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ISSUEDATE = T1.ISSUEDATE ");
        stb.append("      INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        if (PRG_KNJM812B.equals(_param._prgid) && "1".equals(_param._listType)) {
            stb.append(" ), SUBCLASS_STD AS ( ");
            stb.append("   SELECT DISTINCT ");
            stb.append("      T1.SCHREGNO, T1.YEAR ");
            stb.append("   FROM  ");
            stb.append("      SUBCLASS_STD_SELECT_DAT T1  ");
            stb.append("      INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
            stb.append("          AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("          AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("          AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        }
        stb.append(" ) ");

        stb.append(" , SCHRENGOS AS ( ");
        stb.append("   SELECT  ");
        stb.append("      T1.SCHREGNO ");
        stb.append("   FROM  ");
        stb.append("      SCHREG_BASE_MST T1  ");
        stb.append(" ) ");

        stb.append("SELECT ");
        stb.append("    t0.SCHREGNO, ");
        stb.append("  VALUE(" + fldZipcd + ",'') AS ZIPCD,");
        stb.append("  " + fldAddr1 + " AS ADDR1, ");
        stb.append("  " + fldAddr2 + " AS ADDR2, ");
        stb.append("  " + fldName + " AS NAME, ");
        stb.append("    t4.NAME AS NAME2 ");
        stb.append(" FROM ");
        stb.append("  SCHRENGOS t0 ");
        if ((OUT_SONOTA1.equals(_param._output) || OUT_SONOTA2.equals(_param._output))) {
            stb.append("  LEFT JOIN ");
        } else {
            stb.append("  INNER JOIN ");
        }
        stb.append("      " + table + " t1 ON t1.SCHREGNO = t0.SCHREGNO ");
        if (OUT_SONOTA1.equals(_param._output)) {
            stb.append(" AND t1.DIV = '1' ");
        } else if (OUT_SONOTA2.equals(_param._output)) {
            stb.append(" AND t1.DIV = '2' ");
        }
        stb.append("  LEFT JOIN SCHREG_REGD_DAT t2 ON t1.SCHREGNO = t2.SCHREGNO ");
        stb.append("    AND t2.YEAR = '" + _param._year + "' ");
        stb.append("    AND t2.SEMESTER = '" + _param._semester + "' ");
        stb.append("  LEFT JOIN SCHREG_REGD_HDAT t3 ON t2.GRADE || t2.HR_CLASS = t3.GRADE || t3.HR_CLASS ");
        stb.append("    AND t3.YEAR = T2.YEAR ");
        stb.append("    AND t3.SEMESTER = T2.SEMESTER ");
        stb.append("  LEFT JOIN SCHREG_BASE_MST t4 ON t0.SCHREGNO = t4.SCHREGNO ");
        if (PRG_KNJM812B.equals(_param._prgid)) {
            stb.append("  LEFT JOIN GUARDIAN_DAT t5 ON t1.SCHREGNO = t5.SCHREGNO ");
            if ("1".equals(_param._listType)) {
                stb.append("  INNER JOIN SUBCLASS_STD t6 ON t1.SCHREGNO = t6.SCHREGNO AND t2.YEAR = t6.YEAR ");
            }
        }
        stb.append("WHERE ");
        if (PRG_KNJM812B.equals(_param._prgid)) {
            if ("2".equals(_param._listType)) {
                // 住所別（郵便番号順）
                stb.append("  " + fldZipcd + " IN " + SQLUtils.whereIn(true, categoryName) + " ");
            } else if ("1".equals(_param._listType)) {
                // 地域別
                stb.append("  " + fldAreacd + " IN " + SQLUtils.whereIn(true, categoryName) + " ");
            }
        } else {
            stb.append("  t0.SCHREGNO IN " + SQLUtils.whereIn(true, categoryName) + " ");
        }

        if ("1".equals(_param._grdDiv)) {
            stb.append(" AND NOT ((T4.GRD_DIV IS NOT NULL AND T4.GRD_DIV <> '4') AND GRD_DATE < '" + _param._ctrlDate + "' ) ");
        }
        stb.append("ORDER BY ");
        if ("2".equals(_param._output2)) {
            stb.append("t2.GRADE, t2.HR_CLASS, t2.ATTENDNO, t0.SCHREGNO");
        } else {
            stb.append("t0.SCHREGNO");
        }
        return stb.toString();
    }

    public void printMain() {

    	final String form = "knjm812_2".equals(_param._formTypeM812) ? "KNJM812_2.frm" : "KNJM812.frm";
        svf.VrSetForm(form, 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        final int rowMax = "knjm812_2".equals(_param._formTypeM812) ? 6 : 8;
        final int colMax = "knjm812_2".equals(_param._formTypeM812) ? 2 : 3;
        int row = Integer.parseInt(_param._poRow);    //行
        int col = Integer.parseInt(_param._poCol);    //列

        final String[][] categoryNames = split(_param._categoryName, 40);

        int empty = 0;
        try {
            boolean hasData = false;
            log.debug(" total len = " + _param._categoryName.length);

            for (int i = 0; i < categoryNames.length; i++) {
                final String[] categoryName = categoryNames[i];
                log.debug("i = " + i + ",  len = " + categoryNames[i].length);

                final String sql = getSql(categoryName);
                log.debug("main sql="+sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String rsSchregno = rs.getString("SCHREGNO");
                    final String rsZipcd = rs.getString("ZIPCD");
                    final String rsAddr1 = rs.getString("ADDR1");
                    final String rsAddr2 = rs.getString("ADDR2");
                    final String rsName = rs.getString("NAME");
                    final String rsName2 = rs.getString("NAME2");
                    if (StringUtils.isBlank(rsZipcd) && StringUtils.isBlank(rsAddr1) && StringUtils.isBlank(rsAddr2)) {
                        // log.debug(" len = " + StringUtils.defaultString(rsAddr1).length() + ", " + StringUtils.defaultString(rsAddr2).length() + ", " + StringUtils.defaultString(rsZipcd).length());
                        if ((OUT_SONOTA1.equals(_param._output) || OUT_SONOTA2.equals(_param._output))) {
                            // その他1、その他2を選択したとき、学籍番号のみ出力する
                        } else {
                            empty += 1;
                            // log.debug(" " + empty + " skip empty address : schregno = " + rsSchregno + ", name = " + rsName);
                            continue;
                        }
                    }
                    hasData = true;
                    if(col > colMax) {
                        col = 1;
                        row++;
                        if (row > rowMax) {
                            if (hasData) {
                                svf.VrEndPage();
                                _nonedata = true;
                            }
                            row = 1;
                        }
                    }
                    //郵便番号
                    if (!StringUtils.isBlank(rsZipcd)) {
                        svf.VrsOutn("ZIPCODE"  + col, row, "〒" + rsZipcd);
                    }
                    //住所
                    if ("1".equals(_param._useAddrField2) && (getMS932ByteLength(rsAddr1) > 50 || getMS932ByteLength(rsAddr2) > 50)) {
                        svf.VrsOutn("ADDRESS"  + col + "_1_3", row, rsAddr1);
                        svf.VrsOutn("ADDRESS"  + col + "_2_3", row, rsAddr2);
                    } else if (getMS932ByteLength(rsAddr1) > 40 || getMS932ByteLength(rsAddr2) > 40){
                        svf.VrsOutn("ADDRESS"  + col + "_1_2", row, rsAddr1);
                        svf.VrsOutn("ADDRESS"  + col + "_2_2", row, rsAddr2);
                    } else if (getMS932ByteLength(rsAddr1) > 0 || getMS932ByteLength(rsAddr2) > 0) {
                        svf.VrsOutn("ADDRESS"  + col + "_1_1", row, rsAddr1);
                        svf.VrsOutn("ADDRESS"  + col + "_2_1", row, rsAddr2);
                    }
                    final String name = StringUtils.defaultString(rsName);
                    if (!StringUtils.isBlank(name)) {
                        if (getMS932ByteLength(name) > 20) {
                            svf.VrsOutn("NAME" + col + "_1", row, name);
                        } else {
                            svf.VrsOutn("NAME" + col + "_1", row, name + "　様");
                        }
                    }
                    if (OUT_SEITO.equals(_param._output) && _param._printSchregno != null) {
                        svf.VrsOutn("NAME" + col + "_2", row, rsSchregno);
                    } else if (!OUT_SEITO.equals(_param._output) && _param._printSchregName != null) {
                        //(学籍番号 氏名)
                        final String schregno = StringUtils.defaultString(rsSchregno);
                        final String name2 = StringUtils.defaultString(rsName2);
                        if (!StringUtils.isBlank(schregno + name2)) {
                            svf.VrsOutn("NAME" + col + "_2", row, "(" + schregno + " " + name2 + ")");
                        }
                    } else if ((OUT_SONOTA1.equals(_param._output) || OUT_SONOTA2.equals(_param._output))) {
                        svf.VrsOutn("NAME" + col + "_2", row, rsSchregno);
                    }
                    if (OUT_SEITO.equals(_param._output) && PRG_KNJM812B.equals(_param._prgid)) {
                        svf.VrsOutn("NAME" + col + "_3", row, "保護者（保証人）　様");
                    }
                    col++;
                }
            }
            if (hasData) {
                svf.VrEndPage();
                _nonedata = true;
                if (empty > 0) {
                    log.warn(" " + empty + " skip empty address ");
                }
            }
        } catch (final Exception ex) {
            log.error("set_detail1 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74898 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    public class Param {
        final String _year;
        final String _semester;
        final String _poRow;
        final String _poCol;
        final String _output;
        final String _output2;
        final String _printSchregno;
        final String _printSchregName;
        final String _ctrlDate;
        final String _grdDiv;
        final String _prgid;
        final String[] _categoryName;
        final String _listType;
        final String _useAddrField2;
        final String _formTypeM812;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _poRow = request.getParameter("POROW");
            _poCol = request.getParameter("POCOL");
            _listType = request.getParameter("LIST_TYPE");
            _useAddrField2 = request.getParameter("useAddrField2");

            _prgid = request.getParameter("PRGID");
            _output = request.getParameter("OUTPUT");
            _printSchregno = request.getParameter("CHECK2");     // 学籍番号印刷
            _printSchregName = request.getParameter("CHECK1");     // 生徒名出力
            _output2 = request.getParameter("OUTPUT2");           // 出力順
            _ctrlDate = request.getParameter("CTRL_DATE");   // 日付
            _grdDiv = request.getParameter("GRDDIV");        // 出力条件
            if (PRG_KNJM812A.equals(_prgid) || PRG_KNJM550M.equals(_prgid)) {
                final String[] src = request.getParameterValues("category_name");   // 年組番-学籍番号
                final String[] dst = new String[src.length];
                for (int i = 0; i < src.length; i++) {
                    dst[i] = StringUtils.split(src[i], "-")[1];
                }
                _categoryName = dst; // 学籍番号
            } else {
                _categoryName = request.getParameterValues("category_name");   // 学籍番号 or 地域コード or 郵便番号
            }
            _formTypeM812 = request.getParameter("formTypeM812");
        }
    }
}
